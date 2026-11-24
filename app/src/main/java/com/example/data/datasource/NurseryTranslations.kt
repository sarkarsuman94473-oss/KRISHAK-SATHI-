package com.example.data.datasource

import com.example.data.model.AppLanguage
import com.example.data.model.NurseryBusiness
import com.example.data.model.NurseryCatalogueItem
import com.example.data.model.NurseryOrder
import com.example.data.model.NurseryOrderItem
import com.example.data.model.NurseryProduct
import com.example.data.model.SeedItem

object NurseryTranslations {

    private data class NurseryMeta(
        val nameHi: String,
        val ownerHi: String,
        val ownerEn: String,
        val addressHi: String,
        val addressEn: String,
        val badgeHi: String,
        val badgeEn: String
    )

    private data class CatalogueMeta(
        val nameHi: String,
        val varietyHi: String,
        val varietyEn: String
    )

    private data class SeedMeta(
        val cropHi: String,
        val varietyHi: String,
        val varietyEn: String,
        val brandHi: String,
        val brandEn: String,
        val packetHi: String,
        val packetEn: String
    )

    private val nurseryMap: Map<String, NurseryMeta> = mapOf(
        "nurs_sil_01" to NurseryMeta(
            nameHi = "पुष्पांजलि नर्सरी (Pushpanjali Nursery)",
            ownerHi = "सुब्रत रॉय",
            ownerEn = "Subrata Roy",
            addressHi = "सेवक रोड, दूसरा मील, सिलीगुड़ी",
            addressEn = "Sevoke Road, 2nd Mile, Siliguri",
            badgeHi = "सत्यापित थोक नर्सरी",
            badgeEn = "Verified Wholesale Nursery"
        ),
        "nurs_sil_02" to NurseryMeta(
            nameHi = "बाबा लोकनाथ होलसेल नर्सरी",
            ownerHi = "अनिल घोष",
            ownerEn = "Anil Ghosh",
            addressHi = "माटीगाड़ा हाईवे बाईपास, सिलीगुड़ी",
            addressEn = "Matigara Highway Bypass, Siliguri",
            badgeHi = "अधिकृत थोक केंद्र",
            badgeEn = "Authorized Wholesale Hub"
        ),
        "nurs_sil_03" to NurseryMeta(
            nameHi = "सरकार नर्सरी (Sarkar Nursery)",
            ownerHi = "प्रदीप सरकार",
            ownerEn = "Pradip Sarkar",
            addressHi = "शालुगाड़ा बाजार रोड, सिलीगुड़ी",
            addressEn = "Salugara Market Road, Siliguri",
            badgeHi = "सत्यापित नर्सरी",
            badgeEn = "Verified Nursery"
        ),
        "nurs_sil_04" to NurseryMeta(
            nameHi = "दास नर्सरी सिलीगुड़ी (Das Nursery)",
            ownerHi = "बिकाश दास",
            ownerEn = "Bikash Das",
            addressHi = "प्रधान नगर, जंक्शन रेलवे स्टेशन रोड, सिलीगुड़ी",
            addressEn = "Pradhan Nagar, Junction Railway Station Rd, Siliguri",
            badgeHi = "खुदरा विक्रेता",
            badgeEn = "Retail Nursery"
        ),
        "nurs_sil_05" to NurseryMeta(
            nameHi = "मडी हैंड्स सिलीगुड़ी (Muddy Hands)",
            ownerHi = "रोहित शर्मा",
            ownerEn = "Rohit Sharma",
            addressHi = "हिल कार्ट रोड, सिलीगुड़ी",
            addressEn = "Hill Cart Road, Siliguri",
            badgeHi = "सत्यापित गार्डन सेंटर",
            badgeEn = "Verified Garden Center"
        ),
        "nurs_sil_06" to NurseryMeta(
            nameHi = "पेटुनिया नर्सरी (Petunia Nursery)",
            ownerHi = "सुकांत बर्मन",
            ownerEn = "Sukanta Barman",
            addressHi = "बागडोगरा एयरपोर्ट मोड़, सिलीगुड़ी",
            addressEn = "Bagdogra Airport Crossing, Siliguri",
            badgeHi = "सत्यापित नर्सरी",
            badgeEn = "Verified Nursery"
        ),
        "nurs_sil_07" to NurseryMeta(
            nameHi = "सिलीगुड़ी प्लांट ब्लूम (Siliguri Plant Bloom)",
            ownerHi = "राजीव सेनगुप्ता",
            ownerEn = "Rajib Sengupta",
            addressHi = "ईस्टर्न बाईपास रोड, सिलीगुड़ी",
            addressEn = "Eastern Bypass Road, Siliguri",
            badgeHi = "सत्यापित नर्सरी",
            badgeEn = "Verified Nursery"
        ),
        "nurs_sil_08" to NurseryMeta(
            nameHi = "एवरग्रीन कॉर्नर नर्सरी (Evergreen Corner)",
            ownerHi = "तपन मंडल",
            ownerEn = "Tapan Mandal",
            addressHi = "बर्दवान रोड, जलपाईगुड़ी मोड़, सिलीगुड़ी",
            addressEn = "Burdwan Road, Jalpaiguri More, Siliguri",
            badgeHi = "खुदरा उद्यान केंद्र",
            badgeEn = "Retail Garden Shop"
        ),
        "nurs_sil_09" to NurseryMeta(
            nameHi = "ब्लूमिंग ग्रीन्स (Blooming Greens)",
            ownerHi = "देबाशीष पाल",
            ownerEn = "Debashish Pal",
            addressHi = "मेडिकल कॉलेज रोड, सुश्रुत नगर, सिलीगुड़ी",
            addressEn = "Medical College Road, Sushrutanagar, Siliguri",
            badgeHi = "सत्यापित नर्सरी",
            badgeEn = "Verified Nursery"
        ),
        "nurs_sil_10" to NurseryMeta(
            nameHi = "ग्रीन हेवन नर्सरी - सिलीगुड़ी",
            ownerHi = "सुजीत सिन्हा",
            ownerEn = "Sujit Sinha",
            addressHi = "फालाकाटा रोड मोड़, फूलबाड़ी, सिलीगुड़ी",
            addressEn = "Falakata Road Crossing, Fulbari, Siliguri",
            badgeHi = "सत्यापित थोक नर्सरी",
            badgeEn = "Verified Wholesale Nursery"
        ),
        "nurs_sil_11" to NurseryMeta(
            nameHi = "गार्डन नीड शॉप (Garden Need)",
            ownerHi = "अमित अग्रवाल",
            ownerEn = "Amit Agarwal",
            addressHi = "बिधान मार्केट, ब्लॉक-सी, सिलीगुड़ी",
            addressEn = "Bidhan Market, Block-C, Siliguri",
            badgeHi = "बागवानी उपकरण व बीज",
            badgeEn = "Garden Tools & Seeds"
        ),
        "nurs_sil_12" to NurseryMeta(
            nameHi = "प्लांटर्स हब (Planter's Hub)",
            ownerHi = "सौगत मुखर्जी",
            ownerEn = "Saugata Mukherjee",
            addressHi = "मैंगो ग्रोव लेन, चंपासारी, सिलीगुड़ी",
            addressEn = "Mango Grove Lane, Champasari, Siliguri",
            badgeHi = "चाय व फल पौध केंद्र",
            badgeEn = "Tea & Fruit Saplings Hub"
        ),
        "seed_sil_13" to NurseryMeta(
            nameHi = "जायसवाल सीड्स कं. (Jaiswal Seeds Co.)",
            ownerHi = "मुकेश जायसवाल",
            ownerEn = "Mukesh Jaiswal",
            addressHi = "खालपाड़ा बीज पट्टी, सिलीगुड़ी",
            addressEn = "Khalpara Seed Market, Siliguri",
            badgeHi = "सरकारी लाइसेंस प्राप्त डीलर",
            badgeEn = "Govt. Licensed Seed Dealer"
        ),
        "seed_sil_14" to NurseryMeta(
            nameHi = "जीजी क्रॉप साइंस (GG Crop Science)",
            ownerHi = "गौतम घोषाल",
            ownerEn = "Gautam Ghosal",
            addressHi = "बिधान मार्केट फ्रूट व सीड जोन, सिलीगुड़ी",
            addressEn = "Bidhan Market Fruit & Seed Zone, Siliguri",
            badgeHi = "प्रमाणित हाइब्रिड बीज विक्रेता",
            badgeEn = "Certified Hybrid Seed Seller"
        ),
        "seed_sil_15" to NurseryMeta(
            nameHi = "अनूप सीड्स कं. (Anup Seeds Co)",
            ownerHi = "अनूप गुप्ता",
            ownerEn = "Anup Gupta",
            addressHi = "नयाबाजार, खालपाड़ा रोड, सिलीगुड़ी",
            addressEn = "Nayabazar, Khalpara Road, Siliguri",
            badgeHi = "क्षेत्रीय थोक बीज संस्थान",
            badgeEn = "Regional Seed Wholesaler"
        ),
        "seed_sil_16" to NurseryMeta(
            nameHi = "एग्रो हाइब्रिड सीड्स सेंटर",
            ownerHi = "सुशील कुमार",
            ownerEn = "Sushil Kumar",
            addressHi = "माटीगाड़ा थाना रोड, सिलीगुड़ी",
            addressEn = "Matigara Thana Road, Siliguri",
            badgeHi = "सत्यापित सीड्स सेंटर",
            badgeEn = "Verified Seeds Centre"
        ),
        "seed_sil_17" to NurseryMeta(
            nameHi = "इवोजिन एग्रीटेक प्रा. लि. (Evogene Agritech)",
            ownerHi = "निदेशक / सेल्स विभाग",
            ownerEn = "Director / Sales Dept.",
            addressHi = "इंडस्ट्रियल एस्टेट, डाबग्राम, सिलीगुड़ी",
            addressEn = "Industrial Estate, Dabgram, Siliguri",
            badgeHi = "कृषि बायोटेक व थोक बीज आपूर्तिकर्ता",
            badgeEn = "Agri-Biotech & Bulk Seed Supplier"
        ),
        "seed_sil_18" to NurseryMeta(
            nameHi = "अर्बानिक हार्वेस्ट (Urbanic Harvest)",
            ownerHi = "पवन जालान",
            ownerEn = "Pawan Jalan",
            addressHi = "सेवक मोड़, हाकिमपाड़ा, सिलीगुड़ी",
            addressEn = "Sevoke More, Hakimpara, Siliguri",
            badgeHi = "हाई-टेक सब्जी बीज वितरक",
            badgeEn = "Hi-Tech Vegetable Seed Distributor"
        ),
        "seed_sil_19" to NurseryMeta(
            nameHi = "पैरिपोसा सीड्स इंडिया प्रा. लि.",
            ownerHi = "शाखा प्रबंधक",
            ownerEn = "Branch Manager",
            addressHi = "उत्तर बंगाल चैंबर भवन के पास, सेवक रोड, सिलीगुड़ी",
            addressEn = "Near NB Chamber Bhawan, Sevoke Rd, Siliguri",
            badgeHi = "अंतरराष्ट्रीय गुणवत्ता बीज आपूर्तिकर्ता",
            badgeEn = "International Quality Seed Supplier"
        ),
        "seed_sil_20" to NurseryMeta(
            nameHi = "एग्रीकल्चर वर्ल्ड (Agriculture World)",
            ownerHi = "मनोज वर्मा",
            ownerEn = "Manoj Verma",
            addressHi = "हिल कार्ट रोड, बस टर्मिनस के पास, सिलीगुड़ी",
            addressEn = "Hill Cart Road, Near Bus Terminus, Siliguri",
            badgeHi = "संपूर्ण कृषि बीज व पौध सेवा",
            badgeEn = "Complete Seed & Nursery Solutions"
        )
    )

