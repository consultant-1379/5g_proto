################################################################################
#
#  Author   : eustone
#
#  Revision : 1.0
#  Date     : 2024-02-19 16:37:00
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2024
#
# The copyright to the computer program(s) herein is the property of
# Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission
# of Ericsson GmbH in accordance with the terms and conditions stipulated in
# the agreement/contract under which the program(s) have been supplied.
#
################################################################################

General Information
===================

The files in this directory structure are used by DAFT when setting up base stability traffic.

In the top level directory there are one or more *.config files which contains information
about a specific traffic model that should be used for configuring and starting the base
stability traffic.

This file contain job variable definitions that is used by the 107_Traffic_Tools_Configure_And_Start.pm
playlist to configure and start the base stability traffic.
It will set the %::JOB_PARAMS hash used by the DAFT playlist execution and can contain
any variable definitions which will update the hash, unless the same variable is specified with
the -v or --variable parameters to the execute_playlist.pl script which will have precedence
over any variable specified in the file.


Special Variables
=================

Even though any job variable can be specified, then following variables has special meaning for
the 107_Traffic_Tools_Configure_And_Start.pm playlist:

For BSF traffic:
----------------
- BSF_BASH_COMMAND_FILE=<file path>
  or
  BSF_BASH_COMMAND_FILE_01=<file path> and
  BSF_BASH_COMMAND_FILE_02=<file path> etc.

- BSF_CONFIG_FILE=<file path>
  or
  BSF_CONFIG_FILE_01=<file path> and
  BSF_CONFIG_FILE_02=<file path> etc.

- BSF_DSCLOAD_LOAD_SESSION_BINDINGS=<yes|no>

- BSF_DSCLOAD_TRAFFIC_COMMAND=<file path>

- BSF_FETCH_SESSION_BINDING_DATA=<yes|no>

- BSF_K6_COPY_FILE=<from file path> <to file path in K6 pod>
  or
  BSF_K6_COPY_FILE_01=<from file path> <to file path in K6 pod> and
  BSF_K6_COPY_FILE_02=<from file path> <to file path in K6 pod> etc.

- BSF_K6_SCRIPT_FILE=<file path>
  or
  BSF_K6_SCRIPT_FILE_01=<file path> and
  BSF_K6_SCRIPT_FILE_02=<file path> etc.

- BSF_K6_TRAFFIC_COMMAND=<string>
  or
  BSF_K6_TRAFFIC_COMMAND_01=<string> and
  BSF_K6_TRAFFIC_COMMAND_02=<string> etc.

For SCP traffic:
----------------
- SCP_BASH_COMMAND_FILE=<file path>
  or
  SCP_BASH_COMMAND_FILE_01=<file path> and
  SCP_BASH_COMMAND_FILE_02=<file path> etc.

- SCP_CONFIG_FILE=<file path>
  or
  SCP_CONFIG_FILE_01=<file path> and
  SCP_CONFIG_FILE_02=<file path> etc.

- SCP_K6_COPY_FILE=<from file path> <to file path in K6 pod>
  or
  SCP_K6_COPY_FILE_01=<from file path> <to file path in K6 pod> and
  SCP_K6_COPY_FILE_02=<from file path> <to file path in K6 pod> etc.

- SCP_K6_SCRIPT_FILE=<file path>
  or
  SCP_K6_SCRIPT_FILE_01=<file path> and
  SCP_K6_SCRIPT_FILE_02=<file path> etc.

- SCP_K6_TRAFFIC_COMMAND=<string>
  or
  SCP_K6_TRAFFIC_COMMAND_01=<string> and
  SCP_K6_TRAFFIC_COMMAND_02=<string> etc.

For SEPP traffic:
-----------------
- SEPP_BASH_COMMAND_FILE=<file path>
  or
  SEPP_BASH_COMMAND_FILE_01=<file path> and
  SEPP_BASH_COMMAND_FILE_02=<file path> etc.

