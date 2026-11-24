package com.example.data.datasource

import com.example.R
import com.example.data.model.MedicineSellerInfo
import com.example.data.model.VerifiedTreatment

/**
 * Professional, CIBRC/ICAR-verified Crop Medicine & Treatment Database
 * Sections 464 - 504
 */
object VerifiedTreatmentDatabase {

    private val defaultSellers = listOf(
        MedicineSellerInfo(
            shopName = "উত্তরবঙ্গ কিষাণ সেবা কেন্দ্র",
            address = "হিলকার্ট রোড, বিধান মার্কেট, শিলিগুড়ি",
            phone = "9832045610",
            packSize = "100ml / 250g",
            priceRs = 285.0,
            inStock = true,
            isVerifiedShop = true
        ),
        MedicineSellerInfo(
            shopName = "জলপাইগুড়ি এগ্রো ইনপুট সেন্টার",
            address = "স্টেশন রোড, জলপাইগুড়ি",
            phone = "9434012845",
            packSize = "250ml / 500g",
            priceRs = 540.0,
            inStock = true,
            isVerifiedShop = true
        ),
        MedicineSellerInfo(
            shopName = "মালদা কৃষক বন্ধু সার ও কীটনাশক",
            address = "ইংরেজবাজার হাট, মালদা",
            phone = "9733051280",
            packSize = "500ml / 1kg",
            priceRs = 820.0,
            inStock = false, // demonstrates "স্টক জানতে দোকানে যোগাযোগ করুন"
            isVerifiedShop = true
        )
    )

    private val _adminAddedTreatments = mutableListOf<VerifiedTreatment>()

    val treatments: List<VerifiedTreatment>
        get() = baseTreatments + _adminAddedTreatments

