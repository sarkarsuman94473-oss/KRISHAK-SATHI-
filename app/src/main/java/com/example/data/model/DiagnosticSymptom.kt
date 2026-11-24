package com.example.data.model

import com.example.data.datasource.DiagnosticTranslations

data class DiagnosticQuestionOption(
    val optionTextBn: String,
    val optionTextEn: String,
    val diagnosisBn: String,
    val primaryCauseBn: String,     // Cultural / water / weather / nutrient / pest
    val firstActionBn: String,      // Field hygiene / water management
    val biologicalActionBn: String, // Bio-control / organic
    val chemicalActiveIngredient: String? = null,
    val dosePerLitreBn: String = "",
    val dosePerBighaBn: String = "",
    val preHarvestIntervalDays: Int = 0,
    val safetyPPEBn: String = "হাতে দস্তানা ও মুখে মাস্ক পরে শান্ত আবহাওয়ায় স্প্রে করুন।"
) {
    fun getOptionText(lang: AppLanguage): String = DiagnosticTranslations.getOptionText(this, lang)
    fun getDiagnosis(lang: AppLanguage): String = DiagnosticTranslations.getDiagnosis(this, lang)
    fun getPrimaryCause(lang: AppLanguage): String = DiagnosticTranslations.getPrimaryCause(this, lang)
    fun getFirstAction(lang: AppLanguage): String = DiagnosticTranslations.getFirstAction(this, lang)
    fun getBiologicalAction(lang: AppLanguage): String = DiagnosticTranslations.getBiologicalAction(this, lang)
    fun getDosePerLitre(lang: AppLanguage): String = DiagnosticTranslations.getDosePerLitre(this, lang)
    fun getDosePerBigha(lang: AppLanguage): String = DiagnosticTranslations.getDosePerBigha(this, lang)
    fun getSafetyPPE(lang: AppLanguage): String = DiagnosticTranslations.getSafetyPPE(this, lang)
}

data class DiagnosticSymptomTree(
    val symptomId: String,
    val titleBn: String,
    val titleEn: String,
    val iconEmoji: String,
    val initialQuestionBn: String,
    val options: List<DiagnosticQuestionOption>
) {
    fun getTitle(lang: AppLanguage): String = DiagnosticTranslations.getTreeTitle(this, lang)
    fun getInitialQuestion(lang: AppLanguage): String = DiagnosticTranslations.getTreeInitialQuestion(this, lang)
}

data class NutrientDeficiencyInfo(
    val nutrientCode: String,
    val nutrientNameBn: String,
    val nutrientNameEn: String,
    val affectedPartBn: String, // "নিচের পাতা", "কচি ডগা", "সম্পূর্ণ গাছ"
    val symptomsBn: String,
    val solutionBn: String,
    val organicRemedyBn: String
) {
    fun getNutrientName(lang: AppLanguage): String = DiagnosticTranslations.getNutrientName(this, lang)
    fun getAffectedPart(lang: AppLanguage): String = DiagnosticTranslations.getNutrientAffectedPart(this, lang)
    fun getSymptoms(lang: AppLanguage): String = DiagnosticTranslations.getNutrientSymptoms(this, lang)
    fun getSolution(lang: AppLanguage): String = DiagnosticTranslations.getNutrientSolution(this, lang)
    fun getOrganicRemedy(lang: AppLanguage): String = DiagnosticTranslations.getNutrientOrganicRemedy(this, lang)
}

data class FertilizerStagePlan(
    val stageNameBn: String,
    val timingBn: String,
    val objectiveBn: String,
    val recommendedFertilizersBn: String,
    val cautionBn: String
) {
    fun getStageName(lang: AppLanguage): String = DiagnosticTranslations.getStageName(this, lang)
    fun getTiming(lang: AppLanguage): String = DiagnosticTranslations.getStageTiming(this, lang)
    fun getObjective(lang: AppLanguage): String = DiagnosticTranslations.getStageObjective(this, lang)
    fun getRecommendedFertilizers(lang: AppLanguage): String = DiagnosticTranslations.getStageFertilizers(this, lang)
    fun getCaution(lang: AppLanguage): String = DiagnosticTranslations.getStageCaution(this, lang)
}
