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
    primary = EmeraldSprout,
    onPrimary = Color.Black,
    primaryContainer = ForestGreenPrimary,
    onPrimaryContainer = MintLight,
    secondary = AmberSun,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF6D4C00),
    onSecondaryContainer = AmberLight,
    tertiary = CompostSoil,
    onTertiary = Color.White,
    tertiaryContainer = EarthBrown,
    onTertiaryContainer = EarthSurface,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = TextSecondaryDark,
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = MintLight,
    onPrimaryContainer = ForestGreenDark,
    secondary = HarvestGold,
    onSecondary = Color.White,
    secondaryContainer = AmberLight,
    onSecondaryContainer = Color(0xFF5D4000),
    tertiary = CompostSoil,
    onTertiary = Color.White,
    tertiaryContainer = EarthSurface,
    onTertiaryContainer = EarthBrown,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
)

@Composable
fun AgriShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our rich custom agri branding
    content: @Composable () -> Unit,
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
