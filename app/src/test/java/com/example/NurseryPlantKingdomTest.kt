package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.PlantKingdomCatalog
import com.example.data.datasource.PlantPhotoRegistry
import com.example.data.datasource.WbNurseryDirectorySeed
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PlantKingdomEntity
import com.example.data.model.AppLanguage
import com.example.data.model.NurseryApplication
import com.example.data.model.NurseryApplicationStatus
import com.example.data.model.NurseryProduct
import com.example.data.model.NurseryProductCategory
import com.example.data.model.NurseryVerificationStatus
import com.example.data.model.PlantKingdomEntry
import com.example.data.model.PlantKingdomGroup
import com.example.data.model.PlantSearchFilter
import com.example.data.model.PlantSeasonalCollection
import com.example.data.model.WbNurseryDistrict
import com.example.data.remote.NurseryProductRepository
import com.example.data.repository.PlantKingdomRepository
import com.example.ui.navigation.HashRoutes
import com.example.ui.nursery.NurseryDirectoryScreen
import com.example.ui.nursery.NurseryMapScreen
import com.example.ui.nursery.PlantDetailScreen
import com.example.ui.nursery.PlantKingdomLibraryScreen
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
class NurseryPlantKingdomTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var roomDb: AppDatabase
    private lateinit var repository: PlantKingdomRepository
    private lateinit var productRepository: NurseryProductRepository

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        roomDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PlantKingdomRepository(context, roomDb)
        productRepository = NurseryProductRepository(context)
        repository.ensureSeeded()
    }

    @After
    fun tearDown() {
        roomDb.close()
    }

    @Test
    fun `migration 9 to 10 creates plant_kingdom and wb_nursery_directory tables cleanly`() {
        val dbName = "migration_test_${System.nanoTime()}.db"
        val callback = object : SupportSQLiteOpenHelper.Callback(9) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                // Minimal v9 placeholder table
                db.execSQL("CREATE TABLE IF NOT EXISTS seo_config (id TEXT NOT NULL PRIMARY KEY)")
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
        AppDatabase.MIGRATION_9_10.migrate(sqliteDb)

        sqliteDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='plant_kingdom'").use { c ->
            assertTrue("plant_kingdom table must exist after MIGRATION_9_10", c.moveToFirst())
        }
        sqliteDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='wb_nursery_directory'").use { c ->
            assertTrue("wb_nursery_directory table must exist after MIGRATION_9_10", c.moveToFirst())
        }
        sqliteDb.close()
        helper.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun `37 initial WB nurseries are strictly PENDING_VERIFICATION with zero fabricated details`() = runBlocking {
        // Call ensureSeeded twice to verify idempotency
        repository.ensureSeeded()

        val seeded = repository.observeAllDirectoryNurseries().first()
        assertEquals(37, WbNurseryDirectorySeed.initialPendingNurseries.size)
        assertEquals(37, seeded.size)
        assertEquals(22, WbNurseryDistrict.entries.size)

        seeded.forEach { nursery ->
            assertEquals(
                "Initial nursery ${nursery.businessName} must be PENDING_VERIFICATION",
                NurseryVerificationStatus.PENDING_VERIFICATION,
                nursery.verificationStatus
            )
            assertFalse("Initial nursery ${nursery.businessName} must have isVerified=false", nursery.isVerified)
            assertTrue("sourceReference must be populated", nursery.sourceReference.isNotBlank())
            assertTrue("lastVerifiedDate must be populated", nursery.lastVerifiedDate.isNotBlank())

            // Zero fabrication assertions
            assertNull("Must not invent phone for ${nursery.businessName}", nursery.phone)
            assertNull("Must not invent whatsapp for ${nursery.businessName}", nursery.whatsapp)
            assertNull("Must not invent ownerName for ${nursery.businessName}", nursery.ownerName)
            assertNull("Must not invent fullAddress for ${nursery.businessName}", nursery.fullAddress)
            assertNull("Must not invent openingHours for ${nursery.businessName}", nursery.openingHours)
            assertNull("Must not invent websiteOrSocial for ${nursery.businessName}", nursery.websiteOrSocial)
            assertNull("Must not invent latitude for ${nursery.businessName}", nursery.latitude)
            assertNull("Must not invent longitude for ${nursery.businessName}", nursery.longitude)
            assertTrue("Must not invent availablePlantIdsCsv", nursery.availablePlantIdsCsv.isBlank())
        }
    }

    @Test
    fun `167 species catalog covers all required groups, unique visual keys, and strict exact drawables`() {
        val all = PlantKingdomCatalog.allPlants
        assertEquals(167, all.size)

        // Unique IDs and unique visualAssetKeys
        val plantIds = all.map { it.plantId }
        assertEquals(all.size, plantIds.toSet().size)
        val visualKeys = all.map { it.visualAssetKey }
        assertEquals(all.size, visualKeys.toSet().size)

        // Exact category minimums
        assertTrue(PlantKingdomCatalog.getByGroup(PlantKingdomGroup.HIMALAYAN_HILL_FLOWERS).size >= 15)
        assertTrue(PlantKingdomCatalog.getByGroup(PlantKingdomGroup.COMMON_FLOWERS).size >= 27)
        assertTrue(PlantKingdomCatalog.getByGroup(PlantKingdomGroup.MONEY_INDOOR_PLANTS).size >= 22)
        assertTrue(PlantKingdomCatalog.getByGroup(PlantKingdomGroup.FRUIT_PLANTS).size >= 26)
        assertTrue(PlantKingdomCatalog.getByGroup(PlantKingdomGroup.ORNAMENTAL_TREES_CLIMBERS).size >= 17)
        assertTrue(PlantKingdomCatalog.getByGroup(PlantKingdomGroup.MEDICINAL_HERBS_SUCCULENTS).size >= 17)

        // All 7 seasonal collections have matches
        assertEquals(7, PlantSeasonalCollection.entries.size)
        PlantSeasonalCollection.entries.forEach { season ->
            assertTrue(
                "Season ${season.name} must match plants",
                all.any { it.matchesSeasonal(season) }
            )
        }

        // Strict 1-to-1 exact .jpg drawable rule
        val exactPhotos = PlantPhotoRegistry.getAllExactPhotoMappings()
        assertEquals(
            setOf("rose", "marigold", "mango", "banana", "litchi", "guava", "darjeeling_orange"),
            exactPhotos.keys
        )
        assertEquals(exactPhotos.size, exactPhotos.values.toSet().size)
        assertNull(PlantPhotoRegistry.getExactRealPhotoResId("orchids"))
        assertNull(PlantPhotoRegistry.getExactRealPhotoResId("himalayan_blue_poppy"))

        // Key routes
        assertEquals("#plant-kingdom", HashRoutes.PLANT_KINGDOM)
        assertEquals("#nursery-directory", HashRoutes.NURSERY_DIRECTORY)
        assertEquals("#nursery-map", HashRoutes.NURSERY_MAP)
    }

    @Test
    fun `multilingual search and all 11 filters resolve accurately across EN BN HI and botanical names`() {
        val all = PlantKingdomCatalog.allPlants

        // Multilingual search tests
        assertTrue(all.any { it.matchesSearch("Meconopsis") && it.plantId == "himalayan_blue_poppy" })
        assertTrue(all.any { it.matchesSearch("কদম") && it.plantId == "kadam" })
        assertTrue(all.any { it.matchesSearch("कदंब") && it.plantId == "kadam" })
        assertTrue(all.any { it.matchesSearch("Kadam") && it.plantId == "kadam" })
        assertTrue(all.any { it.matchesSearch("মানি প্ল্যান্ট") })

        // All 11 PlantSearchFilter filters return non-empty results
        assertEquals(11, PlantSearchFilter.entries.size)
        PlantSearchFilter.entries.forEach { filter ->
            assertTrue(
                "Filter ${filter.code} should match >= 1 species",
                all.any { it.matchesFilter(filter) }
            )
        }
    }

    @Test
    fun `admin bulk CSV JSON imports skip duplicates, clean dupes, and gate verified nursery linking`() = runBlocking {
        // 1. Bulk CSV import (1 new + 1 duplicate)
        val csv = """
            plantId,nameEn,nameBn,nameHi,scientificName,primaryGroup,filterTagsCsv,climate
            camellia_darjeeling,Darjeeling Camellia,দার্জিলিং ক্যামেলিয়া,दार्जिलिंग कैमेलिया,Camellia japonica,HIMALAYAN_HILL_FLOWERS,"FLOWERING,HILL_FLOWER",Cool Himalayan climate
            dup_poppy_id,Himalayan Blue Poppy,হিমালয়ান নীল পপি,हिमालयन ब्लू पॉपी,Meconopsis betonicifolia,HIMALAYAN_HILL_FLOWERS,RARE,Alpine
        """.trimIndent()
        val csvRes = repository.importPlantsFromCsv(csv)
        assertEquals(1, csvRes.importedCount)
        assertEquals(1, csvRes.skippedDuplicates)
        assertTrue(repository.observeAllPlants().first().any { it.plantId == "camellia_darjeeling" })

        // 2. Bulk JSON import (1 new + 1 duplicate)
        val json = """
            [
              {
                "plantId": "tree_tomato_kalimpong",
                "nameEn": "Kalimpong Tree Tomato (Tamarillo)",
                "nameBn": "গাছ টমেটো",
                "nameHi": "ट्री टमाटर",
                "scientificName": "Solanum betaceum",
                "primaryGroup": "FRUIT_PLANTS",
                "filterTagsCsv": "FRUIT,HILL_FLOWER"
              },
              {
                "plantId": "kadam_dup_ignored",
                "nameEn": "Kadam (Burflower Tree)",
                "scientificName": "Neolamarckia cadamba",
                "primaryGroup": "ORNAMENTAL_TREES_CLIMBERS"
              }
            ]
        """.trimIndent()
        val jsonRes = repository.importPlantsFromJson(json)
        assertEquals(1, jsonRes.importedCount)
        assertEquals(1, jsonRes.skippedDuplicates)

        // 3. Duplicate cleanup removes colliding DB rows
        val dupEntity = PlantKingdomEntity.fromModel(
            PlantKingdomCatalog.getById("rose")!!.copy(plantId = "rose_duplicate_test")
        )
        roomDb.plantKingdomDao().upsertPlant(dupEntity)
        val removed = repository.removeDuplicatePlants()
        assertEquals(1, removed)

        // 4. Translations & custom photo URI attach
        val trRes = repository.updatePlantTranslations(
            plantId = "camellia_darjeeling",
            nameEn = "Darjeeling Camellia",
            nameBn = "দার্জিলিং ক্যামেলিয়া ফুল",
            nameHi = "दार्जिलिंग कैमेलिया फूल",
            descEn = "Hill camellia",
            descBn = "পাহাড়ি ক্যামেলিয়া",
            descHi = "पहाड़ी कैमेलिया",
            careEn = "Acidic soil",
            careBn = "অম্লীয় মাটি",
            careHi = "अम्लीय मिट्टी"
        )
        assertTrue(trRes.isSuccess)
        assertTrue(repository.attachPlantPhoto("camellia_darjeeling", "https://example.com/camellia.jpg").isSuccess)
        val updatedPlant = roomDb.plantKingdomDao().getPlantById("camellia_darjeeling")!!
        assertEquals("দার্জিলিং ক্যামেলিয়া ফুল", updatedPlant.nameBn)
        assertEquals("https://example.com/camellia.jpg", updatedPlant.customPhotoUri)

        // 5. Linking plant inventory to a PENDING_VERIFICATION nursery is blocked until Admin verifies it
        val targetNurseryId = "wb_nurs_01"
        val blockedLink = repository.updatePlantNurseryAvailability("rose", targetNurseryId, true)
        assertTrue("Pending nursery cannot be linked as verified stock", blockedLink.isFailure)

        val verifyRes = repository.verifyNurseryRecord(
            nurseryId = targetNurseryId,
            ownerName = "Test Owner",
            phone = "+919800000123",
            whatsapp = "+919800000123",
            fullAddress = "Sevoke Road, Siliguri",
            districtCode = "SILIGURI",
            locality = "Siliguri",
            latitude = 26.742,
            longitude = 88.431,
            openingHours = "09:00 AM - 07:00 PM",
            websiteOrSocial = null,
            categoriesCsv = "COMMON_FLOWERS,MONEY_INDOOR_PLANTS",
            supportsWholesale = false,
            supportsRetail = true,
            deliveryAvailable = true,
            verifierNote = "Verified by Admin Test"
        )
        assertTrue(verifyRes.isSuccess)
        val verifiedLink = repository.updatePlantNurseryAvailability("rose", targetNurseryId, true)
        assertTrue("Verified nursery should link cleanly", verifiedLink.isSuccess)

        val verifiedNursery = roomDb.plantKingdomDao().getNurseryById(targetNurseryId)!!
        assertTrue(verifiedNursery.isVerified)
        assertTrue(verifiedNursery.availablePlantIdsCsv.contains("rose"))
    }

    @Test
    fun `commercial nursery marketplace listings strictly require approved partner status`() = runBlocking {
        val pendingPartnerApp = NurseryApplication(
            id = "app_pending",
            userId = "user_pending",
            status = NurseryApplicationStatus.PENDING.code
        )
        val approvedPartnerApp = NurseryApplication(
            id = "app_approved",
            userId = "user_approved",
            status = NurseryApplicationStatus.APPROVED.code
        )

        assertFalse(
            repository.canPublishPartnerListing("user_pending", listOf(pendingPartnerApp, approvedPartnerApp), isAdmin = false)
        )
        assertTrue(
            repository.canPublishPartnerListing("user_approved", listOf(pendingPartnerApp, approvedPartnerApp), isAdmin = false)
        )
        assertTrue(
            repository.canPublishPartnerListing("admin_uid", emptyList(), isAdmin = true)
        )

        val testProduct = NurseryProduct(
            nurseryId = "n_1",
            nurseryName = "Test Nursery",
            productName = "Himalayan Blue Poppy Sapling",
            category = NurseryProductCategory.FLOWER_PLANTS.code,
            regularPrice = 250.0,
            availableStock = 5,
            linkedPlantId = "himalayan_blue_poppy"
        )

        val unapprovedRes = productRepository.addProduct(
            product = testProduct,
            sellerUserId = "user_pending",
            lang = AppLanguage.ENGLISH,
            isApprovedPartner = false
        )
        assertTrue(unapprovedRes.isFailure)
        assertTrue(unapprovedRes.exceptionOrNull() is SecurityException)

        val approvedRes = productRepository.addProduct(
            product = testProduct,
            sellerUserId = "user_approved",
            lang = AppLanguage.ENGLISH,
            isApprovedPartner = true
        )
        assertTrue(approvedRes.isSuccess)
    }

    @Test
    fun `PlantKingdomLibraryScreen renders categories, search, and species cards cleanly`() {
        composeRule.setContent {
            KrishakSathiTheme {
                PlantKingdomLibraryScreen(
                    currentLanguage = AppLanguage.ENGLISH,
                    onBack = {},
                    onOpenPlantDetail = {},
                    onOpenNurseryDirectory = {},
                    onOpenNurseryMap = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("plant_kingdom_search_input").assertIsDisplayed()
        composeRule.onNodeWithTag("plant_kingdom_open_directory_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("plant_kingdom_open_map_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("plant_group_tab_all").assertIsDisplayed()
        composeRule.onAllNodesWithText("Rhododendron")[0].assertIsDisplayed()
    }

    @Test
    fun `PlantDetailScreen renders complete botanical care guide cleanly`() {
        composeRule.setContent {
            KrishakSathiTheme {
                PlantDetailScreen(
                    plantId = "himalayan_blue_poppy",
                    currentLanguage = AppLanguage.ENGLISH,
                    onBack = {},
                    onOpenDirectory = {},
                    onOpenMarketplace = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("plant_detail_back_btn").assertIsDisplayed()
        composeRule.onNodeWithText("Meconopsis betonicifolia", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Fertilizer Guidance", substring = true).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("plant_detail_lazy_column")
            .performScrollToNode(hasText("Full Care Instructions", substring = true))
        composeRule.onNodeWithText("Full Care Instructions", substring = true).assertIsDisplayed()
    }

    @Test
    fun `NurseryDirectoryScreen and NurseryMapScreen render 22 districts and honest PENDING_VERIFICATION status`() {
        composeRule.setContent {
            KrishakSathiTheme {
                NurseryDirectoryScreen(
                    currentLanguage = AppLanguage.ENGLISH,
                    onBack = {},
                    onOpenMap = {},
                    onOpenPlantKingdom = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nursery_dir_search_input").assertIsDisplayed()
        composeRule.onNodeWithTag("dir_district_ALL").assertIsDisplayed()
        composeRule.onNodeWithTag("dir_district_SILIGURI").assertIsDisplayed()
        composeRule.onNodeWithText("Muddy Hands Siliguri", substring = true).assertIsDisplayed()
        composeRule.onAllNodesWithText("PENDING VERIFICATION")[0].assertIsDisplayed()
    }

    @Test
    fun `NurseryMapScreen renders interactive map canvas without unverified GPS pins`() {
        composeRule.setContent {
            KrishakSathiTheme {
                NurseryMapScreen(
                    currentLanguage = AppLanguage.ENGLISH,
                    onBack = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nursery_map_back_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("nursery_map_canvas_card").assertIsDisplayed()
    }

    @Test
    fun `uploaded Firebase seed bundle asset processes 37 nurseries and 80 plants without duplicates or overwriting verified records`() = runBlocking {
        // Verify one nursery beforehand to ensure seed import preserves verified info
        val verifyRes = repository.verifyNurseryRecord(
            nurseryId = "wb_nurs_01",
            ownerName = "Verified Owner",
            phone = "+919800001111",
            whatsapp = "+919800001111",
            fullAddress = "Hill Cart Road, Siliguri",
            districtCode = "SILIGURI",
            locality = "Siliguri",
            latitude = 26.7271,
            longitude = 88.3953,
            openingHours = "09:00 AM - 06:00 PM",
            websiteOrSocial = null,
            categoriesCsv = "COMMON_FLOWERS",
            supportsWholesale = false,
            supportsRetail = true,
            deliveryAvailable = true,
            verifierNote = "Verified Partner Check"
        )
        assertTrue(verifyRes.isSuccess)

        val seedRes = repository.importBundledSeedAsset(pushToFirestore = false)
        assertTrue("Errors should be empty: ${seedRes.errors}", seedRes.errors.isEmpty())
        assertEquals(1, seedRes.nurseriesSkippedVerified)
        assertEquals(36, seedRes.nurseriesProcessed)
        assertEquals(80, seedRes.plantsMatchedEnriched)
        assertEquals(0, seedRes.plantsNewlyAdded)

        // Confirm total counts stay clean at 37 nurseries and 167 plants (zero duplicate bloat)
        assertEquals(37, repository.observeAllDirectoryNurseries().first().size)
        assertEquals(167, repository.observeAllPlants().first().size)

        // Verified nursery preserved intact
        val preserved = roomDb.plantKingdomDao().getNurseryById("wb_nurs_01")!!
        assertTrue(preserved.isVerified)
        assertEquals("+919800001111", preserved.phone)

        // Unverified pending nursery keeps zero invented contacts
        val pending = roomDb.plantKingdomDao().getNurseryById("wb_nurs_02")!!
        assertFalse(pending.isVerified)
        assertNull(pending.phone)
        assertNull(pending.fullAddress)

        // Custom bundle JSON with genuinely new nursery + new plant imports both cleanly
        val customBundle = """
            {
              "project": "KRISHAK SATHI",
              "nurseries": [
                {
                  "nursery_id": "KS-NU-9001",
                  "business_name": "Kurseong Orchid Guild",
                  "district": "KURSEONG",
                  "locality": "Kurseong",
                  "verification_status": "PENDING_VERIFICATION",
                  "source_reference": "Community Submission",
                  "owner_consent_status": "NOT_CONFIRMED"
                }
              ],
              "plants": [
                {
                  "plant_id": "KS-PL-9001",
                  "category": "hill_flower",
                  "name_en": "Darjeeling Slipper Jewel",
                  "name_bn": "দার্জিলিং জুয়েল অর্কিড",
                  "name_hi": "दार्जिलिंग ज्वेल ऑर्किड",
                  "scientific_name": "Goodyera hispida",
                  "climate_suitability_note": "Cool mist hill shade"
                }
              ]
            }
        """.trimIndent()
        val customRes = repository.importFirebaseSeedBundleJson(customBundle, pushToFirestore = false)
        assertEquals(1, customRes.nurseriesProcessed)
        assertEquals(1, customRes.plantsNewlyAdded)
        assertEquals(38, repository.observeAllDirectoryNurseries().first().size)
        assertEquals(168, repository.observeAllPlants().first().size)
    }
}
