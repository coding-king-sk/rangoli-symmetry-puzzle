package com.rehan.rangoli.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelCatalogTest {

	@Test
	fun `chapters cover every level index exactly once`() {
		val covered = LevelCatalog.chapters.flatMap { it.range.toList() }
		assertEquals(LevelCatalog.TOTAL, covered.size)
		assertEquals((0 until LevelCatalog.TOTAL).toSet(), covered.toSet())
	}

	@Test
	fun `all shipped levels are generated and solvable`() {
		for (index in 0 until LevelCatalog.TOTAL) {
			val level = LevelCatalog.level(index)
			val validator = SymmetryValidator(level.size)

			assertTrue("Level $index is empty", level.hiddenCells.isNotEmpty())
			assertTrue(
				"Level $index solution is not symmetric",
				validator.isSolved(level.solution, level.symmetry)
			)
			assertTrue(
				"Level $index palette cannot fill the board",
				level.palette.values.sum() >= level.hiddenCells.size
			)
		}
	}

	@Test
	fun `levels use odd grids only`() {
		for (index in 0 until LevelCatalog.TOTAL) {
			assertEquals(1, LevelCatalog.level(index).size % 2)
		}
	}

	@Test
	fun `each level matches its chapter symmetry`() {
		for (index in 0 until LevelCatalog.TOTAL) {
			assertEquals(
				LevelCatalog.chapterOf(index).symmetry,
				LevelCatalog.level(index).symmetry
			)
		}
	}
}
