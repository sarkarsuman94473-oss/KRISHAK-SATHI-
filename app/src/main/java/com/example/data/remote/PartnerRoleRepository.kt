package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.session.UserSessionManager
import com.example.data.model.AppUserRole
import com.example.data.model.DashboardMode
import com.example.data.model.PartnerRegistration
import com.example.data.model.PartnerRegistrationStatus
import com.example.data.model.UserRoleMembership
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

private const val PARTNER_ROLE_TAG = "PartnerRoleRepo"

/**
 * Single-App Multi-Role Marketplace Repository.
 * Manages:
 * - partner_registrations: Partner application lifecycle (PENDING -> UNDER_REVIEW -> APPROVED / REJECTED)
 * - user_roles: Authorized role memberships per account (one identity -> multiple approved partner roles)
 *
 * Security invariants:
 * - Users can NEVER select or grant themselves ADMINISTRATOR privileges.
 * - Platform verified badge & partner dashboard access are granted ONLY after verified Admin approval.
 * - Local Quick Login accounts remain strictly offline-local FARMER_CUSTOMER only.
 */
class PartnerRoleRepository(private val context: Context) {

    private val db: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.e(PARTNER_ROLE_TAG, "Failed to get Firestore instance: ${e.message}")
            null
        }
    }

    private val prefs = context.applicationContext.getSharedPreferences(
        "krishak_sathi_partner_roles",
        Context.MODE_PRIVATE
    )

    private val _registrationsFlow = MutableStateFlow<List<PartnerRegistration>>(emptyList())
    val allRegistrationsFlow: Flow<List<PartnerRegistration>> = _registrationsFlow.asStateFlow()

    private val _membershipsFlow = MutableStateFlow<Map<String, UserRoleMembership>>(emptyMap())
    val allMembershipsFlow: Flow<Map<String, UserRoleMembership>> = _membershipsFlow.asStateFlow()

    init {
        loadFromLocalDisk()
    }

    private fun loadFromLocalDisk() {
        try {
            val rawRegs = prefs.getString("cached_partner_registrations", null)
            if (!rawRegs.isNullOrBlank()) {
                val arr = JSONArray(rawRegs)
                val list = mutableListOf<PartnerRegistration>()
                for (i in 0 until arr.length()) {
                    list.add(jsonToRegistration(arr.getJSONObject(i)))
                }
                _registrationsFlow.value = list
            }
            val rawRoles = prefs.getString("cached_user_role_memberships", null)
            if (!rawRoles.isNullOrBlank()) {
                val arr = JSONArray(rawRoles)
                val map = mutableMapOf<String, UserRoleMembership>()
                for (i in 0 until arr.length()) {
                    val item = jsonToMembership(arr.getJSONObject(i))
                    if (item.userId.isNotBlank()) {
                        map[item.userId] = item
                    }
                }
                _membershipsFlow.value = map
            }
        } catch (e: Exception) {
            Log.e(PARTNER_ROLE_TAG, "Failed loading partner role cache: ${e.message}")
        }
    }

    private fun saveRegistrationsToDisk(list: List<PartnerRegistration>) {
        _registrationsFlow.value = list
        try {
            val arr = JSONArray()
            list.forEach { arr.put(registrationToJson(it)) }
            prefs.edit().putString("cached_partner_registrations", arr.toString()).apply()
        } catch (e: Exception) {
            Log.e(PARTNER_ROLE_TAG, "Failed saving partner registrations: ${e.message}")
        }
    }

    private fun saveMembershipsToDisk(map: Map<String, UserRoleMembership>) {
        _membershipsFlow.value = map
        try {
            val arr = JSONArray()
            map.values.forEach { arr.put(membershipToJson(it)) }
            prefs.edit().putString("cached_user_role_memberships", arr.toString()).apply()
        } catch (e: Exception) {
            Log.e(PARTNER_ROLE_TAG, "Failed saving user role memberships: ${e.message}")
        }
    }

    private fun syncSessionIfMatchingUser(userId: String, membership: UserRoleMembership) {
        val currentSession = UserSessionManager.getSession(context) ?: return
        if (currentSession.userId == userId && UserSessionManager.hasCloudFirebaseSession(context)) {
            UserSessionManager.updateApprovedMarketplaceRoles(
                context = context,
                approvedPartnerRoles = membership.resolvedRoles(includeAdmin = false)
                    .filter { it.isPartnerRole }
                    .toSet()
            )
        }
    }

    fun observeUserRoleMembership(userId: String): Flow<UserRoleMembership> {
        val cleanUid = userId.trim()
        val fallback = defaultMembershipFor(cleanUid)
        val firestore = db
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val isAdmin = currentUser?.email.equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true)

        return if (firestore != null && currentUser != null && cleanUid.isNotBlank() && (currentUser.uid == cleanUid || isAdmin)) {
            firestore.collection("user_roles").document(cleanUid)
                .snapshots()
                .map { snap ->
                    val remote = snap.toObject(UserRoleMembership::class.java)
                    val resolved = remote?.copy(id = snap.id, userId = remote.userId.ifBlank { cleanUid })
                        ?: _membershipsFlow.value[cleanUid]
                        ?: defaultMembershipFor(cleanUid)
                    val updatedMap = _membershipsFlow.value.toMutableMap()
                    updatedMap[cleanUid] = resolved
                    saveMembershipsToDisk(updatedMap)
                    syncSessionIfMatchingUser(cleanUid, resolved)
                    resolved
                }
                .catch { e ->
                    val fsErr = generateSequence(e) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && e is CancellationException) throw e
                    Log.w(PARTNER_ROLE_TAG, "Firestore observeUserRoleMembership deferred: ${fsErr?.code ?: e.message}")
                    emit(_membershipsFlow.value[cleanUid] ?: fallback)
                }
        } else {
            _membershipsFlow.map { map -> map[cleanUid] ?: fallback }
        }
    }

    fun observeUserRegistration(userId: String): Flow<PartnerRegistration?> {
        val cleanUid = userId.trim()
        val firestore = db
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val isAdmin = currentUser?.email.equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true)

        return if (firestore != null && currentUser != null && cleanUid.isNotBlank() && (currentUser.uid == cleanUid || isAdmin)) {
            firestore.collection("partner_registrations")
                .whereEqualTo("userId", cleanUid)
                .snapshots()
                .map { snap ->
                    val remote = snap.documents
                        .mapNotNull { doc ->
                            doc.toObject(PartnerRegistration::class.java)?.copy(id = doc.id)
                        }
                        .maxByOrNull { it.submittedAtMillis }
                    if (remote != null) {
                        val updated = _registrationsFlow.value.toMutableList()
                        val idx = updated.indexOfFirst { it.id == remote.id || it.userId == remote.userId }
                        if (idx >= 0) updated[idx] = remote else updated.add(0, remote)
                        saveRegistrationsToDisk(updated)
                        if (remote.isVerifiedPartner()) {
                            syncMembershipCacheFromApprovedReg(remote)
                        }
                        remote
                    } else {
                        _registrationsFlow.value.firstOrNull { it.userId == cleanUid }
                    }
                }
                .catch { e ->
                    val fsErr = generateSequence(e) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && e is CancellationException) throw e
                    Log.w(PARTNER_ROLE_TAG, "Firestore observeUserRegistration deferred: ${fsErr?.code ?: e.message}")
                    emit(_registrationsFlow.value.firstOrNull { it.userId == cleanUid })
                }
        } else {
            _registrationsFlow.map { list -> list.firstOrNull { it.userId == cleanUid } }
        }
    }

    fun observeAllRegistrations(): Flow<List<PartnerRegistration>> {
        val firestore = db
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        return if (firestore != null && currentUser != null) {
            val isAdmin = currentUser.email.equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true)
            val query = if (isAdmin) {
                firestore.collection("partner_registrations")
            } else {
                firestore.collection("partner_registrations")
                    .whereEqualTo("status", PartnerRegistrationStatus.APPROVED.code)
            }
            query.snapshots()
                .map { snap ->
                    val remoteList = snap.documents.mapNotNull { doc ->
                        doc.toObject(PartnerRegistration::class.java)?.copy(id = doc.id)
                    }
                    if (remoteList.isNotEmpty()) {
                        if (isAdmin) {
                            saveRegistrationsToDisk(remoteList)
                            remoteList
                        } else {
                            (_registrationsFlow.value + remoteList)
                                .associateBy { it.id.ifBlank { it.userId } }
                                .values
                                .filter {
                                    it.isVerifiedPartner() || it.userId == currentUser.uid
                                }
                                .toList()
                        }
                    } else {
                        _registrationsFlow.value
                    }
                }
                .catch { e ->
                    val fsErr = generateSequence(e) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && e is CancellationException) throw e
                    Log.w(PARTNER_ROLE_TAG, "Firestore observeAllRegistrations deferred: ${fsErr?.code ?: e.message}")
                    emit(_registrationsFlow.value)
                }
        } else {
            _registrationsFlow
        }
    }

    /**
     * Submits or updates a Partner Registration request.
     * - Strips any ADMINISTRATOR or FARMER_CUSTOMER role codes.
     * - Enforces initial/resubmitted status = PENDING, platformVerified = false, approvedRoles = emptyList().
     * - Sanitizes attachment names so permanent content:// / file:// URIs never enter cloud state.
     */
    suspend fun submitRegistration(reg: PartnerRegistration): Result<PartnerRegistration> {
        return try {
            val cleanUid = reg.userId.trim()
            if (cleanUid.isBlank()) {
                return Result.failure(IllegalArgumentException("User ID is required."))
            }
            val currentAuth = runCatching { Firebase.auth.currentUser }.getOrNull()
            if (currentAuth != null && currentAuth.uid != cleanUid) {
                return Result.failure(SecurityException("Cannot submit registration for another account."))
            }
            if (reg.businessName.trim().length < 2) {
                return Result.failure(IllegalArgumentException("Business name is required."))
            }
            if (reg.contactPhone.trim().length < 6) {
                return Result.failure(IllegalArgumentException("Valid contact number is required."))
            }
            if (reg.businessLocation.trim().length < 2) {
                return Result.failure(IllegalArgumentException("Business location is required."))
            }
            if (reg.serviceArea.trim().length < 2) {
                return Result.failure(IllegalArgumentException("Service area is required."))
            }

            // Reject non-partner roles & never allow self-selection of ADMINISTRATOR
            val sanitizedCategories = reg.serviceCategories
                .map { AppUserRole.fromCode(it) }
                .filter { it.isSelectableInPartnerRegistration && it != AppUserRole.ADMINISTRATOR }
                .distinct()
                .map { it.code }
            if (sanitizedCategories.isEmpty()) {
                return Result.failure(IllegalArgumentException("Select at least one service category."))
            }

            val safeDocNames = reg.verificationDocumentNames
                .mapNotNull { sanitizeDocumentDisplayName(it) }
                .distinct()
                .take(5)

            val previous = _registrationsFlow.value
            val existing = previous.firstOrNull {
                (reg.id.isNotBlank() && it.id == reg.id) || it.userId == cleanUid
            }
            val effectiveId = reg.id.ifBlank { existing?.id.orEmpty() }
                .ifBlank { "partner_${cleanUid.take(28)}_${System.currentTimeMillis()}" }

            val now = System.currentTimeMillis()
            val sanitizedReg = reg.copy(
                id = effectiveId,
                userId = cleanUid,
                applicantName = reg.applicantName.trim().ifBlank { "Partner" },
                businessName = reg.businessName.trim(),
                contactPhone = reg.contactPhone.trim(),
                whatsappPhone = reg.whatsappPhone.trim(),
                contactEmail = reg.contactEmail.trim(),
                businessLocation = reg.businessLocation.trim(),
                district = reg.district.trim(),
                state = reg.state.trim().ifBlank { "West Bengal" },
                pinCode = reg.pinCode.trim(),
                serviceCategories = sanitizedCategories,
                serviceArea = reg.serviceArea.trim(),
                serviceRadiusKm = reg.serviceRadiusKm.coerceIn(1, 500),
                documentType = reg.documentType.trim().ifBlank { "Trade License" },
                licenseNumber = reg.licenseNumber.trim(),
                gstNumber = reg.gstNumber.trim(),
                documentNotes = reg.documentNotes.trim(),
                verificationDocumentNames = safeDocNames,
                status = PartnerRegistrationStatus.PENDING.code,
                platformVerified = false,
                approvedRoles = emptyList(),
                adminNotes = "",
                rejectionReason = "",
                reviewedByAdminEmail = "",
                submittedAtMillis = now,
                reviewedAtMillis = 0L
            )

            val nextList = previous.toMutableList()
            val idx = nextList.indexOfFirst { it.id == effectiveId || it.userId == cleanUid }
            if (idx >= 0) nextList[idx] = sanitizedReg else nextList.add(0, sanitizedReg)
            saveRegistrationsToDisk(nextList)

            val firestore = db
            if (firestore != null && currentAuth != null) {
                try {
                    val payload = registrationPayloadMap(sanitizedReg, isNew = existing == null)
                    firestore.collection("partner_registrations")
                        .document(effectiveId)
                        .set(payload)
                        .await()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    saveRegistrationsToDisk(previous)
                    Log.e(PARTNER_ROLE_TAG, "Firestore submitRegistration failed: ${e.message}")
                    return Result.failure(e)
                }
            }

            Result.success(sanitizedReg)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminMarkUnderReview(
        regId: String,
        adminNote: String,
        adminEmail: String,
        callerIsAdmin: Boolean = UserSessionManager.isAdmin(context)
    ): Result<PartnerRegistration> {
        if (!callerIsAdmin || !adminEmail.trim().equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true)) {
            return Result.failure(SecurityException("Administrator authorization required."))
        }
        val previous = _registrationsFlow.value
        val list = previous.toMutableList()
        val index = list.indexOfFirst { it.id == regId }
        if (index < 0) return Result.failure(NoSuchElementException("Partner registration not found."))

        val now = System.currentTimeMillis()
        val updated = list[index].copy(
            status = PartnerRegistrationStatus.UNDER_REVIEW.code,
            platformVerified = false,
            approvedRoles = emptyList(),
            adminNotes = adminNote.trim(),
            reviewedByAdminEmail = adminEmail.trim(),
            reviewedAtMillis = now
        )
        list[index] = updated
        saveRegistrationsToDisk(list)

        val firestore = db
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (firestore != null && currentUser != null) {
            try {
                firestore.collection("partner_registrations").document(regId)
                    .update(
                        mapOf(
                            "status" to PartnerRegistrationStatus.UNDER_REVIEW.code,
                            "platformVerified" to false,
                            "approvedRoles" to emptyList<String>(),
                            "adminNotes" to updated.adminNotes,
                            "reviewedByAdminEmail" to updated.reviewedByAdminEmail,
                            "reviewedAtMillis" to now,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    .await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                saveRegistrationsToDisk(previous)
                return Result.failure(e)
            }
        }
        return Result.success(updated)
    }

    suspend fun adminApproveRegistration(
        regId: String,
        grantedRoleCodes: Collection<String>,
        adminNote: String = "",
        adminEmail: String,
        callerIsAdmin: Boolean = UserSessionManager.isAdmin(context)
    ): Result<PartnerRegistration> {
        if (!callerIsAdmin || !adminEmail.trim().equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true)) {
            return Result.failure(SecurityException("Administrator authorization required."))
        }
        val safePartnerRoles = grantedRoleCodes
            .map { AppUserRole.fromCode(it) }
            .filter { it.isSelectableInPartnerRegistration && it != AppUserRole.ADMINISTRATOR }
            .distinct()
        if (safePartnerRoles.isEmpty()) {
            return Result.failure(IllegalArgumentException("Select at least one valid partner role to approve."))
        }

        val previousRegs = _registrationsFlow.value
        val previousMemberships = _membershipsFlow.value
        val list = previousRegs.toMutableList()
        val index = list.indexOfFirst { it.id == regId }
        if (index < 0) return Result.failure(NoSuchElementException("Partner registration not found."))

        val target = list[index]
        val now = System.currentTimeMillis()
        val roleCodes = safePartnerRoles.map { it.code }
        val updated = target.copy(
            status = PartnerRegistrationStatus.APPROVED.code,
            platformVerified = true,
            approvedRoles = roleCodes,
            adminNotes = adminNote.trim(),
            rejectionReason = "",
            reviewedByAdminEmail = adminEmail.trim(),
            reviewedAtMillis = now
        )
        list[index] = updated
        saveRegistrationsToDisk(list)

        val membership = UserRoleMembership(
            id = target.userId,
            userId = target.userId,
            approvedRoles = listOf(AppUserRole.FARMER_CUSTOMER.code) + roleCodes,
            platformVerified = true,
            verifiedBusinessName = target.businessName,
            activeDashboardMode = previousMemberships[target.userId]?.activeDashboardMode
                ?: DashboardMode.FARMER_DASHBOARD.code,
            updatedByAdminEmail = adminEmail.trim()
        )
        val nextMemberships = previousMemberships.toMutableMap()
        nextMemberships[target.userId] = membership
        saveMembershipsToDisk(nextMemberships)

        val firestore = db
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (firestore != null && currentUser != null) {
            try {
                val batch = firestore.batch()
                val regRef = firestore.collection("partner_registrations").document(regId)
                batch.update(
                    regRef,
                    mapOf(
                        "status" to PartnerRegistrationStatus.APPROVED.code,
                        "platformVerified" to true,
                        "approvedRoles" to roleCodes,
                        "adminNotes" to updated.adminNotes,
                        "rejectionReason" to "",
                        "reviewedByAdminEmail" to updated.reviewedByAdminEmail,
                        "reviewedAtMillis" to now,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                val roleRef = firestore.collection("user_roles").document(target.userId)
                batch.set(
                    roleRef,
                    hashMapOf(
                        "userId" to target.userId,
                        "approvedRoles" to membership.approvedRoles,
                        "platformVerified" to true,
                        "verifiedBusinessName" to membership.verifiedBusinessName,
                        "activeDashboardMode" to membership.activeDashboardMode,
                        "updatedByAdminEmail" to membership.updatedByAdminEmail,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                saveRegistrationsToDisk(previousRegs)
                saveMembershipsToDisk(previousMemberships)
                return Result.failure(e)
            }
        }

        syncSessionIfMatchingUser(target.userId, membership)
        return Result.success(updated)
    }

    suspend fun adminRejectRegistration(
        regId: String,
        reason: String,
        adminEmail: String,
        callerIsAdmin: Boolean = UserSessionManager.isAdmin(context)
    ): Result<PartnerRegistration> {
        if (!callerIsAdmin || !adminEmail.trim().equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true)) {
            return Result.failure(SecurityException("Administrator authorization required."))
        }
        val cleanReason = reason.trim()
        if (cleanReason.isBlank()) {
            return Result.failure(IllegalArgumentException("Rejection reason is required."))
        }

        val previousRegs = _registrationsFlow.value
        val previousMemberships = _membershipsFlow.value
        val list = previousRegs.toMutableList()
        val index = list.indexOfFirst { it.id == regId }
        if (index < 0) return Result.failure(NoSuchElementException("Partner registration not found."))

        val target = list[index]
        val now = System.currentTimeMillis()
        val updated = target.copy(
            status = PartnerRegistrationStatus.REJECTED.code,
            platformVerified = false,
            approvedRoles = emptyList(),
            rejectionReason = cleanReason,
            reviewedByAdminEmail = adminEmail.trim(),
            reviewedAtMillis = now
        )
        list[index] = updated
        saveRegistrationsToDisk(list)

        val revokedMembership = UserRoleMembership(
            id = target.userId,
            userId = target.userId,
            approvedRoles = listOf(AppUserRole.FARMER_CUSTOMER.code),
            platformVerified = false,
            verifiedBusinessName = "",
            activeDashboardMode = DashboardMode.FARMER_DASHBOARD.code,
            updatedByAdminEmail = adminEmail.trim()
        )
        val nextMemberships = previousMemberships.toMutableMap()
        nextMemberships[target.userId] = revokedMembership
        saveMembershipsToDisk(nextMemberships)

        val firestore = db
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (firestore != null && currentUser != null) {
            try {
                val batch = firestore.batch()
                val regRef = firestore.collection("partner_registrations").document(regId)
                batch.update(
                    regRef,
                    mapOf(
                        "status" to PartnerRegistrationStatus.REJECTED.code,
                        "platformVerified" to false,
                        "approvedRoles" to emptyList<String>(),
                        "rejectionReason" to cleanReason,
                        "reviewedByAdminEmail" to updated.reviewedByAdminEmail,
                        "reviewedAtMillis" to now,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                val roleRef = firestore.collection("user_roles").document(target.userId)
                batch.set(
                    roleRef,
                    hashMapOf(
                        "userId" to target.userId,
                        "approvedRoles" to listOf(AppUserRole.FARMER_CUSTOMER.code),
                        "platformVerified" to false,
                        "verifiedBusinessName" to "",
                        "activeDashboardMode" to DashboardMode.FARMER_DASHBOARD.code,
                        "updatedByAdminEmail" to adminEmail.trim(),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                saveRegistrationsToDisk(previousRegs)
                saveMembershipsToDisk(previousMemberships)
                return Result.failure(e)
            }
        }

        syncSessionIfMatchingUser(target.userId, revokedMembership)
        return Result.success(updated)
    }

    private fun syncMembershipCacheFromApprovedReg(reg: PartnerRegistration) {
        val roleCodes = reg.resolvedApprovedPartnerRoles().map { it.code }
        if (roleCodes.isEmpty()) return
        val membership = UserRoleMembership(
            id = reg.userId,
            userId = reg.userId,
            approvedRoles = listOf(AppUserRole.FARMER_CUSTOMER.code) + roleCodes,
            platformVerified = true,
            verifiedBusinessName = reg.businessName,
            activeDashboardMode = _membershipsFlow.value[reg.userId]?.activeDashboardMode
                ?: DashboardMode.FARMER_DASHBOARD.code,
            updatedByAdminEmail = reg.reviewedByAdminEmail
        )
        val next = _membershipsFlow.value.toMutableMap()
        next[reg.userId] = membership
        saveMembershipsToDisk(next)
        syncSessionIfMatchingUser(reg.userId, membership)
    }

    private fun defaultMembershipFor(userId: String): UserRoleMembership {
        val session = UserSessionManager.getSession(context)
        val sessionRoles = if (session != null && session.userId == userId) {
            UserSessionManager.getApprovedRoles(context)
                .filter { it != AppUserRole.ADMINISTRATOR }
                .map { it.code }
        } else {
            listOf(AppUserRole.FARMER_CUSTOMER.code)
        }
        val hasPartner = sessionRoles.any { AppUserRole.fromCode(it).isPartnerRole }
        return UserRoleMembership(
            id = userId,
            userId = userId,
            approvedRoles = sessionRoles.distinct(),
            platformVerified = hasPartner,
            activeDashboardMode = if (session != null && session.userId == userId) {
                UserSessionManager.getActiveDashboardMode(context).code
            } else {
                DashboardMode.FARMER_DASHBOARD.code
            }
        )
    }

    private fun sanitizeDocumentDisplayName(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return null
        if (trimmed.startsWith("content://") || trimmed.startsWith("file://")) {
            val seg = trimmed.substringAfterLast('/').substringBefore('?').trim()
            return seg.takeIf { it.isNotBlank() && !it.startsWith("content:") }?.take(80)
                ?: "verification_doc.jpg"
        }
        return trimmed.replace("/", "_").replace("\\", "_").take(80)
    }

    private fun registrationPayloadMap(reg: PartnerRegistration, isNew: Boolean): HashMap<String, Any> {
        val map = hashMapOf<String, Any>(
            "userId" to reg.userId,
            "applicantName" to reg.applicantName,
            "businessName" to reg.businessName,
            "contactPhone" to reg.contactPhone,
            "whatsappPhone" to reg.whatsappPhone,
            "contactEmail" to reg.contactEmail,
            "businessLocation" to reg.businessLocation,
            "district" to reg.district,
            "state" to reg.state,
            "pinCode" to reg.pinCode,
            "serviceCategories" to reg.serviceCategories,
            "serviceArea" to reg.serviceArea,
            "serviceRadiusKm" to reg.serviceRadiusKm,
            "documentType" to reg.documentType,
            "licenseNumber" to reg.licenseNumber,
            "gstNumber" to reg.gstNumber,
            "documentNotes" to reg.documentNotes,
            "verificationDocumentNames" to reg.verificationDocumentNames,
            "status" to reg.status,
            "platformVerified" to reg.platformVerified,
            "approvedRoles" to reg.approvedRoles,
            "adminNotes" to reg.adminNotes,
            "rejectionReason" to reg.rejectionReason,
            "reviewedByAdminEmail" to reg.reviewedByAdminEmail,
            "submittedAtMillis" to reg.submittedAtMillis,
            "reviewedAtMillis" to reg.reviewedAtMillis,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isNew) {
            map["createdAt"] = FieldValue.serverTimestamp()
        }
        return map
    }

    private fun registrationToJson(reg: PartnerRegistration): JSONObject {
        return JSONObject().apply {
            put("id", reg.id)
            put("userId", reg.userId)
            put("applicantName", reg.applicantName)
            put("businessName", reg.businessName)
            put("contactPhone", reg.contactPhone)
            put("whatsappPhone", reg.whatsappPhone)
            put("contactEmail", reg.contactEmail)
            put("businessLocation", reg.businessLocation)
            put("district", reg.district)
            put("state", reg.state)
            put("pinCode", reg.pinCode)
            put("serviceCategories", JSONArray(reg.serviceCategories))
            put("serviceArea", reg.serviceArea)
            put("serviceRadiusKm", reg.serviceRadiusKm)
            put("documentType", reg.documentType)
            put("licenseNumber", reg.licenseNumber)
            put("gstNumber", reg.gstNumber)
            put("documentNotes", reg.documentNotes)
            put("verificationDocumentNames", JSONArray(reg.verificationDocumentNames))
            put("status", reg.status)
            put("platformVerified", reg.platformVerified)
            put("approvedRoles", JSONArray(reg.approvedRoles))
            put("adminNotes", reg.adminNotes)
            put("rejectionReason", reg.rejectionReason)
            put("reviewedByAdminEmail", reg.reviewedByAdminEmail)
            put("submittedAtMillis", reg.submittedAtMillis)
            put("reviewedAtMillis", reg.reviewedAtMillis)
        }
    }

    private fun jsonToRegistration(o: JSONObject): PartnerRegistration {
        fun readStrList(key: String): List<String> {
            val arr = o.optJSONArray(key) ?: return emptyList()
            return buildList {
                for (i in 0 until arr.length()) {
                    val v = arr.optString(i, "")
                    if (v.isNotBlank()) add(v)
                }
            }
        }
        return PartnerRegistration(
            id = o.optString("id", ""),
            userId = o.optString("userId", ""),
            applicantName = o.optString("applicantName", ""),
            businessName = o.optString("businessName", ""),
            contactPhone = o.optString("contactPhone", ""),
            whatsappPhone = o.optString("whatsappPhone", ""),
            contactEmail = o.optString("contactEmail", ""),
            businessLocation = o.optString("businessLocation", ""),
            district = o.optString("district", ""),
            state = o.optString("state", "West Bengal"),
            pinCode = o.optString("pinCode", ""),
            serviceCategories = readStrList("serviceCategories"),
            serviceArea = o.optString("serviceArea", ""),
            serviceRadiusKm = o.optInt("serviceRadiusKm", 30),
            documentType = o.optString("documentType", "Trade License"),
            licenseNumber = o.optString("licenseNumber", ""),
            gstNumber = o.optString("gstNumber", ""),
            documentNotes = o.optString("documentNotes", ""),
            verificationDocumentNames = readStrList("verificationDocumentNames"),
            status = o.optString("status", PartnerRegistrationStatus.PENDING.code),
            platformVerified = o.optBoolean("platformVerified", false),
            approvedRoles = readStrList("approvedRoles"),
            adminNotes = o.optString("adminNotes", ""),
            rejectionReason = o.optString("rejectionReason", ""),
            reviewedByAdminEmail = o.optString("reviewedByAdminEmail", ""),
            submittedAtMillis = o.optLong("submittedAtMillis", System.currentTimeMillis()),
            reviewedAtMillis = o.optLong("reviewedAtMillis", 0L)
        )
    }

    private fun membershipToJson(m: UserRoleMembership): JSONObject {
        return JSONObject().apply {
            put("id", m.id)
            put("userId", m.userId)
            put("approvedRoles", JSONArray(m.approvedRoles))
            put("platformVerified", m.platformVerified)
            put("verifiedBusinessName", m.verifiedBusinessName)
            put("activeDashboardMode", m.activeDashboardMode)
            put("updatedByAdminEmail", m.updatedByAdminEmail)
        }
    }

    private fun jsonToMembership(o: JSONObject): UserRoleMembership {
        val arr = o.optJSONArray("approvedRoles")
        val roles = if (arr != null) {
            buildList {
                for (i in 0 until arr.length()) {
                    val v = arr.optString(i, "")
                    if (v.isNotBlank()) add(v)
                }
            }
        } else {
            listOf(AppUserRole.FARMER_CUSTOMER.code)
        }
        return UserRoleMembership(
            id = o.optString("id", o.optString("userId", "")),
            userId = o.optString("userId", ""),
            approvedRoles = roles,
            platformVerified = o.optBoolean("platformVerified", false),
            verifiedBusinessName = o.optString("verifiedBusinessName", ""),
            activeDashboardMode = o.optString("activeDashboardMode", DashboardMode.FARMER_DASHBOARD.code),
            updatedByAdminEmail = o.optString("updatedByAdminEmail", "")
        )
    }
}
