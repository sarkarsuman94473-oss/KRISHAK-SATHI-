package com.example.data.datasource

import com.example.data.model.DiagnosticQuestionOption
import com.example.data.model.DiagnosticSymptomTree
import com.example.data.model.FertilizerStagePlan
import com.example.data.model.NutrientDeficiencyInfo

object DiagnosticKnowledgeDatabase {

    val symptomTrees: List<DiagnosticSymptomTree> = listOf(
        DiagnosticSymptomTree(
            symptomId = "leaf_yellowing",
            titleBn = "পাতা হলুদ হওয়া",
            titleEn = "Leaf Yellowing",
            iconEmoji = "🍃",
            initialQuestionBn = "হলুদ ভাব গাছের কোথায় এবং কীভাবে শুরু হয়েছে?",
            options = listOf(
                DiagnosticQuestionOption(
                    optionTextBn = "১. গাছের নিচের পুরনো পাতা আগে হলুদ হচ্ছে, গাছ দুর্বল",
                    optionTextEn = "Lower older leaves turn yellow first",
                    diagnosisBn = "নাইট্রোজেনের (N) অভাব অথবা গোড়ায় অতিরিক্ত জল জমে শিকড়ের দমবন্ধ হওয়া।",
                    primaryCauseBn = "মাটিতে অতিরিক্ত জল জমে বাতাস চলাচল বন্ধ অথবা নাইট্রোজেন সারের ঘাটতি।",
                    firstActionBn = "জমির অতিরিক্ত জল নিষ্কাশন করুন। মাটি আলগা করে রোদে শুকিয়ে বাতাস চলাচলের ব্যবস্থা করুন।",
                    biologicalActionBn = "ভার্মিকম্পোস্ট বা গোবর সার প্রয়োগ করুন। ট্রাইকোডার্মা শিকড়ে দিন।",
                    chemicalActiveIngredient = "Urea / NPK 19:19:19",
                    dosePerLitreBn = "৫ গ্রাম প্রতি লিটার জলে স্প্রে",
                    dosePerBighaBn = "বিঘায় ৫-৭ কেজি ইউরিয়া উপরি প্রয়োগ",
                    preHarvestIntervalDays = 3
                ),
                DiagnosticQuestionOption(
                    optionTextBn = "২. কচি ডগা ও নতুন পাতা হলুদ কিন্তু শিরা গাঢ় সবুজ",
                    optionTextEn = "New young leaves yellow with green veins",
                    diagnosisBn = "আয়রন (Fe) বা অনুখাদ্যের ঘাটতি (Interveinal Chlorosis)।",
                    primaryCauseBn = "মাটির উচ্চ ক্ষারীয় pH অথবা ভারী মাটিতে মাইক্রোনিউট্রিয়েন্ট শিকড় দ্বারা শোষণে বাধা।",
                    firstActionBn = "গোড়ার মাটি আলগা করুন। জৈব কম্পোস্ট ও চিলেটেড অনুখাদ্য ব্যবহার করুন।",
                    biologicalActionBn = "হিউমিক অ্যাসিড (Humic Acid) ২ মিলি প্রতি লিটার জলে মিশিয়ে গোড়ায় দিন।",
                    chemicalActiveIngredient = "Chelated Iron (Fe-EDTA 12%)",
                    dosePerLitreBn = "১-১.৫ গ্রাম প্রতি লিটার জলে স্প্রে",
                    dosePerBighaBn = "বিঘায় ২০০ গ্রাম স্প্রে",
                    preHarvestIntervalDays = 0
                ),
                DiagnosticQuestionOption(
                    optionTextBn = "৩. পাতার কিনারায় পোড়া হলুদ-বাদামি দাগ, ফলন কম",
                    optionTextEn = "Leaf edges yellow and burnt brown",
                    diagnosisBn = "পটাশিয়াম (K) বা ম্যাগনেসিয়াম (Mg) এর ঘাটতি।",
                    primaryCauseBn = "বেলে মাটিতে পটাশ ধুয়ে যাওয়া এবং অম্ল মাটিতে ম্যাগনেসিয়াম ঘাটতি।",
                    firstActionBn = "গাছের গোড়ায় আর্দ্রতা বজায় রাখুন এবং পর্যাপ্ত জৈব সার দিন।",
                    biologicalActionBn = "কলা গাছের ছাই বা কাঠকয়লার ছাই মিশিয়ে হালকা জল দিন।",
                    chemicalActiveIngredient = "Sulphate of Potash (0:0:50) / Epsom Salt (MgSO4)",
                    dosePerLitreBn = "৪-৫ গ্রাম প্রতি লিটার জলে স্প্রে",
                    dosePerBighaBn = "বিঘায় ২ কেজি স্প্রে",
                    preHarvestIntervalDays = 0
                ),
                DiagnosticQuestionOption(
                    optionTextBn = "৪. পাতা হলুদ হয়ে নিচের পিঠে জাল বা অতি ক্ষুদ্র পোকা",
                    optionTextEn = "Yellowing with fine webbing / tiny mites underneath",
                    diagnosisBn = "লাল বা হলুদ মাইট (Mites) এবং সাদা মাছি (Whitefly) দ্বারা রস শোষণ।",
                    primaryCauseBn = "শুষ্ক ও গরম আবহাওয়া। ধুলোবালি ও খরা।",
                    firstActionBn = "জলের তীব্র ঝাপটা দিয়ে পাতার নিচের পিঠ ধুয়ে দিন। হলুদ স্টিকি ট্র্যাপ ব্যবহার করুন।",
                    biologicalActionBn = "নিম তেল (Neem Oil 10,000 ppm) ৩ মিলি প্রতি লিটার জলে মিশিয়ে স্প্রে।",
                    chemicalActiveIngredient = "Spiromesifen 22.9% SC",
                    dosePerLitreBn = "১ মিলি প্রতি লিটার জলে স্প্রে",
                    dosePerBighaBn = "বিঘায় ৫০ মিলি স্প্রে",
                    preHarvestIntervalDays = 7,
                    safetyPPEBn = "মাস্ক ও দস্তানা পরুন। হাওয়ার দিকে স্প্রে করুন।"
                )
            )
        ),
        DiagnosticSymptomTree(
            symptomId = "leaf_curl",
            titleBn = "পাতা কুঁকড়ে যাওয়া",
            titleEn = "Leaf Curl",
            iconEmoji = "🌀",
            initialQuestionBn = "পাতা কোন দিকে এবং কীভাবে কুঁকড়াচ্ছে?",
            options = listOf(
                DiagnosticQuestionOption(
                    optionTextBn = "১. পাতার ধার ওপরের দিকে বাটির মতো কুঁকড়ে যাচ্ছে",
                    optionTextEn = "Leaves cup upwards like a bowl",
                    diagnosisBn = "থ্রিপস (Thrips) পোকার কচি পাতা থেকে রস শোষণ।",
                    primaryCauseBn = "শুষ্ক আবহাওয়ায় থ্রিপস পোকার দ্রুত বিস্তার।",
                    firstActionBn = "জমিতে নীল স্টিকি ট্র্যাপ (Blue Sticky Trap) বিঘায় ৬-৮টি স্থাপন করুন।",
                    biologicalActionBn = "ভার্টিসিলিয়াম লেকানি (Verticillium lecanii) ৫ গ্রাম/লিটার অথবা নিম তেল।",
                    chemicalActiveIngredient = "Fipronil 5% SC",
                    dosePerLitreBn = "১.৫ মিলি প্রতি লিটার জলে স্প্রে",
                    dosePerBighaBn = "বিঘায় ১০০ মিলি স্প্রে",
                    preHarvestIntervalDays = 7
                ),
                DiagnosticQuestionOption(
                    optionTextBn = "২. পাতা নিচের দিকে উল্টো বাটির মতো মুড়ে যাচ্ছে ও খসখসে",
                    optionTextEn = "Leaves curl downwards like inverted boat",
                    diagnosisBn = "হলুদ বা লাল মাইট (Mites) আক্রমণ।",
                    primaryCauseBn = "উষ্ণ তাপমাত্রা ও কম আর্দ্রতা।",
                    firstActionBn = "গাছের ওপর জল ছিটিয়ে আর্দ্রতা বজায় রাখুন ও আক্রান্ত ডগা কেটে ফেলুন।",
                    biologicalActionBn = "সালফার ৮০% ডব্লিউপি (Wettable Sulphur 80% WP) ৩ গ্রাম প্রতি লিটার।",
                    chemicalActiveIngredient = "Diafenthiuron 50% WP",
                    dosePerLitreBn = "১.২ গ্রাম প্রতি লিটার জলে স্প্রে",
                    dosePerBighaBn = "বিঘায় ৮০ গ্রাম স্প্রে",
                    preHarvestIntervalDays = 7
                ),
                DiagnosticQuestionOption(
                    optionTextBn = "৩. পাতা হলুদ-সবুজ ছোপযুক্ত, বিকৃত ও ছোট হয়ে যাওয়া",
                    optionTextEn = "Yellow mosaic patches, stunted and curled",
                    diagnosisBn = "লিফ কার্ল ভাইরাস (Leaf Curl Virus) - বাহক সাদা মাছি (Whitefly)।",
                    primaryCauseBn = "সাদা মাছি দ্বারা ভাইরাস ছড়ানো। ভাইরাস একবার হলে গাছে সরাসরি সারানো যায় না।",
                    firstActionBn = "আক্রান্ত গাছ সঙ্গে সঙ্গে শিকড়সহ উপড়ে মাটির নিচে পুঁতে ফেলুন। হলুদ আঠালো ফাঁদ লাগান।",
                    biologicalActionBn = "নিম তেল ৫ মিলি প্রতি লিটার নিয়মিত স্প্রে করে সাদা মাছি দমন করুন।",
                    chemicalActiveIngredient = "Diafenthiuron 50% WP / Acetamiprid 20% SP",
                    dosePerLitreBn = "০.৫ গ্রাম প্রতি লিটার জলে স্প্রে (সাদা মাছি নিয়ন্ত্রণের জন্য)",
                    dosePerBighaBn = "বিঘায় ২৫ গ্রাম স্প্রে",
                    preHarvestIntervalDays = 5
                )
            )
        ),
        DiagnosticSymptomTree(
            symptomId = "flower_issues",
            titleBn = "ফুল না আসা / ফুল ঝরে যাওয়া",
            titleEn = "Flowering Issues & Drop",
            iconEmoji = "🌸",
            initialQuestionBn = "গাছের ফুলের কী সমস্যা দেখা দিচ্ছে?",
            options = listOf(
                DiagnosticQuestionOption(
                    optionTextBn = "১. প্রচুর পাতা ও ঝোপ বাড়ছে কিন্তু ফুল আসছে না",
                    optionTextEn = "Excessive lush leaves but no flowers",
                    diagnosisBn = "অতিরিক্ত নাইট্রোজেন (ইউরিয়া) সার প্রয়োগ ও রোদের অভাব (Vegetative Overgrowth)।",
                    primaryCauseBn = "ইউরিয়া বেশি ও পটাশ-ফসফেটের অভাব এবং গাছ খুব ঘনভাবে লাগানো।",
                    firstActionBn = "ইউরিয়া বা নাইট্রোজেন সার পুরোপুরি বন্ধ করুন। গাছের নিচের অপ্রয়োজনীয় ডাল ছেঁটে আলো-বাতাস ঢুকতে দিন।",
                    biologicalActionBn = "কাঠকয়লার ছাই ও হাড়ের গুঁড়ো মাটির সাথে মিশিয়ে সেচ দিন।",
                    chemicalActiveIngredient = "Monopotassium Phosphate (0:52:34)",
                    dosePerLitreBn = "৪-৫ গ্রাম প্রতি লিটার জলে স্প্রে",
                    dosePerBighaBn = "বিঘায় ৫০০ গ্রাম স্প্রে",
                    preHarvestIntervalDays = 0
                ),
                DiagnosticQuestionOption(
                    optionTextBn = "২. ফুল আসছে কিন্তু কুঁড়ি অবস্থাতেই শুকিয়ে হলুদ হয়ে ঝরে পড়ছে",
                    optionTextEn = "Flowers bloom but drop rapidly / dry out",
                    diagnosisBn = "বোরণ (Boron) এর অভাব অথবা হঠাৎ তাপমাত্রার তীব্র উত্থান-পতন ও থ্রিপস আক্রমণ।",
                    primaryCauseBn = "ফুলের পরাগায়ন ও গর্ভধারণের জন্য বোরণ অতিপ্রয়োজনীয়। বোরণ না পেলে ফুল ঝরে যায়।",
                    firstActionBn = "জমিতে হালকা ও নিয়মিত সেচ দিন, মাটি যেন একেবারে শুকিয়ে ফেটে না যায়।",
                    biologicalActionBn = "সিউইড নির্যাস (Seaweed Extract) ২ মিলি প্রতি লিটার জলে মিশিয়ে স্প্রে।",
                    chemicalActiveIngredient = "Di-Sodium Octaborate Tetrahydrate (Boron 20%)",
                    dosePerLitreBn = "১.৫ - ২ গ্রাম প্রতি লিটার জলে স্প্রে (সকালে বা বিকেলে)",
                    dosePerBighaBn = "বিঘায় ১০০-১৫০ গ্রাম স্প্রে",
                    preHarvestIntervalDays = 0
                ),
                DiagnosticQuestionOption(
                    optionTextBn = "৩. ফুল ফুটছে কিন্তু ফল বাঁধছে না (পরাগায়ন ব্যর্থতা)",
                    optionTextEn = "Flowers open but fail to set fruit",
                    diagnosisBn = "পরাগায়নকারী কীট/মৌমাছির অভাব অথবা উচ্চ তাপমাত্রা (৩৫° সে-র বেশি)।",
                    primaryCauseBn = "তীব্র রাসায়নিক কীটনাশক প্রয়োগের ফলে মৌমাছি ও উপকারী পরাগায়নকারী কীট মারা যাওয়া।",
                    firstActionBn = "ফুলে থাকার সময় দিনের বেলায় কোনো বিষাক্ত কীটনাশক স্প্রে করবেন না। ক্ষেতের ধারে গাঁদা ফুল লাগান।",
                    biologicalActionBn = "লাউ/কুমড়ো জাতীয় ফসলে সকালে হাত দিয়ে কৃত্রিম পরাগায়ন (Hand Pollination) করুন।",
                    chemicalActiveIngredient = "Planofix (Alpha Naphthyl Acetic Acid 4.5% SL)",
                    dosePerLitreBn = "০.২৫ মিলি প্রতি লিটার জলে (৪ লিটার জলে ১ মিলি) - অতিরিক্ত ডোজ বিষাক্ত!",
                    dosePerBighaBn = "বিঘায় ১৫ মিলি স্প্রে",
                    preHarvestIntervalDays = 7
                )
            )
        ),
        DiagnosticSymptomTree(
            symptomId = "fruit_issues",
            titleBn = "ফল না ধরা / ফল ঝরে যাওয়া ও দাগ",
            titleEn = "Fruit Setting & Drop Issues",
            iconEmoji = "🍅",
            initialQuestionBn = "ফলের কোন নির্দিষ্ট লক্ষণটি দেখতে পাচ্ছেন?",
            options = listOf(
                DiagnosticQuestionOption(
                    optionTextBn = "১. ছোট মার্বেল অবস্থায় ফল হলুদ বা কালো হয়ে ঝরে পড়ছে",
                    optionTextEn = "Small fruits turn black/yellow and drop",
                    diagnosisBn = "ক্যালসিয়াম ও বোরণের ঘাটতি অথবা ছত্রাক সংক্রমণ (Anthracnose / Blossom End Rot)।",
                    primaryCauseBn = "অনিয়মিত সেচ ও মাটিতে ক্যালসিয়ামের অভাব।",
                    firstActionBn = "সেচের ব্যবধান নিয়মিত রাখুন। জমিতে খরা ও হঠাৎ অতিরিক্ত জল দুটোই এড়িয়ে চলুন।",
                    biologicalActionBn = "চুন জল (১০০ লিটার জলে ৫০০ গ্রাম কলিচুন) ছেঁকে স্প্রে বা গোড়ায় দিন।",
                    chemicalActiveIngredient = "Calcium Nitrate + Boron",
                    dosePerLitreBn = "৪ গ্রাম ক্যালসিয়াম নাইট্রেট + ১ গ্রাম বোরণ প্রতি লিটার জলে",
                    dosePerBighaBn = "বিঘায় ৫০০ গ্রাম স্প্রে",
                    preHarvestIntervalDays = 0
                ),
                DiagnosticQuestionOption(
                    optionTextBn = "২. ফলের গায়ে ছিদ্র, ভেতরে পোকা ও পচন",
                    optionTextEn = "Boreholes on fruits, rot and frass inside",
                    diagnosisBn = "ফল ও ডগা ছিদ্রকারী পোকা (Fruit and Shoot Borer / Helicoverpa)।",
                    primaryCauseBn = "মথ ডিম পেড়ে কচি ফলের ভেতরে শুঁয়োপোকা প্রবেশ করে।",
                    firstActionBn = "আক্রান্ত ফল সংগ্রহ করে মাটিতে পুঁতে ফেলুন। বিঘায় ৫টি ফেরোমোন ফাঁদ লাগান।",
                    biologicalActionBn = "Bt (Bacillus thuringiensis) ২ গ্রাম/লিটার অথবা নিমতেল ৫ মিলি/লিটার।",
                    chemicalActiveIngredient = "Chlorantraniliprole 18.5% SC",
                    dosePerLitreBn = "০.৪ মিলি প্রতি লিটার জলে স্প্রে (১০ লিটারে ৪ মিলি)",
                    dosePerBighaBn = "বিঘায় ২০-২৫ মিলি স্প্রে",
                    preHarvestIntervalDays = 3
                ),
                DiagnosticQuestionOption(
                    optionTextBn = "৩. ফল মাঝখান দিয়ে ফেটে যাচ্ছে (Fruit Cracking)",
                    optionTextEn = "Fruits cracking / splitting open",
                    diagnosisBn = "বোরণের অভাব এবং দীর্ঘ খরার পর হঠাৎ ভারী সেচ বা বৃষ্টির জল।",
                    primaryCauseBn = "ফলের ভেতরের অংশ দ্রুত ফুলে ওঠা কিন্তু খোসা শক্ত থাকা।",
                    firstActionBn = "মাটিতে মলচিং (Mulching) করে নিয়মিত পরিমিত সেচ বজায় রাখুন।",
                    biologicalActionBn = "জৈব সার ও পটাশ সার সুষম অনুপাতে দিন।",
                    chemicalActiveIngredient = "Boron 20% (Solubor)",
                    dosePerLitreBn = "১.৫ গ্রাম প্রতি লিটার জলে স্প্রে",
                    dosePerBighaBn = "বিঘায় ১০০ গ্রাম স্প্রে",
                    preHarvestIntervalDays = 0
                )
            )
        ),
        DiagnosticSymptomTree(
            symptomId = "leaf_spots",
            titleBn = "কালো বা বাদামি দাগ (Leaf Spot)",
            titleEn = "Black & Brown Leaf Spots",
            iconEmoji = "🟤",
            initialQuestionBn = "দাগের আকার ও প্রকৃতি কেমন?",
            options = listOf(
                DiagnosticQuestionOption(
                    optionTextBn = "১. গোল গোল বাদামি দাগ ও দাগের মাঝে বলয় (রিং)",
                    optionTextEn = "Target-like circular concentric rings (Early Blight / Alternaria)",
                    diagnosisBn = "আগাম ধসা বা অল্টারনারিয়া পাতার দাগ (Early Blight / Alternaria Leaf Spot)।",
                    primaryCauseBn = "উষ্ণ ও ভেজা আবহাওয়া এবং পুরনো পাতার আর্দ্রতা।",
                    firstActionBn = "নিচের দাগযুক্ত পুরনো পাতা ছিঁড়ে পুড়িয়ে দিন। গাছের ওপর জল ছিটানো বন্ধ করুন।",
                    biologicalActionBn = "ট্রাইকোডার্মা ভিরিডি ৫ গ্রাম প্রতি লিটার জলে স্প্রে।",
                    chemicalActiveIngredient = "Azoxystrobin 18.2% + Difenoconazole 11.4% SC",
                    dosePerLitreBn = "১ মিলি প্রতি লিটার জলে স্প্রে",
                    dosePerBighaBn = "বিঘায় ৭০ মিলি স্প্রে",
                    preHarvestIntervalDays = 5
                ),
                DiagnosticQuestionOption(
                    optionTextBn = "২. জলে ভেজা গাঢ় খয়েরি দাগ দ্রুত পাতা ঝলসে দিচ্ছে",
                    optionTextEn = "Water-soaked dark lesions spreading rapidly (Late Blight)",
                    diagnosisBn = "নাবি ধসা রোগ (Late Blight - Phytophthora)।",
                    primaryCauseBn = "টানা কুয়াশাচ্ছন্ন, মেঘলা ও স্যাঁতসেঁতে ঠান্ডা আবহাওয়া (১৫-২২° সেলসিয়াস)।",
                    firstActionBn = "আক্রান্ত গাছ দ্রুত নষ্ট করুন। জমিতে অতিরিক্ত নাইট্রোজেন দেওয়া বন্ধ রাখুন।",
                    biologicalActionBn = "কপার অক্সিক্লোরাইড ৫০% ডব্লিউপি ২.৫ গ্রাম প্রতি লিটারে প্রতিরোধক হিসেবে স্প্রে।",
                    chemicalActiveIngredient = "Dimethomorph 50% WP + Mancozeb 75% WP",
                    dosePerLitreBn = "২ গ্রাম প্রতি লিটার জলে স্প্রে",
                    dosePerBighaBn = "বিঘায় ১৫০ গ্রাম স্প্রে",
                    preHarvestIntervalDays = 7
                )
            )
        ),
        DiagnosticSymptomTree(
            symptomId = "powdery_mildew",
            titleBn = "সাদা গুঁড়ো (Powdery Mildew)",
            titleEn = "White Powder on Leaves",
            iconEmoji = "⚪",
            initialQuestionBn = "সাদা গুঁড়ো পাতার কোন পিঠে দেখা যাচ্ছে?",
            options = listOf(
                DiagnosticQuestionOption(
                    optionTextBn = "১. পাতার ওপরের পিঠে ময়দার মতো সাদা আস্তরণ",
                    optionTextEn = "White flour-like powdery coating on upper leaf surface",
                    diagnosisBn = "পাউডারি মিলডিউ (Powdery Mildew ছত্রাক)।",
                    primaryCauseBn = "শুষ্ক দিন ও রাতে উচ্চ আর্দ্রতা বা কুয়াশা। ছায়াযুক্ত স্যাঁতসেঁতে জায়গা।",
                    firstActionBn = "বাতাস চলাচলের জন্য ঘন ডাল ছাঁটাই করুন। আক্রান্ত পুরনো পাতা সরিয়ে ফেলুন।",
                    biologicalActionBn = "বেকিং সোডা (৫ গ্রাম) + তরল সাবান (১ মিলি) প্রতি লিটার জলে স্প্রে।",
                    chemicalActiveIngredient = "Hexaconazole 5% SC / Wettable Sulphur 80% WP",
                    dosePerLitreBn = "২ মিলি হেক্সাকোনাজোল অথবা ৩ গ্রাম সালফার প্রতি লিটারে",
                    dosePerBighaBn = "বিঘায় ১০০ মিলি স্প্রে",
                    preHarvestIntervalDays = 7
                ),
                DiagnosticQuestionOption(
                    optionTextBn = "২. পাতার নিচে সাদা তুলো ও ওপরে কোণাকৃতি হলুদ দাগ",
                    optionTextEn = "Downy growth underneath, angular yellow spots on top",
                    diagnosisBn = "ডাউনি মিলডিউ (Downy Mildew ছত্রাক)।",
                    primaryCauseBn = "টানা বৃষ্টি ও বাতাসে উচ্চ আর্দ্রতা (৯০% এর বেশি)।",
                    firstActionBn = "মাচার উচ্চতা বৃদ্ধি করুন ও গোড়ায় জল জমা বন্ধ করুন।",
                    biologicalActionBn = "সিউডোমোনাস ফ্লুরোসেন্স (Pseudomonas fluorescens) ৫ গ্রাম প্রতি লিটার জলে।",
                    chemicalActiveIngredient = "Metalaxyl 8% + Mancozeb 64% WP",
                    dosePerLitreBn = "২.৫ গ্রাম প্রতি লিটার জলে স্প্রে",
                    dosePerBighaBn = "বিঘায় ২০০ গ্রাম স্প্রে",
                    preHarvestIntervalDays = 7
                )
            )
        ),
        DiagnosticSymptomTree(
            symptomId = "root_stem_rot",
            titleBn = "গাছ বা শিকড় পচা / ঢলে পড়া",
            titleEn = "Root Rot, Collar Rot & Wilt",
            iconEmoji = "🪵",
            initialQuestionBn = "গাছ কীভাবে শুকিয়ে মারা যাচ্ছে?",
            options = listOf(
                DiagnosticQuestionOption(
                    optionTextBn = "১. হঠাৎ সুস্থ সবুজ গাছ দিনের রোদে নেতিয়ে পড়ে ঢলে মরছে",
                    optionTextEn = "Green plant suddenly wilts under daytime sun",
                    diagnosisBn = "ব্যাকটেরিয়াল বা ফিউজারিয়াম ঢলে পড়া রোগ (Bacterial or Fusarium Wilt)।",
                    primaryCauseBn = "মাটিতে অতিরিক্ত জল জমে শিকড় পচন ও সংবহন নালী ব্লক হয়ে যাওয়া।",
                    firstActionBn = "আক্রান্ত গাছ তুলে ফেলুন ও গর্তে ব্লিচিং পাউডার বা চুন ছিটিয়ে দিন। ড্রেনেজ পরিষ্কার করুন।",
                    biologicalActionBn = "ট্রাইকোডার্মা ও সিউডোমোনাস মিশিয়ে গোড়ার মাটিতে ঢালুন (Drenching)।",
                    chemicalActiveIngredient = "Copper Oxychloride 50% WP + Streptocycline (Plantomycin)",
                    dosePerLitreBn = "৩ গ্রাম কপার + ০.১ গ্রাম স্ট্রেপ্টোসাইক্লিন প্রতি লিটার জলে গোড়ায় প্রয়োগ",
                    dosePerBighaBn = "গোড়ায় ড্রেঞ্চিং করুন",
                    preHarvestIntervalDays = 10
                ),
                DiagnosticQuestionOption(
                    optionTextBn = "২. মাটির কাছে কাণ্ডের গোড়া কালো হয়ে পচে চারা পড়ে যাচ্ছে",
                    optionTextEn = "Collar rot / Damping-off near soil level in seedlings",
                    diagnosisBn = "ড্যাম্পিং অফ বা কলার রট (Damping-off / Collar Rot)।",
                    primaryCauseBn = "বীজতলায় অতিরিক্ত জল ও চারা খুব ঘন হওয়া।",
                    firstActionBn = "বীজতলায় জল দেওয়া কমিয়ে রোদ লাগান। উঁচু বেডে চারা তৈরি করুন।",
                    biologicalActionBn = "বীজ শোধন: প্রতি কেজি বীজে ১০ গ্রাম ট্রাইকোডার্মা মিশিয়ে বপন।",
                    chemicalActiveIngredient = "Carbendazim 12% + Mancozeb 63% WP (Saaf)",
                    dosePerLitreBn = "২ গ্রাম প্রতি লিটার জলে স্প্রে ও গোড়া ভেজানো",
                    dosePerBighaBn = "বিঘায় ১৫০ গ্রাম স্প্রে",
                    preHarvestIntervalDays = 7
                )
            )
        )
    )

