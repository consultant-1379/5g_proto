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
# Created on: Jun 18, 2020
#     Author: eedstl
#
# This makefile is for quickly updating the deployed SCP Manager and Worker without redeploying the integration chart.
#
# upgrade: Uses the helm upgrade command for the existing deployment
# replace: Removes the current deployment and redeploys the new version

TOP := ../..
#BOB := bob/bob
SEPP_CHART_NAME = eric-sepp
#RULESET := rulesets/ruleset2.0-eric-sepp.yaml
#include $(TOP)/CONFIG
#include $(TOP)/devenv/CONFIG
#include $(TOP)/devtools/CONFIG

#include $(TOP)/CONFIG

include $(TOP)/Makefile.config

.PHONY: build deploy deploy-prepare undeploy upgrade

build:
	cd $(TOP); mvn package -DskipTests -Djacoco.skip=true -am -pl :eric-sepp-manager,:eric-scp-sds,:eric-sc-cert-notifier,:eric-sc-tlskeylogagent,:eric-sc-tapcollector,:vtaprecorder
	cd $(TOP); $(BOB) -r $(RULESET_SC) init build:logfwdr build-images:sds build-images:certnotifier build-images:sepp push-images:sds push-images:certnotifier push-images:logfwdr push-images:sepp update-helm:sepp
	cd $(TOP)/esc && make build-sepp-model
	cd $(TOP); $(BOB) -r $(RULESET_SC) package-helm:sepp push-helm:sepp $(NODEBUG)

deploy:
	helm install $(SEPP_CHART_NAME)-$(USER) $(TOP)/.bob/$(SEPP_CHART_NAME)-$(shell cat $(TOP)/.bob/var.sepp-version).tgz \
	--namespace $(KUBE_NAMESPACE) \
	--set imageCredentials.setupmanager.registry.imagePullPolicy=Always,\
	imageCredentials.manager.registry.imagePullPolicy=Always,\
	imageCredentials.setupworker.registry.imagePullPolicy=Always,\
	imageCredentials.worker.registry.imagePullPolicy=Always,\
	imageCredentials.sds.registry.imagePullPolicy=Always,\
	imageCredentials.certnotifier.registry.imagePullPolicy=Always,\
	imageCredentials.logfdwr.registry.imagePullPolicy=Always,\
	imageCredentials.tapagent.registry.imagePullPolicy=Always,\
	imageCredentials.tlskeylogagent.registry.imagePullPolicy=Always,\
	imageCredentials.tapcollector.registry.imagePullPolicy=Always

upgrade-sepp: build-sepp
	helm upgrade $(SEPP_CHART_NAME)-$(USER) $(TOP)/.bob/$(SEPP_CHART_NAME)-$(shell cat $(TOP)/.bob/var.sepp-version).tgz \
	--namespace $(KUBE_NAMESPACE) \
	--set imageCredentials.setupmanager.registry.imagePullPolicy=Always,\
	imageCredentials.manager.registry.imagePullPolicy=Always,\
	imageCredentials.setupworker.registry.imagePullPolicy=Always,\
	imageCredentials.worker.registry.imagePullPolicy=Always,\
	imageCredentials.sds.registry.imagePullPolicy=Always,\
	imageCredentials.certnotifier.registry.imagePullPolicy=Always,\
	imageCredentials.logfdwr.registry.imagePullPolicy=Always,\
	imageCredentials.tapagent.registry.imagePullPolicy=Always,\
	imageCredentials.tlskeylogagent.registry.imagePullPolicy=Always,\
	imageCredentials.tapcollector.registry.imagePullPolicy=Always

deploy-prepare:
	kubectl --namespace $(KUBE_NAMESPACE) delete deployment eric-sepp-worker eric-sepp-manager
	kubectl --namespace $(KUBE_NAMESPACE) delete Service eric-sepp-manager eric-sepp-manager-grpc eric-sepp-worker eric-sepp-worker-2 eric-sepp-cert-notifier eric-sepp-sds eric-sepp-worker-n32c
	kubectl --namespace $(KUBE_NAMESPACE) delete serviceaccount eric-sepp-manager
	kubectl --namespace $(KUBE_NAMESPACE) delete role eric-sepp-manager-$(KUBE_NAMESPACE)
	kubectl --namespace $(KUBE_NAMESPACE) delete rolebinding eric-sepp-manager-$(KUBE_NAMESPACE)
	kubectl --namespace $(KUBE_NAMESPACE) delete ConfigMap eric-sepp-schema-configmap eric-sepp-loglevel-config eric-sepp-worker-loglevel-config
	kubectl --namespace $(KUBE_NAMESPACE) delete ExternalCertificate eric-sepp-nrf-certs-consumer-cr sc-traf-default-key1-sc-traf-default-cert1-external-certificate sc-traf-default-key2-sc-traf-default-cert2-external-certificate sc-traf-root-ca-list1-external-certificate sc-traf-root-ca-list2-external-certificate
	kubectl --namespace $(KUBE_NAMESPACE) delete InternalCertificate eric-sepp-dced-client-cert eric-sepp-manager-mediator-client-internal-certificate eric-sepp-manager-oam-server eric-sepp-worker-mediator-client-internal-certificate eric-sepp-worker-oam-server eric-sepp-manager-fhah-client-certificate eric-sepp-manager-oam-server eric-sepp-manager-rlf-client-internal-certificate eric-sepp-manager-worker-client-internal-certificate eric-sepp-manager-worker-server-internal-certificate eric-sepp-worker-manager-client-internal-certificate eric-sepp-worker-oam-server eric-sepp-worker-rlf-client-internal-certificate
	kubectl --namespace $(KUBE_NAMESPACE) delete ServiceAccount eric-sepp-worker
	kubectl --namespace $(KUBE_NAMESPACE) delete Role eric-sepp-worker
	kubectl --namespace $(KUBE_NAMESPACE) delete RoleBinding eric-sepp-worker
	kubectl --namespace $(KUBE_NAMESPACE) delete InternalUserCA eric-sepp-manager-ca eric-sepp-worker-ca

undeploy:
	helm --namespace $(KUBE_NAMESPACE) delete $(SEPP_CHART_NAME)-$(USER)
