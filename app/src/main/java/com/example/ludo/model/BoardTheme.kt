package com.example.ludo.model

import androidx.compose.ui.graphics.Color

enum class ThemeType {
    ROYAL,
    CLASSIC,
    GALAXY,
    FOREST,
    ICE,
    LAVA,
    BEACH,
    CANDY,
    PIRATE,
    SAKURA
}

data class BoardTheme(
    val type: ThemeType,
    val displayName: String,
    val description: String,
    val emoji: String,
    val boardBgColor: Color,
    val gridLineColor: Color,
    val outerBorderColor: Color,
    val outerBorderSecondaryColor: Color,
    val innerYardBgColor: Color,
    val starColor: Color,
    val starBorderColor: Color,
    val centerTriangleBgColor: Color,
    val isDarkTheme: Boolean = true,
    // Accent override colors for players if theme customizes them
    val redColor: Color = Color(0xFFE53935),
    val greenColor: Color = Color(0xFF4CAF50),
    val yellowColor: Color = Color(0xFFFBC02D),
    val blueColor: Color = Color(0xFF1E88E5)
) {
    companion object {
        val ROYAL = BoardTheme(
            type = ThemeType.ROYAL,
            displayName = "Royal Empire",
            description = "Velvet royal purple with glowing gold frame & crown accents",
            emoji = "👑",
            boardBgColor = Color(0xFF1E172A),
            gridLineColor = Color(0xFF332946),
            outerBorderColor = Color(0xFFFFD700),
            outerBorderSecondaryColor = Color(0xFFB8860B),
            innerYardBgColor = Color(0xFF2D2342),
            starColor = Color(0xFFFFD700),
            starBorderColor = Color(0xFF8B6508),
            centerTriangleBgColor = Color(0xFF191224),
            isDarkTheme = true,
            redColor = Color(0xFFE52B50),
            greenColor = Color(0xFF00C853),
            yellowColor = Color(0xFFFFD700),
            blueColor = Color(0xFF2979FF)
        )

        val CLASSIC = BoardTheme(
            type = ThemeType.CLASSIC,
            displayName = "Classic Ludo",
            description = "Traditional clean ivory board with bold primary colors",
            emoji = "🎲",
            boardBgColor = Color(0xFFFAF9F6),
            gridLineColor = Color(0xFFE0E0E0),
            outerBorderColor = Color(0xFF37474F),
            outerBorderSecondaryColor = Color(0xFF212121),
            innerYardBgColor = Color(0xFFFFFFFF),
            starColor = Color(0xFFFFA000),
            starBorderColor = Color(0xFF5D4037),
            centerTriangleBgColor = Color(0xFFF5F5F5),
            isDarkTheme = false,
            redColor = Color(0xFFE53935),
            greenColor = Color(0xFF4CAF50),
            yellowColor = Color(0xFFFBC02D),
            blueColor = Color(0xFF1E88E5)
        )

        val GALAXY = BoardTheme(
            type = ThemeType.GALAXY,
            displayName = "Nebula Galaxy",
            description = "Cosmic space with cyan stardust & glowing neon tracks",
            emoji = "🌌",
            boardBgColor = Color(0xFF0B0E1B),
            gridLineColor = Color(0xFF1F2942),
            outerBorderColor = Color(0xFF00E5FF),
            outerBorderSecondaryColor = Color(0xFF7C4DFF),
            innerYardBgColor = Color(0xFF12182B),
            starColor = Color(0xFF00E5FF),
            starBorderColor = Color(0xFF006064),
            centerTriangleBgColor = Color(0xFF070A14),
            isDarkTheme = true,
            redColor = Color(0xFFFF1744),
            greenColor = Color(0xFF00E676),
            yellowColor = Color(0xFFFFEA00),
            blueColor = Color(0xFF00B0FF)
        )

        val FOREST = BoardTheme(
            type = ThemeType.FOREST,
            displayName = "Enchanted Forest",
            description = "Emerald moss, mahogany wood trim & blossom stars",
            emoji = "🌲",
            boardBgColor = Color(0xFF1B2A1C),
            gridLineColor = Color(0xFF2D422E),
            outerBorderColor = Color(0xFF8D6E63),
            outerBorderSecondaryColor = Color(0xFF4E342E),
            innerYardBgColor = Color(0xFF243825),
            starColor = Color(0xFFAEEA00),
            starBorderColor = Color(0xFF33691E),
            centerTriangleBgColor = Color(0xFF131F14),
            isDarkTheme = true,
            redColor = Color(0xFFE64A19),
            greenColor = Color(0xFF2E7D32),
            yellowColor = Color(0xFFF57F17),
            blueColor = Color(0xFF0288D1)
        )

        val ICE = BoardTheme(
            type = ThemeType.ICE,
            displayName = "Glacier Frost",
            description = "Frozen crystal ice tiles with silver star engravings",
            emoji = "❄️",
            boardBgColor = Color(0xFFE0F7FA),
            gridLineColor = Color(0xFFB2EBF2),
            outerBorderColor = Color(0xFF00838F),
            outerBorderSecondaryColor = Color(0xFF006064),
            innerYardBgColor = Color(0xFFF1F8E9),
            starColor = Color(0xFF00BCD4),
            starBorderColor = Color(0xFF004D40),
            centerTriangleBgColor = Color(0xFFE0F2F1),
            isDarkTheme = false,
            redColor = Color(0xFFFF5252),
            greenColor = Color(0xFF26A69A),
            yellowColor = Color(0xFFFFCA28),
            blueColor = Color(0xFF29B6F6)
        )

        val LAVA = BoardTheme(
            type = ThemeType.LAVA,
            displayName = "Volcanic Lava",
            description = "Obsidian dark basalt with fiery molten magma tracks",
            emoji = "🌋",
            boardBgColor = Color(0xFF121212),
            gridLineColor = Color(0xFF2C2C2C),
            outerBorderColor = Color(0xFFFF3D00),
            outerBorderSecondaryColor = Color(0xFFDD2C00),
            innerYardBgColor = Color(0xFF1E1E1E),
            starColor = Color(0xFFFF6D00),
            starBorderColor = Color(0xFFBF360C),
            centerTriangleBgColor = Color(0xFF0A0A0A),
            isDarkTheme = true,
            redColor = Color(0xFFFF3D00),
            greenColor = Color(0xFF00E676),
            yellowColor = Color(0xFFFFD600),
            blueColor = Color(0xFF3D5AFE)
        )

        val BEACH = BoardTheme(
            type = ThemeType.BEACH,
            displayName = "Tropical Beach",
            description = "Golden island sand background with ocean turquoise water",
            emoji = "🏖️",
            boardBgColor = Color(0xFFFFF8E1),
            gridLineColor = Color(0xFFFFECB3),
            outerBorderColor = Color(0xFF00ACC1),
            outerBorderSecondaryColor = Color(0xFF00838F),
            innerYardBgColor = Color(0xFFFFFFFF),
            starColor = Color(0xFFFFB300),
            starBorderColor = Color(0xFFE65100),
            centerTriangleBgColor = Color(0xFFFFF3E0),
            isDarkTheme = false,
            redColor = Color(0xFFFF5252),
            greenColor = Color(0xFF00E676),
            yellowColor = Color(0xFFFFB300),
            blueColor = Color(0xFF00B0FF)
        )

        val CANDY = BoardTheme(
            type = ThemeType.CANDY,
            displayName = "Sweet Candy",
            description = "Pastel marshmallow, cotton candy & minty sugar tiles",
            emoji = "🍬",
            boardBgColor = Color(0xFFFFF0F5),
            gridLineColor = Color(0xFFFFD1DC),
            outerBorderColor = Color(0xFFFF69B4),
            outerBorderSecondaryColor = Color(0xFFBA55D3),
            innerYardBgColor = Color(0xFFFFFFFF),
            starColor = Color(0xFFFF1493),
            starBorderColor = Color(0xFFC71585),
            centerTriangleBgColor = Color(0xFFFAEAFF),
            isDarkTheme = false,
            redColor = Color(0xFFFF6B81),
            greenColor = Color(0xFF7BED9F),
            yellowColor = Color(0xFFECCC68),
            blueColor = Color(0xFF70A1FF)
        )

        val PIRATE = BoardTheme(
            type = ThemeType.PIRATE,
            displayName = "Treasure Map",
            description = "Aged parchment scroll with gold doubloons & compass map",
            emoji = "🏴‍☠️",
            boardBgColor = Color(0xFF2D241E),
            gridLineColor = Color(0xFF42352C),
            outerBorderColor = Color(0xFFD4AF37),
            outerBorderSecondaryColor = Color(0xFF8B5A2B),
            innerYardBgColor = Color(0xFF3D3028),
            starColor = Color(0xFFFFD700),
            starBorderColor = Color(0xFF8B4513),
            centerTriangleBgColor = Color(0xFF231B16),
            isDarkTheme = true,
            redColor = Color(0xFFC62828),
            greenColor = Color(0xFF388E3C),
            yellowColor = Color(0xFFFBC02D),
            blueColor = Color(0xFF1565C0)
        )

        val SAKURA = BoardTheme(
            type = ThemeType.SAKURA,
            displayName = "Sakura Garden",
            description = "Japanese cherry blossom pinks with gentle floral petals",
            emoji = "🌸",
            boardBgColor = Color(0xFF2B1F27),
            gridLineColor = Color(0xFF42303C),
            outerBorderColor = Color(0xFFFFB7C5),
            outerBorderSecondaryColor = Color(0xFFDB7093),
            innerYardBgColor = Color(0xFF3B2A35),
            starColor = Color(0xFFFF69B4),
            starBorderColor = Color(0xFF8B008B),
            centerTriangleBgColor = Color(0xFF20161D),
            isDarkTheme = true,
            redColor = Color(0xFFE91E63),
            greenColor = Color(0xFF66BB6A),
            yellowColor = Color(0xFFFFCA28),
            blueColor = Color(0xFF42A5F5)
        )

        val ALL_THEMES = listOf(
            ROYAL, CLASSIC, GALAXY, FOREST, ICE, LAVA, BEACH, CANDY, PIRATE, SAKURA
        )

        fun getByOrdinal(ordinal: Int): BoardTheme {
            return ALL_THEMES.getOrElse(ordinal) { ROYAL }
        }

        fun getByType(type: ThemeType): BoardTheme {
            return ALL_THEMES.find { it.type == type } ?: ROYAL
        }
    }
}
