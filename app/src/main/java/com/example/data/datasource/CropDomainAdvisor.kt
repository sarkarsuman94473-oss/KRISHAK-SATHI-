package com.example.data.datasource

import com.example.data.model.AppLanguage
import com.example.data.model.CropCategory
import com.example.data.model.CropFaq
import com.example.data.model.CropMasterGroup
import com.example.data.model.CropRegion
import com.example.data.model.CropSeason
import com.example.data.model.MasterCrop

/**
 * Provides domain-specific, crop-type-appropriate and regionally aware trilingual guidance
 * across all 22 Master Library sections so Flowers show floriculture practices, Fruit Trees show
 * orchard practices, Indoor/Nursery plants show potting/propagation, and Green Manure/Trees show
 * agroforestry/incorporation guidance.
 */
object CropDomainAdvisor {

    fun resolveSeasons(crop: MasterCrop): List<CropSeason> {
        if (crop.seasons.isNotEmpty()) return crop.seasons
        val seasonText = "${crop.bestSeasonBn} ${crop.summaryEn}".lowercase()
        val detected = mutableSetOf<CropSeason>()

        if (crop.category in listOf(
                CropCategory.TEA,
                CropCategory.FRUITS,
                CropCategory.PLANTATION,
                CropCategory.AGROFORESTRY_OTHER
            ) && crop.harvestDays >= 180
        ) {
            detected.add(CropSeason.PERENNIAL)
        }
        if (crop.category == CropCategory.NURSERY_ORNAMENTAL) {
            detected.add(CropSeason.YEAR_ROUND)
        }
        if (seasonText.contains("সারা বছর") || seasonText.contains("বারোমাস") || seasonText.contains("year")) {
            detected.add(CropSeason.YEAR_ROUND)
        }
        if (seasonText.contains("জুন") || seasonText.contains("জুলাই") || seasonText.contains("আগস্ট") ||
            seasonText.contains("খরিফ") || seasonText.contains("বর্ষা") || seasonText.contains("আমন") ||
            seasonText.contains("kharif") || seasonText.contains("monsoon")
        ) {
            detected.add(CropSeason.KHARIF)
        }
        if (seasonText.contains("অক্টোবর") || seasonText.contains("নভেম্বর") || seasonText.contains("ডিসেম্বর") ||
            seasonText.contains("জানুয়ারি") || seasonText.contains("রবি") || seasonText.contains("শীত") ||
            seasonText.contains("বোরো") || seasonText.contains("rabi") || seasonText.contains("winter")
        ) {
            detected.add(CropSeason.RABI)
        }
        if (seasonText.contains("ফেব্রুয়ারি") || seasonText.contains("মার্চ") || seasonText.contains("এপ্রিল") ||
            seasonText.contains("মে") || seasonText.contains("গ্রীষ্ম") || seasonText.contains("বসন্ত") ||
            seasonText.contains("zaid") || seasonText.contains("summer") || seasonText.contains("আউশ")
        ) {
            detected.add(CropSeason.ZAID)
        }
        if (detected.isEmpty()) {
            detected.add(CropSeason.RABI)
        }
        return detected.toList()
    }

    fun resolveRegions(crop: MasterCrop): List<CropRegion> {
        if (crop.regions.isNotEmpty()) return crop.regions
        val northBengalSpecialties = setOf(
            "tea", "pineapple", "anarosh", "komlalebu", "cardamom", "turmeric", "ginger",
            "bay_leaf", "golmorich", "arecanut", "lichu", "jute", "rice_paddy", "potato",
            "tomato", "cabbage", "cauliflower", "mustard", "orchid", "marigold", "rose",
            "dragon_fruit", "kiwi", "strawberry", "mushroom", "rubber", "citronella"
        )
        val wbCoreCrops = setOf(
            "coconut", "betel_leaf", "mango", "banana", "guava", "papaya", "potol", "jhinge",
            "lal_shak", "palak", "pui_shak", "kalmi_shak", "brinjal", "okra", "lentil",
            "moong", "sesame", "groundnut", "tuberose", "lotus", "hibiscus"
        )
        val dryOrTemperateSpecialist = setOf(
            "apple", "pear", "peach", "plum", "cumin", "jire", "bajra", "moth_bean", "cotton",
            "safflower", "dates"
        )
        return when {
            crop.cropId in dryOrTemperateSpecialist -> listOf(
                CropRegion.OTHER_STATES_INDIA,
                CropRegion.PAN_INDIA
            )
            crop.cropId in northBengalSpecialties || crop.category in listOf(
                CropCategory.LEAFY_VEG, CropCategory.COLE_CROPS, CropCategory.FLOWERS,
                CropCategory.NURSERY_ORNAMENTAL
            ) -> listOf(
                CropRegion.SILIGURI_NORTH_BENGAL,
                CropRegion.WEST_BENGAL,
                CropRegion.OTHER_STATES_INDIA,
                CropRegion.PAN_INDIA
            )
            crop.cropId in wbCoreCrops -> listOf(
                CropRegion.WEST_BENGAL,
                CropRegion.SILIGURI_NORTH_BENGAL,
                CropRegion.OTHER_STATES_INDIA,
                CropRegion.PAN_INDIA
            )
            else -> listOf(
                CropRegion.SILIGURI_NORTH_BENGAL,
                CropRegion.WEST_BENGAL,
                CropRegion.PAN_INDIA
            )
        }
    }

    private data class VarietyInfo(val bn: String, val hi: String, val en: String)

