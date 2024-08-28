<KUBECTL_EXECUTABLE_TOOLS> -n <TOOLS_NAMESPACE> delete -f <BASE_DIR>/traffic_config/model_02/seppsim-secret.yaml
<KUBECTL_EXECUTABLE_TOOLS> -n <TOOLS_NAMESPACE> apply -f <BASE_DIR>/traffic_config/model_02/seppsim-secret.yaml
for n in $(<KUBECTL_EXECUTABLE_TOOLS> -n <TOOLS_NAMESPACE> get pod | grep eric-seppsim-p | awk '{print $1}'); do <KUBECTL_EXECUTABLE_TOOLS> -n <TOOLS_NAMESPACE> delete pod ${n}; done;
sleep 30
