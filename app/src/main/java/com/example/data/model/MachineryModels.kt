package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

enum class MachineryCategory(
    val code: String,
    val enName: String,
    val bnName: String,
    val hiName: String,
    val emoji: String,
    val subcategoriesEn: List<String>,
    val subcategoriesBn: List<String>,
    val subcategoriesHi: List<String>
) {
    TRACTORS_LAND_PREP(
        code = "TRACTORS_LAND_PREP",
        enName = "Tractors & Land Preparation",
        bnName = "ট্রাক্টর ও জমি চাষ যন্ত্রপাতি",
        hiName = "ट्रैक्टर एवं भूमि तैयारी मशीनरी",
        emoji = "🚜",
        subcategoriesEn = listOf(
            "Farm Tractor", "Mini Tractor", "Garden Tractor", "4WD Tractor", "Electric Tractor",
            "Tractor Cabin & Shade Canopy", "Power Tiller", "Rotavator / Rotary Tiller",
            "Mini Tiller / Weeder", "Cultivator", "Disc Harrow", "MB Plough / Mouldboard Plough",
            "Subsoiler", "Laser Land Leveler", "Soil Leveler", "Post Hole Digger",
            "Trench Digger / Earth Auger / Bund Maker"
        ),
        subcategoriesBn = listOf(
            "ফার্ম ট্রাক্টর", "মিনি ট্রাক্টর", "গার্ডেন ট্রাক্টর", "4WD ট্রাক্টর", "ইলেকট্রিক ট্রাক্টর",
            "ট্রাক্টর কেবিন ও শেড ক্যানোপি", "পাওয়ার টিলার", "রোটাভেটর / রোটারি টিলার",
            "মিনি টিলার / উইডার", "কাল্টিভেটর", "ডিস্ক হ্যারো", "এমবি লাঙ্গল (MB Plough)",
            "সাবসয়লার", "লেজার ল্যান্ড লেভেলার", "সয়েল লেভেলার", "পোস্ট হোল ডিগার",
            "ট্রেঞ্চ ডিগার / আর্থ অগার / বান্ড মেকার"
        ),
        subcategoriesHi = listOf(
            "फार्म ट्रैक्टर", "मिनी ट्रैक्टर", "गार्डन ट्रैक्टर", "4WD ट्रैक्टर", "इलेक्ट्रिक ट्रैक्टर",
            "ट्रैक्टर केबिन व शेड कैनोपी", "पावर टिलर", "रोटावेटर / रोटरी टिलर",
            "मिनी टिलर / वीडर", "कल्टीवेटर", "डिस्क हैरो", "एमबी हल (MB Plough)",
            "सबसॉयलर", "लेज़र लैंड लेवलर", "सॉयल लेवलर", "पोस्ट होल डिगर",
            "ट्रेंच डिगर / अर्थ ऑगर / बंड मेकर"
        )
    ),
    SOIL_EARTH_MOVING(
        code = "SOIL_EARTH_MOVING",
        enName = "Soil Digging, Trenching & Earth Moving",
        bnName = "মাটি খনন, নালা কাটা ও আর্থ মুভিং",
        hiName = "मिट्टी खुदाई, खाई एवं अर्थ मूविंग",
        emoji = "⛏️",
        subcategoriesEn = listOf(
            "Agricultural Tractor Front Loader", "Backhoe Attachment for Tractor",
            "Mini Excavator / Agriculture Trencher", "Earth Auger (Manual / Power)",
            "Pond Digging Equipment", "Farm Earth Leveling Blade", "Soil Scoop & Ditcher"
        ),
        subcategoriesBn = listOf(
            "ট্রাক্টর ফ্রন্ট লোডার", "ট্রাক্টরের ব্যাকহো সংযুক্তি",
            "মিনি এক্সকাভেটর / কৃষি ট্রেঞ্চার", "আর্থ অগার (হস্তচালিত / পাওয়ার)",
            "পুকুর খনন যন্ত্রপাতি", "জমি সমতল ব্লেড", "সয়েল স্কুপ ও ডিচার"
        ),
        subcategoriesHi = listOf(
            "ट्रैक्टर फ्रंट लोडर", "ट्रैक्टर बैकहो अटैचमेंट",
            "मिनी एक्सकावेटर / कृषि ट्रेंचर", "अर्थ ऑगर (मैनुअल / पावर)",
            "तालाब खुदाई उपकरण", "खेत समतल ब्लेड", "सॉयल स्कूप व डिचर"
        )
    ),
    SEEDING_PLANTING(
        code = "SEEDING_PLANTING",
        enName = "Seeding & Planting Machinery",
        bnName = "বীজ বপন ও চারা রোপণ যন্ত্র",
        hiName = "बुवाई एवं रोपाई मशीनरी",
        emoji = "🌱",
        subcategoriesEn = listOf(
            "Seed Drill", "Multi-Crop Planter", "Seed-Cum-Fertilizer Drill", "Zero-Till Seed Drill",
            "Rice Transplanter", "Potato Planter", "Sugarcane Planter", "Vegetable Plug / Nursery Seedling Transplanter"
        ),
        subcategoriesBn = listOf(
            "সিড ড্রিল", "মাল্টি-ক্রপ প্ল্যান্টার", "বীজ ও সার একসাথে বপন যন্ত্র", "জিরো-টিল সিড ড্রিল",
            "ধান রোপণ যন্ত্র (রাইস ট্রান্সপ্ল্যান্টার)", "আলু রোপণ যন্ত্র", "আখ রোপণ যন্ত্র", "সবজি ও নার্সারি চারা রোপণ যন্ত্র"
        ),
        subcategoriesHi = listOf(
            "सीड ड्रिल", "मल्टी-क्रॉप प्लांटर", "सीड-कम-फर्टिलाइज़र ड्रिल", "ज़ीरो-टिल सीड ड्रिल",
            "धान रोपाई मशीन (राइस ट्रांसप्लांटर)", "आलू बुवाई मशीन", "गन्ना बुवाई मशीन", "सब्जी व नर्सरी पौध रोपाई यंत्र"
        )
    ),
    IRRIGATION_WATER(
        code = "IRRIGATION_WATER",
        enName = "Irrigation & Water Management",
        bnName = "সেচ ও জল ব্যবস্থাপনা যন্ত্র",
        hiName = "सिंचाई एवं जल प्रबंधन उपकरण",
        emoji = "💧",
        subcategoriesEn = listOf(
            "Diesel Water Pump", "Electric Centrifugal Water Pump", "Submersible Agricultural Pump",
            "Solar Agricultural Water Pump", "Borewell & Tube-Well Accessories",
            "Drip Irrigation System", "Drip Filter & Emitter Kit", "Sprinkler Irrigation System",
            "Rain Gun Irrigation System", "Delivery Hose Pipe & Valves", "Automated Timer & Water Flow Controller"
        ),
        subcategoriesBn = listOf(
            "ডিজেল ওয়াটার পাম্প", "ইলেকট্রিক সেন্ট্রিফিউগাল পাম্প", "সাবমার্সিবল কৃষি পাম্প",
            "সোলার কৃষি ওয়াটার পাম্প", "বোরওয়েল ও টিউবওয়েল যন্ত্রাংশ",
            "ড্রিপ সেচ ব্যবস্থা", "ড্রিপ ফিল্টার ও এমিটার কিট", "স্প্রিংকলার সেচ ব্যবস্থা",
            "রেইন গান সেচ ব্যবস্থা", "ডেলিভারি হোস পাইপ ও ভালভ", "স্বয়ংক্রিয় টাইমার ও জল নিয়ন্ত্রক"
        ),
        subcategoriesHi = listOf(
            "डीज़ल वाटर पंप", "इलेक्ट्रिक सेंट्रीफ्यूगल पंप", "सबमर्सिबल कृषि पंप",
            "सोलर कृषि वाटर पंप", "बोरवेल व ट्यूबवेल एक्सेसरीज़",
            "ड्रिप सिंचाई प्रणाली", "ड्रिप फिल्टर व एमिटर किट", "स्प्रिंकलर सिंचाई प्रणाली",
            "रेन गन सिंचाई प्रणाली", "डिलीवरी होज़ पाइप व वॉल्व", "ऑटोमेटेड टाइमर व फ्लो कंट्रोलर"
        )
    ),
    CROP_PROTECTION_SPRAYING(
        code = "CROP_PROTECTION_SPRAYING",
        enName = "Crop Protection & Spraying",
        bnName = "ফসল সুরক্ষা ও স্প্রে যন্ত্র",
        hiName = "फसल सुरक्षा एवं छिड़काव यंत्र",
        emoji = "🛡️",
        subcategoriesEn = listOf(
            "Hand Compression Sprayer", "Knapsack Manual Sprayer", "Battery Sprayer",
            "Engine Power Sprayer", "Tractor-Mounted Boom Sprayer", "Air Blast Orchard & Tea Sprayer",
            "High-Pressure Washing & Spraying Pump", "Tea Garden Sprayer", "Weed Control Brush Cutter & Weeder"
        ),
        subcategoriesBn = listOf(
            "হ্যান্ড কম্প্রেশন স্প্রেয়ার", "ন্যাপস্যাক ম্যানুয়াল স্প্রেয়ার", "ব্যাটারি স্প্রেয়ার",
            "ইঞ্জিন পাওয়ার স্প্রেয়ার", "ট্রাক্টর বুম স্প্রেয়ার", "অর্চার্ড ও চা বাগান এয়ার ব্লাস্ট স্প্রেয়ার",
            "হাই-প্রেশার ওয়াশিং ও স্প্রে পাম্প", "চা বাগান স্প্রেয়ার", "আগাছা নাশক ব্রাশ কাটার ও উইডার"
        ),
        subcategoriesHi = listOf(
            "हैंड कम्प्रेशन स्प्रेयर", "नैपसैक मैनुअल स्प्रेयर", "बैटरी स्प्रेयर",
            "इंजन पावर स्प्रेयर", "ट्रैक्टर-माउंटेड बूम स्प्रेयर", "एयर ब्लास्ट बाग व चाय स्प्रेयर",
            "हाई-प्रेशर वॉशिंग व स्प्रे पंप", "चाय बागान स्प्रेयर", "खरपतवार ब्रश कटर व वीडर"
        )
    ),
    HARVESTING_MACHINERY(
        code = "HARVESTING_MACHINERY",
        enName = "Harvesting Machinery",
        bnName = "ফসল কাটা ও সংগ্রহ যন্ত্রপাতি",
        hiName = "कटाई एवं हार्वेस्टिंग मशीनरी",
        emoji = "🌾",
        subcategoriesEn = listOf(
            "Combine Harvester", "Track Harvester", "Power Reaper / Self-Propelled Reaper",
            "Reaper Binder", "Brush Cutter Grass & Crop Harvester",
            "Tea Leaf Plucker / Harvesting Machine", "Maize / Rice / Wheat Harvester",
            "Potato Digger & Groundnut Digger"
        ),
        subcategoriesBn = listOf(
            "কম্বাইন হারভেস্টার", "ট্র্যাক হারভেস্টার", "পাওয়ার রিপার / স্ব-চালিত রিপার",
            "রিপার বাইন্ডার", "ব্রাশ কাটার ও ঘাস কাটার মেশিন",
            "চা পাতা তোলার মেশিন (Tea Leaf Plucker)", "ভুট্টা / ধান / গম কাটার যন্ত্র",
            "আলু ও চিনাবাদাম তোলার যন্ত্র"
        ),
        subcategoriesHi = listOf(
            "कंबाइन हार्वेस्टर", "ट्रैक हार्वेस्टर", "पावर रीपर / सेल्फ-प्रोपेल्ड रीपर",
            "रीपर बाइंडर", "ब्रश कटर घास व फसल कटाई मशीन",
            "चाय पत्ती तोड़ने की मशीन (Tea Leaf Plucker)", "मक्का / धान / गेहूं हार्वेस्टर",
            "आलू व मूंगफली खुदाई मशीन"
        )
    ),
    CROP_PROCESSING_STORAGE(
        code = "CROP_PROCESSING_STORAGE",
        enName = "Crop Processing & Storage",
        bnName = "ফসল মাড়াই, প্রক্রিয়াজাতকরণ ও সংরক্ষণ",
        hiName = "फसल प्रसंस्करण एवं भंडारण मशीनरी",
        emoji = "🏭",
        subcategoriesEn = listOf(
            "Paddy / Wheat / Multi-Crop Thresher", "Maize Sheller", "Chaff Cutter / Fodder Chopper",
            "Grain Cleaner & Grader", "Winnower Fan", "Rice Mill / Mini Rice Huller",
            "Flour Mill / Pulverizer", "Oil Expeller / Cold Press", "Solar Crop Dryer",
            "Silo & Moisture-Safe Grain Storage"
        ),
        subcategoriesBn = listOf(
            "ধান / গম / মাল্টি-ক্রপ থ্রেশার", "ভুট্টা ছাড়ানোর মেশিন (Maize Sheller)", "খড় ও পশুখাদ্য কাটার মেশিন (Chaff Cutter)",
            "শস্য পরিষ্কার ও গ্রেডিং মেশিন", "উইনোয়ার ফ্যান (ধান ঝাড়ার পাখা)", "মিনি রাইস মিল / হালার",
            "আটা মিল / পালভারাইজার", "অয়েল এক্সপেলার / তেল ঘানি", "সোলার ফসল ড্রায়ার",
            "সাইলো ও আর্দ্রতা-নিরাপদ শস্য সংরক্ষণ"
        ),
        subcategoriesHi = listOf(
            "धान / गेहूं / मल्टी-क्रॉप थ्रेशर", "मक्का शेलर", "चाफ कटर / चारा काटने की मशीन",
            "ग्रेन क्लीनर व ग्रेडर", "विनोवर पंखा (अनाज ओसाई)", "मिनी राइस मिल / हलर",
            "आटा चक्की / पल्वराइज़र", "ऑयल एक्सपेलर / कोल्ड प्रेस", "सोलर क्रॉप ड्रायर",
            "साइलो व नमी-रोधी अनाज भंडारण"
        )
    ),
    GARDENING_TEA_HORTICULTURE(
        code = "GARDENING_TEA_HORTICULTURE",
        enName = "Gardening, Tea Garden & Horticulture",
        bnName = "বাগান, চা বাগান ও উদ্যানপালন যন্ত্র",
        hiName = "बागवानी, चाय बागान एवं उद्यानिकी उपकरण",
        emoji = "🌿",
        subcategoriesEn = listOf(
            "Lawn Mower", "Hedge Trimmer", "Tea Pruning Machine", "Chainsaw",
            "Wood Chipper & Mulcher", "Greenhouse Fogging System",
            "Polyhouse Fan & Cooling Pad Unit", "Shade Net & Irrigation Kit",
            "Nursery Soil Mixer & Pot Filler"
        ),
        subcategoriesBn = listOf(
            "লন মাওয়ার (ঘাস কাটার মেশিন)", "হেজ ট্রিমার", "চা গাছ ছাঁটাই মেশিন (Tea Pruner)", "চেইনস (Chainsaw)",
            "উড চিপার ও মালচার", "গ্রিনহাউস ফগিং সিস্টেম",
            "পলিহাউস ফ্যান ও কুলিং প্যাড ইউনিট", "শেড নেট ও সেচ কিট",
            "নার্সারি মাটি মিক্সার ও টব ভর্তি যন্ত্র"
        ),
        subcategoriesHi = listOf(
            "लॉन मोवर", "हेज ट्रिमर", "चाय छंटाई मशीन (Tea Pruner)", "चेनसॉ (Chainsaw)",
            "वुड चिपर व मल्चर", "ग्रीनहाउस फॉगिंग सिस्टम",
            "पॉलीहाउस फैन व कूलिंग पैड यूनिट", "शेड नेट व सिंचाई किट",
            "नर्सरी मिट्टी मिक्सर व गमला भरने का यंत्र"
        )
    ),
    SMART_PRECISION_AG(
        code = "SMART_PRECISION_AG",
        enName = "Smart Farming & Precision Agriculture",
        bnName = "স্মার্ট কৃষি ও প্রিসিশন টেকনোলজি",
        hiName = "स्मार्ट खेती एवं प्रिसिजन कृषि तकनीक",
        emoji = "🛰️",
        subcategoriesEn = listOf(
            "Agricultural Spraying Drone (10L-30L)", "Crop Health Mapping & Multispectral Sensor",
            "Digital Soil Testing Kit & NPK Sensor", "Soil Moisture Sensor & pH Meter",
            "Automatic Weather Station", "GPS Field Boundary & Tractor Guidance",
            "Solar Trap & Automated Insect Trap"
        ),
        subcategoriesBn = listOf(
            "কৃষি স্প্রে ড্রোন (১০-৩০ লিটার)", "ফসল স্বাস্থ্য ও মাল্টিস্পেকট্রাল সেন্সর",
            "ডিজিটাল মাটি পরীক্ষা কিট ও NPK সেন্সর", "মাটির আর্দ্রতা ও pH মিটার",
            "স্বয়ংক্রিয় আবহাওয়া স্টেশন", "GPS জমি পরিমাপ ও ট্রাক্টর গাইডেন্স",
            "সোলার আলোক ফাঁদ ও পোকা ধরার যন্ত্র"
        ),
        subcategoriesHi = listOf(
            "कृषि छिड़काव ड्रोन (10L-30L)", "फसल स्वास्थ्य मैपिंग व मल्टीस्पेक्ट्रल सेंसर",
            "डिजिटल मृदा परीक्षण किट व NPK सेंसर", "मृदा नमी सेंसर व pH मीटर",
            "स्वचालित मौसम स्टेशन", "GPS खेत सीमा व ट्रैक्टर गाइडेंस",
            "सोलर लाइट ट्रैप व स्वचालित कीट जाल"
        )
    ),
    SPARE_PARTS_WORKSHOP(
        code = "SPARE_PARTS_WORKSHOP",
        enName = "Spare Parts, Accessories & Workshop Tools",
        bnName = "স্পেয়ার পার্টস, যন্ত্রাংশ ও ওয়ার্কশপ টুলস",
        hiName = "स्पेयर पार्ट्स, एक्सेसरीज़ व वर्कशॉप टूल",
        emoji = "🔧",
        subcategoriesEn = listOf(
            "Tractor Tyres & Tubes", "Hydraulic Cylinders & Hoses", "PTO Shaft & Hitch Pins",
            "Harvester & Rotavator Blades", "Pump Impellers & Seal Kits", "Battery & Alternator Components",
            "Sprayer Nozzles & Lances", "Grease Gun, Spanner & Farm Workshop Kit"
        ),
        subcategoriesBn = listOf(
            "ট্রাক্টরের টায়ার ও টিউব", "হাইড্রোলিক সিলিন্ডার ও হোস পাইপ", "পিটিও শ্যাফট ও হিচ পিন",
            "হারভেস্টার ও রোটাভেটর ব্লেড", "পাম্প ইম্পেলার ও সিল কিট", "ব্যাটারি ও অল্টারনেটর যন্ত্রাংশ",
            "স্প্রেয়ার নজল ও ল্যান্স", "গ্রিজ গান, রেঞ্জ ও ওয়ার্কশপ টুল কিট"
        ),
        subcategoriesHi = listOf(
            "ट्रैक्टर टायर व ट्यूब", "हाइड्रोलिक सिलेंडर व होज़", "पीटीओ शाफ्ट व हिच पिन",
            "हार्वेस्टर व रोटावेटर ब्लेड", "पंप इम्पेलर व सील किट", "बैटरी व अल्टरनेटर पुर्जे",
            "स्प्रेयर नोज़ल व लांस", "ग्रीस गन, स्पैनर व फार्म वर्कशॉप किट"
        )
    );

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }

    fun getSubcategories(lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.BENGALI -> subcategoriesBn
        AppLanguage.HINDI -> subcategoriesHi
        AppLanguage.ENGLISH -> subcategoriesEn
    }

    companion object {
        fun fromCode(code: String?): MachineryCategory =
            values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: TRACTORS_LAND_PREP
    }
}

