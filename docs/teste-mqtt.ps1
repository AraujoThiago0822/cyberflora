# Teste do broker MQTT (Mosquitto local): autenticacao, ACL e entrega de mensagens
$raiz = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $raiz ".env"
$cont = "cyberflora-mosquitto"
function Valor($n) {
    $l = Get-Content $envFile | Where-Object { $_ -like "$n=*" } | Select-Object -First 1
    if (-not $l) { throw "Variavel $n ausente no .env" }
    $l.Substring($n.Length + 1).Trim()
}
$bUser = "cyberflora_backend"; $bPass = Valor "MQTT_BACKEND_PASSWORD"
$dUser = "esp32-001";          $dPass = Valor "MQTT_DEVICE_PASSWORD"
$pUser = "ponte";              $pPass = Valor "MQTT_PONTE_PASSWORD"
$script:falhas = 0
function Check($nome, $ok) {
    if ($ok) { Write-Host "PASS  $nome" -ForegroundColor Green }
    else { Write-Host "FAIL  $nome" -ForegroundColor Red; $script:falhas++ }
}
# Escuta ate 1 mensagem (ou 8 s) em segundo plano, executa a acao e devolve o que chegou
function Ouvir-Enquanto($usuario, $senha, $filtro, [scriptblock]$acao) {
    $job = Start-Job -ScriptBlock {
        param($c, $u, $p, $t)
        docker exec $c mosquitto_sub -h localhost -u $u -P $p -t $t -v -C 1 -W 8 2>&1 | Out-String
    } -ArgumentList $cont, $usuario, $senha, $filtro
    Start-Sleep -Seconds 3
    & $acao | Out-Null
    return (Receive-Job $job -Wait -AutoRemoveJob | Out-String)
}
function Pub($usuario, $senha, $topico, $msg) {
    docker exec $cont mosquitto_pub -h localhost -u $usuario -P $senha -t $topico -m $msg 2>&1 | Out-String
}

$topicoLeitura = "cyberflora/$dUser/sensor/soil_moisture"
$topicoComando = "cyberflora/$dUser/command/pump"

Check "container do Mosquitto esta rodando" ((docker ps --filter "name=$cont" --format "{{.Names}}") -eq $cont)

$r = docker exec $cont mosquitto_pub -h localhost -t $topicoLeitura -m x 2>&1 | Out-String
Check "conexao anonima e recusada" ($r -match "not authori[sz]ed")
$r = Pub $dUser "senha-errada-123" $topicoLeitura "x"
Check "senha errada e recusada" ($r -match "not authori[sz]ed")

$r = Ouvir-Enquanto $bUser $bPass "cyberflora/+/sensor/+" { Pub $dUser $dPass $topicoLeitura "leitura-42.5" }
Check "backend recebe a leitura publicada pelo dispositivo" (($r -match "soil_moisture") -and ($r -match "leitura-42.5"))

$r = Ouvir-Enquanto $bUser $bPass "cyberflora/+/sensor/+" { Pub $dUser $dPass "cyberflora/outro-dispositivo/sensor/soil_moisture" "intruso-99" }
Check "dispositivo NAO publica no topico de outro dispositivo (ACL)" (-not ($r -match "intruso-99"))

$r = Ouvir-Enquanto $dUser $dPass "cyberflora/$dUser/command/#" { Pub $bUser $bPass $topicoComando "ON" }
Check "dispositivo recebe o comando enviado pelo backend" ($r -match "pump ON")

$r = Ouvir-Enquanto $dUser $dPass "cyberflora/$dUser/command/#" { Pub $dUser $dPass $topicoComando "HACK" }
Check "dispositivo NAO consegue enviar comandos (ACL)" (-not ($r -match "HACK"))

$r = Ouvir-Enquanto $bUser $bPass "cyberflora/+/sensor/+" { Pub $pUser $pPass $topicoLeitura "ponte-55.5" }
Check "ponte consegue publicar leituras" ($r -match "ponte-55.5")

$r = Ouvir-Enquanto $dUser $dPass "cyberflora/$dUser/command/#" { Pub $pUser $pPass $topicoComando "HACK2" }
Check "ponte NAO consegue enviar comandos (ACL)" (-not ($r -match "HACK2"))

if ($script:falhas -eq 0) { Write-Host "Todos os testes passaram." -ForegroundColor Green }
else { Write-Host "$($script:falhas) teste(s) falharam." -ForegroundColor Red }