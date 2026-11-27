package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
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
import com.example.util.L10n
import com.example.util.NetworkStatusSnapshot

@Composable
fun NetworkStatusPill(
    snapshot: NetworkStatusSnapshot,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (snapshot.isOnline) RoyalSuccessBg else RoyalWarningBg
    val inkColor = if (snapshot.isOnline) RoyalSuccessText else RoyalWarningText
    val borderColor = if (snapshot.isOnline) RoyalBorder else RoyalGold.copy(alpha = 0.65f)

    Surface(
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .heightIn(min = 32.dp)
            .clickable(onClick = onClick)
            .testTag("network_status_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = if (snapshot.isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = snapshot.getBadgeText(language),
                tint = inkColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = snapshot.getBadgeText(language),
                color = inkColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OfflineModeBanner(
    language: AppLanguage,
    onRetryCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = RoyalWarningBg,
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_mode_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = RoyalWarningText,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = L10n.t("net_offline_banner_title", language),
                    color = RoyalWarningText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = L10n.t("net_offline_banner_desc", language),
                    color = RoyalTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
            OutlinedButton(
                onClick = onRetryCheck,
                border = BorderStroke(1.dp, RoyalGold),
                modifier = Modifier.heightIn(min = 38.dp)
            ) {
                Text(
                    text = L10n.t("net_retry_btn", language),
                    color = RoyalSoftGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun NetworkDetailsDialog(
    snapshot: NetworkStatusSnapshot,
    isCloudSignedIn: Boolean,
    language: AppLanguage,
    onConnectGoogle: () -> Unit,
    onRefreshNetwork: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        titleContentColor = RoyalGold,
        textContentColor = RoyalTextPrimary,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = if (snapshot.isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = if (snapshot.isOnline) RoyalSuccessText else RoyalWarningText
                )
                Text(
                    text = L10n.t("net_dialog_title", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = snapshot.getTransportLabel(language),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = RoyalTextPrimary
                )
                Surface(
                    color = if (isCloudSignedIn) RoyalSuccessBg else RoyalGreenSecondaryBg,
                    border = BorderStroke(1.dp, RoyalBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isCloudSignedIn) {
                            L10n.t("cloud_sync_connected_desc", language)
                        } else {
                            L10n.t("cloud_sync_quick_login_desc", language)
                        },
                        fontSize = 12.sp,
                        color = if (isCloudSignedIn) RoyalSuccessText else RoyalTextSecondary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (!isCloudSignedIn) {
                Button(
                    onClick = {
                        onDismiss()
                        onConnectGoogle()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(L10n.t("cloud_connect_google_btn", language), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        onRefreshNetwork()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Text(L10n.t("btn_close", language), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onRefreshNetwork()
                onDismiss()
            }) {
                Text(L10n.t("net_retry_btn", language), color = RoyalSoftGold)
            }
        }
    )
}

@Composable
fun GoogleCloudSyncPromptDialog(
    language: AppLanguage,
    onContinueOffline: () -> Unit,
    onConnectGoogle: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        titleContentColor = RoyalGold,
        textContentColor = RoyalTextPrimary,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CloudSync, contentDescription = null, tint = RoyalGold)
                Text(
                    text = L10n.t("cloud_sync_prompt_title", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Text(
                text = L10n.t("cloud_sync_prompt_body", language),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = RoyalTextPrimary
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onConnectGoogle()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                modifier = Modifier.heightIn(min = 48.dp).testTag("cloud_prompt_connect_google_btn")
            ) {
                Text(L10n.t("cloud_connect_google_btn", language), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onContinueOffline()
                },
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(L10n.t("cloud_stay_offline_btn", language), color = RoyalSoftGold)
            }
        }
    )
}
