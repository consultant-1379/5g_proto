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

install: $(INTELLIJ)

$(INTELLIJ): $(INTELLIJ_TGZ)
	@sudo -p "Enter password for %u to install IntelliJ ($(INTELLIJ_VERSION)) onto your machine: " tar -C $(INTELLIJ_INSTALL_DIR) -xzf $(INTELLIJ_TGZ)

# removes IntelliJ from your machine !
uninstall:
	@sudo -k -p "Enter password for %u if you want to remove IntelliJ ($(INTELLIJ_VERSION)) from your machine: " rm -rf $(INTELLIJ_HOME)

$(INTELLIJ_TGZ):
	wget $(INTELLIJ_DOWNLOAD_URL)

# removes everything including your local IntelliJ config
clean-all: clean uninstall
	rm -rf ~/.IdeaIC*

clean:
	rm -f *.tgz *.tar.gz

profile:
	@echo 											>> $(PROFILE)
	@echo "# IntelliJ"				 				>> $(PROFILE)
	@echo 'PATH=$$PATH:$(INTELLIJ_HOME)/bin:'		>> $(PROFILE)
	@echo "alias intellij=$(notdir $(INTELLIJ))"	>> $(PROFILE)

manifest:
	@echo -e "$$(echo $(INTELLIJ_VERSION) | sha256sum | cut -f 1 -d ' ')\tt\tintellij\tIntelliJ ($(INTELLIJ_VERSION))" >> $(MANIFEST)


