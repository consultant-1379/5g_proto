################################################################################
#
#  Author   : eustone
#
#  Revision : 1.0
#  Date     : 2024-06-10 18:20:00
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

cloud_config=$(kubectl get secret -n kube-system cloud-config -o jsonpath='{.data.cloud\.conf}'  | base64 -d |sed '/ca-file/a tls-insecure = true' |base64 -w 0)
kubectl patch secret -n kube-system cloud-config --type=json  -p='[{"op" : "replace" ,"path" : "/data/'cloud.conf'" ,"value" : "'${cloud_config}'"}]'
