package com.example.ui.info

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.AgriEarthSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextPrimary

enum class LegalTab(val titleBn: String, val titleHi: String, val titleEn: String) {
    PRIVACY("গোপনীয়তা নীতি", "गोपनीयता नीति", "Privacy Policy"),
    TERMS("ব্যবহারের শর্তাবলী", "नियम व शर्तें", "Terms & Conditions"),
    DATA_POLICY("তথ্য নীতি", "डेटा नीति", "Data Policy"),
    AGRI_DISCLAIMER("কৃষি দায়মুক্তি", "कृषि अस्वीकरण", "Agri Disclaimer"),
    AI_DISCLAIMER("AI তথ্যের সতর্কতা", "एआई अस्वीकरण", "AI Disclaimer"),
    EXPERT_DISCLAIMER("পরামর্শ নীতি", "परामर्श नीति", "Expert Disclaimer"),
    MARKETPLACE_POLICIES("সকল নীতিমালা (২৭)", "सभी नीतियां (27)", "All 27 Policies"),
    INDIAN_COMPLIANCE("ভারতীয় আইন ও মানদণ্ড", "भारतीय कानूनी मैट्रिक्स", "Indian Compliance"),
    GRIEVANCE_REDRESSAL("অভিযোগ ও বিরোধ নিষ্পত্তি", "शिकायत निवारण केंद्र", "Grievance Centre");

    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> titleBn
        AppLanguage.HINDI -> titleHi
        AppLanguage.ENGLISH -> titleEn
    }
}

