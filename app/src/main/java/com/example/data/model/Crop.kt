package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class CropMasterGroup(
    val codeLetter: String,
    val iconEmoji: String,
    val bnName: String,
    val hiName: String,
    val enName: String
) {
    LEAFY_VEGETABLES("A", "🥬", "A. শাক (Leafy Veg)", "A. साग व पत्तेदार (Leafy)", "A. Leafy Vegetables"),
    VEGETABLES("B", "🥕", "B. সবজি (Vegetables)", "B. सब्जियां (Vegetables)", "B. Vegetables"),
    FRUITS("C", "🥭", "C. ফল (Fruits)", "C. फल (Fruits)", "C. Fruits"),
    FLOWERS("D", "🌸", "D. ফুল (Flowers)", "D. फूल (Flowers)", "D. Flowers"),
    CEREALS_GRAINS("E", "🌾", "E. শস্য ও দানা (Cereals)", "E. अनाज व मिलेट (Cereals)", "E. Cereals & Grains"),
    PULSES_LEGUMES("F", "🫘", "F. ডাল (Pulses)", "F. दालें व फलियां (Pulses)", "F. Pulses & Legumes"),
    OILSEEDS("G", "🌻", "G. তেলবীজ (Oilseeds)", "G. तिलहन (Oilseeds)", "G. Oilseed Crops"),
    SPICES_CONDIMENTS("H", "🌶️", "H. মশলা (Spices)", "H. मसाले (Spices)", "H. Spices & Condiments"),
    PLANTATION_COMMERCIAL("I", "🍵", "I. বাগান ও বাণিজ্যিক (Plantation)", "I. बागान व नकदी (Plantation)", "I. Plantation & Commercial"),
    MEDICINAL_AROMATIC("J", "🌿", "J. ঔষধি ও সুগন্ধি (Medicinal)", "J. औषधीय व सुगंधित (Medicinal)", "J. Medicinal & Aromatic"),
    NURSERY_INDOOR_ORNAMENTAL("K", "🪴", "K. নার্সারি ও ইনডোর (Nursery)", "K. नर्सरी व इंडोर (Nursery)", "K. Nursery, Indoor & Ornamental"),
    OTHER_AGROFORESTRY("L", "🌳", "L. পশুখাদ্য, সবুজ সার ও বৃক্ষ", "L. चारा, हरी खाद व वानिकी", "L. Fodder, Green Manure & Trees");

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }
}

enum class CropSeason(val code: String, val bnName: String, val hiName: String, val enName: String) {
    KHARIF("kharif", "খরিফ / বর্ষা", "खरीफ / वर्षा", "Kharif (Monsoon)"),
    RABI("rabi", "রবি / শীত", "रबी / शीत", "Rabi (Winter)"),
    ZAID("zaid", "গ্রীষ্ম / জায়েদ", "जायद / ग्रीष्म", "Zaid (Summer)"),
    YEAR_ROUND("year_round", "সারা বছর", "साल भर", "Year-Round"),
    PERENNIAL("perennial", "বহুবর্ষজীবী", "बहुवर्षीय", "Perennial");

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }
}

enum class CropRegion(val code: String, val bnName: String, val hiName: String, val enName: String) {
    SILIGURI_NORTH_BENGAL(
        "siliguri_nb",
        "শিলিগুড়ি ও উত্তরবঙ্গ",
        "सिलीगुड़ी व उत्तर बंगाल",
        "Siliguri & North Bengal"
    ),
    WEST_BENGAL(
        "west_bengal",
        "সমগ্র পশ্চিমবঙ্গ",
        "संपूर्ण पश्चिम बंगाल",
        "West Bengal"
    ),
    OTHER_STATES_INDIA(
        "other_states",
        "ভারতের অন্যান্য রাজ্য",
        "भारत के अन्य राज्य",
        "Other Indian States"
    ),
    PAN_INDIA(
        "pan_india",
        "সর্বভারতীয়",
        "अखिल भारतीय",
        "National / Pan-India"
    );

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }
}

