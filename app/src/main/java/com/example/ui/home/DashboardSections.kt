package com.example.ui.home

import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.ExpertCategories
import com.example.data.local.entity.ExpertEntity
import com.example.data.datasource.ExpertTranslations
import com.example.data.model.AppLanguage
import com.example.ui.theme.EarthCardBorder
import com.example.ui.theme.EarthInkPrimary
import com.example.ui.theme.EarthInkSecondary
import com.example.ui.theme.EarthSageWash
import com.example.ui.theme.EarthSurfaceWhite
import com.example.ui.theme.EarthTrustBadgeBg
import com.example.ui.theme.EarthTrustBadgeText
import com.example.ui.theme.RoyalBorder
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGreenCardBg
import com.example.ui.theme.RoyalGreenEmerald
import com.example.ui.theme.RoyalGreenSecondaryBg
import com.example.ui.theme.RoyalSoftGold
import com.example.ui.theme.RoyalTextPrimary
import com.example.ui.theme.RoyalTextSecondary
import com.example.util.L10n

data class HomeActionCard(
    val id: String,
    val titleKey: String,
    val subKey: String,
    val iconEmoji: String,
    val icon: ImageVector? = null,
    val isGoldAccent: Boolean = false,
    val accentColor: Color = RoyalGreenEmerald,
    @DrawableRes val drawableResId: Int? = null
)

data class DashboardSectionConfig(
    val sectionId: String,
    val titleKey: String,
    val subKey: String,
    val isWarmSurface: Boolean,
    val tiles: List<HomeActionCard>
)

