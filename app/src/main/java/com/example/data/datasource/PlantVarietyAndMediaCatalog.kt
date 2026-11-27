package com.example.data.datasource

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.DrawableRes
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.MachineryProduct
import com.example.data.model.MasterCrop
import com.example.data.model.PlantKingdomEntry
import com.example.ui.home.DashboardCatalog

/**
 * Agronomic regional suitability classification for Krishak Sathi plants & cultivars.
 */
enum class RegionalSuitabilityTag(
    val code: String,
    val bnName: String,
    val hiName: String,
    val enName: String
) {
    NORTH_BENGAL_PLAINS("NORTH_BENGAL_PLAINS", "উত্তরবঙ্গ সমতল", "उत्तर बंगाल मैदान", "North Bengal Plains"),
    TERAI_DOOARS("TERAI_DOOARS", "তরাই ও ডুয়ার্স", "तराई व डुआर्स", "Terai & Dooars"),
    DARJEELING_KALIMPONG_HILLS("DARJEELING_KALIMPONG_HILLS", "দার্জিলিং ও কালিম্পং পাহাড়", "दार्जिलिंग व कालिम्पोंग पहाड़", "Darjeeling & Kalimpong Hills"),
    TROPICAL("TROPICAL", "গ্রীষ্মমণ্ডলীয় (Tropical)", "उष्णकटिबंधीय (Tropical)", "Tropical"),
    SUBTROPICAL("SUBTROPICAL", "উপ-গ্রীষ্মমণ্ডলীয় (Subtropical)", "उपोष्णकटिबंधीय (Subtropical)", "Subtropical"),
    TEMPERATE("TEMPERATE", "শীতল পার্বত্য (Temperate)", "शीतोष्ण पहाड़ी (Temperate)", "Temperate Hills"),
    PROTECTED_CULTIVATION("PROTECTED_CULTIVATION", "শেড-নেট / পলিহাউস (Protected)", "पॉलीहाउस / शेड-नेट (Protected)", "Protected Cultivation");

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }
}

enum class BotanicalPhotoStage(
    val code: String,
    val bnTitle: String,
    val hiTitle: String,
    val enTitle: String
) {
    YOUNG_SAPLING("YOUNG_SAPLING", "চারা অবস্থা", "पौध अवस्था", "Young Sapling"),
    LEAF_STEM_CLOSEUP("LEAF_STEM_CLOSEUP", "পাতা ও কাণ্ড", "पत्ती व तना", "Leaf & Stem Close-up"),
    MATURE_BEARING_PLANT("MATURE_BEARING_PLANT", "পরিণত গাছ", "परिपक्व पौधा", "Mature Bearing Plant"),
    RIPE_FRUIT("RIPE_FRUIT", "পাকা ফল", "पका हुआ फल", "Ripe Fruit");

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnTitle
        AppLanguage.HINDI -> hiTitle
        AppLanguage.ENGLISH -> enTitle
    }
}

enum class MediaVerificationStatus(
    val code: String,
    val bnLabel: String,
    val hiLabel: String,
    val enLabel: String
) {
    VERIFIED("VERIFIED", "যাচাইকৃত মিডিয়া", "सत्यापित मीडिया", "VERIFIED"),
    IMAGE_PENDING_VERIFICATION(
        "IMAGE_PENDING_VERIFICATION",
        "ছবি যাচাই প্রক্রিয়াধীন (IMAGE_PENDING_VERIFICATION)",
        "फोटो सत्यापन शेष (IMAGE_PENDING_VERIFICATION)",
        "IMAGE_PENDING_VERIFICATION"
    );

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnLabel
        AppLanguage.HINDI -> hiLabel
        AppLanguage.ENGLISH -> enLabel
    }
}

data class PlantMediaRecord(
    val mediaId: String,
    val plantId: String,
    val stage: BotanicalPhotoStage,
    @DrawableRes val drawableResId: Int? = null,
    val customUri: String = "",
    val sourceName: String,
    val creator: String,
    val license: String,
    val attribution: String,
    val verificationStatus: MediaVerificationStatus,
    val verificationDate: String,
    val isSellerStockPhoto: Boolean = false
)

data class PlantVarietyRecord(
    val varietyId: String,
    val parentPlantId: String,
    val nameEn: String,
    val nameBn: String,
    val nameHi: String,
    val specialtyEn: String,
    val specialtyBn: String,
    val specialtyHi: String,
    val seasonEn: String,
    val seasonBn: String,
    val seasonHi: String,
    val recommendedRegions: Set<RegionalSuitabilityTag>
) {
    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> nameBn
        AppLanguage.HINDI -> nameHi
        AppLanguage.ENGLISH -> nameEn
    }

    fun getSpecialty(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> specialtyBn
        AppLanguage.HINDI -> specialtyHi
        AppLanguage.ENGLISH -> specialtyEn
    }

    fun getSeason(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> seasonBn
        AppLanguage.HINDI -> seasonHi
        AppLanguage.ENGLISH -> seasonEn
    }
}

