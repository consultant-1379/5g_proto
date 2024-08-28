# vim:ts=4:sw=4:sw=4:et
#
# COPYRIGHT ERICSSON GMBH 2018
#
# The copyright to the computer program(s) herein is the property
# of Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written
# permission of Ericsson GmbH in accordance with
# the terms and conditions stipulated in the agreement/contract
# under which the program(s) have been supplied.
#
# Created on: Jun 15, 2018
#     Author: eedsvs
#

TOP := ../../..
include $(TOP)/CONFIG
include $(TOP)/devenv/CONFIG
include $(TOP)/devtools/CONFIG

GOPATH := $(PWD)/go

GO_CMD := bsfmgr
GO_CMD_TO_LWR := $(shell tr '[:upper:]' '[:lower:]' <<< $(GO_CMD))

GO_BASE_PKG := ericsson.com/5g
GO_TARGET_PKG := $(GO_BASE_PKG)/$(GO_CMD)


# Currently, one version for both image and chart
VERSION_MAIN := 7.5.3
VERSION_STEP := 65
VERSION := $(VERSION_MAIN)-$(VERSION_STEP)

IMAGE_NAME := $(PROJ_ID)/$(GO_CMD_TO_LWR)

IMAGE_DEV_NAME := $(PROJ_ID)/$(DEV_USER)/$(GO_CMD_TO_LWR)
IMAGE_DEV_TAG := dev
IMAGE_DEV_FULLNAME := $(IMAGE_DEV_NAME):$(IMAGE_DEV_TAG)


.PHONY: all run help clean dep image image-create image-tag build push pull 

all: dep dev-image tag

publish: push

# TODO: add "git push origin $${GIT_TAG}" below. Currently being rejected by Gerrit
freeze: assure-checked-in dep freeze-image tag
	[[ -z $$(git status -s MANIFEST) ]] || git commit -m "Auto commit by bsf/bsfmgr/Makefile." MANIFEST

# tagging in git interferes with helm tagging one dir level up:		
#	@GIT_TAG="$$(cut -f 3 MANIFEST)-$$(cut -f 4 MANIFEST | cut -f 2 -d :)+$$(cut -f 1 MANIFEST | cut -b 1-12)"
#	[[ -z $$(git describe --tags | grep $${GIT_TAG}) ]] &&	\
#	(git tag -am "Auto tagged by bsf/bsfmgr/Makefile.: $${GIT_TAG}" $${GIT_TAG} && \
#	echo "New tag \"$${GIT_TAG}\" sucessfully created") || \
#	echo "Tag \"$${GIT_TAG}\" already existed."

assure-checked-in:
	@true || [[ -z $$(git status -s) ]] || (echo "There are uncommited changes!" && false)

run: build
	$(GOPATH)/bin/$(GO_CMD) --interactive

help: usage

usage:
	@echo "USAGE:"
	@echo "    'make':"
	@echo "        builds and pushes docker image to your private part of $(ARM_DOCKER_REG_URL)."
	@echo
	@echo "        ==> Use this for your daily development on improving this image."
	@echo
	@echo "    'make publish':"
	@echo "        build and pushes docker image to the public part of $(ARM_DOCKER_REG_URL) visible"
	@echo "        to everyone. Based on MANIFEST a new version number stepping will be created"
	@echo "        and automatically checked-in to GIT. Additionally, a new GIT tag will be"
	@echo "        created to be able to rebuild the new Docker image any time later."
	@echo
	@echo "        ==> Use this when an image change has to be made visible to everyone. Be aware"
	@echo "            and communicate possible impacts or risks to everyone potentially impacted."
	@echo
	@echo
	@echo "        Main version changes have to be done by editing MANIFEST manually. Start with"
	@echo "        a stepping of zero to assure the new main version get stepping 1."
	@echo
	@echo "        Example:"
	@echo "        Current version tag is \"v1.0.0-5\" and you want to create a new minor release."
	@echo "        Change \"v1.0.0-5\" in MANIFEST to \"v1.0.1-0\". Commit your changes and freeze."
	@echo "        The new image will be using version tag \"v1.0.1-1\"."
	@echo

clean:
	rm -rf go/src/github.com go/bin go/pkg

dep:
	export GOPATH=$(GOPATH); $(GO) get ericsson.com/...

