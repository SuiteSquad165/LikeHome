# Use an official OpenJDK 21 runtime as the base image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Install bash and dos2unix for compatibility
RUN apk add --no-cache bash dos2unix

# Copy the Gradle wrapper and make it executable
COPY ./gradlew /app/gradlew
RUN dos2unix /app/gradlew && chmod +x /app/gradlew

# Copy the Gradle configuration and project files
COPY ./gradle /app/gradle
COPY build.gradle settings.gradle /app/
COPY ./src /app/src

# Build the application inside the container
RUN /bin/bash ./gradlew clean build --no-daemon

# Copy the correct JAR file into the container
RUN cp build/libs/LikeHome.jar app.jar

# Expose the port your Spring Boot app will run on
EXPOSE 8080

# Run the generated JAR file
CMD ["java", "-jar", "app.jar"]
