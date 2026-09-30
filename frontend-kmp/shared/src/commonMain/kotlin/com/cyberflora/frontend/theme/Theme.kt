package com.cyberflora.frontend.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberFloraColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = BackgroundPrimary,
    secondary = NeonPrimary,
    onSecondary = BackgroundPrimary,
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    surface = SurfaceSecondary,
    onSurface = TextPrimary,
    error = ErrorColor,
    onError = TextPrimary
)

@Composable
fun CyberFloraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CyberFloraColorScheme,
        typography = CyberFloraTypography,
        content = content
    )
}