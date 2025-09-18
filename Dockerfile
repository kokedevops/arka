# Dockerfile para la aplicación principal ARKA E-commerce - Optimizado
FROM eclipse-temurin:21-jdk-alpine AS build

# Variables de entorno para optimizar memoria
ENV GRADLE_OPTS="-Xmx512m -Xms256m -XX:MaxMetaspaceSize=128m"
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Directorio de trabajo
WORKDIR /app

# Instalar herramientas necesarias
RUN apk add --no-cache curl netcat-openbsd

# Copiar archivos de Gradle
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copiar solo el código fuente del main app
COPY src src

# Hacer ejecutable el gradlew
RUN chmod +x ./gradlew

# Construir solo el jar principal con memoria limitada
RUN ./gradlew clean bootJar -x test --no-daemon --max-workers=1 -Xmx512m

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Metadata
LABEL maintainer="ARKA Development Team"
LABEL version="1.0"
LABEL description="ARKA E-commerce Platform - Main Application"

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8888

# Instalar curl para health checks
RUN apk add --no-cache curl

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR generado
COPY --from=build /app/build/libs/*.jar app.jar

# Exponer puerto
EXPOSE 8888

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8888/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
