package com.example.ui.nursery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.data.datasource.StarterNurseryDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.FraudSeedReport
import com.example.data.model.ItemStockStatus
import com.example.data.model.NurseryBusiness
import com.example.data.model.NurseryCatalogueItem
import com.example.data.model.PlantCategory
import com.example.data.model.SeedCategory
import com.example.data.model.SeedItem
import com.example.data.model.WholesaleQuoteRequest
import com.example.data.remote.FirestoreRepository
import com.example.ui.theme.AgriAlertRed
import com.example.ui.theme.AgriEarthSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriGreenPrimaryContainer
import com.example.util.safeStartActivity
import com.example.util.toLocalizedIntOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NurserySeedMarketScreen(
    currentLanguage: AppLanguage,
    userId: String,
    farmerName: String,
    onBack: () -> Unit,
    onOpenNurseryRegistration: () -> Unit = {},
    onOpenNurseryProfile: (String) -> Unit = {},
    onOpenPlantKingdom: () -> Unit = {},
    onOpenNurseryDirectory: () -> Unit = {},
    onOpenNurseryMap: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { FirestoreRepository(context) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlantCategory by remember { mutableStateOf<PlantCategory?>(null) }
    var selectedSeedCategory by remember { mutableStateOf<SeedCategory?>(null) }
    var filterVerifiedOnly by remember { mutableStateOf(false) }
    var filterWholesaleOnly by remember { mutableStateOf(false) }

    // Dialog state
    var selectedNurseryForDetail by remember { mutableStateOf<NurseryBusiness?>(null) }
    var quoteRequestItem by remember { mutableStateOf<Pair<String, String>?>(null) } // (ItemName, TargetNurseryName)
    var showRegisterNurseryDialog by remember { mutableStateOf(false) }
    var showFraudReportDialog by remember { mutableStateOf(false) }

    val favoriteIds = remember { mutableStateListOf<String>() }

    val nurseries = remember {
        StarterNurseryDatabase.starterNurseries
    }
    val seeds = remember {
        StarterNurseryDatabase.starterSeeds
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
                                AppLanguage.BENGALI -> "🌱 নার্সারি ও বীজ বাজার"
                                AppLanguage.HINDI -> "🌱 नर्सरी व बीज बाज़ार"
                                AppLanguage.ENGLISH -> "Nursery & Seed Market"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "শিলিগুড়ি ও উত্তরবঙ্গ চারা গাছ ও বীজ হাব"
                                AppLanguage.HINDI -> "सिलीगुड़ी व उत्तर बंगाल पौध व बीज केंद्र"
                                AppLanguage.ENGLISH -> "Siliguri & North Bengal Plants & Seeds Hub"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("nursery_btn_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onOpenNurseryRegistration() },
                        modifier = Modifier.size(48.dp).testTag("nursery_btn_register")
                    ) {
                        Icon(Icons.Default.AddBusiness, contentDescription = "Register Nursery", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { showFraudReportDialog = true },
                        modifier = Modifier.size(48.dp).testTag("nursery_btn_report")
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = "Report Fake Seed", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
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
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("nursery_search_input"),
                placeholder = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "গাছ, ফল, বীজ, বা নার্সারির নাম লিখুন..."
                            AppLanguage.HINDI -> "पौधे, फल, बीज या नर्सरी का नाम लिखें..."
                            AppLanguage.ENGLISH -> "Search plants, seeds, nursery, variety..."
                        }
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = RoundedCornerShape(12.dp)
            )

            // Quick Entry Bar to Plant Kingdom Library, 22-District Directory, & Map
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedButton(
                        onClick = onOpenPlantKingdom,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("market_btn_plant_kingdom"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "🌿 উদ্ভিদ ভাণ্ডার (১২৪+)"
                                AppLanguage.HINDI -> "🌿 पौधा विश्वकोश (124+)"
                                AppLanguage.ENGLISH -> "🌿 Plant Kingdom (124+)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                item {
                    OutlinedButton(
                        onClick = onOpenNurseryDirectory,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("market_btn_wb_directory"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "🏪 ২২ জেলা ডিরেক্টরি"
                                AppLanguage.HINDI -> "🏪 22 ज़िला निर्देशिका"
                                AppLanguage.ENGLISH -> "🏪 22-District Directory"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                item {
                    OutlinedButton(
                        onClick = onOpenNurseryMap,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("market_btn_nursery_map"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "🗺️ ম্যাপ"
                                AppLanguage.HINDI -> "🗺️ मैप"
                                AppLanguage.ENGLISH -> "🗺️ Map"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Main 4 Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "🌱 নার্সারি ও চারা"
                                AppLanguage.HINDI -> "🌱 नर्सरी व पौधे"
                                AppLanguage.ENGLISH -> "🌱 Nurseries & Plants"
                            },
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "🌾 বীজ বাজার"
                                AppLanguage.HINDI -> "🌾 बीज बाज़ार"
                                AppLanguage.ENGLISH -> "🌾 Seed Market"
                            },
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "📦 পাইকারি বাজার"
                                AppLanguage.HINDI -> "📦 थोक बाज़ार"
                                AppLanguage.ENGLISH -> "📦 Wholesale B2B"
                            },
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "❤️ সংরক্ষিত (${favoriteIds.size})"
                                AppLanguage.HINDI -> "❤️ पसंदीदा (${favoriteIds.size})"
                                AppLanguage.ENGLISH -> "❤️ Favorites (${favoriteIds.size})"
                            },
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Quick Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = filterVerifiedOnly,
                    onClick = { filterVerifiedOnly = !filterVerifiedOnly },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (filterVerifiedOnly) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "যাচাইকৃত"
                                    AppLanguage.HINDI -> "सत्यापित"
                                    AppLanguage.ENGLISH -> "Verified Only"
                                },
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                FilterChip(
                    selected = filterWholesaleOnly,
                    onClick = { filterWholesaleOnly = !filterWholesaleOnly },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "পাইকারি (Bulk)"
                                AppLanguage.HINDI -> "थोक (Bulk)"
                                AppLanguage.ENGLISH -> "Wholesale"
                            },
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Plants & Nurseries Tab
                    PlantCategoriesHeader(
                        selectedCategory = selectedPlantCategory,
                        currentLanguage = currentLanguage,
                        onSelectCategory = { selectedPlantCategory = if (selectedPlantCategory == it) null else it }
                    )

                    val filteredNurseries = nurseries.filter { n ->
                        val matchesQuery = searchQuery.isBlank() ||
                                n.nameBn.contains(searchQuery, ignoreCase = true) ||
                                n.nameEn.contains(searchQuery, ignoreCase = true) ||
                                n.getDisplayName(currentLanguage).contains(searchQuery, ignoreCase = true) ||
                                n.addressBn.contains(searchQuery, ignoreCase = true) ||
                                n.getAddress(currentLanguage).contains(searchQuery, ignoreCase = true) ||
                                n.catalogue.any {
                                    it.nameBn.contains(searchQuery, ignoreCase = true) ||
                                        it.nameEn.contains(searchQuery, ignoreCase = true) ||
                                        it.getName(currentLanguage).contains(searchQuery, ignoreCase = true)
                                }

                        val matchesCategory = selectedPlantCategory == null || n.categories.contains(selectedPlantCategory)
                        val matchesVerified = !filterVerifiedOnly || n.isVerified
                        val matchesWholesale = !filterWholesaleOnly || n.isWholesale

                        matchesQuery && matchesCategory && matchesVerified && matchesWholesale
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            FraudSeedBanner(
                                currentLanguage = currentLanguage,
                                onReportClick = { showFraudReportDialog = true }
                            )
                        }

                        if (filteredNurseries.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "কোনো নার্সারি পাওয়া যায়নি"
                                        AppLanguage.HINDI -> "कोई नर्सरी नहीं मिली"
                                        AppLanguage.ENGLISH -> "No nurseries found"
                                    },
                                    subtitle = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অনুসন্ধানের শর্ত পরিবর্তন করে দেখুন"
                                        AppLanguage.HINDI -> "खोज या फ़िल्टर बदलकर देखें"
                                        AppLanguage.ENGLISH -> "Try clearing search or filters"
                                    }
                                )
                            }
                        } else {
                            items(filteredNurseries, key = { it.id }) { nursery ->
                                NurseryCard(
                                    nursery = nursery,
                                    currentLanguage = currentLanguage,
                                    isFavorite = favoriteIds.contains(nursery.id),
                                    onToggleFavorite = {
                                        if (favoriteIds.contains(nursery.id)) favoriteIds.remove(nursery.id)
                                        else favoriteIds.add(nursery.id)
                                    },
                                    onViewCatalogue = { selectedNurseryForDetail = nursery },
                                    onRequestQuote = {
                                        val itemLabel = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "গাছ ও চারা"
                                            AppLanguage.HINDI -> "पौधे व कलम"
                                            AppLanguage.ENGLISH -> "Plants & Saplings"
                                        }
                                        quoteRequestItem = Pair(itemLabel, nursery.getDisplayName(currentLanguage))
                                    },
                                    onCall = { launchPhoneCall(context, nursery.phone, currentLanguage) },
                                    onWhatsapp = {
                                        val waMsg = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "নমস্কার, কৃষক সাথী অ্যাপ থেকে আপনার নার্সারির চারা সম্পর্কে জানতে চাই।"
                                            AppLanguage.HINDI -> "नमस्ते, कृषक साथी ऐप से मैं आपकी नर्सरी के पौधों के बारे में जानना चाहता हूँ।"
                                            AppLanguage.ENGLISH -> "Hello, I would like to inquire about plants from your nursery on Krishak Sathi."
                                        }
                                        launchWhatsApp(context, nursery.whatsapp, waMsg, currentLanguage)
                                    }
                                )
                            }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
                1 -> {
                    // Seed Market Tab
                    SeedCategoriesHeader(
                        selectedCategory = selectedSeedCategory,
                        currentLanguage = currentLanguage,
                        onSelectCategory = { selectedSeedCategory = if (selectedSeedCategory == it) null else it }
                    )

                    val filteredSeeds = seeds.filter { s ->
                        val matchesQuery = searchQuery.isBlank() ||
                                s.cropNameBn.contains(searchQuery, ignoreCase = true) ||
                                s.cropNameEn.contains(searchQuery, ignoreCase = true) ||
                                s.getCropName(currentLanguage).contains(searchQuery, ignoreCase = true) ||
                                s.varietyBn.contains(searchQuery, ignoreCase = true) ||
                                s.getVariety(currentLanguage).contains(searchQuery, ignoreCase = true) ||
                                s.brandName.contains(searchQuery, ignoreCase = true) ||
                                s.nurseryName.contains(searchQuery, ignoreCase = true)

                        val matchesCategory = selectedSeedCategory == null || s.category == selectedSeedCategory
                        val matchesVerified = !filterVerifiedOnly || s.isVerified

                        matchesQuery && matchesCategory && matchesVerified
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            CertifiedSeedPolicyCard(currentLanguage = currentLanguage)
                        }

                        if (filteredSeeds.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "কোনো বীজ তালিকা পাওয়া যায়নি"
                                        AppLanguage.HINDI -> "कोई बीज सूची नहीं मिली"
                                        AppLanguage.ENGLISH -> "No seed listings found"
                                    },
                                    subtitle = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অনুসন্ধান ফিল্টার পরিবর্তন করুন"
                                        AppLanguage.HINDI -> "अन्य खोज फ़िल्टर आज़माएं"
                                        AppLanguage.ENGLISH -> "Try other search filters"
                                    }
                                )
                            }
                        } else {
                            items(filteredSeeds, key = { it.id }) { seed ->
                                SeedCard(
                                    seed = seed,
                                    currentLanguage = currentLanguage,
                                    isFavorite = favoriteIds.contains(seed.id),
                                    onToggleFavorite = {
                                        if (favoriteIds.contains(seed.id)) favoriteIds.remove(seed.id)
                                        else favoriteIds.add(seed.id)
                                    },
                                    onRequestQuote = {
                                        quoteRequestItem = Pair(
                                            seed.getCropName(currentLanguage) + " (" + seed.getVariety(currentLanguage) + ")",
                                            seed.nurseryName
                                        )
                                    },
                                    onCall = { launchPhoneCall(context, seed.nurseryPhone, currentLanguage) },
                                    onWhatsapp = {
                                        val waMsg = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "নমস্কার, ${seed.getCropName(currentLanguage)} (${seed.getVariety(currentLanguage)}) বীজ প্যাকেটের অর্ডার দিতে চাই।"
                                            AppLanguage.HINDI -> "नमस्ते, मुझे ${seed.getCropName(currentLanguage)} (${seed.getVariety(currentLanguage)}) बीज पैकेट ऑर्डर करना है।"
                                            AppLanguage.ENGLISH -> "Hello, I would like to order ${seed.getCropName(currentLanguage)} (${seed.getVariety(currentLanguage)}) seed packets."
                                        }
                                        launchWhatsApp(context, seed.nurseryWhatsapp, waMsg, currentLanguage)
                                    }
                                )
                            }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
                2 -> {
                    // Wholesale B2B Market
                    WholesaleMarketView(
                        currentLanguage = currentLanguage,
                        nurseries = nurseries.filter { it.isWholesale },
                        onRequestCustomQuote = {
                            val itemTitle = when (currentLanguage) {
                                AppLanguage.BENGALI -> "পাইকারি চাহিদা (Bulk Order)"
                                AppLanguage.HINDI -> "थोक मांग (Bulk Order)"
                                AppLanguage.ENGLISH -> "Bulk Wholesale Order"
                            }
                            val targetTitle = when (currentLanguage) {
                                AppLanguage.BENGALI -> "যেকোনো সার্টিফাইড নার্সারি"
                                AppLanguage.HINDI -> "कोई भी प्रमाणित नर्सरी"
                                AppLanguage.ENGLISH -> "Any Certified Nursery"
                            }
                            quoteRequestItem = Pair(itemTitle, targetTitle)
                        },
                        onSelectNursery = { selectedNurseryForDetail = it }
                    )
                }
                3 -> {
                    // Favorites Tab
                    val favNurseries = nurseries.filter { favoriteIds.contains(it.id) }
                    val favSeeds = seeds.filter { favoriteIds.contains(it.id) }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (favNurseries.isEmpty() && favSeeds.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "কোনো পছন্দের আইটেম সংরক্ষিত নেই"
                                        AppLanguage.HINDI -> "अभी कोई पसंदीदा आइटम नहीं सहेजा गया"
                                        AppLanguage.ENGLISH -> "No saved favorites yet"
                                    },
                                    subtitle = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "চারা বা বীজ কার্ডের হার্ট (❤️) আইকনে ট্যাপ করে সংরক্ষণ করুন"
                                        AppLanguage.HINDI -> "किसी भी पौधे या बीज कार्ड पर हार्ट (❤️) आइकन दबाकर सहेजें"
                                        AppLanguage.ENGLISH -> "Tap the heart icon on any card to save it here"
                                    }
                                )
                            }
                        } else {
                            if (favNurseries.isNotEmpty()) {
                                item {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "সংরক্ষিত নার্সারি (${favNurseries.size})"
                                            AppLanguage.HINDI -> "सहेजी गई नर्सरी (${favNurseries.size})"
                                            AppLanguage.ENGLISH -> "Saved Nurseries (${favNurseries.size})"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                items(favNurseries, key = { "fav_n_${it.id}" }) { n ->
                                    NurseryCard(
                                        nursery = n,
                                        currentLanguage = currentLanguage,
                                        isFavorite = true,
                                        onToggleFavorite = { favoriteIds.remove(n.id) },
                                        onViewCatalogue = { selectedNurseryForDetail = n },
                                        onRequestQuote = {
                                            val itemLabel = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "গাছ ও চারা"
                                                AppLanguage.HINDI -> "पौधे व कलम"
                                                AppLanguage.ENGLISH -> "Plants & Saplings"
                                            }
                                            quoteRequestItem = Pair(itemLabel, n.getDisplayName(currentLanguage))
                                        },
                                        onCall = { launchPhoneCall(context, n.phone, currentLanguage) },
                                        onWhatsapp = {
                                            val msg = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "নমস্কার, সংরক্ষিত তালিকা থেকে আপনার সাথে যোগাযোগ করছি।"
                                                AppLanguage.HINDI -> "नमस्ते, मैं अपनी पसंदीदा सूची से आपसे संपर्क कर रहा हूँ।"
                                                AppLanguage.ENGLISH -> "Hello, contacting you from my saved favorites on Krishak Sathi."
                                            }
                                            launchWhatsApp(context, n.whatsapp, msg, currentLanguage)
                                        }
                                    )
                                }
                            }

                            if (favSeeds.isNotEmpty()) {
                                item {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "সংরক্ষিত বীজ (${favSeeds.size})"
                                            AppLanguage.HINDI -> "सहेजे गए बीज (${favSeeds.size})"
                                            AppLanguage.ENGLISH -> "Saved Seeds (${favSeeds.size})"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(favSeeds, key = { "fav_s_${it.id}" }) { s ->
                                    SeedCard(
                                        seed = s,
                                        currentLanguage = currentLanguage,
                                        isFavorite = true,
                                        onToggleFavorite = { favoriteIds.remove(s.id) },
                                        onRequestQuote = { quoteRequestItem = Pair(s.getCropName(currentLanguage), s.nurseryName) },
                                        onCall = { launchPhoneCall(context, s.nurseryPhone, currentLanguage) },
                                        onWhatsapp = {
                                            val msg = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "নমস্কার, ${s.getCropName(currentLanguage)} বীজ সম্পর্কে জানতে চাই।"
                                                AppLanguage.HINDI -> "नमस्ते, मुझे ${s.getCropName(currentLanguage)} बीज के बारे में जानकारी चाहिए।"
                                                AppLanguage.ENGLISH -> "Hello, I want to know more about ${s.getCropName(currentLanguage)} seeds."
                                            }
                                            launchWhatsApp(context, s.nurseryWhatsapp, msg, currentLanguage)
                                        }
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }
        }
    }

    // Nursery Detail Dialog
    selectedNurseryForDetail?.let { nursery ->
        NurseryDetailDialog(
            nursery = nursery,
            currentLanguage = currentLanguage,
            onDismiss = { selectedNurseryForDetail = null },
            onRequestQuote = { itemTitle ->
                quoteRequestItem = Pair(itemTitle, nursery.getDisplayName(currentLanguage))
            },
            onCall = { launchPhoneCall(context, nursery.phone, currentLanguage) },
            onWhatsapp = {
                val msg = when (currentLanguage) {
                    AppLanguage.BENGALI -> "নমস্কার, কৃষক সাথী থেকে ${nursery.getDisplayName(currentLanguage)} এর চারা কিনতে আগ্রহী।"
                    AppLanguage.HINDI -> "नमस्ते, कृषक साथी के माध्यम से मैं ${nursery.getDisplayName(currentLanguage)} से पौधे खरीदने में रुचि रखता हूँ।"
                    AppLanguage.ENGLISH -> "Hello, interested in buying saplings from ${nursery.getDisplayName(currentLanguage)} via Krishak Sathi."
                }
                launchWhatsApp(context, nursery.whatsapp, msg, currentLanguage)
            },
            onOpenMap = { launchGoogleMaps(context, nursery.getAddress(currentLanguage) + ", " + nursery.city, currentLanguage) }
        )
    }

    // Wholesale Quote Request Dialog
    quoteRequestItem?.let { pair ->
        WholesaleQuoteDialog(
            itemName = pair.first,
            targetNursery = pair.second,
            farmerName = farmerName,
            currentLanguage = currentLanguage,
            onDismiss = { quoteRequestItem = null },
            onSubmit = { req ->
                coroutineScope.launch {
                    try {
                        repo.submitWholesaleQuoteRequest(req)
                        val msg = when (currentLanguage) {
                            AppLanguage.BENGALI -> "পাইকারি কোটেশন অনুরোধ সফলভাবে পাঠানো হয়েছে!"
                            AppLanguage.HINDI -> "थोक कोटेशन अनुरोध सफलतापूर्वक भेजा गया!"
                            AppLanguage.ENGLISH -> "Wholesale quote request submitted successfully!"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        val offlineMsg = when (currentLanguage) {
                            AppLanguage.BENGALI -> "অনুরোধ গৃহীত হয়েছে (অফলাইন সংরক্ষিত)"
                            AppLanguage.HINDI -> "अनुरोध सहेजा गया (ऑफ़लाइन)"
                            AppLanguage.ENGLISH -> "Request saved offline"
                        }
                        Toast.makeText(context, offlineMsg, Toast.LENGTH_SHORT).show()
                    }
                    quoteRequestItem = null
                }
            }
        )
    }

    // Register Nursery Dialog
    if (showRegisterNurseryDialog) {
        RegisterNurseryDialog(
            farmerName = farmerName,
            currentLanguage = currentLanguage,
            onDismiss = { showRegisterNurseryDialog = false },
            onSubmit = { nursery ->
                coroutineScope.launch {
                    try {
                        repo.registerNurseryBusiness(nursery, currentLanguage)
                        val msg = when (currentLanguage) {
                            AppLanguage.BENGALI -> "আপনার নার্সারি নিবন্ধিত হয়েছে! পর্যালোচনার পর যাচাই ব্যাজ যোগ হবে।"
                            AppLanguage.HINDI -> "आपकी नर्सरी पंजीकृत हो गई है! समीक्षा के बाद सत्यापित बैज जुड़ जाएगा।"
                            AppLanguage.ENGLISH -> "Your nursery has been registered! Verified badge will appear after review."
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        val savedMsg = when (currentLanguage) {
                            AppLanguage.BENGALI -> "নিবন্ধন তথ্য সংরক্ষিত হয়েছে!"
                            AppLanguage.HINDI -> "पंजीकरण विवरण सहेजा गया!"
                            AppLanguage.ENGLISH -> "Registration details saved!"
                        }
                        Toast.makeText(context, savedMsg, Toast.LENGTH_SHORT).show()
                    }
                    showRegisterNurseryDialog = false
                }
            }
        )
    }

    // Fraud Report Dialog
    if (showFraudReportDialog) {
        ReportFraudDialog(
            farmerName = farmerName,
            currentLanguage = currentLanguage,
            onDismiss = { showFraudReportDialog = false },
            onSubmit = { report ->
                coroutineScope.launch {
                    try {
                        repo.submitFraudSeedReport(report)
                        val msg = when (currentLanguage) {
                            AppLanguage.BENGALI -> "অভিযোগ সফলভাবে গ্রহণ করা হয়েছে। কৃষি কর্তৃপক্ষকে জানানো হবে।"
                            AppLanguage.HINDI -> "शिकायत सफलतापूर्वक दर्ज कर ली गई है। कृषि विभाग को सूचित किया जाएगा।"
                            AppLanguage.ENGLISH -> "Complaint submitted successfully. Agriculture authorities will be notified."
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        val savedMsg = when (currentLanguage) {
                            AppLanguage.BENGALI -> "অভিযোগ সংরক্ষিত হয়েছে।"
                            AppLanguage.HINDI -> "शिकायत सहेजी गई।"
                            AppLanguage.ENGLISH -> "Complaint saved."
                        }
                        Toast.makeText(context, savedMsg, Toast.LENGTH_SHORT).show()
                    }
                    showFraudReportDialog = false
                }
            }
        )
    }
}

@Composable
fun PlantCategoriesHeader(
    selectedCategory: PlantCategory?,
    currentLanguage: AppLanguage,
    onSelectCategory: (PlantCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(PlantCategory.values()) { cat ->
            val isSelected = selectedCategory == cat
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { onSelectCategory(cat) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = cat.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cat.getDisplayName(currentLanguage),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SeedCategoriesHeader(
    selectedCategory: SeedCategory?,
    currentLanguage: AppLanguage,
    onSelectCategory: (SeedCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SeedCategory.values()) { cat ->
            val isSelected = selectedCategory == cat
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { onSelectCategory(cat) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = cat.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cat.getDisplayName(currentLanguage),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NurseryCard(
    nursery: NurseryBusiness,
    currentLanguage: AppLanguage,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onViewCatalogue: () -> Unit,
    onRequestQuote: () -> Unit,
    onCall: () -> Unit,
    onWhatsapp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nursery_card_${nursery.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Name & Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = nursery.getDisplayName(currentLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = nursery.getAddress(currentLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) AgriAlertRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badges row: Verified badge & Rating & Wholesale
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (nursery.isVerified) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = nursery.getBadge(currentLanguage),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (nursery.isWholesale) {
                    Surface(
                        color = AgriEarthSecondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "📦 পাইকারি উপলব্ধ"
                                AppLanguage.HINDI -> "📦 थोक उपलब्ध"
                                AppLanguage.ENGLISH -> "📦 Wholesale Available"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AgriEarthSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = com.example.ui.theme.RoyalGold)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${nursery.rating} (${nursery.reviewCount})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Available popular crops preview
            if (nursery.catalogue.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "উপলব্ধ চারা ও জাত:"
                        AppLanguage.HINDI -> "उपलब्ध पौधे व किस्में:"
                        AppLanguage.ENGLISH -> "Available Saplings:"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    nursery.catalogue.take(3).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.iconEmoji} ${item.getName(currentLanguage)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(end = 6.dp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹${item.priceRetail.toInt()}/${item.getUnit(currentLanguage)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (item.priceWholesale > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "(পাইকারি ₹${item.priceWholesale.toInt()})"
                                            AppLanguage.HINDI -> "(थोक ₹${item.priceWholesale.toInt()})"
                                            AppLanguage.ENGLISH -> "(Bulk ₹${item.priceWholesale.toInt()})"
                                        },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onViewCatalogue,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "তালিকা দেখুন"
                            AppLanguage.HINDI -> "सूची देखें"
                            AppLanguage.ENGLISH -> "Catalogue"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onRequestQuote,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "পাইকারি দর"
                            AppLanguage.HINDI -> "थोक भाव"
                            AppLanguage.ENGLISH -> "Get Quote"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onCall,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                }

                IconButton(
                    onClick = onWhatsapp,
                    modifier = Modifier
                        .background(Color(0xFF25D366).copy(alpha = 0.16f), CircleShape)
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.Whatsapp, contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeedCard(
    seed: SeedItem,
    currentLanguage: AppLanguage,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onRequestQuote: () -> Unit,
    onCall: () -> Unit,
    onWhatsapp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("seed_card_${seed.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = seed.getCropName(currentLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "জাত: ${seed.getVariety(currentLanguage)} • কোম্পানি: ${seed.getBrand(currentLanguage)}"
                            AppLanguage.HINDI -> "किस्म: ${seed.getVariety(currentLanguage)} • कंपनी: ${seed.getBrand(currentLanguage)}"
                            AppLanguage.ENGLISH -> "Variety: ${seed.getVariety(currentLanguage)} • Brand: ${seed.getBrand(currentLanguage)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) AgriAlertRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Badges
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = Color(seed.stockStatus.colorHex).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "• ${seed.stockStatus.getDisplayName(currentLanguage)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(seed.stockStatus.colorHex),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (seed.isCertified) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "✓ সার্টিফাইড বীজ (${seed.germinationRatePercent}% অঙ্কুরোদ্গম)"
                                AppLanguage.HINDI -> "✓ प्रमाणित बीज (${seed.germinationRatePercent}% अंकुरण)"
                                AppLanguage.ENGLISH -> "✓ Certified (${seed.germinationRatePercent}% Germination)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Seller & Pricing Info
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "প্যাকেট সাইজ: ${seed.getPacketSize(currentLanguage)}"
                            AppLanguage.HINDI -> "पैकेट साइज: ${seed.getPacketSize(currentLanguage)}"
                            AppLanguage.ENGLISH -> "Pack Size: ${seed.getPacketSize(currentLanguage)}"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "দোকান: ${seed.nurseryName}"
                            AppLanguage.HINDI -> "दुकान: ${seed.nurseryName}"
                            AppLanguage.ENGLISH -> "Dealer: ${seed.nurseryName}"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "খুচরো: ₹${seed.retailPrice.toInt()}"
                            AppLanguage.HINDI -> "खुदरा: ₹${seed.retailPrice.toInt()}"
                            AppLanguage.ENGLISH -> "Retail: ₹${seed.retailPrice.toInt()}"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "পাইকারি: ₹${seed.wholesalePrice.toInt()} (ন্যূনতম ${seed.minWholesalePackets} প্যাকেট)"
                            AppLanguage.HINDI -> "थोक: ₹${seed.wholesalePrice.toInt()} (न्यूनतम ${seed.minWholesalePackets} पैकेट)"
                            AppLanguage.ENGLISH -> "Bulk: ₹${seed.wholesalePrice.toInt()} (Min ${seed.minWholesalePackets} packs)"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AgriEarthSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRequestQuote,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "পাইকারি কোটেশন"
                            AppLanguage.HINDI -> "थोक कोटेशन"
                            AppLanguage.ENGLISH -> "Wholesale Quote"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onCall,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                }

                IconButton(
                    onClick = onWhatsapp,
                    modifier = Modifier
                        .background(Color(0xFF25D366).copy(alpha = 0.16f), CircleShape)
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.Whatsapp, contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WholesaleMarketView(
    currentLanguage: AppLanguage,
    nurseries: List<NurseryBusiness>,
    onRequestCustomQuote: () -> Unit,
    onSelectNursery: (NurseryBusiness) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "📦 পাইকারি চাষি ও নার্সারি হাব"
                            AppLanguage.HINDI -> "📦 थोक किसान व नर्सरी हब"
                            AppLanguage.ENGLISH -> "📦 Wholesale Farmer & Nursery B2B Hub"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "একসাথে ৫০০–৫০০০ চারা বা ৫০+ প্যাকেট বীজের ক্ষেত্রে সরাসরি পাইকারি দর পান। একাধিক নার্সারি থেকে অফার তুলনা করুন।"
                            AppLanguage.HINDI -> "एक साथ 500–5000 पौधे या 50+ बीज पैकेट पर सीधा थोक भाव पाएं। कई नर्सरियों के प्रस्तावों की तुलना करें।"
                            AppLanguage.ENGLISH -> "Get bulk direct pricing for 500-5000+ saplings or 50+ seed packets. Compare offers from verified nurseries."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRequestCustomQuote,
                        modifier = Modifier.heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নতুন পাইকারি কোটেশন অনুরোধ পাঠান"
                                AppLanguage.HINDI -> "नया थोक कोटेशन अनुरोध भेजें"
                                AppLanguage.ENGLISH -> "Submit Bulk Quote Request"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "অনুমোদিত পাইকারি নার্সারি ও সরবরাহকারী:"
                    AppLanguage.HINDI -> "अधिकृत थोक नर्सरी व आपूर्तिकर्ता:"
                    AppLanguage.ENGLISH -> "Authorized Wholesale Nurseries & Suppliers:"
                },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(nurseries) { n ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectNursery(n) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = n.getDisplayName(currentLanguage), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = n.getAddress(currentLanguage), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ন্যূনতম পাইকারি অর্ডার: ₹${n.minimumWholesaleValue.toInt()} • ডেলিভারি: ${if (n.deliveryAvailable) "উপলব্ধ" else "পিকআপ মাত্র"}"
                                AppLanguage.HINDI -> "न्यूनतम थोक ऑर्डर: ₹${n.minimumWholesaleValue.toInt()} • डिलीवरी: ${if (n.deliveryAvailable) "उपलब्ध" else "केवल पिकअप"}"
                                AppLanguage.ENGLISH -> "Min Wholesale Order: ₹${n.minimumWholesaleValue.toInt()} • Delivery: ${if (n.deliveryAvailable) "Available" else "Pickup Only"}"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = { onSelectNursery(n) },
                        modifier = Modifier.heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "দর দেখুন"
                                AppLanguage.HINDI -> "भाव देखें"
                                AppLanguage.ENGLISH -> "View Rates"
                            },
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FraudSeedBanner(
    currentLanguage: AppLanguage,
    onReportClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.RoyalWarningBg),
        border = BorderStroke(1.dp, com.example.ui.theme.RoyalGold.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "⚠️ নকল বীজ ও প্রতারণা থেকে সাবধান"
                        AppLanguage.HINDI -> "⚠️ नकली बीज व धोखाधड़ी से सावधान"
                        AppLanguage.ENGLISH -> "⚠️ Caution: Fake Seeds & Unverified Sellers"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = com.example.ui.theme.RoyalGold
                )
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "অনুমোদনহীন বীজ কিনবেন না। সন্দেহজনক বীজ বিক্রেতার বিরুদ্ধে অভিযোগ জানান।"
                        AppLanguage.HINDI -> "बिना लाइसेंस वाले बीज न खरीदें। संदिग्ध बीज विक्रेता की रिपोर्ट करें।"
                        AppLanguage.ENGLISH -> "Always buy certified seeds with lot number. Report suspicious sellers."
                    },
                    fontSize = 11.sp,
                    color = com.example.ui.theme.RoyalTextSecondary
                )
            }
            OutlinedButton(
                onClick = onReportClick,
                modifier = Modifier.heightIn(min = 48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.RoyalSoftGold),
                border = BorderStroke(1.dp, com.example.ui.theme.RoyalGold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "রিপোর্ট"
                        AppLanguage.HINDI -> "रिपोर्ट"
                        AppLanguage.ENGLISH -> "Report"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CertifiedSeedPolicyCard(currentLanguage: AppLanguage) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "সব বীজ লিস্টিং সরকারি বা রেজিস্টার্ড ডিলারের মাধ্যমে অনুমোদিত। প্যাকেট কেনার আগে সিল ও ব্যাচ নম্বর পরীক্ষা করুন।"
                    AppLanguage.HINDI -> "सभी बीज लिस्टिंग सरकारी या पंजीकृत डीलर द्वारा प्रमाणित हैं। खरीदने से पहले सील और बैच नंबर जांचें।"
                    AppLanguage.ENGLISH -> "All seed listings are from licensed dealers. Verify packet seal and batch number before planting."
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun EmptyStateCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🌱", fontSize = 42.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// Dialogs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NurseryDetailDialog(
    nursery: NurseryBusiness,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onRequestQuote: (String) -> Unit,
    onCall: () -> Unit,
    onWhatsapp: () -> Unit,
    onOpenMap: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = nursery.getDisplayName(currentLanguage), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "মালিক: ${nursery.getOwner(currentLanguage)} • ${nursery.getAddress(currentLanguage)}"
                        AppLanguage.HINDI -> "मालिक: ${nursery.getOwner(currentLanguage)} • ${nursery.getAddress(currentLanguage)}"
                        AppLanguage.ENGLISH -> "Owner: ${nursery.getOwner(currentLanguage)} • ${nursery.getAddress(currentLanguage)}"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(modifier = Modifier.height(350.dp)) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenMap,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ম্যাপ / রাস্তা"
                                    AppLanguage.HINDI -> "मानचित्र / दिशा"
                                    AppLanguage.ENGLISH -> "Map & Directions"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "সম্পূর্ণ চারা তালিকা ও দর:"
                            AppLanguage.HINDI -> "संपूर्ण पौध सूची व दरें:"
                            AppLanguage.ENGLISH -> "Complete Plant Catalogue & Rates:"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (nursery.catalogue.isEmpty()) {
                    item {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "এই নার্সারির বিশেষ কাস্টম চারা ও বীজের জন্য সরাসরি কল বা WhatsApp করুন।"
                                AppLanguage.HINDI -> "इस नर्सरी के विशेष पौधों व बीजों के लिए सीधे कॉल या WhatsApp करें।"
                                AppLanguage.ENGLISH -> "Call or WhatsApp directly for custom plant saplings and seed availability from this dealer."
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    items(nursery.catalogue) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val u = item.getUnit(currentLanguage)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "${item.iconEmoji} ${item.getName(currentLanguage)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "জাত: ${item.getVariety(currentLanguage)}"
                                            AppLanguage.HINDI -> "किस्म: ${item.getVariety(currentLanguage)}"
                                            AppLanguage.ENGLISH -> "Variety: ${item.getVariety(currentLanguage)}"
                                        },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "খুচরো: ₹${item.priceRetail.toInt()} | পাইকারি: ₹${item.priceWholesale.toInt()} (ন্যূনতম ${item.minWholesaleQty} $u)"
                                            AppLanguage.HINDI -> "खुदरा: ₹${item.priceRetail.toInt()} | थोक: ₹${item.priceWholesale.toInt()} (न्यूनतम ${item.minWholesaleQty} $u)"
                                            AppLanguage.ENGLISH -> "Retail: ₹${item.priceRetail.toInt()} | Bulk: ₹${item.priceWholesale.toInt()} (Min ${item.minWholesaleQty})"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onRequestQuote(item.getName(currentLanguage)) },
                                    modifier = Modifier.heightIn(min = 48.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "কোটেশন"
                                            AppLanguage.HINDI -> "कोटेशन"
                                            AppLanguage.ENGLISH -> "Quote"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onCall,
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "কল করুন"
                            AppLanguage.HINDI -> "कॉल करें"
                            AppLanguage.ENGLISH -> "Call"
                        }
                    )
                }
                Button(
                    onClick = onWhatsapp,
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Whatsapp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", color = Color.White)
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(8.dp)) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "বন্ধ করুন"
                        AppLanguage.HINDI -> "बंद करें"
                        AppLanguage.ENGLISH -> "Close"
                    }
                )
            }
        }
    )
}

@Composable
fun WholesaleQuoteDialog(
    itemName: String,
    targetNursery: String,
    farmerName: String,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSubmit: (WholesaleQuoteRequest) -> Unit
) {
    var quantityText by remember { mutableStateOf("100") }
    var phoneText by remember { mutableStateOf("") }
    var locationText by remember(currentLanguage) {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "শিলিগুড়ি / জলপাইগুড়ি"
                AppLanguage.HINDI -> "सिलीगुड़ी / जलपाईगुड़ी"
                AppLanguage.ENGLISH -> "Siliguri / Jalpaiguri"
            }
        )
    }
    var notesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "পাইকারি দর কোটেশন অনুরোধ"
                    AppLanguage.HINDI -> "थोक दर कोटेशन अनुरोध"
                    AppLanguage.ENGLISH -> "Request Wholesale Quote"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).imePadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "পণ্য: $itemName\nসরবরাহকারী: $targetNursery"
                        AppLanguage.HINDI -> "उत्पाद: $itemName\nआपूर्तिकर्ता: $targetNursery"
                        AppLanguage.ENGLISH -> "Item: $itemName\nSupplier: $targetNursery"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "প্রয়োজনীয় সংখ্যা (চারা / প্যাকেট)"
                                AppLanguage.HINDI -> "आवश्यक मात्रा (पौधे / पैकेट)"
                                AppLanguage.ENGLISH -> "Required Quantity (Saplings / Packs)"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phoneText,
                    onValueChange = { phoneText = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "আপনার মোবাইল নম্বর"
                                AppLanguage.HINDI -> "आपका मोबाइल नंबर"
                                AppLanguage.ENGLISH -> "Your Mobile Number"
                            }
                        )
                    },
                    placeholder = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "যেমন 98320XXXXX"
                                AppLanguage.HINDI -> "जैसे 98320XXXXX"
                                AppLanguage.ENGLISH -> "e.g. 98320XXXXX"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    singleLine = true
                )
                OutlinedTextField(
                    value = locationText,
                    onValueChange = { locationText = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "ডেলিভারি এলাকা / গ্রাম / পিন"
                                AppLanguage.HINDI -> "डिलीवरी क्षेत्र / गाँव / पिन"
                                AppLanguage.ENGLISH -> "Delivery Area / Village / PIN"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "বিশেষ নির্দেশনা বা জাতের চাহিদা (ঐচ্ছিক)"
                                AppLanguage.HINDI -> "विशेष निर्देश या किस्म की मांग (वैकल्पिक)"
                                AppLanguage.ENGLISH -> "Special Notes or Variety Request (Optional)"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toLocalizedIntOrNull() ?: 50
                    val req = WholesaleQuoteRequest(
                        buyerName = farmerName,
                        buyerPhone = phoneText,
                        deliveryLocation = locationText,
                        itemName = itemName,
                        quantity = qty,
                        targetNurseryName = targetNursery,
                        notes = notesText
                    )
                    onSubmit(req)
                },
                modifier = Modifier.heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "অনুরোধ পাঠান"
                        AppLanguage.HINDI -> "अनुरोध भेजें"
                        AppLanguage.ENGLISH -> "Submit Request"
                    }
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "বাতিল"
                        AppLanguage.HINDI -> "रद्द करें"
                        AppLanguage.ENGLISH -> "Cancel"
                    }
                )
            }
        }
    )
}

