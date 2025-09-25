#!/bin/bash
# 🔧 WildFly JAR Configuration Setup Script
# Script para configurar WildFly específicamente para manejar múltiples JARs ejecutables

set -e

# Load configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/wildfly-jar-config.env"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m'

# Default parameters
WILDFLY_HOME="${WILDFLY_HOME:-/opt/wildfly}"
ACTION="configure"

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
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  -w, --wildfly-home  WildFly installation path [default: /opt/wildfly]"
            echo "  -a, --action        Action to perform (configure|validate|cleanup) [default: configure]"
            exit 0
            ;;
        *)
            echo "Unknown option $1"
            exit 1
            ;;
    esac
done

# Function to check WildFly installation
check_wildfly() {
    if [[ ! -d "$WILDFLY_HOME" ]]; then
        echo -e "${RED}❌ WildFly not found at: $WILDFLY_HOME${NC}"
        return 1
    fi
    
    if [[ ! -f "$WILDFLY_HOME/bin/standalone.sh" ]]; then
        echo -e "${RED}❌ WildFly standalone script not found${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ WildFly installation verified${NC}"
    return 0
}

# Function to create directories
create_directories() {
    echo -e "${YELLOW}📁 Creating necessary directories...${NC}"
    
    local dirs=(
        "$PID_DIR"
        "$LOG_DIR" 
        "$JAR_DIR"
        "$BACKUP_DIR"
        "$WILDFLY_HOME/arka-jars"
        "/opt/arka/config"
        "/opt/arka/scripts"
    )
    
    for dir in "${dirs[@]}"; do
        if [[ ! -d "$dir" ]]; then
            mkdir -p "$dir"
            echo -e "${GREEN}✅ Created: $dir${NC}"
        else
            echo -e "${CYAN}ℹ️  Already exists: $dir${NC}"
        fi
    done
}

# Function to configure WildFly standalone
configure_wildfly_standalone() {
    echo -e "${YELLOW}⚙️  Configuring WildFly standalone...${NC}"
    
    local standalone_conf="$WILDFLY_HOME/bin/standalone.conf"
    local backup_file="$standalone_conf.backup-$(date +%Y%m%d-%H%M%S)"
    
    # Backup original configuration
    if [[ -f "$standalone_conf" && ! -f "$standalone_conf.backup" ]]; then
        cp "$standalone_conf" "$backup_file"
        echo -e "${GREEN}✅ Backup created: $backup_file${NC}"
    fi
    
    # Create optimized standalone configuration
    cat > "$standalone_conf" << EOF
#!/bin/bash

# WildFly Configuration for ARKA JAR Deployment
# Optimized for multiple Spring Boot JAR execution

## Global settings
if [ "x\$JAVA_OPTS" = "x" ]; then
   JAVA_OPTS="$WILDFLY_OPTS"
   JAVA_OPTS="\$JAVA_OPTS -Djboss.modules.system.pkgs=\$JBOSS_MODULES_SYSTEM_PKGS"
   JAVA_OPTS="\$JAVA_OPTS -Djava.awt.headless=true"
fi

## Memory settings optimized for JAR containers
JAVA_OPTS="\$JAVA_OPTS -Xms1024m -Xmx2048m"
JAVA_OPTS="\$JAVA_OPTS -XX:+UseG1GC"
JAVA_OPTS="\$JAVA_OPTS -XX:MaxGCPauseMillis=200"
JAVA_OPTS="\$JAVA_OPTS -XX:+UnlockExperimentalVMOptions"

## Network binding
JAVA_OPTS="\$JAVA_OPTS -Djboss.bind.address=$WILDFLY_BIND_ADDRESS"
JAVA_OPTS="\$JAVA_OPTS -Djboss.bind.address.management=$WILDFLY_MANAGEMENT_BIND_ADDRESS"

## Port offset to avoid conflicts with Spring Boot services
JAVA_OPTS="\$JAVA_OPTS -Djboss.socket.binding.port-offset=100"

## File system optimization
JAVA_OPTS="\$JAVA_OPTS -Djboss.server.temp.dir=/tmp/wildfly"

## JMX configuration
if [[ "$ENABLE_JMX_MONITORING" == "true" ]]; then
    JAVA_OPTS="\$JAVA_OPTS -Dcom.sun.management.jmxremote"
    JAVA_OPTS="\$JAVA_OPTS -Dcom.sun.management.jmxremote.port=9990"
    JAVA_OPTS="\$JAVA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
    JAVA_OPTS="\$JAVA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
fi

## Flight Recorder configuration
if [[ "$ENABLE_FLIGHT_RECORDER" == "true" ]]; then
    JAVA_OPTS="\$JAVA_OPTS -XX:+FlightRecorder"
    JAVA_OPTS="\$JAVA_OPTS -XX:StartFlightRecording=duration=60s,filename=/opt/arka/logs/wildfly-flight-recorder.jfr"
fi

## GC Logging
if [[ "$ENABLE_GC_LOGGING" == "true" ]]; then
    JAVA_OPTS="\$JAVA_OPTS -Xlog:gc:/opt/arka/logs/wildfly-gc.log"
fi

## Deployment scanner optimization
JAVA_OPTS="\$JAVA_OPTS -Djboss.deployment.scanner.auto.deploy.enabled=false"

## Additional ARKA-specific settings
JAVA_OPTS="\$JAVA_OPTS -Darka.deployment.mode=$DEPLOYMENT_MODE"
JAVA_OPTS="\$JAVA_OPTS -Darka.jar.directory=$JAR_DIR"
JAVA_OPTS="\$JAVA_OPTS -Darka.log.directory=$LOG_DIR"

EOF
    
    echo -e "${GREEN}✅ WildFly standalone configuration updated${NC}"
}

