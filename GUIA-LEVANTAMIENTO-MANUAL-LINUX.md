# 🚀 GUÍA DE LEVANTAMIENTO MANUAL - LINUX

## 🎯 **INTRODUCCIÓN**

Esta guía te permite levantar y probar cada microservicio de **Arka Valenzuela** manualmente en Linux, paso a paso, para entender el funcionamiento completo del sistema y realizar pruebas exhaustivas.

---

## 📋 **PREREQUISITOS**

### 🛠️ **Software Requerido**

```bash
# Verificar instalaciones
java -version          # Java 21+
gradle -version        # Gradle 8.0+
docker --version       # Docker 20.0+
docker-compose --version # Docker Compose 2.0+
curl --version         # cURL para pruebas
git --version          # Git
```

### 📦 **Instalación de Dependencias (Ubuntu/Debian)**

```bash
# Actualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar Java 21
sudo apt install openjdk-21-jdk -y

# Instalar Gradle
wget https://services.gradle.org/distributions/gradle-8.4-bin.zip
sudo unzip -d /opt/gradle gradle-8.4-bin.zip
echo 'export GRADLE_HOME=/opt/gradle/gradle-8.4' >> ~/.bashrc
echo 'export PATH=$PATH:$GRADLE_HOME/bin' >> ~/.bashrc
source ~/.bashrc

# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Herramientas adicionales
sudo apt install curl jq tree htop -y
```

---

## 🗄️ **PASO 1: LEVANTAR BASES DE DATOS**

### 🐬 **MySQL para Cotizaciones**

```bash
# Crear directorio para datos
mkdir -p ~/arka-data/mysql

# Levantar MySQL
docker run -d \
  --name arka-mysql \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=arka_cotizaciones \
  -e MYSQL_USER=arka_user \
  -e MYSQL_PASSWORD=arka_password \
  -p 3306:3306 \
  -v ~/arka-data/mysql:/var/lib/mysql \
  mysql:8.0

# Verificar que esté corriendo
docker ps | grep arka-mysql

# Probar conexión
mysql -h localhost -P 3306 -u arka_user -parka_password -e "SHOW DATABASES;"
```

### 🍃 **MongoDB para Solicitudes**

```bash
# Crear directorio para datos
mkdir -p ~/arka-data/mongodb

# Levantar MongoDB
docker run -d \
  --name arka-mongodb \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=admin123 \
  -e MONGO_INITDB_DATABASE=arka_solicitudes \
  -p 27017:27017 \
  -v ~/arka-data/mongodb:/data/db \
  mongo:7.0

# Verificar que esté corriendo
docker ps | grep arka-mongodb

# Probar conexión
docker exec -it arka-mongodb mongosh --host localhost:27017 -u admin -p admin123 --eval "db.adminCommand('ismaster')"
```

### 🔴 **Redis para Cache**

```bash
# Levantar Redis
docker run -d \
  --name arka-redis \
  -p 6379:6379 \
  redis:7-alpine

# Verificar que esté corriendo
docker ps | grep arka-redis

# Probar conexión
redis-cli ping
```

### ✅ **Verificar Todas las Bases de Datos**

```bash
echo "🔍 Verificando bases de datos..."
echo "MySQL: $(docker exec arka-mysql mysqladmin ping -h localhost -u root -prootpassword 2>/dev/null && echo '✅ OK' || echo '❌ FAIL')"
echo "MongoDB: $(docker exec arka-mongodb mongosh --host localhost:27017 -u admin -p admin123 --eval 'db.adminCommand("ping").ok' --quiet 2>/dev/null && echo '✅ OK' || echo '❌ FAIL')"
echo "Redis: $(redis-cli ping 2>/dev/null && echo '✅ OK' || echo '❌ FAIL')"
```

---

## 🌐 **PASO 2: SERVICIOS DE INFRAESTRUCTURA**

### 📋 **Eureka Server (Service Discovery)**

