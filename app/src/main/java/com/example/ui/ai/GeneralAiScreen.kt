package com.example.ui.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.AppDatabase
import com.example.data.local.entity.GeneralAiMessageEntity
import com.example.data.local.entity.GeneralAiSessionEntity
import com.example.data.model.AppLanguage
import com.example.data.remote.AiAdminRepository
import com.example.data.remote.GeneralAiService
import com.example.ui.theme.AgriGreenPrimary
import com.example.util.L10n
import com.example.util.loadSafeBitmapFromUri
import com.example.util.safeStartActivity
import com.example.util.scaleBitmapBounded
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

private const val GENERAL_AI_TYPE = "GENERAL_AI"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralAiScreen(
    userId: String,
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    aiService: GeneralAiService? = null,
    onOpenExperts: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val resolvedService = remember(context, aiService) { aiService ?: GeneralAiService(context) }
    val adminRepo = remember(context) { AiAdminRepository(context) }
    val db = remember(context) { AppDatabase.getInstance(context) }
    val dao = db.generalAiDao()
    val effectiveUserId = userId.ifBlank { "guest" }

    val sessions by dao.getSessionsForUserByType(effectiveUserId, GENERAL_AI_TYPE)
        .collectAsState(initial = emptyList())
    var currentSessionId by remember(effectiveUserId) { mutableStateOf<String?>(null) }
    var currentSessionTitle by remember { mutableStateOf("New Chat") }

    LaunchedEffect(sessions) {
        if (currentSessionId == null && sessions.isNotEmpty()) {
            val first = sessions.first()
            currentSessionId = first.id
            currentSessionTitle = first.title
        }
    }

    val activeSessionId = currentSessionId
    val messagesFlow = remember(activeSessionId, effectiveUserId) {
        if (activeSessionId != null) dao.getMessagesForSession(activeSessionId, effectiveUserId)
        else flowOf(emptyList())
    }
    val messages by messagesFlow.collectAsState(initial = emptyList())

    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var streamingText by remember { mutableStateOf("") }
    var activeGenerationJob by remember { mutableStateOf<Job?>(null) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var lastFailedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var showHistorySheet by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<GeneralAiSessionEntity?>(null) }
    var renameInputText by remember { mutableStateOf("") }

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
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_PROMPT, L10n.t("ai_voice_speak", currentLanguage))
        }
        try {
            voiceLauncher.launch(intent)
        } catch (_: Exception) {
            val msg = when (currentLanguage) {
                AppLanguage.BENGALI -> "এই ডিভাইসে ভয়েস ইনপুট সমর্থিত নয়।"
                AppLanguage.HINDI -> "इस डिवाइस पर वॉइस इनपुट उपलब्ध नहीं है।"
                AppLanguage.ENGLISH -> "Voice dictation is unavailable on this device."
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            val bitmap = loadSafeBitmapFromUri(context, uri, maxDim = 1280)
            if (bitmap != null) {
                selectedBitmap = bitmap
            } else {
                val msg = when (currentLanguage) {
                    AppLanguage.BENGALI -> "ছবি লোড করা যায়নি"
                    AppLanguage.HINDI -> "फ़ोटो लोड नहीं हो सकी"
                    AppLanguage.ENGLISH -> "Failed to load image"
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            selectedBitmap = scaleBitmapBounded(bmp, maxDim = 1280)
            selectedImageUri = null
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                cameraLauncher.launch(null)
            } catch (_: Exception) {
                Toast.makeText(context, "Camera unavailable", Toast.LENGTH_SHORT).show()
            }
        } else {
            val msg = when (currentLanguage) {
                AppLanguage.BENGALI -> "ক্যামেরার অনুমতি প্রয়োজন। গ্যালারি থেকেও ছবি দিতে পারেন।"
                AppLanguage.HINDI -> "कैमरा अनुमति आवश्यक है। गैलरी से भी चुन सकते हैं।"
                AppLanguage.ENGLISH -> "Camera permission required. You can pick from gallery instead."
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

    LaunchedEffect(messages.size, streamingText) {
        if (messages.isNotEmpty() || streamingText.isNotBlank()) {
            listState.animateScrollToItem((messages.size + if (isGenerating) 1 else 0).coerceAtLeast(0))
        }
    }

    fun openGoogleSearch(query: String) {
        val searchUrl = resolvedService.buildGoogleSearchUrl(query)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
        val fallback = when (currentLanguage) {
            AppLanguage.BENGALI -> "ব্রাউজার খোলা যায়নি"
            AppLanguage.HINDI -> "ब्राउज़र नहीं खुल सका"
            AppLanguage.ENGLISH -> "Could not open browser"
        }
        safeStartActivity(context, intent, fallback)
    }

    fun startNewChat() {
        coroutineScope.launch {
            val newId = UUID.randomUUID().toString()
            val defaultTitle = when (currentLanguage) {
                AppLanguage.BENGALI -> "নতুন কথোপকথন"
                AppLanguage.HINDI -> "नई बातचीत"
                AppLanguage.ENGLISH -> "New Conversation"
            }
            val newSession = GeneralAiSessionEntity(
                id = newId,
                userId = effectiveUserId,
                title = defaultTitle,
                assistantType = GENERAL_AI_TYPE
            )
            dao.insertSession(newSession)
            currentSessionId = newId
            currentSessionTitle = defaultTitle
            showHistorySheet = false
        }
    }

    fun sendMessage(
        promptText: String,
        overrideQuery: String? = null,
        isRetryMessageId: String? = null,
        retryBitmap: Bitmap? = null
    ) {
        val query = (overrideQuery ?: promptText).trim()
        val attachedBmp = retryBitmap ?: selectedBitmap
        if ((query.isBlank() && attachedBmp == null) || isGenerating) return

        val effectiveQuery = query.ifBlank {
            when (currentLanguage) {
                AppLanguage.BENGALI -> "এই ছবিটি বিশ্লেষণ করে বিস্তারিত ব্যাখ্যা দিন।"
                AppLanguage.HINDI -> "इस तस्वीर का विश्लेषण कर विस्तार से बताएं।"
                AppLanguage.ENGLISH -> "Analyze and explain this attached image."
            }
        }

        isGenerating = true
        streamingText = ""
        if (isRetryMessageId == null) {
            inputText = ""
            selectedImageUri = null
            selectedBitmap = null
        }

        coroutineScope.launch {
            var sId = currentSessionId
            if (sId == null) {
                val newId = UUID.randomUUID().toString()
                val sessionTitle = effectiveQuery.take(24).ifBlank { "Chat" }
                dao.insertSession(
                    GeneralAiSessionEntity(
                        id = newId,
                        userId = effectiveUserId,
                        title = sessionTitle,
                        assistantType = GENERAL_AI_TYPE
                    )
                )
                currentSessionId = newId
                currentSessionTitle = sessionTitle
                sId = newId
            } else if (isRetryMessageId == null && (currentSessionTitle.contains("নতুন") || currentSessionTitle.contains("नई") || currentSessionTitle.contains("New"))) {
                val updatedTitle = effectiveQuery.take(24)
                dao.renameSession(sId, effectiveUserId, updatedTitle)
                currentSessionTitle = updatedTitle
            }

            if (isRetryMessageId == null) {
                dao.insertMessage(
                    GeneralAiMessageEntity(
                        id = UUID.randomUUID().toString(),
                        sessionId = sId,
                        userId = effectiveUserId,
                        sender = "user",
                        text = effectiveQuery,
                        searchQuery = effectiveQuery
                    )
                )
            } else {
                // Remove old failed/target assistant bubble so retry cleanly replaces it without duplicate user messages
                dao.deleteMessage(isRetryMessageId, effectiveUserId)
            }

            val pastMessages = dao.getMessagesList(sId, effectiveUserId).filter { !it.isError }
            val historyPairs = pastMessages.dropLast(if (isRetryMessageId == null) 1 else 0).map { it.sender to it.text }

            activeGenerationJob = launch {
                try {
                    val result = resolvedService.generateResponseDetailed(
                        userId = effectiveUserId,
                        prompt = effectiveQuery,
                        history = historyPairs,
                        attachedBitmap = attachedBmp,
                        language = currentLanguage,
                        onStreamChunk = { partial -> streamingText = partial }
                    )

                    if (result.success && result.text.isNotBlank()) {
                        lastFailedBitmap = null
                        dao.insertMessage(
                            GeneralAiMessageEntity(
                                id = UUID.randomUUID().toString(),
                                sessionId = sId,
                                userId = effectiveUserId,
                                sender = "assistant",
                                text = result.text,
                                hasSearchFallback = result.shouldSuggestGoogleSearch,
                                searchQuery = effectiveQuery,
                                sourcesJson = AiCitationsCodec.encode(result.citations),
                                providerUsed = result.providerUsed.wireValue,
                                modelUsed = result.modelUsed,
                                fallbackUsed = result.fallbackUsed,
                                isError = false
                            )
                        )
                    } else {
                        lastFailedBitmap = attachedBmp
                        val failureText = result.failure?.userMessage ?: when (currentLanguage) {
                            AppLanguage.BENGALI -> "AI পরিষেবা থেকে উত্তর পাওয়া যায়নি। অনুগ্রহ করে আবার চেষ্টা করুন বা Google-এ দেখুন।"
                            AppLanguage.HINDI -> "AI सेवा से उत्तर नहीं मिल सका। कृपया पुनः प्रयास करें या Google पर देखें।"
                            AppLanguage.ENGLISH -> "Unable to reach AI service. Tap Retry below or check Google Search."
                        }
                        dao.insertMessage(
                            GeneralAiMessageEntity(
                                id = UUID.randomUUID().toString(),
                                sessionId = sId,
                                userId = effectiveUserId,
                                sender = "assistant",
                                text = failureText,
                                hasSearchFallback = true,
                                searchQuery = effectiveQuery,
                                providerUsed = result.providerUsed.wireValue,
                                modelUsed = result.modelUsed,
                                fallbackUsed = result.fallbackUsed,
                                isError = true
                            )
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    lastFailedBitmap = attachedBmp
                    val fallbackText = when (currentLanguage) {
                        AppLanguage.BENGALI -> "সংযোগে সমস্যা হয়েছে। নিচের 'পুনরায় চেষ্টা' বোতামে ট্যাপ করুন।"
                        AppLanguage.HINDI -> "कनेक्शन में समस्या है। कृपया 'पुनः प्रयास' पर टैप करें।"
                        AppLanguage.ENGLISH -> "Connection issue occurred. Tap Retry below."
                    }
                    dao.insertMessage(
                        GeneralAiMessageEntity(
                            id = UUID.randomUUID().toString(),
                            sessionId = sId,
                            userId = effectiveUserId,
                            sender = "assistant",
                            text = fallbackText,
                            hasSearchFallback = true,
                            searchQuery = effectiveQuery,
                            isError = true
                        )
                    )
                } finally {
                    isGenerating = false
                    streamingText = ""
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = "AI",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = L10n.t("ai_assistant_title", currentLanguage),
                                fontSize = 17.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = L10n.t("ai_assistant_subtitle", currentLanguage),
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("ai_history_button")
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Chat History",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { startNewChat() },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("ai_new_chat_button")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "New Chat",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 820.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (messages.isEmpty() && streamingText.isBlank()) {
                        item {
                            WelcomeAiCard(
                                language = currentLanguage,
                                onSelectPrompt = { prompt -> sendMessage(prompt) }
                            )
                        }
                    }

                    items(messages, key = { it.id }) { msg ->
                        ChatBubbleItem(
                            message = msg,
                            currentLanguage = currentLanguage,
                            onCopy = { text -> copyTextToClipboard(context, "AI Assistant", text, currentLanguage) },
                            onShare = { text -> shareAiText(context, "AI Assistant", text, currentLanguage) },
                            onRetry = {
                                if (msg.searchQuery.isNotBlank() || lastFailedBitmap != null) {
                                    sendMessage(
                                        promptText = msg.searchQuery,
                                        isRetryMessageId = msg.id,
                                        retryBitmap = lastFailedBitmap
                                    )
                                }
                            },
                            onFeedback = { helpful ->
                                val vote = if (helpful) 1 else -1
                                coroutineScope.launch {
                                    dao.updateMessageFeedback(msg.id, effectiveUserId, vote)
                                    adminRepo.submitFeedback(
                                        userId = effectiveUserId,
                                        assistantMode = GENERAL_AI_TYPE,
                                        cropId = msg.cropId,
                                        providerUsed = msg.providerUsed,
                                        helpful = helpful,
                                        messagePreview = msg.text
                                    )
                                }
                                val toastMsg = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "আপনার মূল্যায়ন সংরক্ষিত হয়েছে।"
                                    AppLanguage.HINDI -> "आपकी प्रतिक्रिया सहेजी गई है।"
                                    AppLanguage.ENGLISH -> "Feedback saved."
                                }
                                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                            },
                            onOpenUrl = { url -> openCitationUrl(context, url, currentLanguage) },
                            onOpenExperts = onOpenExperts,
                            onGoogleSearch = { query -> openGoogleSearch(query) },
                            isSpeaking = speakingMessageId == msg.id,
                            onToggleSpeak = {
                                val engine = tts
                                if (engine != null && ttsReady) {
                                    if (speakingMessageId == msg.id) {
                                        engine.stop()
                                        speakingMessageId = null
                                    } else {
                                        engine.language = localeFor(currentLanguage)
                                        speakingMessageId = msg.id
                                        engine.speak(msg.text, TextToSpeech.QUEUE_FLUSH, null, msg.id)
                                    }
                                } else {
                                    Toast.makeText(context, L10n.t("ai_tts_read_aloud", currentLanguage), Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    if (isGenerating && streamingText.isNotBlank()) {
                        item { StreamingBubbleItem(text = streamingText, currentLanguage = currentLanguage) }
                    } else if (isGenerating && streamingText.isBlank()) {
                        item { ThinkingIndicatorItem(currentLanguage = currentLanguage) }
                    }
                }

                if (selectedBitmap != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, AgriGreenPrimary, RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = "Attached Image",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ছবি যুক্ত করা হয়েছে"
                                AppLanguage.HINDI -> "छवि संलग्न की गई"
                                AppLanguage.ENGLISH -> "Image attached"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                selectedImageUri = null
                                selectedBitmap = null
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove image", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (isGenerating) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = {
                                activeGenerationJob?.cancel()
                                isGenerating = false
                                streamingText = ""
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("ai_stop_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "🛑 থামুন (Stop)"
                                    AppLanguage.HINDI -> "🛑 रोकें (Stop)"
                                    AppLanguage.ENGLISH -> "🛑 Stop Generating"
                                },
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
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
                                .testTag("ai_camera_button")
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Capture Photo", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("ai_attach_image_button")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Attach Image", tint = MaterialTheme.colorScheme.primary)
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_chat_input"),
                            placeholder = {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "যেকোনো প্রশ্ন লিখুন..."
                                        AppLanguage.HINDI -> "कोई भी प्रश्न पूछें..."
                                        AppLanguage.ENGLISH -> "Ask anything..."
                                    },
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { launchVoiceInput() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("general_ai_voice_btn")
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = L10n.t("ai_voice_speak", currentLanguage),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { launchVoiceInput() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("general_ai_voice_btn")
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = L10n.t("ai_voice_speak", currentLanguage),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        val canSend = !isGenerating && (inputText.isNotBlank() || selectedBitmap != null)
                        IconButton(
                            onClick = { sendMessage(inputText) },
                            enabled = canSend,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (canSend) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("ai_send_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "💬 কথোপকথনের ইতিহাস"
                            AppLanguage.HINDI -> "💬 बातचीत का इतिहास"
                            AppLanguage.ENGLISH -> "💬 Chat History"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { startNewChat() },
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নতুন চ্যাট"
                                AppLanguage.HINDI -> "नई बातचीत"
                                AppLanguage.ENGLISH -> "New Chat"
                            },
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()

                if (sessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "কোনো পূর্ববর্তী চ্যাট নেই"
                                AppLanguage.HINDI -> "कोई पिछली बातचीत नहीं है"
                                AppLanguage.ENGLISH -> "No previous chats"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 360.dp)
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sessions, key = { it.id }) { session ->
                            val isSelected = session.id == currentSessionId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentSessionId = session.id
                                        currentSessionTitle = session.title
                                        showHistorySheet = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = session.title,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            renameInputText = session.title
                                            showRenameDialog = session
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Rename",
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                dao.deleteSession(session.id, effectiveUserId)
                                                dao.deleteMessagesForSession(session.id, effectiveUserId)
                                                if (session.id == currentSessionId) {
                                                    currentSessionId = null
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
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

    showRenameDialog?.let { sessionToRename ->
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "চ্যাটের নাম পরিবর্তন"
                        AppLanguage.HINDI -> "चैट का नाम बदलें"
                        AppLanguage.ENGLISH -> "Rename Chat"
                    }
                )
            },
            text = {
                OutlinedTextField(
                    value = renameInputText,
                    onValueChange = { renameInputText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInputText.isNotBlank()) {
                            coroutineScope.launch {
                                dao.renameSession(sessionToRename.id, effectiveUserId, renameInputText.trim())
                                if (sessionToRename.id == currentSessionId) {
                                    currentSessionTitle = renameInputText.trim()
                                }
                                showRenameDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                ) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "সংরক্ষণ"
                            AppLanguage.HINDI -> "सहेजें"
                            AppLanguage.ENGLISH -> "Save"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "বাতিল"
                            AppLanguage.HINDI -> "रद्द करें"
                            AppLanguage.ENGLISH -> "Cancel"
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun WelcomeAiCard(
    language: AppLanguage,
    onSelectPrompt: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (language) {
                            AppLanguage.BENGALI -> "নমস্কার! আমি আপনার AI Assistant"
                            AppLanguage.HINDI -> "नमस्ते! मैं आपका AI Assistant हूँ"
                            AppLanguage.ENGLISH -> "Hello! I am your AI Assistant"
                        },
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = L10n.t("ai_assistant_subtitle", language),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (language) {
                    AppLanguage.BENGALI -> "আমি আপনাকে যেকোনো বিষয়ে সহায়তা করতে পারি — সাধারণ জ্ঞান, স্কুল-কলেজের পড়াশোনা, গণিত, কোডিং, চিঠি বা ইমেল লেখা, বাংলা অনুবাদ, ব্যবসা ও কৃষি সমাধান।"
                    AppLanguage.HINDI -> "मैं किसी भी विषय में सहायता कर सकता हूँ — सामान्य ज्ञान, शिक्षा, गणित, कोडिंग, अनुवाद, व्यापार और कृषि समाधान।"
                    AppLanguage.ENGLISH -> "I can assist you with any topic — education, math calculations, programming code, writing letters/summaries, language translation, business planning, and agriculture."
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = when (language) {
                    AppLanguage.BENGALI -> "💡 উদাহরণ প্রশ্ন (ট্যাপ করে শুরু করুন):"
                    AppLanguage.HINDI -> "💡 उदाहरण प्रश्न (टैप करें):"
                    AppLanguage.ENGLISH -> "💡 Example Prompts (Tap to start):"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            val samplePrompts = when (language) {
                AppLanguage.BENGALI -> listOf(
                    "সৌরজগতের সবচেয়ে বড় গ্রহ কোনটি এবং কেন?",
                    "একটি সুন্দর ছুটির আবেদনপত্র লিখে দাও বাংলায়",
                    "Python-এ Prime Number বের করার সহজ কোড",
                    "কম পুঁজিতে নতুন ব্যবসা শুরুর ৫টি ভালো আইডিয়া",
                    "আজকের আবহাওয়া ও কৃষি সংক্রান্ত পরামর্শ"
                )
                AppLanguage.HINDI -> listOf(
                    "सौरमंडल का सबसे बड़ा ग्रह कौन सा है?",
                    "अवकाश के लिए एक औपचारिक प्रार्थना पत्र लिखें",
                    "पायथन में प्राइम नंबर ज्ञात करने का कोड",
                    "कम लागत में नए व्यवसाय के 5 विचार"
                )
                AppLanguage.ENGLISH -> listOf(
                    "Explain artificial intelligence in simple terms",
                    "Write a professional email to my supervisor",
                    "Python code to find Fibonacci series with explanation",
                    "Best low-investment business ideas for 2026"
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(samplePrompts) { prompt ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clickable { onSelectPrompt(prompt) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(text = prompt, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: GeneralAiMessageEntity,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit = {},
    onRetry: () -> Unit,
    onFeedback: (Boolean) -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
    onOpenExperts: () -> Unit = {},
    onGoogleSearch: (String) -> Unit,
    isSpeaking: Boolean = false,
    onToggleSpeak: (() -> Unit)? = null
) {
    val isUser = message.sender == "user"
    val citations = remember(message.sourcesJson) { AiCitationsCodec.decode(message.sourcesJson) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = "AI",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 460.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            if (!isUser && message.isError) {
                AiErrorRecoveryCard(
                    userMessage = message.text,
                    currentLanguage = currentLanguage,
                    retryAllowed = true,
                    onRetry = onRetry,
                    onOpenExperts = onOpenExperts,
                    onGoogleSearch = if (message.searchQuery.isNotBlank()) {
                        { onGoogleSearch(message.searchQuery) }
                    } else null
                )
            } else {
                Card(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUser) AgriGreenPrimary else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (!isUser) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AiProviderBadge(
                                    providerUsed = message.providerUsed,
                                    fallbackUsed = message.fallbackUsed,
                                    modelUsed = message.modelUsed
                                )
                            }
                        }

                        Text(
                            text = message.text,
                            fontSize = 14.sp,
                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                            lineHeight = 21.sp
                        )

                        if (!isUser && citations.isNotEmpty()) {
                            AiCitationsSection(
                                citations = citations,
                                currentLanguage = currentLanguage,
                                onOpenUrl = onOpenUrl
                            )
                        }

                        if (!isUser && message.hasSearchFallback && message.searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onGoogleSearch(message.searchQuery) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AgriGreenPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("ai_google_search_fallback_button")
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "Google-এ বিস্তারিত দেখুন"
                                        AppLanguage.HINDI -> "Google पर विवरण देखें"
                                        AppLanguage.ENGLISH -> "View Details on Google"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (!isUser) {
                    AiMessageUtilityBar(
                        messageId = message.id,
                        feedbackVote = message.feedbackVote,
                        currentLanguage = currentLanguage,
                        onCopy = { onCopy(message.text) },
                        onShare = { onShare(message.text) },
                        onRetry = onRetry,
                        onFeedback = onFeedback,
                        isSpeaking = isSpeaking,
                        onToggleSpeak = onToggleSpeak
                    )
                }
            }
        }
    }
}

@Composable
fun StreamingBubbleItem(text: String, currentLanguage: AppLanguage = AppLanguage.BENGALI) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SmartToy,
                contentDescription = "AI",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Card(
            modifier = Modifier.widthIn(max = 420.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 21.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "AI লিখছে..."
                            AppLanguage.HINDI -> "AI लिख रहा है..."
                            AppLanguage.ENGLISH -> "AI is typing..."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ThinkingIndicatorItem(currentLanguage: AppLanguage = AppLanguage.BENGALI) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SmartToy,
                contentDescription = "AI",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "AI উত্তর তৈরি করছে..."
                        AppLanguage.HINDI -> "AI उत्तर तैयार कर रहा है..."
                        AppLanguage.ENGLISH -> "AI is generating response..."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
