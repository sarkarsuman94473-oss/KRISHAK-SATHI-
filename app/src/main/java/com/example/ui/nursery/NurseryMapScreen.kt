package com.example.ui.nursery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.WbNurseryDirectoryEntry
import com.example.data.model.WbNurseryDistrict
import com.example.data.remote.NurseryApplicationRepository
import com.example.data.repository.PlantKingdomRepository
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalSuccessBg
import com.example.ui.theme.RoyalSuccessText
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.RoyalWarningBg
import com.example.ui.theme.RoyalWarningText
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NurseryMapScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val plantRepo = remember { PlantKingdomRepository(context) }
    val appRepo = remember { NurseryApplicationRepository(context) }

    LaunchedEffect(Unit) {
        plantRepo.ensureSeeded()
    }

    val nurseries by plantRepo.observeUnifiedNurseries(appRepo.allApplicationsFlow)
        .collectAsState(initial = emptyList())

    var focusNorthBengal by remember { mutableStateOf(false) }
    var referenceHub by remember { mutableStateOf(WbNurseryDistrict.SILIGURI) }
    var selectedDistrictFilter by remember { mutableStateOf<WbNurseryDistrict?>(null) }
    var filterVerifiedPhoneOnly by remember { mutableStateOf(false) }
    var filterWholesaleOnly by remember { mutableStateOf(false) }
    var filterNearestSort by remember { mutableStateOf(true) }
    var categoryQuery by remember { mutableStateOf<String?>(null) }

    val filtered = remember(
        nurseries,
        selectedDistrictFilter,
        filterVerifiedPhoneOnly,
        filterWholesaleOnly,
        filterNearestSort,
        referenceHub,
        categoryQuery
    ) {
        val base = nurseries.filter { n ->
            val matchDist = selectedDistrictFilter == null || n.districtCode.equals(selectedDistrictFilter!!.code, ignoreCase = true)
            val matchPhone = !filterVerifiedPhoneOnly || n.hasVerifiedPhone()
            val matchWholesale = !filterWholesaleOnly || n.supportsWholesale == true
            val matchCat = categoryQuery == null || n.availablePlantCategoriesCsv.contains(categoryQuery!!, ignoreCase = true)
            matchDist && matchPhone && matchWholesale && matchCat
        }
        if (filterNearestSort) {
            base.sortedBy { entry ->
                val lat = entry.latitude ?: entry.getDistrictEnum().centerLat
                val lng = entry.longitude ?: entry.getDistrictEnum().centerLng
                PlantKingdomRepository.haversineKm(referenceHub.centerLat, referenceHub.centerLng, lat, lng)
            }
        } else {
            base
        }
    }

    val coordinateVerifiedCount = remember(filtered) {
        filtered.count { it.hasVerifiedCoordinates() }
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
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "🗺️ পশ্চিমবঙ্গ নার্সারি ম্যাপ"
                                AppLanguage.HINDI -> "🗺️ पश्चिम बंगाल नर्सरी मैप"
                                AppLanguage.ENGLISH -> "🗺️ West Bengal Nursery Map"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "যাচাইকৃত GPS পিন ও জেলা হাব দূরত্ব"
                                AppLanguage.HINDI -> "सत्यापित GPS पिन व ज़िला केंद्र दूरी"
                                AppLanguage.ENGLISH -> "Verified GPS Pins & District Hub Distances"
                            },
                            fontSize = 11.sp,
                            color = RoyalTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp).testTag("nursery_map_back_btn")
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
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Viewport Toggle & Location Hub Chips
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = !focusNorthBengal,
                                onClick = { focusNorthBengal = false },
                                label = {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "সমগ্র পশ্চিমবঙ্গ"
                                            AppLanguage.HINDI -> "संपूर्ण पश्चिम बंगाल"
                                            AppLanguage.ENGLISH -> "All West Bengal"
                                        },
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalGold,
                                    selectedLabelColor = RoyalGreenSecondaryBg
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = focusNorthBengal,
                                onClick = { focusNorthBengal = true },
                                label = {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "শিলিগুড়ি ও উত্তরবঙ্গ"
                                            AppLanguage.HINDI -> "सिलीगुड़ी व उत्तर बंगाल"
                                            AppLanguage.ENGLISH -> "Siliguri & North Bengal"
                                        },
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalGold,
                                    selectedLabelColor = RoyalGreenSecondaryBg
                                )
                            )
                        }
                    }
                }

                // Interactive Regional Coordinate Canvas Map (plots ONLY verified GPS coordinates + District hubs)
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.65f)),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenSecondaryBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nursery_map_canvas_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(230.dp)
                                    .background(
                                        Brush.verticalGradient(listOf(RoyalGreenPrimaryBg, RoyalGreenCardBg)),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                            ) {
                                val minLat = if (focusNorthBengal) 25.8 else 21.6
                                val maxLat = if (focusNorthBengal) 27.3 else 27.3
                                val minLng = if (focusNorthBengal) 87.8 else 85.9
                                val maxLng = if (focusNorthBengal) 89.8 else 89.8

                                Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                                    // Coordinate grid lines
                                    for (g in 1..4) {
                                        val gx = size.width * (g / 5f)
                                        val gy = size.height * (g / 5f)
                                        drawLine(
                                            color = Color(0xFF286348).copy(alpha = 0.55f),
                                            start = Offset(gx, 0f),
                                            end = Offset(gx, size.height),
                                            strokeWidth = 1.5f
                                        )
                                        drawLine(
                                            color = Color(0xFF286348).copy(alpha = 0.55f),
                                            start = Offset(0f, gy),
                                            end = Offset(size.width, gy),
                                            strokeWidth = 1.5f
                                        )
                                    }

                                    // Plot District Reference Hubs as subtle emerald rings
                                    WbNurseryDistrict.values().forEach { hub ->
                                        if (hub.centerLat in minLat..maxLat && hub.centerLng in minLng..maxLng) {
                                            val nx = ((hub.centerLng - minLng) / (maxLng - minLng)).toFloat()
                                            val ny = 1f - ((hub.centerLat - minLat) / (maxLat - minLat)).toFloat()
                                            val pt = Offset(nx * size.width, ny * size.height)
                                            drawCircle(
                                                color = if (hub == referenceHub) Color(0xFFD6B45C) else Color(0xFF32A66A).copy(alpha = 0.55f),
                                                radius = if (hub == referenceHub) 9f else 5f,
                                                center = pt
                                            )
                                        }
                                    }

                                    // Plot ONLY verified GPS coordinates from database records
                                    filtered.forEach { n ->
                                        val lat = n.latitude
                                        val lng = n.longitude
                                        if (lat != null && lng != null && lat in minLat..maxLat && lng in minLng..maxLng) {
                                            val nx = ((lng - minLng) / (maxLng - minLng)).toFloat()
                                            val ny = 1f - ((lat - minLat) / (maxLat - minLat)).toFloat()
                                            val pt = Offset(nx * size.width, ny * size.height)
                                            drawCircle(color = Color(0xFFA7F3D0), radius = 11f, center = pt)
                                            drawCircle(color = Color(0xFF087A50), radius = 6f, center = pt)
                                        }
                                    }
                                }

                                Surface(
                                    color = Color(0xFF031B13).copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "📍 যাচাইকৃত GPS পিন: $coordinateVerifiedCount • জেলা হাব: ${referenceHub.bnName}"
                                            AppLanguage.HINDI -> "📍 सत्यापित GPS पिन: $coordinateVerifiedCount • केंद्र: ${referenceHub.hiName}"
                                            AppLanguage.ENGLISH -> "📍 Verified GPS Pins: $coordinateVerifiedCount • Hub: ${referenceHub.enName}"
                                        },
                                        color = RoyalSoftGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "স্বচ্ছতা নীতি: কেবলমাত্র যাচাইকৃত GPS স্থানাঙ্কযুক্ত নার্সারি ম্যাপ পিন হিসেবে প্রদর্শিত হয়।"
                                    AppLanguage.HINDI -> "पारदर्शिता नीति: केवल सत्यापित GPS निर्देशांक वाली नर्सरियाँ ही मानचित्र पिन के रूप में दिखती हैं।"
                                    AppLanguage.ENGLISH -> "Accuracy Note: Only nurseries with verified GPS coordinates are plotted as pins (zero estimated fake pins)."
                                },
                                fontSize = 11.sp,
                                color = RoyalTextSecondary
                            )
                        }
                    }
                }

                // 5 Map Filters
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = filterNearestSort,
                                onClick = { filterNearestSort = !filterNearestSort },
                                label = {
                                    Text(
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "নিকটতম নার্সারি"
                                            AppLanguage.HINDI -> "निकटतम नर्सरी"
                                            AppLanguage.ENGLISH -> "Nearest Nursery"
                                        },
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalGreenEmerald, selectedLabelColor = RoyalSoftGold)
                            )
                        }
                        item {
                            FilterChip(
                                selected = filterVerifiedPhoneOnly,
                                onClick = { filterVerifiedPhoneOnly = !filterVerifiedPhoneOnly },
                                label = {
                                    Text(
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "ফোন যাচাইকৃত"
                                            AppLanguage.HINDI -> "फ़ोन सत्यापित"
                                            AppLanguage.ENGLISH -> "Verified Phone Only"
                                        },
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalGreenEmerald, selectedLabelColor = RoyalSoftGold)
                            )
                        }
                        item {
                            FilterChip(
                                selected = filterWholesaleOnly,
                                onClick = { filterWholesaleOnly = !filterWholesaleOnly },
                                label = {
                                    Text(
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "পাইকারি নার্সারি"
                                            AppLanguage.HINDI -> "थोक नर्सरी"
                                            AppLanguage.ENGLISH -> "Wholesale Nurseries"
                                        },
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalGreenEmerald, selectedLabelColor = RoyalSoftGold)
                            )
                        }
                        item {
                            FilterChip(
                                selected = categoryQuery == "Flower",
                                onClick = { categoryQuery = if (categoryQuery == "Flower") null else "Flower" },
                                label = { Text("🌺 Flower Plants", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalGreenEmerald, selectedLabelColor = RoyalSoftGold)
                            )
                        }
                        item {
                            FilterChip(
                                selected = categoryQuery == "Fruit",
                                onClick = { categoryQuery = if (categoryQuery == "Fruit") null else "Fruit" },
                                label = { Text("🥭 Fruit Plants", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalGreenEmerald, selectedLabelColor = RoyalSoftGold)
                            )
                        }
                    }
                }

                // Reference Location & District Picker Row
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(
                            listOf(
                                WbNurseryDistrict.SILIGURI,
                                WbNurseryDistrict.DARJEELING,
                                WbNurseryDistrict.KALIMPONG,
                                WbNurseryDistrict.JALPAIGURI,
                                WbNurseryDistrict.COOCH_BEHAR,
                                WbNurseryDistrict.MALDA,
                                WbNurseryDistrict.KOLKATA
                            )
                        ) { hub ->
                            val isSelected = referenceHub == hub
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    referenceHub = hub
                                    selectedDistrictFilter = if (selectedDistrictFilter == hub) null else hub
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(hub.getDisplayName(currentLanguage), fontSize = 11.sp)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalGold, selectedLabelColor = RoyalGreenSecondaryBg)
                            )
                        }
                    }
                }

                items(filtered, key = { it.nurseryId }) { entry ->
                    MapNurseryDistanceCard(
                        entry = entry,
                        referenceHub = referenceHub,
                        lang = currentLanguage
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun MapNurseryDistanceCard(
    entry: WbNurseryDirectoryEntry,
    referenceHub: WbNurseryDistrict,
    lang: AppLanguage
) {
    val hasExactCoords = entry.hasVerifiedCoordinates()
    val distKm = remember(entry, referenceHub) {
        val lat = entry.latitude ?: entry.getDistrictEnum().centerLat
        val lng = entry.longitude ?: entry.getDistrictEnum().centerLng
        PlantKingdomRepository.haversineKm(referenceHub.centerLat, referenceHub.centerLng, lat, lng)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, RoyalBorder),
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = if (hasExactCoords) Icons.Default.Verified else Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (hasExactCoords) RoyalSuccessText else RoyalWarningText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = entry.getDisplayName(lang),
                        fontWeight = FontWeight.Bold,
                        color = RoyalTextPrimary,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "${entry.getDistrictEnum().getDisplayName(lang)} • " + if (hasExactCoords) {
                        "GPS: ${String.format(Locale.US, "%.4f, %.4f", entry.latitude, entry.longitude)}"
                    } else {
                        when (lang) {
                            AppLanguage.BENGALI -> "সঠিক GPS স্থানাঙ্ক যাচাই অপেক্ষমাণ"
                            AppLanguage.HINDI -> "सटीक GPS निर्देशांक सत्यापन लंबित"
                            AppLanguage.ENGLISH -> "Exact GPS Coordinates Pending Verification"
                        }
                    },
                    fontSize = 11.sp,
                    color = RoyalTextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (hasExactCoords) RoyalSuccessBg else RoyalWarningBg
            ) {
                Text(
                    text = if (hasExactCoords) {
                        String.format(Locale.US, "%.1f km", distKm)
                    } else {
                        "~${distKm.toInt()} km (Hub)"
                    },
                    color = if (hasExactCoords) RoyalSuccessText else RoyalWarningText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
