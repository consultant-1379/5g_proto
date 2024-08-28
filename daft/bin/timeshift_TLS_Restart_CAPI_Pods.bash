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

stack_name=$(kubectl get namespaces -A | grep capi | awk '{print$1}')
kubectl patch deployment -n $stack_name capi-controller-manager -p '{"spec":{"template":{"metadata":{"annotations":{"timeshift": "1"}}}}}'
kubectl patch deployment -n kube-system infra-controller-manager -p '{"spec":{"template":{"metadata":{"annotations":{"timeshift": "1"}}}}}'
kubectl patch deployment -n $stack_name capi-kubeadm-bootstrap-controller-manager -p '{"spec":{"template":{"metadata":{"annotations":{"timeshift": "1"}}}}}'
kubectl patch deployment -n kube-system calico-kube-controllers -p '{"spec":{"template":{"metadata":{"annotations":{"timeshift": "1"}}}}}'
