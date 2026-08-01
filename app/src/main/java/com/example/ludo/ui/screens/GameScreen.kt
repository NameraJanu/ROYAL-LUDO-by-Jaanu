package com.example.ludo.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.GameStatus
import com.example.ludo.model.PlayerColor
import com.example.ludo.ui.components.DiceView
import com.example.ludo.ui.components.LudoBoardCanvas
import com.example.ludo.ui.components.PlayerCard
import com.example.ludo.ui.components.ThemeSelectorDialog
import com.example.ludo.ui.components.VictoryCelebrationDialog
import com.example.ludo.ui.components.bounceClick
import com.example.ludo.viewmodel.LudoViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: LudoViewModel,
    onBackToMenu: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showHowToPlayDialog by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }

    if (uiState.showThemeSelector) {
        ThemeSelectorDialog(
            currentTheme = uiState.currentTheme,
            onThemeSelected = { theme ->
                viewModel.selectTheme(theme)
            },
            onDismiss = { viewModel.closeThemeSelector() }
        )
    }

    if (showHowToPlayDialog) {
        HowToPlayDialog(onDismiss = { showHowToPlayDialog = false })
    }

    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            title = { Text("Pause Game", fontWeight = FontWeight.Bold, color = Color.White) },
            text = { Text("Are you sure you want to exit to the main menu? Current game progress will be saved/lost.", color = Color(0xFFAEAAAE)) },
            containerColor = Color(0xFF231B36),
            confirmButton = {
                TextButton(
                    onClick = {
                        showPauseDialog = false
                        onBackToMenu()
                    }
                ) {
                    Text("Exit to Menu", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPauseDialog = false }) {
                    Text("Resume", color = Color.White)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.gameMode.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .bounceClick { viewModel.openThemeSelector() }
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2D2540))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${uiState.currentTheme.emoji} Theme",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showPauseDialog = true },
                        modifier = Modifier.bounceClick { showPauseDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleMusic() },
                        modifier = Modifier.bounceClick { viewModel.toggleMusic() }
                    ) {
                        Icon(
                            imageVector = if (uiState.musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                            contentDescription = "Music",
                            tint = if (uiState.musicEnabled) Color(0xFFFFE57F) else Color(0xFF78718C)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.openThemeSelector() },
                        modifier = Modifier.bounceClick { viewModel.openThemeSelector() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Themes",
                            tint = Color(0xFFFFD700)
                        )
                    }
                    IconButton(
                        onClick = { showHowToPlayDialog = true },
                        modifier = Modifier.bounceClick { showHowToPlayDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Rules",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleSound() },
                        modifier = Modifier.bounceClick { viewModel.toggleSound() }
                    ) {
                        Icon(
                            imageVector = if (uiState.soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Sound",
                            tint = if (uiState.soundEnabled) Color.White else Color(0xFF78718C)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B1628)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1B1628), Color(0xFF13101E))
                    )
                )
        ) {
            // AnimatedContent for smooth theme switching fade & scale transition
            AnimatedContent(
                targetState = uiState.currentTheme,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.95f, animationSpec = tween(220)))
                        .togetherWith(fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.95f, animationSpec = tween(180)))
                },
                label = "theme_switch_anim",
                modifier = Modifier.fillMaxSize()
            ) { targetTheme ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Player Score Cards (Red / Green)
                    val topPlayers = uiState.players.take(2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        topPlayers.forEachIndexed { index, player ->
                            PlayerCard(
                                player = player,
                                isActive = index == uiState.activePlayerIndex,
                                theme = targetTheme,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Screen Shake effect on capture
                    val boardShake = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
                    LaunchedEffect(uiState.activeCaptureEffect) {
                        if (uiState.activeCaptureEffect != null) {
                            val intensity = 14f
                            for (i in 0..6) {
                                val angle = Random.nextFloat() * 2 * PI.toFloat()
                                val dx = cos(angle) * intensity * (1f - i / 7f)
                                val dy = sin(angle) * intensity * (1f - i / 7f)
                                boardShake.animateTo(Offset(dx, dy), tween(35))
                            }
                            boardShake.snapTo(Offset.Zero)
                        }
                    }

                    // Central Ludo Board Canvas
                    val activeColor = uiState.activePlayer?.color ?: PlayerColor.RED
                    LudoBoardCanvas(
                        players = uiState.players,
                        activePlayerColor = activeColor,
                        movableTokenIds = uiState.movableTokenIds,
                        theme = targetTheme,
                        activeCaptureEffect = uiState.activeCaptureEffect,
                        onTokenTapped = { color, tokenId ->
                            viewModel.onTokenTapped(color, tokenId)
                        },
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .graphicsLayer {
                                translationX = boardShake.value.x
                                translationY = boardShake.value.y
                            }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bottom Player Score Cards (Yellow / Blue)
                    val bottomPlayers = uiState.players.drop(2)
                    if (bottomPlayers.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            bottomPlayers.forEachIndexed { index, player ->
                                PlayerCard(
                                    player = player,
                                    isActive = (index + 2) == uiState.activePlayerIndex,
                                    theme = targetTheme,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bottom Dice & Control Panel
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF38304D), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF221A33)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Turn Status Text
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(activeColor.getThemeColor(targetTheme))
                                            .border(1.dp, Color.White, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${uiState.activePlayer?.name ?: ""}'s Turn",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = uiState.statusMessage,
                                    fontSize = 12.sp,
                                    color = Color(0xFFAEAAAE)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Dice Control
                            val canRoll = uiState.gameStatus == GameStatus.WAITING_FOR_ROLL &&
                                    !(uiState.activePlayer?.isBot ?: false) &&
                                    !uiState.isDiceRolling

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DiceView(
                                    value = uiState.diceValue,
                                    isRolling = uiState.isDiceRolling,
                                    isClickable = canRoll,
                                    playerColor = activeColor.getThemeColor(targetTheme),
                                    onRollClick = { viewModel.rollDice() }
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { viewModel.rollDice() },
                                    enabled = canRoll,
                                    modifier = Modifier
                                        .bounceClick { if (canRoll) viewModel.rollDice() }
                                        .testTag("roll_dice_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = activeColor.getThemeColor(targetTheme)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Casino,
                                        contentDescription = "Roll"
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ROLL", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Victory Celebration Overlay
            if (uiState.gameStatus == GameStatus.GAME_OVER) {
                val winnerColor = uiState.winners.firstOrNull() ?: PlayerColor.RED
                VictoryCelebrationDialog(
                    winnerColor = winnerColor,
                    currentTheme = uiState.currentTheme,
                    onPlayAgain = { viewModel.startNewGame(uiState.gameMode) },
                    onBackToMenu = onBackToMenu
                )
            }
        }
    }
}
