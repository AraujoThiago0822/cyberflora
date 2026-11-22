package com.cyberflora.backend.dto

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

/** Testes de unidade da validação (Bean Validation) do DTO PlantRequest: primeira barreira de RN01 e RN02 no servidor. */
class PlantRequestValidationTest {

    private val validator: Validator = Validation.byDefaultProvider().configure()
        .messageInterpolator(ParameterMessageInterpolator()).buildValidatorFactory().validator

    private fun valida() = PlantRequest(name = "Alface", species = "Lactuca sativa", plantingDate = LocalDate.of(2026, 9, 15), minHumidity = 60.0, maxHumidity = 80.0, active = true)
    private fun campos(req: PlantRequest): Set<String> = validator.validate(req).map { it.propertyPath.toString() }.toSet()

    @Test
    @DisplayName("Requisição válida não gera violações")
    fun requisicao_valida_nao_gera_violacoes() { assertTrue(campos(valida()).isEmpty()) }

    @Test
    @DisplayName("Data de plantio nula é aceita (RN03)")
    fun data_de_plantio_nula_e_aceita() { assertTrue(campos(valida().copy(plantingDate = null)).isEmpty()) }

    @Test
    @DisplayName("Nome em branco viola a validação")
    fun nome_em_branco_viola_a_validacao() { assertEquals(setOf("name"), campos(valida().copy(name = ""))) }

    @Test
    @DisplayName("Nome com 101 caracteres viola a validação")
    fun nome_com_101_caracteres_viola_a_validacao() { assertEquals(setOf("name"), campos(valida().copy(name = "a".repeat(101)))) }

    @Test
    @DisplayName("Espécie em branco viola a validação")
    fun especie_em_branco_viola_a_validacao() { assertEquals(setOf("species"), campos(valida().copy(species = ""))) }

    @Test
    @DisplayName("Espécie com 151 caracteres viola a validação")
    fun especie_com_151_caracteres_viola_a_validacao() { assertEquals(setOf("species"), campos(valida().copy(species = "b".repeat(151)))) }

    @Test
    @DisplayName("Umidade mínima igual a zero viola a validação")
    fun umidade_minima_zero_viola_a_validacao() { assertEquals(setOf("minHumidity"), campos(valida().copy(minHumidity = 0.0))) }

    @Test
    @DisplayName("Umidade máxima negativa viola a validação")
    fun umidade_maxima_negativa_viola_a_validacao() { assertEquals(setOf("maxHumidity"), campos(valida().copy(maxHumidity = -5.0))) }
}