package com.example.data.datasource

import com.example.data.datasource.PlantKingdomCatalog.flowerPlant
import com.example.data.model.PlantKingdomEntry
import com.example.data.model.PlantKingdomGroup.FRUIT_PLANTS
import com.example.data.model.PlantKingdomGroup.MEDICINAL_HERBS_SUCCULENTS
import com.example.data.model.PlantKingdomGroup.MONEY_INDOOR_PLANTS
import com.example.data.model.PlantKingdomGroup.ORNAMENTAL_TREES_CLIMBERS
import com.example.data.model.PlantSearchFilter.FLOWERING
import com.example.data.model.PlantSearchFilter.FRUIT
import com.example.data.model.PlantSearchFilter.HILL_FLOWER
import com.example.data.model.PlantSearchFilter.INDOOR
import com.example.data.model.PlantSearchFilter.MEDICINAL
import com.example.data.model.PlantSearchFilter.MONEY_PLANT
import com.example.data.model.PlantSearchFilter.ORNAMENTAL_TREE
import com.example.data.model.PlantSearchFilter.RARE
import com.example.data.model.PlantSearchFilter.SEASONAL
import com.example.data.model.PlantSearchFilter.SHADE
import com.example.data.model.PlantSearchFilter.SUCCULENT_CACTUS
import com.example.data.model.PlantSeasonalCollection.MONSOON
import com.example.data.model.PlantSeasonalCollection.PERENNIAL
import com.example.data.model.PlantSeasonalCollection.SHADE_LOVING
import com.example.data.model.PlantSeasonalCollection.SUMMER
import com.example.data.model.PlantSeasonalCollection.WINTER

/**
 * Complete Catalog covering:
 * - Module 3: Money Plant & Indoor Plant Collection (22 new + cross-linked Anthurium)
 * - Module 4: Fruit Plants & Trees (26 North Bengal hill & plains varieties)
 * - Module 5: Ornamental Trees & Climbers (17 new + cross-linked climbers/trees)
 * - Module 6: Medicinal Plants, Herbs & Succulents (17 new + cross-linked Neem)
 */
object PlantKingdomGreeneryCatalog {