@Composable
fun RegisterNurseryDialog(
    farmerName: String,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSubmit: (NurseryBusiness) -> Unit
) {
    var nurseryName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf(farmerName) }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isWholesale by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "নতুন নার্সারি বা বীজ দোকান যোগ করুন"
                    AppLanguage.HINDI -> "नई नर्सरी या बीज दुकान जोड़ें"
                    AppLanguage.ENGLISH -> "Register Nursery or Seed Shop"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            LazyColumn(modifier = Modifier.height(350.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "কৃষক সাথী প্ল্যাটফর্মে আপনার নার্সারি বা বীজ কেন্দ্র যুক্ত করে হাজার হাজার কৃষক ও পাইকারদের কাছে পৌঁছান।"
                            AppLanguage.HINDI -> "कृषक साथी प्लेटफॉर्म पर अपनी नर्सरी या बीज केंद्र जोड़कर हजारों किसानों और थोक खरीदारों तक पहुँचें।"
                            AppLanguage.ENGLISH -> "Add your nursery or seed shop on Krishak Sathi to reach thousands of farmers and bulk buyers."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                item {
                    OutlinedTextField(
                        value = nurseryName,
                        onValueChange = { nurseryName = it },
                        label = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "নার্সারি বা প্রতিষ্ঠানের নাম *"
                                    AppLanguage.HINDI -> "नर्सरी या दुकान का नाम *"
                                    AppLanguage.ENGLISH -> "Nursery / Business Name *"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "মালিক বা পরিচালকের নাম"
                                    AppLanguage.HINDI -> "मालिक या संचालक का नाम"
                                    AppLanguage.ENGLISH -> "Owner / Manager Name"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "মোবাইল নম্বর *"
                                    AppLanguage.HINDI -> "मोबाइल नंबर *"
                                    AppLanguage.ENGLISH -> "Mobile Number *"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "WhatsApp নম্বর"
                                    AppLanguage.HINDI -> "WhatsApp नंबर"
                                    AppLanguage.ENGLISH -> "WhatsApp Number"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ঠিকানা ও এলাকা *"
                                    AppLanguage.HINDI -> "पता और क्षेत्र *"
                                    AppLanguage.ENGLISH -> "Full Address & Area *"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                        singleLine = true
                    )
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = isWholesale,
                            onClick = { isWholesale = !isWholesale },
                            label = {
                                Text(
                                    if (isWholesale) {
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "✓ পাইকারি সরবরাহ উপলব্ধ"
                                            AppLanguage.HINDI -> "✓ थोक आपूर्ति उपलब्ध"
                                            AppLanguage.ENGLISH -> "✓ Wholesale Supply Available"
                                        }
                                    } else {
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "শুধুমাত্র খুচরো"
                                            AppLanguage.HINDI -> "केवल खुदरा"
                                            AppLanguage.ENGLISH -> "Retail Only"
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nurseryName.isNotBlank() && phone.isNotBlank()) {
                        val newNursery = NurseryBusiness(
                            nameBn = nurseryName,
                            nameEn = nurseryName,
                            ownerName = ownerName,
                            phone = phone,
                            whatsapp = if (whatsapp.isNotBlank()) whatsapp else phone,
                            addressBn = address,
                            city = "Siliguri",
                            isWholesale = isWholesale,
                            isRetail = true,
                            isVerified = false,
                            badgeText = when (currentLanguage) {
                                AppLanguage.BENGALI -> "পর্যালোচনাধীন (Pending)"
                                AppLanguage.HINDI -> "समीक्षाधीन (Pending)"
                                AppLanguage.ENGLISH -> "Pending Review"
                            }
                        )
                        onSubmit(newNursery)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "নিবন্ধন জমা দিন"
                        AppLanguage.HINDI -> "पंजीकरण जमा करें"
                        AppLanguage.ENGLISH -> "Submit Registration"
                    }
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "বাতিল"
                        AppLanguage.HINDI -> "रद्द करें"
                        AppLanguage.ENGLISH -> "Cancel"
                    }
                )
            }
        }
    )
}

