package com.example.ludo.engine

import com.example.ludo.model.MoveResult
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token

object LudoRulesEngine {

    /**
     * Finds token IDs for the player that can legally move given the dice roll.
     */
    fun getMovableTokenIds(player: Player, diceValue: Int): List<Int> {
        val movables = mutableListOf<Int>()
        for (token in player.tokens) {
            if (token.isHomeGoal) continue
            if (token.isInYard) {
                if (diceValue == 6) {
                    movables.add(token.id)
                }
            } else {
                val nextStep = token.step + diceValue
                if (nextStep <= 56) {
                    movables.add(token.id)
                }
            }
        }
        return movables
    }

    /**
     * Executes a move for the specified token ID and returns the updated players list + MoveResult.
     */
    fun processMove(
        players: List<Player>,
        activeColor: PlayerColor,
        tokenId: Int,
        diceValue: Int
    ): Pair<List<Player>, MoveResult> {
        val activePlayer = players.find { it.color == activeColor }
            ?: return Pair(players, MoveResult.InvalidMove("Active player not found"))

        val token = activePlayer.tokens.find { it.id == tokenId }
            ?: return Pair(players, MoveResult.InvalidMove("Token not found"))

        // Calculate next step
        val nextStep = if (token.isInYard) {
            if (diceValue == 6) 0 else return Pair(players, MoveResult.InvalidMove("Need 6 to leave yard"))
        } else {
            token.step + diceValue
        }

        if (nextStep > 56) {
            return Pair(players, MoveResult.InvalidMove("Move exceeds goal target"))
        }

        val updatedToken = token.copy(step = nextStep)
        var capturedTokenColor: PlayerColor? = null
        var capturedTokenId: Int? = null

        // Check capture logic if landing on main track (step in 0..50)
        var updatedPlayers = players.map { player ->
            if (player.color == activeColor) {
                player.copy(tokens = player.tokens.map { if (it.id == tokenId) updatedToken else it })
            } else player
        }

        if (nextStep in 0..50 && !LudoBoardPaths.isSafeTile(activeColor, nextStep)) {
            val targetMainTrackIndex = updatedToken.getMainTrackIndex()
            if (targetMainTrackIndex != null) {
                // Check if any opponent token is on this main track tile
                for (otherPlayer in updatedPlayers) {
                    if (otherPlayer.color == activeColor) continue
                    for (otherToken in otherPlayer.tokens) {
                        if (otherToken.isOnTrack && otherToken.getMainTrackIndex() == targetMainTrackIndex) {
                            // Captured!
                            capturedTokenColor = otherPlayer.color
                            capturedTokenId = otherToken.id
                            break
                        }
                    }
                    if (capturedTokenColor != null) break
                }
            }
        }

        // Apply capture resetting if an opponent was captured
        if (capturedTokenColor != null && capturedTokenId != null) {
            updatedPlayers = updatedPlayers.map { player ->
                if (player.color == capturedTokenColor) {
                    player.copy(tokens = player.tokens.map {
                        if (it.id == capturedTokenId) it.copy(step = -1) else it
                    })
                } else player
            }
        }

        // Determine MoveResult
        val moveResult = when {
            capturedTokenColor != null && capturedTokenId != null -> {
                MoveResult.Capture(capturedTokenColor, capturedTokenId)
            }
            nextStep == 56 -> {
                MoveResult.ReachedGoal(tokenId)
            }
            else -> MoveResult.Normal
        }

        return Pair(updatedPlayers, moveResult)
    }

    /**
     * Checks if active player gets an extra roll.
     */
    fun givesExtraRoll(diceValue: Int, moveResult: MoveResult): Boolean {
        return diceValue == 6 ||
                moveResult is MoveResult.Capture ||
                moveResult is MoveResult.ReachedGoal ||
                moveResult is MoveResult.ExtraRoll
    }

    /**
     * Returns true if all tokens of a player have reached the center goal (step >= 56).
     */
    fun hasPlayerFinished(player: Player): Boolean {
        return player.tokens.all { it.isHomeGoal }
    }
}
