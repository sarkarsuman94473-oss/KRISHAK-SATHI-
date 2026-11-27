package com.example.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

enum class VideoCategory(
    val code: String,
    val emoji: String,
    val titleBn: String,
    val titleHi: String,
    val titleEn: String
) {
    SOIL_PREP_TESTING(
        code = "SOIL_PREP_TESTING",
        emoji = "🪴",
        titleBn = "মাটি প্রস্তুতি ও পরীক্ষা",
        titleHi = "मिट्टी की तैयारी व परीक्षण",
        titleEn = "Soil Preparation & Testing"
    ),
    SEED_GERMINATION(
        code = "SEED_GERMINATION",
        emoji = "🌱",
        titleBn = "বীজ নির্বাচন ও অঙ্কুরোদগম",
        titleHi = "बीज चयन व अंकुरण",
        titleEn = "Seed Selection & Germination"
    ),
    SEEDLING_NURSERY(
        code = "SEEDLING_NURSERY",
        emoji = "🌿",
        titleBn = "চারা ও নার্সারি ব্যবস্থাপনা",
        titleHi = "पौध व नर्सरी प्रबंधन",
        titleEn = "Seedling & Nursery Management"
    ),
    PADDY_CULTIVATION(
        code = "PADDY_CULTIVATION",
        emoji = "🌾",
        titleBn = "ধান চাষ পদ্ধতি",
        titleHi = "धान की खेती",
        titleEn = "Paddy Cultivation"
    ),
    POTATO_CULTIVATION(
        code = "POTATO_CULTIVATION",
        emoji = "🥔",
        titleBn = "আলু চাষ পদ্ধতি",
        titleHi = "आलू की खेती",
        titleEn = "Potato Cultivation"
    ),
    TOMATO_CULTIVATION(
        code = "TOMATO_CULTIVATION",
        emoji = "🍅",
        titleBn = "টমেটো চাষ পদ্ধতি",
        titleHi = "टमाटर की खेती",
        titleEn = "Tomato Cultivation"
    ),
    VEGETABLE_FARMING(
        code = "VEGETABLE_FARMING",
        emoji = "🥦",
        titleBn = "শাকসবজি চাষ",
        titleHi = "सब्ज़ी की खेती",
        titleEn = "Vegetable Farming"
    ),
    FRUIT_CULTIVATION(
        code = "FRUIT_CULTIVATION",
        emoji = "🥭",
        titleBn = "ফল চাষ ও বাগান পরিচর্যা",
        titleHi = "फल व बागवानी खेती",
        titleEn = "Fruit Cultivation"
    ),
    IRRIGATION_WATER(
        code = "IRRIGATION_WATER",
        emoji = "💧",
        titleBn = "সেচ ও জল ব্যবস্থাপনা",
        titleHi = "सिंचाई व जल प्रबंधन",
        titleEn = "Irrigation & Water Management"
    ),
    FERTILIZER_NUTRIENT(
        code = "FERTILIZER_NUTRIENT",
        emoji = "⚖️",
        titleBn = "সার ও পুষ্টি ব্যবস্থাপনা",
        titleHi = "उर्वरक व पोषक प्रबंधन",
        titleEn = "Fertilizer & Nutrient Management"
    ),
    PEST_DISEASE_ID(
        code = "PEST_DISEASE_ID",
        emoji = "🔍",
        titleBn = "পোকা ও রোগ শনাক্তকরণ",
        titleHi = "कीट व रोग पहचान",
        titleEn = "Pest & Disease Identification"
    ),
    IPM_PRACTICES(
        code = "IPM_PRACTICES",
        emoji = "🛡️",
        titleBn = "সমন্বিত বালাই দমন (IPM)",
        titleHi = "समेकित कीट प्रबंधन (IPM)",
        titleEn = "Integrated Pest Management"
    ),
    ORGANIC_FARMING(
        code = "ORGANIC_FARMING",
        emoji = "♻️",
        titleBn = "জৈব চাষ পদ্ধতি",
        titleHi = "जैविक खेती पद्धतियाँ",
        titleEn = "Organic Farming Practices"
    ),
    MACHINERY_SAFETY(
        code = "MACHINERY_SAFETY",
        emoji = "🚜",
        titleBn = "কৃষি যন্ত্রপাতি ও নিরাপত্তা",
        titleHi = "कृषि यंत्र संचालन व सुरक्षा",
        titleEn = "Machinery Operation & Safety"
    ),
    HARVEST_STORAGE(
        code = "HARVEST_STORAGE",
        emoji = "📦",
        titleBn = "ফসল কাটা ও সংরক্ষণ",
        titleHi = "कटाई व फसल भंडारण",
        titleEn = "Harvesting & Post-Harvest Storage"
    ),
    SEASONAL_WEATHER(
        code = "SEASONAL_WEATHER",
        emoji = "🌦️",
        titleBn = "মরশুমি চাষ ও আবহাওয়া সতর্কতা",
        titleHi = "मौसमी खेती व मौसम जागरूकता",
        titleEn = "Seasonal Farming & Weather"
    );

    fun getTitle(language: AppLanguage): String = when (language) {
        AppLanguage.BENGALI -> titleBn
        AppLanguage.HINDI -> titleHi
        AppLanguage.ENGLISH -> titleEn
    }

    companion object {
        fun fromCode(code: String?): VideoCategory =
            entries.firstOrNull { it.code.equals(code?.trim(), ignoreCase = true) } ?: SOIL_PREP_TESTING
    }
}

