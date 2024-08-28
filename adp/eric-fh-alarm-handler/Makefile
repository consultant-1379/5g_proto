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
# Created on: Jun 26, 2018
#     Author: xkorpap
#

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devtools/CONFIG
include $(TOP)/devenv/CONFIG

ADP_NAME := eric-fh-alarm-handler
ADP_VER := 1.4.0+126

KF_POD_NAME := $(shell $(KUBECTL) --namespace $(KUBE_NAMESPACE) get pods | grep message-bus | grep Running | head -1 | awk '{print $$1}')
KF_POD_NAME_DEFAULT := $(shell $(KUBECTL) --namespace default get pods | grep message-bus | grep Running | head -1 | awk '{print $$1}')
KF_POD_NAME_DEMO := $(shell $(KUBECTL) --namespace $(DEMO_NAMESPACE) get pods | grep message-bus | grep Running | head -1 | awk '{print $$1}')
ALARM_SERVICE_NAME := bsf
ALARM_STATE_TOPIC := AdpAlarmStateIndication
FAULT_TOPIC := AdpFaultIndication

USERNAME := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ADP_REPO_BASE) -u)
PASSWORD := $(shell python $(TOP)/scripts/helm_repositories.py --repository $(ADP_REPO_BASE) -p)

.PHONY: all getkfpodname createkftopics configurekftopics faultmappings deploy undeploy clean clean-all

all: createkftopics configurekftopics faultmappings deploy

all-default: createkftopics-default configurekftopics-default faultmappings-default deploy-default

all-demo: createkftopics-demo configurekftopics-demo faultmappings-demo deploy-demo

getkfpodname: 
	@echo "$(KF_POD_NAME)"

getkfpodname-default: 
	@echo "$(KF_POD_NAME_DEFAULT)"
	
getkfpodname-demo: 
	@echo "$(KF_POD_NAME_DEMO)"	

createkftopics: 
	$(KUBECTL) -n $(KUBE_NAMESPACE) exec $(KF_POD_NAME) -- /usr/bin/kafka-topics --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --topic $(FAULT_TOPIC) --create --partitions 3 --replication-factor 1
	$(KUBECTL) -n $(KUBE_NAMESPACE) exec $(KF_POD_NAME) -- /usr/bin/kafka-topics --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --topic $(ALARM_STATE_TOPIC) --create --partitions 1 --replication-factor 1
	
createkftopics-default: 
	$(KUBECTL) -n default exec $(KF_POD_NAME_DEFAULT) -- /usr/bin/kafka-topics --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --topic $(FAULT_TOPIC) --create --partitions 3 --replication-factor 1
	$(KUBECTL) -n default exec $(KF_POD_NAME_DEFAULT) -- /usr/bin/kafka-topics --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --topic $(ALARM_STATE_TOPIC) --create --partitions 1 --replication-factor 1

createkftopics-demo: 
	$(KUBECTL) -n $(DEMO_NAMESPACE) exec $(KF_POD_NAME_DEMO) -- /usr/bin/kafka-topics --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --topic $(FAULT_TOPIC) --create --partitions 3 --replication-factor 1
	$(KUBECTL) -n $(DEMO_NAMESPACE) exec $(KF_POD_NAME_DEMO) -- /usr/bin/kafka-topics --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --topic $(ALARM_STATE_TOPIC) --create --partitions 1 --replication-factor 1

configurekftopics: 
	$(KUBECTL) -n $(KUBE_NAMESPACE) exec $(KF_POD_NAME) -- /usr/bin/kafka-configs --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --alter --entity-type topics --entity-name $(FAULT_TOPIC) --add-config retention.ms=172800000
	$(KUBECTL) -n $(KUBE_NAMESPACE) exec $(KF_POD_NAME) -- /usr/bin/kafka-configs --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --alter --entity-type topics --entity-name $(ALARM_STATE_TOPIC) --add-config retention.ms=172800000

configurekftopics-default: 
	$(KUBECTL) -n default exec $(KF_POD_NAME_DEFAULT) -- /usr/bin/kafka-configs --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --alter --entity-type topics --entity-name $(FAULT_TOPIC) --add-config retention.ms=172800000
	$(KUBECTL) -n default exec $(KF_POD_NAME_DEFAULT) -- /usr/bin/kafka-configs --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --alter --entity-type topics --entity-name $(ALARM_STATE_TOPIC) --add-config retention.ms=172800000

