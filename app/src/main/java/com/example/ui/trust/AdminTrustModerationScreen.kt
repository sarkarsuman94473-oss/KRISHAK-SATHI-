package com.example.ui.trust

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.data.local.entity.FraudReportEntity
import com.example.data.local.entity.FraudReportStatus
import com.example.data.local.entity.GrievanceStatus
import com.example.data.local.entity.GrievanceTicketEntity
import com.example.data.model.AppLanguage
import com.example.data.repository.TrustAndSafetyRepository
import com.example.ui.theme.RoyalAlertBg
import com.example.ui.theme.RoyalAlertText
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalSuccessBg
import com.example.ui.theme.RoyalSuccessText
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.RoyalWarningBg
import com.example.ui.theme.RoyalWarningText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val PRIMARY_ADMIN_EMAIL = "sarkarsuman94473@gmail.com"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminTrustModerationScreen(
    currentLanguage: AppLanguage,
    adminEmail: String,
    onBack: () -> Unit,
    repository: TrustAndSafetyRepository? = null
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember(repository) { repository ?: TrustAndSafetyRepository(context) }
    val isAuthorizedAdmin = remember(adminEmail) {
        adminEmail.trim().equals(PRIMARY_ADMIN_EMAIL, ignoreCase = true)
    }

    LaunchedEffect(repo) {
        repo.syncSuspensionsFromFirestore()
    }

    val fraudReports by repo.observeAllFraudReports().collectAsState(initial = emptyList())
    val suspensions by repo.observeAllSuspensions().collectAsState(initial = emptyList())
    val grievances by repo.observeAllGrievances().collectAsState(initial = emptyList())
    val auditLogs by repo.observeAllAuditLogs().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    var resolveTargetReport by remember { mutableStateOf<FraudReportEntity?>(null) }
    var grievanceReplyTarget by remember { mutableStateOf<GrievanceTicketEntity?>(null) }

    // Manual seller suspension form
    var manualSellerId by remember { mutableStateOf("") }
    var manualBusinessName by remember { mutableStateOf("") }
    var manualReason by remember { mutableStateOf("") }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("admin_trust_moderation_screen"),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Column {
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = RoyalGreenSecondaryBg,
                        titleContentColor = RoyalTextPrimary
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(48.dp).testTag("admin_trust_back_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = RoyalTextPrimary)
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = RoyalGold)
                            Column {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অ্যাডমিন ট্রাস্ট ও প্রতারণা নিয়ন্ত্রণ"
                                        AppLanguage.HINDI -> "एडमिन ट्रस्ट व धोखाधड़ी नियंत्रण"
                                        AppLanguage.ENGLISH -> "Trust, Fraud & Grievance Center"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isAuthorizedAdmin) adminEmail else "Read-Only Preview ($adminEmail)",
                                    fontSize = 10.sp,
                                    color = RoyalSoftGold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                )

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = RoyalGreenSecondaryBg,
                    contentColor = RoyalGold,
                    edgePadding = 8.dp
                ) {
                    val tabs = listOf(
                        "Fraud Reports (${fraudReports.count { it.status == FraudReportStatus.OPEN }})",
                        "Suspensions (${suspensions.count { it.isSuspended }})",
                        "Grievances (${grievances.count { it.status != GrievanceStatus.RESOLVED }})",
                        "Audit Logs (${auditLogs.size})"
                    )
                    tabs.forEachIndexed { idx, label ->
                        Tab(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            modifier = Modifier.testTag("admin_trust_tab_$idx"),
                            text = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(RoyalGreenPrimaryBg)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!isAuthorizedAdmin) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RoyalAlertBg),
                    border = BorderStroke(1.dp, RoyalAlertText),
                    modifier = Modifier.padding(16.dp).fillMaxWidth().widthIn(max = 640.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = RoyalAlertText, modifier = Modifier.size(28.dp))
                        Text("Admin Access Required", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalAlertText)
                        Text(
                            text = "Trust moderation actions are restricted to the verified admin account ($PRIMARY_ADMIN_EMAIL).",
                            fontSize = 13.sp,
                            color = RoyalTextPrimary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().widthIn(max = 860.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            if (fraudReports.isEmpty()) {
                                item {
                                    EmptyModerationCard("No fraud reports filed yet. All seller reports will appear here.")
                                }
                            } else {
                                items(fraudReports, key = { it.id }) { report ->
                                    val isOpen = report.status == FraudReportStatus.OPEN || report.status == FraudReportStatus.UNDER_REVIEW
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                        border = BorderStroke(1.dp, if (isOpen) RoyalWarningText.copy(alpha = 0.55f) else RoyalBorder),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("admin_fraud_card_${report.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${report.targetType}: ${report.targetName.ifBlank { report.targetId }}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = RoyalTextPrimary,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Surface(
                                                    color = if (isOpen) RoyalWarningBg else RoyalSuccessBg,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = report.status,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isOpen) RoyalWarningText else RoyalSuccessText,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                            Text("Reason: ${report.reasonCode} • Seller ID: ${report.sellerUserId.ifBlank { "N/A" }}", fontSize = 11.sp, color = RoyalSoftGold)
                                            Text(report.description, fontSize = 12.sp, color = RoyalTextPrimary)
                                            Text(
                                                text = "Reported by ${report.reporterName} • ${dateFormatter.format(Date(report.createdAtMillis))}",
                                                fontSize = 10.sp,
                                                color = RoyalTextSecondary
                                            )
                                            if (report.adminNote.isNotBlank()) {
                                                Text("Admin Note: ${report.adminNote}", fontSize = 11.sp, color = RoyalSuccessText)
                                            }
                                            if (isOpen) {
                                                Button(
                                                    onClick = { resolveTargetReport = report },
                                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                                                    modifier = Modifier.heightIn(min = 48.dp).testTag("admin_resolve_fraud_${report.id}")
                                                ) {
                                                    Text("Review & Take Action", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.45f)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Suspend Seller / Dealer ID", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RoyalSoftGold)
                                        OutlinedTextField(
                                            value = manualSellerId,
                                            onValueChange = { manualSellerId = it },
                                            label = { Text("Seller User ID / Dealer ID *") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("admin_suspend_seller_id_input")
                                        )
                                        OutlinedTextField(
                                            value = manualBusinessName,
                                            onValueChange = { manualBusinessName = it },
                                            label = { Text("Business / Nursery Name") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = manualReason,
                                            onValueChange = { manualReason = it },
                                            label = { Text("Suspension Reason *") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    val res = repo.suspendSeller(
                                                        sellerUserId = manualSellerId,
                                                        businessName = manualBusinessName,
                                                        reason = manualReason,
                                                        adminEmail = adminEmail
                                                    )
                                                    Toast.makeText(
                                                        context,
                                                        if (res.isSuccess) "Seller suspended & audit logged" else (res.exceptionOrNull()?.message ?: "Error"),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    if (res.isSuccess) {
                                                        manualSellerId = ""
                                                        manualBusinessName = ""
                                                        manualReason = ""
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = RoyalAlertText, contentColor = Color.White),
                                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("admin_suspend_submit_btn")
                                        ) {
                                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Suspend Seller Immediately", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            items(suspensions, key = { it.sellerUserId }) { sus ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                    border = BorderStroke(1.dp, if (sus.isSuspended) RoyalAlertText else RoyalBorder),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = sus.businessName.ifBlank { sus.sellerUserId },
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalTextPrimary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Surface(
                                                color = if (sus.isSuspended) RoyalAlertBg else RoyalSuccessBg,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = if (sus.isSuspended) "SUSPENDED" else "RESTORED",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (sus.isSuspended) RoyalAlertText else RoyalSuccessText,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                        Text("ID: ${sus.sellerUserId}", fontSize = 11.sp, color = RoyalSoftGold)
                                        Text("Reason: ${sus.reason}", fontSize = 12.sp, color = RoyalTextSecondary)
                                        if (sus.isSuspended) {
                                            OutlinedButton(
                                                onClick = {
                                                    scope.launch {
                                                        repo.unsuspendSeller(sus.sellerUserId, adminEmail)
                                                        Toast.makeText(context, "Seller restored", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                border = BorderStroke(1.dp, RoyalGold),
                                                modifier = Modifier.heightIn(min = 48.dp).testTag("admin_unsuspend_${sus.sellerUserId}")
                                            ) {
                                                Icon(Icons.Default.GppGood, contentDescription = null, modifier = Modifier.size(15.dp), tint = RoyalGold)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Restore Seller Account", fontSize = 11.sp, color = RoyalSoftGold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            if (grievances.isEmpty()) {
                                item {
                                    EmptyModerationCard("No grievance tickets submitted yet.")
                                }
                            } else {
                                items(grievances, key = { it.id }) { ticket ->
                                    val resolved = ticket.status == GrievanceStatus.RESOLVED
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                        border = BorderStroke(1.dp, RoyalBorder),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(ticket.subject, fontWeight = FontWeight.Bold, color = RoyalTextPrimary, modifier = Modifier.weight(1f))
                                                Surface(
                                                    color = if (resolved) RoyalSuccessBg else RoyalWarningBg,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = ticket.status,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (resolved) RoyalSuccessText else RoyalWarningText,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                            Text("${ticket.category} • ${ticket.userName}", fontSize = 11.sp, color = RoyalSoftGold)
                                            Text(ticket.description, fontSize = 12.sp, color = RoyalTextPrimary)
                                            if (ticket.adminResponse.isNotBlank()) {
                                                Text("Response: ${ticket.adminResponse}", fontSize = 11.sp, color = RoyalSuccessText)
                                            }
                                            Button(
                                                onClick = { grievanceReplyTarget = ticket },
                                                colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                                                modifier = Modifier.heightIn(min = 48.dp)
                                            ) {
                                                Text(if (resolved) "Update Response" else "Respond & Resolve", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        3 -> {
                            if (auditLogs.isEmpty()) {
                                item {
                                    EmptyModerationCard("Audit ledger is empty. All admin moderation actions are automatically recorded.")
                                }
                            } else {
                                items(auditLogs, key = { it.id }) { log ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                        border = BorderStroke(1.dp, RoyalBorder),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoyalGold)
                                                Text(dateFormatter.format(Date(log.createdAtMillis)), fontSize = 10.sp, color = RoyalTextSecondary)
                                            }
                                            Text("${log.targetType} (${log.targetId}) • ${log.adminEmail}", fontSize = 11.sp, color = RoyalSoftGold)
                                            Text(log.summary, fontSize = 11.sp, color = RoyalTextPrimary)
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

    resolveTargetReport?.let { target ->
        var adminNote by remember(target.id) { mutableStateOf("") }
        var actionChoice by remember(target.id) { mutableStateOf("ACTION_TAKEN") }
        var alsoSuspendSeller by remember(target.id) { mutableStateOf(target.sellerUserId.isNotBlank()) }

        AlertDialog(
            onDismissRequest = { resolveTargetReport = null },
            containerColor = RoyalGreenCardBg,
            titleContentColor = RoyalSoftGold,
            title = { Text("Resolve Fraud Report", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(target.targetName.ifBlank { target.targetId }, fontWeight = FontWeight.SemiBold, color = RoyalTextPrimary)
                    Text(target.description, fontSize = 12.sp, color = RoyalTextSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = actionChoice == FraudReportStatus.ACTION_TAKEN,
                            onClick = { actionChoice = FraudReportStatus.ACTION_TAKEN },
                            label = { Text("Action Taken") }
                        )
                        FilterChip(
                            selected = actionChoice == FraudReportStatus.DISMISSED,
                            onClick = { actionChoice = FraudReportStatus.DISMISSED; alsoSuspendSeller = false },
                            label = { Text("Dismiss") }
                        )
                    }
                    if (actionChoice == FraudReportStatus.ACTION_TAKEN && target.sellerUserId.isNotBlank()) {
                        FilterChip(
                            selected = alsoSuspendSeller,
                            onClick = { alsoSuspendSeller = !alsoSuspendSeller },
                            label = { Text("Also Suspend Seller (${target.sellerUserId})") }
                        )
                    }
                    OutlinedTextField(
                        value = adminNote,
                        onValueChange = { adminNote = it },
                        label = { Text("Resolution Note *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repo.resolveFraudReportByAdmin(
                                reportId = target.id,
                                newStatus = actionChoice,
                                resolutionAction = if (alsoSuspendSeller) "SUSPENDED_SELLER" else actionChoice,
                                adminNote = adminNote.ifBlank { "Reviewed by Admin" },
                                adminEmail = adminEmail,
                                suspendSellerToo = alsoSuspendSeller && actionChoice == FraudReportStatus.ACTION_TAKEN
                            )
                            resolveTargetReport = null
                            Toast.makeText(context, "Fraud report updated", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { resolveTargetReport = null }) { Text("Cancel", color = RoyalSoftGold) }
            }
        )
    }

    grievanceReplyTarget?.let { ticket ->
        var responseText by remember(ticket.id) { mutableStateOf(ticket.adminResponse) }
        var newStatus by remember(ticket.id) { mutableStateOf(GrievanceStatus.RESOLVED) }

        AlertDialog(
            onDismissRequest = { grievanceReplyTarget = null },
            containerColor = RoyalGreenCardBg,
            titleContentColor = RoyalSoftGold,
            title = { Text("Grievance Response", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(ticket.subject, fontWeight = FontWeight.SemiBold, color = RoyalTextPrimary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(GrievanceStatus.IN_REVIEW, GrievanceStatus.RESOLVED).forEach { st ->
                            FilterChip(
                                selected = newStatus == st,
                                onClick = { newStatus = st },
                                label = { Text(st) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = responseText,
                        onValueChange = { responseText = it },
                        label = { Text("Official Admin Response") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repo.respondToGrievanceByAdmin(
                                ticketId = ticket.id,
                                newStatus = newStatus,
                                adminResponse = responseText.ifBlank { "Resolved by Trust & Safety team" },
                                adminEmail = adminEmail
                            )
                            grievanceReplyTarget = null
                            Toast.makeText(context, "Grievance updated", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text("Save Response") }
            },
            dismissButton = {
                TextButton(onClick = { grievanceReplyTarget = null }) { Text("Cancel", color = RoyalSoftGold) }
            }
        )
    }
}

@Composable
private fun EmptyModerationCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        border = BorderStroke(1.dp, RoyalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RoyalGold)
            Text(message, fontSize = 12.sp, color = RoyalTextSecondary)
        }
    }
}
