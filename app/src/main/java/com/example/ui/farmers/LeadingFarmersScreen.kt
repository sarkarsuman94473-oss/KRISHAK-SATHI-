package com.example.ui.farmers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.datasource.LeadingFarmerTranslations
import com.example.data.local.entity.FarmerCategoryTags
import com.example.data.local.entity.LeadingFarmerEntity
import com.example.data.model.AppLanguage
import com.example.data.repository.LeadingFarmerRepository
import com.example.ui.theme.AgriEarthSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriGreenPrimaryContainer
import com.example.ui.theme.AgriPriceGold
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.util.L10n
import com.example.util.isCompactNarrowScreen

@Composable
fun LeadingFarmersScreen(
    repository: LeadingFarmerRepository,
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(FarmerCategoryTags.ALL) }
    var selectedState by remember { mutableStateOf("All States") }
    var selectedFarmerForDetail by remember { mutableStateOf<LeadingFarmerEntity?>(null) }

    LaunchedEffect(Unit) {
        repository.seedInitialFarmersIfNeeded()
    }

    val farmers by repository.filterFarmers(
        searchQuery = searchQuery,
        categoryTag = selectedCategory,
        selectedState = selectedState
    ).collectAsState(initial = emptyList())

    val stateList = remember {
        listOf(
            "All States",
            "Maharashtra",
            "Haryana",
            "Uttar Pradesh",
            "Odisha",
            "Telangana",
            "Rajasthan",
            "Gujarat",
            "Madhya Pradesh",
            "Uttarakhand",
            "West Bengal"
        )
    }

    @OptIn(ExperimentalLayoutApi::class)
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Surface(
                color = AgriGreenPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack, modifier = Modifier.testTag("leading_farmers_back_btn")) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = L10n.t("leading_farmers_title", currentLanguage),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = L10n.t("leading_farmers_subtitle", currentLanguage),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFFD54F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onOpenAdmin,
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .testTag("leading_farmers_admin_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Management",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("farmers_search_input"),
                        placeholder = {
                            Text(
                                text = L10n.t("leading_farmers_search", currentLanguage),
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.18f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                            focusedBorderColor = AgriPriceGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
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
        ) {
            // Category Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FarmerCategoryTags.FILTERS.forEach { cat ->
                    val isSelected = selectedCategory == cat.key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat.key },
                        label = {
                            Text(
                                text = "${cat.icon} ${cat.getName(currentLanguage)}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgriGreenPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("farmer_filter_${cat.key}")
                    )
                }
            }

            // State Selection Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AgriEarthSecondary,
                    modifier = Modifier.size(16.dp)
                )
                stateList.forEach { stateName ->
                    val isSelected = selectedState == stateName
                    Surface(
                        color = if (isSelected) AgriEarthSecondary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) AgriEarthSecondary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .clickable { selectedState = stateName }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = LeadingFarmerTranslations.getDisplayState(stateName, currentLanguage),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else AgriTextPrimary
                            )
                        }
                    }
                }
            }

            // Result Count Banner (FlowRow wraps cleanly on 320dp screens)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "স্বীকৃত কৃষক: ${farmers.size} জন"
                        AppLanguage.HINDI -> "प्रमाणित किसान: ${farmers.size}"
                        AppLanguage.ENGLISH -> "Verified Pioneers: ${farmers.size}"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextSecondary
                )
                Text(
                    text = L10n.t("leading_farmers_padma_badge", currentLanguage),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Farmers List
            if (farmers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🌾", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = L10n.t("leading_farmers_empty", currentLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AgriTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = L10n.t("leading_farmers_empty_hint", currentLanguage),
                            fontSize = 12.sp,
                            color = AgriTextSecondary
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 760.dp)
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(farmers, key = { it.id }) { farmer ->
                            FarmerCard(
                                farmer = farmer,
                                currentLanguage = currentLanguage,
                                onViewDetail = { selectedFarmerForDetail = farmer },
                                onContact = { selectedFarmerForDetail = farmer }
                            )
                        }

                        // Strict Source Transparency Notice at Bottom
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = L10n.t("leading_farmers_accuracy", currentLanguage),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = L10n.t("leading_farmers_accuracy_desc", currentLanguage),
                                        fontSize = 11.sp,
                                        color = AgriTextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    selectedFarmerForDetail?.let { farmer ->
        FarmerDetailDialog(
            farmer = farmer,
            currentLanguage = currentLanguage,
            repository = repository,
            onDismiss = { selectedFarmerForDetail = null }
        )
    }
}

@Composable
private fun FarmerCard(
    farmer: LeadingFarmerEntity,
    currentLanguage: AppLanguage,
    onViewDetail: () -> Unit,
    onContact: () -> Unit
) {
    val narrow = isCompactNarrowScreen()
    val farmerName = LeadingFarmerTranslations.getDisplayName(farmer, currentLanguage)
    val farmerSpecialization = LeadingFarmerTranslations.getDisplaySpecialization(farmer, currentLanguage)
    val farmerAward = LeadingFarmerTranslations.getDisplayAward(farmer, currentLanguage)
    val farmerLocation = LeadingFarmerTranslations.getDisplayLocation(farmer, currentLanguage)
    val farmerCrops = LeadingFarmerTranslations.getDisplayCrops(farmer, currentLanguage)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetail() }
            .testTag("farmer_card_${farmer.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar / Photo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_krishak_sathi_1793336218748),
                        contentDescription = farmer.fullNameEn,
                        modifier = Modifier.size(52.dp).clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = farmerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AgriTextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AgriEarthSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = farmerLocation,
                            fontSize = 12.sp,
                            color = AgriTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Surface(
                        color = AgriPriceGold,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "🎖 $farmerAward",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.RoyalGreenPrimaryBg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Specialization
            Text(
                text = "🌱 $farmerSpecialization",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Crops
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "প্রধান ফসল: $farmerCrops"
                    AppLanguage.HINDI -> "प्रमुख फसलें: $farmerCrops"
                    AppLanguage.ENGLISH -> "Main Crops: $farmerCrops"
                },
                fontSize = 11.sp,
                color = AgriTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Action Buttons (stacked cleanly on ultra-narrow screens)
            if (narrow) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewDetail,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("view_farmer_story_${farmer.id}")
                    ) {
                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = L10n.t("leading_farmers_story_btn", currentLanguage),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onContact,
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("contact_farmer_btn_${farmer.id}")
                    ) {
                        Icon(
                            imageVector = if (farmer.hasPublicContact) Icons.Default.Call else Icons.Default.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (farmer.hasPublicContact) L10n.t("leading_farmers_contact_btn", currentLanguage) else L10n.t("leading_farmers_details_btn", currentLanguage),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewDetail,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("view_farmer_story_${farmer.id}")
                    ) {
                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = L10n.t("leading_farmers_story_btn", currentLanguage),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onContact,
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("contact_farmer_btn_${farmer.id}")
                    ) {
                        Icon(
                            imageVector = if (farmer.hasPublicContact) Icons.Default.Call else Icons.Default.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (farmer.hasPublicContact) {
                                L10n.t("leading_farmers_contact_btn", currentLanguage)
                            } else {
                                L10n.t("leading_farmers_details_btn", currentLanguage)
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
