package com.example.ui.machinery

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.data.datasource.MachineryImageSourceType
import com.example.data.datasource.MachineryPhotoRegistry
import com.example.data.model.AppLanguage
import com.example.data.model.MachineryBeginnerGuide
import com.example.data.model.MachineryDealerOffer
import com.example.data.model.MachineryProduct
import com.example.ui.theme.MachineryCardBadgeBg
import com.example.ui.theme.MachineryCardBadgeText
import com.example.ui.theme.MachineryCardBodyInk
import com.example.ui.theme.MachineryCardBorder
import com.example.ui.theme.MachineryCardSoftMint
import com.example.ui.theme.MachineryCardTitleInk
import com.example.ui.theme.MachineryCardWarnBg
import com.example.ui.theme.MachineryCardWarnText
import com.example.ui.theme.MachineryCardWhite
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenPrimaryBg
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import kotlinx.coroutines.launch

/**
 * Displays agricultural equipment photo(s) strictly uncropped (`ContentScale.Fit`)
 * on a crisp white gallery frame (`MachineryCardWhite`) so machinery decals, blades,
 * and educational posters remain clean and undistorted.
 */
@Composable
fun MachineryEquipmentImage(
    photoTokens: List<String>,
    fallbackEmoji: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enableSwipePager: Boolean = true,
    showZoomHint: Boolean = true,
    onImageClick: ((Int) -> Unit)? = null
) {
    val cleanTokens = remember(photoTokens) { photoTokens.filter { it.isNotBlank() } }
    val shape = RoundedCornerShape(12.dp)

    Surface(
        color = MachineryCardWhite,
        border = BorderStroke(1.dp, MachineryCardBorder),
        shape = shape,
        modifier = modifier.clip(shape)
    ) {
        if (cleanTokens.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MachineryCardSoftMint)
                    .let { base -> if (onImageClick != null) base.clickable { onImageClick(0) } else base },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(fallbackEmoji, fontSize = 34.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Photo Pending",
                        fontSize = 10.sp,
                        color = MachineryCardBodyInk
                    )
                }
            }
        } else if (enableSwipePager && cleanTokens.size > 1) {
            val pagerState = rememberPagerState(pageCount = { cleanTokens.size })
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    SingleEquipmentPhotoFrame(
                        token = cleanTokens[page],
                        fallbackEmoji = fallbackEmoji,
                        contentDescription = "$contentDescription (${page + 1}/${cleanTokens.size})",
                        modifier = Modifier
                            .fillMaxSize()
                            .let { base ->
                                if (onImageClick != null) base.clickable { onImageClick(page) } else base
                            }
                    )
                }

                // Multi-photo counter badge (top-end)
                Surface(
                    color = RoyalGreenPrimaryBg.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            tint = RoyalGold,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "${pagerState.currentPage + 1}/${cleanTokens.size}",
                            color = RoyalSoftGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Gallery dots at bottom center
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .background(RoyalGreenPrimaryBg.copy(alpha = 0.78f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(cleanTokens.size) { dotIdx ->
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == dotIdx) 7.dp else 5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == dotIdx) RoyalGold
                                    else RoyalTextSecondary.copy(alpha = 0.5f)
                                )
                        )
                    }
                }

                if (showZoomHint && onImageClick != null) {
                    ZoomBadge(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .let { base -> if (onImageClick != null) base.clickable { onImageClick(0) } else base }
            ) {
                SingleEquipmentPhotoFrame(
                    token = cleanTokens.first(),
                    fallbackEmoji = fallbackEmoji,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize()
                )
                if (cleanTokens.size > 1) {
                    Surface(
                        color = RoyalGreenPrimaryBg.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "${cleanTokens.size} photos",
                            color = RoyalSoftGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
                if (showZoomHint && onImageClick != null) {
                    ZoomBadge(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoomBadge(modifier: Modifier = Modifier) {
    Surface(
        color = RoyalGreenPrimaryBg.copy(alpha = 0.86f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.ZoomOutMap,
            contentDescription = "Tap to view full-screen photo",
            tint = RoyalSoftGold,
            modifier = Modifier
                .padding(4.dp)
                .size(13.dp)
        )
    }
}

@Composable
private fun SingleEquipmentPhotoFrame(
    token: String,
    fallbackEmoji: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val drawableRes = remember(token) { MachineryPhotoRegistry.resolveDrawableRes(token) }
    Box(
        modifier = modifier
            .background(MachineryCardWhite)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (drawableRes != null) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            SubcomposeAsyncImage(
                model = token,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = RoyalGreenEmerald,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                error = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(fallbackEmoji, fontSize = 30.sp)
                    }
                }
            )
        }
    }
}

/**
 * Displays a transparent badge showing whether the equipment visual is an original
 * bundled/uploaded photograph vs an educational agricultural illustration poster.
 */
@Composable
fun MachinerySourceProvenanceBadge(
    photoTokens: List<String>,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val firstToken = photoTokens.firstOrNull()
    val (label, bg, fg, icon) = if (firstToken.isNullOrBlank()) {
        QuadBadge(
            text = when (lang) {
                AppLanguage.BENGALI -> "ছবি বাকি"
                AppLanguage.HINDI -> "फोटो बाकी"
                AppLanguage.ENGLISH -> "Photo Pending"
            },
            bg = MachineryCardSoftMint,
            fg = MachineryCardBodyInk,
            icon = Icons.Default.PhotoCamera
        )
    } else {
        when (MachineryPhotoRegistry.getImageSourceType(firstToken)) {
            MachineryImageSourceType.UPLOADED_ORIGINAL -> QuadBadge(
                text = when (lang) {
                    AppLanguage.BENGALI -> "আসল ছবি"
                    AppLanguage.HINDI -> "असली फोटो"
                    AppLanguage.ENGLISH -> "Original Photo"
                },
                bg = MachineryCardBadgeBg,
                fg = MachineryCardBadgeText,
                icon = Icons.Default.PhotoCamera
            )
            MachineryImageSourceType.POSTER_EDUCATIONAL_ILLUSTRATION -> QuadBadge(
                text = when (lang) {
                    AppLanguage.BENGALI -> "শিক্ষামূলক চিত্র"
                    AppLanguage.HINDI -> "शैक्षिक चित्र"
                    AppLanguage.ENGLISH -> "Educational Illustration"
                },
                bg = MachineryCardWarnBg,
                fg = MachineryCardWarnText,
                icon = Icons.Default.School
            )
            MachineryImageSourceType.DEALER_UPLOAD -> QuadBadge(
                text = when (lang) {
                    AppLanguage.BENGALI -> "ডিলার ছবি"
                    AppLanguage.HINDI -> "डीलर फोटो"
                    AppLanguage.ENGLISH -> "Partner Photo"
                },
                bg = MachineryCardBadgeBg,
                fg = MachineryCardBadgeText,
                icon = Icons.Default.Verified
            )
        }
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, fg.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(11.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fg, maxLines = 1)
        }
    }
}

private data class QuadBadge(
    val text: String,
    val bg: Color,
    val fg: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * 10-Point Beginner Learning Guide Card rendered on crisp white/soft-mint surfaces
 * for effortless farmer reading across English, Bengali, and Hindi.
 */
@Composable
fun MachineryBeginnerGuideCard(
    product: MachineryProduct,
    guide: MachineryBeginnerGuide,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val heading = when (lang) {
        AppLanguage.BENGALI -> "নতুন কৃষকদের জন্য ১০-দফা সহজ গাইড"
        AppLanguage.HINDI -> "नए किसानों के लिए 10-बिंदु सरल गाइड"
        AppLanguage.ENGLISH -> "10-Point Beginner Guide — Learn Before Buying or Renting"
    }
    val sections = remember(guide, lang) {
        listOf(
            Pair(
                when (lang) {
                    AppLanguage.BENGALI -> "১. এই যন্ত্রটি কী কাজ করে"
                    AppLanguage.HINDI -> "1. यह मशीन क्या काम करती है"
                    AppLanguage.ENGLISH -> "1. What it does"
                },
                guide.whatItDoes
            ),
            Pair(
                when (lang) {
                    AppLanguage.BENGALI -> "২. কাদের জন্য সবচেয়ে উপযোগী"
                    AppLanguage.HINDI -> "2. किसे इसकी ज़रूरत है"
                    AppLanguage.ENGLISH -> "2. Who needs it"
                },
                guide.whoNeedsIt
            ),
            Pair(
                when (lang) {
                    AppLanguage.BENGALI -> "৩. কোন মাপের জমি ও মাটির জন্য ভালো"
                    AppLanguage.HINDI -> "3. कितने बड़े खेत के लिए उपयुक्त"
                    AppLanguage.ENGLISH -> "3. Farm size & terrain suitability"
                },
                guide.farmSizeSuitability
            ),
            Pair(
                when (lang) {
                    AppLanguage.BENGALI -> "৪. ট্রাক্টর / মোটর শক্তি ও ক্ষমতা"
                    AppLanguage.HINDI -> "4. ट्रैक्टर / मोटर पावर आवश्यकता"
                    AppLanguage.ENGLISH -> "4. Tractor / Power requirement"
                },
                guide.tractorOrPowerRequirement
            ),
            Pair(
                when (lang) {
                    AppLanguage.BENGALI -> "৫. জ্বালানি বা শক্তির উৎস"
                    AppLanguage.HINDI -> "5. ईंधन या बिजली का प्रकार"
                    AppLanguage.ENGLISH -> "5. Fuel or power type"
                },
                guide.fuelOrPowerType
            ),
            Pair(
                when (lang) {
                    AppLanguage.BENGALI -> "৬. ধাপে ধাপে ব্যবহারের নিয়ম"
                    AppLanguage.HINDI -> "6. चरण-दर-चरण उपयोग विधि"
                    AppLanguage.ENGLISH -> "6. Step-by-step usage"
                },
                guide.stepByStepUsage
            ),
            Pair(
                when (lang) {
                    AppLanguage.BENGALI -> "৭. নিরাপত্তা সতর্কতা (অবশ্যই মানুন)"
                    AppLanguage.HINDI -> "7. सुरक्षा सावधानियां"
                    AppLanguage.ENGLISH -> "7. Safety precautions"
                },
                guide.safetyPrecautions
            ),
            Pair(
                when (lang) {
                    AppLanguage.BENGALI -> "৮. যে ভুলগুলো এড়িয়ে চলবেন"
                    AppLanguage.HINDI -> "8. सामान्य गलतियां जिनसे बचें"
                    AppLanguage.ENGLISH -> "8. Common mistakes to avoid"
                },
                guide.commonMistakesToAvoid
            ),
            Pair(
                when (lang) {
                    AppLanguage.BENGALI -> "৯. পরিচর্যা ও রক্ষণাবেক্ষণ চেকলিস্ট"
                    AppLanguage.HINDI -> "9. रखरखाव और सर्विसिंग चेकलिस्ट"
                    AppLanguage.ENGLISH -> "9. Maintenance checklist"
                },
                guide.maintenanceChecklist
            ),
            Pair(
                when (lang) {
                    AppLanguage.BENGALI -> "১০. কিনবেন নাকি ভাড়া নেবেন?"
                    AppLanguage.HINDI -> "10. खरीदें या किराए पर लें?"
                    AppLanguage.ENGLISH -> "10. Buying vs Renting advice"
                },
                guide.buyingVsRentingAdvice
            )
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MachineryCardWhite),
        border = BorderStroke(1.dp, MachineryCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth().testTag("machinery_beginner_guide_card")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = MachineryCardBadgeBg, shape = RoundedCornerShape(8.dp)) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MachineryCardBadgeText,
                        modifier = Modifier.padding(6.dp).size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(heading, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MachineryCardTitleInk)
                    Text(
                        text = "${product.getDisplayName(lang)} • ${product.subcategory}",
                        fontSize = 11.sp,
                        color = MachineryCardBodyInk
                    )
                }
            }
            HorizontalDivider(color = MachineryCardBorder)
            sections.forEach { (title, body) ->
                Surface(
                    color = MachineryCardSoftMint,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MachineryCardBadgeText)
                        Text(body, fontSize = 11.sp, lineHeight = 16.sp, color = MachineryCardTitleInk)
                    }
                }
            }
        }
    }
}

