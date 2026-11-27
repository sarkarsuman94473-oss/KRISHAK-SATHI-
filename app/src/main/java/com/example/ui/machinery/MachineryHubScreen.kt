package com.example.ui.machinery

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.datasource.MachineryImageSourceType
import com.example.data.datasource.MachineryPhotoRegistry
import com.example.data.model.AppLanguage
import com.example.data.model.BookingLifecycleStatus
import com.example.data.model.FarmServiceType
import com.example.data.model.MachineryCategory
import com.example.data.model.MachineryCondition
import com.example.data.model.MachineryProduct
import com.example.data.local.session.UserSessionManager
import com.example.data.repository.MachineryRepository
import com.example.data.repository.TrustAndSafetyRepository
import com.example.ui.components.OfflineModeBanner
import com.example.ui.theme.MachineryCardBadgeBg
import com.example.ui.trust.ActivityTimelineSection
import com.example.ui.theme.MachineryCardBadgeText
import com.example.ui.theme.MachineryCardBodyInk
import com.example.ui.theme.MachineryCardBorder
import com.example.ui.theme.MachineryCardSoftMint
import com.example.ui.theme.MachineryCardTitleInk
import com.example.ui.theme.MachineryCardWarnBg
import com.example.ui.theme.MachineryCardWarnText
import com.example.ui.theme.MachineryCardWhite
import com.example.ui.theme.RoyalAlertBg
import com.example.ui.theme.RoyalAlertText
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.RoyalWarningBg
import com.example.ui.theme.RoyalWarningText
import com.example.ui.theme.royalOutlinedTextFieldColors
import com.example.util.L10n
import com.example.util.NetworkConnectivityMonitor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MachineryHubScreen(
    currentLanguage: AppLanguage,
    userId: String,
    farmerName: String,
    onBack: () -> Unit,
    onOpenDealers: () -> Unit,
    onOpenFullAi: () -> Unit,
    modifier: Modifier = Modifier,
    repository: MachineryRepository? = null,
    onOpenManufacturers: () -> Unit = {},
    onOpenProductDetail: ((MachineryProduct) -> Unit)? = null,
    initialCategoryCode: String? = null,
    initialTabIndex: Int = 0
) {
    var selectedMachineDetail by remember { mutableStateOf<MachineryProduct?>(null) }

    BackHandler {
        if (selectedMachineDetail != null) {
            selectedMachineDetail = null
        } else {
            onBack()
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember(repository) { repository ?: MachineryRepository(context) }
    val trustRepo = remember(context) { TrustAndSafetyRepository(context) }
    val networkStatus by NetworkConnectivityMonitor.status.collectAsState()
    var syncErrorMessage by remember { mutableStateOf<String?>(null) }
    var isSyncing by remember { mutableStateOf(false) }

    fun refreshFromCloud() {
        isSyncing = true
        syncErrorMessage = null
        scope.launch {
            repo.ensureSeeded()
            if (UserSessionManager.hasCloudFirebaseSession(context)) {
                val pRes = repo.pullProductsResult()
                val rRes = repo.pullUserRequestsAndDealersFromFirestore(
                    userId = userId,
                    isAdmin = UserSessionManager.isAdmin(context)
                )
                val err = pRes.exceptionOrNull() ?: rRes.exceptionOrNull()
                if (err != null) {
                    syncErrorMessage = err.localizedMessage ?: "Cloud sync failed"
                }
            }
            isSyncing = false
        }
    }

    LaunchedEffect(repo, userId) {
        refreshFromCloud()
    }

    val publicProducts by repo.observePublicProducts().collectAsState(initial = emptyList())
    val favouriteIds by repo.observeFavouriteIds(userId).collectAsState(initial = emptySet())
    val myInquiries by repo.observeInquiriesForUser(userId).collectAsState(initial = emptyList())
    val myRentals by repo.observeRentalsForUser(userId).collectAsState(initial = emptyList())
    val myServices by repo.observeServicesForUser(userId).collectAsState(initial = emptyList())
    val allActivityTimelines by trustRepo.observeAllTimelines().collectAsState(initial = emptyList())

    var selectedTab by remember(initialTabIndex) { mutableIntStateOf(initialTabIndex.coerceIn(0, 3)) } // 0: Buy, 1: Rent, 2: Services, 3: My Requests & Saved
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryCode by remember(initialCategoryCode) { mutableStateOf<String?>(initialCategoryCode) }
    var selectedSubcategory by remember { mutableStateOf<String?>(null) }
    var selectedCondition by remember { mutableStateOf("ALL") }
    var favouritesOnly by remember { mutableStateOf(false) }
    var subsidyOnly by remember { mutableStateOf(false) }
    var originalPhotosOnly by remember { mutableStateOf(false) }

    val compareIds = remember { mutableStateListOf<String>() }
    var showCompareDialog by remember { mutableStateOf(false) }
    var fullScreenPhotoMachine by remember { mutableStateOf<MachineryProduct?>(null) }
    var selectedServiceForBooking by remember { mutableStateOf<FarmServiceType?>(null) }
    var showAiFinder by remember { mutableStateOf(false) }
    var aiFinderResults by remember { mutableStateOf<Pair<List<MachineryProduct>, String>?>(null) }

    val filteredProducts = remember(
        publicProducts, selectedTab, searchQuery, selectedCategoryCode,
        selectedSubcategory, selectedCondition, favouritesOnly, subsidyOnly,
        originalPhotosOnly, favouriteIds
    ) {
        publicProducts.filter { p ->
            val matchMode = when (selectedTab) {
                0 -> p.forSale
                1 -> p.forRent
                else -> true
            }
            val matchCategory = p.matchesCategory(selectedCategoryCode)
            val matchSub = selectedSubcategory == null || p.subcategory.contains(selectedSubcategory!!, ignoreCase = true)
            val matchCond = selectedCondition == "ALL" || p.condition.equals(selectedCondition, ignoreCase = true)
            val matchFav = !favouritesOnly || favouriteIds.contains(p.id)
            val matchSubsidy = !subsidyOnly || p.getSubsidyCodes().isNotEmpty()
            val matchOriginal = !originalPhotosOnly ||
                MachineryPhotoRegistry.getImageSourceType(p.getPhotoTokens().firstOrNull()) ==
                MachineryImageSourceType.UPLOADED_ORIGINAL
            matchMode && matchCategory && matchSub && matchCond && matchFav && matchSubsidy && matchOriginal && p.matchesSearch(searchQuery)
        }
    }

    val compareList = remember(publicProducts, compareIds.toList()) {
        publicProducts.filter { compareIds.contains(it.id) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                            modifier = Modifier.size(48.dp).testTag("machinery_hub_back_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = RoyalTextPrimary)
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "🚜 কৃষি যন্ত্র ও সেবা হাব"
                                    AppLanguage.HINDI -> "🚜 कृषि यंत्र एवं सेवा हब"
                                    AppLanguage.ENGLISH -> "🚜 Machinery & Services Hub"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "শিলিগুড়ি ও উত্তরবঙ্গ • ক্রয়, ভাড়া, ডিলার"
                                    AppLanguage.HINDI -> "सिलीगुड़ी व उत्तर बंगाल • खरीद, किराया, डीलर"
                                    AppLanguage.ENGLISH -> "Siliguri & North Bengal • Buy, Rent & Dealers"
                                },
                                fontSize = 11.sp,
                                color = RoyalSoftGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showAiFinder = true },
                            modifier = Modifier.size(48.dp).testTag("machinery_ai_finder_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Smart AI Machine Finder", tint = RoyalGold)
                        }
                        IconButton(
                            onClick = onOpenDealers,
                            modifier = Modifier.size(48.dp).testTag("machinery_dealers_btn")
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Siliguri Machinery Dealers", tint = RoyalSoftGold)
                        }
                    }
                )

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = RoyalGreenSecondaryBg,
                    contentColor = RoyalGold,
                    edgePadding = 12.dp,
                    divider = { HorizontalDivider(color = RoyalBorder) }
                ) {
                    val tabTitles = listOf(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "যন্ত্রপাতি ক্রয়"
                            AppLanguage.HINDI -> "मशीनरी खरीद"
                            AppLanguage.ENGLISH -> "Buy Machinery"
                        },
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "ভাড়া (Rent)"
                            AppLanguage.HINDI -> "किराया (Rent)"
                            AppLanguage.ENGLISH -> "Rent Machinery"
                        },
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "খামার সেবা (14)"
                            AppLanguage.HINDI -> "कृषि सेवाएं (14)"
                            AppLanguage.ENGLISH -> "Farm Services (14)"
                        },
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "সংরক্ষিত ও অনুরোধ"
                            AppLanguage.HINDI -> "सहेजे गए व अनुरोध"
                            AppLanguage.ENGLISH -> "Saved & Requests"
                        }
                    )
                    tabTitles.forEachIndexed { idx, title ->
                        Tab(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            modifier = Modifier.testTag("machinery_tab_$idx"),
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == idx) RoyalGold else RoyalTextSecondary
                                )
                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (compareList.size >= 2 && selectedTab in 0..1) {
                Surface(
                    color = RoyalGreenSecondaryBg,
                    border = BorderStroke(1.dp, RoyalGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${compareList.size}/3 selected for comparison",
                            color = RoyalSoftGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { compareIds.clear() },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) { Text("Clear", fontSize = 11.sp, color = RoyalTextPrimary) }
                            Button(
                                onClick = { showCompareDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = RoyalGreenPrimaryBg),
                                modifier = Modifier.heightIn(min = 48.dp).testTag("machinery_compare_open_btn")
                            ) {
                                Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Compare", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(RoyalGreenPrimaryBg)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            val gridColumns = remember(maxWidth) {
                when {
                    maxWidth >= 700.dp -> 3
                    maxWidth >= 340.dp -> 2
                    else -> 1
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 960.dp),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!networkStatus.isOnline) {
                    item {
                        OfflineModeBanner(
                            language = currentLanguage,
                            onRetryCheck = {
                                NetworkConnectivityMonitor.refreshNow(context)
                                refreshFromCloud()
                            }
                        )
                    }
                } else if (!syncErrorMessage.isNullOrBlank()) {
                    item {
                        Surface(
                            color = RoyalAlertBg,
                            border = BorderStroke(1.dp, RoyalAlertText.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("machinery_sync_error_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = syncErrorMessage ?: "",
                                    color = RoyalAlertText,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                androidx.compose.material3.TextButton(
                                    onClick = { refreshFromCloud() },
                                    enabled = !isSyncing
                                ) {
                                    Text(L10n.t("net_retry_btn", currentLanguage), color = RoyalSoftGold)
                                }
                            }
                        }
                    }
                }
                // Quick Action Banner: Siliguri Dealers Map + AI Smart Finder
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "উত্তরবঙ্গ ও শিলিগুড়ি কৃষি যন্ত্রপাতি ও সেবা ডিরেক্টরি"
                                    AppLanguage.HINDI -> "उत्तर बंगाल व सिलीगुड़ी कृषि यंत्र व सेवा निर्देशिका"
                                    AppLanguage.ENGLISH -> "North Bengal & Siliguri Agricultural Machinery Ecosystem"
                                },
                                fontWeight = FontWeight.Bold,
                                color = RoyalSoftGold,
                                fontSize = 13.sp
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onOpenDealers,
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("machinery_open_dealers_card_btn")
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "শিলিগুড়ি ডিলার ও ম্যাপ (১৬টি)"
                                            AppLanguage.HINDI -> "सिलीगुड़ी डीलर व मैप (16)"
                                            AppLanguage.ENGLISH -> "Siliguri Dealers & Map (16)"
                                        },
                                        fontSize = 12.sp
                                    )
                                }
                                OutlinedButton(
                                    onClick = { showAiFinder = true },
                                    border = BorderStroke(1.dp, RoyalGold),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "AI যন্ত্র নির্বাচন সহকারী"
                                            AppLanguage.HINDI -> "AI मशीन चयन सहायक"
                                            AppLanguage.ENGLISH -> "Smart AI Machine Finder"
                                        },
                                        fontSize = 12.sp,
                                        color = RoyalSoftGold
                                    )
                                }
                                OutlinedButton(
                                    onClick = onOpenManufacturers,
                                    border = BorderStroke(1.dp, RoyalGold),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("machinery_open_manufacturers_btn")
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = RoyalSoftGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "প্রস্তুতকারক পোর্টাল (১৯)"
                                            AppLanguage.HINDI -> "निर्माता पोर्टल (19)"
                                            AppLanguage.ENGLISH -> "OEM Portals (19)"
                                        },
                                        fontSize = 12.sp,
                                        color = RoyalSoftGold
                                    )
                                }
                            }
                        }
                    }
                }

                when (selectedTab) {
                    0, 1 -> {
                        // Search & Filters
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "ট্রাক্টর, টিলার, ড্রোন, পাম্প বা ফসল খুঁজুন..."
                                            AppLanguage.HINDI -> "ट्रैक्टर, टिलर, ड्रोन, पंप या फसल खोजें..."
                                            AppLanguage.ENGLISH -> "Search tractors, power tillers, pumps, drones, sprayers..."
                                        },
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(48.dp)) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                singleLine = true,
                                colors = royalOutlinedTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("machinery_search_input")
                            )
                        }

                        // 10 Category Selector
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = selectedCategoryCode == null,
                                        onClick = {
                                            selectedCategoryCode = null
                                            selectedSubcategory = null
                                        },
                                        label = { Text("All Categories (10)", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RoyalGreenEmerald,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                                items(MachineryCategory.values().toList()) { cat ->
                                    FilterChip(
                                        selected = selectedCategoryCode == cat.code,
                                        onClick = {
                                            selectedCategoryCode = if (selectedCategoryCode == cat.code) null else cat.code
                                            selectedSubcategory = null
                                        },
                                        label = { Text("${cat.emoji} ${cat.getDisplayName(currentLanguage)}", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RoyalGreenEmerald,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        // Subcategory chips when a category is selected
                        selectedCategoryCode?.let { catCode ->
                            val catEnum = MachineryCategory.fromCode(catCode)
                            item {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(catEnum.getSubcategories(currentLanguage)) { sub ->
                                        FilterChip(
                                            selected = selectedSubcategory == sub,
                                            onClick = { selectedSubcategory = if (selectedSubcategory == sub) null else sub },
                                            label = { Text(sub, fontSize = 10.sp) }
                                        )
                                    }
                                }
                            }
                        }

                        // Condition + Favourites filter row
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("ALL", MachineryCondition.NEW.code, MachineryCondition.USED.code, MachineryCondition.REFURBISHED.code).forEach { cond ->
                                    item {
                                        FilterChip(
                                            selected = selectedCondition == cond,
                                            onClick = { selectedCondition = cond },
                                            label = {
                                                Text(
                                                    text = if (cond == "ALL") "All Condition" else cond,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        )
                                    }
                                }
                                item {
                                    FilterChip(
                                        selected = favouritesOnly,
                                        onClick = { favouritesOnly = !favouritesOnly },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (favouritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = RoyalGold
                                            )
                                        },
                                        label = { Text("Saved (${favouriteIds.size})", fontSize = 10.sp) }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = subsidyOnly,
                                        onClick = { subsidyOnly = !subsidyOnly },
                                        label = { Text("SMAM / Subsidy", fontSize = 10.sp) },
                                        modifier = Modifier.testTag("filter_subsidy_chip")
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = originalPhotosOnly,
                                        onClick = { originalPhotosOnly = !originalPhotosOnly },
                                        label = { Text("Original Photos", fontSize = 10.sp) },
                                        modifier = Modifier.testTag("filter_original_photos_chip")
                                    )
                                }
                            }
                        }

                        if (filteredProducts.isEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                    border = BorderStroke(1.dp, RoyalBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "No matching machinery found",
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalTextPrimary
                                        )
                                        Text(
                                            text = "Reset filters or ask Smart AI Machine Finder for recommendations.",
                                            fontSize = 12.sp,
                                            color = RoyalTextSecondary
                                        )
                                    }
                                }
                            }
                        } else {
                            val productRows = filteredProducts.chunked(gridColumns)
                            items(
                                items = productRows,
                                key = { rowItems -> rowItems.joinToString("_") { it.id } }
                            ) { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEach { machine ->
                                        val isFav = favouriteIds.contains(machine.id)
                                        val isCompared = compareIds.contains(machine.id)
                                        MachineryProductCard(
                                            machine = machine,
                                            currentLanguage = currentLanguage,
                                            isFavourite = isFav,
                                            isCompared = isCompared,
                                            isCompactGrid = gridColumns > 1,
                                            onToggleFavourite = {
                                                scope.launch { repo.toggleFavourite(userId, machine.id, isFav) }
                                            },
                                            onToggleCompare = {
                                                if (isCompared) {
                                                    compareIds.remove(machine.id)
                                                } else if (compareIds.size < 3) {
                                                    compareIds.add(machine.id)
                                                } else {
                                                    Toast.makeText(context, "Maximum 3 machines can be compared side-by-side", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onZoomPhoto = {
                                                if (machine.getPhotoTokens().isNotEmpty()) {
                                                    fullScreenPhotoMachine = machine
                                                } else if (onOpenProductDetail != null) {
                                                    onOpenProductDetail(machine)
                                                } else {
                                                    selectedMachineDetail = machine
                                                }
                                            },
                                            onClick = {
                                                if (onOpenProductDetail != null) {
                                                    onOpenProductDetail(machine)
                                                } else {
                                                    selectedMachineDetail = machine
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(gridColumns - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // All 14 Farm Services
                        item {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "খামার পরিষেবা বুকিং ও অনুরোধ (১৪টি বিভাগ)"
                                    AppLanguage.HINDI -> "कृषि सेवाएं व बुकिंग अनुरोध (14 श्रेणियां)"
                                    AppLanguage.ENGLISH -> "Agricultural Field & Installation Services (14 Types)"
                                },
                                fontWeight = FontWeight.Bold,
                                color = RoyalSoftGold,
                                fontSize = 14.sp
                            )
                        }
                        items(FarmServiceType.values().toList(), key = { it.code }) { svc ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                border = BorderStroke(1.dp, RoyalBorder),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().clickable { selectedServiceForBooking = svc }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(svc.emoji, fontSize = 24.sp)
                                        Column {
                                            Text(
                                                text = svc.getDisplayName(currentLanguage),
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalTextPrimary,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "Request local provider • Confirmed directly by verified partner",
                                                fontSize = 11.sp,
                                                color = RoyalTextSecondary
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = { selectedServiceForBooking = svc },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Text("Book", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // My Saved Machines & Submitted Requests
                        val savedMachines = publicProducts.filter { favouriteIds.contains(it.id) }
                        item {
                            Text(
                                text = "Saved Machines (${savedMachines.size})",
                                fontWeight = FontWeight.Bold,
                                color = RoyalSoftGold,
                                fontSize = 14.sp
                            )
                        }
                        if (savedMachines.isEmpty()) {
                            item {
                                Text("Tap the heart icon on any machine to bookmark it here.", fontSize = 12.sp, color = RoyalTextSecondary)
                            }
                        } else {
                            val savedRows = savedMachines.chunked(gridColumns)
                            items(
                                items = savedRows,
                                key = { rowItems -> "fav_${rowItems.joinToString("_") { it.id }}" }
                            ) { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEach { machine ->
                                        MachineryProductCard(
                                            machine = machine,
                                            currentLanguage = currentLanguage,
                                            isFavourite = true,
                                            isCompared = compareIds.contains(machine.id),
                                            isCompactGrid = gridColumns > 1,
                                            onToggleFavourite = { scope.launch { repo.toggleFavourite(userId, machine.id, true) } },
                                            onToggleCompare = {
                                                if (compareIds.contains(machine.id)) {
                                                    compareIds.remove(machine.id)
                                                } else if (compareIds.size < 3) {
                                                    compareIds.add(machine.id)
                                                }
                                            },
                                            onZoomPhoto = {
                                                if (machine.getPhotoTokens().isNotEmpty()) {
                                                    fullScreenPhotoMachine = machine
                                                } else if (onOpenProductDetail != null) {
                                                    onOpenProductDetail(machine)
                                                } else {
                                                    selectedMachineDetail = machine
                                                }
                                            },
                                            onClick = {
                                                if (onOpenProductDetail != null) {
                                                    onOpenProductDetail(machine)
                                                } else {
                                                    selectedMachineDetail = machine
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(gridColumns - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        item {
                            HorizontalDivider(color = RoyalBorder)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "My Quotations & Reports (${myInquiries.size})",
                                fontWeight = FontWeight.Bold,
                                color = RoyalSoftGold,
                                fontSize = 14.sp
                            )
                        }
                        items(myInquiries, key = { it.id }) { inq ->
                            RequestStatusCard(
                                title = "${inq.inquiryType}: ${inq.machineName}",
                                subtitle = inq.message,
                                status = inq.status,
                                extra = inq.sellerReply.takeIf { it.isNotBlank() }?.let { "Seller Reply: $it" }
                            )
                        }

                        item {
                            Text(
                                text = "My Rental Requests (${myRentals.size})",
                                fontWeight = FontWeight.Bold,
                                color = RoyalSoftGold,
                                fontSize = 14.sp
                            )
                        }
                        items(myRentals, key = { it.id }) { rent ->
                            val canCancel = rent.status in setOf(
                                BookingLifecycleStatus.REQUESTED_AWAITING_CONFIRMATION,
                                BookingLifecycleStatus.PENDING_SYNC,
                                BookingLifecycleStatus.ACCEPTED,
                                BookingLifecycleStatus.CONFIRMED
                            )
                            RequestStatusCard(
                                title = "Rental: ${rent.machineName}",
                                subtitle = "${rent.durationUnits} x ${rent.rentalUnit} • ${rent.locality} • Operator: ${if (rent.withOperator) "Yes" else "No"}",
                                status = rent.status,
                                extra = rent.notes.takeIf { it.isNotBlank() },
                                onCancel = if (canCancel) {
                                    {
                                        scope.launch {
                                            val res = repo.cancelRentalRequest(rent.id, userId)
                                            Toast.makeText(
                                                context,
                                                if (res.isSuccess) "Rental booking cancelled" else (res.exceptionOrNull()?.message ?: "Cancel failed"),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else null
                            )
                        }

                        item {
                            Text(
                                text = "My Farm Service Bookings (${myServices.size})",
                                fontWeight = FontWeight.Bold,
                                color = RoyalSoftGold,
                                fontSize = 14.sp
                            )
                        }
                        items(myServices, key = { it.id }) { srv ->
                            val canCancel = srv.status in setOf(
                                BookingLifecycleStatus.REQUESTED_AWAITING_CONFIRMATION,
                                BookingLifecycleStatus.PENDING_SYNC,
                                BookingLifecycleStatus.ACCEPTED,
                                BookingLifecycleStatus.CONFIRMED
                            )
                            RequestStatusCard(
                                title = "Service: ${srv.serviceTypeCode}",
                                subtitle = "${srv.cropName} • ${srv.landSizeText} • ${srv.locality}",
                                status = srv.status,
                                extra = srv.preferredDate.takeIf { it.isNotBlank() }?.let { "Preferred: $it" },
                                onCancel = if (canCancel) {
                                    {
                                        scope.launch {
                                            val res = repo.cancelServiceRequest(srv.id, userId)
                                            Toast.makeText(
                                                context,
                                                if (res.isSuccess) "Service booking cancelled" else (res.exceptionOrNull()?.message ?: "Cancel failed"),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else null
                            )
                        }
                        val myEntityIds = (myInquiries.map { it.id } + myRentals.map { it.id } + myServices.map { it.id }).toSet()
                        val myActivityLogs = allActivityTimelines.filter { it.entityId in myEntityIds }
                        if (myActivityLogs.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                                ActivityTimelineSection(logs = myActivityLogs, lang = currentLanguage)
                            }
                        }
                    }
                }
            }
        }
    }

    selectedMachineDetail?.let { machine ->
        val isFav = favouriteIds.contains(machine.id)
        val isComp = compareIds.contains(machine.id)
        Surface(modifier = Modifier.fillMaxSize()) {
            MachineryProductDetailScreen(
                machine = machine,
                currentLanguage = currentLanguage,
                userId = userId,
                farmerName = farmerName,
                repository = repo,
                isFavourite = isFav,
                isCompared = isComp,
                onToggleFavourite = {
                    scope.launch { repo.toggleFavourite(userId, machine.id, isFav) }
                },
                onToggleCompare = {
                    if (isComp) {
                        compareIds.remove(machine.id)
                    } else if (compareIds.size < 3) {
                        compareIds.add(machine.id)
                    } else {
                        Toast.makeText(context, "Maximum 3 machines can be compared side-by-side", Toast.LENGTH_SHORT).show()
                    }
                },
                onOpenDealers = {
                    selectedMachineDetail = null
                    onOpenDealers()
                },
                onHireService = { svc ->
                    selectedMachineDetail = null
                    selectedServiceForBooking = svc
                },
                onBack = { selectedMachineDetail = null }
            )
        }
    }

    fullScreenPhotoMachine?.let { zoomedMachine ->
        MachineryFullScreenImageViewer(
            photoTokens = zoomedMachine.getPhotoTokens(),
            title = zoomedMachine.getDisplayName(currentLanguage),
            subtitle = zoomedMachine.getDisplayBrandModel(currentLanguage),
            onDismiss = { fullScreenPhotoMachine = null }
        )
    }

    if (showCompareDialog && compareList.isNotEmpty()) {
        MachineryCompareDialog(
            machines = compareList,
            currentLanguage = currentLanguage,
            onDismiss = { showCompareDialog = false }
        )
    }

    selectedServiceForBooking?.let { svc ->
        ServiceBookingDialog(
            serviceType = svc,
            currentLanguage = currentLanguage,
            userId = userId,
            farmerName = farmerName,
            onDismiss = { selectedServiceForBooking = null },
            onSubmit = { req ->
                scope.launch {
                    val res = repo.submitServiceRequest(req)
                    Toast.makeText(
                        context,
                        if (res.isSuccess) "Service request recorded (REQUESTED_AWAITING_CONFIRMATION)" else (res.exceptionOrNull()?.message ?: "Duplicate or failed request"),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    if (showAiFinder) {
        SmartAiMachineFinderDialog(
            currentLanguage = currentLanguage,
            results = aiFinderResults,
            onRunRecommendation = { query ->
                scope.launch {
                    aiFinderResults = repo.recommendMachinesForQuery(query, currentLanguage)
                }
            },
            onOpenFullAi = {
                showAiFinder = false
                onOpenFullAi()
            },
            onDismiss = { showAiFinder = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MachineryProductCard(
    machine: MachineryProduct,
    currentLanguage: AppLanguage,
    isFavourite: Boolean,
    isCompared: Boolean,
    isCompactGrid: Boolean = true,
    onToggleFavourite: () -> Unit,
    onToggleCompare: () -> Unit,
    onZoomPhoto: () -> Unit = {},
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photos = remember(machine.photosCsv, machine.firebaseStoragePathsCsv) { machine.getPhotoTokens() }
    val cat = remember(machine.categoryCode) { machine.getCategoryEnum() }
    val purposeText = remember(machine.purposeEn, machine.purposeBn, machine.purposeHi, currentLanguage) {
        machine.getDisplayPurpose(currentLanguage)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MachineryCardWhite),
        border = BorderStroke(if (isCompared) 2.dp else 1.dp, if (isCompared) RoyalGold else MachineryCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("machinery_card_${machine.id}")
    ) {
        Column(
            modifier = Modifier.padding(if (isCompactGrid) 10.dp else 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                MachineryEquipmentImage(
                    photoTokens = photos,
                    fallbackEmoji = cat.emoji,
                    contentDescription = machine.getDisplayName(currentLanguage),
                    enableSwipePager = false,
                    showZoomHint = photos.isNotEmpty(),
                    onImageClick = { onZoomPhoto() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCompactGrid) 126.dp else 160.dp)
                        .testTag("machinery_card_photo_${machine.id}")
                )

                Surface(
                    color = MachineryCardWhite.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
                ) {
                    IconButton(
                        onClick = onToggleFavourite,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save machine",
                            tint = if (isFavourite) RoyalGold else MachineryCardBodyInk,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = machine.getDisplayName(currentLanguage),
                fontWeight = FontWeight.Bold,
                color = MachineryCardTitleInk,
                fontSize = if (isCompactGrid) 13.sp else 15.sp,
                lineHeight = if (isCompactGrid) 17.sp else 19.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = machine.getDisplayBrandModel(currentLanguage),
                fontSize = 10.sp,
                color = MachineryCardBodyInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MachinerySourceProvenanceBadge(photoTokens = photos, lang = currentLanguage)
                Surface(
                    color = if (machine.pendingAdminReview) MachineryCardWarnBg else MachineryCardBadgeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            tint = if (machine.pendingAdminReview) MachineryCardWarnText else MachineryCardBadgeText,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = if (machine.pendingAdminReview) {
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "রিভিউ বাকি"
                                    AppLanguage.HINDI -> "समीक्षा बाकी"
                                    AppLanguage.ENGLISH -> "Admin Review"
                                }
                            } else {
                                cat.emoji
                            },
                            fontSize = 9.sp,
                            color = if (machine.pendingAdminReview) MachineryCardWarnText else MachineryCardBadgeText,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
                if (machine.forSale) {
                    Surface(color = MachineryCardSoftMint, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ক্রয়"
                                AppLanguage.HINDI -> "खरीद"
                                AppLanguage.ENGLISH -> "Buy"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MachineryCardTitleInk,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (machine.forRent) {
                    Surface(color = MachineryCardWarnBg, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ভাড়া"
                                AppLanguage.HINDI -> "किराया"
                                AppLanguage.ENGLISH -> "Rent"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MachineryCardWarnText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (purposeText.isNotBlank()) {
                Text(
                    text = purposeText,
                    fontSize = 10.sp,
                    color = MachineryCardBodyInk,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = machine.getDisplaySuitableCrops(currentLanguage),
                    fontSize = 10.sp,
                    color = MachineryCardBodyInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = machine.salePriceInr?.let { "₹${it.toInt()}" }
                    ?: when (currentLanguage) {
                        AppLanguage.BENGALI -> "দাম ও ডিলার কোটেশন জানতে দেখুন"
                        AppLanguage.HINDI -> "मूल्य व डीलर कोटेशन के लिए देखें"
                        AppLanguage.ENGLISH -> "Price on request"
                    },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MachineryCardBadgeText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable(onClick = onToggleCompare)
                ) {
                    Checkbox(
                        checked = isCompared,
                        onCheckedChange = { onToggleCompare() },
                        modifier = Modifier.size(34.dp)
                    )
                    Text("Compare", fontSize = 10.sp, color = MachineryCardTitleInk, maxLines = 1)
                }
                OutlinedButton(
                    onClick = onClick,
                    border = BorderStroke(1.dp, MachineryCardBadgeText),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(12.dp), tint = MachineryCardBadgeText)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "বিস্তারিত"
                            AppLanguage.HINDI -> "विवरण"
                            AppLanguage.ENGLISH -> "Details"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MachineryCardBadgeText,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestStatusCard(
    title: String,
    subtitle: String,
    status: String,
    extra: String?,
    onCancel: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        border = BorderStroke(1.dp, RoyalBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, color = RoyalTextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Surface(color = RoyalWarningBg, shape = RoundedCornerShape(6.dp)) {
                    Text(status, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RoyalWarningText, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            if (subtitle.isNotBlank()) {
                Text(subtitle, fontSize = 11.sp, color = RoyalTextSecondary)
            }
            extra?.let {
                Text(it, fontSize = 11.sp, color = RoyalSoftGold)
            }
            onCancel?.let { cancelAction ->
                OutlinedButton(
                    onClick = cancelAction,
                    border = BorderStroke(1.dp, RoyalGold),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Cancel Request", fontSize = 11.sp, color = RoyalSoftGold)
                }
            }
        }
    }
}
