package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PlantKingdomEntity
import com.example.data.local.entity.WbNurseryDirectoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantKingdomDao {

    @Query("SELECT * FROM plant_kingdom ORDER BY nameEn ASC")
    fun observeAllPlants(): Flow<List<PlantKingdomEntity>>

    @Query("SELECT * FROM plant_kingdom ORDER BY nameEn ASC")
    suspend fun getAllPlantsOnce(): List<PlantKingdomEntity>

    @Query("SELECT * FROM plant_kingdom WHERE plantId = :plantId LIMIT 1")
    suspend fun getPlantById(plantId: String): PlantKingdomEntity?

    @Query("SELECT COUNT(*) FROM plant_kingdom")
    suspend fun getPlantCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlant(entity: PlantKingdomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlants(entities: List<PlantKingdomEntity>)

    @Query("DELETE FROM plant_kingdom WHERE plantId = :plantId")
    suspend fun deletePlantById(plantId: String)

    @Query("DELETE FROM plant_kingdom WHERE plantId IN (:ids)")
    suspend fun deletePlantsByIds(ids: List<String>)

    // West Bengal & Regional Nursery Directory

    @Query("SELECT * FROM wb_nursery_directory ORDER BY isVerified DESC, businessName ASC")
    fun observeAllNurseries(): Flow<List<WbNurseryDirectoryEntity>>

    @Query("SELECT * FROM wb_nursery_directory ORDER BY isVerified DESC, businessName ASC")
    suspend fun getAllNurseriesOnce(): List<WbNurseryDirectoryEntity>

    @Query("SELECT * FROM wb_nursery_directory WHERE nurseryId = :nurseryId LIMIT 1")
    suspend fun getNurseryById(nurseryId: String): WbNurseryDirectoryEntity?

    @Query("SELECT COUNT(*) FROM wb_nursery_directory")
    suspend fun getNurseryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNursery(entity: WbNurseryDirectoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNurseries(entities: List<WbNurseryDirectoryEntity>)

    @Query("DELETE FROM wb_nursery_directory WHERE nurseryId = :nurseryId")
    suspend fun deleteNurseryById(nurseryId: String)
}