enum class FarmServiceType(
    val code: String,
    val enName: String,
    val bnName: String,
    val hiName: String,
    val emoji: String
) {
    TRACTOR_PLOUGHING("TRACTOR_PLOUGHING", "Tractor ploughing service", "ট্রাক্টর দিয়ে জমি চাষ", "ट्रैक्टर से खेत जुताई सेवा", "🚜"),
    ROTAVATOR_LAND_PREP("ROTAVATOR_LAND_PREP", "Rotavator / land preparation", "রোটাভেটর ও জমি তৈরি সেবা", "रोटावेटर व भूमि तैयारी सेवा", "🌾"),
    MINI_EXCAVATOR_TRENCH("MINI_EXCAVATOR_TRENCH", "Mini excavator / trench digging", "মিনি এক্সকাভেটর ও নালা খনন", "मिनी एक्सकावेटर व खाई खुदाई", "⛏️"),
    POND_CANAL_DIGGING("POND_CANAL_DIGGING", "Pond / canal digging service", "পুকুর ও খাল খনন সেবা", "तालाब व नहर खुदाई सेवा", "🌊"),
    COMBINE_HARVESTING("COMBINE_HARVESTING", "Combine harvester service", "কম্বাইন হারভেস্টার দিয়ে ধান/গম কাটা", "कंबाइन हार्वेस्टर कटाई सेवा", "🌾"),
    POWER_REAPER_SERVICE("POWER_REAPER_SERVICE", "Power reaper service", "পাওয়ার রিপার ফসল কাটা সেবা", "पावर रीपर कटाई सेवा", "✂️"),
    THRESHING_SERVICE("THRESHING_SERVICE", "Threshing service", "ফসল মাড়াই (থ্রেশিং) সেবা", "थ्रेशिंग व मड़ाई सेवा", "🏡"),
    DRONE_SPRAYING("DRONE_SPRAYING", "Agricultural drone spraying", "ড্রোন দিয়ে স্প্রে সেবা", "कृषि ड्रोन छिड़काव सेवा", "🛸"),
    PUMP_BOREWELL_INSTALL("PUMP_BOREWELL_INSTALL", "Pump & borewell installation", "পাম্প ও বোরওয়েল বসানো সেবা", "पंप व बोरवेल इंस्टॉलेशन सेवा", "💧"),
    DRIP_SPRINKLER_INSTALL("DRIP_SPRINKLER_INSTALL", "Drip & sprinkler installation", "ড্রিপ ও স্প্রিংকলার সেচ স্থাপন", "ड्रिप व स्प्रिंकलर सिंचाई सेटअप", "💦"),
    SOLAR_PUMP_INSTALL("SOLAR_PUMP_INSTALL", "Solar water pump installation", "সোলার ওয়াটার পাম্প স্থাপন", "सोलर वाटर पंप इंस्टॉलेशन", "☀️"),
    POLYHOUSE_SHADENET_SETUP("POLYHOUSE_SHADENET_SETUP", "Polyhouse & shade-net setup", "পলিহাউস ও শেড-নেট নির্মাণ", "पॉलीहाउस व शेड-नेट सेटअप", "🌿"),
    MACHINERY_REPAIR("MACHINERY_REPAIR", "Farm machinery repair & mechanic", "কৃষি যন্ত্র মেরামত ও মেকানিক", "कृषि यंत्र मरम्मत व मैकेनिक", "🔧"),
    SOIL_LEVELING_EARTHMOVING("SOIL_LEVELING_EARTHMOVING", "Soil leveling / earthmoving", "জমি সমতলকরণ ও আর্থমুভিং", "भूमि समतलीकरण व अर्थमूविंग", "🚜");

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }
}

