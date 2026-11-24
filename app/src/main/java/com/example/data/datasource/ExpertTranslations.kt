package com.example.data.datasource

import com.example.data.local.entity.ExpertEntity
import com.example.data.model.AppLanguage

object ExpertTranslations {

    private data class LocalizedExpertText(
        val nameBn: String,
        val nameHi: String,
        val nameEn: String,
        val designationBn: String,
        val designationHi: String,
        val designationEn: String,
        val institutionBn: String,
        val institutionHi: String,
        val institutionEn: String,
        val districtsBn: String,
        val districtsHi: String,
        val districtsEn: String,
        val cropsBn: String,
        val cropsHi: String,
        val cropsEn: String,
        val problemsBn: String,
        val problemsHi: String,
        val problemsEn: String
    )

    private val expertMap: Map<String, LocalizedExpertText> = mapOf(
        "exp_01" to LocalizedExpertText(
            nameBn = "ড. অরুণাভ রায়",
            nameHi = "डॉ. अरुणाभ राय",
            nameEn = "Dr. Arunabha Roy",
            designationBn = "সিনিয়র উদ্ভিদ রোগতত্ত্ববিদ",
            designationHi = "वरिष्ठ पादप रोग विशेषज्ञ",
            designationEn = "Senior Plant Pathologist",
            institutionBn = "বিধান চন্দ্র কৃষি বিশ্ববিদ্যালয় (BCKV মোহনপুর)",
            institutionHi = "बिधान चंद्र कृषि विश्वविद्यालय (BCKV मोहनपुर)",
            institutionEn = "Bidhan Chandra Krishi Viswavidyalaya (BCKV Mohanpur)",
            districtsBn = "জলপাইগুড়ি, কোচবিহার, মালদা, নদিয়া, বর্ধমান",
            districtsHi = "जलपाईगुड़ी, कूचबिहार, मालदा, नदिया, बर्दवान",
            districtsEn = "Jalpaiguri, Cooch Behar, Malda, Nadia, Bardhaman",
            cropsBn = "আলু, ধান, টমেটো, বেগুন, মরিচ",
            cropsHi = "आलू, धान, टमाटर, बैंगन, मिर्च",
            cropsEn = "Potato, Paddy, Tomato, Brinjal, Chilli",
            problemsBn = "আলুর নাবী ধসা (Late Blight), ব্যাকটেরিয়াল উইল্ট, গোড়া পচা, শীথ ব্লাইট",
            problemsHi = "आलू का पछेती झुलसा (Late Blight), जीवाणु मुरझान, जड़ सड़न, शीथ ब्लाइट",
            problemsEn = "Late Blight, Bacterial Wilt, Root Rot, Sheath Blight"
        ),
        "exp_02" to LocalizedExpertText(
            nameBn = "ড. প্রিয়াঙ্কা সেনগুপ্ত",
            nameHi = "डॉ. प्रियंका सेनगुप्ता",
            nameEn = "Dr. Priyanka Sengupta",
            designationBn = "মুখ্য ফসল সুরক্ষা বিশেষজ্ঞ",
            designationHi = "मुख्य फसल सुरक्षा वैज्ञानिक",
            designationEn = "Chief Crop Protection Scientist",
            institutionBn = "উত্তর বঙ্গ কৃষি বিশ্ববিদ্যালয় (UBKV পুণ্ডিবাড়ী)",
            institutionHi = "उत्तर बंग कृषि विश्वविद्यालय (UBKV पुंडीबाड़ी)",
            institutionEn = "Uttar Banga Krishi Viswavidyalaya (UBKV Pundibari)",
            districtsBn = "জলপাইগুড়ি, আলিপুরদুয়ার, কোচবিহার, শিলিগুড়ি",
            districtsHi = "जलपाईगुड़ी, अलीपुरद्वार, कूचबिहार, सिलीगुड़ी",
            districtsEn = "Jalpaiguri, Alipurduar, Cooch Behar, Siliguri",
            cropsBn = "সবজি, ফল, চা, ধান",
            cropsHi = "सब्जियाँ, फल, चाय, धान",
            cropsEn = "Vegetables, Fruits, Tea, Paddy",
            problemsBn = "মাজরা পোকা, শোষক পোকা, লাল মাকড়, থ্রিপস ও ফল ছিদ্রকারী পোকা দমন",
            problemsHi = "तना छेदक, रस चूसक कीट, लाल मकड़ी, थ्रिप्स व फल छेदक नियंत्रण",
            problemsEn = "Stem Borer, Sucking Pests, Red Spider Mite, Thrips & Fruit Borer"
        ),
        "exp_03" to LocalizedExpertText(
            nameBn = "প্রফেসর সমীরণ মন্ডল",
            nameHi = "प्रो. समीरण मंडल",
            nameEn = "Prof. Samiran Mondal",
            designationBn = "মাটি পরীক্ষা ও পুষ্টি বিশেষজ্ঞ",
            designationHi = "प्रमुख मृदा वैज्ञानिक",
            designationEn = "Principal Soil Scientist",
            institutionBn = "কৃষি বিজ্ঞান কেন্দ্র, জলপাইগুড়ি (KVK)",
            institutionHi = "कृषि विज्ञान केंद्र, जलपाईगुड़ी (KVK)",
            institutionEn = "Krishi Vigyan Kendra, Jalpaiguri (KVK)",
            districtsBn = "জলপাইগুড়ি, মালদা, দিনাজপুর, মুর্শিদাবাদ",
            districtsHi = "जलपाईगुड़ी, मालदा, दिनाजपुर, मुर्शिदाबाद",
            districtsEn = "Jalpaiguri, Malda, Dinajpur, Murshidabad",
            cropsBn = "মাঠ ফসল, ডালশস্য, তৈলবীজ, শাকসবজি",
            cropsHi = "मैदानी फसलें, दलहन, तिलहन, सब्जियाँ",
            cropsEn = "Field Crops, Pulses, Oilseeds, Vegetables",
            problemsBn = "মাটির অম্লতা সংশোধন, জিঙ্ক ও বোরন ঘাটতি, সয়েল হেলথ কার্ড বিশ্লেষণ",
            problemsHi = "मिट्टी की अम्लता सुधार, जिंक व बोरॉन की कमी, मृदा स्वास्थ्य कार्ड विश्लेषण",
            problemsEn = "Soil Acidity Reclamation, Zinc & Boron Deficiency, Soil Health Card Analysis"
        ),
        "exp_04" to LocalizedExpertText(
            nameBn = "ড. শুভাশিস ভৌমিক",
            nameHi = "डॉ. शुभाशीष भौमिक",
            nameEn = "Dr. Subhasis Bhowmick",
            designationBn = "আলু চাষ ও বীজ প্রত্যয়ন বিশেষজ্ঞ",
            designationHi = "आलू कृषि एवं बीज विशेषज्ञ",
            designationEn = "Potato Agronomist & Seed Specialist",
            institutionBn = "কেন্দ্রীয় আলু গবেষণা কেন্দ্র সহযোগী বিজ্ঞানী (CPRI)",
            institutionHi = "केंद्रीय आलू अनुसंधान संस्थान सहयोगी वैज्ञानिक (CPRI)",
            institutionEn = "Central Potato Research Institute Associate (CPRI)",
            districtsBn = "জলপাইগুড়ি, ধূপগুড়ি, ময়নাগুড়ি, হুগলি, বাঁকুড়া",
            districtsHi = "जलपाईगुड़ी, धूपगुड़ी, मैनागुड़ी, हुगली, बांकुड़ा",
            districtsEn = "Jalpaiguri, Dhupguri, Maynaguri, Hooghly, Bankura",
            cropsBn = "আলু (জ্যোতি, চন্দ্রমুখী, পোখরাজ, কুফরি সূর্য)",
            cropsHi = "आलू (ज्योति, चंद्रमुखी, पोखराज, कुफरी सूर्य)",
            cropsEn = "Potato (Jyoti, Chandramukhi, Pukhraj, Kufri Surya)",
            problemsBn = "বীজ আলু শোধন, ব্লাইট প্রতিরোধ, কন্দ পচা ও ফলন বৃদ্ধির কৌশল",
            problemsHi = "बीज आलू उपचार, झुलसा रोकथाम, कंद सड़न व उपज बढ़ाने की तकनीक",
            problemsEn = "Seed Tuber Treatment, Blight Control, Tuber Rot & Yield Optimization"
        ),
        "exp_05" to LocalizedExpertText(
            nameBn = "ড. দেবব্রত চক্রবর্তী",
            nameHi = "डॉ. देबब्रत चक्रवर्ती",
            nameEn = "Dr. Debabrata Chakraborty",
            designationBn = "চা বাগান ও চারা বিশেষজ্ঞ",
            designationHi = "चाय बागान एवं नर्सरी सलाहकार",
            designationEn = "Tea Cultivation & Nursery Advisor",
            institutionBn = "টি রিসার্চ অ্যাসোসিয়েশন (TRA টোকলাই)",
            institutionHi = "टी रिसर्च एसोसिएशन (TRA टोकलाई)",
            institutionEn = "Tea Research Association (TRA Tocklai North Bengal)",
            districtsBn = "জলপাইগুড়ি, ডুয়ার্স, আলিপুরদুয়ার, দার্জিলিং",
            districtsHi = "जलपाईगुड़ी, डुआर्स, अलीपुरद्वार, दार्जिलिंग",
            districtsEn = "Jalpaiguri, Dooars, Alipurduar, Darjeeling",
            cropsBn = "ক্ষুদ্র চা বাগান, ক্লোনাল নার্সারি",
            cropsHi = "छोटे चाय बागान, क्लोनल नर्सरी",
            cropsEn = "Small Tea Gardens, Clonal Tea Nurseries",
            problemsBn = "টি মসকুইটো বাগ, লাল মাকড়, প্রুনিং ক্যালেন্ডার, খরা ব্যবস্থাপনা",
            problemsHi = "टी मॉस्किटो बग, लाल मकड़ी, छंटाई कैलेंडर व सूखा प्रबंधन",
            problemsEn = "Tea Mosquito Bug, Red Spider, Pruning Calendar, Drought Management"
        ),
        "exp_06" to LocalizedExpertText(
            nameBn = "ড. মৌসুমী দাস",
            nameHi = "डॉ. मौसमी दास",
            nameEn = "Dr. Moushumi Das",
            designationBn = "ধান চাষ ও বীজ বিজ্ঞানী",
            designationHi = "धान प्रजनक व कृषि वैज्ञानिक",
            designationEn = "Rice Breeder & Agronomist",
            institutionBn = "রাইস রিসার্চ স্টেশন, চুঁচুড়া",
            institutionHi = "चावल अनुसंधान केंद्र, चुंचुड़ा",
            institutionEn = "Chinsurah Rice Research Station",
            districtsBn = "বর্ধমান, জলপাইগুড়ি, নদিয়া, বীরভূম",
            districtsHi = "बर्दवान, जलपाईगुड़ी, नदिया, बीरभूम",
            districtsEn = "Bardhaman, Jalpaiguri, Nadia, Birbhum",
            cropsBn = "আমন ধান, বোরো ধান, সুগন্ধি চাল (গোবিন্দভোগ)",
            cropsHi = "अमन धान, बोरो धान, सुगंधित चावल (गोविंदभोग)",
            cropsEn = "Aman Paddy, Boro Paddy, Aromatic Rice (Gobindobhog)",
            problemsBn = "ব্লাস্ট রোগ, বাদামি গাছফড়িং (BPH), ফলস স্মাট, ইউরিয়া বিভাজন",
            problemsHi = "ब्लास्ट रोग, भूरा फुदका (BPH), फॉल्स स्मट व यूरिया प्रबंधन",
            problemsEn = "Rice Blast, Brown Planthopper (BPH), False Smut, Urea Splitting"
        ),
        "exp_07" to LocalizedExpertText(
            nameBn = "ড. অমিত কুমার বিশ্বাস",
            nameHi = "डॉ. अमित कुमार बिस्वास",
            nameEn = "Dr. Amit Kumar Biswas",
            designationBn = "সার ও পুষ্টি বিশেষজ্ঞ",
            designationHi = "पादप पोषण व उर्वरक विशेषज्ञ",
            designationEn = "Plant Nutrition Specialist",
            institutionBn = "কৃষি অধিদপ্তর, পশ্চিমবঙ্গ সরকার",
            institutionHi = "कृषि विभाग, पश्चिम बंगाल सरकार",
            institutionEn = "Department of Agriculture, Govt. of West Bengal",
            districtsBn = "সমগ্র পশ্চিমবঙ্গ (সকল জেলা)",
            districtsHi = "समस्त पश्चिम बंगाल (सभी जिले)",
            districtsEn = "All Districts of West Bengal",
            cropsBn = "শাকসবজি, আলু, ধান, ভুট্টা, সরিষা",
            cropsHi = "सब्जियाँ, आलू, धान, मक्का, सरसों",
            cropsEn = "Vegetables, Potato, Paddy, Maize, Mustard",
            problemsBn = "NPK সারের সঠিক অনুপাত, ন্যানো ইউরিয়া, জিঙ্ক ও ম্যাগনেসিয়াম ঘাটতি",
            problemsHi = "NPK का सही अनुपात, नैनो यूरिया, जिंक व मैग्नीशियम की कमी",
            problemsEn = "Balanced NPK Ratios, Nano Urea, Zinc & Magnesium Deficiency"
        ),
        "exp_08" to LocalizedExpertText(
            nameBn = "ড. তপোময় মুখার্জী",
            nameHi = "डॉ. तपोमय मुखर्जी",
            nameEn = "Dr. Tapomay Mukherjee",
            designationBn = "সবজি ও সংকর জাত বিশেষজ্ঞ",
            designationHi = "सब्जी एवं संकर किस्म विशेषज्ञ",
            designationEn = "Vegetable & Hybrid Crop Specialist",
            institutionBn = "উদ্যানপালন মহাবিদ্যালয়, পুণ্ডিবাড়ী (UBKV)",
            institutionHi = "बागवानी संकाय, UBKV पुंडीबाड़ी",
            institutionEn = "Faculty of Horticulture, UBKV Pundibari",
            districtsBn = "জলপাইগুড়ি, মালদা, মুর্শিদাবাদ, নদিয়া",
            districtsHi = "जलपाईगुड़ी, मालदा, मुर्शिदाबाद, नदिया",
            districtsEn = "Jalpaiguri, Malda, Murshidabad, Nadia",
            cropsBn = "বাঁধাকপি, ফুলকপি, টমেটো, পটল, উচ্ছে, বেগুন",
            cropsHi = "पत्ता गोभी, फूल गोभी, टमाटर, परवल, करेला, बैंगन",
            cropsEn = "Cabbage, Cauliflower, Tomato, Pointed Gourd, Bitter Gourd, Brinjal",
            problemsBn = "ড্যাম্পিং অফ, ফুলকপির ক্লাব রুট, আগাম শীতকালীন সবজির নার্সারি",
            problemsHi = "डैम्पिंग ऑफ, फूलगोभी का क्लब रूट, अगेती शीतकालीन नर्सरी",
            problemsEn = "Damping Off, Cauliflower Club Root, Early Winter Vegetable Nursery"
        ),
        "exp_09" to LocalizedExpertText(
            nameBn = "ডাঃ পার্থ প্রতীম সাহা",
            nameHi = "डॉ. पार्थ प्रतीम साहा",
            nameEn = "Dr. Partha Pratim Saha",
            designationBn = "ভেটেরিনারি সার্জন ও পশুপালন বিশেষজ্ঞ",
            designationHi = "पशु चिकित्सक एवं पशुपालन विशेषज्ञ",
            designationEn = "Veterinary Surgeon & Livestock Specialist",
            institutionBn = "জেলা প্রাণিসম্পদ বিকাশ বিভাগ, পশ্চিমবঙ্গ",
            institutionHi = "जिला पशुधन विकास विभाग, पश्चिम बंगाल",
            institutionEn = "District Animal Resources Development Dept., WB",
            districtsBn = "জলপাইগুড়ি, শিলিগুড়ি, আলিপুরদুয়ার",
            districtsHi = "जलपाईगुड़ी, सिलीगुड़ी, अलीपुरद्वार",
            districtsEn = "Jalpaiguri, Siliguri, Alipurduar",
            cropsBn = "গরু, ছাগল, মুরগি, হাঁস পালন",
            cropsHi = "गाय, बकरी, मुर्गी व बत्तख पालन",
            cropsEn = "Dairy Cattle, Goat, Poultry & Duck Farming",
            problemsBn = "দুগ্ধবতী গাভীর ম্যাস্টাইটিস, কৃমি নিয়ন্ত্রণ, সুষম সাইলেজ ও খাদ্য তালিকা",
            problemsHi = "दुधारू गायों में थनैला रोग, कृमि नियंत्रण, संतुलित चारा व साइलेज",
            problemsEn = "Dairy Mastitis, Deworming, Balanced Silage & Feed Formulation"
        ),
        "exp_10" to LocalizedExpertText(
            nameBn = "ড. ইন্দ্রনীল রায়চৌধুরী",
            nameHi = "डॉ. इंद्रनील रॉयचौधरी",
            nameEn = "Dr. Indranil Roychowdhury",
            designationBn = "জৈব কৃষি ও ভার্মিকম্পোস্ট বিশেষজ্ঞ",
            designationHi = "जैविक खेती एवं वर्मीकम्पोस्ट विशेषज्ञ",
            designationEn = "Organic Farming & Vermicompost Specialist",
            institutionBn = "জৈব কৃষি বিকাশ ট্রাস্ট ও রাজ্য সমন্বয়ক",
            institutionHi = "जैविक कृषि विकास ट्रस्ट एवं राज्य समन्वयक",
            institutionEn = "Organic Agriculture Development Trust & State Coordinator",
            districtsBn = "দার্জিলিং, কালিম্পং, জলপাইগুড়ি, বর্ধমান",
            districtsHi = "दार्जिलिंग, कलिम्पोंग, जलपाईगुड़ी, बर्दवान",
            districtsEn = "Darjeeling, Kalimpong, Jalpaiguri, Bardhaman",
            cropsBn = "জৈব চা, জৈব সবজি, মশলাপাতি, তুলসী ও ভেষজ",
            cropsHi = "जैविक चाय, जैविक सब्जियाँ, मसाले, तुलसी व औषधीय पौधे",
            cropsEn = "Organic Tea, Organic Vegetables, Spices, Tulsi & Medicinal Herbs",
            problemsBn = "জীবাণু সার, ট্রাইকোডার্মা ভিরিডি, পঞ্চগব্য তৈরি ও জৈব সার্টিফিকেশন",
            problemsHi = "जैव उर्वरक, ट्राइकोडर्मा विरिडी, पंचगव्य निर्माण व जैविक प्रमाणन",
            problemsEn = "Bio-Fertilizers, Trichoderma Viride, Panchagavya & Organic Certification"
        )
    )

