package com.example.data.model

enum class AiProviderKind(val wireValue: String) {
    GEMINI("GEMINI"),
    OPENAI("OPENAI");

    companion object {
        fun fromWire(value: String?): AiProviderKind {
            return entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) } ?: GEMINI
        }
    }
}

enum class AiAssistantMode(val wireValue: String) {
    FARMER_AI("FARMER_AI"),
    PLANT_PROBLEM("PLANT_PROBLEM"),
    GENERAL_AI("GENERAL_AI")
}

enum class AiErrorCode {
    NONE,
    VALIDATION_ERROR,
    RATE_LIMITED,
    AUTH_REQUIRED,
    UNCONFIGURED,
    NETWORK_OFFLINE,
    TIMEOUT,
    QUOTA_EXCEEDED,
    SERVICE_UNAVAILABLE,
    UNKNOWN
}

enum class AiProviderHealth {
    ONLINE,
    DEGRADED,
    RATE_LIMITED,
    UNCONFIGURED,
    ERROR
}

data class AiFailureInfo(
    val code: AiErrorCode,
    val userMessage: String,
    val technicalReason: String,
    val primaryAttempted: AiProviderKind,
    val backupAttempted: AiProviderKind? = null,
    val retryAllowed: Boolean = true
)

data class AiSourceCitation(
    val title: String,
    val url: String,
    val institution: String = "",
    val verifiedDate: String = ""
)

data class CropMismatchSuggestion(
    val selectedCropId: String,
    val selectedCropName: String,
    val detectedCropId: String,
    val detectedCropName: String,
    val confirmationMessage: String
)

data class AiGenerationResult(
    val success: Boolean,
    val text: String = "",
    val providerUsed: AiProviderKind = AiProviderKind.GEMINI,
    val modelUsed: String = "",
    val fallbackUsed: Boolean = false,
    val fallbackReason: String? = null,
    val latencyMs: Long = 0L,
    val estimatedTokens: Int = 0,
    val citations: List<AiSourceCitation> = emptyList(),
    val shouldSuggestGoogleSearch: Boolean = false,
    val cropMismatch: CropMismatchSuggestion? = null,
    val failure: AiFailureInfo? = null
)

data class AiAdminConfig(
    val primaryProvider: AiProviderKind = AiProviderKind.GEMINI,
    val backupProviderEnabled: Boolean = false,
    val geminiModelName: String = "gemini-3.8-flash",
    val openAiModelName: String = "gpt-4o-mini",
    val openAiProxyUrl: String = "",
    val timeoutMs: Long = 35_000L,
    val maxRetriesPerProvider: Int = 1,
    val rateLimitPerMinute: Int = 8,
    val dailyQuotaPerUser: Int = 80,
    val enableRagCitations: Boolean = true,
    val enableWeatherContext: Boolean = true
) {
    fun hasValidOpenAiProxy(): Boolean {
        val trimmed = openAiProxyUrl.trim()
        return trimmed.startsWith("https://", ignoreCase = true) &&
            trimmed.length > 12 &&
            !trimmed.contains(" ")
    }
}

data class AiAdminStatusSnapshot(
    val geminiHealth: AiProviderHealth = AiProviderHealth.ONLINE,
    val openAiHealth: AiProviderHealth = AiProviderHealth.UNCONFIGURED,
    val lastError: String? = null
)

data class VerifiedAgriKnowledgeRecord(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val titleHi: String,
    val institution: String,
    val sourceUrl: String,
    val publicationDate: String,
    val verifiedDate: String,
    val category: String,
    val applicableCropIds: List<String>,
    val regionTag: String,
    val keywords: List<String>,
    val summaryEn: String,
    val summaryBn: String,
    val summaryHi: String,
    val isCustomAdminAdded: Boolean = false
) {
    fun getTitle(language: AppLanguage): String = when (language) {
        AppLanguage.BENGALI -> titleBn.ifBlank { titleEn }
        AppLanguage.HINDI -> titleHi.ifBlank { titleEn }
        AppLanguage.ENGLISH -> titleEn
    }

    fun getSummary(language: AppLanguage): String = when (language) {
        AppLanguage.BENGALI -> summaryBn.ifBlank { summaryEn }
        AppLanguage.HINDI -> summaryHi.ifBlank { summaryEn }
        AppLanguage.ENGLISH -> summaryEn
    }
}

data class WeatherContextSnapshot(
    val regionLabel: String,
    val observedCondition: String,
    val tempSummary: String,
    val rainProbability: String,
    val humidity: String,
    val windSpeed: String,
    val advisorySummary: String,
    val isLiveTelemetry: Boolean = false
)