    val greeneryAndTrees: List<PlantKingdomEntry> = listOf(
        // ============================================================
        // MODULE 3: MONEY PLANT & INDOOR PLANT COLLECTION (22 + Anthurium)
        // ============================================================
        indoorPlant("golden_pothos", "Golden Pothos", "গোল্ডেন মানি প্ল্যান্ট (পোথোস)", "गोल्डन मनी प्लांट (पोथोस)", "Epipremnum aureum", true, "Heart-shaped green leaves splashed golden-yellow; thrives in water jars or hanging pots.", "হৃদয় আকৃতির সবুজ পাতায় সোনালি ছাপ; জল বা টব উভয়েই দ্রুত বাড়ে।", "दिल के आकार की हरी पत्तियों पर सुनहरे धब्बे; पानी व गमले दोनों में आसानी से उगता है।", "Moderate indirect light", "Every 4–5 days or change water weekly"),
        indoorPlant("money_plant", "Money Plant (Jade Green Pothos)", "সবুজ মানি প্ল্যান্ট", "हरा मनी प्लांट", "Epipremnum aureum 'Jade'", true, "Classic lush deep-emerald vining Money Plant resilient even in dim living rooms and office desks.", "অল্প আলোতেও চিরসবুজ ও প্রাণবন্ত থাকা ঐতিহ্যবাহী সবুজ মানি প্ল্যান্ট।", "कम रोशनी में भी हरा-भरा रहने वाला पारंपरिक मनी प्लांट।", "Low to bright indirect light", "Weekly when topsoil dries"),
        indoorPlant("marble_queen_pothos", "Marble Queen Pothos", "মার্বেল কুইন মানি প্ল্যান্ট", "मार्बल क्वीन पोथोस", "Epipremnum aureum 'Marble Queen'", true, "Creamy-white & emerald marbled trailing pothos popular across modern home interiors.", "সাদা ও সবুজ মার্বেলের মতো নকশাদার অত্যন্ত আকর্ষণীয় ইনডোর মানি প্ল্যান্ট।", "सफेद और हरे संगमरमर जैसी पत्तियों वाला सुंदर पोथोस।", "Bright indirect light (preserves white variegation)", "Every 5–6 days"),
        indoorPlant("neon_pothos", "Neon Pothos", "নিয়ন গ্রিন পোথোস", "निऑन पोथोस", "Epipremnum aureum 'Neon'", true, "Electric lime-chartreuse foliage brightening shaded corners and shelf planters.", "উজ্জ্বল লেবু-সবুজ রঙের চোখ-জুড়ানো পাতার মানি প্ল্যান্ট।", "चमकीले नींबू-हरे रंग की पत्तियों वाला आकर्षक पोथोस।", "Medium to bright indirect light", "Every 5 days"),
        indoorPlant("manjula_pothos", "Manjula Pothos", "মঞ্জুলা পোথোস", "मंजुला पोथोस", "Epipremnum aureum 'Manjula'", true, "Broad wavy-edged leaves painted with creamy white, silver, and deep green patches.", "চওড়া ঢেউখেলানো পাতায় সাদা, রূপালি ও সবুজ রঙের বাহারি মঞ্জুলা পোথোস।", "चौड़ी लहरदार पत्तियों वाला दुर्लभ व सजावटी मंजुला पोथोस।", "Bright indirect light", "When top 2 inches dry", isRare = true),
        indoorPlant("cebu_blue_pothos", "Cebu Blue Pothos", "সেবু ব্লু পোথোস", "सेबू ब्लू पोथोस", "Epipremnum pinnatum 'Cebu Blue'", true, "Silvery-blue metallic elongated leaves trailing gracefully along moss poles.", "রূপালি-নীলচে আভাযুক্ত লম্বাটে পাতার ব্যতিক্রমী পোথোস লতা।", "चाँदी-नीली चमक वाली लंबी पत्तियों का विशेष पोथोस।", "Bright filtered light", "Every 4–5 days", isRare = true),
        indoorPlant("snake_plant", "Snake Plant (Sansevieria)", "স্নেক প্ল্যান্ট (সানসেভেরিয়া)", "स्नेक प्लांट (नागफनी पौधा)", "Dracaena trifasciata", false, "Upright sword-shaped air-tolerant plant ideal for bedrooms, low light, and minimal watering.", "অল্প আলো ও খুব কম জলে মাসের পর মাস ভালো থাকা খাড়া তলোয়ার-পাতার স্নেক প্ল্যান্ট।", "कम रोशनी व कम पानी में बेडरूम और ऑफिस के लिए सर्वश्रेष्ठ नागफनी/स्नेक प्लांट।", "Low light to bright indirect", "Every 12–15 days (avoid overwatering)"),
        indoorPlant("zz_plant", "ZZ Plant", "জেড জেড প্ল্যান্ট", "ज़ेड ज़ेड प्लांट", "Zamioculcas zamiifolia", false, "Waxy dark-green glossy fronds with underground potato-like rhizomes surviving forgotten watering.", "চকচকে গাঢ় সবুজ পাতা; ঘরের কম আলোতেও অবিশ্বাস্যভাবে টিকে থাকে।", "चमकदार गहरी हरी पत्तियों वाला सबसे कम देखभाल वाला इंडोर पौधा।", "Low to bright indirect", "Every 14–18 days only"),
        indoorPlant("monstera", "Monstera Deliciosa (Swiss Cheese)", "মনস্টেরা ডেলিসিওসা", "मॉन्स्टेरा डेलिसिओसा", "Monstera deliciosa", false, "Dramatic split-leaf tropical indoor icon growing bold fenestrated foliage on moss sticks.", "কাটা-নকশাদার বিশাল পাতার আধুনিক ইনডোর ড্রয়িং-রুম গাছ মনস্টেরা।", "कटे-फटे विशाल पत्तों वाला लोकप्रिय ट्रॉपिकल इंडोर पौधा मॉन्स्टेरा।", "Bright indirect light", "Weekly when top 2 inches feel dry"),
        indoorPlant("philodendron", "Heartleaf Philodendron", "ফিলোডেনড্রন (হার্টলিফ)", "फिलोडेंड्रॉन", "Philodendron hederaceum", false, "Velvety heart-shaped trailing foliage thriving in humid rooms and shaded hanging baskets.", "মসৃণ হৃদপিণ্ড-আকৃতির ঘন সবুজ পাতার ছায়াপ্রিয় ফিলোডেনড্রন।", "मखमली दिल के आकार वाली छाये में तेज़ी से बढ़ने वाली लता।", "Medium to low indirect light", "Every 5–6 days"),
        indoorPlant("syngonium", "Syngonium (Arrowhead Plant)", "সিঙ্গোনিয়াম (অ্যারোহেড)", "सिंगोनियम", "Syngonium podophyllum", false, "Arrowhead leaves in green, pink, or snowy variegated tones easy for tabletop jars and baskets.", "তীরের ফলার মতো সবুজ ও গোলাপি শেডের সহজলভ্য ইনডোর গাছ।", "तीर के आकार वाली हरी, गुलाबी व सफेद धारीदार पत्तियों का आसान पौधा।", "Indirect light", "Every 4–5 days"),
        indoorPlant("aglaonema", "Aglaonema (Chinese Evergreen)", "অ্যাগ্লাওনেমা (চায়না এভারগ্রিন)", "एग्लोनीमा", "Aglaonema commutatum", false, "Lush patterned foliage in red lipstick, silver, or emerald tones built for shaded living spaces.", "লাল-সবুজ ও রূপালি নকশাদার দীর্ঘজীবী ইনডোর সৌন্দর্য বৃদ্ধিকারী গাছ।", "लाल व चाँदी जैसी रंगीन पत्तियों वाला छाया-प्रिय इंडोर पौधा।", "Low to bright indirect light", "Every 6–7 days"),
        indoorPlant("peace_lily", "Peace Lily", "পিস লিলি", "पीस लिली", "Spathiphyllum wallisii", false, "Elegant white hooded spathes rising over lush dark leaves in shaded indoor rooms.", "সাদা শান্তি-ফুল ও সবুজ পাতার অত্যন্ত জনপ্রিয় ইনডোর গাছ।", "घरों के भीतर छाया में भी सफेद फूल देने वाला लोकप्रिय पीस लिली।", "Medium-low indirect light", "Water when leaves droop slightly"),
        indoorPlant("spider_plant", "Spider Plant", "স্পাইডার প্ল্যান্ট", "स्पाइडर प्लांट", "Chlorophytum comosum", false, "Arching green-and-white striped ribbons sending cascading spiderette baby plants.", "সাদা-সবুজ ডোরাকাটা ঝুলন্ত পাতা ও ছোট চারার ঝালরযুক্ত স্পাইডার প্ল্যান্ট।", "हरी-सफेद धारीदार लटकती पत्तियों व छोटे बेबी प्लांट्स वाला स्पाइडर प्लांट।", "Bright indirect light", "Every 4–5 days"),
        indoorPlant("rubber_plant", "Rubber Plant (Burgundy & Green)", "ইন্ডিয়ান রাবার প্ল্যান্ট", "रबर प्लांट", "Ficus elastica", false, "Bold glossy leathery oval leaves in deep burgundy-black or variegated cream-pink.", "মোটা ও চকচকে পাতার আভিজাত্যপূর্ণ ইনডোর গাছ রাবার প্ল্যান্ট।", "मोटी चमकदार बरगंडी व हरी पत्तियों वाला आधुनिक इंडोर वृक्ष।", "Bright filtered window light", "Every 7–10 days"),
        indoorPlant("dracaena", "Dracaena (Song of India / Marginata)", "ড্রাসেনা (সং অব ইন্ডিয়া)", "ड्रेसिना", "Dracaena reflexa / marginata", false, "Sculptural cane plant with ribbon-like variegated leaves suited for sunlit indoor corners.", "লম্বাটে ফিতার মতো হলুদ-সবুজ বা লাল ধারযুক্ত দৃষ্টিনন্দন ড্রাসেনা।", "धारीदार पत्तियों वाला लंबा व सजावटी इंडोर पौधा ड्रेसिना।", "Bright indirect light", "Every 7 days"),
        indoorPlant("areca_palm", "Areca Palm", "এরিকা পাম", "एरेका पाम", "Dypsis lutescens", false, "Feathery golden-stemmed clustering palm adding breezy tropical freshness to living rooms and lawns.", "পালকের মতো সুন্দর চিকন পাতার সবচেয়ে জনপ্রিয় ইনডোর ও গার্ডেন পাম।", "पंखदार हरी पत्तियों वाला सबसे लोकप्रिय इंडोर व लॉन पाम।", "Bright filtered indoor or morning sun", "Every 3–4 days (use filtered water if tips brown)", extraGroups = listOf(ORNAMENTAL_TREES_CLIMBERS), extraFilters = listOf(ORNAMENTAL_TREE)),
        indoorPlant("parlor_palm", "Parlor Palm", "পার্লার পাম", "पार्लर पाम", "Chamaedorea elegans", false, "Compact slow-growing miniature palm adapted strictly to indoor shade without outgrowing tables.", "টেবিল ও বসার ঘরের স্বল্প আলোতে ধীরে বাড়া চমৎকার ছোট পাম গাছ।", "कम रोशनी में मेज़ व कमरे के कोनों के लिए आदर्श छोटा पाम।", "Low to medium indirect shade", "Every 5–7 days"),
        indoorPlant("boston_fern", "Boston Fern", "বস্টন ফার্ন", "बोस्टन फर्न", "Nephrolepis exaltata", false, "Lush cascading feathery fronds thriving in misty Terai & hill verandas and shady bathrooms.", "ঝর্ণার মতো ঝুলন্ত স্নিগ্ধ সবুজ পাতার আর্দ্রতাপ্রিয় বস্টন ফার্ন।", "झरने जैसी लटकती पंखदार पत्तियों वाला नमी-प्रिय बोस्टन फर्न।", "Filtered bright shade", "Mist frequently & keep soil lightly damp"),
        indoorPlant("ficus", "Ficus Benjamina (Weeping Fig)", "ফাইকাস (বেঞ্জামিনা / বনসাই)", "फाइकस बेंजामिना", "Ficus benjamina", false, "Graceful drooping-branch tree popular as indoor patio centrepiece and bonsai.", "নমনীয় শাখা ও ঘন ছোট পাতার দারুণ শোভাময় ফাইকাস ও বনসাই গাছ।", "लटकती टहनियों व घनी छोटी पत्तियों वाला बोनसाई-उपयोगी फाइकस।", "Bright indirect indoor or dappled outdoor sun", "Every 5–6 days"),
        indoorPlant("croton", "Croton (Patabahar)", "পাতাবাহার (ক্রোটন)", "क्रोटन (रंग-बिरंगा)", "Codiaeum variegatum", false, "Kaleidoscopic evergreen shrub flashing gold, crimson, copper, and green patterned foliage.", "লাল, হলুদ, কমলা ও সবুজ বর্ণচ্ছটার রঙিন পাতাবাহার গাছ।", "लाल, पीले व नारंगी रंगों की चमकदार पत्तियों वाला क्रोटन।", "Bright morning sun for vibrant colours", "Every 3–4 days"),
        indoorPlant("calathea", "Calathea (Prayer Plant / Peacock)", "ক্যালাথিয়া (প্রেয়ার প্ল্যান্ট)", "कैलेथिया (पीकॉक प्लांट)", "Goeppertia makoyana", false, "Hand-painted purple-underside patterned leaves that fold upright at dusk in rhythmic movement.", "ময়ূরের পালকের মতো নকশাদার পাতা যা সন্ধ্যায় ওপরের দিকে গুটিয়ে যায়।", "मोरपंख जैसी नक्काशीदार पत्तियों वाला खूबसूरत इंडोर पौधा।", "Medium shade (no direct sun)", "Keep moist with soft/rain water", isRare = true),

        // ============================================================
        // MODULE 4: FRUIT PLANTS & TREES (26 — Plains & North Bengal Hills)
        // ============================================================
        fruitPlant("mango", "Mango (Himsagar / Amrapali / Langra)", "আম গাছ (হিমসাগর, আম্রপালি)", "आम का पौधा (हिमसागर, आम्रपाली)", "Mangifera indica", "Subtropical Plains", "15°C – 42°C", "Deep loamy well-drained soil", "Full sun", "Jan – March flowering; May – July harvest", "12–30 ft (Dwarf Amrapali 8–12 ft)", "Outdoor / Large terrace drum (Amrapali)", "Plains & Lower Terai", "Hopper, fruit fly, anthracnose, powdery mildew", "Apply 1 year compost & NPK after June harvest; skip heavy watering while panicles emerge.", "জুন-জুলাইতে ফল সংগ্রহের পর সার দিন; মুকুল অবস্থায় সেচ কম রাখুন।", "जून-जुलाई तुड़ाई के बाद खाद दें; बौर आते समय सिंचाई कम रखें।"),
        fruitPlant("litchi", "Litchi (Bombai / Muzaffarpur)", "লিচু (বোম্বাই ও মুজাফফরপুর)", "लीची (बंबई / मुजफ्फरपुर)", "Litchi chinensis", "Humid subtropical plains & Terai", "12°C – 36°C", "Deep slightly acidic loam (pH 5.5–6.5)", "Full sun", "May – June harvest", "15 – 28 ft", "Outdoor orchard", "Plains & North Bengal Foothills", "Erinose mite, fruit borer", "Protect fruiting panicles from birds and spray micronutrients zinc & boron pre-bloom.", "মুকুল আসার আগে বোরন ও জিংক স্প্রে করলে ফল ফাটে না।", "बौर आने से पहले बोरॉन व जिंक छिड़कने से फल फटने की समस्या नहीं होती।"),
        fruitPlant("guava", "Guava (Baruipur / L-49 / Thai Pink)", "পেয়ারা (বারুইপুর ও থাই পিঙ্ক)", "अमरूद (L-49 / थाई पिंक)", "Psidium guajava", "Subtropical to warm hills", "12°C – 40°C", "Adaptable loamy soil", "Full sun", "Monsoon & Winter seasons", "8 – 15 ft", "Outdoor / Roof drum planter", "Both Plains & Lower Hills", "Fruit fly, wilt", "Bag developing fruits with perforated poly-paper bags to get spotless chemical-free guavas.", "ফল ছোট অবস্থায় ব্যাগিং করলে ফলের মাছি পোকা থেকে ১০০% সুরক্ষা মেলে।", "छोटे फलों पर बैगिंग करने से फल मक्खी से पूरा बचाव होता है।"),
        fruitPlant("lemon", "Lemon & Lime (Kagzi / Gandharaj)", "লেবু গাছ (কাগজি ও গন্ধরাজ)", "नींबू (कागज़ी व गंधराज)", "Citrus × aurantiifolia", "Subtropical & Terai", "14°C – 38°C", "Well-drained loamy soil", "Full sun (6+ hrs)", "Almost year-round; peak monsoon & winter", "6 – 12 ft", "Outdoor / 16-inch terrace drum", "Both Plains & Lower Hills", "Citrus leaf miner, canker", "Spray copper oxychloride before monsoon against citrus canker; regular micronutrient sprays.", "বর্ষার আগে তামা-ঘটিত ছত্রাকনাশক দিলে ক্যাংকার রোগ হয় না।", "मानसून से पहले कॉपर ऑक्सीक्लोराइड छिड़काव से कैंकर रोग रुकता है।"),
        fruitPlant("darjeeling_orange", "Darjeeling Mandarin Orange", "দার্জিলিং কমলালেবু", "दार्जिलिंग संतरा", "Citrus reticulata", "Sub-temperate cool hills", "8°C – 26°C", "Rich slightly acidic hill loam (pH 5.5–6.2)", "Full hill sun", "Nov – Jan harvest", "10 – 16 ft", "Outdoor hill orchard", "North Bengal Hills (Kurseong, Mirik, Kalimpong)", "Citrus dieback, greening, fruit fly", "Plant disease-free grafted/nucellar clones on Cleopatra or Rangpur rootstock; mulch winter basins.", "নিরোগ কলম বা নিউসেলার চারা লাগান ও শীতকালে গোড়ায় জৈব মাল্চিং করুন।", "रोग-मुक्त ग्राफ्टेड पौधे लगाएँ और जड़ों में जैविक मल्च रखें।"),
        fruitPlant("malta", "Malta / Bari-1 Sweet Orange", "মাল্টা (বারি-১ / ভিয়েতনামি)", "माल्टा (मीठा संतरा)", "Citrus × sinensis", "Subtropical humid", "14°C – 36°C", "Sandy loam with good drainage", "Full sunlight", "Oct – Jan harvest", "8 – 14 ft", "Outdoor / Large roof drum", "Plains & Terai (Siliguri/Jalpaiguri)", "Leaf miner, gummosis", "Prune water-shoots below graft union immediately and provide zinc-boron foliar feed.", "গ্রাফটিং গাঁটের নিচ থেকে ওঠা বন্য ডাল দ্রুত কেটে ফেলুন।", "ग्राफ्टिंग जोड़ के नीचे से निकलने वाली टहनियाँ तुरंत हटा दें।"),
        fruitPlant("amla", "Amla (Indian Gooseberry)", "আমলকি", "आंवला", "Phyllanthus emblica", "Dry to humid subtropical", "10°C – 42°C", "Wide tolerance including sandy loam", "Full sun", "Oct – Jan fruiting", "15 – 28 ft", "Outdoor homestead/orchard", "Both Plains & Lower Hills", "Bark-eating caterpillar, rust", "Choose grafted NA-7 or Chakaiya cultivars for early heavy fruiting.", "শীঘ্র ফলন পেতে কলম করা এনএ-৭ বা চকাইয়া জাত রোপণ করুন।", "शीघ्र फलन के लिए NA-7 या चकैया की कलमी किस्म लगाएँ।"),
        fruitPlant("jackfruit", "Jackfruit (Kathal)", "কাঁঠাল", "कटहल", "Artocarpus heterophyllus", "Warm humid tropical", "16°C – 40°C", "Deep well-drained alluvial soil", "Full sun", "March – July (All-season Thai types avail.)", "20 – 40 ft", "Outdoor orchard", "Plains & Lower Terai", "Fruit rot (Rhizopus), shoot borer", "Prune overcrowding inner limbs and ensure clean orchard floor during fruiting.", "ফল পচা এড়াতে অতিরিক্ত ভেতরের ডাল ছেঁটে সূর্যালোক প্রবেশ করতে দিন।", "फल सड़न रोकने के लिए भीतरी टहनियाँ छांटकर धूप आने दें।"),
        fruitPlant("banana", "Banana (Martaman / Champa / G9)", "কলা গাছ (মর্তমান, চাঁপা, জি-৯)", "केला (मर्तमान, चंपा, G-9)", "Musa × paradisiaca", "Humid warm tropical & Terai", "18°C – 38°C", "Deep moist organic rich loam", "Full sunlight", "11–13 months year-round harvest", "6 – 12 ft", "Outdoor farm / backyard", "Plains & Foothills", "Panama wilt, Sigatoka leaf spot, weevil", "Plant tissue-culture G9/Grand Naine; retain only 1 healthy follower sucker per clump.", "রোগমুক্ত টিস্যু-কালচার চারা বসান ও এক ঝাড়ে একটি মাত্র সুস্থ পাশ-চারা রাখুন।", "रोग-मुक्त टिश्यू-कल्चर पौधे लगाएँ और केवल एक स्वस्थ पुत्ती रखें।"),
        fruitPlant("papaya", "Papaya (Red Lady / Green Berry)", "পেঁপে (রেড লেডি ৭৮৬)", "पपीता (रेड लेडी 786)", "Carica papaya", "Warm frost-free plains", "18°C – 38°C", "Light raised soil (zero waterlogging)", "Full sun", "9 – 10 months onwards year-round", "6 – 10 ft", "Outdoor raised beds / large drum", "Plains & Lower Terai", "Papaya ringspot virus, damping off", "Plant on raised mounds so rain never pools against the hollow stem.", "গোড়ায় জল জমলে গাছ পচে যায়, তাই উঁচু বেডে চারা রোপণ করুন।", "जड़ों में जलजमाव से बचाने के लिए ऊँची मेड़ पर रोपाई करें।"),
        fruitPlant("pomegranate", "Pomegranate (Bhagwa / Super Bhagwa)", "বেদানা / ডালিম (ভাগওয়া)", "अनार (भगवा / सुपर भगवा)", "Punica granatum", "Warm dry-to-subtropical", "12°C – 40°C", "Well-drained loamy soil", "Full blazing sun", "6–7 months after bahar pruning", "6 – 12 ft", "Outdoor garden / drum container", "Plains & Lower Hills", "Bacterial blight, fruit borer", "Ensure even irrigation during fruit swell to prevent cracking.", "ফল বড় হওয়ার সময় নিয়মিত হালকা জল দিন যাতে ফল ফেটে না যায়।", "फल बढ़ते समय नियमित सिंचाई करें ताकि फल न फटें।"),
        fruitPlant("jamun", "Jamun (Thai White / Black Kala Jam)", "কালোজাম (থাই সাদা ও কালো)", "जामुन", "Syzygium cumini", "Humid subtropical & Terai", "14°C – 40°C", "Deep alluvial or loamy soil", "Full sun", "May – July harvest", "15 – 35 ft (grafted dwarf 10–15 ft)", "Outdoor homestead & avenue", "Plains & Lower Hills", "Leaf-eating caterpillar", "Choose grafted Thai or Konkan Bahadoli varieties for compact size & big pulp.", "কম সময়ে ফল পেতে কলম করা থাই কালোজাম রোপণ করুন।", "जल्दी व बड़े फल के लिए ग्राफ्टेड थाई जामुन लगाएँ।"),
        fruitPlant("wax_apple", "Wax Apple (Jamrul / Champa Jamrul)", "জামরুল (থাই লাল ও সাদা)", "जामरुल / वाटर एप्पल", "Syzygium samarangense", "Humid warm tropical & Bengal plains", "18°C – 38°C", "Humus-rich moist loam", "Full sun", "March – June & post-monsoon", "10 – 18 ft", "Outdoor courtyard / large drum", "Plains & Terai", "Fruit fly, scales", "Keep soil moist during fruiting for crisp juicy wax apples.", "ফল ধরার সময় নিয়মিত জল দিলে জামরুল রসালো ও মিষ্টি হয়।", "फल लगते समय पर्याप्त नमी रखने से फल रसीले बनते हैं।"),
        fruitPlant("avocado", "Avocado (Hass / Kalimpong Hill)", "অ্যাভোকাডো (মাখন ফল)", "एवोकाडो (मक्खन फल)", "Persea americana", "Frost-free mild hills & cool humid plains", "12°C – 32°C", "Deep loose well-drained acidic-neutral loam", "Full morning sun", "July – October (variety dependent)", "15 – 28 ft", "Outdoor hill / Terai orchard", "Hills (Kalimpong / Mirik) & Cool Terai", "Phytophthora root rot", "Never plant in water-retentive clay; graft A+B flower types nearby for heavy yield.", "পানি জমে এমন মাটিতে রোপণ করবেন না; পরাগায়নের জন্য দুটি জাত পাশাপাশি বসান।", "जल-जमाव वाली मिट्टी से बचें; बेहतर परागण के लिए दो किस्में साथ लगाएँ।", isRare = true),
        fruitPlant("dragon_fruit", "Dragon Fruit (Red & Pink Pitaya)", "ড্রাগন ফ্রুট", "ड्रैगन फ्रूट", "Selenicereus undatus", "Warm frost-free sun", "16°C – 40°C", "Porous sandy-compost mix", "Full sun", "May – November", "5 – 6 ft concrete/tyre trellis", "Outdoor terrace or commercial pillar", "Plains & Foothills", "Stem rot, sunburn, ants", "Use concrete pillars with top ring support; avoid soggy winter pots.", "সিমেন্টের খুঁটি ও রিং সাপোর্ট ব্যবহার করুন এবং বর্ষায় জল নিকাশ ঠিক রাখুন।", "सीमेंट पिलर व टायर रिंग का सहारा दें तथा जल निकास बनाए रखें।", extraFilters = listOf(FRUIT, SUCCULENT_CACTUS)),
        fruitPlant("pineapple", "Pineapple (Kew / Queen / Siliguri Bidhannagar)", "আনারস (কিউ ও কুইন)", "अनानास (क्यू व क्वीन)", "Ananas comosus", "Humid tropical plains & Terai", "18°C – 36°C", "Slightly acidic sandy loam (pH 5.0–6.0)", "Full sun to light intercrop shade", "June – August & winter off-season", "2 – 3.5 ft rosette", "Outdoor beds / Terai farm", "Siliguri / North Dinajpur / Terai Plains", "Mealybug wilt, heart rot", "Dip suckers/slips in safe fungicide before planting; ideal intercrop in young orchards.", "চারা বসানোর আগে ছত্রাকনাশকে শোধন করে নিন; শিলিগুড়ি অঞ্চলের মাটিতে দারুণ ফলন হয়।", "रोपाई से पहले सकर्स उपचारित करें; सिलीगुड़ी-तराई क्षेत्र में उत्तम उपज।"),
        fruitPlant("coconut", "Coconut (Gangabondam / Tall / Hybrid)", "নারকেল (খাটো ও লম্বা জাত)", "नारियल", "Cocos nucifera", "Warm humid coastal & alluvial plains", "20°C – 38°C", "Deep alluvial sandy loam", "Full sun", "Year-round after 3–5 yrs (Dwarf)", "18 – 50 ft palm", "Outdoor homestead", "Plains", "Rhinoceros beetle, red palm weevil", "Feed 1.3 kg potash + salt + boron yearly per bearing palm and clear dried spathes.", "বছরে একবার পটাশ, বোরন ও খনিজ সার দিন এবং পচা পাতা-খোল পরিষ্কার রাখুন।", "वर्ष में एक बार पोटाश व बोरॉन दें तथा सूखे पत्ते साफ़ रखें।", extraFilters = listOf(FRUIT, ORNAMENTAL_TREE)),
        fruitPlant("areca_nut", "Areca Nut (Supari — Mangala / Mohitnagar)", "সুপারি (মঙ্গলা ও মোহিতনগর)", "सुपारी (मंगला / मोहितनगर)", "Areca catechu", "Humid sub-Himalayan Terai & Dooars", "16°C – 36°C", "Moist organic alluvial loam", "Full sun (young seedlings need partial shade)", "Winter harvest", "20 – 40 ft slim palm", "Outdoor Dooars/Terai farm & boundary", "Jalpaiguri / Alipurduar / Cooch Behar / Siliguri", "Bud rot, yellow leaf disease", "Mohitnagar (Dooars pride!) thrives in Jalpaiguri-Siliguri belt with annual lime-compost basins.", "জলপাইগুড়ি ও কোচবিহারের ঐতিহ্যবাহী মোহিতনগর জাত বসান ও বর্ষার আগে বোর্দো পেস্ট দিন।", "जलपाईगुड़ी-कूचबिहार की प्रसिद्ध मोहितनगर किस्म लगाएँ।", extraFilters = listOf(FRUIT, ORNAMENTAL_TREE)),
        fruitPlant("apple", "Low-Chill & Hill Apple (Anna / Dorsett Golden)", "আপেল চারা (পাহাড়ি ও হরমন-৯৯)", "सेब (लो-चिल व पहाड़ी)", "Malus domestica", "Cool hills & experimental subtropical winters", "0°C – 26°C", "Deep fertile loamy soil (pH 6.0)", "Full sun", "June – August", "10 – 16 ft", "Outdoor orchard / experimental pot", "Hill Regions (High Darjeeling/Kalimpong) & Winter Plains (HRMN-99)", "Scab, woolly aphid, fire blight", "High-hill varieties require cold winter chilling; plains growers should plant only HRMN-99 / Anna low-chill.", "সমতলে কেবল হরমন-৯৯ (HRMN-99) বা আন্না লো-চিল জাত ও পাহাড়ে সাধারণ জাত রোপণ করুন।", "पहाड़ों में पारंपरिक व मैदानों में केवल लो-चिल (HRMN-99 / Anna) किस्म लगाएँ।"),
        fruitPlant("pear", "Hill & Sand Pear (Nashpati)", "নাশপাতি", "नाशपाती", "Pyrus communis / pyrifolia", "Temperate hills & mild plains", "2°C – 28°C", "Deep fertile loamy soil", "Full sun", "July – September", "12 – 20 ft", "Outdoor hill orchard", "Hills (Darjeeling/Kalimpong) & Lower Hills", "Pear psylla, blight", "Low-chill Patharnakh/Gola works in lower foothills while Bartlett/Bagugosha excels in hills.", "পাহাড়ে বাগুগোষা ও নিচু অঞ্চলে গোলানাশপাতি দারুণ ফলন দেয়।", "पहाड़ों में बागुगोशा व निचली पहाड़ियों में पत्थरनाख बढ़िया फलती है।"),
        fruitPlant("peach", "Peach (Darjeeling & Mid-Hill)", "পিচ ফল (পাহাড়ি)", "आड़ू (पीच)", "Prunus persica", "Cool Himalayan mid-hills", "4°C – 26°C", "Light sandy loam with crisp drainage", "Full sun", "May – July", "10 – 16 ft", "Outdoor hill orchard", "Hills (Darjeeling / Kalimpong / Kurseong)", "Peach leaf curl", "Prune annually in winter dormancy and spray safe copper before bud break against leaf curl.", "শীতে ডাল ছাঁটাই ও কুঁড়ি ফোটার আগে তামা-ঘটিত ছত্রাকনাশক স্প্রে করুন।", "सर्दियों में छँटाई करें और कली फूटने से पहले फफूंदनाशी छिड़कें।"),
        fruitPlant("plum", "Plum (Alubukhara / Kalimpong)", "আলুবোখারা (প্লাম)", "आलूबुखारा (प्लम)", "Prunus salicina", "Cool Himalayan slopes", "4°C – 25°C", "Well-drained rich loam", "Full sun", "May – June", "10 – 18 ft", "Outdoor hill terrace", "Hill Regions (1,000–2,200 m)", "Aphids, brown rot", "Plant Santa Rosa or Kala Amritsari for juicy sweet-tart hill plums.", "সান্টা রোসা জাতের কলম রোপণ করলে পাহাড়ে প্রচুর ফলন আসে।", "पहाड़ों में सैंटा रोज़ा किस्म भरपूर उपज देती है।"),
        fruitPlant("apricot", "Apricot (Khubani)", "খুবানি (অ্যাপ্রিকট)", "खुबानी (एप्रीकॉट)", "Prunus armeniaca", "Cool temperate dry-winter hills", "0°C – 24°C", "Deep gritty well-drained hill soil", "Full sun", "May – June", "12 – 20 ft", "Outdoor hill orchard", "Higher Hill Regions (1,600–2,800 m)", "Shot-hole fungus", "Avoid damp valley pockets where spring frost damages early blossoms.", "তুষারপাতমুক্ত রৌদ্রোজ্জ্বল পাহাড়ি ঢালে রোপণ করুন।", "पाला-मुक्त धूपदार पहाड़ी ढलानों पर लगाएँ।", isRare = true),
        fruitPlant("kiwi", "Kiwi Fruit (Hayward / Allison)", "কিউই ফল (পাহাড়ি লতা)", "कीवी फल", "Actinidia deliciosa", "Temperate humid Eastern Himalaya", "4°C – 24°C", "Deep acidic humus-rich loam (pH 5.5)", "Full hill sun with windbreak", "Oct – Nov harvest", "12 – 20 ft vigorous T-bar vine", "Outdoor T-bar pergola orchard", "Hill Regions (Kalimpong / Kurseong / Mirik)", "Crown gall, root rot", "Requires both Male & Female grafted vines (1 male per 8 females) on sturdy steel T-trellis.", "ফল ধরতে হলে স্ত্রী গাছের সাথে অন্তত ১টি পুরুষ গাছ ও মজবুত টি-আকারের মাচা প্রয়োজন।", "फलन के लिए मादा के साथ नर बेल व मज़बूत टी-ट्रेलिस लगाना आवश्यक है।", isRare = true),
        fruitPlant("walnut", "Himalayan Walnut (Akhrot)", "আখরোট", "अखरोट (हिमालयी)", "Juglans regia", "Cool temperate high hills", "0°C – 24°C", "Deep fertile calcareous hill soil", "Full sun", "August – October nut fall", "25 – 50 ft", "Outdoor high-hill farm", "High Hill Regions (1,500–3,000 m)", "Walnut blight, husk fly", "Choose thin-shelled (Kagzi) grafted plants for compact height & early nut yield.", "পাতলা খোসার কাগজি আখরোটের কলম বসালে তাড়াতাড়ি ফলন মেলে।", "पतले छिलके वाले कागज़ी अखरोट की कलमी किस्म लगाएँ।", isRare = true),
        fruitPlant("strawberry", "Strawberry (Winter Dawn / Chandler)", "স্ট্রবেরি", "स्ट्रॉबेरी", "Fragaria × ananassa", "Cool winters & mid-hills", "10°C – 25°C", "Raised mulched soil / cocopeat-perlite mix", "Full winter sun", "Jan – April (Plains); Mar – Jun (Hills)", "6 – 10 inches runner plant", "Raised straw-mulched beds & hanging balcony boxes", "Both Plains (Winter) & Hills (Kalimpong/Mirik)", "Grey mould, red spider mite", "Keep ripening red berries off wet soil using clean paddy-straw or black plastic mulch.", "পাকা ফল মাটির ছোঁয়ায় পচে যাওয়া রোধে খড় বা প্লাস্টিক মাল্চ ব্যবহার করুন।", "फलों को मिट्टी से सड़ने से बचाने हेतु पुआल या प्लास्टिक मल्चिंग करें।"),

        // ============================================================
        // MODULE 5: ORNAMENTAL TREES & CLIMBERS (17 + shared champa/bougainvillea/aparajita/areca_palm)
        // ============================================================
        ornamentalTree("gulmohar", "Gulmohar (Royal Poinciana)", "গুলমোহর / কৃষ্ণচূড়া", "गुलमोहर", "Delonix regia", "Scarlet flame umbrella tree brightening Bengal streets & parks in Baishakh.", "বৈশাখে আগুনে লাল ফুলে পুরো এলাকা রাঙিয়ে দেওয়া গুলমোহর/কৃষ্ণচূড়া।", "वैशाख व गर्मियों में लाल सुर्ख फूलों से सजने वाला गुलमोहर।", "Apr – Jun", "25 – 45 ft umbrella canopy"),
        ornamentalTree("krishnachura", "Dwarf Krishnachura (Peacock Flower)", "ছোট কৃষ্ণচূড়া", "छोटी कृष्णचूड़ा", "Caesalpinia pulcherrima", "Compact fiery crimson-and-gold garden shrub/small tree ideal for home yards.", "ঘরের বাগানের জন্য উপযোগী লাল-হলুদ কম উচ্চতার ছোট কৃষ্ণচূড়া।", "बगीचों के लिए उपयुक्त छोटे कद का लाल-सुनहरा कृष्णचूड़ा।", "Mar – Nov", "6 – 12 ft"),
        ornamentalTree("radhachura", "Radhachura (Yellow Flame Tree)", "রাধাচূড়া", "राधाचूड़ा (पीला गुलमोहर)", "Peltophorum pterocarpum", "Golden-yellow upright flower panicles often planted beside scarlet Gulmohar.", "সোনালি-হলুদ ফুলের খাড়া মঞ্জরিযুক্ত দৃষ্টিনন্দন রাধাচূড়া বৃক্ষ।", "सुनहरे-पीले फूलों वाला सजावटी राधाचूड़ा वृक्ष।", "Apr – Aug", "25 – 45 ft"),
        ornamentalTree("jarul", "Jarul (Pride of India)", "জারুল গাছ", "जारुल", "Lagerstroemia speciosa", "Siliguri-Dooars highway favourite showcasing papery lilac-purple crape blooms.", "শিলিগুড়ি ও ডুয়ার্সের পথের দুপাশে ফোটা বেগুনি-গোলাপি মনোরম জারুল।", "सिलीगुड़ी व डुआर्स की सड़कों पर खिलने वाला बैंगनी जारुल।", "May – Aug", "20 – 35 ft"),
        ornamentalTree("sonajhuri", "Sonajhuri (Earleaf Acacia)", "সোনাঝুরি", "सोनाझुरी", "Acacia auriculiformis", "Fast-growing golden catkins avenue & soil-stabilizing woodland tree.", "হলুদ ঝালরের মতো ফুল ও দ্রুত বর্ধনশীল সোনাঝুরি বৃক্ষ।", "तेज़ी से बढ़ने वाला सुनहरे फूलों का सोनाझुरी वृक्ष।", "Sep – Dec", "25 – 45 ft"),
        ornamentalTree("kadam", "Kadam Tree", "কদম গাছ", "कदंब वृक्ष", "Neolamarckia cadamba", "Bengal monsoon icon bearing fragrant spherical golden-white pom-pom blossoms.", "বর্ষার প্রথম বৃষ্টিতেই সোনালি-সাদা বল আকৃতির সুগন্ধি ফুল ফোটা ঐতিহ্যবাহী কদম গাছ।", "मानसून में सुगंधित गोल फूलों से भर जाने वाला कदंब वृक्ष।", "Jun – Sep", "30 – 50 ft"),
        ornamentalTree("bakul", "Bakul (Spanish Cherry)", "বকুল ফুল গাছ", "मौलश्री (बकुल)", "Mimusops elengi", "Dense evergreen shade tree dropping tiny star-shaped intensely fragrant cream flowers.", "ঘন ছায়াদানকারী চিরসবুজ ও মিষ্টি সুগন্ধি তারা-ফুলের বকুল গাছ।", "घनी छाया और मीठी खुशबू वाले छोटे सितारा-फूलों का मौलश्री वृक्ष।", "Mar – Jul", "20 – 40 ft"),
        ornamentalTree("palash", "Palash (Flame of the Forest)", "পলাশ ফুল", "पलाश (टेसू)", "Butea monosperma", "Spring heralder dazzling leafless Rarh & Bengal landscapes in tangerine-vermilion.", "বসন্তে আগুনরাঙা কমলা-লাল পলাশ ফুলের ঐতিহ্যবাহী বৃক্ষ।", "बसंत में केसरिया-लाल फूलों से महकने वाला पलाश/टेसू वृक्ष।", "Feb – Apr", "20 – 35 ft"),
        ornamentalTree("shimul", "Shimul (Silk Cotton Tree)", "শিমুল গাছ", "सेमल वृक्ष", "Bombax ceiba", "Towering spiny-trunk tree blazing with crimson bird-pollinated cup blooms in February.", "ফেব্রুয়ারিতে পাখি-আকর্ষক বড় লাল ফুলে ভরে ওঠা বিশাল শিমুল গাছ।", "बसंत में बड़े लाल फूलों व रुई देने वाला सेमल वृक्ष।", "Feb – Mar", "40 – 70 ft"),
        ornamentalTree("neem", "Neem Tree", "নিম গাছ", "नीम का पेड़", "Azadirachta indica", "Multipurpose medicinal & natural-pesticide shade tree purifying air and soil.", "সর্বরোগহারী ও জৈব কীটনাশক তৈরির সেরা চিরহরিৎ নিম বৃক্ষ।", "औषधीय गुणों व प्राकृतिक कीटनाशक बनाने वाला सदाबहार नीम।", "Mar – May", "25 – 45 ft", extraGroups = listOf(MEDICINAL_HERBS_SUCCULENTS), extraFilters = listOf(ORNAMENTAL_TREE, MEDICINAL)),
        ornamentalTree("arjun", "Arjun Tree", "অর্জুন গাছ", "अर्जुन वृक्ष", "Terminalia arjuna", "Statuesque pale-bark riverside medicinal & shade avenue tree revered in Ayurveda.", "মসৃণ হালকা বাকলের আয়ুর্বেদিক গুণসম্পন্ন দৃষ্টিনন্দন অর্জুন গাছ।", "चिकनी छाल वाला प्रसिद्ध आयुर्वेदिक व छायादार अर्जुन वृक्ष।", "Apr – Jun", "30 – 60 ft", extraGroups = listOf(MEDICINAL_HERBS_SUCCULENTS), extraFilters = listOf(ORNAMENTAL_TREE, MEDICINAL)),
        ornamentalTree("teak", "Teak (Segun Timber)", "সেগুন গাছ", "सागौन (टीक)", "Tectona grandis", "High-value commercial hardwood plantation tree grown across Terai & West Bengal.", "সর্বোচ্চ বাণিজ্যিক মূল্যের শক্ত কাঠের সেগুন বৃক্ষ।", "उच्च मूल्य वाली इमारती लकड़ी का व्यावसायिक सागौन वृक्ष।", "Jul – Sep", "40 – 70 ft"),
        ornamentalTree("mahogany", "Mahogany Tree", "মেহগনি গাছ", "महोगनी वृक्ष", "Swietenia mahagoni", "Straight-bole rapid timber tree widely planted along North Bengal farm borders.", "জমির আইলে দ্রুত বাড়া বাণিজ্যিক কাঠের মেহগনি বৃক্ষ।", "खेत की मेड़ों पर तेज़ी से बढ़ने वाला इमारती महोगनी वृक्ष।", "Apr – Jun", "35 – 60 ft"),
        ornamentalTree("bamboo", "Ornamental & Buddha Belly Bamboo", "বাঁশ (বুদ্ধা বেলি ও শোভাবর্ধক)", "सजावटी बांस (बुद्धा बेली)", "Bambusa ventricosa", "Lush architectural privacy screen and Feng-shui courtyard bamboo clump.", "বাগান ও সীমানা সাজানোর আকর্ষণীয় সবুজ ও সোনালি শোভাবর্ধক বাঁশ।", "बगीचे और बाड़ की शोभा बढ़ाने वाला सजावटी व बुद्धा-बेली बांस।", "Evergreen canes", "8 – 25 ft clump"),
        ornamentalTree("foxtail_palm", "Foxtail Palm", "ফক্সটেইল পাম", "फॉक्सटेल पाम", "Wodyetia bifurcata", "Smooth-trunked majestic avenue palm with bushy bottle-brush green plumes.", "রাস্তা ও লনের দুপাশে সারিবদ্ধভাবে লাগানোর রাজকীয় ফক্সটেইল পাম।", "ड्राइव-वे व लॉन के लिए लोकप्रिय घने पत्तों वाला फॉक्सटेल पाम।", "Evergreen", "18 – 30 ft palm"),
        ornamentalTree("cycas", "Cycas (Sago Palm)", "সাইকাস (সাগু পাম)", "साइकस (सागो पाम)", "Cycas revoluta", "Ancient slow-growing feathery rosette cycad prized as architectural lawn sculpture.", "ধীরে বাড়া অত্যন্ত টেকসই প্রাচীন সৌন্দর্যমণ্ডিত সাইকাস পাম।", "धीमी गति से बढ़ने वाला अत्यंत टिकाऊ सजावटी साइकस पाम।", "Evergreen architectural", "3 – 8 ft rosette"),
        ornamentalTree("madhabilata", "Madhabilata (Rangoon Creeper)", "মাধবীলতা (রঙ্গুন ক্রিপার)", "मधुमालती (झुमका बेल)", "Combretum indicum", "Spectacular arching climber bursting into white-to-pink-to-crimson fragrant flower tassels.", "সাদা থেকে গোলাপি ও লাল রঙে রূপান্তরিত থোকা থোকা সুগন্ধি মাধবীলতা ফুল।", "सफेद, गुलाबी व लाल रंगों के गुच्छों में खिलने वाली सुगंधित मधुमालती बेल।", "Mar – Nov", "10 – 25 ft climber", extraFilters = listOf(FLOWERING, ORNAMENTAL_TREE), isClimber = true),

        // ============================================================
        // MODULE 6: MEDICINAL PLANTS, HERBS & SUCCULENTS (17 + Neem/Arjun)
        // ============================================================
        herbOrSucculent("tulsi", "Tulsi (Holy Basil — Krishna & Rama)", "তুলসী (কৃষ্ণ ও রাম তুলসী)", "तुलसी (श्यामा व रामा)", "Ocimum tenuiflorum", isSucculent = false, "Sacred aromatic Ayurvedic herb soothing coughs, colds, and daily wellness.", "গৃহস্থ ও আয়ুর্বেদে সর্দি-কাশি ও রোগপ্রতিরোধে ব্যবহৃত পবিত্র তুলসী গাছ।", "आयुर्वेद में खांसी-जुकाम व रोग-प्रतिरोधक क्षमता हेतु पूजनीय तुलसी।", "Full morning sun", "Every 2–3 days"),
        herbOrSucculent("aloe_vera", "Aloe Vera (Ghritakumari)", "অ্যালোভেরা (ঘৃতকুমারী)", "एलोवेरा (ग्वारपाठा)", "Aloe barbadensis miller", isSucculent = true, "Gel-filled rosette succulent famed for skin care, cooling soothing gel, and dry balconies.", "ত্বকের যত্ন ও ভেষজ জেল সমৃদ্ধ অত্যন্ত কম জলের সাকুলেন্ট ঘৃতকুমারী।", "त्वचा व औषधीय जेल से भरपूर कम पानी वाला ग्वारपाठा/एलोवेरा।", "Bright indirect to morning sun", "Every 8–10 days"),
        herbOrSucculent("thankuni", "Thankuni (Gotu Kola / Brahmi)", "থানকুনি পাতা", "थानकुनी / मंडूकपर्णी", "Centella asiatica", isSucculent = false, "Trailing shade-tolerant Bengal medicinal groundcover celebrated for gut health & memory.", "পেটের সুস্থতা ও স্মৃতিশক্তি বৃদ্ধিতে প্রসিদ্ধ বাংলার চিরচেনা থানকুনি পাতা।", "पाचन व स्मरण शक्ति के लिए प्रसिद्ध औषधीय मंडूकपर्णी (ब्रह्मी)।", "Semi-shade", "Keep soil consistently damp"),
        herbOrSucculent("basak", "Basak (Malabar Nut)", "বাসক পাতা", "अडूसा / वासा", "Justicia adhatoda", isSucculent = false, "Evergreen medicinal shrub classically used across Bengali homes against cough and bronchitis.", "সর্দি-কাশি ও শ্বাসকষ্ট উপশমে প্রসিদ্ধ সবুজ ভেষজ বাসক গাছ।", "खांसी व श्वसन रोगों के लिए प्रसिद्ध औषधीय अडूसा/वासा।", "Sun to light shade", "Every 3–4 days"),
        herbOrSucculent("kalmegh", "Kalmegh (King of Bitters)", "কালমেঘ", "कालमेघ", "Andrographis paniculata", isSucculent = false, "Bitter liver-support & immunity medicinal herb native to Bengal gardens.", "লিভারের সুস্থতা ও জ্বর-রোগ প্রতিরোধে কার্যকরী চিরপরিচিত কালমেঘ।", "यकृत (लिवर) स्वास्थ्य व प्रतिरक्षा बढ़ाने वाली प्रसिद्ध जड़ी-बूटी।", "Sun to half-shade", "Every 2–3 days"),
        herbOrSucculent("sarpagandha", "Sarpagandha (Indian Snakeroot)", "সর্পগন্ধা", "सर्पगंधा", "Rauvolfia serpentina", isSucculent = false, "Rare Himalayan-foothill medicinal shrub conserved for its traditional alkaloid roots.", "উত্তরবঙ্গের পাদদেশীয় অঞ্চলের ঐতিহ্যবাহী ও বিরল ঔষধি গাছ সর্পগন্ধা।", "हिमालय की तलहटी का दुर्लभ व महत्वपूर्ण औषधीय पौधा।", "Partial woodland shade", "Every 3–4 days", isRare = true),
        herbOrSucculent("lemongrass", "Lemongrass", "লেমনগ্রাস", "लेमनग्रास", "Cymbopogon citratus", isSucculent = false, "Citrus-scented culinary & herbal tea grass also deterring mosquitoes.", "সুগন্ধি লেবু-চা ও মশা তাড়াতে সহায়ক লেমনগ্রাস ঝাড়।", "सुगंधित हर्बल चाय व मच्छर-रोधी लेमनग्रास।", "Full sun", "Every 3 days"),
        herbOrSucculent("mint", "Mint (Pudina)", "পুদিনা পাতা", "पुदीना", "Mentha spicata", isSucculent = false, "Refreshing aromatic culinary runner thriving in shaded moist balcony troughs.", "চাটনি ও শরবতের জন্য দ্রুত বর্ধনশীল আর্দ্রতাপ্রিয় পুদিনা।", "रसोई व पेय में उपयोग होने वाला ताज़ा पुदीना।", "Morning sun + afternoon shade", "Keep evenly moist"),
        herbOrSucculent("rosemary", "Rosemary", "রোজমেরি", "रोज़मेरी", "Salvia rosmarinus", isSucculent = false, "Pine-scented Mediterranean herb thriving in gritty sunny balconies and Kalimpong hills.", "চুল ও রান্নায় ব্যবহৃত সুগন্ধি রোজমেরি ভেষজ।", "सुगंधित और बालों व पाक कला में उपयोगी रोज़मेरी हर्ब।", "Full bright sun (4–6 hrs)", "Only when top 2 inches dry"),
        herbOrSucculent("lavender", "Lavender", "ল্যাভেন্ডার", "लैवेंडर", "Lavandula angustifolia", isSucculent = false, "Aromatic violet-spiked hill herb suited for Darjeeling-Kalimpong slopes & cool winter pots.", "পাহাড়ি ঠান্ডা আবহাওয়া ও সুগন্ধি তেলের জন্য প্রিয় বেগুনি ল্যাভেন্ডার।", "ठंडी पहाड़ी जलवायु व सुगंधित तेल के लिए प्रसिद्ध लैवेंडर।", "Full sun + gritty dry soil", "Sparingly; hates wet soggy roots", extraFilters = listOf(FLOWERING, HILL_FLOWER, MEDICINAL, RARE), isRare = true),
        herbOrSucculent("curry_leaf", "Curry Leaf (Meetha Neem)", "কারিপাতা", "करी पत्ता (मीठा नीम)", "Murraya koenigii", isSucculent = false, "Indispensable aromatic kitchen tree yielding fresh leaves after every pruning.", "রান্নায় স্বাদ ও সুগন্ধ বাড়াতে অপরিহার্য বহুবর্ষজীবী কারিপাতা।", "भारतीय व्यंजनों की जान, सुगंधित करी पत्ता।", "Full sun", "Every 3 days"),
        herbOrSucculent("adenium", "Adenium (Desert Rose)", "অ্যাডেনিয়াম (মরুগোলাপ)", "अडेनियम (डेज़र्ट रोज़)", "Adenium obesum", isSucculent = true, "Sculptural caudex-trunked flowering succulent producing vibrant pink-crimson trumpets in full sun.", "মোটা গোড়া ও মরুর গোলাপের মতো রঙিন ফুলে ভরা আকর্ষণীয় অ্যাডেনিয়াম।", "मोटे तने और चमकीले फूलों वाला आकर्षक डेज़र्ट रोज़।", "Full blazing sun (5+ hrs)", "Once a week; minimal in winter", extraFilters = listOf(FLOWERING, SUCCULENT_CACTUS)),
        herbOrSucculent("jade_plant", "Jade Plant (Crassula Lucky Plant)", "জেড প্ল্যান্ট (ক্র্যাসুলা)", "जेड प्लांट (क्रेसुला)", "Crassula ovata", isSucculent = true, "Chubby glossy emerald-leaf miniature tree famed for sunny desks and balconies.", "ছোট চকচকে মাংসল পাতার সৌভাগ্য-প্রতীক হিসেবে জনপ্রিয় জেড প্ল্যান্ট।", "मोटे चमकदार पत्तों वाला लोकप्रिय क्रेसुला / जेड प्लांट।", "Bright sunny sill", "Every 7–10 days"),
        herbOrSucculent("echeveria", "Echeveria (Rose Succulent)", "এচেভেরিয়া (পদ্ম সাকুলেন্ট)", "एचेवेरिया", "Echeveria elegans", isSucculent = true, "Symmetrical pastel-blue and rose-tipped stone-lotus succulent cherished across Kalimpong nurseries.", "পাথর-পদ্মের মতো সুবিন্যস্ত রোসেট গড়নের কালিম্পংয়ের বিখ্যাত এচেভেরিয়া।", "कमल जैसी सुंदर आकृति वाला कालिम्पोंग का प्रसिद्ध सक्युलेंट।", "Bright cool sun (avoid hot plains monsoon drip)", "Every 8–12 days on soil only"),
        herbOrSucculent("haworthia", "Haworthia (Zebra Succulent)", "হাওয়ার্থিয়া (জেব্রা সাকুলেন্ট)", "हॉवर्थिया (ज़ेब्रा सक्युलेंट)", "Haworthia fasciata", isSucculent = true, "Compact striped miniature rosette tolerating indoor study desks better than most succulents.", "জেব্রার মতো সাদা ডোরাকাটা ক্ষুদ্র সাকুলেন্ট যা ঘরের ভেতরেও ভালো থাকে।", "ज़ेब्रा जैसी सफेद धारियों वाला छोटा इंडोर-उपयोगी सक्युलेंट।", "Bright indirect indoor light", "Every 10–14 days"),
        herbOrSucculent("cactus", "Ornamental & Golden Barrel Cactus", "ক্যাকটাস (গোল্ডেন ব্যারেল ও গ্রাফটেড)", "कैक्टस (गोल्डन बैरल)", "Echinocactus grusonii / Gymnocalycium", isSucculent = true, "Drought-proof geometric & colour-grafted desert miniatures needing porous cactus grit.", "ন্যূনতম জলে ও কড়া রোদে টিকে থাকা নানা আকৃতির বাহারি ক্যাকটাস।", "बेहद कम पानी में उगने वाला आकर्षक सजावटी कैक्टस।", "Bright sun / sunny sill", "Every 12–18 days"),
        herbOrSucculent("kalanchoe", "Kalanchoe (Flaming Katy)", "ক্যালাঞ্চো (শীতের সাকুলেন্ট ফুল)", "कलांचो", "Kalanchoe blossfeldiana", isSucculent = true, "Scalloped fleshy-leaf winter blooming succulent bursting with red, yellow, and orange bouquets.", "মাংসল পাতায় শীতকালে থোকায় থোকায় দীর্ঘস্থায়ী ফুল ফোটা ক্যালাঞ্চো।", "सर्दियों में लंबे समय तक लाल-पीले फूल देने वाला कलांचो।", "Bright sun to light shade", "Every 6–8 days", extraFilters = listOf(FLOWERING, SUCCULENT_CACTUS, SEASONAL))
    )

