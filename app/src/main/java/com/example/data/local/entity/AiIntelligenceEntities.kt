package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_knowledge_docs",
    indices = [
        Index(value = ["category", "isActive"]),
        Index(value = ["updatedAtMillis"])
    ]
)
data class AiKnowledgeDocEntity(
    @PrimaryKey val id: String,
    val titleEn: String,
    val titleBn: String,
    val titleHi: String,
    val institution: String,
    val sourceUrl: String,
    val publicationDate: String,
    val verifiedDate: String,
    val category: String,
    val applicableCropIdsCsv: String,
    val regionTag: String,
    val keywordsCsv: String,
    val summaryEn: String,
    val summaryBn: String,
    val summaryHi: String,
    val isCustomAdminAdded: Boolean = false,
    val isActive: Boolean = true,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ai_telemetry_logs",
    indices = [
        Index(value = ["userId", "timestampMillis"]),
        Index(value = ["providerUsed", "timestampMillis"])
    ]
)
data class AiTelemetryLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val assistantMode: String, // FARMER_AI, PLANT_PROBLEM, GENERAL_AI
    val cropId: String = "",
    val providerUsed: String, // GEMINI, OPENAI
    val modelUsed: String,
    val fallbackUsed: Boolean = false,
    val success: Boolean = true,
    val errorCode: String = "NONE",
    val errorSummary: String = "",
    val latencyMs: Long = 0L,
    val estimatedTokens: Int = 0,
    val estimatedCostUsd: Double = 0.0,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ai_feedback",
    indices = [
        Index(value = ["timestampMillis"]),
        Index(value = ["reviewStatus"])
    ]
)
data class AiFeedbackEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val assistantMode: String,
    val cropId: String = "",
    val providerUsed: String,
    val helpful: Boolean,
    val messagePreview: String,
    val farmerNote: String = "",
    val reviewStatus: String = "OPEN", // OPEN, REVIEWED
    val timestampMillis: Long = System.currentTimeMillis()
)