- SEPP_CONFIG_FILE=<file path>
  or
  SEPP_CONFIG_FILE_01=<file path> and
  SEPP_CONFIG_FILE_02=<file path> etc.

- SEPP_K6_COPY_FILE=<from file path> <to file path in K6 pod>
  or
  SEPP_K6_COPY_FILE_01=<from file path> <to file path in K6 pod> and
  SEPP_K6_COPY_FILE_02=<from file path> <to file path in K6 pod> etc.

- SEPP_K6_SCRIPT_FILE=<file path>
  or
  SEPP_K6_SCRIPT_FILE_01=<file path> and
  SEPP_K6_SCRIPT_FILE_02=<file path> etc.

- SEPP_K6_TRAFFIC_COMMAND=<string>
  or
  SEPP_K6_TRAFFIC_COMMAND_01=<string> and
  SEPP_K6_TRAFFIC_COMMAND_02=<string> etc.

Inside of these strings or file paths you can have place holders that will be replaced
by values from the job parameters hash variable %::JOB_PARAMS.
For example <BASE_DIR> will be replaced with the value from the $::JOB_PARAMS{'BASE_DIR'} variable.
For more details, plese see "Replacement of place holders" below.


Loading order
=============

The 107_Traffic_Tools_Configure_And_Start.pm playlist will load the configuration data and
start the traffic in the following order:

CMYP Configuration data
-----------------------
1) Loading of CMYP BSF configuration data specified by the BSF_CONFIG_FILE and BSF_CONFIG_FILE_01 etc. variables.
   The file extension should be either .netconf or .cli and it determines if the data is loaded via the CMYP NETCONF or
   CMYP CLI interface.
   The configuration data is loaded using the 'expert' user, unless the CONFIG_USER_NAME variable is specified.

   Unless specifically specified with the BSF_CONFIG_FILE parameter it will also load the action_bsf_init_db.netconf
   file which should be present in the same directory as the other files.

2) Loading of CMYP SCP configuration data specified by the SCP_CONFIG_FILE and SCP_CONFIG_FILE_01 etc. variables.
   The file extension should be either .netconf or .cli and it determines if the data is loaded via the CMYP NETCONF or
   CMYP CLI interface.
   The configuration data is loaded using the 'expert' user, unless the CONFIG_USER_NAME variable is specified.


3) Loading of CMYP SEPP configuration data specified by the SEPP_CONFIG_FILE and SEPP_CONFIG_FILE_01 etc. variables.
   The file extension should be either .netconf or .cli and it determines if the data is loaded via the CMYP NETCONF or
   CMYP CLI interface.
   The configuration data is loaded using the 'expert' user, unless the CONFIG_USER_NAME variable is specified.

BASH or other scripts for configuration
---------------------------------------
4) Loading of other BSF specific configuration data e.g. kubctl commands etc. specified by the BSF_BASH_COMMAND_FILE and
   BSF_BASH_COMMAND_FILE_01 etc. variables.
   NOTE: That these files MUST HAVE executable file attributes e.g. 755 (chmod 755 <file path> and they can contain
   any type of command file e.g. a bash script or just a file with a bunch of commands, or any kind of executable.
   The only limitation is that the command cannot take any input parameters.

5) Loading of other SCP specific configuration data e.g. kubctl commands etc. specified by the SCP_BASH_COMMAND_FILE and
   SCP_BASH_COMMAND_FILE_01 etc. variables.
   NOTE: That these files MUST HAVE executable file attributes e.g. 755 (chmod 755 <file path> and they can contain
   any type of command file e.g. a bash script or just a file with a bunch of commands, or any kind of executable.
   The only limitation is that the command cannot take any input parameters.

6) Loading of other SEPP specific configuration data e.g. kubctl commands etc. specified by the SEPP_BASH_COMMAND_FILE and
   SEPP_BASH_COMMAND_FILE_01 etc. variables.
   NOTE: That these files MUST HAVE executable file attributes e.g. 755 (chmod 755 <file path> and they can contain
   any type of command file e.g. a bash script or just a file with a bunch of commands, or any kind of executable.
   The only limitation is that the command cannot take any input parameters.

Start BSF traffic
-----------------
7) Check to see that all PODs are in working order.

