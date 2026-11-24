package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.net.URI

/**
 * Validates that a government scheme portal URL strictly uses HTTPS and has a valid host.
 */
fun isValidOfficialHttpsUrl(url: String): Boolean {
    val trimmed = url.trim()
    if (!trimmed.startsWith("https://", ignoreCase = false)) return false
    if (trimmed.any { it.isWhitespace() } || trimmed.length !in 10..500) return false
    return try {
        val uri = URI(trimmed)
        val host = uri.host?.lowercase() ?: return false
        uri.scheme == "https" && host.contains(".") && !host.startsWith(".") && !host.endsWith(".")
    } catch (_: Exception) {
        false
    }
}

/**
 * Guardrail helper indicating whether a URL belongs to an authentic official Indian government
 * or statutory agriculture board domain (.gov.in, .nic.in, .ac.in, or verified statutory orgs like sfacindia.com).
 */
fun isOfficialGovernmentPortalDomain(url: String): Boolean {
    if (!isValidOfficialHttpsUrl(url)) return false
    val host = try {
        URI(url.trim()).host?.lowercase().orEmpty()
    } catch (_: Exception) {
        return false
    }
    return host.endsWith(".gov.in") ||
        host.endsWith(".nic.in") ||
        host.endsWith(".ac.in") ||
        host == "maandhan.in" || host.endsWith(".maandhan.in") ||
        host == "jansamarth.in" || host.endsWith(".jansamarth.in") ||
        host == "sfacindia.com" || host.endsWith(".sfacindia.com")
}

object SchemeFallbackText {
    fun checkOfficialWebsite(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "অনুগ্রহ করে অফিশিয়াল ওয়েবসাইট দেখুন।"
        AppLanguage.HINDI -> "कृपया आधिकारिक वेबसाइट देखें।"
        AppLanguage.ENGLISH -> "Please check the official website."
    }

    fun disclaimer(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI ->
            "KRISHAK SATHI শুধুমাত্র সরকারি কৃষি প্রকল্পের তথ্য ও সরাসরি অফিশিয়াল পোর্টালের লিঙ্ক প্রদান করে। এটি কোনো সরকারি ওয়েবসাইট নয় এবং আবেদন অনুমোদন বা সুবিধার নিশ্চয়তা দেয় না। চূড়ান্ত যোগ্যতা, আবেদনের অবস্থা ও সুবিধা সংশ্লিষ্ট সরকারি দপ্তর দ্বারা নির্ধারিত হয়।"
        AppLanguage.HINDI ->
            "KRISHAK SATHI केवल आधिकारिक सरकारी योजना पोर्टल्स की जानकारी और सीधे लिंक प्रदान करता है। यह कोई सरकारी वेबसाइट नहीं है और न ही आवेदन स्वीकृत करती है या लाभ की गारंटी देती है। अंतिम पात्रता, आवेदन स्थिति और लाभ संबंधित सरकारी विभाग द्वारा निर्धारित किए जाते हैं।"
        AppLanguage.ENGLISH ->
            "KRISHAK SATHI provides information and direct links to official government scheme portals. It is not a government website and does not approve applications or guarantee benefits. Final eligibility, application status and benefits are determined by the respective government department."
    }
}

enum class SchemeGovLevel {
    CENTRAL,
    WEST_BENGAL,
    OTHER_STATE;

    fun getLabel(lang: AppLanguage): String = when (this) {
        CENTRAL -> when (lang) {
            AppLanguage.BENGALI -> "কেন্দ্রীয় সরকার"
            AppLanguage.HINDI -> "केंद्र सरकार"
            AppLanguage.ENGLISH -> "Central Government"
        }
        WEST_BENGAL -> when (lang) {
            AppLanguage.BENGALI -> "পশ্চিমবঙ্গ সরকার"
            AppLanguage.HINDI -> "पश्चिम बंगाल सरकार"
            AppLanguage.ENGLISH -> "West Bengal Govt"
        }
        OTHER_STATE -> when (lang) {
            AppLanguage.BENGALI -> "অন্যান্য রাজ্য সরকার"
            AppLanguage.HINDI -> "अन्य राज्य सरकार"
            AppLanguage.ENGLISH -> "Other State Govts"
        }
    }

    companion object {
        fun fromWire(value: String?): SchemeGovLevel =
            entries.firstOrNull { it.name == value } ?: CENTRAL
    }
}

