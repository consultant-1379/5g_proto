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

osCollectConfigCMD="sudo sed -i '/verify=/ s/CONF\.cfn\.ca_certificate/False/' /usr/lib/python3.6/site-packages/os_collect_config/cfn.py"
ansible -a "$osCollectConfigCMD" all -i ~/ansible_inventory.ini
