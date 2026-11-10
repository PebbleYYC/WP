package ca.pebble.watchpatrol.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import ca.pebble.watchpatrol.domain.model.HistogramBucket
import ca.pebble.watchpatrol.domain.model.PricePoint

/** Area + line chart of a model's median price over time. */
@Composable
fun PriceHistoryChart(
    points: List<PricePoint>,
    modifier: Modifier = Modifier,
) {
    val line = MaterialTheme.colorScheme.primary
    val fillTop = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    val fillBottom = MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)
    val grid = MaterialTheme.colorScheme.outlineVariant

    Box(modifier = modifier.fillMaxWidth().height(160.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            if (points.size < 2) return@Canvas
            val values = points.map { it.value }
            val minV = values.min()
            val maxV = values.max()
            val span = (maxV - minV).takeIf { it > 0.0 } ?: 1.0
            val w = size.width
            val h = size.height
            val padBottom = 8f
            val usableH = h - padBottom

            // Baseline grid line.
            drawLine(
                color = grid,
                start = Offset(0f, usableH),
                end = Offset(w, usableH),
                strokeWidth = 1f,
            )

            fun x(i: Int) = w * (i.toFloat() / (points.size - 1))
            fun y(v: Double) = (usableH * (1f - ((v - minV) / span).toFloat())).coerceIn(0f, usableH)

            val linePath = Path().apply {
                moveTo(x(0), y(values[0]))
                for (i in 1 until points.size) lineTo(x(i), y(values[i]))
            }
            val areaPath = Path().apply {
                addPath(linePath)
                lineTo(x(points.size - 1), usableH)
                lineTo(x(0), usableH)
                close()
            }
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(listOf(fillTop, fillBottom)),
            )
            drawPath(path = linePath, color = line, style = strokeStyle())

            // End marker.
            drawCircle(
                color = line,
                radius = 4f,
                center = Offset(x(points.size - 1), y(values.last())),
            )
        }
    }
}

private fun strokeStyle() =
    androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)

/** Vertical bars of a price distribution histogram. */
@Composable
fun PriceHistogram(
    buckets: List<HistogramBucket>,
    modifier: Modifier = Modifier,
) {
    val bar = MaterialTheme.colorScheme.primary
    val barMuted = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)

    Box(modifier = modifier.fillMaxWidth().height(120.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            if (buckets.isEmpty()) return@Canvas
            val maxCount = buckets.maxOf { it.count }.coerceAtLeast(1)
            val gap = 4f
            val barWidth = (size.width - gap * (buckets.size - 1)) / buckets.size
            buckets.forEachIndexed { index, bucket ->
                val barHeight = size.height * (bucket.count.toFloat() / maxCount)
                val left = index * (barWidth + gap)
                drawBar(
                    left = left,
                    top = size.height - barHeight,
                    width = barWidth,
                    height = barHeight,
                    color = if (bucket.count == maxCount) bar else barMuted,
                )
            }
        }
    }
}

private fun DrawScope.drawBar(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    color: androidx.compose.ui.graphics.Color,
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = androidx.compose.ui.geometry.Size(width, height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
    )
}