data class CatalogMediaAuditItem(
    val recordType: String,
    val itemId: String,
    val titleEn: String,
    val titleBn: String,
    val titleHi: String,
    val stageCode: String,
    val status: MediaVerificationStatus,
    val provenanceNote: String
)

data class CatalogMediaAuditReport(
    val totalPlantsAudited: Int,
    val totalPlantStageSlots: Int,
    val verifiedPlantMediaCount: Int,
    val pendingPlantMediaCount: Int,
    val totalCropsAudited: Int,
    val verifiedCropsMediaCount: Int,
    val pendingCropsMediaCount: Int,
    val totalMachineryAudited: Int,
    val verifiedMachineryMediaCount: Int,
    val totalDashboardTilesWithIcons: Int,
    val totalDashboardTilesMissingIcons: Int,
    val totalVarietiesCataloged: Int,
    val pendingReviewItems: List<CatalogMediaAuditItem>
)

object AdminMediaReviewStore {
    private const val PREF_NAME = "krishak_sathi_media_review_v1"
    private const val KEY_PREFIX_URI = "uri_"
    private const val KEY_PREFIX_STATUS = "status_"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveStageReview(
        context: Context,
        plantId: String,
        stage: BotanicalPhotoStage,
        customUri: String,
        status: MediaVerificationStatus
    ) {
        val key = "${plantId.lowercase()}::${stage.code}"
        prefs(context).edit()
            .putString("$KEY_PREFIX_URI$key", customUri.trim())
            .putString("$KEY_PREFIX_STATUS$key", status.code)
            .apply()
    }

    fun getStageCustomUri(context: Context, plantId: String, stage: BotanicalPhotoStage): String {
        val key = "${plantId.lowercase()}::${stage.code}"
        return prefs(context).getString("$KEY_PREFIX_URI$key", "").orEmpty()
    }

    fun getStageStatusOverride(
        context: Context,
        plantId: String,
        stage: BotanicalPhotoStage
    ): MediaVerificationStatus? {
        val key = "${plantId.lowercase()}::${stage.code}"
        val raw = prefs(context).getString("$KEY_PREFIX_STATUS$key", null) ?: return null
        return MediaVerificationStatus.values().firstOrNull { it.code == raw }
    }
}

object PlantVarietyAndMediaCatalog {

    fun getSaplingVerificationDisclaimer(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI ->
            "চারা সতর্কতা: শুধুমাত্র সাধারণ চারার ছবি দেখে সঠিক জাত (যেমন হিমসাগর বনাম ল্যাংড়া) নিশ্চিত করা যায় না। কেনার আগে কলমের গাঁট, ট্যাগ ও যাচাইকৃত নার্সারির তথ্য মিলিয়ে নিন।"
        AppLanguage.HINDI ->
            "पौध सत्यापन सूचना: केवल सामान्य पौधे की फोटो देखकर सटीक किस्म (जैसे हिमसागर बनाम लंगड़ा) की पुष्टि नहीं की जा सकती। खरीद से पहले ग्राफ्ट टैग व सत्यापित नर्सरी से पुष्टि करें।"
        AppLanguage.ENGLISH ->
            "Cultivar Identity Notice: Exact cultivar/variety cannot be confirmed from a generic sapling photograph alone. Always confirm graft tags and nursery certification before purchase."
    }

    private val highChillHillPlantIds = setOf(
        "cherry", "apricot", "peach", "plum", "pear", "kiwi", "walnut",
        "persimmon", "almond", "pistachio", "raspberry", "blackberry",
        "blueberry", "himalayan_blue_poppy", "rhododendron", "magnolia",
        "cymbidium_orchid", "coelogyne_orchid", "pleione_orchid", "paphiopedilum_orchid",
        "himalayan_lily", "himalayan_iris"
    )

    private val protectedCultivationPlantIds = setOf(
        "cymbidium_orchid", "dendrobium_orchid", "coelogyne_orchid", "pleione_orchid",
        "paphiopedilum_orchid", "blueberry", "strawberry", "anthurium", "gerbera",
        "dragon_fruit", "passion_fruit", "jabuticaba", "aloe_vera"
    )

