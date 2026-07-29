package com.rehan.rangoli.domain

import kotlin.random.Random

/**
 * The 100 shipped levels, grouped into six chapters by symmetry type.
 *
 * Levels are generated deterministically from the level index, so the same
 * level number is always the same puzzle on every device -- no level data files
 * to ship, and progress stays valid across updates.
 */
object LevelCatalog {

	const val TOTAL = 100

	private const val SEED_BASE = 20260729L

	data class Chapter(
		val title: String,
		val symmetry: SymmetryType,
		val range: IntRange
	)

	val chapters: List<Chapter> = listOf(
		Chapter("Aadha Aadha", SymmetryType.VERTICAL_MIRROR, 0..14),
		Chapter("Upar Neeche", SymmetryType.HORIZONTAL_MIRROR, 15..29),
		Chapter("Chaar Kone", SymmetryType.FOUR_FOLD_REFLECTION, 30..49),
		Chapter("Ghoomta Rangoli", SymmetryType.ROTATIONAL_90, 50..69),
		Chapter("Tirchi Rekha", SymmetryType.DIAGONAL_MIRROR, 70..84),
		Chapter("Aath Pankhudi", SymmetryType.EIGHT_FOLD_RADIAL, 85..99)
	)

	private val cache = mutableMapOf<Int, Level>()

	fun chapterOf(index: Int): Chapter =
		chapters.first { index in it.range }

	fun specFor(index: Int): LevelSpec {
		require(index in 0 until TOTAL) { "Level index out of range: $index" }

		val chapter = chapterOf(index)
		val span = chapter.range.last - chapter.range.first + 1
		val progress = (index - chapter.range.first).toFloat() / span

		val size = when {
			progress < 0.25f -> 5
			progress < 0.55f -> 7
			progress < 0.85f -> 9
			else -> 11
		}

		val colorCount = when {
			progress < 0.20f -> 2
			progress < 0.50f -> 3
			progress < 0.80f -> 4
			else -> 5
		}

		// Early levels are forgiving; later ones give an exact-fit palette.
		val spareTiles = if (progress < 0.30f) 1 else 0

		return LevelSpec(
			index = index,
			size = size,
			symmetry = chapter.symmetry,
			colors = PaintColor.values().take(colorCount),
			spareTiles = spareTiles
		)
	}

	fun level(index: Int): Level = cache.getOrPut(index) {
		LevelGenerator(Random(SEED_BASE + index * 7919L)).generate(specFor(index))
	}
}
