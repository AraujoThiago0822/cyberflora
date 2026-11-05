package com.cyberflora.backend.controller

import com.cyberflora.backend.service.SensorReadingService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/growing-areas/{areaId}/readings")
class SensorReadingController(private val service: SensorReadingService) {

    @GetMapping
    fun history(
        @PathVariable areaId: Long,
        @RequestParam(defaultValue = "24") hours: Long,
        @RequestParam(required = false) type: String?,
        auth: Authentication
    ) = service.history(areaId, auth.name, hours, type)

    @GetMapping("/latest")
    fun latest(@PathVariable areaId: Long, auth: Authentication) = service.latest(areaId, auth.name)
}