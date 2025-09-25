# 🚀 ARKA AWS Production Deployment
# Script optimizado para despliegue en AWS con perfil específico

param(
    [Parameter(Mandatory=$false)]
    [string]$Action = "deploy",
    
    [Parameter(Mandatory=$false)]
    [string]$Service = "all"
)

# AWS Profile Configuration
$Profile = "aws"
$Region = "us-east-1"

# Service Configuration for AWS
$services = @{
    "config-server" = @{
        "jar" = "config-server.jar"
        "port" = 8888
        "profile" = "native,aws"
        "order" = 1
        "healthPath" = "/actuator/health"
        "startupTime" = 30
    }
    "eureka-server" = @{
        "jar" = "eureka-server.jar"
        "port" = 8761
        "profile" = "aws"
        "order" = 2
        "healthPath" = "/actuator/health"
        "startupTime" = 30
    }
    "api-gateway" = @{
        "jar" = "api-gateway.jar"
        "port" = 8085
        "profile" = "aws"
        "order" = 3
        "healthPath" = "/actuator/health"
        "startupTime" = 45
    }
    "arca-cotizador" = @{
        "jar" = "arca-cotizador.jar"
        "port" = 8081
        "profile" = "aws"
        "order" = 4
        "healthPath" = "/actuator/health"
        "startupTime" = 45
    }
    "arca-gestor-solicitudes" = @{
        "jar" = "arca-gestor-solicitudes.jar"
        "port" = 8082
        "profile" = "aws"
        "order" = 5
        "healthPath" = "/actuator/health"
        "startupTime" = 45
    }
    "hello-world-service" = @{
        "jar" = "hello-world-service.jar"
        "port" = 8083
        "profile" = "aws"
        "order" = 6
        "healthPath" = "/actuator/health"
        "startupTime" = 30
    }
}

# Directories
$distDir = "dist/jars"
$logsDir = "logs/aws"
$pidsDir = "pids/aws"

# Create AWS specific directories
@($logsDir, $pidsDir) | ForEach-Object {
    if (!(Test-Path $_)) {
        New-Item -ItemType Directory -Path $_ -Force | Out-Null
    }
}

function Test-ServiceHealth {
    param($serviceName, $serviceConfig)
    
    $maxAttempts = 30
    $attempt = 0
    
    Write-Host "🏥 Health checking $serviceName..." -ForegroundColor Yellow
    
    while ($attempt -lt $maxAttempts) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$($serviceConfig.port)$($serviceConfig.healthPath)" -TimeoutSec 5 -UseBasicParsing
            if ($response.StatusCode -eq 200) {
                Write-Host "✅ $serviceName is healthy" -ForegroundColor Green
                return $true
            }
        } catch {
            # Service not ready yet
        }
        
        $attempt++
        Start-Sleep -Seconds 2
    }
    
    Write-Host "❌ $serviceName health check failed after $maxAttempts attempts" -ForegroundColor Red
    return $false
}

function Start-AWSService {
    param($serviceName, $serviceConfig)
    
    $jarPath = "$distDir/$($serviceConfig.jar)"
    $logPath = "$logsDir/$serviceName.log"
    $pidPath = "$pidsDir/$serviceName.pid"
    
    if (!(Test-Path $jarPath)) {
        Write-Host "❌ JAR not found: $jarPath" -ForegroundColor Red
        return $false
    }
    
    # Kill any existing process on this port
    try {
        $existingProcess = Get-NetTCPConnection -LocalPort $serviceConfig.port -ErrorAction SilentlyContinue
        if ($existingProcess) {
            $processId = (Get-Process -Id $existingProcess.OwningProcess -ErrorAction SilentlyContinue).Id
            if ($processId) {
                Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
                Start-Sleep -Seconds 3
            }
        }
    } catch {
        # Port might be free
    }
    
    Write-Host "🚀 Starting $serviceName (AWS Profile)..." -ForegroundColor Green
    
    # Java arguments optimized for AWS deployment
    $javaArgs = @(
        "-Xms512m"
        "-Xmx1024m"
        "-XX:+UseG1GC"
        "-XX:MaxGCPauseMillis=200"
        "-Dspring.profiles.active=$($serviceConfig.profile)"
        "-Dserver.port=$($serviceConfig.port)"
        "-Daws.region=$Region"
        "-Dlogging.file.name=$logPath"
        "-jar"
        $jarPath
    )
    
    # Start service
    $processInfo = New-Object System.Diagnostics.ProcessStartInfo
    $processInfo.FileName = "java"
    $processInfo.Arguments = $javaArgs -join " "
    $processInfo.UseShellExecute = $false
    $processInfo.CreateNoWindow = $true
    $processInfo.RedirectStandardOutput = $true
    $processInfo.RedirectStandardError = $true
    
    $process = [System.Diagnostics.Process]::Start($processInfo)
    
    if ($process) {
        # Save PID
        $process.Id | Out-File -FilePath $pidPath
        Write-Host "✅ $serviceName started with PID: $($process.Id)" -ForegroundColor Green
        
        # Wait for startup
        Write-Host "⏳ Waiting $($serviceConfig.startupTime) seconds for $serviceName to start..." -ForegroundColor Yellow
        Start-Sleep -Seconds $serviceConfig.startupTime
        
        # Health check
        if (Test-ServiceHealth $serviceName $serviceConfig) {
            return $true
        } else {
            Write-Host "❌ $serviceName failed health check" -ForegroundColor Red
            Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
            return $false
        }
    } else {
        Write-Host "❌ Failed to start $serviceName" -ForegroundColor Red
        return $false
    }
}

