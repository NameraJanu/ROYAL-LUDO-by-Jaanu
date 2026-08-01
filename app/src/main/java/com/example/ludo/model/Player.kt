package com.example.ludo.model

data class Player(
    val color: PlayerColor,
    val name: String,
    val isBot: Boolean = false,
    val tokens: List<Token> = List(4) { id -> Token(id = id, color = color) },
    val rank: Int = 0                // 1st, 2nd, 3rd, 4th place
) {
    val isFinished: Boolean get() = tokens.all { it.isHomeGoal }
    val tokensInYard: Int get() = tokens.count { it.isInYard }
    val tokensHome: Int get() = tokens.count { it.isHomeGoal }
}
