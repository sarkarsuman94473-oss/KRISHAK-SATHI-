package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.AdminMediaReviewStore
import com.example.data.datasource.BotanicalPhotoStage
import com.example.data.datasource.CatalogMediaAuditEngine
import com.example.data.datasource.MediaVerificationStatus
import com.example.data.datasource.PlantKingdomCatalog
import com.example.data.datasource.PlantVarietyAndMediaCatalog
import com.example.data.datasource.RegionalSuitabilityTag
import com.example.data.local.config.AppConfig
import com.example.data.local.session.UserSession
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.ui.home.DashboardCatalog
import com.example.ui.home.SimpleHomeScreen
import com.example.ui.nursery.PlantDetailScreen
import com.example.ui.nursery.PlantKingdomLibraryScreen
import com.example.ui.theme.KrishakSathiTheme
import com.example.util.L10n
import com.example.util.NetworkConnectivityMonitor
import com.example.util.NetworkStatusSnapshot
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class ProductionUpgradesTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        NetworkConnectivityMonitor.setSnapshotForTesting(
            NetworkStatusSnapshot(isOnline = true, transportType = "WIFI", isValidatedInternet = true)
        )
    }

    @After
    fun tearDown() {
        UserSessionManager.clearSession(context)
        NetworkConnectivityMonitor.setSnapshotForTesting(
            NetworkStatusSnapshot(isOnline = true, transportType = "WIFI", isValidatedInternet = true)
        )
    }

    @Test
    fun `catalog contains 167 species with complete BN HI EN translations and extended fruit groups`() {
        val all = PlantKingdomCatalog.allPlants
        assertEquals(167, all.size)
        assertEquals(all.size, all.map { it.plantId }.toSet().size)
        assertEquals(all.size, all.map { it.visualAssetKey }.toSet().size)

        val bengaliRegex = Regex("[\\u0980-\\u09FF]")
        all.forEach { plant ->
            assertTrue("Missing nameEn for ${plant.plantId}", plant.nameEn.isNotBlank())
            assertTrue("Missing nameBn for ${plant.plantId}", plant.nameBn.isNotBlank())
            assertTrue("Missing nameHi for ${plant.plantId}", plant.nameHi.isNotBlank())
            assertTrue("Missing descriptionEn for ${plant.plantId}", plant.descriptionEn.isNotBlank())
            assertTrue("Missing descriptionBn for ${plant.plantId}", plant.descriptionBn.isNotBlank())
            assertTrue("Missing descriptionHi for ${plant.plantId}", plant.descriptionHi.isNotBlank())
            assertFalse(
                "Bengali script leaked into nameEn for ${plant.plantId}",
                bengaliRegex.containsMatchIn(plant.nameEn)
            )
            assertFalse(
                "Bengali script leaked into nameHi for ${plant.plantId}",
                bengaliRegex.containsMatchIn(plant.nameHi)
            )
        }

        // Representative species from all 6 fruit categories (Indian Tropical, Citrus, Commercial, Hill, Exotic, Nut)
        val requiredFruitIds = listOf(
            "mango", "jamun", "star_fruit", "phalsa",
            "gondhoraj_lemon", "kagzi_lime", "kumquat",
            "dragon_fruit", "passion_fruit", "mulberry",
            "apple", "kiwi", "persimmon", "blueberry",
            "avocado", "rambutan", "mangosteen", "jabuticaba",
            "cashew", "almond", "walnut", "pistachio", "macadamia"
        )
        val presentIds = all.map { it.plantId }.toSet()
        requiredFruitIds.forEach { reqId ->
            assertTrue("Expected fruit plant $reqId in catalog", presentIds.contains(reqId))
        }
    }

    @Test
    fun `varieties, regional suitability, and 4-stage media review workflow work accurately`() {
        val mangoVarieties = PlantVarietyAndMediaCatalog.getVarietiesForPlant("mango")
        assertEquals(12, mangoVarieties.size)
        assertEquals("Himsagar", mangoVarieties.first().nameEn)
        assertEquals("Miyazaki", mangoVarieties.last().nameEn)

        assertEquals(6, PlantVarietyAndMediaCatalog.getVarietiesForPlant("litchi").size)
        assertEquals(5, PlantVarietyAndMediaCatalog.getVarietiesForPlant("guava").size)

        assertTrue(PlantVarietyAndMediaCatalog.getSaplingVerificationDisclaimer(AppLanguage.BENGALI).isNotBlank())
        assertTrue(PlantVarietyAndMediaCatalog.getSaplingVerificationDisclaimer(AppLanguage.HINDI).isNotBlank())
        assertTrue(PlantVarietyAndMediaCatalog.getSaplingVerificationDisclaimer(AppLanguage.ENGLISH).isNotBlank())

        val mango = PlantKingdomCatalog.getById("mango")!!
        val cherry = PlantKingdomCatalog.getById("cherry")!!
        val mangoRegions = PlantVarietyAndMediaCatalog.inferRegionalSuitability(mango)
        val cherryRegions = PlantVarietyAndMediaCatalog.inferRegionalSuitability(cherry)

        assertTrue(mangoRegions.contains(RegionalSuitabilityTag.NORTH_BENGAL_PLAINS))
        assertFalse(cherryRegions.contains(RegionalSuitabilityTag.NORTH_BENGAL_PLAINS))
        assertTrue(cherryRegions.contains(RegionalSuitabilityTag.DARJEELING_KALIMPONG_HILLS))

        val initialAudit = CatalogMediaAuditEngine.runFullAudit(context)
        assertEquals(167, initialAudit.totalPlantsAudited)
        assertEquals(167 * 4, initialAudit.totalPlantStageSlots)
        assertTrue(initialAudit.verifiedPlantMediaCount > 0)
        assertTrue(initialAudit.pendingPlantMediaCount > 0)

        // Verify a pending sapling stage via AdminMediaReviewStore and verify status flips cleanly
        AdminMediaReviewStore.saveStageReview(
            context = context,
            plantId = "mango",
            stage = BotanicalPhotoStage.YOUNG_SAPLING,
            customUri = "content://media/verified_mango_sapling.jpg",
            status = MediaVerificationStatus.VERIFIED
        )
        val updatedGallery = PlantVarietyAndMediaCatalog.getStageMediaGallery(context, mango)
        val saplingStage = updatedGallery.first { it.stage == BotanicalPhotoStage.YOUNG_SAPLING }
        assertEquals(MediaVerificationStatus.VERIFIED, saplingStage.verificationStatus)
        assertEquals("content://media/verified_mango_sapling.jpg", saplingStage.customUri)
    }

    @Test
    fun `dashboard icons, network connectivity labels, quick login vs google sync, and statutory compliance behave accurately`() {
        val allTiles = DashboardCatalog.coreSections.flatMap { it.tiles } + DashboardCatalog.additionalToolCards
        assertTrue(allTiles.isNotEmpty())
        assertTrue(
            "Every dashboard tile must carry an explicit Material vector icon",
            allTiles.all { it.icon != null }
        )

        val auditReport = CatalogMediaAuditEngine.runFullAudit(context)
        assertEquals(0, auditReport.totalDashboardTilesMissingIcons)
        assertEquals(allTiles.size, auditReport.totalDashboardTilesWithIcons)

        val onlineSnap = NetworkStatusSnapshot(isOnline = true, transportType = "WIFI", isValidatedInternet = true)
        val offlineSnap = NetworkStatusSnapshot(isOnline = false, transportType = "NONE", isValidatedInternet = false)
        assertEquals("অনলাইন", onlineSnap.getBadgeText(AppLanguage.BENGALI))
        assertEquals("ऑफ़लाइन मोड", offlineSnap.getBadgeText(AppLanguage.HINDI))
        assertEquals("Offline Mode", offlineSnap.getBadgeText(AppLanguage.ENGLISH))

        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = "farmer_local_1",
                userName = "Ratan Das",
                authProvider = UserSession.PROVIDER_FARMER_DIRECT
            )
        )
        assertTrue(UserSessionManager.isLocalQuickLogin(context))
        assertFalse(UserSessionManager.hasCloudFirebaseSession(context))

        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = "google_uid_1",
                userName = "Ratan Das",
                email = "ratan@example.com",
                authProvider = UserSession.PROVIDER_GOOGLE
            )
        )
        assertFalse(UserSessionManager.isLocalQuickLogin(context))
        assertTrue(UserSessionManager.hasCloudFirebaseSession(context))

        val incompleteCfg = AppConfig()
        assertFalse(incompleteCfg.hasCompleteStatutoryOfficers())

        val completeCfg = AppConfig(
            grievanceOfficerName = "Verified Officer",
            grievanceOfficerEmail = "grievance@krishaksathi.in",
            grievanceOfficerPhone = "+913532000000",
            grievanceOfficerAddress = "Siliguri, West Bengal",
            legalEntityName = "Krishak Sathi Agri Pvt Ltd"
        )
        assertTrue(completeCfg.hasCompleteStatutoryOfficers())
    }

    @Test
    fun `SimpleHomeScreen renders network status pill, hero banner, crops tracker, and offline banner`() {
        composeRule.mainClock.autoAdvance = false
        val offlineSnap = NetworkStatusSnapshot(isOnline = false, transportType = "NONE", isValidatedInternet = false)
        composeRule.setContent {
            KrishakSathiTheme {
                SimpleHomeScreen(
                    farmerName = "Ratan",
                    language = AppLanguage.ENGLISH,
                    networkStatus = offlineSnap,
                    isCloudConnected = false,
                    onCardClick = {},
                    onVoiceClick = {},
                    onProfileClick = {}
                )
            }
        }
        composeRule.mainClock.advanceTimeBy(160L)

        composeRule.onNodeWithTag("network_status_pill").assertIsDisplayed()
        composeRule.onNodeWithTag("offline_mode_banner").assertIsDisplayed()
        composeRule.onNodeWithTag("home_luxury_hero_banner").assertIsDisplayed()
        composeRule.onNodeWithTag("home_my_crops_summary_card").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `PlantDetailScreen displays 4-stage gallery, regional suitability, 12 mango varieties, and action bar`() {
        composeRule.setContent {
            KrishakSathiTheme {
                PlantDetailScreen(
                    plantId = "mango",
                    currentLanguage = AppLanguage.ENGLISH,
                    onBack = {},
                    onOpenDirectory = {},
                    onOpenMarketplace = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("plant_stage_YOUNG_SAPLING").assertIsDisplayed()
        composeRule.onNodeWithTag("plant_stage_RIPE_FRUIT").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("plant_regional_suitability_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("plant_varieties_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("plant_sapling_verification_disclaimer").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Himsagar", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("plant_action_bar_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("plant_action_inquiry_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("plant_action_call_btn").assertIsDisplayed()
    }

    @Test
    fun `PlantKingdomLibraryScreen supports regional suitability filters and trilingual switching`() {
        var activeLang = AppLanguage.ENGLISH
        composeRule.setContent {
            KrishakSathiTheme {
                PlantKingdomLibraryScreen(
                    currentLanguage = activeLang,
                    onBack = {},
                    onOpenPlantDetail = {},
                    onOpenNurseryDirectory = {},
                    onOpenNurseryMap = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("plant_kingdom_showcase_banner").assertIsDisplayed()
        composeRule.onNodeWithTag("plant_region_DARJEELING_KALIMPONG_HILLS").performScrollTo().assertIsDisplayed()

        // Verify L10n lookup returns distinct non-blank Hindi & Bengali text for new plant UI keys
        assertTrue(L10n.t("plant_varieties_title", AppLanguage.BENGALI).isNotBlank())
        assertTrue(L10n.t("plant_varieties_title", AppLanguage.HINDI).isNotBlank())
        assertAllTitlesDistinct("plant_varieties_title")
    }

    private fun assertAllTitlesDistinct(key: String) {
        val en = L10n.t(key, AppLanguage.ENGLISH)
        val bn = L10n.t(key, AppLanguage.BENGALI)
        val hi = L10n.t(key, AppLanguage.HINDI)
        assertTrue("$key missing EN", en.isNotBlank() && en != key)
        assertTrue("$key missing BN", bn.isNotBlank() && bn != key)
        assertTrue("$key missing HI", hi.isNotBlank() && hi != key)
        composeRule.onAllNodesWithText("Darjeeling", substring = true)
    }
}