object DashboardCatalog {
    val coreSections: List<DashboardSectionConfig> = listOf(
        DashboardSectionConfig(
            sectionId = "crop_help",
            titleKey = "dash_sec_crop_title",
            subKey = "dash_sec_crop_sub",
            isWarmSurface = false,
            tiles = listOf(
                HomeActionCard("crops", "dash_tile_crop_advice", "dash_tile_crop_advice_sub", "🌱", Icons.Default.Agriculture, true, Color(0xFF0E7A4E), R.drawable.dash_ico_crops),
                HomeActionCard("nursery", "dash_tile_seeds_sowing", "dash_tile_seeds_sowing_sub", "🌾", Icons.Default.LocalFlorist, false, Color(0xFF1F7A3E), R.drawable.dash_ico_nursery),
                HomeActionCard("medicine", "dash_tile_fertilizer", "dash_tile_fertilizer_sub", "🧪", Icons.Default.Science, false, Color(0xFF0D6E6E), R.drawable.dash_ico_medicine),
                HomeActionCard("weed_control", "dash_tile_weed_control", "dash_tile_weed_control_sub", "🌿", Icons.Default.Grass, false, Color(0xFF3F6E23), R.drawable.dash_ico_weed_control),
                HomeActionCard("problem", "dash_tile_pests_disease", "dash_tile_pests_disease_sub", "🐛", Icons.Default.HealthAndSafety, true, Color(0xFFB45309), R.drawable.dash_ico_problem),
                HomeActionCard("irrigation_advice", "dash_tile_irrigation", "dash_tile_irrigation_sub", "💧", Icons.Default.WaterDrop, false, Color(0xFF0369A1), R.drawable.dash_ico_irrigation_advice),
                HomeActionCard("spray_dose", "dash_tile_spray_dose", "dash_tile_spray_dose_sub", "⚖️", Icons.Default.Calculate, false, Color(0xFF5B4B8A), R.drawable.dash_ico_spray_dose),
                HomeActionCard("market", "dash_tile_market_sell", "dash_tile_market_sell_sub", "💰", Icons.Default.Storefront, true, Color(0xFF087A50), R.drawable.dash_ico_market)
            )
        ),
        DashboardSectionConfig(
            sectionId = "land_legal",
            titleKey = "dash_sec_land_title",
            subKey = "dash_sec_land_sub",
            isWarmSurface = true,
            tiles = listOf(
                HomeActionCard("land_documents", "dash_tile_land_docs", "dash_tile_land_docs_sub", "📄", Icons.Default.Description, false, Color(0xFF7C4D12), R.drawable.dash_ico_land_documents),
                HomeActionCard("land_records", "dash_tile_land_records", "dash_tile_land_records_sub", "🗂", Icons.Default.AccountBalance, true, Color(0xFF087A50), R.drawable.dash_ico_land_records),
                HomeActionCard("land_disputes", "dash_tile_land_disputes", "dash_tile_land_disputes_sub", "⚖️", Icons.Default.Gavel, false, Color(0xFF9A3412), R.drawable.dash_ico_land_disputes),
                HomeActionCard("land_buysell", "dash_tile_land_buysell", "dash_tile_land_buysell_sub", "🤝", Icons.Default.Security, false, Color(0xFF0E7490), R.drawable.dash_ico_land_buysell),
                HomeActionCard("land_possession", "dash_tile_land_possession", "dash_tile_land_possession_sub", "📌", Icons.Default.CheckCircle, false, Color(0xFF15803D), R.drawable.dash_ico_land_possession),
                HomeActionCard("land_inheritance", "dash_tile_land_inheritance", "dash_tile_land_inheritance_sub", "👪", Icons.Default.Handshake, false, Color(0xFF6D4C41), R.drawable.dash_ico_land_inheritance)
            )
        ),
        DashboardSectionConfig(
            sectionId = "schemes_subsidy",
            titleKey = "dash_sec_scheme_title",
            subKey = "dash_sec_scheme_sub",
            isWarmSurface = false,
            tiles = listOf(
                HomeActionCard("govt_schemes", "dash_tile_scheme_all", "dash_tile_scheme_all_sub", "🏛", Icons.Default.AccountBalance, true, Color(0xFF087A50), R.drawable.dash_ico_govt_schemes),
                HomeActionCard("scheme_subsidy", "dash_tile_scheme_subsidy", "dash_tile_scheme_subsidy_sub", "🚜", Icons.Default.Build, false, Color(0xFF0F766E), R.drawable.dash_ico_scheme_subsidy),
                HomeActionCard("scheme_kisan_id", "dash_tile_scheme_kisan_id", "dash_tile_scheme_kisan_id_sub", "💳", Icons.Default.MonetizationOn, false, Color(0xFF1D4ED8), R.drawable.dash_ico_scheme_kisan_id),
                HomeActionCard("scheme_insurance", "dash_tile_scheme_insurance", "dash_tile_scheme_insurance_sub", "🛡", Icons.Default.Security, false, Color(0xFF047857), R.drawable.dash_ico_scheme_insurance),
                HomeActionCard("scheme_apply", "dash_tile_scheme_apply", "dash_tile_scheme_apply_sub", "📝", Icons.Default.Description, false, Color(0xFFB45309), R.drawable.dash_ico_scheme_apply),
                HomeActionCard("scheme_loan", "dash_tile_scheme_loan", "dash_tile_scheme_loan_sub", "🏦", Icons.Default.MonetizationOn, false, Color(0xFF4338CA), R.drawable.dash_ico_scheme_loan)
            )
        ),
        DashboardSectionConfig(
            sectionId = "machinery_market",
            titleKey = "dash_sec_machinery_title",
            subKey = "dash_sec_machinery_sub",
            isWarmSurface = true,
            tiles = listOf(
                HomeActionCard("machinery", "dash_tile_mach_all", "dash_tile_mach_all_sub", "🚜", Icons.Default.Build, true, Color(0xFF087A50), R.drawable.dash_ico_machinery),
                HomeActionCard("machinery_tractor", "dash_tile_mach_tractor", "dash_tile_mach_tractor_sub", "⚙️", Icons.Default.Agriculture, false, Color(0xFF166534), R.drawable.dash_ico_machinery_tractor),
                HomeActionCard("machinery_sprayer", "dash_tile_mach_sprayer", "dash_tile_mach_sprayer_sub", "🚁", Icons.Default.Eco, false, Color(0xFF0369A1), R.drawable.dash_ico_machinery_sprayer),
                HomeActionCard("machinery_irrigation", "dash_tile_mach_irrigation", "dash_tile_mach_irrigation_sub", "💧", Icons.Default.WaterDrop, false, Color(0xFF0E7490), R.drawable.dash_ico_machinery_irrigation),
                HomeActionCard("machinery_rent", "dash_tile_mach_rent", "dash_tile_mach_rent_sub", "📅", Icons.Default.Handshake, false, Color(0xFF7C4D12), R.drawable.dash_ico_machinery_rent),
                HomeActionCard("machinery_dealers", "dash_tile_mach_dealers", "dash_tile_mach_dealers_sub", "📍", Icons.Default.LocationOn, true, Color(0xFFB45309), R.drawable.dash_ico_machinery_dealers)
            )
        )
    )

