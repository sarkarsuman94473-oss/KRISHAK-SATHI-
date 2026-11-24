package com.example.data.model

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    BENGALI("bn", "Bengali", "বাংলা"),
    HINDI("hi", "Hindi", "हिन्दी"),
    ENGLISH("en", "English", "English")
}

enum class LandUnit(val code: String, val bnLabel: String, val hiLabel: String, val enLabel: String, val sqMeters: Double) {
    KATHA("katha", "কাঠা (Katha)", "कट्ठा (Katha)", "Katha", 66.89),
    BIGHA("bigha", "বিঘা (Bigha)", "बीघा (Bigha)", "Bigha", 1337.8),
    SATAK("satak", "শতক / ডেসিমাল", "डेसिमल / शतक", "Satak / Decimal", 40.47),
    ACRE("acre", "একর (Acre)", "एकड़ (Acre)", "Acre", 4046.86),
    HECTARE("hectare", "হেক্টর (Hectare)", "हेक्टेयर (Hectare)", "Hectare", 10000.0),
    SQ_METER("sqm", "বর্গমিটার (Sq M)", "वर्ग मीटर (Sq M)", "Square Meter", 1.0);

    fun toHectares(value: Double): Double = (value * sqMeters) / 10000.0
    fun toAcres(value: Double): Double = (value * sqMeters) / 4046.86
    fun toBigha(value: Double): Double = (value * sqMeters) / 1337.8
    fun toKatha(value: Double): Double = (value * sqMeters) / 66.89

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> bnLabel
        AppLanguage.HINDI -> hiLabel
        AppLanguage.ENGLISH -> enLabel
    }

    companion object {
        fun convert(value: Double, from: LandUnit, to: LandUnit): Double {
            val sqM = value * from.sqMeters
            return sqM / to.sqMeters
        }
    }
}
