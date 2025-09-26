#!/bin/bash

# 🚀 WildFly WAR Deployment Script for ARKA Microservices
# Versión: 2.0 - WAR Deployment Support

set -euo pipefail

# =============================================================================
# CONFIGURACIÓN
# =============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# WildFly Configuration
WILDFLY_HOME="${WILDFLY_HOME:-/opt/wildfly}"
WILDFLY_USER="${WILDFLY_USER:-wildfly}"
WILDFLY_CLI="${WILDFLY_HOME}/bin/jboss-cli.sh"
WILDFLY_DEPLOYMENTS="${WILDFLY_HOME}/standalone/deployments"

# Build Configuration
GRADLE_OPTS="${GRADLE_OPTS:--Xmx2g -Dorg.gradle.daemon=false}"
SPRING_PROFILE="${SPRING_PROFILE:-aws}"

# Microservices Configuration
declare -a MICROSERVICES=("eureka-server" "config-server" "api-gateway" "arca-cotizador" "arca-gestor-solicitudes")
declare -a BUSINESS_SERVICES=("api-gateway" "arca-cotizador" "arca-gestor-solicitudes")

# Health Check Configuration
HEALTH_CHECK_TIMEOUT=180
HEALTH_CHECK_INTERVAL=10

# Colors para output
RED='\033[0;31m'
GREEN='\033[0;32m'  
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# =============================================================================
# FUNCIONES DE UTILIDAD
# =============================================================================

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_banner() {
    echo -e "${BLUE}"
    echo "=================================================="
    echo "🚀 ARKA WildFly WAR Deployment Script v2.0"
    echo "=================================================="
    echo -e "${NC}"
}

show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "OPTIONS:"
    echo "  --action ACTION     Action to perform: build|deploy|undeploy|redeploy|health|full"
    echo "  --service SERVICE   Specific service to deploy (optional)"
    echo "  --profile PROFILE   Spring profile to use (default: aws)"
    echo "  --skip-build        Skip build phase"
    echo "  --skip-tests        Skip tests during build"
    echo "  --force             Force deployment even if health checks fail"
    echo "  --help              Show this help message"
    echo ""
    echo "ACTIONS:"
    echo "  build     - Build WAR files only"
    echo "  deploy    - Deploy WARs to WildFly"
    echo "  undeploy  - Undeploy services from WildFly"
    echo "  redeploy  - Undeploy and deploy services"
    echo "  health    - Perform health checks only"
    echo "  full      - Build, deploy and health check (default)"
    echo ""
    echo "EXAMPLES:"
    echo "  $0 --action full"
    echo "  $0 --action deploy --service api-gateway"
    echo "  $0 --action redeploy --profile dev --skip-tests"
}

# =============================================================================
# FUNCIONES DE VALIDACIÓN
# =============================================================================

validate_requirements() {
    log_info "Validando requisitos del sistema..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        log_error "Java no encontrado. Instala Java 21."
        exit 1
    fi
    
    local java_version
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [[ "$java_version" -lt 11 ]]; then
        log_error "Java 11+ requerido. Versión encontrada: $java_version"
        exit 1
    fi
    
    # Check WildFly
    if [[ ! -d "$WILDFLY_HOME" ]]; then
        log_error "WildFly no encontrado en: $WILDFLY_HOME"
        log_error "Configura WILDFLY_HOME o instala WildFly"
        exit 1
    fi
    
    # Check WildFly CLI
    if [[ ! -f "$WILDFLY_CLI" ]]; then
        log_error "WildFly CLI no encontrado: $WILDFLY_CLI"
        exit 1
    fi
    
    # Check Gradle
    if [[ ! -f "$PROJECT_ROOT/gradlew" ]]; then
        log_error "Gradle wrapper no encontrado en: $PROJECT_ROOT"
        exit 1
    fi
    
    log_success "Todos los requisitos validados correctamente"
}