    val additionalToolCards: List<HomeActionCard> = listOf(
        HomeActionCard("general_ai", "card_general_ai_title", "card_general_ai_sub", "✨", Icons.Default.AutoAwesome, true, RoyalGreenEmerald, R.drawable.dash_ico_general_ai),
        HomeActionCard("resource_library", "card_resource_library_title", "card_resource_library_sub", "📚", Icons.Default.MenuBook, true, RoyalGreenEmerald, R.drawable.dash_ico_resource_library),
        HomeActionCard("community_forum", "card_forum_title", "card_forum_sub", "💬", Icons.Default.Forum, true, RoyalGreenEmerald, R.drawable.dash_ico_community_forum),
        HomeActionCard("expert", "card_expert_title", "card_expert_sub", "☎", Icons.Default.SupportAgent, true, RoyalGreenEmerald, R.drawable.dash_ico_expert),
        HomeActionCard("sell", "card_sell_title", "card_sell_sub", "🧺", Icons.Default.Storefront, false, RoyalGreenEmerald, R.drawable.dash_ico_sell),
        HomeActionCard("govt_info", "card_govt_title", "card_govt_sub", "🏛", Icons.Default.AccountBalance, false, RoyalGreenEmerald, R.drawable.dash_ico_govt_info),
        HomeActionCard("ai", "card_ai_title", "card_ai_sub", "🤖", Icons.Default.SmartToy, true, Color(0xFF0E7A4E), R.drawable.dash_ico_ai),
        HomeActionCard("weather", "card_weather_title", "card_weather_sub", "🌦", Icons.Default.WbSunny, false, Color(0xFF0369A1), R.drawable.dash_ico_weather),
        HomeActionCard("tea", "card_tea_title", "card_tea_sub", "🍵", Icons.Default.Spa, false, Color(0xFF15803D), R.drawable.dash_ico_tea),
        HomeActionCard("orders", "card_orders_title", "card_orders_sub", "📦", Icons.Default.ShoppingBag, false, Color(0xFF7C4D12), R.drawable.dash_ico_orders),
        HomeActionCard("calendar", "card_calendar_title", "card_calendar_sub", "📅", Icons.Default.EventNote, false, Color(0xFF0D6E6E), R.drawable.dash_ico_calendar),
        HomeActionCard("expense", "card_expense_title", "card_expense_sub", "💵", Icons.Default.AccountBalanceWallet, false, Color(0xFFB45309), R.drawable.dash_ico_expense),
        HomeActionCard("leading_farmers", "card_leading_farmers_title", "card_leading_farmers_sub", "🏆", Icons.Default.EmojiEvents, true, Color(0xFFD4AF37), R.drawable.dash_ico_leading_farmers),
        HomeActionCard("video_learning", "card_video_learning_title", "card_video_learning_sub", "🎬", Icons.Default.PlayCircle, true, Color(0xFF15803D), R.drawable.dash_ico_krishi_shiksha)
    )
}

