# 🏗️ ARKA Production JAR Builder
# Script para construir todos los JARs de producción para despliegue

Write-Host "🚀 Building ARKA Production JARs..." -ForegroundColor Green

# Limpiar builds anteriores
Write-Host "🧹 Cleaning previous builds..." -ForegroundColor Yellow
./gradlew clean

# Construir todos los JARs
Write-Host "🔨 Building all production JARs..." -ForegroundColor Yellow
./gradlew bootJar

# Verificar que todos los JARs se crearon correctamente
$jars = @(
    "config-server/build/libs/config-server.jar",
    "eureka-server/build/libs/eureka-server.jar", 
    "api-gateway/build/libs/api-gateway.jar",
    "arca-cotizador/build/libs/arca-cotizador.jar",
    "arca-gestor-solicitudes/build/libs/arca-gestor-solicitudes.jar",
    "hello-world-service/build/libs/hello-world-service.jar"
)

Write-Host "✅ Checking generated JARs:" -ForegroundColor Green
foreach ($jar in $jars) {
    if (Test-Path $jar) {
        $size = (Get-Item $jar).Length / 1MB
        Write-Host "  ✓ $jar - $([math]::Round($size, 2)) MB" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $jar - NOT FOUND!" -ForegroundColor Red
    }
}

# Crear directorio de distribución
$distDir = "dist/jars"
if (!(Test-Path $distDir)) {
    New-Item -ItemType Directory -Path $distDir -Force
}

# Copiar JARs al directorio de distribución
Write-Host "📦 Copying JARs to distribution directory..." -ForegroundColor Yellow
foreach ($jar in $jars) {
    if (Test-Path $jar) {
        $jarName = Split-Path $jar -Leaf
        Copy-Item $jar "$distDir/$jarName"
    }
}

Write-Host "🎉 Production JARs build complete!" -ForegroundColor Green
Write-Host "📁 JARs available in: $distDir" -ForegroundColor Cyan