enum class PartnerBusinessRole(
    val code: String,
    val enName: String,
    val bnName: String,
    val hiName: String
) {
    NURSERY_OWNER("NURSERY_OWNER", "Nursery Owner & Plant Seller", "নার্সারি মালিক ও চারা বিক্রেতা", "नर्सरी मालिक एवं पौधा विक्रेता"),
    TRACTOR_MACHINERY_DEALER("TRACTOR_MACHINERY_DEALER", "Tractor & Farm Machinery Dealer", "ট্রাক্টর ও কৃষি যন্ত্র ডিলার", "ट्रैक्टर व कृषि मशीनरी डीलर"),
    FARM_EQUIPMENT_MANUFACTURER("FARM_EQUIPMENT_MANUFACTURER", "Farm Equipment Manufacturer", "কৃষি যন্ত্রপাতি প্রস্তুতকারক", "कृषि उपकरण निर्माता"),
    SPARE_PARTS_SUPPLIER("SPARE_PARTS_SUPPLIER", "Spare Parts & Hardware Supplier", "স্পেয়ার পার্টস ও হার্ডওয়্যার সরবরাহকারী", "स्पेयर पार्ट्स व हार्डवेयर सप्लायर"),
    IRRIGATION_PUMP_PROVIDER("IRRIGATION_PUMP_PROVIDER", "Irrigation & Pump System Provider", "সেচ ও পাম্প সিস্টেম প্রদানকারী", "सिंचाई व पंप सिस्टम प्रदाता"),
    AGRI_DRONE_SERVICE_PROVIDER("AGRI_DRONE_SERVICE_PROVIDER", "Agricultural Drone Service Provider", "কৃষি ড্রোন পরিষেবা প্রদানকারী", "कृषि ड्रोन सेवा प्रदाता"),
    EARTHMOVING_CONTRACTOR("EARTHMOVING_CONTRACTOR", "Earthmoving & Trench Digging Contractor", "মাটি খনন ও আর্থমুভিং ঠিকাদার", "अर्थमूविंग व खाई खुदाई ठेकेदार"),
    HARVESTER_SERVICE_PROVIDER("HARVESTER_SERVICE_PROVIDER", "Harvester & Thresher Service Provider", "হারভেস্টার ও থ্রেশার পরিষেবা", "हार्वेस्टर व थ्रेशर सेवा प्रदाता"),
    MACHINERY_RENTAL_OWNER("MACHINERY_RENTAL_OWNER", "Tractor & Machinery Rental Owner", "ট্রাক্টর ও যন্ত্রপাতি ভাড়া প্রদানকারী", "ट्रैक्टर व मशीनरी किराया मालिक"),
    MACHINERY_REPAIR_CENTER("MACHINERY_REPAIR_CENTER", "Farm Machinery Mechanic & Repair Center", "কৃষি যন্ত্র মেরামত ও মেকানিক কেন্দ্র", "कृषि यंत्र मरम्मत एवं मैकेनिक केंद्र"),
    POWER_TOOLS_SELLER("POWER_TOOLS_SELLER", "Power Tools & Garden Equipment Seller", "পাওয়ার টুলস ও বাগান যন্ত্র বিক্রেতা", "पावर टूल्स व बागवानी उपकरण विक्रेता"),
    USED_MACHINERY_SELLER("USED_MACHINERY_SELLER", "Used Farm Machinery Seller", "পুরনো কৃষি যন্ত্রপাতি বিক্রেতা", "पुराने कृषि यंत्र विक्रेता");

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }

    companion object {
        fun fromCode(code: String?): PartnerBusinessRole =
            values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: NURSERY_OWNER
    }
}

