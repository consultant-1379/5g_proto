#!/bin/bash

####################################################################################
#
# Author   : eedjoz
#
# Revision : R1A
# Date     : 2020-11-16
#
# Deskcheck: -
#
####################################################################################
#
#  (C) COPYRIGHT ERICSSON GMBH 2020
#
#  The copyright to the computer program(s) herein is the property of
#  Ericsson GmbH, Germany.
#
#  The program(s) may be used and/or copied only  with the written permission
#  of Ericsson GmbH in accordance with the terms and conditions stipulated in
#  the agreement/contract under which the program(s) have been supplied.
#
####################################################################################

####################################################################################
# Input ($1) is the path to the file var.esc-version
#
####################################################################################

# Variables
OUTPUT_DIR=".bob" 
ESC_SERVICE_NAME="eric-sc"

# Parameters
esc_version="$1"


echo "charts created under the umbrella directory"
ls ./${OUTPUT_DIR}/${ESC_SERVICE_NAME}-umbrella/charts

ls ./${OUTPUT_DIR}/${ESC_SERVICE_NAME}-umbrella/charts | grep -e eric-bsf -e eric-sc -e eric-sepp | grep -v eric-sc-manager | grep -v eric-sc-hcagent | grep -v eric-sc-license-consumer | grep -v ${esc_version} > ./${OUTPUT_DIR}/charts-versions.txt

if [ -s ./${OUTPUT_DIR}/charts-versions.txt ];
then
    echo "Versions for these charts do not match the package version (${esc_version})"
    cat ./${OUTPUT_DIR}/charts-versions.txt
    echo 'Creation of csar package will stop now'
    exit 1
fi
echo "The chart versions match the package version (${esc_version})"
echo 'Creation of csar package continues'
exit 0