function Stop-AWSService {
    param($serviceName)
    
    $pidPath = "$pidsDir/$serviceName.pid"
    
    if (Test-Path $pidPath) {
        $pid = Get-Content $pidPath
        try {
            Stop-Process -Id $pid -Force
            Remove-Item $pidPath -Force
            Write-Host "🛑 Stopped $serviceName (PID: $pid)" -ForegroundColor Yellow
        } catch {
            Write-Host "⚠️ Process $pid for $serviceName not found" -ForegroundColor Yellow
            Remove-Item $pidPath -ErrorAction SilentlyContinue
        }
    }
}

function Deploy-AllServices {
    Write-Host "🏗️ Starting AWS Deployment..." -ForegroundColor Cyan
    
    $deployedServices = @()
    $failedServices = @()
    
    # Deploy services in order
    foreach ($serviceName in ($services.Keys | Sort-Object {$services[$_].order})) {
        $serviceConfig = $services[$serviceName]
        
        Write-Host "📦 Deploying $serviceName..." -ForegroundColor White
        
        if (Start-AWSService $serviceName $serviceConfig) {
            $deployedServices += $serviceName
            Write-Host "✅ $serviceName deployed successfully" -ForegroundColor Green
        } else {
            $failedServices += $serviceName
            Write-Host "❌ $serviceName deployment failed" -ForegroundColor Red
            break  # Stop deployment on first failure
        }
    }
    
    # Results
    Write-Host "`n📊 AWS Deployment Results:" -ForegroundColor Cyan
    Write-Host "✅ Deployed: $($deployedServices -join ', ')" -ForegroundColor Green
    
    if ($failedServices.Count -gt 0) {
        Write-Host "❌ Failed: $($failedServices -join ', ')" -ForegroundColor Red
        return $false
    }
    
    # Final health check
    Write-Host "`n🏥 Final health check..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    
    foreach ($serviceName in $deployedServices) {
        $serviceConfig = $services[$serviceName]
        Test-ServiceHealth $serviceName $serviceConfig | Out-Null
    }
    
    Write-Host "`n🎉 AWS Deployment completed successfully!" -ForegroundColor Green
    return $true
}

function Show-AWSStatus {
    Write-Host "📊 ARKA AWS Services Status:" -ForegroundColor Cyan
    Write-Host "Region: $Region | Profile: $Profile" -ForegroundColor White
    Write-Host "=============================================" -ForegroundColor Gray
    
    foreach ($serviceName in ($services.Keys | Sort-Object {$services[$_].order})) {
        $serviceConfig = $services[$serviceName]
        $pidPath = "$pidsDir/$serviceName.pid"
        
        if (Test-Path $pidPath) {
            $pid = Get-Content $pidPath
            try {
                $process = Get-Process -Id $pid -ErrorAction Stop
                $healthStatus = if (Test-ServiceHealth $serviceName $serviceConfig) { "HEALTHY" } else { "UNHEALTHY" }
                $color = if ($healthStatus -eq "HEALTHY") { "Green" } else { "Yellow" }
                Write-Host "  ✅ $serviceName`:$($serviceConfig.port) - RUNNING ($healthStatus)" -ForegroundColor $color
            } catch {
                Write-Host "  ❌ $serviceName`:$($serviceConfig.port) - STOPPED" -ForegroundColor Red
                Remove-Item $pidPath -ErrorAction SilentlyContinue
            }
        } else {
            Write-Host "  ❌ $serviceName`:$($serviceConfig.port) - STOPPED" -ForegroundColor Red
        }
    }
    
    # Show endpoints
    Write-Host "`n🔗 Service Endpoints:" -ForegroundColor Cyan
    Write-Host "  📊 Eureka Dashboard: http://localhost:8761" -ForegroundColor White
    Write-Host "  🚪 API Gateway: http://localhost:8085" -ForegroundColor White
    Write-Host "  ⚙️  Config Server: http://localhost:8888" -ForegroundColor White
}

# Main execution
Write-Host "🌟 ARKA AWS Production Manager" -ForegroundColor Cyan
Write-Host "Action: $Action | Service: $Service" -ForegroundColor White
Write-Host "Region: $Region | Profile: $Profile" -ForegroundColor White
Write-Host "============================================" -ForegroundColor Gray

switch ($Action.ToLower()) {
    "deploy" {
        # Stop all services first
        Write-Host "🛑 Stopping existing services..." -ForegroundColor Yellow
        foreach ($serviceName in ($services.Keys | Sort-Object {$services[$_].order} -Descending)) {
            Stop-AWSService $serviceName
        }
        Start-Sleep -Seconds 5
        
        # Deploy all services
        if (Deploy-AllServices) {
            exit 0
        } else {
            exit 1
        }
    }
    "stop" {
        Write-Host "🛑 Stopping AWS services..." -ForegroundColor Yellow
        foreach ($serviceName in ($services.Keys | Sort-Object {$services[$_].order} -Descending)) {
            Stop-AWSService $serviceName
        }
    }
    "status" {
        Show-AWSStatus
    }
    default {
        Write-Host "❌ Unknown action: $Action" -ForegroundColor Red
        Write-Host "Valid actions: deploy, stop, status" -ForegroundColor Yellow
        exit 1
    }
}

Write-Host "🎉 Operation completed!" -ForegroundColor Green