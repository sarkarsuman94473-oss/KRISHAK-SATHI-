package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.AppLanguage
import com.example.data.model.PromoBannerConfig
import com.example.data.repository.BannerRepository
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import kotlinx.coroutines.delay

@Composable
fun PromoBannerCarousel(
    currentLanguage: AppLanguage,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bannerRepo = remember { BannerRepository(context) }

    LaunchedEffect(Unit) {
        bannerRepo.ensureSeeded()
        bannerRepo.pullFromFirestore()
    }

    val initialFallback = remember(currentLanguage.code) {
        BannerRepository.defaultSeedBanners.filter {
            it.languageCode == currentLanguage.code && it.isActive
        }.sortedBy { it.orderIndex }
    }

    val bannersFlow = remember(currentLanguage.code) {
        bannerRepo.observeActiveBanners(currentLanguage)
    }
    val dbBanners by bannersFlow.collectAsState(initial = initialFallback)

    val banners = remember(dbBanners, currentLanguage.code, initialFallback) {
        val strict = dbBanners
            .filter { it.languageCode == currentLanguage.code && it.isActive }
            .sortedBy { it.orderIndex }
        strict.ifEmpty { initialFallback }
    }

    var previewBanner by remember { mutableStateOf<PromoBannerConfig?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("promo_banner_carousel_container")
    ) {
        key(currentLanguage.code) {
            val pagerState = rememberPagerState(initialPage = 0, pageCount = { banners.size.coerceAtLeast(1) })

            LaunchedEffect(pagerState, banners.size) {
                if (banners.size <= 1) return@LaunchedEffect
                while (true) {
                    delay(5000)
                    if (!pagerState.isScrollInProgress) {
                        val nextPage = (pagerState.currentPage + 1) % banners.size
                        pagerState.animateScrollToPage(nextPage)
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                key = { page -> "${currentLanguage.code}_${banners.getOrNull(page)?.id ?: page}" },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .heightIn(min = 190.dp, max = 300.dp)
        ) { page ->
            val banner = banners[page]
                val drawableId = remember(banner.drawableResName) {
                    BannerRepository.resolveDrawableRes(banner.drawableResName)
                }

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            if (banner.targetCardId.isNotBlank()) {
                                onCardClick(banner.targetCardId)
                            } else {
                                previewBanner = banner
                            }
                        }
                        .testTag("promo_banner_card_${currentLanguage.code}_$page"),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, RoyalBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = RoyalGreenSecondaryBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(RoyalGreenPrimaryBg)
                    ) {
                        when {
                            banner.imageUrl.isNotBlank() -> {
                                SubcomposeAsyncImage(
                                    model = banner.imageUrl,
                                    contentDescription = banner.title,
                                    contentScale = if (banner.isPoster) ContentScale.Fit else ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    loading = {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(color = RoyalGold, modifier = Modifier.size(28.dp))
                                        }
                                    },
                                    error = {
                                        EnglishOrFallbackArtwork(banner = banner)
                                    }
                                )
                            }
                            drawableId != null -> {
                                Image(
                                    painter = painterResource(id = drawableId),
                                    contentDescription = banner.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = if (banner.hideTextOverlay) {
                                        ContentScale.Crop
                                    } else if (banner.isPoster) {
                                        ContentScale.Fit
                                    } else {
                                        ContentScale.Crop
                                    }
                                )
                            }
                            else -> {
                                EnglishOrFallbackArtwork(banner = banner)
                            }
                        }

                        if (!banner.hideTextOverlay) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = if (banner.isPoster) 0.14f else 0.22f),
                                                Color.Black.copy(alpha = if (banner.isPoster) 0.56f else 0.76f)
                                            )
                                        )
                                    )
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (banner.badgeText.isNotBlank()) {
                                    Surface(
                                        color = RoyalGreenEmerald.copy(alpha = 0.94f),
                                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = banner.badgeText,
                                            color = RoyalTextPrimary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable { previewBanner = banner }
                                ) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.45f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Fullscreen,
                                                contentDescription = "View Fullscreen",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = banner.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    lineHeight = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = banner.subtitle,
                                        color = RoyalTextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = RoyalGold,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TouchApp,
                                                contentDescription = null,
                                                tint = RoyalGreenPrimaryBg,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = banner.ctaLabel.ifBlank {
                                                    when (currentLanguage) {
                                                        AppLanguage.BENGALI -> "খুলুন"
                                                        AppLanguage.HINDI -> "खोलें"
                                                        AppLanguage.ENGLISH -> "Open"
                                                    }
                                                },
                                                color = RoyalGreenPrimaryBg,
                                                fontSize = 10.sp,
                                                lineHeight = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Hindi graphic posters already carry rich typography: keep overlay strictly minimal (expand button only)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(48.dp)
                                    .clickable { previewBanner = banner }
                            ) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.55f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Fullscreen,
                                            contentDescription = "View Fullscreen",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pager indicator dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                banners.indices.forEach { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) RoyalGold else RoyalBorder)
                    )
                }
            }
        }
    }

    previewBanner?.let { target ->
        val drawableId = BannerRepository.resolveDrawableRes(target.drawableResName)
        Dialog(
            onDismissRequest = { previewBanner = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                color = Color.Black
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    when {
                        target.imageUrl.isNotBlank() -> {
                            SubcomposeAsyncImage(
                                model = target.imageUrl,
                                contentDescription = target.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 56.dp, bottom = 56.dp, start = 8.dp, end = 8.dp)
                            )
                        }
                        drawableId != null -> {
                            Image(
                                painter = painterResource(id = drawableId),
                                contentDescription = target.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 56.dp, bottom = 56.dp, start = 8.dp, end = 8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        else -> {
                            EnglishOrFallbackArtwork(
                                banner = target,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .align(Alignment.Center)
                                    .padding(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { previewBanner = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = target.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnglishOrFallbackArtwork(
    banner: PromoBannerConfig,
    modifier: Modifier = Modifier
) {
    val icon = when (banner.targetCardId) {
        "machinery" -> Icons.Default.PrecisionManufacturing
        "nursery" -> Icons.Default.LocalFlorist
        else -> Icons.Default.AutoAwesome
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0A321F),
                        RoyalGreenEmerald,
                        Color(0xFF1A5B39)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = RoyalGold.copy(alpha = 0.22f),
            modifier = Modifier
                .size(132.dp)
                .align(Alignment.CenterEnd)
        )
        Text(
            text = "KRISHAK SATHI",
            color = RoyalSoftGold.copy(alpha = 0.25f),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

