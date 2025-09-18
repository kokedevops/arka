# 🏗️ ARKA Config Server Dockerfile
# Multi-stage build para optimizar el tamaño de la imagen

FROM eclipse-temurin:21-jdk-alpine AS builder

ARG MODULE_NAME=config-server

# Información del build
LABEL maintainer="ARKA Development Team"
LABEL description="Config Server for ARKA E-commerce Platform"
LABEL version="1.0.0"

# Variables de entorno
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8888

# Directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Gradle desde la raíz
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle/

# Copiar código fuente del módulo específico
COPY config-server config-server/

# Compilar la aplicación
RUN chmod +x ./gradlew && ./gradlew :${MODULE_NAME}:clean :${MODULE_NAME}:bootJar --no-daemon

# Imagen final
FROM eclipse-temurin:21-jre-alpine

ARG MODULE_NAME=config-server

# Install curl for health checks
RUN apk add --no-cache curl

# Información del contenedor
LABEL maintainer="ARKA Development Team"
LABEL description="ARKA Config Server - Centralized Configuration Management"

# Variables de entorno
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8888
ENV EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/

# Crear usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=builder /app/${MODULE_NAME}/build/libs/*.jar app.jar

# Cambiar propietario
RUN chown spring:spring app.jar

# Usar usuario no-root
USER spring:spring

# Puerto expuesto
EXPOSE 8888

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8888/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
