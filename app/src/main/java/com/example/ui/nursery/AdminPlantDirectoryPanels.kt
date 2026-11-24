package com.example.ui.nursery

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.NurseryVerificationStatus
import com.example.data.model.PlantKingdomEntry
import com.example.data.model.PlantKingdomGroup
import com.example.data.model.WbNurseryDirectoryEntry
import com.example.data.model.WbNurseryDistrict
import com.example.data.repository.CsvImportExecutionResult
import com.example.data.repository.CsvImportPreviewReport
import com.example.data.repository.CsvPreviewAction
import com.example.data.repository.PlantKingdomRepository
import com.example.ui.theme.RoyalBorder
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
import kotlinx.coroutines.launch

@Composable
fun AdminWbDirectoryManagementPanel(
    currentLanguage: AppLanguage
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { PlantKingdomRepository(context) }

    LaunchedEffect(Unit) { repo.ensureSeeded() }

    val allNurseries by repo.observeAllDirectoryNurseries().collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }
    var editingNursery by remember { mutableStateOf<WbNurseryDirectoryEntry?>(null) }
    var showAddNewDialog by remember { mutableStateOf(false) }

    val filtered = remember(allNurseries, query) {
        allNurseries.filter { it.matchesSearch(query) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "পশ্চিমবঙ্গ নার্সারি যাচাই ও ব্যবস্থাপনা (${allNurseries.size})"
                        AppLanguage.HINDI -> "पश्चिम बंगाल नर्सरी सत्यापन (${allNurseries.size})"
                        AppLanguage.ENGLISH -> "WB Directory & Verification (${allNurseries.size})"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RoyalSoftGold
                )
                Button(
                    onClick = { showAddNewDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Nursery", fontSize = 12.sp)
                }
            }
        }

        item {
            AdminCsvImportUtilityCard(
                currentLanguage = currentLanguage,
                repo = repo
            )
        }

        item {
            AdminFirebaseSeedPipelineCard(
                currentLanguage = currentLanguage,
                repo = repo
            )
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search 37+ directory nurseries...") },
                modifier = Modifier.fillMaxWidth().testTag("admin_wb_dir_search"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        items(filtered, key = { it.nurseryId }) { entry ->
            val pending = !entry.isVerified || entry.verificationStatus == NurseryVerificationStatus.PENDING_VERIFICATION
            Card(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, RoyalBorder),
                colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(entry.businessName, fontWeight = FontWeight.Bold, color = RoyalTextPrimary)
                        Text(
                            text = "${entry.getDistrictEnum().enName} • ${entry.verificationStatus}",
                            fontSize = 11.sp,
                            color = if (pending) RoyalWarningText else RoyalSuccessText
                        )
                        if (!entry.phone.isNullOrBlank()) {
                            Text("📞 ${entry.phone}", fontSize = 11.sp, color = RoyalTextSecondary)
                        }
                    }
                    Button(
                        onClick = { editingNursery = entry },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pending) RoyalGold else RoyalGreenEmerald,
                            contentColor = if (pending) RoyalGreenSecondaryBg else RoyalTextPrimary
                        ),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (pending) "Verify" else "Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    editingNursery?.let { target ->
        AdminVerifyNurseryDialog(
            initial = target,
            onDismiss = { editingNursery = null },
            onSave = { owner, phone, whatsapp, addr, distCode, loc, lat, lng, hours, web, cats, wholesale, retail, delivery, note ->
                scope.launch {
                    val res = repo.verifyNurseryRecord(
                        nurseryId = target.nurseryId,
                        ownerName = owner,
                        phone = phone,
                        whatsapp = whatsapp,
                        fullAddress = addr,
                        districtCode = distCode,
                        locality = loc,
                        latitude = lat,
                        longitude = lng,
                        openingHours = hours,
                        websiteOrSocial = web,
                        categoriesCsv = cats,
                        supportsWholesale = wholesale,
                        supportsRetail = retail,
                        deliveryAvailable = delivery,
                        verifierNote = note
                    )
                    Toast.makeText(
                        context,
                        if (res.isSuccess) "Nursery verified & saved" else (res.exceptionOrNull()?.message ?: "Error"),
                        Toast.LENGTH_SHORT
                    ).show()
                    editingNursery = null
                }
            }
        )
    }

    if (showAddNewDialog) {
        AdminVerifyNurseryDialog(
            initial = WbNurseryDirectoryEntry(
                nurseryId = "",
                businessName = "",
                districtCode = WbNurseryDistrict.SILIGURI.code,
                verificationStatus = NurseryVerificationStatus.ADMIN_VERIFIED,
                isVerified = true,
                sourceReference = "Admin Directory Addition"
            ),
            isNew = true,
            onDismiss = { showAddNewDialog = false },
            onSaveNew = { name, owner, phone, whatsapp, addr, distCode, loc, lat, lng, hours, web, cats, wholesale, retail, delivery, note ->
                scope.launch {
                    val entry = WbNurseryDirectoryEntry(
                        businessName = name,
                        ownerName = owner,
                        phone = phone,
                        whatsapp = whatsapp,
                        fullAddress = addr,
                        districtCode = distCode,
                        locality = loc,
                        latitude = lat,
                        longitude = lng,
                        openingHours = hours,
                        websiteOrSocial = web,
                        availablePlantCategoriesCsv = cats,
                        supportsWholesale = wholesale,
                        supportsRetail = retail,
                        deliveryAvailable = delivery,
                        verificationStatus = NurseryVerificationStatus.ADMIN_VERIFIED,
                        isVerified = true,
                        sourceReference = note.ifBlank { "Verified by Krishak Sathi Admin" }
                    )
                    val res = repo.addOrUpdateNursery(entry)
                    Toast.makeText(
                        context,
                        if (res.isSuccess) "Nursery added to directory" else (res.exceptionOrNull()?.message ?: "Error"),
                        Toast.LENGTH_SHORT
                    ).show()
                    showAddNewDialog = false
                }
            }
        )
    }
}

