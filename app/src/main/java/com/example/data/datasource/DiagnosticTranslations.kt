package com.example.data.datasource

import com.example.data.model.AppLanguage
import com.example.data.model.DiagnosticQuestionOption
import com.example.data.model.DiagnosticSymptomTree
import com.example.data.model.FertilizerStagePlan
import com.example.data.model.NutrientDeficiencyInfo

data class SymptomTreeLocale(
    val titleHi: String,
    val initialQuestionHi: String,
    val initialQuestionEn: String
)

data class DiagnosticOptionLocale(
    val optionTextHi: String,
    val diagnosisHi: String,
    val diagnosisEn: String,
    val primaryCauseHi: String,
    val primaryCauseEn: String,
    val firstActionHi: String,
    val firstActionEn: String,
    val biologicalActionHi: String,
    val biologicalActionEn: String,
    val dosePerLitreHi: String,
    val dosePerLitreEn: String,
    val dosePerBighaHi: String,
    val dosePerBighaEn: String,
    val safetyPPEHi: String = "हाथों में दस्ताने और चेहरे पर मास्क पहनकर शांत मौसम में छिड़काव करें।",
    val safetyPPEEn: String = "Wear gloves and face mask, and spray in calm weather."
)

data class NutrientDeficiencyLocale(
    val nutrientNameHi: String,
    val affectedPartHi: String,
    val affectedPartEn: String,
    val symptomsHi: String,
    val symptomsEn: String,
    val solutionHi: String,
    val solutionEn: String,
    val organicRemedyHi: String,
    val organicRemedyEn: String
)

data class FertilizerStageLocale(
    val stageNameHi: String,
    val stageNameEn: String,
    val timingHi: String,
    val timingEn: String,
    val objectiveHi: String,
    val objectiveEn: String,
    val recommendedFertilizersHi: String,
    val recommendedFertilizersEn: String,
    val cautionHi: String,
    val cautionEn: String
)

object DiagnosticTranslations {

    val treeLocales: Map<String, SymptomTreeLocale> = mapOf(
        "leaf_yellowing" to SymptomTreeLocale(
            titleHi = "पत्तों का पीला पड़ना",
            initialQuestionHi = "पौधे में पीलापन कहां और कैसे शुरू हुआ है?",
            initialQuestionEn = "Where and how did yellowing start on the plant?"
        ),
        "leaf_curl" to SymptomTreeLocale(
            titleHi = "पत्तियों का मुड़ना / सिकुड़ना",
            initialQuestionHi = "पत्तियां किस दिशा में और किस तरह मुड़ रही हैं?",
            initialQuestionEn = "Which direction and how are the leaves curling?"
        ),
        "flower_issues" to SymptomTreeLocale(
            titleHi = "फूल न आना / फूल झड़ना",
            initialQuestionHi = "पौधे के फूलों में क्या समस्या दिखाई दे रही है?",
            initialQuestionEn = "What specific flowering issue is visible on the plant?"
        ),
        "fruit_issues" to SymptomTreeLocale(
            titleHi = "फल न लगना / फल गिरना व धब्बे",
            initialQuestionHi = "फलों पर कौन सा विशिष्ट लक्षण दिखाई दे रहा है?",
            initialQuestionEn = "Which specific fruit symptom are you observing?"
        ),
        "leaf_spots" to SymptomTreeLocale(
            titleHi = "काले या भूरे धब्बे (Leaf Spot)",
            initialQuestionHi = "धब्बों का आकार और स्वरूप कैसा है?",
            initialQuestionEn = "What is the shape and nature of the leaf spots?"
        ),
        "powdery_mildew" to SymptomTreeLocale(
            titleHi = "पत्तों पर सफेद चूर्ण (Powdery Mildew)",
            initialQuestionHi = "पत्ते की किस सतह पर सफेद चूर्ण दिखाई दे रहा है?",
            initialQuestionEn = "On which side of the leaf is the white powder visible?"
        ),
        "root_stem_rot" to SymptomTreeLocale(
            titleHi = "पौधा या जड़ सड़ना / मुरझाना",
            initialQuestionHi = "पौधा किस तरह सूखकर मर रहा है?",
            initialQuestionEn = "How is the plant wilting or dying?"
        )
    )

