package com.example.ui.nursery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.PlantKingdomCatalog
import com.example.data.model.AppLanguage
import com.example.data.model.PlantKingdomEntry
import com.example.data.model.PlantKingdomGroup
import com.example.data.model.PlantSearchFilter
import com.example.data.model.PlantSeasonalCollection
import com.example.data.repository.PlantKingdomRepository
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalSuccessBg
import com.example.ui.theme.RoyalSuccessText
import com.example.ui.theme.RoyalTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantKingdomLibraryScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onOpenPlantDetail: (String) -> Unit,
    onOpenNurseryDirectory: () -> Unit,
    onOpenNurseryMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val repo = remember { PlantKingdomRepository(context) }
    val allPlants by repo.observeAllPlants().collectAsState(initial = PlantKingdomCatalog.allPlants)

    LaunchedEffect(Unit) {
        repo.ensureSeeded()
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedGroupTabIndex by remember { mutableIntStateOf(0) } // 0 = All, 1..7 = groups
    var selectedFilter by remember { mutableStateOf<PlantSearchFilter?>(null) }
    var selectedSeason by remember { mutableStateOf<PlantSeasonalCollection?>(null) }

    val publishedPlants = remember(allPlants) {
        allPlants.filter { it.isPublishedForPublic() }
    }

    val groups = remember { PlantKingdomGroup.values().toList() }
    val selectedGroup: PlantKingdomGroup? = if (selectedGroupTabIndex == 0) null else groups.getOrNull(selectedGroupTabIndex - 1)

    val filteredPlants = remember(publishedPlants, searchQuery, selectedGroup, selectedFilter, selectedSeason) {
        publishedPlants.filter { plant ->
            val matchGroup = selectedGroup == null || plant.belongsToGroup(selectedGroup)
            val matchFilter = selectedFilter == null || plant.matchesFilter(selectedFilter!!)
            val matchSeason = selectedSeason == null || plant.matchesSeasonal(selectedSeason!!)
            val matchSearch = plant.matchesSearch(searchQuery)
            matchGroup && matchFilter && matchSeason && matchSearch
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
                                AppLanguage.BENGALI -> "🌿 উদ্ভিদ ও ফুল ভাণ্ডার"
                                AppLanguage.HINDI -> "🌿 पौधा एवं पुष्प विश्वकोश"
                                AppLanguage.ENGLISH -> "🌿 Plant & Flower Kingdom"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "${publishedPlants.size}+ প্রজাতি • হিমালয়, ইনডোর, ফল ও ভেষজ"
                                AppLanguage.HINDI -> "${publishedPlants.size}+ प्रजातियाँ • हिमालय, इंडोर, फल व जड़ी-बूटी"
                                AppLanguage.ENGLISH -> "${publishedPlants.size}+ Species • Himalayan, Indoor, Fruit & Herbs"
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
                        modifier = Modifier.size(48.dp).testTag("plant_kingdom_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenNurseryMap,
                        modifier = Modifier.size(48.dp).testTag("plant_kingdom_map_action")
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Nursery Map", tint = RoyalSoftGold)
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
                // Directory + Map Quick Jump Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenNurseryDirectory,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("plant_kingdom_open_directory_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RoyalGold)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(17.dp), tint = RoyalSoftGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "২২ জেলা নার্সারি"
                                AppLanguage.HINDI -> "22 ज़िला नर्सरी"
                                AppLanguage.ENGLISH -> "22-District Nurseries"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalSoftGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenNurseryMap,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("plant_kingdom_open_map_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RoyalBorder)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(17.dp), tint = RoyalSuccessText)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নার্সারি ম্যাপ"
                                AppLanguage.HINDI -> "नर्सरी मानचित्र"
                                AppLanguage.ENGLISH -> "Nursery Map"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalSuccessText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Multilingual Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("plant_kingdom_search_input"),
                    placeholder = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "ফুল, মানি প্ল্যান্ট, অর্কিড, ফল বা বৈজ্ঞানিক নাম খুঁজুন..."
                                AppLanguage.HINDI -> "फूल, मनी प्लांट, ऑर्किड, फल या वैज्ञानिक नाम खोजें..."
                                AppLanguage.ENGLISH -> "Search flowers, money plant, orchids, fruits or scientific name..."
                            },
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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

                // Botanical Group Tabs (All + 7 Modules)
                ScrollableTabRow(
                    selectedTabIndex = selectedGroupTabIndex,
                    edgePadding = 16.dp,
                    containerColor = RoyalGreenSecondaryBg
                ) {
                    Tab(
                        selected = selectedGroupTabIndex == 0,
                        onClick = { selectedGroupTabIndex = 0 },
                        modifier = Modifier.testTag("plant_group_tab_all"),
                        text = {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "🌿 সব (${allPlants.size})"
                                    AppLanguage.HINDI -> "🌿 सभी (${allPlants.size})"
                                    AppLanguage.ENGLISH -> "🌿 All (${allPlants.size})"
                                },
                                fontWeight = if (selectedGroupTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedGroupTabIndex == 0) RoyalSoftGold else RoyalTextSecondary
                            )
                        }
                    )
                    groups.forEachIndexed { idx, group ->
                        val tabIdx = idx + 1
                        val count = remember(allPlants, group) { allPlants.count { it.belongsToGroup(group) } }
                        Tab(
                            selected = selectedGroupTabIndex == tabIdx,
                            onClick = { selectedGroupTabIndex = tabIdx },
                            modifier = Modifier.testTag("plant_group_tab_${group.code}"),
                            text = {
                                Text(
                                    text = "${group.emoji} ${group.getDisplayName(currentLanguage)} ($count)",
                                    fontWeight = if (selectedGroupTabIndex == tabIdx) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedGroupTabIndex == tabIdx) RoyalSoftGold else RoyalTextSecondary
                                )
                            }
                        )
                    }
                }

                // 11 Search Filter Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PlantSearchFilter.values()) { filter ->
                        val selected = selectedFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { selectedFilter = if (selected) null else filter },
                            modifier = Modifier.testTag("plant_filter_${filter.code}"),
                            label = { Text(filter.getDisplayName(currentLanguage), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalGreenEmerald,
                                selectedLabelColor = RoyalSoftGold
                            )
                        )
                    }
                }

                // 7 Seasonal Collections Row
                if (selectedGroup == PlantKingdomGroup.SEASONAL_FLOWERS ||
                    selectedGroup == PlantKingdomGroup.COMMON_FLOWERS ||
                    selectedGroup == null
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(PlantSeasonalCollection.values()) { season ->
                            val selected = selectedSeason == season
                            FilterChip(
                                selected = selected,
                                onClick = { selectedSeason = if (selected) null else season },
                                modifier = Modifier.testTag("plant_season_${season.code}"),
                                label = { Text(season.getDisplayName(currentLanguage), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalGold,
                                    selectedLabelColor = RoyalGreenSecondaryBg
                                )
                            )
                        }
                    }
                }

                // Count Summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "মোট প্রজাতি: ${filteredPlants.size}"
                            AppLanguage.HINDI -> "कुल प्रजातियाँ: ${filteredPlants.size}"
                            AppLanguage.ENGLISH -> "Showing ${filteredPlants.size} species"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = RoyalSoftGold,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (selectedFilter != null || selectedSeason != null || searchQuery.isNotBlank()) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ফিল্টার মুছুন"
                                AppLanguage.HINDI -> "फ़िल्टर साफ़ करें"
                                AppLanguage.ENGLISH -> "Clear filters"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    selectedFilter = null
                                    selectedSeason = null
                                    searchQuery = ""
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Plant Species List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (filteredPlants.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                border = BorderStroke(1.dp, RoyalBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "কোনো গাছ পাওয়া যায়নি"
                                            AppLanguage.HINDI -> "कोई पौधा नहीं मिला"
                                            AppLanguage.ENGLISH -> "No matching plants found"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "অন্য নাম বা ফিল্টার ব্যবহার করে আবার চেষ্টা করুন।"
                                            AppLanguage.HINDI -> "दूसरा नाम या फ़िल्टर आज़माएँ।"
                                            AppLanguage.ENGLISH -> "Try a different plant name, season, or category."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RoyalTextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredPlants, key = { it.plantId }) { plant ->
                            PlantSpeciesListCard(
                                plant = plant,
                                lang = currentLanguage,
                                onClick = { onOpenPlantDetail(plant.plantId) }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PlantSpeciesListCard(
    plant: PlantKingdomEntry,
    lang: AppLanguage,
    onClick: () -> Unit
) {
    val group = plant.getGroupEnum()
    val nurseryCount = remember(plant.verifiedNurseryIdsCsv) {
        plant.verifiedNurseryIdsCsv.split(",").count { it.isNotBlank() }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("plant_card_${plant.plantId}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, RoyalBorder),
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlantSpeciesArtwork(
                plant = plant,
                lang = lang,
                showAccuracyBadge = false,
                modifier = Modifier.size(90.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = RoyalGreenSecondaryBg,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, RoyalBorder)
                    ) {
                        Text(
                            text = "${group.emoji} ${group.getDisplayName(lang)}",
                            fontSize = 10.sp,
                            color = RoyalTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (plant.isRare) {
                        Surface(
                            color = RoyalGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.BENGALI -> "দুর্লভ"
                                    AppLanguage.HINDI -> "दुर्लभ"
                                    AppLanguage.ENGLISH -> "Rare"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalSoftGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = plant.getDisplayName(lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (plant.scientificName.isNotBlank()) {
                    Text(
                        text = plant.scientificName,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = RoyalSoftGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = plant.getDisplayDescription(lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = RoyalTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌡 ${plant.temperatureRange}",
                        fontSize = 11.sp,
                        color = RoyalTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (nurseryCount > 0) {
                        Surface(
                            color = RoyalSuccessBg,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(12.dp), tint = RoyalSuccessText)
                                Text(
                                    text = "$nurseryCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalSuccessText
                                )
                            }
                        }
                    }
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open Plant Details",
                tint = RoyalSoftGold,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
