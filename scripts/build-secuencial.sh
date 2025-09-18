#!/bin/bash
# Script para build secuencial en Jenkins - Evita problemas de memoria

echo "=== LIMPIANDO CACHE DOCKER ==="
docker system prune -f

echo "=== CONSTRUYENDO SERVICIOS DE FORMA SECUENCIAL ==="

# Orden de construcción optimizado (dependencias primero)
services=(
    "config-server"
    "eureka-server" 
    "main-app"
    "api-gateway"
    "cotizador"
    "gestor-solicitudes"
    "hello-world-service"
)

for service in "${services[@]}"; do
    echo "=== Construyendo $service ==="
    docker compose build --no-cache $service
    
    if [ $? -ne 0 ]; then
        echo "ERROR: Falló la construcción de $service"
        exit 1
    fi
    
    echo "=== $service construido exitosamente ==="
    
    # Limpiar cache intermedio para liberar memoria
    docker image prune -f
    
    # Pausa breve para que el sistema libere recursos
    sleep 5
done

echo "=== TODOS LOS SERVICIOS CONSTRUIDOS EXITOSAMENTE ==="