# Stage 1: Build the application using Maven
FROM maven:3.8.5-openjdk-18 AS build

# Set the working directory
WORKDIR /app

# Copy only the pom.xml and download dependencies (cache optimization)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Stage 2: Create a lightweight runtime image
FROM openjdk:18-jdk-slim

# Set the working directory in the runtime image
WORKDIR /home/app

# Create a user and set permissions for security best practices
RUN groupadd -r app && useradd --no-log-init -r -g app app && chown -R app:app /home/app
USER app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Define the entrypoint
ENTRYPOINT ["java", "-jar", "/home/app/app.jar"]