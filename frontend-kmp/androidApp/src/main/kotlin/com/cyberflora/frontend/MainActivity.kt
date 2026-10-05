package com.cyberflora.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cyberflora.frontend.navigation.AppNavigation
import com.cyberflora.frontend.theme.CyberFloraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CyberFloraTheme {
                AppNavigation()
            }
        }
    }
}