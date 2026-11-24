package com.example.data.datasource

import com.example.data.model.AppLanguage
import com.example.data.model.MarketPriceItem

data class MarketItemLocale(
    val marketHi: String,
    val marketEn: String,
    val districtHi: String,
    val districtEn: String,
    val unitHi: String,
    val unitEn: String,
    val sourceHi: String,
    val sourceEn: String,
    val updatedHi: String,
    val updatedEn: String
)

object MarketPriceTranslations {
    val locales: Map<String, MarketItemLocale> = mapOf(
        "Tomato" to MarketItemLocale(
            marketHi = "सिलीगुड़ी रेगुलेटेड मार्केट",
            marketEn = "Siliguri Regulated Market",
            districtHi = "दार्जिलिंग / जलपाईगुड़ी",
            districtEn = "Darjeeling / Jalpaiguri",
            unitHi = "₹ / क्विंटल (25/किग्रा)",
            unitEn = "₹ / Quintal (25/kg)",
            sourceHi = "Agmarknet WB",
            sourceEn = "Agmarknet WB",
            updatedHi = "आज सुबह 9:00",
            updatedEn = "Today 9:00 AM"
        ),
        "Cabbage" to MarketItemLocale(
            marketHi = "कूचबिहार मंडी",
            marketEn = "Cooch Behar Mandi",
            districtHi = "कूचबिहार",
            districtEn = "Cooch Behar",
            unitHi = "₹ / क्विंटल (14/किग्रा)",
            unitEn = "₹ / Quintal (14/kg)",
            sourceHi = "Agmarknet WB",
            sourceEn = "Agmarknet WB",
            updatedHi = "आज सुबह 9:00",
            updatedEn = "Today 9:00 AM"
        ),
        "Cauliflower" to MarketItemLocale(
            marketHi = "बारासात बाजार",
            marketEn = "Barasat Market",
            districtHi = "उत्तर 24 परगना",
            districtEn = "North 24 Parganas",
            unitHi = "₹ / क्विंटल (28/किग्रा)",
            unitEn = "₹ / Quintal (28/kg)",
            sourceHi = "राज्य कृषि विपणन बोर्ड",
            sourceEn = "State Agri Marketing Board",
            updatedHi = "आज सुबह 8:45",
            updatedEn = "Today 8:45 AM"
        ),
        "Potato (Jyoti)" to MarketItemLocale(
            marketHi = "तारकेश्वर मंडी",
            marketEn = "Tarakeswar Mandi",
            districtHi = "हुगली",
            districtEn = "Hooghly",
            unitHi = "₹ / क्विंटल (17.5/किग्रा)",
            unitEn = "₹ / Quintal (17.5/kg)",
            sourceHi = "Agmarknet WB",
            sourceEn = "Agmarknet WB",
            updatedHi = "आज सुबह 9:15",
            updatedEn = "Today 9:15 AM"
        ),
        "Green Chilli" to MarketItemLocale(
            marketHi = "मेचेदा थोक बाजार",
            marketEn = "Mecheda Wholesale Market",
            districtHi = "पूर्व मेदिनीपुर",
            districtEn = "Purba Medinipur",
            unitHi = "₹ / क्विंटल (52/किग्रा)",
            unitEn = "₹ / Quintal (52/kg)",
            sourceHi = "मेचेदा सहकारी मंडी",
            sourceEn = "Mecheda Co-op Market",
            updatedHi = "आज सुबह 8:30",
            updatedEn = "Today 8:30 AM"
        ),
        "Brinjal" to MarketItemLocale(
            marketHi = "कृष्णनगर थोक बाजार",
            marketEn = "Krishnanagar Wholesale Market",
            districtHi = "नदिया",
            districtEn = "Nadia",
            unitHi = "₹ / क्विंटल (23/किग्रा)",
            unitEn = "₹ / Quintal (23/kg)",
            sourceHi = "Agmarknet WB",
            sourceEn = "Agmarknet WB",
            updatedHi = "आज सुबह 9:10",
            updatedEn = "Today 9:10 AM"
        ),
        "Tea Green Leaf" to MarketItemLocale(
            marketHi = "जलपाईगुड़ी बॉट लीफ फैक्ट्री",
            marketEn = "Jalpaiguri Bought Leaf Factory",
            districtHi = "जलपाईगुड़ी",
            districtEn = "Jalpaiguri",
            unitHi = "₹ / किग्रा",
            unitEn = "₹ / kg",
            sourceHi = "टी बोर्ड न्यूनतम बेंचमार्क दर",
            sourceEn = "Tea Board Min. Benchmark Rate",
            updatedHi = "आज सुबह 9:30",
            updatedEn = "Today 9:30 AM"
        ),
        "Spinach" to MarketItemLocale(
            marketHi = "सियालदह कोल्ड मार्केट",
            marketEn = "Sealdah Cold Market",
            districtHi = "कोलकाता",
            districtEn = "Kolkata",
            unitHi = "₹ / क्विंटल (18/किग्रा)",
            unitEn = "₹ / Quintal (18/kg)",
            sourceHi = "कृषि विपणन विभाग",
            sourceEn = "Agri Marketing Dept",
            updatedHi = "आज सुबह 9:00",
            updatedEn = "Today 9:00 AM"
        ),
        "Mango (Himsagar)" to MarketItemLocale(
            marketHi = "इंग्लिशबाजार आढ़त",
            marketEn = "Englishbazar Wholesale",
            districtHi = "मालदा",
            districtEn = "Malda",
            unitHi = "₹ / क्विंटल (55/किग्रा)",
            unitEn = "₹ / Quintal (55/kg)",
            sourceHi = "मालदा मैंगो मर्चेंट्स",
            sourceEn = "Malda Mango Merchants",
            updatedHi = "कल शाम 5:00",
            updatedEn = "Yesterday 5:00 PM"
        )
    )

