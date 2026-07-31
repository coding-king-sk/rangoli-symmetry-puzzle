package com.rehan.rangoli.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rehan.rangoli.domain.LevelCatalog
import com.rehan.rangoli.presentation.GameController
import kotlinx.coroutines.delay

// Timer colour ramps green → yellow → orange → red
private fun timerColor(seconds: Int): Color = when {
    seconds < 30  -> Color(0xFF4CAF50)
    seconds < 60  -> Color(0xFFFFEB3B)
    seconds < 120 -> Color(0xFFFF9800)
    else          -> Color(0xFFF44336)
}

private fun formatTime(s: Int): String = "%d:%02d".format(s / 60, s % 60)

@Composable
fun GameScreen(
    levelIndex: Int,
    bestTimesMap: Map<Int, Int>,
    onBack: () -> Unit,
    onSolved: (stars: Int, seconds: Int) -> Unit,
    onNext: () -> Unit
) {
    val level   = remember(levelIndex) { LevelCatalog.level(levelIndex) }
    val chapter = remember(levelIndex) { LevelCatalog.chapterOf(levelIndex) }

    // Bumping playCount rebuilds the controller → "play again" without leaving.
    var playCount by rememberSaveable { mutableIntStateOf(0) }
    val controller = remember(levelIndex, playCount) { GameController(level) }
    val sound      = rememberSoundManager()

    // Snapshot the best time BEFORE this attempt, otherwise onSolved has already
    // overwritten it and the "new best!" badge can never show.
    val bestTimeAtStart = remember(levelIndex, playCount) { bestTimesMap[levelIndex] }

    // ── Timer ───────────────────────────────────────────────────────────────
    LaunchedEffect(levelIndex, playCount) {
        while (!controller.solved) {
            delay(1_000)
            controller.tick()
        }
    }

    // ── Wrong-cell shake ────────────────────────────────────────────────────
    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(controller.wrongCells) {
        if (controller.wrongCells.isNotEmpty()) {
            sound.playWrong()
            shakeAnim.snapTo(0f)
            shakeAnim.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    18f at 50
                    -18f at 100
                    14f at 150
                    -14f at 200
                    8f at 250
                    -8f at 300
                    0f at 400
                }
            )
        }
    }

    // ── Fire onSolved exactly once per attempt ──────────────────────────────
    // Keyed on playCount so a replay reports its result too.
    var reported by remember(levelIndex, playCount) { mutableStateOf(false) }
    LaunchedEffect(controller.solved, levelIndex, playCount) {
        if (controller.solved && !reported) {
            reported = true
            sound.playSolve()
            onSolved(controller.stars(), controller.elapsedSeconds)
        }
    }

    val isChapterLast = levelIndex == chapter.range.last

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
            // ── Top bar ─────────────────────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) { Text("Levels") }
                Spacer(Modifier.weight(1f))
                Text(
                    text       = "Level ${level.displayNumber}",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text       = formatTime(controller.elapsedSeconds),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = timerColor(controller.elapsedSeconds)
                )
            }

            Text(
                text  = chapter.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text  = level.symmetry.hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (bestTimeAtStart != null) {
                Text(
                    text  = "Best: ${formatTime(bestTimeAtStart)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Simple hand-rolled timer bar (no Material version dependency)
            TimerBar(
                seconds  = controller.elapsedSeconds,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // ── Board ───────────────────────────────────────────────────────
            RangoliCanvas(
                level          = level,
                pattern        = controller.pattern,
                wrongCells     = controller.wrongCells,
                solved         = controller.solved,
                lastPlacedCell = controller.lastPlacedCell,
                highlightColor = controller.highlightColor,
                shakeOffsetPx  = shakeAnim.value,
                onCellTap      = { cell ->
                    val wasEmpty = controller.pattern[cell] == null
                    controller.tap(cell)
                    if (wasEmpty && controller.pattern[cell] != null) sound.playPlace()
                },
                modifier       = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text  = "${controller.filledCount} / ${controller.requiredCount} bhare",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                if (controller.mistakes > 0) {
                    Text(
                        text  = "Galtiyan: ${controller.mistakes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text  = "Tip: rang ko der tak dabao → sirf wahi rang highlight hoga",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(6.dp))

            PaletteBar(
                remaining         = controller.remaining,
                selected          = controller.selected,
                highlightColor    = controller.highlightColor,
                onSelect          = controller::select,
                onToggleHighlight = controller::toggleHighlight
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick  = { controller.undo(); sound.playUndo() },
                    enabled  = controller.canUndo && !controller.solved,
                    modifier = Modifier.weight(1f)
                ) { Text("Undo") }

                OutlinedButton(
                    onClick  = { controller.hint(); sound.playHint() },
                    enabled  = !controller.solved,
                    modifier = Modifier.weight(1f)
                ) { Text("Hint") }

                OutlinedButton(
                    onClick  = { controller.reset() },
                    enabled  = !controller.solved,
                    modifier = Modifier.weight(1f)
                ) { Text("Reset") }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Single overlay, two variants (no stray banner state) ────────────
        AnimatedVisibility(
            visible = controller.solved,
            enter   = fadeIn() + scaleIn(initialScale = 0.88f),
            exit    = fadeOut()
        ) {
            ResultOverlay(
                isChapterComplete = isChapterLast,
                chapterTitle      = chapter.title,
                stars             = controller.stars(),
                elapsedSeconds    = controller.elapsedSeconds,
                bestTimeAtStart   = bestTimeAtStart,
                isFinalLevel      = levelIndex == LevelCatalog.TOTAL - 1,
                onNext            = onNext,
                onReplay          = { playCount++ },
                onLevels          = onBack
            )
        }
    }
}

@Composable
private fun TimerBar(seconds: Int, modifier: Modifier = Modifier) {
    val fraction = ((seconds % 120) / 120f).coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(timerColor(seconds))
        )
    }
}

@Composable
private fun ResultOverlay(
    isChapterComplete: Boolean,
    chapterTitle: String,
    stars: Int,
    elapsedSeconds: Int,
    bestTimeAtStart: Int?,
    isFinalLevel: Boolean,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    onLevels: () -> Unit
) {
    val isNewBest = bestTimeAtStart == null || elapsedSeconds < bestTimeAtStart

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = if (isChapterComplete) 0.72f else 0.60f)),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.padding(32.dp)) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isChapterComplete) {
                    Text(text = "🎊", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text       = "Chapter Complete!",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text  = "“$chapterTitle” khatam",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text       = "Rangoli poori! 🌸",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text  = starText(stars),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text  = "Waqt: ${formatTime(elapsedSeconds)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isNewBest) {
                    Text(
                        text  = "⚡ Naya best time!",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(18.dp))

                if (!isFinalLevel) {
                    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isChapterComplete) "Agla chapter ➡" else "Agla level ➡")
                    }
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedButton(onClick = onReplay, modifier = Modifier.fillMaxWidth()) {
                    Text("🔄 Dobara khelo")
                }
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = onLevels, modifier = Modifier.fillMaxWidth()) {
                    Text("Level map")
                }
            }
        }
    }
}

internal fun starText(stars: Int): String =
    "\u2605".repeat(stars.coerceIn(0, 3)) + "\u2606".repeat((3 - stars).coerceIn(0, 3))
