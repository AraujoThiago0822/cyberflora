package com.cyberflora.backend.mapper

import com.cyberflora.backend.dto.GrowingAreaRequest
import com.cyberflora.backend.dto.GrowingAreaResponse
import com.cyberflora.backend.entity.GrowingArea
import com.cyberflora.backend.entity.User
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class GrowingAreaMapper {

    fun toEntity(
        request: GrowingAreaRequest,
        owner: User,
        id: Long? = null,
        createdAt: LocalDateTime = LocalDateTime.now()
    ): GrowingArea = GrowingArea(
        id = id,
        name = request.name.trim(),
        description = request.description,
        capacityM2 = request.capacityM2,
        irrigationAutomatic = request.irrigationAutomatic,
        createdAt = createdAt,
        owner = owner
    )

    fun toResponse(entity: GrowingArea): GrowingAreaResponse = GrowingAreaResponse(
        id = entity.id ?: 0,
        name = entity.name,
        description = entity.description,
        capacityM2 = entity.capacityM2,
        irrigationAutomatic = entity.irrigationAutomatic,
        createdAt = entity.createdAt
    )
}