    fun getDisplayName(expert: ExpertEntity, lang: AppLanguage): String {
        val loc = expertMap[expert.id] ?: return stripForNonBengali(expert.name, lang)
        return when (lang) {
            AppLanguage.BENGALI -> loc.nameBn
            AppLanguage.HINDI -> loc.nameHi
            AppLanguage.ENGLISH -> loc.nameEn
        }
    }

    fun getDisplayDesignation(expert: ExpertEntity, lang: AppLanguage): String {
        val loc = expertMap[expert.id] ?: return stripForNonBengali(expert.designation, lang)
        return when (lang) {
            AppLanguage.BENGALI -> loc.designationBn
            AppLanguage.HINDI -> loc.designationHi
            AppLanguage.ENGLISH -> loc.designationEn
        }
    }

    fun getDisplayInstitution(expert: ExpertEntity, lang: AppLanguage): String {
        val loc = expertMap[expert.id] ?: return stripForNonBengali(expert.institution, lang)
        return when (lang) {
            AppLanguage.BENGALI -> loc.institutionBn
            AppLanguage.HINDI -> loc.institutionHi
            AppLanguage.ENGLISH -> loc.institutionEn
        }
    }

    fun getDisplayDistricts(expert: ExpertEntity, lang: AppLanguage): String {
        val loc = expertMap[expert.id] ?: return stripForNonBengali(expert.districtsServed, lang)
        return when (lang) {
            AppLanguage.BENGALI -> loc.districtsBn
            AppLanguage.HINDI -> loc.districtsHi
            AppLanguage.ENGLISH -> loc.districtsEn
        }
    }

