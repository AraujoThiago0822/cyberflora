package com.cyberflora.backend.controller

import com.cyberflora.backend.dto.PlantRequest
import com.cyberflora.backend.service.PlantService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/plants")
class PlantController(private val plantService: PlantService) {

    @GetMapping
    fun listAll(auth: Authentication) = plantService.listAll(auth.name)

    @GetMapping("/search")
    fun search(@RequestParam name: String, auth: Authentication) = plantService.search(name, auth.name)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long, auth: Authentication) = plantService.findById(id, auth.name)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: PlantRequest, auth: Authentication) =
        plantService.create(request, auth.name)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: PlantRequest, auth: Authentication) =
        plantService.update(id, request, auth.name)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long, auth: Authentication) = plantService.delete(id, auth.name)
}