    private val catalogueMap: Map<String, CatalogueMeta> = mapOf(
        "p_01" to CatalogueMeta("आम्रपाली आम कलम", "कलम पौधा", "Grafted Sapling"),
        "p_02" to CatalogueMeta("माल्टा / मौसंबी कलम", "वियतनामी कलम", "Vietnamese Grafted"),
        "p_03" to CatalogueMeta("थाई अमरूद पौधा (L-49)", "टिश्यू कल्चर", "Tissue Culture"),
        "p_04" to CatalogueMeta("टमाटर हाइब्रिड पौध", "अभिलाष / साहो", "Abhilash / Saho Hybrid"),
        "p_05" to CatalogueMeta("फूलगोभी प्रोट्रे पौध", "स्नो व्हाइट", "Snow White"),
        "p_06" to CatalogueMeta("चाय क्लोन पौध (TV-1, TV-26)", "अनुमोदित नर्सरी क्लोन", "Approved Nursery Clone"),
        "bl_01" to CatalogueMeta("महोगनी लकड़ी पौधा", "1 वर्ष का मजबूत पौधा", "1-Year Hardened Sapling"),
        "bl_02" to CatalogueMeta("सागौन पौधा (रूट-स्टंप)", "प्रीमियम ग्रेड", "Premium Grade"),
        "bl_03" to CatalogueMeta("लीची बॉम्बे कलम", "देसी गुटी कलम", "Air-Layered Grafted"),
        "bl_04" to CatalogueMeta("सुपारी पौधा (मंगला किस्म)", "पैकेट पौधा", "Polypack Seedling"),
        "sk_01" to CatalogueMeta("मिर्च पौध (बुलेट / तेजपुरी)", "प्रोट्रे नेट पौध", "Pro-tray Net Seedling"),
        "sk_02" to CatalogueMeta("बैंगन पौध (मुकुटकेशी)", "उन्नत पौध", "Improved Seedling"),
        "sk_03" to CatalogueMeta("कालमेघ व तुलसी पौधा", "पॉलिथीन पैक", "Polybag Sapling"),
        "sk_04" to CatalogueMeta("गेंदा व गुलदाउदी पौध", "कोलकाता कटिंग", "Kolkata Cuttings"),
        "dn_01" to CatalogueMeta("कागजी नींबू कलम पौधा", "बारहमासी फलन", "All-Season Fruiting"),
        "dn_02" to CatalogueMeta("ड्रैगन फ्रूट कटिंग (लाल)", "जड़ निकली कटिंग", "Rooted Cutting"),
        "mh_01" to CatalogueMeta("एलोवेरा / घृतकुमारी", "पुष्ट पत्तेदार पौधा", "Healthy Leaf Sapling"),
        "mh_02" to CatalogueMeta("गुलाब कलम (इंग्लिश रोज)", "गमले में लगा पौधा", "Potted Grafted Rose"),
        "pt_01" to CatalogueMeta("हाइब्रिड पेटुनिया पौध", "रंग-बिरंगे फूल", "Multi-color Flowers"),
        "pt_02" to CatalogueMeta("बोगनवेलिया कलम", "मल्टीकलर बोनसाई/कलम", "Multicolor Bonsai/Grafted"),
        "spb_01" to CatalogueMeta("रेड लेडी 786 पपीता पौध", "टिश्यू पौध", "Tissue Seedling"),
        "spb_02" to CatalogueMeta("हरी शिमला मिर्च पौध", "इंद्रा किस्म", "Indra Hybrid"),
        "eg_01" to CatalogueMeta("अश्वगंधा औषधीय पौधा", "पॉली पैक", "Poly Pack"),
        "bg_01" to CatalogueMeta("नारियल गंगाबोंडम बौनी किस्म", "3 वर्ष में फलन किस्म", "3-Year Early Bearing"),
        "bg_02" to CatalogueMeta("पत्तागोभी ग्रीन एक्सप्रेस पौध", "हाइब्रिड प्रोट्रे", "Hybrid Pro-tray"),
        "gh_01" to CatalogueMeta("अगरवुड पौधा (Aquilaria)", "प्राकृतिक टीकाकरण योग्य", "Natural Inoculation Ready"),
        "gh_02" to CatalogueMeta("गमारी लकड़ी का पौधा", "तेजी से बढ़ने वाला", "Fast-Growing Timber"),
        "gn_01" to CatalogueMeta("ऑर्गेनिक कोकोपीट ब्लॉक (5 किग्रा)", "पौध तैयार करने के लिए", "For Seedling Nursery"),
        "ph_01" to CatalogueMeta("TT-9 व TV-22 चाय क्लोन पौध", "प्रमाणित मदर बुश", "Certified Mother Bush"),
        "app_item_1" to CatalogueMeta("उच्च उपज कलम पौधा", "प्रथम श्रेणी कलम", "Grade-A Grafted"),
        "app_item_2" to CatalogueMeta("सब्जी पौध ट्रे पैक", "प्रोट्रे रेडी पौध", "Pro-tray Ready Seedling")
    )

