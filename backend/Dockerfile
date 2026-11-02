FROM azul/zulu-openjdk-alpine:26.0.2.1-26.32-jre@sha256:1f0e20dd1b41ae1d283ac907421682257518ae1d1e190bd5fc4981ad01321c5d
WORKDIR /app
COPY ./build/libs/kod-game-server-*.jar kod-game-server.jar
EXPOSE 8080
CMD ["java", "-jar", "kod-game-server.jar", "--spring.profiles.active=prod"]
