#!/bin/bash

./gradlew build -x test
docker build -t ghcr.io/lmerynda/knightsofdarkness-game-server .
docker push ghcr.io/lmerynda/knightsofdarkness-game-server
