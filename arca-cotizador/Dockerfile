# Cotizador Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build

ARG MODULE_NAME=arca-cotizador

WORKDIR /workspace/app

# Copy gradle wrapper and build files from root
COPY gradlew gradlew.bat ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Copy all source code (needed for multi-module build)
COPY . .

# Make gradlew executable and build specific module
RUN chmod +x gradlew && \
    echo "=== Gradle Wrapper Version ===" && \
    ./gradlew --version && \
    echo "=== Available projects ===" && \
    ./gradlew projects && \
    echo "=== Building arka-security-common first ===" && \
    ./gradlew :arka-security-common:build -x test --no-daemon && \
    echo "=== Building arca-cotizador ===" && \
    ./gradlew :arca-cotizador:build -x test --no-daemon

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
