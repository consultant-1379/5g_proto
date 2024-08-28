################################################################################
#
#  Author   : eustone
#
#  Revision : 2.11
#  Date     : 2024-02-21 16:02:53
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2024
#
# The copyright to the computer program(s) herein is the property of
# Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission
# of Ericsson GmbH in accordance with the terms and conditions stipulated in
# the agreement/contract under which the program(s) have been supplied.
#
################################################################################
#
# This makefile will/can:
# - create a release DAFT package (make daft)
# - include the current GIT version of DAFT in the DAFT packages directory
#   (make daft-new-package)
# - Set the current GIT version of DAFT as 'latest' in the DAFT packages directory
#   (make daft-update-latest)
# - clean up the working directory (make clean)
#
# -----------
# Macro Usage
# -----------
# DAFT_PKG_DIR=<path to DAFT package directory>
# DAFT_DIR=<path to DAFT parent/installation directory>
# DAFT_RELEASE_NAME=<release name e.g. NDP1.12
# DAFT_RELEASE_PRODUCT_ID=<product id e.g. _DAFT_CXP9036965_9_R1A_
# OUTPUTDIR=<path to where output files for DAFT is to be created>
####


TOP := ..
DAFT_RELEASE_NAME := "ESC1.15"
DAFT_RELEASE_PRODUCT_ID := "_DAFT_CXP9036965_99_R1A_"

DAFT_PKG_DIR := /proj/CBA_comp/esc/daft/daft_packages
DAFT_DIR := /proj/CBA_comp/esc/daft

ARTIFACTORY_URL := "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local"

# Makefile internal macros
# ------------------------
TMPDIR := $(shell mktemp -d)
DATE := $(shell date +'%Y%m%d_%H%M%S')
WORKINGDIR := $(shell pwd)
OUTPUTDIR := $(WORKINGDIR)


.PHONY : all daft daft-new-package daft-update-latest clean daft-artifactory-package

# Target definitions
# ------------------

all :
	make daft

#
# Create a new DAFT release package.
#
daft :
	perl/bin/create_daft_package.pl \
		--git-repository=../ \
		--esc-package \
		--timestamp=$(DATE) \
		--output-directory=$(OUTPUTDIR) \
		--release-name=$(DAFT_RELEASE_NAME) \
		--release-product-id=$(DAFT_RELEASE_PRODUCT_ID) \
		--release-package

#
# Create a new DAFT release package and upload it to artifactory.
#
daft-artifactory-package :
	make DAFT_RELEASE_NAME=$(DAFT_RELEASE_NAME) DAFT_RELEASE_PRODUCT_ID=$(DAFT_RELEASE_PRODUCT_ID) DATE=$(DATE) OUTPUTDIR=$(WORKINGDIR) daft
	[ -f $(WORKINGDIR)/$(DAFT_RELEASE_NAME)$(DAFT_RELEASE_PRODUCT_ID)$(DATE).tar.gz ] && \
		curl -f -k -H "X-JFrog-Art-Api:$(ARTIFACTORY_TOKEN)" --upload-file $(WORKINGDIR)/$(DAFT_RELEASE_NAME)$(DAFT_RELEASE_PRODUCT_ID)$(DATE).tar.gz $(ARTIFACTORY_URL)/esc-daft/$(DAFT_RELEASE_NAME)$(DAFT_RELEASE_PRODUCT_ID)$(DATE).tar.gz && \
		make clean

#
# Create a new DAFT release package and install it in the
# expected directory, normally /proj/CBA_comp/esm/daft/.
#
daft-new-package :
	make DAFT_RELEASE_NAME=$(DAFT_RELEASE_NAME) DAFT_RELEASE_PRODUCT_ID=$(DAFT_RELEASE_PRODUCT_ID) DATE=$(DATE) OUTPUTDIR=$(DAFT_PKG_DIR) daft
	[ -f $(DAFT_PKG_DIR)/DAFT_$(DATE).tar.gz ] && \
		chgrp sufuser $(DAFT_PKG_DIR)/DAFT_$(DATE).tar.gz && \
		chmod g+w $(DAFT_PKG_DIR)/DAFT_$(DATE).tar.gz
	[ -f $(DAFT_PKG_DIR)/$(DAFT_RELEASE_NAME)$(DAFT_RELEASE_PRODUCT_ID)$(DATE).tar.gz ] && \
		chgrp sufuser $(DAFT_PKG_DIR)/$(DAFT_RELEASE_NAME)$(DAFT_RELEASE_PRODUCT_ID)$(DATE).tar.gz && \
		chmod g+w $(DAFT_PKG_DIR)/$(DAFT_RELEASE_NAME)$(DAFT_RELEASE_PRODUCT_ID)$(DATE).tar.gz
	[ -f $(DAFT_PKG_DIR)/DAFT_$(DATE).tar.gz ] && \
		mkdir $(DAFT_DIR)/DAFT_$(DATE) && \
		tar xzf $(DAFT_PKG_DIR)/DAFT_$(DATE).tar.gz -C $(DAFT_DIR)/DAFT_$(DATE) && \
		chgrp -R sufuser $(DAFT_DIR)/DAFT_$(DATE) && \
		chmod -R g+w $(DAFT_DIR)/DAFT_$(DATE)

#
# Create a new DAFT release package and install it in the
# expected directory, normally /proj/CBA_comp/esm/daft/ and
# then update the 'latest' soft link to point to new package.
#
daft-update-latest :
	make DAFT_DIR=$(DAFT_DIR) DAFT_PKG_DIR=$(DAFT_PKG_DIR) DAFT_RELEASE_NAME=$(DAFT_RELEASE_NAME) DAFT_RELEASE_PRODUCT_ID=$(DAFT_RELEASE_PRODUCT_ID) DATE=$(DATE) OUTPUTDIR=$(DAFT_PKG_DIR) daft-new-package
	[ -d $(DAFT_DIR)/DAFT_$(DATE) ] && \
		rm -f $(DAFT_DIR)/latest_esc && \
		ln -s $(DAFT_DIR)/DAFT_$(DATE) $(DAFT_DIR)/latest_esc && \
		ls -l $(DAFT_DIR)/latest_esc

# Pack the daft/perl/bin/system_health_check.pl and daft/perl/lib/ files (except files under Playlists directory).
health-check-package :
	cd $(TOP)/daft && tar czf $(OUTPUTDIR)/health_check_package.tar.gz perl/bin/system_health_check.pl perl/bin/alarm_check.pl perl/bin/print_resource_statistics.pl perl/lib/ADP/* perl/lib/General/* perl/lib/Health_Checks/* perl/lib/YAML* perl/lib/boolean.* expect/* && echo "Created file: $(OUTPUTDIR)/health_check_package.tar.gz"

clean :
	rm -rf $(WORKINGDIR)/DAFT*.tar.gz
	rm -rf $(WORKINGDIR)/ESC*.tar.gz
	rm -rf $(OUTPUTDIR)/health_check_package.tar.gz
	rm -rf $(OUTPUTDIR)/network_config_files/old_*/
	rm -rf $(OUTPUTDIR)/network_config_files/status_*.txt
	rm -rf $(OUTPUTDIR)/status_*.txt
