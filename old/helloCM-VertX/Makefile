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
TOP := ../
include $(TOP)/CONFIG
include $(TOP)/devenv/CONFIG
include $(TOP)/devtools/CONFIG

TARGET := helloCM-VertX
VERSION := 0.0.1-1

TARGET_TO_LWR := $(shell tr '[:upper:]' '[:lower:]' <<< $(TARGET))

BUILD_LIBS := build/libs

JAR := $(TARGET)-$(VERSION).jar
JAR_TARGET := $(BUILD_LIBS)/$(JAR)

CM_RAML_LOC := src/main/java/com/ericsson/esc/bsf/services/cm/adp

.PHONY: all image clean gen-gradle gen-dockerfile

all: image

image: $(JAR_TARGET) gen-dockerfile
	docker build -t $(TARGET_TO_LWR) .

gen-dockerfile: Dockerfile.templ
	@cat Dockerfile.templ																				   			|\
	sed "s/\#\#\#JAVA_JRE_DOCKER_IMG\#\#\#/$$(echo '$(JAVA_JRE_DOCKER_IMG_SLIM)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"	|\
	sed "s/\#\#\#BUILD_LIBS\#\#\#/$$(echo '$(BUILD_LIBS)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"		 			|\
	sed "s/\#\#\#JAR\#\#\#/$$(echo '$(JAR)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"		 							 \
	> Dockerfile && echo "Dockerfile created."

gen-gradle: build.gradle.kts.templ
	@cat build.gradle.kts.templ																				   			|\
	sed "s/\#\#\#VERSION\#\#\#/$$(echo '$(JAVA_JRE_DOCKER_IMG_SLIM)' | sed -e 's/\([[\/.*]\|\]\)/\\&/g')/g"	\
	> build.gradle.kts && echo "build.gradle.kts created."

$(JAR_TARGET): gen-gradle $(CM_RAML_LOC)/raml
	gradle jar

$(CM_RAML_LOC)/raml: cm-mediator_raml_1.1_D.tgz
	tar -C $(CM_RAML_LOC) -xvzf cm-mediator_raml_1.1_D.tgz
	rm -f cm-mediator_raml_1.1_D.tgz

cm-mediator_raml_1.1_D.tgz:
	wget https://cdu-vas.rnd.ki.sw.ericsson.se/vasDeliveries/CM/CAF-1.1.0/API/raml/cm-mediator_raml_1.1_D.tgz

clean:
	gradle clean
	rm -rf Dockerfile build.gradle.kts cm-mediator_raml_1.1_D.tgz $(CM_RAML_LOC)/raml