```bash
# Ir al directorio del proyecto
cd ~/arkavalenzuela-2

# Compilar Eureka Server
echo "🔨 Compilando Eureka Server..."
./gradlew :eureka-server:clean :eureka-server:build -x test

# Levantar Eureka Server
echo "🚀 Iniciando Eureka Server en puerto 8761..."
cd eureka-server
nohup java -jar build/libs/eureka-server-*.jar \
  --spring.profiles.active=local \
  --server.port=8761 \
  --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ \
  > ../logs/eureka-server.log 2>&1 &

# Guardar PID
echo $! > ../pids/eureka-server.pid

cd ..

# Esperar a que inicie
echo "⏳ Esperando que Eureka inicie..."
sleep 30

# Verificar estado
curl -s http://localhost:8761/actuator/health | jq .status
echo "🌐 Eureka Dashboard: http://localhost:8761"
```

### ⚙️ **Config Server (Configuración Centralizada)**

```bash
# Compilar Config Server
echo "🔨 Compilando Config Server..."
./gradlew :config-server:clean :config-server:build -x test

# Levantar Config Server
echo "🚀 Iniciando Config Server en puerto 8888..."
cd config-server
nohup java -jar build/libs/config-server-*.jar \
  --spring.profiles.active=local \
  --server.port=8888 \
  --spring.cloud.config.server.git.uri=file://$PWD/../config-repository \
  --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ \
  > ../logs/config-server.log 2>&1 &

# Guardar PID
echo $! > ../pids/config-server.pid

cd ..

# Esperar a que inicie
echo "⏳ Esperando que Config Server inicie..."
sleep 20

# Verificar estado
curl -s http://localhost:8888/actuator/health | jq .status
echo "⚙️ Config Server: http://localhost:8888"

# Probar configuración
echo "🧪 Probando configuración:"
curl -s http://localhost:8888/api-gateway/default | jq .
```

---

## 🚪 **PASO 3: API GATEWAY**

```bash
# Compilar API Gateway
echo "🔨 Compilando API Gateway..."
./gradlew :api-gateway:clean :api-gateway:build -x test

# Levantar API Gateway
echo "🚀 Iniciando API Gateway en puerto 8080..."
cd api-gateway
nohup java -jar build/libs/api-gateway-*.jar \
  --spring.profiles.active=local \
  --server.port=8080 \
  --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ \
  --spring.cloud.config.uri=http://localhost:8888 \
  --spring.datasource.url=jdbc:mysql://localhost:3306/arka_cotizaciones \
  --spring.datasource.username=arka_user \
  --spring.datasource.password=arka_password \
  --spring.redis.host=localhost \
  --spring.redis.port=6379 \
  > ../logs/api-gateway.log 2>&1 &

# Guardar PID
echo $! > ../pids/api-gateway.pid

cd ..

# Esperar a que inicie
echo "⏳ Esperando que API Gateway inicie..."
sleep 25

# Verificar estado
curl -s http://localhost:8080/actuator/health | jq .status
echo "🚪 API Gateway: http://localhost:8080"

# Verificar registro en Eureka
curl -s http://localhost:8761/eureka/apps | grep -q "API-GATEWAY" && echo "✅ Registrado en Eureka" || echo "❌ No registrado"
```

---

## 🏢 **PASO 4: MICROSERVICIOS DE NEGOCIO**

### 💰 **Arca Cotizador**

```bash
# Compilar Arca Cotizador
echo "🔨 Compilando Arca Cotizador..."
./gradlew :arca-cotizador:clean :arca-cotizador:build -x test

# Levantar Arca Cotizador
echo "🚀 Iniciando Arca Cotizador en puerto 8081..."
cd arca-cotizador
nohup java -jar build/libs/arca-cotizador-*.jar \
  --spring.profiles.active=local \
  --server.port=8081 \
  --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ \
  --spring.cloud.config.uri=http://localhost:8888 \
  --spring.datasource.url=jdbc:mysql://localhost:3306/arka_cotizaciones \
  --spring.datasource.username=arka_user \
  --spring.datasource.password=arka_password \
  --spring.redis.host=localhost \
  --spring.redis.port=6379 \
  > ../logs/arca-cotizador.log 2>&1 &

# Guardar PID
echo $! > ../pids/arca-cotizador.pid

cd ..

# Esperar a que inicie
echo "⏳ Esperando que Arca Cotizador inicie..."
sleep 20

# Verificar estado
curl -s http://localhost:8081/actuator/health | jq .status
echo "💰 Arca Cotizador: http://localhost:8081"
```

