package com.example.ui.machinery

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TimeToLeave
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.MachinerySeedCatalog
import com.example.data.model.AppLanguage
import com.example.data.model.FarmServiceType
import com.example.data.model.MachineryInquiryRecord
import com.example.data.model.MachineryProduct
import com.example.data.model.MachineryRecommendationQuery
import com.example.data.model.MachineryRentalRequest
import com.example.data.model.MachineryServiceRequest
import com.example.data.model.RentalUnit
import com.example.ui.theme.MachineryCardBadgeBg
import com.example.ui.theme.MachineryCardBadgeText
import com.example.ui.theme.MachineryCardBodyInk
import com.example.ui.theme.MachineryCardBorder
import com.example.ui.theme.MachineryCardTitleInk
import com.example.ui.theme.MachineryCardWarnBg
import com.example.ui.theme.MachineryCardWarnText
import com.example.ui.theme.MachineryCardWhite
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MachineryDetailDialog(
    machine: MachineryProduct,
    currentLanguage: AppLanguage,
    userId: String,
    farmerName: String,
    onDismiss: () -> Unit,
    onSubmitInquiry: (MachineryInquiryRecord) -> Unit,
    onSubmitRental: (MachineryRentalRequest) -> Unit,
    onHireService: (FarmServiceType) -> Unit = {}
) {
    val context = LocalContext.current
    var showQuotationForm by remember { mutableStateOf(false) }
    var showRentalForm by remember { mutableStateOf(false) }
    var isIssueReport by remember { mutableStateOf(false) }
    var showFullScreenPhoto by remember { mutableStateOf(false) }
    var zoomInitialIndex by remember { mutableIntStateOf(0) }

    val photos = remember(machine.photosCsv, machine.firebaseStoragePathsCsv) { machine.getPhotoTokens() }
    val hireService = remember(machine.hireServiceTypeCode, machine.categoryCode, machine.subcategory) {
        machine.getHireServiceType()
    }
    val cat = remember(machine.categoryCode) { machine.getCategoryEnum() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        titleContentColor = RoyalTextPrimary,
        textContentColor = RoyalTextPrimary,
        title = {
            Column(modifier = Modifier.testTag("machinery_detail_dialog")) {
                Text(machine.getDisplayName(currentLanguage), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = "${cat.emoji} ${cat.getDisplayName(currentLanguage)} • ${machine.getDisplayBrandModel(currentLanguage)}",
                    fontSize = 11.sp,
                    color = RoyalSoftGold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MachineryEquipmentImage(
                    photoTokens = photos,
                    fallbackEmoji = cat.emoji,
                    contentDescription = machine.getDisplayName(currentLanguage),
                    enableSwipePager = true,
                    showZoomHint = true,
                    onImageClick = { idx ->
                        if (photos.isNotEmpty()) {
                            zoomInitialIndex = idx
                            showFullScreenPhoto = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .testTag("machinery_detail_photo")
                )

                Surface(
                    color = RoyalSuccessBg.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${machine.subcategory} • ${machine.verificationBadgeText} (${machine.condition})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalSuccessText,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                if (machine.pendingAdminReview) {
                    Surface(
                        color = RoyalWarningBg,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, RoyalWarningText.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = RoyalWarningText, modifier = Modifier.size(16.dp))
                            Text(
                                text = machine.adminReviewNote.ifBlank {
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "ব্র্যান্ড/মডেল ও বিস্তারিত স্পেক অ্যাডমিন পর্যালোচনার অধীনে রয়েছে।"
                                        AppLanguage.HINDI -> "ब्रांड/मॉडल और विवरण एडमिन समीक्षा के अधीन हैं।"
                                        AppLanguage.ENGLISH -> "Brand, model, or exact variant flagged for Admin Review."
                                    }
                                },
                                fontSize = 11.sp,
                                color = RoyalWarningText
                            )
                        }
                    }
                }

                val purpose = machine.getDisplayPurpose(currentLanguage)
                if (purpose.isNotBlank()) {
                    SpecLine(
                        label = when (currentLanguage) {
                            AppLanguage.BENGALI -> "ব্যবহার ও উদ্দেশ্য"
                            AppLanguage.HINDI -> "उद्देश्य व उपयोग"
                            AppLanguage.ENGLISH -> "Purpose & Usage"
                        },
                        value = purpose
                    )
                }

                SpecLine(
                    label = when (currentLanguage) {
                        AppLanguage.BENGALI -> "ব্র্যান্ড ও মডেল"
                        AppLanguage.HINDI -> "ब्रांड व मॉडल"
                        AppLanguage.ENGLISH -> "Brand & Model"
                    },
                    value = machine.getDisplayBrandModel(currentLanguage)
                )
                SpecLine(
                    label = when (currentLanguage) {
                        AppLanguage.BENGALI -> "উপযুক্ত ফসল"
                        AppLanguage.HINDI -> "उपयुक्त फसलें"
                        AppLanguage.ENGLISH -> "Suitable Crops"
                    },
                    value = machine.getDisplaySuitableCrops(currentLanguage)
                )

                val instructions = machine.getDisplayOperatingInstructions(currentLanguage)
                if (instructions.isNotBlank()) {
                    SpecLine(
                        label = when (currentLanguage) {
                            AppLanguage.BENGALI -> "চালানোর নিয়ম"
                            AppLanguage.HINDI -> "संचालन निर्देश"
                            AppLanguage.ENGLISH -> "Operating Steps"
                        },
                        value = instructions
                    )
                }

                SpecLine("Dealer / Info", "${machine.sellerBusinessName} (${machine.sellerLocation})")
                SpecLine("HP / Capacity", machine.horsepowerOrCapacity)
                SpecLine("Engine / Motor", machine.engineOrMotorSpec)
                SpecLine("Fuel / Power", machine.fuelOrPowerType)
                SpecLine("Coverage / Width", machine.workingWidthOrCoverage)
                SpecLine("Weight & Size", "${machine.weightKg} • ${machine.dimensionsText}")
                SpecLine("Soil / Terrain", machine.suitableSoilOrTerrain)
                SpecLine("Land Suitability", machine.landSizeSuitability)
                SpecLine(
                    "Sale Price",
                    machine.salePriceInr?.let { "₹${it.toInt()}" } ?: "Price on request — confirm directly with verified dealer"
                )
                if (machine.forRent) {
                    val rentRates = listOfNotNull(
                        machine.rentalRateHourlyInr?.let { "₹${it.toInt()}/hr" },
                        machine.rentalRateDailyInr?.let { "₹${it.toInt()}/day" },
                        machine.rentalRateWeeklyInr?.let { "₹${it.toInt()}/wk" },
                        machine.rentalRatePerAcreInr?.let { "₹${it.toInt()}/acre" }
                    ).joinToString(" • ").ifBlank { "Custom rate confirmed directly with rental owner" }
                    SpecLine("Rental Terms", rentRates)
                }
                SpecLine("Warranty", machine.warrantyInfo)
                if (machine.sparePartsInfo.isNotBlank()) SpecLine("Spare Parts", machine.sparePartsInfo)

                val safety = machine.getDisplaySafetyGuidelines(currentLanguage)
                if (machine.requiresDgcaCompliance || machine.requiresEarthmovingSafety || safety.isNotBlank()) {
                    Surface(
                        color = RoyalWarningBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, RoyalWarningText.copy(alpha = 0.55f))
                    ) {
                        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = RoyalWarningText, modifier = Modifier.size(18.dp))
                            Text(
                                text = safety.ifBlank {
                                    when (currentLanguage) {
                                        AppLanguage.BENGALI -> "যন্ত্র চালানোর আগে সুরক্ষা সরঞ্জাম (PPE) ও প্রস্তুতকারকের নিরাপত্তা বিধি মেনে চলুন।"
                                        AppLanguage.HINDI -> "मशीन चलाने से पहले सुरक्षा उपकरण (PPE) और निर्माता के नियमों का पालन करें।"
                                        AppLanguage.ENGLISH -> "Follow manufacturer safety PPE & statutory rules before operating agricultural equipment."
                                    }
                                },
                                fontSize = 11.sp,
                                color = RoyalWarningText
                            )
                        }
                    }
                }

                Text(
                    text = "Direct Business Transparency: KRISHAK SATHI connects farmers directly with verified machinery partners. Always verify live stock, warranty, invoices & subsidy paperwork before payment.",
                    fontSize = 10.sp,
                    color = RoyalTextSecondary
                )

                if (machine.demoVideoUrl.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(machine.demoVideoUrl)))
                            }.onFailure {
                                Toast.makeText(context, "Unable to open video link", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Watch Demo Video", fontSize = 12.sp)
                    }
                }

                if (machine.hasConfirmedSellerContact()) {
                    Button(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${machine.sellerPhone}")))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Verified Seller: ${machine.sellerPhone}", fontSize = 12.sp)
                    }
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            isIssueReport = false
                            showQuotationForm = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = RoyalGreenSecondaryBg),
                        modifier = Modifier.heightIn(min = 48.dp).testTag("machinery_detail_buy_btn")
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ক্রয় ও কোটেশন"
                                AppLanguage.HINDI -> "खरीद व कोटेशन"
                                AppLanguage.ENGLISH -> "Buy / Quotation"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (machine.forRent) {
                        OutlinedButton(
                            onClick = { showRentalForm = true },
                            border = BorderStroke(1.dp, RoyalGold),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("machinery_detail_rent_btn")
                        ) {
                            Icon(Icons.Default.TimeToLeave, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalSoftGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ভাড়া নিন (Rent)"
                                    AppLanguage.HINDI -> "किराए पर लें (Rent)"
                                    AppLanguage.ENGLISH -> "Rent Equipment"
                                },
                                fontSize = 12.sp,
                                color = RoyalSoftGold
                            )
                        }
                    }

                    if (hireService != null) {
                        OutlinedButton(
                            onClick = { onHireService(hireService) },
                            border = BorderStroke(1.dp, RoyalGreenEmerald),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("machinery_detail_hire_service_btn")
                        ) {
                            Icon(Icons.Default.Engineering, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalSuccessText)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "অপারেটর/কাজ ভাড়া (Hire Service)"
                                    AppLanguage.HINDI -> "सेवा बुक करें (Hire Service)"
                                    AppLanguage.ENGLISH -> "Hire Field Service"
                                },
                                fontSize = 11.sp,
                                color = RoyalSuccessText
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            isIssueReport = true
                            showQuotationForm = true
                        },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalWarningText)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Report Incorrect Listing", fontSize = 11.sp, color = RoyalWarningText)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = RoyalSoftGold) }
        }
    )

    if (showFullScreenPhoto && photos.isNotEmpty()) {
        MachineryFullScreenImageViewer(
            photoTokens = photos,
            title = machine.getDisplayName(currentLanguage),
            subtitle = machine.getDisplayBrandModel(currentLanguage),
            initialIndex = zoomInitialIndex,
            onDismiss = { showFullScreenPhoto = false }
        )
    }

    if (showQuotationForm) {
        MachineryQuotationFormDialog(
            machine = machine,
            currentLanguage = currentLanguage,
            userId = userId,
            farmerName = farmerName,
            isIssueReport = isIssueReport,
            onDismiss = { showQuotationForm = false },
            onSubmit = {
                onSubmitInquiry(it)
                showQuotationForm = false
            }
        )
    }

    if (showRentalForm) {
        MachineryRentalFormDialog(
            machine = machine,
            currentLanguage = currentLanguage,
            userId = userId,
            farmerName = farmerName,
            onDismiss = { showRentalForm = false },
            onSubmit = {
                onSubmitRental(it)
                showRentalForm = false
            }
        )
    }
}

