package com.example.ui.nursery

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.NurseryApplication
import com.example.data.model.NurseryApplicationStatus
import com.example.data.remote.NurseryApplicationRepository
import com.example.ui.machinery.AdminMachineryManagementContent
import com.example.util.L10n
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNurseryManagementScreen(
    repository: NurseryApplicationRepository,
    onNavigateBack: () -> Unit,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val applications by repository.allApplicationsFlow.collectAsState(initial = emptyList())

    val adminSession = remember { UserSessionManager.getSession(context) }
    val adminEmail = adminSession?.email ?: "sarkarsuman94473@gmail.com"

    var adminSectionTab by remember { mutableIntStateOf(0) } // 0: Partner Applications, 1: WB Directory, 2: Plant Kingdom
    var selectedFilter by remember { mutableStateOf<String?>("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAppForReview by remember { mutableStateOf<NurseryApplication?>(null) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showSuspendDialog by remember { mutableStateOf(false) }
    var showRequestInfoDialog by remember { mutableStateOf(false) }
    var reasonInput by remember { mutableStateOf("") }

    val filteredList = remember(applications, selectedFilter, searchQuery) {
        applications.filter { app ->
            val matchesFilter = when (selectedFilter) {
                null, "ALL" -> true
                else -> app.status.equals(selectedFilter, ignoreCase = true)
            }
            val matchesQuery = searchQuery.isBlank() ||
                    app.nurseryNameBn.contains(searchQuery, ignoreCase = true) ||
                    app.nurseryNameEn.contains(searchQuery, ignoreCase = true) ||
                    app.applicantName.contains(searchQuery, ignoreCase = true) ||
                    app.district.contains(searchQuery, ignoreCase = true) ||
                    app.businessPhone.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    val pendingCount = applications.count { it.status == NurseryApplicationStatus.PENDING.code }
    val underReviewCount = applications.count { it.status == NurseryApplicationStatus.UNDER_REVIEW.code }
    val approvedCount = applications.count { it.status == NurseryApplicationStatus.APPROVED.code }
    val rejectedCount = applications.count { it.status == NurseryApplicationStatus.REJECTED.code }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Column {
                            Text(
                                text = L10n.t("admin_nursery_title", currentLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = L10n.t("admin_nursery_subtitle", currentLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(48.dp).testTag("admin_nursery_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = L10n.t("btn_back", currentLanguage)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                ScrollableTabRow(
                    selectedTabIndex = adminSectionTab,
                    edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = adminSectionTab == 0,
                        onClick = { adminSectionTab = 0 },
                        modifier = Modifier.testTag("admin_tab_applications"),
                        text = {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "পার্টনার আবেদন (${applications.size})"
                                    AppLanguage.HINDI -> "पार्टनर आवेदन (${applications.size})"
                                    AppLanguage.ENGLISH -> "Applications (${applications.size})"
                                },
                                fontWeight = if (adminSectionTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = adminSectionTab == 1,
                        onClick = { adminSectionTab = 1 },
                        modifier = Modifier.testTag("admin_tab_wb_directory"),
                        text = {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "WB ডিরেক্টরি ও যাচাই"
                                    AppLanguage.HINDI -> "WB निर्देशिका व सत्यापन"
                                    AppLanguage.ENGLISH -> "WB Directory"
                                },
                                fontWeight = if (adminSectionTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = adminSectionTab == 2,
                        onClick = { adminSectionTab = 2 },
                        modifier = Modifier.testTag("admin_tab_plant_kingdom"),
                        text = {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "উদ্ভিদ ও বাল্ক ইম্পোর্ট"
                                    AppLanguage.HINDI -> "पौधे व बल्क इम्पोर्ट"
                                    AppLanguage.ENGLISH -> "Plant Kingdom & Import"
                                },
                                fontWeight = if (adminSectionTab == 2) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = adminSectionTab == 3,
                        onClick = { adminSectionTab = 3 },
                        modifier = Modifier.testTag("admin_tab_machinery"),
                        text = {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "কৃষি যন্ত্র ও ডিলার"
                                    AppLanguage.HINDI -> "मशीनरी व डीलर"
                                    AppLanguage.ENGLISH -> "Machinery Hub"
                                },
                                fontWeight = if (adminSectionTab == 3) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
        if (adminSectionTab == 1) {
            Box(modifier = Modifier.fillMaxSize().widthIn(max = 760.dp)) {
                AdminWbDirectoryManagementPanel(currentLanguage = currentLanguage)
            }
        } else if (adminSectionTab == 2) {
            Box(modifier = Modifier.fillMaxSize().widthIn(max = 760.dp)) {
                AdminPlantKingdomManagementPanel(currentLanguage = currentLanguage)
            }
        } else if (adminSectionTab == 3) {
            Box(modifier = Modifier.fillMaxSize().widthIn(max = 760.dp)) {
                AdminMachineryManagementContent(
                    currentLanguage = currentLanguage,
                    adminEmail = adminEmail
                )
            }
        } else LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Summary Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = L10n.t("status_pending", currentLanguage),
                        count = pendingCount,
                        badgeColor = com.example.ui.theme.RoyalSoftGold,
                        backgroundColor = com.example.ui.theme.RoyalWarningBg,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = L10n.t("status_approved", currentLanguage),
                        count = approvedCount,
                        badgeColor = com.example.ui.theme.RoyalSuccessText,
                        backgroundColor = com.example.ui.theme.RoyalSuccessBg,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = L10n.t("status_rejected", currentLanguage),
                        count = rejectedCount,
                        badgeColor = com.example.ui.theme.RoyalAlertText,
                        backgroundColor = com.example.ui.theme.RoyalAlertBg,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "নার্সারি, মালিক বা জেলা দিয়ে খুঁজুন..."
                                AppLanguage.HINDI -> "नर्सरी, मालिक या ज़िले से खोजें..."
                                AppLanguage.ENGLISH -> "Search by nursery, owner or district..."
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = L10n.t("btn_search", currentLanguage))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = L10n.t("btn_cancel", currentLanguage))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_nursery_search_field")
                )
            }

            // Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf(
                        "ALL" to when (currentLanguage) {
                            AppLanguage.BENGALI -> "সবগুলো (${applications.size})"
                            AppLanguage.HINDI -> "सभी (${applications.size})"
                            AppLanguage.ENGLISH -> "All (${applications.size})"
                        },
                        NurseryApplicationStatus.PENDING.code to when (currentLanguage) {
                            AppLanguage.BENGALI -> "নতুন আবেদন ($pendingCount)"
                            AppLanguage.HINDI -> "नए आवेदन ($pendingCount)"
                            AppLanguage.ENGLISH -> "Pending ($pendingCount)"
                        },
                        NurseryApplicationStatus.UNDER_REVIEW.code to when (currentLanguage) {
                            AppLanguage.BENGALI -> "পর্যালোচনাধীন ($underReviewCount)"
                            AppLanguage.HINDI -> "समीक्षाधीन ($underReviewCount)"
                            AppLanguage.ENGLISH -> "Under Review ($underReviewCount)"
                        },
                        NurseryApplicationStatus.APPROVED.code to when (currentLanguage) {
                            AppLanguage.BENGALI -> "অনুমোদিত ($approvedCount)"
                            AppLanguage.HINDI -> "स्वीकृत ($approvedCount)"
                            AppLanguage.ENGLISH -> "Approved ($approvedCount)"
                        },
                        NurseryApplicationStatus.REJECTED.code to when (currentLanguage) {
                            AppLanguage.BENGALI -> "বাতিলকৃত ($rejectedCount)"
                            AppLanguage.HINDI -> "अस्वीकृत ($rejectedCount)"
                            AppLanguage.ENGLISH -> "Rejected ($rejectedCount)"
                        },
                        NurseryApplicationStatus.SUSPENDED.code to when (currentLanguage) {
                            AppLanguage.BENGALI -> "স্থগিত"
                            AppLanguage.HINDI -> "निलंबित"
                            AppLanguage.ENGLISH -> "Suspended"
                        }
                    )

                    items(filters) { (code, label) ->
                        val selected = selectedFilter == code
                        FilterChip(
                            selected = selected,
                            onClick = { selectedFilter = code },
                            label = { Text(label, fontSize = 13.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Header for List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "আবেদন তালিকা (${filteredList.size})"
                            AppLanguage.HINDI -> "आवेदन सूची (${filteredList.size})"
                            AppLanguage.ENGLISH -> "Applications (${filteredList.size})"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "ক্লিক করে পর্যালোচনা করুন"
                            AppLanguage.HINDI -> "समीक्षा के लिए टैप करें"
                            AppLanguage.ENGLISH -> "Tap to review"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Outlined.AssignmentLate,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "কোনো আবেদন পাওয়া যায়নি"
                                    AppLanguage.HINDI -> "कोई आवेदन नहीं मिला"
                                    AppLanguage.ENGLISH -> "No applications found"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অন্য ফিল্টার নির্বাচন করুন অথবা অপেক্ষা করুন।"
                                    AppLanguage.HINDI -> "कोई अन्य फ़िल्टर चुनें या प्रतीक्षा करें।"
                                    AppLanguage.ENGLISH -> "Try selecting a different filter."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { app ->
                    val displayName = if (currentLanguage == AppLanguage.BENGALI) app.nurseryNameBn.ifBlank { app.nurseryNameEn } else app.nurseryNameEn.ifBlank { app.nurseryNameBn }
                    AdminApplicationItemCard(
                        app = app,
                        currentLanguage = currentLanguage,
                        onReviewClick = { selectedAppForReview = app },
                        onQuickApprove = {
                            scope.launch {
                                try {
                                    repository.adminApproveApplication(app.id, adminEmail)
                                    val msg = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "$displayName অনুমোদন করা হয়েছে!"
                                        AppLanguage.HINDI -> "$displayName स्वीकृत हो गई है!"
                                        AppLanguage.ENGLISH -> "$displayName has been approved!"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
        }
    }

    // Detail Review Dialog
    selectedAppForReview?.let { app ->
        val displayName = if (currentLanguage == AppLanguage.BENGALI) app.nurseryNameBn.ifBlank { app.nurseryNameEn } else app.nurseryNameEn.ifBlank { app.nurseryNameBn }
        AdminReviewDetailDialog(
            app = app,
            currentLanguage = currentLanguage,
            onDismiss = { selectedAppForReview = null },
            onApprove = {
                scope.launch {
                    try {
                        repository.adminApproveApplication(app.id, adminEmail)
                        val msg = when (currentLanguage) {
                            AppLanguage.BENGALI -> "$displayName সফলভাবে অনুমোদন করা হলো!"
                            AppLanguage.HINDI -> "$displayName सफलतापूर्वक स्वीकृत हुई!"
                            AppLanguage.ENGLISH -> "$displayName approved successfully!"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        selectedAppForReview = null
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onReject = {
                showRejectDialog = true
            },
            onSuspend = {
                showSuspendDialog = true
            },
            onRequestInfo = {
                showRequestInfoDialog = true
            }
        )
    }

    // Reject Dialog
    if (showRejectDialog && selectedAppForReview != null) {
        val targetApp = selectedAppForReview!!
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "আবেদন বাতিল করার কারণ"
                        AppLanguage.HINDI -> "आवेदन अस्वीकार करने का कारण"
                        AppLanguage.ENGLISH -> "Reason for Rejection"
                    }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "আবেদনকারী যাতে তথ্য সংশোধন করে পুনরায় জমা দিতে পারেন, তার জন্য সুস্পষ্ট কারণ লিখুন:"
                            AppLanguage.HINDI -> "ताकि आवेदक विवरण सुधारकर पुनः जमा कर सके, स्पष्ट कारण लिखें:"
                            AppLanguage.ENGLISH -> "Provide a clear reason so the applicant can fix details and resubmit:"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        placeholder = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "যেমন: ট্রেড লাইসেন্স অস্পষ্ট / সঠিক ঠিকানা প্রয়োজন..."
                                    AppLanguage.HINDI -> "जैसे: ट्रेड लाइसेंस स्पष्ट नहीं है / पूरा पता आवश्यक है..."
                                    AppLanguage.ENGLISH -> "e.g. Unclear trade license / complete address required..."
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val defaultReason = when (currentLanguage) {
                            AppLanguage.BENGALI -> "তথ্য অসম্পূর্ণ বা যাচাই করা যায়নি"
                            AppLanguage.HINDI -> "जानकारी अधूरी है या सत्यापित नहीं हो सकी"
                            AppLanguage.ENGLISH -> "Incomplete or unverifiable details"
                        }
                        val reason = reasonInput.ifBlank { defaultReason }
                        scope.launch {
                            try {
                                repository.adminRejectApplication(targetApp.id, reason, adminEmail)
                                val msg = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "আবেদন বাতিল করা হয়েছে"
                                    AppLanguage.HINDI -> "आवेदन अस्वीकार किया गया"
                                    AppLanguage.ENGLISH -> "Application rejected"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                showRejectDialog = false
                                selectedAppForReview = null
                                reasonInput = ""
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "বাতিল নিশ্চিত করুন"
                            AppLanguage.HINDI -> "अस्वीकृति की पुष्टि करें"
                            AppLanguage.ENGLISH -> "Confirm Rejection"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(L10n.t("btn_cancel", currentLanguage))
                }
            }
        )
    }

    // Suspend Dialog
    if (showSuspendDialog && selectedAppForReview != null) {
        val targetApp = selectedAppForReview!!
        AlertDialog(
            onDismissRequest = { showSuspendDialog = false },
            title = {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "নার্সারি কার্যক্রম স্থগিতকরণ"
                        AppLanguage.HINDI -> "नर्सरी निलंबन"
                        AppLanguage.ENGLISH -> "Suspend Nursery"
                    }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "এই নার্সারির সমস্ত পাবলিক লিস্টিং ও বিক্রেতা ড্যাশবোর্ড সাময়িকভাবে বন্ধ থাকবে। স্থগিত করার কারণ লিখুন:"
                            AppLanguage.HINDI -> "इस नर्सरी की सभी सार्वजनिक लिस्टिंग और विक्रेता डैशबोर्ड अस्थायी रूप से बंद रहेंगे। निलंबन का कारण लिखें:"
                            AppLanguage.ENGLISH -> "All public listings and seller dashboard access will be paused. Enter reason for suspension:"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        placeholder = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "স্থগিতকরণের সুনির্দিষ্ট কারণ লিখুন..."
                                    AppLanguage.HINDI -> "निलंबन का स्पष्ट कारण लिखें..."
                                    AppLanguage.ENGLISH -> "Enter specific reason for suspension..."
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val defaultReason = when (currentLanguage) {
                            AppLanguage.BENGALI -> "নীতিমালা লঙ্ঘনের কারণে সাময়িক স্থগিত"
                            AppLanguage.HINDI -> "नीति उल्लंघन के कारण अस्थायी रूप से निलंबित"
                            AppLanguage.ENGLISH -> "Temporarily suspended due to policy violation"
                        }
                        val reason = reasonInput.ifBlank { defaultReason }
                        scope.launch {
                            try {
                                repository.adminSuspendNursery(targetApp.id, reason, adminEmail)
                                val msg = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "নার্সারি স্থগিত করা হয়েছে"
                                    AppLanguage.HINDI -> "नर्सरी निलंबित की गई"
                                    AppLanguage.ENGLISH -> "Nursery suspended"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                showSuspendDialog = false
                                selectedAppForReview = null
                                reasonInput = ""
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "স্থগিত করুন"
                            AppLanguage.HINDI -> "निलंबित करें"
                            AppLanguage.ENGLISH -> "Suspend"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuspendDialog = false }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(L10n.t("btn_cancel", currentLanguage))
                }
            }
        )
    }

    // Request Info Dialog
    if (showRequestInfoDialog && selectedAppForReview != null) {
        val targetApp = selectedAppForReview!!
        AlertDialog(
            onDismissRequest = { showRequestInfoDialog = false },
            title = {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "অতিরিক্ত তথ্যের অনুরোধ"
                        AppLanguage.HINDI -> "अतिरिक्त जानकारी का अनुरोध"
                        AppLanguage.ENGLISH -> "Request Additional Information"
                    }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "আবেদনকারীকে কী ধরনের অতিরিক্ত তথ্য বা কাগজপত্র জমা দিতে হবে তা উল্লেখ করুন:"
                            AppLanguage.HINDI -> "बताएं कि आवेदक को कौन सी अतिरिक्त जानकारी या दस्तावेज़ जमा करने हैं:"
                            AppLanguage.ENGLISH -> "Specify what additional details or documents the applicant needs to provide:"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        placeholder = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "যেমন: নার্সারির সাইনবোর্ডের ছবি ও স্পষ্ট ট্রেড লাইসেন্স কপি..."
                                    AppLanguage.HINDI -> "जैसे: नर्सरी साइनबोर्ड का फ़ोटो और स्पष्ट ट्रेड लाइसेंस..."
                                    AppLanguage.ENGLISH -> "e.g. Clear trade license copy & signboard details..."
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val defaultNote = when (currentLanguage) {
                            AppLanguage.BENGALI -> "অনুরোধ: আরও স্পষ্ট তথ্য প্রয়োজন"
                            AppLanguage.HINDI -> "अनुरोध: अधिक स्पष्ट विवरण आवश्यक है"
                            AppLanguage.ENGLISH -> "Requested: Clearer verification details required"
                        }
                        val note = reasonInput.ifBlank { defaultNote }
                        scope.launch {
                            try {
                                repository.adminRequestInfo(targetApp.id, note, adminEmail)
                                val msg = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "তথ্য সংশোধনের অনুরোধ পাঠানো হয়েছে"
                                    AppLanguage.HINDI -> "जानकारी सुधार का अनुरोध भेजा गया"
                                    AppLanguage.ENGLISH -> "Information request sent"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                showRequestInfoDialog = false
                                selectedAppForReview = null
                                reasonInput = ""
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "অনুরোধ পাঠান"
                            AppLanguage.HINDI -> "अनुरोध भेजें"
                            AppLanguage.ENGLISH -> "Send Request"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestInfoDialog = false }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(L10n.t("btn_cancel", currentLanguage))
                }
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    count: Int,
    badgeColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.25f)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = badgeColor.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminApplicationItemCard(
    app: NurseryApplication,
    currentLanguage: AppLanguage,
    onReviewClick: () -> Unit,
    onQuickApprove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusEnum = NurseryApplicationStatus.fromCode(app.status)

    val (statusColor, statusBg, statusText) = when (statusEnum) {
        NurseryApplicationStatus.PENDING -> Triple(com.example.ui.theme.RoyalSoftGold, com.example.ui.theme.RoyalWarningBg, statusEnum.getLabel(currentLanguage))
        NurseryApplicationStatus.UNDER_REVIEW -> Triple(com.example.ui.theme.RoyalInfoText, com.example.ui.theme.RoyalInfoBg, statusEnum.getLabel(currentLanguage))
        NurseryApplicationStatus.APPROVED -> Triple(com.example.ui.theme.RoyalSuccessText, com.example.ui.theme.RoyalSuccessBg, statusEnum.getLabel(currentLanguage))
        NurseryApplicationStatus.REJECTED -> Triple(com.example.ui.theme.RoyalAlertText, com.example.ui.theme.RoyalAlertBg, statusEnum.getLabel(currentLanguage))
        NurseryApplicationStatus.SUSPENDED -> Triple(com.example.ui.theme.RoyalGold, com.example.ui.theme.RoyalGreenSecondaryBg, statusEnum.getLabel(currentLanguage))
        else -> Triple(com.example.ui.theme.RoyalTextSecondary, com.example.ui.theme.RoyalInputBg, statusEnum.getLabel(currentLanguage))
    }

    val dateFormatted = remember(app.submittedAtMillis, currentLanguage) {
        if (app.submittedAtMillis > 0) {
            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
            sdf.format(Date(app.submittedAtMillis))
        } else when (currentLanguage) {
            AppLanguage.BENGALI -> "সম্প্রতি"
            AppLanguage.HINDI -> "हाल ही में"
            AppLanguage.ENGLISH -> "Recently"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onReviewClick)
            .testTag("admin_nursery_card_${app.id}"),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Status & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Nursery Name
            Text(
                text = app.getDisplayName(currentLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (currentLanguage == AppLanguage.BENGALI && app.nurseryNameEn.isNotBlank() && app.nurseryNameBn != app.nurseryNameEn) {
                Text(
                    text = app.nurseryNameEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Owner info and District
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = app.getDisplayOwnerName(currentLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "${app.district}, ${app.state}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Trade License tag if available
            if (app.tradeLicenseNumber.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.VerifiedUser,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "লাইসেন্স: ${app.tradeLicenseNumber}"
                                AppLanguage.HINDI -> "लाइसेंस: ${app.tradeLicenseNumber}"
                                AppLanguage.ENGLISH -> "License: ${app.tradeLicenseNumber}"
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Action row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onReviewClick,
                    modifier = Modifier.heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "বিস্তারিত দেখুন"
                            AppLanguage.HINDI -> "विवरण देखें"
                            AppLanguage.ENGLISH -> "View Details"
                        }
                    )
                }

                if (statusEnum == NurseryApplicationStatus.PENDING || statusEnum == NurseryApplicationStatus.UNDER_REVIEW) {
                    FilledTonalButton(
                        onClick = onQuickApprove,
                        modifier = Modifier.heightIn(min = 48.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = com.example.ui.theme.RoyalGreenEmerald,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "দ্রুত অনুমোদন"
                                AppLanguage.HINDI -> "त्वरित स्वीकृति"
                                AppLanguage.ENGLISH -> "Quick Approve"
                            },
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminReviewDetailDialog(
    app: NurseryApplication,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onSuspend: () -> Unit,
    onRequestInfo: () -> Unit
) {
    val notSpecified = when (currentLanguage) {
        AppLanguage.BENGALI -> "উল্লেখ নেই"
        AppLanguage.HINDI -> "उल्लेख नहीं है"
        AppLanguage.ENGLISH -> "Not specified"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .imePadding()
                .testTag("admin_review_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Dialog Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "আবেদন বিস্তারিত যাচাই"
                            AppLanguage.HINDI -> "आवेदन विस्तृत सत्यापन"
                            AppLanguage.ENGLISH -> "Application Verification"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = L10n.t("btn_cancel", currentLanguage))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Application Details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Nursery Identity
                    SectionBlock(
                        title = when (currentLanguage) {
                            AppLanguage.BENGALI -> "১. নার্সারি পরিচয়"
                            AppLanguage.HINDI -> "1. नर्सरी पहचान"
                            AppLanguage.ENGLISH -> "1. Nursery Identity"
                        }
                    ) {
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "বাংলা নাম"
                                AppLanguage.HINDI -> "नर्सरी का नाम"
                                AppLanguage.ENGLISH -> "Nursery Name"
                            },
                            value = app.getDisplayName(currentLanguage)
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ইংরেজি নাম"
                                AppLanguage.HINDI -> "अंग्रेज़ी नाम"
                                AppLanguage.ENGLISH -> "English Name"
                            },
                            value = app.nurseryNameEn
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "বিভাগ"
                                AppLanguage.HINDI -> "श्रेणी"
                                AppLanguage.ENGLISH -> "Category"
                            },
                            value = app.primaryCategory
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "প্রতিষ্ঠা সাল"
                                AppLanguage.HINDI -> "स्थापना वर्ष"
                                AppLanguage.ENGLISH -> "Established"
                            },
                            value = app.establishmentYear.ifBlank { notSpecified }
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "বিবরণ"
                                AppLanguage.HINDI -> "विवरण"
                                AppLanguage.ENGLISH -> "Description"
                            },
                            value = app.getDisplayDescription(currentLanguage).ifBlank { notSpecified }
                        )
                    }

                    // Owner Details
                    SectionBlock(
                        title = when (currentLanguage) {
                            AppLanguage.BENGALI -> "২. মালিকের তথ্য"
                            AppLanguage.HINDI -> "2. मालिक का विवरण"
                            AppLanguage.ENGLISH -> "2. Owner Details"
                        }
                    ) {
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নাম"
                                AppLanguage.HINDI -> "नाम"
                                AppLanguage.ENGLISH -> "Name"
                            },
                            value = app.getDisplayOwnerName(currentLanguage)
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "মোবাইল নম্বর"
                                AppLanguage.HINDI -> "मोबाइल नंबर"
                                AppLanguage.ENGLISH -> "Mobile"
                            },
                            value = app.mobileNumber
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ইমেইল"
                                AppLanguage.HINDI -> "ईमेल"
                                AppLanguage.ENGLISH -> "Email"
                            },
                            value = app.email.ifBlank { notSpecified }
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "মোবাইল যাচাই"
                                AppLanguage.HINDI -> "मोबाइल सत्यापन"
                                AppLanguage.ENGLISH -> "Mobile Verified"
                            },
                            value = if (app.isMobileVerified) {
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "হ্যাঁ (যাচাইকৃত)"
                                    AppLanguage.HINDI -> "हाँ (सत्यापित)"
                                    AppLanguage.ENGLISH -> "Yes (Verified)"
                                }
                            } else {
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "না"
                                    AppLanguage.HINDI -> "नहीं"
                                    AppLanguage.ENGLISH -> "No"
                                }
                            }
                        )
                    }

                    // Contact & Delivery
                    SectionBlock(
                        title = when (currentLanguage) {
                            AppLanguage.BENGALI -> "৩. যোগাযোগ ও ডেলিভারি"
                            AppLanguage.HINDI -> "3. संपर्क व डिलीवरी"
                            AppLanguage.ENGLISH -> "3. Contact & Delivery"
                        }
                    ) {
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ব্যবসায়িক ফোন"
                                AppLanguage.HINDI -> "व्यावसायिक फ़ोन"
                                AppLanguage.ENGLISH -> "Business Phone"
                            },
                            value = app.businessPhone.ifBlank { app.mobileNumber }
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "হোয়াটসঅ্যাপ"
                                AppLanguage.HINDI -> "व्हाट्सएप"
                                AppLanguage.ENGLISH -> "WhatsApp"
                            },
                            value = app.whatsappNumber.ifBlank { app.mobileNumber }
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "খোলা থাকার সময়"
                                AppLanguage.HINDI -> "खुलने का समय"
                                AppLanguage.ENGLISH -> "Opening Hours"
                            },
                            value = app.getDisplayOpeningHours(currentLanguage)
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ডেলিভারি সুবিধা"
                                AppLanguage.HINDI -> "डिलीवरी सुविधा"
                                AppLanguage.ENGLISH -> "Delivery"
                            },
                            value = if (app.isDeliveryAvailable) {
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "হ্যাঁ (ব্যাসার্ধ: ${app.deliveryRadiusKm} কিমি)"
                                    AppLanguage.HINDI -> "हाँ (दायरा: ${app.deliveryRadiusKm} किमी)"
                                    AppLanguage.ENGLISH -> "Yes (Radius: ${app.deliveryRadiusKm} km)"
                                }
                            } else {
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "শুধুমাত্র নার্সারি থেকে সংগ্রহ"
                                    AppLanguage.HINDI -> "केवल नर्सरी से पिकअप"
                                    AppLanguage.ENGLISH -> "Pickup Only"
                                }
                            }
                        )
                    }

                    // Address & Location
                    SectionBlock(
                        title = when (currentLanguage) {
                            AppLanguage.BENGALI -> "৪. অবস্থান ও ঠিকানা"
                            AppLanguage.HINDI -> "4. स्थान व पता"
                            AppLanguage.ENGLISH -> "4. Address & Location"
                        }
                    ) {
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "সম্পূর্ণ ঠিকানা"
                                AppLanguage.HINDI -> "पूरा पता"
                                AppLanguage.ENGLISH -> "Full Address"
                            },
                            value = app.getDisplayFullAddress(currentLanguage)
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "শহর/গ্রাম"
                                AppLanguage.HINDI -> "शहर/गाँव"
                                AppLanguage.ENGLISH -> "City/Town"
                            },
                            value = app.cityOrTown
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "জেলা ও রাজ্য"
                                AppLanguage.HINDI -> "ज़िला व राज्य"
                                AppLanguage.ENGLISH -> "District & State"
                            },
                            value = "${app.district}, ${app.state}"
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "পিন কোড"
                                AppLanguage.HINDI -> "पिन कोड"
                                AppLanguage.ENGLISH -> "PIN Code"
                            },
                            value = app.pinCode
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "GPS কো-অর্ডিনেট"
                                AppLanguage.HINDI -> "GPS निर्देशांक"
                                AppLanguage.ENGLISH -> "GPS Coordinates"
                            },
                            value = "${app.latitude}, ${app.longitude}"
                        )
                    }

                    // Documents & Verification
                    SectionBlock(
                        title = when (currentLanguage) {
                            AppLanguage.BENGALI -> "৫. সরকারি নথি ও নিবন্ধন"
                            AppLanguage.HINDI -> "5. सरकारी दस्तावेज़ व पंजीकरण"
                            AppLanguage.ENGLISH -> "5. Documents & Verification"
                        }
                    ) {
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ট্রেড লাইসেন্স নম্বর"
                                AppLanguage.HINDI -> "ट्रेड लाइसेंस नंबर"
                                AppLanguage.ENGLISH -> "Trade License No."
                            },
                            value = app.tradeLicenseNumber.ifBlank { notSpecified }
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নথির ধরন"
                                AppLanguage.HINDI -> "दस्तावेज़ प्रकार"
                                AppLanguage.ENGLISH -> "Document Type"
                            },
                            value = app.documentType
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নথি সংক্রান্ত বিবরণ"
                                AppLanguage.HINDI -> "दस्तावेज़ विवरण"
                                AppLanguage.ENGLISH -> "Document Notes"
                            },
                            value = app.documentNotes.ifBlank { notSpecified }
                        )
                        DetailItem(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "GST নম্বর"
                                AppLanguage.HINDI -> "GST नंबर"
                                AppLanguage.ENGLISH -> "GST Number"
                            },
                            value = app.gstNumber.ifBlank {
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "প্রযোজ্য নয়"
                                    AppLanguage.HINDI -> "लागू नहीं"
                                    AppLanguage.ENGLISH -> "N/A"
                                }
                            }
                        )
                    }

                    if (app.adminRejectionReason.isNotBlank()) {
                        SectionBlock(
                            title = when (currentLanguage) {
                                AppLanguage.BENGALI -> "পূর্ববর্তী মন্তব্য/কারণ"
                                AppLanguage.HINDI -> "पिछली टिप्पणी/कारण"
                                AppLanguage.ENGLISH -> "Previous Remarks/Reason"
                            }
                        ) {
                            Text(
                                text = app.adminRejectionReason,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Action Buttons
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 2
                ) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.RoyalGreenEmerald, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "অনুমোদন করুন"
                                AppLanguage.HINDI -> "स्वीकृत करें"
                                AppLanguage.ENGLISH -> "Approve"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "বাতিল করুন"
                                AppLanguage.HINDI -> "अस्वीकार करें"
                                AppLanguage.ENGLISH -> "Reject"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = onRequestInfo,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Outlined.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "তথ্য চান"
                                AppLanguage.HINDI -> "जानकारी मांगें"
                                AppLanguage.ENGLISH -> "Request Info"
                            },
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (app.status == NurseryApplicationStatus.APPROVED.code) {
                        OutlinedButton(
                            onClick = onSuspend,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.RoyalSoftGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.PauseCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "স্থগিত"
                                    AppLanguage.HINDI -> "निलंबित"
                                    AppLanguage.ENGLISH -> "Suspend"
                                },
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionBlock(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}
