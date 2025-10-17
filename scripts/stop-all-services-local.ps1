# 🛑 Script para detener todos los servicios ARKA
# Autor: ARKA Development Team
# Descripción: Detiene todos los procesos Java/Gradle ejecutándose

Write-Host "========================================" -ForegroundColor Red
Write-Host "🛑 ARKA - Deteniendo Servicios" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red
Write-Host ""

# Buscar y detener procesos de Java/Gradle
Write-Host "🔍 Buscando procesos de Java y Gradle..." -ForegroundColor Yellow

$javaProcesses = Get-Process | Where-Object { $_.ProcessName -like "*java*" -or $_.ProcessName -like "*gradle*" }

if ($javaProcesses.Count -eq 0) {
    Write-Host "✅ No se encontraron procesos en ejecución" -ForegroundColor Green
} else {
    Write-Host "📊 Se encontraron $($javaProcesses.Count) procesos" -ForegroundColor Cyan
    Write-Host ""
    
    foreach ($process in $javaProcesses) {
        Write-Host "   🛑 Deteniendo proceso: $($process.ProcessName) (PID: $($process.Id))" -ForegroundColor Yellow
        try {
            Stop-Process -Id $process.Id -Force
            Write-Host "   ✅ Proceso detenido exitosamente" -ForegroundColor Green
        } catch {
            Write-Host "   ⚠️  Error al detener proceso: $_" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "✅ SERVICIOS DETENIDOS" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Esperar 2 segundos antes de cerrar
Start-Sleep -Seconds 2
