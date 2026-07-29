package com.rehan.rangoli.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Midnight = Color(0xFF0E0B1F)
val MidnightSurface = Color(0xFF1A1533)
val Gold = Color(0xFFF2C14E)

private val RangoliColorScheme = darkColorScheme(
	primary = Gold,
	onPrimary = Midnight,
	background = Midnight,
	onBackground = Color(0xFFEDE9FF),
	surface = MidnightSurface,
	onSurface = Color(0xFFEDE9FF),
	surfaceVariant = Color(0xFF262046),
	onSurfaceVariant = Color(0xFFC9C2E8)
)

@Composable
fun RangoliTheme(content: @Composable () -> Unit) {
	MaterialTheme(
		colorScheme = RangoliColorScheme,
		content = content
	)
}