### 📋 **Arca Gestor Solicitudes**

```bash
# Compilar Arca Gestor Solicitudes
echo "🔨 Compilando Arca Gestor Solicitudes..."
./gradlew :arca-gestor-solicitudes:clean :arca-gestor-solicitudes:build -x test

# Levantar Arca Gestor Solicitudes
echo "🚀 Iniciando Arca Gestor Solicitudes en puerto 8082..."
cd arca-gestor-solicitudes
nohup java -jar build/libs/arca-gestor-solicitudes-*.jar \
  --spring.profiles.active=local \
  --server.port=8082 \
  --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ \
  --spring.cloud.config.uri=http://localhost:8888 \
  --spring.data.mongodb.uri=mongodb://admin:admin123@localhost:27017/arka_solicitudes?authSource=admin \
  --spring.redis.host=localhost \
  --spring.redis.port=6379 \
  > ../logs/arca-gestor-solicitudes.log 2>&1 &

# Guardar PID
echo $! > ../pids/arca-gestor-solicitudes.pid

cd ..

# Esperar a que inicie
echo "⏳ Esperando que Arca Gestor Solicitudes inicie..."
sleep 20

# Verificar estado
curl -s http://localhost:8082/actuator/health | jq .status
echo "📋 Arca Gestor Solicitudes: http://localhost:8082"
```

### 👋 **Hello World Service**

```bash
# Compilar Hello World Service
echo "🔨 Compilando Hello World Service..."
./gradlew :hello-world-service:clean :hello-world-service:build -x test

# Levantar Hello World Service
echo "🚀 Iniciando Hello World Service en puerto 8083..."
cd hello-world-service
nohup java -jar build/libs/hello-world-service-*.jar \
  --spring.profiles.active=local \
  --server.port=8083 \
  --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/ \
  --spring.cloud.config.uri=http://localhost:8888 \
  > ../logs/hello-world-service.log 2>&1 &

# Guardar PID
echo $! > ../pids/hello-world-service.pid

cd ..

# Esperar a que inicie
echo "⏳ Esperando que Hello World Service inicie..."
sleep 15

# Verificar estado
curl -s http://localhost:8083/actuator/health | jq .status
echo "👋 Hello World Service: http://localhost:8083"
```

---

## ✅ **PASO 5: VERIFICACIÓN COMPLETA DEL SISTEMA**

### 🔍 **Script de Verificación General**

```bash
#!/bin/bash
# 📁 verify-system.sh

echo "🔍 VERIFICANDO SISTEMA ARKA VALENZUELA"
echo "======================================"

# Función para verificar servicio
check_service() {
    local name=$1
    local url=$2
    local expected_status=${3:-"UP"}
    
    echo -n "Verificando $name... "
    
    response=$(curl -s "$url" 2>/dev/null)
    if [ $? -eq 0 ]; then
        status=$(echo "$response" | jq -r '.status' 2>/dev/null)
        if [ "$status" = "$expected_status" ]; then
            echo "✅ OK"
            return 0
        else
            echo "❌ FAIL (Status: $status)"
            return 1
        fi
    else
        echo "❌ UNREACHABLE"
        return 1
    fi
}

# Verificar bases de datos
echo "📊 BASES DE DATOS:"
echo -n "MySQL... "
docker exec arka-mysql mysqladmin ping -h localhost -u root -prootpassword >/dev/null 2>&1 && echo "✅ OK" || echo "❌ FAIL"

echo -n "MongoDB... "
docker exec arka-mongodb mongosh --host localhost:27017 -u admin -p admin123 --eval 'db.adminCommand("ping").ok' --quiet >/dev/null 2>&1 && echo "✅ OK" || echo "❌ FAIL"

echo -n "Redis... "
redis-cli ping >/dev/null 2>&1 && echo "✅ OK" || echo "❌ FAIL"

echo ""
echo "🏗️ SERVICIOS DE INFRAESTRUCTURA:"
check_service "Eureka Server" "http://localhost:8761/actuator/health"
check_service "Config Server" "http://localhost:8888/actuator/health"

echo ""
echo "🌐 API GATEWAY:"
check_service "API Gateway" "http://localhost:8080/actuator/health"

echo ""
echo "🏢 MICROSERVICIOS:"
check_service "Arca Cotizador" "http://localhost:8081/actuator/health"
check_service "Arca Gestor Solicitudes" "http://localhost:8082/actuator/health"
check_service "Hello World Service" "http://localhost:8083/actuator/health"

echo ""
echo "🌐 VERIFICANDO EUREKA REGISTRATIONS:"
registered_services=$(curl -s http://localhost:8761/eureka/apps | grep -o '<name>[^<]*</name>' | sed 's/<[^>]*>//g' | sort | uniq)
echo "Servicios registrados:"
echo "$registered_services" | while read service; do
    [ -n "$service" ] && echo "  ✅ $service"
done

echo ""
echo "🎯 ENDPOINTS PRINCIPALES:"
echo "  🌐 Eureka Dashboard: http://localhost:8761"
echo "  ⚙️ Config Server: http://localhost:8888"
echo "  🚪 API Gateway: http://localhost:8080"
echo "  💰 Cotizador Direct: http://localhost:8081"
echo "  📋 Gestor Direct: http://localhost:8082"
echo "  👋 Hello World Direct: http://localhost:8083"
```