    private val seedMap: Map<String, SeedMeta> = mapOf(
        "sd_01" to SeedMeta("टमाटर हाइब्रिड बीज", "साहो 3251 (Syngenta Saho)", "Saho 3251 (Syngenta)", "सिनजेंटा (Syngenta)", "Syngenta", "10 ग्राम (3000 बीज)", "10g (3,000 Seeds)"),
        "sd_02" to SeedMeta("मिर्च हाइब्रिड बीज", "वीएनआर सानिया (VNR Saniya)", "VNR Saniya", "वीएनआर सीड्स (VNR)", "VNR Seeds", "10 ग्राम", "10g"),
        "sd_03" to SeedMeta("पत्तागोभी हाइब्रिड बीज", "ग्रीन एक्सप्रेस F1", "Green Express F1", "सेमिनिस (Seminis)", "Seminis", "10 ग्राम", "10g"),
        "sd_04" to SeedMeta("पालक देसी बीज", "हरा ऑल-ग्रीन किस्म", "All-Green Desi Variety", "नेशनल सीड्स (NSC)", "National Seeds (NSC)", "500 ग्राम", "500g"),
        "sd_05" to SeedMeta("धान बीज (स्वर्णा सब-1)", "बाढ़ सहनशील स्वर्णा सब-1", "Flood-Tolerant Swarna Sub-1", "डब्ल्यूबी स्टेट सीड कॉर्प", "WB State Seed Corp", "10 किग्रा बैग", "10 kg Bag"),
        "sd_06" to SeedMeta("सरसों बीज (बिनॉय B-9)", "बिनॉय (B-9)", "Binoy (B-9)", "आईसीएआर प्रमाणित", "ICAR Certified", "1 किग्रा", "1 kg"),
        "sd_07" to SeedMeta("मटर बीज (GS-10)", "एडवांटा GS-10 गोल्डन", "Advanta GS-10 Golden", "एडवांटा (Advanta)", "Advanta", "1 किग्रा पैक", "1 kg Pack"),
        "sd_08" to SeedMeta("सुपर नेपियर घास कटिंग व बीज", "सुपर नेपियर CO-5", "Super Napier CO-5", "लाइवस्टॉक फीड कॉर्पोरेशन", "Livestock Feed Corp", "100 तना कटिंग बंडल", "100 Stem Cuttings Bundle")
    )

