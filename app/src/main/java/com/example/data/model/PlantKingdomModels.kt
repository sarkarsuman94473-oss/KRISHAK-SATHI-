package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

enum class PlantKingdomGroup(
    val code: String,
    val bnName: String,
    val hiName: String,
    val enName: String,
    val emoji: String
) {
    HIMALAYAN_HILL_FLOWERS(
        code = "HIMALAYAN_HILL_FLOWERS",
        bnName = "হিমালয় ও পাহাড়ি ফুল",
        hiName = "हिमालयी व पहाड़ी फूल",
        enName = "Himalayan & Hill Flowers",
        emoji = "🏔️"
    ),
    COMMON_FLOWERS(
        code = "COMMON_FLOWERS",
        bnName = "সাধারণ ফুল গাছ",
        hiName = "सामान्य फूलों के पौधे",
        enName = "Common Flowering Plants",
        emoji = "🌺"
    ),
    SEASONAL_FLOWERS(
        code = "SEASONAL_FLOWERS",
        bnName = "মৌসুমি ফুল সংগ্রহ",
        hiName = "मौसमी फूल संग्रह",
        enName = "Seasonal Flowers",
        emoji = "🌼"
    ),
    MONEY_INDOOR_PLANTS(
        code = "MONEY_INDOOR_PLANTS",
        bnName = "মানি প্ল্যান্ট ও ইনডোর গাছ",
        hiName = "मनी प्लांट व इंडोर पौधे",
        enName = "Money Plant & Indoor",
        emoji = "🪴"
    ),
    FRUIT_PLANTS(
        code = "FRUIT_PLANTS",
        bnName = "ফল গাছ ও চারা",
        hiName = "फलदार पौधे व पेड़",
        enName = "Fruit Plants & Trees",
        emoji = "🥭"
    ),
    ORNAMENTAL_TREES_CLIMBERS(
        code = "ORNAMENTAL_TREES_CLIMBERS",
        bnName = "শোভাবর্ধক গাছ ও লতা",
        hiName = "सजावटी पेड़ व लताएँ",
        enName = "Ornamental Trees & Climbers",
        emoji = "🌳"
    ),
    MEDICINAL_HERBS_SUCCULENTS(
        code = "MEDICINAL_HERBS_SUCCULENTS",
        bnName = "ঔষধি, ভেষজ ও সাকুলেন্ট",
        hiName = "औषधीय, जड़ी-बूटी व सक्युलेंट",
        enName = "Medicinal, Herbs & Succulents",
        emoji = "🌿"
    );

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }

    companion object {
        fun fromCode(code: String?): PlantKingdomGroup =
            values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: COMMON_FLOWERS
    }
}

enum class PlantSearchFilter(
    val code: String,
    val bnName: String,
    val hiName: String,
    val enName: String
) {
    FLOWERING("FLOWERING", "ফুল গাছ", "फूलों वाले पौधे", "Flowering plants"),
    HILL_FLOWER("HILL_FLOWER", "পাহাড়ি ফুল", "पहाड़ी फूल", "Hill flowers"),
    INDOOR("INDOOR", "ইনডোর গাছ", "इंडोर पौधे", "Indoor plants"),
    MONEY_PLANT("MONEY_PLANT", "মানি প্ল্যান্ট", "मनी प्लांट", "Money plants"),
    FRUIT("FRUIT", "ফল গাছ", "फलदार पौधे", "Fruit plants"),
    SHADE("SHADE", "ছায়াপ্রিয় গাছ", "छाया पसंद पौधे", "Shade plants"),
    MEDICINAL("MEDICINAL", "ঔষধি গাছ", "औषधीय पौधे", "Medicinal plants"),
    ORNAMENTAL_TREE("ORNAMENTAL_TREE", "শোভাবর্ধক বৃক্ষ", "सजावटी पेड़", "Ornamental trees"),
    SUCCULENT_CACTUS("SUCCULENT_CACTUS", "সাকুলেন্ট ও ক্যাকটাস", "सक्युलेंट व कैक्टस", "Succulents & cactus"),
    SEASONAL("SEASONAL", "মৌসুমি গাছ", "मौसमी पौधे", "Seasonal plants"),
    RARE("RARE", "দুর্লভ উদ্ভিদ", "दुर्लभ पौधे", "Rare plants");

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }
}