    fun getDisplayCrops(expert: ExpertEntity, lang: AppLanguage): String {
        val loc = expertMap[expert.id] ?: return stripForNonBengali(expert.supportedCrops, lang)
        return when (lang) {
            AppLanguage.BENGALI -> loc.cropsBn
            AppLanguage.HINDI -> loc.cropsHi
            AppLanguage.ENGLISH -> loc.cropsEn
        }
    }

    fun getDisplayProblems(expert: ExpertEntity, lang: AppLanguage): String {
        val loc = expertMap[expert.id] ?: return stripForNonBengali(expert.specializedProblems, lang)
        return when (lang) {
            AppLanguage.BENGALI -> loc.problemsBn
            AppLanguage.HINDI -> loc.problemsHi
            AppLanguage.ENGLISH -> loc.problemsEn
        }
    }

    fun getDisplayLanguages(expert: ExpertEntity, lang: AppLanguage): String {
        return if (lang == AppLanguage.BENGALI) {
            expert.languages
        } else {
            expert.languages.replace("বাংলা", if (lang == AppLanguage.HINDI) "बांग्ला" else "Bengali")
        }
    }

    fun getDisplayFee(fee: String, lang: AppLanguage): String {
        if (fee.contains("বিনামূল্যে") || fee.contains("Free", ignoreCase = true)) {
            return when (lang) {
                AppLanguage.BENGALI -> "বিনামূল্যে (সরকারি সহায়তা)"
                AppLanguage.HINDI -> "निःशुल्क (सरकारी सहायता)"
                AppLanguage.ENGLISH -> "Free (Govt. Supported)"
            }
        }
        return fee
    }

