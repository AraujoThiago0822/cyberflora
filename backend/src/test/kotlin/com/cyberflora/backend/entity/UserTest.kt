package com.cyberflora.backend.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/** Testes de unidade da entidade de domínio User (data class). */
class UserTest {

    @Test
    @DisplayName("A data de criação é preenchida automaticamente")
    fun data_de_criacao_e_preenchida_automaticamente() {
        assertNotNull(User(name = "Ana", email = "ana@teste.com", password = "hash").createdAt)
    }

    @Test
    @DisplayName("copy preserva a identidade e altera só o campo informado")
    fun copy_preserva_a_identidade_e_altera_so_o_campo_informado() {
        val ana = User(id = 1L, name = "Ana", email = "ana@teste.com", password = "hash")
        val renomeada = ana.copy(name = "Ana Maria")
        assertEquals(1L, renomeada.id)
        assertEquals("ana@teste.com", renomeada.email)
        assertEquals("Ana Maria", renomeada.name)
    }
}