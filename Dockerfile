# Use Eclipse Temurin JDK 21 (official OpenJDK replacement) for base image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory inside the Docker image
WORKDIR /app

# Copy the JAR file from your target directory into the Docker image
COPY target/*.jar employee-management-system.jar

# Expose the port your app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "employee-management-system.jar"]