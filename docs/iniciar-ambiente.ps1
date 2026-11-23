# Prepara e sobe o ambiente integrado: banco, Mosquitto e ponte do Wokwi (e, opcionalmente, o backend).
# Uso: powershell -ExecutionPolicy Bypass -File docs\iniciar-ambiente.ps1 [-Backend] [-Projeto nome]
param([string]$Raiz = "", [string]$Projeto = "", [switch]$Backend)
if (-not $Raiz) { $Raiz = Split-Path -Parent $PSScriptRoot }
docker info *> $null
if ($LASTEXITCODE -ne 0) { Write-Host "Docker Desktop fechado. Abra-o, espere 'Engine running' e rode de novo." -ForegroundColor Red; return }
$EnvFile = Join-Path $Raiz ".env"
if (-not (Test-Path $EnvFile)) { Copy-Item (Join-Path $Raiz ".env.example") $EnvFile; Write-Host ".env criado a partir do modelo." -ForegroundColor Yellow }
if (-not (Test-Path -PathType Leaf (Join-Path $Raiz "docker\mosquitto\passwd"))) {
    Write-Host "Gerando as senhas do Mosquitto..." -ForegroundColor Cyan
    & powershell -ExecutionPolicy Bypass -File (Join-Path $Raiz "docker\mosquitto\setup-mosquitto.ps1")
}
$Prefixo = ""
$Linha = Get-Content $EnvFile | Where-Object { $_ -like "WOKWI_PREFIX=*" } | Select-Object -First 1
if ($Linha) { $Prefixo = $Linha.Substring(14).Trim() }
if ((-not $Prefixo) -or ($Prefixo -like "*troque*") -or (-not (Test-Path (Join-Path $Raiz "iot\wokwi\secrets.h")))) {
    Write-Host "Configurando o modo Wokwi (prefixo e secrets.h)..." -ForegroundColor Cyan
    & powershell -ExecutionPolicy Bypass -File (Join-Path $Raiz "docs\configurar-wokwi.ps1")
    $Linha = Get-Content $EnvFile | Where-Object { $_ -like "WOKWI_PREFIX=*" } | Select-Object -First 1
    $Prefixo = $Linha.Substring(14).Trim()
}
Push-Location (Join-Path $Raiz "docker")
if ($Projeto) { docker compose -p $Projeto --profile wokwi up -d } else { docker compose --profile wokwi up -d }
Pop-Location
Start-Sleep -Seconds 8
docker ps --format "table {{.Names}}\t{{.Status}}"
$Estado = docker inspect -f "{{.State.Running}} {{.State.Restarting}}" cyberflora-ponte 2>$null
if ($Estado -eq "true false") { Write-Host "Ponte do Wokwi no ar." -ForegroundColor Green }
else { Write-Host "A ponte nao esta estavel. Rode: powershell -ExecutionPolicy Bypass -File docs\corrigir-ponte.ps1" -ForegroundColor Red }
Write-Host ""
Write-Host "Prefixo dos topicos: $Prefixo" -ForegroundColor Cyan
Write-Host "Wokwi: o MQTT_PREFIXO do secrets.h do projeto no wokwi.com deve ser igual a esse prefixo (arquivo: iot\wokwi\secrets.h). Depois clique em Start (a seta verde)."
Write-Host "Backend: cd backend ; .\gradlew.bat bootRun   (aplicativo: cd frontend-kmp ; .\gradlew.bat :desktopApp:run)"
if ($Backend) { Start-Process powershell -ArgumentList '-NoExit', '-Command', ("cd '" + (Join-Path $Raiz "backend") + "'; .\gradlew.bat bootRun") }
