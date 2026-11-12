// CyberFlora - firmware do ESP32 (simulado no Wokwi)
// Le a umidade do solo (potenciometro), a temperatura e a umidade do ar (DHT22)
// e publica as leituras por MQTT a cada 10 segundos.
#include <WiFi.h>
#include <PubSubClient.h>
#include <DHTesp.h>
#include "secrets.h"   // DEVICE_ID, MQTT_BROKER, MQTT_PORTA, MQTT_PREFIXO

// Pinos
const int PIN_SOLO = 34;        // ADC1 (entrada analogica) - potenciometro simula o sensor de solo
const int PIN_DHT = 15;         // DHT22 (dados)
const int PIN_LED_STATUS = 2;   // LED verde: conectado ao broker
const int PIN_LED_BOMBA = 4;    // LED azul: simula a bomba de irrigacao

// Calibracao do sensor de solo (leitura bruta 0 a 4095). Sensor capacitivo: seco = valor alto.
// Ajuste com o sensor real: meca no ar/seco e dentro de agua.
const int RAW_SECO = 3200;
const int RAW_MOLHADO = 1300;

// Irrigacao local com histerese: liga abaixo de 40 %, desliga acima de 60 %
const float UMIDADE_LIGA = 40.0;
const float UMIDADE_DESLIGA = 60.0;

const unsigned long INTERVALO_MS = 10000;

WiFiClient rede;
PubSubClient mqtt(rede);
DHTesp dht;
unsigned long ultimoEnvio = 0;
bool primeiroEnvio = true;
bool bombaLigada = false;

float lerUmidadeSolo() {
  long soma = 0;
  for (int i = 0; i < 16; i++) {   // media de 16 leituras reduz o ruido
    soma += analogRead(PIN_SOLO);
    delay(2);
  }
  float bruto = soma / 16.0;
  float pct = (RAW_SECO - bruto) * 100.0 / (RAW_SECO - RAW_MOLHADO);
  return constrain(pct, 0.0, 100.0);
}

bool publicar(const char* tipo, float valor, const char* unidade) {
  char topico[96];
  char payload[64];
  snprintf(topico, sizeof(topico), "%s/%s/sensor/%s", MQTT_PREFIXO, DEVICE_ID, tipo);
  snprintf(payload, sizeof(payload), "{\"value\": %.1f, \"unit\": \"%s\"}", valor, unidade);
  return mqtt.publish(topico, payload);
}

void conectarWifi() {
  WiFi.mode(WIFI_STA);
  WiFi.begin("Wokwi-GUEST", "", 6);   // rede aberta do simulador
  Serial.print("Conectando ao Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    digitalWrite(PIN_LED_STATUS, !digitalRead(PIN_LED_STATUS));
    delay(250);
    Serial.print(".");
  }
  Serial.println(" ok");
}

void conectarMqtt() {
  digitalWrite(PIN_LED_STATUS, LOW);
  while (!mqtt.connected()) {
    Serial.print("Conectando ao broker...");
    String id = String("cyberflora-") + DEVICE_ID + "-" + String(random(10000, 99999));
    if (mqtt.connect(id.c_str())) {
      Serial.println(" ok");
    } else {
      Serial.print(" falhou, rc=");
      Serial.println(mqtt.state());
      delay(3000);
    }
  }
  digitalWrite(PIN_LED_STATUS, HIGH);
}

void enviarLeituras() {
  float solo = lerUmidadeSolo();
  TempAndHumidity ar = dht.getTempAndHumidity();
  bool dhtOk = (dht.getStatus() == DHTesp::ERROR_NONE);

  bool ok = publicar("soil_moisture", solo, "%");
  if (dhtOk) {
    ok = publicar("temperature", ar.temperature, "C") && ok;
    ok = publicar("air_humidity", ar.humidity, "%") && ok;
  }

  if (!bombaLigada && solo < UMIDADE_LIGA) bombaLigada = true;
  else if (bombaLigada && solo > UMIDADE_DESLIGA) bombaLigada = false;
  digitalWrite(PIN_LED_BOMBA, bombaLigada ? HIGH : LOW);

  Serial.printf("solo=%.1f%%  temp=%.1fC  ar=%.1f%%  bomba=%s  envio=%s\n",
                solo, dhtOk ? ar.temperature : NAN, dhtOk ? ar.humidity : NAN,
                bombaLigada ? "LIGADA" : "desligada", ok ? "ok" : "FALHOU");
}

void setup() {
  Serial.begin(115200);
  pinMode(PIN_LED_STATUS, OUTPUT);
  pinMode(PIN_LED_BOMBA, OUTPUT);
  analogReadResolution(12);
  dht.setup(PIN_DHT, DHTesp::DHT22);
  mqtt.setServer(MQTT_BROKER, MQTT_PORTA);
  conectarWifi();
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) conectarWifi();
  if (!mqtt.connected()) conectarMqtt();
  mqtt.loop();

  unsigned long agora = millis();
  if (primeiroEnvio || agora - ultimoEnvio >= INTERVALO_MS) {
    primeiroEnvio = false;
    ultimoEnvio = agora;
    enviarLeituras();
  }
}