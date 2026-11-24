package com.example.ui.expert

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.datasource.ExpertTranslations
import com.example.data.local.entity.ConsultationMessageEntity
import com.example.data.local.entity.ExpertEntity
import com.example.data.model.AppLanguage
import com.example.data.repository.ExpertRepository
import com.example.ui.theme.AgriGreenPrimary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun ExpertChatScreen(
    expert: ExpertEntity,
    farmerId: String,
    farmerName: String,
    currentLanguage: AppLanguage = AppLanguage.BENGALI,
    onBack: () -> Unit,
    onStartAudioCall: () -> Unit,
    onStartVideoCall: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ExpertRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    val messages by repository.observeMessages(expert.id, farmerId).collectAsState(initial = emptyList())
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val quickChips = when (currentLanguage) {
        AppLanguage.BENGALI -> listOf(
            "পাতা হলুদ হয়ে যাচ্ছে",
            "ফল বা কন্দ পচে যাচ্ছে",
            "শিকড়ে পোকার আক্রমণ",
            "কী কীটনাশক ও সঠিক ডোজ কত?",
            "মাটির পিএইচ ও সার প্রয়োগ"
        )
        AppLanguage.HINDI -> listOf(
            "पत्ते पीले पड़ रहे हैं",
            "फल या कंद सड़ रहे हैं",
            "जड़ों में कीट का हमला",
            "सही कीटनाशक व मात्रा क्या है?",
            "मिट्टी का pH और खाद प्रबंधन"
        )
        AppLanguage.ENGLISH -> listOf(
            "Leaves are turning yellow",
            "Fruit or tuber rotting",
            "Root pest attack",
            "Which pesticide & exact dosage?",
            "Soil pH & fertilizer schedule"
        )
    }

    val expertDisplayName = ExpertTranslations.getDisplayName(expert, currentLanguage)
    val expertDisplayDesignation = ExpertTranslations.getDisplayDesignation(expert, currentLanguage)
    val expertDisplayInstitution = ExpertTranslations.getDisplayInstitution(expert, currentLanguage)

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage(text: String, photoUri: String? = null) {
        if (text.isBlank() && photoUri == null) return

        val farmerMsg = ConsultationMessageEntity(
            id = UUID.randomUUID().toString(),
            expertId = expert.id,
            farmerId = farmerId,
            senderRole = "FARMER",
            senderName = farmerName,
            messageText = text.trim(),
            attachmentPhotoUri = photoUri,
            timestamp = System.currentTimeMillis()
        )

        coroutineScope.launch {
            try {
                repository.sendMessage(farmerMsg)
                textInput = ""
                selectedImageUri = null

                delay(1500)
                val replyText = generateExpertAdvice(expert, text, currentLanguage)
                val expertReply = ConsultationMessageEntity(
                    id = UUID.randomUUID().toString(),
                    expertId = expert.id,
                    farmerId = farmerId,
                    senderRole = "EXPERT",
                    senderName = expertDisplayName,
                    messageText = replyText,
                    timestamp = System.currentTimeMillis()
                )
                repository.sendMessage(expertReply)
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "Message error", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                            .testTag("chat_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = expert.photoAvatar, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = expertDisplayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = com.example.ui.theme.RoyalGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "$expertDisplayDesignation • $expertDisplayInstitution",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.example.ui.theme.RoyalTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Audio Call Shortcut
                    IconButton(
                        onClick = onStartAudioCall,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_audio_call_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Audio Call", tint = Color.White)
                    }

                    // Video Call Shortcut
                    IconButton(
                        onClick = onStartVideoCall,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_video_call_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
                    }
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
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .imePadding()
            ) {
                // Quick Symptom Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickChips) { chip ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clickable { sendMessage(chip) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = chip,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Message History List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        // Verified Consultation Header Banner
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "🔒 সরাসরি কৃষি বিশেষজ্ঞের সাথে সুরক্ষিত চ্যাট"
                                        AppLanguage.HINDI -> "🔒 कृषि विशेषज्ञ के साथ सुरक्षित सीधी चैट"
                                        AppLanguage.ENGLISH -> "🔒 Encrypted Direct Consultation Chat"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "রেজিস্ট্রেশন নং: ${expert.registrationId} | প্রতিষ্ঠান: $expertDisplayInstitution"
                                        AppLanguage.HINDI -> "पंजीकरण सं.: ${expert.registrationId} | संस्थान: $expertDisplayInstitution"
                                        AppLanguage.ENGLISH -> "Reg. No.: ${expert.registrationId} | Institute: $expertDisplayInstitution"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    if (messages.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "কোনো বার্তা আদান-প্রদান হয়নি।\nআপনার ফসলের সমস্যার বিবরণ লিখে পাঠান অথবা ছবি তুলুন।"
                                        AppLanguage.HINDI -> "अभी कोई संदेश नहीं भेजा गया है।\nअपनी फसल की समस्या लिखें या फ़ोटो संलग्न करें।"
                                        AppLanguage.ENGLISH -> "No messages yet.\nDescribe your crop problem below or attach a leaf photo."
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    items(messages, key = { it.id }) { msg ->
                        val isFromFarmer = msg.senderRole == "FARMER"
                        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        val timeString = timeFormat.format(Date(msg.timestamp))
                        val youLabel = when (currentLanguage) {
                            AppLanguage.BENGALI -> "আপনি"
                            AppLanguage.HINDI -> "आप"
                            AppLanguage.ENGLISH -> "You"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isFromFarmer) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isFromFarmer) 16.dp else 4.dp,
                                    bottomEnd = if (isFromFarmer) 4.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isFromFarmer) AgriGreenPrimary else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.widthIn(max = 300.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (isFromFarmer) youLabel else "👨‍🔬 $expertDisplayName",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isFromFarmer) com.example.ui.theme.RoyalGold else com.example.ui.theme.RoyalSoftGold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    if (!msg.attachmentPhotoUri.isNullOrBlank()) {
                                        AsyncImage(
                                            model = msg.attachmentPhotoUri,
                                            contentDescription = "Crop Attachment",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                    if (msg.messageText.isNotBlank()) {
                                        Text(
                                            text = msg.messageText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isFromFarmer) Color.White else MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 20.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = timeString,
                                        fontSize = 10.sp,
                                        color = if (isFromFarmer) Color.White.copy(alpha = 0.78f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }

                // Attached Photo Preview bar (if selected)
                if (selectedImageUri != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Photo",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ফসলের ছবি যুক্ত হয়েছে"
                                    AppLanguage.HINDI -> "फसल की फ़ोटो संलग्न की गई"
                                    AppLanguage.ENGLISH -> "Crop photo attached"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                    }
                }

                // Chat Input Row
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Photo Attachment Button
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("chat_attach_photo_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Attach Photo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = {
                                Text(when (currentLanguage) {
                                    AppLanguage.BENGALI -> "সমস্যা বা লক্ষণ লিখুন..."
                                    AppLanguage.HINDI -> "समस्या या लक्षण लिखें..."
                                    AppLanguage.ENGLISH -> "Describe symptoms or issue..."
                                })
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        val canSend = textInput.isNotBlank() || selectedImageUri != null
                        IconButton(
                            onClick = {
                                sendMessage(textInput, selectedImageUri?.toString())
                            },
                            enabled = canSend,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (canSend) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                                .testTag("chat_send_btn")
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
}

/**
 * Intelligent localized response generator tailored to specialist field
 */
private fun generateExpertAdvice(expert: ExpertEntity, query: String, lang: AppLanguage): String {
    val q = query.lowercase()
    return when {
        q.contains("আলু") || q.contains("নাবী ধসা") || q.contains("ব্লাইট") ||
            q.contains("आलू") || q.contains("झुलसा") || q.contains("potato") || q.contains("blight") -> {
            when (lang) {
                AppLanguage.BENGALI -> "নমস্কার। আপনার আলুর সমস্যা পর্যালোচনা করেছি। নাবী ধসা (Late Blight) হলে জমিতে আর্দ্রতা কম রাখুন এবং প্রতি লিটার পানিতে সাইমোক্সানিল ৮% + ম্যানকোজেব ৬৪% (Cymoxanil + Mancozeb) ২.৫ গ্রাম অথবা মেটালাক্সিল ৮% + ম্যানকোজেব ৬৪% মিশিয়ে রোদ ওঠার পর স্প্রে করুন। জমিতে পানি জমতে দেবেন না।"
                AppLanguage.HINDI -> "नमस्कार। आलू में पछेती झुलसा (Late Blight) से बचाव हेतु खेत में जलभराव न होने दें और साइमोक्सानिल 8% + मैंकोजेब 64% (Cymoxanil + Mancozeb) 2.5 ग्राम प्रति लीटर पानी में मिलाकर साफ मौसम में छिड़काव करें।"
                AppLanguage.ENGLISH -> "Hello. Based on your potato symptoms, prevent waterlogging and spray Cymoxanil 8% + Mancozeb 64% at 2.5 g/litre (or Metalaxyl 8% + Mancozeb 64%) during clear sunshine to control Late Blight."
            }
        }
        q.contains("ধান") || q.contains("ব্লাস্ট") || q.contains("মাজরা") ||
            q.contains("धान") || q.contains("तना छेदक") || q.contains("paddy") || q.contains("rice") || q.contains("blast") -> {
            when (lang) {
                AppLanguage.BENGALI -> "ধানে মাজরা পোকা বা ব্লাস্টের জন্য: যদি পাতার আগা মরে বা সাদা শিস দেখা যায়, ক্লোরান্ট্রানিলিপ্রোল ১৮.৫% এসসি (Chlorantraniliprole) ০.৩ মিলি/লিটার অথবা ফ্লুবেনডিয়ামাইড প্রয়োগ করুন। ব্লাস্ট হলে ট্রাইসাইক্লাজোল ৭৫% ডব্লিউপি ০.৬ গ্রাম/লিটার স্প্রে করা প্রয়োজন।"
                AppLanguage.HINDI -> "धान में तना छेदक या ब्लास्ट के लिए: यदि सफेद बाली दिखे तो क्लोरेंट्रानिलिप्रोल 18.5% SC 0.3 मिली/लीटर पानी में स्प्रे करें। ब्लास्ट रोग होने पर ट्राइसाइक्लाज़ोल 75% WP 0.6 ग्राम/लीटर का छिड़काव करें।"
                AppLanguage.ENGLISH -> "For paddy Stem Borer (dead-heart/white-ear), apply Chlorantraniliprole 18.5% SC at 0.3 ml/litre. For Rice Blast lesions, spray Tricyclazole 75% WP at 0.6 g/litre of water."
            }
        }
        q.contains("চা") || q.contains("মাকড়") || q.contains("পোকা") ||
            q.contains("चाय") || q.contains("कीट") || q.contains("tea") || q.contains("pest") || q.contains("mite") -> {
            when (lang) {
                AppLanguage.BENGALI -> "চা বাগানের ক্ষেত্রে: লাল মাকড় বা টি মসকুইটো বাগের লক্ষণ থাকলে এজাডিরাকটিন (নিম তেল ১০,০০০ পিপিএম) ২ মিলি/লিটার বা সালফার ৮০% ডব্লিউপি ২ গ্রাম/লিটার সকালে স্প্রে করুন। প্রুনিং ক্যালেন্ডার কঠোরভাবে মেনে চলুন।"
                AppLanguage.HINDI -> "चाय बागान व कीट नियंत्रण हेतु: लाल मकड़ी या मच्छर बग के लक्षण दिखने पर सुबह के समय एज़ाडिरेक्टिन (नीम तेल 10,000 PPM) 2 मिली/लीटर या सल्फर 80% WP 2 ग्राम/लीटर पानी में छिड़कें।"
                AppLanguage.ENGLISH -> "For Tea Mosquito Bug or Red Spider Mites, spray Azadirachtin (Neem Oil 10,000 PPM) at 2 ml/litre or Sulphur 80% WP at 2 g/litre in the morning while maintaining proper pruning intervals."
            }
        }
        q.contains("পাতা হলুদ") || q.contains("पीले") || q.contains("yellow") -> {
            when (lang) {
                AppLanguage.BENGALI -> "পাতা হলুদ হওয়ার দুটি প্রধান কারণ থাকে—নাইট্রোজেনের অভাব অথবা অতিরিক্ত পানি বা শিকড় পচা। শিকড় পরীক্ষা করে দেখুন। যদি শিকড় ভালো থাকে, তবে ১৯:১৯:১৯ এনপিকে ৫ গ্রাম/লিটার এবং চিলেটেড জিঙ্ক ১ গ্রাম/লিটার স্প্রে করুন।"
                AppLanguage.HINDI -> "पत्ते पीले पड़ने के दो मुख्य कारण हैं—नाइट्रोजन/सूक्ष्म पोषक तत्व की कमी या जलभराव से जड़ सड़न। यदि जड़ें स्वस्थ हैं तो NPK 19:19:19 (5 ग्राम/लीटर) व चेलेटेड जिंक (1 ग्राम/लीटर) का छिड़काव करें।"
                AppLanguage.ENGLISH -> "Yellowing leaves usually indicate Nitrogen/micronutrient deficiency or root waterlogging. Inspect roots first; if healthy, apply a foliar spray of NPK 19:19:19 (5 g/litre) + Chelated Zinc (1 g/litre)."
            }
        }
        q.contains("পচে") || q.contains("গোড়া পচা") || q.contains("ড্যাম্পিং") ||
            q.contains("सड़") || q.contains("rot") || q.contains("tuber") -> {
            when (lang) {
                AppLanguage.BENGALI -> "গোড়া পচা রোগে ট্রাইকোডার্মা ভিরিডি ৫ গ্রাম/কেজি জৈব সারের সাথে মিশিয়ে গাছের গোড়ায় দিন অথবা কপার অক্সিক্লোরাইড ৫০% ডব্লিউপি ৩ গ্রাম/লিটার পানি দিয়ে গোড়া ভিজিয়ে দিন।"
                AppLanguage.HINDI -> "जड़ या फल सड़न के लिए जैविक खाद के साथ ट्राइकोडर्मा विरिडी (5 ग्राम/किलो) जड़ों के पास डालें अथवा कॉपर ऑक्सीक्लोराइड 50% WP 3 ग्राम/लीटर पानी से जड़ क्षेत्र में सिंचाई (drenching) करें।"
                AppLanguage.ENGLISH -> "For root or fruit rot, enrich the root zone with Trichoderma viride (5 g/kg compost) or drench the soil around affected stems with Copper Oxychloride 50% WP at 3 g/litre."
            }
        }
        q.contains("গরু") || q.contains("ছাগল") || q.contains("পশু") ||
            q.contains("पशु") || q.contains("गाय") || q.contains("cattle") || q.contains("goat") -> {
            when (lang) {
                AppLanguage.BENGALI -> "পশুর ক্ষেত্রে জ্বর বা পাতলা পায়খানা হলে দ্রুত তাজা পানির সাথে ওআরএস দিন এবং কোনো অ্যান্টিবায়োটিক দেওয়ার পূর্বে স্থানীয় প্রাণিসম্পদ কর্মকর্তার নির্দেশ নিন।"
                AppLanguage.HINDI -> "पशुओं में बुखार या दस्त होने पर तुरंत स्वच्छ पानी के साथ ORS घोल दें और किसी भी एंटीबायोटिक से पहले स्थानीय पशु चिकित्सक की सलाह अवश्य लें।"
                AppLanguage.ENGLISH -> "For livestock fever or diarrhea, provide clean drinking water with ORS immediately and consult your local veterinary officer before administering any antibiotics."
            }
        }
        else -> when (lang) {
            AppLanguage.BENGALI -> "ধন্যবাদ প্রশ্নটির জন্য। আপনার দেওয়া তথ্য আমরা নথিভুক্ত করেছি। লক্ষণ অনুযায়ী প্রথমত আক্রান্ত পাতা বা অংশ আলাদা করে সরিয়ে ফেলুন, জমি পরিষ্কার রাখুন এবং পরিষ্কার পানিতে অনুমোদিত বালাইনাশক স্প্রে করুন। প্রয়োজনে অডিও বা ভিডিও কলে বিস্তারিত দেখান।"
            AppLanguage.HINDI -> "आपके प्रश्न के लिए धन्यवाद। हमने विवरण दर्ज कर लिया है। सबसे पहले संक्रमित पत्तियों को हटा दें, खेत साफ़ रखें और स्वच्छ पानी में अनुमोदित जैव/फसल सुरक्षा दवा का छिड़काव करें। अधिक सहायता के लिए ऑडियो या वीडियो कॉल करें।"
            AppLanguage.ENGLISH -> "Thank you for sharing the details. We recommend removing severely infected leaves first, ensuring proper drainage, and spraying approved bio/crop-protection inputs in clean water. Use Audio or Video Call above for a live visual inspection."
        }
    }
}
