package com.example.ui.machinery

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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MachineryCategory
import com.example.data.model.MachineryDealerOffer
import com.example.data.model.MachineryManufacturer
import com.example.data.model.MachineryResearchQueueItem
import com.example.data.model.MachineryReviewRecord
import com.example.data.repository.CsvMachineryPreview
import com.example.data.repository.DuplicateMachineryGroup
import com.example.data.repository.MachineryRepository
import com.example.ui.theme.RoyalAlertBg
import com.example.ui.theme.RoyalAlertText
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalSuccessBg
import com.example.ui.theme.RoyalSuccessText
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.ui.theme.RoyalWarningBg
import com.example.ui.theme.RoyalWarningText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
fun LazyListScope.adminOffersAndOemsTabItems(
    allOffers: List<MachineryDealerOffer>,
    allManufacturers: List<MachineryManufacturer>,
    repo: MachineryRepository,
    adminEmail: String,
    scope: CoroutineScope,
    onOpenAddOem: () -> Unit,
    onOpenAddOffer: () -> Unit
) {
    item {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onOpenAddOem,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = RoyalGreenPrimaryBg),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add / Update OEM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onOpenAddOffer,
                border = BorderStroke(1.dp, RoyalGold),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp), tint = RoyalSoftGold)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Dealer Offer", fontSize = 11.sp, color = RoyalSoftGold)
            }
        }
    }

    item {
        Text("Dealer Catalog Offers (${allOffers.size})", fontWeight = FontWeight.Bold, color = RoyalSoftGold, fontSize = 14.sp)
    }
    items(allOffers, key = { it.offerId }) { offer ->
        Card(colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg), border = BorderStroke(1.dp, RoyalBorder)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${offer.dealerName} -> ${offer.machineId}", fontWeight = FontWeight.Bold, color = RoyalTextPrimary, fontSize = 13.sp)
                Text("Locality: ${offer.localityCode} • Stock: ${offer.stockStatus} • Outdated: ${offer.isOutdatedPrice}", fontSize = 11.sp, color = RoyalTextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { scope.launch { repo.flagOfferOutdated(offer.offerId, !offer.isOutdatedPrice, adminEmail) } },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalWarningText)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (offer.isOutdatedPrice) "Clear Outdated" else "Flag Outdated", fontSize = 11.sp, color = RoyalWarningText)
                    }
                    OutlinedButton(
                        onClick = { scope.launch { repo.deleteOffer(offer.offerId, adminEmail) } },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalAlertText)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Offer", fontSize = 11.sp, color = RoyalAlertText)
                    }
                }
            }
        }
    }

    item {
        HorizontalDivider(color = RoyalBorder)
        Text("Verified OEM Portals (${allManufacturers.size})", fontWeight = FontWeight.Bold, color = RoyalSoftGold, fontSize = 14.sp)
    }
    items(allManufacturers, key = { it.id }) { oem ->
        Card(colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg), border = BorderStroke(1.dp, RoyalBorder)) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("${oem.nameEn} (${oem.countryOrOrigin})", fontWeight = FontWeight.Bold, color = RoyalTextPrimary, fontSize = 12.sp)
                Text(oem.officialPortalUrl, fontSize = 10.sp, color = RoyalSoftGold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
fun LazyListScope.adminResearchQueueTabItems(
    queue: List<MachineryResearchQueueItem>,
    repo: MachineryRepository,
    adminEmail: String,
    scope: CoroutineScope,
    onOpenNewResearchItem: () -> Unit
) {
    item {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Admin Research & Verification Queue (${queue.size})", fontWeight = FontWeight.Bold, color = RoyalSoftGold, fontSize = 14.sp)
            Button(
                onClick = onOpenNewResearchItem,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Item", fontSize = 11.sp)
            }
        }
    }

    items(queue, key = { it.id }) { item ->
        Card(colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg), border = BorderStroke(1.dp, RoyalBorder)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.equipmentName, fontWeight = FontWeight.Bold, color = RoyalTextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Surface(
                        color = if (item.status == "APPROVED") RoyalSuccessBg else RoyalWarningBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            item.status,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.status == "APPROVED") RoyalSuccessText else RoyalWarningText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text("Machine ID: ${item.machineId} • Category: ${item.categoryCode}", fontSize = 10.sp, color = RoyalSoftGold)
                Text(item.missingFieldsNote, fontSize = 11.sp, color = RoyalTextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { scope.launch { repo.resolveResearchItem(item.id, "APPROVED", adminEmail) } },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { scope.launch { repo.resolveResearchItem(item.id, "REJECTED", adminEmail) } },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalAlertText)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 11.sp, color = RoyalAlertText)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDuplicatesAndCsvSection(
    repo: MachineryRepository,
    adminEmail: String,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    var dupGroups by remember { mutableStateOf<List<DuplicateMachineryGroup>>(emptyList()) }
    var scannedOnce by remember { mutableStateOf(false) }

    var csvText by remember {
        mutableStateOf(
            "nameEn,categoryCode,subcategory,brand,modelNumber,sellerBusinessName,horsepowerOrCapacity,fuelOrPowerType,suitableCrops\n" +
                "Sample Seed Drill,SEEDING_PLANTING,Zero-Till Seed Drill,Pending Verification,Pending Verification,Admin Catalog Import,35-45 HP Tractor,Tractor PTO,Wheat & Paddy"
        )
    }
    var csvPreview by remember { mutableStateOf<CsvMachineryPreview?>(null) }
    var lastBatchId by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg), border = BorderStroke(1.dp, RoyalBorder)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Duplicate Machine Detection & Safe Merge", fontWeight = FontWeight.Bold, color = RoyalSoftGold, fontSize = 14.sp)
                Button(
                    onClick = {
                        scope.launch {
                            dupGroups = repo.findDuplicateProductGroups()
                            scannedOnce = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                    modifier = Modifier.heightIn(min = 48.dp).testTag("admin_scan_duplicates_btn")
                ) { Text("Scan Catalog for Duplicates") }

                if (scannedOnce && dupGroups.isEmpty()) {
                    Text("No duplicate machinery names or brand+model pairs detected.", fontSize = 11.sp, color = RoyalSuccessText)
                }
                dupGroups.forEach { group ->
                    Surface(color = RoyalWarningBg, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(group.matchReason, fontWeight = FontWeight.Bold, color = RoyalWarningText, fontSize = 12.sp)
                            group.products.forEach { p ->
                                Text("• ${p.id}: ${p.nameEn} (${p.photosCsv})", fontSize = 10.sp, color = RoyalTextPrimary)
                            }
                            if (group.products.size >= 2) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            val keep = group.products.first().id
                                            val dup = group.products[1].id
                                            val res = repo.mergeDuplicateProducts(keep, dup, adminEmail)
                                            if (res.isSuccess) {
                                                dupGroups = repo.findDuplicateProductGroups()
                                                Toast.makeText(context, "Merged $dup into $keep", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Icon(Icons.Default.MergeType, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalSoftGold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Merge Duplicate #2 into #1", fontSize = 11.sp, color = RoyalSoftGold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg), border = BorderStroke(1.dp, RoyalBorder)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CSV Bulk Import Preview & Batch Rollback", fontWeight = FontWeight.Bold, color = RoyalSoftGold, fontSize = 14.sp)
                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    label = { Text("CSV Rows") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { csvPreview = repo.previewCsvImport(csvText) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("admin_csv_preview_btn")
                    ) {
                        Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalSoftGold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Preview CSV", fontSize = 11.sp, color = RoyalSoftGold)
                    }
                    Button(
                        enabled = csvPreview?.validRows?.isNotEmpty() == true,
                        onClick = {
                            val rows = csvPreview?.validRows ?: return@Button
                            scope.launch {
                                val res = repo.commitCsvImportWithBatch(rows, adminEmail)
                                if (res.isSuccess) {
                                    val (batchId, count) = res.getOrThrow()
                                    lastBatchId = batchId
                                    Toast.makeText(context, "Imported $count rows ($batchId)", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                        modifier = Modifier.heightIn(min = 48.dp).testTag("admin_csv_commit_btn")
                    ) { Text("Commit Batch", fontSize = 11.sp) }
                }
                csvPreview?.let { prev ->
                    Text("Valid: ${prev.validRows.size} | Errors: ${prev.errors.size}", fontSize = 11.sp, color = RoyalSuccessText)
                    prev.errors.forEach { err ->
                        Text("• $err", fontSize = 10.sp, color = RoyalAlertText)
                    }
                }
                if (lastBatchId.isNotBlank()) {
                    OutlinedTextField(
                        value = lastBatchId,
                        onValueChange = { lastBatchId = it },
                        label = { Text("Batch ID for Rollback") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val res = repo.rollbackCsvImportBatch(lastBatchId, adminEmail)
                                Toast.makeText(
                                    context,
                                    if (res.isSuccess) "Rolled back ${res.getOrThrow()} rows" else "Rollback error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("admin_csv_rollback_btn")
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalAlertText)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rollback Batch", fontSize = 11.sp, color = RoyalAlertText)
                    }
                }
            }
        }
    }
}

fun LazyListScope.adminReviewModerationItems(
    allReviews: List<MachineryReviewRecord>,
    repo: MachineryRepository,
    adminEmail: String,
    scope: CoroutineScope
) {
    item {
        HorizontalDivider(color = RoyalBorder)
        Text("Farmer Reviews Moderation (${allReviews.size})", fontWeight = FontWeight.Bold, color = RoyalSoftGold, fontSize = 14.sp)
    }
    items(allReviews, key = { "mod_rev_${it.id}" }) { rev ->
        Card(colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg), border = BorderStroke(1.dp, RoyalBorder)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${rev.userName} • ${rev.rating}★ (${rev.machineId})", fontWeight = FontWeight.Bold, color = RoyalTextPrimary, fontSize = 12.sp)
                    Text(rev.comment, fontSize = 11.sp, color = RoyalTextSecondary)
                }
                OutlinedButton(
                    onClick = { scope.launch { repo.deleteReview(rev.id, adminEmail) } },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalAlertText)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 10.sp, color = RoyalAlertText)
                }
            }
        }
    }
}

@Composable
fun AdminOemFormDialog(
    onDismiss: () -> Unit,
    onSave: (MachineryManufacturer) -> Unit
) {
    var nameEn by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("India") }
    var focus by remember { mutableStateOf("") }
    var portalUrl by remember { mutableStateOf("https://") }
    var helpline by remember { mutableStateOf("") }
    var cats by remember { mutableStateOf(MachineryCategory.TRACTORS_LAND_PREP.code) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        title = { Text("Add / Update Verified OEM Portal", color = RoyalSoftGold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("Manufacturer Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country / Origin") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = focus, onValueChange = { focus = it }, label = { Text("Focus Summary") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = portalUrl, onValueChange = { portalUrl = it }, label = { Text("Official HTTPS Portal") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = helpline, onValueChange = { helpline = it }, label = { Text("Official Helpline") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cats, onValueChange = { cats = it }, label = { Text("Category Codes CSV") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                enabled = nameEn.isNotBlank(),
                onClick = {
                    onSave(
                        MachineryManufacturer(
                            nameEn = nameEn.trim(),
                            countryOrOrigin = country.trim(),
                            focusSummaryEn = focus.trim(),
                            officialPortalUrl = portalUrl.trim(),
                            customerCareHelpline = helpline.trim(),
                            supportedCategoriesCsv = cats.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
            ) { Text("Save OEM") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = RoyalSoftGold) }
        }
    )
}

@Composable
fun DealerOfferEditorDialog(
    defaultDealerId: String = "KS-MC-0001",
    defaultDealerName: String = "",
    defaultLocality: String = "SILIGURI",
    onDismiss: () -> Unit,
    onSave: (MachineryDealerOffer) -> Unit
) {
    var machineId by remember { mutableStateOf("ref_mahindra_mahavator_rotavator") }
    var dealerId by remember { mutableStateOf(defaultDealerId) }
    var dealerName by remember { mutableStateOf(defaultDealerName) }
    var locality by remember { mutableStateOf(defaultLocality) }
    var dealerPriceText by remember { mutableStateOf("") }
    var stockStatus by remember { mutableStateOf("Availability Not Confirmed") }
    var offersRent by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        title = { Text("Publish / Update Dealer Offer", color = RoyalSoftGold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = machineId, onValueChange = { machineId = it }, label = { Text("Catalog Machine ID *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dealerId, onValueChange = { dealerId = it }, label = { Text("Dealer ID *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dealerName, onValueChange = { dealerName = it }, label = { Text("Dealer Business Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = locality, onValueChange = { locality = it }, label = { Text("Locality Code") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dealerPriceText, onValueChange = { dealerPriceText = it }, label = { Text("Confirmed Price INR (blank = on request)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = stockStatus, onValueChange = { stockStatus = it }, label = { Text("Stock Status") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = offersRent, onCheckedChange = { offersRent = it })
                    Text("Offers Rental", fontSize = 12.sp, color = RoyalTextPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = machineId.isNotBlank() && dealerName.isNotBlank(),
                onClick = {
                    onSave(
                        MachineryDealerOffer(
                            machineId = machineId.trim(),
                            dealerId = dealerId.trim(),
                            dealerName = dealerName.trim(),
                            localityCode = locality.trim(),
                            dealerPriceInr = dealerPriceText.toDoubleOrNull(),
                            offersRent = offersRent,
                            stockStatus = stockStatus.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
            ) { Text("Save Offer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = RoyalSoftGold) }
        }
    )
}