configurekftopics-demo: 
	$(KUBECTL) -n $(DEMO_NAMESPACE) exec $(KF_POD_NAME_DEMO) -- /usr/bin/kafka-configs --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --alter --entity-type topics --entity-name $(FAULT_TOPIC) --add-config retention.ms=172800000
	$(KUBECTL) -n $(DEMO_NAMESPACE) exec $(KF_POD_NAME_DEMO) -- /usr/bin/kafka-configs --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --alter --entity-type topics --entity-name $(ALARM_STATE_TOPIC) --add-config retention.ms=172800000

deletekftopics: 
	$(KUBECTL) -n $(KUBE_NAMESPACE) exec $(KF_POD_NAME) -- /usr/bin/kafka-topics --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --delete --topic $(FAULT_TOPIC)
	$(KUBECTL) -n $(KUBE_NAMESPACE) exec $(KF_POD_NAME) -- /usr/bin/kafka-topics --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --delete --topic $(ALARM_STATE_TOPIC)
	
deletekftopics-default: 
	$(KUBECTL) -n default exec $(KF_POD_NAME_DEFAULT) -- /usr/bin/kafka-topics --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --delete --topic $(FAULT_TOPIC)
	$(KUBECTL) -n default exec $(KF_POD_NAME_DEFAULT) -- /usr/bin/kafka-topics --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --delete --topic $(ALARM_STATE_TOPIC)	

deletekftopics-demo: 
	$(KUBECTL) -n $(DEMO_NAMESPACE) exec $(KF_POD_NAME_DEMO) -- /usr/bin/kafka-topics --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --delete --topic $(FAULT_TOPIC)
	$(KUBECTL) -n $(DEMO_NAMESPACE) exec $(KF_POD_NAME_DEMO) -- /usr/bin/kafka-topics --zookeeper eric-data-coordinator-zk:2181/eric-data-message-bus-kf --delete --topic $(ALARM_STATE_TOPIC)	

faultmappings: configmap
	$(KUBECTL) -n $(KUBE_NAMESPACE) create configmap faultmappings --from-file=./faultmappings
	rm -rf faultmappings/
	
faultmappings-default: configmap
	$(KUBECTL) -n default create configmap faultmappings --from-file=./faultmappings
	rm -rf faultmappings/	
	
faultmappings-demo: configmap
	$(KUBECTL) -n $(DEMO_NAMESPACE) create configmap faultmappings --from-file=./faultmappings
	rm -rf faultmappings/		