    fun inferRegionalSuitability(plant: PlantKingdomEntry): Set<RegionalSuitabilityTag> {
        val id = plant.plantId.lowercase()
        val text = "${plant.plainsHillSuitability} ${plant.climate} ${plant.indoorOutdoorSuitability}".lowercase()
        val tags = linkedSetOf<RegionalSuitabilityTag>()

        if (id == "apple") {
            tags.add(RegionalSuitabilityTag.DARJEELING_KALIMPONG_HILLS)
            tags.add(RegionalSuitabilityTag.TEMPERATE)
            tags.add(RegionalSuitabilityTag.SUBTROPICAL)
            return tags
        }

        if (id == "pistachio" || id == "almond") {
            tags.add(RegionalSuitabilityTag.TEMPERATE)
            tags.add(RegionalSuitabilityTag.DARJEELING_KALIMPONG_HILLS)
            return tags
        }

        if (id in highChillHillPlantIds) {
            tags.add(RegionalSuitabilityTag.DARJEELING_KALIMPONG_HILLS)
            tags.add(RegionalSuitabilityTag.TEMPERATE)
            if (id in protectedCultivationPlantIds) {
                tags.add(RegionalSuitabilityTag.PROTECTED_CULTIVATION)
            }
            return tags
        }

        if (text.contains("hill") || text.contains("darjeeling") || text.contains("kalimpong") || text.contains("kurseong") || text.contains("mirik")) {
            tags.add(RegionalSuitabilityTag.DARJEELING_KALIMPONG_HILLS)
        }
        if (text.contains("temperate")) {
            tags.add(RegionalSuitabilityTag.TEMPERATE)
        }
        if (text.contains("terai") || text.contains("dooars") || text.contains("siliguri") || text.contains("jalpaiguri")) {
            tags.add(RegionalSuitabilityTag.TERAI_DOOARS)
        }
        if (text.contains("plain") || text.contains("alluvial") || text.contains("bengal")) {
            tags.add(RegionalSuitabilityTag.NORTH_BENGAL_PLAINS)
        }
        if (text.contains("tropical")) {
            tags.add(RegionalSuitabilityTag.TROPICAL)
        }
        if (text.contains("subtropical") || text.contains("sub-tropical")) {
            tags.add(RegionalSuitabilityTag.SUBTROPICAL)
        }
        if (id in protectedCultivationPlantIds || text.contains("polyhouse") || text.contains("protected") || text.contains("shade")) {
            tags.add(RegionalSuitabilityTag.PROTECTED_CULTIVATION)
        }

        if (tags.isEmpty()) {
            tags.add(RegionalSuitabilityTag.NORTH_BENGAL_PLAINS)
            tags.add(RegionalSuitabilityTag.TERAI_DOOARS)
            tags.add(RegionalSuitabilityTag.SUBTROPICAL)
        }
        return tags
    }

