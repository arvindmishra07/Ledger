package com.ledger.app.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SplashBackground = Color(0xFF16302A)
private val SplashWaveDeep = Color(0xFF1B3B2B)
private val SplashWaveMid = Color(0xFF224A37)
private val SplashMintPrimary = Color(0xFFC8F5D2)
private val SplashMintMuted = Color(0xFF8EC79E)
private val SplashGlowArrow = Color(0xFFEAFDF0)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val bookAlpha = remember { Animatable(0f) }
    val arrowProgress = remember { Animatable(0f) }
    val wordmarkAlpha = remember { Animatable(0f) }
    val sparkleAlpha = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }

    // Precise coordinates for ambient sparkles matching the design mockup
    val sparkles = remember {
        listOf(
            Pair(0.18f, 0.28f),
            Pair(0.82f, 0.24f),
            Pair(0.78f, 0.42f),
            Pair(0.22f, 0.65f),
            Pair(0.85f, 0.72f),
            Pair(0.15f, 0.82f)
        )
    }

    LaunchedEffect(Unit) {
        launch { bookAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
        delay(200)
        launch { arrowProgress.animateTo(1f, tween(750, easing = FastOutSlowInEasing)) }
        delay(200)
        launch { sparkleAlpha.animateTo(1f, tween(600)) }
        wordmarkAlpha.animateTo(1f, tween(450))

        delay(850)

        exitAlpha.animateTo(0f, tween(300))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
            .alpha(exitAlpha.value)
    ) {
        // Organic flowing background waves
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Top soft vignette shape
            val topWave = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.18f)
                quadraticBezierTo(w * 0.5f, h * 0.25f, 0f, h * 0.12f)
                close()
            }
            drawPath(topWave, color = SplashWaveDeep.copy(alpha = 0.55f))

            // Lower primary rolling wave
            val bottomWave1 = Path().apply {
                moveTo(0f, h * 0.62f)
                cubicTo(w * 0.25f, h * 0.54f, w * 0.65f, h * 0.68f, w, h * 0.60f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(bottomWave1, color = SplashWaveDeep.copy(alpha = 0.7f))

            // Lower secondary foreground wave
            val bottomWave2 = Path().apply {
                moveTo(0f, h * 0.76f)
                cubicTo(w * 0.35f, h * 0.70f, w * 0.70f, h * 0.84f, w, h * 0.75f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(bottomWave2, color = SplashWaveMid.copy(alpha = 0.5f))

            // Ambient sparkles
            sparkles.forEach { (fx, fy) ->
                val cx = w * fx
                val cy = h * fy
                drawSparkle(cx, cy, 7f * sparkleAlpha.value, SplashMintPrimary.copy(alpha = 0.65f * sparkleAlpha.value))
            }
        }

        // Center Content: Hand-drawn Book Icon, Wordmark, and Tagline
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .size(110.dp)
                    .alpha(bookAlpha.value)
            ) {
                val scale = size.width / 108f
                val baseStroke = 3.2f * scale
                val accentStroke = 1.8f * scale

                val strokeMain = Stroke(width = baseStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
                val strokeOuter = Stroke(width = accentStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)

                // Outer layered page contours
                val leftOuter = Path().apply {
                    moveTo(54f * scale, 41f * scale)
                    quadraticBezierTo(34f * scale, 42f * scale, 25f * scale, 50f * scale)
                    quadraticBezierTo(23f * scale, 67f * scale, 31f * scale, 76f * scale)
                    quadraticBezierTo(42f * scale, 80f * scale, 54f * scale, 77f * scale)
                }
                drawPath(leftOuter, color = SplashMintMuted, style = strokeOuter)

                val rightOuter = Path().apply {
                    moveTo(54f * scale, 41f * scale)
                    quadraticBezierTo(74f * scale, 42f * scale, 83f * scale, 50f * scale)
                    quadraticBezierTo(85f * scale, 67f * scale, 77f * scale, 76f * scale)
                    quadraticBezierTo(66f * scale, 80f * scale, 54f * scale, 77f * scale)
                }
                drawPath(rightOuter, color = SplashMintMuted, style = strokeOuter)

                // Main Book Wings
                val leftWing = Path().apply {
                    moveTo(54f * scale, 44f * scale)
                    quadraticBezierTo(36f * scale, 45f * scale, 28f * scale, 54f * scale)
                    quadraticBezierTo(27f * scale, 68f * scale, 35f * scale, 74f * scale)
                    quadraticBezierTo(44f * scale, 78f * scale, 54f * scale, 74f * scale)
                }
                drawPath(leftWing, color = SplashMintPrimary, style = strokeMain)

                val rightWing = Path().apply {
                    moveTo(54f * scale, 44f * scale)
                    quadraticBezierTo(72f * scale, 45f * scale, 80f * scale, 54f * scale)
                    quadraticBezierTo(81f * scale, 68f * scale, 73f * scale, 74f * scale)
                    quadraticBezierTo(64f * scale, 78f * scale, 54f * scale, 74f * scale)
                }
                drawPath(rightWing, color = SplashMintPrimary, style = strokeMain)

                // Book Center Spine
                drawLine(
                    color = SplashMintPrimary,
                    start = Offset(54f * scale, 44f * scale),
                    end = Offset(54f * scale, 74f * scale),
                    strokeWidth = baseStroke,
                    cap = StrokeCap.Round
                )

                // Animated Growth Loop Arrow
                if (arrowProgress.value > 0f) {
                    val loopPath = Path().apply {
                        moveTo(40f * scale, 73f * scale)
                        cubicTo(30f * scale, 62f * scale, 31f * scale, 44f * scale, 48f * scale, 39f * scale)
                        cubicTo(62f * scale, 35f * scale, 74f * scale, 45f * scale, 68f * scale, 59f * scale)
                        cubicTo(63f * scale, 70f * scale, 48f * scale, 70f * scale, 47f * scale, 56f * scale)
                        cubicTo(46f * scale, 45f * scale, 56f * scale, 33f * scale, 72f * scale, 28f * scale)
                    }

                    val pathLength = 160f * scale
                    drawPath(
                        path = loopPath,
                        color = SplashGlowArrow,
                        style = Stroke(
                            width = baseStroke + 0.5f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(pathLength, pathLength),
                                phase = pathLength * (1f - arrowProgress.value)
                            )
                        )
                    )

                    // Arrowhead reveals upon loop completion
                    if (arrowProgress.value > 0.85f) {
                        val headFade = ((arrowProgress.value - 0.85f) / 0.15f).coerceIn(0f, 1f)
                        val headColor = SplashGlowArrow.copy(alpha = headFade)
                        drawLine(headColor, Offset(64f * scale, 27f * scale), Offset(72f * scale, 28f * scale), baseStroke + 0.5f, StrokeCap.Round)
                        drawLine(headColor, Offset(72f * scale, 28f * scale), Offset(71f * scale, 36f * scale), baseStroke + 0.5f, StrokeCap.Round)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "ledger",
                color = SplashMintPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp,
                modifier = Modifier.alpha(wordmarkAlpha.value)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "your manual finance journal",
                color = SplashMintMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.2.sp,
                modifier = Modifier.alpha(wordmarkAlpha.value)
            )
        }
    }
}

private fun DrawScope.drawSparkle(cx: Float, cy: Float, size: Float, color: Color) {
    if (size <= 0f) return
    val path = Path().apply {
        moveTo(cx, cy - size)
        lineTo(cx + size * 0.25f, cy - size * 0.25f)
        lineTo(cx + size, cy)
        lineTo(cx + size * 0.25f, cy + size * 0.25f)
        lineTo(cx, cy + size)
        lineTo(cx - size * 0.25f, cy + size * 0.25f)
        lineTo(cx - size, cy)
        lineTo(cx - size * 0.25f, cy - size * 0.25f)
        close()
    }
    drawPath(path, color = color)
}