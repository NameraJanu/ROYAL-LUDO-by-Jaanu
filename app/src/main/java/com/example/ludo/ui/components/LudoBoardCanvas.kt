package com.example.ludo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ludo.engine.GridCoord
import com.example.ludo.engine.LudoBoardPaths
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.CaptureEffectData
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LudoBoardCanvas(
    players: List<Player>,
    activePlayerColor: PlayerColor,
    movableTokenIds: List<Int>,
    theme: BoardTheme = BoardTheme.ROYAL,
    activeCaptureEffect: CaptureEffectData? = null,
    modifier: Modifier = Modifier,
    onTokenTapped: (PlayerColor, Int) -> Unit = { _, _ -> }
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // Active Token Pulse
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tokenPulse"
    )

    // Safe Zone Glow Pulse
    val safeZoneGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "safeZoneGlow"
    )

    // Animated Turn Indicator Glow & Scale
    val turnIndicatorGlow by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "turnIndicatorGlow"
    )

    // Shining Home Path Shimmer Progress
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(4.dp)
    ) {
        // 1. Board Surface Canvas (Grid, Yards, Glowing Safe Zones, Shining Home Path, Turn Indicator)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ludo_board_canvas")
                .pointerInput(players, activePlayerColor, movableTokenIds) {
                    detectTapGestures { tapOffset ->
                        val boardWidth = size.width
                        val cellSize = boardWidth / 15f
                        val tapGridX = (tapOffset.x / cellSize).toInt().coerceIn(0, 14)
                        val tapGridY = (tapOffset.y / cellSize).toInt().coerceIn(0, 14)

                        // Find token at tapped grid
                        val activePlayer = players.find { it.color == activePlayerColor } ?: return@detectTapGestures
                        for (tokenId in movableTokenIds) {
                            val token = activePlayer.tokens.find { it.id == tokenId } ?: continue
                            val tokenCoord = LudoBoardPaths.getTokenCoord(
                                color = activePlayer.color,
                                step = token.step,
                                yardSpotIndex = token.yardSpotIndex
                            )
                            if (tokenCoord.x == tapGridX && tokenCoord.y == tapGridY) {
                                onTokenTapped(activePlayer.color, tokenId)
                                break
                            }
                        }
                    }
                }
        ) {
            val boardSize = size.width
            val cellSize = boardSize / 15f

            // Board Background
            drawRect(color = theme.boardBgColor, size = Size(boardSize, boardSize))

            // 15x15 Cell Grid Lines
            for (i in 0..15) {
                val pos = i * cellSize
                drawLine(theme.gridLineColor, Offset(pos, 0f), Offset(pos, boardSize), strokeWidth = 1f)
                drawLine(theme.gridLineColor, Offset(0f, pos), Offset(boardSize, pos), strokeWidth = 1f)
            }

            // Corner Home Yards
            drawHomeYard(PlayerColor.RED, 0, 0, cellSize, theme)
            drawHomeYard(PlayerColor.GREEN, 9, 0, cellSize, theme)
            drawHomeYard(PlayerColor.YELLOW, 9, 9, cellSize, theme)
            drawHomeYard(PlayerColor.BLUE, 0, 9, cellSize, theme)

            // Animated Turn Indicator on Active Player's Yard
            drawAnimatedTurnIndicator(activePlayerColor, cellSize, turnIndicatorGlow, theme)

            // Shining Home Stretches
            drawShiningHomeStretches(cellSize, theme, shimmerProgress)

            // Start Squares
            drawCellFill(1, 6, PlayerColor.RED.getThemeColor(theme), cellSize)
            drawCellFill(8, 1, PlayerColor.GREEN.getThemeColor(theme), cellSize)
            drawCellFill(13, 8, PlayerColor.YELLOW.getThemeColor(theme), cellSize)
            drawCellFill(6, 13, PlayerColor.BLUE.getThemeColor(theme), cellSize)

            // Glowing Safe Zone Stars
            val starCoords = listOf(
                GridCoord(1, 6), GridCoord(6, 2),
                GridCoord(8, 1), GridCoord(12, 6),
                GridCoord(13, 8), GridCoord(8, 12),
                GridCoord(6, 13), GridCoord(2, 8)
            )
            starCoords.forEach { coord ->
                drawGlowingSafeZone(coord.x, coord.y, cellSize, theme, safeZoneGlowAlpha)
                drawStar(coord.x, coord.y, cellSize, theme)
            }

            // Center Goal Triangles
            drawCenterGoalTriangles(cellSize, theme)

            // Subtle Ambient Floating Dust Particles (High-performance Math Canvas)
            drawSubtleBoardParticles(boardSize, shimmerProgress)

            // Outer Frame Border
            drawRect(
                color = theme.outerBorderColor,
                style = Stroke(width = 6f),
                size = Size(boardSize, boardSize)
            )
        }

        // 2. Animated Tokens Layer
        val tokensByCoord = remember(players) {
            val map = mutableMapOf<GridCoord, MutableList<Pair<PlayerColor, Int>>>()
            for (player in players) {
                for (token in player.tokens) {
                    val coord = LudoBoardPaths.getTokenCoord(
                        color = player.color,
                        step = token.step,
                        yardSpotIndex = token.yardSpotIndex
                    )
                    map.getOrPut(coord) { mutableListOf() }.add(Pair(player.color, token.id))
                }
            }
            map
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val boardSize = constraints.maxWidth.toFloat()
            val cellSize = boardSize / 15f

            for (player in players) {
                for (token in player.tokens) {
                    val coord = LudoBoardPaths.getTokenCoord(
                        color = player.color,
                        step = token.step,
                        yardSpotIndex = token.yardSpotIndex
                    )
                    val tokenList = tokensByCoord[coord] ?: emptyList()
                    val indexInTile = tokenList.indexOfFirst { it.first == player.color && it.second == token.id }
                    val offsetMult = if (tokenList.size > 1 && indexInTile >= 0) {
                        (indexInTile - (tokenList.size - 1) / 2f) * 0.22f
                    } else 0f

                    val targetX = (coord.x + 0.5f + offsetMult) * cellSize
                    val targetY = (coord.y + 0.5f) * cellSize
                    val targetOffset = Offset(targetX, targetY)

                    val isThisTokenMovable = (player.color == activePlayerColor && movableTokenIds.contains(token.id))

                    key("${player.color.name}_${token.id}") {
                        AnimatedTokenView(
                            targetOffset = targetOffset,
                            playerColor = player.color,
                            theme = theme,
                            isMovable = isThisTokenMovable,
                            pulseScale = if (isThisTokenMovable) pulseScale else 1.0f,
                            cellSize = cellSize
                        )
                    }
                }
            }
        }

        // 3. Capture Particle Effect Overlay
        if (activeCaptureEffect != null) {
            CaptureParticleEffect(
                gridCoord = activeCaptureEffect.coord,
                color = activeCaptureEffect.color,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun AnimatedTokenView(
    targetOffset: Offset,
    playerColor: PlayerColor,
    theme: BoardTheme,
    isMovable: Boolean,
    pulseScale: Float,
    cellSize: Float
) {
    val animPosition = remember { Animatable(targetOffset, Offset.VectorConverter) }
    val bounceOffset = remember { Animatable(0f) }
    val isMovingState = remember { mutableStateOf(false) }

    LaunchedEffect(targetOffset) {
        val distance = (targetOffset - animPosition.value).getDistance()
        if (distance > 1f) {
            isMovingState.value = true
            val isLargeJump = distance > cellSize * 2f
            val hopDuration = if (isLargeJump) 380 else 150
            val maxBounce = if (isLargeJump) cellSize * 1.8f else cellSize * 0.45f

            launch {
                bounceOffset.animateTo(maxBounce, tween(hopDuration / 2, easing = FastOutLinearInEasing))
                bounceOffset.animateTo(0f, tween(hopDuration / 2, easing = LinearOutSlowInEasing))
                isMovingState.value = false
            }

            animPosition.animateTo(
                targetOffset,
                animationSpec = tween(
                    durationMillis = hopDuration,
                    easing = if (isLargeJump) FastOutSlowInEasing else LinearOutSlowInEasing
                )
            )
        } else {
            animPosition.snapTo(targetOffset)
        }
    }

    val currentBounce = bounceOffset.value
    val currentPos = animPosition.value
    val tokenRadius = cellSize * 0.38f
    val isMoving = isMovingState.value
    val currentPulse = if (isMovable && !isMoving) pulseScale else 1.0f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val groundX = currentPos.x
        val groundY = currentPos.y
        val airY = groundY - currentBounce

        // 1. Dynamic Physics Ground Shadow
        val shadowScale = (1f - (currentBounce / (cellSize * 2f))).coerceIn(0.2f, 1f)
        val shadowAlpha = (0.45f * shadowScale).coerceIn(0.1f, 0.45f)
        drawOval(
            color = Color.Black.copy(alpha = shadowAlpha),
            topLeft = Offset(
                groundX - (tokenRadius * 1.1f * shadowScale),
                groundY + (cellSize * 0.16f) - (tokenRadius * 0.35f * shadowScale)
            ),
            size = Size(
                tokenRadius * 2.2f * shadowScale,
                tokenRadius * 0.7f * shadowScale
            )
        )

        // 2. Active Glow / Motion Trail
        val tokenColor = playerColor.getThemeColor(theme)
        if (isMovable || isMoving) {
            val glowAlpha = if (isMoving) 0.65f else 0.40f
            val glowRadius = (tokenRadius * currentPulse) + (if (isMoving) 9f else 5f)

            // Outer Radial Aura
            drawCircle(
                color = Color.White.copy(alpha = glowAlpha),
                radius = glowRadius + 4f,
                center = Offset(groundX, airY)
            )
            // Golden Rim Glow
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = glowAlpha + 0.25f),
                radius = glowRadius,
                center = Offset(groundX, airY),
                style = Stroke(width = 4f)
            )
        }

        // 3. Token Body Rendering
        val currentRadius = tokenRadius * currentPulse

        // Solid Token Disk
        drawCircle(
            color = tokenColor,
            radius = currentRadius,
            center = Offset(groundX, airY)
        )

        // Gold / White Outer Border
        drawCircle(
            color = if (isMovable || isMoving) Color(0xFFFFD700) else Color.White,
            radius = currentRadius,
            center = Offset(groundX, airY),
            style = Stroke(width = 3.5f)
        )

        // Inner Bevel Contour
        drawCircle(
            color = Color.White.copy(alpha = 0.35f),
            radius = currentRadius * 0.7f,
            center = Offset(groundX, airY),
            style = Stroke(width = 2f)
        )

        // Center Crown / Jewel Emblem Dot
        drawCircle(
            color = if (isMovable || isMoving) Color(0xFFFFD700) else Color.White.copy(alpha = 0.9f),
            radius = currentRadius * 0.28f,
            center = Offset(groundX, airY)
        )

        // Specular Glass Reflection Highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.75f),
            radius = currentRadius * 0.35f,
            center = Offset(groundX - currentRadius * 0.3f, airY - currentRadius * 0.3f)
        )
    }
}

