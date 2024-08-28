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
# Created on: Feb 26, 2020
#     Author: ekoteva
#

TOP := ../..
include $(TOP)/Makefile.config

SFTP_CHART_NAME := eric-atmoz-sftp
RULESET := rulesets/ruleset2.0-eric-sftp.yaml

.PHONY: build build-sftp deploy deploy-sftp print upgrade replace export undeploy undeploy-sftp clean clean-sftp

build: build-sftp

build-sftp: clean-sftp
	cd $(TOP); $(BOB) -r $(RULESET) init image:build-sftp package:helm-chart-sftp push:docker-image-sftp push:helm-chart-sftp

deploy: clean deploy-sftp

deploy-sftp: build-sftp
	cd $(TOP); $(BOB) -r $(RULESET) deploy
	@echo "SFTP uri: sftp://admin@"`$(KUBECTL) get nodes -n $(KUBE_NAMESPACE) -o jsonpath="{.items[4].status.addresses[0].address}"`":"`$(KUBECTL) get -n $(KUBE_NAMESPACE) -o jsonpath="{.spec.ports[0].nodePort}" services eric-atmoz-sftp`"/data"
	@echo "SFTP credentials: username=admin password=admin"
	@sleep 30
	@echo "SFTP ssh key: "`$(KUBECTL) exec \`$(KUBECTL) get pods -n $(KUBE_NAMESPACE) | grep sftp | awk '{print $$1}'\` -n $(KUBE_NAMESPACE) -- cat /etc/ssh/ssh_host_rsa_key.pub | base64 -w 0`

print:
	@echo "SFTP uri: sftp://admin@"`$(KUBECTL) get nodes -n $(KUBE_NAMESPACE) -o jsonpath="{.items[4].status.addresses[0].address}"`":"`$(KUBECTL) get -n $(KUBE_NAMESPACE) -o jsonpath="{.spec.ports[0].nodePort}" services eric-atmoz-sftp`"/data"
	@echo "SFTP credentials: username=admin password=admin"
	@echo "SFTP ssh key: "`$(KUBECTL) exec \`$(KUBECTL) get pods -n $(KUBE_NAMESPACE) | grep sftp | awk '{print $$1}'\` -n $(KUBE_NAMESPACE) -- cat /etc/ssh/ssh_host_rsa_key.pub | base64 -w 0`

#Not working
upgrade: build
	$(HELM) upgrade --namespace $(KUBE_NAMESPACE) $(SFTP_CHART_NAME)-$(USER) $(TOP)/.bob/$(SFTP_CHART_NAME)*.tgz

replace: clean build undeploy deploy

export: build
	cd $(TOP); $(BOB) -r $(RULESET) export

undeploy: undeploy-sftp

undeploy-sftp:
	@echo "Deleting chart(s) of name $(SFTP_CHART_NAME) in namespace $(KUBE_NAMESPACE)..."
	cd $(TOP); $(BOB) -r $(RULESET) undeploy:undeploy-sftp
	@echo "Deleted chart(s) of name $(SFTP_CHART_NAME) in namespace $(KUBE_NAMESPACE)."

clean: clean-sftp

clean-sftp:
	cd $(TOP); $(BOB) -r $(RULESET) clean
