package com.cyberflora.backend.service

import com.cyberflora.backend.dto.PlantRequest
import com.cyberflora.backend.dto.PlantResponse
import com.cyberflora.backend.entity.Plant
import com.cyberflora.backend.entity.User
import com.cyberflora.backend.mapper.PlantMapper
import com.cyberflora.backend.repository.PlantRepository
import com.cyberflora.backend.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class PlantService(
    private val plantRepository: PlantRepository,
    private val userRepository: UserRepository,
    private val plantMapper: PlantMapper
) {

    fun listAll(ownerEmail: String): List<PlantResponse> =
        plantRepository.findAllByOwnerEmail(ownerEmail).map { plantMapper.toResponse(it) }

    fun search(name: String, ownerEmail: String): List<PlantResponse> =
        plantRepository.searchByName(name, ownerEmail).map { plantMapper.toResponse(it) }

    fun findById(id: Long, ownerEmail: String): PlantResponse =
        plantMapper.toResponse(findOwned(id, ownerEmail))

    fun create(request: PlantRequest, ownerEmail: String): PlantResponse {
        val saved = plantRepository.save(plantMapper.toEntity(request, findUser(ownerEmail)))
        return plantMapper.toResponse(saved)
    }

    fun update(id: Long, request: PlantRequest, ownerEmail: String): PlantResponse {
        findOwned(id, ownerEmail)
        val saved = plantRepository.save(plantMapper.toEntity(request, findUser(ownerEmail), id))
        return plantMapper.toResponse(saved)
    }

    fun delete(id: Long, ownerEmail: String) {
        findOwned(id, ownerEmail)
        plantRepository.deleteById(id)
    }

    private fun findUser(email: String): User =
        userRepository.findByEmail(email).orElseThrow { NoSuchElementException("Usuário não encontrado") }

    private fun findOwned(id: Long, ownerEmail: String): Plant =
        plantRepository.findByIdAndOwnerEmail(id, ownerEmail)
            .orElseThrow { NoSuchElementException("Planta com id $id não encontrada") }
}