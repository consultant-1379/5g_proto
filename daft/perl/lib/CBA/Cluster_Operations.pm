package CBA::Cluster_Operations;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.20
#  Date     : 2019-09-17 16:11:30
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2017, 2018, 2019
#
# The copyright to the computer program(s) herein is the property of
# Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission
# of Ericsson GmbH in accordance with the terms and conditions stipulated in
# the agreement/contract under which the program(s) have been supplied.
#
################################################################################

use strict;
use warnings;

use Exporter qw(import);

our @EXPORT_OK = qw(
    cmw_campaign_status
    create_brf_backup
    delete_brf_backup
    get_blade_boot_epochtime
    get_brf_backup_names
    get_brf_backup_names_cliss
    get_dsc_active_controller
    get_node_count
    get_node_hostname_list
    get_node_id_list
    get_node_rpm_information
    get_peer_hostname
    get_this_hostname
    install_rpm
    remove_rpm
    restore_brf_backup
    send_command_cliss
    send_command_signmcli
    send_command_targetblade
    );

use File::Basename qw(dirname basename);
use File::Temp qw/ tempfile tempdir /;
use General::OS_Operations;

# -----------------------------------------------------------------------------
# Give the cmw-campaign-status command and wait for expected status to be
# returned or failure.
# Return it as an integer number.
#
# Input variables:
#    - Campaign name: Name of the campaign to check the status for.
#    - Hide output: 0=No, 1=Yes
#    - Valid Status: E.g. INITIAL, EXECUTING, COMPLETED, COMMITED
#    - Timeout: Number of seconds to wait for expected result to be returned.
#      If the value is =0 then no timeout check is used and the command will
#      execute until the campaign returns the expected result ot a failure.
#
# Return values:
#    Return code 0 = The expected status was returned.
#                1 = An unexpected status was returned or a timeout occurred.
#
# -----------------------------------------------------------------------------
sub cmw_campaign_status {
    my $campaign_name = shift;
    my $hide = shift;
    my $valid_status = shift;
    my $timeout = shift;

    my $current_time = time();
    my $max_time = $current_time + $timeout;
    my $rc;
    my @result = ();

    while (1) {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "/opt/coremw/bin/cmw-campaign-status $campaign_name",
                "hide-output"   => $hide == 0 ? 0 : 1,
                "return-output" => \@result,
            }
        );

        # Return failure to caller if command failed
        if ($rc != 0) {
            print "\nThe command cmw-campaign-status failed with rc=$rc\n";
            return 1;
        }

        # Check result for expected status, failure or timeout
        for (@result) {
            if (/^$campaign_name=$valid_status/i) {
                # Return success to caller
                return 0;
            } elsif (/^$campaign_name=(SUSPENDED_BY_ERROR_DETECTED|ERROR_DETECTED|FAILED)\s*$/) {
                # Return failure to caller
                if ($hide) {
                    print "\nThe campaign failed with status '$1'\n";
                    for (@result) {
                        print $_;
                    }
                }
                return 1;
            } elsif ($timeout > 0) {
                if (time() >= $max_time) {
                    # Max time expired, return failure to caller
                    print "\nThe maximum wait time of $timeout seconds expired\n";
                    return 1;
                }
            }
            # Wait a bit longer to try the command again
            sleep(10);
            last;
        }
    }
}

# -----------------------------------------------------------------------------
# Create a BRF backup.
#
# Input variables:
#    - BRF Backup name
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    Return code 0 = The backup was created successfully.
#                1 = The backup failed or did not finish within 30 minutes.
#
# -----------------------------------------------------------------------------
sub create_brf_backup {
    my $backup_name = shift;
    my $hide = shift;

    my $active_host = "";
    my $command = "";
    my $current_host = "";
    my $end_time;
    my $rc;
    my @result = ();
    my $success = 0;

    # Check if the current host is the active controller for the COM process.
    # If not then ssh is needed to the active controller
    $active_host = get_dsc_active_controller("COM", $hide);
    $current_host = get_this_hostname($hide);
    if ($current_host eq $active_host) {
        $command = "printf 'configure\\nManagedElement=1,SystemFunctions=1,BrM=1,BrmBackupManager=SYSTEM_DATA,createBackup $backup_name\\nend\\nexit\\n' | cliss -b -s";
    } else {
        $command = "ssh $active_host \"printf 'configure\\nManagedElement=1,SystemFunctions=1,BrM=1,BrmBackupManager=SYSTEM_DATA,createBackup $backup_name\\nend\\nexit\\n' | cliss -b -s\"";
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => $hide == 0 ? 0 : 1,
            "return-output" => \@result,
        }
    );

    if ($rc == 0) {
        $success = 0;
        for (@result) {
            if (/^true\s*$/) {
                $success = 1;
            } elsif (/^false\s*$/) {
                $success = 0;
            }
        }
    }

    # Check if the backup started
    if ($success == 0) {
        # The BRF backup did not start
        return 1;
    }

    # Monitor the BRF Backup until it's finished or max 30 minutes
    $end_time = time() + 1800;
    if ($current_host eq $active_host) {
        $command = "printf 'show ManagedElement=1,SystemFunctions=1,BrM=1,BrmBackupManager=SYSTEM_DATA,progressReport\\nexit\\n' | cliss -b -s";
    } else {
        $command = "ssh $active_host \"printf 'show ManagedElement=1,SystemFunctions=1,BrM=1,BrmBackupManager=SYSTEM_DATA,progressReport\\nexit\\n' | cliss -b -s\"";
    }
    while (1) {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => $hide == 0 ? 0 : 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            for (@result) {
                if (/result=SUCCESS/) {
                    # Backup was successful
                    return 0;
                } elsif (/result=FAILURE/) {
                    # Backup failed
                    return 1;
                } elsif (/state=FINISHED/) {
                    # Backup was successful
                    return 0;
                } elsif (/result=CANCELLED/) {
                    # Backup failed
                    return 1;
                }
            }
        } else {
            # Backup failed
            return 1;
        }

        if (time() >= $end_time) {
            last;
        } else {
            sleep 10;
        }
    }

    # Backup failed
    return 1;
}

