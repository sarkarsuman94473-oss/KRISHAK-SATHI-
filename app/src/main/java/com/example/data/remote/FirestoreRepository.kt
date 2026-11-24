package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.BuyerOffer
import com.example.data.model.ExpertInquiry
import com.example.data.model.FarmerCrop
import com.example.data.model.FarmerExpense
import com.example.data.model.FarmerProfile
import com.example.data.model.Order
import com.example.data.model.ProduceListing
import com.example.data.model.TeaGarden
import com.example.data.local.session.UserSessionManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreRepo"

class FirestoreRepository(private val context: Context) {

    val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val auth get() = Firebase.auth

    private fun currentAuthUser() = runCatching { auth.currentUser }.getOrNull()

    private fun isCurrentFirebaseAdmin(): Boolean =
        currentAuthUser()?.email.equals(UserSessionManager.ADMIN_EMAIL, ignoreCase = true)

    private fun isAuthorizedToObserveOwner(ownerId: String): Boolean {
        val current = currentAuthUser() ?: return false
        if (ownerId.isBlank()) return false
        return current.uid == ownerId || isCurrentFirebaseAdmin()
    }

    private fun Throwable.findFirestoreException(): FirebaseFirestoreException? =
        generateSequence(this) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing database.")
    }

    // Farmer Profile
    fun observeProfile(userId: String): Flow<FarmerProfile?> {
        if (!isAuthorizedToObserveOwner(userId)) {
            return flowOf(null)
        }
        return db.collection("farmers").document(userId)
            .snapshots()
            .map { snap ->
                if (snap.exists()) snap.toObject(FarmerProfile::class.java) else null
            }
            .catch { e ->
                val fsErr = e.findFirestoreException()
                if (fsErr == null && e is CancellationException) throw e
                Log.w(TAG, "Profile observer deferred (${fsErr?.code ?: e.message})")
                emit(null)
            }
    }

    suspend fun saveProfile(profile: FarmerProfile) {
        val uid = requireUserId()
        val docRef = db.collection("farmers").document(uid)
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "name" to profile.name,
            "mobileNumber" to profile.mobileNumber,
            "village" to profile.village,
            "district" to profile.district,
            "state" to profile.state,
            "pinCode" to profile.pinCode,
            "farmSize" to profile.farmSize,
            "farmSizeUnit" to profile.farmSizeUnit,
            "mainCrops" to profile.mainCrops,
            "preferredLanguage" to profile.preferredLanguage,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val snap = docRef.get().await()
        if (!snap.exists()) {
            payload["createdAt"] = FieldValue.serverTimestamp()
        }
        docRef.set(payload).await()
    }

    // Farmer Crops
    fun observeFarmerCrops(farmerId: String): Flow<List<FarmerCrop>> {
        if (!isAuthorizedToObserveOwner(farmerId)) {
            return flowOf(emptyList())
        }
        return db.collection("farmer_crops")
            .whereEqualTo("farmerId", farmerId)
            .snapshots()
            .map { it.toObjects(FarmerCrop::class.java) }
            .catch { e ->
                val fsErr = e.findFirestoreException()
                if (fsErr == null && e is CancellationException) throw e
                Log.w(TAG, "Farmer crops observer deferred (${fsErr?.code ?: e.message})")
                emit(emptyList())
            }
    }

    suspend fun addFarmerCrop(crop: FarmerCrop) {
        val uid = requireUserId()
        val payload = hashMapOf<String, Any>(
            "farmerId" to uid,
            "cropName" to crop.cropName,
            "cropCategory" to crop.cropCategory,
            "plantingDate" to crop.plantingDate,
            "landArea" to crop.landArea,
            "landUnit" to crop.landUnit,
            "variety" to crop.variety,
            "notes" to crop.notes,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("farmer_crops").add(payload).await()
    }

    suspend fun deleteFarmerCrop(cropId: String) {
        requireUserId()
        db.collection("farmer_crops").document(cropId).delete().await()
    }

    // Tea Gardens
    fun observeTeaGardens(farmerId: String): Flow<List<TeaGarden>> {
        if (!isAuthorizedToObserveOwner(farmerId)) {
            return flowOf(emptyList())
        }
        return db.collection("tea_gardens")
            .whereEqualTo("farmerId", farmerId)
            .snapshots()
            .map { it.toObjects(TeaGarden::class.java) }
            .catch { e ->
                val fsErr = e.findFirestoreException()
                if (fsErr == null && e is CancellationException) throw e
                Log.w(TAG, "Tea gardens observer deferred (${fsErr?.code ?: e.message})")
                emit(emptyList())
            }
    }

    suspend fun saveTeaGarden(garden: TeaGarden) {
        val uid = requireUserId()
        val docRef = if (garden.id.isNotBlank()) {
            db.collection("tea_gardens").document(garden.id)
        } else {
            db.collection("tea_gardens").document()
        }
        val payload = hashMapOf<String, Any>(
            "farmerId" to uid,
            "gardenName" to garden.gardenName,
            "location" to garden.location,
            "totalLand" to garden.totalLand,
            "landUnit" to garden.landUnit,
            "teaVariety" to garden.teaVariety,
            "plantAgeYears" to garden.plantAgeYears,
            "plantingYear" to garden.plantingYear,
            "sectionsCount" to garden.sectionsCount,
            "irrigationAvailable" to garden.irrigationAvailable,
            "shadeTreesInfo" to garden.shadeTreesInfo,
            "lastPruningDate" to garden.lastPruningDate,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (garden.id.isBlank()) {
            payload["createdAt"] = FieldValue.serverTimestamp()
        }
        docRef.set(payload).await()
    }

    // Produce Listings
    fun observeProduceListings(): Flow<List<ProduceListing>> {
        if (currentAuthUser() == null) {
            return flowOf(emptyList())
        }
        return db.collection("produce_listings")
            .snapshots()
            .map { it.toObjects(ProduceListing::class.java) }
            .catch { e ->
                val fsErr = e.findFirestoreException()
                if (fsErr == null && e is CancellationException) throw e
                Log.w(TAG, "Produce listings observer deferred (${fsErr?.code ?: e.message})")
                emit(emptyList())
            }
    }

    suspend fun addProduceListing(listing: ProduceListing) {
        val uid = requireUserId()
        val payload = hashMapOf<String, Any>(
            "farmerId" to uid,
            "farmerName" to listing.farmerName,
            "farmerPhone" to listing.farmerPhone,
            "product" to listing.product,
            "category" to listing.category,
            "quantity" to listing.quantity,
            "unit" to listing.unit,
            "expectedPricePerUnit" to listing.expectedPricePerUnit,
            "harvestDate" to listing.harvestDate,
            "location" to listing.location,
            "deliveryAvailable" to listing.deliveryAvailable,
            "status" to "ACTIVE",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("produce_listings").add(payload).await()
    }

    // Buyer Offers
    fun observeBuyerOffers(userId: String): Flow<List<BuyerOffer>> {
        if (!isAuthorizedToObserveOwner(userId)) {
            return flowOf(emptyList())
        }
        return db.collection("buyer_offers")
            .whereEqualTo("farmerId", userId)
            .snapshots()
            .map { it.toObjects(BuyerOffer::class.java) }
            .catch { e ->
                val fsErr = e.findFirestoreException()
                if (fsErr == null && e is CancellationException) throw e
                Log.w(TAG, "Buyer offers observer deferred (${fsErr?.code ?: e.message})")
                emit(emptyList())
            }
    }

    suspend fun createBuyerOffer(offer: BuyerOffer) {
        val uid = requireUserId()
        val payload = hashMapOf<String, Any>(
            "listingId" to offer.listingId,
            "farmerId" to offer.farmerId,
            "buyerId" to uid,
            "buyerName" to offer.buyerName,
            "buyerType" to offer.buyerType,
            "buyerPhone" to offer.buyerPhone,
            "offeredPricePerUnit" to offer.offeredPricePerUnit,
            "status" to "PENDING",
            "notes" to offer.notes,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("buyer_offers").add(payload).await()
    }

    suspend fun updateOfferStatus(offerId: String, newStatus: String, counterPrice: Double? = null) {
        requireUserId()
        val updates = mutableMapOf<String, Any>(
            "status" to newStatus,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (counterPrice != null) {
            updates["counterPricePerUnit"] = counterPrice
        }
        db.collection("buyer_offers").document(offerId).update(updates).await()
    }

    // Orders
    fun observeOrders(userId: String): Flow<List<Order>> {
        if (!isAuthorizedToObserveOwner(userId)) {
            return flowOf(emptyList())
        }
        return db.collection("orders")
            .whereEqualTo("farmerId", userId)
            .snapshots()
            .map { it.toObjects(Order::class.java) }
            .catch { e ->
                val fsErr = e.findFirestoreException()
                if (fsErr == null && e is CancellationException) throw e
                Log.w(TAG, "Orders observer deferred (${fsErr?.code ?: e.message})")
                emit(emptyList())
            }
    }

    suspend fun createOrderFromOffer(offer: BuyerOffer, listing: ProduceListing) {
        val uid = requireUserId()
        val agreedPrice = offer.counterPricePerUnit ?: offer.offeredPricePerUnit
        val total = agreedPrice * listing.quantity
        val payload = hashMapOf<String, Any>(
            "listingId" to listing.id,
            "farmerId" to offer.farmerId,
            "buyerId" to offer.buyerId,
            "product" to listing.product,
            "quantity" to listing.quantity,
            "unit" to listing.unit,
            "agreedPricePerUnit" to agreedPrice,
            "totalAmount" to total,
            "status" to "CONFIRMED",
            "paymentMethod" to "CASH_ON_DELIVERY",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("orders").add(payload).await()
        updateOfferStatus(offer.id, "ACCEPTED")
        db.collection("produce_listings").document(listing.id).update("status", "SOLD").await()
    }

    // Expert Inquiries
    fun observeExpertInquiries(userId: String): Flow<List<ExpertInquiry>> {
        if (!isAuthorizedToObserveOwner(userId)) {
            return flowOf(emptyList())
        }
        return db.collection("expert_inquiries")
            .whereEqualTo("farmerId", userId)
            .snapshots()
            .map { it.toObjects(ExpertInquiry::class.java) }
            .catch { e ->
                val fsErr = e.findFirestoreException()
                if (fsErr == null && e is CancellationException) throw e
                Log.w(TAG, "Expert inquiries observer deferred (${fsErr?.code ?: e.message})")
                emit(emptyList())
            }
    }

    suspend fun submitExpertInquiry(inquiry: ExpertInquiry, lang: AppLanguage = AppLanguage.BENGALI) {
        val uid = requireUserId()
        val autoReply = when (lang) {
            AppLanguage.BENGALI -> "কৃষি বিজ্ঞানী ডঃ সেন: আপনার বর্ণিত সমস্যা নিয়ন্ত্রণে সঠিক বায়ু চলাচল ও পরিমিত সেচ নিশ্চিত করুন। অনুমোদিত নিম তেল ৫ মিলি/লিটার স্প্রে করতে পারেন।"
            AppLanguage.HINDI -> "कृषि वैज्ञानिक डॉ. सेन: आपकी समस्या के नियंत्रण के लिए सही वायु संचार और संतुलित सिंचाई सुनिश्चित करें। अनुशंसित नीम तेल 5 मिली/लीटर का छिड़काव कर सकते हैं।"
            AppLanguage.ENGLISH -> "Agri Scientist Dr. Sen: To manage this issue, ensure proper airflow and balanced irrigation. You may spray recommended Neem Oil at 5 ml/litre of water."
        }
        val payload = hashMapOf<String, Any>(
            "farmerId" to uid,
            "farmerName" to inquiry.farmerName,
            "crop" to inquiry.crop,
            "question" to inquiry.question,
            "photoUrl" to inquiry.photoUrl,
            "status" to "ANSWERED",
            "reply" to autoReply,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("expert_inquiries").add(payload).await()
    }

    // Nursery & Seed Market
    suspend fun registerNurseryBusiness(nursery: com.example.data.model.NurseryBusiness, lang: AppLanguage = AppLanguage.BENGALI) {
        val uid = requireUserId()
        val pendingBadge = when (lang) {
            AppLanguage.BENGALI -> "পর্যালোচনাধীন (Pending)"
            AppLanguage.HINDI -> "समीक्षाधीन (Pending)"
            AppLanguage.ENGLISH -> "Pending Verification"
        }
        val payload = hashMapOf<String, Any>(
            "id" to (if (nursery.id.isNotBlank()) nursery.id else "nurs_${System.currentTimeMillis()}"),
            "ownerUserId" to uid,
            "nameBn" to nursery.nameBn,
            "nameEn" to nursery.nameEn,
            "ownerName" to nursery.ownerName,
            "phone" to nursery.phone,
            "whatsapp" to nursery.whatsapp,
            "addressBn" to nursery.addressBn,
            "city" to nursery.city,
            "district" to nursery.district,
            "pinCode" to nursery.pinCode,
            "isVerified" to false, // Pending admin verification
            "verificationStatus" to "PENDING_VERIFICATION",
            "isWholesale" to nursery.isWholesale,
            "isRetail" to nursery.isRetail,
            "minimumWholesaleValue" to nursery.minimumWholesaleValue,
            "deliveryAvailable" to nursery.deliveryAvailable,
            "badgeText" to pendingBadge,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("nurseries").document(payload["id"] as String).set(payload).await()
    }

    suspend fun submitWholesaleQuoteRequest(request: com.example.data.model.WholesaleQuoteRequest) {
        val uid = requireUserId()
        val payload = hashMapOf<String, Any>(
            "buyerId" to uid,
            "buyerName" to request.buyerName,
            "buyerPhone" to request.buyerPhone,
            "deliveryLocation" to request.deliveryLocation,
            "itemName" to request.itemName,
            "itemType" to request.itemType,
            "quantity" to request.quantity,
            "targetNurseryId" to request.targetNurseryId,
            "targetNurseryName" to request.targetNurseryName,
            "notes" to request.notes,
            "status" to "SUBMITTED",
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("wholesale_quotes").add(payload).await()
    }

    suspend fun submitFraudSeedReport(report: com.example.data.model.FraudSeedReport) {
        val uid = requireUserId()
        val payload = hashMapOf<String, Any>(
            "reporterUserId" to uid,
            "reporterName" to report.reporterName,
            "reporterPhone" to report.reporterPhone,
            "sellerOrNurseryName" to report.sellerOrNurseryName,
            "location" to report.location,
            "seedBrand" to report.seedBrand,
            "complaintDetails" to report.complaintDetails,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("fraud_seed_reports").add(payload).await()
    }
}
