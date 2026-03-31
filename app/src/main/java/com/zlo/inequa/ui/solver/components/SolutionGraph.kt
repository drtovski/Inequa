package com.zlo.inequa.ui.solver.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.zlo.inequa.domain.solver.Interval
import kotlin.math.max
import kotlin.math.min

@Composable
fun SolutionGraph(
    intervals: List<Interval>,
    variableName: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val labelPaint = remember(colors.onSurfaceVariant, density) {
        Paint().apply {
            color = android.graphics.Color.argb(
                220,
                (colors.onSurfaceVariant.red * 255).toInt(),
                (colors.onSurfaceVariant.green * 255).toInt(),
                (colors.onSurfaceVariant.blue * 255).toInt()
            )
            textAlign = Paint.Align.CENTER
            textSize = with(density) { 12.dp.toPx() }
            isAntiAlias = true
        }
    }

    val finitePoints = intervals.flatMap { interval ->
        listOfNotNull(interval.start, interval.end)
    }
    val minPoint = finitePoints.minOrNull() ?: -5.0
    val maxPoint = finitePoints.maxOrNull() ?: 5.0
    val span = max(2.0, maxPoint - minPoint)
    val margin = max(1.0, span * 0.25)
    val rangeStart = minPoint - margin
    val rangeEnd = maxPoint + margin

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val leftPadding = 20.dp.toPx()
        val rightPadding = size.width - 20.dp.toPx()
        val centerY = size.height * 0.42f
        val radius = 6.dp.toPx()
        val lineWidth = 2.5.dp.toPx()
        val highlightWidth = 6.dp.toPx()

        fun mapX(value: Double): Float {
            val ratio = ((value - rangeStart) / (rangeEnd - rangeStart)).toFloat()
            return leftPadding + ratio * (rightPadding - leftPadding)
        }

        drawLine(
            color = colors.outlineVariant,
            start = Offset(leftPadding, centerY),
            end = Offset(rightPadding, centerY),
            strokeWidth = lineWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = colors.outlineVariant,
            start = Offset(rightPadding - 10.dp.toPx(), centerY - 5.dp.toPx()),
            end = Offset(rightPadding, centerY),
            strokeWidth = lineWidth
        )
        drawLine(
            color = colors.outlineVariant,
            start = Offset(rightPadding - 10.dp.toPx(), centerY + 5.dp.toPx()),
            end = Offset(rightPadding, centerY),
            strokeWidth = lineWidth
        )

        intervals.forEach { interval ->
            val startX = interval.start?.let(::mapX) ?: leftPadding
            val endX = interval.end?.let(::mapX) ?: rightPadding

            drawLine(
                color = colors.primary,
                start = Offset(startX, centerY),
                end = Offset(endX, centerY),
                strokeWidth = highlightWidth,
                cap = StrokeCap.Round
            )

            interval.start?.let { value ->
                drawEndpoint(
                    x = mapX(value),
                    y = centerY,
                    inclusive = interval.includeStart,
                    fillColor = colors.primary,
                    surfaceColor = colors.surface,
                    radius = radius
                )
                drawContext.canvas.nativeCanvas.drawText(
                    formatNumber(value),
                    mapX(value),
                    size.height - 6.dp.toPx(),
                    labelPaint
                )
            }

            interval.end?.let { value ->
                drawEndpoint(
                    x = mapX(value),
                    y = centerY,
                    inclusive = interval.includeEnd,
                    fillColor = colors.primary,
                    surfaceColor = colors.surface,
                    radius = radius
                )
                drawContext.canvas.nativeCanvas.drawText(
                    formatNumber(value),
                    mapX(value),
                    size.height - 6.dp.toPx(),
                    labelPaint
                )
            }
        }

        drawContext.canvas.nativeCanvas.drawText(
            variableName,
            leftPadding,
            14.dp.toPx(),
            labelPaint
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEndpoint(
    x: Float,
    y: Float,
    inclusive: Boolean,
    fillColor: Color,
    surfaceColor: Color,
    radius: Float
) {
    if (inclusive) {
        drawCircle(
            color = fillColor,
            radius = radius,
            center = Offset(x, y)
        )
    } else {
        drawCircle(
            color = surfaceColor,
            radius = radius,
            center = Offset(x, y)
        )
        drawCircle(
            color = fillColor,
            radius = radius,
            center = Offset(x, y),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

private fun formatNumber(value: Double): String {
    val rounded = value.toLong().toDouble()
    return if (kotlin.math.abs(value - rounded) < 1e-9) {
        rounded.toLong().toString()
    } else {
        value.toString().replace('.', ',')
    }
}
