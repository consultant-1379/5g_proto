# vim:ts=4:sw=4:sw=4:et
#
# COPYRIGHT ERICSSON GMBH 2018
#
# The copyright to the computer program(s) herein is the property
# of Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written
# permission of Ericsson GmbH in accordance with
# the terms and conditions stipulated in the agreement/contract
# under which the program(s) have been supplied.
#
# Created on: Jun 07, 2018
#     Author: eedsvs
#

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devenv/CONFIG

SUBDIRS := helm

.PHONY: all fetch-erikube install-erikube deinstall-erikube clean

all: fetch-erikube install-erikube

fetch-erikube:
	@echo "NOT SUPPORTED - CAN'T CLONE INTO CLONED DIRECTORY STRUCTURE"
	# git clone ssh://$(DEV_USER)@gerrit.ericsson.se:29418/erikube/erikube
	# git fetch ssh://$(DEV_USER)@gerrit.ericsson.se:29418/erikube/erikube refs/changes/17/3756017/3
	# git checkout FETCH_HEAD

install-erikube:
	cd erikube/development && ./deploy-local-host.sh

deinstall-erikube:
	@echo "Deletion not supported by Erikube distro yet!" > /dev/stderr && false

clean:
	rm -rf erikube

