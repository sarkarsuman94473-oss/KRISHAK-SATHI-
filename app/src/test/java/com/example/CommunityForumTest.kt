package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.session.UserSession
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.ForumModerationStatus
import com.example.data.model.ForumReportReason
import com.example.data.model.ForumReportStatus
import com.example.data.model.ForumTopic
import com.example.data.model.UserModerationStatus
import com.example.data.remote.ForumRepository
import com.example.ui.forum.CommunityForumScreen
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CommunityForumTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("krishak_sathi_community_forum_v1", Context.MODE_PRIVATE)
            .edit().clear().commit()
        UserSessionManager.clearSession(context)
    }

    @Test
    fun `forum repository supports post creation, comments, likes, reporting and admin moderation`() = runBlocking {
        val repo = ForumRepository({ null }, context)

        // 1. Create a post categorized by topic & crop
        val createRes = repo.createPost(
            authorId = "farmer_u1",
            authorName = "Ramesh Mandal",
            authorRole = "FARMER",
            title = "Potato late blight prevention",
            content = "Humid morning mist observed in Jalpaiguri fields. Best preventive schedule?",
            topic = ForumTopic.PEST_DISEASE,
            cropId = "potato",
            cropName = "Potato"
        )
        assertTrue(createRes.isSuccess)
        val post = createRes.getOrThrow()

        // 2. Another farmer likes and comments
        val likeRes = repo.toggleLike(post.id, "farmer_u2")
        assertTrue(likeRes.getOrThrow())

        val cmtRes = repo.addComment(
            postId = post.id,
            authorId = "farmer_u2",
            authorName = "Bikash Roy",
            authorRole = "FARMER",
            content = "Spray Mancozeb + Cymoxanil at early onset."
        )
        assertTrue(cmtRes.isSuccess)
        val commentsAfterAdd = repo.observeComments(post.id, isAdmin = false).first()
        assertEquals(1, commentsAfterAdd.size)

        // 3. Report post for review
        val repRes = repo.submitReport(
            targetType = "POST",
            postId = post.id,
            targetAuthorId = post.authorId,
            targetAuthorName = post.authorName,
            targetPreview = post.title,
            reporterId = "farmer_u3",
            reporterName = "Tapas",
            reason = ForumReportReason.WRONG_CATEGORY,
            details = "Check topic tag"
        )
        assertTrue(repRes.isSuccess)
        val report = repRes.getOrThrow()
        assertEquals(ForumReportStatus.PENDING.code, report.status)

        // 4. Admin pins, hides post, resolves report, and warns/mutes author
        assertTrue(repo.adminPinPost(post.id, true).isSuccess)
        assertTrue(repo.adminUpdatePostModeration(post.id, ForumModerationStatus.HIDDEN, "Off-topic duplicate").isSuccess)
        assertTrue(repo.adminResolveReport(report.id, ForumReportStatus.RESOLVED, "Hidden by moderator").isSuccess)

        val publicFeed = repo.observePosts(isAdmin = false).first()
        assertFalse("Hidden post should not appear in regular feed", publicFeed.any { it.id == post.id })
        val adminFeed = repo.observePosts(isAdmin = true).first()
        assertTrue("Hidden post should remain visible in admin view", adminFeed.any { it.id == post.id && it.pinned })

        // 5. Admin mutes author -> muted author is blocked from posting & commenting
        val muteRes = repo.adminSetUserBehaviorStatus(
            targetUserId = "farmer_u1",
            targetUserName = "Ramesh Mandal",
            newStatus = UserModerationStatus.MUTED,
            reason = "Repeated spam",
            muteDurationHours = 24L,
            adminId = "admin_1"
        )
        assertTrue(muteRes.isSuccess)
        assertEquals(1L, muteRes.getOrThrow().warningCount)

        val blockedPost = repo.createPost(
            authorId = "farmer_u1",
            authorName = "Ramesh Mandal",
            authorRole = "FARMER",
            title = "Should fail",
            content = "Muted user post attempt",
            topic = ForumTopic.CROP_CARE,
            cropId = "general",
            cropName = "General Farming"
        )
        assertTrue("Muted user post must fail", blockedPost.isFailure)

        val blockedComment = repo.addComment(
            postId = "starter_post_tea_mite",
            authorId = "farmer_u1",
            authorName = "Ramesh Mandal",
            authorRole = "FARMER",
            content = "Muted reply attempt"
        )
        assertTrue("Muted user reply must fail", blockedComment.isFailure)

        // 6. Admin restores user -> user can post again
        assertTrue(
            repo.adminSetUserBehaviorStatus(
                targetUserId = "farmer_u1",
                targetUserName = "Ramesh Mandal",
                newStatus = UserModerationStatus.ACTIVE,
                reason = "Restored after warning",
                muteDurationHours = 0L,
                adminId = "admin_1"
            ).isSuccess
        )
        val allowedPost = repo.createPost(
            authorId = "farmer_u1",
            authorName = "Ramesh Mandal",
            authorRole = "FARMER",
            title = "Rice nursery bed tips",
            content = "Raising healthy mat nursery seedlings for Boro paddy.",
            topic = ForumTopic.SEEDS_NURSERY,
            cropId = "rice_paddy",
            cropName = "Rice"
        )
        assertTrue(allowedPost.isSuccess)
    }

    @Test
    fun `community forum screen renders feed, creates post and opens admin moderation panel`() {
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = "admin_test_uid",
                userName = "Suman Sarkar",
                email = UserSessionManager.ADMIN_EMAIL,
                role = UserSession.ROLE_ADMIN
            )
        )

        composeRule.setContent {
            KrishakSathiTheme {
                CommunityForumScreen(
                    userId = "admin_test_uid",
                    userName = "Suman Sarkar",
                    currentLanguage = AppLanguage.ENGLISH,
                    onBack = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("forum_search_input").assertIsDisplayed()
        composeRule.onNodeWithTag("forum_new_post_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("forum_moderation_open_btn").assertIsDisplayed()

        // Create a new post via UI
        composeRule.onNodeWithTag("forum_new_post_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("forum_create_title_input")
            .performScrollTo()
            .performTextInput("Neem oil dosage for Brinjal")
        composeRule.onNodeWithTag("forum_create_body_input")
            .performScrollTo()
            .performTextInput("How much neem oil 1500ppm per litre should we mix for shoot borer?")
        composeRule.onNodeWithTag("forum_publish_submit_btn")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("forum_publish_submit_btn").assertDoesNotExist()

        composeRule.onNodeWithTag("forum_feed_filter_MY_POSTS").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Neem oil dosage for Brinjal").performScrollTo().assertIsDisplayed()

        // Open Post Detail and add a comment
        composeRule.onNodeWithText("Neem oil dosage for Brinjal").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("forum_reply_input").performTextInput("Mix 3 ml per litre in evening.")
        composeRule.onNodeWithTag("forum_send_reply_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("forum_detail_list")
            .performScrollToNode(hasText("Mix 3 ml per litre in evening."))
        composeRule.onNodeWithText("Mix 3 ml per litre in evening.").assertIsDisplayed()
        composeRule.onNodeWithTag("forum_detail_back_btn").performClick()
        composeRule.waitForIdle()

        // Open Moderation Panel
        composeRule.onNodeWithTag("forum_moderation_open_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("forum_mod_tab_0").assertIsDisplayed()
        composeRule.onNodeWithTag("forum_mod_tab_1").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("forum_mod_list")
            .performScrollToNode(hasText("Neem oil dosage for Brinjal"))
        composeRule.onNodeWithText("Neem oil dosage for Brinjal").assertIsDisplayed()

        // Verify User Behavior moderation tab renders authors and actions
        composeRule.onNodeWithTag("forum_mod_tab_2").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("forum_mod_warn_admin_test_uid").assertExists()
        composeRule.onNodeWithTag("forum_mod_back_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("forum_search_input").assertIsDisplayed()
    }
}
