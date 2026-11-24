package com.example.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CropMasterDatabase
import com.example.data.datasource.VerifiedTreatmentDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.LandUnit
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.ui.theme.AgriWarningAmber
import com.example.util.L10n
import com.example.util.isCompactNarrowScreen
import com.example.util.toLocalizedDoubleOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DosageCalculatorScreen(
    currentLanguage: AppLanguage,
    initialCropName: String? = null,
    onBack: () -> Unit,
    onContactExpert: () -> Unit
) {
    var selectedLandUnit by remember { mutableStateOf(LandUnit.BIGHA) }
    var landAreaInput by remember { mutableStateOf("1.0") }
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    val cropList = remember { CropMasterDatabase.crops }
    var selectedCrop by remember {
        mutableStateOf(
            cropList.find {
                it.cropId.equals(initialCropName, true) ||
                        (!initialCropName.isNullOrBlank() && (it.nameBn.contains(initialCropName, true) ||
                                it.nameHi.contains(initialCropName, true) ||
                                it.nameEn.contains(initialCropName, true)))
            } ?: cropList.first()
        )
    }
    var cropDropdownExpanded by remember { mutableStateOf(false) }

    // Treatments available for selected crop
    val availableTreatments = remember(selectedCrop) {
        VerifiedTreatmentDatabase.treatments.filter {
            it.cropName.equals(selectedCrop.cropId, true) ||
                    selectedCrop.nameEn.contains(it.cropName, true) ||
                    it.cropName.contains(selectedCrop.nameEn, true)
        }
    }

    var selectedTreatment by remember(availableTreatments) {
        mutableStateOf(availableTreatments.firstOrNull())
    }
    var treatmentDropdownExpanded by remember { mutableStateOf(false) }
    val isNarrow = isCompactNarrowScreen()

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
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("calc_back_btn")
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
                            AppLanguage.BENGALI -> "💊 ওষুধ ও সার ক্যালকুলেটর"
                            AppLanguage.HINDI -> "💊 दवा व उर्वरक कैलकुलेटर"
                            AppLanguage.ENGLISH -> "Safe Dosage Calculator"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
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
                .widthIn(max = 760.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mandatory Question Card: “আপনার জমির পরিমাণ কত?”
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "আপনার জমির পরিমাণ কত?"
                            AppLanguage.HINDI -> "आपकी भूमि का क्षेत्रफल कितना है?"
                            AppLanguage.ENGLISH -> "What is your land area?"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val unitDropdown: @Composable (Modifier) -> Unit = { mod ->
                        ExposedDropdownMenuBox(
                            expanded = unitDropdownExpanded,
                            onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded },
                            modifier = mod
                        ) {
                            OutlinedTextField(
                                value = selectedLandUnit.getLabel(currentLanguage),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("land_unit_selector"),
                                label = {
                                    Text(when (currentLanguage) {
                                        AppLanguage.BENGALI -> "একক (Unit)"
                                        AppLanguage.HINDI -> "इकाई (Unit)"
                                        AppLanguage.ENGLISH -> "Unit"
                                    })
                                },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = unitDropdownExpanded,
                                onDismissRequest = { unitDropdownExpanded = false }
                            ) {
                                LandUnit.values().forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit.getLabel(currentLanguage), fontWeight = FontWeight.SemiBold) },
                                        onClick = {
                                            selectedLandUnit = unit
                                            unitDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (isNarrow) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = landAreaInput,
                                onValueChange = { landAreaInput = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("land_area_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                label = {
                                    Text(when (currentLanguage) {
                                        AppLanguage.BENGALI -> "পরিমাণ (Area)"
                                        AppLanguage.HINDI -> "मात्रा (Area)"
                                        AppLanguage.ENGLISH -> "Area Amount"
                                    })
                                },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                shape = RoundedCornerShape(12.dp)
                            )
                            unitDropdown(Modifier.fillMaxWidth())
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = landAreaInput,
                                onValueChange = { landAreaInput = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("land_area_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                label = {
                                    Text(when (currentLanguage) {
                                        AppLanguage.BENGALI -> "পরিমাণ (Area)"
                                        AppLanguage.HINDI -> "मात्रा (Area)"
                                        AppLanguage.ENGLISH -> "Area Amount"
                                    })
                                },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                shape = RoundedCornerShape(12.dp)
                            )
                            unitDropdown(Modifier.weight(1.3f))
                        }
                    }
                }
            }

            // Crop Selector
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "ফসল নির্বাচন করুন:"
                    AppLanguage.HINDI -> "फसल चुनें:"
                    AppLanguage.ENGLISH -> "Select Crop:"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AgriTextPrimary
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
                        .testTag("calc_crop_selector"),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = cropDropdownExpanded,
                    onDismissRequest = { cropDropdownExpanded = false }
                ) {
                    cropList.forEach { crop ->
                        DropdownMenuItem(
                            text = { Text("${crop.iconEmoji} ${crop.getName(currentLanguage)}", fontWeight = FontWeight.Medium) },
                            onClick = {
                                selectedCrop = crop
                                cropDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Problem / Disease Selector
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "সমস্যা বা রোগ নির্বাচন করুন:"
                    AppLanguage.HINDI -> "समस्या या रोग चुनें:"
                    AppLanguage.ENGLISH -> "Select Problem or Disease:"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AgriTextPrimary
            )

            if (availableTreatments.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = treatmentDropdownExpanded,
                    onExpandedChange = { treatmentDropdownExpanded = !treatmentDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedTreatment?.getProblemName(currentLanguage) ?: when (currentLanguage) {
                            AppLanguage.BENGALI -> "নির্বাচন করুন"
                            AppLanguage.HINDI -> "चुनें"
                            AppLanguage.ENGLISH -> "Select"
                        },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = treatmentDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("calc_treatment_selector"),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = treatmentDropdownExpanded,
                        onDismissRequest = { treatmentDropdownExpanded = false }
                    ) {
                        availableTreatments.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.getProblemName(currentLanguage), fontWeight = FontWeight.Medium) },
                                onClick = {
                                    selectedTreatment = t
                                    treatmentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                // Notice when verified dosage not in official DB
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = AgriWarningAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অনুমোদিত ডোজ বিজ্ঞপ্তি"
                                    AppLanguage.HINDI -> "अनुमोदित खुराक सूचना"
                                    AppLanguage.ENGLISH -> "Approved Dosage Notice"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AgriTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = L10n.t("unverified_dosage_notice", currentLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = AgriTextSecondary
                        )
                    }
                }
            }

            // Detailed Verified Calculation & IPM Display Card
            val treatment = selectedTreatment
            val areaVal = landAreaInput.toLocalizedDoubleOrNull() ?: 0.0

            if (treatment != null && areaVal <= 0.0) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "⚠️ সঠিক ডোজ দেখতে উপরের ঘরে ০-এর বেশি জমির পরিমাণ লিখুন (যেমন: 1.5 বা ১.৫)।"
                            AppLanguage.HINDI -> "⚠️ सटीक खुराक देखने के लिए ऊपर 0 से अधिक भूमि क्षेत्रफल दर्ज करें (जैसे: 1.5)।"
                            AppLanguage.ENGLISH -> "⚠️ Enter a valid land area greater than 0 above to calculate dosage."
                        },
                        modifier = Modifier.padding(14.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = AgriTextPrimary
                    )
                }
            }

            if (treatment != null && areaVal > 0.0) {
                val areaInHectares = selectedLandUnit.toHectares(areaVal)
                val (calcDose, calcWater) = treatment.calculateRequirementForHectare(areaInHectares)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("treatment_card_result"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "📋 অনুমোদিত সমাধান কার্ড"
                                    AppLanguage.HINDI -> "📋 अनुमोदित समाधान कार्ड"
                                    AppLanguage.ENGLISH -> "📋 Approved Solution Card"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = treatment.getResistanceGroup(currentLanguage),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // 1. Problem & Symptoms
                        InfoRow(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "সমস্যা:"
                                AppLanguage.HINDI -> "समस्या:"
                                AppLanguage.ENGLISH -> "Problem:"
                            },
                            value = treatment.getProblemName(currentLanguage)
                        )
                        InfoRow(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "লক্ষণ:"
                                AppLanguage.HINDI -> "लक्षण:"
                                AppLanguage.ENGLISH -> "Symptoms:"
                            },
                            value = treatment.getSymptoms(currentLanguage)
                        )
                        InfoRow(
                            label = when (currentLanguage) {
                                AppLanguage.BENGALI -> "সম্ভাব্য কারণ:"
                                AppLanguage.HINDI -> "संभावित कारण:"
                                AppLanguage.ENGLISH -> "Possible Cause:"
                            },
                            value = treatment.getPossibleCause(currentLanguage)
                        )

                        // 2. First Action & Biological Control
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "১ম করণীয় (কম ঝুঁকির পদক্ষেপ):"
                                        AppLanguage.HINDI -> "प्रथम कदम (कम जोखिम वाला उपाय):"
                                        AppLanguage.ENGLISH -> "First Action (Low-Risk Step):"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = treatment.getFirstAction(currentLanguage),
                                    fontWeight = FontWeight.Medium,
                                    color = AgriTextPrimary,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "জৈব সমাধান:"
                                        AppLanguage.HINDI -> "जैविक समाधान:"
                                        AppLanguage.ENGLISH -> "Biological Control Option:"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = treatment.getBiologicalOption(currentLanguage),
                                    fontWeight = FontWeight.Medium,
                                    color = AgriTextPrimary,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // 3. Calculated Requirement based on Land
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.RoyalGreenCardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.RoyalGold.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                val cardDarkText = com.example.ui.theme.RoyalTextPrimary
                                val cardMutedText = com.example.ui.theme.RoyalTextSecondary
                                val cardGreenTitle = com.example.ui.theme.RoyalGold
                                val hectareUnitLabel = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "হেক্টর"
                                    AppLanguage.HINDI -> "हेक्टेयर"
                                    AppLanguage.ENGLISH -> "hectare"
                                }
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "🧪 আপনার জমির জন্য অনুমোদিত হিসাব:"
                                        AppLanguage.HINDI -> "🧪 आपकी भूमि के लिए अनुमोदित गणना:"
                                        AppLanguage.ENGLISH -> "🧪 Approved Calculation for Your Land:"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = cardGreenTitle,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "নির্বাচিত জমি: $areaVal ${selectedLandUnit.getLabel(currentLanguage)} (≈ ${"%.3f".format(areaInHectares)} $hectareUnitLabel)"
                                        AppLanguage.HINDI -> "चयनित भूमि: $areaVal ${selectedLandUnit.getLabel(currentLanguage)} (≈ ${"%.3f".format(areaInHectares)} $hectareUnitLabel)"
                                        AppLanguage.ENGLISH -> "Selected Land: $areaVal ${selectedLandUnit.getLabel(currentLanguage)} (≈ ${"%.3f".format(areaInHectares)} $hectareUnitLabel)"
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    color = cardDarkText
                                )
                                val activeIng = treatment.getActiveIngredient(currentLanguage)
                                if (calcDose != null && activeIng != null) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "প্রয়োজনীয় রাসায়নিক: ${"%.1f".format(calcDose)} ${treatment.getDoseUnit(currentLanguage)}"
                                            AppLanguage.HINDI -> "आवश्यक रसायन: ${"%.1f".format(calcDose)} ${treatment.getDoseUnit(currentLanguage)}"
                                            AppLanguage.ENGLISH -> "Required Chemical: ${"%.1f".format(calcDose)} ${treatment.getDoseUnit(currentLanguage)}"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = com.example.ui.theme.RoyalSoftGold,
                                        fontSize = 17.sp
                                    )
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "সক্রিয় উপাদান: $activeIng"
                                            AppLanguage.HINDI -> "सक्रिय तत्व: $activeIng"
                                            AppLanguage.ENGLISH -> "Active Ingredient: $activeIng"
                                        },
                                        fontWeight = FontWeight.Medium,
                                        color = cardMutedText
                                    )
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "বাজারের প্রচলিত ব্র‍্যান্ড: ${treatment.getBrandExamples(currentLanguage)}"
                                            AppLanguage.HINDI -> "प्रचलित ब्रांड: ${treatment.getBrandExamples(currentLanguage)}"
                                            AppLanguage.ENGLISH -> "Common Brands: ${treatment.getBrandExamples(currentLanguage)}"
                                        },
                                        fontWeight = FontWeight.Medium,
                                        color = cardMutedText
                                    )
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "প্রয়োজনীয় স্প্রে জল: আনুমানিক ${"%.0f".format(calcWater)} লিটার"
                                            AppLanguage.HINDI -> "आवश्यक स्प्रे पानी: लगभग ${"%.0f".format(calcWater)} लीटर"
                                            AppLanguage.ENGLISH -> "Required Spray Water: Approx. ${"%.0f".format(calcWater)} Litres"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = cardDarkText
                                    )
                                } else {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "এই অবস্থায় কোনো রাসায়নিক বিষ প্রয়োজন নেই, শুধুমাত্র জৈব বা পরিচর্যা যথেষ্ট।"
                                            AppLanguage.HINDI -> "इस अवस्था में किसी रासायनिक कीटनाशक की आवश्यकता नहीं है, केवल जैविक या देखभाल पर्याप्त है।"
                                            AppLanguage.ENGLISH -> "No chemical pesticide is required at this stage; organic care alone is sufficient."
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = cardGreenTitle
                                    )
                                }
                            }
                        }

                        // 4. Safety & Waiting Period (PHI)
                        val phiCard: @Composable (Modifier) -> Unit = { mod ->
                            Card(
                                modifier = mod,
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "ফসল তোলার ব্যবধান (PHI)"
                                            AppLanguage.HINDI -> "तुड़ाई प्रतीक्षा अवधि (PHI)"
                                            AppLanguage.ENGLISH -> "Pre-Harvest Interval (PHI)"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = AgriTextSecondary
                                    )
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "${treatment.preHarvestIntervalDays} দিন"
                                            AppLanguage.HINDI -> "${treatment.preHarvestIntervalDays} दिन"
                                            AppLanguage.ENGLISH -> "${treatment.preHarvestIntervalDays} days"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = AgriWarningAmber
                                    )
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "স্প্রে করার পর দিন বন্ধ রাখুন"
                                            AppLanguage.HINDI -> "छिड़काव के बाद इतने दिन रुकें"
                                            AppLanguage.ENGLISH -> "Wait days after spraying"
                                        },
                                        fontSize = 11.sp,
                                        color = AgriTextSecondary
                                    )
                                }
                            }
                        }
                        val reiCard: @Composable (Modifier) -> Unit = { mod ->
                            Card(
                                modifier = mod,
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "পুনঃপ্রবেশ সময় (REI)"
                                            AppLanguage.HINDI -> "पुनः प्रवेश समय (REI)"
                                            AppLanguage.ENGLISH -> "Re-Entry Interval (REI)"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = AgriTextSecondary
                                    )
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "${treatment.reEntryIntervalHours} ঘণ্টা"
                                            AppLanguage.HINDI -> "${treatment.reEntryIntervalHours} घंटे"
                                            AppLanguage.ENGLISH -> "${treatment.reEntryIntervalHours} hours"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "ক্ষেতে না নামার সময়"
                                            AppLanguage.HINDI -> "खेत में न जाने की अवधि"
                                            AppLanguage.ENGLISH -> "Keep out of field duration"
                                        },
                                        fontSize = 11.sp,
                                        color = AgriTextSecondary
                                    )
                                }
                            }
                        }
                        if (isNarrow) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                phiCard(Modifier.fillMaxWidth())
                                reiCard(Modifier.fillMaxWidth())
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                phiCard(Modifier.weight(1f))
                                reiCard(Modifier.weight(1f))
                            }
                        }

                        // Safety Advice
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = treatment.getSafetyInstructions(currentLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = AgriTextPrimary,
                                lineHeight = 20.sp
                            )
                        }

                        // Official Source Attribution
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "উৎস: ${treatment.officialSource} (অফিসিয়াল লেবেল নির্দেশিকা)"
                                AppLanguage.HINDI -> "स्रोत: ${treatment.officialSource} (आधिकारिक लेबल दिशानिर्देश)"
                                AppLanguage.ENGLISH -> "Source: ${treatment.officialSource} (Official Label Guidelines)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AgriTextSecondary
                        )

                        // Button to Contact Agriculture Expert
                        Button(
                            onClick = onContactExpert,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .testTag("calc_expert_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AgriGreenPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "কৃষি বিশেষজ্ঞের সাথে কথা বলুন"
                                    AppLanguage.HINDI -> "कृषि विशेषज्ञ से बात करें"
                                    AppLanguage.ENGLISH -> "Talk to Agriculture Expert"
                                },
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp
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
private fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        Text(text = value, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = AgriTextPrimary, lineHeight = 20.sp)
    }
}