    private val specificVarieties: Map<String, VarietyInfo> = mapOf(
        "rice_paddy" to VarietyInfo(
            "স্বর্ণ (MTU-7029), শতাব্দী (IET-4786), কালোনুনিয়া ও তুলাইপাঞ্জি (উত্তরবঙ্গের সুগন্ধি জাত), আইআর-৬৪",
            "स्वर्णा (MTU-7029), शताब्दी (IET-4786), कालूनुनिया व तुलाईपंजी (उत्तर बंगाल सुगंधित), IR-64",
            "Swarna (MTU-7029), Shatabdi (IET-4786), Kalonunia & Tulaipanji (North Bengal aromatic), IR-64"
        ),
        "wheat" to VarietyInfo(
            "এইচডি-২৯৬৭, ডিবিডব্লিউ-১৮৭ (করণ বন্দনা), ইউপি-২৬২ ও পশ্চিমবঙ্গ/উত্তরবঙ্গের জন্য কে-৩০৭",
            "HD-2967, DBW-187 (करण वंदना), UP-262 एवं पूर्वी भारत के लिए K-307",
            "HD-2967, DBW-187 (Karan Vandana), UP-262, K-307 (Eastern Gangetic & North Bengal plains)"
        ),
        "potato" to VarietyInfo(
            "কুফরি জ্যোতি (উত্তরবঙ্গে সর্বাধিক জনপ্রিয়), কুফরি পোখরাজ, কুফরি চন্দ্রমুখী, কুফরি হিমালিনী",
            "कुफरी ज्योति (उत्तर बंगाल में लोकप्रिय), कुफरी पुखराज, कुफरी चंद्रमुखी, कुफरी हिमालिनी",
            "Kufri Jyoti (widely grown in North Bengal), Kufri Pukhraj, Kufri Chandramukhi, Kufri Himalini"
        ),
        "tomato" to VarietyInfo(
            "অর্কা রক্ষক, অর্কা সম্রাট, পূসা রুবি, অভিনাশ-২, সাহো-৩২৫১",
            "अर्का रक्षक, अर्का सम्राट, पूसा रूबी, अविनाश-2, साहो-3251",
            "Arka Rakshak (triple disease resistant), Arka Samrat, Pusa Ruby, Saho-3251"
        ),
        "tea" to VarietyInfo(
            "টিভি-১, টিভি-৯, টিভি-২২, টিভি-২৩, টিভি-২৫ ও টিভি-২৬ (টোকলাই ক্লোন), দার্জিলিং পাহাড়ে বি-১৫৭",
            "TV-1, TV-9, TV-22, TV-23, TV-25, TV-26 (टोकलाई क्लोन), दार्जिलिंग पहाड़ियों हेतु B-157",
            "Tocklai clones TV-1, TV-9, TV-22, TV-23, TV-25, TV-26; Darjeeling hills AV-2 & B-157"
        ),
        "mango" to VarietyInfo(
            "আম্রপালি, মল্লিকা, হিমসাগর, ল্যাংড়া, ফজলি ও মালদা-উত্তরবঙ্গের লক্ষ্মণভোগ",
            "आम्रपाली, मल्लिका, हिमसागर, लंगड़ा, फजली एवं मालदा-बंगाल की लक्ष्मणभोग",
            "Amrapali, Mallika, Himsagar, Langra, Fazli, and Lakshmanbhog (Bengal GI)"
        ),
        "banana" to VarietyInfo(
            "টিস্যু কালচার জি-৯ (গ্র্যান্ড নাইন), মর্তমান, কাঁঠালি ও চম্পা (উত্তরবঙ্গে জনপ্রিয়)",
            "टिश्यू कल्चर G-9 (ग्रैंड नैन), मर्तबान, कंठाली व चंपा",
            "Tissue-culture Grand Naine (G-9), Martaman, Kanthali, Champa"
        ),
        "anarosh" to VarietyInfo(
            "জায়ান্ট কিউ (বিধাননগর-শিলিগুড়ির প্রধান বাণিজ্যিক জাত), কুইন (ত্রিপুরা-উত্তরপূর্ব), মরিশাস",
            "जायंट क्यू (सिलीगुड़ी-बिधाननगर की प्रमुख किस्म), क्वीन, मॉरीशस",
            "Giant Kew (primary Bidhannagar/Siliguri commercial cultivar), Queen, Mauritius"
        ),
        "turmeric" to VarietyInfo(
            "প্রতিভা, রোমা, সুরঞ্জনা ও উত্তরবঙ্গের স্থানীয় সুগন্ধি হলুদ",
            "प्रतिभा, रोमा, सुरंजना तथा उत्तर बंगाल की उच्च करक्यूमिन किस्में",
            "IISR Prathibha, Roma, Suranjana, and North Bengal Terai local selections"
        ),
        "ginger" to VarietyInfo(
            "গুরুবাথান (দার্জিলিং-কালিম্পং), নদিয়া, আইআইএসআর বরদা ও মহিমা",
            "गोरुबथान (दार्जिलिंग-कालिम्पोंग), नदिया, IISR वरदा व महिमा",
            "Gorubathan (Darjeeling-Kalimpong hills), Nadia, IISR Varada, Mahima"
        ),
        "cardamom" to VarietyInfo(
            "রামসাই, সাওয়ানি, গোলসাই, ভরলাঙ্গে (দার্জিলিং ও কালিম্পং পাহাড়ের বড় এলাচ); দক্ষিণ ভারতে আইআইএসআর সবর্ণা (ছোট এলাচ)",
            "रामसाई, सावनी, गोलसाई, भरलांगे (दार्जिलिंग-कालिम्पोंग बड़ी इलायची); दक्षिण भारत हेतु IISR सुवर्णा",
            "Ramsai, Sawney, Golsey, Bharlangey (Large Cardamom for Darjeeling-Kalimpong); IISR Suvarna (Small Cardamom)"
        ),
        "mustard" to VarietyInfo(
            "বি-৯ (বিনয়), ঝুমকা, পুসা বোল্ড, আরএইচ-৭৪৯ ও উত্তরবঙ্গের হলুদ সরষে",
            "B-9 (बिनय), झुमका, पूसा बोल्ड, RH-749 एवं पीली सरसों",
            "B-9 (Binoy), Jhumka, Pusa Bold, RH-749, and Yellow Sarson selections"
        ),
        "rose" to VarietyInfo(
            "কাট ফ্লাওয়ার: ফার্স্ট রেড, তাজমহল, গ্র্যান্ড গালা, নোবেলেস; খোলা জমিতে এডওয়ার্ড ও রানি এলিজাবেথ",
            "कट फ्लावर: फर्स्ट रेड, ताजमहल, ग्रैंड गाला, नोबलेस; खुले खेत में एडवर्ड व क्वीन एलिजाबेथ",
            "Cut flowers: First Red, Tajmahal, Grand Gala, Noblesse; Open field: Edward, Queen Elizabeth"
        ),
        "marigold" to VarietyInfo(
            "আফ্রিকান গাঁদা: পূসা নারঙ্গি গেন্দা, পূসা বাসন্তী, ইনকা অরেঞ্জ; ফরাসি গাঁদা ও রক্ত গাঁদা",
            "अफ्रीकन गेंदा: पूसा नारंगी गेंदा, पूसा बसंती, इंका ऑरेंज; फ्रेंच मेरीगोल्ड",
            "African Marigold: Pusa Narangi Gainda, Pusa Basanti, Inca Orange; French Marigold"
        ),
        "tuberose" to VarietyInfo(
            "প্রজ্বল (একক পাঁপড়ি), সুহাসিনী ও বৈভব (ডাবল পাঁপড়ি), শৃঙ্গার (পশ্চিমবঙ্গে সর্বাধিক চাষ)",
            "प्रज्वल (सिंगल), सुहासिनी व वैभव (डबल), श्रृंगार (बंगाल में प्रमुख)",
            "Prajwal (Single), Suhasini & Vaibhav (Double), Shringar (popular in Bengal)"
        ),
        "orchid" to VarietyInfo(
            "সিম্বিডিয়াম (কালিম্পং-দার্জিলিং পাহাড়), ডেন্ড্রোবিয়াম ও ভ্যান্ডা (শিলিগুড়ি ও সমতলের উষ্ণ-আর্দ্র নার্সারি)",
            "सिम्बिडियम (कालिम्पोंग-दार्जिलिंग पहाड़ियां), डेंड्रोबियम व वांडा (मैदानी व तराई नर्सरी)",
            "Cymbidium (Kalimpong-Darjeeling hills); Dendrobium & Vanda (Siliguri & humid plains)"
        )
    )

