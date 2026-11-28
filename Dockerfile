FROM node:26.13.1-trixie-slim@sha256:abe7e5a82b61a8ef67b315b85497c9e65173387ccf2f4a029248184b8e020be5 AS node
FROM mcr.microsoft.com/devcontainers/base:trixie@sha256:41ff7ce12ec45f2704f27a6e091831186ad922ec8112e6339425bfefdf5a70f9

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