    val allVarieties: List<PlantVarietyRecord> = listOf(
        // ---------------- MANGO (12 Verified Varieties) ----------------
        v("mango_himsagar", "mango", "Himsagar", "হিমসাগর আম", "हिमसागर आम", "GI-tagged sweet fibreless aroma; Bengal's crown table mango", "জিআই-স্বীকৃত আঁশহীন সুমিষ্ট ও অতুলনীয় সুগন্ধি বাংলার শ্রেষ্ঠ আম", "जीआई-टैग प्राप्त रेशे-रहित व अति सुगंधित प्रमुख बंगाली आम", "Late May – June", "জ্যৈষ্ঠের শেষ – আষাঢ়", "मई अंत – जून", plainsAndTerai()),
        v("mango_langra", "mango", "Langra", "ল্যাংড়া আম", "लंगड़ा आम", "Thin skin with citrusy-sweet green ripe pulp", "পাতলা খোসা ও পাকা অবস্থায়ও সবুজ-আভাযুক্ত রসালো মিষ্টি শাঁস", "पतला छिलका और पकने पर भी हरे रंग का रसीला मीठा गूदा", "June – Early July", "আষাঢ়", "जून – जुलाई प्रारंभ", plainsAndTerai()),
        v("mango_fazli", "mango", "Fazli", "ফজলি আম", "फजली आम", "Late-season giant Malda heirloom fruit ideal for slices & pulp", "মালদার ঐতিহ্যবাহী বড় আকারের নাবি জাত, জুলাই-আগস্টে পাকে", "मालदा की बड़ी देर से पकने वाली पारंपरिक किस्म", "July – August", "শ্রাবণ – ভাদ্র", "जुलाई – अगस्त", plainsAndTerai()),
        v("mango_amrapali", "mango", "Amrapali", "আম্রপালি আম", "आम्रपाली आम", "Dwarf regular-bearer suited to high-density orchards & terrace drums", "খাটো ও নিয়মিত ফলনশীল — ঘন বাগান ও ছাদের বড় ড্রামের জন্য সেরা", "बौना व नियमित फलने वाला पौधा — घने बाग व ड्रम खेती हेतु उत्तम", "June – July", "আষাঢ় – শ্রাবণ", "जून – जुलाई", plainsAndTerai()),
        v("mango_mallika", "mango", "Mallika", "মল্লিকা আম", "मल्लिका आम", "Neelum × Dasheri cross delivering honeyed orange fibreless flesh", "নীলম ও দশেরির সংকর; গাঢ় কমলা শাঁস ও দীর্ঘ সংরক্ষণ ক্ষমতা", "नीलम × दशहरी संकर; गहरा नारंगी रेशा-रहित मीठा गूदा", "June – July", "আষাঢ় – শ্রাবণ", "जून – जुलाई", plainsAndTerai()),
        v("mango_alphonso", "mango", "Alphonso", "আলফানসো (হাপুস)", "अल्फांसो (हापुस)", "Saffron-toned aromatic export-grade premium cultivar", "জাফরান-রঙা শাঁস ও বিশ্বখ্যাত রপ্তানিযোগ্য সুগন্ধি আম", "केसरिया गूदे व अद्वितीय सुगंध वाली निर्यात-योग्य किस्म", "April – June", "বৈশাখ – জ্যৈষ্ঠ", "अप्रैल – जून", warmPlains()),
        v("mango_kesar", "mango", "Kesar", "কেসর আম", "केसर आम", "Bright saffron pulp and reliable annual bearing across plains", "উজ্জ্বল গেরুয়া শাঁস ও নিয়মিত ভালো ফলনশীল বাণিজ্যিক জাত", "चमकदार केसरिया गूदे वाली लोकप्रिय व्यावसायिक किस्म", "May – June", "জ্যৈষ্ঠ – আষাঢ়", "मई – जून", warmPlains()),
        v("mango_dasheri", "mango", "Dasheri", "দশেরি আম", "दशहरी आम", "Slender sweet North Indian classic bursting with dessert aroma", "পাতলা বীজ ও অতুলনীয় মিষ্টি সুগন্ধযুক্ত উত্তর ভারতীয় জাত", "पतली गुठली व अत्यंत मीठे स्वाद वाला प्रसिद्ध उत्तर भारतीय आम", "June – July", "জ্যৈষ্ঠ – আষাঢ়", "जून – जुलाई", plainsAndTerai()),
        v("mango_banganapalli", "mango", "Banganapalli", "বঙ্গনাপল্লী (সফেদা)", "बंगनपल्ली (सफेदा)", "Large golden oval mango with firm non-fibrous slices", "বড় ডিম্বাকৃতি সোনালি আম ও শক্ত আঁশহীন শাঁস", "बड़ा सुनहरा बिना रेशे का ठोस व मीठा फल", "May – June", "জ্যৈষ্ঠ", "मई – जून", warmPlains()),
        v("mango_gopalbhog", "mango", "Gopalbhog", "গোপালভোগ আম", "गोपालभोग आम", "Early Bengal gourmet heirloom first to arrive in markets", "মৌসুমের শুরুর দিকেই পাকা বাংলার আদি সুগন্ধি মিষ্টি আম", "मौसम की शुरुआत में पकने वाली बंगाल की प्राचीन सुगंधित किस्म", "May", "জ্যৈষ্ঠ", "मई", plainsAndTerai()),
        v("mango_lakshmanbhog", "mango", "Lakshmanbhog", "লক্ষ্মণভোগ আম", "लक्ष्मणभोग आम", "Malda GI blushing golden-rose export cultivar with crisp aroma", "মালদার জিআই-ট্যাগযুক্ত লালচে আভার জনপ্রিয় রপ্তানি জাত", "मालदा की लालिमा-युक्त GI-टैग प्राप्त निर्यात किस्म", "June", "আষাঢ়", "जून", plainsAndTerai()),
        v("mango_miyazaki", "mango", "Miyazaki", "মিয়াজাকি (লাল সূর্যডিম)", "मियाज़ाकी (ताईयो नो तमागो)", "Collector ruby-red skin mango requiring strict canopy & sun control", "সংগ্রাহক-প্রিয় গাঢ় লাল মিয়াজাকি আম (সঠিক ব্যাগিং ও সূর্যালোক প্রয়োজন)", "गहरे लाल रंग की दुर्लभ संग्राहक किस्म (सही बैगिंग आवश्यक)", "June – July", "আষাঢ়", "जून – जुलाई", setOf(RegionalSuitabilityTag.SUBTROPICAL, RegionalSuitabilityTag.TERAI_DOOARS, RegionalSuitabilityTag.NORTH_BENGAL_PLAINS, RegionalSuitabilityTag.PROTECTED_CULTIVATION)),

        // ---------------- LITCHI (6 Verified Varieties) ----------------
        v("litchi_shahi", "litchi", "Shahi", "শাহী লিচু", "शाही लीची", "Early fragrant rose-scented juicy heirloom famous across Eastern India", "আগাম ও গোলাপের সুগন্ধযুক্ত রসালো ঐতিহ্যবাহী লিচু", "अगेती व गुलाब जैसी खुशबू वाली अत्यंत रसदार प्रसिद्ध लीची", "May – Early June", "জ্যৈষ্ঠ", "मई – जून प्रारंभ", plainsAndTerai()),
        v("litchi_bombai", "litchi", "Bombai", "বোম্বাই লিচু", "बंबई लीची", "North Bengal & Murshidabad commercial mainstay with large crimson shoulder", "পশ্চিমবঙ্গ ও উত্তরবঙ্গের প্রধান বাণিজ্যিক বড় লাল লিচু", "पश्चिम बंगाल की प्रमुख बड़े आकार वाली व्यावसायिक किस्म", "May – June", "জ্যৈষ্ঠ – আষাঢ়", "मई – जून", plainsAndTerai()),
        v("litchi_china", "litchi", "China (China-3)", "চায়না-৩ লিচু", "चाइना-3 लीची", "Compact tree canopy with small seed and very high sweet pulp ratio", "ছোট বীজ, পুরু মিষ্টি শাঁস ও কম উচ্চতার লাভজনক জাত", "छोटे बीज व मोटे मीठे गूदे वाला कम ऊँचाई का पौधा", "June", "আষাঢ়", "जून", plainsAndTerai()),
        v("litchi_rose_scented", "litchi", "Rose Scented", "রোজ সেন্টেড লিচু", "रोज़ सेंटेड लीची", "Delicate floral aroma with bright crimson skin & crisp flesh", "গোলাপের মতো মিষ্টি সুবাস ও রসালো সাদা শাঁস", "गुलाबी सुगंध और मीठे सफेद गूदे वाली उत्तम किस्म", "May – June", "জ্যৈষ্ঠ", "मई – जून", plainsAndTerai()),
        v("litchi_bedana", "litchi", "Bedana", "বেদানা (বীজহীন/ক্ষুদ্রবীজ) লিচু", "बेदाना (छोटा बीज) लीची", "Shrivelled tiny chicken-tongue seed surrounded by abundant sweet flesh", "অতি ক্ষুদ্র বীজ ও সর্বাধিক শাঁসযুক্ত অত্যন্ত মিষ্টি জাত", "बहुत छोटे बीज व सर्वाधिक गूदे वाली मीठी बेदाना किस्म", "Late May – June", "জ্যৈষ্ঠের শেষ – আষাঢ়", "मई अंत – जून", plainsAndTerai()),
        v("litchi_purbi", "litchi", "Purbi", "পূর্বি লিচু", "पूर्वी लीची", "Mid-late heavy bunches suited to humid Terai & eastern alluvial belts", "আর্দ্র তরাই ও গাঙ্গেয় সমতলের থোকায় ধরা মাঝারি-নাবি লিচু", "पूर्वी तराई व नम जलवायु के लिए उपयुक्त भारी गुच्छों वाली किस्म", "June", "আষাঢ়", "जून", plainsAndTerai()),

        // ---------------- GUAVA (5 Verified Varieties) ----------------
        v("guava_allahabad_safeda", "guava", "Allahabad Safeda", "এলাহাবাদ সফেদা পেয়ারা", "इलाहाबाद सफेदा अमरूद", "Cream-white crisp sweet flesh and steady high-yield adaptability", "সাদা ও মচমচে মিষ্টি শাঁস এবং চমৎকার সংরক্ষণ গুণ", "सफेद मीठा कुरकुरा गूदा व अधिक उपज क्षमता", "Aug – Jan", "ভাদ্র – মাঘ", "अगस्त – जनवरी", setOf(RegionalSuitabilityTag.NORTH_BENGAL_PLAINS, RegionalSuitabilityTag.TERAI_DOOARS, RegionalSuitabilityTag.DARJEELING_KALIMPONG_HILLS, RegionalSuitabilityTag.SUBTROPICAL)),
        v("guava_l49", "guava", "Lucknow-49 (Sardar)", "লখনউ-৪৯ (সর্দার পেয়ারা)", "लखनऊ-49 (सरदार अमरूद)", "Large cream-white heavy winter fruit tolerating diverse soils", "বড় শীতকালীন মিষ্টি ফল ও উচ্চ রোগপ্রতিরোধ ক্ষমতা", "बड़े आकार, अधिक उपज व रोग-प्रतिरोधक सरदार किस्म", "Oct – Feb", "কার্তিক – মাঘ", "अक्टूबर – फरवरी", setOf(RegionalSuitabilityTag.NORTH_BENGAL_PLAINS, RegionalSuitabilityTag.TERAI_DOOARS, RegionalSuitabilityTag.DARJEELING_KALIMPONG_HILLS, RegionalSuitabilityTag.SUBTROPICAL)),
        v("guava_lalit", "guava", "Lalit", "ললিত (গোলাপি) পেয়ারা", "ललित (गुलाबी गूदा) अमरूद", "Dual-purpose saffron-pink interior rich in pectin & table flavour", "ভেতরে গোলাপি শাঁস, সরাসরি খাওয়া ও জেলি তৈরির সেরা জাত", "गुलाबी-केसरिया गूदे वाली ताज़ा खाने व प्रसंस्करण योग्य किस्म", "Aug – Jan", "ভাদ্র – মাঘ", "अगस्त – जनवरी", plainsAndTerai()),
        v("guava_thai", "guava", "Thai Guava (Thai-7 / VNR Bihi)", "থাই-৭ / ভিএনআর বিহি পেয়ারা", "थाई-7 / VNR बीही अमरूद", "Extra-large crisp crunch (400–800g) staying sweet when bagged early", "৪০০-৮০০ গ্রাম ওজনের বড়, বীজ কম ও দীর্ঘস্থায়ী মচমচে পেয়ারা", "400-800 ग्राम का बड़ा कुरकुरा व कम बीज वाला लोकप्रिय अमरूद", "Year-round flushes", "সারা বছর (ব্যাগিং সহ)", "वर्ष भर (बैगिंग सहित)", plainsAndTerai()),
        v("guava_pink", "guava", "Pink Guava (Thai Pink / Arka Kiran)", "থাই পিঙ্ক / অর্ক কিরণ পেয়ারা", "थाई पिंक / अर्का किरण अमरूद", "Deep salmon-pink velvety aromatic interior & dwarf home-pot bearing", "গাঢ় গোলাপি সুগন্ধি শাঁস — ছাদের টবেই প্রচুর ফল ধরে", "गहरे गुलाबी गूदे वाला सुगंधित व गमलों के लिए उपयुक्त अमरूद", "Oct – Mar", "কার্তিক – চৈत्र", "अक्टूबर – मार्च", plainsAndTerai()),

        // ---------------- OTHER VERIFIED FRUIT, FLOWER & NURSERY CULTIVARS ----------------
        v("darjeeling_mandarin", "darjeeling_orange", "Darjeeling Loose-Jacket Mandarin", "দার্জিলিং ম্যান্ডারিন কমলা", "दार्जिलिंग संतरा (ढीला छिलका)", "Signature GI hill sweet-tart loose-peel citrus of Mirik & Kalimpong", "মিরিক, কার্শিয়াং ও কালিম্পং-এর বিশ্বখ্যাত সুমিষ্ট কমলালেবু", "मिरिक, कुर्सियांग व कालिम्पोंग की प्रसिद्ध पहाड़ी संतरा किस्म", "Nov – Jan", "অগ্রহায়ণ – মাঘ", "नवंबर – जनवरी", setOf(RegionalSuitabilityTag.DARJEELING_KALIMPONG_HILLS, RegionalSuitabilityTag.SUBTROPICAL)),
        v("malta_bari1", "malta", "BARI-1 Sweet Malta", "বারি-১ মিষ্টি মাল্টা", "बारी-1 मीठा माल्टा", "Reliable green-golden sweet juice orange with distinctive coin-ring base", "পেছনে পয়সার মতো দাগযুক্ত অত্যন্ত মিষ্টি রসালো সবুজ মাল্টা", "पीछे सिक्के जैसा निशान व अत्यंत मीठे रस वाला माल्टा", "Oct – Dec", "কার্তিক – পৌষ", "अक्टूबर – दिसंबर", plainsAndTerai()),
        v("banana_martaman", "banana", "Martaman (Sabri / Malbhog)", "মর্তমান কলা (মালভোগ)", "मर्तमान / मालभोग केला", "Aromatic thin-skin dessert banana famed across North Bengal", "উত্তরবঙ্গের জনপ্রিয় সুগন্ধি পাতলা খোসার মর্তমান / মালভোগ কলা", "उत्तर बंगाल का सुगंधित व पतले छिलके वाला मालभोग केला", "12 months cycle", "সারা বছর", "वर्ष भर", plainsAndTerai()),
        v("banana_g9", "banana", "Grand Naine (G-9 Tissue Culture)", "জি-৯ টিস্যু-কালচার কলা", "G-9 टिश्यू कल्चर केला", "Uniform high-bunch export dwarf Cavendish cultivar", "অধিক ওজনের কাঁদি ও রোগমুক্ত টিস্যু-কালচার জি-৯ কলা", "भारी घार व रोग-मुक्त उच्च उपज वाली G-9 किस्म", "11–12 months", "সারা বছর", "वर्ष भर", plainsAndTerai()),
        v("papaya_red_lady", "papaya", "Red Lady 786 (F1)", "রেড লেডি ৭৮৬ (F1)", "रेड लेडी 786 (F1) पपीता", "Bisexual plants fruiting from low height with sweet crimson flesh", "প্রতিটি গাছেই ফল ধরা লাল-কমলা মিষ্টি শাঁসের রেড লেডি পেঁপে", "कम ऊँचाई से ही फलने वाली मीठी व लाल गूदे की पपीता किस्म", "9–10 months onwards", "৯-১০ মাস পর থেকে", "9-10 माह बाद वर्ष भर", plainsAndTerai()),
        v("pineapple_kew", "pineapple", "Kew (Siliguri Bidhannagar)", "কিউ আনারস (বিধাননগর)", "क्यू अनानास (बिधाननगर)", "Juicy commercial canning & fresh-cut cultivar ideal for Terai", "শিলিগুড়ি ও উত্তর দিনাজপুরের বিশাল রসালো কিউ আনারস", "सिलीगुड़ी-तराई की प्रसिद्ध बड़ी रसदार क्यू अनानास किस्म", "Jun – Aug", "আষাঢ় – ভাদ্র", "जून – अगस्त", setOf(RegionalSuitabilityTag.TERAI_DOOARS, RegionalSuitabilityTag.NORTH_BENGAL_PLAINS, RegionalSuitabilityTag.TROPICAL)),
        v("apple_hrmn99", "apple", "HRMN-99 (Low-Chill Subtropical)", "হরমন-৯৯ (লো-চিল সমতল আপেল)", "HRMN-99 (लो-चिल सेब)", "Self-fertile low-chill selection tolerating mild subtropical winters", "কম শীতের সমতল ও নিচু পাহাড়েও ফলনশীল বিশেষ আপেল জাত", "कम ठंड वाले क्षेत्रों में भी फलने वाली लो-चिल सेब किस्म", "Jun – Jul", "জ্যৈষ্ঠ – আষাঢ়", "जून – जुलाई", setOf(RegionalSuitabilityTag.DARJEELING_KALIMPONG_HILLS, RegionalSuitabilityTag.SUBTROPICAL)),
        v("rose_kashmiri", "rose", "Darjeeling & Hybrid Tea Rose", "দার্জিলিং হাইব্রিড টি গোলাপ", "दार्जिलिंग हाइब्रिड टी गुलाब", "Long-stemmed exhibition blooms thriving in crisp hills & winter plains", "লম্বা ডাঁটা ও বড় পাপড়ির দার্জিলিং ও শীতকালীন গোলাপ", "लंबी डंठल व बड़े फूलों वाला हाइब्रिड टी गुलाब", "Nov – Apr", "অগ্রহায়ণ – চৈত্র", "नवंबर – अप्रैल", setOf(RegionalSuitabilityTag.DARJEELING_KALIMPONG_HILLS, RegionalSuitabilityTag.NORTH_BENGAL_PLAINS, RegionalSuitabilityTag.SUBTROPICAL)),
        v("marigold_inca", "marigold", "Inca Orange / Puja Basanti", "ইনকা কমলা ও পূজার গাঁদা", "इनका नारंगी व पूजा गेंदा", "Dense double-petal festive marigold balls bred for cut-garland market", "পূজা ও মালা গাঁথার জন্য বড় ও ঘন কমলা-হলুদ গাঁদা জাত", "पूजा व माला व्यापार के लिए घने बड़े गेंदा के फूल", "Oct – Mar", "আশ্বিন – চৈত্র", "अक्टूबर – मार्च", plainsAndTerai())
    )