/**
 * Official SMAM & West Bengal FAMS/WBFMS subsidy information card with zero approval promises
 * and anti-fraud OTP/Aadhaar safety guidance.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MachinerySubsidyInfoCard(
    subsidyCodes: List<String>,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val codesToShow = remember(subsidyCodes) {
        if (subsidyCodes.isEmpty()) listOf("SMAM", "WBFMS") else subsidyCodes
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MachineryCardWhite),
        border = BorderStroke(1.dp, MachineryCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth().testTag("machinery_subsidy_card")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MachineryCardBadgeText, modifier = Modifier.size(20.dp))
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> "সরকারি কৃষি যন্ত্র ভর্তুকি পোর্টাল (শুধু তথ্য)"
                        AppLanguage.HINDI -> "सरकारी कृषि यंत्र सब्सिडी पोर्टल (केवल जानकारी)"
                        AppLanguage.ENGLISH -> "Government Machinery Subsidy Portals (Info Only)"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MachineryCardTitleInk
                )
            }

            codesToShow.forEach { code ->
                val isSmam = code.equals("SMAM", ignoreCase = true)
                val url = if (isSmam) "https://agrimachinery.nic.in/" else "https://wbfms.wb.gov.in/"
                val label = if (isSmam) {
                    "SMAM — Sub-Mission on Agricultural Mechanization (Govt. of India)"
                } else {
                    "WBFMS — West Bengal Farm Mechanisation Portal"
                }
                Surface(
                    color = MachineryCardSoftMint,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MachineryCardBorder)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MachineryCardTitleInk)
                        Text(url, fontSize = 11.sp, color = MachineryCardBadgeText)
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }.onFailure {
                                    Toast.makeText(context, "Unable to open official portal link", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("open_subsidy_${code.lowercase()}")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp), tint = MachineryCardBadgeText)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open $code Portal", fontSize = 12.sp, color = MachineryCardBadgeText)
                        }
                    }
                }
            }

            Surface(
                color = MachineryCardWarnBg,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MachineryCardWarnText.copy(alpha = 0.35f))
            ) {
                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MachineryCardWarnText, modifier = Modifier.size(16.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> "সতর্কতা: এখানে কোনো ভর্তুকি অনুমোদনের গ্যারান্টি দেওয়া হয় না। ব্লক কৃষি আধিকারিক (ADA/BAO) থেকে যোগ্যতা যাচাই করুন এবং অজানা কাউকে আধার, OTP বা ব্যাংক তথ্য দেবেন না।"
                            AppLanguage.HINDI -> "सावधानी: सब्सिडी स्वीकृति की कोई गारंटी नहीं है। अपने ब्लॉक कृषि अधिकारी से पात्रता जांचें और अनजान व्यक्ति को आधार, OTP या बैंक विवरण न दें।"
                            AppLanguage.ENGLISH -> "Disclaimer: Links provide official portal access only with NO subsidy approval guarantee. Verify eligibility with your Block Agriculture Office and never share Aadhaar, OTP, or bank details with unknown callers."
                        },
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MachineryCardWarnText
                    )
                }
            }
        }
    }
}

/**
 * Compares Sale Price / Price-on-Request vs Verified Rental Options & Dealer Offers transparently.
 */
