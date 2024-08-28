# vim:ts=4:sw=4:sw=4:et
#
# COPYRIGHT ERICSSON GMBH 2019
#
# The copyright to the computer program(s) herein is the property
# of Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written
# permission of Ericsson GmbH in accordance with
# the terms and conditions stipulated in the agreement/contract
# under which the program(s) have been supplied.
#
# Created on: Jan 14, 2019
#     Author: eedbjhe
#

TOP := ../../..
include $(TOP)/CONFIG
include $(TOP)/devtools/CONFIG
include $(TOP)/devenv/CONFIG

ADP_NAME := grafana
ADP_VER := stable

GRAFANA_USER := admin
GRAFANA_PASSWORD := $(shell $(KUBECTL) get secret --namespace $(KUBE_NAMESPACE) $(ADP_NAME)-$(KUBE_NAMESPACE) -o jsonpath="{.data.admin-password}" | base64 --decode ; echo)

DASHBOARD_JSON := $(shell cat BSF-dashboard.template | sed 's/\#\#\#NAMESPACE\#\#\#/$(KUBE_NAMESPACE)/g')

SERVICE_IP := `kubectl get nodes --namespace $(KUBE_NAMESPACE) -o jsonpath="{.items[0].status.addresses[0].address}"`
NODE_PORT := `kubectl get services --namespace $(KUBE_NAMESPACE) -o jsonpath="{.spec.ports[0].nodePort}" $(ADP_NAME)-$(KUBE_NAMESPACE)`

SERVICE_IP_RESULT := $(shell echo $(SERVICE_IP))
NODE_PORT_RESULT := $(shell echo $(NODE_PORT))

REPO := stable/grafana

.PHONY: all deploy undeploy clean clean-all

all: configure

deploy:
	@$(HELM) install --name $(ADP_NAME)-$(KUBE_NAMESPACE) $(REPO) --set sidecar.datasources.enabled=true --set sidecar.dashboards.enabled=true --set sidecar.datasources.label=grafana_datasource --set sidecar.dashboards.label=grafana_dashboard --set service.type=NodePort --namespace $(KUBE_NAMESPACE)
	@echo "Access grafana via http://$(SERVICE_IP):$(NODE_PORT)/dashboards"
	
deploy-demo:
	$(HELM) install --name $(ADP_NAME)-$(DEMO_NAMESPACE) $(REPO) --set sidecar.datasources.enabled=true --set sidecar.dashboards.enabled=true --set sidecar.datasources.label=grafana_datasource --set sidecar.dashboards.label=grafana_dashboard --set service.type=NodePort --namespace $(DEMO_NAMESPACE)
	@echo "Access grafana via http://$(SERVICE_IP):$(NODE_PORT)/dashboards"
		
configure:
	@echo "Username: $(GRAFANA_USER), Password:$(GRAFANA_PASSWORD)"
	@curl --user $(GRAFANA_USER):$(GRAFANA_PASSWORD) 'http://$(SERVICE_IP_RESULT):$(NODE_PORT_RESULT)/api/datasources' -X POST -H 'Content-Type: application/json;charset=UTF-8' --data-binary '{"name":"PM Server","isDefault":false ,"type":"prometheus","url":"http://eric-pm-server:9090","access":"proxy","basicAuth":false}' > /dev/null
	@curl --user $(GRAFANA_USER):$(GRAFANA_PASSWORD) -i -H  "Content-Type: application/json" -X POST http://$(SERVICE_IP_RESULT):$(NODE_PORT_RESULT)/api/dashboards/import -d '{"dashboard":$(DASHBOARD_JSON), "folderId": 0, "inputs": [{"name":"DS_PM_SERVER","pluginId":"prometheus","type":"datasource","value":"PM Server"}], "overwrite": true}' >/dev/null

instructions:
	
	
get_secret:
	$(KUBECTL) get secret --namespace $(KUBE_NAMESPACE) $(ADP_NAME)-$(KUBE_NAMESPACE) -o jsonpath="{.data.admin-password}" | base64 --decode ; echo

undeploy:
	$(HELM) delete --purge $(ADP_NAME)-$(KUBE_NAMESPACE)

clean:

clean-all: undeploy clean

version:
	@echo "$(ADP_NAME): $(ADP_VER)" 

