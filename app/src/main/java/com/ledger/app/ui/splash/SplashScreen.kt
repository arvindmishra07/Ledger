package com.ledger.app.ui.splash


import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ledger.app.ui.theme.BackgroundDark
import com.ledger.app.ui.theme.EmeraldGreen
import com.ledger.app.ui.theme.TealAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val bar1 = remember { Animatable(0f) }
    val bar2 = remember { Animatable(0f) }
    val bar3 = remember { Animatable(0f) }
    val lineProgress = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }
    val logoScale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        bar1.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        bar2.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        bar3.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        lineProgress.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        textAlpha.animateTo(1f, animationSpec = tween(400))

        delay(500)

        exitAlpha.animateTo(0f, animationSpec = tween(350))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .alpha(exitAlpha.value),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale.value)
            ) {
                val w = size.width
                val h = size.height
                val barWidth = w * 0.11f

                // Bar 1 (shortest)
                drawRoundRectBar(
                    x = w * 0.28f,
                    bottom = h * 0.70f,
                    width = barWidth,
                    fullHeight = h * 0.15f,
                    progress = bar1.value,
                    color = Color(0xFF00A87D)
                )
                // Bar 2 (medium)
                drawRoundRectBar(
                    x = w * 0.44f,
                    bottom = h * 0.70f,
                    width = barWidth,
                    fullHeight = h * 0.28f,
                    progress = bar2.value,
                    color = EmeraldGreen
                )
                // Bar 3 (tallest)
                drawRoundRectBar(
                    x = w * 0.60f,
                    bottom = h * 0.70f,
                    width = barWidth,
                    fullHeight = h * 0.43f,
                    progress = bar3.value,
                    color = TealAccent
                )

                // Trend line arcing above the bars
                if (lineProgress.value > 0f) {
                    val startPoint = Offset(w * 0.28f, h * 0.52f)
                    val midPoint = Offset(w * 0.50f, h * 0.36f)
                    val endPoint = Offset(w * 0.72f, h * 0.20f)

                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(startPoint.x, startPoint.y)
                        quadraticBezierTo(midPoint.x, midPoint.y, endPoint.x, endPoint.y)
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFFF5F7FA),
                        style = Stroke(
                            width = 6f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(400f, 400f),
                                phase = 400f - (400f * lineProgress.value)
                            )
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Ledger",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFF5F7FA),
                modifier = Modifier.alpha(textAlpha.value)
            )
            Text(
                text = "Track every rupee",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9BA5B4),
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoundRectBar(
    x: Float,
    bottom: Float,
    width: Float,
    fullHeight: Float,
    progress: Float,
    color: Color
) {
    val animatedHeight = fullHeight * progress
    drawRoundRect(
        color = color,
        topLeft = Offset(x, bottom - animatedHeight),
        size = androidx.compose.ui.geometry.Size(width, animatedHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.25f, width * 0.25f)
    )
}