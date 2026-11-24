package com.example.ui.machinery

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TimeToLeave
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.MachineryExtendedCatalogPart2
import com.example.data.local.entity.FraudTargetType
import com.example.data.model.AppLanguage
import com.example.data.model.FarmServiceType
import com.example.data.model.MachineryProduct
import com.example.data.model.MachineryReviewRecord
import com.example.data.model.NurseryVerificationStatus
import com.example.data.repository.MachineryRepository
import com.example.data.repository.TrustAndSafetyRepository
import com.example.ui.theme.MachineryCardBadgeBg
import com.example.ui.theme.MachineryCardBadgeText
import com.example.ui.theme.MachineryCardBodyInk
import com.example.ui.theme.MachineryCardBorder
import com.example.ui.theme.MachineryCardSoftMint
import com.example.ui.theme.MachineryCardTitleInk
import com.example.ui.theme.MachineryCardWarnBg
import com.example.ui.theme.MachineryCardWarnText
import com.example.ui.theme.MachineryCardWhite
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.trust.AntiFraudWarningBanner
import com.example.ui.trust.FraudReportDialog
import com.example.ui.trust.SafeBuyerProtectionCard
import com.example.ui.trust.SellerTierBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MachineryProductDetailScreen(
    machine: MachineryProduct,
    currentLanguage: AppLanguage,
    userId: String,
    farmerName: String,
    repository: MachineryRepository? = null,
    isFavourite: Boolean? = null,
    isCompared: Boolean = false,
    onToggleFavourite: (() -> Unit)? = null,
    onToggleCompare: (() -> Unit)? = null,
    onOpenDealers: () -> Unit = {},
    onHireService: ((FarmServiceType) -> Unit)? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember(repository) { repository ?: MachineryRepository(context) }
    val trustRepo = remember { TrustAndSafetyRepository(context) }

    LaunchedEffect(repo) { repo.ensureSeeded() }

    val offers by repo.observeOffersForMachine(machine.id).collectAsState(initial = emptyList())
    val allDealers by repo.observeAllDealers().collectAsState(initial = emptyList())
    val reviews by repo.observeReviewsForMachine(machine.id).collectAsState(initial = emptyList())
    val favIds by repo.observeFavouriteIds(userId).collectAsState(initial = emptySet())
    val suspendedSellerIds by trustRepo.observeSuspendedSellerIds().collectAsState(initial = emptySet())

    val isSellerSuspended = remember(machine.sellerUserId, machine.id, suspendedSellerIds) {
        (machine.sellerUserId.isNotBlank() && machine.sellerUserId in suspendedSellerIds) ||
            machine.id in suspendedSellerIds
    }

    val effectiveFav = isFavourite ?: favIds.contains(machine.id)
    val photos = remember(machine.photosCsv, machine.firebaseStoragePathsCsv) { machine.getPhotoTokens() }
    val cat = remember(machine.categoryCode) { machine.getCategoryEnum() }
    val hireService = remember(machine.hireServiceTypeCode, machine.categoryCode, machine.subcategory) {
        machine.getHireServiceType()
    }
    val guide = remember(machine.id, currentLanguage) {
        MachineryExtendedCatalogPart2.buildBeginnerGuide(machine, currentLanguage)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showFullScreenPhoto by remember { mutableStateOf(false) }
    var zoomInitialIndex by remember { mutableIntStateOf(0) }
    var showQuotationForm by remember { mutableStateOf(false) }
    var isIssueReport by remember { mutableStateOf(false) }
    var showRentalForm by remember { mutableStateOf(false) }
    var showFraudDialog by remember { mutableStateOf(false) }
    var localServiceBooking by remember { mutableStateOf<FarmServiceType?>(null) }

    // Review form state
    var reviewRating by remember { mutableIntStateOf(5) }
    var reviewTitle by remember { mutableStateOf("") }
    var reviewComment by remember { mutableStateOf("") }

    val dealersMap = remember(allDealers) { allDealers.associateBy { it.dealerId } }
    val activeOffers = remember(offers, dealersMap, suspendedSellerIds) {
        offers.filter { offer ->
            val dealer = dealersMap[offer.dealerId]
            offer.dealerId !in suspendedSellerIds &&
                (dealer?.claimedByUserId.isNullOrBlank() || dealer?.claimedByUserId !in suspendedSellerIds)
        }
    }
    val linkedSellerDealer = remember(allDealers, machine.sellerUserId, machine.sellerBusinessName) {
        allDealers.firstOrNull {
            it.claimedByUserId == machine.sellerUserId ||
                it.businessName.equals(machine.sellerBusinessName, ignoreCase = true)
        }
    }
    val isSellerVerified = !isSellerSuspended && (
        linkedSellerDealer?.isVerified == true ||
            (machine.listingOrigin == "SELLER_LISTING" && machine.approvalStatus == "APPROVED" && machine.hasConfirmedSellerContact())
        )
    val isAuthorisedDealer = !isSellerSuspended && (linkedSellerDealer?.isAuthorisedDealerVerified == true)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("machinery_detail_dialog"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RoyalGreenSecondaryBg,
                    titleContentColor = RoyalTextPrimary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp).testTag("machinery_detail_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = RoyalTextPrimary)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = machine.getDisplayName(currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${cat.emoji} ${cat.getDisplayName(currentLanguage)} • ${machine.getDisplayBrandModel(currentLanguage)}",
                            fontSize = 10.sp,
                            color = RoyalSoftGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (onToggleFavourite != null) {
                                onToggleFavourite()
                            } else {
                                scope.launch { repo.toggleFavourite(userId, machine.id, effectiveFav) }
                            }
                        },
                        modifier = Modifier.size(48.dp).testTag("machinery_detail_fav_btn")
                    ) {
                        Icon(
                            imageVector = if (effectiveFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save machine",
                            tint = if (effectiveFav) RoyalGold else RoyalTextPrimary
                        )
                    }
                    if (onToggleCompare != null) {
                        IconButton(
                            onClick = onToggleCompare,
                            modifier = Modifier.size(48.dp).testTag("machinery_detail_compare_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = "Compare machine",
                                tint = if (isCompared) RoyalGold else RoyalTextPrimary
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            val shareText = buildString {
                                append("${machine.getDisplayName(currentLanguage)}\n")
                                append("${cat.getDisplayName(currentLanguage)} • ${machine.getDisplayBrandModel(currentLanguage)}\n")
                                if (machine.horsepowerOrCapacity.isNotBlank()) append("Power/Capacity: ${machine.horsepowerOrCapacity}\n")
                                append("Suitable Crops: ${machine.getDisplaySuitableCrops(currentLanguage)}\n")
                                append("Price: ${machine.salePriceInr?.let { "₹${it.toInt()}" } ?: "Price on request"}\n")
                                append("— Shared from KRISHAK SATHI")
                            }
                            runCatching {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, machine.getDisplayName(currentLanguage))
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Equipment Specs"))
                            }
                        },
                        modifier = Modifier.size(48.dp).testTag("machinery_detail_share_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share specs", tint = RoyalTextPrimary)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MachineryCardWhite,
                border = BorderStroke(1.dp, MachineryCardBorder),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isIssueReport = false
                            showQuotationForm = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalGreenEmerald,
                            contentColor = RoyalTextPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("machinery_detail_buy_btn")
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "কোটেশন / ক্রয়"
                                AppLanguage.HINDI -> "खरीद / कोटेशन"
                                AppLanguage.ENGLISH -> "Buy / Quote"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (machine.forRent) {
                        OutlinedButton(
                            onClick = { showRentalForm = true },
                            border = BorderStroke(1.dp, RoyalGreenEmerald),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("machinery_detail_rent_btn")
                        ) {
                            Icon(Icons.Default.TimeToLeave, contentDescription = null, modifier = Modifier.size(15.dp), tint = MachineryCardBadgeText)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ভাড়া নিন"
                                    AppLanguage.HINDI -> "किराया लें"
                                    AppLanguage.ENGLISH -> "Rent"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MachineryCardBadgeText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (hireService != null) {
                        OutlinedButton(
                            onClick = {
                                if (onHireService != null) {
                                    onHireService(hireService)
                                } else {
                                    localServiceBooking = hireService
                                }
                            },
                            border = BorderStroke(1.dp, RoyalGold),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("machinery_detail_hire_service_btn")
                        ) {
                            Icon(Icons.Default.Engineering, contentDescription = null, modifier = Modifier.size(15.dp), tint = MachineryCardWarnText)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "সেবা ভাড়া"
                                    AppLanguage.HINDI -> "सेवा बुक"
                                    AppLanguage.ENGLISH -> "Hire Service"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MachineryCardWarnText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                .background(RoyalGreenPrimaryBg)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 880.dp),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hero Photo & Provenance Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MachineryCardWhite),
                        border = BorderStroke(1.dp, MachineryCardBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            MachineryEquipmentImage(
                                photoTokens = photos,
                                fallbackEmoji = cat.emoji,
                                contentDescription = machine.getDisplayName(currentLanguage),
                                enableSwipePager = true,
                                showZoomHint = true,
                                onImageClick = { idx ->
                                    if (photos.isNotEmpty()) {
                                        zoomInitialIndex = idx
                                        showFullScreenPhoto = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp)
                                    .testTag("machinery_detail_photo")
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MachinerySourceProvenanceBadge(photoTokens = photos, lang = currentLanguage)
                                Surface(color = MachineryCardBadgeBg, shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        text = "${machine.subcategory} • ${machine.condition}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MachineryCardBadgeText,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                                if (machine.operatorTrainingRequired) {
                                    Surface(color = MachineryCardWarnBg, shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = "Operator Training Advised",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MachineryCardWarnText,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            if (machine.pendingAdminReview) {
                                Surface(
                                    color = MachineryCardWarnBg,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, MachineryCardWarnText.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = MachineryCardWarnText, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = machine.adminReviewNote.ifBlank {
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "ব্র্যান্ড/মডেল ও বিস্তারিত স্পেক অ্যাডমিন পর্যালোচনার অধীনে রয়েছে।"
                                                    AppLanguage.HINDI -> "ब्रांड/मॉडल और विवरण एडमिन समीक्षा के अधीन हैं।"
                                                    AppLanguage.ENGLISH -> "Brand, model, or exact variant flagged for Admin Review."
                                                }
                                            },
                                            fontSize = 11.sp,
                                            color = MachineryCardWarnText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5-Tab Navigation Bar
                item {
                    val tabTitles = when (currentLanguage) {
                        AppLanguage.BENGALI -> listOf("সহজ গাইড", "স্পেক ও সুরক্ষা", "ডিলার (${activeOffers.size})", "ভর্তুকি ও খরচ", "রিভিউ (${reviews.size})")
                        AppLanguage.HINDI -> listOf("सरल गाइड", "स्पेक्स व सुरक्षा", "डीलर (${activeOffers.size})", "सब्सिडी व खर्च", "समीक्षा (${reviews.size})")
                        AppLanguage.ENGLISH -> listOf("Overview", "Specs & Safety", "Dealers (${activeOffers.size})", "Subsidies & Cost", "Reviews (${reviews.size})")
                    }
                    Surface(
                        color = RoyalGreenSecondaryBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RoyalBorder)
                    ) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = RoyalGreenSecondaryBg,
                            contentColor = RoyalGold,
                            edgePadding = 8.dp,
                            divider = {}
                        ) {
                            tabTitles.forEachIndexed { idx, label ->
                                Tab(
                                    selected = selectedTab == idx,
                                    onClick = { selectedTab = idx },
                                    modifier = Modifier.testTag("detail_tab_$idx"),
                                    text = {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTab == idx) RoyalGold else RoyalTextSecondary
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                when (selectedTab) {
                    0 -> {
                        // Overview & 10-Point Beginner Guide
                        item {
                            MachineryBeginnerGuideCard(
                                product = machine,
                                guide = guide,
                                lang = currentLanguage
                            )
                        }
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MachineryCardWhite),
                                border = BorderStroke(1.dp, MachineryCardBorder),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "মূল ব্যবহার ও উপযুক্ত ফসল"
                                            AppLanguage.HINDI -> "मुख्य उपयोग व उपयुक्त फसलें"
                                            AppLanguage.ENGLISH -> "Field Suitability & Compatibility"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MachineryCardTitleInk
                                    )
                                    val purpose = machine.getDisplayPurpose(currentLanguage)
                                    if (purpose.isNotBlank()) {
                                        DetailRow("Purpose", purpose)
                                    }
                                    DetailRow("Suitable Crops", machine.getDisplaySuitableCrops(currentLanguage))
                                    if (machine.compatibleWithIdsCsv.isNotBlank()) {
                                        DetailRow("Compatible Equipment", machine.compatibleWithIdsCsv)
                                    }
                                    Text(
                                        text = "Direct Business Transparency: KRISHAK SATHI connects farmers with dealers without hidden commission or fabricated prices.",
                                        fontSize = 10.sp,
                                        color = MachineryCardBodyInk
                                    )
                                }
                            }
                        }
                        item {
                            SafeBuyerProtectionCard(
                                businessName = machine.sellerBusinessName,
                                locationAddress = machine.sellerLocation,
                                verifiedPhone = if (machine.hasConfirmedSellerContact()) machine.sellerPhone else null,
                                isVerified = isSellerVerified,
                                isAuthorisedDealer = isAuthorisedDealer,
                                isSuspended = isSellerSuspended,
                                lastVerifiedDate = machine.lastUpdatedDate,
                                warrantyOrInvoiceNote = machine.warrantyInfo.ifBlank { "Always request GST tax invoice & warranty card." },
                                language = currentLanguage,
                                onReportFraudClick = { showFraudDialog = true }
                            )
                        }
                        item {
                            AntiFraudWarningBanner(language = currentLanguage)
                        }
                    }

                    1 -> {
                        // Class Specs vs Model Verification + Safety + Video
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MachineryCardWhite),
                                border = BorderStroke(1.dp, MachineryCardBorder),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Engineering Class Specifications", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MachineryCardTitleInk)
                                    DetailRow("HP / Capacity", machine.horsepowerOrCapacity)
                                    DetailRow("Engine / Motor", machine.engineOrMotorSpec)
                                    DetailRow("Fuel / Power", machine.fuelOrPowerType)
                                    DetailRow("Coverage / Width", machine.workingWidthOrCoverage)
                                    DetailRow("Weight & Size", listOf(machine.weightKg, machine.dimensionsText).filter { it.isNotBlank() }.joinToString(" • "))
                                    DetailRow("Soil / Terrain", machine.suitableSoilOrTerrain)
                                    DetailRow("Plot Size", machine.landSizeSuitability)
                                    DetailRow("Warranty", machine.warrantyInfo)
                                    DetailRow("Spare Parts", machine.sparePartsInfo)

                                    HorizontalDivider(color = MachineryCardBorder)
                                    Text("Brand & Model Verification Status", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MachineryCardTitleInk)
                                    DetailRow("Brand & Model", machine.getDisplayBrandModel(currentLanguage))
                                    DetailRow("Verification Status", "${machine.infoVerificationStatus} (${machine.verificationBadgeText})")
                                    DetailRow("Last Verified", machine.lastUpdatedDate)
                                }
                            }
                        }

                        val safety = machine.getDisplaySafetyGuidelines(currentLanguage)
                        if (machine.requiresDgcaCompliance || machine.requiresEarthmovingSafety || safety.isNotBlank()) {
                            item {
                                Surface(
                                    color = MachineryCardWarnBg,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MachineryCardWarnText.copy(alpha = 0.45f))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = MachineryCardWarnText, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = safety.ifBlank {
                                                "Wear protective PPE and follow statutory safety checks before operating machinery."
                                            },
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp,
                                            color = MachineryCardWarnText
                                        )
                                    }
                                }
                            }
                        }

                        if (machine.demoVideoUrl.isNotBlank()) {
                            item {
                                OutlinedButton(
                                    onClick = {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(machine.demoVideoUrl)))
                                        }.onFailure {
                                            Toast.makeText(context, "Unable to open video link", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                ) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = RoyalGold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Watch Equipment Demo Video", fontSize = 12.sp, color = RoyalTextPrimary)
                                }
                            }
                        }
                    }

                    2 -> {
                        // Local Dealers & Offers (Zero Fabrication + Anti-Fraud Protection)
                        item {
                            AntiFraudWarningBanner(language = currentLanguage)
                        }
                        item {
                            SafeBuyerProtectionCard(
                                businessName = machine.sellerBusinessName,
                                locationAddress = machine.sellerLocation,
                                verifiedPhone = if (machine.hasConfirmedSellerContact()) machine.sellerPhone else null,
                                isVerified = isSellerVerified,
                                isAuthorisedDealer = isAuthorisedDealer,
                                isSuspended = isSellerSuspended,
                                lastVerifiedDate = machine.lastUpdatedDate,
                                warrantyOrInvoiceNote = machine.warrantyInfo.ifBlank { "Always request GST tax invoice & warranty card." },
                                language = currentLanguage,
                                onReportFraudClick = { showFraudDialog = true }
                            )
                        }
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MachineryCardWhite),
                                border = BorderStroke(1.dp, MachineryCardBorder),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Source & Confirmed Seller Contact", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MachineryCardTitleInk)
                                    DetailRow("Source / Catalog", "${machine.sellerBusinessName} (${machine.sellerLocation})")
                                    if (!isSellerSuspended && machine.hasConfirmedSellerContact()) {
                                        Button(
                                            onClick = {
                                                runCatching {
                                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${machine.sellerPhone}")))
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Call Verified Seller: ${machine.sellerPhone}", fontSize = 12.sp)
                                        }
                                    } else {
                                        Text(
                                            text = if (isSellerSuspended) {
                                                "Seller contact disabled while under trust & safety review."
                                            } else {
                                                "No unverified phone numbers are displayed. Request a quotation or browse verified Siliguri & North Bengal shops."
                                            },
                                            fontSize = 11.sp,
                                            color = MachineryCardBodyInk
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = onOpenDealers,
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp), tint = MachineryCardBadgeText)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Open Siliguri & North Bengal Dealer Directory", fontSize = 12.sp, color = MachineryCardBadgeText)
                                    }
                                }
                            }
                        }

                        if (activeOffers.isEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MachineryCardSoftMint),
                                    border = BorderStroke(1.dp, MachineryCardBorder)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("No Active Local Dealer Offers Yet", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MachineryCardTitleInk)
                                        Text(
                                            text = "Tap 'Buy / Quote' below to request a formal quotation from verified Siliguri or North Bengal dealers.",
                                            fontSize = 11.sp,
                                            color = MachineryCardBodyInk
                                        )
                                    }
                                }
                            }
                        } else {
                            items(activeOffers, key = { it.offerId }) { offer ->
                                val linkedDealer = dealersMap[offer.dealerId]
                                val dealerVerified = linkedDealer?.isVerified == true &&
                                    linkedDealer.verificationStatus != NurseryVerificationStatus.PENDING_VERIFICATION
                                val dealerAuthorized = linkedDealer?.isAuthorisedDealerVerified == true
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MachineryCardWhite),
                                    border = BorderStroke(1.dp, MachineryCardBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(offer.dealerName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MachineryCardTitleInk, modifier = Modifier.weight(1f))
                                            SellerTierBadge(
                                                isVerified = dealerVerified,
                                                isAuthorisedDealer = dealerAuthorized,
                                                language = currentLanguage
                                            )
                                        }
                                        Text("Locality: ${offer.localityCode} • Stock: ${offer.stockStatus}", fontSize = 11.sp, color = MachineryCardBodyInk)
                                        val priceDisplay = offer.dealerPriceInr?.let { "Dealer Price: ₹${it.toInt()}" }
                                            ?: offer.mrpInr?.let { "MRP Ref: ₹${it.toInt()}" }
                                            ?: "Price on request — confirm directly with dealer"
                                        Text(priceDisplay, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MachineryCardBadgeText)
                                        if (offer.isOutdatedPrice) {
                                            Text("⚠ Flagged outdated — re-confirm latest quote before purchase.", fontSize = 10.sp, color = MachineryCardWarnText)
                                        }
                                        if (dealerVerified && !linkedDealer?.phone.isNullOrBlank()) {
                                            Button(
                                                onClick = {
                                                    runCatching {
                                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${linkedDealer?.phone}")))
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                                                modifier = Modifier.heightIn(min = 48.dp)
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Call Verified Dealer", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Subsidies & Cost Comparison
                        item {
                            MachinerySubsidyInfoCard(
                                subsidyCodes = machine.getSubsidyCodes(),
                                lang = currentLanguage
                            )
                        }
                        item {
                            MachineryCostComparisonCard(
                                product = machine,
                                offers = activeOffers,
                                lang = currentLanguage
                            )
                        }
                    }

                    4 -> {
                        // Farmer Reviews & Issue Reporting
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MachineryCardWhite),
                                border = BorderStroke(1.dp, MachineryCardBorder),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val avgRating = if (reviews.isEmpty()) null else reviews.map { it.rating }.average()
                                    Text(
                                        text = if (avgRating != null) {
                                            "Farmer Reviews (${reviews.size}) • Avg ${String.format("%.1f", avgRating)} ★"
                                        } else {
                                            "Farmer Reviews (Be the first to review)"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MachineryCardTitleInk
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        (1..5).forEach { star ->
                                            IconButton(
                                                onClick = { reviewRating = star },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (star <= reviewRating) Icons.Default.Star else Icons.Default.StarBorder,
                                                    contentDescription = "$star stars",
                                                    tint = if (star <= reviewRating) RoyalGold else MachineryCardBodyInk
                                                )
                                            }
                                        }
                                    }
                                    OutlinedTextField(
                                        value = reviewTitle,
                                        onValueChange = { reviewTitle = it },
                                        label = { Text("Headline (optional)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = reviewComment,
                                        onValueChange = { reviewComment = it },
                                        label = { Text("Share field performance or dealer experience *") },
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth().testTag("review_comment_input")
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    val res = repo.submitReview(
                                                        MachineryReviewRecord(
                                                            machineId = machine.id,
                                                            userId = userId,
                                                            userName = farmerName.ifBlank { "Krishak Sathi Farmer" },
                                                            rating = reviewRating,
                                                            title = reviewTitle,
                                                            comment = reviewComment
                                                        )
                                                    )
                                                    if (res.isSuccess) {
                                                        reviewTitle = ""
                                                        reviewComment = ""
                                                        Toast.makeText(context, "Review saved", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            res.exceptionOrNull()?.message ?: "Please enter at least 5 characters",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                                            modifier = Modifier.heightIn(min = 48.dp).testTag("submit_review_btn")
                                        ) { Text("Post Review", fontSize = 12.sp) }

                                        OutlinedButton(
                                            onClick = {
                                                isIssueReport = true
                                                showQuotationForm = true
                                            },
                                            modifier = Modifier.heightIn(min = 48.dp)
                                        ) {
                                            Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp), tint = MachineryCardWarnText)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Report Issue", fontSize = 11.sp, color = MachineryCardWarnText)
                                        }
                                    }
                                }
                            }
                        }

                        items(reviews, key = { it.id }) { rev ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MachineryCardSoftMint),
                                border = BorderStroke(1.dp, MachineryCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(rev.userName.ifBlank { "Farmer" }, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MachineryCardTitleInk)
                                        Text("${"★".repeat(rev.rating)} (${rev.rating}/5)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MachineryCardWarnText)
                                    }
                                    if (rev.title.isNotBlank()) {
                                        Text(rev.title, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MachineryCardTitleInk)
                                    }
                                    Text(rev.comment, fontSize = 11.sp, color = MachineryCardBodyInk)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFullScreenPhoto && photos.isNotEmpty()) {
        MachineryFullScreenImageViewer(
            photoTokens = photos,
            title = machine.getDisplayName(currentLanguage),
            subtitle = machine.getDisplayBrandModel(currentLanguage),
            initialIndex = zoomInitialIndex,
            onDismiss = { showFullScreenPhoto = false }
        )
    }

    if (showQuotationForm) {
        MachineryQuotationFormDialog(
            machine = machine,
            currentLanguage = currentLanguage,
            userId = userId,
            farmerName = farmerName,
            isIssueReport = isIssueReport,
            onDismiss = { showQuotationForm = false },
            onSubmit = { rec ->
                scope.launch {
                    val res = repo.submitInquiryOrReport(rec)
                    Toast.makeText(
                        context,
                        if (res.isSuccess) "Submitted (${rec.inquiryType})" else (res.exceptionOrNull()?.message ?: "Failed"),
                        Toast.LENGTH_SHORT
                    ).show()
                    showQuotationForm = false
                }
            }
        )
    }

    if (showRentalForm) {
        MachineryRentalFormDialog(
            machine = machine,
            currentLanguage = currentLanguage,
            userId = userId,
            farmerName = farmerName,
            onDismiss = { showRentalForm = false },
            onSubmit = { req ->
                scope.launch {
                    val res = repo.submitRentalRequest(req)
                    Toast.makeText(
                        context,
                        if (res.isSuccess) "Rental request sent (awaiting confirmation)" else (res.exceptionOrNull()?.message ?: "Duplicate or failed request"),
                        Toast.LENGTH_LONG
                    ).show()
                    showRentalForm = false
                }
            }
        )
    }

    localServiceBooking?.let { svc ->
        ServiceBookingDialog(
            serviceType = svc,
            currentLanguage = currentLanguage,
            userId = userId,
            farmerName = farmerName,
            onDismiss = { localServiceBooking = null },
            onSubmit = { req ->
                scope.launch {
                    val res = repo.submitServiceRequest(req)
                    Toast.makeText(
                        context,
                        if (res.isSuccess) "Service booking requested" else (res.exceptionOrNull()?.message ?: "Duplicate or failed booking"),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    if (showFraudDialog) {
        FraudReportDialog(
            targetName = "${machine.getDisplayName(currentLanguage)} — ${machine.sellerBusinessName}",
            language = currentLanguage,
            onDismiss = { showFraudDialog = false },
            onSubmit = { reasonCode, description ->
                scope.launch {
                    val res = trustRepo.submitFraudReport(
                        targetType = FraudTargetType.MACHINERY,
                        targetId = machine.id,
                        targetName = "${machine.getDisplayName(currentLanguage)} (${machine.sellerBusinessName})",
                        sellerUserId = machine.sellerUserId,
                        reasonCode = reasonCode,
                        description = description,
                        reporterUserId = userId,
                        reporterName = farmerName
                    )
                    Toast.makeText(
                        context,
                        if (res.isSuccess) "Fraud report submitted to Admin Trust Team" else (res.exceptionOrNull()?.message ?: "Failed"),
                        Toast.LENGTH_LONG
                    ).show()
                    showFraudDialog = false
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$label:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MachineryCardBadgeText,
            modifier = Modifier.width(114.dp)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = MachineryCardTitleInk,
            modifier = Modifier.weight(1f)
        )
    }
}
