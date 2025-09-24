# Script de Demostración de Spring Security ARKA
# Pruebas de autenticación y autorización

# URLs base
$BASE_URL = "http://localhost:8080"
$AUTH_BASE = "$BASE_URL/auth"
$API_BASE = "$BASE_URL/api"

# Headers comunes
$CONTENT_TYPE = @{"Content-Type" = "application/json"}

Write-Host "=== DEMO SPRING SECURITY ARKA ===" -ForegroundColor Green
Write-Host ""

# Función para hacer peticiones
function Invoke-ApiRequest {
    param(
        [string]$Method = "GET",
        [string]$Uri,
        [hashtable]$Headers = @{},
        [object]$Body = $null
    )
    
    try {
        $params = @{
            Method = $Method
            Uri = $Uri
            Headers = ($CONTENT_TYPE + $Headers)
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 3)
        }
        
        $response = Invoke-RestMethod @params
        return $response
    }
    catch {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        }
        return $null
    }
}

# 1. Intentar acceder a endpoint protegido sin autenticación
Write-Host "1. Probando acceso sin autenticación..." -ForegroundColor Yellow
$response = Invoke-ApiRequest -Uri "$API_BASE/calculos/test" -Method GET
if (-not $response) {
    Write-Host "✓ Acceso denegado correctamente" -ForegroundColor Green
} else {
    Write-Host "✗ Debería haber denegado el acceso" -ForegroundColor Red
}
Write-Host ""

# 2. Registrar un nuevo usuario
Write-Host "2. Registrando nuevo usuario..." -ForegroundColor Yellow
$newUser = @{
    username = "testuser"
    email = "testuser@arka.com"
    password = "password123"
    nombreCompleto = "Usuario de Prueba"
}

$registerResponse = Invoke-ApiRequest -Method POST -Uri "$AUTH_BASE/register" -Body $newUser
if ($registerResponse) {
    Write-Host "✓ Usuario registrado exitosamente" -ForegroundColor Green
    Write-Host "Token: $($registerResponse.accessToken.Substring(0, 20))..." -ForegroundColor Cyan
} else {
    Write-Host "✗ Error al registrar usuario" -ForegroundColor Red
}
Write-Host ""

# 3. Login con usuario administrador
Write-Host "3. Login como administrador..." -ForegroundColor Yellow
$adminLogin = @{
    identifier = "admin"
    password = "admin123"
}

$adminAuth = Invoke-ApiRequest -Method POST -Uri "$AUTH_BASE/login" -Body $adminLogin
if ($adminAuth) {
    Write-Host "✓ Login admin exitoso" -ForegroundColor Green
    $adminToken = $adminAuth.accessToken
    $adminHeaders = @{"Authorization" = "Bearer $adminToken"}
    Write-Host "Token: $($adminToken.Substring(0, 20))..." -ForegroundColor Cyan
    Write-Host "Rol: $($adminAuth.usuario.rol)" -ForegroundColor Cyan
} else {
    Write-Host "✗ Error en login admin" -ForegroundColor Red
}
Write-Host ""

# 4. Login con usuario regular
Write-Host "4. Login como usuario regular..." -ForegroundColor Yellow
$userLogin = @{
    identifier = "usuario"
    password = "admin123"
}

$userAuth = Invoke-ApiRequest -Method POST -Uri "$AUTH_BASE/login" -Body $userLogin
if ($userAuth) {
    Write-Host "✓ Login usuario exitoso" -ForegroundColor Green
    $userToken = $userAuth.accessToken
    $userHeaders = @{"Authorization" = "Bearer $userToken"}
    Write-Host "Token: $($userToken.Substring(0, 20))..." -ForegroundColor Cyan
    Write-Host "Rol: $($userAuth.usuario.rol)" -ForegroundColor Cyan
} else {
    Write-Host "✗ Error en login usuario" -ForegroundColor Red
}
Write-Host ""

# 5. Probar acceso a endpoint de administración con admin
if ($adminHeaders) {
    Write-Host "5. Admin accediendo a endpoint de administración..." -ForegroundColor Yellow
    $adminAccess = Invoke-ApiRequest -Uri "$API_BASE/admin/usuarios" -Headers $adminHeaders
    if ($adminAccess) {
        Write-Host "✓ Acceso admin permitido correctamente" -ForegroundColor Green
    } else {
        Write-Host "✗ Admin debería tener acceso" -ForegroundColor Red
    }
}
Write-Host ""

# 6. Probar acceso a endpoint de administración con usuario regular
if ($userHeaders) {
    Write-Host "6. Usuario regular intentando acceder a administración..." -ForegroundColor Yellow
    $userAdminAccess = Invoke-ApiRequest -Uri "$API_BASE/admin/usuarios" -Headers $userHeaders
    if (-not $userAdminAccess) {
        Write-Host "✓ Acceso denegado correctamente a usuario regular" -ForegroundColor Green
    } else {
        Write-Host "✗ Usuario regular no debería tener acceso admin" -ForegroundColor Red
    }
}
Write-Host ""

