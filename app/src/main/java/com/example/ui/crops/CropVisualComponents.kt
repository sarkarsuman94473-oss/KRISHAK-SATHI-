package com.example.ui.crops

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.AppLanguage
import com.example.data.model.CropMasterGroup
import com.example.data.model.MasterCrop

/**
 * Renders either the crop's custom PickVisualMedia URI, its strict cropId-matched bundled
 * photograph drawable, or a clean botanical illustration fallback so no crop ever displays
 * another crop's mismatched photo.
 */
@Composable
fun CropThumbnailVisual(
    crop: MasterCrop,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val customUri = crop.customImageUri
    val resolvedRes = crop.getResolvedImageResId()
    when {
        !customUri.isNullOrBlank() -> {
            SubcomposeAsyncImage(
                model = customUri,
                contentDescription = crop.getName(lang),
                contentScale = ContentScale.Crop,
                modifier = modifier.clip(RoundedCornerShape(12.dp)),
                error = {
                    BotanicalThumbFallback(crop = crop, modifier = Modifier.fillMaxSize())
                }
            )
        }
        resolvedRes != null -> {
            Image(
                painter = painterResource(id = resolvedRes),
                contentDescription = crop.getName(lang),
                contentScale = ContentScale.Crop,
                modifier = modifier.clip(RoundedCornerShape(12.dp))
            )
        }
        else -> {
            BotanicalThumbFallback(crop = crop, modifier = modifier)
        }
    }
}

@Composable
private fun BotanicalThumbFallback(
    crop: MasterCrop,
    modifier: Modifier = Modifier
) {
    val palette = botanicalPaletteForGroup(crop.getMasterGroup())
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(palette.first, palette.second)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.85f, size.height * 0.2f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = size.minDimension * 0.28f,
                center = Offset(size.width * 0.15f, size.height * 0.85f)
            )
        }
        Text(
            text = crop.iconEmoji,
            fontSize = 26.sp
        )
    }
}

@Composable
fun CropHeroVisualBanner(
    crop: MasterCrop,
    lang: AppLanguage,
    modifier: Modifier = Modifier,
    overridePhotoResId: Int? = null
) {
    val customUri = crop.customImageUri
    val resolvedRes = overridePhotoResId ?: crop.getResolvedImageResId()

    when {
        !customUri.isNullOrBlank() && overridePhotoResId == null -> {
            Box(modifier = modifier) {
                SubcomposeAsyncImage(
                    model = customUri,
                    contentDescription = crop.getName(lang),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        BotanicalHeroArtwork(crop = crop, lang = lang, modifier = Modifier.fillMaxSize())
                    }
                )
                PhotoAccuracyBadge(
                    isRealPhoto = true,
                    lang = lang,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                )
            }
        }
        resolvedRes != null -> {
            Box(modifier = modifier) {
                Image(
                    painter = painterResource(id = resolvedRes),
                    contentDescription = crop.getName(lang),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                PhotoAccuracyBadge(
                    isRealPhoto = true,
                    lang = lang,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                )
            }
        }
        else -> {
            BotanicalHeroArtwork(crop = crop, lang = lang, modifier = modifier)
        }
    }
}

@Composable
private fun BotanicalHeroArtwork(
    crop: MasterCrop,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val palette = botanicalPaletteForGroup(crop.getMasterGroup())
    Box(
        modifier = modifier.background(
            Brush.linearGradient(colors = listOf(palette.first, palette.second))
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = size.minDimension * 0.55f,
                center = Offset(size.width * 0.90f, size.height * 0.18f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.minDimension * 0.40f,
                center = Offset(size.width * 0.10f, size.height * 0.90f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.22f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = crop.iconEmoji, fontSize = 38.sp)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = crop.getName(lang),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = crop.scientificName,
                    color = Color.White.copy(alpha = 0.9f),
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                PhotoAccuracyBadge(isRealPhoto = false, lang = lang)
            }
        }
    }
}

@Composable
private fun PhotoAccuracyBadge(
    isRealPhoto: Boolean,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val text = if (isRealPhoto) {
        when (lang) {
            AppLanguage.BENGALI -> "📷 যাচাইকৃত ফসল ছবি"
            AppLanguage.HINDI -> "📷 सत्यापित फसल फोटो"
            AppLanguage.ENGLISH -> "📷 Verified Crop Photo"
        }
    } else {
        when (lang) {
            AppLanguage.BENGALI -> "🎨 উদ্ভিদ পরিচিতি চিত্রণ"
            AppLanguage.HINDI -> "🎨 वानस्पतिक परिचय चित्रण"
            AppLanguage.ENGLISH -> "🎨 Botanical Illustration"
        }
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.55f),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun botanicalPaletteForGroup(group: CropMasterGroup): Pair<Color, Color> = when (group) {
    CropMasterGroup.LEAFY_VEGETABLES -> Color(0xFF1B5E20) to Color(0xFF43A047)
    CropMasterGroup.VEGETABLES -> Color(0xFF2E7D32) to Color(0xFF66BB6A)
    CropMasterGroup.FRUITS -> Color(0xFFE65100) to Color(0xFFFB8C00)
    CropMasterGroup.FLOWERS -> Color(0xFF880E4F) to Color(0xFFD81B60)
    CropMasterGroup.CEREALS_GRAINS -> Color(0xFF827717) to Color(0xFFAFB42B)
    CropMasterGroup.PULSES_LEGUMES -> Color(0xFF4E342E) to Color(0xFF8D6E63)
    CropMasterGroup.OILSEEDS -> Color(0xFFF57F17) to Color(0xFFFBC02D)
    CropMasterGroup.SPICES_CONDIMENTS -> Color(0xFFBF360C) to Color(0xFFE64A19)
    CropMasterGroup.PLANTATION_COMMERCIAL -> Color(0xFF004D40) to Color(0xFF00897B)
    CropMasterGroup.MEDICINAL_AROMATIC -> Color(0xFF1B5E20) to Color(0xFF26A69A)
    CropMasterGroup.NURSERY_INDOOR_ORNAMENTAL -> Color(0xFF006064) to Color(0xFF00ACC1)
    CropMasterGroup.OTHER_AGROFORESTRY -> Color(0xFF33691E) to Color(0xFF689F38)
}
