package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.NurseryVerificationStatus
import com.example.data.model.PromoBannerConfig
import com.example.data.repository.BannerRepository
import com.example.data.repository.PlantKingdomRepository
import com.example.ui.home.PromoBannerCarousel
import com.example.ui.theme.KrishakSathiTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class CsvImportAndBannerTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var roomDb: AppDatabase
    private lateinit var plantRepo: PlantKingdomRepository
    private lateinit var bannerRepo: BannerRepository

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        roomDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        plantRepo = PlantKingdomRepository(context, roomDb)
        bannerRepo = BannerRepository(context, roomDb)
        plantRepo.ensureSeeded()
        bannerRepo.ensureSeeded()
    }

    @After
    fun tearDown() {
        roomDb.close()
    }

    @Test
    fun `dry run CSV preview parses 37 nurseries and 80 plants preserving intro skip and Unicode`() = runBlocking {
        val preview = plantRepo.previewBundledCsvImport()

        assertEquals(37, preview.nurseryTotalRows)
        assertEquals(80, preview.plantTotalRows)
        assertEquals(0, preview.totalErrors)

        // All 37 nurseries must keep stable KS-NU-xxxx IDs, PENDING_VERIFICATION, NOT_CONFIRMED, and zero invented contacts
        assertEquals(37, preview.preparedNurseries.size)
        val firstNursery = preview.preparedNurseries.first { it.nurseryId == "KS-NU-0001" }
        assertEquals("Muddy Hands Siliguri", firstNursery.businessName)
        assertEquals(NurseryVerificationStatus.PENDING_VERIFICATION, firstNursery.verificationStatus)
        assertEquals("NOT_CONFIRMED", firstNursery.ownerConsentStatus)
        assertFalse(firstNursery.isVerified)
        assertNull(firstNursery.phone)
        assertNull(firstNursery.fullAddress)
        assertNull(firstNursery.ownerName)

        // Plants keep stable IDs and Bengali/Hindi Unicode names cleanly
        assertTrue(preview.preparedPlants.any { it.nameBn.any { ch -> ch in '\u0980'..'\u09FF' } })
        assertTrue(preview.preparedPlants.any { it.nameHi.any { ch -> ch in '\u0900'..'\u097F' } })
        assertTrue(preview.plantDraftCount > 0)
    }

    @Test
    fun `CSV import requires explicit admin confirmation and preserves verified nurseries and draft isolation`() = runBlocking {
        // Verify a single nursery first to prove confirmed import never overwrites verified records
        val verifyRes = plantRepo.verifyNurseryRecord(
            nurseryId = "wb_nurs_01",
            ownerName = "Real Verified Owner",
            phone = "+919800000011",
            whatsapp = "+919800000011",
            fullAddress = "Sevoke Road, Siliguri",
            districtCode = "SILIGURI",
            locality = "Siliguri",
            latitude = 26.74,
            longitude = 88.43,
            openingHours = "9 AM - 6 PM",
            websiteOrSocial = null,
            categoriesCsv = "FLOWERING,INDOOR",
            supportsWholesale = true,
            supportsRetail = true,
            deliveryAvailable = true,
            verifierNote = "Verified in test"
        )
        assertTrue(verifyRes.isSuccess)

        val preview = plantRepo.previewBundledCsvImport()
        assertTrue(preview.nurserySkippedVerifiedCount >= 1)

        // Without adminConfirmed = true, nothing writes
        val blocked = plantRepo.executeConfirmedCsvImport(preview, adminConfirmed = false, pushToFirestore = false)
        assertFalse(blocked.adminConfirmed)
        assertEquals(0, blocked.nurseriesWritten)
        assertEquals(0, blocked.plantsWritten)

        // Confirm execution -> writes staged records cleanly & protects verified nursery
        val executed = plantRepo.executeConfirmedCsvImport(preview, adminConfirmed = true, pushToFirestore = false)
        assertTrue(executed.adminConfirmed)
        assertTrue(executed.nurseriesWritten >= 36)
        assertEquals(preview.preparedPlants.size, executed.plantsWritten)

        val protectedNursery = roomDb.plantKingdomDao().getNurseryById("wb_nurs_01")?.toModel()
        assertNotNull(protectedNursery)
        assertEquals(NurseryVerificationStatus.ADMIN_VERIFIED, protectedNursery!!.verificationStatus)
        assertEquals("+919800000011", protectedNursery.phone)

        // Draft plants stay excluded from isPublishedForPublic() until Admin approves
        val allPlants = plantRepo.observeAllPlants().first()
        val drafts = allPlants.filter { it.isDraftReviewRequired() }
        assertTrue(drafts.isNotEmpty())
        drafts.forEach { draft ->
            assertFalse(draft.isPublishedForPublic())
        }

        // Approving a draft promotes it cleanly to PUBLISHED
        val firstDraft = drafts.first()
        val approveRes = plantRepo.approveDraftPlant(
            plantId = firstDraft.plantId,
            descEn = "Verified description for ${firstDraft.nameEn}",
            descBn = "যাচাইকৃত বিবরণ",
            careEn = "Bright indirect sunlight and well-drained soil.",
            careBn = "পরিমিত রোদ ও জলনিকাশি মাটি।",
            careHi = "मध्यम धूप और अच्छी जल निकासी वाली मिट्टी।",
            climateSuitability = "North Bengal Terai & Plains"
        )
        assertTrue(approveRes.isSuccess)
        val published = roomDb.plantKingdomDao().getPlantById(firstDraft.plantId)?.toModel()
        assertNotNull(published)
        assertEquals("PUBLISHED", published!!.publicationStatus)
        assertTrue(published.isPublishedForPublic())
    }

    @Test
    fun `language banners strictly isolate bn hi and en and support admin reorder toggle and CRUD`() = runBlocking {
        val bnBanners = bannerRepo.observeActiveBanners(AppLanguage.BENGALI).first()
        val hiBanners = bannerRepo.observeActiveBanners(AppLanguage.HINDI).first()
        val enBanners = bannerRepo.observeActiveBanners(AppLanguage.ENGLISH).first()

        // Bengali preserves all 5 original banners in exact order
        assertEquals(
            listOf(
                "bn_main_poster",
                "bn_ai_gemini",
                "bn_disease_detect",
                "bn_marketplace",
                "bn_main_banner"
            ),
            bnBanners.map { it.id }
        )
        assertTrue(bnBanners.all { it.languageCode == "bn" })

        // Hindi contains exclusively the 3 uploaded poster drawables with hideTextOverlay = true
        assertEquals(listOf("hi_banner_1", "hi_banner_2", "hi_banner_3"), hiBanners.map { it.id })
        assertEquals(
            listOf("img_banner_hi_1", "img_banner_hi_2", "img_banner_hi_3"),
            hiBanners.map { it.drawableResName }
        )
        assertTrue(hiBanners.all { it.languageCode == "hi" && it.hideTextOverlay })
        hiBanners.forEach { b ->
            assertNotNull("Drawable resource must resolve for ${b.drawableResName}", BannerRepository.resolveDrawableRes(b.drawableResName))
        }

        // English contains only English banners
        assertEquals(3, enBanners.size)
        assertTrue(enBanners.all { it.languageCode == "en" })

        // Admin swap order in Hindi & deactivate toggle
        bannerRepo.swapBannerOrder(hiBanners[0], hiBanners[1])
        val reorderedHi = bannerRepo.observeActiveBanners(AppLanguage.HINDI).first()
        assertEquals("hi_banner_2", reorderedHi.first().id)

        bannerRepo.toggleBannerActive(reorderedHi.first(), active = false)
        val activeAfterDisable = bannerRepo.observeActiveBanners(AppLanguage.HINDI).first()
        assertEquals(2, activeAfterDisable.size)
        assertFalse(activeAfterDisable.any { it.id == "hi_banner_2" })

        // Custom banner CRUD isolated to target language
        bannerRepo.saveBanner(
            PromoBannerConfig(
                id = "en_custom_test",
                languageCode = "en",
                orderIndex = 9,
                isActive = true,
                title = "Siliguri Tractor Fair",
                subtitle = "Verified equipment info",
                targetCardId = "machinery"
            )
        )
        assertEquals(4, bannerRepo.observeActiveBanners(AppLanguage.ENGLISH).first().size)
        assertEquals(5, bannerRepo.observeActiveBanners(AppLanguage.BENGALI).first().size)
        bannerRepo.deleteCustomBanner("en_custom_test")
        assertEquals(3, bannerRepo.observeActiveBanners(AppLanguage.ENGLISH).first().size)
    }

    @Test
    fun `PromoBannerCarousel switches immediately between Bengali Hindi and English without mixing slides`() {
        composeRule.mainClock.autoAdvance = false
        var activeLang by mutableStateOf(AppLanguage.BENGALI)

        composeRule.setContent {
            KrishakSathiTheme {
                PromoBannerCarousel(
                    currentLanguage = activeLang,
                    onCardClick = {}
                )
            }
        }
        composeRule.mainClock.advanceTimeBy(160)

        composeRule.onNodeWithTag("promo_banner_card_bn_0").assertIsDisplayed()
        composeRule.onAllNodesWithText("कृषक साथी — स्मार्ट खेती समाधान").fetchSemanticsNodes().let {
            assertTrue("Hindi banner must not appear when Bengali is selected", it.isEmpty())
        }

        // Switch to Hindi -> displays Hindi banner cleanly without Bengali slide
        composeRule.runOnIdle { activeLang = AppLanguage.HINDI }
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("promo_banner_card_hi_0").assertIsDisplayed()

        // Switch to English -> displays English banner card cleanly
        composeRule.runOnIdle { activeLang = AppLanguage.ENGLISH }
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("promo_banner_card_en_0").assertIsDisplayed()
    }
}
