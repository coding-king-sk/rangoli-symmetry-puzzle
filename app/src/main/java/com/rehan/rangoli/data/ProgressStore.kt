package com.rehan.rangoli.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rehan.rangoli.domain.Achievement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.progressDataStore: DataStore<Preferences> by
	preferencesDataStore(name = "rangoli_progress")

/**
 * Persistent storage for player progress.
 *
 * Keys are kept flat (no serialisation library) so the store stays readable
 * if we ever need to inspect it with adb.
 */
class ProgressStore(private val context: Context) {

	// ── Stars ───────────────────────────────────────────────────────────────

	val stars: Flow<Map<Int, Int>> = context.progressDataStore.data.map { prefs ->
		prefs.asMap().mapNotNull { (key, value) ->
			if (!key.name.startsWith(STAR_PREFIX) || value !is Int) return@mapNotNull null
			val index = key.name.removePrefix(STAR_PREFIX).toIntOrNull() ?: return@mapNotNull null
			index to value
		}.toMap()
	}

	val totalStars: Flow<Int> = stars.map { it.values.sum() }

	/** Only raises a level's rating, never lowers it. */
	suspend fun saveStars(levelIndex: Int, earned: Int) {
		context.progressDataStore.edit { prefs ->
			val key = intPreferencesKey("$STAR_PREFIX$levelIndex")
			if (earned > (prefs[key] ?: 0)) prefs[key] = earned
		}
	}

	// ── Best times ──────────────────────────────────────────────────────────

	val bestTimes: Flow<Map<Int, Int>> = context.progressDataStore.data.map { prefs ->
		prefs.asMap().mapNotNull { (key, value) ->
			if (!key.name.startsWith(TIME_PREFIX) || value !is Int) return@mapNotNull null
			val index = key.name.removePrefix(TIME_PREFIX).toIntOrNull() ?: return@mapNotNull null
			index to value
		}.toMap()
	}

	/** Only stores a time if it beats the previous best. */
	suspend fun saveBestTime(levelIndex: Int, seconds: Int) {
		context.progressDataStore.edit { prefs ->
			val key = intPreferencesKey("$TIME_PREFIX$levelIndex")
			if (seconds < (prefs[key] ?: Int.MAX_VALUE)) prefs[key] = seconds
		}
	}

	// ── Achievements ────────────────────────────────────────────────────────

	private val ACHIEVEMENTS_KEY = stringSetPreferencesKey("achievements")

	val unlockedAchievements: Flow<Set<String>> = context.progressDataStore.data.map { prefs ->
		prefs[ACHIEVEMENTS_KEY] ?: emptySet()
	}

	suspend fun unlockAchievement(achievement: Achievement) {
		context.progressDataStore.edit { prefs ->
			val current = prefs[ACHIEVEMENTS_KEY] ?: emptySet()
			if (achievement.id !in current) prefs[ACHIEVEMENTS_KEY] = current + achievement.id
		}
	}

	// ── Theme ────────────────────────────────────────────────────────────────

	private val THEME_KEY = stringPreferencesKey("theme_mode")

	/** "dark" (default) or "light". */
	val themeMode: Flow<String> = context.progressDataStore.data.map { prefs ->
		prefs[THEME_KEY] ?: "dark"
	}

	suspend fun saveTheme(mode: String) {
		context.progressDataStore.edit { prefs -> prefs[THEME_KEY] = mode }
	}

	// ── Consecutive-mistake-free streak ─────────────────────────────────────

	private val STREAK_KEY = intPreferencesKey("clean_streak")

	val cleanStreak: Flow<Int> = context.progressDataStore.data.map { prefs ->
		prefs[STREAK_KEY] ?: 0
	}

	suspend fun saveStreak(streak: Int) {
		context.progressDataStore.edit { prefs -> prefs[STREAK_KEY] = streak }
	}

	companion object {
		private const val STAR_PREFIX = "stars_"
		private const val TIME_PREFIX = "time_"
	}
}
