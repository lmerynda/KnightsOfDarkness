FROM node:24.22.0-bookworm-slim@sha256:a5f4366e834695d5f363b920c104953a535d3a9864d1bdf74566a0ebe01daee5 AS node
FROM mcr.microsoft.com/devcontainers/base:bookworm@sha256:9a5942e64b35c5497c591b9bf177f4a3002d43440bf0a5e1a14fc34667b1a5c1

RUN apt-get update && apt-get install -y \
    zsh \
    docker.io \
    gnupg \
    ca-certificates \
    curl \
    && apt-get clean

RUN chsh -s $(which zsh)

RUN curl -s https://repos.azul.com/azul-repo.key | gpg --dearmor -o /usr/share/keyrings/azul.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/azul.gpg] https://repos.azul.com/zulu/deb stable main" | tee /etc/apt/sources.list.d/zulu.list && \
    apt-get update && apt-get install -y zulu21-jdk

COPY --from=node /usr/local/ /usr/local/

RUN echo "plugins=(gradle)" >> ~/.zshrc

RUN zsh -c "java --version && node --version && npm --version"

WORKDIR /workspaces/KnightsOfDarkness
