#!make

IMAGE_NAME := iam-sveltekit
IMAGE_TAG := latest

.PHONY: install
install:
	yarn
	yarn install

.PHONY: buildDev
buildDev:
	yarn run buildDev

.PHONY: buildDocker
buildDocker:
	yarn run buildDocker
	docker buildx build . -t ${IMAGE_NAME}:${IMAGE_TAG}

.PHONY: dockerSave
dockerSave:
	docker save ${IMAGE_NAME}:${IMAGE_TAG} | gzip > ${IMAGE_NAME}_${IMAGE_TAG}.tar.gz

.PHONY: check
check:
	yarn run check

.PHONY: run
run: fmt
	BASE_URL="" BROWSER=firefox yarn run dev

.PHONY: lint
lint:
	yarn run lint

.PHONY: fmt
fmt:
	yarn run format

.PHONY: test
test:
	yarn playwright install --with-deps
	yarn run test
	yarn playwright test

.PHONY: showReport
showReport:
	yarn playwright show-report

.PHONY: clean
clean:
	rm -rf build
	rm -rf playwright-report
	rm -rf test-results
	rm -f ${IMAGE_NAME}_${IMAGE_TAG}.tar.gz
