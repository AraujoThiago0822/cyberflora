package com.cyberflora.backend.service

import com.cyberflora.backend.entity.GrowingArea
import com.cyberflora.backend.entity.SensorReading
import com.cyberflora.backend.repository.GrowingAreaRepository
import com.cyberflora.backend.repository.SensorReadingRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import java.time.LocalDateTime
import java.util.Optional

/**
 * Testes de unidade do SensorReadingService (listener MQTT e histórico).
 *  - DRIVER: esta classe, que aciona o serviço.
 *  - MOCK (Mockito): SensorReadingRepository e GrowingAreaRepository, que substituem o banco.
 *  - STUB: given(...).willReturn / willAnswer.
 * anyOr/eqOr registram o matcher do Mockito e devolvem um valor não nulo,
 * porque o Kotlin recusa null em parâmetros não anuláveis.
 */
class SensorReadingServiceTest {

    private lateinit var readings: SensorReadingRepository
    private lateinit var areas: GrowingAreaRepository
    private lateinit var service: SensorReadingService

    private val dono = "ana@teste.com"
    private val topicoUmidade = "cyberflora/esp32-001/sensor/soil_moisture"

    @BeforeEach
    fun montarCenario() {
        readings = mock(SensorReadingRepository::class.java)
        areas = mock(GrowingAreaRepository::class.java)
        service = SensorReadingService(readings, areas)
    }

    private fun <T : Any> anyOr(valor: T, tipo: Class<T>): T = any(tipo) ?: valor
    private fun eqOr(valor: String): String = eq(valor) ?: valor

    private fun permitirGravacao() {
        given(readings.save(anyOr(SensorReading(), SensorReading::class.java))).willAnswer { it.arguments[0] }
    }

    private fun gravada(topico: String, payload: String): SensorReading {
        permitirGravacao()
        return checkNotNull(service.registerFromMqtt(topico, payload)) { "A leitura deveria ter sido gravada: $payload" }
    }

    private fun assertDescartada(topico: String, payload: String) {
        assertNull(service.registerFromMqtt(topico, payload), "Deveria ter sido descartada: $topico | $payload")
        verifyNoInteractions(readings)
    }

    private fun area(device: String?) = GrowingArea(id = 7L, name = "Estufa", deviceId = device)

    private fun leitura(id: Long, tipo: String, valor: Double) =
        SensorReading(id = id, deviceId = "esp32-001", sensorType = tipo, value = valor, unit = "%", readingAt = LocalDateTime.now())

    // ---------- listener: leituras válidas ----------

    @Test
    @DisplayName("Leitura numérica simples é gravada com o deviceId do tópico")
    fun grava_leitura_numerica_com_deviceId_do_topico() {
        val r = gravada(topicoUmidade, "58.0")
        assertEquals("esp32-001", r.deviceId)
        assertEquals("soil_moisture", r.sensorType)
        assertEquals(58.0, r.value)
        assertEquals("%", r.unit)
    }

    @Test
    @DisplayName("A unidade gravada é a do tipo de sensor, não a enviada no payload")
    fun grava_com_a_unidade_do_tipo_e_ignora_a_do_payload() {
        val r = gravada(topicoUmidade, """{"value": 61.5, "unit": "XX"}""")
        assertEquals(61.5, r.value)
        assertEquals("%", r.unit)
    }

    @Test
    @DisplayName("JSON sem unidade é aceito e a temperatura recebe °C")
    fun aceita_json_sem_unidade() {
        val r = gravada("cyberflora/esp32-001/sensor/temperature", """{"value": 24.5}""")
        assertEquals(24.5, r.value)
        assertEquals("°C", r.unit)
    }

    @Test
    @DisplayName("Quebra de linha no fim do payload é tolerada")
    fun aceita_quebra_de_linha_no_final() {
        assertEquals(58.0, gravada(topicoUmidade, "58.0\r\n").value)
    }

    @Test
    @DisplayName("Valores nos limites da faixa são aceitos")
    fun aceita_valores_nos_limites_da_faixa() {
        assertEquals(0.0, gravada(topicoUmidade, "0").value)
        assertEquals(100.0, gravada(topicoUmidade, "100").value)
        assertEquals(-40.0, gravada("cyberflora/esp32-001/sensor/temperature", "-40").value)
        assertEquals(85.0, gravada("cyberflora/esp32-001/sensor/temperature", "85").value)
    }

    // ---------- listener: descartes ----------

    @Test
    @DisplayName("Valores logo fora da faixa são descartados")
    fun descarta_valores_fora_da_faixa() {
        assertDescartada(topicoUmidade, "100.1")
        assertDescartada(topicoUmidade, "-0.1")
        assertDescartada("cyberflora/esp32-001/sensor/temperature", "85.1")
        assertDescartada("cyberflora/esp32-001/sensor/temperature", "-40.1")
    }