tag:
	@[ -f LATEST_IMAGE ] || (echo "Nothing to be tagged." && false)
	@LATEST_IMAGE_DIGEST="$$(cut -f 1 LATEST_IMAGE)";			\
	LATEST_REPO_TAG="$$(cut -f 4 LATEST_IMAGE)";				\
	echo docker tag $$LATEST_IMAGE_DIGEST $$LATEST_REPO_TAG;	\
	docker tag $$LATEST_IMAGE_DIGEST $$LATEST_REPO_TAG

push:
	@[ -f LATEST_IMAGE ] || (echo "Nothing to be tagged." && false)
	@LATEST_REPO_TAG="$$(cut -f 4 LATEST_IMAGE)";				\
	echo docker push $$LATEST_REPO_TAG;							\
	docker push $$LATEST_REPO_TAG

old-dev-image: build
	@IMAGE_DIGEST=$$(docker build -t $(IMAGE_DEV_FULLNAME) -q . | cut -f 2 -d :);			\
	echo -e "$$IMAGE_DIGEST\ti\t$(GO_CMD_TO_LWR)\t$(IMAGE_DEV_FULLNAME)" > LATEST_IMAGE; 	\
	echo "LATEST_IMAGE: $$(cat LATEST_IMAGE)"

freeze-image: build
	@IMAGE_DIGEST=$$(docker build -q . | cut -f 2 -d :);					\
	NEW_TAG="$$(cat MANIFEST | cut -f 4 | cut -f 2 -d : | cut -f 1 -d -)";	\
	REPO_TAG="$(IMAGE_NAME):$$NEW_TAG";										\
	echo -e "$$IMAGE_DIGEST\ti\t$(GO_CMD_TO_LWR)\t$$REPO_TAG" > MANIFEST; 	\
	echo "New Docker image:";												\
	docker tag $$IMAGE_DIGEST $$(cut -f 4 MANIFEST)
	@cp MANIFEST LATEST_IMAGE;									\
	echo "MANIFEST: $$(cat MANIFEST)"

dev-image: build
	@IMAGE_DIGEST=$$(docker build -q . | cut -f 2 -d :);		\
	if grep -q $$IMAGE_DIGEST MANIFEST; then					\
		echo "New Docker image does not differ from old:";		\
	else														\
		OLD_TAG="$$(cat MANIFEST | cut -f 4 | cut -f 2 -d :)";	\
		OLD_TAG_STEP="$${OLD_TAG##*-}";							\
		if [ "$$OLD_TAG_STEP" = "$$OLD_TAG" ]; then				\
			OLD_MAIN_VER="$${OLD_TAG%.*}";						\
			OLD_MIN_VER="$${OLD_TAG##*.}";						\
			NEW_MIN_VER="$$(($$OLD_MIN_VER + 1))";				\
			NEW_TAG="$$OLD_MAIN_VER.$$NEW_MIN_VER-1";			\
		else 													\
			NEW_TAG_STEP="$$(($$OLD_TAG_STEP + 1))";			\
			OLD_TAG_PREFIX="$${OLD_TAG%-*}";					\
			NEW_TAG="$$OLD_TAG_PREFIX-$$NEW_TAG_STEP";			\
		fi;														\
		REPO_TAG="$(IMAGE_NAME):$$NEW_TAG";						\
		echo -e "$$IMAGE_DIGEST\ti\t$(GO_CMD_TO_LWR)\t$$REPO_TAG" > MANIFEST; 	\
		echo "New Docker image:";								\
	fi;															\
	docker tag $$IMAGE_DIGEST $$(cut -f 4 MANIFEST)
	@cp MANIFEST LATEST_IMAGE;									\
	echo "MANIFEST: $$(cat MANIFEST)"

build:
	@[ -d go/bin ] || (echo "No bin directory: run 'make dep' first to resolve depencies." && false)
	export GOPATH=$(GOPATH); cd go/bin; $(GO) build $(GO_TARGET_PKG)

lib/adpgs/log/log-service.raml: $(TOP)/lib/adpgs/log/log-service.zip
	rm -rf lib/adpgs/log
	mkdir -p lib/adpgs/log
	cd lib/adpgs/log; unzip $(TOP)/lib/adpgs/log/log-service.zip

login:
	docker login --username $(ARM_DOCKER_REG_USRSIG) $(ARM_DOCKER_REG_URL)
