@echo off
REM 🚀 Script de Inicio Rápido ARKA E-commerce Platform
REM ===================================================

echo.
echo ========================================
echo 🚀 ARKA E-commerce Platform
echo ========================================
echo.

echo 🔍 Verificando Docker...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker no está instalado o no está en el PATH
    echo Por favor instala Docker Desktop y asegúrate de que esté corriendo
    pause
    exit /b 1
)

echo ✅ Docker encontrado

echo.
echo 🐳 Iniciando todos los servicios...
echo ⏳ Esto puede tomar unos minutos...
echo.

REM Intentar con docker-compose primero, luego con docker compose
docker-compose up -d >nul 2>&1
if %errorlevel% neq 0 (
    docker compose up -d
    if %errorlevel% neq 0 (
        echo ❌ Error iniciando los servicios
        pause
        exit /b 1
    )
)

echo ✅ Servicios iniciados

echo.
echo ⏳ Esperando que los servicios estén listos...
timeout /t 30 /nobreak >nul

echo.
echo 🎉 ¡ARKA E-commerce Platform está corriendo!
echo.
echo 📱 Servicios disponibles:
echo ├── 🏠 Aplicación Principal: http://localhost:8888
echo ├── 🌐 API Gateway:          http://localhost:8080  
echo ├── 🔍 Eureka Server:        http://localhost:8761
echo ├── 📋 Gestor Solicitudes:   http://localhost:8082
echo ├── 💰 Cotizador:            http://localhost:8083
echo └── 👋 Hello World:          http://localhost:8084
echo.
echo 🔗 Endpoints principales:
echo ├── API E-commerce:  http://localhost:8888/productos
echo ├── API Terceros:    http://localhost:8888/api/terceros/ObtenerDatos/productos
echo └── Health Check:    http://localhost:8888/actuator/health
echo.
echo 💡 Comandos útiles:
echo ├── Ver estado:      docker-compose ps
echo ├── Ver logs:        docker-compose logs -f
echo └── Detener todo:    docker-compose down
echo.

pause