    fun getNurseryName(n: NurseryBusiness, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> n.nameBn
        AppLanguage.HINDI -> nurseryMap[n.id]?.nameHi ?: n.nameEn.ifBlank { n.nameBn }
        AppLanguage.ENGLISH -> n.nameEn.ifBlank { n.nameBn }
    }

    fun getOwnerName(n: NurseryBusiness, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> n.ownerName
        AppLanguage.HINDI -> nurseryMap[n.id]?.ownerHi ?: n.ownerName
        AppLanguage.ENGLISH -> nurseryMap[n.id]?.ownerEn ?: n.ownerName
    }

    fun getAddress(n: NurseryBusiness, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> n.addressBn
        AppLanguage.HINDI -> nurseryMap[n.id]?.addressHi ?: "${n.city}, ${n.district}"
        AppLanguage.ENGLISH -> nurseryMap[n.id]?.addressEn ?: "${n.city}, ${n.district}"
    }

    fun getBadge(n: NurseryBusiness, lang: AppLanguage): String {
        val meta = nurseryMap[n.id]
        if (meta != null) {
            return when (lang) {
                AppLanguage.BENGALI -> n.badgeText
                AppLanguage.HINDI -> meta.badgeHi
                AppLanguage.ENGLISH -> meta.badgeEn
            }
        }
        return when (lang) {
            AppLanguage.BENGALI -> n.badgeText
            AppLanguage.HINDI -> when {
                n.badgeText.contains("পর্যালোচনাধীন") -> "समीक्षाधीन (Pending)"
                n.isVerified -> "सत्यापित नर्सरी"
                else -> "पंजीकृत नर्सरी"
            }
            AppLanguage.ENGLISH -> when {
                n.badgeText.contains("পর্যালোচনাধীন") -> "Pending Verification"
                n.isVerified -> "Verified Nursery"
                else -> "Registered Nursery"
            }
        }
    }

    fun getCatalogueName(item: NurseryCatalogueItem, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> item.nameBn
        AppLanguage.HINDI -> catalogueMap[item.id]?.nameHi ?: item.nameEn.ifBlank { item.nameBn }
        AppLanguage.ENGLISH -> item.nameEn.ifBlank { item.nameBn }
    }

    fun getCatalogueVariety(item: NurseryCatalogueItem, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> item.varietyBn
        AppLanguage.HINDI -> catalogueMap[item.id]?.varietyHi ?: item.varietyBn
        AppLanguage.ENGLISH -> catalogueMap[item.id]?.varietyEn ?: item.varietyBn
    }

