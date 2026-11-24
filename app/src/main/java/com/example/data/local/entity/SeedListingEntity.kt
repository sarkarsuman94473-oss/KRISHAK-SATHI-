package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "seed_listings",
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
        Index(value = ["seedCategory"]),
        Index(value = ["stockStatus"])
    ]
)
data class SeedListingEntity(
    @PrimaryKey
    val id: String,
    val nurseryId: String,
    val nurseryName: String,
    val cropNameBn: String,
    val cropNameEn: String,
    val varietyBn: String,
    val companyOrBrand: String,
    val seedCategory: String, // "VEGETABLES", "HYBRID", "DESI", "CEREALS", "OILSEEDS", "PULSES", "FODDER"
    val packetSize: String,
    val retailPrice: Double,
    val wholesalePrice: Double,
    val minWholesalePackets: Int,
    val stockStatus: String = "IN_STOCK", // "IN_STOCK", "LIMITED", "OUT_OF_STOCK"
    val stockPacketsAvailable: Int = 100,
    val germinationRatePercent: Int = 85,
    val purityPercent: Int = 98,
    val lotOrBatchNumber: String = "",
    val isCertified: Boolean = true,
    val isVerified: Boolean = true,
    val sellerPhone: String = "",
    val sellerWhatsapp: String = "",
    val sellerCity: String = "Siliguri"
)
