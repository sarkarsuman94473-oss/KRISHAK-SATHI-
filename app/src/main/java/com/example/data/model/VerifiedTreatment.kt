package com.example.data.model

import com.example.data.datasource.TreatmentTranslations

data class MedicineSellerInfo(
    val shopName: String,
    val address: String,
    val phone: String,
    val packSize: String,
    val priceRs: Double,
    val inStock: Boolean,
    val isVerifiedShop: Boolean = true
) {
    fun getShopName(lang: AppLanguage): String = TreatmentTranslations.getSellerShopName(this, lang)
    fun getAddress(lang: AppLanguage): String = TreatmentTranslations.getSellerAddress(this, lang)
}

data class VerifiedTreatment(
    val problemId: String,
    val cropName: String,
    val problemNameBn: String,
    val problemNameHi: String,
    val problemNameEn: String,
    val symptomsBn: String,
    val symptomsHi: String,
    val symptomsEn: String,
    val possibleCauseBn: String,
    val firstActionBn: String,      // Mechanical/field hygiene
    val biologicalOptionBn: String, // Bio-agents, Neem oil, Trichoderma
    val activeIngredient: String?,  // null if no verified registered chemical
    val brandExamples: String = "",
    val dosePerHectareMlOrGm: Double?,
    val doseUnit: String = "ml",
    val waterPerHectareLiters: Double = 500.0,
    val preHarvestIntervalDays: Int = 7,
    val reEntryIntervalHours: Int = 24,
    val safetyInstructionsBn: String,
    val resistanceGroup: String = "Group 3",
    val officialSource: String = "CIBRC / State Agri Directorate",

    // Upgraded Fields for Sections 464-504
    val medicineId: String = problemId,
    val productName: String = brandExamples,
    val formulation: String = "SC / WG",
    val manufacturer: String = "Verified Agri Manufacturer",
    val productType: String = "Insecticide", // Insecticide, Fungicide, Bio-Pesticide, Bio-Fungicide, Acaricide, PGR
    val growthStageBn: String = "আক্রান্ত প্রাথমিক পর্যায় থেকে পূর্ণ পর্যায়",
    val dosePerLitreBn: String = "০.৫ - ১.০ মিলি/গ্রাম প্রতি লিটার জলে",
    val applicationMethodBn: String = "পরিষ্কার জলে ভালোভাবে গুলে স্প্রেয়ার দিয়ে গাছের সমস্ত পাতা ভিজিয়ে স্প্রে করুন",
    val maxApplications: Int = 2,
    val registrationStatus: String = "ACTIVE", // ACTIVE, RESTRICTED, BANNED, UNDER_REVIEW
    val frontImageResId: Int? = null,
    val productPhotoUrl: String = "",
    val sourceDate: String = "2024-2026",
    val lastReviewed: String = "অক্টোবর ২০২৬",
    val biologicalAlternatives: List<String> = emptyList(),
    val culturalPreventiveActions: List<String> = emptyList(),
    val nearbySellers: List<MedicineSellerInfo> = listOf(
        MedicineSellerInfo(
            shopName = "উত্তরবঙ্গ কিষাণ সেবা কেন্দ্র",
            address = "হিলকার্ট রোড, বিধান মার্কেট, শিলিগুড়ি",
            phone = "9832045610",
            packSize = "100ml / 250g",
            priceRs = 280.0,
            inStock = true,
            isVerifiedShop = true
        ),
        MedicineSellerInfo(
            shopName = "জলপাইগুড়ি এগ্রো ইনপুট সেন্টার",
            address = "স্টেশন রোড, জলপাইগুড়ি",
            phone = "9434012845",
            packSize = "250ml / 500g",
            priceRs = 540.0,
            inStock = true,
            isVerifiedShop = true
        )
    )
) {
    // 1 Hectare = 7.47 Bengal Bighas = 2.471 Acres = 149.4 Kathas = 247.1 Decimals
    fun calculateRequirementForHectare(ha: Double): Pair<Double?, Double> {
        val dose = dosePerHectareMlOrGm?.let { it * ha }
        val water = waterPerHectareLiters * ha
        return Pair(dose, water)
    }

    fun calculateRequirementForBigha(bigha: Double): Pair<Double?, Double> {
        val ha = bigha / 7.47
        return calculateRequirementForHectare(ha)
    }

    fun calculateRequirementForAcre(acre: Double): Pair<Double?, Double> {
        val ha = acre / 2.471
        return calculateRequirementForHectare(ha)
    }

    fun calculateRequirementForKatha(katha: Double): Pair<Double?, Double> {
        val ha = katha / 149.4
        return calculateRequirementForHectare(ha)
    }

    fun calculateRequirementForDecimal(decimal: Double): Pair<Double?, Double> {
        val ha = decimal / 247.1
        return calculateRequirementForHectare(ha)
    }

    fun getProblemName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> problemNameBn
        AppLanguage.HINDI -> problemNameHi.ifBlank { problemNameEn }
        AppLanguage.ENGLISH -> problemNameEn
    }

    fun getSymptoms(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> symptomsBn
        AppLanguage.HINDI -> symptomsHi.ifBlank { symptomsEn }
        AppLanguage.ENGLISH -> symptomsEn
    }

    fun getCropDisplayName(lang: AppLanguage): String = TreatmentTranslations.getCropName(this, lang)
    fun getPossibleCause(lang: AppLanguage): String = TreatmentTranslations.getPossibleCause(this, lang)
    fun getFirstAction(lang: AppLanguage): String = TreatmentTranslations.getFirstAction(this, lang)
    fun getBiologicalOption(lang: AppLanguage): String = TreatmentTranslations.getBiologicalOption(this, lang)
    fun getGrowthStage(lang: AppLanguage): String = TreatmentTranslations.getGrowthStage(this, lang)
    fun getDosePerLitre(lang: AppLanguage): String = TreatmentTranslations.getDosePerLitre(this, lang)
    fun getSafetyInstructions(lang: AppLanguage): String = TreatmentTranslations.getSafetyInstructions(this, lang)
    fun getApplicationMethod(lang: AppLanguage): String = TreatmentTranslations.getApplicationMethod(this, lang)
    fun getProductTypeLabel(lang: AppLanguage): String = TreatmentTranslations.getProductType(this, lang)
    fun getLastReviewedLabel(lang: AppLanguage): String = TreatmentTranslations.getLastReviewed(this, lang)
    fun getDisplayProductName(lang: AppLanguage): String = TreatmentTranslations.cleanParenEnglish(productName, lang)
    fun getDisplayBrandExamples(lang: AppLanguage): String = TreatmentTranslations.cleanParenEnglish(brandExamples, lang)
    fun getDisplayDoseUnit(lang: AppLanguage): String = TreatmentTranslations.cleanParenEnglish(doseUnit, lang)
    fun getDisplayActiveIngredient(lang: AppLanguage): String? = activeIngredient?.let { TreatmentTranslations.cleanParenEnglish(it, lang) }
    fun getActiveIngredient(lang: AppLanguage): String? = getDisplayActiveIngredient(lang)
    fun getDoseUnit(lang: AppLanguage): String = getDisplayDoseUnit(lang)
    fun getBrandExamples(lang: AppLanguage): String = getDisplayBrandExamples(lang)
    fun getResistanceGroup(lang: AppLanguage): String = TreatmentTranslations.cleanParenEnglish(resistanceGroup, lang)
}
