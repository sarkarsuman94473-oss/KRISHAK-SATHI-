package com.example.data.model

import com.example.data.datasource.MarketPriceTranslations
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ProduceListing(
    @DocumentId val id: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val farmerPhone: String = "",
    val product: String = "",
    val category: String = "Vegetables",
    val quantity: Double = 0.0,
    val unit: String = "KG",
    val expectedPricePerUnit: Double = 0.0,
    val harvestDate: String = "",
    val location: String = "",
    val deliveryAvailable: Boolean = false,
    val status: String = "ACTIVE", // ACTIVE, SOLD, CANCELLED
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class BuyerOffer(
    @DocumentId val id: String = "",
    val listingId: String = "",
    val farmerId: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val buyerType: String = "Wholesaler (আড়তদার)",
    val buyerPhone: String = "",
    val offeredPricePerUnit: Double = 0.0,
    val counterPricePerUnit: Double? = null,
    val status: String = "PENDING", // PENDING, ACCEPTED, COUNTERED, REJECTED
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Order(
    @DocumentId val id: String = "",
    val listingId: String = "",
    val farmerId: String = "",
    val buyerId: String = "",
    val product: String = "",
    val quantity: Double = 0.0,
    val unit: String = "KG",
    val agreedPricePerUnit: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: String = "CONFIRMED", // CONFIRMED, READY_FOR_PICKUP, PICKED_UP, DELIVERED, COMPLETED
    val paymentMethod: String = "CASH_ON_DELIVERY",
    val pickupPhotoUrl: String = "",
    val weightConfirmedKg: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class MarketPriceItem(
    val productBn: String,
    val productHi: String,
    val productEn: String,
    val marketNameBn: String,
    val districtBn: String,
    val minPrice: Double,
    val maxPrice: Double,
    val modalPrice: Double,
    val unit: String = "₹ / কুইন্টাল",
    val trend: String = "UP", // UP, DOWN, STABLE
    val source: String = "Agmarknet / রাজ্য কৃষি বিপণন",
    val lastUpdated: String = "আজ সকাল ৮:৩০"
) {
    fun getProduct(lang: AppLanguage): String = MarketPriceTranslations.getProduct(this, lang)
    fun getMarketName(lang: AppLanguage): String = MarketPriceTranslations.getMarketName(this, lang)
    fun getDistrict(lang: AppLanguage): String = MarketPriceTranslations.getDistrict(this, lang)
    fun getUnit(lang: AppLanguage): String = MarketPriceTranslations.getUnit(this, lang)
    fun getSource(lang: AppLanguage): String = MarketPriceTranslations.getSource(this, lang)
    fun getLastUpdated(lang: AppLanguage): String = MarketPriceTranslations.getLastUpdated(this, lang)
    fun getTrendLabel(lang: AppLanguage): String = MarketPriceTranslations.getTrendLabel(trend, lang)
}
