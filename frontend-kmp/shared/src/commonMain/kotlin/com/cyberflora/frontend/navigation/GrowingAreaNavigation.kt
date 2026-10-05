package com.cyberflora.frontend.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.cyberflora.frontend.screens.GrowingAreaFormScreen
import com.cyberflora.frontend.screens.GrowingAreaListScreen
import com.cyberflora.frontend.viewmodel.GrowingAreaViewModel
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val growingAreaCrudConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(CrudScreen.ListScreen::class, CrudScreen.ListScreen.serializer())
            subclass(CrudScreen.FormScreen::class, CrudScreen.FormScreen.serializer())
        }
    }
}

@Composable
fun GrowingAreaNavigation() {
    val viewModel = remember { GrowingAreaViewModel() }
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(growingAreaCrudConfig, CrudScreen.ListScreen)

    LaunchedEffect(Unit) { viewModel.loadAll() }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<CrudScreen.ListScreen> {
                GrowingAreaListScreen(
                    viewModel = viewModel,
                    onAddNew = {
                        viewModel.clearForm()
                        backStack.add(CrudScreen.FormScreen(id = null))
                    },
                    onEditItem = { id ->
                        viewModel.loadForEdit(id)
                        backStack.add(CrudScreen.FormScreen(id = id))
                    }
                )
            }
            entry<CrudScreen.FormScreen> {
                GrowingAreaFormScreen(
                    viewModel = viewModel,
                    onSaved = { backStack.removeAt(backStack.lastIndex) }
                )
            }
        }
    )
}