    private fun indoorPlant(
        id: String,
        nameEn: String,
        nameBn: String,
        nameHi: String,
        scientific: String,
        isMoneyPlant: Boolean,
        descEn: String,
        descBn: String,
        descHi: String,
        sunlight: String,
        watering: String,
        extraGroups: List<com.example.data.model.PlantKingdomGroup> = emptyList(),
        extraFilters: List<com.example.data.model.PlantSearchFilter> = emptyList(),
        isRare: Boolean = false
    ): PlantKingdomEntry = flowerPlant(
        id = id,
        nameEn = nameEn,
        nameBn = nameBn,
        nameHi = nameHi,
        scientificName = scientific,
        group = MONEY_INDOOR_PLANTS,
        secondaryGroups = extraGroups,
        filters = buildList {
            add(INDOOR)
            add(SHADE)
            if (isMoneyPlant) add(MONEY_PLANT)
            if (isRare) add(RARE)
            addAll(extraFilters)
        }.distinct(),
        seasons = listOf(PERENNIAL, SHADE_LOVING),
        descEn = descEn,
        descBn = descBn,
        descHi = descHi,
        climate = "Indoor / Humid Tropical & Hills",
        temp = "15°C – 32°C",
        soil = "Airy cocopeat, perlite, bark & vermicompost (60:20:20)",
        sunlight = sunlight,
        watering = watering,
        fertilizer = "Balanced dilute NPK 19-19-19 or seaweed monthly",
        propagation = if (isMoneyPlant) "Single-node stem cuttings in water or cocopeat" else "Division or stem/leaf cuttings",
        seasonLabel = "Evergreen Foliage Year-Round",
        growthSize = "1 – 5 ft indoor container",
        indoorOutdoor = "Indoor home, office desk & shaded balcony",
        plainsHill = "Both Plains & Hills",
        pestsDiseases = "Mealybugs, root rot from waterlogged saucers",
        careEn = "Ensure drainage holes stay open and wipe dust off leaves gently once a month.",
        careBn = "টবের নিচে জল বেরোনোর ছিদ্র খোলা রাখুন ও মাসে একবার ভেজা কাপড়ে পাতা মুছে দিন।",
        careHi = "गमले का जल-निकास खुला रखें और महीने में एक बार गीले कपड़े से पत्तियाँ पोंछें।",
        isRare = isRare
    )

