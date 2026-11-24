package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ConsultationAppointmentEntity
import com.example.data.local.entity.ConsultationMessageEntity
import com.example.data.local.entity.ExpertAuditLogEntity
import com.example.data.local.entity.ExpertEntity
import com.example.data.local.entity.ExpertReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpertConsultationDao {

    // --- Experts ---
    @Query("SELECT * FROM agriculture_experts WHERE isVerified = 1 ORDER BY rating DESC, reviewCount DESC")
    fun getVerifiedExperts(): Flow<List<ExpertEntity>>

    @Query("SELECT * FROM agriculture_experts ORDER BY createdAt DESC")
    fun getAllExperts(): Flow<List<ExpertEntity>>

    @Query("SELECT * FROM agriculture_experts WHERE id = :id LIMIT 1")
    suspend fun getExpertById(id: String): ExpertEntity?

    @Query("SELECT * FROM agriculture_experts WHERE isVerified = 1 AND category = :category ORDER BY rating DESC")
    fun getVerifiedExpertsByCategory(category: String): Flow<List<ExpertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperts(experts: List<ExpertEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpert(expert: ExpertEntity)

    @Update
    suspend fun updateExpert(expert: ExpertEntity)

    @Query("UPDATE agriculture_experts SET isVerified = :isVerified WHERE id = :expertId")
    suspend fun updateVerificationStatus(expertId: String, isVerified: Boolean)

    @Query("SELECT COUNT(*) FROM agriculture_experts")
    suspend fun getExpertCount(): Int

    // --- Appointments ---
    @Query("SELECT * FROM consultation_appointments WHERE farmerId = :farmerId ORDER BY createdAt DESC")
    fun getAppointmentsForFarmer(farmerId: String): Flow<List<ConsultationAppointmentEntity>>

    @Query("SELECT * FROM consultation_appointments WHERE expertId = :expertId ORDER BY createdAt DESC")
    fun getAppointmentsForExpert(expertId: String): Flow<List<ConsultationAppointmentEntity>>

    @Query("SELECT * FROM consultation_appointments ORDER BY createdAt DESC")
    fun getAllAppointments(): Flow<List<ConsultationAppointmentEntity>>

    @Query("""
        SELECT * FROM consultation_appointments 
        WHERE expertId = :expertId 
          AND scheduledDate = :date 
          AND scheduledTimeSlot = :timeSlot 
          AND status IN ('CONFIRMED', 'PENDING') 
        LIMIT 1
    """)
    suspend fun checkDoubleBooking(
        expertId: String,
        date: String,
        timeSlot: String
    ): ConsultationAppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAppointment(appointment: ConsultationAppointmentEntity)

    @Query("UPDATE consultation_appointments SET status = :status, meetingNotes = :notes WHERE id = :id")
    suspend fun updateAppointmentStatus(id: String, status: String, notes: String = "")

    // --- Messages (1-to-1 Chat) ---
    @Query("""
        SELECT * FROM consultation_messages 
        WHERE (expertId = :expertId AND farmerId = :farmerId)
        ORDER BY timestamp ASC
    """)
    fun getMessages(expertId: String, farmerId: String): Flow<List<ConsultationMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConsultationMessageEntity)

    // --- Reviews ---
    @Query("SELECT * FROM expert_reviews WHERE expertId = :expertId ORDER BY createdAt DESC")
    fun getReviewsForExpert(expertId: String): Flow<List<ExpertReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ExpertReviewEntity)

    // --- Admin Audit Logs ---
    @Query("SELECT * FROM expert_audit_logs ORDER BY timestamp DESC")
    fun getAuditLogs(): Flow<List<ExpertAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: ExpertAuditLogEntity)
}