@Composable
fun MachineryCostComparisonCard(
    product: MachineryProduct,
    offers: List<MachineryDealerOffer>,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val saleText = product.salePriceInr?.let { "₹${it.toInt()}" } ?: when (lang) {
        AppLanguage.BENGALI -> "দাম যাচাই বাকি (ডিলারের সাথে নিশ্চিত করুন)"
        AppLanguage.HINDI -> "मूल्य अनुरोध पर (डीलर से पुष्टि करें)"
        AppLanguage.ENGLISH -> "Price on request — confirm directly with verified seller"
    }
    val rentalRates = listOfNotNull(
        product.rentalRateHourlyInr?.let { "₹${it.toInt()}/hr" },
        product.rentalRateDailyInr?.let { "₹${it.toInt()}/day" },
        product.rentalRateWeeklyInr?.let { "₹${it.toInt()}/wk" },
        product.rentalRatePerAcreInr?.let { "₹${it.toInt()}/acre" }
    ).joinToString(" • ").ifBlank {
        if (product.forRent) "Custom rate confirmed directly with rental provider" else "Not listed for rental"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MachineryCardWhite),
        border = BorderStroke(1.dp, MachineryCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = when (lang) {
                    AppLanguage.BENGALI -> "ক্রয় বনাম ভাড়া ও ডিলার মূল্য তুলনা"
                    AppLanguage.HINDI -> "खरीद बनाम किराया व डीलर मूल्य तुलना"
                    AppLanguage.ENGLISH -> "Cost & Ownership Comparison"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MachineryCardTitleInk
            )
            Surface(color = MachineryCardSoftMint, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Purchase Estimate: $saleText", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MachineryCardTitleInk)
                    Text("Rental Estimate: $rentalRates", fontSize = 11.sp, color = MachineryCardBodyInk)
                    Text("Matched Local Offers: ${offers.size} (${offers.count { !it.isOutdatedPrice }} active)", fontSize = 11.sp, color = MachineryCardBadgeText)
                }
            }
            Text(
                text = "Transparency Rule: KRISHAK SATHI never fabricates dealer prices or discounts. Written dealer quotations remain the single source of truth.",
                fontSize = 10.sp,
                color = MachineryCardBodyInk
            )
        }
    }
}