/**
 * All 18 required Government Farmer Scheme categories.
 */
enum class SchemeCategory {
    CENTRAL_SCHEMES,
    WEST_BENGAL_SCHEMES,
    OTHER_STATE_SCHEMES,
    FINANCIAL_ASSISTANCE,
    CROP_INSURANCE,
    KCC_LOANS,
    IRRIGATION_WATER,
    SEEDS_SOIL,
    MACHINERY_EQUIPMENT,
    ORGANIC_NATURAL,
    HORTICULTURE_FRUITS_VEG,
    TEA_JUTE_COMMERCIAL,
    FISHERIES_DAIRY_LIVESTOCK,
    PENSION_SOCIAL_SECURITY,
    FPO_AGRIBUSINESS,
    MARKETING_STORAGE_INFRA,
    WOMEN_SMALL_FARMERS,
    ALLIED_OTHER;

    fun getLabel(lang: AppLanguage): String = when (this) {
        CENTRAL_SCHEMES -> when (lang) {
            AppLanguage.BENGALI -> "কেন্দ্রীয় সরকারের কৃষক প্রকল্প"
            AppLanguage.HINDI -> "केंद्र सरकार किसान योजनाएं"
            AppLanguage.ENGLISH -> "Central Government Farmer Schemes"
        }
        WEST_BENGAL_SCHEMES -> when (lang) {
            AppLanguage.BENGALI -> "পশ্চিমবঙ্গ সরকারের কৃষক প্রকল্প"
            AppLanguage.HINDI -> "पश्चिम बंगाल सरकार किसान योजनाएं"
            AppLanguage.ENGLISH -> "West Bengal Government Farmer Schemes"
        }
        OTHER_STATE_SCHEMES -> when (lang) {
            AppLanguage.BENGALI -> "অন্যান্য রাজ্য সরকারের কৃষক প্রকল্প"
            AppLanguage.HINDI -> "अन्य राज्य सरकार किसान योजनाएं"
            AppLanguage.ENGLISH -> "Other State Government Farmer Schemes"
        }
        FINANCIAL_ASSISTANCE -> when (lang) {
            AppLanguage.BENGALI -> "কৃষক আর্থিক সহায়তা"
            AppLanguage.HINDI -> "किसान वित्तीय सहायता"
            AppLanguage.ENGLISH -> "Farmer Financial Assistance"
        }
        CROP_INSURANCE -> when (lang) {
            AppLanguage.BENGALI -> "শস্য বিমা প্রকল্প"
            AppLanguage.HINDI -> "फसल बीमा योजनाएं"
            AppLanguage.ENGLISH -> "Crop Insurance Schemes"
        }
        KCC_LOANS -> when (lang) {
            AppLanguage.BENGALI -> "কিষাণ ক্রেডিট কার্ড ও কৃষি ঋণ"
            AppLanguage.HINDI -> "किसान क्रेडिट कार्ड और कृषि ऋण"
            AppLanguage.ENGLISH -> "Kisan Credit Card & Agricultural Loans"
        }
        IRRIGATION_WATER -> when (lang) {
            AppLanguage.BENGALI -> "সেচ ও জল ব্যবস্থাপনা ভর্তুকি"
            AppLanguage.HINDI -> "सिंचाई और जल प्रबंधन सब्सिडी"
            AppLanguage.ENGLISH -> "Irrigation & Water Management Subsidies"
        }
        SEEDS_SOIL -> when (lang) {
            AppLanguage.BENGALI -> "বীজ, সার ও মাটি স্বাস্থ্য সহায়তা"
            AppLanguage.HINDI -> "बीज, उर्वरक और मृदा स्वास्थ्य सहायता"
            AppLanguage.ENGLISH -> "Seeds, Fertilizers & Soil Health Support"
        }
        MACHINERY_EQUIPMENT -> when (lang) {
            AppLanguage.BENGALI -> "কৃষি যন্ত্রপাতি ও সরঞ্জাম ভর্তুকি"
            AppLanguage.HINDI -> "कृषि मशीनरी और उपकरण सब्सिडी"
            AppLanguage.ENGLISH -> "Farm Machinery & Equipment Subsidies"
        }
        ORGANIC_NATURAL -> when (lang) {
            AppLanguage.BENGALI -> "জৈব ও প্রাকৃতিক চাষ প্রকল্প"
            AppLanguage.HINDI -> "जैविक और प्राकृतिक खेती योजनाएं"
            AppLanguage.ENGLISH -> "Organic & Natural Farming Schemes"
        }
        HORTICULTURE_FRUITS_VEG -> when (lang) {
            AppLanguage.BENGALI -> "উদ্যানপালন, ফল ও সবজি চাষ প্রকল্প"
            AppLanguage.HINDI -> "बागवानी, फल और सब्जी खेती योजनाएं"
            AppLanguage.ENGLISH -> "Horticulture, Fruits & Vegetable Farming"
        }
        TEA_JUTE_COMMERCIAL -> when (lang) {
            AppLanguage.BENGALI -> "চা, পাট ও বাণিজ্যিক ফসল প্রকল্প"
            AppLanguage.HINDI -> "चाय, जूट और वाणिज्यिक फसल योजनाएं"
            AppLanguage.ENGLISH -> "Tea, Jute & Commercial Crop Schemes"
        }
        FISHERIES_DAIRY_LIVESTOCK -> when (lang) {
            AppLanguage.BENGALI -> "মৎস্য, দুগ্ধ, পোল্ট্রি ও পশুপালন প্রকল্প"
            AppLanguage.HINDI -> "मत्स्य, डेयरी, मुर्गीपालन व पशुपालन योजनाएं"
            AppLanguage.ENGLISH -> "Fisheries, Dairy, Poultry & Livestock"
        }
        PENSION_SOCIAL_SECURITY -> when (lang) {
            AppLanguage.BENGALI -> "কৃষক পেনশন ও সামাজিক সুরক্ষা"
            AppLanguage.HINDI -> "किसान पेंशन और सामाजिक सुरक्षा"
            AppLanguage.ENGLISH -> "Farmer Pension & Social Security"
        }
        FPO_AGRIBUSINESS -> when (lang) {
            AppLanguage.BENGALI -> "এফপিও (FPO) ও কৃষি ব্যবসা সহায়তা"
            AppLanguage.HINDI -> "एफपीओ (FPO) और कृषि व्यवसाय सहायता"
            AppLanguage.ENGLISH -> "FPO & Agricultural Business Support"
        }
        MARKETING_STORAGE_INFRA -> when (lang) {
            AppLanguage.BENGALI -> "কৃষি বিপণন, সংরক্ষণ ও পরিকাঠামো"
            AppLanguage.HINDI -> "कृषि विपणन, भंडारण और बुनियादी ढांचा"
            AppLanguage.ENGLISH -> "Agri Marketing, Storage & Infrastructure"
        }
        WOMEN_SMALL_FARMERS -> when (lang) {
            AppLanguage.BENGALI -> "মহিলা ও ক্ষুদ্র/প্রান্তিক কৃষক সহায়তা"
            AppLanguage.HINDI -> "महिला एवं लघु/सीमांत किसान सहायता"
            AppLanguage.ENGLISH -> "Women & Small/Marginal Farmer Support"
        }
        ALLIED_OTHER -> when (lang) {
            AppLanguage.BENGALI -> "অন্যান্য কৃষি ও আনুষঙ্গিক ক্ষেত্রের প্রকল্প"
            AppLanguage.HINDI -> "अन्य कृषि और संबद्ध क्षेत्र की योजनाएं"
            AppLanguage.ENGLISH -> "Other Agriculture & Allied Sector Schemes"
        }
    }

