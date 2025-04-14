FROM azul/zulu-openjdk-alpine:21-jre
WORKDIR /app
COPY ./build/libs/kod-game-server-*.jar kod-game-server.jar
EXPOSE 8080
CMD ["java", "-jar", "kod-game-server.jar", "--spring.profiles.active=prod"]
