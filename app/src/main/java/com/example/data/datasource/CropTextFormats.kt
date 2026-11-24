package com.example.data.datasource

import com.example.data.model.AppLanguage
import com.example.data.model.CropCategory
import com.example.data.model.CropMasterGroup
import com.example.data.model.MasterCrop

data class CropLocaleData(
    val seasonHi: String,
    val seasonEn: String,
    val soilHi: String,
    val soilEn: String,
    val seedRateHi: String,
    val seedRateEn: String,
    val spacingHi: String,
    val spacingEn: String,
    val yieldHi: String,
    val yieldEn: String
)

object CropTextFormats {

    fun toAsciiDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            sb.append(
                when (ch) {
                    '০' -> '0'
                    '১' -> '1'
                    '২' -> '2'
                    '৩' -> '3'
                    '৪' -> '4'
                    '৫' -> '5'
                    '৬' -> '6'
                    '৭' -> '7'
                    '৮' -> '8'
                    '৯' -> '9'
                    else -> ch
                }
            )
        }
        return sb.toString()
    }

    fun fallbackFormat(bnText: String, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI) return bnText
        val ascii = toAsciiDigits(bnText)
        return when (lang) {
            AppLanguage.BENGALI -> bnText
            AppLanguage.HINDI -> ascii
                .replace("উপযুক্ত ঋতুতে চাষযোগ্য", "उपयुक्त मौसम में खेती योग्य")
                .replace("উর্বর দোআঁশ মাটি", "उपजाऊ दोमट मिट्टी")
                .replace("বিঘায় ১-২ কেজি বা চারা", "प्रति बीघा 1-2 किग्रा बीज या पौध")
                .replace("৬০ সেমি × ৪৫ সেমি", "60 सेमी × 45 सेमी")
                .replace("বিঘায় ১৫-২০ কুইন্টাল", "15-20 क्विंटल प्रति बीघा")
                .replace("প্রতি বিঘা", "प्रति बीघा")
                .replace("সেমি", "सेमी")
                .replace("মিটার", "मीटर")
                .replace("কেজি", "किग्रा")
                .replace("গ্রাম", "ग्राम")
                .replace("কুইন্টাল", "क्विंटल")
                .replace("দিন", "दिन")
            AppLanguage.ENGLISH -> ascii
                .replace("উপযুক্ত ঋতুতে চাষযোগ্য", "Suitable for cultivation in appropriate season")
                .replace("উর্বর দোআঁশ মাটি", "Fertile loamy soil")
                .replace("বিঘায় ১-২ কেজি বা চারা", "1-2 kg seed or seedlings per bigha")
                .replace("৬০ সেমি × ৪৫ সেমি", "60 cm × 45 cm")
                .replace("বিঘায় ১৫-২০ কুইন্টাল", "15-20 quintals per bigha")
                .replace("প্রতি বিঘা", "per bigha")
                .replace("বিঘায়", "per bigha")
                .replace("সেমি", "cm")
                .replace("মিটার", "m")
                .replace("কেজি", "kg")
                .replace("গ্রাম", "gm")
                .replace("কুইন্টাল", "quintals")
                .replace("দিন", "days")
        }
    }

    fun suitableClimate(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "উষ্ণ ও নাতিশীতোষ্ণ আর্দ্র আবহাওয়া (২০°-৩২° সেলসিয়াস)"
        AppLanguage.HINDI -> "उष्ण व समशीतोष्ण आर्द्र जलवायु (20°–32° सेल्सियस)"
        AppLanguage.ENGLISH -> "Warm and temperate humid climate (20°C–32°C)"
    }

    fun suitableClimateForCrop(crop: MasterCrop, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI && crop.suitableClimateBn != MasterCropDefaultBn.CLIMATE) {
            return crop.suitableClimateBn
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> "মৃদু উষ্ণ ও শীতল রোদযুক্ত আবহাওয়া (১৫°-৩০° সেলসিয়াস); বর্ষায় পলিহাউস বা শেডনেট সুরক্ষা উপকারী"
                AppLanguage.HINDI -> "सौम्य उष्ण व धूपयुक्त जलवायु (15°–30°C); भारी वर्षा में शेडनेट या पॉलीहाउस सुरक्षा लाभदायक"
                AppLanguage.ENGLISH -> "Mild sunny climate (15°C–30°C); rain-shelter or shade-net protection boosts cut-flower quality in heavy monsoon"
            }
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "ইনডোরে উজ্জ্বল পরোক্ষ আলো ও ১৮°-৩০° তাপমাত্রা; আউটডোর নার্সারিতে ৩০-৫০% গ্রিন শেডনেট"
                AppLanguage.HINDI -> "इंडोर में उज्ज्वल अप्रत्यक्ष प्रकाश (18°–30°C); नर्सरी में 30–50% ग्रीन शेडनेट"
                AppLanguage.ENGLISH -> "Bright indirect indoor light (18°C–30°C); 30–50% green shade-net for outdoor nursery hardening"
            }
            CropMasterGroup.FRUITS, CropMasterGroup.PLANTATION_COMMERCIAL -> when (lang) {
                AppLanguage.BENGALI -> "উপ-ক্রান্তীয় থেকে নাতিশীতোষ্ণ আর্দ্র আবহাওয়া (১৮°-৩৫° সেলসিয়াস) — প্রজাতিভেদে পার্বত্য/সমতল উপযোগী"
                AppLanguage.HINDI -> "उपोष्ण से समशीतोष्ण आर्द्र जलवायु (18°–35°C) — प्रजाति अनुसार मैदानी व पहाड़ी क्षेत्रों हेतु उपयुक्त"
                AppLanguage.ENGLISH -> "Subtropical to mild temperate humid climate (18°C–35°C) suited to plains or foothills by species"
            }
            CropMasterGroup.OTHER_AGROFORESTRY -> when (lang) {
                AppLanguage.BENGALI -> "উষ্ণ ও মৌসুমি বৃষ্টিপাতযুক্ত আবহাওয়া (২০°-৩৮° সেলসিয়াস); খরা সহনশীল"
                AppLanguage.HINDI -> "उष्ण व मानसूनी जलवायु (20°–38°C); सूखे के प्रति सहनशील"
                AppLanguage.ENGLISH -> "Warm monsoon-fed climate (20°C–38°C) with strong drought and rain resilience"
            }
            else -> suitableClimate(lang)
        }
    }

    fun recommendedPh(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "৬.০ - ৬.৮ (উর্বর দোআঁশ)"
        AppLanguage.HINDI -> "6.0 – 6.8 (उपजाऊ दोमट मिट्टी)"
        AppLanguage.ENGLISH -> "6.0 – 6.8 (Fertile loamy soil)"
    }

    fun recommendedPhForCrop(crop: MasterCrop, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI && crop.recommendedPhBn != MasterCropDefaultBn.PH) {
            return crop.recommendedPhBn
        }
        return when (crop.category) {
            CropCategory.TEA, CropCategory.SPICES -> when (lang) {
                AppLanguage.BENGALI -> "৪.৮ - ৬.২ (হালকা অম্লীয়, হিউমাস সমৃদ্ধ)"
                AppLanguage.HINDI -> "4.8 – 6.2 (हल्की अम्लीय व ह्यूमस युक्त मिट्टी)"
                AppLanguage.ENGLISH -> "4.8 – 6.2 (Slightly acidic, humus-rich soil)"
            }
            CropCategory.NURSERY_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "৫.৮ - ৬.৮ (ঝুরঝুরে পটিং মিডিয়া)"
                AppLanguage.HINDI -> "5.8 – 6.8 (भुरभुरा पॉटिंग मिश्रण)"
                AppLanguage.ENGLISH -> "5.8 – 6.8 (Well-aerated potting mix)"
            }
            else -> recommendedPh(lang)
        }
    }

    fun landPrep(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "জমি ৩-৪ বার গভীর চাষ দিয়ে মই দিয়ে ঝুরঝুরে ও আগাছামুক্ত করুন। বিঘায় ১-১.৫ টন পচা গোবর সার বা কম্পোস্ট মেশান।"
        AppLanguage.HINDI -> "खेत की 3–4 बार गहरी जुताई कर पाटा चलाएं और खरपतवार मुक्त व भुरभुरा बनाएं। प्रति बीघा 1–1.5 टन सड़ी गोबर खाद या कम्पोस्ट मिलाएं।"
        AppLanguage.ENGLISH -> "Plough deeply 3–4 times, level cleanly and keep weed-free and friable. Mix 1–1.5 tonnes of well-rotted FYM or compost per bigha."
    }

    fun landPrepForCrop(crop: MasterCrop, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI && crop.landPrepBn != MasterCropDefaultBn.LAND_PREP) {
            return crop.landPrepBn
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> "জমি গভীর চাষ করে ১৫-২০ সেমি উঁচু ও জল নিকাশীযুক্ত ১ মিটার চওড়া বেড তৈরি করুন। গোবর সার ও নিম খোল মেশান।"
                AppLanguage.HINDI -> "गहरी जुताई कर 15–20 सेमी ऊंची व जल निकासी वाली 1 मीटर चौड़ी क्यारियां बनाएं। सड़ी गोबर खाद व नीम खली मिलाएं।"
                AppLanguage.ENGLISH -> "Till deeply and form 15–20 cm raised, well-drained 1 m wide flower beds enriched with well-rotted FYM and Neem cake."
            }
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "মাটি ও টব প্রস্তুতি: ৪০% দোআঁশ মাটি + ৩০% ভার্মিকম্পোস্ট + ২০% কোকোপিট + ১০% মোটা বালি/পার্লাইট মিশিয়ে জীবাণুমুক্ত করুন।"
                AppLanguage.HINDI -> "पॉटिंग मिक्स तैयारी: 40% दोमट मिट्टी + 30% केंचुआ खाद + 20% कोकोपीट + 10% मोटी रेत/परलाइट मिलाकर गमला भरें।"
                AppLanguage.ENGLISH -> "Potting Media Prep: Blend 40% garden loam + 30% vermicompost + 20% cocopeat + 10% coarse sand/perlite with clean pot drainage."
            }
            CropMasterGroup.FRUITS -> when (lang) {
                AppLanguage.BENGALI -> "রোপণের ১ মাস আগে প্রজাতি অনুযায়ী ৬০×৬০×৬০ সেমি বা ১×১×১ মিটার গর্ত খুঁড়ে রোদে শুকিয়ে ২০-২৫ কেজি গোবর সার, নিম খোল ও মাটি মিশিয়ে ভরুন।"
                AppLanguage.HINDI -> "रोपाई से 1 माह पूर्व 60×60×60 सेमी या 1×1×1 मी के गड्ढे खोदकर धूप दिखाएं और 20–25 किग्रा सड़ी गोबर खाद, नीम खली व मिट्टी भरें।"
                AppLanguage.ENGLISH -> "Dig 60×60×60 cm or 1×1×1 m orchard pits 1 month prior; solarize and refill with topsoil, 20–25 kg FYM, and Neem cake."
            }
            CropMasterGroup.OTHER_AGROFORESTRY -> when (lang) {
                AppLanguage.BENGALI -> "সবুজ সারের ক্ষেত্রে হালকা ১-২টি চাষ দিন; কৃষি-বান্ধব কাঠ বা বৃক্ষের ক্ষেত্রে ৪৫×৪৫×৪৫ সেমি গর্ত করে সার-মাটি মিশিয়ে ভরুন।"
                AppLanguage.HINDI -> "हरी खाद के लिए 1–2 हल्की जुताई करें; कृषि वानिकी पेड़ों के लिए 45×45×45 सेमी के गड्ढे तैयार कर खाद-मिट्टी भरें।"
                AppLanguage.ENGLISH -> "Give 1–2 light ploughings for green manure/fodder; prepare 45×45×45 cm compost-filled pits for timber/agroforestry trees."
            }
            else -> landPrep(lang)
        }
    }

    fun sowingMethod(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "বীজ শোধন (ট্রাইকোডার্মা দিয়ে) করে নির্দিষ্ট দূরত্বে লাইন দিয়ে বপন করুন।"
        AppLanguage.HINDI -> "ट्राइकोडर्मा से बीज उपचारित करके निर्धारित दूरी पर कतार में बुवाई करें।"
        AppLanguage.ENGLISH -> "Treat seeds with Trichoderma and sow in well-spaced rows at recommended spacing."
    }

    fun sowingMethodForCrop(crop: MasterCrop, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI && crop.sowingMethodBn != MasterCropDefaultBn.SOWING) {
            return crop.sowingMethodBn
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> "জাতিভেদে কন্দ/বাল্ব, শিকড়যুক্ত ডালের কাটিং (Stem cutting) বা বীজতলার চারা উঁচু বেডে নির্দিষ্ট দূরত্বে রোপণ করুন।"
                AppLanguage.HINDI -> "प्रजाति के अनुसार कंद/बल्ब, जड़युक्त कटिंग या नर्सरी पौध को उठी हुई क्यारी में निर्धारित दूरी पर लगाएं।"
                AppLanguage.ENGLISH -> "Plant certified bulbs, rooted stem cuttings, or hardened plugs on raised beds at crop-specific spacing."
            }
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "গোড়া বিভাজন (Division), ডাল কাটিং বা পাতার কাটিং ব্যবহার করে গ্রো-ব্যাগ/টবে জল নিকাশী লেয়ারের উপর বসান।"
                AppLanguage.HINDI -> "कंद विभाजन, तना/पत्ती कटिंग या ग्राफ्टेड पौध को जल निकासी वाले गमले या ग्रो-बैग में लगाएं।"
                AppLanguage.ENGLISH -> "Propagate via offsets, stem cuttings, or grafting into drainage-layered pots or nursery poly-bags."
            }
            CropMasterGroup.FRUITS -> when (lang) {
                AppLanguage.BENGALI -> "উন্নত কলম (Graft / Air Layer) বা টিস্যু কালচার চারা গর্তের মাঝখানে পলিথিন খুলে গ্রাফট জয়েন্ট মাটির উপরে রেখে বসান।"
                AppLanguage.HINDI -> "गड्ढे के बीचोंबीच पॉलिथीन हटाकर कलमी या टिश्यू कल्चर पौध लगाएं; ग्राफ्ट जोड़ जमीन से ऊपर रखें।"
                AppLanguage.ENGLISH -> "Remove poly-bag carefully and plant grafted or tissue-cultured saplings in pit center keeping the graft union above soil level."
            }
            else -> sowingMethod(lang)
        }
    }

    fun nurseryMethod(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "উঁচু বীজতলায় পলিটানেল বা শেডনেটের নিচে চারা তৈরি করুন। ২৫-৩০ দিন বয়সে চারা রোপণ উপযোগী হয়।"
        AppLanguage.HINDI -> "उठी हुई क्यारी (नर्सरी बेड) में शेडनेट या पॉलीटन्नल के नीचे पौध तैयार करें। 25–30 दिन में पौध रोपाई के लिए तैयार हो जाती है।"
        AppLanguage.ENGLISH -> "Raise healthy seedlings on raised nursery beds under shade net or poly-tunnel. Seedlings are ready for transplanting in 25–30 days."
    }

    fun nurseryMethodForCrop(crop: MasterCrop, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI && crop.nurseryMethodBn != MasterCropDefaultBn.NURSERY) {
            return crop.nurseryMethodBn
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> "প্রো-ট্রে (কোকোপিট ও ভার্মিকম্পোস্ট) অথবা মিস্ট চেম্বারে বীজ ও ডালের কাটিংয়ে শিকড় গজিয়ে ২৫-৩০ দিনে মূল বেডে বসান।"
                AppLanguage.HINDI -> "कोकोपीट प्रो-ट्रे या मिस्ट चेंबर में बीज व कटिंग से जड़ें विकसित कर 25–30 दिन में मुख्य क्यारी में रोपें।"
                AppLanguage.ENGLISH -> "Raise flower plugs in cocopeat pro-trays or root stem cuttings under mist/shade-net before bed planting."
            }
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "৫০% শেডনেটের নিচে জীবাণুমুক্ত কোকোপিট-বালি বেডে শিকড় ও কলম তৈরি করে ৬×৮ বা ১০×১০ পলি-ব্যাগে স্থানান্তর করুন।"
                AppLanguage.HINDI -> "50% शेडनेट के नीचे कोकोपीट-रेत में जड़ें व कलम तैयार कर पॉलीबैग या गमले में स्थानांतरित करें।"
                AppLanguage.ENGLISH -> "Root cuttings and graft rootstocks in sterile cocopeat-sand beds under 50% shade-net before potting into poly-bags."
            }
            CropMasterGroup.FRUITS -> when (lang) {
                AppLanguage.BENGALI -> "সরাসরি শংসিত উদ্যানপালন নার্সারি থেকে সুস্থ কলম/গুটি বা টিস্যু কালচার চারা সংগ্রহ করা উত্তম (সাধারণত বীজ থেকে ফল দেরিতে ও গুণমানে ভিন্ন হয়)।"
                AppLanguage.HINDI -> "सही किस्म व जल्दी फलन के लिए प्रमाणित नर्सरी से ही स्वस्थ कलमी या टिश्यू कल्चर पौधे प्राप्त करें।"
                AppLanguage.ENGLISH -> "Source true-to-type grafted, air-layered, or tissue-culture saplings from certified horticulture nurseries."
            }
            CropMasterGroup.CEREALS_GRAINS, CropMasterGroup.PULSES_LEGUMES, CropMasterGroup.OILSEEDS -> when (lang) {
                AppLanguage.BENGALI -> "প্রধানত সরাসরি ক্ষেতে বপন হয় (ধানের ক্ষেত্রে কাদা বা শুকনো উঁচু বীজতলায় ২০-২৫ দিনের চারা তৈরি করা হয়)।"
                AppLanguage.HINDI -> "अधिकांशतः सीधी बुवाई की जाती है (धान के लिए नर्सरी में 20–25 दिन की पौध तैयार की जाती है)।"
                AppLanguage.ENGLISH -> "Mostly direct-seeded in main fields (for Paddy, raise 20–25 day wet/dry nursery beds before transplanting)."
            }
            else -> nurseryMethod(lang)
        }
    }

    fun transplanting(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "পড়ন্ত বিকেলে জমিতে পর্যাপ্ত রস থাকা অবস্থায় চারা রোপণ করুন। রোপণের পরপরই হালকা সেচ দিন।"
        AppLanguage.HINDI -> "शाम के समय खेत में पर्याप्त नमी होने पर पौध की रोपाई करें। रोपाई के तुरंत बाद हल्की सिंचाई दें।"
        AppLanguage.ENGLISH -> "Transplant seedlings in the late afternoon when soil has sufficient moisture, and give light irrigation immediately."
    }

    fun transplantingForCrop(crop: MasterCrop, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI && crop.transplantingBn != MasterCropDefaultBn.TRANSPLANT) {
            return crop.transplantingBn
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "পট পরিবর্তনের (Repotting) সময় শিকড় না ছিঁড়ে ১-২ ইঞ্চি বড় টবে নতুন পটিং মিক্স সহ বসিয়ে পরোক্ষ আলোয় রাখুন।"
                AppLanguage.HINDI -> "रिपॉटिंग के समय जड़ों को नुकसान पहुंचाए बिना 1–2 इंच बड़े गमले में ताजा पॉटिंग मिक्स के साथ लगाएं और हल्की छांव में रखें।"
                AppLanguage.ENGLISH -> "When repotting, transfer root balls gently into a pot 1–2 inches wider with fresh mix and keep shaded for 3–4 days."
            }
            CropMasterGroup.FRUITS -> when (lang) {
                AppLanguage.BENGALI -> "বর্ষার শুরুতে (জুলাই-আগস্ট) বা বসন্তে গর্তে চারা বসিয়ে মাটি চেপে দিন ও বাতাস থেকে বাঁচাতে বাঁশের খুঁটি (Staking) দিন।"
                AppLanguage.HINDI -> "मानसून के आरंभ या वसंत में गड्ढे में पौध रोपकर मिट्टी दबाएं और हवा से बचाने के लिए बांस का सहारा दें।"
                AppLanguage.ENGLISH -> "Plant during early monsoon or spring, firm the soil around the collar, water thoroughly, and stake against wind."
            }
            else -> transplanting(lang)
        }
    }

    fun irrigationSchedule(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "মাটিতে সর্বদা আর্দ্রতা রাখুন। তবে জল যেন গোড়ায় না জমে। ফুল ও ফল আসার সময় নিয়মিত সেচ দিন।"
        AppLanguage.HINDI -> "मिट्टी में नियमित नमी बनाए रखें, लेकिन जड़ों के पास पानी न भरने दें। फूल और फल आने के समय नियमित सिंचाई करें।"
        AppLanguage.ENGLISH -> "Maintain uniform soil moisture without waterlogging at the root zone. Irrigate regularly during flowering and fruit set."
    }

    fun irrigationScheduleForCrop(crop: MasterCrop, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI && crop.irrigationScheduleBn != MasterCropDefaultBn.IRRIGATION) {
            return crop.irrigationScheduleBn
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> "ড্রিপ বা ঝাঁঝরি সেচ সেরা; পাতা ও কুঁড়ি অতিরিক্ত ভেজাবেন না (ছত্রাক রোগ এড়াতে সকালে গোড়ায় সেচ দিন)।"
                AppLanguage.HINDI -> "ड्रिप या जड़ों के पास हल्की सिंचाई उत्तम है; कली और पत्तियों को गीला होने से बचाने हेतु सुबह सिंचाई करें।"
                AppLanguage.ENGLISH -> "Prefer morning drip or root-zone irrigation; avoid wetting buds/foliage late evening to prevent Botrytis blight."
            }
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "টবের উপরের ১-২ ইঞ্চি মাটি শুকালে তবেই জল দিন (সাকুলেন্ট/ক্যাকটাসে সপ্তাহে ১-২ বার মাত্র)। জমে থাকা জল ফেলে দিন।"
                AppLanguage.HINDI -> "गमले की ऊपरी 1–2 इंच मिट्टी सूखने पर ही पानी दें (कैक्टस व सकुलेंट में सप्ताह में 1 बार)। प्लेट में अतिरिक्त पानी न जमने दें।"
                AppLanguage.ENGLISH -> "Water only when the top 1–2 inches of potting mix dry out (cacti/succulents need sparse watering). Empty saucer excess."
            }
            CropMasterGroup.FRUITS -> when (lang) {
                AppLanguage.BENGALI -> "প্রথম ২ বছর রিং বেসিন বা ড্রিপ সেচ দিন; ফুল ফোটার আগে সামান্য জলের টান (Stress) দিয়ে ফল বাঁধার পর নিয়মিত সেচ দিন।"
                AppLanguage.HINDI -> "शुरुआती 2 वर्षों में थाला या ड्रिप सिंचाई दें; बौर से पहले हल्का तनाव व फल लगने के बाद नियमित सिंचाई करें।"
                AppLanguage.ENGLISH -> "Irrigate via ring basins or drip in early orchard years; resume steady moisture strictly once fruit sets and develops."
            }
            else -> irrigationSchedule(lang)
        }
    }

    fun organicManure(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "গোবর সার বিঘায় ৫০০-১০০০ কেজি, ট্রাইকোডার্মা ও নিম খোল ৫০ কেজি জমি তৈরির শেষ চাষে মেশান।"
        AppLanguage.HINDI -> "अंतिम जुताई के समय प्रति बीघा 500–1000 किग्रा सड़ी गोबर खाद, ट्राइकोडर्मा और 50 किग्रा नीम की खली मिलाएं।"
        AppLanguage.ENGLISH -> "Incorporate 500–1000 kg FYM, Trichoderma, and 50 kg Neem Cake per bigha during final land preparation."
    }

    fun organicManureForCrop(crop: MasterCrop, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI && crop.organicManureBn != MasterCropDefaultBn.ORGANIC) {
            return crop.organicManureBn
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "মাসে একবার টব প্রতি ২ মুঠো ভার্মিকম্পোস্ট বা পচা পাতা সার ও এক চিমটি নিম খোল মাটির সাথে হালকা মিশিয়ে দিন।"
                AppLanguage.HINDI -> "महीने में एक बार प्रति गमला 2 मुट्ठी केंचुआ खाद या पत्तियों की खाद व चुटकी भर नीम खली मिलाएं।"
                AppLanguage.ENGLISH -> "Top-dress monthly with 2 handfuls of vermicompost or leaf-mould and a pinch of Neem cake per pot."
            }
            else -> organicManure(lang)
        }
    }

    fun fertilizerSchedule(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "মূল সার: SSP বা DAP এবং MOP। বৃদ্ধির সময় ইউরিয়া ও NPK ১৯:১৯:১৯। ফুল আসার আগে বোরণ ও পটাশ।"
        AppLanguage.HINDI -> "आधार खाद: SSP या DAP और MOP। बढ़वार के समय यूरिया व NPK 19:19:19, तथा फूल आने से पहले बोरॉन और पोटाश दें।"
        AppLanguage.ENGLISH -> "Basal: SSP/DAP and MOP. Vegetative growth: Urea & NPK 19:19:19 foliar spray. Pre-flowering: Boron and Potash."
    }

    fun fertilizerScheduleForCrop(crop: MasterCrop, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI && crop.fertilizerScheduleBn != MasterCropDefaultBn.FERTILIZER) {
            return crop.fertilizerScheduleBn
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> "শুরুতে সুষম NPK (১৯:১৯:১৯) ও পচা গোবর; কুঁড়ি আসার সময় ফসফরাস-পটাশ সমৃদ্ধ (০:৫২:৩৪ ও ১৩:০:৪৫) ব্যবহারে রঙ ও ডাঁটা শক্ত হয়।"
                AppLanguage.HINDI -> "बढ़वार में संतुलित NPK (19:19:19); कली बनते समय पोटाश व फॉस्फोरस (0:52:34 व 13:0:45) से फूलों का रंग व डंठल मजबूत होता है।"
                AppLanguage.ENGLISH -> "Use balanced NPK (19:19:19) during vegetative flush, switching to 0:52:34 & 13:0:45 at bud initiation for bright sturdy stems."
            }
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "শীতকালে কম সার দিন; গরম ও বর্ষায় মাসে ১ বার পাতলা জৈব তরল সার বা NPK ১৯:১৯:১৯ (১ গ্রাম/লিটার) দিন। প্রয়োজনে স্থানীয় নার্সারিতে যাচাই করুন।"
                AppLanguage.HINDI -> "सर्दियों में कम खाद दें; गर्मी-बरसात में माह में एक बार हल्का NPK 19:19:19 (1 ग्राम/लीटर) या तरल जैविक खाद दें।"
                AppLanguage.ENGLISH -> "Reduce feeding in winter dormancy; apply dilute balanced liquid feed or NPK 19:19:19 (1 g/L) monthly during active growth."
            }
            CropMasterGroup.OTHER_AGROFORESTRY -> when (lang) {
                AppLanguage.BENGALI -> "সবুজ সার ও শিম্বগোত্রীয় গাছে বাতাস থেকেই নাইট্রোজেন সংগ্রহ হয়; রোপণে সামান্য ফসফেট (SSP) ও বর্ষায় পটাশ দিলেই যথেষ্ট।"
                AppLanguage.HINDI -> "दलहनी हरी खाद स्वयं नाइट्रोजन स्थिर करती है; केवल बुवाई में हल्का फॉस्फोरस (SSP) पर्याप्त होता है।"
                AppLanguage.ENGLISH -> "Leguminous green manures fix atmospheric nitrogen; only light basal phosphorus (SSP) is typically needed—confirm locally."
            }
            else -> fertilizerSchedule(lang)
        }
    }

    fun floweringGuidance(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "ফুল আসার সময় অতিরিক্ত নাইট্রোজেন সার দেবেন না। বোরণ (Boron 20%) ২ গ্রাম প্রতি লিটারে স্প্রে করলে ফুল ঝরা বন্ধ হয়।"
        AppLanguage.HINDI -> "फूल आने के समय अधिक नाइट्रोजन खाद न दें। बोरॉन 20% (2 ग्राम/लीटर पानी) का छिड़काव करने से फूल झड़ना रुकता है।"
        AppLanguage.ENGLISH -> "Avoid excess nitrogen during flowering. Spray Boron 20% at 2 gm/litre of water to prevent blossom drop."
    }

    fun floweringGuidanceForCrop(crop: MasterCrop, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI && crop.floweringGuidanceBn != MasterCropDefaultBn.FLOWERING) {
            return crop.floweringGuidanceBn
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> "চারা স্থাপনের ৩-৪ সপ্তাহ পর ডগা ভেঙে দিলে (Pinching) প্রচুর পাশ্বর্-শাখা ও কুঁড়ি বের হয়; বড় ফুলের জন্য পাশের ছোট কুঁড়ি ছাঁটাই (Disbudding) করুন।"
                AppLanguage.HINDI -> "रोपाई के 3–4 सप्ताह बाद पिंचिंग (शीर्ष कलिका तोड़ना) करने से अधिक शाखाएं व फूल आते हैं; बड़े फूल के लिए डिस्बडिंग करें।"
                AppLanguage.ENGLISH -> "Pinch terminal shoots 3–4 weeks after planting for bushy branching; disbud side buds where single large blooms are desired."
            }
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "অধিকাংশ ইনডোর গাছ পাতার সৌন্দর্যের জন্য চাষ হয়; শুকনো পাতা ছেঁটে দিন ও টব ঘুরিয়ে সমান আলো দিন। ফুলদায়ী জাত হলে সকালের হালকা রোদ দিন।"
                AppLanguage.HINDI -> "अधिकांश इंडोर पौधे पत्तियों की सुंदरता के लिए उगाए जाते हैं; सूखी पत्तियां छांटें और गमले को घुमाते रहें ताकि समान प्रकाश मिले।"
                AppLanguage.ENGLISH -> "Most indoor ornamentals are grown for foliage; trim yellowing leaves, rotate pots for even light, and give morning sun for flowering pots."
            }
            CropMasterGroup.OTHER_AGROFORESTRY -> when (lang) {
                AppLanguage.BENGALI -> "সবুজ সার (যেমন ধইঞ্চা/শণ) ফুল ফোটার ঠিক আগে অর্থাৎ ৪০-৪৫ দিনে মাটিতে চাষ করে মিশিয়ে দিন; এতে সর্বোচ্চ নাইট্রোজেন পাওয়া যায়।"
                AppLanguage.HINDI -> "हरी खाद वाली फसलों को फूल आने से ठीक पहले (40–45 दिन) खेत में पलट दें ताकि मिट्टी को अधिकतम नाइट्रोजन मिले।"
                AppLanguage.ENGLISH -> "Plough green manure biomass into soil right before flowering (40–45 days) for peak nitrogen mineralization; trees focus on stem form."
            }
            else -> floweringGuidance(lang)
        }
    }

    fun fruitingGuidance(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "ফল বৃদ্ধির সময় পটাশিয়াম (0:0:50) এবং ক্যালসিয়াম নাইট্রেট প্রয়োগ করুন। ফলের আকার ও উজ্জ্বলতা বাড়ে।"
        AppLanguage.HINDI -> "फल बढ़ने के समय पोटाशियम (0:0:50) और कैल्शियम नाइट्रेट दें। इससे फल का आकार, वजन और चमक बढ़ती है।"
        AppLanguage.ENGLISH -> "Apply Sulphate of Potash (0:0:50) and Calcium Nitrate during fruit development for better size, weight, and gloss."
    }

    fun fruitingGuidanceForCrop(crop: MasterCrop, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI && crop.fruitingGuidanceBn != MasterCropDefaultBn.FRUITING) {
            return crop.fruitingGuidanceBn
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> "ফুল কাটার পর পরিষ্কার জলে ২% সুক্রোজ ও সামান্য সাইট্রিক অ্যাসিড দ্রবণে ডাঁটা রাখলে ফুলদানি আয়ু (Vase Life) অনেক বাড়ে।"
                AppLanguage.HINDI -> "फूल काटने के बाद साफ पानी में रखने व पोटाश पोषण से पंखुड़ियों का रंग गहरा व फूलदानी आयु (Vase Life) लंबी होती है।"
                AppLanguage.ENGLISH -> "Condition cut stems immediately in clean water; balanced potash & calcium improve petal firmness and post-harvest vase life."
            }
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "প্রতি বর্ষার আগে বা বছরে একবার শিকড় ছাঁটাই ও পরিচ্ছন্ন ক্যানোপি শেপিং করুন যাতে গাছ ঝোপালো ও সতেজ থাকে।"
                AppLanguage.HINDI -> "साल में एक बार या मानसून से पहले हल्की छंटाई व आकार देने से नर्सरी व सजावटी पौधे घने व स्वस्थ रहते हैं।"
                AppLanguage.ENGLISH -> "Carry out annual structural pruning before monsoon to maintain compact foliage density, air circulation, and nursery sale appeal."
            }
            CropMasterGroup.OTHER_AGROFORESTRY -> when (lang) {
                AppLanguage.BENGALI -> "কাঠ জাতীয় বৃক্ষের নিচের ডালপালা নিয়মিত ছাঁটাই (Prune) করলে সোজা, গিঁট-মুক্ত মূল্যবান গুঁড়ি (Timber Log) তৈরি হয়।"
                AppLanguage.HINDI -> "इमारती लकड़ी वाले पेड़ों की निचली शाखाओं की समय पर छंटाई से सीधा व मजबूत मुख्य तना बनता है।"
                AppLanguage.ENGLISH -> "Prune lower side branches of agroforestry timber trees annually to produce straight, knot-free high-value boles."
            }
            else -> fruitingGuidance(lang)
        }
    }

    fun storage(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "ঠান্ডা, ছায়াযুক্ত ও বাতাস চলাচলকারী শুকনো জায়গায় সংরক্ষণ করুন।"
        AppLanguage.HINDI -> "ठंडी, छायादार और हवादार सूखी जगह पर सुरक्षित भंडारण करें।"
        AppLanguage.ENGLISH -> "Store harvested produce in a cool, shaded, dry, and well-ventilated place."
    }

    fun storageForCrop(crop: MasterCrop, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI && crop.storageBn != MasterCropDefaultBn.STORAGE) {
            return crop.storageBn
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> "ফুল তোলার পরপরই ছায়া ও ঠান্ডা জলের বালতিতে প্রি-কুলিং করে ভিজা কাগজে মুড়ে শীতল পরিবহন করুন।"
                AppLanguage.HINDI -> "कटाई के तुरंत बाद ठंडे पानी में प्री-कूलिंग करें और गीले कागज/बक्सों में छाया में पैक कर भेजें।"
                AppLanguage.ENGLISH -> "Pre-cool cut flowers in clean cold water buckets immediately; pack loosely in ventilated sleeves under shade."
            }
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "চারা বা টব ছায়াযুক্ত প্রদর্শন স্থলে সাজিয়ে রাখুন ও পরিবহনের আগে হালকা জল স্প্রে করে কার্ডবোর্ড/জালি ক্যারেটে প্যাক করুন।"
                AppLanguage.HINDI -> "पौधों को छायादार स्थान पर रखें और बिक्री/परिवहन से पहले हल्की सिंचाई कर सुरक्षित क्रेट में पैक करें।"
                AppLanguage.ENGLISH -> "Keep potted plants under filtered shade and lightly mist root balls before crating for transport."
            }
            else -> storage(lang)
        }
    }

    fun marketInfo(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "স্থানীয় হাট ও পাইকারি বাজারে সকালের দিকে তাজা গ্রেডিং করা ফসল ভালো দামে বিক্রি হয়।"
        AppLanguage.HINDI -> "सुबह के समय स्थानीय हाट और थोक मंडी में ताजी व ग्रेडिंग की हुई उपज का अच्छा भाव मिलता है।"
        AppLanguage.ENGLISH -> "Freshly graded produce sold in local haats and wholesale mandis early morning fetches premium prices."
    }

    fun marketInfoForCrop(crop: MasterCrop, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI && crop.marketInfoBn != MasterCropDefaultBn.MARKET) {
            return crop.marketInfoBn
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> "বিবাহ ও উৎসব মৌসুমে (দুর্গাপূজা, দীপাবলি, শীতকাল) ডাঁটার দৈর্ঘ্য অনুযায়ী গ্রেডিং করলে মল্লিকঘাট বা স্থানীয় ফুল বাজারে চড়া দাম মেলে।"
                AppLanguage.HINDI -> "त्योहार व शादी के सीजन में डंठल की लंबाई अनुसार ग्रेडिंग करने पर फूल मंडी में उच्चतम भाव मिलता है।"
                AppLanguage.ENGLISH -> "Grading by stem length and bud freshness yields strong margins at regional flower mandis during festive and wedding seasons."
            }
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> "কৃষক সাথী নার্সারি মার্কেটপ্লেস, ল্যান্ডস্কেপ ঠিকাদার ও স্থানীয় উদ্যানপ্রেমীদের কাছে টবসহ সুন্দরভাবে উপস্থাপন করলে ভালো মুনাফা হয়।"
                AppLanguage.HINDI -> "नर्सरी मार्केट, लैंडस्केप ठेकेदारों व बागवानी ग्राहकों को स्वस्थ और सजाए गए गमलों के साथ बेचने पर अच्छा लाभ मिलता है।"
                AppLanguage.ENGLISH -> "Strong direct-to-consumer margins through verified nursery listings, landscaping contracts, and urban indoor-plant buyers."
            }
            else -> marketInfo(lang)
        }
    }

    fun commonDeficiencies(lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.BENGALI -> listOf("নাইট্রোজেন (পাতা হলুদ)", "পটাশিয়াম (পাতা পোড়া)", "বোরণ (ফুল ঝরা / ফল বিকৃতি)", "ক্যালসিয়াম (ফল পচা)")
        AppLanguage.HINDI -> listOf("नाइट्रोजन (पत्तियां पीली पड़ना)", "पोटाशियम (पत्ती के किनारे जलना)", "बोरॉन (फूल झड़ना / फल फटना)", "कैल्शियम (फल सड़न)")
        AppLanguage.ENGLISH -> listOf("Nitrogen (yellowing leaves)", "Potassium (scorched margins)", "Boron (blossom drop / fruit cracking)", "Calcium (blossom-end rot)")
    }

    fun commonDeficienciesForCrop(crop: MasterCrop, lang: AppLanguage): List<String> {
        if (lang == AppLanguage.BENGALI && crop.commonDeficiencies != MasterCropDefaultBn.DEFICIENCIES) {
            return crop.commonDeficiencies
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS, CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> listOf("নাইট্রোজেন (নিচের পাতা হলুদ)", "লৌহ/আয়রন ক্লোরোসিস (নতুন পাতা ফ্যাকাশে)", "পটাশিয়াম (পাতার কিনারা শুকিয়ে যাওয়া)", "ম্যাগনেসিয়াম (শিরার মাঝে হলুদ দাগ)")
                AppLanguage.HINDI -> listOf("नाइट्रोजन (पुरानी पत्तियां पीली)", "आयरन क्लोरोसिस (नई पत्तियां पीली)", "पोटाशियम (पत्ती के किनारे जलना)", "मैग्नीशियम (शिराओं के बीच पीलापन)")
                AppLanguage.ENGLISH -> listOf("Nitrogen (lower leaf yellowing)", "Iron Chlorosis (pale new shoots)", "Potassium (scorched leaf tips)", "Magnesium (interveinal yellowing)")
            }
            CropMasterGroup.CEREALS_GRAINS -> when (lang) {
                AppLanguage.BENGALI -> listOf("নাইট্রোজেন (বৃদ্ধি থেমে হলুদ হওয়া)", "জিংক (খয়রা রোগ / বাদামি দাগ)", "সালফার (নতুন পাতা হালকা হলুদ)", "পটাশিয়াম (দানা অপুষ্ট ও গাছ নুইয়ে পড়া)")
                AppLanguage.HINDI -> listOf("नाइट्रोजन (धीमी बढ़वार व पीलापन)", "जिंक (खैरा रोग / कत्थई धब्बे)", "सल्फर (नई पत्तियां हल्की पीली)", "पोटाशियम (कमजोर तना व हल्के दाने)")
                AppLanguage.ENGLISH -> listOf("Nitrogen (stunted yellowing)", "Zinc (Khaira rusty patches)", "Sulphur (pale young leaves)", "Potassium (weak stems / poor grain fill)")
            }
            else -> commonDeficiencies(lang)
        }
    }

    fun physiologicalProblems(lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.BENGALI -> listOf("ফুল ঝরে যাওয়া", "ফল ফেটে যাওয়া (Cracking)", "রোদে পোড়া দাগ (Sunscald)")
        AppLanguage.HINDI -> listOf("फूल झड़ना (Flower Drop)", "फल फटना (Fruit Cracking)", "धूप से झुलसन (Sunscald)")
        AppLanguage.ENGLISH -> listOf("Blossom / Flower Drop", "Fruit Cracking", "Sunscald on exposed fruit")
    }

    fun physiologicalProblemsForCrop(crop: MasterCrop, lang: AppLanguage): List<String> {
        if (lang == AppLanguage.BENGALI && crop.physiologicalProblems != MasterCropDefaultBn.PHYSIOLOGICAL) {
            return crop.physiologicalProblems
        }
        return when (crop.getMasterGroup()) {
            CropMasterGroup.FLOWERS -> when (lang) {
                AppLanguage.BENGALI -> listOf("কুঁড়ি অন্ধ হওয়া / না ফোটা (Bud Blindness)", "ডাঁটা নুইয়ে পড়া (Bent Neck)", "পাপড়ির কিনারা পোড়া (Petal Tip Burn)")
                AppLanguage.HINDI -> listOf("कलियां न खिलना (Bud Blindness)", "डंठल झुकना (Bent Neck)", "पंखुड़ी के किनारे झुलसना (Petal Scorch)")
                AppLanguage.ENGLISH -> listOf("Blind Shoots / Failure to Bloom", "Bent Neck (post-harvest stem wilt)", "Petal Edge Scorch")
            }
            CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> when (lang) {
                AppLanguage.BENGALI -> listOf("অতিরিক্ত জলে শিকড় পচা (Overwatering)", "কম আলোয় লম্বাটে দুর্বল বৃদ্ধি (Etiolation)", "পাতার ডগা বাদামি হওয়া (Low Humidity / Salt Burn)")
                AppLanguage.HINDI -> listOf("अधिक पानी से जड़ सड़न", "कम रोशनी में कमजोर लंबा तना", "कम नमी से पत्तियों के सिरे भूरे होना")
                AppLanguage.ENGLISH -> listOf("Root Rot from Overwatering", "Leggy Etiolation in Low Light", "Brown Leaf Tips (dry air / salt build-up)")
            }
            CropMasterGroup.OTHER_AGROFORESTRY -> when (lang) {
                AppLanguage.BENGALI -> listOf("জলাবদ্ধতায় অঙ্কুর নষ্ট", "ঝোড়ো হাওয়ায় চারা হেলে পড়া (Lodging / Windthrow)", "ছাল ফেটে যাওয়া (Bark Split)")
                AppLanguage.HINDI -> listOf("जलभराव से पौध गलन", "तेज हवा से तना झुकना", "सूखे व धूप से छाल फटना")
                AppLanguage.ENGLISH -> listOf("Waterlogging Stand Loss", "Windthrow / Young Stem Lodging", "Sunscald Bark Splitting")
            }
            else -> physiologicalProblems(lang)
        }
    }

    private object MasterCropDefaultBn {
        const val CLIMATE = "উষ্ণ ও নাতিশীতোষ্ণ আর্দ্র আবহাওয়া (২০°-৩২° সেলসিয়াস)"
        const val PH = "৬.০ - ৬.৮ (উর্বর দোআঁশ)"
        const val LAND_PREP = "জমি ৩-৪ বার গভীর চাষ দিয়ে মই দিয়ে ঝুরঝুরে ও আগাছামুক্ত করুন। বিঘায় ১-১.৫ টন পচা গোবর সার বা কম্পোস্ট মেশান।"
        const val SOWING = "বীজ শোধন (ট্রাইকোডার্মা দিয়ে) করে নির্দিষ্ট দূরত্বে লাইন দিয়ে বপন করুন।"
        const val NURSERY = "উঁচু বীজতলায় পলিটানেল বা শেডনেটের নিচে চারা তৈরি করুন। ২৫-৩০ দিন বয়সে চারা রোপণ উপযোগী হয়।"
        const val TRANSPLANT = "পড়ন্ত বিকেলে জমিতে পর্যাপ্ত রস থাকা অবস্থায় চারা রোপণ করুন। রোপণের পরপরই হালকা সেচ দিন।"
        const val IRRIGATION = "মাটিতে সর্বদা আর্দ্রতা রাখুন। তবে জল যেন গোড়ায় না জমে। ফুল ও ফল আসার সময় নিয়মিত সেচ দিন।"
        const val ORGANIC = "গোবর সার বিঘায় ৫০০-১০০০ কেজি, ট্রাইকোডার্মা ও নিম খোল ৫০ কেজি জমি তৈরির শেষ চাষে মেশান।"
        const val FERTILIZER = "মূল সার: SSP বা DAP এবং MOP। বৃদ্ধির সময় ইউরিয়া ও NPK ১৯:১৯:১৯। ফুল আসার আগে বোরণ ও পটাশ।"
        const val FLOWERING = "ফুল আসার সময় অতিরিক্ত নাইট্রোজেন সার দেবেন না। বোরণ (Boron 20%) ২ গ্রাম প্রতি লিটারে স্প্রে করলে ফুল ঝরা বন্ধ হয়।"
        const val FRUITING = "ফল বৃদ্ধির সময় পটাশিয়াম (0:0:50) এবং ক্যালসিয়াম নাইট্রেট প্রয়োগ করুন। ফলের আকার ও উজ্জ্বলতা বাড়ে।"
        const val STORAGE = "ঠান্ডা, ছায়াযুক্ত ও বাতাস চলাচলকারী শুকনো জায়গায় সংরক্ষণ করুন।"
        const val MARKET = "স্থানীয় হাট ও পাইকারি বাজারে সকালের দিকে তাজা গ্রেডিং করা ফসল ভালো দামে বিক্রি হয়।"
        val DEFICIENCIES = listOf("নাইট্রোজেন (পাতা হলুদ)", "পটাশিয়াম (পাতা পোড়া)", "বোরণ (ফুল ঝরা / ফল বিকৃতি)", "ক্যালসিয়াম (ফল পচা)")
        val PHYSIOLOGICAL = listOf("ফুল ঝরে যাওয়া", "ফল ফেটে যাওয়া (Cracking)", "রোদে পোড়া দাগ (Sunscald)")
    }

    private val pestDiseaseTranslations: Map<String, Pair<String, String>> = mapOf(
        "রেড স্পাইডার মাইট (Red Spider Mite)" to ("लाल मकड़ी (Red Spider Mite)" to "Red Spider Mite"),
        "টি মস্কিটো বাগ (Tea Mosquito Bug)" to ("टी मॉस्किटो बग (Tea Mosquito Bug)" to "Tea Mosquito Bug"),
        "থ্রিপস (Thrips)" to ("थ्रिप्स कीट (Thrips)" to "Thrips"),
        "লুপার শুঁয়োপোকা (Looper)" to ("लूपर इल्ली (Looper Caterpillar)" to "Looper Caterpillar"),
        "ব্লিস্টার ব্লাইট (Blister Blight)" to ("ब्लिस्टर ब्लाइट रोग (Blister Blight)" to "Blister Blight"),
        "কালো পচা রোগ (Black Rot)" to ("काला सड़न रोग (Black Rot)" to "Black Rot"),
        "শিকড় পচা (Root Rot)" to ("जड़ सड़न रोग (Root Rot)" to "Root Rot"),
        "জাবপোকা (Aphids)" to ("माहू / चेपा (Aphids)" to "Aphids"),
        "শুঁয়োপোকা (Leaf Miner)" to ("पत्ती सुरंगक / इल्ली (Leaf Miner)" to "Leaf Miner Caterpillar"),
        "ডাউনি মিলডিউ (Downy Mildew)" to ("मृदुरोमिल आसिता (Downy Mildew)" to "Downy Mildew"),
        "ডাউনি মিলডিউ" to ("मृदुरोमिल आसिता (Downy Mildew)" to "Downy Mildew"),
        "পাতা পোড়া রোগ" to ("पत्ती झुलसा रोग (Leaf Blight)" to "Leaf Blight"),
        "পাতা ছেঁদা পোকা" to ("पत्ती छेदक कीट (Leaf Eating Insect)" to "Leaf Perforating Pest"),
        "শুঁয়োপোকা" to ("इल्ली / सूंडी (Caterpillar)" to "Caterpillar"),
        "গোড়া পচা (Damping off)" to ("आर्द्र गलन / डैम्पिंग ऑफ (Damping Off)" to "Damping Off (Collar Rot)"),
        "ডায়মন্ডব্যাক মথ (DBM)" to ("डायमंडबैक मॉथ (DBM)" to "Diamondback Moth (DBM)"),
        "শুঁয়োপোকা (Caterpillars)" to ("इल्ली / सूंडी (Caterpillars)" to "Caterpillars"),
        "কালো পচা রোগ (Black Rot)" to ("काला सड़न रोग (Black Rot)" to "Black Rot"),
        "ক্লাব রুট (Clubroot)" to ("क्लब रूट रोग (Clubroot)" to "Clubroot"),
        "তামাক শুঁয়োপোকা (Spodoptera)" to ("तंबाकू इल्ली (Spodoptera)" to "Tobacco Caterpillar (Spodoptera)"),
        "জাবপোকা" to ("माहू / चेपा (Aphids)" to "Aphids"),
        "হুইপটেল (বোরন ঘাটতি)" to ("व्हिपटेल व बोरॉन कमी (Whiptail)" to "Whiptail / Boron Deficiency"),
        "ফলছেঁদা পোকা (Fruit Borer)" to ("फल छेदक कीट (Fruit Borer)" to "Fruit Borer"),
        "সাদা মাছি (Whitefly)" to ("सफेद मक्खी (Whitefly)" to "Whitefly"),
        "পাতা সুরঙ্গকারী (Leaf Miner)" to ("पत्ती सुरंगक (Leaf Miner)" to "Leaf Miner"),
        "পাতার কোঁকড়া রোগ (Leaf Curl)" to ("पत्ती मरोड़ वायरस (Leaf Curl Virus)" to "Tomato Leaf Curl Virus"),
        "নাভি ধসা (Late Blight)" to ("पछेती झुलसा (Late Blight)" to "Late Blight"),
        "ব্যাকটেরিয়াল উইল্ট" to ("जीवाणु मुरझान (Bacterial Wilt)" to "Bacterial Wilt"),
        "কাটুই পোকা (Cutworm)" to ("कटुआ कीट (Cutworm)" to "Cutworm"),
        "কাটুই পোকা" to ("कटुआ कीट (Cutworm)" to "Cutworm"),
        "থ্রিপস" to ("थ्रिप्स कीट (Thrips)" to "Thrips"),
        "ফল মাছি পোকা" to ("फल मक्खी (Fruit Fly)" to "Fruit Fly"),
        "শুঁটি ছেঁদা পোকা" to ("फली छेदक (Pod Borer)" to "Pod Borer"),
        "আলুর নলি পোকা (PTM)" to ("आलू का कंद पतंगा (PTM)" to "Potato Tuber Moth (PTM)"),
        "নাবি ধসা (Late Blight)" to ("पछेती झुलसा (Late Blight)" to "Late Blight"),
        "আগে ধসা (Early Blight)" to ("अगेती झुलसा (Early Blight)" to "Early Blight"),
        "সাধারণ স্ক্যাব (Common Scab)" to ("सामान्य स्कैब (Common Scab)" to "Common Scab"),
        "কালো দাগ (Black Scurf)" to ("काली रूसी रोग (Black Scurf)" to "Black Scurf"),
        "হলুদ মাকড় (Yellow Mites)" to ("पीली मकड़ी (Yellow Mite)" to "Yellow Mites"),
        "ফল পচা পোকা" to ("फल छेदक / फल मक्खी" to "Fruit Pest / Borer"),
        "পাতা কোঁকড়া ভাইরাস" to ("पत्ती मरोड़ वायरस (Leaf Curl)" to "Leaf Curl Virus"),
        "অ্যানথ্রাকনোজ (ফল পচা)" to ("एन्थ्रेक्नोज / फल सड़न (Anthracnose)" to "Anthracnose (Fruit Rot)"),
        "আমের হপার পোকা (Mango Hopper)" to ("आम का फुदका कीट (Mango Hopper)" to "Mango Hopper"),
        "ফল মাছি (Fruit Fly)" to ("फल मक्खी (Fruit Fly)" to "Fruit Fly"),
        "ফল মাছি পোকা (Fruit Fly)" to ("फल मक्खी (Fruit Fly)" to "Fruit Fly"),
        "মিলিবাগ (Mealybug)" to ("मिलीबग (Mealybug)" to "Mealybug"),
        "অ্যানথ্রাকনোজ (Anthracnose)" to ("एन्थ्रेक्नोज रोग (Anthracnose)" to "Anthracnose"),
        "পাউডারি মিলডিউ (Powdery Mildew)" to ("चूर्णिल आसिता (Powdery Mildew)" to "Powdery Mildew"),
        "পাউডারি মিলডিউ" to ("चूर्णिल आसिता (Powdery Mildew)" to "Powdery Mildew"),
        "কলা গুবরে পোকা (Rhizome Weevil)" to ("केला राइजोम घुन (Rhizome Weevil)" to "Banana Rhizome Weevil"),
        "মাকড়" to ("मकड़ी (Mites)" to "Mites"),
        "পানামা উইল্ট (Panama Wilt)" to ("पनामा उकठा रोग (Panama Wilt)" to "Panama Wilt"),
        "সিগাটোকা পাতা পোড়া (Sigatoka)" to ("सिगाटोका पत्ती धब्बा (Sigatoka)" to "Sigatoka Leaf Spot"),
        "কাণ্ড ছিদ্রকারী পোকা" to ("तना छेदक कीट (Stem Borer)" to "Stem Borer"),
        "সাদা মরিচা রোগ (White Rust)" to ("सफेद रतुआ रोग (White Rust)" to "White Rust"),
        "সাদা মরিচা রোগ" to ("सफेद रतुआ रोग (White Rust)" to "White Rust"),
        "পাতা মোড়ানো পোকা" to ("पत्ती लपेटक कीट (Leaf Folder)" to "Leaf Folder"),
        "পাতার দাগ রোগ" to ("पत्ती धब्बा रोग (Leaf Spot)" to "Leaf Spot"),
        "সরিষার জাবপোকা (Mustard Aphids)" to ("सरसों का माहू (Mustard Aphid)" to "Mustard Aphids"),
        "অল্টারনারিয়া ব্লাইট" to ("अल्टरनेरिया झुलसा (Alternaria Blight)" to "Alternaria Blight"),
        "অল্টারনারিয়া দাগ" to ("अल्टरनेरिया पत्ती धब्बा (Alternaria)" to "Alternaria Leaf Spot"),
        "গোড়া পচা" to ("जड़ व तना सड़न (Collar Rot)" to "Collar / Root Rot"),
        "স্ল্যাগ" to ("घोंघा / स्लग (Slugs)" to "Slugs"),
        "পাতা পচা রোগ" to ("पत्ती सड़न (Leaf Rot)" to "Leaf Rot"),
        "মিষ্টি আলুর গুঁয়ে পোকা (Weevil)" to ("शकरकंद घुन (Sweet Potato Weevil)" to "Sweet Potato Weevil"),
        "কালো পচা রোগ" to ("काला सड़न रोग (Black Rot)" to "Black Rot"),
        "মাছি পোকা" to ("फल / तना मक्खी" to "Fly Pest"),
        "গাজরের মাছি পোকা" to ("गाजर मक्खी (Carrot Fly)" to "Carrot Fly"),
        "ক্যাভিটি স্পট" to ("कैविटी स्पॉट (Cavity Spot)" to "Cavity Spot"),
        "পাতার ব্লাইট" to ("पत्ती झुलसा (Leaf Blight)" to "Leaf Blight"),
        "সারকোস্পোরা পাতার দাগ" to ("सर्कोस्पोरा पत्ती धब्बा (Cercospora)" to "Cercospora Leaf Spot"),
        "কলার রট বা কন্দ পচা" to ("कंद व तना सड़न (Collar / Tuber Rot)" to "Collar & Tuber Rot"),
        "কলার রট" to ("कॉलर सड़न (Collar Rot)" to "Collar Rot"),
        "ডিবিএম (Diamondback Moth)" to ("डायमंडबैक मॉथ (DBM)" to "Diamondback Moth (DBM)"),
        "ব্ল্যাক রট বা কালো পচা" to ("काला सड़न रोग (Black Rot)" to "Black Rot"),
        "থ্রিপস ও মাইট (পাতা কুঁকড়ানো)" to ("थ्रिप्स और माइट (पत्ती मरोड़)" to "Thrips & Mites (Leaf Curl)"),
        "ফল ছিদ্রকারী পোকা" to ("फल छेदक कीट (Fruit Borer)" to "Fruit Borer"),
        "অ্যানথ্রাকনোজ ফল পচা" to ("एन्थ्रेक्नोज फल सड़न" to "Anthracnose Fruit Rot"),
        "রেড পাম্পকিন বিটল" to ("लाल कद्दू भृंग (Red Pumpkin Beetle)" to "Red Pumpkin Beetle"),
        "রেড বিটল" to ("लाल भृंग (Red Beetle)" to "Red Pumpkin Beetle"),
        "ফল মাছি" to ("फल मक्खी (Fruit Fly)" to "Fruit Fly"),
        "ফল পচা ও কলার রট" to ("फल सड़न व कॉलर रॉट" to "Fruit Rot & Collar Rot"),
        "ফিউজারিয়াম উইল্ট" to ("फ्यूजेरियम उकठा रोग (Fusarium Wilt)" to "Fusarium Wilt"),
        "ফল পচা" to ("फल सड़न (Fruit Rot)" to "Fruit Rot"),
        "জাবপোকা (Black Aphids)" to ("काला माहू (Black Aphids)" to "Black Aphids"),
        "শুঁটি ছিদ্রকারী পোকা" to ("फली छेदक कीट (Pod Borer)" to "Pod Borer"),
        "হলুদ মোজাইক ভাইরাস" to ("पीला मोज़ेक वायरस (YMV)" to "Yellow Mosaic Virus (YMV)"),
        "লিফ মাইনার" to ("पत्ती सुरंगक (Leaf Miner)" to "Leaf Miner"),
        "পার্পল ব্লচ (Purple Blotch)" to ("बैंगनी धब्बा रोग (Purple Blotch)" to "Purple Blotch"),
        "উইল্ট রোগ" to ("उकठा रोग (Wilt)" to "Fusarium Wilt"),
        "ব্লাইট" to ("झुलसा रोग (Blight)" to "Blight"),
        "ফল ও ডগা ছিদ্রকারী পোকা" to ("फल व तना छेदक कीट" to "Fruit & Shoot Borer"),
        "ফল পচা রোগ" to ("फल सड़न रोग (Fruit Rot)" to "Fruit Rot"),
        "সাইট্রাস সাইলা" to ("सिट्रस सायला (Citrus Psylla)" to "Citrus Psylla"),
        "পাতা সুড়ঙ্গকারী পোকা (Leaf Miner)" to ("पत्ती सुरंगक (Leaf Miner)" to "Citrus Leaf Miner"),
        "ক্যাঙ্কার রোগ (Citrus Canker)" to ("सिट्रस कैंकर रोग (Citrus Canker)" to "Citrus Canker"),
        "ডাই-ব্যাক" to ("डाइबैक / टहनी सूखना (Dieback)" to "Twig Dieback"),
        "মাজরা পোকা (Stem Borer)" to ("तना छेदक कीट (Stem Borer)" to "Stem Borer"),
        "পাতা মোড়ানো পোকা (Leaf Folder)" to ("पत्ती लपेटक (Leaf Folder)" to "Leaf Folder"),
        "বাদামি গাছফড়িং (BPH)" to ("भूरा फुदका (BPH)" to "Brown Planthopper (BPH)"),
        "সবুজ পাতাফড়িং (GLH)" to ("हरा फुदका (GLH)" to "Green Leafhopper (GLH)"),
        "ব্লাস্ট রোগ (Blast)" to ("झुलसा / ब्लास्ट रोग (Blast)" to "Rice Blast"),
        "খোল পোড়া রোগ (Sheath Blight)" to ("शीथ ब्लाइट / पर्णच्छद झुलसा" to "Sheath Blight"),
        "ব্যাকটেরিয়াল ব্লাইট (BLB)" to ("जीवाणु झुलसा रोग (BLB)" to "Bacterial Leaf Blight (BLB)"),
        "ফলস স্মাট (False Smut)" to ("आभासी कंडुआ / हल्दिया रोग (False Smut)" to "False Smut"),
        "জাবপোকা (Wheat Aphids)" to ("गेहूं का माहू (Aphids)" to "Wheat Aphids"),
        "উঁইপোকা (Termites)" to ("दीमक (Termites)" to "Termites"),
        "গোলা পোকা" to ("भंडार घुन (Grain Pest)" to "Storage Pest"),
        "হলুদ মরিচা রোগ (Yellow Rust)" to ("पीला रतुआ रोग (Yellow Rust)" to "Yellow Rust"),
        "বাদামি মরিচা রোগ (Brown Rust)" to ("भूरा रतुआ रोग (Brown Rust)" to "Brown Rust"),
        "আলগা চিটা (Loose Smut)" to ("अनावृत कंडुआ (Loose Smut)" to "Loose Smut"),
        "ফল আর্মিওয়ার্ম (Fall Armyworm / FAW)" to ("फॉल आर्मीवर्म (FAW)" to "Fall Armyworm (FAW)"),
        "টারসিকাম লিফ ব্লাইট (Leaf Blight)" to ("टर्सिकम पत्ती झुलसा (Leaf Blight)" to "Turcicum Leaf Blight"),
        "মোচড় পচা রোগ (Downy Mildew)" to ("मृदुरोमिल आसिता (Downy Mildew)" to "Downy Mildew"),
        "শুঁটি ছেঁদা পোকা (Pod Borer)" to ("फली छेदक (Pod Borer)" to "Pod Borer"),
        "উইল্ট রোগ (Fusarium Wilt)" to ("फ्यूजेरियम उकठा रोग (Wilt)" to "Fusarium Wilt"),
        "মরিচা রোগ (Rust)" to ("रतुआ रोग (Rust)" to "Rust"),
        "হলুদ মোজাইক ভাইরাস (YMV)" to ("पीला मोज़ेक वायरस (YMV)" to "Yellow Mosaic Virus (YMV)"),
        "সারকোস্পোরা পাতার দাগ" to ("सर्कोस्पोरा पत्ती धब्बा" to "Cercospora Leaf Spot"),
        "শুঁটি ছেঁদা পোকা (Helicoverpa armigera)" to ("फली छेदक (Helicoverpa)" to "Gram Pod Borer (Helicoverpa)"),
        "উইল্ট রোগ (Wilt)" to ("उकठा रोग (Wilt)" to "Fusarium Wilt"),
        "অ্যাসকোচাইটা ব্লাইট" to ("एस्कोकाइटा झुलसा (Ascochyta Blight)" to "Ascochyta Blight"),
        "শুঁটি মাছি (Pod Fly)" to ("फली मक्खी (Pod Fly)" to "Pod Fly"),
        "শুঁটি ছেঁদা পোকা (Plume Moth)" to ("फली छेदक पतंगा (Plume Moth)" to "Plume Moth Pod Borer"),
        "বন্ধ্যাত্ব মোজাইক (Sterility Mosaic)" to ("बांझपन मोज़ेक रोग (Sterility Mosaic)" to "Sterility Mosaic Disease"),
        "জাবপোকা (Mustard Aphids)" to ("सरसों का माहू / चेपा (Aphids)" to "Mustard Aphids"),
        "স করাত মাছি (Sawfly)" to ("आरा मक्खी (Mustard Sawfly)" to "Mustard Sawfly"),
        "সাদা মরিচা (White Rust)" to ("सफेद रतुआ (White Rust)" to "White Rust"),
        "পাতা ও শুঁটি খাদক পোকা (Antigastra)" to ("पत्ती व फली रोलर (Antigastra)" to "Leaf Webbing & Capsule Borer"),
        "ফিলডি রোগ (Phyllody)" to ("फिलोडी रोग (Phyllody)" to "Sesame Phyllody"),
        "ব্যাকটেরিয়াল ব্লাইট" to ("जीवाणु झुलसा (Bacterial Blight)" to "Bacterial Blight"),
        "সাদা গ্রাব (White Grub)" to ("सफेद लट / गिडार (White Grub)" to "White Grub"),
        "টিক্কা রোগ (Tikka Leaf Spot)" to ("टिक्का पत्ती धब्बा रोग (Tikka)" to "Tikka Leaf Spot"),
        "ডগা মাজরা পোকা (Early Shoot Borer)" to ("अगेती तना छेदक (Shoot Borer)" to "Early Shoot Borer"),
        "শীর্ষ মাজরা পোকা (Top Borer)" to ("शीर्ष तना छेदक (Top Borer)" to "Top Shoot Borer"),
        "উঁইপোকা" to ("दीमक (Termites)" to "Termites"),
        "লাল পচা রোগ (Red Rot)" to ("लाल सड़न रोग (Red Rot)" to "Red Rot"),
        "স্মাট রোগ (Whip Smut)" to ("चाबुक कंडुआ रोग (Whip Smut)" to "Whip Smut"),
        "উইল্ট" to ("उकठा रोग (Wilt)" to "Wilt"),
        "পাটের বিছা পোকা (Jute Hairy Caterpillar)" to ("जूट की बालों वाली सूंडी" to "Jute Hairy Caterpillar"),
        "সেমি-লুপার (Semilooper)" to ("जूट सेमी-लूपर (Semilooper)" to "Jute Semilooper"),
        "হলুদ মাকড় (Yellow Mite)" to ("पीली मकड़ी (Yellow Mite)" to "Yellow Mite"),
        "কান্ড পচা (Stem Rot)" to ("तना सड़न (Stem Rot)" to "Stem Rot"),
        "শিকড় পচা (Root Rot)" to ("जड़ सड़न (Root Rot)" to "Root Rot"),
        "গোলাপী শুঁটি পোকা (Pink Bollworm)" to ("गुलाबी सूंडी (Pink Bollworm)" to "Pink Bollworm"),
        "জেসিড (Jassid)" to ("हरा तेला / जैसिड (Jassid)" to "Jassid (Leafhopper)"),
        "ব্যাকটেরিয়াল ব্লাইট (Angular Leaf Spot)" to ("कोणीय पत्ती धब्बा (Bacterial Blight)" to "Bacterial Blight (Angular Leaf Spot)"),
        "গোড়া পচা (Collar Rot)" to ("कॉलर सड़न रोग (Collar Rot)" to "Collar Rot"),
        "ব্ল্যাক স্পট (Black Spot)" to ("काला धब्बा रोग (Black Spot)" to "Black Spot"),
        "ডাই-ব্যাক (Dieback)" to ("टहनी सूखना / डाइबैक (Dieback)" to "Twig Dieback"),
        "শুঁয়োপোকা (Bud Borer)" to ("कली छेदक सूंडी (Bud Borer)" to "Bud Borer Caterpillar"),
        "বোট্রাইটিস ব্লাইট (Flower Blight)" to ("बोट्राइटिस पुष्प झुलसा (Flower Blight)" to "Botrytis Flower Blight"),
        "রাইজোম স্কেল" to ("प्रकंद स्केल कीट (Rhizome Scale)" to "Rhizome Scale"),
        "রাইজোম ফ্লাই" to ("प्रकंद मक्खी (Rhizome Fly)" to "Rhizome Fly"),
        "কন্দ পচা (Rhizome Rot)" to ("प्रकंद सड़न रोग (Rhizome Rot)" to "Rhizome Rot"),
        "কন্দ পচা (Soft Rot)" to ("मृदु सड़न रोग (Soft Rot)" to "Soft Rhizome Rot")
    )

    fun weedManagement(cropId: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> when (cropId) {
            "potato" -> "বপনের ৩-৪ দিনের মধ্যে মেট্রিবিউজিন (Metribuzin) অথবা ২০-২৫ দিনে আগাছা নিড়িয়ে গোড়ায় মাটি তুলুন।"
            "rice_paddy" -> "রোপণের ৩-৫ দিনের মধ্যে প্রিটিলাক্লোর (Pretilachlor) দিয়ে আগাছা আটকান। ২০-২৫ দিনে প্যাডি উইডার বা হাত নিড়ানি দিন।"
            "wheat" -> "বপনের ৩০-৩৫ দিন পর চওড়া পাতার আগাছার জন্য 2,4-D অথবা মেটসালফিউরন মিথাইল প্রয়োগ করুন।"
            "mustard" -> "বপনের ২০-২৫ দিনে নিড়ানি দিন বা পেন্ডিমিথালিন প্রয়োগ করুন।"
            else -> "বপনের ২০-২৫ দিন পর প্রথম নিড়ানি দিয়ে আগাছা পরিষ্কার করুন। লাইন চাষে প্যাডি উইডার বা কোদাল ব্যবহার করুন।"
        }
        AppLanguage.HINDI -> when (cropId) {
            "potato" -> "बुवाई के 3–4 दिन में मेट्रीब्यूजिन का छिड़काव करें या 20–25 दिन पर निराई-गुड़ाई कर पौधों पर मिट्टी चढ़ाएं।"
            "rice_paddy" -> "रोपाई के 3–5 दिन के भीतर प्रेटिलाक्लोर से खरपतवार रोकें। 20–25 दिन बाद पैडी वीडर या हाथ से निराई करें।"
            "wheat" -> "बुवाई के 30–35 दिन बाद चौड़ी पत्ती वाले खरपतवारों के लिए 2,4-D या मेट्सल्फ्यूरॉन मिथाइल का उपयोग करें।"
            "mustard" -> "बुवाई के 20–25 दिन पर निराई-गुड़ाई करें या पेंडीमेथालिन का प्रयोग करें।"
            else -> "बुवाई के 20–25 दिन बाद पहली निराई कर खेत खरपतवार मुक्त रखें। कतार खेती में कोनो-वीडर या कुदाल चलाएं।"
        }
        AppLanguage.ENGLISH -> when (cropId) {
            "potato" -> "Apply pre-emergence Metribuzin within 3–4 days of planting or perform manual weeding and earthing up at 20–25 days."
            "rice_paddy" -> "Use pre-emergence Pretilachlor 3–5 days after transplanting, followed by cono-weeder or hand weeding at 20–25 days."
            "wheat" -> "Apply 2,4-D or Metsulfuron-methyl at 30–35 days after sowing for broadleaf weed control."
            "mustard" -> "Perform hand weeding at 20–25 days or apply pre-emergence Pendimethalin."
            else -> "Perform first hand weeding 20–25 days after sowing. Use hoeing or wheel-hoe in row cultivation."
        }
    }

    fun seedTreatment(cropId: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> when (cropId) {
            "potato" -> "বীজ আলু কাটার পর ম্যানকোজেব (Mancozeb 2g/L) বা কার্বেন্ডাজিম দ্রবণে ১০ মিনিট চুবিয়ে ছায়ায় ২৪-৪৮ ঘণ্টা শুকিয়ে সুবেরাইজেশন (ত্বক শক্ত) করুন।"
            "rice_paddy" -> "লবণ জল দিয়ে পুষ্ট বীজ বাছাই করুন। ট্রাইকোডার্মা ৫ গ্রাম/কেজি অথবা কার্বেন্ডাজিম ২ গ্রাম/কেজি দিয়ে শোধন করে জাগ দিন।"
            "wheat" -> "কার্বোক্সিন + থাইরাম ২.৫ গ্রাম প্রতি কেজি বীজে মিশিয়ে চিটা রোগ প্রতিরোধ করুন।"
            else -> "বীজ বোনার পূর্বে ট্রাইকোডার্মা ভিরিডি বা কার্বেন্ডাজিম ২ গ্রাম/কেজি হারে মিশিয়ে শোধন করুন।"
        }
        AppLanguage.HINDI -> when (cropId) {
            "potato" -> "कटे हुए बीज आलू को मैनकोजेब (2 ग्राम/लीटर) या कार्बेन्डाजिम के घोल में 10 मिनट डुबोकर छाया में 24–48 घंटे सुखाएं (सुबराइजेशन)।"
            "rice_paddy" -> "नमक के घोल से भारी व स्वस्थ बीज चुनें। ट्राइकोडर्मा (5 ग्राम/किग्रा) या कार्बेन्डाजिम (2 ग्राम/किग्रा) से उपचारित कर अंकुरित करें।"
            "wheat" -> "कंडुआ रोग से बचाव हेतु कार्बोक्सिन + थिरम (2.5 ग्राम/किग्रा बीज) से बीज उपचार करें।"
            else -> "बुवाई से पूर्व ट्राइकोडर्मा विरिडी या कार्बेन्डाजिम (2 ग्राम/किग्रा बीज) से बीज उपचार अवश्य करें।"
        }
        AppLanguage.ENGLISH -> when (cropId) {
            "potato" -> "Dip cut potato tubers in Mancozeb (2 g/L) or Carbendazim for 10 min, then shade-dry 24–48 hours for suberization."
            "rice_paddy" -> "Grade heavy seeds in brine solution. Treat with Trichoderma (5 g/kg) or Carbendazim (2 g/kg) before incubating."
            "wheat" -> "Treat seed with Carboxin + Thiram at 2.5 g/kg seed to prevent loose smut."
            else -> "Treat seeds before sowing with Trichoderma viride or Carbendazim at 2 g/kg of seed."
        }
    }

    fun translatePestOrDisease(rawBn: String, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI) return rawBn
        pestDiseaseTranslations[rawBn]?.let { (hi, en) ->
            return if (lang == AppLanguage.HINDI) hi else en
        }
        return fallbackFormat(rawBn, lang)
    }

    fun defaultPestsForCategory(cat: CropCategory, lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.BENGALI -> when (cat) {
            CropCategory.CEREALS, CropCategory.GRAINS_FIELD_CROPS, CropCategory.MILLETS -> listOf("মাজরা পোকা (Stem Borer)", "পাতা মোড়ানো পোকা", "জাবপোকা")
            CropCategory.PULSES -> listOf("শুঁটি ছেঁদা পোকা (Pod Borer)", "জাবপোকা")
            CropCategory.OILSEEDS -> listOf("জাবপোকা (Aphids)", "শুঁয়োপোকা")
            CropCategory.FRUITS, CropCategory.PLANTATION -> listOf("ফল মাছি পোকা (Fruit Fly)", "সাদা মাছি ও শোষক পোকা")
            CropCategory.FLOWERS -> listOf("থ্রিপস (Thrips)", "মাকড় (Red Spider Mite)", "কুঁড়ি ছিদ্রকারী পোকা")
            CropCategory.NURSERY_ORNAMENTAL -> listOf("মিলিবাগ (Mealybug)", "স্কেল পোকা (Scale)", "জাবপোকা ও মাকড়")
            CropCategory.MEDICINAL_AROMATIC -> listOf("শুঁয়োপোকা ও পাতা খাদক পোকা", "জাবপোকা (Aphids)")
            CropCategory.AGROFORESTRY_OTHER, CropCategory.FODDER -> listOf("উঁইপোকা (Termites)", "কাণ্ড ছিদ্রকারী ও পাতা খাদক পোকা")
            else -> listOf("জাবপোকা (Aphids)", "সাদা মাছি (Whitefly)", "পাতা খাদক শুঁয়োপোকা")
        }
        AppLanguage.HINDI -> when (cat) {
            CropCategory.CEREALS, CropCategory.GRAINS_FIELD_CROPS, CropCategory.MILLETS -> listOf("तना छेदक (Stem Borer)", "पत्ती लपेटक कीट", "माहू (Aphids)")
            CropCategory.PULSES -> listOf("फली छेदक कीट (Pod Borer)", "काला माहू")
            CropCategory.OILSEEDS -> listOf("माहू / चेपा कीट", "पत्ती काटने वाली सूंडी")
            CropCategory.FRUITS, CropCategory.PLANTATION -> listOf("फल मक्खी (Fruit Fly)", "रस चूसने वाले कीट")
            CropCategory.FLOWERS -> listOf("थ्रिप्स कीट (Thrips)", "लाल मकड़ी (Mites)", "कली छेदक इल्ली")
            CropCategory.NURSERY_ORNAMENTAL -> listOf("मिलीबग (Mealybug)", "स्केल कीट (Scale Insects)", "माहू व मकड़ी")
            CropCategory.MEDICINAL_AROMATIC -> listOf("पत्ती खाने वाली सूंडी", "माहू / रस चूसक कीट")
            CropCategory.AGROFORESTRY_OTHER, CropCategory.FODDER -> listOf("दीमक (Termites)", "तना छेदक व पत्ती काटने वाले कीट")
            else -> listOf("माहू / चेपा (Aphids)", "सफेद मक्खी (Whitefly)", "पत्ती खाने वाली सूंडी")
        }
        AppLanguage.ENGLISH -> when (cat) {
            CropCategory.CEREALS, CropCategory.GRAINS_FIELD_CROPS, CropCategory.MILLETS -> listOf("Stem Borer", "Leaf Folder", "Aphids")
            CropCategory.PULSES -> listOf("Pod Borer (Helicoverpa)", "Aphids")
            CropCategory.OILSEEDS -> listOf("Aphids", "Leaf-eating Caterpillars")
            CropCategory.FRUITS, CropCategory.PLANTATION -> listOf("Fruit Fly", "Sap-sucking Bugs & Mites")
            CropCategory.FLOWERS -> listOf("Thrips", "Red Spider Mite", "Bud Borer Caterpillar")
            CropCategory.NURSERY_ORNAMENTAL -> listOf("Mealybugs", "Scale Insects", "Sap-sucking Mites & Aphids")
            CropCategory.MEDICINAL_AROMATIC -> listOf("Defoliating Caterpillars", "Aphids & Sucking Pests")
            CropCategory.AGROFORESTRY_OTHER, CropCategory.FODDER -> listOf("Termites", "Stem Borer & Defoliators")
            else -> listOf("Aphids", "Whiteflies", "Foliar Caterpillars")
        }
    }

    fun defaultDiseasesForCategory(cat: CropCategory, lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.BENGALI -> when (cat) {
            CropCategory.CEREALS, CropCategory.GRAINS_FIELD_CROPS, CropCategory.MILLETS -> listOf("পাতার দাগ ও ব্লাইট রোগ", "মরিচা রোগ (Rust)")
            CropCategory.PULSES -> listOf("উইল্ট / ঢলে পড়া রোগ", "পাউডারি মিলডিউ")
            CropCategory.OILSEEDS -> listOf("অল্টারনারিয়া ব্লাইট", "সাদা মরিচা (White Rust)")
            CropCategory.FRUITS, CropCategory.PLANTATION -> listOf("অ্যানথ্রাকনোজ (Anthracnose)", "ফল পচা ও শিকড় পচা")
            CropCategory.FLOWERS -> listOf("পাউডারি মিলডিউ (Powdery Mildew)", "ফুলের বোট্রাইটিস ব্লাইট ও গোড়া পচা")
            CropCategory.NURSERY_ORNAMENTAL -> listOf("অতিরিক্ত জলে শিকড় পচা (Root Rot)", "পাতার দাগ (Leaf Spot)")
            CropCategory.MEDICINAL_AROMATIC, CropCategory.SPICES -> listOf("কন্দ/শিকড় পচা (Root / Rhizome Rot)", "পাতার দাগ রোগ")
            CropCategory.AGROFORESTRY_OTHER, CropCategory.FODDER -> listOf("গোড়া পচা ও উইল্ট", "পাতার দাগ (Leaf Spot)")
            else -> listOf("ড্যাম্পিং অফ / গোড়া পচা", "পাতার দাগ ও ডাউনি মিলডিউ")
        }
        AppLanguage.HINDI -> when (cat) {
            CropCategory.CEREALS, CropCategory.GRAINS_FIELD_CROPS, CropCategory.MILLETS -> listOf("पत्ती धब्बा व झुलसा रोग", "रतुआ रोग (Rust)")
            CropCategory.PULSES -> listOf("उकठा रोग (Fusarium Wilt)", "चूर्णिल आसिता")
            CropCategory.OILSEEDS -> listOf("अल्टरनेरिया झुलसा", "सफेद रतुआ (White Rust)")
            CropCategory.FRUITS, CropCategory.PLANTATION -> listOf("एन्थ्रेक्नोज (Anthracnose)", "फल व जड़ सड़न रोग")
            CropCategory.FLOWERS -> listOf("चूर्णिल आसिता (Powdery Mildew)", "पुष्प झुलसा (Botrytis) व जड़ सड़न")
            CropCategory.NURSERY_ORNAMENTAL -> listOf("अधिक सिंचाई से जड़ सड़न (Root Rot)", "पत्ती धब्बा (Leaf Spot)")
            CropCategory.MEDICINAL_AROMATIC, CropCategory.SPICES -> listOf("प्रकंद/जड़ सड़न (Rhizome/Root Rot)", "पत्ती धब्बा रोग")
            CropCategory.AGROFORESTRY_OTHER, CropCategory.FODDER -> listOf("जड़ गलन व उकठा", "पत्ती धब्बा रोग")
            else -> listOf("आर्द्र गलन (Damping Off)", "पत्ती धब्बा व मृदुरोमिल आसिता")
        }
        AppLanguage.ENGLISH -> when (cat) {
            CropCategory.CEREALS, CropCategory.GRAINS_FIELD_CROPS, CropCategory.MILLETS -> listOf("Leaf Spot & Blight", "Rust")
            CropCategory.PULSES -> listOf("Fusarium Wilt", "Powdery Mildew")
            CropCategory.OILSEEDS -> listOf("Alternaria Blight", "White Rust")
            CropCategory.FRUITS, CropCategory.PLANTATION -> listOf("Anthracnose", "Fruit & Root Rot")
            CropCategory.FLOWERS -> listOf("Powdery Mildew", "Botrytis Blossom Blight & Collar Rot")
            CropCategory.NURSERY_ORNAMENTAL -> listOf("Overwatering Root Rot", "Fungal Leaf Spot")
            CropCategory.MEDICINAL_AROMATIC, CropCategory.SPICES -> listOf("Rhizome / Root Rot", "Foliar Leaf Spot")
            CropCategory.AGROFORESTRY_OTHER, CropCategory.FODDER -> listOf("Collar Rot & Wilt", "Leaf Spot")
            else -> listOf("Damping Off / Collar Rot", "Leaf Spot & Downy Mildew")
        }
    }
}