    companion object {
        fun fromWire(value: String?): SchemeCategory =
            entries.firstOrNull { it.name == value } ?: CENTRAL_SCHEMES
    }
}

enum class SchemeFarmerType {
    ALL_FARMERS,
    SMALL_MARGINAL,
    TENANT_SHARECROPPER,
    WOMEN_FARMERS,
    TEA_PLANTATION,
    FISHERMEN_DAIRY,
    FPO_SHG,
    YOUNG_AGRI_ENTREPRENEUR;

    fun getLabel(lang: AppLanguage): String = when (this) {
        ALL_FARMERS -> when (lang) {
            AppLanguage.BENGALI -> "সকল কৃষক"
            AppLanguage.HINDI -> "सभी किसान"
            AppLanguage.ENGLISH -> "All Farmers"
        }
        SMALL_MARGINAL -> when (lang) {
            AppLanguage.BENGALI -> "ক্ষুদ্র ও প্রান্তিক কৃষক"
            AppLanguage.HINDI -> "लघु और सीमांत किसान"
            AppLanguage.ENGLISH -> "Small & Marginal Farmers"
        }
        TENANT_SHARECROPPER -> when (lang) {
            AppLanguage.BENGALI -> "ভাগচাষি ও ইজারাদার"
            AppLanguage.HINDI -> "बटाईदार और पट्टेदार किसान"
            AppLanguage.ENGLISH -> "Tenant / Sharecroppers"
        }
        WOMEN_FARMERS -> when (lang) {
            AppLanguage.BENGALI -> "মহিলা কৃষক"
            AppLanguage.HINDI -> "महिला किसान"
            AppLanguage.ENGLISH -> "Women Farmers"
        }
        TEA_PLANTATION -> when (lang) {
            AppLanguage.BENGALI -> "চা চাষি ও বাগান"
            AppLanguage.HINDI -> "चाय उत्पादक व बागान"
            AppLanguage.ENGLISH -> "Tea Growers"
        }
        FISHERMEN_DAIRY -> when (lang) {
            AppLanguage.BENGALI -> "মৎস্য ও পশুপালক"
            AppLanguage.HINDI -> "मत्स्य व पशुपालक"
            AppLanguage.ENGLISH -> "Fishermen & Dairy Farmers"
        }
        FPO_SHG -> when (lang) {
            AppLanguage.BENGALI -> "FPO ও স্বনির্ভর গোষ্ঠী"
            AppLanguage.HINDI -> "FPO एवं स्वयं सहायता समूह"
            AppLanguage.ENGLISH -> "FPOs & SHGs"
        }
        YOUNG_AGRI_ENTREPRENEUR -> when (lang) {
            AppLanguage.BENGALI -> "কৃষি উদ্যোক্তা"
            AppLanguage.HINDI -> "कृषि उद्यमी"
            AppLanguage.ENGLISH -> "Agri-Entrepreneurs"
        }
    }

