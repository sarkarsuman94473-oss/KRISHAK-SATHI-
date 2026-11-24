package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 15 Core Agricultural Specialist Categories
 */
object ExpertCategories {
    const val PLANT_DISEASE = "PLANT_DISEASE"
    const val CROP_PROTECTION = "CROP_PROTECTION"
    const val SOIL_SCIENCE = "SOIL_SCIENCE"
    const val PLANT_NUTRITION = "PLANT_NUTRITION"
    const val POTATO_SPECIALIST = "POTATO_SPECIALIST"
    const val RICE_SPECIALIST = "RICE_SPECIALIST"
    const val VEGETABLE_SPECIALIST = "VEGETABLE_SPECIALIST"
    const val FRUIT_SPECIALIST = "FRUIT_SPECIALIST"
    const val TEA_GARDEN_SPECIALIST = "TEA_GARDEN_SPECIALIST"
    const val HORTICULTURE = "HORTICULTURE"
    const val SEED_NURSERY = "SEED_NURSERY"
    const val ENTOMOLOGY = "ENTOMOLOGY"
    const val IRRIGATION_WATER = "IRRIGATION_WATER"
    const val ORGANIC_FARMING = "ORGANIC_FARMING"
    const val VETERINARY = "VETERINARY"

    data class CategoryItem(
        val key: String,
        val bengaliName: String,
        val englishName: String,
        val hindiName: String,
        val icon: String
    ) {
        fun getName(lang: com.example.data.model.AppLanguage): String = when (lang) {
            com.example.data.model.AppLanguage.BENGALI -> bengaliName
            com.example.data.model.AppLanguage.HINDI -> hindiName
            com.example.data.model.AppLanguage.ENGLISH -> englishName
        }
    }

    val ALL_CATEGORIES = listOf(
        CategoryItem(PLANT_DISEASE, "উদ্ভিদ রোগ বিশেষজ্ঞ", "Plant Disease Specialist", "पौध रोग विशेषज्ञ", "🔬"),
        CategoryItem(CROP_PROTECTION, "ফসল সুরক্ষা বিশেষজ্ঞ", "Crop Protection Specialist", "फसल सुरक्षा विशेषज्ञ", "🛡️"),
        CategoryItem(SOIL_SCIENCE, "মৃত্তিকা বিজ্ঞানী", "Soil Scientist", "मृदा वैज्ञानिक", "🧪"),
        CategoryItem(PLANT_NUTRITION, "সার ও উদ্ভিদ পুষ্টি বিশেষজ্ঞ", "Plant Nutrition & Fertilizer", "उर्वरक व पोषण विशेषज्ञ", "🌿"),
        CategoryItem(POTATO_SPECIALIST, "আলু চাষ বিশেষজ্ঞ", "Potato Farming Specialist", "आलू की खेती विशेषज्ञ", "🥔"),
        CategoryItem(RICE_SPECIALIST, "ধান চাষ বিশেষজ্ঞ", "Rice Farming Specialist", "धान विशेषज्ञ", "🌾"),
        CategoryItem(VEGETABLE_SPECIALIST, "সবজি চাষ বিশেষজ্ঞ", "Vegetable Farming Specialist", "सब्जी विशेषज्ञ", "🥦"),
        CategoryItem(FRUIT_SPECIALIST, "ফল চাষ বিশেষজ্ঞ", "Fruit Farming Specialist", "फल विशेषज्ञ", "🥭"),
        CategoryItem(TEA_GARDEN_SPECIALIST, "চা বাগান বিশেষজ্ঞ", "Tea Garden Specialist", "चाय बागान विशेषज्ञ", "🍵"),
        CategoryItem(HORTICULTURE, "উদ্যানপালন বিশেষজ্ঞ", "Horticulture Specialist", "बागवानी विशेषज्ञ", "🌸"),
        CategoryItem(SEED_NURSERY, "বীজ ও নার্সারি বিশেষজ্ঞ", "Seed & Nursery Specialist", "बीज व नर्सरी विशेषज्ञ", "🌱"),
        CategoryItem(ENTOMOLOGY, "কীটতত্ত্ববিদ ও পোকা দমন", "Entomologist & Pest Control", "कीट विज्ञान विशेषज्ञ", "🐛"),
        CategoryItem(IRRIGATION_WATER, "সেচ ও জল ব্যবস্থাপনা", "Irrigation & Water Management", "सिंचाई व जल प्रबंधन", "💧"),
        CategoryItem(ORGANIC_FARMING, "জৈব কৃষি বিশেষজ্ঞ", "Organic Farming Specialist", "जैविक खेती विशेषज्ञ", "♻️"),
        CategoryItem(VETERINARY, "পশু চিকিৎসক ও পশুপালন", "Veterinary & Livestock Doctor", "पशु चिकित्सक", "🐄")
    )
}

