# 🐳 Script de Despliegue Completo ARKA E-commerce Platform (Windows)
# ==================================================================

Write-Host "🚀 INICIANDO DESPLIEGUE COMPLETO DE ARKA E-commerce Platform" -ForegroundColor Green
Write-Host "==============================================================" -ForegroundColor Green

# Función para mostrar mensajes con colores
function Show-Status {
    param($Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Show-Success {
    param($Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Show-Warning {
    param($Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Show-Error {
    param($Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Verificar que Docker esté instalado y corriendo
function Check-Docker {
    Show-Status "Verificando Docker..."
    
    try {
        $dockerVersion = docker --version
        if (-not $dockerVersion) {
            Show-Error "Docker no está instalado. Por favor instala Docker Desktop."
            exit 1
        }
        
        $dockerInfo = docker info 2>$null
        if (-not $dockerInfo) {
            Show-Error "Docker no está corriendo. Por favor inicia Docker Desktop."
            exit 1
        }
        
        Show-Success "Docker está disponible y corriendo"
    }
    catch {
        Show-Error "Error verificando Docker: $_"
        exit 1
    }
}

# Verificar que Docker Compose esté disponible
function Check-DockerCompose {
    Show-Status "Verificando Docker Compose..."
    
    try {
        $composeVersion = docker-compose --version 2>$null
        if (-not $composeVersion) {
            $composeVersion = docker compose version 2>$null
        }
        
        if (-not $composeVersion) {
            Show-Error "Docker Compose no está disponible."
            exit 1
        }
        
        Show-Success "Docker Compose está disponible"
    }
    catch {
        Show-Error "Error verificando Docker Compose: $_"
        exit 1
    }
}

# Limpiar contenedores y volúmenes anteriores (opcional)
function Cleanup-Previous {
    Show-Warning "¿Deseas limpiar contenedores y volúmenes anteriores? (y/N)"
    $response = Read-Host
    
    if ($response -match "^[yY]") {
        Show-Status "Limpiando contenedores anteriores..."
        
        try {
            docker-compose down -v --remove-orphans 2>$null
        }
        catch {
            try {
                docker compose down -v --remove-orphans 2>$null
            }
            catch {
                # Ignorar errores si no hay contenedores
            }
        }
        
        Show-Status "Limpiando imágenes de ARKA..."
        try {
            $arkaImages = docker images --format "{{.Repository}}:{{.Tag}}" | Where-Object { $_ -match "arka" }
            if ($arkaImages) {
                $arkaImages | ForEach-Object { docker rmi $_ -f 2>$null }
            }
        }
        catch {
            # Ignorar errores
        }
        
        Show-Success "Limpieza completada"
    }
}

# Construir todas las imágenes
function Build-Images {
    Show-Status "Construyendo todas las imágenes Docker..."
    
    try {
        # Intentar con docker-compose primero
        $result = docker-compose build --no-cache 2>$null
        if ($LASTEXITCODE -ne 0) {
            # Si falla, intentar con docker compose
            $result = docker compose build --no-cache
        }
        
        if ($LASTEXITCODE -eq 0) {
            Show-Success "Todas las imágenes construidas exitosamente"
        }
        else {
            Show-Error "Error construyendo las imágenes"
            exit 1
        }
    }
    catch {
        Show-Error "Error durante la construcción: $_"
        exit 1
    }
}

# Iniciar todos los servicios
function Start-Services {
    Show-Status "Iniciando todos los servicios..."
    
    try {
        # Intentar con docker-compose primero
        $result = docker-compose up -d 2>$null
        if ($LASTEXITCODE -ne 0) {
            # Si falla, intentar con docker compose
            $result = docker compose up -d
        }
        
        if ($LASTEXITCODE -eq 0) {
            Show-Success "Todos los servicios iniciados"
        }
        else {
            Show-Error "Error iniciando los servicios"
            exit 1
        }
    }
    catch {
        Show-Error "Error durante el inicio: $_"
        exit 1
    }
}

# Monitorear el estado de los servicios
function Monitor-Services {
    Show-Status "Monitoreando el estado de los servicios..."
    
    Write-Host ""
    Write-Host "🔄 Esperando que todos los servicios estén listos..." -ForegroundColor Yellow
    Write-Host "   (Esto puede tomar 2-3 minutos)" -ForegroundColor Yellow
    Write-Host ""
    
    # Esperar un poco para que los servicios inicien
    Start-Sleep -Seconds 30
    
    # Mostrar estado de los servicios
    Show-Status "Estado actual de los servicios:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    Write-Host ""
    Show-Status "Verificando health checks..."
    
    # Verificar servicios principales
    $services = @("arka-eureka-server", "arka-main-app", "arka-api-gateway", "arka-gestor-solicitudes", "arka-cotizador")
    
    foreach ($service in $services) {
        Show-Status "Verificando $service..."
        
        # Esperar hasta 3 minutos para que el servicio esté healthy
        $maxAttempts = 18
        $healthy = $false
        
        for ($i = 1; $i -le $maxAttempts; $i++) {
            try {
                $status = docker inspect --format='{{.State.Health.Status}}' $service 2>$null
                
                if ($status -eq "healthy") {
                    Show-Success "$service está saludable ✅"
                    $healthy = $true
                    break
                }
                elseif ($status -eq "unhealthy") {
                    Show-Warning "$service no está saludable ⚠️"
                    break
                }
                else {
                    Write-Host "." -NoNewline
                    Start-Sleep -Seconds 10
                }
            }
            catch {
                Write-Host "." -NoNewline
                Start-Sleep -Seconds 10
            }
        }
        
        if (-not $healthy -and $i -gt $maxAttempts) {
            Show-Warning "$service no respondió en tiempo esperado ⏱️"
        }
        Write-Host ""
    }
}

# Mostrar información de acceso
function Show-AccessInfo {
    Write-Host ""
    Write-Host "🎉 ¡DESPLIEGUE COMPLETADO!" -ForegroundColor Green
    Write-Host "========================" -ForegroundColor Green
    Write-Host ""
    Write-Host "📱 SERVICIOS DISPONIBLES:" -ForegroundColor Cyan
    Write-Host "├── 🌐 API Gateway:          http://localhost:8080"
    Write-Host "├── 🏠 Aplicación Principal: http://localhost:8888"
    Write-Host "├── 🔍 Eureka Server:        http://localhost:8761"
    Write-Host "├── 📋 Gestor Solicitudes:   http://localhost:8082"
    Write-Host "├── 💰 Cotizador:            http://localhost:8083"
    Write-Host "└── 👋 Hello World:          http://localhost:8084"
    Write-Host ""
    Write-Host "🗄️ BASES DE DATOS:" -ForegroundColor Cyan
    Write-Host "├── 🐘 PostgreSQL:           localhost:5432 (user: arka, pass: arka123)"
    Write-Host "├── 🍃 MongoDB:              localhost:27017 (user: arka_admin, pass: Arca2025*)"
    Write-Host "└── 🔴 Redis:                localhost:6379 (pass: arka123)"
    Write-Host ""
    Write-Host "🛠️ HERRAMIENTAS:" -ForegroundColor Cyan
    Write-Host "├── 📊 Grafana:              http://localhost:3000 (admin/admin123)"
    Write-Host "├── 📈 Prometheus:           http://localhost:9091"
    Write-Host "├── 🗃️ Mongo Express:        http://localhost:8081"
    Write-Host "├── 📧 MailHog:              http://localhost:8025"
    Write-Host "└── 🐰 RabbitMQ:             http://localhost:15672 (arka/arka123)"
    Write-Host ""
    Write-Host "🔗 API ENDPOINTS PRINCIPALES:" -ForegroundColor Cyan
    Write-Host "├── 🛒 API E-commerce:       http://localhost:8888/productos"
    Write-Host "├── 🔐 API Auth:             http://localhost:8888/api/auth/login"
    Write-Host "├── 🌐 API Terceros:         http://localhost:8888/api/terceros/ObtenerDatos/productos"
    Write-Host "└── ❤️ Health Check:         http://localhost:8888/actuator/health"
    Write-Host ""
    Write-Host "📖 COMANDOS ÚTILES:" -ForegroundColor Cyan
    Write-Host "├── Ver logs:                docker-compose logs -f [servicio]"
    Write-Host "├── Detener todo:            docker-compose down"
    Write-Host "├── Reiniciar servicio:      docker-compose restart [servicio]"
    Write-Host "└── Ver estado:              docker-compose ps"
    Write-Host ""
}

# Función principal
function Main {
    Write-Host "🐳 ARKA E-commerce Platform - Despliegue Docker" -ForegroundColor Magenta
    Write-Host "===============================================" -ForegroundColor Magenta
    Write-Host ""
    
    Check-Docker
    Check-DockerCompose
    Cleanup-Previous
    Build-Images
    Start-Services
    Monitor-Services
    Show-AccessInfo
    
    Write-Host "✅ ¡Despliegue completado exitosamente!" -ForegroundColor Green
    Write-Host ""
    Write-Host "💡 Tip: Usa 'docker-compose logs -f' para ver los logs en tiempo real" -ForegroundColor Yellow
    Write-Host "🔄 Para detener todo: 'docker-compose down'" -ForegroundColor Yellow
}

# Ejecutar función principal
Main