    val nutrientDeficiencies: List<NutrientDeficiencyInfo> = listOf(
        NutrientDeficiencyInfo(
            nutrientCode = "N",
            nutrientNameBn = "নাইট্রোজেন (Nitrogen)",
            nutrientNameEn = "Nitrogen (N)",
            affectedPartBn = "গাছের নিচের পুরনো পাতা",
            symptomsBn = "নিচের পুরনো পাতা সম্পূর্ণ ফ্যাকাশে হলুদ হয়ে যায়। গাছের বৃদ্ধি থমকে যায় এবং ডালপালা কম ছাড়ে।",
            solutionBn = "ইউরিয়া বা ১৯:১৯:১৯ এনপিকে সার উপরি প্রয়োগ করুন (বিঘায় ৫-৮ কেজি)।",
            organicRemedyBn = "ভালভাবে পচা গোবর সার, খোল পচা জল বা কেঁচো সার প্রয়োগ।"
        ),
        NutrientDeficiencyInfo(
            nutrientCode = "P",
            nutrientNameBn = "ফসফরাস (Phosphorus)",
            nutrientNameEn = "Phosphorus (P)",
            affectedPartBn = "নিচের পাতা ও কাণ্ড",
            symptomsBn = "পাতার নিচের পিঠ ও বোঁটায় বেগুনি বা লালচে আভা দেখা যায়। শিকড় বাড়তে চায় না এবং ফুল আসতে দেরি হয়।",
            solutionBn = "সিঙ্গেল সুপার ফসফেট (SSP) বা ডিএপি (DAP) মাটি তৈরির সময় বা গোড়ায় দিন।",
            organicRemedyBn = "হাড়ের গুঁড়ো (Bone meal) বা রক ফসফেট প্রয়োগ।"
        ),
        NutrientDeficiencyInfo(
            nutrientCode = "K",
            nutrientNameBn = "পটাশিয়াম (Potassium)",
            nutrientNameEn = "Potassium (K)",
            affectedPartBn = "পুরনো পাতার কিনারা",
            symptomsBn = "পাতার কিনারা ও ডগা পুড়ে যাওয়ার মতো বাদামি হয়ে কুঁকড়ে যায়। ফল ছোট হয় ও রোগ প্রতিরোধ ক্ষমতা কমে।",
            solutionBn = "মিউরেট অফ পটাশ (MOP) বা সালফেট অফ পটাশ (0:0:50) ৫ গ্রাম/লিটার স্প্রে।",
            organicRemedyBn = "কাঠকয়লা বা কাঠের ছাই গোড়ায় প্রয়োগ।"
        ),
        NutrientDeficiencyInfo(
            nutrientCode = "Ca",
            nutrientNameBn = "ক্যালসিয়াম (Calcium)",
            nutrientNameEn = "Calcium (Ca)",
            affectedPartBn = "কচি ডগা ও ফলের নিচের অংশ",
            symptomsBn = "কচি ডগার বৃদ্ধি বিন্দু মরে যায়। টমেটো ও লঙ্কায় ফলের নিচের অংশ কালো হয়ে পচে যায় (Blossom End Rot)।",
            solutionBn = "ক্যালসিয়াম নাইট্রেট ৫ গ্রাম প্রতি লিটার জলে স্প্রে বা জমিতে প্রয়োগ।",
            organicRemedyBn = "কলিচুন বা জিপসাম প্রয়োগ এবং নিয়মিত সেচ।"
        ),
        NutrientDeficiencyInfo(
            nutrientCode = "Mg",
            nutrientNameBn = "ম্যাগনেসিয়াম (Magnesium)",
            nutrientNameEn = "Magnesium (Mg)",
            affectedPartBn = "পুরনো পাতার শিরা মধ্যবর্তী অংশ",
            symptomsBn = "পুরনো পাতায় প্রধান শিরাগুলো সবুজ থাকে কিন্তু শিরাগুলোর মধ্যবর্তী অংশ হলুদ হয়ে যায়।",
            solutionBn = "ম্যাগনেসিয়াম সালফেট (Epsom Salt) ৫ গ্রাম প্রতি লিটার জলে স্প্রে।",
            organicRemedyBn = "ডলোমাইট চুনাপাথরের গুঁড়ো প্রয়োগ।"
        ),
        NutrientDeficiencyInfo(
            nutrientCode = "S",
            nutrientNameBn = "সালফার বা গন্ধক (Sulphur)",
            nutrientNameEn = "Sulphur (S)",
            affectedPartBn = "কচি ওপরের পাতা",
            symptomsBn = "নতুন কচি পাতা পুরো হালকা হলুদ হয়ে যায়। তেলবীজ ও ডাল ফসলে তেলের মান ও প্রোটিন কমে।",
            solutionBn = "বেন্টোনাইট সালফার (বিঘায় ৩-৪ কেজি) বা জলে দ্রবণীয় সালফার ৮০% ডব্লিউপি স্প্রে।",
            organicRemedyBn = "সরিষার খোল বা জিপসাম প্রয়োগ।"
        ),
        NutrientDeficiencyInfo(
            nutrientCode = "Zn",
            nutrientNameBn = "জিংক বা দস্তা (Zinc)",
            nutrientNameEn = "Zinc (Zn)",
            affectedPartBn = "কচি পাতা ও পর্ব",
            symptomsBn = "পাতার আকার ছোট হয়ে যায় (Little Leaf)। পর্বমধ্য খাটো হয়ে গাছ ঝোপের মতো দেখায়। ধানে খয়রা রোগ।",
            solutionBn = "চিলেটেড জিংক (Zn-EDTA 12%) ১.৫ গ্রাম প্রতি লিটার জলে স্প্রে।",
            organicRemedyBn = "গোবর সার ও জিংক সমৃদ্ধ কম্পোস্ট।"
        ),
        NutrientDeficiencyInfo(
            nutrientCode = "B",
            nutrientNameBn = "বোরণ (Boron)",
            nutrientNameEn = "Boron (B)",
            affectedPartBn = "ফুল, ফল ও কুঁড়ি",
            symptomsBn = "ফুল ও ফলের গুটি ঝরে যায়। ফল ফেটে যায় বা বিকৃত হয়। ফুলকপির মাথা বাদামি পচা হয়।",
            solutionBn = "বোরণ ২০% (Boron 20%) ১.৫ - ২ গ্রাম প্রতি লিটার জলে ফুল আসার আগে ও পরে স্প্রে।",
            organicRemedyBn = "বোরাক্স বা সুষম জৈব সার সেচসহ প্রয়োগ।"
        ),
        NutrientDeficiencyInfo(
            nutrientCode = "Fe",
            nutrientNameBn = "আয়রন বা লোহা (Iron)",
            nutrientNameEn = "Iron (Fe)",
            affectedPartBn = "কচি নতুন পাতা",
            symptomsBn = "কচি পাতা ধবধবে সাদাটে-হলুদ হয়ে যায় কিন্তু শিরাগুলো গাঢ় সবুজ রেখার মতো ফুটে থাকে।",
            solutionBn = "চিলেটেড আয়রন (Fe-EDTA 12%) ১.৫ গ্রাম প্রতি লিটার জলে স্প্রে।",
            organicRemedyBn = "সবুজ সার (ধৈঞ্চা) ও হিউমিক অ্যাসিড প্রয়োগ।"
        ),
        NutrientDeficiencyInfo(
            nutrientCode = "Mn",
            nutrientNameBn = "ম্যাঙ্গানিজ (Manganese)",
            nutrientNameEn = "Manganese (Mn)",
            affectedPartBn = "মাঝের বয়সের পাতা",
            symptomsBn = "পাতার শিরার মাঝে ছোট ছোট হলুদ ছোপ ও শুকনো বাদামি বিন্দু তৈরি হয় (Checkered pattern)।",
            solutionBn = "ম্যাঙ্গানিজ সালফেট ২ গ্রাম প্রতি লিটার জলে স্প্রে।",
            organicRemedyBn = "ভার্মিকম্পোস্ট ও ট্রাইকোডার্মা সংমিশ্রণ।"
        ),
        NutrientDeficiencyInfo(
            nutrientCode = "Cu",
            nutrientNameBn = "কপার বা তামা (Copper)",
            nutrientNameEn = "Copper (Cu)",
            affectedPartBn = "ডগা ও পাতার প্রান্ত",
            symptomsBn = "কচি ডগার বৃদ্ধি নষ্ট হয়ে ডাই-ব্যাক (ডগা শুকিয়ে মরা) হয়। পাতার প্রান্ত বিবর্ণ হয়ে যায়।",
            solutionBn = "কপার অক্সিক্লোরাইড ৫০% ডব্লিউপি ২ গ্রাম প্রতি লিটার জলে স্প্রে।",
            organicRemedyBn = "বোর্দো মিশ্রণ (Bordeaux mixture 1%) প্রয়োগ।"
        )
    )

