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

TOP := ..
include $(TOP)/CONFIG
include $(TOP)/devenv/CONFIG
include CONFIG

SUBDIRS := java jq kubectl helm swagger go eclipse
SUBDIRS_CI := jq kubectl helm swagger python go

PROFILE := ~/.5g.devenv.profile
MANIFEST_TMP := $(shell echo /tmp/MANIFEST.$$$$.tmp)

.PHONY: all all-cicd clean clean-all $(SUBDIRS) profile manifest

all: $(SUBDIRS) profile MANIFEST

all-cicd: $(SUBDIRS_CI) profile-ci MANIFEST-cicd

$(SUBDIRS):
	$(MAKE) -C $@

$(SUBDIRS_CI):
	$(MAKE) -C $@ all-cicd

clean: makesub.clean

clean-all: makesub.clean-all

makesub.%:
	@for a in $(SUBDIRS); do 		\
		$(MAKE) -C $$a $*;			\
	done

%: makesub.%
	@echo "Made target '$@' for subdirs: $(SUBDIRS)."

profile:
	@echo								 > $(PROFILE)
	@echo "# DSC Evo Dev Env Profile" 	>> $(PROFILE)
	@for a in $(SUBDIRS); do 								\
		$(MAKE) -C $$a profile PROFILE=$(PROFILE);			\
	done
	@echo								>> $(PROFILE)
	@echo
	@echo "   >>>   Note to add the line 'source $(PROFILE)' to your ~/.bashrc   <<<"
	@echo

profile-ci:
	@echo								 > $(PROFILE)
	@echo "# DSC Evo Dev Env Profile" 	>> $(PROFILE)
	@for a in $(SUBDIRS_CI); do 								\
		$(MAKE) -C $$a profile PROFILE=$(PROFILE);			\
	done
	@echo								>> $(PROFILE)
	@echo
	@echo "   >>>   Note to add the line 'source $(PROFILE)' to your ~/.bashrc   <<<"
	@echo

manifest:
	@for a in $(SUBDIRS); do 								\
		$(MAKE) -C $$a manifest MANIFEST=$(MANIFEST_TMP);	\
	done
	@cat $(MANIFEST_TMP)							> MANIFEST
	@rm -f $(MANIFEST_TMP)

manifest-ci:
	@for a in $(SUBDIRS_CI); do 								\
		$(MAKE) -C $$a manifest MANIFEST=$(MANIFEST_TMP);	\
	done
	@cat $(MANIFEST_TMP)							> MANIFEST
	@rm -f $(MANIFEST_TMP)
