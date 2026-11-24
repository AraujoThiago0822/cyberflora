# Verifica AO VIVO se o Wokwi esta integrado ao sistema:
# Wokwi -> broker publico -> ponte -> Mosquitto -> backend -> banco -> API.
# Tambem deixa pronto o usuario demo e a area com o dispositivo, para entrar nos aplicativos.
# Uso (backend e ponte no ar, simulacao do Wokwi iniciada):
#   powershell -ExecutionPolicy Bypass -File docs\verificar-wokwi-ao-vivo.ps1
param([string]$Api = "http://localhost:8080", [string]$Dispositivo = "esp32-001", [int]$EsperaSegundos = 90)
$J = "application/json"
$Email = "demo@cyberflora.com"; $Senha = "Senha@123"
function Check($Nome, $Cond, $Det) {
    if ($Cond) { Write-Host ("PASS  " + $Nome) -ForegroundColor Green }
    else { Write-Host ("FAIL  " + $Nome + "  " + $Det) -ForegroundColor Red }
}
try { Invoke-RestMethod -Uri "$Api/signup" -Method Post -ContentType $J -Body (@{ name = "Demo CyberFlora"; email = $Email; password = $Senha; confirmPassword = $Senha } | ConvertTo-Json) | Out-Null } catch { }
try { $Login = Invoke-RestMethod -Uri "$Api/signin" -Method Post -ContentType $J -Body (@{ email = $Email; password = $Senha } | ConvertTo-Json) }
catch { Write-Host "FAIL  backend fora do ar em $Api (ou login falhou). Ligue o backend: cd backend ; .\gradlew.bat bootRun" -ForegroundColor Red; return }
Check "backend no ar e login do usuario demo" ($Login.token.Length -gt 20) ""
$H = @{ Authorization = "Bearer $($Login.token)" }
$Areas = Invoke-RestMethod -Uri "$Api/growing-areas" -Headers $H
$Area = @($Areas) | Where-Object { $_.deviceId -eq $Dispositivo } | Select-Object -First 1
if (-not $Area) {
    try { $Area = Invoke-RestMethod -Uri "$Api/growing-areas" -Method Post -ContentType $J -Headers $H -Body (@{ name = "Estufa Wokwi"; description = "Area ligada ao simulador"; capacityM2 = 10; irrigationAutomatic = $true; deviceId = $Dispositivo } | ConvertTo-Json) }
    catch { Write-Host "FAIL  o dispositivo $Dispositivo ja esta vinculado a OUTRO usuario. Apague ou edite essa area e rode de novo." -ForegroundColor Red; return }
}
Check "area com o dispositivo $Dispositivo pronta (id $($Area.id))" ($Area.deviceId -eq $Dispositivo) ""
$Tipos = @("soil_moisture", "temperature", "air_humidity")
$Fim = (Get-Date).AddSeconds($EsperaSegundos); $Achou = @{}
Write-Host "Aguardando leituras do Wokwi por ate $EsperaSegundos s (inicie a simulacao em wokwi.com: seta verde)..." -ForegroundColor Cyan
while (((Get-Date) -lt $Fim) -and ($Achou.Count -lt $Tipos.Count)) {
    $Ult = Invoke-RestMethod -Uri "$Api/growing-areas/$($Area.id)/readings/latest" -Headers $H
    foreach ($R in @($Ult)) {
        if ($Tipos -contains $R.sensorType) {
            $Idade = ((Get-Date) - [datetime]$R.readingAt).TotalSeconds
            if (($Idade -lt 60) -and ($Idade -gt -5)) { $Achou[$R.sensorType] = $R }
        }
    }
    if ($Achou.Count -lt $Tipos.Count) { Start-Sleep -Seconds 3 }
}
foreach ($T in $Tipos) {
    $R = $Achou[$T]
    Check ("leitura recente de " + $T + " vinda do Wokwi") ($null -ne $R) "nada nos ultimos 60 s"
    if ($R) { Write-Host ("        " + $R.value + " " + $R.unit + "  (" + $R.readingAt + ")") -ForegroundColor DarkGray }
}
Write-Host ""
if ($Achou.Count -eq $Tipos.Count) {
    Write-Host "INTEGRACAO COMPLETA: Wokwi, broker, ponte, Mosquitto, backend, banco e API estao conectados." -ForegroundColor Green
    Write-Host "Entre nos aplicativos com:  $Email  /  $Senha  e abra Areas > Estufa Wokwi > historico."
} else {
    Write-Host "Nada (ou so parte) chegou. Confira, nesta ordem:" -ForegroundColor Yellow
    Write-Host "  1) Wokwi: o Serial Monitor mostra 'envio=ok'? O MQTT_PREFIXO do secrets.h do Wokwi e igual ao WOKWI_PREFIX do .env?"
    Write-Host "  2) Ponte:   docker logs cyberflora-ponte --tail 8   (devem aparecer linhas '[ponte] esp32-001 ...')"
    Write-Host "  3) Backend: a janela do backend mostra '[MQTT] conectado' e linhas '[MQTT] esp32-001 ...'?"
}
