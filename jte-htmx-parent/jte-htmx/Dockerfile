ARG JAVA_MINIMAL=/opt/jre

FROM azul/zulu-openjdk-alpine:21-latest AS CUSTOM_JRE

RUN apk --no-cache add curl ca-certificates binutils

ARG JAVA_MINIMAL

COPY target/jte-htmx.jar app.jar

# https://slinkydeveloper.com/Vert-x-Container-images-with-jlink/
RUN MODS=$(jdeps -q --print-module-deps --ignore-missing-deps app.jar) && \
    echo "MODS = $MODS" && \
    MODS="$MODS,jdk.naming.dns" && \
    echo "MODS = $MODS" && \
    jlink \
    --verbose \
    --add-modules "$MODS" \
    --compress zip-6 \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --output "$JAVA_MINIMAL"

FROM alpine:3

RUN apk --no-cache add curl

ARG JAVA_MINIMAL

ENV JAVA_MINIMAL=/opt/jre
ENV PATH="$PATH:$JAVA_MINIMAL/bin"
COPY --from=CUSTOM_JRE "$JAVA_MINIMAL" "$JAVA_MINIMAL"

WORKDIR /app

COPY target/jte-htmx.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar", "-XX:+UseZGC", "-XX:+ZGenerational", "-Xlog:gc*", "-Xmx32M", "-Xms32M", "-Djava.net.preferIPv4Stack=true"]

EXPOSE 8080
