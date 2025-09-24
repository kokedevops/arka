# 🚀 Quick Start Script - Desarrollo Individual de Microservicios
# Para desarrollo de un servicio específico sin dependencias

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("config-server", "eureka-server", "api-gateway", "hello-world-service", "arca-cotizador", "arca-gestor-solicitudes")]
    [string]$Service,
    
    [Parameter(Mandatory=$false)]
    [switch]$WithDependencies,
    
    [Parameter(Mandatory=$false)]
    [switch]$Debug,
    
    [Parameter(Mandatory=$false)]
    [string]$Profile = "dev",
    
    [Parameter(Mandatory=$false)]
    [int]$Port
)

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "🚀 ARKA MICROSERVICES - QUICK START" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# Configuración de servicios
$serviceConfig = @{
    "config-server" = @{
        Name = "Config Server"
        Port = 8888
        Dependencies = @()
        Description = "Servidor de configuración centralizada"
        TestUrl = "http://localhost:8888/actuator/health"
    }
    "eureka-server" = @{
        Name = "Eureka Server"
        Port = 8761
        Dependencies = @("config-server")
        Description = "Servidor de descubrimiento de servicios"
        TestUrl = "http://localhost:8761/actuator/health"
    }
    "api-gateway" = @{
        Name = "API Gateway"
        Port = 8080
        Dependencies = @("config-server", "eureka-server")
        Description = "Gateway de entrada a los microservicios"
        TestUrl = "http://localhost:8080/actuator/health"
    }
    "hello-world-service" = @{
        Name = "Hello World Service"
        Port = 8081
        Dependencies = @("config-server", "eureka-server")
        Description = "Servicio de ejemplo y testing"
        TestUrl = "http://localhost:8081/hello"
    }
    "arca-cotizador" = @{
        Name = "Arca Cotizador"
        Port = 8082
        Dependencies = @("config-server", "eureka-server")
        Description = "Servicio de cotizaciones"
        TestUrl = "http://localhost:8082/cotizaciones"
    }
    "arca-gestor-solicitudes" = @{
        Name = "Arca Gestor Solicitudes"
        Port = 8083
        Dependencies = @("config-server", "eureka-server")
        Description = "Servicio de gestión de solicitudes"
        TestUrl = "http://localhost:8083/solicitudes"
    }
}

# Función para mostrar ayuda
function Show-Help {
    Write-Host "🎯 USO:" -ForegroundColor Yellow
    Write-Host "   .\quick-start.ps1 -Service <nombre-servicio> [opciones]" -ForegroundColor White
    Write-Host ""
    Write-Host "📋 SERVICIOS DISPONIBLES:" -ForegroundColor Yellow
    foreach ($key in $serviceConfig.Keys | Sort-Object) {
        $config = $serviceConfig[$key]
        Write-Host "   🔹 $key" -ForegroundColor Cyan -NoNewline
        Write-Host " ($($config.Port))" -ForegroundColor Gray -NoNewline
        Write-Host " - $($config.Description)" -ForegroundColor White
    }
    Write-Host ""
    Write-Host "⚙️ OPCIONES:" -ForegroundColor Yellow
    Write-Host "   -WithDependencies  : Iniciar dependencias automáticamente" -ForegroundColor White
    Write-Host "   -Debug             : Ejecutar en modo debug" -ForegroundColor White
    Write-Host "   -Profile <profile> : Perfil de Spring (dev, prod, test)" -ForegroundColor White
    Write-Host "   -Port <puerto>     : Puerto personalizado" -ForegroundColor White
    Write-Host ""
    Write-Host "🌟 EJEMPLOS:" -ForegroundColor Yellow
    Write-Host "   .\quick-start.ps1 -Service arca-cotizador" -ForegroundColor Green
    Write-Host "   .\quick-start.ps1 -Service arca-cotizador -WithDependencies" -ForegroundColor Green
    Write-Host "   .\quick-start.ps1 -Service arca-cotizador -Debug -Profile dev" -ForegroundColor Green
    Write-Host "   .\quick-start.ps1 -Service hello-world-service -Port 9081" -ForegroundColor Green
    Write-Host ""
}