check_wildfly_status() {
    log_info "Verificando estado de WildFly..."
    
    if ! pgrep -f "jboss" > /dev/null; then
        log_warning "WildFly no está ejecutándose"
        
        log_info "Intentando iniciar WildFly..."
        if command -v systemctl &> /dev/null; then
            sudo systemctl start wildfly || {
                log_error "No se pudo iniciar WildFly con systemctl"
                return 1
            }
        else
            nohup "$WILDFLY_HOME/bin/standalone.sh" -b 0.0.0.0 > /dev/null 2>&1 &
            sleep 10
        fi
    fi
    
    # Verificar que WildFly responda
    local retries=0
    local max_retries=30
    
    while [[ $retries -lt $max_retries ]]; do
        if "$WILDFLY_CLI" --connect --command=":whoami" &>/dev/null; then
            log_success "WildFly está ejecutándose y respondiendo"
            return 0
        fi
        
        retries=$((retries + 1))
        log_info "Esperando conexión a WildFly... ($retries/$max_retries)"
        sleep 2
    done
    
    log_error "No se pudo conectar a WildFly después de $max_retries intentos"
    return 1
}

# =============================================================================
# FUNCIONES DE BUILD
# =============================================================================

build_wars() {
    log_info "🔨 Iniciando construcción de WARs..."
    
    cd "$PROJECT_ROOT"
    
    local gradle_cmd="./gradlew"
    local build_tasks="clean"
    
    # Agregar tasks de build para servicios de negocio
    for service in "${BUSINESS_SERVICES[@]}"; do
        build_tasks="$build_tasks :${service}:bootWar"
    done
    
    # Agregar tasks de build para servicios de infraestructura (JARs)
    build_tasks="$build_tasks :eureka-server:bootJar :config-server:bootJar"
    
    if [[ "${SKIP_TESTS:-false}" == "true" ]]; then
        build_tasks="$build_tasks -x test"
        log_warning "Omitiendo tests por parámetro --skip-tests"
    fi
    
    log_info "Ejecutando: $gradle_cmd $build_tasks"
    
    if ! GRADLE_OPTS="$GRADLE_OPTS" $gradle_cmd $build_tasks; then
        log_error "Error en la construcción de WARs"
        return 1
    fi
    
    # Verificar que los WARs se generaron correctamente
    for service in "${BUSINESS_SERVICES[@]}"; do
        local war_file="$PROJECT_ROOT/$service/build/libs/${service}.war"
        if [[ ! -f "$war_file" ]]; then
            log_error "WAR no encontrado: $war_file"
            return 1
        fi
        log_success "✅ WAR generado: $(basename "$war_file")"
    done
    
    log_success "🎉 Construcción de WARs completada exitosamente"
}

# =============================================================================
# FUNCIONES DE DEPLOYMENT
# =============================================================================

configure_wildfly() {
    log_info "🔧 Configurando WildFly para ARKA microservicios..."
    
    local config_file="$SCRIPT_DIR/wildfly-arka-config.cli"
    
    if [[ ! -f "$config_file" ]]; then
        log_error "Archivo de configuración no encontrado: $config_file"
        return 1
    fi
    
    if ! "$WILDFLY_CLI" --connect --file="$config_file"; then
        log_warning "Error aplicando configuración CLI - continuando..."
    fi
    
    log_success "Configuración de WildFly aplicada"
}

deploy_infrastructure_services() {
    log_info "📦 Desplegando servicios de infraestructura (JAR mode)..."
    
    # Eureka Server y Config Server se ejecutan como JARs independientes
    # porque no necesitan servlet container
    
    for service in "eureka-server" "config-server"; do
        local jar_file="$PROJECT_ROOT/$service/build/libs/${service}.jar"
        
        if [[ ! -f "$jar_file" ]]; then
            log_warning "JAR no encontrado para $service: $jar_file"
            continue
        fi
        
        log_info "Iniciando $service como proceso independiente..."
        
        # Matar proceso existente si existe
        pkill -f "$service.jar" || true
        sleep 2
        
        # Iniciar nuevo proceso
        nohup java -jar "$jar_file" \
            --spring.profiles.active="$SPRING_PROFILE" \
            > "$PROJECT_ROOT/logs/${service}.log" 2>&1 &
        
        local pid=$!
        echo $pid > "$PROJECT_ROOT/logs/${service}.pid"
        
        log_success "✅ $service iniciado (PID: $pid)"
    done
}

