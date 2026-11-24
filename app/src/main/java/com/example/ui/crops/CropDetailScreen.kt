package com.example.ui.crops

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.CropDomainAdvisor
import com.example.data.datasource.DiagnosticKnowledgeDatabase
import com.example.data.datasource.VerifiedTreatmentDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.CropRegion
import com.example.data.model.DiagnosticQuestionOption
import com.example.data.model.MasterCrop
import com.example.ui.medicine.FarmerMedicineCard
import com.example.ui.medicine.MedicineDetailDialog
import com.example.ui.medicine.MedicineLandCalculatorDialog
import com.example.ui.medicine.NearbySellersDialog
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.ui.theme.AgriWarningAmber
import com.example.util.isCompactNarrowScreen

enum class CropDetailSection(val labelBn: String, val labelHi: String, val labelEn: String, val iconEmoji: String) {
    CULTIVATION("চাষের নিয়ম", "खेती के नियम", "Cultivation", "🌱"),
    IRRIGATION("জল ও সেচ", "जल व सिंचाई", "Irrigation", "💧"),
    NUTRITION("সার ও পুষ্টি", "उर्वरक व पोषण", "Nutrition", "🌿"),
    PROBLEMS("রোগ-পোকা ও সমস্যা", "रोग-कीट व समस्याएं", "Pest & Disease", "🐛"),
    FLOWERING_FRUITING("ফুল ও ফল", "फूल व फल", "Flowering & Fruit", "🌸"),
    MEDICINE("অনুমোদিত ওষুধ", "अनुशंसित दवाएं", "Medicines", "💊"),
    CALENDAR("ক্যালেন্ডার", "कैलेंडर", "Calendar", "📅"),
    HARVEST_MARKET("তোলা, খরচ ও FAQ", "कटाई, लागत व FAQ", "Harvest & Market", "🧺");

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> labelBn
        AppLanguage.HINDI -> labelHi
        AppLanguage.ENGLISH -> labelEn
    }

    fun getDynamicLabel(crop: MasterCrop, lang: AppLanguage): String = when (this) {
        FLOWERING_FRUITING -> when {
            CropDomainAdvisor.isFloriculture(crop) -> when (lang) {
                AppLanguage.BENGALI -> "🌸 ফুল ও ডাঁটার যত্ন"
                AppLanguage.HINDI -> "🌸 फूल व डंठल देखभाल"
                AppLanguage.ENGLISH -> "🌸 Floriculture Care"
            }
            CropDomainAdvisor.isIndoorOrNursery(crop) -> when (lang) {
                AppLanguage.BENGALI -> "🪴 পাতা ও শেপিং"
                AppLanguage.HINDI -> "🪴 पत्तियां व शेपिंग"
                AppLanguage.ENGLISH -> "🪴 Foliage & Shaping"
            }
            CropDomainAdvisor.isAgroforestryOrGreenManure(crop) -> when (lang) {
                AppLanguage.BENGALI -> "🌳 ছাঁটাই ও জৈবভর"
                AppLanguage.HINDI -> "🌳 छंटाई व बायोमास"
                AppLanguage.ENGLISH -> "🌳 Pruning & Biomass"
            }
            else -> "${iconEmoji} ${getLabel(lang)}"
        }
        HARVEST_MARKET -> when {
            CropDomainAdvisor.isIndoorOrNursery(crop) -> when (lang) {
                AppLanguage.BENGALI -> "🪴 প্রস্তুত, খরচ ও FAQ"
                AppLanguage.HINDI -> "🪴 तैयारी, लागत व FAQ"
                AppLanguage.ENGLISH -> "🪴 Prep, Cost & FAQ"
            }
            else -> "${iconEmoji} ${getLabel(lang)}"
        }
        else -> "${iconEmoji} ${getLabel(lang)}"
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CropDetailScreen(
    crop: MasterCrop,
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onOpenCalculator: (String) -> Unit,
    onDiagnosePhoto: (String) -> Unit = {},
    onCheckMarketPrice: (String) -> Unit = {}
) {
    var selectedSection by remember { mutableStateOf(CropDetailSection.CULTIVATION) }
    val galleryIds = remember(crop.cropId) { crop.getResolvedGalleryResIds() }
    var selectedGalleryResId by remember(crop.cropId) { mutableStateOf<Int?>(null) }
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
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("crop_detail_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${crop.iconEmoji} ${crop.getName(currentLanguage)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 26.sp
                        )
                        Text(
                            text = "${crop.nameEn} • ${crop.scientificName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Horizontal Section Switcher Chips (dynamically named by plant type)
            ScrollableTabRow(
                selectedTabIndex = selectedSection.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                CropDetailSection.values().forEach { section ->
                    val isSelected = selectedSection == section
                    Tab(
                        selected = isSelected,
                        onClick = { selectedSection = section },
                        text = {
                            Text(
                                text = section.getDynamicLabel(crop, currentLanguage),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else AgriTextSecondary,
                                fontSize = 14.sp,
                                lineHeight = 18.sp
                            )
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 760.dp)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Hero Photo or Botanical Illustration Fallback
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .testTag("crop_detail_hero_image"),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        CropHeroVisualBanner(
                            crop = crop,
                            lang = currentLanguage,
                            overridePhotoResId = selectedGalleryResId,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (galleryIds.size > 1) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(galleryIds) { idx, resId ->
                                val activeRes = selectedGalleryResId ?: galleryIds.firstOrNull()
                                val isActive = activeRes == resId
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        width = if (isActive) 2.dp else 1.dp,
                                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clickable { selectedGalleryResId = resId }
                                        .testTag("crop_gallery_thumb_$idx")
                                ) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = "${crop.getName(currentLanguage)} photo ${idx + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    // Overview Summary Card with Trilingual Names, Group, Season & Region Chips
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = crop.iconEmoji, fontSize = 30.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = crop.getName(currentLanguage),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        lineHeight = 26.sp
                                    )
                                    Text(
                                        text = "${crop.getMasterGroup().getDisplayName(currentLanguage)} • ${crop.category.getDisplayName(currentLanguage)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f),
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            Text(
                                text = crop.getSummary(currentLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = 22.sp
                            )

                            val localNamesLine = "EN: ${crop.nameEn} • HI: ${crop.nameHi} • BN: ${crop.nameBn}"
                            Text(
                                text = localNamesLine,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                lineHeight = 17.sp
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                crop.getResolvedSeasons().forEach { s ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
                                    ) {
                                        Text(
                                            text = "🗓️ ${s.getDisplayName(currentLanguage)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                crop.getResolvedRegions().forEach { r ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (r == CropRegion.SILIGURI_NORTH_BENGAL) {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
                                        }
                                    ) {
                                        Text(
                                            text = "📍 ${r.getDisplayName(currentLanguage)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (r == CropRegion.SILIGURI_NORTH_BENGAL) {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Render Selected Section
                    when (selectedSection) {
                        CropDetailSection.CULTIVATION -> CultivationRulesSection(crop, currentLanguage)
                        CropDetailSection.IRRIGATION -> IrrigationSection(crop, currentLanguage)
                        CropDetailSection.NUTRITION -> NutritionSection(crop, currentLanguage, onOpenCalculator)
                        CropDetailSection.PROBLEMS -> PestDiseaseProblemSection(crop, currentLanguage, onOpenCalculator, onDiagnosePhoto)
                        CropDetailSection.FLOWERING_FRUITING -> FloweringFruitingSection(crop, currentLanguage, onOpenCalculator)
                        CropDetailSection.MEDICINE -> VerifiedMedicineSection(crop, currentLanguage, onOpenCalculator)
                        CropDetailSection.CALENDAR -> CropCalendarSection(crop, currentLanguage)
                        CropDetailSection.HARVEST_MARKET -> HarvestMarketSection(crop, currentLanguage, onCheckMarketPrice)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bottom Quick Action Buttons (responsive on <=360dp and large font sizes)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        maxItemsInEachRow = if (isNarrow) 1 else 2
                    ) {
                        Button(
                            onClick = { onOpenCalculator(crop.getName(currentLanguage)) },
                            modifier = Modifier
                                .then(if (isNarrow) Modifier.fillMaxWidth() else Modifier.weight(1f))
                                .heightIn(min = 50.dp)
                                .testTag("crop_open_calculator_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AgriGreenPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Calculate, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "সার ও ওষুধ ডোজ"
                                    AppLanguage.HINDI -> "खाद व दवा खुराक"
                                    AppLanguage.ENGLISH -> "Dosage Calc"
                                },
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { onDiagnosePhoto(crop.getName(currentLanguage)) },
                            modifier = Modifier
                                .then(if (isNarrow) Modifier.fillMaxWidth() else Modifier.weight(1f))
                                .heightIn(min = 50.dp)
                                .testTag("crop_photo_diagnose_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ছবি দিয়ে পরীক্ষা"
                                    AppLanguage.HINDI -> "फोटो से जांच"
                                    AppLanguage.ENGLISH -> "Photo Check"
                                },
                                color = MaterialTheme.colorScheme.primary,
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

@Composable
private fun CultivationRulesSection(crop: MasterCrop, lang: AppLanguage) {
    val isNursery = CropDomainAdvisor.isIndoorOrNursery(crop)
    val isTreeOrFruit = CropDomainAdvisor.isOrchardTree(crop) || CropDomainAdvisor.isAgroforestryOrGreenManure(crop)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = when {
                    isNursery -> when (lang) {
                        AppLanguage.BENGALI -> "🪴 নার্সারি, টব ও পরিচর্যা নির্দেশিকা:"
                        AppLanguage.HINDI -> "🪴 नर्सरी, गमला व देखभाल नियम:"
                        AppLanguage.ENGLISH -> "🪴 Nursery, Potting & Care Guide:"
                    }
                    CropDomainAdvisor.isFloriculture(crop) -> when (lang) {
                        AppLanguage.BENGALI -> "🌸 বাণিজ্যিক ফুল চাষ প্রণালী:"
                        AppLanguage.HINDI -> "🌸 व्यावसायिक फूल खेती विधि:"
                        AppLanguage.ENGLISH -> "🌸 Floriculture Cultivation Guide:"
                    }
                    else -> when (lang) {
                        AppLanguage.BENGALI -> "🌱 সম্পূর্ণ চাষ প্রণালী ও নিয়মাবলী:"
                        AppLanguage.HINDI -> "🌱 संपूर्ण खेती विधि एवं नियम:"
                        AppLanguage.ENGLISH -> "🌱 Complete Cultivation Guide & Practices:"
                    }
                },
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                lineHeight = 23.sp,
                color = MaterialTheme.colorScheme.primary
            )

            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> "🔬 বৈজ্ঞানিক নাম ও শ্রেণি"
                    AppLanguage.HINDI -> "🔬 वैज्ञानिक नाम व श्रेणी"
                    AppLanguage.ENGLISH -> "🔬 Scientific Name & Classification"
                },
                value = buildString {
                    append(crop.scientificName)
                    append(" • ")
                    append(crop.getMasterGroup().getDisplayName(lang))
                    if (crop.subCategory.isNotBlank()) {
                        append(" (${crop.subCategory})")
                    }
                }
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> "📍 আঞ্চলিক উপযোগিতা (শিলিগুড়ি/WB/ভারত)"
                    AppLanguage.HINDI -> "📍 क्षेत्रीय उपयुक्तता (सिलीगुड़ी/WB/भारत)"
                    AppLanguage.ENGLISH -> "📍 Regional Suitability (Siliguri/WB/India)"
                },
                value = crop.getRegionalSuitability(lang)
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> "📅 উপযুক্ত সময় / ঋতু"
                    AppLanguage.HINDI -> "📅 उपयुक्त समय / मौसम"
                    AppLanguage.ENGLISH -> "📅 Best Season"
                },
                value = crop.getBestSeason(lang)
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> "🌤️ অনুকূল আবহাওয়া ও তাপমাত্রা"
                    AppLanguage.HINDI -> "🌤️ अनुकूल जलवायु व तापमान"
                    AppLanguage.ENGLISH -> "🌤️ Suitable Climate & Light"
                },
                value = crop.getSuitableClimate(lang)
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> if (isNursery) "🪴 উপযোগী মাটি / পটিং মিডিয়া" else "🧱 উপযোগী মাটি"
                    AppLanguage.HINDI -> if (isNursery) "🪴 उपयुक्त मिट्टी / पॉटिंग मिक्स" else "🧱 उपयुक्त मिट्टी"
                    AppLanguage.ENGLISH -> if (isNursery) "🪴 Soil / Potting Media" else "🧱 Soil Requirement"
                },
                value = crop.getSoil(lang)
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> "🧪 মাটির pH মাত্রা"
                    AppLanguage.HINDI -> "🧪 मिट्टी का pH स्तर"
                    AppLanguage.ENGLISH -> "🧪 Recommended Soil pH"
                },
                value = crop.getRecommendedPh(lang)
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> when {
                        isNursery -> "🪴 পাত্র ও মিডিয়া প্রস্তুতি"
                        isTreeOrFruit -> "🕳️ গর্ত ও বাগান প্রস্তুতি"
                        else -> "🚜 জমি প্রস্তুতি"
                    }
                    AppLanguage.HINDI -> when {
                        isNursery -> "🪴 गमला व मीडिया तैयारी"
                        isTreeOrFruit -> "🕳️ गड्ढा व बागान तैयारी"
                        else -> "🚜 खेत की तैयारी"
                    }
                    AppLanguage.ENGLISH -> when {
                        isNursery -> "🪴 Container & Media Prep"
                        isTreeOrFruit -> "🕳️ Pit & Orchard Prep"
                        else -> "🚜 Land Preparation"
                    }
                },
                value = crop.getLandPrep(lang)
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> "🌾 উন্নত জাত ও প্রজাতি"
                    AppLanguage.HINDI -> "🌾 अनुशंसित किस्में व प्रजातियां"
                    AppLanguage.ENGLISH -> "🌾 Recommended Varieties / Cultivars"
                },
                value = crop.getVarieties(lang)
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> if (isNursery || isTreeOrFruit) "🌱 চারা / কলমের হার" else "🌾 বীজ / চারার হার"
                    AppLanguage.HINDI -> if (isNursery || isTreeOrFruit) "🌱 पौध / कलम की मात्रा" else "🌾 बीज / पौध दर"
                    AppLanguage.ENGLISH -> if (isNursery || isTreeOrFruit) "🌱 Sapling / Propagation Rate" else "🌾 Seed / Seedling Rate"
                },
                value = crop.getSeedRate(lang)
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> "🌿 শোধন ও সুরক্ষা পদ্ধতি"
                    AppLanguage.HINDI -> "🌿 बीज / कलम उपचार विधि"
                    AppLanguage.ENGLISH -> "🌿 Seed / Cutting Treatment"
                },
                value = crop.getSeedTreatment(lang)
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> "🌱 বপন ও বংশবিস্তার পদ্ধতি"
                    AppLanguage.HINDI -> "🌱 बुवाई व प्रवर्धन विधि"
                    AppLanguage.ENGLISH -> "🌱 Sowing & Propagation Method"
                },
                value = crop.getSowingMethod(lang)
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> "📏 রোপণ দূরত্ব ও ঘনত্ব"
                    AppLanguage.HINDI -> "📏 पौध से पौध की दूरी"
                    AppLanguage.ENGLISH -> "📏 Spacing & Plant Density"
                },
                value = buildString {
                    append(crop.getSpacing(lang))
                    crop.plantDensityPerAcre?.let { density ->
                        val suffix = when (lang) {
                            AppLanguage.BENGALI -> " • আনুমানিক $density গাছ/একর"
                            AppLanguage.HINDI -> " • लगभग $density पौधे/एकड़"
                            AppLanguage.ENGLISH -> " • ~$density plants/acre"
                        }
                        append(suffix)
                    }
                }
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> "🏡 বীজতলা ও নার্সারি"
                    AppLanguage.HINDI -> "🏡 नर्सरी तैयारी"
                    AppLanguage.ENGLISH -> "🏡 Nursery Method"
                },
                value = crop.getNurseryMethod(lang)
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> "🌱 রোপণ / প্রতিস্থাপন পদ্ধতি"
                    AppLanguage.HINDI -> "🌱 रोपाई / रिपोर्टिंग विधि"
                    AppLanguage.ENGLISH -> "🌱 Transplanting / Potting Method"
                },
                value = crop.getTransplanting(lang)
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> "🌿 আগাছা ও অন্তর্বর্তী পরিচর্যা"
                    AppLanguage.HINDI -> "🌿 खरपतवार व अंतर-कृषि देखभाल"
                    AppLanguage.ENGLISH -> "🌿 Weed Management & Intercultural Care"
                },
                value = crop.getWeedManagement(lang)
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> "📦 গড় ফলনকাল / প্রস্তুতি সময়"
                    AppLanguage.HINDI -> "📦 औसत अवधि"
                    AppLanguage.ENGLISH -> "📦 Crop Duration"
                },
                value = when (lang) {
                    AppLanguage.BENGALI -> "${crop.harvestDays} দিন"
                    AppLanguage.HINDI -> "${crop.harvestDays} दिन"
                    AppLanguage.ENGLISH -> "${crop.harvestDays} days"
                }
            )
            FactRow(
                label = when (lang) {
                    AppLanguage.BENGALI -> "⚖️ প্রত্যাশিত ফলন / আউটপুট"
                    AppLanguage.HINDI -> "⚖️ अनुमानित उपज / उत्पादन"
                    AppLanguage.ENGLISH -> "⚖️ Expected Yield"
                },
                value = crop.getExpectedYield(lang)
            )
        }
    }
}

