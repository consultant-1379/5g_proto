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
# Created on: Jun 03, 2018
#     Author: eedsvs
#

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devenv/CONFIG
include $(TOP)/devtools/CONFIG

ADP_NAME := eric-data-visualizer-kb
ADP_VER := 1.3.0-9
ADP_PACKAGE := $(ADP_NAME)-$(ADP_VER)
ADP_PACKAGE_TGZ := $(ADP_PACKAGE).tgz
ADP_ARTIFACTORY_SRV := arm.rnd.ki.sw.ericsson.se
ADP_ARTIFACTORY_BASE := artifactory
ADP_PROJ_BASE := proj-adp-gs-all-helm

DOWNLOAD_BASE_URL := https://$(ADP_ARTIFACTORY_SRV)/$(ADP_ARTIFACTORY_BASE)/$(ADP_PROJ_BASE)/$(ADP_NAME)
USERNAME := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ADP_ARTIFACTORY_SRV) -u)
PASSWORD := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ADP_ARTIFACTORY_SRV) -p)

.PHONY: all deploy undeploy clean clean-all

all: deploy

deploy: $(ADP_PACKAGE_TGZ)
	$(HELM) install --namespace $(KUBE_NAMESPACE) --set ingress.enabled=true --set ingress.hosts={kibana-$(KUBE_NAMESPACE).$(KUBE_HOST).rnd.gic.ericsson.se} $(ADP_PACKAGE_TGZ)

undeploy:
	@echo "Target undeploy not support yet!" > /dev/stderr && false

$(ADP_PACKAGE_TGZ):
	wget $(DOWNLOAD_BASE_URL)/$(ADP_PACKAGE_TGZ) --user $(USERNAME) --header="X-Auth-Token: $(PASSWORD)"

clean:
	rm -f $(ADP_PACKAGE_TGZ)

clean-all: undeploy clean

version:
	@echo "$(ADP_NAME): $(ADP_VER)" 