    companion object {
        fun fromWire(value: String?): SchemeFarmerType =
            entries.firstOrNull { it.name == value } ?: ALL_FARMERS
    }
}

enum class SchemeActivityFocus {
    ALL_ACTIVITIES,
    FIELD_CROPS,
    HORTICULTURE_VEG,
    TEA_COMMERCIAL,
    IRRIGATION_SOLAR,
    MACHINERY_INFRA,
    SOIL_ORGANIC,
    FISHERIES_LIVESTOCK,
    CREDIT_INSURANCE_PENSION,
    MARKETING_FPO;

    fun getLabel(lang: AppLanguage): String = when (this) {
        ALL_ACTIVITIES -> when (lang) {
            AppLanguage.BENGALI -> "সব ফসল ও কার্যক্রম"
            AppLanguage.HINDI -> "सभी फसलें व गतिविधियां"
            AppLanguage.ENGLISH -> "All Crops & Activities"
        }
        FIELD_CROPS -> when (lang) {
            AppLanguage.BENGALI -> "ধান, গম ও মাঠ ফসল"
            AppLanguage.HINDI -> "धान, गेहूं व खेत फसलें"
            AppLanguage.ENGLISH -> "Field Crops & Grains"
        }
        HORTICULTURE_VEG -> when (lang) {
            AppLanguage.BENGALI -> "ফল, সবজি ও উদ্যানপালন"
            AppLanguage.HINDI -> "फल, सब्जी व बागवानी"
            AppLanguage.ENGLISH -> "Fruits, Vegetables & Horticulture"
        }
        TEA_COMMERCIAL -> when (lang) {
            AppLanguage.BENGALI -> "চা, পাট ও বাণিজ্যিক ফসল"
            AppLanguage.HINDI -> "चाय, जूट व वाणिज्यिक फसलें"
            AppLanguage.ENGLISH -> "Tea, Jute & Commercial"
        }
        IRRIGATION_SOLAR -> when (lang) {
            AppLanguage.BENGALI -> "সেচ ও সোলার পাম্প"
            AppLanguage.HINDI -> "सिंचाई व सोलर पंप"
            AppLanguage.ENGLISH -> "Irrigation & Solar"
        }
        MACHINERY_INFRA -> when (lang) {
            AppLanguage.BENGALI -> "কৃষি যন্ত্রপাতি ও পরিকাঠামো"
            AppLanguage.HINDI -> "मशीनरी व बुनियादी ढांचा"
            AppLanguage.ENGLISH -> "Machinery & Infra"
        }
        SOIL_ORGANIC -> when (lang) {
            AppLanguage.BENGALI -> "মাটি স্বাস্থ্য ও জৈব চাষ"
            AppLanguage.HINDI -> "मृदा स्वास्थ्य व जैविक खेती"
            AppLanguage.ENGLISH -> "Soil Health & Organic"
        }
        FISHERIES_LIVESTOCK -> when (lang) {
            AppLanguage.BENGALI -> "মৎস্য, দুগ্ধ ও পশুপালন"
            AppLanguage.HINDI -> "मत्स्य, डेयरी व पशुपालन"
            AppLanguage.ENGLISH -> "Fisheries & Livestock"
        }
        CREDIT_INSURANCE_PENSION -> when (lang) {
            AppLanguage.BENGALI -> "ঋণ, বিমা ও পেনশন"
            AppLanguage.HINDI -> "ऋण, बीमा व पेंशन"
            AppLanguage.ENGLISH -> "Credit, Insurance & Pension"
        }
        MARKETING_FPO -> when (lang) {
            AppLanguage.BENGALI -> "বিপণন, মাণ্ডি ও FPO"
            AppLanguage.HINDI -> "विपणन, मंडी व FPO"
            AppLanguage.ENGLISH -> "Marketing, Mandi & FPO"
        }
    }