enum class FarmingStage(
    val code: String,
    val titleBn: String,
    val titleHi: String,
    val titleEn: String
) {
    PRE_SOWING("PRE_SOWING", "জমি তৈরি ও মাটি", "भूमि व मिट्टी तैयारी", "Land Prep & Soil"),
    SEED_NURSERY("SEED_NURSERY", "বীজ ও নার্সারি", "बीज व नर्सरी", "Seeds & Nursery"),
    VEGETATIVE_CARE("VEGETATIVE_CARE", "পরিচর্যা ও সেচ", "सिंचाई व देखभाल", "Care & Irrigation"),
    CROP_PROTECTION("CROP_PROTECTION", "রোগ, পোকা ও সুরক্ষা", "रोग, कीट व सुरक्षा", "Protection & Safety"),
    HARVEST_POST("HARVEST_POST", "ফসল সংগ্রহ ও সংরক্ষণ", "कटाई व भंडारण", "Harvest & Storage");

    fun getTitle(language: AppLanguage): String = when (language) {
        AppLanguage.BENGALI -> titleBn
        AppLanguage.HINDI -> titleHi
        AppLanguage.ENGLISH -> titleEn
    }

    companion object {
        fun fromCode(code: String?): FarmingStage =
            entries.firstOrNull { it.code.equals(code?.trim(), ignoreCase = true) } ?: PRE_SOWING
    }
}

enum class VideoPublicationStatus(val wireValue: String) {
    DRAFT("DRAFT"),
    UNDER_EXPERT_REVIEW("UNDER_EXPERT_REVIEW"),
    PUBLISHED("PUBLISHED"),
    UNPUBLISHED("UNPUBLISHED");

    fun getLabel(language: AppLanguage): String = when (this) {
        DRAFT -> when (language) {
            AppLanguage.BENGALI -> "খসড়া"
            AppLanguage.HINDI -> "ड्राफ्ट"
            AppLanguage.ENGLISH -> "Draft"
        }
        UNDER_EXPERT_REVIEW -> when (language) {
            AppLanguage.BENGALI -> "বিশেষজ্ঞ পর্যালোচনাধীন"
            AppLanguage.HINDI -> "विशेषज्ञ समीक्षा में"
            AppLanguage.ENGLISH -> "Under Review"
        }
        PUBLISHED -> when (language) {
            AppLanguage.BENGALI -> "প্রকাশিত"
            AppLanguage.HINDI -> "प्रकाशित"
            AppLanguage.ENGLISH -> "Published"
        }
        UNPUBLISHED -> when (language) {
            AppLanguage.BENGALI -> "অপ্রকাশিত"
            AppLanguage.HINDI -> "अप्रकाशित"
            AppLanguage.ENGLISH -> "Unpublished"
        }
    }

    companion object {
        fun fromWire(value: String?): VideoPublicationStatus =
            entries.firstOrNull { it.wireValue.equals(value?.trim(), ignoreCase = true) } ?: PUBLISHED
    }
}

