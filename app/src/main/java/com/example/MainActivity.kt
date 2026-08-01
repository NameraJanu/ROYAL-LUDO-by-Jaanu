package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ludo.model.GameMode
import com.example.ludo.ui.components.ThemeSelectorDialog
import com.example.ludo.ui.screens.GameScreen
import com.example.ludo.ui.screens.MainMenuScreen
import com.example.ludo.viewmodel.LudoViewModel
import com.example.ui.theme.MyApplicationTheme

enum class Screen {
    MAIN_MENU,
    GAME
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LudoApp()
                }
            }
        }
    }
}

@Composable
fun LudoApp(
    viewModel: LudoViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(Screen.MAIN_MENU) }
    val uiState by viewModel.uiState.collectAsState()

    if (currentScreen == Screen.MAIN_MENU && uiState.showThemeSelector) {
        ThemeSelectorDialog(
            currentTheme = uiState.currentTheme,
            onThemeSelected = { theme -> viewModel.selectTheme(theme) },
            onDismiss = { viewModel.closeThemeSelector() }
        )
    }

    // AnimatedContent providing combined Fade, Slide, and Scale screen transitions
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState == Screen.GAME) {
                // Navigating to Game: Slide in from right, fade in, scale up from 0.92
                (slideInHorizontally(animationSpec = tween(380)) { width -> width } +
                        fadeIn(animationSpec = tween(380)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(380)))
                    .togetherWith(
                        slideOutHorizontally(animationSpec = tween(320)) { width -> -width / 3 } +
                                fadeOut(animationSpec = tween(320)) +
                                scaleOut(targetScale = 0.92f, animationSpec = tween(320))
                    )
            } else {
                // Navigating back to Main Menu: Slide in from left, fade in, scale up from 0.92
                (slideInHorizontally(animationSpec = tween(380)) { width -> -width } +
                        fadeIn(animationSpec = tween(380)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(380)))
                    .togetherWith(
                        slideOutHorizontally(animationSpec = tween(320)) { width -> width / 3 } +
                                fadeOut(animationSpec = tween(320)) +
                                scaleOut(targetScale = 0.92f, animationSpec = tween(320))
                    )
            }
        },
        label = "page_transition",
        modifier = Modifier.fillMaxSize()
    ) { screen ->
        when (screen) {
            Screen.MAIN_MENU -> {
                MainMenuScreen(
                    soundEnabled = uiState.soundEnabled,
                    musicEnabled = uiState.musicEnabled,
                    currentTheme = uiState.currentTheme,
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleMusic = { viewModel.toggleMusic() },
                    onOpenThemes = { viewModel.openThemeSelector() },
                    onStartGame = { mode ->
                        viewModel.startNewGame(mode)
                        currentScreen = Screen.GAME
                    }
                )
            }
            Screen.GAME -> {
                GameScreen(
                    viewModel = viewModel,
                    onBackToMenu = {
                        currentScreen = Screen.MAIN_MENU
                    }
                )
            }
        }
    }
}