    companion object {
        fun fromWire(value: String?): SchemeActivityFocus =
            entries.firstOrNull { it.name == value } ?: FIELD_CROPS
    }
}

enum class SchemeApplicationStatus {
    OPEN,
    CLOSED,
    UPCOMING,
    INFO_ONLY,
    DISCONTINUED;

    fun getLabel(lang: AppLanguage): String = when (this) {
        OPEN -> when (lang) {
            AppLanguage.BENGALI -> "আবেদন চলছে"
            AppLanguage.HINDI -> "आवेदन खुला है"
            AppLanguage.ENGLISH -> "Open / Active"
        }
        CLOSED -> when (lang) {
            AppLanguage.BENGALI -> "বর্তমানে বন্ধ"
            AppLanguage.HINDI -> "वर्तमान में बंद"
            AppLanguage.ENGLISH -> "Closed"
        }
        UPCOMING -> when (lang) {
            AppLanguage.BENGALI -> "আসন্ন পর্যায়"
            AppLanguage.HINDI -> "आगामी चरण"
            AppLanguage.ENGLISH -> "Upcoming"
        }
        INFO_ONLY -> when (lang) {
            AppLanguage.BENGALI -> "অফিশিয়াল পোর্টাল তথ্য"
            AppLanguage.HINDI -> "आधिकारिक पोर्टल जानकारी"
            AppLanguage.ENGLISH -> "Official Info Portal"
        }
        DISCONTINUED -> when (lang) {
            AppLanguage.BENGALI -> "স্থগিত / বন্ধ প্রকল্প"
            AppLanguage.HINDI -> "समाप्त / बंद योजना"
            AppLanguage.ENGLISH -> "Discontinued"
        }
    }

    companion object {
        fun fromWire(value: String?): SchemeApplicationStatus =
            entries.firstOrNull { it.name == value } ?: INFO_ONLY
    }
}

