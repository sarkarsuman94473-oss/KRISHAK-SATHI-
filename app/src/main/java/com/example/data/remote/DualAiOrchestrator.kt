package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.ai.AiSecurityGuard
import com.example.data.model.AiAdminConfig
import com.example.data.model.AiAssistantMode
import com.example.data.model.AiErrorCode
import com.example.data.model.AiFailureInfo
import com.example.data.model.AiGenerationResult
import com.example.data.model.AiProviderHealth
import com.example.data.model.AiProviderKind
import com.example.data.model.AiSourceCitation
import com.example.data.model.AppLanguage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

data class AiBackendRequest(
    val systemInstruction: String,
    val history: List<Pair<String, String>>,
    val userPrompt: String,
    val bitmap: Bitmap? = null,
    val jpegBytes: ByteArray? = null,
    val temperature: Float = 0.4f,
    val modelName: String
)

class AiProviderException(
    val provider: AiProviderKind,
    val code: AiErrorCode,
    override val message: String,
    val isTransient: Boolean = false
) : Exception(message)

interface AiBackendClient {
    val providerKind: AiProviderKind
    suspend fun generate(request: AiBackendRequest): String
    fun generateStream(request: AiBackendRequest): Flow<String>
}

/**
 * Primary Gemini integration using Firebase AI Logic (no client API keys).
 */
class GeminiAiLogicClient : AiBackendClient {
    override val providerKind: AiProviderKind = AiProviderKind.GEMINI

    private fun buildContents(request: AiBackendRequest): List<Content> {
        val contents = mutableListOf<Content>()
        contents.add(content("user") { text("SYSTEM INSTRUCTIONS (Strict Policy):\n${request.systemInstruction}") })
        contents.add(content("model") { text("Understood. I will follow all agricultural safety, crop-accuracy, and language instructions.") })
        for ((sender, msg) in request.history.takeLast(8)) {
            val role = if (sender == "user") "user" else "model"
            contents.add(content(role) { text(msg) })
        }
        contents.add(content("user") {
            if (request.bitmap != null) {
                image(request.bitmap)
            }
            text(request.userPrompt)
        })
        return contents
    }

    override suspend fun generate(request: AiBackendRequest): String {
        val model = Firebase.ai.generativeModel(
            modelName = request.modelName.ifBlank { "gemini-3.8-flash" },
            generationConfig = generationConfig {
                temperature = request.temperature
                topK = 40
                topP = 0.95f
            }
        )
        try {
            val response = model.generateContent(buildContents(request))
            val text = response.text?.trim().orEmpty()
            if (text.isBlank()) {
                throw AiProviderException(providerKind, AiErrorCode.SERVICE_UNAVAILABLE, "Empty Gemini response", isTransient = true)
            }
            return text
        } catch (e: CancellationException) {
            throw e
        } catch (e: AiProviderException) {
            throw e
        } catch (e: Exception) {
            throw mapGeminiException(e)
        }
    }

