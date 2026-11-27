package com.example.ui.partner

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.AppUserRole
import com.example.data.remote.PartnerRoleRepository
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartnerDashboardScreen(
    userId: String,
    partnerName: String,
    currentLanguage: AppLanguage,
    repository: PartnerRoleRepository,
    onSwitchToFarmerDashboard: () -> Unit,
    onBack: () -> Unit,
    onOpenPartnerRegistration: () -> Unit,
    onOpenNurserySellerWorkspace: () -> Unit,
    onOpenMachinerySellerWorkspace: () -> Unit,
    onOpenCropBuyerWorkspace: () -> Unit,
    onOpenFarmServicesWorkspace: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val membership by repository.observeUserRoleMembership(userId).collectAsState(initial = null)
    val registration by repository.observeUserRegistration(userId).collectAsState(initial = null)

    val sessionApprovedRoles = remember(userId, membership) {
        val fromSession = UserSessionManager.getApprovedRoles(context)
        val fromRepo = membership?.resolvedRoles(includeAdmin = false).orEmpty()
        (fromSession + fromRepo).filter { it.isPartnerRole }.distinct()
    }
    val hasApprovedPartnerRole = sessionApprovedRoles.isNotEmpty() || UserSessionManager.hasApprovedPartnerRole(context)
    val isVerifiedPartner = (registration?.isVerifiedPartner() == true) ||
        (membership?.platformVerified == true && sessionApprovedRoles.isNotEmpty()) ||
        UserSessionManager.isNurseryOwner(context)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = RoyalGreenSecondaryBg, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("partner_dashboard_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "পার্টনার ড্যাশবোর্ড"
                                AppLanguage.HINDI -> "पार्टनर डैशबोर्ड"
                                AppLanguage.ENGLISH -> "Partner Dashboard"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = registration?.businessName?.ifBlank { partnerName } ?: partnerName,
                            fontSize = 11.sp,
                            color = RoyalTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    OutlinedButton(
                        onClick = onSwitchToFarmerDashboard,
                        border = BorderStroke(1.dp, RoyalGold),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .heightIn(min = 42.dp)
                            .testTag("partner_dashboard_switch_farmer_btn")
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = RoyalSoftGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "কৃষক মোড"
                                AppLanguage.HINDI -> "किसान मोड"
                                AppLanguage.ENGLISH -> "Farmer Mode"
                            },
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(RoyalGreenPrimaryBg),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!hasApprovedPartnerRole) {
                // Role-gated protection: non-partners cannot access partner workspaces
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp)
                            .testTag("partner_dashboard_locked_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                        border = BorderStroke(1.dp, RoyalBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = RoyalGold,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "পার্টনার ড্যাশবোর্ড অনুমোদিত অংশীদারদের জন্য সংরক্ষিত"
                                    AppLanguage.HINDI -> "पार्टनर डैशबोर्ड केवल स्वीकृत पार्टनरों के लिए है"
                                    AppLanguage.ENGLISH -> "Partner Dashboard is restricted to approved partners"
                                },
                                fontWeight = FontWeight.Bold,
                                color = RoyalTextPrimary,
                                fontSize = 16.sp
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "আপনার ব্যবসা যাচাই ও অনুমোদনের জন্য পার্টনার নিবন্ধন ফর্ম পূরণ করুন।"
                                    AppLanguage.HINDI -> "अपना व्यवसाय सत्यापित कराने के लिए पार्टनर पंजीकरण फ़ॉर्म भरें।"
                                    AppLanguage.ENGLISH -> "Complete Partner Registration to unlock business workspaces after Admin verification."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = RoyalTextSecondary
                            )
                            Button(
                                onClick = onOpenPartnerRegistration,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoyalGreenEmerald,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("partner_dashboard_apply_btn")
                            ) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "পার্টনার নিবন্ধন খুলুন"
                                        AppLanguage.HINDI -> "पार्टनर पंजीकरण खोलें"
                                        AppLanguage.ENGLISH -> "Open Partner Registration"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Verified Partner Banner
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.2.dp, RoyalGold.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("partner_dashboard_verified_banner")
                    ) {
                        Column(
                            modifier = Modifier
                                .background(
                                    Brush.verticalGradient(
                                        listOf(RoyalGreenSecondaryBg, RoyalGreenCardBg)
                                    )
                                )
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = registration?.businessName?.ifBlank { partnerName } ?: partnerName,
                                        color = RoyalTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    if (!registration?.serviceArea.isNullOrBlank()) {
                                        Text(
                                            text = registration?.serviceArea.orEmpty(),
                                            color = RoyalTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                if (isVerifiedPartner) {
                                    Surface(
                                        color = RoyalGreenEmerald,
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(1.dp, RoyalGold),
                                        modifier = Modifier.testTag("partner_dashboard_verified_badge")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Verified,
                                                contentDescription = null,
                                                tint = RoyalSoftGold,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "প্ল্যাটফর্ম যাচাইকৃত"
                                                    AppLanguage.HINDI -> "प्लेटफ़ॉर्म सत्यापित"
                                                    AppLanguage.ENGLISH -> "Platform Verified"
                                                },
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                sessionApprovedRoles.forEach { role ->
                                    Surface(
                                        color = RoyalGreenPrimaryBg,
                                        border = BorderStroke(1.dp, RoyalBorder),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = role.getLabel(currentLanguage),
                                            color = RoyalSoftGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Role-specific Workspaces unlocked for this user's approved roles
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "আপনার অনুমোদিত ব্যবসা কর্মক্ষেত্র"
                            AppLanguage.HINDI -> "आपके स्वीकृत व्यवसाय कार्यक्षेत्र"
                            AppLanguage.ENGLISH -> "Your Authorized Partner Workspaces"
                        },
                        color = RoyalSoftGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    if (AppUserRole.NURSERY_SELLER in sessionApprovedRoles) {
                        PartnerWorkspaceCard(
                            icon = Icons.Default.Storefront,
                            title = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নার্সারি বিক্রেতা ও স্টক প্যানেল"
                                AppLanguage.HINDI -> "नर्सरी विक्रेता व स्टॉक पैनल"
                                AppLanguage.ENGLISH -> "Nursery Seller Inventory & Orders"
                            },
                            subtitle = when (currentLanguage) {
                                AppLanguage.BENGALI -> "চারা ও বীজের তালিকা, দাম ও অর্ডার পরিচালনা করুন"
                                AppLanguage.HINDI -> "पौधों व बीजों की सूची, मूल्य और ऑर्डर प्रबंधित करें"
                                AppLanguage.ENGLISH -> "Manage nursery plants, stock availability and incoming customer orders"
                            },
                            actionLabel = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নার্সারি প্যানেল খুলুন"
                                AppLanguage.HINDI -> "नर्सरी पैनल खोलें"
                                AppLanguage.ENGLISH -> "Open Nursery Workspace"
                            },
                            tag = "partner_workspace_nursery_btn",
                            onClick = onOpenNurserySellerWorkspace
                        )
                    }

                    if (AppUserRole.MACHINERY_DEALER in sessionApprovedRoles ||
                        AppUserRole.MACHINERY_RENTAL_OWNER in sessionApprovedRoles
                    ) {
                        PartnerWorkspaceCard(
                            icon = Icons.Default.Agriculture,
                            title = when (currentLanguage) {
                                AppLanguage.BENGALI -> "কৃষি যন্ত্র বিক্রয় ও ভাড়া প্যানেল"
                                AppLanguage.HINDI -> "कृषि मशीनरी बिक्री व किराया पैनल"
                                AppLanguage.ENGLISH -> "Machinery Dealer & Rental Hub"
                            },
                            subtitle = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ট্রাক্টর, স্প্রেয়ার, কোটেশন ও ভাড়ার অনুরোধ পরিচালনা করুন"
                                AppLanguage.HINDI -> "ट्रैक्टर, स्प्रेयर, कोटेशन व किराया अनुरोध प्रबंधित करें"
                                AppLanguage.ENGLISH -> "Respond to machinery quotations and farmer rental requests"
                            },
                            actionLabel = when (currentLanguage) {
                                AppLanguage.BENGALI -> "যন্ত্রপাতি প্যানেল খুলুন"
                                AppLanguage.HINDI -> "मशीनरी पैनल खोलें"
                                AppLanguage.ENGLISH -> "Open Machinery Workspace"
                            },
                            tag = "partner_workspace_machinery_btn",
                            onClick = onOpenMachinerySellerWorkspace
                        )
                    }

                    if (AppUserRole.CROP_BUYER in sessionApprovedRoles) {
                        PartnerWorkspaceCard(
                            icon = Icons.Default.ShoppingBag,
                            title = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ফসল ক্রয় ও অফার পোর্টাল"
                                AppLanguage.HINDI -> "फसल खरीद व ऑफ़र पोर्टल"
                                AppLanguage.ENGLISH -> "Registered Crop Buyer Desk"
                            },
                            subtitle = when (currentLanguage) {
                                AppLanguage.BENGALI -> "কৃষকদের সরাসরি ফসল দেখুন ও মূল্য অফার পাঠান"
                                AppLanguage.HINDI -> "किसानों की सीधी फसल देखें व मूल्य प्रस्ताव भेजें"
                                AppLanguage.ENGLISH -> "Browse farmer produce listings and submit verified buyer offers"
                            },
                            actionLabel = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ফসল মার্কেট খুলুন"
                                AppLanguage.HINDI -> "फसल बाज़ार खोलें"
                                AppLanguage.ENGLISH -> "Open Buyer Marketplace"
                            },
                            tag = "partner_workspace_crop_buyer_btn",
                            onClick = onOpenCropBuyerWorkspace
                        )
                    }

                    if (AppUserRole.FARM_SERVICE_PROVIDER in sessionApprovedRoles) {
                        PartnerWorkspaceCard(
                            icon = Icons.Default.Handyman,
                            title = when (currentLanguage) {
                                AppLanguage.BENGALI -> "কৃষি পরিষেবা ও মেরামতি বুকিং"
                                AppLanguage.HINDI -> "कृषि सेवा व मरम्मत बुकिंग"
                                AppLanguage.ENGLISH -> "Farm Services & Repair Desk"
                            },
                            subtitle = when (currentLanguage) {
                                AppLanguage.BENGALI -> "জমি চাষ, স্প্রে, সেচ ও যন্ত্র মেরামতি বুকিং পরিচালনা করুন"
                                AppLanguage.HINDI -> "जुताई, छिड़काव, सिंचाई व मशीन मरम्मत बुकिंग सँभालें"
                                AppLanguage.ENGLISH -> "Manage ploughing, spraying, irrigation and machinery repair bookings"
                            },
                            actionLabel = when (currentLanguage) {
                                AppLanguage.BENGALI -> "পরিষেবা বুকিং দেখুন"
                                AppLanguage.HINDI -> "सेवा बुकिंग देखें"
                                AppLanguage.ENGLISH -> "Open Service Bookings"
                            },
                            tag = "partner_workspace_services_btn",
                            onClick = onOpenFarmServicesWorkspace
                        )
                    }

                    // Update or manage partner registration details
                    OutlinedButton(
                        onClick = onOpenPartnerRegistration,
                        border = BorderStroke(1.dp, RoyalBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("partner_dashboard_manage_reg_btn")
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = RoyalSoftGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "পার্টনার তথ্য ও নতুন বিভাগ যোগ করুন"
                                AppLanguage.HINDI -> "पार्टनर विवरण व नई श्रेणी जोड़ें"
                                AppLanguage.ENGLISH -> "Manage Registration & Request Additional Roles"
                            },
                            color = RoyalTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PartnerWorkspaceCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
        border = BorderStroke(1.dp, RoyalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RoyalGold,
                    modifier = Modifier.size(26.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = RoyalTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = subtitle,
                        color = RoyalTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalGreenEmerald,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag(tag)
            ) {
                Text(text = actionLabel, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}
