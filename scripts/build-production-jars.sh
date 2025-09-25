#!/bin/bash
# 🏗️ ARKA Production JAR Builder
# Script para construir todos los JARs de producción para despliegue en Amazon Linux

set -e  # Exit on any error

echo "🚀 Building ARKA Production JARs..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Limpiar builds anteriores
echo -e "${YELLOW}🧹 Cleaning previous builds...${NC}"
./gradlew clean

# Construir todos los JARs
echo -e "${YELLOW}🔨 Building all production JARs...${NC}"
./gradlew bootJar

# Verificar que todos los JARs se crearon correctamente
jars=(
    "config-server/build/libs/config-server.jar"
    "eureka-server/build/libs/eureka-server.jar"
    "api-gateway/build/libs/api-gateway.jar"
    "arca-cotizador/build/libs/arca-cotizador.jar"
    "arca-gestor-solicitudes/build/libs/arca-gestor-solicitudes.jar"
    "hello-world-service/build/libs/hello-world-service.jar"
)

echo -e "${GREEN}✅ Checking generated JARs:${NC}"
for jar in "${jars[@]}"; do
    if [[ -f "$jar" ]]; then
        size=$(du -h "$jar" | cut -f1)
        echo -e "  ${GREEN}✓ $jar - $size${NC}"
    else
        echo -e "  ${RED}✗ $jar - NOT FOUND!${NC}"
    fi
done

# Crear directorio de distribución
dist_dir="dist/jars"
mkdir -p "$dist_dir"

# Copiar JARs al directorio de distribución
echo -e "${YELLOW}📦 Copying JARs to distribution directory...${NC}"
for jar in "${jars[@]}"; do
    if [[ -f "$jar" ]]; then
        jar_name=$(basename "$jar")
        cp "$jar" "$dist_dir/$jar_name"
    fi
done

echo -e "${GREEN}🎉 Production JARs build complete!${NC}"
echo -e "${CYAN}📁 JARs available in: $dist_dir${NC}"