# -----------------------------------------------------------------------------
# Delete a BRF backup.
#
# Input variables:
#    - BRF Backup name
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    Return code 0 = The backup was deleted successfully.
#                1 = The backup failed to be deleted.
#
# -----------------------------------------------------------------------------
sub delete_brf_backup {
    my $backup_name = shift;
    my $hide = shift;

    my $active_host = "";
    my $command = "";
    my $current_host = "";
    my $rc;
    my @result = ();
    my $success = 0;

    # Check if the current host is the active controller for the COM process.
    # If not then ssh is needed to the active controller
    $active_host = get_dsc_active_controller("COM", $hide);
    $current_host = get_this_hostname($hide);
    if ($current_host eq $active_host) {
        $command = "printf 'configure\\nManagedElement=1,SystemFunctions=1,BrM=1,BrmBackupManager=SYSTEM_DATA,deleteBackup $backup_name\\nexit\\n' | cliss -b -s";
    } else {
        $command = "ssh $active_host \"printf 'ManagedElement=1,SystemFunctions=1,BrM=1,BrmBackupManager=SYSTEM_DATA,deleteBackup $backup_name\\nexit\\n' | cliss -b -s\"";
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => $hide == 0 ? 0 : 1,
            "return-output" => \@result,
        }
    );

    if ($rc == 0) {
        $success = 0;
        for (@result) {
            if (/^true\s*$/) {
                $success = 1;
            } elsif (/^false\s*$/) {
                $success = 0;
            }
        }
    }

    # Check if the backup was deleted
    if ($success == 1) {
        # The BRF backup was deleted
        return 0;
    } else {
        # The BRF backup was not deleted
        return 1;
    }
}

# -----------------------------------------------------------------------------
# Get the boot time for a specific blade or blade group specified as the number
# of seconds since the Epoch (1970-01-01 00:00:00 +0000 UTC).
# If only one blade is specified then a scalar value is returned and if a group
# is specified then a hash is returned where the key is the host name of the
# blade and the value is the boot time.
#
# Input variables:
#    - Hostname: "" for current blade, "SC-1", "SC-2", "PL-x", "all", "control"
#                or "payload".
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    - Scalar value of boot time since the Epoch, when one hostname is wanted.
#    - Hash with values for each wanted hostname of boot time since the Epoch,
#      when a group of hostnames is wanted.
#    If the value is 0 for one or more hosts then there was a problem to fetch
#    the boot epoch time and it should be treated as an error by the caller.
#
# -----------------------------------------------------------------------------
sub get_blade_boot_epochtime {
    my $host = shift;
    my $hide = shift;

    my %boot_time;
    my @hosts = ();
    my $rc = 0;
    my @result = ();

    # Check that a valid host name is given
    if ($host =~ /^(all|control|payload)$/i) {
        @hosts = get_node_hostname_list($host,$hide);
    } elsif ($host =~ /^(SC|PL)-\d+$/i) {
        push @hosts, uc($host);
    } elsif ($host eq "") {
        push @hosts, get_this_hostname($hide);
    } else {
        print "Wrong host name given '$host', allowed are SC-1, SC-2, PL-x, all, control or payload\n";
        return "";
    }

    # For all wanted hosts deactivate amd remove the RPM
    for (@hosts) {
        $host = $_;

        # Initialize the default value indicating an error
        $boot_time{$host} = 0;

        # Check if the RPM is loaded on the node
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ssh $host grep btime /proc/stat",
                "hide-output"   => $hide == 0 ? 0 : 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            print "Failed to fetch boot time from host '$host'\n";
            # Continue with next host
            next;
        }

        # For all matching RPMS, store the information in the output array
        for (@result) {
            if (/^btime\s+(\d+)\s*$/) {
                $boot_time{$host} = $1;
                last;
            }
        }
    }

    if (scalar @hosts == 1) {
        return $boot_time{$hosts[0]};
    } else {
        return %boot_time;
    }
}

# -----------------------------------------------------------------------------
# Get a list of all existing BRF backups using CLISS.
# Return it as an array.
#
# Input variables:
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    Array with backup names.
#
# -----------------------------------------------------------------------------
sub get_brf_backup_names {
    my $hide = shift;

    my @backup_names = ();
    my $rc;
    my @result = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "lde-brf print labels -t system | grep '^software :'",
            "hide-output"   => $hide == 0 ? 0 : 1,
            "return-output" => \@result,
        }
    );

    if ($rc == 0) {
        for (@result) {
            if (/^software\s*:\s*(\S+)/) {
                push @backup_names, $1;
            }
        }
    }

    return @backup_names;
}

# -----------------------------------------------------------------------------
# Get a list of all existing BRF backups using CLISS.
# Return it as an array.
#
# Input variables:
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    - Array with backup names.
#
# -----------------------------------------------------------------------------
sub get_brf_backup_names_cliss {
    my $hide = shift;

    my $active_host = "";
    my @backup_names = ();
    my $command = "";
    my $current_host = "";
    my $rc;
    my @result = ();

    # Check if the current host is the active controller for the COM process.
    # If not then ssh is needed to the active controller
    $active_host = get_dsc_active_controller("COM", $hide);
    $current_host = get_this_hostname($hide);
    if ($current_host eq $active_host) {
        $command = "printf 'show ManagedElement=1,SystemFunctions=1,BrM=1,BrmBackupManager=SYSTEM_DATA\\nexit\\n' | cliss -b -s";
    } else {
        $command = "ssh $active_host \"printf 'show ManagedElement=1,SystemFunctions=1,BrM=1,BrmBackupManager=SYSTEM_DATA\\nexit\\n' | cliss -b -s\"";
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => $hide == 0 ? 0 : 1,
            "return-output" => \@result,
        }
    );

    if ($rc == 0) {
        for (@result) {
            if (/^\s*BrmBackup=(\S+)/) {
                push @backup_names, $1;
            }
        }
    }

    return @backup_names;
}

