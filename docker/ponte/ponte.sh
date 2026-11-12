#!/bin/sh
# Ponte: repassa leituras do broker publico (Wokwi) para o Mosquitto local.
# Usa a identidade "ponte" (ACL: so publica leituras). Valida deviceId, tipo e tamanho.
set -u
: "${WOKWI_BROKER:?}" "${WOKWI_PORT:?}" "${WOKWI_PREFIX:?}" "${MQTT_PONTE_PASSWORD:?}"
LOCAL_HOST="${LOCAL_HOST:-mosquitto}"
echo "[ponte] escutando ${WOKWI_BROKER}:${WOKWI_PORT} em ${WOKWI_PREFIX}/+/sensor/+"
while true; do
  mosquitto_sub -h "$WOKWI_BROKER" -p "$WOKWI_PORT" -t "${WOKWI_PREFIX}/+/sensor/+" -F '%t %p' | while read -r topico payload; do
    dispositivo=$(echo "$topico" | cut -d/ -f2)
    tipo=$(echo "$topico" | cut -d/ -f4)
    case "$dispositivo" in ""|*[!A-Za-z0-9_-]*) echo "[ponte] descartada (deviceId invalido): $topico"; continue ;; esac
    case "$tipo" in ""|*[!a-z_]*) echo "[ponte] descartada (tipo invalido): $topico"; continue ;; esac
    [ "${#payload}" -le 128 ] || { echo "[ponte] descartada (payload grande): $topico"; continue; }
    mosquitto_pub -h "$LOCAL_HOST" -p 1883 -u ponte -P "$MQTT_PONTE_PASSWORD" \
      -t "cyberflora/${dispositivo}/sensor/${tipo}" -m "$payload" \
      && echo "[ponte] ${dispositivo} ${tipo} = ${payload}"
  done
  echo "[ponte] conexao com o broker publico caiu; nova tentativa em 5 s"
  sleep 5
done