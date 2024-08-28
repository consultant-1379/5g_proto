#!/bin/bash

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

master_nodes_ips=$(kubectl get nodes --selector='node-role.kubernetes.io/control-plane' --no-headers=true -o wide | awk '{print $6}')
for ip in ${master_nodes_ips}; do
    rsync -azvh /home/eccd/front-proxy-ca.crt ${ip}:/home/eccd/front-proxy-ca.crt
    rsync -azvh /home/eccd/front-proxy-ca.key ${ip}:/home/eccd/front-proxy-ca.key
    sleep 2
    rsync -azvh /home/eccd/kubeadmin.config ${ip}:/home/eccd/kubeadmin.config
done
