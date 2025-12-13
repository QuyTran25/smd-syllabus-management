# ============================================
# Stage 1: Build
# ============================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy shared library first (for dependency)
COPY backend/shared/java-common ./shared/java-common
WORKDIR /app/shared/java-common
RUN mvn clean install -DskipTests

# Copy core-service source
WORKDIR /app/core-service
COPY backend/core-service/pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY backend/core-service/src ./src

# Build application
RUN mvn clean package -DskipTests

# ============================================
# Stage 2: Runtime
# ============================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create directories
RUN mkdir -p /app/uploads && chown -R spring:spring /app

USER spring:spring

# Copy built artifact
COPY --from=build /app/core-service/target/*.jar app.jar

# Note: Firebase service account key should be mounted as volume or secret in production
# Example: -v ./firebase-service-account.json:/app/config/firebase-service-account.json

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# JVM options for production
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+UseStringDeduplication"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-development} \
    -jar app.jar"]
