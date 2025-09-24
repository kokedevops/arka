# Script para probar el endpoint de login
Write-Host "Probando endpoint de login con credenciales de administrador..."

$loginData = @{
    username = "admin"
    password = "admin123"
}

$jsonBody = $loginData | ConvertTo-Json
$headers = @{
    'Content-Type' = 'application/json'
}

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -Body $jsonBody -Headers $headers
    Write-Host "✅ Login exitoso!"
    Write-Host "Respuesta: $($response | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "❌ Error en login:"
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)"
    Write-Host "Error: $($_.Exception.Message)"
    if ($_.ErrorDetails) {
        Write-Host "Detalles: $($_.ErrorDetails.Message)"
    }
}

Write-Host "`n" + "="*50
Write-Host "Probando endpoint de login con credenciales de gestor..."

$loginDataGestor = @{
    username = "gestor"
    password = "gestor123"
}

$jsonBodyGestor = $loginDataGestor | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -Body $jsonBodyGestor -Headers $headers
    Write-Host "✅ Login exitoso!"
    Write-Host "Respuesta: $($response | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "❌ Error en login:"
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)"
    Write-Host "Error: $($_.Exception.Message)"
    if ($_.ErrorDetails) {
        Write-Host "Detalles: $($_.ErrorDetails.Message)"
    }
}
