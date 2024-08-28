# vim:ts=4:sw=4:sw=4:et
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
# Created on: May 24, 2021
#     Author: emavefs
#

TOP := ../..
IS_DOCKERHUB_LOGGEDIN := $(shell `cat ~/.docker/config.json | jq -r --arg url "https://index.docker.io/v1/" '.auths | has($$url)'` 1>/dev/null 2>&1 && echo "yes" || echo "no")


push-all: push-certificates-builder push-boringssl-builder

check-dockerhub:
ifeq ($(IS_DOCKERHUB_LOGGEDIN),yes)
	@echo "You are logged in dockerhub, continue..."
else
	@echo "Login to dockerhub first! ";
	exit 1
endif

# Tools
push-certificates-builder:
	cd ./certificates-builder; \
	docker build -t armdocker.rnd.ericsson.se/proj-5g-bsf/certificates-builder:1.0 . ; \
	docker push armdocker.rnd.ericsson.se/proj-5g-bsf/certificates-builder:1.0


push-boringssl-builder:
	cd boringssl-builder; \
	docker build -t armdocker.rnd.ericsson.se/proj-5g-bsf/boringssl-builder:1.0.0 . ; \
	docker push armdocker.rnd.ericsson.se/proj-5g-bsf/boringssl-builder:1.0.0

push-python3-builder:
	cd ./python3-builder; \
	docker build -t armdocker.rnd.ericsson.se/proj-5g-bsf/python3-builder:1.0.0 . ; \
	docker push armdocker.rnd.ericsson.se/proj-5g-bsf/python3-builder:1.0.0

