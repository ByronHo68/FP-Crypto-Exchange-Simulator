#!/bin/bash


usage() {
    echo "Usage: $0 -e <env-file> -p <private-key-path>"
    echo "  -e <env-file>          Specify the environment file (mandatory)"
    echo "  -p <private-key-path>  Specify the SSH private key file path (mandatory)"
    exit 1
}


while getopts "e:p:" option; do
    case $option in
        e) ENV_FILE=$OPTARG ;;
        p) PRIVATE_KEY_FILE=$OPTARG ;;
        *) usage ;;
    esac
done


if [ -z "$ENV_FILE" ] || [ -z "$PRIVATE_KEY_FILE" ]; then
    echo "Error: Both environment file and private key file are required."
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
    "AWS_REGION"
    "LINUX_USER"
    "LINUX_SERVER_ADDRESS"
    "REMOTE_WORK_DIR"
)


for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        echo "Error: $var is not set in the environment file."
        exit 1
    fi
done


AWS_ECR_BASE_URL="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
DOCKER_COMPOSE_FILE=docker-compose.yml
FIREBASE_SERVICE_ACCOUNT_FILE=service-account.json


echo "Copying Docker Compose and environment files to the remote server..."
scp -i "${PRIVATE_KEY_FILE}" "${DOCKER_COMPOSE_FILE}" "$ENV_FILE" "$FIREBASE_SERVICE_ACCOUNT_FILE" "${LINUX_USER}@${LINUX_SERVER_ADDRESS}:${REMOTE_WORK_DIR}"


echo "Deploying containers on remote server..."
ssh -t -i "${PRIVATE_KEY_FILE}" "${LINUX_USER}@${LINUX_SERVER_ADDRESS}" << EOF
  cd ${REMOTE_WORK_DIR}

  # Log in to AWS ECR
  aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ECR_BASE_URL}

  # Pull the latest images and start the services
  docker-compose --env-file $(basename "$ENV_FILE") -f ${DOCKER_COMPOSE_FILE} up --pull always -d
EOF

echo "Deployment complete."