    fun getVarietiesForPlant(plantId: String): List<PlantVarietyRecord> =
        allVarieties.filter { it.parentPlantId.equals(plantId.trim(), ignoreCase = true) }

    fun getStageMediaGallery(
        context: Context,
        plant: PlantKingdomEntry
    ): List<PlantMediaRecord> {
        val cleanId = plant.plantId.trim().lowercase()
        val exactRes = PlantPhotoRegistry.getExactRealPhotoResId(cleanId)

        return BotanicalPhotoStage.values().map { stage ->
            val overrideUri = AdminMediaReviewStore.getStageCustomUri(context, cleanId, stage)
            val overrideStatus = AdminMediaReviewStore.getStageStatusOverride(context, cleanId, stage)

            val bundledStageRes: Int? = when {
                overrideUri.isNotBlank() -> null
                stage == BotanicalPhotoStage.YOUNG_SAPLING && plant.primaryGroup == "FRUIT_PLANTS" ->
                    null // Avoid labelling a generic mango nursery tray as all other distinct species' verified photo; keep honest pending unless exact species photo
                stage == BotanicalPhotoStage.RIPE_FRUIT && exactRes != null -> exactRes
                stage == BotanicalPhotoStage.MATURE_BEARING_PLANT && exactRes != null -> exactRes
                exactRes != null && plant.primaryGroup != "FRUIT_PLANTS" && stage == BotanicalPhotoStage.YOUNG_SAPLING -> exactRes
                else -> null
            }

            val resolvedUri = when {
                overrideUri.isNotBlank() -> overrideUri
                stage == BotanicalPhotoStage.YOUNG_SAPLING && plant.customPhotoUri.isNotBlank() -> plant.customPhotoUri
                else -> ""
            }

            val hasRealPhoto = bundledStageRes != null || resolvedUri.isNotBlank()
            val computedStatus = overrideStatus ?: if (hasRealPhoto) {
                MediaVerificationStatus.VERIFIED
            } else {
                MediaVerificationStatus.IMAGE_PENDING_VERIFICATION
            }

            val sourceName = when {
                overrideUri.isNotBlank() -> "Krishak Sathi Admin Media Review"
                resolvedUri.isNotBlank() -> "Attached Verified Photo (${plant.sourceReference.ifBlank { "Nursery Directory" }})"
                bundledStageRes != null -> "Krishak Sathi Bundled Exact Botanical Drawable"
                else -> "Krishak Sathi Botanical Plate (${stage.enTitle})"
            }

            val attribution = when {
                hasRealPhoto -> "Bundled / Reviewed Media for ${plant.nameEn} (${stage.enTitle})"
                else -> "Photographic reference pending verification; showing species-specific botanical plate."
            }

            PlantMediaRecord(
                mediaId = "${cleanId}_${stage.code}",
                plantId = cleanId,
                stage = stage,
                drawableResId = bundledStageRes,
                customUri = resolvedUri,
                sourceName = sourceName,
                creator = if (hasRealPhoto) "Krishak Sathi Curated Asset" else "Botanical Vector Fallback",
                license = if (hasRealPhoto) "App-Owned / Verified Partner Media" else "In-App Procedural Spec",
                attribution = attribution,
                verificationStatus = computedStatus,
                verificationDate = if (computedStatus == MediaVerificationStatus.VERIFIED) plant.lastVerifiedDate else "Pending Review",
                isSellerStockPhoto = overrideUri.isNotBlank() || (stage == BotanicalPhotoStage.YOUNG_SAPLING && plant.customPhotoUri.isNotBlank())
            )
        }
    }

