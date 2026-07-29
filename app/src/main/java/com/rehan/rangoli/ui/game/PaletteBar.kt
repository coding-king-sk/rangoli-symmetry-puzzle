package com.rehan.rangoli.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rehan.rangoli.domain.PaintColor

/**
 * The colour palette with remaining counts.
 *
 * Each swatch also shows a shape glyph, so the game does not rely on hue alone.
 */
@Composable
fun PaletteBar(
	remaining: Map<PaintColor, Int>,
	selected: PaintColor?,
	onSelect: (PaintColor) -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.Top
	) {
		remaining.entries
			.sortedBy { it.key.ordinal }
			.forEach { (color, count) ->
				val isSelected = color == selected
				val isAvailable = count > 0

				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier.padding(horizontal = 6.dp)
				) {
					Box(
						modifier = Modifier
							.size(if (isSelected) 54.dp else 46.dp)
							.clip(CircleShape)
							.background(Color(color.argb))
							.border(
								width = if (isSelected) 3.dp else 0.dp,
								color = MaterialTheme.colorScheme.primary,
								shape = CircleShape
							)
							.alpha(if (isAvailable) 1f else 0.32f)
							.clickable(enabled = isAvailable) { onSelect(color) },
						contentAlignment = Alignment.Center
					) {
						Text(
							text = count.toString(),
							color = Color.Black.copy(alpha = 0.72f),
							fontWeight = FontWeight.Bold,
							style = MaterialTheme.typography.titleMedium
						)
					}

					Text(
						text = color.glyph,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						style = MaterialTheme.typography.labelMedium,
						modifier = Modifier.padding(top = 4.dp)
					)
				}
			}
	}
}