    fun getVarieties(crop: MasterCrop, lang: AppLanguage): String {
        val explicit = when (lang) {
            AppLanguage.BENGALI -> crop.varietiesBn
            AppLanguage.HINDI -> crop.varietiesHi
            AppLanguage.ENGLISH -> crop.varietiesEn
        }
        if (explicit.isNotBlank()) return explicit
        specificVarieties[crop.cropId]?.let { v ->
            return when (lang) {
                AppLanguage.BENGALI -> v.bn
                AppLanguage.HINDI -> v.hi
                AppLanguage.ENGLISH -> v.en
            }
        }
        return when (lang) {
            AppLanguage.BENGALI -> when (crop.getMasterGroup()) {
                CropMasterGroup.FLOWERS -> "উন্নত বাণিজ্যিক হাইব্রিড ও স্থানীয় সুগন্ধি জাত — নার্সারি বা KVK থেকে শংসিত চারা/কন্দ নিন"
                CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> "ভ্যারিগেটেড, ডোয়ার্ফ ও রোগমুক্ত গ্রাফটেড / টিস্যু কালচার জাত"
                CropMasterGroup.OTHER_AGROFORESTRY -> "দ্রুত বর্ধনশীল শংসিত ক্লোন/হাইব্রিড ও অধিক জৈব ভরসম্পন্ন স্থানীয় জাত"
                else -> "আইসিএআর (ICAR), বিসিকেভি/ইউবিকেভি (UBKV) ও রাজ্য কৃষি দপ্তর অনুমোদিত স্থানীয় ও উচ্চফলনশীল জাত"
            }
            AppLanguage.HINDI -> when (crop.getMasterGroup()) {
                CropMasterGroup.FLOWERS -> "उन्नत व्यावसायिक संकर व स्थानीय सुगंधित किस्में — प्रमाणित नर्सरी या KVK से पौध लें"
                CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> "रंगीन पत्तियों वाली, बौनी (ड्वार्फ) व रोगमुक्त कलमी/टिश्यू कल्चर किस्में"
                CropMasterGroup.OTHER_AGROFORESTRY -> "तेजी से बढ़ने वाले प्रमाणित क्लोन व अधिक बायोमास देने वाली किस्में"
                else -> "ICAR, राज्य कृषि विश्वविद्यालय व KVK द्वारा अनुशंसित स्थानीय एवं अधिक उपज देने वाली किस्में"
            }
            AppLanguage.ENGLISH -> when (crop.getMasterGroup()) {
                CropMasterGroup.FLOWERS -> "Commercial hybrid and fragrant local cultivars — source certified planting material from KVK or verified nurseries"
                CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> "Variegated, compact dwarf, and disease-free grafted or tissue-culture cultivars"
                CropMasterGroup.OTHER_AGROFORESTRY -> "Fast-growing certified clones and high-biomass regional selections"
                else -> "ICAR, State Agricultural University (e.g., UBKV/BCKV), and KVK recommended high-yielding and regional varieties"
            }
        }
    }

