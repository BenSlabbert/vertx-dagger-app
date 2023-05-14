#!make

M := "mvn"

.PHONY: build
build: clean fmt
	${M} install

.PHONY: compile
compile: clean fmt
	${M} compile test-compile

.PHONY: test
test: clean fmt
	${M} test

.PHONY: package
package: clean fmt
	${M} package

.PHONY: verify
verify: clean fmt
	${M} verify

.PHONY: fmt
fmt:
	${M} spotless:apply

.PHONY: wrapper
wrapper:
	${M} -N wrapper:wrapper

.PHONY: native
native: wrapper
	# todo bind to a local dir for m2 repositories for faster builds
	docker buildx build -f Dockerfile.native-parent . -t native-parent-builder:latest
	docker buildx build -f Dockerfile.native . -t iam:native-latest --build-arg MODULE=iam-parent/iam-app --build-arg BINARY=iam
	docker buildx build -f Dockerfile.native . -t catalog:native-latest --build-arg MODULE=catalog  --build-arg BINARY=catalog
	# test the native images
	${M} install -DtestImageTag=native

.PHONY: dockerSave
dockerSave:
	docker save iam:native-latest | gzip > iam-native_latest.tar.gz
	docker save catalog:native-latest | gzip > catalog-native_latest.tar.gz

.PHONY: clean
clean:
	${M} clean
	rm -rf .mvn mvnw mvnw.cmd *.tar.gz
