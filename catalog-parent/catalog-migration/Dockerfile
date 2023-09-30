FROM azul/zulu-openjdk-alpine:21-latest

WORKDIR /app

COPY target/catalog-migration-*-jar-with-dependencies.jar app.jar

CMD ["java", "-jar", "app.jar", "-Xmx=32M", "-Xms=32M"]
