package com.example.ui.info

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.local.config.AppConfigManager
import com.example.data.local.entity.GrievanceTicketEntity
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.repository.TrustAndSafetyRepository
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.EarthCardBorder
import com.example.ui.theme.EarthInkPrimary
import com.example.ui.theme.EarthInkSecondary
import com.example.ui.theme.EarthSurfaceWhite
import com.example.ui.theme.EarthTrustBadgeBg
import com.example.ui.theme.EarthTrustBadgeText
import com.example.ui.theme.EarthWarmCream
import com.example.ui.theme.EarthWarnInk
import com.example.ui.theme.EarthWarnSurface
import com.example.util.L10n
import com.example.util.safeStartActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GrievanceRedressalPanel(
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { TrustAndSafetyRepository(context) }
    val session = remember { UserSessionManager.getSession(context) }
    val activeUserId = session?.userId ?: "guest_user"
    val appConfig by AppConfigManager.configState.collectAsState()
    androidx.compose.runtime.LaunchedEffect(Unit) { AppConfigManager.getConfig(context) }

    val tickets by repo.observeGrievancesForUser(activeUserId).collectAsState(initial = emptyList())

    val categories = remember {
        listOf(
            "PAYMENT_FRAUD" to "Payment / Fraud / Scam",
            "MARKETPLACE" to "Marketplace / Fake or Misrepresented Listing",
            "MACHINERY" to "Machinery Quality / Rental / Warranty Dispute",
            "EXPERT" to "Expert Advice / Agronomy Issue",
            "PRIVACY_DATA" to "Privacy / Account Deletion",
            "APP_SUPPORT" to "App Support / Wrong Catalog or Scheme Info"
        )
    }

    var selectedCategory by remember { mutableStateOf("PAYMENT_FRAUD") }
    var subject by remember { mutableStateOf("") }
    var relatedOrderId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("grievance_redressal_panel"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // SLA & Official Statutory Notice
        Card(
            colors = CardDefaults.cardColors(containerColor = EarthSurfaceWhite),
            border = BorderStroke(1.dp, EarthCardBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = AgriGreenPrimary)
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "অভিযোগ ও বিরোধ নিষ্পত্তি কেন্দ্র (৪৮ ঘণ্টা / ৩০ দিন)"
                            AppLanguage.HINDI -> "शिकायत एवं विवाद निवारण केंद्र (48 घंटे / 30 दिन)"
                            AppLanguage.ENGLISH -> "Grievance & Dispute Redressal Centre"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EarthInkPrimary
                    )
                }

                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "• ৪৮ ঘণ্টার মধ্যে প্রাথমিক স্বীকৃতি ও টিকেট নম্বর প্রদান।\n• ৩০ দিনের মধ্যে অভিযোগ নিষ্পত্তির লক্ষ্যমাত্রা।\n• যেকোনো সময় নিচে আপনার টিকেটের কপি শেয়ার/সংরক্ষণ করতে পারবেন।"
                        AppLanguage.HINDI -> "• 48 घंटे के भीतर पावती एवं टिकट संख्या।\n• 30 दिनों के भीतर समाधान का लक्ष्य।\n• नीचे अपने टिकट की प्रति कभी भी साझा/सुरक्षित कर सकते हैं।"
                        AppLanguage.ENGLISH -> "• Target Acknowledgement: Within 48 hours of submission.\n• Target Resolution SLA: Within 30 days under Indian E-Commerce & IT Grievance Rules.\n• Save or share your grievance ticket reference copy anytime below."
                    },
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = EarthInkSecondary
                )

                if (appConfig.hasCompleteStatutoryOfficers()) {
                    Surface(
                        color = EarthTrustBadgeBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, EarthTrustBadgeText.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().testTag("grievance_verified_officers_card")
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "✅ ${L10n.t("grievance_officers_verified_title", currentLanguage)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EarthTrustBadgeText
                            )
                            Text("Grievance Officer: ${appConfig.grievanceOfficerName}", fontSize = 11.5.sp, color = EarthInkPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Email: ${appConfig.grievanceOfficerEmail} • Phone: ${appConfig.grievanceOfficerPhone}", fontSize = 11.sp, color = EarthInkSecondary)
                            Text("Office: ${appConfig.grievanceOfficerAddress}", fontSize = 11.sp, color = EarthInkSecondary)
                            if (appConfig.nodalOfficerName.isNotBlank()) {
                                Text("Nodal Officer: ${appConfig.nodalOfficerName} (${appConfig.nodalOfficerContact})", fontSize = 11.sp, color = EarthInkSecondary)
                            }
                            Text(
                                text = "Entity: ${appConfig.legalEntityName}" + if (appConfig.gstinNumber.isNotBlank()) " • GSTIN: ${appConfig.gstinNumber}" else "",
                                fontSize = 11.sp,
                                color = EarthInkSecondary
                            )
                        }
                    }
                } else {
                    // Release Blocker Notice for real statutory Officer Credentials
                    Surface(
                        color = EarthWarnSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, EarthWarnInk.copy(alpha = 0.4f))
                    ) {
                        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = EarthWarnInk, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Release Blocker Notice: Formal Grievance Officer & Nodal Officer legal name, Indian office address, official phone/email & GSTIN must be entered by Business Owner & verified by Legal Counsel prior to Google Play production launch.",
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = EarthWarnInk,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Official Government Redressal Escalation Portals (Always Verified)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { safeStartActivity(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:1915")), "NCH: 1915") },
                        modifier = Modifier.heightIn(min = 44.dp).testTag("grievance_nch_call_btn")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("NCH Call 1915", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            safeStartActivity(
                                context,
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://consumerhelpline.gov.in/")),
                                "Unable to open consumerhelpline.gov.in"
                            )
                        },
                        modifier = Modifier.heightIn(min = 44.dp).testTag("grievance_nch_web_btn")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("consumerhelpline.gov.in", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { safeStartActivity(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930")), "Cybercrime: 1930") },
                        modifier = Modifier.heightIn(min = 44.dp).testTag("grievance_cyber_call_btn")
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Cyber 1930", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            safeStartActivity(
                                context,
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://cybercrime.gov.in/")),
                                "Unable to open cybercrime.gov.in"
                            )
                        },
                        modifier = Modifier.heightIn(min = 44.dp).testTag("grievance_cyber_web_btn")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("cybercrime.gov.in", fontSize = 11.sp)
                    }
                }
            }
        }

        // Interactive Grievance Filing Form
        Card(
            colors = CardDefaults.cardColors(containerColor = EarthSurfaceWhite),
            border = BorderStroke(1.dp, EarthCardBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "নতুন অভিযোগ / বিরোধ জমা দিন"
                        AppLanguage.HINDI -> "नई शिकायत / विवाद दर्ज करें"
                        AppLanguage.ENGLISH -> "Submit a Grievance / Dispute Ticket"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = EarthInkPrimary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { (code, label) ->
                        FilterChip(
                            selected = selectedCategory == code,
                            onClick = { selectedCategory = code },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "বিষয় (Subject) *"
                                AppLanguage.HINDI -> "विषय (Subject) *"
                                AppLanguage.ENGLISH -> "Subject *"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("grievance_subject_input")
                )

                OutlinedTextField(
                    value = relatedOrderId,
                    onValueChange = { relatedOrderId = it },
                    label = { Text("Order / Listing / Inquiry ID (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "বিস্তারিত অভিযোগ (ন্যূনতম ১০ অক্ষর) *"
                                AppLanguage.HINDI -> "विस्तृत विवरण (न्यूनतम 10 अक्षर) *"
                                AppLanguage.ENGLISH -> "Full Complaint Details (min 10 chars) *"
                            }
                        )
                    },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("grievance_desc_input")
                )

                Button(
                    enabled = !submitting && subject.trim().length >= 4 && description.trim().length >= 8,
                    onClick = {
                        submitting = true
                        scope.launch {
                            val res = repo.submitGrievanceTicket(
                                userId = activeUserId,
                                userName = session?.userName ?: "Farmer User",
                                category = selectedCategory,
                                subject = subject.trim(),
                                description = description.trim(),
                                relatedEntityId = relatedOrderId.trim()
                            )
                            submitting = false
                            if (res.isSuccess) {
                                val t = res.getOrThrow()
                                subject = ""
                                relatedOrderId = ""
                                description = ""
                                Toast.makeText(
                                    context,
                                    "Ticket ${t.id} submitted (48h acknowledgement SLA)",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    res.exceptionOrNull()?.message ?: "Failed to submit ticket",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("grievance_submit_btn")
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "অভিযোগ টিকেট জমা দিন"
                            AppLanguage.HINDI -> "शिकायत टिकट दर्ज करें"
                            AppLanguage.ENGLISH -> "Submit Grievance Ticket"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // User's Ticket History
        Text(
            text = when (currentLanguage) {
                AppLanguage.BENGALI -> "আপনার জমা দেওয়া টিকেটসমূহ (${tickets.size})"
                AppLanguage.HINDI -> "आपके दर्ज किए गए टिकट (${tickets.size})"
                AppLanguage.ENGLISH -> "Your Submitted Grievance Tickets (${tickets.size})"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = EarthInkPrimary
        )

        if (tickets.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = EarthWarmCream),
                border = BorderStroke(1.dp, EarthCardBorder)
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "এখনো কোনো অভিযোগ টিকেট জমা দেওয়া হয়নি।"
                        AppLanguage.HINDI -> "अभी तक कोई शिकायत टिकट दर्ज नहीं किया गया है।"
                        AppLanguage.ENGLISH -> "No grievance tickets submitted yet."
                    },
                    fontSize = 12.sp,
                    color = EarthInkSecondary,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            tickets.forEach { ticket ->
                TicketStatusCard(ticket = ticket, onShareCopy = {
                    val fmt = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                    val ackByMillis = ticket.createdAtMillis + 48L * 3600_000L
                    val resolveByMillis = ticket.createdAtMillis + 30L * 24 * 3600_000L
                    val textCopy = buildString {
                        append("KRISHAK SATHI — GRIEVANCE TICKET RECEIPT\n")
                        append("Ticket ID: ${ticket.id}\n")
                        append("Category: ${ticket.category}\n")
                        append("Subject: ${ticket.subject}\n")
                        append("Status: ${ticket.status}\n")
                        append("Filed: ${fmt.format(Date(ticket.createdAtMillis))}\n")
                        append("Ack SLA By: ${fmt.format(Date(ackByMillis))}\n")
                        append("Resolution SLA By: ${fmt.format(Date(resolveByMillis))}\n")
                        append("Details: ${ticket.description}\n")
                        if (ticket.adminResponse.isNotBlank()) {
                            append("Official Response: ${ticket.adminResponse}\n")
                        }
                    }
                    runCatching {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Grievance Ticket ${ticket.id}")
                            putExtra(Intent.EXTRA_TEXT, textCopy)
                        }
                        context.startActivity(Intent.createChooser(send, "Save / Share Grievance Receipt"))
                    }
                })
            }
        }
    }
}

@Composable
private fun TicketStatusCard(
    ticket: GrievanceTicketEntity,
    onShareCopy: () -> Unit
) {
    val fmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    Card(
        colors = CardDefaults.cardColors(containerColor = EarthSurfaceWhite),
        border = BorderStroke(1.dp, EarthCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("grievance_ticket_${ticket.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(ticket.id, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EarthTrustBadgeText)
                Surface(color = EarthTrustBadgeBg, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = ticket.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EarthTrustBadgeText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text("${ticket.category} • ${ticket.subject}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EarthInkPrimary)
            Text(ticket.description, fontSize = 11.5.sp, color = EarthInkSecondary)
            val ackByMillis = remember(ticket.createdAtMillis) { ticket.createdAtMillis + 48L * 3600_000L }
            val resolveByMillis = remember(ticket.createdAtMillis) { ticket.createdAtMillis + 30L * 24 * 3600_000L }
            Text(
                text = "Ack by: ${fmt.format(Date(ackByMillis))} • Target resolution: ${fmt.format(Date(resolveByMillis))}",
                fontSize = 10.sp,
                color = EarthInkSecondary
            )
            if (ticket.adminResponse.isNotBlank()) {
                Surface(color = EarthWarmCream, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = "Admin Response: ${ticket.adminResponse}",
                        fontSize = 11.sp,
                        color = EarthInkPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            OutlinedButton(
                onClick = onShareCopy,
                modifier = Modifier.heightIn(min = 42.dp).testTag("share_ticket_${ticket.id}")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Save / Share Ticket Copy", fontSize = 11.sp)
            }
        }
    }
}
