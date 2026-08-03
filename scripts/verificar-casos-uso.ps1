$ErrorActionPreference = "Continue"
$base = "http://localhost:8080/api"
$tmp = Join-Path $env:TEMP "banco-api-test"
New-Item -ItemType Directory -Force -Path $tmp | Out-Null
$pass = 0
$fail = 0

function Ok($msg) { Write-Host "  OK  $msg" -ForegroundColor Green; $script:pass++ }
function Fail($msg) { Write-Host "  FAIL $msg" -ForegroundColor Red; $script:fail++ }
function Step($msg) { Write-Host "`n==> $msg" -ForegroundColor Cyan }

function Write-JsonFile($name, $obj) {
    $path = Join-Path $tmp $name
    $json = $obj | ConvertTo-Json -Depth 8 -Compress
    $utf8 = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($path, $json, $utf8)
    return $path
}

function Api {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body = $null
    )
    $url = "$base$Path"
    $outFile = Join-Path $tmp "resp.json"
    $codeFile = Join-Path $tmp "code.txt"
    if ($null -ne $Body) {
        $bodyFile = Write-JsonFile "body.json" $Body
        $args = @("-s", "-o", $outFile, "-w", "%{http_code}", "-X", $Method, $url, "-H", "Content-Type: application/json", "--data-binary", "@$bodyFile")
    } else {
        $args = @("-s", "-o", $outFile, "-w", "%{http_code}", "-X", $Method, $url)
    }
    $code = & curl.exe @args
    $raw = if (Test-Path $outFile) { Get-Content $outFile -Raw -ErrorAction SilentlyContinue } else { "" }
    $json = $null
    if ($raw -and $raw.Trim().Length -gt 0) {
        try { $json = $raw | ConvertFrom-Json } catch { $json = $null }
    }
    return @{ Status = [int]$code; Body = $json; Raw = $raw }
}

Step "0) Vaciar base de datos"
docker exec trabajo_prueba-db-1 psql -U banco -d banco -c "TRUNCATE TABLE movimientos, cuentas, clientes RESTART IDENTITY CASCADE;" | Out-Null
Start-Sleep -Seconds 1
$check = Api GET "/clientes"
$count = if ($check.Body) { @($check.Body).Count } else { 0 }
if ($check.Status -eq 200 -and $count -eq 0) { Ok "BD vacia (0 clientes)" } else { Fail "BD no vacia status=$($check.Status) count=$count raw=$($check.Raw)" }

Step "1) Creacion de Usuarios (POST /clientes)"
$clientesData = @(
    @{ nombre="Jose Lema"; genero="Masculino"; edad=30; identificacion="1001001001"; direccion="Otavalo sn y principal"; telefono="098254785"; contrasena="1234"; estado=$true },
    @{ nombre="Marianela Montalvo"; genero="Femenino"; edad=28; identificacion="1001001002"; direccion="Amazonas y NNUU"; telefono="097548965"; contrasena="5678"; estado=$true },
    @{ nombre="Juan Osorio"; genero="Masculino"; edad=32; identificacion="1001001003"; direccion="13 junio y Equinoccial"; telefono="098874587"; contrasena="1245"; estado=$true }
)
$ids = @{}
foreach ($c in $clientesData) {
    $r = Api POST "/clientes" $c
    if ($r.Status -eq 201) {
        $ids[$c.nombre] = [int]$r.Body.clienteId
        Ok "Cliente $($c.nombre) id=$($r.Body.clienteId)"
    } else { Fail "Cliente $($c.nombre): status=$($r.Status) $($r.Raw)" }
}

Step "2) Creacion de Cuentas (POST /cuentas)"
$cuentasData = @(
    @{ numeroCuenta="478758"; tipoCuenta="Ahorro"; saldoInicial=2000; estado=$true; cliente="Jose Lema" },
    @{ numeroCuenta="225487"; tipoCuenta="Corriente"; saldoInicial=100; estado=$true; cliente="Marianela Montalvo" },
    @{ numeroCuenta="495878"; tipoCuenta="Ahorros"; saldoInicial=0; estado=$true; cliente="Juan Osorio" },
    @{ numeroCuenta="496825"; tipoCuenta="Ahorros"; saldoInicial=540; estado=$true; cliente="Marianela Montalvo" }
)
$cuentaIds = @{}
foreach ($cu in $cuentasData) {
    $body = @{
        numeroCuenta = $cu.numeroCuenta
        tipoCuenta   = $cu.tipoCuenta
        saldoInicial = $cu.saldoInicial
        estado       = $true
        clienteId    = $ids[$cu.cliente]
    }
    $r = Api POST "/cuentas" $body
    if ($r.Status -eq 201 -and [decimal]$r.Body.saldoActual -eq $cu.saldoInicial) {
        $cuentaIds[$cu.numeroCuenta] = [int]$r.Body.id
        Ok "Cuenta $($cu.numeroCuenta) id=$($r.Body.id) saldo=$($r.Body.saldoActual)"
    } else { Fail "Cuenta $($cu.numeroCuenta): status=$($r.Status) $($r.Raw)" }
}