    fun getRegionalSuitability(crop: MasterCrop, lang: AppLanguage): String {
        val regions = resolveRegions(crop)
        val regionLabels = regions.joinToString(" • ") { it.getDisplayName(lang) }
        val localNote = when (lang) {
            AppLanguage.BENGALI -> when {
                CropRegion.SILIGURI_NORTH_BENGAL in regions ->
                    "শিলিগুড়ি ও উত্তরবঙ্গের তরাই-ডুয়ার্স (উষ্ণ-আর্দ্র ও হালকা অম্লীয় মাটি) এবং সংলগ্ন অঞ্চলের জন্য বিশেষভাবে উপযোগী। স্থানীয় মাটির pH ও বর্ষার জল নিকাশী অনুযায়ী সময়সামঞ্জস্য করুন।"
                CropRegion.WEST_BENGAL in regions ->
                    "পশ্চিমবঙ্গের গাঙ্গেয় পলি ও লাল-কাঁকুরে অঞ্চলে চাষযোগ্য। স্থানীয় আবহাওয়া অনুযায়ী রোপণের তারিখ ঠিক করুন।"
                else ->
                    "প্রধানত ভারতের অন্যান্য উপযুক্ত কৃষি-জলবায়ু অঞ্চলে (শীতল পাহাড় বা শুষ্ক/উপ-শুষ্ক) সফলভাবে চাষ হয়; উত্তরবঙ্গে পরীক্ষামূলক চাষে KVK-এর পরামর্শ নিন।"
            }
            AppLanguage.HINDI -> when {
                CropRegion.SILIGURI_NORTH_BENGAL in regions ->
                    "सिलीगुड़ी व उत्तर बंगाल के तराई-डूआर्स (नमी व हल्की अम्लीय मिट्टी) और पूर्वी भारत के लिए विशेष रूप से उपयुक्त। स्थानीय मिट्टी जांच व मौसम के अनुसार बुवाई तय करें।"
                CropRegion.WEST_BENGAL in regions ->
                    "पश्चिम बंगाल के जलोढ़ और लाल मिट्टी क्षेत्रों में सफलतापूर्वक उगाया जाता है।"
                else ->
                    "मुख्य रूप से भारत के अन्य उपयुक्त कृषि-जलवायु क्षेत्रों (शीत पहाड़ी या शुष्क क्षेत्रों) में उगाया जाता है; स्थानीय परीक्षण से पहले KVK सलाह लें।"
            }
            AppLanguage.ENGLISH -> when {
                CropRegion.SILIGURI_NORTH_BENGAL in regions ->
                    "Well suited to Siliguri & North Bengal Terai-Dooars agro-climatic zones (humid, slightly acidic alluvial soils) as well as broader East/Pan-India belts. Verify local sowing windows with your nearest KVK."
                CropRegion.WEST_BENGAL in regions ->
                    "Well adapted across West Bengal alluvial and lateritic belts; adjust drainage before monsoon planting."
                else ->
                    "Primarily suited to other specialized Indian agro-climatic zones (temperate hills or arid belts). Consult local KVK before commercial trials in North Bengal."
            }
        }
        return "$regionLabels\n$localNote"
    }

