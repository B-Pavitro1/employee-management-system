# Use Eclipse Temurin JDK 17 (official OpenJDK replacement)
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from your target directory to the container
COPY target/*.jar employee-management-system.jar

# Expose the port your app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "employee-management-system.jar"]