package com.example.ludo.model

data class Token(
    val id: Int,                    // 0..3 for each player
    val color: PlayerColor,
    val step: Int = -1,             // -1: Yard, 0..50: Main Track, 51..55: Home Stretch, 56: Home Goal
    val yardSpotIndex: Int = id     // 0..3 position inside home yard circle
) {
    val isInYard: Boolean get() = step == -1
    val isOnTrack: Boolean get() = step in 0..50
    val isInHomeStretch: Boolean get() = step in 51..55
    val isHomeGoal: Boolean get() = step >= 56

    // Calculates the absolute 0..51 main track index given player's start index
    fun getMainTrackIndex(): Int? {
        if (!isOnTrack) return null
        return (color.startIndex + step) % 52
    }
}
