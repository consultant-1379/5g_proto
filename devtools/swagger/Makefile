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
# Created on: Aug 07, 2018
#     Author: eedsvs
#

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devtools/CONFIG
include $(TOP)/devenv/CONFIG

.PHONY: all clean-all clean undeploy profile manifest

all: install

all-cicd: all

install: $(SWAGGER)

$(SWAGGER): $(SWAGGER_HOME)/$(SWAGGER_JAR) swagger
	sudo cp swagger $(SWAGGER)
	sudo chmod +x $(SWAGGER)

swagger:
	echo '#!/bin/sh'			 					 	 > swagger
	echo 'java -jar $(SWAGGER_HOME)/$(SWAGGER_JAR) $$*'	>> swagger

$(SWAGGER_HOME)/$(SWAGGER_JAR): $(SWAGGER_HOME) $(SWAGGER_JAR)
	sudo cp $(SWAGGER_JAR) $(SWAGGER_HOME)

$(SWAGGER_HOME):
	@sudo -p "Enter password for %u if you want to install Swagger $(SWAGGER_VERSION) to your machine: " mkdir -p $(SWAGGER_HOME)/bin

$(SWAGGER_JAR):
	wget $(SWAGGER_DOWNLOAD_URL) -O $(SWAGGER_JAR)

# removes swagger from your machine !
uninstall:
	@sudo -k -p "Enter password for %u if you want to remove Swagger $(SWAGGER_VERSION) from your machine: " rm -rf $(SWAGGER_HOME)

clean-all: clean undeploy

clean:
	rm -f *.jar swagger

profile:
	@echo 											>> $(PROFILE)
	@echo "# swagger"			 					>> $(PROFILE)
	@echo 'PATH=$(SWAGGER_BIN):$$PATH'				>> $(PROFILE)

manifest:
	@echo -e "$$(sha256sum $(SWAGGER_HOME)/$(SWAGGER_JAR) | cut -f 1 -d ' ')\tt\tswagger\t$$($(SWAGGER) version)" >> $(MANIFEST)