8) Clear BSF session binding database.

9) Copy K6 script files specified by the BSF_K6_SCRIPT_FILE and BSF_K6_SCRIPT_FILE_01 etc. variables to the K6 pods.

10) Copy any other files specified by the BSF_K6_COPY_FILE and BSF_K6_COPY_FILE_01 etc. variables to the K6 pods.

11) Start the K6 traffic generator specified by the BSF_K6_TRAFFIC_COMMAND and BSF_K6_TRAFFIC_COMMAND_01 etc.
    variables.
    These commands will be started as background jobs and STDOUT and STDERR saved to files in the job workspace
    directory.

12) Load specific hard coded session binding data for the dscload traffic generator if the variable
    BSF_DSCLOAD_LOAD_SESSION_BINDINGS=yes.

    The data loaded is the following for IPv4 node address:
    export NODE_IP=<node_worker_address>
    export BSFWRK_PORT=$( kubectl get svc --namespace <SC_NAMESPACE> eric-bsf-worker -o jsonpath="{.spec.ports[0].nodePort}")
    export BINDING_IPV4='{"supi":"imsi-12345","ipv4Addr":"10.0.0.1","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf-diamhost.com","pcfDiamRealm":"pcf-diamrealm.com","snssai":{"sst":2,"sd":"DEADF0"}}'
    curl -v -d "$BINDING_IPV4" -H "Content-Type: application/json" -X POST "http://$NODE_IP:$BSFWRK_PORT/nbsf-management/v1/pcfBindings"

    The data loaded is the following for IPv6 node address:
    export NODE_IP=<node_worker_address>
    export BSFWRK_PORT=$( kubectl get svc --namespace <SC_NAMESPACE> eric-bsf-worker -o jsonpath="{.spec.ports[0].nodePort}")
    export BINDING_IPV6='{"supi":"imsi-12345","ipv6Prefix":"2001:db8:abcd:0012::0/64","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf-diamhost.com","pcfDiamRealm":"pcf-diamrealm.com","snssai":{"sst":2,"sd":"DEADF0"}}'
    curl -v -d "$BINDING_IPV6" -H "Content-Type: application/json" -X POST "http://[$NODE_IP]:$BSFWRK_PORT/nbsf-management/v1/pcfBindings"

13) Start the dscload traffic generator specified by the BSF_DSCLOAD_TRAFFIC_COMMAND variable.

Start SCP traffic
-----------------
14) Copy K6 script files specified by the SCP_K6_SCRIPT_FILE and SCP_K6_SCRIPT_FILE_01 etc. variables to the K6 pods.

15) Copy any other files specified by the SCP_K6_COPY_FILE and SCP_K6_COPY_FILE_01 etc. variables to the K6 pods.

16) Start the K6 traffic generator specified by the SCP_K6_TRAFFIC_COMMAND and SCP_K6_TRAFFIC_COMMAND_01 etc.
    variables.
    These commands will be started as background jobs and STDOUT and STDERR saved to files in the job workspace
    directory.

Start SEPP traffic
------------------
15) Copy K6 script files specified by the SEPP_K6_SCRIPT_FILE and SEPP_K6_SCRIPT_FILE_01 etc. variables to the K6 pods.

16) Copy any other files specified by the SEPP_K6_COPY_FILE and SEPP_K6_COPY_FILE_01 etc. variables to the K6 pods.

17) Start the K6 traffic generator specified by the SEPP_K6_TRAFFIC_COMMAND and SEPP_K6_TRAFFIC_COMMAND_01 etc.
    variables.
    These commands will be started as background jobs and STDOUT and STDERR saved to files in the job workspace
    directory.


