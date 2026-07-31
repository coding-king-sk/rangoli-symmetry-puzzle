package com.rehan.rangoli.ui.levels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rehan.rangoli.domain.LevelCatalog
import com.rehan.rangoli.ui.game.starText

private const val TILES_PER_ROW = 5

@Composable
fun LevelMapScreen(
    stars: Map<Int, Int>,
    bestTimes: Map<Int, Int>,
    totalStars: Int,
    isDark: Boolean,
    onLevelClick: (Int) -> Unit,
    onAchievements: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val highestSolved    = remember(stars) { stars.keys.maxOrNull() ?: -1 }
    val completedCount   = stars.size
    val maxPossibleStars = LevelCatalog.TOTAL * 3

    LazyColumn(
        modifier            = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "Rangoli",
                        style      = MaterialTheme.typography.headlineMedium,
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text  = "⭐ $totalStars / $maxPossibleStars  •  $completedCount levels done",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onAchievements) {
                    Text(text = "🏆", style = MaterialTheme.typography.titleLarge)
                }
                IconButton(onClick = onToggleTheme) {
                    Text(
                        text  = if (isDark) "☀️" else "🌙",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            Text(
                text     = "Symmetry poori karo, palette limited hai",
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        LevelCatalog.chapters.forEach { chapter ->
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text       = chapter.title,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text  = chapter.symmetry.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(chapter.range.toList().chunked(TILES_PER_ROW)) { rowIndices ->
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowIndices.forEach { index ->
                        LevelTile(
                            index    = index,
                            stars    = stars[index] ?: 0,
                            unlocked = index <= highestSolved + 1,
                            onClick  = { onLevelClick(index) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(TILES_PER_ROW - rowIndices.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelTile(
    index: Int,
    stars: Int,
    unlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick  = onClick,
        enabled  = unlocked,
        modifier = modifier
            .aspectRatio(1f)
            .alpha(if (unlocked) 1f else 0.32f),
        shape    = RoundedCornerShape(14.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text       = "${index + 1}",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (stars > 0) {
                Text(
                    text  = starText(stars),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