# -----------------------------------------------------------------------------
# Get the hostname of the active controller for a specific service instance.
# Return it as a string.
#
# Input variables:
#    - Service Instance Alias: COM, DSC.DATACOLLECTOR, DSC.WEBSERVER, DSC.DIRECTOR,
#                              DDB.DIRECTOR, SEC or LM
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    - Hostname where Service Instance is running or empty: SC-1, SC-2.
#
# -----------------------------------------------------------------------------
sub get_dsc_active_controller {
    my $type = uc(shift);
    my $hide = shift;

    my $hostname = "";
    my $rc;
    my @result = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "/opt/dsc/cli/dsc-show-active-controller -s $type",
            "hide-output"   => $hide == 0 ? 0 : 1,
            "return-output" => \@result,
        }
    );

    if ($rc == 0) {
        if ($result[0] =~ /^(SC-\d+)$/) {
            $hostname = $1;
        }
    }

    return $hostname;
}

# -----------------------------------------------------------------------------
# Get the number of nodes of type all, control or payload.
# Return it as an integer number.
#
# Input variables:
#    - Type of node: all, control or payload.
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    - Number of nodes of type: all, control or payload.
#
# -----------------------------------------------------------------------------
sub get_node_count {
    my $type = lc(shift);
    my $hide = shift;

    my $number_of_nodes = 0;
    my $rc;
    my @result = ();

    if ($type !~ /^(all|control|payload)$/) {
        print "Wrong or no type specified, supported types are: all | control | payload\n";
        return $number_of_nodes;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "ls -1 /etc/cluster/nodes/$type/ 2>/dev/null | wc -l",
            "hide-output"   => $hide == 0 ? 0 : 1,
            "return-output" => \@result,
        }
    );

    if (scalar @result == 1) {
        if ($result[0] =~ /^(\d+)$/) {
            $number_of_nodes = $1;
        }
    }

    return $number_of_nodes;
}

# -----------------------------------------------------------------------------
# Get node hostname for all, control or payload nodes and return it as an array.
#
# Input variables:
#    - Type of node: all, control or payload.
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    - List of found node host names as an array, or empty array if none found.
#
# -----------------------------------------------------------------------------
sub get_node_hostname_list {
    my $type = lc(shift);
    my $hide = shift;

    my @array = ();
    my $rc;
    my @result = ();

    if ($type !~ /^(all|control|payload)$/) {
        print "Wrong or no type specified, supported types are: all | control | payload\n";
        return @array;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "cat /etc/cluster/nodes/$type/*/hostname 2>/dev/null",
            "hide-output"   => $hide == 0 ? 0 : 1,
            "return-output" => \@result,
        }
    );

    for (@result) {
        if (/^(PL-\d+|SC-\d+)$/) {
            push @array, $1;
        }
    }

    return @array;
}

# -----------------------------------------------------------------------------
# Get node identity numbers for all, control or payload nodes and return it as
# an array.
#
# Input variables:
#    - Type of node: all, control or payload.
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    - List of found node id numbers as an array, or empty array if none found.
#
# -----------------------------------------------------------------------------
sub get_node_id_list {
    my $type = lc(shift);
    my $hide = shift;

    my @array = ();
    my $rc;
    my @result = ();

    if ($type !~ /^(all|control|payload)$/) {
        print "Wrong or no type specified, supported types are: all | control | payload\n";
        return @array;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "cat /etc/cluster/nodes/$type/*/id 2>/dev/null",
            "hide-output"   => $hide == 0 ? 0 : 1,
            "return-output" => \@result,
        }
    );

    for (@result) {
        if (/^(\d+)$/) {
            push @array, $1;
        }
    }

    return @array;
}

# -----------------------------------------------------------------------------
# Get information about if an RPM is installed or not and the name of the RPM.
#
# Input variables:
#    - RPM to check if installed, e.g. DSC_CLUSTER_VERSION-1.9.3.0-CXP9024055_6_R4A
#      or DSC_CLUSTER_VERSION-.+
#    - Host to check if RPM is installed on: SC-1, SC-2, PL-x, control, payload or all.
#    - Array reference where the returned list of nodes the RPM is installed on
#      and the name of the RPM, or an empty array if the RPM is not installed
#      anywhere.
#      Each index of the array contains the following format "<node name>,<RPM name>"
#      for example giving "is_rpm_installed('DSC_CLUSTER_VERSION-.+','all',1)"
#      might return":
#      SC-1,DSC_CLUSTER_VERSION-1.9.3.0-CXP9024055_6_R4A
#      SC-2,DSC_CLUSTER_VERSION-1.9.3.0-CXP9024055_6_R4A
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    0: Information returned
#    1: Failure to return information for some reason
#
# -----------------------------------------------------------------------------
sub get_node_rpm_information {
    my $rpm_pattern = shift;
    my $host = shift;
    my $array_ref = shift;
    my $hide = shift;

    my @hosts = ();
    my $failure_cnt = 0;
    my $rc;
    my $rpm_name;
    my @result = ();

    # Initialize the output array reference
    @$array_ref = ();

    # Check that a valid host name is given
    if ($host =~ /^(all|control|payload)$/i) {
        @hosts = get_node_hostname_list($host,$hide);
    } elsif ($host =~ /^(SC|PL)-\d+$/) {
        push @hosts, uc($host);
    } else {
        print "Wrong host name given '$host', allowed are SC-1, SC-2, PL-x, all, control or payload\n";
        return 1;
    }

    # For all wanted hosts deactivate amd remove the RPM
    for (@hosts) {
        $host = $_;

        # Check if the RPM is loaded on the node
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "/opt/coremw/bin/cmw-rpm-list $host",
                "hide-output"   => $hide == 0 ? 0 : 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            print "Failed to fetch active RPMS from host '$host'\n";
            $failure_cnt++;
            # Continue with next host
            next;
        }

        # For all matching RPMS, store the information in the output array
        for (@result) {
            $rpm_name = $_;
            if ($rpm_name =~ /$rpm_pattern/) {
                push @$array_ref, "$host,$rpm_name";
            }
        }
    }

    if ($failure_cnt == 0) {
        return 0;
    } else {
        return 1;
    }
}

