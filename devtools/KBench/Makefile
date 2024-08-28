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
# Created on: May 10, 2021
#     Author: eedmiz
#

TOP := ../..
#include $(TOP)/CONFIG
#include $(TOP)/devtools/CONFIG
#include $(TOP)/devenv/CONFIG
KB_DOWNLOADLINK:=https://github.com/vmware-tanzu/k-bench/archive/refs/heads/master.zip

.PHONY: all all-cicd clean install manifest

all: install

all-cicd: all

#clean:
#	???

install:
	@echo "Installing k-bench package in folder '~/KBench'..." 
	@sudo -k -p "Enter password for %u if you want to continue: " sleep 1; 
	wget $(KB_DOWNLOADLINK) -O ./k-bench-master.zip
	unzip -u k-bench-master.zip
	mv k-bench-master k-bench
	./install.sh ~
	rm -rf k-bench
	rm -f k-bench-master.zip
	@echo "Instalaltion completed!"

rebuild:
	./rebuild.sh ~
	@echo "Re-instalaltion completed!"

profile:
	@echo "Profile update: Nothing to do here."	
	
uninstall:
	@echo "Warning!!!";\
	echo -n "Are you sure you want to delete folder ~/KBench [y/n]? ";\
	read reply;\
	if [[ ! $$reply =~ ^[Yy] ]]; then \
	    echo "Aborting..."; \
        exit 1; \
    fi
	@sudo -k -p "Enter password for %u if you want to continue: " sleep 1;
	chmod -R 777 ~/KBench;
	rm -rf ~/KBench;
	sudo rm -f /usr/local/bin/kbench;

# no need for versioning jq as it has no influence on any of the deliveries
manifest:
	@# do nothing
