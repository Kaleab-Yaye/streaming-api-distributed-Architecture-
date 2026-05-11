./mvnw spotless:apply
./mvnw clean install -DskipTests
docker-compose build --no-cache api nginx
docker compose up --build