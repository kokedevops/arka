# 🐺 WildFly Deployment Configuration for ARKA
# Script para configurar y desplegar en WildFly Application Server

param(
    [Parameter(Mandatory=$false)]
    [string]$WildFlyHome = "C:\wildfly\wildfly-31.0.0.Final",
    
    [Parameter(Mandatory=$false)]
    [string]$Action = "deploy",
    
    [Parameter(Mandatory=$false)]
    [string]$Profile = "aws"
)

# WildFly Configuration
$WildFlyBin = "$WildFlyHome\bin"
$WildFlyDeployments = "$WildFlyHome\standalone\deployments"
$WildFlyConfig = "$WildFlyHome\standalone\configuration"

# Services that can be deployed to WildFly (excluding infrastructure services)
$wildflyServices = @{
    "arca-cotizador" = @{
        "war" = "arca-cotizador.war"
        "context" = "/cotizador"
        "port" = 8081
    }
    "arca-gestor-solicitudes" = @{
        "war" = "arca-gestor-solicitudes.war"
        "context" = "/gestor"
        "port" = 8082
    }
    "api-gateway" = @{
        "war" = "api-gateway.war"
        "context" = "/gateway"
        "port" = 8085
    }
}

# Infrastructure services (run as standalone JARs)
$infraServices = @{
    "config-server" = @{
        "jar" = "config-server.jar"
        "port" = 8888
        "profile" = "native,$Profile"
    }
    "eureka-server" = @{
        "jar" = "eureka-server.jar"
        "port" = 8761
        "profile" = $Profile
    }
}

function Test-WildFlyInstallation {
    if (!(Test-Path $WildFlyHome)) {
        Write-Host "❌ WildFly not found at: $WildFlyHome" -ForegroundColor Red
        Write-Host "Please install WildFly or update the WildFlyHome parameter" -ForegroundColor Yellow
        return $false
    }
    
    if (!(Test-Path "$WildFlyBin\jboss-cli.bat")) {
        Write-Host "❌ WildFly CLI not found" -ForegroundColor Red
        return $false
    }
    
    Write-Host "✅ WildFly installation verified: $WildFlyHome" -ForegroundColor Green
    return $true
}

function Start-WildFly {
    Write-Host "🐺 Starting WildFly Application Server..." -ForegroundColor Green
    
    # Start WildFly in background
    $wildflyProcess = Start-Process -FilePath "$WildFlyBin\standalone.bat" -ArgumentList "-c standalone-full.xml" -PassThru -WindowStyle Minimized
    
    # Wait for WildFly to start
    Write-Host "⏳ Waiting for WildFly to start..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
    
    # Check if WildFly is running
    $maxAttempts = 20
    $attempt = 0
    
    while ($attempt -lt $maxAttempts) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8080" -TimeoutSec 5 -UseBasicParsing
            if ($response.StatusCode -eq 200) {
                Write-Host "✅ WildFly is running" -ForegroundColor Green
                return $wildflyProcess
            }
        } catch {
            # WildFly not ready yet
        }
        
        $attempt++
        Start-Sleep -Seconds 3
    }
    
    Write-Host "❌ WildFly failed to start" -ForegroundColor Red
    return $null
}

function Deploy-ToWildFly {
    param($serviceName, $serviceConfig)
    
    $warPath = "dist\wars\$($serviceConfig.war)"
    
    if (!(Test-Path $warPath)) {
        Write-Host "❌ WAR not found: $warPath" -ForegroundColor Red
        return $false
    }
    
    Write-Host "📦 Deploying $serviceName to WildFly..." -ForegroundColor Yellow
    
    # Copy WAR to deployments directory
    Copy-Item $warPath $WildFlyDeployments -Force
    
    # Wait for deployment
    $deploymentMarker = "$WildFlyDeployments\$($serviceConfig.war).deployed"
    $maxWait = 60
    $waited = 0
    
    while (!(Test-Path $deploymentMarker) -and $waited -lt $maxWait) {
        Start-Sleep -Seconds 2
        $waited += 2
    }
    
    if (Test-Path $deploymentMarker) {
        Write-Host "✅ $serviceName deployed successfully" -ForegroundColor Green
        return $true
    } else {
        Write-Host "❌ $serviceName deployment failed" -ForegroundColor Red
        return $false
    }
}

