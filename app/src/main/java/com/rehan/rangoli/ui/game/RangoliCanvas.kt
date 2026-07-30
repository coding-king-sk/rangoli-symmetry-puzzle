package com.rehan.rangoli.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.rehan.rangoli.domain.Cell
import com.rehan.rangoli.domain.Level
import com.rehan.rangoli.domain.PaintColor
import com.rehan.rangoli.domain.SymmetryType

/**
 * Board canvas with:
 *  - Dashed symmetry axis
 *  - Kolam dots for empty cells
 *  - Colour highlight (dim unrelated cells)
 *  - Scale-punch animation on the last placed cell
 *  - Horizontal shake on wrong cells
 *  - Solve glow
 */
@Composable
fun RangoliCanvas(
    level: Level,
    pattern: Map<Cell, PaintColor>,
    wrongCells: Set<Cell>,
    solved: Boolean,
    lastPlacedCell: Cell?,
    highlightColor: PaintColor?,
    shakeOffsetPx: Float,
    onCellTap: (Cell) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridSize = level.size
    val axisColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    // Solve glow
    val glow by animateFloatAsState(
        targetValue = if (solved) 1f else 0f,
        animationSpec = tween(700),
        label = "solveGlow"
    )

    // Scale-punch for last placed cell
    val punchScale = remember { Animatable(1f) }
    LaunchedEffect(lastPlacedCell) {
        if (lastPlacedCell != null) {
            punchScale.snapTo(1.28f)
            punchScale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            )
        }
    }

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
        val inset  = cellPx * 0.07f
        val radius = cellPx * 0.22f
        val dot    = cellPx * 0.08f

        // Solve glow background
        if (glow > 0f) {
            drawCircle(
                color  = axisColor.copy(alpha = 0.13f * glow),
                radius = this.size.width * 0.65f
            )
        }

        drawSymmetryAxis(level.symmetry, axisColor)

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val cell    = Cell(row, col)
                val topLeft = Offset(col * cellPx, row * cellPx)
                val center  = topLeft + Offset(cellPx / 2f, cellPx / 2f)
                val color   = pattern[cell]

                val isWrong     = cell in wrongCells
                val xOffset     = if (isWrong) shakeOffsetPx else 0f
                val isHighlit   = highlightColor == null || color == highlightColor ||
                    (cell in level.givenCells && level.givenCells[cell] == highlightColor)
                val dimAlpha    = if (!isHighlit) 0.18f else 1f
                val isLastPlace = cell == lastPlacedCell

                if (color != null) {
                    val cellColor = Color(color.argb).copy(alpha = dimAlpha)
                    val tl = topLeft + Offset(inset + xOffset, inset)
                    val sz = Size(cellPx - inset * 2, cellPx - inset * 2)
                    val cr = CornerRadius(radius)

                    if (isLastPlace && !isWrong) {
                        // Scale-punch on recently placed cell
                        val pivot = Offset(tl.x + sz.width / 2, tl.y + sz.height / 2)
                        scale(punchScale.value, pivot) {
                            drawRoundRect(color = cellColor, topLeft = tl, size = sz, cornerRadius = cr)
                        }
                    } else {
                        drawRoundRect(color = cellColor, topLeft = tl, size = sz, cornerRadius = cr)
                    }

                    // Subtle white stroke on player-placed cells
                    if (cell in level.hiddenCells) {
                        drawRoundRect(
                            color       = Color.White.copy(alpha = 0.30f * dimAlpha),
                            topLeft     = tl,
                            size        = sz,
                            cornerRadius = cr,
                            style       = Stroke(width = cellPx * 0.04f)
                        )
                    }
                } else {
                    // Empty: kolam dot, shifted when this cell is wrong-highlighted
                    drawCircle(
                        color  = Color.White.copy(alpha = 0.22f * dimAlpha),
                        radius = dot,
                        center = center + Offset(xOffset, 0f)
                    )
                }

                // Wrong-cell red ring
                if (isWrong) {
                    drawRoundRect(
                        color        = errorColor.copy(alpha = 0.88f),
                        topLeft      = topLeft + Offset(inset * 0.5f + xOffset, inset * 0.5f),
                        size         = Size(cellPx - inset, cellPx - inset),
                        cornerRadius = CornerRadius(radius),
                        style        = Stroke(width = cellPx * 0.08f)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawSymmetryAxis(symmetry: SymmetryType, color: Color) {
    val w = size.width
    val h = size.height
    val paint = color.copy(alpha = 0.38f)
    val sw    = 1.5.dp.toPx()
    val dash  = PathEffect.dashPathEffect(floatArrayOf(14f, 16f))

    fun line(s: Offset, e: Offset) = drawLine(color = paint, start = s, end = e,
        strokeWidth = sw, pathEffect = dash)

    val vert  = { line(Offset(w / 2f, 0f), Offset(w / 2f, h)) }
    val horiz = { line(Offset(0f, h / 2f), Offset(w, h / 2f)) }
    val diag  = { line(Offset(0f, 0f), Offset(w, h)) }
    val anti  = { line(Offset(w, 0f), Offset(0f, h)) }

    when (symmetry) {
        SymmetryType.VERTICAL_MIRROR    -> vert()
        SymmetryType.HORIZONTAL_MIRROR  -> horiz()
        SymmetryType.DIAGONAL_MIRROR    -> diag()
        SymmetryType.FOUR_FOLD_REFLECTION,
        SymmetryType.ROTATIONAL_90      -> { vert(); horiz() }
        SymmetryType.EIGHT_FOLD_RADIAL  -> { vert(); horiz(); diag(); anti() }
    }
}
