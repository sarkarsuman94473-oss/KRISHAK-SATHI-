package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CropMasterDatabase
import com.example.data.local.AppDatabase
import com.example.data.local.session.UserSession
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.DashboardMode
import com.example.data.model.MachineryCategory
import com.example.data.model.MachineryProduct
import com.example.data.model.MasterCrop
import com.example.data.model.SchemeCategory
import com.example.data.remote.NurseryApplicationRepository
import com.example.data.remote.PartnerRoleRepository
import com.example.data.remote.configureAppCheck
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import com.example.ui.auth.onGoogleSignInClicked
import com.example.ui.components.GoogleCloudSyncPromptDialog
import com.example.util.NetworkConnectivityMonitor
import com.example.data.repository.CustomCropRepository
import com.example.data.repository.LeadingFarmerRepository
import com.example.ui.ai.GeneralAiScreen
import com.example.ui.ai.KrishakAiScreen
import com.example.ui.auth.AuthGateScreen
import com.example.ui.calculator.DosageCalculatorScreen
import com.example.ui.crops.AddFarmerCropScreen
import com.example.ui.crops.AdminAddMasterCropScreen
import com.example.ui.crops.CropDetailScreen
import com.example.ui.crops.MyCropsScreen
import com.example.ui.expense.ExpenseProfitScreen
import com.example.ui.expert.ExpertScreen
import com.example.ui.farmers.LeadingFarmersAdminScreen
import com.example.ui.farmers.LeadingFarmersScreen
import com.example.ui.forum.CommunityForumScreen
import com.example.ui.home.AdminBannerManagementScreen
import com.example.ui.home.SimpleHomeScreen
import com.example.ui.info.AboutUsScreen
import com.example.ui.info.AdminConfigScreen
import com.example.ui.info.ContactUsScreen
import com.example.ui.info.FaqScreen
import com.example.ui.info.GovernmentInfoScreen
import com.example.ui.info.LegalPagesScreen
import com.example.ui.info.LegalTab
import com.example.ui.info.PublicDiscoveryHubScreen
import com.example.ui.land.LandRightsScreen
import com.example.ui.land.LandTopic
import com.example.ui.library.ResourceLibraryScreen
import com.example.ui.machinery.AdminMachineryManagementScreen
import com.example.ui.machinery.MachineryDealerDirectoryScreen
import com.example.ui.machinery.MachineryHubScreen
import com.example.ui.machinery.MachineryManufacturersScreen
import com.example.ui.machinery.MachineryProductDetailScreen
import com.example.ui.market.MarketPriceScreen
import com.example.ui.marketplace.MarketplaceScreen
import com.example.ui.marketplace.SellProduceScreen
import com.example.ui.medicine.CropMedicineHubScreen
import com.example.ui.navigation.HashRoutes
import com.example.ui.navigation.rememberHashRouter
import com.example.ui.nursery.AdminNurseryManagementScreen
import com.example.ui.nursery.NurseryDirectoryScreen
import com.example.ui.nursery.NurseryMapScreen
import com.example.ui.nursery.NurseryPublicProfileScreen
import com.example.ui.nursery.NurseryRegistrationScreen
import com.example.ui.nursery.NurserySeedMarketScreen
import com.example.ui.nursery.NurserySellerDashboardScreen
import com.example.ui.nursery.PlantDetailScreen
import com.example.ui.nursery.PlantKingdomLibraryScreen
import com.example.ui.problem.PlantProblemCheckerScreen
import com.example.ui.partner.PartnerDashboardScreen
import com.example.ui.partner.PartnerRegistrationScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.schemes.GovernmentSchemesScreen
import com.example.ui.tea.TeaGardenScreen
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.trust.AdminTrustModerationScreen
import com.example.ui.theme.KrishakSathiTheme
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.data.remote.VideoLearningRepository
import com.example.ui.video.AdminVideoManagementScreen
import com.example.ui.video.VideoLearningCentreScreen
import com.example.ui.weather.WeatherScreen
import com.example.util.L10n
import com.example.util.LanguageManager
import com.example.util.isExpandedScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import androidx.compose.runtime.key
import androidx.credentials.ClearCredentialStateRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


enum class Screen {
    HOME,
    MY_CROPS,
    NURSERY_MARKET,
    ADD_CROP,
    CROP_DETAIL,
    KRISHAK_AI,
    GENERAL_AI,
    RESOURCE_LIBRARY,
    PLANT_PROBLEM,
    TEA_GARDEN,
    MEDICINE_HUB,
    DOSAGE_CALCULATOR,
    WEATHER,
    MARKET_PRICE,
    MARKETPLACE,
    SELL_PRODUCE,
    EXPENSE_PROFIT,
    EXPERT,
    PROFILE,
    GOVERNMENT_INFO,
    ABOUT_US,
    CONTACT_US,
    LEGAL_PAGES,
    FAQ,
    ADMIN_CONFIG,
    LEADING_FARMERS,
    LEADING_FARMERS_ADMIN,
    NURSERY_REGISTRATION,
    SELLER_DASHBOARD,
    ADMIN_NURSERIES,
    NURSERY_PUBLIC_PROFILE,
    ADMIN_ADD_CROP,
    COMMUNITY_FORUM,
    GOVERNMENT_SCHEMES,
    DISCOVERY_HUB,
    PLANT_KINGDOM,
    PLANT_DETAIL,
    NURSERY_DIRECTORY,
    NURSERY_MAP,
    MACHINERY_HUB,
    MACHINERY_DETAIL,
    MACHINERY_MANUFACTURERS,
    MACHINERY_DEALERS,
    ADMIN_MACHINERY,
    ADMIN_BANNERS,
    ADMIN_TRUST_MODERATION,
    LAND_RIGHTS,
    PARTNER_REGISTRATION,
    PARTNER_DASHBOARD,
    VIDEO_LEARNING,
    ADMIN_VIDEO_LEARNING
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure App Check before any Compose UI or Firebase operation
        configureAppCheck(this, intent)

        setContent {
            AppMainContent()
        }
    }
}

