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
# Created on: Jul 15, 2020
#     Author: emyldim
#

TOP := ../..
BOB := bob/bob
CHART_NAME := eric-log-transformer
RULESET := rulesets/ruleset2.0-eric-log-transformer.yaml
include $(TOP)/CONFIG
include $(TOP)/devenv/CONFIG
include $(TOP)/devtools/CONFIG

include $(TOP)/CONFIG

all: undeploy clean deploy

fetch:
	rm -rf $(TOP)/.bob/eric-log-transformer*.tgz
	cd $(TOP); $(BOB) -r $(RULESET) init fetch

deploy: fetch
	cd $(TOP); $(BOB) -r $(RULESET) deploy

upgrade: fetch
	$(HELM) upgrade $(CHART_NAME)-$(USER) $(TOP)/.bob/$(CHART_NAME)*.tgz

replace: clean fetch undeploy deploy

undeploy:
	cd $(TOP); $(BOB) -r $(RULESET) undeploy

clean:
	cd $(TOP); $(BOB) -r $(RULESET) clean
