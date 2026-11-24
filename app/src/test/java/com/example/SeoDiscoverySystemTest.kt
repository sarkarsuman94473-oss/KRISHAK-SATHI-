package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.FeatureAvailabilityStatus
import com.example.data.model.SeoArticleStatus
import com.example.data.model.SeoContentCategory
import com.example.data.repository.SeoDiscoveryRepository
import com.example.ui.info.AdminConfigScreen
import com.example.ui.info.PublicDiscoveryHubScreen
import com.example.ui.theme.KrishakSathiTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SeoDiscoverySystemTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    private fun resolvePublicDir(): File {
        val direct = File("public")
        if (direct.exists() && direct.isDirectory) return direct
        val parent = File("../public")
        if (parent.exists() && parent.isDirectory) return parent
        throw AssertionError("Could not locate public/ web directory from ${File(".").absolutePath}")
    }

    @Test
    fun migration_8_to_9_creates_seo_tables_and_preserves_existing_data() {
        val dbName = "migration_8_9_test.db"
        context.deleteDatabase(dbName)

        val callback = object : SupportSQLiteOpenHelper.Callback(8) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS dummy_v8 (id TEXT PRIMARY KEY NOT NULL)")
                db.execSQL("INSERT INTO dummy_v8 VALUES ('keep_me')")
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
        }

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(callback)
                .build()
        )
        val rawDb = helper.writableDatabase
        AppDatabase.MIGRATION_8_9.migrate(rawDb)

        val cursor = rawDb.query("SELECT id FROM dummy_v8")
        assertTrue(cursor.moveToFirst())
        assertEquals("keep_me", cursor.getString(0))
        cursor.close()

        val tables = mutableSetOf<String>()
        val tc = rawDb.query("SELECT name FROM sqlite_master WHERE type='table'")
        while (tc.moveToNext()) tables.add(tc.getString(0))
        tc.close()

        assertTrue(tables.contains("seo_articles"))
        assertTrue(tables.contains("seo_config"))
        assertTrue(tables.contains("seo_referral_logs"))
        rawDb.close()
        helper.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun repository_enforces_ai_draft_review_and_generates_valid_robots_and_sitemap() = runBlocking {
        val inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = SeoDiscoveryRepository(context, inMemoryDb)
            repo.ensureSeeded()

            val seededPublished = repo.observePublishedArticles().first()
            assertTrue("Expected seeded multilingual published articles", seededPublished.size >= 12)
            assertTrue(seededPublished.any { it.cropId == "rice" })
            assertTrue(seededPublished.any { it.cropId == "tea" })
            assertTrue(seededPublished.any { it.cropId == "potato" })

            // AI-assisted draft must NOT publish immediately
            val draftRes = repo.createAiAssistedDraft(
                titleEn = "Winter Mustard Fog Protection",
                titleBn = "শীতকালীন কুয়াশায় সরিষা সুরক্ষা",
                titleHi = "कोहरे में सरसों सुरक्षा",
                slug = "winter-mustard-fog",
                category = SeoContentCategory.CROP_GUIDE,
                cropId = "mustard",
                summaryEn = "Field scouting steps during dense North Bengal winter fog.",
                summaryBn = "ঘন কুয়াশায় ক্ষেত পরিদর্শন গাইড।",
                summaryHi = "घने कोहरे में खेत निगरानी।",
                bodyEn = "Scout crops early and check CIBRC label instructions.",
                bodyBn = "সকালে ক্ষেত পরীক্ষা করুন।",
                bodyHi = "सुबह फसल की जांच करें।",
                sourceName = "UBKV Pundibari",
                sourceUrl = "https://www.ubkv.ac.in/"
            )
            assertTrue(draftRes.isSuccess)
            val createdDraft = draftRes.getOrThrow()
            assertEquals(SeoArticleStatus.AI_DRAFT_PENDING_REVIEW.wireValue, createdDraft.status)
            assertFalse(
                "AI draft must remain hidden from published list until approved",
                repo.observePublishedArticles().first().any { it.id == createdDraft.id }
            )

            // Explicit admin approval promotes draft -> PUBLISHED
            val approved = repo.approveAndPublishDraft(createdDraft.id, "admin_reviewer").getOrThrow()
            assertEquals(SeoArticleStatus.PUBLISHED.wireValue, approved.status)
            assertTrue(repo.observePublishedArticles().first().any { it.id == createdDraft.id })

            // Validate robots.txt & sitemap.xml
            val config = repo.defaultConfig()
            val audit = repo.runTechnicalAudit(config, repo.observeAllArticles().first())
            assertTrue(audit.robotsTxtValid)
            assertTrue(audit.googlebotAllowed)
            assertTrue(audit.oaiSearchBotAllowed)
            assertTrue(audit.gptBotSeparated)
            assertTrue(audit.adminAndPrivateDisallowed)
            assertTrue(audit.sitemapValid)

            // Validate brand roadmap separates Live vs Under Development vs Planned
            val caps = repo.brandCapabilities()
            assertTrue(caps.any { it.status == FeatureAvailabilityStatus.LIVE })
            assertTrue(caps.any { it.status == FeatureAvailabilityStatus.UNDER_DEVELOPMENT })
            assertTrue(caps.any { it.status == FeatureAvailabilityStatus.PLANNED && it.detailEn.contains("Coming Soon") })

            // Validate privacy-safe referral log
            repo.recordReferral("google_search", "rice", AppLanguage.BENGALI)
            val summary = repo.observeReferralSummary().first()
            assertEquals(1, summary.totalVisits)
            assertEquals(1, summary.countsByBucket["google_search"])
        } finally {
            inMemoryDb.close()
        }
    }

    @Test
    fun static_public_web_hub_passes_seo_hreflang_robots_and_honesty_audit() {
        val publicDir = resolvePublicDir()

        // 1. Check robots.txt
        val robotsTxt = File(publicDir, "robots.txt").readText()
        assertTrue(robotsTxt.contains("User-agent: Googlebot"))
        assertTrue(robotsTxt.contains("User-agent: OAI-SearchBot"))
        assertTrue(robotsTxt.contains("User-agent: GPTBot"))
        assertTrue(robotsTxt.contains("Disallow: /admin"))
        assertTrue(robotsTxt.contains("Disallow: /api/"))
        assertTrue(robotsTxt.contains("Disallow: /auth/"))
        assertTrue(robotsTxt.contains("Disallow: /firebase/"))
        assertTrue(robotsTxt.contains("Disallow: /users/"))
        assertTrue(robotsTxt.contains("Disallow: /private/"))
        assertTrue(robotsTxt.contains("Sitemap: https://ais-pre-2x4wg44pdpamhvdlijirfv-948907126237.asia-southeast1.run.app/sitemap.xml"))

        // 2. Check sitemap.xml
        val sitemapXml = File(publicDir, "sitemap.xml").readText()
        assertTrue(sitemapXml.contains("<urlset"))
        assertTrue(sitemapXml.contains("hreflang=\"en\""))
        assertTrue(sitemapXml.contains("hreflang=\"bn\""))
        assertTrue(sitemapXml.contains("hreflang=\"hi\""))
        assertTrue(sitemapXml.contains("hreflang=\"x-default\""))
        assertTrue(sitemapXml.contains("/crops/rice.html"))
        assertTrue(sitemapXml.contains("/brand/index.html"))

        // 3. Check Emblem & Crop image assets referenced on disk
        assertTrue(File(publicDir, "assets/aistudio/Krishak_Sathi_Smart_Farming_Emblem_e7589b98.png").exists())
        val cropSlugs = listOf("rice", "potato", "tea", "tomato", "cabbage", "mustard", "mango", "chilli")
        val cropImgFiles = listOf("rice_paddy.jpg", "potato.jpg", "tea.jpg", "tomato.jpg", "cabbage.jpg", "mustard.jpg", "mango.jpg", "chilli.jpg")
        cropImgFiles.forEach { fileName ->
            assertTrue("Missing public crop image: $fileName", File(publicDir, "assets/crops/$fileName").exists())
        }

        // 4. Verify EN / BN / HI HTML pages for canonicals, hreflang, JSON-LD, tagline, honest Play Store Coming Soon & zero fake URLs
        val expectedRelativePages = mutableListOf(
            "index.html", "bn/index.html", "hi/index.html",
            "brand/index.html", "bn/brand/index.html", "hi/brand/index.html",
            "knowledge/index.html", "bn/knowledge/index.html", "hi/knowledge/index.html"
        )
        cropSlugs.forEach { slug ->
            expectedRelativePages.add("crops/$slug.html")
            expectedRelativePages.add("bn/crops/$slug.html")
            expectedRelativePages.add("hi/crops/$slug.html")
        }

        expectedRelativePages.forEach { rel ->
            val f = File(publicDir, rel)
            assertTrue("Missing public HTML page: $rel", f.exists())
            val html = f.readText()
            assertTrue("$rel missing canonical link", html.contains("<link rel=\"canonical\""))
            assertTrue("$rel missing hreflang bn", html.contains("hreflang=\"bn\""))
            assertTrue("$rel missing hreflang hi", html.contains("hreflang=\"hi\""))
            assertTrue("$rel missing JSON-LD", html.contains("application/ld+json"))
            assertTrue("$rel missing canonical tagline", html.contains("KRISHAK SATHI — Growing Together, Digitally."))
            assertTrue("$rel missing honest Play Store status", html.contains("Coming Soon"))
            assertFalse("$rel must never link to a fake Play Store URL", html.contains("play.google.com/store/apps"))
        }
    }

    @Test
    fun public_discovery_hub_renders_tabs_and_searches_articles() {
        composeRule.setContent {
            KrishakSathiTheme {
                PublicDiscoveryHubScreen(
                    currentLanguage = AppLanguage.ENGLISH,
                    onBack = {}
                )
            }
        }

        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithTag("discovery_article_rice").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()

        // Search filters down to Rice
        composeRule.onNodeWithTag("discovery_search_input").performTextInput("Tea")
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithTag("discovery_article_tea").fetchSemanticsNodes().isNotEmpty()
        }

        // Switch to Crop Guides tab
        composeRule.onNodeWithTag("discovery_tab_crops").performClick()
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithTag("discovery_crop_card_rice").fetchSemanticsNodes().isNotEmpty()
        }

        // Switch to Brand & Status tab and verify honest Coming Soon & tagline
        composeRule.onNodeWithTag("discovery_tab_brand").performClick()
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithTag("discovery_brand_hero_card").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Google Play Store: Coming Soon").assertIsDisplayed()
    }

    @Test
    fun admin_config_embeds_seo_management_and_runs_technical_audit() {
        composeRule.setContent {
            KrishakSathiTheme {
                AdminConfigScreen(
                    currentLanguage = AppLanguage.ENGLISH,
                    onBack = {}
                )
            }
        }

        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithTag("admin_seo_section").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("admin_seo_tab_technical").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("admin_seo_run_audit_btn").performScrollTo().performClick()
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithTag("admin_seo_audit_result_card").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("admin_seo_audit_result_card").assertIsDisplayed()
    }
}
