#!/bin/bash
# 🐺 WildFly Deployment Configuration for ARKA
# Script para configurar y desplegar en WildFly Application Server en Amazon Linux

set -e

# Default parameters
WILDFLY_HOME="${WILDFLY_HOME:-/opt/wildfly}"
ACTION="deploy"
PROFILE="aws"

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
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  -w, --wildfly-home  WildFly installation path [default: /opt/wildfly]"
            echo "  -a, --action        Action to perform (deploy|stop|status) [default: deploy]"
            echo "  -p, --profile       Spring profile [default: aws]"
            exit 0
            ;;
        *)
            echo "Unknown option $1"
            exit 1
            ;;
    esac
done

# WildFly paths
WILDFLY_BIN="$WILDFLY_HOME/bin"
WILDFLY_DEPLOYMENTS="$WILDFLY_HOME/standalone/deployments"
WILDFLY_CONFIG="$WILDFLY_HOME/standalone/configuration"

# Services that can be deployed to WildFly (excluding infrastructure services)
declare -A wildfly_services=(
    ["arca-cotizador"]="arca-cotizador.war:/cotizador:8081"
    ["arca-gestor-solicitudes"]="arca-gestor-solicitudes.war:/gestor:8082"
    ["api-gateway"]="api-gateway.war:/gateway:8085"
)

# Infrastructure services (run as standalone JARs)
declare -A infra_services=(
    ["config-server"]="config-server.jar:8888:native,$PROFILE"
    ["eureka-server"]="eureka-server.jar:8761:$PROFILE"
)

# Function to test WildFly installation
test_wildfly_installation() {
    if [[ ! -d "$WILDFLY_HOME" ]]; then
        echo -e "${RED}❌ WildFly not found at: $WILDFLY_HOME${NC}"
        echo -e "${YELLOW}Please install WildFly or update the WILDFLY_HOME parameter${NC}"
        return 1
    fi
    
    if [[ ! -f "$WILDFLY_BIN/jboss-cli.sh" ]]; then
        echo -e "${RED}❌ WildFly CLI not found${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ WildFly installation verified: $WILDFLY_HOME${NC}"
    return 0
}

# Function to start WildFly
start_wildfly() {
    echo -e "${GREEN}🐺 Starting WildFly Application Server...${NC}"
    
    # Start WildFly in background
    nohup "$WILDFLY_BIN/standalone.sh" -c standalone-full.xml > logs/wildfly.log 2>&1 &
    local wildfly_pid=$!
    echo "$wildfly_pid" > pids/wildfly.pid
    
    # Wait for WildFly to start
    echo -e "${YELLOW}⏳ Waiting for WildFly to start...${NC}"
    sleep 30
    
    # Check if WildFly is running
    local max_attempts=20
    local attempt=0
    
    while [[ $attempt -lt $max_attempts ]]; do
        if curl -s -f "http://localhost:8080" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ WildFly is running${NC}"
            return 0
        fi
        
        ((attempt++))
        sleep 3
    done
    
    echo -e "${RED}❌ WildFly failed to start${NC}"
    return 1
}

# Function to deploy to WildFly
deploy_to_wildfly() {
    local service_name=$1
    local config=${wildfly_services[$service_name]}
    
    IFS=':' read -r war context port <<< "$config"
    
    local war_path="dist/wars/$war"
    
    if [[ ! -f "$war_path" ]]; then
        echo -e "${RED}❌ WAR not found: $war_path${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}📦 Deploying $service_name to WildFly...${NC}"
    
    # Copy WAR to deployments directory
    cp "$war_path" "$WILDFLY_DEPLOYMENTS/"
    
    # Wait for deployment
    local deployment_marker="$WILDFLY_DEPLOYMENTS/$war.deployed"
    local max_wait=60
    local waited=0
    
    while [[ ! -f "$deployment_marker" && $waited -lt $max_wait ]]; do
        sleep 2
        ((waited+=2))
    done
    
    if [[ -f "$deployment_marker" ]]; then
        echo -e "${GREEN}✅ $service_name deployed successfully${NC}"
        return 0
    else
        echo -e "${RED}❌ $service_name deployment failed${NC}"
        return 1
    fi
}