@Composable
fun ReportFraudDialog(
    farmerName: String,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSubmit: (FraudSeedReport) -> Unit
) {
    var shopName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var seedBrand by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var reporterPhone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReportProblem, contentDescription = null, tint = AgriAlertRed)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "নকল বীজ বা বিক্রেতা রিপোর্ট"
                        AppLanguage.HINDI -> "नकली बीज या विक्रेता रिपोर्ट"
                        AppLanguage.ENGLISH -> "Report Counterfeit Seed / Fraud"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = AgriAlertRed
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).imePadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "জাল বীজ, নকল প্যাকেট বা অঙ্কুরোদ্গম না হওয়ার বিষয়ে তথ্য দিন। অভিযোগ সংশ্লিষ্ট কৃষি অধিকর্তা দপ্তরে পাঠানো হবে।"
                        AppLanguage.HINDI -> "नकली बीज, जाली पैकेट या अंकुरण न होने की जानकारी दें। शिकायत संबंधित कृषि अधिकारी को भेजी जाएगी।"
                        AppLanguage.ENGLISH -> "Report counterfeit packets or zero-germination seeds. Reports are escalated to agriculture officers."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "দোকান বা বিক্রেতার নাম *"
                                AppLanguage.HINDI -> "दुकान या विक्रेता का नाम *"
                                AppLanguage.ENGLISH -> "Shop or Seller Name *"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = seedBrand,
                    onValueChange = { seedBrand = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "বীজের নাম বা ব্র্যান্ড"
                                AppLanguage.HINDI -> "बीज का नाम या ब्रांड"
                                AppLanguage.ENGLISH -> "Seed Name or Brand"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "দোকানের অবস্থান / বাজার"
                                AppLanguage.HINDI -> "दुकान का स्थान / बाजार"
                                AppLanguage.ENGLISH -> "Shop Location / Market"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = reporterPhone,
                    onValueChange = { reporterPhone = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "আপনার মোবাইল নম্বর"
                                AppLanguage.HINDI -> "आपका मोबाइल नंबर"
                                AppLanguage.ENGLISH -> "Your Mobile Number"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    singleLine = true
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "সমস্যার বিবরণ (সিল ভাঙা, ০% অঙ্কুরোদ্গম ইত্যাদি)"
                                AppLanguage.HINDI -> "समस्या का विवरण (टूटी सील, 0% अंकुरण आदि)"
                                AppLanguage.ENGLISH -> "Issue Details (broken seal, 0% germination, etc.)"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shopName.isNotBlank()) {
                        val report = FraudSeedReport(
                            reporterName = farmerName,
                            reporterPhone = reporterPhone,
                            sellerOrNurseryName = shopName,
                            location = location,
                            seedBrand = seedBrand,
                            complaintDetails = details
                        )
                        onSubmit(report)
                    }
                },
                modifier = Modifier.heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgriAlertRed, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "রিপোর্ট জমা দিন"
                        AppLanguage.HINDI -> "रिपोर्ट जमा करें"
                        AppLanguage.ENGLISH -> "Submit Report"
                    }
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(8.dp)) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "বাতিল"
                        AppLanguage.HINDI -> "रद्द करें"
                        AppLanguage.ENGLISH -> "Cancel"
                    }
                )
            }
        }
    )
}

