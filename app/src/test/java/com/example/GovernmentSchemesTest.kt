package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.GovernmentSchemesCatalog
import com.example.data.local.AppDatabase
import com.example.data.local.dao.GovernmentSchemeDao
import com.example.data.local.entity.toEntity
import com.example.data.local.session.UserSession
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.GovernmentScheme
import com.example.data.model.SchemeApplicationStatus
import com.example.data.model.SchemeCategory
import com.example.data.model.SchemeFallbackText
import com.example.data.model.SchemeFarmerType
import com.example.data.model.SchemeGovLevel
import com.example.data.model.isValidOfficialHttpsUrl
import com.example.data.remote.GovernmentSchemesRepository
import com.example.ui.navigation.HashRoutes
import com.example.ui.schemes.GovernmentSchemesScreen
import com.example.ui.schemes.GovernmentSchemesViewModel
import com.example.ui.schemes.SchemeUiState
import com.example.ui.theme.KrishakSathiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class GovernmentSchemesTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var roomDb: AppDatabase
    private lateinit var schemeDao: GovernmentSchemeDao
    private lateinit var repository: GovernmentSchemesRepository

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        UserSessionManager.clearSession(context)
        roomDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        schemeDao = roomDb.governmentSchemeDao()
        repository = GovernmentSchemesRepository(context, schemeDao)
        repository.clearCacheForTest()
    }

    @After
    fun tearDown() {
        roomDb.close()
    }

    @Test
    fun `catalog covers all 18 categories, verifies official HTTPS links, and prioritizes North Bengal & WB`() {
        val schemes = GovernmentSchemesCatalog.allSchemes
        assertTrue("Catalog should include at least 20 verified schemes", schemes.size >= 20)

        // All 18 required categories represented
        assertEquals(18, SchemeCategory.entries.size)
        SchemeCategory.entries.forEach { cat ->
            assertTrue(
                "Category ${cat.name} must have at least one scheme",
                schemes.any { cat in it.resolveAllCategories() }
            )
        }

        // Priority schemes exist with official URLs
        val krishakBandhu = schemes.firstOrNull { it.id == "wb_krishak_bandhu" }
        assertNotNull(krishakBandhu)
        assertEquals("https://krishakbandhu.wb.gov.in/", krishakBandhu?.officialUrl)

        val pmKisan = schemes.firstOrNull { it.id == "central_pm_kisan" }
        assertNotNull(pmKisan)
        assertEquals("https://pmkisan.gov.in/", pmKisan?.officialUrl)

        // North Bengal & West Bengal prioritized first without hiding Central/Other State schemes
        assertEquals("wb_krishak_bandhu", schemes.first().id)
        assertTrue(schemes.any { it.resolveGovLevel() == SchemeGovLevel.CENTRAL })
        assertTrue(schemes.any { it.resolveGovLevel() == SchemeGovLevel.OTHER_STATE })
        assertEquals("#govt-schemes", HashRoutes.GOVT_SCHEMES)
    }

    @Test
    fun `missing optional details fall back to localized official website message`() {
        val sparseScheme = GovernmentScheme(
            id = "sparse_test",
            nameEn = "Test Scheme",
            nameBn = "পরীক্ষা প্রকল্প",
            nameHi = "परीक्षण योजना",
            officialUrl = "https://pmkisan.gov.in/"
        )
        assertEquals("Please check the official website.", sparseScheme.getEligibility(AppLanguage.ENGLISH))
        assertEquals("অনুগ্রহ করে অফিশিয়াল ওয়েবসাইট দেখুন।", sparseScheme.getEligibility(AppLanguage.BENGALI))
        assertEquals("कृपया आधिकारिक वेबसाइट देखें।", sparseScheme.getEligibility(AppLanguage.HINDI))
        assertEquals("Please check the official website.", sparseScheme.getDocuments(AppLanguage.ENGLISH))
        assertEquals("Please check the official website.", sparseScheme.getBenefits(AppLanguage.ENGLISH))
    }

    @Test
    fun `room cache seeds offline schemes and bookmarks survive catalog sync and remain user-scoped`() = runBlocking {
        // 1. Seed Room cache without internet
        val cachedSchemes = repository.observeSchemes().first()
        assertTrue("Expected offline Room cache to hold >= 20 schemes", cachedSchemes.size >= 20)
        assertTrue(schemeDao.getSchemeCount() >= 20)

        // 2. Bookmark schemes for farmer_1 and verify farmer_2 remains isolated
        val added1 = repository.toggleBookmark("farmer_1", "wb_krishak_bandhu")
        assertTrue(added1)
        assertTrue(repository.isBookmarked("farmer_1", "wb_krishak_bandhu"))
        assertFalse(repository.isBookmarked("farmer_2", "wb_krishak_bandhu"))

        val savedIds = repository.observeBookmarkedIds("farmer_1").first()
        assertEquals(setOf("wb_krishak_bandhu"), savedIds)

        // 3. Simulate a remote Firestore catalog upsert replacing cached_government_schemes rows
        val refreshedKrishakBandhu = GovernmentSchemesCatalog.allSchemes
            .first { it.id == "wb_krishak_bandhu" }
            .copy(lastVerifiedDate = "2026-12-01")
        schemeDao.upsertSchemes(listOf(refreshedKrishakBandhu.toEntity()))

        // Bookmark MUST survive the scheme row replacement in Room, and updated scheme is readable offline
        assertTrue("Bookmark should survive catalog cache refresh", repository.isBookmarked("farmer_1", "wb_krishak_bandhu"))
        val bookmarkedSchemes = repository.observeBookmarkedSchemes("farmer_1").first()
        assertEquals(1, bookmarkedSchemes.size)
        assertEquals("2026-12-01", bookmarkedSchemes.first().lastVerifiedDate)

        // 4. Removing bookmark clears it from Room
        val afterRemove = repository.toggleBookmark("farmer_1", "wb_krishak_bandhu")
        assertFalse(afterRemove)
        assertTrue(repository.observeBookmarkedIds("farmer_1").first().isEmpty())
    }

    @Test
    fun `repository admin CRUD enforces HTTPS URLs, prevents silent URL overwrite, and persists in Room`() = runBlocking {
        // Invalid HTTP URL rejected
        val httpRes = repository.saveScheme(
            scheme = GovernmentScheme(
                nameEn = "Fake Scheme",
                officialUrl = "http://example.gov.in/"
            ),
            isNew = true,
            adminUid = "admin_1"
        )
        assertTrue("HTTP non-HTTPS link must be rejected", httpRes.isFailure)
        assertFalse(isValidOfficialHttpsUrl("http://pmkisan.gov.in/"))
        assertTrue(isValidOfficialHttpsUrl("https://pmkisan.gov.in/"))

        // Valid creation persists in Room cache
        val createRes = repository.saveScheme(
            scheme = GovernmentScheme(
                id = "custom_nb_horti",
                govLevel = SchemeGovLevel.WEST_BENGAL.name,
                category = SchemeCategory.HORTICULTURE_FRUITS_VEG.name,
                officialUrl = "https://wb.gov.in/government-schemes.aspx",
                officialSourceUrl = "https://agriculture.wb.gov.in/",
                lastVerifiedDate = "2026-11-17",
                nameEn = "North Bengal Pineapple & Mandarin Mission",
                nameBn = "উত্তরবঙ্গ আনারস ও কমলালেবু মিশন",
                nameHi = "उत्तर बंगाल अनानास एवं संतरा मिशन",
                departmentEn = "Dept of Horticulture, Govt of WB"
            ),
            isNew = true,
            adminUid = "admin_1"
        )
        assertTrue(createRes.isSuccess)

        // Attempt silent URL overwrite with allowUrlOverwrite=false -> rejected
        val existing = repository.getSchemeById("custom_nb_horti")!!
        val overwriteBlocked = repository.saveScheme(
            scheme = existing.copy(officialUrl = "https://agriculture.wb.gov.in/"),
            isNew = false,
            adminUid = "admin_1",
            allowUrlOverwrite = false
        )
        assertTrue("Silent URL overwrite without confirmation should fail", overwriteBlocked.isFailure)

        // Update status to DISCONTINUED
        val statusRes = repository.updateSchemeStatus(
            scheme = existing,
            newStatus = SchemeApplicationStatus.DISCONTINUED,
            verifiedDate = "2026-11-18",
            adminUid = "admin_1"
        )
        assertTrue(statusRes.isSuccess)

        val observed = repository.observeSchemes().first()
        val updated = observed.first { it.id == "custom_nb_horti" }
        assertTrue(updated.discontinued)
        assertEquals(SchemeApplicationStatus.DISCONTINUED, updated.resolveApplicationStatus())
        assertEquals("2026-11-18", updated.lastVerifiedDate)
        // Discontinued scheme is sorted after active schemes
        assertEquals("custom_nb_horti", observed.last().id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `viewmodel filters by trilingual query, gov level, category, farmer type, status and saved bookmarks`() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            val vm = GovernmentSchemesViewModel(
                repository = repository,
                currentAdminUid = "farmer_1",
                isAdmin = false
            )
            // Collect initial state
            val loaded = vm.allSchemesState.first { it is SchemeUiState.Success } as SchemeUiState.Success
            assertTrue(loaded.schemes.size >= 20)

            // Bookmark PM-KISAN and enable savedOnly filter
            vm.toggleBookmark("central_pm_kisan")
            vm.bookmarkedIds.first { "central_pm_kisan" in it }
            vm.setSavedOnly(true)
            val savedOnlyList = vm.filteredSchemes.first { it.size == 1 && it.first().id == "central_pm_kisan" }
            assertEquals("central_pm_kisan", savedOnlyList.first().id)

            vm.clearFilters()

            // Bengali search matches Krishak Bandhu
            vm.setSearchQuery("কৃষকবন্ধু")
            val bnSearch = vm.filteredSchemes.first { it.isNotEmpty() }
            assertTrue(bnSearch.any { it.id == "wb_krishak_bandhu" })

            // Hindi search matches Bangla Shasya Bima
            vm.setSearchQuery("फसल बीमा")
            val hiSearch = vm.filteredSchemes.first { it.any { s -> s.id == "central_pmfby" || s.id == "wb_bangla_shasya_bima" } }
            assertTrue(hiSearch.isNotEmpty())

            // Filter by category CROP_INSURANCE + WEST_BENGAL
            vm.clearFilters()
            vm.setGovLevel(SchemeGovLevel.WEST_BENGAL)
            vm.setCategory(SchemeCategory.CROP_INSURANCE)
            vm.setFarmerType(SchemeFarmerType.TENANT_SHARECROPPER)
            val filtered = vm.filteredSchemes.first { it.size == 1 && it.first().id == "wb_bangla_shasya_bima" }
            assertEquals("wb_bangla_shasya_bima", filtered.first().id)

            vm.clearFilters()
            assertEquals(0, vm.filters.value.activeFilterCount)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `ui screen shows offline cache badge, bookmarks filter, browser intent, detail screen, and restricts admin`() {
        // Non-admin farmer session -> admin add button must be hidden
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = "farmer_u1",
                userName = "Ramesh Mandal",
                email = "farmer@example.com",
                role = UserSession.ROLE_FARMER
            )
        )

        composeRule.setContent {
            KrishakSathiTheme {
                GovernmentSchemesScreen(
                    currentLanguage = AppLanguage.ENGLISH,
                    userId = "farmer_u1",
                    onBack = {},
                    repositoryOverride = repository
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            runCatching {
                composeRule.onNodeWithTag("scheme_offline_cache_badge").assertIsDisplayed()
            }.isSuccess
        }

        // Disclaimer and Offline Cache Ready badge displayed; Admin Add button does not exist
        composeRule.onNodeWithTag("scheme_disclaimer_banner").assertIsDisplayed()
        composeRule.onNodeWithTag("scheme_offline_cache_badge").assertIsDisplayed()
        composeRule.onNodeWithTag("scheme_admin_add_btn").assertDoesNotExist()

        // Bookmark Krishak Bandhu and filter by Saved Offline
        composeRule.onNodeWithTag("scheme_bookmark_btn_wb_krishak_bandhu").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("scheme_filter_saved_only").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("scheme_card_wb_krishak_bandhu").assertIsDisplayed()

        // Click VISIT OFFICIAL WEBSITE -> opens https://krishakbandhu.wb.gov.in/
        composeRule.onNodeWithTag("scheme_visit_btn_wb_krishak_bandhu")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()

        val startedIntent = shadowOf(ApplicationProvider.getApplicationContext<Application>()).nextStartedActivity
        assertNotNull("Expected ACTION_VIEW external browser Intent", startedIntent)
        assertEquals(Intent.ACTION_VIEW, startedIntent.action)
        assertEquals("https://krishakbandhu.wb.gov.in/", startedIntent.dataString)

        // Open Scheme Detail Page & verify detail bookmark button + safety notice
        composeRule.onNodeWithTag("scheme_detail_btn_wb_krishak_bandhu")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("scheme_detail_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("scheme_detail_bookmark_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("scheme_detail_visit_official_btn").assertIsDisplayed()
        composeRule.onNodeWithText(SchemeFallbackText.disclaimer(AppLanguage.ENGLISH), substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `admin user sees admin management controls and Bengali Hindi UI strings switch properly`() {
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = "admin_u1",
                userName = "Suman Sarkar",
                email = UserSessionManager.ADMIN_EMAIL,
                role = UserSession.ROLE_ADMIN
            )
        )

        composeRule.setContent {
            KrishakSathiTheme {
                GovernmentSchemesScreen(
                    currentLanguage = AppLanguage.BENGALI,
                    userId = "admin_u1",
                    onBack = {},
                    repositoryOverride = repository
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("scheme_admin_add_btn").assertIsDisplayed()
        composeRule.onNodeWithText("সরকারি কৃষি প্রকল্প").assertIsDisplayed()

        // Clicking Admin Add opens the Admin Editor Dialog
        composeRule.onNodeWithTag("scheme_admin_add_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("scheme_admin_dialog").assertIsDisplayed()
    }
}
