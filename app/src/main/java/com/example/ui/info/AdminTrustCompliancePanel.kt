package com.example.ui.info

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.local.entity.FraudReportEntity
import com.example.data.local.entity.FraudReportStatus
import com.example.data.local.entity.GrievanceStatus
import com.example.data.local.entity.GrievanceTicketEntity
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.repository.TrustAndSafetyRepository
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.ui.theme.EarthTrustBadgeBg
import com.example.ui.theme.EarthTrustBadgeText
import com.example.ui.theme.EarthWarnInk
import com.example.ui.theme.EarthWarnSurface
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminTrustCompliancePanel(
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { TrustAndSafetyRepository(context) }
    val adminEmail = remember { UserSessionManager.getSession(context)?.email ?: UserSessionManager.ADMIN_EMAIL }

    val fraudReports by repo.observeAllFraudReports().collectAsState(initial = emptyList())
    val tickets by repo.observeAllGrievances().collectAsState(initial = emptyList())
    val suspensions by repo.observeAllSuspensions().collectAsState(initial = emptyList())
    val auditLogs by repo.observeAllAuditLogs().collectAsState(initial = emptyList())

    var expandedSection by remember { mutableStateOf(true) }
    var moderatingReport by remember { mutableStateOf<FraudReportEntity?>(null) }
    var respondingTicket by remember { mutableStateOf<GrievanceTicketEntity?>(null) }
    var showMatrix by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_trust_compliance_panel")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedSection = !expandedSection },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = AgriGreenPrimary)
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "নিরাপত্তা, প্রতারণা ও অভিযোগ মডারেটর"
                            AppLanguage.HINDI -> "सुरक्षा, धोखाधड़ी व शिकायत मॉडरेशन"
                            AppLanguage.ENGLISH -> "Trust, Fraud & Grievance Moderation"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = if (expandedSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = expandedSection) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val activeSuspensions = suspensions.filter { it.isSuspended }
                    val openReports = fraudReports.filter {
                        it.status == FraudReportStatus.OPEN || it.status == FraudReportStatus.UNDER_REVIEW
                    }
                    val openTickets = tickets.filter {
                        it.status != GrievanceStatus.RESOLVED
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatBadge("Open Reports: ${openReports.size}")
                        StatBadge("Suspended Sellers: ${activeSuspensions.size}")
                        StatBadge("Open Grievances: ${openTickets.size}")
                        StatBadge("Audit Logs: ${auditLogs.size}")
                    }

                    // Fraud Reports Queue
                    Text("Fraud & Safety Reports (${fraudReports.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (fraudReports.isEmpty()) {
                        Text("No fraud reports filed.", fontSize = 12.sp, color = AgriTextSecondary)
                    } else {
                        fraudReports.take(5).forEach { rep ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("${rep.targetName} (${rep.reasonCode}) • ${rep.status}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(rep.description, fontSize = 11.sp, color = AgriTextSecondary)
                                    if (rep.status == FraudReportStatus.OPEN || rep.status == FraudReportStatus.UNDER_REVIEW) {
                                        OutlinedButton(
                                            onClick = { moderatingReport = rep },
                                            modifier = Modifier.heightIn(min = 42.dp).testTag("admin_moderate_report_${rep.id}")
                                        ) {
                                            Text("Review / Suspend", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // Active Seller Suspensions
                    Text("Active Seller Suspensions (${activeSuspensions.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    activeSuspensions.forEach { susp ->
                        Surface(
                            color = EarthWarnSurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(susp.businessName.ifBlank { susp.sellerUserId }, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EarthWarnInk)
                                    Text(susp.reason, fontSize = 11.sp, color = EarthWarnInk)
                                }
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            repo.unsuspendSeller(susp.sellerUserId, adminEmail, "Restored after verification")
                                            Toast.makeText(context, "Seller unsuspended", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.heightIn(min = 42.dp).testTag("admin_unsuspend_${susp.sellerUserId}")
                                ) {
                                    Text("Unsuspend", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // Grievance Tickets Queue
                    Text("Grievance Redressal Queue (${tickets.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    tickets.take(5).forEach { t ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${t.id} • ${t.category} • ${t.status}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(t.subject, fontSize = 11.5.sp)
                                OutlinedButton(
                                    onClick = { respondingTicket = t },
                                    modifier = Modifier.heightIn(min = 42.dp).testTag("admin_respond_ticket_${t.id}")
                                ) {
                                    Text("Respond & Update SLA", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // Indian Legal Compliance Matrix Toggle
                    OutlinedButton(
                        onClick = { showMatrix = !showMatrix },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("admin_toggle_compliance_matrix_btn")
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (showMatrix) "Hide Indian Legal Compliance Matrix (11 Frameworks)"
                            else "View Indian Legal Compliance Matrix & Release Blockers",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (showMatrix) {
                        LegalPolicyCatalog.complianceMatrix.forEach { item ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("${item.frameworkName} — [${item.implementationStatus}]", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                    Text("Owner: ${item.ownerRole} • Reviewed: ${item.reviewDate}", fontSize = 10.sp, color = AgriTextSecondary)
                                    Text(item.gapOrActionNote, fontSize = 11.sp)
                                }
                            }
                        }
                        Surface(
                            color = EarthWarnSurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("Production Release Blockers (Manual Business/Legal Action):", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = EarthWarnInk)
                                LegalPolicyCatalog.releaseBlockers.forEachIndexed { idx, blocker ->
                                    Text("${idx + 1}. $blocker", fontSize = 10.5.sp, color = EarthWarnInk)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    moderatingReport?.let { rep ->
        var adminNote by remember { mutableStateOf("") }
        var statusChoice by remember { mutableStateOf(FraudReportStatus.ACTION_TAKEN) }
        AlertDialog(
            onDismissRequest = { moderatingReport = null },
            title = { Text("Moderate Report: ${rep.targetName}", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Reason: ${rep.reasonCode}\n${rep.description}", fontSize = 12.sp)
                    OutlinedTextField(
                        value = adminNote,
                        onValueChange = { adminNote = it },
                        label = { Text("Resolution Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    repo.resolveFraudReportByAdmin(
                                        reportId = rep.id,
                                        newStatus = statusChoice,
                                        resolutionAction = "SUSPENDED_SELLER",
                                        adminNote = adminNote.ifBlank { rep.reasonCode },
                                        adminEmail = adminEmail,
                                        suspendSellerToo = rep.sellerUserId.isNotBlank()
                                    )
                                    if (rep.sellerUserId.isBlank() && rep.targetId.isNotBlank()) {
                                        repo.suspendSeller(rep.targetId, rep.targetName, adminNote.ifBlank { rep.reasonCode }, adminEmail)
                                    }
                                    Toast.makeText(context, "Report resolved & target suspended", Toast.LENGTH_SHORT).show()
                                    moderatingReport = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EarthWarnInk, contentColor = Color.White)
                        ) { Text("Resolve & Suspend Seller", fontSize = 11.sp) }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    repo.resolveFraudReportByAdmin(
                                        reportId = rep.id,
                                        newStatus = FraudReportStatus.DISMISSED,
                                        resolutionAction = "DISMISSED",
                                        adminNote = adminNote.ifBlank { "No violation verified" },
                                        adminEmail = adminEmail,
                                        suspendSellerToo = false
                                    )
                                    Toast.makeText(context, "Report dismissed", Toast.LENGTH_SHORT).show()
                                    moderatingReport = null
                                }
                            }
                        ) { Text("Dismiss", fontSize = 11.sp) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { moderatingReport = null }) { Text("Close") }
            }
        )
    }

    respondingTicket?.let { t ->
        var replyText by remember { mutableStateOf(t.adminResponse) }
        AlertDialog(
            onDismissRequest = { respondingTicket = null },
            title = { Text("Respond to Ticket ${t.id}", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${t.category}: ${t.subject}\n${t.description}", fontSize = 12.sp)
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Official Response / Resolution") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = replyText.trim().length >= 3,
                    onClick = {
                        scope.launch {
                            repo.respondToGrievanceByAdmin(t.id, GrievanceStatus.RESOLVED, replyText.trim(), adminEmail)
                            Toast.makeText(context, "Ticket marked RESOLVED", Toast.LENGTH_SHORT).show()
                            respondingTicket = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White)
                ) { Text("Resolve Ticket") }
            },
            dismissButton = {
                TextButton(onClick = { respondingTicket = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatBadge(text: String) {
    Surface(color = EarthTrustBadgeBg, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = EarthTrustBadgeText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
