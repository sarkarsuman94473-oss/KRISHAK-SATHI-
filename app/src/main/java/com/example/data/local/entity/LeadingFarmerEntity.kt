package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object FarmerCategoryTags {
    const val ALL = "ALL"
    const val NATURAL_FARMING = "NATURAL_FARMING"
    const val ORGANIC_FARMING = "ORGANIC_FARMING"
    const val MODERN_TECH = "MODERN_TECH"
    const val HORTICULTURE = "HORTICULTURE"
    const val SEED_CONSERVATION = "SEED_CONSERVATION"
    const val AQUACULTURE_INTEGRATED = "AQUACULTURE_INTEGRATED"

    data class FilterCategory(
        val key: String,
        val nameBn: String,
        val nameHi: String,
        val nameEn: String,
        val icon: String
    ) {
        fun getName(lang: com.example.data.model.AppLanguage): String = when (lang) {
            com.example.data.model.AppLanguage.BENGALI -> nameBn
            com.example.data.model.AppLanguage.HINDI -> nameHi.ifBlank { nameEn }
            com.example.data.model.AppLanguage.ENGLISH -> nameEn
        }
    }

    val FILTERS = listOf(
        FilterCategory(ALL, "সকল কৃষক", "सभी किसान", "All Farmers", "🌾"),
        FilterCategory(NATURAL_FARMING, "প্রাকৃতিক কৃষি (ZBNF)", "प्राकृतिक खेती (ZBNF)", "Natural Farming", "🌱"),
        FilterCategory(ORGANIC_FARMING, "জৈব কৃষি", "जैविक खेती", "Organic Farming", "♻️"),
        FilterCategory(MODERN_TECH, "উন্নত প্রযুক্তি ও উদ্ভাবন", "आधुनिक तकनीक व नवाचार", "Modern Tech & Innovation", "⚡"),
        FilterCategory(HORTICULTURE, "উদ্যানপালন ও শাকসবজি", "बागवानी व सब्जियां", "Horticulture & Vegetables", "🥦"),
        FilterCategory(SEED_CONSERVATION, "দেশীয় বীজ ও শস্য সংরক্ষণ", "देसी बीज व फसल संरक्षण", "Seed Conservation", "🫘"),
        FilterCategory(AQUACULTURE_INTEGRATED, "সমন্বিত কৃষি ও মৎস্য", "समेकित कृषि व मत्स्य", "Aquaculture & Integrated", "🐟")
    )
}

@Entity(
    tableName = "leading_farmers",
    indices = [
        Index(value = ["state"]),
        Index(value = ["categoryTag"]),
        Index(value = ["isApproved"]),
        Index(value = ["isFeatured"])
    ]
)
data class LeadingFarmerEntity(
    @PrimaryKey
    val id: String,
    val fullNameEn: String,
    val fullNameBn: String,
    val fullNameHi: String,
    val photoUrl: String? = null,
    val photoResName: String = "ic_krishak_sathi_1793336218748",
    val state: String,
    val district: String,
    val farmingSpecializationEn: String,
    val farmingSpecializationBn: String,
    val farmingSpecializationHi: String,
    val categoryTag: String,
    val mainCrops: String,
    val farmingTechniques: String,
    val yearsOfExperience: Int,
    val keyAwardEn: String,
    val keyAwardBn: String,
    val allAwards: String,
    val innovationContributionEn: String,
    val innovationContributionBn: String,
    val innovationContributionHi: String,
    // Detailed Story & Insights
    val storyHowStarted: String,
    val storyChallenges: String,
    val storyMethods: String,
    val storyAchievements: String,
    val storyLessonsForFarmers: String,
    // Verified Public Contact Info
    val officialBusinessPhone: String? = null,
    val officialPublicContact: String? = null,
    val officialEmail: String? = null,
    val officialWebsite: String? = null,
    val officialFacebook: String? = null,
    val officialInstagram: String? = null,
    val officialYoutube: String? = null,
    val organizationFpoDetails: String? = null,
    val hasPublicContact: Boolean = false,
    val directContactNotice: String = "Direct contact information is not publicly available.",
    // Verification & Admin fields
    val sourceReferences: String,
    val lastVerifiedDate: String,
    val isApproved: Boolean = true,
    val isFeatured: Boolean = true,
    val allowsContactRequests: Boolean = true
)

@Entity(
    tableName = "farmer_contact_requests",
    indices = [
        Index(value = ["farmerId"]),
        Index(value = ["timestamp"])
    ]
)
data class FarmerContactRequestEntity(
    @PrimaryKey
    val id: String,
    val farmerId: String,
    val farmerName: String,
    val requesterName: String,
    val requesterPhone: String,
    val requesterDistrict: String,
    val requesterCrop: String,
    val purposeCategory: String,
    val message: String,
    val status: String = "Submitted",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "farmer_audit_logs",
    indices = [
        Index(value = ["farmerId"]),
        Index(value = ["timestamp"])
    ]
)
data class FarmerAuditLogEntity(
    @PrimaryKey
    val id: String,
    val farmerId: String,
    val action: String,
    val performedBy: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
