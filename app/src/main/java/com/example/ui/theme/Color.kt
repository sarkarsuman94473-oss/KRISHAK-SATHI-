package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// =============================================================================
// ROYAL GREEN PREMIUM COLOR SYSTEM — KRISHAK SATHI
// =============================================================================
val RoyalGreenPrimaryBg = Color(0xFF062D20)    // Primary Background
val RoyalGreenSecondaryBg = Color(0xFF0B3B2B)  // Secondary Background / Header / Nav
val RoyalGreenCardBg = Color(0xFF104A35)       // Card Background
val RoyalGreenEmerald = Color(0xFF087A50)      // Emerald Green (Primary Actions / Badges)
val RoyalGreenHighlight = Color(0xFF32A66A)    // Highlight Green (Icons / Active Accents)
val RoyalGold = Color(0xFFD6B45C)              // Royal Gold (Selected / Premium Accents)
val RoyalSoftGold = Color(0xFFF0D98B)          // Soft Gold (Readable Gold Labels / Icons)
val RoyalTextPrimary = Color(0xFFFFFFFF)       // Primary Text (#FFFFFF)
val RoyalTextSecondary = Color(0xFFC4D5CA)     // Secondary Text (#C4D5CA)
val RoyalBorder = Color(0xFF286348)            // Borders & Dividers (#286348)
val RoyalInputBg = Color(0xFF123F30)           // Input Field Background (#123F30)

// Royal Green Luxury Extended Tokens
val RoyalDeepGreen = Color(0xFF064E3B)         // Deep Royal Emerald Header / Banner
val RoyalEmeraldLuxury = Color(0xFF15803D)     // Fresh Leaf Emerald Accent
val RoyalGoldLuxury = Color(0xFFD4AF37)        // Metallic Gold Highlight
val RoyalNaturalWhite = Color(0xFFFAFAF5)      // Warm Ivory Surface
val RoyalLightGreenTint = Color(0xFFE9F5E5)    // Soft Leaf Tint Surface

// High-Contrast Midnight Emerald Variant (Optional Theme Toggle)
val RoyalMidnightBg = Color(0xFF031B13)
val RoyalMidnightSecondary = Color(0xFF06291D)
val RoyalMidnightCard = Color(0xFF0B3A29)

// Semantic Status Surfaces & Text (Optimized for Dark Royal Green Readability)
val RoyalSuccessBg = Color(0xFF0E4D36)
val RoyalSuccessText = Color(0xFFA7F3D0)
val RoyalWarningBg = Color(0xFF3A2C10)
val RoyalWarningText = RoyalSoftGold
val RoyalAlertBg = Color(0xFF471A1D)
val RoyalAlertText = Color(0xFFFCA5A5)
val RoyalInfoBg = Color(0xFF0D3D47)
val RoyalInfoText = Color(0xFF99F6E4)

// Crisp White / Soft-Mint Product Card Tokens (for Equipment Photos & Clean Marketplace Readability)
val MachineryCardWhite = Color(0xFFFFFFFF)
val MachineryCardSoftMint = Color(0xFFF4F9F6)
val MachineryCardBorder = Color(0xFFC5DDD0)
val MachineryCardTitleInk = Color(0xFF0B2E21)
val MachineryCardBodyInk = Color(0xFF395648)
val MachineryCardBadgeBg = Color(0xFFE5F4EC)
val MachineryCardBadgeText = Color(0xFF095D3C)
val MachineryCardWarnBg = Color(0xFFFFF6DC)
val MachineryCardWarnText = Color(0xFF8C5300)

// Crisp White & Soft Earthy Surface Tokens (Original Farmer-Friendly Dashboard Cards)
val EarthSurfaceWhite = Color(0xFFFFFFFF)
val EarthWarmCream = Color(0xFFFAF7F0)
val EarthSandTint = Color(0xFFF3EFE4)
val EarthSageWash = Color(0xFFEAF4EE)
val EarthCardBorder = Color(0xFFD5E2D9)
val EarthWarmBorder = Color(0xFFE2D9C5)
val EarthInkPrimary = Color(0xFF0A2E20)
val EarthInkSecondary = Color(0xFF425B4E)
val EarthTrustBadgeBg = Color(0xFFE3F5EC)
val EarthTrustBadgeText = Color(0xFF0A5F3E)
val EarthWarnSurface = Color(0xFFFFF8E6)
val EarthWarnInk = Color(0xFF7A4B00)

// Compatibility & Semantic Brand Aliases
val AgriForestGreen = RoyalGreenSecondaryBg
val AgriNaturalGreen = RoyalGreenEmerald
val AgriLeafAccent = RoyalGreenHighlight
val AgriFreshGreen = RoyalGreenHighlight
val AgriSoftOffWhite = RoyalGreenPrimaryBg
val AgriPureWhite = RoyalGreenCardBg
val AgriSubtleGold = RoyalGold
val AgriDarkCharcoal = RoyalTextPrimary
val AgriMutedGrey = RoyalTextSecondary
val AgriSoftGrey = RoyalTextSecondary
val AgriLightBorder = RoyalBorder

val AgriLightBackground = RoyalGreenPrimaryBg
val AgriLightSurface = RoyalGreenCardBg
val AgriLightSurfaceVariant = RoyalGreenSecondaryBg
val AgriLightTextPrimary = RoyalTextPrimary
val AgriLightTextSecondary = RoyalTextSecondary
val AgriLightTextTertiary = RoyalTextSecondary

val AgriDarkBackground = RoyalMidnightBg
val AgriDarkSurface = RoyalMidnightCard
val AgriDarkSurfaceVariant = RoyalMidnightSecondary
val AgriDarkBorder = RoyalBorder
val AgriDarkTextPrimary = RoyalTextPrimary
val AgriDarkTextSecondary = RoyalTextSecondary
val AgriDarkTextTertiary = Color(0xFF9AB5A5)

val AgriGreenPrimary = RoyalGreenEmerald
val AgriGreenOnPrimary = RoyalTextPrimary
val AgriGreenPrimaryContainer = Color(0xFF0E5A3E)
val AgriGreenOnPrimaryContainer = RoyalTextPrimary

val AgriEarthSecondary = RoyalGreenHighlight
val AgriEarthOnSecondary = Color(0xFF032116)
val AgriEarthSecondaryContainer = RoyalInputBg

val AgriGoldTertiary = RoyalGold
val AgriPriceGold = RoyalSoftGold
val AgriAlertRed = Color(0xFFDC2626)
val AgriWarningAmber = RoyalGold
val AgriSuccessGreen = RoyalGreenHighlight

// Theme-aware dynamic accessors
val AgriTextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface

val AgriTextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val AgriBorderColor: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outlineVariant