data class VideoStoryboardScene(
    val sceneNumber: Int,
    val startSeconds: Int,
    val endSeconds: Int,
    val titleBn: String,
    val titleHi: String,
    val titleEn: String,
    val visualDirection: String,
    val animationDirection: String,
    val diagramCue: String,
    val narrationBn: String,
    val narrationHi: String,
    val narrationEn: String,
    val subtitleBn: String = narrationBn,
    val subtitleHi: String = narrationHi,
    val subtitleEn: String = narrationEn
) {
    fun getTitle(language: AppLanguage): String = when (language) {
        AppLanguage.BENGALI -> titleBn
        AppLanguage.HINDI -> titleHi
        AppLanguage.ENGLISH -> titleEn
    }

    fun getNarration(language: AppLanguage): String = when (language) {
        AppLanguage.BENGALI -> narrationBn
        AppLanguage.HINDI -> narrationHi
        AppLanguage.ENGLISH -> narrationEn
    }

    fun getSubtitle(language: AppLanguage): String = when (language) {
        AppLanguage.BENGALI -> subtitleBn.ifBlank { narrationBn }
        AppLanguage.HINDI -> subtitleHi.ifBlank { narrationHi }
        AppLanguage.ENGLISH -> subtitleEn.ifBlank { narrationEn }
    }

    fun formattedTimestampRange(): String = "${formatMMSS(startSeconds)} – ${formatMMSS(endSeconds)}"
}

