#!/bin/bash

# ARKA Microservices - Docker Management Script
# Este script facilita la gestión de los microservicios ARKA usando Docker Compose

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para mostrar ayuda
show_help() {
    echo "ARKA Microservices Docker Management"
    echo ""
    echo "Uso: $0 [COMANDO] [OPCIONES]"
    echo ""
    echo "Comandos disponibles:"
    echo "  build         - Construir todas las imágenes Docker"
    echo "  up            - Iniciar todos los servicios"
    echo "  down          - Detener todos los servicios"
    echo "  restart       - Reiniciar todos los servicios"
    echo "  logs          - Ver logs de todos los servicios"
    echo "  status        - Ver estado de los servicios"
    echo "  clean         - Limpiar contenedores, imágenes y volúmenes"
    echo "  dev           - Modo desarrollo (solo servicios core)"
    echo "  prod          - Modo producción (todos los servicios)"
    echo "  health        - Verificar health checks"
    echo "  scale         - Escalar servicios específicos"
    echo ""
    echo "Opciones:"
    echo "  -h, --help    - Mostrar esta ayuda"
    echo "  -v, --verbose - Modo verbose"
    echo ""
    echo "Ejemplos:"
    echo "  $0 build              # Construir todas las imágenes"
    echo "  $0 up                 # Iniciar todos los servicios"
    echo "  $0 logs api-gateway   # Ver logs del API Gateway"
    echo "  $0 scale gestor-solicitudes=3  # Escalar servicio"
}

# Función para logging
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

error() {
    echo -e "${RED}[ERROR] $1${NC}" >&2
}

warning() {
    echo -e "${YELLOW}[WARNING] $1${NC}"
}

# Verificar si Docker está disponible
check_docker() {
    if ! command -v docker &> /dev/null; then
        error "Docker no está instalado o no está en PATH"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose no está instalado o no está en PATH"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        error "Docker daemon no está ejecutándose"
        exit 1
    fi
}

# Construir imágenes
build_images() {
    log "Construyendo imágenes Docker..."
    docker-compose build --no-cache
    log "Construcción completada"
}

# Iniciar servicios
start_services() {
    local mode=${1:-"prod"}
    
    if [ "$mode" = "dev" ]; then
        log "Iniciando servicios en modo desarrollo..."
        docker-compose up -d eureka-server api-gateway gestor-solicitudes
    else
        log "Iniciando todos los servicios..."
        docker-compose up -d
    fi
    
    log "Servicios iniciados. Esperando que estén listos..."
    sleep 30
    check_health
}

# Detener servicios
stop_services() {
    log "Deteniendo servicios..."
    docker-compose down
    log "Servicios detenidos"
}

# Reiniciar servicios
restart_services() {
    log "Reiniciando servicios..."
    docker-compose restart
    log "Servicios reiniciados"
}

# Ver logs
show_logs() {
    local service=${1:-""}
    
    if [ -n "$service" ]; then
        log "Mostrando logs de $service..."
        docker-compose logs -f "$service"
    else
        log "Mostrando logs de todos los servicios..."
        docker-compose logs -f
    fi
}

# Ver estado
show_status() {
    log "Estado de los servicios:"
    docker-compose ps
    
    echo ""
    log "Uso de recursos:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"
}

# Verificar health checks
check_health() {
    log "Verificando health checks..."
    
    services=("eureka-server:8761" "api-gateway:8080" "gestor-solicitudes:8082" "cotizador:8083")
    
    for service in "${services[@]}"; do
        IFS=':' read -r name port <<< "$service"
        if curl -f "http://localhost:$port/actuator/health" &> /dev/null; then
            echo -e "${GREEN}✓${NC} $name está saludable"
        else
            echo -e "${RED}✗${NC} $name no responde"
        fi
    done
}

# Limpiar recursos
clean_resources() {
    warning "Esta operación eliminará contenedores, imágenes y volúmenes. ¿Continuar? (y/N)"
    read -r response
    
    if [[ "$response" =~ ^[Yy]$ ]]; then
        log "Limpiando recursos Docker..."
        docker-compose down -v --rmi all --remove-orphans
        docker system prune -f
        log "Limpieza completada"
    else
        log "Operación cancelada"
    fi
}

# Escalar servicios
scale_services() {
    local scale_config=$1
    
    if [ -z "$scale_config" ]; then
        error "Especifica la configuración de escalado (ej: gestor-solicitudes=3)"
        exit 1
    fi
    
    log "Escalando servicios: $scale_config"
    docker-compose up -d --scale "$scale_config"
}

# Main script
main() {
    check_docker
    
    case ${1:-""} in
        "build")
            build_images
            ;;
        "up")
            start_services "${2:-prod}"
            ;;
        "down")
            stop_services
            ;;
        "restart")
            restart_services
            ;;
        "logs")
            show_logs "$2"
            ;;
        "status")
            show_status
            ;;
        "clean")
            clean_resources
            ;;
        "dev")
            start_services "dev"
            ;;
        "prod")
            start_services "prod"
            ;;
        "health")
            check_health
            ;;
        "scale")
            scale_services "$2"
            ;;
        "-h"|"--help"|"help")
            show_help
            ;;
        "")
            show_help
            ;;
        *)
            error "Comando desconocido: $1"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
