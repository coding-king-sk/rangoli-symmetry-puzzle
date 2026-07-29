package com.rehan.rangoli.domain

/**
 * Checks a pattern against a symmetry rule.
 *
 * Returns the offending cells rather than a bare boolean, so the UI can show
 * the player exactly where the symmetry broke.
 */
class SymmetryValidator(private val size: Int) {

	fun invalidCells(
		pattern: Map<Cell, PaintColor?>,
		type: SymmetryType
	): Set<Cell> {
		val bad = mutableSetOf<Cell>()

		for (cell in pattern.keys) {
			val orbit = type.orbitOf(cell, size)
			val distinctColors = orbit.map { pattern[it] }.toSet()
			// More than one colour inside an orbit means the symmetry is broken.
			if (distinctColors.size > 1) bad += orbit
		}
		return bad
	}

	fun isSymmetric(pattern: Map<Cell, PaintColor?>, type: SymmetryType): Boolean =
		invalidCells(pattern, type).isEmpty()

	fun isSolved(pattern: Map<Cell, PaintColor?>, type: SymmetryType): Boolean =
		pattern.size == size * size &&
			pattern.values.none { it == null } &&
			isSymmetric(pattern, type)
}