@Composable
fun MachineryQuotationFormDialog(
    machine: MachineryProduct,
    currentLanguage: AppLanguage,
    userId: String,
    farmerName: String,
    isIssueReport: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (MachineryInquiryRecord) -> Unit
) {
    var name by remember(farmerName) { mutableStateOf(farmerName) }
    var phone by remember { mutableStateOf("") }
    var message by remember(machine.id, isIssueReport) {
        mutableStateOf(
            if (isIssueReport) {
                "Incorrect spec/dealer note observed for ${machine.nameEn}:"
            } else {
                "Interested in quotation & subsidy/availability in North Bengal for ${machine.nameEn}."
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        title = {
            Text(
                text = if (isIssueReport) "Report Incorrect Listing" else "Request Quotation",
                color = RoyalTextPrimary,
                modifier = Modifier.testTag("machinery_quotation_dialog")
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Your Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Contact Phone *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Details") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        MachineryInquiryRecord(
                            userId = userId,
                            farmerName = name.ifBlank { farmerName },
                            farmerPhone = phone.trim(),
                            machineId = machine.id,
                            machineName = machine.getDisplayName(currentLanguage),
                            sellerUserId = machine.sellerUserId,
                            inquiryType = if (isIssueReport) "REPORT_ISSUE" else "QUOTATION",
                            message = message.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                modifier = Modifier.heightIn(min = 48.dp).testTag("machinery_quotation_submit_btn")
            ) { Text("Submit") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = RoyalSoftGold) }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MachineryRentalFormDialog(
    machine: MachineryProduct,
    currentLanguage: AppLanguage,
    userId: String,
    farmerName: String,
    onDismiss: () -> Unit,
    onSubmit: (MachineryRentalRequest) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var locality by remember { mutableStateOf(MachinerySeedCatalog.initialLocalities.first().enName) }
    var unit by remember { mutableStateOf(RentalUnit.DAILY) }
    var duration by remember { mutableStateOf("1") }
    var withOperator by remember { mutableStateOf(true) }
    var deliveryReq by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        title = {
            Text(
                text = "Request Machinery Rental",
                color = RoyalTextPrimary,
                modifier = Modifier.testTag("machinery_rental_dialog")
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = locality, onValueChange = { locality = it }, label = { Text("Village / Locality") }, modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RentalUnit.values().forEach { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u.getDisplayName(currentLanguage), fontSize = 10.sp) }
                        )
                    }
                }
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration count") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = withOperator, onCheckedChange = { withOperator = it })
                    Text("With Operator", fontSize = 12.sp, color = RoyalTextPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(checked = deliveryReq, onCheckedChange = { deliveryReq = it })
                    Text("Need Delivery", fontSize = 12.sp, color = RoyalTextPrimary)
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Field crop / date notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        MachineryRentalRequest(
                            userId = userId,
                            farmerName = farmerName,
                            farmerPhone = phone.trim(),
                            machineId = machine.id,
                            machineName = machine.getDisplayName(currentLanguage),
                            sellerUserId = machine.sellerUserId,
                            locality = locality.trim(),
                            rentalUnit = unit.code,
                            durationUnits = duration.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                            withOperator = withOperator,
                            deliveryRequired = deliveryReq,
                            notes = notes.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                modifier = Modifier.heightIn(min = 48.dp).testTag("machinery_rental_submit_btn")
            ) { Text("Send Request") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = RoyalSoftGold) }
        }
    )
}

