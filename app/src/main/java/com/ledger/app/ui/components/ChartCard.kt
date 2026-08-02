package com.ledger.app.ui.components


import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ledger.app.domain.model.CategorySpend
import com.ledger.app.ui.theme.CardShape
import kotlinx.coroutines.launch

/**
 * Animated donut/pie chart showing category breakdown.
 * Pure Canvas implementation — no external chart library needed.
 */
@Composable
fun CategoryPieChart(
    data: List<CategorySpend>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 34f
) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(900))
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (data.isEmpty()) return@Canvas
            val total = data.sumOf { it.total }.toFloat()
            if (total <= 0f) return@Canvas

            var startAngle = -90f
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )

            data.forEach { spend ->
                val sweep = (spend.total.toFloat() / total) * 360f * animatedProgress.value
                val color = runCatching { Color(android.graphics.Color.parseColor(spend.category.colorHex)) }
                    .getOrDefault(Color.Gray)
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = androidx.compose.ui.geometry.Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
                )
                startAngle += (spend.total.toFloat() / total) * 360f
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${data.size}",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "categories",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Card wrapper for any chart content with a title. */
@Composable
fun ChartCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

/** Simple animated bar chart for daily trend / monthly comparison. */
@Composable
fun BarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    val maxValue = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val animatedValues = remember(values) { values.map { Animatable(0f) } }

    LaunchedEffect(values) {
        animatedValues.forEachIndexed { index, anim ->
            launch {
                anim.animateTo(
                    values.getOrElse(index) { 0f },
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 600,
                        delayMillis = index * 40
                    )
                )
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        animatedValues.forEachIndexed { index, anim ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .fillMaxHeight(fraction = (anim.value / maxValue).coerceIn(0f, 1f))
                        .clip(MaterialTheme.shapes.small)
                        .background(barColor)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = labels.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}