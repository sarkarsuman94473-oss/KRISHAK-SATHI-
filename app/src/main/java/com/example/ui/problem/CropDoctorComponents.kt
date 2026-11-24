package com.example.ui.problem

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.LeafCheckRecord
import com.example.data.model.AppLanguage
import com.example.data.model.VerifiedTreatment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal data class CropDoctorPalette(
    val isDark: Boolean,
    val emeraldPrimary: Color,
    val surfaceBg: Color,
    val surfaceLow: Color,
    val surfaceCard: Color,
    val surfaceTonal: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val scannerAccent: Color,
    val tertiaryAccent: Color,
    val tertiaryBadgeBg: Color,
    val tertiaryBadgeText: Color
)

@Composable
internal fun rememberCropDoctorPalette(): CropDoctorPalette {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) {
        CropDoctorPalette(
            isDark = true,
            emeraldPrimary = com.example.ui.theme.RoyalGreenEmerald,
            surfaceBg = com.example.ui.theme.RoyalGreenPrimaryBg,
            surfaceLow = com.example.ui.theme.RoyalGreenSecondaryBg,
            surfaceCard = com.example.ui.theme.RoyalGreenCardBg,
            surfaceTonal = com.example.ui.theme.RoyalInputBg,
            secondaryContainer = Color(0xFF0E5A3E),
            onSecondaryContainer = com.example.ui.theme.RoyalSuccessText,
            scannerAccent = com.example.ui.theme.RoyalGold,
            tertiaryAccent = com.example.ui.theme.RoyalGold,
            tertiaryBadgeBg = com.example.ui.theme.RoyalWarningBg,
            tertiaryBadgeText = com.example.ui.theme.RoyalSoftGold
        )
    } else {
        CropDoctorPalette(
            isDark = false,
            emeraldPrimary = Color(0xFF00652C),
            surfaceBg = Color(0xFFFFF8F5),
            surfaceLow = Color(0xFFFAF2EE),
            surfaceCard = Color(0xFFFFFFFF),
            surfaceTonal = Color(0xFFEEE7E3),
            secondaryContainer = Color(0xFF6BFF8F),
            onSecondaryContainer = Color(0xFF005321),
            scannerAccent = Color(0xFF6BFF8F),
            tertiaryAccent = Color(0xFF854600),
            tertiaryBadgeBg = Color(0xFFFFDCC3),
            tertiaryBadgeText = Color(0xFF2F1500)
        )
    }
}

