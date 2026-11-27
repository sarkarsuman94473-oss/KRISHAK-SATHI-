package com.example.data.local.session

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppUserRole
import com.example.data.model.DashboardMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persistent User Session Manager for KRISHAK SATHI.
 * Preserves one user identity per account, multiple authorized role memberships,
 * and active dashboard switching (Farmer Dashboard <-> Partner Dashboard) without logout.
 */
data class UserSession(
    val userId: String,
    val userName: String,
    val email: String? = null,
    val phone: String? = null,
    val role: String = ROLE_CUSTOMER,
    val authProvider: String = PROVIDER_GOOGLE,
    val loginTimestamp: Long = System.currentTimeMillis(),
    val approvedRolesCsv: String = AppUserRole.FARMER_CUSTOMER.code,
    val activeDashboardMode: String = DashboardMode.FARMER_DASHBOARD.code
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
    private const val KEY_APPROVED_ROLES_CSV = "key_approved_roles_csv"
    private const val KEY_ACTIVE_DASHBOARD_MODE = "key_active_dashboard_mode"
    private const val KEY_PRIVACY_CONSENT_AT = "key_privacy_consent_at"
    private const val KEY_SELLER_AGREEMENT_VERSION = "key_seller_agreement_version"
    private const val KEY_SELLER_AGREEMENT_AT = "key_seller_agreement_at"
    private const val KEY_TOKEN_VALIDATED_AT = "key_token_validated_at"
    const val CURRENT_SELLER_AGREEMENT_VERSION = "SELLER_TERMS_2026_V1"

    private val _sessionState = MutableStateFlow<UserSession?>(null)
    val sessionState: StateFlow<UserSession?> = _sessionState.asStateFlow()

    private var isInitialized = false

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun isAdminEmail(email: String?): Boolean {
        return email?.trim()?.equals(ADMIN_EMAIL, ignoreCase = true) == true
    }

    private fun parseApprovedRoles(
        csv: String,
        legacyRole: String,
        isAdminAccount: Boolean,
        isCloudAccount: Boolean
    ): Set<AppUserRole> {
        val roles = mutableSetOf(AppUserRole.FARMER_CUSTOMER)
        if (!isCloudAccount) {
            return roles
        }
        csv.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { raw ->
                val parsed = AppUserRole.fromCode(raw)
                if (parsed != AppUserRole.ADMINISTRATOR) {
                    roles.add(parsed)
                }
            }
        if (legacyRole == ROLE_NURSERY_OWNER) {
            roles.add(AppUserRole.NURSERY_SELLER)
        }
        if (isAdminAccount) {
            roles.add(AppUserRole.ADMINISTRATOR)
        }
        return roles
    }

    private fun sanitizeSession(session: UserSession): UserSession? {
        val cleanUid = session.userId.trim()
        if (cleanUid.isBlank()) return null

        val cleanName = session.userName.trim().ifBlank { "Krishak" }
        val cleanProvider = session.authProvider.trim().ifBlank { UserSession.PROVIDER_FARMER_DIRECT }

        return if (cleanProvider == UserSession.PROVIDER_FARMER_DIRECT) {
            // Local Quick Login must remain strictly offline-local with standard farmer privileges only.
            session.copy(
                userId = cleanUid,
                userName = cleanName,
                email = null,
                role = ROLE_FARMER,
                authProvider = UserSession.PROVIDER_FARMER_DIRECT,
                approvedRolesCsv = AppUserRole.FARMER_CUSTOMER.code,
                activeDashboardMode = DashboardMode.FARMER_DASHBOARD.code
            )
        } else {
            val cleanEmail = session.email?.trim()?.takeIf { it.isNotBlank() }
            val adminVerified = isAdminEmail(cleanEmail)
            val normalizedRole = when {
                adminVerified -> ROLE_ADMIN
                session.role == ROLE_ADMIN -> ROLE_FARMER
                session.role in setOf(ROLE_CUSTOMER, ROLE_FARMER, ROLE_NURSERY_OWNER, ROLE_EXPERT) -> session.role
                else -> ROLE_FARMER
            }
            val parsedRoles = parseApprovedRoles(
                csv = session.approvedRolesCsv,
                legacyRole = normalizedRole,
                isAdminAccount = adminVerified,
                isCloudAccount = cleanProvider == UserSession.PROVIDER_GOOGLE || cleanProvider == UserSession.PROVIDER_FIREBASE
            )
            val hasPartnerAccess = parsedRoles.any { it.isPartnerRole }
            val requestedMode = DashboardMode.fromCode(session.activeDashboardMode)
            val safeMode = if (requestedMode == DashboardMode.PARTNER_DASHBOARD && hasPartnerAccess) {
                DashboardMode.PARTNER_DASHBOARD.code
            } else {
                DashboardMode.FARMER_DASHBOARD.code
            }
            session.copy(
                userId = cleanUid,
                userName = cleanName,
                email = cleanEmail,
                role = normalizedRole,
                authProvider = cleanProvider,
                approvedRolesCsv = parsedRoles.joinToString(",") { it.code },
                activeDashboardMode = safeMode
            )
        }
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
            val rolesCsv = prefs.getString(KEY_APPROVED_ROLES_CSV, AppUserRole.FARMER_CUSTOMER.code)
                ?: AppUserRole.FARMER_CUSTOMER.code
            val dashboardMode = prefs.getString(KEY_ACTIVE_DASHBOARD_MODE, DashboardMode.FARMER_DASHBOARD.code)
                ?: DashboardMode.FARMER_DASHBOARD.code

            if (!userId.isNullOrBlank()) {
                sanitizeSession(
                    UserSession(
                        userId = userId,
                        userName = userName,
                        email = email,
                        phone = phone,
                        role = role,
                        authProvider = provider,
                        loginTimestamp = time,
                        approvedRolesCsv = rolesCsv,
                        activeDashboardMode = dashboardMode
                    )
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
        val sanitized = sanitizeSession(session)
        if (sanitized == null) {
            clearSession(context)
            return
        }
        val prefs = getPrefs(context)
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, sanitized.userId)
            .putString(KEY_USER_NAME, sanitized.userName)
            .putString(KEY_USER_EMAIL, sanitized.email)
            .putString(KEY_USER_PHONE, sanitized.phone)
            .putString(KEY_USER_ROLE, sanitized.role)
            .putString(KEY_AUTH_PROVIDER, sanitized.authProvider)
            .putLong(KEY_LOGIN_TIME, sanitized.loginTimestamp)
            .putString(KEY_APPROVED_ROLES_CSV, sanitized.approvedRolesCsv)
            .putString(KEY_ACTIVE_DASHBOARD_MODE, sanitized.activeDashboardMode)
            .apply()

        _sessionState.value = sanitized
        isInitialized = true
    }

    fun recordPrivacyConsent(context: Context, timestampMillis: Long = System.currentTimeMillis()) {
        getPrefs(context).edit().putLong(KEY_PRIVACY_CONSENT_AT, timestampMillis).apply()
    }

    fun getPrivacyConsentTimestamp(context: Context): Long =
        getPrefs(context).getLong(KEY_PRIVACY_CONSENT_AT, 0L)

    fun recordTokenValidated(context: Context, timestampMillis: Long = System.currentTimeMillis()) {
        getPrefs(context).edit().putLong(KEY_TOKEN_VALIDATED_AT, timestampMillis).apply()
    }

    fun getLastTokenValidatedAt(context: Context): Long =
        getPrefs(context).getLong(KEY_TOKEN_VALIDATED_AT, 0L)

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
     * Updates the user's primary legacy role and persists to disk.
     * Local Quick Login accounts cannot be elevated, and ROLE_ADMIN requires the verified admin email.
     */
    fun updateUserRole(context: Context, newRole: String) {
        val current = getSession(context) ?: return
        if (current.authProvider == UserSession.PROVIDER_FARMER_DIRECT) return
        if (newRole == ROLE_ADMIN && !isAdminEmail(current.email)) return
        val currentRoles = getApprovedRoles(context).toMutableSet()
        if (newRole == ROLE_NURSERY_OWNER) {
            currentRoles.add(AppUserRole.NURSERY_SELLER)
        }
        val updated = current.copy(
            role = newRole,
            approvedRolesCsv = currentRoles.joinToString(",") { it.code }
        )
        saveSession(context, updated)
    }

    /**
     * Updates approved marketplace roles for an authenticated cloud user after Admin verification.
     * Non-admin users can NEVER hold ADMINISTRATOR role.
     */
    fun updateApprovedMarketplaceRoles(context: Context, approvedPartnerRoles: Set<AppUserRole>) {
        val current = getSession(context) ?: return
        if (!hasCloudFirebaseSession(context)) return
        val nextRoles = mutableSetOf(AppUserRole.FARMER_CUSTOMER)
        approvedPartnerRoles
            .filter { it.isPartnerRole && it != AppUserRole.ADMINISTRATOR }
            .forEach { nextRoles.add(it) }
        if (isAdminEmail(current.email)) {
            nextRoles.add(AppUserRole.ADMINISTRATOR)
        }
        val updatedLegacyRole = when {
            isAdminEmail(current.email) -> ROLE_ADMIN
            AppUserRole.NURSERY_SELLER in nextRoles -> ROLE_NURSERY_OWNER
            else -> current.role
        }
        saveSession(
            context,
            current.copy(
                role = updatedLegacyRole,
                approvedRolesCsv = nextRoles.joinToString(",") { it.code }
            )
        )
    }

    fun getApprovedRoles(context: Context): Set<AppUserRole> {
        val session = getSession(context) ?: return setOf(AppUserRole.FARMER_CUSTOMER)
        return parseApprovedRoles(
            csv = session.approvedRolesCsv,
            legacyRole = session.role,
            isAdminAccount = isAdmin(context),
            isCloudAccount = hasCloudFirebaseSession(context)
        )
    }

    fun hasApprovedPartnerRole(context: Context): Boolean {
        return getApprovedRoles(context).any { it.isPartnerRole }
    }

    fun getActiveDashboardMode(context: Context): DashboardMode {
        val session = getSession(context) ?: return DashboardMode.FARMER_DASHBOARD
        val requested = DashboardMode.fromCode(session.activeDashboardMode)
        return if (requested == DashboardMode.PARTNER_DASHBOARD && hasApprovedPartnerRole(context)) {
            DashboardMode.PARTNER_DASHBOARD
        } else {
            DashboardMode.FARMER_DASHBOARD
        }
    }

    /**
     * Switches between Farmer Dashboard and Partner Dashboard without logging out.
     * Returns false if the user attempts to switch to Partner Dashboard without any approved partner role.
     */
    fun switchDashboardMode(context: Context, targetMode: DashboardMode): Boolean {
        val current = getSession(context) ?: return false
        if (targetMode == DashboardMode.PARTNER_DASHBOARD && !hasApprovedPartnerRole(context)) {
            return false
        }
        saveSession(context, current.copy(activeDashboardMode = targetMode.code))
        return true
    }

    /**
     * Check if user is the designated admin with an authenticated cloud provider.
     */
    fun isAdmin(context: Context): Boolean {
        val session = getSession(context) ?: return false
        if (!hasCloudFirebaseSession(context)) return false
        return isAdminEmail(session.email)
    }

    /**
     * Check if user is an approved nursery owner seller with an authenticated cloud session.
     */
    fun isNurseryOwner(context: Context): Boolean {
        val session = getSession(context) ?: return false
        if (!hasCloudFirebaseSession(context)) return false
        return session.role == ROLE_NURSERY_OWNER ||
            AppUserRole.NURSERY_SELLER in getApprovedRoles(context)
    }

    /**
     * Check if user is signed in using Local Quick Login (offline-first local account).
     */
    fun isLocalQuickLogin(context: Context): Boolean {
        val session = getSession(context) ?: return false
        return session.authProvider == UserSession.PROVIDER_FARMER_DIRECT
    }

    /**
     * Check if user has an active Google / Firebase authenticated cloud session
     * capable of Firestore synchronization across devices.
     */
    fun hasCloudFirebaseSession(context: Context): Boolean {
        val session = getSession(context) ?: return false
        if (session.authProvider == UserSession.PROVIDER_FARMER_DIRECT) return false
        return session.authProvider == UserSession.PROVIDER_GOOGLE ||
            session.authProvider == UserSession.PROVIDER_FIREBASE
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
        isInitialized = true
    }
}
