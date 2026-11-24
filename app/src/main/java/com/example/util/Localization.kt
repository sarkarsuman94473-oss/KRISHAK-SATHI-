package com.example.util

import com.example.data.model.AppLanguage

/**
 * Global Localization helper for Krishak Sathi.
 * Dispatches translation keys across dedicated language files:
 * - BengaliTranslations.kt (BengaliStrings)
 * - HindiTranslations.kt (HindiStrings)
 * - EnglishTranslations.kt (EnglishStrings)
 */
object L10n {
    /**
     * Translate key into user's active language with fallback.
     */
    fun t(key: String, lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.BENGALI -> BengaliStrings[key] ?: EnglishStrings[key] ?: key
            AppLanguage.HINDI -> HindiStrings[key] ?: EnglishStrings[key] ?: key
            AppLanguage.ENGLISH -> EnglishStrings[key] ?: BengaliStrings[key] ?: key
        }
    }

    /**
     * Formatted translation (e.g. "Welcome, %s")
     */
    fun format(key: String, lang: AppLanguage, vararg args: Any): String {
        val template = t(key, lang)
        return try {
            String.format(template, *args)
        } catch (_: Exception) {
            template
        }
    }
}
