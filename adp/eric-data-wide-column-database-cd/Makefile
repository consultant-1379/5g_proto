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
# Created on: Jun 14, 2018
#     Author: eedsvs
#

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devtools/CONFIG
include $(TOP)/devenv/CONFIG

ADP_NAME := eric-data-wide-column-database-cd
ADP_VER := 3.7.0-93
USERNAME := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ADP_REPO_BASE) -u)
PASSWORD := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ADP_REPO_BASE) -p)

.PHONY: all deploy undeploy clean clean-all

all: deploy

deploy:
	$(HELM) install $(ADP_NAME) --repo $(ADP_REPO_BASE) --version $(ADP_VER) --namespace $(KUBE_NAMESPACE) --name $(ADP_NAME)-$(KUBE_NAMESPACE) --username $(USERNAME) --password $(PASSWORD) --set resources.cassandra.requests.cpu=0.4 --set resources.cassandra.requests.memory=1024Mi

deploy-full:
	$(HELM) install $(ADP_NAME) --repo $(ADP_REPO_BASE) --version $(ADP_VER) --namespace $(KUBE_NAMESPACE) --name $(ADP_NAME)-$(KUBE_NAMESPACE) --username $(USERNAME) --password $(PASSWORD)
	
deploy-default:
	$(HELM) install $(ADP_NAME) --repo $(ADP_REPO_BASE) --version $(ADP_VER) --namespace default --name $(ADP_NAME)-default --username $(USERNAME) --password $(PASSWORD)
	
deploy-demo:
	$(HELM) install $(ADP_NAME) --repo $(ADP_REPO_BASE) --version $(ADP_VER) --namespace $(DEMO_NAMESPACE) --name $(ADP_NAME)-$(DEMO_NAMESPACE) --username $(USERNAME) --password $(PASSWORD)

deploy-minikube:
	$(HELM) install $(ADP_NAME) --repo $(ADP_REPO_BASE) --version $(ADP_VER) --namespace $(KUBE_NAMESPACE) --name $(ADP_NAME)-$(KUBE_NAMESPACE) --username $(USERNAME) --password $(PASSWORD) --set resources.cassandra.requests.cpu=0.2 --set resources.cassandra.requests.memory=1536Mi

undeploy:
	$(HELM) delete --purge $(ADP_NAME)-$(KUBE_NAMESPACE)

undeploy-default:
	$(HELM) delete --purge $(ADP_NAME)-default

undeploy-demo:
	$(HELM) delete --purge $(ADP_NAME)-$(DEMO_NAMESPACE)	

upgrade:
	$(HELM) upgrade $(ADP_NAME)-$(KUBE_NAMESPACE) $(ADP_REPO_BASE)/$(ADP_NAME)/$(ADP_NAME)-$(ADP_VER).tgz --namespace $(KUBE_NAMESPACE) --username $(USERNAME) --password $(PASSWORD)

upgrade-default:
	$(HELM) upgrade $(ADP_NAME)-default $(ADP_REPO_BASE)/$(ADP_NAME)/$(ADP_NAME)-$(ADP_VER).tgz --namespace default --username $(USERNAME) --password $(PASSWORD)

version:
	@echo "$(ADP_NAME): $(ADP_VER)" 

