docker tag  api  ghcr.io/kaleab-yaye/central_server_spring_image:latest
docker tag stream-node ghcr.io/kaleab-yaye/stream-node-image:latest
docker tag encoder ghcr.io/kaleab-yaye/encode:latest

docker push ghcr.io/kaleab-yaye/central_server_spring_image:latest
docker push ghcr.io/kaleab-yaye/stream-node-image:latest
docker push ghcr.io/kaleab-yaye/encode:latest