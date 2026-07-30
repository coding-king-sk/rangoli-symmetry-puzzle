package com.rehan.rangoli.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rehan.rangoli.domain.Cell
import com.rehan.rangoli.domain.Level
import com.rehan.rangoli.domain.PaintColor

/**
 * All mutable UI state for a single puzzle attempt.
 *
 * This class intentionally has no Android or Compose-lifecycle imports other
 * than [mutableStateOf]; it is easy to unit-test and to swap out.
 *
 * The timer is driven externally: call [tick] once per second from a
 * LaunchedEffect in the host composable.
 */
class GameController(val level: Level) {

	// ── Core play state ────────────────────────────────────────────────────

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

	// ── Timer ──────────────────────────────────────────────────────────────

	var elapsedSeconds by mutableStateOf(0)
		private set

	/** Must be called once per second from a LaunchedEffect. Stops automatically on solve. */
	fun tick() {
		if (!solved) elapsedSeconds++
	}

	// ── Colour highlight ───────────────────────────────────────────────────

	/**
	 * When non-null the canvas dims every cell that does NOT belong to this
	 * colour, helping the player see how many of this colour are still needed.
	 */
	var highlightColor by mutableStateOf<PaintColor?>(null)
		private set

	fun toggleHighlight(color: PaintColor) {
		highlightColor = if (highlightColor == color) null else color
	}

	fun clearHighlight() {
		highlightColor = null
	}

	// ── Animation hint ─────────────────────────────────────────────────────

	/** The most recently placed (or hinted) cell; drives the scale-pulse animation. */
	var lastPlacedCell by mutableStateOf<Cell?>(null)
		private set

	// ── Derived state ──────────────────────────────────────────────────────

	/** The full board as the player sees it: given clues plus their placements. */
	val pattern: Map<Cell, PaintColor>
		get() = level.givenCells + filled

	/** Tiles remaining per colour. */
	val remaining: Map<PaintColor, Int>
		get() = level.palette.mapValues { (color, total) ->
			total - filled.values.count { it == color }
		}

	val filledCount: Int  get() = filled.size
	val requiredCount: Int get() = level.hiddenCells.size
	val canUndo: Boolean  get() = history.isNotEmpty()

	// ── Actions ────────────────────────────────────────────────────────────

	fun select(color: PaintColor) {
		if (!solved) selected = color
	}

	fun tap(cell: Cell) {
		if (solved) return
		if (cell !in level.hiddenCells) return

		if (filled.containsKey(cell)) {
			// Clear an already-filled cell and return the tile.
			snapshot()
			filled = filled - cell
			wrongCells = wrongCells - cell
			lastPlacedCell = null
			return
		}

		val color = selected ?: return
		if ((remaining[color] ?: 0) <= 0) return

		snapshot()
		lastPlacedCell = cell
		filled = filled + (cell to color)
		wrongCells = emptySet()
		if (filled.size == level.hiddenCells.size) evaluate()
	}

	fun undo() {
		if (solved) return
		val previous = history.removeLastOrNull() ?: return
		filled = previous
		wrongCells = emptySet()
		lastPlacedCell = null
	}

	fun hint() {
		if (solved) return
		val target = level.hiddenCells.firstOrNull { filled[it] != level.solution[it] } ?: return
		val correct = level.solution[target] ?: return
		snapshot()
		hintsUsed++
		lastPlacedCell = target
		filled = filled + (target to correct)
		wrongCells = wrongCells - target
		if (filled.size == level.hiddenCells.size) evaluate()
	}

	fun reset() {
		if (solved) return
		snapshot()
		filled = emptyMap()
		wrongCells = emptySet()
		lastPlacedCell = null
	}

	fun stars(): Int = when {
		mistakes == 0 && hintsUsed == 0 -> 3
		mistakes <= 1 && hintsUsed <= 1  -> 2
		else                             -> 1
	}

	// ── Private helpers ────────────────────────────────────────────────────

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

	private val history = ArrayDeque<Map<Cell, PaintColor>>()

	private companion object {
		const val MAX_HISTORY = 40
	}
}