# -----------------------------------------------------------------------------
# Get hostname of peer node and return it as a string.
#
# Input variables:
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    - Hostname of current node e.g. SC-1 or SC-2, or empty string if no peer.
#
# -----------------------------------------------------------------------------
sub get_peer_hostname {
    my $hide = shift;

    my $hostname = "";
    my $rc;
    my @result = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "cat /etc/cluster/nodes/peer/hostname 2>/dev/null",
            "hide-output"   => $hide == 0 ? 0 : 1,
            "return-output" => \@result,
        }
    );

    for (@result) {
        if (/^(PL-\d+|SC-\d+)$/) {
            $hostname = $1;
            last;
        }
    }

    return $hostname;
}

# -----------------------------------------------------------------------------
# Get hostname of this (current) node and return it as a string.
#
# Input variables:
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    - Hostname of current node e.g. SC-1, SC-2, PL-3 etc.
#
# -----------------------------------------------------------------------------
sub get_this_hostname {
    my $hide = shift;

    my $hostname = "";
    my $rc;
    my @result = ();

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "cat /etc/cluster/nodes/this/hostname 2>/dev/null",
            "hide-output"   => $hide == 0 ? 0 : 1,
            "return-output" => \@result,
        }
    );

    for (@result) {
        if (/^(PL-\d+|SC-\d+)$/) {
            $hostname = $1;
            last;
        }
    }

    return $hostname;
}

# -----------------------------------------------------------------------------
# Install and activate an RPM file on the specified node or nodes.
#
# Input variables:
#    - Path to RPM file to install, e.g. /home/daft/expect-5.45-32.2.x86_64.rpm
#    - Host to install the RPM on: SC-1, SC-2, PL-x, control, payload or all.
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    0: Installation was Successful
#    1: Installation failed for some reason
#
# -----------------------------------------------------------------------------
sub install_rpm {
    my $rpm_file = shift;
    my $host = shift;
    my $hide = shift;

    my @hosts = ();
    my $rc;
    my @result = ();

    # Check that the RPM file exist
    unless (-f "$rpm_file") {
        print "No such RPM file exist '$rpm_file'\n";
        return 1;
    }

    # Check that a valid host name is given
    if ($host =~ /^(all|control|payload)$/i) {
        @hosts = get_node_hostname_list($host,$hide);
    } elsif ($host =~ /^(SC|PL)-\d+$/) {
        push @hosts, uc($host);
    } else {
        print "Wrong host name given '$host', allowed are SC-1, SC-2, PL-x, all, control or payload\n";
        return 1;
    }

    # For all wanted hosts install and activate the RPM
    for (@hosts) {
        $host = $_;
        # Add the RPM to the host, i.e. update /cluster/rpms/ directory with the rpm
        # and update the /cluster/nodes/<node id>/etc/rpm.conf file
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "/opt/coremw/bin/cmw-rpm-config-add $rpm_file $host",
                "hide-output"   => $hide == 0 ? 0 : 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            print "Failed to add the RPM '$rpm_file' to the host '$host'\n";
            for (@result) {
                print "$_\n";
            }
            return 1;
        }

        # Activate the RPM on the host
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "/opt/coremw/bin/cmw-rpm-config-activate $host",
                "hide-output"   => $hide == 0 ? 0 : 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            print "Failed to activate RPMS on the host '$host'\n";
            for (@result) {
                print "$_\n";
            }
            return 1;
        }
    }

    return 0;
}

# -----------------------------------------------------------------------------
# Deactivate and remove an RPM on the specified node or nodes.
#
# Input variables:
#    - RPM to remove, e.g. expect-5.45-32.2 or expect.+
#    - Host to remove the RPM from: SC-1, SC-2, PL-x, control, payload or all.
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    0: Removal was Successful.
#    1: Removal failed for some reason for one or more RPMS or hosts.
#
# -----------------------------------------------------------------------------
sub remove_rpm {
    my $rpm_pattern = shift;
    my $host = shift;
    my $hide = shift;

    my $activate_cnt = 0;
    my @hosts = ();
    my $failure_cnt = 0;
    my $rc;
    my $rpm_name;
    my @result = ();

    # Check that a valid host name is given
    if ($host =~ /^(all|control|payload)$/i) {
        @hosts = get_node_hostname_list($host,$hide);
    } elsif ($host =~ /^(SC|PL)-\d+$/) {
        push @hosts, uc($host);
    } else {
        print "Wrong host name given '$host', allowed are SC-1, SC-2, PL-x, all, control or payload\n";
        return 1;
    }

    # For all wanted hosts deactivate amd remove the RPM
    for (@hosts) {
        $host = $_;

        # Check if the RPM is loaded on the node
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "/opt/coremw/bin/cmw-rpm-list $host",
                "hide-output"   => $hide == 0 ? 0 : 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            print "Failed to fetch active RPMS from host '$host'\n";
            $failure_cnt++;
            # Continue with next host
            next;
        }

        # For all matching RPMS, remove them from the node
        $activate_cnt = 0;
        for (@result) {
            $rpm_name = $_;
            if ($rpm_name =~ /$rpm_pattern/) {
                # Remove the RPM from the host, i.e. update /cluster/rpms/ directory with the rpm
                # and update the /cluster/nodes/<node id>/etc/rpm.conf file
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "/opt/coremw/bin/cmw-rpm-config-delete $rpm_name $host",
                        "hide-output"   => $hide == 0 ? 0 : 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc == 0) {
                    $activate_cnt++;
                } else {
                    print "Failed to delete the RPM '$rpm_name' from the host '$host'\n";
                    for (@result) {
                        print "$_\n";
                    }
                    $failure_cnt++;
                    # Continue with next RPM
                    next;
                }
            }
        }

        if ($activate_cnt > 0) {
            # Deactivate the removed RPMS
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "/opt/coremw/bin/cmw-rpm-config-activate $host",
                    "hide-output"   => $hide == 0 ? 0 : 1,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0) {
                print "Failed to activate RPMS on the host '$host'\n";
                for (@result) {
                    print "$_\n";
                }
                $failure_cnt++;
            }
        }
    }

    if ($failure_cnt == 0) {
        return 0;
    } else {
        return 1;
    }
}

