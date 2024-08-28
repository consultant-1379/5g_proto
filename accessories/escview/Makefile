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

# general Java defs
JAVA_INSTALL_DIR := $(STANDARD_INSTALL_DIR)
JAVA_VERSION_NUM := 11.0.1
JAVA_VERSION := jdk-$(JAVA_VERSION_NUM)
JAVA_HOME := $(JAVA_INSTALL_DIR)/$(JAVA_VERSION)
JAVA := $(JAVA_HOME)/bin/java
JAVAC := $(JAVA_HOME)/bin/javac
JAVA_TGZ := openjdk-$(JAVA_VERSION_NUM)_linux-x64_bin.tar.gz
JAVA_DOWNLOAD_URL := https://download.java.net/java/GA/jdk11/13/GPL/$(JAVA_TGZ)


.PHONY: all clean-all clean undeploy profile manifest

all: run

all-cicd:

install-java: $(JAVA)

# removes java from your machine !
uninstall-java:
	@sudo --reset-timestamp --prompt="Enter password for %u if you want to remove Java $(JAVA_VERSION) from your machine: " rm -rf $(JAVA_HOME)

$(JAVAC): $(JAVA_TGZ)
	@[ -d $(JAVA_HOME) ] || sudo -p "Enter password for %u to install Java onto your machine: " tar -C $(JAVA_INSTALL_DIR) -xzf $(JAVA_TGZ)
	@$(JAVA) --version

$(JAVA_TGZ):
	[ -d $(JAVA_HOME) ] || wget $(JAVA_DOWNLOAD_URL)

clean-all: clean undeploy

clean:
	# rm -f *.tgz *.tar.gz
	mvn clean

profile:
	@echo 											>> $(PROFILE)
	@echo "# java"				 					>> $(PROFILE)
	@echo 'JAVA_HOME=$(JAVA_HOME)'					>> $(PROFILE)
	@echo 'PATH=$$JAVA_HOME/bin:$$PATH'				>> $(PROFILE)
	@echo 'MANPATH=$$MANPATH:$$JAVA_HOME/man'		>> $(PROFILE)

manifest:
	@echo -e "$$(sha256sum $(JAVA) | cut -f 1 -d ' ')\tt\tjava\t$$($(JAVA) --version | head -n1)" >> $(MANIFEST)


# args: (1) host, (2) port#, (3) number of bar charts (default: 8)
run: $(JAVAC)
	JAVA_HOME=$(JAVA_HOME) mvn compile exec:java -Dexec.args="10.210.52.30 31788 8"
