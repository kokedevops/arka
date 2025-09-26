# 🏗️ ARKA WildFly WAR Deployment Guide

## 📋 Resumen Ejecutivo

Esta guía describe el proceso completo para desplegar los microservicios ARKA en WildFly usando archivos WAR y un pipeline automatizado de Jenkins. La solución híbrida combina:

- **Servicios de infraestructura** (Eureka, Config Server) como JARs independientes
- **Servicios de negocio** (API Gateway, Cotizador, Gestor) como WARs en WildFly
- **Pipeline Jenkins** para automatización completa
- **Scripts PowerShell/Bash** para despliegue local y testing

## 🎯 Arquitectura de Despliegue

```
┌─────────────────────────────────────────────────────────────┐
│                    ARKA Deployment Architecture             │
├─────────────────────────────────────────────────────────────┤
│  Jenkins Pipeline                                           │
│  ├── Build Phase: Gradle → JARs + WARs                     │
│  ├── Infrastructure: SystemD Services (JARs)               │
│  └── Business Logic: WildFly Deployments (WARs)            │
├─────────────────────────────────────────────────────────────┤
│  Infrastructure Services (Port-based)                      │
│  ├── Eureka Server    → :8761 (JAR/SystemD)               │
│  └── Config Server    → :8888 (JAR/SystemD)               │
├─────────────────────────────────────────────────────────────┤
│  Business Services (WildFly Context-based)                 │
│  ├── API Gateway      → :8080/api-gateway                 │
│  ├── Arca Cotizador   → :8080/arca-cotizador             │
│  └── Gestor Solicitudes → :8080/arca-gestor-solicitudes   │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Configuración Inicial

### 1. Requisitos del Sistema

```bash
# Software requerido
- Java 21+ (Amazon Corretto recomendado)
- WildFly 31.0.0+ 
- Jenkins 2.400+
- Git
- cURL (para health checks)

# Verificar instalaciones
java -version
$WILDFLY_HOME/bin/jboss-cli.sh --version
jenkins --version
```

### 2. Configuración de WildFly

```bash
# Variables de ambiente
export WILDFLY_HOME="/opt/wildfly"
export WILDFLY_USER="wildfly"
export SPRING_PROFILE="aws"

# Aplicar configuración ARKA
$WILDFLY_HOME/bin/jboss-cli.sh --connect --file=scripts/wildfly-arka-config.cli

# Verificar configuración
$WILDFLY_HOME/bin/jboss-cli.sh --connect --command=":read-children-names(child-type=subsystem)"
```

### 3. Estructura de Archivos Generada

```
proyecto/
├── build.gradle                      # ✅ Configuración WAR habilitada
├── Jenkinsfile-wildfly-wars          # ✅ Pipeline Jenkins para WARs
├── scripts/
│   ├── wildfly-arka-config.cli      # ✅ Configuración WildFly
│   ├── deploy-wars-wildfly.sh       # ✅ Script Bash deployment
│   └── deploy-wars-wildfly.ps1      # ✅ Script PowerShell deployment
└── [microservicio]/
    ├── build.gradle                  # ✅ Configuración WAR específica
    ├── src/main/java/.../ServletInitializer.java  # ✅ WAR support class
    └── src/main/webapp/WEB-INF/jboss-deployment-structure.xml  # ✅ WildFly config
```

## 🔨 Proceso de Build

### 1. Build Local (Desarrollo)

```bash
# Linux/Mac
./scripts/deploy-wars-wildfly.sh --action build --skip-tests

# Windows
.\scripts\deploy-wars-wildfly.ps1 -Action build -SkipTests

