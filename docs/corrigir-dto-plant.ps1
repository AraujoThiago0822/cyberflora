# Ajusta o @Size do DTO PlantRequest (nome 100 e especie 150) e roda o teste de validacao.
# Uso: powershell -ExecutionPolicy Bypass -File docs\corrigir-dto-plant.ps1
param([string]$Raiz = "C:\projetos\cyberflora")
$Dir = Join-Path $Raiz "backend\src\main\kotlin\com\cyberflora\backend\dto"
$Arq = Get-ChildItem $Dir -Filter *.kt | Where-Object { Select-String -Path $_.FullName -Pattern 'class\s+PlantRequest' -Quiet } | Select-Object -First 1
if (-not $Arq) { Write-Host "Nao achei a classe PlantRequest em $Dir" -ForegroundColor Red; return }
Write-Host "Arquivo: $($Arq.FullName)" -ForegroundColor Cyan
$Linhas = New-Object System.Collections.Generic.List[string]
foreach ($L in [System.IO.File]::ReadAllLines($Arq.FullName)) { $Linhas.Add($L) }
$Ini = -1
for ($k = 0; $k -lt $Linhas.Count; $k++) { if ($Linhas[$k] -match 'class\s+PlantRequest') { $Ini = $k; break } }
$Fim = $Linhas.Count - 1
for ($k = $Ini + 1; $k -lt $Linhas.Count; $k++) { if ($Linhas[$k] -match '^\)') { $Fim = $k; break } }
Write-Host "--- PlantRequest ANTES ---"
for ($k = $Ini; $k -le $Fim; $k++) { Write-Host $Linhas[$k] }
function Ajustar($Prop, $Max, $Msg) {
    $I = -1
    for ($k = $Ini; $k -le $script:Fim; $k++) { if ($Linhas[$k] -match ('^\s*val\s+' + $Prop + '\s*:')) { $I = $k; break } }
    if ($I -lt 0) { Write-Host "Campo $Prop nao encontrado" -ForegroundColor Yellow; return $false }
    $J = $I - 1; $Achou = -1
    while (($J -gt $Ini) -and $Linhas[$J].Trim().StartsWith('@')) { if ($Linhas[$J] -match '@field:Size') { $Achou = $J }; $J-- }
    if (($Achou -ge 0) -and ($Linhas[$Achou] -match 'max\s*=\s*(\d+)')) {
        if ([int]$Matches[1] -eq $Max) { Write-Host "$Prop : @Size(max = $Max) ja existe" -ForegroundColor Green; return $false }
        $Linhas[$Achou] = [regex]::Replace($Linhas[$Achou], 'max\s*=\s*\d+', "max = $Max")
        Write-Host "$Prop : limite ajustado para $Max" -ForegroundColor Yellow; return $true
    }
    $Ind = [regex]::Match($Linhas[$I], '^\s*').Value
    $Linhas.Insert($I, $Ind + '@field:Size(max = ' + $Max + ', message = "' + $Msg + '")')
    $script:Fim++
    Write-Host "$Prop : @Size(max = $Max) acrescentado" -ForegroundColor Yellow; return $true
}
$Mudou1 = Ajustar "name" 100 "O nome deve ter no máximo 100 caracteres"
$Mudou2 = Ajustar "species" 150 "A espécie deve ter no máximo 150 caracteres"
if ($Mudou1 -or $Mudou2) {
    if (-not ($Linhas | Where-Object { $_ -match '^import jakarta\.validation\.constraints\.Size' })) {
        $Ult = -1
        for ($k = 0; $k -lt $Linhas.Count; $k++) { if ($Linhas[$k] -match '^import ') { $Ult = $k } }
        if ($Ult -ge 0) { $Linhas.Insert($Ult + 1, 'import jakarta.validation.constraints.Size') }
        else { for ($k = 0; $k -lt $Linhas.Count; $k++) { if ($Linhas[$k] -match '^package ') { $Linhas.Insert($k + 1, ""); $Linhas.Insert($k + 2, 'import jakarta.validation.constraints.Size'); break } } }
        Write-Host "import do Size acrescentado" -ForegroundColor Yellow
    }
    [System.IO.File]::WriteAllLines($Arq.FullName, $Linhas, (New-Object System.Text.UTF8Encoding $false))
    Write-Host "Arquivo gravado." -ForegroundColor Green
} else { Write-Host "Nada a alterar no DTO." -ForegroundColor Green }
Write-Host "Rodando PlantRequestValidationTest..." -ForegroundColor Cyan
Push-Location (Join-Path $Raiz "backend")
& .\gradlew.bat test --tests "*PlantRequestValidationTest" --console=plain *> (Join-Path $env:TEMP "cf-dto.txt")
Pop-Location
$Xml = Join-Path $Raiz "backend\build\test-results\test\TEST-com.cyberflora.backend.dto.PlantRequestValidationTest.xml"
if (Test-Path $Xml) {
    [xml]$X = [System.IO.File]::ReadAllText($Xml)
    Write-Host ("Testes: {0}  Falhas: {1}  Erros: {2}" -f $X.testsuite.tests, $X.testsuite.failures, $X.testsuite.errors) -ForegroundColor Cyan
    foreach ($C in @($X.testsuite.testcase)) { if ($C.failure) { Write-Host ("FALHOU: " + $C.name) -ForegroundColor Red; Write-Host ("  " + $C.failure.message) } }
} else { Write-Host "Sem resultado. Primeiras linhas de erro:" -ForegroundColor Red; Select-String (Join-Path $env:TEMP "cf-dto.txt") -Pattern "^e: |What went wrong" | Select-Object -First 6 | ForEach-Object { $_.Line } }
