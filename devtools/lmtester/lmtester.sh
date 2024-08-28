#!/bin/bash
COMMANDS=("quit", "help")

unset sim_pid

nels_pod=$(kubectl get pods -n 5g-bsf-$USER | awk '/test-nels/ {print $1}')
kubectl -n 5g-bsf-$USER port-forward $nels_pod 9090:8080 &
sim_pid=$!

lm_pod=$(kubectl get pods -n 5g-bsf-$USER | awk '/license-consumer-handler/ {print $1; exit;}')
kubectl -n 5g-bsf-$USER port-forward $lm_pod 8080:8080 &
lm_pid=$!

sleep 3

while true
do
    read -r -p ">" cmd


    case $cmd in
        exit)
            kill -9 $sim_pid
            kill -9 $lm_pid
            exit 0
            ;;
        help)
            "help"
            ;;
        create-nels)
            echo ""
            echo "Executing: curl -i -w \"\\n\" -H \"Content-Type: application/json\" -X POST \"localhost:9090/nels-management/servers\" --data '{\"serverName\": \"Super Bassomatic 76\", \"thriftPort\": 9095 }'"
            echo ""

            curl -i -w "\n" -H "Content-Type: application/json" \
            -X POST "localhost:9090/nels-management/servers" \
            --data '{"serverName": "Super Bassomatic 76", "thriftPort": 9095 }' > server.txt
           
            cat server.txt
           
            NELS_SIM_ID=$(cat server.txt | grep Location | sed -r "s|Location: .*/(.+)\r$|\1|")
            rm -f server.txt
            ;;
        get-nels-info)
            echo ""
            echo "Executing: curl -w \"\\n\" -H \"Content-Type: application/json\" -X GET localhost:9090/nels-management/servers/$NELS_SIM_ID"
            echo ""

            curl -w "\n" -H "Content-Type: application/json" \
            -X GET localhost:9090/nels-management/servers/$NELS_SIM_ID
            ;; 
        start-nels)
            echo ""
            echo "Executing: curl -w \"\\n\" --header \"Content-Type: application/json\" --request PATCH localhost:9090/nels-management/servers/$NELS_SIM_ID --data '{\"status\": \"STARTED\"}'"
            echo ""
           
            curl -w "\n" --header "Content-Type: application/json" \
            --request PATCH localhost:9090/nels-management/servers/$NELS_SIM_ID \
            --data '{"status": "STARTED"}'
            ;;
        stop-nels)
            echo ""
            echo "Executing: curl -w \"\\n\" --header \"Content-Type: application/json\" --request PATCH localhost:9090/nels-management/servers/$NELS_SIM_ID --data '{\"status\": \"STOPPED\"}'"
            echo ""

            curl -w "\n" --header "Content-Type: application/json" \
            --request PATCH localhost:9090/nels-management/servers/$NELS_SIM_ID \
            --data '{"status": "STOPPED"}'
            ;;
        delete-nels)
            echo ""
            echo "Executing: curl -w \"\\n\" --header \"Content-Type: application/json\" --request DELETE localhost:9090/nels-management/servers/$NELS_SIM_ID --data '{}'"
            echo ""

            curl -w "\n" --header "Content-Type: application/json" \
            --request DELETE localhost:9090/nels-management/servers/$NELS_SIM_ID \
            --data '{}'
            ;;
        install-licenses)
            echo ""
            echo "Executing: curl -w \"\\n\" --header \"Content-Type: application/json\" \
                            --request POST localhost:9090/nels-management/servers/$NELS_SIM_ID/license-keys \
                            --data '{
                                \"productType\":\"SIGNALING_CONTROLLER\",
                                \"customerId\":\"Eric123\",
                                \"swltId\":\"Palaven\",
                                \"keys\":[
                                    {\"licenseId\":\"FAT1024197/001\",\"licenseType\":\"CAPACITY_PEAK\",\"start\":\"2019-05-08\",\"stop\":\"2020-05-08\",\"capacity\":100},
                                    {\"licenseId\":\"FAT1024224/001\",\"licenseType\":\"CAPACITY_PEAK\",\"start\":\"2019-05-08\",\"stop\":\"2020-11-25\",\"capacity\":100},
                                    {\"licenseId\":\"LICENSE/A/2\",\"licenseType\":\"CAPACITY_PEAK\",\"start\":\"2018-05-08\",\"stop\":\"2019-05-08\",\"capacity\":50},
                                    {\"licenseId\":\"LICENSE/A/3\",\"licenseType\":\"CAPACITY_PEAK\",\"start\":\"2020-05-13\",\"stop\":\"2021-05-28\",\"capacity\":200}
                                ]
                            }'"
            echo ""

            curl -w "\n" --header "Content-Type: application/json" \
            --request POST localhost:9090/nels-management/servers/$NELS_SIM_ID/license-keys \
            --data '{
                "productType":"SIGNALING_CONTROLLER",
                "customerId":"Eric123",
                "swltId":"Palaven",
                "keys":[
                {"licenseId":"FAT1024197/001","licenseType":"CAPACITY_PEAK","start":"2019-05-08","stop":"2020-05-08","capacity":100},
                {"licenseId":"FAT1024224/001","licenseType":"CAPACITY_PEAK","start":"2019-05-08","stop":"2020-11-25","capacity":100},
                {"licenseId":"LICENSE/A/2","licenseType":"CAPACITY_PEAK","start":"2018-05-08","stop":"2019-05-08","capacity":50},
                {"licenseId":"LICENSE/A/3","licenseType":"CAPACITY_PEAK","start":"2020-05-13","stop":"2021-05-28","capacity":200}
                ]
            }'
            ;;

        lm-get-licenses)
            echo ""
            echo "Executing: curl -w \"\\n\" --header \"Content-Type: application/json\" --request GET localhost:8080/license-manager/api/v1/licenses --data '{\"productType\":\"SIGNALING_CONTROLLER\"}'"
            echo ""

            curl -w "\n" --header "Content-Type: application/json" \
            --request GET localhost:8080/license-manager/api/v1/licenses \
            --data '{"productType":"SIGNALING_CONTROLLER"}'
            ;;
        
        lm-request-licenses)
            echo ""
            echo "Executing: curl -w \"\\n\" --header \"Content-Type: application/json\" --request POST localhost:8080/license-manager/api/v1/licenses/requests \
                  --data '{\"productType\":\"SIGNALING_CONTROLLER\", \"licenses\":[ \
                        {\"keyId\":\"FAT1024197/001\",\"type\":\"CAPACITY_PEAK\"}, \
                        {\"keyId\":\"LICENSE/A/2\",\"type\":\"CAPACITY_PEAK\"}, \
                        {\"keyId\":\"LICENSE/A/3\",\"type\":\"CAPACITY_PEAK\"} \
                    ]}'"
            echo ""

            curl -w "\n" --header "Content-Type: application/json" \
            --request POST localhost:8080/license-manager/api/v1/licenses/requests \
            --data '{"productType":"SIGNALING_CONTROLLER", "licenses":[
                {"keyId":"FAT1024197/001","type":"CAPACITY_PEAK"},
                {"keyId":"LICENSE/A/2","type":"CAPACITY_PEAK"},
                {"keyId":"LICENSE/A/3","type":"CAPACITY_PEAK"}
            ]}' 
            
    esac
done
