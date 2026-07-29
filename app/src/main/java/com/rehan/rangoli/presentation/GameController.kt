package com.rehan.rangoli.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rehan.rangoli.domain.Cell
import com.rehan.rangoli.domain.Level
import com.rehan.rangoli.domain.PaintColor

/**
 * Holds all mutable state for one puzzle attempt.
 *
 * Deliberately thin: every rule that decides what is *correct* lives in the
 * domain layer, so this class only tracks what the player has done.
 */
class GameController(val level: Level) {

	var filled by mutableStateOf<Map<Cell, PaintColor>>(emptyMap())
		private set

	var selected by mutableStateOf<PaintColor?>(level.palette.keys.firstOrNull())
		private set

	var wrongCells by mutableStateOf<Set<Cell>>(emptySet())
		private set

	var mistakes by mutableStateOf(0)
		private set

	var hintsUsed by mutableStateOf(0)
		private set

	var solved by mutableStateOf(false)
		private set

	private val history = ArrayDeque<Map<Cell, PaintColor>>()

	/** Clue cells plus whatever the player has placed. */
	val pattern: Map<Cell, PaintColor>
		get() = level.givenCells + filled

	/** Tiles left per colour. */
	val remaining: Map<PaintColor, Int>
		get() = level.palette.mapValues { (color, total) ->
			total - filled.values.count { it == color }
		}

	val filledCount: Int get() = filled.size
	val requiredCount: Int get() = level.hiddenCells.size
	val canUndo: Boolean get() = history.isNotEmpty()

	fun select(color: PaintColor) {
		if (!solved) selected = color
	}

	fun tap(cell: Cell) {
		if (solved) return
		if (cell !in level.hiddenCells) return

		// Tapping a filled cell clears it and returns the tile to the palette.
		if (filled.containsKey(cell)) {
			snapshot()
			filled = filled - cell
			wrongCells = wrongCells - cell
			return
		}

		val color = selected ?: return
		if ((remaining[color] ?: 0) <= 0) return

		snapshot()
		filled = filled + (cell to color)
		wrongCells = emptySet()
		if (filled.size == level.hiddenCells.size) evaluate()
	}

	fun undo() {
		if (solved) return
		val previous = history.removeLastOrNull() ?: return
		filled = previous
		wrongCells = emptySet()
	}

	fun hint() {
		if (solved) return
		val target = level.hiddenCells.firstOrNull { filled[it] != level.solution[it] } ?: return
		val correct = level.solution[target] ?: return

		snapshot()
		hintsUsed++
		filled = filled + (target to correct)
		wrongCells = wrongCells - target
		if (filled.size == level.hiddenCells.size) evaluate()
	}

	fun reset() {
		if (solved) return
		snapshot()
		filled = emptyMap()
		wrongCells = emptySet()
	}

	fun stars(): Int = when {
		mistakes == 0 && hintsUsed == 0 -> 3
		mistakes <= 1 && hintsUsed <= 1 -> 2
		else -> 1
	}

	private fun evaluate() {
		val wrong = level.hiddenCells.filter { filled[it] != level.solution[it] }.toSet()
		if (wrong.isEmpty()) {
			solved = true
			wrongCells = emptySet()
		} else {
			mistakes++
			wrongCells = wrong
		}
	}

	private fun snapshot() {
		history.addLast(filled)
		if (history.size > MAX_HISTORY) history.removeFirst()
	}

	private companion object {
		const val MAX_HISTORY = 40
	}
}
