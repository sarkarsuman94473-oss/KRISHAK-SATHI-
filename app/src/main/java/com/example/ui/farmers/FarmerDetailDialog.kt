package com.example.ui.farmers

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.datasource.LeadingFarmerTranslations
import com.example.data.local.entity.LeadingFarmerEntity
import com.example.data.model.AppLanguage
import com.example.data.repository.LeadingFarmerRepository
import com.example.ui.theme.AgriEarthSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriPriceGold
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.util.L10n
import com.example.util.isCompactNarrowScreen
import com.example.util.safeStartActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun FarmerDetailDialog(
    farmer: LeadingFarmerEntity,
    currentLanguage: AppLanguage,
    repository: LeadingFarmerRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showContactRequestDialog by remember { mutableStateOf(false) }

    val farmerName = LeadingFarmerTranslations.getDisplayName(farmer, currentLanguage)
    val farmerSpecialization = LeadingFarmerTranslations.getDisplaySpecialization(farmer, currentLanguage)
    val farmerAward = LeadingFarmerTranslations.getDisplayAward(farmer, currentLanguage)
    val farmerLocation = LeadingFarmerTranslations.getDisplayLocation(farmer, currentLanguage)

    fun openUrl(url: String) {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return
        val safeUrl = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "https://$trimmed"
        val msg = when (currentLanguage) {
            AppLanguage.BENGALI -> "ওয়েবসাইট খুলতে সমস্যা হয়েছে"
            AppLanguage.HINDI -> "वेबसाइट खोलने में समस्या हुई"
            AppLanguage.ENGLISH -> "Unable to open website"
        }
        safeStartActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)), msg)
    }

    fun makeCall(phone: String) {
        val cleanPhone = phone.filter { it.isDigit() || it == '+' }
        val msg = when (currentLanguage) {
            AppLanguage.BENGALI -> "ডায়াল করতে সমস্যা হয়েছে"
            AppLanguage.HINDI -> "डायल करने में समस्या हुई"
            AppLanguage.ENGLISH -> "Unable to open dialer"
        }
        if (cleanPhone.isBlank()) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            return
        }
        safeStartActivity(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")), msg)
    }

    fun sendEmail(email: String) {
        val trimmed = email.trim()
        if (trimmed.isBlank()) return
        val subjectPrefix = when (currentLanguage) {
            AppLanguage.BENGALI -> "কৃষক সাথী অ্যাপের মাধ্যমে যোগাযোগ:"
            AppLanguage.HINDI -> "कृषक साथी ऐप के माध्यम से संपर्क:"
            AppLanguage.ENGLISH -> "Contact via Krishak Sathi App:"
        }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$trimmed")
            putExtra(Intent.EXTRA_SUBJECT, "$subjectPrefix $farmerName")
        }
        val msg = when (currentLanguage) {
            AppLanguage.BENGALI -> "ইমেইল অ্যাপ পাওয়া যায়নি"
            AppLanguage.HINDI -> "ईमेल ऐप नहीं मिला"
            AppLanguage.ENGLISH -> "No email app found"
        }
        safeStartActivity(context, intent, msg)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .imePadding(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar
                    Surface(color = AgriGreenPrimary, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, tint = AgriPriceGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = farmerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = farmerLocation,
                                        fontSize = 12.sp,
                                        color = com.example.ui.theme.RoyalTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    .testTag("farmer_detail_close_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }

                    // Scrollable tabs prevent narrow-screen Bengali/Hindi clipping
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 8.dp
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "পরিচিতি"
                                        AppLanguage.HINDI -> "परिचय"
                                        AppLanguage.ENGLISH -> "Overview"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text(L10n.t("tab_story", currentLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text(L10n.t("tab_techniques", currentLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text(L10n.t("tab_contacts", currentLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    // Scrollable Content Body
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (selectedTab) {
                            0 -> {
                                // OVERVIEW TAB
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_krishak_sathi_1793336218748),
                                            contentDescription = farmer.fullNameEn,
                                            modifier = Modifier.size(60.dp).clip(CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = farmerName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AgriTextPrimary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(imageVector = Icons.Default.Verified, contentDescription = "Verified", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Surface(color = AgriPriceGold, shape = RoundedCornerShape(8.dp)) {
                                            Text(
                                                text = "🎖 $farmerAward",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = com.example.ui.theme.RoyalGreenPrimaryBg,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = AgriEarthSecondary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(text = farmerLocation, fontSize = 12.sp, color = AgriTextSecondary)
                                        }
                                        Text(
                                            text = L10n.format("farmer_experience_years", currentLanguage, farmer.yearsOfExperience),
                                            fontSize = 11.sp,
                                            color = AgriTextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Specialization Card
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = L10n.t("farmer_specialization", currentLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(text = farmerSpecialization, fontSize = 13.sp, color = AgriTextPrimary, lineHeight = 20.sp)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    Text(
                                        text = L10n.t("farmer_main_crops", currentLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(text = LeadingFarmerTranslations.getDisplayCrops(farmer, currentLanguage), fontSize = 13.sp, color = AgriTextPrimary)
                                }
                            }

                            // Innovation Card
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = L10n.t("farmer_key_innovation", currentLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = LeadingFarmerTranslations.getDisplayInnovation(farmer, currentLanguage),
                                        fontSize = 13.sp,
                                        color = AgriTextPrimary,
                                        lineHeight = 20.sp
                                    )
                                }
                            }

                            // All Awards
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = L10n.t("farmer_awards", currentLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = LeadingFarmerTranslations.getDisplayAllAwards(farmer, currentLanguage),
                                        fontSize = 13.sp,
                                        color = AgriTextPrimary,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }

                        1 -> {
                            // STORY TAB
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = L10n.t("farmer_journey", currentLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = LeadingFarmerTranslations.getStoryHowStarted(farmer, currentLanguage),
                                        fontSize = 13.sp,
                                        color = AgriTextPrimary,
                                        lineHeight = 21.sp
                                    )
                                }
                            }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = L10n.t("farmer_challenges", currentLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = LeadingFarmerTranslations.getStoryChallenges(farmer, currentLanguage),
                                        fontSize = 13.sp,
                                        color = AgriTextPrimary,
                                        lineHeight = 21.sp
                                    )
                                }
                            }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = L10n.t("farmer_achievements", currentLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = LeadingFarmerTranslations.getStoryAchievements(farmer, currentLanguage),
                                        fontSize = 13.sp,
                                        color = AgriTextPrimary,
                                        lineHeight = 21.sp
                                    )
                                }
                            }
                        }

                        2 -> {
                            // TECHNIQUES & LESSONS TAB
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = L10n.t("farmer_techniques", currentLanguage),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = LeadingFarmerTranslations.getStoryMethods(farmer, currentLanguage),
                                        fontSize = 13.sp,
                                        color = AgriTextPrimary,
                                        lineHeight = 22.sp
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "প্রযুক্তির সারাংশ:"
                                            AppLanguage.HINDI -> "तकनीक का सारांश:"
                                            AppLanguage.ENGLISH -> "Technique Summary:"
                                        },
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = AgriEarthSecondary
                                    )
                                    Text(
                                        text = LeadingFarmerTranslations.getDisplayTechniques(farmer, currentLanguage),
                                        fontSize = 12.sp,
                                        color = AgriTextPrimary
                                    )
                                }
                            }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = L10n.t("farmer_lessons", currentLanguage),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Text(
                                        text = LeadingFarmerTranslations.getStoryLessons(farmer, currentLanguage),
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }

                        3 -> {
                            // CONTACTS & SOURCES TAB
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = L10n.t("farmer_public_contacts", currentLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    if (farmer.hasPublicContact) {
                                        if (farmer.officialPublicContact != null) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "অফিসিয়াল যোগাযোগ / অফিস:"
                                                            AppLanguage.HINDI -> "आधिकारिक संपर्क / कार्यालय:"
                                                            AppLanguage.ENGLISH -> "Official Contact / Office:"
                                                        },
                                                        fontSize = 11.sp,
                                                        color = AgriTextSecondary
                                                    )
                                                    Text(
                                                        text = LeadingFarmerTranslations.getDisplayPublicContact(farmer, currentLanguage)
                                                            ?: farmer.officialPublicContact.orEmpty(),
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                Button(
                                                    onClick = { makeCall(farmer.officialPublicContact) },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = AgriGreenPrimary,
                                                        contentColor = Color.White
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.heightIn(min = 48.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "কল করুন"
                                                            AppLanguage.HINDI -> "कॉल करें"
                                                            AppLanguage.ENGLISH -> "Call"
                                                        },
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }

                                        if (farmer.officialEmail != null) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "অফিশিয়াল ইমেইল:"
                                                            AppLanguage.HINDI -> "आधिकारिक ईमेल:"
                                                            AppLanguage.ENGLISH -> "Official Email:"
                                                        },
                                                        fontSize = 11.sp,
                                                        color = AgriTextSecondary
                                                    )
                                                    Text(text = farmer.officialEmail, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                                OutlinedButton(
                                                    onClick = { sendEmail(farmer.officialEmail) },
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.heightIn(min = 48.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = when (currentLanguage) {
                                                            AppLanguage.BENGALI -> "ইমেইল"
                                                            AppLanguage.HINDI -> "ईमेल"
                                                            AppLanguage.ENGLISH -> "Email"
                                                        },
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }

                                        if (farmer.officialWebsite != null) {
                                            Button(
                                                onClick = { openUrl(farmer.officialWebsite) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = AgriEarthSecondary,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "অফিশিয়াল ওয়েবসাইট ভিজিট করুন"
                                                        AppLanguage.HINDI -> "आधिकारिक वेबसाइट पर जाएं"
                                                        AppLanguage.ENGLISH -> "Visit Official Website"
                                                    },
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }

                                        if (farmer.officialFacebook != null) {
                                            OutlinedButton(
                                                onClick = { openUrl(farmer.officialFacebook) },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Facebook Page", fontSize = 12.sp, color = Color(0xFF1877F2), fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        if (farmer.officialInstagram != null) {
                                            OutlinedButton(
                                                onClick = { openUrl(farmer.officialInstagram) },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = Color(0xFFE4405F), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Instagram Profile", fontSize = 12.sp, color = Color(0xFFE4405F), fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        if (farmer.officialYoutube != null) {
                                            OutlinedButton(
                                                onClick = { openUrl(farmer.officialYoutube) },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("YouTube Channel", fontSize = 12.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        val displayOrg = LeadingFarmerTranslations.getDisplayOrg(farmer, currentLanguage)
                                        if (displayOrg != null) {
                                            Text(
                                                text = when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "সংস্থা / এফপিও: $displayOrg"
                                                    AppLanguage.HINDI -> "संगठन / एफपीओ: $displayOrg"
                                                    AppLanguage.ENGLISH -> "Organization / FPO: $displayOrg"
                                                },
                                                fontSize = 12.sp,
                                                color = AgriTextSecondary
                                            )
                                        }
                                    } else {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "ℹ️ ${LeadingFarmerTranslations.getDisplayContactNotice(farmer, currentLanguage)}",
                                                    fontSize = 12.sp,
                                                    color = AgriTextPrimary,
                                                    lineHeight = 18.sp
                                                )
                                                Text(
                                                    text = when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "ব্যক্তিগত গোপনীয়তা ও সুরক্ষার কারণে কোনো অপ্রকাশিত ব্যক্তিগত ফোন নম্বর প্রদর্শন করা হয় না।"
                                                        AppLanguage.HINDI -> "व्यक्तिगत गोपनीयता व सुरक्षा कारणों से कोई भी अप्रकाशित निजी फोन नंबर प्रदर्शित नहीं किया जाता है।"
                                                        AppLanguage.ENGLISH -> "Unverified private personal numbers are strictly withheld to preserve privacy and safety."
                                                    },
                                                    fontSize = 11.sp,
                                                    color = AgriTextSecondary
                                                )
                                            }
                                        }
                                    }

                                    // Request Contact Button
                                    Button(
                                        onClick = { showContactRequestDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AgriGreenPrimary,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("farmer_request_contact_btn")
                                    ) {
                                        Icon(imageVector = Icons.Default.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = L10n.t("dialog_contact_request", currentLanguage),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            // Sources and Verification Notice
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "📚 সরকারি ও নির্ভরযোগ্য তথ্যসূত্র (Sources):"
                                            AppLanguage.HINDI -> "📚 सरकारी व विश्वसनीय स्रोत (Sources):"
                                            AppLanguage.ENGLISH -> "📚 Verified Public Sources:"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = LeadingFarmerTranslations.getDisplaySources(farmer, currentLanguage),
                                        fontSize = 12.sp,
                                        color = AgriTextPrimary,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "সর্বশেষ তথ্য যাচাই: ${farmer.lastVerifiedDate}"
                                            AppLanguage.HINDI -> "अंतिम जानकारी सत्यापन: ${farmer.lastVerifiedDate}"
                                            AppLanguage.ENGLISH -> "Last Information Verification: ${farmer.lastVerifiedDate}"
                                        },
                                        fontSize = 11.sp,
                                        color = AgriEarthSecondary,
                                        fontWeight = FontWeight.Medium
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

    // Modal Contact Request Form Dialog
    if (showContactRequestDialog) {
        val narrow = isCompactNarrowScreen()
        var reqName by remember { mutableStateOf("") }
        var reqPhone by remember { mutableStateOf("") }
        var reqDistrict by remember { mutableStateOf("") }
        var reqCrop by remember { mutableStateOf("") }
        var reqPurpose by remember(currentLanguage) {
            mutableStateOf(
                when (currentLanguage) {
                    AppLanguage.BENGALI -> "প্রশিক্ষণ ও কৃষি পদ্ধতি অনুসন্ধান"
                    AppLanguage.HINDI -> "प्रशिक्षण व कृषि पद्धति पूछताछ"
                    AppLanguage.ENGLISH -> "Training & Farming Methods Inquiry"
                }
            )
        }
        var reqMessage by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = { if (!isSubmitting) showContactRequestDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp)
                        .imePadding(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .padding(18.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "যোগাযোগের অনুরোধ"
                                    AppLanguage.HINDI -> "संपर्क अनुरोध"
                                    AppLanguage.ENGLISH -> "Request Contact"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { showContactRequestDialog = false },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Text(
                            text = "$farmerName ($farmerLocation)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = AgriTextSecondary
                        )

                        // Strict Disclaimer
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI ->
                                        "⚠️ দ্রষ্টব্য: এই বার্তাটি সংশ্লিষ্ট কৃষক বা তাঁদের অনুমোদিত কৃষক উৎপাদনকারী সংস্থা (FPO/সমিতি)-তে প্রেরণ করা হবে। কৃষকদের সরাসরি কল বা বাধ্যতামূলক পরামর্শের নিশ্চয়তা দেওয়া হয় না।"
                                    AppLanguage.HINDI ->
                                        "⚠️ सूचना: यह अनुरोध संबंधित किसान या उनके अधिकृत कृषक उत्पादक संगठन (FPO) को भेजा जाएगा। सीधे कॉल या बाध्यकारी परामर्श की गारंटी नहीं है।"
                                    AppLanguage.ENGLISH ->
                                        "⚠️ Notice: This request will be routed to the farmer or their authorized FPO. Direct consultation or calls are not guaranteed unless consented."
                                },
                                fontSize = 11.sp,
                                color = AgriTextPrimary,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        OutlinedTextField(
                            value = reqName,
                            onValueChange = { reqName = it },
                            label = { Text(L10n.t("field_your_name", currentLanguage)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = reqPhone,
                            onValueChange = { reqPhone = it },
                            label = { Text(L10n.t("field_mobile_number", currentLanguage)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (narrow) {
                            OutlinedTextField(
                                value = reqDistrict,
                                onValueChange = { reqDistrict = it },
                                label = { Text(L10n.t("field_district", currentLanguage)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = reqCrop,
                                onValueChange = { reqCrop = it },
                                label = { Text(L10n.t("field_your_crop", currentLanguage)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = reqDistrict,
                                    onValueChange = { reqDistrict = it },
                                    label = { Text(L10n.t("field_district", currentLanguage)) },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = reqCrop,
                                    onValueChange = { reqCrop = it },
                                    label = { Text(L10n.t("field_your_crop", currentLanguage)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = reqMessage,
                            onValueChange = { reqMessage = it },
                            label = { Text(L10n.t("field_message_reason", currentLanguage)) },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (reqName.isBlank() || reqPhone.isBlank() || reqMessage.isBlank()) {
                                    val validationMsg = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অনুগ্রহ করে নাম, ফোন এবং বার্তা লিখুন"
                                        AppLanguage.HINDI -> "कृपया नाम, फोन और संदेश दर्ज करें"
                                        AppLanguage.ENGLISH -> "Please enter your name, phone, and message"
                                    }
                                    Toast.makeText(context, validationMsg, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isSubmitting = true
                                scope.launch {
                                    try {
                                        repository.submitContactRequest(
                                            farmerId = farmer.id,
                                            farmerName = farmerName,
                                            requesterName = reqName,
                                            requesterPhone = reqPhone,
                                            requesterDistrict = reqDistrict.ifBlank {
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "উল্লেখিত নয়"
                                                    AppLanguage.HINDI -> "उल्लिखित नहीं"
                                                    AppLanguage.ENGLISH -> "Not specified"
                                                }
                                            },
                                            requesterCrop = reqCrop.ifBlank {
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "সাধারণ"
                                                    AppLanguage.HINDI -> "सामान्य"
                                                    AppLanguage.ENGLISH -> "General"
                                                }
                                            },
                                            purposeCategory = reqPurpose,
                                            message = reqMessage
                                        )
                                        isSubmitting = false
                                        showContactRequestDialog = false
                                        Toast.makeText(
                                            context,
                                            L10n.t("msg_contact_success", currentLanguage),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (_: Exception) {
                                        isSubmitting = false
                                        Toast.makeText(
                                            context,
                                            L10n.t("error", currentLanguage),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AgriGreenPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("farmer_contact_submit_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSubmitting) L10n.t("msg_submitting", currentLanguage) else L10n.t("btn_submit_request", currentLanguage),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
