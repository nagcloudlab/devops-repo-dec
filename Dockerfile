# ==============================================================================
# UPI PAYMENT SERVICE - DOCKERFILE
# ==============================================================================
# Multi-stage build for optimized production image
# 
# STAGES:
# 1. build  - Compile and package the application
# 2. runtime - Minimal JRE image for production
# ==============================================================================

# ------------------------------------------------------------------------------
# STAGE 1: BUILD
# ------------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build

LABEL maintainer="DevOps Team"
LABEL description="UPI Payment Service - Build Stage"

WORKDIR /app

# Copy parent pom and module pom first (for Docker cache optimization)
COPY pom.xml .
COPY transfer-service/pom.xml ./transfer-service/

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B -pl transfer-service

# Copy source code
COPY transfer-service/src ./transfer-service/src

# Build the application (skip tests - already run in CI)
RUN mvn package -DskipTests -B -pl transfer-service

# ------------------------------------------------------------------------------
# STAGE 2: RUNTIME
# ------------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL maintainer="DevOps Team"
LABEL description="UPI Payment Service - Production Image"
LABEL version="1.0.0"

# Security: Run as non-root user
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/transfer-service/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# JVM Configuration
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
