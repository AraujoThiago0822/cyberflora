package com.cyberflora.backend.service

import com.cyberflora.backend.dto.PlantRequest
import com.cyberflora.backend.entity.Plant
import com.cyberflora.backend.entity.User
import com.cyberflora.backend.exception.BusinessRuleException
import com.cyberflora.backend.mapper.PlantMapper
import com.cyberflora.backend.repository.PlantRepository
import com.cyberflora.backend.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import java.time.LocalDate
import java.util.Optional

/**
 * Testes de unidade do caso de uso "Cadastrar Planta" (PlantService).
 *
 * Papéis dos recursos de teste:
 *  - DRIVER: esta própria classe, que aciona o PlantService e confere os resultados.
 *  - MOCK (Mockito): PlantRepository e UserRepository substituem o banco de dados e permitem
 *    verificar as interações (verify).
 *  - STUB (given / willReturn): respostas fixas dos repositórios em cada cenário.
 *  - PlantMapper e as entidades são objetos reais (não simulados).
 *
 * Os testes rejeita_* cobrem os 8 caminhos básicos de validateBusinessRules (V(G) = 8).
 */
class PlantServiceTest {

    private lateinit var plantRepository: PlantRepository   // MOCK
    private lateinit var userRepository: UserRepository     // MOCK
    private lateinit var service: PlantService

    private val ana = User(id = 1L, name = "Ana", email = EMAIL_ANA, password = "hash")
    private val dataPlantio = LocalDate.of(2026, 9, 15)