# Si no se especifica servicio, mostrar ayuda
if (-not $Service) {
    Show-Help
    exit 0
}

$config = $serviceConfig[$Service]
if (-not $config) {
    Write-Host "❌ Servicio '$Service' no encontrado" -ForegroundColor Red
    Show-Help
    exit 1
}

Write-Host "🎯 Servicio seleccionado: $($config.Name)" -ForegroundColor Green
Write-Host "📝 Descripción: $($config.Description)" -ForegroundColor White
Write-Host "🔌 Puerto por defecto: $($config.Port)" -ForegroundColor White

# Override puerto si se especifica
$targetPort = if ($Port) { $Port } else { $config.Port }
if ($Port -and $Port -ne $config.Port) {
    Write-Host "🔧 Puerto personalizado: $Port" -ForegroundColor Yellow
}

Write-Host ""

# Función para verificar si un servicio está ejecutándose
function Test-ServiceRunning {
    param([int]$Port)
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $tcpClient.Connect("localhost", $Port)
        $tcpClient.Close()
        return $true
    } catch {
        return $false
    }
}

# Función para iniciar un servicio
function Start-MicroService {
    param(
        [string]$ServiceName,
        [int]$ServicePort,
        [bool]$IsDebug = $false,
        [string]$SpringProfile = "dev",
        [int]$CustomPort = 0
    )
    
    # Construir argumentos
    $args = @()
    
    if ($CustomPort -gt 0) {
        $args += "--server.port=$CustomPort"
    }
    
    $args += "--spring.profiles.active=$SpringProfile"
    
    # Comando de Gradle
    $gradleArgs = @(":$ServiceName" + ":bootRun")
    
    if ($IsDebug) {
        $gradleArgs += "--debug-jvm"
    }
    
    if ($args.Count -gt 0) {
        $gradleArgs += "--args=`"$($args -join ' ')`""
    }
    
    # Iniciar en nueva ventana
    $windowTitle = "$($serviceConfig[$ServiceName].Name) - Puerto $ServicePort"
    
    Write-Host "🚀 Iniciando $($serviceConfig[$ServiceName].Name)..." -ForegroundColor Blue
    Write-Host "   Comando: .\gradlew.bat $($gradleArgs -join ' ')" -ForegroundColor Gray
    
    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "cd '$PWD'; `$host.ui.RawUI.WindowTitle='$windowTitle'; Write-Host '🚀 $windowTitle' -ForegroundColor Cyan; .\gradlew.bat $($gradleArgs -join ' ')"
    ) -WindowStyle Normal
}

# Verificar si el servicio ya está ejecutándose
if (Test-ServiceRunning -Port $targetPort) {
    Write-Host "⚠️ El puerto $targetPort ya está en uso" -ForegroundColor Yellow
    $response = Read-Host "¿Deseas continuar de todos modos? (y/N)"
    if ($response -notmatch "^[Yy]") {
        Write-Host "❌ Operación cancelada" -ForegroundColor Red
        exit 0
    }
}

