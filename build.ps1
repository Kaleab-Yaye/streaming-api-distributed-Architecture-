./mvnw spotless:apply
./mvnw clean install -DskipTests
docker-compose build --no-cache api nginx
docker compose up -d --build
docker tag api  ghcr.io/kaleab-yaye/api:latest
docker push ghcr.io/kaleab-yaye/api:latest

