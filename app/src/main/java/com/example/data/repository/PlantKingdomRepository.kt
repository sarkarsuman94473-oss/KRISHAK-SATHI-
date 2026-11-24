package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.datasource.PlantKingdomCatalog
import com.example.data.datasource.PlantPhotoRegistry
import com.example.data.datasource.WbNurseryDirectorySeed
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PlantKingdomEntity
import com.example.data.local.entity.WbNurseryDirectoryEntity
import com.example.data.model.NurseryApplication
import com.example.data.model.NurseryVerificationStatus
import com.example.data.model.PlantKingdomEntry
import com.example.data.model.PlantKingdomGroup
import com.example.data.model.WbNurseryDirectoryEntry
import com.example.data.model.WbNurseryDistrict
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class BulkImportResult(
    val importedCount: Int,
    val skippedDuplicates: Int,
    val errors: List<String> = emptyList()
)

data class FirebaseSeedImportResult(
    val nurseriesProcessed: Int,
    val nurseriesSkippedVerified: Int,
    val plantsMatchedEnriched: Int,
    val plantsNewlyAdded: Int,
    val firestoreSyncedDocs: Int,
    val cloudPushSucceeded: Boolean,
    val message: String,
    val errors: List<String> = emptyList()
)

data class FirebaseSyncSummary(
    val plantsCount: Int,
    val nurseriesCount: Int,
    val cloudSuccess: Boolean,
    val message: String
)

class PlantKingdomRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val dao = database.plantKingdomDao()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.w("PlantKingdomRepo", "Firestore unavailable: ${e.message}")
            null
        }
    }

    fun observeAllPlants(): Flow<List<PlantKingdomEntry>> =
        dao.observeAllPlants().map { list -> list.map { it.toModel() } }

    fun observeAllDirectoryNurseries(): Flow<List<WbNurseryDirectoryEntry>> =
        dao.observeAllNurseries().map { list -> list.map { it.toModel() } }

    /**
     * Merges persisted Room WB nursery directory entries (including all 37 PENDING_VERIFICATION
     * initial listings and any admin-verified listings) with dynamically approved Partner
     * NurseryApplications so customer discovery always stays up to date without fabricating data.
     */
    fun observeUnifiedNurseries(
        applicationsFlow: Flow<List<NurseryApplication>>
    ): Flow<List<WbNurseryDirectoryEntry>> = combine(
        observeAllDirectoryNurseries(),
        applicationsFlow
    ) { directoryList, applications ->
        val existingIds = directoryList.map { it.nurseryId }.toHashSet()
        val approvedPartnerEntries = applications
            .filter { it.status.equals("APPROVED", ignoreCase = true) }
            .filter { !existingIds.contains("partner_${it.id}") && !existingIds.contains(it.id) }
            .map { app ->
                val district = WbNurseryDistrict.fromCode(app.district)
                WbNurseryDirectoryEntry(
                    nurseryId = "partner_${app.id}",
                    businessName = app.nurseryNameEn.ifBlank { app.nurseryNameBn },
                    businessNameBn = app.nurseryNameBn.ifBlank { app.nurseryNameEn },
                    businessNameHi = app.nurseryNameEn.ifBlank { app.nurseryNameBn },
                    ownerName = app.applicantName.ifBlank { null },
                    fullAddress = app.fullAddress.ifBlank { null },
                    districtCode = district.code,
                    locality = app.cityOrTown.ifBlank { null },
                    state = app.state.ifBlank { "West Bengal" },
                    phone = app.businessPhone.ifBlank { app.mobileNumber }.ifBlank { null },
                    whatsapp = app.whatsappNumber.ifBlank { app.mobileNumber }.ifBlank { null },
                    latitude = if (app.isLocationAutoDetected || app.mapLocationUrl.isNotBlank()) app.latitude else null,
                    longitude = if (app.isLocationAutoDetected || app.mapLocationUrl.isNotBlank()) app.longitude else null,
                    websiteOrSocial = app.mapLocationUrl.ifBlank { null },
                    openingHours = app.openingHours.ifBlank { null },
                    nurseryPhotosCsv = "",
                    availablePlantCategoriesCsv = app.primaryCategory,
                    availablePlantIdsCsv = "",
                    supportsWholesale = null,
                    supportsRetail = true,
                    deliveryAvailable = app.isDeliveryAvailable,
                    descriptionEn = app.description.ifBlank { "Approved Partner Nursery on Krishak Sathi" },
                    descriptionBn = app.description.ifBlank { "কৃষক সাথী অনুমোদিত পার্টনার নার্সারি" },
                    descriptionHi = app.description.ifBlank { "कृषक साथी अनुमोदित पार्टनर नर्सरी" },
                    verificationStatus = NurseryVerificationStatus.VERIFIED_PARTNER,
                    isVerified = true,
                    sourceReference = "Approved Partner Application (${app.id})",
                    lastVerifiedDate = todayIso(),
                    linkedSellerUserId = app.userId,
                    updatedAtMillis = if (app.reviewedAtMillis > 0L) app.reviewedAtMillis else app.submittedAtMillis
                )
            }
        (approvedPartnerEntries + directoryList).sortedWith(
            compareByDescending<WbNurseryDirectoryEntry> { it.isVerified }
                .thenByDescending { it.districtCode.equals("SILIGURI", ignoreCase = true) }
                .thenBy { if (it.nurseryId.startsWith("wb_nurs_")) it.nurseryId else "zz_${it.businessName.lowercase()}" }
                .thenBy { it.businessName.lowercase() }
        )
    }

    suspend fun ensureSeeded() {
        val existingPlants = dao.getAllPlantsOnce().associateBy { it.plantId }
        val missingPlants = PlantKingdomCatalog.allPlants
            .filter { !existingPlants.containsKey(it.plantId) }
            .map { PlantKingdomEntity.fromModel(it) }
        if (missingPlants.isNotEmpty()) {
            dao.upsertPlants(missingPlants)
        }

        val existingNurseries = dao.getAllNurseriesOnce().associateBy { it.nurseryId }
        val missingNurseries = WbNurseryDirectorySeed.initialPendingNurseries
            .filter { !existingNurseries.containsKey(it.nurseryId) }
            .map { WbNurseryDirectoryEntity.fromModel(it) }
        if (missingNurseries.isNotEmpty()) {
            dao.upsertNurseries(missingNurseries)
        }
    }

    suspend fun upsertPlant(entry: PlantKingdomEntry): Result<Unit> {
        return try {
            if (entry.plantId.isBlank() || entry.nameEn.isBlank()) {
                return Result.failure(IllegalArgumentException("plantId and English name are required"))
            }
            val cleaned = entry.copy(
                plantId = sanitizeId(entry.plantId),
                visualAssetKey = entry.visualAssetKey.ifBlank {
                    PlantPhotoRegistry.getVisualAssetKey(entry.plantId)
                },
                lastVerifiedDate = entry.lastVerifiedDate.ifBlank { todayIso() },
                updatedAtMillis = System.currentTimeMillis()
            )
            dao.upsertPlant(PlantKingdomEntity.fromModel(cleaned))
            syncPlantToFirestore(cleaned)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePlant(plantId: String): Result<Unit> {
        return try {
            dao.deletePlantById(plantId)
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("plant_kingdom")?.document(plantId)?.delete()?.await()
                }
            } catch (e: Exception) {
                Log.w("PlantKingdomRepo", "Firestore delete skipped: ${e.message}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePlantTranslations(
        plantId: String,
        nameEn: String,
        nameBn: String,
        nameHi: String,
        descEn: String,
        descBn: String,
        descHi: String,
        careEn: String,
        careBn: String,
        careHi: String
    ): Result<Unit> {
        val existing = dao.getPlantById(plantId)?.toModel()
            ?: return Result.failure(NoSuchElementException("Plant $plantId not found"))
        val updated = existing.copy(
            nameEn = nameEn.trim(),
            nameBn = nameBn.trim(),
            nameHi = nameHi.trim(),
            descriptionEn = descEn.trim(),
            descriptionBn = descBn.trim(),
            descriptionHi = descHi.trim(),
            careInstructionsEn = careEn.trim(),
            careInstructionsBn = careBn.trim(),
            careInstructionsHi = careHi.trim(),
            lastVerifiedDate = todayIso(),
            updatedAtMillis = System.currentTimeMillis()
        )
        return upsertPlant(updated)
    }

    suspend fun attachPlantPhoto(plantId: String, photoUri: String): Result<Unit> {
        val existing = dao.getPlantById(plantId)?.toModel()
            ?: return Result.failure(NoSuchElementException("Plant $plantId not found"))
        val updated = existing.copy(
            customPhotoUri = photoUri.trim(),
            updatedAtMillis = System.currentTimeMillis()
        )
        return upsertPlant(updated)
    }

    suspend fun updatePlantNurseryAvailability(
        plantId: String,
        verifiedNurseryId: String,
        isAvailable: Boolean
    ): Result<Unit> {
        val nursery = dao.getNurseryById(verifiedNurseryId)?.toModel()
        if (nursery != null && !nursery.isVerified) {
            return Result.failure(IllegalStateException("Only verified nurseries can be marked as stocking commercial inventory"))
        }
        val existingPlant = dao.getPlantById(plantId)?.toModel()
            ?: return Result.failure(NoSuchElementException("Plant $plantId not found"))

        val idSet = existingPlant.verifiedNurseryIdsCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toMutableSet()
        if (isAvailable) idSet.add(verifiedNurseryId) else idSet.remove(verifiedNurseryId)

        val res = upsertPlant(existingPlant.copy(verifiedNurseryIdsCsv = idSet.joinToString(",")))
        if (nursery != null) {
            val plantSet = nursery.availablePlantIdsCsv
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toMutableSet()
            if (isAvailable) plantSet.add(plantId) else plantSet.remove(plantId)
            addOrUpdateNursery(nursery.copy(availablePlantIdsCsv = plantSet.joinToString(",")))
        }
        return res
    }

    /**
     * Detects and removes duplicate records matching on normalized scientificName + nameEn.
     * Keeps the canonical/earliest entry and deletes extra duplicates.
     */
    suspend fun removeDuplicatePlants(): Int {
        val all = dao.getAllPlantsOnce()
        val seen = HashMap<String, PlantKingdomEntity>()
        val duplicateIds = mutableListOf<String>()
        for (entity in all) {
            val sig = if (entity.scientificName.isNotBlank()) {
                "${entity.scientificName.trim().lowercase()}::${entity.nameEn.trim().lowercase()}"
            } else {
                entity.nameEn.trim().lowercase()
            }
            if (seen.containsKey(sig)) {
                duplicateIds.add(entity.plantId)
            } else {
                seen[sig] = entity
            }
        }
        if (duplicateIds.isNotEmpty()) {
            dao.deletePlantsByIds(duplicateIds)
        }
        return duplicateIds.size
    }

    suspend fun importPlantsFromCsv(csvText: String): BulkImportResult {
        val lines = csvText.lines().filter { it.isNotBlank() }
        if (lines.size < 2) {
            return BulkImportResult(0, 0, listOf("CSV must include a header row and at least one data row"))
        }
        val headers = parseCsvRow(lines.first()).map { it.trim().lowercase() }
        val existingById = dao.getAllPlantsOnce().associateBy { it.plantId }.toMutableMap()
        val existingSignatures = existingById.values.map {
            "${it.scientificName.trim().lowercase()}::${it.nameEn.trim().lowercase()}"
        }.toHashSet()

        val toUpsert = mutableListOf<PlantKingdomEntity>()
        val errors = mutableListOf<String>()
        var skipped = 0

        for (rowIdx in 1 until lines.size) {
            val cols = parseCsvRow(lines[rowIdx])
            fun col(vararg names: String): String {
                for (name in names) {
                    val idx = headers.indexOf(name.lowercase())
                    if (idx >= 0 && idx < cols.size) return cols[idx].trim()
                }
                return ""
            }

            val nameEn = col("nameen", "name_en", "name", "english_name")
            val rawId = col("plantid", "plant_id", "id").ifBlank { sanitizeId(nameEn) }
            val sciName = col("scientificname", "scientific_name", "botanical_name")
            if (rawId.isBlank() || nameEn.isBlank()) {
                errors.add("Row ${rowIdx + 1}: missing plantId or nameEn")
                continue
            }
            val sig = "${sciName.trim().lowercase()}::${nameEn.trim().lowercase()}"
            if (existingSignatures.contains(sig) && !existingById.containsKey(rawId)) {
                skipped++
                continue
            }

            val entry = PlantKingdomEntry(
                plantId = rawId,
                nameEn = nameEn,
                nameBn = col("namebn", "name_bn", "bengali_name").ifBlank { nameEn },
                nameHi = col("namehi", "name_hi", "hindi_name").ifBlank { nameEn },
                scientificName = sciName,
                primaryGroup = PlantKingdomGroup.fromCode(col("primarygroup", "group", "category")).code,
                filterTagsCsv = col("filtertags", "filtertagscsv", "tags"),
                seasonalTagsCsv = col("seasonaltags", "seasonaltagscsv", "season_tags"),
                descriptionEn = col("descriptionen", "description_en", "description"),
                descriptionBn = col("descriptionbn", "description_bn"),
                descriptionHi = col("descriptionhi", "description_hi"),
                visualAssetKey = PlantPhotoRegistry.getVisualAssetKey(rawId),
                customPhotoUri = col("photourl", "customphotouri", "image_url"),
                climate = col("climate"),
                temperatureRange = col("temperaturerange", "temperature"),
                soilRequirements = col("soilrequirements", "soil"),
                sunlight = col("sunlight"),
                wateringFrequency = col("wateringfrequency", "watering"),
                fertilizerGuidance = col("fertilizerguidance", "fertilizer"),
                propagationMethod = col("propagationmethod", "propagation"),
                floweringOrFruitingSeason = col("floweringorfruitingseason", "season"),
                growthSize = col("growthsize", "size"),
                indoorOutdoorSuitability = col("indooroutdoorsuitability", "indoor_outdoor"),
                plainsHillSuitability = col("plainshillsuitability", "plains_hill"),
                commonPestsDiseases = col("commonpestsdiseases", "pests_diseases"),
                careInstructionsEn = col("careinstructionsen", "care_en", "care"),
                careInstructionsBn = col("careinstructionsbn", "care_bn"),
                careInstructionsHi = col("careinstructionshi", "care_hi"),
                isVerifiedEntry = true,
                sourceReference = col("sourcereference", "source").ifBlank { "Admin CSV Bulk Import" },
                lastVerifiedDate = todayIso(),
                updatedAtMillis = System.currentTimeMillis()
            )
            val entity = PlantKingdomEntity.fromModel(entry)
            toUpsert.add(entity)
            existingById[rawId] = entity
            existingSignatures.add(sig)
        }

        if (toUpsert.isNotEmpty()) {
            dao.upsertPlants(toUpsert)
            toUpsert.forEach { syncPlantToFirestore(it.toModel()) }
        }
        return BulkImportResult(toUpsert.size, skipped, errors)
    }

    suspend fun importPlantsFromJson(jsonText: String): BulkImportResult {
        val trimmed = jsonText.trim()
        if (trimmed.startsWith("{")) {
            val seedRes = importFirebaseSeedBundleJson(trimmed, pushToFirestore = true)
            return BulkImportResult(
                importedCount = seedRes.plantsNewlyAdded + seedRes.plantsMatchedEnriched + seedRes.nurseriesProcessed,
                skippedDuplicates = seedRes.nurseriesSkippedVerified,
                errors = seedRes.errors
            )
        }
        return try {
            val arr = JSONArray(trimmed)
            val existingById = dao.getAllPlantsOnce().associateBy { it.plantId }.toMutableMap()
            val toUpsert = mutableListOf<PlantKingdomEntity>()
            val errors = mutableListOf<String>()
            var skipped = 0

            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val nameEn = obj.optString("nameEn", obj.optString("name", "")).trim()
                val sciName = obj.optString("scientificName", "").trim()
                val plantId = sanitizeId(obj.optString("plantId", nameEn))
                if (plantId.isBlank() || nameEn.isBlank()) {
                    errors.add("Item ${i + 1}: missing plantId or nameEn")
                    continue
                }
                val normSci = normalizeScientificName(sciName)
                val normEn = normalizeName(nameEn)
                val isDuplicate = existingById.values.any {
                    it.plantId == plantId ||
                        (normSci.isNotBlank() && normalizeScientificName(it.scientificName) == normSci) ||
                        normalizeName(it.nameEn) == normEn
                }
                if (isDuplicate) {
                    skipped++
                    continue
                }
                val entry = PlantKingdomEntry(
                    plantId = plantId,
                    nameEn = nameEn,
                    nameBn = obj.optString("nameBn", nameEn).trim(),
                    nameHi = obj.optString("nameHi", nameEn).trim(),
                    scientificName = sciName,
                    primaryGroup = PlantKingdomGroup.fromCode(obj.optString("primaryGroup", "COMMON_FLOWERS")).code,
                    filterTagsCsv = obj.optString("filterTagsCsv", obj.optString("filterTags", "")),
                    seasonalTagsCsv = obj.optString("seasonalTagsCsv", ""),
                    descriptionEn = obj.optString("descriptionEn", ""),
                    descriptionBn = obj.optString("descriptionBn", ""),
                    descriptionHi = obj.optString("descriptionHi", ""),
                    visualAssetKey = PlantPhotoRegistry.getVisualAssetKey(plantId),
                    customPhotoUri = obj.optString("customPhotoUri", ""),
                    climate = obj.optString("climate", ""),
                    temperatureRange = obj.optString("temperatureRange", ""),
                    soilRequirements = obj.optString("soilRequirements", ""),
                    sunlight = obj.optString("sunlight", ""),
                    wateringFrequency = obj.optString("wateringFrequency", ""),
                    fertilizerGuidance = obj.optString("fertilizerGuidance", ""),
                    propagationMethod = obj.optString("propagationMethod", ""),
                    floweringOrFruitingSeason = obj.optString("floweringOrFruitingSeason", ""),
                    growthSize = obj.optString("growthSize", ""),
                    indoorOutdoorSuitability = obj.optString("indoorOutdoorSuitability", ""),
                    plainsHillSuitability = obj.optString("plainsHillSuitability", ""),
                    commonPestsDiseases = obj.optString("commonPestsDiseases", ""),
                    careInstructionsEn = obj.optString("careInstructionsEn", ""),
                    careInstructionsBn = obj.optString("careInstructionsBn", ""),
                    careInstructionsHi = obj.optString("careInstructionsHi", ""),
                    isRare = obj.optBoolean("isRare", false),
                    isVerifiedEntry = true,
                    sourceReference = obj.optString("sourceReference", "Admin JSON Bulk Import"),
                    lastVerifiedDate = todayIso(),
                    updatedAtMillis = System.currentTimeMillis()
                )
                val entity = PlantKingdomEntity.fromModel(entry)
                toUpsert.add(entity)
                existingById[plantId] = entity
            }
            if (toUpsert.isNotEmpty()) {
                dao.upsertPlants(toUpsert)
                toUpsert.forEach { syncPlantToFirestore(it.toModel()) }
            }
            BulkImportResult(toUpsert.size, skipped, errors)
        } catch (e: Exception) {
            BulkImportResult(0, 0, listOf(e.message ?: "Invalid JSON format"))
        }
    }

    suspend fun exportPlantsToJson(): String {
        val arr = JSONArray()
        for (entity in dao.getAllPlantsOnce()) {
            val obj = JSONObject()
            obj.put("plantId", entity.plantId)
            obj.put("nameEn", entity.nameEn)
            obj.put("nameBn", entity.nameBn)
            obj.put("nameHi", entity.nameHi)
            obj.put("scientificName", entity.scientificName)
            obj.put("primaryGroup", entity.primaryGroup)
            obj.put("filterTagsCsv", entity.filterTagsCsv)
            obj.put("climate", entity.climate)
            obj.put("temperatureRange", entity.temperatureRange)
            obj.put("sunlight", entity.sunlight)
            obj.put("wateringFrequency", entity.wateringFrequency)
            obj.put("lastVerifiedDate", entity.lastVerifiedDate)
            arr.put(obj)
        }
        return arr.toString(2)
    }

    // ------------------------------------------------------------------
    // West Bengal & Regional Nursery Directory Admin & Verification
    // ------------------------------------------------------------------

    suspend fun addOrUpdateNursery(entry: WbNurseryDirectoryEntry): Result<Unit> {
        return try {
            if (entry.businessName.isBlank()) {
                return Result.failure(IllegalArgumentException("Nursery business name is required"))
            }
            val finalId = entry.nurseryId.ifBlank {
                "wb_nurs_${sanitizeId(entry.businessName)}_${System.currentTimeMillis() % 10000}"
            }
            val cleaned = entry.copy(
                nurseryId = finalId,
                businessName = entry.businessName.trim(),
                businessNameBn = entry.businessNameBn.ifBlank { entry.businessName }.trim(),
                businessNameHi = entry.businessNameHi.ifBlank { entry.businessName }.trim(),
                lastVerifiedDate = entry.lastVerifiedDate.ifBlank { todayIso() },
                updatedAtMillis = System.currentTimeMillis()
            )
            dao.upsertNursery(WbNurseryDirectoryEntity.fromModel(cleaned))
            syncNurseryToFirestore(cleaned)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyNurseryRecord(
        nurseryId: String,
        ownerName: String?,
        phone: String?,
        whatsapp: String?,
        fullAddress: String?,
        districtCode: String,
        locality: String?,
        latitude: Double?,
        longitude: Double?,
        openingHours: String?,
        websiteOrSocial: String?,
        categoriesCsv: String,
        supportsWholesale: Boolean?,
        supportsRetail: Boolean?,
        deliveryAvailable: Boolean?,
        verifierNote: String
    ): Result<Unit> {
        val existing = dao.getNurseryById(nurseryId)?.toModel()
            ?: return Result.failure(NoSuchElementException("Nursery $nurseryId not found"))

        val updated = existing.copy(
            ownerName = ownerName?.trim()?.ifBlank { null },
            phone = phone?.trim()?.ifBlank { null },
            whatsapp = whatsapp?.trim()?.ifBlank { null },
            fullAddress = fullAddress?.trim()?.ifBlank { null },
            districtCode = WbNurseryDistrict.fromCode(districtCode).code,
            locality = locality?.trim()?.ifBlank { null },
            latitude = latitude,
            longitude = longitude,
            openingHours = openingHours?.trim()?.ifBlank { null },
            websiteOrSocial = websiteOrSocial?.trim()?.ifBlank { null },
            availablePlantCategoriesCsv = categoriesCsv.trim(),
            supportsWholesale = supportsWholesale,
            supportsRetail = supportsRetail,
            deliveryAvailable = deliveryAvailable,
            verificationStatus = NurseryVerificationStatus.ADMIN_VERIFIED,
            isVerified = true,
            sourceReference = verifierNote.ifBlank { "Verified by Krishak Sathi Admin" },
            lastVerifiedDate = todayIso(),
            updatedAtMillis = System.currentTimeMillis()
        )
        return addOrUpdateNursery(updated)
    }

    /**
     * Only approved nursery partners (`APPROVED` application or verified partner directory record)
     * may publish commercial plant listings into the Marketplace.
     */
    fun canPublishPartnerListing(
        userId: String,
        applications: List<NurseryApplication>,
        isAdmin: Boolean
    ): Boolean {
        if (isAdmin) return true
        return applications.any {
            it.userId == userId && it.status.equals("APPROVED", ignoreCase = true)
        }
    }

    /**
     * Loads and processes the bundled uploaded seed JSON asset
     * (`assets/Krishak_Sathi_Firebase_Seed_Data_3e235bd0.json`), mapping both the 37 pending
     * nurseries and 80 starter plants into Room and syncing to Cloud Firestore when signed in.
     */
    suspend fun importBundledSeedAsset(pushToFirestore: Boolean = true): FirebaseSeedImportResult {
        return try {
            val jsonText = context.assets.open(BUNDLED_SEED_ASSET_NAME).bufferedReader().use { it.readText() }
            importFirebaseSeedBundleJson(jsonText, pushToFirestore = pushToFirestore)
        } catch (e: Exception) {
            FirebaseSeedImportResult(
                nurseriesProcessed = 0,
                nurseriesSkippedVerified = 0,
                plantsMatchedEnriched = 0,
                plantsNewlyAdded = 0,
                firestoreSyncedDocs = 0,
                cloudPushSucceeded = false,
                message = "Failed to load bundled seed asset: ${e.message}",
                errors = listOf(e.message ?: "Asset load error")
            )
        }
    }

    /**
     * Full structured pipeline for Firebase Seed Data JSON (`{ "project": "...", "nurseries": [...], "plants": [...] }`):
     * - Zero-fabrication: empty strings stay null on unverified nurseries; never downgrades already-verified nurseries.
     * - Deduplication: matches plants against the 124-species catalog by normalized scientificName or English name.
     * - Preserves richer existing multilingual botanical guides while filling any missing BN/HI/climate metadata.
     * - Pushes merged records cleanly to Cloud Firestore (`plant_kingdom` & `wb_nurseries`) in WriteBatches.
     */
    suspend fun importFirebaseSeedBundleJson(
        jsonText: String,
        pushToFirestore: Boolean = true
    ): FirebaseSeedImportResult {
        return try {
            ensureSeeded()
            val root = JSONObject(jsonText.trim())
            val errors = mutableListOf<String>()

            // 1. Process nurseries array
            val existingNurseries = dao.getAllNurseriesOnce().map { it.toModel() }
            val existingByNurseryId = existingNurseries.associateBy { it.nurseryId }.toMutableMap()
            val existingByBusinessName = existingNurseries.associateBy { normalizeName(it.businessName) }.toMutableMap()

            val nurseriesToUpsert = mutableListOf<WbNurseryDirectoryEntry>()
            var nurseriesSkippedVerified = 0
            val nurseryArr = root.optJSONArray("nurseries") ?: JSONArray()

            for (i in 0 until nurseryArr.length()) {
                val obj = nurseryArr.optJSONObject(i) ?: continue
                val rawBusinessName = obj.optString("business_name", obj.optString("businessName", "")).trim()
                if (rawBusinessName.isBlank()) {
                    errors.add("Nursery #${i + 1}: missing business_name")
                    continue
                }
                val rawJsonId = sanitizeId(obj.optString("nursery_id", obj.optString("nurseryId", rawBusinessName)))
                val matchedExisting = existingByNurseryId[rawJsonId]
                    ?: existingByBusinessName[normalizeName(rawBusinessName)]

                if (matchedExisting != null && matchedExisting.isVerified) {
                    // Never downgrade or overwrite already-verified partner/admin nursery details
                    nurseriesSkippedVerified++
                    continue
                }

                val suppliedDistrict = obj.optString("district", "").trim()
                val districtCode = when {
                    suppliedDistrict.isNotBlank() -> WbNurseryDistrict.fromCode(suppliedDistrict).code
                    matchedExisting != null -> matchedExisting.districtCode
                    else -> WbNurseryDistrict.OTHER_WB.code
                }
                val suppliedLocality = obj.optString("locality", "").trim().ifBlank { null }
                    ?: matchedExisting?.locality
                val suppliedAddress = obj.optString("full_address", "").trim().ifBlank { null }
                    ?: matchedExisting?.fullAddress
                val suppliedPhone = obj.optString("phone", "").trim().ifBlank { null }
                    ?: matchedExisting?.phone
                val suppliedWhatsapp = obj.optString("whatsapp", "").trim().ifBlank { null }
                    ?: matchedExisting?.whatsapp
                val suppliedOwner = obj.optString("owner_name", "").trim().ifBlank { null }
                    ?: matchedExisting?.ownerName
                val suppliedHours = obj.optString("opening_hours", "").trim().ifBlank { null }
                    ?: matchedExisting?.openingHours
                val suppliedWebsite = obj.optString("website", "").trim()
                    .ifBlank { obj.optString("social_media", "").trim() }
                    .ifBlank { null }
                    ?: matchedExisting?.websiteOrSocial
                val suppliedLat = obj.optString("latitude", "").trim().toDoubleOrNull()
                    ?: matchedExisting?.latitude
                val suppliedLng = obj.optString("longitude", "").trim().toDoubleOrNull()
                    ?: matchedExisting?.longitude
                val suppliedCategories = obj.optString("plant_categories", "").trim()
                    .ifBlank { matchedExisting?.availablePlantCategoriesCsv.orEmpty() }
                val rawSource = obj.optString("source_reference", "").trim()
                val consent = obj.optString("owner_consent_status", "").trim()
                val combinedSource = buildString {
                    append(rawSource.ifBlank { matchedExisting?.sourceReference ?: "Preliminary Seed Directory Import" })
                    if (consent.isNotBlank() && !contains(consent)) {
                        append(" [Consent: ").append(consent).append("]")
                    }
                }.take(290)
                val lastVerified = obj.optString("last_verified_date", "").trim()
                    .ifBlank { matchedExisting?.lastVerifiedDate ?: todayIso() }
                val rawVerifStatus = obj.optString("verification_status", NurseryVerificationStatus.PENDING_VERIFICATION)
                    .trim().uppercase()
                val safeStatus = when (rawVerifStatus) {
                    NurseryVerificationStatus.ADMIN_VERIFIED,
                    NurseryVerificationStatus.VERIFIED_PARTNER,
                    NurseryVerificationStatus.PENDING_VERIFICATION -> rawVerifStatus
                    else -> NurseryVerificationStatus.PENDING_VERIFICATION
                }

                val targetId = matchedExisting?.nurseryId ?: rawJsonId
                val merged = WbNurseryDirectoryEntry(
                    nurseryId = targetId,
                    businessName = rawBusinessName,
                    businessNameBn = matchedExisting?.businessNameBn?.ifBlank { rawBusinessName } ?: rawBusinessName,
                    businessNameHi = matchedExisting?.businessNameHi?.ifBlank { rawBusinessName } ?: rawBusinessName,
                    ownerName = suppliedOwner,
                    fullAddress = suppliedAddress,
                    districtCode = districtCode,
                    locality = suppliedLocality,
                    state = "West Bengal",
                    phone = suppliedPhone,
                    whatsapp = suppliedWhatsapp,
                    latitude = suppliedLat,
                    longitude = suppliedLng,
                    websiteOrSocial = suppliedWebsite,
                    openingHours = suppliedHours,
                    nurseryPhotosCsv = matchedExisting?.nurseryPhotosCsv.orEmpty(),
                    availablePlantCategoriesCsv = suppliedCategories,
                    availablePlantIdsCsv = matchedExisting?.availablePlantIdsCsv.orEmpty(),
                    supportsWholesale = matchedExisting?.supportsWholesale,
                    supportsRetail = matchedExisting?.supportsRetail,
                    deliveryAvailable = matchedExisting?.deliveryAvailable,
                    descriptionEn = matchedExisting?.descriptionEn?.ifBlank {
                        obj.optString("notes", "Publicly listed nursery entry awaiting owner verification.")
                    } ?: obj.optString("notes", "Publicly listed nursery entry awaiting owner verification."),
                    descriptionBn = matchedExisting?.descriptionBn.orEmpty(),
                    descriptionHi = matchedExisting?.descriptionHi.orEmpty(),
                    verificationStatus = safeStatus,
                    isVerified = safeStatus != NurseryVerificationStatus.PENDING_VERIFICATION,
                    sourceReference = combinedSource,
                    lastVerifiedDate = lastVerified,
                    linkedSellerUserId = matchedExisting?.linkedSellerUserId,
                    updatedAtMillis = System.currentTimeMillis()
                )
                nurseriesToUpsert.add(merged)
                existingByNurseryId[targetId] = merged
                existingByBusinessName[normalizeName(rawBusinessName)] = merged
            }
            if (nurseriesToUpsert.isNotEmpty()) {
                dao.upsertNurseries(nurseriesToUpsert.map { WbNurseryDirectoryEntity.fromModel(it) })
            }

            // 2. Process plants array
            val existingPlants = dao.getAllPlantsOnce().map { it.toModel() }
            val existingByPlantId = existingPlants.associateBy { it.plantId }.toMutableMap()
            val existingByScientific = existingPlants
                .filter { it.scientificName.isNotBlank() }
                .associateBy { normalizeScientificName(it.scientificName) }
                .toMutableMap()
            val existingByEn = existingPlants.associateBy { normalizeName(it.nameEn) }.toMutableMap()
            val existingByBaseEn = existingPlants
                .associateBy { normalizeName(it.nameEn.substringBefore("(")) }
                .toMutableMap()
            val existingByGenus = existingPlants
                .filter { it.scientificName.isNotBlank() }
                .groupBy { normalizeScientificName(it.scientificName).substringBefore(" ") }
                .mapValues { it.value.first() }

            val plantsToUpsert = mutableListOf<PlantKingdomEntry>()
            var plantsMatchedEnriched = 0
            var plantsNewlyAdded = 0
            val plantArr = root.optJSONArray("plants") ?: JSONArray()

            for (i in 0 until plantArr.length()) {
                val obj = plantArr.optJSONObject(i) ?: continue
                val nameEn = obj.optString("name_en", obj.optString("nameEn", "")).trim()
                if (nameEn.isBlank()) {
                    errors.add("Plant #${i + 1}: missing name_en")
                    continue
                }
                val nameBn = obj.optString("name_bn", obj.optString("nameBn", "")).trim()
                val nameHi = obj.optString("name_hi", obj.optString("nameHi", "")).trim()
                val sciName = obj.optString("scientific_name", obj.optString("scientificName", "")).trim()
                val rawCat = obj.optString("category", obj.optString("primaryGroup", "flower")).trim().lowercase()
                val mappedGroup = mapSeedCategoryToGroup(rawCat)
                val seedTag = mapSeedCategoryToFilterTag(rawCat)
                val jsonId = sanitizeId(obj.optString("plant_id", obj.optString("plantId", nameEn)))
                val nameSlug = sanitizeId(nameEn)
                val normSci = normalizeScientificName(sciName)
                val isGenusOnly = sciName.contains("spp", ignoreCase = true)

                val matched = existingByPlantId[jsonId]
                    ?: existingByPlantId[nameSlug]
                    ?: existingByScientific[normSci]
                    ?: existingByEn[normalizeName(nameEn)]
                    ?: existingByBaseEn[normalizeName(nameEn)]
                    ?: if (isGenusOnly) existingByGenus[normSci.substringBefore(" ")] else null

                val imageUrl = obj.optString("image_url", obj.optString("customPhotoUri", "")).trim()
                val climateNote = obj.optString("climate_suitability_note", obj.optString("climate", "")).trim()
                val descBn = obj.optString("description_bn", "").trim()
                val careBn = obj.optString("care_instructions_bn", "").trim()
                val sunlight = obj.optString("sunlight", "").trim()
                val watering = obj.optString("watering", "").trim()
                val soil = obj.optString("soil", "").trim()
                val season = obj.optString("flowering_or_fruiting_season", "").trim()
                val sourceRef = obj.optString("source_reference", "").trim()
                val lastVerif = obj.optString("last_verified_date", "").trim()
                val pubStatus = obj.optString("publication_status", "").trim().uppercase()

                val mergedPlant = if (matched != null) {
                    plantsMatchedEnriched++
                    val combinedTags = (matched.filterTagsCsv.split(",") + seedTag)
                        .map { it.trim() }.filter { it.isNotBlank() }.distinct().joinToString(",")
                    matched.copy(
                        nameBn = matched.nameBn.ifBlank { nameBn.ifBlank { nameEn } },
                        nameHi = matched.nameHi.ifBlank { nameHi.ifBlank { nameEn } },
                        scientificName = matched.scientificName.ifBlank { sciName },
                        filterTagsCsv = combinedTags,
                        customPhotoUri = matched.customPhotoUri.ifBlank { imageUrl },
                        climate = matched.climate.ifBlank { climateNote },
                        sunlight = matched.sunlight.ifBlank { sunlight },
                        wateringFrequency = matched.wateringFrequency.ifBlank { watering },
                        soilRequirements = matched.soilRequirements.ifBlank { soil },
                        floweringOrFruitingSeason = matched.floweringOrFruitingSeason.ifBlank { season },
                        descriptionBn = matched.descriptionBn.ifBlank { descBn },
                        careInstructionsBn = matched.careInstructionsBn.ifBlank { careBn },
                        sourceReference = if (sourceRef.isNotBlank() && !matched.sourceReference.contains("Seed")) {
                            "${matched.sourceReference} • Firebase Seed Sync".take(290)
                        } else {
                            matched.sourceReference.ifBlank { "Krishak Sathi Seed Import" }
                        },
                        lastVerifiedDate = lastVerif.ifBlank { matched.lastVerifiedDate },
                        updatedAtMillis = System.currentTimeMillis()
                    )
                } else {
                    plantsNewlyAdded++
                    val newId = sanitizeId(nameEn).take(64)
                    PlantKingdomEntry(
                        plantId = newId,
                        nameEn = nameEn,
                        nameBn = nameBn.ifBlank { nameEn },
                        nameHi = nameHi.ifBlank { nameEn },
                        scientificName = sciName,
                        primaryGroup = mappedGroup.code,
                        filterTagsCsv = seedTag,
                        descriptionEn = obj.optString("plant_type", "").trim()
                            .ifBlank { "$nameEn starter botanical entry imported from Firebase seed bundle." },
                        descriptionBn = descBn,
                        visualAssetKey = PlantPhotoRegistry.getVisualAssetKey(newId),
                        customPhotoUri = imageUrl,
                        climate = climateNote,
                        soilRequirements = soil,
                        sunlight = sunlight,
                        wateringFrequency = watering,
                        floweringOrFruitingSeason = season,
                        careInstructionsBn = careBn,
                        isRare = rawCat == "hill_flower",
                        isVerifiedEntry = pubStatus != "DRAFT_REVIEW_REQUIRED",
                        sourceReference = sourceRef.ifBlank { "Firebase Seed Import ($pubStatus)" }.take(290),
                        lastVerifiedDate = lastVerif.ifBlank { todayIso() },
                        updatedAtMillis = System.currentTimeMillis()
                    )
                }

                plantsToUpsert.add(mergedPlant)
                existingByPlantId[mergedPlant.plantId] = mergedPlant
                if (mergedPlant.scientificName.isNotBlank()) {
                    existingByScientific[normalizeScientificName(mergedPlant.scientificName)] = mergedPlant
                }
                existingByEn[normalizeName(mergedPlant.nameEn)] = mergedPlant
            }

            if (plantsToUpsert.isNotEmpty()) {
                dao.upsertPlants(plantsToUpsert.map { PlantKingdomEntity.fromModel(it) })
            }

            var syncedCount = 0
            var cloudOk = false
            if (pushToFirestore) {
                val pushSummary = pushRecordsToFirestore(plantsToUpsert, nurseriesToUpsert)
                syncedCount = pushSummary.plantsCount + pushSummary.nurseriesCount
                cloudOk = pushSummary.cloudSuccess
            }

            val summaryMsg = buildString {
                append("Nurseries: ${nurseriesToUpsert.size} processed")
                if (nurseriesSkippedVerified > 0) append(" ($nurseriesSkippedVerified verified preserved)")
                append(" • Plants: $plantsMatchedEnriched enriched, $plantsNewlyAdded added")
                if (pushToFirestore) {
                    append(if (cloudOk) " • Cloud synced ($syncedCount docs)" else " • Saved locally (sign in as Admin for Cloud push)")
                }
            }

            FirebaseSeedImportResult(
                nurseriesProcessed = nurseriesToUpsert.size,
                nurseriesSkippedVerified = nurseriesSkippedVerified,
                plantsMatchedEnriched = plantsMatchedEnriched,
                plantsNewlyAdded = plantsNewlyAdded,
                firestoreSyncedDocs = syncedCount,
                cloudPushSucceeded = cloudOk,
                message = summaryMsg,
                errors = errors
            )
        } catch (e: Exception) {
            FirebaseSeedImportResult(
                nurseriesProcessed = 0,
                nurseriesSkippedVerified = 0,
                plantsMatchedEnriched = 0,
                plantsNewlyAdded = 0,
                firestoreSyncedDocs = 0,
                cloudPushSucceeded = false,
                message = "Seed bundle import failed: ${e.message}",
                errors = listOf(e.message ?: "Unknown JSON parsing error")
            )
        }
    }

    /**
     * Pushes the entire Room Plant Kingdom + WB Nursery Directory to Cloud Firestore in batched writes.
     * Firestore rules enforce `isAdmin()` (verified admin email). Non-admin users receive a clean message.
     */
    suspend fun syncAllCatalogAndDirectoryToFirestore(): FirebaseSyncSummary {
        ensureSeeded()
        val plants = dao.getAllPlantsOnce().map { it.toModel() }
        val nurseries = dao.getAllNurseriesOnce().map { it.toModel() }
        return pushRecordsToFirestore(plants, nurseries)
    }

    /**
     * Pulls admin-updated records from Cloud Firestore (`plant_kingdom` & `wb_nurseries`) into Room.
     */
    suspend fun refreshFromFirestore(): FirebaseSyncSummary {
        val db = firestore
        val user = try { Firebase.auth.currentUser } catch (_: Exception) { null }
        if (db == null || user == null) {
            return FirebaseSyncSummary(0, 0, false, "Sign in with Google to pull cloud updates.")
        }
        return try {
            val plantSnap = db.collection("plant_kingdom").get().await()
            val nurserySnap = db.collection("wb_nurseries").get().await()

            val pulledPlants = plantSnap.documents.mapNotNull { d ->
                val plantId = d.getString("plantId")?.trim().orEmpty().ifBlank { d.id }
                val nameEn = d.getString("nameEn")?.trim().orEmpty()
                if (nameEn.isBlank()) return@mapNotNull null
                PlantKingdomEntry(
                    plantId = plantId,
                    nameEn = nameEn,
                    nameBn = d.getString("nameBn") ?: nameEn,
                    nameHi = d.getString("nameHi") ?: nameEn,
                    scientificName = d.getString("scientificName") ?: "",
                    primaryGroup = d.getString("primaryGroup") ?: PlantKingdomGroup.COMMON_FLOWERS.code,
                    secondaryGroupsCsv = d.getString("secondaryGroupsCsv") ?: "",
                    filterTagsCsv = d.getString("filterTagsCsv") ?: "",
                    seasonalTagsCsv = d.getString("seasonalTagsCsv") ?: "",
                    descriptionEn = d.getString("descriptionEn") ?: "",
                    descriptionBn = d.getString("descriptionBn") ?: "",
                    descriptionHi = d.getString("descriptionHi") ?: "",
                    visualAssetKey = d.getString("visualAssetKey")?.ifBlank { PlantPhotoRegistry.getVisualAssetKey(plantId) }
                        ?: PlantPhotoRegistry.getVisualAssetKey(plantId),
                    customPhotoUri = d.getString("customPhotoUri") ?: "",
                    climate = d.getString("climate") ?: "",
                    temperatureRange = d.getString("temperatureRange") ?: "",
                    soilRequirements = d.getString("soilRequirements") ?: "",
                    sunlight = d.getString("sunlight") ?: "",
                    wateringFrequency = d.getString("wateringFrequency") ?: "",
                    fertilizerGuidance = d.getString("fertilizerGuidance") ?: "",
                    propagationMethod = d.getString("propagationMethod") ?: "",
                    floweringOrFruitingSeason = d.getString("floweringOrFruitingSeason") ?: "",
                    growthSize = d.getString("growthSize") ?: "",
                    indoorOutdoorSuitability = d.getString("indoorOutdoorSuitability") ?: "",
                    plainsHillSuitability = d.getString("plainsHillSuitability") ?: "",
                    commonPestsDiseases = d.getString("commonPestsDiseases") ?: "",
                    careInstructionsEn = d.getString("careInstructionsEn") ?: "",
                    careInstructionsBn = d.getString("careInstructionsBn") ?: "",
                    careInstructionsHi = d.getString("careInstructionsHi") ?: "",
                    verifiedNurseryIdsCsv = d.getString("verifiedNurseryIdsCsv") ?: "",
                    isRare = d.getBoolean("isRare") ?: false,
                    isVerifiedEntry = d.getBoolean("isVerifiedEntry") ?: true,
                    publicationStatus = d.getString("publicationStatus") ?: if (d.getBoolean("isVerifiedEntry") == false) "DRAFT_REVIEW_REQUIRED" else "PUBLISHED",
                    sourceReference = d.getString("sourceReference") ?: "Firebase Cloud Sync",
                    lastVerifiedDate = d.getString("lastVerifiedDate") ?: todayIso(),
                    updatedAtMillis = System.currentTimeMillis()
                )
            }
            if (pulledPlants.isNotEmpty()) {
                dao.upsertPlants(pulledPlants.map { PlantKingdomEntity.fromModel(it) })
            }

            val pulledNurseries = nurserySnap.documents.mapNotNull { d ->
                val nurseryId = d.getString("nurseryId")?.trim().orEmpty().ifBlank { d.id }
                val businessName = d.getString("businessName")?.trim().orEmpty()
                if (businessName.isBlank()) return@mapNotNull null
                WbNurseryDirectoryEntry(
                    nurseryId = nurseryId,
                    businessName = businessName,
                    businessNameBn = d.getString("businessNameBn") ?: businessName,
                    businessNameHi = d.getString("businessNameHi") ?: businessName,
                    ownerName = d.getString("ownerName"),
                    fullAddress = d.getString("fullAddress"),
                    districtCode = d.getString("districtCode") ?: WbNurseryDistrict.OTHER_WB.code,
                    locality = d.getString("locality"),
                    state = d.getString("state") ?: "West Bengal",
                    phone = d.getString("phone"),
                    whatsapp = d.getString("whatsapp"),
                    latitude = d.getDouble("latitude"),
                    longitude = d.getDouble("longitude"),
                    websiteOrSocial = d.getString("websiteOrSocial"),
                    openingHours = d.getString("openingHours"),
                    availablePlantCategoriesCsv = d.getString("availablePlantCategoriesCsv") ?: "",
                    availablePlantIdsCsv = d.getString("availablePlantIdsCsv") ?: "",
                    supportsWholesale = d.getBoolean("supportsWholesale"),
                    supportsRetail = d.getBoolean("supportsRetail"),
                    deliveryAvailable = d.getBoolean("deliveryAvailable"),
                    verificationStatus = d.getString("verificationStatus") ?: NurseryVerificationStatus.PENDING_VERIFICATION,
                    ownerConsentStatus = d.getString("ownerConsentStatus") ?: "NOT_CONFIRMED",
                    isVerified = d.getBoolean("isVerified") ?: false,
                    sourceReference = d.getString("sourceReference") ?: "Firebase Cloud Sync",
                    lastVerifiedDate = d.getString("lastVerifiedDate") ?: todayIso(),
                    notes = d.getString("notes") ?: "",
                    updatedAtMillis = System.currentTimeMillis()
                )
            }
            if (pulledNurseries.isNotEmpty()) {
                dao.upsertNurseries(pulledNurseries.map { WbNurseryDirectoryEntity.fromModel(it) })
            }

            FirebaseSyncSummary(
                plantsCount = pulledPlants.size,
                nurseriesCount = pulledNurseries.size,
                cloudSuccess = true,
                message = "Pulled ${pulledPlants.size} plants & ${pulledNurseries.size} nurseries from Firebase."
            )
        } catch (e: Exception) {
            FirebaseSyncSummary(0, 0, false, "Cloud pull failed: ${e.message}")
        }
    }

    private suspend fun pushRecordsToFirestore(
        plants: List<PlantKingdomEntry>,
        nurseries: List<WbNurseryDirectoryEntry>
    ): FirebaseSyncSummary {
        val db = firestore
        val user = try { Firebase.auth.currentUser } catch (_: Exception) { null }
        if (db == null || user == null) {
            return FirebaseSyncSummary(
                plantsCount = 0,
                nurseriesCount = 0,
                cloudSuccess = false,
                message = "Saved in offline Room DB. Sign in as Admin to push to Cloud Firestore."
            )
        }
        return try {
            val allWrites = mutableListOf<Pair<String, Pair<String, Map<String, Any?>>>>()
            plants.forEach { plant ->
                allWrites.add("plant_kingdom" to (plant.plantId to toPlantFirestoreMap(plant)))
            }
            nurseries.forEach { nursery ->
                allWrites.add("wb_nurseries" to (nursery.nurseryId to toNurseryFirestoreMap(nursery)))
            }

            allWrites.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { (col, idPayload) ->
                    batch.set(db.collection(col).document(idPayload.first), idPayload.second)
                }
                batch.commit().await()
            }

            FirebaseSyncSummary(
                plantsCount = plants.size,
                nurseriesCount = nurseries.size,
                cloudSuccess = true,
                message = "Synced ${plants.size} plants & ${nurseries.size} WB nurseries to Firebase Firestore!"
            )
        } catch (e: Exception) {
            Log.w("PlantKingdomRepo", "Firestore batch sync failed: ${e.message}")
            FirebaseSyncSummary(
                plantsCount = 0,
                nurseriesCount = 0,
                cloudSuccess = false,
                message = "Cloud push skipped (${e.message ?: "Admin sign-in required"}). Local DB updated."
            )
        }
    }

    private fun toPlantFirestoreMap(entry: PlantKingdomEntry): Map<String, Any> = hashMapOf(
        "plantId" to entry.plantId,
        "nameEn" to entry.nameEn.take(200),
        "nameBn" to entry.nameBn.take(200),
        "nameHi" to entry.nameHi.take(200),
        "scientificName" to entry.scientificName.take(200),
        "primaryGroup" to entry.primaryGroup,
        "secondaryGroupsCsv" to entry.secondaryGroupsCsv.take(400),
        "filterTagsCsv" to entry.filterTagsCsv.take(400),
        "seasonalTagsCsv" to entry.seasonalTagsCsv.take(400),
        "descriptionEn" to entry.descriptionEn.take(2000),
        "descriptionBn" to entry.descriptionBn.take(2000),
        "descriptionHi" to entry.descriptionHi.take(2000),
        "visualAssetKey" to entry.visualAssetKey.take(160),
        "customPhotoUri" to entry.customPhotoUri.take(1000),
        "climate" to entry.climate.take(400),
        "temperatureRange" to entry.temperatureRange.take(200),
        "soilRequirements" to entry.soilRequirements.take(600),
        "sunlight" to entry.sunlight.take(300),
        "wateringFrequency" to entry.wateringFrequency.take(300),
        "fertilizerGuidance" to entry.fertilizerGuidance.take(600),
        "propagationMethod" to entry.propagationMethod.take(400),
        "floweringOrFruitingSeason" to entry.floweringOrFruitingSeason.take(300),
        "growthSize" to entry.growthSize.take(200),
        "indoorOutdoorSuitability" to entry.indoorOutdoorSuitability.take(300),
        "plainsHillSuitability" to entry.plainsHillSuitability.take(300),
        "commonPestsDiseases" to entry.commonPestsDiseases.take(500),
        "careInstructionsEn" to entry.careInstructionsEn.take(2000),
        "careInstructionsBn" to entry.careInstructionsBn.take(2000),
        "careInstructionsHi" to entry.careInstructionsHi.take(2000),
        "verifiedNurseryIdsCsv" to entry.verifiedNurseryIdsCsv.take(1000),
        "isRare" to entry.isRare,
        "isVerifiedEntry" to entry.isVerifiedEntry,
        "publicationStatus" to entry.publicationStatus.take(40),
        "sourceReference" to entry.sourceReference.ifBlank { "Krishak Sathi Botanical Library" }.take(300),
        "lastVerifiedDate" to entry.lastVerifiedDate.ifBlank { todayIso() }.take(40),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    private fun toNurseryFirestoreMap(entry: WbNurseryDirectoryEntry): Map<String, Any?> = hashMapOf(
        "nurseryId" to entry.nurseryId,
        "businessName" to entry.businessName.take(200),
        "businessNameBn" to entry.businessNameBn.take(200),
        "businessNameHi" to entry.businessNameHi.take(200),
        "ownerName" to entry.ownerName?.take(150),
        "fullAddress" to entry.fullAddress?.take(500),
        "districtCode" to entry.districtCode.ifBlank { WbNurseryDistrict.OTHER_WB.code }.take(60),
        "locality" to entry.locality?.take(150),
        "state" to entry.state.ifBlank { "West Bengal" }.take(80),
        "phone" to entry.phone?.take(40),
        "whatsapp" to entry.whatsapp?.take(40),
        "latitude" to entry.latitude,
        "longitude" to entry.longitude,
        "websiteOrSocial" to entry.websiteOrSocial?.take(500),
        "openingHours" to entry.openingHours?.take(200),
        "availablePlantCategoriesCsv" to entry.availablePlantCategoriesCsv.take(600),
        "availablePlantIdsCsv" to entry.availablePlantIdsCsv.take(1500),
        "supportsWholesale" to entry.supportsWholesale,
        "supportsRetail" to entry.supportsRetail,
        "deliveryAvailable" to entry.deliveryAvailable,
        "verificationStatus" to entry.verificationStatus,
        "ownerConsentStatus" to entry.ownerConsentStatus.take(60),
        "isVerified" to entry.isVerified,
        "sourceReference" to entry.sourceReference.take(300),
        "lastVerifiedDate" to entry.lastVerifiedDate.ifBlank { todayIso() }.take(40),
        "notes" to entry.notes.take(800),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    suspend fun previewBundledCsvImport(): CsvImportPreviewReport {
        val nurseryCsv = try {
            context.assets.open(BUNDLED_NURSERY_CSV_ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (_: Exception) { "" }
        val plantCsv = try {
            context.assets.open(BUNDLED_PLANT_CSV_ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (_: Exception) { "" }
        return previewCsvImport(nurseryCsv, plantCsv)
    }

    suspend fun previewCsvImport(
        nurseryCsvText: String,
        plantCsvText: String
    ): CsvImportPreviewReport {
        val existingNurseries = dao.getAllNurseriesOnce().map { it.toModel() }
        val existingPlants = dao.getAllPlantsOnce().map { it.toModel() }
        return PlantKingdomCsvImporter.buildDryRunPreview(
            nurseryCsvText = nurseryCsvText,
            plantCsvText = plantCsvText,
            existingNurseries = existingNurseries,
            existingPlants = existingPlants
        )
    }

    suspend fun executeConfirmedCsvImport(
        preview: CsvImportPreviewReport,
        adminConfirmed: Boolean,
        pushToFirestore: Boolean = true,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): CsvImportExecutionResult {
        if (!adminConfirmed) {
            return CsvImportExecutionResult(
                adminConfirmed = false,
                nurseriesWritten = 0,
                nurseriesSkippedVerified = 0,
                plantsWritten = 0,
                plantsDraftPendingReview = 0,
                firestoreSyncedDocs = 0,
                cloudPushSucceeded = false,
                errorCount = preview.totalErrors,
                errorLog = listOf("Blocked: explicit Admin confirmation checkbox is required before writing."),
                summaryMessage = "Import cancelled: Admin confirmation required."
            )
        }
        onProgress(0.25f, "Writing ${preview.preparedNurseries.size} nurseries safely to Room...")
        if (preview.preparedNurseries.isNotEmpty()) {
            dao.upsertNurseries(preview.preparedNurseries.map { WbNurseryDirectoryEntity.fromModel(it) })
        }
        onProgress(0.55f, "Writing ${preview.preparedPlants.size} plant catalog entries to Room...")
        if (preview.preparedPlants.isNotEmpty()) {
            dao.upsertPlants(preview.preparedPlants.map { PlantKingdomEntity.fromModel(it) })
        }
        var syncedDocs = 0
        var cloudOk = false
        if (pushToFirestore && (preview.preparedPlants.isNotEmpty() || preview.preparedNurseries.isNotEmpty())) {
            onProgress(0.8f, "Syncing staged records to Cloud Firestore...")
            val sync = pushRecordsToFirestore(preview.preparedPlants, preview.preparedNurseries)
            syncedDocs = sync.plantsCount + sync.nurseriesCount
            cloudOk = sync.cloudSuccess
        }
        onProgress(1f, "CSV Import complete.")
        val summary = "Nurseries written: ${preview.preparedNurseries.size} (${preview.nurserySkippedVerifiedCount} verified protected) • " +
            "Plants written: ${preview.preparedPlants.size} (${preview.plantDraftCount} kept as DRAFT_REVIEW_REQUIRED) • " +
            if (cloudOk) "Cloud synced ($syncedDocs docs)" else "Saved locally (Admin sign-in pushes cloud)"
        return CsvImportExecutionResult(
            adminConfirmed = true,
            nurseriesWritten = preview.preparedNurseries.size,
            nurseriesSkippedVerified = preview.nurserySkippedVerifiedCount,
            plantsWritten = preview.preparedPlants.size,
            plantsDraftPendingReview = preview.plantDraftCount,
            firestoreSyncedDocs = syncedDocs,
            cloudPushSucceeded = cloudOk,
            errorCount = preview.totalErrors,
            errorLog = preview.errorLogLines,
            summaryMessage = summary
        )
    }

    suspend fun approveDraftPlant(
        plantId: String,
        descEn: String,
        descBn: String,
        careEn: String,
        careBn: String,
        careHi: String,
        climateSuitability: String
    ): Result<Unit> {
        if (descEn.isBlank() || careEn.isBlank()) {
            return Result.failure(IllegalArgumentException("Description and care guidance are required before publishing a draft plant."))
        }
        val existing = dao.getPlantById(plantId)?.toModel()
            ?: return Result.failure(NoSuchElementException("Plant $plantId not found"))
        val updated = existing.copy(
            descriptionEn = descEn.trim(),
            descriptionBn = descBn.ifBlank { existing.descriptionBn.ifBlank { descEn.trim() } },
            careInstructionsEn = careEn.trim(),
            careInstructionsBn = careBn.ifBlank { existing.careInstructionsBn.ifBlank { careEn.trim() } },
            careInstructionsHi = careHi.ifBlank { existing.careInstructionsHi },
            climate = climateSuitability.ifBlank { existing.climate },
            isVerifiedEntry = true,
            publicationStatus = "PUBLISHED",
            lastVerifiedDate = todayIso(),
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertPlant(PlantKingdomEntity.fromModel(updated))
        syncPlantToFirestore(updated)
        return Result.success(Unit)
    }

    private suspend fun syncPlantToFirestore(entry: PlantKingdomEntry) {
        try {
            if (firestore != null && Firebase.auth.currentUser != null) {
                firestore?.collection("plant_kingdom")
                    ?.document(entry.plantId)
                    ?.set(toPlantFirestoreMap(entry))
                    ?.await()
            }
        } catch (e: Exception) {
            Log.w("PlantKingdomRepo", "Firestore plant sync deferred: ${e.message}")
        }
    }

    private suspend fun syncNurseryToFirestore(entry: WbNurseryDirectoryEntry) {
        try {
            if (firestore != null && Firebase.auth.currentUser != null) {
                firestore?.collection("wb_nurseries")
                    ?.document(entry.nurseryId)
                    ?.set(toNurseryFirestoreMap(entry))
                    ?.await()
            }
        } catch (e: Exception) {
            Log.w("PlantKingdomRepo", "Firestore nursery sync deferred: ${e.message}")
        }
    }

    private fun normalizeName(raw: String): String =
        raw.trim().lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

    private fun normalizeScientificName(raw: String): String =
        raw.trim().lowercase()
            .substringBefore("/")
            .replace(Regex("\\([^)]*\\)"), " ")
            .replace(Regex("'[^']*'"), " ")
            .replace("×", " ")
            .replace(Regex("(?:^|\\s)spp\\.?(?=\\s|$)"), " ")
            .replace(Regex("\\bhybrids?\\b"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun mapSeedCategoryToGroup(cat: String): PlantKingdomGroup = when (cat.trim().lowercase()) {
        "hill_flower" -> PlantKingdomGroup.HIMALAYAN_HILL_FLOWERS
        "flower" -> PlantKingdomGroup.COMMON_FLOWERS
        "indoor" -> PlantKingdomGroup.MONEY_INDOOR_PLANTS
        "fruit", "hill_fruit" -> PlantKingdomGroup.FRUIT_PLANTS
        "tree" -> PlantKingdomGroup.ORNAMENTAL_TREES_CLIMBERS
        "medicinal", "succulent" -> PlantKingdomGroup.MEDICINAL_HERBS_SUCCULENTS
        else -> PlantKingdomGroup.fromCode(cat)
    }

    private fun mapSeedCategoryToFilterTag(cat: String): String = when (cat.trim().lowercase()) {
        "hill_flower" -> "HILL_FLOWER"
        "flower" -> "FLOWERING"
        "indoor" -> "INDOOR"
        "fruit", "hill_fruit" -> "FRUIT"
        "tree" -> "ORNAMENTAL_TREE"
        "medicinal" -> "MEDICINAL"
        "succulent" -> "SUCCULENT_CACTUS"
        else -> ""
    }

    companion object {
        const val BUNDLED_SEED_ASSET_NAME = "Krishak_Sathi_Firebase_Seed_Data_3e235bd0.json"
        const val BUNDLED_NURSERY_CSV_ASSET = "Krishak_Sathi_Nursery_Directory_432674ce.txt"
        const val BUNDLED_PLANT_CSV_ASSET = "Krishak_Sathi_Plant_Catalog_eef34252.txt"

        fun sanitizeId(raw: String): String =
            raw.trim().lowercase().replace(Regex("[^a-z0-9_]+"), "_").trim('_').ifBlank {
                "plant_${System.currentTimeMillis()}"
            }

        fun todayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
            val earthRadiusKm = 6371.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLng = Math.toRadians(lng2 - lng1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return earthRadiusKm * c
        }

        internal fun parseCsvRow(line: String): List<String> {
            val out = mutableListOf<String>()
            val current = StringBuilder()
            var inQuotes = false
            var i = 0
            while (i < line.length) {
                val c = line[i]
                when {
                    c == '"' -> {
                        if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                            current.append('"')
                            i++
                        } else {
                            inQuotes = !inQuotes
                        }
                    }
                    c == ',' && !inQuotes -> {
                        out.add(current.toString())
                        current.setLength(0)
                    }
                    else -> current.append(c)
                }
                i++
            }
            out.add(current.toString())
            return out
        }
    }
}