deploy_business_services() {
    log_info "🚀 Desplegando microservicios de negocio en WildFly..."
    
    for service in "${BUSINESS_SERVICES[@]}"; do
        if [[ -n "${TARGET_SERVICE:-}" && "$service" != "$TARGET_SERVICE" ]]; then
            log_info "Omitiendo $service (solo desplegando $TARGET_SERVICE)"
            continue
        fi
        
        deploy_single_service "$service"
    done
    
    log_success "✅ Deployment de microservicios completado"
}

deploy_single_service() {
    local service="$1"
    local war_file="$PROJECT_ROOT/$service/build/libs/${service}.war"
    
    if [[ ! -f "$war_file" ]]; then
        log_error "WAR no encontrado para $service: $war_file"
        return 1
    fi
    
    log_info "📦 Desplegando $service..."
    
    # Undeploy si ya existe
    if "$WILDFLY_CLI" --connect --command="deployment-info --name=${service}.war" &>/dev/null; then
        log_info "Removiendo deployment existente de $service..."
        "$WILDFLY_CLI" --connect --command="undeploy ${service}.war"
    fi
    
    # Deploy new WAR
    if ! "$WILDFLY_CLI" --connect --command="deploy $war_file"; then
        log_error "Error desplegando $service"
        return 1
    fi
    
    log_success "✅ $service desplegado correctamente"
}

undeploy_services() {
    log_info "🗑️  Removiendo deployments existentes..."
    
    for service in "${BUSINESS_SERVICES[@]}"; do
        if [[ -n "${TARGET_SERVICE:-}" && "$service" != "$TARGET_SERVICE" ]]; then
            continue
        fi
        
        if "$WILDFLY_CLI" --connect --command="deployment-info --name=${service}.war" &>/dev/null; then
            log_info "Removiendo $service..."
            "$WILDFLY_CLI" --connect --command="undeploy ${service}.war"
            log_success "✅ $service removido"
        else
            log_info "$service no estaba desplegado"
        fi
    done
    
    # Stop infrastructure services
    for service in "eureka-server" "config-server"; do
        local pid_file="$PROJECT_ROOT/logs/${service}.pid"
        if [[ -f "$pid_file" ]]; then
            local pid=$(cat "$pid_file")
            if kill -0 "$pid" 2>/dev/null; then
                log_info "Deteniendo $service (PID: $pid)..."
                kill "$pid"
                rm -f "$pid_file"
                log_success "✅ $service detenido"
            fi
        fi
        
        # Fallback: kill by process name
        pkill -f "$service.jar" || true
    done
}

# =============================================================================
# FUNCIONES DE HEALTH CHECK
# =============================================================================

perform_health_checks() {
    log_info "🏥 Realizando health checks..."
    
    # Create logs directory if not exists
    mkdir -p "$PROJECT_ROOT/logs"
    
    # Wait for services to start
    log_info "Esperando que los servicios inicien..."
    sleep 20
    
    local all_healthy=true
    
    # Check infrastructure services
    check_service_health "eureka-server" "8761" "/actuator/health" || all_healthy=false
    check_service_health "config-server" "8888" "/actuator/health" || all_healthy=false
    
    # Check business services (deployed in WildFly)
    for service in "${BUSINESS_SERVICES[@]}"; do
        local port
        case "$service" in
            "api-gateway") port="8080" ;;
            "arca-cotizador") port="8080" ;;
            "arca-gestor-solicitudes") port="8080" ;;
            *) port="8080" ;;
        esac
        
        check_service_health "$service" "$port" "/actuator/health" || all_healthy=false
    done
    
    if [[ "$all_healthy" == "true" ]]; then
        log_success "🎉 Todos los health checks pasaron exitosamente"
        return 0
    else
        log_error "❌ Algunos health checks fallaron"
        return 1
    fi
}

