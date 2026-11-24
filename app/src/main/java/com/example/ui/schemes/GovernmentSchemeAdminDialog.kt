package com.example.ui.schemes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppLanguage
import com.example.data.model.GovernmentScheme
import com.example.data.model.SchemeActivityFocus
import com.example.data.model.SchemeApplicationStatus
import com.example.data.model.SchemeCategory
import com.example.data.model.SchemeGovLevel
import com.example.data.model.isValidOfficialHttpsUrl
import com.example.ui.theme.RoyalAlertBg
import com.example.ui.theme.RoyalAlertText
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.RoyalWarningBg
import com.example.ui.theme.royalOutlinedTextFieldColors
import com.example.util.L10n

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GovernmentSchemeAdminDialog(
    initialScheme: GovernmentScheme,
    isNew: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (scheme: GovernmentScheme, allowUrlOverwrite: Boolean) -> Unit
) {
    var nameEn by remember { mutableStateOf(initialScheme.nameEn) }
    var nameBn by remember { mutableStateOf(initialScheme.nameBn) }
    var nameHi by remember { mutableStateOf(initialScheme.nameHi) }
    var deptEn by remember { mutableStateOf(initialScheme.departmentEn) }
    var deptBn by remember { mutableStateOf(initialScheme.departmentBn) }
    var deptHi by remember { mutableStateOf(initialScheme.departmentHi) }
    var officialUrl by remember { mutableStateOf(initialScheme.officialUrl) }
    var sourceUrl by remember { mutableStateOf(initialScheme.officialSourceUrl) }
    var shortDescEn by remember { mutableStateOf(initialScheme.shortDescEn) }
    var shortDescBn by remember { mutableStateOf(initialScheme.shortDescBn) }
    var shortDescHi by remember { mutableStateOf(initialScheme.shortDescHi) }
    var eligibilityEn by remember { mutableStateOf(initialScheme.eligibilityEn) }
    var eligibilityBn by remember { mutableStateOf(initialScheme.eligibilityBn) }
    var eligibilityHi by remember { mutableStateOf(initialScheme.eligibilityHi) }
    var documentsEn by remember { mutableStateOf(initialScheme.documentsEn) }
    var benefitsEn by remember { mutableStateOf(initialScheme.benefitsEn) }
    var procedureEn by remember { mutableStateOf(initialScheme.procedureEn) }
    var verifiedDate by remember { mutableStateOf(initialScheme.lastVerifiedDate) }
    var govLevel by remember { mutableStateOf(initialScheme.resolveGovLevel()) }
    var category by remember { mutableStateOf(initialScheme.resolveCategory()) }
    var activityFocus by remember { mutableStateOf(initialScheme.resolveActivityFocus()) }
    var status by remember { mutableStateOf(initialScheme.resolveApplicationStatus()) }
    var discontinued by remember { mutableStateOf(initialScheme.discontinued) }
    var confirmUrlOverwrite by remember { mutableStateOf(false) }

    val urlChanged = !isNew &&
        initialScheme.officialUrl.isNotBlank() &&
        officialUrl.trim() != initialScheme.officialUrl.trim()
    val officialUrlValid = isValidOfficialHttpsUrl(officialUrl)
    val effectiveSource = sourceUrl.trim().ifBlank { officialUrl.trim() }
    val sourceUrlValid = isValidOfficialHttpsUrl(effectiveSource)
    val titleValid = nameEn.isNotBlank() || nameBn.isNotBlank() || nameHi.isNotBlank()
    val canSave = titleValid && officialUrlValid && sourceUrlValid && (!urlChanged || confirmUrlOverwrite)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = RoyalGreenCardBg,
            contentColor = RoyalTextPrimary,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, RoyalBorder),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 560.dp)
                .heightIn(max = 660.dp)
                .testTag("scheme_admin_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isNew) {
                        L10n.t("schemes_admin_add", currentLanguage)
                    } else {
                        L10n.t("schemes_admin_edit", currentLanguage)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = RoyalGold
                )
                HorizontalDivider(color = RoyalBorder)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("Scheme Name (English)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("scheme_admin_name_en"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text("প্রকল্পের নাম (বাংলা)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = nameHi,
                    onValueChange = { nameHi = it },
                    label = { Text("योजना का नाम (हिंदी)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = deptEn,
                    onValueChange = { deptEn = it },
                    label = { Text("Department / Ministry (En)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = deptBn,
                    onValueChange = { deptBn = it },
                    label = { Text("দপ্তর (বাংলা)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = deptHi,
                    onValueChange = { deptHi = it },
                    label = { Text("विभाग (हिंदी)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = officialUrl,
                    onValueChange = { officialUrl = it },
                    label = { Text(L10n.t("schemes_official_portal_url_label", currentLanguage)) },
                    placeholder = { Text("https://example.gov.in/") },
                    isError = officialUrl.isNotBlank() && !officialUrlValid,
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("scheme_admin_official_url"),
                    singleLine = true
                )
                if (officialUrl.isNotBlank() && !officialUrlValid) {
                    Surface(
                        color = RoyalAlertBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = L10n.t("schemes_https_error", currentLanguage),
                            color = RoyalAlertText,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (urlChanged) {
                    Surface(
                        color = RoyalWarningBg,
                        border = BorderStroke(1.dp, RoyalGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Checkbox(
                                checked = confirmUrlOverwrite,
                                onCheckedChange = { confirmUrlOverwrite = it },
                                colors = CheckboxDefaults.colors(checkedColor = RoyalGold),
                                modifier = Modifier.testTag("scheme_admin_confirm_url_overwrite")
                            )
                            Text(
                                text = L10n.t("schemes_confirm_url_overwrite", currentLanguage),
                                color = RoyalSoftGold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = sourceUrl,
                    onValueChange = { sourceUrl = it },
                    label = { Text("Official Info Source URL (https://)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = verifiedDate,
                    onValueChange = { verifiedDate = it },
                    label = { Text(L10n.t("schemes_verified_label", currentLanguage)) },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = L10n.t("schemes_filter_gov_level", currentLanguage),
                    color = RoyalSoftGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SchemeGovLevel.entries.forEach { gl ->
                        FilterChip(
                            selected = govLevel == gl,
                            onClick = { govLevel = gl },
                            label = { Text(gl.getLabel(currentLanguage), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalGreenEmerald,
                                selectedLabelColor = RoyalSoftGold
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                Text(
                    text = L10n.t("schemes_categories_label", currentLanguage),
                    color = RoyalSoftGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SchemeCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.getLabel(currentLanguage), fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalGreenEmerald,
                                selectedLabelColor = RoyalSoftGold
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                Text(
                    text = L10n.t("schemes_filter_activity", currentLanguage),
                    color = RoyalSoftGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SchemeActivityFocus.entries.forEach { af ->
                        FilterChip(
                            selected = activityFocus == af,
                            onClick = { activityFocus = af },
                            label = { Text(af.getLabel(currentLanguage), fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalGreenEmerald,
                                selectedLabelColor = RoyalSoftGold
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                Text(
                    text = L10n.t("schemes_filter_status", currentLanguage),
                    color = RoyalSoftGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SchemeApplicationStatus.entries.forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = {
                                status = st
                                discontinued = (st == SchemeApplicationStatus.DISCONTINUED)
                            },
                            label = { Text(st.getLabel(currentLanguage), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalGreenEmerald,
                                selectedLabelColor = RoyalSoftGold
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = L10n.t("schemes_admin_mark_discontinued", currentLanguage),
                        color = RoyalTextPrimary,
                        fontSize = 13.sp
                    )
                    Switch(
                        checked = discontinued,
                        onCheckedChange = {
                            discontinued = it
                            if (it) status = SchemeApplicationStatus.DISCONTINUED
                            else if (status == SchemeApplicationStatus.DISCONTINUED) status = SchemeApplicationStatus.INFO_ONLY
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = RoyalGold),
                        modifier = Modifier.testTag("scheme_admin_discontinued_switch")
                    )
                }

                OutlinedTextField(
                    value = shortDescEn,
                    onValueChange = { shortDescEn = it },
                    label = { Text("Short Summary (En)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = shortDescBn,
                    onValueChange = { shortDescBn = it },
                    label = { Text("সংক্ষিপ্ত বিবরণ (বাংলা)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = shortDescHi,
                    onValueChange = { shortDescHi = it },
                    label = { Text("संक्षिप्त विवरण (हिंदी)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = eligibilityEn,
                    onValueChange = { eligibilityEn = it },
                    label = { Text("Eligibility Criteria (En)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = eligibilityBn,
                    onValueChange = { eligibilityBn = it },
                    label = { Text("যোগ্যতা (বাংলা)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = eligibilityHi,
                    onValueChange = { eligibilityHi = it },
                    label = { Text("पात्रता मानदंड (हिंदी)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = documentsEn,
                    onValueChange = { documentsEn = it },
                    label = { Text("Required Documents (En)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = benefitsEn,
                    onValueChange = { benefitsEn = it },
                    label = { Text("Benefits / Assistance (En)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = procedureEn,
                    onValueChange = { procedureEn = it },
                    label = { Text("Application Procedure (En)") },
                    colors = royalOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                }
                HorizontalDivider(color = RoyalBorder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, RoyalBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalTextSecondary),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(L10n.t("btn_cancel", currentLanguage))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        enabled = canSave,
                        onClick = {
                            val updated = initialScheme.copy(
                                govLevel = govLevel.name,
                                category = category.name,
                                activityFocus = activityFocus.name,
                                applicationStatus = status.name,
                                discontinued = discontinued,
                                officialUrl = officialUrl.trim(),
                                officialSourceUrl = effectiveSource,
                                lastVerifiedDate = verifiedDate.trim(),
                                nameEn = nameEn.trim(),
                                nameBn = nameBn.trim(),
                                nameHi = nameHi.trim(),
                                departmentEn = deptEn.trim(),
                                departmentBn = deptBn.trim(),
                                departmentHi = deptHi.trim(),
                                shortDescEn = shortDescEn.trim(),
                                shortDescBn = shortDescBn.trim(),
                                shortDescHi = shortDescHi.trim(),
                                eligibilityEn = eligibilityEn.trim(),
                                eligibilityBn = eligibilityBn.trim(),
                                eligibilityHi = eligibilityHi.trim(),
                                documentsEn = documentsEn.trim(),
                                benefitsEn = benefitsEn.trim(),
                                procedureEn = procedureEn.trim(),
                                northBengalPriority = if (govLevel == SchemeGovLevel.WEST_BENGAL) 92 else initialScheme.northBengalPriority
                            )
                            onSave(updated, !urlChanged || confirmUrlOverwrite)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalGreenEmerald,
                            contentColor = RoyalTextPrimary
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("scheme_admin_save_btn")
                    ) {
                        Text(L10n.t("btn_save", currentLanguage), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
