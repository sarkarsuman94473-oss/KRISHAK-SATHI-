package com.example.ui.nursery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.BotanicalPhotoStage
import com.example.data.datasource.PlantKingdomCatalog
import com.example.data.datasource.PlantMediaRecord
import com.example.data.datasource.PlantVarietyAndMediaCatalog
import com.example.data.datasource.RegionalSuitabilityTag
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.remote.NurseryApplicationRepository
import com.example.data.remote.NurseryProductRepository
import com.example.data.repository.PlantKingdomRepository
import com.example.data.repository.TrustAndSafetyRepository
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalSuccessBg
import com.example.ui.theme.RoyalSuccessText
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.RoyalWarningBg
import com.example.ui.theme.RoyalWarningText
import com.example.util.L10n
import com.example.util.safeStartActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlantDetailScreen(
    plantId: String,
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onOpenDirectory: () -> Unit,
    onOpenMarketplace: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val plantRepo = remember { PlantKingdomRepository(context) }
    val appRepo = remember { NurseryApplicationRepository(context) }
    val productRepo = remember { NurseryProductRepository(context) }
    val trustRepo = remember { TrustAndSafetyRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        plantRepo.ensureSeeded()
    }

    val allPlants by plantRepo.observeAllPlants().collectAsState(initial = emptyList())
    val unifiedNurseries by plantRepo.observeUnifiedNurseries(appRepo.allApplicationsFlow).collectAsState(initial = emptyList())
    val approvedProducts by productRepo.observeApprovedMarketplaceProducts().collectAsState(initial = emptyList())

    val plant = remember(allPlants, plantId) {
        allPlants.firstOrNull { it.plantId.equals(plantId, ignoreCase = true) }
            ?: PlantKingdomCatalog.getById(plantId)
            ?: PlantKingdomCatalog.allPlants.first()
    }

    val stageGallery = remember(plant.plantId, allPlants) {
        PlantVarietyAndMediaCatalog.getStageMediaGallery(context, plant)
    }
    var selectedStage by remember(plant.plantId) { mutableStateOf(BotanicalPhotoStage.YOUNG_SAPLING) }
    val activeStageRecord = remember(stageGallery, selectedStage) {
        stageGallery.firstOrNull { it.stage == selectedStage } ?: stageGallery.first()
    }
    var fullScreenRecord by remember { mutableStateOf<PlantMediaRecord?>(null) }

    val varieties = remember(plant.plantId) {
        PlantVarietyAndMediaCatalog.getVarietiesForPlant(plant.plantId)
    }
    var selectedVariety by remember(plant.plantId) { mutableStateOf(varieties.firstOrNull()) }
    val regionTags = remember(plant) {
        PlantVarietyAndMediaCatalog.inferRegionalSuitability(plant)
    }

    val favPrefs = remember(context) {
        context.applicationContext.getSharedPreferences("krishak_fav_plants_v1", Context.MODE_PRIVATE)
    }
    var isFavourite by remember(plant.plantId) {
        mutableStateOf(favPrefs.getBoolean("fav_${plant.plantId}", false))
    }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportDescription by remember { mutableStateOf("") }
    var showInquiryDialog by remember { mutableStateOf(false) }

    val verifiedStockingNurseries = remember(unifiedNurseries, plant) {
        val explicitIds = plant.verifiedNurseryIdsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        unifiedNurseries.filter { n ->
            n.isVerified && (explicitIds.contains(n.nurseryId) ||
                n.availablePlantIdsCsv.split(",").any { it.trim().equals(plant.plantId, ignoreCase = true) })
        }
    }

    val matchingPartnerProducts = remember(approvedProducts, plant) {
        approvedProducts.filter { p ->
            p.linkedPlantId.equals(plant.plantId, ignoreCase = true) ||
                p.productName.contains(plant.nameEn, ignoreCase = true) ||
                (plant.nameBn.length > 2 && p.productName.contains(plant.nameBn, ignoreCase = true))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Column {
                        Text(
                            text = plant.getDisplayName(currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = plant.getGroupEnum().getDisplayName(currentLanguage),
                            fontSize = 11.sp,
                            color = RoyalSoftGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp).testTag("plant_detail_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RoyalGreenSecondaryBg,
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .testTag("plant_detail_lazy_column"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero Visual & Names Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, RoyalBorder),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            PlantStageMediaArtwork(
                                record = activeStageRecord,
                                plant = plant,
                                lang = currentLanguage,
                                onClick = { fullScreenRecord = activeStageRecord },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(216.dp)
                            )

                            Text(
                                text = L10n.t("plant_stages_title", currentLanguage),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalSoftGold
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                stageGallery.forEach { rec ->
                                    val selected = rec.stage == selectedStage
                                    FilterChip(
                                        selected = selected,
                                        onClick = { selectedStage = rec.stage },
                                        modifier = Modifier.testTag("plant_stage_${rec.stage.code}"),
                                        label = { Text(rec.stage.getDisplayName(currentLanguage), fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RoyalGreenEmerald,
                                            selectedLabelColor = RoyalSoftGold
                                        )
                                    )
                                }
                            }

                            Text(
                                text = "${activeStageRecord.sourceName} • ${activeStageRecord.attribution}",
                                fontSize = 11.sp,
                                color = RoyalTextSecondary
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val group = plant.getGroupEnum()
                                Surface(
                                    color = RoyalGreenEmerald,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${group.emoji} ${group.getDisplayName(currentLanguage)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalTextPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                if (plant.isRare) {
                                    Surface(
                                        color = RoyalGold,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "✨ দুর্লভ ও বিশেষ প্রজাতি"
                                                AppLanguage.HINDI -> "✨ दुर्लभ व विशेष प्रजाति"
                                                AppLanguage.ENGLISH -> "✨ Rare & Specialty Species"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalGreenSecondaryBg,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                if (plant.isDraftReviewRequired()) {
                                    Surface(
                                        color = RoyalWarningBg,
                                        border = BorderStroke(1.dp, RoyalWarningText.copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "⚠ খসড়া — কৃষিবিদ পর্যালোচনা ও যাচাই বাকি (DRAFT_REVIEW_REQUIRED)"
                                                AppLanguage.HINDI -> "⚠ ड्राफ्ट — कृषि विशेषज्ञ समीक्षा शेष (DRAFT_REVIEW_REQUIRED)"
                                                AppLanguage.ENGLISH -> "⚠ DRAFT — Pending Agronomic Review & Suitability Verification"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalWarningText,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = plant.getDisplayName(currentLanguage),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = RoyalTextPrimary
                            )
                            Text(
                                text = "EN: ${plant.nameEn}  •  BN: ${plant.nameBn}  •  HI: ${plant.nameHi}",
                                style = MaterialTheme.typography.bodySmall,
                                color = RoyalTextSecondary
                            )
                            if (plant.scientificName.isNotBlank()) {
                                Text(
                                    text = "${when (currentLanguage) {
                                        AppLanguage.BENGALI -> "বৈজ্ঞানিক নাম"
                                        AppLanguage.HINDI -> "वानस्पतिक नाम"
                                        AppLanguage.ENGLISH -> "Botanical Name"
                                    }}: ${plant.scientificName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic,
                                    color = RoyalSoftGold
                                )
                            }
                            Text(
                                text = plant.getDisplayDescription(currentLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = RoyalTextPrimary
                            )
                        }
                    }
                }

                // Regional Climate Suitability Card
                item {
                    val isHillOnlyTemperate = !regionTags.contains(RegionalSuitabilityTag.NORTH_BENGAL_PLAINS) &&
                        !regionTags.contains(RegionalSuitabilityTag.TERAI_DOOARS) &&
                        (regionTags.contains(RegionalSuitabilityTag.TEMPERATE) ||
                            regionTags.contains(RegionalSuitabilityTag.DARJEELING_KALIMPONG_HILLS))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, RoyalBorder),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("plant_regional_suitability_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = L10n.t("plant_regional_suitability_title", currentLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RoyalSoftGold
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                regionTags.forEach { tag ->
                                    Surface(
                                        color = RoyalGreenSecondaryBg,
                                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = tag.getDisplayName(currentLanguage),
                                            color = RoyalSoftGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            if (isHillOnlyTemperate) {
                                Surface(
                                    color = RoyalWarningBg,
                                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = RoyalWarningText, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "উষ্ণ সমতল এলাকায় এই শীতল-জলবায়ুর গাছের ফলন হয় না; কেবল পাহাড়ি বা নিয়ন্ত্রিত পরিবেশেই উপযোগী।"
                                                AppLanguage.HINDI -> "यह ठंडे पहाड़ी क्षेत्र की प्रजाति गर्म मैदानी इलाकों के लिए उपयुक्त नहीं है।"
                                                AppLanguage.ENGLISH -> "Temperate / hill chill species — not recommended for warm open plains."
                                            },
                                            color = RoyalWarningText,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Verified Cultivars & Varieties Card (when applicable)
                if (varieties.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.65f)),
                            colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("plant_varieties_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "${L10n.t("plant_varieties_title", currentLanguage)} (${varieties.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalSoftGold
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    varieties.forEach { v ->
                                        val isSel = selectedVariety?.varietyId == v.varietyId
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { selectedVariety = v },
                                            modifier = Modifier.testTag("plant_variety_chip_${v.varietyId}"),
                                            label = { Text(v.getDisplayName(currentLanguage), fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = RoyalGreenEmerald,
                                                selectedLabelColor = RoyalSoftGold
                                            )
                                        )
                                    }
                                }

                                selectedVariety?.let { activeVar ->
                                    Surface(
                                        color = RoyalGreenSecondaryBg,
                                        border = BorderStroke(1.dp, RoyalBorder),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = activeVar.getDisplayName(currentLanguage),
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalTextPrimary,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = activeVar.getSpecialty(currentLanguage),
                                                color = RoyalTextSecondary,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "${label(currentLanguage, "মৌসুম", "मौसम", "Season")}: ${activeVar.getSeason(currentLanguage)}",
                                                color = RoyalSoftGold,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    color = RoyalWarningBg,
                                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("plant_sapling_verification_disclaimer")
                                ) {
                                    Text(
                                        text = PlantVarietyAndMediaCatalog.getSaplingVerificationDisclaimer(currentLanguage),
                                        color = RoyalWarningText,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Cultivation & Agronomic Specifications Card (All Module 7 Fields)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, RoyalBorder),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "📋 সম্পূর্ণ উদ্ভিদ ও চাষাবাদের বিবরণ"
                                    AppLanguage.HINDI -> "📋 संपूर्ण वनस्पति व खेती विवरण"
                                    AppLanguage.ENGLISH -> "📋 Botanical & Growing Requirements"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RoyalSoftGold
                            )

                            HorizontalDivider(color = RoyalBorder)

                            SpecRow(Icons.Default.Thermostat, label(currentLanguage, "জলবায়ু ও তাপমাত্রা", "जलवायु और तापमान", "Climate & Temperature"), "${plant.climate} (${plant.temperatureRange})")
                            SpecRow(Icons.Default.Landscape, label(currentLanguage, "মাটি", "मिट्टी", "Soil Requirements"), plant.soilRequirements)
                            SpecRow(Icons.Default.WbSunny, label(currentLanguage, "সূর্যালোক", "धूप", "Sunlight"), plant.sunlight)
                            SpecRow(Icons.Default.WaterDrop, label(currentLanguage, "জল/সেচ", "सिंचाई / पानी", "Watering Frequency"), plant.wateringFrequency)
                            SpecRow(Icons.Default.Eco, label(currentLanguage, "সার ও পুষ্টি", "उर्वरक व खाद", "Fertilizer Guidance"), plant.fertilizerGuidance)
                            SpecRow(Icons.Default.LocalFlorist, label(currentLanguage, "বংশবিস্তার / কলম", "प्रवर्धन विधि", "Propagation Method"), plant.propagationMethod)
                            SpecRow(Icons.Default.CalendarMonth, label(currentLanguage, "ফুল বা ফলের মৌসুম", "फूल / फल का मौसम", "Flowering / Fruiting Season"), plant.floweringOrFruitingSeason)
                            SpecRow(Icons.Default.Height, label(currentLanguage, "বৃদ্ধি ও উচ্চতা", "वृद्धि व आकार", "Growth Size"), plant.growthSize)
                            SpecRow(Icons.Default.Storefront, label(currentLanguage, "ইনডোর / আউটডোর", "इंडोर / आउटडोर", "Indoor / Outdoor"), plant.indoorOutdoorSuitability)
                            SpecRow(Icons.Default.Landscape, label(currentLanguage, "সমতল বনাম পাহাড়", "मैदानी व पहाड़ी उपयुक्तता", "Plains vs Hill Suitability"), plant.plainsHillSuitability)
                            SpecRow(Icons.Default.BugReport, label(currentLanguage, "সাধারণ রোগ ও পোকা", "प्रमुख कीट व रोग", "Common Pests & Diseases"), plant.commonPestsDiseases)
                        }
                    }
                }

                // Full Care Instructions
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f)),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenSecondaryBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "🌱 পরিচর্যা নির্দেশিকা"
                                    AppLanguage.HINDI -> "🌱 देखभाल निर्देश"
                                    AppLanguage.ENGLISH -> "🌱 Full Care Instructions"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RoyalSoftGold
                            )
                            Text(
                                text = plant.getDisplayCareInstructions(currentLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = RoyalTextPrimary
                            )
                        }
                    }
                }

                // Verified Nurseries & Partner Marketplace Listings
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, RoyalBorder),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = RoyalSuccessText, modifier = Modifier.size(20.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "যাচাইকৃত নার্সারি ও পার্টনার স্টক"
                                        AppLanguage.HINDI -> "सत्यापित नर्सरी व पार्टनर स्टॉक"
                                        AppLanguage.ENGLISH -> "Verified Nurseries & Partner Stock"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalTextPrimary
                                )
                            }

                            if (matchingPartnerProducts.isEmpty() && verifiedStockingNurseries.isEmpty()) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "বর্তমানে এই প্রজাতির জন্য কোনো যাচাইকৃত পার্টনার বাণিজ্যিক লিস্টিং যুক্ত হয়নি। ডিরেক্টরি থেকে নিকটবর্তী নার্সারি দেখুন।"
                                        AppLanguage.HINDI -> "वर्तमान में इस प्रजाति के लिए कोई सत्यापित व्यावसायिक लिस्टिंग नहीं जुड़ी है। निर्देशिका से नज़दीकी नर्सरी देखें।"
                                        AppLanguage.ENGLISH -> "No verified commercial partner stock linked yet for this species. Browse verified district nurseries below."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RoyalTextSecondary
                                )
                            } else {
                                matchingPartnerProducts.forEach { prod ->
                                    Surface(
                                        color = RoyalSuccessBg,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, RoyalBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(onClick = onOpenMarketplace)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = prod.getDisplayName(currentLanguage),
                                                    fontWeight = FontWeight.Bold,
                                                    color = RoyalSuccessText,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "${prod.getDisplayNurseryName(currentLanguage)} • ${prod.getDisplayLocation(currentLanguage)}",
                                                    color = RoyalTextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = "₹${prod.effectivePrice.toInt()}",
                                                fontWeight = FontWeight.ExtraBold,
                                                color = RoyalSoftGold,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }
                                }

                                verifiedStockingNurseries.forEach { nursery ->
                                    Surface(
                                        color = RoyalGreenSecondaryBg,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, RoyalBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(onClick = onOpenDirectory)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = nursery.getDisplayName(currentLanguage),
                                                    fontWeight = FontWeight.Bold,
                                                    color = RoyalTextPrimary,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = nursery.getDistrictEnum().getDisplayName(currentLanguage),
                                                    color = RoyalTextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = nursery.verificationStatus,
                                                color = RoyalSuccessText,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onOpenDirectory,
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, RoyalGold)
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalSoftGold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "নার্সারি ডিরেক্টরি"
                                            AppLanguage.HINDI -> "नर्सरी निर्देशिका"
                                            AppLanguage.ENGLISH -> "Nursery Directory"
                                        },
                                        fontSize = 12.sp,
                                        color = RoyalSoftGold
                                    )
                                }

                                Button(
                                    onClick = onOpenMarketplace,
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
                                ) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "মার্কেটপ্লেস"
                                            AppLanguage.HINDI -> "मार्केटप्लेस"
                                            AppLanguage.ENGLISH -> "Marketplace"
                                        },
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Functional Farmer Actions Bar
                item {
                    val primaryVerifiedNursery = verifiedStockingNurseries.firstOrNull { !it.phone.isNullOrBlank() }
                        ?: verifiedStockingNurseries.firstOrNull()

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f)),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("plant_action_bar_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showInquiryDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("plant_action_inquiry_btn")
                                ) {
                                    Icon(Icons.Default.RequestQuote, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(L10n.t("plant_action_inquiry", currentLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val phone = primaryVerifiedNursery?.phone?.trim().orEmpty()
                                        if (phone.isNotBlank()) {
                                            safeStartActivity(
                                                context,
                                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")),
                                                L10n.t("plant_info_unavailable", currentLanguage)
                                            )
                                        } else {
                                            Toast.makeText(context, L10n.t("plant_info_unavailable", currentLanguage), Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    border = BorderStroke(1.dp, RoyalGold),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("plant_action_call_btn")
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalSoftGold)
                                    Spacer(Modifier.width(6.dp))
                                    Text(L10n.t("plant_action_call", currentLanguage), fontSize = 12.sp, color = RoyalSoftGold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val lat = primaryVerifiedNursery?.latitude
                                        val lng = primaryVerifiedNursery?.longitude
                                        if (lat != null && lng != null) {
                                            val labelName = Uri.encode(primaryVerifiedNursery.getDisplayName(currentLanguage))
                                            safeStartActivity(
                                                context,
                                                Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng($labelName)")),
                                                L10n.t("plant_info_unavailable", currentLanguage)
                                            )
                                        } else {
                                            Toast.makeText(context, L10n.t("plant_info_unavailable", currentLanguage), Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    border = BorderStroke(1.dp, RoyalBorder),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("plant_action_directions_btn")
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalTextPrimary)
                                    Spacer(Modifier.width(6.dp))
                                    Text(L10n.t("plant_action_directions", currentLanguage), fontSize = 12.sp, color = RoyalTextPrimary)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val nextVal = !isFavourite
                                        favPrefs.edit().putBoolean("fav_${plant.plantId}", nextVal).apply()
                                        isFavourite = nextVal
                                    },
                                    border = BorderStroke(1.dp, if (isFavourite) RoyalGold else RoyalBorder),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("plant_action_favourite_btn")
                                ) {
                                    Icon(
                                        imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isFavourite) RoyalGold else RoyalTextPrimary
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(L10n.t("plant_action_favourite", currentLanguage), fontSize = 12.sp, color = RoyalTextPrimary)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val shareText = "${plant.getDisplayName(currentLanguage)} (${plant.scientificName})\n${plant.getDisplayCareInstructions(currentLanguage)}"
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                        }
                                        safeStartActivity(
                                            context,
                                            Intent.createChooser(shareIntent, L10n.t("plant_action_share", currentLanguage)),
                                            L10n.t("plant_info_unavailable", currentLanguage)
                                        )
                                    },
                                    border = BorderStroke(1.dp, RoyalBorder),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("plant_action_share_btn")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalTextPrimary)
                                    Spacer(Modifier.width(6.dp))
                                    Text(L10n.t("plant_action_share", currentLanguage), fontSize = 12.sp, color = RoyalTextPrimary)
                                }

                                OutlinedButton(
                                    onClick = { showReportDialog = true },
                                    border = BorderStroke(1.dp, RoyalWarningText.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("plant_action_report_btn")
                                ) {
                                    Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalWarningText)
                                    Spacer(Modifier.width(6.dp))
                                    Text(L10n.t("plant_action_report", currentLanguage), fontSize = 12.sp, color = RoyalWarningText)
                                }
                            }
                        }
                    }
                }

                // Provenance & Source Transparency Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, RoyalBorder),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenSecondaryBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "${label(currentLanguage, "সূত্র", "स्रोत", "Source")}: ${plant.sourceReference}",
                                fontSize = 11.sp,
                                color = RoyalTextSecondary
                            )
                            Text(
                                text = "${label(currentLanguage, "সর্বশেষ যাচাই", "अंतिम सत्यापन", "Last Verified")}: ${plant.lastVerifiedDate} • Visual Key: ${plant.visualAssetKey}",
                                fontSize = 11.sp,
                                color = RoyalTextSecondary
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    fullScreenRecord?.let { rec ->
        PlantFullScreenMediaDialog(
            record = rec,
            plant = plant,
            lang = currentLanguage,
            onDismiss = { fullScreenRecord = null }
        )
    }

    if (showInquiryDialog) {
        AlertDialog(
            onDismissRequest = { showInquiryDialog = false },
            containerColor = RoyalGreenCardBg,
            titleContentColor = RoyalGold,
            textContentColor = RoyalTextPrimary,
            title = { Text(L10n.t("plant_action_contact", currentLanguage), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = if (matchingPartnerProducts.isNotEmpty() || verifiedStockingNurseries.isNotEmpty()) {
                        L10n.t("trust_fair_price_note", currentLanguage)
                    } else {
                        "${L10n.t("plant_info_unavailable", currentLanguage)} — ${L10n.t("trust_fair_price_note", currentLanguage)}"
                    },
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInquiryDialog = false
                        onOpenMarketplace()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
                ) {
                    Text(L10n.t("plant_action_quote", currentLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { showInquiryDialog = false }) {
                    Text(L10n.t("btn_close", currentLanguage), color = RoyalSoftGold)
                }
            }
        )
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            containerColor = RoyalGreenCardBg,
            titleContentColor = RoyalGold,
            textContentColor = RoyalTextPrimary,
            title = { Text(L10n.t("plant_action_report", currentLanguage), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = reportDescription,
                    onValueChange = { reportDescription = it },
                    label = { Text(L10n.t("trust_report_fraud", currentLanguage)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val session = UserSessionManager.getSession(context)
                        coroutineScope.launch {
                            val res = trustRepo.submitFraudReport(
                                targetType = "PLANT_CATALOG",
                                targetId = plant.plantId,
                                targetName = plant.nameEn,
                                sellerUserId = "",
                                reasonCode = "WRONG_IMAGE_OR_INFO",
                                description = reportDescription.ifBlank { "Reported issue on ${plant.nameEn} catalog entry." },
                                reporterUserId = session?.userId ?: "guest_reporter",
                                reporterName = session?.userName ?: "Farmer"
                            )
                            Toast.makeText(
                                context,
                                if (res.isSuccess) L10n.t("grievance_submit_btn", currentLanguage) else L10n.t("plant_info_unavailable", currentLanguage),
                                Toast.LENGTH_SHORT
                            ).show()
                            showReportDialog = false
                            reportDescription = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
                ) {
                    Text(L10n.t("grievance_submit_btn", currentLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text(L10n.t("btn_close", currentLanguage), color = RoyalSoftGold)
                }
            }
        )
    }
}

@Composable
private fun SpecRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = RoyalSoftGold,
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalSoftGold
            )
            Text(
                text = value,
                fontSize = 13.sp,
                color = RoyalTextPrimary
            )
        }
    }
}

private fun label(lang: AppLanguage, bn: String, hi: String, en: String): String = when (lang) {
    AppLanguage.BENGALI -> bn
    AppLanguage.HINDI -> hi
    AppLanguage.ENGLISH -> en
}