@Composable
fun LegalPagesScreen(
    currentLanguage: AppLanguage,
    initialTab: LegalTab = LegalTab.PRIVACY,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("legal_back_btn")) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "আইনি ও স্বচ্ছতা তথ্য"
                                    AppLanguage.HINDI -> "कानूनी व पारदर्शिता जानकारी"
                                    AppLanguage.ENGLISH -> "Legal & Transparency Information"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "LEGAL POLICIES & COMPLIANCE",
                                style = MaterialTheme.typography.bodySmall,
                                color = com.example.ui.theme.RoyalGold,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    ScrollableTabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = AgriGreenPrimary,
                        contentColor = Color.White,
                        edgePadding = 12.dp
                    ) {
                    LegalTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            modifier = Modifier.testTag("legal_tab_${tab.name.lowercase()}"),
                            text = {
                                Text(
                                    text = tab.getTitle(currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
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
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = com.example.ui.theme.EarthWarnSurface,
                    border = BorderStroke(1.dp, com.example.ui.theme.EarthWarnInk.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth().testTag("legal_statutory_notice_card")
                ) {
                    Text(
                        text = LegalPolicyCatalog.liabilityNotice(currentLanguage),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = com.example.ui.theme.EarthWarnInk,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                when (selectedTab) {
                    LegalTab.PRIVACY -> PrivacyPolicyContent(currentLanguage)
                    LegalTab.TERMS -> TermsContent(currentLanguage)
                    LegalTab.DATA_POLICY -> DataPolicyContent(currentLanguage)
                    LegalTab.AGRI_DISCLAIMER -> AgriDisclaimerContent(currentLanguage)
                    LegalTab.AI_DISCLAIMER -> AiDisclaimerContent(currentLanguage)
                    LegalTab.EXPERT_DISCLAIMER -> ExpertDisclaimerContent(currentLanguage)
                    LegalTab.MARKETPLACE_POLICIES -> All27PoliciesContent(currentLanguage)
                    LegalTab.INDIAN_COMPLIANCE -> IndianComplianceMatrixContent(currentLanguage)
                    LegalTab.GRIEVANCE_REDRESSAL -> GrievanceRedressalPanel(currentLanguage = currentLanguage)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun All27PoliciesContent(currentLanguage: AppLanguage) {
    LegalCard(
        title = when (currentLanguage) {
            AppLanguage.BENGALI -> "📚 কৃষক সাথী মার্কেটপ্লেস ও নিরাপত্তা নীতিমালা (২৭টি নীতি)"
            AppLanguage.HINDI -> "📚 कृषक साथी मार्केटप्लेस व सुरक्षा नीतियां (27 नीतियां)"
            AppLanguage.ENGLISH -> "📚 Platform Trust, Marketplace & Safety Policies (27 Documents)"
        },
        subtitle = when (currentLanguage) {
            AppLanguage.BENGALI -> "খসড়া সংস্করণ — ভারতীয় আইনি পরামর্শকের পর্যালোচনা সাপেক্ষ"
            AppLanguage.HINDI -> "प्रारूप संस्करण — भारतीय कानूनी सलाहकार समीक्षा के अधीन"
            AppLanguage.ENGLISH -> "Draft Policy Registry — Pending Indian Legal Counsel Sign-Off"
        },
        body = when (currentLanguage) {
            AppLanguage.BENGALI -> "প্রতিটি নীতি কৃষক সুরক্ষা, বিক্রেতা যাচাইকরণ, যন্ত্রপাতির নিরাপত্তা, ন্যায্য মূল্য এবং প্রতারণা প্রতিরোধের জন্য তৈরি।"
            AppLanguage.HINDI -> "प्रत्येक नीति किसान सुरक्षा, विक्रेता सत्यापन, मशीनरी सुरक्षा, उचित मूल्य और धोखाधड़ी रोकथाम के लिए तैयार की गई है।"
            AppLanguage.ENGLISH -> "Each policy governs seller verification, equipment condition transparency, agrochemical safety, AI boundaries, fair mandi sourcing, and anti-fraud protections."
        }
    )
    LegalPolicyCatalog.all27Policies.forEach { policy ->
        LegalCard(
            title = policy.title(currentLanguage),
            subtitle = "${policy.code} • ${if (policy.requiresLegalCounselReview) "Pending Legal Counsel Review" else "Active"}",
            body = policy.body(currentLanguage)
        )
    }
}

@Composable
private fun IndianComplianceMatrixContent(currentLanguage: AppLanguage) {
    LegalCard(
        title = when (currentLanguage) {
            AppLanguage.BENGALI -> "⚖️ ভারতীয় আইনি কমপ্লায়েন্স ম্যাট্রিক্স (১১টি কাঠামো)"
            AppLanguage.HINDI -> "⚖️ भारतीय कानूनी अनुपालन मैट्रिक्स (11 ढांचे)"
            AppLanguage.ENGLISH -> "⚖️ Indian Legal & Regulatory Compliance Matrix (11 Frameworks)"
        },
        subtitle = when (currentLanguage) {
            AppLanguage.BENGALI -> "রিলিজ পূর্বে সম্পূর্ণ ব্যবসায়িক ও আইনি যাচাই আবশ্যক"
            AppLanguage.HINDI -> "रिलीज़ से पहले व्यवसाय व कानूनी सत्यापन अनिवार्य"
            AppLanguage.ENGLISH -> "Implementation Status, Accountability & Pre-Launch Blockers"
        },
        body = LegalPolicyCatalog.releaseBlockers.mapIndexed { i, b -> "${i + 1}. $b" }.joinToString("\n\n")
    )
    LegalPolicyCatalog.complianceMatrix.forEach { item ->
        LegalCard(
            title = "${item.frameworkName} [${item.implementationStatus}]",
            subtitle = "${item.ownerRole} • Reviewed: ${item.reviewDate}",
            body = "Applicability: ${item.applicability}\n\nStatus Note: ${item.gapOrActionNote}"
        )
    }
}

@Composable
private fun PrivacyPolicyContent(currentLanguage: AppLanguage) {
    LegalCard(
        title = when (currentLanguage) {
            AppLanguage.BENGALI -> "🔒 গোপনীয়তা নীতি"
            AppLanguage.HINDI -> "🔒 गोपनीयता नीति"
            AppLanguage.ENGLISH -> "🔒 Privacy Policy"
        },
        subtitle = when (currentLanguage) {
            AppLanguage.BENGALI -> "সর্বশেষ সংস্করণ: নভেম্বর ২০২৬"
            AppLanguage.HINDI -> "अंतिम अपडेट: नवंबर 2026"
            AppLanguage.ENGLISH -> "Last Updated: November 2026"
        },
        body = when (currentLanguage) {
            AppLanguage.BENGALI -> """
                ১. তথ্য সংগ্রহ ও ব্যবহার:
                KRISHAK SATHI কৃষকদের ব্যক্তিগত ডেটার সর্বোচ্চ মর্যাদা রক্ষা করে। আমরা কেবলমাত্র চাষের এলাকা, ফসলের নাম এবং বিশেষজ্ঞ যোগাযোগের সুবিধার্থে প্রয়োজনীয় তথ্য সংগ্রহ করি।

                ২. ডিভাইস ও নিরাপত্তা:
                আমরা কৃষকদের ফোন নম্বর বা ফসলের তথ্য কোনো বাণিজ্যিক বিজ্ঞাপনদাতা বা তৃতীয় পক্ষের কাছে বিক্রি করি না। সমস্ত ডেটা সুরক্ষিত সেশনের মাধ্যমে সংরক্ষিত হয়।

                ৩. ক্যামেরা ও ছবি অ্যাক্সেস:
                পাতার রোগ নির্ণয় ও চ্যাট ফিচারের জন্য ছবি তোলার সুযোগ রাখা হয়েছে। কেবল কৃষক নিজে ছবি নির্বাচন করলেই তা বিশেষজ্ঞ বা ডায়াগনসিসের জন্য প্রক্রিয়াকরণ করা হয়।

                ৪. অ্যাকাউন্টের অধিকার:
                কৃষক যেকোনো সময় তার প্রোফাইল তথ্য মুছে ফেলতে বা সংশোধন করতে পারেন।
            """.trimIndent()
            AppLanguage.HINDI -> """
                1. डेटा संग्रहण व उपयोग:
                KRISHAK SATHI किसानों की निजी जानकारी की पूर्ण सुरक्षा करता है। हम केवल खेती के क्षेत्र, फसल के नाम और विशेषज्ञ परामर्श की सुविधा के लिए आवश्यक जानकारी एकत्र करते हैं।

                2. डिवाइस व सुरक्षा:
                हम किसानों का फ़ोन नंबर या फसल का डेटा किसी भी विज्ञापनदाता या तीसरे पक्ष को नहीं बेचते हैं। सभी डेटा सुरक्षित सत्रों में संग्रहीत रहता है।

                3. कैमरा व फ़ोटो एक्सेस:
                पत्तियों के रोग की पहचान व चैट सुविधा के लिए फ़ोटो अपलोड करने का विकल्प दिया गया है। किसान द्वारा चुने जाने पर ही फ़ोटो का विश्लेषण किया जाता है।

                4. खाते के अधिकार:
                किसान किसी भी समय अपनी प्रोफ़ाइल जानकारी को संपादित या हटा सकते हैं।
            """.trimIndent()
            AppLanguage.ENGLISH -> """
                1. Information Collection & Use:
                KRISHAK SATHI respects every farmer's privacy. We collect only essential details such as farming region, crop types, and query context needed for guidance and expert support.

                2. Security & No Commercial Sharing:
                We never sell or rent farmer phone numbers or farm records to third-party advertisers. All account sessions are stored securely.

                3. Camera & Photo Access:
                Crop photos are accessed strictly when you explicitly select or capture an image for plant diagnosis or expert consultation.

                4. Account Control:
                You may update or delete your profile details at any time.
            """.trimIndent()
        }
    )
}

@Composable
private fun TermsContent(currentLanguage: AppLanguage) {
    LegalCard(
        title = when (currentLanguage) {
            AppLanguage.BENGALI -> "📜 ব্যবহারের নিয়ম ও শর্তাবলী"
            AppLanguage.HINDI -> "📜 उपयोग के नियम व शर्तें"
            AppLanguage.ENGLISH -> "📜 Terms & Conditions"
        },
        subtitle = when (currentLanguage) {
            AppLanguage.BENGALI -> "কৃষক সাথী প্ল্যাটফর্ম ব্যবহারের নীতিমালা"
            AppLanguage.HINDI -> "कृषक साथी प्लेटफॉर्म के उपयोग की शर्तें"
            AppLanguage.ENGLISH -> "Guidelines for Using KRISHAK SATHI Platform"
        },
        body = when (currentLanguage) {
            AppLanguage.BENGALI -> """
                ১. প্ল্যাটফর্মের উদ্দেশ্য:
                KRISHAK SATHI কৃষকদের জন্য একটি আধুনিক কৃষি সহায়ক টুল যা ফসলের হিসাব, বিশেষজ্ঞ পরামর্শ, রোগ নির্ণয় এবং বাজার দরের তথ্য সরবরাহ করে।

                ২. দায়িত্বশীল ব্যবহার:
                ব্যবহারকারীকে সত্য তথ্য প্রদানে অনুরোধ করা হচ্ছে। কোনো ভুয়া বা ক্ষতিকারক তথ্য ছড়ানো আইনত দণ্ডনীয়।

                ৩. সেবা সংশোধন:
                কৃষকদের কল্যাণে নতুন প্রযুক্তি ও সরকারি নির্দেশিকা অনুযায়ী ফিচার সময়ে সময়ে আপডেট করা হবে।

                ৪. বুদ্ধিবৃত্তিক সম্পত্তি:
                অ্যাপের লোগো, কোড, ইন্টারফেস এবং ডেটাবেস কপিরাইট আইন দ্বারা সংরক্ষিত।
            """.trimIndent()
            AppLanguage.HINDI -> """
                1. प्लेटफॉर्म का उद्देश्य:
                KRISHAK SATHI किसानों के लिए एक आधुनिक कृषि सहायक ऐप है जो फसल मार्गदर्शन, विशेषज्ञ परामर्श, रोग पहचान और मंडी भाव की जानकारी प्रदान करता है।

                2. जिम्मेदार उपयोग:
                उपयोगकर्ताओं से अनुरोध है कि वे सही जानकारी प्रदान करें। भ्रामक या हानिकारक सामग्री साझा करना वर्जित है।

                3. सेवा अपडेट:
                किसानों के हित में नई तकनीकों और सरकारी दिशानिर्देशों के अनुसार सुविधाएं समय-समय पर अपडेट की जाती हैं।

                4. बौद्धिक संपदा:
                ऐप का लोगो, इंटरफ़ेस और डेटाबेस कॉपीराइट कानून द्वारा सुरक्षित हैं।
            """.trimIndent()
            AppLanguage.ENGLISH -> """
                1. Platform Purpose:
                KRISHAK SATHI is a digital farming assistant providing crop guides, expert consultations, disease diagnostics, and market insights.

                2. Responsible Conduct:
                Users agree to provide accurate details in listings and inquiries and refrain from posting misleading or harmful content.

                3. Service Updates:
                Features and advisories may be updated periodically in alignment with agricultural best practices and government circulars.

                4. Intellectual Property:
                App branding, interface design, and curated databases are protected under applicable copyright laws.
            """.trimIndent()
        }
    )
}

@Composable
private fun DataPolicyContent(currentLanguage: AppLanguage) {
    LegalCard(
        title = when (currentLanguage) {
            AppLanguage.BENGALI -> "🛡️ ব্যবহারকারীর তথ্য সুরক্ষা নীতি"
            AppLanguage.HINDI -> "🛡️ उपयोगकर्ता डेटा सुरक्षा नीति"
            AppLanguage.ENGLISH -> "🛡️ User Data Policy"
        },
        subtitle = when (currentLanguage) {
            AppLanguage.BENGALI -> "কৃষকের তথ্যের সুরক্ষা আমাদের অঙ্গীকার"
            AppLanguage.HINDI -> "किसानों के डेटा की सुरक्षा हमारी प्राथमिकता है"
            AppLanguage.ENGLISH -> "Our Commitment to Protecting Farmer Data"
        },
        body = when (currentLanguage) {
            AppLanguage.BENGALI -> """
                ১. অন-ডিভাইস ও ক্লাউড স্টোরেজ:
                কৃষকের হিসাব, খরচ ও নোট স্থানীয় ডিভাইসে দ্রুত অ্যাক্সেসের জন্য সংরক্ষিত থাকে। ক্লাউড ব্যাকআপ কেবলমাত্র অনুমোদিত সেশনে সিঙ্ক করা হয়।

                ২. বায়োমেট্রিক ও সংবেদনশীল ডেটা:
                অ্যাপটি কৃষকের কোনো ব্যাংক পিন, পাসওয়ার্ড বা ব্যক্তিগত সংবেদনশীল তথ্য সংরক্ষণ বা দাবি করে না।

                ৩. ডেটা মুছে ফেলার অধিকার:
                লগআউট করলে স্থানীয় অস্থায়ী ক্যাশ সুরক্ষিতভাবে মুছে ফেলা যায়।
            """.trimIndent()
            AppLanguage.HINDI -> """
                1. ऑन-डिवाइस व क्लाउड स्टोरेज:
                किसान का हिसाब, खर्च व नोट्स त्वरित उपयोग के लिए डिवाइस पर सुरक्षित रहते हैं और केवल अधिकृत सत्र में सिंक किए जाते हैं।

                2. संवेदनशील वित्तीय डेटा नहीं:
                यह ऐप कभी भी आपका बैंक पिन, ओटीपी या पासवर्ड नहीं मांगता या संग्रहीत नहीं करता।

                3. डेटा हटाने का अधिकार:
                लॉगआउट करने पर स्थानीय अस्थायी कैश सुरक्षित रूप से हटाया जा सकता है।
            """.trimIndent()
            AppLanguage.ENGLISH -> """
                1. Local & Cloud Storage:
                Your expense ledgers, crop logs, and notes are stored locally for fast offline access and synced only during authorized sessions.

                2. No Sensitive Banking Data:
                KRISHAK SATHI never asks for or stores bank PINs, card numbers, or financial passwords.

                3. Cache & Session Removal:
                Temporary local session data can be cleared safely whenever you sign out.
            """.trimIndent()
        }
    )
}

@Composable
private fun AgriDisclaimerContent(currentLanguage: AppLanguage) {
    LegalCard(
        title = when (currentLanguage) {
            AppLanguage.BENGALI -> "🌾 কৃষি তথ্যের দায়মুক্তি"
            AppLanguage.HINDI -> "🌾 कृषि जानकारी अस्वीकरण"
            AppLanguage.ENGLISH -> "🌾 Agricultural Information Disclaimer"
        },
        subtitle = when (currentLanguage) {
            AppLanguage.BENGALI -> "বাস্তব মাঠ ও আবহাওয়া সংক্রান্ত সতর্কতা"
            AppLanguage.HINDI -> "खेत की वास्तविक स्थिति व मौसम संबंधी सावधानी"
            AppLanguage.ENGLISH -> "Field Conditions & Regional Variation Notice"
        },
        body = when (currentLanguage) {
            AppLanguage.BENGALI -> """
                ১. স্থানীয় আবহাওয়া ও মাটির ভিন্নতা:
                অ্যাপে প্রদত্ত সারের ডোজ, সেচ ও কীটনাশকের মাত্রা আদর্শ বৈজ্ঞানিক সুপারিশ অনুসারে প্রস্তুত। তবে মাটির অম্লতা (pH), স্থানীয় আর্দ্রতা ও তাপমাত্রার পার্থক্যে কার্যকর প্রয়োগে তারতম্য হতে পারে।

                ২. প্যাচ টেস্ট ও প্রয়োগ সতর্কতা:
                যেকোনো নতুন রাসায়নিক বা স্প্রে বড় জমিতে প্রয়োগের পূর্বে একটি ছোট অংশে পরীক্ষা করে নেওয়া উচিত।

                ৩. সরকারি বিভাগীয় তথ্য:
                সরকারি সহায়তা, ভর্তুকি ও প্রকল্পের নিয়মাবলী সংশ্লিষ্ট সরকারি কৃষি পোর্টাল দ্বারা নিয়ন্ত্রিত। চূড়ান্ত তথ্যের জন্য স্থানীয় ব্লক কৃষি কর্মকর্তা (ADA/KPS) বা কিষাণ কল সেন্টারে যোগাযোগ করুন।
            """.trimIndent()
            AppLanguage.HINDI -> """
                1. स्थानीय मौसम व मिट्टी की भिन्नता:
                ऐप में दी गई उर्वरक, सिंचाई व कीटनाशक मात्रा मानक वैज्ञानिक सिफारिशों पर आधारित है। मिट्टी के pH, नमी व तापमान के अनुसार वास्तविक मात्रा बदल सकती है।

                2. छोटे हिस्से में परीक्षण:
                किसी भी नए स्प्रे या रसायन को पूरे खेत में डालने से पहले छोटे क्षेत्र में परीक्षण अवश्य करें।

                3. सरकारी योजना नियम:
                सब्सिडी व सरकारी योजनाओं के नियम संबंधित सरकारी पोर्टलों द्वारा नियंत्रित हैं। अंतिम पुष्टि के लिए नजदीकी कृषि अधिकारी या किसान कॉल सेंटर से संपर्क करें।
            """.trimIndent()
            AppLanguage.ENGLISH -> """
                1. Soil & Microclimate Variations:
                Fertilizer, irrigation, and crop protection dosages are based on standard scientific recommendations. Optimal application may vary by local soil pH, humidity, and weather.

                2. Patch Test Advisory:
                Before spraying any new agrochemical across an entire plot, test on a small section and follow label safety instructions.

                3. Official Scheme Circulars:
                Subsidy and welfare schemes are governed strictly by official government portals. Verify eligibility with your local Agriculture Officer or Kisan Call Center.
            """.trimIndent()
        }
    )
}

@Composable
private fun AiDisclaimerContent(currentLanguage: AppLanguage) {
    LegalCard(
        title = when (currentLanguage) {
            AppLanguage.BENGALI -> "🤖 কৃত্রিম বুদ্ধিমত্তা (AI) তথ্যের সতর্কতা"
            AppLanguage.HINDI -> "🤖 आर्टिफिशियल इंटेलिजेंस (AI) अस्वीकरण"
            AppLanguage.ENGLISH -> "🤖 AI-Generated Information Disclaimer"
        },
        subtitle = when (currentLanguage) {
            AppLanguage.BENGALI -> "Gemini AI ও মেশিন লার্নিং সহায়ক নির্দেশনা"
            AppLanguage.HINDI -> "Gemini AI व मशीन लर्निंग सहायता दिशानिर्देश"
            AppLanguage.ENGLISH -> "Gemini AI & Smart Assistant Usage Guidelines"
        },
        body = when (currentLanguage) {
            AppLanguage.BENGALI -> """
                ১. যাচাইকরণের আবশ্যকতা:
                KRISHAK AI এবং জেনারেল AI সহকারী দ্বারা প্রদত্ত যেকোনো কৃষি পরামর্শ বা রোগ বিশ্লেষণ হলো উন্নত কৃত্রিম বুদ্ধিমত্তাভিত্তিক প্রাথমিক ধারণা।

                ২. বিশেষজ্ঞের সাথে মেলানো আবশ্যক:
                কোনো ব্যয়বহুল ওষুধ কেনা বা বিষাক্ত রাসায়নিক স্প্রে করার পূর্বে অ্যাপের এগ্রিকালচার বিশেষজ্ঞ অথবা স্থানীয় অনুমোদিত কৃষি বিজ্ঞানীর কাছ থেকে ডোজ নিশ্চিত করে নেওয়া আবশ্যক।

                ৩. স্বয়ংক্রিয় সিদ্ধান্ত নয়:
                AI সহকারী সরাসরি কোনো চুক্তি সম্পাদন করে না। এটি কৃষকের সিদ্ধান্ত গ্রহণে একটি দ্রুত ডিজিটাল রেফারেন্স হিসেবে কাজ করে।
            """.trimIndent()
            AppLanguage.HINDI -> """
                1. सत्यापन आवश्यक है:
                KRISHAK AI और जेनरल AI सहायक द्वारा दी जाने वाली सलाह या रोग विश्लेषण प्रारंभिक तकनीकी मार्गदर्शन है।

                2. विशेषज्ञ से पुष्टि:
                महंगी दवाएं खरीदने या रासायनिक छिड़काव से पहले ऐप के कृषि विशेषज्ञ या स्थानीय कृषि वैज्ञानिक से सही मात्रा की पुष्टि अवश्य करें।

                3. केवल डिजिटल संदर्भ:
                AI सहायक केवल किसान के निर्णय में सहायता के लिए एक त्वरित डिजिटल संदर्भ के रूप में कार्य करता है।
            """.trimIndent()
            AppLanguage.ENGLISH -> """
                1. Preliminary AI Guidance:
                Crop advisories and symptom analyses generated by KRISHAK AI or the general AI assistant serve as fast preliminary guidance.

                2. Always Confirm Dosages:
                Before purchasing costly inputs or applying potent pesticides, verify the exact dosage with a verified Agriculture Expert or local agronomist.

                3. Digital Reference Only:
                AI responses assist decision-making and do not replace professional field inspection or formal agreements.
            """.trimIndent()
        }
    )
}

@Composable
private fun ExpertDisclaimerContent(currentLanguage: AppLanguage) {
    LegalCard(
        title = when (currentLanguage) {
            AppLanguage.BENGALI -> "👨‍🔬 বিশেষজ্ঞ পরামর্শ নীতি"
            AppLanguage.HINDI -> "👨‍🔬 विशेषज्ञ परामर्श नीति"
            AppLanguage.ENGLISH -> "👨‍🔬 Expert Consultation Disclaimer"
        },
        subtitle = when (currentLanguage) {
            AppLanguage.BENGALI -> "টেলি-পরামর্শ ও উদ্ভিদ স্বাস্থ্য বিধি"
            AppLanguage.HINDI -> "टेली-परामर्श व पौध स्वास्थ्य दिशानिर्देश"
            AppLanguage.ENGLISH -> "Tele-Advisory & Plant Health Guidelines"
        },
        body = when (currentLanguage) {
            AppLanguage.BENGALI -> """
                ১. দূরবর্তী পরামর্শের সীমাবদ্ধতা:
                অডিও, ভিডিও বা চ্যাটে প্রদত্ত পরামর্শ কৃষকের প্রেরিত ছবি ও বিবরণের ওপর নির্ভরশীল। সরাসরি মাটিতে পরিদর্শনের বিকল্প এটি নয়।

                ২. জরুরি সংক্রমণ:
                আকস্মিক মহামারী রোগ বা পঙ্গপালের মতো সংকটে স্থানীয় কৃষি বিজ্ঞান কেন্দ্র (KVK) বা কৃষি দপ্তরে তাৎক্ষণিক নমুনা জমা দিন।

                ৩. প্রাতিষ্ঠানিক স্বচ্ছতা:
                অ্যাপে তালিকাভুক্ত বিজ্ঞানীগণ তাদের নিজস্ব প্রাতিষ্ঠানিক গবেষণা ও অভিজ্ঞতার ভিত্তিতে পরামর্শ প্রদান করেন।
            """.trimIndent()
            AppLanguage.HINDI -> """
                1. दूरस्थ परामर्श की सीमाएं:
                ऑडियो, वीडियो या चैट में दी जाने वाली सलाह किसान द्वारा भेजे गए विवरण व फ़ोटो पर आधारित होती है और प्रत्यक्ष खेत निरीक्षण का विकल्प नहीं है।

                2. आपातकालीन प्रकोप:
                किसी गंभीर महामारी या टिड्डी जैसे संकट में तुरंत स्थानीय कृषि विज्ञान केंद्र (KVK) या कृषि कार्यालय में नमूना जमा करें।

                3. संस्थागत स्पष्टता:
                सूचीबद्ध वैज्ञानिक अपने शोध व अनुभव के आधार पर तकनीकी मार्गदर्शन प्रदान करते हैं।
            """.trimIndent()
            AppLanguage.ENGLISH -> """
                1. Remote Consultation Scope:
                Advice provided via chat, audio, or video relies on farmer-submitted photos and descriptions and cannot substitute on-site soil or lab inspection.

                2. Severe Outbreaks:
                For sudden epidemics or pest swarms, immediately submit physical samples to your nearest Krishi Vigyan Kendra (KVK) or agriculture office.

                3. Advisory Independence:
                Listed agricultural scientists share guidance based on their research and field experience as informational support.
            """.trimIndent()
        }
    )
}

@Composable
private fun LegalCard(title: String, subtitle: String, body: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Text(text = subtitle, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AgriEarthSecondary)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = body,
                    modifier = Modifier.padding(14.dp),
                    color = AgriTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
