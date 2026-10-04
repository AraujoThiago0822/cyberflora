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
import com.cyberflora.frontend.screens.PlantFormScreen
import com.cyberflora.frontend.screens.PlantListScreen
import com.cyberflora.frontend.viewmodel.PlantViewModel
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val plantCrudConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(CrudScreen.ListScreen::class, CrudScreen.ListScreen.serializer())
            subclass(CrudScreen.FormScreen::class, CrudScreen.FormScreen.serializer())
        }
    }
}

@Composable
fun PlantNavigation() {
    val viewModel = remember { PlantViewModel() }
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(plantCrudConfig, CrudScreen.ListScreen)

    LaunchedEffect(Unit) { viewModel.loadAll() }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<CrudScreen.ListScreen> {
                PlantListScreen(
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
                PlantFormScreen(
                    viewModel = viewModel,
                    onSaved = { backStack.removeAt(backStack.lastIndex) }
                )
            }
        }
    )
}