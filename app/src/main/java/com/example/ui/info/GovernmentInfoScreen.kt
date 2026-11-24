package com.example.ui.info

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.local.config.AppConfigManager
import com.example.data.model.AppLanguage
import com.example.ui.theme.AgriAlertRed
import com.example.ui.theme.AgriEarthSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.util.safeStartActivity

@Composable
fun GovernmentInfoScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onOpenAdminConfig: () -> Unit = {}
) {
    val context = LocalContext.current
    val config by AppConfigManager.configState.collectAsState()

    fun openUrl(url: String) {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return
        val errMsg = when (currentLanguage) {
            AppLanguage.BENGALI -> "লিংক খোলা সম্ভব হয়নি"
            AppLanguage.HINDI -> "लिंक खोलना संभव नहीं हुआ"
            AppLanguage.ENGLISH -> "Unable to open link"
        }
        safeStartActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(trimmed)), errMsg)
    }

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
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("govt_info_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "সরকারি নিবন্ধন ও তথ্য"
                                AppLanguage.HINDI -> "सरकारी पंजीकरण व जानकारी"
                                AppLanguage.ENGLISH -> "Government Registration & Information"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "OFFICIAL REGISTRATION & DIRECTORY",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.example.ui.theme.RoyalGold,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onOpenAdminConfig, modifier = Modifier.size(48.dp).testTag("govt_admin_edit_btn")) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Manage",
                            tint = Color.White
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
                    .widthIn(max = 720.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Status Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (config.isGovtApproved) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (config.isGovtApproved) AgriGreenPrimary else com.example.ui.theme.RoyalWarningBg,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (config.isGovtApproved) Icons.Default.Verified else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (config.isGovtApproved) Color.White else com.example.ui.theme.RoyalGold,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (config.isGovtApproved) {
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "সরকারি অনুমোদন স্থিতি: অনুমোদিত"
                                            AppLanguage.HINDI -> "सरकारी अनुमोदन स्थिति: स्वीकृत"
                                            AppLanguage.ENGLISH -> "Govt. Approval Status: Approved"
                                        }
                                    } else {
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "সরকারি নিবন্ধন ও অনুমোদন স্থিতি"
                                            AppLanguage.HINDI -> "सरकारी पंजीकरण व अनुमोदन स्थिति"
                                            AppLanguage.ENGLISH -> "Govt. Registration & Approval Status"
                                        }
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (config.isGovtApproved) MaterialTheme.colorScheme.onPrimaryContainer else AgriTextPrimary
                                )
                                Text(
                                    text = if (config.isGovtApproved) "OFFICIAL GOVERNMENT APPROVED" else "HONEST STATUS & VERIFICATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (config.isGovtApproved) MaterialTheme.colorScheme.onPrimaryContainer else AgriEarthSecondary
                                )
                            }
                        }

                        // Strict Honest Status Message (explicit paired text & surface colors for Dark Mode legibility)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (config.isGovtApproved) MaterialTheme.colorScheme.surface else com.example.ui.theme.RoyalWarningBg,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (config.isGovtApproved) {
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "এই অ্যাপ্লিকেশনটির সরকারি অনুমোদন ও প্রযুক্তিগত নিবন্ধন সফলভাবে সম্পন্ন ও যাচাইকৃত হয়েছে।"
                                        AppLanguage.HINDI -> "इस एप्लिकेशन का सरकारी अनुमोदन और तकनीकी पंजीकरण सफलतापूर्वक पूर्ण व सत्यापित हो चुका है।"
                                        AppLanguage.ENGLISH -> "This application's government approval and technical registration have been completed and verified."
                                    }
                                } else {
                                    config.getGovtRegistrationStatusText(currentLanguage)
                                },
                                modifier = Modifier.padding(12.dp),
                                color = if (config.isGovtApproved) MaterialTheme.colorScheme.primary else com.example.ui.theme.RoyalSoftGold,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }

                    // Configurable Government Details
                    if (config.isGovtApproved) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            DetailRow(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "নিবন্ধন সার্টিফিকেট নং:"
                                    AppLanguage.HINDI -> "पंजीकरण प्रमाणपत्र सं.:"
                                    AppLanguage.ENGLISH -> "Registration Certificate No.:"
                                },
                                config.govtCertificateNo.ifBlank { "N/A" }
                            )
                            DetailRow(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "প্রদানকারী সরকারি কর্তৃপক্ষ:"
                                    AppLanguage.HINDI -> "जारीकर्ता सरकारी प्राधिकरण:"
                                    AppLanguage.ENGLISH -> "Issuing Govt. Authority:"
                                },
                                config.getGovtIssuingAuthority(currentLanguage)
                            )
                            DetailRow(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অনুমোদনের তারিখ:"
                                    AppLanguage.HINDI -> "अनुमोदन तिथि:"
                                    AppLanguage.ENGLISH -> "Approval Date:"
                                },
                                config.govtApprovalDate.ifBlank { "N/A" }
                            )
                            DetailRow(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অফিসিয়াল রেফারেন্স নং:"
                                    AppLanguage.HINDI -> "आधिकारिक संदर्भ सं.:"
                                    AppLanguage.ENGLISH -> "Official Reference No.:"
                                },
                                config.govtReferenceNo.ifBlank { "N/A" }
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            DetailRow(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "পর্যালোচনাধীন কর্তৃপক্ষ:"
                                    AppLanguage.HINDI -> "समीक्षाधीन प्राधिकरण:"
                                    AppLanguage.ENGLISH -> "Reviewing Authority:"
                                },
                                config.getGovtIssuingAuthority(currentLanguage)
                            )
                            DetailRow(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "প্রাসঙ্গিক বিভাগ:"
                                    AppLanguage.HINDI -> "संबंधित विभाग:"
                                    AppLanguage.ENGLISH -> "Relevant Department:"
                                },
                                config.getGovtDepartmentInfo(currentLanguage)
                            )
                        }
                    }
                }
            }

            // Legal & Transparency Caution Banner
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Policy,
                        contentDescription = null,
                        tint = AgriAlertRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "স্বচ্ছতা ও সত্যনিষ্ঠ নীতি:"
                                AppLanguage.HINDI -> "पारदर्शिता व सत्यनिष्ठा नीति:"
                                AppLanguage.ENGLISH -> "Transparency & Integrity Notice:"
                            },
                            fontWeight = FontWeight.Bold,
                            color = AgriTextPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "KRISHAK SATHI কৃষকদের সুবিধার্থে নির্মিত একটি ডিজিটাল প্রযুক্তি প্ল্যাটফর্ম। সরকারি বৈধ অনুমতি বা সার্টিফিকেট ব্যতীত কোনো প্রতীক, জাতীয় প্রতীক বা অনুমোদন ভিত্তিহীনভাবে দাবি করা হয় না।"
                                AppLanguage.HINDI -> "KRISHAK SATHI किसानों की सहायता हेतु निर्मित एक डिजिटल कृषि मंच है। बिना वैध सरकारी अनुमति या प्रमाणपत्र के कोई भी राष्ट्रीय चिन्ह या अनुमोदन दावा नहीं किया जाता।"
                                AppLanguage.ENGLISH -> "KRISHAK SATHI is a digital agriculture support platform built for farmers. No national emblem or official endorsement is ever claimed without valid certification."
                            },
                            color = AgriTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Official Agricultural Departments & Verified Portals
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "সরকারি কৃষি পোর্টাল ও সহায়তা তথ্য:"
                                AppLanguage.HINDI -> "सरकारी कृषि पोर्टल व सहायता जानकारी:"
                                AppLanguage.ENGLISH -> "Official Agriculture Portals & Helpline:"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AgriTextPrimary
                        )
                    }

                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "কৃষকদের সুবিধার্থে পশ্চিমবঙ্গ সরকার ও ভারত সরকারের অফিসিয়াল কৃষি তথ্যসূত্রসমূহ:"
                            AppLanguage.HINDI -> "किसानों की सुविधा के लिए पश्चिम बंगाल सरकार और भारत सरकार के आधिकारिक कृषि पोर्टल:"
                            AppLanguage.ENGLISH -> "Official agricultural resources from the Government of West Bengal and Government of India:"
                        },
                        fontSize = 12.sp,
                        color = AgriTextSecondary
                    )

                    // Portal 1: Banglar Krishi
                    GovtPortalItem(
                        name = when (currentLanguage) {
                            AppLanguage.BENGALI -> "বাংলার কৃষি পোর্টাল (Banglar Krishi)"
                            AppLanguage.HINDI -> "बांग्लार कृषि पोर्टल (Banglar Krishi)"
                            AppLanguage.ENGLISH -> "Banglar Krishi Portal"
                        },
                        dept = when (currentLanguage) {
                            AppLanguage.BENGALI -> "কৃষি বিভাগ, পশ্চিমবঙ্গ সরকার"
                            AppLanguage.HINDI -> "कृषि विभाग, पश्चिम बंगाल सरकार"
                            AppLanguage.ENGLISH -> "Department of Agriculture, Govt. of West Bengal"
                        },
                        url = "https://banglarkrishi.wb.gov.in",
                        onOpen = { openUrl("https://banglarkrishi.wb.gov.in") }
                    )

                    // Portal 2: PM-Kisan
                    GovtPortalItem(
                        name = when (currentLanguage) {
                            AppLanguage.BENGALI -> "পিএম-কিষাণ সম্মান নিধি (PM-Kisan)"
                            AppLanguage.HINDI -> "पीएम-किसान सम्मान निधि (PM-Kisan)"
                            AppLanguage.ENGLISH -> "PM-KISAN Samman Nidhi"
                        },
                        dept = when (currentLanguage) {
                            AppLanguage.BENGALI -> "কৃষি ও কৃষক কল্যাণ মন্ত্রক, ভারত সরকার"
                            AppLanguage.HINDI -> "कृषि एवं किसान कल्याण मंत्रालय, भारत सरकार"
                            AppLanguage.ENGLISH -> "Ministry of Agriculture & Farmers Welfare, Govt. of India"
                        },
                        url = "https://pmkisan.gov.in",
                        onOpen = { openUrl("https://pmkisan.gov.in") }
                    )

                    // Portal 3: Ministry of Agriculture
                    GovtPortalItem(
                        name = when (currentLanguage) {
                            AppLanguage.BENGALI -> "কেন্দ্রীয় কৃষি পোর্টাল (Agricoop)"
                            AppLanguage.HINDI -> "केंद्रीय कृषि पोर्टल (Agricoop)"
                            AppLanguage.ENGLISH -> "Central Agriculture Portal (Agricoop)"
                        },
                        dept = "Department of Agriculture & Farmers Welfare",
                        url = "https://agricoop.nic.in",
                        onOpen = { openUrl("https://agricoop.nic.in") }
                    )

                    // Portal 4: Kisan Call Center
                    GovtPortalItem(
                        name = when (currentLanguage) {
                            AppLanguage.BENGALI -> "কিষাণ কল সেন্টার (Kisan Call Center)"
                            AppLanguage.HINDI -> "किसान कॉल सेंटर (Kisan Call Center)"
                            AppLanguage.ENGLISH -> "Kisan Call Center"
                        },
                        dept = when (currentLanguage) {
                            AppLanguage.BENGALI -> "টোল ফ্রি সার্বক্ষণিক কৃষক হেল্পলাইন"
                            AppLanguage.HINDI -> "टोल फ्री किसान सहायता हेल्पलाइन"
                            AppLanguage.ENGLISH -> "Toll-Free Farmer Helpline"
                        },
                        url = "tel:18001801551",
                        onOpen = { openUrl("tel:18001801551") }
                    )
                }
            }

            // Edit Admin Section shortcut
            OutlinedButton(
                onClick = onOpenAdminConfig,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("open_admin_footer_settings_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "অ্যাডমিন সেটিংস: তথ্য ও অনুমোদন সম্পাদনা করুন"
                        AppLanguage.HINDI -> "एडमिन सेटिंग्स: जानकारी व अनुमोदन संपादित करें"
                        AppLanguage.ENGLISH -> "Admin Settings: Edit Info & Approval"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AgriTextPrimary, modifier = Modifier.weight(1.2f))
        Text(text = value, fontSize = 12.sp, color = AgriTextSecondary, textAlign = TextAlign.End, modifier = Modifier.weight(1.8f))
    }
}

@Composable
private fun GovtPortalItem(
    name: String,
    dept: String,
    url: String,
    onOpen: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable { onOpen() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AgriTextPrimary)
                Text(text = dept, fontSize = 11.sp, color = AgriEarthSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
