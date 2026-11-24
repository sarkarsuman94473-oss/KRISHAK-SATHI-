package com.example.data.datasource

import com.example.R
import com.example.data.model.CropCategory
import com.example.data.model.CropGuideStep
import com.example.data.model.MasterCrop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CropMasterDatabase {
    private val baseCrops: List<MasterCrop> = listOf(
        // TEA GARDEN
        MasterCrop(
            cropId = "tea",
            nameBn = "চা বাগান (Tea)",
            nameHi = "चाय बागान (Tea)",
            nameEn = "Tea",
            scientificName = "Camellia sinensis",
            category = CropCategory.TEA,
            bestSeasonBn = "মার্চ থেকে নভেম্বর (প্রধান পাতা তোলার সময়)",
            soilBn = "অম্লযুক্ত দোআঁশ মাটি (pH ৪.৫ - ৫.৫), উপযুক্ত নিকাশী ব্যবস্থা",
            seedRatePerBighaBn = "১৮০০ - ২০০০ চারা প্রতি একর (কলম / চারা)",
            spacingBn = "১০৫ সেমি × ৭৫ সেমি বা ১২০ সেমি × ৬০ সেমি",
            plantDensityPerAcre = 1800,
            harvestDays = 365,
            expectedYieldBn = "প্রতি বিঘায় আনুমানিক ৩০০ - ৫০০ কেজি কাঁচা পাতা",
            summaryBn = "উন্নতমানের চা চাষের জন্য সঠিক ছায়া গাছ (Albizia), নিয়মিত ছাঁটাই (Pruning), ড্রেনেজ ও পোকা নিয়ন্ত্রণ আবশ্যক।",
            summaryHi = "उचित छायादार वृक्ष, नियमित छंटाई और कीट नियंत्रण से उच्च गुणवत्ता वाली चाय पत्ती प्राप्त होती है।",
            summaryEn = "Premium tea cultivation requires shade trees, planned pruning cycles, drainage, and active mite monitoring.",
            iconEmoji = "🍵",
            imageResId = R.drawable.img_tea_leaves_1793343045221,
            commonPests = listOf("রেড স্পাইডার মাইট (Red Spider Mite)", "টি মস্কিটো বাগ (Tea Mosquito Bug)", "থ্রিপস (Thrips)", "লুপার শুঁয়োপোকা (Looper)"),
            commonDiseases = listOf("ব্লিস্টার ব্লাইট (Blister Blight)", "কালো পচা রোগ (Black Rot)", "শিকড় পচা (Root Rot)"),
            steps = listOf(
                CropGuideStep(1, "জমি ও ড্রেনেজ প্রস্তুতি", "भूमि व जल निकासी", "Land & Drainage", "মাটির pH ৪.৫-৫.৫ রাখুন। বর্ষায় জল যাতে না জমে গভীর নিকাশী নালা তৈরি করুন।", "मिट्टी की जल निकासी सुनिश्चित करें।", "Ensure deep drainage ditches.", "land_prep"),
                CropGuideStep(30, "ছায়া গাছ রোপণ", "छायादार पेड़", "Shade Tree Planting", "প্রতি বিঘায় নির্দিষ্ট দূরত্বে আলবিজিয়া বা ডেরিস জাতীয় ছায়া গাছ লাগান।", "छायादार पेड़ लगाएं।", "Plant shade trees for leaf quality.", "land_prep"),
                CropGuideStep(120, "সার প্রয়োগ (NPK)", "उर्वरक (NPK)", "Fertilizer Cycle", "প্রথম বৃষ্টির পর ১০:৫:১০ অনুপাতে NPK এবং জিংক সালফেট স্প্রে করুন।", "संतुलित NPK खाद डालें।", "Apply balanced NPK and Zinc sulphate.", "fertilizer"),
                CropGuideStep(210, "টি মস্কিটো ও মাইট নিরীক্ষণ", "कीट निगरानी", "Pest Scouting", "নতুন কচি পাতায় খয়েরি দাগ ও জাল দেখতে নিয়মিত সকালে নিরীক্ষণ করুন।", "कीटों की जांच करें।", "Inspect shoots for red spider mites.", "monitoring"),
                CropGuideStep(300, "ছাঁটাই ও স্কিফিং (Pruning)", "छंटाई (Pruning)", "Pruning & Skiffing", "শীতকালে সুপ্তাবস্থায় হালকা ছাঁটাই (Light Prune) করে টেবিল তৈরি করুন।", "झाड़ियों की सही छंटाई करें।", "Perform winter light prune.", "land_prep"),
                CropGuideStep(365, "পাতা তোলা (Plucking Cycle)", "पत्ती तोड़ाई", "Plucking Cycle", "প্রতি ৭-১০ দিন অন্তর 'দুটি পাতা ও একটি কুঁড়ি' সতর্কভাবে তুলুন।", "दो पत्ती एक कली नियम से तोड़ें।", "Pluck two leaves and a bud every 7-10 days.", "harvest")
            )
        ),

        // LEAFY VEGETABLES
        MasterCrop(
            cropId = "palak",
            nameBn = "পালং শাক (Spinach)",
            nameHi = "पालक (Spinach)",
            nameEn = "Spinach",
            scientificName = "Spinacia oleracea",
            category = CropCategory.LEAFY_VEG,
            bestSeasonBn = "অক্টোবর - জানুয়ারি (শীতকালীন প্রধান)",
            soilBn = "জৈব পদার্থ সমৃদ্ধ উর্বর দোআঁশ মাটি",
            seedRatePerBighaBn = "১.৫ - ২ কেজি বীজ প্রতি বিঘা",
            spacingBn = "সারি থেকে সারি ২০ সেমি, গাছ ১০ সেমি",
            harvestDays = 35,
            expectedYieldBn = "১৫ - ২০ কুইন্টাল প্রতি বিঘা",
            summaryBn = "দ্রুত ফলনশীল শাক। পর্যাপ্ত গোবর সার ও আর্দ্রতা থাকলে ৩৫-৪০ দিনেই ফসল তোলা যায়।",
            summaryHi = "35-40 दिनों में तैयार होने वाली पत्तेदार सब्जी।",
            summaryEn = "Fast growing leafy green ready in 35-40 days with organic manure and regular moisture.",
            iconEmoji = "🥬",
            imageResId = R.drawable.img_palak_1793343281579,
            commonPests = listOf("জাবপোকা (Aphids)", "শুঁয়োপোকা (Leaf Miner)"),
            commonDiseases = listOf("ডাউনি মিলডিউ (Downy Mildew)", "পাতা পোড়া রোগ"),
            steps = listOf(
                CropGuideStep(1, "জমি তৈরি ও গোবর সার", "भूमि तैयारी", "Soil Prep", "জমি ৩-৪ বার চাষ দিয়ে বিঘায় ৫০০ কেজি গোবর সার বা কেঁচো সার মিশিয়ে সমান করুন।", "गोबर खाद मिलाकर खेत तैयार करें।", "Till 3 times and add 500kg compost per bigha.", "land_prep"),
                CropGuideStep(10, "চারা গজানো ও পাতলা করা", "पौध विरलीकरण", "Thinning", "গাছ ঘন হলে অতিরিক্ত চারা তুলে দূরত্ব বজায় রাখুন।", "अतिरिक्त पौधों को निकालें।", "Thin out extra seedlings.", "weeding"),
                CropGuideStep(20, "হালকা সেচ ও আগাছা দমন", "निराई व सिंचाई", "Weeding & Irrigation", "মাটি ভেজা রাখুন, আগাছা পরিষ্কার করুন।", "हल्की सिंचाई व निराई करें।", "Light irrigation and keep weed-free.", "irrigation"),
                CropGuideStep(35, "প্রথম ফসল কাটা", "कटाई", "Harvest", "গোড়া থেকে ৩ সেমি ওপরে ধারালো কাঁচি দিয়ে পাতা কেটে আঁটি বাঁধুন।", "पत्तियों को काटकर बंडल बनाएं।", "Harvest leaves 3cm above root crown.", "harvest")
            )
        ),
        MasterCrop(
            cropId = "lal_shak",
            nameBn = "লাল শাক (Red Amaranth)",
            nameHi = "लाल साग (Amaranth)",
            nameEn = "Red Amaranth",
            scientificName = "Amaranthus tricolor",
            category = CropCategory.LEAFY_VEG,
            bestSeasonBn = "সারা বছর (মার্চ-অক্টোবর সেরা)",
            soilBn = "বেলে দোআঁশ মাটি",
            seedRatePerBighaBn = "৩০০ - ৪০০ গ্রাম বীজ প্রতি বিঘা",
            spacingBn = "ছিটিয়ে বা সারিতে ১৫ সেমি",
            harvestDays = 25,
            expectedYieldBn = "১০ - ১২ কুইন্টাল প্রতি বিঘা",
            summaryBn = "২৫-৩০ দিনেই ঘরে তোলা যায়। প্রচুর পুষ্টি ও আয়রন সমৃদ্ধ।",
            summaryHi = "25-30 दिनों में पौष्टिक लाल साग तैयार।",
            summaryEn = "Fast harvest red amaranth yielding in 25-30 days.",
            iconEmoji = "🥗",
            commonPests = listOf("পাতা ছেঁদা পোকা", "শুঁয়োপোকা"),
            commonDiseases = listOf("গোড়া পচা (Damping off)")
        ),
        MasterCrop(
            cropId = "pui_shak",
            nameBn = "পুঁই শাক (Malabar Spinach)",
            nameHi = "पोई साग (Malabar Spinach)",
            nameEn = "Malabar Spinach",
            scientificName = "Basella alba",
            category = CropCategory.LEAFY_VEG,
            bestSeasonBn = "ফেব্রুয়ারি - জুন",
            soilBn = "দোআঁশ ও এঁটেল দোআঁশ",
            seedRatePerBighaBn = "৪০০ গ্রাম বীজ / কাটিং",
            spacingBn = "৬০ সেমি × ৫০ সেমি (মাচা প্রয়োজন)",
            harvestDays = 50,
            expectedYieldBn = "২৫ - ৩০ কুইন্টাল প্রতি বিঘা",
            summaryBn = "মাচায় চাষ করলে দীর্ঘ সময় ধরে প্রচুর ডগা ও পাতা পাওয়া যায়।",
            summaryHi = "मचान पर लंबे समय तक उत्पादन।",
            summaryEn = "Trellis cultivated hardy leafy green with long harvesting window.",
            iconEmoji = "🌿"
        ),
        MasterCrop(
            cropId = "dhane_pata",
            nameBn = "ধনে পাতা (Coriander)",
            nameHi = "हरा धनिया (Coriander)",
            nameEn = "Coriander",
            scientificName = "Coriandrum sativum",
            category = CropCategory.LEAFY_VEG,
            bestSeasonBn = "অক্টোবর - ডিসেম্বর",
            soilBn = "উর্বর বেলে দোআঁশ মাটি",
            seedRatePerBighaBn = "১.৫ কেজি বীজ (ভেঙে দুই ফালি করে বোনা)",
            spacingBn = "সারিতে ২০ সেমি",
            harvestDays = 35,
            expectedYieldBn = "৮ - ১০ কুইন্টাল প্রতি বিঘা",
            summaryBn = "বীজ বোনার আগে ১২ ঘণ্টা ভিজিয়ে দুই ফালি করে নিলে দ্রুত চারা গজায়।",
            summaryHi = "बीज को भिगोकर बोने से जल्दी अंकुरण।",
            summaryEn = "Soak split seeds for 12 hours before sowing for uniform germination.",
            iconEmoji = "🌱"
        ),

        // MAJOR VEGETABLES
        MasterCrop(
            cropId = "cabbage",
            nameBn = "বাঁধাকপি (Cabbage)",
            nameHi = "पत्तागोभी (Cabbage)",
            nameEn = "Cabbage",
            scientificName = "Brassica oleracea var. capitata",
            category = CropCategory.VEGETABLES,
            bestSeasonBn = "সেপ্টেম্বর - নভেম্বর (রোপণ সময়)",
            soilBn = "জল নিকাশীযুক্ত উর্বর দোআঁশ মাটি",
            seedRatePerBighaBn = "১০০ - ১২০ গ্রাম বীজ (তলায় চারা তৈরি)",
            spacingBn = "৪৫ সেমি × ৪৫ সেমি",
            harvestDays = 75,
            expectedYieldBn = "৪০ - ৫০ কুইন্টাল প্রতি বিঘা",
            summaryBn = "হিমেল আবহাওয়া ও সঠিক নাইট্রোজেন-পটাশ সারে নিরেট সুন্দর বাঁধাকপি গঠিত হয়। ডিবিএম পোকা নজর রাখুন।",
            summaryHi = "ठंडे मौसम की फसल। डायमंडबैक मोथ की नियमित जांच करें।",
            summaryEn = "Cool season vegetable requiring firm heads and diamondback moth monitoring.",
            iconEmoji = "🥬",
            imageResId = R.drawable.img_cabbage_1793343062183,
            commonPests = listOf("ডায়মন্ডব্যাক মথ (DBM)", "শুঁয়োপোকা (Caterpillars)", "জাবপোকা (Aphids)"),
            commonDiseases = listOf("কালো পচা রোগ (Black Rot)", "ক্লাব রুট (Clubroot)"),
            steps = listOf(
                CropGuideStep(1, "বীজতলা তৈরি ও বীজ শোধন", "नर्सरी व बीज उपचार", "Nursery Prep", "ট্রাইকোডার্মা দিয়ে বীজ শোধন করে উঁচু বীজতলায় চারা তৈরি করুন।", "बीज उपचार करें।", "Seed treatment with Trichoderma.", "land_prep"),
                CropGuideStep(25, "মূল জমিতে চারা রোপণ", "पौध रोपाई", "Transplanting", "বিঘায় ২০ কেজি ইউরিয়া, ৪০ কেজি ডিএপি, ৩০ কেজি পটাশ মিশিয়ে ৪৫ সেমি দূরত্বে চারা বসান।", "खेत में पौध रोपें।", "Transplant at 45cm spacing.", "land_prep"),
                CropGuideStep(40, "আগাছা দমন ও মাটি তোলা", "मिट्टी चढ़ाना", "Earthing Up", "গাছের গোড়ায় মাটি তুলে দিন যাতে চারা হেলে না পড়ে।", "पौधों पर मिट्टी चढ़ाएं।", "Earth up roots to prevent lodging.", "weeding"),
                CropGuideStep(55, "মাথা গঠন ও শুঁয়োপোকা দমন", "हेड फॉर्मेशन व कीट", "Head Formation", "কপির মাথা বাঁধতে শুরু করলে ফেরোমোন ফাঁদ লাগান ও নিম তেল স্প্রে করুন।", "नीम तेल का स्प्रे करें।", "Install pheromone traps.", "monitoring"),
                CropGuideStep(75, "ফসল তোলা ও বাজারজাত", "कटाई व बिक्री", "Harvesting", "মাথা শক্ত ও নিরেট হলে ধারালো ছুরি দিয়ে কেটে বাজারে পাঠান।", "ठोस गोभी की कटाई करें।", "Harvest firm compact heads.", "harvest")
            )
        ),
        MasterCrop(
            cropId = "cauliflower",
            nameBn = "ফুলকপি (Cauliflower)",
            nameHi = "फूलगोभी (Cauliflower)",
            nameEn = "Cauliflower",
            scientificName = "Brassica oleracea var. botrytis",
            category = CropCategory.VEGETABLES,
            bestSeasonBn = "আগস্ট - নভেম্বর (আগাম, নাবী জাত)",
            soilBn = "উর্বর গভীর দোআঁশ মাটি, আর্দ্রতাযুক্ত",
            seedRatePerBighaBn = "১০০ - ১৫০ গ্রাম বীজ",
            spacingBn = "৫০ সেমি × ৪৫ সেমি",
            harvestDays = 70,
            expectedYieldBn = "৩৫ - ৪৫ কুইন্টাল প্রতি বিঘা",
            summaryBn = "বোরনের ঘাটতিতে ফুলকপির ফুল বাদামি বা ফাঁপা হতে পারে। বোরন ও মলিবডেনাম নিশ্চিত করুন।",
            summaryHi = "बोरॉन की कमी से फूल भूरा हो सकता है, समय पर बोरॉन दें।",
            summaryEn = "Curd quality depends on timely micronutrients like Boron and Molybdenum.",
            iconEmoji = "🥦",
            imageResId = R.drawable.img_cauliflower_1793343216647,
            commonPests = listOf("তামাক শুঁয়োপোকা (Spodoptera)", "জাবপোকা"),
            commonDiseases = listOf("হুইপটেল (বোরন ঘাটতি)", "ডাউনি মিলডিউ")
        ),
        MasterCrop(
            cropId = "tomato",
            nameBn = "টমেটো (Tomato)",
            nameHi = "टमाटर (Tomato)",
            nameEn = "Tomato",
            scientificName = "Solanum lycopersicum",
            category = CropCategory.VEGETABLES,
            bestSeasonBn = "সেপ্টেম্বর - নভেম্বর ও বসন্তকালীন",
            soilBn = "উন্নত জল নিকাশী দোআঁশ ও বেলে দোআঁশ",
            seedRatePerBighaBn = "৫০ - ৬০ গ্রাম হাইব্রিড বীজ",
            spacingBn = "৬০ সেমি × ৪৫ সেমি",
            harvestDays = 80,
            expectedYieldBn = "৫০ - ৮০ কুইন্টাল প্রতি বিঘা",
            summaryBn = "পাতা কোঁকড়ানো ও ফলছেঁদা পোকা প্রতিরোধে সাদা মাছি দমন ও খুঁটি (staking) জরুরি।",
            summaryHi = "सफेद मक्खी नियंत्रण और पौधों को सहारा (स्टेकिंग) देना आवश्यक।",
            summaryEn = "Requires whitefly control against leaf curl virus and bamboo staking for support.",
            iconEmoji = "🍅",
            imageResId = R.drawable.img_tomato_1793343030528,
            commonPests = listOf("ফলছেঁদা পোকা (Fruit Borer)", "সাদা মাছি (Whitefly)", "পাতা সুরঙ্গকারী (Leaf Miner)"),
            commonDiseases = listOf("পাতার কোঁকড়া রোগ (Leaf Curl)", "নাভি ধসা (Late Blight)", "ব্যাকটেরিয়াল উইল্ট")
        ),
        MasterCrop(
            cropId = "potato",
            nameBn = "আলু (Potato)",
            nameHi = "आलू (Potato)",
            nameEn = "Potato",
            scientificName = "Solanum tuberosum",
            category = CropCategory.ROOT_VEG,
            subCategory = "Tuber & Root Crops",
            aliases = listOf("আলু", "Potato", "Alu", "Aloo", "Potato Tuber"),
            bestSeasonBn = "নভেম্বর ১ম থেকে ৩য় সপ্তাহ (শীতকালীন প্রধান রবি)",
            soilBn = "উর্বর ও ঝুরঝুরে বেলে দোআঁশ মাটি (pH ৫.২ - ৬.৪)",
            seedRatePerBighaBn = "২০০ - ২৫০ কেজি বীজ আলু (৪০-৫০ গ্রাম সাইজের কাটা বা গোটা টিস্যু কালচার বীজ)",
            spacingBn = "সারি থেকে সারি ৫০-৬০ সেমি, বীজ থেকে বীজ ২০ সেমি",
            harvestDays = 90,
            expectedYieldBn = "৭০ - ৯০ কুইন্টাল প্রতি বিঘা",
            summaryBn = "প্রধান অর্থকরী কন্দ ফসল। কুয়াশাচ্ছন্ন আবহাওয়ায় নাবি ধসা (Late Blight) প্রতিরোধে ম্যানকোজেব স্প্রে, সঠিক ভেলি তৈরি, ২০-২৫ দিনে প্রথম মাটি তোলা ও ফসল তোলার ১০-১২ দিন আগে গাছ কাটা (ডিহমিং) জরুরি।",
            summaryHi = "कोहरे के मौसम में लेट ब्लाइट से बचाव, सही मेड़ बनाना और खुदाई से पहले डीहॉमिंग आवश्यक।",
            summaryEn = "Premier winter tuber crop. Requires ridge furrow method, early earthing up, late blight preventive sprays, and pre-harvest dehaulming.",
            iconEmoji = "🥔",
            imageResId = R.drawable.img_potato_1793343095516,
            commonPests = listOf("কাটুই পোকা (Cutworm)", "আলুর নলি পোকা (PTM)", "জাবপোকা (Aphids)"),
            commonDiseases = listOf("নাবি ধসা (Late Blight)", "আগে ধসা (Early Blight)", "সাধারণ স্ক্যাব (Common Scab)", "কালো দাগ (Black Scurf)"),
            weedManagementBn = "বপনের ৩-৪ দিনের মধ্যে মেট্রিবিউজিন (Metribuzin) অথবা ২০-২৫ দিনে আগাছা নিড়িয়ে গোড়ায় মাটি তুলুন।",
            seedTreatmentBn = "বীজ আলু কাটার পর ম্যানকোজেব (Mancozeb 2g/L) বা কার্বেন্ডাজিম দ্রবণে ১০ মিনিট চুবিয়ে ছায়ায় ২৪-৪৮ ঘণ্টা শুকিয়ে সুবেরাইজেশন (ত্বক শক্ত) করুন।",
            steps = listOf(
                CropGuideStep(1, "১. বীজ আলু নির্বাচন ও সুপ্ততা ভাঙা", "बीज चयन व अंकुरण", "Seed Potato Selection & Pre-sprouting", "কোল্ড স্টোরেজ থেকে বের করে ছায়াযুক্ত শুকনো ঘরে ৮-১০ দিন রাখুন যাতে ছোট সবুজ চোখ/অঙ্কুর গজাতে পারে।", "कोल्ड स्टोर से निकाल कर आंखें फूटने दें।", "Bring seed tubers out to break dormancy and sprout in shade.", "land_prep"),
                CropGuideStep(5, "২. চিটিং ও স্প্রাউট পরিচর্যা", "चिटिंग प्रबंधन", "Chitting & Sprout Management", "অঙ্কুর ১-২ সেমি শক্ত ও সবুজ রাখতে ছড়ানো আলোর ঘরে বাতাস চলাচলের ব্যবস্থা করুন।", "अंकुरों को मजबूत व हरा बनाएं।", "Allow sturdy green sprout development under diffused light.", "land_prep"),
                CropGuideStep(10, "৩. বীজ কাটা ও সুবেরাইজেশন", "बीज कटाई व उपचार", "Cut Tuber Treatment & Suberization", "প্রতি টুকরায় অন্তত ২টি চোখ রেখে কাটুন। কাটার পর ম্যানকোজেব বা ট্রাইকোডার্মায় চুবিয়ে ২৪-৪৮ ঘণ্টা ছায়ায় রেখে কাটা অংশ শুকিয়ে চামড়া শক্ত করুন।", "कटे बीज का उपचार कर सुखाएं।", "Cut with 2+ eyes, treat with fungicide and shade dry to suberize cut surfaces.", "land_prep"),
                CropGuideStep(15, "৪. জমি চাষ ও নালা তৈরি", "खेत तैयारी व नाली विधि", "Land Preparation & Furrow Method", "জমি ৩-৪ বার গভীর চাষ দিয়ে ঝুরঝুরে করুন। বিঘায় ১.৫ টন গোবর সার, ফসফেট ও অর্ধেক পটাশ মিশিয়ে ৫০ সেমি দূরত্বে নালা কাটুন।", "गहरी जुताई कर 50 सेमी पर नालियां बनाएं।", "Till deeply and prepare furrows at 50-60cm spacing.", "land_prep"),
                CropGuideStep(18, "৫. বীজ রোপণ ও ভেলি/আইল তৈরি", "बीज रोपाई व मेड़ बनाना", "Planting & Ridge Making", "নালার ভেতরে ২০ সেমি দূরে চোখ উপরের দিকে রেখে বীজ বসিয়ে মাটি দিয়ে ঢেকে উঁচু ভেলি (Ridge) তৈরি করুন।", "मेड़ बनाकर बीज रोपें।", "Place seed with sprouts facing upward and cover into raised ridges.", "land_prep"),
                CropGuideStep(38, "৬. প্রথম মাটি তোলা ও আগাছা নিড়ানি (২০-২৫ দিন)", "पहली मिट्टी चढ़ाना (20-25 दिन)", "First Earthing Up (20-25 days)", "গাছ ১৫-২০ সেমি উঁচু হলে প্রথম চাপান সার (ইউরিয়া) দিয়ে দুই সারির মাঝের মাটি তুলে আলুর গোড়া ভালো করে ঢেকে দিন।", "यूरिया देकर पौधों पर मिट्टी चढ़ाएं।", "Weed, top-dress urea and draw soil up onto the ridge to cover stolons.", "weeding"),
                CropGuideStep(50, "৭. আলু ধরা পর্যায় ও নিয়ন্ত্রিত সেচ", "कंद निर्माण व सिंचाई", "Tuber Initiation Stage Irrigation", "গোড়ায় ছোট ছোট আলু ধরতে শুরু করলে জমিতে হালকা সেচ দিন। ভেলির ওপর জল যেন না ওঠে, নালার অর্ধেক পর্যন্ত জল রাখুন।", "नाली के आधे तक हल्की सिंचाई दें।", "Apply light furrow irrigation without submerging ridges.", "irrigation"),
                CropGuideStep(65, "৮. আলু স্ফীতি ও নাবি ধসা সুরক্ষা", "कंद विकास व लेट ब्लाइट सुरक्षा", "Bulking Stage & Late Blight Protection", "আলুর আকার দ্রুত বড় হয়। কুয়াশাচ্ছন্ন আবহাওয়া দেখা দিলে আগাম ম্যানকোজেব স্প্রে করুন। আক্রমণ হলে সাইমোক্সানিল বা ডাইমিথোমর্ফ দিন।", "कोहरा होने पर लेट ब्लाइट नाशक छिड़कें।", "Spray preventive Mancozeb against Late Blight during foggy mornings.", "monitoring"),
                CropGuideStep(80, "৯. ডিহমিং / গাছ কাটা (তোলার ১০-১২ দিন আগে)", "डीहॉमिंग (पौध कटाई)", "Dehaulming (Vine Cutting) 10-12 Days Prior", "ফসল তোলার ১০-১২ দিন পূর্বে আলুর সমস্ত ডালপালা মাটির ৩ সেমি ওপর থেকে কেটে ফেলুন। এতে মাটির নিচে আলুর চামড়া মোটা ও শক্ত হয়।", "खुदाई से 10-12 दिन पहले बेल काट दें।", "Cut potato vines 10-12 days before harvest to harden the tuber skin.", "land_prep"),
                CropGuideStep(90, "১০. আলু তোলা, কিউরিং ও কোল্ড স্টোরেজ", "खुदाई, सुखाना व कोल्ड स्टोर", "Harvesting, Curing & Cold Storage", "শুকনো আবহাওয়ায় মাটি কুপিয়ে দাগমুক্ত আলু তুলুন। ছায়ায় ১০-১৫ দিন রেখে গ্রেডিং করে ৫০ কেজির বস্তায় স্টোরে পাঠাতে হবে।", "छाया में सुखाकर ग्रेडिंग करें और स्टोर भेजें।", "Dig unbruised tubers, cure in shade for 10-15 days, grade and pack for cold storage.", "harvest")
            )
        ),
        MasterCrop(
            cropId = "chilli",
            nameBn = "লঙ্কা (Chilli)",
            nameHi = "मिर्च (Chilli)",
            nameEn = "Chilli",
            scientificName = "Capsicum annuum",
            category = CropCategory.VEGETABLES,
            bestSeasonBn = "সেপ্টেম্বর-অক্টোবর অথবা ফেব্রুয়ারি-মার্চ",
            soilBn = "উর্বর বেলে দোআঁশ",
            seedRatePerBighaBn = "৫০ - ৬০ গ্রাম",
            spacingBn = "৫০ সেমি × ৪০ সেমি",
            harvestDays = 75,
            expectedYieldBn = "১৫ - ২৫ কুইন্টাল কাঁচা লঙ্কা",
            summaryBn = "থ্রিপস ও মাকড়ের আক্রমণে পাতা ওপর ও নিচের দিকে কোঁকড়ায়। নিয়মিত পর্যবেক্ষণ জরুরি।",
            summaryHi = "थ्रिप्स और माइट्स से पत्ती मुड़ने की समस्या होती है, सतर्क रहें।",
            summaryEn = "Thrips and mites cause upward/downward leaf curling. Monitor shoot tips.",
            iconEmoji = "🌶",
            imageResId = R.drawable.img_chilli_1793343108793,
            commonPests = listOf("থ্রিপস (Thrips)", "হলুদ মাকড় (Yellow Mites)", "ফল পচা পোকা"),
            commonDiseases = listOf("পাতা কোঁকড়া ভাইরাস", "অ্যানথ্রাকনোজ (ফল পচা)")
        ),
        MasterCrop(
            cropId = "brinjal",
            nameBn = "বেগুন (Brinjal)",
            nameHi = "बैंगन (Brinjal)",
            nameEn = "Brinjal",
            scientificName = "Solanum melongena",
            category = CropCategory.VEGETABLES,
            bestSeasonBn = "সারা বছর (জুন-জুলাই ও সেপ্টেম্বর-অক্টোবর)",
            soilBn = "গভীর উর্বর দোআঁশ",
            seedRatePerBighaBn = "৫০ - ৬০ গ্রাম",
            spacingBn = "৭৫ সেমি × ৬০ সেমি",
            harvestDays = 80,
            expectedYieldBn = "৪০ - ৬০ কুইন্টাল প্রতি বিঘা",
            summaryBn = "ডগা ও ফলছেঁদা পোকা প্রধান শত্রু। আক্রান্ত ডগা কেটে মাটিতে পুঁতে ফেলা প্রাথমিক কাজ।",
            summaryHi = "तना व फल छेदक के लिए ग्रसित शाखाओं को तोड़कर नष्ट करें।",
            summaryEn = "Shoot and fruit borer is primary pest. Clip affected tips as first action.",
            iconEmoji = "🍆",
            imageResId = R.drawable.img_brinjal_1793343230024
        ),
        MasterCrop(
            cropId = "okra",
            nameBn = "ভেন্ডি / ঢেঁড়স (Okra)",
            nameHi = "भिंडी (Okra)",
            nameEn = "Okra",
            scientificName = "Abelmoschus esculentus",
            category = CropCategory.VEGETABLES,
            bestSeasonBn = "ফেব্রুয়ারি - এপ্রিল ও জুন - জুলাই",
            soilBn = "দোআঁশ ও এঁটেল দোআঁশ",
            seedRatePerBighaBn = "১ - ১.৫ কেজি বীজ",
            spacingBn = "৪৫ সেমি × ৩০ সেমি",
            harvestDays = 50,
            expectedYieldBn = "১৫ - ২০ কুইন্টাল প্রতি বিঘা",
            summaryBn = "হলুদ শিরা ভাইরাস (YVMV) রোধে রোগ প্রতিরোধী জাত (যেমন অর্কা অনামিকা) নির্বাচন করুন।",
            summaryHi = "पीत शिरा रोग प्रतिरोधी किस्म का चयन करें।",
            summaryEn = "Select Yellow Vein Mosaic Virus resistant seeds to prevent whitefly damage.",
            iconEmoji = "🥬",
            imageResId = R.drawable.img_okra_1793343244337
        ),
        MasterCrop(
            cropId = "cucumber",
            nameBn = "শসা (Cucumber)",
            nameHi = "खीरा (Cucumber)",
            nameEn = "Cucumber",
            scientificName = "Cucumis sativus",
            category = CropCategory.FRUIT_VEG,
            subCategory = "Fruit Vegetables & Cucurbits",
            aliases = listOf("cucumber", "shosha", "শসা", "खीरा"),
            bestSeasonBn = "ফেব্রুয়ারি - এপ্রিল ও জুন - আগস্ট",
            soilBn = "উর্বর বেলে দোআঁশ মাটি, প্রচুর জৈব সার",
            seedRatePerBighaBn = "১৫০ - ২০০ গ্রাম বীজ প্রতি বিঘা",
            spacingBn = "দেড় মিটার × ১ মিটার (মাচায়)",
            harvestDays = 45,
            expectedYieldBn = "২৫ - ৩৫ কুইন্টাল প্রতি বিঘা",
            summaryBn = "মাচায় চাষ করলে ফলের আকার সোজা ও সুন্দর হয়। মাছি পোকা ফাঁদ (Pheromone Trap) ব্যবহার করুন।",
            summaryHi = "मचान खेती से सीधा और चमकदार फल मिलता है।",
            summaryEn = "Trellis culture yields straight uniform fruit. Install cue-lure traps for fruit flies.",
            iconEmoji = "🥒",
            commonPests = listOf("ফল মাছি পোকা (Fruit Fly)", "রেড পাম্পকিন বিটল"),
            commonDiseases = listOf("ডাউনি মিলডিউ", "পাউডারি মিলডিউ")
        ),
        MasterCrop(
            cropId = "bottle_gourd",
            nameBn = "লাউ (Bottle Gourd)",
            nameHi = "लौकी (Bottle Gourd)",
            nameEn = "Bottle Gourd",
            scientificName = "Lagenaria siceraria",
            category = CropCategory.VEGETABLES,
            bestSeasonBn = "সেপ্টেম্বর - নভেম্বর ও বসন্তকালীন",
            soilBn = "জৈব সার সমৃদ্ধ উর্বর দোআঁশ",
            seedRatePerBighaBn = "২০০ গ্রাম",
            spacingBn = "২ মিটার × ২ মিটার মাদায়",
            harvestDays = 60,
            expectedYieldBn = "৪০ - ৬০ কুইন্টাল",
            summaryBn = "মাদায় পর্যাপ্ত গোবর সার ও মাটির আর্দ্রতা বজায় রাখলে প্রচুর ফলন মেলে।",
            summaryHi = "गड्ढों में गोबर की खाद भरकर मचान पर बेल चढ़ाएं।",
            summaryEn = "Pit preparation with abundant FYM on sturdy trellises maximizes yields.",
            iconEmoji = "🌱"
        ),

        // FRUITS
        MasterCrop(
            cropId = "mango",
            nameBn = "আম (Mango)",
            nameHi = "आम (Mango)",
            nameEn = "Mango",
            scientificName = "Mangifera indica",
            category = CropCategory.FRUITS,
            bestSeasonBn = "রোপণ: জুলাই - সেপ্টেম্বর",
            soilBn = "গভীর সুনিষ্কাশিত পলি দোআঁশ মাটি (pH ৬.৫ - ৭.৫)",
            seedRatePerBighaBn = "৪০ - ৫০ টি কলম চারা প্রতি একর (১০ মি × ১০ মি বা আল্ট্রা হাই ডেনসিটি)",
            spacingBn = "১০ মিটার × ১০ মিটার সাধারণ / ৫ মি × ২.৫ মি উচ্চ ঘনত্ব",
            plantDensityPerAcre = 45,
            harvestDays = 120,
            expectedYieldBn = "গাছ প্রতি ৫০ - ১৫০ কেজি ফল (পরিপক্ক অবস্থায়)",
            summaryBn = "মুকুল আসার সময় মেঘলা আবহাওয়ায় হপার পোকা ও পাউডারি মিলডিউ রোগের আক্রমণ বাড়ে। সঠিক ছাঁটাই ফলন বাড়ায়।",
            summaryHi = "बौर आने पर हॉपर कीट और चूर्णिल आसिता से बचाव करें।",
            summaryEn = "Long term orchard tree requiring canopy pruning and flower hopper management.",
            iconEmoji = "🥭",
            imageResId = R.drawable.img_mango_1793343080850,
            commonPests = listOf("আমের হপার পোকা (Mango Hopper)", "ফল মাছি (Fruit Fly)", "মিলিবাগ (Mealybug)"),
            commonDiseases = listOf("অ্যানথ্রাকনোজ (Anthracnose)", "পাউডারি মিলডিউ (Powdery Mildew)")
        ),
        MasterCrop(
            cropId = "banana",
            nameBn = "কলা (Banana)",
            nameHi = "केला (Banana)",
            nameEn = "Banana",
            scientificName = "Musa acuminata",
            category = CropCategory.FRUITS,
            bestSeasonBn = "জুন - আগস্ট বা সেপ্টেম্বর - অক্টোবর",
            soilBn = "উর্বর গভীর দোআঁশ, পর্যাপ্ত জৈব সার",
            seedRatePerBighaBn = "৪০০ - ৪৫০ টি সাকার / টিস্যু চারা প্রতি বিঘা",
            spacingBn = "১.৮ মিটার × ১.৮ মিটার",
            plantDensityPerAcre = 1200,
            harvestDays = 330,
            expectedYieldBn = "বিঘায় ৪০০ - ৫০০ কাঁদি কলা",
            summaryBn = "টিস্যু কালচার জি-৯ জাত চাষে রোগমুক্ত সমরূপ ফলন হয়। কাঁদি আসার পর বাঁশের ঠেকনো দেওয়া আবশ্যক।",
            summaryHi = "टिश्यू कल्चर जी-9 पौधे लगाएं और घार आने पर बांस का सहारा दें।",
            summaryEn = "Tissue culture G9 requires high potassium nutrition and bamboo propping for heavy bunches.",
            iconEmoji = "🍌",
            imageResId = R.drawable.img_banana_1793343260587,
            commonPests = listOf("কলা গুবরে পোকা (Rhizome Weevil)", "মাকড়"),
            commonDiseases = listOf("পানামা উইল্ট (Panama Wilt)", "সিগাটোকা পাতা পোড়া (Sigatoka)")
        ),
        MasterCrop(
            cropId = "papaya",
            nameBn = "পেঁপে (Papaya)",
            nameHi = "पपीता (Papaya)",
            nameEn = "Papaya",
            scientificName = "Carica papaya",
            category = CropCategory.FRUITS,
            bestSeasonBn = "সেপ্টেম্বর - অক্টোবর ও ফেব্রুয়ারি - মার্চ",
            soilBn = "সুনিষ্কাশিত বেলে দোআঁশ (গোড়ায় জল জমা যাবে না)",
            seedRatePerBighaBn = "২৫০ - ৩০০ টি চারা প্রতি বিঘা (রেড লেডি ৭৮৬)",
            spacingBn = "২ মিটার × ২ মিটার",
            plantDensityPerAcre = 900,
            harvestDays = 240,
            expectedYieldBn = "গাছ প্রতি ৪০ - ৭০ কেজি পেঁপে",
            summaryBn = "রেড লেডি হাইব্রিড উভয়লিঙ্গ জাতে প্রতিটি গাছেই ফল ধরে। শিকড়ে জল জমলে গাছ মারা যায়।",
            summaryHi = "जल जमाव से बचाएं। रेड लेडी 786 सबसे लोकप्रिय किस्म।",
            summaryEn = "Zero waterlogging tolerance. Red Lady 786 yields heavily with drip irrigation.",
            iconEmoji = "🍈"
        ),
        MasterCrop(
            cropId = "dragon_fruit",
            nameBn = "ড্রাগন ফল (Dragon Fruit)",
            nameHi = "ड्रैगन फ्रूट (Dragon Fruit)",
            nameEn = "Dragon Fruit",
            scientificName = "Hylocereus undatus",
            category = CropCategory.FRUITS,
            bestSeasonBn = "জুন - জুলাই রোপণ",
            soilBn = "বেলে দোআঁশ ও লাল মাটি, নিষ্কাশন জরুরি",
            seedRatePerBighaBn = "প্রতি খুঁটিতে ৪টি চারা, বিঘায় ১৫০টি খুঁটি",
            spacingBn = "৩ মিটার × ৩ মিটার পোল সিস্টেম",
            plantDensityPerAcre = 1800,
            harvestDays = 365,
            expectedYieldBn = "খুঁটি প্রতি ১৫ - ২৫ কেজি ফল প্রতি বছর",
            summaryBn = "সিমেন্টের খুঁটি ও রিং এর ওপর লতানো ক্যাকটাস। দ্বিতীয় বছর থেকে মে-অক্টোবর পর্যন্ত দফায় দফায় ফল ধরে।",
            summaryHi = "सीमेंट पोल और रिंग पर लगाएं। उच्च मुनाफे की फसल।",
            summaryEn = "Trellis pole cactus providing multiple harvest flushes from year two.",
            iconEmoji = "🌺"
        ),
        MasterCrop(
            cropId = "guava",
            nameBn = "পেয়ারা (Guava)",
            nameHi = "अमरूद (Guava)",
            nameEn = "Guava",
            scientificName = "Psidium guajava",
            category = CropCategory.FRUITS,
            bestSeasonBn = "জুন - আগস্ট",
            soilBn = "সব ধরনের মাটিতে হয়, দোআঁশ সেরা",
            seedRatePerBighaBn = "১০০ - ১৫০ টি কলম চারা প্রতি বিঘা (ভিএনআর বিহি জাত)",
            spacingBn = "৩ মিটার × ৩ মিটার মেদো বা হাই ডেনসিটি",
            plantDensityPerAcre = 400,
            harvestDays = 180,
            expectedYieldBn = "গাছ প্রতি ২৫ - ৪০ কেজি পেয়ারা",
            summaryBn = "ভিএনআর বিহি বা থাই-৭ জাত ব্যাগিং করলে চকচকে ও মাছি পোকা মুক্ত বড় পেয়ারা পাওয়া যায়।",
            summaryHi = "थाई-7 या वीएनआर बिही किस्म लगाएं और फलों पर बैगिंग करें।",
            summaryEn = "Fruit fly bagging gives blemish free large fruit with high market value.",
            iconEmoji = "🍏"
        )
    )

    private val builtInCrops: List<MasterCrop> by lazy {
        val all = mutableListOf<MasterCrop>()
        val existingIds = mutableSetOf<String>()

        fun appendDeduplicated(source: List<MasterCrop>) {
            for (crop in source) {
                val key = crop.cropId.trim().lowercase()
                if (key !in existingIds) {
                    existingIds.add(key)
                    all.add(crop)
                }
            }
        }

        appendDeduplicated(baseCrops)
        appendDeduplicated(LeafyVegetableDatabase.crops)
        appendDeduplicated(RootColeGourdDatabase.crops)
        appendDeduplicated(FruitSpicesPlantationDatabase.crops)
        appendDeduplicated(FieldCropsGrainsDatabase.crops)
        appendDeduplicated(HorticultureFlowerDatabase.crops)
        appendDeduplicated(SpicesPlantationNurseryTreesDatabase.crops)
        all
    }

    @Volatile
    private var customCropsList: List<MasterCrop> = emptyList()

    private val _cropsFlow = MutableStateFlow<List<MasterCrop>>(emptyList())
    val cropsFlow: StateFlow<List<MasterCrop>>
        get() {
            if (_cropsFlow.value.isEmpty()) {
                _cropsFlow.value = buildMergedCrops()
            }
            return _cropsFlow.asStateFlow()
        }

    val crops: List<MasterCrop>
        get() = buildMergedCrops()

    private fun buildMergedCrops(): List<MasterCrop> {
        if (customCropsList.isEmpty()) return builtInCrops
        val merged = LinkedHashMap<String, MasterCrop>(builtInCrops.size + customCropsList.size)
        for (c in builtInCrops) {
            merged[c.cropId.trim().lowercase()] = c
        }
        for (c in customCropsList) {
            merged[c.cropId.trim().lowercase()] = c
        }
        return merged.values.toList()
    }

    fun setCustomCrops(customCrops: List<MasterCrop>) {
        customCropsList = customCrops
        _cropsFlow.value = buildMergedCrops()
    }

    fun addOrUpdateCustomCrop(crop: MasterCrop) {
        val cleanId = crop.cropId.trim().lowercase()
        val updated = customCropsList.filterNot { it.cropId.trim().lowercase() == cleanId } + crop
        setCustomCrops(updated)
    }

    fun getCropById(cropId: String): MasterCrop? {
        val clean = cropId.trim().lowercase()
        return crops.find { 
            it.cropId.equals(clean, ignoreCase = true) || 
            it.aliases.any { alias -> alias.equals(clean, ignoreCase = true) }
        }
    }

    fun getCropOrDefault(name: String, category: String?): MasterCrop {
        val cleanName = name.trim()
        val found = crops.find {
            it.cropId.equals(cleanName, ignoreCase = true) ||
            it.nameBn.contains(cleanName, ignoreCase = true) ||
            it.nameEn.contains(cleanName, ignoreCase = true) ||
            it.nameHi.contains(cleanName, ignoreCase = true) ||
            it.aliases.any { alias -> alias.contains(cleanName, ignoreCase = true) || cleanName.contains(alias, ignoreCase = true) } ||
            (category != null && it.category.code.equals(category, ignoreCase = true))
        }
        if (found != null) return found

        return MasterCrop(
            cropId = name.lowercase().replace(" ", "_"),
            nameBn = name,
            nameHi = name,
            nameEn = name,
            scientificName = "Cultivated Variety",
            category = CropCategory.VEGETABLES,
            bestSeasonBn = "উপযুক্ত ঋতুতে চাষযোগ্য",
            soilBn = "উর্বর দোআঁশ মাটি",
            seedRatePerBighaBn = "বিঘায় ১-২ কেজি বা চারা",
            spacingBn = "৬০ সেমি × ৪৫ সেমি",
            harvestDays = 60,
            expectedYieldBn = "বিঘায় ১৫-২০ কুইন্টাল",
            summaryBn = "$name চাষের জন্য সুষম সার ও সময়মতো সেচ আবশ্যক।",
            summaryHi = "$name की उचित देखभाल करें।",
            summaryEn = "Guide for $name cultivation.",
            iconEmoji = "🌱"
        )
    }
}
