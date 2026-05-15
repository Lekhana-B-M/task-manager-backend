# Step 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Run
FROM eclipse-temurin:21-jdk-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080

# This form is the 'Golden Rule' for Render
ENTRYPOINT ["sh", "-c", "java -jar /app.jar"]