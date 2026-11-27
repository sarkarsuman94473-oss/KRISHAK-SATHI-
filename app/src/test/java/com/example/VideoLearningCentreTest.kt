package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.CropMasterDatabase
import com.example.data.datasource.VideoLearningCatalog
import com.example.data.local.session.UserSession
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.FarmingStage
import com.example.data.model.VideoCategory
import com.example.data.model.VideoPublicationStatus
import com.example.data.remote.VideoLearningRepository
import com.example.ui.theme.KrishakSathiTheme
import com.example.ui.video.AdminVideoManagementScreen
import com.example.ui.video.VideoLearningCentreScreen
import kotlinx.coroutines.runBlocking
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
class VideoLearningCentreTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var repo: VideoLearningRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        UserSessionManager.clearSession(context)
        context.getSharedPreferences("krishak_video_learning_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        repo = VideoLearningRepository(context)
    }

    @After
    fun tearDown() {
        UserSessionManager.clearSession(context)
    }

    @Test
    fun `all 16 video categories and 5 farming stages provide complete BN HI EN localization`() {
        assertEquals(16, VideoCategory.entries.size)
        assertEquals(5, FarmingStage.entries.size)

        VideoCategory.entries.forEach { cat ->
            assertTrue("Missing BN title for ${cat.code}", cat.getTitle(AppLanguage.BENGALI).isNotBlank())
            assertTrue("Missing HI title for ${cat.code}", cat.getTitle(AppLanguage.HINDI).isNotBlank())
            assertTrue("Missing EN title for ${cat.code}", cat.getTitle(AppLanguage.ENGLISH).isNotBlank())
            assertTrue("Missing emoji for ${cat.code}", cat.emoji.isNotBlank())
        }

        FarmingStage.entries.forEach { stage ->
            assertTrue("Missing BN stage title for ${stage.code}", stage.getTitle(AppLanguage.BENGALI).isNotBlank())
            assertTrue("Missing HI stage title for ${stage.code}", stage.getTitle(AppLanguage.HINDI).isNotBlank())
            assertTrue("Missing EN stage title for ${stage.code}", stage.getTitle(AppLanguage.ENGLISH).isNotBlank())
        }
    }

    @Test
    fun `initial 10 curated lessons contain 5 localized scenes valid crop links and zero fabricated dosages`() {
        val lessons = VideoLearningCatalog.getAllBuiltInLessons()
        assertEquals(10, lessons.size)

        val validCropIds = CropMasterDatabase.crops.map { it.cropId }.toSet()
        val forbiddenDosageRegex = Regex("""\b\d+(\.\d+)?\s*(ml/l|gm/l|g/l|kg/acre|litre/acre)\b""", RegexOption.IGNORE_CASE)

        lessons.forEachIndexed { index, lesson ->
            assertEquals(index + 1, lesson.lessonNumber)
            assertTrue("Lesson ${lesson.id} must have >= 5 scenes", lesson.scenes.size >= 5)
            assertTrue("Lesson ${lesson.id} duration must be positive", lesson.durationSeconds > 0)
            assertFalse("Built-in lesson ${lesson.id} must NOT claim playable video until uploaded", lesson.hasPlayableVideo())
            assertEquals(VideoPublicationStatus.PUBLISHED, lesson.status())
            assertTrue(
                "Linked crop ${lesson.linkedCropId} missing from CropMasterDatabase",
                lesson.linkedCropId in validCropIds
            )
            assertTrue(lesson.getTitle(AppLanguage.BENGALI).isNotBlank())
            assertTrue(lesson.getTitle(AppLanguage.HINDI).isNotBlank())
            assertTrue(lesson.getTitle(AppLanguage.ENGLISH).isNotBlank())
            assertTrue(lesson.getSafetyDisclaimer(AppLanguage.BENGALI).isNotBlank())
            assertTrue(lesson.getSafetyDisclaimer(AppLanguage.HINDI).isNotBlank())
            assertTrue(lesson.getSafetyDisclaimer(AppLanguage.ENGLISH).isNotBlank())

            lesson.scenes.forEachIndexed { sceneIdx, scene ->
                assertEquals(sceneIdx + 1, scene.sceneNumber)
                assertTrue(scene.endSeconds > scene.startSeconds)
                assertTrue(scene.getTitle(AppLanguage.BENGALI).isNotBlank())
                assertTrue(scene.getTitle(AppLanguage.HINDI).isNotBlank())
                assertTrue(scene.getTitle(AppLanguage.ENGLISH).isNotBlank())
                assertTrue(scene.getNarration(AppLanguage.BENGALI).isNotBlank())
                assertTrue(scene.getNarration(AppLanguage.HINDI).isNotBlank())
                assertTrue(scene.getNarration(AppLanguage.ENGLISH).isNotBlank())
                assertTrue(scene.visualDirection.isNotBlank())
                assertTrue(scene.animationDirection.isNotBlank())
                assertTrue(scene.diagramCue.isNotBlank())

                val combinedText = "${scene.narrationBn} ${scene.narrationHi} ${scene.narrationEn}"
                assertFalse(
                    "Invented chemical dosage found in ${lesson.id} scene ${scene.sceneNumber}",
                    forbiddenDosageRegex.containsMatchIn(combinedText)
                )
            }
        }
    }

    @Test
    fun `hasPlayableVideo only returns true for real HTTPS stream URLs`() {
        val base = VideoLearningCatalog.initialLessons.first()
        assertFalse(base.hasPlayableVideo())
        assertFalse(base.copy(videoUrl = "content://media/external/video/12").hasPlayableVideo())
        assertTrue(base.copy(videoUrl = "https://firebasestorage.googleapis.com/v0/b/demo/o/lesson1.mp4").hasPlayableVideo())
    }

    @Test
    fun `search crop matching favorite bookmarking progress persistence and AI context work cleanly`() = runBlocking {
        val lessons = repo.getCachedLessons(includeUnpublished = false)
        assertEquals(10, lessons.size)

        // Multilingual search matching
        val tomatoMatches = lessons.filter { it.matchesSearch("টমেটো") }
        assertTrue(tomatoMatches.any { it.id == "vid_01_seed_germination" })
        val paddyMatches = lessons.filter { it.matchesSearch("ধান") }
        assertTrue(paddyMatches.any { it.id == "vid_06_paddy_irrigation_nutrient" })
        val potatoMatches = lessons.filter { it.matchesSearch("आलू") }
        assertTrue(potatoMatches.any { it.id == "vid_07_potato_ridge_earthing" })

        val farmerId = "farmer_test_uid_101"
        val lesson1 = lessons.first()

        // Favorite toggle & progress save
        val favResult = repo.toggleFavorite(farmerId, lesson1, AppLanguage.BENGALI)
        assertTrue(favResult.isSuccess)
        assertTrue(repo.getLocalUserProgressMap(farmerId)[lesson1.id]?.isFavorite == true)

        val progResult = repo.saveProgress(
            userId = farmerId,
            lessonId = lesson1.id,
            positionSeconds = 75,
            totalDurationSeconds = lesson1.durationSeconds,
            completed = false,
            language = AppLanguage.HINDI
        )
        assertTrue(progResult.isSuccess)
        val stored = repo.getLocalUserProgressMap(farmerId)[lesson1.id]!!
        assertEquals(75, stored.lastPositionSeconds)
        assertEquals(AppLanguage.HINDI.code, stored.lastWatchedLanguage)
        assertTrue("Favorite flag should survive progress update", stored.isFavorite)

        // AI grounding context
        val aiContext = VideoLearningRepository.buildAiLessonContext(lesson1, 75, AppLanguage.BENGALI)
        assertTrue(aiContext.contains(lesson1.getTitle(AppLanguage.BENGALI)))
        assertTrue(aiContext.contains("01:15"))
        assertTrue(aiContext.contains("[00:00 – 00:30]"))
        assertTrue(aiContext.contains("Do NOT invent fertilizer or pesticide dosages"))
    }

    @Test
    fun `non-admin users are blocked from modifying lessons or opening AdminVideoManagementScreen`() = runBlocking {
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = "regular_farmer_1",
                userName = "Ratan Mandal",
                email = "ratan@example.com",
                role = UserSession.ROLE_FARMER,
                authProvider = UserSession.PROVIDER_GOOGLE
            )
        )

        val lesson = VideoLearningCatalog.initialLessons.first()
        val saveAttempt = repo.saveOrUpdateLesson(lesson.copy(titleEn = "Tampered"))
        assertTrue(saveAttempt.isFailure)
        assertTrue(saveAttempt.exceptionOrNull() is SecurityException)

        val publishAttempt = repo.setPublicationStatus(
            lesson = lesson,
            newStatus = VideoPublicationStatus.UNPUBLISHED,
            expertReviewed = false,
            reviewedBy = "Unauthorized",
            reviewedDate = "2026-11-26"
        )
        assertTrue(publishAttempt.isFailure)
        assertTrue(publishAttempt.exceptionOrNull() is SecurityException)

        composeRule.setContent {
            KrishakSathiTheme {
                AdminVideoManagementScreen(
                    currentLanguage = AppLanguage.ENGLISH,
                    onBack = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("admin_video_access_denied").assertIsDisplayed()
    }

    @Test
    fun `verified admin can view AdminVideoManagementScreen list and editor`() {
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = "admin_uid_001",
                userName = "Suman Sarkar",
                email = UserSessionManager.ADMIN_EMAIL,
                role = UserSession.ROLE_ADMIN,
                authProvider = UserSession.PROVIDER_GOOGLE
            )
        )
        assertTrue(UserSessionManager.isAdmin(context))

        composeRule.setContent {
            KrishakSathiTheme {
                AdminVideoManagementScreen(
                    currentLanguage = AppLanguage.ENGLISH,
                    onBack = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("admin_video_new_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("admin_video_row_vid_01_seed_germination").performScrollTo().assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("admin_upload_video_btn").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("admin_video_save_btn").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("admin_video_publish_btn").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `VideoLearningCentreScreen supports Bengali Hindi English search storyboard chapters AI and crop guidance hooks`() {
        val aiCalls = mutableListOf<Pair<String, Int>>()
        val cropGuideCalls = mutableListOf<String>()

        composeRule.setContent {
            KrishakSathiTheme {
                VideoLearningCentreScreen(
                    currentLanguage = AppLanguage.BENGALI,
                    userId = "farmer_ui_test",
                    onBack = {},
                    onOpenCropGuide = { cropGuideCalls.add(it) },
                    onAskAiAboutLesson = { lesson, sec -> aiCalls.add(lesson.id to sec) }
                )
            }
        }
        composeRule.waitForIdle()

        // Search tomato in Bengali
        composeRule.onNodeWithTag("video_search_input").assertIsDisplayed().performTextInput("টমেটো")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("video_lesson_card_vid_01_seed_germination")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeRule.waitForIdle()

        // Verify honest storyboard notice + Scene 1
        composeRule.onNodeWithTag("storyboard_honest_notice").assertIsDisplayed()
        composeRule.onNodeWithTag("storyboard_scene_card").assertIsDisplayed()
        composeRule.onNodeWithText("1. ভালো বীজের গুরুত্ব").assertIsDisplayed()

        // Step to Scene 2
        composeRule.onNodeWithTag("storyboard_next_scene_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("2. পরিষ্কার জলে বীজ বাছাই পরীক্ষা").assertIsDisplayed()

        // Verify AI & Crop Guide action buttons
        composeRule.onNodeWithTag("video_ask_ai_btn").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("video_open_crop_guide_btn").performScrollTo().assertIsDisplayed().performClick()

        assertEquals(1, aiCalls.size)
        assertEquals("vid_01_seed_germination", aiCalls.first().first)
        assertEquals(listOf("tomato"), cropGuideCalls)
    }
}
