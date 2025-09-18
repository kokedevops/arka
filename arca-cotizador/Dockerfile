# Cotizador Dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build

ARG MODULE_NAME=arca-cotizador

WORKDIR /workspace/app

# Copy gradle wrapper and build files from root
COPY gradlew gradlew.bat ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Copy all source code (needed for multi-module build)
COPY arca-cotizador arca-cotizador
COPY arka-security-common arka-security-common

# Make gradlew executable and build specific module
RUN chmod +x gradlew && ./gradlew :${MODULE_NAME}:clean :${MODULE_NAME}:build -x test

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

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
