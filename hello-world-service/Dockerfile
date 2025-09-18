# Hello World Service Dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build

ARG MODULE_NAME=hello-world-service

WORKDIR /workspace/app

# Copy gradle wrapper and build files from root
COPY gradlew gradlew.bat ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Copy all source code (needed for multi-module build)
COPY hello-world-service hello-world-service
COPY arka-security-common arka-security-common

# Make gradlew executable and build specific module
RUN chmod +x gradlew && ./gradlew :${MODULE_NAME}:clean :${MODULE_NAME}:build -x test

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

ARG MODULE_NAME=hello-world-service

# Install curl for health checks and create application user
RUN apk add --no-cache curl && \
    addgroup -S arka && adduser -S arka -G arka

WORKDIR /app

# Copy jar file
COPY --from=build /workspace/app/${MODULE_NAME}/build/libs/*.jar app.jar

# Change ownership
RUN chown -R arka:arka /app

USER arka

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
