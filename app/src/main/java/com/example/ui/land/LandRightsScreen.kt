package com.example.ui.land

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.AppLanguage
import com.example.ui.theme.EarthCardBorder
import com.example.ui.theme.EarthInkPrimary
import com.example.ui.theme.EarthInkSecondary
import com.example.ui.theme.EarthSurfaceWhite
import com.example.ui.theme.EarthTrustBadgeBg
import com.example.ui.theme.EarthTrustBadgeText
import com.example.ui.theme.EarthWarmCream
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalWarningBg
import com.example.util.L10n
import com.example.util.safeStartActivity

enum class LandTopic(
    val titleBn: String,
    val titleHi: String,
    val titleEn: String
) {
    DOCUMENTS("জমির কাগজপত্র", "भूमि दस्तावेज", "Land Documents"),
    RECORDS("জমির রেকর্ড", "भूमि रिकॉर्ड", "Land Records"),
    DISPUTES("জমি বিরোধ", "भूमि विवाद", "Land Disputes"),
    BUY_SELL("ক্রয় ও বিক্রয়", "खरीद-बिक्री", "Buy & Sell"),
    POSSESSION_MUTATION("দখল ও মিউটেশন", "कब्जा व नामांतरण", "Possession & Mutation"),
    INHERITANCE("উত্তরাধিকার", "उत्तराधिकार", "Inheritance");

    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> titleBn
        AppLanguage.HINDI -> titleHi
        AppLanguage.ENGLISH -> titleEn
    }
}

