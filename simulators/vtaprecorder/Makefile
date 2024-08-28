TOP := ../..
include $(TOP)/Makefile.config

CHART_NAME := eric-vtaprecorder
RULESET := rulesets/ruleset2.0-eric-vtaprecorder.yaml

.PHONY: build deploy deploy-full export upgrade replace undeploy clean

build: clean
	cd $(TOP); $(BOB) -r $(RULESET) init dtlsServer image package push

deploy: build
	cd $(TOP); $(BOB) -r $(RULESET) deploy

deploy-full: export VTAPRECORDER_REPLICAS = 3
deploy-full: deploy	

export: build
	cd $(TOP); $(BOB) -r $(RULESET) export

upgrade:
	$(HELM) upgrade --namespace=$(KUBE_NAMESPACE) $(CHART_NAME)-$(USER) $(TOP)/.bob/$(CHART_NAME)*.tgz

replace: clean build undeploy deploy

undeploy:
	cd $(TOP); $(BOB) -r $(RULESET) undeploy

clean:
	cd $(TOP); $(BOB) -r $(RULESET) clean
