export NODE_IP_TRAFFIC="<NODE_TRAFFIC_IP_TOOLS>"
export SCPWRK_IP="<SCP_WRKR_IP>"
export SCPWRK_PORT=1090
export CFHSIM_NODEPORT=$(<KUBECTL_EXECUTABLE_TOOLS> get svc eric-chfsim-10 -n <TOOLS_NAMESPACE> -o jsonpath='{.spec.ports[0].nodePort}')
export NRFSIM_NODEPORT=$(<KUBECTL_EXECUTABLE_TOOLS> get svc eric-nrfsim -n <TOOLS_NAMESPACE> -o jsonpath='{.spec.ports[0].nodePort}')

curl -X POST "http://$NODE_IP_TRAFFIC:$CFHSIM_NODEPORT/admin/v1/nrf/set_nf_profile" -H "Content-Type: application/json" -d '{"nfInstanceId":null,"nfType":"CUSTOM_CHF","nfStatus":"REGISTERED","plmnList":[{"mcc":"412","mnc":"088"}],"sNssais":[{"sst":10010,"sd":"Aa10010"}], "locality":"Canada","nfServices":[{"serviceInstanceId":"Pool2.LABCHF.VCCOSL5GL10","serviceName":"nchf-convergedcharging","versions":[{"apiVersionInUri":"v2","apiFullVersion":"2.0.0"},{"apiVersionInUri":"v3","apiFullVersion":"3.0.1"}], "scheme":"http","nfServiceStatus":"REGISTERED","fqdn":"eric-chfsim-10","ipEndPoints":[{"ipv4Address":"'$NODE_IP_TRAFFIC'","transport":"TCP","port":'$CFHSIM_NODEPORT'}],"priority":1,"capacity":20,"nfServiceSetIdList":["setA"],"oauth2Required":false}, {"serviceInstanceId":"Pool2.LABCHF.VCCOSL5GL00","serviceName":"nchf-spendinglimitcontrol","versions":[{"apiVersionInUri":"v1","apiFullVersion":"1.1.3"}], "scheme":"http","nfServiceStatus":"REGISTERED","fqdn":"eric-chfsim-10","ipEndPoints":[{"ipv4Address":"'$NODE_IP_TRAFFIC'","transport":"TCP","port":'$CFHSIM_NODEPORT'}],"priority":1,"capacity":20,"nfServiceSetIdList":["setA"],"oauth2Required":false}], "nfServiceList":{"nchf-spendinglimitcontrolhttp":{"serviceInstanceId":"Pool2.LABCHF.VCCOSL5GL00","serviceName":"nchf-spendinglimitcontrol","versions":[{"apiVersionInUri":"v1","apiFullVersion":"1.1.3"}], "scheme":"http","nfServiceStatus":"REGISTERED","fqdn":"eric-chfsim-10","ipEndPoints":[{"ipv4Address":"'$NODE_IP_TRAFFIC'","transport":"TCP","port":'$CFHSIM_NODEPORT'}],"priority":1,"capacity":20, "nfServiceSetIdList":["setA"],"oauth2Required":false},"nchf-convergedcharginghttp":{"serviceInstanceId":"Pool2.LABCHF.VCCOSL5GL10","serviceName":"nchf-convergedcharging", "versions":[{"apiVersionInUri":"v2","apiFullVersion":"2.0.0"},{"apiVersionInUri":"v3","apiFullVersion":"3.0.1"}], "scheme":"http","nfServiceStatus":"REGISTERED","fqdn":"eric-chfsim-10","ipEndPoints":[{"ipv4Address":"'$NODE_IP_TRAFFIC'","transport":"TCP","port":'$CFHSIM_NODEPORT'}],"priority":1,"capacity":20,"nfServiceSetIdList":["setA"],"oauth2Required":false}}, "nfSetIdList":["setA"],"scpDomains":["ericsson.se"]}'

curl -X POST "http://$NODE_IP_TRAFFIC:$CFHSIM_NODEPORT/admin/v1/nrf/set_addr/http,eric-nrfsim.,$NRFSIM_NODEPORT,$NODE_IP_TRAFFIC" -H "Content-Type: application/json"

curl -X POST "http://$NODE_IP_TRAFFIC:$CFHSIM_NODEPORT/admin/v1/set_envoy_domain/http,$SCPWRK_IP:$SCPWRK_PORT" -H "Content-Type: application/json"

curl -X POST "http://$NODE_IP_TRAFFIC:$CFHSIM_NODEPORT/admin/v1/nrf/set_heartbeat/on" -H "Content-Type: application/json"

curl -X POST "http://$NODE_IP_TRAFFIC:$CFHSIM_NODEPORT/admin/v1/load_test_mode/on" -H "Content-Type: application/json"

curl  -X POST "http://$NODE_IP_TRAFFIC:$CFHSIM_NODEPORT/admin/v1/nrf/do_nf_instance_register"