enum class PlantSeasonalCollection(
    val code: String,
    val bnName: String,
    val hiName: String,
    val enName: String
) {
    WINTER("WINTER", "শীতকালীন ফুল", "सर्दियों के फूल", "Winter flowers"),
    MONSOON("MONSOON", "বর্ষাকালীন ফুল", "मानसून के फूल", "Monsoon flowers"),
    SUMMER("SUMMER", "গ্রীষ্মকালীন ফুল", "गर्मियों के फूल", "Summer flowers"),
    PERENNIAL("PERENNIAL", "বহুবর্ষজীবী ফুল", "बारहमासी फूल", "Perennial flowers"),
    ANNUAL("ANNUAL", "একবর্ষজীবী ফুল", "वार्षिक फूल", "Annual flowers"),
    CLIMBER("CLIMBER", "ফুলের লতা", "फूलों की लताएँ", "Flowering climbers"),
    SHADE_LOVING("SHADE_LOVING", "ছায়াপ্রিয় ফুল", "छाया-प्रेमी फूल", "Shade-loving flowers");

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }
}

enum class WbNurseryDistrict(
    val code: String,
    val bnName: String,
    val hiName: String,
    val enName: String,
    val centerLat: Double,
    val centerLng: Double
) {
    SILIGURI("SILIGURI", "শিলিগুড়ি", "सिलीगुड़ी", "Siliguri", 26.7271, 88.3953),
    DARJEELING("DARJEELING", "দার্জিলিং", "दार्जिलिंग", "Darjeeling", 27.0410, 88.2663),
    KALIMPONG("KALIMPONG", "কালিম্পং", "कालिम्पोंग", "Kalimpong", 27.0594, 88.4695),
    KURSEONG("KURSEONG", "কার্শিয়াং", "कुर्सियांग", "Kurseong", 26.8821, 88.2789),
    MIRIK("MIRIK", "মিরিক", "मिरिक", "Mirik", 26.8855, 88.1870),
    JALPAIGURI("JALPAIGURI", "জলপাইগুড়ি", "जलपाईगुड़ी", "Jalpaiguri", 26.5435, 88.7196),
    ALIPURDUAR("ALIPURDUAR", "আলিপুরদুয়ার", "अलीपुरद्वार", "Alipurduar", 26.4892, 89.5271),
    COOCH_BEHAR("COOCH_BEHAR", "কোচবিহার", "कूचबिहार", "Cooch Behar", 26.3242, 89.4510),
    NORTH_DINAJPUR("NORTH_DINAJPUR", "উত্তর দিনাজপুর", "उत्तर दिनाजपुर", "North Dinajpur", 25.6329, 88.1319),
    SOUTH_DINAJPUR("SOUTH_DINAJPUR", "দক্ষিণ দিনাজপুর", "दक्षिण दिनाजपुर", "South Dinajpur", 25.2218, 88.7745),
    MALDA("MALDA", "মালদা", "मालदा", "Malda", 25.0108, 88.1411),
    KOLKATA("KOLKATA", "কলকাতা", "कोलकाता", "Kolkata", 22.5726, 88.3639),
    HOWRAH("HOWRAH", "হাওড়া", "हावड़ा", "Howrah", 22.5958, 88.2636),
    HOOGHLY("HOOGHLY", "হুগলি", "हुगली", "Hooghly", 22.9089, 88.3967),
    BARDHAMAN("BARDHAMAN", "বর্ধমান", "बर्धमान", "Bardhaman", 23.2324, 87.8615),
    DURGAPUR("DURGAPUR", "দুর্গাপুর", "दुर्गापुर", "Durgapur", 23.5204, 87.3119),
    ASANSOL("ASANSOL", "আসানসোল", "आसनसोल", "Asansol", 23.6739, 86.9524),
    BANKURA("BANKURA", "বাঁকুড়া", "बांकुरा", "Bankura", 23.2313, 87.0784),
    PURULIA("PURULIA", "পুরুলিয়া", "पुरुलिया", "Purulia", 23.3322, 86.3616),
    NADIA("NADIA", "নদীয়া", "नदिया", "Nadia", 23.4710, 88.5565),
    MURSHIDABAD("MURSHIDABAD", "মুর্শিদাবাদ", "मुर्शिदाबाद", "Murshidabad", 24.1759, 88.2802),
    OTHER_WB("OTHER_WB", "অন্যান্য পশ্চিমবঙ্গ জেলা", "अन्य पश्चिम बंगाल ज़िले", "Other West Bengal districts", 24.4000, 88.2000);

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }

    companion object {
        fun fromCode(code: String?): WbNurseryDistrict =
            values().firstOrNull {
                it.code.equals(code, ignoreCase = true) ||
                    it.enName.equals(code, ignoreCase = true) ||
                    it.bnName.equals(code, ignoreCase = true)
            } ?: OTHER_WB
    }
}