private fun DrawScope.drawCellFill(x: Int, y: Int, color: Color, cellSize: Float) {
    drawRect(
        color = color,
        topLeft = Offset(x * cellSize, y * cellSize),
        size = Size(cellSize, cellSize)
    )
    drawRect(
        color = Color(0x33000000),
        topLeft = Offset(x * cellSize, y * cellSize),
        size = Size(cellSize, cellSize),
        style = Stroke(width = 1f)
    )
}

private fun DrawScope.drawHomeYard(color: PlayerColor, startX: Int, startY: Int, cellSize: Float, theme: BoardTheme) {
    val yardSize = cellSize * 6f
    val topLeft = Offset(startX * cellSize, startY * cellSize)
    val mainColor = color.getThemeColor(theme)

    // Outer solid yard color
    drawRect(color = mainColor, topLeft = topLeft, size = Size(yardSize, yardSize))

    // Inner Box Background
    val innerMargin = cellSize * 1f
    val innerSize = cellSize * 4f
    drawRect(
        color = theme.innerYardBgColor,
        topLeft = Offset(topLeft.x + innerMargin, topLeft.y + innerMargin),
        size = Size(innerSize, innerSize)
    )

    // 4 Yard Spots
    val spotCoords = listOf(
        Pair(startX + 2f, startY + 2f),
        Pair(startX + 4f, startY + 2f),
        Pair(startX + 2f, startY + 4f),
        Pair(startX + 4f, startY + 4f)
    )
    spotCoords.forEach { (x, y) ->
        drawCircle(
            color = mainColor.copy(alpha = 0.3f),
            radius = cellSize * 0.45f,
            center = Offset(x * cellSize, y * cellSize)
        )
        drawCircle(
            color = mainColor,
            radius = cellSize * 0.40f,
            center = Offset(x * cellSize, y * cellSize),
            style = Stroke(width = 2.5f)
        )
    }
}

