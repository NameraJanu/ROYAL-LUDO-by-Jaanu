package com.example.ludo.engine

import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import kotlin.random.Random

object LudoAiAgent {

    /**
     * Chooses the best token ID to move for an AI player given moveable tokens and dice value.
     * Uses tactical heuristic scoring (captures, safe tiles, escapes, yard exit, goal line).
     */
    fun chooseBestToken(
        players: List<Player>,
        botColor: PlayerColor,
        movableTokenIds: List<Int>,
        diceValue: Int
    ): Int {
        if (movableTokenIds.isEmpty()) return -1
        if (movableTokenIds.size == 1) return movableTokenIds.first()

        val botPlayer = players.find { it.color == botColor } ?: return movableTokenIds.first()

        var bestTokenId = movableTokenIds.first()
        var highestScore = Int.MIN_VALUE

        for (tokenId in movableTokenIds) {
            val token = botPlayer.tokens.find { it.id == tokenId } ?: continue
            var score = 0

            val currentStep = if (token.isInYard) -1 else token.step
            val nextStep = if (token.isInYard) 0 else token.step + diceValue

            // 1. Entering Goal (step = 56) -> Huge priority
            if (nextStep == 56) {
                score += 1000
            }

            // 2. Capturing an opponent token -> High priority
            if (nextStep in 0..50 && !LudoBoardPaths.isSafeTile(botColor, nextStep)) {
                val targetMainIdx = (botColor.startIndex + nextStep) % 52
                val canCapture = players.any { p ->
                    p.color != botColor && p.tokens.any { t ->
                        t.isOnTrack && t.getMainTrackIndex() == targetMainIdx
                    }
                }
                if (canCapture) {
                    score += 800
                }
            }

            // 3. Exiting Yard on rolling a 6
            if (token.isInYard && diceValue == 6) {
                val tokensActiveCount = botPlayer.tokens.count { it.isOnTrack }
                score += if (tokensActiveCount < 2) 700 else 450
            }

            // 4. Escaping danger (if opponent is 1..6 tiles behind current position)
            if (token.isOnTrack && currentStep in 0..50 && !LudoBoardPaths.isSafeTile(botColor, currentStep)) {
                val currentMainIdx = token.getMainTrackIndex()
                if (currentMainIdx != null) {
                    val isThreatened = players.any { p ->
                        p.color != botColor && p.tokens.any { oppToken ->
                            if (!oppToken.isOnTrack) return@any false
                            val oppIdx = oppToken.getMainTrackIndex() ?: return@any false
                            val dist = (currentMainIdx - oppIdx + 52) % 52
                            dist in 1..6
                        }
                    }
                    if (isThreatened) {
                        score += 500
                    }
                }
            }

            // 5. Landing on Safe Star tile
            if (nextStep in 0..50 && LudoBoardPaths.isSafeTile(botColor, nextStep)) {
                score += 350
            }

            // 6. Entering Home Stretch (step >= 51)
            if (currentStep < 51 && nextStep in 51..55) {
                score += 400
            }

            // 7. Progress reward (favor tokens closer to goal)
            score += nextStep * 5

            if (score > highestScore) {
                highestScore = score
                bestTokenId = tokenId
            }
        }

        return bestTokenId
    }
}
