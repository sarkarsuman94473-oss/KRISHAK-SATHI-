package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.CropMasterDatabase
import com.example.data.datasource.DiagnosticKnowledgeDatabase
import com.example.data.datasource.ExpertTranslations
import com.example.data.datasource.GovernmentSchemesCatalog
import com.example.data.datasource.LeadingFarmerTranslations
import com.example.data.datasource.MarketPriceDatabase
import com.example.data.datasource.NurseryTranslations
import com.example.data.datasource.StarterNurseryDatabase
import com.example.data.datasource.VerifiedTreatmentDatabase
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ExpertCategories
import com.example.data.model.AppLanguage
import com.example.data.model.CropCategory
import com.example.data.model.CropMasterGroup
import com.example.data.model.CropRegion
import com.example.data.model.CropSeason
import com.example.data.model.ForumModerationStatus
import com.example.data.model.ForumReportReason
import com.example.data.model.ForumReportStatus
import com.example.data.model.ForumTopic
import com.example.data.model.ItemStockStatus
import com.example.data.model.NurseryApplicationStatus
import com.example.data.model.NurseryProductCategory
import com.example.data.model.PlantCategory
import com.example.data.model.SchemeActivityFocus
import com.example.data.model.SchemeApplicationStatus
import com.example.data.model.SchemeCategory
import com.example.data.model.SchemeFallbackText
import com.example.data.model.SchemeFarmerType
import com.example.data.model.SchemeGovLevel
import com.example.data.model.SeedCategory
import com.example.data.model.UserModerationStatus
import com.example.data.model.UserRole
import com.example.data.model.isOfficialGovernmentPortalDomain
import com.example.data.model.isValidOfficialHttpsUrl
import com.example.data.remote.ForumRepository
import com.example.data.remote.ForumStarterTranslations
import com.example.data.remote.NurseryApplicationRepository
import com.example.data.remote.NurseryProductRepository
import com.example.data.repository.ExpertRepository
import com.example.data.repository.LeadingFarmerRepository
import com.example.util.BengaliStrings
import com.example.util.EnglishStrings
import com.example.util.HindiStrings
import com.example.util.L10n
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocalizationCoverageTest {

    private val nonBengaliLanguages = listOf(AppLanguage.HINDI, AppLanguage.ENGLISH)
    private val allLanguages = listOf(AppLanguage.BENGALI, AppLanguage.HINDI, AppLanguage.ENGLISH)

    private fun hasBengaliScript(text: String): Boolean = text.any { it in '\u0980'..'\u09FF' }

    private fun assertCleanLocalized(contextLabel: String, lang: AppLanguage, value: String, allowBlank: Boolean = false) {
        if (!allowBlank) {
            assertTrue("Expected non-blank value for $contextLabel in $lang", value.isNotBlank())
        }
        if (lang != AppLanguage.BENGALI) {
            assertFalse(
                "Unexpected Bengali script in $contextLabel for $lang: '$value'",
                hasBengaliScript(value)
            )
        }
    }

    private fun collectCleanError(
        errors: MutableList<String>,
        contextLabel: String,
        lang: AppLanguage,
        value: String,
        allowBlank: Boolean = false
    ) {
        if (!allowBlank && value.isBlank()) {
            errors.add("Expected non-blank value for $contextLabel in $lang")
        }
        if (lang != AppLanguage.BENGALI && hasBengaliScript(value)) {
            errors.add("Unexpected Bengali script in $contextLabel for $lang: '$value'")
        }
    }

    @Test
    fun `verify L10n string maps have key parity and zero Bengali script bleed in Hindi and English`() {
        val allowedNativeBengaliKeys = setOf("lang_bengali", "settings_language_desc")

        val missingInHindi = BengaliStrings.keys - HindiStrings.keys
        assertTrue("Keys missing in HindiStrings: $missingInHindi", missingInHindi.isEmpty())

        val missingInEnglish = BengaliStrings.keys - EnglishStrings.keys
        assertTrue("Keys missing in EnglishStrings: $missingInEnglish", missingInEnglish.isEmpty())

        for ((key, value) in HindiStrings) {
            assertTrue("Blank value for key '$key' in HindiStrings", value.isNotBlank())
            if (key !in allowedNativeBengaliKeys) {
                assertFalse("Bengali bleed in HindiStrings['$key']: $value", hasBengaliScript(value))
            }
        }
        for ((key, value) in EnglishStrings) {
            assertTrue("Blank value for key '$key' in EnglishStrings", value.isNotBlank())
            if (key !in allowedNativeBengaliKeys) {
                assertFalse("Bengali bleed in EnglishStrings['$key']: $value", hasBengaliScript(value))
            }
        }
        for (key in BengaliStrings.keys) {
            for (lang in allLanguages) {
                val resolved = L10n.t(key, lang)
                assertTrue("Resolved string blank for '$key' in $lang", resolved.isNotBlank())
                if (lang != AppLanguage.BENGALI && key !in allowedNativeBengaliKeys) {
                    assertFalse("Bengali bleed from L10n.t('$key', $lang): $resolved", hasBengaliScript(resolved))
                }
            }
        }
    }

    @Test
    fun `verify all crops in CropMasterDatabase translate completely across Bengali Hindi and English`() {
        val crops = CropMasterDatabase.crops
        assertTrue("Expected at least 150 crops in expanded CropMasterDatabase (was ${crops.size})", crops.size >= 150)

        // Ensure zero duplicate cropIds (case-insensitive)
        val ids = crops.map { it.cropId.lowercase() }
        assertEquals("CropMasterDatabase must have zero duplicate cropIds", ids.distinct().size, ids.size)

        // Verify all 12 Master Groups (A-L) are populated
        CropMasterGroup.values().forEach { grp ->
            val groupCrops = crops.filter { it.getMasterGroup() == grp }
            assertTrue("Expected crops in MasterGroup ${grp.name}", groupCrops.isNotEmpty())
        }

        val requiredSpotCheckIds = listOf(
            "tomato", "potato", "rice_paddy", "wheat", "tea", "mustard",
            "chilli", "cauliflower", "cabbage", "mulo", "palak",
            "dhane_pata", "brinjal", "okra", "french_bean", "pumpkin", "bottle_gourd",
            "rose", "marigold", "tuberose", "orchid", "lotus",
            "snake_plant", "money_plant", "ashwagandha", "tulsi", "turmeric",
            "dhaincha", "teak_tree", "dragon_fruit", "kiwi"
        )
        for (id in requiredSpotCheckIds) {
            assertNotNull("Expected crop '$id' to exist in CropMasterDatabase", CropMasterDatabase.getCropById(id))
        }

        val errors = mutableListOf<String>()
        for (crop in crops) {
            for (lang in allLanguages) {
                val prefix = "crop(${crop.cropId})"
                collectCleanError(errors, "$prefix.name", lang, crop.getName(lang))
                collectCleanError(errors, "$prefix.summary", lang, crop.getSummary(lang))
                collectCleanError(errors, "$prefix.bestSeason", lang, crop.getBestSeason(lang))
                collectCleanError(errors, "$prefix.soil", lang, crop.getSoil(lang))
                collectCleanError(errors, "$prefix.seedRate", lang, crop.getSeedRate(lang))
                collectCleanError(errors, "$prefix.spacing", lang, crop.getSpacing(lang))
                collectCleanError(errors, "$prefix.expectedYield", lang, crop.getExpectedYield(lang))
                collectCleanError(errors, "$prefix.suitableClimate", lang, crop.getSuitableClimate(lang))
                collectCleanError(errors, "$prefix.recommendedPh", lang, crop.getRecommendedPh(lang))
                collectCleanError(errors, "$prefix.landPrep", lang, crop.getLandPrep(lang))
                collectCleanError(errors, "$prefix.sowingMethod", lang, crop.getSowingMethod(lang))
                collectCleanError(errors, "$prefix.nurseryMethod", lang, crop.getNurseryMethod(lang))
                collectCleanError(errors, "$prefix.transplanting", lang, crop.getTransplanting(lang))
                collectCleanError(errors, "$prefix.weedManagement", lang, crop.getWeedManagement(lang))
                collectCleanError(errors, "$prefix.seedTreatment", lang, crop.getSeedTreatment(lang))
                collectCleanError(errors, "$prefix.irrigationSchedule", lang, crop.getIrrigationSchedule(lang))
                collectCleanError(errors, "$prefix.organicManure", lang, crop.getOrganicManure(lang))
                collectCleanError(errors, "$prefix.fertilizerSchedule", lang, crop.getFertilizerSchedule(lang))
                collectCleanError(errors, "$prefix.floweringGuidance", lang, crop.getFloweringGuidance(lang))
                collectCleanError(errors, "$prefix.fruitingGuidance", lang, crop.getFruitingGuidance(lang))
                collectCleanError(errors, "$prefix.storage", lang, crop.getStorage(lang))
                collectCleanError(errors, "$prefix.marketInfo", lang, crop.getMarketInfo(lang))
                collectCleanError(errors, "$prefix.varieties", lang, crop.getVarieties(lang))
                collectCleanError(errors, "$prefix.regionalSuitability", lang, crop.getRegionalSuitability(lang))
                collectCleanError(errors, "$prefix.growthStages", lang, crop.getGrowthStagesSummary(lang))
                collectCleanError(errors, "$prefix.harvestMethod", lang, crop.getHarvestMethod(lang))
                collectCleanError(errors, "$prefix.cultivationCost", lang, crop.getCultivationCostInfo(lang))
                collectCleanError(errors, "$prefix.expertTips", lang, crop.getExpertTips(lang))

                crop.getFaqsList(lang).forEachIndexed { idx, faq ->
                    collectCleanError(errors, "$prefix.faq[$idx].q", lang, faq.getQuestion(lang))
                    collectCleanError(errors, "$prefix.faq[$idx].a", lang, faq.getAnswer(lang))
                }

                crop.getCommonPestsList(lang).forEachIndexed { idx, s ->
                    collectCleanError(errors, "$prefix.pests[$idx]", lang, s)
                }
                crop.getCommonDiseasesList(lang).forEachIndexed { idx, s ->
                    collectCleanError(errors, "$prefix.diseases[$idx]", lang, s)
                }
                crop.getCommonDeficienciesList(lang).forEachIndexed { idx, s ->
                    collectCleanError(errors, "$prefix.deficiencies[$idx]", lang, s)
                }
                crop.getPhysiologicalProblemsList(lang).forEachIndexed { idx, s ->
                    collectCleanError(errors, "$prefix.physiological[$idx]", lang, s)
                }

                crop.steps.forEachIndexed { idx, step ->
                    collectCleanError(errors, "$prefix.step[$idx].title", lang, step.getTitle(lang))
                    collectCleanError(errors, "$prefix.step[$idx].description", lang, step.getDescription(lang))
                }
            }
        }
        assertTrue("Crop localization errors (${errors.size}):\n${errors.take(25).joinToString("\n")}", errors.isEmpty())
    }

    @Test
    fun `verify all category and status enums translate across Bengali Hindi and English`() {
        for (lang in allLanguages) {
            CropMasterGroup.values().forEach { assertCleanLocalized("CropMasterGroup.${it.name}", lang, it.getDisplayName(lang)) }
            CropSeason.values().forEach { assertCleanLocalized("CropSeason.${it.name}", lang, it.getDisplayName(lang)) }
            CropRegion.values().forEach { assertCleanLocalized("CropRegion.${it.name}", lang, it.getDisplayName(lang)) }
            CropCategory.values().forEach { assertCleanLocalized("CropCategory.${it.name}", lang, it.getDisplayName(lang)) }
            PlantCategory.values().forEach { assertCleanLocalized("PlantCategory.${it.name}", lang, it.getDisplayName(lang)) }
            SeedCategory.values().forEach { assertCleanLocalized("SeedCategory.${it.name}", lang, it.getDisplayName(lang)) }
            ItemStockStatus.values().forEach { assertCleanLocalized("ItemStockStatus.${it.name}", lang, it.getDisplayName(lang)) }
            NurseryProductCategory.values().forEach { assertCleanLocalized("NurseryProductCategory.${it.name}", lang, it.getDisplayName(lang)) }
            UserRole.values().forEach { assertCleanLocalized("UserRole.${it.name}", lang, it.getLabel(lang)) }
            NurseryApplicationStatus.values().forEach { assertCleanLocalized("NurseryApplicationStatus.${it.name}", lang, it.getLabel(lang)) }
            ExpertCategories.ALL_CATEGORIES.forEach { assertCleanLocalized("ExpertCategory.${it.key}", lang, it.getName(lang)) }
            ForumTopic.values().forEach { assertCleanLocalized("ForumTopic.${it.name}", lang, it.getLabel(lang)) }
            ForumModerationStatus.values().forEach { assertCleanLocalized("ForumModerationStatus.${it.name}", lang, it.getLabel(lang)) }
            ForumReportReason.values().forEach { assertCleanLocalized("ForumReportReason.${it.name}", lang, it.getLabel(lang)) }
            ForumReportStatus.values().forEach { assertCleanLocalized("ForumReportStatus.${it.name}", lang, it.getLabel(lang)) }
            UserModerationStatus.values().forEach { assertCleanLocalized("UserModerationStatus.${it.name}", lang, it.getLabel(lang)) }
            SchemeGovLevel.values().forEach { assertCleanLocalized("SchemeGovLevel.${it.name}", lang, it.getLabel(lang)) }
            SchemeCategory.values().forEach { assertCleanLocalized("SchemeCategory.${it.name}", lang, it.getLabel(lang)) }
            SchemeFarmerType.values().forEach { assertCleanLocalized("SchemeFarmerType.${it.name}", lang, it.getLabel(lang)) }
            SchemeActivityFocus.values().forEach { assertCleanLocalized("SchemeActivityFocus.${it.name}", lang, it.getLabel(lang)) }
            SchemeApplicationStatus.values().forEach { assertCleanLocalized("SchemeApplicationStatus.${it.name}", lang, it.getLabel(lang)) }
            assertCleanLocalized("SchemeFallbackText.checkOfficialWebsite", lang, SchemeFallbackText.checkOfficialWebsite(lang))
            assertCleanLocalized("SchemeFallbackText.disclaimer", lang, SchemeFallbackText.disclaimer(lang))
        }
    }

    @Test
    fun `verify DiagnosticKnowledgeDatabase and VerifiedTreatmentDatabase translate cleanly across all languages`() {
        for (tree in DiagnosticKnowledgeDatabase.symptomTrees) {
            for (lang in allLanguages) {
                assertCleanLocalized("symptom(${tree.symptomId}).title", lang, tree.getTitle(lang))
                assertCleanLocalized("symptom(${tree.symptomId}).initialQuestion", lang, tree.getInitialQuestion(lang))
                tree.options.forEachIndexed { idx, opt ->
                    assertCleanLocalized("symptom(${tree.symptomId}).opt[$idx].optionText", lang, opt.getOptionText(lang))
                    assertCleanLocalized("symptom(${tree.symptomId}).opt[$idx].diagnosis", lang, opt.getDiagnosis(lang))
                    assertCleanLocalized("symptom(${tree.symptomId}).opt[$idx].primaryCause", lang, opt.getPrimaryCause(lang))
                    assertCleanLocalized("symptom(${tree.symptomId}).opt[$idx].firstAction", lang, opt.getFirstAction(lang))
                    assertCleanLocalized("symptom(${tree.symptomId}).opt[$idx].biologicalAction", lang, opt.getBiologicalAction(lang))
                    assertCleanLocalized("symptom(${tree.symptomId}).opt[$idx].dosePerLitre", lang, opt.getDosePerLitre(lang), allowBlank = true)
                    assertCleanLocalized("symptom(${tree.symptomId}).opt[$idx].dosePerBigha", lang, opt.getDosePerBigha(lang), allowBlank = true)
                    assertCleanLocalized("symptom(${tree.symptomId}).opt[$idx].safetyPPE", lang, opt.getSafetyPPE(lang))
                }
            }
        }

        for (def in DiagnosticKnowledgeDatabase.nutrientDeficiencies) {
            for (lang in allLanguages) {
                assertCleanLocalized("nutrient(${def.nutrientCode}).name", lang, def.getNutrientName(lang))
                assertCleanLocalized("nutrient(${def.nutrientCode}).affectedPart", lang, def.getAffectedPart(lang))
                assertCleanLocalized("nutrient(${def.nutrientCode}).symptoms", lang, def.getSymptoms(lang))
                assertCleanLocalized("nutrient(${def.nutrientCode}).solution", lang, def.getSolution(lang))
                assertCleanLocalized("nutrient(${def.nutrientCode}).organicRemedy", lang, def.getOrganicRemedy(lang))
            }
        }

        for (stage in DiagnosticKnowledgeDatabase.fertilizerStages) {
            for (lang in allLanguages) {
                assertCleanLocalized("fertStage(${stage.stageNameBn}).stageName", lang, stage.getStageName(lang))
                assertCleanLocalized("fertStage(${stage.stageNameBn}).timing", lang, stage.getTiming(lang))
                assertCleanLocalized("fertStage(${stage.stageNameBn}).objective", lang, stage.getObjective(lang))
                assertCleanLocalized("fertStage(${stage.stageNameBn}).fertilizers", lang, stage.getRecommendedFertilizers(lang))
                assertCleanLocalized("fertStage(${stage.stageNameBn}).caution", lang, stage.getCaution(lang))
            }
        }

        for (vt in VerifiedTreatmentDatabase.treatments) {
            for (lang in allLanguages) {
                val p = "treatment(${vt.problemId})"
                assertCleanLocalized("$p.cropDisplayName", lang, vt.getCropDisplayName(lang))
                assertCleanLocalized("$p.problemName", lang, vt.getProblemName(lang))
                assertCleanLocalized("$p.symptoms", lang, vt.getSymptoms(lang))
                assertCleanLocalized("$p.possibleCause", lang, vt.getPossibleCause(lang))
                assertCleanLocalized("$p.firstAction", lang, vt.getFirstAction(lang))
                assertCleanLocalized("$p.biologicalOption", lang, vt.getBiologicalOption(lang))
                assertCleanLocalized("$p.growthStage", lang, vt.getGrowthStage(lang))
                assertCleanLocalized("$p.dosePerLitre", lang, vt.getDosePerLitre(lang))
                assertCleanLocalized("$p.safetyInstructions", lang, vt.getSafetyInstructions(lang))
                assertCleanLocalized("$p.applicationMethod", lang, vt.getApplicationMethod(lang))
                assertCleanLocalized("$p.productTypeLabel", lang, vt.getProductTypeLabel(lang))
                assertCleanLocalized("$p.lastReviewedLabel", lang, vt.getLastReviewedLabel(lang))
                assertCleanLocalized("$p.productName", lang, vt.getDisplayProductName(lang), allowBlank = true)
                assertCleanLocalized("$p.brandExamples", lang, vt.getDisplayBrandExamples(lang), allowBlank = true)
                assertCleanLocalized("$p.doseUnit", lang, vt.getDisplayDoseUnit(lang))
                vt.getDisplayActiveIngredient(lang)?.let {
                    assertCleanLocalized("$p.activeIngredient", lang, it, allowBlank = true)
                }
                assertCleanLocalized("$p.resistanceGroup", lang, vt.getResistanceGroup(lang))
                vt.nearbySellers.forEachIndexed { idx, seller ->
                    assertCleanLocalized("$p.seller[$idx].shopName", lang, seller.getShopName(lang))
                    assertCleanLocalized("$p.seller[$idx].address", lang, seller.getAddress(lang))
                }
            }
        }
    }

    @Test
    fun `verify MarketPriceDatabase and StarterNurseryDatabase translate cleanly across all languages`() {
        for (item in MarketPriceDatabase.prices) {
            for (lang in allLanguages) {
                val p = "market(${item.productEn})"
                assertCleanLocalized("$p.product", lang, item.getProduct(lang))
                assertCleanLocalized("$p.marketName", lang, item.getMarketName(lang))
                assertCleanLocalized("$p.district", lang, item.getDistrict(lang))
                assertCleanLocalized("$p.unit", lang, item.getUnit(lang))
                assertCleanLocalized("$p.source", lang, item.getSource(lang))
                assertCleanLocalized("$p.lastUpdated", lang, item.getLastUpdated(lang))
                assertCleanLocalized("$p.trendLabel", lang, item.getTrendLabel(lang))
            }
        }

        for (nursery in StarterNurseryDatabase.starterNurseries) {
            for (lang in allLanguages) {
                val p = "nursery(${nursery.id})"
                assertCleanLocalized("$p.displayName", lang, nursery.getDisplayName(lang))
                assertCleanLocalized("$p.owner", lang, nursery.getOwner(lang))
                assertCleanLocalized("$p.address", lang, nursery.getAddress(lang))
                assertCleanLocalized("$p.badge", lang, nursery.getBadge(lang))
                for (cat in nursery.catalogue) {
                    assertCleanLocalized("$p.catalogue(${cat.id}).name", lang, cat.getName(lang))
                    assertCleanLocalized("$p.catalogue(${cat.id}).variety", lang, cat.getVariety(lang))
                    assertCleanLocalized("$p.catalogue(${cat.id}).unit", lang, cat.getUnit(lang))
                }
            }
        }

        for (seed in StarterNurseryDatabase.starterSeeds) {
            for (lang in allLanguages) {
                val p = "seed(${seed.id})"
                assertCleanLocalized("$p.cropName", lang, seed.getCropName(lang))
                assertCleanLocalized("$p.variety", lang, seed.getVariety(lang))
                assertCleanLocalized("$p.brand", lang, seed.getBrand(lang))
                assertCleanLocalized("$p.packetSize", lang, seed.getPacketSize(lang))
            }
        }
    }

    @Test
    fun `verify seeded NurseryProducts Orders Applications Experts and LeadingFarmers translate cleanly across all languages`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Seeded Nursery Products & Orders
        val productRepo = NurseryProductRepository(context)
        val seededProducts = productRepo.allProductsFlow.first()
        assertTrue("Expected at least 10 seeded nursery products", seededProducts.size >= 10)
        for (prod in seededProducts) {
            for (lang in allLanguages) {
                val p = "nurseryProduct(${prod.id})"
                assertCleanLocalized("$p.name", lang, prod.getDisplayName(lang))
                assertCleanLocalized("$p.nurseryName", lang, prod.getDisplayNurseryName(lang))
                assertCleanLocalized("$p.description", lang, prod.getDisplayDescription(lang))
                assertCleanLocalized("$p.size", lang, prod.getDisplaySize(lang))
                assertCleanLocalized("$p.type", lang, prod.getDisplayType(lang))
                assertCleanLocalized("$p.care", lang, prod.getDisplayCare(lang))
                assertCleanLocalized("$p.location", lang, prod.getDisplayLocation(lang))
            }
        }

        val seededOrders = productRepo.allOrdersFlow.first()
        for (order in seededOrders) {
            for (lang in allLanguages) {
                val p = "nurseryOrder(${order.id})"
                assertCleanLocalized("$p.customerName", lang, NurseryTranslations.getOrderCustomerName(order, lang))
                assertCleanLocalized("$p.address", lang, NurseryTranslations.getOrderAddress(order, lang))
                order.items.forEachIndexed { idx, item ->
                    assertCleanLocalized("$p.item[$idx]", lang, NurseryTranslations.getOrderItemName(item, lang))
                }
            }
        }

        // 2. Seeded Nursery Applications
        val appRepo = NurseryApplicationRepository(context)
        val seededApps = appRepo.allApplicationsFlow.first()
        assertTrue("Expected seeded nursery applications", seededApps.isNotEmpty())
        for (app in seededApps) {
            for (lang in allLanguages) {
                val p = "nurseryApp(${app.id})"
                assertCleanLocalized("$p.displayName", lang, app.getDisplayName(lang))
                assertCleanLocalized("$p.ownerName", lang, app.getDisplayOwnerName(lang))
                assertCleanLocalized("$p.description", lang, app.getDisplayDescription(lang))
                assertCleanLocalized("$p.fullAddress", lang, app.getDisplayFullAddress(lang))
                assertCleanLocalized("$p.openingHours", lang, app.getDisplayOpeningHours(lang))
            }
        }

        // 3. Seeded Agriculture Experts
        val expertRepo = ExpertRepository(context)
        expertRepo.seedInitialExpertsIfEmpty()
        val seededExperts = expertRepo.allExperts.first()
        assertTrue("Expected seeded agriculture experts", seededExperts.isNotEmpty())
        for (expert in seededExperts) {
            for (lang in allLanguages) {
                val p = "expert(${expert.id})"
                assertCleanLocalized("$p.name", lang, ExpertTranslations.getDisplayName(expert, lang))
                assertCleanLocalized("$p.designation", lang, ExpertTranslations.getDisplayDesignation(expert, lang))
                assertCleanLocalized("$p.institution", lang, ExpertTranslations.getDisplayInstitution(expert, lang))
                assertCleanLocalized("$p.districts", lang, ExpertTranslations.getDisplayDistricts(expert, lang))
                assertCleanLocalized("$p.crops", lang, ExpertTranslations.getDisplayCrops(expert, lang))
                assertCleanLocalized("$p.problems", lang, ExpertTranslations.getDisplayProblems(expert, lang))
                assertCleanLocalized("$p.languages", lang, ExpertTranslations.getDisplayLanguages(expert, lang))
                assertCleanLocalized("$p.fee", lang, ExpertTranslations.getDisplayFee(expert.consultationFee, lang))
            }
        }

        // 4. Seeded Leading Farmers
        val roomDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val farmerRepo = LeadingFarmerRepository(roomDb.leadingFarmerDao())
        farmerRepo.seedInitialFarmersIfNeeded()
        val seededFarmers = farmerRepo.approvedFarmers.first()
        assertTrue("Expected at least 10 seeded leading farmers", seededFarmers.size >= 10)
        for (farmer in seededFarmers) {
            for (lang in allLanguages) {
                val p = "leadingFarmer(${farmer.id})"
                assertCleanLocalized("$p.name", lang, LeadingFarmerTranslations.getDisplayName(farmer, lang))
                assertCleanLocalized("$p.specialization", lang, LeadingFarmerTranslations.getDisplaySpecialization(farmer, lang))
                assertCleanLocalized("$p.innovation", lang, LeadingFarmerTranslations.getDisplayInnovation(farmer, lang))
                assertCleanLocalized("$p.location", lang, LeadingFarmerTranslations.getDisplayLocation(farmer, lang))
                assertCleanLocalized("$p.award", lang, LeadingFarmerTranslations.getDisplayAward(farmer, lang))
                assertCleanLocalized("$p.allAwards", lang, LeadingFarmerTranslations.getDisplayAllAwards(farmer, lang))
                assertCleanLocalized("$p.crops", lang, LeadingFarmerTranslations.getDisplayCrops(farmer, lang))
                assertCleanLocalized("$p.techniques", lang, LeadingFarmerTranslations.getDisplayTechniques(farmer, lang))
                assertCleanLocalized("$p.storyHowStarted", lang, LeadingFarmerTranslations.getStoryHowStarted(farmer, lang))
                assertCleanLocalized("$p.storyChallenges", lang, LeadingFarmerTranslations.getStoryChallenges(farmer, lang))
                assertCleanLocalized("$p.storyMethods", lang, LeadingFarmerTranslations.getStoryMethods(farmer, lang))
                assertCleanLocalized("$p.storyAchievements", lang, LeadingFarmerTranslations.getStoryAchievements(farmer, lang))
                assertCleanLocalized("$p.storyLessons", lang, LeadingFarmerTranslations.getStoryLessons(farmer, lang))
                LeadingFarmerTranslations.getDisplayOrg(farmer, lang)?.let {
                    assertCleanLocalized("$p.org", lang, it)
                }
                LeadingFarmerTranslations.getDisplayPublicContact(farmer, lang)?.let {
                    assertCleanLocalized("$p.publicContact", lang, it)
                }
                assertCleanLocalized("$p.contactNotice", lang, LeadingFarmerTranslations.getDisplayContactNotice(farmer, lang))
                assertCleanLocalized("$p.sources", lang, LeadingFarmerTranslations.getDisplaySources(farmer, lang))
            }
        }
        roomDb.close()

        // 5. Seeded Community Forum Posts & Comments
        val forumRepo = ForumRepository({ null }, context)
        val starterPosts = forumRepo.observePosts(isAdmin = true).first()
        assertTrue("Expected starter forum posts", starterPosts.size >= 5)
        for (post in starterPosts) {
            for (lang in allLanguages) {
                assertCleanLocalized("forumPost(${post.id}).title", lang, ForumStarterTranslations.getLocalizedTitle(post, lang))
                assertCleanLocalized("forumPost(${post.id}).content", lang, ForumStarterTranslations.getLocalizedContent(post, lang))
            }
            val comments = forumRepo.observeComments(post.id, isAdmin = true).first()
            for (comment in comments) {
                for (lang in allLanguages) {
                    assertCleanLocalized("forumComment(${comment.id})", lang, ForumStarterTranslations.getLocalizedComment(comment, lang))
                }
            }
        }

        // 6. Verified Government Schemes Catalog (All 18 categories + HTTPS official domains)
        val schemes = GovernmentSchemesCatalog.allSchemes
        assertEquals(18, SchemeCategory.entries.size)
        SchemeCategory.entries.forEach { category ->
            assertTrue(
                "Expected at least one verified scheme for category ${category.name}",
                schemes.any { category in it.resolveAllCategories() }
            )
        }
        for (scheme in schemes) {
            assertTrue("Scheme ${scheme.id} officialUrl must be HTTPS: ${scheme.officialUrl}", isValidOfficialHttpsUrl(scheme.officialUrl))
            assertTrue("Scheme ${scheme.id} officialSourceUrl must be HTTPS: ${scheme.officialSourceUrl}", isValidOfficialHttpsUrl(scheme.officialSourceUrl))
            assertTrue("Scheme ${scheme.id} officialUrl must be official gov domain: ${scheme.officialUrl}", isOfficialGovernmentPortalDomain(scheme.officialUrl))
            assertTrue("Scheme ${scheme.id} officialSourceUrl must be official gov domain: ${scheme.officialSourceUrl}", isOfficialGovernmentPortalDomain(scheme.officialSourceUrl))
            for (lang in allLanguages) {
                val p = "scheme(${scheme.id})"
                assertCleanLocalized("$p.name", lang, scheme.getName(lang))
                assertCleanLocalized("$p.department", lang, scheme.getDepartment(lang))
                assertCleanLocalized("$p.shortDesc", lang, scheme.getShortDesc(lang))
                assertCleanLocalized("$p.objectives", lang, scheme.getObjectives(lang))
                assertCleanLocalized("$p.whoCanApply", lang, scheme.getWhoCanApply(lang))
                assertCleanLocalized("$p.eligibility", lang, scheme.getEligibility(lang))
                assertCleanLocalized("$p.documents", lang, scheme.getDocuments(lang))
                assertCleanLocalized("$p.benefits", lang, scheme.getBenefits(lang))
                assertCleanLocalized("$p.procedure", lang, scheme.getProcedure(lang))
                assertCleanLocalized("$p.statusNote", lang, scheme.getStatusNote(lang))
                assertCleanLocalized("$p.cropTags", lang, scheme.getCropTags(lang), allowBlank = true)
            }
        }
    }
}