# Iniciar dependencias si se solicita
if ($WithDependencies) {
    Write-Host "🔗 Iniciando dependencias..." -ForegroundColor Blue
    
    foreach ($dependency in $config.Dependencies) {
        $depConfig = $serviceConfig[$dependency]
        $depPort = $depConfig.Port
        
        if (-not (Test-ServiceRunning -Port $depPort)) {
            Write-Host "   🔄 Iniciando dependencia: $($depConfig.Name)" -ForegroundColor Yellow
            Start-MicroService -ServiceName $dependency -ServicePort $depPort -SpringProfile $Profile
            
            # Esperar que la dependencia esté lista
            Write-Host "   ⏳ Esperando que $($depConfig.Name) esté disponible..." -ForegroundColor Yellow
            $timeout = 60
            $elapsed = 0
            
            while ($elapsed -lt $timeout) {
                if (Test-ServiceRunning -Port $depPort) {
                    Write-Host "   ✅ $($depConfig.Name) está disponible" -ForegroundColor Green
                    break
                }
                Start-Sleep -Seconds 2
                $elapsed += 2
                Write-Host "." -NoNewline -ForegroundColor Gray
            }
            
            if ($elapsed -ge $timeout) {
                Write-Host ""
                Write-Host "   ⚠️ Timeout esperando $($depConfig.Name), continuando..." -ForegroundColor Yellow
            }
            
            Write-Host ""
        } else {
            Write-Host "   ✅ $($depConfig.Name) ya está ejecutándose" -ForegroundColor Green
        }
    }
    
    Write-Host "🔗 Dependencias listas, iniciando servicio principal..." -ForegroundColor Green
    Start-Sleep -Seconds 5
}

# Iniciar el servicio principal
Write-Host "🎯 Iniciando $($config.Name)..." -ForegroundColor Blue
Start-MicroService -ServiceName $Service -ServicePort $targetPort -IsDebug $Debug -SpringProfile $Profile -CustomPort $Port

# Esperar a que el servicio esté disponible
Write-Host ""
Write-Host "⏳ Esperando que $($config.Name) esté disponible..." -ForegroundColor Yellow

$timeout = 90
$elapsed = 0

while ($elapsed -lt $timeout) {
    if (Test-ServiceRunning -Port $targetPort) {
        Write-Host ""
        Write-Host "✅ $($config.Name) está disponible!" -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 2
    $elapsed += 2
    Write-Host "." -NoNewline -ForegroundColor Gray
}

if ($elapsed -ge $timeout) {
    Write-Host ""
    Write-Host "⚠️ Timeout esperando $($config.Name)" -ForegroundColor Yellow
    Write-Host "   El servicio puede estar iniciándose aún..." -ForegroundColor Yellow
}

# Información final
Write-Host ""
Write-Host "===============================================" -ForegroundColor Green
Write-Host "✅ SERVICIO INICIADO!" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green
Write-Host ""

Write-Host "🌐 URLs de acceso:" -ForegroundColor Cyan
Write-Host "   Servicio: http://localhost:$targetPort" -ForegroundColor White
Write-Host "   Health: http://localhost:$targetPort/actuator/health" -ForegroundColor Gray
Write-Host "   Info: http://localhost:$targetPort/actuator/info" -ForegroundColor Gray

if ($config.TestUrl) {
    Write-Host "   Test: $($config.TestUrl -replace $config.Port, $targetPort)" -ForegroundColor Green
}

Write-Host ""
Write-Host "🔧 Configuración aplicada:" -ForegroundColor Cyan
Write-Host "   Perfil Spring: $Profile" -ForegroundColor White
Write-Host "   Puerto: $targetPort" -ForegroundColor White
Write-Host "   Debug: $(if ($Debug) { 'Habilitado' } else { 'Deshabilitado' })" -ForegroundColor White
Write-Host "   Dependencias: $(if ($WithDependencies) { 'Iniciadas automáticamente' } else { 'No iniciadas' })" -ForegroundColor White

Write-Host ""
Write-Host "💡 Comandos útiles:" -ForegroundColor Cyan
Write-Host "   Health check: .\check-services-health.ps1" -ForegroundColor Gray
Write-Host "   Ver logs: Consultar la ventana del servicio" -ForegroundColor Gray
Write-Host "   Detener servicio: Ctrl+C en la ventana del servicio" -ForegroundColor Gray
Write-Host "   Detener todos: .\stop-all-services.ps1" -ForegroundColor Gray

Write-Host ""
Write-Host "🎉 ¡Listo para desarrollar!" -ForegroundColor Green