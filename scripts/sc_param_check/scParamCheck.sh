#!/bin/bash
#######################################################################################################
#
# COPYRIGHT ERICSSON GMBH 2023-2024
#
# The copyright to the computer program(s) herein is the property of Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission of Ericsson GmbH in
# accordance with the terms and conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#
#######################################################################################################
charts_path=../../esc
charts=helm/eric-sc-umbrella/Chart.yaml,nlf/helm/Chart.yaml,scp/helm/Chart.yaml,rlf/helm/Chart.yaml,slf/helm/Chart.yaml,bsf/helm/Chart.yaml,bsf/bsfdiameter/helm/Chart.yaml,sepp/helm/Chart.yaml,monitor/helm/Chart.yaml
vnf_descriptor=../../esc/release_artifacts/sc_vnf_descriptor.yaml

checkVnfDescriptor="python3 scParamCheck.py $* --check vnfd --vnf-descriptor $vnf_descriptor Main,SC\ Common,SCP,SEPP,BSF,Generic $charts_path $charts"
checkCustomerValues="python3 scParamCheck.py $* --check yamls Main,SC\ Common,SCP,SEPP,BSF,Generic customerValueCheck/ Chart.yaml"
checkAllCharts="python3 scParamCheck.py $* Main,SC\ Common,SCP,SEPP,BSF,Generic $charts_path $charts"
checkParams="python3 scParamCheck.py $*"

cat customerValueCheck/eric-sc-values.yaml | sed -E 's/(^\s*)# (.*$)/\1\2/g' > customerValueCheck/values.yaml

PROG_NAME=`basename $0`


if [ $PROG_NAME == scParamCheck.sh ]; then
    echo "usage: checkParams [-h] ..."
    echo "usage: checkCustomerValues WORKBOOK"
    echo "usage: checkAllCharts WORKBOOK"
    echo "usage: checkVnfDescriptor WORKBOOK"
    echo
    echo "scParamCheck.sh is not supposed to be called directly but rather through softlinks:"
    echo "  checkParams           Calls scParamCheck.py without any tailored parameters. -h for help."
    echo "  checkCustomerValues   Compares given Excel workbook against eric-sc-values.yaml."
    echo "  checkAllCharts        Compares given Excel workbook against all SC chars in $charts_path."
    echo "  checkVnfDescriptor    Compares given Excel workbook against VNF descriptor $vnf_descriptor."
    echo
    echo "  Call above commands with -h or --help for more details."
    echo
else
    CMD=${!PROG_NAME}
    echo $CMD

    eval $CMD
fi