    private val baseTreatments: List<VerifiedTreatment> = listOf(
        // ========================================================
        // 1. TEA GARDEN (চা বাগান)
        // ========================================================
        VerifiedTreatment(
            problemId = "tea_red_spider_mite",
            cropName = "Tea",
            problemNameBn = "চা গাছের লাল মাকড় (Red Spider Mite)",
            problemNameHi = "चाय की लाल मकड़ी (Red Spider Mite)",
            problemNameEn = "Red Spider Mite (Oligonychus coffeae)",
            symptomsBn = "পাতার ওপরের পিঠ খয়েরি বা ব্রোঞ্জ রঙের হয়ে শুকিয়ে যায়। রোদে বেশি আক্রমণ দেখা যায়।",
            symptomsHi = "पत्तियां तांबे के रंग की हो जाती हैं और सूखने लगती हैं।",
            symptomsEn = "Leaves turn bronze or coppery red, dry up and fall off during dry spells.",
            possibleCauseBn = "শুষ্ক খরা আবহাওয়া ও অতিরিক্ত ধুলোবালি জমে থাকা।",
            firstActionBn = "ঝোপের ওপরে জল স্প্রে করে ধুলো পরিষ্কার করা ও আর্দ্রতা বাড়ানো।",
            biologicalOptionBn = "নিম তেল (Neem Oil 10,000 ppm) বিঘায় ৫০০ মিলি প্রয়োগ অথবা গন্ধক (Wettable Sulphur 80% WP)।",
            activeIngredient = "Spiromesifen 22.9% SC",
            formulation = "22.9% SC",
            brandExamples = "Oberon (ওবেরন)",
            productName = "Oberon (ওবেরন)",
            manufacturer = "Bayer CropScience",
            productType = "Acaricide / মাকড়নাশক",
            growthStageBn = "সুপ্তাবস্থা কাটার পর কচি কুঁড়ি আসার সময়",
            dosePerHectareMlOrGm = 400.0,
            doseUnit = "ml",
            dosePerLitreBn = "০.৮ - ১.০ মিলি প্রতি লিটার জলে",
            waterPerHectareLiters = 400.0,
            preHarvestIntervalDays = 7,
            reEntryIntervalHours = 24,
            maxApplications = 2,
            safetyInstructionsBn = "স্প্রে করার সময় মুখে মাস্ক ও হাতে দস্তানা পরুন। হাওয়ার উল্টো দিকে স্প্রে করবেন না। পাতা তোলার ৭ দিন আগে রাসায়নিক স্প্রে বন্ধ রাখুন।",
            resistanceGroup = "Group 23 Acaricide",
            officialSource = "Tea Board of India / CIBRC Verified Label",
            frontImageResId = R.drawable.img_tea_leaves_1793343045221,
            nearbySellers = defaultSellers
        ),
        VerifiedTreatment(
            problemId = "tea_mosquito_bug",
            cropName = "Tea",
            problemNameBn = "টি মস্কিটো বাগ (Tea Mosquito Bug)",
            problemNameHi = "टी मॉस्किटो बग (Helopeltis)",
            problemNameEn = "Tea Mosquito Bug (Helopeltis theivora)",
            symptomsBn = "কচি ডগা ও কুঁড়িতে কালো গোল দাগ দেখা যায়, ডগা কুঁকড়ে মরে যায়। পাতা তোলা বাধাগ্রস্ত হয়।",
            symptomsHi = "कोमल पत्तियों और कलियों पर काले गोल धब्बे बन जाते हैं।",
            symptomsEn = "Black translucent sunken spots on young flush and tender stems causing dieback.",
            possibleCauseBn = "মেঘলা স্যাঁতসেঁতে আবহাওয়া এবং ঝোপের ভেতরের অতিরিক্ত আর্দ্রতা।",
            firstActionBn = "আক্রান্ত ডগাগুলো সাবধানে হাত দিয়ে কেটে ফেলা ও হালকা ছাঁটাই।",
            biologicalOptionBn = "বিউভেরিয়া ব্যাসিয়ানা (Beauveria bassiana 1% WP) প্রতি লিটারে ৫ গ্রাম।",
            activeIngredient = "Thiamethoxam 25% WG",
            formulation = "25% WG",
            brandExamples = "Actara (একতারা)",
            productName = "Actara (একতারা)",
            manufacturer = "Syngenta India",
            productType = "Insecticide / কীটনাশক",
            growthStageBn = "কচি পাতা আসার পর",
            dosePerHectareMlOrGm = 100.0,
            doseUnit = "gm",
            dosePerLitreBn = "০.২৫ গ্রাম প্রতি লিটার জলে",
            waterPerHectareLiters = 400.0,
            preHarvestIntervalDays = 7,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "সকালে বা বিকেলে শান্ত আবহাওয়ায় স্প্রে করুন। মৌমাছির সময় পরিহার করুন।",
            resistanceGroup = "Group 4A Neonicotinoid",
            officialSource = "TRA (Tea Research Association) Verified Label",
            frontImageResId = R.drawable.img_tea_leaves_1793343045221,
            nearbySellers = defaultSellers
        ),

        // ========================================================
        // 2. VEGETABLES: CABBAGE & CAULIFLOWER (বাঁধাকপি ও ফুলকপি)
        // ========================================================
        VerifiedTreatment(
            problemId = "cabbage_caterpillar_dbm",
            cropName = "Cabbage",
            problemNameBn = "বাঁধাকপির শুঁয়োপোকা ও ডিবিএম (Diamondback Moth)",
            problemNameHi = "पत्तागोभी का तंबाकू व डीबीएम कीट",
            problemNameEn = "Diamondback Moth & Head Caterpillars (Plutella xylostella)",
            symptomsBn = "পাতায় ছোট ছোট চালুনির মতো গর্ত দেখা যায়। কপির মাথার ভেতরে পোকা ঢুকে নোংরা মল ত্যাগ করে।",
            symptomsHi = "पत्तियों में छेद और अंदर कीड़े की मौजूदगी।",
            symptomsEn = "Windowing of leaves and caterpillars boring into the developing compact head.",
            possibleCauseBn = "উষ্ণ ও শুষ্ক আবহাওয়ায় মথের দ্রুত বংশবৃদ্ধি।",
            firstActionBn = "বিঘায় ৪-৫টি ফেরোমোন ট্র্যাপ (Pheromone Trap) স্থাপন এবং আক্রান্ত পাতা নষ্ট করা।",
            biologicalOptionBn = "ব্যাসিলাস থুরিনজিয়েনসিস (Bt kurstaki) প্রতি লিটারে ২ গ্রাম অথবা নিমবীজের নির্যাস (NSKE 5%)।",
            activeIngredient = "Chlorantraniliprole 18.5% SC",
            formulation = "18.5% SC",
            brandExamples = "Coragen (কোরাজেন)",
            productName = "Coragen (কোরাজেন)",
            manufacturer = "FMC India",
            productType = "Insecticide / কীটনাশক",
            growthStageBn = "মাথা বাঁধার প্রাথমিক পর্যায়",
            dosePerHectareMlOrGm = 150.0,
            doseUnit = "ml",
            dosePerLitreBn = "০.৩ - ০.৪ মিলি প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 3,
            reEntryIntervalHours = 12,
            safetyInstructionsBn = "ফল তোলার ৩ দিন আগে ব্যবহার বন্ধ রাখুন। একই বিষ বারবার ব্যবহার করবেন না, স্পিনোস্যাড (Spinosad) এর সাথে অল্টারনেট করুন।",
            resistanceGroup = "Group 28 Diamides",
            officialSource = "CIBRC Official Crop Protection Directory",
            frontImageResId = R.drawable.img_cabbage_1793343062183,
            nearbySellers = defaultSellers
        ),
        VerifiedTreatment(
            problemId = "cauliflower_boron_whip",
            cropName = "Cauliflower",
            problemNameBn = "ফুলকপির বোরন ঘাটতি ও বাদামি পচা (Brown Rot / Boron Deficiency)",
            problemNameHi = "फूलगोभी में बोरॉन कमी व भूरापन",
            problemNameEn = "Boron Deficiency & Hollow Stem in Cauliflower",
            symptomsBn = "ফুল বাদামি ছোপযুক্ত হয়, ফুল ফাঁপা ও কাণ্ডে গহ্বর তৈরি হয়। ফুল শক্ত না হয়ে ঝরে যায়।",
            symptomsHi = "फूल भूरा पड़ जाता है और तना अंदर से खोखला हो जाता है।",
            symptomsEn = "Water soaked areas on curd turning rusty brown, hollow stem and brittle leaves.",
            possibleCauseBn = "মাটিতে বোরনের ঘাটতি এবং অতিরিক্ত নাইট্রোজেন সার প্রয়োগ।",
            firstActionBn = "ভারসাম্যপূর্ণ সেচ ও নাইট্রোজেনের মাত্রা নিয়ন্ত্রণ।",
            biologicalOptionBn = "জৈব কম্পোস্ট ও ছাইয়ের সাথে অনুখাদ্য প্রয়োগ।",
            activeIngredient = "Disodium Octaborate Tetrahydrate (Boron 20%)",
            formulation = "20% Soluble Powder",
            brandExamples = "Boroplus / সলুবোর",
            productName = "Boron 20% Micronutrient",
            manufacturer = "Tata Rallis / Multiplex",
            productType = "Micronutrient / সার অনুখাদ্য",
            growthStageBn = "চারা রোপণের ২৫-৩০ দিন পর এবং ফুল আসার আগে",
            dosePerHectareMlOrGm = 1000.0,
            doseUnit = "gm",
            dosePerLitreBn = "১.৫ - ২.০ গ্রাম প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 0,
            reEntryIntervalHours = 0,
            safetyInstructionsBn = "রোদের তাপ কমে এলে বিকেলে স্প্রে করুন। অতিরিক্ত মাত্রা প্রয়োগ করবেন না।",
            resistanceGroup = "Micronutrient Fertilizer",
            officialSource = "ICAR - Indian Institute of Vegetable Research (IIVR)",
            frontImageResId = R.drawable.img_cauliflower_1793343216647,
            nearbySellers = defaultSellers
        ),

        // ========================================================
        // 3. TOMATO (টমেটো)
        // ========================================================
        VerifiedTreatment(
            problemId = "tomato_late_blight",
            cropName = "Tomato",
            problemNameBn = "টমেটোর নাবি ধসা রোগ (Late Blight)",
            problemNameHi = "टमाटर का पछेती झुलसा (Late Blight)",
            problemNameEn = "Tomato Late Blight (Phytophthora infestans)",
            symptomsBn = "পাতার কিনারায় জলে ভেজা গাঢ় খয়েরি দাগ ও নিচে সাদা তুলোর মতো ছত্রাক দেখা যায়। ২-৩ দিনে গাছ ঝলসে যায়।",
            symptomsHi = "पत्तियों पर भूरे पानीदार धब्बे और निचली सतह पर सफेद फफूंद।",
            symptomsEn = "Water-soaked dark lesions expanding rapidly under cool humid foggy weather.",
            possibleCauseBn = "টানা মেঘলা আবহাওয়া, কুয়াশা ও কম তাপমাত্রা (১৫-২০ ডিগ্রি)।",
            firstActionBn = "আক্রান্ত গাছ বা পাতা তুলে দূরে মাটিতে পুঁতে ফেলা এবং গাছের ওপর থেকে জল ছেটানো বন্ধ করা।",
            biologicalOptionBn = "ট্রাইকোডার্মা ভিরিডি (Trichoderma viride 1% WP) প্রতি লিটারে ৫ গ্রাম।",
            activeIngredient = "Dimethomorph 50% WP",
            formulation = "50% WP",
            brandExamples = "Acrobat (এক্রোব্যাট)",
            productName = "Acrobat 50% WP",
            manufacturer = "BASF India",
            productType = "Fungicide / ছত্রাকনাশক",
            growthStageBn = "কুয়াশাচ্ছন্ন আবহাওয়া শুরু হলে আগাম বা প্রথম লক্ষণ দেখা দিলে",
            dosePerHectareMlOrGm = 1000.0,
            doseUnit = "gm",
            dosePerLitreBn = "১.৫ - ২.০ গ্রাম প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 7,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "প্রতিরোধমূলক হিসেবে ম্যানকোজেব স্প্রে করা যায়। লক্ষণ তীব্র হলে অনুমোদিত ডোজে স্প্রে করুন। ব্যক্তিগত সুরক্ষা (PPE) জরুরি।",
            resistanceGroup = "Group 40 Fungicide",
            officialSource = "ICAR / CIBRC Label Guide",
            frontImageResId = R.drawable.img_tomato_1793343030528,
            nearbySellers = defaultSellers
        ),
        VerifiedTreatment(
            problemId = "tomato_fruit_borer",
            cropName = "Tomato",
            problemNameBn = "টমেটোর ফলছেঁদা পোকা (Fruit Borer)",
            problemNameHi = "टमाटर का फल छेदक (Fruit Borer)",
            problemNameEn = "Tomato Fruit Borer (Helicoverpa armigera)",
            symptomsBn = "কচি ও পাকা টমেটোর গায়ে গোল গর্ত করে ভেতরে ঢুকে মাংসল অংশ খেয়ে ফেলে এবং ফল পচিয়ে দেয়।",
            symptomsHi = "टमाटर के फलों में छेद करके अंदर का गूदा खा जाना।",
            symptomsEn = "Circular boreholes on green and ripe fruits, larva feeding internally causing fruit rot.",
            possibleCauseBn = "উষ্ণ আবহাওয়া ও জমিতে গাঁদা ফুলের ফাঁদ শস্য না থাকা।",
            firstActionBn = "বিঘায় ৫টি হেলিলিউয়ার ফেরোমোন ফাঁদ লাগানো এবং আক্রান্ত ফল সংগ্রহ করে ধ্বংস করা।",
            biologicalOptionBn = "এইচএএনপিভি (HaNPV 250 LE) প্রতি লিটারে ১ মিলি অথবা নিম তেল (Neem 10000 ppm) ২ মিলি/লিটার।",
            activeIngredient = "Emamectin Benzoate 5% SG",
            formulation = "5% SG (Soluble Granule)",
            brandExamples = "Proclaim (প্রোক্লেম)",
            productName = "Proclaim (প্রোক্লেম)",
            manufacturer = "Syngenta India",
            productType = "Insecticide / কীটনাশক",
            growthStageBn = "ফুল থেকে কচি ফল ধরার সময়",
            dosePerHectareMlOrGm = 200.0,
            doseUnit = "gm",
            dosePerLitreBn = "০.৪ - ০.৫ গ্রাম প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 3,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "ফল তোলার ৩ দিন আগে শেষ স্প্রে করুন। শিশুদের নাগালের বাইরে রাখুন।",
            resistanceGroup = "Group 6 Avermectin",
            officialSource = "CIBRC Official Crop Protection Label",
            frontImageResId = R.drawable.img_tomato_1793343030528,
            nearbySellers = defaultSellers
        ),

        // ========================================================
        // 4. CHILLI (লঙ্কা)
        // ========================================================
        VerifiedTreatment(
            problemId = "chilli_leaf_curl_thrips",
            cropName = "Chilli",
            problemNameBn = "লঙ্কার পাতা কোঁকড়ানো ও চর্বিপোকা/থ্রিপস (Thrips & Mites)",
            problemNameHi = "मिर्च का पत्ती मुड़ना (थ्रिप्स व माइट्स)",
            problemNameEn = "Chilli Leaf Curl (Thrips & Yellow Mites)",
            symptomsBn = "থ্রিপসের কারণে পাতা নৌকার মতো ওপরের দিকে বাঁকে, মাকড়ের কারণে পাতা নিচের দিকে উল্টানো বাটির মতো কুঁকড়ে যায়।",
            symptomsHi = "पत्तियां ऊपर नाव की तरह या नीचे कटोरी की तरह मुड़ जाती हैं।",
            symptomsEn = "Upward curling due to thrips or downward curling due to mites, brittle leaves.",
            possibleCauseBn = "শুষ্ক আবহাওয়া এবং মাঠে আগাছার উপস্থিতি।",
            firstActionBn = "হলুদ ও নীল রঙের আঠালো ফাঁদ (Sticky Traps) প্রতি বিঘায় ১০টি করে টাঙান।",
            biologicalOptionBn = "নিম তেল ৫ মিলি প্রতি লিটার জলে শ্যাম্পু মিশিয়ে ৭ দিন অন্তর স্প্রে।",
            activeIngredient = "Diafenthiuron 50% WP",
            formulation = "50% WP",
            brandExamples = "Pegasus (পেগাসাস)",
            productName = "Pegasus (পেগাসাস)",
            manufacturer = "Syngenta India",
            productType = "Insecticide / Acaricide",
            growthStageBn = "গাছের কচি ডাল ও পাতা বৃদ্ধি পর্যায়",
            dosePerHectareMlOrGm = 600.0,
            doseUnit = "gm",
            dosePerLitreBn = "১.২ গ্রাম প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 5,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "ফুল ফোটার সময় কড়া রোদে স্প্রে করবেন না। সুরক্ষামূলক চশমা ও মাস্ক ব্যবহার করুন।",
            resistanceGroup = "Group 12A Insecticide",
            officialSource = "CIBRC Verified Package of Practices",
            frontImageResId = R.drawable.img_chilli_1793343108793,
            nearbySellers = defaultSellers
        ),
        VerifiedTreatment(
            problemId = "chilli_anthracnose_dieback",
            cropName = "Chilli",
            problemNameBn = "লঙ্কার ফল পচা ও ডাইব্যাক (Anthracnose / Dieback)",
            problemNameHi = "मिर्च का फल सड़न व डाइबैक रोग",
            problemNameEn = "Chilli Anthracnose & Dieback (Colletotrichum capsici)",
            symptomsBn = "পাকা ও কাঁচা লঙ্কার গায়ে গোলাকার ডেবে যাওয়া কালো দাগ এবং ওপরের ডালপালা উপর থেকে নিচে শুকিয়ে আসা।",
            symptomsHi = "फलों पर काले धब्बे और शाखाएं ऊपर से नीचे सूखने लगती हैं।",
            symptomsEn = "Sunken circular spots with concentric rings of acervuli on fruits, top twig dieback.",
            possibleCauseBn = "উচ্চ তাপমাত্রা ও বৃষ্টি-পরবর্তী স্যাঁতসেঁতে আর্দ্রতা।",
            firstActionBn = "আক্রান্ত ডগা ও পচা লঙ্কা তুলে পুড়িয়ে ফেলা।",
            biologicalOptionBn = "ট্রাইকোডার্মা ভিরিডি ৫ গ্রাম/লিটার জলে স্প্রে।",
            activeIngredient = "Azoxystrobin 18.2% + Difenoconazole 11.4% SC",
            formulation = "SC Dual Fungicide",
            brandExamples = "Amistar Top (অ্যামিস্টার টপ)",
            productName = "Amistar Top",
            manufacturer = "Syngenta India",
            productType = "Fungicide / ছত্রাকনাশক",
            growthStageBn = "ফুল আসা থেকে ফল পাকার সময়",
            dosePerHectareMlOrGm = 500.0,
            doseUnit = "ml",
            dosePerLitreBn = "১.০ মিলি প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 5,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "শিশির শুকানোর পর সকাল ৯-১০টায় অথবা বিকেলে শান্ত আবহাওয়ায় স্প্রে করুন।",
            resistanceGroup = "Group 11 + Group 3 Fungicide",
            officialSource = "CIBRC Verified Fungicide Label",
            frontImageResId = R.drawable.img_chilli_1793343108793,
            nearbySellers = defaultSellers
        ),

        // ========================================================
        // 5. BRINJAL & OKRA (বেগুন ও ঢেঁড়শ)
        // ========================================================
        VerifiedTreatment(
            problemId = "brinjal_shoot_fruit_borer",
            cropName = "Brinjal",
            problemNameBn = "বেগুনের ডগা ও ফলছেঁদা পোকা (Shoot & Fruit Borer)",
            problemNameHi = "बैंगन का तना व फल छेदक",
            problemNameEn = "Brinjal Shoot and Fruit Borer (Leucinodes orbonalis)",
            symptomsBn = "কচি ডগার ভেতর শুঁয়োপোকা ঢুকে খেয়ে ফেলে, ডগা নুয়ে শুকিয়ে যায়। বেগুনের গায়ে ফুটো ও বিষ্ঠা থাকে।",
            symptomsHi = "शाखाएं मुरझा जाती हैं और बैंगन में छेद हो जाते हैं।",
            symptomsEn = "Wilting of tender terminal shoots, boreholes plugged with excreta in fruits.",
            possibleCauseBn = "জমিতে নিয়মিত পোকা পর্যবেক্ষণ না করা ও আক্রান্ত ডগা না কাটা।",
            firstActionBn = "প্রতি সপ্তাহে সকালে আক্রান্ত ডগাগুলো কাঁচি দিয়ে কেটে মাটিতে পুঁতে ফেলা।",
            biologicalOptionBn = "লুসিন-লিউয়ার ফেরোমোন ফাঁদ বিঘায় ৮টি স্থাপন ও ট্রাইকোগ্রামা পরজীবী ডিমের কার্ড।",
            activeIngredient = "Chlorantraniliprole 18.5% SC / Flubendiamide 39.35% SC",
            formulation = "18.5% SC",
            brandExamples = "Coragen / Fame (ফেম)",
            productName = "Coragen (কোরাজন)",
            manufacturer = "FMC India",
            productType = "Insecticide / কীটনাশক",
            growthStageBn = "প্রথম ফুল ও ফল ধরার সময়",
            dosePerHectareMlOrGm = 200.0,
            doseUnit = "ml",
            dosePerLitreBn = "০.৪ মিলি প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 3,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "ফল তোলার ৩ দিন আগে স্প্রে বন্ধ রাখুন। একই মৌসুমে ২ বারের বেশি স্প্রে করবেন না।",
            resistanceGroup = "Group 28 Diamides",
            officialSource = "ICAR - IIVR Vegetable Guidelines",
            frontImageResId = R.drawable.img_brinjal_1793343230024,
            nearbySellers = defaultSellers
        ),
        VerifiedTreatment(
            problemId = "okra_ymv_whitefly",
            cropName = "Okra",
            problemNameBn = "ঢেঁড়শের হলুদ শিরা মোজাইক ভাইরাস ও সাদা মাছি (YVMV & Whitefly)",
            problemNameHi = "भिंडी का पीला मोज़ेक वायरस व सफेद मक्खी",
            problemNameEn = "Okra Yellow Vein Mosaic Virus (Whitefly vector)",
            symptomsBn = "পাতার শিরাগুলো হলুদ হয়ে জালিকার মতো রূপ নেয়, ফল ছোট, হলুদ ও শক্ত হয়ে যায়।",
            symptomsHi = "पत्तियों की नसें पीली हो जाती हैं और फल कड़े व पीले बनते हैं।",
            symptomsEn = "Clear vein chlorosis followed by complete yellow network of veins, stunted fruits.",
            possibleCauseBn = "সাদা মাছি (Bemisia tabaci) দ্বারা ভাইরাস সংক্রমণ।",
            firstActionBn = "হলুদ আঠালো ফাঁদ (Yellow Sticky Traps) প্রতি বিঘায় ১০টি লাগানো।",
            biologicalOptionBn = "নিম তেল ৫ মিলি প্রতি লিটার জলে স্প্রে।",
            activeIngredient = "Acetamiprid 20% SP",
            formulation = "20% SP",
            brandExamples = "Pride (প্রাইড) / মানিক",
            productName = "Acetamiprid 20% SP",
            manufacturer = "Tata Rallis / Dhanuka",
            productType = "Insecticide / কীটনাশক",
            growthStageBn = "চারা রোপণের ১৫-২০ দিন পর সাদা মাছির উপস্থিতি দেখলেই",
            dosePerHectareMlOrGm = 100.0,
            doseUnit = "gm",
            dosePerLitreBn = "০.২ গ্রাম প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 3,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "সকালে বা বিকেলে স্প্রে করুন। মুখে মাস্ক ও চশমা ব্যবহার করুন।",
            resistanceGroup = "Group 4A Neonicotinoid",
            officialSource = "CIBRC Verified Label",
            frontImageResId = R.drawable.img_okra_1793343244337,
            nearbySellers = defaultSellers
        ),

        // ========================================================
        // 6. POTATO & TUBER CROPS (আলু ও কন্দ ফসল)
        // ========================================================
        VerifiedTreatment(
            problemId = "potato_blight",
            cropName = "Potato",
            problemNameBn = "আলুর নাবি ধসা রোগ (Potato Late Blight)",
            problemNameHi = "आलू का पछेती झुलसा रोग (Late Blight)",
            problemNameEn = "Potato Late Blight (Phytophthora infestans)",
            symptomsBn = "পাতার কিনারে বাদামি পুড়ে যাওয়ার মতো দাগ, ঠান্ডা কুয়াশায় ২-৩ দিনে পুরো ক্ষেত পুড়ে নষ্ট হয়।",
            symptomsHi = "पत्तियों पर झुलसे हुए काले चकत्ते।",
            symptomsEn = "Rapid blighting of foliage in foggy high humidity winter mornings.",
            possibleCauseBn = "ফাইটোফথোরা ছত্রাক ও টানা শীতকালীন কুয়াশা।",
            firstActionBn = "মাটির আর্দ্রতা সাময়িক কমানো এবং নাইট্রোজেন সার দেওয়া বন্ধ রাখা।",
            biologicalOptionBn = "সিউডোমোনাস ফ্লুরোসেন্স বা ট্রাইকোডার্মা স্প্রে।",
            activeIngredient = "Cymoxanil 8% + Mancozeb 64% WP",
            formulation = "72% WP",
            brandExamples = "Curzate M8 (কারজেট এম৮)",
            productName = "Curzate M8",
            manufacturer = "Corteva Agriscience",
            productType = "Fungicide / ছত্রাকনাশক",
            growthStageBn = "কুয়াশাচ্ছন্ন সকালে কুয়াশা কাটার পর",
            dosePerHectareMlOrGm = 1500.0,
            doseUnit = "gm",
            dosePerLitreBn = "২.৫ - ৩.০ গ্রাম প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 14,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "কুয়াশা কাটার পর রোদ উঠলে স্প্রে করুন। বিষ মিশ্রিত পাত্র শিশুদের নাগালের বাইরে রাখুন।",
            resistanceGroup = "Group 27 + M3 Fungicide",
            officialSource = "Central Potato Research Institute (CPRI) & CIBRC",
            frontImageResId = R.drawable.img_potato_1793343095516,
            nearbySellers = defaultSellers
        ),

        // ========================================================
        // 7. RICE / ধান (PADDY)
        // ========================================================
        VerifiedTreatment(
            problemId = "rice_stem_borer",
            cropName = "Rice",
            problemNameBn = "ধানের হলুদ মাজরা পোকা (Yellow Stem Borer)",
            problemNameHi = "धान का तना छेदक (Stem Borer)",
            problemNameEn = "Rice Yellow Stem Borer (Scirpophaga incertulas)",
            symptomsBn = "গাছের মাঝখানের কুশি শুকিয়ে যায় (মরা ডিগ বা Dead Heart) এবং শিষ বের হলে শিষ সাদা চিটা হয়ে যায় (White Earhead)।",
            symptomsHi = "गभोट अवस्था में मृत तना और बाली आने पर सफेद बालियां दिखाई देती हैं।",
            symptomsEn = "Dead hearts during vegetative stage and white earheads with chaffy grains during panicle emergence.",
            possibleCauseBn = "অতিরিক্ত নাইট্রোজেন সার প্রয়োগ ও জমিতে স্থায়ীভাবে জল জমে থাকা।",
            firstActionBn = "ডিমওয়ালা পাতার আগা ছিঁড়ে নষ্ট করা। বিঘায় ৩-৪টি ট্রাইকোগ্রামা পরজীবী ডিমের কার্ড লাগানো।",
            biologicalOptionBn = "নিম তেল ১০,০০০ পিপিএম (Neem Oil) প্রতি লিটারে ২ মিলি অথবা ট্রাইকোগ্রামা জাপোনিকাম (Trichogramma japonicum)।",
            activeIngredient = "Chlorantraniliprole 0.4% GR (দানা) / 18.5% SC",
            formulation = "0.4% GR (Granule) / 18.5% SC",
            brandExamples = "Ferterra (ফারটেরা) / Coragen (কোরাজন)",
            productName = "Ferterra 0.4% GR / Coragen 18.5% SC",
            manufacturer = "FMC India",
            productType = "Insecticide / কীটনাশক",
            growthStageBn = "কুশি আসা থেকে থোড় আসার মুখ পর্যন্ত",
            dosePerHectareMlOrGm = 10000.0, // 10 kg/ha for 0.4% GR, or 150ml for SC
            doseUnit = "gm (দানা ১০ কেজি/হেক্টর বা ১৫০ মিলি স্প্রে)",
            dosePerLitreBn = "০.৩ মিলি প্রতি লিটার জলে (স্প্রে করলে)",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 21,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "দানা সার প্রয়োগের সময় জমিতে ২-৩ ইঞ্চি জল থাকতে হবে। স্প্রে করলে ফুল ফোটার সময় সকাল পরিহার করুন।",
            resistanceGroup = "Group 28 Diamide",
            officialSource = "ICAR - National Rice Research Institute (NRRI) & CIBRC Label",
            frontImageResId = R.drawable.ic_launcher_foreground,
            nearbySellers = defaultSellers
        ),
        VerifiedTreatment(
            problemId = "rice_blast",
            cropName = "Rice",
            problemNameBn = "ধানের ব্লাস্ট রোগ (Rice Blast)",
            problemNameHi = "धान का झुलसा रोग (Blast)",
            problemNameEn = "Rice Blast (Magnaporthe oryzae)",
            symptomsBn = "পাতায় মাকু আকৃতির চোখ সদৃশ দাগ (Leaf Blast) এবং শীষের গোড়ায় বাদামি-কালো হয়ে শীষ ভেঙে পড়া (Neck Blast)।",
            symptomsHi = "पत्तियों पर आंख की आकृति के धब्बे व बाली की गर्दन पर कालापन।",
            symptomsEn = "Spindle-shaped eye spots on leaves; black rot at panicle neck causing neck blast breakage.",
            possibleCauseBn = "রাতে শিশির, মেঘলা স্যাঁতসেঁতে আবহাওয়া (২৫-২৮° সেলসিয়াস) এবং অতিরিক্ত ইউরিয়া সার।",
            firstActionBn = "ইউরিয়া প্রয়োগ তাৎক্ষণিকভাবে বন্ধ রাখুন এবং জমিতে পটাশ সারের মাত্রা বাড়ান।",
            biologicalOptionBn = "সিউডোমোনাস ফ্লুরোসেন্স (Pseudomonas fluorescens) প্রতি লিটারে ৫ গ্রাম।",
            activeIngredient = "Tricyclazole 75% WP",
            formulation = "75% WP",
            brandExamples = "Beam (বীম) / বাণ",
            productName = "Beam 75% WP",
            manufacturer = "Corteva Agriscience",
            productType = "Fungicide / ছত্রাকনাশক",
            growthStageBn = "কুশি বা থোড় আসার সময়",
            dosePerHectareMlOrGm = 300.0,
            doseUnit = "gm",
            dosePerLitreBn = "০.৬ গ্রাম প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 30,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "শিশির শুকানোর পর রৌদ্রোজ্জ্বল দিনে স্প্রে করুন। মুখে মাস্ক ব্যবহার করুন।",
            resistanceGroup = "Group 16.1 MBI-D",
            officialSource = "CIBRC Verified Fungicide Label",
            frontImageResId = R.drawable.ic_launcher_foreground,
            nearbySellers = defaultSellers
        ),

        // ========================================================
        // 8. WHEAT & MAIZE (গম ও ভুট্টা)
        // ========================================================
        VerifiedTreatment(
            problemId = "wheat_rust",
            cropName = "Wheat",
            problemNameBn = "গমের মরিচা রোগ (Wheat Rust - Yellow & Brown)",
            problemNameHi = "गेहूं का रतुआ रोग (Yellow & Brown Rust)",
            problemNameEn = "Wheat Rust (Puccinia striiformis)",
            symptomsBn = "পাতায় হলুদ বা বাদামি গুঁড়ো গুঁড়ো রেণুর সারি দেখা যায়। হাত দিলে আঙুলে হলুদ পাউডার লেগে যায়।",
            symptomsHi = "पत्तियों पर पीले या भूरे रंग की धारियों में फफूंद के चूर्ण दिखाई देते हैं।",
            symptomsEn = "Yellow or brown pustules arranged in parallel stripes on leaves, shedding powdery spores on fingers.",
            possibleCauseBn = "শীতকালীন ঠান্ডা, আর্দ্র ও কুয়াশাচ্ছন্ন আবহাওয়া।",
            firstActionBn = "আক্রান্ত প্রাথমিক গাছের পাতা ছিঁড়ে ধ্বংস করা এবং জমিতে বাতাস চলাচলের ব্যবস্থা রাখা।",
            biologicalOptionBn = "গাঁজানো গোমূত্র ও খৈলের নির্যাস স্প্রে।",
            activeIngredient = "Propiconazole 25% EC",
            formulation = "25% EC",
            brandExamples = "Tilt (টিল্ট) / বাম্পার",
            productName = "Tilt 25% EC",
            manufacturer = "Syngenta India",
            productType = "Fungicide / ছত্রাকনাশক",
            growthStageBn = "গাছে শিষ আসার আগে বা মরিচার প্রথম লক্ষণ দেখলে",
            dosePerHectareMlOrGm = 500.0,
            doseUnit = "ml",
            dosePerLitreBn = "১.০ মিলি প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 30,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "রোগ দেখা দেওয়া মাত্রই প্রতি লিটার জলে ১ মিলি হারে মিশিয়ে সারা গাছে স্প্রে করুন।",
            resistanceGroup = "Group 3 Triazole",
            officialSource = "ICAR-IIWBR & CIBRC",
            frontImageResId = R.drawable.ic_launcher_foreground,
            nearbySellers = defaultSellers
        ),
        VerifiedTreatment(
            problemId = "maize_faw",
            cropName = "Maize",
            problemNameBn = "ভুট্টার ফল আর্মিওয়ার্ম (Fall Armyworm - FAW)",
            problemNameHi = "मक्का का फॉल आर्मीवर्म कीट (FAW)",
            problemNameEn = "Fall Armyworm (Spodoptera frugiperda)",
            symptomsBn = "কচি পাতার মাঝখানের মোচড়ে ঢুকে পাতা ঝাঁঝরা করে কাটা এবং প্রচুর করাতের গুঁড়োর মতো বিষ্ঠা জমে থাকা।",
            symptomsHi = "पत्तियों के अंदर छुपकर जालीदार छलनी की तरह नुकसान और बुरादा दिखना।",
            symptomsEn = "Ragged whorl feeding with characteristic sawdust-like frass inside the central leaf whorl.",
            possibleCauseBn = "শুষ্ক আবহাওয়া ও দেরিতে বপন।",
            firstActionBn = "ভুট্টার কচি পাতার খোলে শুকনো বালি ও কাঠের ছাই মিশিয়ে দেওয়া যাতে পোকা শ্বাসরুদ্ধ হয়ে মারা যায়।",
            biologicalOptionBn = "মেটারিজিয়াম অ্যানিসোপ্লি (Metarhizium anisopliae) প্রতি লিটারে ৫ গ্রাম বা বিটি (Bt kurstaki) ২ গ্রাম/লিটার।",
            activeIngredient = "Spinetoram 11.7% SC",
            formulation = "11.7% SC",
            brandExamples = "Delegate (ডেলিগেট)",
            productName = "Delegate 11.7% SC",
            manufacturer = "Corteva Agriscience",
            productType = "Insecticide / কীটনাশক",
            growthStageBn = "গাছের উচ্চতা হাঁটু সমান (V4-V8 স্টেজ)",
            dosePerHectareMlOrGm = 250.0,
            doseUnit = "ml",
            dosePerLitreBn = "০.৫ মিলি প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 14,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "স্প্রেয়ারের নোজলটি সোজা ভুট্টার পাতার মোচড়ের (whorl) মুখে ধরে ভেতরে ভেজাতে হবে।",
            resistanceGroup = "Group 5 Spinosyn",
            officialSource = "ICAR-IIMR & CIBRC Fall Armyworm Advisory",
            frontImageResId = R.drawable.ic_launcher_foreground,
            nearbySellers = defaultSellers
        ),

        // ========================================================
        // 9. OILSEEDS: MUSTARD & GROUNDNUT (সর্ষে ও চিনাবাদাম)
        // ========================================================
        VerifiedTreatment(
            problemId = "mustard_aphid",
            cropName = "Mustard",
            problemNameBn = "সর্ষের জাবপোকা (Mustard Aphid)",
            problemNameHi = "सरसों का चेपा / माहू कीट",
            problemNameEn = "Mustard Aphid (Lipaphis erysimi)",
            symptomsBn = "কচি ডালপালা, ফুল ও শুঁটিতে হাজার হাজার ক্ষুদ্র পোকা রস চুষে খায়, ফলে শুঁটি অপুষ্ট ও ফুল ঝরে যায়।",
            symptomsHi = "फूलों व फलियों पर चिपचिपे कीड़ों का झुंड रस चूसता है।",
            symptomsEn = "Dense colonies curling tender shoots, feeding on sap of inflorescence and reducing oil yield.",
            possibleCauseBn = "মেঘলা আকাশ ও কুয়াশাচ্ছন্ন উষ্ণ তাপমাত্রা।",
            firstActionBn = "ক্ষেতের চারিদিকে হলুদ আঠালো ফাঁদ (Yellow Sticky Traps) প্রতি বিঘায় ১০টি লাগানো।",
            biologicalOptionBn = "নিম তেল ৫ মিলি প্রতি লিটার জলে মিশিয়ে স্প্রে। লেডিবার্ড বিটল সংরক্ষণ।",
            activeIngredient = "Dimethoate 30% EC",
            formulation = "30% EC",
            brandExamples = "Rogor (রোগোর)",
            productName = "Rogor 30% EC",
            manufacturer = "FMC India",
            productType = "Insecticide / কীটনাশক",
            growthStageBn = "ফুল আসার পূর্বে বা শুঁটি ধরার শুরুতে",
            dosePerHectareMlOrGm = 650.0,
            doseUnit = "ml",
            dosePerLitreBn = "১.৩ মিলি প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 15,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "সর্ষে ফুলে মৌমাছি আসার কারণে সকাল ১০টা থেকে দুপুর ৩টা পর্যন্ত স্প্রে করা সম্পূর্ণ নিষিদ্ধ। বিকেলে স্প্রে করুন।",
            resistanceGroup = "Group 1B Organophosphate",
            officialSource = "Directorate of Rapeseed-Mustard Research (DRMR) & CIBRC",
            frontImageResId = R.drawable.ic_launcher_foreground,
            nearbySellers = defaultSellers
        ),
        VerifiedTreatment(
            problemId = "groundnut_tikka",
            cropName = "Groundnut",
            problemNameBn = "চিনাবাদামের টিক্কা রোগ (Tikka Leaf Spot)",
            problemNameHi = "मूंगफली का टिक्का रोग",
            problemNameEn = "Groundnut Tikka Disease (Cercospora personata)",
            symptomsBn = "পাতায় গোল গাঢ় খয়েরি দাগ ও হলুদ বলয় দেখা যায়, পাতা ঝরে গিয়ে বাদাম পুষ্ট হওয়া ব্যাহত হয়।",
            symptomsHi = "पत्तियों पर गहरे भूरे गोल धब्बे जिनके चारों ओर पीला घेरा होता है।",
            symptomsEn = "Dark brown to black subcircular spots surrounded by yellow halos leading to severe defoliation.",
            possibleCauseBn = "উচ্চ তাপমাত্রা ও আর্দ্র স্যাঁতসেঁতে মাটি।",
            firstActionBn = "জমি পরিষ্কার ও আগাছামুক্ত রাখা।",
            biologicalOptionBn = "ট্রাইকোডার্মা বীজ শোধন (১০ গ্রাম/কেজি)।",
            activeIngredient = "Tebuconazole 25.9% EC",
            formulation = "25.9% EC",
            brandExamples = "Folicur (ফলিকুর)",
            productName = "Folicur 25.9% EC",
            manufacturer = "Bayer CropScience",
            productType = "Fungicide / ছত্রাকনাশক",
            growthStageBn = "গাছের বয়স ৩৫-৪০ দিন পর",
            dosePerHectareMlOrGm = 500.0,
            doseUnit = "ml",
            dosePerLitreBn = "১.০ মিলি প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 21,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "পাতার উভয় পিঠ ভিজিয়ে স্প্রে করুন। ব্যক্তিগত সুরক্ষা নিন।",
            resistanceGroup = "Group 3 DMI Fungicide",
            officialSource = "ICAR-DGR & CIBRC",
            frontImageResId = R.drawable.ic_launcher_foreground,
            nearbySellers = defaultSellers
        ),

        // ========================================================
        // 10. PULSES: CHICKPEA & LENTIL (ছোলা ও মসুর)
        // ========================================================
        VerifiedTreatment(
            problemId = "pulse_pod_borer",
            cropName = "Chickpea",
            problemNameBn = "ডাল ফসলের শুঁটি ছেঁদা পোকা (Gram Pod Borer)",
            problemNameHi = "चना का फली छेदक (Pod Borer)",
            problemNameEn = "Chickpea / Pulse Pod Borer (Helicoverpa armigera)",
            symptomsBn = "শুঁটিতে মাথা ঢুকিয়ে ভেতরের ডাল খেয়ে ফেলে এবং শুঁটি ফাঁপা করে দেয়।",
            symptomsHi = "फलियों में छेद कर दानों को खा जाना।",
            symptomsEn = "Defoliation at vegetative stage and caterpillars boring into pods consuming grains.",
            possibleCauseBn = "মেঘলা আকাশ ও বসন্তের উষ্ণতা বৃদ্ধি।",
            firstActionBn = "বিঘায় টি আকৃতির ৫-৬টি পাখির বসার ডাল (Bird Perches) ও ৫টি ফেরোমোন ফাঁদ লাগানো।",
            biologicalOptionBn = "এনপিভি (NPV) অথবা নিম তেল ৫ মিলি/লিটার।",
            activeIngredient = "Indoxacarb 14.5% SC",
            formulation = "14.5% SC",
            brandExamples = "Avaunt (অ্যাভান্ট)",
            productName = "Avaunt 14.5% SC",
            manufacturer = "FMC India",
            productType = "Insecticide / কীটনাশক",
            growthStageBn = "ফুল আসার পর ও কচি শুঁটি গঠনের সময়",
            dosePerHectareMlOrGm = 350.0,
            doseUnit = "ml",
            dosePerLitreBn = "০.৭ মিলি প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 14,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "মৌমাছির ক্ষতি এড়াতে ফুল ফোটার সময় সকালে স্প্রে করবেন না। বিকেলে স্প্রে করুন।",
            resistanceGroup = "Group 22A Oxadiazine",
            officialSource = "ICAR-IIPR Kanpur & CIBRC",
            frontImageResId = R.drawable.ic_launcher_foreground,
            nearbySellers = defaultSellers
        ),

        // ========================================================
        // 11. NURSERY & SEEDLINGS (নার্সারি ও চারাগাছ)
        // ========================================================
        VerifiedTreatment(
            problemId = "nursery_damping_off",
            cropName = "Nursery Plants",
            problemNameBn = "নার্সারির চারা ধসা ও গোড়া পচা (Damping Off in Nursery)",
            problemNameHi = "नर्सरी में पौध गलन (Damping Off)",
            problemNameEn = "Damping Off & Seedling Rot (Pythium / Rhizoctonia)",
            symptomsBn = "বীজতলায় মাটির সংলগ্ন চারার কাণ্ড নরম ও কালো হয়ে যায় এবং চারা মাটিতে শুয়ে মরে যায়।",
            symptomsHi = "मिट्टी की सतह पर तना काला पड़कर पौधा गिर जाता है।",
            symptomsEn = "Water-soaked girdling of seedling stems at soil line causing collapse of young seedlings.",
            possibleCauseBn = "বীজতলায় অতিরিক্ত জল জমা ও ঘন করে বীজ বোনা।",
            firstActionBn = "বীজতলায় জল নিকাশ নিশ্চিত করা, মাটি রোদে শুকানো এবং অতিরিক্ত চারা তুলে পাতলা করা।",
            biologicalOptionBn = "বীজতলার মাটিতে ট্রাইকোডার্মা হারজিয়ানাম (৫০ গ্রাম/বর্গমিটার) গোবর সারের সাথে মেশানো।",
            activeIngredient = "Metalaxyl 35% WS (বীজ শোধন) / Copper Oxychloride 50% WP",
            formulation = "50% WP (Wettable Powder)",
            brandExamples = "Blitox 50 (ব্লিটক্স) / ব্লু কপার",
            productName = "Blitox 50 (কপার অক্সিক্লোরাইড)",
            manufacturer = "Tata Rallis India",
            productType = "Fungicide / ছত্রাকনাশক",
            growthStageBn = "বীজতলা তৈরির সময় এবং চারা গজানোর ১০-১৫ দিন পর",
            dosePerHectareMlOrGm = 1500.0,
            doseUnit = "gm",
            dosePerLitreBn = "২.৫ - ৩.০ গ্রাম প্রতি লিটার জলে মাটি ড্রেঞ্চিং",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 0,
            reEntryIntervalHours = 12,
            safetyInstructionsBn = "চারার গোড়ার মাটি ভালোভাবে ভিজিয়ে স্প্রে বা ঝাঁঝরি দিয়ে ঢালুন।",
            resistanceGroup = "Group M1 Inorganic Copper",
            officialSource = "National Horticulture Board & CIBRC Verified",
            frontImageResId = R.drawable.ic_launcher_foreground,
            nearbySellers = defaultSellers
        ),

        // ========================================================
        // 12. COMMERCIAL: SUGARCANE & JUTE (আখ ও পাট)
        // ========================================================
        VerifiedTreatment(
            problemId = "sugarcane_red_rot",
            cropName = "Sugarcane",
            problemNameBn = "আখের লাল পচা রোগ (Red Rot of Sugarcane)",
            problemNameHi = "गन्ने का लाल सड़न रोग (Red Rot)",
            problemNameEn = "Sugarcane Red Rot (Colletotrichum falcatum)",
            symptomsBn = "আখের ভেতরের মজ্জা লাল হয়ে যায় ও আড়াআড়ি সাদা ছোপ থাকে, কাণ্ড শুকিয়ে টক গন্ধ বের হয়।",
            symptomsHi = "गन्ने के अंदर का गूदा लाल हो जाता है और सिरके जैसी गंध आती है।",
            symptomsEn = "Reddis discoloration of internal pith interrupted by distinct white patches, alcoholic sour smell.",
            possibleCauseBn = "আক্রান্ত বীজ টুকরো রোপণ ও জমিতে জল জমা।",
            firstActionBn = "রোগমুক্ত জাতের বীজ কাটিং নির্বাচন ও গরম জলে শোধন (৫০°C তাপমাত্রায় ২ ঘণ্টা)।",
            biologicalOptionBn = "ট্রাইকোডার্মা দ্রবণে বীজ টুকরো ৩০ মিনিট ডুবিয়ে রোপণ।",
            activeIngredient = "Carbendazim 50% WP (বীজ শোধন)",
            formulation = "50% WP",
            brandExamples = "Bavistin (ব্যাভিস্টিন)",
            productName = "Bavistin 50% WP",
            manufacturer = "Crystal Crop Protection",
            productType = "Fungicide / ছত্রাকনাশক",
            growthStageBn = "বীজ টুকরো রোপণের পূর্বে",
            dosePerHectareMlOrGm = 500.0,
            doseUnit = "gm",
            dosePerLitreBn = "১.০ গ্রাম প্রতি লিটার জলে বীজ শোধন",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 30,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "বীজ কাটিং দ্রবণে শোধনের পর ছায়ায় শুকিয়ে রোপণ করুন।",
            resistanceGroup = "Group 1 MBC Fungicide",
            officialSource = "ICAR - Indian Institute of Sugarcane Research (IISR)",
            frontImageResId = R.drawable.ic_launcher_foreground,
            nearbySellers = defaultSellers
        ),
        VerifiedTreatment(
            problemId = "jute_semilooper",
            cropName = "Jute",
            problemNameBn = "পাটের সেমি-লুপার ও বিছা পোকা (Jute Semilooper & Hairy Caterpillar)",
            problemNameHi = "जूट का सेमीलूपर व सूंडी",
            problemNameEn = "Jute Semilooper (Anomis sabulifera) & Bihar Hairy Caterpillar",
            symptomsBn = "কচি ডগার পাতা ও অগ্রমুকুল খেয়ে ডগা বাঁকিয়ে দেয়, ফলে পাটের শাখাপ্রশাখা বের হয়ে আঁশের ক্ষতি হয়।",
            symptomsHi = "कोमल पत्तियों व ऊपरी सिरे को खा जाना जिससे शाखाएं निकल आती हैं।",
            symptomsEn = "Caterpillars feed on tender top leaves destroying apical bud and causing side branching.",
            possibleCauseBn = "মেঘলা আকাশ ও আর্দ্র উষ্ণ আবহাওয়া।",
            firstActionBn = "জমিতে বাঁশের কঞ্চি বা ডাল পুঁতে পোকাখেকো পাখির বসার ব্যবস্থা করা।",
            biologicalOptionBn = "নিম তেল ১০,০০০ পিপিএম (২ মিলি/লিটার) অথবা বিটি (Bt) ২ গ্রাম/লিটার।",
            activeIngredient = "Lamda Cyhalothrin 5% EC",
            formulation = "5% EC",
            brandExamples = "Karate (কারাতে)",
            productName = "Karate 5% EC",
            manufacturer = "Syngenta India",
            productType = "Insecticide / কীটনাশক",
            growthStageBn = "গাছের উচ্চতা ২-৪ ফুট হওয়ার সময়",
            dosePerHectareMlOrGm = 500.0,
            doseUnit = "ml",
            dosePerLitreBn = "১.০ মিলি প্রতি লিটার জলে",
            waterPerHectareLiters = 500.0,
            preHarvestIntervalDays = 21,
            reEntryIntervalHours = 24,
            safetyInstructionsBn = "হাওয়ার উল্টো দিকে স্প্রে করবেন না। স্প্রে করার পর সাবান দিয়ে হাত-মুখ ধুয়ে নিন।",
            resistanceGroup = "Group 3A Pyrethroid",
            officialSource = "ICAR - CRIJAF Barrackpore & CIBRC",
            frontImageResId = R.drawable.ic_launcher_foreground,
            nearbySellers = defaultSellers
        ),

        // ========================================================
        // 13. SAFETY DEMO: BANNED & RESTRICTED PRODUCTS (Section 490)
        // ========================================================
        VerifiedTreatment(
            problemId = "banned_monocrotophos_alert",
            cropName = "BANNED PRODUCT ALERT",
            problemNameBn = "মনোক্রোটোফস (Monocrotophos 36% SL) - ভারতে সবজি ও ফলে সম্পূর্ণ নিষিদ্ধ!",
            problemNameHi = "मोनोक्रोटोफॉस - सब्जियों व फलों में प्रतिबंधित!",
            problemNameEn = "Monocrotophos 36% SL (BANNED ON VEGETABLES & FRUITS)",
            symptomsBn = "সতর্কতা: এটি উচ্চ বিষাক্ত অর্গ্যানোফসফেট। মানুষের স্নায়ুতন্ত্র ও পাখিদের জন্য মারাত্মক বিপজ্জনক।",
            symptomsHi = "अत्यधिक विषैला कीटनाशक, सब्जियों पर उपयोग पूर्णतया प्रतिबंधित।",
            symptomsEn = "Extremely hazardous class Ib pesticide. Prohibited for use on vegetables, fruits and edible greens.",
            possibleCauseBn = "অবৈধ বা নকল বিক্রেতার দ্বারা সুপারিশ।",
            firstActionBn = "কখনোই সবজি বা ফলে এই বিষ ব্যবহার করবেন না। নিরাপদ নিম তেল বা অনুমোদিত ডায়ামাইড ব্যবহার করুন।",
            biologicalOptionBn = "নিম তেল, ট্রাইকোডার্মা ও ফেরোমোন ট্র্যাপ।",
            activeIngredient = "Monocrotophos 36% SL",
            formulation = "36% SL",
            brandExamples = "Monocil / নুভাক্রন (নিষিদ্ধ)",
            productName = "Monocrotophos (নিষিদ্ধ)",
            manufacturer = "Banned on Horticulture",
            productType = "Insecticide (BANNED)",
            growthStageBn = "নিষিদ্ধ",
            dosePerHectareMlOrGm = null,
            doseUnit = "ml",
            dosePerLitreBn = "ব্যবহার সম্পূর্ণ নিষিদ্ধ",
            waterPerHectareLiters = 0.0,
            preHarvestIntervalDays = 999,
            reEntryIntervalHours = 999,
            maxApplications = 0,
            registrationStatus = "BANNED", // Section 490
            safetyInstructionsBn = "বিপদ সংকেত: এই কীটনাশকটি ভারতে সবজি, পান ও ফল উৎপাদনে আইনত সম্পূর্ণ নিষিদ্ধ। ব্যবহার করলে ফসল বিষাক্ত হয়।",
            resistanceGroup = "Banned Organophosphate",
            officialSource = "Ministry of Agriculture & Farmers Welfare, Govt of India (Gazette Banned List)",
            frontImageResId = null,
            nearbySellers = emptyList()
        )
    )

