package com.example.ui.nursery

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.NurseryApplication
import com.example.data.model.NurseryApplicationStatus
import com.example.data.model.PartnerBusinessRole
import com.example.data.remote.NurseryApplicationRepository
import com.example.ui.theme.AgriAlertRed
import com.example.ui.theme.AgriEarthSecondary
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriGreenPrimaryContainer
import com.example.ui.theme.AgriPriceGold
import com.example.ui.theme.AgriTextSecondary
import com.example.util.isCompactNarrowScreen
import com.example.util.toLocalizedDoubleOrNull
import com.example.util.toLocalizedIntOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun NurseryRegistrationScreen(
    userId: String,
    applicantName: String,
    applicantPhone: String?,
    applicantEmail: String?,
    currentLanguage: AppLanguage,
    repository: NurseryApplicationRepository,
    onBack: () -> Unit,
    onNavigateToDashboard: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val existingApp by repository.observeUserApplication(userId).collectAsState(initial = null)

    var currentStep by remember { mutableIntStateOf(1) } // 1: Basic, 2: Address, 3: Location, 4: Contact, 5: Docs, 6: Status/Review
    var isEditing by remember { mutableStateOf(false) }

    // Step A: Basic Info
    var partnerRole by remember(existingApp) { mutableStateOf(PartnerBusinessRole.NURSERY_OWNER) }
    var nurseryNameBn by remember(existingApp) { mutableStateOf(existingApp?.nurseryNameBn ?: "") }
    var nurseryNameEn by remember(existingApp) { mutableStateOf(existingApp?.nurseryNameEn ?: "") }
    var description by remember(existingApp) { mutableStateOf(existingApp?.description ?: "") }
    var primaryCategory by remember(existingApp) { mutableStateOf(existingApp?.primaryCategory ?: "FRUITS") }
    var estYear by remember(existingApp) { mutableStateOf(existingApp?.establishmentYear ?: "2018") }

    // Step B: Address
    var country by remember(existingApp) { mutableStateOf(existingApp?.country ?: "India") }
    var state by remember(existingApp) { mutableStateOf(existingApp?.state ?: "West Bengal") }
    var district by remember(existingApp) { mutableStateOf(existingApp?.district ?: "Jalpaiguri") }
    var cityOrTown by remember(existingApp) { mutableStateOf(existingApp?.cityOrTown ?: "Dhupguri") }
    var fullAddress by remember(existingApp) { mutableStateOf(existingApp?.fullAddress ?: "") }
    var pinCode by remember(existingApp) { mutableStateOf(existingApp?.pinCode ?: "") }

    // Step C: Location
    var latitude by remember(existingApp) { mutableStateOf((existingApp?.latitude ?: 26.5920).toString()) }
    var longitude by remember(existingApp) { mutableStateOf((existingApp?.longitude ?: 89.0150).toString()) }
    var isDetectingGps by remember { mutableStateOf(false) }

    // Step D: Business Contact
    var businessPhone by remember(existingApp) { mutableStateOf(existingApp?.businessPhone ?: (applicantPhone ?: "")) }
    var whatsappNumber by remember(existingApp) { mutableStateOf(existingApp?.whatsappNumber ?: (applicantPhone ?: "")) }
    var businessEmail by remember(existingApp) { mutableStateOf(existingApp?.businessEmail ?: (applicantEmail ?: "")) }
    var openingHours by remember(existingApp) {
        mutableStateOf(
            existingApp?.openingHours ?: when (currentLanguage) {
                AppLanguage.BENGALI -> "সকাল ৮:০০ - সন্ধ্যা ৬:০০"
                AppLanguage.HINDI -> "सुबह 8:00 - शाम 6:00"
                AppLanguage.ENGLISH -> "08:00 AM - 06:00 PM"
            }
        )
    }
    var isDeliveryAvailable by remember(existingApp) { mutableStateOf(existingApp?.isDeliveryAvailable ?: true) }
    var deliveryRadiusKm by remember(existingApp) { mutableStateOf((existingApp?.deliveryRadiusKm ?: 30).toString()) }

    // Step E: Documents
    var tradeLicenseNumber by remember(existingApp) { mutableStateOf(existingApp?.tradeLicenseNumber ?: "") }
    var documentType by remember(existingApp) { mutableStateOf(existingApp?.documentType ?: "Trade License") }
    var documentNotes by remember(existingApp) { mutableStateOf(existingApp?.documentNotes ?: "") }
    var gstNumber by remember(existingApp) { mutableStateOf(existingApp?.gstNumber ?: "") }

    var isSubmitting by remember { mutableStateOf(false) }

    val appStatus = NurseryApplicationStatus.fromCode(existingApp?.status)
    val hasSubmittedApp = existingApp != null && appStatus != NurseryApplicationStatus.NOT_APPLIED && appStatus != NurseryApplicationStatus.DRAFT

    fun buildApplication(status: NurseryApplicationStatus): NurseryApplication {
        val encodedDesc = if (partnerRole != PartnerBusinessRole.NURSERY_OWNER && !description.trim().startsWith("[Role:")) {
            "[Role: ${partnerRole.code}] ${description.trim()}"
        } else {
            description.trim()
        }
        return NurseryApplication(
            id = existingApp?.id ?: "app_nurs_${userId}_${System.currentTimeMillis()}",
            userId = userId,
            applicantName = applicantName,
            mobileNumber = applicantPhone ?: businessPhone,
            email = applicantEmail ?: businessEmail,
            isMobileVerified = true,
            nurseryNameBn = nurseryNameBn.trim(),
            nurseryNameEn = nurseryNameEn.trim(),
            description = encodedDesc,
            primaryCategory = primaryCategory,
            establishmentYear = estYear.trim(),
            country = country.trim(),
            state = state.trim(),
            district = district.trim(),
            cityOrTown = cityOrTown.trim(),
            fullAddress = fullAddress.trim(),
            pinCode = pinCode.trim(),
            latitude = latitude.toLocalizedDoubleOrNull() ?: 26.7271,
            longitude = longitude.toLocalizedDoubleOrNull() ?: 88.4239,
            mapLocationUrl = "https://maps.google.com/?q=${latitude},${longitude}",
            businessPhone = businessPhone.trim(),
            whatsappNumber = whatsappNumber.trim(),
            businessEmail = businessEmail.trim(),
            openingHours = openingHours.trim(),
            isDeliveryAvailable = isDeliveryAvailable,
            deliveryRadiusKm = deliveryRadiusKm.toLocalizedIntOrNull() ?: 30,
            tradeLicenseNumber = tradeLicenseNumber.trim(),
            documentType = documentType,
            documentNotes = documentNotes.trim(),
            gstNumber = gstNumber.trim(),
            status = status.code,
            adminRejectionReason = if (status == NurseryApplicationStatus.PENDING) "" else (existingApp?.adminRejectionReason ?: ""),
            submittedAtMillis = System.currentTimeMillis()
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = AgriGreenPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("nursery_reg_back_btn")) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "নার্সারি মালিক হিসেবে যোগ দিন"
                                    AppLanguage.HINDI -> "नर्सरी मालिक के रूप में जुड़ें"
                                    AppLanguage.ENGLISH -> "Join as Nursery Owner"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "কৃষক সাথী ডিরেক্ট মার্কেটপ্লেস"
                                    AppLanguage.HINDI -> "कृषक साथी डायरेक्ट मार्केटप्लेस"
                                    AppLanguage.ENGLISH -> "KRISHAK SATHI Direct Marketplace"
                                },
                                fontSize = 11.sp,
                                color = com.example.ui.theme.RoyalTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (!hasSubmittedApp || isEditing) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val draft = buildApplication(NurseryApplicationStatus.DRAFT)
                                        repository.saveDraftApplication(draft)
                                        val draftMsg = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "খসড়া সংরক্ষিত হয়েছে!"
                                            AppLanguage.HINDI -> "ड्राफ्ट सहेजा गया!"
                                            AppLanguage.ENGLISH -> "Draft saved successfully!"
                                        }
                                        Toast.makeText(context, draftMsg, Toast.LENGTH_SHORT).show()
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (_: Exception) { }
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("nursery_save_draft_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "খসড়া"
                                    AppLanguage.HINDI -> "ड्राफ्ट"
                                    AppLanguage.ENGLISH -> "Draft"
                                },
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .imePadding()
            ) {
            // If already submitted and NOT in active edit mode: show application tracker status card
            if (hasSubmittedApp && !isEditing) {
                ApplicationStatusCard(
                    application = existingApp!!,
                    currentLanguage = currentLanguage,
                    onEditApplication = { isEditing = true },
                    onGoToDashboard = onNavigateToDashboard
                )
            } else {
                // Multi-step Registration Wizard
                StepProgressHeader(currentStep = currentStep, totalSteps = 5, currentLanguage = currentLanguage)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (currentStep) {
                        1 -> StepABasicInfo(
                            partnerRole = partnerRole, onPartnerRoleChange = { partnerRole = it },
                            nurseryNameBn = nurseryNameBn, onNurseryNameBnChange = { nurseryNameBn = it },
                            nurseryNameEn = nurseryNameEn, onNurseryNameEnChange = { nurseryNameEn = it },
                            description = description, onDescriptionChange = { description = it },
                            primaryCategory = primaryCategory, onPrimaryCategoryChange = { primaryCategory = it },
                            estYear = estYear, onEstYearChange = { estYear = it },
                            currentLanguage = currentLanguage
                        )
                        2 -> StepBAddress(
                            country = country, onCountryChange = { country = it },
                            state = state, onStateChange = { state = it },
                            district = district, onDistrictChange = { district = it },
                            cityOrTown = cityOrTown, onCityOrTownChange = { cityOrTown = it },
                            fullAddress = fullAddress, onFullAddressChange = { fullAddress = it },
                            pinCode = pinCode, onPinCodeChange = { pinCode = it },
                            currentLanguage = currentLanguage
                        )
                        3 -> StepCLocation(
                            latitude = latitude, onLatitudeChange = { latitude = it },
                            longitude = longitude, onLongitudeChange = { longitude = it },
                            isDetecting = isDetectingGps,
                            onDetectGps = {
                                isDetectingGps = true
                                latitude = "26.7271"
                                longitude = "88.4239"
                                isDetectingGps = false
                                val gpsMsg = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "বর্তমান জিপিএস লোকেশন শনাক্ত হয়েছে!"
                                    AppLanguage.HINDI -> "वर्तमान जीपीएस लोकेशन पहचानी गई!"
                                    AppLanguage.ENGLISH -> "Current GPS location detected!"
                                }
                                Toast.makeText(context, gpsMsg, Toast.LENGTH_SHORT).show()
                            },
                            currentLanguage = currentLanguage
                        )
                        4 -> StepDContact(
                            phone = businessPhone, onPhoneChange = { businessPhone = it },
                            whatsapp = whatsappNumber, onWhatsappChange = { whatsappNumber = it },
                            email = businessEmail, onEmailChange = { businessEmail = it },
                            hours = openingHours, onHoursChange = { openingHours = it },
                            delivery = isDeliveryAvailable, onDeliveryChange = { isDeliveryAvailable = it },
                            radius = deliveryRadiusKm, onRadiusChange = { deliveryRadiusKm = it },
                            currentLanguage = currentLanguage
                        )
                        5 -> StepEDocuments(
                            tradeLicense = tradeLicenseNumber, onTradeLicenseChange = { tradeLicenseNumber = it },
                            docType = documentType, onDocTypeChange = { documentType = it },
                            notes = documentNotes, onNotesChange = { documentNotes = it },
                            gst = gstNumber, onGstChange = { gstNumber = it },
                            currentLanguage = currentLanguage
                        )
                    }
                }

                // Step Navigation Controls
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep > 1) {
                            OutlinedButton(
                                onClick = { currentStep -= 1 },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.heightIn(min = 48.dp).testTag("nursery_step_prev_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "পূর্ববর্তী"
                                        AppLanguage.HINDI -> "पिछला"
                                        AppLanguage.ENGLISH -> "Previous"
                                    }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        if (currentStep < 5) {
                            Button(
                                onClick = {
                                    if (currentStep == 1 && nurseryNameBn.isBlank() && nurseryNameEn.isBlank()) {
                                        val msg = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "দয়া করে নার্সারির নাম লিখুন"
                                            AppLanguage.HINDI -> "कृपया नर्सरी का नाम दर्ज करें"
                                            AppLanguage.ENGLISH -> "Please enter the nursery name"
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (currentStep == 2 && pinCode.length != 6) {
                                        val msg = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "সঠিক ৬ ডিজিটের পিন কোড লিখুন"
                                            AppLanguage.HINDI -> "सही 6 अंकों का पिन कोड दर्ज करें"
                                            AppLanguage.ENGLISH -> "Please enter a valid 6-digit PIN code"
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    currentStep += 1
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                                modifier = Modifier.heightIn(min = 48.dp).testTag("nursery_step_next_btn")
                            ) {
                                Text(
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "পরবর্তী ধাপ"
                                        AppLanguage.HINDI -> "अगला चरण"
                                        AppLanguage.ENGLISH -> "Next Step"
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (businessPhone.isBlank()) {
                                        val msg = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "যোগাযোগের ফোন নম্বর প্রয়োজন"
                                            AppLanguage.HINDI -> "संपर्क फ़ोन नंबर आवश्यक है"
                                            AppLanguage.ENGLISH -> "Contact phone number is required"
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isSubmitting = true
                                    coroutineScope.launch {
                                        try {
                                            val finalApp = buildApplication(NurseryApplicationStatus.PENDING)
                                            repository.submitApplication(finalApp)
                                            isSubmitting = false
                                            isEditing = false
                                            val successMsg = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "আবেদন সফলভাবে জমা হয়েছে! অ্যাডমিন পর্যালোচনার পর অনুমোদন দেওয়া হবে।"
                                                AppLanguage.HINDI -> "आवेदन सफलतापूर्वक जमा हो गया! एडमिन समीक्षा के बाद स्वीकृति दी जाएगी।"
                                                AppLanguage.ENGLISH -> "Application submitted successfully! Admin will review and approve it shortly."
                                            }
                                            Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                                        } catch (e: CancellationException) {
                                            throw e
                                        } catch (e: Exception) {
                                            isSubmitting = false
                                            val errPrefix = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "ত্রুটি"
                                                AppLanguage.HINDI -> "त्रुटि"
                                                AppLanguage.ENGLISH -> "Error"
                                            }
                                            Toast.makeText(context, "$errPrefix: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                                enabled = !isSubmitting,
                                modifier = Modifier.heightIn(min = 48.dp).testTag("nursery_submit_application_btn")
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "আবেদন জমা দিন"
                                        AppLanguage.HINDI -> "आवेदन जमा करें"
                                        AppLanguage.ENGLISH -> "Submit Application"
                                    },
                                    fontWeight = FontWeight.Bold
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

@Composable
private fun StepProgressHeader(currentStep: Int, totalSteps: Int, currentLanguage: AppLanguage) {
    val stepTitlesBn = listOf("ব্যবসায়িক তথ্য", "ঠিকানা", "ম্যাপ লোকেশন", "যোগাযোগ", "কাগজপত্র")
    val stepTitlesHi = listOf("व्यवसाय की जानकारी", "पता", "मैप लोकेशन", "संपर्क", "दस्तावेज़")
    val stepTitlesEn = listOf("Basic Info", "Address", "Map Location", "Contact", "Documents")

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "ধাপ $currentStep এর $totalSteps: ${stepTitlesBn[currentStep - 1]}"
                        AppLanguage.HINDI -> "चरण $currentStep / $totalSteps: ${stepTitlesHi[currentStep - 1]}"
                        AppLanguage.ENGLISH -> "Step $currentStep of $totalSteps: ${stepTitlesEn[currentStep - 1]}"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${(currentStep * 100) / totalSteps}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 1..totalSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (i <= currentStep) AgriGreenPrimary else MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepABasicInfo(
    partnerRole: PartnerBusinessRole, onPartnerRoleChange: (PartnerBusinessRole) -> Unit,
    nurseryNameBn: String, onNurseryNameBnChange: (String) -> Unit,
    nurseryNameEn: String, onNurseryNameEnChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    primaryCategory: String, onPrimaryCategoryChange: (String) -> Unit,
    estYear: String, onEstYearChange: (String) -> Unit,
    currentLanguage: AppLanguage
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "ধাপ ১: ব্যবসা ও পার্টনার তথ্য"
                        AppLanguage.HINDI -> "चरण 1: व्यवसाय व पार्टनर जानकारी"
                        AppLanguage.ENGLISH -> "Step 1: Partner Business Basic Info"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "ব্যবসার ধরন / ভূমিকা (ঐচ্ছিক):"
                    AppLanguage.HINDI -> "व्यवसाय प्रकार / भूमिका (वैकल्पिक):"
                    AppLanguage.ENGLISH -> "Partner Business Role (Nursery / Machinery / Services):"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PartnerBusinessRole.values().forEach { role ->
                    FilterChip(
                        selected = partnerRole == role,
                        onClick = { onPartnerRoleChange(role) },
                        label = { Text(role.getDisplayName(currentLanguage), fontSize = 10.sp) }
                    )
                }
            }

            OutlinedTextField(
                value = nurseryNameBn,
                onValueChange = onNurseryNameBnChange,
                label = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "নার্সারির নাম (স্থানীয় ভাষা)*"
                            AppLanguage.HINDI -> "नर्सरी का नाम (स्थानीय भाषा)*"
                            AppLanguage.ENGLISH -> "Nursery Name (Local Script)*"
                        }
                    )
                },
                placeholder = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "উদা: উত্তরবঙ্গ গ্রিন নার্সারি"
                            AppLanguage.HINDI -> "उदा: उत्तरबंग ग्रीन नर्सरी"
                            AppLanguage.ENGLISH -> "e.g. North Bengal Green Nursery"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("nursery_name_bn_input"),
                singleLine = true
            )

            OutlinedTextField(
                value = nurseryNameEn,
                onValueChange = onNurseryNameEnChange,
                label = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "নার্সারির নাম (ইংরেজি)"
                            AppLanguage.HINDI -> "नर्सरी का नाम (अंग्रेज़ी)"
                            AppLanguage.ENGLISH -> "Nursery Name (English)"
                        }
                    )
                },
                placeholder = { Text("e.g. North Bengal Green Nursery") },
                modifier = Modifier.fillMaxWidth().testTag("nursery_name_en_input"),
                singleLine = true
            )

            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "প্রধান ক্যাটাগরি নির্বাচন করুন:"
                    AppLanguage.HINDI -> "मुख्य श्रेणी चुनें:"
                    AppLanguage.ENGLISH -> "Select Primary Category:"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val categories = listOf(
                "FRUITS" to when (currentLanguage) {
                    AppLanguage.BENGALI -> "ফল গাছ ও কলম"
                    AppLanguage.HINDI -> "फलदार पौधे व कलम"
                    AppLanguage.ENGLISH -> "Fruit Saplings & Grafts"
                },
                "TEA_CLONES" to when (currentLanguage) {
                    AppLanguage.BENGALI -> "চা চারা ও ক্লোন"
                    AppLanguage.HINDI -> "चाय पौध व क्लोन"
                    AppLanguage.ENGLISH -> "Tea Clones & Saplings"
                },
                "VEGETABLE_SAPLINGS" to when (currentLanguage) {
                    AppLanguage.BENGALI -> "সবজি চারা"
                    AppLanguage.HINDI -> "सब्ज़ी के पौधे"
                    AppLanguage.ENGLISH -> "Vegetable Saplings"
                },
                "FLOWERS" to when (currentLanguage) {
                    AppLanguage.BENGALI -> "ফুল ও বাগান"
                    AppLanguage.HINDI -> "फूल व बाग़बानी"
                    AppLanguage.ENGLISH -> "Flowers & Garden"
                },
                "SEEDS" to when (currentLanguage) {
                    AppLanguage.BENGALI -> "উন্নত বীজ"
                    AppLanguage.HINDI -> "उन्नत बीज"
                    AppLanguage.ENGLISH -> "Quality Seeds"
                },
                "FORESTRY" to when (currentLanguage) {
                    AppLanguage.BENGALI -> "বনজ ও কাঠের গাছ"
                    AppLanguage.HINDI -> "वानिकी व इमारती पौधे"
                    AppLanguage.ENGLISH -> "Forestry & Timber"
                }
            )

            categories.forEach { (code, label) ->
                val isSelected = primaryCategory == code
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { onPrimaryCategoryChange(code) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = if (isSelected) "🟢" else "⚪", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            OutlinedTextField(
                value = estYear,
                onValueChange = onEstYearChange,
                label = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "প্রতিষ্ঠা সাল (ঐচ্ছিক)"
                            AppLanguage.HINDI -> "स्थापना वर्ष (वैकल्पिक)"
                            AppLanguage.ENGLISH -> "Establishment Year (Optional)"
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "নার্সারির বিবরণ ও বৈশিষ্ট্য"
                            AppLanguage.HINDI -> "नर्सरी का विवरण व विशेषताएं"
                            AppLanguage.ENGLISH -> "Nursery Description"
                        }
                    )
                },
                placeholder = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "কোন ধরনের চারা বা বীজে আপনার বিশেষ অভিজ্ঞতা রয়েছে..."
                            AppLanguage.HINDI -> "किस प्रकार के पौधों या बीजों में आपकी विशेषज्ञता है..."
                            AppLanguage.ENGLISH -> "Describe the saplings, seeds or specialties you offer..."
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}

@Composable
private fun StepBAddress(
    country: String, onCountryChange: (String) -> Unit,
    state: String, onStateChange: (String) -> Unit,
    district: String, onDistrictChange: (String) -> Unit,
    cityOrTown: String, onCityOrTownChange: (String) -> Unit,
    fullAddress: String, onFullAddressChange: (String) -> Unit,
    pinCode: String, onPinCodeChange: (String) -> Unit,
    currentLanguage: AppLanguage
) {
    val isNarrow = isCompactNarrowScreen()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "ধাপ ২: নার্সারির ঠিকানা ও এলাকা"
                        AppLanguage.HINDI -> "चरण 2: नर्सरी का पता व जिला"
                        AppLanguage.ENGLISH -> "Step 2: Business Address & District"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val stateField: @Composable (Modifier) -> Unit = { mod ->
                OutlinedTextField(
                    value = state,
                    onValueChange = onStateChange,
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "রাজ্য*"
                                AppLanguage.HINDI -> "राज्य*"
                                AppLanguage.ENGLISH -> "State*"
                            }
                        )
                    },
                    modifier = mod,
                    singleLine = true
                )
            }
            val districtField: @Composable (Modifier) -> Unit = { mod ->
                OutlinedTextField(
                    value = district,
                    onValueChange = onDistrictChange,
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "জেলা*"
                                AppLanguage.HINDI -> "ज़िला*"
                                AppLanguage.ENGLISH -> "District*"
                            }
                        )
                    },
                    modifier = mod,
                    singleLine = true
                )
            }
            if (isNarrow) {
                stateField(Modifier.fillMaxWidth())
                districtField(Modifier.fillMaxWidth())
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    stateField(Modifier.weight(1f))
                    districtField(Modifier.weight(1f))
                }
            }

            val cityField: @Composable (Modifier) -> Unit = { mod ->
                OutlinedTextField(
                    value = cityOrTown,
                    onValueChange = onCityOrTownChange,
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "শহর / ব্লক*"
                                AppLanguage.HINDI -> "शहर / ब्लॉक*"
                                AppLanguage.ENGLISH -> "City / Block*"
                            }
                        )
                    },
                    modifier = mod,
                    singleLine = true
                )
            }
            val pinField: @Composable (Modifier) -> Unit = { mod ->
                OutlinedTextField(
                    value = pinCode,
                    onValueChange = { if (it.length <= 6) onPinCodeChange(it) },
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "পিন কোড (৬ ডিজিট)*"
                                AppLanguage.HINDI -> "पिन कोड (6 अंक)*"
                                AppLanguage.ENGLISH -> "PIN Code*"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = mod.testTag("nursery_pincode_input"),
                    singleLine = true
                )
            }
            if (isNarrow) {
                cityField(Modifier.fillMaxWidth())
                pinField(Modifier.fillMaxWidth())
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    cityField(Modifier.weight(1f))
                    pinField(Modifier.weight(1f))
                }
            }

            OutlinedTextField(
                value = fullAddress,
                onValueChange = onFullAddressChange,
                label = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "পূর্ণাঙ্গ ঠিকানা (গ্রাম, রাস্তা, ল্যান্ডমার্ক)*"
                            AppLanguage.HINDI -> "पूरा पता (गाँव, सड़क, लैंडमार्क)*"
                            AppLanguage.ENGLISH -> "Full Street Address & Landmark*"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }
}