```bash
# Hacer ejecutable y correr
chmod +x verify-system.sh
./verify-system.sh
```

---

## 🧪 **PASO 6: PRUEBAS FUNCIONALES**

### 🔐 **Pruebas de Autenticación**

```bash
echo "🔐 PROBANDO AUTENTICACIÓN"
echo "========================"

# 1. Registrar usuario
echo "📝 Registrando nuevo usuario..."
register_response=$(curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@arka.com",
    "password": "Test123!",
    "firstName": "Test",
    "lastName": "User"
  }')

echo "Respuesta registro: $register_response"

# 2. Login
echo "🔑 Haciendo login..."
login_response=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!"
  }')

echo "Respuesta login: $login_response"

# Extraer token
token=$(echo $login_response | jq -r '.token')
echo "🎫 Token obtenido: ${token:0:50}..."

# 3. Verificar token
echo "✅ Verificando token..."
verify_response=$(curl -s -X GET http://localhost:8080/auth/verify \
  -H "Authorization: Bearer $token")

echo "Verificación: $verify_response"
```

### 💰 **Pruebas de Cotizaciones**

```bash
echo "💰 PROBANDO COTIZACIONES"
echo "======================="

# 1. Crear cotización
echo "📄 Creando nueva cotización..."
cotizacion_response=$(curl -s -X POST http://localhost:8080/api/cotizaciones \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d '{
    "cliente": {
      "nombre": "Juan Pérez",
      "email": "juan@email.com",
      "telefono": "123456789"
    },
    "productos": [
      {
        "nombre": "Laptop Gaming",
        "cantidad": 1,
        "precio": 1500.00
      },
      {
        "nombre": "Mouse Gaming",
        "cantidad": 2,
        "precio": 50.00
      }
    ],
    "descuento": 10.0
  }')

echo "Cotización creada: $cotizacion_response"

# Extraer ID de cotización
cotizacion_id=$(echo $cotizacion_response | jq -r '.id')

# 2. Obtener cotización
echo "📖 Obteniendo cotización $cotizacion_id..."
get_cotizacion=$(curl -s -X GET http://localhost:8080/api/cotizaciones/$cotizacion_id \
  -H "Authorization: Bearer $token")

echo "Cotización obtenida: $get_cotizacion"

# 3. Listar todas las cotizaciones
echo "📋 Listando todas las cotizaciones..."
list_cotizaciones=$(curl -s -X GET "http://localhost:8080/api/cotizaciones?page=0&size=10" \
  -H "Authorization: Bearer $token")

echo "Lista de cotizaciones: $list_cotizaciones"

# 4. Actualizar cotización
echo "✏️ Actualizando cotización..."
update_response=$(curl -s -X PUT http://localhost:8080/api/cotizaciones/$cotizacion_id \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d '{
    "cliente": {
      "nombre": "Juan Pérez Actualizado",
      "email": "juan.actualizado@email.com",
      "telefono": "987654321"
    },
    "productos": [
      {
        "nombre": "Laptop Gaming Pro",
        "cantidad": 1,
        "precio": 1800.00
      }
    ],
    "descuento": 15.0
  }')

echo "Cotización actualizada: $update_response"
```

