package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF003730),
    primaryContainer = SalamTealDark,
    onPrimaryContainer = SalamTealLight,
    secondary = DarkSecondary,
    onSecondary = Color(0xFF003730),
    secondaryContainer = Color(0xFF1E3A36),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = SalamCyan,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF5C0000)
)

private val LightColorScheme = lightColorScheme(
    primary = SalamTealPrimary,
    onPrimary = Color.White,
    primaryContainer = SalamTealLight,
    onPrimaryContainer = SalamTealDark,
    secondary = SalamTealAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2F1),
    onSecondaryContainer = Color(0xFF004D40),
    tertiary = SalamCyan,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = SalamEndCallRed,
    onError = Color.White
)

@Composable
fun SalamCallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SalamCallTheme(darkTheme = darkTheme, content = content)
}