@Keep
@IgnoreExtraProperties
data class VideoLesson(
    @DocumentId val id: String = "",
    val lessonNumber: Int = 1,
    val categoryCode: String = VideoCategory.SOIL_PREP_TESTING.code,
    val stageCode: String = FarmingStage.PRE_SOWING.code,
    val linkedCropId: String = "",
    val cropTagsBn: List<String> = emptyList(),
    val cropTagsHi: List<String> = emptyList(),
    val cropTagsEn: List<String> = emptyList(),
    val titleBn: String = "",
    val titleHi: String = "",
    val titleEn: String = "",
    val objectiveBn: String = "",
    val objectiveHi: String = "",
    val objectiveEn: String = "",
    val summaryBn: String = "",
    val summaryHi: String = "",
    val summaryEn: String = "",
    val durationSeconds: Int = 180,
    val thumbnailSpec: String = "",
    val thumbnailDrawableKey: String = "img_video_soil_prep",
    val thumbnailUrl: String = "",
    val videoUrl: String = "",
    val audioUrlBn: String = "",
    val audioUrlHi: String = "",
    val audioUrlEn: String = "",
    val subtitleUrlBn: String = "",
    val subtitleUrlHi: String = "",
    val subtitleUrlEn: String = "",
    val publicationStatus: String = VideoPublicationStatus.PUBLISHED.wireValue,
    val isReviewedByExpert: Boolean = false,
    val reviewedByName: String = "",
    val lastReviewedDate: String = "",
    val contentSource: String = "ICAR / KVK General Extension Reference",
    val guidanceScope: String = "GENERAL_EDUCATIONAL",
    val scenes: List<VideoStoryboardScene> = emptyList(),
    val isBuiltInCatalog: Boolean = true,
    val updatedByAdminEmail: String = "",
    val updatedAt: Timestamp? = null
) {
    fun category(): VideoCategory = VideoCategory.fromCode(categoryCode)
    fun stage(): FarmingStage = FarmingStage.fromCode(stageCode)
    fun status(): VideoPublicationStatus = VideoPublicationStatus.fromWire(publicationStatus)

    fun getTitle(language: AppLanguage): String = when (language) {
        AppLanguage.BENGALI -> titleBn.ifBlank { titleEn }
        AppLanguage.HINDI -> titleHi.ifBlank { titleEn }
        AppLanguage.ENGLISH -> titleEn.ifBlank { titleBn }
    }

    fun getObjective(language: AppLanguage): String = when (language) {
        AppLanguage.BENGALI -> objectiveBn.ifBlank { objectiveEn }
        AppLanguage.HINDI -> objectiveHi.ifBlank { objectiveEn }
        AppLanguage.ENGLISH -> objectiveEn.ifBlank { objectiveBn }
    }

    fun getSummary(language: AppLanguage): String = when (language) {
        AppLanguage.BENGALI -> summaryBn.ifBlank { summaryEn }
        AppLanguage.HINDI -> summaryHi.ifBlank { summaryEn }
        AppLanguage.ENGLISH -> summaryEn.ifBlank { summaryBn }
    }

    fun getCropTags(language: AppLanguage): List<String> = when (language) {
        AppLanguage.BENGALI -> cropTagsBn.ifEmpty { cropTagsEn }
        AppLanguage.HINDI -> cropTagsHi.ifEmpty { cropTagsEn }
        AppLanguage.ENGLISH -> cropTagsEn.ifEmpty { cropTagsBn }
    }

    /**
     * Strictly true ONLY when an actual remote video file (MP4/HLS HTTPS URL) has been uploaded.
     * Prevents misrepresenting static storyboard thumbnails as playable videos.
     */
    fun hasPlayableVideo(): Boolean = videoUrl.trim().startsWith("https://", ignoreCase = true)

    fun getAudioUrlForLanguage(language: AppLanguage): String = when (language) {
        AppLanguage.BENGALI -> audioUrlBn.trim()
        AppLanguage.HINDI -> audioUrlHi.trim()
        AppLanguage.ENGLISH -> audioUrlEn.trim()
    }

    fun getSubtitleUrlForLanguage(language: AppLanguage): String = when (language) {
        AppLanguage.BENGALI -> subtitleUrlBn.trim()
        AppLanguage.HINDI -> subtitleUrlHi.trim()
        AppLanguage.ENGLISH -> subtitleUrlEn.trim()
    }

    fun hasSeparateAudioTrack(language: AppLanguage): Boolean =
        getAudioUrlForLanguage(language).startsWith("https://", ignoreCase = true)

    fun hasSubtitleTrack(language: AppLanguage): Boolean =
        getSubtitleUrlForLanguage(language).startsWith("https://", ignoreCase = true) ||
            scenes.any { it.getSubtitle(language).isNotBlank() }

    fun formattedDuration(): String = formatMMSS(durationSeconds)

    @DrawableRes
    fun fallbackDrawableRes(): Int = when (thumbnailDrawableKey) {
        "img_video_seed_germination" -> R.drawable.img_video_seed_germination
        "img_video_paddy_irrigation" -> R.drawable.img_video_paddy_irrigation
        else -> R.drawable.img_video_soil_prep
    }

    fun getSafetyDisclaimer(language: AppLanguage): String = when (language) {
        AppLanguage.BENGALI ->
            "সাধারণ শিক্ষাগত নির্দেশিকা: মাটি ও ফসলের অবস্থা অনুযায়ী অনুমোদিত মাত্রা জানতে স্থানীয় KVK, কৃষি দপ্তর বা অনুমোদিত কৃষি বিশেষজ্ঞের সাথে যাচাই করুন।"
        AppLanguage.HINDI ->
            "सामान्य शैक्षिक मार्गदर्शन: अपनी मिट्टी व फसल की स्थिति के अनुसार सटीक मात्रा के लिए नजदीकी KVK, कृषि विभाग या प्रमाणित कृषि विशेषज्ञ से परामर्श लें।"
        AppLanguage.ENGLISH ->
            "General educational guide: Always confirm exact local doses, chemical labels, and field recommendations with your nearest KVK or verified agronomist."
    }

    fun matchesSearch(query: String): Boolean {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return true
        val haystack = buildString {
            append(titleBn).append(' ').append(titleHi).append(' ').append(titleEn).append(' ')
            append(summaryBn).append(' ').append(summaryHi).append(' ').append(summaryEn).append(' ')
            append(cropTagsBn.joinToString(" ")).append(' ')
            append(cropTagsHi.joinToString(" ")).append(' ')
            append(cropTagsEn.joinToString(" ")).append(' ')
            append(category().titleBn).append(' ').append(category().titleHi).append(' ').append(category().titleEn)
        }.lowercase()
        return haystack.contains(q)
    }
}

@Keep
@IgnoreExtraProperties
data class FarmerVideoProgress(
    @DocumentId val id: String = "",
    val userId: String = "",
    val lessonId: String = "",
    val lastPositionSeconds: Int = 0,
    val totalDurationSeconds: Int = 0,
    val completed: Boolean = false,
    val isFavorite: Boolean = false,
    val lastWatchedLanguage: String = AppLanguage.BENGALI.code,
    val updatedAtMillis: Long = 0L,
    val updatedAt: Timestamp? = null
) {
    fun progressFraction(): Float {
        if (completed) return 1f
        if (totalDurationSeconds <= 0) return 0f
        return (lastPositionSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
    }
}

fun formatMMSS(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val mins = safe / 60
    val secs = safe % 60
    return "%02d:%02d".format(mins, secs)
}
