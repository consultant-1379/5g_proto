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

#
# This script collects kubectl node and pod logs every 30 seconds
# and write it to a file which is specified by the user.
# It produces no visible output to the user and continues running
# until it's stopped by the user pressing CTRL-C or killed.
#

log_file=$1

while (true) do
    date >> $log_file
    kubectl get nodes -o wide >> $log_file
    kubectl get pods -A -o wide >> $log_file
    sleep 30
done