@Composable
fun EarthyDashboardTileRow(
    rowCards: List<HomeActionCard>,
    columns: Int,
    language: AppLanguage,
    horizontalPad: Dp,
    @Suppress("UNUSED_PARAMETER") isWarmSurface: Boolean,
    @Suppress("UNUSED_PARAMETER") isDarkTheme: Boolean,
    onCardClick: (String) -> Unit,
    iconSize: Dp = 74.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPad),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        rowCards.forEach { card ->
            BoxFreeDashboardIconTile(
                card = card,
                language = language,
                iconSize = iconSize,
                onClick = { onCardClick(card.id) },
                modifier = Modifier.weight(1f)
            )
        }
        repeat(columns - rowCards.size) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun BoxFreeDashboardIconTile(
    card: HomeActionCard,
    language: AppLanguage,
    iconSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val animationsEnabled = remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) > 0f
        }.getOrDefault(true)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && animationsEnabled) 0.94f else 1f,
        animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0),
        label = "dashboard_icon_press_scale"
    )
    val liftOffset by animateDpAsState(
        targetValue = if (isPressed && animationsEnabled) (-2).dp else 0.dp,
        animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0),
        label = "dashboard_icon_press_lift"
    )

    val titleLabel = L10n.t(card.titleKey, language)

    Column(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = false,
                    radius = 52.dp,
                    color = Color(0xFFD4AF37).copy(alpha = 0.24f)
                ),
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .testTag("home_card_${card.id}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (card.drawableResId != null) {
            Image(
                painter = painterResource(id = card.drawableResId),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(iconSize)
                    .scale(scale)
                    .offset(y = liftOffset)
            )
        } else if (card.icon != null) {
            Icon(
                imageVector = card.icon,
                contentDescription = null,
                tint = if (card.isGoldAccent) Color(0xFFD4AF37) else Color.White,
                modifier = Modifier
                    .size(iconSize * 0.58f)
                    .scale(scale)
                    .offset(y = liftOffset)
            )
        }

        Text(
            text = titleLabel,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ExpertHomeShowcaseSection(
    experts: List<ExpertEntity>,
    language: AppLanguage,
    horizontalPad: Dp,
    isDarkTheme: Boolean,
    onViewAllExperts: () -> Unit,
    onTalkExpert: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .testTag("home_expert_showcase"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPad),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = L10n.t("dash_expert_showcase_title", language),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = RoyalTextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(
                onClick = onViewAllExperts,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("home_view_all_experts_btn")
            ) {
                Text(
                    text = L10n.t("dash_expert_view_all", language),
                    color = RoyalSoftGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = RoyalSoftGold,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        if (experts.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = horizontalPad),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(experts.take(3), key = { it.id }) { expert ->
                    val cardBg = if (isDarkTheme) RoyalGreenCardBg else EarthSurfaceWhite
                    val titleInk = if (isDarkTheme) RoyalTextPrimary else EarthInkPrimary
                    val bodyInk = if (isDarkTheme) RoyalTextSecondary else EarthInkSecondary
                    val catItem = ExpertCategories.ALL_CATEGORIES.firstOrNull { it.key == expert.category }

                    Card(
                        modifier = Modifier
                            .width(268.dp)
                            .clickable { onTalkExpert() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, if (isDarkTheme) RoyalBorder else EarthCardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (isDarkTheme) RoyalGreenSecondaryBg else EarthSageWash,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = expert.photoAvatar, fontSize = 22.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = ExpertTranslations.getDisplayName(expert, language),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = titleInk,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (expert.isVerified) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Verified Expert",
                                                tint = if (isDarkTheme) RoyalSoftGold else EarthTrustBadgeText,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = catItem?.getName(language)
                                            ?: ExpertTranslations.getDisplayDesignation(expert, language),
                                        fontSize = 11.5.sp,
                                        color = bodyInk,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (isDarkTheme) RoyalGreenSecondaryBg else EarthTrustBadgeBg,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "⭐ ${expert.rating} • ${expert.reviewCount} ${L10n.t("dash_consultations_label", language)}",
                                        color = if (isDarkTheme) RoyalSoftGold else EarthTrustBadgeText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Button(
                                    onClick = onTalkExpert,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RoyalGreenEmerald,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = L10n.t("dash_more_info", language),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
