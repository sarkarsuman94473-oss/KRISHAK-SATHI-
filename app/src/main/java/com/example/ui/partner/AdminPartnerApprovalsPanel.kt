package com.example.ui.partner

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.AppUserRole
import com.example.data.model.PartnerRegistration
import com.example.data.model.PartnerRegistrationStatus
import com.example.data.remote.PartnerRoleRepository
import com.example.ui.theme.AgriAlertRed
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalSuccessBg
import com.example.ui.theme.RoyalSuccessText
import com.example.ui.theme.RoyalWarningBg
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminPartnerApprovalsPanel(
    currentLanguage: AppLanguage,
    adminEmail: String,
    partnerRepository: PartnerRoleRepository = rememberPartnerRepository()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val registrations by partnerRepository.allRegistrationsFlow.collectAsState(initial = emptyList())

    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var approveTarget by remember { mutableStateOf<PartnerRegistration?>(null) }
    var selectedRolesForApproval by remember { mutableStateOf<Set<AppUserRole>>(emptySet()) }
    var approveAdminNote by remember { mutableStateOf("") }
    var rejectTarget by remember { mutableStateOf<PartnerRegistration?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    val filtered = remember(registrations, selectedStatusFilter) {
        if (selectedStatusFilter == "ALL") {
            registrations
        } else {
            registrations.filter { it.status.equals(selectedStatusFilter, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filterChips = listOf(
                    "ALL" to when (currentLanguage) {
                        AppLanguage.BENGALI -> "সব (${registrations.size})"
                        AppLanguage.HINDI -> "सभी (${registrations.size})"
                        AppLanguage.ENGLISH -> "All (${registrations.size})"
                    },
                    PartnerRegistrationStatus.PENDING.code to PartnerRegistrationStatus.PENDING.getLabel(currentLanguage),
                    PartnerRegistrationStatus.UNDER_REVIEW.code to PartnerRegistrationStatus.UNDER_REVIEW.getLabel(currentLanguage),
                    PartnerRegistrationStatus.APPROVED.code to PartnerRegistrationStatus.APPROVED.getLabel(currentLanguage),
                    PartnerRegistrationStatus.REJECTED.code to PartnerRegistrationStatus.REJECTED.getLabel(currentLanguage)
                )
                items(filterChips) { (code, label) ->
                    FilterChip(
                        selected = selectedStatusFilter == code,
                        onClick = { selectedStatusFilter = code },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(40.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "কোনো পার্টনার আবেদন নেই"
                                AppLanguage.HINDI -> "कोई पार्टनर आवेदन नहीं है"
                                AppLanguage.ENGLISH -> "No partner registrations yet"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { reg ->
                val statusEnum = PartnerRegistrationStatus.fromCode(reg.status) ?: PartnerRegistrationStatus.PENDING
                val isApproved = reg.isVerifiedPartner()
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        if (isApproved) RoyalSuccessText.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_partner_card_${reg.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(reg.businessName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    "${reg.applicantName} • ${reg.contactPhone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = when (statusEnum) {
                                    PartnerRegistrationStatus.APPROVED -> RoyalSuccessBg
                                    PartnerRegistrationStatus.REJECTED -> com.example.ui.theme.RoyalAlertBg
                                    else -> RoyalWarningBg
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = statusEnum.getLabel(currentLanguage),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (statusEnum) {
                                        PartnerRegistrationStatus.APPROVED -> RoyalSuccessText
                                        PartnerRegistrationStatus.REJECTED -> AgriAlertRed
                                        else -> RoyalGold
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "${reg.businessLocation}, ${reg.district} • ${reg.serviceArea} (${reg.serviceRadiusKm} km)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (reg.licenseNumber.isNotBlank() || reg.verificationDocumentNames.isNotEmpty()) {
                            Text(
                                text = "${reg.documentType}: ${reg.licenseNumber.ifBlank { "-" }} | Files: ${reg.verificationDocumentNames.joinToString(", ").ifBlank { "None" }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            reg.requestedPartnerRoles().forEach { role ->
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = role.getLabel(currentLanguage),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider()

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (statusEnum == PartnerRegistrationStatus.PENDING) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val res = partnerRepository.adminMarkUnderReview(
                                                    regId = reg.id,
                                                    adminNote = "Verification initiated by admin",
                                                    adminEmail = adminEmail,
                                                    callerIsAdmin = UserSessionManager.isAdmin(context)
                                                )
                                                if (res.isFailure) {
                                                    Toast.makeText(
                                                        context,
                                                        res.exceptionOrNull()?.localizedMessage ?: "Failed",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } catch (e: CancellationException) {
                                                throw e
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("admin_partner_review_btn_${reg.id}")
                                ) {
                                    Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "পর্যালোচনা শুরু"
                                            AppLanguage.HINDI -> "समीक्षा शुरू"
                                            AppLanguage.ENGLISH -> "Mark Under Review"
                                        }
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    approveTarget = reg
                                    selectedRolesForApproval = reg.requestedPartnerRoles().toSet()
                                    approveAdminNote = reg.adminNotes
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AgriGreenPrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("admin_partner_approve_btn_${reg.id}")
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অনুমোদন"
                                        AppLanguage.HINDI -> "स्वीकृत करें"
                                        AppLanguage.ENGLISH -> "Approve Roles"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    rejectTarget = reg
                                    rejectReason = ""
                                },
                                border = BorderStroke(1.dp, AgriAlertRed.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("admin_partner_reject_btn_${reg.id}")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = AgriAlertRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "বাতিল"
                                        AppLanguage.HINDI -> "अस्वीकार"
                                        AppLanguage.ENGLISH -> "Reject"
                                    },
                                    color = AgriAlertRed
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    approveTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { approveTarget = null },
            title = {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "পার্টনার ভূমিকা অনুমোদন"
                        AppLanguage.HINDI -> "पार्टनर भूमिका अनुमोदन"
                        AppLanguage.ENGLISH -> "Approve Partner Roles"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(target.businessName, fontWeight = FontWeight.SemiBold)
                    AppUserRole.partnerRegistrationChoices.forEach { role ->
                        val checked = role in selectedRolesForApproval
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    selectedRolesForApproval = if (isChecked) {
                                        selectedRolesForApproval + role
                                    } else {
                                        selectedRolesForApproval - role
                                    }
                                }
                            )
                            Text(role.getLabel(currentLanguage), fontSize = 13.sp)
                        }
                    }
                    OutlinedTextField(
                        value = approveAdminNote,
                        onValueChange = { approveAdminNote = it },
                        label = {
                            Text(
                                when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অ্যাডমিন নোট (ঐচ্ছিক)"
                                    AppLanguage.HINDI -> "एडमिन नोट (वैकल्पिक)"
                                    AppLanguage.ENGLISH -> "Admin Note (Optional)"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedRolesForApproval.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            try {
                                val res = partnerRepository.adminApproveRegistration(
                                    regId = target.id,
                                    grantedRoleCodes = selectedRolesForApproval.map { it.code },
                                    adminNote = approveAdminNote,
                                    adminEmail = adminEmail,
                                    callerIsAdmin = UserSessionManager.isAdmin(context)
                                )
                                if (res.isSuccess) {
                                    Toast.makeText(
                                        context,
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "${target.businessName} অনুমোদিত হয়েছে"
                                            AppLanguage.HINDI -> "${target.businessName} स्वीकृत हो गया"
                                            AppLanguage.ENGLISH -> "${target.businessName} approved"
                                        },
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    approveTarget = null
                                } else {
                                    Toast.makeText(
                                        context,
                                        res.exceptionOrNull()?.localizedMessage ?: "Approval failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: CancellationException) {
                                throw e
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                    modifier = Modifier.testTag("admin_partner_confirm_approve_btn")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "নিশ্চিত অনুমোদন"
                            AppLanguage.HINDI -> "अनुमोदन की पुष्टि"
                            AppLanguage.ENGLISH -> "Confirm Approval"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { approveTarget = null }) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "বন্ধ করুন"
                            AppLanguage.HINDI -> "बंद करें"
                            AppLanguage.ENGLISH -> "Cancel"
                        }
                    )
                }
            }
        )
    }

    rejectTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { rejectTarget = null },
            title = {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "পার্টনার আবেদন বাতিল"
                        AppLanguage.HINDI -> "पार्टनर आवेदन अस्वीकार"
                        AppLanguage.ENGLISH -> "Reject Partner Application"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "বাতিলের কারণ লিখুন *"
                                AppLanguage.HINDI -> "अस्वीकृति का कारण लिखें *"
                                AppLanguage.ENGLISH -> "Rejection Reason *"
                            }
                        )
                    },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_partner_reject_reason_input")
                )
            },
            confirmButton = {
                Button(
                    enabled = rejectReason.isNotBlank(),
                    onClick = {
                        scope.launch {
                            try {
                                val res = partnerRepository.adminRejectRegistration(
                                    regId = target.id,
                                    reason = rejectReason,
                                    adminEmail = adminEmail,
                                    callerIsAdmin = UserSessionManager.isAdmin(context)
                                )
                                if (res.isSuccess) {
                                    rejectTarget = null
                                } else {
                                    Toast.makeText(
                                        context,
                                        res.exceptionOrNull()?.localizedMessage ?: "Failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: CancellationException) {
                                throw e
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriAlertRed, contentColor = Color.White),
                    modifier = Modifier.testTag("admin_partner_confirm_reject_btn")
                ) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "বাতিল নিশ্চিত করুন"
                            AppLanguage.HINDI -> "अस्वीकार पुष्टि करें"
                            AppLanguage.ENGLISH -> "Confirm Reject"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectTarget = null }) {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "ফিরে যান"
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
private fun rememberPartnerRepository(): PartnerRoleRepository {
    val context = LocalContext.current
    return remember(context) { PartnerRoleRepository(context) }
}
