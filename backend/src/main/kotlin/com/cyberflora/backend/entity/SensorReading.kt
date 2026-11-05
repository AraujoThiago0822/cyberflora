package com.cyberflora.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "sensor_readings",
    indexes = [Index(name = "idx_reading_device_time", columnList = "device_id, reading_at")]
)
data class SensorReading(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "device_id", nullable = false, length = 64)
    val deviceId: String = "",

    @Column(name = "sensor_type", nullable = false, length = 32)
    val sensorType: String = "",

    @Column(name = "reading_value", nullable = false)
    val value: Double = 0.0,

    @Column(nullable = false, length = 16)
    val unit: String = "",

    @Column(name = "reading_at", nullable = false)
    val readingAt: LocalDateTime = LocalDateTime.now()
)