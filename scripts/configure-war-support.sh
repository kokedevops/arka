#!/bin/bash
# 📦 WAR Configuration Script for ARKA Services on Amazon Linux
# Script para configurar servicios para despliegue WAR en WildFly

set -e

# Default parameters
ACTION="configure"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m'

# Services that need WAR configuration
declare -A services=(
    ["arca-cotizador"]="Arca Cotizador Service"
    ["arca-gestor-solicitudes"]="Arca Gestor Solicitudes Service"  
    ["api-gateway"]="API Gateway Service"
)

# Function to configure Gradle for WAR packaging
configure_gradle_war() {
    local service_name=$1
    local build_file="$service_name/build.gradle"
    
    echo -e "${YELLOW}📦 Configuring $service_name for WAR packaging...${NC}"
    
    if [[ ! -f "$build_file" ]]; then
        echo -e "${RED}❌ build.gradle not found for $service_name${NC}"
        return 1
    fi
    
    # Read current build.gradle content
    local content=$(cat "$build_file")
    
    # Check if spring-boot-starter-web is already configured
    if grep -q "spring-boot-starter-web" "$build_file" && ! grep -q "spring-boot-starter-tomcat" "$build_file"; then
        echo -e "${YELLOW}⚙️  Adding Tomcat exclusion for WAR packaging...${NC}"
        
        # Create backup
        cp "$build_file" "$build_file.bak"
        
        # Add WAR configuration
        cat > "$build_file" << EOF
apply plugin: 'java'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'
apply plugin: 'war'

group = 'com.arka'
version = '1.0.0'
sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    implementation 'org.springframework.cloud:spring-cloud-starter-config'
    implementation 'org.springframework.cloud:spring-cloud-starter-bootstrap'
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
    
    // WAR deployment configuration
    providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'
    
    // JWT and Security
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    implementation 'io.jsonwebtoken:jjwt-impl:0.12.3'
    implementation 'io.jsonwebtoken:jjwt-jackson:0.12.3'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:\${springCloudVersion}"
    }
}

ext {
    springCloudVersion = '2023.0.3'
}

bootWar {
    enabled = true
    archiveBaseName = '$service_name'
    archiveVersion = '1.0.0'
}

bootJar {
    enabled = true
    archiveBaseName = '$service_name'
    archiveVersion = '1.0.0'
}

war {
    enabled = true
    archiveBaseName = '$service_name'
    archiveVersion = '1.0.0'
}

test {
    useJUnitPlatform()
}
EOF
        echo -e "${GREEN}✅ $service_name configured for WAR packaging${NC}"
    else
        echo -e "${GREEN}✅ $service_name already configured for WAR${NC}"
    fi
    
    return 0
}

# Function to create ServletInitializer
create_servlet_initializer() {
    local service_name=$1
    
    echo -e "${YELLOW}🔧 Creating ServletInitializer for $service_name...${NC}"
    
    # Determine main package based on service name
    local package_name
    case "$service_name" in
        "arca-cotizador")
            package_name="com.arka.cotizador"
            ;;
        "arca-gestor-solicitudes")
            package_name="com.arka.gestor"
            ;;
        "api-gateway")
            package_name="com.arka.gateway"
            ;;
        *)
            package_name="com.arka.service"
            ;;
    esac
    
    local src_dir="$service_name/src/main/java/${package_name//./\/}"
    mkdir -p "$src_dir"
    
    local initializer_file="$src_dir/ServletInitializer.java"
    
    # Find main application class
    local main_class=""
    if [[ -f "$src_dir/ArkaApplication.java" ]]; then
        main_class="ArkaApplication"
    elif [[ -f "$src_dir/Application.java" ]]; then
        main_class="Application"
    else
        # Find any class ending with Application
        main_class=$(find "$src_dir" -name "*Application.java" -exec basename {} .java \; | head -n 1)
        if [[ -z "$main_class" ]]; then
            main_class="Application"
        fi
    fi
    
    cat > "$initializer_file" << EOF
package $package_name;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * ServletInitializer for WAR deployment in WildFly
 * This class extends SpringBootServletInitializer to enable WAR deployment
 */
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources($main_class.class);
    }
}
EOF
    
    echo -e "${GREEN}✅ ServletInitializer created for $service_name${NC}"
    return 0
}

# Function to update application.yml for WAR deployment
update_application_config() {
    local service_name=$1
    
    echo -e "${YELLOW}⚙️  Updating application configuration for $service_name...${NC}"
    
    local config_file="$service_name/src/main/resources/application.yml"
    
    if [[ ! -f "$config_file" ]]; then
        echo -e "${YELLOW}⚠️  application.yml not found, creating basic configuration...${NC}"
        mkdir -p "$service_name/src/main/resources"
        
        cat > "$config_file" << EOF
server:
  port: 8080
  servlet:
    context-path: /$service_name

spring:
  application:
    name: $service_name
  profiles:
    active: aws
    
# WAR deployment configuration
logging:
  level:
    com.arka: DEBUG
    org.springframework.security: DEBUG
  file:
    name: logs/$service_name.log
EOF
    else
        echo -e "${GREEN}✅ Using existing application.yml for $service_name${NC}"
    fi
    
    return 0
}

