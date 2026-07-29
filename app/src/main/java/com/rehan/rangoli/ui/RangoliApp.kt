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
import com.rehan.rangoli.domain.LevelCatalog
import com.rehan.rangoli.ui.game.GameScreen
import com.rehan.rangoli.ui.levels.LevelMapScreen
import kotlinx.coroutines.launch

private const val NO_LEVEL = -1

@Composable
fun RangoliApp() {
	val context = LocalContext.current
	val store = remember { ProgressStore(context) }
	val stars by store.stars.collectAsState(initial = emptyMap())
	val scope = rememberCoroutineScope()

	var currentLevel by rememberSaveable { mutableStateOf(NO_LEVEL) }

	if (currentLevel == NO_LEVEL) {
		LevelMapScreen(
			stars = stars,
			onLevelClick = { index -> currentLevel = index }
		)
	} else {
		val playing = currentLevel
		GameScreen(
			levelIndex = playing,
			onBack = { currentLevel = NO_LEVEL },
			onSolved = { earned -> scope.launch { store.saveStars(playing, earned) } },
			onNext = {
				currentLevel = if (playing + 1 < LevelCatalog.TOTAL) playing + 1 else NO_LEVEL
			}
		)
	}
}