# Function to create systemd service for WildFly
create_systemd_service() {
    echo -e "${YELLOW}🔧 Creating systemd service for WildFly...${NC}"
    
    local service_file="/etc/systemd/system/wildfly-arka.service"
    
    sudo tee "$service_file" > /dev/null << EOF
[Unit]
Description=WildFly Application Server for ARKA JAR Deployment
After=network.target
Wants=network.target

[Service]
Type=notify
User=wildfly
Group=wildfly
ExecStart=$WILDFLY_HOME/bin/standalone.sh -c $WILDFLY_CONFIG
ExecReload=/bin/kill -HUP \$MAINPID
ExecStop=/bin/kill -TERM \$MAINPID
NotifyAccess=all
TimeoutStartSec=300
TimeoutStopSec=120
Restart=always
RestartSec=30

# Environment variables
Environment=WILDFLY_HOME=$WILDFLY_HOME
Environment=JAVA_HOME=$JAVA_HOME
Environment=WILDFLY_CONFIG=$WILDFLY_CONFIG

# Working directory
WorkingDirectory=$WILDFLY_HOME

# Resource limits
LimitNOFILE=65536
LimitNPROC=4096

# Security settings
PrivateTmp=yes
ProtectHome=yes
ProtectSystem=strict
ReadWritePaths=$LOG_DIR $PID_DIR $JAR_DIR $BACKUP_DIR /tmp

[Install]
WantedBy=multi-user.target
EOF
    
    # Reload systemd and enable service
    sudo systemctl daemon-reload
    sudo systemctl enable wildfly-arka.service
    
    echo -e "${GREEN}✅ Systemd service created and enabled${NC}"
}