@Composable
private fun AdminVerifyNurseryDialog(
    initial: WbNurseryDirectoryEntry,
    isNew: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String?, String?, String?, String?, String, String?, Double?, Double?, String?, String?, String, Boolean?, Boolean?, Boolean?, String) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onSaveNew: (String, String?, String?, String?, String?, String, String?, Double?, Double?, String?, String?, String, Boolean?, Boolean?, Boolean?, String) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> }
) {
    var name by remember { mutableStateOf(initial.businessName) }
    var owner by remember { mutableStateOf(initial.ownerName.orEmpty()) }
    var phone by remember { mutableStateOf(initial.phone.orEmpty()) }
    var whatsapp by remember { mutableStateOf(initial.whatsapp.orEmpty()) }
    var address by remember { mutableStateOf(initial.fullAddress.orEmpty()) }
    var selectedDistrict by remember { mutableStateOf(WbNurseryDistrict.fromCode(initial.districtCode)) }
    var locality by remember { mutableStateOf(initial.locality.orEmpty()) }
    var latText by remember { mutableStateOf(initial.latitude?.toString().orEmpty()) }
    var lngText by remember { mutableStateOf(initial.longitude?.toString().orEmpty()) }
    var hours by remember { mutableStateOf(initial.openingHours.orEmpty()) }
    var web by remember { mutableStateOf(initial.websiteOrSocial.orEmpty()) }
    var categories by remember { mutableStateOf(initial.availablePlantCategoriesCsv) }
    var wholesale by remember { mutableStateOf(initial.supportsWholesale == true) }
    var retail by remember { mutableStateOf(initial.supportsRetail != false) }
    var delivery by remember { mutableStateOf(initial.deliveryAvailable == true) }
    var verifierNote by remember { mutableStateOf(initial.sourceReference) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalGreenCardBg,
        titleContentColor = RoyalTextPrimary,
        textContentColor = RoyalTextPrimary,
        title = { Text(if (isNew) "Add Verified WB Nursery" else "Verify / Edit: ${initial.businessName}", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isNew) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Business Name *") }, modifier = Modifier.fillMaxWidth())
                }
                Text("District", fontSize = 11.sp, color = RoyalSoftGold, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(WbNurseryDistrict.values()) { d ->
                        FilterChip(
                            selected = selectedDistrict == d,
                            onClick = { selectedDistrict = d },
                            label = { Text(d.enName, fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(value = owner, onValueChange = { owner = it }, label = { Text("Owner Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Verified Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Full Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = locality, onValueChange = { locality = it }, label = { Text("Locality / Area") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = latText, onValueChange = { latText = it }, label = { Text("Lat (optional)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = lngText, onValueChange = { lngText = it }, label = { Text("Lng (optional)") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = hours, onValueChange = { hours = it }, label = { Text("Opening Hours") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = categories, onValueChange = { categories = it }, label = { Text("Available Categories (comma separated)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = web, onValueChange = { web = it }, label = { Text("Website / Social URL") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = verifierNote, onValueChange = { verifierNote = it }, label = { Text("Verification Source Note") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = wholesale, onCheckedChange = { wholesale = it })
                    Text("Wholesale", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(checked = delivery, onCheckedChange = { delivery = it })
                    Text("Delivery", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latText.trim().toDoubleOrNull()
                    val lng = lngText.trim().toDoubleOrNull()
                    if (isNew) {
                        onSaveNew(name, owner, phone, whatsapp, address, selectedDistrict.code, locality, lat, lng, hours, web, categories, wholesale, retail, delivery, verifierNote)
                    } else {
                        onSave(owner, phone, whatsapp, address, selectedDistrict.code, locality, lat, lng, hours, web, categories, wholesale, retail, delivery, verifierNote)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
            ) {
                Text("Verify & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = RoyalSoftGold) }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminPlantKingdomManagementPanel(
    currentLanguage: AppLanguage
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { PlantKingdomRepository(context) }

    LaunchedEffect(Unit) { repo.ensureSeeded() }

    val allPlants by repo.observeAllPlants().collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") } // ALL, PUBLISHED, DRAFT
    var showBulkImportDialog by remember { mutableStateOf(false) }
    var editingPlant by remember { mutableStateOf<PlantKingdomEntry?>(null) }
    var approvingDraftPlant by remember { mutableStateOf<PlantKingdomEntry?>(null) }
    var exportPreview by remember { mutableStateOf<String?>(null) }

    val draftCount = remember(allPlants) { allPlants.count { it.isDraftReviewRequired() } }

    val filtered = remember(allPlants, query, statusFilter) {
        allPlants.filter { plant ->
            val matchStatus = when (statusFilter) {
                "PUBLISHED" -> plant.isPublishedForPublic()
                "DRAFT" -> plant.isDraftReviewRequired()
                else -> true
            }
            matchStatus && plant.matchesSearch(query)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "উদ্ভিদ ভাণ্ডার ব্যবস্থাপনা ও বাল্ক ইম্পোর্ট (${allPlants.size} • খসড়া $draftCount)"
                    AppLanguage.HINDI -> "पौधा विश्वकोश प्रबंधन व बल्क इम्पोर्ट (${allPlants.size} • ड्राफ्ट $draftCount)"
                    AppLanguage.ENGLISH -> "Plant Kingdom & Bulk CSV/JSON Import (${allPlants.size} • Drafts: $draftCount)"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = RoyalSoftGold
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showBulkImportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                    modifier = Modifier.heightIn(min = 48.dp).testTag("admin_plant_bulk_import_btn")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CSV / JSON Import", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        editingPlant = PlantKingdomEntry(
                            plantId = "",
                            primaryGroup = PlantKingdomGroup.COMMON_FLOWERS.code,
                            climate = "Tropical / Subtropical",
                            temperatureRange = "18°C - 32°C",
                            sourceReference = "Krishak Sathi Admin Addition"
                        )
                    },
                    border = BorderStroke(1.dp, RoyalGold),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalSoftGold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Plant", fontSize = 12.sp, color = RoyalSoftGold)
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val removed = repo.removeDuplicatePlants()
                            Toast.makeText(context, "Removed $removed duplicate plants", Toast.LENGTH_SHORT).show()
                        }
                    },
                    border = BorderStroke(1.dp, RoyalBorder),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deduplicate", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            exportPreview = repo.exportPlantsToJson().take(3500)
                        }
                    },
                    border = BorderStroke(1.dp, RoyalBorder),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Export JSON", fontSize = 12.sp)
                }
            }
        }

        item {
            AdminCsvImportUtilityCard(
                currentLanguage = currentLanguage,
                repo = repo
            )
        }

        item {
            AdminFirebaseSeedPipelineCard(
                currentLanguage = currentLanguage,
                repo = repo
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = statusFilter == "ALL",
                    onClick = { statusFilter = "ALL" },
                    label = { Text("All (${allPlants.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == "PUBLISHED",
                    onClick = { statusFilter = "PUBLISHED" },
                    label = { Text("Published (${allPlants.size - draftCount})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = statusFilter == "DRAFT",
                    onClick = { statusFilter = "DRAFT" },
                    label = { Text("Draft Review ($draftCount)", fontSize = 11.sp) }
                )
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search plant EN / BN / HI / scientific name...") },
                modifier = Modifier.fillMaxWidth().testTag("admin_plant_search"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        items(filtered, key = { it.plantId }) { plant ->
            val isDraft = plant.isDraftReviewRequired()
            Card(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (isDraft) RoyalWarningText.copy(alpha = 0.6f) else RoyalBorder),
                colors = CardDefaults.cardColors(containerColor = RoyalGreenCardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlantSpeciesArtwork(
                        plant = plant,
                        lang = currentLanguage,
                        showAccuracyBadge = false,
                        modifier = Modifier.size(56.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${plant.nameEn} • ${plant.nameBn}", fontWeight = FontWeight.Bold, color = RoyalTextPrimary, fontSize = 13.sp)
                        Text(plant.scientificName, fontSize = 11.sp, color = RoyalSoftGold)
                        Text(
                            text = "${plant.publicationStatus} • ${plant.primaryGroup}",
                            fontSize = 10.sp,
                            color = if (isDraft) RoyalWarningText else RoyalTextSecondary
                        )
                        if (isDraft) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { approvingDraftPlant = plant },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoyalGold,
                                    contentColor = RoyalGreenSecondaryBg
                                ),
                                modifier = Modifier.heightIn(min = 38.dp)
                            ) {
                                Text("Review & Approve Draft", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    IconButton(onClick = { editingPlant = plant }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Plant", tint = RoyalSoftGold)
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                repo.deletePlant(plant.plantId)
                                Toast.makeText(context, "Deleted ${plant.nameEn}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Plant", tint = RoyalWarningText)
                    }
                }
            }
        }
    }

    approvingDraftPlant?.let { draft ->
        var descEn by remember(draft.plantId) { mutableStateOf(draft.descriptionEn) }
        var descBn by remember(draft.plantId) { mutableStateOf(draft.descriptionBn) }
        var careEn by remember(draft.plantId) { mutableStateOf(draft.careInstructionsEn) }
        var careBn by remember(draft.plantId) { mutableStateOf(draft.careInstructionsBn) }
        var careHi by remember(draft.plantId) { mutableStateOf(draft.careInstructionsHi) }
        var climateNote by remember(draft.plantId) { mutableStateOf(draft.climate) }

        AlertDialog(
            onDismissRequest = { approvingDraftPlant = null },
            containerColor = RoyalGreenCardBg,
            titleContentColor = RoyalTextPrimary,
            textContentColor = RoyalTextPrimary,
            title = { Text("Approve Draft: ${draft.nameEn} (${draft.plantId})", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = climateNote, onValueChange = { climateNote = it }, label = { Text("Local Suitability / Climate *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = descEn, onValueChange = { descEn = it }, label = { Text("Verified Description EN *") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = descBn, onValueChange = { descBn = it }, label = { Text("Verified Description BN") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = careEn, onValueChange = { careEn = it }, label = { Text("Care Instructions EN *") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = careBn, onValueChange = { careBn = it }, label = { Text("Care Instructions BN") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = careHi, onValueChange = { careHi = it }, label = { Text("Care Instructions HI") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = repo.approveDraftPlant(draft.plantId, descEn, descBn, careEn, careBn, careHi, climateNote)
                            Toast.makeText(
                                context,
                                if (res.isSuccess) "Published ${draft.nameEn}" else (res.exceptionOrNull()?.message ?: "Fill required fields"),
                                Toast.LENGTH_SHORT
                            ).show()
                            if (res.isSuccess) approvingDraftPlant = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
                ) { Text("Approve & Publish") }
            },
            dismissButton = {
                TextButton(onClick = { approvingDraftPlant = null }) { Text("Cancel", color = RoyalSoftGold) }
            }
        )
    }

    if (showBulkImportDialog) {
        var importText by remember { mutableStateOf("plantId,nameEn,nameBn,nameHi,scientificName,primaryGroup,climate,temperatureRange\nhimalayan_cherry,Himalayan Cherry Blossom,হিমালয়ান চেরি,हिमालयी चेरी,Prunus cerasoides,HIMALAYAN_HILL_FLOWERS,Sub-temperate hill,8°C - 22°C") }
        var isJsonMode by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showBulkImportDialog = false },
            containerColor = RoyalGreenCardBg,
            titleContentColor = RoyalTextPrimary,
            textContentColor = RoyalTextPrimary,
            title = { Text("Bulk CSV / JSON Plant Import", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !isJsonMode, onClick = { isJsonMode = false }, label = { Text("CSV Mode") })
                        FilterChip(selected = isJsonMode, onClick = { isJsonMode = true }, label = { Text("JSON Mode") })
                    }
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        minLines = 5,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = if (isJsonMode) {
                                repo.importPlantsFromJson(importText)
                            } else {
                                repo.importPlantsFromCsv(importText)
                            }
                            Toast.makeText(
                                context,
                                "Imported ${res.importedCount}, skipped ${res.skippedDuplicates} duplicates",
                                Toast.LENGTH_LONG
                            ).show()
                            showBulkImportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
                ) {
                    Text("Run Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkImportDialog = false }) { Text("Cancel", color = RoyalSoftGold) }
            }
        )
    }

    editingPlant?.let { target ->
        var plantId by remember { mutableStateOf(target.plantId) }
        var nameEn by remember { mutableStateOf(target.nameEn) }
        var nameBn by remember { mutableStateOf(target.nameBn) }
        var nameHi by remember { mutableStateOf(target.nameHi) }
        var sciName by remember { mutableStateOf(target.scientificName) }
        var photoUri by remember { mutableStateOf(target.customPhotoUri) }
        var careEn by remember { mutableStateOf(target.careInstructionsEn) }

        val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) photoUri = uri.toString()
        }

        AlertDialog(
            onDismissRequest = { editingPlant = null },
            containerColor = RoyalGreenCardBg,
            titleContentColor = RoyalTextPrimary,
            textContentColor = RoyalTextPrimary,
            title = { Text(if (target.plantId.isBlank()) "Add Plant Species" else "Edit ${target.nameEn}") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = plantId, onValueChange = { plantId = it }, label = { Text("Plant ID (snake_case)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("English Name *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nameBn, onValueChange = { nameBn = it }, label = { Text("Bengali Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nameHi, onValueChange = { nameHi = it }, label = { Text("Hindi Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = sciName, onValueChange = { sciName = it }, label = { Text("Scientific Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = careEn, onValueChange = { careEn = it }, label = { Text("Care Instructions") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedButton(
                        onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (photoUri.isBlank()) "Attach Photo (Photo Picker)" else "Photo Attached ✓")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val finalId = plantId.ifBlank { PlantKingdomRepository.sanitizeId(nameEn) }
                            val updated = target.copy(
                                plantId = finalId,
                                nameEn = nameEn.trim(),
                                nameBn = nameBn.ifBlank { nameEn }.trim(),
                                nameHi = nameHi.ifBlank { nameEn }.trim(),
                                scientificName = sciName.trim(),
                                customPhotoUri = photoUri.trim(),
                                careInstructionsEn = careEn.trim()
                            )
                            repo.upsertPlant(updated)
                            editingPlant = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald)
                ) {
                    Text("Save Plant")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPlant = null }) { Text("Cancel", color = RoyalSoftGold) }
            }
        )
    }

    exportPreview?.let { json ->
        AlertDialog(
            onDismissRequest = { exportPreview = null },
            containerColor = RoyalGreenCardBg,
            title = { Text("Exported Plant Kingdom JSON", color = RoyalTextPrimary) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(json, fontSize = 11.sp, color = RoyalTextSecondary)
                }
            },
            confirmButton = {
                TextButton(onClick = { exportPreview = null }) { Text("Close", color = RoyalSoftGold) }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminFirebaseSeedPipelineCard(
    currentLanguage: AppLanguage,
    repo: PlantKingdomRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isBusy by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val jsonFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isBusy = true
                val raw = try {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
                } catch (_: Exception) {
                    ""
                }
                if (raw.isBlank()) {
                    statusMessage = "Selected JSON file could not be read or was empty."
                } else if (raw.trim().startsWith("{")) {
                    val res = repo.importFirebaseSeedBundleJson(raw, pushToFirestore = true)
                    statusMessage = res.message
                } else {
                    val res = repo.importPlantsFromJson(raw)
                    statusMessage = "Imported ${res.importedCount} plant entries (${res.skippedDuplicates} duplicates skipped)."
                }
                isBusy = false
            }
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f)),
        colors = CardDefaults.cardColors(containerColor = RoyalGreenSecondaryBg),
        modifier = Modifier.fillMaxWidth().testTag("admin_firebase_seed_pipeline_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CloudDone, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(18.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "ফায়ারবেস সিড ডেটা ও ক্লাউড সিঙ্ক পাইপলাইন"
                        AppLanguage.HINDI -> "फ़ायरबेस सीड डेटा व क्लाउड सिंक पाइपलाइन"
                        AppLanguage.ENGLISH -> "Firebase Seed Data & Cloud Sync Pipeline"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RoyalSoftGold
                )
            }
            Text(
                text = when (currentLanguage) {
                    AppLanguage.BENGALI -> "আপলোড করা ৩৭টি নার্সারি ও ৮০টি গাছের সিড JSON প্রসেস করুন (যাচাইকৃত ডেটা সুরক্ষিত রেখে) অথবা ক্লাউড Firestore-এ সিঙ্ক করুন।"
                    AppLanguage.HINDI -> "अपलोड किए गए 37 नर्सरी व 80 पौधों के सीड JSON को सुरक्षित रूप से प्रोसेस करें और Cloud Firestore में सिंक करें।"
                    AppLanguage.ENGLISH -> "Process uploaded seed JSON (37 PENDING_VERIFICATION nurseries + 80 starter plants) preserving verified records, pick a .json file, or sync all records with Firestore."
                },
                fontSize = 11.sp,
                color = RoyalTextSecondary
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    enabled = !isBusy,
                    onClick = {
                        scope.launch {
                            isBusy = true
                            val res = repo.importBundledSeedAsset(pushToFirestore = true)
                            statusMessage = res.message
                            Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                            isBusy = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                    modifier = Modifier.heightIn(min = 48.dp).testTag("admin_process_uploaded_seed_btn")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Seed Bundle (37+80)", fontSize = 11.sp)
                }

                OutlinedButton(
                    enabled = !isBusy,
                    onClick = { jsonFilePicker.launch(arrayOf("application/json", "text/plain")) },
                    border = BorderStroke(1.dp, RoyalGold),
                    modifier = Modifier.heightIn(min = 48.dp).testTag("admin_import_json_file_btn")
                ) {
                    Text("Pick .json File", fontSize = 11.sp, color = RoyalSoftGold)
                }

                OutlinedButton(
                    enabled = !isBusy,
                    onClick = {
                        scope.launch {
                            isBusy = true
                            val syncRes = repo.syncAllCatalogAndDirectoryToFirestore()
                            statusMessage = syncRes.message
                            Toast.makeText(context, syncRes.message, Toast.LENGTH_LONG).show()
                            isBusy = false
                        }
                    },
                    border = BorderStroke(1.dp, RoyalBorder),
                    modifier = Modifier.heightIn(min = 48.dp).testTag("admin_push_all_firebase_btn")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync All to Firebase", fontSize = 11.sp)
                }

                OutlinedButton(
                    enabled = !isBusy,
                    onClick = {
                        scope.launch {
                            isBusy = true
                            val pullRes = repo.refreshFromFirestore()
                            statusMessage = pullRes.message
                            Toast.makeText(context, pullRes.message, Toast.LENGTH_SHORT).show()
                            isBusy = false
                        }
                    },
                    border = BorderStroke(1.dp, RoyalBorder),
                    modifier = Modifier.heightIn(min = 48.dp).testTag("admin_pull_firebase_btn")
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pull Cloud", fontSize = 11.sp)
                }
            }

            if (isBusy) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = RoyalGold)
                    Text("Processing Firebase seed pipeline...", fontSize = 11.sp, color = RoyalSoftGold)
                }
            }

            statusMessage?.let { msg ->
                Surface(
                    color = RoyalSuccessBg.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        fontSize = 11.sp,
                        color = RoyalTextPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminCsvImportUtilityCard(
    currentLanguage: AppLanguage,
    repo: PlantKingdomRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var previewReport by remember { mutableStateOf<CsvImportPreviewReport?>(null) }
    var executionResult by remember { mutableStateOf<CsvImportExecutionResult?>(null) }
    var customNurseryCsv by remember { mutableStateOf("") }
    var customPlantCsv by remember { mutableStateOf("") }
    var adminConfirmed by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var progressValue by remember { mutableFloatStateOf(0f) }
    var progressLabel by remember { mutableStateOf("") }

    val nurseryCsvPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val txt = try { context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty() } catch (_: Exception) { "" }
                customNurseryCsv = txt
                previewReport = repo.previewCsvImport(customNurseryCsv, customPlantCsv)
                adminConfirmed = false
                executionResult = null
            }
        }
    }

    val plantCsvPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val txt = try { context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty() } catch (_: Exception) { "" }
                customPlantCsv = txt
                previewReport = repo.previewCsvImport(customNurseryCsv, customPlantCsv)
                adminConfirmed = false
                executionResult = null
            }
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.65f)),
        colors = CardDefaults.cardColors(containerColor = RoyalGreenSecondaryBg),
        modifier = Modifier.fillMaxWidth().testTag("admin_csv_import_utility_card")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.UploadFile, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(18.dp))
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "অ্যাডমিন CSV ড্রাই-রান ইম্পোর্ট ইউটিলিটি (৩৭ নার্সারি + ৮০ গাছ)"
                        AppLanguage.HINDI -> "एडमिन CSV ड्राई-रन इम्पोर्ट यूटिलिटी (37 नर्सरी + 80 पौधे)"
                        AppLanguage.ENGLISH -> "Admin CSV Dry-Run Import Utility (37 Nurseries + 80 Plants)"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RoyalSoftGold
                )
            }
            Text(
                text = "Parses UTF-8 Bengali/Hindi CSVs skipping intro lines, prevents duplicates via stable IDs, keeps starter plants as DRAFT_REVIEW_REQUIRED and nurseries as PENDING_VERIFICATION / NOT_CONFIRMED.",
                fontSize = 11.sp,
                color = RoyalTextSecondary
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = !isRunning,
                    onClick = {
                        scope.launch {
                            isRunning = true
                            adminConfirmed = false
                            executionResult = null
                            previewReport = repo.previewBundledCsvImport()
                            isRunning = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGreenEmerald),
                    modifier = Modifier.heightIn(min = 48.dp).testTag("admin_preview_bundled_csv_btn")
                ) {
                    Text("Preview Bundled CSVs (37+80)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    enabled = !isRunning,
                    onClick = { nurseryCsvPicker.launch(arrayOf("text/*")) },
                    border = BorderStroke(1.dp, RoyalBorder),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Pick Nursery .txt/.csv", fontSize = 11.sp)
                }

                OutlinedButton(
                    enabled = !isRunning,
                    onClick = { plantCsvPicker.launch(arrayOf("text/*")) },
                    border = BorderStroke(1.dp, RoyalBorder),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Pick Plant .txt/.csv", fontSize = 11.sp)
                }
            }

            previewReport?.let { rep ->
                HorizontalDivider(color = RoyalBorder)
                Surface(
                    color = RoyalGreenCardBg,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, RoyalBorder),
                    modifier = Modifier.fillMaxWidth().testTag("admin_csv_dry_run_summary")
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Dry-Run Preview Summary", fontWeight = FontWeight.Bold, color = RoyalGold, fontSize = 12.sp)
                        Text(
                            text = "Total Records: ${rep.totalRecords} • New: ${rep.totalNewRecords} • Existing/Matched: ${rep.totalExistingRecords} • Errors: ${rep.totalErrors}",
                            fontSize = 11.sp,
                            color = RoyalTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Nurseries -> Total ${rep.nurseryTotalRows} | New ${rep.nurseryNewCount} | Upsert ${rep.nurseryExistingCount} | Verified Preserved ${rep.nurserySkippedVerifiedCount}",
                            fontSize = 11.sp,
                            color = RoyalTextSecondary
                        )
                        Text(
                            text = "Plants -> Total ${rep.plantTotalRows} | New ${rep.plantNewCount} | Matched ${rep.plantExistingCount} | DRAFT_REVIEW_REQUIRED ${rep.plantDraftCount}",
                            fontSize = 11.sp,
                            color = RoyalWarningText
                        )
                        if (rep.errorLogLines.isNotEmpty()) {
                            Text("Errors: ${rep.errorLogLines.take(3).joinToString(" | ")}", fontSize = 10.sp, color = RoyalWarningText)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        rep.previewRows.take(6).forEach { row ->
                            Text(
                                text = "• [${row.stableId}] ${row.displayName} (${row.action}): ${row.note}",
                                fontSize = 10.sp,
                                color = RoyalTextSecondary
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = adminConfirmed,
                        onCheckedChange = { adminConfirmed = it },
                        modifier = Modifier.testTag("admin_csv_confirm_checkbox")
                    )
                    Text(
                        text = "I confirm importing ${rep.preparedNurseries.size} nurseries (PENDING_VERIFICATION / NOT_CONFIRMED) & ${rep.preparedPlants.size} plants (${rep.plantDraftCount} drafts) without overwriting verified records.",
                        fontSize = 11.sp,
                        color = RoyalTextPrimary
                    )
                }

                Button(
                    enabled = adminConfirmed && !isRunning && rep.totalRecords > 0,
                    onClick = {
                        scope.launch {
                            isRunning = true
                            executionResult = repo.executeConfirmedCsvImport(
                                preview = rep,
                                adminConfirmed = adminConfirmed,
                                pushToFirestore = true,
                                onProgress = { p, lbl ->
                                    progressValue = p
                                    progressLabel = lbl
                                }
                            )
                            isRunning = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = RoyalGreenSecondaryBg),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("admin_execute_csv_import_btn")
                ) {
                    Text("Execute Confirmed Import", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (isRunning && progressLabel.isNotBlank()) {
                LinearProgressIndicator(progress = { progressValue }, modifier = Modifier.fillMaxWidth(), color = RoyalGold)
                Text(progressLabel, fontSize = 10.sp, color = RoyalSoftGold)
            }

            executionResult?.let { res ->
                Surface(
                    color = RoyalSuccessBg.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admin_csv_execution_result")
                ) {
                    Text(res.summaryMessage, fontSize = 11.sp, color = RoyalTextPrimary, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