enum class MachineryCondition(val code: String, val enName: String, val bnName: String, val hiName: String) {
    NEW("NEW", "New", "নতুন", "नया"),
    USED("USED", "Used", "পুরনো / ব্যবহৃত", "पुराना / उपयोग किया हुआ"),
    REFURBISHED("REFURBISHED", "Refurbished", "সংস্কারকৃত (Refurbished)", "रीफर्बिश्ड");

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }
}

enum class RentalUnit(val code: String, val enName: String, val bnName: String, val hiName: String) {
    HOURLY("HOURLY", "Per Hour", "ঘণ্টা প্রতি", "प्रति घंटा"),
    DAILY("DAILY", "Per Day", "দিন প্রতি", "प्रति दिन"),
    WEEKLY("WEEKLY", "Per Week", "সপ্তাহ প্রতি", "प्रति सप्ताह"),
    PER_ACRE("PER_ACRE", "Per Acre / Bigha", "একর / বিঘা প্রতি", "प्रति एकड़ / बीघा"),
    CUSTOM("CUSTOM", "Custom Agreement", "আলোচনা সাপেক্ষ", "आवश्यकतानुसार");

    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }
}

data class MachineryLocality(
    val code: String,
    val enName: String,
    val bnName: String,
    val hiName: String,
    val districtTag: String,
    val centerLat: Double,
    val centerLng: Double
) {
    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnName
        AppLanguage.HINDI -> hiName
        AppLanguage.ENGLISH -> enName
    }
}

