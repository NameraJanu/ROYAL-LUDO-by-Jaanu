package com.example.ludo.model

import androidx.compose.ui.graphics.Color

enum class PlayerColor(
    val displayName: String,
    val color: Color,
    val darkColor: Color,
    val lightColor: Color,
    val startIndex: Int,        // Index on 52-tile main track
    val yardXOffset: Float,      // 0.0 for left, 1.0 for right
    val yardYOffset: Float       // 0.0 for top, 1.0 for bottom
) {
    RED(
        displayName = "Red",
        color = Color(0xFFE53935),
        darkColor = Color(0xFFB71C1C),
        lightColor = Color(0xFFFFEBEE),
        startIndex = 0,
        yardXOffset = 0f,
        yardYOffset = 0f
    ),
    GREEN(
        displayName = "Green",
        color = Color(0xFF4CAF50),
        darkColor = Color(0xFF1B5E20),
        lightColor = Color(0xFFE8F5E9),
        startIndex = 13,
        yardXOffset = 1f,
        yardYOffset = 0f
    ),
    YELLOW(
        displayName = "Yellow",
        color = Color(0xFFFBC02D),
        darkColor = Color(0xFFF57F17),
        lightColor = Color(0xFFFFFDE7),
        startIndex = 26,
        yardXOffset = 1f,
        yardYOffset = 1f
    ),
    BLUE(
        displayName = "Blue",
        color = Color(0xFF1E88E5),
        darkColor = Color(0xFF0D47A1),
        lightColor = Color(0xFFE3F2FD),
        startIndex = 39,
        yardXOffset = 0f,
        yardYOffset = 1f
    );

    fun getThemeColor(theme: BoardTheme): Color = when (this) {
        RED -> theme.redColor
        GREEN -> theme.greenColor
        YELLOW -> theme.yellowColor
        BLUE -> theme.blueColor
    }
}
