package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import com.example.data.ai.AgriKnowledgeEngine
import com.example.data.ai.CropContextResolver
import com.example.data.model.AiAssistantMode
import com.example.data.model.AiGenerationResult
import com.example.data.model.AiSourceCitation
import com.example.data.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class GeneralAiResponse(
    val text: String,
    val hasSearchFallback: Boolean = false,
    val searchQuery: String = "",
    val citations: List<AiSourceCitation> = emptyList()
)

class GeneralAiService(
    private val context: Context,
    private val adminRepository: AiAdminRepository = AiAdminRepository(context.applicationContext),
    private val orchestrator: DualAiOrchestrator = DualAiOrchestrator(adminRepository)
) {

    fun buildGoogleSearchUrl(query: String): String {
        return try {
            val encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString())
            "https://www.google.com/search?q=$encoded"
        } catch (_: Exception) {
            "https://www.google.com/search?q=${query.trim().replace(" ", "+")}"
        }
    }

    fun shouldSuggestGoogleSearch(query: String): Boolean {
        val q = query.lowercase()
        val keywords = listOf(
            "today", "now", "current", "latest", "news", "price today", "weather today",
            "আজকের", "বর্তমান", "তাজা খবর", "লাইভ", "আজকে", "দাম কত",
            "आज का", "ताज़ा", "लाइव", "वर्तमान"
        )
        return keywords.any { q.contains(it) }
    }

    suspend fun generateResponseDetailed(
        userId: String,
        prompt: String,
        history: List<Pair<String, String>>,
        attachedBitmap: Bitmap?,
        language: AppLanguage,
        onStreamChunk: ((String) -> Unit)? = null
    ): AiGenerationResult {
        val selectedLangName = when (language) {
            AppLanguage.BENGALI -> "Bengali (বাংলা)"
            AppLanguage.HINDI -> "Hindi (हिन्दी)"
            AppLanguage.ENGLISH -> "English"
        }

        // Ground any agriculture/scheme question inside General AI with verified RAG sources too
        val detectedCrop = CropContextResolver.detectCropMentionInQuery(prompt)
        val customDocs = adminRepository.getCustomKnowledgeDocsSync()
        val rag = AgriKnowledgeEngine.retrieveGroundedContext(
            query = prompt,
            resolvedCrop = detectedCrop,
            language = language,
            customDocs = customDocs
        )

        val systemInstruction = buildString {
            append("You are 'AI Assistant' in KRISHAK SATHI, a polite, truthful, versatile assistant powered by dual AI intelligence.\n")
            append("Capabilities: general knowledge, education, math, coding, translation, writing, business, and verified agriculture guidance.\n")
            append("Rules:\n")
            append("1. User's active app language is $selectedLangName. Respond cleanly in $selectedLangName without mixing unrelated scripts (unless explicitly translating).\n")
            append("2. Never invent fake URLs, fake phone numbers, fake laws/schemes, or chemical pesticide dosages.\n")
            append("3. If asked about current live news/market rates/weather today, clearly state you lack live real-time internet feeds and advise checking Google Search.\n")
            if (rag.groundedContextPrompt.isNotBlank()) {
                append(rag.groundedContextPrompt)
            }
        }

        return orchestrator.execute(
            userId = userId,
            assistantMode = AiAssistantMode.GENERAL_AI,
            cropId = detectedCrop?.cropId.orEmpty(),
            language = language,
            systemInstruction = systemInstruction,
            history = history,
            userPrompt = prompt,
            attachedBitmap = attachedBitmap,
            temperature = 0.65f,
            citations = rag.citations,
            shouldSuggestGoogleSearch = shouldSuggestGoogleSearch(prompt),
            onStreamChunk = onStreamChunk
        )
    }

    /**
     * Backward-compatible streaming wrapper.
     */
    fun generateStreamingResponse(
        prompt: String,
        history: List<Pair<String, String>>,
        attachedBitmap: Bitmap?,
        language: AppLanguage,
        userId: String = "guest"
    ): Flow<String> = flow {
        val result = generateResponseDetailed(
            userId = userId,
            prompt = prompt,
            history = history,
            attachedBitmap = attachedBitmap,
            language = language
        )
        if (result.success) {
            emit(result.text)
        } else {
            throw IllegalStateException(result.failure?.userMessage ?: "AI service unavailable")
        }
    }
}
