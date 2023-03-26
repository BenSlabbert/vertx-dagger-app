#!make

M := "mvn"

.PHONY: build
build: clean fmt
	${M} install ${MVN_FLAGS}
	docker buildx build . -t vertx:jvm-latest

.PHONY: fmt
fmt:
	${M} spotless:apply

.PHONY: wrapper
wrapper:
	${M} -N wrapper:wrapper

.PHONY: native
native: wrapper
	docker buildx build -f Dockerfile.native . -t vertx:native-latest

.PHONY: clean
clean:
	${M} clean
	rm -rf .mvn
	rm -rf mvnw mvnw.cmd
