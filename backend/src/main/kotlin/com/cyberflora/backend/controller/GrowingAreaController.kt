package com.cyberflora.backend.controller

import com.cyberflora.backend.dto.GrowingAreaRequest
import com.cyberflora.backend.dto.GrowingAreaResponse
import com.cyberflora.backend.service.GrowingAreaService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/growing-areas")
class GrowingAreaController(
    private val growingAreaService: GrowingAreaService
) {

    @GetMapping
    fun listAll(): ResponseEntity<List<GrowingAreaResponse>> {
        return ResponseEntity.ok(growingAreaService.listAll())
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(growingAreaService.findById(id))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        }
    }

    @PostMapping
    fun create(@Valid @RequestBody request: GrowingAreaRequest): ResponseEntity<GrowingAreaResponse> {
        val created = growingAreaService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: GrowingAreaRequest): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(growingAreaService.update(id, request))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            growingAreaService.delete(id)
            ResponseEntity.noContent().build<Any>()
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        }
    }
}