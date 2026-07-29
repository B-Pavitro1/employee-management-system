########### BUILDING THE APPLICATION #########
FROM maven:3.9.16-eclipse-temurin-21 as build

# Set the working directory inside the Docker image
WORKDIR /app

# Copy the JAR file from your target directory into the Docker image
COPY target/*.jar employee-management-system.jar

# Build the application using Maven
RUN mvn clean package -DskipTests

######### RUNNING THE APPLICATION #########
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR file from the build stage to the runtime stage
COPY --from=build /app/employee-management-system.jar employee-management-system.jar

# Expose the port your app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "employee-management-system.jar"]