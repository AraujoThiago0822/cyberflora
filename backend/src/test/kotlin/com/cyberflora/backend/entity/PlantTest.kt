package com.cyberflora.backend.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

/** Testes de unidade da entidade de domínio Plant (data class). */
class PlantTest {

    private val ana = User(id = 1L, name = "Ana", email = "ana@teste.com", password = "hash")
    private val beto = User(id = 2L, name = "Beto", email = "beto@teste.com", password = "hash")
    private fun planta(dono: User) = Plant(id = 1L, name = "Alface", species = "Lactuca sativa", plantingDate = LocalDate.of(2026, 9, 15), minHumidity = 60.0, maxHumidity = 80.0, active = true, owner = dono)

    @Test
    @DisplayName("Plantas com os mesmos dados são iguais")
    fun plantas_com_os_mesmos_dados_sao_iguais() {
        assertEquals(planta(ana), planta(ana))
    }

    @Test
    @DisplayName("Plantas com donos diferentes não são iguais (RN04)")
    fun plantas_com_donos_diferentes_nao_sao_iguais() {
        assertNotEquals(planta(ana), planta(beto))
    }

    @Test
    @DisplayName("copy altera só o campo informado e preserva o dono")
    fun copy_altera_so_o_campo_informado_e_preserva_o_dono() {
        val inativa = planta(ana).copy(active = false)
        assertEquals(false, inativa.active)
        assertEquals("Alface", inativa.name)
        assertEquals(ana, inativa.owner)
    }
}