TAG?=2.1.1
NAME:=gateway-api
DOCKER_REPOSITORY:=blacklee123
DOCKER_IMAGE_NAME:=$(DOCKER_REPOSITORY)/$(NAME)
VERSION:=2.1.1
EXTRA_RUN_ARGS?=

.PHONY: test
test-version:
	@echo "$(VERSION)"

build:
	docker buildx build --platform linux/amd64 -f Dockerfile -t $(DOCKER_IMAGE_NAME):$(VERSION) .

push:
	docker tag $(DOCKER_IMAGE_NAME):$(VERSION) $(DOCKER_IMAGE_NAME):latest
	docker push $(DOCKER_IMAGE_NAME):$(VERSION)
	docker push $(DOCKER_IMAGE_NAME):latest

version-set:
	next="$(TAG)" && \
	current="$(VERSION)" && \
	sed -i '' "s/$$NAME:$$current/$$NAME:$$next/g" kustomize/deployment.yaml && \
	sed -i '' "s/<version>$$current<\/version>/<version>$$next<\/version>/g" pom.xml && \
	echo "Version $$next set in pom, deployment , Dockerfile"

release:
	git tag $(VERSION)
	git push origin $(VERSION)

rollout: build push
	kubectl rollout restart -n qaq-dev deployment gateway-api