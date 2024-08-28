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
# Created on: Jun 03, 2018
#     Author: eedsvs
#

TOP := ..
include $(TOP)/CONFIG
include CONFIG
include $(TOP)/devtools/CONFIG

PASSWORD := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ARM_DOCKER_REG_URL) -p)


.PHONY: all clean clean-all

all: namespace secret


clean:
	rm -f original-secret.json
	rm -f new-namespace.json
	rm -f rbd-client-secret.json

namespace:
	$(KUBECTL) get namespace $(KUBE_NAMESPACE) &> /dev/null || $(KUBECTL) create namespace $(KUBE_NAMESPACE)

secret:
	@if [ $$($(KUBECTL) -n $(KUBE_NAMESPACE) get secret rbd-user-secret | wc -l) -lt 2 ]; then	\
		$(KUBECTL) get secret rbd-user-secret -o json > original-secret.json; 			\
		echo 'Creating secret "rbd-user-secret" for namespace $(KUBE_NAMESPACE)...';		\
		$(JQ) '.metadata.namespace="$(KUBE_NAMESPACE)"' original-secret.json > new-namespace.json;	\
		rm -f original-secret.json; 											\
		$(JQ) 'del(.metadata.uid, .metadata.creationTimestamp, .metadata.resourceVersion, .metadata.selfLink)' new-namespace.json > rbd-user-secret.json;	\
		rm -f new-namespace.json;\
		$(KUBECTL) create -f rbd-user-secret.json;												\
		rm -f rbd-user-secret.json;\
	else																					\
		echo 'rbd-user-secret already exists';											\
	fi


kubeconf:
	mkdir -p ~/.kube
	install --mode=600 $(KUBECONFIG) ~/.kube/config

reg-docker-repo:
	curl -X PUT -u$(ARM_DOCKER_REG_USRSIG) "https://$(ARM_DOCKER_REG_URL)/artifactory/api/plugins/execute/createRepo?params=projKey=$(ARM_DOCKER_REG_PROJKEY);type=docker-global;ownerId=$(ARM_DOCKER_REG_MGRSID)"

reg-helm-repo:
	curl -X PUT -u$(ARM_DOCKER_REG_USRSIG) "https://$(ARM_DOCKER_REG_URL)/artifactory/api/plugins/execute/createRepo?params=projKey=$(ARM_DOCKER_REG_PROJKEY);type=helm;ownerId=$(ARM_DOCKER_REG_MGRSID)"
	$(HELM) repo add proj-$(ARM_DOCKER_REG_PROJKEY)-helm https://$(ARM_DOCKER_REG_URL)/artifactory/proj-$(ARM_DOCKER_REG_PROJKEY)-helm --username $(DEV_USER) --password $(PASSWORD)

reg-generic-repo:
	curl -X PUT -u$(ARM_DOCKER_REG_USRSIG) "https://$(ARM_DOCKER_REG_URL)/artifactory/api/plugins/execute/createRepo?params=projKey=$(ARM_DOCKER_REG_PROJKEY);type=generic;ownerId=$(ARM_DOCKER_REG_MGRSID)"

