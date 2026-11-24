package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

enum class PlantCategory(val code: String, val bnName: String, val hiName: String, val enName: String, val emoji: String) {
    FRUITS("fruits", "ফল গাছ", "फल के पौधे", "Fruit Plants", "🥭"),
    VEGETABLE_SAPLINGS("veg_saplings", "শাকসবজি চারা", "सब्जी के पौधे", "Vegetable Saplings", "🥬"),
    FLOWERS_ORNAMENTAL("flowers", "ফুল ও বাগান", "फूल व उद्यान", "Flowers & Garden", "🌸"),
    MEDICINAL("medicinal", "ঔষধি গাছ", "औषधीय पौधे", "Medicinal Plants", "🌿"),
    FORESTRY_TIMBER("forestry", "বনজ ও কাঠের গাছ", "वानिकी व इमारती लकड़ी", "Forestry & Timber", "🌲"),
    TEA_CLONES("tea_clones", "চা চারা ও ক্লোন", "चाय पौधे व क्लोन", "Tea Clones", "🍵"),
    SEEDS("seeds", "বীজ বাজার", "बीज बाजार", "Seeds Market", "🌾");

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }
}

enum class SeedCategory(val code: String, val bnName: String, val hiName: String, val enName: String, val emoji: String) {
    VEGETABLES("vegetables", "সবজি বীজ", "सब्जी बीज", "Vegetable Seeds", "🍅"),
    HYBRID("hybrid", "হাইব্রিড বীজ", "हाइब्रिड बीज", "Hybrid Seeds", "⚡"),
    DESI("desi", "দেশি বীজ", "देसी बीज", "Indigenous / Desi", "🌱"),
    CEREALS("cereals", "দানা শস্য বীজ", "अनाज बीज", "Cereal & Grain", "🌾"),
    OILSEEDS("oilseeds", "তৈলবীজ", "तिलहन बीज", "Oilseeds", "🌻"),
    PULSES("pulses", "ডাল বীজ", "दलहन बीज", "Pulses & Legumes", "🫘"),
    FODDER("fodder", "ঘাস ও পশু খাদ্য", "चारा व घास बीज", "Fodder & Grass", "🌿");

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }
}

enum class ItemStockStatus(val bnName: String, val hiName: String, val enName: String, val colorHex: Long) {
    IN_STOCK("মজুত আছে", "स्टॉक उपलब्ध", "In Stock", 0xFF2E7D32),
    LIMITED("সীমিত মজুত", "सीमित स्टॉक", "Limited Stock", 0xFFF57F17),
    OUT_OF_STOCK("স্টক শেষ", "स्टॉक समाप्त", "Out of Stock", 0xFFC62828);

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }
}

data class NurseryCatalogueItem(
    val id: String = "",
    val nameBn: String = "",
    val nameEn: String = "",
    val category: PlantCategory = PlantCategory.FRUITS,
    val varietyBn: String = "",
    val priceRetail: Double = 0.0,
    val priceWholesale: Double = 0.0,
    val minWholesaleQty: Int = 50,
    val unitBn: String = "চারা",
    val stockStatus: ItemStockStatus = ItemStockStatus.IN_STOCK,
    val iconEmoji: String = "🌱"
) {
    fun getName(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getCatalogueName(this, lang)
    fun getVariety(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getCatalogueVariety(this, lang)
    fun getUnit(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getCatalogueUnit(this, lang)
}

data class SeedItem(
    val id: String = "",
    val nurseryId: String = "",
    val nurseryName: String = "",
    val cropNameBn: String = "",
    val cropNameEn: String = "",
    val varietyBn: String = "",
    val brandName: String = "",
    val category: SeedCategory = SeedCategory.VEGETABLES,
    val packetSize: String = "10g",
    val retailPrice: Double = 0.0,
    val wholesalePrice: Double = 0.0,
    val minWholesalePackets: Int = 20,
    val stockStatus: ItemStockStatus = ItemStockStatus.IN_STOCK,
    val germinationRatePercent: Int = 85,
    val isCertified: Boolean = true,
    val isVerified: Boolean = true,
    val nurseryPhone: String = "",
    val nurseryWhatsapp: String = ""
) {
    fun getCropName(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getSeedCropName(this, lang)
    fun getVariety(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getSeedVariety(this, lang)
    fun getBrand(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getSeedBrand(this, lang)
    fun getPacketSize(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getSeedPacketSize(this, lang)
}

data class NurseryBusiness(
    val id: String = "",
    val nameBn: String = "",
    val nameEn: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val addressBn: String = "",
    val city: String = "Siliguri",
    val district: String = "Darjeeling",
    val pinCode: String = "734001",
    val isVerified: Boolean = true,
    val verificationStatus: String = "VERIFIED", // "VERIFIED", "PENDING_VERIFICATION"
    val isWholesale: Boolean = true,
    val isRetail: Boolean = true,
    val categories: List<PlantCategory> = emptyList(),
    val rating: Double = 4.8,
    val reviewCount: Int = 42,
    val minimumWholesaleValue: Double = 1500.0,
    val deliveryAvailable: Boolean = true,
    val badgeText: String = "যাচাইকৃত নার্সারি",
    val catalogue: List<NurseryCatalogueItem> = emptyList()
) {
    fun getDisplayName(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getNurseryName(this, lang)
    fun getOwner(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getOwnerName(this, lang)
    fun getAddress(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getAddress(this, lang)
    fun getBadge(lang: AppLanguage): String = com.example.data.datasource.NurseryTranslations.getBadge(this, lang)
}

@IgnoreExtraProperties
data class WholesaleQuoteRequest(
    val id: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val buyerPhone: String = "",
    val deliveryLocation: String = "",
    val itemName: String = "",
    val itemType: String = "PLANT", // "PLANT" or "SEED"
    val quantity: Int = 100,
    val targetNurseryId: String = "",
    val targetNurseryName: String = "",
    val notes: String = "",
    val status: String = "SUBMITTED", // "SUBMITTED", "OFFER_RECEIVED", "ACCEPTED"
    val createdAtMillis: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class FraudSeedReport(
    val id: String = "",
    val reporterName: String = "",
    val reporterPhone: String = "",
    val sellerOrNurseryName: String = "",
    val location: String = "",
    val seedBrand: String = "",
    val complaintDetails: String = "",
    val reportedAtMillis: Long = System.currentTimeMillis()
)
