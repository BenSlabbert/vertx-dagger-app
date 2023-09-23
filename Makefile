#!make

M := "mvn"

.PHONY: build
build: fmt
	${M} install

.PHONY: compile
compile: fmt
	${M} compile test-compile

.PHONY: test
test: fmt
	${M} test

.PHONY: package
package: fmt
	${M} package

.PHONY: verify
verify: fmt
	${M} verify

.PHONY: fmtCheck
fmtCheck:
	${M} spotless:check

.PHONY: fmt
fmt:
	${M} spotless:apply

.PHONY: wrapper
wrapper:
	${M} -N wrapper:wrapper

.PHONY: native
native: wrapper
	# todo bind to a local dir for m2 repositories for faster builds
	docker buildx build --progress plain -f Dockerfile.native-parent . -t native-parent-builder:latest
	docker buildx build --progress plain -f Dockerfile.native . -t iam:native-latest --build-arg MODULE=iam-parent/iam-app --build-arg BINARY=iam
	# test the native images
	# https://github.com/oracle/graal/issues/5510 wait for this to be propagated to the docker image
	${M} install -DtestImageTag=native
	# create upx images
	docker buildx build --progress plain -f Dockerfile.upx . -t iam:native-upx-latest --build-arg MODULE=iam-parent/iam-app --build-arg BINARY=iam

.PHONY: dockerSave
dockerSave:
	docker save iam:native-latest | gzip > iam-native_latest.tar.gz
	docker save iam:native-upx-latest | gzip > iam-native-upx_latest.tar.gz
	./extractBinries.sh

.PHONY: clean
clean:
	${M} clean
	rm -rf .mvn mvnw mvnw.cmd *.tar.gz
