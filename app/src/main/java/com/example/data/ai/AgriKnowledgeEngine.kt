package com.example.data.ai

import com.example.data.datasource.GovernmentSchemesCatalog
import com.example.data.datasource.TrustedAgriKnowledgeCatalog
import com.example.data.datasource.VerifiedTreatmentDatabase
import com.example.data.local.entity.AiKnowledgeDocEntity
import com.example.data.model.AiSourceCitation
import com.example.data.model.AppLanguage
import com.example.data.model.MasterCrop
import com.example.data.model.VerifiedAgriKnowledgeRecord

data class RagRetrievalResult(
    val groundedContextPrompt: String,
    val citations: List<AiSourceCitation>,
    val matchedRecordsCount: Int
)

/**
 * Retrieves grounded agricultural, scheme, and CIBRC treatment records for AI prompts (RAG)
 * combining curated ICAR/BCKV/UBKV records, custom Admin knowledge docs, VerifiedTreatmentDatabase,
 * and GovernmentSchemesCatalog.
 */
object AgriKnowledgeEngine {

    fun retrieveGroundedContext(
        query: String,
        resolvedCrop: MasterCrop?,
        language: AppLanguage,
        customDocs: List<AiKnowledgeDocEntity> = emptyList(),
        districtHint: String? = null
    ): RagRetrievalResult {
        val normalizedQuery = query.lowercase()
        val cropId = resolvedCrop?.cropId?.lowercase().orEmpty()
        val cropNameEn = resolvedCrop?.nameEn?.lowercase().orEmpty()

        val allDocs = TrustedAgriKnowledgeCatalog.records + customDocs.filter { it.isActive }.map { entity ->
            VerifiedAgriKnowledgeRecord(
                id = entity.id,
                titleEn = entity.titleEn,
                titleBn = entity.titleBn,
                titleHi = entity.titleHi,
                institution = entity.institution,
                sourceUrl = entity.sourceUrl,
                publicationDate = entity.publicationDate,
                verifiedDate = entity.verifiedDate,
                category = entity.category,
                applicableCropIds = entity.applicableCropIdsCsv.split(",").map { it.trim().lowercase() }.filter { it.isNotBlank() },
                regionTag = entity.regionTag,
                keywords = entity.keywordsCsv.split(",").map { it.trim().lowercase() }.filter { it.isNotBlank() },
                summaryEn = entity.summaryEn,
                summaryBn = entity.summaryBn,
                summaryHi = entity.summaryHi,
                isCustomAdminAdded = entity.isCustomAdminAdded
            )
        }

        // Score documents by cropId, keyword match, and regional match
        val scoredDocs = allDocs.map { doc ->
            var score = 0
            if (cropId.isNotBlank() && doc.applicableCropIds.any { it.equals(cropId, ignoreCase = true) }) {
                score += 20
            }
            doc.keywords.forEach { kw ->
                if (kw.length >= 2 && normalizedQuery.contains(kw)) {
                    score += 6
                }
            }
            if (!districtHint.isNullOrBlank() && doc.regionTag.contains(districtHint, ignoreCase = true)) {
                score += 2
            }
            doc to score
        }.filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }

        // Match VerifiedTreatmentDatabase (strictly scoped to crop when crop is selected!)
        val matchedTreatments = VerifiedTreatmentDatabase.treatments.filter { vt ->
            val cropMatches = if (resolvedCrop != null) {
                vt.cropName.equals(cropId, true) ||
                    vt.cropName.equals(cropNameEn, true) ||
                    cropNameEn.contains(vt.cropName, ignoreCase = true)
            } else {
                vt.cropName.contains(normalizedQuery, ignoreCase = true) ||
                    normalizedQuery.contains(vt.cropName.lowercase())
            }
            val problemMatches = vt.problemNameEn.lowercase().split(" ", "(", ")").any { tok ->
                tok.length >= 4 && normalizedQuery.contains(tok)
            } || vt.problemNameBn.contains(query, ignoreCase = true) ||
                vt.problemNameHi.contains(query, ignoreCase = true)

            if (resolvedCrop != null) cropMatches else (cropMatches || problemMatches)
        }.take(2)

