package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ExpertInquiry(
    @DocumentId val id: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val crop: String = "",
    val question: String = "",
    val photoUrl: String = "",
    val status: String = "ANSWERED", // PENDING, ANSWERED
    val reply: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
