package com.example.ui.info

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entity.SeoArticleEntity
import com.example.data.model.AppLanguage
import com.example.data.model.FeatureAvailabilityStatus
import com.example.data.model.SeoContentCategory
import com.example.data.repository.SeoDiscoveryRepository
import com.example.ui.navigation.HashRoutes
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenHighlight
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.royalOutlinedTextFieldColors
import com.example.util.safeStartActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicDiscoveryHubScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onNavigateRoute: (String) -> Unit = {},
    onOpenCropDetailById: (String) -> Unit = {},
    initialTab: Int = 0
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember(context) { SeoDiscoveryRepository(context) }

    LaunchedEffect(Unit) {
        repository.ensureSeeded()
        repository.recordReferral("in_app", "discovery-hub", currentLanguage)
    }

    val publishedArticles by repository.observePublishedArticles().collectAsStateWithLifecycle(initialValue = emptyList())
    val seoConfig by repository.observeConfig().collectAsStateWithLifecycle(initialValue = repository.defaultConfig())

    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<SeoContentCategory?>(null) }
    var expandedArticleId by remember { mutableStateOf<String?>(null) }

    val openUrl: (String, String) -> Unit = { rawUrl, slug ->
        scope.launch { repository.recordReferral("web_outbound", slug, currentLanguage) }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawUrl))
        safeStartActivity(
            context,
            intent,
            when (currentLanguage) {
                AppLanguage.BENGALI -> "লিংক খোলার মতো ব্রাউজার পাওয়া যায়নি"
                AppLanguage.HINDI -> "लिंक खोलने के लिए ब्राउज़र नहीं मिला"
                AppLanguage.ENGLISH -> "No browser app found to open link"
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Column {
                Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(48.dp).testTag("discovery_hub_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.ic_krishak_sathi_logo),
                            contentDescription = "KRISHAK SATHI Logo",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "পাবলিক কৃষি জ্ঞান ও ডিসকভারি হাব"
                                    AppLanguage.HINDI -> "सार्वजनिक कृषि ज्ञान व डिस्कवरी हब"
                                    AppLanguage.ENGLISH -> "Public Knowledge & Discovery Hub"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = SeoDiscoveryRepository.TAGLINE,
                                style = MaterialTheme.typography.bodySmall,
                                color = RoyalSoftGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = RoyalGreenSecondaryBg,
                    contentColor = RoyalSoftGold
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.testTag("discovery_tab_knowledge"),
                        text = {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "জ্ঞান ভাণ্ডার"
                                    AppLanguage.HINDI -> "ज्ञान केंद्र"
                                    AppLanguage.ENGLISH -> "Knowledge Hub"
                                },
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.testTag("discovery_tab_crops"),
                        text = {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ফসল SEO গাইড"
                                    AppLanguage.HINDI -> "फसल गाइड"
                                    AppLanguage.ENGLISH -> "Crop Guides"
                                },
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        icon = { Icon(Icons.Default.Agriculture, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.testTag("discovery_tab_brand"),
                        text = {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ব্র্যান্ড ও রোডম্যাপ"
                                    AppLanguage.HINDI -> "ब्रांड व रोडमैप"
                                    AppLanguage.ENGLISH -> "Brand & Status"
                                },
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        icon = { Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp)) }
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
            when (selectedTab) {
                0 -> {
                    val filtered = remember(publishedArticles, searchQuery, selectedCategory) {
                        publishedArticles.filter { art ->
                            val matchesCat = selectedCategory == null || art.categoryEnum() == selectedCategory
                            val q = searchQuery.trim()
                            val matchesText = q.isBlank() ||
                                art.titleEn.contains(q, ignoreCase = true) ||
                                art.titleBn.contains(q, ignoreCase = true) ||
                                art.titleHi.contains(q, ignoreCase = true) ||
                                art.summaryEn.contains(q, ignoreCase = true) ||
                                art.summaryBn.contains(q, ignoreCase = true) ||
                                art.summaryHi.contains(q, ignoreCase = true) ||
                                art.sourceName.contains(q, ignoreCase = true)
                            matchesCat && matchesText
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth().testTag("discovery_search_input"),
                                placeholder = {
                                    Text(
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "ধান, আলু, চা, মাটি বা সরকারি প্রকল্প খুঁজুন..."
                                            AppLanguage.HINDI -> "धान, आलू, चाय, मिट्टी या सरकारी योजना खोजें..."
                                            AppLanguage.ENGLISH -> "Search rice, potato, tea, soil, IPM, or schemes..."
                                        }
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = RoyalSoftGold) },
                                colors = royalOutlinedTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { selectedCategory = null },
                                    label = {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "সব বিষয়"
                                                AppLanguage.HINDI -> "सभी विषय"
                                                AppLanguage.ENGLISH -> "All Topics"
                                            }
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AgriGreenPrimary,
                                        selectedLabelColor = RoyalSoftGold
                                    )
                                )
                                SeoContentCategory.entries.forEach { cat ->
                                    FilterChip(
                                        selected = selectedCategory == cat,
                                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                                        label = { Text(cat.label(currentLanguage), fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AgriGreenPrimary,
                                            selectedLabelColor = RoyalSoftGold
                                        )
                                    )
                                }
                            }
                        }
                        items(filtered, key = { it.id }) { article ->
                            DiscoveryArticleCard(
                                article = article,
                                currentLanguage = currentLanguage,
                                expanded = expandedArticleId == article.id,
                                onToggleExpand = {
                                    expandedArticleId = if (expandedArticleId == article.id) null else article.id
                                    scope.launch {
                                        repository.recordReferral("in_app_read", article.slug, currentLanguage)
                                    }
                                },
                                onOpenWeb = { openUrl(article.canonicalUrl, article.slug) },
                                onOpenSource = { openUrl(article.sourceUrl, article.slug) },
                                onOpenCrop = {
                                    if (article.cropId.isNotBlank()) {
                                        onOpenCropDetailById(article.cropId)
                                    }
                                }
                            )
                        }
                    }
                }

                1 -> {
                    val cropArticles = remember(publishedArticles) {
                        publishedArticles.filter { it.cropId.isNotBlank() }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = RoyalGreenSecondaryBg),
                                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "পাবলিক ও সার্চ-রেডি ফসল গাইড"
                                            AppLanguage.HINDI -> "सार्वजनिक एवं खोज-अनुकूल फसल गाइड"
                                            AppLanguage.ENGLISH -> "Verified Crop Cultivation & IPM Pages"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalSoftGold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "প্রতিটি ফসল গাইড ICAR / BCKV / UBKV / TRA তথ্যসূত্র এবং বাংলা-হিন্দি-ইংরেজি ওয়েব লিংকসহ প্রকাশিত।"
                                            AppLanguage.HINDI -> "प्रत्येक फसल गाइड ICAR / BCKV / UBKV / TRA संदर्भों व हिंदी-बांग्ला-अंग्रेजी वेब लिंक के साथ उपलब्ध है।"
                                            AppLanguage.ENGLISH -> "Each crop guide pairs field-verified cultivation & pest ID with ICAR/BCKV/UBKV/TRA citations and multilingual web links."
                                        },
                                        fontSize = 12.sp,
                                        color = RoyalTextSecondary
                                    )
                                }
                            }
                        }
                        items(cropArticles, key = { it.id }) { cropArt ->
                            CropSeoGuideCard(
                                article = cropArt,
                                currentLanguage = currentLanguage,
                                onOpenInAppCrop = { onOpenCropDetailById(cropArt.cropId) },
                                onOpenWebGuide = { openUrl(cropArt.canonicalUrl, cropArt.slug) },
                                onOpenPlantProblem = { onNavigateRoute(HashRoutes.PLANT_PROBLEM) }
                            )
                        }
                    }
                }

                else -> {
                    val capabilities = remember { repository.brandCapabilities() }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.65f)),
                                modifier = Modifier.fillMaxWidth().testTag("discovery_brand_hero_card")
                            ) {
                                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_krishak_sathi_logo),
                                            contentDescription = "KRISHAK SATHI Smart Farming Circular Emblem",
                                            modifier = Modifier.size(58.dp).clip(CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "KRISHAK SATHI",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 19.sp,
                                                color = RoyalTextPrimary
                                            )
                                            Text(
                                                text = SeoDiscoveryRepository.TAGLINE,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = RoyalSoftGold
                                            )
                                        }
                                    }

                                    Surface(
                                        color = RoyalGold.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(999.dp),
                                        border = BorderStroke(1.dp, RoyalGold)
                                    ) {
                                        Text(
                                            text = "Google Play Store: ${seoConfig.playStoreStatus}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalSoftGold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                        )
                                    }

                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "ভারতীয় কৃষক, শিলিগুড়ি ও উত্তরবঙ্গের নার্সারি এবং কৃষি বিজ্ঞানীদের ডিজিটাল মঞ্চ। কোনো অবাস্তব দাবি নয় — নিচে চালু এবং উন্নয়নাধীন সুবিধার স্বচ্ছ তালিকা দেওয়া হলো।"
                                            AppLanguage.HINDI -> "भारतीय किसानों, सिलीगुड़ी व उत्तर बंगाल नर्सरियों और कृषि विशेषज्ञों का विश्वसनीय मंच। नीचे लाइव और निर्माणाधीन सुविधाओं की पारदर्शी सूची देखें।"
                                            AppLanguage.ENGLISH -> "A transparent Indian agriculture technology ecosystem built for smallholder growers, Siliguri/North Bengal nurseries, and agronomists. Inspect live vs upcoming capabilities below."
                                        },
                                        fontSize = 13.sp,
                                        color = RoyalTextSecondary
                                    )

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { openUrl(seoConfig.baseWebUrl, "brand-live-web") },
                                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                                            modifier = Modifier.heightIn(min = 48.dp).testTag("discovery_open_live_web_btn")
                                        ) {
                                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "পাবলিক ওয়েব হাব খুলুন"
                                                    AppLanguage.HINDI -> "सार्वजनिक वेब हब खोलें"
                                                    AppLanguage.ENGLISH -> "Open Public Web Hub"
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { onNavigateRoute(HashRoutes.NURSERY_REGISTRATION) },
                                            border = BorderStroke(1.dp, RoyalGold),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalSoftGold),
                                            modifier = Modifier.heightIn(min = 48.dp)
                                        ) {
                                            Text(
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "নার্সারি অংশীদার নিবন্ধন"
                                                    AppLanguage.HINDI -> "नर्सरी पार्टनर पंजीकरण"
                                                    AppLanguage.ENGLISH -> "Nursery Partner Onboarding"
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "স্বচ্ছ ফিচার রোডম্যাপ (Live বনাম উন্নয়নাধীন)"
                                    AppLanguage.HINDI -> "पारदर्शी सुविधा रोडमैप (लाइव बनाम निर्माणाधीन)"
                                    AppLanguage.ENGLISH -> "Transparent Capability Status (Live vs Under Development vs Planned)"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = RoyalSoftGold
                            )
                        }

                        items(capabilities) { cap ->
                            val (statusText, statusColor) = when (cap.status) {
                                FeatureAvailabilityStatus.LIVE -> "Live" to RoyalGreenHighlight
                                FeatureAvailabilityStatus.UNDER_DEVELOPMENT -> "Under Development" to RoyalGold
                                FeatureAvailabilityStatus.PLANNED -> "Coming Soon" to RoyalTextSecondary
                            }
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                border = BorderStroke(1.dp, RoyalBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cap.getName(currentLanguage),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = RoyalTextPrimary,
                                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                                        )
                                        Surface(
                                            color = statusColor.copy(alpha = 0.18f),
                                            shape = RoundedCornerShape(999.dp),
                                            border = BorderStroke(1.dp, statusColor)
                                        ) {
                                            Text(
                                                text = statusText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusColor,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = cap.getDetail(currentLanguage),
                                        fontSize = 12.sp,
                                        color = RoyalTextSecondary
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiscoveryArticleCard(
    article: SeoArticleEntity,
    currentLanguage: AppLanguage,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenWeb: () -> Unit,
    onOpenSource: () -> Unit,
    onOpenCrop: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        border = BorderStroke(1.dp, RoyalBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .testTag("discovery_article_${article.slug}")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = RoyalGreenSecondaryBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, RoyalBorder)
                ) {
                    Text(
                        text = article.categoryEnum().label(currentLanguage),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalSoftGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.lastUpdatedDate,
                        fontSize = 11.sp,
                        color = RoyalTextSecondary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = RoyalSoftGold
                    )
                }
            }

            Text(
                text = article.title(currentLanguage),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = RoyalTextPrimary
            )
            Text(
                text = article.summary(currentLanguage),
                fontSize = 12.5.sp,
                color = RoyalTextSecondary
            )

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    Text(
                        text = article.body(currentLanguage),
                        fontSize = 13.sp,
                        color = RoyalTextPrimary,
                        lineHeight = 19.sp
                    )
                    article.parsedFaqs().forEach { faq ->
                        Surface(
                            color = RoyalGreenSecondaryBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, RoyalBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Q: ${faq.question}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoyalSoftGold)
                                Text(faq.answer, fontSize = 12.sp, color = RoyalTextSecondary)
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = RoyalGreenHighlight, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${article.sourceName} (${article.sourceUrl})",
                            fontSize = 11.sp,
                            color = RoyalSoftGold
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenSource,
                            border = BorderStroke(1.dp, RoyalBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalSoftGold),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Source", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onOpenWeb,
                            border = BorderStroke(1.dp, RoyalGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalSoftGold),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Web Guide", fontSize = 12.sp)
                        }
                        if (article.cropId.isNotBlank()) {
                            Button(
                                onClick = onOpenCrop,
                                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text("Crop Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
private fun CropSeoGuideCard(
    article: SeoArticleEntity,
    currentLanguage: AppLanguage,
    onOpenInAppCrop: () -> Unit,
    onOpenWebGuide: () -> Unit,
    onOpenPlantProblem: () -> Unit
) {
    val cropDrawable = remember(article.cropId) {
        when (article.cropId) {
            "rice" -> R.drawable.img_rice_paddy_1794803719617
            "potato" -> R.drawable.img_potato_1793343095516
            "tea" -> R.drawable.img_tea_leaves_1793343045221
            "tomato" -> R.drawable.img_tomato_1793343030528
            "cabbage" -> R.drawable.img_cabbage_1793343062183
            "mustard" -> R.drawable.img_mustard_crop_1794803721161
            "mango" -> R.drawable.img_mango_1793343080850
            "chilli" -> R.drawable.img_chilli_1793343108793
            else -> R.drawable.img_rice_paddy_1794803719617
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        border = BorderStroke(1.dp, RoyalBorder),
        modifier = Modifier.fillMaxWidth().testTag("discovery_crop_card_${article.cropId}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = cropDrawable),
                    contentDescription = article.title(currentLanguage),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = article.title(currentLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = RoyalTextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = article.metaDescription(currentLanguage),
                        fontSize = 12.sp,
                        color = RoyalTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${article.sourceName} • ${article.lastUpdatedDate}",
                        fontSize = 10.5.sp,
                        color = RoyalSoftGold
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenInAppCrop,
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "অ্যাপে চাষ গাইড"
                            AppLanguage.HINDI -> "ऐप में फसल गाइड"
                            AppLanguage.ENGLISH -> "Open In-App Guide"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    onClick = onOpenWebGuide,
                    border = BorderStroke(1.dp, RoyalGold),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalSoftGold),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("SEO Web Page", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onOpenPlantProblem,
                    border = BorderStroke(1.dp, RoyalBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalTextSecondary),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Pest/Symptom Check", fontSize = 12.sp)
                }
            }
        }
    }
}
