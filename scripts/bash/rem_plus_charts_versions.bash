#!/bin/bash

####################################################################################
#
# Author   : eedjoz
#
# Revision : R1A
# Date     : 2020-08-19
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
#
# Input ($2) is the repository URL to check. 
# It can be one of those (copied from task in bob rule file):
# repository $(if [ \"${env.PACKAGING}\" == \"true\" ];
#            then
#               echo ${HELM_CHART_RELEASE_REPO};
#            else
#               echo ${HELM_CHART_REPO};
#            fi; )/
# NOTE:
# HELM_CHART_REPO: https://armdocker.rnd.ericsson.se/artifactory/proj-5g-bsf-helm
# HELM_CHART_RELEASE_REPO: https://armdocker.rnd.ericsson.se/artifactory/proj-5g-sc-helm
#
####################################################################################

# Variables
status=0
repository=""
repository_local=""
esc_version=""

# Parameters
esc_version="$1"
repository="$2"
authorization="$3"

# Check if the csar package is intended to contain only plus versions
echo "The esc version to check for plus version is:"
cat $esc_version
cat $esc_version | grep +                                               &> /dev/null
status=$?
if [ $status -eq 1 ]; then
   echo "esc version is not a 'plus' version. Therefore no need to remove the charts with plus versions."
   exit 0
fi


# Files can not be deleted from the repository which is input ($2) directly.
# Instead a local copy has to be used (adding "-local") to the repository name
repository_local=${repository}-local



# Proceed to delete the folder

# former authentication curl -f -k -H "X-JFrog-Art-Api:${ARTIFACTORY_TOKEN}" -X DELETE $repository_local/$USER//             &> /dev/null
curl -f -k -H "Authorization: Bearer ${authorization}" -X DELETE $repository_local/$USER//             &> /dev/null
       status=$?
       if [ $status -eq 22 ]; then
          echo -e "\nA folder containing the plus chart versions for user $USER was not existing in artifactory. HTTP error 404 received."
       fi

exit 0
