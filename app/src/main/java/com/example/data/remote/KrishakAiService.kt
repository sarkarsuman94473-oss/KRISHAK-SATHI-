package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import com.example.data.ai.AgriKnowledgeEngine
import com.example.data.ai.CropContextResolver
import com.example.data.ai.FarmerAiContextBundle
import com.example.data.ai.FarmerContextProvider
import com.example.data.model.AiAssistantMode
import com.example.data.model.AiGenerationResult
import com.example.data.model.AppLanguage
import com.example.data.model.MachineryRecommendationQuery
import com.example.data.repository.MachineryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class KrishakAiResponseBundle(
    val result: AiGenerationResult,
    val farmerContext: FarmerAiContextBundle? = null
)

class KrishakAiService(
    private val context: Context,
    private val adminRepository: AiAdminRepository = AiAdminRepository(context.applicationContext),
    private val orchestrator: DualAiOrchestrator = DualAiOrchestrator(adminRepository),
    private val contextProvider: FarmerContextProvider = FarmerContextProvider(context.applicationContext)
) {

    /**
     * Full, grounded, crop-isolated, multi-turn agricultural intelligence call with
     * canonical cropId mismatch detection, ICAR/BCKV/UBKV RAG, farmer/weather/expert context,
     * and Gemini -> OpenAI backup failover.
     */
    suspend fun askFarmingQuestionDetailed(
        userId: String,
        query: String,
        selectedCropIdOrName: String?,
        history: List<Pair<String, String>> = emptyList(),
        attachedBitmap: Bitmap? = null,
        language: AppLanguage,
        allowCropMismatchPrompt: Boolean = true,
        onStreamChunk: ((String) -> Unit)? = null
    ): KrishakAiResponseBundle {
        val selectedCrop = CropContextResolver.resolveByCropIdOrName(selectedCropIdOrName)

        // Check crop mismatch before generating wrong-crop advice
        if (allowCropMismatchPrompt && selectedCrop != null) {
            val mismatch = CropContextResolver.detectCropMismatch(selectedCrop, query, language)
            if (mismatch != null) {
                return KrishakAiResponseBundle(
                    result = AiGenerationResult(
                        success = false,
                        cropMismatch = mismatch
                    )
                )
            }
        }

        // If user has no pinned crop selected, resolve crop dynamically from query if unambiguous
        val effectiveCrop = selectedCrop ?: CropContextResolver.detectCropMentionInQuery(query)
        val effectiveCropId = effectiveCrop?.cropId.orEmpty()

        val customDocs = adminRepository.getCustomKnowledgeDocsSync()
        val cfg = adminRepository.getCurrentConfig()
        val farmerBundle = contextProvider.loadFarmerContext(
            userId = userId,
            resolvedCrop = effectiveCrop,
            query = query,
            language = language,
            includeWeather = cfg.enableWeatherContext
        )

        val rag = AgriKnowledgeEngine.retrieveGroundedContext(
            query = query,
            resolvedCrop = effectiveCrop,
            language = language,
            customDocs = customDocs,
            districtHint = farmerBundle.profile?.district
        )

        val langInstruction = when (language) {
            AppLanguage.BENGALI -> "Reply STRICTLY in simple, respectful, natural Bengali (বাংলা) for rural Indian farmers. Do NOT mix in Hindi or unrelated crops."
            AppLanguage.HINDI -> "Reply STRICTLY in simple, clear Hindi (हिन्दी) for Indian farmers. Do NOT mix in Bengali or unrelated crops."
            AppLanguage.ENGLISH -> "Reply STRICTLY in simple, clear, practical English. Do NOT mix Bengali or Hindi."
        }

        val qLower = query.lowercase()
        val isMachineryQuery = listOf(
            "tractor", "tiller", "rotavator", "pump", "sprayer", "drone", "harvester",
            "reaper", "thresher", "auger", "excavator", "irrigation", "machinery", "equipment",
            "seed drill", "transplanter", "brush cutter", "chaff cutter", "rice mill", "flour mill",
            "chainsaw", "weather station", "soil test", "subsidy", "smam", "wbfms", "pm-kusum",
            "ট্রাক্টর", "টিলার", "পাম্প", "স্প্রে", "ড্রোন", "যন্ত্র", "হারভেস্টার", "ভর্তুকি", "মাড়াই",
            "ट्रैक्टर", "पंप", "ड्रोन", "मशीन", "यंत्र", "रोटावेटर", "सब्सिडी", "थ्रेशर", "स्प्रेयर"
        ).any { qLower.contains(it) }

        val machineryBlock = if (isMachineryQuery) {
            try {
                val machRepo = MachineryRepository(context.applicationContext)
                val (matches, summary) = machRepo.recommendMachinesForQuery(
                    query = MachineryRecommendationQuery(
                        cropName = effectiveCrop?.getName(language) ?: "Mixed Crop",
                        landSizeAcres = 1.5,
                        soilOrTerrain = farmerBundle.profile?.district ?: "North Bengal / Siliguri Terai",
                        irrigationAvailable = true,
                        budgetRangeInr = "Standard",
                        operationNeed = query,
                        skilledOperatorAvailable = false,
                        powerPreference = ""
                    ),
                    lang = language
                )
                buildString {
                    append("\n[APPROVED MACHINERY CATALOG REFERENCE — DO NOT INVENT PRICES/SUBSIDIES]\n")
                    append(summary).append("\n")
                    matches.forEach { m ->
                        append("- ${m.id}: ${m.getDisplayName(language)} (${m.brand} ${m.modelNumber}, ${m.horsepowerOrCapacity}, ${m.fuelOrPowerType}, Crops: ${m.suitableCrops})\n")
                    }
                    append("Subsidy Policy Rule: Only reference official portals SMAM (https://agrimachinery.nic.in/) and West Bengal WBFMS (https://wbfms.wb.gov.in/) for informational guidance. NEVER promise subsidy approval, NEVER guess subsidy amounts, and NEVER request Aadhaar, OTP, or bank details.\n")
                    if (qLower.contains("drone") || qLower.contains("ড্রোন") || qLower.contains("ड्रोन")) {
                        append("DGCA Safety Rule: Strictly remind the farmer of DGCA Drone Rules (UIN, Remote Pilot Certificate, DigitalSky airspace approval around Bagdogra/Siliguri).\n")
                    }
                    if (qLower.contains("excavator") || qLower.contains("dig") || qLower.contains("খনন") || qLower.contains("खुदाई")) {
                        append("Earthmoving Safety Rule: Strictly advise verifying underground utilities, slope stability, and trained operator PPE before trench/pond digging.\n")
                    }
                }
            } catch (_: Exception) {
                ""
            }
        } else ""

        val systemInstruction = buildString {
            append("You are KRISHAK AI, the intelligent agricultural advisor in KRISHAK SATHI.\n")
            append("STRICT HONESTY, CROP ACCURACY & SAFETY RULES:\n")
            append("1. NEVER invent or guess chemical pesticide/herbicide doses, toxicity claims, phone numbers, or fake links.\n")
            append("2. Always prioritize safe, preventive, cultural, and biological/organic steps first (field sanitation, Neem oil, Trichoderma, pheromone/sticky traps, drainage).\n")
            append("3. Only mention verified chemical active ingredients/doses present in [VERIFIED AGRICULTURAL KNOWLEDGE BASE]; otherwise advise verifying label / KVK.\n")
            append("4. Focus STRICTLY on the resolved crop (${effectiveCrop?.getName(language) ?: "General Farming"}) and do NOT confuse or mix advice from other crops.\n")
            append("5. State confidence/uncertainty honestly. If an attached photo or symptom description is unclear, ask targeted follow-up questions or request a clearer close-up photo.\n")
            append("6. Weather note: Any weather metrics provided below are App Regional Reference Forecast (NOT live GPS weather station telemetry); state that basis clearly when mentioning weather.\n")
            append("7. Structure practical problem/disease answers concisely using clear headings (Problem & Confidence, Causes/Other Possibilities, Immediate Safe Actions, Verified Prevention/Safety, When to Consult Expert + Verified Source reference).\n")
            append("8. Language policy: $langInstruction\n")
            if (rag.groundedContextPrompt.isNotBlank()) {
                append(rag.groundedContextPrompt)
            }
            if (farmerBundle.promptBlock.isNotBlank()) {
                append(farmerBundle.promptBlock)
            }
            if (machineryBlock.isNotBlank()) {
                append(machineryBlock)
            }
        }

        val genResult = orchestrator.execute(
            userId = userId,
            assistantMode = AiAssistantMode.FARMER_AI,
            cropId = effectiveCropId,
            language = language,
            systemInstruction = systemInstruction,
            history = history,
            userPrompt = query,
            attachedBitmap = attachedBitmap,
            temperature = 0.35f,
            citations = rag.citations,
            onStreamChunk = onStreamChunk
        )

        return KrishakAiResponseBundle(
            result = genResult,
            farmerContext = farmerBundle
        )
    }

    /**
     * Backward-compatible streaming helper delegating through the upgraded dual-AI orchestrator.
     */
    fun askFarmingQuestion(
        query: String,
        currentCropContext: String?,
        language: AppLanguage,
        userId: String = "guest"
    ): Flow<String> = flow {
        val bundle = askFarmingQuestionDetailed(
            userId = userId,
            query = query,
            selectedCropIdOrName = currentCropContext,
            language = language,
            allowCropMismatchPrompt = false
        )
        if (bundle.result.success) {
            emit(bundle.result.text)
        } else {
            throw IllegalStateException(bundle.result.failure?.userMessage ?: "AI service unavailable")
        }
    }

    /**
     * Upgraded 7-point structured image diagnosis for PlantProblemCheckerScreen.
     */
    suspend fun diagnosePlantProblemDetailed(
        userId: String = "guest",
        bitmap: Bitmap,
        cropIdOrName: String,
        landAreaInfo: String?,
        language: AppLanguage
    ): KrishakAiResponseBundle {
        val resolvedCrop = CropContextResolver.resolveByCropIdOrName(cropIdOrName)
        val cropName = resolvedCrop?.getName(language) ?: cropIdOrName
        val cropId = resolvedCrop?.cropId ?: cropIdOrName

        val customDocs = adminRepository.getCustomKnowledgeDocsSync()
        val farmerBundle = contextProvider.loadFarmerContext(
            userId = userId,
            resolvedCrop = resolvedCrop,
            query = "$cropName leaf disease pest symptoms",
            language = language,
            includeWeather = adminRepository.getCurrentConfig().enableWeatherContext
        )
        val rag = AgriKnowledgeEngine.retrieveGroundedContext(
            query = "$cropName disease pest deficiency blight",
            resolvedCrop = resolvedCrop,
            language = language,
            customDocs = customDocs,
            districtHint = farmerBundle.profile?.district
        )

        val structureInstructions = when (language) {
            AppLanguage.BENGALI -> """
                ছবিটি পর্যবেক্ষণ করে বাংলায় এই ৭টি পরিষ্কার পয়েন্টে উত্তর দিন:
                ⚠️ ছবি দেখে প্রাথমিক ধারণা দেওয়া হয়েছে। চূড়ান্ত সিদ্ধান্তের আগে বিশেষজ্ঞের পরামর্শ নিন।
                1. দৃশ্যমান লক্ষণ (Visible Symptoms — পাতায় কী দেখা যাচ্ছে, ছবি অস্পষ্ট হলে স্পষ্টভাবে জানান ও পরিষ্কার ক্লোজ-আপ ছবি চাইবেন)
                2. সম্ভাব্য রোগ বা পোকা ও নিশ্চয়তার মাত্রা (Likely Disease/Pest & Confidence: High/Medium/Low)
                3. অন্যান্য সম্ভাব্য কারণ (Nutrient Deficiency বা পানির ঘাটতি/জলাবদ্ধতা কি হতে পারে?)
                4. অতিরিক্ত তথ্য বা পরিষ্কার ছবির প্রয়োজন (গাছের বয়স, মাটির আর্দ্রতা বা আরও পরিষ্কার ছবি দরকার কি না)
                5. নিরাপদ প্রাথমিক ও জৈব করণীয় (আক্রান্ত অংশ সরানো, জল নিকাশী, নিম তেল বা জৈব সমাধান)
                6. রাসায়নিক ও ডোজ সতর্কতা (শুধু ডাটাবেসের যাচাইকৃত সক্রিয় উপাদান উল্লেখ করুন, নিজে থেকে ডোজ বানাবেন না; লেবেল ও PPE মেনে চলুন)
                7. কখন কৃষি বিশেষজ্ঞ বা KVK-তে দেখানো জরুরি (সংক্রমণ দ্রুত ছড়ালে বা নিশ্চয়তা কম হলে)
            """.trimIndent()
            AppLanguage.HINDI -> """
                तस्वीर का विश्लेषण कर हिंदी में इन 7 स्पष्ट बिंदुओं में उत्तर दें:
                ⚠️ फोटो के आधार पर प्रारंभिक मूल्यांकन। अंतिम पुष्टि के लिए कृषि विशेषज्ञ से सलाह लें।
                1. दिखाई देने वाले लक्षण (Visible Symptoms — यदि फोटो धुंधली या अस्पष्ट है तो स्पष्ट रूप से बताएं और साफ क्लोज़-अप फोटो मांगें)
                2. संभावित रोग या कीट एवं विश्वास स्तर (Confidence: High/Medium/Low)
                3. अन्य संभावित कारण (पोषक तत्वों की कमी, पानी की कमी या जलभराव)
                4. अतिरिक्त जानकारी की आवश्यकता (फसल की अवस्था या स्पष्ट फोटो)
                5. सुरक्षित तत्काल और जैविक कदम (संक्रमित पत्तियाँ हटाना, नीम तेल आदि)
                6. रासायनिक व खुराक चेतावनी (केवल सत्यापित रिकॉर्ड का ही उल्लेख करें, बिना प्रमाणित खुराक न बताएं; PPE पहनें)
                7. कृषि विशेषज्ञ / KVK से कब संपर्क करें
            """.trimIndent()
            AppLanguage.ENGLISH -> """
                Analyze the uploaded plant/leaf photo and respond strictly in English with these 7 clear points:
                ⚠️ Preliminary visual assessment only. Consult an agriculture expert/KVK before spraying chemicals.
                1. Visible Symptoms (state clearly if photo is blurry/dark/unclear and ask for a clearer close-up)
                2. Most Likely Disease/Pest & Confidence Level (High / Medium / Low)
                3. Other Possible Explanations (distinctly check nutrient deficiency vs water stress vs fungal/viral/pest)
                4. Additional Information / Photo Needed (crop stage, underside of leaf, stem view)
                5. Safe Immediate & Biological/Cultural Steps (sanitation, drainage, Neem/Trichoderma/traps)
                6. Pesticide Safety & Verified Chemical Rules (NEVER invent chemical names or dosages; cite only verified CIBRC facts below or advise KVK)
                7. When Expert Inspection Is Necessary (signs of rapid spread or low confidence)
            """.trimIndent()
        }

        val systemInstruction = buildString {
            append("You are Krishak AI Vision Plant Doctor. Strictly follow agricultural safety and visual honesty rules.\n")
            append("Do NOT hallucinate pests/diseases if the image is blurry, non-plant, or unclear — instead honestly report low confidence and request a clearer photo.\n")
            append("Crop: $cropName (canonical cropId: $cropId)\n")
            if (!landAreaInfo.isNullOrBlank()) append("Land Area: $landAreaInfo\n")
            if (rag.groundedContextPrompt.isNotBlank()) append(rag.groundedContextPrompt)
            if (farmerBundle.promptBlock.isNotBlank()) append(farmerBundle.promptBlock)
        }

        val genResult = orchestrator.execute(
            userId = userId,
            assistantMode = AiAssistantMode.PLANT_PROBLEM,
            cropId = cropId,
            language = language,
            systemInstruction = systemInstruction,
            history = emptyList(),
            userPrompt = structureInstructions,
            attachedBitmap = bitmap,
            temperature = 0.25f,
            citations = rag.citations
        )

        return KrishakAiResponseBundle(
            result = genResult,
            farmerContext = farmerBundle
        )
    }

    suspend fun diagnosePlantProblem(
        bitmap: Bitmap,
        cropName: String,
        landAreaInfo: String?,
        language: AppLanguage
    ): String {
        val bundle = diagnosePlantProblemDetailed(
            userId = "guest",
            bitmap = bitmap,
            cropIdOrName = cropName,
            landAreaInfo = landAreaInfo,
            language = language
        )
        return if (bundle.result.success) {
            bundle.result.text
        } else {
            throw IllegalStateException(bundle.result.failure?.userMessage ?: "Diagnosis failed")
        }
    }
}
