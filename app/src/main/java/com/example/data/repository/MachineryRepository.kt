package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.R
import com.example.data.datasource.MachinerySeedCatalog
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ActivityEntityType
import com.example.data.local.entity.MachineryDealerEntity
import com.example.data.local.entity.MachineryDealerOfferEntity
import com.example.data.local.entity.MachineryFavouriteEntity
import com.example.data.local.entity.MachineryInquiryEntity
import com.example.data.local.entity.MachineryManufacturerEntity
import com.example.data.local.entity.MachineryProductEntity
import com.example.data.local.entity.MachineryRentalEntity
import com.example.data.local.entity.MachineryResearchQueueEntity
import com.example.data.local.entity.MachineryReviewEntity
import com.example.data.local.entity.MachineryServiceRequestEntity
import com.example.data.model.AppLanguage
import com.example.data.model.BookingLifecycleStatus
import com.example.data.model.MachineryAdminAuditLog
import com.example.data.model.MachineryCategory
import com.example.data.model.MachineryDealerEntry
import com.example.data.model.MachineryDealerOffer
import com.example.data.model.MachineryInquiryRecord
import com.example.data.model.MachineryManufacturer
import com.example.data.model.MachineryProduct
import com.example.data.model.MachineryRecommendationQuery
import com.example.data.model.MachineryRentalRequest
import com.example.data.model.MachineryResearchQueueItem
import com.example.data.model.MachineryReviewRecord
import com.example.data.model.MachineryServiceRequest
import com.example.data.model.NurseryVerificationStatus
import com.example.data.remote.MachineryImageStorageManager
import com.example.data.remote.UploadedEquipmentPhoto
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CsvMachineryPreview(
    val validRows: List<MachineryProduct>,
    val errors: List<String>
)

data class DuplicateMachineryGroup(
    val matchReason: String,
    val products: List<MachineryProduct>
)

class MachineryRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val dao = database.machineryDao()
    private val trustRepo: TrustAndSafetyRepository by lazy { TrustAndSafetyRepository(context, database) }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.w("MachineryRepo", "Firestore unavailable: ${e.message}")
            null
        }
    }

    fun observeAllProducts(): Flow<List<MachineryProduct>> =
        dao.observeAllProducts().map { list -> list.map { it.toModel() } }

    fun observePublicProducts(): Flow<List<MachineryProduct>> =
        dao.observeAllProducts().map { list ->
            list.map { it.toModel() }.filter { it.approvalStatus == "APPROVED" }
        }

    fun observeAllDealers(): Flow<List<MachineryDealerEntry>> =
        dao.observeAllDealers().map { list -> list.map { it.toModel() } }

    fun observeAllManufacturers(): Flow<List<MachineryManufacturer>> =
        dao.observeAllManufacturers().map { list -> list.map { it.toModel() } }

    fun observeOffersForMachine(machineId: String): Flow<List<MachineryDealerOffer>> =
        dao.observeOffersForMachine(machineId).map { list -> list.map { it.toModel() } }

    fun observeAllOffers(): Flow<List<MachineryDealerOffer>> =
        dao.observeAllOffers().map { list -> list.map { it.toModel() } }

    fun observeReviewsForMachine(machineId: String): Flow<List<MachineryReviewRecord>> =
        dao.observeReviewsForMachine(machineId).map { list -> list.map { it.toModel() } }

    fun observeAllReviews(): Flow<List<MachineryReviewRecord>> =
        dao.observeAllReviews().map { list -> list.map { it.toModel() } }

    fun observeResearchQueue(): Flow<List<MachineryResearchQueueItem>> =
        dao.observeResearchQueue().map { list -> list.map { it.toModel() } }

    fun observeFavouriteIds(userId: String): Flow<Set<String>> =
        dao.observeFavouriteIds(userId).map { it.toSet() }

    fun observeInquiriesForUser(userId: String): Flow<List<MachineryInquiryRecord>> =
        dao.observeInquiriesForUser(userId).map { list -> list.map { it.toModel() } }

    fun observeAllInquiries(): Flow<List<MachineryInquiryRecord>> =
        dao.observeAllInquiries().map { list -> list.map { it.toModel() } }

    fun observeRentalsForUser(userId: String): Flow<List<MachineryRentalRequest>> =
        dao.observeRentalsForUser(userId).map { list -> list.map { it.toModel() } }

    fun observeAllRentals(): Flow<List<MachineryRentalRequest>> =
        dao.observeAllRentals().map { list -> list.map { it.toModel() } }

    fun observeServicesForUser(userId: String): Flow<List<MachineryServiceRequest>> =
        dao.observeServicesForUser(userId).map { list -> list.map { it.toModel() } }

    fun observeAllServices(): Flow<List<MachineryServiceRequest>> =
        dao.observeAllServices().map { list -> list.map { it.toModel() } }

    suspend fun ensureSeeded() {
        val existingProducts = dao.getAllProductsOnce().associateBy { it.id }
        val toUpsert = MachinerySeedCatalog.referenceCatalogProducts.mapNotNull { seed ->
            val current = existingProducts[seed.id]
            val adminLocked = current != null && current.verificationBadgeText.contains("Admin Verified", ignoreCase = true)
            when {
                current == null -> MachineryProductEntity.fromModel(seed)
                !adminLocked && current.listingOrigin == "REFERENCE_SPEC" && (
                    (current.photosCsv.isBlank() && seed.photosCsv.isNotBlank()) ||
                        (current.photosCsv != seed.photosCsv && seed.photosCsv.contains(current.photosCsv.substringBefore(",").trim())) ||
                        current.purposeEn.isBlank() ||
                        (current.aliasesCsv.isBlank() && seed.aliasesCsv.isNotBlank()) ||
                        (current.applicableCategoriesCsv != seed.applicableCategoriesCsv) ||
                        (current.subsidySchemeCodesCsv != seed.subsidySchemeCodesCsv)
                    ) -> MachineryProductEntity.fromModel(seed)
                else -> null
            }
        }
        if (toUpsert.isNotEmpty()) {
            dao.upsertProducts(toUpsert)
        }

        val existingDealers = dao.getAllDealersOnce().associateBy { it.dealerId }
        val missingDealers = MachinerySeedCatalog.initialPendingDealers
            .filter { !existingDealers.containsKey(it.dealerId) }
            .map { MachineryDealerEntity.fromModel(it) }
        if (missingDealers.isNotEmpty()) {
            dao.upsertDealers(missingDealers)
        }

        val existingManufacturers = dao.getAllManufacturersOnce().associateBy { it.id }
        val missingManufacturers = MachinerySeedCatalog.initialManufacturers
            .filter { !existingManufacturers.containsKey(it.id) }
            .map { MachineryManufacturerEntity.fromModel(it) }
        if (missingManufacturers.isNotEmpty()) {
            dao.upsertManufacturers(missingManufacturers)
        }

        val existingOffers = dao.getAllOffersOnce().associateBy { it.offerId }
        val missingOffers = MachinerySeedCatalog.initialDealerOffers
            .filter { !existingOffers.containsKey(it.offerId) }
            .map { MachineryDealerOfferEntity.fromModel(it) }
        if (missingOffers.isNotEmpty()) {
            dao.upsertOffers(missingOffers)
        }

        val existingResearch = dao.getAllResearchQueueOnce().associateBy { it.id }
        val missingResearch = MachinerySeedCatalog.initialResearchQueue
            .filter { !existingResearch.containsKey(it.id) }
            .map { MachineryResearchQueueEntity.fromModel(it) }
        if (missingResearch.isNotEmpty()) {
            dao.upsertResearchItems(missingResearch)
        }
    }

    suspend fun uploadEquipmentPhotoToStorage(
        machineId: String,
        photoUri: Uri
    ): Result<UploadedEquipmentPhoto> = MachineryImageStorageManager.uploadEquipmentPhoto(context, machineId, photoUri)

    suspend fun pullProductsFromFirestore(): Int {
        val db = firestore ?: return 0
        val user = try { Firebase.auth.currentUser } catch (_: Exception) { null }
        if (user == null || user.uid.isBlank()) return 0
        return try {
            val snap = db.collection("machinery_products").get().await()
            val remote = snap.documents.mapNotNull { d ->
                val nameEn = d.getString("nameEn")?.trim().orEmpty()
                if (nameEn.isBlank()) return@mapNotNull null
                MachineryProduct(
                    id = d.id,
                    sellerUserId = d.getString("sellerUserId") ?: "",
                    dealerId = d.getString("dealerId") ?: "",
                    sellerBusinessName = d.getString("sellerBusinessName") ?: "",
                    sellerLocation = d.getString("sellerLocation") ?: "",
                    sellerLocalityCode = d.getString("sellerLocalityCode") ?: "",
                    sellerPhone = d.getString("sellerPhone"),
                    sellerWhatsapp = d.getString("sellerWhatsapp"),
                    nameEn = nameEn,
                    nameBn = d.getString("nameBn") ?: "",
                    nameHi = d.getString("nameHi") ?: "",
                    categoryCode = d.getString("categoryCode") ?: MachineryCategory.TRACTORS_LAND_PREP.code,
                    subcategory = d.getString("subcategory") ?: "",
                    brand = d.getString("brand") ?: "",
                    modelNumber = d.getString("modelNumber") ?: "",
                    condition = d.getString("condition") ?: "NEW",
                    forSale = d.getBoolean("forSale") ?: true,
                    forRent = d.getBoolean("forRent") ?: false,
                    salePriceInr = d.getDouble("salePriceInr"),
                    photosCsv = d.getString("photosCsv") ?: "",
                    firebaseStoragePathsCsv = d.getString("firebaseStoragePathsCsv") ?: "",
                    listingOrigin = d.getString("listingOrigin") ?: "SELLER_LISTING",
                    approvalStatus = d.getString("approvalStatus") ?: "PENDING_APPROVAL",
                    verificationBadgeText = d.getString("verificationBadgeText") ?: "Synced Listing",
                    requiresDgcaCompliance = d.getBoolean("requiresDgcaCompliance") ?: false,
                    requiresEarthmovingSafety = d.getBoolean("requiresEarthmovingSafety") ?: false,
                    brandVerified = d.getBoolean("brandVerified") ?: false,
                    modelVerified = d.getBoolean("modelVerified") ?: false,
                    pendingAdminReview = d.getBoolean("pendingAdminReview") ?: false,
                    adminReviewNote = d.getString("adminReviewNote") ?: "",
                    hireServiceTypeCode = d.getString("hireServiceTypeCode") ?: "",
                    aliasesCsv = d.getString("aliasesCsv") ?: "",
                    applicableCategoriesCsv = d.getString("applicableCategoriesCsv") ?: "",
                    subsidySchemeCodesCsv = d.getString("subsidySchemeCodesCsv") ?: "",
                    sourceType = d.getString("sourceType") ?: "PARTNER_SUBMISSION"
                )
            }
            if (remote.isNotEmpty()) {
                dao.upsertProducts(remote.map { MachineryProductEntity.fromModel(it) })
            }
            remote.size
        } catch (e: Exception) {
            Log.w("MachineryRepo", "Product pull deferred: ${e.message}")
            0
        }
    }

    suspend fun resolveEquipmentReviewByAdmin(
        machineId: String,
        verifiedBrand: String,
        verifiedModel: String,
        markVerified: Boolean,
        adminEmail: String,
        reviewNote: String = ""
    ): Result<Unit> {
        val existing = dao.getProductById(machineId)?.toModel()
            ?: return Result.failure(NoSuchElementException("Machine $machineId not found"))
        val updated = existing.copy(
            brand = verifiedBrand.trim().ifBlank { existing.brand },
            modelNumber = verifiedModel.trim().ifBlank { existing.modelNumber },
            brandVerified = markVerified && verifiedBrand.isNotBlank(),
            modelVerified = markVerified && verifiedModel.isNotBlank() &&
                !verifiedModel.contains("Pending", ignoreCase = true),
            pendingAdminReview = !markVerified,
            adminReviewNote = reviewNote.trim(),
            verificationBadgeText = if (markVerified) "Admin Verified Equipment" else "Admin Review Required",
            infoVerificationStatus = if (markVerified) "ADMIN_VERIFIED" else "PENDING_REVIEW",
            lastUpdatedDate = todayIso(),
            verificationDate = todayIso(),
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertProduct(MachineryProductEntity.fromModel(updated))
        pushProductToFirestore(updated)
        logAdminAudit(
            MachineryAdminAuditLog(
                id = "audit_${System.currentTimeMillis()}",
                adminEmail = adminEmail,
                action = if (markVerified) "VERIFY_EQUIPMENT_SPEC" else "FLAG_EQUIPMENT_REVIEW",
                targetId = machineId,
                summary = "${updated.nameEn} | brand=${updated.brand} | model=${updated.modelNumber}"
            )
        )
        return Result.success(Unit)
    }

    // -------------------------------------------------------------------------
    // Manufacturers Directory CRUD
    // -------------------------------------------------------------------------
    suspend fun upsertManufacturer(
        manufacturer: MachineryManufacturer,
        adminEmail: String
    ): Result<MachineryManufacturer> {
        return try {
            require(manufacturer.nameEn.isNotBlank()) { "Manufacturer name is required" }
            val clean = manufacturer.copy(
                id = manufacturer.id.ifBlank { "oem_${System.currentTimeMillis()}" },
                lastVerifiedDate = todayIso()
            )
            dao.upsertManufacturer(MachineryManufacturerEntity.fromModel(clean))
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("machinery_manufacturers")?.document(clean.id)?.set(
                        mapOf(
                            "nameEn" to clean.nameEn.take(150),
                            "nameBn" to clean.nameBn.take(150),
                            "nameHi" to clean.nameHi.take(150),
                            "countryOrOrigin" to clean.countryOrOrigin.take(60),
                            "focusSummaryEn" to clean.focusSummaryEn.take(400),
                            "officialPortalUrl" to clean.officialPortalUrl.take(300),
                            "dealerLocatorUrl" to clean.dealerLocatorUrl.take(300),
                            "servicePortalUrl" to clean.servicePortalUrl.take(300),
                            "customerCareHelpline" to clean.customerCareHelpline.take(120),
                            "supportedCategoriesCsv" to clean.supportedCategoriesCsv.take(300),
                            "isVerifiedOfficialLink" to clean.isVerifiedOfficialLink,
                            "verificationStatus" to clean.verificationStatus,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )?.await()
                }
            } catch (_: Exception) {}
            logAdminAudit(
                MachineryAdminAuditLog(
                    id = "audit_${System.currentTimeMillis()}",
                    adminEmail = adminEmail,
                    action = "UPSERT_MANUFACTURER",
                    targetId = clean.id,
                    summary = clean.nameEn
                )
            )
            Result.success(clean)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------------------
    // Many-to-Many Dealer Offers
    // -------------------------------------------------------------------------
    suspend fun upsertDealerOffer(
        offer: MachineryDealerOffer,
        actorEmail: String
    ): Result<MachineryDealerOffer> {
        return try {
            require(offer.machineId.isNotBlank()) { "Machine ID is required" }
            require(offer.dealerId.isNotBlank() || offer.dealerName.isNotBlank()) { "Dealer identity is required" }
            val clean = offer.copy(
                offerId = offer.offerId.ifBlank { "offer_${System.currentTimeMillis()}_${(100..999).random()}" },
                lastPriceUpdatedDate = todayIso(),
                updatedAtMillis = System.currentTimeMillis()
            )
            dao.upsertOffer(MachineryDealerOfferEntity.fromModel(clean))
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("machinery_dealer_offers")?.document(clean.offerId)?.set(
                        mapOf(
                            "machineId" to clean.machineId,
                            "dealerId" to clean.dealerId,
                            "dealerName" to clean.dealerName.take(200),
                            "localityCode" to clean.localityCode,
                            "modelNumber" to clean.modelNumber.take(120),
                            "condition" to clean.condition,
                            "offersSale" to clean.offersSale,
                            "offersRent" to clean.offersRent,
                            "mrpInr" to clean.mrpInr,
                            "dealerPriceInr" to clean.dealerPriceInr,
                            "indicativePriceInr" to clean.indicativePriceInr,
                            "rentalHourlyInr" to clean.rentalHourlyInr,
                            "rentalDailyInr" to clean.rentalDailyInr,
                            "rentalPerAcreInr" to clean.rentalPerAcreInr,
                            "stockStatus" to clean.stockStatus,
                            "verificationStatus" to clean.verificationStatus,
                            "isOutdatedPrice" to clean.isOutdatedPrice,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )?.await()
                }
            } catch (_: Exception) {}
            logAdminAudit(
                MachineryAdminAuditLog(
                    id = "audit_${System.currentTimeMillis()}",
                    adminEmail = actorEmail,
                    action = "UPSERT_DEALER_OFFER",
                    targetId = clean.offerId,
                    summary = "${clean.machineId} <-> ${clean.dealerName} (${clean.stockStatus})"
                )
            )
            Result.success(clean)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun flagOfferOutdated(offerId: String, isOutdated: Boolean, adminEmail: String): Result<Unit> {
        val current = dao.getAllOffersOnce().firstOrNull { it.offerId == offerId }?.toModel()
            ?: return Result.failure(NoSuchElementException("Offer $offerId not found"))
        val updated = current.copy(isOutdatedPrice = isOutdated, updatedAtMillis = System.currentTimeMillis())
        dao.upsertOffer(MachineryDealerOfferEntity.fromModel(updated))
        logAdminAudit(
            MachineryAdminAuditLog(
                id = "audit_${System.currentTimeMillis()}",
                adminEmail = adminEmail,
                action = if (isOutdated) "FLAG_OFFER_OUTDATED" else "CLEAR_OFFER_OUTDATED",
                targetId = offerId,
                summary = "${updated.dealerName} for ${updated.machineId}"
            )
        )
        return Result.success(Unit)
    }

    suspend fun deleteOffer(offerId: String, adminEmail: String): Result<Unit> {
        dao.deleteOfferById(offerId)
        try {
            if (firestore != null && Firebase.auth.currentUser != null) {
                firestore?.collection("machinery_dealer_offers")?.document(offerId)?.delete()?.await()
            }
        } catch (_: Exception) {}
        logAdminAudit(
            MachineryAdminAuditLog(
                id = "audit_${System.currentTimeMillis()}",
                adminEmail = adminEmail,
                action = "DELETE_DEALER_OFFER",
                targetId = offerId,
                summary = "Removed dealer offer $offerId"
            )
        )
        return Result.success(Unit)
    }

    // -------------------------------------------------------------------------
    // Farmer Reviews & Ratings
    // -------------------------------------------------------------------------
    suspend fun submitReview(review: MachineryReviewRecord): Result<MachineryReviewRecord> {
        return try {
            require(review.machineId.isNotBlank()) { "Machine ID is required" }
            require(review.comment.trim().length >= 5) { "Please write a brief review (min 5 characters)" }
            val boundedRating = review.rating.coerceIn(1, 5)
            val clean = review.copy(
                id = review.id.ifBlank { "rev_${System.currentTimeMillis()}_${(100..999).random()}" },
                rating = boundedRating,
                title = review.title.trim().take(120),
                comment = review.comment.trim().take(1000),
                createdAtMillis = System.currentTimeMillis()
            )
            dao.upsertReview(MachineryReviewEntity.fromModel(clean))
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("machinery_reviews")?.document(clean.id)?.set(
                        mapOf(
                            "machineId" to clean.machineId,
                            "userId" to clean.userId,
                            "userName" to clean.userName.take(100),
                            "rating" to clean.rating,
                            "title" to clean.title,
                            "comment" to clean.comment,
                            "verifiedFarmerFlag" to clean.verifiedFarmerFlag,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )?.await()
                }
            } catch (_: Exception) {}
            Result.success(clean)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReview(reviewId: String, adminEmail: String): Result<Unit> {
        dao.deleteReviewById(reviewId)
        try {
            if (firestore != null && Firebase.auth.currentUser != null) {
                firestore?.collection("machinery_reviews")?.document(reviewId)?.delete()?.await()
            }
        } catch (_: Exception) {}
        logAdminAudit(
            MachineryAdminAuditLog(
                id = "audit_${System.currentTimeMillis()}",
                adminEmail = adminEmail,
                action = "MODERATE_REVIEW_DELETE",
                targetId = reviewId,
                summary = "Deleted review $reviewId"
            )
        )
        return Result.success(Unit)
    }

    // -------------------------------------------------------------------------
    // Admin Research Queue
    // -------------------------------------------------------------------------
    suspend fun submitResearchItem(item: MachineryResearchQueueItem): Result<MachineryResearchQueueItem> {
        return try {
            require(item.equipmentName.isNotBlank()) { "Equipment name is required" }
            val clean = item.copy(
                id = item.id.ifBlank { "rq_${System.currentTimeMillis()}" },
                updatedAtMillis = System.currentTimeMillis()
            )
            dao.upsertResearchItem(MachineryResearchQueueEntity.fromModel(clean))
            Result.success(clean)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resolveResearchItem(
        itemId: String,
        newStatus: String,
        adminEmail: String,
        note: String = ""
    ): Result<Unit> {
        val current = dao.getAllResearchQueueOnce().firstOrNull { it.id == itemId }?.toModel()
            ?: return Result.failure(NoSuchElementException("Research item $itemId not found"))
        val updated = current.copy(
            status = newStatus,
            missingFieldsNote = if (note.isNotBlank()) note else current.missingFieldsNote,
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertResearchItem(MachineryResearchQueueEntity.fromModel(updated))
        logAdminAudit(
            MachineryAdminAuditLog(
                id = "audit_${System.currentTimeMillis()}",
                adminEmail = adminEmail,
                action = "RESOLVE_RESEARCH_QUEUE",
                targetId = itemId,
                summary = "${updated.equipmentName} -> $newStatus"
            )
        )
        return Result.success(Unit)
    }

    // -------------------------------------------------------------------------
    // Favourites & Listings
    // -------------------------------------------------------------------------
    suspend fun toggleFavourite(userId: String, machineId: String, currentlySaved: Boolean) {
        val compId = "$userId::$machineId"
        if (currentlySaved) {
            dao.removeFavourite(compId)
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("machinery_favourites")?.document(compId)?.delete()?.await()
                }
            } catch (_: Exception) {}
        } else {
            val fav = MachineryFavouriteEntity(compId, userId, machineId, System.currentTimeMillis())
            dao.addFavourite(fav)
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("machinery_favourites")?.document(compId)?.set(
                        mapOf(
                            "userId" to userId,
                            "machineId" to machineId,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )?.await()
                }
            } catch (_: Exception) {}
        }
    }

    suspend fun submitSellerMachineListing(
        product: MachineryProduct,
        isAdmin: Boolean
    ): Result<MachineryProduct> {
        return try {
            require(product.nameEn.isNotBlank()) { "Machine name is required" }
            require(product.sellerBusinessName.isNotBlank()) { "Business name is required" }
            val cleanId = product.id.ifBlank {
                "mach_${System.currentTimeMillis()}_${(100..999).random()}"
            }
            val needsDroneCompliance = product.categoryCode == MachineryCategory.SMART_PRECISION_AG.code &&
                product.subcategory.contains("Drone", ignoreCase = true)
            val needsEarthSafety = product.categoryCode == MachineryCategory.SOIL_EARTH_MOVING.code

            val normalized = product.copy(
                id = cleanId,
                listingOrigin = "SELLER_LISTING",
                approvalStatus = if (isAdmin) "APPROVED" else "PENDING_APPROVAL",
                verificationBadgeText = if (isAdmin) "Admin Verified Seller Listing" else "Pending Admin Review",
                requiresDgcaCompliance = product.requiresDgcaCompliance || needsDroneCompliance,
                requiresEarthmovingSafety = product.requiresEarthmovingSafety || needsEarthSafety,
                lastUpdatedDate = todayIso(),
                updatedAtMillis = System.currentTimeMillis()
            )
            dao.upsertProduct(MachineryProductEntity.fromModel(normalized))
            pushProductToFirestore(normalized)
            Result.success(normalized)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveOrRejectMachineListing(
        machineId: String,
        approve: Boolean,
        adminEmail: String,
        note: String = ""
    ): Result<Unit> {
        val existing = dao.getProductById(machineId)?.toModel()
            ?: return Result.failure(NoSuchElementException("Machine $machineId not found"))
        val updated = existing.copy(
            approvalStatus = if (approve) "APPROVED" else "REJECTED",
            verificationBadgeText = if (approve) "Verified Partner Listing" else "Rejected: $note",
            lastUpdatedDate = todayIso(),
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertProduct(MachineryProductEntity.fromModel(updated))
        pushProductToFirestore(updated)
        logAdminAudit(
            MachineryAdminAuditLog(
                id = "audit_${System.currentTimeMillis()}",
                adminEmail = adminEmail,
                action = if (approve) "APPROVE_MACHINE_LISTING" else "REJECT_MACHINE_LISTING",
                targetId = machineId,
                summary = "${updated.nameEn} -> ${updated.approvalStatus} $note"
            )
        )
        return Result.success(Unit)
    }

    suspend fun deleteMachineListing(machineId: String, adminEmail: String): Result<Unit> {
        return try {
            dao.deleteProductById(machineId)
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("machinery_products")?.document(machineId)?.delete()?.await()
                }
            } catch (_: Exception) {}
            logAdminAudit(
                MachineryAdminAuditLog(
                    id = "audit_${System.currentTimeMillis()}",
                    adminEmail = adminEmail,
                    action = "DELETE_MACHINE_LISTING",
                    targetId = machineId,
                    summary = "Removed listing $machineId"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------------------
    // Admin Duplicate Detection, Merging, CSV Preview & Batch Rollback
    // -------------------------------------------------------------------------
    suspend fun findDuplicateProductGroups(): List<DuplicateMachineryGroup> {
        val all = dao.getAllProductsOnce().map { it.toModel() }
        val genericBrands = setOf("", "multi-brand class", "pending verification", "generic", "oem compatible class")
        val groups = mutableListOf<DuplicateMachineryGroup>()

        // Group 1: exact normalized name
        all.groupBy { it.nameEn.trim().lowercase() }
            .filter { (key, list) -> key.isNotBlank() && list.size > 1 }
            .forEach { (name, list) ->
                groups.add(DuplicateMachineryGroup("Exact Name: $name", list))
            }

        // Group 2: matching verified brand + model
        val seenIds = groups.flatMap { it.products.map { p -> p.id } }.toSet()
        all.filter { it.id !in seenIds }
            .groupBy { "${it.brand.trim().lowercase()}::${it.modelNumber.trim().lowercase()}" }
            .filter { (key, list) ->
                val b = key.substringBefore("::")
                val m = key.substringAfter("::")
                b !in genericBrands && m.isNotBlank() && !m.contains("pending") && list.size > 1
            }
            .forEach { (key, list) ->
                groups.add(DuplicateMachineryGroup("Brand + Model: ${key.replace("::", " ")}", list))
            }

        return groups
    }

    suspend fun mergeDuplicateProducts(
        keepId: String,
        duplicateId: String,
        adminEmail: String
    ): Result<Unit> {
        if (keepId == duplicateId) {
            return Result.failure(IllegalArgumentException("Keep ID and Duplicate ID must be different"))
        }
        val keep = dao.getProductById(keepId)?.toModel()
            ?: return Result.failure(NoSuchElementException("Primary product $keepId not found"))
        val dup = dao.getProductById(duplicateId)?.toModel()
            ?: return Result.failure(NoSuchElementException("Duplicate product $duplicateId not found"))

        val mergedPhotos = (keep.getPhotoTokens() + dup.getPhotoTokens()).distinct().joinToString(",")
        val mergedAliases = (keep.aliasesCsv.split(",") + dup.aliasesCsv.split(","))
            .map { it.trim() }.filter { it.isNotBlank() }.distinct().joinToString(",")
        val updatedKeep = keep.copy(
            photosCsv = mergedPhotos,
            aliasesCsv = mergedAliases,
            updatedAtMillis = System.currentTimeMillis(),
            lastUpdatedDate = todayIso()
        )
        dao.upsertProduct(MachineryProductEntity.fromModel(updatedKeep))
        dao.deleteProductById(duplicateId)
        logAdminAudit(
            MachineryAdminAuditLog(
                id = "audit_${System.currentTimeMillis()}",
                adminEmail = adminEmail,
                action = "MERGE_DUPLICATE_MACHINERY",
                targetId = keepId,
                summary = "Merged $duplicateId (${dup.nameEn}) into $keepId (${keep.nameEn})"
            )
        )
        return Result.success(Unit)
    }

    fun previewCsvImport(csvText: String): CsvMachineryPreview {
        val lines = csvText.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList()
        if (lines.isEmpty()) return CsvMachineryPreview(emptyList(), listOf("CSV input is empty"))

        val valid = mutableListOf<MachineryProduct>()
        val errors = mutableListOf<String>()
        val startIdx = if (lines.first().lowercase().contains("nameen") || lines.first().lowercase().contains("category")) 1 else 0

        for (i in startIdx until lines.size) {
            val cols = lines[i].split(",").map { it.trim() }
            if (cols.size < 3) {
                errors.add("Row ${i + 1}: Needs at least nameEn, categoryCode, sellerBusinessName")
                continue
            }
            val nameEn = cols.getOrNull(0).orEmpty()
            val catRaw = cols.getOrNull(1).orEmpty()
            val subcat = cols.getOrNull(2).orEmpty()
            val brand = cols.getOrNull(3).orEmpty().ifBlank { "Multi-Brand Class" }
            val model = cols.getOrNull(4).orEmpty().ifBlank { "Pending Verification" }
            val sellerName = cols.getOrNull(5).orEmpty().ifBlank { "Admin CSV Catalog Import" }
            val hp = cols.getOrNull(6).orEmpty()
            val fuel = cols.getOrNull(7).orEmpty()
            val crops = cols.getOrNull(8).orEmpty()

            if (nameEn.isBlank()) {
                errors.add("Row ${i + 1}: Equipment nameEn cannot be blank")
                continue
            }
            val validCat = MachineryCategory.values().firstOrNull { it.code.equals(catRaw, ignoreCase = true) }
            if (validCat == null) {
                errors.add("Row ${i + 1}: Unknown categoryCode '$catRaw'")
                continue
            }
            valid.add(
                MachineryProduct(
                    id = "csv_${System.currentTimeMillis()}_${i}_${(10..99).random()}",
                    sellerUserId = "admin_csv",
                    sellerBusinessName = sellerName,
                    sellerLocation = "Siliguri / North Bengal",
                    sellerLocalityCode = "SILIGURI",
                    nameEn = nameEn,
                    categoryCode = validCat.code,
                    subcategory = subcat.ifBlank { validCat.subcategoriesEn.first() },
                    brand = brand,
                    modelNumber = model,
                    horsepowerOrCapacity = hp,
                    fuelOrPowerType = fuel,
                    suitableCrops = crops,
                    listingOrigin = "SELLER_LISTING",
                    approvalStatus = "APPROVED",
                    verificationBadgeText = "Admin CSV Verified",
                    brandVerified = !brand.contains("Pending", ignoreCase = true),
                    modelVerified = !model.contains("Pending", ignoreCase = true)
                )
            )
        }
        return CsvMachineryPreview(validRows = valid, errors = errors)
    }

    suspend fun commitCsvImportWithBatch(
        rows: List<MachineryProduct>,
        adminEmail: String
    ): Result<Pair<String, Int>> {
        if (rows.isEmpty()) return Result.failure(IllegalArgumentException("No valid CSV rows to import"))
        val batchId = "batch_${System.currentTimeMillis()}"
        val tagged = rows.map { p ->
            p.copy(
                sourceType = "CSV_BATCH:$batchId",
                adminReviewNote = "[CSV_BATCH:$batchId] ${p.adminReviewNote}".trim(),
                lastUpdatedDate = todayIso(),
                updatedAtMillis = System.currentTimeMillis()
            )
        }
        dao.upsertProducts(tagged.map { MachineryProductEntity.fromModel(it) })
        logAdminAudit(
            MachineryAdminAuditLog(
                id = "audit_${System.currentTimeMillis()}",
                adminEmail = adminEmail,
                action = "CSV_BATCH_IMPORT",
                targetId = batchId,
                summary = "Imported ${tagged.size} machinery rows under batch $batchId"
            )
        )
        return Result.success(batchId to tagged.size)
    }

    suspend fun rollbackCsvImportBatch(batchId: String, adminEmail: String): Result<Int> {
        val cleanBatch = batchId.trim()
        if (cleanBatch.isBlank()) return Result.failure(IllegalArgumentException("Batch ID required"))
        val matching = dao.getAllProductsOnce().filter {
            it.sourceType == "CSV_BATCH:$cleanBatch" || it.adminReviewNote.contains("[CSV_BATCH:$cleanBatch]")
        }
        matching.forEach { dao.deleteProductById(it.id) }
        logAdminAudit(
            MachineryAdminAuditLog(
                id = "audit_${System.currentTimeMillis()}",
                adminEmail = adminEmail,
                action = "CSV_BATCH_ROLLBACK",
                targetId = cleanBatch,
                summary = "Rolled back ${matching.size} rows for batch $cleanBatch"
            )
        )
        return Result.success(matching.size)
    }

    // -------------------------------------------------------------------------
    // Dealers & Verification
    // -------------------------------------------------------------------------
    suspend fun submitDealerClaimRequest(
        dealerId: String,
        userId: String,
        claimantName: String,
        phone: String,
        whatsapp: String?,
        address: String,
        brands: String,
        consentConfirmedByClaimant: Boolean
    ): Result<Unit> {
        if (!consentConfirmedByClaimant) {
            return Result.failure(IllegalArgumentException("Explicit owner consent confirmation is required to claim a profile"))
        }
        val existing = dao.getDealerById(dealerId)?.toModel()
            ?: return Result.failure(NoSuchElementException("Dealer $dealerId not found"))
        val updated = existing.copy(
            claimedByUserId = userId,
            claimNote = "Claimed by $claimantName | Submitted Phone: $phone | Addr: $address | Brands: $brands",
            ownerConsentStatus = "OWNER_SUBMITTED_AWAITING_ADMIN",
            verificationStatus = NurseryVerificationStatus.PENDING_VERIFICATION,
            isVerified = false,
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertDealer(MachineryDealerEntity.fromModel(updated))
        pushVerificationClaimToFirestore(dealerId, userId, updated.claimNote.orEmpty())
        return Result.success(Unit)
    }

    suspend fun verifyOrEditDealerByAdmin(
        dealer: MachineryDealerEntry,
        markVerified: Boolean,
        markConsentConfirmed: Boolean,
        markAuthorisedDealer: Boolean,
        adminEmail: String
    ): Result<Unit> {
        val cleanStatus = if (markVerified && markConsentConfirmed) {
            NurseryVerificationStatus.ADMIN_VERIFIED
        } else {
            NurseryVerificationStatus.PENDING_VERIFICATION
        }
        val verifiedBool = cleanStatus != NurseryVerificationStatus.PENDING_VERIFICATION
        val updated = dealer.copy(
            verificationStatus = cleanStatus,
            ownerConsentStatus = if (markConsentConfirmed) "CONFIRMED" else "NOT_CONFIRMED",
            isVerified = verifiedBool,
            isAuthorisedDealerVerified = verifiedBool && markAuthorisedDealer,
            lastVerifiedDate = todayIso(),
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertDealer(MachineryDealerEntity.fromModel(updated))
        pushDealerToFirestore(updated)
        logAdminAudit(
            MachineryAdminAuditLog(
                id = "audit_${System.currentTimeMillis()}",
                adminEmail = adminEmail,
                action = if (verifiedBool) "VERIFY_DEALER" else "UPDATE_DEALER",
                targetId = updated.dealerId,
                summary = "${updated.businessName} | status=$cleanStatus | consent=${updated.ownerConsentStatus}"
            )
        )
        return Result.success(Unit)
    }

    // -------------------------------------------------------------------------
    // Inquiries, Rentals & Services (with 5-min duplicate guard + Cancel flow)
    // -------------------------------------------------------------------------
    suspend fun submitInquiryOrReport(record: MachineryInquiryRecord): Result<MachineryInquiryRecord> {
        return try {
            val clean = record.copy(
                id = record.id.ifBlank { "inq_${System.currentTimeMillis()}_${(100..999).random()}" },
                status = BookingLifecycleStatus.REQUESTED_AWAITING_CONFIRMATION,
                createdAtMillis = System.currentTimeMillis()
            )
            dao.upsertInquiry(MachineryInquiryEntity.fromModel(clean))
            trustRepo.logActivityEvent(
                entityType = ActivityEntityType.MACHINERY_INQUIRY,
                entityId = clean.id,
                actorRole = "FARMER",
                actorName = clean.farmerName,
                statusLabel = clean.status,
                note = "${clean.inquiryType}: ${clean.message.take(120)}"
            )
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("machinery_inquiries")?.document(clean.id)?.set(
                        mapOf(
                            "userId" to clean.userId,
                            "farmerName" to clean.farmerName.take(120),
                            "farmerPhone" to clean.farmerPhone.take(30),
                            "machineId" to clean.machineId,
                            "machineName" to clean.machineName.take(200),
                            "sellerUserId" to clean.sellerUserId,
                            "inquiryType" to clean.inquiryType,
                            "message" to clean.message.take(1000),
                            "status" to clean.status,
                            "sellerReply" to clean.sellerReply.take(1000),
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )?.await()
                }
            } catch (_: Exception) {}
            Result.success(clean)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitRentalRequest(request: MachineryRentalRequest): Result<MachineryRentalRequest> {
        return try {
            val now = System.currentTimeMillis()
            val recentSame = dao.observeRentalsForUser(request.userId).firstOrNull().orEmpty().any {
                it.userId == request.userId &&
                    it.machineId == request.machineId &&
                    it.rentalUnit == request.rentalUnit &&
                    it.durationUnits == request.durationUnits &&
                    it.status != BookingLifecycleStatus.CANCELLED &&
                    it.status != BookingLifecycleStatus.REJECTED &&
                    (now - it.createdAtMillis) < 300_000L
            }
            if (recentSame) {
                return Result.failure(IllegalStateException("A matching rental request was already submitted within the last 5 minutes."))
            }

            val clean = request.copy(
                id = request.id.ifBlank { "rent_${now}_${(100..999).random()}" },
                status = BookingLifecycleStatus.REQUESTED_AWAITING_CONFIRMATION,
                createdAtMillis = now
            )
            dao.upsertRental(MachineryRentalEntity.fromModel(clean))
            trustRepo.logActivityEvent(
                entityType = ActivityEntityType.MACHINERY_RENTAL,
                entityId = clean.id,
                actorRole = "FARMER",
                actorName = clean.farmerName,
                statusLabel = clean.status,
                note = "${clean.durationUnits} x ${clean.rentalUnit} (${clean.locality})"
            )
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("machinery_rentals")?.document(clean.id)?.set(
                        mapOf(
                            "userId" to clean.userId,
                            "farmerName" to clean.farmerName.take(120),
                            "farmerPhone" to clean.farmerPhone.take(30),
                            "machineId" to clean.machineId,
                            "machineName" to clean.machineName.take(200),
                            "sellerUserId" to clean.sellerUserId,
                            "locality" to clean.locality.take(120),
                            "rentalUnit" to clean.rentalUnit,
                            "durationUnits" to clean.durationUnits,
                            "withOperator" to clean.withOperator,
                            "deliveryRequired" to clean.deliveryRequired,
                            "notes" to clean.notes.take(800),
                            "status" to clean.status,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )?.await()
                }
            } catch (_: Exception) {}
            Result.success(clean)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelRentalRequest(rentalId: String, userId: String): Result<Unit> {
        val allRentals = dao.observeAllRentals().firstOrNull() ?: emptyList()
        val match = allRentals.firstOrNull { it.id == rentalId }?.toModel()
            ?: return Result.failure(NoSuchElementException("Rental not found"))
        if (match.userId != userId) {
            return Result.failure(SecurityException("Only the requesting user can cancel this rental booking"))
        }
        return updateRentalStatus(rentalId, BookingLifecycleStatus.CANCELLED)
    }

    suspend fun updateRentalStatus(rentalId: String, newStatus: String): Result<Unit> {
        val allRentals = dao.observeAllRentals().firstOrNull() ?: emptyList()
        val match = allRentals.firstOrNull { it.id == rentalId }?.toModel()
            ?: return Result.failure(NoSuchElementException("Rental not found"))
        val updated = match.copy(status = newStatus)
        dao.upsertRental(MachineryRentalEntity.fromModel(updated))
        trustRepo.logActivityEvent(
            entityType = ActivityEntityType.MACHINERY_RENTAL,
            entityId = rentalId,
            actorRole = if (newStatus == BookingLifecycleStatus.CANCELLED) "FARMER" else "SELLER",
            actorName = if (newStatus == BookingLifecycleStatus.CANCELLED) match.farmerName else match.sellerUserId.ifBlank { "Dealer" },
            statusLabel = newStatus,
            note = "Rental status updated to $newStatus"
        )
        try {
            if (firestore != null && Firebase.auth.currentUser != null) {
                firestore?.collection("machinery_rentals")?.document(rentalId)
                    ?.update(mapOf("status" to newStatus, "updatedAt" to FieldValue.serverTimestamp()))
                    ?.await()
            }
        } catch (_: Exception) {}
        return Result.success(Unit)
    }

    suspend fun updateInquiryReply(inquiryId: String, reply: String, newStatus: String): Result<Unit> {
        val allInquiries = dao.observeAllInquiries().firstOrNull() ?: emptyList()
        val match = allInquiries.firstOrNull { it.id == inquiryId }?.toModel()
            ?: return Result.failure(NoSuchElementException("Inquiry not found"))
        val updated = match.copy(sellerReply = reply.take(1000), status = newStatus)
        dao.upsertInquiry(MachineryInquiryEntity.fromModel(updated))
        trustRepo.logActivityEvent(
            entityType = ActivityEntityType.MACHINERY_INQUIRY,
            entityId = inquiryId,
            actorRole = "SELLER",
            actorName = match.sellerUserId.ifBlank { "Dealer" },
            statusLabel = newStatus,
            note = reply.take(160)
        )
        try {
            if (firestore != null && Firebase.auth.currentUser != null) {
                firestore?.collection("machinery_inquiries")?.document(inquiryId)
                    ?.update(
                        mapOf(
                            "sellerReply" to updated.sellerReply,
                            "status" to newStatus,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )?.await()
            }
        } catch (_: Exception) {}
        return Result.success(Unit)
    }

    suspend fun cancelServiceRequest(serviceId: String, userId: String): Result<Unit> {
        val allServices = dao.observeAllServices().firstOrNull() ?: emptyList()
        val match = allServices.firstOrNull { it.id == serviceId }?.toModel()
            ?: return Result.failure(NoSuchElementException("Service request not found"))
        if (match.userId != userId) {
            return Result.failure(SecurityException("Only the requesting user can cancel this service request"))
        }
        return updateServiceStatus(serviceId, BookingLifecycleStatus.CANCELLED)
    }

    suspend fun updateServiceStatus(serviceId: String, newStatus: String): Result<Unit> {
        val allServices = dao.observeAllServices().firstOrNull() ?: emptyList()
        val match = allServices.firstOrNull { it.id == serviceId }?.toModel()
            ?: return Result.failure(NoSuchElementException("Service request not found"))
        val updated = match.copy(status = newStatus)
        dao.upsertServiceRequest(MachineryServiceRequestEntity.fromModel(updated))
        trustRepo.logActivityEvent(
            entityType = ActivityEntityType.MACHINERY_SERVICE,
            entityId = serviceId,
            actorRole = if (newStatus == BookingLifecycleStatus.CANCELLED) "FARMER" else "PROVIDER",
            actorName = if (newStatus == BookingLifecycleStatus.CANCELLED) match.farmerName else match.providerUserId.ifBlank { "Service Provider" },
            statusLabel = newStatus,
            note = "Service status updated to $newStatus"
        )
        try {
            if (firestore != null && Firebase.auth.currentUser != null) {
                firestore?.collection("machinery_service_requests")?.document(serviceId)
                    ?.update(mapOf("status" to newStatus, "updatedAt" to FieldValue.serverTimestamp()))
                    ?.await()
            }
        } catch (_: Exception) {}
        return Result.success(Unit)
    }

    suspend fun submitServiceRequest(req: MachineryServiceRequest): Result<MachineryServiceRequest> {
        return try {
            val now = System.currentTimeMillis()
            val recentSame = dao.observeServicesForUser(req.userId).firstOrNull().orEmpty().any {
                it.userId == req.userId &&
                    it.serviceTypeCode == req.serviceTypeCode &&
                    it.preferredDate == req.preferredDate &&
                    it.status != BookingLifecycleStatus.CANCELLED &&
                    it.status != BookingLifecycleStatus.REJECTED &&
                    (now - it.createdAtMillis) < 300_000L
            }
            if (recentSame) {
                return Result.failure(IllegalStateException("A matching service request was already submitted within the last 5 minutes."))
            }

            val clean = req.copy(
                id = req.id.ifBlank { "srv_${now}_${(100..999).random()}" },
                status = BookingLifecycleStatus.REQUESTED_AWAITING_CONFIRMATION,
                createdAtMillis = now
            )
            dao.upsertServiceRequest(MachineryServiceRequestEntity.fromModel(clean))
            trustRepo.logActivityEvent(
                entityType = ActivityEntityType.MACHINERY_SERVICE,
                entityId = clean.id,
                actorRole = "FARMER",
                actorName = clean.farmerName,
                statusLabel = clean.status,
                note = "${clean.serviceTypeCode} • ${clean.cropName}"
            )
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("machinery_service_requests")?.document(clean.id)?.set(
                        mapOf(
                            "userId" to clean.userId,
                            "farmerName" to clean.farmerName.take(120),
                            "farmerPhone" to clean.farmerPhone.take(30),
                            "serviceTypeCode" to clean.serviceTypeCode,
                            "locality" to clean.locality.take(120),
                            "landSizeText" to clean.landSizeText.take(80),
                            "cropName" to clean.cropName.take(100),
                            "preferredDate" to clean.preferredDate.take(40),
                            "notes" to clean.notes.take(800),
                            "providerUserId" to clean.providerUserId,
                            "status" to clean.status,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )?.await()
                }
            } catch (_: Exception) {}
            Result.success(clean)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * AI & Catalog-Grounded Machinery Recommendation Helper across the full 103+ product catalog,
     * including aliases, multi-category matches, and official SMAM / WBFMS subsidy links.
     */
    suspend fun recommendMachinesForQuery(
        query: MachineryRecommendationQuery,
        lang: AppLanguage
    ): Pair<List<MachineryProduct>, String> {
        ensureSeeded()
        val approved = dao.getAllProductsOnce()
            .map { it.toModel() }
            .filter { it.approvalStatus == "APPROVED" }

        val opLower = query.operationNeed.lowercase()
        val terrainLower = query.soilOrTerrain.lowercase()
        val powerLower = query.powerPreference.lowercase()
        val cropLower = query.cropName.lowercase()
        val queryTokens = opLower.split(" ", ",", "/", "|").map { it.trim() }.filter { it.length >= 3 }

        val scored = approved.map { machine ->
            var score = 0
            if (opLower.contains("plough") || opLower.contains("tillage") || opLower.contains("land") || opLower.contains("rotavator")) {
                if (machine.matchesCategory(MachineryCategory.TRACTORS_LAND_PREP.code)) score += 5
            }
            if (opLower.contains("dig") || opLower.contains("trench") || opLower.contains("pond") || opLower.contains("auger")) {
                if (machine.matchesCategory(MachineryCategory.SOIL_EARTH_MOVING.code)) score += 5
            }
            if (opLower.contains("seed") || opLower.contains("plant") || opLower.contains("transplant") || opLower.contains("sow")) {
                if (machine.matchesCategory(MachineryCategory.SEEDING_PLANTING.code)) score += 5
            }
            if (opLower.contains("irrig") || opLower.contains("water") || opLower.contains("pump") || opLower.contains("drip") || opLower.contains("sprinkler")) {
                if (machine.matchesCategory(MachineryCategory.IRRIGATION_WATER.code)) score += 5
            }
            if (opLower.contains("spray") || opLower.contains("pest") || opLower.contains("weed") || opLower.contains("drone")) {
                if (machine.matchesCategory(MachineryCategory.CROP_PROTECTION_SPRAYING.code)) score += 5
                if (opLower.contains("drone") && machine.requiresDgcaCompliance) score += 12
            }
            if (opLower.contains("harvest") || opLower.contains("reap") || opLower.contains("pluck") || opLower.contains("combine")) {
                if (machine.matchesCategory(MachineryCategory.HARVESTING_MACHINERY.code)) score += 5
            }
            if (opLower.contains("thresh") || opLower.contains("mill") || opLower.contains("dry") || opLower.contains("store") || opLower.contains("chaff")) {
                if (machine.matchesCategory(MachineryCategory.CROP_PROCESSING_STORAGE.code)) score += 5
            }
            if (cropLower.contains("tea") || opLower.contains("নার্স") || cropLower.contains("garden") || opLower.contains("brush") || opLower.contains("chain")) {
                if (machine.matchesCategory(MachineryCategory.GARDENING_TEA_HORTICULTURE.code) ||
                    machine.suitableCrops.lowercase().contains("tea")) score += 4
            }
            if (opLower.contains("sensor") || opLower.contains("gps") || opLower.contains("weather") || opLower.contains("soil test")) {
                if (machine.matchesCategory(MachineryCategory.SMART_PRECISION_AG.code)) score += 6
            }
            if (opLower.contains("spare") || opLower.contains("tyre") || opLower.contains("battery") || opLower.contains("belt") || opLower.contains("bearing")) {
                if (machine.matchesCategory(MachineryCategory.SPARE_PARTS_WORKSHOP.code)) score += 6
            }
            queryTokens.forEach { token ->
                if (machine.matchesSearch(token)) score += 4
            }
            if (powerLower.isNotBlank() && machine.fuelOrPowerType.lowercase().contains(powerLower)) score += 3
            if (query.landSizeAcres <= 2.0 && machine.landSizeSuitability.lowercase().contains("small")) score += 2
            if (terrainLower.contains("hill") && machine.suitableSoilOrTerrain.lowercase().contains("hill")) score += 3
            machine to score
        }.sortedByDescending { it.second }.take(5).map { it.first }

        val disclaimer = when (lang) {
            AppLanguage.BENGALI -> "বিঃদ্রঃ: নিচের পরামর্শ কেবল কৃষি-প্রকৌশল রেফারেন্স। কেনা বা ভাড়া নেওয়ার আগে যাচাইকৃত ডিলার ও কৃষি দপ্তরের সাথে বর্তমান দাম ও ভর্তুকি নিশ্চিত করুন।"
            AppLanguage.HINDI -> "नोट: यह अनुशंसा केवल कृषि-इंजीनियरिंग संदर्भ के लिए है। खरीदने या किराए से पहले सत्यापित डीलर से वर्तमान मूल्य व सब्सिडी की पुष्टि करें।"
            AppLanguage.ENGLISH -> "Note: Recommendations below come strictly from approved catalog & engineering reference data. Confirm live prices, stock, and subsidy eligibility directly with verified dealers."
        }

        val subsidyNote = when (lang) {
            AppLanguage.BENGALI -> "• সরকারি যন্ত্রপাতি ভর্তুকি পোর্টাল (শুধু তথ্য লিঙ্ক — অনুমোদনের নিশ্চয়তা নয়): SMAM (https://agrimachinery.nic.in/) ও পশ্চিমবঙ্গ WBFMS (https://wbfms.wb.gov.in/)। কোনো অজানা ব্যক্তিকে আধার/OTP বা ব্যাংক তথ্য দেবেন না।"
            AppLanguage.HINDI -> "• सरकारी कृषि यंत्र सब्सिडी पोर्टल (केवल जानकारी लिंक — स्वीकृति की गारंटी नहीं): SMAM (https://agrimachinery.nic.in/) व पश्चिम बंगाल WBFMS (https://wbfms.wb.gov.in/)। किसी अनजान को आधार/OTP या बैंक विवरण न दें।"
            AppLanguage.ENGLISH -> "• Official Subsidy Information Portals (no approval guarantee; verify with Block Agriculture Officer): SMAM (https://agrimachinery.nic.in/) & West Bengal FAMS/WBFMS (https://wbfms.wb.gov.in/). Never share Aadhaar, OTP, or bank info with unverified callers."
        }

        val summary = buildString {
            append(disclaimer)
            append("\n• Plot: ${query.landSizeAcres} acres (${query.cropName.ifBlank { "Mixed Crop" }}) • ${query.soilOrTerrain.ifBlank { "General terrain" }}\n")
            if (!query.skilledOperatorAvailable) {
                append(
                    when (lang) {
                        AppLanguage.BENGALI -> "• দক্ষ অপারেটর না থাকায় অপারেটর-সহ ভাড়া বা সহজে ব্যবহারযোগ্য যন্ত্রকে অগ্রাধিকার দেওয়া হয়েছে।\n"
                        AppLanguage.HINDI -> "• कुशल ऑपरेटर उपलब्ध न होने के कारण ऑपरेटर-सहित किराया या आसान मशीनें सुझाई गई हैं।\n"
                        AppLanguage.ENGLISH -> "• Since a skilled operator is unavailable, consider renting with an operator or walk-behind / low-complexity tools.\n"
                    }
                )
            }
            if (query.landSizeAcres < 2.0 && scored.any { it.categoryCode == MachineryCategory.TRACTORS_LAND_PREP.code }) {
                append(
                    when (lang) {
                        AppLanguage.BENGALI -> "• ছোট জমির (<২ একর) জন্য বড় ট্রাক্টর কেনার চেয়ে পাওয়ার টিলার বা কাস্টম-হায়ারিং ভাড়া নেওয়া অর্থনৈতিকভাবে সাশ্রয়ী হতে পারে।\n"
                        AppLanguage.HINDI -> "• छोटे खेत (<2 एकड़) के लिए बड़ा ट्रैक्टर खरीदने की बजाय पावर टिलर या कस्टम-हायरिंग किराया अधिक किफायती हो सकता है।\n"
                        AppLanguage.ENGLISH -> "• For small plots (<2 acres), hiring on custom-rental or using a Power Tiller may be more cost-effective than purchasing a full 4WD tractor.\n"
                    }
                )
            }
            append(subsidyNote).append("\n")
        }
        return scored to summary
    }

    private suspend fun pushProductToFirestore(p: MachineryProduct) {
        try {
            val db = firestore ?: return
            val user = Firebase.auth.currentUser ?: return
            if (user.uid.isBlank()) return
            db.collection("machinery_products").document(p.id).set(
                hashMapOf(
                    "sellerUserId" to p.sellerUserId,
                    "dealerId" to p.dealerId,
                    "sellerBusinessName" to p.sellerBusinessName.take(200),
                    "sellerLocation" to p.sellerLocation.take(200),
                    "sellerLocalityCode" to p.sellerLocalityCode.take(60),
                    "sellerPhone" to p.sellerPhone?.take(40),
                    "sellerWhatsapp" to p.sellerWhatsapp?.take(40),
                    "nameEn" to p.nameEn.take(200),
                    "nameBn" to p.nameBn.take(200),
                    "nameHi" to p.nameHi.take(200),
                    "categoryCode" to p.categoryCode,
                    "subcategory" to p.subcategory.take(120),
                    "brand" to p.brand.take(100),
                    "modelNumber" to p.modelNumber.take(100),
                    "condition" to p.condition,
                    "forSale" to p.forSale,
                    "forRent" to p.forRent,
                    "salePriceInr" to p.salePriceInr,
                    "photosCsv" to p.photosCsv.take(2000),
                    "firebaseStoragePathsCsv" to p.firebaseStoragePathsCsv.take(2000),
                    "listingOrigin" to p.listingOrigin,
                    "approvalStatus" to p.approvalStatus,
                    "verificationBadgeText" to p.verificationBadgeText.take(120),
                    "requiresDgcaCompliance" to p.requiresDgcaCompliance,
                    "requiresEarthmovingSafety" to p.requiresEarthmovingSafety,
                    "brandVerified" to p.brandVerified,
                    "modelVerified" to p.modelVerified,
                    "pendingAdminReview" to p.pendingAdminReview,
                    "adminReviewNote" to p.adminReviewNote.take(600),
                    "hireServiceTypeCode" to p.hireServiceTypeCode.take(64),
                    "aliasesCsv" to p.aliasesCsv.take(600),
                    "applicableCategoriesCsv" to p.applicableCategoriesCsv.take(300),
                    "subsidySchemeCodesCsv" to p.subsidySchemeCodesCsv.take(120),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.w("MachineryRepo", "Product push deferred: ${e.message}")
        }
    }

    private suspend fun pushDealerToFirestore(d: MachineryDealerEntry) {
        try {
            val db = firestore ?: return
            val user = Firebase.auth.currentUser ?: return
            if (user.uid.isBlank()) return
            db.collection("machinery_dealers").document(d.dealerId).set(
                hashMapOf(
                    "dealerId" to d.dealerId,
                    "businessName" to d.businessName.take(200),
                    "localityCode" to d.localityCode,
                    "district" to d.district.take(100),
                    "state" to d.state.take(80),
                    "fullAddress" to d.fullAddress?.take(400),
                    "pinCode" to d.pinCode?.take(20),
                    "ownerName" to d.ownerName?.take(120),
                    "phone" to d.phone?.take(40),
                    "whatsapp" to d.whatsapp?.take(40),
                    "verificationStatus" to d.verificationStatus,
                    "ownerConsentStatus" to d.ownerConsentStatus,
                    "isVerified" to d.isVerified,
                    "isAuthorisedDealerVerified" to d.isAuthorisedDealerVerified,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.w("MachineryRepo", "Dealer push deferred: ${e.message}")
        }
    }

    private suspend fun pushVerificationClaimToFirestore(dealerId: String, userId: String, claimNote: String) {
        try {
            val db = firestore ?: return
            val user = Firebase.auth.currentUser ?: return
            if (user.uid.isBlank()) return
            db.collection("machinery_verification").document("claim_${dealerId}_${userId}").set(
                hashMapOf(
                    "dealerId" to dealerId,
                    "userId" to userId,
                    "claimNote" to claimNote.take(800),
                    "status" to NurseryVerificationStatus.PENDING_VERIFICATION,
                    "ownerConsentConfirmed" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (_: Exception) {}
    }

    private suspend fun logAdminAudit(log: MachineryAdminAuditLog) {
        try {
            val db = firestore ?: return
            val user = Firebase.auth.currentUser ?: return
            if (user.uid.isBlank()) return
            db.collection("machinery_admin_logs").document(log.id).set(
                hashMapOf(
                    "adminEmail" to log.adminEmail.take(150),
                    "action" to log.action.take(80),
                    "targetId" to log.targetId.take(120),
                    "summary" to log.summary.take(500),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (_: Exception) {}
    }

    private fun todayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
