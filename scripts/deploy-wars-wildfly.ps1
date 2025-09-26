# 🚀 WildFly WAR Deployment Script for ARKA Microservices - Windows Version
# Versión: 2.0 - WAR Deployment Support

param(
    [string]$Action = "full",
    [string]$Service = "",
    [string]$Profile = "aws",
    [switch]$SkipBuild,
    [switch]$SkipTests,
    [switch]$Force,
    [switch]$Help
)

# =============================================================================
# CONFIGURACIÓN
# =============================================================================

$ErrorActionPreference = "Stop"

$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_DIR

# WildFly Configuration
$WILDFLY_HOME = if ($env:WILDFLY_HOME) { $env:WILDFLY_HOME } else { "C:\wildfly" }
$WILDFLY_CLI = "$WILDFLY_HOME\bin\jboss-cli.bat"
$WILDFLY_DEPLOYMENTS = "$WILDFLY_HOME\standalone\deployments"

# Build Configuration
$GRADLE_OPTS = if ($env:GRADLE_OPTS) { $env:GRADLE_OPTS } else { "-Xmx2g -Dorg.gradle.daemon=false" }
$SPRING_PROFILE = $Profile

# Microservices Configuration
$MICROSERVICES = @("eureka-server", "config-server", "api-gateway", "arca-cotizador", "arca-gestor-solicitudes")
$BUSINESS_SERVICES = @("api-gateway", "arca-cotizador", "arca-gestor-solicitudes")

# Health Check Configuration
$HEALTH_CHECK_TIMEOUT = 180
$HEALTH_CHECK_INTERVAL = 10

# =============================================================================
# FUNCIONES DE UTILIDAD
# =============================================================================

