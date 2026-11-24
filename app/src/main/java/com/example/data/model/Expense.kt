package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ExpenseCategory(val code: String, val bnName: String, val hiName: String, val enName: String, val icon: String) {
    SEEDS("seeds", "বীজ ও চারা (Seeds)", "बीज व पौधे", "Seeds & Saplings", "🌱"),
    FERTILIZER("fertilizer", "সার ও খৈল (Fertilizer)", "उर्वरक व खाद", "Fertilizer & Manure", "🧪"),
    MEDICINE("medicine", "কীটনাশক ও ওষুধ (Medicine)", "कीटनाशक व दवा", "Crop Medicine", "💊"),
    LABOUR("labour", "শ্রমিক মজুরি (Labour)", "मजदूरी", "Labour Wages", "👨‍🌾"),
    WATER("water", "সেচ খরচ (Water/Irrigation)", "सिंचाई", "Irrigation", "💧"),
    ELECTRICITY("electricity", "বিদ্যুৎ বা ডিজেল (Electricity/Fuel)", "बिजली / डीजल", "Electricity / Fuel", "⚡"),
    TRACTOR("tractor", "ট্রাক্টর বা জমি চাষ (Tractor/Tillage)", "ट्रैक्टर जुताई", "Tractor & Tillage", "🚜"),
    TRANSPORT("transport", "পরিবহন খরচ (Transport)", "परिवहन", "Transport", "🚛"),
    OTHER("other", "অন্যান্য (Other)", "अन्य", "Other", "📝")
}

@IgnoreExtraProperties
data class FarmerExpense(
    @DocumentId val id: String = "",
    val farmerId: String = "",
    val cropId: String = "",
    val cropName: String = "",
    val category: String = "seeds",
    val amount: Double = 0.0,
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
