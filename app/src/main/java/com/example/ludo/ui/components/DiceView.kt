package com.example.ludo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun DiceView(
    value: Int,
    isRolling: Boolean,
    isClickable: Boolean,
    playerColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    onRollClick: () -> Unit = {}
) {
    // 3D Rotation Animatable properties
    val rotX = remember { Animatable(0f) }
    val rotY = remember { Animatable(0f) }
    val rotZ = remember { Animatable(0f) }

    // Physics translation (bounce height elevation)
    val offsetY = remember { Animatable(0f) }

    // Squash & Stretch scale factors for impact landing
    val scaleX = remember { Animatable(1f) }
    val scaleY = remember { Animatable(1f) }

    // Glow intensity state
    val glowAnim = remember { Animatable(0f) }

    val density = LocalDensity.current.density

    // Pulse animation for clickable state
    val infiniteTransition = rememberInfiniteTransition(label = "dicePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Trigger 3D Physics & Bounce Animation when rolling
    LaunchedEffect(isRolling) {
        if (isRolling) {
            // Glow flare up
            glowAnim.animateTo(1f, tween(150))

            // Randomized 3D tumble angles
            val targetRotX = (Random.nextInt(2, 5) * 360f) + (Random.nextFloat() * 45f - 22.5f)
            val targetRotY = (Random.nextInt(2, 5) * 360f) + (Random.nextFloat() * 45f - 22.5f)
            val targetRotZ = (Random.nextFloat() * 180f - 90f)

            // Lift up into air (parabolic physics jump)
            offsetY.animateTo(-42f, tween(250, easing = LinearOutSlowInEasing))

            // Tumbling spin phase
            rotX.animateTo(targetRotX, tween(500, easing = LinearEasing))
            rotY.animateTo(targetRotY, tween(500, easing = LinearEasing))
            rotZ.animateTo(targetRotZ, tween(500, easing = LinearEasing))

            // Drop back down to board surface
            offsetY.animateTo(0f, tween(250, easing = FastOutLinearInEasing))

            // Landing Impact: Squash & Stretch
            scaleY.animateTo(0.82f, tween(80))
            scaleX.animateTo(1.15f, tween(80))

            // Elastic Spring Bounce Back to normal shape
            scaleY.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            scaleX.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))

            // Reset 3D rotation to clean flat orientation
            rotX.snapTo(0f)
            rotY.snapTo(0f)
            rotZ.snapTo(0f)

            glowAnim.animateTo(0f, tween(300))
        }
    }

    val activeScale = if (isClickable && !isRolling) pulseScale else 1.0f
    val currentElevation = -offsetY.value // positive height above ground

    val goldBorderBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFFFD700), Color(0xFFB8860B), Color(0xFFFFE57F), Color(0xFFFFD700))
    )

    Box(
        modifier = modifier
            .size(size + 24.dp) // extra canvas margin for shadow & glow
            .testTag("dice_view_container"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Dynamic Physics Ground Shadow
        Canvas(
            modifier = Modifier
                .size(size * 0.9f, size * 0.28f)
                .align(Alignment.BottomCenter)
                .offset(y = (-6).dp)
        ) {
            val shadowAlpha = ((1f - (currentElevation / 50f)).coerceIn(0.15f, 0.75f))
            val shadowExpansion = (currentElevation / 40f) * 12f

            drawOval(
                color = Color.Black.copy(alpha = shadowAlpha),
                topLeft = Offset(-shadowExpansion, -shadowExpansion / 2f),
                size = Size(this.size.width + shadowExpansion * 2, this.size.height + shadowExpansion)
            )
        }

        // 2. Glow Aura Effect (Pulsing player/gold glow)
        if (isClickable || glowAnim.value > 0f) {
            val auraAlpha = if (isRolling) glowAnim.value * 0.9f else 0.45f
            val auraColor = if (value == 6) Color(0xFFFFD700) else playerColor

            Box(
                modifier = Modifier
                    .size(size + 14.dp)
                    .scale(activeScale)
                    .offset(y = offsetY.value.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                auraColor.copy(alpha = auraAlpha),
                                auraColor.copy(alpha = auraAlpha * 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // 3. 3D Tumbling Cube Body
        Box(
            modifier = Modifier
                .size(size)
                .scale(activeScale)
                .offset(y = offsetY.value.dp)
                .graphicsLayer {
                    rotationX = rotX.value
                    rotationY = rotY.value
                    rotationZ = rotZ.value
                    this.scaleX = scaleX.value
                    this.scaleY = scaleY.value
                    cameraDistance = 16f * density
                }
                .shadow(
                    elevation = if (isClickable) 18.dp else 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = if (isClickable) playerColor else Color.Black
                )
                .border(
                    width = if (isClickable) 3.5.dp else 2.dp,
                    brush = if (isClickable) goldBorderBrush else Brush.linearGradient(listOf(Color(0xFF999999), Color(0xFF444444))),
                    shape = RoundedCornerShape(20.dp)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF6F5FB),
                            Color(0xFFE2DFEC)
                        )
                    )
                )
                .clickable(enabled = isClickable, onClick = onRollClick)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // 4. 3D Face Canvas with Specular Highlights and Recessed Pips
            Canvas(modifier = Modifier.size(size - 16.dp)) {
                val w = this.size.width
                val h = this.size.height
                val dotRadius = w * 0.115f

                // 3D Inner Bevel Rim Gradient
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White, Color(0x22000000))
                    ),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Red dot for 1 (traditional royal dice), dark navy/gold for others
                val mainDotColor = if (value == 1) Color(0xFFD32F2F) else Color(0xFF1E1B29)

                val left = w * 0.25f
                val center = w * 0.50f
                val right = w * 0.75f

                val top = h * 0.25f
                val middle = h * 0.50f
                val bottom = h * 0.75f

                fun drawPip(x: Float, y: Float, color: Color = mainDotColor) {
                    // Recessed Inner Shadow
                    drawCircle(
                        color = Color(0x44000000),
                        radius = dotRadius + 1.5f,
                        center = Offset(x + 1.2f, y + 1.2f)
                    )

                    // Pip Body
                    drawCircle(
                        color = color,
                        radius = dotRadius,
                        center = Offset(x, y)
                    )

                    // Specular Highlight Glass Bead Reflection
                    drawCircle(
                        color = Color.White.copy(alpha = 0.65f),
                        radius = dotRadius * 0.38f,
                        center = Offset(x - dotRadius * 0.32f, y - dotRadius * 0.32f)
                    )
                }

                when (value) {
                    1 -> {
                        drawPip(center, middle)
                    }
                    2 -> {
                        drawPip(left, top)
                        drawPip(right, bottom)
                    }
                    3 -> {
                        drawPip(left, top)
                        drawPip(center, middle)
                        drawPip(right, bottom)
                    }
                    4 -> {
                        drawPip(left, top)
                        drawPip(right, top)
                        drawPip(left, bottom)
                        drawPip(right, bottom)
                    }
                    5 -> {
                        drawPip(left, top)
                        drawPip(right, top)
                        drawPip(center, middle)
                        drawPip(left, bottom)
                        drawPip(right, bottom)
                    }
                    6 -> {
                        drawPip(left, top)
                        drawPip(right, top)
                        drawPip(left, middle)
                        drawPip(right, middle)
                        drawPip(left, bottom)
                        drawPip(right, bottom)
                    }
                }
            }
        }
    }
}