# Verificar artefactos generados
ls -la */build/libs/*.{war,jar}
```

### 2. Build Jenkins (Producción)

```groovy
// Pipeline stages
stage('Build WARs') {
    steps {
        sh './gradlew :eureka-server:bootJar :config-server:bootJar'
        sh './gradlew :api-gateway:bootWar :arca-cotizador:bootWar :arca-gestor-solicitudes:bootWar'
    }
}
```

### 3. Artefactos Generados

| Servicio | Tipo | Archivo | Despliegue |
|----------|------|---------|------------|
| Eureka Server | JAR | `eureka-server.jar` | SystemD Service |
| Config Server | JAR | `config-server.jar` | SystemD Service |
| API Gateway | WAR | `api-gateway.war` | WildFly Deployment |
| Arca Cotizador | WAR | `arca-cotizador.war` | WildFly Deployment |
| Gestor Solicitudes | WAR | `arca-gestor-solicitudes.war` | WildFly Deployment |

## 🚀 Proceso de Despliegue

### 1. Despliegue Automatizado (Jenkins)

```bash
# Configurar pipeline Jenkins
1. Crear nuevo job Pipeline
2. Configurar SCM → Git → Branch: main
3. Pipeline script from SCM
4. Script Path: Jenkinsfile-wildfly-wars
5. Configurar credentials y variables de ambiente
6. Ejecutar build
```

**Variables de ambiente Jenkins:**
```properties
WILDFLY_HOME=/opt/wildfly
SPRING_PROFILE=aws
GRADLE_OPTS=-Xmx2g -Dorg.gradle.daemon=false
JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
DEPLOY_TIMEOUT=300
HEALTH_CHECK_TIMEOUT=180
SLACK_CHANNEL=#deployments
```

### 2. Despliegue Manual (Testing)

**Linux/Mac:**
```bash
# Despliegue completo
./scripts/deploy-wars-wildfly.sh --action full --profile aws

# Despliegue solo de un servicio
./scripts/deploy-wars-wildfly.sh --action deploy --service api-gateway

# Solo health checks
./scripts/deploy-wars-wildfly.sh --action health
```

**Windows:**
```powershell
# Despliegue completo
.\scripts\deploy-wars-wildfly.ps1 -Action full -Profile aws

# Despliegue solo de un servicio
.\scripts\deploy-wars-wildfly.ps1 -Action deploy -Service api-gateway

# Solo health checks
.\scripts\deploy-wars-wildfly.ps1 -Action health
```

### 3. Comandos WildFly CLI

```bash
# Ver deployments activos
$WILDFLY_CLI --connect --command="deployment-info"

# Undeploy específico
$WILDFLY_CLI --connect --command="undeploy api-gateway.war"

# Deploy manual
$WILDFLY_CLI --connect --command="deploy /path/to/api-gateway.war"

# Ver logs de deployment
$WILDFLY_CLI --connect --command=":read-log-file(lines=50)"
```

## 🏥 Health Checks y Monitoreo

### 1. Endpoints de Salud

| Servicio | Puerto | Endpoint | Ejemplo |
|----------|--------|----------|---------|
| Eureka Server | 8761 | `/actuator/health` | http://localhost:8761/actuator/health |
| Config Server | 8888 | `/actuator/health` | http://localhost:8888/actuator/health |
| API Gateway | 8080 | `/actuator/health` | http://localhost:8080/actuator/health |
| Arca Cotizador | 8080 | `/actuator/health` | http://localhost:8080/actuator/health |
| Gestor Solicitudes | 8080 | `/actuator/health` | http://localhost:8080/actuator/health |

### 2. Scripts de Health Check

```bash
# Health check manual
curl -f http://localhost:8761/actuator/health
curl -f http://localhost:8888/actuator/health
curl -f http://localhost:8080/actuator/health

# Health check automatizado
./scripts/deploy-wars-wildfly.sh --action health
```

### 3. Logs y Debugging

```bash
# Logs de WildFly
tail -f $WILDFLY_HOME/standalone/log/server.log

# Logs de servicios de infraestructura
sudo journalctl -u eureka-server -f
sudo journalctl -u config-server -f

# Logs de aplicación
tail -f logs/*.log

# Status de deployments
$WILDFLY_CLI --connect --command="deployment-info"
```

## 🔧 Configuraciones Específicas

### 1. Configuración de Base de Datos

```bash
# MySQL DataSource (Producción)
/subsystem=datasources/data-source=ArkaDS:add(\
    jndi-name=java:jboss/datasources/ArkaDS,\
    driver-name=mysql,\
    connection-url=jdbc:mysql://localhost:3306/arka_db,\
    user-name=arka_user,\
    password=arka_pass,\
    min-pool-size=5,\
    max-pool-size=20)

# H2 DataSource (Desarrollo)
/subsystem=datasources/data-source=ArkaTestDS:add(\
    jndi-name=java:jboss/datasources/ArkaTestDS,\
    driver-name=h2,\
    connection-url=jdbc:h2:mem:testdb,\
    user-name=sa,\
    password=)
```

### 2. Variables de Sistema

```properties
# Spring Profiles
spring.profiles.active=wildfly,aws

# Eureka Configuration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
eureka.instance.hostname=localhost

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/arka_db
spring.datasource.username=arka_user
spring.datasource.password=arka_pass

# JWT Configuration
jwt.secret=mySecretKey
jwt.expiration=3600
```

### 3. Security Configuration

```xml
<!-- jboss-deployment-structure.xml -->
<jboss-deployment-structure>
    <deployment>
        <exclude-subsystems>
            <subsystem name="logging" />
        </exclude-subsystems>
        <exclusions>
            <module name="org.apache.commons.logging" />
            <module name="org.slf4j" />
            <module name="org.slf4j.impl" />
        </exclusions>
    </deployment>
</jboss-deployment-structure>
```

## 🚨 Troubleshooting

### 1. Problemas Comunes

**Build Errors:**
```bash
# Error: WAR plugin not applied
Solution: Verificar que 'war' plugin esté en build.gradle

# Error: ServletInitializer missing
Solution: Verificar que existe ServletInitializer.java en cada microservicio

# Error: providedRuntime not configured
Solution: Agregar providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'
```

**Deployment Errors:**
```bash
# Error: WildFly not responding
Solution: Verificar que WildFly esté iniciado y accesible

# Error: ClassNotFoundException
Solution: Verificar jboss-deployment-structure.xml

# Error: Port already in use
Solution: Verificar que no hay conflictos de puertos
```

**Runtime Errors:**
```bash
# Error: Service not registering in Eureka
Solution: Verificar configuración de eureka.client.service-url

# Error: Database connection failed
Solution: Verificar DataSource configuration en WildFly

# Error: Authentication errors
Solution: Verificar JWT configuration y Spring Security setup
```

### 2. Comandos de Diagnóstico

```bash
# Verificar estado general
./scripts/deploy-wars-wildfly.sh --action health

# Verificar deployments en WildFly
$WILDFLY_CLI --connect --command="deployment-info"

# Verificar logs de error
tail -n 100 $WILDFLY_HOME/standalone/log/server.log | grep -i error

# Verificar servicios systemd
sudo systemctl status eureka-server config-server

# Test de conectividad
curl -v http://localhost:8761/actuator/health
curl -v http://localhost:8888/actuator/health
curl -v http://localhost:8080/actuator/health
```

### 3. Recovery Procedures

```bash
# Complete restart
./scripts/deploy-wars-wildfly.sh --action undeploy
sudo systemctl restart wildfly
./scripts/deploy-wars-wildfly.sh --action full

# Partial restart (business services only)
./scripts/deploy-wars-wildfly.sh --action redeploy --skip-build

# Infrastructure services restart
sudo systemctl restart eureka-server config-server
```

## 📊 Métricas y Monitoring

### 1. Actuator Endpoints

```bash
# Health status
GET /actuator/health

# Application info
GET /actuator/info

# Metrics
GET /actuator/metrics

# Environment variables
GET /actuator/env
```

### 2. WildFly Management

```bash
# Server status
$WILDFLY_CLI --connect --command=":read-attribute(name=server-state)"

# Deployment status
$WILDFLY_CLI --connect --command="deployment-info"

# Resource usage
$WILDFLY_CLI --connect --command="/core-service=platform-mbean/type=memory:read-resource(include-runtime=true)"
```

## 🎯 Best Practices

### 1. Development Workflow

1. **Desarrollo local**: Usar `gradle bootRun` para desarrollo rápido
2. **Testing de integración**: Usar scripts de despliegue local
3. **Pre-producción**: Usar Jenkins pipeline en rama develop
4. **Producción**: Usar Jenkins pipeline en rama main

### 2. Configuración de Ambientes

```yaml
# application-wildfly.yml
spring:
  profiles:
    active: wildfly
    
eureka:
  instance:
    hostname: ${HOSTNAME:localhost}
    non-secure-port: ${SERVER_PORT:8080}
    
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### 3. Security Considerations

- Usar variables de ambiente para credenciales
- Configurar SSL/TLS para comunicación entre servicios
- Implementar security domains en WildFly
- Rotar JWT secrets periódicamente

## 📚 Referencias y Recursos

### 1. Documentación Oficial

- [WildFly Documentation](https://docs.wildfly.org/)
- [Spring Boot WAR Deployment](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html#deployment.traditional)
- [Jenkins Pipeline Documentation](https://www.jenkins.io/doc/book/pipeline/)

### 2. Archivos de Configuración

- `build.gradle` - Configuración de build y WAR
- `Jenkinsfile-wildfly-wars` - Pipeline de despliegue
- `scripts/wildfly-arka-config.cli` - Configuración WildFly
- `scripts/deploy-wars-wildfly.*` - Scripts de despliegue

### 3. Comandos Rápidos

```bash
# Build y deploy completo
./scripts/deploy-wars-wildfly.sh --action full

# Solo build
./scripts/deploy-wars-wildfly.sh --action build

# Solo deploy
./scripts/deploy-wars-wildfly.sh --action deploy --skip-build

# Health checks
./scripts/deploy-wars-wildfly.sh --action health

# Undeploy todo
./scripts/deploy-wars-wildfly.sh --action undeploy
```

---

## 🎉 Conclusión

La configuración de despliegue WAR para WildFly está completa y lista para producción. El pipeline Jenkins automatiza todo el proceso, desde build hasta health checks, mientras que los scripts locales permiten testing y debugging rápido.

**Beneficios clave:**
- ✅ Despliegue automatizado con Jenkins
- ✅ Arquitectura híbrida optimizada (JARs + WARs)
- ✅ Health checks automáticos
- ✅ Rollback y recovery procedures
- ✅ Monitoring y métricas integradas
- ✅ Scripts multiplataforma (Linux/Windows)

**Próximos pasos:**
1. Configurar Jenkins con el nuevo pipeline
2. Probar despliegue local con scripts
3. Configurar monitoring avanzado (Grafana/Prometheus)
4. Implementar estrategias de backup y disaster recovery