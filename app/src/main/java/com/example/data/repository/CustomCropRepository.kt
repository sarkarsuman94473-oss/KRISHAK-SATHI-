package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.datasource.CropLocaleData
import com.example.data.datasource.CropMasterDatabase
import com.example.data.datasource.CustomCropLocaleRegistry
import com.example.data.model.CropCategory
import com.example.data.model.CropRegion
import com.example.data.model.CropSeason
import com.example.data.model.MasterCrop
import org.json.JSONArray
import org.json.JSONObject

/**
 * Runtime Admin persistence repository for expandable Master Library crops/plants.
 * Persists user/admin-created crops locally in SharedPreferences and immediately registers
 * them in CropMasterDatabase + CustomCropLocaleRegistry so new entries appear across
 * search, filters, and detail views without rebuilding the app.
 */
object CustomCropRepository {

    private const val PREF_NAME = "krishak_sathi_custom_master_crops"
    private const val KEY_CROPS_JSON = "custom_master_crops_v1"

    data class CustomCropRecord(
        val crop: MasterCrop,
        val localeData: CropLocaleData
    )

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    @Volatile
    private var initialized = false

    fun ensureInitialized(context: Context) {
        if (!initialized) {
            loadAndSyncAll(context)
        }
    }

    fun loadAndSyncAll(context: Context): List<MasterCrop> {
        val raw = prefs(context).getString(KEY_CROPS_JSON, null)
        val records = if (raw.isNullOrBlank()) emptyList() else parseRecords(raw)
        CustomCropLocaleRegistry.clear()
        records.forEach { rec ->
            CustomCropLocaleRegistry.put(rec.crop.cropId, rec.localeData)
        }
        val customCrops = records.map { it.crop }
        CropMasterDatabase.setCustomCrops(customCrops)
        initialized = true
        return customCrops
    }

    fun getCustomCrops(context: Context): List<MasterCrop> {
        ensureInitialized(context)
        return CropMasterDatabase.crops.filter { it.isCustomAdded }
    }

    fun saveCustomCrop(context: Context, record: CustomCropRecord) {
        val raw = prefs(context).getString(KEY_CROPS_JSON, null)
        val existing = if (raw.isNullOrBlank()) emptyList() else parseRecords(raw)
        val cleanId = record.crop.cropId.trim().lowercase()
        val normalizedRecord = record.copy(
            crop = record.crop.copy(cropId = cleanId, isCustomAdded = true)
        )
        val updated = existing.filterNot { it.crop.cropId.trim().lowercase() == cleanId } + normalizedRecord
        persistRecords(context, updated)
        CustomCropLocaleRegistry.put(cleanId, normalizedRecord.localeData)
        CropMasterDatabase.setCustomCrops(updated.map { it.crop })
        initialized = true
    }

    fun deleteCustomCrop(context: Context, cropId: String) {
        val raw = prefs(context).getString(KEY_CROPS_JSON, null)
        val existing = if (raw.isNullOrBlank()) emptyList() else parseRecords(raw)
        val cleanId = cropId.trim().lowercase()
        val updated = existing.filterNot { it.crop.cropId.trim().lowercase() == cleanId }
        persistRecords(context, updated)
        CustomCropLocaleRegistry.clear()
        updated.forEach { CustomCropLocaleRegistry.put(it.crop.cropId, it.localeData) }
        CropMasterDatabase.setCustomCrops(updated.map { it.crop })
        initialized = true
    }

    fun clearAll(context: Context) {
        prefs(context).edit().remove(KEY_CROPS_JSON).apply()
        CustomCropLocaleRegistry.clear()
        CropMasterDatabase.setCustomCrops(emptyList())
        initialized = true
    }

    private fun persistRecords(context: Context, records: List<CustomCropRecord>) {
        val arr = JSONArray()
        for (rec in records) {
            val c = rec.crop
            val l = rec.localeData
            val obj = JSONObject().apply {
                put("cropId", c.cropId)
                put("nameBn", c.nameBn)
                put("nameHi", c.nameHi)
                put("nameEn", c.nameEn)
                put("scientificName", c.scientificName)
                put("category", c.category.name)
                put("subCategory", c.subCategory)
                put("aliases", JSONArray(c.aliases))
                put("seasons", JSONArray(c.seasons.map { it.name }))
                put("regions", JSONArray(c.regions.map { it.name }))
                put("bestSeasonBn", c.bestSeasonBn)
                put("soilBn", c.soilBn)
                put("seedRatePerBighaBn", c.seedRatePerBighaBn)
                put("spacingBn", c.spacingBn)
                put("harvestDays", c.harvestDays)
                put("expectedYieldBn", c.expectedYieldBn)
                put("summaryBn", c.summaryBn)
                put("summaryHi", c.summaryHi)
                put("summaryEn", c.summaryEn)
                put("varietiesBn", c.varietiesBn)
                put("varietiesHi", c.varietiesHi)
                put("varietiesEn", c.varietiesEn)
                put("estimatedCostBn", c.estimatedCostBn)
                put("estimatedCostHi", c.estimatedCostHi)
                put("estimatedCostEn", c.estimatedCostEn)
                put("harvestMethodBn", c.harvestMethodBn)
                put("harvestMethodHi", c.harvestMethodHi)
                put("harvestMethodEn", c.harvestMethodEn)
                put("expertTipsBn", c.expertTipsBn)
                put("expertTipsHi", c.expertTipsHi)
                put("expertTipsEn", c.expertTipsEn)
                put("iconEmoji", c.iconEmoji)
                put("customImageUri", c.customImageUri ?: "")
                put("commonPests", JSONArray(c.commonPests))
                put("commonDiseases", JSONArray(c.commonDiseases))
                put("seasonHi", l.seasonHi)
                put("seasonEn", l.seasonEn)
                put("soilHi", l.soilHi)
                put("soilEn", l.soilEn)
                put("seedRateHi", l.seedRateHi)
                put("seedRateEn", l.seedRateEn)
                put("spacingHi", l.spacingHi)
                put("spacingEn", l.spacingEn)
                put("yieldHi", l.yieldHi)
                put("yieldEn", l.yieldEn)
            }
            arr.put(obj)
        }
        prefs(context).edit().putString(KEY_CROPS_JSON, arr.toString()).apply()
    }

