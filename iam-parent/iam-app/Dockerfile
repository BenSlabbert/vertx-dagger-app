FROM azul/zulu-openjdk-alpine:21-latest

WORKDIR /app

COPY target/iam.jar app.jar

COPY target/lib lib

ENTRYPOINT ["java", "-cp", "lib", "-jar", "app.jar", "-Xmx32M", "-Xms32M", "-Djava.net.preferIPv4Stack=true"]

EXPOSE 8080
