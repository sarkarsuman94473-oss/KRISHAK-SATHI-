package com.example.ui.info

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.config.AppConfigManager
import com.example.data.model.AppLanguage
import com.example.ui.theme.AgriEarthSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.util.safeStartActivity

@Composable
fun AboutUsScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onOpenDiscoveryHub: () -> Unit = {}
) {
    val config by AppConfigManager.configState.collectAsState()

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
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("about_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "আমাদের সম্পর্কে (About Us)"
                                AppLanguage.HINDI -> "हमारे बारे में (About Us)"
                                AppLanguage.ENGLISH -> "About Us"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = config.appName,
                            style = MaterialTheme.typography.bodySmall,
                            color = com.example.ui.theme.RoyalGold,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_krishak_sathi_logo),
                            contentDescription = "KRISHAK SATHI Logo",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = config.appName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(text = config.getSubtitle(currentLanguage), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AgriTextPrimary)
                        }
                    }
                    Text(
                        text = config.getDescription(currentLanguage),
                        fontSize = 13.sp,
                        color = AgriTextSecondary,
                        lineHeight = 20.sp
                    )
                    Button(
                        onClick = onOpenDiscoveryHub,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgriGreenPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("about_open_discovery_hub_btn")
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "পাবলিক কৃষি জ্ঞান ও ব্র্যান্ড হাব খুলুন"
                                AppLanguage.HINDI -> "सार्वजनिक कृषि ज्ञान व ब्रांड हब खोलें"
                                AppLanguage.ENGLISH -> "Open Public Knowledge & Brand Hub"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "🎯 আমাদের উদ্দেশ্য ও লক্ষ্য:"
                            AppLanguage.HINDI -> "🎯 हमारा उद्देश्य व लक्ष्य:"
                            AppLanguage.ENGLISH -> "🎯 Our Mission & Vision:"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI ->
                                "১. প্রতিটি ক্ষুদ্র ও মাঝারি কৃষকের হাতে সঠিক তথ্য ও বৈজ্ঞানিক সমাধান পৌঁছে দেওয়া।\n" +
                                    "২. চারা ও বীজ বাজার, ফসল সুরক্ষা ওষুধ, ও নার্সারির সাথে সরাসরি সংযোগ স্থাপন।\n" +
                                    "৩. উন্নত Google Gemini AI যুক্ত কৃষক সহকারী ও ১৫টি বিষয়ের বিশ্ববিদ্যালয় বিজ্ঞানীদের পরামর্শ সুবিধা।\n" +
                                    "৪. স্থানীয় ভাষা (বাংলা, হিন্দি, ইংরেজি) ব্যবহারের মাধ্যমে প্রযুক্তিকে সহজবোধ্য করা।"
                            AppLanguage.HINDI ->
                                "१. प्रत्येक छोटे व मध्यम किसान तक सटीक जानकारी और वैज्ञानिक समाधान पहुँचाना।\n" +
                                    "२. पौध व बीज बाज़ार, फसल सुरक्षा दवाओं और नर्सरी से सीधा जुड़ाव सुनिश्चित करना।\n" +
                                    "३. आधुनिक AI आधारित कृषक सहायक व 15 कृषि क्षेत्रों के वैज्ञानिकों से परामर्श सुविधा।\n" +
                                    "४. स्थानीय भाषाओं (বাংলা, हिन्दी, English) के माध्यम से तकनीक को सरल बनाना।"
                            AppLanguage.ENGLISH ->
                                "1. Deliver accurate information and scientific solutions to every smallholder farmer.\n" +
                                    "2. Connect growers directly with nurseries, quality seed markets, and crop medicine guides.\n" +
                                    "3. Provide smart AI assistance and verified university agricultural expert consultations.\n" +
                                    "4. Make farming technology accessible in local languages (Bengali, Hindi, and English)."
                        },
                        fontSize = 13.sp,
                        color = AgriTextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

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
                            AppLanguage.BENGALI -> "🖼 অফিশিয়াল প্রচার পোস্টার ও ফিচারসমূহ:"
                            AppLanguage.HINDI -> "🖼 आधिकारिक प्रचार पोस्टर व सुविधाएं:"
                            AppLanguage.ENGLISH -> "🖼 Official Promotion Poster & Features:"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(com.example.ui.theme.RoyalGreenSecondaryBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_promo_poster),
                            contentDescription = "KRISHAK SATHI Promo Poster",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.4f),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI ->
                                "🌾 কৃষকের পাশে, সবসময় — স্মার্ট কৃষির সঙ্গী, কৃষক সাথী।\n• ফসলের সমস্যা সমাধান (AI + Gemini)\n• চারা ও বীজের তথ্য\n• ওষুধ ও সার নির্দেশিকা\n• বাজার মূল্য ও বিক্রয় ব্যবস্থা\n• সরকারি প্রকল্প ও বিশেষজ্ঞ সহায়তা"
                            AppLanguage.HINDI ->
                                "🌾 किसान के साथ, हमेशा — स्मार्ट खेती का साथी, कृषक साथी।\n• फसल समस्या समाधान (AI + Gemini)\n• पौध व बीज बाज़ार\n• दवा व उर्वरक गाइड\n• दैनिक मंडी भाव व बिक्री व्यवस्था\n• सरकारी योजनाएं व कृषि विशेषज्ञ सहायता"
                            AppLanguage.ENGLISH ->
                                "🌾 Always With The Farmer — KRISHAK SATHI Smart Farming Companion.\n• Crop Problem Diagnosis (AI + Gemini)\n• Nursery & Seed Market\n• Medicine & Fertilizer Guide\n• Direct Marketplace & Prices\n• Government Schemes & Experts"
                        },
                        fontSize = 12.sp,
                        color = AgriTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "📱 রিলিজ ও সংস্করণ তথ্য:"
                            AppLanguage.HINDI -> "📱 रिलीज़ व संस्करण जानकारी:"
                            AppLanguage.ENGLISH -> "📱 Release & Version Info:"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AgriTextPrimary
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "অ্যাপের নাম: ${config.appName}"
                            AppLanguage.HINDI -> "ऐप का नाम: ${config.appName}"
                            AppLanguage.ENGLISH -> "App Name: ${config.appName}"
                        },
                        fontSize = 12.sp
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "প্রথম প্রকাশ: ${config.releaseDate}"
                            AppLanguage.HINDI -> "प्रथम प्रकाशन: ${config.releaseDate}"
                            AppLanguage.ENGLISH -> "Release Date: ${config.releaseDate}"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "বর্তমান সংস্করণ: ${config.currentVersion}"
                            AppLanguage.HINDI -> "वर्तमान संस्करण: ${config.currentVersion}"
                            AppLanguage.ENGLISH -> "Current Version: ${config.currentVersion}"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "সর্বশেষ আপডেট: ${config.lastUpdatedDate}"
                            AppLanguage.HINDI -> "अंतिम अपडेट: ${config.lastUpdatedDate}"
                            AppLanguage.ENGLISH -> "Last Updated: ${config.lastUpdatedDate}"
                        },
                        fontSize = 12.sp
                    )
                    Text(text = config.copyrightText, fontSize = 11.sp, color = AgriEarthSecondary)
                }
            }
        }
        }
    }
}

