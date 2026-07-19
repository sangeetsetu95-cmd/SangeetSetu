package com.sangeetsetu.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.sangeetsetu.app.AppSettings
import com.sangeetsetu.app.AppTheme
import com.sangeetsetu.app.model.SystemSettings

val LocalSystemSettings = compositionLocalOf { SystemSettings() }

private val PremiumDarkColorScheme = darkColorScheme(
    primary = PremiumGold,
    onPrimary = Color.Black,
    secondary = PremiumGoldDark,
    onSecondary = Color.White,
    tertiary = PremiumGoldLight,
    background = AppBackground,
    onBackground = Color.White,
    surface = CardBackground,
    onSurface = Color.White,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = PremiumGray,
    outline = BorderColor,
)

private val PremiumLightColorScheme = lightColorScheme(
    primary = PremiumGold,
    onPrimary = Color.White,
    secondary = PremiumGoldDark,
    onSecondary = Color.Black,
    tertiary = PremiumGoldLight,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color(0xFFF5F5F5),
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color.DarkGray,
    outline = PremiumGold,
)

private val PremiumRedColorScheme = darkColorScheme(
    primary = Color(0xFFEF4444), // Red
    onPrimary = Color.White,
    secondary = Color(0xFFB91C1C),
    onSecondary = Color.White,
    tertiary = Color(0xFFF87171),
    background = Color(0xFF1A0505), // Dark Red/Black background
    onBackground = Color.White,
    surface = Color(0xFF2D0A0A),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF3D0E0E),
    onSurfaceVariant = Color.LightGray,
    outline = Color(0xFF7F1D1D),
)

@Composable
fun PremiumAdminTheme(
    content: @Composable () -> Unit
) {
    val baseColorScheme = when (AppSettings.theme.value) {
        AppTheme.LIGHT -> PremiumLightColorScheme
        AppTheme.DARK -> PremiumDarkColorScheme
        AppTheme.RED -> PremiumRedColorScheme
        AppTheme.SYSTEM -> if (isSystemInDarkTheme()) PremiumDarkColorScheme else PremiumLightColorScheme
    }

    ProvideStrings {
        MaterialTheme(
            colorScheme = baseColorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = when (AppSettings.theme.value) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.RED -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    },
    systemSettings: SystemSettings = SystemSettings(),
    content: @Composable () -> Unit
) {
    val dynamicPrimary = try { Color(android.graphics.Color.parseColor(systemSettings.theme["primary"])) } catch (e: Exception) { PremiumGold }
    val dynamicBackground = try { Color(android.graphics.Color.parseColor(systemSettings.theme["background"])) } catch (e: Exception) { AppBackground }

    val baseColorScheme = when (AppSettings.theme.value) {
        AppTheme.LIGHT -> PremiumLightColorScheme
        AppTheme.DARK -> PremiumDarkColorScheme
        AppTheme.RED -> PremiumRedColorScheme
        AppTheme.SYSTEM -> if (isSystemInDarkTheme()) PremiumDarkColorScheme else PremiumLightColorScheme
    }

    val colorScheme = if (AppSettings.theme.value == AppTheme.SYSTEM || AppSettings.theme.value == AppTheme.DARK) {
        baseColorScheme.copy(
            primary = dynamicPrimary,
            background = dynamicBackground,
            surface = dynamicBackground.copy(alpha = 0.95f)
        )
    } else {
        baseColorScheme
    }

    CompositionLocalProvider(LocalSystemSettings provides systemSettings) {
        ProvideStrings {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography,
                content = content
            )
        }
    }
}
