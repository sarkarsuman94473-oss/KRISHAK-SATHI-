package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pest_diseases",
    foreignKeys = [
        ForeignKey(
            entity = CropEntity::class,
            parentColumns = ["cropId"],
            childColumns = ["cropId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cropId"])]
)
data class PestDiseaseEntity(
    @PrimaryKey
    val id: String,
    val cropId: String,
    val problemType: String, // "PEST", "DISEASE", "DEFICIENCY"
    val nameBn: String,
    val nameEn: String,
    val symptomsBn: String,
    val favorableConditionBn: String = "",
    val damageStageBn: String = "",
    val severityLevel: String = "MEDIUM" // "LOW", "MEDIUM", "HIGH"
)
