package com.rehan.rangoli.domain

/**
 * Traditional rangoli powder tones.
 *
 * [glyph] exists for accessibility: players who cannot rely on hue alone can
 * switch on shape markers and still tell the colours apart.
 */
enum class PaintColor(
	val argb: Long,
	val label: String,
	val glyph: String
) {
	HALDI(0xFFF2B233, "Haldi", "\u25CF"),
	SINDOOR(0xFFE2482F, "Sindoor", "\u25A0"),
	INDIGO(0xFF4A5BD6, "Indigo", "\u25B2"),
	MARIGOLD(0xFFF2731F, "Marigold", "\u25C6"),
	LEAF(0xFF3FA96B, "Leaf", "\u2605")
}
