package com.cyberflora.backend.repository

import com.cyberflora.backend.entity.SensorReading
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SensorReadingRepository : JpaRepository<SensorReading, Long> {

    fun findByDeviceIdAndReadingAtAfterOrderByReadingAtAsc(deviceId: String, since: LocalDateTime): List<SensorReading>

    fun findByDeviceIdAndSensorTypeAndReadingAtAfterOrderByReadingAtAsc(
        deviceId: String, sensorType: String, since: LocalDateTime
    ): List<SensorReading>

    fun findFirstByDeviceIdAndSensorTypeOrderByReadingAtDesc(deviceId: String, sensorType: String): SensorReading?
}