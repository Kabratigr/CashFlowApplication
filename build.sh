#!/bin/bash
export $(grep -v '^#' environment.properties | xargs -d '\n')
mvn clean package
