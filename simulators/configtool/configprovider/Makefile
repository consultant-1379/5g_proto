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

TARGET := eric-simulator-config-provider
VERSION := 0.0.1-1

TARGET_TO_LWR := $(shell tr '[:upper:]' '[:lower:]' <<< $(TARGET))

IMAGE_NAME := $(PROJ_ID)/$(TARGET_TO_LWR)

IMAGE_DEV_NAME := $(PROJ_ID)/$(DEV_USER)/$(TARGET_TO_LWR)
IMAGE_DEV_TAG := dev
IMAGE_DEV_FULLNAME := $(IMAGE_DEV_NAME):$(IMAGE_DEV_TAG)


BUILD_LIBS := build/libs
3GPP_DIR := 
3GPP_DIR_HTTP := 

JAR := $(TARGET_TO_LWR)-$(VERSION).jar
JAR_TARGET := $(BUILD_LIBS)/$(JAR)

.PHONY: all image dev-image clean gen-gradle gen-reststubs push-3gpp-specs

all: deploy

auth:
	@echo "Authenticate for pushes to $(ARM_DOCKER_REG_URL);";	\
	docker login -u $(DEV_USER) $(ARM_DOCKER_REG_URL)

deploy: dev-image tag push

publish: freeze push

freeze: assure-checked-in freeze-image update-commit-manifest

freeze-tag:
	@LAST_TAG_PREFIX=$$(git describe --tag | cut -f 1 -d - | cut -f 1,2 -d .);				\
	LAST_TAG_LAST_DIGET=$$(git describe --tag | cut -f 1 -d - | cut -f 3 -d .);				\
	NEW_TAG=$$LAST_TAG_PREFIX.$$(($$LAST_TAG_LAST_DIGET + 1));								\
	git tag -a $$NEW_TAG -m "Auto tagged by configtool/configprovider/Makefile." 

assure-checked-in:
	@[[ -z $$(git status -s) ]] || (echo "There are uncommited changes!" && false)

image: jar Dockerfile
	docker build -t $(TARGET_TO_LWR) .

update-commit-manifest:
	@([[ -z $$(git status -s MANIFEST) ]] && echo "MANIFEST same as the one checked-in.") || \
	git commit -m "Auto commited by configtool/configprovider//Makefile." MANIFEST


# New version tag generated from old (development):
# Case 1: old = v1.2.9    => new = v1.0.10-1	([ "$$OLD_TAG_STEP" = "$$OLD_TAG" ])
# Case 2: old = v1.0.7-22 => new = v1.0.7-23
#
# Repo is user specific!
#
dev-image: jar Dockerfile
	@IMAGE_DIGEST=$$(docker build -q . | cut -f 2 -d :);		\
	echo "Image digest:  $$IMAGE_DIGEST";    \
	if grep -q $$IMAGE_DIGEST MANIFEST; then					\
		echo "New Docker image does not differ from old:";		\
	else														\
		OLD_TAG="$$(cat MANIFEST | cut -f 4 | cut -f 2 -d :)";	\
		OLD_TAG="$${OLD_TAG:-v0.0-0}";							\
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
		REPO_TAG="$(IMAGE_DEV_NAME):$$NEW_TAG";					\
		echo -e "$$IMAGE_DIGEST\ti\t$(TARGET_TO_LWR)\t$$REPO_TAG" > MANIFEST; 	\
		echo "New Docker image:";								\
	fi;															\
	docker tag $$IMAGE_DIGEST $$(cut -f 4 MANIFEST)
	@cp MANIFEST LATEST_IMAGE;									\
	echo "MANIFEST: $$(cat MANIFEST)"

