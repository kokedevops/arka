#!/bin/bash
# 🏭 WildFly JAR Deployment Manager para ARKA
# Script para desplegar JARs ejecutables en WildFly igual que gradle bootRun
# Compatible con Jenkins CI/CD pipeline

set -e

# Default parameters
WILDFLY_HOME="${WILDFLY_HOME:-/opt/wildfly}"
ACTION="deploy"
PROFILE="aws"
GRADLE_BUILD="true"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m'

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -w|--wildfly-home)
            WILDFLY_HOME="$2"
            shift 2
            ;;
        -a|--action)
            ACTION="$2"
            shift 2
            ;;
        -p|--profile)
            PROFILE="$2"
            shift 2
            ;;
        --skip-build)
            GRADLE_BUILD="false"
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  -w, --wildfly-home  WildFly installation path [default: /opt/wildfly]"
            echo "  -a, --action        Action to perform (deploy|stop|status|restart) [default: deploy]"
            echo "  -p, --profile       Spring profile [default: aws]"
            echo "  --skip-build        Skip gradle build step"
            exit 0
            ;;
        *)
            echo "Unknown option $1"
            exit 1
            ;;
    esac
done

# Service configuration - igual que gradle bootRun pero como JARs independientes
declare -A services=(
    ["config-server"]="config-server.jar:8888:native,$PROFILE:INFRASTRUCTURE:30"
    ["eureka-server"]="eureka-server.jar:8761:$PROFILE:INFRASTRUCTURE:20"
    ["api-gateway"]="api-gateway.jar:8085:$PROFILE:GATEWAY:10"
    ["arca-cotizador"]="arca-cotizador.jar:8081:$PROFILE:BUSINESS:5"
    ["arca-gestor-solicitudes"]="arca-gestor-solicitudes.jar:8082:$PROFILE:BUSINESS:5"
    ["hello-world-service"]="hello-world-service.jar:8083:$PROFILE:BUSINESS:5"
)

# JVM Configuration for each service type
declare -A jvm_configs=(
    ["INFRASTRUCTURE"]="-Xms512m -Xmx1024m -XX:+UseG1GC"
    ["GATEWAY"]="-Xms256m -Xmx512m -XX:+UseG1GC"
    ["BUSINESS"]="-Xms256m -Xmx512m -XX:+UseG1GC"
)

# Function to check WildFly installation
check_wildfly_installation() {
    if [[ ! -d "$WILDFLY_HOME" ]]; then
        echo -e "${RED}❌ WildFly not found at: $WILDFLY_HOME${NC}"
        echo -e "${YELLOW}Please install WildFly or set WILDFLY_HOME${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ WildFly installation verified: $WILDFLY_HOME${NC}"
    return 0
}

