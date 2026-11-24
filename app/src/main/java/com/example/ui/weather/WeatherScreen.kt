package com.example.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.util.L10n

data class ForecastDay(
    val dayName: String,
    val temp: String,
    val rainProb: String,
    val iconEmoji: String,
    val advice: String
)

@Composable
fun WeatherScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    val forecast = when (currentLanguage) {
        AppLanguage.BENGALI -> listOf(
            ForecastDay("আজ", "২৮° সে.", "১০%", "🌤", "সেচ দেওয়ার স্বাভাবিক সময়। বিকেলে স্প্রে করা নিরাপদ।"),
            ForecastDay("আগামীকাল", "২৬° সে.", "৬০%", "🌧", "ভারী বৃষ্টির সম্ভাবনা আছে। আজ জমিতে জল সেচ না দেওয়াই ভালো।"),
            ForecastDay("পরশু", "২৫° সে.", "৭০%", "⛈", "আগামী ২৪ ঘণ্টায় বৃষ্টি হতে পারে। স্প্রে করার আগে আবহাওয়া আবার পরীক্ষা করুন।"),
            ForecastDay("দিন ৪", "২৭° সে.", "২০%", "⛅", "আকাশ পরিষ্কার থাকবে। সার ছিটানোর ভালো সময়।"),
            ForecastDay("দিন ৫", "২৯° সে.", "৫%", "☀️", "রোদ ঝলমলে দিন। ফসল শুকানো ও কাটার উপযুক্ত সময়।"),
            ForecastDay("দিন ৬", "৩০° সে.", "০%", "☀️", "শুষ্ক দিন। সবজির গোড়ায় হালকা জল সেচ দিন।"),
            ForecastDay("দিন ৭", "২৯° সে.", "১০%", "🌤", "অনুকূল আবহাওয়া। আগাছা পরিষ্কারের কাজ করতে পারেন।")
        )
        AppLanguage.HINDI -> listOf(
            ForecastDay("आज", "28° से.", "10%", "🌤", "सिंचाई के लिए सामान्य समय। शाम को छिड़काव सुरक्षित है।"),
            ForecastDay("कल", "26° से.", "60%", "🌧", "भारी बारिश की संभावना है। आज खेत में सिंचाई न करना बेहतर है।"),
            ForecastDay("परसों (दिन 3)", "25° से.", "70%", "⛈", "अगले 24 घंटों में बारिश हो सकती है। छिड़काव से पहले मौसम फिर जांचें।"),
            ForecastDay("दिन 4", "27° से.", "20%", "⛅", "आसमान साफ रहेगा। खाद डालने के लिए अच्छा समय है।"),
            ForecastDay("दिन 5", "29° से.", "5%", "☀️", "तेज धूप वाला दिन। फसल सुखाने और कटाई के लिए उपयुक्त समय।"),
            ForecastDay("दिन 6", "30° से.", "0%", "☀️", "शुष्क दिन। सब्जियों की जड़ों में हल्की सिंचाई करें।"),
            ForecastDay("दिन 7", "29° से.", "10%", "🌤", "अनुकूल मौसम। खरपतवार साफ करने का कार्य कर सकते हैं।")
        )
        AppLanguage.ENGLISH -> listOf(
            ForecastDay("Today", "28° C", "10%", "🌤", "Normal time for irrigation. Safe for evening spraying."),
            ForecastDay("Tomorrow", "26° C", "60%", "🌧", "Heavy rain likely. Avoid irrigating fields today."),
            ForecastDay("Day 3", "25° C", "70%", "⛈", "Rain expected within 24 hours. Recheck weather before spraying."),
            ForecastDay("Day 4", "27° C", "20%", "⛅", "Clear sky expected. Good timing for fertilizer top-dressing."),
            ForecastDay("Day 5", "29° C", "5%", "☀️", "Bright sunny day. Ideal for crop drying and harvesting."),
            ForecastDay("Day 6", "30° C", "0%", "☀️", "Dry weather. Give light irrigation to vegetable root zones."),
            ForecastDay("Day 7", "29° C", "10%", "🌤", "Favorable conditions. Good day for weeding and field sanitation.")
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("weather_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🌦 ${L10n.t("weather_title", currentLanguage)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 28.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "ℹ️ আঞ্চলিক মৌসুমী রেফারেন্স পূর্বাভাস: স্প্রে বা সেচের আগে লাইভ স্থানীয় বৃষ্টির সতর্কতার জন্য ভারত আবহাওয়া দপ্তর (mausam.imd.gov.in) যাচাই করুন।"
                            AppLanguage.HINDI -> "ℹ️ क्षेत्रीय मौसमी संदर्भ सलाह: छिड़काव या सिंचाई से पहले लाइव स्थानीय मौसम चेतावनी के लिए भारतीय मौसम विभाग (mausam.imd.gov.in) जांचें।"
                            AppLanguage.ENGLISH -> "ℹ️ Regional Seasonal Reference Advisory: Verify live hyper-local rainfall and storm alerts with IMD Mausam (mausam.imd.gov.in) before spraying."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                // Today High-Visibility Weather Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AgriGreenPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = L10n.t("weather_region_label", currentLanguage),
                                    color = com.example.ui.theme.RoyalTextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                                Text(
                                    text = L10n.t("weather_today", currentLanguage),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    lineHeight = 30.sp
                                )
                                Text(
                                    text = "28° C (Max) / 21° C (Min)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp
                                )
                            }
                            Text(text = "🌤", fontSize = 48.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WeatherMetric(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.WaterDrop,
                                label = L10n.t("weather_rain_chance", currentLanguage),
                                value = "10%"
                            )
                            WeatherMetric(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Thermostat,
                                label = L10n.t("weather_humidity", currentLanguage),
                                value = "72%"
                            )
                            WeatherMetric(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Air,
                                label = L10n.t("weather_wind_speed", currentLanguage),
                                value = "11 km/h"
                            )
                        }
                    }
                }

                // Most Important: Weather converted into Direct Farming Advice (Dark-mode safe container)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📢 ${L10n.t("weather_advisory_title", currentLanguage)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = 22.sp
                            )
                        }
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "• আগামীকাল ভারী বৃষ্টির সম্ভাবনা আছে। আজ জমিতে জল সেচ না দেওয়াই ভালো।\n• আগামী ২৪ ঘণ্টায় বৃষ্টি হতে পারে। কীটনাশক বা সার স্প্রে করার আগে আবহাওয়া আবার পরীক্ষা করুন।\n• জমিতে জল নিকাশী নালা তৈরি রাখুন যাতে অতিরিক্ত জল সহজে বের হতে পারে।"
                                AppLanguage.HINDI -> "• कल भारी बारिश की संभावना है। आज खेत में सिंचाई न करना बेहतर है।\n• अगले 24 घंटों में बारिश हो सकती है। कीटनाशक या खाद छिड़कने से पहले मौसम दोबारा जांचें।\n• खेत में जल निकासी की नालियां तैयार रखें ताकि अतिरिक्त पानी आसानी से निकल सके।"
                                AppLanguage.ENGLISH -> "• Heavy rain is likely tomorrow. Avoid irrigating fields today.\n• Rain may occur within the next 24 hours. Recheck forecast before spraying pesticides or fertilizers.\n• Keep field drainage channels clear so excess water drains out easily."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 23.sp
                        )
                    }
                }

                Text(
                    text = "📅 ${L10n.t("weather_7day_forecast", currentLanguage)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary,
                    lineHeight = 24.sp
                )

                forecast.forEach { f ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = f.iconEmoji, fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = f.dayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        lineHeight = 22.sp,
                                        color = AgriTextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = f.temp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "বৃষ্টি: ${f.rainProb}"
                                        AppLanguage.HINDI -> "बारिश: ${f.rainProb}"
                                        AppLanguage.ENGLISH -> "Rain: ${f.rainProb}"
                                    },
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = AgriTextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "পরামর্শ: ${f.advice}"
                                        AppLanguage.HINDI -> "सलाह: ${f.advice}"
                                        AppLanguage.ENGLISH -> "Advice: ${f.advice}"
                                    },
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    color = AgriTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center)
        Text(
            text = label,
            color = com.example.ui.theme.RoyalTextSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