    fun getAppointmentStatusLabel(status: String, lang: AppLanguage): String = when (status) {
        "CONFIRMED" -> when (lang) {
            AppLanguage.BENGALI -> "নিশ্চিত (Confirmed)"
            AppLanguage.HINDI -> "पुष्ट (Confirmed)"
            AppLanguage.ENGLISH -> "Confirmed"
        }
        "PENDING" -> when (lang) {
            AppLanguage.BENGALI -> "বিচারাধীন (Pending)"
            AppLanguage.HINDI -> "लंबित (Pending)"
            AppLanguage.ENGLISH -> "Pending"
        }
        "REJECTED" -> when (lang) {
            AppLanguage.BENGALI -> "প্রত্যাখ্যাত (Rejected)"
            AppLanguage.HINDI -> "अस्वीकृत (Rejected)"
            AppLanguage.ENGLISH -> "Rejected"
        }
        "COMPLETED" -> when (lang) {
            AppLanguage.BENGALI -> "সম্পন্ন (Completed)"
            AppLanguage.HINDI -> "पूर्ण (Completed)"
            AppLanguage.ENGLISH -> "Completed"
        }
        "CANCELLED" -> when (lang) {
            AppLanguage.BENGALI -> "বাতিল (Cancelled)"
            AppLanguage.HINDI -> "रद्द (Cancelled)"
            AppLanguage.ENGLISH -> "Cancelled"
        }
        else -> status
    }

