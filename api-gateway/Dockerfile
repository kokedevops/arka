# API Gateway Dockerfile
FROM openjdk:21-jdk-slim as build

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
FROM openjdk:21-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create application user
RUN groupadd -r arka && useradd -r -g arka arka

WORKDIR /app

# Copy jar file
COPY --from=build /workspace/app/build/libs/*.jar app.jar

# Change ownership
RUN chown -R arka:arka /app

USER arka

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
