package com.example.ui.medicine

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.datasource.CropMasterDatabase
import com.example.data.datasource.ProductVerificationResult
import com.example.data.datasource.VerifiedTreatmentDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.LandUnit
import com.example.data.model.MasterCrop
import com.example.data.model.VerifiedTreatment
import com.example.ui.theme.*
import com.example.util.L10n
import com.example.util.isCompactNarrowScreen
import com.example.util.loadSafeBitmapFromUri
import com.example.util.scaleBitmapBounded
import com.example.util.toLocalizedDoubleOrNull
import com.example.util.toLocalizedIntOrNull

enum class MedicineHubTab(
    val titleBn: String,
    val titleHi: String,
    val titleEn: String,
    val iconEmoji: String
) {
    CROP_PROBLEM_FLOW("ফসল ➔ সমস্যা ➔ ওষুধ", "फसल ➔ समस्या ➔ दवा", "Crop ➔ Problem ➔ Medicine", "🌱"),
    SCAN_VERIFY("ওষুধ স্ক্যান ও যাচাই", "दवा स्कैन व सत्यापन", "Scan & Verify", "🔍"),
    COMPARE_RESISTANCE("তুলনা ও প্রতিরোধ (IPM)", "तुलना व प्रतिरोध (IPM)", "Compare & IPM", "⚖️"),
    ADMIN_MANAGER("অ্যাডমিন ম্যানেজার", "एडमिन मैनेजर", "Admin Manager", "🛠️");

    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> titleBn
        AppLanguage.HINDI -> titleHi
        AppLanguage.ENGLISH -> titleEn
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropMedicineHubScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onOpenCalculator: (String) -> Unit = {},
    onContactExpert: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(MedicineHubTab.CROP_PROBLEM_FLOW) }

    // State for crop-problem flow
    val allCrops = remember { CropMasterDatabase.crops }
    var selectedCrop by remember { mutableStateOf(allCrops.find { it.cropId == "tomato" } ?: allCrops.first()) }

    // Problem category filter
    var selectedProblemCategory by remember { mutableStateOf("ALL") } // ALL, PEST, DISEASE, LEAF, FLOWER_FRUIT

    // Detail dialog state
    var viewingTreatmentDetail by remember { mutableStateOf<VerifiedTreatment?>(null) }
    var viewingSellerDialog by remember { mutableStateOf<VerifiedTreatment?>(null) }
    var viewingCalculatorDialog by remember { mutableStateOf<VerifiedTreatment?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = AgriGreenPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("medicine_hub_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "💊 ${L10n.t("medicine_hub_title", currentLanguage)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 28.sp
                        )
                        Text(
                            text = L10n.t("medicine_hub_subtitle", currentLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = com.example.ui.theme.RoyalTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 12.dp
            ) {
                MedicineHubTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = "${tab.iconEmoji} ${tab.getTitle(currentLanguage)}",
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                MedicineHubTab.CROP_PROBLEM_FLOW -> {
                    CropProblemMedicineFlowTab(
                        currentLanguage = currentLanguage,
                        allCrops = allCrops,
                        selectedCrop = selectedCrop,
                        onSelectCrop = { selectedCrop = it },
                        selectedProblemCategory = selectedProblemCategory,
                        onSelectProblemCategory = { selectedProblemCategory = it },
                        onViewDetail = { viewingTreatmentDetail = it },
                        onViewSellers = { viewingSellerDialog = it },
                        onViewCalculator = { viewingCalculatorDialog = it },
                        onContactExpert = onContactExpert
                    )
                }
                MedicineHubTab.SCAN_VERIFY -> {
                    ScanAndVerifyMedicineTab(
                        currentLanguage = currentLanguage,
                        onViewDetail = { viewingTreatmentDetail = it }
                    )
                }
                MedicineHubTab.COMPARE_RESISTANCE -> {
                    CompareAndResistanceTab(
                        currentLanguage = currentLanguage,
                        selectedCrop = selectedCrop,
                        onViewDetail = { viewingTreatmentDetail = it }
                    )
                }
                MedicineHubTab.ADMIN_MANAGER -> {
                    AdminMedicineManagerTab(
                        currentLanguage = currentLanguage,
                        allCrops = allCrops,
                        onMedicineAdded = {
                            val msg = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নতুন ওষুধ ডাটাবেজে যুক্ত হয়েছে!"
                                AppLanguage.HINDI -> "नई दवा डेटाबेस में जोड़ दी गई!"
                                AppLanguage.ENGLISH -> "New verified medicine added to database!"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Full Details Modal Dialog (Section 480-481)
    viewingTreatmentDetail?.let { tr ->
        MedicineDetailDialog(
            treatment = tr,
            currentLanguage = currentLanguage,
            onDismiss = { viewingTreatmentDetail = null },
            onOpenCalculator = {
                viewingTreatmentDetail = null
                viewingCalculatorDialog = tr
            },
            onViewSellers = {
                viewingTreatmentDetail = null
                viewingSellerDialog = tr
            },
            onContactExpert = onContactExpert
        )
    }

    // Nearby Sellers Modal Dialog (Section 495-496)
    viewingSellerDialog?.let { tr ->
        NearbySellersDialog(
            treatment = tr,
            currentLanguage = currentLanguage,
            onDismiss = { viewingSellerDialog = null }
        )
    }

    // In-Screen Land Calculator Dialog (Section 482)
    viewingCalculatorDialog?.let { tr ->
        MedicineLandCalculatorDialog(
            treatment = tr,
            currentLanguage = currentLanguage,
            onDismiss = { viewingCalculatorDialog = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CropProblemMedicineFlowTab(
    currentLanguage: AppLanguage,
    allCrops: List<MasterCrop>,
    selectedCrop: MasterCrop,
    onSelectCrop: (MasterCrop) -> Unit,
    selectedProblemCategory: String,
    onSelectProblemCategory: (String) -> Unit,
    onViewDetail: (VerifiedTreatment) -> Unit,
    onViewSellers: (VerifiedTreatment) -> Unit,
    onViewCalculator: (VerifiedTreatment) -> Unit,
    onContactExpert: () -> Unit
) {
    var cropDropdownExpanded by remember { mutableStateOf(false) }
    var cropSearchQuery by remember { mutableStateOf("") }

    val filteredCrops = remember(allCrops, cropSearchQuery, currentLanguage) {
        if (cropSearchQuery.isBlank()) allCrops
        else {
            val q = cropSearchQuery.trim().lowercase()
            allCrops.filter {
                it.nameEn.lowercase().contains(q) ||
                    it.nameBn.lowercase().contains(q) ||
                    it.nameHi.lowercase().contains(q) ||
                    it.cropId.lowercase().contains(q)
            }
        }
    }

    val cropTreatments = remember(selectedCrop, selectedProblemCategory) {
        val list = VerifiedTreatmentDatabase.getCropTreatments(selectedCrop.cropId)
            .ifEmpty { VerifiedTreatmentDatabase.getCropTreatments(selectedCrop.nameEn) }

        when (selectedProblemCategory) {
            "PEST" -> list.filter { it.productType.contains("Insecticide", true) || it.productType.contains("Acaricide", true) }
            "DISEASE" -> list.filter { it.productType.contains("Fungicide", true) }
            "LEAF" -> list.filter { it.symptomsBn.contains("পাতা") || it.problemNameBn.contains("পাতা") }
            "FLOWER_FRUIT" -> list.filter { it.symptomsBn.contains("ফুল") || it.symptomsBn.contains("ফল") }
            else -> list
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Step 1: Select Crop (searchable + full crop list, no 40-item cap)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "১. ফসল নির্বাচন করুন:"
                            AppLanguage.HINDI -> "1. फसल चुनें:"
                            AppLanguage.ENGLISH -> "1. Select Crop:"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = cropSearchQuery,
                        onValueChange = { cropSearchQuery = it },
                        placeholder = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ফসল খুঁজুন (যেমন: ধান, আলু, টমেটো)..."
                                    AppLanguage.HINDI -> "फसल खोजें (जैसे: धान, आलू, टमाटर)..."
                                    AppLanguage.ENGLISH -> "Search crop (e.g., Rice, Tomato, Potato)..."
                                },
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (cropSearchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { cropSearchQuery = "" },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("medicine_crop_search")
                    )

                    ExposedDropdownMenuBox(
                        expanded = cropDropdownExpanded,
                        onExpandedChange = { cropDropdownExpanded = !cropDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${selectedCrop.iconEmoji} ${selectedCrop.getName(currentLanguage)}",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_crop_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = cropDropdownExpanded,
                            onDismissRequest = { cropDropdownExpanded = false }
                        ) {
                            filteredCrops.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.iconEmoji} ${c.getName(currentLanguage)}", lineHeight = 20.sp) },
                                    onClick = {
                                        onSelectCrop(c)
                                        cropDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Step 2: Problem Category Filter
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "২. সমস্যার ধরন নির্বাচন করুন:"
                        AppLanguage.HINDI -> "2. समस्या की श्रेणी चुनें:"
                        AppLanguage.ENGLISH -> "2. Select Problem Category:"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AgriTextPrimary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val categories = listOf(
                        "ALL" to when (currentLanguage) {
                            AppLanguage.BENGALI -> "সব সমস্যা"
                            AppLanguage.HINDI -> "सभी समस्याएं"
                            AppLanguage.ENGLISH -> "All Problems"
                        },
                        "PEST" to when (currentLanguage) {
                            AppLanguage.BENGALI -> "🐛 ক্ষতিকর পোকা"
                            AppLanguage.HINDI -> "🐛 हानिकारक कीट"
                            AppLanguage.ENGLISH -> "🐛 Insect Pests"
                        },
                        "DISEASE" to when (currentLanguage) {
                            AppLanguage.BENGALI -> "🦠 ছত্রাক ও রোগ"
                            AppLanguage.HINDI -> "🦠 फफूंद व रोग"
                            AppLanguage.ENGLISH -> "🦠 Diseases"
                        },
                        "LEAF" to when (currentLanguage) {
                            AppLanguage.BENGALI -> "🍃 পাতার সমস্যা"
                            AppLanguage.HINDI -> "🍃 पत्तों की समस्या"
                            AppLanguage.ENGLISH -> "🍃 Leaf Issues"
                        },
                        "FLOWER_FRUIT" to when (currentLanguage) {
                            AppLanguage.BENGALI -> "🌸 ফুল ও ফল ঝরা"
                            AppLanguage.HINDI -> "🌸 फूल व फल झड़ना"
                            AppLanguage.ENGLISH -> "🌸 Flower & Fruit Drop"
                        }
                    )
                    items(categories) { (key, label) ->
                        val isSel = selectedProblemCategory == key
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSel) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onSelectProblemCategory(key) }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else AgriTextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        if (selectedProblemCategory == "LEAF") {
            item { LeafProblemSafetyCard(currentLanguage) }
        } else if (selectedProblemCategory == "FLOWER_FRUIT") {
            item { FlowerFruitProblemSafetyCard(currentLanguage) }
        }

        // Step 3: Verified Medicine Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "৩. সরকারি অনুমোদিত ওষুধ তালিকা (${cropTreatments.size}টি):"
                        AppLanguage.HINDI -> "3. सरकारी अनुमोदित दवा सूची (${cropTreatments.size}):"
                        AppLanguage.ENGLISH -> "3. Govt-Approved Verified Medicines (${cropTreatments.size}):"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AgriTextPrimary
                )
            }
        }

        if (cropTreatments.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("medicine_empty_state")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "${selectedCrop.getName(currentLanguage)}-এর এই ক্যাটাগরিতে এখনো কোনো নির্দিষ্ট রাসায়নিক ওষুধ নিবন্ধিত হয়নি।"
                                AppLanguage.HINDI -> "${selectedCrop.getName(currentLanguage)} की इस श्रेणी में अभी कोई विशिष्ट रासायनिक दवा पंजीकृत नहीं है।"
                                AppLanguage.ENGLISH -> "No specific verified chemical medicine registered for ${selectedCrop.getName(currentLanguage)} in this category yet."
                            },
                            fontWeight = FontWeight.SemiBold,
                            color = AgriTextPrimary,
                            lineHeight = 20.sp
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ভুল ওষুধ ব্যবহার এড়াতে জৈব সমাধান (নিম তেল বা ট্রাইকোডার্মা) অনুসরণ করুন অথবা কৃষি বিশেষজ্ঞের পরামর্শ নিন।"
                                AppLanguage.HINDI -> "गलत दवा से बचने के लिए जैविक उपाय (नीम तेल या ट्राइकोडर्मा) अपनाएं या कृषि विशेषज्ञ से सलाह लें।"
                                AppLanguage.ENGLISH -> "To avoid wrong dosing, follow organic IPM practices (Neem oil or Trichoderma) or consult an agriculture expert."
                            },
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = AgriTextSecondary
                        )
                        Button(
                            onClick = onContactExpert,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AgriGreenPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "বিশেষজ্ঞের পরামর্শ নিন"
                                    AppLanguage.HINDI -> "विशेषज्ञ से सलाह लें"
                                    AppLanguage.ENGLISH -> "Consult Agriculture Expert"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            items(cropTreatments) { tr ->
                FarmerMedicineCard(
                    treatment = tr,
                    currentLanguage = currentLanguage,
                    onViewDetail = { onViewDetail(tr) },
                    onViewSellers = { onViewSellers(tr) },
                    onViewCalculator = { onViewCalculator(tr) },
                    onContactExpert = onContactExpert
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FarmerMedicineCard(
    treatment: VerifiedTreatment,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onViewDetail: () -> Unit,
    onViewSellers: () -> Unit,
    onViewCalculator: () -> Unit,
    onContactExpert: () -> Unit
) {
    val activeIngDisplay = treatment.getDisplayActiveIngredient(currentLanguage)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medicine_card_${treatment.problemId}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (treatment.productType.contains("Bio")) com.example.ui.theme.RoyalSuccessBg else com.example.ui.theme.RoyalInfoBg
                ) {
                    Text(
                        text = treatment.getProductTypeLabel(currentLanguage),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (treatment.productType.contains("Bio")) com.example.ui.theme.RoyalSuccessText else com.example.ui.theme.RoyalInfoText
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (treatment.registrationStatus == "ACTIVE") MaterialTheme.colorScheme.primaryContainer else com.example.ui.theme.RoyalAlertBg
                ) {
                    Text(
                        text = if (treatment.registrationStatus == "ACTIVE") {
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "✓ CIBRC নিবন্ধিত"
                                AppLanguage.HINDI -> "✓ CIBRC पंजीकृत"
                                AppLanguage.ENGLISH -> "✓ CIBRC Registered"
                            }
                        } else {
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "⛔ নিষিদ্ধ/নিয়ন্ত্রিত"
                                AppLanguage.HINDI -> "⛔ प्रतिबंधित / नियंत्रित"
                                AppLanguage.ENGLISH -> "⛔ Banned / Restricted"
                            }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (treatment.registrationStatus == "ACTIVE") MaterialTheme.colorScheme.onPrimaryContainer else com.example.ui.theme.RoyalAlertText
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (treatment.frontImageResId != null) {
                        Image(
                            painter = painterResource(id = treatment.frontImageResId),
                            contentDescription = "Product Photo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = treatment.getDisplayProductName(currentLanguage).ifBlank {
                            activeIngDisplay ?: when (currentLanguage) {
                                AppLanguage.BENGALI -> "পরীক্ষিত ওষুধ"
                                AppLanguage.HINDI -> "सत्यापित दवा"
                                AppLanguage.ENGLISH -> "Verified Medicine"
                            }
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "সক্রিয় উপাদান: ${activeIngDisplay ?: "অনুমোদিত মিশ্রণ"}"
                            AppLanguage.HINDI -> "सक्रिय घटक: ${activeIngDisplay ?: "अनुमोदित मिश्रण"}"
                            AppLanguage.ENGLISH -> "Active Ingredient: ${activeIngDisplay ?: "Approved Blend"}"
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "ফর্মুলেশন: ${treatment.formulation} • প্রস্তুতকারক: ${treatment.manufacturer}"
                            AppLanguage.HINDI -> "फॉर्मूलेशन: ${treatment.formulation} • निर्माता: ${treatment.manufacturer}"
                            AppLanguage.ENGLISH -> "Formulation: ${treatment.formulation} • Maker: ${treatment.manufacturer}"
                        },
                        fontSize = 12.sp,
                        color = AgriTextSecondary
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "অনুমোদিত ফসল: ${treatment.getCropDisplayName(currentLanguage)}"
                            AppLanguage.HINDI -> "अनुमोदित फसल: ${treatment.getCropDisplayName(currentLanguage)}"
                            AppLanguage.ENGLISH -> "Approved Crop: ${treatment.getCropDisplayName(currentLanguage)}"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AgriEarthSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "🎯 কোন সমস্যার জন্য: ${treatment.getProblemName(currentLanguage)}"
                            AppLanguage.HINDI -> "🎯 समस्या / रोग: ${treatment.getProblemName(currentLanguage)}"
                            AppLanguage.ENGLISH -> "🎯 Target Problem: ${treatment.getProblemName(currentLanguage)}"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = com.example.ui.theme.RoyalSoftGold
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "📏 অফিশিয়াল স্প্রে ডোজ: ${treatment.getDosePerLitre(currentLanguage)}"
                            AppLanguage.HINDI -> "📏 आधिकारिक छिड़काव मात्रा: ${treatment.getDosePerLitre(currentLanguage)}"
                            AppLanguage.ENGLISH -> "📏 Label Spray Dose: ${treatment.getDosePerLitre(currentLanguage)}"
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = AgriTextPrimary
                    )
                    if (treatment.dosePerHectareMlOrGm != null) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "🌾 হেক্টর প্রতি অনুমোদিত মাত্রা: ${treatment.dosePerHectareMlOrGm} ${treatment.getDisplayDoseUnit(currentLanguage)} (${treatment.waterPerHectareLiters.toInt()}L জল)"
                                AppLanguage.HINDI -> "🌾 प्रति हेक्टेयर अनुमोदित मात्रा: ${treatment.dosePerHectareMlOrGm} ${treatment.getDisplayDoseUnit(currentLanguage)} (${treatment.waterPerHectareLiters.toInt()}L पानी)"
                                AppLanguage.ENGLISH -> "🌾 Approved Rate per Hectare: ${treatment.dosePerHectareMlOrGm} ${treatment.getDisplayDoseUnit(currentLanguage)} (${treatment.waterPerHectareLiters.toInt()}L water)"
                            },
                            fontSize = 12.sp,
                            color = AgriTextSecondary
                        )
                    }
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "⏳ ফসল তোলার অপেক্ষাকাল (PHI): ${treatment.preHarvestIntervalDays} দিন • রি-এন্ট্রি: ${treatment.reEntryIntervalHours} ঘণ্টা"
                            AppLanguage.HINDI -> "⏳ तुड़ाई प्रतीक्षा (PHI): ${treatment.preHarvestIntervalDays} दिन • पुनः प्रवेश: ${treatment.reEntryIntervalHours} घंटे"
                            AppLanguage.ENGLISH -> "⏳ Pre-Harvest Interval (PHI): ${treatment.preHarvestIntervalDays} days • Re-entry: ${treatment.reEntryIntervalHours} hrs"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgriPriceGold
                    )
                }
            }

            val isNarrow = isCompactNarrowScreen()
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = if (isNarrow) 1 else 2
            ) {
                Button(
                    onClick = onViewDetail,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreenPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "বিস্তারিত দেখুন"
                            AppLanguage.HINDI -> "विवरण देखें"
                            AppLanguage.ENGLISH -> "View Details"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onViewSellers,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "কোথায় পাব?"
                            AppLanguage.HINDI -> "कहां खरीदें?"
                            AppLanguage.ENGLISH -> "Where to Buy"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onViewCalculator,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "জমি হিসাব"
                            AppLanguage.HINDI -> "भूमि गणना"
                            AppLanguage.ENGLISH -> "Land Calc"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onContactExpert,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "বিশেষজ্ঞ মতামত"
                            AppLanguage.HINDI -> "विशेषज्ञ सलाह"
                            AppLanguage.ENGLISH -> "Ask Expert"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LeafProblemSafetyCard(currentLanguage: AppLanguage) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.RoyalWarningBg),
        border = BorderStroke(1.dp, com.example.ui.theme.RoyalGold.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "⚠️ পাতার সমস্যা সুরক্ষা সতর্কতা:"
                    AppLanguage.HINDI -> "⚠️ पत्ते की समस्या सुरक्षा चेतावनी:"
                    AppLanguage.ENGLISH -> "⚠️ Leaf Problem Safety Advisory:"
                },
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.RoyalGold,
                fontSize = 14.sp
            )
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "পাতা হলুদ বা কুঁকড়ে গেলেই সঙ্গে সঙ্গে ওষুধ স্প্রে করবেন না! আগে নিশ্চিত হোন:\n• নাইট্রোজেন ঘাটতি (পুরনো পাতা সমভাবে হলুদ)\n• জমিতে অতিরিক্ত জল জমে শিকড় শ্বাসরুদ্ধ হওয়া\n• থ্রিপস বা সাদা মাছি পোকা (পাতার উল্টো পিঠে দেখুন)\n• আবহাওয়ার চরম ঠান্ডা বা গরমের ধকল"
                    AppLanguage.HINDI -> "पत्ते पीले या मुड़ने पर तुरंत रसायन न छिड़कें! पहले जांचें:\n• नाइट्रोजन की कमी (नीचे के पुराने पत्ते पीले होना)\n• खेत में अधिक जलभराव से जड़ों का दम घुटना\n• थ्रिप्स या सफेद मक्खी (पत्ते के नीचे जांचें)\n• अत्यधिक ठंड या गर्मी का तनाव"
                    AppLanguage.ENGLISH -> "Do not spray chemicals immediately when leaves turn yellow or curl! First check:\n• Nitrogen deficiency (older lower leaves turning uniformly yellow)\n• Waterlogging causing root suffocation\n• Thrips or whiteflies (inspect undersides of leaves)\n• Extreme heat or cold weather stress"
                },
                fontSize = 12.sp,
                color = com.example.ui.theme.RoyalTextPrimary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun FlowerFruitProblemSafetyCard(currentLanguage: AppLanguage) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.RoyalSuccessBg),
        border = BorderStroke(1.dp, com.example.ui.theme.RoyalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "🌸 ফুল ও ফল ঝরা সুরক্ষা নিয়ম:"
                    AppLanguage.HINDI -> "🌸 फूल व फल झड़ना रोकथाम नियम:"
                    AppLanguage.ENGLISH -> "🌸 Flower & Fruit Drop Safety Guidelines:"
                },
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.RoyalSuccessText,
                fontSize = 14.sp
            )
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "ফুল বা ফল ঝরে পড়লে অবিলম্বে অযথা হরমোন স্প্রে করবেন না। আগে যাচাই করুন:\n১. অতিরিক্ত নাইট্রোজেন সার দেওয়া হয়েছে কি না?\n২. মাটিতে আর্দ্রতার কম-বেশি (অনিয়মিত সেচ)\n৩. বোরনের ঘাটতি (১.৫ গ্রাম বোরন ২০% প্রতি লিটার স্প্রে কার্যকর)\n৪. মৌমাছি ও পরাগায়নকারী কীট পর্যাপ্ত রয়েছে কি না?"
                    AppLanguage.HINDI -> "फूल या फल झड़ने पर अनावश्यक हार्मोन न छिड़कें। पहले जांचें:\n1. क्या अधिक नाइट्रोजन खाद दी गई है?\n2. मिट्टी की नमी में उतार-चढ़ाव (अनियमित सिंचाई)\n3. बोरॉन की कमी (बोरॉन 20% @ 1.5 ग्राम/लीटर छिड़काव लाभकारी)\n4. खेत में मधुमक्खियां व परागण करने वाले कीट पर्याप्त हैं या नहीं?"
                    AppLanguage.ENGLISH -> "Do not spray hormones hastily upon flower/fruit drop. Verify first:\n1. Has excess nitrogen been applied?\n2. Irregular irrigation / fluctuating soil moisture\n3. Boron deficiency (spray Boron 20% @ 1.5 g/litre water)\n4. Presence of active bees and pollinators in the field"
                },
                fontSize = 12.sp,
                color = com.example.ui.theme.RoyalTextPrimary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ScanAndVerifyMedicineTab(
    currentLanguage: AppLanguage,
    onViewDetail: (VerifiedTreatment) -> Unit
) {
    val context = LocalContext.current
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scannedInputText by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationResult by remember(currentLanguage) { mutableStateOf<ProductVerificationResult?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = loadSafeBitmapFromUri(context, uri, maxDim = 1280)
            if (bitmap != null) {
                selectedBitmap = bitmap
                if (scannedInputText.isBlank()) {
                    scannedInputText = "Coragen Chlorantraniliprole 18.5% SC FMC"
                }
            } else {
                val msg = when (currentLanguage) {
                    AppLanguage.BENGALI -> "ছবি লোড করা যায়নি"
                    AppLanguage.HINDI -> "फ़ोटो लोड नहीं हो सकी"
                    AppLanguage.ENGLISH -> "Unable to load image"
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            selectedBitmap = scaleBitmapBounded(bitmap, maxDim = 1280)
            if (scannedInputText.isBlank()) {
                scannedInputText = "Coragen Chlorantraniliprole 18.5% SC FMC"
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                cameraLauncher.launch(null)
            } catch (_: Exception) {
                val msg = when (currentLanguage) {
                    AppLanguage.BENGALI -> "ক্যামেরা চালু করা যায়নি।"
                    AppLanguage.HINDI -> "कैमरा चालू नहीं हो सका।"
                    AppLanguage.ENGLISH -> "Could not launch camera."
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        } else {
            val msg = when (currentLanguage) {
                AppLanguage.BENGALI -> "ক্যামেরার অনুমতি প্রয়োজন। গ্যালারি থেকেও ছবি দিতে পারেন।"
                AppLanguage.HINDI -> "कैमरा अनुमति आवश्यक है। आप गैलरी से भी फोटो चुन सकते हैं।"
                AppLanguage.ENGLISH -> "Camera permission required. You can pick from Gallery."
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    fun launchCameraSafely() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            try {
                cameraLauncher.launch(null)
            } catch (_: Exception) {
                Toast.makeText(context, "Camera unavailable", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "📷 ওষুধের বোতল বা প্যাকেট স্ক্যান করুন"
                            AppLanguage.HINDI -> "📷 दवा की बोतल या पैकेट स्कैन करें"
                            AppLanguage.ENGLISH -> "📷 Scan Medicine Bottle or Packet"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "ওষুধের সামনের লেবেল, সক্রিয় উপাদান (Active Ingredient), ব্যাচ ও মেয়াদ উত্তীর্ণের ছবি তুলুন:"
                            AppLanguage.HINDI -> "दवा का लेबल, सक्रिय घटक (Active Ingredient), बैच व एक्सपायरी की फोटो लें:"
                            AppLanguage.ENGLISH -> "Capture front label showing active ingredient, batch number, and expiry details:"
                        },
                        fontSize = 13.sp,
                        color = AgriTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { launchCameraSafely() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AgriGreenPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ক্যামেরা"
                                    AppLanguage.HINDI -> "कैमरा"
                                    AppLanguage.ENGLISH -> "Camera"
                                }
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "গ্যালারি"
                                    AppLanguage.HINDI -> "गैलरी"
                                    AppLanguage.ENGLISH -> "Gallery"
                                }
                            )
                        }
                    }

                    selectedBitmap?.let { bmp ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Scanned label",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    OutlinedTextField(
                        value = scannedInputText,
                        onValueChange = { scannedInputText = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "লেবেল বা পণ্যের নাম / ব্যাচ নম্বর লিখুন"
                                AppLanguage.HINDI -> "दवा का नाम, घटक या बैच नंबर लिखें"
                                AppLanguage.ENGLISH -> "Enter product name, ingredient, or batch code"
                            })
                        },
                        placeholder = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "যেমন: Coragen, Beam, একতারা..."
                                AppLanguage.HINDI -> "उदा: Coragen, Beam, Actara..."
                                AppLanguage.ENGLISH -> "e.g., Coragen, Beam, Actara..."
                            })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            isVerifying = true
                            verificationResult = VerifiedTreatmentDatabase.verifyProductAuthenticity(scannedInputText, currentLanguage)
                            isVerifying = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgriGreenPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "✅ পণ্য ও উপাদান যাচাই করুন"
                                AppLanguage.HINDI -> "✅ उत्पाद व घटक सत्यापित करें"
                                AppLanguage.ENGLISH -> "✅ Verify Product & Ingredient"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        verificationResult?.let { res ->
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (res.isBanned) com.example.ui.theme.RoyalAlertBg
                        else if (res.isAuthenticVerified) com.example.ui.theme.RoyalSuccessBg
                        else com.example.ui.theme.RoyalWarningBg
                    ),
                    border = BorderStroke(1.dp, com.example.ui.theme.RoyalBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = res.statusTitleBn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (res.isBanned) com.example.ui.theme.RoyalAlertText
                            else if (res.isAuthenticVerified) com.example.ui.theme.RoyalSuccessText
                            else com.example.ui.theme.RoyalGold
                        )
                        Text(
                            text = res.messageBn,
                            fontSize = 13.sp,
                            color = com.example.ui.theme.RoyalTextPrimary,
                            lineHeight = 20.sp
                        )

                        res.matchedTreatment?.let { tr ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { onViewDetail(tr) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AgriGreenPrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            ) {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "এই পণ্যের অনুমোদিত ব্যবহার বিধি দেখুন"
                                        AppLanguage.HINDI -> "इस उत्पाद के अनुमोदित उपयोग नियम देखें"
                                        AppLanguage.ENGLISH -> "View Approved Usage Guidelines"
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

@Composable
private fun CompareAndResistanceTab(
    currentLanguage: AppLanguage,
    selectedCrop: MasterCrop,
    onViewDetail: (VerifiedTreatment) -> Unit
) {
    val options = remember(selectedCrop) {
        VerifiedTreatmentDatabase.getCropTreatments(selectedCrop.cropId)
            .ifEmpty { VerifiedTreatmentDatabase.getCropTreatments(selectedCrop.nameEn) }
            .take(3)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "⚖️ একাধিক অনুমোদিত বিকল্পের তুলনা:"
                            AppLanguage.HINDI -> "⚖️ एकाधिक अनुमोदित विकल्पों की तुलना:"
                            AppLanguage.ENGLISH -> "⚖️ Compare Approved Options & Resistance Groups:"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "অ্যাপ কোনো একটি ওষুধকে অন্ধভাবে 'সেরা' বলে দাবি করে না। বিভিন্ন গ্রুপ ও উপাদানের বিকল্প দেখে সিদ্ধান্ত নিন:"
                            AppLanguage.HINDI -> "ऐप किसी एक दवा को बिना आधार 'सर्वश्रेष्ठ' नहीं मानता। विभिन्न रासायनिक समूहों और जैविक विकल्पों की तुलना कर निर्णय लें:"
                            AppLanguage.ENGLISH -> "Compare active ingredients, IRAC/FRAC resistance groups, PHI days, and biological options before selecting:"
                        },
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = AgriTextSecondary
                    )
                }
            }
        }

        if (options.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "${selectedCrop.getName(currentLanguage)}-এর জন্য এখনো তুলনামূলক ওষুধ তালিকা যুক্ত হয়নি।"
                            AppLanguage.HINDI -> "${selectedCrop.getName(currentLanguage)} के लिए तुलना सूची अभी उपलब्ध नहीं है।"
                            AppLanguage.ENGLISH -> "No comparison entries registered for ${selectedCrop.getName(currentLanguage)} yet."
                        },
                        modifier = Modifier.padding(16.dp),
                        fontSize = 13.sp,
                        color = AgriTextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        items(options.indices.toList()) { index ->
            val tr = options[index]
            val optionLabel = when (currentLanguage) {
                AppLanguage.BENGALI -> "বিকল্প ${index + 1}"
                AppLanguage.HINDI -> "विकल्प ${index + 1}"
                AppLanguage.ENGLISH -> "Option ${index + 1}"
            }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = optionLabel, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "উপাদান: ${tr.getDisplayActiveIngredient(currentLanguage)} (${tr.formulation})"
                            AppLanguage.HINDI -> "सक्रिय घटक: ${tr.getDisplayActiveIngredient(currentLanguage)} (${tr.formulation})"
                            AppLanguage.ENGLISH -> "Active Ingredient: ${tr.getDisplayActiveIngredient(currentLanguage)} (${tr.formulation})"
                        },
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "ব্র্যান্ড: ${tr.getDisplayBrandExamples(currentLanguage)} • প্রস্তুতকারক: ${tr.manufacturer}"
                            AppLanguage.HINDI -> "ब्रांड: ${tr.getDisplayBrandExamples(currentLanguage)} • निर्माता: ${tr.manufacturer}"
                            AppLanguage.ENGLISH -> "Brands: ${tr.getDisplayBrandExamples(currentLanguage)} • Maker: ${tr.manufacturer}"
                        },
                        fontSize = 12.sp,
                        color = AgriTextSecondary
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "প্রতিরোধ গ্রুপ: ${tr.resistanceGroup}"
                            AppLanguage.HINDI -> "प्रतिरोध समूह: ${tr.resistanceGroup}"
                            AppLanguage.ENGLISH -> "Resistance Group: ${tr.resistanceGroup}"
                        },
                        fontWeight = FontWeight.SemiBold,
                        color = AgriPriceGold
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "অপেক্ষাকাল (PHI): ${tr.preHarvestIntervalDays} দিন"
                            AppLanguage.HINDI -> "तुड़ाई प्रतीक्षा (PHI): ${tr.preHarvestIntervalDays} दिन"
                            AppLanguage.ENGLISH -> "Pre-Harvest Interval (PHI): ${tr.preHarvestIntervalDays} days"
                        },
                        fontSize = 12.sp,
                        color = AgriTextPrimary
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "জৈব বিকল্প: ${tr.getBiologicalOption(currentLanguage)}"
                            AppLanguage.HINDI -> "जैविक विकल्प: ${tr.getBiologicalOption(currentLanguage)}"
                            AppLanguage.ENGLISH -> "Bio Alternative: ${tr.getBiologicalOption(currentLanguage)}"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onViewDetail(tr) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgriGreenPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "বিস্তারিত ও হিসেব দেখুন"
                                AppLanguage.HINDI -> "विवरण व मात्रा देखें"
                                AppLanguage.ENGLISH -> "View Details & Dose"
                            },
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMedicineManagerTab(
    currentLanguage: AppLanguage,
    allCrops: List<MasterCrop>,
    onMedicineAdded: () -> Unit
) {
    var activeIngredient by remember { mutableStateOf("") }
    var formulation by remember { mutableStateOf("18.5% SC") }
    var brandName by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var cropName by remember { mutableStateOf(allCrops.first().nameEn) }
    var targetProblem by remember { mutableStateOf("") }
    var officialDose by remember { mutableStateOf("150.0") }
    var doseUnit by remember { mutableStateOf("ml") }
    var phiDays by remember { mutableStateOf("7") }
    var officialSource by remember { mutableStateOf("CIBRC Verified") }
    var isBanned by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "🛠️ ওষুধ ও চিকিৎসা ডাটাবেজ পরিচালক (Admin Manager)"
                            AppLanguage.HINDI -> "🛠️ दवा एवं उपचार डेटाबेस प्रबंधक (Admin Manager)"
                            AppLanguage.ENGLISH -> "🛠️ Medicine & Treatment Admin Manager"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "নতুন নিবন্ধিত রাসায়নিক উপাদান, ফর্মুলেশন ও ডোজ যাচাই করে যুক্ত করুন:"
                            AppLanguage.HINDI -> "नया पंजीकृत सक्रिय घटक, फॉर्मूलेशन व अनुमोदित खुराक सत्यापित कर जोड़ें:"
                            AppLanguage.ENGLISH -> "Add verified registered active ingredients, formulations, and CIBRC label dosages:"
                        },
                        fontSize = 12.sp,
                        color = AgriTextSecondary
                    )

                    OutlinedTextField(
                        value = activeIngredient,
                        onValueChange = { activeIngredient = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "সক্রিয় উপাদান (Active Ingredient)*"
                                AppLanguage.HINDI -> "सक्रिय घटक (Active Ingredient)*"
                                AppLanguage.ENGLISH -> "Active Ingredient*"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formulation,
                        onValueChange = { formulation = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "ফর্মুলেশন (যেমন 20% SP, 50% WP)*"
                                AppLanguage.HINDI -> "फॉर्मूलेशन (जैसे 20% SP, 50% WP)*"
                                AppLanguage.ENGLISH -> "Formulation (e.g. 20% SP, 50% WP)*"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = brandName,
                        onValueChange = { brandName = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "অনুমোদিত ব্র্যান্ড (Brand Name)"
                                AppLanguage.HINDI -> "अनुमोदित ब्रांड नाम (Brand Name)"
                                AppLanguage.ENGLISH -> "Approved Brand Name"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = manufacturer,
                        onValueChange = { manufacturer = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "প্রস্তুতকারক কোম্পানি (Manufacturer)"
                                AppLanguage.HINDI -> "निर्माता कंपनी (Manufacturer)"
                                AppLanguage.ENGLISH -> "Manufacturer"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetProblem,
                        onValueChange = { targetProblem = it },
                        label = {
                            Text(when (currentLanguage) {
                                AppLanguage.BENGALI -> "লক্ষ্য রোগ/পোকা (Target Pest/Disease)*"
                                AppLanguage.HINDI -> "लक्षित कीट या रोग (Target Problem)*"
                                AppLanguage.ENGLISH -> "Target Pest / Disease*"
                            })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = officialDose,
                            onValueChange = { officialDose = it },
                            label = {
                                Text(when (currentLanguage) {
                                    AppLanguage.BENGALI -> "হেক্টর প্রতি ডোজ"
                                    AppLanguage.HINDI -> "प्रति हेक्टेयर मात्रा"
                                    AppLanguage.ENGLISH -> "Dose / Hectare"
                                })
                            },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = phiDays,
                            onValueChange = { phiDays = it },
                            label = {
                                Text(when (currentLanguage) {
                                    AppLanguage.BENGALI -> "PHI দিন"
                                    AppLanguage.HINDI -> "PHI दिन"
                                    AppLanguage.ENGLISH -> "PHI Days"
                                })
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isBanned,
                            onCheckedChange = { isBanned = it }
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "পণ্যটিকে নিষিদ্ধ (BANNED / RESTRICTED) হিসেবে চিহ্নিত করুন"
                                AppLanguage.HINDI -> "इस उत्पाद को प्रतिबंधित (BANNED / RESTRICTED) चिह्नित करें"
                                AppLanguage.ENGLISH -> "Mark product as BANNED / RESTRICTED"
                            },
                            color = if (isBanned) AgriAlertRed else AgriTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (activeIngredient.isNotBlank() && targetProblem.isNotBlank()) {
                                val newEntry = VerifiedTreatment(
                                    problemId = "med_${System.currentTimeMillis()}",
                                    cropName = cropName,
                                    problemNameBn = targetProblem,
                                    problemNameHi = targetProblem,
                                    problemNameEn = targetProblem,
                                    symptomsBn = "অনুমোদিত লেবেল নির্দেশিকা অনুসারে প্রয়োগ করুন",
                                    symptomsHi = "आधिकारिक लेबल निर्देश अनुसार उपयोग करें",
                                    symptomsEn = "Apply as per official CIBRC label instructions",
                                    possibleCauseBn = "রোগ/পোকার সংক্রমণ",
                                    firstActionBn = "আক্রান্ত অংশ অপসারণ ও জমি পরিচ্ছন্ন রাখা",
                                    biologicalOptionBn = "নিম তেল বা ট্রাইকোডার্মা",
                                    activeIngredient = activeIngredient,
                                    brandExamples = brandName,
                                    productName = brandName.ifBlank { activeIngredient },
                                    formulation = formulation,
                                    manufacturer = manufacturer.ifBlank { "Registered Manufacturer" },
                                    dosePerHectareMlOrGm = officialDose.toLocalizedDoubleOrNull() ?: 500.0,
                                    doseUnit = doseUnit,
                                    dosePerLitreBn = "১ মিলি/গ্রাম প্রতি লিটার জলে",
                                    waterPerHectareLiters = 500.0,
                                    preHarvestIntervalDays = phiDays.toLocalizedIntOrNull() ?: 7,
                                    reEntryIntervalHours = 24,
                                    registrationStatus = if (isBanned) "BANNED" else "ACTIVE",
                                    safetyInstructionsBn = "স্প্রে করার সময় ব্যক্তিগত সুরক্ষা সরঞ্জাম (PPE) পরিধান করুন।",
                                    officialSource = officialSource
                                )
                                VerifiedTreatmentDatabase.addAdminVerifiedTreatment(newEntry)
                                activeIngredient = ""
                                brandName = ""
                                targetProblem = ""
                                onMedicineAdded()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgriGreenPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "ডাটাবেজে যুক্ত করুন"
                                AppLanguage.HINDI -> "डेटाबेस में जोड़ें"
                                AppLanguage.ENGLISH -> "Add to Database"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineDetailDialog(
    treatment: VerifiedTreatment,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onDismiss: () -> Unit,
    onOpenCalculator: () -> Unit,
    onViewSellers: () -> Unit,
    onContactExpert: () -> Unit
) {
    val activeIngDisplay = treatment.getDisplayActiveIngredient(currentLanguage)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "💊 ওষুধের সম্পূর্ণ বিবরণ"
                    AppLanguage.HINDI -> "💊 दवा का पूरा विवरण"
                    AppLanguage.ENGLISH -> "💊 Complete Medicine Details"
                },
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = treatment.getDisplayProductName(currentLanguage).ifBlank {
                        activeIngDisplay ?: when (currentLanguage) {
                            AppLanguage.BENGALI -> "পরীক্ষিত ওষুধ"
                            AppLanguage.HINDI -> "सत्यापित दवा"
                            AppLanguage.ENGLISH -> "Verified Medicine"
                        }
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = AgriTextPrimary
                )
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "সক্রিয় উপাদান: $activeIngDisplay"
                        AppLanguage.HINDI -> "सक्रिय घटक: $activeIngDisplay"
                        AppLanguage.ENGLISH -> "Active Ingredient: $activeIngDisplay"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "ফর্মুলেশন: ${treatment.formulation}"
                        AppLanguage.HINDI -> "फॉर्मूलेशन: ${treatment.formulation}"
                        AppLanguage.ENGLISH -> "Formulation: ${treatment.formulation}"
                    }
                )
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "প্রস্তুতকারক: ${treatment.manufacturer}"
                        AppLanguage.HINDI -> "निर्माता: ${treatment.manufacturer}"
                        AppLanguage.ENGLISH -> "Manufacturer: ${treatment.manufacturer}"
                    }
                )
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "কোন ফসলে: ${treatment.getCropDisplayName(currentLanguage)}"
                        AppLanguage.HINDI -> "अनुमोदित फसल: ${treatment.getCropDisplayName(currentLanguage)}"
                        AppLanguage.ENGLISH -> "Approved Crop: ${treatment.getCropDisplayName(currentLanguage)}"
                    },
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "কোন সমস্যার জন্য: ${treatment.getProblemName(currentLanguage)}"
                        AppLanguage.HINDI -> "लक्षित समस्या: ${treatment.getProblemName(currentLanguage)}"
                        AppLanguage.ENGLISH -> "Target Problem: ${treatment.getProblemName(currentLanguage)}"
                    },
                    color = AgriAlertRed,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "💡 এই ওষুধটি কী জন্য?"
                        AppLanguage.HINDI -> "💡 यह दवा किस लिए है?"
                        AppLanguage.ENGLISH -> "💡 Symptoms & Indication:"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = treatment.getSymptoms(currentLanguage), fontSize = 13.sp)

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "🌿 জৈব ও বিকল্প সমাধান:"
                        AppLanguage.HINDI -> "🌿 जैविक एवं वैकल्पिक उपाय:"
                        AppLanguage.ENGLISH -> "🌿 Organic & Biological Alternative:"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = treatment.getBiologicalOption(currentLanguage), fontSize = 13.sp)

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "📏 অফিশিয়াল স্প্রে মাত্রা:"
                        AppLanguage.HINDI -> "📏 आधिकारिक छिड़काव मात्रा:"
                        AppLanguage.ENGLISH -> "📏 Official Label Spray Dose:"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = treatment.getDosePerLitre(currentLanguage), fontSize = 13.sp)
                if (treatment.dosePerHectareMlOrGm != null) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "হেক্টর প্রতি: ${treatment.dosePerHectareMlOrGm} ${treatment.getDisplayDoseUnit(currentLanguage)} (${treatment.waterPerHectareLiters.toInt()} লিটার জল)"
                            AppLanguage.HINDI -> "प्रति हेक्टेयर: ${treatment.dosePerHectareMlOrGm} ${treatment.getDisplayDoseUnit(currentLanguage)} (${treatment.waterPerHectareLiters.toInt()} लीटर पानी)"
                            AppLanguage.ENGLISH -> "Per Hectare: ${treatment.dosePerHectareMlOrGm} ${treatment.getDisplayDoseUnit(currentLanguage)} (${treatment.waterPerHectareLiters.toInt()} L water)"
                        },
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "⏳ ফসল তোলার আগে কতদিন অপেক্ষা করবেন (PHI)?"
                        AppLanguage.HINDI -> "⏳ तुड़ाई से पहले प्रतीक्षा अवधि (PHI):"
                        AppLanguage.ENGLISH -> "⏳ Pre-Harvest Waiting Period (PHI):"
                    },
                    fontWeight = FontWeight.Bold,
                    color = AgriPriceGold
                )
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "${treatment.preHarvestIntervalDays} দিন"
                        AppLanguage.HINDI -> "${treatment.preHarvestIntervalDays} दिन"
                        AppLanguage.ENGLISH -> "${treatment.preHarvestIntervalDays} days"
                    },
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "🛡️ সুরক্ষা ও সতর্কতা:"
                        AppLanguage.HINDI -> "🛡️ सुरक्षा एवं सावधानियां:"
                        AppLanguage.ENGLISH -> "🛡️ Safety & Precautions:"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = treatment.getSafetyInstructions(currentLanguage), fontSize = 12.sp)

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "উৎস: ${treatment.officialSource} • পর্যালোচনা: ${treatment.getLastReviewedLabel(currentLanguage)}"
                        AppLanguage.HINDI -> "स्रोत: ${treatment.officialSource} • समीक्षा: ${treatment.getLastReviewedLabel(currentLanguage)}"
                        AppLanguage.ENGLISH -> "Source: ${treatment.officialSource} • Reviewed: ${treatment.getLastReviewedLabel(currentLanguage)}"
                    },
                    fontSize = 11.sp,
                    color = AgriTextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenCalculator,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreenPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "জমি হিসাব করুন"
                        AppLanguage.HINDI -> "भूमि गणना करें"
                        AppLanguage.ENGLISH -> "Calculate Dose"
                    }
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onViewSellers) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "কোথায় পাব?"
                        AppLanguage.HINDI -> "कहां खरीदें?"
                        AppLanguage.ENGLISH -> "Where to Buy"
                    }
                )
            }
        }
    )
}

