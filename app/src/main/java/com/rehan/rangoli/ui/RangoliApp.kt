package com.rehan.rangoli.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.rehan.rangoli.data.ProgressStore
import com.rehan.rangoli.domain.Achievement
import com.rehan.rangoli.domain.LevelCatalog
import com.rehan.rangoli.ui.achievements.AchievementsScreen
import com.rehan.rangoli.ui.game.GameScreen
import com.rehan.rangoli.ui.levels.LevelMapScreen
import com.rehan.rangoli.ui.theme.RangoliTheme
import kotlinx.coroutines.launch

/*
 * Navigation is stored as plain Ints.
 *
 * IMPORTANT: a sealed class cannot be put in `rememberSaveable` unless it is
 * Parcelable or has a custom Saver. Doing so throws
 *   IllegalArgumentException: ... cannot be saved using the current SaveableStateRegistry
 * the moment Android saves instance state (backgrounding the app, rotating the
 * screen, or the system reclaiming memory). Ints are saveable out of the box.
 */
private const val SCREEN_LEVEL_MAP    = 0
private const val SCREEN_ACHIEVEMENTS = 1
private const val SCREEN_GAME         = 2

@Composable
fun RangoliApp() {
    val context = LocalContext.current
    val store   = remember { ProgressStore(context) }
    val scope   = rememberCoroutineScope()

    // ── Persistent state ────────────────────────────────────────────────────
    val stars        by store.stars.collectAsState(initial = emptyMap())
    val bestTimes    by store.bestTimes.collectAsState(initial = emptyMap())
    val totalStars   by store.totalStars.collectAsState(initial = 0)
    val achievements by store.unlockedAchievements.collectAsState(initial = emptySet())
    val streak       by store.cleanStreak.collectAsState(initial = 0)
    val themeMode    by store.themeMode.collectAsState(initial = "dark")
    val colorBlind   by store.colorBlindMode.collectAsState(initial = false)
    val isDark = themeMode == "dark"

    // ── Navigation (saveable primitives only) ───────────────────────────────
    var screen       by rememberSaveable { mutableIntStateOf(SCREEN_LEVEL_MAP) }
    var playingIndex by rememberSaveable { mutableIntStateOf(0) }

    RangoliTheme(isDark = isDark) {
        when (screen) {

            SCREEN_ACHIEVEMENTS -> AchievementsScreen(
                unlockedIds    = achievements,
                totalStars     = totalStars,
                completedCount = stars.size,
                onBack         = { screen = SCREEN_LEVEL_MAP }
            )

            SCREEN_GAME -> GameScreen(
                levelIndex     = playingIndex,
                bestTimesMap   = bestTimes,
                colorBlindMode = colorBlind,
                onBack         = { screen = SCREEN_LEVEL_MAP },
                onSolved       = { earned, seconds ->
                    scope.launch {
                        val solvedIndex = playingIndex
                        store.saveStars(solvedIndex, earned)
                        store.saveBestTime(solvedIndex, seconds)

                        // Mistake-free streak
                        val newStreak = if (earned >= 2) streak + 1 else 0
                        store.saveStreak(newStreak)

                        // ── Achievement checks ──
                        val allStars = stars + (solvedIndex to maxOf(earned, stars[solvedIndex] ?: 0))
                        val allTimes = bestTimes + (solvedIndex to seconds)
                        val perfectCount = allStars.values.count { it == 3 }

                        suspend fun unlock(a: Achievement) {
                            if (a.id !in achievements) store.unlockAchievement(a)
                        }

                        if (allStars.size == 1)                       unlock(Achievement.FIRST_WIN)
                        if (earned == 3)                              unlock(Achievement.PERFECT_SOLVE)
                        if (seconds <= 60)                            unlock(Achievement.SPEED_STAR)
                        if (allStars.size >= 50)                      unlock(Achievement.HALFWAY)
                        if (allStars.size >= LevelCatalog.TOTAL)       unlock(Achievement.MASTER)
                        if (newStreak >= 5)                           unlock(Achievement.STREAK_FIVE)
                        if ((0..14).all { it in allStars })            unlock(Achievement.CHAPTER_ONE)
                        if (perfectCount >= 10) {
                            unlock(Achievement.THREE_STARS_TEN)
                            unlock(Achievement.NO_HINT_TEN)
                        }
                        if (allTimes.values.count { it <= 60 } >= 10)  unlock(Achievement.SPEED_TEN)
                    }
                },
                onNext = {
                    if (playingIndex + 1 < LevelCatalog.TOTAL) {
                        playingIndex += 1
                    } else {
                        screen = SCREEN_LEVEL_MAP
                    }
                }
            )

            else -> LevelMapScreen(
                stars              = stars,
                bestTimes          = bestTimes,
                totalStars         = totalStars,
                isDark             = isDark,
                colorBlindMode     = colorBlind,
                onLevelClick       = { index ->
                    playingIndex = index
                    screen = SCREEN_GAME
                },
                onAchievements     = { screen = SCREEN_ACHIEVEMENTS },
                onToggleTheme      = {
                    scope.launch { store.saveTheme(if (isDark) "light" else "dark") }
                },
                onToggleColorBlind = {
                    scope.launch { store.saveColorBlind(!colorBlind) }
                }
            )
        }
    }
}
