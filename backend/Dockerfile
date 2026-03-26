# Start with a lightweight Linux image with Java 17 installed
FROM eclipse-temurin:17-jre-alpine

# Create a directory inside the container
WORKDIR /app

# Copy the compiled Spring Boot .jar file into the container
COPY target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# The command that runs when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]