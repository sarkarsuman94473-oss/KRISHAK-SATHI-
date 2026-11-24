package com.example.ui.footer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.data.local.config.AppConfigManager
import com.example.data.model.AppLanguage
import com.example.ui.info.LegalTab
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.util.L10n
import com.example.util.safeStartActivity

/**
 * Premium, responsive Corporate Footer for KRISHAK SATHI.
 * Displays branding, official description, configurable release & version info,
 * social media links, quick links to legal and support pages, and contact details.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppFooter(
    currentLanguage: AppLanguage,
    onNavigateAbout: () -> Unit = {},
    onNavigateContact: () -> Unit = {},
    onNavigateLegal: (LegalTab) -> Unit = {},
    onNavigateGovtInfo: () -> Unit = {},
    onNavigateExperts: () -> Unit = {},
    onNavigateFaq: () -> Unit = {},
    onNavigateLeadingFarmers: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config by AppConfigManager.configState.collectAsState()

    fun safeOpenUrl(url: String, serviceName: String) {
        val trimmed = url.trim()
        if (trimmed.isBlank()) {
            val notConfiguredMsg = when (currentLanguage) {
                AppLanguage.BENGALI -> "$serviceName লিংক কনফিগার করা হয়নি"
                AppLanguage.HINDI -> "$serviceName लिंक कॉन्फ़िगर नहीं किया गया है"
                AppLanguage.ENGLISH -> "$serviceName link is not configured"
            }
            Toast.makeText(context, notConfiguredMsg, Toast.LENGTH_SHORT).show()
            return
        }
        val failedMsg = when (currentLanguage) {
            AppLanguage.BENGALI -> "$serviceName খোলা সম্ভব হয়নি"
            AppLanguage.HINDI -> "$serviceName खोलना संभव नहीं हुआ"
            AppLanguage.ENGLISH -> "Unable to open $serviceName"
        }
        safeStartActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(trimmed)), failedMsg)
    }

    Surface(
        color = RoyalGreenSecondaryBg, // Royal Green Secondary Background (#0B3B2B)
        border = BorderStroke(1.dp, RoyalBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_corporate_footer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Branding Header & Tagline
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(RoyalGreenEmerald, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🌱", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.appName,
                        fontSize = 20.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalTextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = config.getSubtitle(currentLanguage),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RoyalGold
                    )
                }
            }

            // 2. Official Description
            Text(
                text = config.getDescription(currentLanguage),
                fontSize = 12.sp,
                color = RoyalTextSecondary,
                lineHeight = 18.sp
            )

            // 3. Social Media Section (Only shows icons with configured URLs)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = L10n.t("footer_connect_with_us", currentLanguage),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (config.facebookUrl.isNotBlank()) {
                        SocialMediaIconButton(
                            label = "Facebook",
                            iconText = "f",
                            badgeColor = Color(0xFF1877F2),
                            onClick = { safeOpenUrl(config.facebookUrl, "Facebook") }
                        )
                    }

                    if (config.instagramUrl.isNotBlank()) {
                        SocialMediaIconButton(
                            label = "Instagram",
                            iconText = "📷",
                            badgeColor = Color(0xFFE4405F),
                            onClick = { safeOpenUrl(config.instagramUrl, "Instagram") }
                        )
                    }

                    if (config.youtubeUrl.isNotBlank()) {
                        SocialMediaIconButton(
                            label = "YouTube",
                            iconText = "▶",
                            badgeColor = Color(0xFFFF0000),
                            onClick = { safeOpenUrl(config.youtubeUrl, "YouTube") }
                        )
                    }

                    if (config.whatsappUrl.isNotBlank()) {
                        SocialMediaIconButton(
                            label = "WhatsApp",
                            iconText = "💬",
                            badgeColor = Color(0xFF25D366),
                            onClick = { safeOpenUrl(config.whatsappUrl, "WhatsApp") }
                        )
                    }

                    if (config.twitterUrl.isNotBlank()) {
                        SocialMediaIconButton(
                            label = "X",
                            iconText = "𝕏",
                            badgeColor = Color.Black,
                            onClick = { safeOpenUrl(config.twitterUrl, "X") }
                        )
                    }
                }
            }

            HorizontalDivider(color = RoyalBorder, thickness = 1.dp)

            // 4. Quick Links Grid (Responsive FlowRow)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = L10n.t("footer_quick_links", currentLanguage),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalGold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FooterLinkChip(L10n.t("footer_link_about", currentLanguage), onClick = onNavigateAbout)
                    FooterLinkChip(L10n.t("footer_link_contact", currentLanguage), onClick = onNavigateContact)
                    FooterLinkChip(L10n.t("footer_link_privacy", currentLanguage), onClick = { onNavigateLegal(LegalTab.PRIVACY) })
                    FooterLinkChip(L10n.t("footer_link_terms", currentLanguage), onClick = { onNavigateLegal(LegalTab.TERMS) })
                    FooterLinkChip(L10n.t("footer_link_disclaimer", currentLanguage), onClick = { onNavigateLegal(LegalTab.AGRI_DISCLAIMER) })
                    FooterLinkChip(L10n.t("footer_link_ai_disclaimer", currentLanguage), onClick = { onNavigateLegal(LegalTab.AI_DISCLAIMER) })
                    FooterLinkChip(L10n.t("footer_link_help", currentLanguage), onClick = onNavigateContact)
                    FooterLinkChip(L10n.t("footer_link_govt_info", currentLanguage), onClick = onNavigateGovtInfo)
                    FooterLinkChip(L10n.t("footer_link_experts", currentLanguage), onClick = onNavigateExperts)
                    FooterLinkChip(L10n.t("footer_link_leading_farmers", currentLanguage), onClick = onNavigateLeadingFarmers)
                    FooterLinkChip(L10n.t("footer_link_faq", currentLanguage), onClick = onNavigateFaq)
                }
            }

            HorizontalDivider(color = RoyalBorder, thickness = 1.dp)

            // 5. Contact and Support Details Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                border = BorderStroke(1.dp, RoyalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📞 ${L10n.t("footer_customer_care", currentLanguage)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = RoyalGold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = RoyalSoftGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = config.getSupportPhone(currentLanguage), color = RoyalTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = RoyalSoftGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = config.supportEmail, color = RoyalTextSecondary, fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = RoyalSoftGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = config.getSupportHours(currentLanguage), color = RoyalTextSecondary, fontSize = 11.sp)
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = RoyalSoftGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = config.getBusinessAddress(currentLanguage), color = RoyalTextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onNavigateContact,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald, contentColor = RoyalTextPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("footer_contact_support_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(L10n.t("footer_btn_contact_support", currentLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            HorizontalDivider(color = RoyalBorder, thickness = 1.dp)

            // 6. Release & Version Information Block
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "অ্যাপ: ${config.appName}  •  সংস্করণ: ${config.currentVersion}"
                        AppLanguage.HINDI -> "ऐप: ${config.appName}  •  संस्करण: ${config.currentVersion}"
                        AppLanguage.ENGLISH -> "App Name: ${config.appName}  •  Version: ${config.currentVersion}"
                    },
                    color = RoyalTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "প্রকাশিত: ${config.releaseDate}  •  সর্বশেষ আপডেট: ${config.lastUpdatedDate}"
                        AppLanguage.HINDI -> "जारी तिथि: ${config.releaseDate}  •  अंतिम अपडेट: ${config.lastUpdatedDate}"
                        AppLanguage.ENGLISH -> "Released: ${config.releaseDate}  •  Last Updated: ${config.lastUpdatedDate}"
                    },
                    color = RoyalTextSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = config.copyrightText,
                    color = RoyalTextSecondary.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SocialMediaIconButton(
    label: String,
    iconText: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = badgeColor,
        modifier = Modifier
            .size(48.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = iconText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun FooterLinkChip(
    title: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = RoyalGreenCardBg,
        border = BorderStroke(1.dp, RoyalBorder),
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = RoyalTextPrimary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = RoyalGold,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