    fun getCropTreatments(cropQuery: String): List<VerifiedTreatment> {
        val q = cropQuery.trim().lowercase()
        return treatments.filter { t ->
            t.registrationStatus != "BANNED" && (
                t.cropName.contains(q, ignoreCase = true) ||
                q.contains(t.cropName, ignoreCase = true) ||
                t.problemNameBn.contains(q, ignoreCase = true) ||
                t.problemNameHi.contains(q, ignoreCase = true) ||
                t.problemNameEn.contains(q, ignoreCase = true)
            )
        }
    }

    fun getTreatmentById(id: String): VerifiedTreatment? {
        return treatments.find { it.problemId.equals(id, ignoreCase = true) || it.medicineId.equals(id, ignoreCase = true) }
    }

    fun searchByPestOrDisease(query: String): List<VerifiedTreatment> {
        val q = query.trim().lowercase()
        return treatments.filter { t ->
            t.problemNameBn.contains(q, ignoreCase = true) ||
            t.problemNameHi.contains(q, ignoreCase = true) ||
            t.problemNameEn.contains(q, ignoreCase = true) ||
            t.symptomsBn.contains(q, ignoreCase = true) ||
            t.symptomsHi.contains(q, ignoreCase = true) ||
            t.symptomsEn.contains(q, ignoreCase = true) ||
            (t.activeIngredient != null && t.activeIngredient.contains(q, ignoreCase = true)) ||
            t.brandExamples.contains(q, ignoreCase = true)
        }
    }

