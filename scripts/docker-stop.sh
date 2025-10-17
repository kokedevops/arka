#!/bin/bash

echo "🐳 ARKA - Parando Docker Compose"
echo "================================="
echo ""

# Navigate to project root
cd "$(dirname "$0")/.."

echo "📁 Directorio del proyecto: $(pwd)"
echo ""

# Check if docker-compose.yml exists
if [[ ! -f "docker-compose.yml" ]]; then
    echo "❌ ERROR: docker-compose.yml no encontrado"
    echo "   Asegúrate de estar en el directorio correcto"
    exit 1
fi

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    echo "❌ ERROR: Docker no está corriendo"
    echo "   No se pueden parar los servicios"
    exit 1
fi

echo "✅ Docker está disponible"
echo ""

# Check if services are running
echo "🔍 Verificando servicios en ejecución..."
if ! docker-compose ps | grep -q "Up"; then
    echo "ℹ️  No hay servicios corriendo con Docker Compose"
    
    # Check for orphan containers
    orphans=$(docker ps -a --filter "label=com.docker.compose.project=arka-1" --format "{{.Names}}")
    if [[ -n "$orphans" ]]; then
        echo ""
        echo "🔍 Contenedores huérfanos encontrados:"
        echo "$orphans"
        echo ""
        read -p "¿Quieres eliminar contenedores huérfanos? (y/N): " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "🧹 Eliminando contenedores huérfanos..."
            docker-compose down --remove-orphans
        fi
    fi
    
    exit 0
fi

echo "📋 Servicios actualmente corriendo:"
docker-compose ps
echo ""

# Confirm shutdown
read -p "¿Estás seguro de que quieres parar todos los servicios? (y/N): " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Operación cancelada"
    exit 0
fi

echo "🛑 Parando servicios Docker Compose..."

# Stop and remove containers
if docker-compose down; then
    echo "✅ Servicios parados exitosamente"
else
    echo "❌ ERROR: Falló al parar servicios"
    exit 1
fi

echo ""
echo "🧹 Limpiando recursos..."

# Option to remove volumes
echo ""
read -p "¿Quieres eliminar también los volúmenes de datos? (y/N): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🗑️  Eliminando volúmenes..."
    docker-compose down -v
    echo "✅ Volúmenes eliminados"
fi

# Option to remove images
echo ""
read -p "¿Quieres eliminar las imágenes Docker construidas? (y/N): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🗑️  Eliminando imágenes ARKA..."
    
    # Remove ARKA images
    arka_images=$(docker images --filter "reference=*arka*" --filter "reference=*arka*" -q)
    if [[ -n "$arka_images" ]]; then
        docker rmi $arka_images 2>/dev/null || true
        echo "✅ Imágenes ARKA eliminadas"
    else
        echo "ℹ️  No se encontraron imágenes ARKA para eliminar"
    fi
fi

# Check for leftover containers
echo ""
echo "🔍 Verificando contenedores restantes..."
remaining=$(docker ps -a --filter "label=com.docker.compose.project=arka-1" --format "{{.Names}}")
if [[ -n "$remaining" ]]; then
    echo "⚠️  Contenedores restantes encontrados:"
    echo "$remaining"
    echo ""
    read -p "¿Quieres forzar la eliminación de contenedores restantes? (y/N): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "⚡ Forzando eliminación..."
        docker ps -a --filter "label=com.docker.compose.project=arka-1" -q | xargs docker rm -f 2>/dev/null || true
        echo "✅ Contenedores restantes eliminados"
    fi
else
    echo "✅ No hay contenedores restantes"
fi

# Check ports
echo ""
echo "🔍 Verificando que los puertos estén libres..."
ports=("8761" "8080" "8081" "8082" "8083" "3306")

for port in "${ports[@]}"; do
    if netstat -tuln 2>/dev/null | grep -q ":$port " || ss -tuln 2>/dev/null | grep -q ":$port "; then
        echo "⚠️  Puerto $port aún en uso"
    else
        echo "✅ Puerto $port libre"
    fi
done

echo ""
echo "================================="
echo "🎉 Docker Compose parado completamente"
echo "================================="
echo ""
echo "📊 Resumen:"
echo "✅ Servicios Docker parados"
echo "✅ Contenedores eliminados"
echo "✅ Recursos limpiados"
echo ""
echo "🔄 Para volver a iniciar:"
echo "./docker-start.sh"
echo ""
echo "🛠️  Para limpiar completamente Docker:"
echo "docker system prune -a --volumes  # ⚠️  Elimina TODO en Docker"
