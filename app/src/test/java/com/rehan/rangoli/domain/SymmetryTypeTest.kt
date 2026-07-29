package com.rehan.rangoli.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SymmetryTypeTest {

	private val sizes = listOf(5, 7, 9, 11)

	@Test
	fun `vertical mirror pairs opposite columns`() {
		val orbit = SymmetryType.VERTICAL_MIRROR.orbitOf(Cell(1, 0), size = 5)
		assertEquals(setOf(Cell(1, 0), Cell(1, 4)), orbit)
	}

	@Test
	fun `horizontal mirror pairs opposite rows`() {
		val orbit = SymmetryType.HORIZONTAL_MIRROR.orbitOf(Cell(0, 2), size = 5)
		assertEquals(setOf(Cell(0, 2), Cell(4, 2)), orbit)
	}

	@Test
	fun `diagonal mirror swaps row and column`() {
		val orbit = SymmetryType.DIAGONAL_MIRROR.orbitOf(Cell(3, 1), size = 5)
		assertEquals(setOf(Cell(3, 1), Cell(1, 3)), orbit)
	}

	@Test
	fun `centre cell of an odd grid is its own only partner`() {
		for (size in sizes) {
			val mid = (size - 1) / 2
			for (type in SymmetryType.entries) {
				assertEquals(
					"$type on ${size}x$size",
					setOf(Cell(mid, mid)),
					type.orbitOf(Cell(mid, mid), size)
				)
			}
		}
	}

	@Test
	fun `rotational orbit has four distinct cells`() {
		val orbit = SymmetryType.ROTATIONAL_90.orbitOf(Cell(0, 1), size = 5)
		assertEquals(4, orbit.size)
		assertTrue(Cell(0, 1) in orbit)
	}

	@Test
	fun `eight fold orbit has eight distinct cells for a generic cell`() {
		val orbit = SymmetryType.EIGHT_FOLD_RADIAL.orbitOf(Cell(0, 1), size = 5)
		assertEquals(8, orbit.size)
	}

	@Test
	fun `rotational symmetry is not the same as mirror symmetry`() {
		val rotational = SymmetryType.ROTATIONAL_90.orbitOf(Cell(0, 1), size = 5)
		val mirrored = SymmetryType.VERTICAL_MIRROR.orbitOf(Cell(0, 1), size = 5)
		assertTrue(rotational != mirrored)
	}

	@Test
	fun `orbits partition the board`() {
		for (size in sizes) {
			for (type in SymmetryType.entries) {
				val cells = allCells(size)
				val orbits = cells.map { type.orbitOf(it, size) }.distinct()
				val covered = orbits.flatten().toSet()

				assertEquals("$type on ${size}x$size", cells.toSet(), covered)
				assertEquals(
					"$type orbits overlap on ${size}x$size",
					cells.size,
					orbits.sumOf { it.size }
				)
			}
		}
	}

	@Test
	fun `fundamental domain contains a member of every orbit`() {
		for (size in sizes) {
			for (type in SymmetryType.entries) {
				val domain = type.fundamentalDomain(size)
				for (cell in allCells(size)) {
					val orbit = type.orbitOf(cell, size)
					assertTrue(
						"$type on ${size}x$size has an undeducible orbit at $cell",
						orbit.any { it in domain }
					)
				}
			}
		}
	}

	private fun allCells(size: Int): List<Cell> =
		(0 until size).flatMap { row -> (0 until size).map { col -> Cell(row, col) } }
}
