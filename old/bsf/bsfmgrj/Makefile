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
# Created on: Jun 15, 2018
#     Author: eedsvs
#

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devenv/CONFIG
include $(TOP)/devtools/CONFIG

BUILD_LIBS := build/libs

JAR := bsfmgrj-$(VERSION).jar
JAR_TARGET := $(BUILD_LIBS)/$(JAR)

.PHONY: all image clean build-gradle dockerfile

all: image

image: $(JAR_TARGET) dockerfile
	docker build -t bsfmgrj .

dockerfile: Dockerfile.templ
	@cat Dockerfile.templ																				   			|\
	sed "s/\#\#\#JAVA_JRE_DOCKER_IMG\#\#\#/$$(echo '$(JAVA_JRE_DOCKER_IMG_SLIM)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"	|\
	sed "s/\#\#\#BUILD_LIBS\#\#\#/$$(echo '$(BUILD_LIBS)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"		 			|\
	sed "s/\#\#\#JAR\#\#\#/$$(echo '$(JAR)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"		 							 \
	> Dockerfile && echo "Dockerfile created."

$(JAR_TARGET): build-gradle
	gradle jar

clean:
	gradle clean
	rm -rf .gradle Dockerfile build.gradle.kts 

run:
	gradle run

jar:
	gradle jar

