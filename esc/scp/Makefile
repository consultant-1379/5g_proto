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
SCP_CHART_NAME = eric-scp

include $(TOP)/Makefile.config

.PHONY: build deploy deploy-prepare undeploy upgrade

build:
	cd $(TOP); mvn package -DskipTests -Djacoco.skip=true -am -pl :eric-scp-manager,:eric-scp-sds,:eric-sc-cert-notifier,:eric-sc-tlskeylogagent,:eric-sc-tapcollector,:vtaprecorder
	cd $(TOP); $(BOB) -r $(RULESET_SC) init build:logfwdr build-images:sds build-images:certnotifier build-images:scp push-images:sds push-images:certnotifier push-images:logfwdr push-images:scp update-helm:scp $(NODEBUG)
	cd $(TOP)/esc && make build-scp-model
	cd $(TOP); $(BOB) -r $(RULESET_SC) package-helm:scp push-helm:scp

#TODO: Check why overriding global imagePullPolicy variable is not working
deploy: build
	helm install $(SCP_CHART_NAME)-$(USER) $(TOP)/.bob/$(SCP_CHART_NAME)-$(shell cat $(TOP)/.bob/var.scp-version).tgz \
	--namespace 5g-bsf-$(USER) \
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
	kubectl --ignore-not-found=true --namespace $(KUBE_NAMESPACE) delete deployment eric-scp-worker eric-scp-manager
	kubectl --ignore-not-found=true --namespace $(KUBE_NAMESPACE) delete configmap eric-scp-schema-configmap eric-scp-loglevel-config eric-scp-worker-loglevel-config
	kubectl --ignore-not-found=true --namespace $(KUBE_NAMESPACE) delete svc eric-scp-manager-grpc eric-scp-worker eric-scp-cert-notifier eric-scp-sds eric-scp-manager
	kubectl --ignore-not-found=true --namespace $(KUBE_NAMESPACE) delete serviceaccount eric-scp-manager eric-scp-worker	
	kubectl --ignore-not-found=true --namespace $(KUBE_NAMESPACE) delete role eric-scp-manager-$(KUBE_NAMESPACE)
	kubectl --ignore-not-found=true --namespace $(KUBE_NAMESPACE) delete rolebinding eric-scp-manager-$(KUBE_NAMESPACE)
	kubectl --ignore-not-found=true --namespace $(KUBE_NAMESPACE) delete ExternalCertificate eric-scp-nrf-certs-consumer-cr eric-scp-traf-certs-consumer-cr
	kubectl --ignore-not-found=true --namespace $(KUBE_NAMESPACE) delete InternalCertificate eric-scp-manager-mediator-client-internal-certificate eric-scp-manager-oam-server eric-scp-worker-mediator-client-internal-certificate eric-scp-worker-oam-server eric-scp-manager-yang-internal-certificate eric-scp-dced-client-cert eric-scp-manager-rlf-client-internal-certificate eric-scp-worker-rlf-client-internal-certificate eric-scp-manager-fhah-client-certificate eric-scp-worker-slf-client-internal-certificate
	kubectl --ignore-not-found=true --namespace $(KUBE_NAMESPACE) delete Role eric-scp-worker
	kubectl --ignore-not-found=true --namespace $(KUBE_NAMESPACE) delete RoleBinding eric-scp-worker
	kubectl --ignore-not-found=true --namespace $(KUBE_NAMESPACE) delete ConfigMap eric-scp-manager-loglevel-config

undeploy:
	helm delete $(SCP_CHART_NAME)-$(USER) --namespace $(KUBE_NAMESPACE)

upgrade: build
	helm upgrade $(SCP_CHART_NAME)-$(USER) $(TOP)/.bob/$(SCP_CHART_NAME)-$(shell cat $(TOP)/.bob/var.scp-version).tgz \
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
	