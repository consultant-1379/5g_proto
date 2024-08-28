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
# Created on: Dec 16, 2019
#     Author: eaoknkr
#

TOP := ../..
include $(TOP)/Makefile.config

RULESET := rulesets/ruleset2.0-eric-nels.yaml
ADP_NAME := eric-test-nels-simulator
#Error 404
ADP_VER := 0.5.0-15
ADP_PACKAGE := $(ADP_NAME)-$(ADP_VER)
ADP_PACKAGE_TGZ := $(ADP_PACKAGE).tgz
ADP_ARTIFACTORY_SRV := arm.rnd.ki.sw.ericsson.se
ADP_ARTIFACTORY_BASE := artifactory
ADP_PROJ_BASE := proj-adp-eric-test-nels-sim-helm

DOWNLOAD_BASE_URL := https://$(ADP_ARTIFACTORY_SRV)/$(ADP_ARTIFACTORY_BASE)/$(ADP_PROJ_BASE)/$(ADP_NAME)
USERNAME := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ADP_ARTIFACTORY_SRV) -u)
PASSWORD := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ADP_ARTIFACTORY_SRV) -p)

.PHONY: all deploy undeploy clean clean-all download version

all: deploy

deploy: 
	cd $(TOP); $(BOB) -r $(RULESET) init create-nels-dir get-nels deploy

undeploy:
	$(HELM) delete $(ADP_NAME)-$(USER) --namespace 5g-bsf-$(USER)

download: $(ADP_PACKAGE_TGZ)
    
$(ADP_PACKAGE_TGZ):
	wget $(DOWNLOAD_BASE_URL)/$(ADP_PACKAGE_TGZ) --user $(USERNAME) --header="X-Auth-Token: $(PASSWORD)"

clean:
	rm -f $(ADP_PACKAGE_TGZ)

clean-all: undeploy clean clean-bob

clean-bob:
	cd $(TOP); $(BOB) -r $(RULESET) clean

version:
	@echo "$(ADP_NAME): $(ADP_VER)" 
