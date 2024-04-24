# Use an Ubuntu base image
FROM ubuntu:22.04

RUN apt-get update

RUN apt-get install -y openjdk-17-jdk

RUN apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

RUN npm install -g typescript

# Set environment variables
ENV JAVA_HOME /usr/lib/jvm/java-17-openjdk-amd64
ENV NODE_PATH /usr/lib/node_modules

# Set default command (optional)
CMD ["/bin/bash"]

# Expose ports (if needed)
# EXPOSE 8080

# Set working directory (if needed)
# WORKDIR /workspace