#!make

.PHONY: install
install:
	yarn
	yarn install

.PHONY: buildDev
buildDev:
	yarn run buildDev

.PHONY: check
check:
	yarn run check

.PHONY: run
run: fmt
	BASE_URL="" yarn run dev

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
	rm -rf node_modules
	rm -rf .yarn/cache
	rm -rf .yarn/install-state.gz
	rm -rf build
	rm -rf playwright-report
	rm -rf test-results