# Function to build JARs with gradle
build_jars() {
    if [[ "$GRADLE_BUILD" != "true" ]]; then
        echo -e "${YELLOW}⏭️  Skipping gradle build as requested${NC}"
        return 0
    fi
    
    echo -e "${YELLOW}🔨 Building JARs with gradle build...${NC}"
    
    # Clean and build all JARs
    ./gradlew clean build -x test
    
    # Create distribution directory
    mkdir -p "dist/jars"
    
    # Copy generated JARs
    local success_count=0
    local total_count=${#services[@]}
    
    for service_name in "${!services[@]}"; do
        local config=${services[$service_name]}
        IFS=':' read -r jar_name port profile type priority <<< "$config"
        
        local jar_source="$service_name/build/libs/$jar_name"
        local jar_dest="dist/jars/$jar_name"
        
        if [[ -f "$jar_source" ]]; then
            cp "$jar_source" "$jar_dest"
            local jar_size=$(du -h "$jar_dest" | cut -f1)
            echo -e "${GREEN}✅ $jar_name copied ($jar_size)${NC}"
            ((success_count++))
        else
            echo -e "${RED}❌ $jar_name not found at $jar_source${NC}"
        fi
    done
    
    if [[ $success_count -eq $total_count ]]; then
        echo -e "${GREEN}🎉 All JARs built successfully ($success_count/$total_count)${NC}"
        return 0
    else
        echo -e "${RED}❌ Some JARs failed to build ($success_count/$total_count)${NC}"
        return 1
    fi
}

# Function to start a service
start_service() {
    local service_name=$1
    local config=${services[$service_name]}
    IFS=':' read -r jar_name port profile type priority <<< "$config"
    
    local jar_path="dist/jars/$jar_name"
    
    if [[ ! -f "$jar_path" ]]; then
        echo -e "${RED}❌ JAR not found: $jar_path${NC}"
        return 1
    fi
    
    echo -e "${GREEN}🚀 Starting $service_name (port $port)...${NC}"
    
    # Get JVM configuration for service type
    local jvm_args=${jvm_configs[$type]}
    
    # Build Java command - igual que bootRun pero con JAR
    local java_args=(
        $jvm_args
        "-Dspring.profiles.active=$profile"
        "-Dserver.port=$port"
        "-Dmanagement.endpoints.web.exposure.include=health,info,metrics"
        "-Dlogging.file.name=logs/$service_name.log"
        "-jar"
        "$jar_path"
    )
    
    # Start service in background
    nohup java "${java_args[@]}" > "logs/$service_name.log" 2>&1 &
    local pid=$!
    
    # Save PID for management
    echo "$pid" > "pids/$service_name.pid"
    
    echo -e "${GREEN}✅ $service_name started with PID: $pid${NC}"
    
    # Wait for service to be ready
    local max_wait=60
    local waited=0
    local health_url="http://localhost:$port/actuator/health"
    
    echo -e "${YELLOW}⏳ Waiting for $service_name health check...${NC}"
    
    while [[ $waited -lt $max_wait ]]; do
        if curl -s -f "$health_url" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name is healthy${NC}"
            return 0
        fi
        
        sleep 2
        ((waited+=2))
    done
    
    echo -e "${YELLOW}⚠️  $service_name started but health check timeout${NC}"
    return 0
}

# Function to stop a service
stop_service() {
    local service_name=$1
    local pid_file="pids/$service_name.pid"
    
    if [[ ! -f "$pid_file" ]]; then
        echo -e "${YELLOW}⚠️  $service_name is not running${NC}"
        return 0
    fi
    
    local pid=$(cat "$pid_file")
    
    if ! kill -0 "$pid" 2>/dev/null; then
        echo -e "${YELLOW}⚠️  $service_name PID $pid is not running${NC}"
        rm -f "$pid_file"
        return 0
    fi
    
    echo -e "${YELLOW}🛑 Stopping $service_name (PID: $pid)...${NC}"
    
    # Graceful shutdown
    kill -TERM "$pid"
    
    # Wait for graceful shutdown
    local max_wait=15
    local waited=0
    
    while [[ $waited -lt $max_wait ]] && kill -0 "$pid" 2>/dev/null; do
        sleep 1
        ((waited++))
    done
    
    # Force kill if still running
    if kill -0 "$pid" 2>/dev/null; then
        echo -e "${RED}🔥 Force killing $service_name...${NC}"
        kill -9 "$pid"
    fi
    
    rm -f "$pid_file"
    echo -e "${GREEN}✅ $service_name stopped${NC}"
}

# Function to check service status
check_service_status() {
    local service_name=$1
    local config=${services[$service_name]}
    IFS=':' read -r jar_name port profile type priority <<< "$config"
    
    local pid_file="pids/$service_name.pid"
    local health_url="http://localhost:$port/actuator/health"
    
    if [[ -f "$pid_file" ]]; then
        local pid=$(cat "$pid_file")
        
        if kill -0 "$pid" 2>/dev/null; then
            if curl -s -f "$health_url" > /dev/null 2>&1; then
                echo -e "  ${GREEN}✅ $service_name - RUNNING & HEALTHY (PID: $pid, Port: $port)${NC}"
                return 0
            else
                echo -e "  ${YELLOW}⚠️  $service_name - RUNNING but UNHEALTHY (PID: $pid, Port: $port)${NC}"
                return 1
            fi
        else
            echo -e "  ${RED}❌ $service_name - STOPPED (stale PID file)${NC}"
            rm -f "$pid_file"
            return 2
        fi
    else
        echo -e "  ${RED}❌ $service_name - STOPPED${NC}"
        return 2
    fi
}

# Function to show deployment status
show_deployment_status() {
    echo -e "${CYAN}📊 WildFly JAR Deployment Status:${NC}"
    echo -e "${WHITE}WildFly Home: $WILDFLY_HOME${NC}"
    echo -e "${WHITE}Profile: $PROFILE${NC}"
    echo -e "${WHITE}Distribution: dist/jars/${NC}"
    echo "=================================================="
    
    # Sort services by priority (higher number = start first)
    local sorted_services=($(
        for service_name in "${!services[@]}"; do
            local config=${services[$service_name]}
            IFS=':' read -r jar_name port profile type priority <<< "$config"
            echo "$priority:$service_name"
        done | sort -rn | cut -d: -f2
    ))
    
    local running_count=0
    local healthy_count=0
    local total_count=${#services[@]}
    
    for service_name in "${sorted_services[@]}"; do
        local config=${services[$service_name]}
        IFS=':' read -r jar_name port profile type priority <<< "$config"
        
        echo -e "\n${CYAN}🔍 $service_name ($type):${NC}"
        
        check_service_status "$service_name"
        local status=$?
        
        case $status in
            0) ((running_count++)); ((healthy_count++)) ;;
            1) ((running_count++)) ;;
        esac
    done
    
    echo -e "\n${CYAN}📈 Summary:${NC}"
    echo -e "  ${WHITE}Total Services: $total_count${NC}"
    echo -e "  ${GREEN}Running: $running_count${NC}"
    echo -e "  ${GREEN}Healthy: $healthy_count${NC}"
    
    if [[ $healthy_count -eq $total_count ]]; then
        echo -e "\n${GREEN}🎉 All services are running and healthy!${NC}"
    elif [[ $running_count -gt 0 ]]; then
        echo -e "\n${YELLOW}⚠️  Some services may need attention${NC}"
    else
        echo -e "\n${RED}❌ No services are currently running${NC}"
    fi
}