# -----------------------------------------------------------------------------
# Restore a BRF backup using the "lde-brf restore" command so it works even if
# CLISS doesn't work.
# The reboot to perform the actual restore on all blades is not done in this
# subroutine so the user must trigger that separately by e.g. command:
# "lde-reboot --all".
#
# Input variables:
#    - BRF Backup name
#    - Hide output: 0=No, 1=Yes
#
# Return values:
#    Return code 0 = The backup was restored successfully.
#                1 = The backup restoration failed for some reason.
#
# -----------------------------------------------------------------------------
sub restore_brf_backup {
    my $backup_name = shift;
    my $hide = shift;

    my @backups = ();
    my $found = 0;
    my $rc;
    my @result = ();

    # Check that the backup name exists
    # ---------------------------------
    @backups = get_brf_backup_names($hide);
    if (scalar @backups == 0) {
        print "No BRF backups exist\n";
        return 1;
    }
    $found = 0;
    for (@backups) {
        if ("$_" eq "$backup_name") {
            $found = 1;
            last;
        }
    }
    if ($found == 0) {
        print "No BRF backup with the name '$backup_name' exist\n";
        return 1;
    }

    # Prepare the restore operation
    # -----------------------------
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "lde-brf restore -l $backup_name -t system",
            "hide-output"   => $hide == 0 ? 0 : 1,
            "return-output" => \@result,
        }
    );
    if ($rc != 0) {
        print "Failed to prepare BRF restore\n";
        return 1;
    } else {
        for (@result) {
            if (/(error|warning)/i) {
                # WARNING: backup type software for label 'test_backup' does not exist
                print "Failed to prepare BRF restore\n";
                return 1;
            }
        }
    }

    # Commit the restore operation
    # ----------------------------
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "lde-brf restore --commit -l $backup_name",
            "hide-output"   => $hide == 0 ? 0 : 1,
            "return-output" => \@result,
        }
    );
    if ($rc != 0) {
        print "Failed to commit BRF restore\n";
        return 1;
    } else {
        my $expected_rpm_sync_count = get_node_count("all", $hide);
        my $rpm_sync_count = 0;
        for (@result) {
            if (/^Completed RPM synchronization of node \d+/) {
                $rpm_sync_count++;
            }
        }
        if ($rpm_sync_count == 0) {
            print "No 'Completed RPM synchronization of node' message found\n";
            return 1;
        } elsif ($expected_rpm_sync_count != $rpm_sync_count) {
            print "Warning: Found $rpm_sync_count when expecting $expected_rpm_sync_count 'Completed RPM synchronization of node' messages\nWhich might still be OK on scaled nodes\n";
        }
    }

    # Check that node is ready to be rebooted
    # ---------------------------------------
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "lde-brf print reboot",
            "hide-output"   => $hide == 0 ? 0 : 1,
            "return-output" => \@result,
        }
    );
    if ($rc != 0) {
        print "Failed to print reboot BRF information\n";
        return 1;
    } else {
        $found = 0;
        for (@result) {
            if (/^$backup_name containing 'software' data will be activated during next reboot/) {
                $found = 1;
                last;
            }
        }
        if ($found == 0) {
            print "The expected '$backup_name' backup will not be restored at next reboot\n";
            return 1;
        }
    }

    # Restore of BRF backup was successful, all that is needed now to activate
    # the restoration is a cluster reboot with 'lde-reboot --all'
    return 0;
}

