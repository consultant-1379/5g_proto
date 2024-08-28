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
# Created on: Sep 12, 2018
#     Author: eedbjhe
#

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devtools/CONFIG
include $(TOP)/devenv/CONFIG

.PHONY: all clean-all clean undeploy profile manifest

all: install

install: $(ECLIPSE)

$(ECLIPSE): $(ECLIPSE_TGZ)
	@sudo -p "Enter password for %u to install Eclipse ($(ECLIPSE_VERSION)) onto your machine: " sudo tar -C $(ECLIPSE_INSTALL_DIR) -xzf $(ECLIPSE_TGZ)

# removes Eclipse from your machine !
uninstall:
	@sudo -k -p "Enter password for %u if you want to remove Eclipse ($(ECLIPSE_VERSION)) from your machine: " sudo rm -rf $(ECLIPSE_HOME)

$(ECLIPSE_TGZ):
	wget $(ECLIPSE_DOWNLOAD_URL)

# removes everything including your local IntelliJ config
clean-all: clean uninstall
	rm -rf ~/.eclipse*

clean:
	rm -f *.tgz *.tar.gz

profile:
	@echo 											>> $(PROFILE)
	@echo "# Eclipse"				 				>> $(PROFILE)
	@echo 'PATH=$$PATH:$(ECLIPSE_HOME)/:'		    >> $(PROFILE)
	@echo "alias eclipse=$(notdir $(ECLIPSE))"	    >> $(PROFILE)

manifest:
	@echo -e "$$(echo $(ECLIPSE_VERSION) | sha256sum | cut -f 1 -d ' ')\tt\teclipse\tEclipse ($(ECLIPSE_VERSION))" >> $(MANIFEST)


