# vim:ts=4:sw=4:sw=4:et
#
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
# Created on: Jan 14, 2020
#     Author: eotkkni
#

TOP := ../..
include $(TOP)/Makefile.config

BOB_OUTDIR := ../.bob
EJBCA_NAME := eric-sec-certm-ejbca
RULESET := rulesets/ruleset2.0-eric-ejbca.yaml

.PHONY: build build-ejbca deploy deploy-ejbca uprgade replace export undeploy undeploy-ejbca clean clean-ejbca

build: clean build-ejbca

build-ejbca:
	cd $(TOP); $(BOB) -r $(RULESET) init image:build-ejbca package:helm-chart-ejbca push:docker-image-ejbca push:helm-chart-ejbca

deploy: clean deploy-ejbca

deploy-ejbca: build-ejbca
	cd $(TOP); $(BOB) -r $(RULESET) deploy:deploy-ejbca

upgrade: build
	$(HELM) upgrade $(EJBCA_NAME)-$(USER) --namespace 5g-bsf-$(USER) $(TOP)/.bob/$(EJBCA_NAME)*.tgz

replace: clean build undeploy deploy

export: build
	cd $(TOP); $(BOB) -r $(RULESET) export

undeploy: undeploy-ejbca

undeploy-ejbca:
	cd $(TOP); $(BOB) -r $(RULESET) undeploy:undeploy-ejbca

clean: clean-ejbca

clean-ejbca:
	cd $(TOP); $(BOB) -r $(RULESET) clean
