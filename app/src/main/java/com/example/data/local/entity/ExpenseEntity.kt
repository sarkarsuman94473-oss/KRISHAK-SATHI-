package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farmer_expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val firestoreId: String = "",
    val cropName: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = ""
)
