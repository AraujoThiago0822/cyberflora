# Gera docker\mosquitto\passwd a partir das senhas do arquivo .env (raiz do projeto)
$raiz = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$envFile = Join-Path $raiz ".env"
if (-not (Test-Path $envFile)) { throw "Arquivo .env nao encontrado em $raiz" }

function Valor($nome) {
    $l = Get-Content $envFile | Where-Object { $_ -like "$nome=*" } | Select-Object -First 1
    if (-not $l) { throw "Variavel $nome ausente no .env" }
    $v = $l.Substring($nome.Length + 1).Trim()
    if ([string]::IsNullOrWhiteSpace($v)) { throw "Variavel $nome esta vazia no .env" }
    return $v
}

$pasta = $PSScriptRoot
$arquivo = Join-Path $pasta "passwd"
$imagem = "eclipse-mosquitto:2.0.18"
# Se o Docker criou uma PASTA com esse nome (acontece quando o arquivo nao existe), remove
if (Test-Path $arquivo) { Remove-Item -Recurse -Force $arquivo }

$usuarios = @(
    @("cyberflora_backend", "MQTT_BACKEND_PASSWORD"),
    @("esp32-001", "MQTT_DEVICE_PASSWORD"),
    @("ponte", "MQTT_PONTE_PASSWORD")
)
$primeiro = $true
foreach ($u in $usuarios) {
    $docker = @("run", "--rm", "-v", "${pasta}:/mosquitto/config", $imagem, "mosquitto_passwd", "-b")
    if ($primeiro) { $docker += "-c" }
    $docker += @("/mosquitto/config/passwd", $u[0], (Valor $u[1]))
    & docker @docker
    if ($LASTEXITCODE -ne 0) { throw "Falha ao criar o usuario $($u[0])" }
    $primeiro = $false
}
Write-Host "Arquivo passwd gerado com os usuarios: cyberflora_backend, esp32-001 e ponte" -ForegroundColor Green