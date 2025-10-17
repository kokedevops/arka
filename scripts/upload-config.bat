@echo off
echo ==========================================
echo SUBIENDO CONFIGURACIONES AL GITHUB REPO
echo ==========================================
echo.

REM Verificar si existe el directorio .git
if not exist ".git" (
    echo Inicializando repositorio Git...
    git init
    git remote add origin https://github.com/kokedevops/arka-config-server.git
)

echo Agregando archivos de configuración...
git add config-repository/

echo Realizando commit...
git commit -m "feat: agregar configuraciones de microservicios desde arka-1"

echo Subiendo al repositorio remoto...
git push origin main

echo.
echo ==========================================
echo CONFIGURACIONES SUBIDAS EXITOSAMENTE!
echo ==========================================
echo.
echo Para actualizar configuraciones en tiempo real:
echo 1. Modifica los archivos en config-repository/
echo 2. Ejecuta este script nuevamente
echo 3. Llama al endpoint de refresh en cada servicio:
echo    POST http://localhost:8081/actuator/refresh
echo    POST http://localhost:8082/actuator/refresh
echo    etc.
echo.
pause
