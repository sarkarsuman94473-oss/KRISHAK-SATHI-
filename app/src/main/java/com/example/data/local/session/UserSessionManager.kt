package com.example.data.local.session

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persistent User Session Manager for KRISHAK SATHI.
 * Ensures that once a user signs in, the session is reliably persisted across
 * app restarts, process termination, and cold starts without repeated login prompts or flickering.
 */
data class UserSession(
    val userId: String,
    val userName: String,
    val email: String? = null,
    val phone: String? = null,
    val role: String = ROLE_CUSTOMER,
    val authProvider: String = PROVIDER_GOOGLE,
    val loginTimestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val ROLE_CUSTOMER = "CUSTOMER"
        const val ROLE_FARMER = "FARMER"
        const val ROLE_NURSERY_OWNER = "NURSERY_OWNER"
        const val ROLE_EXPERT = "EXPERT"
        const val ROLE_ADMIN = "ADMIN"

        const val PROVIDER_GOOGLE = "GOOGLE"
        const val PROVIDER_FARMER_DIRECT = "FARMER_DIRECT"
        const val PROVIDER_FIREBASE = "FIREBASE"
    }
}

object UserSessionManager {
    const val ROLE_CUSTOMER = "CUSTOMER"
    const val ROLE_FARMER = "FARMER"
    const val ROLE_NURSERY_OWNER = "NURSERY_OWNER"
    const val ROLE_EXPERT = "EXPERT"
    const val ROLE_ADMIN = "ADMIN"

    const val ADMIN_EMAIL = "sarkarsuman94473@gmail.com"
    private const val PREF_NAME = "krishak_sathi_user_session"
    private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
    private const val KEY_USER_ID = "key_user_id"
    private const val KEY_USER_NAME = "key_user_name"
    private const val KEY_USER_EMAIL = "key_user_email"
    private const val KEY_USER_PHONE = "key_user_phone"
    private const val KEY_USER_ROLE = "key_user_role"
    private const val KEY_AUTH_PROVIDER = "key_auth_provider"
    private const val KEY_LOGIN_TIME = "key_login_time"
    private const val KEY_PRIVACY_CONSENT_AT = "key_privacy_consent_at"
    private const val KEY_SELLER_AGREEMENT_VERSION = "key_seller_agreement_version"
    private const val KEY_SELLER_AGREEMENT_AT = "key_seller_agreement_at"
    const val CURRENT_SELLER_AGREEMENT_VERSION = "SELLER_TERMS_2026_V1"

    private val _sessionState = MutableStateFlow<UserSession?>(null)
    val sessionState: StateFlow<UserSession?> = _sessionState.asStateFlow()

    private var isInitialized = false

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Initializes the session cache from disk on app start.
     */
    fun init(context: Context): UserSession? {
        val prefs = getPrefs(context)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val session = if (isLoggedIn) {
            val userId = prefs.getString(KEY_USER_ID, null)
            val userName = prefs.getString(KEY_USER_NAME, "Krishak") ?: "Krishak"
            val email = prefs.getString(KEY_USER_EMAIL, null)
            val phone = prefs.getString(KEY_USER_PHONE, null)
            val role = prefs.getString(KEY_USER_ROLE, UserSession.ROLE_FARMER) ?: UserSession.ROLE_FARMER
            val provider = prefs.getString(KEY_AUTH_PROVIDER, UserSession.PROVIDER_GOOGLE) ?: UserSession.PROVIDER_GOOGLE
            val time = prefs.getLong(KEY_LOGIN_TIME, System.currentTimeMillis())

            if (!userId.isNullOrBlank()) {
                UserSession(
                    userId = userId,
                    userName = userName,
                    email = email,
                    phone = phone,
                    role = role,
                    authProvider = provider,
                    loginTimestamp = time
                )
            } else null
        } else null

        _sessionState.value = session
        isInitialized = true
        return session
    }

    fun isLoggedIn(context: Context): Boolean {
        if (!isInitialized) {
            init(context)
        }
        return _sessionState.value != null
    }

    fun getSession(context: Context): UserSession? {
        if (!isInitialized) {
            return init(context)
        }
        return _sessionState.value
    }

    /**
     * Securely stores user credentials to survive app termination.
     */
    fun saveSession(context: Context, session: UserSession) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_USER_NAME, session.userName)
            .putString(KEY_USER_EMAIL, session.email)
            .putString(KEY_USER_PHONE, session.phone)
            .putString(KEY_USER_ROLE, session.role)
            .putString(KEY_AUTH_PROVIDER, session.authProvider)
            .putLong(KEY_LOGIN_TIME, session.loginTimestamp)
            .apply()

        _sessionState.value = session
    }

    fun recordPrivacyConsent(context: Context, timestampMillis: Long = System.currentTimeMillis()) {
        getPrefs(context).edit().putLong(KEY_PRIVACY_CONSENT_AT, timestampMillis).apply()
    }

    fun getPrivacyConsentTimestamp(context: Context): Long =
        getPrefs(context).getLong(KEY_PRIVACY_CONSENT_AT, 0L)

    fun recordSellerAgreementAcceptance(
        context: Context,
        version: String = CURRENT_SELLER_AGREEMENT_VERSION,
        timestampMillis: Long = System.currentTimeMillis()
    ) {
        getPrefs(context).edit()
            .putString(KEY_SELLER_AGREEMENT_VERSION, version)
            .putLong(KEY_SELLER_AGREEMENT_AT, timestampMillis)
            .apply()
    }

    fun hasAcceptedSellerAgreement(
        context: Context,
        version: String = CURRENT_SELLER_AGREEMENT_VERSION
    ): Boolean {
        val prefs = getPrefs(context)
        return prefs.getString(KEY_SELLER_AGREEMENT_VERSION, null) == version &&
            prefs.getLong(KEY_SELLER_AGREEMENT_AT, 0L) > 0L
    }

    /**
     * Updates the user's role and persists to disk.
     */
    fun updateUserRole(context: Context, newRole: String) {
        val current = getSession(context) ?: return
        val updated = current.copy(role = newRole)
        saveSession(context, updated)
    }

    /**
     * Check if user is the designated admin.
     */
    fun isAdmin(context: Context): Boolean {
        val session = getSession(context) ?: return false
        return session.role == ROLE_ADMIN || session.email?.trim()?.equals(ADMIN_EMAIL, ignoreCase = true) == true
    }

    /**
     * Check if user is an approved nursery owner seller.
     */
    fun isNurseryOwner(context: Context): Boolean {
        val session = getSession(context) ?: return false
        return session.role == ROLE_NURSERY_OWNER
    }

    /**
     * Check if user is a standard customer/farmer.
     */
    fun isCustomer(context: Context): Boolean {
        val session = getSession(context) ?: return true
        return session.role == ROLE_CUSTOMER || session.role == ROLE_FARMER
    }

    /**
     * Explicit sign-out clears the disk credentials.
     */
    fun clearSession(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().clear().apply()
        _sessionState.value = null
    }
}