    val optionLocales: Map<String, DiagnosticOptionLocale> = mapOf(
        // leaf_yellowing
        "Lower older leaves turn yellow first" to DiagnosticOptionLocale(
            optionTextHi = "1. पौधे के नीचे के पुराने पत्ते पहले पीले पड़ रहे हैं, पौधा कमजोर है",
            diagnosisHi = "नाइट्रोजन (N) की कमी या जड़ों में अतिरिक्त पानी भरने से जड़ का दम घुटना।",
            diagnosisEn = "Nitrogen (N) deficiency or root suffocation caused by waterlogging.",
            primaryCauseHi = "मिट्टी में अत्यधिक पानी जमा होने से हवा का संचार रुकना या नाइट्रोजन खाद की कमी।",
            primaryCauseEn = "Poor soil aeration due to excess standing water or lack of nitrogen.",
            firstActionHi = "खेत का अतिरिक्त पानी निकालें। मिट्टी की हल्की गुड़ाई करके धूप व हवा लगने दें।",
            firstActionEn = "Drain excess water from the field. Loosen topsoil for aeration and sunlight.",
            biologicalActionHi = "वर्मीकम्पोस्ट या सड़ी गोबर खाद डालें। जड़ों में ट्राइकोडर्मा का प्रयोग करें।",
            biologicalActionEn = "Apply vermicompost or well-rotted FYM. Drench root zone with Trichoderma.",
            dosePerLitreHi = "5 ग्राम प्रति लीटर पानी में छिड़काव",
            dosePerLitreEn = "5 g per litre water foliar spray",
            dosePerBighaHi = "प्रति बीघा 5-7 किग्रा यूरिया की टॉप ड्रेसिंग",
            dosePerBighaEn = "5-7 kg Urea top dressing per bigha"
        ),
        "New young leaves yellow with green veins" to DiagnosticOptionLocale(
            optionTextHi = "2. नई कोमल पत्तियां पीली लेकिन नसें गहरी हरी दिख रही हैं",
            diagnosisHi = "आयरन (Fe) या सूक्ष्म पोषक तत्वों की कमी (Interveinal Chlorosis)।",
            diagnosisEn = "Iron (Fe) or micronutrient deficiency (Interveinal Chlorosis).",
            primaryCauseHi = "मिट्टी का अधिक क्षारीय pH या भारी मिट्टी में जड़ों द्वारा सूक्ष्म तत्वों का अवशोषण न हो पाना।",
            primaryCauseEn = "High soil alkalinity (pH) or impaired root uptake of micronutrients in heavy soil.",
            firstActionHi = "पौधे के पास की मिट्टी हल्की करें। जैविक कम्पोस्ट और चिलेटेड सूक्ष्म पोषक तत्व दें।",
            firstActionEn = "Loosen soil around base. Apply organic compost and chelated micronutrients.",
            biologicalActionHi = "ह्यूमिक एसिड (Humic Acid) 2 मिली प्रति लीटर पानी में घोलकर जड़ों में दें।",
            biologicalActionEn = "Apply Humic Acid @ 2 ml/litre around root zone.",
            dosePerLitreHi = "1-1.5 ग्राम प्रति लीटर पानी में छिड़काव",
            dosePerLitreEn = "1-1.5 g per litre water spray",
            dosePerBighaHi = "प्रति बीघा 200 ग्राम छिड़काव",
            dosePerBighaEn = "200 g foliar spray per bigha"
        ),
        "Leaf edges yellow and burnt brown" to DiagnosticOptionLocale(
            optionTextHi = "3. पत्तों के किनारों पर जले जैसे पीले-भूरे दाग, उपज कम",
            diagnosisHi = "पोटाश (K) या मैग्नीशियम (Mg) की कमी।",
            diagnosisEn = "Potassium (K) or Magnesium (Mg) deficiency.",
            primaryCauseHi = "बलुई मिट्टी में पोटाश बह जाना तथा अम्लीय मिट्टी में मैग्नीशियम की कमी।",
            primaryCauseEn = "Potassium leaching in sandy soils and magnesium deficiency in acidic soils.",
            firstActionHi = "पौधे की जड़ों के पास नमी बनाए रखें और पर्याप्त जैविक खाद डालें।",
            firstActionEn = "Maintain uniform root-zone moisture and apply adequate organic manure.",
            biologicalActionHi = "केले के तने की राख या लकड़ी की राख मिलाकर हल्की सिंचाई करें।",
            biologicalActionEn = "Apply wood ash or banana pseudostem ash with light irrigation.",
            dosePerLitreHi = "4-5 ग्राम प्रति लीटर पानी में छिड़काव",
            dosePerLitreEn = "4-5 g per litre water spray",
            dosePerBighaHi = "प्रति बीघा 2 किग्रा छिड़काव",
            dosePerBighaEn = "2 kg per bigha spray"
        ),
        "Yellowing with fine webbing / tiny mites underneath" to DiagnosticOptionLocale(
            optionTextHi = "4. पत्ते पीले होकर निचली सतह पर जाला या अति सूक्ष्म कीट",
            diagnosisHi = "लाल या पीली मकड़ी (Mites) और सफेद मक्खी (Whitefly) द्वारा रस चूसना।",
            diagnosisEn = "Sap sucking by red/yellow mites and whiteflies.",
            primaryCauseHi = "शुष्क व गर्म मौसम, धूल और सूखा।",
            primaryCauseEn = "Hot dry weather, dust accumulation, and drought stress.",
            firstActionHi = "पानी की तेज धार से पत्तों की निचली सतह धोएं। पीला चिपचिपा जाल (Yellow Sticky Trap) लगाएं।",
            firstActionEn = "Wash leaf undersides with strong water jet. Install Yellow Sticky Traps.",
            biologicalActionHi = "नीम का तेल (10,000 ppm) 3 मिली प्रति लीटर पानी में मिलाकर छिड़काव करें।",
            biologicalActionEn = "Spray Neem Oil (10,000 ppm) @ 3 ml per litre water.",
            dosePerLitreHi = "1 मिली प्रति लीटर पानी में छिड़काव",
            dosePerLitreEn = "1 ml per litre water spray",
            dosePerBighaHi = "प्रति बीघा 50 मिली छिड़काव",
            dosePerBighaEn = "50 ml per bigha spray",
            safetyPPEHi = "मास्क और दस्ताने पहनें। हवा की दिशा में छिड़काव करें।",
            safetyPPEEn = "Wear mask and gloves. Spray along wind direction."
        ),

        // leaf_curl
        "Leaves cup upwards like a bowl" to DiagnosticOptionLocale(
            optionTextHi = "1. पत्तों के किनारे ऊपर की ओर कटोरी की तरह मुड़ रहे हैं",
            diagnosisHi = "थ्रिप्स (Thrips) कीट द्वारा कोमल पत्तियों से रस चूसना।",
            diagnosisEn = "Thrips sucking sap from young tender leaves.",
            primaryCauseHi = "शुष्क मौसम में थ्रिप्स कीट का तेजी से फैलाव।",
            primaryCauseEn = "Rapid multiplication of thrips during dry weather.",
            firstActionHi = "खेत में प्रति बीघा 6-8 नीले चिपचिपे जाल (Blue Sticky Trap) लगाएं।",
            firstActionEn = "Install 6-8 Blue Sticky Traps per bigha in the field.",
            biologicalActionHi = "वर्टिसिलियम लेकानी (Verticillium lecanii) 5 ग्राम/लीटर या नीम तेल का छिड़काव।",
            biologicalActionEn = "Spray Verticillium lecanii @ 5 g/L or Neem oil.",
            dosePerLitreHi = "1.5 मिली प्रति लीटर पानी में छिड़काव",
            dosePerLitreEn = "1.5 ml per litre water spray",
            dosePerBighaHi = "प्रति बीघा 100 मिली छिड़काव",
            dosePerBighaEn = "100 ml per bigha spray"
        ),
        "Leaves curl downwards like inverted boat" to DiagnosticOptionLocale(
            optionTextHi = "2. पत्ते नीचे की ओर उल्टी नाव की तरह मुड़ रहे हैं और खुरदुरे हैं",
            diagnosisHi = "पीली या लाल मकड़ी (Mites) का प्रकोप।",
            diagnosisEn = "Infestation of Yellow or Red Mites.",
            primaryCauseHi = "उच्च तापमान और कम आर्द्रता।",
            primaryCauseEn = "High temperature and low relative humidity.",
            firstActionHi = "पौधों पर पानी छिड़क कर नमी बनाए रखें और प्रभावित कोमल सिरे काट दें।",
            firstActionEn = "Mist water to maintain humidity and prune heavily curled shoots.",
            biologicalActionHi = "घुलनशील गंधक 80% WP (Wettable Sulphur) 3 ग्राम प्रति लीटर पानी में छिड़काव।",
            biologicalActionEn = "Spray Wettable Sulphur 80% WP @ 3 g/litre water.",
            dosePerLitreHi = "1.2 ग्राम प्रति लीटर पानी में छिड़काव",
            dosePerLitreEn = "1.2 g per litre water spray",
            dosePerBighaHi = "प्रति बीघा 80 ग्राम छिड़काव",
            dosePerBighaEn = "80 g per bigha spray"
        ),
        "Yellow mosaic patches, stunted and curled" to DiagnosticOptionLocale(
            optionTextHi = "3. पत्तों पर पीले-हरे चकत्ते, पत्ते छोटे और टेढ़े-मेढ़े हो गए हैं",
            diagnosisHi = "पत्ती मरोड़ विषाणु (Leaf Curl Virus) - वाहक सफेद मक्खी (Whitefly)।",
            diagnosisEn = "Leaf Curl Virus transmitted by Whitefly.",
            primaryCauseHi = "सफेद मक्खी द्वारा वायरस का प्रसार। वायरस होने के बाद दवा से सीधे ठीक नहीं होता।",
            primaryCauseEn = "Virus spread by whiteflies. Infected plants cannot be cured once viral symptoms appear.",
            firstActionHi = "संक्रमित पौधे को जड़ सहित उखाड़कर मिट्टी में दबा दें। पीले चिपचिपे जाल लगाएं।",
            firstActionEn = "Uproot and bury infected plants immediately. Install Yellow Sticky Traps.",
            biologicalActionHi = "सफेद मक्खी नियंत्रण के लिए नियमित 5 मिली/लीटर नीम तेल का छिड़काव करें।",
            biologicalActionEn = "Regularly spray Neem oil @ 5 ml/L to suppress whitefly vectors.",
            dosePerLitreHi = "0.5 ग्राम प्रति लीटर पानी में छिड़काव (सफेद मक्खी नियंत्रण हेतु)",
            dosePerLitreEn = "0.5 g per litre water spray (for whitefly vector control)",
            dosePerBighaHi = "प्रति बीघा 25 ग्राम छिड़काव",
            dosePerBighaEn = "25 g per bigha spray"
        ),

        // flower_issues
        "Excessive lush leaves but no flowers" to DiagnosticOptionLocale(
            optionTextHi = "1. खूब पत्ते और झाड़ी बढ़ रही है लेकिन फूल नहीं आ रहे हैं",
            diagnosisHi = "अत्यधिक नाइट्रोजन (यूरिया) खाद और धूप की कमी (अधिक वानस्पतिक वृद्धि)।",
            diagnosisEn = "Excessive Nitrogen (Urea) application and lack of sunlight (Vegetative Overgrowth).",
            primaryCauseHi = "यूरिया की अधिकता, फास्फोरस-पोटाश की कमी तथा बहुत घनी रोपाई।",
            primaryCauseEn = "Excess urea, phosphorus-potash deficiency, and overly dense planting.",
            firstActionHi = "यूरिया व नाइट्रोजन खाद पूरी तरह बंद करें। निचली फालतू शाखाएं छांटकर हवा व धूप आने दें।",
            firstActionEn = "Stop Urea/nitrogen completely. Prune excess lower shoots for light and airflow.",
            biologicalActionHi = "लकड़ी की राख और हड्डी का चूरा मिट्टी में मिलाकर सिंचाई करें।",
            biologicalActionEn = "Mix wood ash and bone meal into soil and irrigate.",
            dosePerLitreHi = "4-5 ग्राम प्रति लीटर पानी में छिड़काव",
            dosePerLitreEn = "4-5 g per litre water spray",
            dosePerBighaHi = "प्रति बीघा 500 ग्राम छिड़काव",
            dosePerBighaEn = "500 g per bigha spray"
        ),
        "Flowers bloom but drop rapidly / dry out" to DiagnosticOptionLocale(
            optionTextHi = "2. फूल आ रहे हैं लेकिन कली अवस्था में ही सूखकर पीले होकर झड़ जाते हैं",
            diagnosisHi = "बोरॉन (Boron) की कमी या अचानक तापमान बदलाव व थ्रिप्स का हमला।",
            diagnosisEn = "Boron deficiency or sudden temperature fluctuation and thrips attack.",
            primaryCauseHi = "परागण और फल बनने के लिए बोरॉन अत्यंत आवश्यक है; इसकी कमी से फूल झड़ते हैं।",
            primaryCauseEn = "Boron is vital for pollination and fertilization; its lack triggers flower drop.",
            firstActionHi = "खेत में हल्की व नियमित सिंचाई करें; मिट्टी सूखने या फटने न दें।",
            biologicalActionEn = "Spray Seaweed Extract @ 2 ml per litre water.",
            firstActionEn = "Provide light, regular irrigation; prevent soil from drying and cracking.",
            biologicalActionHi = "समुद्री शैवाल अर्क (Seaweed Extract) 2 मिली प्रति लीटर पानी में छिड़काव करें।",
            dosePerLitreHi = "1.5 - 2 ग्राम प्रति लीटर पानी में छिड़काव (सुबह या शाम)",
            dosePerLitreEn = "1.5 - 2 g per litre water spray (morning or evening)",
            dosePerBighaHi = "प्रति बीघा 100-150 ग्राम छिड़काव",
            dosePerBighaEn = "100-150 g per bigha spray"
        ),
        "Flowers open but fail to set fruit" to DiagnosticOptionLocale(
            optionTextHi = "3. फूल खिल रहे हैं पर फल नहीं बन रहे (परागण विफलता)",
            diagnosisHi = "परागण करने वाली मधुमक्खियों/कीटों की कमी या अत्यधिक तापमान (35°C से ऊपर)।",
            diagnosisEn = "Lack of pollinators/bees or excessively high temperature (>35°C).",
            primaryCauseHi = "तेज रासायनिक कीटनाशकों से मधुमक्खियों और मित्र कीटों का नष्ट होना।",
            primaryCauseEn = "Loss of bees and beneficial pollinators due to harsh daytime insecticides.",
            firstActionHi = "फूल आने के समय दिन में कोई विषैला कीटनाशक न छिड़कें। मेड़ पर गेंदे के फूल लगाएं।",
            firstActionEn = "Avoid toxic daytime sprays during flowering. Plant marigold along field borders.",
            biologicalActionHi = "लौकी/कद्दू जैसी फसलों में सुबह के समय हाथ से परागण (Hand Pollination) करें।",
            biologicalActionEn = "Perform hand pollination in the morning for cucurbits like bottle gourd and pumpkin.",
            dosePerLitreHi = "0.25 मिली प्रति लीटर पानी (4 लीटर में 1 मिली) — अधिक मात्रा नुकसानदायक है!",
            dosePerLitreEn = "0.25 ml per litre water (1 ml per 4 L) — overdose causes toxicity!",
            dosePerBighaHi = "प्रति बीघा 15 मिली छिड़काव",
            dosePerBighaEn = "15 ml per bigha spray"
        ),

        // fruit_issues
        "Small fruits turn black/yellow and drop" to DiagnosticOptionLocale(
            optionTextHi = "1. छोटे कंचे जैसे फल पीले या काले पड़कर झड़ रहे हैं",
            diagnosisHi = "कैल्शियम व बोरॉन की कमी या फफूंद संक्रमण (Anthracnose / Blossom End Rot)।",
            diagnosisEn = "Calcium & Boron deficiency or fungal infection (Anthracnose / Blossom End Rot).",
            primaryCauseHi = "अनियमित सिंचाई और मिट्टी में कैल्शियम की कमी।",
            primaryCauseEn = "Irregular irrigation and low available soil calcium.",
            firstActionHi = "सिंचाई का अंतराल नियमित रखें। सूखे और अचानक जलभराव दोनों से बचें।",
            firstActionEn = "Keep irrigation intervals steady. Avoid both drought stress and sudden waterlogging.",
            biologicalActionHi = "चूने का साफ पानी (100 लीटर पानी में 500 ग्राम बुझा चूना) छानकर छिड़कें या जड़ में दें।",
            biologicalActionEn = "Apply decanted lime water (500 g slaked lime in 100 L water) via spray or drench.",
            dosePerLitreHi = "4 ग्राम कैल्शियम नाइट्रेट + 1 ग्राम बोरॉन प्रति लीटर पानी",
            dosePerLitreEn = "4 g Calcium Nitrate + 1 g Boron per litre water",
            dosePerBighaHi = "प्रति बीघा 500 ग्राम छिड़काव",
            dosePerBighaEn = "500 g per bigha spray"
        ),
        "Boreholes on fruits, rot and frass inside" to DiagnosticOptionLocale(
            optionTextHi = "2. फल के ऊपर छेद, अंदर कीड़ा और सड़न",
            diagnosisHi = "फल एवं तना छेदक कीट (Fruit and Shoot Borer / Helicoverpa)।",
            diagnosisEn = "Fruit and Shoot Borer / Helicoverpa.",
            primaryCauseHi = "पतंगे अंडे देते हैं और सुंडी कोमल फलों के अंदर घुसकर नुकसान पहुंचाती है।",
            primaryCauseEn = "Moths lay eggs and emerging larvae bore directly into developing fruits.",
            firstActionHi = "प्रभावित फल इकट्ठा करके मिट्टी में दबा दें। प्रति बीघा 5 फेरोमोन ट्रैप लगाएं।",
            firstActionEn = "Collect and bury infested fruits. Install 5 pheromone traps per bigha.",
            biologicalActionHi = "Bt (Bacillus thuringiensis) 2 ग्राम/लीटर या नीम तेल 5 मिली/लीटर का छिड़काव।",
            biologicalActionEn = "Spray Bt (Bacillus thuringiensis) @ 2 g/L or Neem oil @ 5 ml/L.",
            dosePerLitreHi = "0.4 मिली प्रति लीटर पानी (10 लीटर में 4 मिली)",
            dosePerLitreEn = "0.4 ml per litre water (4 ml per 10 L)",
            dosePerBighaHi = "प्रति बीघा 20-25 मिली छिड़काव",
            dosePerBighaEn = "20-25 ml per bigha spray"
        ),
        "Fruits cracking / splitting open" to DiagnosticOptionLocale(
            optionTextHi = "3. फल बीच से फट रहे हैं (Fruit Cracking)",
            diagnosisHi = "बोरॉन की कमी तथा लंबे सूखे के बाद अचानक भारी सिंचाई या बारिश।",
            diagnosisEn = "Boron deficiency combined with sudden heavy irrigation or rain after prolonged dry spell.",
            primaryCauseHi = "फल का अंदरूनी गूदा तेजी से फूलना जबकि ऊपरी छिलका सख्त रहना।",
            primaryCauseEn = "Rapid internal pulp expansion while outer fruit skin remains inelastic.",
            firstActionHi = "मल्चिंग (Mulching) करके मिट्टी में नियमित हल्की नमी बनाए रखें।",
            firstActionEn = "Use organic/plastic mulching and maintain consistent moderate soil moisture.",
            biologicalActionHi = "संतुलित अनुपात में जैविक खाद और पोटाश खाद दें।",
            biologicalActionEn = "Supply balanced organic manure along with potash.",
            dosePerLitreHi = "1.5 ग्राम प्रति लीटर पानी में छिड़काव",
            dosePerLitreEn = "1.5 g per litre water spray",
            dosePerBighaHi = "प्रति बीघा 100 ग्राम छिड़काव",
            dosePerBighaEn = "100 g per bigha spray"
        ),

        // leaf_spots
        "Target-like circular concentric rings (Early Blight / Alternaria)" to DiagnosticOptionLocale(
            optionTextHi = "1. गोल भूरे धब्बे और धब्बों के भीतर छल्लेदार घेरे",
            diagnosisHi = "अगेती झुलसा या अल्टरनेरिया पत्ता धब्बा रोग (Early Blight / Alternaria)।",
            diagnosisEn = "Early Blight / Alternaria Leaf Spot.",
            primaryCauseHi = "गर्म व नम मौसम तथा नीचे के पुराने पत्तों पर लगातार नमी।",
            primaryCauseEn = "Warm humid conditions and persistent leaf wetness on lower older foliage.",
            firstActionHi = "नीचे के दागी पुराने पत्ते तोड़कर नष्ट करें। पौधों के ऊपर से पानी छिड़कना बंद करें।",
            firstActionEn = "Remove and burn infected lower leaves. Avoid overhead sprinkler watering.",
            biologicalActionHi = "ट्राइकोडर्मा विरिडी 5 ग्राम प्रति लीटर पानी में घोलकर छिड़काव करें।",
            biologicalActionEn = "Spray Trichoderma viride @ 5 g per litre water.",
            dosePerLitreHi = "1 मिली प्रति लीटर पानी में छिड़काव",
            dosePerLitreEn = "1 ml per litre water spray",
            dosePerBighaHi = "प्रति बीघा 70 मिली छिड़काव",
            dosePerBighaEn = "70 ml per bigha spray"
        ),
        "Water-soaked dark lesions spreading rapidly (Late Blight)" to DiagnosticOptionLocale(
            optionTextHi = "2. पानी जैसे भीगे गहरे भूरे धब्बे जो तेजी से पत्ते झुलसा रहे हैं",
            diagnosisHi = "पछेती झुलसा रोग (Late Blight - Phytophthora)।",
            diagnosisEn = "Late Blight (Phytophthora infestans).",
            primaryCauseHi = "लगातार कोहरा, बादल और ठंडा नम मौसम (15-22°C)।",
            primaryCauseEn = "Continuous fog, cloudy skies, and cool damp weather (15-22°C).",
            firstActionHi = "संक्रमित पौधे तुरंत नष्ट करें। खेत में अतिरिक्त नाइट्रोजन खाद बंद करें।",
            firstActionEn = "Destroy heavily infected plants promptly. Stop excess nitrogen top dressing.",
            biologicalActionHi = "बचाव हेतु कॉपर ऑक्सीक्लोराइड 50% WP 2.5 ग्राम/लीटर पानी में छिड़कें।",
            biologicalActionEn = "Apply preventive spray of Copper Oxychloride 50% WP @ 2.5 g/litre.",
            dosePerLitreHi = "2 ग्राम प्रति लीटर पानी में छिड़काव",
            dosePerLitreEn = "2 g per litre water spray",
            dosePerBighaHi = "प्रति बीघा 150 ग्राम छिड़काव",
            dosePerBighaEn = "150 g per bigha spray"
        ),

        // powdery_mildew
        "White flour-like powdery coating on upper leaf surface" to DiagnosticOptionLocale(
            optionTextHi = "1. पत्तों की ऊपरी सतह पर आटे जैसा सफेद चूर्ण जमा है",
            diagnosisHi = "चूर्णिल आसिता रोग (Powdery Mildew फफूंद)।",
            diagnosisEn = "Powdery Mildew fungal disease.",
            primaryCauseHi = "शुष्क दिन व रात में अधिक नमी या कोहरा तथा छायादार नमी वाले स्थान।",
            primaryCauseEn = "Dry days coupled with cool humid nights or shaded damp microclimate.",
            firstActionHi = "हवा के संचार हेतु घनी शाखाएं छांटें। संक्रमित पुराने पत्ते हटा दें।",
            firstActionEn = "Prune dense canopy for better airflow and remove infected lower leaves.",
            biologicalActionHi = "बेकिंग सोडा (5 ग्राम) + तरल साबुन (1 मिली) प्रति लीटर पानी में छिड़कें।",
            biologicalActionEn = "Spray Baking Soda (5 g) + liquid soap (1 ml) per litre water.",
            dosePerLitreHi = "2 मिली हेक्साकोनाजोल या 3 ग्राम घुलनशील गंधक प्रति लीटर",
            dosePerLitreEn = "2 ml Hexaconazole or 3 g Wettable Sulphur per litre",
            dosePerBighaHi = "प्रति बीघा 100 मिली छिड़काव",
            dosePerBighaEn = "100 ml per bigha spray"
        ),
        "Downy growth underneath, angular yellow spots on top" to DiagnosticOptionLocale(
            optionTextHi = "2. पत्ते के नीचे सफेद रुई जैसा और ऊपर कोनेदार पीले धब्बे",
            diagnosisHi = "मृदुरोमिल आसिता रोग (Downy Mildew फफूंद)।",
            diagnosisEn = "Downy Mildew fungal disease.",
            primaryCauseHi = "लगातार बारिश और हवा में अत्यधिक नमी (90% से अधिक)।",
            primaryCauseEn = "Prolonged rainfall and very high relative humidity (>90%).",
            firstActionHi = "मचान की ऊंचाई बढ़ाएं और जड़ों के पास जलभराव रोकें।",
            firstActionEn = "Raise trellis height for aeration and prevent standing water near roots.",
            biologicalActionHi = "स्यूडोमोनास फ्लोरेसेंस (Pseudomonas fluorescens) 5 ग्राम प्रति लीटर पानी में छिड़कें।",
            biologicalActionEn = "Spray Pseudomonas fluorescens @ 5 g per litre water.",
            dosePerLitreHi = "2.5 ग्राम प्रति लीटर पानी में छिड़काव",
            dosePerLitreEn = "2.5 g per litre water spray",
            dosePerBighaHi = "प्रति बीघा 200 ग्राम छिड़काव",
            dosePerBighaEn = "200 g per bigha spray"
        ),

        // root_stem_rot
        "Green plant suddenly wilts under daytime sun" to DiagnosticOptionLocale(
            optionTextHi = "1. अचानक हरा-भरा पौधा दिन की धूप में मुरझा कर सूख रहा है",
            diagnosisHi = "जीवाणु या फ्यूजेरियम उकठा रोग (Bacterial or Fusarium Wilt)।",
            diagnosisEn = "Bacterial or Fusarium Wilt.",
            primaryCauseHi = "मिट्टी में जलभराव से जड़ सड़न और पौधे की जल वाहिकाओं का बंद होना।",
            primaryCauseEn = "Root rot in waterlogged soil and clogging of vascular water vessels.",
            firstActionHi = "प्रभावित पौधा उखाड़ें और गड्ढे में ब्लीचिंग पाउडर या चूना डालें। जल निकासी साफ करें।",
            firstActionEn = "Uproot wilted plants and treat pits with lime or bleaching powder. Clear drainage channels.",
            biologicalActionHi = "ट्राइकोडर्मा व स्यूडोमोनास मिलाकर जड़ों की मिट्टी में घोल डालें (Drenching)।",
            biologicalActionEn = "Soil-drench roots with Trichoderma + Pseudomonas suspension.",
            dosePerLitreHi = "3 ग्राम कॉपर + 0.1 ग्राम स्ट्रेप्टोसाइक्लिन प्रति लीटर पानी जड़ में डालें",
            dosePerLitreEn = "3 g Copper Oxychloride + 0.1 g Streptocycline per litre root drench",
            dosePerBighaHi = "जड़ों के पास ड्रेंचिंग करें",
            dosePerBighaEn = "Apply as root-zone soil drench"
        ),
        "Collar rot / Damping-off near soil level in seedlings" to DiagnosticOptionLocale(
            optionTextHi = "2. मिट्टी की सतह पर तने का निचला हिस्सा काला पड़कर सड़ रहा है और पौधा गिर रहा है",
            diagnosisHi = "आर्द्र गलन या कॉलर सड़न (Damping-off / Collar Rot)।",
            diagnosisEn = "Damping-off / Collar Rot.",
            primaryCauseHi = "नर्सरी में अतिरिक्त पानी और पौधों का बहुत घना होना।",
            primaryCauseEn = "Overwatering in nursery beds and overcrowded seedlings.",
            firstActionHi = "नर्सरी में सिंचाई कम करें और धूप लगने दें। ऊंची क्यारियों पर पौध तैयार करें।",
            firstActionEn = "Reduce nursery watering, allow sunlight, and raise seedlings on raised beds.",
            biologicalActionHi = "बीजोपचार: प्रति किग्रा बीज में 10 ग्राम ट्राइकोडर्मा मिलाकर बुवाई करें।",
            biologicalActionEn = "Seed treatment: Coat seeds with Trichoderma @ 10 g/kg prior to sowing.",
            dosePerLitreHi = "2 ग्राम प्रति लीटर पानी से छिड़काव व जड़ भिगोना",
            dosePerLitreEn = "2 g per litre water spray and collar drench",
            dosePerBighaHi = "प्रति बीघा 150 ग्राम छिड़काव",
            dosePerBighaEn = "150 g per bigha spray"
        )
    )

