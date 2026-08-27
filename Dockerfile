# Multi-stage build for Java 21 Spring Boot application
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Kopiera endast pom.xml först för att cacha beroenden (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Kopiera källkoden och bygg JAR-filen
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtimesteg
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Best practice: Skapa en non-root användare för säkerhet
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Kopiera JAR-filen från byggsteget
COPY --from=builder --chown=spring:spring /build/target/*.jar app.jar

# Exponera port
EXPOSE 8084

# Starta applikationen
ENTRYPOINT ["java", "-jar", "app.jar"]