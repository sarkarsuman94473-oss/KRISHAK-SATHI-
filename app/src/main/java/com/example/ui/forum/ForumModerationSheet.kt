package com.example.ui.forum

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ForumModerationStatus
import com.example.data.model.ForumPost
import com.example.data.model.ForumReport
import com.example.data.model.ForumReportReason
import com.example.data.model.ForumReportStatus
import com.example.data.model.ForumUserModeration
import com.example.data.model.UserModerationStatus
import com.example.data.remote.ForumStarterTranslations
import com.example.ui.theme.AgriAlertRed
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
import com.example.ui.theme.royalOutlinedTextFieldColors
import com.example.util.L10n

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ForumModerationPanel(
    currentLanguage: AppLanguage,
    posts: List<ForumPost>,
    reports: List<ForumReport>,
    userModerations: List<ForumUserModeration>,
    onBack: () -> Unit,
    onHideOrRestorePost: (postId: String, nextStatus: ForumModerationStatus, reason: String) -> Unit,
    onPinOrUnpinPost: (postId: String, pinned: Boolean) -> Unit,
    onDeletePost: (postId: String) -> Unit,
    onResolveReport: (reportId: String, status: ForumReportStatus, note: String) -> Unit,
    onModerateUser: (userId: String, userName: String, status: UserModerationStatus, reason: String, muteHours: Long) -> Unit
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var moderationReasonNote by remember { mutableStateOf("") }

    val activeReason = moderationReasonNote.trim().ifBlank {
        L10n.t("forum_mod_reason_default", currentLanguage)
    }

    // Merge recorded moderations with unique authors visible in posts so admins can moderate any author
    val combinedUsers = remember(posts, userModerations) {
        val map = LinkedHashMap<String, ForumUserModeration>()
        userModerations.forEach { map[it.userId] = it }
        posts.forEach { post ->
            if (post.authorId.isNotBlank() && !map.containsKey(post.authorId)) {
                map[post.authorId] = ForumUserModeration(
                    id = post.authorId,
                    userId = post.authorId,
                    userName = post.authorName,
                    status = UserModerationStatus.ACTIVE.code,
                    warningCount = 0L
                )
            }
        }
        map.values.toList()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = RoyalGreenPrimaryBg,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = RoyalGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = L10n.t("forum_moderation_title", currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = RoyalTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("forum_mod_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = L10n.t("btn_back", currentLanguage),
                            tint = RoyalSoftGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalGreenSecondaryBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val pendingCount = reports.count { it.status == ForumReportStatus.PENDING.code }
            val tabs = listOf(
                "${L10n.t("forum_mod_tab_reports", currentLanguage)} ($pendingCount)",
                "${L10n.t("forum_mod_tab_posts", currentLanguage)} (${posts.size})",
                "${L10n.t("forum_mod_tab_users", currentLanguage)} (${combinedUsers.size})"
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = RoyalGreenSecondaryBg,
                contentColor = RoyalGold,
                indicator = { positions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(positions[selectedTab]),
                        color = RoyalGold
                    )
                }
            ) {
                tabs.forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == idx) RoyalSoftGold else RoyalTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.testTag("forum_mod_tab_$idx")
                    )
                }
            }

            OutlinedTextField(
                value = moderationReasonNote,
                onValueChange = { if (it.length <= 280) moderationReasonNote = it },
                placeholder = { Text(L10n.t("forum_mod_reason_hint", currentLanguage)) },
                singleLine = true,
                colors = royalOutlinedTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("forum_mod_reason_input")
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("forum_mod_list"),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        if (reports.isEmpty()) {
                            item {
                                Text(
                                    text = L10n.t("forum_mod_no_reports", currentLanguage),
                                    color = RoyalTextSecondary,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(reports, key = { it.id }) { report ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                    border = BorderStroke(1.dp, RoyalBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = RoyalAlertBg,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "${report.targetType} • ${ForumReportReason.fromCode(report.reason).getLabel(currentLanguage)}",
                                                    color = RoyalAlertText,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            Surface(
                                                color = RoyalWarningBg,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = ForumReportStatus.fromCode(report.status).getLabel(currentLanguage),
                                                    color = RoyalWarningText,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "${report.targetAuthorName} — ${report.targetPreview}",
                                            color = RoyalTextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        if (report.details.isNotBlank()) {
                                            Text(
                                                text = report.details,
                                                color = RoyalTextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Text(
                                            text = "${L10n.t("forum_reported_by", currentLanguage)}: ${report.reporterName}",
                                            color = RoyalTextSecondary,
                                            fontSize = 11.sp
                                        )

                                        if (report.status == ForumReportStatus.PENDING.code) {
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        onHideOrRestorePost(report.postId, ForumModerationStatus.HIDDEN, activeReason)
                                                        onResolveReport(report.id, ForumReportStatus.RESOLVED, activeReason)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = AgriAlertRed, contentColor = Color.White),
                                                    modifier = Modifier.heightIn(min = 48.dp)
                                                ) {
                                                    Text(L10n.t("forum_mod_hide", currentLanguage), fontSize = 12.sp)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        onModerateUser(report.targetAuthorId, report.targetAuthorName, UserModerationStatus.WARNED, activeReason, 0L)
                                                        onResolveReport(report.id, ForumReportStatus.RESOLVED, activeReason)
                                                    },
                                                    border = BorderStroke(1.dp, RoyalGold),
                                                    modifier = Modifier.heightIn(min = 48.dp)
                                                ) {
                                                    Text(L10n.t("forum_mod_warn", currentLanguage), color = RoyalSoftGold, fontSize = 12.sp)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        onModerateUser(report.targetAuthorId, report.targetAuthorName, UserModerationStatus.MUTED, activeReason, 168L)
                                                        onResolveReport(report.id, ForumReportStatus.RESOLVED, activeReason)
                                                    },
                                                    border = BorderStroke(1.dp, RoyalAlertText),
                                                    modifier = Modifier.heightIn(min = 48.dp)
                                                ) {
                                                    Text(L10n.t("forum_mod_mute_7d", currentLanguage), color = RoyalAlertText, fontSize = 12.sp)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        onResolveReport(report.id, ForumReportStatus.DISMISSED, activeReason)
                                                    },
                                                    border = BorderStroke(1.dp, RoyalBorder),
                                                    modifier = Modifier.heightIn(min = 48.dp)
                                                ) {
                                                    Text(L10n.t("forum_mod_dismiss", currentLanguage), color = RoyalTextSecondary, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        items(posts, key = { it.id }) { post ->
                            val isHidden = post.moderationStatus == ForumModerationStatus.HIDDEN.code
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                border = BorderStroke(1.dp, if (post.pinned) RoyalGold else RoyalBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = post.authorName,
                                            color = RoyalSoftGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Surface(
                                            color = if (isHidden) RoyalAlertBg else RoyalSuccessBg,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = ForumModerationStatus.fromCode(post.moderationStatus).getLabel(currentLanguage),
                                                color = if (isHidden) RoyalAlertText else RoyalSuccessText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = ForumStarterTranslations.getLocalizedTitle(post, currentLanguage),
                                        color = RoyalTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onPinOrUnpinPost(post.id, !post.pinned) },
                                            border = BorderStroke(1.dp, RoyalGold),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("forum_mod_pin_${post.id}")
                                        ) {
                                            Icon(Icons.Default.PushPin, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (post.pinned) L10n.t("forum_mod_unpin", currentLanguage) else L10n.t("forum_mod_pin", currentLanguage),
                                                color = RoyalSoftGold,
                                                fontSize = 12.sp
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val next = if (isHidden) ForumModerationStatus.APPROVED else ForumModerationStatus.HIDDEN
                                                onHideOrRestorePost(post.id, next, activeReason)
                                            },
                                            border = BorderStroke(1.dp, RoyalBorder),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("forum_mod_visibility_${post.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                tint = RoyalTextPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isHidden) L10n.t("forum_mod_restore", currentLanguage) else L10n.t("forum_mod_hide", currentLanguage),
                                                color = RoyalTextPrimary,
                                                fontSize = 12.sp
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { onDeletePost(post.id) },
                                            border = BorderStroke(1.dp, RoyalAlertText.copy(alpha = 0.6f)),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("forum_mod_delete_${post.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = RoyalAlertText, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(L10n.t("forum_delete_post", currentLanguage), color = RoyalAlertText, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        if (combinedUsers.isEmpty()) {
                            item {
                                Text(
                                    text = L10n.t("forum_mod_no_users", currentLanguage),
                                    color = RoyalTextSecondary,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(combinedUsers, key = { it.userId }) { userMod ->
                                val statusEnum = UserModerationStatus.fromCode(userMod.status)
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                                    border = BorderStroke(1.dp, RoyalBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = userMod.userName,
                                                    color = RoyalTextPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "${L10n.t("forum_strikes_label", currentLanguage)}: ${userMod.warningCount}",
                                                    color = RoyalTextSecondary,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Surface(
                                                color = when (statusEnum) {
                                                    UserModerationStatus.ACTIVE -> RoyalSuccessBg
                                                    UserModerationStatus.WARNED -> RoyalWarningBg
                                                    UserModerationStatus.MUTED, UserModerationStatus.BANNED -> RoyalAlertBg
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = statusEnum.getLabel(currentLanguage),
                                                    color = when (statusEnum) {
                                                        UserModerationStatus.ACTIVE -> RoyalSuccessText
                                                        UserModerationStatus.WARNED -> RoyalWarningText
                                                        UserModerationStatus.MUTED, UserModerationStatus.BANNED -> RoyalAlertText
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                        if (userMod.moderationReason.isNotBlank()) {
                                            Text(
                                                text = userMod.moderationReason,
                                                color = RoyalTextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { onModerateUser(userMod.userId, userMod.userName, UserModerationStatus.WARNED, activeReason, 0L) },
                                                border = BorderStroke(1.dp, RoyalGold),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("forum_mod_warn_${userMod.userId}")
                                            ) {
                                                Text(L10n.t("forum_mod_warn", currentLanguage), color = RoyalSoftGold, fontSize = 12.sp)
                                            }
                                            OutlinedButton(
                                                onClick = { onModerateUser(userMod.userId, userMod.userName, UserModerationStatus.MUTED, activeReason, 24L) },
                                                border = BorderStroke(1.dp, RoyalWarningText),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("forum_mod_mute24_${userMod.userId}")
                                            ) {
                                                Text(L10n.t("forum_mod_mute_24h", currentLanguage), color = RoyalWarningText, fontSize = 12.sp)
                                            }
                                            OutlinedButton(
                                                onClick = { onModerateUser(userMod.userId, userMod.userName, UserModerationStatus.BANNED, activeReason, 0L) },
                                                border = BorderStroke(1.dp, RoyalAlertText),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("forum_mod_ban_${userMod.userId}")
                                            ) {
                                                Text(L10n.t("forum_mod_ban", currentLanguage), color = RoyalAlertText, fontSize = 12.sp)
                                            }
                                            if (statusEnum != UserModerationStatus.ACTIVE) {
                                                Button(
                                                    onClick = { onModerateUser(userMod.userId, userMod.userName, UserModerationStatus.ACTIVE, activeReason, 0L) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald, contentColor = Color.White),
                                                    modifier = Modifier
                                                        .heightIn(min = 48.dp)
                                                        .testTag("forum_mod_restore_user_${userMod.userId}")
                                                ) {
                                                    Text(L10n.t("forum_mod_restore_user", currentLanguage), fontSize = 12.sp)
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
    }
}