    val nutrientLocales: Map<String, NutrientDeficiencyLocale> = mapOf(
        "N" to NutrientDeficiencyLocale(
            nutrientNameHi = "नाइट्रोजन (Nitrogen)",
            affectedPartHi = "पौधे के नीचे के पुराने पत्ते",
            affectedPartEn = "Lower older leaves",
            symptomsHi = "नीचे के पुराने पत्ते पूरी तरह हल्के पीले हो जाते हैं। पौधे की बढ़वार रुक जाती है और कल्ले कम निकलते हैं।",
            symptomsEn = "Lower older leaves turn uniformly pale yellow. Plant growth stalls with poor branching or tillering.",
            solutionHi = "यूरिया या NPK 19:19:19 खाद की टॉप ड्रेसिंग करें (5-8 किग्रा प्रति बीघा)।",
            solutionEn = "Top-dress Urea or foliar spray NPK 19:19:19 (5-8 kg/bigha Urea).",
            organicRemedyHi = "अच्छी सड़ी गोबर खाद, सरसों की खली का घोल या वर्मीकम्पोस्ट डालें।",
            organicRemedyEn = "Apply well-decomposed FYM, fermented oilcake water, or vermicompost."
        ),
        "P" to NutrientDeficiencyLocale(
            nutrientNameHi = "फास्फोरस (Phosphorus)",
            affectedPartHi = "निचले पत्ते व तना",
            affectedPartEn = "Lower leaves & stem",
            symptomsHi = "पत्तों की निचली सतह और डंठल पर बैंगनी या लालिमा दिखती है। जड़ें नहीं बढ़तीं और फूल देर से आते हैं।",
            symptomsEn = "Purplish or reddish tint on leaf undersides and petioles. Poor root growth and delayed flowering.",
            solutionHi = "बुवाई/रोपाई के समय या जड़ के पास सिंगल सुपर फास्फेट (SSP) या DAP डालें।",
            solutionEn = "Apply Single Super Phosphate (SSP) or DAP during soil preparation or root-zone placement.",
            organicRemedyHi = "हड्डी का चूरा (Bone Meal) या रॉक फॉस्फेट का प्रयोग करें।",
            organicRemedyEn = "Apply bone meal or rock phosphate enriched compost."
        ),
        "K" to NutrientDeficiencyLocale(
            nutrientNameHi = "पोटाश / पोटैशियम (Potassium)",
            affectedPartHi = "पुराने पत्तों के किनारे",
            affectedPartEn = "Margins of older leaves",
            symptomsHi = "पत्तों के किनारे और सिरे झुलसे हुए भूरे होकर मुड़ जाते हैं। फल छोटे रह जाते हैं और रोग प्रतिरोधक क्षमता घटती है।",
            symptomsEn = "Scorched brown leaf margins curling inward. Undersized fruits and reduced disease resistance.",
            solutionHi = "म्यूरेट ऑफ पोटाश (MOP) या सल्फेट ऑफ पोटाश (0:0:50) @ 5 ग्राम/लीटर छिड़काव।",
            solutionEn = "Apply Muriate of Potash (MOP) or foliar spray Sulphate of Potash (0:0:50) @ 5 g/L.",
            organicRemedyHi = "जड़ों के पास लकड़ी या फसल अवशेष की राख मिलाएं।",
            organicRemedyEn = "Apply wood ash around the root zone."
        ),
        "Ca" to NutrientDeficiencyLocale(
            nutrientNameHi = "कैल्शियम (Calcium)",
            affectedPartHi = "नई कोमल कोंपलें व फल का निचला सिरा",
            affectedPartEn = "Growing tips & blossom end of fruits",
            symptomsHi = "नई कोंपलों का सिरा सूखकर मर जाता है। टमाटर व मिर्च के फलों का निचला सिरा काला पड़कर सड़ जाता है (Blossom End Rot)।",
            symptomsEn = "Terminal buds die back; sunken black rot develops at the bottom of tomato and chilli fruits (Blossom End Rot).",
            solutionHi = "कैल्शियम नाइट्रेट @ 5 ग्राम प्रति लीटर पानी में छिड़काव या मिट्टी में दें।",
            solutionEn = "Spray or soil-apply Calcium Nitrate @ 5 g per litre water.",
            organicRemedyHi = "बुझा चूना या जिप्सम डालें और सिंचाई नियमित रखें।",
            organicRemedyEn = "Apply agricultural lime or gypsum and maintain steady irrigation."
        ),
        "Mg" to NutrientDeficiencyLocale(
            nutrientNameHi = "मैग्नीशियम (Magnesium)",
            affectedPartHi = "पुराने पत्तों की नसों के बीच का भाग",
            affectedPartEn = "Interveinal tissue of older leaves",
            symptomsHi = "पुराने पत्तों की मुख्य नसें हरी रहती हैं लेकिन नसों के बीच का हिस्सा पीला पड़ जाता है।",
            symptomsEn = "Main veins on older leaves stay green while interveinal areas turn yellow.",
            solutionHi = "मैग्नीशियम सल्फेट (Epsom Salt) @ 5 ग्राम प्रति लीटर पानी में छिड़काव।",
            solutionEn = "Foliar spray Magnesium Sulphate (Epsom Salt) @ 5 g per litre water.",
            organicRemedyHi = "डोलोमाइट चूना पत्थर चूर्ण का प्रयोग करें।",
            organicRemedyEn = "Apply dolomite limestone powder to soil."
        ),
        "S" to NutrientDeficiencyLocale(
            nutrientNameHi = "सल्फर / गंधक (Sulphur)",
            affectedPartHi = "ऊपर की नई पत्तियां",
            affectedPartEn = "Young upper leaves",
            symptomsHi = "ऊपर की नई पत्तियां पूरी तरह हल्की पीली हो जाती हैं। तिलहन व दलहन में तेल व प्रोटीन की मात्रा घटती है।",
            symptomsEn = "Young upper leaves turn uniformly pale yellow. Oil and protein quality drops in oilseeds and pulses.",
            solutionHi = "बेंटोनाइट सल्फर (3-4 किग्रा/बीघा) या घुलनशील गंधक 80% WP का छिड़काव।",
            solutionEn = "Apply Bentonite Sulphur (3-4 kg/bigha) or spray Wettable Sulphur 80% WP.",
            organicRemedyHi = "सरसों की खली या जिप्सम का प्रयोग करें।",
            organicRemedyEn = "Apply mustard oilcake or agricultural gypsum."
        ),
        "Zn" to NutrientDeficiencyLocale(
            nutrientNameHi = "जिंक / जस्ता (Zinc)",
            affectedPartHi = "कोमल पत्तियां व गांठें",
            affectedPartEn = "Young leaves & internodes",
            symptomsHi = "पत्तों का आकार छोटा रह जाता है (Little Leaf)। गांठों के बीच की दूरी घटने से पौधा झाड़ी जैसा दिखता है। धान में खैरा रोग।",
            symptomsEn = "Small malformed leaves (Little Leaf), shortened internodes causing rosetting, and Khaira disease in rice.",
            solutionHi = "चिलेटेड जिंक (Zn-EDTA 12%) @ 1.5 ग्राम प्रति लीटर पानी में छिड़काव।",
            solutionEn = "Spray Chelated Zinc (Zn-EDTA 12%) @ 1.5 g per litre water.",
            organicRemedyHi = "गोबर खाद और जिंक-समृद्ध कम्पोस्ट का उपयोग।",
            organicRemedyEn = "Apply FYM and zinc-enriched organic compost."
        ),
        "B" to NutrientDeficiencyLocale(
            nutrientNameHi = "बोरॉन (Boron)",
            affectedPartHi = "फूल, फल व कलियां",
            affectedPartEn = "Flowers, fruits & terminal buds",
            symptomsHi = "फूल व छोटे फल झड़ जाते हैं। फल फट जाते हैं या टेढ़े बनते हैं। फूलगोभी का फूल भूरा व खोखला हो जाता है।",
            symptomsEn = "Flower and fruit drop, fruit cracking/deformity, and brown/hollow curd in cauliflower.",
            solutionHi = "बोरॉन 20% @ 1.5 - 2 ग्राम प्रति लीटर पानी में फूल आने से पहले व बाद में छिड़काव।",
            solutionEn = "Spray Boron 20% @ 1.5 - 2 g/L before and right after flowering.",
            organicRemedyHi = "सिंचाई के साथ बोरेक्स व संतुलित जैविक खाद दें।",
            organicRemedyEn = "Apply Borax along with balanced organic manure and moisture."
        ),
        "Fe" to NutrientDeficiencyLocale(
            nutrientNameHi = "आयरन / लोहा (Iron)",
            affectedPartHi = "नई कोमल पत्तियां",
            affectedPartEn = "Young emerging leaves",
            symptomsHi = "नई पत्तियां बिल्कुल सफेद-पीली हो जाती हैं लेकिन नसें गहरी हरी रेखाओं जैसी उभरी रहती हैं।",
            symptomsEn = "New leaves turn pale whitish-yellow while veins stand out sharply dark green.",
            solutionHi = "चिलेटेड आयरन (Fe-EDTA 12%) @ 1.5 ग्राम प्रति लीटर पानी में छिड़काव।",
            solutionEn = "Spray Chelated Iron (Fe-EDTA 12%) @ 1.5 g per litre water.",
            organicRemedyHi = "हरी खाद (ढैंचा) और ह्यूमिक एसिड का प्रयोग करें।",
            organicRemedyEn = "Incorporate green manure (Dhaincha) and Humic Acid."
        ),
        "Mn" to NutrientDeficiencyLocale(
            nutrientNameHi = "मैंगनीज (Manganese)",
            affectedPartHi = "मध्यम आयु के पत्ते",
            affectedPartEn = "Middle-aged leaves",
            symptomsHi = "पत्तों की नसों के बीच छोटे-छोटे पीले चकत्ते व सूखे भूरे बिंदु बनते हैं (Checkered pattern)।",
            symptomsEn = "Checkered yellow mottling between veins accompanied by tiny dry brown necrotic specks.",
            solutionHi = "मैंगनीज सल्फेट @ 2 ग्राम प्रति लीटर पानी में छिड़काव।",
            solutionEn = "Spray Manganese Sulphate @ 2 g per litre water.",
            organicRemedyHi = "वर्मीकम्पोस्ट और ट्राइकोडर्मा का मिश्रण उपयोग करें।",
            organicRemedyEn = "Apply vermicompost blended with Trichoderma."
        ),
        "Cu" to NutrientDeficiencyLocale(
            nutrientNameHi = "कॉपर / तांबा (Copper)",
            affectedPartHi = "शीर्ष कोंपलें व पत्तों के सिरे",
            affectedPartEn = "Shoot tips & leaf margins",
            symptomsHi = "शीर्ष कलिका सूखकर मरने लगती है (Dieback) और पत्तों के किनारे बदरंग हो जाते हैं।",
            symptomsEn = "Shoot tip dieback and bleached/discolored leaf margins.",
            solutionHi = "कॉपर ऑक्सीक्लोराइड 50% WP @ 2 ग्राम प्रति लीटर पानी में छिड़काव।",
            solutionEn = "Spray Copper Oxychloride 50% WP @ 2 g per litre water.",
            organicRemedyHi = "1% बोर्डो मिश्रण (Bordeaux mixture) का प्रयोग करें।",
            organicRemedyEn = "Apply 1% Bordeaux mixture spray."
        )
    )

