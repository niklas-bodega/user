# Multi-stage build for Java 21 Spring Boot application
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

# Install Maven
RUN apt-get update && apt-get install -y maven

# Copy pom.xml and download dependencies (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source code and build
COPY src/ src/
RUN mvn clean package -q -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/target/*.jar app.jar

# Expose port
EXPOSE 8084

# Health check
# --interval=30s --timeout=3s --start-period=10s --retries=3 \
#    CMD java -cp app.jar org.springframework.boot.loader.launch.JarLauncher --spring.health.endpoint=health 2>/dev/null || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
