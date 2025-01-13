#!/bin/bash

./gradlew build
docker build -t ghcr.io/lmerynda/knightsofdarkness-game-server .
docker push ghcr.io/lmerynda/knightsofdarkness-game-server
