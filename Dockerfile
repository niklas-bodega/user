# Multi-stage build for Java 21 Spring Boot application
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Install Maven
RUN apt-get update && apt-get install -y maven

# Copy pom.xml and download dependencies (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Best practice: Skapa en non-root användare för säkerhet
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Kopiera JAR-filen från byggsteget och ge rättigheter till den nya användaren
COPY --from=builder --chown=spring:spring /build/target/*.jar app.jar

# Exponera porten
EXPOSE 8084

# Starta applikationen
ENTRYPOINT ["java", "-jar", "app.jar"]