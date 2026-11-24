package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Default Royal Green Premium Scheme (#062D20, #0B3B2B, #104A35, #087A50, #32A66A, #D6B45C)
val RoyalGreenColorScheme = darkColorScheme(
    primary = RoyalGreenHighlight,
    onPrimary = Color(0xFF032116),
    primaryContainer = AgriGreenPrimaryContainer,
    onPrimaryContainer = RoyalTextPrimary,
    secondary = RoyalGreenEmerald,
    onSecondary = RoyalTextPrimary,
    secondaryContainer = RoyalInputBg,
    onSecondaryContainer = RoyalTextSecondary,
    tertiary = RoyalGold,
    onTertiary = Color(0xFF1F1600),
    tertiaryContainer = RoyalWarningBg,
    onTertiaryContainer = RoyalSoftGold,
    background = RoyalGreenPrimaryBg,
    onBackground = RoyalTextPrimary,
    surface = RoyalGreenCardBg,
    onSurface = RoyalTextPrimary,
    surfaceVariant = RoyalGreenSecondaryBg,
    onSurfaceVariant = RoyalTextSecondary,
    surfaceContainerLowest = RoyalMidnightBg,
    surfaceContainerLow = RoyalGreenSecondaryBg,
    surfaceContainer = RoyalGreenCardBg,
    surfaceContainerHigh = RoyalInputBg,
    surfaceContainerHighest = Color(0xFF165740),
    outline = RoyalBorder,
    outlineVariant = RoyalBorder,
    error = Color(0xFFF87171),
    onError = Color(0xFF370B0B),
    errorContainer = RoyalAlertBg,
    onErrorContainer = RoyalAlertText
)

// Optional Midnight Emerald Contrast Scheme (when darkTheme toggle is enabled)
val RoyalMidnightColorScheme = darkColorScheme(
    primary = RoyalGreenHighlight,
    onPrimary = Color(0xFF021A11),
    primaryContainer = Color(0xFF0A4730),
    onPrimaryContainer = RoyalTextPrimary,
    secondary = RoyalGreenEmerald,
    onSecondary = RoyalTextPrimary,
    secondaryContainer = Color(0xFF0E3325),
    onSecondaryContainer = RoyalTextSecondary,
    tertiary = RoyalGold,
    onTertiary = Color(0xFF1F1600),
    tertiaryContainer = RoyalWarningBg,
    onTertiaryContainer = RoyalSoftGold,
    background = RoyalMidnightBg,
    onBackground = RoyalTextPrimary,
    surface = RoyalMidnightCard,
    onSurface = RoyalTextPrimary,
    surfaceVariant = RoyalMidnightSecondary,
    onSurfaceVariant = RoyalTextSecondary,
    surfaceContainerLowest = Color(0xFF02120C),
    surfaceContainerLow = RoyalMidnightSecondary,
    surfaceContainer = RoyalMidnightCard,
    surfaceContainerHigh = RoyalInputBg,
    surfaceContainerHighest = RoyalGreenCardBg,
    outline = RoyalBorder,
    outlineVariant = RoyalBorder,
    error = Color(0xFFF87171),
    onError = Color(0xFF370B0B),
    errorContainer = RoyalAlertBg,
    onErrorContainer = RoyalAlertText
)

@Composable
fun royalOutlinedTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = RoyalInputBg,
    unfocusedContainerColor = RoyalInputBg,
    disabledContainerColor = RoyalGreenSecondaryBg,
    focusedBorderColor = RoyalGold,
    unfocusedBorderColor = RoyalBorder,
    focusedTextColor = RoyalTextPrimary,
    unfocusedTextColor = RoyalTextPrimary,
    focusedLabelColor = RoyalSoftGold,
    unfocusedLabelColor = RoyalTextSecondary,
    focusedPlaceholderColor = RoyalTextSecondary,
    unfocusedPlaceholderColor = RoyalTextSecondary,
    cursorColor = RoyalGold
)

@Composable
fun KrishakSathiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RoyalMidnightColorScheme else RoyalGreenColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
