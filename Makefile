#!make

M := "mvn"

.PHONY: build
build: clean fmt
	${M} install

.PHONY: fmt
fmt:
	${M} spotless:apply

.PHONY: wrapper
wrapper:
	${M} -N wrapper:wrapper

.PHONY: native
native: wrapper
	docker buildx build -f Dockerfile.native . -t vertx:native-latest
	# test the native image
	${M} install -DtestImageTag=native

.PHONY: clean
clean:
	${M} clean
	rm -rf .mvn
	rm -rf mvnw mvnw.cmd
