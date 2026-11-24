package com.example.data.model

/**
 * Publishing lifecycle status for SEO Knowledge Hub & Crop Guide articles.
 * AI-assisted drafts ALWAYS begin as [AI_DRAFT_PENDING_REVIEW] and require explicit admin approval.
 */
enum class SeoArticleStatus(val wireValue: String) {
    DRAFT("DRAFT"),
    AI_DRAFT_PENDING_REVIEW("AI_DRAFT_PENDING_REVIEW"),
    PUBLISHED("PUBLISHED"),
    ARCHIVED("ARCHIVED");

    companion object {
        fun fromWire(raw: String?): SeoArticleStatus =
            entries.firstOrNull { it.wireValue == raw || it.name == raw } ?: DRAFT
    }
}

enum class SeoContentCategory(
    val wireValue: String,
    val labelEn: String,
    val labelBn: String,
    val labelHi: String
) {
    CROP_GUIDE("CROP_GUIDE", "Crop Cultivation Guides", "ফসল চাষ গাইড", "फसल खेती गाइड"),
    DISEASE_PEST("DISEASE_PEST", "Diseases & Pest ID", "রোগ ও পোকা শনাক্তকরণ", "रोग एवं कीट पहचान"),
    SOIL_NUTRIENTS("SOIL_NUTRIENTS", "Soil Health & Nutrients", "মাটির স্বাস্থ্য ও পুষ্টি", "मृदा स्वास्थ्य व पोषण"),
    IRRIGATION_WATER("IRRIGATION_WATER", "Irrigation & Water", "সেচ ও জল ব্যবস্থাপনা", "सिंचाई व जल प्रबंधन"),
    ORGANIC_IPM("ORGANIC_IPM", "Organic Farming & IPM", "জৈব চাষ ও IPM", "जैविक खेती व IPM"),
    PESTICIDE_SAFETY("PESTICIDE_SAFETY", "Pesticide Safety & CIBRC", "কীটনাশক সুরক্ষা", "कीटनाशक सुरक्षा"),
    GOVT_SCHEMES("GOVT_SCHEMES", "Govt Farmer Schemes", "সরকারি কৃষি প্রকল্প", "सरकारी किसान योजनाएं"),
    NURSERY_SEEDS("NURSERY_SEEDS", "Nurseries & Certified Seeds", "নার্সারি ও বীজ", "नर्सरी व प्रमाणित बीज"),
    MACHINERY_STORAGE("MACHINERY_STORAGE", "Harvest, Storage & Tools", "সংগ্রহ, সংরক্ষণ ও যন্ত্র", "कटाई, भंडारण व उपकरण"),
    SILIGURI_NORTH_BENGAL("SILIGURI_NORTH_BENGAL", "Siliguri & North Bengal Hub", "শিলিগুড়ি ও উত্তরবঙ্গ", "सिलीगुड़ी व उत्तर बंगाल"),
    BRAND_STORY("BRAND_STORY", "Brand & Ecosystem", "ব্র্যান্ড ও পরিচিতি", "ब्रांड व परिचय");

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> labelBn
        AppLanguage.HINDI -> labelHi
        AppLanguage.ENGLISH -> labelEn
    }

    companion object {
        fun fromWire(raw: String?): SeoContentCategory =
            entries.firstOrNull { it.wireValue == raw || it.name == raw } ?: CROP_GUIDE
    }
}

data class SeoFaqPair(
    val question: String,
    val answer: String
)

data class SeoTechnicalAuditResult(
    val robotsTxtValid: Boolean,
    val googlebotAllowed: Boolean,
    val oaiSearchBotAllowed: Boolean,
    val gptBotSeparated: Boolean,
    val adminAndPrivateDisallowed: Boolean,
    val sitemapValid: Boolean,
    val publishedCount: Int,
    val pendingReviewDrafts: Int,
    val missingCitationCount: Int,
    val notes: List<String>
)

data class SeoReferralSummary(
    val totalVisits: Int,
    val countsByBucket: Map<String, Int>,
    val topSlugs: List<Pair<String, Int>>
)

enum class FeatureAvailabilityStatus {
    LIVE,
    UNDER_DEVELOPMENT,
    PLANNED
}

data class BrandCapabilityRow(
    val nameEn: String,
    val nameBn: String,
    val nameHi: String,
    val status: FeatureAvailabilityStatus,
    val detailEn: String,
    val detailBn: String,
    val detailHi: String
) {
    fun getName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> nameBn
        AppLanguage.HINDI -> nameHi
        AppLanguage.ENGLISH -> nameEn
    }

    fun getDetail(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> detailBn
        AppLanguage.HINDI -> detailHi
        AppLanguage.ENGLISH -> detailEn
    }
}
