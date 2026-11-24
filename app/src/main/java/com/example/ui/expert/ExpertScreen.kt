package com.example.ui.expert

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ExpertInquiry
import com.example.data.remote.FirestoreRepository
import com.example.ui.theme.AgriGreenPrimary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun ExpertScreen(
    currentLanguage: AppLanguage,
    userId: String,
    farmerName: String,
    onBack: () -> Unit,
    onOpenAdminConfig: () -> Unit = {}
) {
    AgricultureExpertsScreen(
        currentLanguage = currentLanguage,
        userId = userId,
        farmerName = farmerName,
        onBack = onBack,
        onOpenAdminConfig = onOpenAdminConfig
    )
}

@Composable
fun LegacyExpertInquiryView(
    currentLanguage: AppLanguage,
    userId: String,
    farmerName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { FirestoreRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val inquiries by repository.observeExpertInquiries(userId).collectAsState(initial = emptyList())

    var questionText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var cropName by remember(currentLanguage) {
        mutableStateOf(
            when (currentLanguage) {
                AppLanguage.BENGALI -> "টমেটো"
                AppLanguage.HINDI -> "टमाटर"
                AppLanguage.ENGLISH -> "Tomato"
            }
        )
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
                            .testTag("expert_back_btn")
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
                            AppLanguage.BENGALI -> "☎ কৃষি বিশেষজ্ঞের পরামর্শ"
                            AppLanguage.HINDI -> "☎ कृषि विशेषज्ञ से सलाह"
                            AppLanguage.ENGLISH -> "Ask Agriculture Expert"
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
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Toll Free & Kisan Call Center Banner
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "সরকারি কিষাণ কল সেন্টার (Kisan Call Center):"
                                        AppLanguage.HINDI -> "सरकारी किसान कॉल सेंटर (Kisan Call Center):"
                                        AppLanguage.ENGLISH -> "Government Kisan Call Center:"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "টোল ফ্রি নম্বর: ১৮০০-১৮০-১৫৫১ (সকাল ৬টা থেকে রাত ১০টা)\nকৃষি বিশ্ববিদ্যালয়ের বিজ্ঞানীরা সরাসরি উত্তর দেন।"
                                    AppLanguage.HINDI -> "टोल फ्री नंबर: 1800-180-1551 (सुबह 6 से रात 10 बजे)\nकृषि विश्वविद्यालय के वैज्ञानिक सीधे उत्तर देते हैं।"
                                    AppLanguage.ENGLISH -> "Toll-Free: 1800-180-1551 (6:00 AM to 10:00 PM)\nDirect agricultural scientists telephone helpline."
                                },
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Inquiry Submission Form
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "কৃষি বিশেষজ্ঞকে প্রশ্ন পাঠান:"
                                    AppLanguage.HINDI -> "कृषि विशेषज्ञ को प्रश्न भेजें:"
                                    AppLanguage.ENGLISH -> "Send Inquiry to Agriculture Expert:"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = cropName,
                                onValueChange = { cropName = it },
                                label = {
                                    Text(when (currentLanguage) {
                                        AppLanguage.BENGALI -> "ফসল (যেমন: বাঁধাকপি, ধান, চা)"
                                        AppLanguage.HINDI -> "फसल (उदा. गोभी, धान, चाय)"
                                        AppLanguage.ENGLISH -> "Crop (e.g. Cabbage, Rice, Tea)"
                                    })
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = questionText,
                                onValueChange = { questionText = it },
                                label = {
                                    Text(when (currentLanguage) {
                                        AppLanguage.BENGALI -> "আপনার সমস্যা বা প্রশ্ন বিস্তারিত লিখুন..."
                                        AppLanguage.HINDI -> "अपनी समस्या या प्रश्न विस्तार से लिखें..."
                                        AppLanguage.ENGLISH -> "Describe your problem or question in detail..."
                                    })
                                },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    if (isSubmitting) return@Button
                                    if (questionText.isBlank()) {
                                        val emptyErr = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "অনুগ্রহ করে আপনার প্রশ্নটি লিখুন"
                                            AppLanguage.HINDI -> "कृपया अपना प्रश्न लिखें"
                                            AppLanguage.ENGLISH -> "Please write your question"
                                        }
                                        Toast.makeText(context, emptyErr, Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isSubmitting = true
                                    coroutineScope.launch {
                                        try {
                                            repository.submitExpertInquiry(
                                                ExpertInquiry(
                                                    farmerId = userId,
                                                    farmerName = farmerName,
                                                    crop = cropName.trim(),
                                                    question = questionText.trim()
                                                ),
                                                currentLanguage
                                            )
                                            questionText = ""
                                            val okMsg = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "প্রশ্ন সফলভাবে পাঠানো হয়েছে"
                                                AppLanguage.HINDI -> "प्रश्न सफलतापूर्वक भेजा गया"
                                                AppLanguage.ENGLISH -> "Question sent successfully"
                                            }
                                            Toast.makeText(context, okMsg, Toast.LENGTH_SHORT).show()
                                        } catch (ce: CancellationException) {
                                            throw ce
                                        } catch (e: Exception) {
                                            val failMsg = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "প্রশ্ন পাঠানো যায়নি: ${e.message ?: ""}"
                                                AppLanguage.HINDI -> "प्रश्न भेजा नहीं जा सका: ${e.message ?: ""}"
                                                AppLanguage.ENGLISH -> "Could not send inquiry: ${e.message ?: ""}"
                                            }
                                            Toast.makeText(context, failMsg, Toast.LENGTH_LONG).show()
                                        } finally {
                                            isSubmitting = false
                                        }
                                    }
                                },
                                enabled = !isSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .testTag("submit_expert_question_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AgriGreenPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "প্রশ্ন পাঠান (Send to Expert)"
                                        AppLanguage.HINDI -> "प्रश्न भेजें (Send to Expert)"
                                        AppLanguage.ENGLISH -> "Send to Expert"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "বিশেষজ্ঞদের উত্তরের ইতিহাস (Past Responses):"
                            AppLanguage.HINDI -> "विशेषज्ञों के उत्तरों का इतिहास (Past Responses):"
                            AppLanguage.ENGLISH -> "Past Responses from Experts:"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (inquiries.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "কোনো প্রশ্ন পাঠানো হয়নি। উপরের ফর্ম থেকে প্রশ্ন পাঠালে বিশেষজ্ঞ বিজ্ঞানী উত্তর দেবেন।"
                                    AppLanguage.HINDI -> "कोई प्रश्न नहीं भेजा गया। उपरोक्त फॉर्म से प्रश्न भेजने पर कृषि वैज्ञानिक उत्तर देंगे।"
                                    AppLanguage.ENGLISH -> "No questions sent yet. Submit your query above and agricultural experts will reply."
                                },
                                modifier = Modifier.padding(20.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }
                } else {
                    items(inquiries) { inq ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val cropLabel = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "ফসল:"
                                        AppLanguage.HINDI -> "फसल:"
                                        AppLanguage.ENGLISH -> "Crop:"
                                    }
                                    Text(
                                        text = "$cropLabel ${inq.crop}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "বিশেষজ্ঞ যাচাইকৃত"
                                                AppLanguage.HINDI -> "विशेषज्ञ सत्यापित"
                                                AppLanguage.ENGLISH -> "EXPERT VERIFIED"
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                val questionLabel = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "প্রশ্ন:"
                                    AppLanguage.HINDI -> "प्रश्न:"
                                    AppLanguage.ENGLISH -> "Question:"
                                }
                                Text(
                                    text = "$questionLabel ${inq.question}",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )

                                if (inq.reply.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "👨‍🔬 বিশেষজ্ঞ বিজ্ঞানীর উত্তর:"
                                                    AppLanguage.HINDI -> "👨‍🔬 कृषि वैज्ञानिक का उत्तर:"
                                                    AppLanguage.ENGLISH -> "👨‍🔬 Expert Scientist Response:"
                                                },
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = inq.reply,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface,
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
        }
    }
}
