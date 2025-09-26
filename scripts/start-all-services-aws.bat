@echo off
echo 🚀 INICIANDO DESPLIEGUE ARKA MICROSERVICES - PROFILE AWS
echo ================================================================

echo.
echo 📋 ORDEN DE DESPLIEGUE:
echo 1. Eureka Server (puerto 8761)
echo 2. Config Server (puerto 8888) 
echo 3. API Gateway (puerto 8085)
echo 4. Microservicios de negocio
echo.

echo ⏳ Esperando 10 segundos antes de comenzar...
timeout /t 10 /nobreak >nul

echo.
echo 🎯 PASO 1: Iniciando Eureka Server...
start "Eureka Server" cmd /k "gradle :eureka-server:bootRun --args='--spring.profiles.active=aws'"

echo ⏳ Esperando 30 segundos para que Eureka se inicie completamente...
timeout /t 30 /nobreak >nul

echo.
echo 🎯 PASO 2: Iniciando Config Server...
start "Config Server" cmd /k "gradle :config-server:bootRun --args='--spring.profiles.active=native,aws'"

echo ⏳ Esperando 20 segundos para que Config Server se inicie...
timeout /t 20 /nobreak >nul

echo.
echo 🎯 PASO 3: Iniciando API Gateway...
start "API Gateway" cmd /k "gradle :api-gateway:bootRun --args='--spring.profiles.active=aws'"

echo ⏳ Esperando 15 segundos para que API Gateway se inicie...
timeout /t 15 /nobreak >nul

echo.
echo 🎯 PASO 4: Iniciando Microservicios de Negocio...
start "Arca Cotizador" cmd /k "gradle :arca-cotizador:bootRun --args='--spring.profiles.active=aws'"
start "Arca Gestor Solicitudes" cmd /k "gradle :arca-gestor-solicitudes:bootRun --args='--spring.profiles.active=aws'"
start "Hello World Service" cmd /k "gradle :hello-world-service:bootRun --args='--spring.profiles.active=aws'"

echo.
echo ✅ TODOS LOS SERVICIOS INICIADOS EN PROFILE AWS
echo ================================================================
echo 📊 PUERTOS CONFIGURADOS:
echo   - Eureka Server: http://localhost:8761
echo   - Config Server: http://localhost:8888
echo   - API Gateway: http://localhost:8085  ^(CAMBIADO TEMPORALMENTE^)
echo   - Arca Cotizador: http://localhost:8081
echo   - Arca Gestor Solicitudes: http://localhost:8082
echo   - Hello World Service: http://localhost:8083
echo.
echo 💡 NOTA: API Gateway está en puerto 8085 para evitar conflictos
echo    Una vez resuelto el conflicto, cambiar de vuelta a 8080
echo.
echo ⚠️  IMPORTANTE: Configurar las variables de entorno AWS antes del despliegue:
echo    - AWS_HOSTNAME=tu-instancia-ec2.compute.amazonaws.com
echo    - AWS_EUREKA_HOST=tu-instancia-ec2.compute.amazonaws.com
echo    - AWS_CONFIG_HOST=tu-instancia-ec2.compute.amazonaws.com
echo    - AWS_DB_HOST=tu-rds-instance.region.rds.amazonaws.com
echo    - AWS_REGION=us-east-1
echo.
pause