Replacement of place holders
============================
The files and special variables mentioned above can also contain place holders that will be replaced by values taken
from the current deployment(s) on the node.
A place holder is any string without white space surrounder by < > characters, for example <SC_NAMESPACE> and they
will be replaced by either values read from the node or taken from the network configuration file.

The following special place holders values are read from the node by the 938_Node_Information.pm playlist:
    <BSF_WRKR_IP>
    <BSF_WRKR_NODEPORT>
    <BSF_WRKR_PORT>
    <BSF_WRKR_TLS_NODEPORT>
    <BSF_WRKR_TLS_PORT>
    <CHFSIM_1_IP>
    <CHFSIM_1_NODEPORT_HTTP>
    <CHFSIM_1_NODEPORT_HTTPS>
    <CHFSIM_1_PORT_HTTP>
    <CHFSIM_1_PORT_HTTPS>
    <CHFSIM_2_IP>
    <CHFSIM_2_NODEPORT_HTTP>
    <CHFSIM_2_NODEPORT_HTTPS>
    <CHFSIM_2_PORT_HTTP>
    <CHFSIM_2_PORT_HTTPS>
    <CHFSIM_3_IP>
    <CHFSIM_3_NODEPORT_HTTP>
    <CHFSIM_3_NODEPORT_HTTPS>
    <CHFSIM_3_PORT_HTTP>
    <CHFSIM_3_PORT_HTTPS>
    <CHFSIM_4_IP>
    <CHFSIM_4_NODEPORT_HTTP>
    <CHFSIM_4_NODEPORT_HTTPS>
    <CHFSIM_4_PORT_HTTP>
    <CHFSIM_4_PORT_HTTPS>
    <CHFSIM_5_IP>
    <CHFSIM_5_NODEPORT_HTTP>
    <CHFSIM_5_NODEPORT_HTTPS>
    <CHFSIM_5_PORT_HTTP>
    <CHFSIM_5_PORT_HTTPS>
    <CHFSIM_6_IP>
    <CHFSIM_6_NODEPORT_HTTP>
    <CHFSIM_6_NODEPORT_HTTPS>
    <CHFSIM_6_PORT_HTTP>
    <CHFSIM_6_PORT_HTTPS>
    <CHFSIM_7_IP>
    <CHFSIM_7_NODEPORT_HTTP>
    <CHFSIM_7_NODEPORT_HTTPS>
    <CHFSIM_7_PORT_HTTP>
    <CHFSIM_7_PORT_HTTPS>
    <CHFSIM_8_IP>
    <CHFSIM_8_NODEPORT_HTTP>
    <CHFSIM_8_NODEPORT_HTTPS>
    <CHFSIM_8_PORT_HTTP>
    <CHFSIM_8_PORT_HTTPS>
    <CHFSIM_9_IP>
    <CHFSIM_9_NODEPORT_HTTP>
    <CHFSIM_9_NODEPORT_HTTPS>
    <CHFSIM_9_PORT_HTTP>
    <CHFSIM_9_PORT_HTTPS>
    <CHFSIM_10_IP>
    <CHFSIM_10_NODEPORT_HTTP>
    <CHFSIM_10_NODEPORT_HTTPS>
    <CHFSIM_10_PORT_HTTP>
    <CHFSIM_10_PORT_HTTPS>
    <CHFSIM_11_IP>
    <CHFSIM_11_NODEPORT_HTTP>
    <CHFSIM_11_NODEPORT_HTTPS>
    <CHFSIM_11_PORT_HTTP>
    <CHFSIM_11_PORT_HTTPS>
    <CHFSIM_12_IP>
    <CHFSIM_12_NODEPORT_HTTP>
    <CHFSIM_12_NODEPORT_HTTPS>
    <CHFSIM_12_PORT_HTTP>
    <CHFSIM_12_PORT_HTTPS>
    <CMYP_CLI_NODEPORT>
    <CMYP_CLI_PORT>
    <CMYP_IP>
    <CMYP_NETCONF_NODEPORT>
    <CMYP_NETCONF_PORT>
    <CMYP_NETCONF_TLS_NODEPORT>
    <CMYP_NETCONF_TLS_PORT>
    <CSA_WRKR_IP>
    <CSA_WRKR_NODEPORT>
    <CSA_WRKR_PORT>
    <CSA_WRKR_TLS_NODEPORT>
    <CSA_WRKR_TLS_PORT>
    <FIRST_WORKER_IP>
    <FIRST_WORKER_IP_TOOLS>
    <NODE_TRAFFIC_IP>
    <NODE_TRAFFIC_IPV6>
    <NODE_TRAFFIC_IPV6_TOOLS>
    <NODE_TRAFFIC_IP_TOOLS>
    <NRFSIM_IP>
    <NRFSIM_NODEPORT_HTTP>
    <NRFSIM_NODEPORT_HTTPS>
    <NRFSIM_PORT>
    <NRFSIM_PORT_HTTP>
    <NRFSIM_PORT_HTTPS>
    <SCP_WRKR_IP>
    <SCP_WRKR_NODEPORT>
    <SCP_WRKR_PORT>
    <SCP_WRKR_TLS_NODEPORT>
    <SCP_WRKR_TLS_PORT>
    <SEPPSIM_P1_IP>
    <SEPPSIM_P1_NODEPORT>
    <SEPPSIM_P1_NODEPORT_HTTP>
    <SEPPSIM_P1_NODEPORT_HTTPS>
    <SEPPSIM_P1_PORT_HTTP>
    <SEPPSIM_P1_PORT_HTTPS>
    <SEPPSIM_P2_IP>
    <SEPPSIM_P2_NODEPORT>
    <SEPPSIM_P2_NODEPORT_HTTP>
    <SEPPSIM_P2_NODEPORT_HTTPS>
    <SEPPSIM_P2_PORT_HTTP>
    <SEPPSIM_P2_PORT_HTTPS>
    <SEPPSIM_P3_IP>
    <SEPPSIM_P3_NODEPORT>
    <SEPPSIM_P3_NODEPORT_HTTP>
    <SEPPSIM_P3_NODEPORT_HTTPS>
    <SEPPSIM_P3_PORT_HTTP>
    <SEPPSIM_P3_PORT_HTTPS>
    <SEPPSIM_P4_IP>
    <SEPPSIM_P4_NODEPORT>
    <SEPPSIM_P4_NODEPORT_HTTP>
    <SEPPSIM_P4_NODEPORT_HTTPS>
    <SEPPSIM_P4_PORT_HTTP>
    <SEPPSIM_P4_PORT_HTTPS>
    <SEPPSIM_P5_IP>
    <SEPPSIM_P5_NODEPORT>
    <SEPPSIM_P5_NODEPORT_HTTP>
    <SEPPSIM_P5_NODEPORT_HTTPS>
    <SEPPSIM_P5_PORT_HTTP>
    <SEPPSIM_P5_PORT_HTTPS>
    <SEPPSIM_P6_IP>
    <SEPPSIM_P6_NODEPORT>
    <SEPPSIM_P6_NODEPORT_HTTP>
    <SEPPSIM_P6_NODEPORT_HTTPS>
    <SEPPSIM_P6_PORT_HTTP>
    <SEPPSIM_P6_PORT_HTTPS>
    <SEPPSIM_P7_IP>
    <SEPPSIM_P7_NODEPORT>
    <SEPPSIM_P7_NODEPORT_HTTP>
    <SEPPSIM_P7_NODEPORT_HTTPS>
    <SEPPSIM_P7_PORT_HTTP>
    <SEPPSIM_P7_PORT_HTTPS>
    <SEPPSIM_P8_IP>
    <SEPPSIM_P8_NODEPORT>
    <SEPPSIM_P8_NODEPORT_HTTP>
    <SEPPSIM_P8_NODEPORT_HTTPS>
    <SEPPSIM_P8_PORT_HTTP>
    <SEPPSIM_P8_PORT_HTTPS>
    <SEPP_WRKR_IP>
    <SEPP_WRKR_NODEPORT>
    <SEPP_WRKR_PORT>
    <SEPP_WRKR_TLS_NODEPORT>
    <SEPP_WRKR_TLS_PORT>
    <VIP_SIG2_SEPP>
    <VIP_SIG_BSF>
    <VIP_SIG_SCP>
    <VIP_SIG_SEPP>
    <WORKER_IP>
    <WORKER_IP_TOOLS>

