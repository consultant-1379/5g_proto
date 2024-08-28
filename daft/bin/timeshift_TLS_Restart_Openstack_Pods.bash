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

kubectl patch daemonset -n kube-system openstack-cinder-csi-nodeplugin -p '{"spec":{"template":{"metadata":{"annotations":{"timeshift": "1"}}}}}'
kubectl patch deployment -n kube-system openstack-cinder-csi-controllerplugin -p '{"spec":{"template":{"metadata":{"annotations":{"timeshift": "1"}}}}}'
kubectl patch daemonset -n kube-system openstack-cloud-controller-manager -p '{"spec":{"template":{"metadata":{"annotations":{"timeshift": "1"}}}}}'
