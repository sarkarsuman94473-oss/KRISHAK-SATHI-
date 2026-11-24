package com.example.ui.schemes

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.GovernmentScheme
import com.example.data.model.SchemeActivityFocus
import com.example.data.model.SchemeApplicationStatus
import com.example.data.model.SchemeCategory
import com.example.data.model.SchemeFallbackText
import com.example.data.model.SchemeFarmerType
import com.example.data.model.SchemeGovLevel
import com.example.data.remote.GovernmentSchemesRepository
import com.example.ui.theme.RoyalAlertBg
import com.example.ui.theme.RoyalAlertText
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
import com.example.ui.theme.RoyalWarningBg
import com.example.ui.theme.royalOutlinedTextFieldColors
import com.example.util.L10n

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GovernmentSchemesScreen(
    currentLanguage: AppLanguage,
    userId: String,
    onBack: () -> Unit,
    repositoryOverride: GovernmentSchemesRepository? = null,
    initialCategory: SchemeCategory? = null,
    initialSearchQuery: String = ""
) {
    val context = LocalContext.current
    val isAdmin = remember(userId) { UserSessionManager.isAdmin(context) }
    val repository = remember(repositoryOverride) { repositoryOverride ?: GovernmentSchemesRepository(context) }
    val viewModel = remember(repository, userId, isAdmin) {
        GovernmentSchemesViewModel(
            repository = repository,
            currentAdminUid = userId,
            isAdmin = isAdmin
        )
    }

    LaunchedEffect(initialCategory, initialSearchQuery) {
        if (initialCategory != null) {
            viewModel.setCategory(initialCategory)
        }
        if (initialSearchQuery.isNotBlank()) {
            viewModel.setSearchQuery(initialSearchQuery)
        }
    }

    val allState by viewModel.allSchemesState.collectAsStateWithLifecycle()
    val schemes by viewModel.filteredSchemes.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val selectedDetail by viewModel.selectedScheme.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsStateWithLifecycle()
    val cachedCount by viewModel.cachedSchemesCount.collectAsStateWithLifecycle()

    var showAdvancedFilters by remember { mutableStateOf(false) }
    var editingScheme by remember { mutableStateOf<GovernmentScheme?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    LaunchedEffect(actionMessage) {
        if (!actionMessage.isNullOrBlank()) {
            Toast.makeText(context, actionMessage, Toast.LENGTH_SHORT).show()
            viewModel.consumeActionMessage()
        }
    }

    val detail = selectedDetail
    if (detail != null) {
        BackHandler {
            viewModel.openSchemeDetail(null)
        }
        GovernmentSchemeDetailScreen(
            scheme = detail,
            currentLanguage = currentLanguage,
            isAdmin = isAdmin,
            isBookmarked = detail.id in bookmarkedIds,
            onToggleBookmark = {
                viewModel.toggleBookmark(
                    schemeId = detail.id,
                    addedMessage = L10n.t("schemes_bookmark_added_toast", currentLanguage),
                    removedMessage = L10n.t("schemes_bookmark_removed_toast", currentLanguage)
                )
            },
            onBack = { viewModel.openSchemeDetail(null) },
            onVisitOfficialWebsite = { url ->
                viewModel.openOfficialWebsite(context, url, currentLanguage)
            },
            onAdminEdit = {
                isCreatingNew = false
                editingScheme = detail
            },
            onAdminUpdateStatus = { newStatus, verifiedDate ->
                viewModel.updateAdminSchemeStatus(detail, newStatus, verifiedDate)
            }
        )
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = RoyalGreenPrimaryBg,
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
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("schemes_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = L10n.t("btn_back", currentLanguage),
                                tint = RoyalGold
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = L10n.t("schemes_title", currentLanguage),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = L10n.t("schemes_subtitle", currentLanguage),
                                fontSize = 11.sp,
                                color = RoyalTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        if (isAdmin) {
                            IconButton(
                                onClick = {
                                    isCreatingNew = true
                                    editingScheme = GovernmentScheme(
                                        govLevel = SchemeGovLevel.WEST_BENGAL.name,
                                        category = SchemeCategory.WEST_BENGAL_SCHEMES.name,
                                        northBengalPriority = 90,
                                        applicationStatus = SchemeApplicationStatus.OPEN.name
                                    )
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("scheme_admin_add_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = L10n.t("schemes_admin_add", currentLanguage),
                                    tint = RoyalGold
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(RoyalGreenPrimaryBg),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 920.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Official Transparency & Disclaimer Banner
                    item {
                        Surface(
                            color = RoyalGreenSecondaryBg,
                            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("scheme_disclaimer_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = RoyalGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = L10n.t("schemes_disclaimer_title", currentLanguage),
                                        color = RoyalSoftGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = SchemeFallbackText.disclaimer(currentLanguage),
                                        color = RoyalTextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // 2. Search Bar
                    item {
                        OutlinedTextField(
                            value = filters.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("scheme_search_input"),
                            placeholder = {
                                Text(
                                    text = L10n.t("schemes_search_hint", currentLanguage),
                                    fontSize = 13.sp,
                                    color = RoyalTextSecondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = RoyalGold
                                )
                            },
                            trailingIcon = {
                                if (filters.searchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { viewModel.setSearchQuery("") },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = L10n.t("schemes_clear_filters", currentLanguage),
                                            tint = RoyalTextSecondary
                                        )
                                    }
                                }
                            },
                            colors = royalOutlinedTextFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }

                    // 3. Saved Bookmarks & Government Level Filter Chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                SchemeSelectChip(
                                    label = L10n.t("schemes_filter_all_gov", currentLanguage),
                                    selected = filters.govLevel == null && !filters.savedOnly,
                                    onClick = {
                                        viewModel.setGovLevel(null)
                                        viewModel.setSavedOnly(false)
                                    },
                                    tag = "scheme_gov_ALL"
                                )
                            }
                            item {
                                val savedLabel = if (bookmarkedIds.isNotEmpty()) {
                                    "${L10n.t("schemes_filter_saved_only", currentLanguage)} (${bookmarkedIds.size})"
                                } else {
                                    L10n.t("schemes_filter_saved_only", currentLanguage)
                                }
                                SchemeSelectChip(
                                    label = savedLabel,
                                    selected = filters.savedOnly,
                                    onClick = { viewModel.toggleSavedOnly() },
                                    tag = "scheme_filter_saved_only"
                                )
                            }
                            items(SchemeGovLevel.entries) { level ->
                                SchemeSelectChip(
                                    label = level.getLabel(currentLanguage),
                                    selected = filters.govLevel == level,
                                    onClick = {
                                        viewModel.setGovLevel(if (filters.govLevel == level) null else level)
                                    },
                                    tag = "scheme_gov_${level.name}"
                                )
                            }
                        }
                    }

                    // 4. All 18 Scheme Category Chips
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = L10n.t("schemes_categories_label", currentLanguage),
                                color = RoyalSoftGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    SchemeSelectChip(
                                        label = L10n.t("schemes_filter_all_categories", currentLanguage),
                                        selected = filters.category == null,
                                        onClick = { viewModel.setCategory(null) },
                                        tag = "scheme_cat_ALL"
                                    )
                                }
                                items(SchemeCategory.entries) { category ->
                                    SchemeSelectChip(
                                        label = category.getLabel(currentLanguage),
                                        selected = filters.category == category,
                                        onClick = {
                                            viewModel.setCategory(if (filters.category == category) null else category)
                                        },
                                        tag = "scheme_cat_${category.name}"
                                    )
                                }
                            }
                        }
                    }

                    // 5. Advanced Filters Toggle + Clear Filters + Count
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { showAdvancedFilters = !showAdvancedFilters },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("scheme_advanced_filters_toggle")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = RoyalGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = L10n.t("schemes_more_filters", currentLanguage),
                                        color = RoyalSoftGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (filters.activeFilterCount > 0) {
                                        TextButton(
                                            onClick = { viewModel.clearFilters() },
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("scheme_clear_filters_btn")
                                        ) {
                                            Text(
                                                text = L10n.t("schemes_clear_filters", currentLanguage),
                                                color = RoyalGold,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    if (cachedCount > 0) {
                                        Surface(
                                            color = RoyalGreenSecondaryBg,
                                            border = BorderStroke(1.dp, RoyalGreenHighlight.copy(alpha = 0.6f)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("scheme_offline_cache_badge")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.OfflinePin,
                                                    contentDescription = null,
                                                    tint = RoyalGreenHighlight,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = L10n.t("schemes_offline_cache_badge", currentLanguage),
                                                    color = RoyalGreenHighlight,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Surface(
                                        color = RoyalGreenCardBg,
                                        border = BorderStroke(1.dp, RoyalBorder),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${schemes.size} ${L10n.t("schemes_count_label", currentLanguage)}",
                                            color = RoyalGreenHighlight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(visible = showAdvancedFilters) {
                                Surface(
                                    color = RoyalGreenSecondaryBg,
                                    border = BorderStroke(1.dp, RoyalBorder),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = L10n.t("schemes_filter_farmer_type", currentLanguage),
                                            color = RoyalSoftGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            SchemeFarmerType.entries.forEach { ft ->
                                                SchemeSelectChip(
                                                    label = ft.getLabel(currentLanguage),
                                                    selected = filters.farmerType == ft,
                                                    onClick = {
                                                        viewModel.setFarmerType(if (filters.farmerType == ft) null else ft)
                                                    },
                                                    tag = "scheme_farmer_${ft.name}"
                                                )
                                            }
                                        }

                                        Text(
                                            text = L10n.t("schemes_filter_activity", currentLanguage),
                                            color = RoyalSoftGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            SchemeActivityFocus.entries.forEach { af ->
                                                SchemeSelectChip(
                                                    label = af.getLabel(currentLanguage),
                                                    selected = filters.activityFocus == af,
                                                    onClick = {
                                                        viewModel.setActivityFocus(if (filters.activityFocus == af) null else af)
                                                    },
                                                    tag = "scheme_activity_${af.name}"
                                                )
                                            }
                                        }

                                        Text(
                                            text = L10n.t("schemes_filter_status", currentLanguage),
                                            color = RoyalSoftGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            SchemeApplicationStatus.entries.forEach { st ->
                                                SchemeSelectChip(
                                                    label = st.getLabel(currentLanguage),
                                                    selected = filters.status == st,
                                                    onClick = {
                                                        viewModel.setStatus(if (filters.status == st) null else st)
                                                    },
                                                    tag = "scheme_status_${st.name}"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. Schemes List / Loading / Empty
                    when (allState) {
                        is SchemeUiState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = RoyalGold)
                                }
                            }
                        }
                        is SchemeUiState.Error -> {
                            item {
                                Surface(
                                    color = RoyalAlertBg,
                                    border = BorderStroke(1.dp, RoyalAlertText.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = (allState as SchemeUiState.Error).message,
                                        color = RoyalAlertText,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                        is SchemeUiState.Success -> {
                            if (schemes.isEmpty()) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                        border = BorderStroke(1.dp, RoyalBorder),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = L10n.t(
                                                    if (filters.savedOnly) "schemes_no_bookmarks_title" else "schemes_empty_title",
                                                    currentLanguage
                                                ),
                                                color = RoyalTextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = L10n.t(
                                                    if (filters.savedOnly) "schemes_no_bookmarks_desc" else "schemes_empty_desc",
                                                    currentLanguage
                                                ),
                                                color = RoyalTextSecondary,
                                                fontSize = 13.sp
                                            )
                                            Button(
                                                onClick = { viewModel.clearFilters() },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = RoyalGreenEmerald,
                                                    contentColor = RoyalTextPrimary
                                                ),
                                                modifier = Modifier.heightIn(min = 48.dp)
                                            ) {
                                                Text(L10n.t("schemes_clear_filters", currentLanguage))
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(schemes, key = { it.id }) { scheme ->
                                    GovernmentSchemeCard(
                                        scheme = scheme,
                                        language = currentLanguage,
                                        isAdmin = isAdmin,
                                        isBookmarked = scheme.id in bookmarkedIds,
                                        onToggleBookmark = {
                                            viewModel.toggleBookmark(
                                                schemeId = scheme.id,
                                                addedMessage = L10n.t("schemes_bookmark_added_toast", currentLanguage),
                                                removedMessage = L10n.t("schemes_bookmark_removed_toast", currentLanguage)
                                            )
                                        },
                                        onVisitOfficialWebsite = {
                                            viewModel.openOfficialWebsite(context, scheme.officialUrl, currentLanguage)
                                        },
                                        onViewDetails = {
                                            viewModel.openSchemeDetail(scheme.id)
                                        },
                                        onAdminEdit = {
                                            isCreatingNew = false
                                            editingScheme = scheme
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val draft = editingScheme
    if (isAdmin && draft != null) {
        GovernmentSchemeAdminDialog(
            initialScheme = draft,
            isNew = isCreatingNew,
            currentLanguage = currentLanguage,
            onDismiss = { editingScheme = null },
            onSave = { updatedScheme, allowUrlOverwrite ->
                viewModel.saveAdminScheme(
                    scheme = updatedScheme,
                    isNew = isCreatingNew,
                    allowUrlOverwrite = allowUrlOverwrite
                ) { ok ->
                    if (ok) {
                        editingScheme = null
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GovernmentSchemeCard(
    scheme: GovernmentScheme,
    language: AppLanguage,
    isAdmin: Boolean,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onVisitOfficialWebsite: () -> Unit,
    onViewDetails: () -> Unit,
    onAdminEdit: () -> Unit
) {
    val govLevel = scheme.resolveGovLevel()
    val status = scheme.resolveApplicationStatus()
    val statusColor = when (status) {
        SchemeApplicationStatus.OPEN -> RoyalGreenEmerald
        SchemeApplicationStatus.UPCOMING -> Color(0xFF8A6512)
        SchemeApplicationStatus.CLOSED -> Color(0xFF6B3A2C)
        SchemeApplicationStatus.DISCONTINUED -> Color(0xFF7F1D1D)
        SchemeApplicationStatus.INFO_ONLY -> RoyalGreenSecondaryBg
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("scheme_card_${scheme.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        border = BorderStroke(
            width = 1.dp,
            color = if (scheme.northBengalPriority >= 95 && !scheme.discontinued) RoyalGold.copy(alpha = 0.65f) else RoyalBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Metadata Badges + Offline Bookmark Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = if (govLevel == SchemeGovLevel.WEST_BENGAL) RoyalGold.copy(alpha = 0.18f) else RoyalGreenSecondaryBg,
                        border = BorderStroke(1.dp, if (govLevel == SchemeGovLevel.WEST_BENGAL) RoyalGold else RoyalBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = govLevel.getLabel(language),
                            color = if (govLevel == SchemeGovLevel.WEST_BENGAL) RoyalSoftGold else RoyalTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (scheme.northBengalPriority >= 94 && !scheme.discontinued) {
                        Surface(
                            color = RoyalWarningBg,
                            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = L10n.t("schemes_north_bengal_priority", language),
                                color = RoyalSoftGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Surface(
                        color = statusColor,
                        border = BorderStroke(1.dp, RoyalBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = status.getLabel(language),
                            color = RoyalTextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("scheme_bookmark_btn_${scheme.id}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = L10n.t(
                            if (isBookmarked) "schemes_bookmark_remove" else "schemes_bookmark_add",
                            language
                        ),
                        tint = if (isBookmarked) RoyalGold else RoyalTextSecondary
                    )
                }
            }

            // Title & Department
            Text(
                text = scheme.getName(language),
                color = RoyalTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = RoyalSoftGold,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = scheme.getDepartment(language),
                    color = RoyalSoftGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            // Category & Short Description
            Text(
                text = scheme.resolveCategory().getLabel(language),
                color = RoyalGreenHighlight,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = scheme.getShortDesc(language),
                color = RoyalTextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Verified Date & Domain Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = RoyalGreenHighlight,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${L10n.t("schemes_verified_label", language)}: ${scheme.lastVerifiedDate}",
                    color = RoyalTextSecondary,
                    fontSize = 11.sp
                )
                if (isAdmin) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onAdminEdit,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("scheme_admin_edit_btn_${scheme.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = RoyalGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = L10n.t("schemes_admin_edit", language),
                            color = RoyalGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Primary Action Row: VISIT OFFICIAL WEBSITE + View Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onVisitOfficialWebsite,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGreenEmerald,
                        contentColor = RoyalTextPrimary
                    ),
                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1.35f)
                        .heightIn(min = 48.dp)
                        .testTag("scheme_visit_btn_${scheme.id}")
                ) {
                    Text(
                        text = L10n.t("schemes_visit_official_btn", language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = RoyalSoftGold,
                        modifier = Modifier.size(16.dp)
                    )
                }

                OutlinedButton(
                    onClick = onViewDetails,
                    border = BorderStroke(1.dp, RoyalBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalSoftGold),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("scheme_detail_btn_${scheme.id}")
                ) {
                    Text(
                        text = L10n.t("schemes_view_details_btn", language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SchemeSelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = RoyalSoftGold,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = RoyalGreenSecondaryBg,
            labelColor = RoyalTextSecondary,
            selectedContainerColor = RoyalGreenEmerald,
            selectedLabelColor = RoyalSoftGold
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = RoyalBorder,
            selectedBorderColor = RoyalGold
        ),
        modifier = Modifier
            .heightIn(min = 48.dp)
            .testTag(tag)
    )
}
