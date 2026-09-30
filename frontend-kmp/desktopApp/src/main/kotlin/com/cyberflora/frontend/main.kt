package com.cyberflora.frontend

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.cyberflora.frontend.theme.CyberFloraTheme
import com.cyberflora.frontend.screens.SplashScreen

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CyberFlora",
    ) {
                CyberFloraTheme {
            SplashScreen(onFinished = {})
        }
    }
}