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
# Created on: Dec 19, 2021
#     Author: ekoteva
#

TOP := ../..
include $(TOP)/Makefile.config

CAI_CHART_NAME := eric-caisim
RULESET := rulesets/ruleset2.0-eric-caisim.yaml

.PHONY: build build-cai deploy deploy-cai upgrade replace export undeploy undeploy-cai clean clean-cai

build: build-cai

build-cai: clean-cai
	cd $(TOP); $(BOB) -r $(RULESET) init image:build-cai package:helm-chart-cai push:docker-image-cai push:helm-chart-cai

deploy: clean deploy-cai

deploy-cai: build-cai
	cd $(TOP); $(BOB) -r $(RULESET) deploy:deploy-cai

upgrade: build
	$(HELM) upgrade $(CAI_CHART_NAME)-$(USER) $(TOP)/.bob/$(CAI_CHART_NAME)*.tgz --namespace 5g-bsf-$(USER)

replace: clean build undeploy deploy

export: build
	cd $(TOP); $(BOB) -r $(RULESET) export

undeploy: undeploy-cai

undeploy-cai:
	@echo "Deleting chart(s) of name $(CAI_CHART_NAME) in namespace $(KUBE_NAMESPACE)..."
	cd $(TOP); $(BOB) -r $(RULESET) undeploy:undeploy-cai
	@echo "Deleted chart(s) of name $(CAI_CHART_NAME) in namespace $(KUBE_NAMESPACE)."

clean: clean-cai

clean-cai:
	cd $(TOP); $(BOB) -r $(RULESET) clean
