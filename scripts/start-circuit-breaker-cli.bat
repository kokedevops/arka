@echo off
echo =========================================
echo ARKA CIRCUIT BREAKER CLI
echo =========================================
echo.

echo Iniciando CLI interactiva para Circuit Breaker...
echo.

REM Configurar variables de entorno para CLI
set SPRING_PROFILES_ACTIVE=dev
set ARKA_CLI_ENABLED=true

echo Configuración:
echo • Profile: %SPRING_PROFILES_ACTIVE%
echo • CLI Enabled: %ARKA_CLI_ENABLED%
echo.

REM Navegar al directorio del proyecto
cd /d "c:\Users\valen\arka\arka-1"

echo Ejecutando aplicación con CLI habilitada...
echo.
echo ⚡ Para usar la CLI, ejecute el servicio con el parámetro --cli
echo ⚡ Ejemplo: gradle :arca-gestor-solicitudes:bootRun --args="--cli"
echo.

REM Ejecutar la aplicación con CLI
gradle :arca-gestor-solicitudes:bootRun --args="--cli" -Darka.cli.enabled=true

echo.
echo =========================================
echo CLI CIRCUIT BREAKER FINALIZADA
echo =========================================
pause