private fun DrawScope.drawAnimatedTurnIndicator(
    activeColor: PlayerColor,
    cellSize: Float,
    glowAlpha: Float,
    theme: BoardTheme
) {
    val (startX, startY) = when (activeColor) {
        PlayerColor.RED -> Pair(0f, 0f)
        PlayerColor.GREEN -> Pair(9f, 0f)
        PlayerColor.YELLOW -> Pair(9f, 9f)
        PlayerColor.BLUE -> Pair(0f, 9f)
    }

    val yardSize = cellSize * 6f
    val topLeft = Offset(startX * cellSize, startY * cellSize)
    val themeColor = activeColor.getThemeColor(theme)

    // Outer Glowing Crown Aura around Active Player Yard
    drawRoundRect(
        color = Color(0xFFFFD700).copy(alpha = glowAlpha * 0.65f),
        topLeft = Offset(topLeft.x - 4f, topLeft.y - 4f),
        size = Size(yardSize + 8f, yardSize + 8f),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
        style = Stroke(width = 6f)
    )

    drawRoundRect(
        color = themeColor.copy(alpha = glowAlpha * 0.8f),
        topLeft = Offset(topLeft.x - 2f, topLeft.y - 2f),
        size = Size(yardSize + 4f, yardSize + 4f),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        style = Stroke(width = 3.5f)
    )
}

