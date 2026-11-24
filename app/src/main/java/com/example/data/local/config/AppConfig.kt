package com.example.data.local.config

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Configurable metadata for KRISHAK SATHI, including Government Registration & Approval details,
 * Release dates, Versioning, Social Media URLs, and Support Contact information.
 */
data class AppConfig(
    val appName: String = "KRISHAK SATHI",
    val appSubtitleBn: String = "কৃষকের পাশে, সবসময়",
    val appSubtitleHi: String = "किसान के साथ, हमेशा",
    val appSubtitleEn: String = "Always Beside the Farmer",
    val appDescription: String = "KRISHAK SATHI is a digital agriculture support platform designed to help farmers access agricultural information, AI assistance, expert consultations, crop guidance, weather updates, and agricultural marketplace services.",
    val releaseDate: String = "January 15, 2026",
    val currentVersion: String = "1.0.0",
    val lastUpdatedDate: String = "November 3, 2026",
    val copyrightText: String = "© 2026 KRISHAK SATHI. All Rights Reserved.",

    // Government Registration & Approval (Honest by default; no fake numbers or claims)
    val isGovtApproved: Boolean = false,
    val govtRegistrationStatusText: String = "সরকারি নিবন্ধন ও অনুমোদনের বিবরণী অফিসিয়াল যাচাইয়ের পর প্রকাশিত হবে। (Government registration or approval details will be published after official verification.)",
    val govtCertificateNo: String = "",
    val govtIssuingAuthority: String = "পশ্চিমবঙ্গ কৃষি বিভাগ ও কেন্দ্রীয় কৃষি মন্ত্রক (পর্যালোচনাধীন)",
    val govtApprovalDate: String = "",
    val govtReferenceNo: String = "",
    val govtDepartmentInfo: String = "Department of Agriculture, Govt. of West Bengal & Ministry of Agriculture & Farmers Welfare, Govt. of India",
    val govtOfficialWebsites: String = "https://banglarkrishi.wb.gov.in, https://pmkisan.gov.in, https://agricoop.nic.in",

    // Social Media Links (Official/Configurable; if blank, icon will be hidden)
    val facebookUrl: String = "https://www.facebook.com/krishaksathiofficial",
    val instagramUrl: String = "https://www.instagram.com/krishaksathi_app",
    val youtubeUrl: String = "https://www.youtube.com/@krishaksathi",
    val whatsappUrl: String = "",
    val twitterUrl: String = "", // empty by default so it's safely hidden

    // Contact and Support
    val supportEmail: String = "support@krishaksathi.in",
    val supportPhone: String = "1800-180-1551 (টোল ফ্রি কিষাণ কল সেন্টার)",
    val businessAddress: String = "কৃষক সাথী ডিজিটাল এগ্রি সেন্টার, জলপাইগুড়ি / কলকাতা, পশ্চিমবঙ্গ, ভারত - ৭০০০০১",
    val supportHours: String = "সোম - শনি: সকাল ৬:০০টা - রাত ১০:০০টা",
    val officialWebsite: String = "https://krishaksathi.in"
) {
    fun getSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> appSubtitleBn
        AppLanguage.HINDI -> appSubtitleHi
        AppLanguage.ENGLISH -> appSubtitleEn
    }

    fun getDescription(lang: AppLanguage): String {
        val defaultDesc = "KRISHAK SATHI is a digital agriculture support platform designed to help farmers access agricultural information, AI assistance, expert consultations, crop guidance, weather updates, and agricultural marketplace services."
        if (appDescription != defaultDesc && lang == AppLanguage.ENGLISH) return appDescription
        return when (lang) {
            AppLanguage.BENGALI -> "কৃষক সাথী হলো কৃষকদের জন্য একটি আধুনিক ডিজিটাল কৃষি সহায়তা প্ল্যাটফর্ম — যেখানে কৃষি তথ্য, AI সহায়তা, বিশেষজ্ঞ পরামর্শ, ফসল নির্দেশিকা, আবহাওয়া ও কৃষি মার্কেটপ্লেস সেবা পাওয়া যায়।"
            AppLanguage.HINDI -> "कृषक साथी किसानों के लिए एक आधुनिक डिजिटल कृषि सहायता प्लेटफॉर्म है — जहाँ कृषि जानकारी, एआई सहायता, विशेषज्ञ परामर्श, फसल मार्गदर्शन, मौसम अपडेट और कृषि मार्केटप्लेस सेवाएँ मिलती हैं।"
            AppLanguage.ENGLISH -> appDescription
        }
    }

    fun getSupportPhone(lang: AppLanguage): String {
        val defaultPhone = "1800-180-1551 (টোল ফ্রি কিষাণ কল সেন্টার)"
        if (supportPhone != defaultPhone) return supportPhone
        return when (lang) {
            AppLanguage.BENGALI -> "1800-180-1551 (টোল ফ্রি কিষাণ কল সেন্টার)"
            AppLanguage.HINDI -> "1800-180-1551 (टोल फ्री किसान कॉल सेंटर)"
            AppLanguage.ENGLISH -> "1800-180-1551 (Toll-Free Kisan Call Centre)"
        }
    }

    fun getBusinessAddress(lang: AppLanguage): String {
        val defaultAddr = "কৃষক সাথী ডিজিটাল এগ্রি সেন্টার, জলপাইগুড়ি / কলকাতা, পশ্চিমবঙ্গ, ভারত - ৭০০০০১"
        if (businessAddress != defaultAddr) return businessAddress
        return when (lang) {
            AppLanguage.BENGALI -> defaultAddr
            AppLanguage.HINDI -> "कृषक साथी डिजिटल एग्री सेंटर, जलपाईगुड़ी / कोलकाता, पश्चिम बंगाल, भारत - 700001"
            AppLanguage.ENGLISH -> "Krishak Sathi Digital Agri Centre, Jalpaiguri / Kolkata, West Bengal, India - 700001"
        }
    }

    fun getSupportHours(lang: AppLanguage): String {
        val defaultHours = "সোম - শনি: সকাল ৬:০০টা - রাত ১০:০০টা"
        if (supportHours != defaultHours) return supportHours
        return when (lang) {
            AppLanguage.BENGALI -> defaultHours
            AppLanguage.HINDI -> "सोमवार - शनिवार: सुबह 6:00 बजे - रात 10:00 बजे"
            AppLanguage.ENGLISH -> "Mon - Sat: 6:00 AM - 10:00 PM"
        }
    }

    fun getGovtRegistrationStatusText(lang: AppLanguage): String {
        val defaultStatus = "সরকারি নিবন্ধন ও অনুমোদনের বিবরণী অফিসিয়াল যাচাইয়ের পর প্রকাশিত হবে। (Government registration or approval details will be published after official verification.)"
        if (govtRegistrationStatusText != defaultStatus) return govtRegistrationStatusText
        return when (lang) {
            AppLanguage.BENGALI -> "সরকারি নিবন্ধন ও অনুমোদনের বিবরণী অফিসিয়াল যাচাইয়ের পর প্রকাশিত হবে।"
            AppLanguage.HINDI -> "सरकारी पंजीकरण और अनुमोदन का विवरण आधिकारिक सत्यापन के बाद प्रकाशित किया जाएगा।"
            AppLanguage.ENGLISH -> "Government registration and approval details will be published after official verification."
        }
    }

    fun getGovtIssuingAuthority(lang: AppLanguage): String {
        val defaultAuth = "পশ্চিমবঙ্গ কৃষি বিভাগ ও কেন্দ্রীয় কৃষি মন্ত্রক (পর্যালোচনাধীন)"
        if (govtIssuingAuthority != defaultAuth) return govtIssuingAuthority
        return when (lang) {
            AppLanguage.BENGALI -> defaultAuth
            AppLanguage.HINDI -> "पश्चिम बंगाल कृषि विभाग एवं केंद्रीय कृषि मंत्रालय (समीक्षाधीन)"
            AppLanguage.ENGLISH -> "Department of Agriculture (WB) & Ministry of Agriculture (Under Review)"
        }
    }

    fun getGovtDepartmentInfo(lang: AppLanguage): String {
        val defaultDept = "Department of Agriculture, Govt. of West Bengal & Ministry of Agriculture & Farmers Welfare, Govt. of India"
        if (govtDepartmentInfo != defaultDept && lang == AppLanguage.ENGLISH) return govtDepartmentInfo
        return when (lang) {
            AppLanguage.BENGALI -> "কৃষি বিভাগ, পশ্চিমবঙ্গ সরকার এবং কৃষি ও কৃষক কল্যাণ মন্ত্রক, ভারত সরকার"
            AppLanguage.HINDI -> "कृषि विभाग, पश्चिम बंगाल सरकार एवं कृषि व किसान कल्याण मंत्रालय, भारत सरकार"
            AppLanguage.ENGLISH -> govtDepartmentInfo
        }
    }
}

