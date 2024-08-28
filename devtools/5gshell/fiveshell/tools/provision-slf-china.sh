#!/bin/bash
#
# Provision the NRF with data for SLF (SUPI-Ranges)
# Commands taken from https://wcdma-confluence.rnd.ki.sw.ericsson.se/display/DSCNode/CURL+Commands+for+Manual+Testing+of+Micro-Services+in+SC#CURLCommandsforManualTestingofMicro-ServicesinSC-SLFProvisioning
#
# Usage: provision-slf.sh <node-ip> <nrfsim-port>
#
# Alexander.Langer@ericsson.com 2020-06-02 

NODE_IP=$1
NRFSIM_PORT=$2

echo "Node-IP: $NODE_IP"
echo "Port: $NRFSIM_PORT"
echo "-------------------------"

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce100" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce100","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"Anhui-1","priority":1,"chfInfo":{"supiRangeList":[{"start":"460001357924600","end":"460001357924609"}]},"nfServicePersistence":false,"nfServices":[{"serviceInstanceId":"1","serviceName":"nchf-convergedcharging","versions":[{"apiVersionInUri":"v1","apiFullVersion":"1.0"}],"scheme":"http","nfServiceStatus":"REGISTERED"},{"serviceInstanceId":"2","serviceName":"nchf-spendinglimitcontrol","versions":[{"apiVersionInUri":"v1","apiFullVersion":"1.0"}],"scheme":"http","nfServiceStatus": "REGISTERED"}]}' | jq |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce101" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce101","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"Anhui-2","priority":2,"chfInfo":{"supiRangeList":[{"start":"460001357924600","end":"460001357924609"}]},"nfServicePersistence":false,"nfServices":[{"serviceInstanceId":"1","serviceName":"nchf-spendinglimitcontrol","versions":[{"apiVersionInUri":"v1","apiFullVersion":"1.0"}],"scheme":"http","nfServiceStatus": "REGISTERED"}]}' | jq |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce102" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce102","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"Hebei-1","priority":1,"chfInfo":{"supiRangeList":[{"start":"460001357924610","end":"460001357924619"}]},"nfServicePersistence":false}' | jq |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce103" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce103","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"Henan-1","priority":1,"chfInfo":{"supiRangeList":[{"start":"460001357924620","end":"460001357924629"}]},"nfServicePersistence":false}' | jq |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce104" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce104","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"Hubei-1","priority":1,"chfInfo":{"supiRangeList":[{"start":"460001357924630","end":"460001357924639"}]},"nfServicePersistence":false}' | jq |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce105" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce105","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"Hunan-1","priority":1,"chfInfo":{"supiRangeList":[{"start":"460001357924640","end":"460001357924649"}]},"nfServicePersistence":false}' | jq |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce106" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce106","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"Jiangxi-1","priority":1,"chfInfo":{"supiRangeList":[{"start":"460001357924650","end":"460001357924659"}]},"nfServicePersistence":false}' | jq |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce107" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce107","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"Shanxi-1","priority":1,"chfInfo":{"supiRangeList":[{"start":"460001357924660","end":"460001357924669"}]},"nfServicePersistence":false}' | jq |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce108" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce108","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"Shaanxi-1","priority":1,"chfInfo":{"supiRangeList":[{"start":"460001357924670","end":"460001357924679"}]},"nfServicePersistence":false}' | jq |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce109" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce109","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"Shaanxi-2","priority":2,"chfInfo":{"supiRangeList":[{"start":"460001357924670","end":"460001357924679"}]},"nfServicePersistence":false}' | jq |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce10a" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce10a","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"Shanghai-1","priority":1,"chfInfo":{"supiRangeList":[{"start":"460001357924680","end":"460001357924689"}]},"nfServicePersistence":false}' | jq |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce10b" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce10b","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"Zhejiang-1","priority":1,"chfInfo":{"supiRangeList":[{"pattern":"^imsi-46000135792469[0-9]{1}$"}]},"nfServicePersistence":false}' | jq |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce10c" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce10c","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"Zhejiang-2","priority":2,"chfInfo":{"supiRangeList":[{"pattern":"^imsi-46000135792469[0-9]{1}$"}]},"nfServicePersistence":false}' | jq |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce10d" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce10d","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"Zhejiang-3","priority":3,"chfInfo":{"supiRangeList":[{"pattern":"^imsi-46000135792469[0-9]{1}$"}]},"nfServicePersistence":false}' | jq |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