@Keep
@IgnoreExtraProperties
data class GovernmentScheme(
    @DocumentId
    val id: String = "",
    val govLevel: String = SchemeGovLevel.CENTRAL.name,
    val category: String = SchemeCategory.CENTRAL_SCHEMES.name,
    val extraCategories: List<String> = emptyList(),
    val farmerTypes: List<String> = listOf(SchemeFarmerType.ALL_FARMERS.name),
    val activityFocus: String = SchemeActivityFocus.FIELD_CROPS.name,
    val northBengalPriority: Int = 50,
    val discontinued: Boolean = false,
    val applicationStatus: String = SchemeApplicationStatus.INFO_ONLY.name,
    val officialUrl: String = "",
    val officialSourceUrl: String = "",
    val lastVerifiedDate: String = "2026-11-17",
    val nameEn: String = "",
    val nameBn: String = "",
    val nameHi: String = "",
    val departmentEn: String = "",
    val departmentBn: String = "",
    val departmentHi: String = "",
    val shortDescEn: String = "",
    val shortDescBn: String = "",
    val shortDescHi: String = "",
    val objectivesEn: String = "",
    val objectivesBn: String = "",
    val objectivesHi: String = "",
    val whoCanApplyEn: String = "",
    val whoCanApplyBn: String = "",
    val whoCanApplyHi: String = "",
    val eligibilityEn: String = "",
    val eligibilityBn: String = "",
    val eligibilityHi: String = "",
    val documentsEn: String = "",
    val documentsBn: String = "",
    val documentsHi: String = "",
    val benefitsEn: String = "",
    val benefitsBn: String = "",
    val benefitsHi: String = "",
    val procedureEn: String = "",
    val procedureBn: String = "",
    val procedureHi: String = "",
    val statusNoteEn: String = "",
    val statusNoteBn: String = "",
    val statusNoteHi: String = "",
    val cropTagsEn: String = "",
    val cropTagsBn: String = "",
    val cropTagsHi: String = "",
    val updatedByAdmin: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    fun resolveGovLevel(): SchemeGovLevel = SchemeGovLevel.fromWire(govLevel)
    fun resolveCategory(): SchemeCategory = SchemeCategory.fromWire(category)
    fun resolveAllCategories(): Set<SchemeCategory> = buildSet {
        add(resolveCategory())
        extraCategories.forEach { wire ->
            SchemeCategory.entries.firstOrNull { it.name == wire }?.let { add(it) }
        }
    }

    fun resolveFarmerTypes(): Set<SchemeFarmerType> =
        farmerTypes.mapNotNull { wire ->
            SchemeFarmerType.entries.firstOrNull { it.name == wire }
        }.ifEmpty { listOf(SchemeFarmerType.ALL_FARMERS) }.toSet()

    fun resolveActivityFocus(): SchemeActivityFocus = SchemeActivityFocus.fromWire(activityFocus)

    fun resolveApplicationStatus(): SchemeApplicationStatus =
        if (discontinued) SchemeApplicationStatus.DISCONTINUED
        else SchemeApplicationStatus.fromWire(applicationStatus)

    private fun pickLocalized(bn: String, hi: String, en: String, lang: AppLanguage, fallbackToWebsite: Boolean): String {
        val chosen = when (lang) {
            AppLanguage.BENGALI -> bn.ifBlank { en }
            AppLanguage.HINDI -> hi.ifBlank { en }
            AppLanguage.ENGLISH -> en.ifBlank { bn }
        }.trim()
        return if (chosen.isNotBlank()) chosen
        else if (fallbackToWebsite) SchemeFallbackText.checkOfficialWebsite(lang)
        else ""
    }

    fun getName(lang: AppLanguage): String = pickLocalized(nameBn, nameHi, nameEn, lang, fallbackToWebsite = false)
    fun getDepartment(lang: AppLanguage): String = pickLocalized(departmentBn, departmentHi, departmentEn, lang, fallbackToWebsite = true)
    fun getShortDesc(lang: AppLanguage): String = pickLocalized(shortDescBn, shortDescHi, shortDescEn, lang, fallbackToWebsite = true)
    fun getObjectives(lang: AppLanguage): String = pickLocalized(objectivesBn, objectivesHi, objectivesEn, lang, fallbackToWebsite = true)
    fun getWhoCanApply(lang: AppLanguage): String = pickLocalized(whoCanApplyBn, whoCanApplyHi, whoCanApplyEn, lang, fallbackToWebsite = true)
    fun getEligibility(lang: AppLanguage): String = pickLocalized(eligibilityBn, eligibilityHi, eligibilityEn, lang, fallbackToWebsite = true)
    fun getDocuments(lang: AppLanguage): String = pickLocalized(documentsBn, documentsHi, documentsEn, lang, fallbackToWebsite = true)
    fun getBenefits(lang: AppLanguage): String = pickLocalized(benefitsBn, benefitsHi, benefitsEn, lang, fallbackToWebsite = true)
    fun getProcedure(lang: AppLanguage): String = pickLocalized(procedureBn, procedureHi, procedureEn, lang, fallbackToWebsite = true)
    fun getStatusNote(lang: AppLanguage): String = pickLocalized(statusNoteBn, statusNoteHi, statusNoteEn, lang, fallbackToWebsite = true)
    fun getCropTags(lang: AppLanguage): String = pickLocalized(cropTagsBn, cropTagsHi, cropTagsEn, lang, fallbackToWebsite = false)

    fun matchesSearch(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return listOf(
            nameEn, nameBn, nameHi,
            departmentEn, departmentBn, departmentHi,
            shortDescEn, shortDescBn, shortDescHi,
            cropTagsEn, cropTagsBn, cropTagsHi,
            benefitsEn, benefitsBn, benefitsHi,
            resolveCategory().getLabel(AppLanguage.ENGLISH),
            resolveCategory().getLabel(AppLanguage.BENGALI),
            resolveCategory().getLabel(AppLanguage.HINDI)
        ).any { it.lowercase().contains(q) }
    }

    fun toFirestoreWriteMap(isNew: Boolean, adminUid: String): Map<String, Any> {
        val map = hashMapOf<String, Any>(
            "govLevel" to resolveGovLevel().name,
            "category" to resolveCategory().name,
            "extraCategories" to extraCategories.distinct().take(10),
            "farmerTypes" to farmerTypes.distinct().ifEmpty { listOf(SchemeFarmerType.ALL_FARMERS.name) }.take(10),
            "activityFocus" to resolveActivityFocus().name,
            "northBengalPriority" to northBengalPriority.coerceIn(0, 100),
            "discontinued" to discontinued,
            "applicationStatus" to (if (discontinued) SchemeApplicationStatus.DISCONTINUED.name else resolveApplicationStatus().name),
            "officialUrl" to officialUrl.trim(),
            "officialSourceUrl" to officialSourceUrl.trim().ifBlank { officialUrl.trim() },
            "lastVerifiedDate" to lastVerifiedDate.trim().ifBlank { "2026-11-17" },
            "nameEn" to nameEn.trim().take(200),
            "nameBn" to nameBn.trim().ifBlank { nameEn.trim() }.take(200),
            "nameHi" to nameHi.trim().ifBlank { nameEn.trim() }.take(200),
            "departmentEn" to departmentEn.trim().take(200),
            "departmentBn" to departmentBn.trim().ifBlank { departmentEn.trim() }.take(200),
            "departmentHi" to departmentHi.trim().ifBlank { departmentEn.trim() }.take(200),
            "shortDescEn" to shortDescEn.trim().take(600),
            "shortDescBn" to shortDescBn.trim().take(600),
            "shortDescHi" to shortDescHi.trim().take(600),
            "objectivesEn" to objectivesEn.trim().take(1000),
            "objectivesBn" to objectivesBn.trim().take(1000),
            "objectivesHi" to objectivesHi.trim().take(1000),
            "whoCanApplyEn" to whoCanApplyEn.trim().take(800),
            "whoCanApplyBn" to whoCanApplyBn.trim().take(800),
            "whoCanApplyHi" to whoCanApplyHi.trim().take(800),
            "eligibilityEn" to eligibilityEn.trim().take(1200),
            "eligibilityBn" to eligibilityBn.trim().take(1200),
            "eligibilityHi" to eligibilityHi.trim().take(1200),
            "documentsEn" to documentsEn.trim().take(1000),
            "documentsBn" to documentsBn.trim().take(1000),
            "documentsHi" to documentsHi.trim().take(1000),
            "benefitsEn" to benefitsEn.trim().take(1200),
            "benefitsBn" to benefitsBn.trim().take(1200),
            "benefitsHi" to benefitsHi.trim().take(1200),
            "procedureEn" to procedureEn.trim().take(1200),
            "procedureBn" to procedureBn.trim().take(1200),
            "procedureHi" to procedureHi.trim().take(1200),
            "statusNoteEn" to statusNoteEn.trim().take(500),
            "statusNoteBn" to statusNoteBn.trim().take(500),
            "statusNoteHi" to statusNoteHi.trim().take(500),
            "cropTagsEn" to cropTagsEn.trim().take(300),
            "cropTagsBn" to cropTagsBn.trim().take(300),
            "cropTagsHi" to cropTagsHi.trim().take(300),
            "updatedByAdmin" to adminUid.take(128),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isNew) {
            map["createdAt"] = FieldValue.serverTimestamp()
        }
        return map
    }
}
