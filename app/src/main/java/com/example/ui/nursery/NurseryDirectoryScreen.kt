package com.example.ui.nursery

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.WbNurseryDirectorySeed
import com.example.data.model.AppLanguage
import com.example.data.model.NurseryVerificationStatus
import com.example.data.model.WbNurseryDirectoryEntry
import com.example.data.model.WbNurseryDistrict
import com.example.data.remote.NurseryApplicationRepository
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
import com.example.util.safeStartActivity

private enum class DirectoryFilterMode(
    val bnLabel: String,
    val hiLabel: String,
    val enLabel: String
) {
    ALL("সব", "सभी", "All"),
    VERIFIED_PARTNERS("যাচাইকৃত পার্টনার", "सत्यापित पार्टनर", "Verified Partners"),
    PENDING("যাচাই অপেক্ষমাণ", "सत्यापन लंबित", "Pending Verification"),
    PHONE_ONLY("ফোন উপলব্ধ", "फ़ोन उपलब्ध", "Verified Phone Only"),
    WHOLESALE("পাইকারি নার্সারি", "थोक नर्सरी", "Wholesale Nurseries");

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnLabel
        AppLanguage.HINDI -> hiLabel
        AppLanguage.ENGLISH -> enLabel
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NurseryDirectoryScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenPlantKingdom: () -> Unit,
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
        .collectAsState(initial = WbNurseryDirectorySeed.initialPendingNurseries)

    var searchQuery by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf<WbNurseryDistrict?>(null) }
    var filterMode by remember { mutableStateOf(DirectoryFilterMode.ALL) }
    var selectedNurseryDetail by remember { mutableStateOf<WbNurseryDirectoryEntry?>(null) }

    val districts = remember { WbNurseryDistrict.values().toList() }

    val filteredNurseries = remember(nurseries, searchQuery, selectedDistrict, filterMode) {
        nurseries.filter { entry ->
            val matchDistrict = selectedDistrict == null || entry.districtCode.equals(selectedDistrict!!.code, ignoreCase = true)
            val matchMode = when (filterMode) {
                DirectoryFilterMode.ALL -> true
                DirectoryFilterMode.VERIFIED_PARTNERS -> entry.isVerified
                DirectoryFilterMode.PENDING -> !entry.isVerified || entry.verificationStatus == NurseryVerificationStatus.PENDING_VERIFICATION
                DirectoryFilterMode.PHONE_ONLY -> entry.hasVerifiedPhone()
                DirectoryFilterMode.WHOLESALE -> entry.supportsWholesale == true
            }
            val matchSearch = entry.matchesSearch(searchQuery)
            matchDistrict && matchMode && matchSearch
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
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "🏪 পশ্চিমবঙ্গ নার্সারি ডিরেক্টরি"
                                AppLanguage.HINDI -> "🏪 पश्चिम बंगाल नर्सरी निर्देशिका"
                                AppLanguage.ENGLISH -> "🏪 West Bengal Nursery Directory"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "শিলিগুড়ি, উত্তরবঙ্গ ও ২২ জেলার নার্সারি"
                                AppLanguage.HINDI -> "सिलीगुड़ी, उत्तर बंगाल व 22 ज़िलों की नर्सरी"
                                AppLanguage.ENGLISH -> "Siliguri, North Bengal & 22 Districts • India-Ready"
                            },
                            fontSize = 11.sp,
                            color = RoyalTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp).testTag("nursery_dir_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenMap,
                        modifier = Modifier.size(48.dp).testTag("nursery_dir_map_btn")
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Open Nursery Map", tint = RoyalSoftGold)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .imePadding()
            ) {
                // Quick Actions: Interactive Map + Plant Kingdom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenMap,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("dir_jump_map_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RoyalGold)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalSoftGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ইন্টারঅ্যাক্টিভ ম্যাপ"
                                AppLanguage.HINDI -> "इंटरैक्टिव मैप"
                                AppLanguage.ENGLISH -> "Interactive Map"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalSoftGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenPlantKingdom,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("dir_jump_plant_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RoyalBorder)
                    ) {
                        Icon(Icons.Default.LocalFlorist, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalSuccessText)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "উদ্ভিদ ভাণ্ডার"
                                AppLanguage.HINDI -> "पौधा विश्वकोश"
                                AppLanguage.ENGLISH -> "Plant Kingdom"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalSuccessText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("nursery_dir_search_input"),
                    placeholder = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "নার্সারির নাম, জেলা বা এলাকা খুঁজুন..."
                                AppLanguage.HINDI -> "नर्सरी का नाम, ज़िला या क्षेत्र खोजें..."
                                AppLanguage.ENGLISH -> "Search nursery name, district or locality..."
                            },
                            fontSize = 13.sp
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

                // Verification / Type Filter Row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DirectoryFilterMode.values()) { mode ->
                        val selected = filterMode == mode
                        FilterChip(
                            selected = selected,
                            onClick = { filterMode = mode },
                            modifier = Modifier.testTag("dir_filter_${mode.name}"),
                            label = { Text(mode.label(currentLanguage), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalGreenEmerald,
                                selectedLabelColor = RoyalSoftGold
                            )
                        )
                    }
                }

                // All 22 District Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedDistrict == null,
                            onClick = { selectedDistrict = null },
                            modifier = Modifier.testTag("dir_district_ALL"),
                            label = {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "সব জেলা (২২)"
                                        AppLanguage.HINDI -> "सभी ज़िले (22)"
                                        AppLanguage.ENGLISH -> "All Districts (22)"
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalGold,
                                selectedLabelColor = RoyalGreenSecondaryBg
                            )
                        )
                    }
                    items(districts) { dist ->
                        val sel = selectedDistrict == dist
                        FilterChip(
                            selected = sel,
                            onClick = { selectedDistrict = if (sel) null else dist },
                            modifier = Modifier.testTag("dir_district_${dist.code}"),
                            label = { Text(dist.getDisplayName(currentLanguage), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalGold,
                                selectedLabelColor = RoyalGreenSecondaryBg
                            )
                        )
                    }
                }

                // Count Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "তালিকাভুক্ত নার্সারি: ${filteredNurseries.size}"
                            AppLanguage.HINDI -> "सूचीबद्ध नर्सरियाँ: ${filteredNurseries.size}"
                            AppLanguage.ENGLISH -> "${filteredNurseries.size} nurseries listed"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = RoyalSoftGold
                    )
                    if (selectedDistrict != null || filterMode != DirectoryFilterMode.ALL || searchQuery.isNotBlank()) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "সব দেখান"
                                AppLanguage.HINDI -> "सब दिखाएँ"
                                AppLanguage.ENGLISH -> "Reset"
                            },
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .clickable {
                                    selectedDistrict = null
                                    filterMode = DirectoryFilterMode.ALL
                                    searchQuery = ""
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNurseries, key = { it.nurseryId }) { nursery ->
                        DirectoryNurseryCard(
                            entry = nursery,
                            lang = currentLanguage,
                            onClick = { selectedNurseryDetail = nursery }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }

    selectedNurseryDetail?.let { entry ->
        DirectoryNurseryDetailDialog(
            entry = entry,
            lang = currentLanguage,
            context = context,
            onDismiss = { selectedNurseryDetail = null },
            onOpenMap = {
                selectedNurseryDetail = null
                onOpenMap()
            }
        )
    }
}

