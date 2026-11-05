package com.cyberflora.backend.mqtt

import com.cyberflora.backend.service.SensorReadingService
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlin.concurrent.thread

/** Listener MQTT: assina cyberflora/+/sensor/+ (QoS 1) e entrega cada mensagem ao SensorReadingService. */
@Component
class MqttListener(
    private val readingService: SensorReadingService,
    @Value("\${mqtt.enabled:true}") private val enabled: Boolean,
    @Value("\${mqtt.broker-url:tcp://127.0.0.1:1883}") private val brokerUrl: String,
    @Value("\${mqtt.username:cyberflora_backend}") private val brokerUser: String,
    @Value("\${mqtt.password:}") private val brokerPassword: String,
    @Value("\${mqtt.client-id:cyberflora-backend}") private val clientId: String,
    @Value("\${mqtt.topic:cyberflora/+/sensor/+}") private val topic: String
) : MqttCallbackExtended {

    private val log = LoggerFactory.getLogger(javaClass)

    @Volatile private var client: MqttAsyncClient? = null
    @Volatile private var running = false

    @PostConstruct
    fun start() {
        if (!enabled) { log.info("[MQTT] listener desativado (mqtt.enabled=false)"); return }
        if (brokerPassword.isBlank()) { log.warn("[MQTT] MQTT_BACKEND_PASSWORD vazio: listener não iniciado"); return }
        running = true
        thread(name = "mqtt-connect", isDaemon = true) { connectWithRetry() }
    }

    private fun options() = MqttConnectOptions().apply {
        userName = brokerUser
        password = brokerPassword.toCharArray()
        isCleanSession = false          // sessão persistente: o broker guarda mensagens QoS 1 enquanto o backend está fora
        isAutomaticReconnect = true
        connectionTimeout = 10
        keepAliveInterval = 30
    }

    private fun connectWithRetry() {
        while (running) {
            try {
                val c = MqttAsyncClient(brokerUrl, clientId, MemoryPersistence())
                c.setCallback(this)
                client = c
                c.connect(options()).waitForCompletion(15_000)
                c.subscribe(topic, 1).waitForCompletion(15_000)
                log.info("[MQTT] conectado a {} e inscrito em {}", brokerUrl, topic)
                return
            } catch (e: Exception) {
                log.warn("[MQTT] broker indisponível ({}); nova tentativa em 10 s", e.message)
                runCatching { client?.close() }
                Thread.sleep(10_000)
            }
        }
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        if (reconnect) {
            runCatching { client?.subscribe(topic, 1) }.onFailure { log.error("[MQTT] falha ao reassinar: {}", it.message) }
            log.info("[MQTT] reconectado a {}", serverURI)
        }
    }

    override fun connectionLost(cause: Throwable?) {
        log.warn("[MQTT] conexão perdida: {}", cause?.message)
    }

    override fun messageArrived(receivedTopic: String?, message: MqttMessage?) {
        if (receivedTopic == null || message == null) return
        val payload = String(message.payload, Charsets.UTF_8)
        try {
            val saved = readingService.registerFromMqtt(receivedTopic, payload)
            if (saved != null) log.info("[MQTT] {} {} = {} {}", saved.deviceId, saved.sensorType, saved.value, saved.unit)
        } catch (e: Exception) {
            log.error("[MQTT] erro ao processar a mensagem: {}", e.message)
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {}

    @PreDestroy
    fun stop() {
        running = false
        runCatching { client?.disconnect()?.waitForCompletion(3_000) }
        runCatching { client?.close() }
    }
}