object NurseryVerificationStatus {
    const val PENDING_VERIFICATION = "PENDING_VERIFICATION"
    const val VERIFIED_PARTNER = "VERIFIED_PARTNER"
    const val ADMIN_VERIFIED = "ADMIN_VERIFIED"
}

@IgnoreExtraProperties
data class PlantKingdomEntry(
    val plantId: String = "",
    val nameEn: String = "",
    val nameBn: String = "",
    val nameHi: String = "",
    val scientificName: String = "",
    val primaryGroup: String = PlantKingdomGroup.COMMON_FLOWERS.code,
    val secondaryGroupsCsv: String = "",
    val filterTagsCsv: String = "",
    val seasonalTagsCsv: String = "",
    val descriptionEn: String = "",
    val descriptionBn: String = "",
    val descriptionHi: String = "",
    val visualAssetKey: String = "",
    val customPhotoUri: String = "",
    val climate: String = "",
    val temperatureRange: String = "",
    val soilRequirements: String = "",
    val sunlight: String = "",
    val wateringFrequency: String = "",
    val fertilizerGuidance: String = "",
    val propagationMethod: String = "",
    val floweringOrFruitingSeason: String = "",
    val growthSize: String = "",
    val indoorOutdoorSuitability: String = "",
    val plainsHillSuitability: String = "",
    val commonPestsDiseases: String = "",
    val careInstructionsEn: String = "",
    val careInstructionsBn: String = "",
    val careInstructionsHi: String = "",
    val verifiedNurseryIdsCsv: String = "",
    val isRare: Boolean = false,
    val isVerifiedEntry: Boolean = true,
    val publicationStatus: String = "PUBLISHED",
    val sourceReference: String = "ICAR / BCKV / UBKV / BSI Botanical Compendium",
    val lastVerifiedDate: String = "2026-11-18",
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun isDraftReviewRequired(): Boolean =
        publicationStatus.equals("DRAFT_REVIEW_REQUIRED", ignoreCase = true) || !isVerifiedEntry

    fun isPublishedForPublic(): Boolean =
        isVerifiedEntry && !publicationStatus.equals("DRAFT_REVIEW_REQUIRED", ignoreCase = true)

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> nameBn.ifBlank { nameEn }
        AppLanguage.HINDI -> nameHi.ifBlank { nameEn }
        AppLanguage.ENGLISH -> nameEn.ifBlank { nameBn }
    }

    fun getDisplayDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> descriptionBn.ifBlank { descriptionEn }
        AppLanguage.HINDI -> descriptionHi.ifBlank { descriptionEn }
        AppLanguage.ENGLISH -> descriptionEn.ifBlank { descriptionBn }
    }

    fun getDisplayCareInstructions(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> careInstructionsBn.ifBlank { careInstructionsEn }
        AppLanguage.HINDI -> careInstructionsHi.ifBlank { careInstructionsEn }
        AppLanguage.ENGLISH -> careInstructionsEn.ifBlank { careInstructionsBn }
    }

    fun getGroupEnum(): PlantKingdomGroup = PlantKingdomGroup.fromCode(primaryGroup)

    fun belongsToGroup(group: PlantKingdomGroup): Boolean {
        if (primaryGroup.equals(group.code, ignoreCase = true)) return true
        return secondaryGroupsCsv.split(",").any { it.trim().equals(group.code, ignoreCase = true) }
    }

    fun matchesFilter(filter: PlantSearchFilter): Boolean {
        val tags = filterTagsCsv.split(",").map { it.trim().uppercase() }
        return tags.contains(filter.code) || (filter == PlantSearchFilter.RARE && isRare)
    }

    fun matchesSeasonal(col: PlantSeasonalCollection): Boolean {
        val tags = seasonalTagsCsv.split(",").map { it.trim().uppercase() }
        return tags.contains(col.code)
    }

    fun matchesSearch(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return nameEn.lowercase().contains(q) ||
            nameBn.lowercase().contains(q) ||
            nameHi.lowercase().contains(q) ||
            scientificName.lowercase().contains(q) ||
            descriptionEn.lowercase().contains(q) ||
            descriptionBn.lowercase().contains(q) ||
            descriptionHi.lowercase().contains(q) ||
            climate.lowercase().contains(q)
    }
}

