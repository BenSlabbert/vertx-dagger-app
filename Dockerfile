FROM azul/zulu-openjdk-alpine:17-latest as builder

WORKDIR /app

COPY target/vertx.jar app.jar

COPY target/lib lib

ENTRYPOINT ["java", "-cp", "lib", "-jar", "app.jar"]

EXPOSE 8080