        // Match GovernmentSchemesCatalog if query touches schemes/subsidy/insurance/loan
        val schemeQueryKeywords = listOf(
            "scheme", "yojana", "kisan", "bandhu", "insurance", "bima", "bsb", "pmfby", "kcc", "loan", "subsidy", "pension",
            "প্রকল্প", "যোজনা", "কৃষক বন্ধু", "বীমা", "লোন", "ঋণ", "ভর্তুকি",
            "योजना", "बीमा", "ऋण", "केसीसी", "सब्सिडी", "किसान"
        )
        val matchedSchemes = if (schemeQueryKeywords.any { normalizedQuery.contains(it) }) {
            GovernmentSchemesCatalog.allSchemes.filter { scheme ->
                val searchBlob = "${scheme.nameEn} ${scheme.nameBn} ${scheme.nameHi} ${scheme.shortDescEn} ${scheme.cropTagsEn}".lowercase()
                schemeQueryKeywords.any { kw -> normalizedQuery.contains(kw) && searchBlob.contains(kw) }
            }.ifEmpty { GovernmentSchemesCatalog.allSchemes.take(3) }.take(3)
        } else {
            emptyList()
        }

        val citations = linkedMapOf<String, AiSourceCitation>()
        val promptBuilder = StringBuilder()

        if (scoredDocs.isNotEmpty() || matchedTreatments.isNotEmpty() || matchedSchemes.isNotEmpty() || resolvedCrop != null) {
            promptBuilder.append("\n[VERIFIED AGRICULTURAL KNOWLEDGE BASE (RAG CONTEXT)]:\n")
        }

        if (resolvedCrop != null) {
            promptBuilder.append("- Canonical Crop Profile (${resolvedCrop.cropId}): ${resolvedCrop.getName(language)} (${resolvedCrop.scientificName}) | ")
            promptBuilder.append("Best Season: ${resolvedCrop.getBestSeason(language)} | ")
            promptBuilder.append("Soil & pH: ${resolvedCrop.getSoil(language)} (${resolvedCrop.getRecommendedPh(language)}) | ")
            promptBuilder.append("Harvest Duration: ${resolvedCrop.harvestDays} days.\n")
        }

        scoredDocs.forEach { doc ->
            promptBuilder.append("- [${doc.institution} | Verified ${doc.verifiedDate} | Pub ${doc.publicationDate}]: ")
            promptBuilder.append("${doc.getTitle(language)} — ${doc.getSummary(language)}\n")
            if (doc.sourceUrl.startsWith("https://")) {
                citations[doc.sourceUrl] = AiSourceCitation(
                    title = doc.getTitle(language),
                    url = doc.sourceUrl,
                    institution = doc.institution,
                    verifiedDate = doc.verifiedDate
                )
            }
        }

        matchedTreatments.forEach { vt ->
            promptBuilder.append("- [CIBRC/ICAR Treatment Record]: Crop=${vt.getCropDisplayName(language)}, Problem=${vt.getProblemName(language)}, ")
            promptBuilder.append("Biological/First=${vt.getBiologicalOption(language)}, ")
            promptBuilder.append("Verified Chemical=${vt.getActiveIngredient(language)} (${vt.getBrandExamples(language)}), ")
            promptBuilder.append("Dose=${vt.dosePerHectareMlOrGm} ${vt.getDoseUnit(language)} in ${vt.waterPerHectareLiters}L water/ha, ")
            promptBuilder.append("PHI=${vt.preHarvestIntervalDays} days, Safety=${vt.getSafetyInstructions(language)}.\n")
        }

        matchedSchemes.forEach { scheme ->
            promptBuilder.append("- [Official Govt Scheme | Verified ${scheme.lastVerifiedDate}]: ")
            promptBuilder.append("${scheme.getName(language)} (${scheme.getDepartment(language)}) — ${scheme.getShortDesc(language)} | ")
            promptBuilder.append("Portal: ${scheme.officialUrl}\n")
            if (scheme.officialUrl.startsWith("https://")) {
                citations[scheme.officialUrl] = AiSourceCitation(
                    title = scheme.getName(language),
                    url = scheme.officialUrl,
                    institution = scheme.getDepartment(language),
                    verifiedDate = scheme.lastVerifiedDate
                )
            }
        }

        return RagRetrievalResult(
            groundedContextPrompt = promptBuilder.toString(),
            citations = citations.values.toList(),
            matchedRecordsCount = scoredDocs.size + matchedTreatments.size + matchedSchemes.size
        )
    }
}
