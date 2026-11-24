package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.AppLanguage
import com.example.data.model.SeoArticleStatus
import com.example.data.model.SeoContentCategory
import com.example.data.model.SeoFaqPair

@Entity(
    tableName = "seo_articles",
    indices = [
        Index(value = ["slug"], unique = true),
        Index(value = ["status", "category"]),
        Index(value = ["updatedAtMillis"])
    ]
)
data class SeoArticleEntity(
    @PrimaryKey val id: String,
    val slug: String,
    val category: String,
    val cropId: String,
    val status: String,
    val titleEn: String,
    val titleBn: String,
    val titleHi: String,
    val metaDescriptionEn: String,
    val metaDescriptionBn: String,
    val metaDescriptionHi: String,
    val summaryEn: String,
    val summaryBn: String,
    val summaryHi: String,
    val bodyEn: String,
    val bodyBn: String,
    val bodyHi: String,
    val faqsSerialized: String,
    val sourceName: String,
    val sourceUrl: String,
    val canonicalUrl: String,
    val lastUpdatedDate: String,
    val isAiAssisted: Boolean,
    val reviewedByAdmin: String,
    val updatedAtMillis: Long
) {
    fun statusEnum(): SeoArticleStatus = SeoArticleStatus.fromWire(status)
    fun categoryEnum(): SeoContentCategory = SeoContentCategory.fromWire(category)

    fun title(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> titleBn.ifBlank { titleEn }
        AppLanguage.HINDI -> titleHi.ifBlank { titleEn }
        AppLanguage.ENGLISH -> titleEn.ifBlank { titleBn }
    }

    fun metaDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> metaDescriptionBn.ifBlank { metaDescriptionEn }
        AppLanguage.HINDI -> metaDescriptionHi.ifBlank { metaDescriptionEn }
        AppLanguage.ENGLISH -> metaDescriptionEn.ifBlank { metaDescriptionBn }
    }

    fun summary(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> summaryBn.ifBlank { summaryEn }
        AppLanguage.HINDI -> summaryHi.ifBlank { summaryEn }
        AppLanguage.ENGLISH -> summaryEn.ifBlank { summaryBn }
    }

    fun body(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bodyBn.ifBlank { bodyEn }
        AppLanguage.HINDI -> bodyHi.ifBlank { bodyEn }
        AppLanguage.ENGLISH -> bodyEn.ifBlank { bodyBn }
    }

    fun parsedFaqs(): List<SeoFaqPair> {
        if (faqsSerialized.isBlank()) return emptyList()
        return faqsSerialized.split("||").mapNotNull { entry ->
            val parts = entry.split("::")
            if (parts.size >= 2) SeoFaqPair(parts[0].trim(), parts[1].trim()) else null
        }
    }
}

@Entity(tableName = "seo_config")
data class SeoConfigEntity(
    @PrimaryKey val id: String = "default",
    val baseWebUrl: String,
    val googleSearchConsoleToken: String,
    val allowGooglebot: Boolean,
    val allowOaiSearchBot: Boolean,
    val allowGptBot: Boolean,
    val playStoreStatus: String,
    val updatedByAdmin: String,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "seo_referral_logs",
    indices = [
        Index(value = ["sourceBucket"]),
        Index(value = ["timestampMillis"])
    ]
)
data class SeoReferralLogEntity(
    @PrimaryKey val id: String,
    val sourceBucket: String,
    val targetSlug: String,
    val languageCode: String,
    val timestampMillis: Long
)
