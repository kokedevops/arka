#!/bin/bash
# 🐳 Script de Despliegue Completo ARKA E-commerce Platform
# ==========================================================

echo "🚀 INICIANDO DESPLIEGUE COMPLETO DE ARKA E-commerce Platform"
echo "=============================================================="

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para mostrar estado
show_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

show_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

show_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

show_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verificar que Docker esté instalado y corriendo
check_docker() {
    show_status "Verificando Docker..."
    if ! command -v docker &> /dev/null; then
        show_error "Docker no está instalado. Por favor instala Docker primero."
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        show_error "Docker no está corriendo. Por favor inicia Docker."
        exit 1
    fi
    
    show_success "Docker está disponible y corriendo"
}

# Verificar que Docker Compose esté disponible
check_docker_compose() {
    show_status "Verificando Docker Compose..."
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        show_error "Docker Compose no está disponible."
        exit 1
    fi
    show_success "Docker Compose está disponible"
}

# Limpiar contenedores y volúmenes anteriores (opcional)
cleanup_previous() {
    show_warning "¿Deseas limpiar contenedores y volúmenes anteriores? (y/N)"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        show_status "Limpiando contenedores anteriores..."
        docker-compose down -v --remove-orphans 2>/dev/null || docker compose down -v --remove-orphans 2>/dev/null || true
        
        show_status "Limpiando imágenes de ARKA..."
        docker images | grep arka | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true
        
        show_success "Limpieza completada"
    fi
}

# Construir todas las imágenes
build_images() {
    show_status "Construyendo todas las imágenes Docker..."
    
    # Usar docker-compose o docker compose según disponibilidad
    if command -v docker-compose &> /dev/null; then
        DOCKER_COMPOSE_CMD="docker-compose"
    else
        DOCKER_COMPOSE_CMD="docker compose"
    fi
    
    $DOCKER_COMPOSE_CMD build --no-cache
    
    if [ $? -eq 0 ]; then
        show_success "Todas las imágenes construidas exitosamente"
    else
        show_error "Error construyendo las imágenes"
        exit 1
    fi
}

# Iniciar todos los servicios
start_services() {
    show_status "Iniciando todos los servicios..."
    
    # Usar docker-compose o docker compose según disponibilidad
    if command -v docker-compose &> /dev/null; then
        DOCKER_COMPOSE_CMD="docker-compose"
    else
        DOCKER_COMPOSE_CMD="docker compose"
    fi
    
    $DOCKER_COMPOSE_CMD up -d
    
    if [ $? -eq 0 ]; then
        show_success "Todos los servicios iniciados"
    else
        show_error "Error iniciando los servicios"
        exit 1
    fi
}

# Monitorear el estado de los servicios
monitor_services() {
    show_status "Monitoreando el estado de los servicios..."
    
    echo ""
    echo "🔄 Esperando que todos los servicios estén listos..."
    echo "   (Esto puede tomar 2-3 minutos)"
    echo ""
    
    # Esperar un poco para que los servicios inicien
    sleep 30
    
    # Mostrar estado de los servicios
    show_status "Estado actual de los servicios:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    echo ""
    show_status "Verificando health checks..."
    
    # Verificar servicios principales
    services=("arka-eureka-server" "arka-main-app" "arka-api-gateway" "arka-gestor-solicitudes" "arka-cotizador")
    
    for service in "${services[@]}"; do
        show_status "Verificando $service..."
        
        # Esperar hasta 3 minutos para que el servicio esté healthy
        for i in {1..18}; do
            status=$(docker inspect --format='{{.State.Health.Status}}' $service 2>/dev/null || echo "no-health-check")
            
            if [ "$status" = "healthy" ]; then
                show_success "$service está saludable ✅"
                break
            elif [ "$status" = "unhealthy" ]; then
                show_warning "$service no está saludable ⚠️"
                break
            else
                echo -n "."
                sleep 10
            fi
            
            if [ $i -eq 18 ]; then
                show_warning "$service no respondió en tiempo esperado ⏱️"
            fi
        done
    done
}

# Mostrar información de acceso
show_access_info() {
    echo ""
    echo "🎉 ¡DESPLIEGUE COMPLETADO!"
    echo "========================"
    echo ""
    echo "📱 SERVICIOS DISPONIBLES:"
    echo "├── 🌐 API Gateway:          http://localhost:8080"
    echo "├── 🏠 Aplicación Principal: http://localhost:8888"
    echo "├── 🔍 Eureka Server:        http://localhost:8761"
    echo "├── 📋 Gestor Solicitudes:   http://localhost:8082"
    echo "├── 💰 Cotizador:            http://localhost:8083"
    echo "└── 👋 Hello World:          http://localhost:8084"
    echo ""
    echo "🗄️ BASES DE DATOS:"
    echo "├── 🐘 PostgreSQL:           localhost:5432 (user: arka, pass: arka123)"
    echo "├── 🍃 MongoDB:              localhost:27017 (user: arka_admin, pass: Arca2025*)"
    echo "└── 🔴 Redis:                localhost:6379 (pass: arka123)"
    echo ""
    echo "🛠️ HERRAMIENTAS:"
    echo "├── 📊 Grafana:              http://localhost:3000 (admin/admin123)"
    echo "├── 📈 Prometheus:           http://localhost:9091"
    echo "├── 🗃️ Mongo Express:        http://localhost:8081"
    echo "├── 📧 MailHog:              http://localhost:8025"
    echo "└── 🐰 RabbitMQ:             http://localhost:15672 (arka/arka123)"
    echo ""
    echo "🔗 API ENDPOINTS PRINCIPALES:"
    echo "├── 🛒 API E-commerce:       http://localhost:8888/productos"
    echo "├── 🔐 API Auth:             http://localhost:8888/api/auth/login"
    echo "├── 🌐 API Terceros:         http://localhost:8888/api/terceros/ObtenerDatos/productos"
    echo "└── ❤️ Health Check:         http://localhost:8888/actuator/health"
    echo ""
    echo "📖 COMANDOS ÚTILES:"
    echo "├── Ver logs:                docker-compose logs -f [servicio]"
    echo "├── Detener todo:            docker-compose down"
    echo "├── Reiniciar servicio:      docker-compose restart [servicio]"
    echo "└── Ver estado:              docker-compose ps"
    echo ""
}

# Función principal
main() {
    echo "🐳 ARKA E-commerce Platform - Despliegue Docker"
    echo "==============================================="
    echo ""
    
    check_docker
    check_docker_compose
    cleanup_previous
    build_images
    start_services
    monitor_services
    show_access_info
    
    echo "✅ ¡Despliegue completado exitosamente!"
    echo ""
    echo "💡 Tip: Usa 'docker-compose logs -f' para ver los logs en tiempo real"
    echo "🔄 Para detener todo: 'docker-compose down'"
}

# Ejecutar función principal
main "$@"
