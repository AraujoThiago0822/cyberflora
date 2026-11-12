# Teste da ponte: broker publico (Wokwi) -> ponte (Docker) -> Mosquitto local -> backend -> API
$raiz = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $raiz ".env"
function Valor($n) { $l = Get-Content $envFile | Where-Object { $_ -like "$n=*" } | Select-Object -First 1; if (-not $l) { throw "Variavel $n ausente no .env. Rode docs\configurar-wokwi.ps1" }; $l.Substring($n.Length + 1).Trim() }
$broker = Valor "WOKWI_BROKER"; $porta = Valor "WOKWI_PORT"; $prefixo = Valor "WOKWI_PREFIX"
$dev = "wokwi-teste"
$api = "http://localhost:8080"; $j = "application/json"; $s = Get-Random
$img = "eclipse-mosquitto:2.0.18"
function Qtd($r) { if ($null -eq $r) { 0 } else { @($r).Count } }
function Check($n, $ok) { if ($ok) { Write-Host "PASS  $n" -ForegroundColor Green } else { Write-Host "FAIL  $n" -ForegroundColor Red } }
function PubPublico($pref, $tipo, $msg) { $msg | docker run --rm -i $img mosquitto_pub -h $broker -p $porta -t "$pref/$dev/sensor/$tipo" -s 2>&1 | Out-Null }

Check "ponte esta rodando" ((docker ps --filter "name=cyberflora-ponte" --format "{{.Names}}") -eq "cyberflora-ponte")

$reg = Invoke-RestMethod -Uri "$api/signup" -Method Post -ContentType $j -Body (@{ name = "ana"; email = "ana$s@teste.com"; password = "Senha@123"; confirmPassword = "Senha@123" } | ConvertTo-Json)
$ha = @{ Authorization = "Bearer $($reg.token)" }
$corpo = @{ name = "Estufa Wokwi $s"; description = "Teste da ponte"; capacityM2 = 10; irrigationAutomatic = $true; deviceId = $dev } | ConvertTo-Json
try { $area = Invoke-RestMethod -Uri "$api/growing-areas" -Method Post -ContentType $j -Headers $ha -Body $corpo }
catch { Write-Host "O dispositivo $dev ja esta vinculado a outra area. Apague essa area e rode de novo." -ForegroundColor Yellow; exit 1 }
Check "area criada com o dispositivo $dev" ($area.deviceId -eq $dev)

$url = "$api/growing-areas/$($area.id)/readings?hours=1&type=soil_moisture"
$n0 = Qtd (Invoke-RestMethod -Uri $url -Headers $ha)
Start-Sleep -Seconds 3
PubPublico $prefixo "soil_moisture" '{"value": 47.5, "unit": "%"}'
for ($i = 0; $i -lt 25; $i++) { Start-Sleep -Seconds 1; if ((Qtd (Invoke-RestMethod -Uri $url -Headers $ha)) -gt $n0) { break } }
Start-Sleep -Seconds 2
$h = Invoke-RestMethod -Uri $url -Headers $ha
$ultimo = @($h)[-1]
Check "leitura publicada no broker publico chegou ao banco pela ponte (novas: $((Qtd $h) - $n0), valor: $($ultimo.value))" ((((Qtd $h) - $n0) -eq 1) -and ($ultimo.value -eq 47.5))

PubPublico $prefixo "soil_moisture" 'abc'
PubPublico $prefixo "soil_moisture" '150'
PubPublico "outro-prefixo-$s" "soil_moisture" '11.1'
Start-Sleep -Seconds 10
$h2 = Invoke-RestMethod -Uri $url -Headers $ha
Check "payload invalido, valor fora da faixa e prefixo alheio NAO entram (total: $(Qtd $h2))" (((Qtd $h2) -eq (Qtd $h)) -and (@($h2)[-1].value -eq 47.5))

Invoke-RestMethod -Uri "$api/growing-areas/$($area.id)" -Method Delete -Headers $ha | Out-Null
Write-Host "Area de teste removida." -ForegroundColor DarkGray