package com.cyberflora.backend.dto

import java.time.LocalDateTime

data class SensorReadingResponse(
    val id: Long,
    val deviceId: String,
    val sensorType: String,
    val value: Double,
    val unit: String,
    val readingAt: LocalDateTime
)