    val stageLocales: List<FertilizerStageLocale> = listOf(
        FertilizerStageLocale(
            stageNameHi = "1. मुख्य खाद (Basal Dose)",
            stageNameEn = "1. Basal Dose (Land Preparation)",
            timingHi = "खेत की अंतिम जुताई पर बुवाई या रोपाई से पहले",
            timingEn = "Final ploughing prior to sowing or transplanting",
            objectiveHi = "जड़ों का फैलाव व शुरुआती पोषण संचय",
            objectiveEn = "Early root expansion and baseline nutrient availability",
            recommendedFertilizersHi = "सड़ी गोबर/कम्पोस्ट खाद (1 टन/बीघा) + SSP (35 किग्रा/बीघा) या DAP (15 किग्रा/बीघा) + MOP (15 किग्रा/बीघा) + नीम खली (30 किग्रा)।",
            recommendedFertilizersEn = "Well-rotted FYM/Compost (1 ton/bigha) + SSP (35 kg/bigha) or DAP (15 kg/bigha) + MOP (15 kg/bigha) + Neem Cake (30 kg).",
            cautionHi = "रासायनिक खाद सीधे बीज के संपर्क में न डालें; मिट्टी में 2-3 इंच नीचे मिलाएं।",
            cautionEn = "Avoid direct seed contact with chemical fertilizers; mix 2-3 inches deep into soil."
        ),
        FertilizerStageLocale(
            stageNameHi = "2. प्रारंभिक वृद्धि अवस्था (Vegetative Stage)",
            stageNameEn = "2. Vegetative Growth Stage",
            timingHi = "रोपाई के 15 से 25 दिन बाद",
            timingEn = "15 to 25 days after transplanting/sowing",
            objectiveHi = "तने, शाखाओं व पत्तियों का मजबूत विकास",
            objectiveEn = "Strong stem, branching, and canopy leaf growth",
            recommendedFertilizersHi = "यूरिया (6-8 किग्रा/बीघा) टॉप ड्रेसिंग या NPK 19:19:19 (5 ग्राम/लीटर छिड़काव) + चिलेटेड जिंक।",
            recommendedFertilizersEn = "Urea top dressing (6-8 kg/bigha) or NPK 19:19:19 (5 g/L spray) + Chelated Zinc.",
            cautionHi = "सूखी मिट्टी में यूरिया न डालें; हल्की सिंचाई के बाद नमी रहने पर डालें।",
            cautionEn = "Never apply Urea to dry soil; apply after light irrigation while soil is moist."
        ),
        FertilizerStageLocale(
            stageNameHi = "3. फूल आने से पहले (Pre-Flowering Stage)",
            stageNameEn = "3. Pre-Flowering Stage",
            timingHi = "रोपाई के 35 से 45 दिन बाद (कलियां निकलते समय)",
            timingEn = "35 to 45 days after planting (bud initiation)",
            objectiveHi = "अधिक फूल कलियों का निर्माण और फूल झड़ना रोकना",
            objectiveEn = "Maximize flower bud formation and prevent blossom drop",
            recommendedFertilizersHi = "NPK 12:61:0 (5 ग्राम/लीटर) + बोरॉन 20% (1.5 ग्राम/लीटर पानी में छिड़काव)।",
            recommendedFertilizersEn = "NPK 12:61:0 (5 g/L) + Boron 20% (1.5 g/L foliar spray).",
            cautionHi = "इस समय अतिरिक्त नाइट्रोजन या यूरिया देने से पत्ते ज्यादा बढ़ेंगे और फूल कम आएंगे।",
            cautionEn = "Excess nitrogen/urea at this stage triggers leafy overgrowth and reduces flowering."
        ),
        FertilizerStageLocale(
            stageNameHi = "4. फल विकास अवस्था (Fruit Development Stage)",
            stageNameEn = "4. Fruit Development Stage",
            timingHi = "रोपाई के 50 से 70 दिन बाद (फल बढ़ते समय)",
            timingEn = "50 to 70 days after planting (fruit sizing)",
            objectiveHi = "फलों का वजन, आकार, मिठास व चमक बढ़ाना",
            objectiveEn = "Increase fruit weight, size, sweetness, and color gloss",
            recommendedFertilizersHi = "पोटाश सल्फेट (0:0:50) @ 5 ग्राम/लीटर + कैल्शियम नाइट्रेट @ 4 ग्राम/लीटर छिड़काव।",
            recommendedFertilizersEn = "Sulphate of Potash (0:0:50) @ 5 g/L + Calcium Nitrate @ 4 g/L spray.",
            cautionHi = "सिंचाई अनियमित न करें; सूखे और अधिक पानी के उतार-चढ़ाव से फल फट सकते हैं।",
            cautionEn = "Maintain steady irrigation; alternating drought and excess moisture causes fruit cracking."
        ),
        FertilizerStageLocale(
            stageNameHi = "5. तुड़ाई व अगली बढ़वार अवस्था (Harvest & Flush Stage)",
            stageNameEn = "5. Harvest & Next Flush Stage",
            timingHi = "पहली तुड़ाई के बाद अगली फसल बहार के लिए",
            timingEn = "After first picking to support subsequent flushes",
            objectiveHi = "नई कलियां लाना व निरंतर उपज बनाए रखना",
            objectiveEn = "Encourage fresh shoots and sustain repeat harvests",
            recommendedFertilizersHi = "हल्का खली घोल या सूक्ष्म पोषक तत्व छिड़काव। चाय बागान में हल्का यूरिया व पोटाश छिड़काव।",
            recommendedFertilizersEn = "Light oilcake extract or micronutrient spray. In tea gardens, light urea and potash.",
            cautionHi = "किसी भी कीटनाशक छिड़काव के बाद निर्धारित प्रतीक्षा अवधि (PHI) पूरा होने पर ही तुड़ाई करें।",
            cautionEn = "Strictly observe the Pre-Harvest Interval (PHI) after any pesticide spray before picking."
        )
    )