Not all place holders are allowed in all special variables and files, so what is currently
replaced are as follows:

For the following variables and files can any of the above mentioned place holders be used
(as well as any other DAFT job variable name in the %::JOB_PARAMS hash):

    - xxxx_BASH_COMMAND_FILE and xxxx_BASH_COMMAND_FILE_yy
    - xxxx_CONFIG_FILE and xxxx_CONFIG_FILE_yy
    - xxxx_K6_TRAFFIC_COMMAND and xxxx_K6_TRAFFIC_COMMAND_yy

For the following variables will only the place holder <BASE_DIR> in the file path be replaced:

    - xxxx_K6_COPY_FILE and xxxx_K6_COPY_FILE_yy

For the following variables will only the place holder <BASE_DIR> be replaced in the file path
and only <SEPPSIM_P1_NODEPORT> to <SEPPSIM_P8_NODEPORT> and <TOOLS_NAMESPACE> inside the script
file be replaced:

    - xxxx_K6_SCRIPT_FILE and xxxx_K6_SCRIPT_FILE_yy

where xxxx is one of BSF, SCP or SEPP and yy is any alphanumerical characters e.g. 01, 02 etc.

For the following special variables there is no place holder replacement:
    - BSF_DSCLOAD_LOAD_SESSION_BINDINGS
    - BSF_DSCLOAD_TRAFFIC_COMMAND
    - BSF_FETCH_SESSION_BINDING_DATA


