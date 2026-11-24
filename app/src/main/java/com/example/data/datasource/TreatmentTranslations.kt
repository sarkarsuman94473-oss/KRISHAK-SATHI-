package com.example.data.datasource

import com.example.data.model.AppLanguage
import com.example.data.model.MedicineSellerInfo
import com.example.data.model.VerifiedTreatment

data class TreatmentLocale(
    val cropNameHi: String,
    val possibleCauseHi: String,
    val possibleCauseEn: String,
    val firstActionHi: String,
    val firstActionEn: String,
    val biologicalOptionHi: String,
    val biologicalOptionEn: String,
    val growthStageHi: String,
    val growthStageEn: String,
    val dosePerLitreHi: String,
    val dosePerLitreEn: String,
    val safetyHi: String,
    val safetyEn: String,
    val productTypeHi: String,
    val productTypeEn: String
)

object TreatmentTranslations {

    val treatmentLocales: Map<String, TreatmentLocale> = mapOf(
        "tea_red_spider_mite" to TreatmentLocale(
            cropNameHi = "चाय",
            possibleCauseHi = "शुष्क सूखे का मौसम और पत्तियों पर अधिक धूल जमना।",
            possibleCauseEn = "Dry spell weather and heavy dust accumulation on foliage.",
            firstActionHi = "झाड़ियों पर पानी का छिड़काव कर धूल साफ करें और नमी बढ़ाएं।",
            firstActionEn = "Spray clean water overhead to wash off dust and increase canopy humidity.",
            biologicalOptionHi = "नीम का तेल (10,000 ppm) 500 मिली प्रति बीघा या घुलनशील गंधक 80% WP।",
            biologicalOptionEn = "Neem Oil (10,000 ppm) @ 500 ml/bigha or Wettable Sulphur 80% WP.",
            growthStageHi = "सुप्तावस्था के बाद नई कोमल कलियां निकलते समय",
            growthStageEn = "During active bud break and tender flush emergence",
            dosePerLitreHi = "0.8 - 1.0 मिली प्रति लीटर पानी में",
            dosePerLitreEn = "0.8 - 1.0 ml per litre water",
            safetyHi = "छिड़काव करते समय मास्क व दस्ताने पहनें। हवा की विपरीत दिशा में छिड़काव न करें। तुड़ाई से 7 दिन पहले रसायन बंद करें।",
            safetyEn = "Wear face mask and gloves while spraying. Never spray against the wind. Stop chemical spray 7 days before leaf plucking.",
            productTypeHi = "मकड़ीनाशक (Acaricide)",
            productTypeEn = "Acaricide"
        ),
        "tea_mosquito_bug" to TreatmentLocale(
            cropNameHi = "चाय",
            possibleCauseHi = "बादल भरा नम मौसम और चाय झाड़ियों के भीतर अधिक आर्द्रता।",
            possibleCauseEn = "Overcast humid weather and high moisture inside dense tea bushes.",
            firstActionHi = "संक्रमित कोमल टहनियां सावधानी से हाथ से काटकर हटाएं व हल्की छंटाई करें।",
            firstActionEn = "Hand-pluck affected tender shoots carefully and perform light skiffing/pruning.",
            biologicalOptionHi = "ब्यूवेरिया बैसियाना (Beauveria bassiana 1% WP) 5 ग्राम प्रति लीटर।",
            biologicalOptionEn = "Beauveria bassiana 1% WP @ 5 g per litre water.",
            growthStageHi = "नई कोमल पत्तियां निकलने के तुरंत बाद",
            growthStageEn = "Soon after new tender flush emergence",
            dosePerLitreHi = "0.25 ग्राम प्रति लीटर पानी में",
            dosePerLitreEn = "0.25 g per litre water",
            safetyHi = "सुबह या शाम शांत मौसम में छिड़काव करें। मधुमक्खियों के समय से बचें।",
            safetyEn = "Spray in calm morning or evening hours. Avoid bee foraging hours.",
            productTypeHi = "कीटनाशक (Insecticide)",
            productTypeEn = "Insecticide"
        ),
        "cabbage_caterpillar_dbm" to TreatmentLocale(
            cropNameHi = "पत्तागोभी",
            possibleCauseHi = "गर्म व शुष्क मौसम में पतंगों का तेजी से प्रजनन।",
            possibleCauseEn = "Rapid moth multiplication in warm and dry weather.",
            firstActionHi = "प्रति बीघा 4-5 फेरोमोन ट्रैप लगाएं और प्रभावित पत्ते नष्ट करें।",
            firstActionEn = "Install 4-5 pheromone traps per bigha and destroy infested outer leaves.",
            biologicalOptionHi = "बैसिलस थुरिंजिएंसिस (Bt kurstaki) 2 ग्राम/लीटर या नीम बीज अर्क (NSKE 5%)।",
            biologicalOptionEn = "Bacillus thuringiensis (Bt kurstaki) @ 2 g/L or Neem Seed Kernel Extract (NSKE 5%).",
            growthStageHi = "गोभी का सिर बंधने की शुरुआती अवस्था",
            growthStageEn = "Early head formation stage",
            dosePerLitreHi = "0.3 - 0.4 मिली प्रति लीटर पानी में",
            dosePerLitreEn = "0.3 - 0.4 ml per litre water",
            safetyHi = "तुड़ाई से 3 दिन पहले प्रयोग बंद करें। एक ही कीटनाशक बार-बार न डालें, स्पिनोसैड के साथ अदला-बदली करें।",
            safetyEn = "Stop application 3 days before harvest. Avoid repeat sprays of the same group; rotate with Spinosad.",
            productTypeHi = "कीटनाशक (Insecticide)",
            productTypeEn = "Insecticide"
        ),
        "cauliflower_boron_whip" to TreatmentLocale(
            cropNameHi = "फूलगोभी",
            possibleCauseHi = "मिट्टी में बोरॉन की कमी और अत्यधिक नाइट्रोजन खाद का प्रयोग।",
            possibleCauseEn = "Boron deficiency in soil compounded by excess nitrogen fertilization.",
            firstActionHi = "संतुलित सिंचाई करें और नाइट्रोजन खाद की मात्रा नियंत्रित रखें।",
            firstActionEn = "Maintain balanced irrigation and avoid excess nitrogen doses.",
            biologicalOptionHi = "जैविक कम्पोस्ट व राख के साथ सूक्ष्म पोषक तत्व दें।",
            biologicalOptionEn = "Apply organic compost and wood ash along with balanced micronutrients.",
            growthStageHi = "रोपाई के 25-30 दिन बाद और फूल बनने से पहले",
            growthStageEn = "25-30 days after transplanting and pre-curd stage",
            dosePerLitreHi = "1.5 - 2.0 ग्राम प्रति लीटर पानी में",
            dosePerLitreEn = "1.5 - 2.0 g per litre water",
            safetyHi = "धूप कम होने पर शाम के समय छिड़काव करें। अनुशंसित मात्रा से अधिक न डालें।",
            safetyEn = "Spray in the afternoon/evening when heat subsides. Do not exceed recommended dosage.",
            productTypeHi = "सूक्ष्म पोषक तत्व (Micronutrient)",
            productTypeEn = "Micronutrient Fertilizer"
        ),
        "tomato_late_blight" to TreatmentLocale(
            cropNameHi = "टमाटर",
            possibleCauseHi = "लगातार बादल, कोहरा और ठंडा तापमान (15-20°C)।",
            possibleCauseEn = "Persistent overcast foggy weather and cool temperatures (15-20°C).",
            firstActionHi = "संक्रमित पौधे या पत्ते तोड़कर दूर जमीन में गाड़ दें और ऊपर से पानी छिड़कना बंद करें।",
            firstActionEn = "Uproot/remove infected plants and bury away from field; stop overhead watering.",
            biologicalOptionHi = "ट्राइकोडर्मा विरिडी (1% WP) 5 ग्राम प्रति लीटर पानी में छिड़काव।",
            biologicalOptionEn = "Trichoderma viride 1% WP @ 5 g per litre water spray.",
            growthStageHi = "कोहरा शुरू होते ही बचाव हेतु या पहले लक्षण दिखते ही",
            growthStageEn = "Preventively at onset of foggy weather or at first blight spot",
            dosePerLitreHi = "1.5 - 2.0 ग्राम प्रति लीटर पानी में",
            dosePerLitreEn = "1.5 - 2.0 g per litre water",
            safetyHi = "बचाव के लिए मैन्कोजेब का प्रयोग किया जा सकता है। लक्षण बढ़ने पर सही मात्रा व सुरक्षा उपकरण (PPE) के साथ छिड़कें।",
            safetyEn = "Mancozeb may be used preventively. At symptom onset spray recommended dose wearing full PPE.",
            productTypeHi = "फफूंदनाशक (Fungicide)",
            productTypeEn = "Fungicide"
        ),
        "tomato_fruit_borer" to TreatmentLocale(
            cropNameHi = "टमाटर",
            possibleCauseHi = "गर्म मौसम और खेत में गेंदे की ट्रैप फसल न लगाना।",
            possibleCauseEn = "Warm weather and absence of marigold trap crops in the field.",
            firstActionHi = "प्रति बीघा 5 हेलिल्यूर फेरोमोन ट्रैप लगाएं और कीड़े लगे फल इकट्ठा कर नष्ट करें।",
            firstActionEn = "Install 5 Helilure pheromone traps per bigha and destroy bored fruits.",
            biologicalOptionHi = "HaNPV 250 LE @ 1 मिली/लीटर या नीम तेल (10,000 ppm) 2 मिली/लीटर।",
            biologicalOptionEn = "HaNPV 250 LE @ 1 ml/L or Neem oil (10,000 ppm) @ 2 ml/L.",
            growthStageHi = "फूल आने से लेकर छोटे फल बनते समय",
            growthStageEn = "From flowering to early fruit set",
            dosePerLitreHi = "0.4 - 0.5 ग्राम प्रति लीटर पानी में",
            dosePerLitreEn = "0.4 - 0.5 g per litre water",
            safetyHi = "तुड़ाई से 3 दिन पहले अंतिम छिड़काव करें। बच्चों की पहुंच से दूर रखें।",
            safetyEn = "Complete final spray at least 3 days before harvest. Keep away from children.",
            productTypeHi = "कीटनाशक (Insecticide)",
            productTypeEn = "Insecticide"
        ),
        "chilli_leaf_curl_thrips" to TreatmentLocale(
            cropNameHi = "मिर्च",
            possibleCauseHi = "शुष्क मौसम और खेत में खरपतवारों की अधिकता।",
            possibleCauseEn = "Dry weather and unmanaged weeds harboring pests.",
            firstActionHi = "प्रति बीघा 10 पीले और नीले चिपचिपे जाल (Sticky Traps) लगाएं।",
            firstActionEn = "Hang 10 Yellow and Blue Sticky Traps per bigha across the field.",
            biologicalOptionHi = "5 मिली नीम का तेल प्रति लीटर पानी में थोड़ा शैम्पू मिलाकर हर 7 दिन पर छिड़कें।",
            biologicalOptionEn = "Spray Neem oil @ 5 ml/L mixed with mild emulsifier every 7 days.",
            growthStageHi = "पौधे की नई शाखाएं व पत्ते बढ़ते समय",
            growthStageEn = "Active vegetative shoot and leaf growth stage",
            dosePerLitreHi = "1.2 ग्राम प्रति लीटर पानी में",
            dosePerLitreEn = "1.2 g per litre water",
            safetyHi = "फूल खिलते समय तेज धूप में छिड़काव न करें। सुरक्षात्मक चश्मा व मास्क पहनें।",
            safetyEn = "Do not spray in harsh noon sun during flowering. Wear protective goggles and mask.",
            productTypeHi = "कीटनाशक / मकड़ीनाशक (Insecticide / Acaricide)",
            productTypeEn = "Insecticide / Acaricide"
        ),
        "chilli_anthracnose_dieback" to TreatmentLocale(
            cropNameHi = "मिर्च",
            possibleCauseHi = "अधिक तापमान और बारिश के बाद लगातार नमी।",
            possibleCauseEn = "High temperatures accompanied by high post-rain humidity.",
            firstActionHi = "सूखती हुई टहनियां और सड़े फल तोड़कर जला दें।",
            firstActionEn = "Pick and burn affected twigs and rotten chilli pods.",
            biologicalOptionHi = "ट्राइकोडर्मा विरिडी 5 ग्राम/लीटर पानी में छिड़काव करें।",
            biologicalOptionEn = "Spray Trichoderma viride @ 5 g/litre water.",
            growthStageHi = "फूल आने से फल पकने तक",
            growthStageEn = "From flowering through fruit ripening",
            dosePerLitreHi = "1.0 मिली प्रति लीटर पानी में",
            dosePerLitreEn = "1.0 ml per litre water",
            safetyHi = "ओस सूखने के बाद सुबह 9-10 बजे या शाम को शांत मौसम में छिड़काव करें।",
            safetyEn = "Spray after morning dew dries (9-10 AM) or in calm evening weather.",
            productTypeHi = "फफूंदनाशक (Fungicide)",
            productTypeEn = "Fungicide"
        ),
        "brinjal_shoot_fruit_borer" to TreatmentLocale(
            cropNameHi = "बैंगन",
            possibleCauseHi = "नियमित निगरानी न करना और सूखी टहनियां समय पर न काटना।",
            possibleCauseEn = "Lack of regular scouting and failure to prune wilted shoots promptly.",
            firstActionHi = "हर सप्ताह सुबह सूखी हुई कोंपलें कैंची से काटकर मिट्टी में दबा दें।",
            firstActionEn = "Weekly clip wilted terminal shoots with shears in the morning and bury them.",
            biologicalOptionHi = "प्रति बीघा 8 ल्यूसिन-ल्यूर फेरोमोन ट्रैप और ट्राइकोग्रामा कार्ड लगाएं।",
            biologicalOptionEn = "Install 8 Leucin-lure pheromone traps/bigha and release Trichogramma egg cards.",
            growthStageHi = "पहला फूल और फल लगते समय",
            growthStageEn = "At first flowering and fruiting stage",
            dosePerLitreHi = "0.4 मिली प्रति लीटर पानी में",
            dosePerLitreEn = "0.4 ml per litre water",
            safetyHi = "तुड़ाई से 3 दिन पहले छिड़काव बंद करें। एक मौसम में 2 से अधिक बार न छिड़कें।",
            safetyEn = "Stop spray 3 days before harvest. Do not exceed 2 applications per season.",
            productTypeHi = "कीटनाशक (Insecticide)",
            productTypeEn = "Insecticide"
        ),
        "okra_ymv_whitefly" to TreatmentLocale(
            cropNameHi = "भिंडी",
            possibleCauseHi = "सफेद मक्खी (Bemisia tabaci) द्वारा वायरस का फैलाव।",
            possibleCauseEn = "Viral transmission via whitefly vector (Bemisia tabaci).",
            firstActionHi = "प्रति बीघा 10 पीले चिपचिपे जाल (Yellow Sticky Traps) लगाएं।",
            firstActionEn = "Install 10 Yellow Sticky Traps per bigha early in the season.",
            biologicalOptionHi = "नीम का तेल 5 मिली प्रति लीटर पानी में छिड़काव करें।",
            biologicalOptionEn = "Spray Neem oil @ 5 ml per litre water.",
            growthStageHi = "बुवाई के 15-20 दिन बाद सफेद मक्खी दिखते ही",
            growthStageEn = "15-20 days after sowing at first whitefly sighting",
            dosePerLitreHi = "0.2 ग्राम प्रति लीटर पानी में",
            dosePerLitreEn = "0.2 g per litre water",
            safetyHi = "सुबह या शाम को छिड़काव करें। मास्क व चश्मा अवश्य पहनें।",
            safetyEn = "Spray in morning or evening hours. Always wear mask and eye protection.",
            productTypeHi = "कीटनाशक (Insecticide)",
            productTypeEn = "Insecticide"
        ),
        "potato_blight" to TreatmentLocale(
            cropNameHi = "आलू",
            possibleCauseHi = "फाइटोफ्थोरा फफूंद और सर्दियों का लगातार कोहरा।",
            possibleCauseEn = "Phytophthora fungus spread under persistent winter fog and high humidity.",
            firstActionHi = "मिट्टी की नमी अस्थायी रूप से कम करें और नाइट्रोजन खाद बंद करें।",
            firstActionEn = "Temporarily reduce irrigation moisture and halt nitrogen fertilizer.",
            biologicalOptionHi = "स्यूडोमोनास फ्लोरेसेंस या ट्राइकोडर्मा का छिड़काव।",
            biologicalOptionEn = "Preventive spray of Pseudomonas fluorescens or Trichoderma.",
            growthStageHi = "कोहरे भरी सुबह में कोहरा छंटने के बाद",
            growthStageEn = "During winter fog periods once morning mist clears",
            dosePerLitreHi = "2.5 - 3.0 ग्राम प्रति लीटर पानी में",
            dosePerLitreEn = "2.5 - 3.0 g per litre water",
            safetyHi = "कोहरा छंटने के बाद धूप निकलने पर छिड़कें। दवा का घोल बच्चों से दूर रखें।",
            safetyEn = "Spray only after morning fog lifts into clear sun. Keep containers away from children.",
            productTypeHi = "फफूंदनाशक (Fungicide)",
            productTypeEn = "Fungicide"
        ),
        "rice_stem_borer" to TreatmentLocale(
            cropNameHi = "धान",
            possibleCauseHi = "अधिक नाइट्रोजन खाद डालना और खेत में लगातार पानी भरा रहना।",
            possibleCauseEn = "Excess nitrogen fertilization and continuous standing water in paddy field.",
            firstActionHi = "अंडों वाले पत्तों के सिरे तोड़कर नष्ट करें। प्रति बीघा 3-4 ट्राइकोग्रामा कार्ड लगाएं।",
            firstActionEn = "Clip egg-mass bearing leaf tips and release 3-4 Trichogramma cards per bigha.",
            biologicalOptionHi = "नीम तेल (10,000 ppm) 2 मिली/लीटर या ट्राइकोग्रामा जापोनिकम का उपयोग।",
            biologicalOptionEn = "Neem oil (10,000 ppm) @ 2 ml/L or Trichogramma japonicum.",
            growthStageHi = "कल्ले फूटने से लेकर बाली निकलने से पहले तक",
            growthStageEn = "From active tillering through panicle initiation stage",
            dosePerLitreHi = "0.3 मिली प्रति लीटर पानी में (स्प्रे करने पर)",
            dosePerLitreEn = "0.3 ml per litre water (when spraying SC)",
            safetyHi = "दानेदार दवा डालते समय खेत में 2-3 इंच पानी होना चाहिए। स्प्रे करते समय सुबह फूल आने के समय से बचें।",
            safetyEn = "Maintain 2-3 inches of standing water when broadcasting granules. Avoid morning bloom hours if spraying.",
            productTypeHi = "कीटनाशक (Insecticide)",
            productTypeEn = "Insecticide"
        ),
        "rice_blast" to TreatmentLocale(
            cropNameHi = "धान",
            possibleCauseHi = "रात में ओस, बादल भरा मौसम (25-28°C) और अधिक यूरिया।",
            possibleCauseEn = "Night dew, cloudy damp weather (25-28°C), and excess urea.",
            firstActionHi = "यूरिया का प्रयोग तुरंत रोकें और पोटाश खाद की मात्रा बढ़ाएं।",
            firstActionEn = "Stop Urea immediately and supplement Potassium in the field.",
            biologicalOptionHi = "स्यूडोमोनास फ्लोरेसेंस 5 ग्राम प्रति लीटर पानी में छिड़काव।",
            biologicalOptionEn = "Spray Pseudomonas fluorescens @ 5 g per litre water.",
            growthStageHi = "कल्ले निकलते या बाली फूटते समय",
            growthStageEn = "Active tillering or booting / panicle emergence stage",
            dosePerLitreHi = "0.6 ग्राम प्रति लीटर पानी में",
            dosePerLitreEn = "0.6 g per litre water",
            safetyHi = "ओस सूखने के बाद धूप वाले दिन छिड़काव करें। चेहरे पर मास्क लगाएं।",
            safetyEn = "Spray on a sunny day after dew evaporates. Wear face mask.",
            productTypeHi = "फफूंदनाशक (Fungicide)",
            productTypeEn = "Fungicide"
        ),
        "wheat_rust" to TreatmentLocale(
            cropNameHi = "गेहूं",
            possibleCauseHi = "सर्दियों की ठंड, अधिक नमी व कोहरा भरा मौसम।",
            possibleCauseEn = "Cool winter temperatures, heavy dew, and humid foggy weather.",
            firstActionHi = "शुरुआती संक्रमित पत्तियां तोड़कर नष्ट करें और खेत में हवा संचार रखें।",
            firstActionEn = "Remove early rust-infected leaves and maintain proper field ventilation.",
            biologicalOptionHi = "किण्वित गोमूत्र व नीम खली अर्क का छिड़काव।",
            biologicalOptionEn = "Spray fermented cow-urine and neem-cake extract.",
            growthStageHi = "बाली आने से पहले या रतुआ के पहले लक्षण दिखने पर",
            growthStageEn = "Before earhead emergence or at first rust pustule appearance",
            dosePerLitreHi = "1.0 मिली प्रति लीटर पानी में",
            dosePerLitreEn = "1.0 ml per litre water",
            safetyHi = "रोग दिखते ही 1 मिली/लीटर पानी की दर से पूरे पौधे पर समान छिड़काव करें।",
            safetyEn = "At very first sign, mix 1 ml/litre and spray foliage evenly.",
            productTypeHi = "फफूंदनाशक (Fungicide)",
            productTypeEn = "Fungicide"
        ),
        "maize_faw" to TreatmentLocale(
            cropNameHi = "मक्का",
            possibleCauseHi = "शुष्क मौसम और देर से बुवाई।",
            possibleCauseEn = "Dry warm weather and delayed sowing.",
            firstActionHi = "मक्का के पौधे की बीच की पोंगली (Whorl) में सूखी रेत व लकड़ी की राख मिलाएं जिससे सुंडी दम तोड़ दे।",
            firstActionEn = "Apply dry sand mixed with wood ash into central leaf whorls to suffocate larvae.",
            biologicalOptionHi = "मेटारिज़ियम एनिसोप्ली 5 ग्राम/लीटर या Bt kurstaki 2 ग्राम/लीटर।",
            biologicalOptionEn = "Metarhizium anisopliae @ 5 g/L or Bt kurstaki @ 2 g/L.",
            growthStageHi = "पौधे घुटने जितनी ऊंचाई पर (V4-V8 अवस्था)",
            growthStageEn = "Knee-high stage (V4-V8 whorl stage)",
            dosePerLitreHi = "0.5 मिली प्रति लीटर पानी में",
            dosePerLitreEn = "0.5 ml per litre water",
            safetyHi = "स्प्रेयर का नोज़ल सीधे मक्का की बीच की पोंगली में रखकर अच्छी तरह भिगोएं।",
            safetyEn = "Direct the sprayer nozzle straight into the central maize whorl for thorough penetration.",
            productTypeHi = "कीटनाशक (Insecticide)",
            productTypeEn = "Insecticide"
        ),
        "mustard_aphid" to TreatmentLocale(
            cropNameHi = "सरसों",
            possibleCauseHi = "बादल भरा आकाश और हल्की धुंध के साथ गुनगुना तापमान।",
            possibleCauseEn = "Cloudy skies and mild humid winter weather.",
            firstActionHi = "खेत के चारों ओर प्रति बीघा 10 पीले चिपचिपे जाल लगाएं।",
            firstActionEn = "Place 10 Yellow Sticky Traps per bigha around field borders.",
            biologicalOptionHi = "नीम तेल 5 मिली/लीटर पानी में छिड़कें। लेडीबर्ड बीटल का संरक्षण करें।",
            biologicalOptionEn = "Spray Neem oil @ 5 ml/L. Conserve beneficial Ladybird beetles.",
            growthStageHi = "फूल आने से पहले या फली बनते समय",
            growthStageEn = "Pre-flowering or early pod formation stage",
            dosePerLitreHi = "1.3 मिली प्रति लीटर पानी में",
            dosePerLitreEn = "1.3 ml per litre water",
            safetyHi = "सरसों पर मधुमक्खियां आने के कारण सुबह 10 से दोपहर 3 बजे तक छिड़काव पूरी तरह वर्जित है। शाम को छिड़कें।",
            safetyEn = "Avoid spraying between 10 AM and 3 PM to protect pollinating honeybees. Spray in late afternoon.",
            productTypeHi = "कीटनाशक (Insecticide)",
            productTypeEn = "Insecticide"
        ),
        "groundnut_tikka" to TreatmentLocale(
            cropNameHi = "मूंगफली",
            possibleCauseHi = "अधिक तापमान और गीली नम मिट्टी।",
            possibleCauseEn = "Warm weather combined with humid wet canopy/soil.",
            firstActionHi = "खेत को साफ व खरपतवार मुक्त रखें।",
            firstActionEn = "Keep field clean and weed-free.",
            biologicalOptionHi = "ट्राइकोडर्मा से बीजोपचार (10 ग्राम/किग्रा बीज)।",
            biologicalOptionEn = "Seed treatment with Trichoderma @ 10 g/kg seed.",
            growthStageHi = "पौधे की उम्र 35-40 दिन होने पर",
            growthStageEn = "At 35-40 days crop age",
            dosePerLitreHi = "1.0 मिली प्रति लीटर पानी में",
            dosePerLitreEn = "1.0 ml per litre water",
            safetyHi = "पत्तियों की दोनों सतहों पर अच्छी तरह छिड़काव करें। व्यक्तिगत सुरक्षा अपनाएं।",
            safetyEn = "Cover both sides of foliage thoroughly while wearing protective gear.",
            productTypeHi = "फफूंदनाशक (Fungicide)",
            productTypeEn = "Fungicide"
        ),
        "pulse_pod_borer" to TreatmentLocale(
            cropNameHi = "चना",
            possibleCauseHi = "बादल भरा मौसम और वसंत की बढ़ती गर्मी।",
            possibleCauseEn = "Overcast weather and warming early spring temperatures.",
            firstActionHi = "प्रति बीघा 5-6 अंग्रेजी T आकार के पक्षी बैठने के डंडे (Bird Perches) व 5 फेरोमोन ट्रैप लगाएं।",
            firstActionEn = "Install 5-6 T-shaped bird perches and 5 pheromone traps per bigha.",
            biologicalOptionHi = "NPV या नीम का तेल 5 मिली/लीटर पानी में।",
            biologicalOptionEn = "HaNPV bio-agent or Neem oil @ 5 ml/litre.",
            growthStageHi = "फूल आने के बाद और छोटी फलियां बनते समय",
            growthStageEn = "After flowering and early tender pod formation",
            dosePerLitreHi = "0.7 मिली प्रति लीटर पानी में",
            dosePerLitreEn = "0.7 ml per litre water",
            safetyHi = "मधुमक्खियों के बचाव हेतु फूल अवस्था में सुबह छिड़काव न करें; शाम को छिड़कें।",
            safetyEn = "Do not spray in morning hours during bloom to protect bees; spray late afternoon.",
            productTypeHi = "कीटनाशक (Insecticide)",
            productTypeEn = "Insecticide"
        ),
        "nursery_damping_off" to TreatmentLocale(
            cropNameHi = "नर्सरी पौधे",
            possibleCauseHi = "नर्सरी क्यारी में पानी जमा होना और बहुत घनी बुवाई।",
            possibleCauseEn = "Water stagnation in nursery beds and overly dense seed sowing.",
            firstActionHi = "नर्सरी में जल निकासी सुधारें, मिट्टी धूप में सुखाएं व घने पौधे विरले करें।",
            firstActionEn = "Ensure nursery drainage, solarize beds, and thin overcrowded seedlings.",
            biologicalOptionHi = "नर्सरी मिट्टी में गोबर खाद के साथ ट्राइकोडर्मा हर्ज़ियानम (50 ग्राम/वर्ग मीटर) मिलाएं।",
            biologicalOptionEn = "Incorporate Trichoderma harzianum (50 g/sq.m) with FYM into nursery soil.",
            growthStageHi = "नर्सरी तैयारी के समय व अंकुरण के 10-15 दिन बाद",
            growthStageEn = "During nursery preparation and 10-15 days post emergence",
            dosePerLitreHi = "2.5 - 3.0 ग्राम प्रति लीटर पानी से मिट्टी ड्रेंचिंग",
            dosePerLitreEn = "2.5 - 3.0 g per litre soil drench",
            safetyHi = "पौधों के तने के पास मिट्टी अच्छी तरह भिगोकर हजारे से डालें।",
            safetyEn = "Drench soil uniformly around the seedling base using a rose-can.",
            productTypeHi = "फफूंदनाशक (Fungicide)",
            productTypeEn = "Fungicide"
        ),
        "sugarcane_red_rot" to TreatmentLocale(
            cropNameHi = "गन्ना",
            possibleCauseHi = "रोगग्रस्त गन्ने के टुकड़ों की बुवाई और खेत में जलभराव।",
            possibleCauseEn = "Planting infected seed setts and poor drainage.",
            firstActionHi = "रोगमुक्त बीज चुनें व गर्म पानी (50°C पर 2 घंटे) से उपचारित करें।",
            firstActionEn = "Select healthy seed setts and treat in hot water (50°C for 2 hours).",
            biologicalOptionHi = "बुवाई से पहले बीज टुकड़ों को ट्राइकोडर्मा घोल में 30 मिनट डुबोएं।",
            biologicalOptionEn = "Dip seed setts in Trichoderma suspension for 30 minutes prior to planting.",
            growthStageHi = "गन्ने के टुकड़ों की बुवाई से पहले",
            growthStageEn = "Pre-planting sett treatment",
            dosePerLitreHi = "1.0 ग्राम प्रति लीटर पानी में बीज उपचार",
            dosePerLitreEn = "1.0 g per litre water sett dip",
            safetyHi = "बीज टुकड़ों को घोल में उपचारित करने के बाद छाया में सुखाकर बुवाई करें।",
            safetyEn = "Dry treated setts in shade briefly before furrow planting.",
            productTypeHi = "फफूंदनाशक (Fungicide)",
            productTypeEn = "Fungicide"
        ),
        "jute_semilooper" to TreatmentLocale(
            cropNameHi = "जूट",
            possibleCauseHi = "बादल भरा आसमान और नम गर्म मौसम।",
            possibleCauseEn = "Overcast humid weather during active jute growth.",
            firstActionHi = "खेत में बांस की टहनियां गाड़ें ताकि कीटभक्षी पक्षी बैठकर सुंडियां खा सकें।",
            firstActionEn = "Place bamboo stakes in the field as perches for insect-eating birds.",
            biologicalOptionHi = "नीम का तेल (10,000 ppm) 2 मिली/लीटर या Bt 2 ग्राम/लीटर।",
            biologicalOptionEn = "Neem oil (10,000 ppm) @ 2 ml/L or Bt @ 2 g/L.",
            growthStageHi = "पौधे की ऊंचाई 2-4 फीट होने पर",
            growthStageEn = "When crop height is 2-4 feet",
            dosePerLitreHi = "1.0 मिली प्रति लीटर पानी में",
            dosePerLitreEn = "1.0 ml per litre water",
            safetyHi = "हवा की विपरीत दिशा में छिड़काव न करें। काम के बाद साबुन से हाथ-मुंह अच्छी तरह धोएं।",
            safetyEn = "Do not spray against wind. Wash hands and face thoroughly with soap afterwards.",
            productTypeHi = "कीटनाशक (Insecticide)",
            productTypeEn = "Insecticide"
        ),
        "banned_monocrotophos_alert" to TreatmentLocale(
            cropNameHi = "प्रतिबंधित उत्पाद चेतावनी",
            possibleCauseHi = "अवैध या अनधिकृत विक्रेता द्वारा गलत सलाह।",
            possibleCauseEn = "Recommended illegally by unverified or unauthorized dealers.",
            firstActionHi = "सब्जियों या फलों पर इस जहर का प्रयोग कभी न करें। सुरक्षित नीम तेल या अनुशंसित विकल्प अपनाएं।",
            firstActionEn = "Never use on vegetables or fruits. Switch to Neem oil or safe CIBRC-approved alternatives.",
            biologicalOptionHi = "नीम तेल, ट्राइकोडर्मा और फेरोमोन ट्रैप।",
            biologicalOptionEn = "Neem oil, Trichoderma, and pheromone traps.",
            growthStageHi = "पूर्णतः प्रतिबंधित (BANNED)",
            growthStageEn = "PROHIBITED",
            dosePerLitreHi = "प्रयोग पूर्णतः प्रतिबंधित",
            dosePerLitreEn = "Usage strictly prohibited",
            safetyHi = "खतरे की चेतावनी: भारत में सब्जियों, पान व फलों में यह कीटनाशक कानूनी रूप से पूर्णतः प्रतिबंधित है।",
            safetyEn = "DANGER ALERT: Legally banned across India on vegetables, fruits, and edible crops.",
            productTypeHi = "कीटनाशक (प्रतिबंधित)",
            productTypeEn = "Insecticide (BANNED)"
        )
    )