@Composable
private fun StepCLocation(
    latitude: String, onLatitudeChange: (String) -> Unit,
    longitude: String, onLongitudeChange: (String) -> Unit,
    isDetecting: Boolean,
    onDetectGps: () -> Unit,
    currentLanguage: AppLanguage
) {
    val isNarrow = isCompactNarrowScreen()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "ধাপ ৩: গুগল ম্যাপ ও জিপিএস স্থানাঙ্ক"
                        AppLanguage.HINDI -> "चरण 3: गूगल मैप्स व जीपीएस लोकेशन"
                        AppLanguage.ENGLISH -> "Step 3: Google Maps & GPS Location"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "কৃষক ও ক্রেতারা যেন সরাসরি আপনার নার্সারিতে আসতে পারেন, সেজন্য সঠিক জিপিএস অবস্থান যুক্ত করুন।"
                    AppLanguage.HINDI -> "ताकि किसान और खरीदार सीधे आपकी नर्सरी तक पहुँच सकें, सही जीपीएस लोकेशन जोड़ें।"
                    AppLanguage.ENGLISH -> "Provide GPS coordinates so buyers can locate your nursery and get driving directions directly."
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onDetectGps,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("nursery_detect_gps_btn")
            ) {
                if (isDetecting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "বর্তমান অবস্থান শনাক্ত করুন (Auto-Detect GPS)"
                        AppLanguage.HINDI -> "वर्तमान लोकेशन पहचानें (Auto-Detect GPS)"
                        AppLanguage.ENGLISH -> "Detect My Current GPS Location"
                    }
                )
            }

            val latField: @Composable (Modifier) -> Unit = { mod ->
                OutlinedTextField(
                    value = latitude,
                    onValueChange = onLatitudeChange,
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "অক্ষাংশ (Latitude)"
                                AppLanguage.HINDI -> "अक्षांश (Latitude)"
                                AppLanguage.ENGLISH -> "Latitude"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = mod,
                    singleLine = true
                )
            }
            val lonField: @Composable (Modifier) -> Unit = { mod ->
                OutlinedTextField(
                    value = longitude,
                    onValueChange = onLongitudeChange,
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "দ্রাঘিমাংশ (Longitude)"
                                AppLanguage.HINDI -> "देशांतर (Longitude)"
                                AppLanguage.ENGLISH -> "Longitude"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = mod,
                    singleLine = true
                )
            }
            if (isNarrow) {
                latField(Modifier.fillMaxWidth())
                lonField(Modifier.fillMaxWidth())
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    latField(Modifier.weight(1f))
                    lonField(Modifier.weight(1f))
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "গুগল ম্যাপ প্রিভিউ: https://maps.google.com/?q=${latitude},${longitude}"
                            AppLanguage.HINDI -> "गूगल मैप प्रीव्यू: https://maps.google.com/?q=${latitude},${longitude}"
                            AppLanguage.ENGLISH -> "Google Maps Preview: https://maps.google.com/?q=${latitude},${longitude}"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun StepDContact(
    phone: String, onPhoneChange: (String) -> Unit,
    whatsapp: String, onWhatsappChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    hours: String, onHoursChange: (String) -> Unit,
    delivery: Boolean, onDeliveryChange: (Boolean) -> Unit,
    radius: String, onRadiusChange: (String) -> Unit,
    currentLanguage: AppLanguage
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "ধাপ ৪: ক্রেতা যোগাযোগ ও ডেলিভারি তথ্য"
                        AppLanguage.HINDI -> "चरण 4: ग्राहक संपर्क व डिलीवरी जानकारी"
                        AppLanguage.ENGLISH -> "Step 4: Buyer Contact & Delivery Terms"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "যোগাযোগের ফোন নম্বর*"
                            AppLanguage.HINDI -> "संपर्क फ़ोन नंबर*"
                            AppLanguage.ENGLISH -> "Contact Phone Number*"
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth().testTag("nursery_phone_input"),
                singleLine = true
            )

            OutlinedTextField(
                value = whatsapp,
                onValueChange = onWhatsappChange,
                label = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "হোয়াটসঅ্যাপ নম্বর (অর্ডার চ্যাট)"
                            AppLanguage.HINDI -> "व्हाट्सएप बिज़नेस नंबर"
                            AppLanguage.ENGLISH -> "WhatsApp Business Number"
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "ব্যবসায়িক ইমেল (ঐচ্ছিক)"
                            AppLanguage.HINDI -> "व्यवसाय ईमेल (वैकल्पिक)"
                            AppLanguage.ENGLISH -> "Business Email (Optional)"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = hours,
                onValueChange = onHoursChange,
                label = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "নার্সারি খোলার সময়সূচী"
                            AppLanguage.HINDI -> "नर्सरी खुलने का समय"
                            AppLanguage.ENGLISH -> "Opening Hours"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "হোম ডেলিভারি সুবিধা আছে?"
                            AppLanguage.HINDI -> "क्या होम डिलीवरी उपलब्ध है?"
                            AppLanguage.ENGLISH -> "Delivery Available?"
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "কাছাকাছি এলাকায় চারা পৌঁছানোর ব্যবস্থা"
                            AppLanguage.HINDI -> "आसपास के खेतों तक पौधे पहुँचाने की सुविधा"
                            AppLanguage.ENGLISH -> "Truck/vehicle delivery to buyer fields"
                        },
                        fontSize = 11.sp,
                        color = AgriTextSecondary
                    )
                }
                Switch(
                    checked = delivery,
                    onCheckedChange = onDeliveryChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AgriGreenPrimary)
                )
            }

            if (delivery) {
                OutlinedTextField(
                    value = radius,
                    onValueChange = onRadiusChange,
                    label = {
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "সর্বোচ্চ ডেলিভারি দূরত্ব (কিমি)"
                                AppLanguage.HINDI -> "अधिकतम डिलीवरी दूरी (किमी)"
                                AppLanguage.ENGLISH -> "Delivery Radius (KM)"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
private fun StepEDocuments(
    tradeLicense: String, onTradeLicenseChange: (String) -> Unit,
    docType: String, onDocTypeChange: (String) -> Unit,
    notes: String, onNotesChange: (String) -> Unit,
    gst: String, onGstChange: (String) -> Unit,
    currentLanguage: AppLanguage
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "ধাপ ৫: ব্যবসা ও পরিচিতি যাচাই (ঐচ্ছিক)"
                        AppLanguage.HINDI -> "चरण 5: सत्यापन दस्तावेज़ (वैकल्पिक)"
                        AppLanguage.ENGLISH -> "Step 5: Verification Documents (Optional)"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "সরকারি যাচাইকৃত ব্যাজ পাওয়ার জন্য ট্রেড লাইসেন্স বা রেজিস্ট্রেশন তথ্য দিতে পারেন। কোনো অপ্রয়োজনীয় নথি বাধ্যতামূলক নয়।"
                            AppLanguage.HINDI -> "सत्यापित विक्रेता बैज जल्दी पाने के लिए आप ट्रेड लाइसेंस या पंजीकरण विवरण दे सकते हैं। दस्तावेज़ पूरी तरह वैकल्पिक हैं।"
                            AppLanguage.ENGLISH -> "Trade license helps obtain the Verified Seller badge quickly. Documents are purely optional and never exposed to the public."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        lineHeight = 16.sp
                    )
                }
            }

            OutlinedTextField(
                value = tradeLicense,
                onValueChange = onTradeLicenseChange,
                label = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "ট্রেড লাইসেন্স নম্বর (যদি থাকে)"
                            AppLanguage.HINDI -> "ट्रेड लाइसेंस / पंजीकरण संख्या"
                            AppLanguage.ENGLISH -> "Trade License / Reg Number"
                        }
                    )
                },
                placeholder = { Text("e.g. TL/2022/4481") },
                modifier = Modifier.fillMaxWidth().testTag("nursery_trade_license_input"),
                singleLine = true
            )

            OutlinedTextField(
                value = gst,
                onValueChange = onGstChange,
                label = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "জিএসটি নম্বর (যদি থাকে)"
                            AppLanguage.HINDI -> "जीएसटी नंबर (वैकल्पिक)"
                            AppLanguage.ENGLISH -> "GST Number (Optional)"
                        }
                    )
                },
                placeholder = { Text("e.g. 19AAAAA0000A1Z5") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "অতিরিক্ত যাচাইকরণ নোট বা স্বীকৃতি"
                            AppLanguage.HINDI -> "अतिरिक्त सत्यापन नोट्स या प्रमाणपत्र"
                            AppLanguage.ENGLISH -> "Verification Notes & Certifications"
                        }
                    )
                },
                placeholder = {
                    Text(
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "হর্টিকালচার মিশন বা কৃষি বিশ্ববিদ্যালয়ের কোনো প্রশংসাপত্র থাকলে উল্লেখ করুন..."
                            AppLanguage.HINDI -> "यदि बागवानी मिशन या कृषि विश्वविद्यालय का कोई प्रमाणपत्र हो तो लिखें..."
                            AppLanguage.ENGLISH -> "Mention any horticulture mission or university recognition..."
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }
}