# New version tag generated from old (development):
# Case 1: old = v1.2.9    => new = v1.0.10		([ "$$OLD_TAG_STEP" = "$$OLD_TAG" ])
# Case 2: old = v1.0.7-22 => new = v1.0.7
#
# Repo is public.
#
freeze-image: jar Dockerfile
	@IMAGE_DIGEST=$$(docker build -q . | cut -f 2 -d :);							\
	OLD_TAG="$$(cat MANIFEST | cut -f 4 | cut -f 2 -d :)";							\
	OLD_TAG_STEP="$${OLD_TAG##*-}";													\
		if [ "$$OLD_TAG_STEP" = "$$OLD_TAG" ]; then									\
			OLD_MAIN_VER="$${OLD_TAG%.*}";											\
			OLD_MIN_VER="$${OLD_TAG##*.}";											\
			NEW_MIN_VER="$$(($$OLD_MIN_VER + 1))";									\
			NEW_TAG="$$OLD_MAIN_VER.$$NEW_MIN_VER";									\
		else 																		\
			NEW_TAG="$$(cat MANIFEST | cut -f 4 | cut -f 2 -d : | cut -f 1 -d -)";	\
		fi;																			\
	REPO_TAG="$(IMAGE_NAME):$$NEW_TAG";												\
	echo -e "$$IMAGE_DIGEST\ti\t$(TARGET_TO_LWR)\t$$REPO_TAG" > MANIFEST; 			\
	echo "New Docker image:";														\
	docker tag $$IMAGE_DIGEST $$(cut -f 4 MANIFEST)
	@cp MANIFEST LATEST_IMAGE;														\
	echo "MANIFEST: $$(cat MANIFEST)"

tag:
	@[ -f LATEST_IMAGE ] || (echo "Nothing to be tagged." && false)
	@LATEST_IMAGE_DIGEST="$$(cut -f 1 LATEST_IMAGE)";			\
	LATEST_REPO_TAG="$$(cut -f 4 LATEST_IMAGE)";				\
	echo docker tag $$LATEST_IMAGE_DIGEST $$LATEST_REPO_TAG;	\
	docker tag $$LATEST_IMAGE_DIGEST $$LATEST_REPO_TAG

push:
	@[ -f LATEST_IMAGE ] || (echo "Nothing to be pushed." && false)
	@LATEST_REPO_TAG="$$(cut -f 4 LATEST_IMAGE)";				\
	echo docker push $$LATEST_REPO_TAG;							\
	docker push $$LATEST_REPO_TAG

jar: $(JAR_TARGET)

run: jar
	java -jar $(JAR_TARGET)

Dockerfile: Dockerfile.templ
	@cat Dockerfile.templ																				   			|\
	sed "s/\#\#\#JAVA_JRE_DOCKER_IMG\#\#\#/$$(echo '$(JAVA_JRE_DOCKER_IMG_FAT)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"	|\
	sed "s/\#\#\#BUILD_LIBS\#\#\#/$$(echo '$(BUILD_LIBS)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"		 			|\
	sed "s/\#\#\#JAR\#\#\#/$$(echo '$(JAR)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"		 							 \
	> Dockerfile && echo "Dockerfile created."

gradle.properties: gradle.properties.templ 
	@cat gradle.properties.templ |\
	sed "s/\#\#\#VERSION\#\#\#/$$(echo '$(VERSION)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"  \
	> gradle.properties && echo "gradle.properties created."

gen-gradle: gradle.properties

$(JAR_TARGET): gen-gradle
	gradle jar

gen-reststubs:
	@for OPENAPI_SPEC in src/main/resources/3gpp/*.yaml; do 			\
		swagger generate -i $${OPENAPI_SPEC} -l jaxrs-resteasy -o .; 	\
	done

push-3gpp-specs:
	@PATCHED_OPENAPI_SPECS="{";														\
	FIRST_FILE=true;																\
	for PATCHED_OPENAPI_SPEC in src/main/resources/3gpp/patched/*.yaml; do 			\
		if [ "$$FIRST_FILE" = true ]; then											\
			FIRST_FILE=false;														\
		else																		\
			PATCHED_OPENAPI_SPECS="$$PATCHED_OPENAPI_SPECS,";						\
		fi;																			\
		PATCHED_OPENAPI_SPECS="$$PATCHED_OPENAPI_SPECS$$PATCHED_OPENAPI_SPEC"; 		\
	done; 																			\
	PATCHED_OPENAPI_SPECS="$${PATCHED_OPENAPI_SPECS}}";								\
	echo "Uploading $$PATCHED_OPENAPI_SPECS to /resources/3gpp/ at $(ARM_DOCKER_GEN_REPO_URL)...";		\
	curl -u$(ARM_DOCKER_REG_USRSIG) -T $${PATCHED_OPENAPI_SPECS} $(ARM_DOCKER_GEN_REPO_URL)/resources/3gpp/patched/

clean: gen-gradle
	gradle clean
	rm -rf Dockerfile gradle.properties
	rm -rf src/gen src/main/java/io src/main/webapp/ README.md pom.xml settings.gradle .swagger-codegen/ .swagger-codegen-ignore
