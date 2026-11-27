package com.example.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.R
import com.example.data.datasource.VideoLearningCatalog
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.FarmerVideoProgress
import com.example.data.model.VideoLesson
import com.example.data.model.VideoPublicationStatus
import com.example.data.model.VideoStoryboardScene
import com.example.data.model.formatMMSS
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.storage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class UploadedVideoMediaAsset(
    val storagePath: String,
    val downloadUrl: String,
    val mimeType: String
)

/**
 * Firestore & Firebase Storage repository for KRISHAK SATHI Video Learning Centre ("কৃষি শিক্ষা").
 *
 * Security & Honesty Rules:
 * - Always resolves Firestore via `R.string.firestore_database_id` (never `(default)`).
 * - Admin write/publish/upload operations strictly validate `UserSessionManager.isAdmin(context)`.
 * - Progress/favorite writes strictly validate authenticated `uid == userId` or local offline cache.
 * - Preserves built-in 10 storyboard lessons offline and merges live cloud overrides/uploads cleanly.
 */
class VideoLearningRepository(private val context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val db: FirebaseFirestore? by lazy {
        runCatching {
            val dbId = appContext.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        }.getOrNull()
    }

    private fun hasActiveFirebaseSession(): Boolean =
        runCatching { Firebase.auth.currentUser != null }.getOrDefault(false)

    /**
     * Merges built-in curated storyboard lessons with locally cached & cloud Firestore lessons.
     * Farmers only see `PUBLISHED` lessons; Admins see all statuses (`DRAFT`, `UNDER_EXPERT_REVIEW`, `PUBLISHED`, `UNPUBLISHED`).
     */
    fun getCachedLessons(includeUnpublished: Boolean): List<VideoLesson> {
        val builtInMap = VideoLearningCatalog.getAllBuiltInLessons().associateBy { it.id }.toMutableMap()
        val cachedOverrides = loadCachedLessonsFromPrefs()
        cachedOverrides.forEach { overrideLesson ->
            val existing = builtInMap[overrideLesson.id]
            builtInMap[overrideLesson.id] = if (existing != null && overrideLesson.scenes.isEmpty()) {
                overrideLesson.copy(scenes = existing.scenes)
            } else {
                overrideLesson
            }
        }
        val sorted = builtInMap.values.sortedWith(compareBy<VideoLesson> { it.lessonNumber }.thenBy { it.id })
        return if (includeUnpublished) sorted else sorted.filter { it.status() == VideoPublicationStatus.PUBLISHED }
    }

    fun observeLessons(includeUnpublished: Boolean): Flow<List<VideoLesson>> = callbackFlow {
        // Emit cached + built-in catalog immediately so UI renders crisply even offline
        trySend(getCachedLessons(includeUnpublished))

        val firestore = db
        if (firestore == null || !hasActiveFirebaseSession()) {
            awaitClose { }
            return@callbackFlow
        }

        val query: Query = if (includeUnpublished) {
            firestore.collection(COLLECTION_LESSONS)
        } else {
            firestore.collection(COLLECTION_LESSONS)
                .whereEqualTo("publicationStatus", VideoPublicationStatus.PUBLISHED.wireValue)
        }

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Video lessons listener deferred: ${error.message}")
                trySend(getCachedLessons(includeUnpublished))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val cloudLessons = snapshot.documents.mapNotNull { doc ->
                    runCatching { docToLesson(doc.id, doc.data.orEmpty()) }.getOrNull()
                }
                if (cloudLessons.isNotEmpty()) {
                    saveCachedLessonsToPrefs(cloudLessons)
                }
                val merged = mergeCatalogWithOverrides(cloudLessons, includeUnpublished)
                trySend(merged)
            }
        }
        awaitClose { registration.remove() }
    }.catch { e ->
        if (e is CancellationException) throw e
        Log.w(TAG, "observeLessons fallback: ${e.message}")
        emit(getCachedLessons(includeUnpublished))
    }

    private fun mergeCatalogWithOverrides(
        cloudLessons: List<VideoLesson>,
        includeUnpublished: Boolean
    ): List<VideoLesson> {
        val merged = VideoLearningCatalog.getAllBuiltInLessons().associateBy { it.id }.toMutableMap()
        loadCachedLessonsFromPrefs().forEach { localItem ->
            val base = merged[localItem.id]
            merged[localItem.id] = if (base != null && localItem.scenes.isEmpty()) {
                localItem.copy(scenes = base.scenes)
            } else {
                localItem
            }
        }
        cloudLessons.forEach { cloud ->
            val base = merged[cloud.id]
            merged[cloud.id] = if (base != null && cloud.scenes.isEmpty()) {
                cloud.copy(scenes = base.scenes)
            } else {
                cloud
            }
        }
        val list = merged.values.sortedWith(compareBy<VideoLesson> { it.lessonNumber }.thenBy { it.id })
        return if (includeUnpublished) list else list.filter { it.status() == VideoPublicationStatus.PUBLISHED }
    }

    fun getLocalUserProgressMap(userId: String): Map<String, FarmerVideoProgress> {
        if (userId.isBlank()) return emptyMap()
        val raw = prefs.getString("${PREF_PROGRESS_PREFIX}$userId", null) ?: return emptyMap()
        return runCatching {
            val arr = JSONArray(raw)
            val result = mutableMapOf<String, FarmerVideoProgress>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val lessonId = obj.optString("lessonId").trim()
                if (lessonId.isEmpty()) continue
                result[lessonId] = FarmerVideoProgress(
                    id = "${userId}_$lessonId",
                    userId = userId,
                    lessonId = lessonId,
                    lastPositionSeconds = obj.optInt("lastPositionSeconds", 0),
                    totalDurationSeconds = obj.optInt("totalDurationSeconds", 0),
                    completed = obj.optBoolean("completed", false),
                    isFavorite = obj.optBoolean("isFavorite", false),
                    lastWatchedLanguage = obj.optString("lastWatchedLanguage", AppLanguage.BENGALI.code),
                    updatedAtMillis = obj.optLong("updatedAtMillis", 0L)
                )
            }
            result.toMap()
        }.getOrDefault(emptyMap())
    }

    fun observeUserProgress(userId: String): Flow<Map<String, FarmerVideoProgress>> = callbackFlow {
        trySend(getLocalUserProgressMap(userId))

        val firestore = db
        val authUid = runCatching { Firebase.auth.currentUser?.uid }.getOrNull()
        if (firestore == null || userId.isBlank() || authUid.isNullOrBlank() || authUid != userId) {
            awaitClose { }
            return@callbackFlow
        }

        val registration = firestore.collection(COLLECTION_PROGRESS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Progress listener deferred: ${error.message}")
                    trySend(getLocalUserProgressMap(userId))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val merged = getLocalUserProgressMap(userId).toMutableMap()
                    snapshot.documents.forEach { doc ->
                        val data = doc.data ?: return@forEach
                        val lessonId = (data["lessonId"] as? String).orEmpty()
                        if (lessonId.isNotBlank()) {
                            val item = FarmerVideoProgress(
                                id = doc.id,
                                userId = (data["userId"] as? String).orEmpty(),
                                lessonId = lessonId,
                                lastPositionSeconds = (data["lastPositionSeconds"] as? Number)?.toInt() ?: 0,
                                totalDurationSeconds = (data["totalDurationSeconds"] as? Number)?.toInt() ?: 0,
                                completed = data["completed"] as? Boolean ?: false,
                                isFavorite = data["isFavorite"] as? Boolean ?: false,
                                lastWatchedLanguage = (data["lastWatchedLanguage"] as? String)
                                    ?: AppLanguage.BENGALI.code,
                                updatedAtMillis = (data["updatedAtMillis"] as? Number)?.toLong() ?: 0L,
                                updatedAt = data["updatedAt"] as? Timestamp
                            )
                            val existing = merged[lessonId]
                            if (existing == null || item.updatedAtMillis >= existing.updatedAtMillis) {
                                merged[lessonId] = item
                            }
                        }
                    }
                    persistProgressMapToPrefs(userId, merged)
                    trySend(merged.toMap())
                }
            }
        awaitClose { registration.remove() }
    }.catch { e ->
        if (e is CancellationException) throw e
        emit(getLocalUserProgressMap(userId))
    }

    suspend fun saveProgress(
        userId: String,
        lessonId: String,
        positionSeconds: Int,
        totalDurationSeconds: Int,
        completed: Boolean,
        language: AppLanguage,
        favoriteOverride: Boolean? = null
    ): Result<FarmerVideoProgress> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID is required to save progress"))
        }
        val cleanLessonId = lessonId.trim()
        if (cleanLessonId.isEmpty()) {
            return Result.failure(IllegalArgumentException("Lesson ID cannot be blank"))
        }
        val currentMap = getLocalUserProgressMap(userId).toMutableMap()
        val prev = currentMap[cleanLessonId]
        val nowMillis = System.currentTimeMillis()
        val resolvedCompleted = completed || (prev?.completed == true && positionSeconds >= (totalDurationSeconds * 0.9))
        val updated = FarmerVideoProgress(
            id = "${userId}_$cleanLessonId",
            userId = userId,
            lessonId = cleanLessonId,
            lastPositionSeconds = positionSeconds.coerceAtLeast(0),
            totalDurationSeconds = totalDurationSeconds.coerceAtLeast(1),
            completed = resolvedCompleted,
            isFavorite = favoriteOverride ?: prev?.isFavorite ?: false,
            lastWatchedLanguage = language.code,
            updatedAtMillis = nowMillis
        )
        currentMap[cleanLessonId] = updated
        persistProgressMapToPrefs(userId, currentMap)

        val firestore = db
        val authUid = runCatching { Firebase.auth.currentUser?.uid }.getOrNull()
        if (firestore != null && authUid == userId) {
            try {
                val payload = mapOf(
                    "userId" to userId,
                    "lessonId" to cleanLessonId,
                    "lastPositionSeconds" to updated.lastPositionSeconds,
                    "totalDurationSeconds" to updated.totalDurationSeconds,
                    "completed" to updated.completed,
                    "isFavorite" to updated.isFavorite,
                    "lastWatchedLanguage" to updated.lastWatchedLanguage,
                    "updatedAtMillis" to updated.updatedAtMillis,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection(COLLECTION_PROGRESS)
                    .document("${userId}_$cleanLessonId")
                    .set(payload, SetOptions.merge())
                    .await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Cloud progress sync deferred offline: ${e.message}")
            }
        }
        return Result.success(updated)
    }

    suspend fun toggleFavorite(
        userId: String,
        lesson: VideoLesson,
        language: AppLanguage
    ): Result<FarmerVideoProgress> {
        val current = getLocalUserProgressMap(userId)[lesson.id]
        val nextFav = !(current?.isFavorite ?: false)
        return saveProgress(
            userId = userId,
            lessonId = lesson.id,
            positionSeconds = current?.lastPositionSeconds ?: 0,
            totalDurationSeconds = current?.totalDurationSeconds ?: lesson.durationSeconds,
            completed = current?.completed ?: false,
            language = language,
            favoriteOverride = nextFav
        )
    }

    /**
     * Admin-only save/update of a video lesson & its metadata/URLs.
     */
    suspend fun saveOrUpdateLesson(lesson: VideoLesson): Result<VideoLesson> {
        if (!UserSessionManager.isAdmin(appContext)) {
            return Result.failure(SecurityException("Only verified KRISHAK SATHI admins can modify video lessons."))
        }
        val cleanId = lesson.id.ifBlank { "vid_custom_${UUID.randomUUID().toString().take(8)}" }
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val adminEmail = UserSessionManager.getSession(appContext)?.email.orEmpty()
        val sanitized = lesson.copy(
            id = cleanId,
            updatedByAdminEmail = adminEmail
        )

        // Update local cache immediately
        val currentCache = loadCachedLessonsFromPrefs().associateBy { it.id }.toMutableMap()
        currentCache[cleanId] = sanitized
        saveCachedLessonsToPrefs(currentCache.values.toList())

        val firestore = db
        val fbUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (firestore == null || fbUser == null) {
            return Result.failure(IllegalStateException("Google Admin sign-in required to sync lesson changes to cloud."))
        }

        return try {
            val payload = lessonToFirestoreMap(sanitized)
            firestore.collection(COLLECTION_LESSONS)
                .document(cleanId)
                .set(payload, SetOptions.merge())
                .await()
            Result.success(sanitized)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync lesson to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun setPublicationStatus(
        lesson: VideoLesson,
        newStatus: VideoPublicationStatus,
        expertReviewed: Boolean,
        reviewedBy: String,
        reviewedDate: String
    ): Result<VideoLesson> {
        if (!UserSessionManager.isAdmin(appContext)) {
            return Result.failure(SecurityException("Only verified admins can publish or unpublish lessons."))
        }
        val updated = lesson.copy(
            publicationStatus = newStatus.wireValue,
            isReviewedByExpert = expertReviewed,
            reviewedByName = reviewedBy.ifBlank { "Krishak Sathi Admin Review" },
            lastReviewedDate = reviewedDate
        )
        return saveOrUpdateLesson(updated)
    }

    /**
     * Admin-only Firebase Storage upload for a real video stream (.mp4), audio narration (.mp3/.m4a), or subtitle (.vtt/.srt).
     */
    suspend fun uploadAdminMediaFile(
        lessonId: String,
        uri: Uri,
        mediaSlot: String, // "video", "audio_bn", "audio_hi", "audio_en", "sub_bn", "sub_hi", "sub_en"
        mimeTypeHint: String
    ): Result<UploadedVideoMediaAsset> = withContext(Dispatchers.IO) {
        if (!UserSessionManager.isAdmin(appContext)) {
            return@withContext Result.failure(SecurityException("Admin verification required for media upload."))
        }
        val fbUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (fbUser == null || fbUser.uid.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Please sign in with Admin Google account before uploading files."))
        }
        val cleanId = lessonId.ifBlank { "draft_${UUID.randomUUID().toString().take(8)}" }
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val ext = when {
            mediaSlot == "video" -> "mp4"
            mediaSlot.startsWith("audio_") -> "m4a"
            mediaSlot.startsWith("sub_") -> "vtt"
            else -> "bin"
        }
        val storagePath = "video_lessons/$cleanId/${mediaSlot}_${UUID.randomUUID().toString().take(8)}.$ext"
        return@withContext try {
            val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(IllegalStateException("Unable to read selected file stream."))
            require(bytes.isNotEmpty()) { "Selected file is empty." }
            val maxAllowedBytes = if (mediaSlot == "video") 120 * 1024 * 1024 else 25 * 1024 * 1024
            require(bytes.size <= maxAllowedBytes) {
                "File size (${bytes.size / (1024 * 1024)} MB) exceeds safe mobile upload limit (${maxAllowedBytes / (1024 * 1024)} MB)."
            }
            val ref = Firebase.storage.reference.child(storagePath)
            ref.putBytes(bytes).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(
                UploadedVideoMediaAsset(
                    storagePath = storagePath,
                    downloadUrl = url,
                    mimeType = mimeTypeHint
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Admin media upload failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Admin-only compressed thumbnail upload via Photo Picker (zero storage permissions).
     */
    suspend fun uploadAdminThumbnail(lessonId: String, uri: Uri): Result<UploadedVideoMediaAsset> {
        if (!UserSessionManager.isAdmin(appContext)) {
            return Result.failure(SecurityException("Admin verification required for thumbnail upload."))
        }
        val fbUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (fbUser == null || fbUser.uid.isBlank()) {
            return Result.failure(IllegalStateException("Admin Google Sign-In required before cloud upload."))
        }
        val compressed = MachineryImageStorageManager.compressUriToJpeg(appContext, uri).getOrElse {
            return Result.failure(it)
        }
        val cleanId = lessonId.ifBlank { "draft_${UUID.randomUUID().toString().take(8)}" }
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val storagePath = "video_lessons/$cleanId/thumb_${UUID.randomUUID().toString().take(8)}.jpg"
        return try {
            val ref = Firebase.storage.reference.child(storagePath)
            ref.putBytes(compressed).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(
                UploadedVideoMediaAsset(
                    storagePath = storagePath,
                    downloadUrl = url,
                    mimeType = "image/jpeg"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Thumbnail upload failed: ${e.message}")
            Result.failure(e)
        }
    }

    private fun lessonToFirestoreMap(lesson: VideoLesson): Map<String, Any> {
        val scenesPayload = lesson.scenes.map { scene ->
            mapOf(
                "sceneNumber" to scene.sceneNumber,
                "startSeconds" to scene.startSeconds,
                "endSeconds" to scene.endSeconds,
                "titleBn" to scene.titleBn,
                "titleHi" to scene.titleHi,
                "titleEn" to scene.titleEn,
                "visualDirection" to scene.visualDirection,
                "animationDirection" to scene.animationDirection,
                "diagramCue" to scene.diagramCue,
                "narrationBn" to scene.narrationBn,
                "narrationHi" to scene.narrationHi,
                "narrationEn" to scene.narrationEn,
                "subtitleBn" to scene.subtitleBn,
                "subtitleHi" to scene.subtitleHi,
                "subtitleEn" to scene.subtitleEn
            )
        }
        return mapOf(
            "lessonNumber" to lesson.lessonNumber,
            "categoryCode" to lesson.categoryCode,
            "stageCode" to lesson.stageCode,
            "linkedCropId" to lesson.linkedCropId,
            "cropTagsBn" to lesson.cropTagsBn,
            "cropTagsHi" to lesson.cropTagsHi,
            "cropTagsEn" to lesson.cropTagsEn,
            "titleBn" to lesson.titleBn,
            "titleHi" to lesson.titleHi,
            "titleEn" to lesson.titleEn,
            "objectiveBn" to lesson.objectiveBn,
            "objectiveHi" to lesson.objectiveHi,
            "objectiveEn" to lesson.objectiveEn,
            "summaryBn" to lesson.summaryBn,
            "summaryHi" to lesson.summaryHi,
            "summaryEn" to lesson.summaryEn,
            "durationSeconds" to lesson.durationSeconds,
            "thumbnailSpec" to lesson.thumbnailSpec,
            "thumbnailDrawableKey" to lesson.thumbnailDrawableKey,
            "thumbnailUrl" to lesson.thumbnailUrl,
            "videoUrl" to lesson.videoUrl,
            "audioUrlBn" to lesson.audioUrlBn,
            "audioUrlHi" to lesson.audioUrlHi,
            "audioUrlEn" to lesson.audioUrlEn,
            "subtitleUrlBn" to lesson.subtitleUrlBn,
            "subtitleUrlHi" to lesson.subtitleUrlHi,
            "subtitleUrlEn" to lesson.subtitleUrlEn,
            "publicationStatus" to lesson.publicationStatus,
            "isReviewedByExpert" to lesson.isReviewedByExpert,
            "reviewedByName" to lesson.reviewedByName,
            "lastReviewedDate" to lesson.lastReviewedDate,
            "contentSource" to lesson.contentSource,
            "guidanceScope" to lesson.guidanceScope,
            "scenes" to scenesPayload,
            "isBuiltInCatalog" to lesson.isBuiltInCatalog,
            "updatedByAdminEmail" to lesson.updatedByAdminEmail,
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun docToLesson(docId: String, data: Map<String, Any>): VideoLesson {
        val rawScenes = data["scenes"] as? List<Map<String, Any>> ?: emptyList()
        val parsedScenes = rawScenes.mapIndexed { idx, m ->
            VideoStoryboardScene(
                sceneNumber = (m["sceneNumber"] as? Number)?.toInt() ?: (idx + 1),
                startSeconds = (m["startSeconds"] as? Number)?.toInt() ?: 0,
                endSeconds = (m["endSeconds"] as? Number)?.toInt() ?: 30,
                titleBn = (m["titleBn"] as? String).orEmpty(),
                titleHi = (m["titleHi"] as? String).orEmpty(),
                titleEn = (m["titleEn"] as? String).orEmpty(),
                visualDirection = (m["visualDirection"] as? String).orEmpty(),
                animationDirection = (m["animationDirection"] as? String).orEmpty(),
                diagramCue = (m["diagramCue"] as? String).orEmpty(),
                narrationBn = (m["narrationBn"] as? String).orEmpty(),
                narrationHi = (m["narrationHi"] as? String).orEmpty(),
                narrationEn = (m["narrationEn"] as? String).orEmpty(),
                subtitleBn = (m["subtitleBn"] as? String).orEmpty(),
                subtitleHi = (m["subtitleHi"] as? String).orEmpty(),
                subtitleEn = (m["subtitleEn"] as? String).orEmpty()
            )
        }
        return VideoLesson(
            id = docId,
            lessonNumber = (data["lessonNumber"] as? Number)?.toInt() ?: 1,
            categoryCode = (data["categoryCode"] as? String).orEmpty(),
            stageCode = (data["stageCode"] as? String).orEmpty(),
            linkedCropId = (data["linkedCropId"] as? String).orEmpty(),
            cropTagsBn = (data["cropTagsBn"] as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
            cropTagsHi = (data["cropTagsHi"] as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
            cropTagsEn = (data["cropTagsEn"] as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
            titleBn = (data["titleBn"] as? String).orEmpty(),
            titleHi = (data["titleHi"] as? String).orEmpty(),
            titleEn = (data["titleEn"] as? String).orEmpty(),
            objectiveBn = (data["objectiveBn"] as? String).orEmpty(),
            objectiveHi = (data["objectiveHi"] as? String).orEmpty(),
            objectiveEn = (data["objectiveEn"] as? String).orEmpty(),
            summaryBn = (data["summaryBn"] as? String).orEmpty(),
            summaryHi = (data["summaryHi"] as? String).orEmpty(),
            summaryEn = (data["summaryEn"] as? String).orEmpty(),
            durationSeconds = (data["durationSeconds"] as? Number)?.toInt() ?: 180,
            thumbnailSpec = (data["thumbnailSpec"] as? String).orEmpty(),
            thumbnailDrawableKey = (data["thumbnailDrawableKey"] as? String).orEmpty(),
            thumbnailUrl = (data["thumbnailUrl"] as? String).orEmpty(),
            videoUrl = (data["videoUrl"] as? String).orEmpty(),
            audioUrlBn = (data["audioUrlBn"] as? String).orEmpty(),
            audioUrlHi = (data["audioUrlHi"] as? String).orEmpty(),
            audioUrlEn = (data["audioUrlEn"] as? String).orEmpty(),
            subtitleUrlBn = (data["subtitleUrlBn"] as? String).orEmpty(),
            subtitleUrlHi = (data["subtitleUrlHi"] as? String).orEmpty(),
            subtitleUrlEn = (data["subtitleUrlEn"] as? String).orEmpty(),
            publicationStatus = (data["publicationStatus"] as? String) ?: VideoPublicationStatus.PUBLISHED.wireValue,
            isReviewedByExpert = data["isReviewedByExpert"] as? Boolean ?: false,
            reviewedByName = (data["reviewedByName"] as? String).orEmpty(),
            lastReviewedDate = (data["lastReviewedDate"] as? String).orEmpty(),
            contentSource = (data["contentSource"] as? String).orEmpty(),
            guidanceScope = (data["guidanceScope"] as? String) ?: "GENERAL_EDUCATIONAL",
            scenes = parsedScenes,
            isBuiltInCatalog = data["isBuiltInCatalog"] as? Boolean ?: false,
            updatedByAdminEmail = (data["updatedByAdminEmail"] as? String).orEmpty(),
            updatedAt = data["updatedAt"] as? Timestamp
        )
    }

    private fun loadCachedLessonsFromPrefs(): List<VideoLesson> {
        val raw = prefs.getString(PREF_LESSONS_CACHE, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val scenesArr = o.optJSONArray("scenes") ?: JSONArray()
                    val scenes = buildList {
                        for (j in 0 until scenesArr.length()) {
                            val s = scenesArr.optJSONObject(j) ?: continue
                            add(
                                VideoStoryboardScene(
                                    sceneNumber = s.optInt("sceneNumber", j + 1),
                                    startSeconds = s.optInt("startSeconds", 0),
                                    endSeconds = s.optInt("endSeconds", 30),
                                    titleBn = s.optString("titleBn"),
                                    titleHi = s.optString("titleHi"),
                                    titleEn = s.optString("titleEn"),
                                    visualDirection = s.optString("visualDirection"),
                                    animationDirection = s.optString("animationDirection"),
                                    diagramCue = s.optString("diagramCue"),
                                    narrationBn = s.optString("narrationBn"),
                                    narrationHi = s.optString("narrationHi"),
                                    narrationEn = s.optString("narrationEn"),
                                    subtitleBn = s.optString("subtitleBn"),
                                    subtitleHi = s.optString("subtitleHi"),
                                    subtitleEn = s.optString("subtitleEn")
                                )
                            )
                        }
                    }
                    add(
                        VideoLesson(
                            id = o.optString("id"),
                            lessonNumber = o.optInt("lessonNumber", 1),
                            categoryCode = o.optString("categoryCode"),
                            stageCode = o.optString("stageCode"),
                            linkedCropId = o.optString("linkedCropId"),
                            cropTagsBn = o.optJSONArray("cropTagsBn").toStringList(),
                            cropTagsHi = o.optJSONArray("cropTagsHi").toStringList(),
                            cropTagsEn = o.optJSONArray("cropTagsEn").toStringList(),
                            titleBn = o.optString("titleBn"),
                            titleHi = o.optString("titleHi"),
                            titleEn = o.optString("titleEn"),
                            objectiveBn = o.optString("objectiveBn"),
                            objectiveHi = o.optString("objectiveHi"),
                            objectiveEn = o.optString("objectiveEn"),
                            summaryBn = o.optString("summaryBn"),
                            summaryHi = o.optString("summaryHi"),
                            summaryEn = o.optString("summaryEn"),
                            durationSeconds = o.optInt("durationSeconds", 180),
                            thumbnailSpec = o.optString("thumbnailSpec"),
                            thumbnailDrawableKey = o.optString("thumbnailDrawableKey"),
                            thumbnailUrl = o.optString("thumbnailUrl"),
                            videoUrl = o.optString("videoUrl"),
                            audioUrlBn = o.optString("audioUrlBn"),
                            audioUrlHi = o.optString("audioUrlHi"),
                            audioUrlEn = o.optString("audioUrlEn"),
                            subtitleUrlBn = o.optString("subtitleUrlBn"),
                            subtitleUrlHi = o.optString("subtitleUrlHi"),
                            subtitleUrlEn = o.optString("subtitleUrlEn"),
                            publicationStatus = o.optString("publicationStatus", VideoPublicationStatus.PUBLISHED.wireValue),
                            isReviewedByExpert = o.optBoolean("isReviewedByExpert", false),
                            reviewedByName = o.optString("reviewedByName"),
                            lastReviewedDate = o.optString("lastReviewedDate"),
                            contentSource = o.optString("contentSource"),
                            guidanceScope = o.optString("guidanceScope", "GENERAL_EDUCATIONAL"),
                            scenes = scenes,
                            isBuiltInCatalog = o.optBoolean("isBuiltInCatalog", false),
                            updatedByAdminEmail = o.optString("updatedByAdminEmail")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun saveCachedLessonsToPrefs(lessons: List<VideoLesson>) {
        val arr = JSONArray()
        lessons.forEach { lesson ->
            val obj = JSONObject().apply {
                put("id", lesson.id)
                put("lessonNumber", lesson.lessonNumber)
                put("categoryCode", lesson.categoryCode)
                put("stageCode", lesson.stageCode)
                put("linkedCropId", lesson.linkedCropId)
                put("cropTagsBn", JSONArray(lesson.cropTagsBn))
                put("cropTagsHi", JSONArray(lesson.cropTagsHi))
                put("cropTagsEn", JSONArray(lesson.cropTagsEn))
                put("titleBn", lesson.titleBn)
                put("titleHi", lesson.titleHi)
                put("titleEn", lesson.titleEn)
                put("objectiveBn", lesson.objectiveBn)
                put("objectiveHi", lesson.objectiveHi)
                put("objectiveEn", lesson.objectiveEn)
                put("summaryBn", lesson.summaryBn)
                put("summaryHi", lesson.summaryHi)
                put("summaryEn", lesson.summaryEn)
                put("durationSeconds", lesson.durationSeconds)
                put("thumbnailSpec", lesson.thumbnailSpec)
                put("thumbnailDrawableKey", lesson.thumbnailDrawableKey)
                put("thumbnailUrl", lesson.thumbnailUrl)
                put("videoUrl", lesson.videoUrl)
                put("audioUrlBn", lesson.audioUrlBn)
                put("audioUrlHi", lesson.audioUrlHi)
                put("audioUrlEn", lesson.audioUrlEn)
                put("subtitleUrlBn", lesson.subtitleUrlBn)
                put("subtitleUrlHi", lesson.subtitleUrlHi)
                put("subtitleUrlEn", lesson.subtitleUrlEn)
                put("publicationStatus", lesson.publicationStatus)
                put("isReviewedByExpert", lesson.isReviewedByExpert)
                put("reviewedByName", lesson.reviewedByName)
                put("lastReviewedDate", lesson.lastReviewedDate)
                put("contentSource", lesson.contentSource)
                put("guidanceScope", lesson.guidanceScope)
                put("isBuiltInCatalog", lesson.isBuiltInCatalog)
                put("updatedByAdminEmail", lesson.updatedByAdminEmail)
                val sArr = JSONArray()
                lesson.scenes.forEach { s ->
                    sArr.put(JSONObject().apply {
                        put("sceneNumber", s.sceneNumber)
                        put("startSeconds", s.startSeconds)
                        put("endSeconds", s.endSeconds)
                        put("titleBn", s.titleBn)
                        put("titleHi", s.titleHi)
                        put("titleEn", s.titleEn)
                        put("visualDirection", s.visualDirection)
                        put("animationDirection", s.animationDirection)
                        put("diagramCue", s.diagramCue)
                        put("narrationBn", s.narrationBn)
                        put("narrationHi", s.narrationHi)
                        put("narrationEn", s.narrationEn)
                        put("subtitleBn", s.subtitleBn)
                        put("subtitleHi", s.subtitleHi)
                        put("subtitleEn", s.subtitleEn)
                    })
                }
                put("scenes", sArr)
            }
            arr.put(obj)
        }
        prefs.edit().putString(PREF_LESSONS_CACHE, arr.toString()).apply()
    }

    private fun persistProgressMapToPrefs(userId: String, map: Map<String, FarmerVideoProgress>) {
        val arr = JSONArray()
        map.values.forEach { p ->
            arr.put(JSONObject().apply {
                put("lessonId", p.lessonId)
                put("lastPositionSeconds", p.lastPositionSeconds)
                put("totalDurationSeconds", p.totalDurationSeconds)
                put("completed", p.completed)
                put("isFavorite", p.isFavorite)
                put("lastWatchedLanguage", p.lastWatchedLanguage)
                put("updatedAtMillis", p.updatedAtMillis)
            })
        }
        prefs.edit().putString("${PREF_PROGRESS_PREFIX}$userId", arr.toString()).apply()
    }

    companion object {
        private const val TAG = "VideoLearningRepo"
        private const val PREFS_NAME = "krishak_video_learning_prefs"
        private const val PREF_LESSONS_CACHE = "cached_video_lessons_json"
        private const val PREF_PROGRESS_PREFIX = "progress_"
        const val COLLECTION_LESSONS = "video_lessons"
        const val COLLECTION_PROGRESS = "farmer_video_progress"

        /**
         * Builds a grounded, verified lesson transcript + chapter timestamps block for Krishak AI
         * so farmers asking questions from a video lesson receive answers citing the lesson title
         * and timestamped chapters without inventing external claims or chemical dosages.
         */
        fun buildAiLessonContext(
            lesson: VideoLesson,
            currentPositionSeconds: Int,
            language: AppLanguage
        ): String = buildString {
            append("[VIDEO LESSON LEARNING CONTEXT — CITE LESSON TITLE & TIMESTAMPS IN ANSWER]\n")
            append("Lesson Title: ${lesson.getTitle(language)} (ID: ${lesson.id})\n")
            append("Category: ${lesson.category().getTitle(language)} | Stage: ${lesson.stage().getTitle(language)}\n")
            append("Source & Review: ${lesson.contentSource} | Reviewed: ${lesson.isReviewedByExpert} (${lesson.lastReviewedDate})\n")
            append("Farmer Current Timestamp: ${formatMMSS(currentPositionSeconds)} / ${lesson.formattedDuration()}\n")
            append("Objective: ${lesson.getObjective(language)}\n")
            append("Chapter Transcripts & Timestamps:\n")
            lesson.scenes.forEach { scene ->
                append("- [${scene.formattedTimestampRange()}] ${scene.getTitle(language)}: ${scene.getNarration(language)}\n")
            }
            append("Important Safety Guardrail: Do NOT invent fertilizer or pesticide dosages not verified in the lesson/KB. Always refer the farmer to KVK / verified agronomist for exact field dosages.\n")
        }
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) {
            val s = optString(i).trim()
            if (s.isNotEmpty()) add(s)
        }
    }
}
