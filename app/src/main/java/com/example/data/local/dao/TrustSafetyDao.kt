package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FraudReportEntity
import com.example.data.local.entity.GrievanceTicketEntity
import com.example.data.local.entity.InquiryActivityLogEntity
import com.example.data.local.entity.SellerSuspensionEntity
import com.example.data.local.entity.TrustAuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrustSafetyDao {
    // Fraud Reports
    @Query("SELECT * FROM fraud_reports ORDER BY createdAtMillis DESC")
    fun observeAllFraudReports(): Flow<List<FraudReportEntity>>

    @Query("SELECT * FROM fraud_reports WHERE reporterUserId = :userId ORDER BY createdAtMillis DESC")
    fun observeFraudReportsForUser(userId: String): Flow<List<FraudReportEntity>>

    @Query("SELECT * FROM fraud_reports WHERE id = :id LIMIT 1")
    suspend fun getFraudReportById(id: String): FraudReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFraudReport(report: FraudReportEntity)

    // Grievance Tickets
    @Query("SELECT * FROM grievance_tickets ORDER BY createdAtMillis DESC")
    fun observeAllGrievances(): Flow<List<GrievanceTicketEntity>>

    @Query("SELECT * FROM grievance_tickets WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeGrievancesForUser(userId: String): Flow<List<GrievanceTicketEntity>>

    @Query("SELECT * FROM grievance_tickets WHERE id = :id LIMIT 1")
    suspend fun getGrievanceById(id: String): GrievanceTicketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGrievance(ticket: GrievanceTicketEntity)

    // Seller Suspensions
    @Query("SELECT sellerUserId FROM seller_suspensions WHERE isSuspended = 1")
    fun observeSuspendedSellerIds(): Flow<List<String>>

    @Query("SELECT * FROM seller_suspensions ORDER BY updatedAtMillis DESC")
    fun observeAllSuspensions(): Flow<List<SellerSuspensionEntity>>

    @Query("SELECT * FROM seller_suspensions WHERE sellerUserId = :sellerUserId LIMIT 1")
    suspend fun getSuspensionBySellerId(sellerUserId: String): SellerSuspensionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSuspension(suspension: SellerSuspensionEntity)

    // Immutable Audit Logs
    @Query("SELECT * FROM trust_audit_logs ORDER BY createdAtMillis DESC")
    fun observeAllAuditLogs(): Flow<List<TrustAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAuditLog(log: TrustAuditLogEntity)

    // Activity Timelines
    @Query("SELECT * FROM inquiry_activity_logs WHERE entityId = :entityId ORDER BY timestampMillis ASC")
    fun observeTimelineForEntity(entityId: String): Flow<List<InquiryActivityLogEntity>>

    @Query("SELECT * FROM inquiry_activity_logs ORDER BY timestampMillis ASC")
    fun observeAllTimelines(): Flow<List<InquiryActivityLogEntity>>

    @Query("SELECT * FROM inquiry_activity_logs WHERE entityId = :entityId ORDER BY timestampMillis ASC")
    suspend fun getTimelineForEntityOnce(entityId: String): List<InquiryActivityLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(entry: InquiryActivityLogEntity)
}