    fun getTreeTitle(tree: DiagnosticSymptomTree, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> tree.titleBn
        AppLanguage.HINDI -> treeLocales[tree.symptomId]?.titleHi ?: tree.titleEn
        AppLanguage.ENGLISH -> tree.titleEn
    }

    fun getTreeInitialQuestion(tree: DiagnosticSymptomTree, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> tree.initialQuestionBn
        AppLanguage.HINDI -> treeLocales[tree.symptomId]?.initialQuestionHi ?: tree.initialQuestionBn
        AppLanguage.ENGLISH -> treeLocales[tree.symptomId]?.initialQuestionEn ?: tree.initialQuestionBn
    }

    fun getOptionText(opt: DiagnosticQuestionOption, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> opt.optionTextBn
        AppLanguage.HINDI -> optionLocales[opt.optionTextEn]?.optionTextHi ?: opt.optionTextEn
        AppLanguage.ENGLISH -> opt.optionTextEn
    }

    fun getDiagnosis(opt: DiagnosticQuestionOption, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> opt.diagnosisBn
        AppLanguage.HINDI -> optionLocales[opt.optionTextEn]?.diagnosisHi ?: opt.diagnosisBn
        AppLanguage.ENGLISH -> optionLocales[opt.optionTextEn]?.diagnosisEn ?: opt.diagnosisBn
    }