@Composable
internal fun CropDoctorHeader(
    currentLanguage: AppLanguage,
    palette: CropDoctorPalette,
    onBack: () -> Unit,
    onCycleLanguage: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Surface(
        color = palette.surfaceBg.copy(alpha = 0.96f),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("problem_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.ic_krishak_sathi_logo),
                        contentDescription = "Krishak Sathi Logo",
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "কৃষক সাথী"
                                    AppLanguage.HINDI -> "कृषक साथी"
                                    AppLanguage.ENGLISH -> "Krishak Sathi"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (palette.isDark) palette.onSecondaryContainer else palette.emeraldPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = palette.secondaryContainer.copy(alpha = if (palette.isDark) 0.55f else 0.35f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Eco,
                                        contentDescription = null,
                                        tint = if (palette.isDark) palette.scannerAccent else palette.emeraldPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.BENGALI -> "সাথী"
                                            AppLanguage.HINDI -> "साथी"
                                            AppLanguage.ENGLISH -> "Sathi"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.onSecondaryContainer,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "ফসলের স্বাস্থ্য • Crop Health"
                                AppLanguage.HINDI -> "फसल स्वास्थ्य • Crop Health"
                                AppLanguage.ENGLISH -> "Crop Health"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = palette.surfaceTonal,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onCycleLanguage)
                            .testTag("crop_doctor_lang_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Switch Language",
                                tint = if (palette.isDark) palette.scannerAccent else palette.emeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "বাংলা"
                                    AppLanguage.HINDI -> "HI"
                                    AppLanguage.ENGLISH -> "EN"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenProfile,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(palette.surfaceTonal)
                            .border(
                                1.5.dp,
                                (if (palette.isDark) palette.scannerAccent else palette.emeraldPrimary).copy(alpha = 0.25f),
                                CircleShape
                            )
                            .testTag("crop_doctor_profile_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Farmer Profile",
                            tint = if (palette.isDark) palette.scannerAccent else palette.emeraldPrimary
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        }
    }
}

@Composable
internal fun AiCropDoctorBannerCard(
    currentLanguage: AppLanguage,
    palette: CropDoctorPalette
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_ready_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_dot"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surfaceLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(palette.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = palette.onSecondaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "AI ক্রপ ডক্টর"
                            AppLanguage.HINDI -> "AI क्रॉप डॉक्टर"
                            AppLanguage.ENGLISH -> "AI Crop Doctor"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "তাৎক্ষণিক ফসল রোগ নির্ণয় ও সমাধান"
                            AppLanguage.HINDI -> "तुरंत फसल रोग निदान एवं समाधान"
                            AppLanguage.ENGLISH -> "Instant crop disease diagnosis & cure"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = palette.surfaceTonal,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                (if (palette.isDark) palette.scannerAccent else palette.emeraldPrimary)
                                    .copy(alpha = pulseAlpha)
                            )
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "AI প্রস্তুত"
                            AppLanguage.HINDI -> "AI तैयार"
                            AppLanguage.ENGLISH -> "AI Ready"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
internal fun LeafScannerViewport(
    selectedBitmap: Bitmap?,
    cropDisplayName: String,
    isAnalyzing: Boolean,
    currentLanguage: AppLanguage,
    palette: CropDoctorPalette
) {
    val scanTransition = rememberInfiniteTransition(label = "scanner_line")
    val scanProgress by scanTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_y"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(com.example.ui.theme.RoyalGreenSecondaryBg)
    ) {
        if (selectedBitmap != null) {
            Image(
                bitmap = selectedBitmap.asImageBitmap(),
                contentDescription = "Selected Plant Leaf Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_promo_disease_detect_1793678742155),
                contentDescription = "Leaf Disease Scanner Viewfinder",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Ambient gradient vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.78f)
                        )
                    )
                )
        )

        // Targeting corner reticle brackets & sweeping green laser line
        val accentColor = palette.scannerAccent
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pad = 22.dp.toPx()
            val bracketLen = 24.dp.toPx()
            val strokeW = 3.5.dp.toPx()

            // Sweeping horizontal scan beam
            val scanY = size.height * scanProgress
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        accentColor.copy(alpha = 0.9f),
                        Color.White.copy(alpha = 0.95f),
                        accentColor.copy(alpha = 0.9f),
                        Color.Transparent
                    )
                ),
                start = Offset(pad, scanY),
                end = Offset(size.width - pad, scanY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Top-left bracket
            drawLine(accentColor, Offset(pad, pad), Offset(pad + bracketLen, pad), strokeW, StrokeCap.Round)
            drawLine(accentColor, Offset(pad, pad), Offset(pad, pad + bracketLen), strokeW, StrokeCap.Round)

            // Top-right bracket
            drawLine(accentColor, Offset(size.width - pad, pad), Offset(size.width - pad - bracketLen, pad), strokeW, StrokeCap.Round)
            drawLine(accentColor, Offset(size.width - pad, pad), Offset(size.width - pad, pad + bracketLen), strokeW, StrokeCap.Round)

            // Bottom-left bracket
            drawLine(accentColor, Offset(pad, size.height - pad), Offset(pad + bracketLen, size.height - pad), strokeW, StrokeCap.Round)
            drawLine(accentColor, Offset(pad, size.height - pad), Offset(pad, size.height - pad - bracketLen), strokeW, StrokeCap.Round)

            // Bottom-right bracket
            drawLine(accentColor, Offset(size.width - pad, size.height - pad), Offset(size.width - pad - bracketLen, size.height - pad), strokeW, StrokeCap.Round)
            drawLine(accentColor, Offset(size.width - pad, size.height - pad), Offset(size.width - pad, size.height - pad - bracketLen), strokeW, StrokeCap.Round)
        }

        // HUD Overlay Chips
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.62f)
                ) {
                    Text(
                        text = cropDisplayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.68f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                isAnalyzing -> when (currentLanguage) {
                                    AppLanguage.BENGALI -> "স্ক্যান হচ্ছে..."
                                    AppLanguage.HINDI -> "स्कैन हो रहा है..."
                                    AppLanguage.ENGLISH -> "Analyzing..."
                                }
                                selectedBitmap != null -> when (currentLanguage) {
                                    AppLanguage.BENGALI -> "ছবি প্রস্তুত"
                                    AppLanguage.HINDI -> "फोटो तैयार"
                                    AppLanguage.ENGLISH -> "Photo Ready"
                                }
                                else -> when (currentLanguage) {
                                    AppLanguage.BENGALI -> "AI ভিশন প্রস্তুত"
                                    AppLanguage.HINDI -> "AI विज़न तैयार"
                                    AppLanguage.ENGLISH -> "AI Vision Ready"
                                }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor
                        )
                    }
                }
            }

            if (selectedBitmap == null) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.58f)
            ) {
                Text(
                    text = if (selectedBitmap != null) {
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "নিচে 'রোগ পরীক্ষা করুন' বাটনে ট্যাপ করুন"
                            AppLanguage.HINDI -> "नीचे 'रोग जांचें' बटन पर टैप करें"
                            AppLanguage.ENGLISH -> "Tap 'Analyze Leaf' below for report"
                        }
                    } else {
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "আক্রান্ত পাতাটি মাঝখানে রাখুন"
                            AppLanguage.HINDI -> "पत्ती को बीच में रखें"
                            AppLanguage.ENGLISH -> "Place leaf inside center frame"
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.95f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
internal fun RecentLeafCheckCard(
    record: LeafCheckRecord,
    currentLanguage: AppLanguage,
    palette: CropDoctorPalette,
    onViewDetails: () -> Unit
) {
    val timeLabel = try {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(record.timestampMs))
    } catch (_: Exception) {
        ""
    }
    val previewSnippet = record.diagnosisText
        .lineSequence()
        .map { it.trim() }
        .firstOrNull { it.isNotBlank() && !it.startsWith("⚠️") }
        ?: record.diagnosisText.take(120)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surfaceCard),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.surfaceTonal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = if (palette.isDark) palette.scannerAccent else palette.emeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = record.cropName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val meta = listOfNotNull(
                            record.landArea.takeIf { it.isNotBlank() },
                            timeLabel.takeIf { it.isNotBlank() }
                        ).joinToString(" • ")
                        if (meta.isNotBlank()) {
                            Text(
                                text = meta,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = palette.tertiaryBadgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(palette.tertiaryAccent)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "নিরাময়যোগ্য"
                                AppLanguage.HINDI -> "उपचार योग्य"
                                AppLanguage.ENGLISH -> "Treatable"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.tertiaryBadgeText
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = palette.surfaceLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = palette.tertiaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "AI নির্ণয় সারসংক্ষেপ:"
                                AppLanguage.HINDI -> "AI निदान सारांश:"
                                AppLanguage.ENGLISH -> "AI Diagnosis Summary:"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.tertiaryAccent
                        )
                    }
                    Text(
                        text = previewSnippet,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = if (palette.isDark) palette.scannerAccent else palette.emeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "সংরক্ষিত AI স্ক্যান"
                            AppLanguage.HINDI -> "सहेजा गया AI स्कैन"
                            AppLanguage.ENGLISH -> "Saved AI Check"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onViewDetails)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.BENGALI -> "সম্পূর্ণ সমাধান দেখুন"
                                AppLanguage.HINDI -> "पूरा उपचार देखें"
                                AppLanguage.ENGLISH -> "View Full Cure Steps"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (palette.isDark) palette.onSecondaryContainer else palette.emeraldPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (palette.isDark) palette.onSecondaryContainer else palette.emeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DiseaseLibraryItemCard(
    treatment: VerifiedTreatment,
    currentLanguage: AppLanguage,
    palette: CropDoctorPalette,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surfaceCard),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surfaceTonal)
            ) {
                Image(
                    painter = painterResource(
                        id = treatment.frontImageResId ?: R.drawable.img_promo_disease_detect_1793678742155
                    ),
                    contentDescription = treatment.getProblemName(currentLanguage),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.68f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = treatment.getCropDisplayName(currentLanguage),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = treatment.getProblemName(currentLanguage),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = treatment.getProductTypeLabel(currentLanguage),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${
                        when (currentLanguage) {
                            AppLanguage.BENGALI -> "উপায়: "
                            AppLanguage.HINDI -> "उपाय: "
                            AppLanguage.ENGLISH -> "Remedy: "
                        }
                    }${treatment.getBiologicalOption(currentLanguage)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (palette.isDark) palette.onSecondaryContainer else palette.emeraldPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun LeafDiagnosisDetailDialog(
    record: LeafCheckRecord,
    currentLanguage: AppLanguage,
    palette: CropDoctorPalette,
    onDismiss: () -> Unit,
    onOpenCalculator: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📋 ${record.cropName}",
                fontWeight = FontWeight.Bold,
                color = if (palette.isDark) palette.onSecondaryContainer else palette.emeraldPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (record.landArea.isNotBlank()) {
                    Text(
                        text = record.landArea,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = record.diagnosisText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onOpenCalculator()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.emeraldPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "ডোজ হিসাব"
                        AppLanguage.HINDI -> "खुराक गणना"
                        AppLanguage.ENGLISH -> "Dosage Calc"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "বন্ধ করুন"
                        AppLanguage.HINDI -> "बंद करें"
                        AppLanguage.ENGLISH -> "Close"
                    }
                )
            }
        }
    )
}

