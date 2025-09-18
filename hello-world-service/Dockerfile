# Hello World Service Dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace/app

# Copy gradle wrapper and build files
COPY gradlew gradlew.bat ./
COPY gradle gradle
COPY build.gradle ./
COPY settings.gradle ./

# Copy source code
COPY src src

# Make gradlew executable and build
RUN chmod +x gradlew && ./gradlew clean build -x test

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create application user
RUN addgroup -S arka && adduser -S arka -G arka

WORKDIR /app

# Copy jar file
COPY --from=build /workspace/app/build/libs/*.jar app.jar

# Change ownership
RUN chown -R arka:arka /app

USER arka

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
