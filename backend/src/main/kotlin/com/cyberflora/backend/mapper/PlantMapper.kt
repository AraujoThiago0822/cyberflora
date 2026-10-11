package com.cyberflora.backend.mapper

import com.cyberflora.backend.dto.PlantRequest
import com.cyberflora.backend.dto.PlantResponse
import com.cyberflora.backend.entity.Plant
import com.cyberflora.backend.entity.User
import org.springframework.stereotype.Component

@Component
class PlantMapper {

    fun toEntity(request: PlantRequest, owner: User, id: Long? = null): Plant = Plant(
        id = id,
        name = request.name.trim(),
        species = request.species.trim(),
        plantingDate = request.plantingDate,
        minHumidity = request.minHumidity,
        maxHumidity = request.maxHumidity,
        active = request.active,
        owner = owner
    )

    fun toResponse(entity: Plant): PlantResponse = PlantResponse(
        id = entity.id ?: 0,
        name = entity.name,
        species = entity.species,
        plantingDate = entity.plantingDate,
        minHumidity = entity.minHumidity,
        maxHumidity = entity.maxHumidity,
        active = entity.active
    )
}