@IgnoreExtraProperties
data class MachineryProduct(
    val id: String = "",
    val sellerUserId: String = "",
    val dealerId: String = "",
    val sellerBusinessName: String = "",
    val sellerLocation: String = "",
    val sellerLocalityCode: String = "",
    val sellerPhone: String? = null,
    val sellerWhatsapp: String? = null,
    val nameEn: String = "",
    val nameBn: String = "",
    val nameHi: String = "",
    val categoryCode: String = MachineryCategory.TRACTORS_LAND_PREP.code,
    val subcategory: String = "",
    val brand: String = "",
    val modelNumber: String = "",
    val horsepowerOrCapacity: String = "",
    val engineOrMotorSpec: String = "",
    val fuelOrPowerType: String = "",
    val workingWidthOrCoverage: String = "",
    val weightKg: String = "",
    val dimensionsText: String = "",
    val suitableCrops: String = "",
    val suitableSoilOrTerrain: String = "",
    val landSizeSuitability: String = "",
    val condition: String = MachineryCondition.NEW.code,
    val forSale: Boolean = true,
    val forRent: Boolean = false,
    val salePriceInr: Double? = null, // null -> "Price on request"
    val rentalRateHourlyInr: Double? = null,
    val rentalRateDailyInr: Double? = null,
    val rentalRateWeeklyInr: Double? = null,
    val rentalRatePerAcreInr: Double? = null,
    val stockStatus: String = "CONFIRM_WITH_SELLER",
    val warrantyInfo: String = "As per manufacturer / verified seller terms",
    val sparePartsInfo: String = "",
    val installationMaintenanceNotes: String = "",
    val safetyUsageGuidelines: String = "",
    val demoVideoUrl: String = "",
    val photosCsv: String = "",
    val listingOrigin: String = "REFERENCE_SPEC", // "REFERENCE_SPEC" or "SELLER_LISTING"
    val approvalStatus: String = "APPROVED", // "PENDING_APPROVAL", "APPROVED", "REJECTED"
    val verificationBadgeText: String = "Reference Catalog Spec",
    val requiresDgcaCompliance: Boolean = false,
    val requiresEarthmovingSafety: Boolean = false,
    val lastUpdatedDate: String = "2026-11-19",
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val purposeEn: String = "",
    val purposeBn: String = "",
    val purposeHi: String = "",
    val suitableCropsBn: String = "",
    val suitableCropsHi: String = "",
    val operatingInstructionsEn: String = "",
    val operatingInstructionsBn: String = "",
    val operatingInstructionsHi: String = "",
    val safetyGuidelinesBn: String = "",
    val safetyGuidelinesHi: String = "",
    val brandVerified: Boolean = true,
    val modelVerified: Boolean = true,
    val pendingAdminReview: Boolean = false,
    val adminReviewNote: String = "",
    val hireServiceTypeCode: String = "",
    val firebaseStoragePathsCsv: String = "",
    val aliasesCsv: String = "",
    val applicableCategoriesCsv: String = "",
    val sourceUrl: String = "",
    val sourceType: String = "ENGINEERING_REFERENCE",
    val verificationDate: String = "2026-11-21",
    val infoVerificationStatus: String = "VERIFIED_CLASS",
    val priceVerificationStatus: String = "CONFIRM_WITH_SELLER",
    val imageUsageStatus: String = "APPROVED_IN_APP",
    val beginnerGuideJson: String = "",
    val compatibleWithIdsCsv: String = "",
    val subsidySchemeCodesCsv: String = "",
    val operatorTrainingRequired: Boolean = false
) {
    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> nameBn.ifBlank { nameEn }
        AppLanguage.HINDI -> nameHi.ifBlank { nameEn }
        AppLanguage.ENGLISH -> nameEn.ifBlank { nameBn }
    }

    fun getDisplayPurpose(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> purposeBn.ifBlank { purposeEn }
        AppLanguage.HINDI -> purposeHi.ifBlank { purposeEn }
        AppLanguage.ENGLISH -> purposeEn.ifBlank { purposeBn }
    }

    fun getDisplaySuitableCrops(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> suitableCropsBn.ifBlank { suitableCrops }
        AppLanguage.HINDI -> suitableCropsHi.ifBlank { suitableCrops }
        AppLanguage.ENGLISH -> suitableCrops
    }

    fun getDisplayOperatingInstructions(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> operatingInstructionsBn.ifBlank { operatingInstructionsEn.ifBlank { installationMaintenanceNotes } }
        AppLanguage.HINDI -> operatingInstructionsHi.ifBlank { operatingInstructionsEn.ifBlank { installationMaintenanceNotes } }
        AppLanguage.ENGLISH -> operatingInstructionsEn.ifBlank { installationMaintenanceNotes }
    }

    fun getDisplaySafetyGuidelines(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> safetyGuidelinesBn.ifBlank { safetyUsageGuidelines }
        AppLanguage.HINDI -> safetyGuidelinesHi.ifBlank { safetyUsageGuidelines }
        AppLanguage.ENGLISH -> safetyUsageGuidelines
    }

    fun getDisplayBrandModel(lang: AppLanguage): String {
        val brandText = brand.takeIf { brandVerified && it.isNotBlank() }
        val modelText = modelNumber.takeIf { modelVerified && it.isNotBlank() }
        return when {
            brandText != null && modelText != null -> "$brandText $modelText"
            brandText != null -> when (lang) {
                AppLanguage.BENGALI -> "$brandText (মডেল যাচাই বাকি)"
                AppLanguage.HINDI -> "$brandText (मॉडल सत्यापन बाकी)"
                AppLanguage.ENGLISH -> "$brandText (Model pending verification)"
            }
            else -> when (lang) {
                AppLanguage.BENGALI -> "ব্র্যান্ড ও মডেল অ্যাডমিন যাচাই বাকি"
                AppLanguage.HINDI -> "ब्रांड व मॉडल एडमिन समीक्षा बाकी"
                AppLanguage.ENGLISH -> "Brand & model pending admin review"
            }
        }
    }

    fun getPhotoTokens(): List<String> = buildList {
        photosCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { add(it) }
        firebaseStoragePathsCsv.split(",").map { it.trim() }
            .filter { it.startsWith("https://") || it.startsWith("http://") }
            .forEach { if (!contains(it)) add(it) }
    }.distinct()

    fun getHireServiceType(): FarmServiceType? {
        if (hireServiceTypeCode.isNotBlank()) {
            return FarmServiceType.values().firstOrNull { it.code.equals(hireServiceTypeCode, ignoreCase = true) }
        }
        return when (getCategoryEnum()) {
            MachineryCategory.TRACTORS_LAND_PREP ->
                if (subcategory.contains("Rotavator", ignoreCase = true) || subcategory.contains("Tiller", ignoreCase = true)) {
                    FarmServiceType.ROTAVATOR_LAND_PREP
                } else {
                    FarmServiceType.TRACTOR_PLOUGHING
                }
            MachineryCategory.SOIL_EARTH_MOVING -> FarmServiceType.MINI_EXCAVATOR_TRENCH
            MachineryCategory.IRRIGATION_WATER ->
                if (subcategory.contains("Drip", ignoreCase = true) || subcategory.contains("Filter", ignoreCase = true)) {
                    FarmServiceType.DRIP_SPRINKLER_INSTALL
                } else if (subcategory.contains("Solar", ignoreCase = true)) {
                    FarmServiceType.SOLAR_PUMP_INSTALL
                } else {
                    FarmServiceType.PUMP_BOREWELL_INSTALL
                }
            MachineryCategory.HARVESTING_MACHINERY ->
                if (subcategory.contains("Combine", ignoreCase = true)) FarmServiceType.COMBINE_HARVESTING else FarmServiceType.POWER_REAPER_SERVICE
            MachineryCategory.CROP_PROCESSING_STORAGE -> FarmServiceType.THRESHING_SERVICE
            MachineryCategory.SMART_PRECISION_AG ->
                if (requiresDgcaCompliance) FarmServiceType.DRONE_SPRAYING else FarmServiceType.MACHINERY_REPAIR
            MachineryCategory.SPARE_PARTS_WORKSHOP -> FarmServiceType.MACHINERY_REPAIR
            else -> FarmServiceType.MACHINERY_REPAIR
        }
    }

    fun getCategoryEnum(): MachineryCategory = MachineryCategory.fromCode(categoryCode)

    fun hasConfirmedSellerContact(): Boolean =
        listingOrigin == "SELLER_LISTING" && approvalStatus == "APPROVED" && !sellerPhone.isNullOrBlank()

    fun matchesCategory(catCode: String?): Boolean {
        if (catCode.isNullOrBlank()) return true
        if (categoryCode.equals(catCode, ignoreCase = true)) return true
        return applicableCategoriesCsv.split(",").any { it.trim().equals(catCode, ignoreCase = true) }
    }

    fun getSubsidyCodes(): List<String> =
        subsidySchemeCodesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }.distinct()

    fun matchesSearch(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return nameEn.lowercase().contains(q) ||
            nameBn.lowercase().contains(q) ||
            nameHi.lowercase().contains(q) ||
            aliasesCsv.lowercase().contains(q) ||
            purposeEn.lowercase().contains(q) ||
            purposeBn.lowercase().contains(q) ||
            purposeHi.lowercase().contains(q) ||
            brand.lowercase().contains(q) ||
            modelNumber.lowercase().contains(q) ||
            subcategory.lowercase().contains(q) ||
            sellerLocation.lowercase().contains(q) ||
            suitableCrops.lowercase().contains(q) ||
            suitableCropsBn.lowercase().contains(q) ||
            suitableCropsHi.lowercase().contains(q) ||
            getCategoryEnum().enName.lowercase().contains(q) ||
            getCategoryEnum().bnName.lowercase().contains(q) ||
            getCategoryEnum().hiName.lowercase().contains(q)
    }
}

