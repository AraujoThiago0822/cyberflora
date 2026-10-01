package com.cyberflora.frontend.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface CrudSection : NavKey {
    @Serializable data object Plants : CrudSection
    @Serializable data object GrowingAreas : CrudSection
}