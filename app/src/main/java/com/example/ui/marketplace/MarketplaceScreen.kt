package com.example.ui.marketplace

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.datasource.CropMasterDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.NurseryOrder
import com.example.data.model.NurseryOrderItem
import com.example.data.model.NurseryProduct
import com.example.data.model.NurseryProductCategory
import com.example.data.model.ProduceListing
import com.example.data.remote.FirestoreRepository
import com.example.data.remote.NurseryProductRepository
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriPriceGold
import com.example.util.L10n
import com.example.util.isCompactNarrowScreen
import com.example.util.safeStartActivity
import com.example.util.toLocalizedDoubleOrNull
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Warning
import com.example.data.local.entity.FraudTargetType
import com.example.data.local.session.UserSessionManager
import com.example.data.model.NurseryApplication
import com.example.data.model.NurseryApplicationStatus
import com.example.data.remote.NurseryApplicationRepository
import com.example.data.repository.TrustAndSafetyRepository
import com.example.ui.components.OfflineModeBanner
import com.example.ui.trust.AntiFraudWarningBanner
import com.example.ui.trust.FraudReportDialog
import com.example.ui.trust.SellerTierBadge
import com.example.util.NetworkConnectivityMonitor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

fun findProduceImage(productName: String): Int? {
    val query = productName.trim().lowercase()
    return CropMasterDatabase.crops.find { crop ->
        crop.nameBn.contains(query, ignoreCase = true) ||
        crop.nameEn.contains(query, ignoreCase = true) ||
        crop.nameHi.contains(query, ignoreCase = true) ||
        query.contains(crop.nameBn) ||
        query.contains(crop.nameEn, ignoreCase = true) ||
        query.contains(crop.cropId)
    }?.getResolvedImageResId()
}

private fun localizeProduceName(raw: String, lang: AppLanguage): String {
    val query = raw.trim().lowercase()
    if (query.isBlank()) return raw
    val match = CropMasterDatabase.crops.find { crop ->
        crop.cropId.equals(query, ignoreCase = true) ||
        crop.nameBn.equals(raw.trim(), ignoreCase = true) ||
        crop.nameEn.equals(raw.trim(), ignoreCase = true) ||
        crop.nameHi.equals(raw.trim(), ignoreCase = true) ||
        query.contains(crop.nameBn.lowercase()) ||
        query.contains(crop.nameEn.lowercase())
    }
    return match?.getName(lang) ?: raw
}

private fun localizeMarketplaceStatus(status: String, lang: AppLanguage): String {
    return when (status.uppercase()) {
        "ACTIVE" -> when (lang) {
            AppLanguage.BENGALI -> "বিক্রির জন্য প্রস্তুত"
            AppLanguage.HINDI -> "बिक्री के लिए तैयार"
            AppLanguage.ENGLISH -> "Ready for Sale"
        }
        "SOLD" -> when (lang) {
            AppLanguage.BENGALI -> "বিক্রিত"
            AppLanguage.HINDI -> "बिका हुआ"
            AppLanguage.ENGLISH -> "Sold"
        }
        "PENDING" -> when (lang) {
            AppLanguage.BENGALI -> "অপেক্ষমাণ"
            AppLanguage.HINDI -> "लंबित"
            AppLanguage.ENGLISH -> "Pending"
        }
        "COUNTERED" -> when (lang) {
            AppLanguage.BENGALI -> "পাল্টা প্রস্তাব"
            AppLanguage.HINDI -> "जवाबी प्रस्ताव"
            AppLanguage.ENGLISH -> "Countered"
        }
        "ACCEPTED" -> when (lang) {
            AppLanguage.BENGALI -> "গৃহীত"
            AppLanguage.HINDI -> "स्वीकृत"
            AppLanguage.ENGLISH -> "Accepted"
        }
        "REJECTED" -> when (lang) {
            AppLanguage.BENGALI -> "বাতিল"
            AppLanguage.HINDI -> "अस्वीकृत"
            AppLanguage.ENGLISH -> "Rejected"
        }
        "CONFIRMED" -> when (lang) {
            AppLanguage.BENGALI -> "নিশ্চিত"
            AppLanguage.HINDI -> "पुष्ट (Confirmed)"
            AppLanguage.ENGLISH -> "Confirmed"
        }
        else -> status
    }
}

