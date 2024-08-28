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
# Created on: Jan 22, 2019
#     Author: eedsvs
#

TOP := ../..
include $(TOP)/Makefile.config

K6_CHART_NAME := eric-k6
INFLUXDB_CHART_NAME := eric-influxdb
RULESET := rulesets/ruleset2.0-eric-k6.yaml

.PHONY: init build build-k6 build-influxdb deploy deploy-load deploy-k6 deploy-k6-bsf deploy-influxdb \
        deploy-influxdb-tls add-certificates upgrade-k6 upgrade-influxdb replace export undeploy undeploy-load \
		undeploy-k6 undeploy-influxdb clean-k6 clean-influxdb clean

init:
	cd $(TOP); $(BOB) -r $(RULESET) init

build: init build-k6 build-influxdb

build-k6: clean-k6 
	cd $(TOP); $(BOB) -r $(RULESET) init:k6-version image:build-k6 package:helm-chart-k6 push:docker-image-k6 push:helm-chart-k6

build-influxdb: clean-influxdb
	cd $(TOP); $(BOB) -r $(RULESET) init:influxdb-version image:build-influxdb package:helm-chart-influxdb push:docker-image-influxdb push:helm-chart-influxdb

deploy: clean init add-certificates deploy-k6

deploy-load: clean init add-certificates deploy-k6 deploy-influxdb

deploy-k6: build-k6
	cd $(TOP); $(BOB) -r $(RULESET) deploy:deploy-k6

deploy-k6-bsf: build-k6
	cd $(TOP); $(BOB) -r $(RULESET) deploy:deploy-k6-bsf

deploy-influxdb: build-influxdb
	cd $(TOP); $(BOB) -r $(RULESET) deploy:deploy-influxdb
	
deploy-influxdb-tls: build-influxdb
	cd $(TOP); $(BOB) -r $(RULESET) deploy:deploy-influxdb-tls

add-certificates:
	cd $(TOP); java -jar /proj/sc-tools/bin/supreme/supreme.jar generate -d k6 -p $(CERTS_OUTDIR)/k6_supreme.yaml
	cd $(TOP); mkdir -p devtools/k6/certs
	cd $(TOP); cp -R $(CERTS_OUTDIR)/rootca devtools/k6/certs
	cd $(TOP); cp -R $(CERTS_OUTDIR)/k6/* devtools/k6/certs

upgrade-k6: build-k6
	$(HELM) upgrade $(K6_CHART_NAME)-$(USER) $(TOP)/.bob/$(K6_CHART_NAME)-$(shell cat $(TOP)/.bob/var.k6-version).tgz --namespace 5g-bsf-$(USER)

upgrade-influxdb: build-influxdb
	$(HELM) upgrade $(INFLUXDB_CHART_NAME)-$(USER) $(TOP)/.bob/$(INFLUXDB_CHART_NAME)-$(shell cat $(TOP)/.bob/var.influxdb-version).tgz --namespace 5g-bsf-$(USER)

replace: clean init build undeploy deploy

export: build add-certificates
	cd $(TOP); $(BOB) -r $(RULESET) export

undeploy: undeploy-k6

undeploy-load: undeploy-k6 undeploy-influxdb

undeploy-k6:
	@echo "Deleting chart(s) of name $(K6_CHART_NAME) in namespace $(KUBE_NAMESPACE)..."
	cd $(TOP); $(BOB) -r $(RULESET) undeploy:undeploy-k6
	@echo "Deleted chart(s) of name $(K6_CHART_NAME) in namespace $(KUBE_NAMESPACE)."

undeploy-influxdb:
	@echo "Deleting chart(s) of name $(INFLUXDB_CHART_NAME) in namespace $(KUBE_NAMESPACE)..."
	cd $(TOP); $(BOB) -r $(RULESET) undeploy:undeploy-influxdb
	@echo "Deleted chart(s) of name $(INFLUXDB_CHART_NAME) in namespace $(KUBE_NAMESPACE)."

clean-k6:
	cd $(TOP); $(BOB) -r $(RULESET) clean:k6-helm clean:k6-version

clean-influxdb:
	cd $(TOP); $(BOB) -r $(RULESET) clean:influxdb-helm clean:influxdb-version

clean: clean-k6 clean-influxdb
	cd $(TOP); $(BOB) -r $(RULESET) clean:export
