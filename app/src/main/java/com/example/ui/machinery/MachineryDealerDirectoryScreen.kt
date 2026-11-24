package com.example.ui.machinery

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datasource.MachinerySeedCatalog
import com.example.data.local.entity.FraudTargetType
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppLanguage
import com.example.data.model.MachineryDealerEntry
import com.example.data.model.NurseryVerificationStatus
import com.example.data.repository.MachineryRepository
import com.example.data.repository.TrustAndSafetyRepository
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
import com.example.ui.trust.AntiFraudWarningBanner
import com.example.ui.trust.FraudReportDialog
import com.example.ui.trust.SellerTierBadge
import com.example.util.L10n
import com.example.util.safeStartActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MachineryDealerDirectoryScreen(
    currentLanguage: AppLanguage,
    userId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { MachineryRepository(context) }
    val trustRepo = remember { TrustAndSafetyRepository(context) }

    LaunchedEffect(Unit) { repo.ensureSeeded() }

    val dealers by repo.observeAllDealers().collectAsState(initial = emptyList())
    val allOffers by repo.observeAllOffers().collectAsState(initial = emptyList())
    val suspendedSellerIds by trustRepo.observeSuspendedSellerIds().collectAsState(initial = emptySet())
    val offersCountByDealer = remember(allOffers) { allOffers.groupingBy { it.dealerId }.eachCount() }
    val localities = MachinerySeedCatalog.initialLocalities

    var searchQuery by remember { mutableStateOf("") }
    var selectedLocalityCode by remember { mutableStateOf<String?>(null) }
    var verifiedOnly by remember { mutableStateOf(false) }
    var claimingDealer by remember { mutableStateOf<MachineryDealerEntry?>(null) }
    var reportingDealer by remember { mutableStateOf<MachineryDealerEntry?>(null) }

    val filteredDealers = remember(dealers, suspendedSellerIds, searchQuery, selectedLocalityCode, verifiedOnly) {
        dealers.filter { d ->
            val notSuspended = d.dealerId !in suspendedSellerIds &&
                (d.claimedByUserId.isNullOrBlank() || d.claimedByUserId !in suspendedSellerIds)
            val matchLocality = selectedLocalityCode == null || d.localityCode.equals(selectedLocalityCode, ignoreCase = true)
            val matchVerif = !verifiedOnly || (d.isVerified && d.verificationStatus != NurseryVerificationStatus.PENDING_VERIFICATION)
            notSuspended && matchLocality && matchVerif && d.matchesSearch(searchQuery)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Column {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "কাছাকাছি কৃষি যন্ত্রপাতি দোকান ও ডিরেক্টরি"
                                AppLanguage.HINDI -> "नज़दीकी कृषि मशीनरी दुकान व डीलर खोजें"
                                AppLanguage.ENGLISH -> "FIND AGRICULTURAL MACHINERY SHOPS NEAR YOU"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "Siliguri, Darjeeling & North Bengal (${dealers.size} entries)",
                            fontSize = 11.sp,
                            color = RoyalTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("machinery_dealers_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RoyalGreenSecondaryBg,
                    titleContentColor = RoyalTextPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .imePadding(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AntiFraudWarningBanner(language = currentLanguage)
                }
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f)),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenSecondaryBg),
                        modifier = Modifier.fillMaxWidth().testTag("machinery_north_bengal_map_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Siliguri & North Bengal 20-Locality Hub Map",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = RoyalSoftGold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(RoyalGreenPrimaryBg, RoundedCornerShape(12.dp))
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                    val minLat = 25.15
                                    val maxLat = 27.15
                                    val minLng = 88.10
                                    val maxLng = 89.60
                                    localities.forEach { loc ->
                                        val nx = ((loc.centerLng - minLng) / (maxLng - minLng)).toFloat().coerceIn(0.05f, 0.95f)
                                        val ny = (1f - ((loc.centerLat - minLat) / (maxLat - minLat))).toFloat().coerceIn(0.08f, 0.92f)
                                        val isSelected = selectedLocalityCode == loc.code
                                        drawCircle(
                                            color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF4CAF50),
                                            radius = if (isSelected) 11f else 6f,
                                            center = Offset(size.width * nx, size.height * ny)
                                        )
                                    }
                                }
                                Text(
                                    text = selectedLocalityCode?.let { code ->
                                        localities.firstOrNull { it.code == code }?.getDisplayName(currentLanguage)
                                    } ?: "Tap a locality chip below • Only admin-verified shops unlock calling",
                                    fontSize = 10.sp,
                                    color = RoyalTextSecondary,
                                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        placeholder = { Text("Search shop name, Siliguri locality, district, brand...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("machinery_dealer_search")
                    )
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = selectedLocalityCode == null,
                                onClick = { selectedLocalityCode = null },
                                label = { Text("All (20)", fontSize = 11.sp) }
                            )
                        }
                        items(localities, key = { it.code }) { loc ->
                            FilterChip(
                                selected = selectedLocalityCode == loc.code,
                                onClick = {
                                    selectedLocalityCode = if (selectedLocalityCode == loc.code) null else loc.code
                                },
                                label = { Text(loc.getDisplayName(currentLanguage), fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = verifiedOnly,
                            onClick = { verifiedOnly = !verifiedOnly },
                            label = { Text("Verified Partners Only", fontSize = 11.sp) }
                        )
                        Text("${filteredDealers.size} listings", fontSize = 11.sp, color = RoyalTextSecondary)
                    }
                }

                items(filteredDealers, key = { it.dealerId }) { dealer ->
                    val isPending = !dealer.isVerified || dealer.verificationStatus == NurseryVerificationStatus.PENDING_VERIFICATION
                    val offerCount = offersCountByDealer[dealer.dealerId] ?: 0
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isPending) RoyalWarningText.copy(alpha = 0.45f) else RoyalBorder),
                        colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                        modifier = Modifier.fillMaxWidth().testTag("dealer_card_${dealer.dealerId}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dealer.getDisplayName(currentLanguage), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RoyalTextPrimary)
                                    Text("${dealer.dealerId} • ${dealer.district}, ${dealer.state}", fontSize = 11.sp, color = RoyalSoftGold)
                                }
                                SellerTierBadge(
                                    isVerified = !isPending,
                                    isAuthorisedDealer = dealer.isAuthorisedDealerVerified,
                                    language = currentLanguage
                                )
                            }

                            Text(
                                text = "Owner Consent: ${dealer.ownerConsentStatus} • Availability Not Confirmed" +
                                    if (offerCount > 0) " ($offerCount linked catalog offers)" else "",
                                fontSize = 11.sp,
                                color = if (dealer.ownerConsentStatus == "CONFIRMED") RoyalSuccessText else RoyalWarningText
                            )

                            if (!dealer.fullAddress.isNullOrBlank()) {
                                Text("Address: ${dealer.fullAddress}", fontSize = 11.sp, color = RoyalTextSecondary)
                            }
                            if (dealer.isAuthorisedDealerVerified && dealer.brandsCarriedCsv.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = RoyalSuccessText, modifier = Modifier.size(14.dp))
                                    Text("Verified Authorised Brands: ${dealer.brandsCarriedCsv}", fontSize = 11.sp, color = RoyalSuccessText)
                                }
                            }
                            if (dealer.notes.isNotBlank()) {
                                Text(dealer.notes, fontSize = 10.sp, color = RoyalTextSecondary)
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!dealer.phone.isNullOrBlank() && !isPending) {
                                    Button(
                                        onClick = {
                                            runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${dealer.phone}"))) }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call Shop", fontSize = 11.sp)
                                    }
                                }
                                val mapsTarget = dealer.fullAddress?.takeIf { it.isNotBlank() }
                                    ?: "${dealer.businessName}, ${dealer.district}, ${dealer.state}"
                                OutlinedButton(
                                    onClick = {
                                        val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(mapsTarget)}")
                                        safeStartActivity(context, Intent(Intent.ACTION_VIEW, uri), "Maps unavailable")
                                    },
                                    border = BorderStroke(1.dp, RoyalSuccessText.copy(alpha = 0.7f)),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("dealer_directions_btn_${dealer.dealerId}")
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(15.dp), tint = RoyalSuccessText)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(L10n.t("trust_get_directions", currentLanguage), fontSize = 11.sp, color = RoyalSuccessText)
                                }
                                OutlinedButton(
                                    onClick = { claimingDealer = dealer },
                                    border = BorderStroke(1.dp, RoyalGold),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("claim_dealer_btn_${dealer.dealerId}")
                                ) {
                                    Text("Claim Business Profile", fontSize = 11.sp, color = RoyalSoftGold)
                                }
                                OutlinedButton(
                                    onClick = { reportingDealer = dealer },
                                    border = BorderStroke(1.dp, RoyalWarningText.copy(alpha = 0.6f)),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("report_dealer_btn_${dealer.dealerId}")
                                ) {
                                    Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalWarningText)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Report", fontSize = 11.sp, color = RoyalWarningText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    claimingDealer?.let { target ->
        var claimantName by remember(target.dealerId) { mutableStateOf("") }
        var phone by remember(target.dealerId) { mutableStateOf("") }
        var whatsapp by remember(target.dealerId) { mutableStateOf("") }
        var address by remember(target.dealerId) { mutableStateOf("") }
        var brands by remember(target.dealerId) { mutableStateOf("") }
        var consentConfirmed by remember(target.dealerId) { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { claimingDealer = null },
            containerColor = RoyalGreenCardBg,
            title = { Text("Claim & Verify: ${target.businessName}", color = RoyalTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = claimantName, onValueChange = { claimantName = it }, label = { Text("Owner / Manager Name *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Business Phone *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp (optional)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Full Siliguri / NB Address *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = brands, onValueChange = { brands = it }, label = { Text("Machinery Brands / Services Offered") }, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = consentConfirmed, onCheckedChange = { consentConfirmed = it })
                        Text("I am an authorised owner/representative consenting to Admin verification before publishing.", fontSize = 11.sp, color = RoyalTextPrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = consentConfirmed && claimantName.isNotBlank() && phone.isNotBlank() && address.isNotBlank(),
                    onClick = {
                        scope.launch {
                            val res = repo.submitDealerClaimRequest(
                                dealerId = target.dealerId,
                                userId = userId,
                                claimantName = claimantName.trim(),
                                phone = phone.trim(),
                                whatsapp = whatsapp.trim().ifBlank { null },
                                address = address.trim(),
                                brands = brands.trim(),
                                consentConfirmedByClaimant = consentConfirmed
                            )
                            Toast.makeText(
                                context,
                                if (res.isSuccess) "Claim submitted for Admin Verification!" else (res.exceptionOrNull()?.message ?: "Error"),
                                Toast.LENGTH_LONG
                            ).show()
                            claimingDealer = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
                ) { Text("Submit Claim") }
            },
            dismissButton = {
                TextButton(onClick = { claimingDealer = null }) { Text("Cancel", color = RoyalSoftGold) }
            }
        )
    }

    reportingDealer?.let { target ->
        val session = remember { UserSessionManager.getSession(context) }
        FraudReportDialog(
            targetName = target.getDisplayName(currentLanguage),
            language = currentLanguage,
            onDismiss = { reportingDealer = null },
            onSubmit = { reasonCode, description ->
                scope.launch {
                    val res = trustRepo.submitFraudReport(
                        targetType = FraudTargetType.SELLER,
                        targetId = target.dealerId,
                        targetName = target.getDisplayName(currentLanguage),
                        sellerUserId = target.claimedByUserId ?: target.dealerId,
                        reasonCode = reasonCode,
                        description = description,
                        reporterUserId = userId,
                        reporterName = session?.userName ?: "Farmer"
                    )
                    Toast.makeText(
                        context,
                        if (res.isSuccess) "Fraud report submitted to Admin Trust Team" else (res.exceptionOrNull()?.message ?: "Failed"),
                        Toast.LENGTH_LONG
                    ).show()
                    reportingDealer = null
                }
            }
        )
    }
}