@Entity(
    tableName = "agriculture_experts",
    indices = [
        Index(value = ["category"]),
        Index(value = ["isVerified"]),
        Index(value = ["registrationId"], unique = true)
    ]
)
data class ExpertEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val designation: String,
    val qualification: String,
    val category: String,
    val yearsExperience: Int,
    val registrationId: String,
    val institution: String,
    val languages: String, // comma separated e.g. "বাংলা, English, हिन्दी"
    val districtsServed: String, // e.g. "জলপাইগুড়ি, কোচবিহার, দার্জিলিং"
    val availabilityStatus: String, // "ONLINE_NOW", "AVAILABLE_TODAY", "APPOINTMENT_ONLY"
    val consultationFee: String, // e.g. "বিনামূল্যে (Free)"
    val isVerified: Boolean = false,
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val photoAvatar: String = "👨‍🔬",
    val supportedCrops: String = "", // e.g. "আলু, ধান, টমেটো, বাঁধাকপি"
    val specializedProblems: String = "", // e.g. "ধসা রোগ, পাতা হলুদ, ফল পচা"
    val phone: String = "",
    val email: String = "",
    val bio: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "consultation_appointments",
    indices = [
        Index(value = ["farmerId"]),
        Index(value = ["expertId"]),
        Index(value = ["expertId", "scheduledDate", "scheduledTimeSlot"], unique = true)
    ]
)
data class ConsultationAppointmentEntity(
    @PrimaryKey
    val id: String,
    val farmerId: String,
    val farmerName: String,
    val expertId: String,
    val expertName: String,
    val expertCategory: String,
    val cropName: String,
    val problemDescription: String,
    val consultationMethod: String, // "CHAT", "AUDIO_CALL", "VIDEO_CALL", "FARM_VISIT"
    val scheduledDate: String, // "YYYY-MM-DD"
    val scheduledTimeSlot: String, // "10:00 AM - 10:30 AM"
    val status: String, // "PENDING", "CONFIRMED", "REJECTED", "COMPLETED", "CANCELLED"
    val meetingNotes: String = "",
    val fee: String = "বিনামূল্যে (Free)",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "consultation_messages",
    indices = [
        Index(value = ["expertId", "farmerId", "timestamp"]),
        Index(value = ["farmerId"])
    ]
)
data class ConsultationMessageEntity(
    @PrimaryKey
    val id: String,
    val expertId: String,
    val farmerId: String,
    val senderRole: String, // "FARMER" or "EXPERT"
    val senderName: String,
    val messageText: String,
    val attachmentPhotoUri: String? = null,
    val cropContext: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(
    tableName = "expert_reviews",
    indices = [
        Index(value = ["expertId"]),
        Index(value = ["farmerId"])
    ]
)
data class ExpertReviewEntity(
    @PrimaryKey
    val id: String,
    val expertId: String,
    val farmerId: String,
    val farmerName: String,
    val rating: Int,
    val reviewText: String,
    val crop: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expert_audit_logs",
    indices = [
        Index(value = ["expertId", "timestamp"])
    ]
)
data class ExpertAuditLogEntity(
    @PrimaryKey
    val id: String,
    val expertId: String,
    val expertName: String,
    val adminAction: String, // "APPROVED", "REJECTED", "SUSPENDED", "ACTIVATED"
    val reason: String,
    val adminName: String = "Govt Agri Board Admin",
    val timestamp: Long = System.currentTimeMillis()
)