Step "3) Nueva Cuenta Corriente Jose Lema"
$r = Api POST "/cuentas" @{
    numeroCuenta="585545"; tipoCuenta="Corriente"; saldoInicial=1000; estado=$true; clienteId=$ids["Jose Lema"]
}
if ($r.Status -eq 201) {
    $cuentaIds["585545"] = [int]$r.Body.id
    Ok "Cuenta 585545 creada saldo=$($r.Body.saldoActual)"
} else { Fail "Cuenta 585545: $($r.Raw)" }

Step "4) Movimientos del caso de uso"
$movimientos = @(
    @{ cuenta="478758"; tipo="DEBITO"; valor=575; fecha="2022-02-08T10:00:00"; saldoEsperado=1425 },
    @{ cuenta="225487"; tipo="CREDITO"; valor=600; fecha="2022-02-10T10:00:00"; saldoEsperado=700 },
    @{ cuenta="495878"; tipo="CREDITO"; valor=150; fecha="2022-02-09T10:00:00"; saldoEsperado=150 },
    @{ cuenta="496825"; tipo="DEBITO"; valor=540; fecha="2022-02-08T11:00:00"; saldoEsperado=0 }
)
foreach ($m in $movimientos) {
    $r = Api POST "/movimientos" @{
        tipoMovimiento = $m.tipo
        valor          = $m.valor
        cuentaId       = $cuentaIds[$m.cuenta]
        fecha          = $m.fecha
    }
    if ($r.Status -eq 201 -and [decimal]$r.Body.saldo -eq $m.saldoEsperado) {
        $signoOk = ($m.tipo -eq "CREDITO" -and [decimal]$r.Body.valor -gt 0) -or ($m.tipo -eq "DEBITO" -and [decimal]$r.Body.valor -lt 0)
        if ($signoOk) { Ok "$($m.tipo) $($m.valor) en $($m.cuenta) -> saldo $($r.Body.saldo) valor=$($r.Body.valor)" }
        else { Fail "Signo incorrecto en $($m.cuenta): valor=$($r.Body.valor)" }
    } else { Fail "Movimiento $($m.cuenta): status=$($r.Status) $($r.Raw)" }
}

Step "5) Reporte Marianela"
$r = Api GET "/reportes?clienteId=$($ids['Marianela Montalvo'])&fechaInicio=2022-02-01&fechaFin=2022-02-28"
if ($r.Status -eq 200) {
    $movs = @($r.Body.movimientos)
    $json225 = $movs | Where-Object { $_.'Numero Cuenta' -eq "225487" } | Select-Object -First 1
    if ($json225 -and [decimal]$json225.Movimiento -eq 600 -and [decimal]$json225.'Saldo Disponible' -eq 700) {
        Ok "JSON ejemplo 225487: Movimiento=600 SaldoDisponible=700"
    } else { Fail "JSON ejemplo 225487 no coincide: $($json225 | ConvertTo-Json -Compress)" }
    if ($r.Body.pdfBase64 -and $r.Body.pdfBase64.Length -gt 100) { Ok "PDF base64 len=$($r.Body.pdfBase64.Length)" }
    else { Fail "PDF base64 ausente" }
    Ok "Totales creditos=$($r.Body.totalCreditos) debitos=$($r.Body.totalDebitos) items=$($movs.Count)"
} else { Fail "Reporte: $($r.Raw)" }

Step "6) Reglas de negocio"
$r = Api POST "/movimientos" @{ tipoMovimiento="DEBITO"; valor=10; cuentaId=$cuentaIds["496825"] }
if ($r.Status -eq 400 -and $r.Body.message -eq "Saldo no disponible") { Ok "Saldo no disponible" }
else { Fail "Esperado Saldo no disponible: $($r.Status) $($r.Raw)" }

$cu = Api POST "/cuentas" @{
    numeroCuenta="777001"; tipoCuenta="Ahorros"; saldoInicial=5000; estado=$true; clienteId=$ids["Jose Lema"]
}
$r = Api POST "/movimientos" @{ tipoMovimiento="DEBITO"; valor=1001; cuentaId=[int]$cu.Body.id }
if ($r.Status -eq 400 -and $r.Body.message -eq "Cupo diario Excedido") { Ok "Cupo diario Excedido" }
else { Fail "Esperado Cupo diario Excedido: $($r.Status) $($r.Raw)" }

Step "7) GET / PUT / PATCH / DELETE"
$r = Api GET "/clientes/$($ids['Jose Lema'])"
if ($r.Status -eq 200 -and $r.Body.nombre -eq "Jose Lema") { Ok "GET cliente by id" } else { Fail "GET cliente $($r.Raw)" }

$r = Api GET "/cuentas/$($cuentaIds['585545'])"
if ($r.Status -eq 200 -and $r.Body.numeroCuenta -eq "585545") { Ok "GET cuenta by id" } else { Fail "GET cuenta $($r.Raw)" }