    fun getPrimaryCause(opt: DiagnosticQuestionOption, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> opt.primaryCauseBn
        AppLanguage.HINDI -> optionLocales[opt.optionTextEn]?.primaryCauseHi ?: opt.primaryCauseBn
        AppLanguage.ENGLISH -> optionLocales[opt.optionTextEn]?.primaryCauseEn ?: opt.primaryCauseBn
    }

    fun getFirstAction(opt: DiagnosticQuestionOption, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> opt.firstActionBn
        AppLanguage.HINDI -> optionLocales[opt.optionTextEn]?.firstActionHi ?: opt.firstActionBn
        AppLanguage.ENGLISH -> optionLocales[opt.optionTextEn]?.firstActionEn ?: opt.firstActionBn
    }

    fun getBiologicalAction(opt: DiagnosticQuestionOption, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> opt.biologicalActionBn
        AppLanguage.HINDI -> optionLocales[opt.optionTextEn]?.biologicalActionHi ?: opt.biologicalActionBn
        AppLanguage.ENGLISH -> optionLocales[opt.optionTextEn]?.biologicalActionEn ?: opt.biologicalActionBn
    }

    fun getDosePerLitre(opt: DiagnosticQuestionOption, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> opt.dosePerLitreBn
        AppLanguage.HINDI -> optionLocales[opt.optionTextEn]?.dosePerLitreHi ?: CropTextFormats.toAsciiDigits(opt.dosePerLitreBn)
        AppLanguage.ENGLISH -> optionLocales[opt.optionTextEn]?.dosePerLitreEn ?: CropTextFormats.toAsciiDigits(opt.dosePerLitreBn)
    }

