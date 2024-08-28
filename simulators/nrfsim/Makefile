# vim:ts=4:sw=4:sw=4:et
#
# COPYRIGHT ERICSSON GMBH 2019
#
# The copyright to the computer program(s) herein is the property
# of Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written
# permission of Ericsson GmbH in accordance with
# the terms and conditions stipulated in the agreement/contract
# under which the program(s) have been supplied.
#
# Created on: Jan 22, 2019
#     Author: eedstl
#

TOP := ../..

include $(TOP)/Makefile.config

CHART_NAME := eric-nrfsim
RULESET := rulesets/ruleset2.0-eric-nrfsim.yaml

.PHONY: all build build-full clean deploy deploy-full deployment-status export undeploy upgrade replace

all: deployment-status clean deploy

build:
	cd $(TOP); $(BOB) -r $(RULESET) init build image package push

build-full:
	cd $(TOP); $(BOB) -r $(RULESET) init build image package-full push

clean:
	cd $(TOP); $(BOB) -r $(RULESET) clean

deploy: deployment-status build
	cd $(TOP); $(BOB) -r $(RULESET_SC) install-certificates:nrfsim
	cd $(TOP); $(BOB) -r $(RULESET) deploy

deploy-full: deployment-status build-full
	cd $(TOP); $(BOB) -r $(RULESET_SC) install-certificates:nrfsim
	cd $(TOP); $(BOB) -r $(RULESET) deploy

deployment-status:
ifeq ($(shell helm list -aA | grep eric-nrfsim-$(USER) | wc -l ),1)
	@echo "###############################"
	@echo "# NRFSim is already deployed. #"
	@echo "###############################"
	exit 1
endif

export:
	cd $(TOP); $(BOB) -r $(RULESET) init build image package export

undeploy:
	cd $(TOP); $(BOB) -r $(RULESET) undeploy

upgrade: build
	helm upgrade $(CHART_NAME)-$(USER) $(TOP)/.bob/$(CHART_NAME)-$(shell cat $(TOP)/.bob/var.nrfsim-version).tgz --namespace 5g-bsf-$(USER)

replace: build undeploy
	#Wait for the deployment-status check to pass
	sleep 3
	make deploy
