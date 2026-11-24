package com.example.ui.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.FarmerProfile
import com.example.data.model.NurseryApplicationStatus
import com.example.data.remote.FirestoreRepository
import com.example.data.remote.NurseryApplicationRepository
import com.example.ui.auth.signOutFarmer
import com.example.ui.theme.AgriAlertRed
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.util.L10n
import com.example.util.isCompactNarrowScreen
import com.example.util.toLocalizedDoubleOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import android.content.Intent
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import com.example.data.repository.TrustAndSafetyRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    currentLanguage: AppLanguage,
    userId: String,
    isDarkTheme: Boolean = false,
    onThemeChange: (Boolean) -> Unit = {},
    onLanguageChange: (AppLanguage) -> Unit,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onOpenNurseryRegistration: () -> Unit = {},
    onOpenSellerDashboard: () -> Unit = {},
    onOpenAdminNurseryManagement: () -> Unit = {},
    onOpenAdminTrustModeration: () -> Unit = {},
    onOpenGrievanceRedressal: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { FirestoreRepository(context) }
    val credentialManager = remember { CredentialManager.create(context) }
    val coroutineScope = rememberCoroutineScope()

    val profile by repository.observeProfile(userId).collectAsState(initial = null)

    var name by remember(profile, currentLanguage) {
        mutableStateOf(
            profile?.name ?: when (currentLanguage) {
                AppLanguage.BENGALI -> "সুমন সরকার"
                AppLanguage.HINDI -> "सुमन सरकार"
                AppLanguage.ENGLISH -> "Suman Sarkar"
            }
        )
    }
    var mobile by remember(profile) { mutableStateOf(profile?.mobileNumber ?: "") }
    var village by remember(profile, currentLanguage) {
        mutableStateOf(
            profile?.village ?: when (currentLanguage) {
                AppLanguage.BENGALI -> "শান্তিপুর / ধূপগুড়ি"
                AppLanguage.HINDI -> "शांतिपुर / धूपगुड़ी"
                AppLanguage.ENGLISH -> "Shantipur / Dhupguri"
            }
        )
    }
    var district by remember(profile, currentLanguage) {
        mutableStateOf(
            profile?.district ?: when (currentLanguage) {
                AppLanguage.BENGALI -> "জলপাইগুড়ি"
                AppLanguage.HINDI -> "जलपाईगुड़ी"
                AppLanguage.ENGLISH -> "Jalpaiguri"
            }
        )
    }
    var state by remember(profile, currentLanguage) {
        mutableStateOf(
            profile?.state ?: when (currentLanguage) {
                AppLanguage.BENGALI -> "পশ্চিমবঙ্গ"
                AppLanguage.HINDI -> "पश्चिम बंगाल"
                AppLanguage.ENGLISH -> "West Bengal"
            }
        )
    }
    var farmSize by remember(profile) { mutableStateOf((profile?.farmSize ?: 4.0).toString()) }

    var savedFeedback by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    val isNarrow = isCompactNarrowScreen()
    val trustRepo = remember { TrustAndSafetyRepository(context) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("profile_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "👤 ${L10n.t("profile_title", currentLanguage)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 28.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(if (isNarrow) 12.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // Language Selection Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = L10n.t("profile_lang_card", currentLanguage), fontWeight = FontWeight.Bold, color = AgriTextPrimary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = if (isNarrow) 2 else 3
                    ) {
                        AppLanguage.values().forEach { lang ->
                            val isSel = currentLanguage == lang
                            Button(
                                onClick = { onLanguageChange(lang) },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSel) Color.White else AgriTextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = lang.nativeName, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Theme Mode Card (White Mode / Black Mode)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_theme_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = L10n.t("profile_theme_card", currentLanguage),
                            fontWeight = FontWeight.Bold,
                            color = AgriTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = if (isNarrow) 1 else 2
                    ) {
                        Button(
                            onClick = { onThemeChange(false) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("theme_white_mode_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isDarkTheme) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (!isDarkTheme) Color.White else AgriTextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = L10n.t("profile_theme_white", currentLanguage),
                                fontWeight = if (!isDarkTheme) FontWeight.Bold else FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = { onThemeChange(true) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("theme_black_mode_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDarkTheme) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isDarkTheme) Color.White else AgriTextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = L10n.t("profile_theme_black", currentLanguage),
                                fontWeight = if (isDarkTheme) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Farmer Profile Details
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = L10n.t("profile_farmer_info", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(L10n.t("profile_name_label", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text(L10n.t("profile_phone_label", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = village,
                        onValueChange = { village = it },
                        label = { Text(L10n.t("profile_village_label", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        maxItemsInEachRow = if (isNarrow) 1 else 2
                    ) {
                        OutlinedTextField(
                            value = district,
                            onValueChange = { district = it },
                            label = { Text(L10n.t("profile_district_label", currentLanguage)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = farmSize,
                            onValueChange = { farmSize = it },
                            label = { Text(L10n.t("profile_farm_size_label", currentLanguage)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    if (savedFeedback) {
                        Text(
                            text = "✓ ${L10n.t("profile_saved_success", currentLanguage)}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (isSaving) return@Button
                            isSaving = true
                            coroutineScope.launch {
                                try {
                                    repository.saveProfile(
                                        FarmerProfile(
                                            userId = userId,
                                            name = name.trim(),
                                            mobileNumber = mobile.trim(),
                                            village = village.trim(),
                                            district = district.trim(),
                                            state = state.trim(),
                                            farmSize = farmSize.toLocalizedDoubleOrNull() ?: 0.0,
                                            farmSizeUnit = "bigha",
                                            preferredLanguage = currentLanguage.code
                                        )
                                    )
                                    savedFeedback = true
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (_: Exception) {
                                    val msg = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "ক্লাউড সিঙ্ক করতে Google লগইন প্রয়োজন"
                                        AppLanguage.HINDI -> "क्लाउड सिंक के लिए Google लॉगिन आवश्यक है"
                                        AppLanguage.ENGLISH -> "Sign in with Google to sync profile to cloud"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp)
                            .testTag("save_profile_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgriGreenPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(L10n.t("profile_btn_save", currentLanguage), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Nursery Portal & Role Management Section
            val currentSession = remember { UserSessionManager.getSession(context) }
            val nurseryRepo = remember { NurseryApplicationRepository(context) }
            val allApps by nurseryRepo.allApplicationsFlow.collectAsState(initial = emptyList())
            val userApp = remember(allApps, currentSession) {
                allApps.find { it.userId == currentSession?.userId }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_nursery_portal_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নার্সারি ও ব্যবসা পোর্টাল"
                                AppLanguage.HINDI -> "नर्सरी व व्यवसाय पोर्टल"
                                AppLanguage.ENGLISH -> "Nursery & Business Portal"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Role Badge
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val roleDisplay = when (currentSession?.role) {
                            UserSessionManager.ROLE_ADMIN -> when (currentLanguage) {
                                AppLanguage.BENGALI -> "🛡️ সিস্টেম অ্যাডমিনিস্ট্রেটর"
                                AppLanguage.HINDI -> "🛡️ सिस्टम एडमिनिस्ट्रेटर"
                                AppLanguage.ENGLISH -> "🛡️ System Administrator"
                            }
                            UserSessionManager.ROLE_NURSERY_OWNER -> when (currentLanguage) {
                                AppLanguage.BENGALI -> "🌱 নিবন্ধিত নার্সারি মালিক"
                                AppLanguage.HINDI -> "🌱 पंजीकृत नर्सरी मालिक"
                                AppLanguage.ENGLISH -> "🌱 Registered Nursery Owner"
                            }
                            else -> when (currentLanguage) {
                                AppLanguage.BENGALI -> "👤 সাধারণ কৃষক ও গ্রাহক"
                                AppLanguage.HINDI -> "👤 सामान्य किसान व ग्राहक"
                                AppLanguage.ENGLISH -> "👤 Farmer & Customer"
                            }
                        }
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "বর্তমান ভূমিকা: $roleDisplay"
                                AppLanguage.HINDI -> "वर्तमान भूमिका: $roleDisplay"
                                AppLanguage.ENGLISH -> "Current Role: $roleDisplay"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    // Admin Action
                    if (UserSessionManager.isAdmin(context)) {
                        Button(
                            onClick = onOpenAdminNurseryManagement,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("open_admin_nursery_mgmt_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = com.example.ui.theme.RoyalGreenEmerald,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "নার্সারি আবেদন অনুমোদন প্যানেল"
                                    AppLanguage.HINDI -> "नर्सरी आवेदन अनुमोदन पैनल"
                                    AppLanguage.ENGLISH -> "Nursery Application Approval Panel"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onOpenAdminTrustModeration,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("open_admin_trust_moderation_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AgriGreenPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অ্যাডমিন ট্রাস্ট, প্রতারণা ও অভিযোগ প্যানেল"
                                    AppLanguage.HINDI -> "एडमिन ट्रस्ट, धोखाधड़ी व शिकायत पैनल"
                                    AppLanguage.ENGLISH -> "Admin Trust & Fraud Moderation Panel"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Seller Dashboard Action
                    if (UserSessionManager.isNurseryOwner(context) || userApp?.status == NurseryApplicationStatus.APPROVED.code) {
                        Button(
                            onClick = onOpenSellerDashboard,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("open_seller_dashboard_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AgriGreenPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "বিক্রেতা ড্যাশবোর্ড খুলুন"
                                    AppLanguage.HINDI -> "विक्रेता डैशबोर्ड खोलें"
                                    AppLanguage.ENGLISH -> "Open Seller Dashboard"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Customer / Applicant Status Flow
                        when (userApp?.status) {
                            NurseryApplicationStatus.PENDING.code, NurseryApplicationStatus.UNDER_REVIEW.code -> {
                                Surface(
                                    color = com.example.ui.theme.RoyalWarningBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.RoyalGold.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "⏳ আবেদন পর্যালোচনাধীন রয়েছে"
                                                AppLanguage.HINDI -> "⏳ आवेदन समीक्षाधीन है"
                                                AppLanguage.ENGLISH -> "⏳ Application Under Review"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            color = com.example.ui.theme.RoyalGold
                                        )
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "আপনার নার্সারি '${userApp.nurseryNameBn}' যাচাইয়ের কাজ চলছে। অ্যাডমিন যাচাই সম্পন্ন হলে নোটিফিকেশন পাবেন।"
                                                AppLanguage.HINDI -> "आपकी नर्सरी '${userApp.nurseryNameBn}' का सत्यापन चल रहा है। एडमिन अनुमोदन के बाद आपको सूचना मिलेगी।"
                                                AppLanguage.ENGLISH -> "Verification for your nursery '${userApp.nurseryNameBn}' is in progress. You will be notified once approved by Admin."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = com.example.ui.theme.RoyalTextSecondary
                                        )
                                        Button(
                                            onClick = onOpenNurseryRegistration,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = com.example.ui.theme.RoyalGold,
                                                contentColor = com.example.ui.theme.RoyalGreenPrimaryBg
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp)
                                        ) {
                                            Text(
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "আবেদনের বর্তমান স্থিতি দেখুন"
                                                    AppLanguage.HINDI -> "आवेदन की वर्तमान स्थिति देखें"
                                                    AppLanguage.ENGLISH -> "View Application Status"
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            NurseryApplicationStatus.REJECTED.code -> {
                                Surface(
                                    color = com.example.ui.theme.RoyalAlertBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.RoyalAlertText.copy(alpha = 0.45f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "⚠️ আবেদনে তথ্য সংশোধন প্রয়োজন"
                                                AppLanguage.HINDI -> "⚠️ आवेदन में जानकारी सुधार आवश्यक है"
                                                AppLanguage.ENGLISH -> "⚠️ Application Needs Correction"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            color = com.example.ui.theme.RoyalAlertText
                                        )
                                        if (userApp.adminRejectionReason.isNotBlank()) {
                                            Text(
                                                text = when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "অ্যাডমিন মন্তব্য: ${userApp.adminRejectionReason}"
                                                    AppLanguage.HINDI -> "एडमिन टिप्पणी: ${userApp.adminRejectionReason}"
                                                    AppLanguage.ENGLISH -> "Admin Remark: ${userApp.adminRejectionReason}"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = com.example.ui.theme.RoyalTextPrimary
                                            )
                                        }
                                        Button(
                                            onClick = onOpenNurseryRegistration,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = AgriAlertRed,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp)
                                        ) {
                                            Text(
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "তথ্য সংশোধন করে পুনরায় জমা দিন"
                                                    AppLanguage.HINDI -> "जानकारी सुधारकर पुनः जमा करें"
                                                    AppLanguage.ENGLISH -> "Edit Info & Resubmit"
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {
                                // Not applied yet
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "আপনি কি নার্সারি মালিক বা ফসলের চারা বিক্রেতা? সরাসরি কৃষকদের কাছে চারা ও বীজ বিক্রি করতে যুক্ত হন।"
                                            AppLanguage.HINDI -> "क्या आप नर्सरी मालिक या पौधा विक्रेता हैं? सीधे किसानों को पौधे और बीज बेचने के लिए जुड़ें।"
                                            AppLanguage.ENGLISH -> "Are you a nursery owner or sapling seller? Join to sell certified plants and seeds directly to farmers."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AgriTextSecondary
                                    )
                                    Button(
                                        onClick = onOpenNurseryRegistration,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .testTag("profile_join_nursery_btn"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AgriGreenPrimary,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Storefront, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "নার্সারি মালিক হিসেবে যুক্ত হন"
                                                AppLanguage.HINDI -> "नर्सरी मालिक के रूप में जुड़ें"
                                                AppLanguage.ENGLISH -> "JOIN AS NURSERY OWNER"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Privacy, Data Portability, Grievance Redressal & Account Controls (DPDP Act Alignment)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("profile_privacy_controls_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "গোপনীয়তা, ডেটা ও অভিযোগ সহায়তা"
                                AppLanguage.HINDI -> "गोपनीयता, डेटा व शिकायत सहायता"
                                AppLanguage.ENGLISH -> "Privacy, Data Export & Grievance Controls"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    val consentTs = remember { UserSessionManager.getPrivacyConsentTimestamp(context) }
                    val consentFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                    Text(
                        text = if (consentTs > 0L) {
                            "DPDP Privacy Consent Recorded: ${consentFmt.format(Date(consentTs))}"
                        } else {
                            "DPDP Data Rights: Export your local profile/records or request account deletion anytime."
                        },
                        fontSize = 12.sp,
                        color = AgriTextSecondary
                    )

                    OutlinedButton(
                        onClick = onOpenGrievanceRedressal,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("profile_open_grievance_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "অভিযোগ ও লিগ্যাল সেন্টার খুলুন (৪৮ ঘণ্টা SLA)"
                                AppLanguage.HINDI -> "शिकायत एवं कानूनी केंद्र खोलें (48 घंटे SLA)"
                                AppLanguage.ENGLISH -> "Open Grievance & Legal Centre"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val exportPayload = buildString {
                                append("{\n")
                                append("  \"app\": \"KRISHAK SATHI\",\n")
                                append("  \"userId\": \"${userId.replace("\"", "")}\",\n")
                                append("  \"name\": \"${name.replace("\"", "")}\",\n")
                                append("  \"mobile\": \"${mobile.replace("\"", "")}\",\n")
                                append("  \"village\": \"${village.replace("\"", "")}\",\n")
                                append("  \"district\": \"${district.replace("\"", "")}\",\n")
                                append("  \"state\": \"${state.replace("\"", "")}\",\n")
                                append("  \"farmSizeBigha\": \"$farmSize\",\n")
                                append("  \"language\": \"${currentLanguage.code}\",\n")
                                append("  \"exportedAt\": \"${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}\"\n")
                                append("}")
                            }
                            runCatching {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Krishak Sathi Farmer Data Backup")
                                    putExtra(Intent.EXTRA_TEXT, exportPayload)
                                }
                                context.startActivity(Intent.createChooser(intent, "Export Profile Backup"))
                            }.onFailure {
                                Toast.makeText(context, "Unable to open share sheet", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("profile_export_data_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "ব্যক্তিগত ডেটা ব্যাকআপ / এক্সপোর্ট করুন"
                                AppLanguage.HINDI -> "व्यक्तिगत डेटा बैकअप / एक्सपोर्ट करें"
                                AppLanguage.ENGLISH -> "Export Profile & Data Backup (JSON)"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = { showDeleteAccountDialog = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("profile_delete_account_btn"),
                        border = BorderStroke(1.dp, AgriAlertRed.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = AgriAlertRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "অ্যাকাউন্ট মুছে ফেলার অনুরোধ ও ডেটা সাফ করুন"
                                AppLanguage.HINDI -> "खाता हटाने का अनुरोध व डेटा साफ़ करें"
                                AppLanguage.ENGLISH -> "Request Account Deletion & Clear Data"
                            },
                            color = AgriAlertRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Sign Out Button (Gated Behind Confirmation Dialog)
            Button(
                onClick = { showSignOutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("sign_out_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.example.ui.theme.RoyalAlertBg,
                    contentColor = com.example.ui.theme.RoyalAlertText
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.RoyalAlertText.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "লগআউট করুন"
                        AppLanguage.HINDI -> "लॉग आउट करें"
                        AppLanguage.ENGLISH -> "Sign Out"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "লগআউট নিশ্চিত করুন"
                        AppLanguage.HINDI -> "लॉग आउट की पुष्टि करें"
                        AppLanguage.ENGLISH -> "Confirm Sign Out"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "আপনি কি নিশ্চিত যে আপনি আপনার KRISHAK SATHI সেশন থেকে সাইন আউট করতে চান?"
                        AppLanguage.HINDI -> "क्या आप वाकई अपने KRISHAK SATHI सत्र से साइन आउट करना चाहते हैं?"
                        AppLanguage.ENGLISH -> "Are you sure you want to sign out of your KRISHAK SATHI session?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        signOutFarmer(context, credentialManager, onSignOut, coroutineScope)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriAlertRed, contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_sign_out_btn")
                ) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "লগআউট করুন"
                            AppLanguage.HINDI -> "साइन आउट करें"
                            AppLanguage.ENGLISH -> "Sign Out"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "বাতিল"
                            AppLanguage.HINDI -> "रद्द करें"
                            AppLanguage.ENGLISH -> "Cancel"
                        }
                    )
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteAccountDialog = false },
            title = {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "অ্যাকাউন্ট ও ডেটা মুছে ফেলা"
                        AppLanguage.HINDI -> "खाता व डेटा विलोपन अनुरोध"
                        AppLanguage.ENGLISH -> "Account Deletion & Data Erasure"
                    },
                    fontWeight = FontWeight.Bold,
                    color = AgriAlertRed
                )
            },
            text = {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "এটি আপনার গোপনীয়তা (PRIVACY_DATA) মুছে ফেলার অনুরোধ লগ করবে, এই ডিভাইসের সেশন ক্যাশ মুছে ফেলবে এবং সম্ভব হলে ক্লাউড অ্যাকাউন্ট সরিয়ে সাইন আউট করবে।"
                        AppLanguage.HINDI -> "यह आपका गोपनीयता (PRIVACY_DATA) विलोपन अनुरोध दर्ज करेगा, डिवाइस का स्थानीय सत्र साफ़ करेगा और क्लाउड खाता हटाने का प्रयास कर साइन आउट करेगा।"
                        AppLanguage.ENGLISH -> "This logs a formal DPDP deletion ticket, clears your local device session/profile cache, attempts Firebase account removal if recently authenticated, and signs you out."
                    },
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    enabled = !isDeletingAccount,
                    onClick = {
                        isDeletingAccount = true
                        coroutineScope.launch {
                            runCatching {
                                trustRepo.submitGrievanceTicket(
                                    userId = userId,
                                    userName = name.ifBlank { "Farmer" },
                                    category = "PRIVACY_DATA",
                                    subject = "Account Deletion Request ($userId)",
                                    description = "User requested full account erasure & local cache purge via Profile DPDP controls.",
                                    relatedEntityId = userId
                                )
                            }
                            runCatching {
                                Firebase.auth.currentUser?.delete()?.await()
                            }
                            isDeletingAccount = false
                            showDeleteAccountDialog = false
                            Toast.makeText(
                                context,
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ডেটা মুছে ফেলার অনুরোধ সম্পন্ন ও সেশন সাফ করা হয়েছে।"
                                    AppLanguage.HINDI -> "खाता विलोपन अनुरोध दर्ज व सत्र साफ़ किया गया।"
                                    AppLanguage.ENGLISH -> "Deletion request logged and local session cleared."
                                },
                                Toast.LENGTH_LONG
                            ).show()
                            signOutFarmer(context, credentialManager, onSignOut, coroutineScope)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriAlertRed, contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_delete_account_btn")
                ) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "নিশ্চিত মুছুন"
                            AppLanguage.HINDI -> "हटाने की पुष्टि करें"
                            AppLanguage.ENGLISH -> "Confirm Delete"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeletingAccount,
                    onClick = { showDeleteAccountDialog = false }
                ) {
                    Text(L10n.t("btn_close", currentLanguage))
                }
            }
        )
    }
}
