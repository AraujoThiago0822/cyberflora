# Simula o ESP32 publicando leituras no Mosquitto (para testar o historico e o grafico)
param([int]$Minutos = 3, [string]$Dispositivo = "esp32-001")
$raiz = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $raiz ".env"
function Valor($n) { $l = Get-Content $envFile | Where-Object { $_ -like "$n=*" } | Select-Object -First 1; if (-not $l) { throw "Variavel $n ausente no .env" }; $l.Substring($n.Length + 1).Trim() }
$senha = Valor "MQTT_DEVICE_PASSWORD"
$inv = [System.Globalization.CultureInfo]::InvariantCulture
function Pub($tipo, $valor) {
    $txt = [string]::Format($inv, "{0:F1}", $valor)
    docker exec cyberflora-mosquitto mosquitto_pub -h localhost -u $Dispositivo -P $senha -t "cyberflora/$Dispositivo/sensor/$tipo" -m $txt | Out-Null
}
$fim = (Get-Date).AddMinutes($Minutos)
$i = 0; $solo = 60.0
Write-Host "Publicando para $Dispositivo por $Minutos minuto(s). Ctrl+C para parar." -ForegroundColor Cyan
while ((Get-Date) -lt $fim) {
    $i++
    $solo = [Math]::Max(15.0, [Math]::Min(90.0, $solo + (Get-Random -Minimum -30 -Maximum 31) / 10.0))
    Pub "soil_moisture" $solo
    Pub "temperature" (26 + 3 * [Math]::Sin($i / 6.0))
    Pub "air_humidity" (65 + 10 * [Math]::Cos($i / 8.0))
    Write-Host ("[{0}] solo={1:F1}%" -f (Get-Date -Format "HH:mm:ss"), $solo)
    Start-Sleep -Seconds 3
}
Write-Host "Simulacao concluida." -ForegroundColor Green