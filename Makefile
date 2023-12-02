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
	docker buildx build --progress plain -f Dockerfile.native . -t iam-grpc:native-latest --build-arg MODULE=iam-parent/iam-grpc --build-arg BINARY=iam-grpc
	docker buildx build --progress plain -f Dockerfile.native . -t catalog:native-latest --build-arg MODULE=catalog-parent/catalog-app --build-arg BINARY=catalog
	docker buildx build --progress plain -f Dockerfile.native . -t payment:native-latest --build-arg MODULE=payment-parent/payment-app --build-arg BINARY=payment
	${M} install -DtestImageTag=native
	# create upx images
	docker buildx build --progress plain -f Dockerfile.upx . -t iam:native-upx-latest --build-arg MODULE=iam-parent/iam-app --build-arg BINARY=iam
	docker buildx build --progress plain -f Dockerfile.upx . -t iam-grpc:native-upx-latest --build-arg MODULE=iam-parent/iam-grpc --build-arg BINARY=iam-grpc
	docker buildx build --progress plain -f Dockerfile.upx . -t catalog:native-upx-latest --build-arg MODULE=catalog-parent/catalog-app --build-arg BINARY=catalog
	docker buildx build --progress plain -f Dockerfile.upx . -t payment:native-upx-latest --build-arg MODULE=payment-parent/payment-app --build-arg BINARY=payment

.PHONY: dockerSave
dockerSave:
	docker save iam:native-latest | gzip > iam-native_latest.tar.gz
	docker save iam-grpc:native-latest | gzip > iam-grpc-native_latest.tar.gz
	docker save catalog:native-latest | gzip > catalog-native_latest.tar.gz
	docker save payment:native-latest | gzip > payment-native_latest.tar.gz
	docker save iam:native-upx-latest | gzip > iam-native-upx_latest.tar.gz
	docker save iam-grpc:native-upx-latest | gzip > iam-grpc-native-upx_latest.tar.gz
	docker save catalog:native-upx-latest | gzip > catalog-native-upx_latest.tar.gz
	docker save payment:native-upx-latest | gzip > payment-native-upx_latest.tar.gz
	./extractBinries.sh

.PHONY: clean
clean:
	${M} clean
	rm -rf mvnw mvnw.cmd *.tar.gz
