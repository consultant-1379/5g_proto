# vim:ts=4:sw=4:sw=4:et
#
# COPYRIGHT ERICSSON GMBH 2020
#
# The copyright to the computer program(s) herein is the property
# of Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written
# permission of Ericsson GmbH in accordance with
# the terms and conditions stipulated in the agreement/contract
# under which the program(s) have been supplied.
#
# Created on: Jul 15, 2020
#     Author: eedbjhe
#

TOP := ../..
include $(TOP)/Makefile.config

CHART_NAME := eric-seppsim
RULESET := rulesets/ruleset2.0-eric-seppsim.yaml

.PHONY: all build build-full build-seppsim-scp build-seppsim-scp-full clean deploy deploy-full deploy-seppsim-scp deploy-seppsim-scp-full export lint upgrade replace undeploy

all: clean deploy

build:
	cd $(TOP); $(BOB) -r $(RULESET) init build image package push

build-full:
	cd $(TOP); $(BOB) -r $(RULESET) init build image package-full push

build-full-n32c:
	cd $(TOP); $(BOB) -r $(RULESET) init build image package-full push

build-seppsim-scp:
	cd $(TOP); $(BOB) -r $(RULESET) init build image package push

build-seppsim-scp-full:
	cd $(TOP); $(BOB) -r $(RULESET) init build image package-full push

clean:
	cd $(TOP); $(BOB) -r $(RULESET) clean

deploy: deployment-status build
	cd $(TOP); $(BOB) -r $(RULESET_SC) install-certificates:seppsim
	cd $(TOP); $(BOB) -r $(RULESET) deploy

deploy-full: deployment-status build-full
	cd $(TOP); $(BOB) -r $(RULESET_SC) install-certificates:seppsim
	cd $(TOP); $(BOB) -r $(RULESET) deploy

deploy-full-n32c: deployment-status build-full-n32c
	cd $(TOP); $(BOB) -r $(RULESET_SC) install-certificates:seppsim-n32c
	cd $(TOP); $(BOB) -r $(RULESET) deploy

deploy-seppsim-scp: deployment-status build-seppsim-scp
	cd $(TOP); $(BOB) -r $(RULESET_SC) install-certificates:seppsim-scp
	cd $(TOP); $(BOB) -r $(RULESET) deploy
	
deploy-seppsim-scp-full: deployment-status build-seppsim-scp-full
	cd $(TOP); $(BOB) -r $(RULESET_SC) install-certificates:seppsim-scp
	cd $(TOP); $(BOB) -r $(RULESET) deploy

deployment-status:
ifeq ($(shell helm list -aA | grep $(CHART_NAME)-$(USER) | wc -l ),1)
	@echo "################################"
	@echo "# SEPPSim is already deployed. #"
	@echo "################################"
	exit 1
endif

export:
	cd $(TOP); $(BOB) -r $(RULESET) init build image export

export-seppsim-scp: export

lint:
	cd $(TOP); $(BOB) -r $(RULESET) lint
	
upgrade: build
	$(HELM) upgrade $(CHART_NAME)-$(USER) $(TOP)/.bob/$(CHART_NAME)-$(shell cat $(TOP)/.bob/var.seppsim-version).tgz --namespace 5g-bsf-$(USER)

replace: build undeploy
	#Wait for the deployment-status check to pass
	sleep 3
	make deploy

undeploy:
	cd $(TOP); $(BOB) -r $(RULESET) undeploy