    fun getConsultationMethodLabel(method: String, lang: AppLanguage): String = when (method) {
        "VIDEO_CALL" -> when (lang) {
            AppLanguage.BENGALI -> "ভিডিও কল"
            AppLanguage.HINDI -> "वीडियो कॉल"
            AppLanguage.ENGLISH -> "Video Call"
        }
        "AUDIO_CALL" -> when (lang) {
            AppLanguage.BENGALI -> "অডিও কল"
            AppLanguage.HINDI -> "ऑडियो कॉल"
            AppLanguage.ENGLISH -> "Audio Call"
        }
        "CHAT" -> when (lang) {
            AppLanguage.BENGALI -> "সরাসরি চ্যাট"
            AppLanguage.HINDI -> "लाइव चैट"
            AppLanguage.ENGLISH -> "Direct Chat"
        }
        "FARM_VISIT" -> when (lang) {
            AppLanguage.BENGALI -> "মাঠে পরিদর্শন"
            AppLanguage.HINDI -> "खेत का दौरा"
            AppLanguage.ENGLISH -> "Farm Visit"
        }
        else -> method
    }

    fun getDisplayAppointmentExpertName(expertId: String, rawName: String, lang: AppLanguage): String {
        val loc = expertMap[expertId]
        if (loc != null) {
            return when (lang) {
                AppLanguage.BENGALI -> loc.nameBn
                AppLanguage.HINDI -> loc.nameHi
                AppLanguage.ENGLISH -> loc.nameEn
            }
        }
        return stripForNonBengali(rawName, lang)
    }