@Composable
private fun ApplicationStatusCard(
    application: NurseryApplication,
    currentLanguage: AppLanguage,
    onEditApplication: () -> Unit,
    onGoToDashboard: () -> Unit
) {
    val status = NurseryApplicationStatus.fromCode(application.status)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentLanguage == AppLanguage.BENGALI) application.nurseryNameBn.ifBlank { application.nurseryNameEn }
                            else application.nurseryNameEn.ifBlank { application.nurseryNameBn },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${application.cityOrTown}, ${application.district}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(status.colorHex).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(status.colorHex))
                    ) {
                        Text(
                            text = status.getLabel(currentLanguage),
                            color = Color(status.colorHex),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // If REJECTED: Show clear alert box with admin reason & edit button
                if (status == NurseryApplicationStatus.REJECTED) {
                    Surface(
                        color = AgriAlertRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AgriAlertRed)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = AgriAlertRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "আবেদন সংশোধনের কারণ (Admin Feedback):"
                                        AppLanguage.HINDI -> "आवेदन सुधार का कारण (एडमिन टिप्पणी):"
                                        AppLanguage.ENGLISH -> "Application Feedback from Admin:"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AgriAlertRed
                                )
                            }
                            Text(
                                text = application.adminRejectionReason.ifBlank {
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "দয়া করে সঠিক ফোন নম্বর ও ঠিকানার তথ্য দিয়ে পুনরায় আবেদন করুন।"
                                        AppLanguage.HINDI -> "कृपया सही फ़ोन नंबर और पूरा पता अपडेट करके फिर से आवेदन करें।"
                                        AppLanguage.ENGLISH -> "Please update contact details and complete nursery address before resubmitting."
                                    }
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Button(
                        onClick = onEditApplication,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("nursery_edit_resubmit_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "তথ্য সংশোধন ও পুনরায় জমা দিন"
                                AppLanguage.HINDI -> "जानकारी सुधारें और फिर से जमा करें"
                                AppLanguage.ENGLISH -> "Edit & Resubmit Application"
                            }
                        )
                    }
                } else if (status == NurseryApplicationStatus.APPROVED) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অভিনন্দন! আপনার নার্সারি অনুমোদিত হয়েছে। আপনি এখন সরাসরি ক্রেতাদের কাছে চারা ও বীজ বিক্রি করতে পারেন।"
                                    AppLanguage.HINDI -> "बधाई हो! आपकी नर्सरी स्वीकृत हो गई है। अब आप सीधे खरीदारों को पौधे व बीज बेच सकते हैं।"
                                    AppLanguage.ENGLISH -> "Congratulations! Your nursery business is approved. You can now list plants, manage inventory and receive orders."
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Button(
                        onClick = onGoToDashboard,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("nursery_open_seller_dashboard_btn")
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "সেলার ড্যাশবোর্ড খুলুন"
                                AppLanguage.HINDI -> "विक्रेता डैशबोर्ड खोलें"
                                AppLanguage.ENGLISH -> "Open Seller Dashboard"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // PENDING or UNDER_REVIEW
                    Surface(
                        color = Color(status.colorHex).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(status.colorHex))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "আপনার আবেদনটি বর্তমানে পর্যালোচনার টেবিলে রয়েছে। সাধারণ ২৪-৪৮ ঘণ্টার মধ্যে অ্যাডমিন যাচাই করে সিদ্ধান্ত জানাবেন।"
                                    AppLanguage.HINDI -> "आपका आवेदन वर्तमान में समीक्षाधीन है। एडमिन 24-48 घंटों के भीतर विवरण सत्यापित कर निर्णय देंगे।"
                                    AppLanguage.ENGLISH -> "Your nursery application is currently under review. Admin will verify details and activate your seller privileges shortly."
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onEditApplication,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            when (currentLanguage) {
                                AppLanguage.BENGALI -> "আবেদনের তথ্য পরিবর্তন করুন"
                                AppLanguage.HINDI -> "आवेदन विवरण अपडेट करें"
                                AppLanguage.ENGLISH -> "Update Application Details"
                            }
                        )
                    }
                }
            }
        }
    }
}
