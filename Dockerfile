######### BUILDING #########
FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests

######### RUNNING #########
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar employee-management-system.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "employee-management-system.jar"]