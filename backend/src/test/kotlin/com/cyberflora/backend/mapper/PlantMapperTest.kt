package com.cyberflora.backend.mapper

import com.cyberflora.backend.dto.PlantRequest
import com.cyberflora.backend.entity.Plant
import com.cyberflora.backend.entity.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

/** Testes de unidade do PlantMapper (conversão DTO <-> entidade). Sem mocks: o mapper não tem dependências. */
class PlantMapperTest {

    private val mapper = PlantMapper()
    private val dono = User(id = 1L, name = "Ana", email = "ana@teste.com", password = "hash")
    private val data = LocalDate.of(2026, 9, 15)
    private val requisicao = PlantRequest(name = "Alface", species = "Lactuca sativa", plantingDate = data, minHumidity = 60.0, maxHumidity = 80.0, active = true)

    @Test
    @DisplayName("toEntity copia os campos e define o dono")
    fun toEntity_copia_os_campos_e_define_o_dono() {
        val entidade = mapper.toEntity(requisicao, dono)
        assertEquals("Alface", entidade.name)
        assertEquals(data, entidade.plantingDate)
        assertEquals(60.0, entidade.minHumidity)
        assertEquals(dono, entidade.owner)
    }

    @Test
    @DisplayName("toEntity sem id gera entidade nova")
    fun toEntity_sem_id_gera_entidade_nova() {
        assertNull(mapper.toEntity(requisicao, dono).id)
    }

    @Test
    @DisplayName("toEntity com id informado preserva o id (atualização)")
    fun toEntity_com_id_informado_preserva_o_id() {
        assertEquals(7L, mapper.toEntity(requisicao, dono, 7L).id)
    }

    @Test
    @DisplayName("toResponse copia todos os campos")
    fun toResponse_copia_todos_os_campos() {
        val resposta = mapper.toResponse(Plant(id = 3L, name = "Alface", species = "Lactuca sativa", plantingDate = data, minHumidity = 60.0, maxHumidity = 80.0, active = false, owner = dono))
        assertEquals(3L, resposta.id)
        assertEquals("Lactuca sativa", resposta.species)
        assertEquals(80.0, resposta.maxHumidity)
        assertEquals(false, resposta.active)
    }

    @Test
    @DisplayName("toResponse converte id nulo em 0")
    fun toResponse_converte_id_nulo_em_zero() {
        val resposta = mapper.toResponse(Plant(id = null, name = "Alface", species = "Lactuca sativa", plantingDate = null, minHumidity = 60.0, maxHumidity = 80.0, active = true, owner = dono))
        assertEquals(0L, resposta.id)
    }
}