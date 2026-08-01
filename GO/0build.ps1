# gonna use this to docker compose my containers, with out having to type every thing from scratch
$env:GOOS="linux"; $env:GOARCH="amd64"; go build -o go-stream-node .
docker-compose build --no-cache go-stream-node nginx
docker compose up --build