# 🚀 Script para iniciar todos los servicios ARKA en modo LOCAL
# Autor: ARKA Development Team
# Descripción: Inicia todos los microservicios con perfil local

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🏠 ARKA - Inicio de Servicios LOCAL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Función para iniciar un servicio
function Start-Service {
    param(
        [string]$ServiceName,
        [string]$GradleTask,
        [string]$Description,
        [int]$WaitSeconds = 0
    )
    
    Write-Host "🚀 Iniciando $Description..." -ForegroundColor Green
    Write-Host "   Comando: gradle $GradleTask --args='--spring.profiles.active=local'" -ForegroundColor Gray
    
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\..'; gradle $GradleTask --args='--spring.profiles.active=local'"
    
    if ($WaitSeconds -gt 0) {
        Write-Host "⏳ Esperando $WaitSeconds segundos..." -ForegroundColor Yellow
        Start-Sleep -Seconds $WaitSeconds
    }
    
    Write-Host "✅ $Description iniciado" -ForegroundColor Green
    Write-Host ""
}

# Verificar que estamos en el directorio correcto
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

Write-Host "📂 Directorio del proyecto: $projectRoot" -ForegroundColor Cyan
Write-Host ""

# 1️⃣ Eureka Server (PRIMERO)
Write-Host "=" * 50 -ForegroundColor Cyan
Write-Host "1️⃣  INICIANDO EUREKA SERVER" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan
Start-Service -ServiceName "eureka-server" `
              -GradleTask ":eureka-server:bootRun" `
              -Description "Eureka Server (Discovery)" `
              -WaitSeconds 30

# 2️⃣ Config Server (SEGUNDO)
Write-Host "=" * 50 -ForegroundColor Cyan
Write-Host "2️⃣  INICIANDO CONFIG SERVER" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan
Write-Host "🚀 Iniciando Config Server (Configuración)..." -ForegroundColor Green
Write-Host "   Comando: gradle :config-server:bootRun --args='--spring.profiles.active=local,native'" -ForegroundColor Gray

Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\..'; gradle :config-server:bootRun --args='--spring.profiles.active=local,native'"

Write-Host "⏳ Esperando 20 segundos..." -ForegroundColor Yellow
Start-Sleep -Seconds 20
Write-Host "✅ Config Server (Configuración) iniciado" -ForegroundColor Green
Write-Host ""

# 3️⃣ API Gateway (TERCERO)
Write-Host "=" * 50 -ForegroundColor Cyan
Write-Host "3️⃣  INICIANDO API GATEWAY" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan
Start-Service -ServiceName "api-gateway" `
              -GradleTask ":api-gateway:bootRun" `
              -Description "API Gateway (Puerta de entrada)" `
              -WaitSeconds 20

# 4️⃣ Microservicios
Write-Host "=" * 50 -ForegroundColor Cyan
Write-Host "4️⃣  INICIANDO MICROSERVICIOS" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan

Start-Service -ServiceName "arca-cotizador" `
              -GradleTask ":arca-cotizador:bootRun" `
              -Description "Arca Cotizador (Puerto 8081)" `
              -WaitSeconds 5

Start-Service -ServiceName "arca-gestor-solicitudes" `
              -GradleTask ":arca-gestor-solicitudes:bootRun" `
              -Description "Arca Gestor Solicitudes (Puerto 8082)" `
              -WaitSeconds 5

Start-Service -ServiceName "hello-world-service" `
              -GradleTask ":hello-world-service:bootRun" `
              -Description "Hello World Service (Puerto 8083)" `
              -WaitSeconds 5

# 5️⃣ Aplicación Principal (ÚLTIMO)
Write-Host "=" * 50 -ForegroundColor Cyan
Write-Host "5️⃣  INICIANDO APLICACIÓN PRINCIPAL" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan
Start-Service -ServiceName "main-app" `
              -GradleTask "bootRun" `
              -Description "Aplicación Principal (Puerto 8090)" `
              -WaitSeconds 10

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "✅ TODOS LOS SERVICIOS INICIADOS" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "🔗 URLs de acceso:" -ForegroundColor Cyan
Write-Host "   🔍 Eureka Dashboard: http://localhost:8761" -ForegroundColor White
Write-Host "   ⚙️  Config Server:    http://localhost:8888" -ForegroundColor White
Write-Host "   🚪 API Gateway:       http://localhost:8085" -ForegroundColor White
Write-Host "   💰 Arca Cotizador:    http://localhost:8081" -ForegroundColor White
Write-Host "   📝 Arca Gestor:       http://localhost:8082" -ForegroundColor White
Write-Host "   🌍 Hello World:       http://localhost:8083" -ForegroundColor White
Write-Host "   🏠 App Principal:     http://localhost:8090" -ForegroundColor White
Write-Host ""
Write-Host "📊 Base de Datos:" -ForegroundColor Cyan
Write-Host "   MySQL: 192.168.100.77:3306" -ForegroundColor White
Write-Host "   Base de datos: arkavalenzuelabd" -ForegroundColor White
Write-Host ""
Write-Host "⚠️  Nota: Espera 2-3 minutos para que todos los servicios se registren en Eureka" -ForegroundColor Yellow
Write-Host ""
Write-Host "Presiona cualquier tecla para salir..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