    private fun fruitPlant(
        id: String,
        nameEn: String,
        nameBn: String,
        nameHi: String,
        scientific: String,
        climate: String,
        temp: String,
        soil: String,
        sunlight: String,
        season: String,
        growthSize: String,
        indoorOutdoor: String,
        plainsHill: String,
        pests: String,
        careEn: String,
        careBn: String,
        careHi: String,
        extraFilters: List<com.example.data.model.PlantSearchFilter> = emptyList(),
        isRare: Boolean = false
    ): PlantKingdomEntry = flowerPlant(
        id = id,
        nameEn = nameEn,
        nameBn = nameBn,
        nameHi = nameHi,
        scientificName = scientific,
        group = FRUIT_PLANTS,
        filters = buildList {
            add(FRUIT)
            if (isRare) add(RARE)
            addAll(extraFilters)
        }.distinct(),
        seasons = listOf(PERENNIAL),
        descEn = "Cultivated fruit variety suited to $plainsHill under $climate conditions.",
        descBn = "$plainsHill অঞ্চলের জলবায়ুতে চাষযোগ্য উন্নত ও ফলনশীল $nameBn চারা।",
        descHi = "$plainsHill क्षेत्र की जलवायु के लिए उपयुक्त उच्च उपज वाली $nameHi किस्म।",
        climate = climate,
        temp = temp,
        soil = soil,
        sunlight = sunlight,
        watering = "Deep basin irrigation at establishment & fruit fill; drip preferred",
        fertilizer = "Organic farmyard manure + NPK + zinc/boron split pre-monsoon & post-harvest",
        propagation = "True-to-type grafted saplings, air layering, or certified tissue-culture",
        seasonLabel = season,
        growthSize = growthSize,
        indoorOutdoor = indoorOutdoor,
        plainsHill = plainsHill,
        pestsDiseases = pests,
        careEn = careEn,
        careBn = careBn,
        careHi = careHi,
        isRare = isRare
    )