# Function to start infrastructure service
start_infra_service() {
    local service_name=$1
    local config=${infra_services[$service_name]}
    
    IFS=':' read -r jar port profile <<< "$config"
    
    local jar_path="dist/jars/$jar"
    
    if [[ ! -f "$jar_path" ]]; then
        echo -e "${RED}❌ JAR not found: $jar_path${NC}"
        return 1
    fi
    
    echo -e "${GREEN}🚀 Starting $service_name...${NC}"
    
    local java_args=(
        "-Xms256m"
        "-Xmx512m"
        "-Dspring.profiles.active=$profile"
        "-Dserver.port=$port"
        "-jar"
        "$jar_path"
    )
    
    nohup java "${java_args[@]}" > "logs/$service_name.log" 2>&1 &
    local pid=$!
    
    # Save PID for later management
    echo "$pid" > "pids/$service_name.pid"
    
    echo -e "${GREEN}✅ $service_name started with PID: $pid${NC}"
    return 0
}

# Function to build WARs
build_wars() {
    echo -e "${YELLOW}🔨 Building WAR files for WildFly deployment...${NC}"
    
    # Create WAR distribution directory
    mkdir -p "dist/wars"
    
    # Build WARs
    for service_name in "${!wildfly_services[@]}"; do
        echo -e "${WHITE}📦 Building $service_name.war...${NC}"
        
        # Build the WAR
        ./gradlew ":$service_name:bootWar"
        
        local war_source="$service_name/build/libs/$service_name.war"
        local war_dest="dist/wars/$service_name.war"
        
        if [[ -f "$war_source" ]]; then
            cp "$war_source" "$war_dest"
            echo -e "${GREEN}✅ $service_name.war created${NC}"
        else
            echo -e "${RED}❌ Failed to build $service_name.war${NC}"
            return 1
        fi
    done
    
    return 0
}

# Function to show WildFly status
show_wildfly_status() {
    echo -e "${CYAN}📊 WildFly Deployment Status:${NC}"
    echo -e "${WHITE}WildFly Home: $WILDFLY_HOME${NC}"
    echo -e "${WHITE}Profile: $PROFILE${NC}"
    echo "=========================================="
    
    # Check WildFly status
    if curl -s -f "http://localhost:8080" > /dev/null 2>&1; then
        echo -e "${GREEN}🐺 WildFly Server: RUNNING (http://localhost:8080)${NC}"
    else
        echo -e "${RED}🐺 WildFly Server: STOPPED${NC}"
    fi
    
    # Check deployed applications
    echo -e "\n${CYAN}📦 Deployed Applications:${NC}"
    for service_name in "${!wildfly_services[@]}"; do
        local config=${wildfly_services[$service_name]}
        IFS=':' read -r war context port <<< "$config"
        
        local deployment_file="$WILDFLY_DEPLOYMENTS/$war"
        local deployment_marker="$deployment_file.deployed"
        
        if [[ -f "$deployment_marker" ]]; then
            echo -e "  ${GREEN}✅ $service_name - DEPLOYED (http://localhost:8080$context)${NC}"
        elif [[ -f "$deployment_file" ]]; then
            echo -e "  ${YELLOW}⏳ $service_name - DEPLOYING${NC}"
        else
            echo -e "  ${RED}❌ $service_name - NOT DEPLOYED${NC}"
        fi
    done
    
    # Check infrastructure services
    echo -e "\n${CYAN}🏗️ Infrastructure Services:${NC}"
    for service_name in "${!infra_services[@]}"; do
        local config=${infra_services[$service_name]}
        IFS=':' read -r jar port profile <<< "$config"
        
        local pid_file="pids/$service_name.pid"
        
        if [[ -f "$pid_file" ]]; then
            local pid=$(cat "$pid_file")
            if kill -0 "$pid" 2>/dev/null; then
                echo -e "  ${GREEN}✅ $service_name - RUNNING (http://localhost:$port)${NC}"
            else
                echo -e "  ${RED}❌ $service_name - STOPPED${NC}"
                rm -f "$pid_file"
            fi
        else
            echo -e "  ${RED}❌ $service_name - STOPPED${NC}"
        fi
    done
}

