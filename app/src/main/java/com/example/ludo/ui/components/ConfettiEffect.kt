package com.example.ludo.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val initialX: Float,
    val speedY: Float,
    val speedX: Float,
    val color: Color,
    val size: Float,
    val phase: Float
)

private data class FireworkBurst(
    val centerX: Float,
    val centerY: Float,
    val color: Color,
    val particles: List<FireworkSpark>
)

private data class FireworkSpark(
    val angle: Double,
    val speed: Float,
    val radius: Float
)

@Composable
fun ConfettiEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "victory_effects")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val confetti = remember {
        val colors = listOf(
            Color(0xFFE53935), Color(0xFF4CAF50), Color(0xFFFBC02D),
            Color(0xFF1E88E5), Color(0xFFFFD700), Color(0xFFE91E63),
            Color(0xFF00E5FF), Color(0xFF7C4DFF)
        )
        List(90) {
            ConfettiParticle(
                initialX = Random.nextFloat(),
                speedY = Random.nextFloat() * 0.7f + 0.5f,
                speedX = Random.nextFloat() * 0.3f - 0.15f,
                color = colors[Random.nextInt(colors.size)],
                size = Random.nextFloat() * 14f + 8f,
                phase = Random.nextFloat() * 2f * PI.toFloat()
            )
        }
    }

    val fireworks = remember {
        val colors = listOf(
            Color(0xFFFFD700), Color(0xFFFF1744), Color(0xFF00E676),
            Color(0xFF00B0FF), Color(0xFFE040FB)
        )
        List(5) {
            val cx = Random.nextFloat() * 0.8f + 0.1f
            val cy = Random.nextFloat() * 0.4f + 0.1f
            val burstColor = colors[it % colors.size]
            FireworkBurst(
                centerX = cx,
                centerY = cy,
                color = burstColor,
                particles = List(28) {
                    FireworkSpark(
                        angle = Random.nextDouble(0.0, 2 * PI),
                        speed = Random.nextFloat() * 150f + 50f,
                        radius = Random.nextFloat() * 5f + 3f
                    )
                }
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Draw Fireworks Bursts
        fireworks.forEachIndexed { idx, fw ->
            val fireworkPhase = ((progress + idx * 0.2f) % 1.0f)
            val alpha = (1f - fireworkPhase).coerceIn(0f, 1f)
            val center = Offset(fw.centerX * width, fw.centerY * height)

            drawCircle(
                color = fw.color.copy(alpha = alpha * 0.5f),
                radius = fireworkPhase * 160f,
                center = center,
                style = Stroke(width = (1f - fireworkPhase) * 6f)
            )

            fw.particles.forEach { spark ->
                val dist = spark.speed * fireworkPhase
                val sx = (center.x + cos(spark.angle) * dist).toFloat()
                val sy = (center.y + sin(spark.angle) * dist).toFloat()

                drawCircle(
                    color = fw.color.copy(alpha = alpha),
                    radius = spark.radius * (1f - fireworkPhase * 0.4f),
                    center = Offset(sx, sy)
                )
            }
        }

        // 2. Draw Falling Confetti Stream
        confetti.forEach { p ->
            val curY = ((p.phase + p.speedY * progress * 4f) % 1.2f) * height - height * 0.1f
            val curX = (p.initialX + sin(progress * 2 * PI.toFloat() + p.phase) * 0.08f) * width

            drawRect(
                color = p.color,
                topLeft = Offset(curX, curY),
                size = Size(p.size, p.size * 0.6f)
            )
        }
    }
}
