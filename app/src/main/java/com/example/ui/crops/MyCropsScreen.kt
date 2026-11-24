package com.example.ui.crops

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CropMasterDatabase
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.CropCategory
import com.example.data.model.CropMasterGroup
import com.example.data.model.CropRegion
import com.example.data.model.CropSeason
import com.example.data.model.FarmerCrop
import com.example.data.model.MasterCrop
import com.example.data.remote.FirestoreRepository
import com.example.data.repository.CustomCropRepository
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.util.isCompactNarrowScreen
import com.example.util.toLocalizedDoubleOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun MyCropsScreen(
    currentLanguage: AppLanguage,
    userId: String,
    onBack: () -> Unit,
    onAddCrop: () -> Unit,
    onSelectCropDetail: (MasterCrop) -> Unit,
    onOpenAdminAddCrop: () -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        CustomCropRepository.ensureInitialized(context)
    }

    val repository = remember { FirestoreRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val farmerCrops by repository.observeFarmerCrops(userId).collectAsState(initial = emptyList())
    val allMasterCrops by CropMasterDatabase.cropsFlow.collectAsState()
    val isAdmin = remember(context) { UserSessionManager.isAdmin(context) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedGroupFilter by remember { mutableStateOf<CropMasterGroup?>(null) }
    var selectedSubCategoryFilter by remember { mutableStateOf<CropCategory?>(null) }
    var selectedSeasonFilter by remember { mutableStateOf<CropSeason?>(null) }
    var selectedRegionFilter by remember { mutableStateOf<CropRegion?>(null) }
    var deletingCropId by remember { mutableStateOf<String?>(null) }

    val groupCounts = remember(allMasterCrops) {
        allMasterCrops.groupingBy { it.getMasterGroup() }.eachCount()
    }

    val availableSubCategories = remember(allMasterCrops, selectedGroupFilter) {
        if (selectedGroupFilter == null) {
            emptyList()
        } else {
            allMasterCrops
                .filter { it.getMasterGroup() == selectedGroupFilter }
                .map { it.category }
                .distinct()
        }
    }

    val filteredMasterCrops = remember(
        allMasterCrops,
        searchQuery,
        selectedGroupFilter,
        selectedSubCategoryFilter,
        selectedSeasonFilter,
        selectedRegionFilter,
        currentLanguage
    ) {
        val q = searchQuery.trim()
        allMasterCrops.filter { crop ->
            val matchQuery = q.isBlank() ||
                crop.nameBn.contains(q, ignoreCase = true) ||
                crop.nameEn.contains(q, ignoreCase = true) ||
                crop.nameHi.contains(q, ignoreCase = true) ||
                crop.scientificName.contains(q, ignoreCase = true) ||
                crop.subCategory.contains(q, ignoreCase = true) ||
                crop.category.getDisplayName(currentLanguage).contains(q, ignoreCase = true) ||
                crop.getVarieties(currentLanguage).contains(q, ignoreCase = true) ||
                crop.aliases.any { it.contains(q, ignoreCase = true) }

            val matchGroup = selectedGroupFilter == null || crop.getMasterGroup() == selectedGroupFilter
            val matchSubCat = selectedSubCategoryFilter == null || crop.category == selectedSubCategoryFilter
            val matchSeason = selectedSeasonFilter == null || selectedSeasonFilter in crop.getResolvedSeasons()
            val matchRegion = selectedRegionFilter == null || selectedRegionFilter in crop.getResolvedRegions()

            matchQuery && matchGroup && matchSubCat && matchSeason && matchRegion
        }
    }

    val resetAllFilters: () -> Unit = {
        searchQuery = ""
        selectedGroupFilter = null
        selectedSubCategoryFilter = null
        selectedSeasonFilter = null
        selectedRegionFilter = null
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("my_crops_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "🌱 আমার ফসল ও গাইড"
                            AppLanguage.HINDI -> "🌱 मेरी फसलें व गाइड"
                            AppLanguage.ENGLISH -> "My Crops & Guide"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCrop,
                containerColor = AgriGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_crop_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Crop")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // Active Farmer Crops Section
            item {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "আমার চাষ করা ফসল (${farmerCrops.size}টি):"
                        AppLanguage.HINDI -> "मेरी उगाई गई फसलें (${farmerCrops.size}):"
                        AppLanguage.ENGLISH -> "My Cultivated Crops (${farmerCrops.size}):"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
            }

            if (farmerCrops.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "এখনও কোনো ফসল যোগ করা হয়নি।"
                                    AppLanguage.HINDI -> "अभी तक कोई फसल नहीं जोड़ी गई है।"
                                    AppLanguage.ENGLISH -> "No crops added yet."
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = AgriTextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onAddCrop,
                                modifier = Modifier.heightIn(min = 48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AgriGreenPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "+ ফসল যোগ করুন"
                                        AppLanguage.HINDI -> "+ नई फसल जोड़ें"
                                        AppLanguage.ENGLISH -> "+ Add Crop"
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                items(farmerCrops, key = { it.id }) { fc ->
                    val matchedCrop = CropMasterDatabase.getCropOrDefault(fc.cropName, fc.cropCategory)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCropDetail(matchedCrop) }
                            .testTag("farmer_crop_${fc.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = matchedCrop.iconEmoji, fontSize = 32.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (fc.cropName == matchedCrop.nameBn || fc.cropName == matchedCrop.nameEn || fc.cropName == matchedCrop.nameHi) {
                                                matchedCrop.getName(currentLanguage)
                                            } else fc.cropName,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "জমি: ${fc.landArea} ${fc.landUnit} • বোনার তারিখ: ${fc.plantingDate}"
                                                AppLanguage.HINDI -> "भूमि: ${fc.landArea} ${fc.landUnit} • बुवाई तिथि: ${fc.plantingDate}"
                                                AppLanguage.ENGLISH -> "Land: ${fc.landArea} ${fc.landUnit} • Planted: ${fc.plantingDate}"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                        )
                                        if (fc.variety.isNotBlank()) {
                                            Text(
                                                text = when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "জাত: ${fc.variety}"
                                                    AppLanguage.HINDI -> "किस्म: ${fc.variety}"
                                                    AppLanguage.ENGLISH -> "Variety: ${fc.variety}"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    enabled = deletingCropId != fc.id,
                                    modifier = Modifier.size(48.dp),
                                    onClick = {
                                        coroutineScope.launch {
                                            deletingCropId = fc.id
                                            try {
                                                repository.deleteFarmerCrop(fc.id)
                                            } catch (e: CancellationException) {
                                                throw e
                                            } catch (_: Exception) {
                                                val errMsg = when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "মুছতে সমস্যা হয়েছে। অনুগ্রহ করে আবার চেষ্টা করুন।"
                                                    AppLanguage.HINDI -> "हटाने में समस्या हुई। कृपया पुनः प्रयास करें।"
                                                    AppLanguage.ENGLISH -> "Could not delete crop. Please sign in or try again."
                                                }
                                                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                                            } finally {
                                                deletingCropId = null
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "📖 সম্পূর্ণ চাষ নির্দেশিকা, রোগ-পোকা ও ওষুধ দেখতে স্পর্শ করুন"
                                            AppLanguage.HINDI -> "📖 संपूर्ण खेती गाइड, रोग-कीट व दवाएं देखने के लिए टैप करें"
                                            AppLanguage.ENGLISH -> "📖 Tap to view complete cultivation guide, pests & medicines"
                                        },
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "➔", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            // Master Crop Directory Search & Multi-Axis Filters
            item {
                Spacer(modifier = Modifier.height(10.dp))
                MasterLibraryFilterHeader(
                    currentLanguage = currentLanguage,
                    totalCropsCount = allMasterCrops.size,
                    filteredCount = filteredMasterCrops.size,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedGroup = selectedGroupFilter,
                    onSelectGroup = { grp ->
                        selectedGroupFilter = grp
                        selectedSubCategoryFilter = null
                    },
                    groupCounts = groupCounts,
                    availableSubCategories = availableSubCategories,
                    selectedSubCategory = selectedSubCategoryFilter,
                    onSelectSubCategory = { selectedSubCategoryFilter = it },
                    selectedSeason = selectedSeasonFilter,
                    onSelectSeason = { selectedSeasonFilter = it },
                    selectedRegion = selectedRegionFilter,
                    onSelectRegion = { selectedRegionFilter = it },
                    isAdmin = isAdmin,
                    onOpenAdminAddCrop = onOpenAdminAddCrop,
                    onResetAllFilters = resetAllFilters
                )
            }

            if (filteredMasterCrops.isEmpty()) {
                item {
                    MasterLibraryEmptyFilterCard(
                        currentLanguage = currentLanguage,
                        onResetAllFilters = resetAllFilters
                    )
                }
            } else {
                items(filteredMasterCrops, key = { it.cropId }) { crop ->
                    MasterCropLibraryCard(
                        crop = crop,
                        currentLanguage = currentLanguage,
                        onClick = { onSelectCropDetail(crop) }
                    )
                }
            }
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFarmerCropScreen(
    currentLanguage: AppLanguage,
    userId: String,
    onBack: () -> Unit,
    onCropAdded: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { FirestoreRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    val allMasterCrops by CropMasterDatabase.cropsFlow.collectAsState()
    LaunchedEffect(Unit) {
        CustomCropRepository.ensureInitialized(context)
    }

    var selectedCrop by remember {
        mutableStateOf(
            allMasterCrops.find { it.cropId == "tomato" } ?: allMasterCrops.first()
        )
    }
    var plantingDate by remember(currentLanguage) {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "আজকের তারিখ"
                AppLanguage.HINDI -> "आज की तारीख"
                AppLanguage.ENGLISH -> "Today's Date"
            }
        )
    }
    var landArea by remember { mutableStateOf("1.0") }
    var landUnit by remember(currentLanguage) {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "বিঘা"
                AppLanguage.HINDI -> "बीघा"
                AppLanguage.ENGLISH -> "Bigha"
            }
        )
    }
    var variety by remember(currentLanguage) {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "অভিলাষ হাইব্রিড"
                AppLanguage.HINDI -> "अभिलाष हाइब्रिड"
                AppLanguage.ENGLISH -> "Abhilash Hybrid"
            }
        )
    }
    var notes by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val isNarrow = isCompactNarrowScreen()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "নতুন ফসল যুক্ত করুন"
                            AppLanguage.HINDI -> "नई फसल जोड़ें"
                            AppLanguage.ENGLISH -> "Add New Crop"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "১. ফসল নির্বাচন করুন:"
                        AppLanguage.HINDI -> "1. फसल चुनें:"
                        AppLanguage.ENGLISH -> "1. Select Crop:"
                    },
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedCrop.iconEmoji} ${selectedCrop.getName(currentLanguage)}",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        allMasterCrops.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.iconEmoji} ${c.getName(currentLanguage)}") },
                                onClick = {
                                    selectedCrop = c
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "২. জমিতে বোনার তারিখ:"
                        AppLanguage.HINDI -> "2. बुवाई की तारीख:"
                        AppLanguage.ENGLISH -> "2. Planting Date:"
                    },
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                OutlinedTextField(
                    value = plantingDate,
                    onValueChange = { plantingDate = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "৩. জমির পরিমাণ ও একক:"
                        AppLanguage.HINDI -> "3. भूमि का क्षेत्रफल व इकाई:"
                        AppLanguage.ENGLISH -> "3. Land Area & Unit:"
                    },
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                if (isNarrow) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = landArea,
                            onValueChange = { landArea = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "পরিমাণ"
                                        AppLanguage.HINDI -> "क्षेत्रफल"
                                        AppLanguage.ENGLISH -> "Area"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = landUnit,
                            onValueChange = { landUnit = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "একক (বিঘা/কাঠা/একর)"
                                        AppLanguage.HINDI -> "इकाई (बीघा/कट्ठा/एकड़)"
                                        AppLanguage.ENGLISH -> "Unit (Bigha/Katha/Acre)"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = landArea,
                            onValueChange = { landArea = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "পরিমাণ"
                                        AppLanguage.HINDI -> "क्षेत्रफल"
                                        AppLanguage.ENGLISH -> "Area"
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = landUnit,
                            onValueChange = { landUnit = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "একক (বিঘা/কাঠা/একর)"
                                        AppLanguage.HINDI -> "इकाई (बीघा/कट्ठा/एकड़)"
                                        AppLanguage.ENGLISH -> "Unit (Bigha/Katha/Acre)"
                                    }
                                )
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "৪. জাত বা ভ্যারাইটি (ঐচ্ছিক):"
                        AppLanguage.HINDI -> "4. किस्म या वैरायटी (वैकल्पिक):"
                        AppLanguage.ENGLISH -> "4. Variety / Cultivar (Optional):"
                    },
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                OutlinedTextField(
                    value = variety,
                    onValueChange = { variety = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "৫. অতিরিক্ত নোট বা মন্তব্য (ঐচ্ছিক):"
                        AppLanguage.HINDI -> "5. अतिरिक्त नोट्स (वैकल्पिक):"
                        AppLanguage.ENGLISH -> "5. Notes / Remarks (Optional):"
                    },
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    enabled = !isSaving,
                    onClick = {
                        coroutineScope.launch {
                            isSaving = true
                            try {
                                val parsedArea = landArea.toLocalizedDoubleOrNull()?.takeIf { it > 0.0 } ?: 1.0
                                val newCrop = FarmerCrop(
                                    farmerId = userId,
                                    cropName = selectedCrop.getName(currentLanguage),
                                    cropCategory = selectedCrop.category.name,
                                    plantingDate = plantingDate,
                                    landArea = parsedArea,
                                    landUnit = landUnit,
                                    variety = variety,
                                    notes = notes
                                )
                                repository.addFarmerCrop(newCrop)
                                onCropAdded()
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                val errMsg = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ফসল সংরক্ষণ করা যায়নি। অনুগ্রহ করে সাইন-ইন ও ইন্টারনেট যাচাই করুন।"
                                    AppLanguage.HINDI -> "फसल सहेजी नहीं जा सकी। कृपया साइन-इन और इंटरनेट जांचें।"
                                    AppLanguage.ENGLISH -> "Could not save crop. Please ensure you are signed in with Google."
                                }
                                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .testTag("save_farmer_crop_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "ফসল সংরক্ষণ করুন ও সময়সূচী তৈরি করুন"
                            AppLanguage.HINDI -> "फसल सहेजें व कैलेंडर बनाएं"
                            AppLanguage.ENGLISH -> "Save Crop & Create Schedule"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
