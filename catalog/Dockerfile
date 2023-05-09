FROM azul/zulu-openjdk-alpine:17-latest as builder

WORKDIR /app

COPY target/catalog.jar app.jar

COPY target/lib lib

ENTRYPOINT ["java", "-cp", "lib", "-jar", "app.jar"]

EXPOSE 8080
EXPOSE 50051
