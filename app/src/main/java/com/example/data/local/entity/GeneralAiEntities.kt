package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "general_ai_sessions",
    indices = [Index(value = ["userId", "updatedAt"])]
)
data class GeneralAiSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "'GENERAL_AI'")
    val assistantType: String = "GENERAL_AI",
    @ColumnInfo(defaultValue = "''")
    val cropId: String = "",
    @ColumnInfo(defaultValue = "'GEMINI'")
    val providerUsed: String = "GEMINI",
    @ColumnInfo(defaultValue = "'gemini-3.8-flash'")
    val modelUsed: String = "gemini-3.8-flash"
)

@Entity(
    tableName = "general_ai_messages",
    indices = [
        Index(value = ["sessionId", "timestamp"]),
        Index(value = ["userId"])
    ]
)
data class GeneralAiMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val userId: String,
    val sender: String, // "user" or "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val hasSearchFallback: Boolean = false,
    val searchQuery: String = "",
    val sourcesJson: String = "",
    val imageBase64OrUri: String? = null,
    @ColumnInfo(defaultValue = "'GEMINI'")
    val providerUsed: String = "GEMINI",
    @ColumnInfo(defaultValue = "'gemini-3.8-flash'")
    val modelUsed: String = "gemini-3.8-flash",
    @ColumnInfo(defaultValue = "0")
    val fallbackUsed: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val isError: Boolean = false,
    @ColumnInfo(defaultValue = "''")
    val cropId: String = "",
    @ColumnInfo(defaultValue = "0")
    val feedbackVote: Int = 0, // 0 = none, 1 = helpful, -1 = not helpful
    @ColumnInfo(defaultValue = "''")
    val feedbackNote: String = ""
)