    private fun ornamentalTree(
        id: String,
        nameEn: String,
        nameBn: String,
        nameHi: String,
        scientific: String,
        descEn: String,
        descBn: String,
        descHi: String,
        season: String,
        size: String,
        extraGroups: List<com.example.data.model.PlantKingdomGroup> = emptyList(),
        extraFilters: List<com.example.data.model.PlantSearchFilter> = listOf(ORNAMENTAL_TREE),
        isClimber: Boolean = false
    ): PlantKingdomEntry = flowerPlant(
        id = id,
        nameEn = nameEn,
        nameBn = nameBn,
        nameHi = nameHi,
        scientificName = scientific,
        group = ORNAMENTAL_TREES_CLIMBERS,
        secondaryGroups = extraGroups,
        filters = extraFilters,
        seasons = if (isClimber) listOf(PERENNIAL, MONSOON, SUMMER) else listOf(PERENNIAL),
        descEn = descEn,
        descBn = descBn,
        descHi = descHi,
        climate = "Tropical, Subtropical Plains & Foothills",
        temp = "12°C – 40°C",
        soil = "Deep fertile well-drained garden or avenue loam",
        sunlight = "Full sunlight",
        watering = "Regular first 2 summers; drought-hardy once established",
        fertilizer = "Annual compost ring pre-monsoon",
        propagation = "Nursery-grown polybag saplings, air layering, or cuttings",
        seasonLabel = season,
        growthSize = size,
        indoorOutdoor = "Outdoor garden, avenue, boundary & pergola",
        plainsHill = "Plains & Lower Hills",
        pestsDiseases = "Stem borer, leaf caterpillars",
        careEn = "Stake tall young saplings during monsoon storms and mulch root ring annually.",
        careBn = "চারা অবস্থায় ঝড়ে হেলে পড়া ঠেকাতে শক্ত খুঁটি বাঁধুন এবং বছরে একবার জৈব সার দিন।",
        careHi = "छोटे पौधे को आंधी से बचाने के लिए मज़बूत सहारा दें और सालाना जैविक खाद डालें।"
    )

