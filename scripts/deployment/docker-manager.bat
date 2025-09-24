@echo off
REM ARKA Microservices - Docker Management Script for Windows
REM Este script facilita la gestión de los microservicios ARKA usando Docker Compose

setlocal enabledelayedexpansion

REM Verificar si Docker está disponible
where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker no está instalado o no está en PATH
    exit /b 1
)

where docker-compose >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker Compose no está instalado o no está en PATH
    exit /b 1
)

REM Función para mostrar ayuda
if "%1"=="help" goto :show_help
if "%1"=="-h" goto :show_help
if "%1"=="--help" goto :show_help
if "%1"=="" goto :show_help

REM Procesar comandos
if "%1"=="build" goto :build_images
if "%1"=="up" goto :start_services
if "%1"=="down" goto :stop_services
if "%1"=="restart" goto :restart_services
if "%1"=="logs" goto :show_logs
if "%1"=="status" goto :show_status
if "%1"=="clean" goto :clean_resources
if "%1"=="dev" goto :start_dev
if "%1"=="prod" goto :start_prod
if "%1"=="health" goto :check_health

echo [ERROR] Comando desconocido: %1
goto :show_help

:show_help
echo ARKA Microservices Docker Management
echo.
echo Uso: %0 [COMANDO] [OPCIONES]
echo.
echo Comandos disponibles:
echo   build         - Construir todas las imágenes Docker
echo   up            - Iniciar todos los servicios
echo   down          - Detener todos los servicios
echo   restart       - Reiniciar todos los servicios
echo   logs          - Ver logs de todos los servicios
echo   status        - Ver estado de los servicios
echo   clean         - Limpiar contenedores, imágenes y volúmenes
echo   dev           - Modo desarrollo (solo servicios core^)
echo   prod          - Modo producción (todos los servicios^)
echo   health        - Verificar health checks
echo.
echo Ejemplos:
echo   %0 build              # Construir todas las imágenes
echo   %0 up                 # Iniciar todos los servicios
echo   %0 logs               # Ver logs de todos los servicios
echo   %0 health             # Verificar health checks
goto :eof

:build_images
echo [INFO] Construyendo imágenes Docker...
docker-compose build --no-cache
if %errorlevel% equ 0 (
    echo [SUCCESS] Construcción completada
) else (
    echo [ERROR] Error en la construcción
)
goto :eof

:start_services
echo [INFO] Iniciando todos los servicios...
docker-compose up -d
if %errorlevel% equ 0 (
    echo [SUCCESS] Servicios iniciados
    timeout /t 30 /nobreak >nul
    goto :check_health
) else (
    echo [ERROR] Error al iniciar servicios
)
goto :eof

:start_dev
echo [INFO] Iniciando servicios en modo desarrollo...
docker-compose up -d eureka-server api-gateway gestor-solicitudes
if %errorlevel% equ 0 (
    echo [SUCCESS] Servicios de desarrollo iniciados
    timeout /t 30 /nobreak >nul
    goto :check_health
) else (
    echo [ERROR] Error al iniciar servicios de desarrollo
)
goto :eof

:start_prod
echo [INFO] Iniciando todos los servicios en modo producción...
docker-compose up -d
if %errorlevel% equ 0 (
    echo [SUCCESS] Servicios de producción iniciados
    timeout /t 30 /nobreak >nul
    goto :check_health
) else (
    echo [ERROR] Error al iniciar servicios de producción
)
goto :eof

:stop_services
echo [INFO] Deteniendo servicios...
docker-compose down
if %errorlevel% equ 0 (
    echo [SUCCESS] Servicios detenidos
) else (
    echo [ERROR] Error al detener servicios
)
goto :eof

:restart_services
echo [INFO] Reiniciando servicios...
docker-compose restart
if %errorlevel% equ 0 (
    echo [SUCCESS] Servicios reiniciados
) else (
    echo [ERROR] Error al reiniciar servicios
)
goto :eof

:show_logs
echo [INFO] Mostrando logs de todos los servicios...
docker-compose logs -f
goto :eof

:show_status
echo [INFO] Estado de los servicios:
docker-compose ps
echo.
echo [INFO] Uso de recursos:
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"
goto :eof

:check_health
echo [INFO] Verificando health checks...

curl -f "http://localhost:8761/actuator/health" >nul 2>nul
if %errorlevel% equ 0 (
    echo [OK] Eureka Server está saludable
) else (
    echo [ERROR] Eureka Server no responde
)

curl -f "http://localhost:8080/actuator/health" >nul 2>nul
if %errorlevel% equ 0 (
    echo [OK] API Gateway está saludable
) else (
    echo [ERROR] API Gateway no responde
)

curl -f "http://localhost:8082/actuator/health" >nul 2>nul
if %errorlevel% equ 0 (
    echo [OK] Gestor Solicitudes está saludable
) else (
    echo [ERROR] Gestor Solicitudes no responde
)

curl -f "http://localhost:8083/actuator/health" >nul 2>nul
if %errorlevel% equ 0 (
    echo [OK] Cotizador está saludable
) else (
    echo [ERROR] Cotizador no responde
)
goto :eof

:clean_resources
echo [WARNING] Esta operación eliminará contenedores, imágenes y volúmenes. ¿Continuar? (y/N^)
set /p response=
if /i "!response!"=="y" (
    echo [INFO] Limpiando recursos Docker...
    docker-compose down -v --rmi all --remove-orphans
    docker system prune -f
    echo [SUCCESS] Limpieza completada
) else (
    echo [INFO] Operación cancelada
)
goto :eof
