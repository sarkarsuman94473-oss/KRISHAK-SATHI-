package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nurseries")
data class NurseryEntity(
    @PrimaryKey
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val ownerName: String,
    val phone: String,
    val whatsapp: String,
    val addressBn: String,
    val city: String,
    val district: String,
    val pinCode: String,
    val latitude: Double = 26.7271,
    val longitude: Double = 88.4239,
    val sellerType: String = "NURSERY", // "NURSERY", "SEED_STORE", "HYBRID_HUB", "COMMERCIAL_GROWER"
    val licenseNumber: String = "",
    val yearsInBusiness: Int = 5,
    val isVerified: Boolean = true,
    val verificationStatus: String = "VERIFIED", // "VERIFIED", "PENDING_VERIFICATION", "COMMUNITY_SUBMITTED"
    val isWholesale: Boolean = true,
    val isRetail: Boolean = true,
    val categoriesCsv: String = "FRUITS,VEGETABLE_SAPLINGS,FLOWERS_ORNAMENTAL,MEDICINAL,FORESTRY_TIMBER,TEA_CLONES,SEEDS",
    val rating: Double = 4.8,
    val reviewCount: Int = 0,
    val deliveryAvailable: Boolean = false,
    val deliveryRadiusKm: Int = 30,
    val minimumWholesaleOrderValue: Double = 0.0,
    val badgeText: String = "যাচাইকৃত নার্সারি"
)
