FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY  target/stream.jar stream.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "stream.jar"]