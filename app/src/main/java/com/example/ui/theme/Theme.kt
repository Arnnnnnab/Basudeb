package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB07F), // Soft spiritual saffron
    onPrimary = Color(0xFF4E2000),
    primaryContainer = Color(0xFF703200),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFFBAC8DB), // Soft sky blue
    onSecondary = Color(0xFF243140),
    background = Color(0xFF040E1B), // Beautiful deep Śrī Kṛṣṇa Cosmic Midnight Blue
    surface = Color(0xFF0C192E), // Spiritual midnight blue slate card surface
    onBackground = Color(0xFFF0F4FA),
    onSurface = Color(0xFFF0F4FA),
    error = Color(0xFFF2B8B5)
)

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = Color.White,
    primaryContainer = SaffronLight,
    onPrimaryContainer = SaffronDark,
    secondary = DeepMaroon, // Śrī Kṛṣṇa Shyama Midnight Blue
    onSecondary = Color.White,
    tertiary = GoldAccent, // Temple Gold
    onTertiary = HighContrastText,
    background = TempleBackground,
    surface = TempleSurface,
    onBackground = HighContrastText,
    onSurface = HighContrastText,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
