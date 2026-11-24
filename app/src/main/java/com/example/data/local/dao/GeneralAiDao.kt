package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GeneralAiMessageEntity
import com.example.data.local.entity.GeneralAiSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneralAiDao {
    @Query("SELECT * FROM general_ai_sessions WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getSessionsForUser(userId: String): Flow<List<GeneralAiSessionEntity>>

    @Query("SELECT * FROM general_ai_sessions WHERE userId = :userId AND assistantType = :assistantType ORDER BY updatedAt DESC")
    fun getSessionsForUserByType(userId: String, assistantType: String): Flow<List<GeneralAiSessionEntity>>

    @Query("SELECT * FROM general_ai_sessions WHERE userId = :userId AND assistantType = :assistantType ORDER BY updatedAt DESC")
    fun getSessionsByType(userId: String, assistantType: String): Flow<List<GeneralAiSessionEntity>>

    @Query("SELECT * FROM general_ai_sessions WHERE userId = :userId AND assistantType = :assistantType AND cropId = :cropId ORDER BY updatedAt DESC")
    fun getSessionsByTypeAndCrop(userId: String, assistantType: String, cropId: String): Flow<List<GeneralAiSessionEntity>>

    @Query("SELECT * FROM general_ai_sessions WHERE userId = :userId AND assistantType = :assistantType AND cropId = :cropId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestSessionForCrop(userId: String, assistantType: String, cropId: String): GeneralAiSessionEntity?

    @Query("SELECT * FROM general_ai_sessions WHERE id = :sessionId AND userId = :userId LIMIT 1")
    suspend fun getSessionById(sessionId: String, userId: String): GeneralAiSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GeneralAiSessionEntity)

    @Update
    suspend fun updateSession(session: GeneralAiSessionEntity)

    @Query("UPDATE general_ai_sessions SET title = :title, updatedAt = :updatedAt WHERE id = :sessionId AND userId = :userId")
    suspend fun renameSession(sessionId: String, userId: String, title: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE general_ai_sessions SET cropId = :cropId, providerUsed = :providerUsed, modelUsed = :modelUsed, updatedAt = :updatedAt WHERE id = :sessionId AND userId = :userId")
    suspend fun updateSessionMetadata(
        sessionId: String,
        userId: String,
        cropId: String,
        providerUsed: String,
        modelUsed: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM general_ai_sessions WHERE id = :sessionId AND userId = :userId")
    suspend fun deleteSession(sessionId: String, userId: String)

    @Query("SELECT * FROM general_ai_messages WHERE sessionId = :sessionId AND userId = :userId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String, userId: String): Flow<List<GeneralAiMessageEntity>>

    @Query("SELECT * FROM general_ai_messages WHERE sessionId = :sessionId AND userId = :userId ORDER BY timestamp ASC")
    suspend fun getMessagesList(sessionId: String, userId: String): List<GeneralAiMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: GeneralAiMessageEntity)

    @Query("UPDATE general_ai_messages SET feedbackVote = :vote, feedbackNote = :note WHERE id = :messageId AND userId = :userId")
    suspend fun updateMessageFeedback(messageId: String, userId: String, vote: Int, note: String = "")

    @Query("DELETE FROM general_ai_messages WHERE sessionId = :sessionId AND userId = :userId")
    suspend fun deleteMessagesForSession(sessionId: String, userId: String)

    @Query("DELETE FROM general_ai_messages WHERE id = :messageId AND userId = :userId")
    suspend fun deleteMessage(messageId: String, userId: String)
}