# -----------------------------------------------------------------------------
# Send a command to the Signalling Manager CLI with the option to hide the
# output to the user and to log output to a detailed log file. It can also
# return the collected output to the user.
#
# Input variables:
#    - hash reference %params = Input parameters to the sub routine which can be
#      any of the following keys:
#         "command":
#             Command to send to the cliss shell as a scalar string value.
#         "commands":
#             An array reference of commands to send to the cliss shell as a
#             scalar string value.
#         "command-in-output":
#             Include the executed command in returned output when value =1 or
#             don't include it when value =0 or when not specified.
#             This parameter is probably only useful when you execute multiple
#             commands and want to look for the start of a command in the output.
#             The default is to not include the executed command in the returned
#             output.
#         "discard-stderr":
#             If specified then STDERR output is redirected to /dev/null and thus
#             discarded.
#             The default is to always redirect STDERR to STDOUT.
#         "hide-command":
#             Hide the command from the user when value =1 and don't hide it when
#             value =0 or when not specified.
#             The default is to always show the executed command to the user.
#         "hide-output":
#             Hide the output from the command from the user when value =1 and
#             don't hide output when value =0 or when not specified.
#             The default is to always show the executed command and it's result
#             to the user.
#         "ignore-errors":
#             Ignore errors reported by the command or commands and always return
#             successful return code to the caller when value =1 or return back
#             failure code to the caller if a command failed when value =0 or
#             when not specified.
#             The default is to always return back an error if a command failed.
#         "no-command-timestamp":
#             Do not show a timestamp to the user when a command is executed when
#             value =1 or show it when value =0 or not specified.
#             The default is to always show the timestamp to the user when a
#             command is executed.
#         "raw-output":
#             If specified then what is returned is exactly what is received from
#             the node including CR LF.
#             By default CR and LF are removed.
#         "return-output":
#             Array reference of variable where the result of the command is
#             returned to the caller.
#             If multiple commands are sent then the returned result will be the
#             output of all commands.
#         "return-stderr":
#             Array reference of variable where the output on STDERR of the
#             command is returned to the caller.
#             If multiple commands are sent then the returned output will be the
#             output of all commands.
#             If this is not specified and "discard-stderr" is also not specified
#             then STDERR is redirected to STDOUT and passed back to the user as
#             normal output from the command.
#         "stop-on-error":
#             If specified and multiple commands are specified with key "commands"
#             and an error occurs then execution of remaining commands after the
#             failed command will not be done.
#             The default is to always execute all commands even if there are
#             failures, but the final return code will be the return code of
#             the last failed command.
#
# Example:
# my @result = ();
# my $rc = CBA::Cluster_Operations::send_command_cliss(
#     {
#         "command"       => "show ManagedElement=1",
#         "hide-output"   => 1,
#         "return-output" => \@result,
#     }
# );
#
# my @result = ();
# my $rc = General::OS_Operations::send_command_cliss(
#     {
#         "commands"            => [
#             "show ManagedElement=1",
#             "show-config",
#             "exit",
#         ],
#         "hide-output"         => 1,
#         "command-in-output"   => 1,
#         "return-output"       => \@result,
#     }
# );
#
#
# Return values:
#    Return code 0 = The call was successful.
#                1 = The call failed for some reason.
#                x = Any other value returned by the sent command.
#
# -----------------------------------------------------------------------------
sub send_command_cliss {
    my %params = %{$_[0]};

    # Initialize local variables
    my $active_host;
    my $command           = exists $params{"command"} ? $params{"command"} : undef;
    my @commands          = exists $params{"commands"} ? @{$params{"commands"}} : ();
    my $current_host;
    my $error_detected    = 0;
    my $fh;
    my $filename;
    my $ignore_errors     = exists $params{"ignore-errors"} ? $params{"ignore-errors"} : 0;
    my %options;
    my $rc                = 0;
    my @result            = ();
    my $return_output     = exists $params{"return-output"} ? $params{"return-output"} : undef;
    my $template;

    # Check that we have a command to send
    if (defined $command) {
        # One command specified
        if (@commands) {
            print "You have to specify either 'command' or 'commands', not both\n";
            return 1;
        } else {
            push @commands, $command;
        }
    } elsif (@commands) {
        # Multiple commands specified
    } else {
        # No commands specified
        print "You have to specify a command to send, with 'command' or 'commands'\n";
        return 1;
    }

    # Check that we have an exit; command at the end, otherwise the command
    # sending will be hanging waiting for a timeout.
    unless ($commands[-1] =~ /^exit$/i) {
        push @commands, "exit";
    }

    # Create a temporary file to be used for sending the commands
    %options = (
        DIR => dirname $General::OS_Operations::temporary_output_filename,
        SUFFIX => ".cmd",
        UNLINK => 1,
    );
    $template = "cliss_command_file_XXXX";
    ($fh, $filename) = tempfile( $template, %options);

    # Write commands to the file
    for (@commands) {
        print $fh "$_\n";
    }
    close $fh;

    # Check if the current host is the active controller for the COM process.
    # If not then ssh is needed to the active controller
    $active_host = get_dsc_active_controller("COM", 1);
    $current_host = get_this_hostname(1);
    # Create a new command that will execute the created command file
    if ($current_host eq $active_host) {
        $command = "cat $filename | /opt/com/bin/cliss -b -s";
    } else {
        $command = "ssh $active_host \"cat $filename | /opt/com/bin/cliss -b -s\"";
    }
    $params{'command'} = $command;
    delete $params{'commands'};

    # Always return the output to us so we can check for errors since
    # the signalling manager doesn't update the return code when a command
    # fails.
    unless (defined $return_output) {
        $params{'return-output'} = \@result;
    }

    $rc = General::OS_Operations::send_command( \%params );

    # Check for errors in the output
    if ($ignore_errors == 0) {
        for (@{$params{'return-output'}}) {
            if (/^ERROR: .*/) {
                $error_detected = 1;
                last;
            }
        }
    }

    # Delete the command file
    unlink $filename;

    # Return a proper return code.
    # If command failed return that error code,
    # but if command was successful but cliss printed
    # an error then return 1 as failre.
    # If not error was detected return 0 for success.
    if ($rc != 0) {
        return $rc;
    } elsif ($error_detected) {
        return 1;
    } else {
        return 0;
    }
}