private data class LandGuideCardData(
    val headingBn: String,
    val headingHi: String,
    val headingEn: String,
    val summaryBn: String,
    val summaryHi: String,
    val summaryEn: String,
    val pointsBn: List<String>,
    val pointsHi: List<String>,
    val pointsEn: List<String>,
    val officialUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandRightsScreen(
    currentLanguage: AppLanguage,
    initialTopic: LandTopic = LandTopic.DOCUMENTS,
    onBack: () -> Unit,
    onOpenExperts: () -> Unit = {}
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var selectedTopic by remember { mutableStateOf(initialTopic) }
    LaunchedEffect(initialTopic) {
        selectedTopic = initialTopic
    }

    val guides = remember(selectedTopic) { buildLandGuides(selectedTopic) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = RoyalGreenPrimaryBg,
        topBar = {
            Column {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = RoyalGreenSecondaryBg,
                        titleContentColor = RoyalTextPrimary
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("land_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = RoyalTextPrimary
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = L10n.t("land_screen_title", currentLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = L10n.t("land_screen_sub", currentLanguage),
                                style = MaterialTheme.typography.bodySmall,
                                color = RoyalSoftGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                )
                ScrollableTabRow(
                    selectedTabIndex = selectedTopic.ordinal,
                    containerColor = RoyalGreenSecondaryBg,
                    contentColor = RoyalSoftGold,
                    edgePadding = 12.dp
                ) {
                    LandTopic.entries.forEach { topic ->
                        Tab(
                            selected = selectedTopic == topic,
                            onClick = { selectedTopic = topic },
                            modifier = Modifier.testTag("land_tab_${topic.name}"),
                            text = {
                                Text(
                                    text = topic.getTitle(currentLanguage),
                                    fontWeight = if (selectedTopic == topic) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (selectedTopic == topic) RoyalSoftGold else Color.White.copy(alpha = 0.85f)
                                )
                            }
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
                .background(RoyalGreenPrimaryBg),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 780.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Legal Educational Disclaimer Banner
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = RoyalWarningBg),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = RoyalGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = L10n.t("land_disclaimer_title", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = RoyalSoftGold
                                )
                                Text(
                                    text = L10n.t("land_disclaimer_body", currentLanguage),
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = Color.White.copy(alpha = 0.92f)
                                )
                            }
                        }
                    }
                }

                items(guides) { guide ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = EarthSurfaceWhite),
                        border = BorderStroke(1.dp, EarthCardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = EarthTrustBadgeBg,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = when (selectedTopic) {
                                            LandTopic.DOCUMENTS -> Icons.Default.Description
                                            LandTopic.RECORDS -> Icons.Default.AccountBalance
                                            LandTopic.DISPUTES -> Icons.Default.Gavel
                                            else -> Icons.Default.Security
                                        },
                                        contentDescription = null,
                                        tint = EarthTrustBadgeText,
                                        modifier = Modifier.padding(8.dp).size(20.dp)
                                    )
                                }
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> guide.headingBn
                                        AppLanguage.HINDI -> guide.headingHi
                                        AppLanguage.ENGLISH -> guide.headingEn
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp,
                                    color = EarthInkPrimary
                                )
                            }

                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> guide.summaryBn
                                    AppLanguage.HINDI -> guide.summaryHi
                                    AppLanguage.ENGLISH -> guide.summaryEn
                                },
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = EarthInkSecondary
                            )

                            Surface(
                                color = EarthWarmCream,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, EarthCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val points = when (currentLanguage) {
                                        AppLanguage.BENGALI -> guide.pointsBn
                                        AppLanguage.HINDI -> guide.pointsHi
                                        AppLanguage.ENGLISH -> guide.pointsEn
                                    }
                                    points.forEach { pt ->
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = RoyalGreenEmerald,
                                                modifier = Modifier
                                                    .padding(top = 2.dp)
                                                    .size(16.dp)
                                            )
                                            Text(
                                                text = pt,
                                                fontSize = 13.sp,
                                                lineHeight = 19.sp,
                                                color = EarthInkPrimary,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            if (!guide.officialUrl.isNullOrBlank()) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(guide.officialUrl))
                                        safeStartActivity(
                                            context,
                                            intent,
                                            L10n.t("scheme_browser_error", currentLanguage)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RoyalGreenEmerald,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("land_official_portal_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = L10n.t("land_open_banglarbhumi", currentLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Connect with Advisor Card
                item {
                    OutlinedButton(
                        onClick = onOpenExperts,
                        border = BorderStroke(1.dp, RoyalGold),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = RoyalGreenSecondaryBg,
                            contentColor = RoyalSoftGold
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = RoyalGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = L10n.t("dash_expert_view_all", currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun buildLandGuides(topic: LandTopic): List<LandGuideCardData> = when (topic) {
    LandTopic.DOCUMENTS -> listOf(
        LandGuideCardData(
            headingBn = "কৃষিজমির আবশ্যিক কাগজপত্রের চেকলিস্ট",
            headingHi = "कृषि भूमि के आवश्यक दस्तावेजों की सूची",
            headingEn = "Essential Agricultural Land Document Checklist",
            summaryBn = "জমির মালিকানা ও সরকারি প্রকল্প (যেমন কৃষকবন্ধু বা শস্য বিমা) পেতে নিচের নথিগুলো গুছিয়ে রাখুন।",
            summaryHi = "भूमि स्वामित्व और सरकारी योजनाओं (जैसे पीएम-किसान या फसल बीमा) का लाभ लेने के लिए ये दस्तावेज सुरक्षित रखें।",
            summaryEn = "Keep these documents organized for land ownership proof and government farmer scheme enrollment.",
            pointsBn = listOf(
                "মূল রেজিস্ট্রিকৃত বিক্রয় বা দান দলিল (Original Sale/Gift Deed)",
                "সর্বশেষ সংশোধিত খতিয়ান / পর্চা (ROR - Record of Rights)",
                "হালনাগাদ ভূমি রাজস্ব বা খাজনা প্রদানের রসিদ",
                "মৌজা ম্যাপের দাগ নম্বর (Plot / Dag Number) এবং জমির শ্রেণি (শালি/সোনা/ডাঙ্গা)",
                "ভাগচাষি হলে বর্গা রেকর্ড বা স্বীকৃত শংসাপত্র"
            ),
            pointsHi = listOf(
                "मूल पंजीकृत बिक्री या दान विलेख (Sale Deed)",
                "अद्यतन खतियान / आरओआर (Record of Rights) की प्रति",
                "ताजा भूमि राजस्व या लगान भुगतान रसीद",
                "मौजा मानचित्र का खसरा / दाग नंबर एवं भूमि का प्रकार",
                "बंटाईदार किसानों के लिए मान्य बंटाई प्रमाणपत्र"
            ),
            pointsEn = listOf(
                "Original registered Sale Deed, Gift Deed, or Partition Deed",
                "Updated Khatian / Porcha (Record of Rights - ROR)",
                "Up-to-date annual Land Revenue (Khajna) tax receipt",
                "Plot / Dag number on the Mouza map and land classification",
                "Valid Barga / tenancy record if cultivating as a sharecropper"
            )
        )
    )
    LandTopic.RECORDS -> listOf(
        LandGuideCardData(
            headingBn = "অনলাইনে খতিয়ান ও দাগের তথ্য যাচাই",
            headingHi = "ऑनलाइन खतियान व प्लॉट रिकॉर्ड सत्यापन",
            headingEn = "Verify Khatian & Plot Records Online",
            summaryBn = "পশ্চিমবঙ্গের সরকারি বাংলারভূমি পোর্টালে জেলা, ব্লক ও মৌজা দিয়ে খতিয়ান ও দাগের প্রকৃত অবস্থা দেখা যায়।",
            summaryHi = "आधिकारिक भूमि पोर्टल पर जिला, ब्लॉक और मौजा चुनकर खतियान व प्लॉट का सही विवरण देखा जा सकता है।",
            summaryEn = "Check official Plot & Khatian records directly on the official state land portal before transactions.",
            pointsBn = listOf(
                "বাংলারভূমি পোর্টালে 'Citizen Services' থেকে খতিয়ান ও প্লট তথ্য নির্বাচন করুন।",
                "সঠিক জেলা, ব্লক ও মৌজার নাম বেছে দাগ বা খতিয়ান নম্বর লিখুন।",
                "মালিকের নাম, জমির পরিমাণ (একর/শতক) এবং শ্রেণি মিলিয়ে দেখুন।",
                "সার্টিফায়েড কপির (ROR) জন্য অফিশিয়াল পোর্টালে অনলাইনে আবেদন করা যায়।"
            ),
            pointsHi = listOf(
                "आधिकारिक भूमि पोर्टल के 'Citizen Services' अनुभाग में जाएं।",
                "सही जिला, ब्लॉक और मौजा चुनकर खतियान या प्लॉट संख्या दर्ज करें।",
                "मालिक का नाम, रकबा और भूमि का वर्ग ध्यान से मिलाएं।",
                "प्रमाणित प्रति (ROR) के लिए ऑनलाइन आवेदन किया जा सकता है।"
            ),
            pointsEn = listOf(
                "Select 'Know Your Property / Citizen Services' on the official land portal.",
                "Choose District, Block, and Mouza accurately before entering Khatian or Plot (Dag) number.",
                "Cross-check owner name, total area share, and agricultural land classification.",
                "Apply only through official government portals for certified ROR copies."
            ),
            officialUrl = "https://banglarbhumi.gov.in/"
        )
    )
    LandTopic.DISPUTES -> listOf(
        LandGuideCardData(
            headingBn = "সীমানা ও জমি সংক্রান্ত বিরোধের আইনি পথ",
            headingHi = "भूमि सीमा व विवाद समाधान के कानूनी उपाय",
            headingEn = "Resolving Boundary & Agricultural Land Disputes Safely",
            summaryBn = "জমির আল বা সীমানা নিয়ে বিবাদ হলে কখনোই আইন হাতে তুলে নেবেন না; সরকারি সার্ভে ও মধ্যস্থতা পথ অনুসরণ করুন।",
            summaryHi = "मेड़ या भूमि सीमा विवाद होने पर कानूनी व सरकारी सर्वेक्षण प्रक्रिया का ही पालन करें।",
            summaryEn = "Always resolve farm boundary or title disputes through formal survey and legal channels.",
            pointsBn = listOf(
                "BL&LRO অফিসে সরকারি আমিন দ্বারা জমি মাপার (Demarcation) লিখিত আবেদন করুন।",
                "গ্রাম পঞ্চায়েত বা লোক আদালত / আইনি পরিষেবা কর্তৃপক্ষের (DLSA) মাধ্যমে বিনামূল্যে আইনি পরামর্শ নেওয়া যায়।",
                "সমস্ত দলিল, খাজনা রসিদ ও খতিয়ানের ফটোকপি নিরাপদে সংরক্ষণ করুন।",
                "উপস্থিত সাক্ষী ও প্রতিবেশী চাষিদের সম্মতিতে মাপজোখ করানো ভালো।"
            ),
            pointsHi = listOf(
                "राजस्व अधिकारी (BL&LRO / तहसील) के पास सरकारी सीमांकन आवेदन दें।",
                "जिला विधिक सेवा प्राधिकरण (DLSA) या लोक अदालत से निःशुल्क कानूनी सहायता प्राप्त करें।",
                "सभी दस्तावेजों, लगान रसीदों व पुराने रिकॉर्ड की प्रति सुरक्षित रखें।",
                "पड़ोसी किसानों और ग्राम प्रतिनिधियों की उपस्थिति में मापी कराएं।"
            ),
            pointsEn = listOf(
                "Submit a formal application at the local BL&LRO / Tehsil office for official land demarcation.",
                "Seek free legal aid through District Legal Services Authority (DLSA) or Lok Adalat.",
                "Keep photocopies of deeds, revenue receipts, and historical khatian extracts organized.",
                "Document boundary measurements transparently in the presence of authorized surveyors."
            )
        )
    )
    LandTopic.BUY_SELL -> listOf(
        LandGuideCardData(
            headingBn = "নিরাপদ জমি ক্রয়-বিক্রয় সুরক্ষা গাইড",
            headingHi = "सुरक्षित भूमि खरीद-बिक्री सुरक्षा गाइड",
            headingEn = "Safe Land Buy & Sell Verification Checklist",
            summaryBn = "কোনো জমি কেনার আগে অন্তত ১৫–৩০ বছরের চেইন দলিল এবং দায়মুক্তি (Non-Encumbrance) যাচাই করুন।",
            summaryHi = "जमीन खरीदने से पहले पिछले 15–30 वर्षों की चेन डीड और ऋण भार (Encumbrance) की जांच अवश्य करें।",
            summaryEn = "Before buying or paying any advance, verify ownership history and encumbrance records.",
            pointsBn = listOf(
                "বিক্রেতার নামে হালনাগাদ মিউটেশন (পর্চা) এবং খাজনা রসিদ আছে কি না দেখুন।",
                "সাব-রেজিস্ট্রি অফিসে সার্চিং (NEC) করে নিশ্চিত হন যে জমিতে কোনো ব্যাংক লোন বা মামলা নেই।",
                "সরেজমিনে জমির দখল, রাস্তা এবং জলনিকাশি ব্যবস্থা নিজে দেখে নিন।",
                "অগ্রিম বা বায়না অর্থ সর্বদা ব্যাংক বা লিখিত চুক্তির মাধ্যমে আদান-প্রদান করুন।"
            ),
            pointsHi = listOf(
                "जांचें कि विक्रेता के नाम अद्यतन खतियान और ताजा लगान रसीद है या नहीं।",
                "सब-रजिस्ट्रार कार्यालय से Non-Encumbrance Certificate (NEC) की जांच कराएं कि भूमि पर कोई कर्ज या केस तो नहीं है।",
                "खेत पर जाकर वास्तविक कब्जा, रास्ता और सिंचाई सुविधा स्वयं जांचें।",
                "अग्रिम भुगतान हमेशा बैंक माध्यम और लिखित एग्रीमेंट के साथ ही करें।"
            ),
            pointsEn = listOf(
                "Verify the seller's name appears on the updated Khatian and latest land tax receipt.",
                "Obtain a Non-Encumbrance Certificate (NEC) search from the sub-registrar office to rule out mortgages or court stays.",
                "Physically inspect plot possession, access paths, and irrigation drainage before signing.",
                "Make advance or final payments strictly via traceable bank transfers with registered receipts."
            )
        )
    )
    LandTopic.POSSESSION_MUTATION -> listOf(
        LandGuideCardData(
            headingBn = "দখল ও মিউটেশন (নামজারি) প্রক্রিয়া",
            headingHi = "कब्जा व नामांतरण (म्यूटेशन) प्रक्रिया",
            headingEn = "Possession & Land Mutation (Namjari) Guide",
            summaryBn = "জমি রেজিস্ট্রি বা উত্তরাধিকার সূত্রে পাওয়ার পর সরকারি রেকর্ডে নিজের নাম তুলতে মিউটেশন করা বাধ্যতামূলক।",
            summaryHi = "भूमि रजिस्ट्री या विरासत के बाद सरकारी खतियान में नाम दर्ज कराने के लिए म्यूटेशन (दाखिल-खारिज) अनिवार्य है।",
            summaryEn = "After registration or inheritance, complete Mutation so your name reflects in official land records.",
            pointsBn = listOf(
                "রেজিস্ট্রি দলিল, পূর্বের পর্চা, চেইন দলিল ও খাজনা রসিদ নিয়ে মিউটেশন আবেদন করুন।",
                "বাংলারভূমি পোর্টাল বা স্থানীয় BL&LRO অফিসে শুনানির তারিখে মূল নথি দেখাতে হয়।",
                "মিউটেশন অনুমোদিত হলে নতুন খতিয়ান নম্বর পাবেন এবং নিয়মিত খাজনা প্রদান করতে পারবেন।"
            ),
            pointsHi = listOf(
                "रजिस्ट्री विलेख, पिछला खतियान और लगान रसीद के साथ नामांतरण आवेदन करें।",
                "राजस्व कार्यालय में सुनवाई की तिथि पर मूल दस्तावेज प्रस्तुत करें।",
                "नामांतरण स्वीकृत होने के बाद नया खतियान नंबर प्राप्त करें व नियमित लगान जमा करें।"
            ),
            pointsEn = listOf(
                "Apply for mutation with registered sale deed, prior ROR, link deeds, and fee receipt.",
                "Attend the revenue officer hearing with original documents when notified.",
                "Once approved, download your updated Khatian and continue paying annual revenue tax."
            ),
            officialUrl = "https://banglarbhumi.gov.in/"
        )
    )
    LandTopic.INHERITANCE -> listOf(
        LandGuideCardData(
            headingBn = "উত্তরাধিকার ও ওয়ারিশন সনদ নির্দেশিকা",
            headingHi = "उत्तराधिकार व वारिसान प्रमाणपत्र गाइड",
            headingEn = "Inheritance & Warisan (Legal Heir) Guidance",
            summaryBn = "পৈতৃক জমির মালিকানা সন্তান বা আইনগত উত্তরাধিকারীদের নামে সঠিকভাবে ভাগ ও রেকর্ড করার নিয়ম।",
            summaryHi = "पैतृक कृषि भूमि को वैध उत्तराधिकारियों के नाम दर्ज व विभाजित करने की सरल प्रक्रिया।",
            summaryEn = "Steps to record inherited agricultural land properly in the names of legal heirs.",
            pointsBn = listOf(
                "প্রথমে মৃত ব্যক্তির মৃত্যু সনদ (Death Certificate) সংগ্রহ করুন।",
                "পঞ্চায়েত বা পুরসভা থেকে বৈধ ওয়ারিশন (Legal Heir) সার্টিফিকেট নিন।",
                "সকল উত্তরাধিকারীর সম্মতিতে ওয়ারিশ মিউটেশন বা রেজিস্ট্রিকৃত বণ্টননামা (Partition Deed) করা যায়।",
                "সঠিক রেকর্ডের ফলে সকল অংশীদার আলাদাভাবে সরকারি প্রকল্পের সুবিধা পেতে পারেন।"
            ),
            pointsHi = listOf(
                "सबसे पहले भू-स्वामी का मृत्यु प्रमाणपत्र प्राप्त करें।",
                "ग्राम पंचायत या सक्षम प्राधिकरण से वैध वारिसान (उत्तराधिकारी) प्रमाणपत्र बनवाएं।",
                "सभी वारिसों की सहमति से विरासत नामांतरण या बंटवारा विलेख (Partition Deed) कराएं।",
                "सही रिकॉर्ड होने से प्रत्येक किसान अलग-अलग सरकारी योजनाओं का लाभ ले सकता है।"
            ),
            pointsEn = listOf(
                "First obtain the official Death Certificate of the deceased landowner.",
                "Obtain a Legal Heir (Warisan) Certificate from the competent local authority / Panchayat.",
                "File for inheritance mutation or execute a registered Partition Deed with mutual consent of heirs.",
                "Updated individual khatians help each farmer access crop insurance and financial schemes."
            )
        )
    )
}