    private fun plainsAndTerai() = setOf(
        RegionalSuitabilityTag.NORTH_BENGAL_PLAINS,
        RegionalSuitabilityTag.TERAI_DOOARS,
        RegionalSuitabilityTag.SUBTROPICAL,
        RegionalSuitabilityTag.TROPICAL
    )

    private fun warmPlains() = setOf(
        RegionalSuitabilityTag.NORTH_BENGAL_PLAINS,
        RegionalSuitabilityTag.TROPICAL,
        RegionalSuitabilityTag.SUBTROPICAL
    )

    private fun v(
        id: String,
        parentId: String,
        en: String,
        bn: String,
        hi: String,
        specEn: String,
        specBn: String,
        specHi: String,
        seasEn: String,
        seasBn: String,
        seasHi: String,
        regions: Set<RegionalSuitabilityTag>
    ) = PlantVarietyRecord(
        varietyId = id,
        parentPlantId = parentId,
        nameEn = en,
        nameBn = bn,
        nameHi = hi,
        specialtyEn = specEn,
        specialtyBn = specBn,
        specialtyHi = specHi,
        seasonEn = seasEn,
        seasonBn = seasBn,
        seasonHi = seasHi,
        recommendedRegions = regions
    )
}

object CatalogMediaAuditEngine {

