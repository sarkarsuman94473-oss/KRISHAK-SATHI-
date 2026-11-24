package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FarmerAuditLogEntity
import com.example.data.local.entity.FarmerContactRequestEntity
import com.example.data.local.entity.LeadingFarmerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadingFarmerDao {
    @Query("SELECT * FROM leading_farmers ORDER BY isFeatured DESC, fullNameEn ASC")
    fun getAllFarmers(): Flow<List<LeadingFarmerEntity>>

    @Query("SELECT * FROM leading_farmers WHERE isApproved = 1 ORDER BY isFeatured DESC, fullNameEn ASC")
    fun getApprovedFarmers(): Flow<List<LeadingFarmerEntity>>

    @Query("SELECT * FROM leading_farmers WHERE isApproved = 1 AND isFeatured = 1")
    fun getFeaturedFarmers(): Flow<List<LeadingFarmerEntity>>

    @Query("SELECT * FROM leading_farmers WHERE id = :id")
    fun getFarmerById(id: String): Flow<LeadingFarmerEntity?>

    @Query("SELECT * FROM leading_farmers WHERE id = :id")
    suspend fun getFarmerByIdSync(id: String): LeadingFarmerEntity?

    @Query("SELECT COUNT(*) FROM leading_farmers")
    suspend fun countFarmers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmer(farmer: LeadingFarmerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmers(farmers: List<LeadingFarmerEntity>)

    @Update
    suspend fun updateFarmer(farmer: LeadingFarmerEntity)

    @Delete
    suspend fun deleteFarmer(farmer: LeadingFarmerEntity)

    @Query("UPDATE leading_farmers SET isApproved = :approved WHERE id = :id")
    suspend fun updateApprovalStatus(id: String, approved: Boolean)

    @Query("UPDATE leading_farmers SET isFeatured = :featured WHERE id = :id")
    suspend fun updateFeaturedStatus(id: String, featured: Boolean)

    // Contact Requests
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactRequest(request: FarmerContactRequestEntity)

    @Query("SELECT * FROM farmer_contact_requests ORDER BY timestamp DESC")
    fun getAllContactRequests(): Flow<List<FarmerContactRequestEntity>>

    @Query("UPDATE farmer_contact_requests SET status = :status WHERE id = :id")
    suspend fun updateRequestStatus(id: String, status: String)

    // Audit Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: FarmerAuditLogEntity)

    @Query("SELECT * FROM farmer_audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<FarmerAuditLogEntity>>
}
