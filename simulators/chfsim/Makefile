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

CHFSIM_CHART_NAME := eric-chfsim
REDIS_CHART_NAME := eric-chfsim-redis
RULESET := rulesets/ruleset2.0-eric-chfsim.yaml

.PHONY: build build-chfsim build-chfsim-full build-chfsim-sepp build-full build-redis build-sepp \
        clean clean-chfsim clean-redis deploy deploy-chfsim deploy-chfsim-full deploy-chfsim-sepp \
        deploy-full deploy-redis deploy-sepp export redis-base-image replace undeploy undeploy-chfsim \
        undeploy-redis upgrade upgrade-chfsim upgrade-redis

# chfsim-1, -2, 3
build: build-chfsim build-redis

build-chfsim: 
	cd $(TOP); $(BOB) -r $(RULESET) init build image:build-eric-chfsim package:copy-helm-chart-chfsim tools:remove-deployments package:helm-chart-chfsim push:docker-image-chfsim push:helm-chart-chfsim
	
build-chfsim-full:
	cd $(TOP); $(BOB) -r $(RULESET) init build image:build-eric-chfsim package:copy-helm-chart-chfsim tools:remove-deployments-non-stability package:helm-chart-chfsim push:docker-image-chfsim push:helm-chart-chfsim	

build-chfsim-full-stability:
	cd $(TOP); $(BOB) -r $(RULESET) init build image:build-eric-chfsim package:copy-helm-chart-chfsim package:helm-chart-chfsim push:docker-image-chfsim push:helm-chart-chfsim	

build-chfsim-sepp:
	cd $(TOP); $(BOB) -r $(RULESET) init build image:build-eric-chfsim package:copy-helm-chart-chfsim-sepp package:helm-chart-chfsim push:docker-image-chfsim push:helm-chart-chfsim	

# chfsim-1, -2, -3, -4, -5, -6, -7, -8
build-full: build-chfsim-full build-redis

# # chfsim-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12
build-full-stability: build-chfsim-full-stability build-redis

build-redis:
	cd $(TOP); $(BOB) -r $(RULESET) init image:build-eric-chfsim-redis package:helm-chart-redis push:docker-image-redis push:helm-chart-redis

# chfsim-1-mnc-123-mcc-123, -2-mnc-123-mcc-123,
# chfsim-3-mnc-456-mcc-456, -4-mnc-456-mcc-456, -5-mnc-456-mcc-456, -6-mnc-456-mcc-456, -7-mnc-456-mcc-456, -8-mnc-456-mcc-456
build-sepp: build-chfsim-sepp build-redis

clean:
	cd $(TOP); $(BOB) -r $(RULESET) clean

clean-chfsim:
	cd $(TOP); $(BOB) -r $(RULESET) base.init:create-output-dir base.init:set-build-proxy clean:maven clean:chfsim-certificates clean:chfsim-helm clean:chfsim-version
	
clean-redis:
	cd $(TOP); $(BOB) -r $(RULESET) clean:redis-helm clean:redis-version

deploy: build deploy-chfsim deploy-redis

deploy-chfsim: build-chfsim
	cd $(TOP); $(BOB) -r $(RULESET_SC) install-certificates:chfsim
	cd $(TOP); $(BOB) -r $(RULESET) deploy:deploy-chfsim

deploy-chfsim-full: build-chfsim-full
	cd $(TOP); $(BOB) -r $(RULESET_SC) install-certificates:chfsim
	cd $(TOP); $(BOB) -r $(RULESET) deploy:deploy-chfsim

deploy-chfsim-full-stability: build-chfsim-full-stability
	cd $(TOP); $(BOB) -r $(RULESET_SC) install-certificates:chfsim
	cd $(TOP); $(BOB) -r $(RULESET) deploy:deploy-chfsim
	
deploy-chfsim-sepp: build-chfsim-sepp
	cd $(TOP); $(BOB) -r $(RULESET_SC) install-certificates:chfsim-sepp
	cd $(TOP); $(BOB) -r $(RULESET) deploy:deploy-chfsim

deploy-full: build-full deploy-chfsim-full deploy-redis

deploy-full-stability: build-full-stability deploy-chfsim-full-stability deploy-redis

deploy-redis: build-redis	
	cd $(TOP); $(BOB) -r $(RULESET) deploy:deploy-redis

deploy-sepp: build-sepp deploy-chfsim-sepp deploy-redis

export:
	cd $(TOP); $(BOB) -r $(RULESET) init build image:build-eric-chfsim package:copy-helm-chart-chfsim package:helm-chart-chfsim
	cd $(TOP); $(BOB) -r $(RULESET) image:build-eric-chfsim-redis package:helm-chart-redis
	cd $(TOP); $(BOB) -r $(RULESET) export

redis-base-image:
	cd $(TOP); $(BOB) -r $(RULESET) redis-base-image

replace: build undeploy deploy

undeploy: undeploy-chfsim undeploy-redis
	
undeploy-chfsim:	
	cd $(TOP); $(BOB) -r $(RULESET) undeploy:undeploy-chfsim

undeploy-redis:	
	cd $(TOP); $(BOB) -r $(RULESET) undeploy:undeploy-redis

upgrade: upgrade-chfsim upgrade-redis
	
upgrade-chfsim: build-chfsim
		$(HELM) upgrade --namespace=$(KUBE_NAMESPACE) $(CHFSIM_CHART_NAME)-$(USER) $(TOP)/.bob/$(CHFSIM_CHART_NAME)-$(shell cat $(TOP)/.bob/var.chfsim-version).tgz

upgrade-redis: build-redis
		$(HELM) upgrade --namespace=$(KUBE_NAMESPACE) $(REDIS_CHART_NAME)-$(USER) $(TOP)/.bob/$(REDIS_CHART_NAME)-$(shell cat $(TOP)/.bob/var.redis-version).tgz
