package com.rehan.rangoli.domain

/** Everything needed to play one puzzle. */
data class Level(
	val index: Int,
	val size: Int,
	val symmetry: SymmetryType,
	/** The complete, correct pattern. */
	val solution: Map<Cell, PaintColor>,
	/** Cells revealed up front as the clue. */
	val givenCells: Map<Cell, PaintColor>,
	/** Cells the player has to fill. */
	val hiddenCells: Set<Cell>,
	/** How many tiles of each colour the player is allowed to place. */
	val palette: Map<PaintColor, Int>
) {
	val displayNumber: Int get() = index + 1
}

/** Difficulty knobs. Tuning these is how the 100 levels are shaped. */
data class LevelSpec(
	val index: Int,
	val size: Int,
	val symmetry: SymmetryType,
	val colors: List<PaintColor>,
	/**
	 * Extra tiles beyond what the solution needs. 0 makes the palette an exact
	 * fit (strict), higher values are forgiving. Unused colours become decoys.
	 */
	val spareTiles: Int
)
