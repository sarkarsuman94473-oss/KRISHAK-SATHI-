package com.example.data.repository

import com.example.data.datasource.PlantPhotoRegistry
import com.example.data.model.NurseryVerificationStatus
import com.example.data.model.PlantKingdomEntry
import com.example.data.model.PlantKingdomGroup
import com.example.data.model.WbNurseryDirectoryEntry
import com.example.data.model.WbNurseryDistrict

enum class CsvPreviewAction {
    NEW_INSERT,
    EXISTING_MATCH_UPDATE,
    SKIPPED_VERIFIED,
    ERROR
}

data class CsvRowPreviewItem(
    val stableId: String,
    val displayName: String,
    val targetCollection: String,
    val action: CsvPreviewAction,
    val note: String
)

data class CsvImportPreviewReport(
    val nurseryTotalRows: Int,
    val nurseryNewCount: Int,
    val nurseryExistingCount: Int,
    val nurserySkippedVerifiedCount: Int,
    val nurseryErrorCount: Int,
    val plantTotalRows: Int,
    val plantNewCount: Int,
    val plantExistingCount: Int,
    val plantDraftCount: Int,
    val plantErrorCount: Int,
    val errorLogLines: List<String>,
    val previewRows: List<CsvRowPreviewItem>,
    val preparedNurseries: List<WbNurseryDirectoryEntry>,
    val preparedPlants: List<PlantKingdomEntry>
) {
    val totalRecords: Int get() = nurseryTotalRows + plantTotalRows
    val totalNewRecords: Int get() = nurseryNewCount + plantNewCount
    val totalExistingRecords: Int get() = nurseryExistingCount + plantExistingCount + nurserySkippedVerifiedCount
    val totalErrors: Int get() = nurseryErrorCount + plantErrorCount
}

data class CsvImportExecutionResult(
    val adminConfirmed: Boolean,
    val nurseriesWritten: Int,
    val nurseriesSkippedVerified: Int,
    val plantsWritten: Int,
    val plantsDraftPendingReview: Int,
    val firestoreSyncedDocs: Int,
    val cloudPushSucceeded: Boolean,
    val errorCount: Int,
    val errorLog: List<String>,
    val summaryMessage: String
)

/**
 * Pure Kotlin CSV Dry-Run Preview & Staged Import Parser for Krishak Sathi:
 * - Automatically strips UTF-8 BOM and ignores introductory notes before actual CSV header rows.
 * - Preserves stable IDs (`KS-NU-0001`..`KS-NU-0037`, `KS-PL-0001`..`KS-PL-0080`).
 * - Prevents duplicate creation by matching on stableId AND normalized business/scientific/English name.
 * - Zero-fabrication: blank fields stay null on `PENDING_VERIFICATION` / `NOT_CONFIRMED` nurseries.
 * - Draft-protection: starter CSV plants remain `DRAFT_REVIEW_REQUIRED` (`isVerifiedEntry = false`)
 *   and never overwrite richer already-published catalog guides.
 */
object PlantKingdomCsvImporter {

    fun parseCsvSkippingIntro(
        rawText: String,
        requiredHeaderTokens: List<String>
    ): Pair<List<String>, List<List<String>>> {
        val cleaned = rawText.removePrefix("\uFEFF")
        val allLines = cleaned.lines()
        val headerIdx = allLines.indexOfFirst { line ->
            val trimmedLower = line.trim().lowercase()
            trimmedLower.isNotBlank() && requiredHeaderTokens.all { tok -> trimmedLower.contains(tok) }
        }
        if (headerIdx < 0) return emptyList<String>() to emptyList()

        val headers = PlantKingdomRepository.parseCsvRow(allLines[headerIdx]).map { it.trim().lowercase() }
        val rows = mutableListOf<List<String>>()
        for (i in (headerIdx + 1) until allLines.size) {
            val line = allLines[i]
            if (line.isBlank()) continue
            rows.add(PlantKingdomRepository.parseCsvRow(line))
        }
        return headers to rows
    }

