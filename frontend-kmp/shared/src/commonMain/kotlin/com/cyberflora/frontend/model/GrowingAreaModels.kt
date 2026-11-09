package com.cyberflora.frontend.model

import kotlinx.serialization.Serializable

@Serializable
data class GrowingAreaRequest(
    val name: String,
    val description: String,
    val capacityM2: Double,
    val irrigationAutomatic: Boolean,
    val deviceId: String? = null
)

@Serializable
data class GrowingAreaResponse(
    val id: Long,
    val name: String,
    val description: String,
    val capacityM2: Double,
    val irrigationAutomatic: Boolean,
    val createdAt: String,
    val deviceId: String? = null
)