# -----------------------------------------------------------------------------
# Send a command to the Signalling Manager CLI with the option to hide the
# output to the user and to log output to a detailed log file. It can also
# return the collected output to the user.
#
# Input variables:
#    - hash reference %params = Input parameters to the sub routine which can be
#      any of the following keys:
#         "command":
#             Command to send to the signalling manager CLI as a scalar string
#             value.
#         "commands":
#             An array reference of commands to send to the signalling manager
#             CLI as a scalar string value.
#         "command-in-output":
#             Include the executed command in returned output when value =1 or
#             don't include it when value =0 or when not specified.
#             This parameter is probably only useful when you execute multiple
#             commands and want to look for the start of a command in the output.
#             The default is to not include the executed command in the returned
#             output.
#         "discard-stderr":
#             If specified then STDERR output is redirected to /dev/null and thus
#             discarded.
#             The default is to always redirect STDERR to STDOUT.
#         "hide-command":
#             Hide the command from the user when value =1 and don't hide it when
#             value =0 or when not specified.
#             The default is to always show the executed command to the user.
#         "hide-output":
#             Hide the output from the command from the user when value =1 and
#             don't hide output when value =0 or when not specified.
#             The default is to always show the executed command and it's result
#             to the user.
#         "ignore-errors":
#             Ignore errors reported by the command or commands and always return
#             successful return code to the caller when value =1 or return back
#             failure code to the caller if a command failed when value =0 or
#             when not specified.
#             The default is to always return back an error if a command failed.
#         "no-command-timestamp":
#             Do not show a timestamp to the user when a command is executed when
#             value =1 or show it when value =0 or not specified.
#             The default is to always show the timestamp to the user when a
#             command is executed.
#         "raw-output":
#             If specified then what is returned is exactly what is received from
#             the node including CR LF.
#             By default CR and LF are removed.
#         "return-output":
#             Array reference of variable where the result of the command is
#             returned to the caller.
#             If multiple commands are sent then the returned result will be the
#             output of all commands.
#         "return-stderr":
#             Array reference of variable where the output on STDERR of the
#             command is returned to the caller.
#             If multiple commands are sent then the returned output will be the
#             output of all commands.
#             If this is not specified and "discard-stderr" is also not specified
#             then STDERR is redirected to STDOUT and passed back to the user as
#             normal output from the command.
#         "stop-on-error":
#             If specified and multiple commands are specified with key "commands"
#             and an error occurs then execution of remaining commands after the
#             failed command will not be done.
#             The default is to always execute all commands even if there are
#             failures, but the final return code will be the return code of
#             the last failed command.
#
# Example:
# my @result = ();
# my $rc = CBA::Cluster_Operations::send_command_signmcli(
#     {
#         "command"       => "printall;",
#         "hide-output"   => 1,
#         "return-output" => \@result,
#     }
# );
#
# my @result = ();
# my $rc = General::OS_Operations::send_command_signmcli(
#     {
#         "commands"            => [
#             "expert:mode=on;",
#             "printall;",
#             "exit;",
#         ],
#         "hide-output"         => 1,
#         "command-in-output"   => 1,
#         "return-output"       => \@result,
#     }
# );
#
#
# Return values:
#    Return code 0 = The call was successful.
#                1 = The call failed for some reason.
#                x = Any other value returned by the sent command.
#
# -----------------------------------------------------------------------------
sub send_command_signmcli {
    my %params = %{$_[0]};

    # Initialize local variables
    my $command           = exists $params{"command"} ? $params{"command"} : undef;
    my @commands          = exists $params{"commands"} ? @{$params{"commands"}} : ();
    my $error_detected    = 0;
    my $fh;
    my $filename;
    my $ignore_errors     = exists $params{"ignore-errors"} ? $params{"ignore-errors"} : 0;
    my $lock_file         = "/storage/system/config/ss7caf-ana90137/etc/signmgr.lck";
    my %options;
    my $rc                = 0;
    my @result            = ();
    my $return_output     = exists $params{"return-output"} ? $params{"return-output"} : undef;
    my $template;

    # Check that we have a command to send
    if (defined $command) {
        # One command specified
        if (@commands) {
            print "You have to specify either 'command' or 'commands', not both\n";
            return 1;
        } else {
            push @commands, $command;
        }
    } elsif (@commands) {
        # Multiple commands specified
    } else {
        # No commands specified
        print "You have to specify a command to send, with 'command' or 'commands'\n";
        return 1;
    }

    # Check that we have an exit; command at the end, otherwise the command
    # sending will be hanging waiting for a timeout.
    unless ($commands[-1] =~ /^(exit;|exit)$/i) {
        push @commands, "exit;";
    }

    # Create a temporary file to be used for sending the commands
    %options = (
        DIR => dirname $General::OS_Operations::temporary_output_filename,
        SUFFIX => ".cmd",
        UNLINK => 1,
    );
    $template = "signmcli_command_file_XXXX";
    ($fh, $filename) = tempfile( $template, %options);

    # Write commands to the file
    for (@commands) {
        print $fh "$_\n";
    }
    close $fh;

    # Check if we have a signmgr.lck lock file which will prevent the
    # signaling manager from starting properly.
    # If there is a file then we remove it since nobody else should
    # be doing things on the node when this subroutine is called.
    if (-f "$lock_file") {
        unlink $lock_file;
    }

    # Create a new command that will execute the created command file
    $command = "cat $filename | /opt/dsc/cli/dsc-connect-signalling-mgr -batch=yes";
    $params{'command'} = $command;
    delete $params{'commands'};

    # Always return the output to us so we can check for errors since
    # the signalling manager doesn't update the return code when a command
    # fails.
    unless (defined $return_output) {
        $params{'return-output'} = \@result;
    }

    $rc = General::OS_Operations::send_command( \%params );

    # Check for errors in the output
    if ($ignore_errors == 0) {
        for (@{$params{'return-output'}}) {
            if (/^ERROR.*/) {
                $error_detected = 1;
                last;
            }
        }
    }

    # Delete the command file
    unlink $filename;

    # Return a proper return code.
    # If command failed return that error code,
    # but if command was successful but signalling manager printed
    # an error then return 1 as failre.
    # If not error was detected return 0 for success.
    if ($rc != 0) {
        return $rc;
    } elsif ($error_detected) {
        return 1;
    } else {
        return 0;
    }
}

