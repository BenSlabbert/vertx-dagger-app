ARG JAVA_MINIMAL=/opt/jre

FROM azul/zulu-openjdk-alpine:21-latest AS CUSTOM_JRE

RUN apk --no-cache add curl ca-certificates binutils

ARG JAVA_MINIMAL

RUN jlink \
    --verbose \
    --add-modules java.base,java.sql,java.naming,java.security.jgss,java.security.sasl,java.instrument,jdk.unsupported,java.management \
    --compress zip-6 \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --output "$JAVA_MINIMAL"

FROM alpine:3

ARG JAVA_MINIMAL

ENV JAVA_MINIMAL=/opt/jre
ENV PATH="$PATH:$JAVA_MINIMAL/bin"
COPY --from=CUSTOM_JRE "$JAVA_MINIMAL" "$JAVA_MINIMAL"

WORKDIR /app

COPY target/catalog.jar app.jar

ENTRYPOINT ["java", "-cp", "lib", "-jar", "app.jar", "-XX:+UseZGC", "-XX:+ZGenerational", "-Xlog:gc*", "-Xmx32M", "-Xms32M", "-Djava.net.preferIPv4Stack=true"]

EXPOSE 8080