    fun getDisplaySampleText(raw: String, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI) return raw
        return when (raw) {
            "আলু (কুফরি জ্যোতি)" -> if (lang == AppLanguage.HINDI) "आलू (कुफरी ज्योति)" else "Potato (Kufri Jyoti)"
            "বীজ আলুর দাগ ও কাটা রোগ" -> if (lang == AppLanguage.HINDI) "बीज आलू में दाग व सड़न समस्या" else "Seed potato blemishes & cut rot"
            "সাধারণ ফসল" -> if (lang == AppLanguage.HINDI) "सामान्य फसल" else "General Crop"
            "বিশেষজ্ঞের সার্বিক পরামর্শ" -> if (lang == AppLanguage.HINDI) "विशेषज्ञ का समग्र परामर्श" else "General expert consultation"
            "কৃষক নিজে বাতিল করেছেন" -> if (lang == AppLanguage.HINDI) "किसान द्वारा रद्द किया गया" else "Cancelled by farmer"
            "কৃষি বিশ্ববিদ্যালয় ও ডিগ্রি যাচাইকরণ সফল।" -> if (lang == AppLanguage.HINDI) "कृषि विश्वविद्यालय व डिग्री सत्यापन सफल।" else "University degree & registration verified."
            "নথিপত্রে তথ্যের অসংগতি।" -> if (lang == AppLanguage.HINDI) "दस्तावेजों में जानकारी की विसंगति।" else "Discrepancy found in submitted documents."
            else -> stripForNonBengali(raw, lang)
        }
    }

    private fun stripForNonBengali(raw: String, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI) return raw
        val hasBengali = raw.any { it in '\u0980'..'\u09FF' }
        if (!hasBengali) return raw
        val parenMatch = Regex("\\(([^()]+)\\)").findAll(raw)
            .map { it.groupValues[1].trim() }
            .firstOrNull { candidate -> candidate.none { ch -> ch in '\u0980'..'\u09FF' } && candidate.any { ch -> ch.isLetter() } }
        if (parenMatch != null) return parenMatch
        return when (raw.trim()) {
            "কৃষি বিজ্ঞানী" -> if (lang == AppLanguage.HINDI) "कृषि वैज्ञानिक" else "Agricultural Scientist"
            "কৃষি সম্প্রসারণ অধিদপ্তর" -> if (lang == AppLanguage.HINDI) "कृषि विस्तार विभाग" else "Department of Agricultural Extension"
            "পশ্চিমবঙ্গের সকল জেলা" -> if (lang == AppLanguage.HINDI) "पश्चिम बंगाल के सभी जिले" else "All Districts of West Bengal"
            "আলু, ধান, শাকসবজি" -> if (lang == AppLanguage.HINDI) "आलू, धान, सब्जियाँ" else "Potato, Paddy, Vegetables"
            "আলু" -> if (lang == AppLanguage.HINDI) "आलू" else "Potato"
            else -> raw.replace(Regex("[\u0980-\u09FF]+"), "").trim().ifBlank {
                if (lang == AppLanguage.HINDI) "अतिरिक्त जानकारी" else "Additional Info"
            }
        }
    }
}
