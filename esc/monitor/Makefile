# vim:ts=4:sw=4:sw=4:et
#
# COPYRIGHT ERICSSON GMBH 2022
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

BOB := bob/bob
CHART_NAME_MONITOR := eric-sc-monitor

.PHONY: build clean deploy replace undeploy upgrade

prepare:
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete deployment eric-sc-monitor
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete eric-sc-monitor-secret \
	                                                eric-sc-monitor-tls-external-server-certificate-secret \
	                                                eric-sc-monitor-tls-external-server-certificate-secret-emergency

build:
	cd $(TOP); mvn package -DskipTests -Djacoco.skip=true -am -pl :eric-sc-monitor
	cd $(TOP); $(BOB) -r $(RULESET_SC) init build-images:monitor build-images:monitor $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET_SC) package-helm:monitor push-helm:monitor $(NODEBUG)

deploy: build
	$(HELM) install $(CHART_NAME_MONITOR)-$(USER) \
	                $(TOP)/.bob/$(CHART_NAME_MONITOR)-$(shell cat $(TOP)/.bob/var.monitor-version).tgz \
	                --namespace $(KUBE_NAMESPACE) \
	                --set spec.replicaCount=1

upgrade: build
	$(HELM) upgrade $(CHART_NAME_MONITOR)-$(USER) \
	        $(TOP)/.bob/$(CHART_NAME_MONITOR)-$(shell cat $(TOP)/.bob/var.monitor-version).tgz \
	        --namespace $(KUBE_NAMESPACE)

replace: build undeploy deploy

undeploy:
	$(HELM) delete $(CHART_NAME_MONITOR)-$(USER) \
	                --namespace $(KUBE_NAMESPACE)

clean:
	@echo "Removing existing local chart files."
	rm -f $(TOP)/.bob/$(CHART_NAME_MONITOR)*.tgz
	cd $(TOP); mvn clean -am -pl :eric-sc-monitor