    private fun parseRecords(jsonString: String): List<CustomCropRecord> {
        val result = mutableListOf<CustomCropRecord>()
        try {
            val arr = JSONArray(jsonString)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val cropId = obj.optString("cropId").trim()
                if (cropId.isBlank()) continue
                val catName = obj.optString("category", CropCategory.VEGETABLES.name)
                val category = runCatching { CropCategory.valueOf(catName) }.getOrDefault(CropCategory.VEGETABLES)

                val aliases = obj.optJSONArray("aliases").toStringList()
                val seasons = obj.optJSONArray("seasons").toStringList()
                    .mapNotNull { runCatching { CropSeason.valueOf(it) }.getOrNull() }
                val regions = obj.optJSONArray("regions").toStringList()
                    .mapNotNull { runCatching { CropRegion.valueOf(it) }.getOrNull() }
                val pests = obj.optJSONArray("commonPests").toStringList()
                val diseases = obj.optJSONArray("commonDiseases").toStringList()
                val uriStr = obj.optString("customImageUri").takeIf { it.isNotBlank() }

                val crop = MasterCrop(
                    cropId = cropId,
                    nameBn = obj.optString("nameBn"),
                    nameHi = obj.optString("nameHi"),
                    nameEn = obj.optString("nameEn"),
                    scientificName = obj.optString("scientificName"),
                    category = category,
                    subCategory = obj.optString("subCategory"),
                    aliases = aliases,
                    seasons = seasons,
                    regions = regions,
                    bestSeasonBn = obj.optString("bestSeasonBn"),
                    soilBn = obj.optString("soilBn"),
                    seedRatePerBighaBn = obj.optString("seedRatePerBighaBn"),
                    spacingBn = obj.optString("spacingBn"),
                    harvestDays = obj.optInt("harvestDays", 60),
                    expectedYieldBn = obj.optString("expectedYieldBn"),
                    summaryBn = obj.optString("summaryBn"),
                    summaryHi = obj.optString("summaryHi"),
                    summaryEn = obj.optString("summaryEn"),
                    varietiesBn = obj.optString("varietiesBn"),
                    varietiesHi = obj.optString("varietiesHi"),
                    varietiesEn = obj.optString("varietiesEn"),
                    estimatedCostBn = obj.optString("estimatedCostBn"),
                    estimatedCostHi = obj.optString("estimatedCostHi"),
                    estimatedCostEn = obj.optString("estimatedCostEn"),
                    harvestMethodBn = obj.optString("harvestMethodBn"),
                    harvestMethodHi = obj.optString("harvestMethodHi"),
                    harvestMethodEn = obj.optString("harvestMethodEn"),
                    expertTipsBn = obj.optString("expertTipsBn"),
                    expertTipsHi = obj.optString("expertTipsHi"),
                    expertTipsEn = obj.optString("expertTipsEn"),
                    iconEmoji = obj.optString("iconEmoji", "🌱").ifBlank { "🌱" },
                    customImageUri = uriStr,
                    commonPests = pests,
                    commonDiseases = diseases,
                    isCustomAdded = true
                )

                val localeData = CropLocaleData(
                    seasonHi = obj.optString("seasonHi"),
                    seasonEn = obj.optString("seasonEn"),
                    soilHi = obj.optString("soilHi"),
                    soilEn = obj.optString("soilEn"),
                    seedRateHi = obj.optString("seedRateHi"),
                    seedRateEn = obj.optString("seedRateEn"),
                    spacingHi = obj.optString("spacingHi"),
                    spacingEn = obj.optString("spacingEn"),
                    yieldHi = obj.optString("yieldHi"),
                    yieldEn = obj.optString("yieldEn")
                )
                result.add(CustomCropRecord(crop, localeData))
            }
        } catch (_: Exception) {
            // Gracefully ignore malformed persisted entries
        }
        return result
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until length()) {
            val s = optString(i)
            if (s.isNotBlank()) list.add(s)
        }
        return list
    }
}
