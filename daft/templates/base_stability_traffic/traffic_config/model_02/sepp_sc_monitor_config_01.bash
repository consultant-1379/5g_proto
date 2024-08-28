export MONITOR_USER=$(<KUBECTL_EXECUTABLE_TOOLS> get secret eric-sc-monitor-secret -n <TOOLS_NAMESPACE> -o jsonpath='{.data.username}' | base64 -d)
export MONITOR_PWD=$(<KUBECTL_EXECUTABLE_TOOLS> get secret eric-sc-monitor-secret -n <TOOLS_NAMESPACE> -o jsonpath='{.data.password}' | base64 -d)
export MONITOR_IP="<NODE_TRAFFIC_IP_TOOLS>"
export MONITOR_NODEPORT=$(<KUBECTL_EXECUTABLE_TOOLS> get svc eric-sc-monitor -n <TOOLS_NAMESPACE> -o jsonpath='{.spec.ports[0].nodePort}')

curl -u ${MONITOR_USER}:${MONITOR_PWD} -k -X PUT "https://$MONITOR_IP:$MONITOR_NODEPORT/monitor/api/v0/commands?target=eric-seppsim-p1&command=config" -d '{"ownDomain": "nfudm1.5gc.mnc012.mcc210.3gppnetwork.org", "ownIpAddress": "localhost"}' --cert <BASE_DIR>/traffic_config/model_02/server.crt --key <BASE_DIR>/traffic_config/model_02/server.key -v |jq
curl -u ${MONITOR_USER}:${MONITOR_PWD} -k -X PUT "https://$MONITOR_IP:$MONITOR_NODEPORT/monitor/api/v0/commands?target=eric-seppsim-p2&command=config" -d '{"ownDomain": "nfudm1.5gc.mnc012.mcc210.3gppnetwork.org", "ownIpAddress": "localhost"}' --cert <BASE_DIR>/traffic_config/model_02/server.crt --key <BASE_DIR>/traffic_config/model_02/server.key -v |jq
curl -u ${MONITOR_USER}:${MONITOR_PWD} -k -X PUT "https://$MONITOR_IP:$MONITOR_NODEPORT/monitor/api/v0/commands?target=eric-seppsim-p3&command=config" -d '{"ownDomain": "nfudm1.5gc.mnc012.mcc210.3gppnetwork.org", "ownIpAddress": "localhost"}' --cert <BASE_DIR>/traffic_config/model_02/server.crt --key <BASE_DIR>/traffic_config/model_02/server.key -v |jq
curl -u ${MONITOR_USER}:${MONITOR_PWD} -k -X PUT "https://$MONITOR_IP:$MONITOR_NODEPORT/monitor/api/v0/commands?target=eric-seppsim-p4&command=config" -d '{"ownDomain": "nfudm1.5gc.mnc012.mcc210.3gppnetwork.org", "ownIpAddress": "localhost"}' --cert <BASE_DIR>/traffic_config/model_02/server.crt --key <BASE_DIR>/traffic_config/model_02/server.key -v |jq
