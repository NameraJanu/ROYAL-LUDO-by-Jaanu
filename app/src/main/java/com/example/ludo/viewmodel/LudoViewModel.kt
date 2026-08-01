package com.example.ludo.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ludo.audio.LudoAudioEngine
import com.example.ludo.engine.LudoAiAgent
import com.example.ludo.engine.LudoBoardPaths
import com.example.ludo.engine.LudoRulesEngine
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.CaptureEffectData
import com.example.ludo.model.GameMode
import com.example.ludo.model.GameState
import com.example.ludo.model.GameStatus
import com.example.ludo.model.MoveResult
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class LudoViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("royal_ludo_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val audioEngine = LudoAudioEngine(application)

    init {
        // Load saved theme and audio settings
        val savedThemeOrdinal = prefs.getInt("selected_theme_type", BoardTheme.ROYAL.type.ordinal)
        val initialTheme = BoardTheme.getByOrdinal(savedThemeOrdinal)
        val savedSound = prefs.getBoolean("sound_enabled", true)
        val savedMusic = prefs.getBoolean("music_enabled", true)

        audioEngine.isSoundEnabled = savedSound
        audioEngine.isMusicEnabled = savedMusic

        _uiState.update { 
            it.copy(
                currentTheme = initialTheme,
                soundEnabled = savedSound,
                musicEnabled = savedMusic
            ) 
        }

        startNewGame(GameMode.VS_COMPUTER)
    }

    fun startNewGame(mode: GameMode) {
        val currentTheme = _uiState.value.currentTheme
        val currentSound = _uiState.value.soundEnabled
        val currentMusic = _uiState.value.musicEnabled

        val players = when (mode) {
            GameMode.VS_COMPUTER -> listOf(
                Player(color = PlayerColor.RED, name = "You (Red)", isBot = false),
                Player(color = PlayerColor.GREEN, name = "Bot Green", isBot = true),
                Player(color = PlayerColor.YELLOW, name = "Bot Yellow", isBot = true),
                Player(color = PlayerColor.BLUE, name = "Bot Blue", isBot = true)
            )
            GameMode.PASS_AND_PLAY_2P -> listOf(
                Player(color = PlayerColor.RED, name = "Player 1 (Red)", isBot = false),
                Player(color = PlayerColor.YELLOW, name = "Player 2 (Yellow)", isBot = false)
            )
            GameMode.PASS_AND_PLAY_4P -> listOf(
                Player(color = PlayerColor.RED, name = "Player 1 (Red)", isBot = false),
                Player(color = PlayerColor.GREEN, name = "Player 2 (Green)", isBot = false),
                Player(color = PlayerColor.YELLOW, name = "Player 3 (Yellow)", isBot = false),
                Player(color = PlayerColor.BLUE, name = "Player 4 (Blue)", isBot = false)
            )
        }

        _uiState.value = GameState(
            gameMode = mode,
            players = players,
            activePlayerIndex = 0,
            diceValue = 1,
            isDiceRolling = false,
            gameStatus = GameStatus.WAITING_FOR_ROLL,
            consecutiveSixes = 0,
            movableTokenIds = emptyList(),
            statusMessage = "${players.first().name}'s turn to roll!",
            winners = emptyList(),
            currentTheme = currentTheme,
            soundEnabled = currentSound,
            musicEnabled = currentMusic
        )

        checkAndTriggerBotTurn()
    }

    fun toggleSound() {
        val newSoundState = !_uiState.value.soundEnabled
        prefs.edit().putBoolean("sound_enabled", newSoundState).apply()
        audioEngine.isSoundEnabled = newSoundState
        _uiState.update { it.copy(soundEnabled = newSoundState) }
        if (newSoundState) audioEngine.playButtonClickSound()
    }

    fun toggleMusic() {
        val newMusicState = !_uiState.value.musicEnabled
        prefs.edit().putBoolean("music_enabled", newMusicState).apply()
        audioEngine.isMusicEnabled = newMusicState
        _uiState.update { it.copy(musicEnabled = newMusicState) }
        if (_uiState.value.soundEnabled) audioEngine.playButtonClickSound()
    }

    fun selectTheme(theme: BoardTheme) {
        prefs.edit().putInt("selected_theme_type", theme.type.ordinal).apply()
        _uiState.update { it.copy(currentTheme = theme, showThemeSelector = false) }
        if (_uiState.value.soundEnabled) {
            audioEngine.playThemeChangeSound()
        }
    }

    fun openThemeSelector() {
        _uiState.update { it.copy(showThemeSelector = true) }
        if (_uiState.value.soundEnabled) audioEngine.playButtonClickSound()
    }

    fun closeThemeSelector() {
        _uiState.update { it.copy(showThemeSelector = false) }
        if (_uiState.value.soundEnabled) audioEngine.playButtonClickSound()
    }

    fun rollDice() {
        val currentState = _uiState.value
        if (currentState.gameStatus != GameStatus.WAITING_FOR_ROLL || currentState.isDiceRolling) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDiceRolling = true, statusMessage = "Rolling dice...") }

            if (currentState.soundEnabled) {
                audioEngine.playDiceRollSound()
            }

            // Dice roll animation (6 frames)
            for (i in 0 until 6) {
                _uiState.update { it.copy(diceValue = Random.nextInt(1, 7)) }
                delay(60)
            }

            val finalDice = Random.nextInt(1, 7)
            val activePlayer = _uiState.value.activePlayer ?: return@launch

            var currentSixes = _uiState.value.consecutiveSixes
            if (finalDice == 6) {
                currentSixes++
            } else {
                currentSixes = 0
            }

            // Rule: 3 consecutive sixes penalty
            if (currentSixes == 3) {
                _uiState.update {
                    it.copy(
                        diceValue = 6,
                        isDiceRolling = false,
                        consecutiveSixes = 0,
                        statusMessage = "Rolled 3 sixes in a row! Turn lost."
                    )
                }
                delay(1200)
                passTurn()
                return@launch
            }

            val movables = LudoRulesEngine.getMovableTokenIds(activePlayer, finalDice)

            _uiState.update {
                it.copy(
                    diceValue = finalDice,
                    isDiceRolling = false,
                    consecutiveSixes = currentSixes,
                    movableTokenIds = movables
                )
            }

            if (movables.isEmpty()) {
                _uiState.update {
                    it.copy(
                        statusMessage = "Rolled $finalDice. No valid moves available.",
                        gameStatus = GameStatus.TURN_PASSING
                    )
                }
                delay(1000)
                if (finalDice == 6) {
                    _uiState.update {
                        it.copy(
                            statusMessage = "Rolled a 6! Roll again.",
                            gameStatus = GameStatus.WAITING_FOR_ROLL
                        )
                    }
                    checkAndTriggerBotTurn()
                } else {
                    passTurn()
                }
            } else if (movables.size == 1 && activePlayer.isBot) {
                _uiState.update {
                    it.copy(
                        gameStatus = GameStatus.WAITING_FOR_TOKEN_SELECTION,
                        statusMessage = "Moving token..."
                    )
                }
                delay(400)
                moveToken(movables.first())
            } else {
                _uiState.update {
                    it.copy(
                        gameStatus = GameStatus.WAITING_FOR_TOKEN_SELECTION,
                        statusMessage = if (activePlayer.isBot) "Bot calculating tactical move..." else "Tap a highlighted token to move!"
                    )
                }
                if (activePlayer.isBot) {
                    delay(550)
                    val chosenId = LudoAiAgent.chooseBestToken(
                        players = _uiState.value.players,
                        botColor = activePlayer.color,
                        movableTokenIds = movables,
                        diceValue = finalDice
                    )
                    moveToken(chosenId)
                }
            }
        }
    }

    fun onTokenTapped(color: PlayerColor, tokenId: Int) {
        val currentState = _uiState.value
        val activePlayer = currentState.activePlayer ?: return

        if (currentState.gameStatus != GameStatus.WAITING_FOR_TOKEN_SELECTION) return
        if (activePlayer.isBot) return
        if (activePlayer.color != color) return
        if (!currentState.movableTokenIds.contains(tokenId)) return

        moveToken(tokenId)
    }

    private fun moveToken(tokenId: Int) {
        val currentState = _uiState.value
        val activePlayer = currentState.activePlayer ?: return
        val diceVal = currentState.diceValue

        viewModelScope.launch {
            _uiState.update { it.copy(gameStatus = GameStatus.MOVING_TOKEN, movableTokenIds = emptyList()) }

            val token = activePlayer.tokens.find { it.id == tokenId } ?: return@launch
            val startStep = token.step
            val stepsToAnimate = if (token.isInYard) 1 else diceVal

            for (s in 1..stepsToAnimate) {
                val stepVal = if (token.isInYard) 0 else startStep + s
                val tempToken = token.copy(step = stepVal)
                val tempPlayers = _uiState.value.players.map { p ->
                    if (p.color == activePlayer.color) {
                        p.copy(tokens = p.tokens.map { if (it.id == tokenId) tempToken else it })
                    } else p
                }
                _uiState.update { it.copy(players = tempPlayers) }

                if (currentState.soundEnabled) {
                    audioEngine.playTokenStepSound()
                }
                val stepDelay = if (token.isInYard) 400L else 160L
                delay(stepDelay)
            }

            // Process full move with rules engine
            val (updatedPlayers, moveResult) = LudoRulesEngine.processMove(
                players = _uiState.value.players,
                activeColor = activePlayer.color,
                tokenId = tokenId,
                diceValue = diceVal
            )

            val soundOn = _uiState.value.soundEnabled
            when (moveResult) {
                is MoveResult.Capture -> {
                    val finalToken = updatedPlayers.find { it.color == activePlayer.color }?.tokens?.find { it.id == tokenId }
                    val targetStep = finalToken?.step ?: 0
                    val captureGrid = LudoBoardPaths.getTokenCoord(
                        color = activePlayer.color,
                        step = targetStep,
                        yardSpotIndex = 0
                    )

                    _uiState.update {
                        it.copy(
                            activeCaptureEffect = CaptureEffectData(
                                coord = captureGrid,
                                color = moveResult.capturedTokenColor
                            )
                        )
                    }
                    if (soundOn) audioEngine.playCaptureSound()
                    delay(650)
                    _uiState.update { it.copy(activeCaptureEffect = null) }
                }
                is MoveResult.ReachedGoal -> {
                    if (soundOn) audioEngine.playGoalSound()
                }
                else -> {}
            }

            _uiState.update { it.copy(players = updatedPlayers) }

            // Check finished
            val updatedActivePlayer = updatedPlayers.find { it.color == activePlayer.color }
            val currentWinners = _uiState.value.winners.toMutableList()

            if (updatedActivePlayer != null && LudoRulesEngine.hasPlayerFinished(updatedActivePlayer)) {
                if (!currentWinners.contains(activePlayer.color)) {
                    currentWinners.add(activePlayer.color)
                }
            }

            val activeRemainingPlayers = updatedPlayers.filter { !LudoRulesEngine.hasPlayerFinished(it) }
            val isGameOver = (updatedPlayers.size > 1 && activeRemainingPlayers.size <= 1) ||
                    (updatedPlayers.size == 1 && activeRemainingPlayers.isEmpty())

            if (isGameOver) {
                if (soundOn) audioEngine.playVictorySound()
                _uiState.update {
                    it.copy(
                        gameStatus = GameStatus.GAME_OVER,
                        winners = currentWinners,
                        statusMessage = "👑 Victory! ${currentWinners.firstOrNull()?.displayName ?: activePlayer.color.displayName} Wins!"
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(winners = currentWinners) }

            val hasActivePlayerFinished = updatedActivePlayer != null && LudoRulesEngine.hasPlayerFinished(updatedActivePlayer)
            val getsExtraTurn = LudoRulesEngine.givesExtraRoll(diceVal, moveResult) && !hasActivePlayerFinished
            if (getsExtraTurn) {
                val reasonText = when {
                    diceVal == 6 -> "Rolled a 6! Roll again."
                    moveResult is MoveResult.Capture -> "Captured an opponent! Extra turn!"
                    moveResult is MoveResult.ReachedGoal -> "Reached Goal! Extra turn!"
                    else -> "Bonus turn!"
                }
                _uiState.update {
                    it.copy(
                        gameStatus = GameStatus.WAITING_FOR_ROLL,
                        statusMessage = reasonText
                    )
                }
                checkAndTriggerBotTurn()
            } else {
                _uiState.update { it.copy(consecutiveSixes = 0) }
                passTurn()
            }
        }
    }

    private fun passTurn() {
        val currentState = _uiState.value
        if (currentState.players.isEmpty()) return

        var nextIdx = (currentState.activePlayerIndex + 1) % currentState.players.size

        var safetyCount = 0
        while (currentState.players[nextIdx].isFinished && safetyCount < currentState.players.size) {
            nextIdx = (nextIdx + 1) % currentState.players.size
            safetyCount++
        }

        val nextPlayer = currentState.players[nextIdx]
        _uiState.update {
            it.copy(
                activePlayerIndex = nextIdx,
                gameStatus = GameStatus.WAITING_FOR_ROLL,
                consecutiveSixes = 0,
                movableTokenIds = emptyList(),
                statusMessage = "${nextPlayer.name}'s turn to roll!"
            )
        }

        checkAndTriggerBotTurn()
    }

    private fun checkAndTriggerBotTurn() {
        val currentState = _uiState.value
        val activePlayer = currentState.activePlayer ?: return

        if (activePlayer.isBot && currentState.gameStatus == GameStatus.WAITING_FOR_ROLL) {
            viewModelScope.launch {
                delay(600)
                rollDice()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stopMusicLoop()
    }
}
