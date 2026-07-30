package com.rehan.rangoli.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Shared accent
val Gold    = Color(0xFFF2C14E)
val Sindoor = Color(0xFFE2482F)

// Dark (default)
val Midnight        = Color(0xFF0E0B1F)
val MidnightSurface = Color(0xFF1A1533)

private val DarkColors = darkColorScheme(
    primary          = Gold,
    onPrimary        = Midnight,
    background       = Midnight,
    onBackground     = Color(0xFFEDE9FF),
    surface          = MidnightSurface,
    onSurface        = Color(0xFFEDE9FF),
    surfaceVariant   = Color(0xFF262046),
    onSurfaceVariant = Color(0xFFC9C2E8),
    error            = Color(0xFFFF5252),
    onError          = Color(0xFFFFFFFF)
)

// Light
private val LightColors = lightColorScheme(
    primary          = Color(0xFF7A5C00),
    onPrimary        = Color(0xFFFFFFFF),
    background       = Color(0xFFFFF8EE),
    onBackground     = Color(0xFF1C1A0F),
    surface          = Color(0xFFFFF1D6),
    onSurface        = Color(0xFF1C1A0F),
    surfaceVariant   = Color(0xFFEDE0C4),
    onSurfaceVariant = Color(0xFF4E4534),
    error            = Color(0xFFB3261E),
    onError          = Color(0xFFFFFFFF)
)

@Composable
fun RangoliTheme(isDark: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isDark) DarkColors else LightColors,
        content     = content
    )
}