    private fun herbOrSucculent(
        id: String,
        nameEn: String,
        nameBn: String,
        nameHi: String,
        scientific: String,
        isSucculent: Boolean,
        descEn: String,
        descBn: String,
        descHi: String,
        sunlight: String,
        watering: String,
        extraFilters: List<com.example.data.model.PlantSearchFilter> = emptyList(),
        isRare: Boolean = false
    ): PlantKingdomEntry = flowerPlant(
        id = id,
        nameEn = nameEn,
        nameBn = nameBn,
        nameHi = nameHi,
        scientificName = scientific,
        group = MEDICINAL_HERBS_SUCCULENTS,
        filters = buildList {
            if (isSucculent) add(SUCCULENT_CACTUS) else add(MEDICINAL)
            if (isRare) add(RARE)
            addAll(extraFilters)
        }.distinct(),
        seasons = listOf(PERENNIAL),
        descEn = descEn,
        descBn = descBn,
        descHi = descHi,
        climate = if (isSucculent) "Bright arid-tolerant or cool hills" else "Tropical/Subtropical Plains & Hills",
        temp = if (isSucculent) "10°C – 36°C" else "14°C – 36°C",
        soil = if (isSucculent) "Gritty porous cactus mix: pumice/cinder, river sand & compost (50:30:20)" else "Organic rich loamy potting mix",
        sunlight = sunlight,
        watering = watering,
        fertilizer = if (isSucculent) "Light low-nitrogen cactus feed in spring" else "Organic vermicompost monthly — avoid harsh chemical sprays on edible herbs",
        propagation = if (isSucculent) "Leaf-pulling calloused cuttings & offsets" else "Seeds, runners & soft stem cuttings",
        seasonLabel = if (isSucculent) "Year-round foliage / Spring–Winter blooms" else "Harvest fresh leaves most of year",
        growthSize = "6 inches – 3 ft",
        indoorOutdoor = if (isSucculent) "Sunny windowsill, balcony & rockery" else "Kitchen balcony, terrace & herbal bed",
        plainsHill = "Both Plains & Hills",
        pestsDiseases = if (isSucculent) "Mealybugs & root rot from overwatering" else "Aphids, caterpillars (treat only with neem/organic soap)",
        careEn = if (isSucculent) "Wait until pot dries out 100% before watering again." else "Harvest top shoots gently to encourage bushy fresh leaf growth; avoid chemical pesticides.",
        careBn = if (isSucculent) "টবের মাটি সম্পূর্ণ শুকালে তবেই জল দিন; কড়া বর্ষায় শেডে রাখুন।" else "কচি ডগা সংগ্রহ করলে গাছ ঝোপালো হয়; ভোজ্য পাতায় রাসায়নিক বিষ দেবেন না।",
        careHi = if (isSucculent) "मिट्टी पूरी तरह सूखने के बाद ही पानी दें और तेज़ बारिश से बचाएँ।" else "ताज़ी पत्तियों पर रासायनिक कीटनाशक न छिड़कें; हल्की कटाई से नई शाखाएँ आती हैं।",
        isRare = isRare
    )
}
