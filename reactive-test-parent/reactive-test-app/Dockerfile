FROM azul/zulu-openjdk-alpine:21-latest

WORKDIR /app

COPY target/reactive-test-app.jar app.jar

COPY target/lib lib

ENTRYPOINT ["java", "-cp", "lib", "-jar", "app.jar", "-Xmx32M", "-Xms32M", "-Djava.net.preferIPv4Stack=true"]

EXPOSE 8080
EXPOSE 50051
