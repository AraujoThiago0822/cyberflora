package com.cyberflora.backend.service

import com.cyberflora.backend.dto.GrowingAreaRequest
import com.cyberflora.backend.dto.GrowingAreaResponse
import com.cyberflora.backend.entity.GrowingArea
import com.cyberflora.backend.repository.GrowingAreaRepository
import org.springframework.stereotype.Service

@Service
class GrowingAreaService(
    private val growingAreaRepository: GrowingAreaRepository
) {

    fun listAll(): List<GrowingAreaResponse> {
        return growingAreaRepository.findAll().map { it.toResponse() }
    }

    fun findById(id: Long): GrowingAreaResponse {
        val area = growingAreaRepository.findById(id)
            .orElseThrow { NoSuchElementException("Área de cultivo com id $id não encontrada") }
        return area.toResponse()
    }

    fun create(request: GrowingAreaRequest): GrowingAreaResponse {
        val area = GrowingArea(
            name = request.name,
            description = request.description,
            capacityM2 = request.capacityM2,
            irrigationAutomatic = request.irrigationAutomatic
        )
        return growingAreaRepository.save(area).toResponse()
    }

    fun update(id: Long, request: GrowingAreaRequest): GrowingAreaResponse {
        val existing = growingAreaRepository.findById(id)
            .orElseThrow { NoSuchElementException("Área de cultivo com id $id não encontrada") }

        val updated = existing.copy(
            name = request.name,
            description = request.description,
            capacityM2 = request.capacityM2,
            irrigationAutomatic = request.irrigationAutomatic
        )
        return growingAreaRepository.save(updated).toResponse()
    }

    fun delete(id: Long) {
        if (!growingAreaRepository.existsById(id)) {
            throw NoSuchElementException("Área de cultivo com id $id não encontrada")
        }
        growingAreaRepository.deleteById(id)
    }

    private fun GrowingArea.toResponse() = GrowingAreaResponse(
        id = this.id ?: 0,
        name = this.name,
        description = this.description,
        capacityM2 = this.capacityM2,
        irrigationAutomatic = this.irrigationAutomatic,
        createdAt = this.createdAt
    )
}