# Function to create port monitoring script
create_port_monitor() {
    echo -e "${YELLOW}🔍 Creating port monitoring script...${NC}"
    
    local monitor_script="/opt/arka/scripts/port-monitor.sh"
    
    cat > "$monitor_script" << 'EOF'
#!/bin/bash
# Port monitoring script for ARKA services

# Service ports to monitor
PORTS=(8888 8761 8085 8081 8082 8083 8080)

echo "Port Monitoring Report - $(date)"
echo "=================================="

for port in "${PORTS[@]}"; do
    if netstat -tuln | grep -q ":$port "; then
        service=$(lsof -ti:$port 2>/dev/null | head -1)
        if [[ -n "$service" ]]; then
            process_name=$(ps -p $service -o comm= 2>/dev/null)
            echo "✅ Port $port: ACTIVE ($process_name - PID: $service)"
        else
            echo "⚠️  Port $port: ACTIVE (unknown process)"
        fi
    else
        echo "❌ Port $port: FREE"
    fi
done

echo ""
echo "Network connections:"
netstat -tuln | grep -E ":(8888|8761|8085|8081|8082|8083|8080) "
EOF
    
    chmod +x "$monitor_script"
    echo -e "${GREEN}✅ Port monitor created: $monitor_script${NC}"
}

# Function to create log rotation configuration
create_log_rotation() {
    echo -e "${YELLOW}📝 Creating log rotation configuration...${NC}"
    
    local logrotate_config="/etc/logrotate.d/arka-jars"
    
    sudo tee "$logrotate_config" > /dev/null << EOF
$LOG_DIR/*.log {
    daily
    missingok
    rotate 30
    compress
    notifempty
    create 644 wildfly wildfly
    postrotate
        # Send SIGUSR1 to all java processes to reopen log files
        for pid in \$(pgrep java); do
            kill -USR1 \$pid 2>/dev/null || true
        done
    endscript
}

$WILDFLY_HOME/standalone/log/*.log {
    daily
    missingok
    rotate 15
    compress
    notifempty
    create 644 wildfly wildfly
}
EOF
    
    echo -e "${GREEN}✅ Log rotation configured${NC}"
}

# Function to create monitoring cron jobs
create_monitoring_cron() {
    echo -e "${YELLOW}⏰ Creating monitoring cron jobs...${NC}"
    
    local cron_script="/opt/arka/scripts/monitoring-cron.sh"
    
    cat > "$cron_script" << EOF
#!/bin/bash
# Monitoring cron job for ARKA JAR services

# Health check all services
/opt/arka/scripts/deploy-jars-wildfly.sh --action status >> /opt/arka/logs/health-check.log 2>&1

# Check disk space
df -h | grep -E "(/$|/opt)" >> /opt/arka/logs/disk-usage.log

# Check memory usage
free -m >> /opt/arka/logs/memory-usage.log

# Check CPU usage
top -bn1 | grep "Cpu(s)" >> /opt/arka/logs/cpu-usage.log

# Port monitoring
/opt/arka/scripts/port-monitor.sh >> /opt/arka/logs/port-monitor.log
EOF
    
    chmod +x "$cron_script"
    
    # Add to crontab (run every 5 minutes)
    (crontab -l 2>/dev/null; echo "*/5 * * * * /opt/arka/scripts/monitoring-cron.sh") | crontab -
    
    echo -e "${GREEN}✅ Monitoring cron jobs created${NC}"
}

# Function to set permissions
set_permissions() {
    echo -e "${YELLOW}🔒 Setting up permissions...${NC}"
    
    # Create wildfly user if not exists
    if ! id "wildfly" &>/dev/null; then
        sudo useradd -r -s /sbin/nologin -d $WILDFLY_HOME wildfly
        echo -e "${GREEN}✅ Created wildfly user${NC}"
    fi
    
    # Set ownership
    sudo chown -R wildfly:wildfly $WILDFLY_HOME
    sudo chown -R wildfly:wildfly /opt/arka
    
    # Set permissions
    find $WILDFLY_HOME -type d -exec chmod 755 {} \;
    find $WILDFLY_HOME -name "*.sh" -exec chmod +x {} \;
    find /opt/arka -type d -exec chmod 755 {} \;
    find /opt/arka -name "*.sh" -exec chmod +x {} \;
    
    echo -e "${GREEN}✅ Permissions configured${NC}"
}

