package com.cyberflora.backend.service

import com.cyberflora.backend.dto.SensorReadingResponse
import com.cyberflora.backend.entity.SensorReading
import com.cyberflora.backend.repository.GrowingAreaRepository
import com.cyberflora.backend.repository.SensorReadingRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.math.ceil

private data class Faixa(val min: Double, val max: Double, val unidade: String)

private val FAIXAS = mapOf(
    "soil_moisture" to Faixa(0.0, 100.0, "%"),
    "air_humidity" to Faixa(0.0, 100.0, "%"),
    "temperature" to Faixa(-40.0, 85.0, "°C"),
    "luminosity" to Faixa(0.0, 200000.0, "lx")
)

private val DEVICE_ID = Regex("[A-Za-z0-9_-]{1,64}")
private val NUMERO = Regex("""-?\d{1,9}(\.\d{1,6})?""")
private val JSON_LEITURA = Regex("""\{\s*"value"\s*:\s*(-?\d{1,9}(?:\.\d{1,6})?)\s*(?:,\s*"unit"\s*:\s*"[^"\\]{0,16}"\s*)?\}""")

private const val MAX_PONTOS = 500

@Service
class SensorReadingService(
    private val readingRepository: SensorReadingRepository,
    private val areaRepository: GrowingAreaRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Trata uma mensagem recebida do broker. O deviceId e o tipo vêm do TÓPICO (garantido pela ACL do broker),
     * nunca do conteúdo. A unidade é sempre a do tipo de sensor. Retorna a leitura salva, ou null se descartada.
     */
    fun registerFromMqtt(topic: String, payload: String): SensorReading? {
        val partes = topic.split("/")
        if (partes.size != 4 || partes[0] != "cyberflora" || partes[2] != "sensor") return descartar(topic, "tópico fora do padrão")
        val deviceId = partes[1]
        val tipo = partes[3]
        if (!DEVICE_ID.matches(deviceId)) return descartar(topic, "deviceId inválido")
        val faixa = FAIXAS[tipo] ?: return descartar(topic, "tipo de sensor desconhecido")
        val valor = lerValor(payload) ?: return descartar(topic, "payload inválido")
        if (valor < faixa.min || valor > faixa.max) return descartar(topic, "valor fora da faixa ($valor)")
        return readingRepository.save(
            SensorReading(deviceId = deviceId, sensorType = tipo, value = valor, unit = faixa.unidade, readingAt = LocalDateTime.now())
        )
    }

    fun history(areaId: Long, ownerEmail: String, hours: Long, type: String?): List<SensorReadingResponse> {
        val device = deviceOf(areaId, ownerEmail) ?: return emptyList()
        val desde = LocalDateTime.now().minusHours(hours.coerceIn(1, 72))
        val linhas = if (type.isNullOrBlank())
            readingRepository.findByDeviceIdAndReadingAtAfterOrderByReadingAtAsc(device, desde)
        else
            readingRepository.findByDeviceIdAndSensorTypeAndReadingAtAfterOrderByReadingAtAsc(device, type, desde)
        return reduzir(linhas).map { toResponse(it) }
    }

    fun latest(areaId: Long, ownerEmail: String): List<SensorReadingResponse> {
        val device = deviceOf(areaId, ownerEmail) ?: return emptyList()
        return FAIXAS.keys
            .mapNotNull { readingRepository.findFirstByDeviceIdAndSensorTypeOrderByReadingAtDesc(device, it) }
            .map { toResponse(it) }
    }

    private fun deviceOf(areaId: Long, ownerEmail: String): String? =
        areaRepository.findByIdAndOwnerEmail(areaId, ownerEmail)
            .orElseThrow { NoSuchElementException("Área de cultivo com id $areaId não encontrada") }
            .deviceId

    private fun reduzir(linhas: List<SensorReading>): List<SensorReading> {
        if (linhas.size <= MAX_PONTOS) return linhas
        val passo = ceil(linhas.size.toDouble() / MAX_PONTOS).toInt()
        return linhas.filterIndexed { i, _ -> i % passo == 0 }
    }

    private fun lerValor(payload: String): Double? {
        val texto = payload.trim()
        if (texto.length > 256) return null
        if (NUMERO.matches(texto)) return texto.toDouble()
        return JSON_LEITURA.matchEntire(texto)?.groupValues?.get(1)?.toDouble()
    }

    private fun descartar(topic: String, motivo: String): SensorReading? {
        log.warn("[MQTT] mensagem descartada ({}): {}", motivo, topic.take(120).replace(Regex("[\\r\\n\\t]"), "_"))
        return null
    }

    private fun toResponse(r: SensorReading) = SensorReadingResponse(
        id = r.id ?: 0, deviceId = r.deviceId, sensorType = r.sensorType, value = r.value, unit = r.unit, readingAt = r.readingAt
    )
}