    fun getCatalogueUnit(item: NurseryCatalogueItem, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> item.unitBn
        AppLanguage.HINDI -> when (item.unitBn) {
            "চারা" -> "पौधा"
            "গাছ" -> "पौधा"
            "ব্লক" -> "ब्लॉक"
            else -> item.unitBn
        }
        AppLanguage.ENGLISH -> when (item.unitBn) {
            "চারা" -> "Sapling"
            "গাছ" -> "Plant"
            "ব্লক" -> "Block"
            else -> item.unitBn
        }
    }

    fun getSeedCropName(seed: SeedItem, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> seed.cropNameBn
        AppLanguage.HINDI -> seedMap[seed.id]?.cropHi ?: seed.cropNameEn.ifBlank { seed.cropNameBn }
        AppLanguage.ENGLISH -> seed.cropNameEn.ifBlank { seed.cropNameBn }
    }

    fun getSeedVariety(seed: SeedItem, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> seed.varietyBn
        AppLanguage.HINDI -> seedMap[seed.id]?.varietyHi ?: seed.varietyBn
        AppLanguage.ENGLISH -> seedMap[seed.id]?.varietyEn ?: seed.varietyBn
    }

    fun getSeedBrand(seed: SeedItem, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> seed.brandName
        AppLanguage.HINDI -> seedMap[seed.id]?.brandHi ?: seed.brandName
        AppLanguage.ENGLISH -> seedMap[seed.id]?.brandEn ?: seed.brandName
    }

    fun getSeedPacketSize(seed: SeedItem, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> seed.packetSize
        AppLanguage.HINDI -> seedMap[seed.id]?.packetHi ?: seed.packetSize
        AppLanguage.ENGLISH -> seedMap[seed.id]?.packetEn ?: seed.packetSize
    }

    private data class ProductMeta(
        val nameHi: String,
        val nameEn: String,
        val nurseryHi: String,
        val nurseryEn: String,
        val descHi: String,
        val descEn: String,
        val sizeHi: String,
        val sizeEn: String,
        val typeHi: String,
        val typeEn: String,
        val careHi: String,
        val careEn: String,
        val locHi: String,
        val locEn: String
    )

