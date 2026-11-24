package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.example.data.local.entity.CropEntity
import com.example.data.local.entity.NurseryEntity
import com.example.data.local.entity.PestDiseaseEntity
import com.example.data.local.entity.PlantCatalogEntity
import com.example.data.local.entity.SeedListingEntity
import com.example.data.local.entity.TreatmentEntity
import kotlinx.coroutines.flow.Flow

// ==========================================
// Relational classes for 1-to-Many & Joins
// ==========================================

data class CropWithPestDiseases(
    @Embedded val crop: CropEntity,
    @Relation(
        parentColumn = "cropId",
        entityColumn = "cropId"
    )
    val pestDiseases: List<PestDiseaseEntity>
)

data class ProblemWithTreatments(
    @Embedded val problem: PestDiseaseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "problemId"
    )
    val treatments: List<TreatmentEntity>
)

data class NurseryWithPlantCatalog(
    @Embedded val nursery: NurseryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "nurseryId"
    )
    val plantCatalog: List<PlantCatalogEntity>
)

data class NurseryWithSeedListings(
    @Embedded val nursery: NurseryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "nurseryId"
    )
    val seedListings: List<SeedListingEntity>
)

data class NurseryWithFullInventory(
    @Embedded val nursery: NurseryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "nurseryId"
    )
    val plantCatalog: List<PlantCatalogEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "nurseryId"
    )
    val seedListings: List<SeedListingEntity>
)

data class PlantCatalogWithSeller(
    @Embedded val plant: PlantCatalogEntity,
    @Relation(
        parentColumn = "nurseryId",
        entityColumn = "id"
    )
    val seller: NurseryEntity
)

data class SeedListingWithSeller(
    @Embedded val seed: SeedListingEntity,
    @Relation(
        parentColumn = "nurseryId",
        entityColumn = "id"
    )
    val seller: NurseryEntity
)

// ==========================================
// DAOs
// ==========================================

@Dao
interface CropDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrops(crops: List<CropEntity>)

    @Query("SELECT * FROM crops ORDER BY nameBn ASC")
    fun getAllCrops(): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE categoryCode = :categoryCode ORDER BY nameBn ASC")
    fun getCropsByCategory(categoryCode: String): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE cropId = :cropId LIMIT 1")
    suspend fun getCropById(cropId: String): CropEntity?

    @Transaction
    @Query("SELECT * FROM crops WHERE cropId = :cropId")
    fun getCropWithProblems(cropId: String): Flow<CropWithPestDiseases?>
}

@Dao
interface PestDiseaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPestDiseases(items: List<PestDiseaseEntity>)

    @Query("SELECT * FROM pest_diseases WHERE cropId = :cropId")
    fun getProblemsForCrop(cropId: String): Flow<List<PestDiseaseEntity>>

    @Transaction
    @Query("SELECT * FROM pest_diseases WHERE id = :problemId")
    fun getProblemWithTreatments(problemId: String): Flow<ProblemWithTreatments?>
}

@Dao
interface TreatmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatments(items: List<TreatmentEntity>)

    @Query("SELECT * FROM treatments WHERE problemId = :problemId")
    fun getTreatmentsForProblem(problemId: String): Flow<List<TreatmentEntity>>

    @Query("SELECT * FROM treatments WHERE cropId = :cropId")
    fun getTreatmentsForCrop(cropId: String): Flow<List<TreatmentEntity>>
}

@Dao
interface NurseryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNurseries(items: List<NurseryEntity>)

    @Query("SELECT * FROM nurseries ORDER BY isVerified DESC, rating DESC")
    fun getAllNurseries(): Flow<List<NurseryEntity>>

    @Query("SELECT * FROM nurseries WHERE city LIKE '%' || :city || '%' OR district LIKE '%' || :city || '%'")
    fun getNurseriesByLocation(city: String): Flow<List<NurseryEntity>>

    @Query("SELECT * FROM nurseries WHERE isWholesale = 1")
    fun getWholesaleNurseries(): Flow<List<NurseryEntity>>

    @Query("SELECT * FROM nurseries WHERE sellerType = :sellerType")
    fun getNurseriesBySellerType(sellerType: String): Flow<List<NurseryEntity>>

    @Query("SELECT * FROM nurseries WHERE id = :id LIMIT 1")
    suspend fun getNurseryById(id: String): NurseryEntity?

    @Transaction
    @Query("SELECT * FROM nurseries WHERE id = :nurseryId")
    fun getNurseryWithPlants(nurseryId: String): Flow<NurseryWithPlantCatalog?>

    @Transaction
    @Query("SELECT * FROM nurseries WHERE id = :nurseryId")
    fun getNurseryWithSeeds(nurseryId: String): Flow<NurseryWithSeedListings?>

    @Transaction
    @Query("SELECT * FROM nurseries WHERE id = :nurseryId")
    fun getNurseryWithFullInventory(nurseryId: String): Flow<NurseryWithFullInventory?>
}

@Dao
interface PlantCatalogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlants(items: List<PlantCatalogEntity>)

    @Query("SELECT * FROM plant_catalog ORDER BY isFeatured DESC, plantNameBn ASC")
    fun getAllPlants(): Flow<List<PlantCatalogEntity>>

    @Query("SELECT * FROM plant_catalog WHERE category = :category ORDER BY plantNameBn ASC")
    fun getPlantsByCategory(category: String): Flow<List<PlantCatalogEntity>>

    @Query("SELECT * FROM plant_catalog WHERE stockStatus = :status")
    fun getPlantsByStockStatus(status: String): Flow<List<PlantCatalogEntity>>

    @Query("SELECT * FROM plant_catalog WHERE nurseryId = :nurseryId")
    fun getPlantsForNursery(nurseryId: String): Flow<List<PlantCatalogEntity>>

    @Query("SELECT * FROM plant_catalog WHERE id = :id LIMIT 1")
    suspend fun getPlantById(id: String): PlantCatalogEntity?

    @Query("SELECT * FROM plant_catalog WHERE plantNameBn LIKE '%' || :query || '%' OR plantNameEn LIKE '%' || :query || '%' OR varietyBn LIKE '%' || :query || '%'")
    fun searchPlants(query: String): Flow<List<PlantCatalogEntity>>

    @Transaction
    @Query("SELECT * FROM plant_catalog WHERE id = :id")
    fun getPlantWithSeller(id: String): Flow<PlantCatalogWithSeller?>
}

@Dao
interface SeedListingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeedListings(items: List<SeedListingEntity>)

    @Query("SELECT * FROM seed_listings ORDER BY isVerified DESC, cropNameBn ASC")
    fun getAllSeeds(): Flow<List<SeedListingEntity>>

    @Query("SELECT * FROM seed_listings WHERE seedCategory = :category")
    fun getSeedsByCategory(category: String): Flow<List<SeedListingEntity>>

    @Query("SELECT * FROM seed_listings WHERE stockStatus = :status")
    fun getSeedsByStockStatus(status: String): Flow<List<SeedListingEntity>>

    @Query("SELECT * FROM seed_listings WHERE nurseryId = :nurseryId")
    fun getSeedsForNursery(nurseryId: String): Flow<List<SeedListingEntity>>

    @Query("SELECT * FROM seed_listings WHERE cropNameBn LIKE '%' || :query || '%' OR varietyBn LIKE '%' || :query || '%' OR companyOrBrand LIKE '%' || :query || '%'")
    fun searchSeeds(query: String): Flow<List<SeedListingEntity>>

    @Transaction
    @Query("SELECT * FROM seed_listings WHERE id = :id")
    fun getSeedWithSeller(id: String): Flow<SeedListingWithSeller?>
}