### 📋 **Pruebas de Solicitudes**

```bash
echo "📋 PROBANDO SOLICITUDES"
echo "======================"

# 1. Crear solicitud
echo "📝 Creando nueva solicitud..."
solicitud_response=$(curl -s -X POST http://localhost:8080/api/solicitudes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d '{
    "titulo": "Solicitud de Compra Laptops",
    "descripcion": "Necesitamos 10 laptops para el equipo de desarrollo",
    "categoria": "TECNOLOGIA",
    "prioridad": "ALTA",
    "solicitante": {
      "nombre": "María García",
      "email": "maria@company.com",
      "departamento": "IT"
    },
    "items": [
      {
        "descripcion": "Laptop Dell XPS 13",
        "cantidad": 10,
        "presupuestoEstimado": 15000.00
      }
    ]
  }')

echo "Solicitud creada: $solicitud_response"

# Extraer ID de solicitud
solicitud_id=$(echo $solicitud_response | jq -r '.id')

# 2. Obtener solicitud
echo "📖 Obteniendo solicitud $solicitud_id..."
get_solicitud=$(curl -s -X GET http://localhost:8080/api/solicitudes/$solicitud_id \
  -H "Authorization: Bearer $token")

echo "Solicitud obtenida: $get_solicitud"

# 3. Cambiar estado de solicitud
echo "🔄 Cambiando estado a EN_REVISION..."
status_response=$(curl -s -X PUT http://localhost:8080/api/solicitudes/$solicitud_id/estado \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d '{
    "nuevoEstado": "EN_REVISION",
    "comentario": "Solicitud recibida y en proceso de revisión"
  }')

echo "Estado actualizado: $status_response"

# 4. Listar solicitudes por estado
echo "📋 Listando solicitudes en revisión..."
list_solicitudes=$(curl -s -X GET "http://localhost:8080/api/solicitudes?estado=EN_REVISION&page=0&size=10" \
  -H "Authorization: Bearer $token")

echo "Lista de solicitudes: $list_solicitudes"
```

### 👋 **Pruebas de Hello World**

```bash
echo "👋 PROBANDO HELLO WORLD SERVICE"
echo "==============================="

# 1. Saludo simple
echo "👋 Probando saludo simple..."
hello_response=$(curl -s -X GET http://localhost:8080/api/hello \
  -H "Authorization: Bearer $token")

echo "Respuesta: $hello_response"

# 2. Saludo personalizado
echo "👤 Probando saludo personalizado..."
hello_name_response=$(curl -s -X GET "http://localhost:8080/api/hello/Juan" \
  -H "Authorization: Bearer $token")

echo "Respuesta personalizada: $hello_name_response"

# 3. Información del servicio
echo "ℹ️ Información del servicio..."
info_response=$(curl -s -X GET http://localhost:8080/api/hello/info \
  -H "Authorization: Bearer $token")

echo "Info del servicio: $info_response"
```

### 🔄 **Pruebas de Programación Reactiva**

```bash
echo "🔄 PROBANDO ENDPOINTS REACTIVOS"
echo "==============================="

# 1. Múltiples llamadas asíncronas
echo "⚡ Probando múltiples llamadas asíncronas..."
reactive_response=$(curl -s -X GET http://localhost:8080/api/reactive/multiple-calls \
  -H "Authorization: Bearer $token")

echo "Respuesta reactiva: $reactive_response"

# 2. Stream de datos
echo "📡 Probando stream de datos (primeros 5 segundos)..."
timeout 5 curl -s -X GET http://localhost:8080/api/reactive/stream \
  -H "Authorization: Bearer $token" \
  -H "Accept: text/plain" || echo "Stream terminado"

# 3. Backpressure test
echo "🌊 Probando manejo de backpressure..."
backpressure_response=$(curl -s -X GET http://localhost:8080/api/reactive/backpressure-test \
  -H "Authorization: Bearer $token")

echo "Respuesta backpressure: $backpressure_response"
```