function Write-InfoLog {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-SuccessLog {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-WarningLog {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-ErrorLog {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Show-Banner {
    Write-Host "==================================================" -ForegroundColor Blue
    Write-Host "🚀 ARKA WildFly WAR Deployment Script v2.0" -ForegroundColor Blue
    Write-Host "==================================================" -ForegroundColor Blue
}

function Show-Usage {
    Write-Host "Usage: deploy-wars-wildfly.ps1 [OPTIONS]"
    Write-Host ""
    Write-Host "OPTIONS:"
    Write-Host "  -Action ACTION      Action to perform: build|deploy|undeploy|redeploy|health|full"
    Write-Host "  -Service SERVICE    Specific service to deploy (optional)"
    Write-Host "  -Profile PROFILE    Spring profile to use (default: aws)"
    Write-Host "  -SkipBuild          Skip build phase"
    Write-Host "  -SkipTests          Skip tests during build"
    Write-Host "  -Force              Force deployment even if health checks fail"
    Write-Host "  -Help               Show this help message"
    Write-Host ""
    Write-Host "ACTIONS:"
    Write-Host "  build     - Build WAR files only"
    Write-Host "  deploy    - Deploy WARs to WildFly"
    Write-Host "  undeploy  - Undeploy services from WildFly"
    Write-Host "  redeploy  - Undeploy and deploy services"
    Write-Host "  health    - Perform health checks only"
    Write-Host "  full      - Build, deploy and health check (default)"
    Write-Host ""
    Write-Host "EXAMPLES:"
    Write-Host "  .\deploy-wars-wildfly.ps1 -Action full"
    Write-Host "  .\deploy-wars-wildfly.ps1 -Action deploy -Service api-gateway"
    Write-Host "  .\deploy-wars-wildfly.ps1 -Action redeploy -Profile dev -SkipTests"
}

# =============================================================================
# FUNCIONES DE VALIDACIÓN
# =============================================================================

function Test-Requirements {
    Write-InfoLog "Validando requisitos del sistema..."
    
    # Check Java
    try {
        $javaVersion = & java -version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Java no encontrado"
        }
        Write-SuccessLog "Java encontrado"
    }
    catch {
        Write-ErrorLog "Java no encontrado. Instala Java 21."
        exit 1
    }
    
    # Check WildFly
    if (-not (Test-Path $WILDFLY_HOME)) {
        Write-ErrorLog "WildFly no encontrado en: $WILDFLY_HOME"
        Write-ErrorLog "Configura `$env:WILDFLY_HOME o instala WildFly"
        exit 1
    }
    
    # Check WildFly CLI
    if (-not (Test-Path $WILDFLY_CLI)) {
        Write-ErrorLog "WildFly CLI no encontrado: $WILDFLY_CLI"
        exit 1
    }
    
    # Check Gradle
    if (-not (Test-Path "$PROJECT_ROOT\gradlew.bat")) {
        Write-ErrorLog "Gradle wrapper no encontrado en: $PROJECT_ROOT"
        exit 1
    }
    
    Write-SuccessLog "Todos los requisitos validados correctamente"
}

function Test-WildFlyStatus {
    Write-InfoLog "Verificando estado de WildFly..."
    
    $wildflyProcess = Get-Process -Name "java" -ErrorAction SilentlyContinue | 
                      Where-Object { $_.CommandLine -like "*jboss*" }
    
    if (-not $wildflyProcess) {
        Write-WarningLog "WildFly no está ejecutándose"
        
        Write-InfoLog "Intentando iniciar WildFly..."
        $wildflyStandalone = "$WILDFLY_HOME\bin\standalone.bat"
        if (Test-Path $wildflyStandalone) {
            Start-Process -FilePath $wildflyStandalone -ArgumentList "-b", "0.0.0.0" -WindowStyle Hidden
            Start-Sleep -Seconds 10
        }
        else {
            Write-ErrorLog "No se pudo encontrar standalone.bat"
            return $false
        }
    }
    
    # Verificar que WildFly responda
    $retries = 0
    $maxRetries = 30
    
    while ($retries -lt $maxRetries) {
        try {
            $result = & $WILDFLY_CLI --connect --command=":whoami" 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-SuccessLog "WildFly está ejecutándose y respondiendo"
                return $true
            }
        }
        catch {
            # Continue trying
        }
        
        $retries++
        Write-InfoLog "Esperando conexión a WildFly... ($retries/$maxRetries)"
        Start-Sleep -Seconds 2
    }
    
    Write-ErrorLog "No se pudo conectar a WildFly después de $maxRetries intentos"
    return $false
}

# =============================================================================
# FUNCIONES DE BUILD
# =============================================================================

function Build-Wars {
    Write-InfoLog "🔨 Iniciando construcción de WARs..."
    
    Set-Location $PROJECT_ROOT
    
    $gradleCmd = ".\gradlew.bat"
    $buildTasks = "clean"
    
    # Agregar tasks de build para servicios de negocio
    foreach ($service in $BUSINESS_SERVICES) {
        $buildTasks += " :${service}:bootWar"
    }
    
    # Agregar tasks de build para servicios de infraestructura (JARs)
    $buildTasks += " :eureka-server:bootJar :config-server:bootJar"
    
    if ($SkipTests) {
        $buildTasks += " -x test"
        Write-WarningLog "Omitiendo tests por parámetro -SkipTests"
    }
    
    Write-InfoLog "Ejecutando: $gradleCmd $buildTasks"
    
    $env:GRADLE_OPTS = $GRADLE_OPTS
    
    try {
        Invoke-Expression "$gradleCmd $buildTasks"
        if ($LASTEXITCODE -ne 0) {
            throw "Build failed with exit code $LASTEXITCODE"
        }
    }
    catch {
        Write-ErrorLog "Error en la construcción de WARs: $_"
        exit 1
    }
    
    # Verificar que los WARs se generaron correctamente
    foreach ($service in $BUSINESS_SERVICES) {
        $warFile = "$PROJECT_ROOT\$service\build\libs\$service.war"
        if (-not (Test-Path $warFile)) {
            Write-ErrorLog "WAR no encontrado: $warFile"
            exit 1
        }
        Write-SuccessLog "✅ WAR generado: $(Split-Path $warFile -Leaf)"
    }
    
    Write-SuccessLog "🎉 Construcción de WARs completada exitosamente"
}

# =============================================================================
# FUNCIONES DE DEPLOYMENT
# =============================================================================

function Set-WildFlyConfiguration {
    Write-InfoLog "🔧 Configurando WildFly para ARKA microservicios..."
    
    $configFile = "$SCRIPT_DIR\wildfly-arka-config.cli"
    
    if (-not (Test-Path $configFile)) {
        Write-ErrorLog "Archivo de configuración no encontrado: $configFile"
        return $false
    }
    
    try {
        & $WILDFLY_CLI --connect --file=$configFile
        if ($LASTEXITCODE -ne 0) {
            Write-WarningLog "Error aplicando configuración CLI - continuando..."
        }
    }
    catch {
        Write-WarningLog "Error aplicando configuración CLI - continuando..."
    }
    
    Write-SuccessLog "Configuración de WildFly aplicada"
    return $true
}

function Deploy-InfrastructureServices {
    Write-InfoLog "📦 Desplegando servicios de infraestructura (JAR mode)..."
    
    # Crear directorio de logs si no existe
    $logsDir = "$PROJECT_ROOT\logs"
    if (-not (Test-Path $logsDir)) {
        New-Item -ItemType Directory -Path $logsDir -Force | Out-Null
    }
    
    foreach ($service in @("eureka-server", "config-server")) {
        $jarFile = "$PROJECT_ROOT\$service\build\libs\$service.jar"
        
        if (-not (Test-Path $jarFile)) {
            Write-WarningLog "JAR no encontrado para $service : $jarFile"
            continue
        }
        
        Write-InfoLog "Iniciando $service como proceso independiente..."
        
        # Matar proceso existente si existe
        Get-Process -Name "java" -ErrorAction SilentlyContinue | 
            Where-Object { $_.CommandLine -like "*$service.jar*" } | 
            Stop-Process -Force -ErrorAction SilentlyContinue
        
        Start-Sleep -Seconds 2
        
        # Iniciar nuevo proceso
        $logFile = "$PROJECT_ROOT\logs\$service.log"
        $processArgs = @(
            "-jar", $jarFile,
            "--spring.profiles.active=$SPRING_PROFILE"
        )
        
        $process = Start-Process -FilePath "java" -ArgumentList $processArgs -RedirectStandardOutput $logFile -RedirectStandardError $logFile -PassThru -WindowStyle Hidden
        
        $process.Id | Out-File "$PROJECT_ROOT\logs\$service.pid" -Encoding UTF8
        
        Write-SuccessLog "✅ $service iniciado (PID: $($process.Id))"
    }
}

function Deploy-BusinessServices {
    Write-InfoLog "🚀 Desplegando microservicios de negocio en WildFly..."
    
    foreach ($service in $BUSINESS_SERVICES) {
        if ($Service -and $service -ne $Service) {
            Write-InfoLog "Omitiendo $service (solo desplegando $Service)"
            continue
        }
        
        Deploy-SingleService $service
    }
    
    Write-SuccessLog "✅ Deployment de microservicios completado"
}

function Deploy-SingleService {
    param([string]$ServiceName)
    
    $warFile = "$PROJECT_ROOT\$ServiceName\build\libs\$ServiceName.war"
    
    if (-not (Test-Path $warFile)) {
        Write-ErrorLog "WAR no encontrado para $ServiceName : $warFile"
        return $false
    }
    
    Write-InfoLog "📦 Desplegando $ServiceName..."
    
    # Undeploy si ya existe
    try {
        & $WILDFLY_CLI --connect --command="deployment-info --name=$ServiceName.war" 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-InfoLog "Removiendo deployment existente de $ServiceName..."
            & $WILDFLY_CLI --connect --command="undeploy $ServiceName.war"
        }
    }
    catch {
        # Service not deployed, continue
    }
    
    # Deploy new WAR
    try {
        & $WILDFLY_CLI --connect --command="deploy $warFile"
        if ($LASTEXITCODE -ne 0) {
            Write-ErrorLog "Error desplegando $ServiceName"
            return $false
        }
    }
    catch {
        Write-ErrorLog "Error desplegando $ServiceName : $_"
        return $false
    }
    
    Write-SuccessLog "✅ $ServiceName desplegado correctamente"
    return $true
}

function Remove-Services {
    Write-InfoLog "🗑️  Removiendo deployments existentes..."
    
    foreach ($service in $BUSINESS_SERVICES) {
        if ($Service -and $service -ne $Service) {
            continue
        }
        
        try {
            & $WILDFLY_CLI --connect --command="deployment-info --name=$service.war" 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-InfoLog "Removiendo $service..."
                & $WILDFLY_CLI --connect --command="undeploy $service.war"
                Write-SuccessLog "✅ $service removido"
            }
            else {
                Write-InfoLog "$service no estaba desplegado"
            }
        }
        catch {
            Write-InfoLog "$service no estaba desplegado"
        }
    }
    
    # Stop infrastructure services
    foreach ($service in @("eureka-server", "config-server")) {
        $pidFile = "$PROJECT_ROOT\logs\$service.pid"
        if (Test-Path $pidFile) {
            $pid = Get-Content $pidFile -ErrorAction SilentlyContinue
            if ($pid) {
                $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
                if ($process) {
                    Write-InfoLog "Deteniendo $service (PID: $pid)..."
                    $process | Stop-Process -Force
                    Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
                    Write-SuccessLog "✅ $service detenido"
                }
            }
        }
        
        # Fallback: kill by process name
        Get-Process -Name "java" -ErrorAction SilentlyContinue | 
            Where-Object { $_.CommandLine -like "*$service.jar*" } | 
            Stop-Process -Force -ErrorAction SilentlyContinue
    }
}

