# vim:ts=4:sw=4:sw=4:et
#
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
# Created on: Nov 13, 2018
#     Author: xkorpap
#

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devtools/CONFIG
include $(TOP)/devenv/CONFIG

ADP_NAME := eric-pm-server
ADP_VER := 1.3.0-27
ADP_PACKAGE := $(ADP_NAME)-$(ADP_VER)
ADP_PACKAGE_TGZ := $(ADP_PACKAGE).tgz
ADP_ARTIFACTORY_SRV := arm.rnd.ki.sw.ericsson.se
ADP_ARTIFACTORY_BASE := artifactory
ADP_PROJ_BASE := proj-adp-pm-server-helm

REPO := https://$(ADP_ARTIFACTORY_SRV)/$(ADP_ARTIFACTORY_BASE)/$(ADP_PROJ_BASE)
USERNAME := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ADP_ARTIFACTORY_SRV) -u)
PASSWORD := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ADP_ARTIFACTORY_SRV) -p)

JSON_PATH := '{.items[*].status.addresses[?(@.type=="InternalIP")].address}'
TARGET_CMD := `kubectl get nodes -o jsonpath=$(JSON_PATH)`
TARGETS := $(shell echo "$(TARGET_CMD)" | sed -r 's/(\w+\.\w+\.\w+\.\w+)/- "\1:10255"/g' | sed 's/\([^-]\) /\1\#\#\#NEWLINE\#\#\#            /g')

.PHONY: all deploy undeploy clean clean-all

all: deploy

prometheus.yaml:
	@cat prometheus.yaml.template																				   |\
	sed "s/\#\#\#NAMESPACE\#\#\#/$$(echo '$(KUBE_NAMESPACE)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"			   \
	> prometheus.yaml && echo "prometheus.yaml created."

prometheus-demo.yaml:
	@cat prometheus-demo.yaml.template																				   |\
	sed "s/\#\#\#NAMESPACE\#\#\#/$$(echo '$(DEMO_NAMESPACE)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"			   |\
	sed "s/\#\#\#TARGETS\#\#\#/$$( echo '$(TARGETS)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"			   |\
	sed "s/\#\#\#NEWLINE\#\#\#/\n/g" \
	> prometheus.yaml && echo "prometheus.yaml created for demo."

deploy: clean prometheus.yaml
	$(HELM) install $(ADP_NAME) -f prometheus.yaml --repo $(REPO) --version $(ADP_VER) --namespace $(KUBE_NAMESPACE) --name $(ADP_NAME)-$(KUBE_NAMESPACE) --set server.replicaCount=2 --set server.persistentVolume.enabled=false --set rbac.appMonitoring.enabled=true --set rbac.appMonitoring.configFileCreate=false --set server.service.type=NodePort --set log.level=debug --username $(USERNAME) --password $(PASSWORD)
	#$(HELM) install $(ADP_NAME) --repo $(REPO) --version $(ADP_VER) --namespace $(KUBE_NAMESPACE) --name $(ADP_NAME)-$(KUBE_NAMESPACE) --set server.replicaCount=2 --set server.persistentVolume.enabled=false --set rbac.appMonitoring.enabled=true --set rbac.appMonitoring.configFileCreate=true --set server.service.type=NodePort --set log.level=debug --set server.serviceAccountName="eric-pm-server"
	# 

deploy-default: 
	$(HELM) install $(ADP_NAME) --repo $(REPO) --version $(ADP_VER) --namespace default --name $(ADP_NAME)-default --set server.replicaCount=2 --set server.persistentVolume.enabled=true --set rbac.appMonitoring.enabled=true --set rbac.appMonitoring.configFileCreate=true --username $(USERNAME) --password $(PASSWORD)
	
deploy-demo: clean prometheus-demo.yaml
	$(HELM) install $(ADP_NAME) -f prometheus.yaml --repo $(REPO) --version $(ADP_VER) --namespace $(DEMO_NAMESPACE) --name $(ADP_NAME)-$(DEMO_NAMESPACE) --set server.replicaCount=2 --set server.persistentVolume.enabled=false --set rbac.appMonitoring.enabled=true --set rbac.appMonitoring.configFileCreate=false --set server.service.type=NodePort --username $(USERNAME) --password $(PASSWORD)

undeploy:
	$(HELM) delete --purge $(ADP_NAME)-$(KUBE_NAMESPACE)
	
undeploy-default:
	$(HELM) delete --purge $(ADP_NAME)-default

undeploy-demo:
	$(HELM) delete --purge $(ADP_NAME)-$(DEMO_NAMESPACE)

$(ADP_PACKAGE_TGZ):
	wget $(REPO)/$(ADP_PACKAGE_TGZ) --user $(USERNAME) --header="X-Auth-Token: $(PASSWORD)" 

clean:
	rm prometheus.yaml

clean-all: undeploy clean

version:
	@echo "$(ADP_NAME): $(ADP_VER)" 

