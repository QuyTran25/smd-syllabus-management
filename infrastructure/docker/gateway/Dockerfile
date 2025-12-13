# ============================================
# Stage 1: Build
# ============================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy shared library first (for dependency)
COPY backend/shared/java-common ./shared/java-common
WORKDIR /app/shared/java-common
RUN mvn clean install -DskipTests

# Copy gateway source
WORKDIR /app/gateway
COPY backend/gateway/pom.xml .
RUN mvn dependency:go-offline -B

COPY backend/gateway/src ./src
RUN mvn clean package -DskipTests

# ============================================
# Stage 2: Runtime
# ============================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built artifact
COPY --from=build /app/gateway/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-development}", \
    "-jar", \
    "app.jar"]
