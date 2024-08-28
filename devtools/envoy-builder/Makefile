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
# Created on: May 22, 2019
#     Author: eedbjhe
#
#
#Changelog:
# 
# See README file

TOP := ../..
BOB := bob/bob
RULESET := ruleset2.0-eric-envoy.yaml

include $(TOP)/CONFIG
include $(TOP)/devtools/CONFIG
include $(TOP)/devenv/CONFIG


.PHONY: all image push-image

all: image push-image

image:
	cd $(TOP); $(BOB) -r $(RULESET) builder:build
	
image-libs:
	cd $(TOP); $(BOB) -r $(RULESET) builder-libs:build

push-image:
	cd $(TOP); $(BOB) -r $(RULESET) builder:push

push-image-libs:	
	cd $(TOP); $(BOB) -r $(RULESET) builder-libs:push

	