object AppConfigManager {
    private const val PREFS_NAME = "krishak_sathi_app_config"

    private const val KEY_APP_NAME = "key_app_name"
    private const val KEY_APP_DESC = "key_app_desc"
    private const val KEY_RELEASE_DATE = "key_release_date"
    private const val KEY_CURRENT_VERSION = "key_current_version"
    private const val KEY_LAST_UPDATED = "key_last_updated"
    private const val KEY_COPYRIGHT = "key_copyright"

    private const val KEY_IS_GOVT_APPROVED = "key_is_govt_approved"
    private const val KEY_GOVT_STATUS_TEXT = "key_govt_status_text"
    private const val KEY_GOVT_CERT_NO = "key_govt_cert_no"
    private const val KEY_GOVT_AUTHORITY = "key_govt_authority"
    private const val KEY_GOVT_APPROVAL_DATE = "key_govt_approval_date"
    private const val KEY_GOVT_REF_NO = "key_govt_ref_no"
    private const val KEY_GOVT_DEPT_INFO = "key_govt_dept_info"
    private const val KEY_GOVT_WEBSITES = "key_govt_websites"

    private const val KEY_FB_URL = "key_fb_url"
    private const val KEY_IG_URL = "key_ig_url"
    private const val KEY_YT_URL = "key_yt_url"
    private const val KEY_WA_URL = "key_wa_url"
    private const val KEY_X_URL = "key_x_url"

