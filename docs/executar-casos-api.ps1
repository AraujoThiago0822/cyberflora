# Executa os casos de teste de sistema que acionam diretamente a API (regras do servidor, RN04 e RN05).
# Com o backend rodando:  powershell -ExecutionPolicy Bypass -File docs\executar-casos-api.ps1
param([string]$Api = "http://localhost:8080")
$J = "application/json"; $S = Get-Random
$script:Ok = 0; $script:Falhas = 0
function Chamar($Metodo, $Uri, $Corpo, $Cab) {
    try {
        $R = Invoke-RestMethod -Uri $Uri -Method $Metodo -ContentType $J -Headers $Cab -Body $Corpo
        return @{ Codigo = 200; Corpo = $R; Erro = "" }
    } catch {
        $Cod = [int]$_.Exception.Response.StatusCode; $Msg = ""
        try { $Msg = ($_.ErrorDetails.Message | ConvertFrom-Json).error } catch { }
        return @{ Codigo = $Cod; Corpo = $null; Erro = $Msg }
    }
}
function Check($Caso, $Nome, $Cond, $Detalhe) {
    if ($Cond) { Write-Host ("PASS  {0}  {1}" -f $Caso, $Nome) -ForegroundColor Green; $script:Ok++ }
    else { Write-Host ("FAIL  {0}  {1}  -> {2}" -f $Caso, $Nome, $Detalhe) -ForegroundColor Red; $script:Falhas++ }
}
function Planta($Nome, $Esp, $Min, $Max) { @{ name = $Nome; species = $Esp; plantingDate = "2026-09-15"; minHumidity = $Min; maxHumidity = $Max; active = $true } | ConvertTo-Json }
function Qtd($R) { if ($null -eq $R) { 0 } else { @($R).Count } }
function Reg($N) { Invoke-RestMethod -Uri "$Api/signup" -Method Post -ContentType $J -Body (@{ name = $N; email = "$N$S@teste.com"; password = "Senha@123"; confirmPassword = "Senha@123" } | ConvertTo-Json) }
$Ana = Reg "ana"; $Beto = Reg "beto"
$Ha = @{ Authorization = "Bearer $($Ana.token)" }; $Hb = @{ Authorization = "Bearer $($Beto.token)" }
$Url = "$Api/plants"
Write-Host "Casos executados pela API (numeracao da planilha de teste)" -ForegroundColor Cyan

$R = Chamar "Post" $Url (Planta "Alface" "Lactuca sativa" 60 80) $Ha
Check "#01" "cadastro valido (60 e 80) e gravado" (($R.Codigo -eq 200) -and ($R.Corpo.id -gt 0)) "codigo $($R.Codigo) $($R.Erro)"
$IdAna = $R.Corpo.id
$R = Chamar "Post" $Url (Planta "Alface" "Lactuca sativa" 0 80) $Ha
Check "#15" "umidade minima = 0 e rejeitada (400)" ($R.Codigo -eq 400) "codigo $($R.Codigo)"
$R = Chamar "Post" $Url (Planta "Alface" "Lactuca sativa" 1 80) $Ha
Check "#16" "umidade minima = 1 e aceita" ($R.Codigo -eq 200) "codigo $($R.Codigo) $($R.Erro)"
$R = Chamar "Post" $Url (Planta "Alface" "Lactuca sativa" 60 100) $Ha
Check "#17" "umidade maxima = 100 e aceita" ($R.Codigo -eq 200) "codigo $($R.Codigo) $($R.Erro)"
$R = Chamar "Post" $Url (Planta "Alface" "Lactuca sativa" 60 101) $Ha
Check "#18" "umidade maxima = 101 e rejeitada (400, superior a 100%)" (($R.Codigo -eq 400) -and ($R.Erro -like "*superior a 100%*")) "codigo $($R.Codigo) $($R.Erro)"
$R = Chamar "Post" $Url (Planta "Alface" "Lactuca sativa" 60 60) $Ha
Check "#19" "minima = maxima e rejeitada (400)" (($R.Codigo -eq 400) -and ($R.Erro -like "*menor que a m*")) "codigo $($R.Codigo) $($R.Erro)"
$R = Chamar "Post" $Url (Planta "Alface" "Lactuca sativa" 79 80) $Ha
Check "#20" "minima = maxima - 1 e aceita" ($R.Codigo -eq 200) "codigo $($R.Codigo) $($R.Erro)"
$R = Chamar "Post" $Url (Planta ("a" * 100) "Lactuca sativa" 60 80) $Ha
Check "#21" "nome com 100 caracteres e aceito" ($R.Codigo -eq 200) "codigo $($R.Codigo) $($R.Erro)"
$R = Chamar "Post" $Url (Planta ("a" * 101) "Lactuca sativa" 60 80) $Ha
Check "#22" "nome com 101 caracteres e rejeitado pelo servidor (400)" (($R.Codigo -eq 400) -and ($R.Erro -like "*100*")) "codigo $($R.Codigo) $($R.Erro)"
$R = Chamar "Post" $Url (Planta "Alface" ("b" * 150) 60 80) $Ha
Check "#23" "especie com 150 caracteres e aceita" ($R.Codigo -eq 200) "codigo $($R.Codigo) $($R.Erro)"
$R = Chamar "Post" $Url (Planta "Alface" ("b" * 151) 60 80) $Ha
Check "#24" "especie com 151 caracteres e rejeitada pelo servidor (400)" (($R.Codigo -eq 400) -and ($R.Erro -like "*150*")) "codigo $($R.Codigo) $($R.Erro)"
$R = Chamar "Post" $Url (Planta "Alface" "Lactuca sativa" 90 70) $Ha
Check "#31" "requisicao direta com minima 90 e maxima 70 = 400 (regra do servidor)" (($R.Codigo -eq 400) -and ($R.Erro -like "*menor que a m*")) "codigo $($R.Codigo) $($R.Erro)"
$ListaBeto = Invoke-RestMethod -Uri $Url -Headers $Hb
$R = Chamar "Get" "$Url/$IdAna" $null $Hb
Check "#32" "RN04: outro usuario nao ve a planta (lista vazia e GET por id = 404)" (((Qtd $ListaBeto) -eq 0) -and ($R.Codigo -eq 404)) "lista $(Qtd $ListaBeto), codigo $($R.Codigo)"
$R = Chamar "Post" $Url (Planta "Alface" "Lactuca sativa" 60 80) $null
Check "#33" "RN05: cadastro sem token e recusado (401 ou 403)" (($R.Codigo -eq 401) -or ($R.Codigo -eq 403)) "codigo $($R.Codigo)"
$R = Chamar "Post" $Url (Planta "Alface" "Lactuca sativa" 60 80) @{ Authorization = "Bearer token.invalido.abc" }
Check "#29 (API)" "RN05: token invalido e recusado (401 ou 403)" (($R.Codigo -eq 401) -or ($R.Codigo -eq 403)) "codigo $($R.Codigo)"
Write-Host ""
if ($script:Falhas -eq 0) { Write-Host ("Todos os {0} casos passaram." -f $script:Ok) -ForegroundColor Green } else { Write-Host ("{0} passaram e {1} falharam." -f $script:Ok, $script:Falhas) -ForegroundColor Red }
