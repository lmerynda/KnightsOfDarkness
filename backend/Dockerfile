FROM azul/zulu-openjdk-alpine:25.0.5-jre@sha256:d0e68819f46c96d557fb112a6ea7636e4e36c543005f6325316efc4ec6f7fe5a
WORKDIR /app
COPY ./build/libs/kod-game-server-*.jar kod-game-server.jar
EXPOSE 8080
CMD ["java", "-jar", "kod-game-server.jar", "--spring.profiles.active=prod"]
