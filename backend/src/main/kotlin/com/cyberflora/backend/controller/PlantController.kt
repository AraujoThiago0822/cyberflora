package com.cyberflora.backend.controller

import com.cyberflora.backend.dto.PlantRequest
import com.cyberflora.backend.dto.PlantResponse
import com.cyberflora.backend.service.PlantService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/plants")
class PlantController(
    private val plantService: PlantService
) {

    @GetMapping
    fun listAll(): ResponseEntity<List<PlantResponse>> {
        return ResponseEntity.ok(plantService.listAll())
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(plantService.findById(id))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        }
    }

    @PostMapping
    fun create(@Valid @RequestBody request: PlantRequest): ResponseEntity<PlantResponse> {
        val created = plantService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: PlantRequest): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(plantService.update(id, request))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            plantService.delete(id)
            ResponseEntity.noContent().build<Any>()
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        }
    }
}