package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "treatments",
    foreignKeys = [
        ForeignKey(
            entity = PestDiseaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["problemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["problemId"]),
        Index(value = ["cropId"])
    ]
)
data class TreatmentEntity(
    @PrimaryKey
    val id: String,
    val problemId: String,
    val cropId: String,
    val treatmentCategory: String, // "CHEMICAL", "ORGANIC", "PREVENTIVE"
    val commercialNameBn: String,
    val activeIngredient: String,
    val dosePerLitre: String,
    val dosePerBigha: String,
    val phiDays: Int,
    val instructionsBn: String,
    val isCibrcApproved: Boolean = true
)
