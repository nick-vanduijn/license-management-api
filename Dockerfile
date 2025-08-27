# Production-ready multi-stage Docker build
FROM eclipse-temurin:17-jdk-alpine AS builder

# Install build dependencies
RUN apk add --no-cache git

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first for better layer caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests -B

# Production runtime stage
FROM eclipse-temurin:17-jre-alpine AS runtime

# Install security updates and create non-root user
RUN apk update && \
    apk upgrade && \
    apk add --no-cache \
        curl \
        dumb-init && \
    rm -rf /var/cache/apk/* && \
    addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy application JAR from builder stage
COPY --from=builder /app/target/license-management-api-*.jar app.jar

# Create directories for logs and data
RUN mkdir -p /var/log/license-management-api && \
    chown -R appuser:appgroup /app /var/log/license-management-api

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for production
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Use dumb-init for proper signal handling
ENTRYPOINT ["dumb-init", "--"]

# Run application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Metadata
LABEL maintainer="license-team@company.com"
LABEL version="1.0.0"
LABEL description="Production-ready License Management API"
LABEL org.opencontainers.image.source="https://github.com/company/license-management-api"
