package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plant_catalog",
    foreignKeys = [
        ForeignKey(
            entity = NurseryEntity::class,
            parentColumns = ["id"],
            childColumns = ["nurseryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["nurseryId"]),
        Index(value = ["category"]),
        Index(value = ["stockStatus"])
    ]
)
data class PlantCatalogEntity(
    @PrimaryKey
    val id: String,
    val nurseryId: String,
    val category: String, // "FRUITS", "VEGETABLE_SAPLINGS", "FLOWERS_ORNAMENTAL", "MEDICINAL", "FORESTRY_TIMBER", "TEA_CLONES"
    val plantNameBn: String,
    val plantNameEn: String,
    val varietyBn: String,
    val scientificName: String = "",
    val saplingType: String = "GRAFTED", // "GRAFTED", "TISSUE_CULTURE", "SEEDLING", "AIR_LAYERED", "STEM_CUTTING"
    val ageMonths: Int = 6,
    val stockStatus: String = "IN_STOCK", // "IN_STOCK", "LIMITED", "OUT_OF_STOCK"
    val availableQuantity: Int = 100,
    val retailPrice: Double,
    val wholesalePrice: Double = 0.0,
    val minWholesaleQty: Int = 50,
    val unit: String = "চারা", // "চারা", "গাছ", "কাটিং", "প্যাকেট"
    val iconEmoji: String = "🌱",
    val sellerName: String = "",
    val sellerPhone: String = "",
    val sellerCity: String = "Siliguri",
    val isFeatured: Boolean = false,
    val badgeText: String = ""
)