@Composable
fun ContactUsScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val config by AppConfigManager.configState.collectAsState()

    var feedbackMsg by remember { mutableStateOf("") }
    var senderName by remember { mutableStateOf("") }
    var sentFeedback by remember { mutableStateOf(false) }

    fun dialPhone(phone: String) {
        val num = phone.filter { it.isDigit() || it == '+' }
        val msg = when (currentLanguage) {
            AppLanguage.BENGALI -> "ডায়াল করা সম্ভব হয়নি"
            AppLanguage.HINDI -> "कॉल डायल करना संभव नहीं हुआ"
            AppLanguage.ENGLISH -> "Unable to open phone dialer"
        }
        if (num.isBlank()) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            return
        }
        safeStartActivity(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num")), msg)
    }

    fun sendEmail(email: String) {
        val trimmed = email.trim()
        if (trimmed.isBlank()) return
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$trimmed")
            putExtra(Intent.EXTRA_SUBJECT, "Krishak Sathi Farmer Inquiry")
        }
        val msg = when (currentLanguage) {
            AppLanguage.BENGALI -> "ইমেইল অ্যাপ পাওয়া যায়নি"
            AppLanguage.HINDI -> "ईमेल ऐप उपलब्ध नहीं है"
            AppLanguage.ENGLISH -> "No email app found"
        }
        safeStartActivity(context, intent, msg)
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
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("contact_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "যোগাযোগ ও সহায়তা (Contact Us)"
                                AppLanguage.HINDI -> "संपर्क व सहायता (Contact Us)"
                                AppLanguage.ENGLISH -> "Contact & Support"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "OFFICIAL HELPLINE & SUPPORT",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.example.ui.theme.RoyalGold,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Direct Contact Cards (use dynamic config supportPhone with safe fallback)
                ContactInfoCard(
                    icon = Icons.Default.Call,
                    title = when (currentLanguage) {
                        AppLanguage.BENGALI -> "টোল ফ্রি কৃষক হেল্পলাইন"
                        AppLanguage.HINDI -> "टोल फ्री किसान हेल्पलाइन"
                        AppLanguage.ENGLISH -> "Toll-Free Farmer Helpline"
                    },
                    value = config.getSupportPhone(currentLanguage),
                    actionLabel = when (currentLanguage) {
                        AppLanguage.BENGALI -> "কল করুন"
                        AppLanguage.HINDI -> "कॉल करें"
                        AppLanguage.ENGLISH -> "Call"
                    },
                    onAction = { dialPhone(config.supportPhone.ifBlank { "18001801551" }) }
                )

            ContactInfoCard(
                icon = Icons.Default.Email,
                title = when (currentLanguage) {
                    AppLanguage.BENGALI -> "অফিসিয়াল সাপোর্ট ইমেইল"
                    AppLanguage.HINDI -> "आधिकारिक सहायता ईमेल"
                    AppLanguage.ENGLISH -> "Official Support Email"
                },
                value = config.supportEmail,
                actionLabel = when (currentLanguage) {
                    AppLanguage.BENGALI -> "ইমেইল"
                    AppLanguage.HINDI -> "ईमेल"
                    AppLanguage.ENGLISH -> "Email"
                },
                onAction = { sendEmail(config.supportEmail) }
            )

            ContactInfoCard(
                icon = Icons.Default.Schedule,
                title = when (currentLanguage) {
                    AppLanguage.BENGALI -> "সাপোর্ট সময়সীমা"
                    AppLanguage.HINDI -> "सहायता समय"
                    AppLanguage.ENGLISH -> "Support Hours"
                },
                value = config.getSupportHours(currentLanguage),
                actionLabel = null,
                onAction = {}
            )

            ContactInfoCard(
                icon = Icons.Default.LocationOn,
                title = when (currentLanguage) {
                    AppLanguage.BENGALI -> "অফিসিয়াল ব্যবসায়িক ঠিকানা"
                    AppLanguage.HINDI -> "आधिकारिक कार्यालय पता"
                    AppLanguage.ENGLISH -> "Official Business Address"
                },
                value = config.getBusinessAddress(currentLanguage),
                actionLabel = null,
                onAction = {}
            )

            // Contact Support Message Form
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
                            AppLanguage.BENGALI -> "✉️ দ্রুত বার্তা বা সমস্যা পাঠান:"
                            AppLanguage.HINDI -> "✉️ त्वरित संदेश या समस्या भेजें:"
                            AppLanguage.ENGLISH -> "✉️ Send Quick Message or Feedback:"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { senderName = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "আপনার নাম ও ফোন নম্বর"
                                AppLanguage.HINDI -> "आपका नाम व फ़ोन नंबर"
                                AppLanguage.ENGLISH -> "Your Name & Phone Number"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = feedbackMsg,
                        onValueChange = { feedbackMsg = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "আপনার সমস্যা বা মতামত বিস্তারিত লিখুন..."
                                AppLanguage.HINDI -> "अपनी समस्या या सुझाव विस्तार से लिखें..."
                                AppLanguage.ENGLISH -> "Write your issue or feedback in detail..."
                            })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    if (sentFeedback) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "✓ আপনার বার্তা সফলভাবে গৃহীত হয়েছে! শীঘ্রই যোগাযোগ করা হবে।"
                                AppLanguage.HINDI -> "✓ आपका संदेश सफलतापूर्वक प्राप्त हो गया है! शीघ्र संपर्क किया जाएगा।"
                                AppLanguage.ENGLISH -> "✓ Your message has been received! We will get back to you shortly."
                            },
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (feedbackMsg.isNotBlank()) {
                                sentFeedback = true
                                feedbackMsg = ""
                            } else {
                                val errMsg = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অনুগ্রহ করে আপনার বার্তা লিখুন"
                                    AppLanguage.HINDI -> "कृपया अपना संदेश लिखें"
                                    AppLanguage.ENGLISH -> "Please write your message first"
                                }
                                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgriGreenPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "বার্তা জমা দিন"
                                AppLanguage.HINDI -> "संदेश भेजें"
                                AppLanguage.ENGLISH -> "Send Message"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContactInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.widthIn(min = 180.dp, max = 480.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AgriTextPrimary)
                    Text(text = value, fontSize = 12.sp, color = AgriTextSecondary)
                }
            }

            if (actionLabel != null) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreenPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FaqScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    val faqItems = when (currentLanguage) {
        AppLanguage.BENGALI -> listOf(
            Pair("কৃষি বিশেষজ্ঞদের সাথে কীভাবে কথা বলব?", "অ্যাপের 'কৃষি বিশেষজ্ঞ' সেকশনে যান। সেখানে ১৫টি বিভাগের বিজ্ঞানীদের প্রোফাইল দেখে সরাসরি চ্যাট, অডিও কল, ভিডিও কল অথবা অ্যাপয়েন্টমেন্ট বুকিং করতে পারবেন।"),
            Pair("KRISHAK AI এবং Gemini AI কীভাবে কাজ করে?", "কৃষক সহায়ক AI আপনার ফসলের রোগবালাই, পাতা হলুদ হওয়া বা সারের প্রশ্ন বাংলায় উত্তর দেয়। তবে জটিল ক্ষেত্রে বিজ্ঞানীর পরামর্শ নেওয়া বাঞ্ছনীয়।"),
            Pair("নার্সারি ও বীজ বাজারে কীভাবে অর্ডার করব?", "নার্সারি বাজারে গিয়ে নির্ভরযোগ্য বিক্রেতা নির্বাচন করুন এবং সরাসরি চ্যাট বা কল করে পছন্দের চারা ও বীজ কিনতে পারবেন।"),
            Pair("বাজার দর কোথা থেকে সংগৃহীত হয়?", "পশ্চিমবঙ্গ ও আশপাশের নিকটস্থ পাইকারি মণ্ডির রিয়েল-টাইম রেট থেকে তথ্য সংগ্রহ করে প্রদর্শিত হয়।"),
            Pair("আমার খামার ও ফসলের ডেটা কি সুরক্ষিত?", "হ্যাঁ, আপনার কোনো ব্যক্তিগত তথ্য বাণিজ্যিক বিজ্ঞাপনদাতার কাছে শেয়ার করা হয় না। সমস্ত হিসাব স্থানীয় ডিভাইসে নিরাপদে জমা থাকে।"),
            Pair("সরকারি প্রকল্পের সুবিধা কীভাবে পাওয়া যাবে?", "'সরকারি তথ্য ও নিবন্ধন' পেজে গিয়ে বাংলার কৃষি ও পিএম-কিষাণ পোর্টালের সরাসরি লিংক পাওয়া যাবে।")
        )
        AppLanguage.HINDI -> listOf(
            Pair("मैं कृषि विशेषज्ञों से कैसे बात कर सकता हूँ?", "ऐप के 'कृषि विशेषज्ञ' अनुभाग में जाएं। वहाँ 15 कृषि क्षेत्रों के वैज्ञानिकों की प्रोफ़ाइल देखकर सीधे चैट, ऑडियो कॉल, वीडियो कॉल या अपॉइंटमेंट बुक कर सकते हैं।"),
            Pair("KRISHAK AI और Gemini AI कैसे काम करते हैं?", "कृषक सहायक AI आपकी फसलों के रोग, पीले पत्ते या उर्वरक से जुड़े सवालों के तुरंत उत्तर देता है। गंभीर मामलों में विशेषज्ञ वैज्ञानिक से सलाह अवश्य लें।"),
            Pair("नर्सरी व बीज बाज़ार से ऑर्डर कैसे करें?", "नर्सरी बाज़ार में जाकर सत्यापित विक्रेता चुनें और सीधे चैट या कॉल करके अपने पसंदीदा पौधे व बीज खरीदें।"),
            Pair("मंडी के बाज़ार भाव कहाँ से आते हैं?", "नजदीकी कृषि मंडियों के नवीनतम थोक भावों से जानकारी एकत्र कर ऐप में दिखाई जाती है।"),
            Pair("क्या मेरा खेत और फसल डेटा सुरक्षित है?", "हाँ, आपकी कोई भी निजी जानकारी विज्ञापनदाताओं के साथ साझा नहीं की जाती। आपका सारा रिकॉर्ड सुरक्षित रहता है।"),
            Pair("सरकारी योजनाओं का लाभ कैसे प्राप्त करें?", "'सरकारी जानकारी व पंजीकरण' पृष्ठ पर जाकर आप कृषि पोर्टल और पीएम-किसान के सीधे लिंक प्राप्त कर सकते हैं।")
        )
        AppLanguage.ENGLISH -> listOf(
            Pair("How can I consult Agriculture Experts?", "Go to the 'Agriculture Experts' section in the app. Browse verified scientists across 15 specialties to start a chat, audio/video call, or book an appointment."),
            Pair("How do KRISHAK AI and Gemini AI work?", "Krishak Sathi AI answers questions on crop diseases, yellowing leaves, or fertilizers in your selected language. For critical issues, always confirm with a verified specialist."),
            Pair("How do I order from the Nursery & Seed Market?", "Open Nursery & Seeds Market, browse verified sellers, inspect plant details, and order directly or contact the nursery via phone or message."),
            Pair("Where do market prices come from?", "Prices are compiled from wholesale mandi rates across major West Bengal and nearby agricultural markets."),
            Pair("Is my farm and crop data safe?", "Yes, your personal data is never shared with commercial advertisers and your farm records remain securely stored."),
            Pair("How can I access Government agriculture schemes?", "Visit the 'Government Registration & Info' page for direct official links to Banglar Krishi, PM-KISAN, Agricoop, and the Kisan Call Center.")
        )
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
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("faq_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "সাধারণ জিজ্ঞাসা (FAQ)"
                                AppLanguage.HINDI -> "अक्सर पूछे जाने वाले प्रश्न (FAQ)"
                                AppLanguage.ENGLISH -> "Frequently Asked Questions (FAQ)"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "HELP & FREQUENTLY ASKED QUESTIONS",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.example.ui.theme.RoyalGold,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                faqItems.forEach { (q, a) ->
                    FaqAccordionItem(question = q, answer = a)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun FaqAccordionItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AgriTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = answer,
                        fontSize = 13.sp,
                        color = AgriTextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
