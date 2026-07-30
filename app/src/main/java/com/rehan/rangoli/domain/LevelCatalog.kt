package com.rehan.rangoli.domain

import kotlin.random.Random

object LevelCatalog {
	const val TOTAL = 100
	private const val SEED = 0x52616E67L // "Rang"

	data class Chapter(
		val title: String,
		val symmetry: SymmetryType,
		val range: IntRange
	)

	val chapters: List<Chapter> = listOf(
		Chapter("Aadha Aadha",     SymmetryType.VERTICAL_MIRROR,      0..14),
		Chapter("Upar Neeche",     SymmetryType.HORIZONTAL_MIRROR,    15..29),
		Chapter("Chaar Kone",      SymmetryType.FOUR_FOLD_REFLECTION, 30..49),
		Chapter("Ghoomta Rangoli", SymmetryType.ROTATIONAL_90,        50..69),
		Chapter("Tirchi Rekha",    SymmetryType.DIAGONAL_MIRROR,      70..84),
		Chapter("Aath Pankhudi",   SymmetryType.EIGHT_FOLD_RADIAL,    85..99),
	)

	fun chapterOf(index: Int): Chapter =
		chapters.first { index in it.range }

	fun specFor(index: Int): LevelSpec {
		val chapter = chapterOf(index)
		val progress = (index - chapter.range.first).toFloat() / chapter.range.count()
		val size = when {
			progress < 0.25f -> 5
			progress < 0.50f -> 7
			progress < 0.75f -> 9
			else             -> 11
		}
		val colorCount = when {
			progress < 0.25f -> 2
			progress < 0.50f -> 3
			progress < 0.75f -> 4
			else             -> 5
		}.coerceAtMost(PaintColor.values().size)
		val spare = if (progress < 0.3f) 1 else 0
		return LevelSpec(
			index      = index,
			size       = size,
			symmetry   = chapter.symmetry,
			colors     = PaintColor.values().take(colorCount),
			spareTiles = spare
		)
	}

	private val cache = HashMap<Int, Level>()

	fun level(index: Int): Level = cache.getOrPut(index) {
		LevelGenerator(Random(SEED + index * 7919L)).generate(specFor(index))
	}
}
