package com.example.ui.nursery

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
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.data.datasource.PlantKingdomCatalog
import com.example.data.model.AppLanguage
import com.example.data.remote.NurseryApplicationRepository
import com.example.data.remote.NurseryProductRepository
import com.example.data.repository.PlantKingdomRepository
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
                            PlantSpeciesArtwork(
                                plant = plant,
                                lang = currentLanguage,
                                showAccuracyBadge = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
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
