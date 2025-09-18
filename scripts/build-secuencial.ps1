# Script para build secuencial en Jenkins - Evita problemas de memoria
# Versión PowerShell

Write-Host "=== LIMPIANDO CACHE DOCKER ===" -ForegroundColor Green
docker system prune -f

Write-Host "=== CONSTRUYENDO SERVICIOS DE FORMA SECUENCIAL ===" -ForegroundColor Green

# Orden de construcción optimizado (dependencias primero)
$services = @(
    "config-server",
    "eureka-server", 
    "main-app",
    "api-gateway",
    "cotizador",
    "gestor-solicitudes",
    "hello-world-service"
)

foreach ($service in $services) {
    Write-Host "=== Construyendo $service ===" -ForegroundColor Yellow
    docker compose build --no-cache $service
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Falló la construcción de $service" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "=== $service construido exitosamente ===" -ForegroundColor Green
    
    # Limpiar cache intermedio para liberar memoria
    docker image prune -f
    
    # Pausa breve para que el sistema libere recursos
    Start-Sleep -Seconds 5
}

Write-Host "=== TODOS LOS SERVICIOS CONSTRUIDOS EXITOSAMENTE ===" -ForegroundColor Green