@Composable
private fun DirectoryNurseryCard(
    entry: WbNurseryDirectoryEntry,
    lang: AppLanguage,
    onClick: () -> Unit
) {
    val district = entry.getDistrictEnum()
    val isPending = !entry.isVerified || entry.verificationStatus == NurseryVerificationStatus.PENDING_VERIFICATION

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("dir_nursery_card_${entry.nurseryId}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isPending) RoyalBorder else RoyalGold.copy(alpha = 0.65f)),
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPending) RoyalWarningBg else RoyalSuccessBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPending) Icons.Default.PendingActions else Icons.Default.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isPending) RoyalWarningText else RoyalSuccessText
                        )
                        Text(
                            text = if (isPending) {
                                when (lang) {
                                    AppLanguage.BENGALI -> "যাচাই অপেক্ষমাণ (PENDING VERIFICATION)"
                                    AppLanguage.HINDI -> "सत्यापन लंबित (PENDING VERIFICATION)"
                                    AppLanguage.ENGLISH -> "PENDING VERIFICATION"
                                }
                            } else {
                                when (lang) {
                                    AppLanguage.BENGALI -> "যাচাইকৃত নার্সারি"
                                    AppLanguage.HINDI -> "सत्यापित नर्सरी"
                                    AppLanguage.ENGLISH -> "VERIFIED PARTNER"
                                }
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPending) RoyalWarningText else RoyalSuccessText
                        )
                    }
                }

                Text(
                    text = district.getDisplayName(lang),
                    fontSize = 11.sp,
                    color = RoyalSoftGold,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = entry.getDisplayName(lang),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RoyalTextPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = RoyalSoftGold, modifier = Modifier.size(15.dp))
                Text(
                    text = entry.fullAddress ?: entry.locality ?: when (lang) {
                        AppLanguage.BENGALI -> "ঠিকানা যাচাই অপেক্ষমাণ (${district.bnName})"
                        AppLanguage.HINDI -> "पता सत्यापन लंबित (${district.hiName})"
                        AppLanguage.ENGLISH -> "Address pending verification (${district.enName})"
                    },
                    fontSize = 12.sp,
                    color = RoyalTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (entry.hasVerifiedPhone()) {
                Text(
                    text = "📞 ${entry.phone}",
                    fontSize = 12.sp,
                    color = RoyalSuccessText,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> "📞 যোগাযোগের নম্বর: যাচাইয়ের পর যুক্ত হবে"
                        AppLanguage.HINDI -> "📞 फ़ोन नंबर: सत्यापन के बाद उपलब्ध होगा"
                        AppLanguage.ENGLISH -> "📞 Phone: Pending owner/admin verification"
                    },
                    fontSize = 11.sp,
                    color = RoyalTextSecondary
                )
            }

            Text(
                text = "${entry.sourceReference} • ${entry.lastVerifiedDate}",
                fontSize = 10.sp,
                color = RoyalTextSecondary.copy(alpha = 0.85f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DirectoryNurseryDetailDialog(
    entry: WbNurseryDirectoryEntry,
    lang: AppLanguage,
    context: Context,
    onDismiss: () -> Unit,
    onOpenMap: () -> Unit
) {
    val district = entry.getDistrictEnum()
    val pendingLabel = when (lang) {
        AppLanguage.BENGALI -> "যাচাই অপেক্ষমাণ (প্রদত্ত নয়)"
        AppLanguage.HINDI -> "सत्यापन लंबित (उपलब्ध नहीं)"
        AppLanguage.ENGLISH -> "Not provided — pending verification"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        titleContentColor = RoyalTextPrimary,
        textContentColor = RoyalTextPrimary,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(entry.getDisplayName(lang), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "${district.getDisplayName(lang)}, ${entry.state}",
                    fontSize = 12.sp,
                    color = RoyalSoftGold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = if (entry.isVerified) RoyalSuccessBg else RoyalWarningBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = entry.verificationStatus,
                        color = if (entry.isVerified) RoyalSuccessText else RoyalWarningText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                HorizontalDivider(color = RoyalBorder)

                InfoField(label(lang, "মালিক / কর্তৃপক্ষ", "स्वामी / संपर्क", "Owner Name"), entry.ownerName ?: pendingLabel)
                InfoField(label(lang, "সম্পূর্ণ ঠিকানা", "पूरा पता", "Full Address"), entry.fullAddress ?: entry.locality ?: pendingLabel)
                InfoField(label(lang, "ফোন নম্বর", "फ़ोन नंबर", "Phone Number"), entry.phone ?: pendingLabel)
                InfoField(label(lang, "হোয়াটসঅ্যাপ", "व्हाट्सएप", "WhatsApp"), entry.whatsapp ?: pendingLabel)
                InfoField(
                    label(lang, "খোলার সময়", "खुलने का समय", "Opening Hours"),
                    entry.openingHours ?: pendingLabel
                )
                InfoField(
                    label(lang, "জিপিএস অবস্থান", "GPS निर्देशांक", "GPS Coordinates"),
                    if (entry.latitude != null && entry.longitude != null) "${entry.latitude}, ${entry.longitude}" else pendingLabel
                )
                InfoField(
                    label(lang, "উপলব্ধ গাছের ধরন", "उपलब्ध पौध श्रेणियाँ", "Available Plant Categories"),
                    entry.availablePlantCategoriesCsv.ifBlank { pendingLabel }
                )
                InfoField(
                    label(lang, "পাইকারি / খুচরা ও ডেলিভারি", "थोक / खुदरा व डिलीवरी", "Wholesale / Retail & Delivery"),
                    buildString {
                        append(when (entry.supportsWholesale) {
                            true -> "Wholesale: Yes"
                            false -> "Wholesale: No"
                            null -> "Wholesale: Pending"
                        })
                        append(" • ")
                        append(when (entry.deliveryAvailable) {
                            true -> "Delivery: Yes"
                            false -> "Delivery: No"
                            null -> "Delivery: Pending"
                        })
                    }
                )
                InfoField(
                    label(lang, "তথ্যসূত্র ও যাচাই তারিখ", "स्रोत व सत्यापन तिथि", "Source & Last Checked"),
                    "${entry.sourceReference} (${entry.lastVerifiedDate})"
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!entry.phone.isNullOrBlank()) {
                        Button(
                            onClick = {
                                val clean = entry.phone.replace(" ", "").replace("-", "")
                                safeStartActivity(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")), entry.phone)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", fontSize = 12.sp)
                        }
                    }
                    if (!entry.whatsapp.isNullOrBlank()) {
                        Button(
                            onClick = {
                                val clean = entry.whatsapp.replace("+", "").replace(" ", "")
                                safeStartActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$clean")), entry.whatsapp)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.Whatsapp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 12.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = onOpenMap,
                        border = BorderStroke(1.dp, RoyalGold),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalSoftGold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(label(lang, "ম্যাপে দেখুন", "मैप देखें", "Open Map"), fontSize = 12.sp, color = RoyalSoftGold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(label(lang, "বন্ধ করুন", "बंद करें", "Close"), color = RoyalSoftGold)
            }
        }
    )
}

@Composable
private fun InfoField(title: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalSoftGold)
        Text(value, fontSize = 13.sp, color = RoyalTextPrimary)
    }
}

private fun label(lang: AppLanguage, bn: String, hi: String, en: String): String = when (lang) {
    AppLanguage.BENGALI -> bn
    AppLanguage.HINDI -> hi
    AppLanguage.ENGLISH -> en
}
