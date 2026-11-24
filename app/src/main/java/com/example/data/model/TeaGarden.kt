package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class TeaGarden(
    @DocumentId val id: String = "",
    val farmerId: String = "",
    val gardenName: String = "",
    val location: String = "",
    val totalLand: Double = 0.0,
    val landUnit: String = "bigha",
    val teaVariety: String = "TV-1 / Clone",
    val plantAgeYears: Double = 5.0,
    val plantingYear: Double = 2018.0,
    val sectionsCount: Double = 2.0,
    val irrigationAvailable: Boolean = true,
    val shadeTreesInfo: String = "Albizia odoratissima / Derris robusta",
    val lastPruningDate: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
