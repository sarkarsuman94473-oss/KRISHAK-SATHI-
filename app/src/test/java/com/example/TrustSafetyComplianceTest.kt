package com.example

import android.content.Context
import android.os.Looper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ActivityEntityType
import com.example.data.local.entity.FraudReasonCode
import com.example.data.local.entity.FraudReportStatus
import com.example.data.local.entity.FraudTargetType
import com.example.data.local.entity.GrievanceStatus
import com.example.data.local.session.UserSession
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.remote.FirestoreRepository
import com.example.data.remote.NurseryApplicationRepository
import com.example.data.repository.TrustAndSafetyRepository
import com.example.ui.info.LegalPagesScreen
import com.example.ui.info.LegalPolicyCatalog
import com.example.ui.info.LegalTab
import com.example.ui.theme.KrishakSathiTheme
import com.example.ui.trust.SafeBuyerProtectionCard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TrustSafetyComplianceTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var roomDb: AppDatabase
    private lateinit var trustRepo: TrustAndSafetyRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        roomDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        trustRepo = TrustAndSafetyRepository(context, roomDb)
    }

    @After
    fun tearDown() {
        composeRule.mainClock.autoAdvance = true
        UserSessionManager.clearSession(context)
        roomDb.close()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `fraud report requires valid description and admin resolution suspends seller with audit log`() = runBlocking {
        val rejected = trustRepo.submitFraudReport(
            targetType = FraudTargetType.SELLER,
            targetId = "seller_bad_1",
            targetName = "Unverified Agro Traders",
            sellerUserId = "seller_bad_1",
            reasonCode = FraudReasonCode.ADVANCE_PAYMENT_SCAM,
            description = "short",
            reporterUserId = "farmer_101",
            reporterName = "Ramesh"
        )
        assertTrue(rejected.isFailure)

        val submitted = trustRepo.submitFraudReport(
            targetType = FraudTargetType.SELLER,
            targetId = "seller_bad_1",
            targetName = "Unverified Agro Traders",
            sellerUserId = "seller_bad_1",
            reasonCode = FraudReasonCode.ADVANCE_PAYMENT_SCAM,
            description = "Asked for 5000 INR UPI advance before showing genuine tractor papers.",
            reporterUserId = "farmer_101",
            reporterName = "Ramesh"
        )
        assertTrue(submitted.isSuccess)
        val report = submitted.getOrThrow()

        val resolveResult = trustRepo.resolveFraudReportByAdmin(
            reportId = report.id,
            newStatus = FraudReportStatus.ACTION_TAKEN,
            resolutionAction = "SUSPEND_SELLER",
            adminNote = "Verified advance-payment complaint; account suspended.",
            adminEmail = UserSessionManager.ADMIN_EMAIL,
            suspendSellerToo = true
        )
        assertTrue(resolveResult.isSuccess)
        assertTrue(trustRepo.isSellerSuspended("seller_bad_1"))
        assertTrue("seller_bad_1" in trustRepo.observeSuspendedSellerIds().first())

        val auditLogs = trustRepo.observeAllAuditLogs().first()
        assertTrue(auditLogs.any { it.action == "RESOLVE_FRAUD_REPORT" && it.targetId == "seller_bad_1" })
        assertTrue(auditLogs.any { it.action == "SUSPEND_SELLER" && it.targetId == "seller_bad_1" })

        val restore = trustRepo.unsuspendSeller("seller_bad_1", UserSessionManager.ADMIN_EMAIL, "KYC docs verified")
        assertTrue(restore.isSuccess)
        assertFalse(trustRepo.isSellerSuspended("seller_bad_1"))
    }

    @Test
    fun `grievance submission enforces validation and admin response logs audit entry`() = runBlocking {
        val shortTicket = trustRepo.submitGrievanceTicket(
            userId = "farmer_202",
            userName = "Suman",
            category = "MARKETPLACE",
            subject = "Hi",
            description = "Too short"
        )
        assertTrue(shortTicket.isFailure)

        val validTicket = trustRepo.submitGrievanceTicket(
            userId = "farmer_202",
            userName = "Suman",
            category = "PAYMENT_FRAUD",
            subject = "Dealer asked advance payment",
            description = "Seller requested UPI transfer outside marketplace without receipt."
        )
        assertTrue(validTicket.isSuccess)
        val ticket = validTicket.getOrThrow()
        assertEquals(GrievanceStatus.SUBMITTED, ticket.status)

        val adminReply = trustRepo.respondToGrievanceByAdmin(
            ticketId = ticket.id,
            newStatus = GrievanceStatus.RESOLVED,
            adminResponse = "Seller warned and reported listing removed.",
            adminEmail = UserSessionManager.ADMIN_EMAIL
        )
        assertTrue(adminReply.isSuccess)

        val updated = trustRepo.observeGrievancesForUser("farmer_202").first().first { it.id == ticket.id }
        assertEquals(GrievanceStatus.RESOLVED, updated.status)
        assertTrue(updated.adminResponse.contains("reported listing removed"))
        assertTrue(trustRepo.observeAllAuditLogs().first().any { it.action == "RESPOND_GRIEVANCE" && it.targetId == ticket.id })
    }

    @Test
    fun `activity timeline logs events and LegalPolicyCatalog preserves 27 policies and 11 compliance frameworks`() = runBlocking {
        trustRepo.logActivityEvent(
            entityType = ActivityEntityType.MACHINERY_INQUIRY,
            entityId = "inq_500",
            actorRole = "FARMER",
            actorName = "Bimal",
            statusLabel = "REQUESTED",
            note = "Requested dealer quotation"
        )
        val timeline = trustRepo.getTimelineForEntityOnce("inq_500")
        assertEquals(1, timeline.size)
        assertEquals("REQUESTED", timeline.first().statusLabel)

        assertEquals(27, LegalPolicyCatalog.all27Policies.size)
        assertEquals(11, LegalPolicyCatalog.complianceMatrix.size)
        assertEquals(6, LegalPolicyCatalog.releaseBlockers.size)

        AppLanguage.values().forEach { lang ->
            val notice = LegalPolicyCatalog.liabilityNotice(lang)
            assertTrue(notice.contains("KRISHAK SATHI"))
            assertTrue(notice.contains("1915"))
        }
        assertFalse(LegalPolicyCatalog.PLATFORM_LIABILITY_NOTICE_EN.contains("9876543210"))
    }

    @Test
    fun `user session tracks privacy consent, seller terms acceptance, admin gate, and clears on sign out`() {
        val adminSession = UserSession(
            userId = "adm_1",
            userName = "Suman Sarkar",
            email = UserSessionManager.ADMIN_EMAIL,
            role = UserSession.ROLE_FARMER,
            authProvider = UserSession.PROVIDER_GOOGLE
        )
        UserSessionManager.saveSession(context, adminSession)
        UserSessionManager.recordPrivacyConsent(context, 123456789L)
        assertTrue(UserSessionManager.isAdmin(context))
        assertEquals(123456789L, UserSessionManager.getPrivacyConsentTimestamp(context))

        assertFalse(UserSessionManager.hasAcceptedSellerAgreement(context))
        UserSessionManager.recordSellerAgreementAcceptance(context)
        assertTrue(UserSessionManager.hasAcceptedSellerAgreement(context))

        UserSessionManager.clearSession(context)
        assertFalse(UserSessionManager.isLoggedIn(context))
    }

    @Test
    fun `safe buyer protection card strictly hides call button when seller is unverified or suspended`() {
        composeRule.mainClock.autoAdvance = false

        composeRule.setContent {
            KrishakSathiTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Box(modifier = Modifier.testTag("wrap_unverified")) {
                        SafeBuyerProtectionCard(
                            businessName = "Test Machinery Hub",
                            locationAddress = "Siliguri, WB",
                            verifiedPhone = "9800001122",
                            isVerified = false,
                            isAuthorisedDealer = false,
                            isSuspended = false,
                            lastVerifiedDate = "2026-11-22",
                            warrantyOrInvoiceNote = "GST invoice provided",
                            language = AppLanguage.ENGLISH,
                            onReportFraudClick = {}
                        )
                    }
                    Box(modifier = Modifier.testTag("wrap_verified")) {
                        SafeBuyerProtectionCard(
                            businessName = "Verified Machinery Hub",
                            locationAddress = "Siliguri, WB",
                            verifiedPhone = "9800001122",
                            isVerified = true,
                            isAuthorisedDealer = false,
                            isSuspended = false,
                            lastVerifiedDate = "2026-11-22",
                            warrantyOrInvoiceNote = "GST invoice provided",
                            language = AppLanguage.ENGLISH,
                            onReportFraudClick = {}
                        )
                    }
                    Box(modifier = Modifier.testTag("wrap_suspended")) {
                        SafeBuyerProtectionCard(
                            businessName = "Suspended Machinery Hub",
                            locationAddress = "Siliguri, WB",
                            verifiedPhone = "9800001122",
                            isVerified = true,
                            isAuthorisedDealer = false,
                            isSuspended = true,
                            lastVerifiedDate = "2026-11-22",
                            warrantyOrInvoiceNote = "GST invoice provided",
                            language = AppLanguage.ENGLISH,
                            onReportFraudClick = {}
                        )
                    }
                }
            }
        }
        composeRule.mainClock.advanceTimeBy(160)
        composeRule.waitForIdle()

        // Unverified -> Call button hidden, report button visible
        composeRule.onNode(
            hasTestTag("safe_buyer_protection_card") and hasAnyAncestor(hasTestTag("wrap_unverified"))
        ).performScrollTo().assertIsDisplayed()
        composeRule.onNode(
            hasTestTag("safe_card_call_btn") and hasAnyAncestor(hasTestTag("wrap_unverified"))
        ).assertDoesNotExist()
        composeRule.onNode(
            hasTestTag("safe_card_report_fraud_btn") and hasAnyAncestor(hasTestTag("wrap_unverified"))
        ).performScrollTo().assertIsDisplayed()

        // Verified and active -> Call button appears
        composeRule.onNode(
            hasTestTag("safe_card_call_btn") and hasAnyAncestor(hasTestTag("wrap_verified"))
        ).performScrollTo().assertIsDisplayed()

        // Suspended even if verified -> Call button must stay hidden
        composeRule.onNode(
            hasTestTag("safe_buyer_protection_card") and hasAnyAncestor(hasTestTag("wrap_suspended"))
        ).performScrollTo().assertIsDisplayed()
        composeRule.onNode(
            hasTestTag("safe_card_call_btn") and hasAnyAncestor(hasTestTag("wrap_suspended"))
        ).assertDoesNotExist()
    }

    @Test
    fun `legal pages screen renders statutory liability notice and policy tabs`() {
        composeRule.setContent {
            KrishakSathiTheme {
                LegalPagesScreen(
                    currentLanguage = AppLanguage.ENGLISH,
                    initialTab = LegalTab.MARKETPLACE_POLICIES,
                    onBack = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("legal_statutory_notice_card").assertIsDisplayed()
        composeRule.onNodeWithTag("legal_tab_indian_compliance").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("legal_tab_grievance_redressal").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("grievance_redressal_panel").assertIsDisplayed()
    }

    @Test
    fun `firestore observers return safe fallback without snapshot errors when firebase user is unauthenticated`() = runBlocking {
        val repo = FirestoreRepository(context)
        assertNull(repo.observeProfile("farmer_suman_sarkar").first())
        assertTrue(repo.observeFarmerCrops("farmer_suman_sarkar").first().isEmpty())
        assertTrue(repo.observeFarmerCrops("").first().isEmpty())
        assertTrue(repo.observeTeaGardens("farmer_suman_sarkar").first().isEmpty())
        assertTrue(repo.observeProduceListings().first().isEmpty())
        assertTrue(repo.observeBuyerOffers("farmer_suman_sarkar").first().isEmpty())
        assertTrue(repo.observeOrders("farmer_suman_sarkar").first().isEmpty())
        assertTrue(repo.observeExpertInquiries("farmer_suman_sarkar").first().isEmpty())

        val nurseryRepo = NurseryApplicationRepository(context)
        assertNull(nurseryRepo.observeUserApplication("farmer_suman_sarkar").first())
        assertTrue(nurseryRepo.observeAllApplications().first().isNotEmpty())
    }
}
