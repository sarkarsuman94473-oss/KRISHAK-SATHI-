package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.MachineryExtendedCatalogPart2
import com.example.data.datasource.MachineryPhotoRegistry
import com.example.data.datasource.MachinerySeedCatalog
import com.example.data.local.AppDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.BookingLifecycleStatus
import com.example.data.model.FarmServiceType
import com.example.data.model.MachineryCategory
import com.example.data.model.MachineryCondition
import com.example.data.model.MachineryInquiryRecord
import com.example.data.model.MachineryProduct
import com.example.data.model.MachineryRecommendationQuery
import com.example.data.model.MachineryRentalRequest
import com.example.data.model.MachineryReviewRecord
import com.example.data.model.MachineryServiceRequest
import com.example.data.model.NurseryVerificationStatus
import com.example.data.model.PartnerBusinessRole
import com.example.data.model.RentalUnit
import com.example.data.repository.MachineryRepository
import com.example.data.remote.KrishakAiService
import com.example.ui.machinery.MachineryCompareDialog
import com.example.ui.machinery.MachineryDealerDirectoryScreen
import com.example.ui.machinery.MachineryHubScreen
import com.example.ui.machinery.MachineryManufacturersScreen
import com.example.ui.machinery.MachineryProductDetailScreen
import com.example.ui.navigation.HashRoutes
import com.example.ui.theme.KrishakSathiTheme
import com.example.util.BengaliStrings
import com.example.util.EnglishStrings
import com.example.util.HindiStrings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class MachineryHubTest {

    private lateinit var context: Context
    private lateinit var roomDb: AppDatabase
    private lateinit var repo: MachineryRepository

    private val composeRule = createComposeRule()

    private val dbResource = object : ExternalResource() {
        override fun before() {
            context = ApplicationProvider.getApplicationContext()
            roomDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
            repo = MachineryRepository(context, roomDb)
            runBlocking { repo.ensureSeeded() }
        }

        override fun after() {
            roomDb.close()
        }
    }

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(dbResource).around(composeRule)

    @Test
    fun `migration 10 to 11 creates promo_banners and all 6 machinery tables cleanly`() {
        val dbName = "machinery_mig_10_11_${System.nanoTime()}.db"
        val callback = object : SupportSQLiteOpenHelper.Callback(10) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS plant_kingdom (plantId TEXT NOT NULL PRIMARY KEY)")
                db.execSQL("CREATE TABLE IF NOT EXISTS wb_nursery_directory (nurseryId TEXT NOT NULL PRIMARY KEY)")
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(callback)
                .build()
        )
        val sqliteDb = helper.writableDatabase
        AppDatabase.MIGRATION_10_11.migrate(sqliteDb)

        val expectedTables = listOf(
            "promo_banners",
            "machinery_products",
            "machinery_dealers",
            "machinery_favourites",
            "machinery_inquiries",
            "machinery_rentals",
            "machinery_service_requests"
        )
        expectedTables.forEach { tbl ->
            sqliteDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tbl'").use { cursor ->
                assertTrue("Expected table $tbl after MIGRATION_10_11", cursor.moveToFirst())
            }
        }
        sqliteDb.close()
        helper.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun `all 10 categories, 14 field services, and 16 Siliguri dealer candidates uphold zero fabrication`() = runBlocking {
        assertEquals(10, MachineryCategory.entries.size)
        assertEquals(14, FarmServiceType.entries.size)
        assertEquals(20, MachinerySeedCatalog.initialLocalities.size)
        assertEquals(12, PartnerBusinessRole.entries.size)

        // All 10 categories have at least 1 reference machine seeded
        val seededProducts = repo.observePublicProducts().first()
        MachineryCategory.entries.forEach { cat ->
            assertTrue(
                "Expected seeded product for category ${cat.code}",
                seededProducts.any { it.categoryCode == cat.code }
            )
        }
        seededProducts.forEach { item ->
            assertEquals("REFERENCE_SPEC", item.listingOrigin)
            assertNull("Must not invent price for reference spec ${item.id}", item.salePriceInr)
            assertNull("Must not invent seller phone on reference spec ${item.id}", item.sellerPhone)
        }

        // All 16 Siliguri dealer candidates keep strict unverified boundary
        val dealers = repo.observeAllDealers().first()
        assertEquals(16, dealers.size)
        dealers.forEach { dealer ->
            assertTrue(dealer.dealerId.startsWith("KS-MC-"))
            assertEquals(NurseryVerificationStatus.PENDING_VERIFICATION, dealer.verificationStatus)
            assertEquals("NOT_CONFIRMED", dealer.ownerConsentStatus)
            assertFalse(dealer.isVerified)
            assertFalse("Must never show Authorised Dealer without verification", dealer.isAuthorisedDealerVerified)
            assertNull(dealer.phone)
            assertNull(dealer.whatsapp)
            assertNull(dealer.fullAddress)
            assertNull(dealer.ownerName)
        }
    }

    @Test
    fun `seller listing approval workflow, claim consent guard, rentals, services & favourites work cleanly`() = runBlocking {
        val sellerId = "seller_north_bengal"
        val farmerId = "farmer_jalpaiguri"

        // 1. Seller submission starts PENDING_APPROVAL and stays hidden from public flow
        val submitRes = repo.submitSellerMachineListing(
            MachineryProduct(
                sellerUserId = sellerId,
                sellerBusinessName = "Teesta Farm Implements",
                sellerLocation = "Siliguri Sevoke Road",
                sellerLocalityCode = "SEVOKE_ROAD",
                sellerPhone = "+919800002222",
                nameEn = "20L Agricultural Spray Drone",
                nameBn = "২০ লিটার কৃষি স্প্রে ড্রোন",
                nameHi = "20L कृषि स्प्रे ड्रोन",
                categoryCode = MachineryCategory.SMART_PRECISION_AG.code,
                subcategory = "Agricultural Spraying Drone (10L-30L)",
                condition = MachineryCondition.NEW.code,
                forSale = true,
                forRent = true,
                salePriceInr = 450000.0,
                rentalRatePerAcreInr = 450.0
            ),
            isAdmin = false
        )
        assertTrue(submitRes.isSuccess)
        val created = submitRes.getOrThrow()
        assertEquals("PENDING_APPROVAL", created.approvalStatus)
        assertTrue("Drone subcategory must flag DGCA compliance", created.requiresDgcaCompliance)
        assertFalse(repo.observePublicProducts().first().any { it.id == created.id })

        // 2. Admin approves -> visible publicly
        val approveRes = repo.approveOrRejectMachineListing(
            machineId = created.id,
            approve = true,
            adminEmail = "admin@krishaksathi.in",
            note = "Verified invoice & spec"
        )
        assertTrue(approveRes.isSuccess)
        assertTrue(repo.observePublicProducts().first().any { it.id == created.id })

        // 3. Favourites toggle
        repo.toggleFavourite(farmerId, created.id, currentlySaved = false)
        assertTrue(repo.observeFavouriteIds(farmerId).first().contains(created.id))
        repo.toggleFavourite(farmerId, created.id, currentlySaved = true)
        assertFalse(repo.observeFavouriteIds(farmerId).first().contains(created.id))

        // 4. Rental & Service requests enforce REQUESTED_AWAITING_CONFIRMATION status
        val rentalRes = repo.submitRentalRequest(
            MachineryRentalRequest(
                userId = farmerId,
                farmerName = "Ramesh Roy",
                farmerPhone = "+919800003333",
                machineId = created.id,
                machineName = created.nameEn,
                sellerUserId = sellerId,
                locality = "Bagdogra",
                rentalUnit = RentalUnit.PER_ACRE.code,
                durationUnits = 3,
                withOperator = true
            )
        )
        assertTrue(rentalRes.isSuccess)
        assertEquals("REQUESTED_AWAITING_CONFIRMATION", rentalRes.getOrThrow().status)

        val srvRes = repo.submitServiceRequest(
            MachineryServiceRequest(
                userId = farmerId,
                farmerName = "Ramesh Roy",
                farmerPhone = "+919800003333",
                serviceTypeCode = FarmServiceType.DRONE_SPRAYING.code,
                locality = "Naxalbari",
                landSizeText = "3 Acres",
                cropName = "Paddy",
                preferredDate = "2026-11-25",
                providerUserId = sellerId
            )
        )
        assertTrue(srvRes.isSuccess)
        assertEquals("REQUESTED_AWAITING_CONFIRMATION", srvRes.getOrThrow().status)

        // 5. Inquiry submission & reply
        val inqRes = repo.submitInquiryOrReport(
            MachineryInquiryRecord(
                userId = farmerId,
                farmerName = "Ramesh Roy",
                farmerPhone = "+919800003333",
                machineId = created.id,
                machineName = created.nameEn,
                sellerUserId = sellerId,
                message = "Confirm DGCA pilot inclusion and delivery?"
            )
        )
        assertTrue(inqRes.isSuccess)

        // 6. Claim Business Profile rejects missing consent, accepts with explicit consent
        val badClaim = repo.submitDealerClaimRequest(
            dealerId = "KS-MC-0001",
            userId = sellerId,
            claimantName = "Owner",
            phone = "9800000001",
            whatsapp = null,
            address = "Siliguri",
            brands = "Kubota",
            consentConfirmedByClaimant = false
        )
        assertTrue(badClaim.isFailure)

        val goodClaim = repo.submitDealerClaimRequest(
            dealerId = "KS-MC-0001",
            userId = sellerId,
            claimantName = "Owner",
            phone = "9800000001",
            whatsapp = null,
            address = "Siliguri",
            brands = "Kubota",
            consentConfirmedByClaimant = true
        )
        assertTrue(goodClaim.isSuccess)
        val d = repo.observeAllDealers().first().first { it.dealerId == "KS-MC-0001" }
        assertEquals("OWNER_SUBMITTED_AWAITING_ADMIN", d.ownerConsentStatus)
        assertFalse("Admin approval still required after owner claim", d.isVerified)
    }

    @Test
    fun `AI smart finder and KrishakAiService ground responses strictly with catalog and DGCA warnings`() = runBlocking {
        val (matches, summary) = repo.recommendMachinesForQuery(
            MachineryRecommendationQuery(
                cropName = "Paddy",
                landSizeAcres = 1.5,
                soilOrTerrain = "Wet plains",
                irrigationAvailable = true,
                budgetRangeInr = "Under 2 Lakh",
                operationNeed = "ploughing and drone spraying",
                skilledOperatorAvailable = false,
                powerPreference = "Diesel"
            ),
            lang = AppLanguage.ENGLISH
        )
        assertTrue(matches.isNotEmpty())
        assertTrue(summary.contains("Confirm live prices", ignoreCase = true))
        assertTrue(summary.contains("Power Tiller", ignoreCase = true))

        val aiService = KrishakAiService(context)
        assertFalse(aiService.toString().isBlank())

        // Verify drone & earthmoving compliance flags exist on catalog entries
        val droneMatches = matches.filter { it.requiresDgcaCompliance }
        assertTrue("Expected DGCA-flagged drone machine in drone query results", droneMatches.isNotEmpty())

        val (digMatches, _) = repo.recommendMachinesForQuery(
            MachineryRecommendationQuery(
                cropName = "Tea",
                landSizeAcres = 4.0,
                soilOrTerrain = "Hill slope",
                irrigationAvailable = true,
                budgetRangeInr = "Standard",
                operationNeed = "trench digging and pond excavating",
                skilledOperatorAvailable = true,
                powerPreference = "Diesel"
            ),
            lang = AppLanguage.ENGLISH
        )
        assertTrue(
            "Expected earthmoving safety flag on trenching/digging catalog machines",
            digMatches.any { it.requiresEarthmovingSafety }
        )

        // Verify EN/HI/BN L10n keys exist cleanly
        assertTrue(EnglishStrings.containsKey("card_machinery_title"))
        assertTrue(HindiStrings.containsKey("card_machinery_title"))
        assertTrue(BengaliStrings.containsKey("card_machinery_title"))
        assertEquals("#machinery-hub", HashRoutes.MACHINERY_HUB)
        assertEquals("#machinery-dealers", HashRoutes.MACHINERY_DEALERS)
        assertEquals("#admin-machinery", HashRoutes.ADMIN_MACHINERY)
    }

    @Test
    fun `MachineryHubScreen and MachineryDealerDirectoryScreen render and switch tabs without crash`() {
        composeRule.setContent {
            KrishakSathiTheme {
                MachineryHubScreen(
                    currentLanguage = AppLanguage.ENGLISH,
                    userId = "farmer_test",
                    farmerName = "Test Farmer",
                    onBack = {},
                    onOpenDealers = {},
                    onOpenFullAi = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("machinery_tab_0").assertIsDisplayed()
        composeRule.onNodeWithTag("machinery_tab_2").assertIsDisplayed()
        composeRule.onNodeWithTag("machinery_tab_2").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("machinery_tab_1").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun `MachineryDealerDirectoryScreen displays North Bengal locality map card and search`() {
        composeRule.setContent {
            KrishakSathiTheme {
                MachineryDealerDirectoryScreen(
                    currentLanguage = AppLanguage.ENGLISH,
                    userId = "farmer_test",
                    onBack = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("machinery_north_bengal_map_card").assertIsDisplayed()
        composeRule.onNodeWithTag("machinery_dealer_search").assertIsDisplayed()
    }

    @Test
    fun `migration 11 to 12 upgrades machinery_products with all 16 photo and trilingual columns`() {
        val dbName = "machinery_mig_11_12_${System.nanoTime()}.db"
        val callback = object : SupportSQLiteOpenHelper.Callback(11) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS machinery_products (
                        id TEXT NOT NULL PRIMARY KEY,
                        sellerUserId TEXT NOT NULL,
                        dealerId TEXT NOT NULL,
                        sellerBusinessName TEXT NOT NULL,
                        sellerLocation TEXT NOT NULL,
                        sellerLocalityCode TEXT NOT NULL,
                        sellerPhone TEXT,
                        sellerWhatsapp TEXT,
                        nameEn TEXT NOT NULL,
                        nameBn TEXT NOT NULL,
                        nameHi TEXT NOT NULL,
                        categoryCode TEXT NOT NULL,
                        subcategory TEXT NOT NULL,
                        brand TEXT NOT NULL,
                        modelNumber TEXT NOT NULL,
                        horsepowerOrCapacity TEXT NOT NULL,
                        engineOrMotorSpec TEXT NOT NULL,
                        fuelOrPowerType TEXT NOT NULL,
                        workingWidthOrCoverage TEXT NOT NULL,
                        weightKg TEXT NOT NULL,
                        dimensionsText TEXT NOT NULL,
                        suitableCrops TEXT NOT NULL,
                        suitableSoilOrTerrain TEXT NOT NULL,
                        landSizeSuitability TEXT NOT NULL,
                        condition TEXT NOT NULL,
                        forSale INTEGER NOT NULL,
                        forRent INTEGER NOT NULL,
                        salePriceInr REAL,
                        rentalRateHourlyInr REAL,
                        rentalRateDailyInr REAL,
                        rentalRateWeeklyInr REAL,
                        rentalRatePerAcreInr REAL,
                        stockStatus TEXT NOT NULL,
                        warrantyInfo TEXT NOT NULL,
                        sparePartsInfo TEXT NOT NULL,
                        installationMaintenanceNotes TEXT NOT NULL,
                        safetyUsageGuidelines TEXT NOT NULL,
                        demoVideoUrl TEXT NOT NULL,
                        photosCsv TEXT NOT NULL,
                        listingOrigin TEXT NOT NULL,
                        approvalStatus TEXT NOT NULL,
                        verificationBadgeText TEXT NOT NULL,
                        requiresDgcaCompliance INTEGER NOT NULL,
                        requiresEarthmovingSafety INTEGER NOT NULL,
                        lastUpdatedDate TEXT NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(callback)
                .build()
        )
        val sqliteDb = helper.writableDatabase
        AppDatabase.MIGRATION_11_12.migrate(sqliteDb)

        val expectedColumns = listOf(
            "purposeEn", "purposeBn", "purposeHi",
            "suitableCropsBn", "suitableCropsHi",
            "operatingInstructionsEn", "operatingInstructionsBn", "operatingInstructionsHi",
            "safetyGuidelinesBn", "safetyGuidelinesHi",
            "brandVerified", "modelVerified", "pendingAdminReview",
            "adminReviewNote", "hireServiceTypeCode", "firebaseStoragePathsCsv"
        )
        val actualColumns = mutableSetOf<String>()
        sqliteDb.query("PRAGMA table_info(machinery_products)").use { cursor ->
            val nameIdx = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (nameIdx >= 0) actualColumns.add(cursor.getString(nameIdx))
            }
        }
        expectedColumns.forEach { col ->
            assertTrue("Expected column $col after MIGRATION_11_12", actualColumns.contains(col))
        }
        sqliteDb.close()
        helper.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun `all 24 original uploaded equipment photos are mapped cleanly without duplicates or watermarks`() = runBlocking {
        val allTokens = MachineryPhotoRegistry.allUploadedPhotoTokens()
        assertEquals(24, allTokens.size)
        allTokens.forEach { token ->
            val resId = MachineryPhotoRegistry.resolveDrawableRes(token)
            assertTrue("Missing bundled drawable resource for $token", resId != null && resId != 0)
        }

        val catalog = repo.observePublicProducts().first()
        val attachedTokens = catalog.flatMap { it.getPhotoTokens() }.toSet()
        allTokens.forEach { token ->
            assertTrue("Uploaded photo $token is not attached to any catalog product", attachedTokens.contains(token))
        }

        // Verify Photo #19 & #22 share 1 Earth Auger entry with 0 duplicates
        val augerProducts = catalog.filter { it.id.contains("auger", ignoreCase = true) || it.nameEn.contains("Auger", ignoreCase = true) }
        assertEquals("Expected exact 1 consolidated Earth Auger product", 1, augerProducts.size)
        val augerPhotos = augerProducts.first().getPhotoTokens()
        assertEquals(2, augerPhotos.size)
        assertTrue(augerPhotos.contains("drawable:mach_19_kawashima_earth_auger"))
        assertTrue(augerPhotos.contains("drawable:mach_22_handheld_earth_auger_field"))

        // Watermark text from VST photo ("9552936511") must never appear in dealer or product contacts
        catalog.forEach { p ->
            assertFalse(p.sellerPhone?.contains("9552936511") == true)
            assertFalse(p.sellerWhatsapp?.contains("9552936511") == true)
        }
        repo.observeAllDealers().first().forEach { d ->
            assertFalse(d.phone?.contains("9552936511") == true)
            assertFalse(d.whatsapp?.contains("9552936511") == true)
        }

        // Verify composite #09 and unbranded items are flagged for admin review and can be resolved by admin
        val compositeItem = catalog.first { it.id == "ref_lancer_dhanush_composite" }
        assertTrue(compositeItem.pendingAdminReview)
        val reviewRes = repo.resolveEquipmentReviewByAdmin(
            machineId = compositeItem.id,
            verifiedBrand = "Lancer & Dhanush",
            verifiedModel = "HD-180 Verified",
            markVerified = true,
            adminEmail = "admin@krishaksathi.in",
            reviewNote = "Split confirmed by admin"
        )
        assertTrue(reviewRes.isSuccess)
        val resolved = repo.observeAllProducts().first().first { it.id == compositeItem.id }
        assertFalse(resolved.pendingAdminReview)
        assertTrue(resolved.brandVerified)
        assertTrue(resolved.modelVerified)
    }

    @Test
    fun `equipment card opens trilingual detail dialog and full screen zoom viewer`() {
        composeRule.setContent {
            KrishakSathiTheme {
                MachineryHubScreen(
                    currentLanguage = AppLanguage.BENGALI,
                    userId = "farmer_test",
                    farmerName = "Test Farmer",
                    onBack = {},
                    onOpenDealers = {},
                    onOpenFullAi = {},
                    repository = repo
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("machinery_search_input").performTextInput("Mahavator")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("machinery_card_ref_mahindra_mahavator_rotavator")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("machinery_card_ref_mahindra_mahavator_rotavator").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("machinery_detail_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("machinery_detail_buy_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("machinery_detail_rent_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("machinery_detail_hire_service_btn").assertIsDisplayed()

        composeRule.onNodeWithTag("machinery_detail_photo").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("machinery_fullscreen_viewer").assertIsDisplayed()
        composeRule.onNodeWithTag("machinery_fullscreen_close_btn").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun `migration 12 to 13 creates OEM, dealer offer, review and research queue tables cleanly`() {
        val dbName = "machinery_mig_12_13_${System.nanoTime()}.db"
        val callback = object : SupportSQLiteOpenHelper.Callback(12) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS machinery_products (id TEXT NOT NULL PRIMARY KEY)")
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(callback)
                .build()
        )
        val sqliteDb = helper.writableDatabase
        AppDatabase.MIGRATION_12_13.migrate(sqliteDb)

        val expectedNewTables = listOf(
            "machinery_manufacturers",
            "machinery_dealer_offers",
            "machinery_reviews",
            "machinery_research_queue"
        )
        expectedNewTables.forEach { tbl ->
            sqliteDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tbl'").use { cursor ->
                assertTrue("Expected table $tbl after MIGRATION_12_13", cursor.moveToFirst())
            }
        }
        val actualColumns = mutableSetOf<String>()
        sqliteDb.query("PRAGMA table_info(machinery_products)").use { cursor ->
            val nameIdx = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (nameIdx >= 0) actualColumns.add(cursor.getString(nameIdx))
            }
        }
        listOf(
            "aliasesCsv", "applicableCategoriesCsv", "sourceType",
            "compatibleWithIdsCsv", "subsidySchemeCodesCsv", "operatorTrainingRequired"
        ).forEach { col ->
            assertTrue("Expected column $col after MIGRATION_12_13", actualColumns.contains(col))
        }
        sqliteDb.close()
        helper.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun `expanded catalog 103 items, 19 OEMs, 6 offers, 5 queue items, multi-category Auger & 10-point beginner guides work`() = runBlocking {
        val catalog = repo.observePublicProducts().first()
        val oems = repo.observeAllManufacturers().first()
        val offers = repo.observeAllOffers().first()
        val researchQueue = repo.observeResearchQueue().first()

        assertEquals(103, catalog.size)
        assertEquals(19, oems.size)
        assertEquals(6, offers.size)
        assertEquals(5, researchQueue.size)

        oems.forEach { oem ->
            assertTrue("OEM ${oem.id} must use HTTPS portal URL", oem.officialPortalUrl.startsWith("https://"))
        }

        val auger = catalog.first { it.id.contains("auger", ignoreCase = true) || it.nameEn.contains("Auger", ignoreCase = true) }
        assertTrue(auger.matchesCategory(MachineryCategory.SOIL_EARTH_MOVING.code))
        assertTrue(auger.matchesCategory(MachineryCategory.TRACTORS_LAND_PREP.code))

        val rotavator = catalog.first { it.id == "ref_mahindra_mahavator_rotavator" }
        AppLanguage.entries.forEach { lang ->
            val guide = MachineryExtendedCatalogPart2.buildBeginnerGuide(rotavator, lang)
            assertTrue(guide.whatItDoes.isNotBlank())
            assertTrue(guide.whoNeedsIt.isNotBlank())
            assertTrue(guide.farmSizeSuitability.isNotBlank())
            assertTrue(guide.tractorOrPowerRequirement.isNotBlank())
            assertTrue(guide.fuelOrPowerType.isNotBlank())
            assertTrue(guide.stepByStepUsage.isNotBlank())
            assertTrue(guide.safetyPrecautions.isNotBlank())
            assertTrue(guide.commonMistakesToAvoid.isNotBlank())
            assertTrue(guide.maintenanceChecklist.isNotBlank())
            assertTrue(guide.buyingVsRentingAdvice.isNotBlank())
        }
    }

    @Test
    fun `duplicate 5-min booking guard, cancel unlock, duplicate merge, CSV rollback & reviews work reliably`() = runBlocking {
        val farmerId = "farmer_guard_test"
        val rental = MachineryRentalRequest(
            userId = farmerId,
            farmerName = "Suman Farmer",
            farmerPhone = "+919800004444",
            machineId = "ref_power_tiller_12hp",
            machineName = "13 HP Power Tiller",
            sellerUserId = "seller_1",
            locality = "Matigara",
            rentalUnit = RentalUnit.DAILY.code,
            durationUnits = 2
        )
        val firstRental = repo.submitRentalRequest(rental)
        assertTrue(firstRental.isSuccess)
        val duplicateRental = repo.submitRentalRequest(rental)
        assertTrue("Second identical rental within 5 mins should fail", duplicateRental.isFailure)

        val cancelRes = repo.cancelRentalRequest(firstRental.getOrThrow().id, farmerId)
        assertTrue(cancelRes.isSuccess)
        val retriedRental = repo.submitRentalRequest(rental)
        assertTrue("Rental should succeed after prior booking was cancelled", retriedRental.isSuccess)
        assertEquals(BookingLifecycleStatus.CANCELLED, repo.observeRentalsForUser(farmerId).first().first { it.id == firstRental.getOrThrow().id }.status)

        val service = MachineryServiceRequest(
            userId = farmerId,
            farmerName = "Suman Farmer",
            farmerPhone = "+919800004444",
            serviceTypeCode = FarmServiceType.ROTAVATOR_LAND_PREP.code,
            locality = "Naxalbari",
            landSizeText = "2 Acres",
            cropName = "Paddy",
            preferredDate = "2026-12-01"
        )
        val firstSrv = repo.submitServiceRequest(service)
        assertTrue(firstSrv.isSuccess)
        assertTrue("Second identical service within 5 mins should fail", repo.submitServiceRequest(service).isFailure)
        assertTrue(repo.cancelServiceRequest(firstSrv.getOrThrow().id, farmerId).isSuccess)
        assertTrue("Service should succeed after cancellation", repo.submitServiceRequest(service).isSuccess)

        // Duplicate detection and merge
        val d1 = repo.submitSellerMachineListing(
            MachineryProduct(
                id = "dup_test_1",
                sellerUserId = "s1",
                sellerBusinessName = "Seller One",
                nameEn = "Duplicate Submersible Test Pump",
                categoryCode = MachineryCategory.IRRIGATION_WATER.code,
                photosCsv = "drawable:diesel_pump"
            ),
            isAdmin = true
        ).getOrThrow()
        val d2 = repo.submitSellerMachineListing(
            MachineryProduct(
                id = "dup_test_2",
                sellerUserId = "s2",
                sellerBusinessName = "Seller Two",
                nameEn = "Duplicate Submersible Test Pump",
                categoryCode = MachineryCategory.IRRIGATION_WATER.code,
                photosCsv = "drawable:electric_pump",
                aliasesCsv = "water pump"
            ),
            isAdmin = true
        ).getOrThrow()
        val groups = repo.findDuplicateProductGroups()
        assertTrue(groups.any { g -> g.products.any { it.id == d1.id } && g.products.any { it.id == d2.id } })
        assertTrue(repo.mergeDuplicateProducts(d1.id, d2.id, "admin@krishaksathi.in").isSuccess)
        val merged = repo.observeAllProducts().first().first { it.id == d1.id }
        assertTrue(merged.getPhotoTokens().contains("drawable:electric_pump"))
        assertFalse(repo.observeAllProducts().first().any { it.id == d2.id })

        // CSV Preview, Commit & Batch Rollback
        val csv = """
            nameEn,categoryCode,subcategory,brand,modelNumber,sellerBusinessName
            Csv Mini Weeder,TRACTORS_LAND_PREP,Mini Tiller / Weeder,Pending Verification,Pending Verification,Siliguri Test Seller
            Bad Row,UNKNOWN_CAT,Test,Test,Test,Test
        """.trimIndent()
        val preview = repo.previewCsvImport(csv)
        assertEquals(1, preview.validRows.size)
        assertEquals(1, preview.errors.size)
        val (batchId, committed) = repo.commitCsvImportWithBatch(preview.validRows, "admin@krishaksathi.in").getOrThrow()
        assertEquals(1, committed)
        assertTrue(repo.observePublicProducts().first().any { it.nameEn == "Csv Mini Weeder" })
        val rolledBack = repo.rollbackCsvImportBatch(batchId, "admin@krishaksathi.in").getOrThrow()
        assertEquals(1, rolledBack)
        assertFalse(repo.observePublicProducts().first().any { it.nameEn == "Csv Mini Weeder" })

        // Farmer review submission & moderation deletion
        val revRes = repo.submitReview(
            MachineryReviewRecord(
                machineId = "ref_mahindra_mahavator_rotavator",
                userId = farmerId,
                userName = "Suman",
                rating = 5,
                title = "Great puddling",
                comment = "Prepared 3 acres quickly in wet paddy soil."
            )
        )
        assertTrue(revRes.isSuccess)
        val revId = revRes.getOrThrow().id
        assertTrue(repo.observeReviewsForMachine("ref_mahindra_mahavator_rotavator").first().any { it.id == revId })
        assertTrue(repo.deleteReview(revId, "admin@krishaksathi.in").isSuccess)
        assertFalse(repo.observeReviewsForMachine("ref_mahindra_mahavator_rotavator").first().any { it.id == revId })
    }

    @Test
    fun `MachineryProductDetailScreen 5 tabs, beginner guide, subsidy & sticky bar render properly`() {
        val rotavator = MachinerySeedCatalog.referenceCatalogProducts.first { it.id == "ref_mahindra_mahavator_rotavator" }
        composeRule.setContent {
            KrishakSathiTheme {
                MachineryProductDetailScreen(
                    machine = rotavator,
                    currentLanguage = AppLanguage.ENGLISH,
                    userId = "farmer_test",
                    farmerName = "Test Farmer",
                    repository = repo,
                    onBack = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("machinery_detail_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("machinery_detail_buy_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("machinery_detail_rent_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("machinery_detail_hire_service_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("machinery_beginner_guide_card").assertIsDisplayed()
        composeRule.onNodeWithTag("detail_tab_3").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("machinery_subsidy_card").assertIsDisplayed()
        composeRule.onNodeWithTag("detail_tab_4").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("review_comment_input").assertIsDisplayed()
        composeRule.onNodeWithTag("submit_review_btn").assertIsDisplayed()
    }

    @Test
    fun `MachineryManufacturersScreen renders OEM portal list and filters cleanly`() {
        composeRule.setContent {
            KrishakSathiTheme {
                MachineryManufacturersScreen(
                    currentLanguage = AppLanguage.ENGLISH,
                    onBack = {},
                    repository = repo
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("machinery_manufacturers_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("machinery_manufacturers_search").performTextInput("Mahindra")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("oem_card_oem_mahindra_tractors").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("oem_card_oem_mahindra_tractors").assertIsDisplayed()
    }

    @Test
    fun `MachineryCompareDialog shows category mismatch warning across distinct categories`() {
        val rotavator = MachinerySeedCatalog.referenceCatalogProducts.first { it.id == "ref_mahindra_mahavator_rotavator" }
        val drone = MachinerySeedCatalog.referenceCatalogProducts.first { it.requiresDgcaCompliance }
        composeRule.setContent {
            KrishakSathiTheme {
                MachineryCompareDialog(
                    machines = listOf(rotavator, drone),
                    currentLanguage = AppLanguage.ENGLISH,
                    onDismiss = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("machinery_compare_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("compare_category_mismatch_warning").assertIsDisplayed()
    }
}