@Composable
internal fun LibraryTreatmentDetailDialog(
    treatment: VerifiedTreatment,
    currentLanguage: AppLanguage,
    palette: CropDoctorPalette,
    onDismiss: () -> Unit,
    onOpenCalculator: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = treatment.getProblemName(currentLanguage),
                fontWeight = FontWeight.Bold,
                color = if (palette.isDark) palette.onSecondaryContainer else palette.emeraldPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${treatment.getCropDisplayName(currentLanguage)} • ${treatment.getProductTypeLabel(currentLanguage)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.tertiaryAccent,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = treatment.getSymptoms(currentLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.BENGALI -> "🌿 জৈব ও প্রথম করণীয়:"
                        AppLanguage.HINDI -> "🌿 जैविक एवं प्राथमिक उपचार:"
                        AppLanguage.ENGLISH -> "🌿 Biological & First Action:"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (palette.isDark) palette.onSecondaryContainer else palette.emeraldPrimary
                )
                Text(
                    text = "${treatment.getFirstAction(currentLanguage)}\n${treatment.getBiologicalOption(currentLanguage)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                treatment.getActiveIngredient(currentLanguage)?.let { ing ->
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "🧪 অনুমোদিত ওষুধ ও মাত্রা: $ing (${treatment.getDosePerLitre(currentLanguage)})"
                            AppLanguage.HINDI -> "🧪 अनुमोदित दवा व मात्रा: $ing (${treatment.getDosePerLitre(currentLanguage)})"
                            AppLanguage.ENGLISH -> "🧪 Verified Chemical & Dose: $ing (${treatment.getDosePerLitre(currentLanguage)})"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "🛡️ ${treatment.getSafetyInstructions(currentLanguage)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${treatment.officialSource} • PHI: ${treatment.preHarvestIntervalDays}d",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onOpenCalculator()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.emeraldPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "সঠিক ডোজ মাপুন"
                        AppLanguage.HINDI -> "सही खुराक गिनें"
                        AppLanguage.ENGLISH -> "Open Dosage Calc"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    when (currentLanguage) {
                        AppLanguage.BENGALI -> "বন্ধ করুন"
                        AppLanguage.HINDI -> "बंद करें"
                        AppLanguage.ENGLISH -> "Close"
                    }
                )
            }
        }
    )
}
