# Cotizador Dockerfile - Optimizado para Jenkins
FROM eclipse-temurin:21-jdk-alpine AS build

ARG MODULE_NAME=arca-cotizador

# Variables de entorno para optimizar memoria - MUY RESTRICTIVO
ENV GRADLE_OPTS="-Xmx512m -Xms256m -XX:MaxMetaspaceSize=128m"
ENV JAVA_OPTS="-Xmx512m -Xms256m"

WORKDIR /workspace/app

# Copy gradle wrapper and build files from root
COPY gradlew gradlew.bat ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Copy only necessary modules to reduce build context
COPY arca-cotizador arca-cotizador
COPY arka-security-common arka-security-common

# Make gradlew executable and build in stages
RUN chmod +x gradlew

# Build step by step to avoid memory issues
RUN ./gradlew --version
RUN ./gradlew :arka-security-common:build -x test --no-daemon --max-workers=1
RUN ./gradlew :arca-cotizador:build -x test --no-daemon --max-workers=1

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

ARG MODULE_NAME=arca-cotizador

# Install curl for health checks and create application user
RUN apk add --no-cache curl && \
    addgroup -S arka && adduser -S arka -G arka

WORKDIR /app

# Copy jar file
COPY --from=build /workspace/app/${MODULE_NAME}/build/libs/*.jar app.jar

# Create data directory and change ownership
RUN mkdir -p /app/data && \
    chown -R arka:arka /app

USER arka

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
