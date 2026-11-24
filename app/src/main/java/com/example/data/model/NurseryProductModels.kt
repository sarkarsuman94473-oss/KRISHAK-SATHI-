package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

enum class NurseryProductCategory(
    val code: String,
    val translationKey: String,
    val bnName: String,
    val hiName: String,
    val enName: String,
    val emoji: String
) {
    FRUIT_PLANTS("fruit_plants", "cat_fruit_plants", "ফল গাছ", "फल के पौधे", "Fruit Plants", "🥭"),
    FLOWER_PLANTS("flower_plants", "cat_flower_plants", "ফুল ও বাগান", "फूलों के पौधे", "Flower Plants", "🌸"),
    INDOOR_PLANTS("indoor_plants", "cat_indoor_plants", "ইনডোর গাছ", "इंडोर पौधे", "Indoor Plants", "🪴"),
    OUTDOOR_PLANTS("outdoor_plants", "cat_outdoor_plants", "আউটডোর গাছ", "आउटडोर पौधे", "Outdoor Plants", "🌳"),
    VEGETABLE_PLANTS("vegetable_plants", "cat_vegetable_plants", "সবজি চারা", "सब्जी के पौधे", "Vegetable Plants", "🥬"),
    MEDICINAL_PLANTS("medicinal_plants", "cat_medicinal_plants", "ঔষধি গাছ", "औषधीय पौधे", "Medicinal Plants", "🌿"),
    SEEDS("seeds", "cat_seeds", "উন্নত বীজ", "उन्नत बीज", "Seeds", "🌾"),
    FERTILIZERS("fertilizers", "cat_fertilizers", "জৈব ও রাসায়নিক সার", "उर्वरक व खाद", "Fertilizers", "🧪"),
    GARDENING_TOOLS("gardening_tools", "cat_gardening_tools", "বাগান যন্ত্রপাতি", "बागवानी उपकरण", "Gardening Tools", "✂️"),
    POTS_ACCESSORIES("pots_accessories", "cat_pots_accessories", "টব ও আনুষাঙ্গিক", "गमले और सहायक सामग्री", "Pots & Accessories", "🏺");

    fun getDisplayName(lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.BENGALI -> bnName
            AppLanguage.HINDI -> hiName
            AppLanguage.ENGLISH -> enName
        }
    }

    companion object {
        fun fromCode(code: String?): NurseryProductCategory {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: FRUIT_PLANTS
        }
    }
}

@IgnoreExtraProperties
data class NurseryProduct(
    val id: String = "",
    val nurseryId: String = "",
    val nurseryName: String = "",
    val sellerUserId: String = "",
    val productName: String = "",
    val category: String = NurseryProductCategory.FRUIT_PLANTS.code,
    val images: List<String> = emptyList(), // Multiple image URLs or local resource drawables
    val description: String = "",
    val regularPrice: Double = 0.0,
    val discountPrice: Double? = null,
    val availableStock: Int = 10,
    val reservedStock: Int = 0,
    val plantSize: String = "১-২ ফুট (Medium)",
    val plantType: String = "কলম চারা (Grafted)",
    val plantCareInstructions: String = "",
    val deliveryAvailable: Boolean = true,
    val deliveryRadiusKm: Int = 30,
    val productLocation: String = "Siliguri / Jalpaiguri",
    val isApproved: Boolean = true, // Approved status for marketplace listing
    val status: String = "ACTIVE", // "ACTIVE", "OUT_OF_STOCK", "DRAFT", "ARCHIVED"
    val linkedPlantId: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val effectivePrice: Double
        get() = if (discountPrice != null && discountPrice > 0.0 && discountPrice < regularPrice) discountPrice else regularPrice

    val isActuallyInStock: Boolean
        get() = status == "ACTIVE" && (availableStock - reservedStock) > 0

    val freeStock: Int
        get() = (availableStock - reservedStock).coerceAtLeast(0)

    fun getDisplayName(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getProductName(this, lang)
    fun getDisplayNurseryName(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getProductNurseryName(this, lang)
    fun getDisplayDescription(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getProductDescription(this, lang)
    fun getDisplaySize(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getProductSize(this, lang)
    fun getDisplayType(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getProductType(this, lang)
    fun getDisplayCare(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getProductCare(this, lang)
    fun getDisplayLocation(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getProductLocation(this, lang)
}

@IgnoreExtraProperties
data class NurseryOrderItem(
    val productId: String = "",
    val productName: String = "",
    val category: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0
)

@IgnoreExtraProperties
data class NurseryOrder(
    val id: String = "",
    val orderNumber: String = "ORD-${System.currentTimeMillis() % 100000}",
    val nurseryId: String = "",
    val nurseryName: String = "",
    val sellerUserId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val deliveryAddress: String = "",
    val items: List<NurseryOrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "NEW", // "NEW", "ACCEPTED", "DISPATCHED", "COMPLETED", "CANCELLED"
    val paymentMethod: String = "CASH_ON_DELIVERY",
    val orderDateMillis: Long = System.currentTimeMillis(),
    val updatedDateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)

data class SellerDashboardMetrics(
    val totalProducts: Int = 0,
    val activeProducts: Int = 0,
    val outOfStockProducts: Int = 0,
    val newOrdersCount: Int = 0,
    val acceptedOrdersCount: Int = 0,
    val completedOrdersCount: Int = 0,
    val totalSalesAmount: Double = 0.0
)
