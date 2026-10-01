package com.cyberflora.frontend.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface RootDestination : NavKey {
    @Serializable data object Splash : RootDestination
    @Serializable data object Login : RootDestination
    @Serializable data object Register : RootDestination
    @Serializable data object Home : RootDestination
}