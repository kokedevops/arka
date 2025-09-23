# 🛑 Script para detener todos los microservicios
# Stop All Services - Arka Microservices

Write-Host "===============================================" -ForegroundColor Red
Write-Host "🛑 DETENIENDO TODOS LOS MICROSERVICIOS" -ForegroundColor Red
Write-Host "===============================================" -ForegroundColor Red
Write-Host ""

# Función para detener procesos por puerto
function Stop-ProcessByPort {
    param([int]$Port)
    
    try {
        $connections = netstat -ano | Select-String ":$Port "
        
        foreach ($connection in $connections) {
            $connectionString = $connection.ToString().Trim()
            $parts = $connectionString -split '\s+'
            
            if ($parts.Length -ge 5) {
                $pid = $parts[4]
                
                if ($pid -ne "0" -and $pid -match '^\d+$') {
                    try {
                        $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
                        if ($process) {
                            Write-Host "🔄 Deteniendo proceso en puerto $Port (PID: $pid)..." -ForegroundColor Yellow
                            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                            Write-Host "✅ Proceso $pid detenido" -ForegroundColor Green
                        }
                    } catch {
                        Write-Host "⚠️ No se pudo detener proceso $pid" -ForegroundColor Yellow
                    }
                }
            }
        }
    } catch {
        Write-Host "⚠️ Error al verificar puerto $Port" -ForegroundColor Yellow
    }
}

# Función para detener procesos Java de Gradle
function Stop-GradleJavaProcesses {
    Write-Host "🔍 Buscando procesos Java de Gradle..." -ForegroundColor Blue
    
    try {
        # Buscar procesos Java que contengan gradle en la línea de comandos
        $javaProcesses = Get-WmiObject Win32_Process | Where-Object { 
            $_.Name -eq "java.exe" -and 
            $_.CommandLine -like "*gradle*" -and
            $_.CommandLine -like "*bootRun*"
        }
        
        if ($javaProcesses) {
            Write-Host "📋 Encontrados $($javaProcesses.Count) procesos de microservicios:" -ForegroundColor Yellow
            
            foreach ($process in $javaProcesses) {
                try {
                    # Extraer nombre del servicio de la línea de comandos
                    $serviceName = "Unknown"
                    if ($process.CommandLine -match ":([^:]+):bootRun") {
                        $serviceName = $matches[1]
                    }
                    
                    Write-Host "   🔄 Deteniendo $serviceName (PID: $($process.ProcessId))..." -ForegroundColor Yellow
                    Stop-Process -Id $process.ProcessId -Force -ErrorAction SilentlyContinue
                    Write-Host "   ✅ $serviceName detenido" -ForegroundColor Green
                } catch {
                    Write-Host "   ❌ Error deteniendo proceso $($process.ProcessId)" -ForegroundColor Red
                }
            }
        } else {
            Write-Host "ℹ️ No se encontraron procesos Java de Gradle ejecutándose" -ForegroundColor Cyan
        }
    } catch {
        Write-Host "❌ Error al buscar procesos Java: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Lista de puertos de los microservicios
$servicePorts = @(
    @{Name="Config Server"; Port=8888},
    @{Name="Eureka Server"; Port=8761},
    @{Name="API Gateway"; Port=8080},
    @{Name="Hello World Service"; Port=8081},
    @{Name="Arca Cotizador"; Port=8082},
    @{Name="Arca Gestor Solicitudes"; Port=8083}
)

Write-Host "🔍 Verificando servicios activos..." -ForegroundColor Blue
Write-Host ""

# Verificar qué servicios están ejecutándose
$runningServices = @()
foreach ($service in $servicePorts) {
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $tcpClient.Connect("localhost", $service.Port)
        $tcpClient.Close()
        $runningServices += $service
        Write-Host "🟢 $($service.Name) - Puerto $($service.Port) ACTIVO" -ForegroundColor Green
    } catch {
        Write-Host "🔴 $($service.Name) - Puerto $($service.Port) INACTIVO" -ForegroundColor Gray
    }
}

if ($runningServices.Count -eq 0) {
    Write-Host ""
    Write-Host "ℹ️ No hay servicios ejecutándose en los puertos esperados" -ForegroundColor Cyan
    Write-Host "✅ Verificando procesos Java restantes..." -ForegroundColor Green
    Stop-GradleJavaProcesses
} else {
    Write-Host ""
    Write-Host "🛑 Deteniendo $($runningServices.Count) servicios activos..." -ForegroundColor Red
    Write-Host ""
    
    # Detener servicios en orden inverso (recomendado)
    $serviceOrder = @(
        "Arca Gestor Solicitudes",
        "Arca Cotizador", 
        "Hello World Service",
        "API Gateway",
        "Eureka Server",
        "Config Server"
    )
    
    foreach ($serviceName in $serviceOrder) {
        $service = $runningServices | Where-Object { $_.Name -eq $serviceName }
        if ($service) {
            Write-Host "🛑 Deteniendo $($service.Name)..." -ForegroundColor Yellow
            Stop-ProcessByPort -Port $service.Port
            Start-Sleep -Seconds 2
        }
    }
    
    # Detener cualquier proceso Java de Gradle restante
    Write-Host ""
    Write-Host "🧹 Limpieza final de procesos Gradle..." -ForegroundColor Blue
    Stop-GradleJavaProcesses
}

# Verificación final
Write-Host ""
Write-Host "🔍 Verificación final..." -ForegroundColor Blue
Start-Sleep -Seconds 3

$stillRunning = @()
foreach ($service in $servicePorts) {
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $tcpClient.Connect("localhost", $service.Port)
        $tcpClient.Close()
        $stillRunning += $service
    } catch {
        # Puerto libre - servicio detenido correctamente
    }
}

