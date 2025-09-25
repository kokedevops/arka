#!/bin/bash
# 🚀 ARKA AWS Production Deployment
# Script optimizado para despliegue en Amazon Linux con perfil AWS

set -e  # Exit on any error

# Default parameters
ACTION="deploy"
SERVICE="all"
PROFILE="aws"
REGION="us-east-1"

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
        -a|--action)
            ACTION="$2"
            shift 2
            ;;
        -s|--service)
            SERVICE="$2"
            shift 2
            ;;
        -p|--profile)
            PROFILE="$2"
            shift 2
            ;;
        -r|--region)
            REGION="$2"
            shift 2
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  -a, --action    Action to perform (deploy|stop|status) [default: deploy]"
            echo "  -s, --service   Service to manage (all|service-name) [default: all]"
            echo "  -p, --profile   Spring profile [default: aws]"
            echo "  -r, --region    AWS region [default: us-east-1]"
            exit 0
            ;;
        *)
            echo "Unknown option $1"
            exit 1
            ;;
    esac
done

# Service Configuration for AWS
declare -A services=(
    ["config-server"]="config-server.jar:8888:native,aws:1:/actuator/health:30"
    ["eureka-server"]="eureka-server.jar:8761:aws:2:/actuator/health:30"
    ["api-gateway"]="api-gateway.jar:8085:aws:3:/actuator/health:45"
    ["arca-cotizador"]="arca-cotizador.jar:8081:aws:4:/actuator/health:45"
    ["arca-gestor-solicitudes"]="arca-gestor-solicitudes.jar:8082:aws:5:/actuator/health:45"
    ["hello-world-service"]="hello-world-service.jar:8083:aws:6:/actuator/health:30"
)

# Directories
dist_dir="dist/jars"
logs_dir="logs/aws"
pids_dir="pids/aws"

# Create AWS specific directories
mkdir -p "$logs_dir" "$pids_dir"

# Function to parse service configuration
parse_service_config() {
    local service_name=$1
    local config=${services[$service_name]}
    
    IFS=':' read -r jar port profile order health_path startup_time <<< "$config"
    
    echo "$jar:$port:$profile:$order:$health_path:$startup_time"
}

# Function to test service health
test_service_health() {
    local service_name=$1
    local config=$(parse_service_config "$service_name")
    local port=$(echo "$config" | cut -d':' -f2)
    local health_path=$(echo "$config" | cut -d':' -f5)
    
    local max_attempts=30
    local attempt=0
    
    echo -e "${YELLOW}🏥 Health checking $service_name...${NC}"
    
    while [[ $attempt -lt $max_attempts ]]; do
        if curl -s -f "http://localhost:$port$health_path" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name is healthy${NC}"
            return 0
        fi
        
        ((attempt++))
        sleep 2
    done
    
    echo -e "${RED}❌ $service_name health check failed after $max_attempts attempts${NC}"
    return 1
}

# Function to start AWS service
start_aws_service() {
    local service_name=$1
    local config=$(parse_service_config "$service_name")
    
    IFS=':' read -r jar port profile order health_path startup_time <<< "$config"
    
    local jar_path="$dist_dir/$jar"
    local log_path="$logs_dir/$service_name.log"
    local pid_path="$pids_dir/$service_name.pid"
    
    if [[ ! -f "$jar_path" ]]; then
        echo -e "${RED}❌ JAR not found: $jar_path${NC}"
        return 1
    fi
    
    # Kill any existing process on this port
    local existing_pid=$(lsof -ti:$port 2>/dev/null || true)
    if [[ -n "$existing_pid" ]]; then
        echo -e "${YELLOW}⚠️  Killing existing process on port $port (PID: $existing_pid)${NC}"
        kill -9 "$existing_pid" 2>/dev/null || true
        sleep 3
    fi
    
    echo -e "${GREEN}🚀 Starting $service_name (AWS Profile)...${NC}"
    
    # Java arguments optimized for AWS deployment
    local java_args=(
        "-Xms512m"
        "-Xmx1024m"
        "-XX:+UseG1GC"
        "-XX:MaxGCPauseMillis=200"
        "-Dspring.profiles.active=$profile"
        "-Dserver.port=$port"
        "-Daws.region=$REGION"
        "-Dlogging.file.name=$log_path"
        "-jar"
        "$jar_path"
    )
    
    # Start service in background
    nohup java "${java_args[@]}" > "$log_path" 2>&1 &
    local pid=$!
    
    # Save PID
    echo "$pid" > "$pid_path"
    echo -e "${GREEN}✅ $service_name started with PID: $pid${NC}"
    
    # Wait for startup
    echo -e "${YELLOW}⏳ Waiting $startup_time seconds for $service_name to start...${NC}"
    sleep "$startup_time"
    
    # Health check
    if test_service_health "$service_name"; then
        return 0
    else
        echo -e "${RED}❌ $service_name failed health check${NC}"
        kill "$pid" 2>/dev/null || true
        rm -f "$pid_path"
        return 1
    fi
}

# Function to stop AWS service
stop_aws_service() {
    local service_name=$1
    local pid_path="$pids_dir/$service_name.pid"
    
    if [[ -f "$pid_path" ]]; then
        local pid=$(cat "$pid_path")
        if kill -0 "$pid" 2>/dev/null; then
            kill -TERM "$pid"
            sleep 5
            
            # Force kill if still running
            if kill -0 "$pid" 2>/dev/null; then
                kill -9 "$pid"
            fi
            
            echo -e "${YELLOW}🛑 Stopped $service_name (PID: $pid)${NC}"
        else
            echo -e "${YELLOW}⚠️  Process $pid for $service_name not found${NC}"
        fi
        rm -f "$pid_path"
    else
        echo -e "${YELLOW}⚠️  No PID file found for $service_name${NC}"
    fi
}