# Function to stop services
stop_services() {
    echo -e "${YELLOW}🛑 Stopping WildFly and services...${NC}"
    
    # Stop infrastructure services
    for service_name in "${!infra_services[@]}"; do
        local pid_file="pids/$service_name.pid"
        if [[ -f "$pid_file" ]]; then
            local pid=$(cat "$pid_file")
            if kill -0 "$pid" 2>/dev/null; then
                kill -TERM "$pid"
                rm -f "$pid_file"
                echo -e "${YELLOW}🛑 Stopped $service_name${NC}"
            else
                echo -e "${YELLOW}⚠️  Could not stop $service_name${NC}"
                rm -f "$pid_file"
            fi
        fi
    done
    
    # Stop WildFly
    if [[ -f "pids/wildfly.pid" ]]; then
        local wildfly_pid=$(cat "pids/wildfly.pid")
        if kill -0 "$wildfly_pid" 2>/dev/null; then
            echo -e "${YELLOW}🛑 Stopping WildFly gracefully...${NC}"
            "$WILDFLY_BIN/jboss-cli.sh" --connect command=:shutdown 2>/dev/null || true
            sleep 5
            
            # Force kill if still running
            if kill -0 "$wildfly_pid" 2>/dev/null; then
                kill -9 "$wildfly_pid"
            fi
            
            rm -f "pids/wildfly.pid"
            echo -e "${YELLOW}🛑 WildFly stopped${NC}"
        fi
    fi
}

# Main execution
echo -e "${CYAN}🐺 WildFly Deployment Manager${NC}"
echo -e "${WHITE}Action: $ACTION | Profile: $PROFILE${NC}"
echo -e "${WHITE}WildFly Home: $WILDFLY_HOME${NC}"
echo "========================================"

if ! test_wildfly_installation; then
    exit 1
fi

# Create necessary directories
mkdir -p "dist/wars" "dist/jars" "pids" "logs"

case "$ACTION" in
    "deploy")
        echo -e "${GREEN}🚀 Starting WildFly deployment process...${NC}"
        
        # Build WARs and JARs
        echo -e "${YELLOW}🔨 Building artifacts...${NC}"
        ./gradlew clean bootJar
        
        if ! build_wars; then
            echo -e "${RED}❌ WAR build failed${NC}"
            exit 1
        fi
        
        # Copy JARs for infrastructure services
        cp "config-server/build/libs/config-server.jar" "dist/jars/" || exit 1
        cp "eureka-server/build/libs/eureka-server.jar" "dist/jars/" || exit 1
        
        # Start infrastructure services first
        echo -e "${YELLOW}🏗️ Starting infrastructure services...${NC}"
        for service_name in "${!infra_services[@]}"; do
            start_infra_service "$service_name"
            sleep 10
        done
        
        # Start WildFly
        if ! start_wildfly; then
            echo -e "${RED}❌ Failed to start WildFly${NC}"
            exit 1
        fi
        
        # Deploy applications to WildFly
        echo -e "${YELLOW}📦 Deploying applications to WildFly...${NC}"
        for service_name in "${!wildfly_services[@]}"; do
            if ! deploy_to_wildfly "$service_name"; then
                echo -e "${RED}❌ Failed to deploy $service_name${NC}"
                exit 1
            fi
        done
        
        echo -e "${GREEN}🎉 WildFly deployment completed!${NC}"
        ;;
    "status")
        show_wildfly_status
        ;;
    "stop")
        stop_services
        ;;
    *)
        echo -e "${RED}❌ Unknown action: $ACTION${NC}"
        echo -e "${YELLOW}Valid actions: deploy, stop, status${NC}"
        exit 1
        ;;
esac

echo -e "${GREEN}🎉 Operation completed!${NC}"