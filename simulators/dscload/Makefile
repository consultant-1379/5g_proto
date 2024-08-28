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
# Created on: Jun 9, 2020
#     Author: ekonpap
#

TOP := ../..
include $(TOP)/Makefile.config

DSCLOAD_CHART_NAME := eric-dscload
RULESET := rulesets/ruleset2.0-eric-dscload.yaml

.PHONY: build build-dscload deploy deploy-load deploy-dscload upgrade replace export undeploy undeploy-load undeploy-dscload clean-dscload clean

build: build-dscload

build-dscload: clean-dscload
	cd $(TOP); $(BOB) -r $(RULESET) init image:build-dscload package:helm-chart-dscload push:docker-image-dscload push:helm-chart-dscload

deploy: clean deploy-dscload

deploy-load: clean deploy-dscload

deploy-dscload: build-dscload
	cd $(TOP); $(BOB) -r $(RULESET) deploy:deploy-dscload

upgrade: build
	$(HELM) upgrade $(DSCLOAD_CHART_NAME)-$(USER) $(TOP)/.bob/$(DSCLOAD_CHART_NAME)-$(shell cat $(TOP)/.bob/var.dscload-version).tgz --namespace 5g-bsf-$(USER)

replace: clean build undeploy deploy

export: build
	cd $(TOP); $(BOB) -r $(RULESET) export

undeploy: undeploy-dscload

undeploy-load: undeploy-dscload

undeploy-dscload:
	@echo "Deleting chart(s) of name $(DSCLOAD_CHART_NAME) in namespace $(KUBE_NAMESPACE)..."
	cd $(TOP); $(BOB) -r $(RULESET) undeploy:undeploy-dscload
	@echo "Deleted chart(s) of name $(DSCLOAD_CHART_NAME) in namespace $(KUBE_NAMESPACE)."

clean-dscload:
	cd $(TOP); $(BOB) -r $(RULESET) clean

clean: clean-dscload
