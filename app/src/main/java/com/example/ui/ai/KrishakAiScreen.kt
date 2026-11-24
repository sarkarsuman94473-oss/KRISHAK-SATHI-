package com.example.ui.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ai.CropContextResolver
import com.example.data.datasource.CropMasterDatabase
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ExpertEntity
import com.example.data.local.entity.GeneralAiMessageEntity
import com.example.data.local.entity.GeneralAiSessionEntity
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.CropMismatchSuggestion
import com.example.data.model.MasterCrop
import com.example.data.remote.AiAdminRepository
import com.example.data.remote.KrishakAiService
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalInputBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.RoyalWarningBg
import com.example.ui.theme.RoyalWarningText
import com.example.util.loadSafeBitmapFromUri
import com.example.util.scaleBitmapBounded
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

private const val FARMER_AI_TYPE = "FARMER_AI"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KrishakAiScreen(
    currentLanguage: AppLanguage,
    selectedCropContext: String?,
    onBack: () -> Unit,
    userId: String = LocalContext.current.let { UserSessionManager.getSession(it)?.userId ?: "guest" },
    aiService: KrishakAiService? = null,
    onOpenExperts: () -> Unit = {}
) {
    val context = LocalContext.current
    val resolvedService = remember(context, aiService) { aiService ?: KrishakAiService(context) }
    val adminRepo = remember(context) { AiAdminRepository(context) }
    val db = remember(context) { AppDatabase.getInstance(context) }
    val dao = db.generalAiDao()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val effectiveUserId = userId.ifBlank { "guest" }

    // Canonical crop resolution (prevents stale or cross-crop leaks)
    var activeCrop by remember(selectedCropContext) {
        mutableStateOf(CropContextResolver.resolveByCropIdOrName(selectedCropContext))
    }
    val activeCropId = activeCrop?.cropId.orEmpty()

    val sessions by dao.getSessionsForUserByType(effectiveUserId, FARMER_AI_TYPE)
        .collectAsState(initial = emptyList())

    var currentSessionId by remember(effectiveUserId, activeCropId) { mutableStateOf<String?>(null) }

    // Switch to most recent session scoped ONLY to (userId, FARMER_AI, activeCropId)
    LaunchedEffect(effectiveUserId, activeCropId, sessions.size) {
        val matching = dao.getLatestSessionForCrop(effectiveUserId, FARMER_AI_TYPE, activeCropId)
        currentSessionId = matching?.id
    }

    val activeSessionId = currentSessionId
    val messagesFlow = remember(activeSessionId, effectiveUserId) {
        if (activeSessionId != null) dao.getMessagesForSession(activeSessionId, effectiveUserId)
        else flowOf(emptyList())
    }
    val persistedMessages by messagesFlow.collectAsState(initial = emptyList())

    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var streamingText by remember { mutableStateOf("") }
    var activeGenerationJob by remember { mutableStateOf<Job?>(null) }

    var attachedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pendingCropMismatch by remember { mutableStateOf<CropMismatchSuggestion?>(null) }
    var pendingMismatchQuery by remember { mutableStateOf<String?>(null) }
    var pendingMismatchBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var lastFailedRetryQuery by remember { mutableStateOf<String?>(null) }
    var lastFailedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var matchedExperts by remember { mutableStateOf<List<ExpertEntity>>(emptyList()) }

    var showHistorySheet by remember { mutableStateOf(false) }
    var showCropPickerSheet by remember { mutableStateOf(false) }
    var cropSearchQuery by remember { mutableStateOf("") }

    // TTS Setup & Lifecycle cleanup
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }
    var speakingMessageId by remember { mutableStateOf<String?>(null) }

    fun localeFor(lang: AppLanguage): Locale = when (lang) {
        AppLanguage.BENGALI -> Locale("bn", "IN")
        AppLanguage.HINDI -> Locale("hi", "IN")
        AppLanguage.ENGLISH -> Locale("en", "IN")
    }

    DisposableEffect(context) {
        val ttsEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
            }
        }
        ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                if (speakingMessageId == utteranceId) speakingMessageId = null
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                if (speakingMessageId == utteranceId) speakingMessageId = null
            }
        })
        tts = ttsEngine
        onDispose {
            ttsEngine.stop()
            ttsEngine.shutdown()
        }
    }

    LaunchedEffect(currentLanguage, ttsReady, tts) {
        if (ttsReady) {
            tts?.language = localeFor(currentLanguage)
        }
    }

    // Voice Input
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull()?.trim()
            if (!spoken.isNullOrBlank()) {
                inputText = if (inputText.isBlank()) spoken else "$inputText $spoken"
            }
        }
    }

    fun launchVoiceInput() {
        val langTag = when (currentLanguage) {
            AppLanguage.BENGALI -> "bn-IN"
            AppLanguage.HINDI -> "hi-IN"
            AppLanguage.ENGLISH -> "en-IN"
        }
        val prompt = when (currentLanguage) {
            AppLanguage.BENGALI -> "আপনার কৃষি প্রশ্ন বলুন..."
            AppLanguage.HINDI -> "अपना कृषि प्रश्न बोलें..."
            AppLanguage.ENGLISH -> "Speak your farming question..."
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
        }
        try {
            voiceLauncher.launch(intent)
        } catch (_: Exception) {
            val msg = when (currentLanguage) {
                AppLanguage.BENGALI -> "এই ডিভাইসে ভয়েস ইনপুট সমর্থিত নয়। দয়া করে টাইপ করুন।"
                AppLanguage.HINDI -> "इस डिवाइस पर वॉइस इनपुट उपलब्ध नहीं है। कृपया टाइप करें।"
                AppLanguage.ENGLISH -> "Voice dictation is unavailable on this device. Please type your query."
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Photo Picker & Camera (ZERO storage permission, CAMERA permission checked cleanly)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bmp = loadSafeBitmapFromUri(context, uri, maxDim = 1280)
            if (bmp != null) {
                attachedBitmap = bmp
            } else {
                val msg = when (currentLanguage) {
                    AppLanguage.BENGALI -> "ছবি লোড করা যায়নি, অন্য ছবি বেছে নিন।"
                    AppLanguage.HINDI -> "फ़ोटो लोड नहीं हो सकी, दूसरी फ़ोटो चुनें।"
                    AppLanguage.ENGLISH -> "Failed to load image. Please select another photo."
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            attachedBitmap = scaleBitmapBounded(bmp, maxDim = 1280)
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
                    AppLanguage.BENGALI -> "ক্যামেরা চালু করা যায়নি, গ্যালারি থেকে ছবি দিন।"
                    AppLanguage.HINDI -> "कैमरा नहीं खुल सका, कृपया गैलरी से चुनें।"
                    AppLanguage.ENGLISH -> "Camera unavailable, please pick from gallery."
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        } else {
            val msg = when (currentLanguage) {
                AppLanguage.BENGALI -> "ক্যামেরার অনুমতি প্রয়োজন। গ্যালারি থেকেও ছবি দিতে পারেন।"
                AppLanguage.HINDI -> "कैमरा अनुमति आवश्यक है। आप गैलरी से भी चुन सकते हैं।"
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
                Toast.makeText(context, "Camera unavailable", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun toggleSpeak(id: String, text: String) {
        if (speakingMessageId == id) {
            tts?.stop()
            speakingMessageId = null
        } else {
            tts?.stop()
            speakingMessageId = id
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
        }
    }

    suspend fun ensureSessionForActiveCrop(initialPrompt: String, cropOverride: MasterCrop? = activeCrop): String {
        val targetCropId = cropOverride?.cropId.orEmpty()
        val existingId = currentSessionId
        if (existingId != null) {
            val existing = dao.getSessionById(existingId, effectiveUserId)
            if (existing != null && existing.cropId == targetCropId) {
                return existingId
            }
        }
        val cropLabel = cropOverride?.getName(currentLanguage) ?: when (currentLanguage) {
            AppLanguage.BENGALI -> "সাধারণ কৃষি"
            AppLanguage.HINDI -> "सामान्य कृषि"
            AppLanguage.ENGLISH -> "General Farming"
        }
        val title = "$cropLabel: ${initialPrompt.take(22)}".trim()
        val newId = UUID.randomUUID().toString()
        dao.insertSession(
            GeneralAiSessionEntity(
                id = newId,
                userId = effectiveUserId,
                title = title,
                assistantType = FARMER_AI_TYPE,
                cropId = targetCropId
            )
        )
        currentSessionId = newId
        return newId
    }

    fun startNewSessionForCrop(crop: MasterCrop?) {
        activeCrop = crop
        pendingCropMismatch = null
        pendingMismatchQuery = null
        pendingMismatchBitmap = null
        matchedExperts = emptyList()
        coroutineScope.launch {
            val cId = crop?.cropId.orEmpty()
            val cropTitle = crop?.getName(currentLanguage) ?: when (currentLanguage) {
                AppLanguage.BENGALI -> "নতুন কৃষি চ্যাট"
                AppLanguage.HINDI -> "नई कृषि बातचीत"
                AppLanguage.ENGLISH -> "New Farming Chat"
            }
            val newId = UUID.randomUUID().toString()
            dao.insertSession(
                GeneralAiSessionEntity(
                    id = newId,
                    userId = effectiveUserId,
                    title = cropTitle,
                    assistantType = FARMER_AI_TYPE,
                    cropId = cId
                )
            )
            currentSessionId = newId
            showHistorySheet = false
        }
    }

    fun executeQuery(
        queryText: String,
        bitmapToSend: Bitmap?,
        isRetryWithoutDuplicateUserMessage: Boolean = false,
        allowMismatchCheck: Boolean = true,
        targetCrop: MasterCrop? = activeCrop
    ) {
        val trimmed = queryText.trim()
        if ((trimmed.isBlank() && bitmapToSend == null) || isGenerating) return

        val effectiveQuery = trimmed.ifBlank {
            when (currentLanguage) {
                AppLanguage.BENGALI -> "এই পাতার/ফসলের ছবিটি বিশ্লেষণ করে রোগ, কারণ ও নিরাপদ প্রতিকার বলুন।"
                AppLanguage.HINDI -> "इस फसल/पत्ती की फोटो का विश्लेषण कर रोग, कारण और सुरक्षित उपाय बताएं।"
                AppLanguage.ENGLISH -> "Analyze this crop/leaf photo and explain visible symptoms, likely problem, and safe actions."
            }
        }

        if (!isRetryWithoutDuplicateUserMessage) {
            inputText = ""
            attachedBitmap = null
        }
        pendingCropMismatch = null
        isGenerating = true
        streamingText = ""

        activeGenerationJob = coroutineScope.launch {
            try {
                // Pre-check mismatch before inserting into active crop session
                val sessionCropId = targetCrop?.cropId.orEmpty()
                val preliminaryHistory = currentSessionId?.let { sId ->
                    dao.getMessagesList(sId, effectiveUserId).filter { !it.isError }.map { it.sender to it.text }
                } ?: emptyList()

                if (!isRetryWithoutDuplicateUserMessage) {
                    // Only save user message once mismatch is checked OR directly run detailed call
                    // We run mismatch check first using CropContextResolver so wrong-crop prompts don't pollute current crop history!
                    if (allowMismatchCheck && targetCrop != null) {
                        val mismatch = CropContextResolver.detectCropMismatch(targetCrop, effectiveQuery, currentLanguage)
                        if (mismatch != null) {
                            pendingCropMismatch = mismatch
                            pendingMismatchQuery = effectiveQuery
                            pendingMismatchBitmap = bitmapToSend
                            isGenerating = false
                            return@launch
                        }
                    }
                }

                val sessionId = ensureSessionForActiveCrop(effectiveQuery, targetCrop)
                if (!isRetryWithoutDuplicateUserMessage) {
                    dao.insertMessage(
                        GeneralAiMessageEntity(
                            id = UUID.randomUUID().toString(),
                            sessionId = sessionId,
                            userId = effectiveUserId,
                            sender = "user",
                            text = effectiveQuery,
                            searchQuery = effectiveQuery,
                            cropId = sessionCropId
                        )
                    )
                }

                val historyForModel = if (preliminaryHistory.isNotEmpty()) {
                    preliminaryHistory
                } else {
                    dao.getMessagesList(sessionId, effectiveUserId)
                        .filter { !it.isError }
                        .dropLast(if (isRetryWithoutDuplicateUserMessage) 0 else 1)
                        .map { it.sender to it.text }
                }

                val bundle = resolvedService.askFarmingQuestionDetailed(
                    userId = effectiveUserId,
                    query = effectiveQuery,
                    selectedCropIdOrName = targetCrop?.cropId,
                    history = historyForModel,
                    attachedBitmap = bitmapToSend,
                    language = currentLanguage,
                    allowCropMismatchPrompt = false,
                    onStreamChunk = { partial -> streamingText = partial }
                )

                val res = bundle.result
                matchedExperts = bundle.farmerContext?.matchedExperts.orEmpty()

                if (res.success && res.text.isNotBlank()) {
                    lastFailedRetryQuery = null
                    lastFailedBitmap = null
                    dao.insertMessage(
                        GeneralAiMessageEntity(
                            id = UUID.randomUUID().toString(),
                            sessionId = sessionId,
                            userId = effectiveUserId,
                            sender = "ai",
                            text = res.text,
                            searchQuery = effectiveQuery,
                            sourcesJson = AiCitationsCodec.encode(res.citations),
                            providerUsed = res.providerUsed.wireValue,
                            modelUsed = res.modelUsed,
                            fallbackUsed = res.fallbackUsed,
                            isError = false,
                            cropId = sessionCropId
                        )
                    )
                } else {
                    lastFailedRetryQuery = effectiveQuery
                    lastFailedBitmap = bitmapToSend
                    val errText = res.failure?.userMessage ?: when (currentLanguage) {
                        AppLanguage.BENGALI -> "AI পরিষেবা থেকে উত্তর পাওয়া যায়নি। নিচের 'পুনরায় চেষ্টা' বোতামে ট্যাপ করুন।"
                        AppLanguage.HINDI -> "AI सेवा से उत्तर नहीं मिल सका। कृपया नीचे 'पुनः प्रयास' पर टैप करें।"
                        AppLanguage.ENGLISH -> "Unable to reach AI service. Tap Retry below."
                    }
                    dao.insertMessage(
                        GeneralAiMessageEntity(
                            id = UUID.randomUUID().toString(),
                            sessionId = sessionId,
                            userId = effectiveUserId,
                            sender = "ai",
                            text = errText,
                            searchQuery = effectiveQuery,
                            providerUsed = res.providerUsed.wireValue,
                            modelUsed = res.modelUsed,
                            fallbackUsed = res.fallbackUsed,
                            isError = true,
                            cropId = sessionCropId
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastFailedRetryQuery = effectiveQuery
                lastFailedBitmap = bitmapToSend
                val sessionId = currentSessionId
                if (sessionId != null) {
                    dao.insertMessage(
                        GeneralAiMessageEntity(
                            id = UUID.randomUUID().toString(),
                            sessionId = sessionId,
                            userId = effectiveUserId,
                            sender = "ai",
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "সংযোগ ত্রুটি: আবার চেষ্টা করুন।"
                                AppLanguage.HINDI -> "कनेक्शन त्रुटि: कृपया पुनः प्रयास करें।"
                                AppLanguage.ENGLISH -> "Connection error. Please tap Retry."
                            },
                            searchQuery = effectiveQuery,
                            isError = true,
                            cropId = targetCrop?.cropId.orEmpty()
                        )
                    )
                }
            } finally {
                isGenerating = false
                streamingText = ""
            }
        }
    }

    LaunchedEffect(persistedMessages.size, streamingText) {
        if (persistedMessages.isNotEmpty() || streamingText.isNotBlank()) {
            val targetIndex = if (streamingText.isNotBlank()) {
                persistedMessages.size
            } else {
                (persistedMessages.size - 1).coerceAtLeast(0)
            }
            listState.animateScrollToItem(targetIndex)
        }
    }

    val initialGreeting = when (currentLanguage) {
        AppLanguage.BENGALI -> "নমস্কার! আমি আপনার 'কৃষক AI' সহকারী। ফসল নির্বাচন করুন বা সরাসরি প্রশ্ন করুন ও পাতার ছবি দিন।"
        AppLanguage.HINDI -> "नमस्ते! मैं आपका कृषक AI सहायक हूँ। अपनी फसल चुनें या सीधे प्रश्न व पत्ती की फोटो भेजें।"
        AppLanguage.ENGLISH -> "Hello! I am your Krishak AI assistant. Select a crop or ask any farming question with text, voice, or leaf photo."
    }

    val quickQuestions = when (currentLanguage) {
        AppLanguage.BENGALI -> listOf(
            "ধানের মাজরা পোকা ও ব্লাস্ট রোগের সমাধান কী?",
            "আলু চাষে নাবি ধসা (Late Blight) প্রতিরোধ কীভাবে করব?",
            "গম চাষে প্রথম সেচ (CRI) কখন দিতে হয়?",
            "সর্ষের জমিতে জাবপোকা দমন কীভাবে করব?",
            "এক বিঘা বাঁধাকপিতে আনুমানিক কত খরচ হয়?",
            "২ একরের কম জমির জন্য পাওয়ার টিলার না ট্রাক্টর কোনটি ভালো?"
        )
        AppLanguage.HINDI -> listOf(
            "धान में तना छेदक व झुलसा रोग का समाधान?",
            "आलू में लेट ब्लाइट से बचाव कैसे करें?",
            "गेहूं में पहली सिंचाई (CRI) कब दें?",
            "सरसों में माहू कीट नियंत्रण?",
            "2 एकड़ से कम भूमि के लिए पावर टिलर या ट्रैक्टर कौन उपयुक्त है?"
        )
        AppLanguage.ENGLISH -> listOf(
            "How to control stem borer & blast in rice?",
            "How to prevent late blight in potatoes?",
            "When to give first irrigation (CRI) in wheat?",
            "How to control aphids in mustard?",
            "Power tiller vs tractor or pump for small farms?"
        )
    }

    val featuredCropIds = remember {
        listOf("rice_paddy", "potato", "tomato", "tea", "cabbage", "cauliflower", "chilli", "mango")
    }
    val featuredCrops = remember(featuredCropIds) {
        featuredCropIds.mapNotNull { CropMasterDatabase.getCropById(it) }
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
                            .testTag("ai_back_btn")
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
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "🤖 KRISHAK AI (কৃষক AI)"
                                AppLanguage.HINDI -> "🤖 कृषक एआई (Krishak AI)"
                                AppLanguage.ENGLISH -> "🤖 KRISHAK AI"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 26.sp
                        )
                        val cropLabel = activeCrop?.let { "${it.iconEmoji} ${it.getName(currentLanguage)}" } ?: when (currentLanguage) {
                            AppLanguage.BENGALI -> "সব ফসল (স্বয়ংক্রিয় শনাক্তকরণ)"
                            AppLanguage.HINDI -> "सभी फसलें (स्वतः पहचान)"
                            AppLanguage.ENGLISH -> "All Crops (Auto-detect)"
                        }
                        Text(
                            text = cropLabel,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = RoyalSoftGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("krishak_ai_history_btn")
                    ) {
                        Icon(Icons.Default.History, contentDescription = "History", tint = Color.White)
                    }
                    IconButton(
                        onClick = { startNewSessionForCrop(activeCrop) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("krishak_ai_new_chat_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Chat", tint = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 820.dp)
            ) {
                // Canonical Crop Isolation Selector Bar
                Surface(
                    color = RoyalGreenSecondaryBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            val selected = activeCrop == null
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    activeCrop = null
                                    pendingCropMismatch = null
                                },
                                label = {
                                    Text(
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "🌱 সব ফসল"
                                            AppLanguage.HINDI -> "🌱 सभी फसलें"
                                            AppLanguage.ENGLISH -> "🌱 All Crops"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalGreenEmerald,
                                    selectedLabelColor = RoyalSoftGold,
                                    containerColor = RoyalGreenCardBg,
                                    labelColor = RoyalTextSecondary
                                ),
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .testTag("crop_chip_all")
                            )
                        }
                        items(featuredCrops, key = { it.cropId }) { crop ->
                            val selected = activeCrop?.cropId == crop.cropId
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    activeCrop = crop
                                    pendingCropMismatch = null
                                },
                                label = {
                                    Text(
                                        "${crop.iconEmoji} ${crop.getName(currentLanguage)}",
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalGreenEmerald,
                                    selectedLabelColor = RoyalSoftGold,
                                    containerColor = RoyalGreenCardBg,
                                    labelColor = RoyalTextSecondary
                                ),
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .testTag("crop_chip_${crop.cropId}")
                            )
                        }
                        item {
                            FilterChip(
                                selected = activeCrop != null && featuredCrops.none { it.cropId == activeCrop?.cropId },
                                onClick = { showCropPickerSheet = true },
                                label = {
                                    Text(
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "+ আরও ফসল"
                                            AppLanguage.HINDI -> "+ और फसलें"
                                            AppLanguage.ENGLISH -> "+ More Crops"
                                        },
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalGreenEmerald,
                                    selectedLabelColor = RoyalSoftGold,
                                    containerColor = RoyalGreenCardBg,
                                    labelColor = RoyalTextSecondary
                                ),
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .testTag("crop_chip_more")
                            )
                        }
                    }
                }
                HorizontalDivider(color = RoyalBorder)

                // Crop Mismatch Confirmation Banner
                AnimatedVisibility(visible = pendingCropMismatch != null) {
                    pendingCropMismatch?.let { mismatch ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = RoyalWarningBg),
                            border = BorderStroke(1.dp, RoyalGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("crop_mismatch_banner")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = mismatch.confirmationMessage,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = RoyalWarningText,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            val switched = CropContextResolver.resolveByCropIdOrName(mismatch.detectedCropId)
                                            val q = pendingMismatchQuery
                                            val bmp = pendingMismatchBitmap
                                            pendingCropMismatch = null
                                            pendingMismatchQuery = null
                                            pendingMismatchBitmap = null
                                            if (switched != null) {
                                                activeCrop = switched
                                                currentSessionId = null
                                                if (!q.isNullOrBlank()) {
                                                    executeQuery(
                                                        queryText = q,
                                                        bitmapToSend = bmp,
                                                        allowMismatchCheck = false,
                                                        targetCrop = switched
                                                    )
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald, contentColor = Color.White),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("mismatch_switch_crop_btn")
                                    ) {
                                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = mismatch.detectedCropName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val q = pendingMismatchQuery
                                            val bmp = pendingMismatchBitmap
                                            pendingCropMismatch = null
                                            pendingMismatchQuery = null
                                            pendingMismatchBitmap = null
                                            if (!q.isNullOrBlank()) {
                                                executeQuery(
                                                    queryText = q,
                                                    bitmapToSend = bmp,
                                                    allowMismatchCheck = false,
                                                    targetCrop = activeCrop
                                                )
                                            }
                                        },
                                        border = BorderStroke(1.dp, RoyalGold),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("mismatch_keep_crop_btn")
                                    ) {
                                        Text(
                                            text = mismatch.selectedCropName,
                                            fontSize = 12.sp,
                                            color = RoyalSoftGold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("krishak_ai_messages_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (persistedMessages.isEmpty()) {
                        item(key = "greeting") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.95f)
                                    .testTag("chat_bubble_greeting"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "🤖 Krishak AI",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = initialGreeting,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = AgriTextPrimary,
                                        lineHeight = 23.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { toggleSpeak("greeting", initialGreeting) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (speakingMessageId == "greeting") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = if (speakingMessageId == "greeting") MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("tts_btn_greeting")
                                    ) {
                                        Icon(
                                            imageVector = if (speakingMessageId == "greeting") Icons.Default.Stop else Icons.Default.VolumeUp,
                                            contentDescription = "Read Aloud",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (speakingMessageId == "greeting") {
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "থামান"
                                                    AppLanguage.HINDI -> "रोकें"
                                                    AppLanguage.ENGLISH -> "Stop"
                                                }
                                            } else {
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "🔊 শুনুন"
                                                    AppLanguage.HINDI -> "🔊 सुनें"
                                                    AppLanguage.ENGLISH -> "🔊 Listen"
                                                }
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Questions Chips
                        item {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "সাধারণ প্রশ্ন (ট্যাপ করে জিজ্ঞাসা করুন):"
                                    AppLanguage.HINDI -> "सामान्य प्रश्न (टैप करके पूछें):"
                                    AppLanguage.ENGLISH -> "Common Questions (tap to ask):"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = AgriTextSecondary,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                quickQuestions.forEach { q ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .testTag("quick_question_${q.take(10)}"),
                                        onClick = { executeQuery(q, null) },
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "💬 $q",
                                            modifier = Modifier.padding(12.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AgriTextPrimary,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(persistedMessages, key = { it.id }) { msg ->
                        val isAi = msg.sender != "user"
                        val citations = remember(msg.sourcesJson) { AiCitationsCodec.decode(msg.sourcesJson) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                        ) {
                            if (isAi && msg.isError) {
                                Box(modifier = Modifier.fillMaxWidth(0.95f)) {
                                    AiErrorRecoveryCard(
                                        userMessage = msg.text,
                                        currentLanguage = currentLanguage,
                                        retryAllowed = true,
                                        onRetry = {
                                            val retryPrompt = msg.searchQuery.ifBlank { lastFailedRetryQuery ?: "" }
                                            if (retryPrompt.isNotBlank() || lastFailedBitmap != null) {
                                                executeQuery(
                                                    queryText = retryPrompt,
                                                    bitmapToSend = lastFailedBitmap,
                                                    isRetryWithoutDuplicateUserMessage = true,
                                                    allowMismatchCheck = false
                                                )
                                            }
                                        },
                                        onOpenExperts = onOpenExperts
                                    )
                                }
                            } else {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(if (isAi) 0.94f else 0.85f)
                                        .testTag("chat_bubble_${msg.id}"),
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isAi) 4.dp else 16.dp,
                                        bottomEnd = if (isAi) 16.dp else 4.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isAi) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    border = if (isAi) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isAi) "🤖 Krishak AI" else when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "কৃষক (আপনি)"
                                                    AppLanguage.HINDI -> "किसान (आप)"
                                                    AppLanguage.ENGLISH -> "Farmer (You)"
                                                },
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isAi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            if (isAi) {
                                                AiProviderBadge(
                                                    providerUsed = msg.providerUsed,
                                                    fallbackUsed = msg.fallbackUsed,
                                                    modelUsed = msg.modelUsed
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = msg.text,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isAi) AgriTextPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                                            lineHeight = 23.sp
                                        )

                                        if (isAi && citations.isNotEmpty()) {
                                            AiCitationsSection(
                                                citations = citations,
                                                currentLanguage = currentLanguage,
                                                onOpenUrl = { url -> openCitationUrl(context, url, currentLanguage) }
                                            )
                                        }

                                        if (isAi && msg.text.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { toggleSpeak(msg.id, msg.text) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (speakingMessageId == msg.id) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
                                                    contentColor = if (speakingMessageId == msg.id) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer
                                                ),
                                                shape = RoundedCornerShape(16.dp),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("tts_btn_${msg.id}")
                                            ) {
                                                Icon(
                                                    imageVector = if (speakingMessageId == msg.id) Icons.Default.Stop else Icons.Default.VolumeUp,
                                                    contentDescription = "Read Aloud",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (speakingMessageId == msg.id) {
                                                        when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "থামান"
                                                            AppLanguage.HINDI -> "रोकें"
                                                            AppLanguage.ENGLISH -> "Stop"
                                                        }
                                                    } else {
                                                        when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "🔊 শুনুন"
                                                            AppLanguage.HINDI -> "🔊 सुनें"
                                                            AppLanguage.ENGLISH -> "🔊 Listen"
                                                        }
                                                    },
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            AiMessageUtilityBar(
                                                messageId = msg.id,
                                                feedbackVote = msg.feedbackVote,
                                                currentLanguage = currentLanguage,
                                                onCopy = { copyTextToClipboard(context, "Krishak AI", msg.text, currentLanguage) },
                                                onShare = { shareAiText(context, "Krishak AI Advice", msg.text, currentLanguage) },
                                                onRetry = if (msg.searchQuery.isNotBlank()) {
                                                    {
                                                        executeQuery(
                                                            queryText = msg.searchQuery,
                                                            bitmapToSend = null,
                                                            isRetryWithoutDuplicateUserMessage = true,
                                                            allowMismatchCheck = false
                                                        )
                                                    }
                                                } else null,
                                                onFeedback = { helpful ->
                                                    val vote = if (helpful) 1 else -1
                                                    coroutineScope.launch {
                                                        dao.updateMessageFeedback(msg.id, effectiveUserId, vote)
                                                        adminRepo.submitFeedback(
                                                            userId = effectiveUserId,
                                                            assistantMode = FARMER_AI_TYPE,
                                                            cropId = msg.cropId.ifBlank { activeCropId },
                                                            providerUsed = msg.providerUsed,
                                                            helpful = helpful,
                                                            messagePreview = msg.text
                                                        )
                                                    }
                                                    val toastMsg = if (helpful) {
                                                        when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "ধন্যবাদ! আপনার মূল্যায়ন সংরক্ষিত হয়েছে।"
                                                            AppLanguage.HINDI -> "धन्यवाद! आपकी प्रतिक्रिया सुरक्षित कर ली गई है।"
                                                            AppLanguage.ENGLISH -> "Thank you! Your feedback has been saved."
                                                        }
                                                    } else {
                                                        when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "মূল্যায়ন সংরক্ষিত হয়েছে। প্রয়োজনে বিশেষজ্ঞের পরামর্শ নিন।"
                                                            AppLanguage.HINDI -> "प्रतिक्रिया सहेजी गई। आवश्यकता हो तो कृषि विशेषज्ञ से सलाह लें।"
                                                            AppLanguage.ENGLISH -> "Feedback recorded. Consider consulting a verified expert below."
                                                        }
                                                    }
                                                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isGenerating) {
                        item(key = "streaming_bubble") {
                            Card(
                                modifier = Modifier.fillMaxWidth(0.92f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, RoyalBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = RoyalSoftGold)
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "Krishak AI বিশ্লেষণ করছে..."
                                                AppLanguage.HINDI -> "Krishak AI विश्लेषण कर रहा है..."
                                                AppLanguage.ENGLISH -> "Krishak AI is analyzing..."
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalSoftGold
                                        )
                                    }
                                    if (streamingText.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = streamingText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = AgriTextPrimary,
                                            lineHeight = 21.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!isGenerating && matchedExperts.isNotEmpty() && persistedMessages.isNotEmpty()) {
                        item(key = "matched_experts") {
                            MatchedExpertsSuggestionCard(
                                experts = matchedExperts,
                                currentLanguage = currentLanguage,
                                onOpenExperts = onOpenExperts
                            )
                        }
                    }
                }

                // Stop Generation Control
                if (isGenerating) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = {
                                activeGenerationJob?.cancel()
                                isGenerating = false
                                streamingText = ""
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("krishak_ai_stop_btn")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "থামুন"
                                    AppLanguage.HINDI -> "रोकें"
                                    AppLanguage.ENGLISH -> "Stop"
                                },
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Image Attachment Preview
                if (attachedBitmap != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, RoyalGold, RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                bitmap = attachedBitmap!!.asImageBitmap(),
                                contentDescription = "Attached leaf photo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ছবি সংযুক্ত করা হয়েছে"
                                AppLanguage.HINDI -> "फ़ोटो संलग्न की गई"
                                AppLanguage.ENGLISH -> "Crop photo attached"
                            },
                            fontSize = 12.sp,
                            color = RoyalSoftGold,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { attachedBitmap = null },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove image", tint = RoyalTextSecondary)
                        }
                    }
                }

                // Input Bar (Camera + Gallery + Text + Voice + Send)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { launchCameraSafely() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("krishak_ai_camera_btn")
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Capture Photo", tint = RoyalSoftGold)
                        }
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("krishak_ai_gallery_btn")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Pick Gallery Photo", tint = RoyalSoftGold)
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_input_text_field"),
                            placeholder = {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "বাংলায় কৃষি প্রশ্ন লিখুন..."
                                        AppLanguage.HINDI -> "हिंदी में कृषि प्रश्न लिखें..."
                                        AppLanguage.ENGLISH -> "Ask farming question..."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AgriTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = AgriTextPrimary
                            ),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { executeQuery(inputText, attachedBitmap) })
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { launchVoiceInput() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .testTag("ai_mic_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Dictation",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        val canSend = (inputText.isNotBlank() || attachedBitmap != null) && !isGenerating
                        IconButton(
                            onClick = { executeQuery(inputText, attachedBitmap) },
                            enabled = canSend,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (canSend) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                                .testTag("ai_send_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Crop-Isolated History Sheet
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = RoyalGreenSecondaryBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "💬 পূর্ববর্তী কৃষি আলোচনা"
                            AppLanguage.HINDI -> "💬 पिछली कृषि बातचीत"
                            AppLanguage.ENGLISH -> "💬 Saved Crop Consultations"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalTextPrimary
                    )
                    Button(
                        onClick = { startNewSessionForCrop(activeCrop) },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald, contentColor = Color.White),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "নতুন"
                                AppLanguage.HINDI -> "नया"
                                AppLanguage.ENGLISH -> "New"
                            },
                            fontSize = 12.sp
                        )
                    }
                }
                HorizontalDivider(color = RoyalBorder)
                if (sessions.isEmpty()) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "কোনো সংরক্ষিত আলোচনা নেই"
                            AppLanguage.HINDI -> "कोई सहेजी गई बातचीत नहीं है"
                            AppLanguage.ENGLISH -> "No saved consultations yet"
                        },
                        color = RoyalTextSecondary,
                        modifier = Modifier.padding(24.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sessions, key = { it.id }) { sess ->
                            val isSelected = sess.id == currentSessionId
                            val sessCrop = CropContextResolver.resolveByCropIdOrName(sess.cropId)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        activeCrop = sessCrop
                                        currentSessionId = sess.id
                                        showHistorySheet = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) RoyalGreenEmerald else RoyalGreenCardBg
                                ),
                                border = BorderStroke(1.dp, if (isSelected) RoyalGold else RoyalBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sess.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = RoyalTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = sessCrop?.getName(currentLanguage) ?: when (currentLanguage) {
                                                AppLanguage.BENGALI -> "সাধারণ কৃষি"
                                                AppLanguage.HINDI -> "सामान्य कृषि"
                                                AppLanguage.ENGLISH -> "General Farming"
                                            },
                                            fontSize = 11.sp,
                                            color = RoyalSoftGold
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                dao.deleteSession(sess.id, effectiveUserId)
                                                dao.deleteMessagesForSession(sess.id, effectiveUserId)
                                                if (sess.id == currentSessionId) currentSessionId = null
                                            }
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoyalTextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // All Master Crops Picker Sheet
    if (showCropPickerSheet) {
        val filteredCrops = remember(cropSearchQuery) {
            val q = cropSearchQuery.trim().lowercase()
            if (q.isBlank()) CropMasterDatabase.crops.take(60)
            else CropMasterDatabase.crops.filter {
                it.nameEn.contains(q, true) || it.nameBn.contains(q, true) ||
                    it.nameHi.contains(q, true) || it.cropId.contains(q, true)
            }.take(60)
        }
        ModalBottomSheet(
            onDismissRequest = { showCropPickerSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = RoyalGreenSecondaryBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "🌾 ফসল নির্বাচন করুন"
                        AppLanguage.HINDI -> "🌾 फसल चुनें"
                        AppLanguage.ENGLISH -> "🌾 Select Crop Context"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalTextPrimary
                )
                OutlinedTextField(
                    value = cropSearchQuery,
                    onValueChange = { cropSearchQuery = it },
                    placeholder = { Text("Search Potato, Rice, Tomato, Tea...", color = RoyalTextSecondary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoyalInputBg, RoundedCornerShape(12.dp))
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredCrops, key = { it.cropId }) { crop ->
                        Card(
                            onClick = {
                                activeCrop = crop
                                pendingCropMismatch = null
                                showCropPickerSheet = false
                            },
                            colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                            border = BorderStroke(1.dp, RoyalBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = "${crop.iconEmoji} ${crop.getName(currentLanguage)} (${crop.cropId})",
                                color = RoyalTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
