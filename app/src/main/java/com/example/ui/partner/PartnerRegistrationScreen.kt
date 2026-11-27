package com.example.ui.partner

import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalSuccessBg
import com.example.ui.theme.RoyalSuccessText
import com.example.ui.theme.RoyalWarningBg
import com.example.util.isCompactNarrowScreen
import com.example.util.toLocalizedIntOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartnerRegistrationScreen(
    userId: String,
    applicantName: String,
    applicantPhone: String?,
    applicantEmail: String?,
    currentLanguage: AppLanguage,
    repository: PartnerRoleRepository,
    onBack: () -> Unit,
    onOpenPartnerDashboard: () -> Unit = {}
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isNarrow = isCompactNarrowScreen()

    val existingReg by repository.observeUserRegistration(userId).collectAsState(initial = null)
    val membership by repository.observeUserRoleMembership(userId).collectAsState(initial = null)

    var isEditMode by remember(existingReg?.id, existingReg?.status) {
        mutableStateOf(
            existingReg == null || existingReg?.status == PartnerRegistrationStatus.REJECTED.code
        )
    }

    var businessName by remember(existingReg) { mutableStateOf(existingReg?.businessName.orEmpty()) }
    var contactPhone by remember(existingReg) {
        mutableStateOf(existingReg?.contactPhone?.ifBlank { applicantPhone.orEmpty() } ?: applicantPhone.orEmpty())
    }
    var whatsappPhone by remember(existingReg) {
        mutableStateOf(existingReg?.whatsappPhone?.ifBlank { applicantPhone.orEmpty() } ?: applicantPhone.orEmpty())
    }
    var contactEmail by remember(existingReg) {
        mutableStateOf(existingReg?.contactEmail?.ifBlank { applicantEmail.orEmpty() } ?: applicantEmail.orEmpty())
    }
    var businessLocation by remember(existingReg) { mutableStateOf(existingReg?.businessLocation.orEmpty()) }
    var district by remember(existingReg) { mutableStateOf(existingReg?.district ?: "Jalpaiguri") }
    var state by remember(existingReg) { mutableStateOf(existingReg?.state ?: "West Bengal") }
    var pinCode by remember(existingReg) { mutableStateOf(existingReg?.pinCode.orEmpty()) }
    var serviceArea by remember(existingReg) { mutableStateOf(existingReg?.serviceArea.orEmpty()) }
    var serviceRadiusKm by remember(existingReg) {
        mutableStateOf((existingReg?.serviceRadiusKm ?: 35).toString())
    }
    var selectedCategories by remember(existingReg) {
        mutableStateOf(
            existingReg?.serviceCategories
                ?.map { AppUserRole.fromCode(it) }
                ?.filter { it.isSelectableInPartnerRegistration }
                ?.toSet()
                ?: emptySet()
        )
    }
    var documentType by remember(existingReg) {
        mutableStateOf(existingReg?.documentType?.ifBlank { "Trade License" } ?: "Trade License")
    }
    var licenseNumber by remember(existingReg) { mutableStateOf(existingReg?.licenseNumber.orEmpty()) }
    var gstNumber by remember(existingReg) { mutableStateOf(existingReg?.gstNumber.orEmpty()) }
    var documentNotes by remember(existingReg) { mutableStateOf(existingReg?.documentNotes.orEmpty()) }
    var attachedDocNames by remember(existingReg) {
        mutableStateOf(existingReg?.verificationDocumentNames.orEmpty())
    }

    var formError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val isQuickLogin = remember(userId) { UserSessionManager.isLocalQuickLogin(context) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(3)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val resolvedNames = uris.mapIndexed { idx, uri ->
                val cursorName = runCatching {
                    context.contentResolver.query(
                        uri,
                        arrayOf(OpenableColumns.DISPLAY_NAME),
                        null,
                        null,
                        null
                    )?.use { c ->
                        if (c.moveToFirst()) {
                            val colIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (colIdx >= 0) c.getString(colIdx) else null
                        } else null
                    }
                }.getOrNull()
                cursorName?.takeIf { it.isNotBlank() } ?: "partner_doc_${idx + 1}.jpg"
            }
            attachedDocNames = (attachedDocNames + resolvedNames).distinct().take(5)
        }
    }

    val regStatus = PartnerRegistrationStatus.fromCode(existingReg?.status)
    val isVerifiedPartner = existingReg?.isVerifiedPartner() == true ||
        (membership?.platformVerified == true && membership?.hasApprovedPartnerRole() == true)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
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
                            .testTag("partner_reg_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ফিরে যান"
                                AppLanguage.HINDI -> "वापस जाएँ"
                                AppLanguage.ENGLISH -> "Back"
                            },
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "পার্টনার হিসেবে যুক্ত হন"
                                AppLanguage.HINDI -> "पार्टनर के रूप में जुड़ें"
                                AppLanguage.ENGLISH -> "Become a Partner"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নার্সারি, যন্ত্রপাতি, ক্রয় ও পরিষেবা নিবন্ধন"
                                AppLanguage.HINDI -> "नर्सरी, मशीनरी, खरीद व कृषि सेवा पंजीकरण"
                                AppLanguage.ENGLISH -> "Nursery, Machinery, Buyer & Service Verification"
                            },
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                .imePadding()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(if (isNarrow) 12.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Local Quick Login Gate Notice
                if (isQuickLogin) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = RoyalWarningBg),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("partner_reg_quick_login_notice")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = RoyalGold)
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "ক্লাউড পার্টনার অনুমোদনের জন্য Google লগইন প্রয়োজন"
                                        AppLanguage.HINDI -> "क्लाउड पार्टनर स्वीकृति के लिए Google लॉगिन आवश्यक है"
                                        AppLanguage.ENGLISH -> "Google Sign-In Required for Cloud Partner Verification"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalGreenPrimaryBg,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অফলাইন কুইক লগইন কেবল কৃষক সুবিধার জন্য। প্রোফাইল থেকে Google অ্যাকাউন্ট সংযুক্ত করে পার্টনার আবেদন জমা দিন।"
                                    AppLanguage.HINDI -> "ऑफ़लाइन क्विक लॉगिन केवल किसान उपयोग के लिए है। प्रोफ़ाइल से Google खाता जोड़कर पार्टनर आवेदन जमा करें।"
                                    AppLanguage.ENGLISH -> "Quick Login accounts are restricted to local Farmer tools. Connect Google Sign-In in Profile to submit verified partner applications."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = RoyalGreenPrimaryBg
                            )
                        }
                    }
                }

                // Status & Verification Lifecycle Card (Shown whenever user has a registration or approved role)
                if (existingReg != null || isVerifiedPartner) {
                    PartnerStatusLifecycleCard(
                        registration = existingReg,
                        isVerifiedPartner = isVerifiedPartner,
                        currentLanguage = currentLanguage,
                        onOpenPartnerDashboard = onOpenPartnerDashboard,
                        onEditResubmit = { isEditMode = true }
                    )
                }

                // Registration Form (shown when not yet submitted, editing rejected/pending, or updating details)
                AnimatedVisibility(visible = isEditMode || existingReg == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Role / Service Categories Card (Strictly excludes Administrator)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Handshake, contentDescription = null, tint = AgriGreenPrimary)
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "১. আপনার পার্টনার পরিষেবা বিভাগ নির্বাচন করুন *"
                                            AppLanguage.HINDI -> "1. अपनी पार्टनर सेवा श्रेणियाँ चुनें *"
                                            AppLanguage.ENGLISH -> "1. Select Partner Service Categories *"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = AgriTextPrimary
                                    )
                                }
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "একটি অ্যাকাউন্ট থেকেই একাধিক অনুমোদিত ব্যবসা ভূমিকা পরিচালনা করা যাবে (অ্যাডমিন ভূমিকা নির্বাচনযোগ্য নয়)।"
                                        AppLanguage.HINDI -> "एक ही खाते से कई स्वीकृत भूमिकाएँ चलाई जा सकती हैं (एडमिन भूमिका चयन योग्य नहीं है)।"
                                        AppLanguage.ENGLISH -> "One account can hold multiple approved marketplace roles. Administrator privileges cannot be self-selected."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgriTextSecondary
                                )

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AppUserRole.partnerRegistrationChoices.forEach { role ->
                                        val selected = role in selectedCategories
                                        FilterChip(
                                            selected = selected,
                                            onClick = {
                                                selectedCategories = if (selected) {
                                                    selectedCategories - role
                                                } else {
                                                    selectedCategories + role
                                                }
                                            },
                                            label = {
                                                Text(
                                                    text = role.getLabel(currentLanguage),
                                                    fontSize = 13.sp,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            leadingIcon = if (selected) {
                                                {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            } else null,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AgriGreenPrimary,
                                                selectedLabelColor = Color.White,
                                                selectedLeadingIconColor = RoyalGold
                                            ),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("partner_role_chip_${role.code}")
                                        )
                                    }
                                }
                            }
                        }

                        // Business & Contact Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Business, contentDescription = null, tint = AgriGreenPrimary)
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "২. ব্যবসা ও যোগাযোগের তথ্য"
                                            AppLanguage.HINDI -> "2. व्यवसाय व संपर्क विवरण"
                                            AppLanguage.ENGLISH -> "2. Business & Contact Details"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = AgriTextPrimary
                                    )
                                }

                                OutlinedTextField(
                                    value = businessName,
                                    onValueChange = { businessName = it },
                                    label = {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "ব্যবসা / প্রতিষ্ঠানের নাম *"
                                                AppLanguage.HINDI -> "व्यवसाय / प्रतिष्ठान का नाम *"
                                                AppLanguage.ENGLISH -> "Business / Firm Name *"
                                            }
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("partner_reg_business_input")
                                )

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    maxItemsInEachRow = if (isNarrow) 1 else 2
                                ) {
                                    OutlinedTextField(
                                        value = contactPhone,
                                        onValueChange = { contactPhone = it },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                        label = {
                                            Text(
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "যোগাযোগের মোবাইল নম্বর *"
                                                    AppLanguage.HINDI -> "संपर्क मोबाइल नंबर *"
                                                    AppLanguage.ENGLISH -> "Contact Phone *"
                                                }
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("partner_reg_phone_input")
                                    )
                                    OutlinedTextField(
                                        value = whatsappPhone,
                                        onValueChange = { whatsappPhone = it },
                                        label = {
                                            Text(
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "হোয়াটসঅ্যাপ নম্বর"
                                                    AppLanguage.HINDI -> "व्हाट्सऐप नंबर"
                                                    AppLanguage.ENGLISH -> "WhatsApp Number"
                                                }
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                OutlinedTextField(
                                    value = contactEmail,
                                    onValueChange = { contactEmail = it },
                                    label = {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "ব্যবসায়িক ইমেইল"
                                                AppLanguage.HINDI -> "व्यवसाय ईमेल"
                                                AppLanguage.ENGLISH -> "Business Email"
                                            }
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Location & Service Area Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AgriGreenPrimary)
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "৩. ব্যবসার ঠিকানা ও পরিষেবা এলাকা"
                                            AppLanguage.HINDI -> "3. व्यवसाय का स्थान व सेवा क्षेत्र"
                                            AppLanguage.ENGLISH -> "3. Business Location & Service Area"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = AgriTextPrimary
                                    )
                                }

                                OutlinedTextField(
                                    value = businessLocation,
                                    onValueChange = { businessLocation = it },
                                    label = {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "ব্যবসার পূর্ণ ঠিকানা *"
                                                AppLanguage.HINDI -> "व्यवसाय का पूरा पता *"
                                                AppLanguage.ENGLISH -> "Business Location / Full Address *"
                                            }
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("partner_reg_location_input")
                                )

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    maxItemsInEachRow = if (isNarrow) 1 else 3
                                ) {
                                    OutlinedTextField(
                                        value = district,
                                        onValueChange = { district = it },
                                        label = {
                                            Text(
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "জেলা"
                                                    AppLanguage.HINDI -> "ज़िला"
                                                    AppLanguage.ENGLISH -> "District"
                                                }
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = state,
                                        onValueChange = { state = it },
                                        label = {
                                            Text(
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "রাজ্য"
                                                    AppLanguage.HINDI -> "राज्य"
                                                    AppLanguage.ENGLISH -> "State"
                                                }
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = pinCode,
                                        onValueChange = { pinCode = it },
                                        label = {
                                            Text(
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "পিন কোড"
                                                    AppLanguage.HINDI -> "पिन कोड"
                                                    AppLanguage.ENGLISH -> "PIN Code"
                                                }
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    maxItemsInEachRow = if (isNarrow) 1 else 2
                                ) {
                                    OutlinedTextField(
                                        value = serviceArea,
                                        onValueChange = { serviceArea = it },
                                        label = {
                                            Text(
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "পরিষেবা এলাকা (ব্লক / জেলা) *"
                                                    AppLanguage.HINDI -> "सेवा क्षेत्र (ब्लॉक / ज़िला) *"
                                                    AppLanguage.ENGLISH -> "Service Area (Blocks / Towns) *"
                                                }
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(2f)
                                            .testTag("partner_reg_service_area_input")
                                    )
                                    OutlinedTextField(
                                        value = serviceRadiusKm,
                                        onValueChange = { serviceRadiusKm = it },
                                        label = {
                                            Text(
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "দূরত্ব (কিমি)"
                                                    AppLanguage.HINDI -> "दायरा (किमी)"
                                                    AppLanguage.ENGLISH -> "Radius (km)"
                                                }
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Verification Documents & Licenses Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Badge, contentDescription = null, tint = AgriGreenPrimary)
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "৪. যাচাইকরণ নথি ও লাইসেন্স"
                                            AppLanguage.HINDI -> "4. सत्यापन दस्तावेज़ व लाइसेंस"
                                            AppLanguage.ENGLISH -> "4. Verification Documents & Licenses"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = AgriTextPrimary
                                    )
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "Trade License",
                                        "Nursery License",
                                        "GST / Udyam",
                                        "Seed/Mandi License",
                                        "Identity Proof"
                                    ).forEach { docOpt ->
                                        FilterChip(
                                            selected = documentType == docOpt,
                                            onClick = { documentType = docOpt },
                                            label = { Text(docOpt, fontSize = 12.sp) },
                                            modifier = Modifier.heightIn(min = 40.dp)
                                        )
                                    }
                                }

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    maxItemsInEachRow = if (isNarrow) 1 else 2
                                ) {
                                    OutlinedTextField(
                                        value = licenseNumber,
                                        onValueChange = { licenseNumber = it },
                                        label = {
                                            Text(
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "লাইসেন্স / নিবন্ধন নম্বর"
                                                    AppLanguage.HINDI -> "लाइसेंस / पंजीकरण संख्या"
                                                    AppLanguage.ENGLISH -> "License / Registration No."
                                                }
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("partner_reg_license_input")
                                    )
                                    OutlinedTextField(
                                        value = gstNumber,
                                        onValueChange = { gstNumber = it },
                                        label = {
                                            Text(
                                                when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "GST / Udyam নম্বর (ঐচ্ছিক)"
                                                    AppLanguage.HINDI -> "GST / उद्यम संख्या (वैकल्पिक)"
                                                    AppLanguage.ENGLISH -> "GST / Udyam No. (Optional)"
                                                }
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                OutlinedTextField(
                                    value = documentNotes,
                                    onValueChange = { documentNotes = it },
                                    label = {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "নথি ও অভিজ্ঞতা সংক্রান্ত বিবরণ"
                                                AppLanguage.HINDI -> "दस्तावेज़ व अनुभव विवरण"
                                                AppLanguage.ENGLISH -> "Document & Business Notes"
                                            }
                                        )
                                    },
                                    minLines = 2,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("partner_reg_pick_docs_btn"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        when (currentLanguage) {
                                            AppLanguage.BENGALI -> "লাইসেন্স / দোকানের প্রমাণপত্র ছবি সংযুক্ত করুন"
                                            AppLanguage.HINDI -> "लाइसेंस / व्यवसाय प्रमाण फोटो जोड़ें"
                                            AppLanguage.ENGLISH -> "Attach License / Verification Photos"
                                        },
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (attachedDocNames.isNotEmpty()) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        attachedDocNames.forEach { fileName ->
                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(fileName, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    IconButton(
                                                        onClick = { attachedDocNames = attachedDocNames - fileName },
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (!formError.isNullOrBlank()) {
                            Text(
                                text = formError.orEmpty(),
                                color = AgriAlertRed,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("partner_reg_error_text")
                            )
                        }

                        Button(
                            enabled = !isSubmitting,
                            onClick = {
                                formError = null
                                if (isQuickLogin) {
                                    formError = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "জমা দিতে প্রোফাইল থেকে Google অ্যাকাউন্ট সংযুক্ত করুন।"
                                        AppLanguage.HINDI -> "आवेदन जमा करने के लिए प्रोफ़ाइल से Google खाता जोड़ें।"
                                        AppLanguage.ENGLISH -> "Connect a Google account in Profile before submitting partner verification."
                                    }
                                    return@Button
                                }
                                if (selectedCategories.isEmpty()) {
                                    formError = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "অন্তত একটি পরিষেবা বিভাগ নির্বাচন করুন।"
                                        AppLanguage.HINDI -> "कम से कम एक सेवा श्रेणी चुनें।"
                                        AppLanguage.ENGLISH -> "Please select at least one service category."
                                    }
                                    return@Button
                                }
                                if (businessName.isBlank() || contactPhone.isBlank() || businessLocation.isBlank() || serviceArea.isBlank()) {
                                    formError = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "সব বাধ্যতামূলক (*) তথ্য পূরণ করুন।"
                                        AppLanguage.HINDI -> "सभी अनिवार्य (*) फ़ील्ड भरें।"
                                        AppLanguage.ENGLISH -> "Please fill in all required (*) fields."
                                    }
                                    return@Button
                                }

                                isSubmitting = true
                                scope.launch {
                                    try {
                                        val res = repository.submitRegistration(
                                            PartnerRegistration(
                                                id = existingReg?.id.orEmpty(),
                                                userId = userId,
                                                applicantName = applicantName,
                                                businessName = businessName,
                                                contactPhone = contactPhone,
                                                whatsappPhone = whatsappPhone,
                                                contactEmail = contactEmail,
                                                businessLocation = businessLocation,
                                                district = district,
                                                state = state,
                                                pinCode = pinCode,
                                                serviceCategories = selectedCategories.map { it.code },
                                                serviceArea = serviceArea,
                                                serviceRadiusKm = serviceRadiusKm.toLocalizedIntOrNull() ?: 35,
                                                documentType = documentType,
                                                licenseNumber = licenseNumber,
                                                gstNumber = gstNumber,
                                                documentNotes = documentNotes,
                                                verificationDocumentNames = attachedDocNames
                                            )
                                        )
                                        if (res.isSuccess) {
                                            isEditMode = false
                                            val okMsg = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "পার্টনার নিবন্ধন আবেদন জমা হয়েছে (অপেক্ষমাণ)।"
                                                AppLanguage.HINDI -> "पार्टनर पंजीकरण आवेदन जमा हो गया (लंबित)।"
                                                AppLanguage.ENGLISH -> "Partner registration submitted (Pending verification)."
                                            }
                                            Toast.makeText(context, okMsg, Toast.LENGTH_LONG).show()
                                        } else {
                                            formError = res.exceptionOrNull()?.localizedMessage ?: "Submission failed"
                                        }
                                    } catch (e: CancellationException) {
                                        throw e
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("partner_reg_submit_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AgriGreenPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when {
                                        regStatus == PartnerRegistrationStatus.REJECTED -> when (currentLanguage) {
                                            AppLanguage.BENGALI -> "সংশোধন করে পুনরায় জমা দিন"
                                            AppLanguage.HINDI -> "सुधारकर पुनः जमा करें"
                                            AppLanguage.ENGLISH -> "Update & Resubmit Application"
                                        }
                                        else -> when (currentLanguage) {
                                            AppLanguage.BENGALI -> "পার্টনার নিবন্ধন জমা দিন"
                                            AppLanguage.HINDI -> "पार्टनर पंजीकरण जमा करें"
                                            AppLanguage.ENGLISH -> "Submit Partner Registration"
                                        }
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PartnerStatusLifecycleCard(
    registration: PartnerRegistration?,
    isVerifiedPartner: Boolean,
    currentLanguage: AppLanguage,
    onOpenPartnerDashboard: () -> Unit,
    onEditResubmit: () -> Unit
) {
    val status = PartnerRegistrationStatus.fromCode(registration?.status)
    val accentColor = when {
        isVerifiedPartner -> RoyalSuccessText
        status == PartnerRegistrationStatus.UNDER_REVIEW -> Color(0xFF0288D1)
        status == PartnerRegistrationStatus.REJECTED -> AgriAlertRed
        else -> RoyalGold
    }
    val containerColor = when {
        isVerifiedPartner -> RoyalSuccessBg
        status == PartnerRegistrationStatus.REJECTED -> com.example.ui.theme.RoyalAlertBg
        else -> RoyalWarningBg
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("partner_status_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isVerifiedPartner) Icons.Default.Verified else Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = accentColor
                    )
                    Text(
                        text = registration?.businessName?.ifBlank { null }
                            ?: when (currentLanguage) {
                                AppLanguage.BENGALI -> "পার্টনার স্থিতি"
                                AppLanguage.HINDI -> "पार्टनर स्थिति"
                                AppLanguage.ENGLISH -> "Partner Verification Status"
                            },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = RoyalGreenPrimaryBg
                    )
                }

                if (isVerifiedPartner) {
                    Surface(
                        color = RoyalGreenEmerald,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, RoyalGold),
                        modifier = Modifier.testTag("partner_verified_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = RoyalSoftGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "যাচাইকৃত পার্টনার"
                                    AppLanguage.HINDI -> "सत्यापित पार्टनर"
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

            // Lifecycle Stepper: Pending -> Under Review -> Approved / Rejected
            val steps = listOf(
                PartnerRegistrationStatus.PENDING,
                PartnerRegistrationStatus.UNDER_REVIEW,
                if (status == PartnerRegistrationStatus.REJECTED) PartnerRegistrationStatus.REJECTED else PartnerRegistrationStatus.APPROVED
            )
            val currentIdx = when {
                isVerifiedPartner || status == PartnerRegistrationStatus.APPROVED -> 2
                status == PartnerRegistrationStatus.REJECTED -> 2
                status == PartnerRegistrationStatus.UNDER_REVIEW -> 1
                else -> 0
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { idx, stepStatus ->
                    val reached = idx <= currentIdx
                    val dotColor = if (reached) {
                        if (stepStatus == PartnerRegistrationStatus.REJECTED) AgriAlertRed else AgriGreenPrimary
                    } else {
                        Color.Gray.copy(alpha = 0.45f)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Text(
                            text = stepStatus.getLabel(currentLanguage),
                            fontSize = 11.sp,
                            fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal,
                            color = RoyalGreenPrimaryBg,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            val rolesToShow = registration?.resolvedApprovedPartnerRoles()
                ?.takeIf { it.isNotEmpty() }
                ?: registration?.requestedPartnerRoles().orEmpty()
            if (rolesToShow.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rolesToShow.forEach { role ->
                        Surface(
                            color = Color.White.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = role.getLabel(currentLanguage),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RoyalGreenPrimaryBg,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (status == PartnerRegistrationStatus.REJECTED && !registration?.rejectionReason.isNullOrBlank()) {
                HorizontalDivider(color = AgriAlertRed.copy(alpha = 0.25f))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "বাতিলের কারণ: ${registration?.rejectionReason}"
                        AppLanguage.HINDI -> "अस्वीकृति का कारण: ${registration?.rejectionReason}"
                        AppLanguage.ENGLISH -> "Rejection Reason: ${registration?.rejectionReason}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AgriAlertRed
                )
            }

            if (isVerifiedPartner) {
                Button(
                    onClick = onOpenPartnerDashboard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("partner_reg_open_dashboard_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreenPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = RoyalSoftGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "পার্টনার ড্যাশবোর্ড খুলুন"
                            AppLanguage.HINDI -> "पार्टनर डैशबोर्ड खोलें"
                            AppLanguage.ENGLISH -> "Open Partner Dashboard"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (status == PartnerRegistrationStatus.REJECTED) {
                OutlinedButton(
                    onClick = onEditResubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("partner_reg_resubmit_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "তথ্য সংশোধন করুন"
                            AppLanguage.HINDI -> "विवरण सुधारें"
                            AppLanguage.ENGLISH -> "Edit & Resubmit Details"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