function Start-InfraService {
    param($serviceName, $serviceConfig)
    
    $jarPath = "dist\jars\$($serviceConfig.jar)"
    
    if (!(Test-Path $jarPath)) {
        Write-Host "❌ JAR not found: $jarPath" -ForegroundColor Red
        return $false
    }
    
    Write-Host "🚀 Starting $serviceName..." -ForegroundColor Green
    
    $javaArgs = @(
        "-Xms256m"
        "-Xmx512m"
        "-Dspring.profiles.active=$($serviceConfig.profile)"
        "-Dserver.port=$($serviceConfig.port)"
        "-jar"
        $jarPath
    )
    
    $process = Start-Process -FilePath "java" -ArgumentList $javaArgs -PassThru -WindowStyle Minimized
    
    # Save PID for later management
    $process.Id | Out-File -FilePath "pids\$serviceName.pid"
    
    Write-Host "✅ $serviceName started with PID: $($process.Id)" -ForegroundColor Green
    return $true
}

function Build-WARs {
    Write-Host "🔨 Building WAR files for WildFly deployment..." -ForegroundColor Yellow
    
    # Create WAR distribution directory
    if (!(Test-Path "dist\wars")) {
        New-Item -ItemType Directory -Path "dist\wars" -Force | Out-Null
    }
    
    # Build WARs (modify build.gradle files to support WAR packaging)
    foreach ($serviceName in $wildflyServices.Keys) {
        Write-Host "📦 Building $serviceName.war..." -ForegroundColor White
        
        # Build the WAR
        & gradlew ":$serviceName`:bootWar"
        
        $warSource = "$serviceName\build\libs\$serviceName.war"
        $warDest = "dist\wars\$serviceName.war"
        
        if (Test-Path $warSource) {
            Copy-Item $warSource $warDest -Force
            Write-Host "✅ $serviceName.war created" -ForegroundColor Green
        } else {
            Write-Host "❌ Failed to build $serviceName.war" -ForegroundColor Red
            return $false
        }
    }
    
    return $true
}

function Show-WildFlyStatus {
    Write-Host "📊 WildFly Deployment Status:" -ForegroundColor Cyan
    Write-Host "WildFly Home: $WildFlyHome" -ForegroundColor White
    Write-Host "Profile: $Profile" -ForegroundColor White
    Write-Host "==========================================" -ForegroundColor Gray
    
    # Check WildFly status
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080" -TimeoutSec 5 -UseBasicParsing
        Write-Host "🐺 WildFly Server: RUNNING (http://localhost:8080)" -ForegroundColor Green
    } catch {
        Write-Host "🐺 WildFly Server: STOPPED" -ForegroundColor Red
    }
    
    # Check deployed applications
    Write-Host "`n📦 Deployed Applications:" -ForegroundColor Cyan
    foreach ($serviceName in $wildflyServices.Keys) {
        $serviceConfig = $wildflyServices[$serviceName]
        $deploymentFile = "$WildFlyDeployments\$($serviceConfig.war)"
        $deploymentMarker = "$deploymentFile.deployed"
        
        if (Test-Path $deploymentMarker) {
            Write-Host "  ✅ $serviceName - DEPLOYED (http://localhost:8080$($serviceConfig.context))" -ForegroundColor Green
        } elseif (Test-Path $deploymentFile) {
            Write-Host "  ⏳ $serviceName - DEPLOYING" -ForegroundColor Yellow
        } else {
            Write-Host "  ❌ $serviceName - NOT DEPLOYED" -ForegroundColor Red
        }
    }
    
    # Check infrastructure services
    Write-Host "`n🏗️ Infrastructure Services:" -ForegroundColor Cyan
    foreach ($serviceName in $infraServices.Keys) {
        $serviceConfig = $infraServices[$serviceName]
        $pidFile = "pids\$serviceName.pid"
        
        if (Test-Path $pidFile) {
            $pid = Get-Content $pidFile
            try {
                $process = Get-Process -Id $pid -ErrorAction Stop
                Write-Host "  ✅ $serviceName - RUNNING (http://localhost:$($serviceConfig.port))" -ForegroundColor Green
            } catch {
                Write-Host "  ❌ $serviceName - STOPPED" -ForegroundColor Red
                Remove-Item $pidFile -ErrorAction SilentlyContinue
            }
        } else {
            Write-Host "  ❌ $serviceName - STOPPED" -ForegroundColor Red
        }
    }
}