    private val productMap: Map<String, ProductMeta> = mapOf(
        "np_01" to ProductMeta(
            nameHi = "आम्रपाली आम कलम पौधा (Grafted Amrapali)",
            nameEn = "Amrapali Mango Grafted Sapling",
            nurseryHi = "उत्तरबंग ग्रीन नर्सरी व टी रिसर्च हब",
            nurseryEn = "North Bengal Green Nursery & Tea Research Hub",
            descHi = "उच्च उपज देने वाली मीठी आम्रपाली किस्म की 1 वर्ष पुरानी स्वस्थ कलम। पहले वर्ष से ही बौर और फल आते हैं।",
            descEn = "1-year-old healthy grafted Amrapali mango sapling. Bears flowers and fruits from the very first year.",
            sizeHi = "2-3 फीट (सामान्य वृद्धि)",
            sizeEn = "2-3 ft (Standard Growth)",
            typeHi = "कलम पौधा (Grafted)",
            typeEn = "Grafted Sapling",
            careHi = "पर्याप्त धूप में रखें। पहले 15 दिन सुबह हल्का पानी दें। मानसून में जल निकासी की व्यवस्था रखें।",
            careEn = "Keep in full sunlight. Water lightly every morning for the first 15 days. Ensure proper drainage during monsoon.",
            locHi = "सिलीगुड़ी, दार्जिलिंग",
            locEn = "Siliguri, Darjeeling"
        ),
        "np_02" to ProductMeta(
            nameHi = "टीवी-29 अनुमोदित चाय पौध (TV-29 Tea Clone)",
            nameEn = "TV-29 Approved Tea Clone Sapling",
            nurseryHi = "उत्तरबंग ग्रीन नर्सरी व टी रिसर्च हब",
            nurseryEn = "North Bengal Green Nursery & Tea Research Hub",
            descHi = "टी रिसर्च एसोसिएशन (TRA) द्वारा अनुमोदित सूखा-सहिष्णु और उच्च उपज वाला TV-29 चाय क्लोन।",
            descEn = "Tea Research Association (TRA) approved drought-tolerant, high-yielding TV-29 tea clone sapling.",
            sizeHi = "1-1.5 फीट (प्रोट्रे पैक)",
            sizeEn = "1-1.5 ft (Pro-tray Pack)",
            typeHi = "सत्यापित क्लोन (TRA अनुमोदित)",
            typeEn = "Verified Clone (TRA Approved)",
            careHi = "चाय बागान के छायादार पेड़ों के नीचे लगाएं। अम्लीय मिट्टी (pH 4.5-5.5) में बेहतर उपज।",
            careEn = "Plant under shade trees in tea gardens. Thrives best in acidic soil (pH 4.5-5.5).",
            locHi = "जलपाईगुड़ी / डुआर्स",
            locEn = "Jalpaiguri / Dooars"
        ),
        "np_03" to ProductMeta(
            nameHi = "ड्रैगन फ्रूट पौध (रेड वेलवेट किस्म)",
            nameEn = "Dragon Fruit Sapling (Red Velvet)",
            nurseryHi = "माँ तारा हाईटेक प्लांट नर्सरी",
            nurseryEn = "Maa Tara Hi-Tech Plant Nursery",
            descHi = "वियतनामी लाल गूदे वाली मीठी ड्रैगन फ्रूट किस्म। सीमेंट के खंभे के सहारे आसानी से उगाई जा सकती है।",
            descEn = "Sweet Vietnamese red-fleshed dragon fruit cutting. Easy to train on cement support poles.",
            sizeHi = "1.5 फीट कटिंग पौध",
            sizeEn = "1.5 ft Rooted Cutting",
            typeHi = "मदर प्लांट कटिंग",
            typeEn = "Mother Plant Cutting",
            careHi = "बलुई दोमट मिट्टी में लगाएं। भारी बारिश में जड़ों में पानी जमा न होने दें।",
            careEn = "Plant in sandy-loam soil. Prevent waterlogging during heavy rainfall.",
            locHi = "मयनागुड़ी, जलपाईगुड़ी",
            locEn = "Maynaguri, Jalpaiguri"
        ),
        "np_04" to ProductMeta(
            nameHi = "हाइब्रिड गुड़हल व थाई गुलाब पौधा",
            nameEn = "Hybrid Hibiscus & Thai Rose Plants",
            nurseryHi = "माँ तारा हाईटेक प्लांट नर्सरी",
            nurseryEn = "Maa Tara Hi-Tech Plant Nursery",
            descHi = "साल भर खिलने वाले रंग-बिरंगे फूल। बालकनी और होम गार्डन की सुंदरता के लिए उपयुक्त।",
            descEn = "All-season colorful flowering plants. Ideal for balconies and home gardens.",
            sizeHi = "गमले सहित मध्यम आकार",
            sizeEn = "Medium Size (With Pot)",
            typeHi = "लेयरिंग / कलम",
            typeEn = "Layered / Grafted",
            careHi = "रोजाना 4-5 घंटे सीधी धूप और महीने में एक बार जैविक खाद दें।",
            careEn = "Provide 4-5 hours of direct sunlight daily and apply organic compost once a month.",
            locHi = "मयनागुड़ी",
            locEn = "Maynaguri"
        ),
        "np_05" to ProductMeta(
            nameHi = "स्नेक प्लांट व एरेका पाम (इंडोर एयर प्यूरीफायर)",
            nameEn = "Snake Plant & Areca Palm (Indoor Air Purifier)",
            nurseryHi = "उत्तरबंग ग्रीन नर्सरी व टी रिसर्च हब",
            nurseryEn = "North Bengal Green Nursery & Tea Research Hub",
            descHi = "घर के अंदर की हवा शुद्ध करने और ऑक्सीजन बढ़ाने वाले कम देखभाल वाले इंडोर पौधे।",
            descEn = "Low-maintenance indoor plants that purify indoor air and boost oxygen.",
            sizeHi = "2 फीट (सिरेमिक गमले सहित)",
            sizeEn = "2 ft (With Ceramic Pot)",
            typeHi = "इंडोर सजावटी पौधा",
            typeEn = "Indoor Ornamental",
            careHi = "सीधी तेज धूप से बचाकर कमरे के उजले कोने में रखें। मिट्टी सूखने पर ही पानी दें।",
            careEn = "Keep in a bright corner away from harsh direct sun. Water only when topsoil feels dry.",
            locHi = "सिलीगुड़ी",
            locEn = "Siliguri"
        ),
        "np_06" to ProductMeta(
            nameHi = "प्रोट्रे टमाटर व बैंगन हाइब्रिड पौध (50 पीस ट्रे)",
            nameEn = "Pro-tray Tomato & Brinjal Hybrid Seedlings (50-Cell Tray)",
            nurseryHi = "उत्तरबंग ग्रीन नर्सरी व टी रिसर्च हब",
            nurseryEn = "North Bengal Green Nursery & Tea Research Hub",
            descHi = "कोकोपीट और वर्मीकम्पोस्ट में तैयार रोग-मुक्त 50 पीस उच्च उपज वाली सब्जी पौध ट्रे।",
            descEn = "Disease-free 50-cell high-yielding vegetable seedling tray grown in cocopeat & vermicompost.",
            sizeHi = "25 दिन की तैयार पौध",
            sizeEn = "25-Day Ready Seedlings",
            typeHi = "प्रोट्रे हाइब्रिड पौध",
            typeEn = "Pro-tray Hybrid Seedlings",
            careHi = "खेत में शाम के समय रोपाई करें और जड़ों के पास हल्की सिंचाई दें।",
            careEn = "Transplant in the field during late afternoon and give light root-zone irrigation.",
            locHi = "दार्जिलिंग",
            locEn = "Darjeeling"
        ),
        "np_07" to ProductMeta(
            nameHi = "कालमेघ व अश्वगंधा औषधीय पौधा",
            nameEn = "Kalmegh & Ashwagandha Medicinal Plants",
            nurseryHi = "माँ तारा हाईटेक प्लांट नर्सरी",
            nurseryEn = "Maa Tara Hi-Tech Plant Nursery",
            descHi = "आयुर्वेदिक और रोग-प्रतिरोधक गुणों से भरपूर प्राकृतिक औषधीय पौधे।",
            descEn = "Natural medicinal saplings packed with Ayurvedic and immunity-boosting properties.",
            sizeHi = "1 फीट",
            sizeEn = "1 ft",
            typeHi = "देसी औषधीय पौधा",
            typeEn = "Desi Medicinal",
            careHi = "किसी भी मिट्टी में आसानी से उगते हैं। अधिक पानी की आवश्यकता नहीं है।",
            careEn = "Grows easily in most soils. Does not require excessive watering.",
            locHi = "जलपाईगुड़ी",
            locEn = "Jalpaiguri"
        ),
        "np_08" to ProductMeta(
            nameHi = "वर्मीकम्पोस्ट व ट्राइकोडर्मा जैविक खाद (5 किग्रा पैक)",
            nameEn = "Vermicompost & Trichoderma Organic Manure (5 kg Pack)",
            nurseryHi = "उत्तरबंग ग्रीन नर्सरी व टी रिसर्च हब",
            nurseryEn = "North Bengal Green Nursery & Tea Research Hub",
            descHi = "पौधों की तेज वृद्धि और मिट्टी की उर्वरता बढ़ाने के लिए 100% शुद्ध केंचुआ खाद।",
            descEn = "100% pure earthworm compost for faster plant growth and soil fertility enrichment.",
            sizeHi = "5 किग्रा सील्ड पैक",
            sizeEn = "5 kg Sealed Pack",
            typeHi = "जैविक खाद",
            typeEn = "Organic Fertilizer",
            careHi = "महीने में एक बार प्रत्येक पौधे की जड़ के पास 100-150 ग्राम मिट्टी में मिलाएं।",
            careEn = "Mix 100-150g into soil around the base of each plant once a month.",
            locHi = "सिलीगुड़ी",
            locEn = "Siliguri"
        ),
        "np_09" to ProductMeta(
            nameHi = "प्रूनिंग कैंची व स्प्रेयर सेट (बागवानी उपकरण)",
            nameEn = "Pruning Shears & Sprayer Set (Gardening Tools)",
            nurseryHi = "माँ तारा हाईटेक प्लांट नर्सरी",
            nurseryEn = "Maa Tara Hi-Tech Plant Nursery",
            descHi = "पौधों की छंटाई और छिड़काव के लिए मजबूत स्टेनलेस स्टील बागवानी टूल सेट।",
            descEn = "Heavy-duty stainless steel tool set for tree pruning and foliar spraying.",
            sizeHi = "मानक आकार",
            sizeEn = "Standard Size",
            typeHi = "बागवानी उपकरण",
            typeEn = "Gardening Tools",
            careHi = "उपयोग के बाद सूखे कपड़े से पोंछकर सुरक्षित रखें।",
            careEn = "Wipe dry with a clean cloth after every use.",
            locHi = "जलपाईगुड़ी",
            locEn = "Jalpaiguri"
        ),
        "np_10" to ProductMeta(
            nameHi = "10 इंच टेराकोटा व यूवी प्लास्टिक गमला सेट",
            nameEn = "10-Inch Terracotta & UV Planter Pot Set",
            nurseryHi = "उत्तरबंग ग्रीन नर्सरी व टी रिसर्च हब",
            nurseryEn = "North Bengal Green Nursery & Tea Research Hub",
            descHi = "छत बागवानी और बालकनी में पौधे लगाने के लिए जल निकासी छेद वाले टिकाऊ गमले।",
            descEn = "Durable UV-resistant pots with proper drainage holes for terrace and balcony gardens.",
            sizeHi = "10 इंच ड्रेनेज पैक",
            sizeEn = "10-Inch Drainage Pack",
            typeHi = "गमले व सहायक सामग्री",
            typeEn = "Pots & Accessories",
            careHi = "साफ करने में आसान और धूप में टिकाऊ।",
            careEn = "Easy to clean and weather-durable in sunlight.",
            locHi = "सिलीगुड़ी",
            locEn = "Siliguri"
        )
    )