# -----------------------------------------------------------------------------
# Send a command to the Linux OS but to a specific blade with the option to hide the
# output to the user and to log output to a detailed log file. It can also
# return the collected output to the user.
#
# Input variables:
#    - hash reference %params = Input parameters to the sub routine which can be
#      any of the following keys:
#         "command":
#             Command to send to the Linux OS but to a specific blade as a
#             scalar string value.
#         "commands":
#             An array reference of commands to send to the Linux OS but to a
#             specific blade as a scalar string value.
#         "command-in-output":
#             Include the executed command in returned output when value =1 or
#             don't include it when value =0 or when not specified.
#             This parameter is probably only useful when you execute multiple
#             commands and want to look for the start of a command in the output.
#             The default is to not include the executed command in the returned
#             output.
#         "discard-stderr":
#             If specified then STDERR output is redirected to /dev/null and thus
#             discarded.
#             The default is to always redirect STDERR to STDOUT.
#         "hide-command":
#             Hide the command from the user when value =1 and don't hide it when
#             value =0 or when not specified.
#             The default is to always show the executed command to the user.
#         "hide-output":
#             Hide the output from the command from the user when value =1 and
#             don't hide output when value =0 or when not specified.
#             The default is to always show the executed command and it's result
#             to the user.
#         "ignore-errors":
#             Ignore errors reported by the command or commands and always return
#             successful return code to the caller when value =1 or return back
#             failure code to the caller if a command failed when value =0 or
#             when not specified.
#             The default is to always return back an error if a command failed.
#         "no-command-timestamp":
#             Do not show a timestamp to the user when a command is executed when
#             value =1 or show it when value =0 or not specified.
#             The default is to always show the timestamp to the user when a
#             command is executed.
#         "raw-output":
#             If specified then what is returned is exactly what is received from
#             the node including CR LF.
#             By default CR and LF are removed.
#         "return-output":
#             Array reference of variable where the result of the command is
#             returned to the caller.
#             If multiple commands are sent then the returned result will be the
#             output of all commands.
#         "return-stderr":
#             Array reference of variable where the output on STDERR of the
#             command is returned to the caller.
#             If multiple commands are sent then the returned output will be the
#             output of all commands.
#             If this is not specified and "discard-stderr" is also not specified
#             then STDERR is redirected to STDOUT and passed back to the user as
#             normal output from the command.
#         "stop-on-error":
#             If specified and multiple commands are specified with key "commands"
#             and an error occurs then execution of remaining commands after the
#             failed command will not be done.
#             The default is to always execute all commands even if there are
#             failures, but the final return code will be the return code of
#             the last failed command.
#         "target-blade":
#             If specified then the commands will be sent to the specific blade
#             one by one using SSH if needed. This means that a command cannot
#             depend on the results from a previous command.
#             Allowed values for the "target-blade" parameter can be one of the
#             following:
#
#               "active-controller":
#               The command will be sent to the blade which is currently running
#               the COM process.
#
#               "SC-1":
#               "SC-2":
#               "PL-3":
#               "PL-4":
#                 etc.
#               The command will be sent to the specific blade.
#
#               "all":
#               The command will be send to all blades.
#               NOTE: With the current implementation that is using the DSC 'all'
#               command, the exit code will not show failures of any command.
#
#               "pl":
#               The command will be send to all payload blades.
#               NOTE: With the current implementation that is using the DSC 'pl'
#               command, the exit code will not show failures of any command.
#
#               "sc":
#               The command will be send to all controller blades.
#               NOTE: With the current implementation that is using the DSC 'sc'
#               command, the exit code will not show failures of any command.
#
# Example:
# my @result = ();
# my $rc = CBA::Cluster_Operations::send_command_targetblade(
#     {
#         "command"       => "hostname",
#         "hide-output"   => 1,
#         "return-output" => \@result,
#         "target-blade"  => "SC-1",
#     }
# );
#
# my @result = ();
# my $rc = General::OS_Operations::send_command_targetblade(
#     {
#         "commands"            => [
#             "hostname",
#             "ifconfig",
#             "df -h",
#         ],
#         "hide-output"         => 1,
#         "command-in-output"   => 1,
#         "return-output"       => \@result,
#         "target-blade"        => "activecontroller",
#     }
# );
#
#
# Return values:
#    Return code 0 = The call was successful.
#                1 = The call failed for some reason.
#                x = Any other value returned by the sent command.
#
# -----------------------------------------------------------------------------
sub send_command_targetblade {
    my %params = %{$_[0]};

    # Initialize local variables
    my $active_host;
    my $command           = exists $params{"command"} ? $params{"command"} : undef;
    my $command_prefix = "";
    my $command_suffix = "";
    my @commands          = exists $params{"commands"} ? @{$params{"commands"}} : ();
    my $current_host;
    my $error_detected    = 0;
    my $fh;
    my $filename;
    my %options;
    my $rc                = 0;
    my @result            = ();
    my $return_output     = exists $params{"return-output"} ? $params{"return-output"} : undef;
    my $target_blade      = exists $params{"target-blade"} ? $params{"target-blade"} : "";
    my $template;

    # Check that we have a command to send
    if (defined $command) {
        # One command specified
        if (@commands) {
            print "You have to specify either 'command' or 'commands', not both\n";
            return 1;
        } else {
            push @commands, $command;
            delete $params{'command'};
        }
    } elsif (@commands) {
        # Multiple commands specified
    } else {
        # No commands specified
        print "You have to specify a command to send, with 'command' or 'commands'\n";
        return 1;
    }

    # Check that valid values for target-blade has been specified and modify
    # the command prefix and suffix accordingly.
    $current_host = get_this_hostname(1);
    if ($target_blade) {
        if ($target_blade !~ /^(sc-\d|pl-\d+|activecontroller|sc|pl|all)$/i) {
            print "Wrong value for 'target-blade' specified (=$target_blade)\nOnly SC-1, SC-2, PL-3, PL-4..., activecontroller, all, sc and pl is allowed\n";
            return 1;
        }
        if ($target_blade =~ /^activecontroller$/i) {
            $active_host = get_dsc_active_controller("COM", 1);
            if ($current_host ne $active_host) {
                $command_prefix = "ssh $active_host \"";
                $command_suffix = "\"";
            }
        } elsif ($target_blade =~ /^all$/i) {
            $command_prefix = "/opt/dsc/cli/all ";
            $command_suffix = "";
        } elsif ($target_blade =~ /^sc$/i) {
            $command_prefix = "/opt/dsc/cli/sc ";
            $command_suffix = "";
        } elsif ($target_blade =~ /^pl$/i) {
            $command_prefix = "/opt/dsc/cli/pl ";
            $command_suffix = "";
        } elsif ($target_blade =~ /^(sc-\d|pl-\d+)$/i) {
            if (uc($current_host) ne uc($target_blade)) {
                # Target blade is not the same blade as current blade
                $command_prefix = "ssh $target_blade \"";
                $command_suffix = "\"";
            } else {
                # Target blade is the same blade as current blade
                $command_prefix = "";
                $command_suffix = "";
            }
        } else {
            # We should not end up here but just in case we report it as an error
            print "Wrong value for 'target-blade' specified (=$target_blade)\nOnly SC-1, SC-2, PL-3, PL-4..., activecontroller, all, sc and pl is allowed\nThis is a coding fault because we should not end up here\n";
            return 1;
        }
    } else {
        # No target blade is specified
        $command_prefix = "";
        $command_suffix = "";
    }

    # Modify the commands with prefix and suffix
    for (@commands) {
        s/^(.+)$/$command_prefix$1$command_suffix/;
    }

    # Create a new command array that will be executed
    $params{'commands'} = \@commands;

    $rc = General::OS_Operations::send_command( \%params );

    return $rc;
}

1;