# Main execution
Write-Host "🐺 WildFly Deployment Manager" -ForegroundColor Cyan
Write-Host "Action: $Action | Profile: $Profile" -ForegroundColor White
Write-Host "WildFly Home: $WildFlyHome" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Gray

if (!(Test-WildFlyInstallation)) {
    exit 1
}

# Create necessary directories
@("dist\wars", "dist\jars", "pids", "logs") | ForEach-Object {
    if (!(Test-Path $_)) {
        New-Item -ItemType Directory -Path $_ -Force | Out-Null
    }
}

switch ($Action.ToLower()) {
    "deploy" {
        Write-Host "🚀 Starting WildFly deployment process..." -ForegroundColor Green
        
        # Build WARs and JARs
        Write-Host "🔨 Building artifacts..." -ForegroundColor Yellow
        & gradlew clean bootJar
        
        if (!(Build-WARs)) {
            Write-Host "❌ WAR build failed" -ForegroundColor Red
            exit 1
        }
        
        # Copy JARs for infrastructure services
        Copy-Item "config-server\build\libs\config-server.jar" "dist\jars\" -Force
        Copy-Item "eureka-server\build\libs\eureka-server.jar" "dist\jars\" -Force
        
        # Start infrastructure services first
        Write-Host "🏗️ Starting infrastructure services..." -ForegroundColor Yellow
        foreach ($serviceName in $infraServices.Keys) {
            Start-InfraService $serviceName $infraServices[$serviceName]
            Start-Sleep -Seconds 10
        }
        
        # Start WildFly
        $wildflyProcess = Start-WildFly
        if (!$wildflyProcess) {
            Write-Host "❌ Failed to start WildFly" -ForegroundColor Red
            exit 1
        }
        
        # Deploy applications to WildFly
        Write-Host "📦 Deploying applications to WildFly..." -ForegroundColor Yellow
        foreach ($serviceName in $wildflyServices.Keys) {
            if (!(Deploy-ToWildFly $serviceName $wildflyServices[$serviceName])) {
                Write-Host "❌ Failed to deploy $serviceName" -ForegroundColor Red
                exit 1
            }
        }
        
        Write-Host "🎉 WildFly deployment completed!" -ForegroundColor Green
    }
    
    "status" {
        Show-WildFlyStatus
    }
    
    "stop" {
        Write-Host "🛑 Stopping WildFly and services..." -ForegroundColor Yellow
        
        # Stop infrastructure services
        foreach ($serviceName in $infraServices.Keys) {
            $pidFile = "pids\$serviceName.pid"
            if (Test-Path $pidFile) {
                $pid = Get-Content $pidFile
                try {
                    Stop-Process -Id $pid -Force
                    Remove-Item $pidFile
                    Write-Host "🛑 Stopped $serviceName" -ForegroundColor Yellow
                } catch {
                    Write-Host "⚠️ Could not stop $serviceName" -ForegroundColor Yellow
                }
            }
        }
        
        # Stop WildFly
        try {
            & "$WildFlyBin\jboss-cli.bat" --connect command=:shutdown
            Write-Host "🛑 WildFly stopped" -ForegroundColor Yellow
        } catch {
            Write-Host "⚠️ Could not gracefully stop WildFly" -ForegroundColor Yellow
        }
    }
    
    default {
        Write-Host "❌ Unknown action: $Action" -ForegroundColor Red
        Write-Host "Valid actions: deploy, stop, status" -ForegroundColor Yellow
        exit 1
    }
}

Write-Host "🎉 Operation completed!" -ForegroundColor Green