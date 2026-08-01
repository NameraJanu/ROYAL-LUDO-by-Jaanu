package com.example.ludo.engine

import androidx.compose.ui.geometry.Offset
import com.example.ludo.model.PlayerColor

data class GridCoord(val x: Int, val y: Int)

object LudoBoardPaths {

    // 52 Main Track Tiles (0..51)
    val MAIN_PATH_COORDS = listOf(
        GridCoord(1, 6),  // 0: Red Start
        GridCoord(2, 6),  // 1
        GridCoord(3, 6),  // 2
        GridCoord(4, 6),  // 3
        GridCoord(5, 6),  // 4
        GridCoord(6, 5),  // 5
        GridCoord(6, 4),  // 6
        GridCoord(6, 3),  // 7
        GridCoord(6, 2),  // 8: Safe Star (Top Left)
        GridCoord(6, 1),  // 9
        GridCoord(6, 0),  // 10
        GridCoord(7, 0),  // 11
        GridCoord(8, 0),  // 12
        GridCoord(8, 1),  // 13: Green Start
        GridCoord(8, 2),  // 14
        GridCoord(8, 3),  // 15
        GridCoord(8, 4),  // 16
        GridCoord(8, 5),  // 17
        GridCoord(9, 6),  // 18
        GridCoord(10, 6), // 19
        GridCoord(11, 6), // 20
        GridCoord(12, 6), // 21: Safe Star (Top Right)
        GridCoord(13, 6), // 22
        GridCoord(14, 6), // 23
        GridCoord(14, 7), // 24
        GridCoord(14, 8), // 25
        GridCoord(13, 8), // 26: Yellow Start
        GridCoord(12, 8), // 27
        GridCoord(11, 8), // 28
        GridCoord(10, 8), // 29
        GridCoord(9, 8),  // 30
        GridCoord(8, 9),  // 31
        GridCoord(8, 10), // 32
        GridCoord(8, 11), // 33
        GridCoord(8, 12), // 34: Safe Star (Bottom Right)
        GridCoord(8, 13), // 35
        GridCoord(8, 14), // 36
        GridCoord(7, 14), // 37
        GridCoord(6, 14), // 38
        GridCoord(6, 13), // 39: Blue Start
        GridCoord(6, 12), // 40
        GridCoord(6, 11), // 41
        GridCoord(6, 10), // 42
        GridCoord(6, 9),  // 43
        GridCoord(5, 8),  // 44
        GridCoord(4, 8),  // 45
        GridCoord(3, 8),  // 46
        GridCoord(2, 8),  // 47: Safe Star (Bottom Left)
        GridCoord(1, 8),  // 48
        GridCoord(0, 8),  // 49
        GridCoord(0, 7),  // 50
        GridCoord(0, 6)   // 51
    )

    // Indices of safe star tiles on main track
    val SAFE_TILE_INDICES = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    // Starting square index for each color on the main track
    val START_INDICES = mapOf(
        PlayerColor.RED to 0,
        PlayerColor.GREEN to 13,
        PlayerColor.YELLOW to 26,
        PlayerColor.BLUE to 39
    )

    // Home Stretch Tiles (5 tiles per color)
    val HOME_STRETCH_COORDS = mapOf(
        PlayerColor.RED to listOf(
            GridCoord(1, 7), GridCoord(2, 7), GridCoord(3, 7), GridCoord(4, 7), GridCoord(5, 7)
        ),
        PlayerColor.GREEN to listOf(
            GridCoord(7, 1), GridCoord(7, 2), GridCoord(7, 3), GridCoord(7, 4), GridCoord(7, 5)
        ),
        PlayerColor.YELLOW to listOf(
            GridCoord(13, 7), GridCoord(12, 7), GridCoord(11, 7), GridCoord(10, 7), GridCoord(9, 7)
        ),
        PlayerColor.BLUE to listOf(
            GridCoord(7, 13), GridCoord(7, 12), GridCoord(7, 11), GridCoord(7, 10), GridCoord(7, 9)
        )
    )

    // Center Goal Center Coordinates
    val CENTER_GOAL_COORD = GridCoord(7, 7)

    // Yard Spot Coordinates for 4 tokens per color (x, y inside 15x15 grid)
    val YARD_SPOT_COORDS = mapOf(
        PlayerColor.RED to listOf(
            GridCoord(1, 1), GridCoord(4, 1), GridCoord(1, 4), GridCoord(4, 4)
        ),
        PlayerColor.GREEN to listOf(
            GridCoord(10, 1), GridCoord(13, 1), GridCoord(10, 4), GridCoord(13, 4)
        ),
        PlayerColor.YELLOW to listOf(
            GridCoord(10, 10), GridCoord(13, 10), GridCoord(10, 13), GridCoord(13, 13)
        ),
        PlayerColor.BLUE to listOf(
            GridCoord(1, 10), GridCoord(4, 10), GridCoord(1, 13), GridCoord(4, 13)
        )
    )

    /**
     * Converts a player token's step into a 15x15 grid coordinate.
     * step = -1: Yard spot
     * step = 0..50: Main track
     * step = 51..55: Home stretch
     * step >= 56: Home center goal
     */
    fun getTokenCoord(color: PlayerColor, step: Int, yardSpotIndex: Int): GridCoord {
        return when {
            step == -1 -> YARD_SPOT_COORDS[color]?.getOrNull(yardSpotIndex) ?: GridCoord(1, 1)
            step in 0..50 -> {
                val startIdx = START_INDICES[color] ?: 0
                val mainTrackIdx = (startIdx + step) % 52
                MAIN_PATH_COORDS[mainTrackIdx]
            }
            step in 51..55 -> {
                val stretchIdx = step - 51
                HOME_STRETCH_COORDS[color]?.getOrNull(stretchIdx) ?: CENTER_GOAL_COORD
            }
            else -> CENTER_GOAL_COORD
        }
    }

    /**
     * Determines if a step on main track is a safe tile for that token.
     */
    fun isSafeTile(color: PlayerColor, step: Int): Boolean {
        if (step in 51..56) return true // Home stretch is always safe
        if (step in 0..50) {
            val startIdx = START_INDICES[color] ?: 0
            val mainTrackIdx = (startIdx + step) % 52
            return SAFE_TILE_INDICES.contains(mainTrackIdx)
        }
        return false
    }
}