    override fun generateStream(request: AiBackendRequest): Flow<String> = flow {
        val model = Firebase.ai.generativeModel(
            modelName = request.modelName.ifBlank { "gemini-3.8-flash" },
            generationConfig = generationConfig {
                temperature = request.temperature
                topK = 40
                topP = 0.95f
            }
        )
        try {
            var emittedAny = false
            model.generateContentStream(buildContents(request)).collect { chunk ->
                val piece = chunk.text
                if (!piece.isNullOrEmpty()) {
                    emittedAny = true
                    emit(piece)
                }
            }
            if (!emittedAny) {
                throw AiProviderException(providerKind, AiErrorCode.SERVICE_UNAVAILABLE, "Empty Gemini stream", isTransient = true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: AiProviderException) {
            throw e
        } catch (e: Exception) {
            throw mapGeminiException(e)
        }
    }

    private fun mapGeminiException(e: Exception): AiProviderException {
        val msg = e.localizedMessage ?: e.javaClass.simpleName
        val lower = msg.lowercase()
        return when {
            lower.contains("429") || lower.contains("quota") || lower.contains("resource_exhausted") ->
                AiProviderException(providerKind, AiErrorCode.QUOTA_EXCEEDED, msg, isTransient = false)
            lower.contains("timeout") || e is SocketTimeoutException ->
                AiProviderException(providerKind, AiErrorCode.TIMEOUT, msg, isTransient = true)
            lower.contains("unable to resolve host") || lower.contains("network") || lower.contains("failed to connect") ->
                AiProviderException(providerKind, AiErrorCode.NETWORK_OFFLINE, msg, isTransient = true)
            lower.contains("403") || lower.contains("503") || lower.contains("unavailable") || lower.contains("app check") ->
                AiProviderException(providerKind, AiErrorCode.SERVICE_UNAVAILABLE, msg, isTransient = true)
            else ->
                AiProviderException(providerKind, AiErrorCode.UNKNOWN, msg, isTransient = false)
        }
    }
}

/**
 * Backup OpenAI ChatGPT client communicating ONLY via an Admin-configured HTTPS server proxy.
 * Sends the authenticated farmer's Firebase ID Token in Authorization header; NEVER holds an OpenAI API key in APK.
 */
class OpenAiProxyClient(
    private val proxyUrlProvider: () -> String,
    private val timeoutMsProvider: () -> Long,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()
) : AiBackendClient {
    override val providerKind: AiProviderKind = AiProviderKind.OPENAI

    override suspend fun generate(request: AiBackendRequest): String = withContext(Dispatchers.IO) {
        val endpoint = proxyUrlProvider().trim()
        val tempCfg = AiAdminConfig(openAiProxyUrl = endpoint)
        if (!tempCfg.hasValidOpenAiProxy()) {
            throw AiProviderException(
                provider = providerKind,
                code = AiErrorCode.UNCONFIGURED,
                message = "OpenAI server proxy HTTPS endpoint is not configured in Admin AI Settings.",
                isTransient = false
            )
        }

        val idToken = try {
            Firebase.auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (_: Exception) {
            null
        }
        if (idToken.isNullOrBlank()) {
            throw AiProviderException(
                provider = providerKind,
                code = AiErrorCode.AUTH_REQUIRED,
                message = "Google Sign-In session token required to access OpenAI backup proxy.",
                isTransient = false
            )
        }

        val messagesJson = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", request.systemInstruction)
            })
            request.history.takeLast(8).forEach { (sender, txt) ->
                put(JSONObject().apply {
                    put("role", if (sender == "user") "user" else "assistant")
                    put("content", txt)
                })
            }
            if (request.jpegBytes != null) {
                val b64 = Base64.encodeToString(request.jpegBytes, Base64.NO_WRAP)
                val parts = JSONArray().apply {
                    put(JSONObject().apply {
                        put("type", "text")
                        put("text", request.userPrompt)
                    })
                    put(JSONObject().apply {
                        put("type", "image_url")
                        put("image_url", JSONObject().apply {
                            put("url", "data:image/jpeg;base64,$b64")
                        })
                    })
                }
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", parts)
                })
            } else {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", request.userPrompt)
                })
            }
        }

        val bodyJson = JSONObject().apply {
            put("model", request.modelName.ifBlank { "gpt-4o-mini" })
            put("temperature", request.temperature.toDouble())
            put("messages", messagesJson)
        }

        val req = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $idToken")
            .addHeader("Content-Type", "application/json")
            .post(bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val clientForTimeout = httpClient.newBuilder()
            .callTimeout(timeoutMsProvider().coerceIn(5_000L, 60_000L), TimeUnit.MILLISECONDS)
            .build()

        try {
            clientForTimeout.newCall(req).execute().use { resp ->
                val rawBody = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    val code = when (resp.code) {
                        401, 403 -> AiErrorCode.AUTH_REQUIRED
                        429 -> AiErrorCode.QUOTA_EXCEEDED
                        408, 504 -> AiErrorCode.TIMEOUT
                        500, 502, 503 -> AiErrorCode.SERVICE_UNAVAILABLE
                        else -> AiErrorCode.UNKNOWN
                    }
                    throw AiProviderException(
                        provider = providerKind,
                        code = code,
                        message = "OpenAI proxy HTTP ${resp.code}",
                        isTransient = resp.code in listOf(408, 500, 502, 503, 504)
                    )
                }
                val parsed = parseProxyResponseText(rawBody)
                if (parsed.isBlank()) {
                    throw AiProviderException(providerKind, AiErrorCode.SERVICE_UNAVAILABLE, "Empty proxy response", isTransient = true)
                }
                parsed
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: AiProviderException) {
            throw e
        } catch (e: SocketTimeoutException) {
            throw AiProviderException(providerKind, AiErrorCode.TIMEOUT, e.message ?: "Timeout", isTransient = true)
        } catch (e: IOException) {
            throw AiProviderException(providerKind, AiErrorCode.NETWORK_OFFLINE, e.message ?: "Network error", isTransient = true)
        } catch (e: Exception) {
            throw AiProviderException(providerKind, AiErrorCode.UNKNOWN, e.message ?: "Proxy error", isTransient = false)
        }
    }

    override fun generateStream(request: AiBackendRequest): Flow<String> = flow {
        emit(generate(request))
    }

    private fun parseProxyResponseText(raw: String): String {
        val obj = JSONObject(raw)
        obj.optString("text", "").takeIf { it.isNotBlank() }?.let { return it.trim() }
        obj.optString("reply", "").takeIf { it.isNotBlank() }?.let { return it.trim() }
        val choices = obj.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            val first = choices.optJSONObject(0)
            val msgObj = first?.optJSONObject("message")
            val content = msgObj?.optString("content", "")?.trim()
            if (!content.isNullOrBlank()) return content
        }
        return ""
    }
}

