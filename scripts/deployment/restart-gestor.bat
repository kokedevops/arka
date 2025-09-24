@echo off
echo Reiniciando arca-gestor-solicitudes para cargar usuarios por defecto...

echo Matando procesos Java en puerto 8082...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8082') do taskkill /f /pid %%a >nul 2>&1

echo Esperando 3 segundos...
timeout /t 3 /nobreak >nul

echo Iniciando arca-gestor-solicitudes...
cd /d "c:\Users\valen\arkavalenzuela"
start "ARCA-GESTOR" cmd /c "gradlew.bat :arca-gestor-solicitudes:bootRun"

echo arca-gestor-solicitudes iniciado. Revisa la ventana ARCA-GESTOR para ver los logs.
pause
