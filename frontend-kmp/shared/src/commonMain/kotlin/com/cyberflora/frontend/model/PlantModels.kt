package com.cyberflora.frontend.model

import kotlinx.serialization.Serializable

@Serializable
data class PlantRequest(
    val name: String,
    val species: String,
    val plantingDate: String?,
    val minHumidity: Double,
    val maxHumidity: Double,
    val active: Boolean
)

@Serializable
data class PlantResponse(
    val id: Long,
    val name: String,
    val species: String,
    val plantingDate: String?,
    val minHumidity: Double,
    val maxHumidity: Double,
    val active: Boolean
)