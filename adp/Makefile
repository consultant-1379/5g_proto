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

TOP := ../
include $(TOP)/CONFIG

# leave adp-gs-cm and eric-fh-alarm-handler out for now, until message bus comes up faster
SUBDIRS := eric-data-document-database-pg eric-cm-mediator eric-data-wide-column-database-cd eric-fh-alarm-handler eric-data-coordinator-zk eric-data-message-bus-kf eric-pm-server 

.PHONY: all clean clean-all version $(SUBDIRS)

all: $(SUBDIRS)

deploy-all-demo: all-demo

all-demo:
	make -C eric-data-document-database-pg deploy-demo
	sleep 5
	make -C eric-cm-mediator deploy-demo
	sleep 5
	make -C eric-data-coordinator-zk deploy-demo
	sleep 5
	make -C eric-data-message-bus-kf deploy-demo
	sleep 140
	make -C eric-fh-alarm-handler all-demo
	make -C eric-data-wide-column-database-cd deploy-demo
	make -C eric-pm-server deploy-demo
	make -C ../simulators/nrfsim deploy.demo
	make -C external/grafana deploy-demo	

undeploy-all-demo:
	make -C eric-data-document-database-pg undeploy-demo
	make -C eric-cm-mediator undeploy-demo
	make -C eric-data-coordinator-zk undeploy-demo
	make -C eric-data-message-bus-kf undeploy-demo
	make -C eric-fh-alarm-handler undeploy-demo
	make -C eric-data-wide-column-database-cd undeploy-demo
	make -C eric-pm-server undeploy-demo
	make -C ../simulators/nrfsim undeploy.demo
	make -C external/grafana undeploy-demo

$(SUBDIRS):
	$(MAKE) -C $@

clean: makesub.clean

clean-all: makesub.clean-all

version: makesub.version

test: cm-mediator
	@echo $(REV)

cm-mediator:
	#COMP = "CM Mediator"
	@$(eval REV=`ls`)
	#@echo $(REV)

makesub.%:
	@for a in $(SUBDIRS); do 				\
		$(MAKE) -C $$a $*;					\
	done