check_service_health() {
    local service="$1"
    local port="$2"
    local endpoint="$3"
    local url="http://localhost:${port}${endpoint}"
    
    log_info "Verificando health de $service en $url..."
    
    local retries=0
    local max_retries=$((HEALTH_CHECK_TIMEOUT / HEALTH_CHECK_INTERVAL))
    
    while [[ $retries -lt $max_retries ]]; do
        if curl -f -s "$url" > /dev/null 2>&1; then
            log_success "✅ $service está saludable"
            return 0
        fi
        
        retries=$((retries + 1))
        log_info "Health check $service... ($retries/$max_retries)"
        sleep $HEALTH_CHECK_INTERVAL
    done
    
    log_error "❌ $service health check falló después de ${HEALTH_CHECK_TIMEOUT}s"
    
    # Show service logs for debugging
    local log_file="$PROJECT_ROOT/logs/${service}.log"
    if [[ -f "$log_file" ]]; then
        log_warning "Últimas líneas del log de $service:"
        tail -20 "$log_file" | sed 's/^/  /'
    fi
    
    return 1
}

# =============================================================================
# FUNCIÓN PRINCIPAL
# =============================================================================

main() {
    local action="full"
    local skip_build=false
    local skip_tests=false
    local force=false
    
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --action)
                action="$2"
                shift 2
                ;;
            --service)
                TARGET_SERVICE="$2"
                shift 2
                ;;
            --profile)
                SPRING_PROFILE="$2"
                shift 2
                ;;
            --skip-build)
                skip_build=true
                shift
                ;;
            --skip-tests)
                SKIP_TESTS=true
                shift
                ;;
            --force)
                force=true
                shift
                ;;
            --help)
                show_usage
                exit 0
                ;;
            *)
                log_error "Parámetro desconocido: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    print_banner
    
    log_info "Configuración:"
    log_info "  - Acción: $action"
    log_info "  - Perfil Spring: $SPRING_PROFILE"
    log_info "  - WildFly Home: $WILDFLY_HOME"
    [[ -n "${TARGET_SERVICE:-}" ]] && log_info "  - Servicio específico: $TARGET_SERVICE"
    [[ "$skip_build" == "true" ]] && log_info "  - Omitir build: Sí"
    [[ "${SKIP_TESTS:-false}" == "true" ]] && log_info "  - Omitir tests: Sí"
    echo ""
    
    # Validar requisitos
    validate_requirements
    
    # Ejecutar acción
    case "$action" in
        "build")
            build_wars
            ;;
        "deploy")
            check_wildfly_status
            configure_wildfly
            if [[ "$skip_build" != "true" ]]; then
                build_wars
            fi
            deploy_infrastructure_services
            deploy_business_services
            ;;
        "undeploy")
            check_wildfly_status
            undeploy_services
            ;;
        "redeploy")
            check_wildfly_status
            undeploy_services
            sleep 5
            configure_wildfly
            if [[ "$skip_build" != "true" ]]; then
                build_wars
            fi
            deploy_infrastructure_services
            deploy_business_services
            ;;
        "health")
            perform_health_checks
            ;;
        "full")
            check_wildfly_status
            configure_wildfly
            if [[ "$skip_build" != "true" ]]; then
                build_wars
            fi
            undeploy_services
            sleep 5
            deploy_infrastructure_services
            deploy_business_services
            
            if [[ "$force" != "true" ]]; then
                perform_health_checks
            fi
            ;;
        *)
            log_error "Acción desconocida: $action"
            show_usage
            exit 1
            ;;
    esac
    
    log_success "🎉 Operación '$action' completada exitosamente!"
}

# Ejecutar función principal con todos los argumentos
main "$@"