data class CropFaq(
    val questionBn: String,
    val questionHi: String,
    val questionEn: String,
    val answerBn: String,
    val answerHi: String,
    val answerEn: String
) {
    fun getQuestion(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> questionBn
        AppLanguage.HINDI -> questionHi.ifBlank { questionEn }
        AppLanguage.ENGLISH -> questionEn
    }

    fun getAnswer(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> answerBn
        AppLanguage.HINDI -> answerHi.ifBlank { answerEn }
        AppLanguage.ENGLISH -> answerEn
    }
}

enum class CropCategory(val code: String, val bnName: String, val hiName: String, val enName: String) {
    TEA("tea", "চা বাগান (Tea)", "चाय बागान (Tea)", "Tea Garden"),
    LEAFY_VEG("leafy_veg", "শাক (Leafy Veg)", "साग (Leafy Veg)", "Leafy Vegetables"),
    ROOT_VEG("root_veg", "মূল ও কন্দ (Root Veg)", "कंद व मूल (Root)", "Root & Tuber"),
    COLE_CROPS("cole_crops", "কপি জাতীয় (Cole Crops)", "गोभी वर्गीय (Cole)", "Cole Crops"),
    FRUIT_VEG("fruit_veg", "ফল সবজি (Fruit Veg)", "फलदार सब्जियां (Fruit)", "Fruit Vegetables"),
    GOURD("gourd", "লাউ ও কুমড়ো (Gourds)", "लता व कद्दू वर्गीय (Gourds)", "Cucurbits & Gourds"),
    BEANS_LEGUMES("beans_legumes", "শিম ও ডাল (Beans & Legumes)", "फलियां (Beans)", "Beans & Legumes"),
    BULB("bulb", "পেঁয়াজ ও রসুন (Bulb)", "कंद फसलें (Bulb)", "Bulb Crops"),
    SPICES("spices", "মশলা জাতীয় (Spices)", "मसाले (Spices)", "Spices & Condiments"),
    VEGETABLES("vegetables", "অন্যান্য সবজি (Vegetables)", "सब्जियां (Vegetables)", "Vegetables"),
    FRUITS("fruits", "ফল চাষ (Fruits)", "फल (Fruits)", "Fruits"),
    FLOWERS("flowers", "ফুল চাষ (Flowers)", "फूलों की खेती (Flowers)", "Flowers & Floriculture"),
    PLANTATION("plantation", "বাগান ফসল (Plantation)", "बागवानी (Plantation)", "Plantation Crops"),
    CEREALS("cereals", "দানাশস্য (Cereals)", "अनाज (Cereals)", "Cereals"),
    PULSES("pulses", "ডালশস্য (Pulses)", "दालें (Pulses)", "Pulses"),
    GRAINS_FIELD_CROPS("grains_field", "🌾 দানা শস্য ও মাঠ ফসল (Grains & Field)", "अनाज व खेत फसलें (Grains & Field)", "Grains & Field Crops"),
    MILLETS("millets", "🌱 মিলেট / বাজরা (Millets)", "कदन्न (Millets)", "Millets"),
    OILSEEDS("oilseeds", "🌻 তৈলবীজ ফসল (Oilseeds)", "तिलहन (Oilseeds)", "Oilseeds"),
    COMMERCIAL("commercial", "🌿 বাণিজ্যিক মাঠ ফসল (Commercial)", "व्यावसायिक फसलें (Commercial)", "Commercial Field Crops"),
    MEDICINAL_AROMATIC("medicinal_aromatic", "🌿 ঔষধি ও সুগন্ধি (Medicinal)", "औषधीय व सुगंधित (Medicinal)", "Medicinal & Aromatic Plants"),
    NURSERY_ORNAMENTAL("nursery_ornamental", "🪴 নার্সারি, ইনডোর ও শোভাবর্ধক", "नर्सरी व इंडोर पौधे", "Nursery, Indoor & Ornamental"),
    FODDER("fodder", "🐄 পশুখাদ্য ফসল (Fodder)", "चारा फसलें (Fodder)", "Fodder Crops"),
    AGROFORESTRY_OTHER("agroforestry_other", "🌳 সবুজ সার, আচ্ছাদন ও বনজ", "हरी खाद व कृषि वानिकी", "Green Manure, Cover & Trees");

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }

    fun toMasterGroup(): CropMasterGroup = when (this) {
        LEAFY_VEG -> CropMasterGroup.LEAFY_VEGETABLES
        ROOT_VEG, COLE_CROPS, FRUIT_VEG, GOURD, BEANS_LEGUMES, BULB, VEGETABLES -> CropMasterGroup.VEGETABLES
        FRUITS -> CropMasterGroup.FRUITS
        FLOWERS -> CropMasterGroup.FLOWERS
        CEREALS, GRAINS_FIELD_CROPS, MILLETS -> CropMasterGroup.CEREALS_GRAINS
        PULSES -> CropMasterGroup.PULSES_LEGUMES
        OILSEEDS -> CropMasterGroup.OILSEEDS
        SPICES -> CropMasterGroup.SPICES_CONDIMENTS
        TEA, PLANTATION, COMMERCIAL -> CropMasterGroup.PLANTATION_COMMERCIAL
        MEDICINAL_AROMATIC -> CropMasterGroup.MEDICINAL_AROMATIC
        NURSERY_ORNAMENTAL -> CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL
        FODDER, AGROFORESTRY_OTHER -> CropMasterGroup.OTHER_AGROFORESTRY
    }
}

