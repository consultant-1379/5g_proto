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
# Created on: Jan 19, 2020
#     Author: ekoteva
#

TOP := ../..
BOB := bob/bob
BOB_OUTDIR := ../.bob
REFERRAL_LDAP_NAME := eric-referral-ldap
RULESET := rulesets/ruleset2.0-eric-referral-ldap.yaml
include $(TOP)/CONFIG
include $(TOP)/devenv/CONFIG
include $(TOP)/devtools/CONFIG

include $(TOP)/CONFIG

build: clean build-referral-ldap

build-referral-ldap:
	cd $(TOP); $(BOB) -r $(RULESET) init package:helm-chart-referral-ldap push:helm-chart-referral-ldap

deploy: clean deploy-referral-ldap

deploy-tls: clean deploy-referral-ldap-tls
	
deploy-referral-ldap: build-referral-ldap
	cd $(TOP); $(BOB) -r $(RULESET) deploy:deploy-referral-ldap
	
deploy-referral-ldap-tls: build-referral-ldap
	cd $(TOP); $(BOB) -r $(RULESET) deploy:deploy-referral-ldap-tls

upgrade: build
	$(HELM) upgrade $(REFERRAL_LDAP_NAME)-$(USER) $(TOP)/.bob/$(OPENLDAP_NAME)*.tgz

replace: clean build undeploy deploy

export: build
	cd $(TOP); $(BOB) -r $(RULESET) export

undeploy: undeploy-referral-ldap

undeploy-referral-ldap:
	cd $(TOP); $(BOB) -r $(RULESET) undeploy:undeploy-referral-ldap

clean: clean-referral-ldap

clean-referral-ldap:
	@if [ -a $(TOP)/.bob/$(REFERRAL_LDAP_NAME)*.tgz ]; then rm -f $(TOP)/.bob/$(REFERRAL_LDAP_NAME)*.tgz; fi;
