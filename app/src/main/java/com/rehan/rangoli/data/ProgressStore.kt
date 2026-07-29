package com.rehan.rangoli.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.progressDataStore: DataStore<Preferences> by
	preferencesDataStore(name = "rangoli_progress")

/** Stores the best star rating earned per level. */
class ProgressStore(private val context: Context) {

	val stars: Flow<Map<Int, Int>> = context.progressDataStore.data.map { preferences ->
		preferences.asMap()
			.mapNotNull { (key, value) ->
				val name = key.name
				if (!name.startsWith(STAR_PREFIX) || value !is Int) return@mapNotNull null
				val index = name.removePrefix(STAR_PREFIX).toIntOrNull()
					?: return@mapNotNull null
				index to value
			}
			.toMap()
	}

	/** Only ever raises a level's rating, never lowers it. */
	suspend fun saveStars(levelIndex: Int, earned: Int) {
		context.progressDataStore.edit { preferences ->
			val key = intPreferencesKey("$STAR_PREFIX$levelIndex")
			val best = preferences[key] ?: 0
			if (earned > best) preferences[key] = earned
		}
	}

	private companion object {
		const val STAR_PREFIX = "stars_"
	}
}
