# ==========================================
# STAGE 1: Build the Java Code (Maven)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Compile the code and create the .jar file
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Run the Application
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the compiled .jar from the "build" stage above
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Run the app, respecting the AWS memory limits
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]