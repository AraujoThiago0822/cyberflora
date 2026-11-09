package com.cyberflora.frontend.model

import kotlinx.serialization.Serializable

@Serializable
data class SensorReadingResponse(
    val id: Long,
    val deviceId: String,
    val sensorType: String,
    val value: Double,
    val unit: String,
    val readingAt: String
)