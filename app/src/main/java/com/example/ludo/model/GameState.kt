package com.example.ludo.model

import com.example.ludo.engine.GridCoord

sealed class MoveResult {
    data object Normal : MoveResult()
    data class Capture(val capturedTokenColor: PlayerColor, val capturedTokenId: Int) : MoveResult()
    data class ReachedGoal(val tokenId: Int) : MoveResult()
    data class ExtraRoll(val reason: String) : MoveResult()
    data class InvalidMove(val reason: String) : MoveResult()
    data class GameFinished(val winner: PlayerColor, val rankings: List<PlayerColor>) : MoveResult()
}

enum class GameStatus {
    WAITING_FOR_ROLL,
    WAITING_FOR_TOKEN_SELECTION,
    MOVING_TOKEN,
    TURN_PASSING,
    GAME_OVER
}

data class CaptureEffectData(
    val coord: GridCoord,
    val color: PlayerColor,
    val timestamp: Long = System.currentTimeMillis()
)

data class GameState(
    val gameMode: GameMode = GameMode.PASS_AND_PLAY_2P,
    val players: List<Player> = emptyList(),
    val activePlayerIndex: Int = 0,
    val diceValue: Int = 1,
    val isDiceRolling: Boolean = false,
    val gameStatus: GameStatus = GameStatus.WAITING_FOR_ROLL,
    val consecutiveSixes: Int = 0,
    val movableTokenIds: List<Int> = emptyList(),
    val statusMessage: String = "",
    val winners: List<PlayerColor> = emptyList(),
    val lastMoveDescription: String? = null,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val currentTheme: BoardTheme = BoardTheme.ROYAL,
    val showThemeSelector: Boolean = false,
    val activeCaptureEffect: CaptureEffectData? = null,
    val selectedTokenId: Int? = null,
    val animateTokenId: Int? = null,
    val animateFromStep: Int? = null,
    val animateToStep: Int? = null
) {
    val activePlayer: Player? get() = players.getOrNull(activePlayerIndex)
}
