# Step 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Run
FROM eclipse-temurin:21-jdk-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080

# WE ARE HARDCODING THE URL HERE FOR ONE TEST
ENTRYPOINT ["java", "-Dspring.datasource.url=# Step 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Run
FROM eclipse-temurin:21-jdk-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080

# WE ARE HARDCODING THE URL HERE FOR ONE TEST
ENTRYPOINT ["java", "-Dspring.datasource.url=jdbc:postgresql://adminuser:DSMj0wl0nPEFLuRn3AGs4wMyjOditcAF@dpg-d83i4ot7vvec739fqcig-a/mytaskdb_5h8f", "-Dspring.datasource.username=adminuser", "-Dspring.datasource.password=DSMj0wl0nPEFLuRn3AGs4wMyjOditcAF", "-jar", "/app.jar"]