configmap:    
	@mkdir -p ./faultmappings
	@echo -e '[\n{ "faultName": "Test", "vendor": 193, "code": 10000, "defaultSeverity": "Warning", "defaultDescription": "This is a test alarm, please ignore.", "category": "CommunicationsAlarm" },\n{ "faultName": "NrfGroupUnavailable", "vendor": 193, "code": 1, "defaultSeverity": "Major", "defaultDescription": "The NRF-group is unavailable", "category": "CommunicationsAlarm" }\n]'> ./faultmappings/$(ALARM_SERVICE_NAME)-5g-bsf-eamarro.json
	@echo -e '[\n{ "faultName": "Test", "vendor": 193, "code": 10000, "defaultSeverity": "Warning", "defaultDescription": "This is a test alarm, please ignore.", "category": "CommunicationsAlarm" },\n{ "faultName": "NrfGroupUnavailable", "vendor": 193, "code": 1, "defaultSeverity": "Major", "defaultDescription": "The NRF-group is unavailable", "category": "CommunicationsAlarm" }\n]'> ./faultmappings/$(ALARM_SERVICE_NAME)-5g-bsf-eedaxs.json
	@echo -e '[\n{ "faultName": "Test", "vendor": 193, "code": 10000, "defaultSeverity": "Warning", "defaultDescription": "This is a test alarm, please ignore.", "category": "CommunicationsAlarm" },\n{ "faultName": "NrfGroupUnavailable", "vendor": 193, "code": 1, "defaultSeverity": "Major", "defaultDescription": "The NRF-group is unavailable", "category": "CommunicationsAlarm" }\n]'> ./faultmappings/$(ALARM_SERVICE_NAME)-5g-bsf-eedbjhe.json
	@echo -e '[\n{ "faultName": "Test", "vendor": 193, "code": 10000, "defaultSeverity": "Warning", "defaultDescription": "This is a test alarm, please ignore.", "category": "CommunicationsAlarm" },\n{ "faultName": "NrfGroupUnavailable", "vendor": 193, "code": 1, "defaultSeverity": "Major", "defaultDescription": "The NRF-group is unavailable", "category": "CommunicationsAlarm" }\n]'> ./faultmappings/$(ALARM_SERVICE_NAME)-5g-bsf-eedevag.json
	@echo -e '[\n{ "faultName": "Test", "vendor": 193, "code": 10000, "defaultSeverity": "Warning", "defaultDescription": "This is a test alarm, please ignore.", "category": "CommunicationsAlarm" },\n{ "faultName": "NrfGroupUnavailable", "vendor": 193, "code": 1, "defaultSeverity": "Major", "defaultDescription": "The NRF-group is unavailable", "category": "CommunicationsAlarm" }\n]'> ./faultmappings/$(ALARM_SERVICE_NAME)-5g-bsf-eedmiz.json
	@echo -e '[\n{ "faultName": "Test", "vendor": 193, "code": 10000, "defaultSeverity": "Warning", "defaultDescription": "This is a test alarm, please ignore.", "category": "CommunicationsAlarm" },\n{ "faultName": "NrfGroupUnavailable", "vendor": 193, "code": 1, "defaultSeverity": "Major", "defaultDescription": "The NRF-group is unavailable", "category": "CommunicationsAlarm" }\n]'> ./faultmappings/$(ALARM_SERVICE_NAME)-5g-bsf-eedstl.json
	@echo -e '[\n{ "faultName": "Test", "vendor": 193, "code": 10000, "defaultSeverity": "Warning", "defaultDescription": "This is a test alarm, please ignore.", "category": "CommunicationsAlarm" },\n{ "faultName": "NrfGroupUnavailable", "vendor": 193, "code": 1, "defaultSeverity": "Major", "defaultDescription": "The NRF-group is unavailable", "category": "CommunicationsAlarm" }\n]'> ./faultmappings/$(ALARM_SERVICE_NAME)-5g-bsf-eedsvs.json
	@echo -e '[\n{ "faultName": "Test", "vendor": 193, "code": 10000, "defaultSeverity": "Warning", "defaultDescription": "This is a test alarm, please ignore.", "category": "CommunicationsAlarm" },\n{ "faultName": "NrfGroupUnavailable", "vendor": 193, "code": 1, "defaultSeverity": "Major", "defaultDescription": "The NRF-group is unavailable", "category": "CommunicationsAlarm" }\n]'> ./faultmappings/$(ALARM_SERVICE_NAME)-5g-bsf-ehamada.json
	@echo -e '[\n{ "faultName": "Test", "vendor": 193, "code": 10000, "defaultSeverity": "Warning", "defaultDescription": "This is a test alarm, please ignore.", "category": "CommunicationsAlarm" },\n{ "faultName": "NrfGroupUnavailable", "vendor": 193, "code": 1, "defaultSeverity": "Major", "defaultDescription": "The NRF-group is unavailable", "category": "CommunicationsAlarm" }\n]'> ./faultmappings/$(ALARM_SERVICE_NAME)-5g-bsf-eisskoi.json
	@echo -e '[\n{ "faultName": "Test", "vendor": 193, "code": 10000, "defaultSeverity": "Warning", "defaultDescription": "This is a test alarm, please ignore.", "category": "CommunicationsAlarm" },\n{ "faultName": "NrfGroupUnavailable", "vendor": 193, "code": 1, "defaultSeverity": "Major", "defaultDescription": "The NRF-group is unavailable", "category": "CommunicationsAlarm" }\n]'> ./faultmappings/$(ALARM_SERVICE_NAME)-5g-bsf-emaxrue.json
	@echo -e '[\n{ "faultName": "Test", "vendor": 193, "code": 10000, "defaultSeverity": "Warning", "defaultDescription": "This is a test alarm, please ignore.", "category": "CommunicationsAlarm" },\n{ "faultName": "NrfGroupUnavailable", "vendor": 193, "code": 1, "defaultSeverity": "Major", "defaultDescription": "The NRF-group is unavailable", "category": "CommunicationsAlarm" }\n]'> ./faultmappings/$(ALARM_SERVICE_NAME)-5g-bsf-xchrfar.json
	@echo -e '[\n{ "faultName": "Test", "vendor": 193, "code": 10000, "defaultSeverity": "Warning", "defaultDescription": "This is a test alarm, please ignore.", "category": "CommunicationsAlarm" },\n{ "faultName": "NrfGroupUnavailable", "vendor": 193, "code": 1, "defaultSeverity": "Major", "defaultDescription": "The NRF-group is unavailable", "category": "CommunicationsAlarm" }\n]'> ./faultmappings/$(ALARM_SERVICE_NAME)-5g-bsf-xkorpap.json
	@echo -e '[\n{ "faultName": "Test", "vendor": 193, "code": 10000, "defaultSeverity": "Warning", "defaultDescription": "This is a test alarm, please ignore.", "category": "CommunicationsAlarm" },\n{ "faultName": "NrfGroupUnavailable", "vendor": 193, "code": 1, "defaultSeverity": "Major", "defaultDescription": "The NRF-group is unavailable", "category": "CommunicationsAlarm" }\n]'> ./faultmappings/$(ALARM_SERVICE_NAME)-5g-bsf-eifelesc.json
	@echo -e '[\n{ "faultName": "Test", "vendor": 193, "code": 10000, "defaultSeverity": "Warning", "defaultDescription": "This is a test alarm, please ignore.", "category": "CommunicationsAlarm" },\n{ "faultName": "NrfGroupUnavailable", "vendor": 193, "code": 1, "defaultSeverity": "Major", "defaultDescription": "The NRF-group is unavailable", "category": "CommunicationsAlarm" }\n]'> ./faultmappings/$(ALARM_SERVICE_NAME)-5g-bsf-demo.json

