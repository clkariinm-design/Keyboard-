package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = DeepMidnight,
    primaryContainer = EmeraldMuted,
    onPrimaryContainer = EmeraldLight,
    secondary = VividViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2E1065),
    onSecondaryContainer = VioletLight,
    tertiary = ElectricCyan,
    onTertiary = DeepMidnight,
    tertiaryContainer = Color(0xFF083344),
    onTertiaryContainer = CyanLight,
    background = DeepMidnight,
    onBackground = SlateText,
    surface = SurfaceDark,
    onSurface = SlateText,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = SlateMuted,
    outline = BorderDark,
    outlineVariant = SurfaceDarkCard,
    error = SunsetCoral,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF059669),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF4C1D95),
    tertiary = Color(0xFF0891B2),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFFAFE),
    onTertiaryContainer = Color(0xFF155E75),
    background = SurfaceLight,
    onBackground = TextDark,
    surface = SurfaceLightCard,
    onSurface = TextDark,
    surfaceVariant = SurfaceLightElevated,
    onSurfaceVariant = TextMutedLight,
    outline = BorderLight,
    outlineVariant = Color(0xFFF1F5F9),
    error = Color(0xFFE11D48),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
