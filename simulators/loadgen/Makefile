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
#     Author: eedstl
#

TOP := ../..
include $(TOP)/Makefile.config

CHART_NAME := eric-loadgen
RULESET := rulesets/ruleset2.0-eric-loadgen.yaml

.PHONY: all build deploy export upgrade replace undeploy clean

all: undeploy clean deploy

build:
	cd $(TOP); $(BOB) -r $(RULESET) init build image package push:docker-image

deploy: build	
	cd $(TOP); $(BOB) -r $(RULESET) deploy

export:
	cd $(TOP); $(BOB) -r $(RULESET) init build image package export

upgrade: build
	@echo
	@echo
	@echo "WARNING: Upgrade is broken"
	@echo
	@echo
	helm upgrade $(CHART_NAME)-$(USER) $(TOP)/.bob/$(CHART_NAME)*.tgz --namespace 5g-bsf-$(USER)

replace: clean build undeploy deploy

undeploy:
	cd $(TOP); $(BOB) -r $(RULESET) undeploy

clean:
	cd $(TOP); $(BOB) -r $(RULESET) clean
