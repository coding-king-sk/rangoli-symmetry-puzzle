package com.rehan.rangoli.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelGeneratorTest {

	@Test
	fun `generated solution is always symmetric`() {
		for (size in listOf(5, 7, 9, 11)) {
			val validator = SymmetryValidator(size)
			for (type in SymmetryType.entries) {
				repeat(25) { seed ->
					val level = LevelGenerator(Random(seed.toLong())).generate(
						spec(size = size, symmetry = type)
					)
					assertTrue(
						"$type on ${size}x$size seed $seed",
						validator.isSolved(level.solution, type)
					)
				}
			}
		}
	}

	@Test
	fun `given and hidden cells cover the whole board exactly once`() {
		for (type in SymmetryType.entries) {
			val level = LevelGenerator(Random(42)).generate(spec(size = 7, symmetry = type))

			assertEquals(7 * 7, level.solution.size)
			assertEquals(7 * 7, level.givenCells.size + level.hiddenCells.size)
			assertTrue(level.givenCells.keys.none { it in level.hiddenCells })
		}
	}

	@Test
	fun `every hidden cell is deducible from a revealed partner`() {
		for (type in SymmetryType.entries) {
			val level = LevelGenerator(Random(7)).generate(spec(size = 9, symmetry = type))

			for (cell in level.hiddenCells) {
				val orbit = type.orbitOf(cell, level.size)
				assertTrue(
					"$type: $cell has no revealed partner",
					orbit.any { it in level.givenCells }
				)
			}
		}
	}

	@Test
	fun `strict palette exactly covers the hidden cells`() {
		for (type in SymmetryType.entries) {
			val level = LevelGenerator(Random(11)).generate(
				spec(size = 7, symmetry = type, spareTiles = 0)
			)
			assertEquals(
				"$type palette mismatch",
				level.hiddenCells.size,
				level.palette.values.sum()
			)
		}
	}

	@Test
	fun `palette counts match the solution colours`() {
		val level = LevelGenerator(Random(3)).generate(
			spec(size = 9, symmetry = SymmetryType.FOUR_FOLD_REFLECTION, spareTiles = 0)
		)

		for ((color, count) in level.palette) {
			val needed = level.hiddenCells.count { level.solution[it] == color }
			assertEquals(color.name, needed, count)
		}
	}

	@Test
	fun `generation is deterministic for a given seed`() {
		val first = LevelGenerator(Random(99)).generate(spec())
		val second = LevelGenerator(Random(99)).generate(spec())
		assertEquals(first.solution, second.solution)
	}

	private fun spec(
		size: Int = 7,
		symmetry: SymmetryType = SymmetryType.VERTICAL_MIRROR,
		spareTiles: Int = 0
	) = LevelSpec(
		index = 0,
		size = size,
		symmetry = symmetry,
		colors = PaintColor.values().take(3),
		spareTiles = spareTiles
	)
}
