package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.R
import com.example.data.ai.AiSecurityGuard
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AiFeedbackEntity
import com.example.data.local.entity.AiKnowledgeDocEntity
import com.example.data.local.entity.AiTelemetryLogEntity
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AiAdminConfig
import com.example.data.model.AiAdminStatusSnapshot
import com.example.data.model.AiProviderHealth
import com.example.data.model.AiProviderKind
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

private const val ADMIN_AI_TAG = "AiAdminRepository"

/**
 * Manages Admin AI provider configuration, dual-provider health status, RAG knowledge docs,
 * anonymized telemetry logs, and farmer feedback review.
 * Never persists raw API keys, tokens, or unredacted user prompts in SharedPreferences or Firestore.
 */
class AiAdminRepository(
    context: Context,
    private val db: AppDatabase = AppDatabase.getInstance(context)
) {
    private val appContext = context.applicationContext
    private val aiDao = db.aiIntelligenceDao()
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _configState = MutableStateFlow(loadLocalConfig())
    val configState: StateFlow<AiAdminConfig> = _configState.asStateFlow()

    private val _statusState = MutableStateFlow(initialStatus(_configState.value))
    val statusState: StateFlow<AiAdminStatusSnapshot> = _statusState.asStateFlow()

    private fun initialStatus(cfg: AiAdminConfig): AiAdminStatusSnapshot {
        return AiAdminStatusSnapshot(
            geminiHealth = AiProviderHealth.ONLINE,
            openAiHealth = if (cfg.hasValidOpenAiProxy()) AiProviderHealth.ONLINE else AiProviderHealth.UNCONFIGURED,
            lastError = null
        )
    }

    private fun getFirestoreOrNull(): FirebaseFirestore? {
        return try {
            val dbId = appContext.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank()) FirebaseFirestore.getInstance(dbId) else null
        } catch (_: Exception) {
            null
        }
    }

    fun getCurrentConfig(): AiAdminConfig = _configState.value

    private fun loadLocalConfig(): AiAdminConfig {
        return AiAdminConfig(
            primaryProvider = AiProviderKind.fromWire(
                prefs.getString(KEY_PRIMARY_PROVIDER, AiProviderKind.GEMINI.wireValue)
            ),
            backupProviderEnabled = prefs.getBoolean(KEY_BACKUP_ENABLED, false),
            geminiModelName = prefs.getString(KEY_GEMINI_MODEL, "gemini-3.8-flash")
                .orEmpty().ifBlank { "gemini-3.8-flash" },
            openAiModelName = prefs.getString(KEY_OPENAI_MODEL, "gpt-4o-mini")
                .orEmpty().ifBlank { "gpt-4o-mini" },
            openAiProxyUrl = prefs.getString(KEY_OPENAI_PROXY_URL, "").orEmpty(),
            timeoutMs = prefs.getLong(KEY_TIMEOUT_MS, 35_000L).coerceIn(5_000L, 60_000L),
            maxRetriesPerProvider = prefs.getInt(KEY_MAX_RETRIES, 1).coerceIn(0, 1),
            rateLimitPerMinute = prefs.getInt(KEY_RATE_LIMIT_PER_MIN, 8).coerceIn(1, 60),
            dailyQuotaPerUser = prefs.getInt(KEY_DAILY_QUOTA, 80).coerceIn(5, 500),
            enableRagCitations = prefs.getBoolean(KEY_ENABLE_RAG, true),
            enableWeatherContext = prefs.getBoolean(KEY_ENABLE_WEATHER, true)
        )
    }

    private fun persistLocalConfig(clean: AiAdminConfig) {
        prefs.edit()
            .putString(KEY_PRIMARY_PROVIDER, clean.primaryProvider.wireValue)
            .putBoolean(KEY_BACKUP_ENABLED, clean.backupProviderEnabled)
            .putString(KEY_GEMINI_MODEL, clean.geminiModelName)
            .putString(KEY_OPENAI_MODEL, clean.openAiModelName)
            .putString(KEY_OPENAI_PROXY_URL, clean.openAiProxyUrl)
            .putLong(KEY_TIMEOUT_MS, clean.timeoutMs)
            .putInt(KEY_MAX_RETRIES, clean.maxRetriesPerProvider)
            .putInt(KEY_RATE_LIMIT_PER_MIN, clean.rateLimitPerMinute)
            .putInt(KEY_DAILY_QUOTA, clean.dailyQuotaPerUser)
            .putBoolean(KEY_ENABLE_RAG, clean.enableRagCitations)
            .putBoolean(KEY_ENABLE_WEATHER, clean.enableWeatherContext)
            .commit()

        _configState.value = clean
        _statusState.value = _statusState.value.copy(
            openAiHealth = if (clean.hasValidOpenAiProxy()) AiProviderHealth.ONLINE else AiProviderHealth.UNCONFIGURED
        )
    }

    suspend fun saveAdminConfig(config: AiAdminConfig, adminEmail: String): Result<Unit> {
        val trimmedProxy = config.openAiProxyUrl.trim()
        if (trimmedProxy.isNotEmpty() && (!trimmedProxy.startsWith("https://", ignoreCase = true) || trimmedProxy.contains(" ") || trimmedProxy.length <= 12)) {
            return Result.failure(IllegalArgumentException("OpenAI proxy URL must be a valid https:// endpoint."))
        }

        val clean = config.copy(
            geminiModelName = config.geminiModelName.trim().ifBlank { "gemini-3.8-flash" },
            openAiModelName = config.openAiModelName.trim().ifBlank { "gpt-4o-mini" },
            openAiProxyUrl = trimmedProxy,
            timeoutMs = config.timeoutMs.coerceIn(5_000L, 60_000L),
            maxRetriesPerProvider = config.maxRetriesPerProvider.coerceIn(0, 1),
            rateLimitPerMinute = config.rateLimitPerMinute.coerceIn(1, 60),
            dailyQuotaPerUser = config.dailyQuotaPerUser.coerceIn(5, 500)
        )
        persistLocalConfig(clean)

        try {
            val authUser = Firebase.auth.currentUser
            val firestore = getFirestoreOrNull()
            if (authUser != null && firestore != null && authUser.email.equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true)) {
                val payload = hashMapOf<String, Any>(
                    "primaryProvider" to clean.primaryProvider.wireValue,
                    "backupProviderEnabled" to clean.backupProviderEnabled,
                    "geminiModelName" to clean.geminiModelName,
                    "openAiModelName" to clean.openAiModelName,
                    "openAiProxyUrl" to clean.openAiProxyUrl,
                    "timeoutMs" to clean.timeoutMs,
                    "maxRetriesPerProvider" to clean.maxRetriesPerProvider,
                    "rateLimitPerMinute" to clean.rateLimitPerMinute,
                    "dailyQuotaPerUser" to clean.dailyQuotaPerUser,
                    "enableRagCitations" to clean.enableRagCitations,
                    "enableWeatherContext" to clean.enableWeatherContext,
                    "updatedByAdmin" to adminEmail.ifBlank { UserSessionManager.ADMIN_EMAIL }.take(128),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("ai_admin_config").document("active").set(payload).await()
            }
        } catch (e: Exception) {
            Log.w(ADMIN_AI_TAG, "Firestore ai_admin_config sync skipped (local saved): ${e.message}")
        }
        return Result.success(Unit)
    }

    suspend fun syncConfigFromCloud() {
        try {
            if (Firebase.auth.currentUser == null) return
            val firestore = getFirestoreOrNull() ?: return
            val snap = firestore.collection("ai_admin_config").document("active").get().await()
            if (!snap.exists()) return
            val remote = AiAdminConfig(
                primaryProvider = AiProviderKind.fromWire(snap.getString("primaryProvider")),
                backupProviderEnabled = snap.getBoolean("backupProviderEnabled") ?: false,
                geminiModelName = snap.getString("geminiModelName")?.takeIf { it.isNotBlank() } ?: "gemini-3.8-flash",
                openAiModelName = snap.getString("openAiModelName")?.takeIf { it.isNotBlank() } ?: "gpt-4o-mini",
                openAiProxyUrl = snap.getString("openAiProxyUrl").orEmpty().takeIf { it.startsWith("https://", true) }.orEmpty(),
                timeoutMs = (snap.getLong("timeoutMs") ?: 35_000L).coerceIn(5_000L, 60_000L),
                maxRetriesPerProvider = (snap.getLong("maxRetriesPerProvider") ?: 1L).toInt().coerceIn(0, 1),
                rateLimitPerMinute = (snap.getLong("rateLimitPerMinute") ?: 8L).toInt().coerceIn(1, 60),
                dailyQuotaPerUser = (snap.getLong("dailyQuotaPerUser") ?: 80L).toInt().coerceIn(5, 500),
                enableRagCitations = snap.getBoolean("enableRagCitations") ?: true,
                enableWeatherContext = snap.getBoolean("enableWeatherContext") ?: true
            )
            persistLocalConfig(remote)
        } catch (e: Exception) {
            Log.w(ADMIN_AI_TAG, "Unable to sync ai_admin_config from cloud: ${e.message}")
        }
    }

    suspend fun syncKnowledgeDocsFromCloud() {
        try {
            if (Firebase.auth.currentUser == null) return
            val firestore = getFirestoreOrNull() ?: return
            val snap = firestore.collection("ai_knowledge_docs").get().await()
            val docs = snap.documents.mapNotNull { doc ->
                val titleEn = doc.getString("titleEn") ?: return@mapNotNull null
                val url = doc.getString("sourceUrl") ?: return@mapNotNull null
                val summaryEn = doc.getString("summaryEn") ?: return@mapNotNull null
                AiKnowledgeDocEntity(
                    id = doc.id,
                    titleEn = titleEn,
                    titleBn = doc.getString("titleBn").orEmpty(),
                    titleHi = doc.getString("titleHi").orEmpty(),
                    institution = doc.getString("institution") ?: "ICAR",
                    sourceUrl = url,
                    publicationDate = doc.getString("publicationDate") ?: "2026-11-17",
                    verifiedDate = doc.getString("verifiedDate") ?: "2026-11-17",
                    category = doc.getString("category") ?: "AGRONOMY",
                    applicableCropIdsCsv = doc.getString("applicableCropIdsCsv").orEmpty(),
                    regionTag = doc.getString("regionTag") ?: "West Bengal",
                    keywordsCsv = doc.getString("keywordsCsv").orEmpty(),
                    summaryEn = summaryEn,
                    summaryBn = doc.getString("summaryBn").orEmpty(),
                    summaryHi = doc.getString("summaryHi").orEmpty(),
                    isCustomAdminAdded = true,
                    isActive = doc.getBoolean("isActive") ?: true,
                    updatedAtMillis = System.currentTimeMillis()
                )
            }
            if (docs.isNotEmpty()) {
                aiDao.upsertKnowledgeDocs(docs)
            }
        } catch (e: Exception) {
            Log.w(ADMIN_AI_TAG, "Unable to sync ai_knowledge_docs: ${e.message}")
        }
    }

    suspend fun syncTelemetryAndFeedbackForAdmin() {
        try {
            val authUser = Firebase.auth.currentUser ?: return
            if (!authUser.email.equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true)) return
            val firestore = getFirestoreOrNull() ?: return

            val fbSnap = firestore.collection("ai_feedback")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            fbSnap.documents.forEach { d ->
                aiDao.insertFeedback(
                    AiFeedbackEntity(
                        id = d.id,
                        userId = d.getString("userId") ?: "farmer",
                        assistantMode = d.getString("assistantMode") ?: "FARMER_AI",
                        cropId = d.getString("cropId").orEmpty(),
                        providerUsed = d.getString("providerUsed") ?: "GEMINI",
                        helpful = d.getBoolean("helpful") ?: true,
                        messagePreview = d.getString("messagePreview").orEmpty(),
                        farmerNote = d.getString("farmerNote").orEmpty(),
                        reviewStatus = d.getString("reviewStatus") ?: "OPEN",
                        timestampMillis = d.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            Log.w(ADMIN_AI_TAG, "Unable to sync admin telemetry/feedback: ${e.message}")
        }
    }

    fun observeCustomKnowledgeDocs(): Flow<List<AiKnowledgeDocEntity>> = aiDao.getAllKnowledgeDocs()

    suspend fun getCustomKnowledgeDocsSync(): List<AiKnowledgeDocEntity> = aiDao.getActiveKnowledgeDocsSync()

    suspend fun saveCustomKnowledgeDoc(doc: AiKnowledgeDocEntity) {
        aiDao.upsertKnowledgeDoc(doc)
        try {
            val authUser = Firebase.auth.currentUser
            val firestore = getFirestoreOrNull()
            if (authUser != null && firestore != null && authUser.email.equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true)) {
                val payload = hashMapOf<String, Any>(
                    "titleEn" to doc.titleEn.take(200),
                    "titleBn" to doc.titleBn.take(200),
                    "titleHi" to doc.titleHi.take(200),
                    "institution" to doc.institution.take(150),
                    "sourceUrl" to doc.sourceUrl.take(500),
                    "publicationDate" to doc.publicationDate,
                    "verifiedDate" to doc.verifiedDate,
                    "category" to doc.category.take(60),
                    "applicableCropIdsCsv" to doc.applicableCropIdsCsv,
                    "regionTag" to doc.regionTag,
                    "keywordsCsv" to doc.keywordsCsv,
                    "summaryEn" to doc.summaryEn.take(1500),
                    "summaryBn" to doc.summaryBn.take(1500),
                    "summaryHi" to doc.summaryHi.take(1500),
                    "isActive" to doc.isActive,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("ai_knowledge_docs").document(doc.id).set(payload).await()
            }
        } catch (e: Exception) {
            Log.w(ADMIN_AI_TAG, "Firestore ai_knowledge_docs sync skipped: ${e.message}")
        }
    }

    suspend fun deleteCustomKnowledgeDoc(id: String) {
        aiDao.deleteKnowledgeDoc(id)
        try {
            val authUser = Firebase.auth.currentUser
            val firestore = getFirestoreOrNull()
            if (authUser != null && firestore != null && authUser.email.equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true)) {
                firestore.collection("ai_knowledge_docs").document(id).delete().await()
            }
        } catch (e: Exception) {
            Log.w(ADMIN_AI_TAG, "Firestore delete ai_knowledge_docs skipped: ${e.message}")
        }
    }

    fun observeTelemetry(limit: Int = 80): Flow<List<AiTelemetryLogEntity>> = aiDao.getRecentTelemetry(limit)

    suspend fun clearLocalTelemetry() {
        aiDao.clearTelemetry()
    }

    fun observeFeedback(limit: Int = 80): Flow<List<AiFeedbackEntity>> = aiDao.getRecentFeedback(limit)

    suspend fun submitFeedback(
        userId: String,
        assistantMode: String,
        cropId: String,
        providerUsed: String,
        helpful: Boolean,
        messagePreview: String,
        farmerNote: String = ""
    ) {
        val safeUserId = userId.ifBlank { "guest" }
        val cleanPreview = AiSecurityGuard.redactTechnicalError(messagePreview).take(240)
        val cleanNote = AiSecurityGuard.redactTechnicalError(farmerNote).take(300)
        val entity = AiFeedbackEntity(
            id = "fb_${UUID.randomUUID()}",
            userId = safeUserId,
            assistantMode = assistantMode,
            cropId = cropId.take(64),
            providerUsed = AiProviderKind.fromWire(providerUsed).wireValue,
            helpful = helpful,
            messagePreview = cleanPreview,
            farmerNote = cleanNote,
            reviewStatus = "OPEN"
        )
        aiDao.insertFeedback(entity)

        try {
            val authUid = Firebase.auth.currentUser?.uid
            val firestore = getFirestoreOrNull()
            if (authUid != null && firestore != null) {
                val payload = hashMapOf<String, Any>(
                    "userId" to authUid,
                    "assistantMode" to assistantMode,
                    "cropId" to entity.cropId,
                    "providerUsed" to entity.providerUsed,
                    "helpful" to helpful,
                    "messagePreview" to cleanPreview,
                    "farmerNote" to cleanNote,
                    "reviewStatus" to "OPEN",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("ai_feedback").document(entity.id).set(payload).await()
            }
        } catch (e: Exception) {
            Log.w(ADMIN_AI_TAG, "Firestore ai_feedback sync skipped: ${e.message}")
        }
    }

    suspend fun markFeedbackReviewed(id: String) {
        aiDao.updateFeedbackStatus(id, "REVIEWED")
        try {
            val authUser = Firebase.auth.currentUser
            val firestore = getFirestoreOrNull()
            if (authUser != null && firestore != null && authUser.email.equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true)) {
                firestore.collection("ai_feedback").document(id).update(
                    mapOf(
                        "reviewStatus" to "REVIEWED",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: Exception) {
            Log.w(ADMIN_AI_TAG, "Firestore feedback review update skipped: ${e.message}")
        }
    }

    suspend fun getUserRequestsTodayCount(userId: String): Int {
        val dayAgo = System.currentTimeMillis() - 86_400_000L
        return aiDao.countUserTelemetrySince(userId.ifBlank { "guest" }, dayAgo)
    }

    fun updateProviderHealth(provider: AiProviderKind, health: AiProviderHealth, errorMsg: String?) {
        val cleanErr = errorMsg?.let { AiSecurityGuard.redactTechnicalError(it) }
        _statusState.value = when (provider) {
            AiProviderKind.GEMINI -> _statusState.value.copy(geminiHealth = health, lastError = cleanErr)
            AiProviderKind.OPENAI -> _statusState.value.copy(openAiHealth = health, lastError = cleanErr)
        }
    }

    suspend fun recordTelemetry(
        userId: String,
        assistantMode: String,
        cropId: String,
        provider: AiProviderKind,
        model: String,
        fallbackUsed: Boolean,
        success: Boolean,
        errorCode: String,
        errorSummary: String,
        latencyMs: Long,
        estimatedTokens: Int
    ) {
        val safeUserId = userId.ifBlank { "guest" }
        val costUsd = (estimatedTokens.coerceAtLeast(0) / 1_000_000.0) *
            if (provider == AiProviderKind.OPENAI) 0.60 else 0.15
        val logId = "tel_${UUID.randomUUID()}"
        val redactedError = if (errorSummary.isBlank()) "" else AiSecurityGuard.redactTechnicalError(errorSummary).take(200)

        aiDao.insertTelemetry(
            AiTelemetryLogEntity(
                id = logId,
                userId = safeUserId,
                assistantMode = assistantMode,
                cropId = cropId.take(64),
                providerUsed = provider.wireValue,
                modelUsed = model.take(64),
                fallbackUsed = fallbackUsed,
                success = success,
                errorCode = errorCode.take(40),
                errorSummary = redactedError,
                latencyMs = latencyMs.coerceAtLeast(0L),
                estimatedTokens = estimatedTokens.coerceAtLeast(0),
                estimatedCostUsd = costUsd
            )
        )

        try {
            val authUid = Firebase.auth.currentUser?.uid
            val firestore = getFirestoreOrNull()
            if (authUid != null && firestore != null) {
                val payload = hashMapOf<String, Any>(
                    "userId" to authUid,
                    "assistantMode" to assistantMode,
                    "cropId" to cropId.take(64),
                    "providerUsed" to provider.wireValue,
                    "modelUsed" to model.take(64),
                    "fallbackUsed" to fallbackUsed,
                    "success" to success,
                    "errorCode" to errorCode.take(40),
                    "errorSummary" to redactedError,
                    "latencyMs" to latencyMs.coerceAtLeast(0L),
                    "estimatedTokens" to estimatedTokens.coerceAtLeast(0),
                    "estimatedCostUsd" to costUsd,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("ai_telemetry_logs").document(logId).set(payload).await()
            }
        } catch (e: Exception) {
            Log.w(ADMIN_AI_TAG, "Firestore ai_telemetry_logs sync skipped: ${e.message}")
        }
    }

    companion object {
        private const val PREFS_NAME = "krishak_sathi_ai_admin_cfg_v1"
        private const val KEY_PRIMARY_PROVIDER = "primary_provider"
        private const val KEY_BACKUP_ENABLED = "backup_enabled"
        private const val KEY_GEMINI_MODEL = "gemini_model"
        private const val KEY_OPENAI_MODEL = "openai_model"
        private const val KEY_OPENAI_PROXY_URL = "openai_proxy_url"
        private const val KEY_TIMEOUT_MS = "timeout_ms"
        private const val KEY_MAX_RETRIES = "max_retries"
        private const val KEY_RATE_LIMIT_PER_MIN = "rate_limit_per_min"
        private const val KEY_DAILY_QUOTA = "daily_quota"
        private const val KEY_ENABLE_RAG = "enable_rag"
        private const val KEY_ENABLE_WEATHER = "enable_weather"

        fun estimateTokens(promptText: String, responseText: String): Int {
            val totalChars = promptText.length + responseText.length
            return ((totalChars + 3) / 4).coerceAtLeast(1)
        }
    }
}
