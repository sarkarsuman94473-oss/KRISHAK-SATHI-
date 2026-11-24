package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crops")
data class CropEntity(
    @PrimaryKey
    val cropId: String,
    val nameBn: String,
    val nameHi: String,
    val nameEn: String,
    val scientificName: String,
    val categoryCode: String,
    val bestSeasonBn: String,
    val soilBn: String,
    val recommendedPhBn: String,
    val seedRateBn: String,
    val spacingBn: String,
    val harvestDays: Int,
    val summaryBn: String,
    val suitableClimateBn: String,
    val irrigationGuidanceBn: String,
    val storageBn: String,
    val iconEmoji: String,
    val isCustomAdded: Boolean = false
)
