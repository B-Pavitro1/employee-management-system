# Use a base image with Java runtime
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from your target directory to the container
COPY target/*.jar employee-management-system.jar

# Command to run the application
ENTRYPOINT ["java", "-jar", "employee-management-system.jar"]