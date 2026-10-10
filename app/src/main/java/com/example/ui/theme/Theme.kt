package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.UiThemeMode

// Option A: Clean iOS Bento Palette
private val BentoLightScheme = lightColorScheme(
    primary = PolyGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = PolyGreenContainer,
    onPrimaryContainer = PolyGreenOnContainer,
    secondary = PolyGreenMedium,
    onSecondary = Color.White,
    tertiary = PolyAmber,
    onTertiary = Color.White,
    tertiaryContainer = PolyAmberContainer,
    onTertiaryContainer = PolyAmberDark,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight
)

// Option B: Dark Luxury Glassmorphism Palette
private val DarkGlassScheme = darkColorScheme(
    primary = PolyGreenLight,
    onPrimary = Color.Black,
    primaryContainer = PolyGreenOnContainer,
    onPrimaryContainer = PolyGreenContainer,
    secondary = PolyGreenMedium,
    onSecondary = Color.Black,
    tertiary = PolyAmber,
    onTertiary = Color.Black,
    tertiaryContainer = PolyAmberDark,
    onTertiaryContainer = PolyAmberContainer,
    background = Color(0xFF0A0F0D),
    onBackground = Color(0xFFF9FAFB),
    surface = Color(0xFF141D18),
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF1E2A23),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF2E3D34)
)

// Option C: Minimalist Editorial Monochromatic Palette
private val EditorialLightScheme = lightColorScheme(
    primary = Color(0xFF0F172A), // Slate 900
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFF334155),
    onSecondary = Color.White,
    tertiary = PolyGreenPrimary,
    onTertiary = Color.White,
    tertiaryContainer = PolyGreenContainer,
    onTertiaryContainer = PolyGreenOnContainer,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF020617),
    surface = Color.White,
    onSurface = Color(0xFF020617),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    themeMode: UiThemeMode = UiThemeMode.IOS_BENTO,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeMode) {
        UiThemeMode.IOS_BENTO -> if (darkTheme) DarkGlassScheme else BentoLightScheme
        UiThemeMode.DARK_GLASS -> DarkGlassScheme
        UiThemeMode.MINIMAL_EDITORIAL -> EditorialLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
