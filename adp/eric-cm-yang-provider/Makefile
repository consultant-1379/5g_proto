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
# Created on: Feb 27, 2019
#     Author: eedbjhe
#

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devtools/CONFIG
include $(TOP)/devenv/CONFIG

ADP_NAME := eric-cm-yang-provider
ADP_VER := 2.0.0-4

USERNAME := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ADP_REPO_BASE) -u)
PASSWORD := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ADP_REPO_BASE) -p)

.PHONY: all deploy undeploy clean clean-all install-secret

all: deploy

install-secret:
	kubectl create secret generic cm-yang-users --namespace $(KUBE_NAMESPACE) --from-file=users.yaml 

install-secret-default:
	kubectl create secret generic cm-yang-users --namespace default --from-file=users.yaml

install-secret-demo:
	kubectl create secret generic cm-yang-users --namespace $(DEMO_NAMESPACE) --from-file=users.yaml
	
deploy:
	$(HELM) install $(ADP_NAME) --repo $(ADP_REPO_BASE) --version $(ADP_VER) --namespace $(KUBE_NAMESPACE) --name $(ADP_NAME)-$(KUBE_NAMESPACE) -f values.yaml --username $(USERNAME) --password $(PASSWORD)

deploy-default:
	$(HELM) install $(ADP_NAME) --repo $(ADP_REPO_BASE) --version $(ADP_VER) --namespace default --name $(ADP_NAME)-default --username $(USERNAME) --password $(PASSWORD)
	
deploy-demo:
	$(HELM) install $(ADP_NAME) --repo $(ADP_REPO_BASE) --version $(ADP_VER) --namespace $(DEMO_NAMESPACE) --name $(ADP_NAME)-$(DEMO_NAMESPACE) --username $(USERNAME) --password $(PASSWORD) --set externaldb.host=eric-data-document-database-pg,externaldb.user=bsf_user,externaldb.password=bsf_password,externaldb.dbname=bsf_database,CMM.host=eric-cm-mediator,userConfig.secretName=cm-yang-users,userConfig.secretKey=users.yaml,persistence.accessMode=ReadWriteOnce

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

