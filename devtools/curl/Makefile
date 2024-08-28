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
# Created on: Jun 03, 2018
#     Author: eedsvs
#

TOP := ../..
include $(TOP)/CONFIG
include ../CONFIG

.PHONY: all dev-env docker-build-images image package deploy clean clean-all clean-docker

all: image

clean-all: clean clean-docker

clean:
	rm -f *tgz

clean-docker:
	docker ps -a -q --filter ancestor=$(CURL_IMAGE_NAME) | while read a; do docker stop $$a && docker rm $$a; done
	docker image ls $(CURL_IMAGE_NAME) -q | while read a; do docker image rm $$a; done

image:
	@if [ $$(docker image ls $(CURL_IMAGE_NAME) | wc -l) -lt 2 ]; then										\
		echo 'Creating CURL image $(CURL_IMAGE_NAME)...';													\
		docker build -t $(CURL_IMAGE_NAME) .;																\
	else																									\
		echo 'CURL image $(CURL_IMAGE_NAME) found.';														\
	fi


