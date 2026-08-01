package com.example.ludo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.PlayerColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class VictoryConfetti(
    val initialX: Float,
    val speedY: Float,
    val speedX: Float,
    val color: Color,
    val size: Float,
    val phase: Float,
    val rotationSpeed: Float
)

private data class VictoryFirework(
    val centerX: Float,
    val centerY: Float,
    val color: Color,
    val particles: List<VictorySpark>
)

private data class VictorySpark(
    val angle: Double,
    val speed: Float,
    val radius: Float
)

private data class GoldCoinParticle(
    val startX: Float,
    val speedX: Float,
    val speedY: Float,
    val size: Float,
    val rotationSpeed: Float,
    val delay: Float
)

@Composable
fun VictoryCelebrationDialog(
    winnerColor: PlayerColor,
    currentTheme: BoardTheme,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    // Entrance Spring Scale Animation
    val scaleAnim = remember { Animatable(0.2f) }
    // Continuous Bobbing Animation
    val bobbingAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Pop in with elastic spring bounce
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "victory_bob")
    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rays"
    )

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .testTag("victory_dialog"),
            contentAlignment = Alignment.Center
        ) {
            // 1. Fullscreen Particle & Fireworks Canvas Overlay
            VictoryBackgroundEffectsCanvas(rayRotation = rayRotation)

            // 2. Centerpiece Trophy, Crown, and Victory Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .scale(scaleAnim.value)
            ) {
                // Shiny 3D Crown & Trophy Hero Banner
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(160.dp)
                ) {
                    // Golden Sunburst Background Glow
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700).copy(alpha = 0.5f),
                                        winnerColor.getThemeColor(currentTheme).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Canvas Trophy + Crown
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawGoldenTrophyAndCrown(winnerColor.getThemeColor(currentTheme))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Glassmorphic Victory Card
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF1E1730).copy(alpha = 0.94f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD700),
                                winnerColor.getThemeColor(currentTheme),
                                Color(0xFFFFE57F)
                            )
                        )
                    ),
                    shadowElevation = 24.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // "VICTORY!" Badge Header
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFFD700),
                                            Color(0xFFFFA000)
                                        )
                                    )
                                )
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "👑 VICTORY 👑",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1A1208),
                                letterSpacing = 1.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Winner Player Avatar Ring
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(winnerColor.getThemeColor(currentTheme))
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${winnerColor.displayName} Player",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Crown Champion of Royal Ludo!",
                            fontSize = 14.sp,
                            color = Color(0xFFAEB2C6),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onBackToMenu,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF5E5478))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Menu",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Menu", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = onPlayAgain,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD700)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Play Again",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Play Again",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VictoryBackgroundEffectsCanvas(rayRotation: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "victory_particles")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles_progress"
    )

    // Falling Confetti ribbons
    val confetti = remember {
        val colors = listOf(
            Color(0xFFFFD700), Color(0xFFFF1744), Color(0xFF00E676),
            Color(0xFF00B0FF), Color(0xFFE040FB), Color(0xFFFF9100),
            Color(0xFF00E5FF), Color(0xFFFFFFFF)
        )
        List(110) {
            VictoryConfetti(
                initialX = Random.nextFloat(),
                speedY = Random.nextFloat() * 0.65f + 0.45f,
                speedX = Random.nextFloat() * 0.28f - 0.14f,
                color = colors[it % colors.size],
                size = Random.nextFloat() * 14f + 8f,
                phase = Random.nextFloat() * 2f * PI.toFloat(),
                rotationSpeed = Random.nextFloat() * 12f - 6f
            )
        }
    }

    // Fireworks Bursts
    val fireworks = remember {
        val colors = listOf(
            Color(0xFFFFD700), Color(0xFFFF2A6D), Color(0xFF05FFA1),
            Color(0xFF00F0FF), Color(0xFFD946EF)
        )
        List(6) {
            val cx = Random.nextFloat() * 0.82f + 0.09f
            val cy = Random.nextFloat() * 0.45f + 0.08f
            VictoryFirework(
                centerX = cx,
                centerY = cy,
                color = colors[it % colors.size],
                particles = List(32) {
                    VictorySpark(
                        angle = Random.nextDouble(0.0, 2 * PI),
                        speed = Random.nextFloat() * 160f + 60f,
                        radius = Random.nextFloat() * 5.5f + 3f
                    )
                }
            )
        }
    }

    // Bouncing Gold Coins
    val goldCoins = remember {
        List(25) {
            GoldCoinParticle(
                startX = Random.nextFloat() * 0.8f + 0.1f,
                speedX = Random.nextFloat() * 260f - 130f,
                speedY = -(Random.nextFloat() * 320f + 200f),
                size = Random.nextFloat() * 16f + 16f,
                rotationSpeed = Random.nextFloat() * 10f + 5f,
                delay = Random.nextFloat() * 0.4f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Rotating Golden Light Rays
        rotate(rayRotation, pivot = Offset(w / 2f, h * 0.38f)) {
            val numRays = 12
            val rayAngle = 360f / numRays
            val rayLength = h * 1.2f
            for (i in 0 until numRays) {
                if (i % 2 == 0) {
                    val angle1 = (i * rayAngle) * (PI / 180.0)
                    val angle2 = ((i + 0.65f) * rayAngle) * (PI / 180.0)
                    val center = Offset(w / 2f, h * 0.38f)

                    val rayPath = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo((center.x + cos(angle1) * rayLength).toFloat(), (center.y + sin(angle1) * rayLength).toFloat())
                        lineTo((center.x + cos(angle2) * rayLength).toFloat(), (center.y + sin(angle2) * rayLength).toFloat())
                        close()
                    }
                    drawPath(rayPath, color = Color(0xFFFFD700).copy(alpha = 0.07f))
                }
            }
        }

        // 2. Fireworks Bursts
        fireworks.forEachIndexed { idx, fw ->
            val fireworkPhase = ((progress + idx * 0.18f) % 1.0f)
            val alpha = (1f - fireworkPhase).coerceIn(0f, 1f)
            val center = Offset(fw.centerX * w, fw.centerY * h)

            drawCircle(
                color = fw.color.copy(alpha = alpha * 0.45f),
                radius = fireworkPhase * 180f,
                center = center,
                style = Stroke(width = (1f - fireworkPhase) * 6f)
            )

            fw.particles.forEach { spark ->
                val dist = spark.speed * fireworkPhase
                val sx = (center.x + cos(spark.angle) * dist).toFloat()
                val sy = (center.y + sin(spark.angle) * dist + (fireworkPhase * fireworkPhase * 40f)).toFloat()

                drawCircle(
                    color = fw.color.copy(alpha = alpha),
                    radius = spark.radius * (1f - fireworkPhase * 0.35f),
                    center = Offset(sx, sy)
                )
            }
        }

        // 3. Gold Coins Explosion Physics
        goldCoins.forEach { coin ->
            val pCoin = (progress - coin.delay).coerceIn(0f, 1f)
            if (pCoin > 0f) {
                val cx = (coin.startX * w) + (coin.speedX * pCoin)
                val cy = (h * 0.45f) + (coin.speedY * pCoin) + (0.5f * 800f * pCoin * pCoin) // gravity formula
                val alphaCoin = (1f - pCoin * 0.8f).coerceIn(0f, 1f)

                rotate(pCoin * coin.rotationSpeed * 180f, pivot = Offset(cx, cy)) {
                    // Coin Outer Rim
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = alphaCoin),
                        radius = coin.size,
                        center = Offset(cx, cy)
                    )
                    // Coin Gold Rim Stroke
                    drawCircle(
                        color = Color(0xFFB8860B).copy(alpha = alphaCoin),
                        radius = coin.size,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.5f)
                    )
                    // Coin Inner Face
                    drawCircle(
                        color = Color(0xFFFFE57F).copy(alpha = alphaCoin),
                        radius = coin.size * 0.72f,
                        center = Offset(cx, cy)
                    )
                    // Specular Highlight
                    drawCircle(
                        color = Color.White.copy(alpha = alphaCoin * 0.7f),
                        radius = coin.size * 0.3f,
                        center = Offset(cx - coin.size * 0.3f, cy - coin.size * 0.3f)
                    )
                }
            }
        }

        // 4. Falling Confetti Stream
        confetti.forEach { p ->
            val curY = ((p.phase + p.speedY * progress * 3.8f) % 1.25f) * h - h * 0.1f
            val curX = (p.initialX + sin(progress * 2.5f * PI.toFloat() + p.phase) * 0.09f) * w

            rotate(progress * 360f * p.rotationSpeed, pivot = Offset(curX, curY)) {
                drawRoundRect(
                    color = p.color,
                    topLeft = Offset(curX, curY),
                    size = Size(p.size, p.size * 0.55f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
        }
    }
}

private fun DrawScope.drawGoldenTrophyAndCrown(playerThemeColor: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f + 10f

    // 1. Trophy Base Pedestal
    val baseWidth = w * 0.42f
    val baseHeight = h * 0.11f
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF3E2723), Color(0xFF1B0000))
        ),
        topLeft = Offset(cx - baseWidth / 2f, cy + h * 0.26f),
        size = Size(baseWidth, baseHeight),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    )
    // Gold Plate on Pedestal
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFFFD700), Color(0xFFFFE57F), Color(0xFFFFD700))
        ),
        topLeft = Offset(cx - baseWidth * 0.3f, cy + h * 0.28f),
        size = Size(baseWidth * 0.6f, baseHeight * 0.45f),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )

    // 2. Trophy Stem & Cup Body
    val cupWidth = w * 0.36f
    val cupHeight = h * 0.32f

    // Cup Path
    val cupPath = Path().apply {
        moveTo(cx - cupWidth / 2f, cy - cupHeight / 2f)
        lineTo(cx + cupWidth / 2f, cy - cupHeight / 2f)
        cubicTo(
            cx + cupWidth / 2f, cy + cupHeight * 0.2f,
            cx + cupWidth * 0.2f, cy + cupHeight / 2f,
            cx, cy + cupHeight / 2f
        )
        cubicTo(
            cx - cupWidth * 0.2f, cy + cupHeight / 2f,
            cx - cupWidth / 2f, cy + cupHeight * 0.2f,
            cx - cupWidth / 2f, cy - cupHeight / 2f
        )
        close()
    }

    // Metallic Gold Cup Fill
    drawPath(
        path = cupPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFE57F),
                Color(0xFFFFD700),
                Color(0xFFB8860B),
                Color(0xFFFFD700),
                Color(0xFFFFF59D)
            ),
            start = Offset(cx - cupWidth, cy),
            end = Offset(cx + cupWidth, cy)
        )
    )

    // Handles (Left & Right)
    val handlePathLeft = Path().apply {
        moveTo(cx - cupWidth / 2f, cy - cupHeight * 0.35f)
        cubicTo(
            cx - cupWidth * 0.95f, cy - cupHeight * 0.35f,
            cx - cupWidth * 0.95f, cy + cupHeight * 0.15f,
            cx - cupWidth / 2f, cy + cupHeight * 0.1f
        )
    }
    val handlePathRight = Path().apply {
        moveTo(cx + cupWidth / 2f, cy - cupHeight * 0.35f)
        cubicTo(
            cx + cupWidth * 0.95f, cy - cupHeight * 0.35f,
            cx + cupWidth * 0.95f, cy + cupHeight * 0.15f,
            cx + cupWidth / 2f, cy + cupHeight * 0.1f
        )
    }
    drawPath(handlePathLeft, color = Color(0xFFFFD700), style = Stroke(width = 6.dp.toPx()))
    drawPath(handlePathRight, color = Color(0xFFFFD700), style = Stroke(width = 6.dp.toPx()))

    // Star Emblem in Center of Cup
    val starRadius = cupWidth * 0.22f
    val starPath = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) starRadius else starRadius * 0.42f
        val a = i * PI / 5 - PI / 2
        val sx = (cx + r * cos(a)).toFloat()
        val sy = (cy - cupHeight * 0.08f + r * sin(a)).toFloat()
        if (i == 0) starPath.moveTo(sx, sy) else starPath.lineTo(sx, sy)
    }
    starPath.close()
    drawPath(starPath, color = playerThemeColor)

    // 3. Golden Crown on Top of Trophy
    val crownWidth = w * 0.38f
    val crownTopY = cy - cupHeight / 2f - h * 0.18f
    val crownBottomY = cy - cupHeight / 2f - 2.dp.toPx()

    val crownPath = Path().apply {
        moveTo(cx - crownWidth / 2f, crownBottomY)
        lineTo(cx - crownWidth / 2f, crownTopY + 12.dp.toPx())
        lineTo(cx - crownWidth * 0.24f, crownTopY + 22.dp.toPx())
        lineTo(cx, crownTopY) // Center peak
        lineTo(cx + crownWidth * 0.24f, crownTopY + 22.dp.toPx())
        lineTo(cx + crownWidth / 2f, crownTopY + 12.dp.toPx())
        lineTo(cx + crownWidth / 2f, crownBottomY)
        close()
    }

    drawPath(
        path = crownPath,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFFFD700), Color(0xFFFFE57F), Color(0xFFFFB300))
        )
    )

    // Jewels on Crown Peaks
    drawCircle(Color(0xFFE53935), radius = 4.dp.toPx(), center = Offset(cx - crownWidth / 2f, crownTopY + 12.dp.toPx()))
    drawCircle(Color(0xFF1E88E5), radius = 4.dp.toPx(), center = Offset(cx, crownTopY))
    drawCircle(Color(0xFF4CAF50), radius = 4.dp.toPx(), center = Offset(cx + crownWidth / 2f, crownTopY + 12.dp.toPx()))
}
