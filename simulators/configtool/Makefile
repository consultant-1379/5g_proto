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
# Created on: Aug 30, 2018
#     Author: eedsvs
#

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devenv/CONFIG
include $(TOP)/devtools/CONFIG

SUBDIRS := configprovider

APP := eric-simulator-config

# Currently, one version for both image and chart
VERSION_MAIN := 1.1.1
VERSION_STEP_INIT := 1
VERSION_INIT := $(VERSION_MAIN)-$(VERSION_STEP)

CHART_DESCR := ESC CONFIGTOOL NF. (c) Ericsson AB 2018.
CHART_NAME := $(APP)
CHART_VER := $(VERSION)
# CHART_TGZ := $(CHART_NAME)-$(CHART_VER).tgz

# Note that helm requires dir name and package name to be the same
# target dir and file sources relative to the helm subdir
HELM_TARGET_DIR := $(CHART_NAME)
HELM_FILE_SRCS := Chart.yaml values.yaml templates/deployments.yaml
HELM_FILE_TARGETS := $(HELM_FILE_SRCS:%=helm/$(HELM_TARGET_DIR)/%)


.PHONY: all clean clean-all $(SUBDIRS) chart chart-tgz clean_old_chart deploy deploy-chart helm

all: makesub.all 

# OK
deploy: all manifest chart deploy-chart

# NOT TESTED IN THIS CONTEXT!
publish: freeze chart push-chart deploy-chart

# NOT TESTED IN THIS CONTEXT!
freeze: makesub.publish manifest update-commit-manifest freeze-tag

freeze-tag:
	@LAST_TAG_PREFIX=$$(git describe --tag | cut -f 1 -d - | cut -f 1,2 -d .);				\
	LAST_TAG_LAST_DIGET=$$(git describe --tag | cut -f 1 -d - | cut -f 3 -d .);				\
	NEW_TAG=$$LAST_TAG_PREFIX.$$(($$LAST_TAG_LAST_DIGET + 1));								\
	git tag -a $$NEW_TAG -m "Auto tagged by configtool/Makefile." 


help: usage

usage:
	@echo
	@echo
	@echo "NOT ACURRATE!!! - TO BE DISCUSSED HOW WE WANT THIS IN THE FUTURE"
	@echo
	@echo
	@echo "USAGE:"
	@echo "    'make':"
	@echo "        builds and pushes docker image to $(ARM_DOCKER_REG_URL) under your name for."
	@echo "        development purposes."
	@echo
	@echo "        ==> Use this for your daily development on improving this image."
	@echo
	@echo "    'make publish':"
	@echo "        build and pushes docker image to $(ARM_DOCKER_REG_URL) that will be visible"
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

update-commit-manifest: manifest
	@([[ -z $$(git status -s MANIFEST) ]] && echo "MANIFEST same as the one checked-in.") || \
	git commit -m "Auto commit by configtool/Makefile." MANIFEST

$(SUBDIRS):
	$(MAKE) -C $@

clean: makesub.clean clean-local

clean-all: makesub.clean-all clean-local

clean-local: clean_old_chart


makesub.%:
	@for a in $(SUBDIRS); do 		\
		echo "$(MAKE) -C $$a $*";	\
		$(MAKE) -C $$a $*;			\
	done

manifest: $(TOP)/devtools/MANIFEST $(SUBDIRS)/MANIFEST
	@rm -f MANIFEST.new
	@touch MANIFEST.new
	@for a in $(SUBDIRS); do 								\
		cat $$a/MANIFEST >> MANIFEST.new;					\
	done
	@cat $(TOP)/devtools/MANIFEST >> MANIFEST.new
	@mv MANIFEST.new MANIFEST
	@echo -e "MANIFEST:\n$$(cat MANIFEST)"


chart: clean_old_chart chart-tgz

clean_old_chart:
	rm -rf helm/$(CHART_NAME) helm/Chart.yaml helm/values.yaml *.tgz helm/*.tgz

chart-tgz: $(HELM_FILE_TARGETS)
	$(HELM) release helm/$(HELM_TARGET_DIR)
	$(HELM) package helm/$(HELM_TARGET_DIR)

push-chart:
	curl -u$(ARM_DOCKER_REG_USRSIG) -T $(CHART_NAME)*.tgz "$(PROJ_ID_HELM)"
	
deploy: 
	$(HELM) install --namespace $(KUBE_NAMESPACE) *.tgz
	@echo "		>>> Use 'h list' to see $(CHART_NAME) running."
	@echo

undeploy:
	@echo "Deleting chart(s) of name $(CHART_NAME) in namespace $(KUBE_NAMESPACE)..."
	@$(HELM) --namespace $(KUBE_NAMESPACE) list | grep $(CHART_NAME) | cut -f 1 | while read chart; do echo helm delete --purge $$chart; $(HELM) delete --purge $$chart; done
	@echo "Deleted chart(s) of name $(CHART_NAME) in namespace $(KUBE_NAMESPACE)."

helm/Chart.yaml: helm/Chart.templ.yaml
	@cat helm/Chart.templ.yaml																				   |\
	sed "s/\#\#\#CHART_DESCR\#\#\#/$$(echo '$(CHART_DESCR)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"			   |\
	sed "s/\#\#\#CHART_NAME\#\#\#/$$(echo '$(CHART_NAME)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"			   |\
	sed "s/\#\#\#CHART_VER\#\#\#/$$(echo '$(CHART_VER)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"					\
	> helm/Chart.yaml && echo "helm/Chart.yaml created."

helm/values.yaml: helm/values.templ.yaml
	@IMAGE_NAME="$$(cut -f 4 configprovider/MANIFEST | cut -f 1 -d :)";										\
	IMAGE_TAG="$$(cut -f 4 configprovider/MANIFEST | cut -f 2 -d :)";										\
	cat helm/values.templ.yaml																	   |\
	sed "s/\#\#\#IMAGE_NAME\#\#\#/$$(echo $$IMAGE_NAME | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"	   |\
	sed "s/\#\#\#IMAGE_TAG\#\#\#/$$(echo $$IMAGE_TAG | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"		   |\
	sed "s/\#\#\#CHART_NAME\#\#\#/$$(echo '$(CHART_NAME)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"	\
	> helm/values.yaml	&& echo "helm/values.yaml created."
	
helm/$(HELM_TARGET_DIR)/%.yaml: helm/%.yaml helm/$(HELM_TARGET_DIR)/templates
	cp $< $@

helm/$(HELM_TARGET_DIR)/templates/%.yaml: helm/templates/%.yaml helm/$(HELM_TARGET_DIR)/templates 
	cp $< $@

helm/$(HELM_TARGET_DIR)/templates:
	mkdir -p helm/$(HELM_TARGET_DIR)/templates

