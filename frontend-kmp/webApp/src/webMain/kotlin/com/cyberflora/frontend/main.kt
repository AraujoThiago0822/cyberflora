package com.cyberflora.frontend

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.cyberflora.frontend.navigation.AppNavigation
import com.cyberflora.frontend.theme.CyberFloraTheme
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        CyberFloraTheme {
            AppNavigation()
        }
    }
}