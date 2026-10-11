package com.cyberflora.backend.service

import com.cyberflora.backend.dto.GrowingAreaRequest
import com.cyberflora.backend.dto.GrowingAreaResponse
import com.cyberflora.backend.entity.GrowingArea
import com.cyberflora.backend.entity.User
import com.cyberflora.backend.mapper.GrowingAreaMapper
import com.cyberflora.backend.repository.GrowingAreaRepository
import com.cyberflora.backend.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class GrowingAreaService(
    private val growingAreaRepository: GrowingAreaRepository,
    private val userRepository: UserRepository,
    private val growingAreaMapper: GrowingAreaMapper
) {

    fun listAll(ownerEmail: String): List<GrowingAreaResponse> =
        growingAreaRepository.findAllByOwnerEmail(ownerEmail).map { growingAreaMapper.toResponse(it) }

    fun search(name: String, ownerEmail: String): List<GrowingAreaResponse> =
        growingAreaRepository.searchByName(name, ownerEmail).map { growingAreaMapper.toResponse(it) }

    fun findById(id: Long, ownerEmail: String): GrowingAreaResponse =
        growingAreaMapper.toResponse(findOwned(id, ownerEmail))

    fun create(request: GrowingAreaRequest, ownerEmail: String): GrowingAreaResponse {
        val saved = growingAreaRepository.save(growingAreaMapper.toEntity(request, findUser(ownerEmail)))
        return growingAreaMapper.toResponse(saved)
    }

    fun update(id: Long, request: GrowingAreaRequest, ownerEmail: String): GrowingAreaResponse {
        val existing = findOwned(id, ownerEmail)
        val updated = growingAreaMapper.toEntity(request, findUser(ownerEmail), existing.id, existing.createdAt)
        return growingAreaMapper.toResponse(growingAreaRepository.save(updated))
    }

    fun delete(id: Long, ownerEmail: String) {
        findOwned(id, ownerEmail)
        growingAreaRepository.deleteById(id)
    }

    private fun findUser(email: String): User =
        userRepository.findByEmail(email).orElseThrow { NoSuchElementException("Usuário não encontrado") }

    private fun findOwned(id: Long, ownerEmail: String): GrowingArea =
        growingAreaRepository.findByIdAndOwnerEmail(id, ownerEmail)
            .orElseThrow { NoSuchElementException("Área de cultivo com id $id não encontrada") }
}