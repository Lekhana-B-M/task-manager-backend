# Step 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Run
FROM eclipse-temurin:21-jdk-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080

# This "Shell form" ensures variables like $DB_URL are actually used
ENTRYPOINT java -Dserver.port=${PORT} -Dspring.datasource.url=${DB_URL} -Dspring.datasource.username=${DB_USER} -Dspring.datasource.password=${DB_PASSWORD} -jar /app.jar
