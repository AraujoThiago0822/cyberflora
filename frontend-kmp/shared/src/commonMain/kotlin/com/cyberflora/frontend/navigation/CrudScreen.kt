package com.cyberflora.frontend.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface CrudScreen : NavKey {
    @Serializable data object ListScreen : CrudScreen
    @Serializable data class FormScreen(val id: Long? = null) : CrudScreen
}