<KUBECTL_EXECUTABLE_TOOLS> -n <TOOLS_NAMESPACE> delete -f <BASE_DIR>/traffic_config/model_02/scp_seppsim_p3.yaml
<KUBECTL_EXECUTABLE_TOOLS> -n <TOOLS_NAMESPACE> delete -f <BASE_DIR>/traffic_config/model_02/scp_seppsim_p4.yaml
<KUBECTL_EXECUTABLE_TOOLS> -n <TOOLS_NAMESPACE> apply -f <BASE_DIR>/traffic_config/model_02/scp_seppsim_p3.yaml
<KUBECTL_EXECUTABLE_TOOLS> -n <TOOLS_NAMESPACE> apply -f <BASE_DIR>/traffic_config/model_02/scp_seppsim_p4.yaml
<KUBECTL_EXECUTABLE_TOOLS> -n <TOOLS_NAMESPACE> scale deployment eric-seppsim-p3 --replicas 0
<KUBECTL_EXECUTABLE_TOOLS> -n <TOOLS_NAMESPACE> scale deployment eric-seppsim-p4 --replicas 0
<KUBECTL_EXECUTABLE_TOOLS> -n <TOOLS_NAMESPACE> scale deployment eric-seppsim-p3 --replicas 1
<KUBECTL_EXECUTABLE_TOOLS> -n <TOOLS_NAMESPACE> scale deployment eric-seppsim-p4 --replicas 1
sleep 30
