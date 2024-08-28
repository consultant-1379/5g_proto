#
# COPYRIGHT ERICSSON GMBH 2021
#
# The copyright to the computer program(s) herein is the property
# of Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written
# permission of Ericsson GmbH in accordance with
# the terms and conditions stipulated in the agreement/contract
# under which the program(s) have been supplied.
#
# Created on: Jul 7, 2021
#     Author: emldpng
#

TOP := ../..
include $(TOP)/Makefile.config

BSF_LOAD_CHART_NAME := eric-bsf-load
RULESET := rulesets/ruleset2.0-eric-bsf-load.yaml

.PHONY: build clean deploy undeploy 

build:
	cd $(TOP); $(BOB) -r $(RULESET) init build image lint package push

deploy: build
	cd $(TOP); $(BOB) -r $(RULESET) deploy

undeploy:
	cd $(TOP); $(BOB) -r $(RULESET) undeploy

clean:
	cd $(TOP); $(BOB) -r $(RULESET) clean
