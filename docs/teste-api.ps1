$api = "http://localhost:8080"
$s = Get-Random
function Qtd($r) { if ($null -eq $r) { 0 } else { @($r).Count } }
function Reg($n) { Invoke-RestMethod -Uri "$api/signup" -Method Post -ContentType "application/json" -Body (@{ name = $n; email = "$n$s@teste.com"; password = "Senha@123"; confirmPassword = "Senha@123" } | ConvertTo-Json) }
function Codigo($sb) { try { & $sb | Out-Null; 200 } catch { [int]$_.Exception.Response.StatusCode } }
function Check($nome, $ok) { if ($ok) { Write-Host "PASS  $nome" -ForegroundColor Green } else { Write-Host "FAIL  $nome" -ForegroundColor Red } }

$a = Reg "ana"; $b = Reg "beto"
$ha = @{ Authorization = "Bearer $($a.token)" }; $hb = @{ Authorization = "Bearer $($b.token)" }
$planta = '{"name":"Alface","species":"Lactuca sativa","plantingDate":"2026-09-15","minHumidity":60,"maxHumidity":80,"active":true}'
$area = '{"name":"Estufa 1","description":"Teste","capacityM2":12.5,"irrigationAutomatic":true}'

Check "signin retorna token" ((Invoke-RestMethod -Uri "$api/signin" -Method Post -ContentType "application/json" -Body (@{ email = "ana$s@teste.com"; password = "Senha@123" } | ConvertTo-Json)).token.Length -gt 20)
Check "sem token = 401/403" ((Codigo { Invoke-RestMethod -Uri "$api/plants" }) -in 401,403)

$p = Invoke-RestMethod -Uri "$api/plants" -Method Post -ContentType "application/json" -Headers $ha -Body $planta
Check "Ana cria planta" ($p.id -gt 0)
$r = Invoke-RestMethod -Uri "$api/plants" -Headers $ha
Check "Ana lista 1 planta (veio: $(Qtd $r))" ((Qtd $r) -eq 1)
$r = Invoke-RestMethod -Uri "$api/plants" -Headers $hb
Check "Beto lista 0 plantas (veio: $(Qtd $r))" ((Qtd $r) -eq 0)
Check "Beto GET planta da Ana = 404" ((Codigo { Invoke-RestMethod -Uri "$api/plants/$($p.id)" -Headers $hb }) -eq 404)
Check "Beto DELETE planta da Ana = 404" ((Codigo { Invoke-RestMethod -Uri "$api/plants/$($p.id)" -Method Delete -Headers $hb }) -eq 404)
$r = Invoke-RestMethod -Uri "$api/plants/search?name=alf" -Headers $ha
Check "Ana busca alf - JPQL LIKE (veio: $(Qtd $r))" ((Qtd $r) -eq 1)
$r = Invoke-RestMethod -Uri "$api/plants/search?name=alf" -Headers $hb
Check "Beto busca alf = 0 (veio: $(Qtd $r))" ((Qtd $r) -eq 0)
Check "corpo invalido = 400" ((Codigo { Invoke-RestMethod -Uri "$api/plants" -Method Post -ContentType "application/json" -Headers $ha -Body '{"name":"","species":"x","plantingDate":null,"minHumidity":-1,"maxHumidity":5,"active":true}' }) -eq 400)
Check "Ana atualiza (PUT)" ((Invoke-RestMethod -Uri "$api/plants/$($p.id)" -Method Put -ContentType "application/json" -Headers $ha -Body $planta.Replace("Alface","Alface Roxa")).name -eq "Alface Roxa")
Check "Ana apaga (DELETE)" ((Codigo { Invoke-RestMethod -Uri "$api/plants/$($p.id)" -Method Delete -Headers $ha }) -eq 200)

$ar = Invoke-RestMethod -Uri "$api/growing-areas" -Method Post -ContentType "application/json" -Headers $ha -Body $area
Check "Ana cria area" ($ar.id -gt 0)
$r = Invoke-RestMethod -Uri "$api/growing-areas" -Headers $hb
Check "Beto lista 0 areas (veio: $(Qtd $r))" ((Qtd $r) -eq 0)
Check "Beto GET area da Ana = 404" ((Codigo { Invoke-RestMethod -Uri "$api/growing-areas/$($ar.id)" -Headers $hb }) -eq 404)
$r = Invoke-RestMethod -Uri "$api/growing-areas/search?name=estu" -Headers $ha
Check "Ana busca area estu - JPQL LIKE (veio: $(Qtd $r))" ((Qtd $r) -eq 1)