    fun getDosePerBigha(opt: DiagnosticQuestionOption, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> opt.dosePerBighaBn
        AppLanguage.HINDI -> optionLocales[opt.optionTextEn]?.dosePerBighaHi ?: CropTextFormats.toAsciiDigits(opt.dosePerBighaBn)
        AppLanguage.ENGLISH -> optionLocales[opt.optionTextEn]?.dosePerBighaEn ?: CropTextFormats.toAsciiDigits(opt.dosePerBighaBn)
    }

    fun getSafetyPPE(opt: DiagnosticQuestionOption, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> opt.safetyPPEBn
        AppLanguage.HINDI -> optionLocales[opt.optionTextEn]?.safetyPPEHi
            ?: "हाथों में दस्ताने और चेहरे पर मास्क पहनकर शांत मौसम में छिड़काव करें।"
        AppLanguage.ENGLISH -> optionLocales[opt.optionTextEn]?.safetyPPEEn
            ?: "Wear gloves and face mask, and spray in calm weather."
    }

    fun getNutrientName(info: NutrientDeficiencyInfo, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> info.nutrientNameBn
        AppLanguage.HINDI -> nutrientLocales[info.nutrientCode]?.nutrientNameHi ?: info.nutrientNameEn
        AppLanguage.ENGLISH -> info.nutrientNameEn
    }

