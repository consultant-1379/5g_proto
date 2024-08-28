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
# Created on: Jun 21, 2018
#     Author: xkorpap
#

ADP_NAME := eric-data-coordinator-zk
ADP_VER := 1.6.0+18

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devtools/CONFIG
include $(TOP)/devenv/CONFIG
USERNAME := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ADP_REPO_BASE) -u)
PASSWORD := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ADP_REPO_BASE) -p)

.PHONY: all deploy deploy-default undeploy undeploy-default upgrade upgrade-default version

all: deploy

deploy:
	$(HELM) install $(ADP_NAME) --repo $(ADP_REPO_BASE) --version $(ADP_VER) --namespace $(KUBE_NAMESPACE) --name $(ADP_NAME)-$(KUBE_NAMESPACE) --set persistantVolumeClaim.storageClassName=$(KUBE_STORAGE_CLASS) --username $(USERNAME) --password $(PASSWORD)
	
deploy-default:
	$(HELM) install $(ADP_NAME) --repo $(ADP_REPO_BASE) --version $(ADP_VER) --namespace default --name $(ADP_NAME)-default --set persistantVolumeClaim.storageClassName=$(KUBE_STORAGE_CLASS) --username $(USERNAME) --password $(PASSWORD)

deploy-demo:
	$(HELM) install $(ADP_NAME) --repo $(ADP_REPO_BASE) --version $(ADP_VER) --namespace $(DEMO_NAMESPACE) --name $(ADP_NAME)-$(DEMO_NAMESPACE) --set persistantVolumeClaim.storageClassName=$(KUBE_STORAGE_CLASS),persistantVolumeClaim.storage=2Gi --username $(USERNAME) --password $(PASSWORD)
	 
undeploy:
	$(HELM) delete --purge $(ADP_NAME)-$(KUBE_NAMESPACE)
	$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc datadir-$(ADP_NAME)-0
	$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc datadir-$(ADP_NAME)-1
	$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc datadir-$(ADP_NAME)-2	
	
undeploy-default:
	$(HELM) delete --purge $(ADP_NAME)-default
	$(KUBECTL) -n default delete pvc datadir-$(ADP_NAME)-0
	$(KUBECTL) -n default delete pvc datadir-$(ADP_NAME)-1
	$(KUBECTL) -n default delete pvc datadir-$(ADP_NAME)-2

undeploy-demo:
	$(HELM) delete --purge $(ADP_NAME)-$(DEMO_NAMESPACE)
	$(KUBECTL) -n $(DEMO_NAMESPACE) delete pvc datadir-$(ADP_NAME)-0
	$(KUBECTL) -n $(DEMO_NAMESPACE) delete pvc datadir-$(ADP_NAME)-1
	
upgrade:
	$(HELM) upgrade $(ADP_NAME)-$(KUBE_NAMESPACE) $(ADP_REPO_BASE)/$(ADP_NAME)/$(ADP_NAME)-$(ADP_VER).tgz --namespace $(KUBE_NAMESPACE) --username $(USERNAME) --password $(PASSWORD)

upgrade-default:
	$(HELM) upgrade $(ADP_NAME)-default $(ADP_REPO_BASE)/$(ADP_NAME)/$(ADP_NAME)-$(ADP_VER).tgz --namespace default	--username $(USERNAME) --password $(PASSWORD)

version:
	@echo "$(ADP_NAME): $(ADP_VER)" 

