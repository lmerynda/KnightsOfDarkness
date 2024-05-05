# Use an Ubuntu base image
FROM mcr.microsoft.com/devcontainers/base:bullseye

RUN sh -c "$(curl -fsSL https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"
