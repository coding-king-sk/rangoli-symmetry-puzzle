package com.rehan.rangoli.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rehan.rangoli.domain.LevelCatalog
import com.rehan.rangoli.presentation.GameController

@Composable
fun GameScreen(
	levelIndex: Int,
	onBack: () -> Unit,
	onSolved: (Int) -> Unit,
	onNext: () -> Unit
) {
	val level = remember(levelIndex) { LevelCatalog.level(levelIndex) }
	val chapter = remember(levelIndex) { LevelCatalog.chapterOf(levelIndex) }
	val controller = remember(levelIndex) { GameController(level) }

	LaunchedEffect(controller.solved) {
		if (controller.solved) onSolved(controller.stars())
	}

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 12.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				TextButton(onClick = onBack) { Text("Levels") }
				Spacer(modifier = Modifier.weight(1f))
				Text(
					text = "Level ${level.displayNumber}",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
			}

			Text(
				text = chapter.title,
				style = MaterialTheme.typography.labelLarge,
				color = MaterialTheme.colorScheme.primary
			)
			Text(
				text = level.symmetry.hint,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)

			Spacer(modifier = Modifier.height(16.dp))

			RangoliCanvas(
				level = level,
				pattern = controller.pattern,
				wrongCells = controller.wrongCells,
				solved = controller.solved,
				onCellTap = controller::tap,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(10.dp))

			Row(modifier = Modifier.fillMaxWidth()) {
				Text(
					text = "${controller.filledCount} / ${controller.requiredCount} bhare",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				Spacer(modifier = Modifier.weight(1f))
				if (controller.mistakes > 0) {
					Text(
						text = "Galtiyan: ${controller.mistakes}",
						style = MaterialTheme.typography.bodySmall,
						color = Color(0xFFFF8A80)
					)
				}
			}

			Spacer(modifier = Modifier.height(14.dp))

			PaletteBar(
				remaining = controller.remaining,
				selected = controller.selected,
				onSelect = controller::select
			)

			Spacer(modifier = Modifier.height(16.dp))

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				OutlinedButton(
					onClick = controller::undo,
					enabled = controller.canUndo && !controller.solved,
					modifier = Modifier.weight(1f)
				) { Text("Undo") }

				OutlinedButton(
					onClick = controller::hint,
					enabled = !controller.solved,
					modifier = Modifier.weight(1f)
				) { Text("Hint") }

				OutlinedButton(
					onClick = controller::reset,
					enabled = !controller.solved,
					modifier = Modifier.weight(1f)
				) { Text("Reset") }
			}

			Spacer(modifier = Modifier.height(24.dp))
		}

		if (controller.solved) {
			SolvedOverlay(
				stars = controller.stars(),
				isLast = levelIndex == LevelCatalog.TOTAL - 1,
				onNext = onNext,
				onLevels = onBack
			)
		}
	}
}

@Composable
private fun SolvedOverlay(
	stars: Int,
	isLast: Boolean,
	onNext: () -> Unit,
	onLevels: () -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Black.copy(alpha = 0.62f)),
		contentAlignment = Alignment.Center
	) {
		Card(modifier = Modifier.padding(32.dp)) {
			Column(
				modifier = Modifier.padding(24.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(
					text = "Rangoli poori!",
					style = MaterialTheme.typography.headlineSmall,
					color = MaterialTheme.colorScheme.primary,
					fontWeight = FontWeight.Bold
				)

				Spacer(modifier = Modifier.height(12.dp))

				Text(
					text = starText(stars),
					style = MaterialTheme.typography.headlineMedium,
					color = MaterialTheme.colorScheme.primary
				)

				Spacer(modifier = Modifier.height(20.dp))

				if (!isLast) {
					Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
						Text("Agla level")
					}
					Spacer(modifier = Modifier.height(8.dp))
				}

				OutlinedButton(onClick = onLevels, modifier = Modifier.fillMaxWidth()) {
					Text("Level map")
				}
			}
		}
	}
}

internal fun starText(stars: Int): String =
	"\u2605".repeat(stars) + "\u2606".repeat((3 - stars).coerceAtLeast(0))