    @BeforeEach
    fun montarCenario() {
        plantRepository = mock(PlantRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        service = PlantService(plantRepository, userRepository, PlantMapper())
    }

    private fun requisicao(
        nome: String = "Alface", especie: String = "Lactuca sativa", data: LocalDate? = dataPlantio,
        min: Double = 60.0, max: Double = 80.0, ativa: Boolean = true
    ) = PlantRequest(name = nome, species = especie, plantingDate = data, minHumidity = min, maxHumidity = max, active = ativa)

    private fun entidade(req: PlantRequest, id: Long? = null) = Plant(
        id = id, name = req.name.trim(), species = req.species.trim(), plantingDate = req.plantingDate,
        minHumidity = req.minHumidity, maxHumidity = req.maxHumidity, active = req.active, owner = ana
    )

    /** STUB: o usuário existe e o repositório devolve a planta com id gerado. */
    private fun prepararCadastroValido(req: PlantRequest, idGerado: Long = 10L) {
        val nova = entidade(req)
        given(userRepository.findByEmail(EMAIL_ANA)).willReturn(Optional.of(ana))
        given(plantRepository.save(nova)).willReturn(nova.copy(id = idGerado))
    }

    private fun assertRegraViolada(req: PlantRequest, trecho: String) {
        val erro = assertThrows(BusinessRuleException::class.java) { service.create(req, EMAIL_ANA) }
        assertTrue(erro.message!!.contains(trecho), "Mensagem inesperada: ${erro.message}")
        verifyNoInteractions(plantRepository, userRepository)
    }

    // ---------- cadastro válido ----------

    @Test
    @DisplayName("Fluxo principal: cadastra a planta vinculada ao usuário autenticado (RN04)")
    fun cadastra_planta_vinculada_ao_usuario_autenticado() {
        val req = requisicao()
        prepararCadastroValido(req)
        val resposta = service.create(req, EMAIL_ANA)
        assertEquals(10L, resposta.id)
        assertEquals("Alface", resposta.name)
        assertEquals(60.0, resposta.minHumidity)
        verify(plantRepository).save(entidade(req))
    }

    @Test
    @DisplayName("Fluxo alternativo: data de plantio não informada é aceita (RN03)")
    fun cadastra_planta_sem_data_de_plantio() {
        val req = requisicao(data = null)
        prepararCadastroValido(req)
        assertNull(service.create(req, EMAIL_ANA).plantingDate)
    }

    @Test
    @DisplayName("Valores limite: umidade 1 e 100 são aceitos (RN02)")
    fun aceita_umidade_nos_limites_1_e_100() {
        val req = requisicao(min = 1.0, max = 100.0)
        prepararCadastroValido(req)
        val resposta = service.create(req, EMAIL_ANA)
        assertEquals(1.0, resposta.minHumidity)
        assertEquals(100.0, resposta.maxHumidity)
    }

    @Test
    @DisplayName("Valores limite: mínima uma unidade abaixo da máxima é aceita (RN02)")
    fun aceita_umidade_minima_uma_unidade_abaixo_da_maxima() {
        val req = requisicao(min = 99.0, max = 100.0)
        prepararCadastroValido(req)
        assertEquals(99.0, service.create(req, EMAIL_ANA).minHumidity)
    }

    @Test
    @DisplayName("Valores limite: nome com 100 caracteres é aceito (RN01)")
    fun aceita_nome_com_100_caracteres() {
        val req = requisicao(nome = "a".repeat(100))
        prepararCadastroValido(req)
        assertEquals(100, service.create(req, EMAIL_ANA).name.length)
    }

    @Test
    @DisplayName("Valores limite: espécie com 150 caracteres é aceita (RN01)")
    fun aceita_especie_com_150_caracteres() {
        val req = requisicao(especie = "b".repeat(150))
        prepararCadastroValido(req)
        assertEquals(150, service.create(req, EMAIL_ANA).species.length)
    }

    // ---------- regras violadas (caminhos B2 a B8 de validateBusinessRules) ----------

    @Test
    @DisplayName("RN01: nome em branco é rejeitado")
    fun rejeita_nome_em_branco() = assertRegraViolada(requisicao(nome = "   "), "nome")

    @Test
    @DisplayName("RN01: nome com 101 caracteres é rejeitado")
    fun rejeita_nome_com_101_caracteres() = assertRegraViolada(requisicao(nome = "a".repeat(101)), "no máximo 100")

    @Test
    @DisplayName("RN01: espécie em branco é rejeitada")
    fun rejeita_especie_em_branco() = assertRegraViolada(requisicao(especie = ""), "espécie")

    @Test
    @DisplayName("RN01: espécie com 151 caracteres é rejeitada")
    fun rejeita_especie_com_151_caracteres() = assertRegraViolada(requisicao(especie = "b".repeat(151)), "no máximo 150")

    @Test
    @DisplayName("RN02: umidade mínima abaixo de 1 é rejeitada")
    fun rejeita_umidade_minima_abaixo_de_1() = assertRegraViolada(requisicao(min = 0.99), "inferior a 1%")

    @Test
    @DisplayName("RN02: umidade máxima acima de 100 é rejeitada")
    fun rejeita_umidade_maxima_acima_de_100() = assertRegraViolada(requisicao(max = 100.01), "superior a 100%")

    @Test
    @DisplayName("RN02: umidade mínima igual à máxima é rejeitada")
    fun rejeita_umidade_minima_igual_a_maxima() = assertRegraViolada(requisicao(min = 80.0, max = 80.0), "menor que a máxima")

    @Test
    @DisplayName("RN02: umidade mínima maior que a máxima é rejeitada")
    fun rejeita_umidade_minima_maior_que_a_maxima() = assertRegraViolada(requisicao(min = 90.0, max = 70.0), "menor que a máxima")

    @Test
    @DisplayName("RN04: usuário autenticado inexistente falha e nada é gravado")
    fun falha_quando_usuario_autenticado_nao_existe() {
        given(userRepository.findByEmail("fantasma@teste.com")).willReturn(Optional.empty())
        assertThrows(NoSuchElementException::class.java) { service.create(requisicao(), "fantasma@teste.com") }
        verifyNoInteractions(plantRepository)
    }

    // ---------- atualizar, excluir e consultar (isolamento por usuário, RN04) ----------

    @Test
    @DisplayName("RN04: atualiza a planta do próprio usuário")
    fun atualiza_planta_do_proprio_usuario() {
        val req = requisicao(nome = "Alface Roxa")
        val atualizada = entidade(req, id = 5L)
        given(plantRepository.findByIdAndOwnerEmail(5L, EMAIL_ANA)).willReturn(Optional.of(entidade(requisicao(), 5L)))
        given(userRepository.findByEmail(EMAIL_ANA)).willReturn(Optional.of(ana))
        given(plantRepository.save(atualizada)).willReturn(atualizada)
        val resposta = service.update(5L, req, EMAIL_ANA)
        assertEquals(5L, resposta.id)
        assertEquals("Alface Roxa", resposta.name)
    }

    @Test
    @DisplayName("RN04: não atualiza planta de outro usuário")
    fun nao_atualiza_planta_de_outro_usuario() {
        given(plantRepository.findByIdAndOwnerEmail(5L, EMAIL_BETO)).willReturn(Optional.empty())
        assertThrows(NoSuchElementException::class.java) { service.update(5L, requisicao(), EMAIL_BETO) }
        verify(plantRepository).findByIdAndOwnerEmail(5L, EMAIL_BETO)
        verifyNoMoreInteractions(plantRepository)
    }

    @Test
    @DisplayName("A atualização aplica as mesmas regras de negócio do cadastro")
    fun atualizacao_aplica_as_regras_de_negocio() {
        assertThrows(BusinessRuleException::class.java) { service.update(5L, requisicao(min = 90.0, max = 70.0), EMAIL_ANA) }
        verifyNoInteractions(plantRepository)
    }

    @Test
    @DisplayName("RN04: exclui a planta do próprio usuário")
    fun exclui_planta_do_proprio_usuario() {
        given(plantRepository.findByIdAndOwnerEmail(5L, EMAIL_ANA)).willReturn(Optional.of(entidade(requisicao(), 5L)))
        service.delete(5L, EMAIL_ANA)
        verify(plantRepository).deleteById(5L)
    }

    @Test
    @DisplayName("RN04: não exclui planta de outro usuário")
    fun nao_exclui_planta_de_outro_usuario() {
        given(plantRepository.findByIdAndOwnerEmail(5L, EMAIL_BETO)).willReturn(Optional.empty())
        assertThrows(NoSuchElementException::class.java) { service.delete(5L, EMAIL_BETO) }
        verify(plantRepository).findByIdAndOwnerEmail(5L, EMAIL_BETO)
        verifyNoMoreInteractions(plantRepository)
    }

    @Test
    @DisplayName("RN04: a listagem traz apenas as plantas do usuário")
    fun lista_apenas_as_plantas_do_usuario() {
        given(plantRepository.findAllByOwnerEmail(EMAIL_ANA))
            .willReturn(listOf(entidade(requisicao(), 1L), entidade(requisicao(nome = "Manjericão"), 2L)))
        val lista = service.listAll(EMAIL_ANA)
        assertEquals(2, lista.size)
        assertEquals("Manjericão", lista[1].name)
        verify(plantRepository).findAllByOwnerEmail(EMAIL_ANA)
    }

    @Test
    @DisplayName("A busca por nome delega ao repositório com o dono")
    fun busca_por_nome_delega_ao_repositorio_com_o_dono() {
        given(plantRepository.searchByName("alf", EMAIL_ANA)).willReturn(listOf(entidade(requisicao(), 1L)))
        assertEquals(1, service.search("alf", EMAIL_ANA).size)
    }

    @Test
    @DisplayName("RN04: consulta por id de planta de outro usuário falha")
    fun consulta_por_id_de_planta_de_outro_usuario_falha() {
        given(plantRepository.findByIdAndOwnerEmail(5L, EMAIL_BETO)).willReturn(Optional.empty())
        assertThrows(NoSuchElementException::class.java) { service.findById(5L, EMAIL_BETO) }
    }

    private companion object {
        const val EMAIL_ANA = "ana@teste.com"
        const val EMAIL_BETO = "beto@teste.com"
    }
}