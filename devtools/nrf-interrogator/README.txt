Demo Instructions: Traffic Routing on SUPI         2020-04-07 eedala


VDI                   KaaS-Cluster
---------      --------------------------------------------------------------
                                                   Prov.A   Prov.B   Prov.C
curl               Envoy       NRF-Interrogator    ChfSim1  ChfSim2  ChfSim3
 |    CC CREATE      |                 |               |
 |-----{SUPI}------->|                 |               |
 |    JSON Body      |LUA:             |               |
 |                   |extract SUPI     |               |
 |                   |-----[SUPI]----->|               |
 |                   | Header "supi"   |Find province  |
 |                   |                 |for SUPI       |
 |                   |<--[Province]----|               |
 |                   |          Header |               |
 |                   | "province-name" |               |
 |                   | ="ProvinceA"    |               |
 |                   |                 |               |
 |                   |LUA: set         |               |
 |                   |routing header   |               |
 |                   |"x-province"     |               |
 |                   |                 |               |
 |                   |Envoy: route     |               |
 |                   |on header        |               |
 |                   |"x-province"     |               |
 |                   |---CC CREATE Req.--------------->|
 |                   |<---------------CC CREATE Rep.---|
 |<--CC CREATE Rep.--|
   

1. Deploy the container with the NRF-Interrogator simulator:
   cd devtools/nrf-interrogator
   make deploy
2. Start the NRF-Interrogator simulator:
   k exec -it nrf-interrogator-* -- bash
   python ./app.py

3. Deploy the container with the standalone Envoy:
   cd devtools/envoy-standalone
   make deploy
4. Start Envoy:
   k exec -it envoy-standalone-deployment* -- bash
   envoy -c 21-supi-lua.yaml -l debug

5. Deploy ChfSim:
   cd simulators/chfsim
   make deploy

6. Make requests to test the setup. The NRF-Interrogation simulator
   maps SUPI values as follows:
   [1000,1999] -> Province A = ChfSim 1
   [2000,2999] -> Province B = ChfSim 2
   [3000,3999] -> Province C = ChfSim 3
   curl -vs -X POST -H "Content-Type: application/json" -d '{"supi": "1500"}' 'http://$NODEIP:$ENVOYPORT/nchf-convergedcharging/v1/chargingdata'
   curl -vs -X POST -H "Content-Type: application/json" -d '{"supi": "2345"}' 'http://$NODEIP:$ENVOYPORT/nchf-convergedcharging/v1/chargingdata'
   curl -vs -X POST -H "Content-Type: application/json" -d '{"supi": "3000"}' 'http://$NODEIP:$ENVOYPORT/nchf-convergedcharging/v1/chargingdata'
   curl -vs -X POST -H "Content-Type: application/json" -d '{"supi": "15000"}' 'http://$NODEIP:$ENVOYPORT/nchf-convergedcharging/v1/chargingdata'

EOF
