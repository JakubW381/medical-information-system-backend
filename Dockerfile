# === BUILD STAGE ===
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# === RUNTIME STAGE ===
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY target/his-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