@IgnoreExtraProperties
data class MachineryDealerEntry(
    val dealerId: String = "",
    val businessName: String = "",
    val businessNameBn: String = "",
    val businessNameHi: String = "",
    val partnerRoleCode: String = PartnerBusinessRole.TRACTOR_MACHINERY_DEALER.code,
    val localityCode: String = "SILIGURI",
    val district: String = "Siliguri / Darjeeling",
    val state: String = "West Bengal",
    val fullAddress: String? = null,
    val pinCode: String? = null,
    val ownerName: String? = null,
    val phone: String? = null,
    val whatsapp: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val brandsCarriedCsv: String = "",
    val supportedCategoriesCsv: String = "",
    val servicesOfferedCsv: String = "",
    val isAuthorisedDealerVerified: Boolean = false,
    val offersSale: Boolean = false,
    val offersRent: Boolean = false,
    val offersRepair: Boolean = false,
    val verificationStatus: String = NurseryVerificationStatus.PENDING_VERIFICATION,
    val ownerConsentStatus: String = "NOT_CONFIRMED",
    val isVerified: Boolean = false,
    val claimedByUserId: String? = null,
    val claimNote: String? = null,
    val sourceReference: String = "",
    val notes: String = "",
    val lastVerifiedDate: String = "2026-11-19",
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> businessNameBn.ifBlank { businessName }
        AppLanguage.HINDI -> businessNameHi.ifBlank { businessName }
        AppLanguage.ENGLISH -> businessName.ifBlank { businessNameBn }
    }

    fun matchesSearch(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return businessName.lowercase().contains(q) ||
            businessNameBn.lowercase().contains(q) ||
            businessNameHi.lowercase().contains(q) ||
            localityCode.lowercase().contains(q) ||
            district.lowercase().contains(q) ||
            (pinCode?.lowercase()?.contains(q) == true) ||
            (fullAddress?.lowercase()?.contains(q) == true) ||
            brandsCarriedCsv.lowercase().contains(q)
    }
}