/**
 * Resilient Dual-AI Orchestrator:
 * - Enforces input validation, image limits, and sliding-window + daily rate limits.
 * - Executes Primary Provider with bounded timeout & single transient retry.
 * - Automatically falls back to Backup Provider when enabled & configured.
 * - Never fakes AI answers when providers fail; returns explicit failure details for UI Retry.
 */
class DualAiOrchestrator(
    private val adminRepository: AiAdminRepository,
    private val geminiClient: AiBackendClient = GeminiAiLogicClient(),
    private val openAiClient: AiBackendClient = OpenAiProxyClient(
        proxyUrlProvider = { adminRepository.getCurrentConfig().openAiProxyUrl },
        timeoutMsProvider = { adminRepository.getCurrentConfig().timeoutMs }
    )
) {
    private fun clientFor(kind: AiProviderKind): AiBackendClient = when (kind) {
        AiProviderKind.GEMINI -> geminiClient
        AiProviderKind.OPENAI -> openAiClient
    }

    private fun modelFor(kind: AiProviderKind, cfg: AiAdminConfig): String = when (kind) {
        AiProviderKind.GEMINI -> cfg.geminiModelName
        AiProviderKind.OPENAI -> cfg.openAiModelName
    }

    suspend fun execute(
        userId: String,
        assistantMode: AiAssistantMode,
        cropId: String,
        language: AppLanguage,
        systemInstruction: String,
        history: List<Pair<String, String>>,
        userPrompt: String,
        attachedBitmap: Bitmap? = null,
        temperature: Float = 0.4f,
        citations: List<AiSourceCitation> = emptyList(),
        shouldSuggestGoogleSearch: Boolean = false,
        onStreamChunk: ((String) -> Unit)? = null
    ): AiGenerationResult {
        val cfg = adminRepository.getCurrentConfig()
        val safeUserId = userId.ifBlank { "guest" }

        // 1. Validate & sanitize text
        val sanitized = AiSecurityGuard.sanitizeUserInput(userPrompt, attachedBitmap != null, language)
        if (!sanitized.isValid) {
            return AiGenerationResult(
                success = false,
                failure = AiFailureInfo(
                    code = AiErrorCode.VALIDATION_ERROR,
                    userMessage = sanitized.blockedReason ?: "Invalid input",
                    technicalReason = "Input validation failed",
                    primaryAttempted = cfg.primaryProvider,
                    retryAllowed = false
                )
            )
        }

        // 2. Validate & compress attached image
        val (bitmapPayload, bitmapErr) = AiSecurityGuard.validateAndCompressBitmap(attachedBitmap, language)
        if (bitmapErr != null) {
            return AiGenerationResult(
                success = false,
                failure = AiFailureInfo(
                    code = AiErrorCode.VALIDATION_ERROR,
                    userMessage = bitmapErr,
                    technicalReason = "Image validation failed",
                    primaryAttempted = cfg.primaryProvider,
                    retryAllowed = false
                )
            )
        }

        // 3. Per-user sliding window & daily rate limiting
        if (!AiSecurityGuard.checkAndRecordRateLimit(safeUserId, cfg.rateLimitPerMinute)) {
            val rateMsg = when (language) {
                AppLanguage.BENGALI -> "আপনি অল্প সময়ে অনেক প্রশ্ন করেছেন। দয়া করে এক মিনিট অপেক্ষা করে আবার চেষ্টা করুন।"
                AppLanguage.HINDI -> "आपने बहुत कम समय में कई प्रश्न पूछे हैं। कृपया 1 मिनट प्रतीक्षा करके पुनः प्रयास करें।"
                AppLanguage.ENGLISH -> "Rate limit reached. Please wait a minute before sending another AI request."
            }
            return AiGenerationResult(
                success = false,
                failure = AiFailureInfo(
                    code = AiErrorCode.RATE_LIMITED,
                    userMessage = rateMsg,
                    technicalReason = "Per-minute rate limit (${cfg.rateLimitPerMinute}/min) exceeded",
                    primaryAttempted = cfg.primaryProvider,
                    retryAllowed = true
                )
            )
        }

        val todayCount = adminRepository.getUserRequestsTodayCount(safeUserId)
        if (todayCount >= cfg.dailyQuotaPerUser) {
            val quotaMsg = when (language) {
                AppLanguage.BENGALI -> "আজকের দৈনিক AI ব্যবহারের সীমা (${cfg.dailyQuotaPerUser}) পূর্ণ হয়েছে। জরুরি সাহায্যের জন্য কৃষি বিশেষজ্ঞের সাথে যোগাযোগ করুন।"
                AppLanguage.HINDI -> "आज की दैनिक AI उपयोग सीमा (${cfg.dailyQuotaPerUser}) पूरी हो गई है। कृपया कृषि विशेषज्ञ से परामर्श लें।"
                AppLanguage.ENGLISH -> "Daily AI usage quota (${cfg.dailyQuotaPerUser}) reached. Please consult verified experts or retry tomorrow."
            }
            return AiGenerationResult(
                success = false,
                failure = AiFailureInfo(
                    code = AiErrorCode.RATE_LIMITED,
                    userMessage = quotaMsg,
                    technicalReason = "Daily quota ($todayCount/${cfg.dailyQuotaPerUser}) exceeded",
                    primaryAttempted = cfg.primaryProvider,
                    retryAllowed = false
                )
            )
        }

        // 4. Execute Primary with timeout & single bounded transient retry
        val primary = cfg.primaryProvider
        val backup = if (primary == AiProviderKind.GEMINI) AiProviderKind.OPENAI else AiProviderKind.GEMINI
        val startMs = System.currentTimeMillis()
        var primaryError: AiProviderException? = null

        val primaryReq = AiBackendRequest(
            systemInstruction = systemInstruction,
            history = history,
            userPrompt = sanitized.sanitizedText.ifBlank { "Analyze the attached plant image." },
            bitmap = bitmapPayload?.bitmap,
            jpegBytes = bitmapPayload?.jpegBytes,
            temperature = temperature,
            modelName = modelFor(primary, cfg)
        )

        val primaryResult = attemptProvider(clientFor(primary), primaryReq, cfg.timeoutMs, cfg.maxRetriesPerProvider, onStreamChunk)
        if (primaryResult.isSuccess) {
            val text = primaryResult.getOrThrow()
            val latency = System.currentTimeMillis() - startMs
            val tokens = AiAdminRepository.estimateTokens(systemInstruction + sanitized.sanitizedText, text)
            adminRepository.updateProviderHealth(primary, AiProviderHealth.ONLINE, null)
            adminRepository.recordTelemetry(
                userId = safeUserId,
                assistantMode = assistantMode.wireValue,
                cropId = cropId,
                provider = primary,
                model = primaryReq.modelName,
                fallbackUsed = false,
                success = true,
                errorCode = AiErrorCode.NONE.name,
                errorSummary = "",
                latencyMs = latency,
                estimatedTokens = tokens
            )
            return AiGenerationResult(
                success = true,
                text = text,
                providerUsed = primary,
                modelUsed = primaryReq.modelName,
                fallbackUsed = false,
                latencyMs = latency,
                estimatedTokens = tokens,
                citations = if (cfg.enableRagCitations) citations else emptyList(),
                shouldSuggestGoogleSearch = shouldSuggestGoogleSearch
            )
        } else {
            primaryError = primaryResult.exceptionOrNull() as? AiProviderException
                ?: AiProviderException(primary, AiErrorCode.UNKNOWN, "Primary provider failed")
            val primaryHealth = when (primaryError.code) {
                AiErrorCode.QUOTA_EXCEEDED -> AiProviderHealth.RATE_LIMITED
                AiErrorCode.UNCONFIGURED -> AiProviderHealth.UNCONFIGURED
                AiErrorCode.TIMEOUT, AiErrorCode.SERVICE_UNAVAILABLE -> AiProviderHealth.DEGRADED
                else -> AiProviderHealth.ERROR
            }
            adminRepository.updateProviderHealth(primary, primaryHealth, "${primary.wireValue}: ${primaryError.code} - ${primaryError.message}")
        }

        // 5. Controlled Backup Failover (only if backup enabled and valid)
        val canTryBackup = cfg.backupProviderEnabled &&
            (backup != AiProviderKind.OPENAI || cfg.hasValidOpenAiProxy() || openAiClient !is OpenAiProxyClient)

        var backupError: AiProviderException? = null
        if (canTryBackup) {
            val backupReq = primaryReq.copy(modelName = modelFor(backup, cfg))
            val backupResult = attemptProvider(clientFor(backup), backupReq, cfg.timeoutMs, maxRetries = 0, onStreamChunk = onStreamChunk)
            if (backupResult.isSuccess) {
                val text = backupResult.getOrThrow()
                val latency = System.currentTimeMillis() - startMs
                val tokens = AiAdminRepository.estimateTokens(systemInstruction + sanitized.sanitizedText, text)
                adminRepository.updateProviderHealth(backup, AiProviderHealth.ONLINE, null)
                adminRepository.recordTelemetry(
                    userId = safeUserId,
                    assistantMode = assistantMode.wireValue,
                    cropId = cropId,
                    provider = backup,
                    model = backupReq.modelName,
                    fallbackUsed = true,
                    success = true,
                    errorCode = primaryError.code.name,
                    errorSummary = "Failover from ${primary.wireValue}: ${primaryError.message}",
                    latencyMs = latency,
                    estimatedTokens = tokens
                )
                return AiGenerationResult(
                    success = true,
                    text = text,
                    providerUsed = backup,
                    modelUsed = backupReq.modelName,
                    fallbackUsed = true,
                    fallbackReason = primaryError.code.name,
                    latencyMs = latency,
                    estimatedTokens = tokens,
                    citations = if (cfg.enableRagCitations) citations else emptyList(),
                    shouldSuggestGoogleSearch = shouldSuggestGoogleSearch
                )
            } else {
                backupError = backupResult.exceptionOrNull() as? AiProviderException
                    ?: AiProviderException(backup, AiErrorCode.UNKNOWN, "Backup provider failed")
                adminRepository.updateProviderHealth(
                    backup,
                    if (backupError.code == AiErrorCode.UNCONFIGURED) AiProviderHealth.UNCONFIGURED else AiProviderHealth.ERROR,
                    "${backup.wireValue}: ${backupError.code} - ${backupError.message}"
                )
            }
        }

        // 6. Both unavailable / failed -> Transparent localized failure (never mock AI answers!)
        val latency = System.currentTimeMillis() - startMs
        val effectiveError = backupError ?: primaryError
        val combinedReason = buildString {
            append("${primary.wireValue}=${primaryError.code}")
            if (backupError != null) append(", ${backup.wireValue}=${backupError.code}")
        }
        adminRepository.recordTelemetry(
            userId = safeUserId,
            assistantMode = assistantMode.wireValue,
            cropId = cropId,
            provider = primary,
            model = primaryReq.modelName,
            fallbackUsed = canTryBackup,
            success = false,
            errorCode = primaryError.code.name,
            errorSummary = "$combinedReason (${primaryError.message})",
            latencyMs = latency,
            estimatedTokens = 0
        )

        val userFriendlyError = when (effectiveError.code) {
            AiErrorCode.NETWORK_OFFLINE -> when (language) {
                AppLanguage.BENGALI -> "ইন্টারনেট সংযোগ বিচ্ছিন্ন রয়েছে। নেটওয়ার্ক পরীক্ষা করে আবার চেষ্টা করুন।"
                AppLanguage.HINDI -> "इंटरनेट कनेक्शन उपलब्ध नहीं है। नेटवर्क जांचकर पुनः प्रयास करें।"
                AppLanguage.ENGLISH -> "You appear offline or network is unreachable. Check internet and tap Retry."
            }
            AiErrorCode.TIMEOUT -> when (language) {
                AppLanguage.BENGALI -> "AI সার্ভার সাড়া দিতে বেশি সময় নিচ্ছে (Timeout)। অনুগ্রহ করে আবার চেষ্টা করুন।"
                AppLanguage.HINDI -> "AI सर्वर से उत्तर मिलने में समय लग रहा है (Timeout)। कृपया पुनः प्रयास करें।"
                AppLanguage.ENGLISH -> "AI service timed out while responding. Please tap Retry."
            }
            AiErrorCode.QUOTA_EXCEEDED -> when (language) {
                AppLanguage.BENGALI -> "AI পরিষেবার সাময়িক কোটা পূর্ণ হয়েছে এবং ব্যাকআপ প্রোভাইডার উপলব্ধ নেই। একটু পর আবার চেষ্টা করুন।"
                AppLanguage.HINDI -> "AI सेवा का कोटा अस्थायी रूप से पूर्ण हो गया है और बैकअप उपलब्ध नहीं है। कुछ देर बाद पुनः प्रयास करें।"
                AppLanguage.ENGLISH -> "AI quota limit temporarily reached and backup AI is unavailable. Please retry shortly."
            }
            else -> when (language) {
                AppLanguage.BENGALI -> "এই মুহূর্তে AI পরিষেবা থেকে উত্তর পাওয়া যায়নি। নিচের 'পুনরায় চেষ্টা করুন' বোতামে ট্যাপ করুন বা বিশেষজ্ঞ ডিরেক্টরি দেখুন।"
                AppLanguage.HINDI -> "इस समय AI सेवा से उत्तर प्राप्त नहीं हो सका। कृपया 'पुनः प्रयास करें' पर टैप करें या कृषि विशेषज्ञ से संपर्क करें।"
                AppLanguage.ENGLISH -> "AI service is currently unavailable. Please tap Retry below or consult Verified Experts."
            }
        }

        return AiGenerationResult(
            success = false,
            providerUsed = primary,
            modelUsed = primaryReq.modelName,
            fallbackUsed = canTryBackup,
            latencyMs = latency,
            failure = AiFailureInfo(
                code = effectiveError.code,
                userMessage = userFriendlyError,
                technicalReason = AiSecurityGuard.redactTechnicalError(combinedReason),
                primaryAttempted = primary,
                backupAttempted = if (canTryBackup) backup else null,
                retryAllowed = true
            )
        )
    }

    private suspend fun attemptProvider(
        client: AiBackendClient,
        request: AiBackendRequest,
        timeoutMs: Long,
        maxRetries: Int,
        onStreamChunk: ((String) -> Unit)?
    ): Result<String> {
        val attempts = 1 + maxRetries.coerceIn(0, 1)
        var lastEx: AiProviderException? = null
        for (attempt in 1..attempts) {
            try {
                val text = withTimeout(timeoutMs) {
                    if (onStreamChunk != null) {
                        val sb = StringBuilder()
                        client.generateStream(request).collect { chunk ->
                            sb.append(chunk)
                            onStreamChunk(sb.toString())
                        }
                        sb.toString().trim()
                    } else {
                        client.generate(request).trim()
                    }
                }
                if (text.isBlank()) {
                    throw AiProviderException(client.providerKind, AiErrorCode.SERVICE_UNAVAILABLE, "Blank response", isTransient = true)
                }
                return Result.success(text)
            } catch (e: CancellationException) {
                if (e is TimeoutCancellationException) {
                    lastEx = AiProviderException(client.providerKind, AiErrorCode.TIMEOUT, "Timed out after ${timeoutMs}ms", isTransient = true)
                } else {
                    throw e
                }
            } catch (e: AiProviderException) {
                lastEx = e
            } catch (e: Exception) {
                lastEx = AiProviderException(client.providerKind, AiErrorCode.UNKNOWN, e.message ?: "Error", isTransient = false)
            }

            if (lastEx?.isTransient != true || attempt >= attempts) break
        }
        return Result.failure(lastEx ?: AiProviderException(client.providerKind, AiErrorCode.UNKNOWN, "Provider failed"))
    }
}