    fun runFullAudit(
        context: Context,
        plants: List<PlantKingdomEntry> = PlantKingdomCatalog.allPlants,
        crops: List<MasterCrop> = CropMasterDatabase.crops,
        machinery: List<MachineryProduct> = MachinerySeedCatalog.referenceCatalogProducts
    ): CatalogMediaAuditReport {
        var verifiedPlantStages = 0
        var pendingPlantStages = 0
        val pendingItems = mutableListOf<CatalogMediaAuditItem>()

        plants.forEach { plant ->
            val gallery = PlantVarietyAndMediaCatalog.getStageMediaGallery(context, plant)
            gallery.forEach { rec ->
                if (rec.verificationStatus == MediaVerificationStatus.VERIFIED) {
                    verifiedPlantStages++
                } else {
                    pendingPlantStages++
                    if (rec.stage == BotanicalPhotoStage.YOUNG_SAPLING) {
                        pendingItems.add(
                            CatalogMediaAuditItem(
                                recordType = "PLANT_SPECIES",
                                itemId = plant.plantId,
                                titleEn = plant.nameEn,
                                titleBn = plant.nameBn,
                                titleHi = plant.nameHi,
                                stageCode = rec.stage.code,
                                status = MediaVerificationStatus.IMAGE_PENDING_VERIFICATION,
                                provenanceNote = rec.attribution
                            )
                        )
                    }
                }
            }
        }

        val verifiedCrops = crops.count { it.imageResId != null && it.imageResId != 0 }
        val pendingCrops = crops.size - verifiedCrops
        crops.filter { it.imageResId == null || it.imageResId == 0 }.forEach { crop ->
            pendingItems.add(
                CatalogMediaAuditItem(
                    recordType = "MASTER_CROP",
                    itemId = crop.cropId,
                    titleEn = crop.nameEn,
                    titleBn = crop.nameBn,
                    titleHi = crop.nameHi,
                    stageCode = "PRIMARY_CARD",
                    status = MediaVerificationStatus.IMAGE_PENDING_VERIFICATION,
                    provenanceNote = "Crop card uses vector placeholder; verified photo pending review."
                )
            )
        }

        val verifiedMachinery = machinery.count { m ->
            m.photosCsv.split(",").any { it.isNotBlank() }
        }

        val allDashboardCards = DashboardCatalog.coreSections.flatMap { it.tiles } + DashboardCatalog.additionalToolCards
        val tilesMissingIcons = allDashboardCards.count { it.icon == null }

        return CatalogMediaAuditReport(
            totalPlantsAudited = plants.size,
            totalPlantStageSlots = plants.size * BotanicalPhotoStage.values().size,
            verifiedPlantMediaCount = verifiedPlantStages,
            pendingPlantMediaCount = pendingPlantStages,
            totalCropsAudited = crops.size,
            verifiedCropsMediaCount = verifiedCrops,
            pendingCropsMediaCount = pendingCrops,
            totalMachineryAudited = machinery.size,
            verifiedMachineryMediaCount = verifiedMachinery,
            totalDashboardTilesWithIcons = allDashboardCards.size - tilesMissingIcons,
            totalDashboardTilesMissingIcons = tilesMissingIcons,
            totalVarietiesCataloged = PlantVarietyAndMediaCatalog.allVarieties.size,
            pendingReviewItems = pendingItems
        )
    }

    /**
     * Quick check verifying bundled nursery sapling reference drawable exists.
     */
    @DrawableRes
    fun getNurseryShowcaseDrawable(): Int = R.drawable.img_nursery_saplings_tray_1795520198312

    @DrawableRes
    fun getHomeHeroDrawable(): Int = R.drawable.img_royal_agri_hero_1795520197823
}