    fun containsBengali(text: String): Boolean = text.any { it in '\u0980'..'\u09FF' }

    fun getProductName(p: NurseryProduct, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> p.productName
        AppLanguage.HINDI -> productMap[p.id]?.nameHi ?: p.productName
        AppLanguage.ENGLISH -> productMap[p.id]?.nameEn ?: p.productName
    }

    fun getProductNurseryName(p: NurseryProduct, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> p.nurseryName
        AppLanguage.HINDI -> productMap[p.id]?.nurseryHi ?: p.nurseryName
        AppLanguage.ENGLISH -> productMap[p.id]?.nurseryEn ?: p.nurseryName
    }

    fun getProductDescription(p: NurseryProduct, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> p.description
        AppLanguage.HINDI -> productMap[p.id]?.descHi ?: p.description
        AppLanguage.ENGLISH -> productMap[p.id]?.descEn ?: p.description
    }

    fun getProductSize(p: NurseryProduct, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> p.plantSize
        AppLanguage.HINDI -> productMap[p.id]?.sizeHi ?: p.plantSize
        AppLanguage.ENGLISH -> productMap[p.id]?.sizeEn ?: p.plantSize
    }

    fun getProductType(p: NurseryProduct, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> p.plantType
        AppLanguage.HINDI -> productMap[p.id]?.typeHi ?: p.plantType
        AppLanguage.ENGLISH -> productMap[p.id]?.typeEn ?: p.plantType
    }

    fun getProductCare(p: NurseryProduct, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> p.plantCareInstructions
        AppLanguage.HINDI -> productMap[p.id]?.careHi ?: p.plantCareInstructions
        AppLanguage.ENGLISH -> productMap[p.id]?.careEn ?: p.plantCareInstructions
    }

    fun getProductLocation(p: NurseryProduct, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> p.productLocation
        AppLanguage.HINDI -> productMap[p.id]?.locHi ?: p.productLocation
        AppLanguage.ENGLISH -> productMap[p.id]?.locEn ?: p.productLocation
    }

