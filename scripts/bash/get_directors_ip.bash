#!/bin/bash

####################################################################################
#
# Author   : eedjoz
#
# Revision : R1B
# Date     : 2023-01-12
#
# Deskcheck: -

####################################################################################
#
#  (C) COPYRIGHT ERICSSON GMBH 2021-2023
#
#  The copyright to the computer program(s) herein is the property of
#  Ericsson GmbH, Germany.
#
#  The program(s) may be used and/or copied only  with the written permission
#  of Ericsson GmbH in accordance with the terms and conditions stipulated in
#  the agreement/contract under which the program(s) have been supplied.
#
####################################################################################

# The script gets as input the node name (from the initial form) and assigns the ip address of director-0


case "$1" in
    Pikachu*)
        echo '10.117.19.200'  > ./var.DIRECTOR-0-IP
        ;;
    Eevee*)
        echo '10.221.169.167' > ./var.DIRECTOR-0-IP
        ;;
    n200-dsc8989*)
        echo '10.117.12.69'  > ./var.DIRECTOR-0-IP
        ;;
    n200-dsc8991*)
        echo '10.117.12.85'  > ./var.DIRECTOR-0-IP
        ;;
    n103-dsc5672*)
        echo '10.117.139.37'  > ./var.DIRECTOR-0-IP
        ;;
    n103-dsc5429*)
        echo '10.117.139.5'  > ./var.DIRECTOR-0-IP
        ;;
    n103-dsc12709*)
        echo '214.7.192.69'  > ./var.DIRECTOR-0-IP
        ;;
    sc-flexikube-39357*)
        echo '214.6.6.72'  > ./var.DIRECTOR-0-IP
        ;;
    mc-ipv4-1015*)
        echo '10.228.219.18'  > ./var.DIRECTOR-0-IP
        ;;
    Snorlax*)
        echo '10.155.195.148'  > ./var.DIRECTOR-0-IP
        ;;
    n104-cnis*)
        echo '10.120.230.36'  > ./var.DIRECTOR-0-IP
        ;;
    n106-cnis*)
        echo '214.6.255.228'  > ./var.DIRECTOR-0-IP
        ;;
    n293-cnis*)
        echo '214.15.26.68'  > ./var.DIRECTOR-0-IP
        ;;

    *)
        echo "The IP address of the director for node $1 couldn't be found. IaaS automated jobs for this node are not set up for Jenkins yet"
        exit 1
        ;;
esac

exit 0
