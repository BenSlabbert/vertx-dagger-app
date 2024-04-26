FROM azul/zulu-openjdk-alpine:22-latest

WORKDIR /app

COPY target/catalog-migration-*-jar-with-dependencies.jar app.jar

ENTRYPOINT ["java", "-cp", "lib", "-jar", "app.jar", "-Xmx32M", "-Xms32M", "-Djava.net.preferIPv4Stack=true", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseEpsilonGC"]
