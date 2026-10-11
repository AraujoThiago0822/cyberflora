package com.cyberflora.backend.controller

import com.cyberflora.backend.dto.GrowingAreaRequest
import com.cyberflora.backend.service.GrowingAreaService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/growing-areas")
class GrowingAreaController(private val growingAreaService: GrowingAreaService) {

    @GetMapping
    fun listAll(auth: Authentication) = growingAreaService.listAll(auth.name)

    @GetMapping("/search")
    fun search(@RequestParam name: String, auth: Authentication) = growingAreaService.search(name, auth.name)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long, auth: Authentication) = growingAreaService.findById(id, auth.name)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: GrowingAreaRequest, auth: Authentication) =
        growingAreaService.create(request, auth.name)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: GrowingAreaRequest, auth: Authentication) =
        growingAreaService.update(id, request, auth.name)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long, auth: Authentication) = growingAreaService.delete(id, auth.name)
}