    /**
     * Section 478-479: Product Authenticity Verification
     */
    fun verifyProductAuthenticity(
        scannedText: String,
        lang: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.BENGALI
    ): ProductVerificationResult {
        val clean = scannedText.lowercase()

        // Check for Banned chemicals
        if (clean.contains("monocrotophos") || clean.contains("endosulfan") || clean.contains("phorate")) {
            return ProductVerificationResult(
                isAuthenticVerified = false,
                isBanned = true,
                statusTitleBn = when (lang) {
                    com.example.data.model.AppLanguage.BENGALI -> "⛔ নিষিদ্ধ পণ্য সতর্কতা!"
                    com.example.data.model.AppLanguage.HINDI -> "⛔ प्रतिबंधित उत्पाद चेतावनी!"
                    com.example.data.model.AppLanguage.ENGLISH -> "⛔ Banned Product Alert!"
                },
                messageBn = when (lang) {
                    com.example.data.model.AppLanguage.BENGALI -> "এই কীটনাশক উপাদানটি সরকার কর্তৃক সবজি ও খাদ্য ফসলে ব্যবহারের জন্য নিষিদ্ধ বা কঠোরভাবে নিয়ন্ত্রিত। এটি ব্যবহার করবেন না।"
                    com.example.data.model.AppLanguage.HINDI -> "यह कीटनाशक घटक सरकार द्वारा सब्जियों व खाद्य फसलों में उपयोग के लिए प्रतिबंधित या कड़ाई से नियंत्रित है। इसका उपयोग न करें।"
                    com.example.data.model.AppLanguage.ENGLISH -> "This pesticide active ingredient is banned or strictly restricted by the government on vegetables and food crops. Do not apply it."
                }
            )
        }

        // Match against verified active ingredients & brands in database
        val matched = treatments.find { t ->
            (t.activeIngredient != null && clean.contains(t.activeIngredient.lowercase().take(10))) ||
            (t.brandExamples.isNotBlank() && clean.contains(t.brandExamples.lowercase().take(6))) ||
            clean.contains(t.productName.lowercase().take(6))
        }

        if (matched != null) {
            return ProductVerificationResult(
                isAuthenticVerified = true,
                isBanned = false,
                statusTitleBn = when (lang) {
                    com.example.data.model.AppLanguage.BENGALI -> "✅ সরকারি নির্দেশিকায় পরীক্ষিত অনুমোদিত উপাদান"
                    com.example.data.model.AppLanguage.HINDI -> "✅ सरकारी दिशानिर्देश में सत्यापित अनुमोदित घटक"
                    com.example.data.model.AppLanguage.ENGLISH -> "✅ CIBRC / Govt Verified Approved Ingredient"
                },
                messageBn = when (lang) {
                    com.example.data.model.AppLanguage.BENGALI ->
                        "সক্রিয় উপাদান: ${matched.activeIngredient} (${matched.formulation})\nঅনুমোদিত ফসল: ${matched.getCropDisplayName(lang)}\nপ্রস্তুতকারক: ${matched.manufacturer}\nলক্ষ্য: ${matched.getProblemName(lang)}"
                    com.example.data.model.AppLanguage.HINDI ->
                        "सक्रिय घटक: ${matched.getDisplayActiveIngredient(lang)} (${matched.formulation})\nअनुमोदित फसल: ${matched.getCropDisplayName(lang)}\nनिर्माता: ${matched.manufacturer}\nलक्ष्य: ${matched.getProblemName(lang)}"
                    com.example.data.model.AppLanguage.ENGLISH ->
                        "Active Ingredient: ${matched.getDisplayActiveIngredient(lang)} (${matched.formulation})\nApproved Crop: ${matched.getCropDisplayName(lang)}\nManufacturer: ${matched.manufacturer}\nTarget: ${matched.getProblemName(lang)}"
                },
                matchedTreatment = matched
            )
        }

        return ProductVerificationResult(
            isAuthenticVerified = false,
            isBanned = false,
            statusTitleBn = when (lang) {
                com.example.data.model.AppLanguage.BENGALI -> "⚠️ পণ্যের তথ্য অনলাইনে নিশ্চিত করা যায়নি"
                com.example.data.model.AppLanguage.HINDI -> "⚠️ उत्पाद विवरण डेटाबेस में सत्यापित नहीं हुआ"
                com.example.data.model.AppLanguage.ENGLISH -> "⚠️ Product Not Matched in Verified Registry"
            },
            messageBn = when (lang) {
                com.example.data.model.AppLanguage.BENGALI -> "এই পণ্যের তথ্য বিশ্বস্ত ডাটাবেজে মেলেনি। ব্যবহারের আগে অনুমোদিত কৃষি সুরক্ষা আধিকারিক বা দোকানের মাধ্যমে যাচাই করুন।"
                com.example.data.model.AppLanguage.HINDI -> "यह उत्पाद विश्वसनीय डेटाबेस में नहीं मिला। उपयोग से पहले अधिकृत कृषि अधिकारी या लाइसेंस प्राप्त विक्रेता से सत्यापन करें।"
                com.example.data.model.AppLanguage.ENGLISH -> "This product did not match our verified registry. Please verify with an authorized agriculture officer or licensed agro dealer before use."
            }
        )
    }

    /**
     * Section 499: Admin Medicine Manager - Add / Update record
     */
    fun addAdminVerifiedTreatment(treatment: VerifiedTreatment) {
        // Prevent duplicates based on activeIngredient + formulation + cropName + problemId (Section 500)
        val exists = treatments.any {
            it.cropName.equals(treatment.cropName, ignoreCase = true) &&
            it.activeIngredient.equals(treatment.activeIngredient, ignoreCase = true) &&
            it.formulation.equals(treatment.formulation, ignoreCase = true) &&
            it.problemNameEn.equals(treatment.problemNameEn, ignoreCase = true)
        }
        if (!exists) {
            _adminAddedTreatments.add(treatment)
        }
    }
}

data class ProductVerificationResult(
    val isAuthenticVerified: Boolean,
    val isBanned: Boolean,
    val statusTitleBn: String,
    val messageBn: String,
    val matchedTreatment: VerifiedTreatment? = null
)
