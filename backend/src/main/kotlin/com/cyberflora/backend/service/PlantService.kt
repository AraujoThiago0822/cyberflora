package com.cyberflora.backend.service

import com.cyberflora.backend.dto.PlantRequest
import com.cyberflora.backend.dto.PlantResponse
import com.cyberflora.backend.entity.Plant
import com.cyberflora.backend.entity.User
import com.cyberflora.backend.exception.BusinessRuleException
import com.cyberflora.backend.mapper.PlantMapper
import com.cyberflora.backend.repository.PlantRepository
import com.cyberflora.backend.repository.UserRepository
import org.springframework.stereotype.Service

/** Caso de uso "Cadastrar Planta" (e demais operações de plantas). Toda consulta é filtrada pelo dono (RN04). */
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
        validateBusinessRules(request)
        val saved = plantRepository.save(plantMapper.toEntity(request, findUser(ownerEmail)))
        return plantMapper.toResponse(saved)
    }

    fun update(id: Long, request: PlantRequest, ownerEmail: String): PlantResponse {
        validateBusinessRules(request)
        findOwned(id, ownerEmail)
        val saved = plantRepository.save(plantMapper.toEntity(request, findUser(ownerEmail), id))
        return plantMapper.toResponse(saved)
    }

    fun delete(id: Long, ownerEmail: String) {
        findOwned(id, ownerEmail)
        plantRepository.deleteById(id)
    }

    /** RN01 e RN02 no servidor (as verificações do aplicativo não substituem as do servidor). */
    private fun validateBusinessRules(request: PlantRequest) {
        if (request.name.isBlank()) throw BusinessRuleException("O nome da planta é obrigatório")
        if (request.name.trim().length > 100) throw BusinessRuleException("O nome deve ter no máximo 100 caracteres")
        if (request.species.isBlank()) throw BusinessRuleException("A espécie é obrigatória")
        if (request.species.trim().length > 150) throw BusinessRuleException("A espécie deve ter no máximo 150 caracteres")
        if (request.minHumidity < 1.0) throw BusinessRuleException("A umidade mínima não pode ser inferior a 1%")
        if (request.maxHumidity > 100.0) throw BusinessRuleException("A umidade máxima não pode ser superior a 100%")
        if (request.minHumidity >= request.maxHumidity) throw BusinessRuleException("A umidade mínima deve ser menor que a máxima")
    }

    private fun findUser(email: String): User =
        userRepository.findByEmail(email).orElseThrow { NoSuchElementException("Usuário não encontrado") }

    private fun findOwned(id: Long, ownerEmail: String): Plant =
        plantRepository.findByIdAndOwnerEmail(id, ownerEmail)
            .orElseThrow { NoSuchElementException("Planta com id $id não encontrada") }
}