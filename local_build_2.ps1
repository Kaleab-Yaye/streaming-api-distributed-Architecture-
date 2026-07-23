cd ./Go
$env:GOOS="linux"; $env:GOARCH="amd64"; go build -o go-stream-node .
cd ..
docker-compose build --no-cache api nginx minio
docker compose up --build

