package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class TwoMindsExtendedColors(
    val isDark: Boolean,
    val background: Color,
    val card: Color,
    val cardElevated: Color,
    val subtleSurface: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val gold: Color,
    val goldBg: Color,
    val emerald: Color,
    val emeraldBg: Color,
    val coral: Color,
    val infoBlue: Color,
    val sidebarBg: Color,
    val chatBubbleMine: Color,
    val chatBubblePartner: Color
)

private val DarkExtendedColors = TwoMindsExtendedColors(
    isDark = true,
    background = DarkBackground,
    card = DarkSurface,
    cardElevated = DarkSurfaceElevated,
    subtleSurface = DarkSurfaceSubtle,
    border = DarkBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    gold = GoldAccent,
    goldBg = GoldAccentMuted,
    emerald = EmeraldGrowth,
    emeraldBg = EmeraldSoftBgDark,
    coral = CoralAlert,
    infoBlue = InfoBlue,
    sidebarBg = SidebarDarkBg,
    chatBubbleMine = Color(0xFF282116),
    chatBubblePartner = DarkSurfaceElevated
)

private val LightExtendedColors = TwoMindsExtendedColors(
    isDark = false,
    background = LightBackground,
    card = LightSurface,
    cardElevated = LightSurfaceElevated,
    subtleSurface = LightSurfaceSubtle,
    border = LightBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    gold = Color(0xFFC8872B),
    goldBg = GoldAccentLightBg,
    emerald = Color(0xFF1D9A5C),
    emeraldBg = EmeraldSoftBgLight,
    coral = Color(0xFFD93F3F),
    infoBlue = Color(0xFF2E6CE6),
    sidebarBg = SidebarDarkBg,
    chatBubbleMine = Color(0xFF151922),
    chatBubblePartner = Color(0xFFFFFFFF)
)

val LocalTwoMindsColors = staticCompositionLocalOf { DarkExtendedColors }

private val DarkMaterialColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = Color(0xFF1A140B),
    primaryContainer = GoldAccentMuted,
    onPrimaryContainer = GoldAccent,
    secondary = EmeraldGrowth,
    onSecondary = Color(0xFF071D11),
    secondaryContainer = EmeraldSoftBgDark,
    onSecondaryContainer = EmeraldGrowth,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = CoralAlert,
    onError = Color.White
)

private val LightMaterialColorScheme = lightColorScheme(
    primary = LightTextPrimary,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceSubtle,
    onPrimaryContainer = LightTextPrimary,
    secondary = Color(0xFF1D9A5C),
    onSecondary = Color.White,
    secondaryContainer = EmeraldSoftBgLight,
    onSecondaryContainer = Color(0xFF11633A),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceSubtle,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = Color(0xFFD93F3F),
    onError = Color.White
)

private val TwoMindsShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TwoMindsTheme(
    themeMode: String = "dark",
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val extended = if (useDark) DarkExtendedColors else LightExtendedColors
    val colorScheme = if (useDark) DarkMaterialColorScheme else LightMaterialColorScheme

    CompositionLocalProvider(LocalTwoMindsColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TwoMindsTypography,
            shapes = TwoMindsShapes,
            content = content
        )
    }
}

object TwoMindsThemeTokens {
    val colors: TwoMindsExtendedColors
        @Composable get() = LocalTwoMindsColors.current
}