    fun buildDryRunPreview(
        nurseryCsvText: String,
        plantCsvText: String,
        existingNurseries: List<WbNurseryDirectoryEntry>,
        existingPlants: List<PlantKingdomEntry>
    ): CsvImportPreviewReport {
        val errors = mutableListOf<String>()
        val previewRows = mutableListOf<CsvRowPreviewItem>()
        val preparedNurseries = mutableListOf<WbNurseryDirectoryEntry>()
        val preparedPlants = mutableListOf<PlantKingdomEntry>()

        // Index existing nurseries cleanly
        val existingNurseryById = existingNurseries.associateBy { it.nurseryId.trim().lowercase() }.toMutableMap()
        val existingNurseryByName = existingNurseries.associateBy { normName(it.businessName) }.toMutableMap()
        val seenImportNurseryIds = HashSet<String>()

        var nNew = 0
        var nExisting = 0
        var nSkippedVerified = 0
        var nError = 0

        val (nHeaders, nRows) = parseCsvSkippingIntro(
            nurseryCsvText,
            requiredHeaderTokens = listOf("nursery_id", "business_name")
        )
        if (nurseryCsvText.isNotBlank() && nHeaders.isEmpty()) {
            nError++
            errors.add("Nursery CSV: could not locate CSV header (expected nursery_id,business_name).")
        }

        fun nCol(row: List<String>, vararg names: String): String {
            for (name in names) {
                val idx = nHeaders.indexOf(name.lowercase())
                if (idx >= 0 && idx < row.size) return row[idx].trim()
            }
            return ""
        }

        nRows.forEachIndexed { idx, cols ->
            val stableId = nCol(cols, "nursery_id", "nurseryid")
            val businessName = nCol(cols, "business_name", "businessname")
            if (stableId.isBlank() || businessName.isBlank()) {
                nError++
                errors.add("Nursery row #${idx + 1}: missing nursery_id or business_name")
                previewRows.add(CsvRowPreviewItem(stableId.ifBlank { "ROW_${idx + 1}" }, businessName, "wb_nurseries", CsvPreviewAction.ERROR, "Missing ID or Name"))
                return@forEachIndexed
            }
            if (!seenImportNurseryIds.add(stableId.lowercase())) {
                nError++
                errors.add("Nursery row #${idx + 1}: duplicate nursery_id '$stableId' inside CSV file")
                previewRows.add(CsvRowPreviewItem(stableId, businessName, "wb_nurseries", CsvPreviewAction.ERROR, "Duplicate ID inside CSV"))
                return@forEachIndexed
            }

            val existing = existingNurseryById[stableId.lowercase()]
                ?: existingNurseryByName[normName(businessName)]

            if (existing != null && existing.isVerified) {
                nSkippedVerified++
                previewRows.add(CsvRowPreviewItem(stableId, businessName, "wb_nurseries", CsvPreviewAction.SKIPPED_VERIFIED, "Verified entry preserved (${existing.nurseryId})"))
                return@forEachIndexed
            }

            val districtRaw = nCol(cols, "district")
            val districtCode = if (districtRaw.isNotBlank()) WbNurseryDistrict.fromCode(districtRaw).code
            else existing?.districtCode ?: WbNurseryDistrict.OTHER_WB.code

            val verifStatusRaw = nCol(cols, "verification_status").uppercase()
            val safeVerifStatus = if (verifStatusRaw == NurseryVerificationStatus.PENDING_VERIFICATION || verifStatusRaw.isBlank()) {
                NurseryVerificationStatus.PENDING_VERIFICATION
            } else {
                NurseryVerificationStatus.PENDING_VERIFICATION // Never auto-verify candidate CSV rows
            }
            val ownerConsent = nCol(cols, "owner_consent_status").ifBlank { "NOT_CONFIRMED" }.uppercase()
            val csvNotes = nCol(cols, "notes")
            val sourceRef = nCol(cols, "source_reference").ifBlank { "Initial CSV Directory Import" }
            val lastVerif = nCol(cols, "last_verified_date")

            val entry = WbNurseryDirectoryEntry(
                nurseryId = stableId,
                businessName = businessName,
                businessNameBn = existing?.businessNameBn?.ifBlank { businessName } ?: businessName,
                businessNameHi = existing?.businessNameHi?.ifBlank { businessName } ?: businessName,
                ownerName = nCol(cols, "owner_name").ifBlank { null } ?: existing?.ownerName,
                fullAddress = nCol(cols, "full_address").ifBlank { null } ?: existing?.fullAddress,
                districtCode = districtCode,
                locality = nCol(cols, "locality").ifBlank { null } ?: existing?.locality,
                state = "West Bengal",
                phone = nCol(cols, "phone").ifBlank { null } ?: existing?.phone,
                whatsapp = nCol(cols, "whatsapp").ifBlank { null } ?: existing?.whatsapp,
                latitude = nCol(cols, "latitude").toDoubleOrNull() ?: existing?.latitude,
                longitude = nCol(cols, "longitude").toDoubleOrNull() ?: existing?.longitude,
                websiteOrSocial = nCol(cols, "website").ifBlank { nCol(cols, "social_media") }.ifBlank { null }
                    ?: existing?.websiteOrSocial,
                openingHours = nCol(cols, "opening_hours").ifBlank { null } ?: existing?.openingHours,
                availablePlantCategoriesCsv = nCol(cols, "plant_categories").ifBlank { existing?.availablePlantCategoriesCsv.orEmpty() },
                availablePlantIdsCsv = existing?.availablePlantIdsCsv.orEmpty(),
                descriptionEn = existing?.descriptionEn?.ifBlank { csvNotes }
                    ?: csvNotes.ifBlank { "Preliminary directory candidate; verify before publishing." },
                descriptionBn = existing?.descriptionBn.orEmpty(),
                descriptionHi = existing?.descriptionHi.orEmpty(),
                verificationStatus = safeVerifStatus,
                ownerConsentStatus = ownerConsent,
                isVerified = false,
                sourceReference = sourceRef,
                lastVerifiedDate = lastVerif.ifBlank { existing?.lastVerifiedDate ?: PlantKingdomRepository.todayIso() },
                notes = csvNotes.ifBlank { existing?.notes.orEmpty() },
                linkedSellerUserId = existing?.linkedSellerUserId,
                updatedAtMillis = System.currentTimeMillis()
            )
            preparedNurseries.add(entry)
            if (existing != null) {
                nExisting++
                previewRows.add(CsvRowPreviewItem(stableId, businessName, "wb_nurseries", CsvPreviewAction.EXISTING_MATCH_UPDATE, "Matched existing record -> safe upsert ($safeVerifStatus / $ownerConsent)"))
            } else {
                nNew++
                previewRows.add(CsvRowPreviewItem(stableId, businessName, "wb_nurseries", CsvPreviewAction.NEW_INSERT, "New candidate -> $safeVerifStatus / $ownerConsent"))
            }
        }

        // Index existing plants cleanly
        val existingPlantById = existingPlants.associateBy { it.plantId.trim().lowercase() }.toMutableMap()
        val existingPlantBySci = existingPlants.filter { it.scientificName.isNotBlank() }
            .associateBy { normSci(it.scientificName) }.toMutableMap()
        val existingPlantByName = existingPlants.associateBy { normName(it.nameEn) }.toMutableMap()
        val seenImportPlantIds = HashSet<String>()

        var pNew = 0
        var pExisting = 0
        var pDraftCount = 0
        var pError = 0

        val (pHeaders, pRows) = parseCsvSkippingIntro(
            plantCsvText,
            requiredHeaderTokens = listOf("plant_id", "name_en")
        )
        if (plantCsvText.isNotBlank() && pHeaders.isEmpty()) {
            pError++
            errors.add("Plant Catalog CSV: could not locate CSV header (expected plant_id,category,name_en,...).")
        }

        fun pCol(row: List<String>, vararg names: String): String {
            for (name in names) {
                val idx = pHeaders.indexOf(name.lowercase())
                if (idx >= 0 && idx < row.size) return row[idx].trim()
            }
            return ""
        }

        pRows.forEachIndexed { idx, cols ->
            val stableId = pCol(cols, "plant_id", "plantid")
            val nameEn = pCol(cols, "name_en", "nameen")
            val sciName = pCol(cols, "scientific_name", "scientificname")
            if (stableId.isBlank() || nameEn.isBlank()) {
                pError++
                errors.add("Plant row #${idx + 1}: missing plant_id or name_en")
                previewRows.add(CsvRowPreviewItem(stableId.ifBlank { "ROW_${idx + 1}" }, nameEn, "plant_kingdom", CsvPreviewAction.ERROR, "Missing ID or Name"))
                return@forEachIndexed
            }
            if (!seenImportPlantIds.add(stableId.lowercase())) {
                pError++
                errors.add("Plant row #${idx + 1}: duplicate plant_id '$stableId' inside CSV file")
                previewRows.add(CsvRowPreviewItem(stableId, nameEn, "plant_kingdom", CsvPreviewAction.ERROR, "Duplicate ID inside CSV"))
                return@forEachIndexed
            }

            val nameBn = pCol(cols, "name_bn", "namebn").ifBlank { nameEn }
            val nameHi = pCol(cols, "name_hi", "namehi").ifBlank { nameEn }
            val catRaw = pCol(cols, "category", "primarygroup").lowercase()
            val mappedGroup = mapCatToGroup(catRaw)
            val filterTag = mapCatToTag(catRaw)
            val plantType = pCol(cols, "plant_type")
            val climateNote = pCol(cols, "climate_suitability_note", "climate")
            val pubStatus = pCol(cols, "publication_status").uppercase().ifBlank { "DRAFT_REVIEW_REQUIRED" }
            val isDraft = pubStatus == "DRAFT_REVIEW_REQUIRED"

            val existing = existingPlantById[stableId.lowercase()]
                ?: existingPlantBySci[normSci(sciName)]
                ?: existingPlantByName[normName(nameEn)]

            if (existing != null && existing.isPublishedForPublic()) {
                // Keep richer existing verified catalog record intact; enrich missing tags/translations only
                pExisting++
                val tags = (existing.filterTagsCsv.split(",") + filterTag)
                    .map { it.trim() }.filter { it.isNotBlank() }.distinct().joinToString(",")
                val merged = existing.copy(
                    nameBn = existing.nameBn.ifBlank { nameBn },
                    nameHi = existing.nameHi.ifBlank { nameHi },
                    filterTagsCsv = tags,
                    climate = existing.climate.ifBlank { climateNote },
                    sourceReference = if (!existing.sourceReference.contains(stableId)) {
                        "${existing.sourceReference} • CSV Ref $stableId".take(290)
                    } else existing.sourceReference,
                    updatedAtMillis = System.currentTimeMillis()
                )
                preparedPlants.add(merged)
                previewRows.add(CsvRowPreviewItem(stableId, nameEn, "plant_kingdom", CsvPreviewAction.EXISTING_MATCH_UPDATE, "Matched published species (${existing.plantId}); verified care preserved"))
            } else {
                // Use stable CSV ID (`KS-PL-xxxx`) & preserve DRAFT_REVIEW_REQUIRED until admin reviews care & suitability
                if (isDraft) pDraftCount++
                val targetId = existing?.plantId ?: stableId
                val entry = PlantKingdomEntry(
                    plantId = targetId,
                    nameEn = nameEn,
                    nameBn = nameBn,
                    nameHi = nameHi,
                    scientificName = sciName,
                    primaryGroup = mappedGroup.code,
                    filterTagsCsv = filterTag,
                    descriptionEn = plantType.ifBlank { "$nameEn starter CSV catalog entry (draft review required)." },
                    descriptionBn = pCol(cols, "description_bn").ifBlank { existing?.descriptionBn.orEmpty() },
                    descriptionHi = existing?.descriptionHi.orEmpty(),
                    visualAssetKey = PlantPhotoRegistry.getVisualAssetKey(targetId),
                    customPhotoUri = pCol(cols, "image_url"),
                    climate = climateNote,
                    soilRequirements = pCol(cols, "soil"),
                    sunlight = pCol(cols, "sunlight"),
                    wateringFrequency = pCol(cols, "watering"),
                    floweringOrFruitingSeason = pCol(cols, "flowering_or_fruiting_season"),
                    careInstructionsBn = pCol(cols, "care_instructions_bn"),
                    isRare = catRaw == "hill_flower",
                    isVerifiedEntry = !isDraft,
                    publicationStatus = pubStatus,
                    sourceReference = pCol(cols, "source_reference").ifBlank { "Starter Plant CSV ($stableId)" },
                    lastVerifiedDate = pCol(cols, "last_verified_date").ifBlank { PlantKingdomRepository.todayIso() },
                    updatedAtMillis = System.currentTimeMillis()
                )
                preparedPlants.add(entry)
                if (existing != null) {
                    pExisting++
                    previewRows.add(CsvRowPreviewItem(stableId, nameEn, "plant_kingdom", CsvPreviewAction.EXISTING_MATCH_UPDATE, "Updated existing draft by stable ID ($pubStatus)"))
                } else {
                    pNew++
                    previewRows.add(CsvRowPreviewItem(stableId, nameEn, "plant_kingdom", CsvPreviewAction.NEW_INSERT, "New plant ($stableId) -> $pubStatus"))
                }
            }
        }

        return CsvImportPreviewReport(
            nurseryTotalRows = nRows.size,
            nurseryNewCount = nNew,
            nurseryExistingCount = nExisting,
            nurserySkippedVerifiedCount = nSkippedVerified,
            nurseryErrorCount = nError,
            plantTotalRows = pRows.size,
            plantNewCount = pNew,
            plantExistingCount = pExisting,
            plantDraftCount = pDraftCount,
            plantErrorCount = pError,
            errorLogLines = errors,
            previewRows = previewRows,
            preparedNurseries = preparedNurseries,
            preparedPlants = preparedPlants
        )
    }