private fun DrawScope.drawShiningHomeStretches(
    cellSize: Float,
    theme: BoardTheme,
    shimmerProgress: Float
) {
    val stretchConfigs = listOf(
        Pair(PlayerColor.RED, (1..5).map { GridCoord(it, 7) }),
        Pair(PlayerColor.GREEN, (1..5).map { GridCoord(7, it) }),
        Pair(PlayerColor.YELLOW, (9..13).map { GridCoord(it, 7) }),
        Pair(PlayerColor.BLUE, (9..13).map { GridCoord(7, it) })
    )

    stretchConfigs.forEach { (color, coords) ->
        val baseColor = color.getThemeColor(theme)
        coords.forEachIndexed { idx, coord ->
            drawCellFill(coord.x, coord.y, baseColor, cellSize)

            // Traveling Shimmer Highlight Beam
            val cellProgress = (idx / 5f)
            val diff = (shimmerProgress - cellProgress).let { if (it < 0) it + 1f else it }
            if (diff in 0f..0.3f) {
                val shineAlpha = (1f - (diff / 0.3f)) * 0.6f
                drawRect(
                    color = Color.White.copy(alpha = shineAlpha),
                    topLeft = Offset(coord.x * cellSize, coord.y * cellSize),
                    size = Size(cellSize, cellSize)
                )
            }
        }
    }
}