@IgnoreExtraProperties
data class WbNurseryDirectoryEntry(
    val nurseryId: String = "",
    val businessName: String = "",
    val businessNameBn: String = "",
    val businessNameHi: String = "",
    val ownerName: String? = null,
    val fullAddress: String? = null,
    val districtCode: String = WbNurseryDistrict.OTHER_WB.code,
    val locality: String? = null,
    val state: String = "West Bengal",
    val phone: String? = null,
    val whatsapp: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val websiteOrSocial: String? = null,
    val openingHours: String? = null,
    val nurseryPhotosCsv: String = "",
    val availablePlantCategoriesCsv: String = "",
    val availablePlantIdsCsv: String = "",
    val supportsWholesale: Boolean? = null,
    val supportsRetail: Boolean? = null,
    val deliveryAvailable: Boolean? = null,
    val descriptionEn: String = "",
    val descriptionBn: String = "",
    val descriptionHi: String = "",
    val verificationStatus: String = NurseryVerificationStatus.PENDING_VERIFICATION,
    val ownerConsentStatus: String = "NOT_CONFIRMED",
    val isVerified: Boolean = false,
    val sourceReference: String = "",
    val lastVerifiedDate: String = "2026-11-18",
    val notes: String = "",
    val linkedSellerUserId: String? = null,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> businessNameBn.ifBlank { businessName }
        AppLanguage.HINDI -> businessNameHi.ifBlank { businessName }
        AppLanguage.ENGLISH -> businessName.ifBlank { businessNameBn }
    }

    fun getDistrictEnum(): WbNurseryDistrict = WbNurseryDistrict.fromCode(districtCode)

    fun hasVerifiedPhone(): Boolean = !phone.isNullOrBlank()

    fun hasVerifiedCoordinates(): Boolean = latitude != null && longitude != null

    fun matchesSearch(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return businessName.lowercase().contains(q) ||
            businessNameBn.lowercase().contains(q) ||
            businessNameHi.lowercase().contains(q) ||
            (ownerName?.lowercase()?.contains(q) == true) ||
            (fullAddress?.lowercase()?.contains(q) == true) ||
            (locality?.lowercase()?.contains(q) == true) ||
            getDistrictEnum().enName.lowercase().contains(q) ||
            getDistrictEnum().bnName.lowercase().contains(q) ||
            getDistrictEnum().hiName.lowercase().contains(q)
    }
}