Write-Host ""
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "📊 RESUMEN DEL SHUTDOWN" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

if ($stillRunning.Count -eq 0) {
    Write-Host "✅ TODOS LOS SERVICIOS DETENIDOS CORRECTAMENTE" -ForegroundColor Green
    Write-Host "🎯 Todos los puertos están libres" -ForegroundColor Green
} else {
    Write-Host "⚠️ ALGUNOS SERVICIOS AÚN ESTÁN EJECUTÁNDOSE:" -ForegroundColor Yellow
    foreach ($service in $stillRunning) {
        Write-Host "   🟡 $($service.Name) - Puerto $($service.Port)" -ForegroundColor Yellow
    }
    Write-Host ""
    Write-Host "💡 Puedes intentar:" -ForegroundColor Cyan
    Write-Host "   1. Ejecutar este script nuevamente" -ForegroundColor White
    Write-Host "   2. Usar Ctrl+C en las ventanas de los servicios" -ForegroundColor White
    Write-Host "   3. Usar Task Manager para terminar procesos java.exe" -ForegroundColor White
}

# Mostrar procesos Java restantes
Write-Host ""
Write-Host "🔍 Procesos Java activos:" -ForegroundColor Blue
try {
    $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
    if ($javaProcesses) {
        foreach ($process in $javaProcesses) {
            try {
                $processInfo = Get-WmiObject Win32_Process -Filter "ProcessId = $($process.Id)" -ErrorAction SilentlyContinue
                if ($processInfo -and $processInfo.CommandLine -like "*gradle*") {
                    Write-Host "   ⚠️ Java/Gradle PID $($process.Id): $($processInfo.CommandLine.Substring(0, [Math]::Min(80, $processInfo.CommandLine.Length)))..." -ForegroundColor Yellow
                } else {
                    Write-Host "   ℹ️ Java PID $($process.Id): (no relacionado con Gradle)" -ForegroundColor Gray
                }
            } catch {
                Write-Host "   ℹ️ Java PID $($process.Id): (información no disponible)" -ForegroundColor Gray
            }
        }
    } else {
        Write-Host "   ✅ No hay procesos Java ejecutándose" -ForegroundColor Green
    }
} catch {
    Write-Host "   ⚠️ Error al verificar procesos Java" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "💡 Comandos útiles para verificación manual:" -ForegroundColor Cyan
Write-Host "   Ver procesos Java: Get-Process java" -ForegroundColor Gray
Write-Host "   Ver puertos en uso: netstat -ano | findstr ':808'" -ForegroundColor Gray
Write-Host "   Terminar proceso por PID: Stop-Process -Id <PID> -Force" -ForegroundColor Gray
Write-Host ""

Write-Host "🔄 Para reiniciar los servicios, ejecuta: .\start-all-services.ps1" -ForegroundColor Cyan
Write-Host ""

Write-Host "✅ Script de shutdown completado!" -ForegroundColor Green
Write-Host "Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray