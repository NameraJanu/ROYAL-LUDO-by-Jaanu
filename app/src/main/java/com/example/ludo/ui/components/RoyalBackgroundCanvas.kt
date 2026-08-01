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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class RoyalParticle(
    val initialX: Float,
    val speedY: Float,
    val speedX: Float,
    val size: Float,
    val phase: Float,
    val type: ParticleType,
    val color: Color,
    val rotationSpeed: Float
)

private enum class ParticleType {
    SPARKLE,
    MINI_CROWN,
    GOLD_DOT,
    GLOW_ORB
}

@Composable
fun RoyalBackgroundCanvas(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "royal_bg")

    val bgProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgProgress"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraPulse"
    )

    val particles = remember {
        val colors = listOf(
            Color(0xFFFFD700),
            Color(0xFFFFE57F),
            Color(0xFFFF9100),
            Color(0xFF8E24AA),
            Color(0xFF2979FF)
        )
        List(40) { i ->
            val pType = when (i % 6) {
                0 -> ParticleType.MINI_CROWN
                1, 2 -> ParticleType.SPARKLE
                3, 4 -> ParticleType.GOLD_DOT
                else -> ParticleType.GLOW_ORB
            }
            RoyalParticle(
                initialX = Random.nextFloat(),
                speedY = Random.nextFloat() * 0.15f + 0.08f,
                speedX = Random.nextFloat() * 0.12f - 0.06f,
                size = when (pType) {
                    ParticleType.MINI_CROWN -> Random.nextFloat() * 12f + 14f
                    ParticleType.GLOW_ORB -> Random.nextFloat() * 24f + 20f
                    else -> Random.nextFloat() * 8f + 6f
                },
                phase = Random.nextFloat() * 2f * PI.toFloat(),
                type = pType,
                color = colors[i % colors.size],
                rotationSpeed = Random.nextFloat() * 4f - 2f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Shifting Royal Dark Background Gradient
        val topBgColor = Color(0xFF130E20)
        val centerBgColor = Color(0xFF231938)
        val bottomBgColor = Color(0xFF0F0B1A)

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(topBgColor, centerBgColor, bottomBgColor)
            )
        )

        // 2. Pulsing Radial Golden & Purple Aura Glows
        val auraCenterX = w * 0.5f + cos(bgProgress * 2 * PI).toFloat() * 60f
        val auraCenterY = h * 0.35f + sin(bgProgress * 2 * PI).toFloat() * 40f
        val auraRadius = (w * 0.75f) * auraPulse

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFD700).copy(alpha = 0.18f),
                    Color(0xFF6A1B9A).copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = Offset(auraCenterX, auraCenterY),
                radius = auraRadius
            ),
            radius = auraRadius,
            center = Offset(auraCenterX, auraCenterY)
        )

        // Secondary Bottom Blue Accent Aura
        val blueAuraRadius = (w * 0.6f) * (2f - auraPulse * 0.8f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF2979FF).copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.85f),
                radius = blueAuraRadius
            ),
            radius = blueAuraRadius,
            center = Offset(w * 0.5f, h * 0.85f)
        )

        // 3. Floating Royal Particles Loop
        particles.forEach { p ->
            val curY = ((p.phase + p.speedY * bgProgress * 4f) % 1.2f) * h - h * 0.1f
            // Invert logic so particles float upwards
            val upwardY = h - curY
            val curX = (p.initialX + sin(bgProgress * 2 * PI.toFloat() + p.phase) * 0.08f) * w

            val pAlpha = (sin(p.phase + bgProgress * 4f) * 0.35f + 0.65f).coerceIn(0.15f, 0.95f)

            when (p.type) {
                ParticleType.MINI_CROWN -> {
                    rotate(bgProgress * 360f * p.rotationSpeed, pivot = Offset(curX, upwardY)) {
                        val crownWidth = p.size
                        val crownHeight = p.size * 0.7f
                        val crownPath = Path().apply {
                            moveTo(curX - crownWidth / 2f, upwardY + crownHeight / 2f)
                            lineTo(curX - crownWidth / 2f, upwardY - crownHeight / 2f + 4f)
                            lineTo(curX - crownWidth * 0.25f, upwardY)
                            lineTo(curX, upwardY - crownHeight / 2f)
                            lineTo(curX + crownWidth * 0.25f, upwardY)
                            lineTo(curX + crownWidth / 2f, upwardY - crownHeight / 2f + 4f)
                            lineTo(curX + crownWidth / 2f, upwardY + crownHeight / 2f)
                            close()
                        }
                        drawPath(crownPath, color = Color(0xFFFFD700).copy(alpha = pAlpha * 0.75f))
                    }
                }
                ParticleType.SPARKLE -> {
                    rotate(bgProgress * 180f * p.rotationSpeed, pivot = Offset(curX, upwardY)) {
                        val r1 = p.size
                        val r2 = p.size * 0.35f
                        val sparklePath = Path()
                        for (k in 0 until 8) {
                            val r = if (k % 2 == 0) r1 else r2
                            val a = (k * PI / 4)
                            val sx = (curX + cos(a) * r).toFloat()
                            val sy = (upwardY + sin(a) * r).toFloat()
                            if (k == 0) sparklePath.moveTo(sx, sy) else sparklePath.lineTo(sx, sy)
                        }
                        sparklePath.close()
                        drawPath(sparklePath, color = p.color.copy(alpha = pAlpha))
                    }
                }
                ParticleType.GOLD_DOT -> {
                    drawCircle(
                        color = p.color.copy(alpha = pAlpha),
                        radius = p.size * 0.5f,
                        center = Offset(curX, upwardY)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = pAlpha * 0.6f),
                        radius = p.size * 0.2f,
                        center = Offset(curX, upwardY)
                    )
                }
                ParticleType.GLOW_ORB -> {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                p.color.copy(alpha = pAlpha * 0.25f),
                                Color.Transparent
                            ),
                            center = Offset(curX, upwardY),
                            radius = p.size
                        ),
                        radius = p.size,
                        center = Offset(curX, upwardY)
                    )
                }
            }
        }
    }
}