Currently supported traffic models
==================================

Model 1
-------
Loaded with the base_stability_traffic_bsf_scp_sepp_model.config or traffic_bsf_scp_sepp_model_01.config file.
This traffic does the following.........

Model 2
-------
Loaded with the base_stability_traffic_bsf_scp_sepp_model_02.config file.
This traffic does the following.........

udm-ca(sc-traf-root-ca-list1)                  ca-int    ca-ext                                      external(rp-ca)
                                 sc-traf-default-key1    sc-traf-default-key2
seppsim5 =============================================sepp========================================== seppsim1(rp1-ca) (n32)
nfUdm11.5gc.mnc567.mcc765.3gppnetwork.org              |                                             pSepp11.5gc.mnc012.mcc210.3gppnetwork.org
                                                       |
seppsim6 ==============================================|============================================ seppsim2(rp1-ca) (n32)
nfUdm12.5gc.mnc567.mcc765.3gppnetwork.org              |                                             pSepp12.5gc.mnc012.mcc210.3gppnetwork.org
                                                       |
seppsim7 ==============================================|============================================ seppsim3(rp2-ca) (n32)
nfUdm13.5gc.mnc567.mcc765.3gppnetwork.org              |                                             pSepp21.5gc.mnc123.mcc321.3gppnetwork.org
                                                       |
seppsim8 ==============================================|============================================ seppsim4(rp3-ca)
nfUdm14.5gc.mnc567.mcc765.3gppnetwork.org              |                                             pSepp31.5gc.mnc234.mcc432.3gppnetwork.org
                                      sepp.5gc.mnc567.mcc765.3gppnetwork.org