# Function to deploy all services
deploy_all_services() {
    echo -e "${GREEN}🚀 Starting WildFly JAR deployment process...${NC}"
    
    # Create necessary directories
    mkdir -p "dist/jars" "pids" "logs"
    
    # Build JARs if requested
    if ! build_jars; then
        echo -e "${RED}❌ Failed to build JARs${NC}"
        return 1
    fi
    
    # Sort services by priority (higher number = start first)
    local sorted_services=($(
        for service_name in "${!services[@]}"; do
            local config=${services[$service_name]}
            IFS=':' read -r jar_name port profile type priority <<< "$config"
            echo "$priority:$service_name"
        done | sort -rn | cut -d: -f2
    ))
    
    # Start services in order
    local success_count=0
    
    for service_name in "${sorted_services[@]}"; do
        if start_service "$service_name"; then
            ((success_count++))
            
            # Add delay between service starts
            sleep 10
        else
            echo -e "${RED}❌ Failed to start $service_name${NC}"
        fi
    done
    
    echo -e "\n${CYAN}📊 Deployment Summary:${NC}"
    echo -e "${WHITE}Successfully started: $success_count/${#services[@]} services${NC}"
    
    if [[ $success_count -eq ${#services[@]} ]]; then
        echo -e "${GREEN}🎉 All services deployed successfully!${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  Some services failed to deploy${NC}"
        return 1
    fi
}

# Function to stop all services
stop_all_services() {
    echo -e "${YELLOW}🛑 Stopping all services...${NC}"
    
    # Stop in reverse order (business first, infrastructure last)
    local sorted_services=($(
        for service_name in "${!services[@]}"; do
            local config=${services[$service_name]}
            IFS=':' read -r jar_name port profile type priority <<< "$config"
            echo "$priority:$service_name"
        done | sort -n | cut -d: -f2
    ))
    
    for service_name in "${sorted_services[@]}"; do
        stop_service "$service_name"
    done
    
    echo -e "${GREEN}✅ All services stopped${NC}"
}

# Function to restart all services
restart_all_services() {
    echo -e "${YELLOW}🔄 Restarting all services...${NC}"
    
    stop_all_services
    sleep 5
    deploy_all_services
}

# Main execution
echo -e "${CYAN}🏭 WildFly JAR Deployment Manager${NC}"
echo -e "${WHITE}Action: $ACTION | Profile: $PROFILE${NC}"
echo -e "${WHITE}Gradle Build: $GRADLE_BUILD${NC}"
echo "================================================"

if ! check_wildfly_installation; then
    exit 1
fi

case "$ACTION" in
    "deploy")
        deploy_all_services
        ;;
    "stop")
        stop_all_services
        ;;
    "status")
        show_deployment_status
        ;;
    "restart")
        restart_all_services
        ;;
    *)
        echo -e "${RED}❌ Unknown action: $ACTION${NC}"
        echo -e "${YELLOW}Valid actions: deploy, stop, status, restart${NC}"
        exit 1
        ;;
esac

echo -e "${GREEN}🎉 Operation completed!${NC}"