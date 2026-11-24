package com.example.data.ai

import android.graphics.Bitmap
import com.example.data.model.AppLanguage
import com.example.util.scaleBitmapBounded
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap

data class SanitizedPromptResult(
    val isValid: Boolean,
    val sanitizedText: String,
    val blockedReason: String? = null
)

data class ValidatedBitmapPayload(
    val bitmap: Bitmap,
    val jpegBytes: ByteArray
)

/**
 * Enforces input validation, prompt-injection neutralization, image size limits,
 * sliding-window per-user rate limiting, and privacy-safe log redaction.
 */
object AiSecurityGuard {
    const val MAX_QUERY_LENGTH = 2000
    const val MAX_IMAGE_DIM = 1280
    const val MAX_IMAGE_BYTES = 2 * 1024 * 1024 // 2 MB after JPEG compression

    private val injectionPatterns = listOf(
        Regex("ignore\\s+(all\\s+)?(previous|above|prior)\\s+instructions?", RegexOption.IGNORE_CASE),
        Regex("reveal\\s+(your\\s+)?(system\\s+prompt|api\\s+key|secret|instructions)", RegexOption.IGNORE_CASE),
        Regex("bypass\\s+(safety|pesticide|rules|security)", RegexOption.IGNORE_CASE),
        Regex("</?system>", RegexOption.IGNORE_CASE)
    )

    private val perUserTimestamps = ConcurrentHashMap<String, MutableList<Long>>()

    fun sanitizeUserInput(raw: String, hasImage: Boolean, language: AppLanguage): SanitizedPromptResult {
        val cleaned = raw
            .replace("\u0000", "")
            .replace(Regex("[\\r\\n]{3,}"), "\n\n")
            .trim()

        if (cleaned.isBlank() && !hasImage) {
            val msg = when (language) {
                AppLanguage.BENGALI -> "দয়া করে আপনার প্রশ্ন লিখুন বা একটি ছবি যুক্ত করুন।"
                AppLanguage.HINDI -> "कृपया अपना प्रश्न लिखें या कोई फ़ोटो संलग्न करें।"
                AppLanguage.ENGLISH -> "Please enter a question or attach an image."
            }
            return SanitizedPromptResult(false, "", msg)
        }

        if (cleaned.length > MAX_QUERY_LENGTH) {
            val msg = when (language) {
                AppLanguage.BENGALI -> "প্রশ্নটি খুব দীর্ঘ (সর্বোচ্চ $MAX_QUERY_LENGTH অক্ষর)। দয়া করে সংক্ষেপে লিখুন।"
                AppLanguage.HINDI -> "प्रश्न बहुत लंबा है (अधिकतम $MAX_QUERY_LENGTH अक्षर)। कृपया संक्षेप में लिखें।"
                AppLanguage.ENGLISH -> "Query exceeds maximum length of $MAX_QUERY_LENGTH characters. Please shorten your prompt."
            }
            return SanitizedPromptResult(false, "", msg)
        }

        // Neutralize control/prompt-injection directives rather than allowing role breakout
        var safeText = cleaned
        for (pattern in injectionPatterns) {
            if (pattern.containsMatchIn(safeText)) {
                safeText = safeText.replace(pattern, "[redacted-directive]")
            }
        }
        return SanitizedPromptResult(true, safeText)
    }

    fun validateAndCompressBitmap(bitmap: Bitmap?, language: AppLanguage): Pair<ValidatedBitmapPayload?, String?> {
        if (bitmap == null) return null to null
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) {
            val err = when (language) {
                AppLanguage.BENGALI -> "ছবিটি বৈধ নয়। অনুগ্রহ করে আবার ছবি নির্বাচন করুন।"
                AppLanguage.HINDI -> "चयनित फ़ोटो अमान्य है। कृपया दूसरी फ़ोटो चुनें।"
                AppLanguage.ENGLISH -> "Selected image is invalid or corrupted. Please choose another photo."
            }
            return null to err
        }

        val bounded = scaleBitmapBounded(bitmap, MAX_IMAGE_DIM)
        var quality = 85
        var bytes: ByteArray
        do {
            val baos = ByteArrayOutputStream()
            bounded.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            bytes = baos.toByteArray()
            quality -= 15
        } while (bytes.size > MAX_IMAGE_BYTES && quality >= 40)

        if (bytes.size > MAX_IMAGE_BYTES) {
            val err = when (language) {
                AppLanguage.BENGALI -> "ছবির আকার অনেক বড়। দয়া করে ছোট বা পরিষ্কার ছবি আপলোড করুন।"
                AppLanguage.HINDI -> "फ़ोटो का आकार बहुत बड़ा है। कृपया छोटी फ़ोटो अपलोड करें।"
                AppLanguage.ENGLISH -> "Image size exceeds safe limit. Please upload a smaller photo."
            }
            return null to err
        }

        return ValidatedBitmapPayload(bounded, bytes) to null
    }

    @Synchronized
    fun checkAndRecordRateLimit(
        userId: String,
        maxPerMinute: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        val safeCap = maxPerMinute.coerceIn(1, 60)
        val bucket = perUserTimestamps.getOrPut(userId.ifBlank { "guest" }) { mutableListOf() }
        val cutoff = nowMillis - 60_000L
        bucket.removeAll { it < cutoff }
        if (bucket.size >= safeCap) {
            return false
        }
        bucket.add(nowMillis)
        return true
    }

    fun clearRateLimitsForTest() {
        perUserTimestamps.clear()
    }

    fun redactTechnicalError(raw: String?): String {
        if (raw.isNullOrBlank()) return "Unknown error"
        return raw
            .replace(Regex("AIza[0-9A-Za-z\\-_]{20,}"), "[REDACTED_KEY]")
            .replace(Regex("sk-[0-9A-Za-z\\-_]{16,}"), "[REDACTED_KEY]")
            .replace(Regex("Bearer\\s+[A-Za-z0-9\\-._~+/]+=*", RegexOption.IGNORE_CASE), "Bearer [REDACTED]")
            .take(180)
    }
}
