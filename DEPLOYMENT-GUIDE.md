# 🚀 ARKA Production Deployment Guide

Este documento describe cómo desplegar los microservicios ARKA en producción usando **Jenkins CI/CD** con **gradle build** y **WildFly JAR deployment**.

## 📋 Índice

- [Jenkins + Gradle + WildFly JAR Deployment (Recomendado)](#jenkins-jar-deployment)
- [Opción Alternativa: Despliegue Directo con JARs](#direct-jar-deployment)
- [Opción Enterprise: Despliegue con WildFly WAR](#wildfly-war-deployment)
- [Jenkins Pipeline Configuration](#jenkins-configuration)
- [Monitoreo y Gestión](#monitoring)

---

## 🎯 Jenkins + Gradle + WildFly JAR Deployment {#jenkins-jar-deployment}

### 🏭 Arquitectura del Despliegue

Esta configuración usa Jenkins para ejecutar `gradle build`, generar JARs ejecutables, y desplegarlos en WildFly como servicios independientes. **Funciona exactamente igual que `gradle bootRun`** pero está optimizado para producción.

```
Jenkins Pipeline → gradle build → JARs → WildFly JAR Deployment → Health Checks
```

### Prerrequisitos

#### 🔧 Sistema Base
- **Amazon Linux 2** o compatible
- **Java 21** (Amazon Corretto)
- **WildFly 31+** instalado en `/opt/wildfly`
- **Jenkins** con plugins: Pipeline, GitHub, Test Results

#### 🗄️ Base de Datos AWS
- **MySQL RDS**: `172.31.48.25:3306/arka-base`
- **DocumentDB**: `arka-docdb-cluster.cluster-ch0z9qzpkrpq.us-east-1.docdb.amazonaws.com:27017/arka-notifications`
- **S3 Bucket**: `arka-s3-bucket2`

### 🔨 Configuración Inicial

#### 1. Configurar WildFly para JARs

```bash
# Ejecutar configuración automática de WildFly
./scripts/setup-wildfly-jars.sh --action configure --wildfly-home /opt/wildfly

# Validar configuración
./scripts/setup-wildfly-jars.sh --action validate
```

#### 2. Verificar Build Gradle

Los `build.gradle` están configurados automáticamente para generar JARs ejecutables:

```gradle
bootJar {
    enabled = true
    archiveFileName = 'service-name.jar'
    launchScript()  // Hace el JAR ejecutable como script
}

springBoot {
    buildInfo()     // Información de build para monitoreo
}
```

### 🚀 Proceso de Despliegue

#### Opción A: Jenkins Pipeline (Recomendado)

1. **Crear Job en Jenkins**:
   ```groovy
   // Usar Jenkinsfile-jars-wildfly
   pipeline {
       agent any
       // ... configuración completa en el archivo
   }
   ```

2. **Configurar Variables**:
   - `WILDFLY_HOME`: `/opt/wildfly`
   - `SPRING_PROFILE`: `aws`
   - `JAVA_HOME`: `/usr/lib/jvm/java-21-amazon-corretto`

3. **Ejecutar Pipeline**:
   - Jenkins ejecuta `gradle build`
   - Genera JARs en `build/libs/`
   - Copia JARs a `dist/jars/`
   - Usa `deploy-jars-wildfly.sh` para desplegar
   - Ejecuta health checks automáticos

#### Opción B: Despliegue Manual

```bash
# 1. Build con gradle
./gradlew clean build

# 2. Desplegar JARs en WildFly
./scripts/deploy-jars-wildfly.sh --action deploy --profile aws

# 3. Verificar estado
./scripts/deploy-jars-wildfly.sh --action status
```

### 📦 Servicios y Configuración

| Servicio | JAR | Puerto | Tipo | JVM Memory | Health Check |
|----------|-----|--------|------|-----------|--------------|
| Config Server | `config-server.jar` | 8888 | Infrastructure | 512m-1024m | `/actuator/health` |
| Eureka Server | `eureka-server.jar` | 8761 | Infrastructure | 512m-1024m | `/actuator/health` |
| API Gateway | `api-gateway.jar` | 8085 | Gateway | 256m-512m | `/actuator/health` |
| Arca Cotizador | `arca-cotizador.jar` | 8081 | Business | 256m-512m | `/actuator/health` |
| Gestor Solicitudes | `arca-gestor-solicitudes.jar` | 8082 | Business | 256m-512m | `/actuator/health` |
| Hello World | `hello-world-service.jar` | 8083 | Business | 256m-512m | `/actuator/health` |

### 🔄 Gestión de Servicios

```bash
# Ver estado completo
./scripts/deploy-jars-wildfly.sh --action status

# Reiniciar todos los servicios
./scripts/deploy-jars-wildfly.sh --action restart

# Detener todos los servicios
./scripts/deploy-jars-wildfly.sh --action stop

# Monitoreo de puertos
./scripts/port-monitor.sh
```

### 📊 Características del Despliegue

#### ✅ Ventajas sobre bootRun:
- **Producción Ready**: JVMs optimizadas, GC tunning
- **Health Checks**: Verificación automática de servicios
- **Process Management**: PIDs, logs, reinicio automático
- **Startup Ordering**: Servicios inician en orden correcto
- **Resource Management**: Límites de memoria por servicio
- **Monitoring**: JMX, Flight Recorder, GC logging
- **Service Discovery**: Eureka registration automático

#### 🏭 Optimizaciones WildFly:
- **Port Offset**: WildFly usa puerto 8180 (8080+100)
- **Independent JARs**: Cada servicio corre como proceso independiente
- **Shared Resources**: Logs centralizados, configuración compartida
- **Zero Downtime**: Rolling deployments disponibles

---

## 🎯 Opción Alternativa: Despliegue Directo con JARs {#direct-jar-deployment}

Para entornos sin WildFly, usar despliegue directo:

### 🔨 Build de Producción

```bash
# Amazon Linux
./scripts/build-production-jars.sh

# Windows (PowerShell)
.\scripts\build-production-jars.ps1
```

### 🚀 Despliegue AWS Directo

```bash
# Amazon Linux
./scripts/deploy-aws-production.sh --action deploy --profile aws

# Windows (PowerShell)
.\scripts\deploy-aws-production.ps1 -Action deploy
```

### 🔗 Endpoints de Producción

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8085
- **Config Server**: http://localhost:8888/actuator/health
- **Cotizador API**: http://localhost:8081/actuator/health
- **Gestor API**: http://localhost:8082/actuator/health

---

## 🐺 Opción 2: Despliegue con WildFly {#wildfly-deployment}

### Prerrequisitos
- **WildFly 31.0.0** instalado en `C:\wildfly\wildfly-31.0.0.Final`
- **Java 21** configurado
- **Perfil AWS** configurado

### 🔧 Configuración WAR

```powershell
# Configurar soporte WAR para servicios de negocio
.\scripts\configure-war-support.ps1
```

### 🚀 Despliegue WildFly

```powershell
# Desplegar en WildFly
.\scripts\deploy-wildfly.ps1 -Action deploy -Profile aws

# Ver estado
.\scripts\deploy-wildfly.ps1 -Action status

# Detener
.\scripts\deploy-wildfly.ps1 -Action stop
```

### 📦 Arquitectura WildFly

**Servicios de Infraestructura** (JARs independientes):
- Config Server (8888)
- Eureka Server (8761)

**Servicios de Negocio** (WARs en WildFly):
- API Gateway → http://localhost:8080/gateway
- Arca Cotizador → http://localhost:8080/cotizador  
- Gestor Solicitudes → http://localhost:8080/gestor

---

## 🏗️ Jenkins Pipeline Configuration {#jenkins-configuration}

### Jenkinsfile Simple (Recomendado)

Usa `Jenkinsfile-aws-simple` para despliegue básico:

```groovy
pipeline {
    agent any
    environment {
        AWS_PROFILE = 'aws'
    }
    stages {
        stage('Build JARs') {
            steps {
                bat 'gradlew clean bootJar'
            }
        }
        stage('Deploy AWS') {
            steps {
                bat 'powershell -File "scripts/deploy-aws-production.ps1" -Action deploy'
            }
        }
    }
}
```

---

## 🐺 Opción Enterprise: Despliegue con WildFly WAR {#wildfly-war-deployment}

Para despliegues enterprise con WildFly usando WARs:

### Prerrequisitos
- **WildFly 31+** instalado
- **Java 21** configurado

### 🔧 Configuración WAR

```bash
# Configurar soporte WAR para servicios de negocio
./scripts/configure-war-support.sh --action configure

# Verificar configuración
./scripts/configure-war-support.sh --action status
```

### 🚀 Despliegue WildFly WAR

```bash
# Desplegar en WildFly con WARs + JARs
./scripts/deploy-wildfly.sh --action deploy --profile aws
```

---

## 🏗️ Jenkins Pipeline Configuration {#jenkins-configuration}

### Configuración del Pipeline

#### 1. Crear Job en Jenkins

1. **Nuevo Item** → **Pipeline**
2. **Pipeline** → **Pipeline script from SCM**
3. **SCM**: Git
4. **Repository URL**: Tu repositorio
5. **Script Path**: `Jenkinsfile-jars-wildfly`

#### 2. Variables de Entorno

Configurar en Jenkins las siguientes variables:

```bash
# Sistema
WILDFLY_HOME=/opt/wildfly
JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
SPRING_PROFILE=aws

# Base de Datos
AWS_RDS_ENDPOINT=172.31.48.25
AWS_RDS_DATABASE=arka-base
AWS_DOCDB_ENDPOINT=arka-docdb-cluster.cluster-ch0z9qzpkrpq.us-east-1.docdb.amazonaws.com
AWS_S3_BUCKET=arka-s3-bucket2
```

#### 3. Credenciales Secretas

En Jenkins → **Manage Credentials**:
- `aws-rds-password`: `Temporal123*`
- `aws-docdb-password`: `Temporal123*`
- `config-client-password`: `arka-client-2025`

#### 4. Pipeline Stages

El `Jenkinsfile-jars-wildfly` incluye:

1. **Checkout**: Código fuente
2. **Environment Setup**: Verificación Java/Gradle
3. **Build & Test**: Paralelo
   - Compilación con `gradle build`
   - Unit tests
   - Code quality (Checkstyle, SpotBugs)
4. **Prepare JARs**: Copiar a `dist/jars/`
5. **Deploy to WildFly**: Usar `deploy-jars-wildfly.sh`
6. **Health Check**: Verificar servicios
7. **Smoke Tests**: Conectividad básica

#### 5. Triggers

```groovy
triggers {
    githubPush()                    // Build en push
    cron('H 2 * * *')              // Build nocturno
}
```

### Build Automático

El pipeline ejecuta automáticamente:

```bash
# 1. Gradle build
./gradlew clean build --parallel --build-cache

# 2. Despliegue JAR en WildFly
./scripts/deploy-jars-wildfly.sh --action deploy --profile aws --skip-build

# 3. Health checks
./scripts/deploy-jars-wildfly.sh --action status
```

### Notificaciones

Configurar Slack/Email en el pipeline:

```groovy
post {
    success {
        // slackSend(channel: '#deployments', color: 'good', message: '🎉 Deployment successful!')
    }
    failure {
        // slackSend(channel: '#deployments', color: 'danger', message: '❌ Deployment failed!')
    }
}
```

### Artifacts y Reportes

Jenkins archiva automáticamente:
- **JARs**: `**/build/libs/*.jar`
- **Test Results**: `**/build/test-results/**/*.xml`
- **Health Checks**: `health-check-results.json`
- **Logs**: `logs/*.log`

---

## 📊 Monitoreo y Gestión {#monitoring}

### Health Checks Automáticos

Todos los servicios exponen endpoints de salud que son verificados automáticamente:

```bash
# Verificar salud de todos los servicios
./scripts/deploy-jars-wildfly.sh --action status

# Health checks individuales
curl http://localhost:8888/actuator/health  # Config Server
curl http://localhost:8761/actuator/health  # Eureka
curl http://localhost:8085/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # Cotizador
curl http://localhost:8082/actuator/health  # Gestor Solicitudes
curl http://localhost:8083/actuator/health  # Hello World
```

### Monitoring Dashboard

El sistema incluye:
- **JMX Monitoring**: Puerto 9999+ por servicio
- **Flight Recorder**: Perfiles de performance
- **GC Logging**: Análisis de garbage collection
- **Port Monitoring**: Script de verificación de puertos

```bash
# Script de monitoreo completo
./scripts/port-monitor.sh

# Cron job automático (cada 5 minutos)
*/5 * * * * /opt/arka/scripts/monitoring-cron.sh
```

### Logs Centralizados

Los logs se almacenan organizadamente:
- **Servicios**: `/opt/arka/logs/service-name.log`
- **WildFly**: `/opt/wildfly/standalone/log/server.log`
- **Health Checks**: `/opt/arka/logs/health-check.log`
- **System Monitoring**: `/opt/arka/logs/cpu-usage.log`, `memory-usage.log`

### Gestión de Procesos

```bash
# Ver PIDs de todos los servicios
ls -la /opt/arka/pids/

# Gestión manual de servicios
sudo systemctl start wildfly-arka    # Iniciar WildFly
sudo systemctl status wildfly-arka   # Estado WildFly

# Reinicio rolling (uno por uno)
./scripts/deploy-jars-wildfly.sh --action restart
```

---

## 🔧 Troubleshooting

### Problemas Comunes

#### 1. **Puerto ocupado**
```bash
# Verificar qué usa el puerto
netstat -tulpn | grep :8085
lsof -ti:8085

# Solución automática en el script
./scripts/deploy-jars-wildfly.sh --action stop
```

#### 2. **Servicios no arrancan**
```bash
# Ver logs específicos
tail -f /opt/arka/logs/config-server.log

# Verificar dependencias
./scripts/deploy-jars-wildfly.sh --action status
```

#### 3. **Config Server 401/403**
Verificar autenticación:
- Usuario: `config-client`
- Password: `arka-client-2025`
- Perfil: `native,aws`

#### 4. **Jenkins Build Failures**
```bash
# Verificar espacio en disco
df -h

# Limpiar builds anteriores
./gradlew clean
rm -rf ~/.gradle/caches/

# Verificar permisos
chmod +x scripts/*.sh
```

#### 5. **Base de Datos Connection**
```bash
# Test MySQL RDS
mysql -h 172.31.48.25 -u admin -p arka-base

# Test DocumentDB
mongo --host arka-docdb-cluster.cluster-ch0z9qzpkrpq.us-east-1.docdb.amazonaws.com:27017 \
      --username admin --password
```

### Secuencia de Inicio Manual

Si el script automático falla:

```bash
# 1. Config Server (primero)
java -Xms512m -Xmx1024m -jar dist/jars/config-server.jar \
  --spring.profiles.active=native,aws \
  --server.port=8888

# 2. Eureka Server (segundo)
java -Xms512m -Xmx1024m -jar dist/jars/eureka-server.jar \
  --spring.profiles.active=aws \
  --server.port=8761

# 3. API Gateway (tercero)
java -Xms256m -Xmx512m -jar dist/jars/api-gateway.jar \
  --spring.profiles.active=aws \
  --server.port=8085

# 4. Servicios de negocio (paralelo)
java -Xms256m -Xmx512m -jar dist/jars/arca-cotizador.jar \
  --spring.profiles.active=aws --server.port=8081 &

java -Xms256m -Xmx512m -jar dist/jars/arca-gestor-solicitudes.jar \
  --spring.profiles.active=aws --server.port=8082 &

java -Xms256m -Xmx512m -jar dist/jars/hello-world-service.jar \
  --spring.profiles.active=aws --server.port=8083 &
```

### Debug Mode

Para troubleshooting avanzado:

```bash
# Habilitar debug logging
export ARKA_DEBUG_MODE=true

# Ejecutar con debug
./scripts/deploy-jars-wildfly.sh --action deploy --profile aws

# Ver logs en tiempo real
tail -f /opt/arka/logs/*.log
```

---

## 📝 Migración desde gradle bootRun

### Antes (Desarrollo)
```bash
# Cada servicio en terminal separada
./gradlew :config-server:bootRun --args='--spring.profiles.active=native,aws'
./gradlew :eureka-server:bootRun --args='--spring.profiles.active=aws'
./gradlew :api-gateway:bootRun --args='--spring.profiles.active=aws'
./gradlew :arca-cotizador:bootRun --args='--spring.profiles.active=aws'
./gradlew :arca-gestor-solicitudes:bootRun --args='--spring.profiles.active=aws'
./gradlew :hello-world-service:bootRun --args='--spring.profiles.active=aws'
```

### Después (Producción con Jenkins + WildFly)
```bash
# Jenkins Pipeline (automático)
Jenkins → Build → Deploy → Health Check

# O manual (equivale a bootRun pero optimizado)
./scripts/deploy-jars-wildfly.sh --action deploy --profile aws
```

### Comparación de Rendimiento

| Aspecto | gradle bootRun | WildFly JAR Deployment |
|---------|----------------|------------------------|
| **Inicio** | ~60s (secuencial) | ~45s (optimizado) |
| **Memoria** | ~3GB total | ~2.5GB total (tuneado) |
| **Monitoreo** | Manual | Automático |
| **Health Checks** | No | Sí |
| **Process Management** | No | PIDs, restart automático |
| **Production Ready** | No | Sí |

---

## ✅ Checklist de Despliegue

### Pre-despliegue
- [ ] **Amazon Linux** configurado con Java 21
- [ ] **WildFly** instalado y configurado
- [ ] **Jenkins** con plugins necesarios
- [ ] **Base de datos** MySQL RDS accesible
- [ ] **DocumentDB** configurado y accesible
- [ ] **S3 Bucket** configurado
- [ ] **Scripts** ejecutables y permisos correctos

### Despliegue
- [ ] **Jenkins Pipeline** configurado con `Jenkinsfile-jars-wildfly`
- [ ] **gradle build** exitoso
- [ ] **JARs** generados en `dist/jars/`
- [ ] **Config Server** iniciado y saludable
- [ ] **Eureka Server** iniciado y healthy
- [ ] **API Gateway** iniciado y registrado en Eureka
- [ ] **Servicios de negocio** iniciados y registrados

### Post-despliegue
- [ ] **Health checks** pasando en todos los servicios
- [ ] **Eureka Dashboard** mostrando servicios registrados
- [ ] **Endpoints API** respondiendo correctamente
- [ ] **Logs** generándose sin errores
- [ ] **Monitoring** funcionando (port-monitor, cron jobs)
- [ ] **Systemd service** habilitado y funcionando

---

## 🎉 ¡Listo para Producción!

Con esta configuración completa, tus 6 microservicios ARKA están listos para despliegue productivo usando:

1. **Jenkins CI/CD** para build automático
2. **gradle build** para generar JARs optimizados
3. **WildFly JAR Deployment** para ejecución igual a `bootRun` pero production-ready
4. **Health Checks** y monitoring automático
5. **Process management** con systemd y scripts de gestión

**El resultado es idéntico a `gradle bootRun` pero optimizado para producción enterprise.**