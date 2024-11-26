#!/bin/bash


usage() {
    echo "Usage: $0 -e <env-file>"
    echo "  -e <env-file>    Specify the environment file (mandatory)"
    exit 1
}


while getopts "e:" option; do
    case $option in
        e) ENV_FILE=$OPTARG ;;
        *) usage ;;
    esac
done


if [ -z "$ENV_FILE" ]; then
    echo "Error: Environment file is required."
    usage
fi


if [ -f "$ENV_FILE" ]; then
    echo "Loading environment variables from $ENV_FILE..."
    source "$ENV_FILE"
else
    echo "Error: Environment file '$ENV_FILE' not found."
    exit 1
fi


REQUIRED_VARS=(
    "FRONTEND_IMAGE_NAME"
    "BACKEND_IMAGE_NAME"
)
for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        echo "Error: $var is not set in the environment file."
        exit 1
    fi
done

DOCKER_COMPOSE_FILE=docker-compose.yml


echo "Starting Docker Compose services..."
docker-compose --env-file "$ENV_FILE" -f ${DOCKER_COMPOSE_FILE} up -d

echo "Docker Compose services are now running."
