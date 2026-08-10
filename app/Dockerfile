Create a multi-stage Dockerfile for a Java Spring Boot backend application
Apply Docker best practices for optimizing and securing Docker images

In this step, you'll create a multi-stage Dockerfile to containerize your Spring Boot backend. The Dockerfile will use Maven to build your application in one stage and then copy the resulting .jar into a lightweight Java runtime image in a second stage. This results in a smaller, production-ready Docker image that contains only the compiled application and its runtime dependencies.

Use the following framework:

# Step 1: Use Maven with JDK 17 to build the app
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Step 2: Use lightweight JRE 17 for running the app
FROM eclipse-temurin:17.0.15_6-jre

WORKDIR /app

COPY --from=builder /app/target/back-end-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]