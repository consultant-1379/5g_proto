export MONITOR_USER=$(<KUBECTL_EXECUTABLE_TOOLS> get secret eric-sc-monitor-secret -n <TOOLS_NAMESPACE> -o jsonpath='{.data.username}'  | base64 -d)
export MONITOR_PWD=$(<KUBECTL_EXECUTABLE_TOOLS> get secret eric-sc-monitor-secret -n <TOOLS_NAMESPACE> -o jsonpath='{.data.password}'   | base64 -d)
export MONITOR_IP="<NODE_TRAFFIC_IP_TOOLS>"
export MONITOR_NODEPORT=$(<KUBECTL_EXECUTABLE_TOOLS> get svc eric-sc-monitor -n <TOOLS_NAMESPACE> -o jsonpath='{.spec.ports[0].nodePort}')

#sleep of 5s since the curl command was to fast after the patch command resulting in connection refused.
curl -k -v --cert <BASE_DIR>/traffic_config/model_02/server.crt --key <BASE_DIR>/traffic_config/model_02/server.key -u ${MONITOR_USER}:${MONITOR_PWD} -X PUT "https://$MONITOR_IP:$MONITOR_NODEPORT/monitor/api/v0/commands?target=eric-nrfsim&command=config" -H "Content-Type: application/json" -d '{"loadTestMode":{"isEnabled":false},"nnrfAccessToken":{"response":{"delayInMillis":0,"doDrop":false}},"nnrfBootstrapping":{"response":{"delayInMillis":0,"doDrop":false}},"nnrfNfDiscovery":{"validityPeriodInSecs":5,"response":{"delayInMillis":0,"doDrop":false},"maxPayloadSize":124},"nnrfNfManagement":{"heartBeatTimerInSecs":259200,"validityPeriodInSecs":259200,"response":{"delayInMillis":0,"doDrop":false},"provisioning":[]}}'
