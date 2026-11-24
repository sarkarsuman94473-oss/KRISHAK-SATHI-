package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FarmerProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val mobileNumber: String = "",
    val village: String = "",
    val district: String = "",
    val state: String = "West Bengal",
    val pinCode: String = "",
    val farmSize: Double = 0.0,
    val farmSizeUnit: String = "bigha",
    val mainCrops: String = "",
    val preferredLanguage: String = "bn",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