# =============================================================================
# FUNCIONES DE HEALTH CHECK
# =============================================================================

function Test-ServicesHealth {
    Write-InfoLog "🏥 Realizando health checks..."
    
    # Create logs directory if not exists
    $logsDir = "$PROJECT_ROOT\logs"
    if (-not (Test-Path $logsDir)) {
        New-Item -ItemType Directory -Path $logsDir -Force | Out-Null
    }
    
    # Wait for services to start
    Write-InfoLog "Esperando que los servicios inicien..."
    Start-Sleep -Seconds 20
    
    $allHealthy = $true
    
    # Check infrastructure services
    if (-not (Test-ServiceHealth "eureka-server" "8761" "/actuator/health")) { $allHealthy = $false }
    if (-not (Test-ServiceHealth "config-server" "8888" "/actuator/health")) { $allHealthy = $false }
    
    # Check business services (deployed in WildFly)
    foreach ($service in $BUSINESS_SERVICES) {
        $port = switch ($service) {
            "api-gateway" { "8080" }
            "arca-cotizador" { "8080" }
            "arca-gestor-solicitudes" { "8080" }
            default { "8080" }
        }
        
        if (-not (Test-ServiceHealth $service $port "/actuator/health")) { $allHealthy = $false }
    }
    
    if ($allHealthy) {
        Write-SuccessLog "🎉 Todos los health checks pasaron exitosamente"
        return $true
    }
    else {
        Write-ErrorLog "❌ Algunos health checks fallaron"
        return $false
    }
}

