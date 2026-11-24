package com.example.data.local

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class LeafCheckRecord(
    val id: String,
    val cropId: String,
    val cropName: String,
    val landArea: String,
    val diagnosisText: String,
    val timestampMs: Long
)

object LeafCheckHistoryStore {
    private const val PREFS_NAME = "krishak_sathi_leaf_checks"
    private const val KEY_RECORDS = "recent_leaf_checks_json"
    private const val MAX_RECORDS = 10

    fun getRecords(context: Context): List<LeafCheckRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val cropId = obj.optString("cropId", "")
                    val cropName = obj.optString("cropName", "")
                    val diagnosis = obj.optString("diagnosisText", "")
                    if (cropName.isNotBlank() && diagnosis.isNotBlank()) {
                        add(
                            LeafCheckRecord(
                                id = obj.optString("id", "${obj.optLong("timestampMs", 0L)}_$i"),
                                cropId = cropId,
                                cropName = cropName,
                                landArea = obj.optString("landArea", ""),
                                diagnosisText = diagnosis,
                                timestampMs = obj.optLong("timestampMs", System.currentTimeMillis())
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addRecord(context: Context, record: LeafCheckRecord): List<LeafCheckRecord> {
        val updated = (listOf(record) + getRecords(context)).take(MAX_RECORDS)
        saveRecords(context, updated)
        return updated
    }

    fun clearRecords(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_RECORDS)
            .apply()
    }

    private fun saveRecords(context: Context, records: List<LeafCheckRecord>) {
        val arr = JSONArray()
        records.forEach { rec ->
            arr.put(
                JSONObject().apply {
                    put("id", rec.id)
                    put("cropId", rec.cropId)
                    put("cropName", rec.cropName)
                    put("landArea", rec.landArea)
                    put("diagnosisText", rec.diagnosisText)
                    put("timestampMs", rec.timestampMs)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECORDS, arr.toString())
            .apply()
    }
}
