package com.rehan.rangoli.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

// ─── Simple screen stack ─────────────────────────────────────────────────────────

private sealed class Screen {
    object LevelMap      : Screen()
    object Achievements  : Screen()
    data class Game(val levelIndex: Int) : Screen()
}

@Composable
fun RangoliApp() {
    val context = LocalContext.current
    val store   = remember { ProgressStore(context) }
    val scope   = rememberCoroutineScope()

    // ── Persistent state ───────────────────────────────────────────────
    val stars        by store.stars.collectAsState(initial = emptyMap())
    val bestTimes    by store.bestTimes.collectAsState(initial = emptyMap())
    val totalStars   by store.totalStars.collectAsState(initial = 0)
    val achievements by store.unlockedAchievements.collectAsState(initial = emptySet())
    val streak       by store.cleanStreak.collectAsState(initial = 0)
    val themeMode    by store.themeMode.collectAsState(initial = "dark")
    val isDark = themeMode == "dark"

    // ── Navigation ───────────────────────────────────────────────────────
    var screen by rememberSaveable { mutableStateOf<Screen>(Screen.LevelMap) }

    RangoliTheme(isDark = isDark) {
        when (val s = screen) {

            Screen.LevelMap -> LevelMapScreen(
                stars          = stars,
                bestTimes      = bestTimes,
                totalStars     = totalStars,
                isDark         = isDark,
                onLevelClick   = { index -> screen = Screen.Game(index) },
                onAchievements = { screen = Screen.Achievements },
                onToggleTheme  = {
                    scope.launch { store.saveTheme(if (isDark) "light" else "dark") }
                }
            )

            Screen.Achievements -> AchievementsScreen(
                unlockedIds    = achievements,
                totalStars     = totalStars,
                completedCount = stars.size,
                onBack         = { screen = Screen.LevelMap }
            )

            is Screen.Game -> {
                val playing = s.levelIndex
                GameScreen(
                    levelIndex           = playing,
                    starsMap             = stars,
                    bestTimesMap         = bestTimes,
                    unlockedAchievements = achievements,
                    onBack               = { screen = Screen.LevelMap },
                    onSolved             = { earned, seconds ->
                        scope.launch {
                            store.saveStars(playing, earned)
                            store.saveBestTime(playing, seconds)

                            // Update streak
                            val newStreak = if (earned >= 2) streak + 1 else 0
                            store.saveStreak(newStreak)

                            // ── Achievement checks ──
                            val allStars = stars + (playing to maxOf(earned, stars[playing] ?: 0))
                            val newUnlocked = achievements.toMutableSet()

                            fun maybeUnlock(a: Achievement) = scope.launch {
                                if (a.id !in newUnlocked) {
                                    store.unlockAchievement(a)
                                    newUnlocked.add(a.id)
                                }
                            }

                            if (allStars.size == 1)  maybeUnlock(Achievement.FIRST_WIN)
                            if (earned == 3)         maybeUnlock(Achievement.PERFECT_SOLVE)
                            if (seconds <= 60)       maybeUnlock(Achievement.SPEED_STAR)
                            if (allStars.size >= 50) maybeUnlock(Achievement.HALFWAY)
                            if (allStars.size >= LevelCatalog.TOTAL) maybeUnlock(Achievement.MASTER)
                            if (newStreak >= 5)      maybeUnlock(Achievement.STREAK_FIVE)

                            // Chapter 1 complete
                            val ch1Done = (0..14).all { it in allStars }
                            if (ch1Done) maybeUnlock(Achievement.CHAPTER_ONE)

                            // 10 levels with 3 stars
                            val perfectCount = allStars.values.count { it == 3 }
                            if (perfectCount >= 10) maybeUnlock(Achievement.THREE_STARS_TEN)

                            // 10 speed clears tracked via bestTimes
                            val speedCount = (bestTimes + (playing to seconds)).values.count { it <= 60 }
                            if (speedCount >= 10) maybeUnlock(Achievement.SPEED_TEN)

                            // 10 levels without hints (stars==3 is a good proxy)
                            if (perfectCount >= 10) maybeUnlock(Achievement.NO_HINT_TEN)
                        }
                    },
                    onNext = {
                        screen = if (playing + 1 < LevelCatalog.TOTAL)
                            Screen.Game(playing + 1)
                        else
                            Screen.LevelMap
                    }
                )
            }
        }
    }
}