@Composable
fun NearbySellersDialog(
    treatment: VerifiedTreatment,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "📍 কোথায় পাবেন (নিকটস্থ অনুমোদিত দোকান)"
                    AppLanguage.HINDI -> "📍 कहां मिलेगा (नजदीकी अधिकृत केंद्र)"
                    AppLanguage.ENGLISH -> "📍 Where to Buy (Authorized Local Dealers)"
                },
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "পণ্য: ${treatment.getDisplayProductName(currentLanguage)} (${treatment.getDisplayActiveIngredient(currentLanguage)})"
                        AppLanguage.HINDI -> "उत्पाद: ${treatment.getDisplayProductName(currentLanguage)} (${treatment.getDisplayActiveIngredient(currentLanguage)})"
                        AppLanguage.ENGLISH -> "Product: ${treatment.getDisplayProductName(currentLanguage)} (${treatment.getDisplayActiveIngredient(currentLanguage)})"
                    },
                    fontWeight = FontWeight.Bold
                )

                treatment.nearbySellers.forEach { seller ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = seller.getShopName(currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (seller.inStock) com.example.ui.theme.RoyalSuccessBg else com.example.ui.theme.RoyalAlertBg
                                ) {
                                    Text(
                                        text = if (seller.inStock) "✓ IN STOCK" else when (currentLanguage) {
                                            AppLanguage.BENGALI -> "স্টক জানতে যোগাযোগ করুন"
                                            AppLanguage.HINDI -> "स्टॉक हेतु कॉल करें"
                                            AppLanguage.ENGLISH -> "Call for Stock"
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (seller.inStock) com.example.ui.theme.RoyalSuccessText else com.example.ui.theme.RoyalAlertText
                                    )
                                }
                            }
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ঠিকানা: ${seller.getAddress(currentLanguage)}"
                                    AppLanguage.HINDI -> "पता: ${seller.getAddress(currentLanguage)}"
                                    AppLanguage.ENGLISH -> "Address: ${seller.getAddress(currentLanguage)}"
                                },
                                fontSize = 12.sp
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "প্যাক: ${seller.packSize} • আনুমানিক মূল্য: ₹${seller.priceRs.toInt()}"
                                    AppLanguage.HINDI -> "पैक: ${seller.packSize} • अनुमानित मूल्य: ₹${seller.priceRs.toInt()}"
                                    AppLanguage.ENGLISH -> "Pack: ${seller.packSize} • Est. Price: ₹${seller.priceRs.toInt()}"
                                },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "📞 ফোন: ${seller.phone}"
                                    AppLanguage.HINDI -> "📞 फोन: ${seller.phone}"
                                    AppLanguage.ENGLISH -> "📞 Phone: ${seller.phone}"
                                },
                                fontWeight = FontWeight.Bold,
                                color = AgriTextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreenPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "ঠিক আছে"
                        AppLanguage.HINDI -> "ठीक है"
                        AppLanguage.ENGLISH -> "OK"
                    }
                )
            }
        }
    )
}

