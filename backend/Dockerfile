FROM azul/zulu-openjdk-alpine:21-jre

WORKDIR /app

COPY web/build/libs/*.jar kod-game-server.jar

# Expose the port the app will run on
EXPOSE 8080

# Run the application in the container
CMD ["java", "-jar", "kod-game-server.jar"]