@Composable
fun AppMainContent() {
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val authScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        CustomCropRepository.ensureInitialized(context)
        NetworkConnectivityMonitor.startMonitoring(context)
    }
    var isDarkTheme by rememberSaveable { mutableStateOf(false) }
    var authNotice by remember { mutableStateOf<String?>(null) }
    var userSession by remember {
        val saved = UserSessionManager.getSession(context)
        val fbUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val reconciled = when {
            fbUser != null -> {
                val sameCloudUser = saved != null &&
                    saved.userId == fbUser.uid &&
                    saved.authProvider != UserSession.PROVIDER_FARMER_DIRECT
                val resolvedEmail = fbUser.email?.trim() ?: if (sameCloudUser) saved?.email else null
                val resolvedRole = when {
                    resolvedEmail?.equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true) == true -> UserSession.ROLE_ADMIN
                    sameCloudUser -> saved?.role ?: UserSession.ROLE_FARMER
                    else -> UserSession.ROLE_FARMER
                }
                UserSession(
                    userId = fbUser.uid,
                    userName = fbUser.displayName?.takeIf { it.isNotBlank() }
                        ?: (if (sameCloudUser) saved?.userName else null)
                        ?: "Farmer",
                    email = resolvedEmail,
                    phone = fbUser.phoneNumber ?: (if (sameCloudUser) saved?.phone else null),
                    role = resolvedRole,
                    authProvider = UserSession.PROVIDER_GOOGLE,
                    approvedRolesCsv = if (sameCloudUser) {
                        saved?.approvedRolesCsv ?: com.example.data.model.AppUserRole.FARMER_CUSTOMER.code
                    } else {
                        com.example.data.model.AppUserRole.FARMER_CUSTOMER.code
                    },
                    activeDashboardMode = if (sameCloudUser) {
                        saved?.activeDashboardMode ?: DashboardMode.FARMER_DASHBOARD.code
                    } else {
                        DashboardMode.FARMER_DASHBOARD.code
                    }
                ).also { UserSessionManager.saveSession(context, it) }
            }
            else -> saved
        }
        mutableStateOf(reconciled)
    }
    var currentLanguage by rememberSaveable { mutableStateOf(LanguageManager.getLanguage(context)) }
    var sawFirebaseUser by remember {
        mutableStateOf(runCatching { Firebase.auth.currentUser != null }.getOrDefault(false))
    }

    LaunchedEffect(userSession?.userId, userSession?.authProvider) {
        val active = userSession ?: return@LaunchedEffect
        if (!UserSessionManager.hasCloudFirebaseSession(context)) return@LaunchedEffect
        val fbUser = runCatching { Firebase.auth.currentUser }.getOrNull() ?: return@LaunchedEffect
        if (!NetworkConnectivityMonitor.isCurrentlyOnline()) return@LaunchedEffect
        try {
            fbUser.getIdToken(false).await()
            fbUser.reload().await()
            if (Firebase.auth.currentUser == null) {
                UserSessionManager.clearSession(context)
                userSession = null
                authNotice = when (currentLanguage) {
                    AppLanguage.BENGALI -> "আপনার অ্যাকাউন্ট সেশন মেয়াদোত্তীর্ণ বা বাতিল হয়েছে। অনুগ্রহ করে আবার সাইন ইন করুন।"
                    AppLanguage.HINDI -> "आपका खाता सत्र समाप्त या निरस्त हो गया है। कृपया पुनः साइन इन करें।"
                    AppLanguage.ENGLISH -> "Your cloud session expired or was revoked. Please sign in again."
                }
            } else {
                UserSessionManager.recordTokenValidated(context)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseAuthInvalidUserException) {
            UserSessionManager.clearSession(context)
            runCatching { Firebase.auth.signOut() }
            userSession = null
            authNotice = when (currentLanguage) {
                AppLanguage.BENGALI -> "অ্যাকাউন্ট সেশন বাতিল হয়েছে (${e.errorCode})। অনুগ্রহ করে পুনরায় Google দিয়ে প্রবেশ করুন।"
                AppLanguage.HINDI -> "खाता सत्र अमान्य है (${e.errorCode})। कृपया पुनः Google से साइन इन करें।"
                AppLanguage.ENGLISH -> "Session no longer valid (${e.errorCode}). Please reauthenticate with Google."
            }
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            UserSessionManager.clearSession(context)
            runCatching { Firebase.auth.signOut() }
            userSession = null
            authNotice = when (currentLanguage) {
                AppLanguage.BENGALI -> "নিরাপত্তা টোকেন যাচাই ব্যর্থ হয়েছে। অনুগ্রহ করে পুনরায় সাইন ইন করুন।"
                AppLanguage.HINDI -> "सुरक्षा टोकन सत्यापन विफल रहा। कृपया पुनः साइन इन करें।"
                AppLanguage.ENGLISH -> "Security credentials expired. Please reauthenticate with Google."
            }
        } catch (_: Exception) {
            // Keep existing persisted session when offline or temporarily unreachable
        }
    }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val fbUser = auth.currentUser
            if (fbUser != null) {
                sawFirebaseUser = true
                val existing = UserSessionManager.getSession(context)
                val sameCloudUser = existing != null &&
                    existing.userId == fbUser.uid &&
                    existing.authProvider != UserSession.PROVIDER_FARMER_DIRECT
                val resolvedEmail = fbUser.email?.trim() ?: if (sameCloudUser) existing?.email else null
                val resolvedRole = when {
                    resolvedEmail?.equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true) == true -> UserSession.ROLE_ADMIN
                    sameCloudUser -> existing?.role ?: UserSession.ROLE_FARMER
                    else -> UserSession.ROLE_FARMER
                }
                val s = UserSession(
                    userId = fbUser.uid,
                    userName = fbUser.displayName?.takeIf { it.isNotBlank() }
                        ?: (if (sameCloudUser) existing?.userName else null)
                        ?: "Farmer",
                    email = resolvedEmail,
                    phone = fbUser.phoneNumber ?: (if (sameCloudUser) existing?.phone else null),
                    role = resolvedRole,
                    authProvider = UserSession.PROVIDER_GOOGLE,
                    approvedRolesCsv = if (sameCloudUser) {
                        existing?.approvedRolesCsv ?: com.example.data.model.AppUserRole.FARMER_CUSTOMER.code
                    } else {
                        com.example.data.model.AppUserRole.FARMER_CUSTOMER.code
                    },
                    activeDashboardMode = if (sameCloudUser) {
                        existing?.activeDashboardMode ?: DashboardMode.FARMER_DASHBOARD.code
                    } else {
                        DashboardMode.FARMER_DASHBOARD.code
                    }
                )
                UserSessionManager.saveSession(context, s)
                userSession = s
                authNotice = null
            } else if (sawFirebaseUser && UserSessionManager.hasCloudFirebaseSession(context)) {
                sawFirebaseUser = false
                UserSessionManager.clearSession(context)
                userSession = null
                authNotice = when (currentLanguage) {
                    AppLanguage.BENGALI -> "সেশন শেষ হয়েছে। অনুগ্রহ করে আবার সাইন ইন করুন।"
                    AppLanguage.HINDI -> "सत्र समाप्त हो गया है। कृपया पुनः साइन इन करें।"
                    AppLanguage.ENGLISH -> "Your session ended. Please sign in again."
                }
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    KrishakSathiTheme(darkTheme = isDarkTheme) {
        if (userSession == null) {
            AuthGateScreen(
                currentLanguage = currentLanguage,
                onLanguageChange = { lang ->
                    LanguageManager.saveLanguage(context, lang)
                    currentLanguage = lang
                },
                onAuthSuccess = {
                    authNotice = null
                    userSession = UserSessionManager.getSession(context)
                },
                authNotice = authNotice
            )
        } else {
            key(userSession!!.userId) {
                FarmerNavHost(
                    userId = userSession!!.userId,
                    farmerName = userSession!!.userName,
                    currentLanguage = currentLanguage,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    onThemeChange = { isDarkTheme = it },
                    onLanguageChange = { lang ->
                        LanguageManager.saveLanguage(context, lang)
                        currentLanguage = lang
                    },
                    onSignOut = {
                        sawFirebaseUser = false
                        authNotice = null
                        UserSessionManager.clearSession(context)
                        try {
                            Firebase.auth.signOut()
                        } catch (_: Exception) {
                        }
                        authScope.launch {
                            runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
                        }
                        userSession = null
                    },
                    isCloudConnected = UserSessionManager.hasCloudFirebaseSession(context),
                    onRefreshSession = {
                        userSession = UserSessionManager.getSession(context)
                    }
                )
            }
        }
    }
}

