FROM azul/zulu-openjdk-alpine:17-latest

WORKDIR /app

COPY target/iam.jar app.jar

COPY target/lib lib

ENTRYPOINT ["java", "-cp", "lib", "-jar", "app.jar"]

EXPOSE 8080
EXPOSE 50051
