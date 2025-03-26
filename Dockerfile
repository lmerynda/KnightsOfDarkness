FROM mcr.microsoft.com/devcontainers/base:bullseye

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

RUN curl -fsSL https://deb.nodesource.com/setup_22.x | zsh - && apt-get install -y nodejs

RUN echo "plugins=(gradle)" >> ~/.zshrc

RUN zsh -c "java --version && node --version"

WORKDIR /workspaces/KnightsOfDarkness
