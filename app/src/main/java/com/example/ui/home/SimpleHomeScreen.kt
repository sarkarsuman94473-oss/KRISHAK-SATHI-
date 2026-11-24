package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.datasource.CropMasterDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.MasterCrop
import com.example.data.repository.ExpertRepository
import com.example.ui.crops.CropThumbnailVisual
import com.example.ui.footer.AppFooter
import com.example.ui.info.LegalTab
import com.example.ui.theme.EarthCardBorder
import com.example.ui.theme.EarthInkPrimary
import com.example.ui.theme.EarthInkSecondary
import com.example.ui.theme.EarthSurfaceWhite
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenHighlight
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.royalOutlinedTextFieldColors
import com.example.util.L10n
import com.example.util.isCompactNarrowScreen
import com.example.util.isExpandedScreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimpleHomeScreen(
    farmerName: String,
    language: AppLanguage,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onCardClick: (String) -> Unit,
    onVoiceClick: () -> Unit,
    onProfileClick: () -> Unit,
    onOpenCropDetail: (MasterCrop) -> Unit = { _ -> onCardClick("crops") },
    onNavigateAbout: () -> Unit = {},
    onNavigateContact: () -> Unit = {},
    onNavigateLegal: (LegalTab) -> Unit = {},
    onNavigateGovtInfo: () -> Unit = {},
    onNavigateExperts: () -> Unit = {},
    onNavigateFaq: () -> Unit = {},
    onNavigateLeadingFarmers: () -> Unit = {},
    onLanguageChange: (AppLanguage) -> Unit = {}
) {
    val context = LocalContext.current
    val expertRepository = remember { ExpertRepository(context) }
    LaunchedEffect(expertRepository) {
        expertRepository.seedInitialExpertsIfEmpty()
    }
    val verifiedExperts by expertRepository.verifiedExperts.collectAsState(initial = emptyList())

    val popularCrops = remember {
        listOf("tomato", "potato", "rice_paddy", "tea", "rose", "mango")
            .mapNotNull { CropMasterDatabase.getCropById(it) }
    }

    var searchQuery by remember { mutableStateOf("") }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val isNarrow = isCompactNarrowScreen()
    val isExpanded = isExpandedScreen()
    val columns = if (isExpanded) 3 else 2
    val horizontalPad = if (isNarrow) 12.dp else 16.dp

    val normalizedQuery = searchQuery.trim()
    val filteredSections = remember(normalizedQuery, language) {
        if (normalizedQuery.isBlank()) {
            DashboardCatalog.coreSections
        } else {
            DashboardCatalog.coreSections.mapNotNull { sec ->
                val matching = sec.tiles.filter { tile ->
                    L10n.t(tile.titleKey, language).contains(normalizedQuery, ignoreCase = true) ||
                        L10n.t(tile.subKey, language).contains(normalizedQuery, ignoreCase = true) ||
                        L10n.t(sec.titleKey, language).contains(normalizedQuery, ignoreCase = true)
                }
                if (matching.isNotEmpty()) sec.copy(tiles = matching) else null
            }
        }
    }

    val filteredExtraTools = remember(normalizedQuery, language) {
        if (normalizedQuery.isBlank()) {
            DashboardCatalog.additionalToolCards
        } else {
            DashboardCatalog.additionalToolCards.filter { tile ->
                L10n.t(tile.titleKey, language).contains(normalizedQuery, ignoreCase = true) ||
                    L10n.t(tile.subKey, language).contains(normalizedQuery, ignoreCase = true)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 940.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Original Royal Green Header (Logo, Greeting, Quick Actions, Language Pills, Search, Weather & Voice)
                item {
                    Surface(
                        color = RoyalGreenSecondaryBg,
                        border = BorderStroke(1.dp, RoyalBorder),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_royal_header")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(RoyalGreenSecondaryBg, RoyalGreenPrimaryBg)
                                    )
                                )
                                .padding(horizontal = horizontalPad, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, RoyalGold, CircleShape)
                                            .background(RoyalGreenCardBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_krishak_sathi_logo),
                                            contentDescription = "KRISHAK SATHI Logo",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = L10n.t("app_title", language).uppercase(),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp,
                                            color = RoyalGold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = L10n.format("greeting_farmer", language, farmerName),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalTextPrimary,
                                            lineHeight = 22.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = L10n.t("home_subtitle", language),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = RoyalTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(modifier = Modifier.size(48.dp)) {
                                        IconButton(
                                            onClick = { showNotificationsDialog = true },
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(RoyalGreenCardBg, CircleShape)
                                                .border(1.dp, RoyalBorder, CircleShape)
                                                .testTag("home_notifications_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = L10n.t("home_notifications_title", language),
                                                tint = RoyalSoftGold
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .size(9.dp)
                                                .clip(CircleShape)
                                                .background(RoyalGold)
                                        )
                                    }

                                    IconButton(
                                        onClick = onToggleTheme,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(RoyalGreenCardBg, CircleShape)
                                            .border(1.dp, RoyalBorder, CircleShape)
                                            .testTag("home_theme_toggle_btn")
                                    ) {
                                        Icon(
                                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = if (isDarkTheme) L10n.t("theme_toggle_white", language) else L10n.t("theme_toggle_black", language),
                                            tint = RoyalTextSecondary
                                        )
                                    }

                                    IconButton(
                                        onClick = onProfileClick,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(RoyalGreenEmerald, CircleShape)
                                            .border(1.5.dp, RoyalGold.copy(alpha = 0.7f), CircleShape)
                                            .testTag("home_profile_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = L10n.t("profile", language),
                                            tint = RoyalTextPrimary
                                        )
                                    }
                                }
                            }

                            // Language Switcher Quick Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    val selected = language == lang
                                    val shortLabel = when (lang) {
                                        AppLanguage.BENGALI -> "বাংলা"
                                        AppLanguage.HINDI -> "हिन्दी"
                                        AppLanguage.ENGLISH -> "English"
                                    }
                                    Surface(
                                        color = if (selected) RoyalGreenEmerald else RoyalGreenCardBg,
                                        border = BorderStroke(
                                            width = if (selected) 1.5.dp else 1.dp,
                                            color = if (selected) RoyalGold else RoyalBorder
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .clickable { onLanguageChange(lang) }
                                            .testTag("home_lang_${lang.code}")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = shortLabel,
                                                color = if (selected) RoyalSoftGold else RoyalTextSecondary,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Working Dashboard Search Bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = L10n.t("dash_search_hint", language),
                                        color = RoyalTextSecondary,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = L10n.t("dash_search_hint", language),
                                        tint = RoyalSoftGold
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { searchQuery = "" },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear search",
                                                tint = RoyalTextSecondary
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = royalOutlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("home_search_input")
                            )

                            // Weather & Voice Quick Strip
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                maxItemsInEachRow = if (isNarrow) 1 else 2
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 66.dp)
                                        .clickable { onCardClick("weather") }
                                        .testTag("home_weather_banner"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                    border = BorderStroke(1.dp, RoyalBorder),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(RoyalGreenEmerald.copy(alpha = 0.35f), CircleShape)
                                                .border(1.dp, RoyalGold.copy(alpha = 0.55f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "🌦", fontSize = 20.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = L10n.t("home_weather", language),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = L10n.t("home_weather_sample", language),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 11.sp,
                                                color = RoyalSoftGold,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 66.dp)
                                        .clickable { onVoiceClick() }
                                        .testTag("home_voice_action_card"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                    border = BorderStroke(1.dp, RoyalBorder),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(RoyalGreenEmerald, CircleShape)
                                                .border(1.dp, RoyalGold.copy(alpha = 0.55f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = "Voice Input",
                                                tint = RoyalSoftGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = L10n.t("home_voice", language),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = L10n.t("home_voice_hint", language),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 11.sp,
                                                color = RoyalTextSecondary,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Promo Banner Carousel (hidden only during search)
                if (normalizedQuery.isBlank()) {
                    item {
                        PromoBannerCarousel(
                            currentLanguage = language,
                            onCardClick = onCardClick,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // 3. Verified Expert Consultation Showcase Preview
                if (normalizedQuery.isBlank()) {
                    item {
                        ExpertHomeShowcaseSection(
                            experts = verifiedExperts,
                            language = language,
                            horizontalPad = horizontalPad,
                            isDarkTheme = isDarkTheme,
                            onViewAllExperts = onNavigateExperts,
                            onTalkExpert = { onCardClick("expert") }
                        )
                    }
                }

                // 4. Empty search notice if query yields nothing
                if (normalizedQuery.isNotBlank() && filteredSections.isEmpty() && filteredExtraTools.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPad),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) RoyalGreenCardBg else EarthSurfaceWhite),
                            border = BorderStroke(1.dp, if (isDarkTheme) RoyalBorder else EarthCardBorder)
                        ) {
                            Text(
                                text = L10n.t("dash_search_empty", language),
                                color = if (isDarkTheme) RoyalTextPrimary else EarthInkPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                }

                // 5. Four Categorized Dashboard Sections (Crop Help, Land/Legal, Schemes/Subsidy, Machinery/Marketplace)
                filteredSections.forEach { section ->
                    item(key = "header_${section.sectionId}") {
                        SectionHeadingRow(
                            title = L10n.t(section.titleKey, language),
                            subtitle = L10n.t(section.subKey, language),
                            badgeText = "${section.tiles.size}",
                            modifier = Modifier.padding(horizontal = horizontalPad, vertical = 2.dp)
                        )
                    }

                    items(
                        items = section.tiles.chunked(columns),
                        key = { chunk -> "sec_${section.sectionId}_${chunk.first().id}" }
                    ) { rowCards ->
                        EarthyDashboardTileRow(
                            rowCards = rowCards,
                            columns = columns,
                            language = language,
                            horizontalPad = horizontalPad,
                            isWarmSurface = section.isWarmSurface,
                            isDarkTheme = isDarkTheme,
                            onCardClick = onCardClick
                        )
                    }
                }

                // 6. More Smart Farming Tools (Preserving all legacy quick shortcuts & testTags)
                if (filteredExtraTools.isNotEmpty()) {
                    item {
                        SectionHeadingRow(
                            title = L10n.t("dash_other_tools_title", language),
                            subtitle = L10n.t("home_more_services_title", language),
                            badgeText = "${filteredExtraTools.size}",
                            modifier = Modifier.padding(horizontal = horizontalPad, vertical = 2.dp)
                        )
                    }

                    items(
                        items = filteredExtraTools.chunked(columns),
                        key = { chunk -> "extra_${chunk.first().id}" }
                    ) { rowCards ->
                        EarthyDashboardTileRow(
                            rowCards = rowCards,
                            columns = columns,
                            language = language,
                            horizontalPad = horizontalPad,
                            isWarmSurface = false,
                            isDarkTheme = isDarkTheme,
                            onCardClick = onCardClick
                        )
                    }
                }

                // 7. Popular Crops Horizontal Showcase
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPad),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = L10n.t("home_popular_crops_title", language),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = RoyalTextPrimary
                            )
                            TextButton(
                                onClick = { onCardClick("crops") },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text(
                                    text = L10n.t("home_popular_crops_view_all", language),
                                    color = RoyalSoftGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = RoyalSoftGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = horizontalPad),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(popularCrops, key = { it.cropId }) { crop ->
                                Card(
                                    modifier = Modifier
                                        .width(154.dp)
                                        .clickable { onOpenCropDetail(crop) }
                                        .testTag("home_popular_crop_${crop.cropId}"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDarkTheme) RoyalGreenCardBg else EarthSurfaceWhite
                                    ),
                                    border = BorderStroke(1.dp, if (isDarkTheme) RoyalBorder else EarthCardBorder),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        CropThumbnailVisual(
                                            crop = crop,
                                            lang = language,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(92.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = crop.getName(language),
                                            color = if (isDarkTheme) RoyalTextPrimary else EarthInkPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Surface(
                                            color = RoyalGreenSecondaryBg,
                                            border = BorderStroke(1.dp, RoyalBorder),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = crop.getBestSeason(language),
                                                color = RoyalSoftGold,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 8. Seasonal Farming Guide Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPad, vertical = 4.dp)
                            .testTag("home_seasonal_guide_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = null,
                                        tint = RoyalGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = L10n.t("home_seasonal_guide_title", language),
                                        color = RoyalGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Surface(
                                    color = RoyalGreenEmerald,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = L10n.t("home_seasonal_guide_badge", language),
                                        color = RoyalTextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = L10n.t("home_seasonal_guide_headline", language),
                                color = RoyalTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                lineHeight = 22.sp
                            )
                            Text(
                                text = L10n.t("home_seasonal_guide_desc", language),
                                color = RoyalTextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                            Button(
                                onClick = { onCardClick("resource_library") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoyalGreenEmerald,
                                    contentColor = RoyalTextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text(
                                    text = L10n.t("home_seasonal_guide_cta", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = RoyalSoftGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // 9. Latest Agricultural Updates
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPad, vertical = 4.dp)
                            .testTag("home_updates_section"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionHeadingRow(
                            title = L10n.t("home_updates_title", language),
                            subtitle = null,
                            badgeText = "LIVE",
                            isGold = true
                        )
                        val updates = listOf(
                            Triple("home_update_1_tag", "home_update_1_title", "home_update_1_desc") to "market",
                            Triple("home_update_2_tag", "home_update_2_title", "home_update_2_desc") to "govt_info",
                            Triple("home_update_3_tag", "home_update_3_title", "home_update_3_desc") to "medicine"
                        )
                        updates.forEach { (keys, targetCardId) ->
                            val (tagKey, titleKey, descKey) = keys
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCardClick(targetCardId) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                border = BorderStroke(1.dp, RoyalBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(RoyalGreenSecondaryBg, CircleShape)
                                            .border(1.dp, RoyalBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Campaign,
                                            contentDescription = null,
                                            tint = RoyalSoftGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            color = RoyalGreenEmerald.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = L10n.t(tagKey, language),
                                                color = RoyalSoftGold,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = L10n.t(titleKey, language),
                                            color = RoyalTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = L10n.t(descKey, language),
                                            color = RoyalTextSecondary,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = RoyalGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 10. Daily Farming Tips
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPad, vertical = 4.dp)
                            .testTag("home_tips_section"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenSecondaryBg),
                        border = BorderStroke(1.dp, RoyalBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = RoyalGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = L10n.t("home_tips_title", language),
                                    color = RoyalGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            listOf("home_tip_1", "home_tip_2", "home_tip_3").forEachIndexed { idx, tipKey ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = RoyalGreenEmerald,
                                        shape = CircleShape,
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${idx + 1}",
                                                color = RoyalSoftGold,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = L10n.t(tipKey, language),
                                        color = RoyalTextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // 11. Corporate Footer
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    AppFooter(
                        currentLanguage = language,
                        onNavigateAbout = onNavigateAbout,
                        onNavigateContact = onNavigateContact,
                        onNavigateLegal = onNavigateLegal,
                        onNavigateGovtInfo = onNavigateGovtInfo,
                        onNavigateExperts = onNavigateExperts,
                        onNavigateFaq = onNavigateFaq,
                        onNavigateLeadingFarmers = onNavigateLeadingFarmers
                    )
                }
            }
        }
    }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            containerColor = RoyalGreenCardBg,
            titleContentColor = RoyalGold,
            textContentColor = RoyalTextPrimary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = RoyalGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = L10n.t("home_notifications_title", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val notifs = listOf(
                        Triple("notif_weather_title", "notif_weather_desc", "weather"),
                        Triple("notif_mandi_title", "notif_mandi_desc", "market"),
                        Triple("notif_scheme_title", "notif_scheme_desc", "govt_schemes")
                    )
                    notifs.forEach { (titleKey, descKey, routeId) ->
                        Surface(
                            color = RoyalGreenSecondaryBg,
                            border = BorderStroke(1.dp, RoyalBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showNotificationsDialog = false
                                    onCardClick(routeId)
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = L10n.t(titleKey, language),
                                    color = RoyalSoftGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = L10n.t(descKey, language),
                                    color = RoyalTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNotificationsDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGreenEmerald,
                        contentColor = RoyalTextPrimary
                    ),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(L10n.t("btn_close", language), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SectionHeadingRow(
    title: String,
    subtitle: String?,
    badgeText: String,
    modifier: Modifier = Modifier,
    isGold: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.5.sp,
                lineHeight = 21.sp,
                color = RoyalTextPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    color = RoyalTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Surface(
            color = if (isGold) RoyalGold.copy(alpha = 0.2f) else RoyalGreenCardBg,
            border = BorderStroke(1.dp, if (isGold) RoyalGold else RoyalBorder),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = badgeText,
                color = if (isGold) RoyalSoftGold else RoyalGreenHighlight,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}
