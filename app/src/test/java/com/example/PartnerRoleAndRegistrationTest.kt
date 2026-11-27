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
import com.example.data.local.session.UserSession
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.AppUserRole
import com.example.data.model.DashboardMode
import com.example.data.model.PartnerRegistration
import com.example.data.model.PartnerRegistrationStatus
import com.example.data.remote.PartnerRoleRepository
import com.example.ui.partner.PartnerDashboardScreen
import com.example.ui.partner.PartnerRegistrationScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.KrishakSathiTheme
import com.example.util.NetworkConnectivityMonitor
import com.example.util.NetworkStatusSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class PartnerRoleAndRegistrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("krishak_sathi_partner_roles", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("krishak_sathi_nursery_apps", Context.MODE_PRIVATE).edit().clear().commit()
        UserSessionManager.clearSession(context)
        NetworkConnectivityMonitor.setSnapshotForTesting(
            NetworkStatusSnapshot(isOnline = true, transportType = "WIFI", isValidatedInternet = true)
        )
    }

    @After
    fun tearDown() {
        UserSessionManager.clearSession(context)
    }

    @Test
    fun `all seven marketplace roles exist and Administrator is excluded from partner registration choices`() {
        val allRoles = AppUserRole.values().toSet()
        assertEquals(7, allRoles.size)
        assertTrue(AppUserRole.FARMER_CUSTOMER in allRoles)
        assertTrue(AppUserRole.NURSERY_SELLER in allRoles)
        assertTrue(AppUserRole.MACHINERY_DEALER in allRoles)
        assertTrue(AppUserRole.MACHINERY_RENTAL_OWNER in allRoles)
        assertTrue(AppUserRole.CROP_BUYER in allRoles)
        assertTrue(AppUserRole.FARM_SERVICE_PROVIDER in allRoles)
        assertTrue(AppUserRole.ADMINISTRATOR in allRoles)

        val selectable = AppUserRole.partnerRegistrationChoices
        assertEquals(5, selectable.size)
        assertFalse(AppUserRole.ADMINISTRATOR in selectable)
        assertFalse(AppUserRole.FARMER_CUSTOMER in selectable)
    }

    @Test
    fun `quick login session cannot self-grant partner roles or switch to partner dashboard`() {
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = "local_farmer_1",
                userName = "Ratan",
                email = UserSessionManager.ADMIN_EMAIL,
                role = UserSession.ROLE_ADMIN,
                authProvider = UserSession.PROVIDER_FARMER_DIRECT,
                approvedRolesCsv = "${AppUserRole.NURSERY_SELLER.code},${AppUserRole.ADMINISTRATOR.code}",
                activeDashboardMode = DashboardMode.PARTNER_DASHBOARD.code
            )
        )

        val roles = UserSessionManager.getApprovedRoles(context)
        assertEquals(setOf(AppUserRole.FARMER_CUSTOMER), roles)
        assertFalse(UserSessionManager.isAdmin(context))
        assertFalse(UserSessionManager.hasApprovedPartnerRole(context))
        assertEquals(DashboardMode.FARMER_DASHBOARD, UserSessionManager.getActiveDashboardMode(context))
        assertFalse(UserSessionManager.switchDashboardMode(context, DashboardMode.PARTNER_DASHBOARD))
    }

    @Test
    fun `non-admin cloud account can hold multiple approved roles across restart but never Administrator`() {
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = "cloud_partner_1",
                userName = "Bimal Das",
                email = "bimal@example.com",
                role = UserSession.ROLE_FARMER,
                authProvider = UserSession.PROVIDER_GOOGLE
            )
        )

        UserSessionManager.updateApprovedMarketplaceRoles(
            context,
            setOf(AppUserRole.ADMINISTRATOR, AppUserRole.NURSERY_SELLER, AppUserRole.CROP_BUYER)
        )

        val rolesBeforeRestart = UserSessionManager.getApprovedRoles(context)
        assertFalse(AppUserRole.ADMINISTRATOR in rolesBeforeRestart)
        assertFalse(UserSessionManager.isAdmin(context))
        assertTrue(AppUserRole.FARMER_CUSTOMER in rolesBeforeRestart)
        assertTrue(AppUserRole.NURSERY_SELLER in rolesBeforeRestart)
        assertTrue(AppUserRole.CROP_BUYER in rolesBeforeRestart)

        assertTrue(UserSessionManager.switchDashboardMode(context, DashboardMode.PARTNER_DASHBOARD))
        assertEquals(DashboardMode.PARTNER_DASHBOARD, UserSessionManager.getActiveDashboardMode(context))

        // Simulate app restart via init()
        val reinitialized = UserSessionManager.init(context)
        assertNotNull(reinitialized)
        assertEquals("cloud_partner_1", reinitialized!!.userId)
        assertEquals(DashboardMode.PARTNER_DASHBOARD, UserSessionManager.getActiveDashboardMode(context))
        assertTrue(AppUserRole.CROP_BUYER in UserSessionManager.getApprovedRoles(context))

        // Switch back to Farmer Dashboard without logging out
        assertTrue(UserSessionManager.switchDashboardMode(context, DashboardMode.FARMER_DASHBOARD))
        assertTrue(UserSessionManager.isLoggedIn(context))
        assertEquals("cloud_partner_1", UserSessionManager.getSession(context)?.userId)
    }

    @Test
    fun `partner registration lifecycle enforces pending review rejection resubmit and verified badge gating`() = runBlocking {
        val uid = "partner_user_99"
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = uid,
                userName = "Suman Roy",
                email = "suman.partner@example.com",
                role = UserSession.ROLE_FARMER,
                authProvider = UserSession.PROVIDER_GOOGLE
            )
        )

        val repo = PartnerRoleRepository(context)
        val submitResult = repo.submitRegistration(
            PartnerRegistration(
                userId = uid,
                applicantName = "Suman Roy",
                businessName = "Green Bengal Agro Hub",
                contactPhone = "9832012345",
                businessLocation = "Sevoke Road, Siliguri",
                district = "Darjeeling",
                serviceCategories = listOf(
                    AppUserRole.ADMINISTRATOR.code, // attempt self-grant admin
                    AppUserRole.NURSERY_SELLER.code,
                    AppUserRole.MACHINERY_RENTAL_OWNER.code
                ),
                serviceArea = "Siliguri & Jalpaiguri",
                licenseNumber = "TL-2026-101",
                verificationDocumentNames = listOf("content://media/external/images/media/44", "trade_license.jpg"),
                status = PartnerRegistrationStatus.APPROVED.code, // attempt spoof
                platformVerified = true // attempt spoof
            )
        )

        assertTrue(submitResult.isSuccess)
        val pending = submitResult.getOrThrow()
        assertEquals(PartnerRegistrationStatus.PENDING.code, pending.status)
        assertFalse(pending.isVerifiedPartner())
        assertFalse(AppUserRole.ADMINISTRATOR.code in pending.serviceCategories)
        assertEquals(2, pending.requestedPartnerRoles().size)
        assertTrue(pending.verificationDocumentNames.none { it.startsWith("content://") })
        assertFalse(UserSessionManager.hasApprovedPartnerRole(context))

        // Non-admin approval attempt must fail
        val unauthorizedApprove = repo.adminApproveRegistration(
            regId = pending.id,
            grantedRoleCodes = listOf(AppUserRole.NURSERY_SELLER.code),
            adminEmail = "fakeadmin@example.com",
            callerIsAdmin = false
        )
        assertTrue(unauthorizedApprove.isFailure)

        // Admin marks under review
        val underReview = repo.adminMarkUnderReview(
            regId = pending.id,
            adminNote = "Checking license",
            adminEmail = UserSessionManager.ADMIN_EMAIL,
            callerIsAdmin = true
        ).getOrThrow()
        assertEquals(PartnerRegistrationStatus.UNDER_REVIEW.code, underReview.status)
        assertFalse(underReview.isVerifiedPartner())

        // Admin rejects first time
        val rejected = repo.adminRejectRegistration(
            regId = pending.id,
            reason = "Upload clearer trade license photo",
            adminEmail = UserSessionManager.ADMIN_EMAIL,
            callerIsAdmin = true
        ).getOrThrow()
        assertEquals(PartnerRegistrationStatus.REJECTED.code, rejected.status)
        assertFalse(rejected.isVerifiedPartner())

        // Applicant resubmits -> returns to PENDING with rejection reason cleared
        val resubmitted = repo.submitRegistration(
            rejected.copy(
                licenseNumber = "TL-2026-101-VERIFIED",
                verificationDocumentNames = listOf("clear_trade_license.jpg")
            )
        ).getOrThrow()
        assertEquals(PartnerRegistrationStatus.PENDING.code, resubmitted.status)
        assertEquals("", resubmitted.rejectionReason)
        assertFalse(resubmitted.isVerifiedPartner())

        // Admin approves multiple non-admin roles
        val approved = repo.adminApproveRegistration(
            regId = resubmitted.id,
            grantedRoleCodes = listOf(
                AppUserRole.ADMINISTRATOR.code, // must be stripped
                AppUserRole.NURSERY_SELLER.code,
                AppUserRole.MACHINERY_RENTAL_OWNER.code
            ),
            adminNote = "Verified license",
            adminEmail = UserSessionManager.ADMIN_EMAIL,
            callerIsAdmin = true
        ).getOrThrow()

        assertEquals(PartnerRegistrationStatus.APPROVED.code, approved.status)
        assertTrue(approved.isVerifiedPartner())
        assertEquals(
            listOf(AppUserRole.NURSERY_SELLER, AppUserRole.MACHINERY_RENTAL_OWNER),
            approved.resolvedApprovedPartnerRoles()
        )

        val membership = repo.observeUserRoleMembership(uid).first()
        assertTrue(membership.platformVerified)
        assertTrue(AppUserRole.NURSERY_SELLER in membership.resolvedRoles())
        assertTrue(AppUserRole.MACHINERY_RENTAL_OWNER in membership.resolvedRoles())
        assertFalse(UserSessionManager.isAdmin(context))
        assertTrue(UserSessionManager.hasApprovedPartnerRole(context))
        assertTrue(UserSessionManager.switchDashboardMode(context, DashboardMode.PARTNER_DASHBOARD))
    }

    @Test
    fun `PartnerRegistrationScreen excludes admin choice and validates submission cleanly`() {
        val uid = "cloud_applicant_ui"
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = uid,
                userName = "Tapan Sen",
                email = "tapan@example.com",
                role = UserSession.ROLE_FARMER,
                authProvider = UserSession.PROVIDER_GOOGLE
            )
        )
        val repo = PartnerRoleRepository(context)

        composeRule.setContent {
            KrishakSathiTheme {
                PartnerRegistrationScreen(
                    userId = uid,
                    applicantName = "Tapan Sen",
                    applicantPhone = "9800112233",
                    applicantEmail = "tapan@example.com",
                    currentLanguage = AppLanguage.ENGLISH,
                    repository = repo,
                    onBack = {},
                    onOpenPartnerDashboard = {}
                )
            }
        }
        composeRule.waitForIdle()

        // Admin option must not exist; partner chips and no premature verified badge
        composeRule.onNodeWithTag("partner_role_chip_${AppUserRole.ADMINISTRATOR.code}").assertDoesNotExist()
        composeRule.onNodeWithTag("partner_verified_badge").assertDoesNotExist()
        composeRule.onNodeWithTag("partner_role_chip_${AppUserRole.CROP_BUYER.code}").performScrollTo().performClick()
        composeRule.onNodeWithTag("partner_reg_business_input").performScrollTo().performTextInput("Terai Crop Traders")
        composeRule.onNodeWithTag("partner_reg_location_input").performScrollTo().performTextInput("Matigara Market")
        composeRule.onNodeWithTag("partner_reg_service_area_input").performScrollTo().performTextInput("Darjeeling & Jalpaiguri")
        composeRule.onNodeWithTag("partner_reg_submit_btn").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("partner_status_card").assertIsDisplayed()
        composeRule.onNodeWithTag("partner_verified_badge").assertDoesNotExist()
    }

    @Test
    fun `ProfileScreen displays Become a Partner and switches dashboards without logout once approved`() {
        val uid = "cloud_multi_partner"
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = uid,
                userName = "Mousumi Paul",
                email = "mousumi@example.com",
                role = UserSession.ROLE_FARMER,
                authProvider = UserSession.PROVIDER_GOOGLE,
                approvedRolesCsv = "${AppUserRole.FARMER_CUSTOMER.code},${AppUserRole.NURSERY_SELLER.code},${AppUserRole.CROP_BUYER.code}"
            )
        )
        var switchedMode: DashboardMode? = null

        composeRule.setContent {
            KrishakSathiTheme {
                ProfileScreen(
                    currentLanguage = AppLanguage.ENGLISH,
                    userId = uid,
                    onLanguageChange = {},
                    onBack = {},
                    onSignOut = {},
                    onOpenPartnerRegistration = {},
                    onSwitchDashboardMode = { mode ->
                        switchedMode = mode
                        UserSessionManager.switchDashboardMode(context, mode)
                    }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("profile_become_partner_btn").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("profile_verified_partner_badge").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_switch_partner_btn").performScrollTo().assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        assertEquals(DashboardMode.PARTNER_DASHBOARD, switchedMode)
        assertTrue(UserSessionManager.isLoggedIn(context))
        assertEquals(DashboardMode.PARTNER_DASHBOARD, UserSessionManager.getActiveDashboardMode(context))
    }

    @Test
    fun `PartnerDashboardScreen enforces role gating and renders authorized workspaces`() {
        val uid = "locked_farmer"
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = uid,
                userName = "Nirmal Roy",
                email = "nirmal@example.com",
                role = UserSession.ROLE_FARMER,
                authProvider = UserSession.PROVIDER_GOOGLE
            )
        )
        val repo = PartnerRoleRepository(context)

        composeRule.setContent {
            KrishakSathiTheme {
                PartnerDashboardScreen(
                    userId = uid,
                    partnerName = "Nirmal Roy",
                    currentLanguage = AppLanguage.BENGALI,
                    repository = repo,
                    onSwitchToFarmerDashboard = {},
                    onBack = {},
                    onOpenPartnerRegistration = {},
                    onOpenNurserySellerWorkspace = {},
                    onOpenMachinerySellerWorkspace = {},
                    onOpenCropBuyerWorkspace = {},
                    onOpenFarmServicesWorkspace = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("partner_dashboard_locked_card").assertIsDisplayed()
        composeRule.onNodeWithTag("partner_dashboard_verified_badge").assertDoesNotExist()
    }

    @Test
    fun `PartnerDashboardScreen shows role-specific workspaces and Farmer Mode switch for approved partner`() {
        val uid = "approved_multi_partner"
        UserSessionManager.saveSession(
            context,
            UserSession(
                userId = uid,
                userName = "Debashish Bose",
                email = "deb@example.com",
                role = UserSession.ROLE_NURSERY_OWNER,
                authProvider = UserSession.PROVIDER_GOOGLE,
                approvedRolesCsv = "${AppUserRole.FARMER_CUSTOMER.code},${AppUserRole.NURSERY_SELLER.code},${AppUserRole.CROP_BUYER.code}"
            )
        )
        val repo = PartnerRoleRepository(context)
        var switchedToFarmer = false

        composeRule.setContent {
            KrishakSathiTheme {
                PartnerDashboardScreen(
                    userId = uid,
                    partnerName = "Debashish Bose",
                    currentLanguage = AppLanguage.ENGLISH,
                    repository = repo,
                    onSwitchToFarmerDashboard = { switchedToFarmer = true },
                    onBack = {},
                    onOpenPartnerRegistration = {},
                    onOpenNurserySellerWorkspace = {},
                    onOpenMachinerySellerWorkspace = {},
                    onOpenCropBuyerWorkspace = {},
                    onOpenFarmServicesWorkspace = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("partner_dashboard_verified_badge").assertIsDisplayed()
        composeRule.onNodeWithTag("partner_workspace_nursery_btn").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("partner_workspace_crop_buyer_btn").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("partner_workspace_machinery_btn").assertDoesNotExist()
        composeRule.onNodeWithText("Switch", substring = true) // non-crashing sanity check
        composeRule.onNodeWithTag("partner_dashboard_switch_farmer_btn").performClick()
        assertTrue(switchedToFarmer)
    }
}
