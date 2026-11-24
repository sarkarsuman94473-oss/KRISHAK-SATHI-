package com.example.ui.farmers

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.datasource.LeadingFarmerTranslations
import com.example.data.local.entity.FarmerCategoryTags
import com.example.data.local.entity.LeadingFarmerEntity
import com.example.data.model.AppLanguage
import com.example.data.repository.LeadingFarmerRepository
import com.example.ui.theme.AgriEarthSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriPriceGold
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.util.L10n
import com.example.util.isCompactNarrowScreen
import com.example.util.toLocalizedIntOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun LeadingFarmersAdminScreen(
    repository: LeadingFarmerRepository,
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    val allFarmers by repository.allFarmers.collectAsState(initial = emptyList())
    val contactRequests by repository.allContactRequests.collectAsState(initial = emptyList())
    val auditLogs by repository.allAuditLogs.collectAsState(initial = emptyList())

    var farmerToEdit by remember { mutableStateOf<LeadingFarmerEntity?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    @OptIn(ExperimentalLayoutApi::class)
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("admin_farmers_back_btn")) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "সফল কৃষক ব্যবস্থাপনা (অ্যাডমিন)"
                                    AppLanguage.HINDI -> "सफल किसान प्रबंधन (एडमिन)"
                                    AppLanguage.ENGLISH -> "Leading Farmers Management (Admin)"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "যাচাই, প্রোফাইল ও অডিট নিয়ন্ত্রণ"
                                    AppLanguage.HINDI -> "सत्यापन, प्रोफ़ाइल और ऑडिट नियंत्रण"
                                    AppLanguage.ENGLISH -> "Verification, Profiles & Audit Log"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = com.example.ui.theme.RoyalGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { isCreatingNew = true },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.RoyalGold, contentColor = com.example.ui.theme.RoyalGreenPrimaryBg),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.heightIn(min = 48.dp).testTag("admin_add_farmer_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নতুন কৃষক"
                                AppLanguage.HINDI -> "नया किसान"
                                AppLanguage.ENGLISH -> "Add Farmer"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "কৃষক তালিকা (${allFarmers.size})"
                                AppLanguage.HINDI -> "किसान सूची (${allFarmers.size})"
                                AppLanguage.ENGLISH -> "Farmers (${allFarmers.size})"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    },
                    icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "যোগাযোগ বার্তা (${contactRequests.size})"
                                AppLanguage.HINDI -> "संपर्क संदेश (${contactRequests.size})"
                                AppLanguage.ENGLISH -> "Requests (${contactRequests.size})"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    },
                    icon = { Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "অডিট লগ (${auditLogs.size})"
                                AppLanguage.HINDI -> "ऑडिट लॉग (${auditLogs.size})"
                                AppLanguage.ENGLISH -> "Audit Log (${auditLogs.size})"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: FARMERS MANAGEMENT
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allFarmers) { farmer ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth().testTag("admin_farmer_card_${farmer.id}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = LeadingFarmerTranslations.getDisplayName(farmer, currentLanguage),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = "${LeadingFarmerTranslations.getDisplayLocation(farmer, currentLanguage)} | ${LeadingFarmerTranslations.getDisplayAward(farmer, currentLanguage)}",
                                                fontSize = 12.sp,
                                                color = AgriEarthSecondary
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { farmerToEdit = farmer }) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = L10n.t("edit", currentLanguage),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        try {
                                                            repository.deleteFarmer(farmer, performedBy = "Admin Console")
                                                            val msg = when (currentLanguage) {
                                                                AppLanguage.BENGALI -> "কৃষক প্রোফাইল মুছে ফেলা হয়েছে"
                                                                AppLanguage.HINDI -> "किसान प्रोफ़ाइल हटा दी गई"
                                                                AppLanguage.ENGLISH -> "Farmer profile deleted"
                                                            }
                                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                        } catch (e: CancellationException) {
                                                            throw e
                                                        } catch (_: Exception) {
                                                            Toast.makeText(context, L10n.t("error", currentLanguage), Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = L10n.t("delete", currentLanguage),
                                                    tint = com.example.ui.theme.RoyalAlertText
                                                )
                                            }
                                        }
                                    }

                                    HorizontalDivider()

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Approved Switch
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (farmer.isApproved) {
                                                    when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "প্রকাশিত (অনুমোদিত)"
                                                        AppLanguage.HINDI -> "प्रकाशित (स्वीकृत)"
                                                        AppLanguage.ENGLISH -> "Published (Approved)"
                                                    }
                                                } else {
                                                    when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "অপ্রকাশিত (অপেক্ষমাণ)"
                                                        AppLanguage.HINDI -> "अप्रकाशित (लंबित)"
                                                        AppLanguage.ENGLISH -> "Unpublished (Pending)"
                                                    }
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (farmer.isApproved) MaterialTheme.colorScheme.primary else AgriTextSecondary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Switch(
                                                checked = farmer.isApproved,
                                                onCheckedChange = { checked ->
                                                    scope.launch {
                                                        try {
                                                            repository.toggleApproval(farmer.id, checked, performedBy = "Admin Console")
                                                        } catch (e: CancellationException) {
                                                            throw e
                                                        } catch (_: Exception) {
                                                            Toast.makeText(context, L10n.t("error", currentLanguage), Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = AgriGreenPrimary)
                                            )
                                        }

                                        // Featured Switch
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (farmer.isFeatured) {
                                                    when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "ফিচার্ড ⭐"
                                                        AppLanguage.HINDI -> "फीचर्ड ⭐"
                                                        AppLanguage.ENGLISH -> "Featured ⭐"
                                                    }
                                                } else {
                                                    when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "সাধারণ"
                                                        AppLanguage.HINDI -> "सामान्य"
                                                        AppLanguage.ENGLISH -> "Regular"
                                                    }
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (farmer.isFeatured) AgriPriceGold else AgriTextSecondary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Switch(
                                                checked = farmer.isFeatured,
                                                onCheckedChange = { checked ->
                                                    scope.launch {
                                                        try {
                                                            repository.toggleFeatured(farmer.id, checked, performedBy = "Admin Console")
                                                        } catch (e: CancellationException) {
                                                            throw e
                                                        } catch (_: Exception) {
                                                            Toast.makeText(context, L10n.t("error", currentLanguage), Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = AgriPriceGold)
                                            )
                                        }
                                    }

                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "তথ্যসূত্র: ${LeadingFarmerTranslations.getDisplaySources(farmer, currentLanguage)}"
                                            AppLanguage.HINDI -> "स्रोत: ${LeadingFarmerTranslations.getDisplaySources(farmer, currentLanguage)}"
                                            AppLanguage.ENGLISH -> "Sources: ${LeadingFarmerTranslations.getDisplaySources(farmer, currentLanguage)}"
                                        },
                                        fontSize = 11.sp,
                                        color = AgriTextSecondary
                                    )
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "যাচাইয়ের তারিখ: ${farmer.lastVerifiedDate}"
                                            AppLanguage.HINDI -> "सत्यापन तिथि: ${farmer.lastVerifiedDate}"
                                            AppLanguage.ENGLISH -> "Last Verified: ${farmer.lastVerifiedDate}"
                                        },
                                        fontSize = 11.sp,
                                        color = AgriTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: CONTACT REQUESTS
                    if (contactRequests.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "এখনও কোনো যোগাযোগের অনুরোধ জমা পড়েনি"
                                    AppLanguage.HINDI -> "अभी तक कोई संपर्क अनुरोध प्राप्त नहीं हुआ है"
                                    AppLanguage.ENGLISH -> "No contact requests submitted yet"
                                },
                                fontSize = 14.sp,
                                color = AgriTextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(contactRequests) { req ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "কৃষক: ${req.farmerName}"
                                                    AppLanguage.HINDI -> "किसान: ${req.farmerName}"
                                                    AppLanguage.ENGLISH -> "Farmer: ${req.farmerName}"
                                                },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = when (req.status) {
                                                    "Submitted" -> com.example.ui.theme.RoyalWarningBg
                                                    "Under Review" -> com.example.ui.theme.RoyalInfoBg
                                                    else -> com.example.ui.theme.RoyalSuccessBg
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = LeadingFarmerTranslations.getDisplayRequestStatus(req.status, currentLanguage),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (req.status) {
                                                        "Submitted" -> com.example.ui.theme.RoyalSoftGold
                                                        "Under Review" -> com.example.ui.theme.RoyalInfoText
                                                        else -> com.example.ui.theme.RoyalSuccessText
                                                    },
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "অনুরোধকারী: ${req.requesterName} (${req.requesterPhone})"
                                                AppLanguage.HINDI -> "अनुरोधकर्ता: ${req.requesterName} (${req.requesterPhone})"
                                                AppLanguage.ENGLISH -> "Requester: ${req.requesterName} (${req.requesterPhone})"
                                            },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "জেলা: ${req.requesterDistrict} | ফসল: ${req.requesterCrop}"
                                                AppLanguage.HINDI -> "जिला: ${req.requesterDistrict} | फसल: ${req.requesterCrop}"
                                                AppLanguage.ENGLISH -> "District: ${req.requesterDistrict} | Crop: ${req.requesterCrop}"
                                            },
                                            fontSize = 12.sp,
                                            color = AgriTextSecondary
                                        )
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "উদ্দেশ্য: ${req.purposeCategory}"
                                                AppLanguage.HINDI -> "उद्देश्य: ${req.purposeCategory}"
                                                AppLanguage.ENGLISH -> "Purpose: ${req.purposeCategory}"
                                            },
                                            fontSize = 12.sp,
                                            color = AgriEarthSecondary
                                        )

                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "\"${req.message}\"",
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }

                                        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(req.timestamp))
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "সময়: $dateStr"
                                                AppLanguage.HINDI -> "समय: $dateStr"
                                                AppLanguage.ENGLISH -> "Time: $dateStr"
                                            },
                                            fontSize = 11.sp,
                                            color = AgriTextSecondary
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    scope.launch {
                                                        try {
                                                            repository.updateRequestStatus(req.id, "Under Review")
                                                        } catch (e: CancellationException) {
                                                            throw e
                                                        } catch (_: Exception) {
                                                            Toast.makeText(context, L10n.t("error", currentLanguage), Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                            ) {
                                                Text(
                                                    text = when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "পর্যালোচনায় রাখুন"
                                                        AppLanguage.HINDI -> "समीक्षा में रखें"
                                                        AppLanguage.ENGLISH -> "Mark Under Review"
                                                    },
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        try {
                                                            repository.updateRequestStatus(req.id, "Transmitted to Organization")
                                                            val msg = when (currentLanguage) {
                                                                AppLanguage.BENGALI -> "বার্তার স্ট্যাটাস আপডেট করা হয়েছে"
                                                                AppLanguage.HINDI -> "संदेश की स्थिति अपडेट की गई"
                                                                AppLanguage.ENGLISH -> "Request status updated"
                                                            }
                                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                        } catch (e: CancellationException) {
                                                            throw e
                                                        } catch (_: Exception) {
                                                            Toast.makeText(context, L10n.t("error", currentLanguage), Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = AgriGreenPrimary,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                            ) {
                                                Text(
                                                    text = when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "প্রেরিত মার্ক করুন"
                                                        AppLanguage.HINDI -> "प्रेषित चिह्नित करें"
                                                        AppLanguage.ENGLISH -> "Mark Transmitted"
                                                    },
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: AUDIT LOGS
                    if (auditLogs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "কোনো অডিট লগ পাওয়া যায়নি"
                                    AppLanguage.HINDI -> "कोई ऑडिट लॉग नहीं मिला"
                                    AppLanguage.ENGLISH -> "No audit logs found"
                                },
                                fontSize = 14.sp,
                                color = AgriTextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(auditLogs) { log ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = LeadingFarmerTranslations.getAuditActionLabel(log.action, currentLanguage),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                                            Text(text = dateStr, fontSize = 10.sp, color = AgriTextSecondary)
                                        }
                                        Text(text = log.details, fontSize = 12.sp, color = AgriTextPrimary)
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "সম্পাদনকারী: ${log.performedBy}"
                                                AppLanguage.HINDI -> "निष्पादित द्वारा: ${log.performedBy}"
                                                AppLanguage.ENGLISH -> "Performed by: ${log.performedBy}"
                                            },
                                            fontSize = 10.sp,
                                            color = AgriEarthSecondary
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

    // Edit/Create Farmer Dialog
    if (farmerToEdit != null || isCreatingNew) {
        val editing = farmerToEdit
        var nameEn by remember { mutableStateOf(editing?.fullNameEn ?: "") }
        var nameBn by remember { mutableStateOf(editing?.fullNameBn ?: "") }
        var nameHi by remember { mutableStateOf(editing?.fullNameHi ?: "") }
        var state by remember { mutableStateOf(editing?.state ?: "") }
        var district by remember { mutableStateOf(editing?.district ?: "") }
        var specEn by remember { mutableStateOf(editing?.farmingSpecializationEn ?: "") }
        var specBn by remember { mutableStateOf(editing?.farmingSpecializationBn ?: "") }
        var specHi by remember { mutableStateOf(editing?.farmingSpecializationHi ?: "") }
        var categoryTag by remember { mutableStateOf(editing?.categoryTag ?: FarmerCategoryTags.NATURAL_FARMING) }
        var mainCrops by remember { mutableStateOf(editing?.mainCrops ?: "") }
        var techniques by remember { mutableStateOf(editing?.farmingTechniques ?: "") }
        var experience by remember { mutableStateOf((editing?.yearsOfExperience ?: 20).toString()) }
        var awardEn by remember { mutableStateOf(editing?.keyAwardEn ?: "Padma Shri") }
        var awardBn by remember { mutableStateOf(editing?.keyAwardBn ?: "পদ্মশ্রী সম্মাননা") }
        var allAwards by remember { mutableStateOf(editing?.allAwards ?: "") }
        var innovationEn by remember { mutableStateOf(editing?.innovationContributionEn ?: "") }
        var innovationBn by remember { mutableStateOf(editing?.innovationContributionBn ?: "") }
        var storyHowStarted by remember { mutableStateOf(editing?.storyHowStarted ?: "") }
        var storyChallenges by remember { mutableStateOf(editing?.storyChallenges ?: "") }
        var storyMethods by remember { mutableStateOf(editing?.storyMethods ?: "") }
        var storyAchievements by remember { mutableStateOf(editing?.storyAchievements ?: "") }
        var storyLessons by remember { mutableStateOf(editing?.storyLessonsForFarmers ?: "") }
        var contactPhone by remember { mutableStateOf(editing?.officialPublicContact ?: "") }
        var contactEmail by remember { mutableStateOf(editing?.officialEmail ?: "") }
        var websiteUrl by remember { mutableStateOf(editing?.officialWebsite ?: "") }
        var fbUrl by remember { mutableStateOf(editing?.officialFacebook ?: "") }
        var igUrl by remember { mutableStateOf(editing?.officialInstagram ?: "") }
        var ytUrl by remember { mutableStateOf(editing?.officialYoutube ?: "") }
        var sources by remember { mutableStateOf(editing?.sourceReferences ?: "") }
        var verifyDate by remember { mutableStateOf(editing?.lastVerifiedDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
        var isApproved by remember { mutableStateOf(editing?.isApproved ?: true) }
        var isFeatured by remember { mutableStateOf(editing?.isFeatured ?: false) }

        Dialog(
            onDismissRequest = {
                farmerToEdit = null
                isCreatingNew = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 640.dp)
                        .imePadding(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isCreatingNew) {
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "নতুন কৃষক প্রোফাইল তৈরি"
                                        AppLanguage.HINDI -> "नया किसान प्रोफ़ाइल बनाएं"
                                        AppLanguage.ENGLISH -> "Create New Farmer Profile"
                                    }
                                } else {
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "কৃষক প্রোফাইল সম্পাদনা"
                                        AppLanguage.HINDI -> "किसान प्रोफ़ाइल संपादित करें"
                                        AppLanguage.ENGLISH -> "Edit Farmer Profile"
                                    }
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { farmerToEdit = null; isCreatingNew = false },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = nameEn,
                            onValueChange = { nameEn = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "ইংরেজি পূর্ণ নাম *"
                                        AppLanguage.HINDI -> "अंग्रेजी पूरा नाम *"
                                        AppLanguage.ENGLISH -> "English Full Name *"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nameBn,
                            onValueChange = { nameBn = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "বাংলা পূর্ণ নাম *"
                                        AppLanguage.HINDI -> "बंगाली पूरा नाम *"
                                        AppLanguage.ENGLISH -> "Bengali Full Name *"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nameHi,
                            onValueChange = { nameHi = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "হিন্দি নাম"
                                        AppLanguage.HINDI -> "हिन्दी नाम"
                                        AppLanguage.ENGLISH -> "Hindi Name"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (isCompactNarrowScreen()) {
                            OutlinedTextField(
                                value = state,
                                onValueChange = { state = it },
                                label = {
                                    Text(
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "রাজ্য *"
                                            AppLanguage.HINDI -> "राज्य *"
                                            AppLanguage.ENGLISH -> "State *"
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = district,
                                onValueChange = { district = it },
                                label = {
                                    Text(
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "জেলা *"
                                            AppLanguage.HINDI -> "जिला *"
                                            AppLanguage.ENGLISH -> "District *"
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = state,
                                    onValueChange = { state = it },
                                    label = {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "রাজ্য *"
                                                AppLanguage.HINDI -> "राज्य *"
                                                AppLanguage.ENGLISH -> "State *"
                                            }
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    label = {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "জেলা *"
                                                AppLanguage.HINDI -> "जिला *"
                                                AppLanguage.ENGLISH -> "District *"
                                            }
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = awardEn,
                            onValueChange = { awardEn = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "প্রধান সম্মাননা (যেমন: Padma Shri 2019) *"
                                        AppLanguage.HINDI -> "मुख्य सम्मान (जैसे: Padma Shri 2019) *"
                                        AppLanguage.ENGLISH -> "Key Award (e.g. Padma Shri 2019) *"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = allAwards,
                            onValueChange = { allAwards = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "সকল পদক ও পুরস্কারের তালিকা"
                                        AppLanguage.HINDI -> "सभी पुरस्कारों की सूची"
                                        AppLanguage.ENGLISH -> "All Awards & Honors"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = specBn,
                            onValueChange = { specBn = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "বিশেষ পারদর্শিতা *"
                                        AppLanguage.HINDI -> "विशेष विशेषज्ञता *"
                                        AppLanguage.ENGLISH -> "Farming Specialization *"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = mainCrops,
                            onValueChange = { mainCrops = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "প্রধান ফসলসমূহ *"
                                        AppLanguage.HINDI -> "प्रमुख फसलें *"
                                        AppLanguage.ENGLISH -> "Main Crops *"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = techniques,
                            onValueChange = { techniques = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "ব্যবহৃত কৃষি প্রযুক্তি ও পদ্ধতি *"
                                        AppLanguage.HINDI -> "उपयोग की गई कृषि तकनीक व विधि *"
                                        AppLanguage.ENGLISH -> "Farming Techniques & Methods *"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = innovationBn,
                            onValueChange = { innovationBn = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "বিশেষ অবদান ও উদ্ভাবন *"
                                        AppLanguage.HINDI -> "प्रमुख नवाचार व योगदान *"
                                        AppLanguage.ENGLISH -> "Key Innovation & Contribution *"
                                    }
                                )
                            },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = storyHowStarted,
                            onValueChange = { storyHowStarted = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "সাফল্যের শুরু কীভাবে হলো (গল্প) *"
                                        AppLanguage.HINDI -> "सफलता की शुरुआत (कहानी) *"
                                        AppLanguage.ENGLISH -> "Journey & How It Started *"
                                    }
                                )
                            },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = storyLessons,
                            onValueChange = { storyLessons = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অন্যান্য কৃষকদের জন্য শিক্ষণীয় বিষয় *"
                                        AppLanguage.HINDI -> "अन्य किसानों के लिए सीख *"
                                        AppLanguage.ENGLISH -> "Key Lessons for Farmers *"
                                    }
                                )
                            },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "প্রকাশিত অফিসিয়াল ফোন / যোগাযোগ (ঐচ্ছিক)"
                                        AppLanguage.HINDI -> "सार्वजनिक आधिकारिक फोन / संपर्क (वैकल्पिक)"
                                        AppLanguage.ENGLISH -> "Official Public Phone / Contact (Optional)"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অফিসিয়াল ইমেইল (ঐচ্ছিক)"
                                        AppLanguage.HINDI -> "आधिकारिक ईमेल (वैकल्पिक)"
                                        AppLanguage.ENGLISH -> "Official Email (Optional)"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = websiteUrl,
                            onValueChange = { websiteUrl = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অফিসিয়াল ওয়েবসাইট / প্রোফাইল লিংক"
                                        AppLanguage.HINDI -> "आधिकारिक वेबसाइट / प्रोफ़ाइल लिंक"
                                        AppLanguage.ENGLISH -> "Official Website / Profile Link"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = fbUrl,
                            onValueChange = { fbUrl = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অফিসিয়াল ফেসবুক পেজ (URL)"
                                        AppLanguage.HINDI -> "आधिकारिक फेसबुक पेज (URL)"
                                        AppLanguage.ENGLISH -> "Official Facebook URL"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = igUrl,
                            onValueChange = { igUrl = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অফিসিয়াল ইনস্টাগ্রাম (URL)"
                                        AppLanguage.HINDI -> "आधिकारिक इंस्टाग्राम (URL)"
                                        AppLanguage.ENGLISH -> "Official Instagram URL"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = ytUrl,
                            onValueChange = { ytUrl = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অফিসিয়াল ইউটিউব চ্যানেল (URL)"
                                        AppLanguage.HINDI -> "आधिकारिक यूट्यूब चैनल (URL)"
                                        AppLanguage.ENGLISH -> "Official YouTube URL"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = sources,
                            onValueChange = { sources = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "সরকারি / নির্ভরযোগ্য তথ্যসূত্র *"
                                        AppLanguage.HINDI -> "सरकारी / विश्वसनीय स्रोत *"
                                        AppLanguage.ENGLISH -> "Verified Official Sources *"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = verifyDate,
                            onValueChange = { verifyDate = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "যাচাইয়ের তারিখ (YYYY-MM-DD)"
                                        AppLanguage.HINDI -> "सत्यापन तिथि (YYYY-MM-DD)"
                                        AppLanguage.ENGLISH -> "Verification Date (YYYY-MM-DD)"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "প্রকাশিত অনুমোদন"
                                    AppLanguage.HINDI -> "प्रकाशन स्वीकृत"
                                    AppLanguage.ENGLISH -> "Published Approval"
                                },
                                fontWeight = FontWeight.Bold
                            )
                            Switch(checked = isApproved, onCheckedChange = { isApproved = it })
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "হোমপেজে ফিচার্ড প্রদর্শন"
                                    AppLanguage.HINDI -> "होमपेज पर फीचर्ड दिखाएं"
                                    AppLanguage.ENGLISH -> "Show Featured on Home"
                                },
                                fontWeight = FontWeight.Bold
                            )
                            Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (nameEn.isBlank() || nameBn.isBlank() || state.isBlank() || sources.isBlank()) {
                                val errMsg = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অনুগ্রহ করে নাম, রাজ্য ও তথ্যসূত্র পূরণ করুন"
                                    AppLanguage.HINDI -> "कृपया नाम, राज्य और स्रोत भरें"
                                    AppLanguage.ENGLISH -> "Please fill in Name, State, and Sources"
                                }
                                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val newFarmer = LeadingFarmerEntity(
                                id = editing?.id ?: "farmer_${nameEn.trim().lowercase().replace(" ", "_")}_${UUID.randomUUID().toString().take(4)}",
                                fullNameEn = nameEn.trim(),
                                fullNameBn = nameBn.trim(),
                                fullNameHi = nameHi.ifBlank { nameEn.trim() },
                                photoResName = "ic_krishak_sathi_1793336218748",
                                state = state.trim(),
                                district = district.trim(),
                                farmingSpecializationEn = specEn.ifBlank { specBn.trim() },
                                farmingSpecializationBn = specBn.trim(),
                                farmingSpecializationHi = specHi.ifBlank { specEn.ifBlank { specBn.trim() } },
                                categoryTag = categoryTag,
                                mainCrops = mainCrops.trim(),
                                farmingTechniques = techniques.trim(),
                                yearsOfExperience = experience.toLocalizedIntOrNull() ?: 25,
                                keyAwardEn = awardEn.trim(),
                                keyAwardBn = awardBn.trim(),
                                allAwards = allAwards.ifBlank { awardEn.trim() },
                                innovationContributionEn = innovationEn.ifBlank { innovationBn.trim() },
                                innovationContributionBn = innovationBn.trim(),
                                innovationContributionHi = innovationEn.ifBlank { innovationBn.trim() },
                                storyHowStarted = storyHowStarted.trim(),
                                storyChallenges = storyChallenges.ifBlank { "Overcame initial market resistance and technical challenges." },
                                storyMethods = storyMethods.ifBlank { techniques.trim() },
                                storyAchievements = storyAchievements.ifBlank { allAwards.trim() },
                                storyLessonsForFarmers = storyLessons.trim(),
                                officialBusinessPhone = null,
                                officialPublicContact = contactPhone.ifBlank { null },
                                officialEmail = contactEmail.ifBlank { null },
                                officialWebsite = websiteUrl.ifBlank { null },
                                officialFacebook = fbUrl.ifBlank { null },
                                officialInstagram = igUrl.ifBlank { null },
                                officialYoutube = ytUrl.ifBlank { null },
                                organizationFpoDetails = null,
                                hasPublicContact = contactPhone.isNotBlank() || contactEmail.isNotBlank() || websiteUrl.isNotBlank() || fbUrl.isNotBlank() || igUrl.isNotBlank() || ytUrl.isNotBlank(),
                                directContactNotice = if (contactPhone.isNotBlank() || contactEmail.isNotBlank()) "Public contact details verified." else "Direct contact information is not publicly available.",
                                sourceReferences = sources.trim(),
                                lastVerifiedDate = verifyDate.trim(),
                                isApproved = isApproved,
                                isFeatured = isFeatured,
                                allowsContactRequests = true
                            )

                            scope.launch {
                                try {
                                    repository.saveFarmer(newFarmer, performedBy = "Admin Console")
                                    val savedMsg = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে!"
                                        AppLanguage.HINDI -> "प्रोफ़ाइल सफलतापूर्वक सहेजा गया!"
                                        AppLanguage.ENGLISH -> "Profile saved successfully!"
                                    }
                                    Toast.makeText(context, savedMsg, Toast.LENGTH_SHORT).show()
                                    farmerToEdit = null
                                    isCreatingNew = false
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (_: Exception) {
                                    Toast.makeText(context, L10n.t("error", currentLanguage), Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgriGreenPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("admin_save_farmer_submit_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "প্রোফাইল সংরক্ষণ করুন"
                                AppLanguage.HINDI -> "प्रोफ़ाइल सहेजें"
                                AppLanguage.ENGLISH -> "Save Profile"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            }
        }
    }
}