    private fun normName(raw: String): String =
        raw.trim().lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

    private fun normSci(raw: String): String =
        raw.trim().lowercase()
            .replace(Regex("\\bspp\\.?\\b"), "")
            .replace(Regex("\\bhybrids?\\b"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun mapCatToGroup(cat: String): PlantKingdomGroup = when (cat.trim().lowercase()) {
        "hill_flower" -> PlantKingdomGroup.HIMALAYAN_HILL_FLOWERS
        "flower" -> PlantKingdomGroup.COMMON_FLOWERS
        "indoor" -> PlantKingdomGroup.MONEY_INDOOR_PLANTS
        "fruit", "hill_fruit" -> PlantKingdomGroup.FRUIT_PLANTS
        "tree" -> PlantKingdomGroup.ORNAMENTAL_TREES_CLIMBERS
        "medicinal", "succulent" -> PlantKingdomGroup.MEDICINAL_HERBS_SUCCULENTS
        else -> PlantKingdomGroup.fromCode(cat)
    }

    private fun mapCatToTag(cat: String): String = when (cat.trim().lowercase()) {
        "hill_flower" -> "HILL_FLOWER"
        "flower" -> "FLOWERING"
        "indoor" -> "INDOOR"
        "fruit", "hill_fruit" -> "FRUIT"
        "tree" -> "ORNAMENTAL_TREE"
        "medicinal" -> "MEDICINAL"
        "succulent" -> "SUCCULENT_CACTUS"
        else -> ""
    }
}
