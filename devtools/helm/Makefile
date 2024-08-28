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

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devtools/CONFIG
include $(TOP)/devenv/CONFIG

DOCKER_MNT := $$'-v ~/.kube:/root/.kube:Z -v $$(git rev-parse --show-toplevel):/build:Z'
HELM_CMD := $$'docker run --rm '$(DOCKER_MNT) $(HELM_IMAGE_NAME)$$' bash -c $$\'cd \'$${PWD\#$$(git rev-parse --show-toplevel)/}$$\'; helm $$@\' bash $$@'
HELM_FUNC_NAME := helm
HELM_FUNC := $$'function $(HELM_FUNC_NAME)() { '$(HELM_CMD)$$'; }'

.PHONY: all dev-env docker-build-images image package deploy clean clean-all clean-docker

all: install

all-cicd: all

clean-all: clean clean-docker

clean:
	rm -f *tar.gz*
	
binary:
	wget https://storage.googleapis.com/kubernetes-helm/helm-$(HELM_VERSION)-linux-amd64.tar.gz	
	
install: binary
	@sudo -p "Enter password for %u to install helm onto your machine: " tar -C /usr/local/bin -xzf helm-$(HELM_VERSION)-linux-amd64.tar.gz linux-amd64/helm --strip-components=1
	@echo "##############################################################################################################################"
	@echo "# If you are upgrading from the containerized helm, remember to remove the function helm() line from your .5g.devenv.profile #"
	@echo "##############################################################################################################################"

uninstall:
	@sudo -p "Enter password for %u to uninstall helm from your machine: " rm -rf /usr/local/bin/helm

clean-docker:
	docker ps -a -q --filter ancestor=$(HELM_IMAGE_NAME) | while read a; do docker stop $$a && docker rm $$a; done
	docker image ls $(HELM_IMAGE_NAME) -q | while read a; do docker image rm $$a; done
	docker ps -a -q --filter ancestor=secret-server | while read a; do docker stop $$a; done

image:
	@if [ $$(docker image ls $(HELM_IMAGE_NAME) | wc -l) -lt 2 ]; then										\
		echo 'Creating HELM image $(HELM_IMAGE_NAME)...';													\
		docker build -t secret-server ../secretsServer/;                                        \
		docker run -it --rm -d --name secrets-server --volume ~/.artifactory/helm_repositories.yaml:/files/helm_repositories.yaml secret-server;     \
		docker build -t $(HELM_IMAGE_NAME) --network=container:secrets-server .;																\
	else																									\
		echo 'HELM image $(HELM_IMAGE_NAME) found.';														\
	fi

profile:
	@echo 													>> $(PROFILE)
	@echo "# helm"						 					>> $(PROFILE)
	@echo alias h=\'helm --namespace $(KUBE_NAMESPACE)\'	>> $(PROFILE)

manifest:
	@[ $$(docker image ls $(HELM_IMAGE_NAME) | wc -l) -eq 2 ]
	@echo -e "$$(docker image inspect --format='{{.Id}}' $(HELM_IMAGE_NAME) | cut -f 2 -d :)\ti\t$(HELM_FUNC_NAME)\t$(HELM_IMAGE_NAME)" >> $(MANIFEST)