/**
 * Full-screen pinch-to-zoom, pan & multi-photo gallery preview modal for equipment inspection.
 */
@Composable
fun MachineryFullScreenImageViewer(
    photoTokens: List<String>,
    title: String,
    subtitle: String = "",
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    val cleanTokens = remember(photoTokens) { photoTokens.filter { it.isNotBlank() } }
    if (cleanTokens.isEmpty()) {
        onDismiss()
        return
    }
    val safeStart = initialIndex.coerceIn(0, cleanTokens.lastIndex)
    val pagerState = rememberPagerState(initialPage = safeStart, pageCount = { cleanTokens.size })
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF5031710))
                .padding(12.dp)
                .testTag("machinery_fullscreen_viewer")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = RoyalTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            color = RoyalSoftGold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .background(RoyalGreenCardBg, CircleShape)
                        .testTag("machinery_fullscreen_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close full-screen preview",
                        tint = RoyalTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = null,
                        tint = RoyalGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Pinch or double-tap to zoom • Swipe if multiple photos",
                        color = RoyalTextSecondary,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "${pagerState.currentPage + 1}/${cleanTokens.size}",
                    color = RoyalGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                var scale by remember(page) { mutableFloatStateOf(1f) }
                var offset by remember(page) { mutableStateOf(Offset.Zero) }

                LaunchedEffect(pagerState.currentPage) {
                    if (pagerState.currentPage != page) {
                        scale = 1f
                        offset = Offset.Zero
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MachineryCardWhite)
                        .pointerInput(page) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1.2f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.2f
                                    }
                                }
                            )
                        }
                        .pointerInput(page) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val nextScale = (scale * zoom).coerceIn(1f, 4f)
                                scale = nextScale
                                offset = if (nextScale <= 1.02f) {
                                    Offset.Zero
                                } else {
                                    Offset(
                                        x = (offset.x + pan.x).coerceIn(-600f, 600f),
                                        y = (offset.y + pan.y).coerceIn(-600f, 600f)
                                    )
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    SingleEquipmentPhotoFrame(
                        token = cleanTokens[page],
                        fallbackEmoji = "🚜",
                        contentDescription = "$title photo ${page + 1}",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                    )
                }
            }

            if (cleanTokens.size > 1) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    itemsIndexed(cleanTokens) { idx, token ->
                        val selected = idx == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) RoyalGold else RoyalBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    scope.launch { pagerState.animateScrollToPage(idx) }
                                }
                        ) {
                            SingleEquipmentPhotoFrame(
                                token = token,
                                fallbackEmoji = "🚜",
                                contentDescription = "Thumbnail ${idx + 1}",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
