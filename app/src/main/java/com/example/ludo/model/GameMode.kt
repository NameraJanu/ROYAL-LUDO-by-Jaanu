package com.example.ludo.model

enum class GameMode(val title: String, val description: String) {
    VS_COMPUTER("Play vs Computer", "Single player vs intelligent AI bots"),
    PASS_AND_PLAY_2P("Pass & Play (2 Players)", "Local 2-player game on one device"),
    PASS_AND_PLAY_4P("Pass & Play (4 Players)", "Local 4-player game on one device")
}
