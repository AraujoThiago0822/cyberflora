# Corrige a ponte do Wokwi (container em loop de reinicio), garante fim de linha LF e recria o container.
# Uso: powershell -ExecutionPolicy Bypass -File docs\corrigir-ponte.ps1   (no clone: -Projeto clonetest)
param([string]$Raiz = "", [string]$Projeto = "")
if (-not $Raiz) { $Raiz = Split-Path -Parent $PSScriptRoot }
$Utf8 = New-Object System.Text.UTF8Encoding $false
$Docker = Join-Path $Raiz "docker"
$Ponte = Join-Path $Docker "ponte\ponte.sh"
Write-Host "== log atual da ponte ==" -ForegroundColor Cyan
docker logs cyberflora-ponte --tail 6 2>&1
if (Test-Path $Ponte) { Write-Host ("ponte.sh com CRLF (causa comum do erro): " + ([System.IO.File]::ReadAllText($Ponte)).Contains("`r")) -ForegroundColor Yellow }
$Novo = @'
#!/bin/sh
# Ponte: repassa leituras do broker publico (Wokwi) para o Mosquitto local.
# Usa a identidade "ponte" (ACL: so publica leituras). Valida deviceId, tipo e tamanho.
for v in WOKWI_BROKER WOKWI_PORT WOKWI_PREFIX MQTT_PONTE_PASSWORD; do
  eval "val=\${$v:-}"
  if [ -z "$val" ]; then echo "[ponte] ERRO: variavel $v ausente no .env (rode docs/configurar-wokwi.ps1)"; sleep 30; exit 1; fi
done
WOKWI_BROKER=$(printf '%s' "$WOKWI_BROKER" | tr -d '\r'); WOKWI_PORT=$(printf '%s' "$WOKWI_PORT" | tr -d '\r')
WOKWI_PREFIX=$(printf '%s' "$WOKWI_PREFIX" | tr -d '\r'); MQTT_PONTE_PASSWORD=$(printf '%s' "$MQTT_PONTE_PASSWORD" | tr -d '\r')
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
'@
[System.IO.File]::WriteAllText($Ponte, ($Novo -replace "`r`n", "`n"), $Utf8)
$Compose = Join-Path $Docker "docker-compose.yml"
$C = [System.IO.File]::ReadAllText($Compose)
$Entry = @'
entrypoint: ["sh", "-c", "tr -d '\\r' < /ponte.sh > /tmp/ponte.sh && sh /tmp/ponte.sh"]
'@
$C = [regex]::Replace($C, 'entrypoint:\s*\[[^\]]*\]', $Entry.Trim())
[System.IO.File]::WriteAllText($Compose, ($C -replace "`r`n", "`n"), $Utf8)
$Ga = Join-Path $Raiz ".gitattributes"
$Atual = ""; if (Test-Path $Ga) { $Atual = [System.IO.File]::ReadAllText($Ga) }
foreach ($L in @("*.sh text eol=lf", "docker/mosquitto/* text eol=lf", "docker/ponte/* text eol=lf")) { if ($Atual -notmatch [regex]::Escape($L)) { $Atual = $Atual.TrimEnd() + "`n" + $L + "`n" } }
[System.IO.File]::WriteAllText($Ga, $Atual.TrimStart(), $Utf8)
Write-Host "ponte.sh, docker-compose.yml e .gitattributes atualizados." -ForegroundColor Green
Push-Location $Docker
if ($Projeto) { docker compose -p $Projeto --profile wokwi up -d --force-recreate ponte } else { docker compose --profile wokwi up -d --force-recreate ponte }
Pop-Location
Start-Sleep -Seconds 8
Write-Host "== log depois da correcao ==" -ForegroundColor Cyan
docker logs cyberflora-ponte --tail 8 2>&1
$Estado = docker inspect -f "{{.State.Running}} {{.State.Restarting}}" cyberflora-ponte
if ($Estado -eq "true false") { Write-Host "Ponte no ar, sem reiniciar em loop." -ForegroundColor Green }
else { Write-Host "A ponte ainda nao esta estavel (estado: $Estado). Copie as linhas do log acima e envie." -ForegroundColor Red }
