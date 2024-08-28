# COPYRIGHT ERICSSON GMBH 2022
#
# The copyright to the computer program(s) herein is the property
# of Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written
# permission of Ericsson GmbH in accordance with
# the terms and conditions stipulated in the agreement/contract
# under which the program(s) have been supplied.
#
# Created on: Jan 12, 2022
#     Author: eaoknkr
#

TOP := ../..
include $(TOP)/Makefile.config

RULESET := rulesets/ruleset2.0-eric-supreme.yaml

.PHONY: all push publish clean

all:
	@if [ ! -d "$(TOP)/.bob" ]; then cd $(TOP); $(BOB) -r $(RULESET) init; fi
	cd $(TOP); $(BOB) -r $(RULESET) build

push:
	cd $(TOP); $(BOB) -r $(RULESET) push-image

publish: push
	cd $(TOP); cp devtools/supreme/target/supreme-1.0.22-shaded.jar /proj/sc-tools/bin/supreme/supreme.jar
	cd $(TOP); cp devtools/supreme/src/main/resources/properties.yaml /proj/sc-tools/bin/supreme/
	cd $(TOP); cp devtools/supreme/src/main/resources/simple_prop3.yaml /proj/sc-tools/bin/supreme/

clean:
	@if [ ! -d "$(TOP)/.bob" ]; then cd $(TOP); $(BOB) -r $(RULESET) init; fi
	cd $(TOP); $(BOB) -r $(RULESET) clean
