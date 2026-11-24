package com.example.ui.crops

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CropCategory
import com.example.data.model.CropMasterGroup
import com.example.data.model.CropRegion
import com.example.data.model.CropSeason
import com.example.data.model.MasterCrop
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary

@Composable
fun MasterLibraryFilterHeader(
    currentLanguage: AppLanguage,
    totalCropsCount: Int,
    filteredCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedGroup: CropMasterGroup?,
    onSelectGroup: (CropMasterGroup?) -> Unit,
    groupCounts: Map<CropMasterGroup, Int>,
    availableSubCategories: List<CropCategory>,
    selectedSubCategory: CropCategory?,
    onSelectSubCategory: (CropCategory?) -> Unit,
    selectedSeason: CropSeason?,
    onSelectSeason: (CropSeason?) -> Unit,
    selectedRegion: CropRegion?,
    onSelectRegion: (CropRegion?) -> Unit,
    isAdmin: Boolean,
    onOpenAdminAddCrop: () -> Unit,
    onResetAllFilters: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "🌱 সম্পূর্ণ ফসল ও উদ্ভিদ বিশ্বকোষ"
                        AppLanguage.HINDI -> "🌱 संपूर्ण फसल व पौधा विश्वकोश"
                        AppLanguage.ENGLISH -> "🌱 Complete Crop & Plant Master Library"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "মোট $totalCropsCount প্রজাতি • প্রদর্শিত: $filteredCount (১২ শ্রেণি A–L)"
                        AppLanguage.HINDI -> "कुल $totalCropsCount फसलें/पौधे • प्रदर्शित: $filteredCount (12 श्रेणियां A–L)"
                        AppLanguage.ENGLISH -> "$totalCropsCount total entries • Showing $filteredCount (12 groups A–L)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (isAdmin) {
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onOpenAdminAddCrop,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("my_crops_admin_add_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "নতুন ফসল"
                            AppLanguage.HINDI -> "नई फसल"
                            AppLanguage.ENGLISH -> "Master Crop"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            singleLine = true,
            placeholder = {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "ফসল, ফুল, ফল, বীজ বা বৈজ্ঞানিক নাম খুঁজুন..."
                        AppLanguage.HINDI -> "फसल, फूल, फल, किस्म या वैज्ञानिक नाम खोजें..."
                        AppLanguage.ENGLISH -> "Search crop, flower, fruit, variety, or scientific name..."
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("crops_search_field"),
            shape = RoundedCornerShape(14.dp)
        )

        // 1. 12 Master Groups (A-L)
        FilterSectionLabel(
            text = when (currentLanguage) {
                AppLanguage.BENGALI -> "প্রধান শ্রেণি (A–L):"
                AppLanguage.HINDI -> "मुख्य श्रेणियां (A–L):"
                AppLanguage.ENGLISH -> "Master Categories (A–L):"
            }
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterPill(
                    label = when (currentLanguage) {
                        AppLanguage.BENGALI -> "সব (${totalCropsCount})"
                        AppLanguage.HINDI -> "सभी (${totalCropsCount})"
                        AppLanguage.ENGLISH -> "All (${totalCropsCount})"
                    },
                    selected = selectedGroup == null,
                    onClick = { onSelectGroup(null) },
                    modifier = Modifier.testTag("group_filter_ALL")
                )
            }
            items(CropMasterGroup.values(), key = { it.name }) { group ->
                val count = groupCounts[group] ?: 0
                FilterPill(
                    label = "${group.iconEmoji} ${group.getDisplayName(currentLanguage)} ($count)",
                    selected = selectedGroup == group,
                    onClick = { onSelectGroup(group) },
                    modifier = Modifier.testTag("group_filter_${group.codeLetter}")
                )
            }
        }

        // 2. Specific Sub-category Pills when selected group has multiple sub-categories
        if (selectedGroup != null && availableSubCategories.size > 1) {
            FilterSectionLabel(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "উপ-শ্রেণি:"
                    AppLanguage.HINDI -> "उप-श्रेणी:"
                    AppLanguage.ENGLISH -> "Sub-Category:"
                }
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterPill(
                        label = when (currentLanguage) {
                            AppLanguage.BENGALI -> "সব (${selectedGroup.codeLetter})"
                            AppLanguage.HINDI -> "सभी (${selectedGroup.codeLetter})"
                            AppLanguage.ENGLISH -> "All (${selectedGroup.codeLetter})"
                        },
                        selected = selectedSubCategory == null,
                        onClick = { onSelectSubCategory(null) }
                    )
                }
                items(availableSubCategories, key = { it.name }) { subCat ->
                    FilterPill(
                        label = subCat.getDisplayName(currentLanguage),
                        selected = selectedSubCategory == subCat,
                        onClick = { onSelectSubCategory(subCat) },
                        modifier = Modifier.testTag("subcat_filter_${subCat.code}")
                    )
                }
            }
        }

        // 3. Season Filter
        FilterSectionLabel(
            text = when (currentLanguage) {
                AppLanguage.BENGALI -> "ঋতু / মৌসুম:"
                AppLanguage.HINDI -> "मौसम / सीजन:"
                AppLanguage.ENGLISH -> "Season:"
            }
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterPill(
                    label = when (currentLanguage) {
                        AppLanguage.BENGALI -> "সব ঋতু"
                        AppLanguage.HINDI -> "सभी मौसम"
                        AppLanguage.ENGLISH -> "All Seasons"
                    },
                    selected = selectedSeason == null,
                    onClick = { onSelectSeason(null) },
                    modifier = Modifier.testTag("season_filter_ALL")
                )
            }
            items(CropSeason.values(), key = { it.name }) { season ->
                FilterPill(
                    label = season.getDisplayName(currentLanguage),
                    selected = selectedSeason == season,
                    onClick = { onSelectSeason(season) },
                    modifier = Modifier.testTag("season_filter_${season.code}")
                )
            }
        }

        // 4. Regional Suitability Filter (Siliguri & North Bengal prioritized first)
        FilterSectionLabel(
            text = when (currentLanguage) {
                AppLanguage.BENGALI -> "অঞ্চলভিত্তিক উপযোগিতা:"
                AppLanguage.HINDI -> "क्षेत्रीय उपयुक्तता:"
                AppLanguage.ENGLISH -> "Regional Focus:"
            }
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterPill(
                    label = when (currentLanguage) {
                        AppLanguage.BENGALI -> "সব অঞ্চল"
                        AppLanguage.HINDI -> "सभी क्षेत्र"
                        AppLanguage.ENGLISH -> "All Regions"
                    },
                    selected = selectedRegion == null,
                    onClick = { onSelectRegion(null) },
                    modifier = Modifier.testTag("region_filter_ALL")
                )
            }
            items(CropRegion.values(), key = { it.name }) { region ->
                FilterPill(
                    label = if (region == CropRegion.SILIGURI_NORTH_BENGAL) {
                        "📍 ${region.getDisplayName(currentLanguage)}"
                    } else {
                        region.getDisplayName(currentLanguage)
                    },
                    selected = selectedRegion == region,
                    onClick = { onSelectRegion(region) },
                    modifier = Modifier.testTag("region_filter_${region.code}")
                )
            }
        }

        val hasActiveFilters = searchQuery.isNotBlank() ||
            selectedGroup != null ||
            selectedSubCategory != null ||
            selectedSeason != null ||
            selectedRegion != null

        if (hasActiveFilters) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onResetAllFilters,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("clear_filters_inline_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "ফিল্টার মুছুন"
                            AppLanguage.HINDI -> "फ़िल्टर साफ़ करें"
                            AppLanguage.ENGLISH -> "Clear Filters"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = AgriTextSecondary
    )
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = if (selected) Color.White else AgriTextPrimary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MasterCropLibraryCard(
    crop: MasterCrop,
    currentLanguage: AppLanguage,
    onClick: () -> Unit
) {
    val group = crop.getMasterGroup()
    val regions = crop.getResolvedRegions()
    val isSiliguriSuitable = CropRegion.SILIGURI_NORTH_BENGAL in regions

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("crop_item_${crop.cropId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CropThumbnailVisual(
                crop = crop,
                lang = currentLanguage,
                modifier = Modifier.size(62.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = crop.getName(currentLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = crop.scientificName,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${group.codeLetter} • ${crop.category.getDisplayName(currentLanguage)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (isSiliguriSuitable) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "📍 শিলিগুড়ি-উত্তরবঙ্গ"
                                    AppLanguage.HINDI -> "📍 सिलीगुड़ी-उ. बंगाल"
                                    AppLanguage.ENGLISH -> "📍 Siliguri & NB"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (crop.isCustomAdded) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অ্যাডমিন যুক্ত"
                                    AppLanguage.HINDI -> "एडमिन द्वारा"
                                    AppLanguage.ENGLISH -> "Custom"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "সময়কাল: ${crop.harvestDays} দিন • ফলন: ${crop.getExpectedYield(currentLanguage)}"
                        AppLanguage.HINDI -> "अवधि: ${crop.harvestDays} दिन • उपज: ${crop.getExpectedYield(currentLanguage)}"
                        AppLanguage.ENGLISH -> "Duration: ${crop.harvestDays} days • Yield: ${crop.getExpectedYield(currentLanguage)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = AgriTextSecondary,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MasterLibraryEmptyFilterCard(
    currentLanguage: AppLanguage,
    onResetAllFilters: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "🔍", fontSize = 32.sp)
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "এই ফিল্টারের সাথে মিলছে এমন কোনো ফসল পাওয়া যায়নি।"
                    AppLanguage.HINDI -> "इस खोज व फ़िल्टर से मेल खाने वाली कोई फसल नहीं मिली।"
                    AppLanguage.ENGLISH -> "No crops or plants match the current search and filters."
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AgriTextSecondary
            )
            Button(
                onClick = onResetAllFilters,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreenPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("reset_crop_filters_btn")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "সব ফিল্টার রিসেট করুন"
                        AppLanguage.HINDI -> "सभी फ़िल्टर रीसेट करें"
                        AppLanguage.ENGLISH -> "Reset All Filters"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
