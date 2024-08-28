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
# Created on: Mar 22, 2019
#     Author: eedbjhe
#
# This makefile is for deploying the SC with all ADP components
#

include Makefile.user

TOP := ..
BOB := bob/bob
CHART_NAME = eric-sc
BSF_CHART_NAME = eric-bsf
NLF_CHART_NAME = eric-nlf
SCP_CHART_NAME = eric-scp
SEPP_CHART_NAME = eric-sepp
RLF_CHART_NAME = eric-sc-rlf
SLF_CHART_NAME = eric-sc-slf
CHFSIM_CHART_NAME = eric-chfsim
NRFSIM_CHART_NAME = eric-nrfsim
SEPPSIM_CHART_NAME = eric-seppsim
RULESET := ruleset2.0-eric-esc.yaml
CRD_RULESET := rulesets/ruleset2.0-eric-crd.yaml
ENVOY_RULESET := rulesets/ruleset2.0-eric-envoy.yaml
LOADGEN_RULESET := rulesets/ruleset2.0-eric-loadgen.yaml
CHFSIM_RULESET := rulesets/ruleset2.0-eric-chfsim.yaml
NRFSIM_RULESET := rulesets/ruleset2.0-eric-nrfsim.yaml
SEPPSIM_RULESET := rulesets/ruleset2.0-eric-seppsim.yaml
TOOLS_RULESET := rulesets/ruleset2.0-eric-tools.yaml
MODEL_RULESET := rulesets/ruleset2.0-yang.yaml
SFTP_RULESET := rulesets/ruleset2.0-eric-sftp.yaml
VTAPRECORDER_RULESET := rulesets/ruleset2.0-eric-vtaprecorder.yaml
CSAR_RULESET := csar/ruleset2.0.yaml
VALUES_YAML := values-final.yaml
OUTPUT_DIR := $(TOP)/.bob
ESC_VERSION = $(OUTPUT_DIR)/var.esc-version
#MAKE_VERSION = $(OUTPUT_DIR)/var.make-version
CERTS_OUTDIR := .certificates
USAGE_HISTORY_FILE = /proj/sc-tools/tmp/usage-history
USAGE_HISTORY_MAX_SIZE = 52428800

# usage history
usage-history:
	@if [ $$(stat -c %s $(USAGE_HISTORY_FILE)) -gt $(USAGE_HISTORY_MAX_SIZE) ]; then \
		cp "$(USAGE_HISTORY_FILE)" "$(USAGE_HISTORY_FILE)-$$(date +%Y%m%d%H%M%S)"; \
		chmod 7777 -fR "$(USAGE_HISTORY_FILE)-$$(date +%Y%m%d%H%M%S)" \
		echo "File created $$(date)" > $(USAGE_HISTORY_FILE); \
	fi
ifeq ($(CNCS),true)
	echo "NEW -> $$(date)" >> $(USAGE_HISTORY_FILE)
else
	echo "LEGACY -> $$(date)" >> $(USAGE_HISTORY_FILE)
endif

# print target list
.PHONY: help
help:
	cat Makefile | grep -E '^deploy-|^undeploy|^upgrade-' | sort -t: -u -k1,1 | awk '{ print substr($$1, 1, length($$1)-1) }'


OBJECTS := $(OUTPUT_DIR)/$(BSF_CHART_NAME)-diameter-*.tgz $(OUTPUT_DIR)/$(CHART_NAME)-monitor-*.tgz $(OUTPUT_DIR)/$(CHART_NAME)-rlf-*.tgz $(OUTPUT_DIR)/$(CHART_NAME)-slf-*.tgz $(OUTPUT_DIR)/$(CHART_NAME)-cert-notifier-*.tgz $(OUTPUT_DIR)/$(BSF_CHART_NAME)-*.tgz $(OUTPUT_DIR)/$(SCP_CHART_NAME)-*.tgz $(OUTPUT_DIR)/$(SEPP_CHART_NAME)-*.tgz

rwildcard=$(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))

DEBUG ?= 1
ifeq ($(DEBUG), 0)
	export BUILD_LOG_LEVEL=warn
	export MVN_ARGS=-B -q -Dmaven.test.redirectTestOutputToFile=true
	export DEBUG_FLAG=1>/dev/null
	export DEBUG_CURL_FLAG1=-s
	export DEBUG_CURL_FLAG2=-S
	NODEBUG= -q
else
	export MVN_ARGS=
	export DEBUG_FLAG=
	export DEBUG_CURL_FLAG1=
	export DEBUG_CURL_FLAG2=
endif

include $(TOP)/CONFIG
include $(TOP)/devenv/CONFIG
include $(TOP)/devtools/CONFIG

#Used for tool-package
TIMESTAMP := $(shell date "+%Y%m%d%H%M%S")

.PHONY: init build esc-parent libs
all: clean libs deploy-sims deploy
all-full: clean libs deploy-sims deploy-full
undeploy-all: undeploy undeploy-sims
undeploy-all-full: undeploy-full undeploy-sims


############
### Info ###
############

deprecation-info:
	@echo "#############################################################################################################"
	@echo "# You are using a target or build/deployment-method that will be deprecated soon due to the switch to CNCS. #"
	@echo "# Please update your Makefile.user to start using the CNCS deployment method.                               #"
	@echo "#                                                                                                           #"
	@echo "# The quickest way to start is to set                                                                       #"
	@echo "# export CNCS=true                                                                                          #"
	@echo "# in your Makefile.user                                                                                     #"
	@echo "#                                                                                                           #"
	@echo "# Other/New/updated parameters are described in the Makefile.user.bck file.                                 #"
	@echo "#############################################################################################################"
	@echo
	@read -p "Press enter to continue"

################################################
### Builds ADPal, Cnal, Common and Utilities ###
################################################

build: init esc-parent
ifeq ($(SCP),true)
	cd $(TOP); $(BOB) -r $(RULESET) build:logfwdr $(NODEBUG)
endif
ifeq ($(SEPP),true)
	cd $(TOP); $(BOB) -r $(RULESET) build:logfwdr $(NODEBUG)
endif

esc-parent:
	cd $(TOP); $(BOB) -r $(RULESET) build:esc-parent $(NODEBUG)
	
logfwdr:
	cd $(TOP); $(BOB) -r $(RULESET) build:logfwdr $(NODEBUG)
	
libs: init
	cd $(TOP); $(BOB) -r $(RULESET) libs:build build:copy-ericsson-libs-to-user-mvn-repo $(NODEBUG)

###################################################################
### Upload ADPal, Cnal, Common and Utilities to test maven repo ###
###################################################################

maven-test-repo:
	cd $(TOP); $(BOB) -r $(RULESET)  push-libs-maven:all

maven-test-repo-adpal:
	cd $(TOP); $(BOB) -r $(RULESET)  push-libs-maven:adpal

maven-test-repo-cnal:
	cd $(TOP); $(BOB) -r $(RULESET)  push-libs-maven:cnal

maven-test-repo-common:
	cd $(TOP); $(BOB) -r $(RULESET)  push-libs-maven:common

maven-test-repo-utilities:
	cd $(TOP); $(BOB) -r $(RULESET)  push-libs-maven:utilities

maven-test-repo-esc-parent:
	cd $(TOP); $(BOB) -r $(RULESET)  push-libs-maven:esc-parent

maven-test-repo-eric-chfsim:
	cd $(TOP); $(BOB) -r $(RULESET)  push-libs-maven:eric-chfsim

maven-test-repo-eric-nrfsim:
	cd $(TOP); $(BOB) -r $(RULESET)  push-libs-maven:eric-nrfsim

maven-test-repo-eric-seppsim:
	cd $(TOP); $(BOB) -r $(RULESET)  push-libs-maven:eric-seppsim

####################################################
### Creates and deploys secrets for ADP services ###
####################################################

secrets: 
	cd $(TOP); $(BOB) -r $(RULESET) deploy:adp-secrets
		
####################################################
### Creates and deploys crds for ADP services ###
####################################################

deploy-crds:
	cd $(TOP); $(BOB) -r $(CRD_RULESET) init prepare fetch deploy

undeploy-crds:
	cd $(TOP); $(BOB) -r $(CRD_RULESET) init undeploy

##############################
### Deploys the simulators ###
##############################

deploy-loadgen:
	make -C ../simulators/loadgen deploy

deploy-chfsim:
	make -C ../simulators/chfsim deploy

deploy-chfsim-full:
	make -C ../simulators/chfsim deploy-full

deploy-nrfsim:
	make -C ../simulators/nrfsim deploy

deploy-nrfsim-full:
	make -C ../simulators/nrfsim deploy-full

deploy-seppsim:
	make -C ../simulators/seppsim deploy

deploy-sims: deploy-chfsim deploy-nrfsim deploy-seppsim

undeploy-loadgen:
	make -C ../simulators/loadgen undeploy

undeploy-chfsim:
	make -C ../simulators/chfsim undeploy

undeploy-nrfsim:
	make -C ../simulators/nrfsim undeploy

undeploy-seppsim:
	make -C ../simulators/seppsim undeploy

undeploy-sims: undeploy-chfsim undeploy-nrfsim undeploy-seppsim

######################
### Helper targets ###
######################

init:
	cd $(TOP); $(BOB) -r $(RULESET) init $(NODEBUG)

init-tools:
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) init $(NODEBUG)


############################################	
### build targets for all deploy targets ###
############################################

clean-umbrella: init
	cd $(TOP); $(BOB) -r $(RULESET) update-umbrella:clean $(NODEBUG)
	

#########################################	
### Undeploy all the new make targets ###
#########################################

undeploy-all-targets: undeploy remove-volumes 	 
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete job  -l 'app.kubernetes.io/name=eric-cm-mediator-key-init'


