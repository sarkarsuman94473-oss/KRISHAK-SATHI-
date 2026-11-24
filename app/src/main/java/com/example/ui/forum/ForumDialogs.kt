package com.example.ui.forum

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.datasource.CropMasterDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.ForumReportReason
import com.example.data.model.ForumTopic
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.royalOutlinedTextFieldColors
import com.example.util.L10n

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateForumPostDialog(
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onPublish: (title: String, content: String, topic: ForumTopic, cropId: String, cropName: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf(ForumTopic.CROP_CARE) }
    var selectedCropId by remember { mutableStateOf("general") }
    var selectedCropName by remember(currentLanguage) {
        mutableStateOf(L10n.t("forum_general_crop", currentLanguage))
    }
    var showCropPicker by remember { mutableStateOf(false) }

    val canSubmit = title.trim().length >= 3 && content.trim().length >= 5

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .imePadding(),
            shape = RoundedCornerShape(20.dp),
            color = RoyalGreenCardBg,
            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = L10n.t("forum_new_post_btn", currentLanguage),
                        color = RoyalSoftGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("forum_create_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = L10n.t("btn_close", currentLanguage),
                            tint = RoyalTextSecondary
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 160) title = it },
                    label = { Text(L10n.t("forum_post_title_label", currentLanguage)) },
                    placeholder = { Text(L10n.t("forum_post_title_hint", currentLanguage)) },
                    singleLine = true,
                    colors = royalOutlinedTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forum_create_title_input")
                )

                Text(
                    text = L10n.t("forum_select_topic", currentLanguage),
                    color = RoyalSoftGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ForumTopic.values().forEach { topic ->
                        val selected = selectedTopic == topic
                        FilterChip(
                            selected = selected,
                            onClick = { selectedTopic = topic },
                            label = {
                                Text(
                                    text = topic.getLabel(currentLanguage),
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalGreenEmerald,
                                selectedLabelColor = RoyalSoftGold,
                                containerColor = RoyalGreenSecondaryBg,
                                labelColor = RoyalTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = RoyalBorder,
                                selectedBorderColor = RoyalGold
                            ),
                            modifier = Modifier.testTag("forum_create_topic_${topic.code}")
                        )
                    }
                }

                Text(
                    text = L10n.t("forum_select_crop", currentLanguage),
                    color = RoyalSoftGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    color = RoyalGreenSecondaryBg,
                    border = BorderStroke(1.dp, RoyalBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { showCropPicker = true }
                        .testTag("forum_create_crop_picker_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCropName,
                            color = RoyalTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = L10n.t("forum_select_crop", currentLanguage),
                            tint = RoyalGold
                        )
                    }
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = { if (it.length <= 3000) content = it },
                    label = { Text(L10n.t("forum_post_body_label", currentLanguage)) },
                    placeholder = { Text(L10n.t("forum_post_body_hint", currentLanguage)) },
                    minLines = 4,
                    maxLines = 8,
                    colors = royalOutlinedTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forum_create_body_input")
                )

                Button(
                    onClick = {
                        if (canSubmit) {
                            onPublish(
                                title.trim(),
                                content.trim(),
                                selectedTopic,
                                selectedCropId,
                                selectedCropName
                            )
                        }
                    },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("forum_publish_submit_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGold,
                        contentColor = RoyalGreenPrimaryBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = L10n.t("forum_publish_btn", currentLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    if (showCropPicker) {
        ForumCropSelectorDialog(
            currentLanguage = currentLanguage,
            includeAllOption = false,
            onDismiss = { showCropPicker = false },
            onSelectCrop = { cropId, cropLabel ->
                selectedCropId = cropId
                selectedCropName = cropLabel
                showCropPicker = false
            }
        )
    }
}

@Composable
fun ForumCropSelectorDialog(
    currentLanguage: AppLanguage,
    includeAllOption: Boolean,
    onDismiss: () -> Unit,
    onSelectCrop: (cropId: String, cropLabel: String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val allCrops = remember { CropMasterDatabase.crops }
    val filtered = remember(query, allCrops, currentLanguage) {
        if (query.isBlank()) allCrops.take(40)
        else allCrops.filter { crop ->
            crop.getName(currentLanguage).contains(query, ignoreCase = true) ||
                crop.nameEn.contains(query, ignoreCase = true) ||
                crop.cropId.contains(query, ignoreCase = true)
        }.take(60)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        titleContentColor = RoyalGold,
        textContentColor = RoyalTextPrimary,
        title = {
            Text(
                text = L10n.t("forum_select_crop", currentLanguage),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(L10n.t("forum_search_crop_hint", currentLanguage)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RoyalGold) },
                    singleLine = true,
                    colors = royalOutlinedTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (includeAllOption) {
                        item {
                            Surface(
                                color = RoyalGreenSecondaryBg,
                                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clickable {
                                        onSelectCrop("", L10n.t("forum_all_crops", currentLanguage))
                                    }
                            ) {
                                Text(
                                    text = "🌐 ${L10n.t("forum_all_crops", currentLanguage)}",
                                    color = RoyalSoftGold,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                    item {
                        Surface(
                            color = RoyalGreenSecondaryBg,
                            border = BorderStroke(1.dp, RoyalBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable {
                                    onSelectCrop("general", L10n.t("forum_general_crop", currentLanguage))
                                }
                        ) {
                            Text(
                                text = "🌾 ${L10n.t("forum_general_crop", currentLanguage)}",
                                color = RoyalTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    items(filtered, key = { it.cropId }) { crop ->
                        val label = crop.getName(currentLanguage)
                        Surface(
                            color = RoyalGreenSecondaryBg,
                            border = BorderStroke(1.dp, RoyalBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable { onSelectCrop(crop.cropId, label) }
                        ) {
                            Text(
                                text = "${crop.iconEmoji} $label",
                                color = RoyalTextPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(L10n.t("btn_close", currentLanguage), color = RoyalSoftGold)
            }
        }
    )
}

@Composable
fun ReportContentDialog(
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSubmit: (ForumReportReason, String) -> Unit
) {
    var selectedReason by remember { mutableStateOf(ForumReportReason.SPAM) }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        titleContentColor = RoyalGold,
        textContentColor = RoyalTextPrimary,
        title = {
            Text(
                text = L10n.t("forum_report_title", currentLanguage),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = L10n.t("forum_report_reason_label", currentLanguage),
                    color = RoyalTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                ForumReportReason.values().forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable { selectedReason = reason }
                            .testTag("forum_report_reason_${reason.code}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = RoyalGold,
                                unselectedColor = RoyalTextSecondary
                            )
                        )
                        Text(
                            text = reason.getLabel(currentLanguage),
                            color = RoyalTextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
                OutlinedTextField(
                    value = details,
                    onValueChange = { if (it.length <= 500) details = it },
                    placeholder = { Text(L10n.t("forum_report_details_hint", currentLanguage)) },
                    minLines = 2,
                    maxLines = 4,
                    colors = royalOutlinedTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forum_report_details_input")
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(L10n.t("btn_cancel", currentLanguage), color = RoyalTextSecondary)
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedReason, details.trim()) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalGold,
                    contentColor = RoyalGreenPrimaryBg
                ),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("forum_report_submit_btn")
            ) {
                Text(L10n.t("forum_report_submit", currentLanguage), fontWeight = FontWeight.Bold)
            }
        }
    )
}