    fun getOrderCustomerName(order: NurseryOrder, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI) return order.customerName
        return when (order.id) {
            "ord_101" -> if (lang == AppLanguage.HINDI) "सुमन सरकार" else "Suman Sarkar"
            "ord_102" -> if (lang == AppLanguage.HINDI) "प्रणब कुमार दास" else "Pranab Kumar Das"
            "ord_103" -> if (lang == AppLanguage.HINDI) "अनीता रॉय" else "Anita Roy"
            else -> order.customerName
        }
    }

    fun getOrderAddress(order: NurseryOrder, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI) return order.deliveryAddress
        return when (order.id) {
            "ord_101" -> if (lang == AppLanguage.HINDI) "शालुगाड़ा रोड, चंपासारी, सिलीगुड़ी" else "Salugara Road, Champasari, Siliguri"
            "ord_102" -> if (lang == AppLanguage.HINDI) "बीरपाड़ा, अलीपुरद्वार" else "Birpara, Alipurduar"
            "ord_103" -> if (lang == AppLanguage.HINDI) "सेवक रोड, सिलीगुड़ी" else "Sevoke Road, Siliguri"
            else -> order.deliveryAddress
        }
    }

    fun getOrderItemName(item: NurseryOrderItem, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI) return item.productName
        val mapped = productMap[item.productId]
        if (mapped != null) {
            return if (lang == AppLanguage.HINDI) mapped.nameHi else mapped.nameEn
        }
        return item.productName
    }

    private data class AppMeta(
        val nameHi: String,
        val nameEn: String,
        val ownerHi: String,
        val ownerEn: String,
        val descHi: String,
        val descEn: String,
        val addressHi: String,
        val addressEn: String,
        val hoursHi: String,
        val hoursEn: String
    )

    private val appMap: Map<String, AppMeta> = mapOf(
        "app_siliguri_green" to AppMeta(
            nameHi = "उत्तरबंग ग्रीन नर्सरी व टी रिसर्च हब",
            nameEn = "North Bengal Green Nursery & Tea Research Hub",
            ownerHi = "रबींद्रनाथ राय",
            ownerEn = "Rabindranath Ray",
            descHi = "उन्नत किस्म के चाय पौधे, कलम और देसी फल पौधों का विश्वसनीय एवं सरकार अनुमोदित केंद्र।",
            descEn = "Govt. approved trusted hub for high-yielding tea clones, grafted saplings, and indigenous fruit plants.",
            addressHi = "शालुगाड़ा रोड, चंपासारी, सिलीगुड़ी",
            addressEn = "Salugara Road, Champasari, Siliguri",
            hoursHi = "सुबह 7:00 - शाम 7:00",
            hoursEn = "07:00 AM - 07:00 PM"
        ),
        "app_kisan_phul" to AppMeta(
            nameHi = "माँ तारा हाईटेक प्लांट नर्सरी",
            nameEn = "Maa Tara High-Tech Plant Nursery",
            ownerHi = "बिप्लब कुमार साहा",
            ownerEn = "Biplab Kumar Saha",
            descHi = "किफायती थोक दरों पर उच्च उपज सब्जी पौध, ड्रैगन फ्रूट, माल्टा व आम्रपाली कलम की आपूर्ति।",
            descEn = "Wholesale supplier of high-yielding vegetable seedlings, dragon fruit, malta, and grafted Amrapali mango saplings.",
            addressHi = "धूपगुड़ी बाजार के पास, जलपाईगुड़ी",
            addressEn = "Near Dhupguri Market, Jalpaiguri",
            hoursHi = "सुबह 8:00 - शाम 6:00",
            hoursEn = "08:00 AM - 06:00 PM"
        )
    )

    fun getApplicationDisplayName(app: com.example.data.model.NurseryApplication, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> app.nurseryNameBn.ifBlank { app.nurseryNameEn }
        AppLanguage.HINDI -> appMap[app.id]?.nameHi ?: app.nurseryNameEn.ifBlank { app.nurseryNameBn }
        AppLanguage.ENGLISH -> appMap[app.id]?.nameEn ?: app.nurseryNameEn.ifBlank { app.nurseryNameBn }
    }

    fun getApplicationOwnerName(app: com.example.data.model.NurseryApplication, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> app.applicantName
        AppLanguage.HINDI -> appMap[app.id]?.ownerHi ?: app.applicantName
        AppLanguage.ENGLISH -> appMap[app.id]?.ownerEn ?: app.applicantName
    }

    fun getApplicationDescription(app: com.example.data.model.NurseryApplication, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> app.description
        AppLanguage.HINDI -> appMap[app.id]?.descHi ?: app.description
        AppLanguage.ENGLISH -> appMap[app.id]?.descEn ?: app.description
    }

    fun getApplicationFullAddress(app: com.example.data.model.NurseryApplication, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> app.fullAddress
        AppLanguage.HINDI -> appMap[app.id]?.addressHi ?: "${app.cityOrTown}, ${app.district}"
        AppLanguage.ENGLISH -> appMap[app.id]?.addressEn ?: "${app.cityOrTown}, ${app.district}"
    }

    fun getApplicationOpeningHours(app: com.example.data.model.NurseryApplication, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> app.openingHours
        AppLanguage.HINDI -> appMap[app.id]?.hoursHi ?: when {
            containsBengali(app.openingHours) -> "सुबह 8:00 - शाम 6:00"
            else -> app.openingHours
        }
        AppLanguage.ENGLISH -> appMap[app.id]?.hoursEn ?: when {
            containsBengali(app.openingHours) -> "08:00 AM - 06:00 PM"
            else -> app.openingHours
        }
    }
}


