package com.example.data.ai

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ExpertEntity
import com.example.data.model.AppLanguage
import com.example.data.model.FarmerCrop
import com.example.data.model.FarmerProfile
import com.example.data.model.MasterCrop
import com.example.data.model.TeaGarden
import com.example.data.model.WeatherContextSnapshot
import com.example.data.repository.ExpertRepository
import com.example.util.L10n
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

private const val CONTEXT_TAG = "FarmerContextProvider"

data class FarmerAiContextBundle(
    val profile: FarmerProfile? = null,
    val crops: List<FarmerCrop> = emptyList(),
    val teaGardens: List<TeaGarden> = emptyList(),
    val weather: WeatherContextSnapshot,
    val matchedExperts: List<ExpertEntity> = emptyList(),
    val promptBlock: String
)

/**
 * Safely aggregates ONLY the signed-in farmer's authorized profile/crop records,
 * WeatherScreen regional reference snapshot (clearly marked as regional advisory rather than live GPS telemetry),
 * and matching verified experts from ExpertRepository.
 */
class FarmerContextProvider(
    private val context: Context
) {
    private val db: AppDatabase by lazy { AppDatabase.getInstance(context.applicationContext) }

    fun getCurrentWeatherSnapshot(language: AppLanguage, districtHint: String? = null): WeatherContextSnapshot {
        val region = districtHint?.takeIf { it.isNotBlank() } ?: L10n.t("weather_region_label", language)
        return when (language) {
            AppLanguage.BENGALI -> WeatherContextSnapshot(
                regionLabel = region,
                observedCondition = "আংশিক মেঘলা (আঞ্চলিক পূর্বাভাস)",
                tempSummary = "২৮° সে. সর্বোচ্চ / ২১° সে. সর্বনিম্ন",
                rainProbability = "আজ ১০%, আগামীকাল ৬০%",
                humidity = "৭২%",
                windSpeed = "১১ কিমি/ঘণ্টা",
                advisorySummary = "আগামীকাল ভারী বৃষ্টির সম্ভাবনা আছে; স্প্রে করার আগে আবহাওয়া পরীক্ষা করুন ও জল নিকাশী নালা পরিষ্কার রাখুন।",
                isLiveTelemetry = false
            )
            AppLanguage.HINDI -> WeatherContextSnapshot(
                regionLabel = region,
                observedCondition = "आंशिक बादल (क्षेत्रीय पूर्वानुमान)",
                tempSummary = "28°C अधिकतम / 21°C न्यूनतम",
                rainProbability = "आज 10%, कल 60%",
                humidity = "72%",
                windSpeed = "11 किमी/घंटा",
                advisorySummary = "कल भारी बारिश की संभावना है; छिड़काव से पहले मौसम जांचें और जल निकासी तैयार रखें।",
                isLiveTelemetry = false
            )
            AppLanguage.ENGLISH -> WeatherContextSnapshot(
                regionLabel = region,
                observedCondition = "Partly Cloudy (App Regional Advisory)",
                tempSummary = "28°C Max / 21°C Min",
                rainProbability = "Today 10%, Tomorrow 60%",
                humidity = "72%",
                windSpeed = "11 km/h",
                advisorySummary = "Heavy rain likely tomorrow; recheck local sky before spraying and keep drainage clear.",
                isLiveTelemetry = false
            )
        }
    }

    suspend fun loadFarmerContext(
        userId: String,
        resolvedCrop: MasterCrop?,
        query: String,
        language: AppLanguage,
        includeWeather: Boolean = true
    ): FarmerAiContextBundle {
        var profile: FarmerProfile? = null
        var crops: List<FarmerCrop> = emptyList()
        var gardens: List<TeaGarden> = emptyList()

        val activeAuthUid = try {
            Firebase.auth.currentUser?.uid
        } catch (_: Exception) {
            null
        }

        // Strict privacy rule: only query Firestore documents belonging strictly to the signed-in userId
        if (!activeAuthUid.isNullOrBlank() && activeAuthUid == userId) {
            withTimeoutOrNull(2_500L) {
                try {
                    val firestore = FirebaseFirestore.getInstance(
                        context.getString(R.string.firestore_database_id)
                    )
                    val profSnap = firestore.collection("farmers").document(userId).get().await()
                    if (profSnap.exists()) {
                        profile = profSnap.toObject(FarmerProfile::class.java)
                    }
                    val cropSnap = firestore.collection("farmer_crops")
                        .whereEqualTo("farmerId", userId)
                        .limit(8)
                        .get()
                        .await()
                    crops = cropSnap.toObjects(FarmerCrop::class.java)

                    if (resolvedCrop?.cropId == "tea" || query.contains("tea", true) || query.contains("চা") || query.contains("चाय")) {
                        val teaSnap = firestore.collection("tea_gardens")
                            .whereEqualTo("farmerId", userId)
                            .limit(3)
                            .get()
                            .await()
                        gardens = teaSnap.toObjects(TeaGarden::class.java)
                    }
                } catch (e: Exception) {
                    Log.w(CONTEXT_TAG, "Skipping remote farmer context fetch: ${e.message}")
                }
            }
        }

        val weather = getCurrentWeatherSnapshot(language, profile?.district)

        val experts = try {
            val expertRepo = ExpertRepository(context.applicationContext)
            expertRepo.seedInitialExpertsIfEmpty()
            val allVerified = db.expertConsultationDao().getVerifiedExperts().first()
            matchExperts(allVerified, resolvedCrop, query)
        } catch (e: Exception) {
            Log.w(CONTEXT_TAG, "Unable to query verified experts: ${e.message}")
            emptyList()
        }

        val promptBlock = buildString {
            if (profile != null || crops.isNotEmpty() || gardens.isNotEmpty()) {
                append("\n[AUTHORIZED FARMER CONTEXT]:\n")
                profile?.let { p ->
                    val location = listOf(p.village, p.district, p.state).filter { it.isNotBlank() }.joinToString(", ")
                    if (location.isNotBlank()) append("- Location: $location\n")
                    if (p.farmSize > 0) append("- Farm Size: ${p.farmSize} ${p.farmSizeUnit}\n")
                    if (p.mainCrops.isNotBlank()) append("- Main Crops: ${p.mainCrops}\n")
                }
                if (crops.isNotEmpty()) {
                    append("- Farmer's Registered Crops: ")
                    append(
                        crops.joinToString("; ") { c ->
                            "${c.cropName}${if (c.variety.isNotBlank()) " (${c.variety})" else ""}, planted ${c.plantingDate.ifBlank { "N/A" }}, area ${c.landArea} ${c.landUnit}"
                        }
                    )
                    append("\n")
                }
                if (gardens.isNotEmpty()) {
                    append("- Tea Gardens: ")
                    append(
                        gardens.joinToString("; ") { g ->
                            "${g.gardenName} (${g.totalLand} ${g.landUnit}, variety ${g.teaVariety}, last pruning ${g.lastPruningDate.ifBlank { "N/A" }})"
                        }
                    )
                    append("\n")
                }
            }

            if (includeWeather) {
                append("\n[REGIONAL WEATHER REFERENCE (App Forecast Snapshot - NOT Live GPS Station Telemetry)]:\n")
                append("- Region: ${weather.regionLabel} | Temp: ${weather.tempSummary} | Rain: ${weather.rainProbability} | Humidity: ${weather.humidity} | Wind: ${weather.windSpeed}\n")
                append("- Note: Remind farmer to check local field moisture & live sky before spraying.\n")
            }

            if (experts.isNotEmpty()) {
                append("\n[VERIFIED REAL EXPERTS IN KRISHAK SATHI DIRECTORY]:\n")
                experts.forEach { ex ->
                    append("- ${ex.name} | ${ex.institution} | Crops: ${ex.supportedCrops} | Phone: ${ex.phone}\n")
                }
                append("- IMPORTANT: Never invent any other expert phone numbers or contacts.\n")
            }
        }

        return FarmerAiContextBundle(
            profile = profile,
            crops = crops,
            teaGardens = gardens,
            weather = weather,
            matchedExperts = experts,
            promptBlock = promptBlock
        )
    }

    private fun matchExperts(
        experts: List<ExpertEntity>,
        resolvedCrop: MasterCrop?,
        query: String
    ): List<ExpertEntity> {
        val qLower = query.lowercase()
        val cropTokens = buildList {
            resolvedCrop?.let {
                add(it.nameEn.lowercase())
                add(it.nameBn.substringBefore("(").trim().lowercase())
                add(it.nameHi.substringBefore("(").trim().lowercase())
            }
        }
        return experts
            .map { ex ->
                val blob = "${ex.supportedCrops} ${ex.specializedProblems} ${ex.category} ${ex.designation}".lowercase()
                var score = 0
                if (cropTokens.any { tok -> tok.length >= 2 && blob.contains(tok) }) score += 10
                if (qLower.contains("soil") || qLower.contains("মাটি") || qLower.contains("मिट्टी")) {
                    if (ex.category.contains("SOIL", true)) score += 6
                }
                if (qLower.contains("cow") || qLower.contains("dairy") || qLower.contains("fish") || qLower.contains("গরু") || qLower.contains("মাছ")) {
                    if (ex.category.contains("VETERINARY", true)) score += 8
                }
                if (qLower.contains("organic") || qLower.contains("জৈব") || qLower.contains("जैविक")) {
                    if (ex.category.contains("ORGANIC", true)) score += 6
                }
                ex to score
            }
            .sortedByDescending { it.second }
            .take(2)
            .map { it.first }
    }
}
