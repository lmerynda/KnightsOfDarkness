# Use the official OpenJDK base image
FROM openjdk:21-jdk-slim AS build

# Set the working directory to /app inside the container
WORKDIR /app

# Copy everything from the current directory (backend) into /app inside the container
COPY . .

# Build the application using Gradle (avoiding tests in the build process)
RUN ./gradlew build -x test

# Expose the port the app will run on
EXPOSE 8080

# Run the application in the container
CMD ["java", "-jar", "web/build/libs/web-0.0.1-SNAPSHOT.jar"]
