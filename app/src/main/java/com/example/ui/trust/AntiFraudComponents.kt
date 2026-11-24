package com.example.ui.trust

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.FraudTargetType
import com.example.data.local.entity.InquiryActivityLogEntity
import com.example.data.model.AppLanguage
import com.example.ui.theme.EarthCardBorder
import com.example.ui.theme.EarthInkPrimary
import com.example.ui.theme.EarthInkSecondary
import com.example.ui.theme.EarthSageWash
import com.example.ui.theme.EarthSurfaceWhite
import com.example.ui.theme.EarthTrustBadgeBg
import com.example.ui.theme.EarthTrustBadgeText
import com.example.ui.theme.EarthWarmCream
import com.example.ui.theme.EarthWarnInk
import com.example.ui.theme.EarthWarnSurface
import com.example.ui.theme.RoyalAlertBg
import com.example.ui.theme.RoyalAlertText
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.util.safeStartActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SellerTrustTier {
    PUBLIC_LISTING,
    VERIFIED_SELLER,
    AUTHORISED_DEALER
}

@Composable
fun SellerTierBadge(
    isVerified: Boolean = false,
    isAuthorisedDealer: Boolean = false,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val tier = when {
        isAuthorisedDealer && isVerified -> SellerTrustTier.AUTHORISED_DEALER
        isVerified -> SellerTrustTier.VERIFIED_SELLER
        else -> SellerTrustTier.PUBLIC_LISTING
    }
    val lang = language
    val (bgColor, textColor, label) = when (tier) {
        SellerTrustTier.AUTHORISED_DEALER -> Triple(
            EarthTrustBadgeBg,
            EarthTrustBadgeText,
            when (lang) {
                AppLanguage.BENGALI -> "অনুমোদিত ব্র্যান্ড ডিলার"
                AppLanguage.HINDI -> "अधिकृत ब्रांड डीलर"
                AppLanguage.ENGLISH -> "Authorised Brand Dealer"
            }
        )
        SellerTrustTier.VERIFIED_SELLER -> Triple(
            EarthSageWash,
            EarthTrustBadgeText,
            when (lang) {
                AppLanguage.BENGALI -> "যাচাইকৃত বিক্রেতা"
                AppLanguage.HINDI -> "सत्यापित विक्रेता"
                AppLanguage.ENGLISH -> "Verified Seller"
            }
        )
        SellerTrustTier.PUBLIC_LISTING -> Triple(
            EarthWarmCream,
            EarthInkSecondary,
            when (lang) {
                AppLanguage.BENGALI -> "পাবলিক তালিকা (যাচাই বাকি)"
                AppLanguage.HINDI -> "सार्वजनिक सूची (सत्यापन बाकी)"
                AppLanguage.ENGLISH -> "Public Listing (Unverified)"
            }
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.28f)),
        modifier = modifier.testTag("seller_tier_badge_${tier.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (tier == SellerTrustTier.PUBLIC_LISTING) Icons.Default.Info else Icons.Default.Verified,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AntiFraudWarningBanner(
    language: AppLanguage,
    onReportClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val lang = language
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = EarthWarnSurface),
        border = BorderStroke(1.dp, EarthWarnInk.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("anti_fraud_warning_banner")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = EarthWarnInk,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> "নিরাপদ লেনদেন ও প্রতারণা সতর্কতা"
                        AppLanguage.HINDI -> "सुरक्षित लेनदेन और धोखाधड़ी चेतावनी"
                        AppLanguage.ENGLISH -> "Buyer Safety & Anti-Fraud Protection"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = EarthWarnInk
                )
            }

            Text(
                text = when (lang) {
                    AppLanguage.BENGALI -> "• কখনোই OTP, UPI PIN বা ব্যাংকের পাসওয়ার্ড কারো সাথে শেয়ার করবেন না।\n• যাচাই ও যন্ত্র/পণ্য সরাসরি পরীক্ষা না করে অগ্রিম টাকা পাঠাবেন না।\n• সর্বদা GST বিল, ইনভয়েস ও লিখিত ওয়ারেন্টি শর্ত নিন।"
                    AppLanguage.HINDI -> "• कभी भी OTP, UPI PIN या बैंक पासवर्ड साझा न करें।\n• प्रत्यक्ष जांच व सत्यापन के बिना अनजान खाते में अग्रिम राशि न भेजें।\n• हमेशा पक्का GST बिल, रसीद और लिखित वारंटी शर्तें प्राप्त करें।"
                    AppLanguage.ENGLISH -> "• Never share OTP, UPI PIN, or bank credentials with anyone.\n• Avoid sending unverified advance payments before checking seller KYC & invoice.\n• KRISHAK SATHI does not collect payments — settle directly with verified sellers."
                },
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
                color = EarthInkPrimary
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        safeStartActivity(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:1915")), "NCH Helpline: 1915")
                    },
                    border = BorderStroke(1.dp, EarthWarnInk.copy(alpha = 0.5f)),
                    modifier = Modifier.heightIn(min = 42.dp).testTag("helpline_nch_1915_btn")
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = EarthWarnInk, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("NCH 1915", fontSize = 11.sp, color = EarthWarnInk, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        safeStartActivity(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930")), "Cybercrime Helpline: 1930")
                    },
                    border = BorderStroke(1.dp, EarthWarnInk.copy(alpha = 0.5f)),
                    modifier = Modifier.heightIn(min = 42.dp).testTag("helpline_cyber_1930_btn")
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = EarthWarnInk, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Cyber 1930", fontSize = 11.sp, color = EarthWarnInk, fontWeight = FontWeight.Bold)
                }

                if (onReportClick != null) {
                    Button(
                        onClick = onReportClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EarthWarnInk, contentColor = Color.White),
                        modifier = Modifier.heightIn(min = 42.dp).testTag("report_fraud_inline_btn")
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = when (lang) {
                                AppLanguage.BENGALI -> "প্রতারণা রিপোর্ট"
                                AppLanguage.HINDI -> "रिपोर्ट करें"
                                AppLanguage.ENGLISH -> "Report Fraud"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SafeBuyerProtectionCard(
    businessName: String,
    locationAddress: String,
    verifiedPhone: String?,
    isVerified: Boolean,
    isAuthorisedDealer: Boolean = false,
    isSuspended: Boolean,
    lastVerifiedDate: String = "",
    warrantyOrInvoiceNote: String = "Always request GST tax invoice & written warranty terms.",
    returnPolicyNote: String = "Inspect equipment condition & specs on site before releasing balance payment.",
    language: AppLanguage,
    onReportFraudClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = language
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isSuspended) RoyalAlertBg else EarthSurfaceWhite),
        border = BorderStroke(1.dp, if (isSuspended) RoyalAlertText.copy(alpha = 0.5f) else EarthCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("safe_buyer_protection_card")
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
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> "নিরাপদ ক্রেতা সুরক্ষা ও বিক্রেতার বিবরণ"
                        AppLanguage.HINDI -> "सुरक्षित खरीदार सुरक्षा व विक्रेता विवरण"
                        AppLanguage.ENGLISH -> "Safe Buyer Protection & Seller KYC"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSuspended) RoyalAlertText else EarthInkPrimary,
                    modifier = Modifier.weight(1f)
                )
                SellerTierBadge(
                    isVerified = isVerified,
                    isAuthorisedDealer = isAuthorisedDealer,
                    language = lang
                )
            }

            if (isSuspended) {
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, RoyalAlertText.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = RoyalAlertText, modifier = Modifier.size(16.dp))
                        Text(
                            text = when (lang) {
                                AppLanguage.BENGALI -> "সতর্কতা: নিরাপত্তা পর্যালোচনার কারণে এই বিক্রেতা বর্তমানে স্থগিত (Suspended) অবস্থায় আছেন।"
                                AppLanguage.HINDI -> "चेतावनी: सुरक्षा समीक्षा के कारण यह विक्रेता वर्तमान में निलंबित (Suspended) है।"
                                AppLanguage.ENGLISH -> "Notice: This seller account is currently SUSPENDED under safety review."
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalAlertText
                        )
                    }
                }
            }

            Text(
                text = buildString {
                    append("• Seller: ${businessName.ifBlank { "Catalog Reference" }}\n")
                    append("• Location: ${locationAddress.ifBlank { "Confirm exact store address before visit" }}\n")
                    if (lastVerifiedDate.isNotBlank()) {
                        append("• Catalog / Record Date: $lastVerifiedDate\n")
                    }
                    append("• Invoice & Warranty: $warrantyOrInvoiceNote\n")
                    append("• Return / Inspection: $returnPolicyNote")
                },
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
                color = if (isSuspended) RoyalAlertText else EarthInkSecondary
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isSuspended && isVerified && !verifiedPhone.isNullOrBlank()) {
                    Button(
                        onClick = {
                            safeStartActivity(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$verifiedPhone")), "Dial: $verifiedPhone")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald, contentColor = Color.White),
                        modifier = Modifier.heightIn(min = 44.dp).testTag("safe_card_call_btn")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Call $verifiedPhone", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (locationAddress.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            val encoded = Uri.encode("$businessName $locationAddress".trim())
                            safeStartActivity(
                                context,
                                Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encoded")),
                                "Maps app not available"
                            )
                        },
                        border = BorderStroke(1.dp, EarthTrustBadgeText.copy(alpha = 0.5f)),
                        modifier = Modifier.heightIn(min = 44.dp).testTag("safe_card_directions_btn")
                    ) {
                        Icon(Icons.Default.Directions, contentDescription = null, tint = EarthTrustBadgeText, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = when (lang) {
                                AppLanguage.BENGALI -> "দিকনির্দেশনা"
                                AppLanguage.HINDI -> "दिशा-निर्देश"
                                AppLanguage.ENGLISH -> "Get Directions"
                            },
                            fontSize = 11.sp,
                            color = EarthTrustBadgeText
                        )
                    }
                }

                OutlinedButton(
                    onClick = onReportFraudClick,
                    border = BorderStroke(1.dp, EarthWarnInk.copy(alpha = 0.55f)),
                    modifier = Modifier.heightIn(min = 44.dp).testTag("safe_card_report_fraud_btn")
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = EarthWarnInk, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> "সমস্যা / প্রতারণা রিপোর্ট"
                            AppLanguage.HINDI -> "धोखाधड़ी रिपोर्ट"
                            AppLanguage.ENGLISH -> "Report Seller / Listing"
                        },
                        fontSize = 11.sp,
                        color = EarthWarnInk
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FraudReportDialog(
    targetName: String,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSubmit: (reasonCode: String, description: String) -> Unit
) {
    val lang = language
    val reasonOptions = remember {
        listOf(
            "FAKE_SELLER" to "Fake Seller / Impersonation",
            "ADVANCE_PAYMENT_SCAM" to "Advance Payment / OTP Scam",
            "COUNTERFEIT_PRODUCT" to "Counterfeit / Wrong Machine",
            "OVERCHARGING" to "Price Mismatch / Overcharging",
            "UNSAFE_EQUIPMENT" to "Unsafe / Defective Equipment",
            "MISLEADING_AD" to "Misleading Specs or Claims",
            "OTHER" to "Other Safety Concern"
        )
    }
    var selectedReason by remember { mutableStateOf(reasonOptions.first().first) }
    var description by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        title = {
            Text(
                text = when (lang) {
                    AppLanguage.BENGALI -> "নিরাপত্তা ও প্রতারণা রিপোর্ট"
                    AppLanguage.HINDI -> "सुरक्षा व धोखाधड़ी रिपोर्ट"
                    AppLanguage.ENGLISH -> "Report Safety or Fraud Issue"
                },
                color = RoyalSoftGold,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Target: $targetName",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RoyalTextPrimary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    reasonOptions.forEach { (code, label) ->
                        FilterChip(
                            selected = selectedReason == code,
                            onClick = { selectedReason = code },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = {
                        Text(
                            when (lang) {
                                AppLanguage.BENGALI -> "বিস্তারিত বিবরণ লিখুন *"
                                AppLanguage.HINDI -> "विस्तृत विवरण लिखें *"
                                AppLanguage.ENGLISH -> "Describe the issue (min 5 chars) *"
                            }
                        )
                    },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("fraud_report_desc_input")
                )
            }
        },
        confirmButton = {
            Button(
                enabled = description.trim().length >= 8,
                onClick = { onSubmit(selectedReason, description.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = Color.Black),
                modifier = Modifier.testTag("fraud_report_submit_btn")
            ) {
                Text(
                    when (lang) {
                        AppLanguage.BENGALI -> "জমা দিন"
                        AppLanguage.HINDI -> "जमा करें"
                        AppLanguage.ENGLISH -> "Submit Report"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = RoyalSoftGold)
            }
        }
    )
}

@Composable
fun ActivityTimelineSection(
    logs: List<InquiryActivityLogEntity>,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    if (logs.isEmpty()) return
    val fmt = remember { SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()) }
    Card(
        colors = CardDefaults.cardColors(containerColor = EarthSurfaceWhite),
        border = BorderStroke(1.dp, EarthCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("activity_timeline_section")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Timeline, contentDescription = null, tint = EarthTrustBadgeText, modifier = Modifier.size(18.dp))
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> "সাম্প্রতিক অনুরোধ ও কার্যকলাপ টাইমলাইন"
                        AppLanguage.HINDI -> "हाल के अनुरोध व गतिविधि टाइमलाइन"
                        AppLanguage.ENGLISH -> "Inquiry & Booking Activity Timeline"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = EarthInkPrimary
                )
            }
            logs.take(6).forEach { entry ->
                Surface(
                    color = EarthWarmCream,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(entry.statusLabel, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EarthTrustBadgeText)
                            Text(fmt.format(Date(entry.timestampMillis)), fontSize = 10.sp, color = EarthInkSecondary)
                        }
                        if (entry.note.isNotBlank()) {
                            Text(entry.note, fontSize = 11.sp, color = EarthInkPrimary)
                        }
                    }
                }
            }
        }
    }
}
