package com.cyberflora.backend.service

import com.cyberflora.backend.dto.PlantRequest
import com.cyberflora.backend.dto.PlantResponse
import com.cyberflora.backend.entity.Plant
import com.cyberflora.backend.repository.PlantRepository
import org.springframework.stereotype.Service

@Service
class PlantService(
    private val plantRepository: PlantRepository
) {

    fun listAll(): List<PlantResponse> {
        return plantRepository.findAll().map { it.toResponse() }
    }

    fun findById(id: Long): PlantResponse {
        val plant = plantRepository.findById(id)
            .orElseThrow { NoSuchElementException("Planta com id $id não encontrada") }
        return plant.toResponse()
    }

    fun create(request: PlantRequest): PlantResponse {
        val plant = Plant(
            name = request.name,
            species = request.species,
            plantingDate = request.plantingDate,
            minHumidity = request.minHumidity,
            maxHumidity = request.maxHumidity,
            active = request.active
        )
        return plantRepository.save(plant).toResponse()
    }

    fun update(id: Long, request: PlantRequest): PlantResponse {
        val existing = plantRepository.findById(id)
            .orElseThrow { NoSuchElementException("Planta com id $id não encontrada") }

        val updated = existing.copy(
            name = request.name,
            species = request.species,
            plantingDate = request.plantingDate,
            minHumidity = request.minHumidity,
            maxHumidity = request.maxHumidity,
            active = request.active
        )
        return plantRepository.save(updated).toResponse()
    }

    fun delete(id: Long) {
        if (!plantRepository.existsById(id)) {
            throw NoSuchElementException("Planta com id $id não encontrada")
        }
        plantRepository.deleteById(id)
    }

    private fun Plant.toResponse() = PlantResponse(
        id = this.id ?: 0,
        name = this.name,
        species = this.species,
        plantingDate = this.plantingDate,
        minHumidity = this.minHumidity,
        maxHumidity = this.maxHumidity,
        active = this.active
    )
}