# Function to show WAR configuration status
show_war_status() {
    echo -e "${CYAN}📊 WAR Configuration Status:${NC}"
    echo "=========================================="
    
    for service_name in "${!services[@]}"; do
        local service_description=${services[$service_name]}
        
        echo -e "\n${WHITE}🔍 $service_description ($service_name):${NC}"
        
        # Check build.gradle
        local build_file="$service_name/build.gradle"
        if [[ -f "$build_file" ]] && grep -q "apply plugin: 'war'" "$build_file"; then
            echo -e "  ${GREEN}✅ Gradle WAR plugin configured${NC}"
        else
            echo -e "  ${RED}❌ Gradle WAR plugin not configured${NC}"
        fi
        
        # Check ServletInitializer
        if find "$service_name/src/main/java" -name "ServletInitializer.java" -type f 2>/dev/null | grep -q .; then
            echo -e "  ${GREEN}✅ ServletInitializer present${NC}"
        else
            echo -e "  ${RED}❌ ServletInitializer missing${NC}"
        fi
        
        # Check if WAR can be built
        if [[ -f "$service_name/build/libs/$service_name.war" ]]; then
            local war_size=$(du -h "$service_name/build/libs/$service_name.war" | cut -f1)
            echo -e "  ${GREEN}✅ WAR file exists ($war_size)${NC}"
        else
            echo -e "  ${YELLOW}⚠️  WAR file not built yet${NC}"
        fi
    done
    
    echo -e "\n${CYAN}💡 To build WARs: ./gradlew bootWar${NC}"
    echo -e "${CYAN}💡 To deploy: ./deploy-wildfly.sh${NC}"
}

# Function to configure all services for WAR
configure_all_services() {
    echo -e "${GREEN}🔧 Configuring all services for WAR deployment...${NC}"
    
    local success_count=0
    local total_count=${#services[@]}
    
    for service_name in "${!services[@]}"; do
        local service_description=${services[$service_name]}
        
        echo -e "\n${YELLOW}📦 Configuring $service_description ($service_name)...${NC}"
        
        if [[ ! -d "$service_name" ]]; then
            echo -e "${RED}❌ Service directory not found: $service_name${NC}"
            continue
        fi
        
        # Configure Gradle for WAR
        if configure_gradle_war "$service_name"; then
            # Create ServletInitializer
            if create_servlet_initializer "$service_name"; then
                # Update application configuration
                if update_application_config "$service_name"; then
                    ((success_count++))
                    echo -e "${GREEN}✅ $service_name configured successfully${NC}"
                else
                    echo -e "${RED}❌ Failed to update application config for $service_name${NC}"
                fi
            else
                echo -e "${RED}❌ Failed to create ServletInitializer for $service_name${NC}"
            fi
        else
            echo -e "${RED}❌ Failed to configure Gradle for $service_name${NC}"
        fi
    done
    
    echo -e "\n${CYAN}📊 Configuration Summary:${NC}"
    echo -e "${WHITE}Successfully configured: $success_count/$total_count services${NC}"
    
    if [[ $success_count -eq $total_count ]]; then
        echo -e "${GREEN}🎉 All services configured for WAR deployment!${NC}"
        echo -e "${YELLOW}💡 Next steps:${NC}"
        echo -e "   1. Build WARs: ${WHITE}./gradlew bootWar${NC}"
        echo -e "   2. Deploy to WildFly: ${WHITE}./scripts/deploy-wildfly.sh${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  Some services failed configuration${NC}"
        return 1
    fi
}

# Function to build test WARs
build_test_wars() {
    echo -e "${YELLOW}🔨 Building test WARs...${NC}"
    
    # Clean and build
    ./gradlew clean
    ./gradlew bootWar
    
    echo -e "\n${CYAN}📦 WAR Build Results:${NC}"
    for service_name in "${!services[@]}"; do
        local war_file="$service_name/build/libs/$service_name.war"
        if [[ -f "$war_file" ]]; then
            local war_size=$(du -h "$war_file" | cut -f1)
            echo -e "  ${GREEN}✅ $service_name.war ($war_size)${NC}"
        else
            echo -e "  ${RED}❌ $service_name.war (build failed)${NC}"
        fi
    done
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -a|--action)
            ACTION="$2"
            shift 2
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  -a, --action    Action to perform (configure|status|build) [default: configure]"
            echo ""
            echo "Actions:"
            echo "  configure  - Configure all services for WAR deployment"
            echo "  status     - Show WAR configuration status"
            echo "  build      - Build test WARs"
            exit 0
            ;;
        *)
            echo "Unknown option $1"
            exit 1
            ;;
    esac
done

# Main execution
echo -e "${CYAN}📦 WAR Configuration Manager${NC}"
echo -e "${WHITE}Action: $ACTION${NC}"
echo "========================================"

case "$ACTION" in
    "configure")
        configure_all_services
        ;;
    "status")
        show_war_status
        ;;
    "build")
        build_test_wars
        ;;
    *)
        echo -e "${RED}❌ Unknown action: $ACTION${NC}"
        echo -e "${YELLOW}Valid actions: configure, status, build${NC}"
        exit 1
        ;;
esac

echo -e "${GREEN}🎉 Operation completed!${NC}"