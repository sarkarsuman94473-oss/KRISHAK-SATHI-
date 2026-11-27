package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.NurseryApplication
import com.example.data.model.NurseryApplicationStatus
import com.example.data.model.UserRole
import com.example.data.local.session.UserSession
import com.example.data.local.session.UserSessionManager
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

private const val TAG = "NurseryAppRepo"

class NurseryApplicationRepository(private val context: Context) {

    private val db: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Firestore instance: ${e.message}")
            null
        }
    }

    private val prefs = context.applicationContext.getSharedPreferences("krishak_sathi_nursery_apps", Context.MODE_PRIVATE)

    // Local in-memory cache for ultra-responsive UI & offline resilience
    private val _applicationsFlow = MutableStateFlow<List<NurseryApplication>>(emptyList())
    val allApplicationsFlow: Flow<List<NurseryApplication>> = _applicationsFlow.asStateFlow()

    init {
        loadFromLocalDisk()
    }

    private fun loadFromLocalDisk() {
        try {
            val raw = prefs.getString("cached_nursery_apps", null)
            if (!raw.isNullOrBlank()) {
                val array = JSONArray(raw)
                val list = mutableListOf<NurseryApplication>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(jsonToApp(obj))
                }
                _applicationsFlow.value = list
            } else {
                // Pre-seed initial verified nursery applications for immediate exploration
                val initialList = listOf(
                    NurseryApplication(
                        id = "app_siliguri_green",
                        userId = "usr_pioneer_nursery_1",
                        applicantName = "রবীন্দ্রনাথ রায় (Rabindranath Ray)",
                        mobileNumber = "9832014587",
                        email = "rabin.nursery@example.com",
                        isMobileVerified = true,
                        nurseryNameBn = "উত্তরবঙ্গ গ্রিন নার্সারি ও টি রিসার্চ হাব",
                        nurseryNameEn = "North Bengal Green Nursery & Tea Research Hub",
                        description = "উন্নত জাতের চা চারা, কলম ও দেশি ফলের সবচেয়ে বড় নির্ভরযোগ্য প্রতিষ্ঠান। সরকার অনুমোদিত।",
                        primaryCategory = "TEA_CLONES",
                        establishmentYear = "2012",
                        country = "India",
                        state = "West Bengal",
                        district = "Darjeeling",
                        cityOrTown = "Siliguri",
                        fullAddress = "শালুগাড়া রোড, চম্পাসারি, শিলিগুড়ি",
                        pinCode = "734003",
                        latitude = 26.7580,
                        longitude = 88.4310,
                        businessPhone = "9832014587",
                        whatsappNumber = "9832014587",
                        openingHours = "সকাল ৭:০০ - সন্ধ্যা ৭:০০",
                        isDeliveryAvailable = true,
                        deliveryRadiusKm = 60,
                        tradeLicenseNumber = "TL/DARJ/2018/8892",
                        status = NurseryApplicationStatus.APPROVED.code,
                        submittedAtMillis = System.currentTimeMillis() - 86400000L * 30
                    ),
                    NurseryApplication(
                        id = "app_kisan_phul",
                        userId = "usr_kisan_phul_2",
                        applicantName = "বিপ্লব কুমার সাহা (Biplab Kumar Saha)",
                        mobileNumber = "9749123890",
                        email = "biplab.kisan@example.com",
                        isMobileVerified = true,
                        nurseryNameBn = "মা তারা হাইটেক প্ল্যান্ট নার্সারি",
                        nurseryNameEn = "Maa Tara High-Tech Plant Nursery",
                        description = "উচ্চফলনশীল সবজি চারা, ড্রাগন ফ্রুট, মাল্টা ও আম্রপালি কলম সুলভ মূল্যে পাইকারি সরবরাহ।",
                        primaryCategory = "FRUITS",
                        establishmentYear = "2016",
                        country = "India",
                        state = "West Bengal",
                        district = "Jalpaiguri",
                        cityOrTown = "Dhupguri",
                        fullAddress = "ধূপগুড়ি বাজার সংলগ্ন, জলপাইগুড়ি",
                        pinCode = "735210",
                        latitude = 26.5920,
                        longitude = 89.0150,
                        businessPhone = "9749123890",
                        whatsappNumber = "9749123890",
                        openingHours = "সকাল ৮:০০ - সন্ধ্যা ৬:০০",
                        isDeliveryAvailable = true,
                        deliveryRadiusKm = 40,
                        tradeLicenseNumber = "TL/JPG/2020/5541",
                        status = NurseryApplicationStatus.APPROVED.code,
                        submittedAtMillis = System.currentTimeMillis() - 86400000L * 15
                    )
                )
                saveToLocalDisk(initialList)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading local apps: ${e.message}")
        }
    }

    private fun saveToLocalDisk(list: List<NurseryApplication>) {
        _applicationsFlow.value = list
        try {
            val array = JSONArray()
            for (app in list) {
                array.put(appToJson(app))
            }
            prefs.edit().putString("cached_nursery_apps", array.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving local apps: ${e.message}")
        }
    }

    fun observeUserApplication(userId: String): Flow<NurseryApplication?> {
        val firestore = db
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        val isAdmin = currentUser?.email.equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true)
        return if (firestore != null && currentUser != null && userId.isNotBlank() && (currentUser.uid == userId || isAdmin)) {
            firestore.collection("nursery_applications")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snap ->
                    val app = snap.documents.firstOrNull()?.toObject(NurseryApplication::class.java)
                    app ?: _applicationsFlow.value.firstOrNull { it.userId == userId }
                }
                .catch { e ->
                    val fsErr = generateSequence(e) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && e is CancellationException) throw e
                    Log.w(TAG, "Firestore observeUserApplication deferred: ${fsErr?.code ?: e.message}")
                    emit(_applicationsFlow.value.firstOrNull { it.userId == userId })
                }
        } else {
            _applicationsFlow.map { list -> list.firstOrNull { it.userId == userId } }
        }
    }

    fun observeAllApplications(): Flow<List<NurseryApplication>> {
        val firestore = db
        val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        return if (firestore != null && currentUser != null) {
            val isAdmin = currentUser.email.equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true)
            val query = if (isAdmin) {
                firestore.collection("nursery_applications")
            } else {
                firestore.collection("nursery_applications")
                    .whereEqualTo("status", NurseryApplicationStatus.APPROVED.code)
            }
            query.snapshots()
                .map { snap ->
                    val remoteList = snap.toObjects(NurseryApplication::class.java)
                    if (remoteList.isNotEmpty()) {
                        if (isAdmin) {
                            saveToLocalDisk(remoteList)
                            remoteList
                        } else {
                            // Merge approved remote applications with local cached applications
                            val merged = (_applicationsFlow.value + remoteList)
                                .associateBy { it.id.ifBlank { it.userId } }
                                .values
                                .toList()
                            merged
                        }
                    } else {
                        _applicationsFlow.value
                    }
                }
                .catch { e ->
                    val fsErr = generateSequence(e) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && e is CancellationException) throw e
                    Log.w(TAG, "Firestore observeAllApplications deferred: ${fsErr?.code ?: e.message}")
                    emit(_applicationsFlow.value)
                }
        } else {
            _applicationsFlow
        }
    }

    fun observeApprovedNurseries(): Flow<List<NurseryApplication>> {
        return observeAllApplications().map { list ->
            list.filter { it.status == NurseryApplicationStatus.APPROVED.code }
        }
    }

    suspend fun saveDraftApplication(app: NurseryApplication) {
        val draftApp = app.copy(
            status = NurseryApplicationStatus.DRAFT.code,
            submittedAtMillis = System.currentTimeMillis()
        )
        persistApplication(draftApp)
    }

    suspend fun submitApplication(app: NurseryApplication) {
        val submittedApp = app.copy(
            status = NurseryApplicationStatus.PENDING.code,
            adminRejectionReason = "",
            submittedAtMillis = System.currentTimeMillis()
        )
        persistApplication(submittedApp)
    }

    suspend fun adminApproveApplication(appId: String, adminEmail: String) {
        val previous = _applicationsFlow.value
        val list = previous.toMutableList()
        val index = list.indexOfFirst { it.id == appId }
        if (index < 0) throw NoSuchElementException("Nursery application not found")
        val updated = list[index].copy(
            status = NurseryApplicationStatus.APPROVED.code,
            adminRejectionReason = "",
            reviewedByAdminEmail = adminEmail,
            reviewedAtMillis = System.currentTimeMillis()
        )
        list[index] = updated
        saveToLocalDisk(list)

        val firestore = db
        if (firestore != null && runCatching { Firebase.auth.currentUser }.getOrNull() != null) {
            try {
                firestore.collection("nursery_applications").document(appId).update(
                    mapOf(
                        "status" to NurseryApplicationStatus.APPROVED.code,
                        "reviewedByAdminEmail" to adminEmail,
                        "reviewedAtMillis" to System.currentTimeMillis(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                saveToLocalDisk(previous)
                Log.e(TAG, "Firestore approve error: ${e.message}")
                throw e
            }
        }

        // If the applicant is the current user on device, upgrade role in session
        val currentSession = UserSessionManager.getSession(context)
        if (currentSession != null && currentSession.userId == updated.userId) {
            UserSessionManager.updateUserRole(context, UserSession.ROLE_NURSERY_OWNER)
        }
    }

    suspend fun adminRejectApplication(appId: String, reason: String, adminEmail: String) {
        val previous = _applicationsFlow.value
        val list = previous.toMutableList()
        val index = list.indexOfFirst { it.id == appId }
        if (index < 0) throw NoSuchElementException("Nursery application not found")
        val updated = list[index].copy(
            status = NurseryApplicationStatus.REJECTED.code,
            adminRejectionReason = reason,
            reviewedByAdminEmail = adminEmail,
            reviewedAtMillis = System.currentTimeMillis()
        )
        list[index] = updated
        saveToLocalDisk(list)

        val firestore = db
        if (firestore != null && runCatching { Firebase.auth.currentUser }.getOrNull() != null) {
            try {
                firestore.collection("nursery_applications").document(appId).update(
                    mapOf(
                        "status" to NurseryApplicationStatus.REJECTED.code,
                        "adminRejectionReason" to reason,
                        "reviewedByAdminEmail" to adminEmail,
                        "reviewedAtMillis" to System.currentTimeMillis(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                saveToLocalDisk(previous)
                Log.e(TAG, "Firestore reject error: ${e.message}")
                throw e
            }
        }
    }

    suspend fun adminSuspendNursery(appId: String, reason: String, adminEmail: String) {
        val previous = _applicationsFlow.value
        val list = previous.toMutableList()
        val index = list.indexOfFirst { it.id == appId }
        if (index < 0) throw NoSuchElementException("Nursery application not found")
        val updated = list[index].copy(
            status = NurseryApplicationStatus.SUSPENDED.code,
            adminRejectionReason = reason,
            reviewedByAdminEmail = adminEmail,
            reviewedAtMillis = System.currentTimeMillis()
        )
        list[index] = updated
        saveToLocalDisk(list)

        val firestore = db
        if (firestore != null && runCatching { Firebase.auth.currentUser }.getOrNull() != null) {
            try {
                firestore.collection("nursery_applications").document(appId).update(
                    mapOf(
                        "status" to NurseryApplicationStatus.SUSPENDED.code,
                        "adminRejectionReason" to reason,
                        "reviewedByAdminEmail" to adminEmail,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                saveToLocalDisk(previous)
                Log.e(TAG, "Firestore suspend error: ${e.message}")
                throw e
            }
        }
    }

    suspend fun adminRequestInfo(appId: String, note: String, adminEmail: String) {
        val previous = _applicationsFlow.value
        val list = previous.toMutableList()
        val index = list.indexOfFirst { it.id == appId }
        if (index < 0) throw NoSuchElementException("Nursery application not found")
        val updated = list[index].copy(
            status = NurseryApplicationStatus.UNDER_REVIEW.code,
            adminNotes = note,
            reviewedByAdminEmail = adminEmail,
            reviewedAtMillis = System.currentTimeMillis()
        )
        list[index] = updated
        saveToLocalDisk(list)

        val firestore = db
        if (firestore != null && runCatching { Firebase.auth.currentUser }.getOrNull() != null) {
            try {
                firestore.collection("nursery_applications").document(appId).update(
                    mapOf(
                        "status" to NurseryApplicationStatus.UNDER_REVIEW.code,
                        "adminNotes" to note,
                        "reviewedByAdminEmail" to adminEmail,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                saveToLocalDisk(previous)
                Log.e(TAG, "Firestore requestInfo error: ${e.message}")
                throw e
            }
        }
    }

    private suspend fun persistApplication(app: NurseryApplication) {
        val effectiveId = if (app.id.isNotBlank()) app.id else "app_nurs_${System.currentTimeMillis()}"
        val finalApp = app.copy(id = effectiveId)

        val previous = _applicationsFlow.value
        val list = previous.toMutableList()
        val index = list.indexOfFirst { it.id == effectiveId || (it.userId == finalApp.userId && finalApp.userId.isNotBlank()) }
        if (index >= 0) {
            list[index] = finalApp
        } else {
            list.add(0, finalApp)
        }
        saveToLocalDisk(list)

        val firestore = db
        if (firestore != null && runCatching { Firebase.auth.currentUser }.getOrNull() != null) {
            try {
                val payload = hashMapOf<String, Any>(
                    "id" to finalApp.id,
                    "userId" to finalApp.userId,
                    "applicantName" to finalApp.applicantName,
                    "mobileNumber" to finalApp.mobileNumber,
                    "email" to finalApp.email,
                    "isMobileVerified" to finalApp.isMobileVerified,
                    "nurseryNameBn" to finalApp.nurseryNameBn,
                    "nurseryNameEn" to finalApp.nurseryNameEn,
                    "nurseryLogoUrl" to finalApp.nurseryLogoUrl.takeIf { !it.startsWith("content://") }.orEmpty(),
                    "nurseryCoverUrl" to finalApp.nurseryCoverUrl.takeIf { !it.startsWith("content://") }.orEmpty(),
                    "description" to finalApp.description,
                    "primaryCategory" to finalApp.primaryCategory,
                    "establishmentYear" to finalApp.establishmentYear,
                    "country" to finalApp.country,
                    "state" to finalApp.state,
                    "district" to finalApp.district,
                    "cityOrTown" to finalApp.cityOrTown,
                    "fullAddress" to finalApp.fullAddress,
                    "pinCode" to finalApp.pinCode,
                    "latitude" to finalApp.latitude,
                    "longitude" to finalApp.longitude,
                    "businessPhone" to finalApp.businessPhone,
                    "whatsappNumber" to finalApp.whatsappNumber,
                    "businessEmail" to finalApp.businessEmail,
                    "openingHours" to finalApp.openingHours,
                    "isDeliveryAvailable" to finalApp.isDeliveryAvailable,
                    "deliveryRadiusKm" to finalApp.deliveryRadiusKm,
                    "tradeLicenseNumber" to finalApp.tradeLicenseNumber,
                    "documentType" to finalApp.documentType,
                    "documentNotes" to finalApp.documentNotes,
                    "gstNumber" to finalApp.gstNumber,
                    "status" to finalApp.status,
                    "submittedAtMillis" to finalApp.submittedAtMillis,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("nursery_applications").document(finalApp.id).set(payload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                saveToLocalDisk(previous)
                Log.e(TAG, "Error syncing to Firestore: ${e.message}")
                throw e
            }
        }
    }

    private fun appToJson(app: NurseryApplication): JSONObject {
        val o = JSONObject()
        o.put("id", app.id)
        o.put("userId", app.userId)
        o.put("applicantName", app.applicantName)
        o.put("mobileNumber", app.mobileNumber)
        o.put("email", app.email)
        o.put("isMobileVerified", app.isMobileVerified)
        o.put("nurseryNameBn", app.nurseryNameBn)
        o.put("nurseryNameEn", app.nurseryNameEn)
        o.put("nurseryLogoUrl", app.nurseryLogoUrl)
        o.put("nurseryCoverUrl", app.nurseryCoverUrl)
        o.put("description", app.description)
        o.put("primaryCategory", app.primaryCategory)
        o.put("establishmentYear", app.establishmentYear)
        o.put("country", app.country)
        o.put("state", app.state)
        o.put("district", app.district)
        o.put("cityOrTown", app.cityOrTown)
        o.put("fullAddress", app.fullAddress)
        o.put("pinCode", app.pinCode)
        o.put("latitude", app.latitude)
        o.put("longitude", app.longitude)
        o.put("businessPhone", app.businessPhone)
        o.put("whatsappNumber", app.whatsappNumber)
        o.put("businessEmail", app.businessEmail)
        o.put("openingHours", app.openingHours)
        o.put("isDeliveryAvailable", app.isDeliveryAvailable)
        o.put("deliveryRadiusKm", app.deliveryRadiusKm)
        o.put("tradeLicenseNumber", app.tradeLicenseNumber)
        o.put("documentType", app.documentType)
        o.put("documentNotes", app.documentNotes)
        o.put("gstNumber", app.gstNumber)
        o.put("status", app.status)
        o.put("adminRejectionReason", app.adminRejectionReason)
        o.put("adminNotes", app.adminNotes)
        o.put("reviewedByAdminEmail", app.reviewedByAdminEmail)
        o.put("submittedAtMillis", app.submittedAtMillis)
        o.put("reviewedAtMillis", app.reviewedAtMillis)
        return o
    }

    private fun jsonToApp(o: JSONObject): NurseryApplication {
        return NurseryApplication(
            id = o.optString("id", ""),
            userId = o.optString("userId", ""),
            applicantName = o.optString("applicantName", ""),
            mobileNumber = o.optString("mobileNumber", ""),
            email = o.optString("email", ""),
            isMobileVerified = o.optBoolean("isMobileVerified", true),
            nurseryNameBn = o.optString("nurseryNameBn", ""),
            nurseryNameEn = o.optString("nurseryNameEn", ""),
            nurseryLogoUrl = o.optString("nurseryLogoUrl", ""),
            nurseryCoverUrl = o.optString("nurseryCoverUrl", ""),
            description = o.optString("description", ""),
            primaryCategory = o.optString("primaryCategory", "FRUITS"),
            establishmentYear = o.optString("establishmentYear", ""),
            country = o.optString("country", "India"),
            state = o.optString("state", "West Bengal"),
            district = o.optString("district", "Jalpaiguri"),
            cityOrTown = o.optString("cityOrTown", "Siliguri"),
            fullAddress = o.optString("fullAddress", ""),
            pinCode = o.optString("pinCode", ""),
            latitude = o.optDouble("latitude", 26.7271),
            longitude = o.optDouble("longitude", 88.4239),
            businessPhone = o.optString("businessPhone", ""),
            whatsappNumber = o.optString("whatsappNumber", ""),
            businessEmail = o.optString("businessEmail", ""),
            openingHours = o.optString("openingHours", "08:00 AM - 06:00 PM"),
            isDeliveryAvailable = o.optBoolean("isDeliveryAvailable", true),
            deliveryRadiusKm = o.optInt("deliveryRadiusKm", 30),
            tradeLicenseNumber = o.optString("tradeLicenseNumber", ""),
            documentType = o.optString("documentType", "Trade License"),
            documentNotes = o.optString("documentNotes", ""),
            gstNumber = o.optString("gstNumber", ""),
            status = o.optString("status", NurseryApplicationStatus.PENDING.code),
            adminRejectionReason = o.optString("adminRejectionReason", ""),
            adminNotes = o.optString("adminNotes", ""),
            reviewedByAdminEmail = o.optString("reviewedByAdminEmail", ""),
            submittedAtMillis = o.optLong("submittedAtMillis", System.currentTimeMillis()),
            reviewedAtMillis = o.optLong("reviewedAtMillis", 0L)
        )
    }
}