private fun routeToScreen(route: String): Screen = when (route) {
    HashRoutes.HOME -> Screen.HOME
    HashRoutes.AI_ASSISTANT -> Screen.GENERAL_AI
    HashRoutes.MARKETPLACE -> Screen.MARKETPLACE
    HashRoutes.SELL_PRODUCE -> Screen.SELL_PRODUCE
    HashRoutes.RESOURCE_LIBRARY -> Screen.RESOURCE_LIBRARY
    HashRoutes.MY_CROPS -> Screen.MY_CROPS
    HashRoutes.ADD_CROP -> Screen.ADD_CROP
    HashRoutes.CROP_DETAIL -> Screen.CROP_DETAIL
    HashRoutes.PROFILE -> Screen.PROFILE
    HashRoutes.FARMER_AI -> Screen.KRISHAK_AI
    HashRoutes.NURSERY_MARKET -> Screen.NURSERY_MARKET
    HashRoutes.MEDICINE_HUB -> Screen.MEDICINE_HUB
    HashRoutes.PLANT_PROBLEM -> Screen.PLANT_PROBLEM
    HashRoutes.TEA_GARDEN -> Screen.TEA_GARDEN
    HashRoutes.WEATHER -> Screen.WEATHER
    HashRoutes.MARKET_PRICE -> Screen.MARKET_PRICE
    HashRoutes.DOSAGE_CALCULATOR -> Screen.DOSAGE_CALCULATOR
    HashRoutes.EXPENSE_PROFIT -> Screen.EXPENSE_PROFIT
    HashRoutes.EXPERT -> Screen.EXPERT
    HashRoutes.GOVT_INFO -> Screen.GOVERNMENT_INFO
    HashRoutes.ABOUT_US -> Screen.ABOUT_US
    HashRoutes.CONTACT_US -> Screen.CONTACT_US
    HashRoutes.LEGAL_PAGES -> Screen.LEGAL_PAGES
    HashRoutes.FAQ -> Screen.FAQ
    HashRoutes.ADMIN_CONFIG -> Screen.ADMIN_CONFIG
    HashRoutes.LEADING_FARMERS -> Screen.LEADING_FARMERS
    HashRoutes.LEADING_FARMERS_ADMIN -> Screen.LEADING_FARMERS_ADMIN
    HashRoutes.NURSERY_REGISTRATION -> Screen.NURSERY_REGISTRATION
    HashRoutes.SELLER_DASHBOARD -> Screen.SELLER_DASHBOARD
    HashRoutes.ADMIN_NURSERIES -> Screen.ADMIN_NURSERIES
    HashRoutes.NURSERY_PUBLIC_PROFILE -> Screen.NURSERY_PUBLIC_PROFILE
    HashRoutes.ADMIN_ADD_CROP -> Screen.ADMIN_ADD_CROP
    HashRoutes.COMMUNITY_FORUM -> Screen.COMMUNITY_FORUM
    HashRoutes.GOVT_SCHEMES -> Screen.GOVERNMENT_SCHEMES
    HashRoutes.DISCOVERY_HUB -> Screen.DISCOVERY_HUB
    HashRoutes.PLANT_KINGDOM -> Screen.PLANT_KINGDOM
    HashRoutes.NURSERY_DIRECTORY -> Screen.NURSERY_DIRECTORY
    HashRoutes.NURSERY_MAP -> Screen.NURSERY_MAP
    HashRoutes.MACHINERY_HUB -> Screen.MACHINERY_HUB
    HashRoutes.MACHINERY_DETAIL -> Screen.MACHINERY_DETAIL
    HashRoutes.MACHINERY_MANUFACTURERS -> Screen.MACHINERY_MANUFACTURERS
    HashRoutes.MACHINERY_DEALERS -> Screen.MACHINERY_DEALERS
    HashRoutes.ADMIN_MACHINERY -> Screen.ADMIN_MACHINERY
    HashRoutes.ADMIN_BANNERS -> Screen.ADMIN_BANNERS
    HashRoutes.ADMIN_TRUST_MODERATION -> Screen.ADMIN_TRUST_MODERATION
    HashRoutes.LAND_RIGHTS -> Screen.LAND_RIGHTS
    HashRoutes.PARTNER_REGISTRATION -> Screen.PARTNER_REGISTRATION
    HashRoutes.PARTNER_DASHBOARD -> Screen.PARTNER_DASHBOARD
    HashRoutes.VIDEO_LEARNING -> Screen.VIDEO_LEARNING
    HashRoutes.ADMIN_VIDEO_LEARNING -> Screen.ADMIN_VIDEO_LEARNING
    else -> Screen.HOME
}