private fun DrawScope.drawGlowingSafeZone(
    gridX: Int,
    gridY: Int,
    cellSize: Float,
    theme: BoardTheme,
    glowAlpha: Float
) {
    val centerX = (gridX + 0.5f) * cellSize
    val centerY = (gridY + 0.5f) * cellSize

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFD700).copy(alpha = glowAlpha * 0.75f),
                Color(0xFFFFD700).copy(alpha = glowAlpha * 0.25f),
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = cellSize * 0.8f
        ),
        radius = cellSize * 0.8f,
        center = Offset(centerX, centerY)
    )
}

private fun DrawScope.drawStar(gridX: Int, gridY: Int, cellSize: Float, theme: BoardTheme) {
    val centerX = (gridX + 0.5f) * cellSize
    val centerY = (gridY + 0.5f) * cellSize
    val outerRadius = cellSize * 0.32f
    val innerRadius = cellSize * 0.14f

    val path = Path()
    for (i in 0 until 10) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = i * PI / 5 - PI / 2
        val x = (centerX + radius * cos(angle)).toFloat()
        val y = (centerY + radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(path, color = theme.starColor)
    drawPath(path, color = theme.starBorderColor, style = Stroke(width = 1.5f))
}

private fun DrawScope.drawCenterGoalTriangles(cellSize: Float, theme: BoardTheme) {
    val leftX = 6 * cellSize
    val rightX = 9 * cellSize
    val topY = 6 * cellSize
    val bottomY = 9 * cellSize
    val centerX = 7.5f * cellSize
    val centerY = 7.5f * cellSize

    // Left Red
    val pathRed = Path().apply {
        moveTo(leftX, topY)
        lineTo(centerX, centerY)
        lineTo(leftX, bottomY)
        close()
    }
    drawPath(pathRed, color = PlayerColor.RED.getThemeColor(theme))

    // Top Green
    val pathGreen = Path().apply {
        moveTo(leftX, topY)
        lineTo(rightX, topY)
        lineTo(centerX, centerY)
        close()
    }
    drawPath(pathGreen, color = PlayerColor.GREEN.getThemeColor(theme))

    // Right Yellow
    val pathYellow = Path().apply {
        moveTo(rightX, topY)
        lineTo(rightX, bottomY)
        lineTo(centerX, centerY)
        close()
    }
    drawPath(pathYellow, color = PlayerColor.YELLOW.getThemeColor(theme))

    // Bottom Blue
    val pathBlue = Path().apply {
        moveTo(leftX, bottomY)
        lineTo(rightX, bottomY)
        lineTo(centerX, centerY)
        close()
    }
    drawPath(pathBlue, color = PlayerColor.BLUE.getThemeColor(theme))
}

private fun DrawScope.drawSubtleBoardParticles(boardSize: Float, shimmerProgress: Float) {
    val particleCount = 18
    for (i in 0 until particleCount) {
        val seed = i * 137.5f
        val baseX = (sin(seed) * 0.5f + 0.5f) * boardSize
        val baseY = (cos(seed * 1.3f) * 0.5f + 0.5f) * boardSize
        val offsetY = ((shimmerProgress + (i / particleCount.toFloat())) % 1f) * -28f
        val alpha = sin(PI * ((shimmerProgress + (i / particleCount.toFloat())) % 1f)).toFloat() * 0.4f

        drawCircle(
            color = Color(0xFFFFD700).copy(alpha = alpha.coerceIn(0f, 0.5f)),
            radius = (1.5f + (i % 3) * 0.8f).dp.toPx(),
            center = Offset(baseX, baseY + offsetY)
        )
    }
}
