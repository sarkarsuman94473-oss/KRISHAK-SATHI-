package com.example.ui.problem

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ai.CropContextResolver
import com.example.data.datasource.CropMasterDatabase
import com.example.data.datasource.VerifiedTreatmentDatabase
import com.example.data.local.LeafCheckHistoryStore
import com.example.data.local.LeafCheckRecord
import com.example.data.local.entity.ExpertEntity
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AiAssistantMode
import com.example.data.model.AiGenerationResult
import com.example.data.model.AppLanguage
import com.example.data.model.VerifiedTreatment
import com.example.data.remote.AiAdminRepository
import com.example.data.remote.KrishakAiService
import com.example.ui.ai.AiCitationsSection
import com.example.ui.ai.AiErrorRecoveryCard
import com.example.ui.ai.AiMessageUtilityBar
import com.example.ui.ai.AiProviderBadge
import com.example.ui.ai.MatchedExpertsSuggestionCard
import com.example.ui.ai.copyTextToClipboard
import com.example.ui.ai.openCitationUrl
import com.example.ui.ai.shareAiText
import com.example.ui.theme.AgriWarningAmber
import com.example.util.L10n
import com.example.util.isCompactNarrowScreen
import com.example.util.loadSafeBitmapFromUri
import com.example.util.scaleBitmapBounded
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlantProblemCheckerScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onOpenCalculator: (cropName: String) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenVoiceAssistant: () -> Unit = {},
    userId: String = LocalContext.current.let { UserSessionManager.getSession(it)?.userId ?: "guest" },
    initialCropId: String? = null,
    aiService: KrishakAiService? = null,
    onOpenExperts: () -> Unit = {},
    initialBitmap: Bitmap? = null
) {
    val context = LocalContext.current
    val resolvedService = remember(context, aiService) { aiService ?: KrishakAiService(context) }
    val adminRepo = remember(context) { AiAdminRepository(context) }
    val effectiveUserId = userId.ifBlank { "guest" }
    val coroutineScope = rememberCoroutineScope()
    val isNarrow = isCompactNarrowScreen()
    val palette = rememberCropDoctorPalette()

    var selectedBitmap by remember(initialBitmap) { mutableStateOf<Bitmap?>(initialBitmap) }
    var selectedCrop by remember(initialCropId) {
        mutableStateOf(
            CropContextResolver.resolveByCropIdOrName(initialCropId)
                ?: CropMasterDatabase.crops.find { it.cropId == "cabbage" }
                ?: CropMasterDatabase.crops.first()
        )
    }
    var landAreaInput by remember(currentLanguage) {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "১ বিঘা (1 Bigha)"
                AppLanguage.HINDI -> "1 बीघा (1 Bigha)"
                AppLanguage.ENGLISH -> "1 Bigha"
            }
        )
    }
    var cropDropdownExpanded by remember { mutableStateOf(false) }

    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<String?>(null) }
    var lastResultMeta by remember { mutableStateOf<AiGenerationResult?>(null) }
    var matchedExperts by remember { mutableStateOf<List<ExpertEntity>>(emptyList()) }
    var diagnosisFeedbackVote by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    var resultSectionTopPx by remember { mutableIntStateOf(0) }

    LaunchedEffect(isAnalyzing, analysisResult, lastResultMeta?.failure, resultSectionTopPx, scrollState.maxValue) {
        if (!isAnalyzing && (analysisResult != null || lastResultMeta?.failure != null) && resultSectionTopPx > 0) {
            scrollState.scrollTo(resultSectionTopPx.coerceAtMost(scrollState.maxValue))
        }
    }

    var recentChecks by remember { mutableStateOf(LeafCheckHistoryStore.getRecords(context)) }
    var activeHistoryRecord by remember { mutableStateOf<LeafCheckRecord?>(null) }
    var activeLibraryTreatment by remember { mutableStateOf<VerifiedTreatment?>(null) }
    var showAllLibrary by remember { mutableStateOf(false) }

    fun runVisionDiagnosis() {
        val bmp = selectedBitmap
        if (bmp != null && !isAnalyzing) {
            isAnalyzing = true
            analysisResult = null
            lastResultMeta = null
            diagnosisFeedbackVote = 0
            coroutineScope.launch {
                try {
                    val bundle = resolvedService.diagnosePlantProblemDetailed(
                        userId = effectiveUserId,
                        bitmap = bmp,
                        cropIdOrName = selectedCrop.cropId,
                        landAreaInfo = landAreaInput,
                        language = currentLanguage
                    )
                    val res = bundle.result
                    lastResultMeta = res
                    matchedExperts = bundle.farmerContext?.matchedExperts.orEmpty()
                    if (res.success && res.text.isNotBlank()) {
                        analysisResult = res.text
                        recentChecks = LeafCheckHistoryStore.addRecord(
                            context,
                            LeafCheckRecord(
                                id = "chk_${System.currentTimeMillis()}",
                                cropId = selectedCrop.cropId,
                                cropName = "${selectedCrop.iconEmoji} ${selectedCrop.getName(currentLanguage)}",
                                landArea = landAreaInput,
                                diagnosisText = res.text,
                                timestampMs = System.currentTimeMillis()
                            )
                        )
                    } else {
                        analysisResult = null
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    analysisResult = null
                    lastResultMeta = AiGenerationResult(
                        success = false,
                        failure = com.example.data.model.AiFailureInfo(
                            code = com.example.data.model.AiErrorCode.UNKNOWN,
                            userMessage = when (currentLanguage) {
                                AppLanguage.BENGALI -> "বিশ্লেষণ ত্রুটি: ${e.localizedMessage ?: "সংযোগ পরীক্ষা করুন"}"
                                AppLanguage.HINDI -> "विश्लेषण त्रुटि: ${e.localizedMessage ?: "कनेक्शन जांचें"}"
                                AppLanguage.ENGLISH -> "Analysis error: ${e.localizedMessage ?: "Check connection"}"
                            },
                            technicalReason = e.message ?: "Exception",
                            primaryAttempted = com.example.data.model.AiProviderKind.GEMINI,
                            retryAllowed = true
                        )
                    )
                } finally {
                    isAnalyzing = false
                }
            }
        }
    }

    // Prioritize verified library items matching selected crop, followed by other verified treatments
    val libraryTreatments = remember(selectedCrop) {
        val all = VerifiedTreatmentDatabase.treatments
        val cropSpecific = all.filter {
            it.cropName.equals(selectedCrop.cropId, ignoreCase = true) ||
                it.cropName.equals(selectedCrop.nameEn, ignoreCase = true) ||
                selectedCrop.nameEn.contains(it.cropName, ignoreCase = true)
        }
        val remaining = all.filter { it !in cropSpecific }
        cropSpecific + remaining
    }
    val displayedLibrary = remember(libraryTreatments, showAllLibrary) {
        if (showAllLibrary) libraryTreatments else libraryTreatments.take(5)
    }

    // Android Photo Picker (bounded bitmap + stream safely closed)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = loadSafeBitmapFromUri(context, uri, maxDim = 1280)
            if (bitmap != null) {
                selectedBitmap = bitmap
                analysisResult = null
            } else {
                val msg = when (currentLanguage) {
                    AppLanguage.BENGALI -> "ছবি লোড করা যায়নি, অনুগ্রহ করে অন্য ছবি বেছে নিন।"
                    AppLanguage.HINDI -> "फ़ोटो लोड नहीं हो सकी, कृपया दूसरी फ़ोटो चुनें।"
                    AppLanguage.ENGLISH -> "Unable to load image. Please select another photo."
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
            analysisResult = null
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
                AppLanguage.ENGLISH -> "Camera permission is required. You can also pick from Gallery."
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
                val msg = when (currentLanguage) {
                    AppLanguage.BENGALI -> "ক্যামেরা পাওয়া যায়নি, গ্যালারি ব্যবহার করুন।"
                    AppLanguage.HINDI -> "कैमरा उपलब्ध नहीं है, कृपया गैलरी का उपयोग करें।"
                    AppLanguage.ENGLISH -> "Camera unavailable, please use Gallery."
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = palette.surfaceBg,
        topBar = {
            CropDoctorHeader(
                currentLanguage = currentLanguage,
                palette = palette,
                onBack = onBack,
                onCycleLanguage = {
                    val next = when (currentLanguage) {
                        AppLanguage.BENGALI -> AppLanguage.HINDI
                        AppLanguage.HINDI -> AppLanguage.ENGLISH
                        AppLanguage.ENGLISH -> AppLanguage.BENGALI
                    }
                    onLanguageChange(next)
                },
                onOpenProfile = onOpenProfile
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(palette.surfaceBg),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = if (isNarrow) 12.dp else 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top AI Crop Doctor Reassurance Banner
                AiCropDoctorBannerCard(
                    currentLanguage = currentLanguage,
                    palette = palette
                )

                // Hero Diagnostic Interactive Camera Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surfaceCard),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LeafScannerViewport(
                            selectedBitmap = selectedBitmap,
                            cropDisplayName = "${selectedCrop.iconEmoji} ${selectedCrop.getName(currentLanguage)}",
                            isAnalyzing = isAnalyzing,
                            currentLanguage = currentLanguage,
                            palette = palette
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "আক্রান্ত পাতা বা ফসলের ছবি তুলুন"
                                        AppLanguage.HINDI -> "संक्रमित पत्ती या फसल की फोटो लें"
                                        AppLanguage.ENGLISH -> "Take photo of infected leaf or crop"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "পাতার ছবি দিন এবং কয়েক সেকেন্ডে সঠিক চিকিৎসা পান"
                                        AppLanguage.HINDI -> "पत्ती की फोटो खींचें और 5 सेकंड में सटीक उपचार पाएं"
                                        AppLanguage.ENGLISH -> "Capture or pick a leaf photo for rapid AI diagnosis & cure"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Crop & Land Context Selectors (preserves existing testTags)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                maxItemsInEachRow = if (isNarrow) 1 else 2
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = cropDropdownExpanded,
                                    onExpandedChange = { cropDropdownExpanded = !cropDropdownExpanded },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = "${selectedCrop.iconEmoji} ${selectedCrop.getName(currentLanguage)}",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = {
                                            Text(
                                                text = L10n.t("plant_problem_step1", currentLanguage),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropDropdownExpanded)
                                        },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                            .testTag("problem_crop_selector"),
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = cropDropdownExpanded,
                                        onDismissRequest = { cropDropdownExpanded = false }
                                    ) {
                                        CropMasterDatabase.crops.forEach { crop ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        "${crop.iconEmoji} ${crop.getName(currentLanguage)}",
                                                        fontWeight = FontWeight.Medium,
                                                        lineHeight = 20.sp
                                                    )
                                                },
                                                onClick = {
                                                    selectedCrop = crop
                                                    cropDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = landAreaInput,
                                    onValueChange = { landAreaInput = it },
                                    singleLine = true,
                                    label = {
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "জমির পরিমাণ"
                                                AppLanguage.HINDI -> "भूमि का क्षेत्रफल"
                                                AppLanguage.ENGLISH -> "Land Area"
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    placeholder = {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "যেমন: ২ বিঘা"
                                                AppLanguage.HINDI -> "जैसे: 2 बीघा"
                                                AppLanguage.ENGLISH -> "e.g. 2 Bigha"
                                            }
                                        )
                                    },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("problem_land_input")
                                )
                            }

                            // Primary Action Button Row (Open Camera / From Gallery)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                maxItemsInEachRow = if (isNarrow) 1 else 2
                            ) {
                                Button(
                                    onClick = { launchCameraSafely() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("take_photo_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = palette.emeraldPrimary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(19.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "ক্যামেরা খুলুন"
                                            AppLanguage.HINDI -> "कैमरा खोलें"
                                            AppLanguage.ENGLISH -> "Open Camera"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("pick_gallery_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = palette.surfaceTonal,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = if (palette.isDark) palette.scannerAccent else palette.emeraldPrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "গ্যালারি থেকে"
                                            AppLanguage.HINDI -> "गैलरी से चुनें"
                                            AppLanguage.ENGLISH -> "From Gallery"
                                        },
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Analyze Button
                            Button(
                                onClick = { runVisionDiagnosis() },
                                enabled = selectedBitmap != null && !isAnalyzing,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .testTag("diagnose_action_btn"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.emeraldPrimary,
                                    contentColor = Color.White,
                                    disabledContainerColor = palette.surfaceTonal,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                if (isAnalyzing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "AI বিশ্লেষণ চলছে..."
                                            AppLanguage.HINDI -> "AI विश्लेषण चल रहा है..."
                                            AppLanguage.ENGLISH -> "AI Analysis in Progress..."
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                } else {
                                    Text(
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "🔍 রোগ পরীক্ষা ও রিপোর্ট দেখুন"
                                            AppLanguage.HINDI -> "🔍 रोग जांचें व समाधान पाएं"
                                            AppLanguage.ENGLISH -> "🔍 Analyze Leaf & Get Cure"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }

                            // Voice Assistant Diagnostic Prompt Row
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = palette.surfaceLow,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onOpenVoiceAssistant() }
                                    .testTag("crop_doctor_voice_btn")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    (if (palette.isDark) palette.scannerAccent else palette.emeraldPrimary)
                                                        .copy(alpha = 0.14f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = "Voice Prompt",
                                                tint = if (palette.isDark) palette.scannerAccent else palette.emeraldPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "বলে লক্ষণ জানান (Voice Prompt)"
                                                    AppLanguage.HINDI -> "बोलकर लक्षण बताएं (Voice Prompt)"
                                                    AppLanguage.ENGLISH -> "Describe Symptoms by Voice"
                                                },
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "ট্যাপ করে বলুন: \"গমের পাতা হলুদ হচ্ছে...\""
                                                    AppLanguage.HINDI -> "बोलें: \"गेहूं के पत्ते पीले हो रहे हैं...\""
                                                    AppLanguage.ENGLISH -> "Tap & ask Krishak AI about leaf symptoms"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = if (palette.isDark) palette.scannerAccent else palette.emeraldPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Live AI Analysis Result or Transparent Error Recovery Presentation
                AnimatedVisibility(
                    visible = analysisResult != null || lastResultMeta?.failure != null,
                    modifier = Modifier.onGloballyPositioned { coords ->
                        resultSectionTopPx = coords.positionInParent().y.toInt()
                    }
                ) {
                    val failure = lastResultMeta?.failure
                    if (failure != null && analysisResult == null) {
                        AiErrorRecoveryCard(
                            userMessage = failure.userMessage,
                            currentLanguage = currentLanguage,
                            retryAllowed = failure.retryAllowed,
                            onRetry = { runVisionDiagnosis() },
                            onOpenExperts = onOpenExperts
                        )
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("diagnosis_result_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = palette.surfaceCard),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = if (palette.isDark) palette.scannerAccent else palette.emeraldPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "AI রোগ নির্ণয় রিপোর্ট"
                                                AppLanguage.HINDI -> "AI रोग निदान रिपोर्ट"
                                                AppLanguage.ENGLISH -> "AI Disease Diagnosis Report"
                                            },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (palette.isDark) palette.onSecondaryContainer else palette.emeraldPrimary
                                        )
                                    }
                                    lastResultMeta?.let { meta ->
                                        AiProviderBadge(
                                            providerUsed = meta.providerUsed.wireValue,
                                            fallbackUsed = meta.fallbackUsed,
                                            modelUsed = meta.modelUsed
                                        )
                                    }
                                }

                                val reportText = analysisResult ?: ""
                                Text(
                                    text = reportText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 22.sp
                                )

                                lastResultMeta?.citations?.takeIf { it.isNotEmpty() }?.let { cites ->
                                    AiCitationsSection(
                                        citations = cites,
                                        currentLanguage = currentLanguage,
                                        onOpenUrl = { url -> openCitationUrl(context, url, currentLanguage) }
                                    )
                                }

                                AiMessageUtilityBar(
                                    messageId = "vision_report",
                                    feedbackVote = diagnosisFeedbackVote,
                                    currentLanguage = currentLanguage,
                                    onCopy = { copyTextToClipboard(context, "Crop Diagnosis", reportText, currentLanguage) },
                                    onShare = { shareAiText(context, "Krishak Sathi Crop Diagnosis", reportText, currentLanguage) },
                                    onRetry = { runVisionDiagnosis() },
                                    onFeedback = { helpful ->
                                        diagnosisFeedbackVote = if (helpful) 1 else -1
                                        coroutineScope.launch {
                                            adminRepo.submitFeedback(
                                                userId = effectiveUserId,
                                                assistantMode = AiAssistantMode.PLANT_PROBLEM.wireValue,
                                                cropId = selectedCrop.cropId,
                                                providerUsed = lastResultMeta?.providerUsed?.wireValue ?: "GEMINI",
                                                helpful = helpful,
                                                messagePreview = reportText
                                            )
                                        }
                                        val msg = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "ধন্যবাদ! মূল্যায়ন সংরক্ষিত হয়েছে।"
                                            AppLanguage.HINDI -> "धन्यवाद! प्रतिक्रिया सहेजी गई।"
                                            AppLanguage.ENGLISH -> "Thank you! Feedback saved."
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )

                                if (matchedExperts.isNotEmpty()) {
                                    MatchedExpertsSuggestionCard(
                                        experts = matchedExperts,
                                        currentLanguage = currentLanguage,
                                        onOpenExperts = onOpenExperts
                                    )
                                }

                                Button(
                                    onClick = { onOpenCalculator(selectedCrop.cropId) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 50.dp)
                                        .testTag("open_dosage_calc_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = palette.emeraldPrimary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.Calculate, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "এই ফসলের সঠিক ওষুধ ও ডোজ হিসাব করুন"
                                            AppLanguage.HINDI -> "इस फसल की सही दवा और खुराक की गणना करें"
                                            AppLanguage.ENGLISH -> "Calculate Exact Medicine & Dosage"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Mandatory Safety Disclaimer Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surfaceLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AgriWarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = L10n.t("safety_disclaimer", currentLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 19.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Recent Leaf Checks History Section (backed by real saved AI scans)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = if (palette.isDark) palette.scannerAccent else palette.emeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "সাম্প্রতিক পাতা পরীক্ষা"
                                    AppLanguage.HINDI -> "हाल की पत्ती जांच"
                                    AppLanguage.ENGLISH -> "Recent Leaf Checks"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (recentChecks.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    LeafCheckHistoryStore.clearRecords(context)
                                    recentChecks = emptyList()
                                },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("crop_doctor_clear_history_btn")
                            ) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "মুছুন"
                                        AppLanguage.HINDI -> "साफ़ करें"
                                        AppLanguage.ENGLISH -> "Clear"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = if (palette.isDark) palette.onSecondaryContainer else palette.emeraldPrimary
                                )
                            }
                        }
                    }

                    if (recentChecks.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = palette.surfaceCard),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "এখনো কোনো পাতা স্ক্যান সংরক্ষিত নেই। উপরে ছবি তুলে বা গ্যালারি থেকে বেছে বিশ্লেষণ করলে এখানে ইতিহাস দেখা যাবে।"
                                    AppLanguage.HINDI -> "अभी कोई पत्ती जांच सहेजी नहीं गई है। ऊपर फोटो लेकर AI विश्लेषण करने पर आपका इतिहास यहाँ दिखेगा।"
                                    AppLanguage.ENGLISH -> "No leaf scans saved yet. Capture or select a leaf photo above and analyze it to view your saved history here."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else {
                        recentChecks.forEach { rec ->
                            RecentLeafCheckCard(
                                record = rec,
                                currentLanguage = currentLanguage,
                                palette = palette,
                                onViewDetails = { activeHistoryRecord = rec }
                            )
                        }
                    }
                }

                // Crop Disease Library Section (powered by VerifiedTreatmentDatabase)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ফসলের রোগ ও প্রতিকার লাইব্রেরি"
                                    AppLanguage.HINDI -> "फसल रोग एवं उपचार पुस्तकालय"
                                    AppLanguage.ENGLISH -> "Crop Disease Library"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "সাধারণ রোগ, জৈব সমাধান ও অনুমোদিত চিকিৎসা"
                                    AppLanguage.HINDI -> "सामान्य पत्ती रोग व त्वरित प्रमाणित उपचार"
                                    AppLanguage.ENGLISH -> "Common leaf diseases & verified natural/CIBRC cures"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = (if (palette.isDark) palette.scannerAccent else palette.emeraldPrimary)
                                .copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "${libraryTreatments.size} ধরন"
                                    AppLanguage.HINDI -> "${libraryTreatments.size} प्रकार"
                                    AppLanguage.ENGLISH -> "${libraryTreatments.size} Types"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (palette.isDark) palette.onSecondaryContainer else palette.emeraldPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    displayedLibrary.forEach { treatment ->
                        DiseaseLibraryItemCard(
                            treatment = treatment,
                            currentLanguage = currentLanguage,
                            palette = palette,
                            onClick = { activeLibraryTreatment = treatment }
                        )
                    }

                    if (libraryTreatments.size > 5) {
                        OutlinedButton(
                            onClick = { showAllLibrary = !showAllLibrary },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("crop_doctor_library_toggle_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (showAllLibrary) {
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "কম দেখুন"
                                        AppLanguage.HINDI -> "कम दिखाएं"
                                        AppLanguage.ENGLISH -> "Show Fewer Diseases"
                                    }
                                } else {
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "সব ${libraryTreatments.size}টি রোগ ও সমাধান দেখুন"
                                        AppLanguage.HINDI -> "सभी ${libraryTreatments.size} रोग व उपचार देखें"
                                        AppLanguage.ENGLISH -> "View All ${libraryTreatments.size} Diseases"
                                    }
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.heightIn(min = 24.dp))
            }
        }
    }

    activeHistoryRecord?.let { rec ->
        LeafDiagnosisDetailDialog(
            record = rec,
            currentLanguage = currentLanguage,
            palette = palette,
            onDismiss = { activeHistoryRecord = null },
            onOpenCalculator = {
                onOpenCalculator(rec.cropId.ifBlank { selectedCrop.cropId })
            }
        )
    }

    activeLibraryTreatment?.let { tr ->
        LibraryTreatmentDetailDialog(
            treatment = tr,
            currentLanguage = currentLanguage,
            palette = palette,
            onDismiss = { activeLibraryTreatment = null },
            onOpenCalculator = {
                onOpenCalculator(tr.cropName)
            }
        )
    }
}
