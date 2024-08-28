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

TOP := ..
include $(TOP)/CONFIG
include $(TOP)/devenv/CONFIG
include $(TOP)/devtools/CONFIG

GOPATH := $(PWD)/go

GO_CMD := helloCM
GO_CMD_TO_LWR := $(shell tr '[:upper:]' '[:lower:]' <<< $(GO_CMD))

GO_BASE_PKG := ericsson.com/5g/bsf
GO_TARGET_PKG := $(GO_BASE_PKG)/trials/$(GO_CMD)


# Currently, one version for both image and chart
VERSION := 0.0.1-65

IMAGE_NAME := $(PROJ_ID)/$(GO_CMD_TO_LWR)
IMAGE_TAG := v$(VERSION)
IMAGE_FULLNAME := $(IMAGE_NAME):$(IMAGE_TAG)

CHART_DESCR := DSC-Evo Hello CM Prototype. (c) Ericsson AB 2018.
CHART_NAME := $(GO_CMD_TO_LWR)
CHART_VER := $(VERSION)
CHART_TGZ := $(CHART_NAME)-$(CHART_VER).tgz

# Note that helm requires dir name and package name to be the same
# target dir and file sources relative to the helm subdir
HELM_TARGET_DIR := $(CHART_NAME)
HELM_FILE_SRCS := Chart.yaml values.yaml templates/deployments.yaml
HELM_FILE_TARGETS := $(HELM_FILE_SRCS:%=helm/$(HELM_TARGET_DIR)/%)


.PHONY: usage clean dep build image push pull deploy helm

all: dep image chart

run: build
	$(GOPATH)/bin/$(GO_CMD) --interactive

usage:
	@echo "USAGE:"
	@echo "    'make':"
	@echo "        creates $(GO_CMD) docker image + HELM chart $(CHART_TGZ)."
	@echo "            "
	@echo "    'make deploy':"
	@echo "        pushes docker image to $(ARM_DOCKER_REG_URL)"
	@echo "        deploys HELM chart on Kubernetes cluster $(KUBE_CLUSTER)."
	@echo

clean: clean_old_chart
	rm -rf go/src/github.com go/bin go/pkg

dep:
	export GOPATH=$(GOPATH); $(GO) get ericsson.com/...

image: build
	docker build -t $(IMAGE_FULLNAME) .

build:
	@[ -d go/bin ] || (echo "No bin directory: run 'make dep' first to resolve depencies." && false)
	export GOPATH=$(GOPATH); cd go/bin; $(GO) build $(GO_TARGET_PKG)

lib/adpgs/log/log-service.raml: $(TOP)/lib/adpgs/log/log-service.zip
	rm -rf lib/adpgs/log
	mkdir -p lib/adpgs/log
	cd lib/adpgs/log; unzip $(TOP)/lib/adpgs/log/log-service.zip

push:
	docker login --username $(ARM_DOCKER_REG_USRSIG) $(ARM_DOCKER_REG_URL)
	docker push $(IMAGE_FULLNAME)

pull:
	docker login --username $(ARM_DOCKER_REG_USRSIG) $(ARM_DOCKER_REG_URL)
	docker pull $(IMAGE_FULLNAME)

chart: clean_old_chart $(CHART_TGZ)

clean_old_chart:
	rm -rf helm/$(CHART_NAME) helm/Chart.yaml helm/values.yaml *.tgz helm/*.tgz

$(CHART_TGZ): $(HELM_FILE_TARGETS)
	cd helm; $(HELM) package $(CHART_NAME)
	mv -f helm/*.tgz .
	
deploy: push $(CHART_TGZ)
	$(HELM) install --namespace $(KUBE_NAMESPACE) $(CHART_TGZ)
	@echo "		>>> Use 'h list' to see $(CHART_NAME) running."
	@echo

undeploy:
	@echo "Deleting chart(s) of name $(CHART_NAME) in namespace $(KUBE_NAMESPACE)..."
	@$(HELM) --namespace $(KUBE_NAMESPACE) list | grep $(CHART_NAME) | cut -f 1 | while read chart; do echo helm delete --purge $$chart; $(HELM) delete --purge $$chart; done
	@echo "Deleted chart(s) of name $(CHART_NAME) in namespace $(KUBE_NAMESPACE)."

helm/Chart.yaml: helm/Chart.templ.yaml
	@cat helm/Chart.templ.yaml																				   |\
	sed "s/\#\#\#CHART_DESCR\#\#\#/$$(echo '$(CHART_DESCR)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"			   |\
	sed "s/\#\#\#CHART_NAME\#\#\#/$$(echo '$(CHART_NAME)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"				   |\
	sed "s/\#\#\#CHART_VER\#\#\#/$$(echo '$(CHART_VER)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"	\
	> helm/Chart.yaml && echo "helm/Chart.yaml created."

helm/values.yaml: helm/values.templ.yaml
	@cat helm/values.templ.yaml																				   |\
	sed "s/\#\#\#IMAGE_NAME\#\#\#/$$(echo '$(IMAGE_NAME)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"			   |\
	sed "s/\#\#\#IMAGE_TAG\#\#\#/$$(echo '$(IMAGE_TAG)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"				   |\
	sed "s/\#\#\#CHART_NAME\#\#\#/$$(echo '$(CHART_NAME)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"	\
	> helm/values.yaml	&& echo "helm/values.yaml created."
	
helm/$(HELM_TARGET_DIR)/%.yaml: helm/%.yaml helm/$(HELM_TARGET_DIR)/templates
	cp $< $@

helm/$(HELM_TARGET_DIR)/templates/%.yaml: helm/templates/%.yaml helm/$(HELM_TARGET_DIR)/templates 
	cp $< $@

helm/$(HELM_TARGET_DIR)/templates:
	mkdir -p helm/$(HELM_TARGET_DIR)/templates

