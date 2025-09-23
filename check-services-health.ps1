# 🔍 Script para verificar el health de todos los servicios
# Health Check Completo para Microservicios

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "🔍 ARKA MICROSERVICES - HEALTH CHECK" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# Función para verificar health endpoint
function Test-ServiceHealth {
    param(
        [string]$ServiceName,
        [string]$Url,
        [int]$Port
    )
    
    try {
        # Verificar si el puerto está abierto
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $tcpClient.Connect("localhost", $Port)
        $tcpClient.Close()
        
        # Verificar health endpoint
        $response = Invoke-RestMethod -Uri $Url -TimeoutSec 10 -ErrorAction Stop
        
        if ($response.status -eq "UP") {
            Write-Host "✅ $ServiceName" -ForegroundColor Green -NoNewline
            Write-Host " - Status: UP" -ForegroundColor White -NoNewline
            Write-Host " - Port: $Port" -ForegroundColor Gray
            
            # Información adicional si está disponible
            if ($response.components) {
                foreach ($component in $response.components.PSObject.Properties) {
                    $componentStatus = $component.Value.status
                    $color = if ($componentStatus -eq "UP") { "Green" } else { "Yellow" }
                    Write-Host "   └─ $($component.Name): $componentStatus" -ForegroundColor $color
                }
            }
            return $true
        } else {
            Write-Host "⚠️ $ServiceName" -ForegroundColor Yellow -NoNewline
            Write-Host " - Status: $($response.status)" -ForegroundColor White -NoNewline
            Write-Host " - Port: $Port" -ForegroundColor Gray
            return $false
        }
    } catch {
        Write-Host "❌ $ServiceName" -ForegroundColor Red -NoNewline
        Write-Host " - NO RESPONSE" -ForegroundColor White -NoNewline
        Write-Host " - Port: $Port" -ForegroundColor Gray
        Write-Host "   └─ Error: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Función para verificar Eureka Registry
function Test-EurekaRegistry {
    try {
        Write-Host ""
        Write-Host "🔍 Verificando servicios registrados en Eureka..." -ForegroundColor Blue
        
        $response = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Headers @{"Accept"="application/json"} -TimeoutSec 10
        
        if ($response.applications.application) {
            Write-Host "✅ Servicios registrados en Eureka:" -ForegroundColor Green
            
            $applications = $response.applications.application
            if ($applications -is [System.Array]) {
                foreach ($app in $applications) {
                    $instanceCount = if ($app.instance -is [System.Array]) { $app.instance.Count } else { 1 }
                    Write-Host "   └─ $($app.name): $instanceCount instancia(s)" -ForegroundColor White
                }
            } else {
                $instanceCount = if ($applications.instance -is [System.Array]) { $applications.instance.Count } else { 1 }
                Write-Host "   └─ $($applications.name): $instanceCount instancia(s)" -ForegroundColor White
            }
        } else {
            Write-Host "⚠️ No hay servicios registrados en Eureka" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ Error al consultar Eureka Registry: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Función para verificar Gateway Routes
function Test-GatewayRoutes {
    try {
        Write-Host ""
        Write-Host "🌐 Verificando rutas del API Gateway..." -ForegroundColor Blue
        
        $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/gateway/routes" -TimeoutSec 10
        
        if ($response) {
            Write-Host "✅ Rutas configuradas en API Gateway:" -ForegroundColor Green
            foreach ($route in $response) {
                Write-Host "   └─ $($route.route_id): $($route.uri)" -ForegroundColor White
            }
        } else {
            Write-Host "⚠️ No hay rutas configuradas en API Gateway" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ Error al consultar Gateway Routes: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Lista de servicios para verificar
$services = @(
    @{Name="Config Server"; Url="http://localhost:8888/actuator/health"; Port=8888},
    @{Name="Eureka Server"; Url="http://localhost:8761/actuator/health"; Port=8761},
    @{Name="API Gateway"; Url="http://localhost:8080/actuator/health"; Port=8080},
    @{Name="Hello World Service"; Url="http://localhost:8081/actuator/health"; Port=8081},
    @{Name="Arca Cotizador"; Url="http://localhost:8082/actuator/health"; Port=8082},
    @{Name="Arca Gestor Solicitudes"; Url="http://localhost:8083/actuator/health"; Port=8083}
)

# Ejecutar health checks
Write-Host "🔍 Verificando health de todos los servicios..." -ForegroundColor Blue
Write-Host ""

$healthyServices = 0
$totalServices = $services.Count

foreach ($service in $services) {
    if (Test-ServiceHealth -ServiceName $service.Name -Url $service.Url -Port $service.Port) {
        $healthyServices++
    }
}

# Verificaciones adicionales
Test-EurekaRegistry
Test-GatewayRoutes

# Resumen final
Write-Host ""
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "📊 RESUMEN DEL HEALTH CHECK" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

$healthPercentage = [math]::Round(($healthyServices / $totalServices) * 100, 2)

if ($healthyServices -eq $totalServices) {
    Write-Host "🎉 TODOS LOS SERVICIOS ESTÁN SALUDABLES!" -ForegroundColor Green
    Write-Host "✅ $healthyServices/$totalServices servicios UP ($healthPercentage%)" -ForegroundColor Green
} elseif ($healthyServices -gt 0) {
    Write-Host "⚠️ ALGUNOS SERVICIOS TIENEN PROBLEMAS" -ForegroundColor Yellow
    Write-Host "⚠️ $healthyServices/$totalServices servicios UP ($healthPercentage%)" -ForegroundColor Yellow
} else {
    Write-Host "❌ TODOS LOS SERVICIOS ESTÁN DOWN" -ForegroundColor Red
    Write-Host "❌ $healthyServices/$totalServices servicios UP ($healthPercentage%)" -ForegroundColor Red
}

Write-Host ""
Write-Host "🌐 URLs de acceso rápido:" -ForegroundColor Cyan
Write-Host "   Eureka Dashboard: http://localhost:8761" -ForegroundColor White
Write-Host "   API Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "   Config Server: http://localhost:8888" -ForegroundColor White
Write-Host ""

Write-Host "🔧 Comandos de troubleshooting:" -ForegroundColor Cyan
Write-Host "   Ver logs del servicio: .\gradlew.bat :<service>:bootRun --debug" -ForegroundColor Gray
Write-Host "   Reiniciar servicio: Ctrl+C en la ventana del servicio, luego reiniciar" -ForegroundColor Gray
Write-Host "   Ver procesos Java: Get-Process java" -ForegroundColor Gray
Write-Host ""

# Test de conectividad entre servicios
Write-Host "🔗 Probando conectividad entre servicios..." -ForegroundColor Blue

# Test API Gateway -> Hello World
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/hello-world-service/hello" -TimeoutSec 5
    Write-Host "✅ API Gateway -> Hello World: OK" -ForegroundColor Green
} catch {
    Write-Host "❌ API Gateway -> Hello World: FAIL" -ForegroundColor Red
}

# Test API Gateway -> Cotizador
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/arca-cotizador/actuator/health" -TimeoutSec 5
    Write-Host "✅ API Gateway -> Cotizador: OK" -ForegroundColor Green
} catch {
    Write-Host "❌ API Gateway -> Cotizador: FAIL" -ForegroundColor Red
}

# Test API Gateway -> Gestor
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/arca-gestor-solicitudes/actuator/health" -TimeoutSec 5
    Write-Host "✅ API Gateway -> Gestor: OK" -ForegroundColor Green
} catch {
    Write-Host "❌ API Gateway -> Gestor: FAIL" -ForegroundColor Red
}

Write-Host ""
Write-Host "✅ Health check completado!" -ForegroundColor Green
Write-Host "Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
Write-Host ""

# Opción para monitoreo continuo
$response = Read-Host "¿Deseas ejecutar monitoreo continuo? (y/N)"
if ($response -match "^[Yy]") {
    Write-Host ""
    Write-Host "🔄 Iniciando monitoreo continuo (Ctrl+C para detener)..." -ForegroundColor Yellow
    Write-Host ""
    
    while ($true) {
        Start-Sleep -Seconds 30
        Write-Host "🔍 Health check automático - $(Get-Date -Format 'HH:mm:ss')" -ForegroundColor Blue
        
        $healthyNow = 0
        foreach ($service in $services) {
            try {
                $response = Invoke-RestMethod -Uri $service.Url -TimeoutSec 5
                if ($response.status -eq "UP") {
                    $healthyNow++
                }
            } catch {
                # Silenciar errores en monitoreo continuo
            }
        }
        
        if ($healthyNow -eq $totalServices) {
            Write-Host "✅ Todos los servicios OK ($healthyNow/$totalServices)" -ForegroundColor Green
        } else {
            Write-Host "⚠️ Servicios con problemas ($healthyNow/$totalServices)" -ForegroundColor Yellow
        }
    }
}