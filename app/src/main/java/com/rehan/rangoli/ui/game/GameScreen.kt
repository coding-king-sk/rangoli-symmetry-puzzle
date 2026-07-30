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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rehan.rangoli.domain.LevelCatalog
import com.rehan.rangoli.presentation.GameController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Timer colour changes: green → yellow → orange → red
private fun timerColor(seconds: Int): Color = when {
    seconds < 30  -> Color(0xFF4CAF50)
    seconds < 60  -> Color(0xFFFFEB3B)
    seconds < 120 -> Color(0xFFFF9800)
    else          -> Color(0xFFF44336)
}

private fun formatTime(s: Int): String =
    "%d:%02d".format(s / 60, s % 60)

@Composable
fun GameScreen(
    levelIndex: Int,
    starsMap: Map<Int, Int>,
    bestTimesMap: Map<Int, Int>,
    unlockedAchievements: Set<String>,
    onBack: () -> Unit,
    onSolved: (stars: Int, seconds: Int) -> Unit,
    onNext: () -> Unit
) {
    val level   = remember(levelIndex) { LevelCatalog.level(levelIndex) }
    val chapter = remember(levelIndex) { LevelCatalog.chapterOf(levelIndex) }

    // playCount lets the player replay without leaving the screen
    var playCount by rememberSaveable { mutableStateOf(0) }
    val controller = remember(levelIndex, playCount) { GameController(level) }
    val sound      = rememberSoundManager()
    val scope      = rememberCoroutineScope()

    // ── Timer ──────────────────────────────────────────────────────────────
    LaunchedEffect(levelIndex, playCount) {
        while (!controller.solved) {
            delay(1_000)
            controller.tick()
        }
    }

    // ── Wrong-cell shake ───────────────────────────────────────────────────
    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(controller.wrongCells) {
        if (controller.wrongCells.isNotEmpty()) {
            sound.playWrong()
            shakeAnim.animateTo(
                0f,
                keyframes {
                    durationMillis = 400
                     18f at  50
                    -18f at 100
                     14f at 150
                    -14f at 200
                     8f  at 250
                    -8f  at 300
                     0f  at 400
                }
            )
        }
    }

    // ── Solve callback ────────────────────────────────────────────────────
    var solveCallbackFired by remember { mutableStateOf(false) }
    LaunchedEffect(controller.solved) {
        if (controller.solved && !solveCallbackFired) {
            solveCallbackFired = true
            sound.playSolve()
            onSolved(controller.stars(), controller.elapsedSeconds)
        }
    }

    val isChapterLast = levelIndex == chapter.range.last
    val bestTime      = bestTimesMap[levelIndex]

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
                // Timer pill
                Text(
                    text  = formatTime(controller.elapsedSeconds),
                    style = MaterialTheme.typography.labelLarge,
                    color = timerColor(controller.elapsedSeconds)
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

            // Best time badge
            if (bestTime != null) {
                Text(
                    text  = "Best: ${formatTime(bestTime)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Timer progress bar (fills every 2 min then resets hue)
            LinearProgressIndicator(
                progress         = { ((controller.elapsedSeconds % 120) / 120f).coerceIn(0f, 1f) },
                modifier         = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                color            = timerColor(controller.elapsedSeconds),
                trackColor       = MaterialTheme.colorScheme.surfaceVariant
            )

            // ── Board ──────────────────────────────────────────────────────
            RangoliCanvas(
                level           = level,
                pattern         = controller.pattern,
                wrongCells      = controller.wrongCells,
                solved          = controller.solved,
                lastPlacedCell  = controller.lastPlacedCell,
                highlightColor  = controller.highlightColor,
                shakeOffsetPx   = shakeAnim.value,
                onCellTap       = { cell ->
                    controller.tap(cell)
                    if (cell in level.hiddenCells && !controller.wrongCells.contains(cell))
                        sound.playPlace()
                },
                modifier        = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Progress + mistakes row
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

            // ── Palette (tap = select, long-press = highlight) ────────────────
            PaletteBar(
                remaining         = controller.remaining,
                selected          = controller.selected,
                highlightColor    = controller.highlightColor,
                onSelect          = controller::select,
                onToggleHighlight = controller::toggleHighlight
            )

            Spacer(Modifier.height(14.dp))

            // ── Action buttons ─────────────────────────────────────────────
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

        // ── Solved overlay ───────────────────────────────────────────────
        AnimatedVisibility(
            visible = controller.solved,
            enter   = fadeIn() + scaleIn(initialScale = 0.88f),
            exit    = fadeOut()
        ) {
            SolvedOverlay(
                stars          = controller.stars(),
                elapsedSeconds = controller.elapsedSeconds,
                bestTime       = bestTime,
                isChapterLast  = isChapterLast,
                isLast         = levelIndex == LevelCatalog.TOTAL - 1,
                onNext         = onNext,
                onReplay       = { scope.launch { playCount++ } },
                onLevels       = onBack
            )
        }

        // ── Chapter-complete banner (shows briefly when last level of chapter done) ─
        var showChapterBanner by remember { mutableStateOf(false) }
        LaunchedEffect(controller.solved) {
            if (controller.solved && isChapterLast) {
                delay(1_200)
                showChapterBanner = true
            }
        }
        AnimatedVisibility(
            visible = showChapterBanner && !controller.solved.not(),
            enter   = fadeIn() + scaleIn(),
            exit    = fadeOut()
        ) {
            if (showChapterBanner) {
                ChapterCompleteOverlay(
                    chapterTitle = chapter.title,
                    onDismiss    = { showChapterBanner = false }
                )
            }
        }
    }
}

// ─── Solved overlay ───────────────────────────────────────────────────────────

@Composable
private fun SolvedOverlay(
    stars: Int,
    elapsedSeconds: Int,
    bestTime: Int?,
    isChapterLast: Boolean,
    isLast: Boolean,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    onLevels: () -> Unit
) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.60f)),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.padding(32.dp)) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text       = if (isChapterLast) "Chapter poori! 🎊" else "Rangoli poori! 🌸",
                    style      = MaterialTheme.typography.headlineSmall,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

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
                if (bestTime != null && elapsedSeconds < bestTime) {
                    Text(
                        text  = "⚡ Naya best time!",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFFD700)
                    )
                } else if (bestTime != null) {
                    Text(
                        text  = "Best: ${formatTime(bestTime)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(18.dp))

                if (!isLast) {
                    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                        Text("Agla level ➡")
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

// ─── Chapter-complete banner ───────────────────────────────────────────────────

@Composable
fun ChapterCompleteOverlay(chapterTitle: String, onDismiss: () -> Unit) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.70f)),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.padding(40.dp)) {
            Column(
                modifier            = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text       = "🎊",
                    style      = MaterialTheme.typography.displayMedium
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text       = "Chapter Complete!",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "“$chapterTitle” khatam",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Aage bado!")
                }
            }
        }
    }
}

internal fun starText(stars: Int): String =
    "\u2605".repeat(stars) + "\u2606".repeat((3 - stars).coerceAtLeast(0))
