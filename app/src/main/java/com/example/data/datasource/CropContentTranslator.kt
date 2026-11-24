package com.example.data.datasource

import com.example.data.model.AppLanguage
import com.example.data.model.MasterCrop
import java.util.concurrent.ConcurrentHashMap

object CustomCropLocaleRegistry {
    private val dynamicEntries = ConcurrentHashMap<String, CropLocaleData>()

    fun put(cropId: String, localeData: CropLocaleData) {
        dynamicEntries[cropId.trim().lowercase()] = localeData
    }

    fun get(cropId: String): CropLocaleData? = dynamicEntries[cropId.trim().lowercase()]

    fun clear() {
        dynamicEntries.clear()
    }
}

object CropContentTranslator {

    private val allCropTranslations: Map<String, CropLocaleData> by lazy {
        VegRootFruitCropTranslations.entries +
            FieldCropTranslations.entries +
            MasterLibraryTranslations.entries
    }

    fun getLocaleData(cropId: String): CropLocaleData? =
        CustomCropLocaleRegistry.get(cropId)
            ?: allCropTranslations[cropId]
            ?: allCropTranslations[cropId.trim().lowercase()]

    fun getBestSeason(crop: MasterCrop, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> crop.bestSeasonBn
        AppLanguage.HINDI -> getLocaleData(crop.cropId)?.seasonHi?.takeIf { it.isNotBlank() }
            ?: CropTextFormats.fallbackFormat(crop.bestSeasonBn, lang)
        AppLanguage.ENGLISH -> getLocaleData(crop.cropId)?.seasonEn?.takeIf { it.isNotBlank() }
            ?: CropTextFormats.fallbackFormat(crop.bestSeasonBn, lang)
    }

    fun getSoil(crop: MasterCrop, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> crop.soilBn
        AppLanguage.HINDI -> getLocaleData(crop.cropId)?.soilHi?.takeIf { it.isNotBlank() }
            ?: CropTextFormats.fallbackFormat(crop.soilBn, lang)
        AppLanguage.ENGLISH -> getLocaleData(crop.cropId)?.soilEn?.takeIf { it.isNotBlank() }
            ?: CropTextFormats.fallbackFormat(crop.soilBn, lang)
    }

    fun getSeedRate(crop: MasterCrop, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> crop.seedRatePerBighaBn
        AppLanguage.HINDI -> getLocaleData(crop.cropId)?.seedRateHi?.takeIf { it.isNotBlank() }
            ?: CropTextFormats.fallbackFormat(crop.seedRatePerBighaBn, lang)
        AppLanguage.ENGLISH -> getLocaleData(crop.cropId)?.seedRateEn?.takeIf { it.isNotBlank() }
            ?: CropTextFormats.fallbackFormat(crop.seedRatePerBighaBn, lang)
    }

    fun getSpacing(crop: MasterCrop, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> crop.spacingBn
        AppLanguage.HINDI -> getLocaleData(crop.cropId)?.spacingHi?.takeIf { it.isNotBlank() }
            ?: CropTextFormats.fallbackFormat(crop.spacingBn, lang)
        AppLanguage.ENGLISH -> getLocaleData(crop.cropId)?.spacingEn?.takeIf { it.isNotBlank() }
            ?: CropTextFormats.fallbackFormat(crop.spacingBn, lang)
    }

    fun getExpectedYield(crop: MasterCrop, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> crop.expectedYieldBn
        AppLanguage.HINDI -> getLocaleData(crop.cropId)?.yieldHi?.takeIf { it.isNotBlank() }
            ?: CropTextFormats.fallbackFormat(crop.expectedYieldBn, lang)
        AppLanguage.ENGLISH -> getLocaleData(crop.cropId)?.yieldEn?.takeIf { it.isNotBlank() }
            ?: CropTextFormats.fallbackFormat(crop.expectedYieldBn, lang)
    }

    fun getCommonPests(crop: MasterCrop, lang: AppLanguage): List<String> {
        val raw = crop.commonPests
        if (raw.isEmpty()) return CropTextFormats.defaultPestsForCategory(crop.category, lang)
        return raw.map { CropTextFormats.translatePestOrDisease(it, lang) }
    }

    fun getCommonDiseases(crop: MasterCrop, lang: AppLanguage): List<String> {
        val raw = crop.commonDiseases
        if (raw.isEmpty()) return CropTextFormats.defaultDiseasesForCategory(crop.category, lang)
        return raw.map { CropTextFormats.translatePestOrDisease(it, lang) }
    }

    fun getSuitableClimate(crop: MasterCrop, lang: AppLanguage): String =
        CropTextFormats.suitableClimateForCrop(crop, lang)

    fun getRecommendedPh(crop: MasterCrop, lang: AppLanguage): String =
        CropTextFormats.recommendedPhForCrop(crop, lang)

    fun getLandPrep(crop: MasterCrop, lang: AppLanguage): String =
        CropTextFormats.landPrepForCrop(crop, lang)

    fun getSowingMethod(crop: MasterCrop, lang: AppLanguage): String =
        CropTextFormats.sowingMethodForCrop(crop, lang)

    fun getNurseryMethod(crop: MasterCrop, lang: AppLanguage): String =
        CropTextFormats.nurseryMethodForCrop(crop, lang)

    fun getTransplanting(crop: MasterCrop, lang: AppLanguage): String =
        CropTextFormats.transplantingForCrop(crop, lang)

    fun getWeedManagement(crop: MasterCrop, lang: AppLanguage): String =
        if (lang == AppLanguage.BENGALI) crop.weedManagementBn else CropTextFormats.weedManagement(crop.cropId, lang)

    fun getSeedTreatment(crop: MasterCrop, lang: AppLanguage): String =
        if (lang == AppLanguage.BENGALI) crop.seedTreatmentBn else CropTextFormats.seedTreatment(crop.cropId, lang)

    fun getIrrigationSchedule(crop: MasterCrop, lang: AppLanguage): String =
        CropTextFormats.irrigationScheduleForCrop(crop, lang)

    fun getOrganicManure(crop: MasterCrop, lang: AppLanguage): String =
        CropTextFormats.organicManureForCrop(crop, lang)

    fun getFertilizerSchedule(crop: MasterCrop, lang: AppLanguage): String =
        CropTextFormats.fertilizerScheduleForCrop(crop, lang)

    fun getFloweringGuidance(crop: MasterCrop, lang: AppLanguage): String =
        CropTextFormats.floweringGuidanceForCrop(crop, lang)

    fun getFruitingGuidance(crop: MasterCrop, lang: AppLanguage): String =
        CropTextFormats.fruitingGuidanceForCrop(crop, lang)

    fun getStorage(crop: MasterCrop, lang: AppLanguage): String =
        CropTextFormats.storageForCrop(crop, lang)

    fun getMarketInfo(crop: MasterCrop, lang: AppLanguage): String =
        CropTextFormats.marketInfoForCrop(crop, lang)

    fun getCommonDeficiencies(crop: MasterCrop, lang: AppLanguage): List<String> =
        CropTextFormats.commonDeficienciesForCrop(crop, lang)

    fun getPhysiologicalProblems(crop: MasterCrop, lang: AppLanguage): List<String> =
        CropTextFormats.physiologicalProblemsForCrop(crop, lang)
}
