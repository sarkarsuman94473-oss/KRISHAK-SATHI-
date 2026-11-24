package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AiFeedbackEntity
import com.example.data.local.entity.AiKnowledgeDocEntity
import com.example.data.local.entity.AiTelemetryLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiIntelligenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertKnowledgeDoc(doc: AiKnowledgeDocEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertKnowledgeDocs(docs: List<AiKnowledgeDocEntity>)

    @Query("SELECT * FROM ai_knowledge_docs WHERE isActive = 1 ORDER BY updatedAtMillis DESC")
    suspend fun getActiveKnowledgeDocsSync(): List<AiKnowledgeDocEntity>

    @Query("SELECT * FROM ai_knowledge_docs ORDER BY updatedAtMillis DESC")
    fun getAllKnowledgeDocs(): Flow<List<AiKnowledgeDocEntity>>

    @Query("DELETE FROM ai_knowledge_docs WHERE id = :id")
    suspend fun deleteKnowledgeDoc(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelemetry(log: AiTelemetryLogEntity)

    @Query("SELECT * FROM ai_telemetry_logs ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentTelemetry(limit: Int = 80): Flow<List<AiTelemetryLogEntity>>

    @Query("SELECT * FROM ai_telemetry_logs ORDER BY timestampMillis DESC LIMIT :limit")
    suspend fun getRecentTelemetrySync(limit: Int = 80): List<AiTelemetryLogEntity>

    @Query("SELECT COUNT(*) FROM ai_telemetry_logs WHERE userId = :userId AND timestampMillis >= :sinceMillis AND success = 1")
    suspend fun countUserTelemetrySince(userId: String, sinceMillis: Long): Int

    @Query("DELETE FROM ai_telemetry_logs")
    suspend fun clearTelemetry()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(fb: AiFeedbackEntity)

    @Query("SELECT * FROM ai_feedback ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentFeedback(limit: Int = 80): Flow<List<AiFeedbackEntity>>

    @Query("UPDATE ai_feedback SET reviewStatus = :status WHERE id = :id")
    suspend fun updateFeedbackStatus(id: String, status: String)
}
