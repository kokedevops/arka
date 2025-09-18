# API Gateway Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build

ARG MODULE_NAME=api-gateway

WORKDIR /workspace/app

# Copy gradle wrapper and build files from root
COPY gradlew gradlew.bat ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Copy all source code (needed for multi-module build)
COPY api-gateway api-gateway

# Make gradlew executable and build specific module
RUN chmod +x gradlew && \
    ./gradlew --version && \
    ./gradlew :${MODULE_NAME}:clean :${MODULE_NAME}:build -x test --no-daemon --parallel

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

ARG MODULE_NAME=api-gateway

# Install curl for health checks and create application user
RUN apk add --no-cache curl && \
    addgroup -S arka && adduser -S arka -G arka

WORKDIR /app

# Copy jar file
COPY --from=build /workspace/app/${MODULE_NAME}/build/libs/*.jar app.jar

# Change ownership
RUN chown -R arka:arka /app

USER arka

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
