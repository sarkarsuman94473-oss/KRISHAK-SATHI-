package com.example.data.datasource

import com.example.data.model.VerifiedAgriKnowledgeRecord

/**
 * Curated offline-capable official agricultural reference corpus sourced from
 * ICAR, BCKV (Bidhan Chandra Krishi Viswavidyalaya), UBKV (Uttar Banga Krishi Viswavidyalaya),
 * TRA (Tea Research Association - Tocklai), Govt of India DA&FW, and CIBRC.
 * Every sourceUrl uses verified HTTPS official domains.
 */
object TrustedAgriKnowledgeCatalog {

    val records: List<VerifiedAgriKnowledgeRecord> = listOf(
        VerifiedAgriKnowledgeRecord(
            id = "icar_nrri_rice_ipm",
            titleEn = "ICAR-NRRI Rice Blast & Stem Borer IPM Guide",
            titleBn = "আইসিএআর ধানের ব্লাস্ট ও মাজরা পোকা দমন নির্দেশিকা",
            titleHi = "आईसीएआर धान ब्लास्ट एवं तना छेदक प्रबंधन मार्गदर्शिका",
            institution = "ICAR-NRRI",
            sourceUrl = "https://icar-nrri.in/",
            publicationDate = "2025-11-15",
            verifiedDate = "2026-11-17",
            category = "CROP_PROTECTION",
            applicableCropIds = listOf("rice_paddy", "aman_rice", "boro_rice"),
            regionTag = "West Bengal & Eastern India",
            keywords = listOf("rice", "paddy", "ধান", "धान", "blast", "stem borer", "মাজরা", "ব্লাস্ট", "तना छेदक"),
            summaryEn = "Avoid excessive nitrogen top-dressing during active tillering to reduce Rice Blast. Install 5 pheromone traps/acre and use Trichoderma seed treatment before nursery sowing; follow CIBRC-approved doses only via KVK.",
            summaryBn = "ধানের কুশি ছাড়ার সময় অতিরিক্ত ইউরিয়া প্রয়োগ এড়িয়ে চলুন। একর প্রতি ৫টি ফেরোমন ফাঁদ ব্যবহার করুন এবং বীজতলার আগে ট্রাইকোডার্মা দিয়ে বীজ শোধন করুন।",
            summaryHi = "धान में कल्ले निकलते समय अधिक यूरिया न डालें। तना छेदक के लिए प्रति एकड़ 5 फेरोमोन ट्रैप लगाएं और ट्राइकोडर्मा से बीज उपचार करें।"
        ),
        VerifiedAgriKnowledgeRecord(
            id = "bckv_cpri_potato_late_blight",
            titleEn = "BCKV & ICAR-CPRI Potato Late Blight Prevention",
            titleBn = "বিসিকেভি ও সিপিআরআই আলুর নাবি ধসা প্রতিরোধ নির্দেশিকা",
            titleHi = "बीसीकेवी एवं सीपीआरआई आलू लेट ब्लाइट (झुलसा) सलाह",
            institution = "BCKV & ICAR-CPRI",
            sourceUrl = "https://cpri.icar.gov.in/",
            publicationDate = "2026-01-10",
            verifiedDate = "2026-11-17",
            category = "CROP_PROTECTION",
            applicableCropIds = listOf("potato"),
            regionTag = "West Bengal",
            keywords = listOf("potato", "আলু", "आलू", "late blight", "নাবি ধসা", "ঝुलसा", "झुलसा", "phytophthora", "fog"),
            summaryEn = "Prolonged winter fog (12-20 C, >85% RH) triggers Late Blight in potato. Use disease-free certified tubers, ridge earthing-up, proper field drainage, and consult local KVK for CIBRC-approved protective sprays before spread.",
            summaryBn = "শীতের কুয়াশা ও বেশি আর্দ্রতায় আলুর নাবি ধসা দ্রুত ছড়ায়। রোগমুক্ত বীজ আলু ব্যবহার, সময়মতো ভেলি বাঁধা ও জল নিকাশী নিশ্চিত করুন এবং প্রয়োজনে কেভিকে-র পরামর্শে অনুমোদিত ছত্রাকনাশক ব্যবহার করুন।",
            summaryHi = "कोहरे और अधिक नमी में आलू का लेट ब्लाइट तेजी से फैलता है। रोगमुक्त बीज, उचित मिट्टी चढ़ाना व जल निकासी रखें तथा केवीके की सलाह से अनुमोदित फफूंदनाशी का उपयोग करें।"
        ),
        VerifiedAgriKnowledgeRecord(
            id = "ubkv_terai_vegetables",
            titleEn = "UBKV North Bengal Cole Crops & Vegetable IPM",
            titleBn = "ইউবিকেভি উত্তরবঙ্গ সবজি ও কপি ফসল ব্যবস্থাপনা",
            titleHi = "यूबीकेवी उत्तर बंगाल सब्जी एवं गोभी वर्गीय फसल सलाह",
            institution = "UBKV Cooch Behar",
            sourceUrl = "https://www.ubkv.ac.in/",
            publicationDate = "2026-02-05",
            verifiedDate = "2026-11-17",
            category = "AGRONOMY",
            applicableCropIds = listOf("cabbage", "cauliflower", "broccoli", "tomato", "brinjal", "chilli"),
            regionTag = "Siliguri & North Bengal",
            keywords = listOf("cabbage", "cauliflower", "tomato", "brinjal", "chilli", "বাঁধাকপি", "ফুলকপি", "টমেটো", "বেগুন", "লঙ্কা", "गोभी", "टमाटर", "aphid", "diamondback", "blight", "curl"),
            summaryEn = "In acidic Terai/North Bengal soils, test soil pH and apply agricultural lime as per Soil Health Card before transplanting vegetables. Use Neem oil (Azadirachtin), yellow sticky traps, and observe 7-14 day Pre-Harvest Interval.",
            summaryBn = "উত্তরবঙ্গের অম্লীয় মাটিতে চারা রোপণের আগে মাটি পরীক্ষা করে চুন প্রয়োগ করুন। জাবপোকা ও সাদা মাছির জন্য নিম তেল এবং হলুদ আঠালো ফাঁদ ব্যবহার করুন।",
            summaryHi = "तराई क्षेत्र की अम्लीय मिट्टी में रोपाई से पहले मृदा परीक्षण के अनुसार चूना डालें। प्रारंभिक कीट नियंत्रण के लिए नीम तेल और पीले चिपचिपे ट्रैप लगाएं।"
        ),
        VerifiedAgriKnowledgeRecord(
            id = "tra_tocklai_tea_ipm",
            titleEn = "TRA Tocklai Tea Garden Drainage & Red Spider Mite Guide",
            titleBn = "টোকলাই চা গবেষণা কেন্দ্র — নিকাশী ও লাল মাকড় দমন",
            titleHi = "टीआरए टोकलाई चाय बागान जल निकासी एवं रेड स्पाइडर माइट गाइड",
            institution = "TRA Tocklai",
            sourceUrl = "https://www.tocklai.org/",
            publicationDate = "2026-03-12",
            verifiedDate = "2026-11-17",
            category = "PLANTATION",
            applicableCropIds = listOf("tea"),
            regionTag = "Darjeeling, Terai & Dooars",
            keywords = listOf("tea", "চা", "চায়ের", "चाय", "red spider", "blister blight", "mosquito bug", "helopeltis", "প্রুনিং"),
            summaryEn = "Maintain tea soil pH 4.5-5.5, clear deep drainage channels before monsoon to prevent Root Rot, conserve shade trees, and follow strict Tea Board PPC-approved spray rounds and plucking intervals.",
            summaryBn = "চা বাগানের মাটির pH ৪.৫–৫.৫ বজায় রাখুন, বর্ষার আগে নিকাশী নালা পরিষ্কার রাখুন এবং শুধুমাত্র অনুমোদিত PPC তালিকাভুক্ত নিরাপদ স্প্রে ও প্লাকিং বিরতি মেনে চলুন।",
            summaryHi = "चाय बागान की मिट्टी का pH 4.5-5.5 बनाए रखें, मानसून से पहले जल निकासी नालियाँ साफ करें और केवल अनुमोदित पीपीसी दिशानिर्देशों का पालन करें।"
        ),
        VerifiedAgriKnowledgeRecord(
            id = "icar_wheat_mustard_rabi",
            titleEn = "ICAR Wheat CRI Irrigation & Mustard Aphid Timing",
            titleBn = "আইসিএআর গমের সিআরআই সেচ ও সর্ষের জাবপোকা প্রতিরোধ",
            titleHi = "आईसीएआर गेहूं सिंचाई (CRI) एवं सरसों माहू नियंत्रण",
            institution = "ICAR-IIWBR & DRMR",
            sourceUrl = "https://icar.org.in/",
            publicationDate = "2025-11-20",
            verifiedDate = "2026-11-17",
            category = "IRRIGATION",
            applicableCropIds = listOf("wheat", "mustard", "maize"),
            regionTag = "Eastern India",
            keywords = listOf("wheat", "mustard", "গম", "সর্ষে", "সরিষা", "गेहूं", "सरसों", "cri", "irrigation", "সেচ", "জাবপোকা", "aphid", "माहू"),
            summaryEn = "Wheat first critical irrigation is Crown Root Initiation (CRI) at 20-25 days after sowing. Early mustard sowing by late October reduces winter aphid build-up; use neem oil before systemic sprays.",
            summaryBn = "গমের বপনের ২০–২৫ দিন পর (CRI পর্যায়ে) প্রথম সেচ দেওয়া অত্যন্ত জরুরি। সর্ষে সময়মতো বুনলে জাবপোকার আক্রমণ কম হয়।",
            summaryHi = "गेहूं में बुवाई के 20-25 दिन बाद सीआरआई (CRI) अवस्था पर पहली सिंचाई आवश्यक है। सरसों की समय पर बुवाई से माहू कीट का प्रकोप कम रहता है।"
        ),
        VerifiedAgriKnowledgeRecord(
            id = "cibrc_pesticide_safety",
            titleEn = "CIBRC & PPQS Safe Pesticide Label & PPE Protocol",
            titleBn = "সিআইবিআরসি নিরাপদ কীটনাশক লেবেল ও সুরক্ষা বিধি",
            titleHi = "सीआईबीआरसी सुरक्षित कीटनाशक लेबल एवं पीपीई दिशानिर्देश",
            institution = "CIBRC (DA&FW, Govt of India)",
            sourceUrl = "https://ppqs.gov.in/",
            publicationDate = "2026-01-01",
            verifiedDate = "2026-11-17",
            category = "SAFETY",
            applicableCropIds = listOf("rice_paddy", "potato", "tomato", "tea", "cabbage", "chilli", "brinjal", "mango"),
            regionTag = "All India",
            keywords = listOf("spray", "pesticide", "fungicide", "dose", "dosage", "phi", "ppe", "স্প্রে", "ওষুধ", "ডোজ", "दवा", "छिड़काव", "खुराक"),
            summaryEn = "Never guess pesticide doses. Use only CIBRC-registered formulations matching the target crop label claim, wear PPE (gloves/mask), observe Pre-Harvest Interval (PHI), and protect honeybees and water sources.",
            summaryBn = "কখনো অনুমান করে ওষুধ বা ডোজ দেবেন না। বোতলের লেবেল, পিপিই (মাস্ক ও গ্লাভস) এবং ফসল তোলার আগে অপেক্ষার দিন (PHI) অবশ্যই মেনে চলুন।",
            summaryHi = "बिना लेबल जांचें दवा या खुराक का अनुमान न लगाएं। छिड़काव के समय दस्ताने व मास्क पहनें और कटाई से पहले प्रतीक्षा अवधि (PHI) का पालन करें।"
        ),
        VerifiedAgriKnowledgeRecord(
            id = "dafw_soil_health_card",
            titleEn = "DA&FW Soil Health Card & Integrated Nutrient Management",
            titleBn = "ভারত সরকার মৃত্তিকা স্বাস্থ্য কার্ড ও সুষম সার ব্যবস্থাপনা",
            titleHi = "मृदा स्वास्थ्य कार्ड एवं संतुलित पोषक तत्व प्रबंधन",
            institution = "DA&FW Soil Health Portal",
            sourceUrl = "https://soilhealth.dac.gov.in/",
            publicationDate = "2026-04-01",
            verifiedDate = "2026-11-17",
            category = "SOIL_NUTRITION",
            applicableCropIds = listOf("rice_paddy", "potato", "tomato", "wheat", "mustard", "cabbage"),
            regionTag = "All India",
            keywords = listOf("soil", "ph", "npk", "urea", "dap", "mop", "compost", "deficiency", "yellow", "মাটি", "সার", "ইউরিয়া", "मिट्टी", "खाद", "उर्वरक"),
            summaryEn = "Test soil every 2 years. Apply full Phosphorus and Potassium at basal stage, split Nitrogen across active growth, and enrich soil organic carbon with 4-5 tonnes/ha well-decomposed FYM or vermicompost.",
            summaryBn = "প্রতি ২ বছর অন্তর মাটি পরীক্ষা করুন। ফসফরাস ও পটাশ মূল চাষের সময় প্রয়োগ করুন, ইউরিয়া ভাগ করে দিন এবং জৈব সার/কেঁচো সার ব্যবহার করুন।",
            summaryHi = "हर 2 वर्ष में मिट्टी की जांच कराएं। फास्फोरस और पोटाश की पूरी मात्रा बुवाई के समय दें तथा यूरिया को 2-3 किस्तों में गोबर/केंचुआ खाद के साथ दें।"
        )
    )
}