    val fertilizerStages: List<FertilizerStagePlan> = listOf(
        FertilizerStagePlan(
            stageNameBn = "১. মূল সার (Basal Dose)",
            timingBn = "জমি তৈরির শেষ চাষে বীজ বপন বা চারা রোপণের পূর্বে",
            objectiveBn = "গাছের মূল বিস্তার ও প্রাথমিক খাদ্য সঞ্চয়",
            recommendedFertilizersBn = "পচা গোবর/কম্পোস্ট (বিঘায় ১ টন) + SSP (বিঘায় ৩৫ কেজি) বা DAP (বিঘায় ১৫ কেজি) + MOP (বিঘায় ১৫ কেজি) + নিমখোল (৩০ কেজি)।",
            cautionBn = "রাসায়নিক সার বীজের সরাসরি সংস্পর্শে দেবেন না। অন্তত ২-৩ ইঞ্চি নিচে মাটির সাথে মিশিয়ে দিন।"
        ),
        FertilizerStagePlan(
            stageNameBn = "২. প্রাথমিক বৃদ্ধি পর্যায় (Vegetative Stage)",
            timingBn = "রোপণের ১৫ থেকে ২৫ দিন পর",
            objectiveBn = "গাছের কাণ্ড, শাখা-প্রশাখা ও পাতা বৃদ্ধি",
            recommendedFertilizersBn = "ইউরিয়া (বিঘায় ৬-৮ কেজি) উপরি প্রয়োগ অথবা NPK ১৯:১৯:১৯ (৫ গ্রাম/লিটার স্প্রে) + জিংক চিলেটেড।",
            cautionBn = "শুকনো জমিতে ইউরিয়া দেবেন না। হালকা সেচ দিয়ে মাটিতে রস থাকা অবস্থায় দিন।"
        ),
        FertilizerStagePlan(
            stageNameBn = "৩. প্রাক-ফুল পর্যায় (Pre-Flowering Stage)",
            timingBn = "রোপণের ৩৫ থেকে ৪৫ দিন পর (কুঁড়ি আসার সময়)",
            objectiveBn = "প্রচুর ফুলের কুঁড়ি তৈরি ও ফুল ঝরা রোধ",
            recommendedFertilizersBn = "NPK ১২:৬১:০ (৫ গ্রাম/লিটার) + বোরণ ২০% (১.৫ গ্রাম/লিটার জলে স্প্রে)।",
            cautionBn = "এই সময় অতিরিক্ত নাইট্রোজেন বা ইউরিয়া দিলে গাছ বেশি পাতা ছেড়ে ফুল কমে যাবে।"
        ),
        FertilizerStagePlan(
            stageNameBn = "৪. ফল বৃদ্ধি পর্যায় (Fruit Development Stage)",
            timingBn = "রোপণের ৫০ থেকে ৭০ দিন পর (ফল বড় হওয়ার সময়)",
            objectiveBn = "ফলের ওজন, আকার, মিষ্টতা ও উজ্জ্বল রঙ বৃদ্ধি",
            recommendedFertilizersBn = "পটাশিয়াম সালফেট (0:0:50) ৫ গ্রাম/লিটার + ক্যালসিয়াম নাইট্রেট ৪ গ্রাম/লিটার স্প্রে।",
            cautionBn = "অনিয়মিত সেচ দেবেন না। খরা ও অতিরিক্ত জলের তারতম্যে ফল ফেটে যেতে পারে।"
        ),
        FertilizerStagePlan(
            stageNameBn = "৫. ফসল তোলা পর্যায় (Harvest & Flush Stage)",
            timingBn = "প্রথম দফা ফসল কাটার পর পরবর্তী ফ্লাশের জন্য",
            objectiveBn = "নতুন কুঁড়ি ও পরবর্তী ফলন বজায় রাখা",
            recommendedFertilizersBn = "হালকা খোল পচা জল বা অনুখাদ্য মিশ্রণ স্প্রে। চা বাগানে হালকা ইউরিয়া ও পটাশ স্প্রে।",
            cautionBn = "বিষ বা কীটনাশক স্প্রে করার অপেক্ষাকাল (PHI) নিশ্চিতভাবে মেনে তবেই ফসল তুলুন।"
        )
    )
}