    @Test
    @DisplayName("Payloads inválidos são descartados")
    fun descarta_payloads_invalidos() {
        assertDescartada(topicoUmidade, "abc")
        assertDescartada(topicoUmidade, "")
        assertDescartada(topicoUmidade, "NaN")
        assertDescartada(topicoUmidade, "1e5")
        assertDescartada(topicoUmidade, """{"value": "x"}""")
        assertDescartada(topicoUmidade, "1234567890")
    }

    @Test
    @DisplayName("Payload acima de 256 caracteres é descartado")
    fun descarta_payload_grande_demais() {
        assertDescartada(topicoUmidade, "1".repeat(300))
    }

    @Test
    @DisplayName("Campos extras no JSON (por exemplo, um deviceId forjado) fazem descartar a mensagem")
    fun descarta_json_com_campos_extras() {
        assertDescartada(topicoUmidade, """{"value": 50, "deviceId": "outro"}""")
    }

    @Test
    @DisplayName("Tipo de sensor desconhecido é descartado")
    fun descarta_tipo_desconhecido() {
        assertDescartada("cyberflora/esp32-001/sensor/desconhecido", "10")
    }

    @Test
    @DisplayName("Tópicos fora do padrão ou de outro sentido são descartados")
    fun descarta_topicos_fora_do_padrao() {
        assertDescartada("outro/esp32-001/sensor/soil_moisture", "10")
        assertDescartada("cyberflora/esp32-001/command/soil_moisture", "10")
        assertDescartada("cyberflora/esp32-001/sensor", "10")
        assertDescartada("cyberflora/esp32-001/sensor/soil_moisture/extra", "10")
    }

    @Test
    @DisplayName("deviceId com caracteres inválidos é descartado")
    fun descarta_deviceId_invalido() {
        assertDescartada("cyberflora/esp 32/sensor/soil_moisture", "10")
        assertDescartada("cyberflora/dev.001/sensor/soil_moisture", "10")
    }

    // ---------- histórico e isolamento ----------

    @Test
    @DisplayName("Histórico de área de outro usuário falha com 404 e não consulta leituras")
    fun historico_de_area_de_outro_usuario_falha() {
        given(areas.findByIdAndOwnerEmail(7L, "beto@teste.com")).willReturn(Optional.empty())
        assertThrows(NoSuchElementException::class.java) { service.history(7L, "beto@teste.com", 24, null) }
        verifyNoInteractions(readings)
    }

    @Test
    @DisplayName("Área sem dispositivo vinculado devolve histórico vazio")
    fun historico_de_area_sem_dispositivo_e_vazio() {
        given(areas.findByIdAndOwnerEmail(7L, dono)).willReturn(Optional.of(area(null)))
        assertTrue(service.history(7L, dono, 24, null).isEmpty())
        verifyNoInteractions(readings)
    }

    @Test
    @DisplayName("Histórico com filtro de tipo usa a consulta por tipo e mapeia a resposta")
    fun historico_filtra_por_tipo() {
        given(areas.findByIdAndOwnerEmail(7L, dono)).willReturn(Optional.of(area("esp32-001")))
        given(
            readings.findByDeviceIdAndSensorTypeAndReadingAtAfterOrderByReadingAtAsc(
                eqOr("esp32-001"), eqOr("soil_moisture"), anyOr(LocalDateTime.now(), LocalDateTime::class.java)
            )
        ).willReturn(listOf(leitura(1L, "soil_moisture", 50.0), leitura(2L, "soil_moisture", 52.0)))
        val r = service.history(7L, dono, 24, "soil_moisture")
        assertEquals(2, r.size)
        assertEquals(52.0, r[1].value)
    }

    @Test
    @DisplayName("Histórico grande é reduzido para no máximo 500 pontos")
    fun historico_e_limitado_a_500_pontos() {
        given(areas.findByIdAndOwnerEmail(7L, dono)).willReturn(Optional.of(area("esp32-001")))
        val muitas = (1..1200).map { leitura(it.toLong(), "soil_moisture", 50.0) }
        given(
            readings.findByDeviceIdAndReadingAtAfterOrderByReadingAtAsc(
                eqOr("esp32-001"), anyOr(LocalDateTime.now(), LocalDateTime::class.java)
            )
        ).willReturn(muitas)
        val r = service.history(7L, dono, 24, null)
        assertTrue(r.size <= 500, "Veio ${r.size} pontos")
        assertEquals(1L, r.first().id)
    }

    @Test
    @DisplayName("latest devolve só os tipos de sensor que têm leitura")
    fun latest_retorna_apenas_tipos_com_leitura() {
        given(areas.findByIdAndOwnerEmail(7L, dono)).willReturn(Optional.of(area("esp32-001")))
        given(readings.findFirstByDeviceIdAndSensorTypeOrderByReadingAtDesc(eqOr("esp32-001"), eqOr("soil_moisture")))
            .willReturn(leitura(9L, "soil_moisture", 44.0))
        val r = service.latest(7L, dono)
        assertEquals(1, r.size)
        assertEquals(44.0, r[0].value)
    }
}