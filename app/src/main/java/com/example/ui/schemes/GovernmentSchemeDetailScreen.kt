package com.example.ui.schemes

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.GovernmentScheme
import com.example.data.model.SchemeApplicationStatus
import com.example.data.model.SchemeFallbackText
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenHighlight
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.util.L10n
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GovernmentSchemeDetailScreen(
    scheme: GovernmentScheme,
    currentLanguage: AppLanguage,
    isAdmin: Boolean,
    isBookmarked: Boolean = false,
    onToggleBookmark: () -> Unit = {},
    onBack: () -> Unit,
    onVisitOfficialWebsite: (String) -> Unit,
    onAdminEdit: () -> Unit,
    onAdminUpdateStatus: (SchemeApplicationStatus, String) -> Unit
) {
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val status = scheme.resolveApplicationStatus()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = RoyalGreenPrimaryBg,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RoyalGreenSecondaryBg,
                    titleContentColor = RoyalTextPrimary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("scheme_detail_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = L10n.t("btn_back", currentLanguage),
                            tint = RoyalGold
                        )
                    }
                },
                title = {
                    Text(
                        text = scheme.getName(currentLanguage),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("scheme_detail_bookmark_btn")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = L10n.t(
                                if (isBookmarked) "schemes_bookmark_remove" else "schemes_bookmark_add",
                                currentLanguage
                            ),
                            tint = RoyalGold
                        )
                    }
                    if (isAdmin) {
                        IconButton(
                            onClick = onAdminEdit,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("scheme_detail_admin_edit_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = L10n.t("schemes_admin_edit", currentLanguage),
                                tint = RoyalGold
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(RoyalGreenPrimaryBg)
                .testTag("scheme_detail_screen"),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 920.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mandatory Safety & Transparency Notice
                item {
                    Surface(
                        color = RoyalGreenSecondaryBg,
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scheme_detail_disclaimer")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = RoyalGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = L10n.t("schemes_disclaimer_title", currentLanguage),
                                    color = RoyalSoftGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = SchemeFallbackText.disclaimer(currentLanguage),
                                    color = RoyalTextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Header Summary Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = RoyalGreenSecondaryBg,
                                    border = BorderStroke(1.dp, RoyalGold),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = scheme.resolveGovLevel().getLabel(currentLanguage),
                                        color = RoyalSoftGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    color = RoyalGreenEmerald,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = status.getLabel(currentLanguage),
                                        color = RoyalTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = scheme.getName(currentLanguage),
                                color = RoyalTextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 26.sp
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = RoyalGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = scheme.getDepartment(currentLanguage),
                                    color = RoyalSoftGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = scheme.getShortDesc(currentLanguage),
                                color = RoyalTextSecondary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                scheme.resolveAllCategories().forEach { cat ->
                                    Surface(
                                        color = RoyalGreenSecondaryBg,
                                        border = BorderStroke(1.dp, RoyalBorder),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = cat.getLabel(currentLanguage),
                                            color = RoyalGreenHighlight,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = RoyalGreenHighlight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${L10n.t("schemes_verified_label", currentLanguage)}: ${scheme.lastVerifiedDate}",
                                    color = RoyalTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Primary Official Website CTA
                            Button(
                                onClick = { onVisitOfficialWebsite(scheme.officialUrl) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoyalGreenEmerald,
                                    contentColor = RoyalTextPrimary
                                ),
                                border = BorderStroke(1.5.dp, RoyalGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .testTag("scheme_detail_visit_official_btn")
                            ) {
                                Text(
                                    text = L10n.t("schemes_visit_official_btn", currentLanguage),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    tint = RoyalSoftGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = "${L10n.t("schemes_official_portal_url_label", currentLanguage)}: ${scheme.officialUrl}",
                                color = RoyalSoftGold,
                                fontSize = 11.sp
                            )

                            if (scheme.officialSourceUrl.isNotBlank() && scheme.officialSourceUrl != scheme.officialUrl) {
                                OutlinedButton(
                                    onClick = { onVisitOfficialWebsite(scheme.officialSourceUrl) },
                                    border = BorderStroke(1.dp, RoyalBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalSoftGold),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("scheme_detail_visit_source_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = L10n.t("schemes_official_source_btn", currentLanguage),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Detailed Sections
                item {
                    DetailSectionCard(
                        title = L10n.t("schemes_section_objectives", currentLanguage),
                        body = scheme.getObjectives(currentLanguage)
                    )
                }
                item {
                    DetailSectionCard(
                        title = L10n.t("schemes_section_who_can_apply", currentLanguage),
                        body = scheme.getWhoCanApply(currentLanguage)
                    )
                }
                item {
                    DetailSectionCard(
                        title = L10n.t("schemes_section_eligibility", currentLanguage),
                        body = scheme.getEligibility(currentLanguage)
                    )
                }
                item {
                    DetailSectionCard(
                        title = L10n.t("schemes_section_documents", currentLanguage),
                        body = scheme.getDocuments(currentLanguage)
                    )
                }
                item {
                    DetailSectionCard(
                        title = L10n.t("schemes_section_benefits", currentLanguage),
                        body = scheme.getBenefits(currentLanguage)
                    )
                }
                item {
                    DetailSectionCard(
                        title = L10n.t("schemes_section_procedure", currentLanguage),
                        body = scheme.getProcedure(currentLanguage)
                    )
                }
                item {
                    DetailSectionCard(
                        title = L10n.t("schemes_section_status_note", currentLanguage),
                        body = "${status.getLabel(currentLanguage)} — ${scheme.getStatusNote(currentLanguage)}"
                    )
                }

                // Admin Verification & Status Controls
                if (isAdmin) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = RoyalGreenSecondaryBg),
                            border = BorderStroke(1.dp, RoyalGold)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = L10n.t("schemes_admin_controls_title", currentLanguage),
                                    color = RoyalGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onAdminUpdateStatus(SchemeApplicationStatus.OPEN, todayDate) },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("scheme_detail_admin_status_btn")
                                    ) {
                                        Text(L10n.t("schemes_admin_mark_open_verified", currentLanguage), fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val target = if (scheme.discontinued) {
                                                SchemeApplicationStatus.INFO_ONLY
                                            } else {
                                                SchemeApplicationStatus.DISCONTINUED
                                            }
                                            onAdminUpdateStatus(target, todayDate)
                                        },
                                        border = BorderStroke(1.dp, RoyalGold),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalSoftGold),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("scheme_detail_admin_discontinue_btn")
                                    ) {
                                        Text(
                                            text = if (scheme.discontinued) {
                                                L10n.t("schemes_admin_restore", currentLanguage)
                                            } else {
                                                L10n.t("schemes_admin_mark_discontinued", currentLanguage)
                                            },
                                            fontSize = 12.sp
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

@Composable
private fun DetailSectionCard(
    title: String,
    body: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        border = BorderStroke(1.dp, RoyalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = RoyalSoftGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = body,
                color = RoyalTextPrimary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}
