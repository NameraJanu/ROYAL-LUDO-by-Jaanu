package com.example.ludo.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.GameMode
import com.example.ludo.ui.components.RoyalBackgroundCanvas
import com.example.ludo.ui.components.bounceClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainMenuScreen(
    soundEnabled: Boolean,
    musicEnabled: Boolean,
    currentTheme: BoardTheme,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onOpenThemes: () -> Unit,
    onStartGame: (GameMode) -> Unit
) {
    var showHowToPlay by remember { mutableStateOf(false) }

    if (showHowToPlay) {
        HowToPlayDialog(onDismiss = { showHowToPlay = false })
    }

    // Staggered Fade-In Animatable States
    val headerAnim = remember { Animatable(0f) }
    val logoAnim = remember { Animatable(0f) }
    val card1Anim = remember { Animatable(0f) }
    val card2Anim = remember { Animatable(0f) }
    val card3Anim = remember { Animatable(0f) }
    val footerAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val springSpec = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
        launch { headerAnim.animateTo(1f, tween(300)) }
        delay(80)
        launch { logoAnim.animateTo(1f, springSpec) }
        delay(100)
        launch { card1Anim.animateTo(1f, springSpec) }
        delay(80)
        launch { card2Anim.animateTo(1f, springSpec) }
        delay(80)
        launch { card3Anim.animateTo(1f, springSpec) }
        delay(80)
        launch { footerAnim.animateTo(1f, tween(300)) }
    }

    // Pulsing Glowing Logo Breathing Transition
    val infiniteTransition = rememberInfiniteTransition(label = "logoGlow")
    val crownPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crownPulseScale"
    )

    val auraGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraGlowAlpha"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Floating Royal Particles & Dynamic Background Canvas
            RoyalBackgroundCanvas()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 2. Header Bar with Smooth Fade-In
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(headerAnim.value)
                        .graphicsLayer { translationY = (1f - headerAnim.value) * -30f },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showHowToPlay = true },
                        modifier = Modifier.bounceClick { showHowToPlay = true }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "How to Play",
                            tint = Color.White
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onToggleMusic,
                            modifier = Modifier.bounceClick { onToggleMusic() }
                        ) {
                            Icon(
                                imageVector = if (musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                contentDescription = "Toggle Music",
                                tint = if (musicEnabled) Color(0xFFFFE57F) else Color(0xFF78718C)
                            )
                        }

                        IconButton(
                            onClick = onOpenThemes,
                            modifier = Modifier.bounceClick { onOpenThemes() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Select Theme",
                                tint = Color(0xFFFFD700)
                            )
                        }

                        IconButton(
                            onClick = onToggleSound,
                            modifier = Modifier.bounceClick { onToggleSound() }
                        ) {
                            Icon(
                                imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "Toggle Sound",
                                tint = if (soundEnabled) Color.White else Color(0xFF78718C)
                            )
                        }
                    }
                }

                // 3. Glowing Logo & Royal Title Banner
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .alpha(logoAnim.value)
                        .graphicsLayer {
                            translationY = (1f - logoAnim.value) * 40f
                            scaleX = logoAnim.value
                            scaleY = logoAnim.value
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(120.dp)
                    ) {
                        // Soft Radial Aura Glow
                        Box(
                            modifier = Modifier
                                .size(118.dp)
                                .scale(crownPulseScale)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFFD700).copy(alpha = auraGlowAlpha),
                                            Color(0xFFFFA000).copy(alpha = auraGlowAlpha * 0.5f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Center Golden Crown Box
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .scale(crownPulseScale)
                                .shadow(
                                    elevation = 16.dp,
                                    shape = CircleShape,
                                    spotColor = Color(0xFFFFD700)
                                )
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFFE57F),
                                            Color(0xFFFFD700),
                                            Color(0xFFB8860B),
                                            Color(0xFF1E172A)
                                        )
                                    )
                                )
                                .border(3.dp, Color(0xFFFFD700), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "👑",
                                fontSize = 44.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "ROYAL LUDO",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD700),
                        letterSpacing = 2.5.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Pass & Play • Smart AI • 10 Themes",
                        fontSize = 13.sp,
                        color = Color(0xFFAEAAAE),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Active Theme Badge Button
                    Box(
                        modifier = Modifier
                            .bounceClick { onOpenThemes() }
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF2E273F))
                            .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "${currentTheme.emoji} Theme: ${currentTheme.displayName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }

                // 4. Staggered Game Mode Cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .alpha(card1Anim.value)
                            .graphicsLayer { translationY = (1f - card1Anim.value) * 50f }
                    ) {
                        GameModeCard(
                            title = GameMode.VS_COMPUTER.title,
                            description = "Play against 3 Smart Bot opponents",
                            icon = Icons.Default.Person,
                            accentColor = Color(0xFFFFD700),
                            tag = "mode_vs_computer",
                            onClick = { onStartGame(GameMode.VS_COMPUTER) }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .alpha(card2Anim.value)
                            .graphicsLayer { translationY = (1f - card2Anim.value) * 50f }
                    ) {
                        GameModeCard(
                            title = GameMode.PASS_AND_PLAY_2P.title,
                            description = GameMode.PASS_AND_PLAY_2P.description,
                            icon = Icons.Default.People,
                            accentColor = Color(0xFF00E676),
                            tag = "mode_pass_play_2p",
                            onClick = { onStartGame(GameMode.PASS_AND_PLAY_2P) }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .alpha(card3Anim.value)
                            .graphicsLayer { translationY = (1f - card3Anim.value) * 50f }
                    ) {
                        GameModeCard(
                            title = GameMode.PASS_AND_PLAY_4P.title,
                            description = GameMode.PASS_AND_PLAY_4P.description,
                            icon = Icons.Default.Group,
                            accentColor = Color(0xFF2979FF),
                            tag = "mode_pass_play_4p",
                            onClick = { onStartGame(GameMode.PASS_AND_PLAY_4P) }
                        )
                    }
                }

                // 5. Footer Buttons with Bounce Feedback
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .alpha(footerAnim.value)
                        .graphicsLayer { translationY = (1f - footerAnim.value) * 30f },
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenThemes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick { onOpenThemes() },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF382E52))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Themes",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Select Board Theme (10 Styles)", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { showHowToPlay = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick { showHowToPlay = true },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Game Rules & How to Play",
                            color = Color(0xFFAEAAAE),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFF3D3354), RoundedCornerShape(20.dp))
            .bounceClick(onClick = onClick)
            .testTag(tag),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF251E36)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color(0xFFAEAAAE)
                )
            }
        }
    }
}
