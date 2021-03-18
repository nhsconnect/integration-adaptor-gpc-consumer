#!/bin/bash 

LIGHT_GREEN='\033[1;32m' 
NC='\033[0m'

echo -e "${LIGHT_GREEN}Following gpc-consumer container logs${NC}"
cd ../docker || exit 1
docker-compose logs -f gpc-consumer
