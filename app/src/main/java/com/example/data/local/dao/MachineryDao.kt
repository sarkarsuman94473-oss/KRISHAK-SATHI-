package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
import kotlinx.coroutines.flow.Flow

@Dao
interface MachineryDao {

    @Query("SELECT * FROM machinery_products ORDER BY listingOrigin ASC, updatedAtMillis DESC")
    fun observeAllProducts(): Flow<List<MachineryProductEntity>>

    @Query("SELECT * FROM machinery_products")
    suspend fun getAllProductsOnce(): List<MachineryProductEntity>

    @Query("SELECT * FROM machinery_products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): MachineryProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProduct(entity: MachineryProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProducts(entities: List<MachineryProductEntity>)

    @Query("DELETE FROM machinery_products WHERE id = :id")
    suspend fun deleteProductById(id: String)

    // Dealers
    @Query("SELECT * FROM machinery_dealers ORDER BY isVerified DESC, businessName ASC")
    fun observeAllDealers(): Flow<List<MachineryDealerEntity>>

    @Query("SELECT * FROM machinery_dealers")
    suspend fun getAllDealersOnce(): List<MachineryDealerEntity>

    @Query("SELECT * FROM machinery_dealers WHERE dealerId = :dealerId LIMIT 1")
    suspend fun getDealerById(dealerId: String): MachineryDealerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDealer(entity: MachineryDealerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDealers(entities: List<MachineryDealerEntity>)

    // Manufacturers
    @Query("SELECT * FROM machinery_manufacturers ORDER BY nameEn ASC")
    fun observeAllManufacturers(): Flow<List<MachineryManufacturerEntity>>

    @Query("SELECT * FROM machinery_manufacturers")
    suspend fun getAllManufacturersOnce(): List<MachineryManufacturerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertManufacturers(entities: List<MachineryManufacturerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertManufacturer(entity: MachineryManufacturerEntity)

    // Many-to-many Dealer Offers
    @Query("SELECT * FROM machinery_dealer_offers WHERE machineId = :machineId ORDER BY isOutdatedPrice ASC, updatedAtMillis DESC")
    fun observeOffersForMachine(machineId: String): Flow<List<MachineryDealerOfferEntity>>

    @Query("SELECT * FROM machinery_dealer_offers ORDER BY updatedAtMillis DESC")
    fun observeAllOffers(): Flow<List<MachineryDealerOfferEntity>>

    @Query("SELECT * FROM machinery_dealer_offers")
    suspend fun getAllOffersOnce(): List<MachineryDealerOfferEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOffer(entity: MachineryDealerOfferEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOffers(entities: List<MachineryDealerOfferEntity>)

    @Query("DELETE FROM machinery_dealer_offers WHERE offerId = :offerId")
    suspend fun deleteOfferById(offerId: String)

    // Reviews
    @Query("SELECT * FROM machinery_reviews WHERE machineId = :machineId ORDER BY createdAtMillis DESC")
    fun observeReviewsForMachine(machineId: String): Flow<List<MachineryReviewEntity>>

    @Query("SELECT * FROM machinery_reviews ORDER BY createdAtMillis DESC")
    fun observeAllReviews(): Flow<List<MachineryReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReview(entity: MachineryReviewEntity)

    @Query("DELETE FROM machinery_reviews WHERE id = :id")
    suspend fun deleteReviewById(id: String)

    // Research Queue
    @Query("SELECT * FROM machinery_research_queue ORDER BY updatedAtMillis DESC")
    fun observeResearchQueue(): Flow<List<MachineryResearchQueueEntity>>

    @Query("SELECT * FROM machinery_research_queue")
    suspend fun getAllResearchQueueOnce(): List<MachineryResearchQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertResearchItem(entity: MachineryResearchQueueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertResearchItems(entities: List<MachineryResearchQueueEntity>)

    // Favourites
    @Query("SELECT machineId FROM machinery_favourites WHERE userId = :userId")
    fun observeFavouriteIds(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavourite(entity: MachineryFavouriteEntity)

    @Query("DELETE FROM machinery_favourites WHERE compositeId = :compositeId")
    suspend fun removeFavourite(compositeId: String)

    // Inquiries / Quotes
    @Query("SELECT * FROM machinery_inquiries WHERE userId = :userId OR sellerUserId = :userId ORDER BY createdAtMillis DESC")
    fun observeInquiriesForUser(userId: String): Flow<List<MachineryInquiryEntity>>

    @Query("SELECT * FROM machinery_inquiries ORDER BY createdAtMillis DESC")
    fun observeAllInquiries(): Flow<List<MachineryInquiryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInquiry(entity: MachineryInquiryEntity)

    // Rentals
    @Query("SELECT * FROM machinery_rentals WHERE userId = :userId OR sellerUserId = :userId ORDER BY createdAtMillis DESC")
    fun observeRentalsForUser(userId: String): Flow<List<MachineryRentalEntity>>

    @Query("SELECT * FROM machinery_rentals ORDER BY createdAtMillis DESC")
    fun observeAllRentals(): Flow<List<MachineryRentalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRental(entity: MachineryRentalEntity)

    // Services
    @Query("SELECT * FROM machinery_service_requests WHERE userId = :userId OR providerUserId = :userId ORDER BY createdAtMillis DESC")
    fun observeServicesForUser(userId: String): Flow<List<MachineryServiceRequestEntity>>

    @Query("SELECT * FROM machinery_service_requests ORDER BY createdAtMillis DESC")
    fun observeAllServices(): Flow<List<MachineryServiceRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertServiceRequest(entity: MachineryServiceRequestEntity)
}
