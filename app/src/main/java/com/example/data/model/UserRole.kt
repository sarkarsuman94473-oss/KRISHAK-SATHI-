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

/**
 * Single-App Multi-Role Marketplace Roles for KRISHAK SATHI.
 * Eligible users can hold multiple approved roles under one account while
 * Administrator privileges remain strictly server/email-verified and non-selectable.
 */
enum class AppUserRole(
    val code: String,
    val bnLabel: String,
    val hiLabel: String,
    val enLabel: String,
    val isPartnerRole: Boolean,
    val isSelectableInPartnerRegistration: Boolean
) {
    FARMER_CUSTOMER(
        code = "FARMER_CUSTOMER",
        bnLabel = "কৃষক / গ্রাহক",
        hiLabel = "किसान / ग्राहक",
        enLabel = "Farmer / Customer",
        isPartnerRole = false,
        isSelectableInPartnerRegistration = false
    ),
    NURSERY_SELLER(
        code = "NURSERY_SELLER",
        bnLabel = "নার্সারি বিক্রেতা",
        hiLabel = "नर्सरी विक्रेता",
        enLabel = "Nursery Seller",
        isPartnerRole = true,
        isSelectableInPartnerRegistration = true
    ),
    MACHINERY_DEALER(
        code = "MACHINERY_DEALER",
        bnLabel = "যন্ত্রপাতি ডিলার",
        hiLabel = "मशीनरी डीलर",
        enLabel = "Machinery Dealer",
        isPartnerRole = true,
        isSelectableInPartnerRegistration = true
    ),
    MACHINERY_RENTAL_OWNER(
        code = "MACHINERY_RENTAL_OWNER",
        bnLabel = "যন্ত্র ভাড়া প্রদানকারী",
        hiLabel = "मशीनरी किराया मालिक",
        enLabel = "Machinery Rental Owner",
        isPartnerRole = true,
        isSelectableInPartnerRegistration = true
    ),
    CROP_BUYER(
        code = "CROP_BUYER",
        bnLabel = "ফসল ক্রেতা",
        hiLabel = "फसल खरीदार",
        enLabel = "Crop Buyer",
        isPartnerRole = true,
        isSelectableInPartnerRegistration = true
    ),
    FARM_SERVICE_PROVIDER(
        code = "FARM_SERVICE_PROVIDER",
        bnLabel = "কৃষি পরিষেবা প্রদানকারী",
        hiLabel = "कृषि सेवा प्रदाता",
        enLabel = "Farm Service Provider",
        isPartnerRole = true,
        isSelectableInPartnerRegistration = true
    ),
    ADMINISTRATOR(
        code = "ADMINISTRATOR",
        bnLabel = "সিস্টেম অ্যাডমিনিস্ট্রেটর",
        hiLabel = "सिस्टम एडमिनिस्ट्रेटर",
        enLabel = "Administrator",
        isPartnerRole = false,
        isSelectableInPartnerRegistration = false
    );

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnLabel
        AppLanguage.HINDI -> hiLabel
        AppLanguage.ENGLISH -> enLabel
    }

    companion object {
        val partnerRegistrationChoices: List<AppUserRole>
            get() = values().filter { it.isSelectableInPartnerRegistration && it != ADMINISTRATOR }

        fun fromCode(code: String?): AppUserRole {
            return when (code?.trim()?.uppercase()) {
                "ADMINISTRATOR", "ADMIN" -> ADMINISTRATOR
                "NURSERY_SELLER", "NURSERY_OWNER", "SELLER" -> NURSERY_SELLER
                "MACHINERY_DEALER" -> MACHINERY_DEALER
                "MACHINERY_RENTAL_OWNER" -> MACHINERY_RENTAL_OWNER
                "CROP_BUYER" -> CROP_BUYER
                "FARM_SERVICE_PROVIDER" -> FARM_SERVICE_PROVIDER
                else -> FARMER_CUSTOMER
            }
        }

        fun parseRoleCodes(codes: Collection<String>): Set<AppUserRole> {
            val resolved = mutableSetOf(FARMER_CUSTOMER)
            codes.forEach { raw ->
                val role = fromCode(raw)
                if (role != ADMINISTRATOR) {
                    resolved.add(role)
                }
            }
            return resolved
        }
    }
}

