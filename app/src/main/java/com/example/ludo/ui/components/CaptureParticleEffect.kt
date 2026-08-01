package com.example.ludo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ludo.engine.GridCoord
import com.example.ludo.model.PlayerColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ExplosionParticle(
    val angle: Double,
    val speed: Float,
    val initialRadius: Float,
    val color: Color,
    val isStar: Boolean,
    val spinSpeed: Float
)

@Composable
fun CaptureParticleEffect(
    gridCoord: GridCoord,
    color: PlayerColor,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            color.color,
            Color(0xFFFFD700), // Gold
            Color(0xFFFFFFFF), // White
            Color(0xFFFF6D00), // Amber Spark
            color.color.copy(alpha = 0.9f)
        )
        List(36) { i ->
            ExplosionParticle(
                angle = Random.nextDouble(0.0, 2 * PI),
                speed = Random.nextFloat() * 180f + 60f,
                initialRadius = Random.nextFloat() * 7f + 3.5f,
                color = colors[i % colors.size],
                isStar = (i % 4 == 0),
                spinSpeed = Random.nextFloat() * 10f - 5f
            )
        }
    }

    LaunchedEffect(gridCoord) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    if (progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val boardSize = size.width
            val cellSize = boardSize / 15f

            val center = Offset(
                x = (gridCoord.x + 0.5f) * cellSize,
                y = (gridCoord.y + 0.5f) * cellSize
            )

            val p = progress.value
            val alpha = (1f - p).coerceIn(0f, 1f)

            // 1. Golden Flash Flare at Center
            val flashAlpha = if (p < 0.2f) (p / 0.2f) else ((1f - p) / 0.8f).coerceIn(0f, 1f)
            val flashRadius = (p * 2.8f + 0.5f) * cellSize

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (flashAlpha * 0.95f).coerceIn(0f, 1f)),
                        Color(0xFFFFD700).copy(alpha = (flashAlpha * 0.85f).coerceIn(0f, 1f)),
                        color.color.copy(alpha = (flashAlpha * 0.5f).coerceIn(0f, 1f)),
                        Color.Transparent
                    ),
                    center = center,
                    radius = flashRadius
                ),
                radius = flashRadius,
                center = center
            )

            // 2. Triple Shockwave Rings
            // Outer Player-Color Shockwave
            val shock1Radius = p * cellSize * 3.2f
            drawCircle(
                color = color.color.copy(alpha = alpha * 0.9f),
                radius = shock1Radius,
                center = center,
                style = Stroke(width = (1f - p) * 14f)
            )

            // Middle Gold Shockwave
            val shock2Radius = p * cellSize * 2.2f
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = alpha),
                radius = shock2Radius,
                center = center,
                style = Stroke(width = (1f - p) * 8f)
            )

            // Inner Core Hot White Ring
            val shock3Radius = p * cellSize * 1.2f
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = shock3Radius,
                center = center,
                style = Stroke(width = (1f - p) * 5f)
            )

            // 3. Particle Explosion Dynamics
            particles.forEach { pt ->
                val dist = pt.speed * p
                val px = (center.x + cos(pt.angle) * dist).toFloat()
                val py = (center.y + sin(pt.angle) * dist + (p * p * 30f)).toFloat() // slight gravity pull

                val ptRadius = pt.initialRadius * (1f - p * 0.6f)
                val ptAlpha = alpha.coerceIn(0f, 1f)

                if (pt.isStar) {
                    // Draw 4-point Diamond Sparkle
                    val rotAngle = p * pt.spinSpeed
                    val starPath = Path().apply {
                        val r1 = ptRadius * 1.8f
                        val r2 = ptRadius * 0.5f
                        for (k in 0 until 8) {
                            val r = if (k % 2 == 0) r1 else r2
                            val a = rotAngle + (k * PI / 4)
                            val x = (px + cos(a) * r).toFloat()
                            val y = (py + sin(a) * r).toFloat()
                            if (k == 0) moveTo(x, y) else lineTo(x, y)
                        }
                        close()
                    }
                    drawPath(starPath, color = pt.color.copy(alpha = ptAlpha))
                } else {
                    // Round Sparkle with Glow
                    drawCircle(
                        color = pt.color.copy(alpha = ptAlpha),
                        radius = ptRadius,
                        center = Offset(px, py)
                    )
                    // Core highlight
                    drawCircle(
                        color = Color.White.copy(alpha = (ptAlpha * 0.8f).coerceIn(0f, 1f)),
                        radius = ptRadius * 0.4f,
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}
