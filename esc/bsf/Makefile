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
# Created on: Dec 13, 2018
#     Author: eedbjhe
#
# This makefile is for quickly updating the deployed BSF Manager and Worker without redeploying the integration chart.
#
# upgrade: Uses the helm upgrade command for the existing deployment
# replace: Removes the current deployment and redeploys the new version

TOP := ../..
BOB := bob/bob
CHART_NAME = eric-bsf
RULESET := ruleset2.0.yaml
include $(TOP)/CONFIG
include $(TOP)/devenv/CONFIG
include $(TOP)/devtools/CONFIG

include $(TOP)/CONFIG

build:
	@if [ ! -d "$(TOP)/.bob" ]; then cd $(TOP); $(BOB) -r $(RULESET) init; fi
	cd $(TOP); $(BOB) -r $(RULESET) build image push:docker-images package:update-helm-chart lint package:helm-chart 
	
deploy: clean build
	$(HELM) install --namespace $(KUBE_NAMESPACE) --name $(CHART_NAME)-$(USER) $(TOP)/.bob/$(CHART_NAME)*.tgz

deploy-demo: clean build
	$(HELM) install --namespace $(DEMO_NAMESPACE) --name $(CHART_NAME)-demo $(TOP)/.bob/$(CHART_NAME)*.tgz

upgrade: build
	$(HELM) upgrade $(CHART_NAME)-$(USER) $(TOP)/.bob/$(CHART_NAME)*.tgz

replace: clean build undeploy deploy

replace-demo: undeploy-demo deploy-demo
	
undeploy:
	@echo "Deleting chart(s) of name $(CHART_NAME) in namespace $(KUBE_NAMESPACE)..."	
	@$(HELM) --namespace $(KUBE_NAMESPACE) list | egrep "\s+$(CHART_NAME)-" | cut -f 1 | xargs -I '{}' $(HELM) delete --purge {}
	@echo "Deleted chart(s) of name $(CHART_NAME) in namespace $(KUBE_NAMESPACE)."	

undeploy-demo:
	@echo "Deleting chart(s) of name $(CHART_NAME) in namespace $(DEMO_NAMESPACE)..."	
	@$(HELM) --namespace $(DEMO_NAMESPACE) list | egrep "\s+$(CHART_NAME)-" | cut -f 1 | xargs -I '{}' $(HELM) delete --purge {}
	@echo "Deleted chart(s) of name $(CHART_NAME) in namespace $(DEMO_NAMESPACE)."	
	
clean:
	@echo "Removing existing local chart files."
	rm -f $(TOP)/.bob/*.tgz	