# Function to deploy all services
deploy_all_services() {
    echo -e "${CYAN}🏗️ Starting AWS Deployment...${NC}"
    
    local deployed_services=()
    local failed_services=()
    
    # Get services sorted by order
    local sorted_services=()
    while IFS= read -r service; do
        sorted_services+=("$service")
    done < <(for service in "${!services[@]}"; do
        local config=$(parse_service_config "$service")
        local order=$(echo "$config" | cut -d':' -f4)
        echo "$order:$service"
    done | sort -n | cut -d':' -f2)
    
    # Deploy services in order
    for service_name in "${sorted_services[@]}"; do
        echo -e "${WHITE}📦 Deploying $service_name...${NC}"
        
        if start_aws_service "$service_name"; then
            deployed_services+=("$service_name")
            echo -e "${GREEN}✅ $service_name deployed successfully${NC}"
        else
            failed_services+=("$service_name")
            echo -e "${RED}❌ $service_name deployment failed${NC}"
            break  # Stop deployment on first failure
        fi
    done
    
    # Results
    echo -e "\n${CYAN}📊 AWS Deployment Results:${NC}"
    echo -e "${GREEN}✅ Deployed: ${deployed_services[*]}${NC}"
    
    if [[ ${#failed_services[@]} -gt 0 ]]; then
        echo -e "${RED}❌ Failed: ${failed_services[*]}${NC}"
        return 1
    fi
    
    # Final health check
    echo -e "\n${YELLOW}🏥 Final health check...${NC}"
    sleep 10
    
    for service_name in "${deployed_services[@]}"; do
        test_service_health "$service_name" > /dev/null
    done
    
    echo -e "\n${GREEN}🎉 AWS Deployment completed successfully!${NC}"
    return 0
}

# Function to show AWS status
show_aws_status() {
    echo -e "${CYAN}📊 ARKA AWS Services Status:${NC}"
    echo -e "${WHITE}Region: $REGION | Profile: $PROFILE${NC}"
    echo "============================================="
    
    # Get services sorted by order
    local sorted_services=()
    while IFS= read -r service; do
        sorted_services+=("$service")
    done < <(for service in "${!services[@]}"; do
        local config=$(parse_service_config "$service")
        local order=$(echo "$config" | cut -d':' -f4)
        echo "$order:$service"
    done | sort -n | cut -d':' -f2)
    
    for service_name in "${sorted_services[@]}"; do
        local config=$(parse_service_config "$service_name")
        local port=$(echo "$config" | cut -d':' -f2)
        local pid_path="$pids_dir/$service_name.pid"
        
        if [[ -f "$pid_path" ]]; then
            local pid=$(cat "$pid_path")
            if kill -0 "$pid" 2>/dev/null; then
                if test_service_health "$service_name" > /dev/null 2>&1; then
                    echo -e "  ${GREEN}✅ $service_name:$port - RUNNING (HEALTHY)${NC}"
                else
                    echo -e "  ${YELLOW}⚠️  $service_name:$port - RUNNING (UNHEALTHY)${NC}"
                fi
            else
                echo -e "  ${RED}❌ $service_name:$port - STOPPED${NC}"
                rm -f "$pid_path"
            fi
        else
            echo -e "  ${RED}❌ $service_name:$port - STOPPED${NC}"
        fi
    done
    
    # Show endpoints
    echo -e "\n${CYAN}🔗 Service Endpoints:${NC}"
    echo -e "  ${WHITE}📊 Eureka Dashboard: http://localhost:8761${NC}"
    echo -e "  ${WHITE}🚪 API Gateway: http://localhost:8085${NC}"
    echo -e "  ${WHITE}⚙️  Config Server: http://localhost:8888${NC}"
}

# Main execution
echo -e "${CYAN}🌟 ARKA AWS Production Manager${NC}"
echo -e "${WHITE}Action: $ACTION | Service: $SERVICE${NC}"
echo -e "${WHITE}Region: $REGION | Profile: $PROFILE${NC}"
echo "============================================"

case "$ACTION" in
    "deploy")
        # Stop all services first
        echo -e "${YELLOW}🛑 Stopping existing services...${NC}"
        for service in "${!services[@]}"; do
            stop_aws_service "$service"
        done
        sleep 5
        
        # Deploy all services
        if deploy_all_services; then
            exit 0
        else
            exit 1
        fi
        ;;
    "stop")
        echo -e "${YELLOW}🛑 Stopping AWS services...${NC}"
        # Get services sorted by order (reverse for stopping)
        local sorted_services=()
        while IFS= read -r service; do
            sorted_services+=("$service")
        done < <(for service in "${!services[@]}"; do
            local config=$(parse_service_config "$service")
            local order=$(echo "$config" | cut -d':' -f4)
            echo "$order:$service"
        done | sort -nr | cut -d':' -f2)
        
        for service_name in "${sorted_services[@]}"; do
            stop_aws_service "$service_name"
        done
        ;;
    "status")
        show_aws_status
        ;;
    *)
        echo -e "${RED}❌ Unknown action: $ACTION${NC}"
        echo -e "${YELLOW}Valid actions: deploy, stop, status${NC}"
        exit 1
        ;;
esac

echo -e "${GREEN}🎉 Operation completed!${NC}"