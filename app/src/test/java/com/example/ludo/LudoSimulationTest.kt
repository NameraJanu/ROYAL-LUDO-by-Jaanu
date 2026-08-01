package com.example.ludo

import com.example.ludo.engine.LudoAiAgent
import com.example.ludo.engine.LudoBoardPaths
import com.example.ludo.engine.LudoRulesEngine
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.GameMode
import com.example.ludo.model.MoveResult
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LudoSimulationTest {

    @Test
    fun test100CompleteMatchesSimulation() {
        val gameModes = listOf(
            GameMode.VS_COMPUTER,
            GameMode.PASS_AND_PLAY_2P,
            GameMode.PASS_AND_PLAY_4P
        )
        val themes = BoardTheme.ALL_THEMES

        var totalMatchesPlayed = 0
        var totalMovesExecuted = 0
        var totalCapturesOccurred = 0
        var totalGoalsReached = 0

        for (matchIndex in 1..100) {
            val selectedMode = gameModes[matchIndex % gameModes.size]
            val selectedTheme = themes[matchIndex % themes.size]

            val initialPlayers = createPlayersForMode(selectedMode)
            var currentPlayers = initialPlayers
            var activePlayerIdx = 0
            var consecutiveSixes = 0
            val winners = mutableListOf<PlayerColor>()

            var turnCount = 0
            val maxTurnsSafety = 3000

            while (winners.size < currentPlayers.size - 1 && turnCount < maxTurnsSafety) {
                turnCount++
                val activePlayer = currentPlayers[activePlayerIdx]

                // If player is already finished, skip turn
                if (LudoRulesEngine.hasPlayerFinished(activePlayer)) {
                    activePlayerIdx = (activePlayerIdx + 1) % currentPlayers.size
                    continue
                }

                val diceRoll = Random.nextInt(1, 7)

                if (diceRoll == 6) {
                    consecutiveSixes++
                } else {
                    consecutiveSixes = 0
                }

                // 3 sixes penalty
                if (consecutiveSixes == 3) {
                    consecutiveSixes = 0
                    activePlayerIdx = (activePlayerIdx + 1) % currentPlayers.size
                    continue
                }

                val movables = LudoRulesEngine.getMovableTokenIds(activePlayer, diceRoll)

                if (movables.isEmpty()) {
                    if (diceRoll != 6) {
                        activePlayerIdx = (activePlayerIdx + 1) % currentPlayers.size
                    }
                    continue
                }

                // Choose token
                val chosenTokenId = if (activePlayer.isBot) {
                    LudoAiAgent.chooseBestToken(currentPlayers, activePlayer.color, movables, diceRoll)
                } else {
                    movables.first()
                }

                assertTrue("Chosen token ID must be in movable list", movables.contains(chosenTokenId))

                // Execute move
                val (updatedPlayers, moveResult) = LudoRulesEngine.processMove(
                    players = currentPlayers,
                    activeColor = activePlayer.color,
                    tokenId = chosenTokenId,
                    diceValue = diceRoll
                )

                totalMovesExecuted++
                currentPlayers = updatedPlayers

                // Verify state consistency
                val movedPlayer = currentPlayers.first { it.color == activePlayer.color }
                val movedToken = movedPlayer.tokens.first { it.id == chosenTokenId }

                assertTrue("Token step must not exceed 56", movedToken.step <= 56)

                // Verify coordinates rendering
                val coord = LudoBoardPaths.getTokenCoord(
                    color = activePlayer.color,
                    step = movedToken.step,
                    yardSpotIndex = movedToken.id
                )
                assertNotNull("Coord must not be null", coord)
                assertTrue("X coord in 0..14", coord.x in 0..14)
                assertTrue("Y coord in 0..14", coord.y in 0..14)

                when (moveResult) {
                    is MoveResult.Capture -> {
                        totalCapturesOccurred++
                        val capturedPlayer = currentPlayers.first { it.color == moveResult.capturedTokenColor }
                        val capturedToken = capturedPlayer.tokens.first { it.id == moveResult.capturedTokenId }
                        assertEquals("Captured token must return to yard (-1)", -1, capturedToken.step)
                    }
                    is MoveResult.ReachedGoal -> {
                        totalGoalsReached++
                        assertEquals("Reached goal token must have step 56", 56, movedToken.step)
                    }
                    is MoveResult.InvalidMove -> {
                        throw IllegalStateException("Invalid move executed in simulation: ${moveResult.reason}")
                    }
                    else -> {}
                }

                if (LudoRulesEngine.hasPlayerFinished(movedPlayer)) {
                    if (!winners.contains(movedPlayer.color)) {
                        winners.add(movedPlayer.color)
                    }
                }

                // Check game over
                val activeRemaining = currentPlayers.filter { !LudoRulesEngine.hasPlayerFinished(it) }
                if (activeRemaining.size <= 1) {
                    break
                }

                val getsExtraTurn = LudoRulesEngine.givesExtraRoll(diceRoll, moveResult) &&
                        !LudoRulesEngine.hasPlayerFinished(movedPlayer)

                if (!getsExtraTurn) {
                    consecutiveSixes = 0
                    activePlayerIdx = (activePlayerIdx + 1) % currentPlayers.size
                }
            }

            totalMatchesPlayed++
            assertTrue("Match $matchIndex should produce at least 1 winner", winners.isNotEmpty())
        }

        assertEquals("Should complete 100 simulated matches", 100, totalMatchesPlayed)
        assertTrue("Total moves should be > 1000", totalMovesExecuted > 1000)
        assertTrue("Total captures should be > 0", totalCapturesOccurred > 0)
        assertTrue("Total goals reached should be > 0", totalGoalsReached > 0)
    }

    private fun createPlayersForMode(mode: GameMode): List<Player> {
        return when (mode) {
            GameMode.VS_COMPUTER -> listOf(
                Player(PlayerColor.RED, "Player 1", isBot = false),
                Player(PlayerColor.GREEN, "Bot Green", isBot = true),
                Player(PlayerColor.YELLOW, "Bot Yellow", isBot = true),
                Player(PlayerColor.BLUE, "Bot Blue", isBot = true)
            )
            GameMode.PASS_AND_PLAY_2P -> listOf(
                Player(PlayerColor.RED, "Player 1", isBot = false),
                Player(PlayerColor.GREEN, "Player 2", isBot = false)
            )
            GameMode.PASS_AND_PLAY_4P -> listOf(
                Player(PlayerColor.RED, "Player 1", isBot = false),
                Player(PlayerColor.GREEN, "Player 2", isBot = false),
                Player(PlayerColor.YELLOW, "Player 3", isBot = false),
                Player(PlayerColor.BLUE, "Player 4", isBot = false)
            )
        }
    }
}