deploy: faultmappings
	$(HELM) install $(ADP_NAME) --repo $(ADP_REPO_BASE) --version $(ADP_VER) --namespace $(KUBE_NAMESPACE) --name $(ADP_NAME)-$(KUBE_NAMESPACE) --set backend.hostname=eric-data-document-database-pg.$(KUBE_NAMESPACE),backend.user=bsf_user,backend.password=bsf_password,backend.dbname=bsf_database --username $(USERNAME) --password $(PASSWORD)
	
deploy-default: faultmappings-default
	$(HELM) install $(ADP_NAME) --repo $(ADP_REPO_BASE) --version $(ADP_VER) --namespace default --name $(ADP_NAME)-default --set backend.hostname=eric-data-document-database-pg.default,backend.user=bsf_user,backend.password=bsf_password,backend.dbname=bsf_database --username $(USERNAME) --password $(PASSWORD)	

deploy-demo: faultmappings-demo
	$(HELM) install $(ADP_NAME) --repo $(ADP_REPO_BASE) --version $(ADP_VER) --namespace $(DEMO_NAMESPACE) --name $(ADP_NAME)-$(DEMO_NAMESPACE) --set backend.hostname=eric-data-document-database-pg.$(DEMO_NAMESPACE),backend.user=bsf_user,backend.password=bsf_password,backend.dbname=bsf_database,replicaCount=1 --username $(USERNAME) --password $(PASSWORD)

undeploy-default:
	$(HELM) delete --purge $(ADP_NAME)-default
	$(KUBECTL) -n default delete configmap faultmappings
	
undeploy-demo:
	$(HELM) delete --purge $(ADP_NAME)-$(DEMO_NAMESPACE)
	$(KUBECTL) -n $(DEMO_NAMESPACE) delete configmap faultmappings

undeploy:
	$(HELM) delete --purge $(ADP_NAME)-$(KUBE_NAMESPACE)
	$(KUBECTL) -n $(KUBE_NAMESPACE) delete configmap faultmappings

upgrade:
	$(HELM) upgrade $(ADP_NAME)-$(KUBE_NAMESPACE) $(ADP_REPO_BASE)/$(ADP_NAME)/$(ADP_NAME)-$(ADP_VER).tgz --namespace $(KUBE_NAMESPACE) --username $(USERNAME) --password $(PASSWORD)

upgrade-default:
	$(HELM) upgrade $(ADP_NAME)-default $(ADP_REPO_BASE)/$(ADP_NAME)/$(ADP_NAME)-$(ADP_VER).tgz --namespace default --username $(USERNAME) --password $(PASSWORD)

clean-all: undeploy deletekftopics

clean-all-default: undeploy-default deletekftopics-default

version:
	@echo "$(ADP_NAME): $(ADP_VER)" 

