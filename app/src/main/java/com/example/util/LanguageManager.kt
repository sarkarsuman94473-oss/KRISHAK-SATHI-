package com.example.util

import android.content.Context
import com.example.data.model.AppLanguage
import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "krishak_sathi_language_prefs"
    private const val KEY_LANGUAGE = "selected_app_language"

    /**
     * Gets the persisted language preference.
     * If first-time user, auto-detects device language:
     * - Bengali if device locale starts with "bn"
     * - Hindi if device locale starts with "hi"
     * - English as standard fallback
     */
    fun getLanguage(context: Context): AppLanguage {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCode = prefs.getString(KEY_LANGUAGE, null)
        if (!savedCode.isNullOrBlank()) {
            return AppLanguage.values().firstOrNull { it.code.equals(savedCode, ignoreCase = true) }
                ?: AppLanguage.BENGALI
        }

        // Auto-detect from system locale on initial run
        val systemLang = Locale.getDefault().language.lowercase()
        val detected = when {
            systemLang.startsWith("bn") -> AppLanguage.BENGALI
            systemLang.startsWith("hi") -> AppLanguage.HINDI
            else -> AppLanguage.ENGLISH
        }

        // Persist default choice
        saveLanguage(context, detected)
        return detected
    }

    /**
     * Persists the selected language preference so it survives app restarts,
     * logout, login, and background process termination.
     */
    fun saveLanguage(context: Context, language: AppLanguage) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }
}