@Composable
fun MachineryCompareDialog(
    machines: List<MachineryProduct>,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val distinctCategories = remember(machines) { machines.map { it.categoryCode }.distinct() }
    val hasCategoryMismatch = distinctCategories.size > 1

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        title = {
            Text(
                text = "Side-by-Side Comparison (${machines.size})",
                color = RoyalSoftGold,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("machinery_compare_dialog")
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hasCategoryMismatch) {
                    Surface(
                        color = MachineryCardWarnBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MachineryCardWarnText.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth().testTag("compare_category_mismatch_warning")
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MachineryCardWarnText, modifier = Modifier.size(18.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ভিন্ন বিভাগের যন্ত্র তুলনা সতর্কতা: আপনি ${distinctCategories.size}টি আলাদা বিভাগের যন্ত্র তুলনা করছেন। সঠিক সিদ্ধান্তের জন্য একই শ্রেণির যন্ত্র তুলনা করুন।"
                                    AppLanguage.HINDI -> "अलग श्रेणी तुलना चेतावनी: आप ${distinctCategories.size} अलग श्रेणियों की मशीनों की तुलना कर रहे हैं। सटीक तुलना के लिए एक ही वर्ग के यंत्र चुनें।"
                                    AppLanguage.ENGLISH -> "Category Mismatch Warning: You are comparing ${distinctCategories.size} different machinery categories. Compare within the same category for apples-to-apples HP & cost checks."
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MachineryCardWarnText
                            )
                        }
                    }
                }

                machines.forEach { m ->
                    val cat = m.getCategoryEnum()
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MachineryCardWhite),
                        border = BorderStroke(1.dp, MachineryCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(m.getDisplayName(currentLanguage), fontWeight = FontWeight.Bold, color = MachineryCardTitleInk, fontSize = 13.sp)
                            Surface(color = MachineryCardBadgeBg, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "${cat.emoji} ${cat.getDisplayName(currentLanguage)}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MachineryCardBadgeText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text("${m.getDisplayBrandModel(currentLanguage)} • ${m.horsepowerOrCapacity}", fontSize = 11.sp, color = MachineryCardBodyInk)
                            Text("Fuel: ${m.fuelOrPowerType} • Terrain: ${m.suitableSoilOrTerrain}", fontSize = 11.sp, color = MachineryCardBodyInk)
                            Text("Coverage: ${m.workingWidthOrCoverage} • Land: ${m.landSizeSuitability}", fontSize = 11.sp, color = MachineryCardBodyInk)
                            if (m.compatibleWithIdsCsv.isNotBlank()) {
                                Text("Pairs with: ${m.compatibleWithIdsCsv}", fontSize = 10.sp, color = MachineryCardBadgeText)
                            }
                            Text(
                                text = "Price: ${m.salePriceInr?.let { "₹${it.toInt()}" } ?: "Price on request"} • Rent: ${if (m.forRent) "Available" else "Sale only"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MachineryCardBadgeText
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = RoyalSoftGold) }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceBookingDialog(
    serviceType: FarmServiceType,
    currentLanguage: AppLanguage,
    userId: String,
    farmerName: String,
    onDismiss: () -> Unit,
    onSubmit: (MachineryServiceRequest) -> Unit
) {
    var name by remember { mutableStateOf(farmerName) }
    var phone by remember { mutableStateOf("") }
    var locality by remember { mutableStateOf("Siliguri") }
    var landSize by remember { mutableStateOf("2 Bigha / 0.66 Acre") }
    var crop by remember { mutableStateOf("Paddy") }
    var prefDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        title = {
            Text("${serviceType.emoji} ${serviceType.getDisplayName(currentLanguage)}", color = RoyalTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Status starts as REQUESTED_AWAITING_CONFIRMATION until confirmed directly by a verified service provider.",
                    fontSize = 11.sp,
                    color = RoyalWarningText
                )
                if (serviceType == FarmServiceType.DRONE_SPRAYING) {
                    Text(
                        text = "DGCA Rule Notice: Drone spraying near Bagdogra Airport or restricted airspace requires valid UIN, RPC & DigitalSky clearance.",
                        fontSize = 10.sp,
                        color = RoyalWarningText
                    )
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Farmer Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = locality, onValueChange = { locality = it }, label = { Text("Locality / Village") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = landSize, onValueChange = { landSize = it }, label = { Text("Land Size (Bigha/Acre)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = crop, onValueChange = { crop = it }, label = { Text("Crop Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = prefDate, onValueChange = { prefDate = it }, label = { Text("Preferred Date") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Additional Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        MachineryServiceRequest(
                            userId = userId,
                            farmerName = name.ifBlank { farmerName },
                            farmerPhone = phone.trim(),
                            serviceTypeCode = serviceType.code,
                            locality = locality.trim(),
                            landSizeText = landSize.trim(),
                            cropName = crop.trim(),
                            preferredDate = prefDate.trim(),
                            notes = notes.trim()
                        )
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
            ) { Text("Submit Request") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = RoyalSoftGold) }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartAiMachineFinderDialog(
    currentLanguage: AppLanguage,
    results: Pair<List<MachineryProduct>, String>?,
    onRunRecommendation: (MachineryRecommendationQuery) -> Unit,
    onOpenFullAi: () -> Unit,
    onDismiss: () -> Unit
) {
    var crop by remember { mutableStateOf("Paddy") }
    var acres by remember { mutableStateOf("1.5") }
    var terrain by remember { mutableStateOf("Alluvial Terai Plains") }
    var operation by remember { mutableStateOf("Ploughing & Spraying") }
    var powerPref by remember { mutableStateOf("Diesel") }
    var skilledOp by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        title = { Text("Smart AI Machine Finder", color = RoyalSoftGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = crop, onValueChange = { crop = it }, label = { Text("Crop") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = acres, onValueChange = { acres = it }, label = { Text("Land Size (Acres)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = terrain, onValueChange = { terrain = it }, label = { Text("Soil / Terrain (Plains / Hill / Orchard)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = operation, onValueChange = { operation = it }, label = { Text("Operation (Ploughing, Irrigation, Spray, Harvest, Drone, Digging)") }, modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Diesel", "Electric", "Battery", "Solar", "Petrol").forEach { p ->
                        FilterChip(selected = powerPref == p, onClick = { powerPref = p }, label = { Text(p, fontSize = 10.sp) })
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = skilledOp, onCheckedChange = { skilledOp = it })
                    Text("Skilled operator available on farm", fontSize = 12.sp, color = RoyalTextPrimary)
                }

                Button(
                    onClick = {
                        onRunRecommendation(
                            MachineryRecommendationQuery(
                                cropName = crop.trim(),
                                landSizeAcres = acres.toDoubleOrNull() ?: 1.5,
                                soilOrTerrain = terrain.trim(),
                                irrigationAvailable = true,
                                budgetRangeInr = "Standard",
                                operationNeed = operation.trim(),
                                skilledOperatorAvailable = skilledOp,
                                powerPreference = powerPref
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = RoyalGreenSecondaryBg),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) { Text("Find Catalog Matches", fontWeight = FontWeight.Bold) }

                results?.let { (items, note) ->
                    Surface(color = RoyalGreenSecondaryBg, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(note, fontSize = 11.sp, color = RoyalSoftGold)
                            items.forEach { m ->
                                Text("• ${m.getDisplayName(currentLanguage)} (${m.horsepowerOrCapacity})", fontSize = 11.sp, color = RoyalTextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onOpenFullAi,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) { Text("Ask Full Krishak Gemini AI", fontSize = 12.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = RoyalSoftGold) }
        }
    )
}

@Composable
private fun SpecLine(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("$label:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalSoftGold, modifier = Modifier.width(108.dp))
        Text(value, fontSize = 11.sp, color = RoyalTextPrimary, modifier = Modifier.weight(1f))
    }
}
