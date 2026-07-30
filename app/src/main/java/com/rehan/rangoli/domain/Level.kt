package com.rehan.rangoli.domain

data class Level(
	val index: Int,
	val size: Int,
	val symmetry: SymmetryType,
	val solution: Map<Cell, PaintColor>,
	val givenCells: Map<Cell, PaintColor>,
	val hiddenCells: Set<Cell>,
	val palette: Map<PaintColor, Int>
) {
	/** 1-based number shown to the player. */
	val displayNumber: Int get() = index + 1
}

data class LevelSpec(
	val index: Int,
	val size: Int,
	val symmetry: SymmetryType,
	val colors: List<PaintColor>,
	val spareTiles: Int
)
