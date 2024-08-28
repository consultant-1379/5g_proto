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
#

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devenv/CONFIG
include $(TOP)/devtools/CONFIG


VERSION := latest

IMAGE_NAME := $(PROJ_ID)/nrf-interrogator
IMAGE_TAG := v$(VERSION)
IMAGE_FULLNAME := $(IMAGE_NAME):$(IMAGE_TAG)

CHART_DESCR := Simulation of the NRF Interrogator
CHART_NAME := nrf-interrogator
CHART_VER := $(VERSION)
CHART_TGZ := $(CHART_NAME)-$(CHART_VER).tgz

# Note that helm requires dir name and package name to be the same
# target dir and file sources relative to the helm subdir
HELM_NAME := $(CHART_NAME)-$(USER)
HELM_TARGET_DIR := $(CHART_NAME)
HELM_FILE_SRCS := Chart.yaml values.yaml templates/deployments.yaml
HELM_FILE_TARGETS := $(HELM_FILE_SRCS:%=helm/$(HELM_TARGET_DIR)/%)


.PHONY: usage clean dep build image push pull deploy helm

all: image $(CHART_TGZ)

usage:
	@echo "USAGE:"
	@echo "    'make':"
	@echo "        creates envoy docker image + HELM chart $(CHART_TGZ)."
	@echo "            "
	@echo "    'make deploy':"
	@echo "        pushes docker image to $(ARM_DOCKER_REG_URL)"
	@echo "        deploys HELM chart on Kubernetes cluster $(KUBE_CLUSTER)."
	@echo

clean:
	rm -rf helm/Chart.yaml helm/values.yaml helm/$(HELM_TARGET_DIR) $(CHART_TGZ)


image: 
	docker build -t $(IMAGE_FULLNAME) .

push: image
#	docker login --username $(ARM_DOCKER_REG_USRSIG) $(ARM_DOCKER_REG_URL)
	docker push $(IMAGE_FULLNAME)

pull:
	#docker login --username $(ARM_DOCKER_REG_USRSIG) $(ARM_DOCKER_REG_URL)
	docker pull $(IMAGE_FULLNAME)

chart: $(CHART_TGZ)

$(CHART_TGZ): $(HELM_FILE_TARGETS)
	cd helm; $(HELM) package $(CHART_NAME)
	mv -f helm/$(CHART_TGZ) .
	rm -rf helm/$(CHART_NAME) helm/Chart.yaml helm/values.yaml

deploy: push $(CHART_TGZ)
	$(HELM) install --namespace $(KUBE_NAMESPACE) --name $(HELM_NAME) $(CHART_TGZ)
	@echo "		>>> Use 'h list' to see $(CHART_NAME) running."
	@echo

undeploy:
	@echo "Deleting chart(s) of name $(CHART_NAME) in namespace $(KUBE_NAMESPACE)..."
	@$(HELM) --namespace $(KUBE_NAMESPACE) list | grep $(CHART_NAME) | cut -f 1 | while read chart; do echo helm delete --purge $$chart; $(HELM) delete --purge $$chart; done
	@echo "Deleted chart(s) of name $(CHART_NAME) in namespace $(KUBE_NAMESPACE)."

helm/Chart.yaml:
	@echo 'apiVersion: $(HELM_CHART_API_VER)'		>  helm/Chart.yaml
	@echo 'description: $(CHART_DESCR)'				>> helm/Chart.yaml
	@echo 'name: $(CHART_NAME)'						>> helm/Chart.yaml
	@echo 'version: $(CHART_VER)'					>> helm/Chart.yaml
	@echo 'helm/Chart.yaml created.'
	@[ $$(cat helm/Chart.yaml | wc -l) -eq 4 ] || (echo "Failed to create helm/Chart.yaml!" > /dev/stderr && false)

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