@IgnoreExtraProperties
data class MachineryInquiryRecord(
    val id: String = "",
    val userId: String = "",
    val farmerName: String = "",
    val farmerPhone: String = "",
    val machineId: String = "",
    val machineName: String = "",
    val sellerUserId: String = "",
    val inquiryType: String = "QUOTATION", // QUOTATION or REPORT_ISSUE
    val message: String = "",
    val status: String = "REQUESTED_AWAITING_CONFIRMATION",
    val sellerReply: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class MachineryRentalRequest(
    val id: String = "",
    val userId: String = "",
    val farmerName: String = "",
    val farmerPhone: String = "",
    val machineId: String = "",
    val machineName: String = "",
    val sellerUserId: String = "",
    val locality: String = "",
    val rentalUnit: String = RentalUnit.DAILY.code,
    val durationUnits: Int = 1,
    val withOperator: Boolean = true,
    val deliveryRequired: Boolean = false,
    val fuelPolicyNote: String = "Discuss directly with verified rental owner",
    val securityDepositNote: String = "Confirmed directly between farmer and provider",
    val notes: String = "",
    val status: String = "REQUESTED_AWAITING_CONFIRMATION",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class MachineryServiceRequest(
    val id: String = "",
    val userId: String = "",
    val farmerName: String = "",
    val farmerPhone: String = "",
    val serviceTypeCode: String = FarmServiceType.TRACTOR_PLOUGHING.code,
    val locality: String = "",
    val landSizeText: String = "",
    val cropName: String = "",
    val preferredDate: String = "",
    val notes: String = "",
    val providerUserId: String = "",
    val status: String = "REQUESTED_AWAITING_CONFIRMATION",
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class MachineryRecommendationQuery(
    val cropName: String,
    val landSizeAcres: Double,
    val soilOrTerrain: String,
    val irrigationAvailable: Boolean,
    val budgetRangeInr: String,
    val operationNeed: String,
    val skilledOperatorAvailable: Boolean,
    val powerPreference: String // Diesel / Electric / Battery / Solar / Manual
)

@IgnoreExtraProperties
data class MachineryAdminAuditLog(
    val id: String = "",
    val adminEmail: String = "",
    val action: String = "",
    val targetId: String = "",
    val summary: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)

object BookingLifecycleStatus {
    const val DRAFT = "DRAFT"
    const val PENDING_SYNC = "PENDING_SYNC"
    const val REQUESTED_AWAITING_CONFIRMATION = "REQUESTED_AWAITING_CONFIRMATION"
    const val ACCEPTED = "ACCEPTED"
    const val CONFIRMED = "CONFIRMED"
    const val REJECTED = "REJECTED"
    const val CANCELLED = "CANCELLED"
    const val COMPLETED = "COMPLETED"
}

@IgnoreExtraProperties
data class MachineryManufacturer(
    val id: String = "",
    val nameEn: String = "",
    val nameBn: String = "",
    val nameHi: String = "",
    val countryOrOrigin: String = "India",
    val focusSummaryEn: String = "",
    val officialPortalUrl: String = "",
    val dealerLocatorUrl: String = "",
    val servicePortalUrl: String = "",
    val customerCareHelpline: String = "",
    val supportedCategoriesCsv: String = "",
    val isVerifiedOfficialLink: Boolean = true,
    val verificationStatus: String = "OFFICIAL_INFO_LINK_ONLY",
    val sourceUrl: String = "",
    val lastVerifiedDate: String = "2026-11-21"
) {
    fun getDisplayName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> nameBn.ifBlank { nameEn }
        AppLanguage.HINDI -> nameHi.ifBlank { nameEn }
        AppLanguage.ENGLISH -> nameEn
    }
}

@IgnoreExtraProperties
data class MachineryDealerOffer(
    val offerId: String = "",
    val machineId: String = "",
    val dealerId: String = "",
    val dealerName: String = "",
    val localityCode: String = "SILIGURI",
    val modelNumber: String = "",
    val condition: String = MachineryCondition.NEW.code,
    val offersSale: Boolean = true,
    val offersRent: Boolean = false,
    val mrpInr: Double? = null,
    val dealerPriceInr: Double? = null,
    val indicativePriceInr: Double? = null,
    val rentalHourlyInr: Double? = null,
    val rentalDailyInr: Double? = null,
    val rentalPerAcreInr: Double? = null,
    val operatorChargeNote: String = "Confirm operator charges directly with verified provider",
    val transportChargeNote: String = "Confirm local delivery/pickup charges with seller",
    val fuelIncludedNote: String = "Fuel & consumables as agreed with provider",
    val taxNote: String = "GST & documentation as applicable",
    val warrantyText: String = "Manufacturer / verified seller invoice terms",
    val verifiedDiscountText: String = "",
    val stockStatus: String = "Availability Not Confirmed",
    val priceSource: String = "PUBLIC_CATALOG_PENDING_CONFIRMATION",
    val verificationStatus: String = "CATALOG_SUPPORTED_PENDING_CONFIRMATION",
    val lastPriceUpdatedDate: String = "2026-11-21",
    val isOutdatedPrice: Boolean = false,
    val catalogUrl: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class MachineryReviewRecord(
    val id: String = "",
    val machineId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 5,
    val title: String = "",
    val comment: String = "",
    val verifiedFarmerFlag: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class MachineryResearchQueueItem(
    val id: String = "",
    val machineId: String = "",
    val equipmentName: String = "",
    val categoryCode: String = "",
    val missingFieldsNote: String = "",
    val candidateSourceUrl: String = "",
    val status: String = "PENDING_RESEARCH", // PENDING_RESEARCH, APPROVED, REJECTED
    val submittedBy: String = "system",
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class MachineryBeginnerGuide(
    val whatItDoes: String,
    val whoNeedsIt: String,
    val farmSizeSuitability: String,
    val tractorOrPowerRequirement: String,
    val fuelOrPowerType: String,
    val stepByStepUsage: String,
    val safetyPrecautions: String,
    val commonMistakesToAvoid: String,
    val maintenanceChecklist: String,
    val buyingVsRentingAdvice: String
)