    private const val KEY_SUPPORT_EMAIL = "key_support_email"
    private const val KEY_SUPPORT_PHONE = "key_support_phone"
    private const val KEY_BUSINESS_ADDRESS = "key_business_address"
    private const val KEY_SUPPORT_HOURS = "key_support_hours"
    private const val KEY_OFFICIAL_WEBSITE = "key_official_website"

    private val _configState = MutableStateFlow(AppConfig())
    val configState: StateFlow<AppConfig> = _configState.asStateFlow()

    private var isInitialized = false

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun init(context: Context): AppConfig {
        val prefs = getPrefs(context)
        val default = AppConfig()

        val config = AppConfig(
            appName = prefs.getString(KEY_APP_NAME, default.appName) ?: default.appName,
            appDescription = prefs.getString(KEY_APP_DESC, default.appDescription) ?: default.appDescription,
            releaseDate = prefs.getString(KEY_RELEASE_DATE, default.releaseDate) ?: default.releaseDate,
            currentVersion = prefs.getString(KEY_CURRENT_VERSION, default.currentVersion) ?: default.currentVersion,
            lastUpdatedDate = prefs.getString(KEY_LAST_UPDATED, default.lastUpdatedDate) ?: default.lastUpdatedDate,
            copyrightText = prefs.getString(KEY_COPYRIGHT, default.copyrightText) ?: default.copyrightText,

            isGovtApproved = prefs.getBoolean(KEY_IS_GOVT_APPROVED, default.isGovtApproved),
            govtRegistrationStatusText = prefs.getString(KEY_GOVT_STATUS_TEXT, default.govtRegistrationStatusText) ?: default.govtRegistrationStatusText,
            govtCertificateNo = prefs.getString(KEY_GOVT_CERT_NO, default.govtCertificateNo) ?: default.govtCertificateNo,
            govtIssuingAuthority = prefs.getString(KEY_GOVT_AUTHORITY, default.govtIssuingAuthority) ?: default.govtIssuingAuthority,
            govtApprovalDate = prefs.getString(KEY_GOVT_APPROVAL_DATE, default.govtApprovalDate) ?: default.govtApprovalDate,
            govtReferenceNo = prefs.getString(KEY_GOVT_REF_NO, default.govtReferenceNo) ?: default.govtReferenceNo,
            govtDepartmentInfo = prefs.getString(KEY_GOVT_DEPT_INFO, default.govtDepartmentInfo) ?: default.govtDepartmentInfo,
            govtOfficialWebsites = prefs.getString(KEY_GOVT_WEBSITES, default.govtOfficialWebsites) ?: default.govtOfficialWebsites,

            facebookUrl = prefs.getString(KEY_FB_URL, default.facebookUrl) ?: default.facebookUrl,
            instagramUrl = prefs.getString(KEY_IG_URL, default.instagramUrl) ?: default.instagramUrl,
            youtubeUrl = prefs.getString(KEY_YT_URL, default.youtubeUrl) ?: default.youtubeUrl,
            whatsappUrl = prefs.getString(KEY_WA_URL, default.whatsappUrl) ?: default.whatsappUrl,
            twitterUrl = prefs.getString(KEY_X_URL, default.twitterUrl) ?: default.twitterUrl,

            supportEmail = prefs.getString(KEY_SUPPORT_EMAIL, default.supportEmail) ?: default.supportEmail,
            supportPhone = prefs.getString(KEY_SUPPORT_PHONE, default.supportPhone) ?: default.supportPhone,
            businessAddress = prefs.getString(KEY_BUSINESS_ADDRESS, default.businessAddress) ?: default.businessAddress,
            supportHours = prefs.getString(KEY_SUPPORT_HOURS, default.supportHours) ?: default.supportHours,
            officialWebsite = prefs.getString(KEY_OFFICIAL_WEBSITE, default.officialWebsite) ?: default.officialWebsite
        )

        _configState.value = config
        isInitialized = true
        return config
    }