// Helpers
private fun launchPhoneCall(context: Context, rawPhone: String, currentLanguage: AppLanguage) {
    val cleanNumber = rawPhone.replace(" ", "").replace("-", "")
    val fallback = when (currentLanguage) {
        AppLanguage.BENGALI -> "ফোন নম্বর: $rawPhone"
        AppLanguage.HINDI -> "फ़ोन नंबर: $rawPhone"
        AppLanguage.ENGLISH -> "Phone: $rawPhone"
    }
    safeStartActivity(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")), fallback)
}

private fun launchWhatsApp(context: Context, rawPhone: String, message: String, currentLanguage: AppLanguage) {
    val cleanPhone = rawPhone.replace("+", "").replace(" ", "").replace("-", "")
    val uri = Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode(message)}")
    val fallback = when (currentLanguage) {
        AppLanguage.BENGALI -> "WhatsApp নম্বর: $rawPhone"
        AppLanguage.HINDI -> "WhatsApp नंबर: $rawPhone"
        AppLanguage.ENGLISH -> "WhatsApp: $rawPhone"
    }
    safeStartActivity(context, Intent(Intent.ACTION_VIEW, uri), fallback)
}

private fun launchGoogleMaps(context: Context, locationQuery: String, currentLanguage: AppLanguage) {
    val geoIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(locationQuery)))
    if (!safeStartActivity(context, geoIntent)) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=" + Uri.encode(locationQuery)))
        val fallback = when (currentLanguage) {
            AppLanguage.BENGALI -> "ঠিকানা: $locationQuery"
            AppLanguage.HINDI -> "पता: $locationQuery"
            AppLanguage.ENGLISH -> "Address: $locationQuery"
        }
        safeStartActivity(context, webIntent, fallback)
    }
}
