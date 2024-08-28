export NODE_IP_TRAFFIC="<NODE_TRAFFIC_IP_TOOLS>"
export NRFSIM_NODEPORT=$(<KUBECTL_EXECUTABLE_TOOLS> get svc eric-nrfsim -n <TOOLS_NAMESPACE> -o jsonpath='{.spec.ports[0].nodePort}')

curl -X PUT "http://$NODE_IP_TRAFFIC:$NRFSIM_NODEPORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce103" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce103","nfType":"UDM","nfStatus":"REGISTERED","fqdn":"round_robin_nf_pool","priority":1,"udmInfo":{"gpsiRanges":[{"start":"460030100000000","end":"460030399999999"}]}}'
