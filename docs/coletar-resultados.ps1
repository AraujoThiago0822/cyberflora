# Coleta os resultados dos testes de unidade (JUnit) e da cobertura (JaCoCo) para preencher o item 10 do documento.
# Uso: powershell -ExecutionPolicy Bypass -File docs\coletar-resultados.ps1
param([string]$Raiz = "C:\projetos\cyberflora")
$Backend = Join-Path $Raiz "backend"
$Log = Join-Path $env:TEMP "cf-testes.txt"
# Garante que o JaCoCo esta configurado (plugin, relatorio XML/HTML e execucao depois dos testes)
$Gradle = Join-Path $Backend "build.gradle.kts"
$Gt = [System.IO.File]::ReadAllText($Gradle); $Mudou = $false
if (($Gt -notmatch '(?m)^\s*jacoco\s*$') -and ($Gt -notmatch 'id\("jacoco"\)')) { $Gt = ([regex]'plugins\s*\{').Replace($Gt, "plugins {`n    jacoco", 1); $Mudou = $true }
if ($Gt -notmatch 'JacocoReport') { $Gt = $Gt.TrimEnd() + "`n`ntasks.withType<JacocoReport>().configureEach {`n    reports {`n        xml.required.set(true)`n        html.required.set(true)`n    }`n}`n"; $Mudou = $true }
if ($Gt -notmatch 'finalizedBy\("jacocoTestReport"\)') { $Gt = $Gt.TrimEnd() + "`n`ntasks.withType<Test> {`n    finalizedBy(""jacocoTestReport"")`n}`n"; $Mudou = $true }
if ($Mudou) { [System.IO.File]::WriteAllText($Gradle, $Gt, (New-Object System.Text.UTF8Encoding $false)); Write-Host "build.gradle.kts ajustado para o JaCoCo." -ForegroundColor Yellow }
Write-Host "1/4 Executando os testes de unidade do UC01 (leva alguns minutos)..." -ForegroundColor Cyan
Push-Location $Backend
& .\gradlew.bat test --tests "*PlantServiceTest" --tests "*PlantMapperTest" --tests "*PlantTest" --tests "*UserTest" --tests "*PlantRequestValidationTest" --console=plain *> $Log
Pop-Location
$PastaXml = Join-Path $Backend "build\test-results\test"
if (-not (Test-Path $PastaXml)) {
    Write-Host "Nao encontrei os resultados. Primeiras linhas de erro do log ($Log):" -ForegroundColor Red
    Select-String $Log -Pattern "^e: |FAILED|error:|What went wrong" | Select-Object -First 8 | ForEach-Object { $_.Line }
    return
}
Write-Host "2/4 Lendo os resultados do JUnit..." -ForegroundColor Cyan
$Total = 0; $Falhas = 0; $Erros = 0; $Ignorados = 0; $Falharam = @()
foreach ($Arq in Get-ChildItem $PastaXml -Filter "TEST-*.xml") {
    [xml]$Xml = [System.IO.File]::ReadAllText($Arq.FullName)
    $Suite = $Xml.testsuite
    $Total += [int]$Suite.tests; $Falhas += [int]$Suite.failures; $Erros += [int]$Suite.errors; $Ignorados += [int]$Suite.skipped
    foreach ($Caso in @($Suite.testcase)) {
        if ($Caso.failure -or $Caso.error) { $Msg = ""; if ($Caso.failure) { $Msg = [string]$Caso.failure.message }; if ($Msg.Length -gt 150) { $Msg = $Msg.Substring(0, 150) }; $Falharam += ("{0}.{1} -> {2}" -f $Suite.name.Split('.')[-1], $Caso.name, $Msg) }
    }
}
$Passaram = $Total - $Falhas - $Erros - $Ignorados
Write-Host "3/4 Lendo a cobertura do JaCoCo..." -ForegroundColor Cyan
$XmlJ = Join-Path $Backend "build\reports\jacoco\test\jacocoTestReport.xml"
if (-not (Test-Path $XmlJ)) {
    Push-Location $Backend
    & .\gradlew.bat jacocoTestReport --console=plain *> (Join-Path $env:TEMP "cf-jacoco.txt")
    Pop-Location
}
$Linhas = @()
if (Test-Path $XmlJ) {
    $Texto = [System.IO.File]::ReadAllText($XmlJ) -replace '<!DOCTYPE[^>]*>', ''
    [xml]$J = $Texto
    $Alvo = @("service/PlantService", "mapper/PlantMapper", "entity/Plant", "entity/User", "dto/PlantRequest", "exception/BusinessRuleException")
    foreach ($A in $Alvo) {
        $Nome = "com/cyberflora/backend/$A"
        $Mi = 0; $Ci = 0; $Mb = 0; $Cb = 0; $Metodos = @{}
        foreach ($Pk in $J.report.package) {
            foreach ($Cl in $Pk.class) {
                if ($Cl.name -eq $Nome -or $Cl.name.StartsWith($Nome + '$')) {
                    foreach ($Ct in $Cl.counter) {
                        if ($Ct.type -eq "INSTRUCTION") { $Mi += [int]$Ct.missed; $Ci += [int]$Ct.covered }
                        if ($Ct.type -eq "BRANCH") { $Mb += [int]$Ct.missed; $Cb += [int]$Ct.covered }
                    }
                    if ($Cl.name -eq $Nome) {
                        foreach ($Me in $Cl.method) {
                            $Cm = $Me.counter | Where-Object { $_.type -eq "METHOD" }
                            if ($Cm -and [int]$Cm.covered -gt 0 -and $Me.name -notmatch '[$<]|^component') { $Metodos[$Me.name] = 1 }
                        }
                    }
                }
            }
        }
        $PI = if (($Mi + $Ci) -gt 0) { "{0:N1}%" -f (100.0 * $Ci / ($Mi + $Ci)) } else { "n/d" }
        $PB = if (($Mb + $Cb) -gt 0) { "{0:N1}%" -f (100.0 * $Cb / ($Mb + $Cb)) } else { "sem ramos" }
        $Linhas += ("{0,-10} {1,-24} instrucoes {2,-8} ramos {3,-9} metodos cobertos: {4}" -f $A.Split('/')[0], $A.Split('/')[1], $PI, $PB, (($Metodos.Keys | Sort-Object) -join ", "))
    }
} else { $Linhas += "Relatorio do JaCoCo nao encontrado. Confira se o plugin foi aplicado (aplicar-plano-de-testes.ps1) e rode: .\gradlew.bat test jacocoTestReport" }
Write-Host "4/4 Compactando os relatorios HTML..." -ForegroundColor Cyan
$Zip = Join-Path $Raiz "docs\relatorio-testes.zip"
if (Test-Path $Zip) { Remove-Item $Zip }
$Pastas = @((Join-Path $Backend "build\reports\tests\test"), (Join-Path $Backend "build\reports\jacoco\test\html")) | Where-Object { Test-Path $_ }
if ($Pastas.Count -gt 0) { Compress-Archive -Path $Pastas -DestinationPath $Zip }
$Gi = Join-Path $Raiz ".gitignore"
foreach ($Reg in @("docs/relatorio-testes.zip", "docs/resultados-testes.txt")) { if (-not (Select-String -Path $Gi -Pattern ([regex]::Escape($Reg)) -Quiet)) { Add-Content $Gi "`n$Reg" } }
$Saida = @("", "===== COPIE ESTES VALORES PARA O DOCUMENTO (item 10) =====", "Testes executados: $Total", "Passaram: $Passaram", "Falharam: $($Falhas + $Erros)    Ignorados: $Ignorados")
if ($Falharam.Count -gt 0) { $Saida += "Testes que falharam:"; foreach ($F in $Falharam) { $Saida += "  - $F" } }
$Saida += ""; $Saida += "Cobertura por classe:"; $Saida += $Linhas
$Saida += ""; $Saida += "Relatorios HTML (para capturas de tela): $Backend\build\reports\tests\test\index.html  e  $Backend\build\reports\jacoco\test\html\index.html"
$Saida += "Arquivo ZIP para subir no OneDrive e gerar o link: $Zip"
$Saida | ForEach-Object { Write-Host $_ }
$Saida | Out-File (Join-Path $Raiz "docs\resultados-testes.txt") -Encoding utf8