@Composable
private fun IrrigationSection(crop: MasterCrop, lang: AppLanguage) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> "💧 জল ও সেচ ব্যবস্থাপনা:"
                        AppLanguage.HINDI -> "💧 जल व सिंचाई प्रबंधन:"
                        AppLanguage.ENGLISH -> "💧 Water & Irrigation Management:"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = AgriTextPrimary
                )
            }

            Text(
                text = crop.getIrrigationSchedule(lang),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AgriTextPrimary,
                lineHeight = 22.sp
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                val isNursery = CropDomainAdvisor.isIndoorOrNursery(crop)
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> if (isNursery) "⚠️ টব ও নার্সারি জল দেওয়ার নিয়ম:" else "⚠️ সেচের সোনালী নিয়ম:"
                            AppLanguage.HINDI -> if (isNursery) "⚠️ गमले व नर्सरी सिंचाई के नियम:" else "⚠️ सिंचाई के सुनहरे नियम:"
                            AppLanguage.ENGLISH -> if (isNursery) "⚠️ Potted & Nursery Watering Rules:" else "⚠️ Golden Rules of Irrigation:"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isNursery) {
                            when (lang) {
                                AppLanguage.BENGALI -> "• টবের নিচে অবশ্যই জল নিকাশী ছিদ্র রাখুন; অতিরিক্ত জলে শিকড় পচে যায়।"
                                AppLanguage.HINDI -> "• गमले में जल निकासी के छेद अवश्य रखें; अतिरिक्त पानी से जड़ सड़ जाती है।"
                                AppLanguage.ENGLISH -> "• Ensure open pot drainage holes at all times; standing water causes root rot."
                            }
                        } else {
                            when (lang) {
                                AppLanguage.BENGALI -> "• জমিতে কখনোই জল জমতে দেবেন না; গোড়ায় জল জমলে শিকড় পচে গাছ মারা যায়।"
                                AppLanguage.HINDI -> "• खेत में कभी पानी जमा न होने दें; जड़ में जलभराव से पौधा सड़कर मर सकता है।"
                                AppLanguage.ENGLISH -> "• Never allow standing water around stems; waterlogging causes root rot."
                            }
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = if (isNursery) {
                            when (lang) {
                                AppLanguage.BENGALI -> "• আঙুল দিয়ে মাটির উপরের ১-২ ইঞ্চি শুষ্ক মনে হলেই কেবল হালকা জল দিন।"
                                AppLanguage.HINDI -> "• ऊपर की 1-2 इंच मिट्टी सूखी महसूस होने पर ही हल्का पानी दें।"
                                AppLanguage.ENGLISH -> "• Check top 1–2 inches of potting mix and water thoroughly only when dry to touch."
                            }
                        } else {
                            when (lang) {
                                AppLanguage.BENGALI -> "• কুঁড়ি, ফুল ও ফল ধরার সময় অতিরিক্ত খরা বা অনিয়মিত সেচ দিলে ঝরে যাওয়ার প্রবণতা বাড়ে।"
                                AppLanguage.HINDI -> "• कली, फूल व फल बनते समय सूखा या अनियमित सिंचाई करने से झड़ने की समस्या बढ़ती है।"
                                AppLanguage.ENGLISH -> "• Avoid drought stress or irregular watering during budding, flowering, and set."
                            }
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> "• বিকেলে বা ভোরের শান্ত সময়ে হালকা জল/সেচ দিন।"
                            AppLanguage.HINDI -> "• सुबह या शाम के ठंडे समय में हल्की सिंचाई करें।"
                            AppLanguage.ENGLISH -> "• Water during cool early morning or late afternoon hours."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionSection(crop: MasterCrop, lang: AppLanguage, onOpenCalculator: (String) -> Unit) {
    var showAllNutrients by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> "🌿 সার ও জৈব পুষ্টি সূচি:"
                        AppLanguage.HINDI -> "🌿 उर्वरक व जैविक पोषण अनुसूची:"
                        AppLanguage.ENGLISH -> "🌿 Organic & Chemical Fertilizer Schedule:"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    lineHeight = 23.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                FactRow(
                    label = when (lang) {
                        AppLanguage.BENGALI -> "💩 জৈব সার"
                        AppLanguage.HINDI -> "💩 जैविक खाद"
                        AppLanguage.ENGLISH -> "💩 Organic Manure"
                    },
                    value = crop.getOrganicManure(lang)
                )
                FactRow(
                    label = when (lang) {
                        AppLanguage.BENGALI -> "⚗️ রাসায়নিক সার সূচি"
                        AppLanguage.HINDI -> "⚗️ रासायनिक उर्वरक अनुसूची"
                        AppLanguage.ENGLISH -> "⚗️ Chemical Fertilizer Schedule"
                    },
                    value = crop.getFertilizerSchedule(lang)
                )
                val cropDefs = crop.getCommonDeficienciesList(lang)
                if (cropDefs.isNotEmpty()) {
                    FactRow(
                        label = when (lang) {
                            AppLanguage.BENGALI -> "⚠️ এই ফসলে সাধারণ পুষ্টির ঘাটতি"
                            AppLanguage.HINDI -> "⚠️ इस फसल में मुख्य पोषक तत्वों की कमी"
                            AppLanguage.ENGLISH -> "⚠️ Common Nutrient Deficiencies in Crop"
                        },
                        value = cropDefs.joinToString(" • ")
                    )
                }
            }
        }

        Text(
            text = when (lang) {
                AppLanguage.BENGALI -> "📈 ৫টি ধাপে সার প্রয়োগ ক্যালেন্ডার:"
                AppLanguage.HINDI -> "📈 5 चरणों का उर्वरक कैलेंडर:"
                AppLanguage.ENGLISH -> "📈 5-Stage Fertilizer Application Calendar:"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = AgriTextPrimary
        )

        DiagnosticKnowledgeDatabase.fertilizerStages.forEach { stage ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = stage.getStageName(lang),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stage.getTiming(lang), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                    }
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> "উদ্দেশ্য: ${stage.getObjective(lang)}"
                            AppLanguage.HINDI -> "उद्देश्य: ${stage.getObjective(lang)}"
                            AppLanguage.ENGLISH -> "Objective: ${stage.getObjective(lang)}"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AgriTextSecondary
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> "প্রয়োগ: ${stage.getRecommendedFertilizers(lang)}"
                            AppLanguage.HINDI -> "मात्रा: ${stage.getRecommendedFertilizers(lang)}"
                            AppLanguage.ENGLISH -> "Apply: ${stage.getRecommendedFertilizers(lang)}"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> "সতর্কতা: ${stage.getCaution(lang)}"
                            AppLanguage.HINDI -> "सावधानी: ${stage.getCaution(lang)}"
                            AppLanguage.ENGLISH -> "Caution: ${stage.getCaution(lang)}"
                        },
                        fontSize = 12.sp,
                        color = AgriWarningAmber
                    )
                }
            }
        }

        // 11 Nutrients deficiency lookup
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> "🧪 ১১টি পুষ্টি উপাদানের অভাব লক্ষণ:"
                            AppLanguage.HINDI -> "🧪 11 प्रमुख पोषक तत्वों की कमी के लक्षण:"
                            AppLanguage.ENGLISH -> "🧪 11 Nutrient Deficiency Guide:"
                        },
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clickable { showAllNutrients = !showAllNutrients }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (lang) {
                                AppLanguage.BENGALI -> if (showAllNutrients) "সংক্ষিপ্ত করুন ▲" else "সবগুলো দেখুন ▼"
                                AppLanguage.HINDI -> if (showAllNutrients) "कम दिखाएं ▲" else "सभी देखें ▼"
                                AppLanguage.ENGLISH -> if (showAllNutrients) "Show Less ▲" else "View All ▼"
                            },
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val nutrientsToShow = if (showAllNutrients) DiagnosticKnowledgeDatabase.nutrientDeficiencies else DiagnosticKnowledgeDatabase.nutrientDeficiencies.take(3)
                nutrientsToShow.forEach { n ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.BENGALI -> "${n.getNutrientName(lang)} • স্থান: ${n.getAffectedPart(lang)}"
                                    AppLanguage.HINDI -> "${n.getNutrientName(lang)} • स्थान: ${n.getAffectedPart(lang)}"
                                    AppLanguage.ENGLISH -> "${n.getNutrientName(lang)} • Part: ${n.getAffectedPart(lang)}"
                                },
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = when (lang) {
                                    AppLanguage.BENGALI -> "লক্ষণ: ${n.getSymptoms(lang)}"
                                    AppLanguage.HINDI -> "लक्षण: ${n.getSymptoms(lang)}"
                                    AppLanguage.ENGLISH -> "Symptoms: ${n.getSymptoms(lang)}"
                                },
                                fontSize = 13.sp,
                                color = AgriTextPrimary
                            )
                            Text(
                                text = when (lang) {
                                    AppLanguage.BENGALI -> "প্রতিকার: ${n.getSolution(lang)}"
                                    AppLanguage.HINDI -> "उपचार: ${n.getSolution(lang)}"
                                    AppLanguage.ENGLISH -> "Remedy: ${n.getSolution(lang)}"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = AgriTextPrimary
                            )
                            Text(
                                text = when (lang) {
                                    AppLanguage.BENGALI -> "জৈব সমাধান: ${n.getOrganicRemedy(lang)}"
                                    AppLanguage.HINDI -> "जैविक उपाय: ${n.getOrganicRemedy(lang)}"
                                    AppLanguage.ENGLISH -> "Organic Option: ${n.getOrganicRemedy(lang)}"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PestDiseaseProblemSection(
    crop: MasterCrop,
    lang: AppLanguage,
    onOpenCalculator: (String) -> Unit,
    onDiagnosePhoto: (String) -> Unit
) {
    var selectedTreeId by remember { mutableStateOf<String?>(null) }
    var selectedOption by remember { mutableStateOf<DiagnosticQuestionOption?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Explicit KVK Safety Verification Banner
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> "নিরাপত্তা নির্দেশিকা: যেকোনো রোগ বা পোকা নির্ণয় ও রাসায়নিক কীটনাশক স্প্রে করার আগে আপনার এলাকার KVK বা কৃষি আধিকারিকের থেকে ডোজ ও অপেক্ষাকাল (PHI) নিশ্চিত করুন।"
                        AppLanguage.HINDI -> "सुरक्षा निर्देश: किसी भी रोग/कीट के लिए रासायनिक छिड़काव से पहले अपने नजदीकी KVK या कृषि अधिकारी से खुराक व प्रतीक्षा अवधि (PHI) की पुष्टि करें।"
                        AppLanguage.ENGLISH -> "Safety Advisory: Always confirm field diagnosis, approved chemical dosage, and pre-harvest interval (PHI) with your local KVK or Agriculture Officer."
                    },
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Crop-specific Pests & Diseases overview card
        val pests = crop.getCommonPestsList(lang)
        val diseases = crop.getCommonDiseasesList(lang)
        if (pests.isNotEmpty() || diseases.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> "🛡️ এই ফসলের প্রধান পোকা ও রোগবালাই:"
                            AppLanguage.HINDI -> "🛡️ इस फसल के प्रमुख कीट एवं रोग:"
                            AppLanguage.ENGLISH -> "🛡️ Major Pests & Diseases of This Crop:"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (pests.isNotEmpty()) {
                        FactRow(
                            label = when (lang) {
                                AppLanguage.BENGALI -> "🐛 প্রধান ক্ষতিকর পোকা"
                                AppLanguage.HINDI -> "🐛 प्रमुख हानिकारक कीट"
                                AppLanguage.ENGLISH -> "🐛 Common Pests"
                            },
                            value = pests.joinToString(" • ")
                        )
                    }
                    if (diseases.isNotEmpty()) {
                        FactRow(
                            label = when (lang) {
                                AppLanguage.BENGALI -> "🦠 প্রধান রোগসমূহ"
                                AppLanguage.HINDI -> "🦠 प्रमुख रोग"
                                AppLanguage.ENGLISH -> "🦠 Common Diseases"
                            },
                            value = diseases.joinToString(" • ")
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> "🐛 সমস্যা বা লক্ষণ নির্বাচন করুন (১-ট্যাপ নির্ণয়):"
                        AppLanguage.HINDI -> "🐛 समस्या या लक्षण चुनें (तुरंत पहचान):"
                        AppLanguage.ENGLISH -> "🐛 Select Symptom or Problem (1-Tap Diagnosis):"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> "আপনার ফসলে কোন লক্ষণটি দেখছেন? স্পর্শ করুন:"
                        AppLanguage.HINDI -> "अपनी फसल में कौन सा लक्षण दिख रहा है? टैप करें:"
                        AppLanguage.ENGLISH -> "Which symptom do you observe on your crop? Tap below:"
                    },
                    fontSize = 13.sp,
                    color = AgriTextSecondary
                )

                // Diagnostic Symptom Trees
                DiagnosticKnowledgeDatabase.symptomTrees.forEach { tree ->
                    val isExpanded = selectedTreeId == tree.symptomId
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable {
                                selectedTreeId = if (isExpanded) null else tree.symptomId
                                selectedOption = null
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = tree.iconEmoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = tree.getTitle(lang),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else AgriTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (isExpanded) "▲" else "▼",
                                    color = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else AgriTextSecondary
                                )
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "❓ ${tree.getInitialQuestion(lang)}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                tree.options.forEach { opt ->
                                    val isOptSelected = selectedOption == opt
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .padding(vertical = 4.dp)
                                            .border(
                                                width = if (isOptSelected) 2.dp else 1.dp,
                                                color = if (isOptSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedOption = opt }
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = opt.getOptionText(lang),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isOptSelected) MaterialTheme.colorScheme.primary else AgriTextPrimary
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

        // Show Full Diagnosis when option is selected
        selectedOption?.let { opt ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> "🎯 সম্ভাব্য রোগ ও সমাধান:"
                            AppLanguage.HINDI -> "🎯 संभावित रोग एवं समाधान:"
                            AppLanguage.ENGLISH -> "🎯 Likely Diagnosis & Solution:"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> "রোগ / কারণ: ${opt.getDiagnosis(lang)}"
                            AppLanguage.HINDI -> "रोग / समस्या: ${opt.getDiagnosis(lang)}"
                            AppLanguage.ENGLISH -> "Diagnosis: ${opt.getDiagnosis(lang)}"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> "মূল কারণ: ${opt.getPrimaryCause(lang)}"
                            AppLanguage.HINDI -> "मुख्य कारण: ${opt.getPrimaryCause(lang)}"
                            AppLanguage.ENGLISH -> "Primary Cause: ${opt.getPrimaryCause(lang)}"
                        },
                        fontSize = 13.sp,
                        color = AgriTextPrimary
                    )
                    FactRow(
                        label = when (lang) {
                            AppLanguage.BENGALI -> "১. প্রথম করণীয় (পরিচ্ছন্নতা)"
                            AppLanguage.HINDI -> "1. पहला कदम (खेत की स्वच्छता)"
                            AppLanguage.ENGLISH -> "1. First Action (Field Hygiene)"
                        },
                        value = opt.getFirstAction(lang)
                    )
                    FactRow(
                        label = when (lang) {
                            AppLanguage.BENGALI -> "২. জৈব সমাধান (Bio-Control)"
                            AppLanguage.HINDI -> "2. जैविक उपचार (Bio-Control)"
                            AppLanguage.ENGLISH -> "2. Biological / Organic Option"
                        },
                        value = opt.getBiologicalAction(lang)
                    )

                    if (opt.chemicalActiveIngredient != null) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = when (lang) {
                                        AppLanguage.BENGALI -> "💊 পরীক্ষিত ওষুধ (সক্রিয় উপাদান):"
                                        AppLanguage.HINDI -> "💊 अनुशंसित दवा (सक्रिय घटक):"
                                        AppLanguage.ENGLISH -> "💊 Verified Medicine (Active Ingredient):"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = AgriWarningAmber
                                )
                                Text(
                                    text = when (lang) {
                                        AppLanguage.BENGALI -> "• উপাদান: ${opt.chemicalActiveIngredient}"
                                        AppLanguage.HINDI -> "• घटक: ${opt.chemicalActiveIngredient}"
                                        AppLanguage.ENGLISH -> "• Ingredient: ${opt.chemicalActiveIngredient}"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = AgriTextPrimary
                                )
                                Text(
                                    text = when (lang) {
                                        AppLanguage.BENGALI -> "• স্প্রে ডোজ: ${opt.getDosePerLitre(lang)}"
                                        AppLanguage.HINDI -> "• छिड़काव मात्रा: ${opt.getDosePerLitre(lang)}"
                                        AppLanguage.ENGLISH -> "• Spray Dose: ${opt.getDosePerLitre(lang)}"
                                    },
                                    color = AgriTextPrimary
                                )
                                val perBigha = opt.getDosePerBigha(lang)
                                if (perBigha.isNotBlank()) {
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.BENGALI -> "• জমির হিসাব: $perBigha"
                                            AppLanguage.HINDI -> "• प्रति बीघा मात्रा: $perBigha"
                                            AppLanguage.ENGLISH -> "• Land Dose: $perBigha"
                                        },
                                        color = AgriTextPrimary
                                    )
                                }
                                Text(
                                    text = when (lang) {
                                        AppLanguage.BENGALI -> "• ফসল তোলার অপেক্ষাকাল (PHI): ${opt.preHarvestIntervalDays} দিন"
                                        AppLanguage.HINDI -> "• तुड़ाई प्रतीक्षा अवधि (PHI): ${opt.preHarvestIntervalDays} दिन"
                                        AppLanguage.ENGLISH -> "• Pre-Harvest Interval (PHI): ${opt.preHarvestIntervalDays} days"
                                    },
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when (lang) {
                                        AppLanguage.BENGALI -> "• সুরক্ষা: ${opt.getSafetyPPE(lang)}"
                                        AppLanguage.HINDI -> "• सुरक्षा: ${opt.getSafetyPPE(lang)}"
                                        AppLanguage.ENGLISH -> "• Safety PPE: ${opt.getSafetyPPE(lang)}"
                                    },
                                    fontSize = 12.sp,
                                    color = AgriTextSecondary
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onOpenCalculator(crop.getName(lang)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgriGreenPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            when (lang) {
                                AppLanguage.BENGALI -> "এই ওষুধের জমিভিত্তিক ডোজ বের করুন"
                                AppLanguage.HINDI -> "इस दवा की भूमि अनुसार खुराक निकालें"
                                AppLanguage.ENGLISH -> "Calculate Land-Based Dose for This Medicine"
                            },
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FloweringFruitingSection(crop: MasterCrop, lang: AppLanguage, onOpenCalculator: (String) -> Unit) {
    val isFlower = CropDomainAdvisor.isFloriculture(crop)
    val isNursery = CropDomainAdvisor.isIndoorOrNursery(crop)
    val isAgroforestry = CropDomainAdvisor.isAgroforestryOrGreenManure(crop)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = when {
                    isFlower -> when (lang) {
                        AppLanguage.BENGALI -> "🌸 কুঁড়ি, পিঞ্চিং ও ফুলের মান উন্নয়ন:"
                        AppLanguage.HINDI -> "🌸 कली, पिंचिंग व फूलों की गुणवत्ता सुधार:"
                        AppLanguage.ENGLISH -> "🌸 Bud Initiation, Pinching & Flower Quality:"
                    }
                    isNursery -> when (lang) {
                        AppLanguage.BENGALI -> "🪴 পাতার উজ্জ্বলতা, আলো ও শেপিং পরিচর্যা:"
                        AppLanguage.HINDI -> "🪴 पत्तियों की चमक, रोशनी व शेपिंग देखभाल:"
                        AppLanguage.ENGLISH -> "🪴 Foliage Health, Light & Shaping Care:"
                    }
                    isAgroforestry -> when (lang) {
                        AppLanguage.BENGALI -> "🌳 ছাঁটাই, বায়োমাস ও মাটি সমৃদ্ধকরণ:"
                        AppLanguage.HINDI -> "🌳 छंटाई, बायोमास व मृदा संवर्धन:"
                        AppLanguage.ENGLISH -> "🌳 Pruning, Biomass & Soil Incorporation:"
                    }
                    else -> when (lang) {
                        AppLanguage.BENGALI -> "🌸 ফুল ও ফল ঝরা রোধ ও ফলন বৃদ্ধি:"
                        AppLanguage.HINDI -> "🌸 फूल व फल झड़ना रोकें एवं उपज बढ़ाएं:"
                        AppLanguage.ENGLISH -> "🌸 Flowering, Fruit Setting & Yield Boost:"
                    }
                },
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.primary
            )

            FactRow(
                label = when {
                    isFlower -> when (lang) {
                        AppLanguage.BENGALI -> "✂️ পিঞ্চিং ও কুঁড়ি ব্যবস্থাপনা"
                        AppLanguage.HINDI -> "✂️ पिंचिंग व कली प्रबंधन"
                        AppLanguage.ENGLISH -> "✂️ Pinching & Bud Management"
                    }
                    isNursery -> when (lang) {
                        AppLanguage.BENGALI -> "🌤️ আলো ও নতুন পাতার যত্ন"
                        AppLanguage.HINDI -> "🌤️ रोशनी व नई पत्तियों की देखभाल"
                        AppLanguage.ENGLISH -> "🌤️ Light & New Foliage Care"
                    }
                    isAgroforestry -> when (lang) {
                        AppLanguage.BENGALI -> "🌿 অঙ্গজ বৃদ্ধি ও কাণ্ড গঠন"
                        AppLanguage.HINDI -> "🌿 वानस्पतिक वृद्धि व तना विकास"
                        AppLanguage.ENGLISH -> "🌿 Canopy & Biomass Growth"
                    }
                    else -> when (lang) {
                        AppLanguage.BENGALI -> "🌸 ফুল আসার পরিচর্যা"
                        AppLanguage.HINDI -> "🌸 फूल आते समय देखभाल"
                        AppLanguage.ENGLISH -> "🌸 Flowering Care"
                    }
                },
                value = crop.getFloweringGuidance(lang)
            )
            FactRow(
                label = when {
                    isFlower -> when (lang) {
                        AppLanguage.BENGALI -> "💐 ডাঁটার দৈর্ঘ্য ও প্রস্ফুটন"
                        AppLanguage.HINDI -> "💐 डंठल की लंबाई व पुष्पन"
                        AppLanguage.ENGLISH -> "💐 Stem Length & Bloom Quality"
                    }
                    isNursery -> when (lang) {
                        AppLanguage.BENGALI -> "🪴 ছাঁটাই, ডালপালা ও টব পরিবর্তন"
                        AppLanguage.HINDI -> "🪴 छंटाई, आकार व रिपोर्टिंग"
                        AppLanguage.ENGLISH -> "🪴 Pruning, Shaping & Repotting"
                    }
                    isAgroforestry -> when (lang) {
                        AppLanguage.BENGALI -> "🪵 ফলন, কাটা ও জৈব প্রয়োগ"
                        AppLanguage.HINDI -> "🪵 कटाई, चारा व मिट्टी में पलटना"
                        AppLanguage.ENGLISH -> "🪵 Incorporation / Timber / Fodder Harvest"
                    }
                    else -> when (lang) {
                        AppLanguage.BENGALI -> "🍅 ফল ধারণ ও বৃদ্ধি"
                        AppLanguage.HINDI -> "🍅 फल बनना व विकास"
                        AppLanguage.ENGLISH -> "🍅 Fruiting & Development"
                    }
                },
                value = crop.getFruitingGuidance(lang)
            )

            val physProblems = crop.getPhysiologicalProblemsList(lang)
            if (physProblems.isNotEmpty()) {
                FactRow(
                    label = when (lang) {
                        AppLanguage.BENGALI -> "⚠️ সম্ভাব্য শারীরবৃত্তীয় সমস্যা"
                        AppLanguage.HINDI -> "⚠️ संभावित शारीरिक विकार"
                        AppLanguage.ENGLISH -> "⚠️ Common Physiological Disorders"
                    },
                    value = physProblems.joinToString(" • ")
                )
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> "💡 বিশেষজ্ঞ ও অভিজ্ঞ চাষির পরামর্শ:"
                            AppLanguage.HINDI -> "💡 विशेषज्ञ व अनुभवी किसान सलाह:"
                            AppLanguage.ENGLISH -> "💡 Expert & Grower Advice:"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = crop.getExpertTips(lang),
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun VerifiedMedicineSection(crop: MasterCrop, lang: AppLanguage, onOpenCalculator: (String) -> Unit) {
    var detailDialogTreatment by remember { mutableStateOf<com.example.data.model.VerifiedTreatment?>(null) }
    var sellerDialogTreatment by remember { mutableStateOf<com.example.data.model.VerifiedTreatment?>(null) }
    var calcDialogTreatment by remember { mutableStateOf<com.example.data.model.VerifiedTreatment?>(null) }

    val treatments = remember(crop) {
        VerifiedTreatmentDatabase.getCropTreatments(crop.cropId)
            .ifEmpty { VerifiedTreatmentDatabase.getCropTreatments(crop.nameEn) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> "💊 সরকারি অনুমোদিত পরীক্ষিত ওষুধ তালিকা:"
                        AppLanguage.HINDI -> "💊 सरकारी अनुमोदित सत्यापित दवा सूची:"
                        AppLanguage.ENGLISH -> "💊 Verified Govt-Approved Crop Medicines:"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> "শুধুমাত্র CIBRC ও কৃষি গবেষণা নির্দেশিত নির্ভরযোগ্য রাসায়নিক উপাদান ও সঠিক ডোজ:"
                        AppLanguage.HINDI -> "केवल CIBRC व कृषि अनुसंधान द्वारा अनुशंसित विश्वसनीय घटक व सही खुराक:"
                        AppLanguage.ENGLISH -> "Only CIBRC and ICAR-recommended active ingredients with label-verified dosages:"
                    },
                    fontSize = 13.sp,
                    color = AgriTextSecondary
                )
            }
        }

        if (treatments.isEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = AgriWarningAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (lang) {
                                AppLanguage.BENGALI -> "${crop.getName(lang)}-এর জন্য আলাদা CIBRC ওষুধ তালিকা এই মুহূর্তে যুক্ত নেই।"
                                AppLanguage.HINDI -> "${crop.getName(lang)} के लिए अलग CIBRC दवा सूची अभी उपलब्ध नहीं है।"
                                AppLanguage.ENGLISH -> "No crop-specific CIBRC medicine record is listed yet for ${crop.getName(lang)}."
                            },
                            fontWeight = FontWeight.Bold,
                            color = AgriTextPrimary,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Button(
                        onClick = { onOpenCalculator(crop.getName(lang)) },
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            when (lang) {
                                AppLanguage.BENGALI -> "সাধারণ ডোজ ক্যালকুলেটর ও বিশেষজ্ঞ পরামর্শ দেখুন"
                                AppLanguage.HINDI -> "सामान्य खुराक कैलकुलेटर व विशेषज्ञ सलाह देखें"
                                AppLanguage.ENGLISH -> "Open General Dosage Calculator & Advice"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            treatments.forEach { t ->
                FarmerMedicineCard(
                    treatment = t,
                    currentLanguage = lang,
                    onViewDetail = { detailDialogTreatment = t },
                    onViewSellers = { sellerDialogTreatment = t },
                    onViewCalculator = { calcDialogTreatment = t },
                    onContactExpert = { onOpenCalculator(crop.getName(lang)) }
                )
            }
        }
    }

    detailDialogTreatment?.let { tr ->
        MedicineDetailDialog(
            treatment = tr,
            currentLanguage = lang,
            onDismiss = { detailDialogTreatment = null },
            onOpenCalculator = {
                detailDialogTreatment = null
                calcDialogTreatment = tr
            },
            onViewSellers = {
                detailDialogTreatment = null
                sellerDialogTreatment = tr
            },
            onContactExpert = { onOpenCalculator(crop.getName(lang)) }
        )
    }

    sellerDialogTreatment?.let { tr ->
        NearbySellersDialog(
            treatment = tr,
            currentLanguage = lang,
            onDismiss = { sellerDialogTreatment = null }
        )
    }

    calcDialogTreatment?.let { tr ->
        MedicineLandCalculatorDialog(
            treatment = tr,
            currentLanguage = lang,
            onDismiss = { calcDialogTreatment = null }
        )
    }
}

@Composable
private fun CropCalendarSection(crop: MasterCrop, lang: AppLanguage) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = when (lang) {
                AppLanguage.BENGALI -> "📅 বৃদ্ধির পর্যায় ও চাষ ক্যালেন্ডার:"
                AppLanguage.HINDI -> "📅 विकास चरण व देखभाल कैलेंडर:"
                AppLanguage.ENGLISH -> "📅 Growth Stages & Cultivation Calendar:"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> "🌱 বৃদ্ধির মূল পর্যায়সমূহ:"
                        AppLanguage.HINDI -> "🌱 प्रमुख विकास चरण:"
                        AppLanguage.ENGLISH -> "🌱 Key Growth Stages:"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = crop.getGrowthStagesSummary(lang),
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (crop.steps.isNotEmpty()) {
            crop.steps.forEach { step ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = step.getTitle(lang),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.BENGALI -> "দিন ${step.dayNumber}"
                                    AppLanguage.HINDI -> "दिन ${step.dayNumber}"
                                    AppLanguage.ENGLISH -> "Day ${step.dayNumber}"
                                },
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = step.getDescription(lang), fontSize = 13.sp, lineHeight = 20.sp, color = AgriTextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun HarvestMarketSection(crop: MasterCrop, lang: AppLanguage, onCheckMarketPrice: (String) -> Unit) {
    val faqs = remember(crop.cropId, lang) { crop.getFaqsList(lang) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> "🧺 তোলা, সংরক্ষণ, খরচ ও বাজার তথ্য:"
                        AppLanguage.HINDI -> "🧺 कटाई, भंडारण, लागत व मंडी जानकारी:"
                        AppLanguage.ENGLISH -> "🧺 Harvesting, Storage, Cost & Marketing:"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                FactRow(
                    label = when (lang) {
                        AppLanguage.BENGALI -> "⏱️ পরিপক্কতার সময়"
                        AppLanguage.HINDI -> "⏱️ परिपक्वता अवधि"
                        AppLanguage.ENGLISH -> "⏱️ Maturity Duration"
                    },
                    value = when (lang) {
                        AppLanguage.BENGALI -> "প্রায় ${crop.harvestDays} দিনে সংগ্রহ/প্রস্তুতির উপযুক্ত হয়।"
                        AppLanguage.HINDI -> "लगभग ${crop.harvestDays} दिनों में कटाई/तैयारी के लिए उपयुक्त।"
                        AppLanguage.ENGLISH -> "Ready for harvest / market stage in approximately ${crop.harvestDays} days."
                    }
                )
                FactRow(
                    label = when (lang) {
                        AppLanguage.BENGALI -> "✂️ সংগ্রহ ও তোলার পদ্ধতি"
                        AppLanguage.HINDI -> "✂️ कटाई व तुड़ाई विधि"
                        AppLanguage.ENGLISH -> "✂️ Harvesting Method"
                    },
                    value = crop.getHarvestMethod(lang)
                )
                FactRow(
                    label = when (lang) {
                        AppLanguage.BENGALI -> "📦 সংরক্ষণ ও পরিবহন"
                        AppLanguage.HINDI -> "📦 भंडारण एवं रख-रखाव"
                        AppLanguage.ENGLISH -> "📦 Post-Harvest & Storage"
                    },
                    value = crop.getStorage(lang)
                )
                FactRow(
                    label = when (lang) {
                        AppLanguage.BENGALI -> "💰 বাজার ও বিক্রয় তথ্য"
                        AppLanguage.HINDI -> "💰 मंडी व बाजार जानकारी"
                        AppLanguage.ENGLISH -> "💰 Market Information"
                    },
                    value = crop.getMarketInfo(lang)
                )
                FactRow(
                    label = when (lang) {
                        AppLanguage.BENGALI -> "📊 আনুমানিক খরচ ও আয় (দাবিত্যাগ সহ)"
                        AppLanguage.HINDI -> "📊 अनुमानित लागत व आय (अस्वीकरण सहित)"
                        AppLanguage.ENGLISH -> "📊 Approximate Cost Economics"
                    },
                    value = crop.getCultivationCostInfo(lang)
                )
                FactRow(
                    label = when (lang) {
                        AppLanguage.BENGALI -> "💡 বিশেষজ্ঞের অতিরিক্ত টিপস"
                        AppLanguage.HINDI -> "💡 विशेषज्ञ के महत्वपूर्ण सुझाव"
                        AppLanguage.ENGLISH -> "💡 Expert Tips"
                    },
                    value = crop.getExpertTips(lang)
                )

                Button(
                    onClick = { onCheckMarketPrice(crop.getName(lang)) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreenPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        when (lang) {
                            AppLanguage.BENGALI -> "আজকের বাজার দর দেখুন ও বিক্রি করুন"
                            AppLanguage.HINDI -> "आज के मंडी भाव देखें व बेचें"
                            AppLanguage.ENGLISH -> "Check Today's Mandi Prices & Sell"
                        },
                        lineHeight = 20.sp
                    )
                }
            }
        }

        if (faqs.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> "❓ সচরাচর জিজ্ঞাসিত প্রশ্ন (FAQs):"
                            AppLanguage.HINDI -> "❓ अक्सर पूछे जाने वाले प्रश्न (FAQs):"
                            AppLanguage.ENGLISH -> "❓ Frequently Asked Questions (FAQs):"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    faqs.forEach { faq ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Q: ${faq.getQuestion(lang)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AgriTextPrimary
                                )
                                Text(
                                    text = faq.getAnswer(lang),
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    color = AgriTextSecondary
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
private fun FactRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, lineHeight = 18.sp)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = AgriTextPrimary, lineHeight = 21.sp)
    }
}
