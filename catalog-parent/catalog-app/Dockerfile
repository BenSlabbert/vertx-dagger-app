FROM azul/zulu-openjdk-alpine:17-latest

WORKDIR /app

COPY target/catalog.jar app.jar

COPY target/lib lib

ENTRYPOINT ["java", "-cp", "lib", "-jar", "app.jar", "-Xmx=32M", "-Xms=32M"]

EXPOSE 8080
EXPOSE 50051
