package com.example.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.R
import com.example.data.datasource.GovernmentSchemesCatalog
import com.example.data.local.AppDatabase
import com.example.data.local.dao.GovernmentSchemeDao
import com.example.data.local.entity.GovernmentSchemeBookmarkEntity
import com.example.data.local.entity.toEntity
import com.example.data.model.AppLanguage
import com.example.data.model.GovernmentScheme
import com.example.data.model.SchemeApplicationStatus
import com.example.data.model.isValidOfficialHttpsUrl
import com.example.util.L10n
import com.example.util.safeStartActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private const val SCHEMES_TAG = "GovtSchemesRepo"
private const val PREFS_NAME = "krishak_sathi_govt_schemes_v1"
private const val KEY_ADMIN_OVERRIDES_JSON = "admin_scheme_overrides_json"
private const val COLLECTION_GOVERNMENT_SCHEMES = "government_schemes"

class GovernmentSchemesRepository(
    context: Context,
    private val schemeDao: GovernmentSchemeDao = AppDatabase.getInstance(context.applicationContext).governmentSchemeDao(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val seedMutex = Mutex()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance(appContext.getString(R.string.firestore_database_id))
        } catch (e: Exception) {
            Log.w(SCHEMES_TAG, "Firestore unavailable, running with verified local Room cache", e)
            null
        }
    }

    private fun hasActiveFirebaseAuth(): Boolean {
        return try {
            Firebase.auth.currentUser != null
        } catch (_: Exception) {
            false
        }
    }

    private fun normalizeUserId(userId: String): String =
        userId.trim().ifBlank { "guest" }

    /**
     * Ensures the local Room cache is seeded with the verified offline catalog
     * and migrates any legacy SharedPreferences admin overrides without clobbering
     * newer Firestore-synced entries or user bookmarks.
     */
    suspend fun ensureCacheSeeded() = withContext(ioDispatcher) {
        seedMutex.withLock {
            val existing = schemeDao.getAllCachedSchemesSync()
            val existingIds = existing.map { it.id }.toSet()
            val now = System.currentTimeMillis()

            val missingCatalogEntities = GovernmentSchemesCatalog.allSchemes
                .filter { it.id !in existingIds }
                .map { it.toEntity(cachedAtMillis = now, isLocalAdminOverride = false) }

            if (missingCatalogEntities.isNotEmpty()) {
                schemeDao.upsertSchemes(missingCatalogEntities)
            }

            // One-time migration of any legacy SharedPreferences overrides into Room
            val legacyRaw = prefs.getString(KEY_ADMIN_OVERRIDES_JSON, null)
            if (!legacyRaw.isNullOrBlank()) {
                val migrated = parseLegacyOverridesJson(legacyRaw)
                    .map { it.toEntity(cachedAtMillis = now, isLocalAdminOverride = true) }
                if (migrated.isNotEmpty()) {
                    schemeDao.upsertSchemes(migrated)
                }
                prefs.edit().remove(KEY_ADMIN_OVERRIDES_JSON).apply()
            }
        }
    }

    /**
     * Offline-first stream backed by Room (`cached_government_schemes`).
     * Room emits immediately from device SQLite storage even without internet,
     * while any live Firestore updates are persisted into Room and reflected reactively.
     */
    fun observeSchemes(): Flow<List<GovernmentScheme>> = channelFlow {
        ensureCacheSeeded()

        launch {
            schemeDao.observeCachedSchemes()
                .map { entities ->
                    entities.map { it.toModel() }.sortedWith(
                        compareBy<GovernmentScheme> { it.discontinued }
                            .thenByDescending { it.northBengalPriority }
                            .thenBy { it.nameEn.lowercase() }
                    )
                }
                .collectLatest { send(it) }
        }

        launch {
            val db = firestore
            if (db != null && hasActiveFirebaseAuth()) {
                db.collection(COLLECTION_GOVERNMENT_SCHEMES).snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(GovernmentScheme::class.java)?.copy(id = doc.id)
                            } catch (e: Exception) {
                                Log.w(SCHEMES_TAG, "Skipping malformed scheme doc ${doc.id}", e)
                                null
                            }
                        }
                    }
                    .catch { error ->
                        val firestoreError = generateSequence(error) { it.cause }
                            .filterIsInstance<FirebaseFirestoreException>()
                            .firstOrNull()
                        if (firestoreError == null && error is CancellationException) throw error
                        handleForumFirestoreError(error, ForumOperationType.LIST, COLLECTION_GOVERNMENT_SCHEMES)
                    }
                    .collect { remoteSchemes ->
                        if (remoteSchemes.isNotEmpty()) {
                            val syncedAt = System.currentTimeMillis()
                            schemeDao.upsertSchemes(
                                remoteSchemes.map {
                                    it.toEntity(cachedAtMillis = syncedAt, isLocalAdminOverride = false)
                                }
                            )
                        }
                    }
            }
        }
    }.flowOn(ioDispatcher)

    fun observeBookmarkedIds(userId: String): Flow<Set<String>> =
        schemeDao.observeBookmarkedIds(normalizeUserId(userId))
            .map { it.toSet() }

    fun observeBookmarkedSchemes(userId: String): Flow<List<GovernmentScheme>> =
        schemeDao.observeBookmarkedSchemes(normalizeUserId(userId))
            .map { entities -> entities.map { it.toModel() } }

    fun observeLastCachedAtMillis(): Flow<Long?> =
        schemeDao.observeLastCachedAtMillis()

    suspend fun isBookmarked(userId: String, schemeId: String): Boolean =
        schemeDao.isBookmarkedSync(normalizeUserId(userId), schemeId)

    /**
     * Toggles offline bookmark persistence in Room for [schemeId].
     * Returns `true` if bookmarked after toggle, `false` if removed.
     */
    suspend fun toggleBookmark(userId: String, schemeId: String): Boolean {
        val uid = normalizeUserId(userId)
        val currentlySaved = schemeDao.isBookmarkedSync(uid, schemeId)
        return if (currentlySaved) {
            schemeDao.deleteBookmark(uid, schemeId)
            false
        } else {
            schemeDao.insertBookmark(
                GovernmentSchemeBookmarkEntity(
                    userId = uid,
                    schemeId = schemeId,
                    bookmarkedAtMillis = System.currentTimeMillis()
                )
            )
            true
        }
    }

    suspend fun getSchemeById(schemeId: String): GovernmentScheme? {
        ensureCacheSeeded()
        return schemeDao.getCachedSchemeById(schemeId)?.toModel()
            ?: GovernmentSchemesCatalog.allSchemes.firstOrNull { it.id == schemeId }
    }

    suspend fun saveScheme(
        scheme: GovernmentScheme,
        isNew: Boolean,
        adminUid: String,
        allowUrlOverwrite: Boolean = true
    ): Result<String> {
        val officialUrlTrimmed = scheme.officialUrl.trim()
        if (!isValidOfficialHttpsUrl(officialUrlTrimmed)) {
            return Result.failure(IllegalArgumentException("Official portal URL must start with https:// and be valid."))
        }
        val sourceUrlTrimmed = scheme.officialSourceUrl.trim().ifBlank { officialUrlTrimmed }
        if (!isValidOfficialHttpsUrl(sourceUrlTrimmed)) {
            return Result.failure(IllegalArgumentException("Official source URL must start with https:// and be valid."))
        }
        if (scheme.nameEn.isBlank() && scheme.nameBn.isBlank() && scheme.nameHi.isBlank()) {
            return Result.failure(IllegalArgumentException("Scheme title cannot be empty."))
        }

        val resolvedId = if (isNew && scheme.id.isBlank()) {
            "scheme_${UUID.randomUUID().toString().take(8)}"
        } else {
            scheme.id.trim().ifBlank { "scheme_${UUID.randomUUID().toString().take(8)}" }
        }

        val existing = getSchemeById(resolvedId)
        if (!isNew && existing != null &&
            existing.officialUrl.isNotBlank() &&
            existing.officialUrl.trim() != officialUrlTrimmed &&
            !allowUrlOverwrite
        ) {
            return Result.failure(IllegalStateException("Confirm overwriting existing verified official URL before saving."))
        }

        val sanitized = scheme.copy(
            id = resolvedId,
            officialUrl = officialUrlTrimmed,
            officialSourceUrl = sourceUrlTrimmed,
            nameEn = scheme.nameEn.ifBlank { scheme.nameBn.ifBlank { scheme.nameHi } },
            nameBn = scheme.nameBn.ifBlank { scheme.nameEn.ifBlank { scheme.nameHi } },
            nameHi = scheme.nameHi.ifBlank { scheme.nameEn.ifBlank { scheme.nameBn } },
            departmentEn = scheme.departmentEn.ifBlank { scheme.departmentBn.ifBlank { scheme.departmentHi.ifBlank { "Government Department" } } },
            departmentBn = scheme.departmentBn.ifBlank { scheme.departmentEn.ifBlank { scheme.departmentHi.ifBlank { "সরকারি দপ্তর" } } },
            departmentHi = scheme.departmentHi.ifBlank { scheme.departmentEn.ifBlank { scheme.departmentBn.ifBlank { "सरकारी विभाग" } } },
            applicationStatus = if (scheme.discontinued) SchemeApplicationStatus.DISCONTINUED.name else scheme.resolveApplicationStatus().name,
            updatedByAdmin = adminUid
        )

        // Persist immediately in Room offline cache
        schemeDao.upsertScheme(
            sanitized.toEntity(
                cachedAtMillis = System.currentTimeMillis(),
                isLocalAdminOverride = true
            )
        )

        val db = firestore
        if (db != null && hasActiveFirebaseAuth()) {
            try {
                val docRef = db.collection(COLLECTION_GOVERNMENT_SCHEMES).document(resolvedId)
                val existingDoc = docRef.get().await()
                val writeMap = sanitized.toFirestoreWriteMap(isNew = !existingDoc.exists(), adminUid = adminUid)
                if (existingDoc.exists()) {
                    docRef.update(writeMap).await()
                } else {
                    docRef.set(writeMap).await()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleForumFirestoreError(e, ForumOperationType.WRITE, "$COLLECTION_GOVERNMENT_SCHEMES/$resolvedId")
                return Result.failure(e)
            }
        }
        return Result.success(resolvedId)
    }

    suspend fun updateSchemeStatus(
        scheme: GovernmentScheme,
        newStatus: SchemeApplicationStatus,
        verifiedDate: String,
        adminUid: String
    ): Result<Unit> {
        val isDiscontinued = newStatus == SchemeApplicationStatus.DISCONTINUED
        val updated = scheme.copy(
            applicationStatus = newStatus.name,
            discontinued = isDiscontinued,
            lastVerifiedDate = verifiedDate.trim().ifBlank { scheme.lastVerifiedDate },
            updatedByAdmin = adminUid
        )
        return saveScheme(
            scheme = updated,
            isNew = false,
            adminUid = adminUid,
            allowUrlOverwrite = true
        ).map { Unit }
    }

    fun openOfficialWebsite(context: Context, url: String, lang: AppLanguage): Boolean {
        val trimmed = url.trim()
        if (!isValidOfficialHttpsUrl(trimmed)) {
            return false
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trimmed))
        return safeStartActivity(
            context = context,
            intent = intent,
            fallbackToast = L10n.t("scheme_browser_error", lang)
        )
    }

    suspend fun clearCacheForTest() {
        schemeDao.clearAllCachedSchemes()
        prefs.edit().remove(KEY_ADMIN_OVERRIDES_JSON).apply()
    }

    private fun parseLegacyOverridesJson(raw: String): List<GovernmentScheme> {
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val scheme = jsonToScheme(obj)
                    if (scheme.id.isNotBlank()) add(scheme)
                }
            }
        } catch (e: Exception) {
            Log.w(SCHEMES_TAG, "Failed migrating legacy scheme overrides", e)
            emptyList()
        }
    }

    private fun jsonToScheme(o: JSONObject): GovernmentScheme {
        fun jsonArrayToList(key: String): List<String> {
            val arr = o.optJSONArray(key) ?: return emptyList()
            return buildList {
                for (i in 0 until arr.length()) {
                    val v = arr.optString(i)
                    if (v.isNotBlank()) add(v)
                }
            }
        }
        return GovernmentScheme(
            id = o.optString("id"),
            govLevel = o.optString("govLevel"),
            category = o.optString("category"),
            extraCategories = jsonArrayToList("extraCategories"),
            farmerTypes = jsonArrayToList("farmerTypes"),
            activityFocus = o.optString("activityFocus"),
            northBengalPriority = o.optInt("northBengalPriority", 50),
            discontinued = o.optBoolean("discontinued", false),
            applicationStatus = o.optString("applicationStatus"),
            officialUrl = o.optString("officialUrl"),
            officialSourceUrl = o.optString("officialSourceUrl"),
            lastVerifiedDate = o.optString("lastVerifiedDate"),
            nameEn = o.optString("nameEn"),
            nameBn = o.optString("nameBn"),
            nameHi = o.optString("nameHi"),
            departmentEn = o.optString("departmentEn"),
            departmentBn = o.optString("departmentBn"),
            departmentHi = o.optString("departmentHi"),
            shortDescEn = o.optString("shortDescEn"),
            shortDescBn = o.optString("shortDescBn"),
            shortDescHi = o.optString("shortDescHi"),
            objectivesEn = o.optString("objectivesEn"),
            objectivesBn = o.optString("objectivesBn"),
            objectivesHi = o.optString("objectivesHi"),
            whoCanApplyEn = o.optString("whoCanApplyEn"),
            whoCanApplyBn = o.optString("whoCanApplyBn"),
            whoCanApplyHi = o.optString("whoCanApplyHi"),
            eligibilityEn = o.optString("eligibilityEn"),
            eligibilityBn = o.optString("eligibilityBn"),
            eligibilityHi = o.optString("eligibilityHi"),
            documentsEn = o.optString("documentsEn"),
            documentsBn = o.optString("documentsBn"),
            documentsHi = o.optString("documentsHi"),
            benefitsEn = o.optString("benefitsEn"),
            benefitsBn = o.optString("benefitsBn"),
            benefitsHi = o.optString("benefitsHi"),
            procedureEn = o.optString("procedureEn"),
            procedureBn = o.optString("procedureBn"),
            procedureHi = o.optString("procedureHi"),
            statusNoteEn = o.optString("statusNoteEn"),
            statusNoteBn = o.optString("statusNoteBn"),
            statusNoteHi = o.optString("statusNoteHi"),
            cropTagsEn = o.optString("cropTagsEn"),
            cropTagsBn = o.optString("cropTagsBn"),
            cropTagsHi = o.optString("cropTagsHi"),
            updatedByAdmin = o.optString("updatedByAdmin")
        )
    }
}