    fun getConfig(context: Context): AppConfig {
        if (!isInitialized) {
            return init(context)
        }
        return _configState.value
    }

    fun updateConfig(context: Context, newConfig: AppConfig) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putString(KEY_APP_NAME, newConfig.appName)
            .putString(KEY_APP_DESC, newConfig.appDescription)
            .putString(KEY_RELEASE_DATE, newConfig.releaseDate)
            .putString(KEY_CURRENT_VERSION, newConfig.currentVersion)
            .putString(KEY_LAST_UPDATED, newConfig.lastUpdatedDate)
            .putString(KEY_COPYRIGHT, newConfig.copyrightText)

            .putBoolean(KEY_IS_GOVT_APPROVED, newConfig.isGovtApproved)
            .putString(KEY_GOVT_STATUS_TEXT, newConfig.govtRegistrationStatusText)
            .putString(KEY_GOVT_CERT_NO, newConfig.govtCertificateNo)
            .putString(KEY_GOVT_AUTHORITY, newConfig.govtIssuingAuthority)
            .putString(KEY_GOVT_APPROVAL_DATE, newConfig.govtApprovalDate)
            .putString(KEY_GOVT_REF_NO, newConfig.govtReferenceNo)
            .putString(KEY_GOVT_DEPT_INFO, newConfig.govtDepartmentInfo)
            .putString(KEY_GOVT_WEBSITES, newConfig.govtOfficialWebsites)

            .putString(KEY_FB_URL, newConfig.facebookUrl)
            .putString(KEY_IG_URL, newConfig.instagramUrl)
            .putString(KEY_YT_URL, newConfig.youtubeUrl)
            .putString(KEY_WA_URL, newConfig.whatsappUrl)
            .putString(KEY_X_URL, newConfig.twitterUrl)

            .putString(KEY_SUPPORT_EMAIL, newConfig.supportEmail)
            .putString(KEY_SUPPORT_PHONE, newConfig.supportPhone)
            .putString(KEY_BUSINESS_ADDRESS, newConfig.businessAddress)
            .putString(KEY_SUPPORT_HOURS, newConfig.supportHours)
            .putString(KEY_OFFICIAL_WEBSITE, newConfig.officialWebsite)
            .apply()

        _configState.value = newConfig
    }
}
