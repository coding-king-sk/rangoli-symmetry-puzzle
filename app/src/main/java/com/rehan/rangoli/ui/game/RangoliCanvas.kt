package com.rehan.rangoli.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.rehan.rangoli.domain.Cell
import com.rehan.rangoli.domain.Level
import com.rehan.rangoli.domain.SymmetryType

/**
 * Draws the rangoli board.
 *
 * The dashed symmetry axis is drawn first and on purpose: without it players
 * have to guess what the puzzle is even asking for.
 */
@Composable
fun RangoliCanvas(
	level: Level,
	pattern: Map<Cell, com.rehan.rangoli.domain.PaintColor>,
	wrongCells: Set<Cell>,
	solved: Boolean,
	onCellTap: (Cell) -> Unit,
	modifier: Modifier = Modifier
) {
	val gridSize = level.size
	val axisColor = MaterialTheme.colorScheme.primary
	val glow by animateFloatAsState(
		targetValue = if (solved) 1f else 0f,
		animationSpec = tween(durationMillis = 700),
		label = "solveGlow"
	)

	Canvas(
		modifier = modifier
			.aspectRatio(1f)
			.pointerInput(gridSize) {
				detectTapGestures { offset ->
					val cellPx = size.width / gridSize.toFloat()
					val col = (offset.x / cellPx).toInt().coerceIn(0, gridSize - 1)
					val row = (offset.y / cellPx).toInt().coerceIn(0, gridSize - 1)
					onCellTap(Cell(row, col))
				}
			}
	) {
		val cellPx = this.size.width / gridSize
		val inset = cellPx * 0.06f
		val dotRadius = cellPx * 0.07f

		if (glow > 0f) {
			drawCircle(
				color = axisColor.copy(alpha = 0.14f * glow),
				radius = this.size.width * 0.62f
			)
		}

		drawSymmetryAxis(level.symmetry, axisColor)

		for (row in 0 until gridSize) {
			for (col in 0 until gridSize) {
				val cell = Cell(row, col)
				val topLeft = Offset(col * cellPx, row * cellPx)
				val color = pattern[cell]

				if (color != null) {
					drawRoundRect(
						color = Color(color.argb),
						topLeft = topLeft + Offset(inset, inset),
						size = Size(cellPx - inset * 2, cellPx - inset * 2),
						cornerRadius = CornerRadius(cellPx * 0.22f)
					)

					// Player-placed tiles get a faint outline so the clue half of
					// the board stays readable.
					if (cell in level.hiddenCells) {
						drawRoundRect(
							color = Color.White.copy(alpha = 0.35f),
							topLeft = topLeft + Offset(inset, inset),
							size = Size(cellPx - inset * 2, cellPx - inset * 2),
							cornerRadius = CornerRadius(cellPx * 0.22f),
							style = Stroke(width = cellPx * 0.04f)
						)
					}
				} else {
					// Empty cell: a kolam dot, not a grid line.
					drawCircle(
						color = Color.White.copy(alpha = 0.22f),
						radius = dotRadius,
						center = topLeft + Offset(cellPx / 2f, cellPx / 2f)
					)
				}

				if (cell in wrongCells) {
					drawRoundRect(
						color = Color(0xFFFF5252),
						topLeft = topLeft,
						size = Size(cellPx, cellPx),
						cornerRadius = CornerRadius(cellPx * 0.22f),
						style = Stroke(width = cellPx * 0.07f)
					)
				}
			}
		}
	}
}

private fun DrawScope.drawSymmetryAxis(symmetry: SymmetryType, color: Color) {
	val width = size.width
	val height = size.height
	val axis = color.copy(alpha = 0.40f)
	val strokeWidth = 1.5.dp.toPx()
	val dash = PathEffect.dashPathEffect(floatArrayOf(14f, 16f))

	fun line(start: Offset, end: Offset) = drawLine(
		color = axis,
		start = start,
		end = end,
		strokeWidth = strokeWidth,
		pathEffect = dash
	)

	val vertical = { line(Offset(width / 2f, 0f), Offset(width / 2f, height)) }
	val horizontal = { line(Offset(0f, height / 2f), Offset(width, height / 2f)) }
	val mainDiagonal = { line(Offset(0f, 0f), Offset(width, height)) }
	val antiDiagonal = { line(Offset(width, 0f), Offset(0f, height)) }

	when (symmetry) {
		SymmetryType.VERTICAL_MIRROR -> vertical()
		SymmetryType.HORIZONTAL_MIRROR -> horizontal()
		SymmetryType.DIAGONAL_MIRROR -> mainDiagonal()
		SymmetryType.FOUR_FOLD_REFLECTION,
		SymmetryType.ROTATIONAL_90 -> {
			vertical()
			horizontal()
		}
		SymmetryType.EIGHT_FOLD_RADIAL -> {
			vertical()
			horizontal()
			mainDiagonal()
			antiDiagonal()
		}
	}
}
