package com.example.ui.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ExpertEntity
import com.example.data.model.AiSourceCitation
import com.example.data.model.AppLanguage
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.RoyalAlertBg
import com.example.ui.theme.RoyalAlertText
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalInfoBg
import com.example.ui.theme.RoyalInfoText
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalSuccessBg
import com.example.ui.theme.RoyalSuccessText
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.util.safeStartActivity
import org.json.JSONArray
import org.json.JSONObject

object AiCitationsCodec {
    fun encode(citations: List<AiSourceCitation>): String {
        if (citations.isEmpty()) return ""
        return try {
            val arr = JSONArray()
            citations.forEach { c ->
                arr.put(
                    JSONObject().apply {
                        put("title", c.title)
                        put("url", c.url)
                        put("institution", c.institution)
                        put("verifiedDate", c.verifiedDate)
                    }
                )
            }
            arr.toString()
        } catch (_: Exception) {
            ""
        }
    }

    fun decode(rawJson: String): List<AiSourceCitation> {
        if (rawJson.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(rawJson)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val url = obj.optString("url", "")
                    val title = obj.optString("title", "")
                    if (title.isNotBlank() && url.startsWith("https://")) {
                        add(
                            AiSourceCitation(
                                title = title,
                                url = url,
                                institution = obj.optString("institution", ""),
                                verifiedDate = obj.optString("verifiedDate", "")
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}

fun copyTextToClipboard(context: Context, label: String, text: String, language: AppLanguage) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    val msg = when (language) {
        AppLanguage.BENGALI -> "কপি করা হয়েছে"
        AppLanguage.HINDI -> "कॉपी किया गया"
        AppLanguage.ENGLISH -> "Copied to clipboard"
    }
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun shareAiText(context: Context, subject: String, body: String, language: AppLanguage) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    val chooserTitle = when (language) {
        AppLanguage.BENGALI -> "AI পরামর্শ শেয়ার করুন"
        AppLanguage.HINDI -> "AI सलाह साझा करें"
        AppLanguage.ENGLISH -> "Share AI Advice"
    }
    val fallback = when (language) {
        AppLanguage.BENGALI -> "শেয়ার করার অ্যাপ পাওয়া যায়নি"
        AppLanguage.HINDI -> "शेयर करने के लिए ऐप नहीं मिला"
        AppLanguage.ENGLISH -> "No share app available"
    }
    safeStartActivity(context, Intent.createChooser(sendIntent, chooserTitle), fallback)
}

fun openCitationUrl(context: Context, url: String, language: AppLanguage) {
    if (!url.startsWith("https://")) return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val fallback = when (language) {
        AppLanguage.BENGALI -> "ব্রাউজার খোলা যায়নি"
        AppLanguage.HINDI -> "ब्राउज़र नहीं खुल सका"
        AppLanguage.ENGLISH -> "Could not open browser"
    }
    safeStartActivity(context, intent, fallback)
}

@Composable
fun AiProviderBadge(
    providerUsed: String,
    fallbackUsed: Boolean,
    modelUsed: String = "",
    modifier: Modifier = Modifier
) {
    val isOpenAi = providerUsed.equals("OPENAI", ignoreCase = true)
    val bgColor = if (fallbackUsed) RoyalInfoBg else RoyalSuccessBg
    val fgColor = if (fallbackUsed) RoyalInfoText else RoyalSuccessText
    val label = buildString {
        append(if (isOpenAi) "ChatGPT Backup" else "Google Gemini")
        if (fallbackUsed) append(" • Failover")
        if (modelUsed.isNotBlank()) append(" (${modelUsed.take(18)})")
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(1.dp, fgColor.copy(alpha = 0.35f)),
        modifier = modifier.testTag("ai_provider_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = fgColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = fgColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiCitationsSection(
    citations: List<AiSourceCitation>,
    currentLanguage: AppLanguage,
    onOpenUrl: (String) -> Unit
) {
    if (citations.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = when (currentLanguage) {
                AppLanguage.BENGALI -> "📚 যাচাইকৃত কৃষি সূত্র (ট্যাপ করে দেখুন):"
                AppLanguage.HINDI -> "📚 सत्यापित कृषि स्रोत (खोलने के लिए टैप करें):"
                AppLanguage.ENGLISH -> "📚 Verified Sources (Tap to open):"
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RoyalSoftGold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            citations.forEachIndexed { idx, citation ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RoyalGreenSecondaryBg,
                    border = BorderStroke(1.dp, RoyalBorder),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable { onOpenUrl(citation.url) }
                        .testTag("ai_citation_chip_$idx")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = RoyalSoftGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Column {
                            Text(
                                text = citation.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RoyalTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val sub = listOf(citation.institution, citation.verifiedDate)
                                .filter { it.isNotBlank() }
                                .joinToString(" • ")
                            if (sub.isNotBlank()) {
                                Text(
                                    text = sub,
                                    fontSize = 10.sp,
                                    color = RoyalTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open verified source",
                            tint = RoyalSoftGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiErrorRecoveryCard(
    userMessage: String,
    currentLanguage: AppLanguage,
    retryAllowed: Boolean = true,
    onRetry: () -> Unit,
    onOpenExperts: (() -> Unit)? = null,
    onGoogleSearch: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalAlertBg),
        border = BorderStroke(1.dp, RoyalAlertText.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_error_recovery_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    tint = RoyalAlertText,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = userMessage,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium,
                    color = RoyalAlertText,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (retryAllowed) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalGreenEmerald,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("ai_retry_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "পুনরায় চেষ্টা"
                                AppLanguage.HINDI -> "पुनः प्रयास"
                                AppLanguage.ENGLISH -> "Retry"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (onOpenExperts != null) {
                    OutlinedButton(
                        onClick = onOpenExperts,
                        border = BorderStroke(1.dp, RoyalGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("ai_error_experts_btn")
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = RoyalSoftGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "বিশেষজ্ঞ"
                                AppLanguage.HINDI -> "विशेषज्ञ"
                                AppLanguage.ENGLISH -> "Experts"
                            },
                            fontSize = 12.sp,
                            color = RoyalSoftGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (onGoogleSearch != null) {
                    OutlinedButton(
                        onClick = onGoogleSearch,
                        border = BorderStroke(1.dp, RoyalTextSecondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("ai_error_search_btn")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = RoyalTextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Google", fontSize = 12.sp, color = RoyalTextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun AiMessageUtilityBar(
    messageId: String,
    feedbackVote: Int,
    currentLanguage: AppLanguage,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onRetry: (() -> Unit)? = null,
    onFeedback: (Boolean) -> Unit,
    isSpeaking: Boolean = false,
    onToggleSpeak: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onToggleSpeak != null) {
                IconButton(
                    onClick = onToggleSpeak,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("ai_tts_btn_$messageId")
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                        contentDescription = if (isSpeaking) "Stop Reading" else "Read Aloud",
                        tint = if (isSpeaking) RoyalSoftGold else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            IconButton(
                onClick = onCopy,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("ai_copy_btn_$messageId")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = onShare,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("ai_share_btn_$messageId")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (onRetry != null) {
                IconButton(
                    onClick = onRetry,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("ai_msg_retry_btn_$messageId")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onFeedback(true) },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("feedback_up_$messageId")
            ) {
                Icon(
                    imageVector = if (feedbackVote == 1) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = when (currentLanguage) {
                        AppLanguage.BENGALI -> "উপকারী"
                        AppLanguage.HINDI -> "मददगार"
                        AppLanguage.ENGLISH -> "Helpful"
                    },
                    tint = if (feedbackVote == 1) RoyalSoftGold else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = { onFeedback(false) },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("feedback_down_$messageId")
            ) {
                Icon(
                    imageVector = if (feedbackVote == -1) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                    contentDescription = when (currentLanguage) {
                        AppLanguage.BENGALI -> "আরও উন্নতি প্রয়োজন"
                        AppLanguage.HINDI -> "सुधार आवश्यक"
                        AppLanguage.ENGLISH -> "Not Helpful"
                    },
                    tint = if (feedbackVote == -1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun MatchedExpertsSuggestionCard(
    experts: List<ExpertEntity>,
    currentLanguage: AppLanguage,
    onOpenExperts: () -> Unit
) {
    if (experts.isEmpty()) return
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_matched_experts_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = RoyalSoftGold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "যাচাইকৃত কৃষি বিশেষজ্ঞ পরামর্শ"
                        AppLanguage.HINDI -> "सत्यापित कृषि विशेषज्ञ सहायता"
                        AppLanguage.ENGLISH -> "Consult Verified Agriculture Experts"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalSoftGold
                )
            }
            experts.take(2).forEach { expert ->
                Text(
                    text = "${expert.photoAvatar} ${expert.name} • ${expert.institution} (${expert.consultationFee})",
                    fontSize = 12.sp,
                    color = RoyalTextPrimary,
                    lineHeight = 17.sp
                )
            }
            Button(
                onClick = onOpenExperts,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("ai_consult_experts_btn")
            ) {
                Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "বিশেষজ্ঞের সাথে যোগাযোগ / বুকিং"
                        AppLanguage.HINDI -> "विशेषज्ञ से संपर्क / अपॉइंटमेंट"
                        AppLanguage.ENGLISH -> "Open Verified Experts Directory"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