enum class DashboardMode(
    val code: String,
    val bnLabel: String,
    val hiLabel: String,
    val enLabel: String
) {
    FARMER_DASHBOARD(
        code = "FARMER_DASHBOARD",
        bnLabel = "কৃষক ড্যাশবোর্ড",
        hiLabel = "किसान डैशबोर्ड",
        enLabel = "Farmer Dashboard"
    ),
    PARTNER_DASHBOARD(
        code = "PARTNER_DASHBOARD",
        bnLabel = "পার্টনার ড্যাশবোর্ড",
        hiLabel = "पार्टनर डैशबोर्ड",
        enLabel = "Partner Dashboard"
    );

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnLabel
        AppLanguage.HINDI -> hiLabel
        AppLanguage.ENGLISH -> enLabel
    }

    companion object {
        fun fromCode(code: String?): DashboardMode =
            values().firstOrNull { it.code.equals(code?.trim(), ignoreCase = true) } ?: FARMER_DASHBOARD
    }
}

enum class PartnerRegistrationStatus(
    val code: String,
    val bnLabel: String,
    val hiLabel: String,
    val enLabel: String,
    val colorHex: Long
) {
    PENDING("PENDING", "জমা হয়েছে (অপেক্ষমাণ)", "लंबित (समीक्षा शेष)", "Pending", 0xFFF57F17),
    UNDER_REVIEW("UNDER_REVIEW", "পর্যালোচনাধীন", "समीक्षाधीन", "Under Review", 0xFF0288D1),
    APPROVED("APPROVED", "অনুমোদিত পার্টনার", "स्वीकृत पार्टनर", "Approved", 0xFF2E7D32),
    REJECTED("REJECTED", "বাতিলকৃত (সংশোধন প্রয়োজন)", "अस्वीकृत (सुधार आवश्यक)", "Rejected", 0xFFC62828);

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnLabel
        AppLanguage.HINDI -> hiLabel
        AppLanguage.ENGLISH -> enLabel
    }

    companion object {
        fun fromCode(code: String?): PartnerRegistrationStatus? =
            values().firstOrNull { it.code.equals(code?.trim(), ignoreCase = true) }
    }
}

@IgnoreExtraProperties
data class UserRoleMembership(
    @DocumentId val id: String = "",
    val userId: String = "",
    val approvedRoles: List<String> = listOf(AppUserRole.FARMER_CUSTOMER.code),
    val platformVerified: Boolean = false,
    val verifiedBusinessName: String = "",
    val activeDashboardMode: String = DashboardMode.FARMER_DASHBOARD.code,
    val updatedByAdminEmail: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun resolvedRoles(includeAdmin: Boolean = false): Set<AppUserRole> {
        val set = AppUserRole.parseRoleCodes(approvedRoles).toMutableSet()
        if (includeAdmin && approvedRoles.any { it.equals(AppUserRole.ADMINISTRATOR.code, ignoreCase = true) }) {
            set.add(AppUserRole.ADMINISTRATOR)
        }
        return set
    }

    fun hasApprovedPartnerRole(): Boolean =
        resolvedRoles().any { it.isPartnerRole }
}

@IgnoreExtraProperties
data class PartnerRegistration(
    @DocumentId val id: String = "",
    val userId: String = "",
    val applicantName: String = "",
    val businessName: String = "",
    val contactPhone: String = "",
    val whatsappPhone: String = "",
    val contactEmail: String = "",
    val businessLocation: String = "",
    val district: String = "",
    val state: String = "West Bengal",
    val pinCode: String = "",
    val serviceCategories: List<String> = emptyList(),
    val serviceArea: String = "",
    val serviceRadiusKm: Int = 30,
    val documentType: String = "Trade License",
    val licenseNumber: String = "",
    val gstNumber: String = "",
    val documentNotes: String = "",
    val verificationDocumentNames: List<String> = emptyList(),
    val status: String = PartnerRegistrationStatus.PENDING.code,
    val platformVerified: Boolean = false,
    val approvedRoles: List<String> = emptyList(),
    val adminNotes: String = "",
    val rejectionReason: String = "",
    val reviewedByAdminEmail: String = "",
    val submittedAtMillis: Long = System.currentTimeMillis(),
    val reviewedAtMillis: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun isVerifiedPartner(): Boolean =
        status.equals(PartnerRegistrationStatus.APPROVED.code, ignoreCase = true) && platformVerified

    fun requestedPartnerRoles(): List<AppUserRole> =
        serviceCategories
            .map { AppUserRole.fromCode(it) }
            .filter { it.isSelectableInPartnerRegistration }
            .distinct()

    fun resolvedApprovedPartnerRoles(): List<AppUserRole> =
        if (!isVerifiedPartner()) emptyList()
        else approvedRoles
            .map { AppUserRole.fromCode(it) }
            .filter { it.isSelectableInPartnerRegistration }
            .distinct()
}
