FROM azul/zulu-openjdk-alpine:23-latest

WORKDIR /app

COPY target/payment-migration-*-jar-with-dependencies.jar app.jar

ENTRYPOINT ["java", "-cp", "lib", "-jar", "app.jar", "-Xmx32M", "-Xms32M", "-Djava.net.preferIPv4Stack=true", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseEpsilonGC"]

LABEL org.opencontainers.image.source="https://github.com/BenSlabbert/vertx-dagger-app"