$r = Api PUT "/clientes/$($ids['Jose Lema'])" @{
    nombre="Jose Lema"; genero="Masculino"; edad=31; identificacion="1001001001"
    direccion="Otavalo sn y principal"; telefono="098254785"; contrasena="1234"; estado=$true
}
if ($r.Status -eq 200 -and $r.Body.edad -eq 31) { Ok "PUT cliente edad=31" } else { Fail "PUT cliente $($r.Raw)" }

$r = Api PATCH "/clientes/$($ids['Jose Lema'])" @{ telefono="0999999999" }
if ($r.Status -eq 200 -and $r.Body.telefono -eq "0999999999") { Ok "PATCH cliente telefono" } else { Fail "PATCH cliente $($r.Raw)" }

$r = Api PUT "/cuentas/$($cuentaIds['585545'])" @{
    numeroCuenta="585545"; tipoCuenta="Corriente"; saldoInicial=1000; estado=$true; clienteId=$ids["Jose Lema"]
}
if ($r.Status -eq 200) { Ok "PUT cuenta 585545" } else { Fail "PUT cuenta $($r.Raw)" }

$r = Api PATCH "/cuentas/$($cuentaIds['585545'])" @{ estado=$false }
if ($r.Status -eq 200 -and $r.Body.estado -eq $false) { Ok "PATCH cuenta estado=false" } else { Fail "PATCH cuenta $($r.Raw)" }
Api PATCH "/cuentas/$($cuentaIds['585545'])" @{ estado=$true } | Out-Null

$r = Api GET "/clientes?buscar=Marianela"
if ($r.Status -eq 200 -and @($r.Body).Count -ge 1) { Ok "buscar clientes Marianela" } else { Fail "buscar clientes $($r.Raw)" }

$r = Api GET "/cuentas?buscar=478758"
if ($r.Status -eq 200 -and @($r.Body).Count -ge 1) { Ok "buscar cuentas 478758" } else { Fail "buscar cuentas $($r.Raw)" }

$movs = Api GET "/movimientos"
$movId = @($movs.Body | Select-Object -First 1).id
$r = Api PATCH "/movimientos/$movId" @{ fecha="2022-02-11T12:00:00" }
if ($r.Status -eq 200) { Ok "PATCH movimiento id=$movId" } else { Fail "PATCH movimiento $($r.Raw)" }

$tmpCu = Api POST "/cuentas" @{
    numeroCuenta="777002"; tipoCuenta="Ahorros"; saldoInicial=200; estado=$true; clienteId=$ids["Juan Osorio"]
}
$tmpMov = Api POST "/movimientos" @{ tipoMovimiento="CREDITO"; valor=50; cuentaId=[int]$tmpCu.Body.id }
$r = Api DELETE "/movimientos/$($tmpMov.Body.id)"
$after = Api GET "/cuentas/$($tmpCu.Body.id)"
if ($r.Status -eq 204 -and [decimal]$after.Body.saldoActual -eq 200) { Ok "DELETE movimiento revierte saldo a 200" }
else { Fail "DELETE movimiento status=$($r.Status) saldo=$($after.Body.saldoActual)" }

$extra = Api POST "/clientes" @{
    nombre="Temp Delete"; genero="Otro"; edad=20; identificacion="1888888888"
    direccion="X"; telefono="0988888888"; contrasena="x"; estado=$true
}
$extraCu = Api POST "/cuentas" @{
    numeroCuenta="777003"; tipoCuenta="Ahorros"; saldoInicial=10; estado=$true; clienteId=[int]$extra.Body.clienteId
}
$r1 = Api DELETE "/cuentas/$($extraCu.Body.id)"
$r2 = Api DELETE "/clientes/$($extra.Body.clienteId)"
if ($r1.Status -eq 204 -and $r2.Status -eq 204) { Ok "DELETE cuenta y cliente temporales" }
else { Fail "DELETE temp cuenta=$($r1.Status) cliente=$($r2.Status)" }

Step "8) Estado final"
$clientes = Api GET "/clientes"
$cuentas = Api GET "/cuentas"
$movimientos = Api GET "/movimientos"
Write-Host "  Clientes: $(@($clientes.Body).Count)"
Write-Host "  Cuentas: $(@($cuentas.Body).Count)"
Write-Host "  Movimientos: $(@($movimientos.Body).Count)"
@($cuentas.Body) | Sort-Object numeroCuenta | ForEach-Object {
    Write-Host ("  Cuenta {0}: saldoActual={1} (inicial={2}) cliente={3}" -f $_.numeroCuenta, $_.saldoActual, $_.saldoInicial, $_.clienteNombre)
}

Write-Host "`n=============================="
Write-Host "RESULTADO: $pass OK / $fail FAIL"
Write-Host "=============================="
if ($fail -gt 0) { exit 1 } else { exit 0 }
