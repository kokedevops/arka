# 🚀 Script para iniciar todos los microservicios con bootRun
# Versión optimizada para Windows PowerShell

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "🚀 ARKA MICROSERVICES - BOOTRUN DEPLOYMENT" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# Función para verificar si un puerto está en uso
function Test-Port {
    param([int]$Port)
    try {
        $connection = New-Object System.Net.Sockets.TcpClient
        $connection.Connect("localhost", $Port)
        $connection.Close()
        return $true
    } catch {
        return $false
    }
}

# Función para esperar que un servicio esté disponible
function Wait-ForService {
    param(
        [string]$ServiceName,
        [int]$Port,
        [int]$TimeoutSeconds = 60
    )
    
    Write-Host "⏳ Esperando que $ServiceName esté disponible en puerto $Port..." -ForegroundColor Yellow
    
    $elapsed = 0
    while ($elapsed -lt $TimeoutSeconds) {
        if (Test-Port -Port $Port) {
            Write-Host "✅ $ServiceName está disponible!" -ForegroundColor Green
            return $true
        }
        Start-Sleep -Seconds 2
        $elapsed += 2
        Write-Host "." -NoNewline -ForegroundColor Gray
    }
    
    Write-Host ""
    Write-Host "❌ Timeout esperando $ServiceName" -ForegroundColor Red
    return $false
}

# Verificar si Gradle está disponible
Write-Host "🔍 Verificando Gradle..." -ForegroundColor Blue
try {
    $gradleVersion = & .\gradlew.bat --version 2>$null
    Write-Host "✅ Gradle detectado" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: Gradle no encontrado. Asegúrate de que gradlew.bat esté disponible." -ForegroundColor Red
    exit 1
}

# Verificar si hay servicios ya ejecutándose
Write-Host ""
Write-Host "🔍 Verificando puertos disponibles..." -ForegroundColor Blue

$ports = @(8888, 8761, 8080, 8081, 8082, 8083)
$usedPorts = @()

foreach ($port in $ports) {
    if (Test-Port -Port $port) {
        $usedPorts += $port
    }
}

if ($usedPorts.Count -gt 0) {
    Write-Host "⚠️ Los siguientes puertos están en uso: $($usedPorts -join ', ')" -ForegroundColor Yellow
    $response = Read-Host "¿Deseas continuar de todos modos? (y/N)"
    if ($response -notmatch "^[Yy]") {
        Write-Host "❌ Deployment cancelado por el usuario" -ForegroundColor Red
        exit 0
    }
}

Write-Host ""
Write-Host "🚀 Iniciando microservicios en orden..." -ForegroundColor Green
Write-Host ""

# 1️⃣ CONFIG SERVER
Write-Host "1️⃣ Iniciando Config Server (Puerto: 8888)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "cd '$PWD'; Write-Host '🔧 CONFIG SERVER - Puerto 8888' -ForegroundColor Cyan; .\gradlew.bat :config-server:bootRun"
) -WindowStyle Normal

# Esperar a que Config Server esté disponible
if (-not (Wait-ForService -ServiceName "Config Server" -Port 8888 -TimeoutSeconds 60)) {
    Write-Host "❌ No se pudo iniciar Config Server. Abortando..." -ForegroundColor Red
    exit 1
}

Write-Host ""

# 2️⃣ EUREKA SERVER
Write-Host "2️⃣ Iniciando Eureka Server (Puerto: 8761)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "cd '$PWD'; Write-Host '🔍 EUREKA SERVER - Puerto 8761' -ForegroundColor Cyan; .\gradlew.bat :eureka-server:bootRun"
) -WindowStyle Normal

# Esperar a que Eureka esté disponible
if (-not (Wait-ForService -ServiceName "Eureka Server" -Port 8761 -TimeoutSeconds 60)) {
    Write-Host "❌ No se pudo iniciar Eureka Server. Abortando..." -ForegroundColor Red
    exit 1
}

Write-Host ""

# 3️⃣ API GATEWAY
Write-Host "3️⃣ Iniciando API Gateway (Puerto: 8080)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "cd '$PWD'; Write-Host '🌐 API GATEWAY - Puerto 8080' -ForegroundColor Cyan; .\gradlew.bat :api-gateway:bootRun"
) -WindowStyle Normal

# Esperar a que API Gateway esté disponible
if (-not (Wait-ForService -ServiceName "API Gateway" -Port 8080 -TimeoutSeconds 60)) {
    Write-Host "❌ No se pudo iniciar API Gateway. Abortando..." -ForegroundColor Red
    exit 1
}

Write-Host ""

