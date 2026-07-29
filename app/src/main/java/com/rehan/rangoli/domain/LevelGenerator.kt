package com.rehan.rangoli.domain

import kotlin.random.Random

/**
 * Builds a level by colouring symmetry orbits.
 *
 * This is the key trick of the whole project: pick one colour per orbit and the
 * resulting pattern *cannot* be asymmetric. Then hide the region outside the
 * fundamental domain and derive the palette from what was hidden. Every level
 * is solvable with zero verification work.
 */
class LevelGenerator(private val random: Random = Random.Default) {

	fun generate(spec: LevelSpec): Level {
		require(spec.size % 2 == 1) { "Grid size must be odd, was ${spec.size}" }
		require(spec.colors.isNotEmpty()) { "Level needs at least one colour" }

		val size = spec.size
		val allCells = (0 until size).flatMap { row ->
			(0 until size).map { col -> Cell(row, col) }
		}

		// 1. Group cells into orbits - one orbit is one independent colour choice.
		val orbits = allCells.map { spec.symmetry.orbitOf(it, size) }.distinct()

		// 2. Colour each orbit. Symmetry is now guaranteed.
		val solution = mutableMapOf<Cell, PaintColor>()
		for (orbit in orbits) {
			val color = spec.colors[random.nextInt(spec.colors.size)]
			orbit.forEach { solution[it] = color }
		}

		// 3. Reveal the fundamental domain, hide the rest.
		val given = spec.symmetry.fundamentalDomain(size)
		val hidden = allCells.filterNot { it in given }.toSet()

		// 4. The palette is exactly what the hidden cells need, plus any spares.
		val palette = spec.colors
			.associateWith { color ->
				hidden.count { solution[it] == color } + spec.spareTiles
			}
			.filterValues { it > 0 }

		return Level(
			index = spec.index,
			size = size,
			symmetry = spec.symmetry,
			solution = solution,
			givenCells = solution.filterKeys { it in given },
			hiddenCells = hidden,
			palette = palette
		)
	}
}
