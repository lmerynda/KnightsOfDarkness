FROM node:26.13.1-trixie-slim@sha256:e6bedef217677b276c591795e169840edd9096dbf13f29df8ef8d1856b8820bc AS node
FROM mcr.microsoft.com/devcontainers/base:trixie@sha256:38f9e6c291f054e3c868d95a5477cbfcf76c0fa7c218dda65dc6aa3fbfe06871

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
    apt-get update && apt-get install -y zulu25-jdk

COPY --from=node /usr/local/ /usr/local/

RUN echo "plugins=(gradle)" >> ~/.zshrc

RUN zsh -c "java --version && node --version && npm --version"

WORKDIR /workspaces/KnightsOfDarkness