    fun getNutrientAffectedPart(info: NutrientDeficiencyInfo, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> info.affectedPartBn
        AppLanguage.HINDI -> nutrientLocales[info.nutrientCode]?.affectedPartHi ?: info.affectedPartBn
        AppLanguage.ENGLISH -> nutrientLocales[info.nutrientCode]?.affectedPartEn ?: info.affectedPartBn
    }

    fun getNutrientSymptoms(info: NutrientDeficiencyInfo, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> info.symptomsBn
        AppLanguage.HINDI -> nutrientLocales[info.nutrientCode]?.symptomsHi ?: info.symptomsBn
        AppLanguage.ENGLISH -> nutrientLocales[info.nutrientCode]?.symptomsEn ?: info.symptomsBn
    }

    fun getNutrientSolution(info: NutrientDeficiencyInfo, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> info.solutionBn
        AppLanguage.HINDI -> nutrientLocales[info.nutrientCode]?.solutionHi ?: info.solutionBn
        AppLanguage.ENGLISH -> nutrientLocales[info.nutrientCode]?.solutionEn ?: info.solutionBn
    }

    fun getNutrientOrganicRemedy(info: NutrientDeficiencyInfo, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> info.organicRemedyBn
        AppLanguage.HINDI -> nutrientLocales[info.nutrientCode]?.organicRemedyHi ?: info.organicRemedyBn
        AppLanguage.ENGLISH -> nutrientLocales[info.nutrientCode]?.organicRemedyEn ?: info.organicRemedyBn
    }

    private fun indexForStage(stage: FertilizerStagePlan): Int {
        val idx = DiagnosticKnowledgeDatabase.fertilizerStages.indexOfFirst { it.stageNameBn == stage.stageNameBn }
        return if (idx in stageLocales.indices) idx else 0
    }

    fun getStageName(stage: FertilizerStagePlan, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> stage.stageNameBn
        AppLanguage.HINDI -> stageLocales[indexForStage(stage)].stageNameHi
        AppLanguage.ENGLISH -> stageLocales[indexForStage(stage)].stageNameEn
    }

    fun getStageTiming(stage: FertilizerStagePlan, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> stage.timingBn
        AppLanguage.HINDI -> stageLocales[indexForStage(stage)].timingHi
        AppLanguage.ENGLISH -> stageLocales[indexForStage(stage)].timingEn
    }

    fun getStageObjective(stage: FertilizerStagePlan, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> stage.objectiveBn
        AppLanguage.HINDI -> stageLocales[indexForStage(stage)].objectiveHi
        AppLanguage.ENGLISH -> stageLocales[indexForStage(stage)].objectiveEn
    }

    fun getStageFertilizers(stage: FertilizerStagePlan, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> stage.recommendedFertilizersBn
        AppLanguage.HINDI -> stageLocales[indexForStage(stage)].recommendedFertilizersHi
        AppLanguage.ENGLISH -> stageLocales[indexForStage(stage)].recommendedFertilizersEn
    }

    fun getStageCaution(stage: FertilizerStagePlan, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> stage.cautionBn
        AppLanguage.HINDI -> stageLocales[indexForStage(stage)].cautionHi
        AppLanguage.ENGLISH -> stageLocales[indexForStage(stage)].cautionEn
    }
}
