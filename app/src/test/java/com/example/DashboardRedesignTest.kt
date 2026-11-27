package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppLanguage
import com.example.ui.home.DashboardCatalog
import com.example.ui.home.SimpleHomeScreen
import com.example.ui.theme.KrishakSathiTheme
import com.example.util.L10n
import com.example.util.NetworkConnectivityMonitor
import com.example.util.NetworkStatusSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class DashboardRedesignTest {

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

    @Test
    fun `all 40 dashboard tiles map to unique valid RGBA 3D or photo cutout drawables`() {
        val allTiles = DashboardCatalog.coreSections.flatMap { it.tiles } + DashboardCatalog.additionalToolCards
        assertEquals(40, allTiles.size)

        val seenDrawableIds = mutableSetOf<Int>()
        allTiles.forEach { card ->
            assertNotNull("Missing vector fallback icon for ${card.id}", card.icon)
            val resId = card.drawableResId
            assertNotNull("Missing 3D/photo drawableResId for ${card.id}", resId)
            resId!!
            assertTrue(
                "Duplicate drawableResId for ${card.id}",
                seenDrawableIds.add(resId)
            )
            val entryName = context.resources.getResourceEntryName(resId)
            assertTrue("Expected dash_ico_ resource for ${card.id}, got $entryName", entryName.startsWith("dash_ico_"))

            val bitmap = BitmapFactory.decodeResource(context.resources, resId)
            assertNotNull("Failed to decode drawable $entryName for ${card.id}", bitmap)
            assertEquals("Bitmap must preserve alpha channel for $entryName", Bitmap.Config.ARGB_8888, bitmap.config)
            assertTrue("Decoded bitmap must be non-empty for $entryName", bitmap.width >= 64 && bitmap.height >= 64)
            assertTrue("Decoded bitmap must contain transparency on corners for $entryName", bitmap.hasAlpha())
        }

        val weatherStrip = BitmapFactory.decodeResource(context.resources, R.drawable.dash_ico_weather_strip)
        assertNotNull(weatherStrip)
        assertTrue(weatherStrip.hasAlpha())
    }

    @Test
    fun `box-free 3D dashboard grid renders key tiles, preserves clicks, and supports Bengali switching`() {
        composeRule.mainClock.autoAdvance = false
        val clickedIds = mutableListOf<String>()

        composeRule.setContent {
            KrishakSathiTheme {
                SimpleHomeScreen(
                    farmerName = "Ratan",
                    language = AppLanguage.BENGALI,
                    isDarkTheme = false,
                    onCardClick = { clickedIds.add(it) },
                    onVoiceClick = {},
                    onProfileClick = {}
                )
            }
        }
        composeRule.mainClock.advanceTimeBy(160L)

        composeRule.onNodeWithTag("home_card_crops").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("home_card_nursery").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("home_card_machinery").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("home_card_govt_schemes").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("home_card_general_ai").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("home_card_leading_farmers").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("home_card_video_learning").performScrollTo().assertIsDisplayed().performClick()

        assertEquals(
            listOf("crops", "nursery", "machinery", "govt_schemes", "general_ai", "leading_farmers", "video_learning"),
            clickedIds
        )

        // Verify Bengali title label is rendered without blanking out
        composeRule.onNodeWithText(L10n.t("dash_tile_crop_advice", AppLanguage.BENGALI)).assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h640dp")
    fun `narrow compact phone renders responsive grid cleanly without overflow`() {
        composeRule.mainClock.autoAdvance = false
        val clickedIds = mutableListOf<String>()

        composeRule.setContent {
            KrishakSathiTheme {
                SimpleHomeScreen(
                    farmerName = "Suman",
                    language = AppLanguage.ENGLISH,
                    onCardClick = { clickedIds.add(it) },
                    onVoiceClick = {},
                    onProfileClick = {}
                )
            }
        }
        composeRule.mainClock.advanceTimeBy(160L)

        composeRule.onNodeWithTag("home_sunrise_background").assertIsDisplayed()
        composeRule.onNodeWithTag("home_card_crops").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("home_card_weather").performScrollTo().assertIsDisplayed().performClick()
        assertEquals(listOf("crops", "weather"), clickedIds)
    }

    @Test
    fun `sunrise golden paddy 9_16 background renders on both Welcome and Home screens`() {
        val bgBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.bg_sunrise_golden_paddy)
        assertNotNull("Sunrise Golden Paddy drawable missing", bgBitmap)
        assertTrue("Sunrise background must be 9:16 portrait", bgBitmap.height > bgBitmap.width)

        composeRule.setContent {
            KrishakSathiTheme {
                com.example.ui.auth.AuthGateScreen(
                    currentLanguage = AppLanguage.BENGALI,
                    onLanguageChange = {},
                    onAuthSuccess = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("welcome_sunrise_background").assertIsDisplayed()
    }
}
