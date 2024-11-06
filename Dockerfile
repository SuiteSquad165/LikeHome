# Use a base image with Java 21
FROM azul/zulu-openjdk-debian:21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradle configuration files and wrapper
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy the source code
COPY src/main src/main

# Give execute permission to Gradlew
RUN chmod +x ./gradlew

# Build the application with Gradle, using no-daemon to free up memory after the build
RUN ./gradlew bootJar --no-daemon

# Use a slim base image for the runtime
FROM azul/zulu-openjdk-debian:21-jre-headless

WORKDIR /app

# Copy only the necessary files from the build stage
COPY --from=build /app/build/libs/LikeHome.jar LikeHome.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "LikeHome.jar"]