data class CropGuideStep(
    val dayNumber: Int,
    val titleBn: String,
    val titleHi: String,
    val titleEn: String,
    val descriptionBn: String,
    val descriptionHi: String,
    val descriptionEn: String,
    val actionCategory: String // "land_prep", "irrigation", "fertilizer", "weeding", "monitoring", "harvest"
) {
    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> titleBn
        AppLanguage.HINDI -> titleHi.ifBlank { titleEn }
        AppLanguage.ENGLISH -> titleEn
    }

    fun getDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> descriptionBn
        AppLanguage.HINDI -> descriptionHi.ifBlank { descriptionEn }
        AppLanguage.ENGLISH -> descriptionEn
    }
}

data class MasterCrop(
    val cropId: String,
    val nameBn: String,
    val nameHi: String,
    val nameEn: String,
    val scientificName: String,
    val category: CropCategory,
    val bestSeasonBn: String,
    val soilBn: String,
    val seedRatePerBighaBn: String,
    val spacingBn: String,
    val plantDensityPerAcre: Int? = null,
    val harvestDays: Int,
    val expectedYieldBn: String,
    val summaryBn: String,
    val summaryHi: String,
    val summaryEn: String,
    val steps: List<CropGuideStep> = emptyList(),
    val commonPests: List<String> = emptyList(),
    val commonDiseases: List<String> = emptyList(),
    val iconEmoji: String = "🌱",
    val imageResId: Int? = null,
    // Expanded fields for complete crop guidance (Section 169)
    val suitableClimateBn: String = "উষ্ণ ও নাতিশীতোষ্ণ আর্দ্র আবহাওয়া (২০°-৩২° সেলসিয়াস)",
    val recommendedPhBn: String = "৬.০ - ৬.৮ (উর্বর দোআঁশ)",
    val landPrepBn: String = "জমি ৩-৪ বার গভীর চাষ দিয়ে মই দিয়ে ঝুরঝুরে ও আগাছামুক্ত করুন। বিঘায় ১-১.৫ টন পচা গোবর সার বা কম্পোস্ট মেশান।",
    val sowingMethodBn: String = "বীজ শোধন (ট্রাইকোডার্মা দিয়ে) করে নির্দিষ্ট দূরত্বে লাইন দিয়ে বপন করুন।",
    val nurseryMethodBn: String = "উঁচু বীজতলায় পলিটানেল বা শেডনেটের নিচে চারা তৈরি করুন। ২৫-৩০ দিন বয়সে চারা রোপণ উপযোগী হয়।",
    val transplantingBn: String = "পড়ন্ত বিকেলে জমিতে পর্যাপ্ত রস থাকা অবস্থায় চারা রোপণ করুন। রোপণের পরপরই হালকা সেচ দিন।",
    val irrigationScheduleBn: String = "মাটিতে সর্বদা আর্দ্রতা রাখুন। তবে জল যেন গোড়ায় না জমে। ফুল ও ফল আসার সময় নিয়মিত সেচ দিন।",
    val organicManureBn: String = "গোবর সার বিঘায় ৫০০-১০০০ কেজি, ট্রাইকোডার্মা ও নিম খোল ৫০ কেজি জমি তৈরির শেষ চাষে মেশান।",
    val fertilizerScheduleBn: String = "মূল সার: SSP বা DAP এবং MOP। বৃদ্ধির সময় ইউরিয়া ও NPK ১৯:১৯:১৯। ফুল আসার আগে বোরণ ও পটাশ।",
    val floweringGuidanceBn: String = "ফুল আসার সময় অতিরিক্ত নাইট্রোজেন সার দেবেন না। বোরণ (Boron 20%) ২ গ্রাম প্রতি লিটারে স্প্রে করলে ফুল ঝরা বন্ধ হয়।",
    val fruitingGuidanceBn: String = "ফল বৃদ্ধির সময় পটাশিয়াম (0:0:50) এবং ক্যালসিয়াম নাইট্রেট প্রয়োগ করুন। ফলের আকার ও উজ্জ্বলতা বাড়ে।",
    val storageBn: String = "ঠান্ডা, ছায়াযুক্ত ও বাতাস চলাচলকারী শুকনো জায়গায় সংরক্ষণ করুন।",
    val marketInfoBn: String = "স্থানীয় হাট ও পাইকারি বাজারে সকালের দিকে তাজা গ্রেডিং করা ফসল ভালো দামে বিক্রি হয়।",
    val commonDeficiencies: List<String> = listOf("নাইট্রোজেন (পাতা হলুদ)", "পটাশিয়াম (পাতা পোড়া)", "বোরণ (ফুল ঝরা / ফল বিকৃতি)", "ক্যালসিয়াম (ফল পচা)"),
    val physiologicalProblems: List<String> = listOf("ফুল ঝরে যাওয়া", "ফল ফেটে যাওয়া (Cracking)", "রোদে পোড়া দাগ (Sunscald)"),
    val aliases: List<String> = emptyList(),
    val subCategory: String = "",
    val weedManagementBn: String = "বপনের ২০-২৫ দিন পর প্রথম নিড়ানি দিয়ে আগাছা পরিষ্কার করুন। লাইন চাষে প্যাডি উইডার বা কোদাল ব্যবহার করুন।",
    val seedTreatmentBn: String = "বীজ বোনার পূর্বে ট্রাইকোডার্মা ভিরিডি বা কার্বেন্ডাজিম ২ গ্রাম/কেজি হারে মিশিয়ে শোধন করুন।",
    // Master Library v2 backward-compatible extensions
    val seasons: List<CropSeason> = emptyList(),
    val regions: List<CropRegion> = emptyList(),
    val varietiesBn: String = "",
    val varietiesHi: String = "",
    val varietiesEn: String = "",
    val estimatedCostBn: String = "",
    val estimatedCostHi: String = "",
    val estimatedCostEn: String = "",
    val harvestMethodBn: String = "",
    val harvestMethodHi: String = "",
    val harvestMethodEn: String = "",
    val growthStagesBn: String = "",
    val growthStagesHi: String = "",
    val growthStagesEn: String = "",
    val expertTipsBn: String = "",
    val expertTipsHi: String = "",
    val expertTipsEn: String = "",
    val customImageUri: String? = null,
    val galleryImageResIds: List<Int> = emptyList(),
    val faqs: List<CropFaq> = emptyList(),
    val isCustomAdded: Boolean = false
) {
    fun getName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> nameBn
        AppLanguage.HINDI -> nameHi.ifBlank { nameEn }
        AppLanguage.ENGLISH -> nameEn
    }

    fun getSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> summaryBn
        AppLanguage.HINDI -> summaryHi.ifBlank { summaryEn }
        AppLanguage.ENGLISH -> summaryEn
    }

    fun getBestSeason(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getBestSeason(this, lang)
    fun getSoil(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getSoil(this, lang)
    fun getSeedRate(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getSeedRate(this, lang)
    fun getSpacing(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getSpacing(this, lang)
    fun getExpectedYield(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getExpectedYield(this, lang)
    fun getCommonPestsList(lang: AppLanguage): List<String> = com.example.data.datasource.CropContentTranslator.getCommonPests(this, lang)
    fun getCommonDiseasesList(lang: AppLanguage): List<String> = com.example.data.datasource.CropContentTranslator.getCommonDiseases(this, lang)
    fun getSuitableClimate(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getSuitableClimate(this, lang)
    fun getRecommendedPh(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getRecommendedPh(this, lang)
    fun getLandPrep(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getLandPrep(this, lang)
    fun getSowingMethod(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getSowingMethod(this, lang)
    fun getNurseryMethod(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getNurseryMethod(this, lang)
    fun getTransplanting(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getTransplanting(this, lang)
    fun getWeedManagement(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getWeedManagement(this, lang)
    fun getSeedTreatment(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getSeedTreatment(this, lang)
    fun getIrrigationSchedule(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getIrrigationSchedule(this, lang)
    fun getOrganicManure(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getOrganicManure(this, lang)
    fun getFertilizerSchedule(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getFertilizerSchedule(this, lang)
    fun getFloweringGuidance(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getFloweringGuidance(this, lang)
    fun getFruitingGuidance(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getFruitingGuidance(this, lang)
    fun getStorage(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getStorage(this, lang)
    fun getMarketInfo(lang: AppLanguage): String = com.example.data.datasource.CropContentTranslator.getMarketInfo(this, lang)
    fun getCommonDeficienciesList(lang: AppLanguage): List<String> = com.example.data.datasource.CropContentTranslator.getCommonDeficiencies(this, lang)
    fun getPhysiologicalProblemsList(lang: AppLanguage): List<String> = com.example.data.datasource.CropContentTranslator.getPhysiologicalProblems(this, lang)

    fun getMasterGroup(): CropMasterGroup = category.toMasterGroup()
    fun getResolvedImageResId(): Int? = imageResId ?: com.example.data.datasource.CropPhotoRegistry.getPhotoResId(cropId)
    fun getResolvedGalleryResIds(): List<Int> = com.example.data.datasource.CropPhotoRegistry.getGalleryResIds(this)
    fun hasDedicatedPhoto(): Boolean = !customImageUri.isNullOrBlank() || getResolvedImageResId() != null
    fun getResolvedSeasons(): List<CropSeason> = com.example.data.datasource.CropDomainAdvisor.resolveSeasons(this)
    fun getResolvedRegions(): List<CropRegion> = com.example.data.datasource.CropDomainAdvisor.resolveRegions(this)
    fun getVarieties(lang: AppLanguage): String = com.example.data.datasource.CropDomainAdvisor.getVarieties(this, lang)
    fun getRegionalSuitability(lang: AppLanguage): String = com.example.data.datasource.CropDomainAdvisor.getRegionalSuitability(this, lang)
    fun getGrowthStagesSummary(lang: AppLanguage): String = com.example.data.datasource.CropDomainAdvisor.getGrowthStagesSummary(this, lang)
    fun getHarvestMethod(lang: AppLanguage): String = com.example.data.datasource.CropDomainAdvisor.getHarvestMethod(this, lang)
    fun getCultivationCostInfo(lang: AppLanguage): String = com.example.data.datasource.CropDomainAdvisor.getCultivationCostInfo(this, lang)
    fun getExpertTips(lang: AppLanguage): String = com.example.data.datasource.CropDomainAdvisor.getExpertTips(this, lang)
    fun getFaqsList(lang: AppLanguage): List<CropFaq> = com.example.data.datasource.CropDomainAdvisor.getFaqs(this)
}

@IgnoreExtraProperties
data class FarmerCrop(
    @DocumentId val id: String = "",
    val farmerId: String = "",
    val cropName: String = "",
    val cropCategory: String = "",
    val plantingDate: String = "",
    val landArea: Double = 0.0,
    val landUnit: String = "bigha",
    val variety: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