    fun getGrowthStagesSummary(crop: MasterCrop, lang: AppLanguage): String {
        val explicit = when (lang) {
            AppLanguage.BENGALI -> crop.growthStagesBn
            AppLanguage.HINDI -> crop.growthStagesHi
            AppLanguage.ENGLISH -> crop.growthStagesEn
        }
        if (explicit.isNotBlank()) return explicit
        if (crop.steps.isNotEmpty()) {
            return crop.steps.joinToString("\n") {
                val dayLbl = when (lang) {
                    AppLanguage.BENGALI -> "দিন ${it.dayNumber}"
                    AppLanguage.HINDI -> "दिन ${it.dayNumber}"
                    AppLanguage.ENGLISH -> "Day ${it.dayNumber}"
                }
                "• $dayLbl: ${it.getTitle(lang)}"
            }
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> "১) অঙ্কুরোদগম / কলম রোপণ → ২) মূল বিস্তার → ৩) পিঞ্চিং ও শাখা বৃদ্ধি → ৪) কুঁড়ি গঠন ও ডিসবাডিং → ৫) পূর্ণ প্রস্ফুটন ও ফুল সংগ্রহ"
                AppLanguage.HINDI -> "1) रोपाई/अंकुरण → 2) जड़ विकास → 3) पिंचिंग व शाखा फैलाव → 4) कली निर्माण → 5) पूर्ण पुष्पन व तुड़ाई"
                AppLanguage.ENGLISH -> "1) Establishment / Sprouting → 2) Vegetative Pinching → 3) Bud Initiation & Disbudding → 4) Bloom Opening → 5) Harvest & Conditioning"
            }
            CropMasterGroup.FRUITS -> when (lang) {
                AppLanguage.BENGALI -> "১) চারা রোপণ ও শিকড় স্থাপন → ২) অঙ্গজ বৃদ্ধি ও ক্যানোপি ট্রেনিং → ৩) সুপ্ত অবস্থা ও মুকুল/ফুল → ৪) ফল বাঁধা ও বৃদ্ধি → ৫) পরিপক্কতা ও সংগ্রহ"
                AppLanguage.HINDI -> "1) पौध रोपाई व जड़ जमाव → 2) वानस्पतिक बढ़वार व कटाई-छंटाई → 3) पुष्प व बौर → 4) फल विकास → 5) परिपक्वता व तुड़ाई"
                AppLanguage.ENGLISH -> "1) Sapling Establishment → 2) Canopy Training & Pruning → 3) Flowering Induction → 4) Fruit Set & Sizing → 5) Maturity & Picking"
            }
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "১) মিডিয়া প্রস্তুতি ও রোপণ → ২) পরোক্ষ আলোয় শিকড় গঠন → ৩) নতুন পাতা ও ডাল ছাঁটাই → ৪) প্রতি বছর পট পরিবর্তন ও হার্ডেনিং"
                AppLanguage.HINDI -> "1) पॉटिंग मिक्स तैयारी व रोपाई → 2) हल्की छाया में जड़ विकास → 3) नई पत्तियां व छंटाई → 4) रिपोर्टिंग व हार्डनिंग"
                AppLanguage.ENGLISH -> "1) Potting Mix & Planting → 2) Root Establishment → 3) Foliage Flush & Shaping → 4) Hardening / Annual Repotting"
            }
            CropMasterGroup.OTHER_AGROFORESTRY -> when (lang) {
                AppLanguage.BENGALI -> "১) গজানো ও প্রাথমিক বৃদ্ধি → ২) দ্রুত জৈব ভর তৈরি → ৩) (সবুজ সার হলে ফুলের আগে মাটিতে মেশানো / পশুখাদ্য বা বৃক্ষ হলে ছাঁটাই ও প্রথম কাটা)"
                AppLanguage.HINDI -> "1) अंकुरण व प्रारंभिक विकास → 2) तीव्र बायोमास वृद्धि → 3) (हरी खाद: फूल आने से पहले मिट्टी में मिलाना / चारा-वृक्ष: नियमित कटाई व छंटाई)"
                AppLanguage.ENGLISH -> "1) Germination & Establishment → 2) Rapid Biomass Flush → 3) Pre-flower Incorporation (Green Manure) or Scheduled Cutting / Pruning"
            }
            else -> when (lang) {
                AppLanguage.BENGALI -> "১) অঙ্কুরোদগম (১-১০ দিন) → ২) অঙ্গজ বৃদ্ধি ও কুশি/ডাল (১৫-৩৫ দিন) → ৩) ফুল ও পরাগায়ন (৪০-৫৫ দিন) → ৪) দানা/ফল পুষ্ট হওয়া → ৫) পরিপক্কতা ও সংগ্রহ (${crop.harvestDays} দিন)"
                AppLanguage.HINDI -> "1) अंकुरण (1–10 दिन) → 2) वानस्पतिक वृद्धि (15–35 दिन) → 3) पुष्पन (40–55 दिन) → 4) फल/दाना विकास → 5) परिपक्वता व कटाई (${crop.harvestDays} दिन)"
                AppLanguage.ENGLISH -> "1) Germination (Days 1–10) → 2) Vegetative Flush (Days 15–35) → 3) Flowering (Days 40–55) → 4) Pod/Grain/Fruit Fill → 5) Harvest Maturity (~${crop.harvestDays} days)"
            }
        }
    }

    fun getHarvestMethod(crop: MasterCrop, lang: AppLanguage): String {
        val explicit = when (lang) {
            AppLanguage.BENGALI -> crop.harvestMethodBn
            AppLanguage.HINDI -> crop.harvestMethodHi
            AppLanguage.ENGLISH -> crop.harvestMethodEn
        }
        if (explicit.isNotBlank()) return explicit
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> "ভোরবেলা বা পড়ন্ত বিকেলে কুঁড়ি অর্ধ-ফোটা (Tight-bud / Half-open) অবস্থায় ধারালো সেকাটিউর দিয়ে তির্যকভাবে ডাঁটা কেটে সঙ্গে সঙ্গে পরিষ্কার জলে রাখুন।"
                AppLanguage.HINDI -> "सुबह या शाम को कली की सही अवस्था (आधी खिली/टाइट बड) में तेज कटर से तिरछा काटकर तुरंत साफ पानी में रखें।"
                AppLanguage.ENGLISH -> "Harvest in cool morning/evening hours at proper tight-bud or half-open stage using clean secateurs; immerse stems in clean water immediately."
            }
            CropMasterGroup.FRUITS -> when (lang) {
                AppLanguage.BENGALI -> "ফলের প্রজাতি অনুযায়ী সঠিক রঙ ও চিনি-অম্ল অনুপাত (TSS) এলে বোঁটাসহ সাবধানে পেড়ে ছায়ায় গ্রেডিং ও ক্যারেট প্যাকিং করুন।"
                AppLanguage.HINDI -> "फल के सही रंग, सुगंध व परिपक्वता आने पर डंठल सहित सावधानी से तोड़ें, छाया में ग्रेडिंग कर क्रेट में पैक करें।"
                AppLanguage.ENGLISH -> "Pick carefully with pedicel attached at optimal physiological maturity and color break; sort in shade and pack in ventilated crates."
            }
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "নার্সারি চারা বা ইনডোর পট গাছে শিকড় সুদৃঢ় হলে ও নতুন ২-৩টি পাতা ছাড়ার পর হার্ডেনিং করে সরবরাহ বা প্রতিস্থাপন করুন।"
                AppLanguage.HINDI -> "नर्सरी पौधे या इंडोर गमले में जड़ें मजबूत होने व हार्डनिंग के बाद रोपाई या बिक्री के लिए तैयार करें।"
                AppLanguage.ENGLISH -> "Prepare ornamental pots or nursery saplings for sale or field transplanting after healthy root ball formation and 7–10 days of hardening."
            }
            CropMasterGroup.OTHER_AGROFORESTRY -> when (lang) {
                AppLanguage.BENGALI -> "সবুজ সার হলে ৪০-৪৫ দিনে ফুল আসার আগে লাঙল/রোটাভেটরে মাটিতে মিশিয়ে দিন। পশুখাদ্য হলে নির্দিষ্ট ব্যবধানে মাটির ৫-৮ সেমি ওপর থেকে কাটুন।"
                AppLanguage.HINDI -> "हरी खाद को 40–45 दिन पर फूल आने से पहले खेत में पलट दें। चारा फसल को निर्धारित अंतराल पर जमीन से 5–8 सेमी ऊपर से काटें।"
                AppLanguage.ENGLISH -> "Incorporate green manure crops before flowering (40–45 DAS) with rotavator/plough; harvest fodder in planned cuts 5–8 cm above ground."
            }
            else -> when (lang) {
                AppLanguage.BENGALI -> "${crop.harvestDays} দিনের কাছাকাছি ফসল পরিপক্ক হলে শুষ্ক আবহাওয়ায় কচি/নিরোগ অবস্থায় পরিচ্ছন্নভাবে সংগ্রহ ও বাছাই করুন।"
                AppLanguage.HINDI -> "लगभग ${crop.harvestDays} दिनों में परिपक्व होने पर शुष्क मौसम में स्वच्छ तरीके से कटाई व छंटाई करें।"
                AppLanguage.ENGLISH -> "Harvest during dry weather around ${crop.harvestDays} days at market maturity, followed by clean sorting and grading."
            }
        }
    }

    fun getCultivationCostInfo(crop: MasterCrop, lang: AppLanguage): String {
        val explicit = when (lang) {
            AppLanguage.BENGALI -> crop.estimatedCostBn
            AppLanguage.HINDI -> crop.estimatedCostHi
            AppLanguage.ENGLISH -> crop.estimatedCostEn
        }
        if (explicit.isNotBlank()) return explicit
        return when (crop.getMasterGroup()) {
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "পাত্র, কোকোপিট মিডিয়া ও শেডনেটের মানের ওপর খরচ নির্ভর করে (আনুমানিক ৫০-১৫০ টাকা প্রতি টব/গ্রো-ব্যাগ বা নर्सারি স্কেল অনুযায়ী)। চূড়ান্ত খরচ ও বাজারদর স্থানীয় নার্সারি ও KVK থেকে যাচাই করুন।"
                AppLanguage.HINDI -> "लागत गमले, कोकोपीट मिश्रण व शेडनेट की गुणवत्ता पर निर्भर करती है। स्थानीय नर्सरी बाजार और KVK से सटीक आकलन अवश्य लें।"
                AppLanguage.ENGLISH -> "Costs vary substantially with pots, cocopeat potting media, poly-tunnel structure, and scale. Confirm local input and market prices with nearby nurseries or KVK."
            }
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> "আনুমানিক বিঘা প্রতি খরচ খোলা জমিতে প্রায় ১৮,০০০–৩৫,০০০ টাকা (পলিহাউস ও বাল্ব/টিস্যু জাতের ক্ষেত্রে বেশি হতে পারে)। উৎসবের মরসুমে ভালো লাভ মেলে; স্থানীয় বাজারদর ও কৃষি দপ্তর থেকে যাচাই করুন।"
                AppLanguage.HINDI -> "खुले खेत में अनुमानित लागत लगभग ₹18,000–₹35,000 प्रति बीघा (पॉलीहाउस या बल्ब किस्मों में अधिक)। सटीक अर्थशास्त्र हेतु स्थानीय KVK व मंडी से पुष्टि करें।"
                AppLanguage.ENGLISH -> "Approximate open-field cost ranges ~₹18,000–₹35,000 per bigha (higher for polyhouse/bulbous ornamentals). Confirm real-time input costs and mandi demand locally."
            }
            CropMasterGroup.FRUITS, CropMasterGroup.PLANTATION_COMMERCIAL -> when (lang) {
                AppLanguage.BENGALI -> "প্রথম বছর চারা, গর্ত ও বেড়া/সেচের প্রাথমিক খরচ আনুমানিক ২০,০০০–৪৫,০০০ টাকা/বিঘা; পরবর্তী বছরে পরিচর্যা খরচ কমে এবং স্থায়ী ফলন থেকে আয় বাড়ে। স্থানীয় উদ্যানপালন দপ্তরের পরামর্শ নিন।"
                AppLanguage.HINDI -> "प्रथम वर्ष पौध, गड्ढा व सिंचाई स्थापना लागत लगभग ₹20,000–₹45,000 प्रति बीघा; अगले वर्षों में रखरखाव कम व लाभ बढ़ता है। उद्यान विभाग से सटीक योजना लें।"
                AppLanguage.ENGLISH -> "Initial establishment (saplings, pits, irrigation) averages ~₹20,000–₹45,000 per bigha with lower maintenance in fruiting years. Confirm horticulture subsidies and rates locally."
            }
            else -> when (lang) {
                AppLanguage.BENGALI -> "জমি চাষ, বীজ, সার ও শ্রমিক সহ আনুমানিক বিঘা প্রতি খরচ ৮,০০০–২০,০০০ টাকা হতে পারে (ফসল ও অঞ্চলভেদে ভিন্ন)। চূড়ান্ত হিসাব ও ন্যায্য দামের জন্য স্থানীয় কৃষি দপ্তর/KVK যাচাই করুন।"
                AppLanguage.HINDI -> "जुताई, बीज, खाद व श्रम सहित अनुमानित लागत लगभग ₹8,000–₹20,000 प्रति बीघा हो सकती है (क्षेत्र अनुसार भिन्न)। सटीक आकलन हेतु स्थानीय कृषि विशेषज्ञ से पुष्टि करें।"
                AppLanguage.ENGLISH -> "Approximate input cost ranges ~₹8,000–₹20,000 per bigha depending on seeds, irrigation, and labor. Confirm exact local rates and profitability with your Block Agriculture Office or KVK."
            }
        }
    }

    fun getExpertTips(crop: MasterCrop, lang: AppLanguage): String {
        val explicit = when (lang) {
            AppLanguage.BENGALI -> crop.expertTipsBn
            AppLanguage.HINDI -> crop.expertTipsHi
            AppLanguage.ENGLISH -> crop.expertTipsEn
        }
        if (explicit.isNotBlank()) return explicit
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> "• নিয়মিত পিঞ্চিং (মাথা ভেঙে দেওয়া) করলে বেশি শাখা ও অধিক ফুল মেলে।\n• রোগমুক্ত বাল্ব/কাটিং ব্যবহার করুন এবং ফসলের অবশিষ্টাংশ পুড়িয়ে বা কম্পোস্ট করে ক্ষেত পরিষ্কার রাখুন।\n• জৈব ছত্রাকনাশক ও রাসায়নিক স্প্রে করার আগে স্থানীয় উদ্যানপালন বিশেষজ্ঞের সঙ্গে ডোজ নিশ্চিত করুন।"
                AppLanguage.HINDI -> "• समय पर पिंचिंग (शीर्ष कलिका तोड़ना) से अधिक शाखाएं और फूल आते हैं।\n• रोगमुक्त कंद/कलम चुनें और खेत में जल निकासी रखें।\n• किसी भी रासायनिक छिड़काव से पहले स्थानीय उद्यान विशेषज्ञ/KVK से खुराक की पुष्टि अवश्य करें।"
                AppLanguage.ENGLISH -> "• Perform timely pinching to stimulate lateral flowering branches and higher stem counts.\n• Use disease-free bulbs/cuttings and keep beds well-drained to prevent root/collar rot.\n• Verify any cultivar-specific spray dosage with your local Horticulture Officer or KVK."
            }
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "• ইনডোর ও নার্সারি গাছে অতিরিক্ত জল দেবেন না; উপরের ২ ইঞ্চি মাটি শুকালে তবেই জল দিন।\n• টবের নিচে অবশ্যই জল নিকাশী ছিদ্র রাখুন এবং মাসে একবার হালকা ভার্মিকম্পোস্ট দিন।\n• নতুন গ্রাফটেড চারা সরাসরি কড়া রোদে না রেখে ধাপে ধাপে হার্ডেনিং করুন।"
                AppLanguage.HINDI -> "• इंडोर और गमले वाले पौधों में अधिक पानी न डालें; ऊपरी 2 इंच मिट्टी सूखने पर ही सिंचाई करें।\n• गमले में जल निकासी छेद रखें और महीने में एक बार केंचुआ खाद दें।"
                AppLanguage.ENGLISH -> "• Avoid overwatering potted plants—water only when the top 2 inches of mix feel dry.\n• Always ensure drainage holes at pot bases and feed monthly with light vermicompost.\n• Gradually harden grafted or tissue-culture saplings before full-sun field planting."
            }
            CropMasterGroup.FRUITS -> when (lang) {
                AppLanguage.BENGALI -> "• ফসল তোলার পরপরই মরা ও রোগা ডাল ছাঁটাই (Prune) করে বোর্দো পেস্ট লাগান।\n• মাছি পোকা দমনে প্রতি একরে ৮-১০টি মিথাইল ইউজিনল ফেরোমোন ফাঁদ ও ফলের ব্যাগিং ব্যবহার করুন।\n• রাসায়নিক প্রয়োগের আগে KVK বিশেষজ্ঞের পরামর্শ ও অপেক্ষাকাল (PHI) মেনে চলুন।"
                AppLanguage.HINDI -> "• फल तुड़ाई के तुरंत बाद सूखी व रोगग्रस्त टहनियों की छंटाई कर बोर्डो पेस्ट लगाएं।\n• फल मक्खी नियंत्रण हेतु फेरोमोन ट्रैप और फलों की बैगिंग अपनाएं।"
                AppLanguage.ENGLISH -> "• Prune dead/diseased wood immediately after harvest and seal cuts with Bordeaux paste.\n• Use fruit fly pheromone traps and pre-harvest fruit bagging to minimize pesticide reliance.\n• Always confirm spray timing and pre-harvest intervals (PHI) with local agricultural officers."
            }
            else -> when (lang) {
                AppLanguage.BENGALI -> "• বপনের আগে মাটি পরীক্ষা (Soil Health Card) করিয়ে সুষম সার দিন।\n• হলুদ আঠালো ফাঁদ ও ফেরোমোন ফাঁদ দিয়ে পোকা প্রাথমিক অবস্থায় শনাক্ত করুন।\n• যেকোনো রোগ বা রাসায়নিক স্প্রের চূড়ান্ত মাত্রা এলাকার কৃষি আধিকারিক (ADA/KVK)-এর কাছ থেকে যাচাই করে নিন।"
                AppLanguage.HINDI -> "• बुवाई से पहले मृदा स्वास्थ्य कार्ड के अनुसार संतुलित खाद डालें।\n• पीले चिपचिपे ट्रैप व फेरोमोन ट्रैप से कीटों की शुरुआती पहचान करें।\n• किसी भी कीटनाशक/फफूंदनाशक का उपयोग करने से पहले स्थानीय KVK या कृषि अधिकारी से सलाह लें।"
                AppLanguage.ENGLISH -> "• Base fertilizer doses on a Soil Health Card test for higher efficiency and lower cost.\n• Deploy yellow sticky traps and pheromone lures early to reduce chemical sprays.\n• Always confirm local crop protection doses and seasonal advisories with your nearest KVK or Agriculture Officer."
            }
        }
    }

    fun getFaqs(crop: MasterCrop): List<CropFaq> {
        if (crop.faqs.isNotEmpty()) return crop.faqs
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> listOf(
                CropFaq(
                    "ফুল দীর্ঘদিন তাজা রাখতে ও গাছের আয়ু বাড়াতে কী করা উচিত?",
                    "फूलों को लंबे समय तक ताजा रखने और अधिक कलियां पाने के लिए क्या करें?",
                    "How do I maximize flower bud count and vase life?",
                    "ফুল আসার আগে অতিরিক্ত ইউরিয়া বন্ধ করে পটাশ ও বোরণ ব্যবহার করুন, নিয়মিত শুকনো ফুল কেটে (Deadheading) দিন এবং ভোরবেলা ফসল তুলুন।",
                    "कली आते समय अधिक यूरिया बंद कर पोटाश व बोरॉन दें, सूखे फूल काटते रहें और सुबह कटाई करें।",
                    "Avoid excess nitrogen once buds form, use balanced potash/boron, deadhead spent blooms, and harvest in the cool morning."
                ),
                CropFaq(
                    "এই ফুল কি শিলিগুড়ি ও উত্তরবঙ্গে চাষ করা সম্ভব?",
                    "क्या यह फूल सिलीगुड़ी और उत्तर बंगाल में उगाया जा सकता है?",
                    "Is this flower suitable for Siliguri & North Bengal?",
                    "বর্ষায় জল নিকাশী উঁচু বেড বা শেডনেট/পলিহাউস ব্যবহার করলে শিলিগুড়ি-উত্তরবঙ্গের আর্দ্র আবহাওয়ায় অধিকাংশ বাণিজ্যিক ফুল খুব ভালো ফলে; স্থানীয় নার্সারি/KVK-তে জাত নিশ্চিত করুন।",
                    "बरसात में ऊंची क्यारी व जल निकासी या शेडनेट का उपयोग कर उत्तर बंगाल की जलवायु में इसे सफलतापूर्वक उगाया जा सकता है।",
                    "With raised well-drained beds or shade-net/polyhouse protection during monsoon, most ornamentals thrive in North Bengal. Confirm local cultivars at your KVK."
                )
            )
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> listOf(
                CropFaq(
                    "ইনডোর বা টবের গাছের পাতা হলুদ হয়ে ঝরে পড়লে কী করব?",
                    "इंडोर या गमले के पौधे की पत्तियां पीली पड़ने पर क्या करें?",
                    "Why do leaves turn yellow on potted or indoor plants?",
                    "প্রধানত গোড়ায় অতিরিক্ত জল জমে গেলে বা টবে আলো কম থাকলে পাতা হলুদ হয়। জল দেওয়া কমিয়ে নিষ্কাশন ছিদ্র পরিষ্কার করুন এবং উজ্জ্বল পরোক্ষ আলোয় রাখুন।",
                    "अधिक पानी जमा होने या कम रोशनी के कारण पत्तियां पीली होती हैं। सिंचाई कम करें, जल निकासी ठीक करें और अप्रत्यक्ष उजाले में रखें।",
                    "Overwatering and poor pot drainage are the top causes. Let the top soil dry, clear drainage holes, and move to bright indirect light."
                ),
                CropFaq(
                    "নার্সারি গাছের জন্য আদর্শ পটিং মিক্স অনুপাত কী?",
                    "नर्सरी व गमलों के लिए आदर्श पॉटिंग मिक्स अनुपात क्या है?",
                    "What is the ideal potting mix for nursery/ornamental plants?",
                    "সাধারণত ৪০% দোআঁশ বাগানের মাটি + ৩০% পচা ভার্মিকম্পোস্ট + ২০% কোকোপিট + ১০% মোটা বালি/পার্লাইট মিশিয়ে ব্যবহার করলে শিকড় দ্রুত ও সুস্থভাবে বাড়ে।",
                    "40% बगीचे की दोमट मिट्टी + 30% केंचुआ खाद + 20% कोकोपीट + 10% मोटी रेत/परलाइट का मिश्रण सबसे उपयुक्त रहता है।",
                    "A balanced 40% loamy garden soil + 30% vermicompost + 20% cocopeat + 10% coarse sand/perlite mix gives healthy aeration and moisture retention."
                )
            )
            else -> listOf(
                CropFaq(
                    "এই ফসল চাষে মাটির pH ও জল নিকাশী কতটা জরুরি?",
                    "इस फसल के लिए मिट्टी का pH और जल निकासी कितनी जरूरी है?",
                    "How important are soil pH and drainage for this crop?",
                    "সঠিক pH-এ শিকড় সব পুষ্টি সহজে গ্রহণ করে এবং জল নিকাশী ঠিক থাকলে গোড়া পচা ও ছত্রাক রোগ ৭০% পর্যন্ত কমে।",
                    "सही pH पर पौधा सभी पोषक तत्व आसानी से लेता है और जल निकासी से जड़ सड़न रोग काफी कम हो जाते हैं।",
                    "Correct soil pH maximizes nutrient uptake while proper drainage prevents collar and root rot fungal outbreaks."
                ),
                CropFaq(
                    "সঠিক স্থানীয় জাত ও সারের মাত্রা কোথা থেকে যাচাই করব?",
                    "स्थानीय किस्म और खाद-दवा की सही मात्रा कहां से सत्यापित करें?",
                    "Where should I verify local varieties and exact chemical dosages?",
                    "প্রতিটি এলাকার মাটি ও আবহাওয়া আলাদা, তাই চূড়ান্ত বীজ বপন ও কীটনাশক স্প্রে-র আগে নিকটবর্তী কৃষি বিজ্ঞান কেন্দ্র (KVK) বা ব্লক কৃষি দপ্তরের পরামর্শ নিন।",
                    "हर क्षेत्र की मिट्टी व मौसम अलग होता है, इसलिए अंतिम बुवाई व छिड़काव से पहले अपने नजदीकी KVK या कृषि अधिकारी से सलाह अवश्य लें।",
                    "Because soil and weather vary by block, always verify local varieties and chemical label doses with your nearest KVK or Agriculture Officer."
                )
            )
        }
    }

    fun isFloriculture(crop: MasterCrop): Boolean = crop.category == CropCategory.FLOWERS
    fun isOrchardTree(crop: MasterCrop): Boolean = crop.category == CropCategory.FRUITS || crop.category == CropCategory.PLANTATION
    fun isIndoorOrNursery(crop: MasterCrop): Boolean = crop.category == CropCategory.NURSERY_ORNAMENTAL
    fun isAgroforestryOrGreenManure(crop: MasterCrop): Boolean =
        crop.category == CropCategory.AGROFORESTRY_OTHER || crop.category == CropCategory.FODDER
}