undeploy:
ifeq ($(CNCS),true)
ifeq ($(BSF),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.undeploy.bsf
endif
ifeq ($(SCP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.undeploy.scp
endif
ifeq ($(SEPP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.undeploy.sepp
endif
ifeq ($(DIAMETER),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.undeploy.sc-dsc
endif
	cd $(TOP); $(BOB) -r $(RULESET) sc.undeploy.sc-cs
	cd $(TOP); $(BOB) -r $(RULESET) sc.undeploy.cncs-a
	cd $(TOP); $(BOB) -r $(RULESET) sc.undeploy.cncs-b
else
	cd $(TOP); $(BOB) -r $(RULESET) undeploy:all
endif

wreck:
ifeq ($(CNCS),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.clean:delete-namespace sc.clean:create-namespace
	cd $(TOP); $(BOB) -r $(RULESET) sc.clean:cncs-cluster-resources
	cd $(TOP); $(BOB) -r $(RULESET) sc.clean:sc-cs-cluster-resources
ifeq ($(BSF),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.clean:bsf-cluster-resources
endif
ifeq ($(SCP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.clean:scp-cluster-resources
endif
ifeq ($(SEPP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.clean:sepp-cluster-resources
endif
ifeq ($(DIAMETER),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.clean:dsc-cluster-resources
endif
else
	cd $(TOP); $(BOB) -r $(RULESET) clean-resources:delete-namespace clean-resources:create-namespace clean-resources:remove-cluster-resources
endif

#################
# Sonar Scanner #
#################

sonar: init
	cd $(TOP); $(BOB) -r $(RULESET) sonar 
	@echo "#################################################################################################################"
	@echo "# You run the Sonar Scanner.                                       				               #" 
	@echo "# To see the results of your scan, please refer to the corresponding html page provided at the end of the test. #" 
	@echo "#################################################################################################################"

	
undeploy-full: undeploy



############################
### Deploys only the BSF ###
############################

build-bsf:
	cd $(TOP); mvn package -DskipTests -Djacoco.skip=true -am -pl :eric-bsf-common,:eric-bsf-worker,:eric-bsf-manager,:eric-bsf-diameter,:eric-bsf-worker-cddjmxexporter,:eric-sc-cert-notifier,:eric-sc-tlskeylogagent,:eric-sc-tapcollector,:vtaprecorder
	cd $(TOP); $(BOB) -r $(RULESET) init build-images:certnotifier build-images:bsf build-images:bsf-diameter push-images:certnotifier push-images:bsf push-images:bsf-diameter update-helm:bsf update-helm:bsf-diameter $(NODEBUG)
	@make build-bsf-model
	cd $(TOP); $(BOB) -r $(RULESET) package-helm:bsf package-helm:bsf-diameter push-helm:bsf push-helm:bsf-diameter $(NODEBUG)

deploy-bsf-prepare:
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete deployment eric-bsf-diameter eric-bsf-manager eric-bsf-worker
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete Service eric-bsf-cert-notifier eric-bsf-diameter eric-bsf-diameter-cdd eric-bsf-manager eric-bsf-worker eric-bsf-worker-cdd
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete PodDisruptionBudget eric-bsf-worker
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete ServiceAccount eric-bsf-manager
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete ConfigMap eric-bsf-schema-configmap eric-bsf-worker-jmx-config
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete Role eric-bsf-manager-5g-bsf-eedbjhe
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete RoleBinding eric-bsf-manager-5g-bsf-eedbjhe
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete ExternalCertificate eric-bsf-nrf-certs-consumer-cr eric-bsf-traf-certs-consumer-cr
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete InternalCertificate eric-bsf-manager-mediator-client-internal-certificate eric-bsf-manager-oam-server-internal-certificate eric-bsf-worker-mediator-client-internal-certificate eric-bsf-worker-oam-server-internal-certificate eric-bsf-dced-client-cert eric-bsf-wcdb-client-cert eric-bsf-manager-server-certificate

#TODO: Check why overriding global imagePullPolicy variable is not working
deploy-bsf: build-bsf
	$(HELM) install $(BSF_CHART_NAME)-$(USER) $(TOP)/.bob/$(BSF_CHART_NAME)-$(shell cat $(TOP)/.bob/var.bsf-version).tgz \
	--namespace $(KUBE_NAMESPACE) \
	--set imageCredentials.setupmanager.registry.imagePullPolicy=Always,\
	imageCredentials.manager.registry.imagePullPolicy=Always,\
	imageCredentials.worker.registry.imagePullPolicy=Always,\
	imageCredentials.cddjmxexporter.registry.imagePullPolicy=Always,\
	imageCredentials.certnotifier.registry.imagePullPolicy=Always,\
	imageCredentials.tapagent.registry.imagePullPolicy=Always,\
	imageCredentials.tlskeylogagent.registry.imagePullPolicy=Always,\
        imageCredentials.tapcollector.registry.imagePullPolicy=Always

#TODO: Check why overriding global imagePullPolicy variable is not working
upgrade-bsf: build-bsf
	$(HELM) upgrade $(BSF_CHART_NAME)-$(USER) $(TOP)/.bob/$(BSF_CHART_NAME)-$(shell cat $(TOP)/.bob/var.bsf-version).tgz \
	--namespace $(KUBE_NAMESPACE) \
	--set imageCredentials.setupmanager.registry.imagePullPolicy=Always,\
	imageCredentials.manager.registry.imagePullPolicy=Always,\
	imageCredentials.worker.registry.imagePullPolicy=Always,\
	imageCredentials.cddjmxexporter.registry.imagePullPolicy=Always,\
	imageCredentials.certnotifier.registry.imagePullPolicy=Always

undeploy-bsf:
	@$(HELM) delete $(BSF_CHART_NAME)-$(USER) --namespace $(KUBE_NAMESPACE) 


############################
### Deploys only the SCP ###
############################

deploy-scp:
	cd $(TOP)/esc/scp && make deploy

deploy-scp-prepare:
	cd $(TOP)/esc/scp && make deploy-prepare

upgrade-scp:
	cd $(TOP)/esc/scp && make upgrade-scp

undeploy-scp:
	cd $(TOP)/esc/scp && make undeploy


#############################
### Deploys only the SEPP ###
#############################

deploy-sepp:
	cd $(TOP)/esc/sepp && make deploy

deploy-sepp-prepare:
	cd $(TOP)/esc/sepp && make deploy-prepare

upgrade-sepp:
	cd $(TOP)/esc/sepp && make upgrade-scp

undeploy-sepp:
	cd $(TOP)/esc/sepp && make undeploy


############################
### Deploys only the RLF ###
############################

build-rlf: init
	cd $(TOP); mvn package -DskipTests -Djacoco.skip=true -am -pl :eric-sc-rlf
	cd $(TOP); $(BOB) -r $(RULESET) build-images:rlf push-images:rlf update-helm:rlf lint:rlf package-helm:rlf push-helm:rlf $(NODEBUG)

deploy-rlf-prepare:
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete deployments/eric-sc-rlf
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete PodDisruptionBudget eric-sc-rlf
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete Service eric-sc-rlf
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete ServiceAccount eric-sc-rlf
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete Role eric-sc-rlf-5g-bsf-eedbjhe
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete RoleBinding eric-sc-rlf-5g-bsf-eedbjhe
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete InternalCertificate eric-sc-rlf-server-certificate eric-sc-rlf-mediator-client-internal-certificate eric-sc-rlf-mediator-server-internal-certificate eric-sc-rlf-dced-client-cert
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete InternalUserCA eric-sc-rlf-ca
	$(KUBECTL) --namespace $(KUBE_NAMESPACE) delete ConfigMap eric-sc-rlf-loglevel-config

deploy-rlf: build-rlf
	$(HELM) install $(RLF_CHART_NAME)-$(USER) $(TOP)/.bob/eric-sc-rlf-*.tgz --namespace $(KUBE_NAMESPACE) --set imageCredentials.rlf.registry.imagePullPolicy=Always

upgrade-rlf: build-rlf
	$(HELM) --namespace $(KUBE_NAMESPACE) upgrade $(RLF_CHART_NAME)-$(USER) $(TOP)/.bob/$(RLF_CHART_NAME)*.tgz --set imageCredentials.rlf.registry.imagePullPolicy=Always

undeploy-rlf:
	$(HELM) delete $(RLF_CHART_NAME)-$(USER) --namespace $(KUBE_NAMESPACE)


############################
### Deploys only the NLF ###
############################

build-nlf:
	cd $(TOP); mvn package -DskipTests -Djacoco.skip=true -am -pl :eric-sc-nlf
	cd $(TOP); $(BOB) -r $(RULESET) build-images:nlf push-images:nlf update-helm:nlf lint:nlf package-helm:nlf push-helm:nlf $(NODEBUG)

deploy-nlf: build-nlf
	$(HELM) install $(NLF_CHART_NAME)-$(USER) $(TOP)/.bob/eric-sc-nlf-*.tgz --namespace $(KUBE_NAMESPACE) --set imageCredentials.nlf.registry.imagePullPolicy=Always

upgrade-nlf: build-nlf
	$(HELM) --namespace $(KUBE_NAMESPACE) upgrade $(NLF_CHART_NAME)-$(USER) $(TOP)/.bob/$(NLF_CHART_NAME)*.tgz --namespace $(KUBE_NAMESPACE) --set imageCredentials.nlf.registry.imagePullPolicy=Always

undeploy-nlf:
	@$(HELM) delete $(NLF_CHART_NAME)-$(USER) --namespace $(KUBE_NAMESPACE) 

deploy-nlf-prepare:
	kubectl --namespace $(KUBE_NAMESPACE) --ignore-not-found=true delete deployment eric-sc-nlf
	kubectl --namespace $(KUBE_NAMESPACE) --ignore-not-found=true delete ConfigMap "eric-sc-nlf-loglevel-config"
	kubectl --namespace $(KUBE_NAMESPACE)  --ignore-not-found=true delete Role "eric-sc-nlf-5g-bsf-$(USER)"
	kubectl --namespace $(KUBE_NAMESPACE)  --ignore-not-found=true delete RoleBinding "eric-sc-nlf-5g-bsf-$(USER)"
	kubectl --namespace $(KUBE_NAMESPACE)  --ignore-not-found=true delete InternalCertificate "eric-sc-nlf-dced-client-cert"
	kubectl --namespace $(KUBE_NAMESPACE)  --ignore-not-found=true delete InternalCertificate "eric-sc-nlf-mediator-client-internal-certificate"
	kubectl --namespace $(KUBE_NAMESPACE)  --ignore-not-found=true delete InternalCertificate "eric-sc-nlf-mediator-server-internal-certificate"
	kubectl --namespace $(KUBE_NAMESPACE)  --ignore-not-found=true delete InternalCertificate "eric-sc-nlf-server-certificate"
	kubectl --namespace $(KUBE_NAMESPACE)  --ignore-not-found=true delete InternalCertificate "eric-sc-nlf-manager-fhah-client-certificate"
	kubectl --namespace $(KUBE_NAMESPACE)  --ignore-not-found=true delete InternalCertificate eric-scp-manager-nlf-client-internal-certificate eric-sepp-manager-nlf-client-internal-certificate
	kubectl --namespace $(KUBE_NAMESPACE)  --ignore-not-found=true delete InternalUserCA "eric-sc-nlf-ca"
	kubectl --namespace $(KUBE_NAMESPACE)  --ignore-not-found=true delete PodDisruptionBudget "eric-sc-nlf"
	kubectl --namespace $(KUBE_NAMESPACE)  --ignore-not-found=true delete ServiceAccount "eric-sc-nlf"
	kubectl --namespace $(KUBE_NAMESPACE)  --ignore-not-found=true delete Service "eric-sc-nlf"
	


############################
### Deploys only the SLF ###
############################

build-slf:
	cd $(TOP); mvn package -DskipTests -Djacoco.skip=true -am -pl :eric-sc-slf
	cd $(TOP); $(BOB) -r $(RULESET) build-images:slf push-images:slf update-helm:slf lint:slf package-helm:slf push-helm:slf $(NODEBUG)

deploy-slf: build-slf
	$(HELM) install $(SLF_CHART_NAME)-$(USER) $(TOP)/.bob/eric-sc-slf-*.tgz --namespace $(KUBE_NAMESPACE) --set imageCredentials.slf.registry.imagePullPolicy=Always

upgrade-slf: build-slf
	$(HELM) --namespace $(KUBE_NAMESPACE) upgrade $(SLF_CHART_NAME)-$(USER) $(TOP)/.bob/$(SLF_CHART_NAME)*.tgz --namespace $(KUBE_NAMESPACE) --set imageCredentials.slf.registry.imagePullPolicy=Always

undeploy-slf:
	@$(HELM) delete $(SLF_CHART_NAME)-$(USER) --namespace $(KUBE_NAMESPACE) 


################################
### Deploys only the MONITOR ###
################################

deploy-monitor:
	@make -C ../esc/monitor deploy

upgrade-monitor:
	@make -C ../esc/monitor upgrade

replace-monitor:
	@make -C ../esc/monitor replace

undeploy-monitor:
	@make -C ../esc/monitor undeploy

clean-monitor:
	@make -C ../esc/monitor clean


#########
# Envoy #
#########

build-envoy-base-image: 
	cd $(TOP); $(BOB) -r $(ENVOY_RULESET) envoy:build envoy:image envoy:image-debug 

push-envoy-base-image:	
	cd $(TOP); $(BOB) -r $(ENVOY_RULESET) envoy:push-image envoy:push-image-debug
	
###########################################
# Tool Package with K6 and the simulators #
###########################################

#Each tool has an export rule/target where it writes the artifacts to its own output-directory.
#This target invokes the export rules and then collects the artifacts in its own export directory which is then zipped.

tool-package:
	#Create structure
	@rm -rf ../.bob/export/
	@mkdir -p ../.bob/export/helm
	@mkdir -p ../.bob/export/docker
	@mkdir -p ../.bob/export/versions
	@mkdir -p ../.bob/export/certificates/k6
	@mkdir -p ../.bob/export/certificates/scpwrk
	#Store commit ID
	@git rev-parse HEAD >> ../.bob/export/COMMIT
	#Make and export K6
	@make -C ../devtools/k6 export
	@cp -r ../.bob/export-k6/* ../.bob/export
	@mv ../.bob/export/certificates/*.key ../.bob/export/certificates/k6
	@mv ../.bob/export/certificates/*.crt ../.bob/export/certificates/k6
	#Make and export CHFSim/Redis
	@make -C ../simulators/chfsim export
	@cp -r ../.bob/export-chfsim/* ../.bob/export
	#Make and export NRFSim
	@make -C ../simulators/nrfsim export
	@cp -r ../.bob/export-nrfsim/* ../.bob/export
	#Make and export SEPPSim
	@make -C ../simulators/seppsim export
	@cp -r ../.bob/export-seppsim/* ../.bob/export
	#Check if scp-certificates are present, if not, they are created
	#This check is performed to not tamper with already existing installations, as calling the certificate rule will replace the currently deployed secret
	@if [ ! -d "../.bob/certificates/scpwrk" ]; then \
		java -jar /proj/sc-tools/bin/supreme/supreme.jar generate install -d scpwrk -p $(CERTS_OUTDIR)/supreme.yaml; \
	fi
	@cp ../scripts/certificates/certm_worker/keys/scpwrk/* ../.bob/export/certificates/scpwrk
	#Include Readme/Certificate script
	@cp ../scripts/tool-package/* ../.bob/export/
	#Copy version numbers to automate loading and tagging of the images
	@cp ../.bob/var.k6-version ../.bob/var.influxdb-version ../.bob/var.influxdb2-chart-version ../.bob/var.grafana-chart-version ../.bob/var.telegraf-chart-version ../.bob/var.chfsim-version ../.bob/var.redis-version ../.bob/var.nrfsim-version ../.bob/var.seppsim-version ../.bob/export/versions/
	echo $(USER) > ../.bob/export/versions/user_id
	#ZIP it
	@cd ../.bob/export && zip -r ../tool-package-$(TIMESTAMP).zip . && cd -		

#############################
### CSAR package creation ###
#############################

# Set $RELEASE to true beforehand.
csar: export RELEASE=true
csar: export PACKAGING=true
csar: export CSAR_CHARTS=true
csar: export BSF=true
csar: export BSF_DIAMETER=true
csar: export WCDB=true
csar: export SCP=true
csar: export SEPP=true
csar: export LOGGING=true
csar: export PRODUCTION=true
csar:
	cd $(TOP); $(BOB) -r $(RULESET) init release:generate-released-version build  build-images push-images update-helm lint package-helm push-helm update-umbrella repository-index package-umbrella check-charts-versions push-umbrella fetch-crds csar 

# Possibilty for creating csar package for test purposes without deploying
#
# Meaning of the variables:
# RELEASE controls how the versioning will be displayed, so that a "clean" numbering will appear (eg 1.1.0+1234), and no hashes, no "dirty" appendix.
# PACKAGING controls whether the csar&images will be stored on the production or on the test repos. If it is set to FALSE the storage takes place on the development repos.
# The variables related to each micro-service (BSF, SCP,...) are not strictly needed in order to generate csar pkgs. 
# LOGGING & PRODUCTION have to do with logging possibilities and armdocker usage.
# CSAR_DETAIL allows to append a string which will be used for the name of the folder creation where the csar pkg lies.
# CSAR_CHARTS stores the helm charts inside a user folder in order to have them isolated from the rest and avoid swaping with other chart versions when creating the umbrella.
# (path for such folder eg in the development repo: https://armdocker.rnd.ericsson.se/artifactory/proj-5g-bsf-helm/eiffelesc/ It is created each time and contains ONLY the charts of that specific run).
#
csar-test: export RELEASE=true 
csar-test: export PACKAGING=false
csar-test: export CSAR_CHARTS=true
csar-test: export BSF=true
csar-test: export BSF_DIAMETER=true
csar-test: export WCDB=true
csar-test: export SCP=true
csar-test: export SEPP=true
csar-test: export LOGGING=true
csar-test: export PRODUCTION=true
csar-test: export CSAR_DETAIL?=false
csar-test:
	cd $(TOP); $(BOB) -r $(RULESET) init release:generate-released-version build build-images push-images update-helm lint package-helm push-helm update-umbrella repository-index package-umbrella check-charts-versions push-umbrella fetch-crds csar


csar-like-deploy: export RELEASE=true
csar-like-deploy: export PACKAGING=false
csar-like-deploy: export CSAR_CHARTS=true
csar-like-deploy: export BSF=true
csar-like-deploy: export BSF_DIAMETER=true
csar-like-deploy: export WCDB=true
csar-like-deploy: export SCP=true
csar-like-deploy: export SEPP=true
csar-like-deploy: export LOGGING=true
csar-like-deploy: export PRODUCTION=true
csar-like-deploy:
	cd $(TOP); $(BOB) -r $(RULESET) init build build-images push-images update-helm package-helm push-helm update-umbrella repository-index package-umbrella check-charts-versions push-umbrella fetch-crds test-csar:adp-secrets test-csar:unzip-csar deploy-crds test-csar:all


csar-test-fast: export RELEASE=true 
csar-test-fast: export PACKAGING=false
csar-test-fast: export CSAR_CHARTS=true
csar-test-fast: export BSF=true
csar-test-fast: export BSF_DIAMETER=true
csar-test-fast: export WCDB=true
csar-test-fast: export SCP=true
csar-test-fast: export SEPP=true
csar-test-fast: export LOGGING=true
csar-test-fast: export PRODUCTION=true
csar-test-fast: export CSAR_DETAIL?=false
csar-test-fast:
	cd $(TOP); $(BOB) -r $(RULESET) init release:generate-released-version build build-images push-images update-helm package-helm push-helm update-umbrella repository-index package-umbrella-fast check-charts-versions push-umbrella fetch-crds csar

csar-cncs:
	cd $(TOP); $(BOB) -r $(RULESET) sc.init $(NODEBUG)
	cd $(TOP); $(BOB) -r $(CSAR_RULESET) init-csar $(NODEBUG)
	cd $(TOP); $(BOB) -r $(CSAR_RULESET) fetch-dependencies $(NODEBUG)
	cd $(TOP); $(BOB) -r $(CSAR_RULESET) dependencies-versions $(NODEBUG)
	cd $(TOP); $(BOB) -r $(CSAR_RULESET) fetch-crds $(NODEBUG)
	cd $(TOP); $(BOB) -r $(CSAR_RULESET) create-csar $(NODEBUG)
	cd $(TOP); $(BOB) -r $(CSAR_RULESET) push-csar $(NODEBUG)

#####################
### Release Notes ###
#####################
#
# How to use release notes
#
# The below targets create release notes that summarizes bugs
# (DND-xxxx) and support cases (SCDS-xxxxx). The release notes
# are for internal use only, e.g. to be puplished on
# https://wcdma-confluence.rnd.ki.sw.ericsson.se/display/DSCNode/Internal+Release+Information.
#
# The targets release-notes and release-notes-no-titles produces
# the same output except for the former pulling the bug/cases'
# slogans from the JIRA server. The output becomes much more
# readable by this. Therefore release-notes assumes the 'curl'
# command to be available and requires a manual user login.
# Therfore, use release-notes-no-titles in case this make target
# shall be used within automated scripting.
#
# Requirements:
#       Both targets base on the use of GIT tags. At least 2! tags must
#       be available (check with 'git log --oneline'). All bugs and support
#       cases are listed between the latest release tag and the release
#       tag before that. Any commits younger than the latest release tag
#       are ignored!
# 
#       IMPORTANT Create a release tag in the right format:
#               1. Switch to the release branch in question and do a 'git pull'.
#               2. Check with 'git log --oneline' if the GIT HEAD already has a tag.
#               3. If not, create a new release tag in the semantic version
#                  format with with leading loweer case 'v'. The annotation '-m'
#                  has to reflect the product number + the date in the format MM/DD/YYYY.
#                  Product number creation as below (more info on: https://wcdma-confluence.rnd.ki.sw.ericsson.se/pages/viewpage.action?pageId=247537604)
#
#
#                  Example:
#                       'git tag -a v1.3.1+329 -m "CXP 903 8365/1 R4B329 05/20/2021"'
#                                    | | | |-------------------------|       = the '329' in 'R4B329'
#                                    | | |--------------------------|        = the 'B'   in 'R4B329' (A=0, B=1, C=2, ...)
#                                    | |---------------------------|         = the '4'   in 'R4B329'. Note the +1 logic!
#                                    |--------------------------|            = the '1'   in 'CXP 903 8365/1'
#
#
#                  Example (same as before but tag for a past commit):
#                        'git tag -a v1.3.1+329 42bc4bde -m "CXP 903 8365/1 R4B329 05/20/2021"
#
#               4. Eventually push the new tag 'git push origin v1.3.1+329'
#
#       IMPORTANT Create valid commit comments in the right format:
#               1. The first line! of a commit message that fixes a bug must
#                  contain a string of the form DND-XXXXX, SCDS-XXXXX or GSSUPP-XXXXXX.
#               2. Check with 'git log --oneline' in case of doubts.
#       
#       
#
# Example:
#               1. After having tagged perform a dry run to confirm release notes will be created for the intended SW version:
#               $ make release-notes DRY=true
#               Creating internal release notes for SC1.3.0+61 (RELEASED_VERSION_TAG=1) compared to SC1.2.3+9 (COMPARED_VERSION_TAG=2).
#               
#               Found 82 issue(s) between these versions that will be filtered for DND bugs and SCDS cases that report bugs:
#               SCDS-223 SCDS-197 DND-24476 DND-24464 DND-24456 DND-24455 DND-24452 DND-24451 DND-24444 DND-24418 DND-24365 DND-24317 DND-24311 DND-24277 DND-24165 DND-24162 DND-24105 DND-24098 DND-24086 DND-24078 DND-24076 DND-24071 DND-24070 DND-24049 DND-24028 DND-23907 DND-23884 DND-23858 DND-23853 DND-23850 DND-23842 DND-23840 DND-23830 DND-23827 DND-23825 DND-23818 DND-23815 DND-23809 DND-23807 DND-23786 DND-23761 DND-23756 DND-23735 DND-23732 DND-23722 DND-23717 DND-23706 DND-23703 DND-23701 DND-23700 DND-23699 DND-23680 DND-23673 DND-23655 DND-23653 DND-23652 DND-23651 DND-23649 DND-23646 DND-23583 DND-23579 DND-23578 DND-23575 DND-23571 DND-23570 DND-23514 DND-23409 DND-23400 DND-23368 DND-23366 DND-23365 DND-23362 DND-23055 DND-23025 DND-22697 DND-22414 DND-22411 DND-22346 DND-22345 DND-21568 DND-20842 DND-20183
#               
#               This make target requires git tags be correctly defined (see Makefile comments for more details).
#               
#               Use version tag parameters to check different versions:
#                   make release-notes RELEASED_VERSION_TAG=1 COMPARED_VERSION_TAG=2            // Compare latest with former version = default behavior
#                   make release-notes RELEASED_VERSION_TAG=2 COMPARED_VERSION_TAG=5            // Compare former version with the version 3 releases older
#                   make release-notes DRY=true RELEASED_VERSION_TAG=2 COMPARED_VERSION_TAG=5   // Ditto, just show issues that will be processed
#               
#               Pick from the following available version tags:
#                    1	v1.3.0+61       CXP 903 8365/1 R4A61 17/12/2020
#                    2	v1.2.3+9        CXP 903 8365/1 R3D9 22/10/2020
#                    3	v1.2.2+263      CXP 903 8365/1 R3C263 08/10/2020
#                    4	v1.2.2+247      CXP 903 8365/1 R3C247 08/10/2020
#                    5	v1.2.1+141      CXP 903 8365/1 R3B141 18/09/2020
#                  ...  ...             ...
#                   27	v1.0.0+16       SC 1.0.0+16 07/26/2019
#                   28	DUMMY           DUMMY
#
#               $ make release-notes
#               Creating internal release notes for SC1.3.0+61 (RELEASED_VERSION_TAG=1) compared to SC1.2.3+9 (COMPARED_VERSION_TAG=2).
#               Access to https://cc-jira.rnd.ki.sw.ericsson.se/browse: Enter host password for user 'eedsvs':
#               
#               >>>>>>>>>>>> Import below up to '<<<<...' with 'Insert Markup' on Confluence >>>>>>>>>>>>
#               h1. {color:blue}CXP 903 8365/1 R4A61 17/12/2020 (v1.3.0+61){color}
#               h2. CSAR Package
#               
#                   - [eric-sc-1.3.0+61.csar|https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/SC1.3.0/1.3.0+61/csar/eric-sc-1.3.0+61.csar] ([MD5|https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/SC1.3.0/1.3.0+61/csar/eric-sc-1.3.0+61.csar.md5])
#               
#               h2. values.yaml
#                   - [eric-sc-values-1.3.0+61.yaml|https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/SC1.3.0/1.3.0+61/release-artifacts/eric-sc-values-1.3.0+61.yaml]
#               
#               h2. eric-sc-tools
#                   - [eric-sc-tools-1.3.0+61.tgz|https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/SC1.3.0/1.3.0+61/tools/eric-sc-tools-1.3.0+61.tgz]
#               
#               h2. Fixed bugs and support cases since CXP 903 8365/1 R3D9 22/10/2020 (v1.2.3+9):
#                   - [DND-21568: etcd client forcefully deleted, Dynamic Producer Heartbeats unsuccessful for 15 minutes - CC Jira|https://cc-jira.rnd.ki.sw.ericsson.se/browse/DND-21568]
#                   - [DND-22411: Vertx HTTP server does not shutdown gracefully - CC Jira|https://cc-jira.rnd.ki.sw.ericsson.se/browse/DND-22411]
#                   - [DND-22697: Wrong logging format - CC Jira|https://cc-jira.rnd.ki.sw.ericsson.se/browse/DND-22697]
#                   - [DND-23025: make tool-package should not depend on deployed SC - CC Jira|https://cc-jira.rnd.ki.sw.ericsson.se/browse/DND-23025]
#                   - [DND-23514: scp/sepp managers create and use secrets with the same name - CC Jira|https://cc-jira.rnd.ki.sw.ericsson.se/browse/DND-23514]
#                   ...
#                   - [DND-24464: wrong backend.dbuser used for mediator/alarm-handler/cmyp - CC Jira|https://cc-jira.rnd.ki.sw.ericsson.se/browse/DND-24464]
#                   - [DND-24476: VIP_SIG_SEPP still visible in eric-sc-values.yaml file - CC Jira|https://cc-jira.rnd.ki.sw.ericsson.se/browse/DND-24476]
#                   - [SCDS-197: Capacity based weighted round robin load balancing in a producer pool not working - CC Jira|https://cc-jira.rnd.ki.sw.ericsson.se/browse/SCDS-197]
#                   - [SCDS-223: BSF register to NRF without ipv6 endpoint registered - CC Jira|https://cc-jira.rnd.ki.sw.ericsson.se/browse/SCDS-223]
#               
#               \\
#               ----
#               <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
#               $


#
# 1 = most recent tag, 2 = the one before, etc...
RELEASED_VERSION_TAG := 1
COMPARED_VERSION_TAG := 2

# Create release notes of currently tagged version compared to former version (requires curl + manual password entry!)
#
# In case you need a very quick summary (w/o accessing Jira) of which SC versions and which DND cases will be reported
# invoke a dry run through "make release-notes DRY=true".
# The latter is espesically useful to see the impact of changing RELEASED_VERSION_TAG and COMPARED_VERSION_TAG.
#
release-notes:
	@JIRA_BASE_URL="https://eteamproject.internal.ericsson.com/browse"; \
	REPO_BASE_URL="https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc"; \
	TOOLS_BASE_URL="$${REPO_BASE_URL}/tools/eric-sc-tools-"; \
	RELEASED_VERSION=$$(git tag -l | sort -V | tail -$(RELEASED_VERSION_TAG) | head -1); \
	VERSION_PREFIX_BASE=$$(echo "$${RELEASED_VERSION:1}" | grep -oP '^[0-9]*\.[0-9]*\.[0-9]*'); \
	RELEASED_VERSION_ANNOTATED="$$(git tag -ln | sort -V | tail -$(RELEASED_VERSION_TAG) | head -1 | cut -c 17-) ($$RELEASED_VERSION)"; \
	COMPARED_VERSION=$$(git tag -l | sort -V | tail -$(COMPARED_VERSION_TAG) | head -1); \
	COMPARED_VERSION_ANNOTATED="$$(git tag -ln | sort -V | tail -$(COMPARED_VERSION_TAG) | head -1 | cut -c 17-) ($$COMPARED_VERSION)"; \
	RELEASE_BASE_URL="$${REPO_BASE_URL}/SC$${VERSION_PREFIX_BASE}/$${RELEASED_VERSION:1}"; \
	CSAR_PKG_URL="$${RELEASE_BASE_URL}/csar/eric-sc-$${RELEASED_VERSION:1}.csar"; \
	VALUES_YAML_URL="$${RELEASE_BASE_URL}/release-artifacts/eric-sc-values-$${RELEASED_VERSION:1}.yaml"; \
	TOOLS_URL="$${RELEASE_BASE_URL}/tools/eric-sc-tools-$${RELEASED_VERSION:1}.tgz"; \
	TMP_DIR=$$(mktemp -d); \
	REL_NOTES_TMP_FILE="$${TMP_DIR}/rel.notes.tmp"; \
	git log $${COMPARED_VERSION}..$${RELEASED_VERSION} --oneline | grep -ohE '(SCDS|DND|GSSUPP)\-[0-9]+' | sort -r | uniq > $${REL_NOTES_TMP_FILE}; \
	readarray -t ISSUE_IDS < $${REL_NOTES_TMP_FILE}; \
	echo "Creating internal release notes for SC$${RELEASED_VERSION:1} (RELEASED_VERSION_TAG=${RELEASED_VERSION_TAG}) compared to SC$${COMPARED_VERSION:1} (COMPARED_VERSION_TAG=${COMPARED_VERSION_TAG})."; \
	if [ "$${DRY}" == "true" ]; then \
		rm -rf $$TMP_DIR; \
		echo; \
		echo "Found $${#ISSUE_IDS[@]} issue(s) between these versions that will be filtered for DND bugs and SCDS cases that report bugs:"; \
		echo "$${ISSUE_IDS[@]}"; \
		echo; \
		echo "This make target requires git tags be correctly defined (see Makefile comments for more details)."; \
		echo; \
		echo "Use version tag parameters to check different versions:"; \
		echo "    make release-notes RELEASED_VERSION_TAG=1 COMPARED_VERSION_TAG=2            // Compare latest with former version = default behavior"; \
		echo "    make release-notes RELEASED_VERSION_TAG=2 COMPARED_VERSION_TAG=5            // Compare former version with the version 3 releases older"; \
		echo "    make release-notes DRY=true RELEASED_VERSION_TAG=2 COMPARED_VERSION_TAG=5   // Ditto, just show issues that will be processed"; \
		echo; \
		echo "Pick from the following available version tags:"; \
		git tag -ln | sort -V | tac | cat -n; \
	else \
		echo -n "Access to $$JIRA_BASE_URL: "; \
		curl -s -u $$USER --create-dirs -o $$TMP_DIR/#1.html $$JIRA_BASE_URL/{$$(echo "$${ISSUE_IDS[@]}" | sed 's/ /,/g')}; \
		grep -aohP '(?<=<title>).*(?=</title>)' $$TMP_DIR/*.html > $$TMP_DIR/titles.txt; \
		readarray -t BUG_TITLES < $$TMP_DIR/titles.txt; \
		rm -rf $$TMP_DIR; \
		echo; \
		echo ">>>>>>>>>>>> Import below up to '<<<<...' with 'Insert Markup' on Confluence >>>>>>>>>>>>"; \
		echo "h1. {color:blue}$$RELEASED_VERSION_ANNOTATED{color}"; \
		echo "h2. CSAR Package"; \
		echo; \
		echo "    - [$$(echo $${CSAR_PKG_URL} | cut -d/ -f10)|$${CSAR_PKG_URL}] ([MD5|$${CSAR_PKG_URL}.md5])"; \
		echo; \
		echo "h2. values.yaml"; \
		echo "    - [$$(echo $${VALUES_YAML_URL} | cut -d/ -f10)|$${VALUES_YAML_URL}]"; \
		echo; \
		echo "h2. eric-sc-tools"; \
		echo "    - [$$(echo $${TOOLS_URL} | cut -d/ -f10)|$${TOOLS_URL}]"; \
		echo; \
		echo "h2. Fixed bugs and support cases since $$COMPARED_VERSION_ANNOTATED:"; \
		for i in "$${!BUG_TITLES[@]}"; do \
			BUG_ID=$$(echo $${BUG_TITLES[$$i]} | grep -aoP '(?<=^\[).*?(?=\])'); \
			BUG_TITLE=$$(echo $${BUG_TITLES[$$i]} | sed 's/\[.*\][[:space:]]//g' | sed 's/\([\[{}\|\\]\)/\\\1/g' | sed 's/]/\\\]/g'); \
			echo "    - [$${BUG_ID}": "$${BUG_TITLE}|$$JIRA_BASE_URL/$${BUG_ID}]"; \
		done; \
		echo; \
		echo "\\\\"; \
		echo "----"; \
		echo "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"; \
		echo; \
	fi

check-baseline-deployment:		 
	echo $(BASELINE_DEPLOYMENTS)

###########################################
### Create file for helm authentication ###
###########################################

create-helm-auth:
	cd $(TOP); $(BOB) -r $(RULESET) create-helm-auth

############################################
### Removes the persistent storage files ###
############################################	

remove-log-services:
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete deployment -l 'app.kubernetes.io/name=eric-data-search-engine'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete sts -l 'app.kubernetes.io/name=eric-data-search-engine'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete all -l 'app.kubernetes.io/name=eric-data-search-engine'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete ds  -l 'app.kubernetes.io/name=eric-log-shipper'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete all -l 'app.kubernetes.io/name=eric-log-shipper'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete deployment -l 'app.kubernetes.io/name=eric-data-log-transformer'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete all -l 'app.kubernetes.io/name=eric-data-log-transformer'

remove-volumes:
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app=eric-data-document-database-pg'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app.kubernetes.io/name=eric-data-wide-column-database-cd'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app=eric-data-wide-column-database-cd'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app=eric-data-coordinator-zk'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app.kubernetes.io/name=eric-pm-bulk-reporter'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app.kubernetes.io/name=eric-data-search-engine'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app=eric-data-search-engine'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app.kubernetes.io/name=eric-data-distributed-coordinator-ed'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app.kubernetes.io/name=eric-data-distributed-coordinator-ed-sc'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app=eric-sec-ldap-server'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app=eric-pm-server'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app.kubernetes.io/name=eric-ctrl-bro' 
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app.kubernetes.io/name=eric-data-distributed-coordinator-ed-agent'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app.kubernetes.io/name=eric-data-coordinator-zk-agent'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app.kubernetes.io/name=eric-data-distributed-coordinator-ed' 
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app.kubernetes.io/name=eric-sec-key-management'
	-$(KUBECTL) -n $(KUBE_NAMESPACE) delete pvc -l 'app=eric-data-message-bus-kf'

remove-all:
	cd $(TOP); $(BOB) -r $(RULESET) clean-resources:remove-all-resources

clean-charts:
	@echo "Removing existing local chart files."
	rm -f $(TOP)/.bob/*.tgz; rm -f $(TOP)/charts/*.tgz

clean-targets:
	# TODO: What does this thing do?replace with clean target
	rm -f $(OBJECTS) bsf/bsfwrk/target/*.jar scp/lib/rxgrpc-envoy-control-api/target/*.jar ../envoyproxy/logfwdr/logforwarder ../envoyproxy/sds/target/*.jar bsf/cddjmxexporter/target/*.jar


clean:
	cd $(TOP); $(BOB) -r $(RULESET) init clean $(NODEBUG)
	rm -rf ../.bob/tool-package.zip

############################################
### Builds yang model ###
############################################

execute-model-tasks-zip:
	cd $(TOP); $(BOB) -r $(MODEL_RULESET) init zipyangfiles

zip-bsf-model: export COMPONENT=BSF
zip-bsf-model:
	@make execute-model-tasks-zip

zip-scp-model: export COMPONENT=SCP
zip-scp-model:
	@make execute-model-tasks-zip

zip-sepp-model: export COMPONENT=SEPP
zip-sepp-model:
	@make execute-model-tasks-zip

zip-all-models: export COMPONENT=ALL
zip-all-models:
	@make execute-model-tasks-zip



execute-model-tasks-generate:
	cd $(TOP); $(BOB) -r $(MODEL_RULESET) init generate
ifeq ($(CNCS),true)
	cd $(TOP); $(BOB) -r $(MODEL_RULESET) copy-output-cncs
else
	cd $(TOP); $(BOB) -r $(MODEL_RULESET) copy-output
endif

build-bsf-model: export COMPONENT=BSF
build-bsf-model:
	@make execute-model-tasks-generate

build-diameter-model: export COMPONENT=DIAMETER
build-diameter-model:
	@make execute-model-tasks-generate

build-scp-model: export COMPONENT=SCP
build-scp-model:
	@make execute-model-tasks-generate

build-pvtb-model: export COMPONENT=PVTB
build-pvtb-model:
	@make execute-model-tasks-generate

build-sepp-model: export COMPONENT=SEPP
build-sepp-model:
	@make execute-model-tasks-generate

build-models:
ifeq ($(SEPP),true)
	@make build-sepp-model
endif
ifeq ($(BSF),true)
	@make build-bsf-model
endif
ifeq ($(SCP),true)
	@make build-scp-model
endif
ifeq ($(VTAP),true)
	@make build-pvtb-model
endif

build-all-models-before-image-build:
ifeq ($(SEPP),true)
	@make build-sepp-model
endif
ifeq ($(BSF),true)
	@make build-bsf-model
endif

build-all-models-after-image-build:
ifeq ($(SCP),true)
	@make build-scp-model
endif
ifeq ($(PVTB),true)
	@make build-pvtb-model
endif

execute-model-tasks-publish:
	@VERSION_PREFIX=$$(cat ../VERSION_PREFIX); \
	VERSION_PREFIX_BASE=$$(echo "$${VERSION_PREFIX:0}" | grep -oP '^[0-9]*\.[0-9]*'); \
	echo "Release version extracted from file '5g_proto/VERSION_PREFIX' is: " $$VERSION_PREFIX_BASE;\
	echo -n "If this is ok then just press enter, otherwise enter correct release version: ";\
	read reply;\
	if [[ ! $$reply = "" ]]; then \
	    VERSION_PREFIX_BASE=$$reply; \
	    echo "Release version changed to:" $$reply; \
	    export RELEASE_PACKAGE=$$reply; \
	else \
	    export RELEASE_PACKAGE=$$VERSION_PREFIX_BASE; \
	fi; \
	cd $(TOP); $(BOB) -r $(MODEL_RULESET) init setreleasepackageversion publish \

publish-sc-common-model: export COMPONENT=SC
publish-sc-common-model: export NOINIT=true
publish-sc-common-model:
	@make execute-model-tasks-publish

publish-bsf-model: export COMPONENT=BSF
publish-bsf-model:
	@make execute-model-tasks-publish

publish-scp-model: export COMPONENT=SCP
publish-scp-model:
	@make execute-model-tasks-publish

publish-sepp-model: export COMPONENT=SEPP
publish-sepp-model:
	@make execute-model-tasks-publish

publish-all-models: publish-sc-common-model publish-bsf-model publish-scp-model publish-sepp-model



execute-model-tasks-all:
	@VERSION_PREFIX=$$(cat ../VERSION_PREFIX); \
	VERSION_PREFIX_BASE=$$(echo "$${VERSION_PREFIX:0}" | grep -oP '^[0-9]*\.[0-9]*'); \
	echo "Release version extracted from file '5g_proto/VERSION_PREFIX' is: " $$VERSION_PREFIX_BASE;\
	echo -n "If this is ok then just press enter, otherwise enter correct release version: ";\
	read reply;\
	if [[ ! $$reply = "" ]]; then \
	    VERSION_PREFIX_BASE=$$reply; \
	    echo "Release version changed to:" $$reply; \
	    export RELEASE_PACKAGE=$$reply; \
	else \
	export RELEASE_PACKAGE=$$VERSION_PREFIX_BASE; \
	fi; \
	cd $(TOP); $(BOB) -r $(MODEL_RULESET) init generate copy-output publish 

build-publish-bsf-model: export COMPONENT=BSF
build-publish-bsf-model:
	@make execute-model-tasks-all

build-publish-scp-model: export COMPONENT=SCP
build-publish-scp-model:
	@make execute-model-tasks-all

build-publish-sepp-model: export COMPONENT=SEPP
build-publish-sepp-model:
	@make execute-model-tasks-all

build-publish-all-models: publish-sc-common-model build-publish-bsf-model build-publish-scp-model build-publish-sepp-model

undeploy-sim-tools:
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) undeploy


##### DND-XXXXX ##
deploy-sim-tools: init-tools copy build-sim-tools build-images-sim-tools push-images-sim-tools package-helm-sim-tools push-helm-sim-tools package-sim-tools modify-sim-tools package-final-sim-tools

package-sim-tools:	
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) update-tools    
	
package-final-sim-tools:
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) package-tools:all package-tools:tools push-helm:tools 


ifeq ($(CSAR),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) repository-index    csar zip push-package-tgz $(NODEBUG)
else
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) deploy-tools $(NODEBUG)
endif
ifeq ($(SKIP_CERTS),false)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) certs $(NODEBUG)
endif 

copy:
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) update-tools-generic:copy  

build-sim-tools:
ifeq ($(SEPPSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) build:seppsim 
endif
ifeq ($(CHFSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) build:chfsim 
endif
ifeq ($(NRFSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) build:nrfsim 
endif

build-images-sim-tools:
ifeq ($(SEPPSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) build-images:seppsim  
endif
ifeq ($(CHFSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) build-images:chfsim 
endif
ifeq ($(K6),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) build-images:k6
endif
ifeq ($(ATMOZ_SFTP),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) build-images:sftp
endif	
ifeq ($(SYSLOG),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) build-images:syslog 
endif
ifeq ($(DSCLOAD),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) build-images:dscload
endif
ifeq ($(REDIS),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) build-images:redis 
endif
ifeq ($(NRFSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) build-images:nrfsim 
endif

push-images-sim-tools:
ifeq ($(SEPPSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-images:seppsim 
endif
ifeq ($(CHFSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-images:chfsim 
endif
ifeq ($(K6),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-images:k6  
endif
ifeq ($(ATMOZ_SFTP),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-images:sftp 
endif
ifeq ($(SYSLOG),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-images:syslog 
endif	
ifeq ($(DSCLOAD),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-images:dscload 
endif
ifeq ($(REDIS),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-images:redis  
endif
ifeq ($(NRFSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-images:nrfsim 
endif

package-helm-sim-tools:
ifeq ($(SEPPSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) package-helm:seppsim 
endif
ifeq ($(CHFSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) package-helm:chfsim 
endif
ifeq ($(K6),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) package-helm:k6
endif
ifeq ($(INFLUXDB2),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) package-helm:influxdb2
endif
ifeq ($(TELEGRAF),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) package-helm:telegraf
endif
ifeq ($(GRAFANA),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) package-helm:grafana
endif
ifeq ($(SYSLOG),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) package-helm:syslog 
endif
ifeq ($(ATMOZ_SFTP),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) package-helm:sftp 
endif	
ifeq ($(DSCLOAD),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) package-helm:dscload 
endif
ifeq ($(REDIS),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) package-helm:redis 
endif
ifeq ($(NRFSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) package-helm:nrfsim
endif

push-helm-sim-tools:
ifeq ($(SEPPSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-helm:seppsim 
endif
ifeq ($(CHFSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-helm:chfsim 
endif
ifeq ($(K6),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-helm:k6 
endif
ifeq ($(INFLUXDB2),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-helm:influxdb2
endif
ifeq ($(TELEGRAF),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-helm:telegraf
endif
ifeq ($(GRAFANA),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-helm:grafana
endif
ifeq ($(SYSLOG),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-helm:syslog 
endif
ifeq ($(ATMOZ_SFTP),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-helm:sftp  
endif	
ifeq ($(DSCLOAD),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-helm:dscload 
endif
ifeq ($(REDIS),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-helm:redis 
endif
ifeq ($(NRFSIM),true)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) push-helm:nrfsim 
endif

update-sim-tools:
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) update-tools

modify-sim-tools:
ifeq ($(SEPPSIM),false)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) modify-selected:seppsim 
endif
ifeq ($(CHFSIM),false)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) modify-selected:chfsim
endif
ifeq ($(K6),false)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) modify-selected:k6 
endif
ifeq ($(SYSLOG),false)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) modify-selected:syslog 
endif
ifeq ($(INFLUXDB2),false)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) modify-selected:influxdb2 
endif
ifeq ($(TELEGRAF),false)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) modify-selected:telegraf
endif
ifeq ($(GRAFANA),false)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) modify-selected:grafana
endif
ifeq ($(ATMOZ_SFTP),false)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) modify-selected:sftp 
endif	
ifeq ($(DSCLOAD),false)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) modify-selected:dscload
endif
ifeq ($(REDIS),false)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) modify-selected:redis 
endif
ifeq ($(NRFSIM),false)
	cd $(TOP); $(BOB) -r $(TOOLS_RULESET) modify-selected:nrfsim
endif

loadConfig:
ifeq ($(SKIP_DEFAULT_USERS),false)
ifeq ($(LOAD_DUMMY_CFG),true)
	cd $(TOP); $(BOB) -r $(RULESET) loadConfig:sc $(NODEBUG)
endif
endif

loadUsers:
ifeq ($(SKIP_DEFAULT_USERS),false)
	cd $(TOP); $(BOB) -r $(RULESET) install-default-users:default-users $(NODEBUG)
endif

loadCertificates:
ifeq ($(SKIP_DEFAULT_USERS),false)
ifeq ($(SKIP_CERTS),false)
ifeq ($(GEORED),false)
ifeq ($(NETCONF_TLS),false)
	cd $(TOP); $(BOB) -r $(RULESET) install-certificates:rootca $(NODEBUG)
endif
endif
	cd $(TOP); $(BOB) -r $(RULESET) install-certificates:nbi $(NODEBUG)
ifeq ($(BSF),true)
	cd $(TOP); $(BOB) -r $(RULESET) install-certificates:bsf-manager install-certificates:bsf-worker $(NODEBUG)
endif
ifeq ($(SCP),true)
	cd $(TOP); $(BOB) -r $(RULESET) install-certificates:scp-manager install-certificates:scp-worker $(NODEBUG)
endif
ifeq ($(SEPP),true)
	cd $(TOP); $(BOB) -r $(RULESET) install-certificates:sepp-manager install-certificates:sepp-worker $(NODEBUG)
endif
ifeq ($(NLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) install-certificates:nlf $(NODEBUG)
endif
ifeq ($(SLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) install-certificates:slf $(NODEBUG)
endif
ifeq ($(PVTB_DTLS),true)
	cd $(TOP); $(BOB) -r $(RULESET) install-certificates:pvtb $(NODEBUG)
endif
ifeq ($(LOGGING),true)
ifeq ($(SYSLOG),true)
	cd $(TOP); $(BOB) -r $(RULESET) install-certificates:transformer install-certificates:syslog $(NODEBUG)
endif
ifeq ($(LUMBERJACK),true)
	cd $(TOP); $(BOB) -r $(RULESET) install-certificates:ext-lumberjack-x $(NODEBUG)
endif
endif
ifeq ($(REFERRAL_LDAP),true)
	cd $(TOP); $(BOB) -r $(RULESET) install-certificates:internal-ldap install-certificates:referral-ldap $(NODEBUG)
endif
ifeq ($(PM_REMOTE_WRITE),true)
	cd $(TOP); $(BOB) -r $(RULESET) install-certificates:pmrw install-certificates:influxdb install-certificates:telegraf $(NODEBUG)
endif
endif
endif

load-configuration:
ifeq ($(SKIP_DEFAULT_USERS),false)
ifeq ($(LOAD_DUMMY_CFG),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.configuration:sc $(NODEBUG)
endif
endif

load-users:
ifeq ($(SKIP_DEFAULT_USERS),false)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.configuration:users $(NODEBUG)
endif

load-certificates:
ifeq ($(SKIP_DEFAULT_USERS),false)
ifeq ($(SKIP_CERTS),false)
ifeq ($(NETCONF_TLS),false)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:rootca-install $(NODEBUG)
endif
endif
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:nbi-install $(NODEBUG)
ifeq ($(BSF),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:bsf-manager-install sc.config.certificates:bsf-worker-install $(NODEBUG)
endif
ifeq ($(DIAMETER),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:diameter-install $(NODEBUG)
endif
ifeq ($(SCP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:scp-manager-install sc.config.certificates:scp-worker-install $(NODEBUG)
endif
ifeq ($(SEPP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:sepp-manager-install sc.config.certificates:sepp-worker-install $(NODEBUG)
endif
ifeq ($(NLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:nlf-install $(NODEBUG)
endif
ifeq ($(SLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:slf-install $(NODEBUG)
endif
ifeq ($(VTAP_DTLS),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:pvtb-install $(NODEBUG)
endif
ifeq ($(LOGGING),true)
ifeq ($(SYSLOG),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:transformer-install sc.config.certificates:syslog-install $(NODEBUG)
endif
ifeq ($(LUMBERJACK),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:ext-lumberjack-x-install $(NODEBUG)
endif
endif
ifeq ($(REFERRAL_LDAP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:internal-ldap-install sc.config.certificates:referral-ldap-install $(NODEBUG)
endif
ifeq ($(PM_REMOTE_WRITE),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:pmrw-install sc.config.certificates:influxdb-install sc.config.certificates:telegraf-install $(NODEBUG)
endif
endif

build-sc:
	cd $(TOP); $(BOB) -r $(RULESET) sc.init sc.build $(NODEBUG)
	@make build-models
	cd $(TOP); $(BOB) -r $(RULESET) sc.images-local $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scmonitor.all-local $(NODEBUG)
ifeq ($(RLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scrlf.all-local $(NODEBUG)
endif
ifeq ($(NLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scnlf.all-local $(NODEBUG)
endif
ifeq ($(BSF),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scbsf.all-internal-local $(NODEBUG)
endif
ifeq ($(SCP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scscp.all-internal-local $(NODEBUG)
endif
ifeq ($(SEPP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scsepp.all-internal-local $(NODEBUG)
endif
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.pre-package:prepare-dependency sc.scspider.package:prepare-local sc.scspider.clean:inca-tmp-folder $(NODEBUG)

init-deploy:
	cd $(TOP); $(BOB) -r $(RULESET) sc.init:delete-output-dir sc.base.init:create-output-dir $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) sc.base.init:copy-docker-config sc.base.init:copy-helm-credentials sc.base.init:api-tokens $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) sc.base.init:set-namespace sc.base.init:set-kube-config sc.base.init:set-supreme-properties $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) sc.base.cncs-init:gerrit-password sc.init:set-ingressHost sc.base.init:set-build-proxy $(NODEBUG)

fetch-spider-dependencies:
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:create-dependencies-folder $(NODEBUG)
ifeq ($(CI_SPIDER),true)
	@cd $(TOP); \
	export CHART_NAME="eric-sc-spider";\
	export CHART_REPO="https://arm.sero.gic.ericsson.se/artifactory/proj-sc-all-internal-helm/";\
	git fetch -q --tags --force;\
	export CI_COMMIT=$$( git rev-list "NIGHTLY-CNCS-CI" -1 --abbrev-commit );\
	if [ "$$CHART_VERSION" == "" ]; then export CHART_VERSION=$$( git tag --sort=-taggerdate --points-at $$CI_COMMIT | sed /$$( head -n1 VERSION_PREFIX)/d | sed /NIGHTLY/d | sed -n 1p );fi;\
	$(BOB) -r $(RULESET) --qq sc.scspider.post-package:create-dependencies-folder;\
	$(BOB) -r $(RULESET) --qq sc.scspider.post-package:fetch-dependency;\
	tar -C eric-sc-spider/charts -xzvf ./.bob/eric-sc-spider-dependencies/eric-sc-spider-$$CHART_VERSION.tgz eric-sc-spider/Chart.yaml;\
	echo "#######################";\
	echo "CI Commit: $$CI_COMMIT";\
	echo "Chart name: $$CHART_NAME";\
	echo "Chart repo: $$CHART_REPO";\
	echo "Chart version: $$CHART_VERSION";\
	echo "#######################";\
	cat eric-sc-spider/charts/eric-sc-spider/Chart.yaml;\
	echo "#######################";
endif
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:fetch-dependencies $(NODEBUG)

create-day0-objects:
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:rootca-create $(NODEBUG)
ifeq ($(NETCONF_TLS),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:netconf-tls-create $(NODEBUG)
endif
ifeq ($(SYSLOG),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:transformer-create $(NODEBUG)
endif
ifeq ($(GEORED),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.certificates:geored-create $(NODEBUG)
endif
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.configuration:certm $(NODEBUG)
ifeq ($(VTAP_LITE),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.configuration:tapagent $(NODEBUG)
endif
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.secrets:default-secrets sc.config.secrets:regcred-secret sc.config.configmaps:default-configmaps $(NODEBUG)

pre-deploy-sc-cncs:
	@make init-deploy fetch-spider-dependencies
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:unpack-cncs-b-chart sc.config.prepare:cncs-b-faultmappings sc.config.prepare:cncs-b-profile sc.config.prepare:cncs-b-values $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:unpack-cncs-a-chart sc.config.prepare:cncs-a-faultmappings sc.config.prepare:cncs-a-profile sc.config.prepare:cncs-a-values $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:unpack-sc-cs-chart sc.config.prepare:sc-cs-faultmappings sc.config.prepare:sc-cs-profile sc.config.prepare:sc-cs-values $(NODEBUG)
ifeq ($(BSF),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:unpack-bsf-chart sc.config.prepare:bsf-faultmappings sc.config.prepare:bsf-profile sc.config.prepare:bsf-values $(NODEBUG)
endif
ifeq ($(SCP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:unpack-scp-chart sc.config.prepare:scp-faultmappings sc.config.prepare:scp-profile sc.config.prepare:scp-values $(NODEBUG)
endif
ifeq ($(SEPP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:unpack-sepp-chart sc.config.prepare:sepp-faultmappings sc.config.prepare:sepp-profile sc.config.prepare:sepp-values $(NODEBUG)
endif
ifeq ($(DIAMETER),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:unpack-sc-dsc-chart sc.config.prepare:sc-dsc-faultmappings sc.config.prepare:sc-dsc-profile sc.config.prepare:sc-dsc-values $(NODEBUG)
endif
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.clean:roles $(NODEBUG)
	@make create-day0-objects

pre-upgrade-sc-cncs:
	@make init-deploy fetch-spider-dependencies
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:unpack-cncs-b-chart sc.config.prepare:cncs-b-profile sc.config.prepare:cncs-b-values $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:unpack-cncs-a-chart sc.config.prepare:cncs-a-profile sc.config.prepare:cncs-a-values $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:unpack-sc-cs-chart sc.config.prepare:sc-cs-profile sc.config.prepare:sc-cs-values $(NODEBUG)
ifeq ($(BSF),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:unpack-bsf-chart sc.config.prepare:bsf-profile sc.config.prepare:bsf-values $(NODEBUG)
endif
ifeq ($(SCP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:unpack-scp-chart sc.config.prepare:scp-profile sc.config.prepare:scp-values $(NODEBUG)
endif
ifeq ($(SEPP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:unpack-sepp-chart sc.config.prepare:sepp-profile sc.config.prepare:sepp-values $(NODEBUG)
endif
ifeq ($(DIAMETER),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.scspider.post-package:unpack-sc-dsc-chart sc.config.prepare:sc-dsc-profile sc.config.prepare:sc-dsc-values $(NODEBUG)
endif

post-deploy-sc-cncs:
	@make deploy-httpproxies
	@make load-users load-certificates load-configuration
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.clean:config-archives sc.config.clean:certm-config-secret $(NODEBUG)

deploy-httpproxies:
	cd $(TOP); $(BOB) -r $(RULESET) sc.post-deploy:pm-httpproxy sc.post-deploy:osmn-httpproxy sc.post-deploy:envoy-admin-httpproxy sc.post-deploy:search-engine-httpproxy $(NODEBUG)

deploy-sc-dsc:
	cd $(TOP); $(BOB) -r $(RULESET) sc.deploy:sc-dsc $(NODEBUG)
	
deploy-sc-bsf:
	cd $(TOP); $(BOB) -r $(RULESET) sc.deploy:bsf $(NODEBUG)

deploy-sc-scp:
	cd $(TOP); $(BOB) -r $(RULESET) sc.deploy:scp $(NODEBUG)

deploy-sc-sepp:
	cd $(TOP); $(BOB) -r $(RULESET) sc.deploy:sepp $(NODEBUG)

deploy-sc-cs:
	cd $(TOP); $(BOB) -r $(RULESET) sc.deploy:sc-cs $(NODEBUG)

deploy-cncs-b:
	cd $(TOP); $(BOB) -r $(RULESET) sc.deploy:cncs-b $(NODEBUG)

deploy-cncs-a:
	cd $(TOP); $(BOB) -r $(RULESET) sc.deploy:cncs-a $(NODEBUG)

deploy-atmoz-sftp:
	cd $(TOP); $(BOB) -r $(SFTP_RULESET) init:sftp-version image package push deploy $(NODEBUG)

deploy-vtaprecorder:
	cd $(TOP); $(BOB) -r $(VTAPRECORDER_RULESET) init:version dtlsServer image package push deploy $(NODEBUG)

deploy-sc-cncs:
ifeq ($(VTAP),true)
	@make deploy-vtaprecorder
endif
ifeq ($(VTAP_LITE),true)
	@make deploy-atmoz-sftp
endif
	@make deploy-cncs-b
	@make deploy-cncs-a
	@make deploy-sc-cs
ifeq ($(SCP),true)
	@make deploy-sc-scp
endif
ifeq ($(SEPP),true)
	@make deploy-sc-sepp
endif
ifeq ($(BSF),true)
	@make deploy-sc-bsf
endif
ifeq ($(DIAMETER),true)
	@make deploy-sc-dsc
endif

deploy:
ifeq ($(GEORED),true)
	@make deploy-geored
else
	@make usage-history
ifeq ($(CNCS),true)
	@make pre-deploy-sc-cncs
	@make deploy-sc-cncs
	@make post-deploy-sc-cncs
else
	@make deprecation-info
	@make deploy-sc-non-cncs
endif
endif

deploy-geored:
	cd $(TOP); $(BOB) -r $(RULESET) sc.deploy-geored:check-geored-ns
	@make pre-deploy-sc-cncs
	@make deploy-sc-cncs
	@make post-deploy-sc-cncs
	cd $(TOP); $(BOB) -r $(RULESET) sc.deploy-geored:switch-to-dc2 $(NODEBUG)
	@make fetch-spider-dependencies
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.prepare:bsf-values $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.clean:roles $(NODEBUG)
	@make create-day0-objects
	@make deploy-sc-cncs
	@make post-deploy-sc-cncs
	cd $(TOP); $(BOB) -r $(RULESET) sc.deploy-geored:switch-to-dc1 $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) sc.config.prepare:bsf-values $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) sc.upgrade:bsf $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) sc.deploy-geored:config-geored $(NODEBUG)

deploy-sc-non-cncs: build build-all-models-before-image-build images update-helm build-all-models-after-image-build push-helm update-umbrellas
deploy-sc-non-cncs:
ifeq ($(GEORED),true)
	cd $(TOP); $(BOB) -r $(RULESET) package-umbrella-fast deploy-geored
else
ifeq ($(TAPAGENT),true)
	cd $(TOP); $(BOB) -r $(SFTP_RULESET) init:sftp-version image package push deploy $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) config-tapagent $(NODEBUG)
endif
	cd $(TOP); $(BOB) -r $(RULESET) package-umbrella-fast $(NODEBUG)
ifeq ($(NETCONF_TLS),true)
	cd $(TOP); $(BOB) -r $(RULESET) create-certificates:rootca create-certificates:netconf-tls $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) deploy:create-certm-config $(NODEBUG)
endif
	cd $(TOP); $(BOB) -r $(RULESET) deploy:adp-secrets deploy:imagePullSecret $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) generate-values $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) deploy:ihc $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) deploy:pm-httpproxy deploy:osmn-httpproxy deploy:envoy-admin-httpproxy deploy:search-engine-httpproxy $(NODEBUG)
ifeq ($(VTAPRECORDER),true)
	cd $(TOP); $(BOB) -r $(VTAPRECORDER_RULESET) init:version dtlsServer image package push deploy $(NODEBUG)
endif
	@make loadUsers loadCertificates loadConfig
	cd $(TOP); $(BOB) -r $(RULESET) deploy:delete-certm-config-secret deploy:delete-certm-config-secret2 $(NODEBUG)
endif
	@make deprecation-info

update-umbrellas:
	cd $(TOP); $(BOB) -r $(RULESET) update-umbrella:clean update-umbrella:copy update-umbrella:update-product-numbers update-umbrella:monitor $(NODEBUG)
ifeq ($(BSF),true)
	cd $(TOP); $(BOB) -r $(RULESET) update-umbrella:bsf $(NODEBUG)
endif
ifeq ($(BSF_DIAMETER),true)
	cd $(TOP); $(BOB) -r $(RULESET) update-umbrella:bsf-diameter $(NODEBUG)
endif
ifeq ($(SCP),true)
	cd $(TOP); $(BOB) -r $(RULESET) update-umbrella:scp $(NODEBUG)
endif
ifeq ($(SEPP),true)
	cd $(TOP); $(BOB) -r $(RULESET) update-umbrella:sepp $(NODEBUG)
endif
ifeq ($(NLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) update-umbrella:nlf $(NODEBUG)
endif
ifeq ($(RLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) update-umbrella:rlf $(NODEBUG)	
endif
ifeq ($(SLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) update-umbrella:slf $(NODEBUG)
endif

upgrade-sc-cncs:
	cd $(TOP); $(BOB) -r $(RULESET) sc.upgrade:cncs-b sc.upgrade:cncs-a sc.upgrade:sc-cs $(NODEBUG)
ifeq ($(BSF),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.upgrade:bsf $(NODEBUG)
endif
ifeq ($(SCP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.upgrade:scp $(NODEBUG)
endif
ifeq ($(SEPP),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.upgrade:sepp $(NODEBUG)
endif
ifeq ($(DIAMETER),true)
	cd $(TOP); $(BOB) -r $(RULESET) sc.upgrade:sc-dsc $(NODEBUG)
endif

upgrade:
ifeq ($(CNCS),true)
	@make pre-upgrade-sc-cncs
	@make upgrade-sc-cncs
else
	@make upgrade-sc-non-cncs
endif

upgrade-sc-non-cncs: build  build-all-models-before-image-build images update-helm  lint build-all-models-after-image-build push-helm update-umbrellas
upgrade-sc-non-cncs:
	cd $(TOP); $(BOB) -r $(RULESET) package-umbrella-fast upgrade $(NODEBUG)

lint:
ifneq ($(SPEED),fast)
	cd $(TOP); $(BOB) -r $(RULESET) lint
endif 

update-helm:
ifeq ($(BSF),true)
	cd $(TOP); $(BOB) -r $(RULESET) update-helm:bsf $(NODEBUG) &
endif
ifeq ($(BSF_DIAMETER),true)
	cd $(TOP); $(BOB) -r $(RULESET) update-helm:bsf-diameter $(NODEBUG) &
endif
ifeq ($(SCP),true)
	cd $(TOP); $(BOB) -r $(RULESET) update-helm:scp $(NODEBUG) &
endif
ifeq ($(SEPP),true)
	cd $(TOP); $(BOB) -r $(RULESET) update-helm:sepp $(NODEBUG) &
endif
ifeq ($(NLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) update-helm:nlf $(NODEBUG) &
endif
ifeq ($(RLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) update-helm:rlf $(NODEBUG) &
endif
ifeq ($(SLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) update-helm:slf $(NODEBUG) &
endif
	cd $(TOP); $(BOB) -r $(RULESET) update-helm:monitor $(NODEBUG)

push-helm:
ifeq ($(BSF),true)
	cd $(TOP); $(BOB) -r $(RULESET) package-helm:bsf push-helm:bsf $(NODEBUG) &
endif
ifeq ($(BSF_DIAMETER),true)
	cd $(TOP); $(BOB) -r $(RULESET) package-helm:bsf-diameter push-helm:bsf-diameter $(NODEBUG) &
endif
ifeq ($(SCP),true)
	cd $(TOP); $(BOB) -r $(RULESET) package-helm:scp push-helm:scp $(NODEBUG) &
endif
ifeq ($(SEPP),true)
	cd $(TOP); $(BOB) -r $(RULESET) package-helm:sepp push-helm:sepp $(NODEBUG) &
endif
ifeq ($(NLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) package-helm:nlf push-helm:nlf $(NODEBUG) &
endif
ifeq ($(RLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) package-helm:rlf push-helm:rlf $(NODEBUG) &
endif
ifeq ($(SLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) package-helm:slf push-helm:slf $(NODEBUG) &
endif
	cd $(TOP); $(BOB) -r $(RULESET) package-helm:monitor push-helm:monitor $(NODEBUG)

images:
ifeq ($(BSF),true)
	cd $(TOP); $(BOB) -r $(RULESET) build-images:bsf push-images:bsf $(NODEBUG) &
endif
ifeq ($(BSF_DIAMETER),true)
	cd $(TOP); $(BOB) -r $(RULESET) build-images:bsf-diameter push-images:bsf-diameter $(NODEBUG) &
endif
ifeq ($(SCP),true)
	cd $(TOP); $(BOB) -r $(RULESET) build-images:scp push-images:scp push-images:logfwdr build-images:sds push-images:sds $(NODEBUG) &
endif
ifeq ($(SEPP),true)
	cd $(TOP); $(BOB) -r $(RULESET) build-images:sepp push-images:sepp push-images:logfwdr build-images:sds push-images:sds $(NODEBUG) &
endif
ifeq ($(NLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) build-images:nlf push-images:nlf $(NODEBUG) &
endif
ifeq ($(RLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) build-images:rlf push-images:rlf $(NODEBUG) &
endif
ifeq ($(SLF),true)
	cd $(TOP); $(BOB) -r $(RULESET) build-images:slf push-images:slf $(NODEBUG) &
endif	
ifeq ($(TAPAGENT),true)
	cd $(TOP); $(BOB) -r $(RULESET) push-images:tapagent $(NODEBUG)
	cd $(TOP); $(BOB) -r $(RULESET) build-images:tlskeylogagent push-images:tlskeylogagent $(NODEBUG) &
endif
ifeq ($(TAPCOLLECTOR),true)
	cd $(TOP); $(BOB) -r $(RULESET) build-images:tapcollector push-images:tapcollector $(NODEBUG) &
endif
	cd $(TOP); $(BOB) -r $(RULESET) build-images:certnotifier push-images:certnotifier $(NODEBUG) &
	cd $(TOP); $(BOB) -r $(RULESET) build-images:monitor push-images:monitor $(NODEBUG) &

deploy-ci:
	@make CI_SPIDER=true deploy

upgrade-ci:
	@make CI_SPIDER=true upgrade

show-scspider-ci:
	@cd $(TOP); \
	export CHART_NAME="eric-sc-spider";\
	export CHART_REPO="https://arm.sero.gic.ericsson.se/artifactory/proj-sc-all-internal-helm/";\
	git fetch -q --tags --force;\
	if [ "$$CHART_VERSION" == "" ];\
	then \
	   export CI_COMMIT=$$( git rev-list "NIGHTLY-CNCS-CI" -1 --abbrev-commit );\
	   export CHART_VERSION=$$( git tag --sort=-taggerdate --points-at $$CI_COMMIT | sed /$$( head -n1 VERSION_PREFIX)/d | sed /NIGHTLY/d | sed -n 1p );\
	else \
	   export CI_COMMIT=$$( git rev-list "$$CHART_VERSION" -1 --abbrev-commit );\
	fi;\
	echo "#######################";\
	echo "CI Commit: $$CI_COMMIT";\
	echo "Chart name: $$CHART_NAME";\
	echo "Chart repo: $$CHART_REPO";\
	echo "Chart version: $$CHART_VERSION";\
	echo "#######################";\
	git show --no-patch $$CHART_VERSION | sed -n -e "/apiVersion/,/commit/ p" | grep -v "commit";\
	echo "#######################"

show-scspider-ci-latest:
	export CHART_VERSION="";
	@make show-scspider-ci
