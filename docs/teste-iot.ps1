# Teste do fluxo IoT: ESP32 (simulado) -> Mosquitto -> Listener -> PostgreSQL -> API
$raiz = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $raiz ".env"
function Valor($n) { $l = Get-Content $envFile | Where-Object { $_ -like "$n=*" } | Select-Object -First 1; if (-not $l) { throw "Variavel $n ausente no .env" }; $l.Substring($n.Length + 1).Trim() }
$dev = "esp32-001"; $devPass = Valor "MQTT_DEVICE_PASSWORD"
$api = "http://localhost:8080"; $j = "application/json"; $s = Get-Random
function Qtd($r) { if ($null -eq $r) { 0 } else { @($r).Count } }
function Codigo($sb) { try { & $sb | Out-Null; 200 } catch { [int]$_.Exception.Response.StatusCode } }
function Check($n, $ok) { if ($ok) { Write-Host "PASS  $n" -ForegroundColor Green } else { Write-Host "FAIL  $n" -ForegroundColor Red } }
function Pub($topico, $msg) { $msg | docker exec -i cyberflora-mosquitto mosquitto_pub -h localhost -u $dev -P $devPass -t $topico -q 1 -s | Out-Null }
function Reg($n) { Invoke-RestMethod -Uri "$api/signup" -Method Post -ContentType $j -Body (@{ name = $n; email = "$n$s@teste.com"; password = "Senha@123"; confirmPassword = "Senha@123" } | ConvertTo-Json) }

$a = Reg "ana"; $b = Reg "beto"
$ha = @{ Authorization = "Bearer $($a.token)" }; $hb = @{ Authorization = "Bearer $($b.token)" }
$areaBody = @{ name = "Estufa IoT $s"; description = "Teste IoT"; capacityM2 = 10; irrigationAutomatic = $true; deviceId = $dev } | ConvertTo-Json
try { $area = Invoke-RestMethod -Uri "$api/growing-areas" -Method Post -ContentType $j -Headers $ha -Body $areaBody }
catch { Write-Host "O dispositivo $dev ja esta vinculado a outra area. Edite ou apague essa area e rode de novo." -ForegroundColor Yellow; exit 1 }
Check "area criada com o dispositivo vinculado" ($area.deviceId -eq $dev)

$semDevice = @{ name = "Estufa IoT renomeada $s"; description = "Teste IoT"; capacityM2 = 10; irrigationAutomatic = $true } | ConvertTo-Json
$atual = Invoke-RestMethod -Uri "$api/growing-areas/$($area.id)" -Method Put -ContentType $j -Headers $ha -Body $semDevice
Check "editar a area sem informar o deviceId preserva o vinculo" ($atual.deviceId -eq $dev)

$url = "$api/growing-areas/$($area.id)/readings?hours=1&type=soil_moisture"
$antes = Qtd (Invoke-RestMethod -Uri $url -Headers $ha)
Start-Sleep -Seconds 3
Pub "cyberflora/$dev/sensor/soil_moisture" '{"value": 61.5, "unit": "%"}'
Pub "cyberflora/$dev/sensor/soil_moisture" '58.0'
Pub "cyberflora/$dev/sensor/soil_moisture" '150'
Pub "cyberflora/$dev/sensor/soil_moisture" 'abc'
Pub "cyberflora/$dev/sensor/temperature" '{"value": 24.5}'
for ($i = 0; $i -lt 15; $i++) {
    Start-Sleep -Seconds 1
    $h = Invoke-RestMethod -Uri $url -Headers $ha
    if (((Qtd $h) - $antes) -ge 2) { break }
}
Start-Sleep -Seconds 2
$h = Invoke-RestMethod -Uri $url -Headers $ha
$novas = (Qtd $h) - $antes
Check "2 leituras validas de umidade gravadas (veio: $novas)" ($novas -eq 2)
$ultimo = @($h)[-1]
Check "ultima leitura de umidade = 58.0 em % (veio: $($ultimo.value) $($ultimo.unit))" (($ultimo.value -eq 58.0) -and ($ultimo.unit -eq "%"))
$ult = Invoke-RestMethod -Uri "$api/growing-areas/$($area.id)/readings/latest" -Headers $ha
$temp = @($ult) | Where-Object { $_.sensorType -eq "temperature" } | Select-Object -First 1
Check "latest traz a temperatura 24.5 (veio: $($temp.value))" ($temp.value -eq 24.5)
Check "valor fora da faixa (150) foi descartado" (-not (@($h) | Where-Object { $_.value -eq 150 }))
Check "outro usuario nao ve o historico da area (404)" ((Codigo { Invoke-RestMethod -Uri $url -Headers $hb }) -eq 404)
Check "sem token = 401/403" ((Codigo { Invoke-RestMethod -Uri $url }) -in 401, 403)
$outra = @{ name = "Outra $s"; description = "x"; capacityM2 = 5; irrigationAutomatic = $true; deviceId = $dev } | ConvertTo-Json
Check "outro usuario nao vincula o mesmo dispositivo (409)" ((Codigo { Invoke-RestMethod -Uri "$api/growing-areas" -Method Post -ContentType $j -Headers $hb -Body $outra }) -eq 409)
$ruim = @{ name = "Ruim $s"; description = "x"; capacityM2 = 5; irrigationAutomatic = $true; deviceId = "dev 01" } | ConvertTo-Json
Check "deviceId com caractere invalido = 400" ((Codigo { Invoke-RestMethod -Uri "$api/growing-areas" -Method Post -ContentType $j -Headers $ha -Body $ruim }) -eq 400)
Invoke-RestMethod -Uri "$api/growing-areas/$($area.id)" -Method Delete -Headers $ha | Out-Null
Write-Host "Area de teste removida (o dispositivo $dev foi liberado)." -ForegroundColor DarkGray