# Function to validate configuration
validate_configuration() {
    echo -e "${CYAN}🔍 Validating WildFly JAR configuration...${NC}"
    
    local errors=0
    
    # Check directories
    local required_dirs=("$PID_DIR" "$LOG_DIR" "$JAR_DIR" "$BACKUP_DIR")
    for dir in "${required_dirs[@]}"; do
        if [[ ! -d "$dir" ]]; then
            echo -e "${RED}❌ Missing directory: $dir${NC}"
            ((errors++))
        else
            echo -e "${GREEN}✅ Directory exists: $dir${NC}"
        fi
    done
    
    # Check WildFly configuration
    if [[ -f "$WILDFLY_HOME/bin/standalone.conf" ]]; then
        echo -e "${GREEN}✅ WildFly configuration file exists${NC}"
    else
        echo -e "${RED}❌ Missing WildFly configuration${NC}"
        ((errors++))
    fi
    
    # Check systemd service
    if systemctl is-enabled wildfly-arka.service &>/dev/null; then
        echo -e "${GREEN}✅ Systemd service enabled${NC}"
    else
        echo -e "${RED}❌ Systemd service not enabled${NC}"
        ((errors++))
    fi
    
    # Check scripts
    local scripts=("/opt/arka/scripts/port-monitor.sh" "/opt/arka/scripts/monitoring-cron.sh")
    for script in "${scripts[@]}"; do
        if [[ -x "$script" ]]; then
            echo -e "${GREEN}✅ Script executable: $script${NC}"
        else
            echo -e "${RED}❌ Script not executable: $script${NC}"
            ((errors++))
        fi
    done
    
    if [[ $errors -eq 0 ]]; then
        echo -e "\n${GREEN}🎉 Configuration validation passed!${NC}"
        return 0
    else
        echo -e "\n${RED}❌ Configuration validation failed with $errors errors${NC}"
        return 1
    fi
}

# Function to cleanup configuration
cleanup_configuration() {
    echo -e "${YELLOW}🧹 Cleaning up WildFly JAR configuration...${NC}"
    
    # Stop services
    sudo systemctl stop wildfly-arka.service 2>/dev/null || true
    sudo systemctl disable wildfly-arka.service 2>/dev/null || true
    
    # Remove systemd service
    sudo rm -f /etc/systemd/system/wildfly-arka.service
    sudo systemctl daemon-reload
    
    # Remove monitoring cron
    crontab -l | grep -v "monitoring-cron.sh" | crontab -
    
    # Remove configurations (keep backups)
    rm -f /etc/logrotate.d/arka-jars
    
    echo -e "${GREEN}✅ Configuration cleaned up${NC}"
}

# Main execution
echo -e "${CYAN}🔧 WildFly JAR Configuration Manager${NC}"
echo -e "${WHITE}Action: $ACTION${NC}"
echo -e "${WHITE}WildFly Home: $WILDFLY_HOME${NC}"
echo "=============================================="

case "$ACTION" in
    "configure")
        if ! check_wildfly; then
            exit 1
        fi
        
        create_directories
        configure_wildfly_standalone
        create_systemd_service
        create_port_monitor
        create_log_rotation
        create_monitoring_cron
        set_permissions
        
        echo -e "\n${GREEN}🎉 WildFly JAR configuration completed!${NC}"
        echo -e "${YELLOW}💡 Next steps:${NC}"
        echo -e "   1. Start WildFly: ${WHITE}sudo systemctl start wildfly-arka${NC}"
        echo -e "   2. Deploy JARs: ${WHITE}./scripts/deploy-jars-wildfly.sh --action deploy${NC}"
        echo -e "   3. Check status: ${WHITE}./scripts/deploy-jars-wildfly.sh --action status${NC}"
        ;;
    "validate")
        validate_configuration
        ;;
    "cleanup")
        cleanup_configuration
        ;;
    *)
        echo -e "${RED}❌ Unknown action: $ACTION${NC}"
        echo -e "${YELLOW}Valid actions: configure, validate, cleanup${NC}"
        exit 1
        ;;
esac

echo -e "${GREEN}🎉 Operation completed!${NC}"