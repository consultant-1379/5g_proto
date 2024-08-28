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
# Created on: Jul 18, 2018
#     Author: xkorpap
#

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devtools/CONFIG
include $(TOP)/devenv/CONFIG

.PHONY: all all-cicd clean binary manifest

all: binary

all-cicd: all

clean:
	rm -f jq-linux64*

binary: clean
	wget $(JQ_DOWNLOAD_URL)
	chmod +x ./jq-linux64
	sudo -p "Enter password for %u to install jq4 onto your machine: " sudo mv ./jq-linux64 /usr/local/bin/jq

uninstall:
	sudo -p "Enter password for %u to remove jq from your machine: " sudo rm -f /usr/local/bin/jq

# no need for versioning jq as it has no influence on any of the deliveries
manifest:
	@# do nothing