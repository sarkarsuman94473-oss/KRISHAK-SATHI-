package com.example.data.ai

import com.example.data.datasource.CropMasterDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.CropMismatchSuggestion
import com.example.data.model.MasterCrop

/**
 * Guarantees canonical cropId resolution and detects crop-context vs query-crop mismatches
 * so Krishak Sathi never mixes advice or conversation history across crops.
 */
object CropContextResolver {

    private val aliasMap: Map<String, List<String>> = mapOf(
        "rice_paddy" to listOf("rice", "paddy", "aman", "boro", "aus", "gobindobhog", "ধান", "ধানের", "আমন", "বোরো", "धान", "चावल"),
        "potato" to listOf("potato", "kufri", "jyoti", "chandramukhi", "আলু", "আলুর", "आलू"),
        "tomato" to listOf("tomato", "টমেটো", "টমেটোর", "टमाटर"),
        "tea" to listOf("tea", "tea garden", "cha bagan", "tea bush", "চা", "চায়ের", "চা গাছ", "চা বাগান", "चाय"),
        "cabbage" to listOf("cabbage", "বাঁধাকপি", "বাঁধাকপির", "पत्तागोभी", "बंदगोभी"),
        "cauliflower" to listOf("cauliflower", "ফুলকপি", "ফুলকপির", "फूलगोभी"),
        "chilli" to listOf("chilli", "chili", "pepper", "লঙ্কা", "মরিচ", "লঙ্কার", "মরিচের", "मिर्च", "हरी मिर्च"),
        "mango" to listOf("mango", "himsagar", "langra", "fazli", "আম", "আমের", "आम"),
        "wheat" to listOf("wheat", "গম", "গমের", "गेहूं", "गेहूँ"),
        "mustard" to listOf("mustard", "সরষে", "সর্ষে", "সরিষা", "सरसों"),
        "brinjal" to listOf("brinjal", "eggplant", "বেগুন", "বেগুনের", "बैंगन"),
        "okra" to listOf("okra", "ladyfinger", "bhindi", "ঢেঁড়স", "ভেন্ডি", "भिंडी"),
        "banana" to listOf("banana", "কলা", "কলার", "केला"),
        "papaya" to listOf("papaya", "পেঁপে", "পपीता", "पपीता"),
        "guava" to listOf("guava", "পেয়ারা", "अमरूद"),
        "onion" to listOf("onion", "পেঁয়াজ", "পিয়াজ", "प्याज"),
        "garlic" to listOf("garlic", "রসুন", "लहसुन"),
        "jute" to listOf("jute", "পাট", "পাটের", "जूट", "पटसन"),
        "maize" to listOf("maize", "corn", "ভুট্টা", "मक्का"),
        "rose" to listOf("rose", "গোলাপ", "गुलाब")
    )

    fun resolveByCropIdOrName(cropIdOrName: String?): MasterCrop? {
        val raw = cropIdOrName?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val crops = CropMasterDatabase.crops

        // 1. Exact cropId match first (highest precedence)
        crops.firstOrNull { it.cropId.equals(raw, ignoreCase = true) }?.let { return it }

        // 2. Exact display name match across BN / HI / EN
        crops.firstOrNull { c ->
            c.nameEn.equals(raw, ignoreCase = true) ||
                c.nameBn.equals(raw, ignoreCase = true) ||
                c.nameHi.equals(raw, ignoreCase = true)
        }?.let { return it }

        // 3. Alias lookup
        val lower = raw.lowercase()
        for ((canonicalId, aliases) in aliasMap) {
            if (aliases.any { lower == it || (it.length >= 3 && lower.contains(it)) }) {
                crops.firstOrNull { it.cropId.equals(canonicalId, ignoreCase = true) }?.let { return it }
            }
        }

        // 4. Fallback substring match on crop names
        return crops.firstOrNull { c ->
            c.nameEn.contains(raw, ignoreCase = true) ||
                c.nameBn.contains(raw, ignoreCase = true) ||
                c.nameHi.contains(raw, ignoreCase = true)
        }
    }

    /**
     * Scans natural language query for an explicit crop mention (e.g. farmer asks about Rice while Potato is selected).
     */
    fun detectCropMentionInQuery(query: String): MasterCrop? {
        val lower = query.lowercase()
        val crops = CropMasterDatabase.crops

        // Word/phrase boundary matching for English & direct Unicode substring for BN/HI
        for ((canonicalId, aliases) in aliasMap) {
            for (alias in aliases) {
                val matched = if (alias.all { it.code < 128 }) {
                    Regex("\\b${Regex.escape(alias)}\\b", RegexOption.IGNORE_CASE).containsMatchIn(lower)
                } else {
                    lower.contains(alias)
                }
                if (matched) {
                    crops.firstOrNull { it.cropId.equals(canonicalId, ignoreCase = true) }?.let { return it }
                }
            }
        }

        // Scan remaining catalog names cleanly (prioritize longer names so short tokens don't false-match)
        return crops
            .sortedByDescending { it.nameEn.length }
            .firstOrNull { c ->
                val enMatch = c.nameEn.length >= 4 &&
                    Regex("\\b${Regex.escape(c.nameEn.lowercase())}\\b").containsMatchIn(lower)
                val bnClean = c.nameBn.substringBefore("(").trim()
                val bnMatch = bnClean.length >= 2 && lower.contains(bnClean)
                val hiClean = c.nameHi.substringBefore("(").trim()
                val hiMatch = hiClean.length >= 2 && lower.contains(hiClean)
                enMatch || bnMatch || hiMatch
            }
    }

    fun detectCropMismatch(
        selectedCrop: MasterCrop?,
        query: String,
        language: AppLanguage
    ): CropMismatchSuggestion? {
        if (selectedCrop == null) return null
        val mentioned = detectCropMentionInQuery(query) ?: return null
        if (mentioned.cropId.equals(selectedCrop.cropId, ignoreCase = true)) return null

        val selectedName = selectedCrop.getName(language)
        val detectedName = mentioned.getName(language)
        val message = when (language) {
            AppLanguage.BENGALI -> "আপনার বর্তমান নির্বাচিত ফসল \"$selectedName\", কিন্তু প্রশ্নে \"$detectedName\"-এর কথা বলা হয়েছে। আপনি কি \"$detectedName\" ফসলের জন্য সঠিক পরামর্শ চান নাকি \"$selectedName\" হিসেবেই রাখবেন?"
            AppLanguage.HINDI -> "आपकी वर्तमान चयनित फसल \"$selectedName\" है, लेकिन प्रश्न \"$detectedName\" के बारे में है। क्या आप \"$detectedName\" के लिए सलाह चाहते हैं या \"$selectedName\" ही रखना चाहते हैं?"
            AppLanguage.ENGLISH -> "Your current crop context is \"$selectedName\", while your question mentions \"$detectedName\". Switch conversation to \"$detectedName\" so advice stays accurate, or keep \"$selectedName\"?"
        }

        return CropMismatchSuggestion(
            selectedCropId = selectedCrop.cropId,
            selectedCropName = selectedName,
            detectedCropId = mentioned.cropId,
            detectedCropName = detectedName,
            confirmationMessage = message
        )
    }
}
