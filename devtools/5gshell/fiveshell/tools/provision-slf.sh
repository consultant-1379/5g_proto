#!/bin/bash
#
# Provision the NRF with data for SLF (SUPI-Ranges)
# This data (province names) matches the sample_config_rcc.netconf
#
# Commands taken (and province names modified) from:
# https://wcdma-confluence.rnd.ki.sw.ericsson.se/display/DSCNode/CURL+Commands+for+Manual+Testing+of+Micro-Services+in+SC#CURLCommandsforManualTestingofMicro-ServicesinSC-SLFProvisioning
#
# Usage: provision-slf.sh <node-ip> <nrfsim-port>
#
# Alexander.Langer@ericsson.com 2020-06-04

NODE_IP=${1:-`kubectl get nodes -o jsonpath="{.items[7].status.addresses[0].address}"`}
NRFSIM_PORT=${2:-`kubectl get -o jsonpath="{.spec.ports[0].nodePort}" services eric-nrfsim`}

echo "Node-IP: $NODE_IP"
echo "Port: $NRFSIM_PORT"
echo "-------------------------"


curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce102" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce102","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"RegionA","priority":1,"chfInfo":{"supiRangeList":[{"start":"460001357924610","end":"460001357924619"}]},"nfServicePersistence":false}' | jl |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce103" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce103","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"RegionB","priority":1,"chfInfo":{"supiRangeList":[{"start":"460001357924620","end":"460001357924629"}]},"nfServicePersistence":false}' | jl |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

curl -sS -X PUT "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/2ec8ac0b-265e-4165-86e9-e0735e6ce104" -H "Content-Type: application/json" -d '{"nfInstanceId":"2ec8ac0b-265e-4165-86e9-e0735e6ce104","nfType":"CHF","nfStatus":"REGISTERED","fqdn":"RegionC","priority":1,"chfInfo":{"supiRangeList":[{"start":"460001357924630","end":"460001357924639"}]},"nfServicePersistence":false}' | jl |egrep '(fqdn|Status|start|end|serviceName|^})' |sed 's/  //g' |sed 's/\}/-------------------------/'

echo "Done."
