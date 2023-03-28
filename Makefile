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
	docker buildx build -f Dockerfile.native . -t iam:native-latest
	# test the native image
	${M} install -DtestImageTag=native

.PHONY: dockerSave
dockerSave:
	docker save iam:jvm-latest | gzip > iam-jvm_latest.tar.gz
	docker save iam:native-latest | gzip > iam-native_latest.tar.gz

.PHONY: clean
clean:
	${M} clean
	rm -rf .mvn
	rm -rf mvnw mvnw.cmd
