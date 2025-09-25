# 🚀 ARKA Production Deployment Script
# Script para ejecutar todos los servicios usando JARs en lugar de bootRun

param(
    [Parameter(Mandatory=$false)]
    [string]$Profile = "aws",
    
    [Parameter(Mandatory=$false)]
    [string]$Action = "start",
    
    [Parameter(Mandatory=$false)]
    [string]$Service = "all"
)

# Configuración de servicios y sus puertos
$services = @{
    "config-server" = @{
        "jar" = "config-server.jar"
        "port" = 8888
        "profile" = "native,$Profile"
        "order" = 1
    }
    "eureka-server" = @{
        "jar" = "eureka-server.jar"
        "port" = 8761
        "profile" = $Profile
        "order" = 2
    }
    "api-gateway" = @{
        "jar" = "api-gateway.jar"
        "port" = 8085
        "profile" = $Profile
        "order" = 3
    }
    "arca-cotizador" = @{
        "jar" = "arca-cotizador.jar"
        "port" = 8081
        "profile" = $Profile
        "order" = 4
    }
    "arca-gestor-solicitudes" = @{
        "jar" = "arca-gestor-solicitudes.jar"
        "port" = 8082
        "profile" = $Profile
        "order" = 5
    }
    "hello-world-service" = @{
        "jar" = "hello-world-service.jar"
        "port" = 8083
        "profile" = $Profile
        "order" = 6
    }
}

$distDir = "dist/jars"
$logsDir = "logs"
$pidsDir = "pids"

# Crear directorios necesarios
@($logsDir, $pidsDir) | ForEach-Object {
    if (!(Test-Path $_)) {
        New-Item -ItemType Directory -Path $_ -Force
    }
}

function Start-Service {
    param($serviceName, $serviceConfig)
    
    $jarPath = "$distDir/$($serviceConfig.jar)"
    $logPath = "$logsDir/$serviceName.log"
    $pidPath = "$pidsDir/$serviceName.pid"
    
    if (!(Test-Path $jarPath)) {
        Write-Host "❌ JAR not found: $jarPath" -ForegroundColor Red
        return $false
    }
    
    # Verificar si el puerto está libre
    $portInUse = Get-NetTCPConnection -LocalPort $serviceConfig.port -ErrorAction SilentlyContinue
    if ($portInUse) {
        Write-Host "⚠️  Port $($serviceConfig.port) is already in use by $serviceName" -ForegroundColor Yellow
        return $false
    }
    
    Write-Host "🚀 Starting $serviceName on port $($serviceConfig.port)..." -ForegroundColor Green
    
    # Comando Java para ejecutar el JAR
    $javaArgs = @(
        "-jar"
        $jarPath
        "--spring.profiles.active=$($serviceConfig.profile)"
        "--server.port=$($serviceConfig.port)"
    )
    
    # Iniciar proceso en segundo plano
    $process = Start-Process -FilePath "java" -ArgumentList $javaArgs -WindowStyle Hidden -PassThru -RedirectStandardOutput $logPath
    
    # Guardar PID
    $process.Id | Out-File -FilePath $pidPath
    
    Write-Host "✅ $serviceName started with PID: $($process.Id)" -ForegroundColor Green
    return $true
}

function Stop-Service {
    param($serviceName)
    
    $pidPath = "$pidsDir/$serviceName.pid"
    
    if (Test-Path $pidPath) {
        $pid = Get-Content $pidPath
        try {
            $process = Get-Process -Id $pid -ErrorAction Stop
            Stop-Process -Id $pid -Force
            Remove-Item $pidPath
            Write-Host "🛑 Stopped $serviceName (PID: $pid)" -ForegroundColor Yellow
        } catch {
            Write-Host "⚠️  Process $pid for $serviceName not found" -ForegroundColor Yellow
            Remove-Item $pidPath -ErrorAction SilentlyContinue
        }
    } else {
        Write-Host "⚠️  No PID file found for $serviceName" -ForegroundColor Yellow
    }
}

function Show-Status {
    Write-Host "📊 ARKA Services Status:" -ForegroundColor Cyan
    Write-Host "Profile: $Profile" -ForegroundColor White
    Write-Host "----------------------------------------" -ForegroundColor Gray
    
    foreach ($serviceName in ($services.Keys | Sort-Object {$services[$_].order})) {
        $serviceConfig = $services[$serviceName]
        $pidPath = "$pidsDir/$serviceName.pid"
        
        if (Test-Path $pidPath) {
            $pid = Get-Content $pidPath
            try {
                $process = Get-Process -Id $pid -ErrorAction Stop
                $status = "RUNNING"
                $color = "Green"
            } catch {
                $status = "STOPPED"
                $color = "Red"
                Remove-Item $pidPath -ErrorAction SilentlyContinue
            }
        } else {
            $status = "STOPPED"
            $color = "Red"
        }
        
        Write-Host "  $serviceName`:$($serviceConfig.port) - $status" -ForegroundColor $color
    }
}

# Main execution
Write-Host "🏗️  ARKA Production Deployment Manager" -ForegroundColor Cyan
Write-Host "Profile: $Profile | Action: $Action | Service: $Service" -ForegroundColor White
Write-Host "================================================" -ForegroundColor Gray

switch ($Action.ToLower()) {
    "start" {
        if ($Service -eq "all") {
            # Iniciar servicios en orden
            foreach ($serviceName in ($services.Keys | Sort-Object {$services[$_].order})) {
                $serviceConfig = $services[$serviceName]
                Start-Service $serviceName $serviceConfig
                Start-Sleep -Seconds 5  # Esperar entre servicios
            }
        } else {
            if ($services.ContainsKey($Service)) {
                Start-Service $Service $services[$Service]
            } else {
                Write-Host "❌ Unknown service: $Service" -ForegroundColor Red
            }
        }
    }
    "stop" {
        if ($Service -eq "all") {
            # Detener servicios en orden inverso
            foreach ($serviceName in ($services.Keys | Sort-Object {$services[$_].order} -Descending)) {
                Stop-Service $serviceName
            }
        } else {
            if ($services.ContainsKey($Service)) {
                Stop-Service $Service
            } else {
                Write-Host "❌ Unknown service: $Service" -ForegroundColor Red
            }
        }
    }
    "restart" {
        & $MyInvocation.MyCommand.Path -Profile $Profile -Action "stop" -Service $Service
        Start-Sleep -Seconds 3
        & $MyInvocation.MyCommand.Path -Profile $Profile -Action "start" -Service $Service
    }
    "status" {
        Show-Status
    }
    default {
        Write-Host "❌ Unknown action: $Action" -ForegroundColor Red
        Write-Host "Valid actions: start, stop, restart, status" -ForegroundColor Yellow
    }
}

Write-Host "🎉 Operation completed!" -ForegroundColor Green