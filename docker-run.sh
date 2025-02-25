#!/bin/bash
export $(grep -v '^#' environment.properties | xargs -d '\n')
docker-compose up -d
