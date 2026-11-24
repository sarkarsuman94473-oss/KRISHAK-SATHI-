package com.example.ui.tea

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CropMasterDatabase
import com.example.data.datasource.VerifiedTreatmentDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.TeaGarden
import com.example.data.remote.FirestoreRepository
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.util.L10n
import com.example.util.isCompactNarrowScreen
import com.example.util.toLocalizedDoubleOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun TeaGardenScreen(
    currentLanguage: AppLanguage,
    userId: String,
    onBack: () -> Unit,
    onOpenSprayCalculator: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { FirestoreRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    val teaGardens by repository.observeTeaGardens(userId).collectAsState(initial = emptyList())
    val teaMaster = remember { CropMasterDatabase.crops.first { it.cropId == "tea" } }

    var gardenName by remember(currentLanguage) {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "আমার চা বাগান (উত্তরবঙ্গ সেকশন)"
                AppLanguage.HINDI -> "मेरा चाय बागान (उत्तर बंगाल अनुभाग)"
                AppLanguage.ENGLISH -> "My Tea Garden (North Bengal Section)"
            }
        )
    }
    var location by remember(currentLanguage) {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "জলপাইগুড়ি / ডুয়ার্স"
                AppLanguage.HINDI -> "जलपाईगुड़ी / डुआर्स"
                AppLanguage.ENGLISH -> "Jalpaiguri / Dooars"
            }
        )
    }
    var landArea by remember { mutableStateOf("5.0") }
    var landUnit by remember(currentLanguage) {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "বিঘা"
                AppLanguage.HINDI -> "बीघा"
                AppLanguage.ENGLISH -> "Bigha"
            }
        )
    }

    var isSaving by remember { mutableStateOf(false) }
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
                            .testTag("tea_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🍵 ${L10n.t("tea_garden_title", currentLanguage)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
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
                .imePadding()
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(L10n.t("tea_tab_details", currentLanguage), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(L10n.t("tea_tab_guide", currentLanguage), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(L10n.t("tea_tab_calculator", currentLanguage), fontWeight = FontWeight.Bold) }
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
            when (selectedTab) {
                0 -> {
                    // Garden Profile & Management Details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "চা বাগান প্রোফাইল"
                                        AppLanguage.HINDI -> "चाय बागान प्रोफ़ाइल"
                                        AppLanguage.ENGLISH -> "Tea Garden Profile"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "ক্ষুদ্র চা চাষী ও সাধারণ বাগান পরিচালনার সহজ হিসাব।"
                                        AppLanguage.HINDI -> "छोटे चाय उत्पादकों और सामान्य बागान प्रबंधन का सरल हिसाब।"
                                        AppLanguage.ENGLISH -> "Simple management tracker for small tea growers and estates."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = gardenName,
                            onValueChange = { gardenName = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "বাগানের নাম"
                                        AppLanguage.HINDI -> "बागान का नाम"
                                        AppLanguage.ENGLISH -> "Garden Name"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
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
                                                AppLanguage.BENGALI -> "জমির পরিমাণ"
                                                AppLanguage.HINDI -> "भूमि का क्षेत्रफल"
                                                AppLanguage.ENGLISH -> "Land Area"
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
                                                AppLanguage.BENGALI -> "একক (বিঘা/একর)"
                                                AppLanguage.HINDI -> "इकाई (बीघा/एकड़)"
                                                AppLanguage.ENGLISH -> "Unit (Bigha/Acre)"
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = landArea,
                                    onValueChange = { landArea = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    label = {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "জমির পরিমাণ"
                                                AppLanguage.HINDI -> "भूमि का क्षेत्रफल"
                                                AppLanguage.ENGLISH -> "Land Area"
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
                                                AppLanguage.BENGALI -> "একক (বিঘা/একর)"
                                                AppLanguage.HINDI -> "इकाई (बीघा/एकड़)"
                                                AppLanguage.ENGLISH -> "Unit (Bigha/Acre)"
                                            }
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অবস্থান (জেলা / ব্লক)"
                                        AppLanguage.HINDI -> "स्थान (जिला / ब्लॉक)"
                                        AppLanguage.ENGLISH -> "Location (District / Block)"
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Key Garden Metrics
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "🌱 বর্তমান বাগান অবস্থা:"
                                        AppLanguage.HINDI -> "🌱 वर्तमान बागान स्थिति:"
                                        AppLanguage.ENGLISH -> "🌱 Current Garden Status:"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "• চায়ের ক্লোন জাত: TV-1 / TV-9 / বেটজান"
                                        AppLanguage.HINDI -> "• चाय की क्लोन किस्म: TV-1 / TV-9 / बेटजान"
                                        AppLanguage.ENGLISH -> "• Tea Clonal Variety: TV-1 / TV-9 / Betjan"
                                    },
                                    fontWeight = FontWeight.Medium,
                                    color = AgriTextPrimary
                                )
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "• গাছের বয়স: আনুমানিক ৬ বছর (পরিপক্ক ঝোপ)"
                                        AppLanguage.HINDI -> "• पौधे की आयु: लगभग 6 वर्ष (परिपक्व झाड़ी)"
                                        AppLanguage.ENGLISH -> "• Bush Age: Approx. 6 years (Mature bush)"
                                    },
                                    fontWeight = FontWeight.Medium,
                                    color = AgriTextPrimary
                                )
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "• ছায়া গাছ: সিরিষ (Albizia odoratissima) পর্যাপ্ত"
                                        AppLanguage.HINDI -> "• छायादार वृक्ष: सिरिस (Albizia odoratissima) पर्याप्त"
                                        AppLanguage.ENGLISH -> "• Shade Trees: Siris (Albizia odoratissima) adequate"
                                    },
                                    fontWeight = FontWeight.Medium,
                                    color = AgriTextPrimary
                                )
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "• নিকাশী নালা: বর্ষার আগে পরিষ্কার আবশ্যক"
                                        AppLanguage.HINDI -> "• जल निकासी नालियां: मानसून से पहले सफाई आवश्यक"
                                        AppLanguage.ENGLISH -> "• Drainage Channels: Cleaning required before monsoon"
                                    },
                                    fontWeight = FontWeight.Medium,
                                    color = AgriTextPrimary
                                )
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "• শেষ ছাঁটাই: গত ডিসেম্বর (হালকা ছাঁটাই)"
                                        AppLanguage.HINDI -> "• पिछली छंटाई: पिछले दिसंबर में (हल्की छंटाई)"
                                        AppLanguage.ENGLISH -> "• Last Pruning: Last December (Light Prune)"
                                    },
                                    fontWeight = FontWeight.Medium,
                                    color = AgriTextPrimary
                                )
                            }
                        }

                        Button(
                            enabled = !isSaving,
                            onClick = {
                                coroutineScope.launch {
                                    isSaving = true
                                    try {
                                        val garden = TeaGarden(
                                            farmerId = userId,
                                            gardenName = gardenName,
                                            location = location,
                                            totalLand = landArea.toLocalizedDoubleOrNull()?.takeIf { it > 0.0 } ?: 5.0,
                                            landUnit = landUnit.ifBlank { "bigha" }
                                        )
                                        repository.saveTeaGarden(garden)
                                        val savedMsg = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "বাগান তথ্য সংরক্ষিত হয়েছে।"
                                            AppLanguage.HINDI -> "बागान विवरण सहेजा गया।"
                                            AppLanguage.ENGLISH -> "Tea garden details saved."
                                        }
                                        Toast.makeText(context, savedMsg, Toast.LENGTH_SHORT).show()
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (_: Exception) {
                                        val errMsg = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "সংরক্ষণে সমস্যা হয়েছে। অনুগ্রহ করে সাইন-ইন যাচাই করুন।"
                                            AppLanguage.HINDI -> "सहेजने में त्रुटि। कृपया साइन-इन जांचें।"
                                            AppLanguage.ENGLISH -> "Could not save garden details. Please sign in first."
                                        }
                                        Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("save_tea_garden_btn"),
                            shape = RoundedCornerShape(12.dp),
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
                                    AppLanguage.BENGALI -> "বাগান তথ্য সংরক্ষণ করুন"
                                    AppLanguage.HINDI -> "बागान विवरण सहेजें"
                                    AppLanguage.ENGLISH -> "Save Garden Details"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
                1 -> {
                    // Cultivation & Pruning Guide
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        teaMaster.steps.forEach { step ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "ধাপ: ${step.getTitle(currentLanguage)}"
                                                AppLanguage.HINDI -> "चरण: ${step.getTitle(currentLanguage)}"
                                                AppLanguage.ENGLISH -> "Step: ${step.getTitle(currentLanguage)}"
                                            },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "দিন ${step.dayNumber}"
                                                AppLanguage.HINDI -> "दिन ${step.dayNumber}"
                                                AppLanguage.ENGLISH -> "Day ${step.dayNumber}"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = step.getDescription(currentLanguage),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = AgriTextPrimary,
                                        lineHeight = 21.sp
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Tea Spray & Fertilizer Calculator Quick Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "চা স্প্রে ও সার গণক"
                                        AppLanguage.HINDI -> "चाय स्प्रे व उर्वरक कैलकुलेटर"
                                        AppLanguage.ENGLISH -> "Tea Spray & Fertilizer Calculator"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "চা বোর্ডের ট্রেনিং ও CIBRC-এর অনুমোদিত তালিকা অনুযায়ী জমি অনুপাতে সঠিক ডোজ।"
                                        AppLanguage.HINDI -> "टी बोर्ड प्रशिक्षण व CIBRC अनुमोदित सूची के अनुसार भूमि अनुपात में सटीक खुराक।"
                                        AppLanguage.ENGLISH -> "Accurate land-ratio dosage per Tea Board PPC and CIBRC approved guidelines."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // Red spider mite & Mosquito bug verified items
                        val teaTreatments = VerifiedTreatmentDatabase.treatments.filter { it.cropName == "Tea" }
                        teaTreatments.forEach { t ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = "🐛 ${t.getProblemName(currentLanguage)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                    val ingText = t.getActiveIngredient(currentLanguage) ?: when (currentLanguage) {
                                        AppLanguage.BENGALI -> "জৈব সমাধান"
                                        AppLanguage.HINDI -> "जैविक समाधान"
                                        AppLanguage.ENGLISH -> "Organic solution"
                                    }
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "সক্রিয় উপাদান: $ingText"
                                            AppLanguage.HINDI -> "सक्रिय तत्व: $ingText"
                                            AppLanguage.ENGLISH -> "Active Ingredient: $ingText"
                                        },
                                        fontWeight = FontWeight.SemiBold,
                                        color = AgriTextPrimary
                                    )
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "অনুমোদিত ডোজ: ${t.dosePerHectareMlOrGm} ${t.getDoseUnit(currentLanguage)}/হেক্টর (জমি অনুযায়ী স্বয়ংক্রিয় গণনা)"
                                            AppLanguage.HINDI -> "अनुमोदित खुराक: ${t.dosePerHectareMlOrGm} ${t.getDoseUnit(currentLanguage)}/हेक्टेयर (भूमि के अनुसार स्वचालित गणना)"
                                            AppLanguage.ENGLISH -> "Approved Dose: ${t.dosePerHectareMlOrGm} ${t.getDoseUnit(currentLanguage)}/hectare (auto-calculated by land area)"
                                        },
                                        fontWeight = FontWeight.Medium,
                                        color = AgriTextSecondary
                                    )
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "প্রতিরোধ গ্রুপ: ${t.getResistanceGroup(currentLanguage)}"
                                            AppLanguage.HINDI -> "प्रतिरोध समूह: ${t.getResistanceGroup(currentLanguage)}"
                                            AppLanguage.ENGLISH -> "Resistance Group: ${t.getResistanceGroup(currentLanguage)}"
                                        },
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "পাতা তোলার বন্ধ সময় (PHI): ${t.preHarvestIntervalDays} দিন"
                                            AppLanguage.HINDI -> "पत्तियां तोड़ने की प्रतीक्षा अवधि (PHI): ${t.preHarvestIntervalDays} दिन"
                                            AppLanguage.ENGLISH -> "Plucking Wait Period (PHI): ${t.preHarvestIntervalDays} days"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onOpenSprayCalculator,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("open_tea_calc_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AgriGreenPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Calculate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "জমি মেপে চা স্প্রে হিসাব খুলুন"
                                    AppLanguage.HINDI -> "भूमि मापकर चाय स्प्रे कैलकुलेटर खोलें"
                                    AppLanguage.ENGLISH -> "Open Tea Spray Calculator by Land Size"
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
        }
    }
}