function Test-ServiceHealth {
    param(
        [string]$ServiceName,
        [string]$Port,
        [string]$Endpoint
    )
    
    $url = "http://localhost:$Port$Endpoint"
    
    Write-InfoLog "Verificando health de $ServiceName en $url..."
    
    $retries = 0
    $maxRetries = [math]::Floor($HEALTH_CHECK_TIMEOUT / $HEALTH_CHECK_INTERVAL)
    
    while ($retries -lt $maxRetries) {
        try {
            $response = Invoke-WebRequest -Uri $url -TimeoutSec 5 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-SuccessLog "✅ $ServiceName está saludable"
                return $true
            }
        }
        catch {
            # Continue trying
        }
        
        $retries++
        Write-InfoLog "Health check $ServiceName... ($retries/$maxRetries)"
        Start-Sleep -Seconds $HEALTH_CHECK_INTERVAL
    }
    
    Write-ErrorLog "❌ $ServiceName health check falló después de ${HEALTH_CHECK_TIMEOUT}s"
    
    # Show service logs for debugging
    $logFile = "$PROJECT_ROOT\logs\$ServiceName.log"
    if (Test-Path $logFile) {
        Write-WarningLog "Últimas líneas del log de $ServiceName :"
        Get-Content $logFile -Tail 20 | ForEach-Object { Write-Host "  $_" }
    }
    
    return $false
}

# =============================================================================
# FUNCIÓN PRINCIPAL
# =============================================================================

function Main {
    if ($Help) {
        Show-Usage
        exit 0
    }
    
    Show-Banner
    
    Write-InfoLog "Configuración:"
    Write-InfoLog "  - Acción: $Action"
    Write-InfoLog "  - Perfil Spring: $SPRING_PROFILE"
    Write-InfoLog "  - WildFly Home: $WILDFLY_HOME"
    if ($Service) { Write-InfoLog "  - Servicio específico: $Service" }
    if ($SkipBuild) { Write-InfoLog "  - Omitir build: Sí" }
    if ($SkipTests) { Write-InfoLog "  - Omitir tests: Sí" }
    Write-Host ""
    
    # Validar requisitos
    Test-Requirements
    
    # Ejecutar acción
    switch ($Action.ToLower()) {
        "build" {
            Build-Wars
        }
        "deploy" {
            if (-not (Test-WildFlyStatus)) { exit 1 }
            Set-WildFlyConfiguration
            if (-not $SkipBuild) {
                Build-Wars
            }
            Deploy-InfrastructureServices
            Deploy-BusinessServices
        }
        "undeploy" {
            if (-not (Test-WildFlyStatus)) { exit 1 }
            Remove-Services
        }
        "redeploy" {
            if (-not (Test-WildFlyStatus)) { exit 1 }
            Remove-Services
            Start-Sleep -Seconds 5
            Set-WildFlyConfiguration
            if (-not $SkipBuild) {
                Build-Wars
            }
            Deploy-InfrastructureServices
            Deploy-BusinessServices
        }
        "health" {
            if (-not (Test-ServicesHealth)) { exit 1 }
        }
        "full" {
            if (-not (Test-WildFlyStatus)) { exit 1 }
            Set-WildFlyConfiguration
            if (-not $SkipBuild) {
                Build-Wars
            }
            Remove-Services
            Start-Sleep -Seconds 5
            Deploy-InfrastructureServices
            Deploy-BusinessServices
            
            if (-not $Force) {
                if (-not (Test-ServicesHealth)) { exit 1 }
            }
        }
        default {
            Write-ErrorLog "Acción desconocida: $Action"
            Show-Usage
            exit 1
        }
    }
    
    Write-SuccessLog "🎉 Operación '$Action' completada exitosamente!"
}

# Ejecutar función principal
Main