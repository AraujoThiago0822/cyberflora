# Configura o modo Wokwi: grava WOKWI_* no .env e gera iot\wokwi\secrets.h
$raiz = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $raiz ".env"
$exFile = Join-Path $raiz ".env.example"
$utf8 = New-Object System.Text.UTF8Encoding $false
if (-not (Test-Path $envFile)) { throw ".env nao encontrado em $raiz" }

function Ler-Env($arq, $nome) {
    if (-not (Test-Path $arq)) { return "" }
    $l = Get-Content $arq | Where-Object { $_ -like "$nome=*" } | Select-Object -First 1
    if ($l) { return $l.Substring($nome.Length + 1).Trim() } else { return "" }
}
function Gravar-Env($arq, $nome, $valor, [bool]$soSeAusente) {
    $linhas = New-Object System.Collections.Generic.List[string]
    if (Test-Path $arq) { foreach ($l in (Get-Content $arq)) { $linhas.Add($l) } }
    $achou = $false
    for ($i = 0; $i -lt $linhas.Count; $i++) {
        if ($linhas[$i] -like "$nome=*") { $achou = $true; if (-not $soSeAusente) { $linhas[$i] = "$nome=$valor" } }
    }
    if (-not $achou) { $linhas.Add("$nome=$valor") }
    [System.IO.File]::WriteAllText($arq, (($linhas -join "`n") + "`n"), $utf8)
}

$broker = Ler-Env $envFile "WOKWI_BROKER"; if (-not $broker) { $broker = "broker.hivemq.com" }
$porta = Ler-Env $envFile "WOKWI_PORT"; if (-not $porta) { $porta = "1883" }
$prefixo = Ler-Env $envFile "WOKWI_PREFIX"
if (-not $prefixo -or $prefixo -like "*troque*") {
    $chars = 'abcdefghijklmnopqrstuvwxyz0123456789'
    $b = New-Object byte[] 12
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b)
    $prefixo = "cyberflora-" + (-join ($b | ForEach-Object { $chars[$_ % $chars.Length] }))
}
Gravar-Env $envFile "WOKWI_BROKER" $broker $false
Gravar-Env $envFile "WOKWI_PORT" $porta $false
Gravar-Env $envFile "WOKWI_PREFIX" $prefixo $false
Gravar-Env $exFile "WOKWI_BROKER" "broker.hivemq.com" $true
Gravar-Env $exFile "WOKWI_PORT" "1883" $true
Gravar-Env $exFile "WOKWI_PREFIX" "cyberflora-troque-este-prefixo" $true

$secrets = "#pragma once`n// Gerado por docs\configurar-wokwi.ps1. NAO versionar.`n#define DEVICE_ID `"esp32-001`"`n#define MQTT_BROKER `"$broker`"`n#define MQTT_PORTA $porta`n#define MQTT_PREFIXO `"$prefixo`"`n"
$dest = Join-Path $raiz "iot\wokwi\secrets.h"
[System.IO.File]::WriteAllText($dest, $secrets, $utf8)
Write-Host "Modo Wokwi configurado." -ForegroundColor Green
Write-Host "  Broker publico : ${broker}:${porta}"
Write-Host "  Prefixo        : $prefixo"
Write-Host "  secrets.h      : $dest"