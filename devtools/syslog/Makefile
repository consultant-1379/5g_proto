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
#     Author: evouioa
#

TOP := ../..
BOB := bob/bob
CHART_NAME := eric-syslog
RULESET := rulesets/ruleset2.0-eric-syslog.yaml
include $(TOP)/CONFIG
include $(TOP)/devenv/CONFIG
include $(TOP)/devtools/CONFIG

include $(TOP)/CONFIG

all: undeploy clean deploy

build:
	rm -rf $(TOP)/.bob/eric-syslog*.tgz	
	cd $(TOP); $(BOB) -r $(RULESET) init image package push
	#cd $(TOP)/scripts/; ./install_certs.sh syslog

deploy: deploy-ipv4-tls

deploy-ipv4-tls: build	
	cd $(TOP); $(BOB) -r $(RULESET) deploy:ipv4-tls
	
deploy-ipv6-tls: build	
	cd $(TOP); $(BOB) -r $(RULESET) deploy:ipv6-tls
	
deploy-ipv4-cleartext: build	
	cd $(TOP); $(BOB) -r $(RULESET) deploy:ipv4-cleartext
	
deploy-ipv6-cleartext: build	
	cd $(TOP); $(BOB) -r $(RULESET) deploy:ipv6-cleartext

export:
	cd $(TOP); $(BOB) -r $(RULESET) init build image package #Pushing the image is not necessary in this case
	cd $(TOP); $(BOB) -r $(RULESET) export

upgrade: build
	$(HELM) upgrade $(CHART_NAME)-$(USER) $(TOP)/.bob/$(CHART_NAME)*.tgz

replace: clean build undeploy deploy

undeploy:
	cd $(TOP); $(BOB) -r $(RULESET) undeploy

clean:
	cd $(TOP); $(BOB) -r $(RULESET) clean
