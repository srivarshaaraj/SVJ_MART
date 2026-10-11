# ==========================================
# SVJ MART - Cloud Deployment Dockerfile
# Multi-stage build for Java 21 & Spring Boot
# ==========================================

# Stage 1: Build the Application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build the executable jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Production Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create persistent storage folder for H2 database
RUN mkdir -p /app/data

# Copy the built fat jar from Stage 1
COPY --from=build /app/target/svj-mart-1.0.0.jar app.jar

# Expose default port
EXPOSE 8080

# Environment defaults
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

# Run the Spring Boot application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