---

## 📊 **PASO 7: MONITOREO Y MÉTRICAS**

### 📈 **Verificar Actuator Endpoints**

```bash
echo "📊 VERIFICANDO MÉTRICAS Y HEALTH CHECKS"
echo "======================================="

services=("8761:eureka-server" "8888:config-server" "8080:api-gateway" "8081:arca-cotizador" "8082:arca-gestor-solicitudes" "8083:hello-world-service")

for service in "${services[@]}"; do
    port=$(echo $service | cut -d: -f1)
    name=$(echo $service | cut -d: -f2)
    
    echo "📊 $name (puerto $port):"
    echo "  Health: $(curl -s http://localhost:$port/actuator/health | jq -r '.status')"
    echo "  Info: $(curl -s http://localhost:$port/actuator/info | jq -r '.app.name // "N/A"')"
    echo "  Metrics available: $(curl -s http://localhost:$port/actuator/metrics | jq -r '.names | length') metrics"
    echo ""
done
```

### 🎯 **Métricas Específicas**

```bash
echo "🎯 MÉTRICAS ESPECÍFICAS"
echo "======================"

# JVM Memory
echo "💾 Memoria JVM API Gateway:"
curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq '.measurements[0].value'

# HTTP Requests
echo "🌐 Total HTTP Requests API Gateway:"
curl -s http://localhost:8080/actuator/metrics/http.server.requests | jq '.measurements[0].value'

# Database Connections
echo "🗄️ Conexiones DB Arca Cotizador:"
curl -s http://localhost:8081/actuator/metrics/hikaricp.connections.active | jq '.measurements[0].value'

# Custom Business Metrics (si están implementadas)
echo "💼 Métricas de Negocio:"
curl -s http://localhost:8080/actuator/prometheus | grep arka_business || echo "No hay métricas personalizadas aún"
```

---

## 🛠️ **SCRIPTS DE UTILIDAD**

### 🚀 **Script de Inicio Completo**

```bash
#!/bin/bash
# 📁 start-all-manual.sh

set -e

echo "🚀 INICIANDO ARKA VALENZUELA - MODO MANUAL"
echo "=========================================="

# Crear directorios necesarios
mkdir -p logs pids

# 1. Bases de datos
echo "🗄️ Iniciando bases de datos..."
./start-databases.sh

# 2. Compilar todos los servicios
echo "🔨 Compilando todos los servicios..."
./gradlew clean build -x test

# 3. Servicios de infraestructura
echo "🏗️ Iniciando servicios de infraestructura..."
./start-eureka.sh
sleep 30
./start-config-server.sh
sleep 20

# 4. API Gateway
echo "🚪 Iniciando API Gateway..."
./start-api-gateway.sh
sleep 25

# 5. Microservicios
echo "🏢 Iniciando microservicios..."
./start-cotizador.sh &
./start-gestor-solicitudes.sh &
./start-hello-world.sh &

# Esperar que todos inicien
sleep 30

# 6. Verificar sistema
echo "✅ Verificando sistema..."
./verify-system.sh

echo "🎉 ¡Sistema Arka Valenzuela iniciado completamente!"
echo "🌐 Accede a http://localhost:8080 para comenzar"
```

### 🛑 **Script de Parada**

```bash
#!/bin/bash
# 📁 stop-all.sh

echo "🛑 DETENIENDO ARKA VALENZUELA"
echo "============================="

# Matar procesos Java
echo "🔫 Deteniendo servicios Java..."
if [ -d "pids" ]; then
    for pidfile in pids/*.pid; do
        if [ -f "$pidfile" ]; then
            pid=$(cat "$pidfile")
            service_name=$(basename "$pidfile" .pid)
            echo "  Deteniendo $service_name (PID: $pid)..."
            kill -TERM "$pid" 2>/dev/null || echo "    Ya estaba detenido"
            rm "$pidfile"
        fi
    done
fi

# Detener contenedores Docker
echo "🐳 Deteniendo contenedores Docker..."
docker stop arka-mysql arka-mongodb arka-redis 2>/dev/null || echo "  Contenedores ya detenidos"
docker rm arka-mysql arka-mongodb arka-redis 2>/dev/null || echo "  Contenedores ya removidos"

echo "✅ Sistema detenido completamente"
```

