package com.example.ui.crops

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.datasource.CropLocaleData
import com.example.data.datasource.CropMasterDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.CropCategory
import com.example.data.model.CropRegion
import com.example.data.model.CropSeason
import com.example.data.model.MasterCrop
import com.example.data.repository.CustomCropRepository
import com.example.ui.theme.AgriGreenPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminAddMasterCropScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onCropSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        CustomCropRepository.ensureInitialized(context)
    }

    val allCrops by CropMasterDatabase.cropsFlow.collectAsState()
    val customCrops = remember(allCrops) { allCrops.filter { it.isCustomAdded } }

    var nameEn by remember { mutableStateOf("") }
    var nameBn by remember { mutableStateOf("") }
    var nameHi by remember { mutableStateOf("") }
    var scientificName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CropCategory.VEGETABLES) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var subCategory by remember { mutableStateOf("") }
    var iconEmoji by remember { mutableStateOf("🌱") }
    var harvestDaysInput by remember { mutableStateOf("65") }

    var selectedSeasons by remember { mutableStateOf(setOf(CropSeason.KHARIF, CropSeason.RABI)) }
    var selectedRegions by remember {
        mutableStateOf(setOf(CropRegion.SILIGURI_NORTH_BENGAL, CropRegion.WEST_BENGAL, CropRegion.PAN_INDIA))
    }

    var seasonTextEn by remember { mutableStateOf("") }
    var seasonTextHi by remember { mutableStateOf("") }
    var seasonTextBn by remember { mutableStateOf("") }

    var soilEn by remember { mutableStateOf("") }
    var spacingEn by remember { mutableStateOf("") }
    var seedRateEn by remember { mutableStateOf("") }
    var yieldEn by remember { mutableStateOf("") }
    var varietiesEn by remember { mutableStateOf("") }
    var summaryEn by remember { mutableStateOf("") }
    var summaryBn by remember { mutableStateOf("") }
    var summaryHi by remember { mutableStateOf("") }
    var costEn by remember { mutableStateOf("") }
    var harvestMethodEn by remember { mutableStateOf("") }
    var expertTipsEn by remember { mutableStateOf("") }
    var pestsCommaEn by remember { mutableStateOf("") }
    var diseasesCommaEn by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            selectedPhotoUri = uri.toString()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
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
                            .testTag("admin_add_crop_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "অ্যাডমিন: নতুন ফসল ও উদ্ভিদ যুক্ত করুন"
                            AppLanguage.HINDI -> "एडमिन: नई फसल व पौधा जोड़ें"
                            AppLanguage.ENGLISH -> "Admin: Add Master Crop / Plant"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
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
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "📚 সম্প্রসারণযোগ্য মাস্টার লাইব্রেরি"
                                AppLanguage.HINDI -> "📚 विस्तार योग्य मास्टर फसल पुस्तकालय"
                                AppLanguage.ENGLISH -> "📚 Expandable Crop Master Library"
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "এখান থেকে নতুন শাকসবজি, ফল, ফুল, ঔষধি বা নার্সারি গাছ যুক্ত করলে অ্যাপ পুনঃনির্মাণ ছাড়াই সঙ্গে সঙ্গে সার্চ ও তালিকায় দেখাবে।"
                                AppLanguage.HINDI -> "यहां से जोड़ी गई नई फसल, फूल, फल या नर्सरी पौधे बिना ऐप दोबारा बनाए तुरंत खोज और सूची में दिखेंगे।"
                                AppLanguage.ENGLISH -> "Crops added here are saved locally and immediately appear in search, filters, and detail guides without rebuilding the app."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }

                // Photo Picker Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!selectedPhotoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = selectedPhotoUri,
                                contentDescription = "Selected crop image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = iconEmoji.ifBlank { "🌱" }, fontSize = 28.sp)
                                }
                            }
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
                                .testTag("admin_pick_crop_photo_btn")
                        ) {
                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> if (selectedPhotoUri == null) "গ্যালারি থেকে ছবি বাছুন (ঐচ্ছিক)" else "ছবি পরিবর্তন করুন"
                                    AppLanguage.HINDI -> if (selectedPhotoUri == null) "गैलरी से फोटो चुनें (वैकल्पिक)" else "फोटो बदलें"
                                    AppLanguage.ENGLISH -> if (selectedPhotoUri == null) "Pick Real Photo (Optional)" else "Change Selected Photo"
                                },
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "ইংরেজি নাম (বাধ্যতামূলক)*"
                                AppLanguage.HINDI -> "अंग्रेजी नाम (अनिवार्य)*"
                                AppLanguage.ENGLISH -> "Crop / Plant Name (English)*"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_crop_name_en_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameBn,
                        onValueChange = { nameBn = it },
                        label = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "বাংলা নাম"
                                    AppLanguage.HINDI -> "बंगाली नाम"
                                    AppLanguage.ENGLISH -> "Bengali Name"
                                }
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_crop_name_bn_input")
                    )
                    OutlinedTextField(
                        value = nameHi,
                        onValueChange = { nameHi = it },
                        label = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "হিন্দি নাম"
                                    AppLanguage.HINDI -> "हिंदी नाम"
                                    AppLanguage.ENGLISH -> "Hindi Name"
                                }
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_crop_name_hi_input")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = scientificName,
                        onValueChange = { scientificName = it },
                        label = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "বৈজ্ঞানিক নাম"
                                    AppLanguage.HINDI -> "वैज्ञानिक नाम"
                                    AppLanguage.ENGLISH -> "Scientific Name"
                                }
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlinedTextField(
                        value = harvestDaysInput,
                        onValueChange = { harvestDaysInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "দিন"
                                    AppLanguage.HINDI -> "दिन"
                                    AppLanguage.ENGLISH -> "Days"
                                }
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f)
                    )
                }

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedCategory.toMasterGroup().codeLetter} • ${selectedCategory.getDisplayName(currentLanguage)}",
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "শ্রেণি (A–L)"
                                    AppLanguage.HINDI -> "श्रेणी (A–L)"
                                    AppLanguage.ENGLISH -> "Category (A–L)"
                                }
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("admin_crop_category_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        CropCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.toMasterGroup().codeLetter}: ${cat.getDisplayName(currentLanguage)}") },
                                onClick = {
                                    selectedCategory = cat
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = subCategory,
                        onValueChange = { subCategory = it },
                        label = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "উপ-শ্রেণি"
                                    AppLanguage.HINDI -> "उप-श्रेणी"
                                    AppLanguage.ENGLISH -> "Sub-Category"
                                }
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlinedTextField(
                        value = iconEmoji,
                        onValueChange = { iconEmoji = it },
                        label = { Text("Emoji") },
                        singleLine = true,
                        modifier = Modifier.weight(0.6f)
                    )
                }

                // Season Chips
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "উপযুক্ত ঋতু:"
                        AppLanguage.HINDI -> "उपयुक्त मौसम:"
                        AppLanguage.ENGLISH -> "Suitable Seasons:"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CropSeason.values().forEach { season ->
                        val selected = season in selectedSeasons
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedSeasons = if (selected && selectedSeasons.size > 1) {
                                    selectedSeasons - season
                                } else {
                                    selectedSeasons + season
                                }
                            },
                            label = { Text(season.getDisplayName(currentLanguage), fontSize = 12.sp) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                // Region Chips
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "আঞ্চলিক উপযোগিতা:"
                        AppLanguage.HINDI -> "क्षेत्रीय उपयुक्तता:"
                        AppLanguage.ENGLISH -> "Regional Suitability:"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CropRegion.values().forEach { region ->
                        val selected = region in selectedRegions
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedRegions = if (selected && selectedRegions.size > 1) {
                                    selectedRegions - region
                                } else {
                                    selectedRegions + region
                                }
                            },
                            label = { Text(region.getDisplayName(currentLanguage), fontSize = 12.sp) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = seasonTextEn,
                    onValueChange = { seasonTextEn = it },
                    label = { Text("Sowing / Planting Window (EN)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = seasonTextBn,
                        onValueChange = { seasonTextBn = it },
                        label = { Text("রোপণ সময় (BN)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = seasonTextHi,
                        onValueChange = { seasonTextHi = it },
                        label = { Text("बुवाई समय (HI)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = soilEn,
                    onValueChange = { soilEn = it },
                    label = { Text("Soil Requirement / Media") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = seedRateEn,
                        onValueChange = { seedRateEn = it },
                        label = { Text("Seed / Sapling Rate") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = spacingEn,
                        onValueChange = { spacingEn = it },
                        label = { Text("Plant Spacing") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = yieldEn,
                    onValueChange = { yieldEn = it },
                    label = { Text("Expected Yield / Output") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = varietiesEn,
                    onValueChange = { varietiesEn = it },
                    label = { Text("Recommended Varieties / Cultivars") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = summaryEn,
                    onValueChange = { summaryEn = it },
                    label = { Text("Cultivation Overview (English)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = summaryBn,
                    onValueChange = { summaryBn = it },
                    label = { Text("চাষের সারসংক্ষেপ (বাংলা - ঐচ্ছিক)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = summaryHi,
                    onValueChange = { summaryHi = it },
                    label = { Text("खेती का सारांश (हिन्दी - वैकल्पिक)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = harvestMethodEn,
                        onValueChange = { harvestMethodEn = it },
                        label = { Text("Harvesting Method") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = costEn,
                        onValueChange = { costEn = it },
                        label = { Text("Approx. Cost Info") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = pestsCommaEn,
                    onValueChange = { pestsCommaEn = it },
                    label = { Text("Major Pests (comma-separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = diseasesCommaEn,
                    onValueChange = { diseasesCommaEn = it },
                    label = { Text("Major Diseases (comma-separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = expertTipsEn,
                    onValueChange = { expertTipsEn = it },
                    label = { Text("Expert Tips & Precaution Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val trimmedEn = nameEn.trim()
                        if (trimmedEn.isBlank()) {
                            val msg = when (currentLanguage) {
                                AppLanguage.BENGALI -> "দয়া করে ফসল বা উদ্ভিদের ইংরেজি নাম লিখুন।"
                                AppLanguage.HINDI -> "कृपया फसल या पौधे का अंग्रेजी नाम दर्ज करें।"
                                AppLanguage.ENGLISH -> "Please enter the crop or plant English name."
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val slug = trimmedEn.lowercase()
                            .replace(Regex("[^a-z0-9]+"), "_")
                            .trim('_')
                            .ifBlank { "custom_crop_${System.currentTimeMillis()}" }
                        val uniqueId = if (CropMasterDatabase.getCropById(slug)?.isCustomAdded == false) {
                            "${slug}_custom"
                        } else {
                            slug
                        }
                        val cleanBnName = nameBn.trim().ifBlank { "$trimmedEn (কাস্টম)" }
                        val cleanHiName = nameHi.trim().ifBlank { trimmedEn }
                        val days = harvestDaysInput.trim().toIntOrNull()?.takeIf { it > 0 } ?: 60

                        val cleanSeasonEn = seasonTextEn.trim().ifBlank { "Suitable regional season" }
                        val cleanSeasonHi = seasonTextHi.trim().ifBlank { cleanSeasonEn }
                        val cleanSeasonBn = seasonTextBn.trim().ifBlank { cleanSeasonEn }

                        val cleanSoilEn = soilEn.trim().ifBlank { "Well-drained fertile loamy soil" }
                        val cleanRateEn = seedRateEn.trim().ifBlank { "Per KVK / nursery recommendation" }
                        val cleanSpacingEn = spacingEn.trim().ifBlank { "Standard row spacing" }
                        val cleanYieldEn = yieldEn.trim().ifBlank { "Depends on variety & regional care" }

                        val cleanSumEn = summaryEn.trim().ifBlank { "$trimmedEn cultivar added to Master Library." }
                        val cleanSumHi = summaryHi.trim().ifBlank { cleanSumEn }
                        val cleanSumBn = summaryBn.trim().ifBlank { cleanSumEn }

                        val pestsList = pestsCommaEn.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val diseasesList = diseasesCommaEn.split(",").map { it.trim() }.filter { it.isNotBlank() }

                        val crop = MasterCrop(
                            cropId = uniqueId,
                            nameBn = cleanBnName,
                            nameHi = cleanHiName,
                            nameEn = trimmedEn,
                            scientificName = scientificName.trim().ifBlank { "Botanical Cultivar" },
                            category = selectedCategory,
                            subCategory = subCategory.trim().ifBlank { selectedCategory.enName },
                            aliases = listOfNotNull(trimmedEn, nameBn.trim().takeIf { it.isNotBlank() }, nameHi.trim().takeIf { it.isNotBlank() }),
                            seasons = selectedSeasons.toList(),
                            regions = selectedRegions.toList(),
                            bestSeasonBn = cleanSeasonBn,
                            soilBn = cleanSoilEn,
                            seedRatePerBighaBn = cleanRateEn,
                            spacingBn = cleanSpacingEn,
                            harvestDays = days,
                            expectedYieldBn = cleanYieldEn,
                            summaryBn = cleanSumBn,
                            summaryHi = cleanSumHi,
                            summaryEn = cleanSumEn,
                            varietiesBn = varietiesEn.trim(),
                            varietiesHi = varietiesEn.trim(),
                            varietiesEn = varietiesEn.trim(),
                            estimatedCostBn = costEn.trim(),
                            estimatedCostHi = costEn.trim(),
                            estimatedCostEn = costEn.trim(),
                            harvestMethodBn = harvestMethodEn.trim(),
                            harvestMethodHi = harvestMethodEn.trim(),
                            harvestMethodEn = harvestMethodEn.trim(),
                            expertTipsBn = expertTipsEn.trim(),
                            expertTipsHi = expertTipsEn.trim(),
                            expertTipsEn = expertTipsEn.trim(),
                            commonPests = pestsList,
                            commonDiseases = diseasesList,
                            iconEmoji = iconEmoji.trim().ifBlank { "🌱" },
                            customImageUri = selectedPhotoUri,
                            isCustomAdded = true
                        )

                        val localeData = CropLocaleData(
                            seasonHi = cleanSeasonHi,
                            seasonEn = cleanSeasonEn,
                            soilHi = cleanSoilEn,
                            soilEn = cleanSoilEn,
                            seedRateHi = cleanRateEn,
                            seedRateEn = cleanRateEn,
                            spacingHi = cleanSpacingEn,
                            spacingEn = cleanSpacingEn,
                            yieldHi = cleanYieldEn,
                            yieldEn = cleanYieldEn
                        )

                        CustomCropRepository.saveCustomCrop(
                            context,
                            CustomCropRepository.CustomCropRecord(crop, localeData)
                        )

                        val savedMsg = when (currentLanguage) {
                            AppLanguage.BENGALI -> "$cleanBnName মাস্টার লাইব্রেরিতে সংরক্ষিত হয়েছে!"
                            AppLanguage.HINDI -> "$cleanHiName मास्टर लाइब्रेरी में सहेजा गया!"
                            AppLanguage.ENGLISH -> "$trimmedEn saved to Master Library!"
                        }
                        Toast.makeText(context, savedMsg, Toast.LENGTH_SHORT).show()
                        onCropSaved()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("admin_save_master_crop_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreenPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "মাস্টার লাইব্রেরিতে সংরক্ষণ করুন"
                            AppLanguage.HINDI -> "मास्टर लाइब्रेरी में सहेजें"
                            AppLanguage.ENGLISH -> "Save Crop to Master Library"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                if (customCrops.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "অ্যাডমিন দ্বারা যুক্ত ফসল (${customCrops.size}):"
                            AppLanguage.HINDI -> "एडमिन द्वारा जोड़ी गई फसलें (${customCrops.size}):"
                            AppLanguage.ENGLISH -> "Custom Admin-Added Crops (${customCrops.size}):"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    customCrops.forEach { custom ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    CropThumbnailVisual(
                                        crop = custom,
                                        lang = currentLanguage,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = custom.getName(currentLanguage),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = custom.getMasterGroup().getDisplayName(currentLanguage),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { CustomCropRepository.deleteCustomCrop(context, custom.cropId) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Custom Crop",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
