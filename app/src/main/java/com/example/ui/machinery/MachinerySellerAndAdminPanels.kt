package com.example.ui.machinery

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.MachineryCategory
import com.example.data.model.MachineryCondition
import com.example.data.model.MachineryDealerEntry
import com.example.data.model.MachineryInquiryRecord
import com.example.data.model.MachineryProduct
import com.example.data.model.MachineryResearchQueueItem
import com.example.data.model.NurseryVerificationStatus
import com.example.data.repository.MachineryRepository
import com.example.data.repository.TrustAndSafetyRepository
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MachinerySellerPanel(
    userId: String,
    businessName: String,
    locality: String,
    phone: String?,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { MachineryRepository(context) }
    val trustRepo = remember { TrustAndSafetyRepository(context) }
    val suspendedSellerIds by trustRepo.observeSuspendedSellerIds().collectAsState(initial = emptySet())
    val isSellerSuspended = userId in suspendedSellerIds

    LaunchedEffect(Unit) { repo.ensureSeeded() }

    val allProducts by repo.observeAllProducts().collectAsState(initial = emptyList())
    val allInquiries by repo.observeAllInquiries().collectAsState(initial = emptyList())
    val allRentals by repo.observeAllRentals().collectAsState(initial = emptyList())
    val allServices by repo.observeAllServices().collectAsState(initial = emptyList())

    val myProducts = remember(allProducts, userId) {
        allProducts.filter { it.sellerUserId == userId }
    }
    val relevantInquiries = remember(allInquiries, userId) {
        allInquiries.filter { it.sellerUserId == userId || it.sellerUserId.isBlank() }
    }
    val relevantRentals = remember(allRentals, userId) {
        allRentals.filter { it.sellerUserId == userId || it.sellerUserId.isBlank() }
    }
    val relevantServices = remember(allServices, userId) {
        allServices.filter { it.providerUserId == userId || it.providerUserId.isBlank() }
    }

    var showListingEditor by remember { mutableStateOf<MachineryProduct?>(null) }
    var showDealerOfferEditor by remember { mutableStateOf(false) }
    var replyingInquiry by remember { mutableStateOf<MachineryInquiryRecord?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(RoyalGreenPrimaryBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Machinery, Rentals & Field Services",
                            fontWeight = FontWeight.Bold,
                            color = RoyalSoftGold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Partner submissions require Admin approval before public display.",
                            fontSize = 11.sp,
                            color = RoyalTextSecondary
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isSellerSuspended) {
                                    Toast.makeText(context, "Seller account is suspended by Admin Trust Team", Toast.LENGTH_LONG).show()
                                } else {
                                    showListingEditor = MachineryProduct(
                                        sellerUserId = userId,
                                        sellerBusinessName = businessName,
                                        sellerLocation = locality,
                                        sellerPhone = phone
                                    )
                                }
                            },
                            enabled = !isSellerSuspended,
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("seller_add_machine_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Machine", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                if (isSellerSuspended) {
                                    Toast.makeText(context, "Seller account is suspended by Admin Trust Team", Toast.LENGTH_LONG).show()
                                } else {
                                    showDealerOfferEditor = true
                                }
                            },
                            enabled = !isSellerSuspended,
                            border = BorderStroke(1.dp, RoyalGold),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("seller_post_offer_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalSoftGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post Dealer Offer", fontSize = 12.sp, color = RoyalSoftGold)
                        }
                    }
                    if (isSellerSuspended) {
                        Text(
                            text = "⛔ Seller account suspended by Admin Trust & Moderation.",
                            color = RoyalAlertText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Text("My Machinery Listings (${myProducts.size})", fontWeight = FontWeight.Bold, color = RoyalSoftGold, fontSize = 14.sp)
        }
        if (myProducts.isEmpty()) {
            item {
                Text("No seller machinery listings yet. Tap 'Add Machine' to submit one for admin verification.", fontSize = 12.sp, color = RoyalTextSecondary)
            }
        } else {
            items(myProducts, key = { it.id }) { m ->
                Card(colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg), border = BorderStroke(1.dp, RoyalBorder)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        MachineryEquipmentImage(
                            photoTokens = m.getPhotoTokens(),
                            fallbackEmoji = m.getCategoryEnum().emoji,
                            contentDescription = m.getDisplayName(currentLanguage),
                            enableSwipePager = false,
                            showZoomHint = false,
                            modifier = Modifier.size(width = 76.dp, height = 64.dp)
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(m.getDisplayName(currentLanguage), fontWeight = FontWeight.Bold, color = RoyalTextPrimary, modifier = Modifier.weight(1f))
                                Surface(
                                    color = if (m.approvalStatus == "APPROVED") RoyalSuccessBg else RoyalWarningBg,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        m.approvalStatus,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (m.approvalStatus == "APPROVED") RoyalSuccessText else RoyalWarningText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("${m.getDisplayBrandModel(currentLanguage)} • ${m.condition} • Sale: ${m.forSale} • Rent: ${m.forRent}", fontSize = 11.sp, color = RoyalTextSecondary)
                            OutlinedButton(onClick = { showListingEditor = m }, modifier = Modifier.heightIn(min = 48.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Listing", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider(color = RoyalBorder)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Customer Inquiries (${relevantInquiries.size})", fontWeight = FontWeight.Bold, color = RoyalSoftGold, fontSize = 14.sp)
        }
        items(relevantInquiries, key = { it.id }) { inq ->
            Card(colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg), border = BorderStroke(1.dp, RoyalBorder)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${inq.farmerName} (${inq.farmerPhone}) • ${inq.machineName}", fontWeight = FontWeight.Bold, color = RoyalTextPrimary, fontSize = 12.sp)
                    Text(inq.message, fontSize = 11.sp, color = RoyalTextSecondary)
                    Text("Status: ${inq.status}", fontSize = 10.sp, color = RoyalSoftGold)
                    OutlinedButton(onClick = { replyingInquiry = inq }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Reply & Mark Responded", fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Text("Rental Requests (${relevantRentals.size})", fontWeight = FontWeight.Bold, color = RoyalSoftGold, fontSize = 14.sp)
        }
        items(relevantRentals, key = { it.id }) { rent ->
            Card(colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg), border = BorderStroke(1.dp, RoyalBorder)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${rent.farmerName} • ${rent.machineName} (${rent.durationUnits} ${rent.rentalUnit})", fontWeight = FontWeight.Bold, color = RoyalTextPrimary, fontSize = 12.sp)
                    Text("Locality: ${rent.locality} • Status: ${rent.status}", fontSize = 11.sp, color = RoyalTextSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { scope.launch { repo.updateRentalStatus(rent.id, "CONFIRMED") } },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) { Text("Confirm", fontSize = 11.sp) }
                        OutlinedButton(
                            onClick = { scope.launch { repo.updateRentalStatus(rent.id, "COMPLETED") } },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) { Text("Mark Completed", fontSize = 11.sp) }
                    }
                }
            }
        }

        item {
            Text("Service Requests (${relevantServices.size})", fontWeight = FontWeight.Bold, color = RoyalSoftGold, fontSize = 14.sp)
        }
        items(relevantServices, key = { it.id }) { srv ->
            Card(colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg), border = BorderStroke(1.dp, RoyalBorder)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${srv.farmerName} • ${srv.serviceTypeCode} (${srv.cropName})", fontWeight = FontWeight.Bold, color = RoyalTextPrimary, fontSize = 12.sp)
                    Text("${srv.locality} • ${srv.landSizeText} • Status: ${srv.status}", fontSize = 11.sp, color = RoyalTextSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { scope.launch { repo.updateServiceStatus(srv.id, "CONFIRMED") } },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) { Text("Accept Service", fontSize = 11.sp) }
                        OutlinedButton(
                            onClick = { scope.launch { repo.updateServiceStatus(srv.id, "COMPLETED") } },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) { Text("Completed", fontSize = 11.sp) }
                    }
                }
            }
        }
    }

    showListingEditor?.let { base ->
        MachineListingFormDialog(
            initial = base,
            isAdmin = false,
            currentLanguage = currentLanguage,
            onUploadPhoto = { uri, machineId, onUploaded ->
                scope.launch {
                    val res = repo.uploadEquipmentPhotoToStorage(machineId, uri)
                    if (res.isSuccess) {
                        val info = res.getOrThrow()
                        onUploaded(info.downloadUrl, info.storagePath)
                        Toast.makeText(context, "Compressed & uploaded (${info.byteSize / 1024} KB)", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Using local preview URI until sign-in/online sync", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showListingEditor = null },
            onSave = { submitted ->
                scope.launch {
                    val res = repo.submitSellerMachineListing(submitted, isAdmin = false)
                    Toast.makeText(
                        context,
                        if (res.isSuccess) "Submitted for Admin Verification (PENDING_APPROVAL)" else (res.exceptionOrNull()?.message ?: "Error"),
                        Toast.LENGTH_LONG
                    ).show()
                    showListingEditor = null
                }
            }
        )
    }

    replyingInquiry?.let { inq ->
        var replyText by remember { mutableStateOf(inq.sellerReply) }
        AlertDialog(
            onDismissRequest = { replyingInquiry = null },
            containerColor = RoyalGreenCardBg,
            title = { Text("Reply to Farmer Inquiry", color = RoyalTextPrimary) },
            text = {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Official Quote / Availability Note") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repo.updateInquiryReply(inq.id, replyText.trim(), "RESPONDED")
                            replyingInquiry = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
                ) { Text("Send Reply") }
            },
            dismissButton = {
                TextButton(onClick = { replyingInquiry = null }) { Text("Cancel", color = RoyalSoftGold) }
            }
        )
    }

    if (showDealerOfferEditor) {
        DealerOfferEditorDialog(
            defaultDealerName = businessName,
            defaultLocality = locality.ifBlank { "SILIGURI" },
            onDismiss = { showDealerOfferEditor = false },
            onSave = { offer ->
                scope.launch {
                    val res = repo.upsertDealerOffer(offer, userId)
                    Toast.makeText(
                        context,
                        if (res.isSuccess) "Dealer offer published" else (res.exceptionOrNull()?.message ?: "Error"),
                        Toast.LENGTH_SHORT
                    ).show()
                    showDealerOfferEditor = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMachineryManagementScreen(
    currentLanguage: AppLanguage,
    adminEmail: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalGreenSecondaryBg, titleContentColor = RoyalTextPrimary),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("admin_machinery_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Admin Machinery & Dealer Panel", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).background(RoyalGreenPrimaryBg),
            contentAlignment = Alignment.TopCenter
        ) {
            AdminMachineryManagementContent(
                currentLanguage = currentLanguage,
                adminEmail = adminEmail,
                modifier = Modifier.fillMaxSize().widthIn(max = 840.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminMachineryManagementContent(
    currentLanguage: AppLanguage,
    adminEmail: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { MachineryRepository(context) }

    LaunchedEffect(Unit) { repo.ensureSeeded() }

    val allProducts by repo.observeAllProducts().collectAsState(initial = emptyList())
    val allDealers by repo.observeAllDealers().collectAsState(initial = emptyList())
    val allManufacturers by repo.observeAllManufacturers().collectAsState(initial = emptyList())
    val allOffers by repo.observeAllOffers().collectAsState(initial = emptyList())
    val allReviews by repo.observeAllReviews().collectAsState(initial = emptyList())
    val researchQueue by repo.observeResearchQueue().collectAsState(initial = emptyList())
    val allInquiries by repo.observeAllInquiries().collectAsState(initial = emptyList())
    val allRentals by repo.observeAllRentals().collectAsState(initial = emptyList())
    val allServices by repo.observeAllServices().collectAsState(initial = emptyList())

    var adminTab by remember { mutableIntStateOf(0) }
    var listingFilter by remember { mutableStateOf("ALL") }
    var editingDealer by remember { mutableStateOf<MachineryDealerEntry?>(null) }
    var addingVerifiedListing by remember { mutableStateOf<MachineryProduct?>(null) }
    var reviewingEquipment by remember { mutableStateOf<MachineryProduct?>(null) }
    var showAddOemDialog by remember { mutableStateOf(false) }
    var showAddOfferDialog by remember { mutableStateOf(false) }
    var showNewResearchDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().background(RoyalGreenPrimaryBg).imePadding()) {
        ScrollableTabRow(
            selectedTabIndex = adminTab,
            containerColor = RoyalGreenSecondaryBg,
            contentColor = RoyalGold,
            edgePadding = 12.dp
        ) {
            listOf(
                "Listings (${allProducts.size})",
                "Dealers (${allDealers.size})",
                "Offers & OEMs (${allOffers.size})",
                "Research Queue (${researchQueue.size})",
                "Duplicates & CSV",
                "Reviews & Reports",
                "Analytics"
            ).forEachIndexed { idx, title ->
                Tab(
                    selected = adminTab == idx,
                    onClick = { adminTab = idx },
                    modifier = Modifier.testTag("admin_machinery_tab_$idx"),
                    text = { Text(title, fontSize = 12.sp, color = if (adminTab == idx) RoyalGold else RoyalTextSecondary) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (adminTab) {
                0 -> {
                    val flaggedReviewCount = allProducts.count { it.pendingAdminReview || it.approvalStatus == "PENDING_APPROVAL" }
                    item {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "ALL" to "ALL (${allProducts.size})",
                                "REVIEW_PENDING" to "REVIEW NEEDED ($flaggedReviewCount)",
                                "PENDING_APPROVAL" to "PENDING_APPROVAL",
                                "APPROVED" to "APPROVED",
                                "REJECTED" to "REJECTED"
                            ).forEach { (st, label) ->
                                FilterChip(
                                    selected = listingFilter == st,
                                    onClick = { listingFilter = st },
                                    label = { Text(label, fontSize = 10.sp) },
                                    modifier = Modifier.testTag("admin_filter_$st")
                                )
                            }
                            Button(
                                onClick = {
                                    addingVerifiedListing = MachineryProduct(
                                        sellerUserId = "admin",
                                        sellerBusinessName = "KRISHAK SATHI Verified Catalog"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = RoyalGreenPrimaryBg),
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Verified Machine", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    val shown = allProducts.filter {
                        when (listingFilter) {
                            "ALL" -> true
                            "REVIEW_PENDING" -> it.pendingAdminReview || it.approvalStatus == "PENDING_APPROVAL"
                            else -> it.approvalStatus == listingFilter
                        }
                    }
                    items(shown, key = { it.id }) { m ->
                        Card(colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg), border = BorderStroke(1.dp, RoyalBorder)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    MachineryEquipmentImage(
                                        photoTokens = m.getPhotoTokens(),
                                        fallbackEmoji = m.getCategoryEnum().emoji,
                                        contentDescription = m.nameEn,
                                        enableSwipePager = false,
                                        showZoomHint = false,
                                        modifier = Modifier.size(width = 76.dp, height = 64.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text("${m.id}: ${m.nameEn}", fontWeight = FontWeight.Bold, color = RoyalTextPrimary, fontSize = 13.sp)
                                        Text("Origin: ${m.listingOrigin} • Status: ${m.approvalStatus}", fontSize = 11.sp, color = RoyalTextSecondary)
                                        Text("Brand/Model: ${m.getDisplayBrandModel(currentLanguage)}", fontSize = 11.sp, color = RoyalSoftGold)
                                        if (m.pendingAdminReview) {
                                            Text(
                                                text = "⚠ Flagged for review: ${m.adminReviewNote.ifBlank { "Verify brand/model/spec" }}",
                                                fontSize = 10.sp,
                                                color = RoyalWarningText
                                            )
                                        }
                                    }
                                }
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { reviewingEquipment = m },
                                        border = BorderStroke(1.dp, RoyalGold),
                                        modifier = Modifier.heightIn(min = 48.dp).testTag("admin_review_btn_${m.id}")
                                    ) {
                                        Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalSoftGold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Review & Preview", fontSize = 11.sp, color = RoyalSoftGold)
                                    }
                                    Button(
                                        onClick = { scope.launch { repo.approveOrRejectMachineListing(m.id, true, adminEmail) } },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { scope.launch { repo.approveOrRejectMachineListing(m.id, false, adminEmail, "Incomplete specs/verification") } },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalAlertText)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject", fontSize = 11.sp, color = RoyalAlertText)
                                    }
                                    OutlinedButton(
                                        onClick = { scope.launch { repo.deleteMachineListing(m.id, adminEmail) } },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalAlertText)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete", fontSize = 11.sp, color = RoyalAlertText)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    item {
                        Text(
                            text = "Verification Rule: Do NOT verify phone/owner/authorisation without explicit confirmed consent.",
                            fontSize = 11.sp,
                            color = RoyalWarningText
                        )
                    }
                    items(allDealers, key = { it.dealerId }) { d ->
                        Card(colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg), border = BorderStroke(1.dp, RoyalBorder)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${d.dealerId} — ${d.businessName}", fontWeight = FontWeight.Bold, color = RoyalTextPrimary, fontSize = 13.sp)
                                Text("Locality: ${d.localityCode} • Status: ${d.verificationStatus} • Consent: ${d.ownerConsentStatus}", fontSize = 11.sp, color = RoyalSoftGold)
                                if (!d.claimNote.isNullOrBlank()) {
                                    Text("Claim Submission: ${d.claimNote}", fontSize = 10.sp, color = RoyalWarningText)
                                }
                                OutlinedButton(
                                    onClick = { editingDealer = d },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit & Verify Dealer", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    adminOffersAndOemsTabItems(
                        allOffers = allOffers,
                        allManufacturers = allManufacturers,
                        repo = repo,
                        adminEmail = adminEmail,
                        scope = scope,
                        onOpenAddOem = { showAddOemDialog = true },
                        onOpenAddOffer = { showAddOfferDialog = true }
                    )
                }

                3 -> {
                    adminResearchQueueTabItems(
                        queue = researchQueue,
                        repo = repo,
                        adminEmail = adminEmail,
                        scope = scope,
                        onOpenNewResearchItem = { showNewResearchDialog = true }
                    )
                }

                4 -> {
                    item {
                        AdminDuplicatesAndCsvSection(
                            repo = repo,
                            adminEmail = adminEmail,
                            scope = scope
                        )
                    }
                }

                5 -> {
                    val issueReports = allInquiries.filter { it.inquiryType == "REPORT_ISSUE" }
                    item {
                        Text("Reported Listing Issues (${issueReports.size})", fontWeight = FontWeight.Bold, color = RoyalAlertText, fontSize = 14.sp)
                    }
                    items(issueReports, key = { "rep_${it.id}" }) { r ->
                        Card(colors = CardDefaults.cardColors(containerColor = RoyalAlertBg.copy(alpha = 0.25f)), border = BorderStroke(1.dp, RoyalAlertText)) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("${r.machineId}: ${r.machineName}", fontWeight = FontWeight.Bold, color = RoyalTextPrimary, fontSize = 12.sp)
                                Text(r.message, fontSize = 11.sp, color = RoyalTextSecondary)
                            }
                        }
                    }
                    item {
                        Text("All Inquiries (${allInquiries.size}) • Rentals (${allRentals.size}) • Services (${allServices.size})", fontWeight = FontWeight.Bold, color = RoyalSoftGold, fontSize = 13.sp)
                    }
                    adminReviewModerationItems(
                        allReviews = allReviews,
                        repo = repo,
                        adminEmail = adminEmail,
                        scope = scope
                    )
                }

                6 -> {
                    val verifiedDealers = allDealers.count { it.isVerified }
                    val pendingDealers = allDealers.size - verifiedDealers
                    val approvedCount = allProducts.count { it.approvalStatus == "APPROVED" }
                    val pendingCount = allProducts.count { it.approvalStatus == "PENDING_APPROVAL" }
                    val pendingResearch = researchQueue.count { it.status == "PENDING_RESEARCH" }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg), border = BorderStroke(1.dp, RoyalGold)) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Machinery Hub Analytics Summary", fontWeight = FontWeight.Bold, color = RoyalSoftGold, fontSize = 15.sp)
                                Text("Catalog Listings: $approvedCount Approved / $pendingCount Pending Admin Review", fontSize = 12.sp, color = RoyalTextPrimary)
                                Text("Dealers: $verifiedDealers Verified / $pendingDealers Pending Verification", fontSize = 12.sp, color = RoyalTextPrimary)
                                Text("OEM Portals: ${allManufacturers.size} • Dealer Offers: ${allOffers.size}", fontSize = 12.sp, color = RoyalTextPrimary)
                                Text("Research Queue: $pendingResearch Pending / ${researchQueue.size} Total • Reviews: ${allReviews.size}", fontSize = 12.sp, color = RoyalTextPrimary)
                                Text("Total Quotations/Issues: ${allInquiries.size} • Rentals: ${allRentals.size} • Farm Services: ${allServices.size}", fontSize = 12.sp, color = RoyalTextSecondary)
                            }
                        }
                    }
                    items(MachineryCategory.values().toList(), key = { it.code }) { cat ->
                        val catCount = allProducts.count { it.matchesCategory(cat.code) && it.approvalStatus == "APPROVED" }
                        Text("${cat.emoji} ${cat.getDisplayName(currentLanguage)}: $catCount approved items", fontSize = 12.sp, color = RoyalTextPrimary)
                    }
                }
            }
        }
    }

    editingDealer?.let { d ->
        var address by remember { mutableStateOf(d.fullAddress.orEmpty()) }
        var pinCode by remember { mutableStateOf(d.pinCode.orEmpty()) }
        var owner by remember { mutableStateOf(d.ownerName.orEmpty()) }
        var phone by remember { mutableStateOf(d.phone.orEmpty()) }
        var brands by remember { mutableStateOf(d.brandsCarriedCsv) }
        var markVerified by remember { mutableStateOf(d.isVerified) }
        var markConsent by remember { mutableStateOf(d.ownerConsentStatus == "CONFIRMED") }
        var markAuth by remember { mutableStateOf(d.isAuthorisedDealerVerified) }

        AlertDialog(
            onDismissRequest = { editingDealer = null },
            containerColor = RoyalGreenCardBg,
            title = { Text("Verify / Edit Dealer: ${d.businessName}", color = RoyalSoftGold, fontSize = 15.sp) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Confirmed Full Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pinCode, onValueChange = { pinCode = it }, label = { Text("PIN Code") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = owner, onValueChange = { owner = it }, label = { Text("Confirmed Owner Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Confirmed Phone") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = brands, onValueChange = { brands = it }, label = { Text("Verified Brands") }, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = markConsent, onCheckedChange = { markConsent = it })
                        Text("Owner Consent CONFIRMED", fontSize = 12.sp, color = RoyalTextPrimary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = markVerified, onCheckedChange = { markVerified = it })
                        Text("Mark Dealer VERIFIED", fontSize = 12.sp, color = RoyalTextPrimary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = markAuth, onCheckedChange = { markAuth = it })
                        Text("Verified Authorised Brand Dealer", fontSize = 12.sp, color = RoyalTextPrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repo.verifyOrEditDealerByAdmin(
                                dealer = d.copy(
                                    fullAddress = address.trim().ifBlank { null },
                                    pinCode = pinCode.trim().ifBlank { null },
                                    ownerName = owner.trim().ifBlank { null },
                                    phone = phone.trim().ifBlank { null },
                                    brandsCarriedCsv = brands.trim()
                                ),
                                markVerified = markVerified,
                                markConsentConfirmed = markConsent,
                                markAuthorisedDealer = markAuth,
                                adminEmail = adminEmail
                            )
                            Toast.makeText(context, "Dealer updated", Toast.LENGTH_SHORT).show()
                            editingDealer = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingDealer = null }) { Text("Cancel", color = RoyalSoftGold) }
            }
        )
    }

    addingVerifiedListing?.let { base ->
        MachineListingFormDialog(
            initial = base,
            isAdmin = true,
            currentLanguage = currentLanguage,
            onUploadPhoto = { uri, machineId, onUploaded ->
                scope.launch {
                    val res = repo.uploadEquipmentPhotoToStorage(machineId, uri)
                    if (res.isSuccess) {
                        val info = res.getOrThrow()
                        onUploaded(info.downloadUrl, info.storagePath)
                        Toast.makeText(context, "Uploaded to Storage (${info.byteSize / 1024} KB)", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { addingVerifiedListing = null },
            onSave = { submitted ->
                scope.launch {
                    val res = repo.submitSellerMachineListing(submitted, isAdmin = true)
                    Toast.makeText(context, if (res.isSuccess) "Verified listing published" else "Error", Toast.LENGTH_SHORT).show()
                    addingVerifiedListing = null
                }
            }
        )
    }

    reviewingEquipment?.let { target ->
        AdminEquipmentReviewDialog(
            machine = target,
            currentLanguage = currentLanguage,
            onDismiss = { reviewingEquipment = null },
            onSaveReview = { brand, model, verified, note ->
                scope.launch {
                    val res = repo.resolveEquipmentReviewByAdmin(
                        machineId = target.id,
                        verifiedBrand = brand,
                        verifiedModel = model,
                        markVerified = verified,
                        adminEmail = adminEmail,
                        reviewNote = note
                    )
                    Toast.makeText(
                        context,
                        if (res.isSuccess) "Equipment review updated" else "Failed to update review",
                        Toast.LENGTH_SHORT
                    ).show()
                    reviewingEquipment = null
                }
            }
        )
    }

    if (showAddOemDialog) {
        AdminOemFormDialog(
            onDismiss = { showAddOemDialog = false },
            onSave = { oem ->
                scope.launch {
                    val res = repo.upsertManufacturer(oem, adminEmail)
                    Toast.makeText(
                        context,
                        if (res.isSuccess) "OEM portal saved" else (res.exceptionOrNull()?.message ?: "Error"),
                        Toast.LENGTH_SHORT
                    ).show()
                    showAddOemDialog = false
                }
            }
        )
    }

    if (showAddOfferDialog) {
        DealerOfferEditorDialog(
            defaultDealerName = "Admin Verified Offer",
            onDismiss = { showAddOfferDialog = false },
            onSave = { offer ->
                scope.launch {
                    val res = repo.upsertDealerOffer(offer, adminEmail)
                    Toast.makeText(
                        context,
                        if (res.isSuccess) "Dealer offer saved" else (res.exceptionOrNull()?.message ?: "Error"),
                        Toast.LENGTH_SHORT
                    ).show()
                    showAddOfferDialog = false
                }
            }
        )
    }

    if (showNewResearchDialog) {
        var machineIdInput by remember { mutableStateOf("") }
        var equipmentNameInput by remember { mutableStateOf("") }
        var categoryCodeInput by remember { mutableStateOf(MachineryCategory.TRACTORS_LAND_PREP.code) }
        var missingNoteInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewResearchDialog = false },
            containerColor = RoyalGreenCardBg,
            title = { Text("Add Research / Verification Item", color = RoyalSoftGold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = machineIdInput, onValueChange = { machineIdInput = it }, label = { Text("Machine ID (optional)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = equipmentNameInput, onValueChange = { equipmentNameInput = it }, label = { Text("Equipment Name *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = categoryCodeInput, onValueChange = { categoryCodeInput = it }, label = { Text("Category Code") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = missingNoteInput, onValueChange = { missingNoteInput = it }, label = { Text("Missing Photo / Spec Verification Note") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    enabled = equipmentNameInput.isNotBlank(),
                    onClick = {
                        scope.launch {
                            val res = repo.submitResearchItem(
                                MachineryResearchQueueItem(
                                    machineId = machineIdInput.trim(),
                                    equipmentName = equipmentNameInput.trim(),
                                    categoryCode = categoryCodeInput.trim(),
                                    missingFieldsNote = missingNoteInput.trim(),
                                    submittedBy = adminEmail
                                )
                            )
                            Toast.makeText(
                                context,
                                if (res.isSuccess) "Added to research queue" else "Error",
                                Toast.LENGTH_SHORT
                            ).show()
                            showNewResearchDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
                ) { Text("Add Item") }
            },
            dismissButton = {
                TextButton(onClick = { showNewResearchDialog = false }) { Text("Cancel", color = RoyalSoftGold) }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminEquipmentReviewDialog(
    machine: MachineryProduct,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSaveReview: (verifiedBrand: String, verifiedModel: String, markVerified: Boolean, reviewNote: String) -> Unit
) {
    var brandInput by remember { mutableStateOf(machine.brand) }
    var modelInput by remember { mutableStateOf(machine.modelNumber) }
    var reviewNoteInput by remember { mutableStateOf(machine.adminReviewNote) }
    var markVerified by remember { mutableStateOf(!machine.pendingAdminReview && machine.brandVerified) }
    var showZoomViewer by remember { mutableStateOf(false) }
    val photos = remember(machine.photosCsv, machine.firebaseStoragePathsCsv) { machine.getPhotoTokens() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        title = {
            Column(modifier = Modifier.testTag("admin_equipment_review_dialog")) {
                Text("Admin Equipment Review & Preview", color = RoyalSoftGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("${machine.id} • ${machine.getDisplayName(currentLanguage)}", color = RoyalTextPrimary, fontSize = 12.sp)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MachineryEquipmentImage(
                    photoTokens = photos,
                    fallbackEmoji = machine.getCategoryEnum().emoji,
                    contentDescription = machine.nameEn,
                    enableSwipePager = true,
                    showZoomHint = true,
                    onImageClick = {
                        if (photos.isNotEmpty()) showZoomViewer = true
                    },
                    modifier = Modifier.fillMaxWidth().height(175.dp)
                )

                Text(
                    text = "Category: ${machine.getCategoryEnum().emoji} ${machine.getCategoryEnum().getDisplayName(currentLanguage)}",
                    fontSize = 11.sp,
                    color = RoyalSoftGold,
                    fontWeight = FontWeight.SemiBold
                )
                val purpose = machine.getDisplayPurpose(currentLanguage)
                if (purpose.isNotBlank()) {
                    Text("Purpose: $purpose", fontSize = 11.sp, color = RoyalTextSecondary)
                }
                OutlinedTextField(
                    value = brandInput,
                    onValueChange = { brandInput = it },
                    label = { Text("Verified Brand (leave blank if unconfirmed)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = modelInput,
                    onValueChange = { modelInput = it },
                    label = { Text("Verified Model") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reviewNoteInput,
                    onValueChange = { reviewNoteInput = it },
                    label = { Text("Admin Verification / Audit Note") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = markVerified, onCheckedChange = { markVerified = it })
                    Text("Clear Pending Flag & Mark Verified", fontSize = 12.sp, color = RoyalTextPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveReview(brandInput, modelInput, markVerified, reviewNoteInput) },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                modifier = Modifier.testTag("admin_review_save_btn")
            ) { Text("Save Review") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = RoyalSoftGold) }
        }
    )

    if (showZoomViewer && photos.isNotEmpty()) {
        MachineryFullScreenImageViewer(
            photoTokens = photos,
            title = machine.getDisplayName(currentLanguage),
            subtitle = "Admin Inspection Preview",
            onDismiss = { showZoomViewer = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MachineListingFormDialog(
    initial: MachineryProduct,
    isAdmin: Boolean,
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    onUploadPhoto: ((Uri, String, (String, String) -> Unit) -> Unit)? = null,
    onDismiss: () -> Unit,
    onSave: (MachineryProduct) -> Unit
) {
    var nameEn by remember { mutableStateOf(initial.nameEn) }
    var brand by remember { mutableStateOf(initial.brand) }
    var modelNo by remember { mutableStateOf(initial.modelNumber) }
    var categoryCode by remember { mutableStateOf(initial.categoryCode) }
    var subcategory by remember { mutableStateOf(initial.subcategory.ifBlank { "Power Tiller" }) }
    var hpSpec by remember { mutableStateOf(initial.horsepowerOrCapacity) }
    var crops by remember { mutableStateOf(initial.suitableCrops.ifBlank { "Paddy, Potato, Tea" }) }
    var purposeEn by remember { mutableStateOf(initial.purposeEn) }
    var instructionsEn by remember { mutableStateOf(initial.operatingInstructionsEn) }
    var safetyGuidelines by remember { mutableStateOf(initial.safetyUsageGuidelines) }
    var condition by remember { mutableStateOf(initial.condition) }
    var forSale by remember { mutableStateOf(initial.forSale) }
    var forRent by remember { mutableStateOf(initial.forRent) }
    var priceText by remember { mutableStateOf(initial.salePriceInr?.toInt()?.toString().orEmpty()) }
    var aliasesCsv by remember { mutableStateOf(initial.aliasesCsv) }
    var warrantyInfo by remember { mutableStateOf(initial.warrantyInfo) }
    var photoUriCsv by remember { mutableStateOf(initial.photosCsv) }
    var storagePathsCsv by remember { mutableStateOf(initial.firebaseStoragePathsCsv) }
    var showPreviewStep by remember { mutableStateOf(false) }

    val draftId = remember(initial.id) { initial.id.ifBlank { "mach_${System.currentTimeMillis()}" } }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val localToken = uri.toString()
            photoUriCsv = if (photoUriCsv.isBlank()) localToken else "$photoUriCsv,$localToken"
            onUploadPhoto?.invoke(uri, draftId) { downloadUrl, storagePath ->
                val updatedPhotos = photoUriCsv.split(",").map { it.trim() }.filter { it.isNotBlank() && it != localToken } + downloadUrl
                photoUriCsv = updatedPhotos.distinct().joinToString(",")
                storagePathsCsv = listOf(storagePathsCsv, storagePath)
                    .flatMap { it.split(",") }
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString(",")
            }
        }
    }

    val buildDraftProduct: () -> MachineryProduct = {
        initial.copy(
            id = draftId,
            nameEn = nameEn.trim(),
            nameBn = initial.nameBn.ifBlank { nameEn.trim() },
            nameHi = initial.nameHi.ifBlank { nameEn.trim() },
            brand = brand.trim(),
            modelNumber = modelNo.trim(),
            brandVerified = brand.isNotBlank(),
            modelVerified = modelNo.isNotBlank(),
            pendingAdminReview = !isAdmin,
            categoryCode = categoryCode,
            subcategory = subcategory.trim(),
            horsepowerOrCapacity = hpSpec.trim(),
            suitableCrops = crops.trim(),
            purposeEn = purposeEn.trim(),
            operatingInstructionsEn = instructionsEn.trim(),
            safetyUsageGuidelines = safetyGuidelines.trim(),
            condition = condition,
            forSale = forSale,
            forRent = forRent,
            salePriceInr = priceText.toDoubleOrNull(),
            aliasesCsv = aliasesCsv.trim(),
            warrantyInfo = warrantyInfo.trim(),
            photosCsv = photoUriCsv.trim(),
            firebaseStoragePathsCsv = storagePathsCsv
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        title = {
            Text(
                text = when {
                    showPreviewStep -> "Preview Listing Before Publishing"
                    isAdmin -> "Add Admin Verified Machine"
                    else -> "Submit Machine Listing"
                },
                color = RoyalSoftGold
            )
        },
        text = {
            if (showPreviewStep) {
                val draft = buildDraftProduct()
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MachineryEquipmentImage(
                        photoTokens = draft.getPhotoTokens(),
                        fallbackEmoji = draft.getCategoryEnum().emoji,
                        contentDescription = draft.nameEn,
                        enableSwipePager = true,
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                    Text(draft.nameEn.ifBlank { "(Untitled Machine)" }, fontWeight = FontWeight.Bold, color = RoyalTextPrimary, fontSize = 15.sp)
                    Text("${draft.getCategoryEnum().emoji} ${draft.getCategoryEnum().getDisplayName(currentLanguage)} • ${draft.subcategory}", fontSize = 11.sp, color = RoyalSoftGold)
                    Text("Brand / Model: ${draft.getDisplayBrandModel(currentLanguage)}", fontSize = 11.sp, color = RoyalTextPrimary)
                    if (draft.purposeEn.isNotBlank()) Text("Purpose: ${draft.purposeEn}", fontSize = 11.sp, color = RoyalTextSecondary)
                    Text("Suitable Crops: ${draft.suitableCrops}", fontSize = 11.sp, color = RoyalTextSecondary)
                    if (draft.operatingInstructionsEn.isNotBlank()) Text("Instructions: ${draft.operatingInstructionsEn}", fontSize = 11.sp, color = RoyalTextSecondary)
                    if (draft.safetyUsageGuidelines.isNotBlank()) Text("Safety: ${draft.safetyUsageGuidelines}", fontSize = 11.sp, color = RoyalWarningText)
                    Text("Price: ${draft.salePriceInr?.let { "₹${it.toInt()}" } ?: "Price on request"} • Rent: ${draft.forRent}", fontSize = 11.sp, color = RoyalGold)
                }
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("Machine Name *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = modelNo, onValueChange = { modelNo = it }, label = { Text("Model Number") }, modifier = Modifier.fillMaxWidth())
                    Text("Category (All 10)", fontSize = 11.sp, color = RoyalSoftGold, fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        MachineryCategory.values().forEach { cat ->
                            FilterChip(
                                selected = categoryCode == cat.code,
                                onClick = { categoryCode = cat.code },
                                label = { Text("${cat.emoji} ${cat.getDisplayName(currentLanguage)}", fontSize = 9.sp) }
                            )
                        }
                    }
                    OutlinedTextField(value = subcategory, onValueChange = { subcategory = it }, label = { Text("Subcategory") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = hpSpec, onValueChange = { hpSpec = it }, label = { Text("HP / Capacity Spec") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = purposeEn, onValueChange = { purposeEn = it }, label = { Text("Purpose & Usage") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = crops, onValueChange = { crops = it }, label = { Text("Suitable Crops") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = instructionsEn, onValueChange = { instructionsEn = it }, label = { Text("Operating Instructions") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = safetyGuidelines, onValueChange = { safetyGuidelines = it }, label = { Text("Safety Precautions") }, modifier = Modifier.fillMaxWidth())
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MachineryCondition.values().forEach { c ->
                            FilterChip(selected = condition == c.code, onClick = { condition = c.code }, label = { Text(c.code, fontSize = 10.sp) })
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = forSale, onCheckedChange = { forSale = it })
                        Text("For Sale", fontSize = 11.sp, color = RoyalTextPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Checkbox(checked = forRent, onCheckedChange = { forRent = it })
                        Text("For Rent", fontSize = 11.sp, color = RoyalTextPrimary)
                    }
                    OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Indicative Price INR (blank = Price on Request)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = aliasesCsv, onValueChange = { aliasesCsv = it }, label = { Text("Search Keywords / Aliases (CSV)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = warrantyInfo, onValueChange = { warrantyInfo = it }, label = { Text("Warranty / Service Terms") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = photoUriCsv, onValueChange = { photoUriCsv = it }, label = { Text("Photo Tokens / URLs (comma-separated)") }, modifier = Modifier.fillMaxWidth())
                    if (photoUriCsv.isNotBlank()) {
                        MachineryEquipmentImage(
                            photoTokens = photoUriCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            fallbackEmoji = MachineryCategory.fromCode(categoryCode).emoji,
                            contentDescription = "Attached equipment photos",
                            enableSwipePager = true,
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Attach & Compress Photo (Zero-Permission Picker)", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (!showPreviewStep) {
                    OutlinedButton(onClick = { showPreviewStep = true }) {
                        Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalSoftGold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Preview", fontSize = 11.sp, color = RoyalSoftGold)
                    }
                } else {
                    OutlinedButton(onClick = { showPreviewStep = false }) {
                        Text("Edit", fontSize = 11.sp, color = RoyalSoftGold)
                    }
                }
                Button(
                    onClick = { onSave(buildDraftProduct()) },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
                ) { Text(if (isAdmin) "Publish" else "Submit") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = RoyalSoftGold) }
        }
    )
}