### 🔄 **Script de Restart**

```bash
#!/bin/bash
# 📁 restart-all.sh

echo "🔄 REINICIANDO ARKA VALENZUELA"
echo "=============================="

./stop-all.sh
sleep 5
./start-all-manual.sh
```

### 📊 **Script de Logs**

```bash
#!/bin/bash
# 📁 tail-logs.sh

service=${1:-"all"}

if [ "$service" = "all" ]; then
    echo "📊 LOGS DE TODOS LOS SERVICIOS"
    echo "=============================="
    tail -f logs/*.log
else
    echo "📊 LOGS DE $service"
    echo "=================="
    tail -f logs/$service.log
fi
```

---

## 🏆 **VALIDACIÓN FINAL**

### ✅ **Checklist de Verificación**

```bash
#!/bin/bash
# 📁 final-validation.sh

echo "🏆 VALIDACIÓN FINAL DEL SISTEMA"
echo "==============================="

all_good=true

# Función de verificación
verify() {
    local test_name="$1"
    local command="$2"
    echo -n "✓ $test_name... "
    if eval "$command" >/dev/null 2>&1; then
        echo "✅ PASS"
    else
        echo "❌ FAIL"
        all_good=false
    fi
}

# Tests de conectividad
verify "MySQL conectividad" "mysql -h localhost -P 3306 -u arka_user -parka_password -e 'SELECT 1'"
verify "MongoDB conectividad" "docker exec arka-mongodb mongosh --host localhost:27017 -u admin -p admin123 --eval 'db.adminCommand(\"ping\")' --quiet"
verify "Redis conectividad" "redis-cli ping"

# Tests de servicios
verify "Eureka Server health" "curl -f http://localhost:8761/actuator/health"
verify "Config Server health" "curl -f http://localhost:8888/actuator/health"
verify "API Gateway health" "curl -f http://localhost:8080/actuator/health"
verify "Arca Cotizador health" "curl -f http://localhost:8081/actuator/health"
verify "Arca Gestor health" "curl -f http://localhost:8082/actuator/health"
verify "Hello World health" "curl -f http://localhost:8083/actuator/health"

# Tests funcionales
verify "Login endpoint" "curl -f -X POST http://localhost:8080/auth/login -H 'Content-Type: application/json' -d '{\"username\":\"admin\",\"password\":\"admin\"}'"
verify "Eureka apps listing" "curl -f http://localhost:8761/eureka/apps"
verify "Config server config" "curl -f http://localhost:8888/api-gateway/default"

if [ "$all_good" = true ]; then
    echo ""
    echo "🎉 ¡VALIDACIÓN EXITOSA!"
    echo "El sistema Arka Valenzuela está funcionando correctamente."
    echo ""
    echo "🌐 URLs principales:"
    echo "  • Eureka: http://localhost:8761"
    echo "  • API Gateway: http://localhost:8080"
    echo "  • Swagger UI: http://localhost:8080/swagger-ui.html"
else
    echo ""
    echo "❌ VALIDACIÓN FALLÓ"
    echo "Revisa los logs para más detalles."
    exit 1
fi
```

---

## 📚 **COMANDOS DE REFERENCIA RÁPIDA**

```bash
# 🚀 Inicio rápido
./start-all-manual.sh

# 🛑 Parar todo
./stop-all.sh

# 🔄 Reiniciar
./restart-all.sh

# 📊 Ver logs
./tail-logs.sh [servicio]

# ✅ Verificar sistema
./verify-system.sh

# 🏆 Validación final
./final-validation.sh

# 🧪 Ejecutar pruebas
./run-functional-tests.sh

# 📊 Ver métricas
curl http://localhost:8080/actuator/metrics

# 🌐 Eureka dashboard
open http://localhost:8761

# 🚪 API Gateway
open http://localhost:8080
```

---

*Guía de Levantamiento Manual en Linux*  
*Proyecto: Arka Valenzuela*  
*Documentación completa paso a paso*  
*Fecha: 8 de Septiembre de 2025*