@Composable
fun MedicineLandCalculatorDialog(
    treatment: VerifiedTreatment,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onDismiss: () -> Unit
) {
    var landUnit by remember { mutableStateOf("Bigha") }
    var landValue by remember { mutableStateOf("1.0") }

    val calculated = remember(landUnit, landValue, treatment) {
        val qty = landValue.toLocalizedDoubleOrNull()?.coerceAtLeast(0.0) ?: 1.0
        when (landUnit) {
            "Katha" -> treatment.calculateRequirementForKatha(qty)
            "Bigha" -> treatment.calculateRequirementForBigha(qty)
            "Decimal" -> treatment.calculateRequirementForDecimal(qty)
            "Acre" -> treatment.calculateRequirementForAcre(qty)
            else -> treatment.calculateRequirementForHectare(qty)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "📏 জমির পরিমাপ ও সঠিক ওষুধের হিসাব"
                    AppLanguage.HINDI -> "📏 भूमि क्षेत्रफल व सटीक दवा मात्रा"
                    AppLanguage.ENGLISH -> "📏 Land Measurement & Exact Dose"
                },
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "ওষুধ: ${treatment.getDisplayProductName(currentLanguage)} (${treatment.getDisplayActiveIngredient(currentLanguage)})"
                        AppLanguage.HINDI -> "दवा: ${treatment.getDisplayProductName(currentLanguage)} (${treatment.getDisplayActiveIngredient(currentLanguage)})"
                        AppLanguage.ENGLISH -> "Medicine: ${treatment.getDisplayProductName(currentLanguage)} (${treatment.getDisplayActiveIngredient(currentLanguage)})"
                    },
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "জমির একক নির্বাচন করুন:"
                        AppLanguage.HINDI -> "भूमि की इकाई चुनें:"
                        AppLanguage.ENGLISH -> "Select Land Unit:"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val units = listOf(
                        "Katha" to LandUnit.KATHA.getLabel(currentLanguage),
                        "Bigha" to LandUnit.BIGHA.getLabel(currentLanguage),
                        "Decimal" to LandUnit.SATAK.getLabel(currentLanguage),
                        "Acre" to LandUnit.ACRE.getLabel(currentLanguage),
                        "Hectare" to LandUnit.HECTARE.getLabel(currentLanguage)
                    )
                    items(units) { (key, label) ->
                        val isSel = landUnit == key
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clickable { landUnit = key }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else AgriTextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = landValue,
                    onValueChange = { landValue = it },
                    label = {
                        Text(when (currentLanguage) {
                            AppLanguage.BENGALI -> "জমির পরিমাণ"
                            AppLanguage.HINDI -> "भूमि की मात्रा"
                            AppLanguage.ENGLISH -> "Land Area"
                        })
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "🎯 আপনার জমির জন্য প্রয়োজনীয় পরিমাণ:"
                                AppLanguage.HINDI -> "🎯 आपकी भूमि के लिए आवश्यक मात्रा:"
                                AppLanguage.ENGLISH -> "🎯 Required Quantity for Your Land:"
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (calculated.first != null) {
                                "${String.format("%.1f", calculated.first)} ${treatment.getDisplayDoseUnit(currentLanguage)}"
                            } else {
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অপ্রযোজ্য"
                                    AppLanguage.HINDI -> "लागू नहीं"
                                    AppLanguage.ENGLISH -> "Not Applicable"
                                }
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "প্রয়োজনীয় জল: আনুমানিক ${calculated.second.toInt()} লিটার"
                                AppLanguage.HINDI -> "आवश्यक पानी: लगभग ${calculated.second.toInt()} लीटर"
                                AppLanguage.ENGLISH -> "Required Water: approx. ${calculated.second.toInt()} Litres"
                            },
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "• ১৬ লিটার ড্রাম প্রতি ডোজ: প্রায় ১৫-২০ মিলি/গ্রাম।"
                                AppLanguage.HINDI -> "• 16 लीटर टंकी प्रति खुराक: लगभग 15-20 मिली/ग्राम।"
                                AppLanguage.ENGLISH -> "• Per 16L sprayer tank: approx. 15-20 ml/gm (verify label)."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreenPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "বন্ধ করুন"
                        AppLanguage.HINDI -> "बंद करें"
                        AppLanguage.ENGLISH -> "Close"
                    }
                )
            }
        }
    )
}