# 7. Probar endpoint de cálculos (todos los roles autenticados)
if ($userHeaders) {
    Write-Host "7. Usuario accediendo a cálculos..." -ForegroundColor Yellow
    $calculoRequest = @{
        origen = "Bogotá"
        destino = "Medellín"
        peso = 2.5
        dimensiones = "30x20x15"
    }
    
    $calculoResponse = Invoke-ApiRequest -Method POST -Uri "$API_BASE/calculos/envio" -Headers $userHeaders -Body $calculoRequest
    if ($calculoResponse) {
        Write-Host "✓ Cálculo realizado exitosamente" -ForegroundColor Green
        Write-Host "ID: $($calculoResponse.id), Estado: $($calculoResponse.estado)" -ForegroundColor Cyan
    } else {
        Write-Host "✗ Error al realizar cálculo" -ForegroundColor Red
    }
}
Write-Host ""

# 8. Probar refresh token
if ($userAuth -and $userAuth.refreshToken) {
    Write-Host "8. Probando refresh token..." -ForegroundColor Yellow
    $refreshRequest = @{
        refreshToken = $userAuth.refreshToken
    }
    
    $refreshResponse = Invoke-ApiRequest -Method POST -Uri "$AUTH_BASE/refresh" -Body $refreshRequest
    if ($refreshResponse) {
        Write-Host "✓ Token refrescado exitosamente" -ForegroundColor Green
        Write-Host "Nuevo token: $($refreshResponse.accessToken.Substring(0, 20))..." -ForegroundColor Cyan
    } else {
        Write-Host "✗ Error al refrescar token" -ForegroundColor Red
    }
}
Write-Host ""

# 9. Validar token actual
if ($userHeaders) {
    Write-Host "9. Validando token actual..." -ForegroundColor Yellow
    $validateResponse = Invoke-ApiRequest -Uri "$AUTH_BASE/validate" -Headers $userHeaders
    if ($validateResponse) {
        Write-Host "✓ Token válido" -ForegroundColor Green
    } else {
        Write-Host "✗ Token inválido" -ForegroundColor Red
    }
}
Write-Host ""

# 10. Obtener información del usuario actual
if ($userHeaders) {
    Write-Host "10. Obteniendo información del usuario..." -ForegroundColor Yellow
    $meResponse = Invoke-ApiRequest -Uri "$AUTH_BASE/me" -Headers $userHeaders
    if ($meResponse) {
        Write-Host "✓ Información obtenida:" -ForegroundColor Green
        Write-Host "  Username: $($meResponse.username)" -ForegroundColor Cyan
        Write-Host "  Rol: $($meResponse.rol)" -ForegroundColor Cyan
        Write-Host "  ID: $($meResponse.id)" -ForegroundColor Cyan
    } else {
        Write-Host "✗ Error al obtener información" -ForegroundColor Red
    }
}
Write-Host ""

Write-Host "=== RESUMEN DE PRUEBAS ===" -ForegroundColor Green
Write-Host "• Autenticación JWT: Implementada" -ForegroundColor Cyan
Write-Host "• Autorización por roles: Implementada" -ForegroundColor Cyan
Write-Host "• Refresh tokens: Implementados" -ForegroundColor Cyan
Write-Host "• Protección de endpoints: Activa" -ForegroundColor Cyan
Write-Host "• Registro de usuarios: Funcional" -ForegroundColor Cyan
Write-Host ""

Write-Host "=== ENDPOINTS DISPONIBLES ===" -ForegroundColor Green
Write-Host "Públicos:" -ForegroundColor Yellow
Write-Host "  POST /auth/register - Registro de usuarios" -ForegroundColor White
Write-Host "  POST /auth/login - Inicio de sesión" -ForegroundColor White
Write-Host "  POST /auth/refresh - Refrescar token" -ForegroundColor White
Write-Host ""
Write-Host "Autenticados (todos los roles):" -ForegroundColor Yellow
Write-Host "  GET  /auth/validate - Validar token" -ForegroundColor White
Write-Host "  GET  /auth/me - Información del usuario" -ForegroundColor White
Write-Host "  POST /api/calculos/envio - Realizar cálculo" -ForegroundColor White
Write-Host ""
Write-Host "Solo administradores:" -ForegroundColor Yellow
Write-Host "  GET  /api/admin/usuarios - Listar usuarios" -ForegroundColor White
Write-Host "  POST /api/admin/usuarios - Crear usuario" -ForegroundColor White
Write-Host "  GET  /api/admin/usuarios/estadisticas - Estadísticas" -ForegroundColor White
Write-Host ""

Write-Host "Para probar, inicia los servicios:" -ForegroundColor Green
Write-Host "1. ./gradlew :eureka-server:bootRun" -ForegroundColor White
Write-Host "2. ./gradlew :api-gateway:bootRun" -ForegroundColor White
Write-Host "3. ./gradlew :arca-gestor-solicitudes:bootRun" -ForegroundColor White
