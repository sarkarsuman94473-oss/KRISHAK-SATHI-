package com.example.ui.info

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.local.config.AppConfig
import com.example.data.local.config.AppConfigManager
import com.example.data.model.AppLanguage
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.util.L10n

@Composable
fun AdminConfigScreen(
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onBack: () -> Unit,
    onNavigateLeadingFarmersAdmin: () -> Unit = {},
    onNavigateAdminAddCrop: () -> Unit = {},
    onNavigateBannerAdmin: () -> Unit = {},
    onNavigateMachineryAdmin: () -> Unit = {},
    onNavigateTrustModeration: () -> Unit = {},
    onNavigateVideoAdmin: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentConfig = remember { AppConfigManager.getConfig(context) }

    var appName by remember { mutableStateOf(currentConfig.appName) }
    var description by remember { mutableStateOf(currentConfig.appDescription) }
    var releaseDate by remember { mutableStateOf(currentConfig.releaseDate) }
    var version by remember { mutableStateOf(currentConfig.currentVersion) }
    var lastUpdated by remember { mutableStateOf(currentConfig.lastUpdatedDate) }
    var copyright by remember { mutableStateOf(currentConfig.copyrightText) }

    var isGovtApproved by remember { mutableStateOf(currentConfig.isGovtApproved) }
    var govtStatusText by remember { mutableStateOf(currentConfig.govtRegistrationStatusText) }
    var govtCertNo by remember { mutableStateOf(currentConfig.govtCertificateNo) }
    var govtAuthority by remember { mutableStateOf(currentConfig.govtIssuingAuthority) }
    var govtApprovalDate by remember { mutableStateOf(currentConfig.govtApprovalDate) }
    var govtRefNo by remember { mutableStateOf(currentConfig.govtReferenceNo) }
    var govtDeptInfo by remember { mutableStateOf(currentConfig.govtDepartmentInfo) }

    var fbUrl by remember { mutableStateOf(currentConfig.facebookUrl) }
    var igUrl by remember { mutableStateOf(currentConfig.instagramUrl) }
    var ytUrl by remember { mutableStateOf(currentConfig.youtubeUrl) }
    var waUrl by remember { mutableStateOf(currentConfig.whatsappUrl) }
    var xUrl by remember { mutableStateOf(currentConfig.twitterUrl) }

    var email by remember { mutableStateOf(currentConfig.supportEmail) }
    var phone by remember { mutableStateOf(currentConfig.supportPhone) }
    var address by remember { mutableStateOf(currentConfig.businessAddress) }
    var hours by remember { mutableStateOf(currentConfig.supportHours) }
    var website by remember { mutableStateOf(currentConfig.officialWebsite) }

    var grievanceName by remember { mutableStateOf(currentConfig.grievanceOfficerName) }
    var grievanceEmail by remember { mutableStateOf(currentConfig.grievanceOfficerEmail) }
    var grievancePhone by remember { mutableStateOf(currentConfig.grievanceOfficerPhone) }
    var grievanceAddr by remember { mutableStateOf(currentConfig.grievanceOfficerAddress) }
    var nodalName by remember { mutableStateOf(currentConfig.nodalOfficerName) }
    var nodalContact by remember { mutableStateOf(currentConfig.nodalOfficerContact) }
    var legalEntity by remember { mutableStateOf(currentConfig.legalEntityName) }
    var gstinNumber by remember { mutableStateOf(currentConfig.gstinNumber) }

    @OptIn(ExperimentalLayoutApi::class)
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("admin_config_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "ফুটার ও অ্যাপ তথ্য ব্যবস্থাপনা (অ্যাডমিন)"
                            AppLanguage.HINDI -> "फ़ुटर व ऐप जानकारी प्रबंधन (एडमिन)"
                            AppLanguage.ENGLISH -> "Footer & App Config (Admin Panel)"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Leading Farmers Management Shortcut Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.widthIn(min = 180.dp, max = 480.dp)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "🏆 সফল কৃষক ডিরেক্টরি ব্যবস্থাপনা"
                                    AppLanguage.HINDI -> "🏆 सफल किसान निर्देशिका प्रबंधन"
                                    AppLanguage.ENGLISH -> "🏆 Leading Farmers Directory Admin"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "স্বীকৃত কৃষকদের প্রোফাইল, তথ্যসূত্র ও যোগাযোগের বার্তা পরিচালনা করুন"
                                    AppLanguage.HINDI -> "प्रमाणित किसानों की प्रोफ़ाइल, संदर्भ और संपर्क अनुरोध प्रबंधित करें"
                                    AppLanguage.ENGLISH -> "Manage verified farmer profiles, references, and contact requests"
                                },
                                fontSize = 12.sp,
                                color = AgriTextSecondary
                            )
                        }
                        Button(
                            onClick = onNavigateLeadingFarmersAdmin,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AgriGreenPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("admin_goto_farmers_management_btn")
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "প্রবেশ"
                                    AppLanguage.HINDI -> "खोलें"
                                    AppLanguage.ENGLISH -> "Open"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Expandable Master Crop Library Management Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.widthIn(min = 180.dp, max = 480.dp)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "📚 ফসল মাস্টার লাইব্রেরি (নতুন ফসল যুক্ত করুন)"
                                    AppLanguage.HINDI -> "📚 फसल मास्टर लाइब्रेरी (नई फसल जोड़ें)"
                                    AppLanguage.ENGLISH -> "📚 Crop Master Library (Add New Crops / Plants)"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অ্যাপ আপডেট ছাড়াই নতুন ফসল, ফুল, ফল, ঔষধি ও নার্সারি গাছ যুক্ত করুন"
                                    AppLanguage.HINDI -> "ऐप अपडेट के बिना नई फसलें, फूल, फल व नर्सरी पौधे जोड़ें"
                                    AppLanguage.ENGLISH -> "Expand master library with custom crops, varieties, and photos at runtime"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        Button(
                            onClick = onNavigateAdminAddCrop,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AgriGreenPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("admin_goto_add_master_crop_btn")
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "+ যুক্ত করুন"
                                    AppLanguage.HINDI -> "+ जोड़ें"
                                    AppLanguage.ENGLISH -> "+ Add Crop"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Language-Isolated Home Banner Manager Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.widthIn(min = 180.dp, max = 480.dp)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "🖼️ ভাষা-ভিত্তিক হোম ব্যানার ম্যানেজার (BN/HI/EN)"
                                    AppLanguage.HINDI -> "🖼️ भाषा-आधारित होम बैनर प्रबंधक (BN/HI/EN)"
                                    AppLanguage.ENGLISH -> "🖼️ Language-Based Home Banner Manager"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "বাংলা, হিন্দি ও ইংরেজি ব্যানার আলাদাভাবে আপলোড, ক্রমবিন্যাস ও সক্রিয়/নিষ্ক্রিয় করুন"
                                    AppLanguage.HINDI -> "बंगाली, हिंदी व अंग्रेज़ी बैनर अलग-अलग अपलोड, क्रमबद्ध व सक्रिय करें"
                                    AppLanguage.ENGLISH -> "Upload, replace, reorder & activate BN/HI/EN home carousel banners separately"
                                },
                                fontSize = 12.sp,
                                color = AgriTextSecondary
                            )
                        }
                        Button(
                            onClick = onNavigateBannerAdmin,
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("admin_goto_banners_btn")
                        ) {
                            Text("Banners", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Machinery & Siliguri/NB Dealer Verification Admin Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.widthIn(min = 180.dp, max = 480.dp)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "🚜 কৃষি যন্ত্রপাতি ও শিলিগুড়ি ডিলার যাচাই প্যানেল"
                                    AppLanguage.HINDI -> "🚜 कृषि मशीनरी व सिलीगुड़ी डीलर सत्यापन पैनल"
                                    AppLanguage.ENGLISH -> "🚜 Machinery Listings & Dealer Verification Admin"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "বিক্রেতার যন্ত্র অনুমোদন, ১৬+ শিলিগুড়ি ডিলার সম্মতি যাচাই এবং অডিট লগ"
                                    AppLanguage.HINDI -> "विक्रेता मशीन स्वीकृति, 16+ सिलीगुड़ी डीलर सहमति सत्यापन व ऑडिट लॉग"
                                    AppLanguage.ENGLISH -> "Approve seller machinery listings, verify dealer consent & inspect audit stats"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        Button(
                            onClick = onNavigateMachineryAdmin,
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("admin_goto_machinery_btn")
                        ) {
                            Text("Machinery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Trust, Fraud, Seller Suspension & Grievance Moderation Admin Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.widthIn(min = 180.dp, max = 480.dp)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "🛡️ ট্রাস্ট, প্রতারণা ও অভিযোগ মডারেটর সেন্টার"
                                    AppLanguage.HINDI -> "🛡️ ट्रस्ट, धोखाधड़ी व शिकायत मॉडरेशन सेंटर"
                                    AppLanguage.ENGLISH -> "🛡️ Trust, Fraud & Grievance Moderation Center"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "প্রতারণা রিপোর্ট পর্যালোচনা, বিক্রেতা স্থগিতকরণ, অভিযোগ নিষ্পত্তি ও অডিট লগ"
                                    AppLanguage.HINDI -> "धोखाधड़ी रिपोर्ट, विक्रेता निलंबन, शिकायत समाधान व ऑडिट लॉग प्रबंधन"
                                    AppLanguage.ENGLISH -> "Moderate fraud reports, suspend bad actors, resolve grievances & view audit ledger"
                                },
                                fontSize = 12.sp,
                                color = AgriTextSecondary
                            )
                        }
                        Button(
                            onClick = onNavigateTrustModeration,
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("admin_goto_trust_moderation_btn")
                        ) {
                            Text("Moderation", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Krishi Shiksha Video Learning Studio Admin Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.widthIn(min = 180.dp, max = 480.dp)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "🎬 কৃষি শিক্ষা — ভিডিও লার্নিং স্টুডিও (অ্যাডমিন)"
                                    AppLanguage.HINDI -> "🎬 कृषि शिक्षा — वीडियो लर्निंग स्टूडियो (एडमिन)"
                                    AppLanguage.ENGLISH -> "🎬 Krishi Shiksha — Video Learning Admin Studio"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ভিডিও ফাইল, থাম্বনেইল, বাংলা-হিন্দি-ইংরেজি অডিও/সাবটাইটেল আপলোড ও প্রকাশনা"
                                    AppLanguage.HINDI -> "वीडियो फ़ाइल, थंबनेल, बहुभाषी ऑडियो/सबटाइटल अपलोड और प्रकाशन प्रबंधन"
                                    AppLanguage.ENGLISH -> "Upload farming videos, thumbnails, BN/HI/EN narration & publish lessons"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        Button(
                            onClick = onNavigateVideoAdmin,
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("admin_goto_video_learning_btn")
                        ) {
                            Text("Videos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Embedded Trust & Legal Compliance Moderator Summary
                AdminTrustCompliancePanel(currentLanguage = currentLanguage)

                // AI Intelligence, Dual-Provider Failover & RAG Control Center
                AdminAiControlSection(currentLanguage = currentLanguage)

                // Public Agriculture Knowledge Hub, Crop SEO & Discovery Control
                AdminSeoManagementSection(currentLanguage = currentLanguage)

                // App Information & Release
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "📱 রিলিজ ও বিবরণ সেটিংস:"
                            AppLanguage.HINDI -> "📱 रिलीज़ और विवरण सेटिंग्स:"
                            AppLanguage.ENGLISH -> "📱 Release & App Description Settings:"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "অ্যাপের নাম"
                                AppLanguage.HINDI -> "ऐप का नाम"
                                AppLanguage.ENGLISH -> "App Name"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = releaseDate,
                        onValueChange = { releaseDate = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "রিলিজ তারিখ"
                                AppLanguage.HINDI -> "रिलीज़ तिथि"
                                AppLanguage.ENGLISH -> "Release Date"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = version,
                        onValueChange = { version = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "বর্তমান সংস্করণ"
                                AppLanguage.HINDI -> "वर्तमान संस्करण"
                                AppLanguage.ENGLISH -> "Current Version"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lastUpdated,
                        onValueChange = { lastUpdated = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "সর্বশেষ আপডেট তারিখ"
                                AppLanguage.HINDI -> "अंतिम अपडेट तिथि"
                                AppLanguage.ENGLISH -> "Last Updated Date"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = copyright,
                        onValueChange = { copyright = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "কপিরাইট টেক্সট"
                                AppLanguage.HINDI -> "कॉपीराइट टेक्स्ट"
                                AppLanguage.ENGLISH -> "Copyright Text"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "অ্যাপের বিবরণ"
                                AppLanguage.HINDI -> "ऐप विवरण"
                                AppLanguage.ENGLISH -> "App Description"
                            })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            }

            // Government Registration & Approval
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "🏛️ সরকারি নিবন্ধন ও অনুমোদন তথ্য:"
                            AppLanguage.HINDI -> "🏛️ सरकारी पंजीकरण व अनुमोदन विवरण:"
                            AppLanguage.ENGLISH -> "🏛️ Government Registration & Approval:"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "সরকারি অনুমোদন যাচাইকৃত হয়েছে?"
                                AppLanguage.HINDI -> "क्या सरकारी अनुमोदन सत्यापित है?"
                                AppLanguage.ENGLISH -> "Government approval verified?"
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Switch(
                            checked = isGovtApproved,
                            onCheckedChange = { isGovtApproved = it }
                        )
                    }

                    if (!isGovtApproved) {
                        OutlinedTextField(
                            value = govtStatusText,
                            onValueChange = { govtStatusText = it },
                            label = {
                                Text(when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অননুমোদিত/যাচাইাধীন স্থিতি টেক্সট"
                                    AppLanguage.HINDI -> "लंबित / सत्यापन स्थिति पाठ"
                                    AppLanguage.ENGLISH -> "Pending Verification Status Text"
                                })
                            },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    } else {
                        OutlinedTextField(
                            value = govtCertNo,
                            onValueChange = { govtCertNo = it },
                            label = {
                                Text(when (currentLanguage) {
                                    AppLanguage.BENGALI -> "নিবন্ধন সার্টিফিকেট নং"
                                    AppLanguage.HINDI -> "पंजीकरण प्रमाणपत्र संख्या"
                                    AppLanguage.ENGLISH -> "Registration Certificate No."
                                })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = govtAuthority,
                            onValueChange = { govtAuthority = it },
                            label = {
                                Text(when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ইস্যুকারী সরকারি কর্তৃপক্ষ"
                                    AppLanguage.HINDI -> "जारीकर्ता सरकारी प्राधिकरण"
                                    AppLanguage.ENGLISH -> "Issuing Government Authority"
                                })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = govtApprovalDate,
                            onValueChange = { govtApprovalDate = it },
                            label = {
                                Text(when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অনুমোদনের তারিখ"
                                    AppLanguage.HINDI -> "अनुमोदन तिथि"
                                    AppLanguage.ENGLISH -> "Approval Date"
                                })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = govtRefNo,
                            onValueChange = { govtRefNo = it },
                            label = {
                                Text(when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অফিসিয়াল রেফারেন্স নং"
                                    AppLanguage.HINDI -> "आधिकारिक संदर्भ संख्या"
                                    AppLanguage.ENGLISH -> "Official Reference No."
                                })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = govtDeptInfo,
                        onValueChange = { govtDeptInfo = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "প্রাসঙ্গিক কৃষি বিভাগীয় তথ্য"
                                AppLanguage.HINDI -> "संबंधित कृषि विभाग जानकारी"
                                AppLanguage.ENGLISH -> "Relevant Agriculture Department Info"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Social Media Links (if left blank, icon is safely hidden)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "🌐 সোশ্যাল মিডিয়া লিংক (ফাঁকা রাখলে আইকন লুকানো থাকবে):"
                            AppLanguage.HINDI -> "🌐 सोशल मीडिया लिंक (खाली रखने पर आइकन छिपा रहेगा):"
                            AppLanguage.ENGLISH -> "🌐 Social Media Links (leave blank to hide icon):"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(value = fbUrl, onValueChange = { fbUrl = it }, label = { Text("Facebook Page URL") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = igUrl, onValueChange = { igUrl = it }, label = { Text("Instagram Profile URL") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ytUrl, onValueChange = { ytUrl = it }, label = { Text("YouTube Channel URL") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = waUrl, onValueChange = { waUrl = it }, label = { Text("WhatsApp Contact Link") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = xUrl,
                        onValueChange = { xUrl = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "X (Twitter) প্রোফাইল লিংক (ঐচ্ছিক)"
                                AppLanguage.HINDI -> "X (Twitter) प्रोफ़ाइल लिंक (वैकल्पिक)"
                                AppLanguage.ENGLISH -> "X (Twitter) Profile URL (Optional)"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Support & Contact Information
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "📞 সাপোর্ট ও যোগাযোগের তথ্য:"
                            AppLanguage.HINDI -> "📞 सहायता एवं संपर्क जानकारी:"
                            AppLanguage.ENGLISH -> "📞 Support & Contact Information:"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "অফিসিয়াল সাপোর্ট ইমেইল"
                                AppLanguage.HINDI -> "आधिकारिक सहायता ईमेल"
                                AppLanguage.ENGLISH -> "Official Support Email"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "অফিসিয়াল ফোন নম্বর"
                                AppLanguage.HINDI -> "आधिकारिक फ़ोन नंबर"
                                AppLanguage.ENGLISH -> "Official Phone Number"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { hours = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "সাপোর্ট সময়সীমা"
                                AppLanguage.HINDI -> "सहायता समय"
                                AppLanguage.ENGLISH -> "Support Hours"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "ব্যবসায়িক ঠিকানা"
                                AppLanguage.HINDI -> "व्यावसायिक पता"
                                AppLanguage.ENGLISH -> "Business Address"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = website,
                        onValueChange = { website = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "অফিসিয়াল ওয়েবসাইট"
                                AppLanguage.HINDI -> "आधिकारिक वेबसाइट"
                                AppLanguage.ENGLISH -> "Official Website"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Statutory Grievance & Nodal Officers (India E-Commerce & IT Rules)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("admin_statutory_officers_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "⚖️ ${L10n.t("admin_statutory_title", currentLanguage)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = grievanceName,
                        onValueChange = { grievanceName = it },
                        label = { Text(L10n.t("admin_grievance_officer_name", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth().testTag("admin_grievance_name_input")
                    )
                    OutlinedTextField(
                        value = grievanceEmail,
                        onValueChange = { grievanceEmail = it },
                        label = { Text(L10n.t("admin_grievance_officer_email", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth().testTag("admin_grievance_email_input")
                    )
                    OutlinedTextField(
                        value = grievancePhone,
                        onValueChange = { grievancePhone = it },
                        label = { Text(L10n.t("admin_grievance_officer_phone", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth().testTag("admin_grievance_phone_input")
                    )
                    OutlinedTextField(
                        value = grievanceAddr,
                        onValueChange = { grievanceAddr = it },
                        label = { Text(L10n.t("admin_grievance_officer_addr", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth().testTag("admin_grievance_addr_input")
                    )
                    OutlinedTextField(
                        value = nodalName,
                        onValueChange = { nodalName = it },
                        label = { Text(L10n.t("admin_nodal_officer_name", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nodalContact,
                        onValueChange = { nodalContact = it },
                        label = { Text(L10n.t("admin_nodal_officer_contact", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = legalEntity,
                        onValueChange = { legalEntity = it },
                        label = { Text(L10n.t("admin_legal_entity_name", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth().testTag("admin_legal_entity_input")
                    )
                    OutlinedTextField(
                        value = gstinNumber,
                        onValueChange = { gstinNumber = it },
                        label = { Text(L10n.t("admin_gstin_number", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    val updated = AppConfig(
                        appName = appName.ifBlank { "KRISHAK SATHI" },
                        appDescription = description,
                        releaseDate = releaseDate.ifBlank { "January 15, 2026" },
                        currentVersion = version.ifBlank { "1.0.0" },
                        lastUpdatedDate = lastUpdated.ifBlank { "November 3, 2026" },
                        copyrightText = copyright.ifBlank { "© 2026 KRISHAK SATHI. All Rights Reserved." },
                        isGovtApproved = isGovtApproved,
                        govtRegistrationStatusText = govtStatusText,
                        govtCertificateNo = govtCertNo,
                        govtIssuingAuthority = govtAuthority,
                        govtApprovalDate = govtApprovalDate,
                        govtReferenceNo = govtRefNo,
                        govtDepartmentInfo = govtDeptInfo,
                        facebookUrl = fbUrl,
                        instagramUrl = igUrl,
                        youtubeUrl = ytUrl,
                        whatsappUrl = waUrl,
                        twitterUrl = xUrl,
                        supportEmail = email,
                        supportPhone = phone,
                        businessAddress = address,
                        supportHours = hours,
                        officialWebsite = website,
                        grievanceOfficerName = grievanceName.trim(),
                        grievanceOfficerEmail = grievanceEmail.trim(),
                        grievanceOfficerPhone = grievancePhone.trim(),
                        grievanceOfficerAddress = grievanceAddr.trim(),
                        nodalOfficerName = nodalName.trim(),
                        nodalOfficerContact = nodalContact.trim(),
                        legalEntityName = legalEntity.trim(),
                        gstinNumber = gstinNumber.trim()
                    )
                    AppConfigManager.updateConfig(context, updated)
                    val savedMsg = when (currentLanguage) {
                        AppLanguage.BENGALI -> "তথ্য সফলভাবে সংরক্ষিত হয়েছে এবং ফুটারে আপডেট হয়েছে!"
                        AppLanguage.HINDI -> "जानकारी सफलतापूर्वक सहेजी गई और फ़ुटर में अपडेट हो गई!"
                        AppLanguage.ENGLISH -> "Configuration saved and updated in footer!"
                    }
                    Toast.makeText(context, savedMsg, Toast.LENGTH_LONG).show()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("admin_save_config_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "সংরক্ষণ করুন"
                        AppLanguage.HINDI -> "परिवर्तन सहेजें"
                        AppLanguage.ENGLISH -> "Save Changes"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
        }
    }
}
