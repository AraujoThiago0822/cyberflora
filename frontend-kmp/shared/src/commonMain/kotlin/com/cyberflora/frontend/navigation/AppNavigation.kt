package com.cyberflora.frontend.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.cyberflora.frontend.screens.LoginScreen
import com.cyberflora.frontend.screens.RegisterScreen
import com.cyberflora.frontend.screens.SplashScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val rootConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(RootDestination.Splash::class, RootDestination.Splash.serializer())
            subclass(RootDestination.Login::class, RootDestination.Login.serializer())
            subclass(RootDestination.Register::class, RootDestination.Register.serializer())
            subclass(RootDestination.Home::class, RootDestination.Home.serializer())
        }
    }
}

@Composable
fun AppNavigation() {
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(rootConfig, RootDestination.Splash)

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<RootDestination.Splash> {
                SplashScreen(onFinished = {
                    backStack.removeAt(backStack.lastIndex)
                    backStack.add(RootDestination.Login)
                })
            }
            entry<RootDestination.Login> {
                LoginScreen(
                    onLoginSuccess = {
                        backStack.removeAt(backStack.lastIndex)
                        backStack.add(RootDestination.Home)
                    },
                    onGoToRegister = { backStack.add(RootDestination.Register) }
                )
            }
            entry<RootDestination.Register> {
                RegisterScreen(
                    onRegisterSuccess = {
                        backStack.removeAt(backStack.lastIndex) // tira Register
                        backStack.removeAt(backStack.lastIndex) // tira Login
                        backStack.add(RootDestination.Home)
                    },
                    onBackToLogin = { backStack.removeAt(backStack.lastIndex) }
                )
            }
            entry<RootDestination.Home> {
                HomeNavigation()
            }
        }
    )
}