@Composable
fun FarmerNavHost(
    userId: String,
    farmerName: String,
    currentLanguage: AppLanguage,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onThemeChange: (Boolean) -> Unit = {},
    onLanguageChange: (AppLanguage) -> Unit,
    onSignOut: () -> Unit,
    isCloudConnected: Boolean = true,
    onRefreshSession: () -> Unit = {}
) {
    val hashRouter = rememberHashRouter(HashRoutes.HOME)
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var activeCropDetail by remember { mutableStateOf<MasterCrop?>(null) }
    var activePlantDetailId by remember { mutableStateOf<String?>(null) }
    var activeMachineryDetail by remember { mutableStateOf<MachineryProduct?>(null) }
    var selectedCropForCalc by remember { mutableStateOf<String?>(null) }
    var showPostLoginLanguagePrompt by rememberSaveable { mutableStateOf(true) }
    var showGoogleSyncPrompt by remember { mutableStateOf(false) }
    var activeLegalTab by remember { mutableStateOf(LegalTab.PRIVACY) }
    var activeLandTopic by remember { mutableStateOf(LandTopic.DOCUMENTS) }
    var activeSchemeCategory by remember { mutableStateOf<SchemeCategory?>(null) }
    var activeSchemeQuery by remember { mutableStateOf("") }
    var activeMachineryCategoryCode by remember { mutableStateOf<String?>(null) }
    var activeMachineryTab by remember { mutableStateOf(0) }
    var activeAiLessonContext by remember { mutableStateOf<String?>(null) }
    var activeAiPromptSeed by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val coroutineScope = rememberCoroutineScope()
    val networkStatus by NetworkConnectivityMonitor.status.collectAsState()
    val nurseryAppRepo = remember { NurseryApplicationRepository(context) }
    val partnerRoleRepo = remember { PartnerRoleRepository(context) }
    LaunchedEffect(userId, isCloudConnected) {
        if (isCloudConnected) {
            partnerRoleRepo.observeUserRoleMembership(userId).collect {}
        }
    }
    var selectedPublicNurseryId by remember { mutableStateOf("nurs_sil_01") }
    val leadingFarmerRepository = remember {
        LeadingFarmerRepository(AppDatabase.getInstance(context).leadingFarmerDao())
    }
    val expandedLayout = isExpandedScreen()

    fun launchGoogleCloudSync() {
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                onRefreshSession()
                Toast.makeText(
                    context,
                    L10n.t("cloud_sync_connected_desc", currentLanguage),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onAuthError = { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            },
            scope = coroutineScope,
            currentLanguage = currentLanguage
        )
    }

    fun navigateToRoute(route: String, clearTop: Boolean = false) {
        hashRouter.navigate(route, clearTop = clearTop)
        currentScreen = routeToScreen(hashRouter.currentRoute)
    }

    fun handleBack(fallbackRoute: String = HashRoutes.HOME) {
        if (hashRouter.canGoBack()) {
            hashRouter.goBack()
            currentScreen = routeToScreen(hashRouter.currentRoute)
        } else {
            navigateToRoute(fallbackRoute, clearTop = true)
        }
    }

    if (showPostLoginLanguagePrompt) {
        PostLoginLanguageDialog(
            currentLanguage = currentLanguage,
            onSelectLanguage = { lang -> onLanguageChange(lang) },
            onConfirm = { showPostLoginLanguagePrompt = false }
        )
    }

    if (showGoogleSyncPrompt) {
        GoogleCloudSyncPromptDialog(
            language = currentLanguage,
            onContinueOffline = { showGoogleSyncPrompt = false },
            onConnectGoogle = { launchGoogleCloudSync() },
            onDismiss = { showGoogleSyncPrompt = false }
        )
    }

    val showBottomBar = currentScreen in listOf(
        Screen.HOME,
        Screen.GENERAL_AI,
        Screen.MARKETPLACE,
        Screen.RESOURCE_LIBRARY,
        Screen.PROFILE
    )

    val navItems = listOf(
        Triple(Screen.HOME, HashRoutes.HOME, Triple(Icons.Filled.Home, Icons.Outlined.Home, L10n.t("home", currentLanguage))) to "nav_item_home",
        Triple(Screen.RESOURCE_LIBRARY, HashRoutes.RESOURCE_LIBRARY, Triple(Icons.Filled.Explore, Icons.Outlined.Explore, L10n.t("nav_explore", currentLanguage))) to "nav_item_library",
        Triple(Screen.GENERAL_AI, HashRoutes.AI_ASSISTANT, Triple(Icons.Filled.SmartToy, Icons.Outlined.SmartToy, L10n.t("ai_assistant", currentLanguage))) to "nav_item_ai_assistant",
        Triple(Screen.MARKETPLACE, HashRoutes.MARKETPLACE, Triple(Icons.Filled.LocalMall, Icons.Outlined.LocalMall, L10n.t("nav_market", currentLanguage))) to "nav_item_sell",
        Triple(Screen.PROFILE, HashRoutes.PROFILE, Triple(Icons.Filled.Person, Icons.Outlined.Person, L10n.t("profile", currentLanguage))) to "nav_item_profile"
    )
    val navBarContainer = Color(0xFF064E3B)
    val navEmeraldActive = Color(0xFF15803D)
    val navGoldHighlight = Color(0xFFD4AF37)
    val navInactiveNeutral = Color(0xFFC5D8C4)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (showBottomBar && !expandedLayout) {
                Column {
                    HorizontalDivider(thickness = 1.dp, color = navGoldHighlight.copy(alpha = 0.28f))
                    NavigationBar(
                        containerColor = navBarContainer,
                        tonalElevation = 0.dp
                    ) {
                        navItems.forEach { (itemData, tag) ->
                            val (screen, route, iconLabel) = itemData
                            val (selectedIcon, unselectedIcon, label) = iconLabel
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { navigateToRoute(route) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        maxLines = 2,
                                        textAlign = TextAlign.Center
                                    )
                                },
                                alwaysShowLabel = true,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = navGoldHighlight,
                                    unselectedIconColor = navInactiveNeutral,
                                    unselectedTextColor = navInactiveNeutral,
                                    indicatorColor = navEmeraldActive
                                ),
                                modifier = Modifier.testTag(tag)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        BackHandler(enabled = currentScreen != Screen.HOME) {
            handleBack(HashRoutes.HOME)
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            if (showBottomBar && expandedLayout) {
                NavigationRail(
                    containerColor = navBarContainer,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    navItems.forEach { (itemData, tag) ->
                        val (screen, route, iconLabel) = itemData
                        val (selectedIcon, unselectedIcon, label) = iconLabel
                        val isSelected = currentScreen == screen
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { navigateToRoute(route) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = navGoldHighlight,
                                unselectedIconColor = navInactiveNeutral,
                                unselectedTextColor = navInactiveNeutral,
                                indicatorColor = navEmeraldActive
                            ),
                            modifier = Modifier.testTag(tag)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                VerticalDivider(thickness = 1.dp, color = navGoldHighlight.copy(alpha = 0.28f))
            }

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                when (currentScreen) {
                    Screen.HOME -> {
                        SimpleHomeScreen(
                            farmerName = farmerName,
                            language = currentLanguage,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = onToggleTheme,
                            onLanguageChange = onLanguageChange,
                            onCardClick = { cardId ->
                                when (cardId) {
                                    "general_ai" -> navigateToRoute(HashRoutes.AI_ASSISTANT)
                                    "resource_library" -> navigateToRoute(HashRoutes.RESOURCE_LIBRARY)
                                    "video_learning" -> navigateToRoute(HashRoutes.VIDEO_LEARNING)
                                    "crops" -> navigateToRoute(HashRoutes.MY_CROPS)
                                    "nursery" -> navigateToRoute(HashRoutes.NURSERY_MARKET)
                                    "ai" -> {
                                        selectedCropForCalc = null
                                        activeAiLessonContext = null
                                        activeAiPromptSeed = null
                                        navigateToRoute(HashRoutes.FARMER_AI)
                                    }
                                    "problem" -> {
                                        selectedCropForCalc = null
                                        navigateToRoute(HashRoutes.PLANT_PROBLEM)
                                    }
                                    "tea" -> navigateToRoute(HashRoutes.TEA_GARDEN)
                                    "medicine", "weed_control" -> navigateToRoute(HashRoutes.MEDICINE_HUB)
                                    "spray_dose" -> {
                                        selectedCropForCalc = null
                                        navigateToRoute(HashRoutes.DOSAGE_CALCULATOR)
                                    }
                                    "irrigation_advice" -> {
                                        activeMachineryCategoryCode = "IRRIGATION_WATER"
                                        activeMachineryTab = 0
                                        navigateToRoute(HashRoutes.MACHINERY_HUB)
                                    }
                                    "weather" -> navigateToRoute(HashRoutes.WEATHER)
                                    "market" -> navigateToRoute(HashRoutes.MARKET_PRICE)
                                    "sell" -> navigateToRoute(HashRoutes.MARKETPLACE)
                                    "orders" -> navigateToRoute(HashRoutes.MARKETPLACE)
                                    "calendar" -> navigateToRoute(HashRoutes.MY_CROPS)
                                    "expense" -> navigateToRoute(HashRoutes.EXPENSE_PROFIT)
                                    "expert" -> navigateToRoute(HashRoutes.EXPERT)
                                    "leading_farmers" -> navigateToRoute(HashRoutes.LEADING_FARMERS)
                                    "govt_info" -> navigateToRoute(HashRoutes.GOVT_INFO)
                                    "govt_schemes" -> {
                                        activeSchemeCategory = null
                                        activeSchemeQuery = ""
                                        navigateToRoute(HashRoutes.GOVT_SCHEMES)
                                    }
                                    "scheme_subsidy" -> {
                                        activeSchemeCategory = SchemeCategory.MACHINERY_EQUIPMENT
                                        activeSchemeQuery = ""
                                        navigateToRoute(HashRoutes.GOVT_SCHEMES)
                                    }
                                    "scheme_kisan_id" -> {
                                        activeSchemeCategory = SchemeCategory.FINANCIAL_ASSISTANCE
                                        activeSchemeQuery = ""
                                        navigateToRoute(HashRoutes.GOVT_SCHEMES)
                                    }
                                    "scheme_insurance" -> {
                                        activeSchemeCategory = SchemeCategory.CROP_INSURANCE
                                        activeSchemeQuery = ""
                                        navigateToRoute(HashRoutes.GOVT_SCHEMES)
                                    }
                                    "scheme_apply" -> {
                                        activeSchemeCategory = SchemeCategory.WEST_BENGAL_SCHEMES
                                        activeSchemeQuery = ""
                                        navigateToRoute(HashRoutes.GOVT_SCHEMES)
                                    }
                                    "scheme_loan" -> {
                                        activeSchemeCategory = SchemeCategory.KCC_LOANS
                                        activeSchemeQuery = ""
                                        navigateToRoute(HashRoutes.GOVT_SCHEMES)
                                    }
                                    "land_documents" -> {
                                        activeLandTopic = LandTopic.DOCUMENTS
                                        navigateToRoute(HashRoutes.LAND_RIGHTS)
                                    }
                                    "land_records" -> {
                                        activeLandTopic = LandTopic.RECORDS
                                        navigateToRoute(HashRoutes.LAND_RIGHTS)
                                    }
                                    "land_disputes" -> {
                                        activeLandTopic = LandTopic.DISPUTES
                                        navigateToRoute(HashRoutes.LAND_RIGHTS)
                                    }
                                    "land_buysell" -> {
                                        activeLandTopic = LandTopic.BUY_SELL
                                        navigateToRoute(HashRoutes.LAND_RIGHTS)
                                    }
                                    "land_possession" -> {
                                        activeLandTopic = LandTopic.POSSESSION_MUTATION
                                        navigateToRoute(HashRoutes.LAND_RIGHTS)
                                    }
                                    "land_inheritance" -> {
                                        activeLandTopic = LandTopic.INHERITANCE
                                        navigateToRoute(HashRoutes.LAND_RIGHTS)
                                    }
                                    "community_forum" -> navigateToRoute(HashRoutes.COMMUNITY_FORUM)
                                    "machinery" -> {
                                        activeMachineryCategoryCode = null
                                        activeMachineryTab = 0
                                        navigateToRoute(HashRoutes.MACHINERY_HUB)
                                    }
                                    "machinery_tractor" -> {
                                        activeMachineryCategoryCode = "TRACTORS_LAND_PREP"
                                        activeMachineryTab = 0
                                        navigateToRoute(HashRoutes.MACHINERY_HUB)
                                    }
                                    "machinery_sprayer" -> {
                                        activeMachineryCategoryCode = "CROP_PROTECTION_SPRAYING"
                                        activeMachineryTab = 0
                                        navigateToRoute(HashRoutes.MACHINERY_HUB)
                                    }
                                    "machinery_irrigation" -> {
                                        activeMachineryCategoryCode = "IRRIGATION_WATER"
                                        activeMachineryTab = 0
                                        navigateToRoute(HashRoutes.MACHINERY_HUB)
                                    }
                                    "machinery_rent" -> {
                                        activeMachineryCategoryCode = null
                                        activeMachineryTab = 1
                                        navigateToRoute(HashRoutes.MACHINERY_HUB)
                                    }
                                    "machinery_dealers" -> navigateToRoute(HashRoutes.MACHINERY_DEALERS)
                                    else -> navigateToRoute(HashRoutes.HOME)
                                }
                            },
                            onVoiceClick = {
                                selectedCropForCalc = null
                                navigateToRoute(HashRoutes.FARMER_AI)
                            },
                            onProfileClick = { navigateToRoute(HashRoutes.PROFILE) },
                            onOpenCropDetail = { crop ->
                                activeCropDetail = crop
                                navigateToRoute(HashRoutes.CROP_DETAIL)
                            },
                            onNavigateAbout = { navigateToRoute(HashRoutes.ABOUT_US) },
                            onNavigateContact = { navigateToRoute(HashRoutes.CONTACT_US) },
                            onNavigateLegal = { tab ->
                                activeLegalTab = tab
                                navigateToRoute(HashRoutes.LEGAL_PAGES)
                            },
                            onNavigateGovtInfo = { navigateToRoute(HashRoutes.GOVT_INFO) },
                            onNavigateExperts = { navigateToRoute(HashRoutes.EXPERT) },
                            onNavigateFaq = { navigateToRoute(HashRoutes.FAQ) },
                            onNavigateLeadingFarmers = { navigateToRoute(HashRoutes.LEADING_FARMERS) },
                            userId = userId,
                            networkStatus = networkStatus,
                            isCloudConnected = isCloudConnected,
                            onConnectGoogleSync = { showGoogleSyncPrompt = true },
                            onAddCropClick = { navigateToRoute(HashRoutes.ADD_CROP) },
                            onOpenPlantKingdom = { navigateToRoute(HashRoutes.PLANT_KINGDOM) },
                            onOpenPartnerDashboard = {
                                if (UserSessionManager.switchDashboardMode(context, DashboardMode.PARTNER_DASHBOARD)) {
                                    onRefreshSession()
                                    navigateToRoute(HashRoutes.PARTNER_DASHBOARD)
                                } else {
                                    navigateToRoute(HashRoutes.PARTNER_REGISTRATION)
                                }
                            }
                        )
                    }
                    Screen.GENERAL_AI -> {
                        GeneralAiScreen(
                            userId = userId,
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenExperts = { navigateToRoute(HashRoutes.EXPERT) }
                        )
                    }
                    Screen.RESOURCE_LIBRARY -> {
                        ResourceLibraryScreen(
                            currentLanguage = currentLanguage,
                            onNavigateRoute = { route -> navigateToRoute(route) },
                            onBack = { handleBack(HashRoutes.HOME) }
                        )
                    }
                    Screen.MY_CROPS -> {
                        MyCropsScreen(
                            currentLanguage = currentLanguage,
                            userId = userId,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onAddCrop = { navigateToRoute(HashRoutes.ADD_CROP) },
                            onSelectCropDetail = { crop ->
                                activeCropDetail = crop
                                navigateToRoute(HashRoutes.CROP_DETAIL)
                            },
                            onOpenAdminAddCrop = { navigateToRoute(HashRoutes.ADMIN_ADD_CROP) }
                        )
                    }
                    Screen.NURSERY_MARKET -> {
                        NurserySeedMarketScreen(
                            currentLanguage = currentLanguage,
                            userId = userId,
                            farmerName = farmerName,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenNurseryRegistration = { navigateToRoute(HashRoutes.NURSERY_REGISTRATION) },
                            onOpenNurseryProfile = { nId ->
                                selectedPublicNurseryId = nId
                                navigateToRoute(HashRoutes.NURSERY_PUBLIC_PROFILE)
                            },
                            onOpenPlantKingdom = { navigateToRoute(HashRoutes.PLANT_KINGDOM) },
                            onOpenNurseryDirectory = { navigateToRoute(HashRoutes.NURSERY_DIRECTORY) },
                            onOpenNurseryMap = { navigateToRoute(HashRoutes.NURSERY_MAP) }
                        )
                    }
                    Screen.ADD_CROP -> {
                        AddFarmerCropScreen(
                            currentLanguage = currentLanguage,
                            userId = userId,
                            onBack = { handleBack(HashRoutes.MY_CROPS) },
                            onCropAdded = { handleBack(HashRoutes.MY_CROPS) }
                        )
                    }
                    Screen.CROP_DETAIL -> {
                        val crop = activeCropDetail
                        if (crop != null) {
                            CropDetailScreen(
                                crop = crop,
                                currentLanguage = currentLanguage,
                                onBack = { handleBack(HashRoutes.MY_CROPS) },
                                onOpenCalculator = { cName ->
                                    selectedCropForCalc = cName
                                    navigateToRoute(HashRoutes.DOSAGE_CALCULATOR)
                                },
                                onDiagnosePhoto = { cName ->
                                    selectedCropForCalc = cName
                                    navigateToRoute(HashRoutes.PLANT_PROBLEM)
                                },
                                onCheckMarketPrice = { _ ->
                                    navigateToRoute(HashRoutes.MARKET_PRICE)
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                handleBack(HashRoutes.MY_CROPS)
                            }
                        }
                    }
                    Screen.KRISHAK_AI -> {
                        KrishakAiScreen(
                            currentLanguage = currentLanguage,
                            selectedCropContext = selectedCropForCalc,
                            onBack = {
                                activeAiLessonContext = null
                                activeAiPromptSeed = null
                                handleBack(HashRoutes.HOME)
                            },
                            userId = userId,
                            onOpenExperts = { navigateToRoute(HashRoutes.EXPERT) },
                            initialExtraContext = activeAiLessonContext,
                            initialPromptSeed = activeAiPromptSeed
                        )
                    }
                    Screen.PLANT_PROBLEM -> {
                        PlantProblemCheckerScreen(
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenCalculator = { cName ->
                                selectedCropForCalc = cName
                                navigateToRoute(HashRoutes.DOSAGE_CALCULATOR)
                            },
                            onLanguageChange = onLanguageChange,
                            onOpenProfile = { navigateToRoute(HashRoutes.PROFILE) },
                            onOpenVoiceAssistant = {
                                selectedCropForCalc = null
                                navigateToRoute(HashRoutes.FARMER_AI)
                            },
                            userId = userId,
                            initialCropId = selectedCropForCalc,
                            onOpenExperts = { navigateToRoute(HashRoutes.EXPERT) }
                        )
                    }
                    Screen.TEA_GARDEN -> {
                        TeaGardenScreen(
                            currentLanguage = currentLanguage,
                            userId = userId,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenSprayCalculator = {
                                selectedCropForCalc = "Tea"
                                navigateToRoute(HashRoutes.DOSAGE_CALCULATOR)
                            }
                        )
                    }
                    Screen.MEDICINE_HUB -> {
                        CropMedicineHubScreen(
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenCalculator = { cName ->
                                selectedCropForCalc = cName
                                navigateToRoute(HashRoutes.DOSAGE_CALCULATOR)
                            },
                            onContactExpert = { navigateToRoute(HashRoutes.EXPERT) }
                        )
                    }
                    Screen.DOSAGE_CALCULATOR -> {
                        DosageCalculatorScreen(
                            currentLanguage = currentLanguage,
                            initialCropName = selectedCropForCalc,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onContactExpert = { navigateToRoute(HashRoutes.EXPERT) }
                        )
                    }
                    Screen.WEATHER -> {
                        WeatherScreen(
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) }
                        )
                    }
                    Screen.MARKET_PRICE -> {
                        MarketPriceScreen(
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) }
                        )
                    }
                    Screen.MARKETPLACE -> {
                        MarketplaceScreen(
                            currentLanguage = currentLanguage,
                            userId = userId,
                            farmerName = farmerName,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onSellClick = { navigateToRoute(HashRoutes.SELL_PRODUCE) },
                            onOpenMachineryHub = { navigateToRoute(HashRoutes.MACHINERY_HUB) }
                        )
                    }
                    Screen.SELL_PRODUCE -> {
                        SellProduceScreen(
                            currentLanguage = currentLanguage,
                            userId = userId,
                            farmerName = farmerName,
                            onBack = { handleBack(HashRoutes.MARKETPLACE) },
                            onSuccess = { handleBack(HashRoutes.MARKETPLACE) }
                        )
                    }
                    Screen.EXPENSE_PROFIT -> {
                        ExpenseProfitScreen(
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) }
                        )
                    }
                    Screen.EXPERT -> {
                        ExpertScreen(
                            currentLanguage = currentLanguage,
                            userId = userId,
                            farmerName = farmerName,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenAdminConfig = { navigateToRoute(HashRoutes.ADMIN_CONFIG) }
                        )
                    }
                    Screen.PROFILE -> {
                        ProfileScreen(
                            currentLanguage = currentLanguage,
                            userId = userId,
                            isDarkTheme = isDarkTheme,
                            onThemeChange = onThemeChange,
                            onLanguageChange = onLanguageChange,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onSignOut = onSignOut,
                            onOpenNurseryRegistration = { navigateToRoute(HashRoutes.NURSERY_REGISTRATION) },
                            onOpenSellerDashboard = { navigateToRoute(HashRoutes.SELLER_DASHBOARD) },
                            onOpenAdminNurseryManagement = { navigateToRoute(HashRoutes.ADMIN_NURSERIES) },
                            onOpenAdminTrustModeration = { navigateToRoute(HashRoutes.ADMIN_TRUST_MODERATION) },
                            onOpenGrievanceRedressal = {
                                activeLegalTab = LegalTab.GRIEVANCE_REDRESSAL
                                navigateToRoute(HashRoutes.LEGAL_PAGES)
                            },
                            onCloudSessionUpdated = onRefreshSession,
                            onOpenPartnerRegistration = { navigateToRoute(HashRoutes.PARTNER_REGISTRATION) },
                            onSwitchDashboardMode = { targetMode ->
                                val switched = UserSessionManager.switchDashboardMode(context, targetMode)
                                if (switched) {
                                    onRefreshSession()
                                    if (targetMode == DashboardMode.PARTNER_DASHBOARD) {
                                        navigateToRoute(HashRoutes.PARTNER_DASHBOARD)
                                    } else {
                                        navigateToRoute(HashRoutes.HOME, clearTop = true)
                                    }
                                } else {
                                    navigateToRoute(HashRoutes.PARTNER_REGISTRATION)
                                }
                            }
                        )
                    }
                    Screen.GOVERNMENT_INFO -> {
                        GovernmentInfoScreen(
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenAdminConfig = { navigateToRoute(HashRoutes.ADMIN_CONFIG) }
                        )
                    }
                    Screen.ABOUT_US -> {
                        AboutUsScreen(
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenDiscoveryHub = { navigateToRoute(HashRoutes.DISCOVERY_HUB) }
                        )
                    }
                    Screen.CONTACT_US -> {
                        ContactUsScreen(
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) }
                        )
                    }
                    Screen.LEGAL_PAGES -> {
                        LegalPagesScreen(
                            currentLanguage = currentLanguage,
                            initialTab = activeLegalTab,
                            onBack = { handleBack(HashRoutes.HOME) }
                        )
                    }
                    Screen.FAQ -> {
                        FaqScreen(
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) }
                        )
                    }
                    Screen.ADMIN_CONFIG -> {
                        if (UserSessionManager.isAdmin(context)) {
                            AdminConfigScreen(
                                currentLanguage = currentLanguage,
                                onBack = { handleBack(HashRoutes.GOVT_INFO) },
                                onNavigateLeadingFarmersAdmin = { navigateToRoute(HashRoutes.LEADING_FARMERS_ADMIN) },
                                onNavigateAdminAddCrop = { navigateToRoute(HashRoutes.ADMIN_ADD_CROP) },
                                onNavigateBannerAdmin = { navigateToRoute(HashRoutes.ADMIN_BANNERS) },
                                onNavigateMachineryAdmin = { navigateToRoute(HashRoutes.ADMIN_MACHINERY) },
                                onNavigateTrustModeration = { navigateToRoute(HashRoutes.ADMIN_TRUST_MODERATION) },
                                onNavigateVideoAdmin = { navigateToRoute(HashRoutes.ADMIN_VIDEO_LEARNING) }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                Toast.makeText(context, "Admin sign-in required.", Toast.LENGTH_SHORT).show()
                                handleBack(HashRoutes.HOME)
                            }
                        }
                    }
                    Screen.LEADING_FARMERS -> {
                        LeadingFarmersScreen(
                            repository = leadingFarmerRepository,
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenAdmin = { navigateToRoute(HashRoutes.LEADING_FARMERS_ADMIN) }
                        )
                    }
                    Screen.LEADING_FARMERS_ADMIN -> {
                        if (UserSessionManager.isAdmin(context)) {
                            LeadingFarmersAdminScreen(
                                repository = leadingFarmerRepository,
                                currentLanguage = currentLanguage,
                                onBack = { handleBack(HashRoutes.LEADING_FARMERS) }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                Toast.makeText(context, "Admin sign-in required.", Toast.LENGTH_SHORT).show()
                                handleBack(HashRoutes.HOME)
                            }
                        }
                    }
                    Screen.NURSERY_REGISTRATION -> {
                        val sess = UserSessionManager.getSession(context)
                        NurseryRegistrationScreen(
                            userId = userId,
                            applicantName = farmerName,
                            applicantPhone = sess?.phone,
                            applicantEmail = sess?.email,
                            currentLanguage = currentLanguage,
                            repository = nurseryAppRepo,
                            onBack = { handleBack(HashRoutes.PROFILE) },
                            onNavigateToDashboard = { navigateToRoute(HashRoutes.SELLER_DASHBOARD) }
                        )
                    }
                    Screen.SELLER_DASHBOARD -> {
                        NurserySellerDashboardScreen(
                            userId = userId,
                            currentLanguage = currentLanguage,
                            repository = nurseryAppRepo,
                            onBackToHome = { handleBack(HashRoutes.HOME) },
                            onOpenPublicStore = { app ->
                                selectedPublicNurseryId = app.id
                                navigateToRoute(HashRoutes.NURSERY_PUBLIC_PROFILE)
                            },
                            onLanguageChange = onLanguageChange
                        )
                    }
                    Screen.ADMIN_NURSERIES -> {
                        if (UserSessionManager.isAdmin(context)) {
                            AdminNurseryManagementScreen(
                                repository = nurseryAppRepo,
                                currentLanguage = currentLanguage,
                                onNavigateBack = { handleBack(HashRoutes.PROFILE) }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                Toast.makeText(context, "Admin sign-in required.", Toast.LENGTH_SHORT).show()
                                handleBack(HashRoutes.HOME)
                            }
                        }
                    }
                    Screen.NURSERY_PUBLIC_PROFILE -> {
                        NurseryPublicProfileScreen(
                            nurseryId = selectedPublicNurseryId,
                            applicationRepository = nurseryAppRepo,
                            currentLanguage = currentLanguage,
                            onNavigateBack = { handleBack(HashRoutes.NURSERY_MARKET) }
                        )
                    }
                    Screen.ADMIN_ADD_CROP -> {
                        if (UserSessionManager.isAdmin(context)) {
                            AdminAddMasterCropScreen(
                                currentLanguage = currentLanguage,
                                onBack = { handleBack(HashRoutes.MY_CROPS) },
                                onCropSaved = { handleBack(HashRoutes.MY_CROPS) }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                Toast.makeText(context, "Admin sign-in required.", Toast.LENGTH_SHORT).show()
                                handleBack(HashRoutes.HOME)
                            }
                        }
                    }
                    Screen.COMMUNITY_FORUM -> {
                        CommunityForumScreen(
                            userId = userId,
                            userName = farmerName,
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) }
                        )
                    }
                    Screen.GOVERNMENT_SCHEMES -> {
                        GovernmentSchemesScreen(
                            currentLanguage = currentLanguage,
                            userId = userId,
                            onBack = { handleBack(HashRoutes.HOME) },
                            initialCategory = activeSchemeCategory,
                            initialSearchQuery = activeSchemeQuery
                        )
                    }
                    Screen.DISCOVERY_HUB -> {
                        PublicDiscoveryHubScreen(
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onNavigateRoute = { route -> navigateToRoute(route) },
                            onOpenCropDetailById = { cId ->
                                activeCropDetail = CropMasterDatabase.getCropOrDefault(cId, null)
                                navigateToRoute(HashRoutes.CROP_DETAIL)
                            }
                        )
                    }
                    Screen.PLANT_KINGDOM -> {
                        PlantKingdomLibraryScreen(
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenPlantDetail = { plantId ->
                                activePlantDetailId = plantId
                                currentScreen = Screen.PLANT_DETAIL
                            },
                            onOpenNurseryDirectory = { navigateToRoute(HashRoutes.NURSERY_DIRECTORY) },
                            onOpenNurseryMap = { navigateToRoute(HashRoutes.NURSERY_MAP) }
                        )
                    }
                    Screen.PLANT_DETAIL -> {
                        PlantDetailScreen(
                            plantId = activePlantDetailId ?: "rose",
                            currentLanguage = currentLanguage,
                            onBack = { currentScreen = Screen.PLANT_KINGDOM },
                            onOpenDirectory = { navigateToRoute(HashRoutes.NURSERY_DIRECTORY) },
                            onOpenMarketplace = { navigateToRoute(HashRoutes.NURSERY_MARKET) }
                        )
                    }
                    Screen.NURSERY_DIRECTORY -> {
                        NurseryDirectoryScreen(
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenMap = { navigateToRoute(HashRoutes.NURSERY_MAP) },
                            onOpenPlantKingdom = { navigateToRoute(HashRoutes.PLANT_KINGDOM) }
                        )
                    }
                    Screen.NURSERY_MAP -> {
                        NurseryMapScreen(
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.NURSERY_DIRECTORY) }
                        )
                    }
                    Screen.MACHINERY_HUB -> {
                        MachineryHubScreen(
                            currentLanguage = currentLanguage,
                            userId = userId,
                            farmerName = farmerName,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenDealers = { navigateToRoute(HashRoutes.MACHINERY_DEALERS) },
                            onOpenManufacturers = { navigateToRoute(HashRoutes.MACHINERY_MANUFACTURERS) },
                            onOpenProductDetail = { prod ->
                                activeMachineryDetail = prod
                                navigateToRoute(HashRoutes.MACHINERY_DETAIL)
                            },
                            onOpenFullAi = {
                                selectedCropForCalc = null
                                navigateToRoute(HashRoutes.FARMER_AI)
                            },
                            initialCategoryCode = activeMachineryCategoryCode,
                            initialTabIndex = activeMachineryTab
                        )
                    }
                    Screen.MACHINERY_DETAIL -> {
                        val selectedProduct = activeMachineryDetail
                        if (selectedProduct != null) {
                            MachineryProductDetailScreen(
                                machine = selectedProduct,
                                currentLanguage = currentLanguage,
                                userId = userId,
                                farmerName = farmerName,
                                onOpenDealers = { navigateToRoute(HashRoutes.MACHINERY_DEALERS) },
                                onBack = { handleBack(HashRoutes.MACHINERY_HUB) }
                            )
                        } else {
                            LaunchedEffect(Unit) { handleBack(HashRoutes.MACHINERY_HUB) }
                        }
                    }
                    Screen.MACHINERY_MANUFACTURERS -> {
                        MachineryManufacturersScreen(
                            currentLanguage = currentLanguage,
                            onBack = { handleBack(HashRoutes.MACHINERY_HUB) }
                        )
                    }
                    Screen.MACHINERY_DEALERS -> {
                        MachineryDealerDirectoryScreen(
                            currentLanguage = currentLanguage,
                            userId = userId,
                            onBack = { handleBack(HashRoutes.MACHINERY_HUB) }
                        )
                    }
                    Screen.ADMIN_MACHINERY -> {
                        if (UserSessionManager.isAdmin(context)) {
                            val sess = UserSessionManager.getSession(context)
                            AdminMachineryManagementScreen(
                                currentLanguage = currentLanguage,
                                adminEmail = sess?.email.orEmpty(),
                                onBack = { handleBack(HashRoutes.ADMIN_CONFIG) }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                Toast.makeText(context, "Admin sign-in required.", Toast.LENGTH_SHORT).show()
                                handleBack(HashRoutes.HOME)
                            }
                        }
                    }
                    Screen.ADMIN_BANNERS -> {
                        if (UserSessionManager.isAdmin(context)) {
                            AdminBannerManagementScreen(
                                currentLanguage = currentLanguage,
                                onBack = { handleBack(HashRoutes.ADMIN_CONFIG) }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                Toast.makeText(context, "Admin sign-in required.", Toast.LENGTH_SHORT).show()
                                handleBack(HashRoutes.HOME)
                            }
                        }
                    }
                    Screen.ADMIN_TRUST_MODERATION -> {
                        if (UserSessionManager.isAdmin(context)) {
                            val sess = UserSessionManager.getSession(context)
                            AdminTrustModerationScreen(
                                currentLanguage = currentLanguage,
                                adminEmail = sess?.email.orEmpty(),
                                onBack = { handleBack(HashRoutes.ADMIN_CONFIG) }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                Toast.makeText(context, "Admin sign-in required.", Toast.LENGTH_SHORT).show()
                                handleBack(HashRoutes.HOME)
                            }
                        }
                    }
                    Screen.LAND_RIGHTS -> {
                        LandRightsScreen(
                            currentLanguage = currentLanguage,
                            initialTopic = activeLandTopic,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenExperts = { navigateToRoute(HashRoutes.EXPERT) }
                        )
                    }
                    Screen.PARTNER_REGISTRATION -> {
                        val sess = UserSessionManager.getSession(context)
                        PartnerRegistrationScreen(
                            userId = userId,
                            applicantName = farmerName,
                            applicantPhone = sess?.phone,
                            applicantEmail = sess?.email,
                            currentLanguage = currentLanguage,
                            repository = partnerRoleRepo,
                            onBack = { handleBack(HashRoutes.PROFILE) },
                            onOpenPartnerDashboard = {
                                if (UserSessionManager.switchDashboardMode(context, DashboardMode.PARTNER_DASHBOARD)) {
                                    onRefreshSession()
                                    navigateToRoute(HashRoutes.PARTNER_DASHBOARD)
                                }
                            }
                        )
                    }
                    Screen.PARTNER_DASHBOARD -> {
                        PartnerDashboardScreen(
                            userId = userId,
                            partnerName = farmerName,
                            currentLanguage = currentLanguage,
                            repository = partnerRoleRepo,
                            onSwitchToFarmerDashboard = {
                                UserSessionManager.switchDashboardMode(context, DashboardMode.FARMER_DASHBOARD)
                                onRefreshSession()
                                navigateToRoute(HashRoutes.HOME, clearTop = true)
                            },
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenPartnerRegistration = { navigateToRoute(HashRoutes.PARTNER_REGISTRATION) },
                            onOpenNurserySellerWorkspace = { navigateToRoute(HashRoutes.SELLER_DASHBOARD) },
                            onOpenMachinerySellerWorkspace = {
                                activeMachineryCategoryCode = null
                                activeMachineryTab = 0
                                navigateToRoute(HashRoutes.MACHINERY_HUB)
                            },
                            onOpenCropBuyerWorkspace = { navigateToRoute(HashRoutes.MARKETPLACE) },
                            onOpenFarmServicesWorkspace = {
                                activeMachineryCategoryCode = null
                                activeMachineryTab = 2
                                navigateToRoute(HashRoutes.MACHINERY_HUB)
                            }
                        )
                    }
                    Screen.VIDEO_LEARNING -> {
                        VideoLearningCentreScreen(
                            currentLanguage = currentLanguage,
                            userId = userId,
                            onBack = { handleBack(HashRoutes.HOME) },
                            onOpenAdminVideoManager = { navigateToRoute(HashRoutes.ADMIN_VIDEO_LEARNING) },
                            onOpenCropGuide = { cId ->
                                activeCropDetail = CropMasterDatabase.getCropOrDefault(cId, null)
                                navigateToRoute(HashRoutes.CROP_DETAIL)
                            },
                            onAskAiAboutLesson = { lesson, currentSeconds ->
                                selectedCropForCalc = lesson.linkedCropId.ifBlank { null }
                                activeAiLessonContext = VideoLearningRepository.buildAiLessonContext(
                                    lesson = lesson,
                                    currentPositionSeconds = currentSeconds,
                                    language = currentLanguage
                                )
                                activeAiPromptSeed = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "\"${lesson.getTitle(currentLanguage)}\" পাঠ সম্পর্কে বিস্তারিত বুঝিয়ে বলুন।"
                                    AppLanguage.HINDI -> "\"${lesson.getTitle(currentLanguage)}\" वीडियो पाठ के बारे में सरल भाषा में समझाएं।"
                                    AppLanguage.ENGLISH -> "Explain key steps from the lesson \"${lesson.getTitle(currentLanguage)}\"."
                                }
                                navigateToRoute(HashRoutes.FARMER_AI)
                            }
                        )
                    }
                    Screen.ADMIN_VIDEO_LEARNING -> {
                        if (UserSessionManager.isAdmin(context)) {
                            AdminVideoManagementScreen(
                                currentLanguage = currentLanguage,
                                onBack = { handleBack(HashRoutes.VIDEO_LEARNING) }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                Toast.makeText(context, "Admin sign-in required.", Toast.LENGTH_SHORT).show()
                                handleBack(HashRoutes.HOME)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostLoginLanguageDialog(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        containerColor = RoyalGreenCardBg,
        titleContentColor = RoyalGold,
        textContentColor = RoyalTextPrimary,
        title = {
            Text(
                text = L10n.t("dialog_select_language", currentLanguage),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = L10n.t("dialog_welcome_farmer", currentLanguage),
                    fontSize = 13.sp,
                    color = RoyalTextSecondary
                )
                AppLanguage.values().forEach { lang ->
                    val isSelected = currentLanguage == lang
                    Button(
                        onClick = { onSelectLanguage(lang) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("post_login_lang_${lang.code}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) RoyalGreenEmerald else RoyalGreenSecondaryBg,
                            contentColor = if (isSelected) RoyalSoftGold else RoyalTextPrimary
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) RoyalGold else RoyalBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${lang.nativeName} (${lang.name.lowercase().replaceFirstChar { it.uppercase() }})",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalGreenEmerald,
                    contentColor = RoyalTextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = L10n.t("btn_continue", currentLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    )
}
