# Step 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Run
FROM eclipse-temurin:21-jdk-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080

# We force the 'prod' profile on Render
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]