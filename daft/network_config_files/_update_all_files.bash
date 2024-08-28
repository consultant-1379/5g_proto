#!/bin/bash

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.3
#  Date     : 2023-06-12 12:18:14
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021-2023
#
# The copyright to the computer program(s) herein is the property of
# Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission
# of Ericsson GmbH in accordance with the terms and conditions stipulated in
# the agreement/contract under which the program(s) have been supplied.
#
################################################################################

input_dir=$(dirname $(realpath $0))
daft_dir=$(realpath $input_dir/../)
backup_dir="$input_dir/$(date +old_%Y%m%d_%H%M%S)"
one_line=0

echo -en "\nCreating backup directory $backup_dir\n"
mkdir -p $backup_dir
if [ $? -ne 0 ]; then
    echo "Unable to create backup directory"
    exit 1
fi

#
# xxx.xml
#
echo -en "\n$daft_dir/perl/bin/update_network_config_files.pl --file-pattern '*.xml' -i $input_dir -k -b $backup_dir -t $daft_dir/templates/Network_Config_Template.xml\n"
if [ $one_line -eq 0 ]; then
    $daft_dir/perl/bin/update_network_config_files.pl --file-pattern '*.xml' -i $input_dir -k -b $backup_dir -t $daft_dir/templates/Network_Config_Template.xml
else
    $daft_dir/perl/bin/update_network_config_files.pl --file-pattern '*.xml' -i $input_dir -k -b $backup_dir -t $daft_dir/templates/Network_Config_Template.xml -r one-line
fi
if [ $? -ne 0 ]; then
    echo "Unable to update the network config files"
    exit 1
fi

echo -en "\nRemember to delete the backup directory $backup_dir\n\n";
exit 0
