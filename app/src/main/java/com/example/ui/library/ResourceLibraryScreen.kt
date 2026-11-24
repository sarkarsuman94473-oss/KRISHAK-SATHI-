package com.example.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage

data class ResourceTopic(
    val id: String,
    val titleBn: String,
    val titleHi: String,
    val titleEn: String,
    val descriptionBn: String,
    val descriptionHi: String,
    val descriptionEn: String,
    val icon: ImageVector,
    val categoryBadgeBn: String,
    val categoryBadgeHi: String,
    val categoryBadgeEn: String,
    val targetHashRoute: String
) {
    fun getBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> categoryBadgeBn
        AppLanguage.HINDI -> categoryBadgeHi
        AppLanguage.ENGLISH -> categoryBadgeEn
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceLibraryScreen(
    currentLanguage: AppLanguage,
    onNavigateRoute: (String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val topics = remember {
        listOf(
            ResourceTopic(
                id = "crop_database",
                titleBn = "🌾 ফসল ও চাষ পদ্ধতি এনসাইক্লোপিডিয়া",
                titleHi = "🌾 फसल एवं कृषि पद्धति ज्ञानकोष",
                titleEn = "🌾 Crop Cultivation Encyclopedia",
                descriptionBn = "আলু, ধান, গম, শাকসবজি ও ফলমূলের বীজ থেকে ফসল কাটার ১০টি পূর্ণাঙ্গ ধাপ।",
                descriptionHi = "आलू, धान, गेहूं, सब्जियां और फलों की बुवाई से कटाई तक संपूर्ण जानकारी।",
                descriptionEn = "Complete 10-stage cultivation guides for vegetables, paddy, wheat, fruits, and millets.",
                icon = Icons.Default.Agriculture,
                categoryBadgeBn = "ফসল তথ্য",
                categoryBadgeHi = "फसल जानकारी",
                categoryBadgeEn = "Crop Info",
                targetHashRoute = "#my-crops"
            ),
            ResourceTopic(
                id = "medicine_hub",
                titleBn = "💊 সরকারি নির্দেশিকায় পরীক্ষিত ওষুধ ও কীটনাশক",
                titleHi = "💊 सरकारी अनुमोदित फसल सुरक्षा व दवाइयां",
                titleEn = "💊 Verified Crop Medicines & IPM Guide",
                descriptionBn = "CIBRC সার্টিফায়েড সক্রিয় উপাদান, সরকারি লেবেল ডোজ, পিএইচআই ও প্রতিরোধ গ্রুপ।",
                descriptionHi = "सीआईबीआरसी प्रमाणित सक्रिय तत्व, आधिकारिक खुराक और प्रतीक्षा अवधि।",
                descriptionEn = "Verified CIBRC active ingredients, label dosages, PHI days, and IPM alternatives.",
                icon = Icons.Default.LocalHospital,
                categoryBadgeBn = "সুরক্ষা ও চিকিৎসা",
                categoryBadgeHi = "सुरक्षा व उपचार",
                categoryBadgeEn = "Crop Protection",
                targetHashRoute = "#medicine-hub"
            ),
            ResourceTopic(
                id = "nursery_market",
                titleBn = "🌱 নার্সারি ও সার্টিফায়েড বীজ বাজার",
                titleHi = "🌱 नर्सरी एवं प्रमाणित बीज बाजार",
                titleEn = "🌱 Nursery & Certified Seed Market",
                descriptionBn = "শিলিগুড়ি ও উত্তরবঙ্গের বিশ্বস্ত নার্সারির চারা, কলম ও উন্নত হাইব্রিড বীজের সংগ্রহ।",
                descriptionHi = "सिलीगुड़ी और उत्तर बंगाल की सत्यापित नर्सरी, फलदार पौधे एवं संकर बीज।",
                descriptionEn = "Verified nursery directories, fruit saplings, and hybrid seed dealers with direct contact.",
                icon = Icons.Default.LocalFlorist,
                categoryBadgeBn = "বীজ ও চারা",
                categoryBadgeHi = "बीज व पौधे",
                categoryBadgeEn = "Seeds & Saplings",
                targetHashRoute = "#nursery-market"
            ),
            ResourceTopic(
                id = "plant_kingdom",
                titleBn = "🌺 উদ্ভিদ ও ফুল ভাণ্ডার (হিমালয়, ইনডোর ও ফল)",
                titleHi = "🌺 पौधा एवं पुष्प विश्वकोश (124+ प्रजातियाँ)",
                titleEn = "🌺 Plant & Flower Kingdom Encyclopedia (124+)",
                descriptionBn = "দার্জিলিং-হিমালয় অর্কিড, মানি প্ল্যান্ট ও ইনডোর, ফল গাছ, শোভাময় বৃক্ষ ও ভেষজ গাইড।",
                descriptionHi = "दार्जिलिंग ऑर्किड, मनी प्लांट, इंडोर पौधे, फलदार पेड़, सजावटी वृक्ष और जड़ी-बूटी गाइड।",
                descriptionEn = "Himalayan hill flowers, indoor money plants, fruit saplings, ornamental trees, herbs & succulents.",
                icon = Icons.Default.LocalFlorist,
                categoryBadgeBn = "উদ্ভিদ জগৎ",
                categoryBadgeHi = "वनस्पति कोष",
                categoryBadgeEn = "Plant Kingdom",
                targetHashRoute = "#plant-kingdom"
            ),
            ResourceTopic(
                id = "nursery_directory",
                titleBn = "🏪 শিলিগুড়ি ও ২২ জেলার নার্সারি ডিরেক্টরি ও ম্যাপ",
                titleHi = "🏪 सिलीगुड़ी व 22 ज़िला नर्सरी निर्देशिका व मैप",
                titleEn = "🏪 Siliguri & 22-District WB Nursery Directory",
                descriptionBn = "শিলিগুড়ি, দার্জিলিং, জলপাইগুড়ি, কলকাতা সহ পশ্চিমবঙ্গের সব জেলার নার্সারি ও যাচাই স্ট্যাটাস।",
                descriptionHi = "सिलीगुड़ी, दार्जिलिंग, जलपाईगुड़ी, कोलकाता सहित 22 ज़िलों की नर्सरियाँ व मानचित्र।",
                descriptionEn = "District-wise West Bengal nursery directory & interactive map with transparent verification status.",
                icon = Icons.Default.LocalFlorist,
                categoryBadgeBn = "নার্সারি ডিরেক্টরি",
                categoryBadgeHi = "नर्सरी निर्देशिका",
                categoryBadgeEn = "22 Districts",
                targetHashRoute = "#nursery-directory"
            ),
            ResourceTopic(
                id = "machinery_hub",
                titleBn = "🚜 কৃষি যন্ত্রপাতি, ভাড়া ও খামার সেবা হাব",
                titleHi = "🚜 कृषि मशीनरी, किराया व कृषि सेवा हब",
                titleEn = "🚜 Agricultural Machinery, Rental & Services Hub",
                descriptionBn = "শিলিগুড়ি ও উত্তরবঙ্গের ১৬+ ডিলার ডিরেক্টরি, ট্রাক্টর/টিলার/পাম্প/ড্রোন ও ১৪টি খামার সেবা।",
                descriptionHi = "सिलीगुड़ी व उत्तर बंगाल के 16+ डीलर, ट्रैक्टर/पावर टिलर/पंप/ड्रोन एवं 14 कृषि सेवाएं।",
                descriptionEn = "Buy/rent farm equipment, compare specs, find Siliguri & North Bengal dealers & book 14 services.",
                icon = Icons.Default.Build,
                categoryBadgeBn = "যন্ত্র ও সেবা",
                categoryBadgeHi = "मशीनरी व सेवा",
                categoryBadgeEn = "Machinery",
                targetHashRoute = "#machinery-hub"
            ),
            ResourceTopic(
                id = "plant_problem",
                titleBn = "🔬 রোগবালাই ও পাতা-ফলের সমস্যা নির্ণয়",
                titleHi = "🔬 पादप रोग एवं कीट पहचान",
                titleEn = "🔬 Plant Problem & Symptom Checker",
                descriptionBn = "পাতা কোঁকড়ানো, হলুদ দাগ, ডগা ও ফল পচা রোগের লক্ষণ বিশ্লেষণ ও সমাধান।",
                descriptionHi = "पत्ती मुड़ना, पीले धब्बे, फल सड़न रोग के लक्षण और प्राथमिक जैविक उपचार।",
                descriptionEn = "Identify leaf curling, yellow spots, fruit borers, and disease symptoms with expert advice.",
                icon = Icons.Default.Search,
                categoryBadgeBn = "রোগ নির্ণয়",
                categoryBadgeHi = "रोग निदान",
                categoryBadgeEn = "Diagnosis",
                targetHashRoute = "#plant-problem"
            ),
            ResourceTopic(
                id = "tea_garden",
                titleBn = "🍵 উত্তরবঙ্গ চা বাগান পরিচর্যা গাইড",
                titleHi = "🍵 उत्तर बंगाल चाय बागान प्रबंधन",
                titleEn = "🍵 Tea Garden Cultivation & Care",
                descriptionBn = "টি বোর্ড অনুমোদিত স্প্রে সূচি, লাল মাকড় ও লুপার পোকার বৈজ্ঞানিক নিয়ন্ত্রণ।",
                descriptionHi = "टी बोर्ड अनुमोदित छिड़काव सूची, लाल मकड़ी एवं कीट नियंत्रण।",
                descriptionEn = "Tea Board approved PPC guidelines, red spider mite, loopers, and plucking cycles.",
                icon = Icons.Default.Spa,
                categoryBadgeBn = "বাগান ফসল",
                categoryBadgeHi = "बागान फसल",
                categoryBadgeEn = "Plantation",
                targetHashRoute = "#tea-garden"
            ),
            ResourceTopic(
                id = "dosage_calculator",
                titleBn = "🧪 বিঘা ও কাঠা ভিত্তিক সার-ওষুধ ক্যালকুলেটর",
                titleHi = "🧪 बीघा एवं एकड़ आधारित खुराक कैलकुलेटर",
                titleEn = "🧪 Land Dose & Water Requirement Calculator",
                descriptionBn = "১ বিঘা, ১ কাঠা বা ডেসিমেল জমির জন্য সঠিক মিলি/গ্রাম ও জলের নিখুঁত হিসাব।",
                descriptionHi = "एक बीघा, कट्ठा या एकड़ भूमि के लिए सटीक मिली/ग्राम और पानी की मात्रा।",
                descriptionEn = "Exact chemical and water requirement calculated strictly from CIBRC per-hectare rates.",
                icon = Icons.Default.Calculate,
                categoryBadgeBn = "জমি হিসাব",
                categoryBadgeHi = "भूमि गणना",
                categoryBadgeEn = "Land Calculator",
                targetHashRoute = "#dosage-calculator"
            ),
            ResourceTopic(
                id = "govt_schemes",
                titleBn = "📜 কৃষকবন্ধু ও সরকারি কৃষি সহায়তা প্রকল্প",
                titleHi = "📜 सरकारी कृषि योजनाएं एवं सहायता",
                titleEn = "📜 Government Farming Schemes & Subsidies",
                descriptionBn = "কৃষকবন্ধু অনুদান, পিএম কিষাণ, বাংলা শস্য বিমা এবং কৃষি যন্ত্রাংশ ভর্তুকির তথ্য।",
                descriptionHi = "कृषक बंधु, पीएम किसान, फसल बीमा और कृषि उपकरण अनुदान की विस्तृत जानकारी।",
                descriptionEn = "Krishak Bandhu, PM-Kisan, Bangla Shasya Bima, and agricultural subsidy guidelines.",
                icon = Icons.Default.Shield,
                categoryBadgeBn = "সরকারি সুবিধা",
                categoryBadgeHi = "सरकारी योजनाएं",
                categoryBadgeEn = "Govt Schemes",
                targetHashRoute = "#govt-schemes"
            ),
            ResourceTopic(
                id = "market_mandi",
                titleBn = "💰 পাইকারি মাণ্ডি ও দৈনিক বাজার দর",
                titleHi = "💰 दैनिक मंडी भाव एवं बाजार दर",
                titleEn = "💰 Wholesale Mandi Prices & Trends",
                descriptionBn = "শিলিগুড়ি, জলপাইগুড়ি, মালদা ও পার্শ্ববর্তী প্রধান মাণ্ডির রিয়েল-টাইম দর।",
                descriptionHi = "सिलीगुड़ी, जलपाईगुड़ी, मालदा मंडियों के वर्तमान दैनिक थोक एवं खुदरा भाव।",
                descriptionEn = "Compare daily wholesale rates and price trends across major regional markets.",
                icon = Icons.Default.MonetizationOn,
                categoryBadgeBn = "বাজার দর",
                categoryBadgeHi = "मंडी भाव",
                categoryBadgeEn = "Market Prices",
                targetHashRoute = "#market-price"
            ),
            ResourceTopic(
                id = "community_forum",
                titleBn = "💬 কৃষক কমিউনিটি ফোরাম ও আলোচনা",
                titleHi = "💬 किसान सामुदायिक मंच एवं चर्चा",
                titleEn = "💬 Farmer Community Forum & Q&A",
                descriptionBn = "ফসল ও বিষয় অনুযায়ী অভিজ্ঞ কৃষকদের সাথে প্রশ্নোত্তর ও চাষের টিপস শেয়ার করুন।",
                descriptionHi = "फसल और विषय के अनुसार साथी किसानों के साथ सवाल-जवाब और खेती टिप्स साझा करें।",
                descriptionEn = "Discuss crop issues, share tips by crop or topic, and collaborate with peer farmers.",
                icon = Icons.Default.Forum,
                categoryBadgeBn = "কমিউনিটি",
                categoryBadgeHi = "समुदाय",
                categoryBadgeEn = "Community",
                targetHashRoute = "#community-forum"
            ),
            ResourceTopic(
                id = "discovery_hub",
                titleBn = "🌐 পাবলিক কৃষি জ্ঞান ভাণ্ডার ও ডিসকভারি হাব",
                titleHi = "🌐 सार्वजनिक कृषि ज्ञान व डिस्कवरी हब",
                titleEn = "🌐 Public Knowledge Hub & SEO Guides",
                descriptionBn = "যাচাইকৃত ICAR/BCKV/UBKV চাষ গাইড, ফসল SEO পৃষ্ঠা ও ব্র্যান্ড পরিচিতি।",
                descriptionHi = "प्रमाणित ICAR/BCKV/UBKV खेती गाइड, फसल SEO पृष्ठ व ब्रांड जानकारी।",
                descriptionEn = "Multilingual ICAR/BCKV/UBKV cultivation guides, crop SEO pages & transparent roadmap.",
                icon = Icons.Default.Agriculture,
                categoryBadgeBn = "পাবলিক হাব",
                categoryBadgeHi = "सार्वजनिक हब",
                categoryBadgeEn = "Public Hub",
                targetHashRoute = "#discovery-hub"
            )
        )
    }

    val filteredTopics = remember(searchQuery, topics) {
        if (searchQuery.isBlank()) topics
        else topics.filter {
            it.titleBn.contains(searchQuery, ignoreCase = true) ||
            it.titleHi.contains(searchQuery, ignoreCase = true) ||
            it.titleEn.contains(searchQuery, ignoreCase = true) ||
            it.descriptionBn.contains(searchQuery, ignoreCase = true) ||
            it.descriptionHi.contains(searchQuery, ignoreCase = true) ||
            it.descriptionEn.contains(searchQuery, ignoreCase = true) ||
            it.categoryBadgeBn.contains(searchQuery, ignoreCase = true) ||
            it.categoryBadgeHi.contains(searchQuery, ignoreCase = true) ||
            it.categoryBadgeEn.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Column {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "📚 কৃষি রিসোর্স লাইব্রেরি"
                                AppLanguage.HINDI -> "📚 कृषि संसाधन पुस्तकालय"
                                AppLanguage.ENGLISH -> "📚 Agricultural Resource Library"
                            },
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "প্রয়োজনীয় কৃষি তথ্য, গাইড ও নির্দেশিকা"
                                AppLanguage.HINDI -> "महत्वपूर्ण कृषि दिशा-निर्देश व संसाधन"
                                AppLanguage.ENGLISH -> "Comprehensive Agricultural Guides & Resources"
                            },
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("resource_search_input"),
                placeholder = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "রিসোর্স লাইব্রেরি বা বিষয় খুঁজুন (উদাঃ ওষুধ, ধান, সার)..."
                            AppLanguage.HINDI -> "संसाधन खोजें (जैसे: दवा, धान, खाद)..."
                            AppLanguage.ENGLISH -> "Search resources (e.g. medicine, paddy, fertilizer)..."
                        },
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Topic List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTopics, key = { it.id }) { topic ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateRoute(topic.targetHashRoute) }
                            .testTag("resource_topic_${topic.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = topic.icon,
                                    contentDescription = topic.getBadge(currentLanguage),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = topic.getBadge(currentLanguage),
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> topic.titleBn
                                        AppLanguage.HINDI -> topic.titleHi
                                        AppLanguage.ENGLISH -> topic.titleEn
                                    },
                                    fontSize = 15.sp,
                                    lineHeight = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> topic.descriptionBn
                                        AppLanguage.HINDI -> topic.descriptionHi
                                        AppLanguage.ENGLISH -> topic.descriptionEn
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 17.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
