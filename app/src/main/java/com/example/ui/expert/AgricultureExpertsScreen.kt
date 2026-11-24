package com.example.ui.expert

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.ExpertTranslations
import com.example.data.local.entity.ConsultationAppointmentEntity
import com.example.data.local.entity.ExpertCategories
import com.example.data.local.entity.ExpertEntity
import com.example.data.model.AppLanguage
import com.example.data.repository.ExpertRepository
import com.example.ui.theme.AgriAlertRed
import com.example.ui.theme.AgriEarthSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriPriceGold
import com.example.util.isCompactNarrowScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class ExpertViewMode {
    DIRECTORY,
    MY_APPOINTMENTS,
    INQUIRY_HISTORY,
    ADMIN_PORTAL
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgricultureExpertsScreen(
    currentLanguage: AppLanguage,
    userId: String,
    farmerName: String,
    onBack: () -> Unit,
    onOpenAdminConfig: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { ExpertRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    // Ensure initial seed data is loaded
    LaunchedEffect(Unit) {
        repository.seedInitialExpertsIfEmpty()
    }

    val verifiedExperts by repository.verifiedExperts.collectAsState(initial = emptyList())
    val allExperts by repository.allExperts.collectAsState(initial = emptyList())
    val myAppointments by repository.getAppointmentsForFarmer(userId).collectAsState(initial = emptyList())
    val auditLogs by repository.getAuditLogs().collectAsState(initial = emptyList())

    var viewMode by remember { mutableStateOf(ExpertViewMode.DIRECTORY) }

    // Active sub-screen navigations
    var activeChatExpert by remember { mutableStateOf<ExpertEntity?>(null) }
    var activeCallExpert by remember { mutableStateOf<Pair<ExpertEntity, Boolean>?>(null) } // expert to isVideo

    // Search and filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryKey by remember { mutableStateOf<String?>(null) }
    var onlineOnly by remember { mutableStateOf(false) }

    // Appointment Booking Dialog state
    var bookingExpert by remember { mutableStateOf<ExpertEntity?>(null) }

    // Handle back button on sub-screens
    BackHandler(enabled = activeChatExpert != null || activeCallExpert != null) {
        if (activeCallExpert != null) {
            activeCallExpert = null
        } else if (activeChatExpert != null) {
            activeChatExpert = null
        }
    }

    if (activeCallExpert != null) {
        val (expert, isVideo) = activeCallExpert!!
        ConsultationCallScreen(
            expert = expert,
            isVideoCall = isVideo,
            currentLanguage = currentLanguage,
            onEndCall = { activeCallExpert = null }
        )
        return
    }

    if (activeChatExpert != null) {
        ExpertChatScreen(
            expert = activeChatExpert!!,
            farmerId = userId,
            farmerName = farmerName,
            currentLanguage = currentLanguage,
            onBack = { activeChatExpert = null },
            onStartAudioCall = {
                activeCallExpert = Pair(activeChatExpert!!, false)
            },
            onStartVideoCall = {
                activeCallExpert = Pair(activeChatExpert!!, true)
            }
        )
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("expert_nav_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "কৃষি বিশেষজ্ঞ"
                                    AppLanguage.HINDI -> "कृषि विशेषज्ञ"
                                    AppLanguage.ENGLISH -> "Agriculture Experts"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "AGRICULTURE EXPERTS",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = com.example.ui.theme.RoyalGold,
                                letterSpacing = 0.8.sp,
                                maxLines = 1
                            )
                        }

                        // Toll Free Kisan Call Center Quick Call Icon
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x33FFFFFF),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clickable {
                                    val tollMsg = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "টোল ফ্রি কিষাণ কল সেন্টার: ১৮০০-১৮০-১৫৫১"
                                        AppLanguage.HINDI -> "टोल फ्री किसान कॉल सेंटर: 1800-180-1551"
                                        AppLanguage.ENGLISH -> "Toll-Free Kisan Call Centre: 1800-180-1551"
                                    }
                                    Toast.makeText(context, tollMsg, Toast.LENGTH_LONG).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Toll Free Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                if (!isCompactNarrowScreen()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentLanguage == AppLanguage.BENGALI) "১৮০০-১৮০-১৫৫১" else "1800-180-1551",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Navigation Tabs
                    ScrollableTabRow(
                        selectedTabIndex = viewMode.ordinal.coerceAtMost(2),
                        containerColor = AgriGreenPrimary,
                        contentColor = Color.White,
                        edgePadding = 8.dp
                    ) {
                        Tab(
                            selected = viewMode == ExpertViewMode.DIRECTORY,
                            onClick = { viewMode = ExpertViewMode.DIRECTORY },
                            text = {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "বিশেষজ্ঞ খুঁজুন"
                                        AppLanguage.HINDI -> "विशेषज्ञ खोजें"
                                        AppLanguage.ENGLISH -> "Find Experts"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            },
                            icon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = viewMode == ExpertViewMode.MY_APPOINTMENTS,
                            onClick = { viewMode = ExpertViewMode.MY_APPOINTMENTS },
                            text = {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অ্যাপয়েন্টমেন্ট (${myAppointments.size})"
                                        AppLanguage.HINDI -> "अपॉइंटमेंट (${myAppointments.size})"
                                        AppLanguage.ENGLISH -> "Bookings (${myAppointments.size})"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            },
                            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = viewMode == ExpertViewMode.ADMIN_PORTAL,
                            onClick = { viewMode = ExpertViewMode.ADMIN_PORTAL },
                            text = {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অ্যাডমিন ও যাচাই"
                                        AppLanguage.HINDI -> "एडमिन व सत्यापन"
                                        AppLanguage.ENGLISH -> "Admin & Verify"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .imePadding()
            ) {
                when (viewMode) {
                    ExpertViewMode.DIRECTORY -> {
                        ExpertDirectoryContent(
                            verifiedExperts = verifiedExperts,
                            currentLanguage = currentLanguage,
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            selectedCategoryKey = selectedCategoryKey,
                            onSelectCategory = { selectedCategoryKey = it },
                            onlineOnly = onlineOnly,
                            onToggleOnlineOnly = { onlineOnly = !onlineOnly },
                            onResetFilters = {
                                selectedCategoryKey = null
                                searchQuery = ""
                                onlineOnly = false
                            },
                            onChatClick = { expert -> activeChatExpert = expert },
                            onAudioCallClick = { expert -> activeCallExpert = Pair(expert, false) },
                            onVideoCallClick = { expert -> activeCallExpert = Pair(expert, true) },
                            onBookClick = { expert -> bookingExpert = expert }
                        )
                    }
                    ExpertViewMode.MY_APPOINTMENTS -> {
                        FarmerAppointmentsContent(
                            appointments = myAppointments,
                            currentLanguage = currentLanguage,
                            onCancelAppointment = { apptId ->
                                coroutineScope.launch {
                                    try {
                                        val note = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "কৃষক নিজে বাতিল করেছেন"
                                            AppLanguage.HINDI -> "किसान द्वारा रद्द किया गया"
                                            AppLanguage.ENGLISH -> "Cancelled by farmer"
                                        }
                                        repository.updateAppointmentStatus(apptId, "CANCELLED", note)
                                        val msg = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "অ্যাপয়েন্টমেন্ট বাতিল করা হয়েছে"
                                            AppLanguage.HINDI -> "अपॉइंटमेंट रद्द कर दिया गया है"
                                            AppLanguage.ENGLISH -> "Appointment cancelled"
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    } catch (ce: CancellationException) {
                                        throw ce
                                    } catch (e: Exception) {
                                        Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                    ExpertViewMode.INQUIRY_HISTORY -> {
                        viewMode = ExpertViewMode.DIRECTORY
                    }
                    ExpertViewMode.ADMIN_PORTAL -> {
                        AdminPortalContent(
                            allExperts = allExperts,
                            auditLogs = auditLogs,
                            currentLanguage = currentLanguage,
                            onApprove = { expert ->
                                val dispName = ExpertTranslations.getDisplayName(expert, currentLanguage)
                                coroutineScope.launch {
                                    try {
                                        repository.verifyOrRejectExpert(
                                            expertId = expert.id,
                                            expertName = dispName,
                                            approve = true,
                                            reason = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "কৃষি বিশ্ববিদ্যালয় ও ডিগ্রি যাচাইকরণ সফল।"
                                                AppLanguage.HINDI -> "कृषि विश्वविद्यालय व डिग्री सत्यापन सफल।"
                                                AppLanguage.ENGLISH -> "University degree & registration verified."
                                            },
                                            adminName = "West Bengal Agriculture Directorate"
                                        )
                                        val msg = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "$dispName যাচাইকৃত বিশেষজ্ঞ হিসেবে অনুমোদিত হয়েছে।"
                                            AppLanguage.HINDI -> "$dispName को प्रमाणित विशेषज्ञ के रूप में स्वीकृत किया गया।"
                                            AppLanguage.ENGLISH -> "$dispName approved as a verified expert."
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    } catch (ce: CancellationException) {
                                        throw ce
                                    } catch (e: Exception) {
                                        Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onReject = { expert ->
                                val dispName = ExpertTranslations.getDisplayName(expert, currentLanguage)
                                coroutineScope.launch {
                                    try {
                                        repository.verifyOrRejectExpert(
                                            expertId = expert.id,
                                            expertName = dispName,
                                            approve = false,
                                            reason = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "নথিপত্রে তথ্যের অসংগতি।"
                                                AppLanguage.HINDI -> "दस्तावेजों में जानकारी की विसंगति।"
                                                AppLanguage.ENGLISH -> "Discrepancy in submitted documents."
                                            },
                                            adminName = "West Bengal Agriculture Directorate"
                                        )
                                        val msg = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "$dispName এর আবেদন নামঞ্জুর করা হয়েছে।"
                                            AppLanguage.HINDI -> "$dispName का आवेदन अस्वीकार कर दिया गया।"
                                            AppLanguage.ENGLISH -> "$dispName application rejected/suspended."
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    } catch (ce: CancellationException) {
                                        throw ce
                                    } catch (e: Exception) {
                                        Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onSubmitNewExpert = { newExp ->
                                coroutineScope.launch {
                                    try {
                                        repository.applyForExpert(newExp)
                                        val msg = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "নতুন বিশেষজ্ঞের আবেদন গৃহীত হয়েছে।"
                                            AppLanguage.HINDI -> "नए विशेषज्ञ का आवेदन प्राप्त हुआ।"
                                            AppLanguage.ENGLISH -> "New expert application submitted."
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    } catch (ce: CancellationException) {
                                        throw ce
                                    } catch (e: Exception) {
                                        Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onOpenAdminConfig = onOpenAdminConfig
                        )
                    }
                }
            }
        }
    }

    // Appointment Booking Dialog
    if (bookingExpert != null) {
        AppointmentBookingDialog(
            expert = bookingExpert!!,
            farmerId = userId,
            farmerName = farmerName,
            currentLanguage = currentLanguage,
            onDismiss = { bookingExpert = null },
            onConfirmBooking = { appointment ->
                coroutineScope.launch {
                    try {
                        val result = repository.bookAppointment(appointment, currentLanguage)
                        result.onSuccess {
                            bookingExpert = null
                            val okMsg = when (currentLanguage) {
                                AppLanguage.BENGALI -> "অ্যাপয়েন্টমেন্ট সফলভাবে বুকিং করা হয়েছে!"
                                AppLanguage.HINDI -> "अपॉइंटमेंट सफलतापूर्वक बुक हो गया है!"
                                AppLanguage.ENGLISH -> "Appointment booked successfully!"
                            }
                            Toast.makeText(context, okMsg, Toast.LENGTH_LONG).show()
                            viewMode = ExpertViewMode.MY_APPOINTMENTS
                        }.onFailure { ex ->
                            val errMsg = when (currentLanguage) {
                                AppLanguage.BENGALI -> ex.message ?: "বুকিং ব্যর্থ হয়েছে"
                                AppLanguage.HINDI -> ex.message ?: "बुकिंग विफल रही"
                                AppLanguage.ENGLISH -> ex.message ?: "Booking failed"
                            }
                            Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                        }
                    } catch (ce: CancellationException) {
                        throw ce
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message ?: "Booking failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

@Composable
private fun ExpertDirectoryContent(
    verifiedExperts: List<ExpertEntity>,
    currentLanguage: AppLanguage,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategoryKey: String?,
    onSelectCategory: (String?) -> Unit,
    onlineOnly: Boolean,
    onToggleOnlineOnly: () -> Unit,
    onResetFilters: () -> Unit,
    onChatClick: (ExpertEntity) -> Unit,
    onAudioCallClick: (ExpertEntity) -> Unit,
    onVideoCallClick: (ExpertEntity) -> Unit,
    onBookClick: (ExpertEntity) -> Unit
) {
    // Filter logic matches localized + original fields so searching in any language works
    val filteredExperts = verifiedExperts.filter { exp ->
        val matchesCategory = selectedCategoryKey == null || exp.category == selectedCategoryKey
        val matchesOnline = !onlineOnly || exp.availabilityStatus == "ONLINE_NOW"
        val query = searchQuery.trim().lowercase()
        val matchesQuery = query.isEmpty() ||
                exp.name.lowercase().contains(query) ||
                ExpertTranslations.getDisplayName(exp, currentLanguage).lowercase().contains(query) ||
                exp.institution.lowercase().contains(query) ||
                ExpertTranslations.getDisplayInstitution(exp, currentLanguage).lowercase().contains(query) ||
                exp.supportedCrops.lowercase().contains(query) ||
                ExpertTranslations.getDisplayCrops(exp, currentLanguage).lowercase().contains(query) ||
                exp.specializedProblems.lowercase().contains(query) ||
                ExpertTranslations.getDisplayProblems(exp, currentLanguage).lowercase().contains(query) ||
                exp.districtsServed.lowercase().contains(query) ||
                ExpertTranslations.getDisplayDistricts(exp, currentLanguage).lowercase().contains(query) ||
                exp.languages.lowercase().contains(query)
        matchesCategory && matchesOnline && matchesQuery
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Section Subtitle Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "👨‍🔬", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "আপনার ফসলের সমস্যায় সরাসরি বিশেষজ্ঞের পরামর্শ নিন।"
                                AppLanguage.HINDI -> "अपनी फसल की समस्याओं के लिए सीधे कृषि विशेषज्ञ से सलाह लें।"
                                AppLanguage.ENGLISH -> "Consult verified agriculture experts directly for crop issues."
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "BCKV, UBKV, ICAR ও কৃষি বিজ্ঞান কেন্দ্রের সরকারি যাচাইকৃত বিজ্ঞানী ও উদ্ভিদ ডাক্তারবৃন্দ।"
                                AppLanguage.HINDI -> "BCKV, UBKV, ICAR व कृषि विज्ञान केंद्र के सत्यापित वैज्ञानिक और पौध चिकित्सक।"
                                AppLanguage.ENGLISH -> "Verified scientists and plant doctors from BCKV, UBKV, ICAR & Krishi Vigyan Kendras."
                            },
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expert_search_input"),
                placeholder = {
                    Text(when (currentLanguage) {
                        AppLanguage.BENGALI -> "ফসল (আলু, ধান, চা) বা রোগ (ধসা, পোকা) দিয়ে খুঁজুন..."
                        AppLanguage.HINDI -> "फसल (आलू, धान, चाय) या रोग से खोजें..."
                        AppLanguage.ENGLISH -> "Search crop (Potato, Rice, Tea) or disease..."
                    })
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchChange("") },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        // 15 Categories Horizontal Filter Bar
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "বিশেষজ্ঞ বিভাগ (১৫টি ক্ষেত্র):"
                            AppLanguage.HINDI -> "विशेषज्ञ विभाग (15 क्षेत्र):"
                            AppLanguage.ENGLISH -> "Specialist Categories (15 Fields):"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (onlineOnly) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clickable { onToggleOnlineOnly() }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (onlineOnly) {
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "🟢 শুধু অনলাইন"
                                        AppLanguage.HINDI -> "🟢 केवल ऑनलाइन"
                                        AppLanguage.ENGLISH -> "🟢 Online Only"
                                    }
                                } else {
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "সবাই"
                                        AppLanguage.HINDI -> "सभी"
                                        AppLanguage.ENGLISH -> "All"
                                    }
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (selectedCategoryKey == null) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clickable { onSelectCategory(null) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "সকল বিশেষজ্ঞ"
                                        AppLanguage.HINDI -> "सभी विशेषज्ञ"
                                        AppLanguage.ENGLISH -> "All Experts"
                                    },
                                    color = if (selectedCategoryKey == null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    items(ExpertCategories.ALL_CATEGORIES) { cat ->
                        val isSelected = selectedCategoryKey == cat.key
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clickable { onSelectCategory(cat.key) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = cat.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.getName(currentLanguage),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Empty State
        if (filteredExperts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🌱", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "বর্তমানে কোনো যাচাইকৃত বিশেষজ্ঞ উপলব্ধ নেই। নতুন বিশেষজ্ঞ যুক্ত হলে এখানে দেখতে পাবেন।"
                                AppLanguage.HINDI -> "वर्तमान में कोई सत्यापित विशेषज्ञ उपलब्ध नहीं है। नए विशेषज्ञ जुड़ने पर यहाँ दिखाई देंगे।"
                                AppLanguage.ENGLISH -> "No verified experts match your filter right now. Try resetting filters to view all specialists."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        if (selectedCategoryKey != null || searchQuery.isNotEmpty() || onlineOnly) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = onResetFilters,
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text(when (currentLanguage) {
                                    AppLanguage.BENGALI -> "সকল ফিল্টার মুছুন"
                                    AppLanguage.HINDI -> "सभी फ़िल्टर हटाएं"
                                    AppLanguage.ENGLISH -> "Reset Filters"
                                })
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredExperts, key = { it.id }) { expert ->
                var showMoreInfo by remember(expert.id) { mutableStateOf(false) }
                VerifiedExpertCard(
                    expert = expert,
                    currentLanguage = currentLanguage,
                    onChatClick = { onChatClick(expert) },
                    onAudioCallClick = { onAudioCallClick(expert) },
                    onVideoCallClick = { onVideoCallClick(expert) },
                    onBookClick = { onBookClick(expert) },
                    onMoreInfoClick = { showMoreInfo = true }
                )
                if (showMoreInfo) {
                    ExpertMoreInfoDialog(
                        expert = expert,
                        currentLanguage = currentLanguage,
                        onDismiss = { showMoreInfo = false },
                        onChatClick = {
                            showMoreInfo = false
                            onChatClick(expert)
                        },
                        onBookClick = {
                            showMoreInfo = false
                            onBookClick(expert)
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VerifiedExpertCard(
    expert: ExpertEntity,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onChatClick: () -> Unit,
    onAudioCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onBookClick: () -> Unit,
    onMoreInfoClick: (() -> Unit)? = null
) {
    val isNarrow = isCompactNarrowScreen()
    val dispName = ExpertTranslations.getDisplayName(expert, currentLanguage)
    val dispDesignation = ExpertTranslations.getDisplayDesignation(expert, currentLanguage)
    val dispInstitution = ExpertTranslations.getDisplayInstitution(expert, currentLanguage)
    val dispDistricts = ExpertTranslations.getDisplayDistricts(expert, currentLanguage)
    val dispCrops = ExpertTranslations.getDisplayCrops(expert, currentLanguage)
    val dispProblems = ExpertTranslations.getDisplayProblems(expert, currentLanguage)
    val dispLanguages = ExpertTranslations.getDisplayLanguages(expert, currentLanguage)
    val dispFee = ExpertTranslations.getDisplayFee(expert.consultationFee, currentLanguage)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expert_card_${expert.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Avatar, Name, Verified Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = expert.photoAvatar, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dispName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dispDesignation,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dispInstitution,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Verification Status Badge (Strictly gated on expert.isVerified)
                if (expert.isVerified) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Badge",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "যাচাইকৃত"
                                    AppLanguage.HINDI -> "सत्यापित"
                                    AppLanguage.ENGLISH -> "VERIFIED"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = com.example.ui.theme.RoyalWarningBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.RoyalGold)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "যাচাই প্রক্রিয়াধীন"
                                AppLanguage.HINDI -> "सत्यापन लंबित"
                                AppLanguage.ENGLISH -> "PENDING"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = com.example.ui.theme.RoyalSoftGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Key Credentials Grid
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "🎓 যোগ্যতা: "
                                AppLanguage.HINDI -> "🎓 योग्यता: "
                                AppLanguage.ENGLISH -> "🎓 Qualification: "
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = expert.qualification,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "🆔 রেজিস্ট্রেশন নং: "
                                AppLanguage.HINDI -> "🆔 पंजीकरण सं.: "
                                AppLanguage.ENGLISH -> "🆔 Registration ID: "
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = expert.registrationId,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "⏱️ অভিজ্ঞতা: "
                                AppLanguage.HINDI -> "⏱️ अनुभव: "
                                AppLanguage.ENGLISH -> "⏱️ Experience: "
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val expText = when (currentLanguage) {
                            AppLanguage.BENGALI -> "${expert.yearsExperience} বছর | ভাষা: $dispLanguages"
                            AppLanguage.HINDI -> "${expert.yearsExperience} वर्ष | भाषाएं: $dispLanguages"
                            AppLanguage.ENGLISH -> "${expert.yearsExperience} yrs | Languages: $dispLanguages"
                        }
                        Text(
                            text = expText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "📍 সেবাপ্রাপ্ত জেলা: "
                                AppLanguage.HINDI -> "📍 सेवा क्षेत्र: "
                                AppLanguage.ENGLISH -> "📍 Districts Served: "
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dispDistricts,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Supported crops and specialty tags
            if (dispCrops.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "🌾 ফসল: "
                            AppLanguage.HINDI -> "🌾 फसलें: "
                            AppLanguage.ENGLISH -> "🌾 Crops: "
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dispCrops,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (dispProblems.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "🔬 বিশেষজ্ঞতা: "
                            AppLanguage.HINDI -> "🔬 विशेषज्ञता: "
                            AppLanguage.ENGLISH -> "🔬 Focus Areas: "
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = dispProblems,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Rating, Fee & Online Status Bar
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = AgriPriceGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "${expert.rating} (${expert.reviewCount} জন কৃষক)"
                            AppLanguage.HINDI -> "${expert.rating} (${expert.reviewCount} किसान)"
                            AppLanguage.ENGLISH -> "${expert.rating} (${expert.reviewCount} farmers)"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (expert.availabilityStatus == "ONLINE_NOW") MaterialTheme.colorScheme.primaryContainer else com.example.ui.theme.RoyalWarningBg
                ) {
                    Text(
                        text = if (expert.availabilityStatus == "ONLINE_NOW") {
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "🟢 এখনই অনলাইন"
                                AppLanguage.HINDI -> "🟢 अभी ऑनलाइन"
                                AppLanguage.ENGLISH -> "🟢 Online Now"
                            }
                        } else {
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "🟡 আজ উপলব্ধ"
                                AppLanguage.HINDI -> "🟡 आज उपलब्ध"
                                AppLanguage.ENGLISH -> "🟡 Available Today"
                            }
                        },
                        color = if (expert.availabilityStatus == "ONLINE_NOW") MaterialTheme.colorScheme.onPrimaryContainer else com.example.ui.theme.RoyalSoftGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = dispFee,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Farmer Action Buttons: 2x2 grid on phones / non-English labels so text never clips or truncates
            if (isNarrow || currentLanguage != AppLanguage.ENGLISH) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onChatClick,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("expert_chat_btn_${expert.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AgriGreenPrimary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "চ্যাট"
                                    AppLanguage.HINDI -> "चैट"
                                    AppLanguage.ENGLISH -> "Chat"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onAudioCallClick,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("expert_audio_btn_${expert.id}"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অডিও"
                                    AppLanguage.HINDI -> "ऑडियो"
                                    AppLanguage.ENGLISH -> "Audio"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onVideoCallClick,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("expert_video_btn_${expert.id}"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ভিডিও"
                                    AppLanguage.HINDI -> "वीडियो"
                                    AppLanguage.ENGLISH -> "Video"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = onBookClick,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("expert_book_btn_${expert.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = com.example.ui.theme.RoyalGreenEmerald,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "বুকিং"
                                    AppLanguage.HINDI -> "बुकिंग"
                                    AppLanguage.ENGLISH -> "Book"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onChatClick,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("expert_chat_btn_${expert.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgriGreenPrimary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onAudioCallClick,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("expert_audio_btn_${expert.id}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Audio", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    OutlinedButton(
                        onClick = onVideoCallClick,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("expert_video_btn_${expert.id}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Video", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Button(
                        onClick = onBookClick,
                        modifier = Modifier
                            .weight(1.1f)
                            .heightIn(min = 48.dp)
                            .testTag("expert_book_btn_${expert.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.example.ui.theme.RoyalGreenEmerald,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Book", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (onMoreInfoClick != null) {
                OutlinedButton(
                    onClick = onMoreInfoClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("expert_more_info_btn_${expert.id}"),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = com.example.util.L10n.t("dash_more_info", currentLanguage),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ExpertMoreInfoDialog(
    expert: ExpertEntity,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onChatClick: () -> Unit,
    onBookClick: () -> Unit
) {
    val dispName = ExpertTranslations.getDisplayName(expert, currentLanguage)
    val dispDesignation = ExpertTranslations.getDisplayDesignation(expert, currentLanguage)
    val dispInstitution = ExpertTranslations.getDisplayInstitution(expert, currentLanguage)
    val dispDistricts = ExpertTranslations.getDisplayDistricts(expert, currentLanguage)
    val dispCrops = ExpertTranslations.getDisplayCrops(expert, currentLanguage)
    val dispProblems = ExpertTranslations.getDisplayProblems(expert, currentLanguage)
    val dispLanguages = ExpertTranslations.getDisplayLanguages(expert, currentLanguage)

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = com.example.ui.theme.RoyalGreenCardBg,
        titleContentColor = com.example.ui.theme.RoyalGold,
        textContentColor = com.example.ui.theme.RoyalTextPrimary,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = dispName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = dispDesignation, fontSize = 12.sp, color = com.example.ui.theme.RoyalTextSecondary)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = dispInstitution, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = com.example.ui.theme.RoyalSoftGold)
                Text(
                    text = "${expert.qualification}  •  Reg: ${expert.registrationId}",
                    fontSize = 12.sp,
                    color = com.example.ui.theme.RoyalTextSecondary
                )
                Text(
                    text = "⭐ ${expert.rating} (${expert.reviewCount} ${com.example.util.L10n.t("dash_consultations_label", currentLanguage)})  •  ${expert.yearsExperience} yrs",
                    fontSize = 12.sp,
                    color = com.example.ui.theme.RoyalTextPrimary
                )
                if (expert.bio.isNotBlank()) {
                    Text(text = expert.bio, fontSize = 12.sp, lineHeight = 18.sp, color = com.example.ui.theme.RoyalTextSecondary)
                }
                Text(text = "$dispDistricts", fontSize = 12.sp, color = com.example.ui.theme.RoyalTextSecondary)
                if (dispCrops.isNotBlank()) {
                    Text(text = dispCrops, fontSize = 12.sp, color = com.example.ui.theme.RoyalSoftGold)
                }
                if (dispProblems.isNotBlank()) {
                    Text(text = dispProblems, fontSize = 12.sp, color = com.example.ui.theme.RoyalTextSecondary)
                }
                Text(text = dispLanguages, fontSize = 11.sp, color = com.example.ui.theme.RoyalTextSecondary)
            }
        },
        confirmButton = {
            Button(
                onClick = onBookClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.example.ui.theme.RoyalGreenEmerald,
                    contentColor = Color.White
                ),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "বুকিং"
                        AppLanguage.HINDI -> "बुकिंग"
                        AppLanguage.ENGLISH -> "Book"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onChatClick, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "চ্যাট"
                            AppLanguage.HINDI -> "चैट"
                            AppLanguage.ENGLISH -> "Chat"
                        },
                        color = com.example.ui.theme.RoyalSoftGold,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(com.example.util.L10n.t("btn_close", currentLanguage), color = com.example.ui.theme.RoyalTextSecondary)
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FarmerAppointmentsContent(
    appointments: List<ConsultationAppointmentEntity>,
    currentLanguage: AppLanguage,
    onCancelAppointment: (String) -> Unit
) {
    if (appointments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 480.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "📅", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "আপনার কোনো শিডিউল করা অ্যাপয়েন্টমেন্ট নেই।\nবিশেষজ্ঞ তালিকায় গিয়ে পরামর্শের সময় বুকিং করুন।"
                        AppLanguage.HINDI -> "आपका कोई निर्धारित अपॉइंटमेंट नहीं है।\nविशेषज्ञ सूची में जाकर परामर्श समय बुक करें।"
                        AppLanguage.ENGLISH -> "No scheduled appointments yet.\nVisit the Experts tab to book a consultation slot."
                    },
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "আমার বিশেষজ্ঞ পরামর্শ বুকিং (${appointments.size}):"
                            AppLanguage.HINDI -> "मेरी विशेषज्ञ परामर्श बुकिंग (${appointments.size}):"
                            AppLanguage.ENGLISH -> "My Expert Consultation Bookings (${appointments.size}):"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(appointments, key = { it.id }) { appt ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ExpertTranslations.getDisplayAppointmentExpertName(appt.expertId, appt.expertName, currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )

                                val statusColor = when (appt.status) {
                                    "CONFIRMED" -> MaterialTheme.colorScheme.primary
                                    "PENDING" -> com.example.ui.theme.RoyalSoftGold
                                    "REJECTED" -> MaterialTheme.colorScheme.error
                                    "COMPLETED" -> MaterialTheme.colorScheme.secondary
                                    "CANCELLED" -> MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = statusColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = ExpertTranslations.getAppointmentStatusLabel(appt.status, currentLanguage),
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            val cropPrefix = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ফসল:"
                                AppLanguage.HINDI -> "फसल:"
                                AppLanguage.ENGLISH -> "Crop:"
                            }
                            val problemPrefix = when (currentLanguage) {
                                AppLanguage.BENGALI -> "সমস্যা:"
                                AppLanguage.HINDI -> "समस्या:"
                                AppLanguage.ENGLISH -> "Issue:"
                            }
                            Text(
                                text = "$cropPrefix ${ExpertTranslations.getDisplaySampleText(appt.cropName, currentLanguage)}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$problemPrefix ${ExpertTranslations.getDisplaySampleText(appt.problemDescription, currentLanguage)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val dateTimeLabel = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "তারিখ: ${appt.scheduledDate} | সময়: ${appt.scheduledTimeSlot}"
                                        AppLanguage.HINDI -> "तिथि: ${appt.scheduledDate} | समय: ${appt.scheduledTimeSlot}"
                                        AppLanguage.ENGLISH -> "Date: ${appt.scheduledDate} | Slot: ${appt.scheduledTimeSlot}"
                                    }
                                    val methodLabel = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "পদ্ধতি:"
                                        AppLanguage.HINDI -> "माध्यम:"
                                        AppLanguage.ENGLISH -> "Mode:"
                                    }
                                    Text(
                                        text = dateTimeLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$methodLabel ${ExpertTranslations.getConsultationMethodLabel(appt.consultationMethod, currentLanguage)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            if (appt.meetingNotes.isNotBlank()) {
                                val notePrefix = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "বিজ্ঞানী নোট:"
                                    AppLanguage.HINDI -> "विशेषज्ञ टिप्पणी:"
                                    AppLanguage.ENGLISH -> "Expert Note:"
                                }
                                Text(
                                    text = "$notePrefix ${ExpertTranslations.getDisplaySampleText(appt.meetingNotes, currentLanguage)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp
                                )
                            }

                            if (appt.status == "PENDING" || appt.status == "CONFIRMED") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { onCancelAppointment(appt.id) },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "বুকিং বাতিল করুন"
                                                AppLanguage.HINDI -> "बुकिंग रद्द करें"
                                                AppLanguage.ENGLISH -> "Cancel Booking"
                                            },
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminPortalContent(
    allExperts: List<ExpertEntity>,
    auditLogs: List<com.example.data.local.entity.ExpertAuditLogEntity>,
    currentLanguage: AppLanguage,
    onApprove: (ExpertEntity) -> Unit,
    onReject: (ExpertEntity) -> Unit,
    onSubmitNewExpert: (ExpertEntity) -> Unit,
    onOpenAdminConfig: () -> Unit = {}
) {
    var showAddExpertDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "🏛️ পশ্চিমবঙ্গ কৃষি উপদেষ্টা পরিষদ ও অ্যাডমিন প্যানেল"
                                AppLanguage.HINDI -> "🏛️ कृषि सलाहकार बोर्ड व एडमिन पैनल"
                                AppLanguage.ENGLISH -> "🏛️ Agriculture Advisory Board & Admin Panel"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নতুন বিশেষজ্ঞদের ডিগ্রি, রেজিস্ট্রেশন আইডি ও যোগ্যতা যাচাইপূর্বক অনুমোদন বা বাতিল করুন।"
                                AppLanguage.HINDI -> "नए विशेषज्ञों की डिग्री, पंजीकरण आईडी और योग्यता सत्यापित कर स्वीकृत या अस्वीकृत करें।"
                                AppLanguage.ENGLISH -> "Review degrees, registration IDs, and credentials before approving or suspending experts."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showAddExpertDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AgriGreenPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "+ নতুন বিশেষজ্ঞ"
                                        AppLanguage.HINDI -> "+ नए विशेषज्ञ"
                                        AppLanguage.ENGLISH -> "+ Add Expert"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onOpenAdminConfig,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "⚙️ ফুটার ও সরকারি তথ্য"
                                        AppLanguage.HINDI -> "⚙️ फ़ुटर व सरकारी जानकारी"
                                        AppLanguage.ENGLISH -> "⚙️ Footer & Govt Info"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "বিশেষজ্ঞ যাচাইকরণ তালিকা (${allExperts.size}):"
                        AppLanguage.HINDI -> "विशेषज्ञ सत्यापन सूची (${allExperts.size}):"
                        AppLanguage.ENGLISH -> "Expert Verification Roster (${allExperts.size}):"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(allExperts, key = { it.id }) { expert ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ExpertTranslations.getDisplayName(expert, currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${ExpertTranslations.getDisplayDesignation(expert, currentLanguage)} • ${ExpertTranslations.getDisplayInstitution(expert, currentLanguage)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (expert.isVerified) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = if (expert.isVerified) {
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "যাচাইকৃত (Approved)"
                                            AppLanguage.HINDI -> "सत्यापित (Approved)"
                                            AppLanguage.ENGLISH -> "Approved"
                                        }
                                    } else {
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "বিচারাধীন (Pending)"
                                            AppLanguage.HINDI -> "लंबित (Pending)"
                                            AppLanguage.ENGLISH -> "Pending"
                                        }
                                    },
                                    color = if (expert.isVerified) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        val regPrefix = when (currentLanguage) {
                            AppLanguage.BENGALI -> "রেজিস্ট্রেশন নং: ${expert.registrationId} | যোগ্যতা: ${expert.qualification}"
                            AppLanguage.HINDI -> "पंजीकरण सं.: ${expert.registrationId} | योग्यता: ${expert.qualification}"
                            AppLanguage.ENGLISH -> "Reg. ID: ${expert.registrationId} | Qual.: ${expert.qualification}"
                        }
                        Text(text = regPrefix, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!expert.isVerified) {
                                Button(
                                    onClick = { onApprove(expert) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AgriGreenPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "অনুমোদন করুন"
                                            AppLanguage.HINDI -> "स्वीकृत करें"
                                            AppLanguage.ENGLISH -> "Approve"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onReject(expert) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "বাতিল"
                                            AppLanguage.HINDI -> "अस्वीकार"
                                            AppLanguage.ENGLISH -> "Reject"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onReject(expert) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                ) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "স্থগিত করুন (Suspend)"
                                            AppLanguage.HINDI -> "निलंबित करें (Suspend)"
                                            AppLanguage.ENGLISH -> "Suspend / Deactivate"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "অডিট লগ ও যাচাইকরণ ইতিহাস (${auditLogs.size}):"
                        AppLanguage.HINDI -> "ऑडिट लॉग व सत्यापन इतिहास (${auditLogs.size}):"
                        AppLanguage.ENGLISH -> "Audit Logs & Verification History (${auditLogs.size}):"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(auditLogs, key = { it.id }) { log ->
                val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                val dateStr = dateFormat.format(Date(log.timestamp))
                val logExpertName = ExpertTranslations.getDisplayAppointmentExpertName(log.expertId, log.expertName, currentLanguage)
                val reasonLabel = when (currentLanguage) {
                    AppLanguage.BENGALI -> "কারণ:"
                    AppLanguage.HINDI -> "कारण:"
                    AppLanguage.ENGLISH -> "Reason:"
                }
                val adminLabel = when (currentLanguage) {
                    AppLanguage.BENGALI -> "অ্যাডমিন:"
                    AppLanguage.HINDI -> "एडमिन:"
                    AppLanguage.ENGLISH -> "Admin:"
                }

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$logExpertName — [${log.adminAction}]",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = dateStr,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$reasonLabel ${ExpertTranslations.getDisplaySampleText(log.reason, currentLanguage)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$adminLabel ${log.adminName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }

    if (showAddExpertDialog) {
        AddNewExpertApplicationDialog(
            currentLanguage = currentLanguage,
            onDismiss = { showAddExpertDialog = false },
            onSubmit = { exp ->
                onSubmitNewExpert(exp)
                showAddExpertDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppointmentBookingDialog(
    expert: ExpertEntity,
    farmerId: String,
    farmerName: String,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onDismiss: () -> Unit,
    onConfirmBooking: (ConsultationAppointmentEntity) -> Unit
) {
    val defaultCrop = when (currentLanguage) {
        AppLanguage.BENGALI -> "আলু"
        AppLanguage.HINDI -> "आलू"
        AppLanguage.ENGLISH -> "Potato"
    }
    var cropName by remember(currentLanguage) { mutableStateOf(defaultCrop) }
    var problemText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("2026-11-05") }
    var selectedTimeSlot by remember { mutableStateOf("11:00 AM - 11:30 AM") }
    var consultationMethod by remember { mutableStateOf("VIDEO_CALL") }

    val dateOptions = listOf("2026-11-05", "2026-11-06", "2026-11-07")
    val slotOptions = listOf(
        "10:00 AM - 10:30 AM",
        "11:00 AM - 11:30 AM",
        "03:00 PM - 03:30 PM",
        "04:00 PM - 04:30 PM"
    )
    val methodKeys = listOf("VIDEO_CALL", "AUDIO_CALL", "CHAT", "FARM_VISIT")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "পরামর্শের সময় নির্ধারণ"
                        AppLanguage.HINDI -> "परामर्श समय निर्धारित करें"
                        AppLanguage.ENGLISH -> "Schedule Consultation"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = ExpertTranslations.getDisplayName(expert, currentLanguage),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = cropName,
                    onValueChange = { cropName = it },
                    label = {
                        Text(when (currentLanguage) {
                            AppLanguage.BENGALI -> "ফসলের নাম"
                            AppLanguage.HINDI -> "फसल का नाम"
                            AppLanguage.ENGLISH -> "Crop Name"
                        })
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = problemText,
                    onValueChange = { problemText = it },
                    label = {
                        Text(when (currentLanguage) {
                            AppLanguage.BENGALI -> "সমস্যার বিবরণ (যেমন: পাতায় ধসা দাগ)"
                            AppLanguage.HINDI -> "समस्या का विवरण (उदा. पत्तों पर धब्बे)"
                            AppLanguage.ENGLISH -> "Problem Details (e.g. leaf blight spots)"
                        })
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "তারিখ নির্বাচন করুন:"
                        AppLanguage.HINDI -> "तिथि चुनें:"
                        AppLanguage.ENGLISH -> "Select Date:"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dateOptions.forEach { d ->
                        val isSelected = selectedDate == d
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .clickable { selectedDate = d }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = d,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "সময়সূচী:"
                        AppLanguage.HINDI -> "समय स्लॉट:"
                        AppLanguage.ENGLISH -> "Time Slot:"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    slotOptions.forEach { slot ->
                        val isSelected = selectedTimeSlot == slot
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp)
                                .clickable { selectedTimeSlot = slot }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = slot,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "পরামর্শের মাধ্যম:"
                        AppLanguage.HINDI -> "परामर्श का माध्यम:"
                        AppLanguage.ENGLISH -> "Consultation Mode:"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    methodKeys.forEach { key ->
                        val isSelected = consultationMethod == key
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) com.example.ui.theme.RoyalGreenEmerald else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp)
                                .clickable { consultationMethod = key }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = ExpertTranslations.getConsultationMethodLabel(key, currentLanguage),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val appt = ConsultationAppointmentEntity(
                        id = UUID.randomUUID().toString(),
                        farmerId = farmerId,
                        farmerName = farmerName,
                        expertId = expert.id,
                        expertName = ExpertTranslations.getDisplayName(expert, currentLanguage),
                        expertCategory = expert.category,
                        cropName = cropName.ifBlank {
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "সাধারণ ফসল"
                                AppLanguage.HINDI -> "सामान्य फसल"
                                AppLanguage.ENGLISH -> "General Crop"
                            }
                        },
                        problemDescription = problemText.ifBlank {
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "বিশেষজ্ঞের সার্বিক পরামর্শ"
                                AppLanguage.HINDI -> "विशेषज्ञ का समग्र परामर्श"
                                AppLanguage.ENGLISH -> "General expert consultation"
                            }
                        },
                        consultationMethod = consultationMethod,
                        scheduledDate = selectedDate,
                        scheduledTimeSlot = selectedTimeSlot,
                        status = "CONFIRMED",
                        fee = expert.consultationFee
                    )
                    onConfirmBooking(appt)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreenPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(when (currentLanguage) {
                    AppLanguage.BENGALI -> "নিশ্চিত করুন"
                    AppLanguage.HINDI -> "पुष्टि करें"
                    AppLanguage.ENGLISH -> "Confirm"
                })
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(when (currentLanguage) {
                    AppLanguage.BENGALI -> "বাতিল"
                    AppLanguage.HINDI -> "रद्द करें"
                    AppLanguage.ENGLISH -> "Cancel"
                })
            }
        }
    )
}

@Composable
fun AddNewExpertApplicationDialog(
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onDismiss: () -> Unit,
    onSubmit: (ExpertEntity) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var regId by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var crops by remember(currentLanguage) {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "আলু, ধান, শাকসবজি"
                AppLanguage.HINDI -> "आलू, धान, सब्जियाँ"
                AppLanguage.ENGLISH -> "Potato, Paddy, Vegetables"
            }
        )
    }
    var categoryKey by remember { mutableStateOf(ExpertCategories.PLANT_DISEASE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "বিশেষজ্ঞ হিসেবে আবেদন"
                    AppLanguage.HINDI -> "विशेषज्ञ के रूप में आवेदन"
                    AppLanguage.ENGLISH -> "Apply as Verified Expert"
                },
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(when (currentLanguage) {
                            AppLanguage.BENGALI -> "পুরো নাম (Dr./Prof./Mr.) *"
                            AppLanguage.HINDI -> "पूरा नाम (Dr./Prof./Mr.) *"
                            AppLanguage.ENGLISH -> "Full Name (Dr./Prof.) *"
                        })
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = {
                        Text(when (currentLanguage) {
                            AppLanguage.BENGALI -> "পদবী (যেমন: উদ্ভিদ রোগ বিশেষজ্ঞ)"
                            AppLanguage.HINDI -> "पद (उदा. पादप रोग विशेषज्ञ)"
                            AppLanguage.ENGLISH -> "Designation (e.g. Plant Pathologist)"
                        })
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = qualification,
                    onValueChange = { qualification = it },
                    label = {
                        Text(when (currentLanguage) {
                            AppLanguage.BENGALI -> "শিক্ষাগত যোগ্যতা (M.Sc / Ph.D)"
                            AppLanguage.HINDI -> "शैक्षणिक योग्यता (M.Sc / Ph.D)"
                            AppLanguage.ENGLISH -> "Qualification (M.Sc / Ph.D)"
                        })
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = regId,
                    onValueChange = { regId = it },
                    label = {
                        Text(when (currentLanguage) {
                            AppLanguage.BENGALI -> "রেজিস্ট্রেশন নং (Govt/Council ID) *"
                            AppLanguage.HINDI -> "पंजीकरण सं. (Govt/Council ID) *"
                            AppLanguage.ENGLISH -> "Registration ID (Govt/Council) *"
                        })
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = {
                        Text(when (currentLanguage) {
                            AppLanguage.BENGALI -> "বিশ্ববিদ্যালয় / প্রতিষ্ঠান"
                            AppLanguage.HINDI -> "विश्वविद्यालय / संस्थान"
                            AppLanguage.ENGLISH -> "University / Institution"
                        })
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = crops,
                    onValueChange = { crops = it },
                    label = {
                        Text(when (currentLanguage) {
                            AppLanguage.BENGALI -> "সমর্থিত ফসলসমূহ"
                            AppLanguage.HINDI -> "प्रमुख फसलें"
                            AppLanguage.ENGLISH -> "Supported Crops"
                        })
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || regId.isBlank()) {
                        val msg = when (currentLanguage) {
                            AppLanguage.BENGALI -> "অনুগ্রহ করে পুরো নাম এবং রেজিস্ট্রেশন নং লিখুন"
                            AppLanguage.HINDI -> "कृपया पूरा नाम और पंजीकरण संख्या दर्ज करें"
                            AppLanguage.ENGLISH -> "Please enter Full Name and Registration ID"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val newExp = ExpertEntity(
                        id = UUID.randomUUID().toString(),
                        name = name.trim(),
                        designation = designation.ifBlank {
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "কৃষি বিজ্ঞানী"
                                AppLanguage.HINDI -> "कृषि वैज्ञानिक"
                                AppLanguage.ENGLISH -> "Agricultural Scientist"
                            }
                        },
                        qualification = qualification.ifBlank { "M.Sc. Agriculture" },
                        category = categoryKey,
                        yearsExperience = 5,
                        registrationId = regId.trim(),
                        institution = institution.ifBlank {
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "কৃষি সম্প্রসারণ অধিদপ্তর"
                                AppLanguage.HINDI -> "कृषि विस्तार विभाग"
                                AppLanguage.ENGLISH -> "Department of Agricultural Extension"
                            }
                        },
                        languages = "বাংলা, English, हिन्दी",
                        districtsServed = when (currentLanguage) {
                            AppLanguage.BENGALI -> "পশ্চিমবঙ্গের সকল জেলা"
                            AppLanguage.HINDI -> "पश्चिम बंगाल के सभी जिले"
                            AppLanguage.ENGLISH -> "All Districts of West Bengal"
                        },
                        availabilityStatus = "ONLINE_NOW",
                        consultationFee = "Free (Govt. Supported)",
                        isVerified = false,
                        rating = 5.0,
                        reviewCount = 0,
                        photoAvatar = "👨‍🔬",
                        supportedCrops = crops
                    )
                    onSubmit(newExp)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreenPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(when (currentLanguage) {
                    AppLanguage.BENGALI -> "জমা দিন"
                    AppLanguage.HINDI -> "आवेदन जमा करें"
                    AppLanguage.ENGLISH -> "Submit Application"
                })
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(when (currentLanguage) {
                    AppLanguage.BENGALI -> "বন্ধ"
                    AppLanguage.HINDI -> "बंद करें"
                    AppLanguage.ENGLISH -> "Close"
                })
            }
        }
    )
}
