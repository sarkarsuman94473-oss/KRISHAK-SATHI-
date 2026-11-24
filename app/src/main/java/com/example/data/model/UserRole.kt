package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Three primary user roles for KRISHAK SATHI:
 * 1. CUSTOMER (Ordinary farmers & buyers, default role)
 * 2. NURSERY_OWNER (Approved agricultural business sellers)
 * 3. ADMIN (Platform administrator)
 */
enum class UserRole(val code: String, val bnLabel: String, val hiLabel: String, val enLabel: String) {
    CUSTOMER("CUSTOMER", "গ্রাহক / কৃষক", "ग्राहक / किसान", "Customer / Farmer"),
    NURSERY_OWNER("NURSERY_OWNER", "নার্সারি মালিক / বিক্রেতা", "नर्सरी मालिक / विक्रेता", "Nursery Owner / Seller"),
    ADMIN("ADMIN", "সিস্টেম অ্যাডমিন", "सिस्टम एडमिन", "System Administrator");

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnLabel
        AppLanguage.HINDI -> hiLabel
        AppLanguage.ENGLISH -> enLabel
    }

    companion object {
        fun fromCode(code: String?): UserRole {
            return when (code?.uppercase()) {
                "ADMIN" -> ADMIN
                "NURSERY_OWNER", "SELLER" -> NURSERY_OWNER
                "CUSTOMER", "FARMER", "EXPERT", null, "" -> CUSTOMER
                else -> CUSTOMER
            }
        }
    }
}

/**
 * Nursery Owner Application Status:
 * - NOT_APPLIED: User has not initiated an application
 * - DRAFT: Application saved locally/remotely, not yet submitted
 * - PENDING: Submitted by user, awaiting admin review
 * - UNDER_REVIEW: Admin has opened and is actively reviewing
 * - APPROVED: Approved by admin, user role upgraded to NURSERY_OWNER
 * - REJECTED: Rejected by admin with explanatory reason, user can edit & resubmit
 * - SUSPENDED: Previously approved seller suspended by admin
 */
enum class NurseryApplicationStatus(val code: String, val bnLabel: String, val hiLabel: String, val enLabel: String, val colorHex: Long) {
    NOT_APPLIED("NOT_APPLIED", "আবেদন করা হয়নি", "आवेदन नहीं किया गया", "Not Applied", 0xFF9E9E9E),
    DRAFT("DRAFT", "খসড়া সংরক্ষিত", "ड्राफ्ट सहेजा गया", "Draft", 0xFF757575),
    PENDING("PENDING", "অনুমোদনের অপেক্ষায়", "स्वीकृति की प्रतीक्षा में", "Pending Approval", 0xFFF57F17),
    UNDER_REVIEW("UNDER_REVIEW", "পর্যালোচনাধীন", "समीक्षाधीन", "Under Review", 0xFF0288D1),
    APPROVED("APPROVED", "অনুমোদিত ও সক্রিয়", "स्वीकृत एवं सक्रिय", "Approved & Active", 0xFF2E7D32),
    REJECTED("REJECTED", "আবেদন বাতিল", "आवेदन अस्वीकृत", "Application Rejected", 0xFFC62828),
    SUSPENDED("SUSPENDED", "স্থগিত", "निलंबित", "Suspended", 0xFFB71C1C);

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnLabel
        AppLanguage.HINDI -> hiLabel
        AppLanguage.ENGLISH -> enLabel
    }

    companion object {
        fun fromCode(code: String?): NurseryApplicationStatus {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: NOT_APPLIED
        }
    }
}

/**
 * Complete Nursery Business Application & Profile entity.
 */
@IgnoreExtraProperties
data class NurseryApplication(
    @DocumentId val id: String = "",
    val userId: String = "",
    val applicantName: String = "",
    val mobileNumber: String = "",
    val email: String = "",
    val isMobileVerified: Boolean = true,
    
    // STEP A: Basic Business Information
    val nurseryNameBn: String = "",
    val nurseryNameEn: String = "",
    val nurseryLogoUrl: String = "",
    val nurseryCoverUrl: String = "",
    val description: String = "",
    val primaryCategory: String = "FRUITS",
    val establishmentYear: String = "",
    
    // STEP B: Address
    val country: String = "India",
    val state: String = "West Bengal",
    val district: String = "Jalpaiguri",
    val cityOrTown: String = "Siliguri",
    val fullAddress: String = "",
    val pinCode: String = "",
    
    // STEP C: Location
    val latitude: Double = 26.7271,
    val longitude: Double = 88.4239,
    val mapLocationUrl: String = "",
    val isLocationAutoDetected: Boolean = false,
    
    // STEP D: Business Contact
    val businessPhone: String = "",
    val whatsappNumber: String = "",
    val businessEmail: String = "",
    val openingHours: String = "08:00 AM - 06:00 PM",
    val isDeliveryAvailable: Boolean = true,
    val deliveryRadiusKm: Int = 30,
    
    // STEP E: Verification Documents
    val tradeLicenseNumber: String = "",
    val documentType: String = "Trade License",
    val documentNotes: String = "",
    val gstNumber: String = "",
    
    // Status & Admin Decisions
    val status: String = NurseryApplicationStatus.PENDING.code,
    val adminRejectionReason: String = "",
    val adminNotes: String = "",
    val reviewedByAdminEmail: String = "",
    val submittedAtMillis: Long = System.currentTimeMillis(),
    val reviewedAtMillis: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun getDisplayName(lang: AppLanguage): String =
        com.example.data.datasource.NurseryTranslations.getApplicationDisplayName(this, lang)

    fun getDisplayOwnerName(lang: AppLanguage): String =
        com.example.data.datasource.NurseryTranslations.getApplicationOwnerName(this, lang)

    fun getDisplayDescription(lang: AppLanguage): String =
        com.example.data.datasource.NurseryTranslations.getApplicationDescription(this, lang)

    fun getDisplayFullAddress(lang: AppLanguage): String =
        com.example.data.datasource.NurseryTranslations.getApplicationFullAddress(this, lang)

    fun getDisplayOpeningHours(lang: AppLanguage): String =
        com.example.data.datasource.NurseryTranslations.getApplicationOpeningHours(this, lang)
}