private fun localizeUnit(unit: String, lang: AppLanguage): String {
    return when (unit.trim().uppercase()) {
        "KG", "কেজি", "किग्रा" -> when (lang) {
            AppLanguage.BENGALI -> "কেজি"
            AppLanguage.HINDI -> "किग्रा"
            AppLanguage.ENGLISH -> "kg"
        }
        "QUINTAL", "কুইন্টাল", "क्विंटल" -> when (lang) {
            AppLanguage.BENGALI -> "কুইন্টাল"
            AppLanguage.HINDI -> "क्विंटल"
            AppLanguage.ENGLISH -> "Quintal"
        }
        else -> unit
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarketplaceScreen(
    currentLanguage: AppLanguage,
    userId: String,
    farmerName: String,
    onBack: () -> Unit,
    onSellClick: () -> Unit,
    onOpenMachineryHub: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { FirestoreRepository(context) }
    val nurseryProductRepo = remember { NurseryProductRepository(context) }
    val nurseryAppRepo = remember { NurseryApplicationRepository(context) }
    val trustRepo = remember { TrustAndSafetyRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    var processingOfferId by remember { mutableStateOf<String?>(null) }

    val listings by repository.observeProduceListings().collectAsState(initial = emptyList())
    val offers by repository.observeBuyerOffers(userId).collectAsState(initial = emptyList())
    val orders by repository.observeOrders(userId).collectAsState(initial = emptyList())
    val approvedNurseryProducts by nurseryProductRepo.observeApprovedMarketplaceProducts().collectAsState(initial = emptyList())
    val nurseryApps by nurseryAppRepo.allApplicationsFlow.collectAsState(initial = emptyList())
    val suspendedSellerIds by trustRepo.observeSuspendedSellerIds().collectAsState(initial = emptySet())

    val networkStatus by NetworkConnectivityMonitor.status.collectAsState()
    var syncErrorMessage by remember { mutableStateOf<String?>(null) }
    var isSyncing by remember { mutableStateOf(false) }

    fun refreshFromCloud() {
        if (!UserSessionManager.hasCloudFirebaseSession(context)) return
        isSyncing = true
        syncErrorMessage = null
        coroutineScope.launch {
            val res = nurseryProductRepo.pullFromFirestore(
                sellerUserId = "",
                isAdmin = UserSessionManager.isAdmin(context)
            )
            isSyncing = false
            if (res.isFailure) {
                syncErrorMessage = res.exceptionOrNull()?.localizedMessage ?: "Cloud sync failed"
            }
        }
    }

    LaunchedEffect(userId) {
        refreshFromCloud()
    }

    var orderingProduct by remember { mutableStateOf<NurseryProduct?>(null) }
    var viewingSellerProfile by remember { mutableStateOf<NurseryProduct?>(null) }
    var reportingProduct by remember { mutableStateOf<NurseryProduct?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("marketplace_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🧺 ${L10n.t("marketplace_title", currentLanguage)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = onSellClick,
                    containerColor = AgriGreenPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("sell_produce_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Sell Produce")
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
            if (!networkStatus.isOnline) {
                OfflineModeBanner(
                    language = currentLanguage,
                    onRetryCheck = {
                        NetworkConnectivityMonitor.refreshNow(context)
                        refreshFromCloud()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            } else if (!syncErrorMessage.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("marketplace_sync_error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = syncErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { refreshFromCloud() }, enabled = !isSyncing) {
                            Text(L10n.t("net_retry_btn", currentLanguage))
                        }
                    }
                }
            }
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clickable(onClick = onOpenMachineryHub)
                    .testTag("marketplace_machinery_shortcut_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "🚜 কৃষি যন্ত্রপাতি, ভাড়া ও ডিলার হাব"
                                    AppLanguage.HINDI -> "🚜 कृषि मशीनरी, किराया व डीलर हब"
                                    AppLanguage.ENGLISH -> "🚜 Farm Machinery, Rentals & Dealers"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "শিলিগুড়ি ও উত্তরবঙ্গ: ট্রাক্টর, টিলার, পাম্প, ড্রোন"
                                    AppLanguage.HINDI -> "सिलीगुड़ी व उत्तर बंगाल: ट्रैक्टर, टिलर, पंप, ड्रोन"
                                    AppLanguage.ENGLISH -> "Tractors, Power Tillers, Pumps, Drones & Services"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    FilledTonalButton(
                        onClick = onOpenMachineryHub,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "খুলুন"
                                AppLanguage.HINDI -> "खोलें"
                                AppLanguage.ENGLISH -> "Open"
                            },
                            fontSize = 11.sp
                        )
                    }
                }
            }
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(L10n.t("market_tab_nursery_plants", currentLanguage), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(L10n.t("market_tab_farmer_produce", currentLanguage), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "দরদাম ও প্রস্তাব (${offers.size})"
                                AppLanguage.HINDI -> "मोलभाव व प्रस्ताव (${offers.size})"
                                AppLanguage.ENGLISH -> "Negotiation & Offers (${offers.size})"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "আমার অর্ডার (${orders.size})"
                                AppLanguage.HINDI -> "मेरे ऑर्डर (${orders.size})"
                                AppLanguage.ENGLISH -> "My Orders (${orders.size})"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .imePadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Approved Nursery Products Marketplace View
                            NurseryCustomerMarketplaceView(
                                products = approvedNurseryProducts,
                                nurseryApps = nurseryApps,
                                suspendedSellerIds = suspendedSellerIds,
                                currentLanguage = currentLanguage,
                                onOrderClick = { orderingProduct = it },
                                onProfileClick = { viewingSellerProfile = it },
                                onReportClick = { reportingProduct = it }
                            )
                        }
                        1 -> {
                            // Produce listings
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    AntiFraudWarningBanner(language = currentLanguage)
                                }
                                item {
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Store,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "সরাসরি কৃষক থেকে যাচাইকৃত পাইকার ও খুচরো ক্রেতাদের কাছে ফসল বিক্রির সুবিধা।"
                                                    AppLanguage.HINDI -> "किसानों से सीधे सत्यापित थोक व खुदरा खरीदारों को फसल बेचने की सुविधा।"
                                                    AppLanguage.ENGLISH -> "Sell your harvest directly from farm to verified wholesalers and retail buyers."
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }

                                if (listings.isEmpty()) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "বর্তমানে কোনো ফসল তালিকাভুক্ত নেই।"
                                                        AppLanguage.HINDI -> "वर्तमान में कोई फसल सूचीबद्ध नहीं है।"
                                                        AppLanguage.ENGLISH -> "No produce listings available right now."
                                                    },
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Button(
                                                    onClick = onSellClick,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = AgriGreenPrimary,
                                                        contentColor = Color.White
                                                    ),
                                                    modifier = Modifier.heightIn(min = 48.dp)
                                                ) {
                                                    Text(
                                                        when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "আমার ফসল বিক্রি করতে দিন"
                                                            AppLanguage.HINDI -> "मेरी फसल बेचने के लिए डालें"
                                                            AppLanguage.ENGLISH -> "List My Produce for Sale"
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    items(listings) { item ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("listing_card_${item.id}"),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            val imgRes = findProduceImage(item.product)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (imgRes != null) {
                                                    Image(
                                                        painter = painterResource(id = imgRes),
                                                        contentDescription = item.product,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(64.dp)
                                                            .clip(RoundedCornerShape(12.dp))
                                                    )
                                                }
                                                val localizedUnit = localizeUnit(item.unit, currentLanguage)
                                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = localizeProduceName(item.product, currentLanguage),
                                                            style = MaterialTheme.typography.titleLarge,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (item.status == "ACTIVE") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                                        ) {
                                                            Text(
                                                                text = localizeMarketplaceStatus(item.status, currentLanguage),
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 12.sp,
                                                                color = if (item.status == "ACTIVE") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }

                                                    Text(
                                                        text = when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "পরিমাণ: ${item.quantity} $localizedUnit • প্রত্যাশিত দাম: ₹${item.expectedPricePerUnit} / $localizedUnit"
                                                            AppLanguage.HINDI -> "मात्रा: ${item.quantity} $localizedUnit • अपेक्षित भाव: ₹${item.expectedPricePerUnit} / $localizedUnit"
                                                            AppLanguage.ENGLISH -> "Qty: ${item.quantity} $localizedUnit • Expected Rate: ₹${item.expectedPricePerUnit} / $localizedUnit"
                                                        },
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "কৃষক: ${item.farmerName} • এলাকা: ${item.location}"
                                                            AppLanguage.HINDI -> "किसान: ${item.farmerName} • स्थान: ${item.location}"
                                                            AppLanguage.ENGLISH -> "Farmer: ${item.farmerName} • Location: ${item.location}"
                                                        },
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    if (item.harvestDate.isNotBlank()) {
                                                        Text(
                                                            text = when (currentLanguage) {
                                                                AppLanguage.BENGALI -> "তোলার তারিখ: ${item.harvestDate}"
                                                                AppLanguage.HINDI -> "कटाई की तारीख: ${item.harvestDate}"
                                                                AppLanguage.ENGLISH -> "Harvest Date: ${item.harvestDate}"
                                                            },
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            // Buyer offers & Negotiation
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (offers.isEmpty()) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Text(
                                                text = when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "এখনও কোনো দরদাম বা অফার আসেনি। নতুন ক্রেতারা প্রস্তাব দিলে এখানে দেখা যাবে।"
                                                    AppLanguage.HINDI -> "अभी तक कोई मोलभाव या प्रस्ताव नहीं आया है। नए खरीदारों के प्रस्ताव यहाँ दिखेंगे।"
                                                    AppLanguage.ENGLISH -> "No offers or negotiations yet. New buyer offers will appear here."
                                                },
                                                modifier = Modifier.padding(20.dp),
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    items(offers) { offer ->
                                        val isProcessing = processingOfferId == offer.id
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("offer_card_${offer.id}"),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Store,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = offer.buyerName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 16.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    val locBuyerType = if (offer.buyerType.contains("Wholesaler", ignoreCase = true)) {
                                                        when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "পাইকার (আড়তদার)"
                                                            AppLanguage.HINDI -> "थोक खरीदार"
                                                            AppLanguage.ENGLISH -> "Wholesaler"
                                                        }
                                                    } else offer.buyerType
                                                    Text(
                                                        text = locBuyerType,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Text(
                                                    text = when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "ক্রেতার প্রস্তাবিত দর: ₹${offer.offeredPricePerUnit} / ইউনিট"
                                                        AppLanguage.HINDI -> "खरीदार का प्रस्तावित भाव: ₹${offer.offeredPricePerUnit} / इकाई"
                                                        AppLanguage.ENGLISH -> "Buyer's Offer: ₹${offer.offeredPricePerUnit} / unit"
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp,
                                                    color = AgriPriceGold
                                                )

                                                if (offer.counterPricePerUnit != null) {
                                                    Text(
                                                        text = when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "আপনার পাল্টা প্রস্তাব (Counter): ₹${offer.counterPricePerUnit}"
                                                            AppLanguage.HINDI -> "आपका जवाबी प्रस्ताव (Counter): ₹${offer.counterPricePerUnit}"
                                                            AppLanguage.ENGLISH -> "Your Counter Offer: ₹${offer.counterPricePerUnit}"
                                                        },
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                // Action buttons: Accept / Counter / Reject
                                                if (offer.status == "PENDING" || offer.status == "COUNTERED") {
                                                    FlowRow(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Button(
                                                            onClick = {
                                                                if (isProcessing) return@Button
                                                                processingOfferId = offer.id
                                                                coroutineScope.launch {
                                                                    try {
                                                                        val matchListing = listings.find { it.id == offer.listingId }
                                                                        if (matchListing != null) {
                                                                            repository.createOrderFromOffer(offer, matchListing)
                                                                        } else {
                                                                            repository.updateOfferStatus(offer.id, "ACCEPTED")
                                                                        }
                                                                    } catch (e: CancellationException) {
                                                                        throw e
                                                                    } catch (e: Exception) {
                                                                        Toast.makeText(context, e.localizedMessage ?: "Error updating offer", Toast.LENGTH_SHORT).show()
                                                                    } finally {
                                                                        processingOfferId = null
                                                                    }
                                                                }
                                                            },
                                                            enabled = !isProcessing,
                                                            modifier = Modifier.heightIn(min = 48.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = AgriGreenPrimary,
                                                                contentColor = Color.White
                                                            ),
                                                            shape = RoundedCornerShape(10.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                when (currentLanguage) {
                                                                    AppLanguage.BENGALI -> "মেনে নিন"
                                                                    AppLanguage.HINDI -> "स्वीकार करें"
                                                                    AppLanguage.ENGLISH -> "Accept"
                                                                }
                                                            )
                                                        }

                                                        Button(
                                                            onClick = {
                                                                if (isProcessing) return@Button
                                                                processingOfferId = offer.id
                                                                coroutineScope.launch {
                                                                    try {
                                                                        val counter = offer.offeredPricePerUnit + 2.0
                                                                        repository.updateOfferStatus(offer.id, "COUNTERED", counter)
                                                                    } catch (e: CancellationException) {
                                                                        throw e
                                                                    } catch (e: Exception) {
                                                                        Toast.makeText(context, e.localizedMessage ?: "Error countering offer", Toast.LENGTH_SHORT).show()
                                                                    } finally {
                                                                        processingOfferId = null
                                                                    }
                                                                }
                                                            },
                                                            enabled = !isProcessing,
                                                            modifier = Modifier.heightIn(min = 48.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                                contentColor = MaterialTheme.colorScheme.onSurface
                                                            ),
                                                            shape = RoundedCornerShape(10.dp)
                                                        ) {
                                                            Text(
                                                                when (currentLanguage) {
                                                                    AppLanguage.BENGALI -> "কাউন্টার +২"
                                                                    AppLanguage.HINDI -> "काउंटर +2"
                                                                    AppLanguage.ENGLISH -> "Counter +2"
                                                                }
                                                            )
                                                        }

                                                        Button(
                                                            onClick = {
                                                                if (isProcessing) return@Button
                                                                processingOfferId = offer.id
                                                                coroutineScope.launch {
                                                                    try {
                                                                        repository.updateOfferStatus(offer.id, "REJECTED")
                                                                    } catch (e: CancellationException) {
                                                                        throw e
                                                                    } catch (e: Exception) {
                                                                        Toast.makeText(context, e.localizedMessage ?: "Error rejecting offer", Toast.LENGTH_SHORT).show()
                                                                    } finally {
                                                                        processingOfferId = null
                                                                    }
                                                                }
                                                            },
                                                            enabled = !isProcessing,
                                                            modifier = Modifier.heightIn(min = 48.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                                                            ),
                                                            shape = RoundedCornerShape(10.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                when (currentLanguage) {
                                                                    AppLanguage.BENGALI -> "বাতিল"
                                                                    AppLanguage.HINDI -> "अस्वीकार"
                                                                    AppLanguage.ENGLISH -> "Reject"
                                                                }
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    Text(
                                                        text = when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "অবস্থা: ${localizeMarketplaceStatus(offer.status, currentLanguage)}"
                                                            AppLanguage.HINDI -> "स्थिति: ${localizeMarketplaceStatus(offer.status, currentLanguage)}"
                                                            AppLanguage.ENGLISH -> "Status: ${localizeMarketplaceStatus(offer.status, currentLanguage)}"
                                                        },
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (offer.status == "ACCEPTED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            // Confirmed Orders
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (orders.isEmpty()) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Text(
                                                text = when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "এখনও কোনো নিশ্চিত অর্ডার নেই। অফার মেনে নিলে এখানে রসিদ ও ডেলিভারি স্ট্যাটাস দেখা যাবে।"
                                                    AppLanguage.HINDI -> "अभी तक कोई पक्का ऑर्डर नहीं है। प्रस्ताव स्वीकार करने पर रसीद व डिलीवरी स्थिति यहाँ दिखेगी।"
                                                    AppLanguage.ENGLISH -> "No confirmed orders yet. Receipts and delivery status will appear here after accepting an offer."
                                                },
                                                modifier = Modifier.padding(20.dp),
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    items(orders) { ord ->
                                        val ordUnit = localizeUnit(ord.unit, currentLanguage)
                                        val ordProduct = localizeProduceName(ord.product, currentLanguage)
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "অর্ডার #${ord.id.take(8).uppercase()}"
                                                            AppLanguage.HINDI -> "ऑर्डर #${ord.id.take(8).uppercase()}"
                                                            AppLanguage.ENGLISH -> "Order #${ord.id.take(8).uppercase()}"
                                                        },
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.primaryContainer
                                                    ) {
                                                        Text(
                                                            text = localizeMarketplaceStatus(ord.status, currentLanguage),
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "ফসল: $ordProduct • পরিমাণ: ${ord.quantity} $ordUnit"
                                                        AppLanguage.HINDI -> "फसल: $ordProduct • मात्रा: ${ord.quantity} $ordUnit"
                                                        AppLanguage.ENGLISH -> "Crop: $ordProduct • Qty: ${ord.quantity} $ordUnit"
                                                    },
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "নির্ধারিত দর: ₹${ord.agreedPricePerUnit} / $ordUnit"
                                                        AppLanguage.HINDI -> "तय भाव: ₹${ord.agreedPricePerUnit} / $ordUnit"
                                                        AppLanguage.ENGLISH -> "Agreed Rate: ₹${ord.agreedPricePerUnit} / $ordUnit"
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    color = AgriPriceGold
                                                )
                                                Text(
                                                    text = when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "মোট মূল্য: ₹${"%.0f".format(ord.totalAmount)}"
                                                        AppLanguage.HINDI -> "कुल मूल्य: ₹${"%.0f".format(ord.totalAmount)}"
                                                        AppLanguage.ENGLISH -> "Total Value: ₹${"%.0f".format(ord.totalAmount)}"
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "পেমেন্ট মোড: ক্যাশ অন ডেলিভারি / ইউপিআই"
                                                        AppLanguage.HINDI -> "भुगतान माध्यम: कैश ऑन डिलीवरी / यूपीआई"
                                                        AppLanguage.ENGLISH -> "Payment Mode: Cash on Delivery / UPI"
                                                    },
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        }

        // Order Confirmation Dialog for Plant/Sapling
        orderingProduct?.let { prod ->
            CustomerOrderPlantDialog(
                product = prod,
                currentLanguage = currentLanguage,
                defaultCustomerName = farmerName,
                onDismiss = { orderingProduct = null },
                onConfirmOrder = { order ->
                    coroutineScope.launch {
                        try {
                            val result = nurseryProductRepo.placeCustomerOrder(order, currentLanguage)
                            if (result.isSuccess) {
                                Toast.makeText(context, L10n.t("dialog_order_success", currentLanguage), Toast.LENGTH_LONG).show()
                                orderingProduct = null
                            } else {
                                val fallbackErr = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অর্ডার সম্পন্ন করা যায়নি"
                                    AppLanguage.HINDI -> "ऑर्डर पूरा नहीं हो सका"
                                    AppLanguage.ENGLISH -> "Could not place order"
                                }
                                Toast.makeText(context, result.exceptionOrNull()?.message ?: fallbackErr, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Toast.makeText(context, e.localizedMessage ?: "Error placing order", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
        }

        // Seller Profile Dialog
        viewingSellerProfile?.let { prod ->
            val matchedApp = nurseryApps.firstOrNull {
                it.id == prod.nurseryId || (prod.sellerUserId.isNotBlank() && it.userId == prod.sellerUserId)
            }
            val isSuspended = (prod.sellerUserId in suspendedSellerIds) || (prod.nurseryId in suspendedSellerIds)
            val isVerifiedSeller = !isSuspended && (matchedApp?.status == NurseryApplicationStatus.APPROVED.code)
            SellerProfileDialog(
                product = prod,
                isVerifiedSeller = isVerifiedSeller,
                isSuspended = isSuspended,
                verifiedPhone = if (isVerifiedSeller) {
                    matchedApp?.businessPhone?.ifBlank { matchedApp.mobileNumber }?.ifBlank { null }
                } else null,
                currentLanguage = currentLanguage,
                onReportClick = {
                    viewingSellerProfile = null
                    reportingProduct = prod
                },
                onDismiss = { viewingSellerProfile = null }
            )
        }

        reportingProduct?.let { targetProd ->
            val session = remember { UserSessionManager.getSession(context) }
            FraudReportDialog(
                targetName = "${targetProd.getDisplayName(currentLanguage)} — ${targetProd.getDisplayNurseryName(currentLanguage)}",
                language = currentLanguage,
                onDismiss = { reportingProduct = null },
                onSubmit = { reasonCode, description ->
                    coroutineScope.launch {
                        val res = trustRepo.submitFraudReport(
                            targetType = FraudTargetType.NURSERY_PRODUCT,
                            targetId = targetProd.id,
                            targetName = "${targetProd.getDisplayName(currentLanguage)} (${targetProd.getDisplayNurseryName(currentLanguage)})",
                            sellerUserId = targetProd.sellerUserId.ifBlank { targetProd.nurseryId },
                            reasonCode = reasonCode,
                            description = description,
                            reporterUserId = userId,
                            reporterName = session?.userName ?: farmerName
                        )
                        Toast.makeText(
                            context,
                            if (res.isSuccess) "Fraud report submitted to Admin Trust Team" else (res.exceptionOrNull()?.message ?: "Failed"),
                            Toast.LENGTH_LONG
                        ).show()
                        reportingProduct = null
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SellProduceScreen(
    currentLanguage: AppLanguage,
    userId: String,
    farmerName: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { FirestoreRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val isNarrow = isCompactNarrowScreen()

    var product by remember(currentLanguage) {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "টমেটো"
                AppLanguage.HINDI -> "टमाटर"
                AppLanguage.ENGLISH -> "Tomato"
            }
        )
    }
    var quantity by remember { mutableStateOf("500") }
    var unit by remember { mutableStateOf("KG") }
    var expectedPrice by remember { mutableStateOf("25") }
    var location by remember(currentLanguage) {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "বর্ধমান / হুগলি"
                AppLanguage.HINDI -> "बर्दवान / हुगली"
                AppLanguage.ENGLISH -> "Bardhaman / Hooghly"
            }
        )
    }
    var phone by remember { mutableStateOf(UserSessionManager.getSession(context)?.phone ?: "") }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "ফসল বিক্রি করতে দিন"
                            AppLanguage.HINDI -> "फसल बेचने के लिए सूचीबद्ध करें"
                            AppLanguage.ENGLISH -> "List Produce for Sale"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Reference Market Price Banner for Fair Price Control
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PriceCheck, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "স্থানীয় মণ্ডি রেফারেন্স দর:"
                                    AppLanguage.HINDI -> "स्थानीय मंडी संदर्भ भाव:"
                                    AppLanguage.ENGLISH -> "Local Mandi Reference Rates:"
                                },
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "টমেটো গড় দর ₹২৫/কেজি • বাঁধাকপি ₹১৪/কেজি"
                                    AppLanguage.HINDI -> "टमाटर औसत भाव ₹25/किग्रा • पत्तागोभी ₹14/किग्रा"
                                    AppLanguage.ENGLISH -> "Tomato avg ₹25/kg • Cabbage ₹14/kg"
                                },
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "১. ফসলের নাম:"
                        AppLanguage.HINDI -> "1. फसल का नाम:"
                        AppLanguage.ENGLISH -> "1. Crop / Produce Name:"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = product,
                    onValueChange = { product = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "২. মোট পরিমাণ ও একক:"
                        AppLanguage.HINDI -> "2. कुल मात्रा और इकाई:"
                        AppLanguage.ENGLISH -> "2. Total Quantity & Unit:"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isNarrow) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "পরিমাণ"
                                        AppLanguage.HINDI -> "मात्रा"
                                        AppLanguage.ENGLISH -> "Quantity"
                                    }
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "একক (KG/কুইন্টাল)"
                                        AppLanguage.HINDI -> "इकाई (KG/क्विंटल)"
                                        AppLanguage.ENGLISH -> "Unit (KG/Quintal)"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "পরিমাণ"
                                        AppLanguage.HINDI -> "मात्रा"
                                        AppLanguage.ENGLISH -> "Quantity"
                                    }
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "একক (KG/কুইন্টাল)"
                                        AppLanguage.HINDI -> "इकाई (KG/क्विंटल)"
                                        AppLanguage.ENGLISH -> "Unit (KG/Quintal)"
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "৩. আপনার প্রত্যাশিত দর (₹):"
                        AppLanguage.HINDI -> "3. आपका अपेक्षित भाव (₹):"
                        AppLanguage.ENGLISH -> "3. Your Expected Rate (₹):"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = expectedPrice,
                    onValueChange = { expectedPrice = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "দর (₹ প্রতি একক)"
                                AppLanguage.HINDI -> "भाव (₹ प्रति इकाई)"
                                AppLanguage.ENGLISH -> "Rate (₹ per unit)"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "৪. ফসলের অবস্থান (গ্রাম/জেলা):"
                        AppLanguage.HINDI -> "4. फसल का स्थान (गाँव/जिला):"
                        AppLanguage.ENGLISH -> "4. Farm Location (Village/District):"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        if (isSubmitting) return@Button
                        val parsedQty = quantity.toLocalizedDoubleOrNull()
                        val parsedPrice = expectedPrice.toLocalizedDoubleOrNull()
                        if (product.isBlank() || unit.isBlank() || location.isBlank() || parsedQty == null || parsedQty <= 0.0 || parsedPrice == null || parsedPrice <= 0.0) {
                            val errMsg = when (currentLanguage) {
                                AppLanguage.BENGALI -> "দয়া করে ফসলের নাম, সঠিক পরিমাণ, দর ও ঠিকানা লিখুন"
                                AppLanguage.HINDI -> "कृपया फसल का नाम, सही मात्रा, भाव और स्थान दर्ज करें"
                                AppLanguage.ENGLISH -> "Please enter valid crop name, quantity, rate and location"
                            }
                            Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        coroutineScope.launch {
                            try {
                                val listing = ProduceListing(
                                    farmerId = userId,
                                    farmerName = farmerName,
                                    farmerPhone = phone,
                                    product = product.trim(),
                                    quantity = parsedQty,
                                    unit = unit.trim(),
                                    expectedPricePerUnit = parsedPrice,
                                    location = location.trim()
                                )
                                repository.addProduceListing(listing)
                                onSuccess()
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                val failMsg = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ফসল তালিকা প্রকাশ করতে সমস্যা হয়েছে"
                                    AppLanguage.HINDI -> "फसल सूची प्रकाशित करने में समस्या हुई"
                                    AppLanguage.ENGLISH -> "Failed to publish produce listing"
                                }
                                Toast.makeText(context, failMsg, Toast.LENGTH_SHORT).show()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("submit_produce_listing_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "বিক্রির জন্য প্রকাশ করুন"
                            AppLanguage.HINDI -> "बिक्री के लिए प्रकाशित करें"
                            AppLanguage.ENGLISH -> "Publish Listing for Sale"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// CUSTOMER MARKETPLACE VIEW FOR NURSERY PLANTS & PRODUCTS
// -----------------------------------------------------------------------------
@Composable
fun NurseryCustomerMarketplaceView(
    products: List<NurseryProduct>,
    nurseryApps: List<NurseryApplication> = emptyList(),
    suspendedSellerIds: Set<String> = emptySet(),
    currentLanguage: AppLanguage,
    onOrderClick: (NurseryProduct) -> Unit,
    onProfileClick: (NurseryProduct) -> Unit,
    onReportClick: (NurseryProduct) -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf<String?>(null) }

    val filteredProducts = remember(products, searchQuery, selectedCat, suspendedSellerIds) {
        products.filter { p ->
            val notSuspended = p.sellerUserId !in suspendedSellerIds && p.nurseryId !in suspendedSellerIds
            val matchCat = selectedCat == null || p.category.equals(selectedCat, ignoreCase = true)
            val matchQuery = searchQuery.isBlank() ||
                    p.productName.contains(searchQuery, ignoreCase = true) ||
                    p.nurseryName.contains(searchQuery, ignoreCase = true) ||
                    p.productLocation.contains(searchQuery, ignoreCase = true)
            notSuspended && matchCat && matchQuery
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AntiFraudWarningBanner(language = currentLanguage)
            }

            // Search & Category Filters
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "চারা, বীজ বা নার্সারির নাম দিয়ে খুঁজুন..."
                                AppLanguage.HINDI -> "पौधे, बीज या नर्सरी के नाम से खोजें..."
                                AppLanguage.ENGLISH -> "Search saplings, seeds, or nursery..."
                            }
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCat == null,
                            onClick = { selectedCat = null },
                            modifier = Modifier.heightIn(min = 44.dp),
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "সব চারা (${products.size})"
                                        AppLanguage.HINDI -> "सभी पौधे (${products.size})"
                                        AppLanguage.ENGLISH -> "All Plants (${products.size})"
                                    },
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                    items(NurseryProductCategory.values()) { cat ->
                        FilterChip(
                            selected = selectedCat == cat.code,
                            onClick = { selectedCat = if (selectedCat == cat.code) null else cat.code },
                            modifier = Modifier.heightIn(min = 44.dp),
                            label = { Text("${cat.emoji} ${cat.getDisplayName(currentLanguage)}", fontSize = 12.sp) }
                        )
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "কোনো পণ্য পাওয়া যায়নি"
                                AppLanguage.HINDI -> "कोई उत्पाद नहीं मिला"
                                AppLanguage.ENGLISH -> "No products found"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredProducts, key = { it.id }) { prod ->
                    val matchedApp = nurseryApps.firstOrNull {
                        it.id == prod.nurseryId || (prod.sellerUserId.isNotBlank() && it.userId == prod.sellerUserId)
                    }
                    val isSellerVerified = matchedApp?.status == NurseryApplicationStatus.APPROVED.code
                    val verifiedPhone = if (isSellerVerified) {
                        matchedApp?.businessPhone?.ifBlank { matchedApp.mobileNumber }?.ifBlank { null }
                    } else null
                    val verifiedWhatsapp = if (isSellerVerified) {
                        matchedApp?.whatsappNumber?.ifBlank { verifiedPhone }?.ifBlank { null }
                    } else null

                    CustomerNurseryProductCard(
                        product = prod,
                        isVerifiedSeller = isSellerVerified,
                        hasVerifiedPhone = !verifiedPhone.isNullOrBlank(),
                        hasVerifiedWhatsapp = !verifiedWhatsapp.isNullOrBlank(),
                        currentLanguage = currentLanguage,
                        onOrder = { onOrderClick(prod) },
                        onProfile = { onProfileClick(prod) },
                        onReport = { onReportClick(prod) },
                        onCall = {
                            val phone = verifiedPhone ?: return@CustomerNurseryProductCard
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            val noApp = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ফোন নম্বর: $phone"
                                AppLanguage.HINDI -> "फ़ोन नंबर: $phone"
                                AppLanguage.ENGLISH -> "Dialer not available: $phone"
                            }
                            safeStartActivity(context, intent, noApp)
                        },
                        onWhatsApp = {
                            val waRaw = verifiedWhatsapp?.filter { it.isDigit() }.orEmpty()
                            if (waRaw.length < 8) return@CustomerNurseryProductCard
                            val clean = if (waRaw.length == 10) "91$waRaw" else waRaw
                            val msg = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নমস্কার, আমি কৃষক সাথী মার্কেটপ্লেস থেকে আপনার '${prod.productName}' সম্পর্কে জানতে চাই।"
                                AppLanguage.HINDI -> "नमस्ते, मैं कृषक साथी मार्केटप्लेस से आपके '${prod.productName}' के बारे में जानना चाहता हूँ।"
                                AppLanguage.ENGLISH -> "Hello, I would like to inquire about '${prod.productName}' from Krishak Sathi Marketplace."
                            }
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$clean&text=${Uri.encode(msg)}")
                            val noWa = when (currentLanguage) {
                                AppLanguage.BENGALI -> "WhatsApp খুলতে পারেনি: +$clean"
                                AppLanguage.HINDI -> "WhatsApp नहीं खुल सका: +$clean"
                                AppLanguage.ENGLISH -> "Could not open WhatsApp: +$clean"
                            }
                            safeStartActivity(context, Intent(Intent.ACTION_VIEW, uri), noWa)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomerNurseryProductCard(
    product: NurseryProduct,
    isVerifiedSeller: Boolean = false,
    hasVerifiedPhone: Boolean = false,
    hasVerifiedWhatsapp: Boolean = false,
    currentLanguage: AppLanguage,
    onOrder: () -> Unit,
    onProfile: () -> Unit,
    onReport: () -> Unit = {},
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    val categoryEnum = NurseryProductCategory.fromCode(product.category)
    val inStock = product.isActuallyInStock
    val isNarrow = isCompactNarrowScreen()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: Category, Name & Stock Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(categoryEnum.emoji, fontSize = 28.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.getDisplayName(currentLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${categoryEnum.getDisplayName(currentLanguage)} • ${product.getDisplayType(currentLanguage)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    color = if (inStock) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (inStock) {
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "মজুত আছে (${product.freeStock})"
                                AppLanguage.HINDI -> "स्टॉक उपलब्ध (${product.freeStock})"
                                AppLanguage.ENGLISH -> "In Stock (${product.freeStock})"
                            }
                        } else {
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "স্টক শেষ"
                                AppLanguage.HINDI -> "स्टॉक समाप्त"
                                AppLanguage.ENGLISH -> "Out of Stock"
                            }
                        },
                        color = if (inStock) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Nursery & Location
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .heightIn(min = 36.dp)
                        .clickable(onClick = onProfile)
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = product.getDisplayNurseryName(currentLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    SellerTierBadge(
                        isVerified = isVerifiedSeller,
                        isAuthorisedDealer = false,
                        language = currentLanguage
                    )
                }
                Text(
                    text = "📍 ${product.getDisplayLocation(currentLanguage)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            // Price & Size Row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "₹${product.effectivePrice.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (product.discountPrice != null && product.discountPrice < product.regularPrice) {
                        Text(
                            text = "₹${product.regularPrice.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "সাইজ: ${product.getDisplaySize(currentLanguage)}"
                        AppLanguage.HINDI -> "आकार: ${product.getDisplaySize(currentLanguage)}"
                        AppLanguage.ENGLISH -> "Size: ${product.getDisplaySize(currentLanguage)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (product.plantCareInstructions.isNotBlank()) {
                val displayCare = product.getDisplayCare(currentLanguage)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "💡 পরিচর্যা: $displayCare"
                            AppLanguage.HINDI -> "💡 देखभाल: $displayCare"
                            AppLanguage.ENGLISH -> "💡 Care: $displayCare"
                        },
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Action Buttons (Direct Order, WhatsApp, Call)
            if (isNarrow) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOrder,
                        enabled = inStock,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(L10n.t("btn_order_now", currentLanguage), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (hasVerifiedWhatsapp) {
                            OutlinedButton(
                                onClick = onWhatsApp,
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.RoyalSoftGold),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (hasVerifiedPhone) {
                            OutlinedButton(
                                onClick = onCall,
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(L10n.t("btn_call_seller", currentLanguage), fontSize = 12.sp)
                            }
                        }
                        OutlinedButton(
                            onClick = onReport,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Report", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onOrder,
                        enabled = inStock,
                        modifier = Modifier.weight(1.2f).heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(L10n.t("btn_order_now", currentLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (hasVerifiedWhatsapp) {
                        OutlinedButton(
                            onClick = onWhatsApp,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.RoyalSoftGold),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (hasVerifiedPhone) {
                        OutlinedButton(
                            onClick = onCall,
                            modifier = Modifier.weight(0.9f).heightIn(min = 48.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(L10n.t("btn_call_seller", currentLanguage), fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onReport,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Report", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// CUSTOMER DIRECT ORDER CONFIRMATION MODAL
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderPlantDialog(
    product: NurseryProduct,
    currentLanguage: AppLanguage,
    defaultCustomerName: String,
    onDismiss: () -> Unit,
    onConfirmOrder: (NurseryOrder) -> Unit
) {
    val context = LocalContext.current
    var quantity by remember { mutableIntStateOf(1) }
    var customerName by remember { mutableStateOf(defaultCustomerName) }
    var phone by remember { mutableStateOf(UserSessionManager.getSession(context)?.phone ?: "") }
    var deliveryAddress by remember(currentLanguage) {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "শালুগাড়া, শিলিগুড়ি, জলপাইগুড়ি"
                AppLanguage.HINDI -> "शालुगाड़ा, सिलीगुड़ी, जलपाईगुड़ी"
                AppLanguage.ENGLISH -> "Salugara, Siliguri, Jalpaiguri"
            }
        )
    }
    var isSubmitting by remember { mutableStateOf(false) }

    val totalAmount = quantity * product.effectivePrice

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = L10n.t("dialog_order_title", currentLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = L10n.t("btn_close", currentLanguage))
                    }
                }

                HorizontalDivider()

                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "পণ্য: ${product.getDisplayName(currentLanguage)}"
                        AppLanguage.HINDI -> "उत्पाद: ${product.getDisplayName(currentLanguage)}"
                        AppLanguage.ENGLISH -> "Product: ${product.getDisplayName(currentLanguage)}"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "নার্সারি: ${product.getDisplayNurseryName(currentLanguage)} (📍 ${product.getDisplayLocation(currentLanguage)})"
                        AppLanguage.HINDI -> "नर्सरी: ${product.getDisplayNurseryName(currentLanguage)} (📍 ${product.getDisplayLocation(currentLanguage)})"
                        AppLanguage.ENGLISH -> "Nursery: ${product.getDisplayNurseryName(currentLanguage)} (📍 ${product.getDisplayLocation(currentLanguage)})"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quantity Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        L10n.t("dialog_order_qty", currentLanguage),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = { if (quantity > 1) quantity-- },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        FilledTonalButton(
                            onClick = { if (quantity < product.freeStock) quantity++ },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "আপনার নাম"
                                AppLanguage.HINDI -> "आपका नाम"
                                AppLanguage.ENGLISH -> "Your Name"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(L10n.t("dialog_order_phone_hint", currentLanguage)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "ডেলিভারি ঠিকানা"
                                AppLanguage.HINDI -> "डिलीवरी का पता"
                                AppLanguage.ENGLISH -> "Delivery Address"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "মোট মূল্য (ক্যাশ অন ডেলিভারি):"
                                AppLanguage.HINDI -> "कुल राशि (कैश ऑन डिलीवरी):"
                                AppLanguage.ENGLISH -> "Total (Cash on Delivery):"
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "₹${totalAmount.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isSubmitting) return@Button
                        if (customerName.isBlank() || phone.filter { it.isDigit() }.length < 8 || deliveryAddress.isBlank()) {
                            val errMsg = when (currentLanguage) {
                                AppLanguage.BENGALI -> "দয়া করে আপনার নাম, সঠিক মোবাইল নম্বর ও ঠিকানা লিখুন"
                                AppLanguage.HINDI -> "कृपया अपना नाम, सही मोबाइल नंबर और पता दर्ज करें"
                                AppLanguage.ENGLISH -> "Please enter valid name, phone number and delivery address"
                            }
                            Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        val order = NurseryOrder(
                            nurseryId = product.nurseryId,
                            nurseryName = product.nurseryName,
                            sellerUserId = product.sellerUserId,
                            customerId = "cust_direct_${System.currentTimeMillis() % 10000}",
                            customerName = customerName.trim(),
                            customerPhone = phone.trim(),
                            deliveryAddress = deliveryAddress.trim(),
                            items = listOf(
                                NurseryOrderItem(
                                    productId = product.id,
                                    productName = product.productName,
                                    category = product.category,
                                    quantity = quantity,
                                    unitPrice = product.effectivePrice,
                                    totalPrice = totalAmount
                                )
                            ),
                            totalAmount = totalAmount,
                            status = "NEW"
                        )
                        onConfirmOrder(order)
                        isSubmitting = false
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreenPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(L10n.t("dialog_order_submit", currentLanguage), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SELLER PROFILE MODAL DIALOG
// -----------------------------------------------------------------------------
@Composable
fun SellerProfileDialog(
    product: NurseryProduct,
    isVerifiedSeller: Boolean = false,
    isSuspended: Boolean = false,
    verifiedPhone: String? = null,
    currentLanguage: AppLanguage,
    onReportClick: () -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "নার্সারি বিক্রেতা প্রোফাইল"
                            AppLanguage.HINDI -> "नर्सरी विक्रेता प्रोफ़ाइल"
                            AppLanguage.ENGLISH -> "Nursery Seller Profile"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = L10n.t("btn_close", currentLanguage))
                    }
                }

                HorizontalDivider()

                SellerTierBadge(
                    isVerified = isVerifiedSeller && !isSuspended,
                    isAuthorisedDealer = false,
                    language = currentLanguage
                )

                if (isSuspended) {
                    Text(
                        text = "Notice: This seller is currently suspended under Trust & Safety review.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.RoyalAlertText
                    )
                }

                Text(
                    product.getDisplayNurseryName(currentLanguage),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "📍 অবস্থান: ${product.getDisplayLocation(currentLanguage)}"
                        AppLanguage.HINDI -> "📍 स्थान: ${product.getDisplayLocation(currentLanguage)}"
                        AppLanguage.ENGLISH -> "📍 Location: ${product.getDisplayLocation(currentLanguage)}"
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (verifiedPhone != null && !isSuspended) {
                    Text(
                        text = "📞 Verified Contact: $verifiedPhone",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "🚚 ডেলিভারি এলাকা: সর্বোচ্চ ${product.deliveryRadiusKm} কিমি পর্যন্ত"
                        AppLanguage.HINDI -> "🚚 डिलीवरी क्षेत्र: अधिकतम ${product.deliveryRadiusKm} किमी तक"
                        AppLanguage.ENGLISH -> "🚚 Delivery Radius: Up to ${product.deliveryRadiusKm} km"
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "🌱 বিশেষত্ব: ফল চারা, চা ক্লোন, বনজ ও ঔষধি চারা"
                        AppLanguage.HINDI -> "🌱 विशेषता: फल पौधे, चाय क्लोन, वानिकी व औषधीय पौधे"
                        AppLanguage.ENGLISH -> "🌱 Specialty: Fruit saplings, tea clones, timber & medicinal plants"
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onReportClick,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Report Seller / Listing Issue")
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreenPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(L10n.t("btn_close", currentLanguage))
                }
            }
        }
    }
}