    fun cleanParenEnglish(raw: String, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI) return raw
        val cleaned = raw
            .replace("(নিষিদ্ধ)", if (lang == AppLanguage.HINDI) "(प्रतिबंधित)" else "(BANNED)")
            .replace("(ওবেরন)", "")
            .replace("(একতারা)", "")
            .replace("(কোরাজেন)", "")
            .replace("(কোরাজন)", "")
            .replace("(এক্রোব্যাট)", "")
            .replace("(প্রোক্লেম)", "")
            .replace("(পেগাসাস)", "")
            .replace("(অ্যামিস্টার টপ)", "")
            .replace("(কারজেট এম৮)", "")
            .replace("(ফারটেরা)", "")
            .replace("(বীম)", "")
            .replace("(টিল্ট)", "")
            .replace("(ডেলিগেট)", "")
            .replace("(রোগোর)", "")
            .replace("(ফলিকুর)", "")
            .replace("(অ্যাভান্ট)", "")
            .replace("(ব্লিটক্স)", "")
            .replace("(ব্যাভিস্টিন)", "")
            .replace("(কারাতে)", "")
            .replace("(সলুবোর)", "Solubor")
            .replace("সলুবোর", "Solubor")
            .replace("(ফেম)", "(Fame)")
            .replace("(প্রাইড)", "")
            .replace("/ মানিক", "/ Manik")
            .replace("/ ব্লু কপার", "/ Blue Copper")
            .replace("/ বাণ", "/ Baan")
            .replace("/ বাম্পার", "/ Bumper")
            .replace("নুভাক্রন", "Nuvacron")
            .replace("(কপার অক্সিক্লোরাইড)", "(Copper Oxychloride)")
            .replace("gm (দানা ১০ কেজি/হেক্টর বা ১৫০ মিলি স্প্রে)", if (lang == AppLanguage.HINDI) "ग्राम (दाने 10 किग्रा/हे. या 150 मिली स्प्रे)" else "gm (Granule 10 kg/ha or 150 ml spray)")
            .replace("(দানা ১০ কেজি/হেক্টর বা ১৫০ মিলি স্প্রে)", "(Granule 10 kg/ha or 150 ml spray)")
            .replace("(দানা)", "(Granule)")
            .replace("(বীজ শোধন)", "(Seed Treatment)")
            .replace(Regex("\\bgm\\b"), if (lang == AppLanguage.HINDI) "ग्राम" else "gm")
            .replace(Regex("\\bml\\b"), if (lang == AppLanguage.HINDI) "मिली" else "ml")
            .trim()
        return CropTextFormats.toAsciiDigits(cleaned).replace(Regex("\\s+"), " ")
    }

    private val cropNamesBn = mapOf(
        "Tea" to "চা (Tea)",
        "Cabbage" to "বাঁধাকপি",
        "Cauliflower" to "ফুলকপি",
        "Tomato" to "টমেটো",
        "Chilli" to "লঙ্কা",
        "Brinjal" to "বেগুন",
        "Okra" to "ঢেঁড়শ",
        "Potato" to "আলু",
        "Rice" to "ধান",
        "Wheat" to "গম",
        "Maize" to "ভুট্টা",
        "Mustard" to "সর্ষে",
        "Groundnut" to "চিনাবাদাম",
        "Chickpea" to "ছোলা",
        "Nursery Plants" to "নার্সারি চারা",
        "Sugarcane" to "আখ",
        "Jute" to "পাট",
        "BANNED PRODUCT ALERT" to "নিষিদ্ধ পণ্য সতর্কতা"
    )

    private val cropNamesHi = mapOf(
        "Tea" to "चाय",
        "Cabbage" to "पत्तागोभी",
        "Cauliflower" to "फूलगोभी",
        "Tomato" to "टमाटर",
        "Chilli" to "मिर्च",
        "Brinjal" to "बैंगन",
        "Okra" to "भिंडी",
        "Potato" to "आलू",
        "Rice" to "धान",
        "Wheat" to "गेहूं",
        "Maize" to "मक्का",
        "Mustard" to "सरसों",
        "Groundnut" to "मूंगफली",
        "Chickpea" to "चना",
        "Nursery Plants" to "नर्सरी पौधे",
        "Sugarcane" to "गन्ना",
        "Jute" to "पटसन / जूट",
        "BANNED PRODUCT ALERT" to "प्रतिबंधित उत्पाद चेतावनी"
    )

    private fun translateDynamicField(raw: String, fallbackHi: String, fallbackEn: String, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI) return raw
        if (raw.none { it in '\u0980'..'\u09FF' }) return raw
        val normalized = CropTextFormats.toAsciiDigits(raw.trim())
        val knownPhrases = when (normalized) {
            "আক্রান্ত প্রাথমিক পর্যায় থেকে পূর্ণ পর্যায়" ->
                "प्रारंभिक संक्रमण से पूर्ण लक्षण अवस्था तक" to "Early symptom stage to severe infestation stage"
            "0.5 - 1.0 মিলি/গ্রাম প্রতি লিটার জলে" ->
                "0.5 - 1.0 मिली/ग्राम प्रति लीटर पानी में" to "0.5 - 1.0 ml/gm per litre water"
            "পরিষ্কার জলে ভালোভাবে গুলে স্প্রেয়ার দিয়ে গাছের সমস্ত পাতা ভিজিয়ে স্প্রে করুন" ->
                "साफ पानी में अच्छी तरह घोलकर स्प्रेयर से पौधे के सभी पत्तों को भिगोते हुए छिड़काव करें।" to
                    "Dissolve thoroughly in clean water and spray evenly to wet both sides of all foliage."
            "ছত্রাকজনিত রোগ" ->
                "फफूंद जनित रोग (Fungal Disease)" to "Fungal Disease"
            "ব্যাকটেরিয়াজনিত রোগ" ->
                "जीवाणु जनित रोग (Bacterial Disease)" to "Bacterial Disease"
            "ভাইরাসজনিত রোগ" ->
                "विषाणु जनित रोग (Viral Disease)" to "Viral Disease"
            "জমির জল নিষ্কাশন ও নাইট্রোজেন সার কমানো" ->
                "खेत से अतिरिक्त जल निकासी और नाइट्रोजन खाद कम करना" to "Drain excess field water and reduce nitrogen application"
            "সিউডোমোনাস ফ্লুরোসেন্স স্প্রে" ->
                "स्यूडोमोनास फ्लोरेसेंस का छिड़काव" to "Spray Pseudomonas fluorescens"
            "স্প্রে করার সময় গ্লাভস ও মাস্ক ব্যবহার করুন" ->
                "छिड़काव के समय दस्ताने व मास्क का उपयोग करें" to "Wear protective gloves and face mask while spraying"
            else -> null
        }
        if (knownPhrases != null) {
            return if (lang == AppLanguage.HINDI) knownPhrases.first else knownPhrases.second
        }
        val mapped = normalized
            .replace("মিলি/গ্রাম", if (lang == AppLanguage.HINDI) "मिली/ग्राम" else "ml/gm")
            .replace("মিলি", if (lang == AppLanguage.HINDI) "मिली" else "ml")
            .replace("গ্রাম", if (lang == AppLanguage.HINDI) "ग्राम" else "gm")
            .replace("প্রতি লিটার জলে", if (lang == AppLanguage.HINDI) "प्रति लीटर पानी में" else "per litre water")
            .replace("কেজি", if (lang == AppLanguage.HINDI) "किग्रा" else "kg")
            .replace("দিন", if (lang == AppLanguage.HINDI) "दिन" else "days")
        if (mapped.none { it in '\u0980'..'\u09FF' }) return mapped
        return if (lang == AppLanguage.HINDI) fallbackHi else fallbackEn
    }

    fun getCropName(t: VerifiedTreatment, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> cropNamesBn[t.cropName] ?: t.cropName
        AppLanguage.HINDI -> treatmentLocales[t.problemId]?.cropNameHi ?: cropNamesHi[t.cropName] ?: t.cropName
        AppLanguage.ENGLISH -> t.cropName
    }

    fun getPossibleCause(t: VerifiedTreatment, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> t.possibleCauseBn
        AppLanguage.HINDI -> treatmentLocales[t.problemId]?.possibleCauseHi
            ?: translateDynamicField(t.possibleCauseBn, "फफूंद/कीट या मौसम जनित कारण", "Fungal, pest, or weather-induced condition", lang)
        AppLanguage.ENGLISH -> treatmentLocales[t.problemId]?.possibleCauseEn
            ?: translateDynamicField(t.possibleCauseBn, "फफूंद/कीट या मौसम जनित कारण", "Fungal, pest, or weather-induced condition", lang)
    }

    fun getFirstAction(t: VerifiedTreatment, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> t.firstActionBn
        AppLanguage.HINDI -> treatmentLocales[t.problemId]?.firstActionHi
            ?: translateDynamicField(t.firstActionBn, "खेत की सफाई करें और संतुलित जल प्रबंधन अपनाएं।", "Practice field hygiene and maintain proper drainage.", lang)
        AppLanguage.ENGLISH -> treatmentLocales[t.problemId]?.firstActionEn
            ?: translateDynamicField(t.firstActionBn, "खेत की सफाई करें और संतुलित जल प्रबंधन अपनाएं।", "Practice field hygiene and maintain proper drainage.", lang)
    }

    fun getBiologicalOption(t: VerifiedTreatment, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> t.biologicalOptionBn
        AppLanguage.HINDI -> treatmentLocales[t.problemId]?.biologicalOptionHi
            ?: translateDynamicField(t.biologicalOptionBn, "नीम तेल या जैविक फफूंदनाशी का छिड़काव करें।", "Apply Neem oil or approved bio-control agents.", lang)
        AppLanguage.ENGLISH -> treatmentLocales[t.problemId]?.biologicalOptionEn
            ?: translateDynamicField(t.biologicalOptionBn, "नीम तेल या जैविक फफूंदनाशी का छिड़काव करें।", "Apply Neem oil or approved bio-control agents.", lang)
    }

    fun getGrowthStage(t: VerifiedTreatment, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> t.growthStageBn
        AppLanguage.HINDI -> treatmentLocales[t.problemId]?.growthStageHi
            ?: translateDynamicField(t.growthStageBn, "प्रारंभिक संक्रमण अवस्था", "Early symptom onset stage", lang)
        AppLanguage.ENGLISH -> treatmentLocales[t.problemId]?.growthStageEn
            ?: translateDynamicField(t.growthStageBn, "प्रारंभिक संक्रमण अवस्था", "Early symptom onset stage", lang)
    }

    fun getDosePerLitre(t: VerifiedTreatment, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> t.dosePerLitreBn
        AppLanguage.HINDI -> treatmentLocales[t.problemId]?.dosePerLitreHi
            ?: translateDynamicField(t.dosePerLitreBn, "अनुशंसित लेबल मात्रा (कृषि विशेषज्ञ अनुसार)", "Recommended label dosage per litre", lang)
        AppLanguage.ENGLISH -> treatmentLocales[t.problemId]?.dosePerLitreEn
            ?: translateDynamicField(t.dosePerLitreBn, "अनुशंसित लेबल मात्रा (कृषि विशेषज्ञ अनुसार)", "Recommended label dosage per litre", lang)
    }

    fun getSafetyInstructions(t: VerifiedTreatment, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> t.safetyInstructionsBn
        AppLanguage.HINDI -> treatmentLocales[t.problemId]?.safetyHi
            ?: translateDynamicField(t.safetyInstructionsBn, "छिड़काव के समय दस्ताने व मास्क पहनें और लेबल निर्देशों का पालन करें।", "Wear protective gloves and mask while spraying; follow registered label safety rules.", lang)
        AppLanguage.ENGLISH -> treatmentLocales[t.problemId]?.safetyEn
            ?: translateDynamicField(t.safetyInstructionsBn, "छिड़काव के समय दस्ताने व मास्क पहनें और लेबल निर्देशों का पालन करें।", "Wear protective gloves and mask while spraying; follow registered label safety rules.", lang)
    }

    fun getApplicationMethod(t: VerifiedTreatment, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> t.applicationMethodBn
        AppLanguage.HINDI -> "साफ पानी में अच्छी तरह घोलकर स्प्रेयर से पौधे के सभी पत्तों को भिगोते हुए छिड़काव करें।"
        AppLanguage.ENGLISH -> "Dissolve thoroughly in clean water and spray evenly to wet both sides of all foliage."
    }

    fun getProductType(t: VerifiedTreatment, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> t.productType
        AppLanguage.HINDI -> treatmentLocales[t.problemId]?.productTypeHi ?: cleanParenEnglish(t.productType, lang)
        AppLanguage.ENGLISH -> treatmentLocales[t.problemId]?.productTypeEn ?: cleanParenEnglish(t.productType, lang)
    }

    fun getLastReviewed(t: VerifiedTreatment, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> t.lastReviewed
        AppLanguage.HINDI -> if (t.lastReviewed == "অক্টোবর ২০২৬") "अक्टूबर 2026" else CropTextFormats.toAsciiDigits(t.lastReviewed)
        AppLanguage.ENGLISH -> if (t.lastReviewed == "অক্টোবর ২০২৬") "October 2026" else CropTextFormats.toAsciiDigits(t.lastReviewed)
    }

    fun getSellerShopName(seller: MedicineSellerInfo, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> seller.shopName
        AppLanguage.HINDI -> when (seller.phone) {
            "9832045610" -> "उत्तरबंग किसान सेवा केंद्र"
            "9434012845" -> "जलपाईगुड़ी एग्रो इनपुट सेंटर"
            "9733051280" -> "मालदा कृषक बंधु खाद एवं कीटनाशक"
            else -> seller.shopName
        }
        AppLanguage.ENGLISH -> when (seller.phone) {
            "9832045610" -> "Uttarbanga Kisan Seva Kendra"
            "9434012845" -> "Jalpaiguri Agro Input Centre"
            "9733051280" -> "Malda Krishak Bandhu Agro Store"
            else -> seller.shopName
        }
    }

    fun getSellerAddress(seller: MedicineSellerInfo, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> seller.address
        AppLanguage.HINDI -> when (seller.phone) {
            "9832045610" -> "हिलकार्ट रोड, बिधान मार्केट, सिलीगुड़ी"
            "9434012845" -> "स्टेशन रोड, जलपाईगुड़ी"
            "9733051280" -> "इंग्लिशबाजार हाट, मालदा"
            else -> seller.address
        }
        AppLanguage.ENGLISH -> when (seller.phone) {
            "9832045610" -> "Hill Cart Road, Bidhan Market, Siliguri"
            "9434012845" -> "Station Road, Jalpaiguri"
            "9733051280" -> "Englishbazar Haat, Malda"
            else -> seller.address
        }
    }
}