    fun getProduct(item: MarketPriceItem, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> item.productBn
        AppLanguage.HINDI -> item.productHi.ifBlank { item.productEn }
        AppLanguage.ENGLISH -> item.productEn
    }

    fun getMarketName(item: MarketPriceItem, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> item.marketNameBn
        AppLanguage.HINDI -> locales[item.productEn]?.marketHi ?: item.marketNameBn
        AppLanguage.ENGLISH -> locales[item.productEn]?.marketEn ?: item.marketNameBn
    }

    fun getDistrict(item: MarketPriceItem, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> item.districtBn
        AppLanguage.HINDI -> locales[item.productEn]?.districtHi ?: item.districtBn
        AppLanguage.ENGLISH -> locales[item.productEn]?.districtEn ?: item.districtBn
    }

    fun getUnit(item: MarketPriceItem, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> item.unit
        AppLanguage.HINDI -> locales[item.productEn]?.unitHi ?: CropTextFormats.toAsciiDigits(item.unit)
        AppLanguage.ENGLISH -> locales[item.productEn]?.unitEn ?: CropTextFormats.toAsciiDigits(item.unit)
    }

    fun getSource(item: MarketPriceItem, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> item.source
        AppLanguage.HINDI -> locales[item.productEn]?.sourceHi ?: item.source
        AppLanguage.ENGLISH -> locales[item.productEn]?.sourceEn ?: item.source
    }

    fun getLastUpdated(item: MarketPriceItem, lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> item.lastUpdated
        AppLanguage.HINDI -> locales[item.productEn]?.updatedHi ?: CropTextFormats.toAsciiDigits(item.lastUpdated)
        AppLanguage.ENGLISH -> locales[item.productEn]?.updatedEn ?: CropTextFormats.toAsciiDigits(item.lastUpdated)
    }

    fun getTrendLabel(trend: String, lang: AppLanguage): String = when (trend) {
        "UP" -> when (lang) {
            AppLanguage.BENGALI -> "ঊর্ধ্বমুখী"
            AppLanguage.HINDI -> "बढ़त (तेजी)"
            AppLanguage.ENGLISH -> "Rising"
        }
        "DOWN" -> when (lang) {
            AppLanguage.BENGALI -> "নিম্নমুখী"
            AppLanguage.HINDI -> "गिरावट (मंदी)"
            AppLanguage.ENGLISH -> "Falling"
        }
        else -> when (lang) {
            AppLanguage.BENGALI -> "স্থির"
            AppLanguage.HINDI -> "स्थिर"
            AppLanguage.ENGLISH -> "Stable"
        }
    }
}
