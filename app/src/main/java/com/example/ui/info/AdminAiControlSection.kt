package com.example.ui.info

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.TrustedAgriKnowledgeCatalog
import com.example.data.local.entity.AiKnowledgeDocEntity
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AiAdminConfig
import com.example.data.model.AiProviderHealth
import com.example.data.model.AiProviderKind
import com.example.data.model.AppLanguage
import com.example.data.remote.AiAdminRepository
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.RoyalAlertBg
import com.example.ui.theme.RoyalAlertText
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalSuccessBg
import com.example.ui.theme.RoyalSuccessText
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.RoyalWarningBg
import com.example.ui.theme.RoyalWarningText
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminAiControlSection(
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repo = remember(context) { AiAdminRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    val currentConfig by repo.configState.collectAsState()
    val statusSnapshot by repo.statusState.collectAsState()
    val customDocs by repo.observeCustomKnowledgeDocs().collectAsState(initial = emptyList())
    val telemetryLogs by repo.observeTelemetry(80).collectAsState(initial = emptyList())
    val feedbackList by repo.observeFeedback(80).collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) } // 0=Config, 1=Telemetry, 2=Knowledge, 3=Feedback

    // Editable config fields synced when cloud/local changes
    var primaryProvider by remember(currentConfig.primaryProvider) { mutableStateOf(currentConfig.primaryProvider) }
    var backupEnabled by remember(currentConfig.backupProviderEnabled) { mutableStateOf(currentConfig.backupProviderEnabled) }
    var geminiModel by remember(currentConfig.geminiModelName) { mutableStateOf(currentConfig.geminiModelName) }
    var openAiModel by remember(currentConfig.openAiModelName) { mutableStateOf(currentConfig.openAiModelName) }
    var openAiProxyUrl by remember(currentConfig.openAiProxyUrl) { mutableStateOf(currentConfig.openAiProxyUrl) }
    var timeoutMsText by remember(currentConfig.timeoutMs) { mutableStateOf(currentConfig.timeoutMs.toString()) }
    var ratePerMinText by remember(currentConfig.rateLimitPerMinute) { mutableStateOf(currentConfig.rateLimitPerMinute.toString()) }
    var dailyQuotaText by remember(currentConfig.dailyQuotaPerUser) { mutableStateOf(currentConfig.dailyQuotaPerUser.toString()) }
    var enableRag by remember(currentConfig.enableRagCitations) { mutableStateOf(currentConfig.enableRagCitations) }
    var enableWeather by remember(currentConfig.enableWeatherContext) { mutableStateOf(currentConfig.enableWeatherContext) }

    var showAddKnowledgeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repo.syncConfigFromCloud()
        repo.syncKnowledgeDocsFromCloud()
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_ai_control_panel")
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RoyalSoftGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "🤖 AI ইন্টেলিজেন্স ও ব্যাকআপ কন্ট্রোল (Admin)"
                                AppLanguage.HINDI -> "🤖 AI इंटेलिजेंस एवं बैकअप नियंत्रण (Admin)"
                                AppLanguage.ENGLISH -> "🤖 AI Intelligence Control Center"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = RoyalSoftGold
                        )
                        Text(
                            text = "Gemini Primary • OpenAI Server Proxy Backup • RAG & Telemetry",
                            fontSize = 11.sp,
                            color = RoyalTextSecondary
                        )
                    }
                }
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            repo.syncConfigFromCloud()
                            repo.syncKnowledgeDocsFromCloud()
                            repo.syncTelemetryAndFeedbackForAdmin()
                            Toast.makeText(context, "Cloud AI state refreshed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("admin_ai_sync_btn")
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = "Sync Cloud AI", tint = RoyalSoftGold)
                }
            }

            // Live Health Badges
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProviderStatusChip("Gemini: ${statusSnapshot.geminiHealth.name}", statusSnapshot.geminiHealth)
                ProviderStatusChip("OpenAI Backup: ${statusSnapshot.openAiHealth.name}", statusSnapshot.openAiHealth)
            }

            // Tab selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tabs = listOf(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "⚙️ সেটিংস"
                        AppLanguage.HINDI -> "⚙️ सेटिंग्स"
                        AppLanguage.ENGLISH -> "⚙️ Providers"
                    },
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "📊 ব্যবহার ও ত্রুটি"
                        AppLanguage.HINDI -> "📊 लॉग व लागत"
                        AppLanguage.ENGLISH -> "📊 Telemetry (${telemetryLogs.size})"
                    },
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "📚 তথ্যভাণ্ডার"
                        AppLanguage.HINDI -> "📚 ज्ञान आधार"
                        AppLanguage.ENGLISH -> "📚 RAG (${TrustedAgriKnowledgeCatalog.records.size + customDocs.size})"
                    },
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "👍 মতামত (${feedbackList.size})"
                        AppLanguage.HINDI -> "👍 प्रतिक्रिया (${feedbackList.size})"
                        AppLanguage.ENGLISH -> "👍 Feedback (${feedbackList.size})"
                    }
                )
                tabs.forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalGreenEmerald,
                            selectedLabelColor = RoyalSoftGold,
                            containerColor = RoyalGreenSecondaryBg,
                            labelColor = RoyalTextSecondary
                        ),
                        modifier = Modifier
                            .heightIn(min = 42.dp)
                            .testTag("admin_ai_tab_$index")
                    )
                }
            }

            HorizontalDivider(color = RoyalBorder)

            when (selectedTab) {
                0 -> {
                    // Provider Selection
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "প্রাথমিক AI প্রোভাইডার:"
                            AppLanguage.HINDI -> "प्राथमिक AI प्रदाता:"
                            AppLanguage.ENGLISH -> "Primary AI Provider:"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RoyalTextPrimary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AiProviderKind.entries.forEach { kind ->
                            FilterChip(
                                selected = primaryProvider == kind,
                                onClick = { primaryProvider = kind },
                                label = {
                                    Text(
                                        if (kind == AiProviderKind.GEMINI) "Google Gemini" else "OpenAI Proxy",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                modifier = Modifier.testTag("admin_primary_provider_${kind.wireValue}")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "স্বয়ংক্রিয় ব্যাকআপ ফেইলওভার চালু করুন"
                                AppLanguage.HINDI -> "स्वचालित बैकअप फ़ेलओवर सक्षम करें"
                                AppLanguage.ENGLISH -> "Enable automatic backup failover"
                            },
                            fontSize = 13.sp,
                            color = RoyalTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = backupEnabled, onCheckedChange = { backupEnabled = it })
                    }

                    OutlinedTextField(
                        value = geminiModel,
                        onValueChange = { geminiModel = it },
                        label = { Text("Gemini Model (e.g. gemini-3.8-flash)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = openAiModel,
                        onValueChange = { openAiModel = it },
                        label = { Text("OpenAI Backup Model (e.g. gpt-4o-mini)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = openAiProxyUrl,
                        onValueChange = { openAiProxyUrl = it },
                        label = { Text("OpenAI Server Proxy HTTPS URL (No keys in app)") },
                        placeholder = { Text("https://us-central1-your-project.cloudfunctions.net/openaiProxy") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_openai_proxy_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = timeoutMsText,
                            onValueChange = { timeoutMsText = it.filter { c -> c.isDigit() } },
                            label = { Text("Timeout ms") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = ratePerMinText,
                            onValueChange = { ratePerMinText = it.filter { c -> c.isDigit() } },
                            label = { Text("Req / min") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dailyQuotaText,
                            onValueChange = { dailyQuotaText = it.filter { c -> c.isDigit() } },
                            label = { Text("Daily Cap") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("RAG Grounded Citations", fontSize = 13.sp, color = RoyalTextPrimary)
                        Switch(checked = enableRag, onCheckedChange = { enableRag = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Regional Weather Context", fontSize = 13.sp, color = RoyalTextPrimary)
                        Switch(checked = enableWeather, onCheckedChange = { enableWeather = it })
                    }

                    Button(
                        onClick = {
                            val email = UserSessionManager.getSession(context)?.email ?: UserSessionManager.ADMIN_EMAIL
                            val newCfg = AiAdminConfig(
                                primaryProvider = primaryProvider,
                                backupProviderEnabled = backupEnabled,
                                geminiModelName = geminiModel.ifBlank { "gemini-3.8-flash" },
                                openAiModelName = openAiModel.ifBlank { "gpt-4o-mini" },
                                openAiProxyUrl = openAiProxyUrl.trim(),
                                timeoutMs = (timeoutMsText.toLongOrNull() ?: 35_000L).coerceIn(5_000L, 60_000L),
                                maxRetriesPerProvider = 1,
                                rateLimitPerMinute = (ratePerMinText.toIntOrNull() ?: 8).coerceIn(1, 60),
                                dailyQuotaPerUser = (dailyQuotaText.toIntOrNull() ?: 80).coerceIn(5, 500),
                                enableRagCitations = enableRag,
                                enableWeatherContext = enableWeather
                            )
                            coroutineScope.launch {
                                val res = repo.saveAdminConfig(newCfg, email)
                                if (res.isSuccess) {
                                    Toast.makeText(context, "AI configuration saved!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Invalid config", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("admin_ai_save_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save AI Provider Settings", fontWeight = FontWeight.Bold)
                    }
                }

                1 -> {
                    // Telemetry & Cost Estimates
                    val totalReq = telemetryLogs.size
                    val successReq = telemetryLogs.count { it.success }
                    val fallbackCount = telemetryLogs.count { it.fallbackUsed }
                    val avgLatency = if (totalReq > 0) telemetryLogs.map { it.latencyMs }.average().toLong() else 0L
                    val totalTokens = telemetryLogs.sumOf { it.estimatedTokens }
                    val totalCost = telemetryLogs.sumOf { it.estimatedCostUsd }

                    Surface(
                        color = RoyalGreenSecondaryBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RoyalBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Requests: $totalReq  •  Success: $successReq  •  Failovers: $fallbackCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalSoftGold
                            )
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Avg Latency: %d ms  •  Est. Tokens: %d  •  Est. Cost: $%.4f",
                                    avgLatency,
                                    totalTokens,
                                    totalCost
                                ),
                                fontSize = 11.sp,
                                color = RoyalTextSecondary
                            )
                        }
                    }

                    if (telemetryLogs.isEmpty()) {
                        Text("No AI telemetry logs recorded yet.", fontSize = 12.sp, color = RoyalTextSecondary)
                    } else {
                        telemetryLogs.take(12).forEach { log ->
                            Surface(
                                color = if (log.success) RoyalGreenSecondaryBg else RoyalAlertBg,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "${log.providerUsed}${if (log.fallbackUsed) " (FAILOVER)" else ""} • ${log.assistantMode}${if (log.cropId.isNotBlank()) " [${log.cropId}]" else ""}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (log.success) RoyalTextPrimary else RoyalAlertText
                                    )
                                    Text(
                                        text = "${log.latencyMs}ms • ${log.estimatedTokens} tok" +
                                            if (!log.success) " • ${log.errorCode}: ${log.errorSummary}" else "",
                                        fontSize = 11.sp,
                                        color = RoyalTextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = { coroutineScope.launch { repo.clearLocalTelemetry() } },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Clear Local Logs", color = RoyalTextPrimary)
                        }
                    }
                }

                2 -> {
                    // RAG Knowledge Base Manager
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Curated Built-in (${TrustedAgriKnowledgeCatalog.records.size}) + Custom (${customDocs.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalSoftGold
                        )
                        Button(
                            onClick = { showAddKnowledgeDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald, contentColor = Color.White),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("admin_ai_add_knowledge_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Source", fontSize = 12.sp)
                        }
                    }

                    customDocs.forEach { doc ->
                        Surface(
                            color = RoyalGreenSecondaryBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, RoyalBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = RoyalSoftGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(doc.titleEn, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalTextPrimary)
                                    Text("${doc.institution} • ${doc.sourceUrl}", fontSize = 10.sp, color = RoyalTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                IconButton(
                                    onClick = { coroutineScope.launch { repo.deleteCustomKnowledgeDoc(doc.id) } },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Source", tint = RoyalAlertText)
                                }
                            }
                        }
                    }

                    TrustedAgriKnowledgeCatalog.records.take(4).forEach { rec ->
                        Text(
                            text = "✓ ${rec.institution}: ${rec.titleEn} (${rec.verifiedDate})",
                            fontSize = 11.sp,
                            color = RoyalTextSecondary
                        )
                    }
                }

                3 -> {
                    // Feedback Review
                    if (feedbackList.isEmpty()) {
                        Text("No farmer feedback submitted yet.", fontSize = 12.sp, color = RoyalTextSecondary)
                    } else {
                        feedbackList.take(15).forEach { fb ->
                            Surface(
                                color = RoyalGreenSecondaryBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, RoyalBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(
                                                imageVector = if (fb.helpful) Icons.Default.ThumbUp else Icons.Default.ThumbDown,
                                                contentDescription = null,
                                                tint = if (fb.helpful) RoyalSuccessText else RoyalAlertText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "${fb.assistantMode} • ${fb.providerUsed}${if (fb.cropId.isNotBlank()) " • ${fb.cropId}" else ""}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalSoftGold
                                            )
                                        }
                                        Text(fb.reviewStatus, fontSize = 10.sp, color = RoyalTextSecondary)
                                    }
                                    Text(
                                        text = fb.messagePreview,
                                        fontSize = 11.sp,
                                        color = RoyalTextPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (fb.reviewStatus != "REVIEWED") {
                                        OutlinedButton(
                                            onClick = { coroutineScope.launch { repo.markFeedbackReviewed(fb.id) } },
                                            modifier = Modifier.heightIn(min = 42.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalSoftGold)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Mark Reviewed", fontSize = 11.sp, color = RoyalSoftGold)
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

    if (showAddKnowledgeDialog) {
        var kTitle by remember { mutableStateOf("") }
        var kInstitution by remember { mutableStateOf("ICAR / BCKV") }
        var kUrl by remember { mutableStateOf("https://") }
        var kCrops by remember { mutableStateOf("") }
        var kSummary by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddKnowledgeDialog = false },
            title = { Text("Add Verified Agricultural Source") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = kTitle, onValueChange = { kTitle = it }, label = { Text("Document Title (EN)") }, singleLine = true)
                    OutlinedTextField(value = kInstitution, onValueChange = { kInstitution = it }, label = { Text("Institution (ICAR/BCKV/UBKV)") }, singleLine = true)
                    OutlinedTextField(value = kUrl, onValueChange = { kUrl = it }, label = { Text("Official HTTPS URL") }, singleLine = true)
                    OutlinedTextField(value = kCrops, onValueChange = { kCrops = it }, label = { Text("Crop IDs CSV (e.g. rice_paddy,potato)") }, singleLine = true)
                    OutlinedTextField(value = kSummary, onValueChange = { kSummary = it }, label = { Text("Verified Summary") }, maxLines = 3)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (kTitle.isNotBlank() && kUrl.startsWith("https://") && kSummary.isNotBlank()) {
                            coroutineScope.launch {
                                val doc = AiKnowledgeDocEntity(
                                    id = "kb_custom_${UUID.randomUUID().toString().take(8)}",
                                    titleEn = kTitle.trim(),
                                    titleBn = kTitle.trim(),
                                    titleHi = kTitle.trim(),
                                    institution = kInstitution.trim(),
                                    sourceUrl = kUrl.trim(),
                                    publicationDate = "2026-11-17",
                                    verifiedDate = "2026-11-17",
                                    category = "AGRONOMY",
                                    applicableCropIdsCsv = kCrops.trim(),
                                    regionTag = "West Bengal",
                                    keywordsCsv = kCrops.trim(),
                                    summaryEn = kSummary.trim(),
                                    summaryBn = kSummary.trim(),
                                    summaryHi = kSummary.trim(),
                                    isCustomAdminAdded = true
                                )
                                repo.saveCustomKnowledgeDoc(doc)
                                showAddKnowledgeDialog = false
                            }
                        } else {
                            Toast.makeText(context, "Provide title, https:// URL, and summary", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                ) {
                    Text("Save Source")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddKnowledgeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProviderStatusChip(label: String, health: AiProviderHealth) {
    val (bg, fg) = when (health) {
        AiProviderHealth.ONLINE -> RoyalSuccessBg to RoyalSuccessText
        AiProviderHealth.UNCONFIGURED, AiProviderHealth.DEGRADED, AiProviderHealth.RATE_LIMITED -> RoyalWarningBg to RoyalWarningText
        AiProviderHealth.ERROR -> RoyalAlertBg to RoyalAlertText
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