# 4️⃣ HELLO WORLD SERVICE
Write-Host "4️⃣ Iniciando Hello World Service (Puerto: 8081)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "cd '$PWD'; Write-Host '👋 HELLO WORLD SERVICE - Puerto 8081' -ForegroundColor Cyan; .\gradlew.bat :hello-world-service:bootRun"
) -WindowStyle Normal

Start-Sleep -Seconds 5

# 5️⃣ ARCA COTIZADOR
Write-Host "5️⃣ Iniciando Arca Cotizador (Puerto: 8082)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "cd '$PWD'; Write-Host '💰 ARCA COTIZADOR - Puerto 8082' -ForegroundColor Cyan; .\gradlew.bat :arca-cotizador:bootRun"
) -WindowStyle Normal

Start-Sleep -Seconds 5

# 6️⃣ ARCA GESTOR SOLICITUDES
Write-Host "6️⃣ Iniciando Arca Gestor Solicitudes (Puerto: 8083)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "cd '$PWD'; Write-Host '📋 ARCA GESTOR SOLICITUDES - Puerto 8083' -ForegroundColor Cyan; .\gradlew.bat :arca-gestor-solicitudes:bootRun"
) -WindowStyle Normal

Write-Host ""
Write-Host "⏳ Esperando que todos los servicios estén completamente iniciados..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

Write-Host ""
Write-Host "===============================================" -ForegroundColor Green
Write-Host "✅ TODOS LOS SERVICIOS INICIADOS!" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green
Write-Host ""

Write-Host "🌐 URLs de acceso:" -ForegroundColor Cyan
Write-Host "   📊 Eureka Dashboard: http://localhost:8761" -ForegroundColor White
Write-Host "   🔧 Config Server: http://localhost:8888" -ForegroundColor White
Write-Host "   🌐 API Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "   👋 Hello World: http://localhost:8081/hello" -ForegroundColor White
Write-Host "   💰 Cotizador: http://localhost:8082/cotizaciones" -ForegroundColor White
Write-Host "   📋 Gestor: http://localhost:8083/solicitudes" -ForegroundColor White
Write-Host ""

Write-Host "🔍 Health Checks:" -ForegroundColor Cyan
Write-Host "   Config Server: http://localhost:8888/actuator/health" -ForegroundColor Gray
Write-Host "   Eureka Server: http://localhost:8761/actuator/health" -ForegroundColor Gray
Write-Host "   API Gateway: http://localhost:8080/actuator/health" -ForegroundColor Gray
Write-Host "   Hello World: http://localhost:8081/actuator/health" -ForegroundColor Gray
Write-Host "   Cotizador: http://localhost:8082/actuator/health" -ForegroundColor Gray
Write-Host "   Gestor: http://localhost:8083/actuator/health" -ForegroundColor Gray
Write-Host ""

Write-Host "📱 Comandos útiles:" -ForegroundColor Cyan
Write-Host "   Ver servicios registrados: Invoke-RestMethod http://localhost:8761/eureka/apps" -ForegroundColor Gray
Write-Host "   Health check completo: .\check-services-health.ps1" -ForegroundColor Gray
Write-Host "   Detener todos: .\stop-all-services.ps1" -ForegroundColor Gray
Write-Host ""

Write-Host "💡 Tip: Cada servicio se ejecuta en su propia ventana de PowerShell" -ForegroundColor Yellow
Write-Host "      Puedes cerrar esta ventana, los servicios seguirán ejecutándose" -ForegroundColor Yellow
Write-Host ""

# Verificación final de servicios
Write-Host "🔍 Verificación final de servicios..." -ForegroundColor Blue
Start-Sleep -Seconds 5

$services = @(
    @{Name="Config Server"; Port=8888},
    @{Name="Eureka Server"; Port=8761},
    @{Name="API Gateway"; Port=8080},
    @{Name="Hello World"; Port=8081},
    @{Name="Cotizador"; Port=8082},
    @{Name="Gestor"; Port=8083}
)

$allUp = $true
foreach ($service in $services) {
    if (Test-Port -Port $service.Port) {
        Write-Host "✅ $($service.Name) - Puerto $($service.Port)" -ForegroundColor Green
    } else {
        Write-Host "❌ $($service.Name) - Puerto $($service.Port)" -ForegroundColor Red
        $allUp = $false
    }
}

Write-Host ""
if ($allUp) {
    Write-Host "🎉 ¡DEPLOYMENT COMPLETADO EXITOSAMENTE!" -ForegroundColor Green
    Write-Host "🚀 ¡Tu ecosistema de microservicios está listo para usar!" -ForegroundColor Green
} else {
    Write-Host "⚠️ Algunos servicios pueden estar iniciándose aún. Espera unos minutos más." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Presiona cualquier tecla para salir..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")