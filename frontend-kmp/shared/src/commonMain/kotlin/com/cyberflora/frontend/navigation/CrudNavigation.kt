package com.cyberflora.frontend.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.cyberflora.frontend.screens.FormPlaceholder
import com.cyberflora.frontend.screens.ListPlaceholder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val crudConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(CrudScreen.ListScreen::class, CrudScreen.ListScreen.serializer())
            subclass(CrudScreen.FormScreen::class, CrudScreen.FormScreen.serializer())
        }
    }
}

@Composable
fun CrudNavigation(title: String) {
        val backStack: NavBackStack<NavKey> = rememberNavBackStack(crudConfig, CrudScreen.ListScreen)

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<CrudScreen.ListScreen> {
                ListPlaceholder(
                    title = title,
                    onAddNew = { backStack.add(CrudScreen.FormScreen(id = null)) },
                    onEditItem = { id -> backStack.add(CrudScreen.FormScreen(id = id)) }
                )
            }
            entry<CrudScreen.FormScreen> { key ->
                FormPlaceholder(
                    title = title,
                    id = key.id,
                    onSave = { backStack.removeAt(backStack.lastIndex) }
                )
            }
        }
    )
}