package Playlist::011_Deploy_With_Traffic;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.21
#  Date     : 2024-06-03 17:38:21
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2023-2024
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
use File::Basename qw(dirname basename);

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    parse_network_config_parameters
    usage
    usage_return_playlist_variables
    );

#
# Used Perl package files
#

use General::File_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::001_Deploy_SC;
use Playlist::003_Undeploy_SC;
use Playlist::004_Config_Management;
use Playlist::005_User_Management;
use Playlist::007_Certificate_Management;
use Playlist::102_Supreme_Certificate_Management;
use Playlist::103_Tools_Management;
use Playlist::106_Traffic_Tools_Remove;
use Playlist::107_Traffic_Tools_Configure_And_Start;
use Playlist::108_Miscellaneous_Tasks;
use Playlist::109_Traffic_Tools_Install;
use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::920_Check_Network_Config_Information;

#
# Variable Declarations
#

my $debug_command = "";

my %playlist_variables;
set_playlist_variables();

# -----------------------------------------------------------------------------
# Playlist logic.
#
# Input variables:
#  -
#
# Output variables:
#  -
#
# Return values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub main {

    my $rc;

    # Set job type to be a deployment
    $::JOB_PARAMS{'JOBTYPE'} = "DEPLOY_WITH_TRAFFIC";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    } else {
        $debug_command = "";
    }

    $::JOB_PARAMS{'PLAYLIST_CNT'} = 1 unless (exists $::JOB_PARAMS{'PLAYLIST_CNT'});
    $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR_CREATED'} = "no" unless (exists $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR_CREATED'});

    # If the playlist needs to have access to Network Configuration parameters
    # then it needs to check for this and give an error or ask the user to
    # provide the file name.
    if (Playlist::920_Check_Network_Config_Information::is_network_config_specified() == 0) {
        parse_network_config_parameters();
    } else {
        General::Logging::log_user_error_message("You must specify a network configuration file for this Playlist to run");
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    # Read package specific parameters in file 'package.config' and store as $::JOB_PARAMS
    if (-f "$::JOB_PARAMS{'SOFTWARE_DIR'}/package.config") {
        General::Playlist_Operations::job_parameters_read("$::JOB_PARAMS{'SOFTWARE_DIR'}/package.config", \%::JOB_PARAMS);
    }

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P011S01, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P011S02, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Prepare_Remote_Nodes_P011S03, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Download_Software_P011S04, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Clean_Registry_P011S05, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Remove_Node_Labels_And_Taints_P011S06, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Undeploy_Tools_P011S07, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Undeploy_SC_P011S08, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Reserve_Workers_P011S09, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Deploy_SC_P011S10, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Add_Users_P011S11, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Generate_Install_Certificates_For_SC_P011S12, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Deploy_Tools_P011S13, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Configure_Traffic_P011S14, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Start_Traffic_P011S15, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P011S16, \&Fallback001_P011S99 );
    return $rc if $rc < 0;

    return $rc;
}

####################
#                  #
# Step Definitions #
#                  #
####################

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Initialize_Job_Environment_P011S01 {

    my $rc;

    # We don't need to do these checks on the current server.
    $::JOB_PARAMS{'SKIP_DOCKER_AND_NERDCTL_CHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_HELM_CHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_KUBECTL_CHECK'} = "yes";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::901_Initialize_Job_Environment::main } );
    return $rc if $rc < 0;

    return $rc;
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Check_Job_Parameters_P011S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P011S02T01 } );
    return $rc if $rc < 0;

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Check_Job_Parameters_P011S02T01 {

        my $message = "";
        my $rc = 0;
        my @result;
        my @std_error;
        my $var_name_config;
        my $var_name_job;

        # Get the proper NODE_VIP_SC value
        $var_name_config = "cluster_ssh_vip";
        $var_name_job = "NODE_VIP_SC";
        General::Logging::log_user_message("Checking Job parameter '$var_name_job' and Network Config parameter '$var_name_config'");
        if ($::JOB_PARAMS{$var_name_job} ne "") {
            # Already set
        } elsif (exists $::NETWORK_CONFIG_PARAMS{$var_name_config}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{$var_name_config}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{$var_name_job} = $::NETWORK_CONFIG_PARAMS{$var_name_config}{'value'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter '$var_name_config' has not been set and Job parameter '$var_name_job' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter '$var_name_job', nor Network Config parameter '$var_name_config' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }
        $message .= "$var_name_job=$::JOB_PARAMS{$var_name_job}\n";
        $::JOB_PARAMS{'HOSTNAME_SC'} = fetch_remote_host_name($::JOB_PARAMS{$var_name_job});

        # Get the proper NODE_VIP_TOOLS value
        $var_name_config = "cluster_ssh_vip_tools";
        $var_name_job = "NODE_VIP_TOOLS";
        General::Logging::log_user_message("Checking Job parameter '$var_name_job' and Network Config parameter '$var_name_config'");
        if ($::JOB_PARAMS{$var_name_job} ne "") {
            # Already set
        } elsif (exists $::NETWORK_CONFIG_PARAMS{$var_name_config}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{$var_name_config}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{$var_name_job} = $::NETWORK_CONFIG_PARAMS{$var_name_config}{'value'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter '$var_name_config' has not been set and Job parameter '$var_name_job' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter '$var_name_job', nor Network Config parameter '$var_name_config' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }
        $message .= "$var_name_job=$::JOB_PARAMS{$var_name_job}\n";
        $::JOB_PARAMS{'HOSTNAME_TOOLS'} = fetch_remote_host_name($::JOB_PARAMS{$var_name_job});

        # Get the proper NODE_VIP_TOOLS value
        $var_name_job = "NODE_JOB_WORKSPACE_DIR";
        General::Logging::log_user_message("Checking Job parameter '$var_name_job'");
        if (exists $::JOB_PARAMS{$var_name_job} && $::JOB_PARAMS{$var_name_job} ne "") {
            # Already set by the user on the command line, should not really happen because it's not documented but handled anyway
        } elsif ($::JOB_PARAMS{'_JOB_WORKSPACE_DIR'} =~ /^.+\/(\S+)$/) {
            # Extract just the last part of the directory name, not the path
            $::JOB_PARAMS{$var_name_job} = "$::JOB_PARAMS{'NODE_WORKSPACE_DIR'}/$1";
        } else {
            # Should not happen, but just in case
            my $temp_name = sprintf "Job_Started_From_Remote_Host_%s_%s", $::JOB_PARAMS{'DAFT_HOSTNAME'}, time();
            $::JOB_PARAMS{$var_name_job} = "$::JOB_PARAMS{'NODE_WORKSPACE_DIR'}/$temp_name";
        }
        $message .= "$var_name_job=$::JOB_PARAMS{$var_name_job}\n";

        # Get the proper KUBECONFIG value
        General::Logging::log_user_message("Checking Job parameter 'KUBECONFIG'");
        if ($::JOB_PARAMS{'KUBECONFIG'} eq "") {
            if (-f "$ENV{'HOME'}/.kube/config") {
                $::JOB_PARAMS{'KUBECONFIG'} = "$ENV{'HOME'}/.kube/config";
                $::JOB_PARAMS{'KUBECONFIG_SC'} = "$ENV{'HOME'}/.kube/config";
            } else {
                General::Logging::log_user_error_message("The Job parameter 'KUBECONFIG' has not been set and the file $ENV{'HOME'}/.kube/config does not exist");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            $::JOB_PARAMS{'KUBECONFIG_SC'} = $::JOB_PARAMS{'KUBECONFIG'};
        }
        $message .= "KUBECONFIG=$::JOB_PARAMS{'KUBECONFIG'}\n";
        $message .= "KUBECONFIG_SC=$::JOB_PARAMS{'KUBECONFIG_SC'}\n";
        if ($::JOB_PARAMS{'KUBECONFIG'} =~ /^.+\/(\S+)$/) {
            $::JOB_PARAMS{"NODE_KUBECONFIG_SC"} = "$::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/kubeconfig_files/sc_$1";
            $message .= sprintf "NODE_KUBECONFIG_SC=%s\n", $::JOB_PARAMS{'NODE_KUBECONFIG_SC'};
        } else {
            # Should not happen
            General::Logging::log_user_error_message("Wrong format of Job parameter 'KUBECONFIG'");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        if ($::JOB_PARAMS{'KUBECONFIG_TOOLS'} eq "") {
            $::JOB_PARAMS{'KUBECONFIG_TOOLS'} = $::JOB_PARAMS{'KUBECONFIG'};
        }

        # Get the proper KUBECONFIG_SC value
        $var_name_job = "KUBECONFIG_TOOLS";
        General::Logging::log_user_message("Checking Job parameter '$var_name_job'");
        if ($::JOB_PARAMS{$var_name_job} eq "") {
            if (-f "$ENV{'HOME'}/.kube/config") {
                $::JOB_PARAMS{$var_name_job} = "$ENV{'HOME'}/.kube/config";
            } else {
                General::Logging::log_user_error_message("The Job parameter '$var_name_job' has not been set and the file $ENV{'HOME'}/.kube/config does not exist");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        }
        $message .= "$var_name_job=$::JOB_PARAMS{$var_name_job}\n";
        if ($::JOB_PARAMS{$var_name_job} =~ /^.+\/(\S+)$/) {
            $::JOB_PARAMS{"NODE_$var_name_job"} = "$::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/kubeconfig_files/tools_$1";
            $message .= sprintf "NODE_$var_name_job=%s\n", $::JOB_PARAMS{"NODE_$var_name_job"};
        } else {
            # Should not happen
            General::Logging::log_user_error_message("Wrong format of Job parameter '$var_name_job'");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        if ($::JOB_PARAMS{'NODE_VIP_SC'} ne $::JOB_PARAMS{'NODE_VIP_TOOLS'}) {
            # We will be using two different nodes for SC and TOOLS deployments.
            # Check that also the KUBECONFIG files are different.
            if ($::JOB_PARAMS{'KUBECONFIG'} eq $::JOB_PARAMS{'KUBECONFIG_TOOLS'}) {
                General::Logging::log_user_error_message("Since NODE_VIP_SC and NODE_VIP_TOOLS are different then also KUBECONFIG and KUBECONFIG_TOOLS must be different");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } elsif ($::JOB_PARAMS{'KUBECONFIG'} ne $::JOB_PARAMS{'KUBECONFIG_TOOLS'}) {
            # We will be using two different KUBECONFIG files for SC and TOOLS deployments but the NODE_VIP addresses are the same
            General::Logging::log_user_error_message("Since KUBECONFIG and KUBECONFIG_TOOLS are different then also NODE_VIP_SC and NODE_VIP_TOOLS must be different");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        if ($::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE'} =~ /^(\d+)(\D+)$/) {
            $::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_SIZE'} = $1;
            $::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} = $2;
        } else {
            $::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_SIZE'} = 30000;
            $::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} = "MB";
        }

        # Get the proper NODE_SOFTWARE_DIR value
        $var_name_job = "NODE_SOFTWARE_DIR";
        General::Logging::log_user_message("Checking Job parameter '$var_name_job'");
        if ($::JOB_PARAMS{$var_name_job} eq "") {
            # We need to download the software in this job and then transfer it to the node(s).
            # So we need to check that there is enough space for the package.
            General::Logging::log_user_message("Check that there is enough space ($::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_SIZE'}$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'}) on this server for downloading the software package");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "df -B 1$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to check available disk space");
                return General::Playlist_Operations::RC_FALLBACK;
            }
            my $available = 0;
            for (@result) {
                if (/^\S+\s+\d+\s+\d+\s+(\d+)\s+\d+\%\s+.+/) {
                    #Filesystem     1kB-blocks     Used Available Use% Mounted on
                    #/dev/vda3       107353547 85885479  21468068  81% /
                    $available = $1;
                }
            }
            if ($available < $::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_SIZE'}) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("There is not enough free disk space to download the software.\nWe need $::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_SIZE'}$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} but only $available$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} is available\n");
                return General::Playlist_Operations::RC_FALLBACK;
            }
            General::Logging::log_user_message("There is enough free disk space to download the software.\nWe need $::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_SIZE'}$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} and $available$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} is available\n");

            # Check NODE_VIP_SC node
            General::Logging::log_user_message("Check that there is enough space ($::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_SIZE'}$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'}) on the SC server $::JOB_PARAMS{'NODE_VIP_SC'} for uploading the software package");
            $rc = General::OS_Operations::send_command_via_ssh(
                {
                    "commands"            => [ "mkdir -p $::JOB_PARAMS{'NODE_WORKSPACE_DIR'}", "df -B 1$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} $::JOB_PARAMS{'NODE_WORKSPACE_DIR'}" ],
                    "expect-debug"        => 0,
                    "hide-output"         => 1,
                    "ip"                  => $::JOB_PARAMS{'NODE_VIP_SC'},
                    "port"                => 22,
                    "password"            => "notneeded",
                    "return-output"       => \@result,
                    "return-stderr"       => \@std_error,
                    "stop-on-error"       => 1,
                    "timeout"             => -1,    # No timeout
                    "user"                => "eccd",
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to check available disk space on the node: $::JOB_PARAMS{'NODE_VIP_SC'}");
                return 1;
            }
            $available = 0;
            for (@result) {
                if (/^\S+\s+\d+\s+\d+\s+(\d+)\s+\d+\%\s+.+/) {
                    #Filesystem     1kB-blocks     Used Available Use% Mounted on
                    #/dev/vda3       107353547 85885479  21468068  81% /
                    $available = $1;
                }
            }
            if ($available < $::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_SIZE'}) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("There is not enough free disk space to upload the software.\nWe need $::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_SIZE'}$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} but only $available$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} is available\n");
                return General::Playlist_Operations::RC_FALLBACK;
            }
            General::Logging::log_user_message("There is enough free disk space to upload the software.\nWe need $::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_SIZE'}$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} and $available$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} is available\n");

            if ($::JOB_PARAMS{'NODE_VIP_SC'} ne $::JOB_PARAMS{'NODE_VIP_TOOLS'}) {
                # Check NODE_VIP_TOOLS node
                General::Logging::log_user_message("Check that there is enough space ($::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_SIZE'}$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'}) on the SC server $::JOB_PARAMS{'NODE_VIP_TOOLS'} for uploading the software package");
                $rc = General::OS_Operations::send_command_via_ssh(
                    {
                        "commands"            => [ "mkdir -p $::JOB_PARAMS{'NODE_WORKSPACE_DIR'}", "df -B 1$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} $::JOB_PARAMS{'NODE_WORKSPACE_DIR'}" ],
                        "expect-debug"        => 0,
                        "hide-output"         => 1,
                        "ip"                  => $::JOB_PARAMS{'NODE_VIP_TOOLS'},
                        "port"                => 22,
                        "password"            => "notneeded",
                        "return-output"       => \@result,
                        "return-stderr"       => \@std_error,
                        "stop-on-error"       => 1,
                        "timeout"             => -1,    # No timeout
                        "user"                => "eccd",
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to check available disk space on the node: $::JOB_PARAMS{'NODE_VIP_TOOLS'}");
                    return 1;
                }
                $available = 0;
                for (@result) {
                    if (/^\S+\s+\d+\s+\d+\s+(\d+)\s+\d+\%\s+.+/) {
                        #Filesystem     1kB-blocks     Used Available Use% Mounted on
                        #/dev/vda3       107353547 85885479  21468068  81% /
                        $available = $1;
                    }
                }
                if ($available < $::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_SIZE'}) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("There is not enough free disk space to upload the software.\nWe need $::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_SIZE'}$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} but only $available$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} is available\n");
                    return General::Playlist_Operations::RC_FALLBACK;
                }
                General::Logging::log_user_message("There is enough free disk space to upload the software.\nWe need $::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_SIZE'}$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} and $available$::JOB_PARAMS{'MINIMUM_NEEDED_DISK_SPACE_UNIT'} is available\n");
            }
        }

        if ($::JOB_PARAMS{'DEPLOY_UCC'} eq "yes") {
            # We need special handling for the Ultra Compact Core case.
            if (exists $::JOB_PARAMS{'CONFIG_FILE_HELM_CHART_1'} && $::JOB_PARAMS{'CONFIG_FILE_HELM_CHART_1'} ne "") {
                # The user has already specified this parameter, so let's use it as-is and
                # we assume that the user knows what they are doing.
            } else {
                # The user didn't specify the parameter or it's empty, so we use the DAFT template file.
                $::JOB_PARAMS{'CONFIG_FILE_HELM_CHART_1'} = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/values_files/ucc_sepp.yaml";
                $message .= "CONFIG_FILE_HELM_CHART_1=$::JOB_PARAMS{'CONFIG_FILE_HELM_CHART_1'}\n";
            }

            if (exists $::JOB_PARAMS{'ENABLED_CNF'} && $::JOB_PARAMS{'ENABLED_CNF'} ne "") {
                # The user has already specified this parameter, so let's use it as-is and
                # we assume that the user knows what they are doing if they don't specify sepp.
            } else {
                # The user didn't specify the parameter or it's empty, so we change it to 'sepp'.
                $::JOB_PARAMS{'ENABLED_CNF'} = "sepp";
                $message .= "ENABLED_CNF=$::JOB_PARAMS{'ENABLED_CNF'}\n";
            }

            $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} .= "|UCC1" if ($::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} !~ /UCC(1|2)/);
            if ($::JOB_PARAMS{'SKIP_RESERVE_WORKERS_FOR_UCC'} eq "no" && $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} !~ /NODESELECTOR(1|2)/) {
                # 'UCC' not specified as network config option, so add it at the end
                $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} .= "|NODESELECTOR1";
            }
            $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} =~ s/^\|//;     # Remove preceeding |
            $message .= "NETWORK_CONFIG_OPTIONS=$::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'}\n";
        }

        if ($::JOB_PARAMS{'SKIP_ALL'} eq "yes") {
            # We need to mark all SKIP_xxxx from this playlist with "yes", except for the ones specifically set by the
            # user on the command line.
            $message .= "\nUpdated because of SKIP_ALL=yes:\n";
            for my $variable (sort keys %playlist_variables) {
                next if ($variable eq "SKIP_ALL");
                next unless ($variable =~ /^SKIP_\S+/);

                if (exists $::JOB_PARAMS{$variable} && $::playlist_variables{$variable}{'default_value_used'} eq "yes") {
                    # The user did not specify the variable on the command line, so set it to "yes" to skip this task
                    $::JOB_PARAMS{$variable} = "yes";
                }
                $message .= "$variable=$::JOB_PARAMS{$variable}\n";
            }
        }

        General::Logging::log_user_message("Used values:\n$message");

        return $rc;
    }
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Prepare_Remote_Nodes_P011S03 {

    my $rc = 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Node_Directories_P011S03T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_And_Upload_DAFT_If_Needed_P011S03T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Upload_Network_Config_File_P011S03T03 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Upload_KUBECONFIG_Files_P011S03T04 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Upload_Config_Files_If_User_Specified_P011S03T05 } );
    return $rc if $rc < 0;

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Create_Node_Directories_P011S03T01 {

        my @commands;
        my $rc = 0;
        my @result;
        my @std_error;

        push @commands, "${debug_command}mkdir -p $::JOB_PARAMS{'NODE_DAFT_DIR'}" if ($::JOB_PARAMS{'NODE_DAFT_DIR'} ne "");
        push @commands, "${debug_command}mkdir -p $::JOB_PARAMS{'NODE_SOFTWARE_DIR'}" if ($::JOB_PARAMS{'NODE_SOFTWARE_DIR'} ne "");
        push @commands, "${debug_command}mkdir -p $::JOB_PARAMS{'NODE_WORKSPACE_DIR'}" if ($::JOB_PARAMS{'NODE_WORKSPACE_DIR'} ne "");
        push @commands, "${debug_command}mkdir -p $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/config_files";
        push @commands, "${debug_command}mkdir -p $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/daft";
        push @commands, "${debug_command}mkdir -p $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/kubeconfig_files";
        push @commands, "${debug_command}mkdir -p $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/playlist_logs";
        push @commands, "${debug_command}mkdir -p $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/software";

        # Update NODE_VIP_SC node
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => \@commands,
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $::JOB_PARAMS{'NODE_VIP_SC'},
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to create directories on the node: $::JOB_PARAMS{'NODE_VIP_SC'}");
            show_warning_message_for_rerun_of_playlist();
            return 1;
        }

        if ($::JOB_PARAMS{'NODE_VIP_SC'} ne $::JOB_PARAMS{'NODE_VIP_TOOLS'}) {
            # Update NODE_VIP_TOOLS node
            $rc = General::OS_Operations::send_command_via_ssh(
                {
                    "commands"            => \@commands,
                    "expect-debug"        => 0,
                    "hide-output"         => 1,
                    "ip"                  => $::JOB_PARAMS{'NODE_VIP_TOOLS'},
                    "port"                => 22,
                    "password"            => "notneeded",
                    "return-output"       => \@result,
                    "return-stderr"       => \@std_error,
                    "stop-on-error"       => 1,
                    "timeout"             => -1,    # No timeout
                    "user"                => "eccd",
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to create directories on the node: $::JOB_PARAMS{'NODE_VIP_TOOLS'}");
                return 1;
            }
        }

        $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR_CREATED'} = "yes";

        return $rc;
    }

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Create_And_Upload_DAFT_If_Needed_P011S03T02 {

        my $command;
        my @commands;
        my $rc = 0;
        my @result;
        my @std_error;
        my $upload_download_csar_file = 1;
        my $upload_adp_collect_file = 1;

        if ($::JOB_PARAMS{'NODE_DAFT_DIR'} ne "") {
            # Use the provided directory

            # Check NODE_VIP_SC node
            General::Logging::log_user_message("Checking that some DAFT files exists on SC node: $::JOB_PARAMS{'NODE_VIP_SC'}");
            $rc = General::OS_Operations::send_command_via_ssh(
                {
                    "commands"            => [ "ls -l $::JOB_PARAMS{'NODE_DAFT_DIR'}/playlist_extensions" ],
                    "expect-debug"        => 0,
                    "hide-output"         => 1,
                    "ip"                  => $::JOB_PARAMS{'NODE_VIP_SC'},
                    "port"                => 22,
                    "password"            => "notneeded",
                    "return-output"       => \@result,
                    "return-stderr"       => \@std_error,
                    "stop-on-error"       => 1,
                    "timeout"             => -1,    # No timeout
                    "user"                => "eccd",
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to find the daft files on the node: $::JOB_PARAMS{'NODE_VIP_SC'}");
                return 1;
            }

            if ($::JOB_PARAMS{'NODE_VIP_SC'} ne $::JOB_PARAMS{'NODE_VIP_TOOLS'}) {
                # Check NODE_VIP_TOOLS node
                General::Logging::log_user_message("Checking that some DAFT files exists on TOOLS node: $::JOB_PARAMS{'NODE_VIP_TOOLS'}");
                $rc = General::OS_Operations::send_command_via_ssh(
                    {
                        "commands"            => [ "ls -l $::JOB_PARAMS{'NODE_DAFT_DIR'}/playlist_extensions" ],
                        "expect-debug"        => 0,
                        "hide-output"         => 1,
                        "ip"                  => $::JOB_PARAMS{'NODE_VIP_TOOLS'},
                        "port"                => 22,
                        "password"            => "notneeded",
                        "return-output"       => \@result,
                        "return-stderr"       => \@std_error,
                        "stop-on-error"       => 1,
                        "timeout"             => -1,    # No timeout
                        "user"                => "eccd",
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to find the daft files on the node: $::JOB_PARAMS{'NODE_VIP_TOOLS'}");
                    return 1;
                }
            }

            General::Logging::log_user_message("No need to upload DAFT Package to remote node(s) directory: $::JOB_PARAMS{'NODE_DAFT_DIR'}");
        } else {
            # Place DAFT under the workspace directory on the node
            $::JOB_PARAMS{'NODE_DAFT_DIR'} = "$::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/daft";

            # Upload the files from the currently used directory using scp
            $command = "${debug_command}scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr";
            if ($::JOB_PARAMS{'NODE_VIP_SC'} =~ /:/) {
                $command .= " -6 $::JOB_PARAMS{'_PACKAGE_DIR'}/* eccd\@[$::JOB_PARAMS{'NODE_VIP_SC'}]:$::JOB_PARAMS{'NODE_DAFT_DIR'}";
            } else {
                $command .= " $::JOB_PARAMS{'_PACKAGE_DIR'}/* eccd\@$::JOB_PARAMS{'NODE_VIP_SC'}:$::JOB_PARAMS{'NODE_DAFT_DIR'}";
            }
            push @commands, $command;

            if ($::JOB_PARAMS{'NODE_VIP_SC'} ne $::JOB_PARAMS{'NODE_VIP_TOOLS'}) {
                $command = "${debug_command}scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr";
                if ($::JOB_PARAMS{'NODE_VIP_TOOLS'} =~ /:/) {
                    $command .= " -6 $::JOB_PARAMS{'_PACKAGE_DIR'}/* eccd\@[$::JOB_PARAMS{'NODE_VIP_TOOLS'}]:$::JOB_PARAMS{'NODE_DAFT_DIR'}";
                } else {
                    $command .= " $::JOB_PARAMS{'_PACKAGE_DIR'}/* eccd\@$::JOB_PARAMS{'NODE_VIP_TOOLS'}:$::JOB_PARAMS{'NODE_DAFT_DIR'}";
                }
                push @commands, $command;
            }

            General::Logging::log_user_message("Uploading DAFT package files to node(s)");
            $rc = General::OS_Operations::send_command(
                {
                    "commands"      => \@commands,
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy the DAFT files to the node(s)");
                return 1;
            }

            General::Logging::log_user_message("DAFT package uploaded to remote node(s) directory: $::JOB_PARAMS{'NODE_DAFT_DIR'}");
        }

        return 0;
    }

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Upload_Network_Config_File_P011S03T03 {

        my $command;
        my @commands;
        my $rc = 0;

        if ($::network_file =~ /^.+\/(\S+\.xml)$/) {
            $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'} = "$::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/$1";
        } else {
            # Should not happen
            General::Logging::log_user_error_message("Incorrect format of network config file: $::network_file");
            return 1;
        }

        # Upload the files from the currently used directory using scp
        $command = "${debug_command}scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr";
        if ($::JOB_PARAMS{'NODE_VIP_SC'} =~ /:/) {
            $command .= " -6 $::network_file eccd\@[$::JOB_PARAMS{'NODE_VIP_SC'}]:$::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}";
        } else {
            $command .= " $::network_file eccd\@$::JOB_PARAMS{'NODE_VIP_SC'}:$::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}";
        }
        push @commands, $command;

        if ($::JOB_PARAMS{'NODE_VIP_SC'} ne $::JOB_PARAMS{'NODE_VIP_TOOLS'}) {
            $command = "${debug_command}scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr";
            if ($::JOB_PARAMS{'NODE_VIP_SC'} =~ /:/) {
                $command .= " -6 $::network_file eccd\@[$::JOB_PARAMS{'NODE_VIP_TOOLS'}]:$::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}";
            } else {
                $command .= " $::network_file eccd\@$::JOB_PARAMS{'NODE_VIP_TOOLS'}:$::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}";
            }
            push @commands, $command;
        }

        General::Logging::log_user_message("Uploading network config file to node(s)");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to copy the network config file file to the node(s)");
            return 1;
        }

        General::Logging::log_user_message("Network config file uploaded to remote node(s) file: $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}/");

        return 0;
    }

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Upload_KUBECONFIG_Files_P011S03T04 {

        my $command_sc;
        my $command_tools;
        my @commands;
        my $rc = 0;

        # Upload the files from the currently used directory using scp
        $command_sc = "${debug_command}scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr";
        $command_tools = "${debug_command}scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr";
        if ($::JOB_PARAMS{'NODE_VIP_SC'} =~ /:/) {
            $command_sc .= " -6 $::JOB_PARAMS{'KUBECONFIG_SC'} eccd\@[$::JOB_PARAMS{'NODE_VIP_SC'}]:$::JOB_PARAMS{'NODE_KUBECONFIG_SC'}";
            $command_tools .= " -6 $::JOB_PARAMS{'KUBECONFIG_TOOLS'} eccd\@[$::JOB_PARAMS{'NODE_VIP_SC'}]:$::JOB_PARAMS{'NODE_KUBECONFIG_TOOLS'}";
        } else {
            $command_sc .= " $::JOB_PARAMS{'KUBECONFIG_SC'} eccd\@$::JOB_PARAMS{'NODE_VIP_SC'}:$::JOB_PARAMS{'NODE_KUBECONFIG_SC'}";
            $command_tools .= " $::JOB_PARAMS{'KUBECONFIG_TOOLS'} eccd\@$::JOB_PARAMS{'NODE_VIP_SC'}:$::JOB_PARAMS{'NODE_KUBECONFIG_TOOLS'}";
        }
        push @commands, $command_sc;
        push @commands, $command_tools;

        if ($::JOB_PARAMS{'NODE_VIP_SC'} ne $::JOB_PARAMS{'NODE_VIP_TOOLS'}) {
            $command_sc = "${debug_command}scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr";
            $command_tools = "${debug_command}scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr";
            if ($::JOB_PARAMS{'NODE_VIP_TOOLS'} =~ /:/) {
                $command_sc .= " -6 $::JOB_PARAMS{'KUBECONFIG_SC'} eccd\@[$::JOB_PARAMS{'NODE_VIP_TOOLS'}]:$::JOB_PARAMS{'NODE_KUBECONFIG_SC'}";
                $command_tools .= " -6 $::JOB_PARAMS{'KUBECONFIG_TOOLS'} eccd\@[$::JOB_PARAMS{'NODE_VIP_TOOLS'}]:$::JOB_PARAMS{'NODE_KUBECONFIG_TOOLS'}";
            } else {
                $command_sc .= " $::JOB_PARAMS{'KUBECONFIG_SC'} eccd\@$::JOB_PARAMS{'NODE_VIP_TOOLS'}:$::JOB_PARAMS{'NODE_KUBECONFIG_SC'}";
                $command_tools .= " $::JOB_PARAMS{'KUBECONFIG_TOOLS'} eccd\@$::JOB_PARAMS{'NODE_VIP_TOOLS'}:$::JOB_PARAMS{'NODE_KUBECONFIG_TOOLS'}";
            }
            push @commands, $command_sc;
            push @commands, $command_tools;
        }

        General::Logging::log_user_message("Uploading KUBECONFIG files to node(s)");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to copy the KUBECONFIG files to the node(s)");
            return 1;
        }

        General::Logging::log_user_message("KUBECONFIG files uploaded to remote node(s) directory: $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/kubeconfig_files/");

        return 0;
    }

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Upload_Config_Files_If_User_Specified_P011S03T05 {

        my $command_sc;
        my $command_tools;
        my @commands;
        my $rc = 0;

        for my $variable (sort keys %::JOB_PARAMS) {
            next unless ($variable =~ /^CONFIG_FILE_.+/);

            # Check if file exist, if not ignore the file
            if (-f $::JOB_PARAMS{$variable}) {
                # File exist, mark the file to be copied to the node and change the path to the file to point to the node workspace file.
                $command_sc = "${debug_command}scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr";
                if ($::JOB_PARAMS{'NODE_VIP_SC'} =~ /:/) {
                    $command_sc .= " -6 $::JOB_PARAMS{$variable} eccd\@[$::JOB_PARAMS{'NODE_VIP_SC'}]:$::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/config_files/";
                } else {
                    $command_sc .= " $::JOB_PARAMS{$variable} eccd\@$::JOB_PARAMS{'NODE_VIP_SC'}:$::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/config_files/";
                }
                push @commands, $command_sc;

                if ($::JOB_PARAMS{'NODE_VIP_SC'} ne $::JOB_PARAMS{'NODE_VIP_TOOLS'}) {
                    $command_tools = "${debug_command}scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr";
                    if ($::JOB_PARAMS{'NODE_VIP_TOOLS'} =~ /:/) {
                        $command_tools .= " -6 $::JOB_PARAMS{$variable} eccd\@[$::JOB_PARAMS{'NODE_VIP_TOOLS'}]:$::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/config_files/";
                    } else {
                        $command_tools .= " $::JOB_PARAMS{$variable} eccd\@$::JOB_PARAMS{'NODE_VIP_TOOLS'}:$::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/config_files/";
                    }
                    push @commands, $command_tools;
                }

                # Update the file path
                $::JOB_PARAMS{$variable} = "$::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/config_files/" . File::Basename::basename($::JOB_PARAMS{$variable});
            }
        }

        # Check if we have any files to upload
        unless (@commands) {
            General::Logging::log_user_message("No config files to copy to node(s)");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Copy config files to node(s)");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to copy the files to the node(s)");
            return 1;
        }

        General::Logging::log_user_message("Config files uploaded to remote node(s) directory: $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/config_files/");

        return 0;
    }
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Download_Software_P011S04 {

    my $message = "Download Software";
    my $rc = 0;
    my $var_name = "SKIP_DOWNLOAD_SOFTWARE";

    if ($::JOB_PARAMS{$var_name} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Download_Software_P011S04T01 } );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Task skipped because $var_name=yes");
        push @::JOB_STATUS, "(-) $message skipped because $var_name=yes";
    }

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Download_Software_P011S04T01 {

        my $command = "$debug_command";
        my @commands = ();
        my $fetch_software = 1;
        my $ip = $::JOB_PARAMS{'NODE_VIP_SC'};
        my $rc = 0;
        my @result;
        my $software_exist_on_node = 1;
        my @std_error;

        # Download software to this server, if needed
        #   Upload software to NODE_VIP_SC and NODE_VIP_TOOLS
        # Download software on NODE_VIP_SC, if needed
        # Download software on NODE_VIP_TOOLS, if needed

        if ($::JOB_PARAMS{'NODE_SOFTWARE_DIR'} eq "") {
            # No node directory specified for storing the software, so it will be uploaded to the workspace directory on the node.
            $::JOB_PARAMS{'NODE_SOFTWARE_DIR'} = "$::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/software/$::JOB_PARAMS{'SC_SOFTWARE_VERSION'}";
        }

        # Check if the software exist on the node(s)
        $software_exist_on_node = 1;
        $ip = $::JOB_PARAMS{'NODE_VIP_SC'};
        General::Logging::log_user_message("Checking if software exist on node: $ip");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ "${debug_command}mkdir -p $::JOB_PARAMS{'NODE_SOFTWARE_DIR'}", "ls -l $::JOB_PARAMS{'NODE_SOFTWARE_DIR'}/csar/*.csar" ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc == 0) {
            General::Logging::log_user_message("  Software exist on node: $ip");
        } else {
            $software_exist_on_node = 0;
            General::Logging::log_user_message("  Software does not exist on node: $ip");
        }
        if ($::JOB_PARAMS{'NODE_VIP_SC'} ne $::JOB_PARAMS{'NODE_VIP_TOOLS'}) {
            $ip = $::JOB_PARAMS{'NODE_VIP_TOOLS'};
            General::Logging::log_user_message("Checking if software exist on node: $ip");
            $rc = General::OS_Operations::send_command_via_ssh(
                {
                    "commands"            => [ "${debug_command}mkdir -p $::JOB_PARAMS{'NODE_SOFTWARE_DIR'}", "ls -l $::JOB_PARAMS{'NODE_SOFTWARE_DIR'}/csar/*.csar" ],
                    "expect-debug"        => 0,
                    "hide-output"         => 1,
                    "ip"                  => $ip,
                    "port"                => 22,
                    "password"            => "notneeded",
                    "return-output"       => \@result,
                    "return-stderr"       => \@std_error,
                    "stop-on-error"       => 1,
                    "timeout"             => -1,    # No timeout
                    "user"                => "eccd",
                }
            );
            if ($rc == 0) {
                General::Logging::log_user_message("  Software exist on node: $ip");
            } else {
                $software_exist_on_node = 0;
                General::Logging::log_user_message("  Software does not exist on node: $ip");
            }
        }

        if ($::JOB_PARAMS{'REUSE_ALREADY_DOWNLOADED_SOFTWARE'} eq "yes" && $software_exist_on_node == 1) {
            # No need to fetch the software
            General::Logging::log_user_message("$message not needed because software already exist on the node(s) and REUSE_ALREADY_DOWNLOADED_SOFTWARE=yes");
            push @::JOB_STATUS, "(-) $message skipped because REUSE_ALREADY_DOWNLOADED_SOFTWARE=yes";
            return 0;
        }

        # Check if we need to fetch the software
        if ($::JOB_PARAMS{'SOFTWARE_DIR'} eq "") {
            # No software directory specified on this server, so we should store the software temporarilly in the job workspace directory
            # unless the software already exist on both the SC and TOOLS node and we should not re-download the software.
            $fetch_software = 1;
            $::JOB_PARAMS{'SOFTWARE_DIR'} = "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software/$::JOB_PARAMS{'SC_SOFTWARE_VERSION'}";
            #$::JOB_PARAMS{'SOFTWARE_DIR'} = "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software";
        } else {
            # Check if the software exist on the current server
            General::Logging::log_user_message("Checking if software exists on current server");
            $rc = General::OS_Operations::send_command(
                {
                    "commands"            => [ "${debug_command}mkdir -p $::JOB_PARAMS{'SOFTWARE_DIR'}", "ls -l $::JOB_PARAMS{'SOFTWARE_DIR'}/csar/*.csar" ],
                    "hide-output"   => 1,
                }
            );
            if ($rc == 0) {
                General::Logging::log_user_message("  Software exist on current cluster");
                # The software already exist
                if ($::JOB_PARAMS{'REUSE_ALREADY_DOWNLOADED_SOFTWARE'} eq "yes") {
                    # No need to fetch it again
                    General::Logging::log_user_message("$message not needed because software already exist on the current cluster and REUSE_ALREADY_DOWNLOADED_SOFTWARE=yes");
                    $fetch_software = 0;
                } else {
                    # The software already exist on current server but we should fetch it again, so we need to fetch it
                    General::Logging::log_user_message("$message needed because software exist on the current cluster but REUSE_ALREADY_DOWNLOADED_SOFTWARE=no");
                    $fetch_software = 1;
                }
            } else {
                # The software does not exist
                General::Logging::log_user_message("  Software does not exist on current cluster");
                General::Logging::log_user_message("$message needed because software does not exist on the current cluster");
                $fetch_software = 1;
            }
        }

        if ($fetch_software == 1) {
            $command = "$debug_command";
            my $download_dir = "";
            my $software_version_regexp = $::JOB_PARAMS{'SC_SOFTWARE_VERSION'};
            $software_version_regexp =~ s/\+/\\+/g;
            if ($::JOB_PARAMS{'SOFTWARE_DIR'} =~ /^(.+)\/$software_version_regexp$/) {
                $download_dir = $1;
            } else {
                $download_dir = $::JOB_PARAMS{'SOFTWARE_DIR'};
                $::JOB_PARAMS{'SOFTWARE_DIR'} = "$::JOB_PARAMS{'SOFTWARE_DIR'}/$::JOB_PARAMS{'SC_SOFTWARE_VERSION'}";
            }
            if (-f "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/download_csar.pl") {
                $command .= "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/download_csar.pl --artifact-token $::JOB_PARAMS{'ARTIFACTORY_TOKEN'} --noprogress --color=no -p $::JOB_PARAMS{'SC_SOFTWARE_VERSION'} -l $::JOB_PARAMS{'ARTIFACTORY_URL'} -t $download_dir";
            } elsif (-f "$::JOB_PARAMS{'_PACKAGE_DIR'}/../scripts/download_csar.pl") {
                $command .= "$::JOB_PARAMS{'_PACKAGE_DIR'}/../scripts/download_csar.pl --artifact-token $::JOB_PARAMS{'ARTIFACTORY_TOKEN'} --noprogress --color=no -p $::JOB_PARAMS{'SC_SOFTWARE_VERSION'} -l $::JOB_PARAMS{'ARTIFACTORY_URL'} -t $download_dir";
            } else {
                General::Logging::log_user_error_message("$message failed because the download_csar.pl script could not be found on the current server");
                push @::JOB_STATUS, "(x) $message failed";
                return 1;
            }

            General::Logging::log_user_message("$message to current server\nThis will take a while to complete.\n");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("$message failed on current server");
                push @::JOB_STATUS, "(x) $message failed";
                return 1;
            }
        }

        # Now we need to upload the software to the node(s) with scp
        $command = "${debug_command}scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr";
        if ($::JOB_PARAMS{'NODE_VIP_SC'} =~ /:/) {
            $command .= " -6 $::JOB_PARAMS{'SOFTWARE_DIR'}/* eccd\@[$::JOB_PARAMS{'NODE_VIP_SC'}]:$::JOB_PARAMS{'NODE_SOFTWARE_DIR'}";
        } else {
            $command .= " $::JOB_PARAMS{'SOFTWARE_DIR'}/* eccd\@$::JOB_PARAMS{'NODE_VIP_SC'}:$::JOB_PARAMS{'NODE_SOFTWARE_DIR'}";
        }
        push @commands, $command;

        if ($::JOB_PARAMS{'NODE_VIP_SC'} ne $::JOB_PARAMS{'NODE_VIP_TOOLS'}) {
            $command = "${debug_command}scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr";
            if ($::JOB_PARAMS{'NODE_VIP_TOOLS'} =~ /:/) {
                $command .= " -6 $::JOB_PARAMS{'SOFTWARE_DIR'}/* eccd\@[$::JOB_PARAMS{'NODE_VIP_TOOLS'}]:$::JOB_PARAMS{'NODE_SOFTWARE_DIR'}";
            } else {
                $command .= " $::JOB_PARAMS{'SOFTWARE_DIR'}/* eccd\@$::JOB_PARAMS{'NODE_VIP_TOOLS'}:$::JOB_PARAMS{'NODE_SOFTWARE_DIR'}";
            }
            push @commands, $command;
        }

        General::Logging::log_user_message("Uploading Software package files to node(s)\nThis will take a while to complete.");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to copy the Software files to the node(s)");
            push @::JOB_STATUS, "(x) $message failed";
            return 1;
        }

        General::Logging::log_user_message("Software package uploaded to remote node(s) directory: $::JOB_PARAMS{'NODE_SOFTWARE_DIR'}");

        push @::JOB_STATUS, "(/) $message was successful";

        return $rc;
    }
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Clean_Registry_P011S05 {

    my $message = "Clean Registry";
    my $rc = 0;
    my $var_name = "SKIP_CLEAN_REGISTRY";

    if ($::JOB_PARAMS{$var_name} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Clean_Registry_P011S05T01} );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Task skipped because $var_name=yes");
        push @::JOB_STATUS, "(-) $message skipped because $var_name=yes";
    }

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Clean_Registry_P011S05T01 {

        my $command = "$debug_command";
        my $ip = $::JOB_PARAMS{'NODE_VIP_SC'};
        my $rc = 0;
        my @result;
        my @std_error;
        my $playlist_cnt = sprintf "%02d_", $::JOB_PARAMS{'PLAYLIST_CNT'};

        my $extra_parameters = fetch_user_specified_job_parameters("108_Miscellaneous_Tasks");
        $extra_parameters .= " -v CLEAN_REGISTRY=yes" unless ($extra_parameters =~ /-v CLEAN_REGISTRY=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=/);
        $extra_parameters .= " -v INPUT_AVAILABLE=no" unless ($extra_parameters =~ /-v INPUT_AVAILABLE=/);
        $extra_parameters .= " -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes" unless ($extra_parameters =~ /-v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=/);
        $extra_parameters .= " -w $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/playlist_logs";

        $command .= "$::JOB_PARAMS{'NODE_DAFT_DIR'}/perl/bin/execute_playlist.pl -p 108_Miscellaneous_Tasks -j ${playlist_cnt}Clean_Registry -n $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}$extra_parameters";

        # Check NODE_VIP_SC node
        $ip = $::JOB_PARAMS{'NODE_VIP_SC'};
        General::Logging::log_user_message("$message on node: $ip ($::JOB_PARAMS{'HOSTNAME_SC'})\nThis will take a while to complete.");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ "$command -v KUBECONFIG=$::JOB_PARAMS{'NODE_KUBECONFIG_SC'}" ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$message failed on node: $ip");
            push @::JOB_STATUS, "(x) $message failed";
            show_warning_message_for_rerun_of_playlist();
            return 1;
        }

        $::JOB_PARAMS{'PLAYLIST_CNT'}++;

        if ($::JOB_PARAMS{'NODE_VIP_SC'} ne $::JOB_PARAMS{'NODE_VIP_TOOLS'}) {
            # Check NODE_VIP_TOOLS node
            $ip = $::JOB_PARAMS{'NODE_VIP_TOOLS'};
            $playlist_cnt = sprintf "%02d_", $::JOB_PARAMS{'PLAYLIST_CNT'};
            $command .= "$::JOB_PARAMS{'NODE_DAFT_DIR'}/perl/bin/execute_playlist.pl -p 108_Miscellaneous_Tasks -j ${playlist_cnt}Clean_Registry -n $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}$extra_parameters";
            General::Logging::log_user_message("$message on node: $ip ($::JOB_PARAMS{'HOSTNAME_TOOLS'})\nThis will take a while to complete.");
            $rc = General::OS_Operations::send_command_via_ssh(
                {
                    "commands"            => [ "$command -v KUBECONFIG=$::JOB_PARAMS{'NODE_KUBECONFIG_TOOLS'}" ],
                    "expect-debug"        => 0,
                    "hide-output"         => 1,
                    "ip"                  => $ip,
                    "port"                => 22,
                    "password"            => "notneeded",
                    "return-output"       => \@result,
                    "return-stderr"       => \@std_error,
                    "stop-on-error"       => 1,
                    "timeout"             => -1,    # No timeout
                    "user"                => "eccd",
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("$message failed on node: $ip");
                push @::JOB_STATUS, "(x) $message failed";
                show_warning_message_for_rerun_of_playlist();
                return 1;
            }

            $::JOB_PARAMS{'PLAYLIST_CNT'}++;
        }

        push @::JOB_STATUS, "(/) $message was successful";

        return $rc;
    }
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Remove_Node_Labels_And_Taints_P011S06 {

    my $message;
    my $rc = 0;
    my $var_name;

    $message = "Remove Node Labels and Taints for UCC";
    $var_name = "SKIP_REMOVE_NODE_LABELS_FOR_UCC";
    if ($::JOB_PARAMS{$var_name} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Node_Labels_And_Taints_For_UCC_P011S06T01} );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Task skipped because $var_name=yes");
        push @::JOB_STATUS, "(-) $message skipped because $var_name=yes";
    }

    $message = "Remove Node Labels and Taints for Tools";
    $var_name = "SKIP_REMOVE_NODE_LABELS_FOR_TOOLS";
    if ($::JOB_PARAMS{$var_name} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Node_Labels_And_Taints_For_Tools_P011S06T02} );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Task skipped because $var_name=yes");
        push @::JOB_STATUS, "(-) $message skipped because $var_name=yes";
    }

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Remove_Node_Labels_And_Taints_For_UCC_P011S06T01 {

        my $command = "$debug_command";
        my $ip = $::JOB_PARAMS{'NODE_VIP_SC'};
        my $rc = 0;
        my @result;
        my @std_error;
        my $playlist_cnt = sprintf "%02d_", $::JOB_PARAMS{'PLAYLIST_CNT'};

        my $extra_parameters = fetch_user_specified_job_parameters("103_Tools_Management");
        $extra_parameters .= " -v DO_REMOVE_LABEL=yes";
        $extra_parameters .= " -v DO_REMOVE_TAINT=yes";
        $extra_parameters .= " -v NODE_LABEL=usage=ucc";
        $extra_parameters .= " -v NODE_TAINTS=PreferNoSchedule";
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=/);
        $extra_parameters .= " -v INPUT_AVAILABLE=no" unless ($extra_parameters =~ /-v INPUT_AVAILABLE=/);
        $extra_parameters .= " -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes" unless ($extra_parameters =~ /-v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=/);
        $extra_parameters .= " -w $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/playlist_logs";
        # We do not set KUBECONFIG or KUBECONFIG for this command since we connect
        # to the SC IP and there we should have $HOME/.kube/config set properly.

        $command .= "$::JOB_PARAMS{'NODE_DAFT_DIR'}/perl/bin/execute_playlist.pl -p 103_Tools_Management -j ${playlist_cnt}Remove_Node_Label_And_Taints_For_UCC -n $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}$extra_parameters";

        # Check NODE_VIP_SC node
        General::Logging::log_user_message("$message on node: $ip ($::JOB_PARAMS{'HOSTNAME_SC'})\nThis will take a while to complete.");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ $command ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$message failed on node: $ip");
            push @::JOB_STATUS, "(x) $message failed";
            show_warning_message_for_rerun_of_playlist();
            return 1;
        }

        $::JOB_PARAMS{'PLAYLIST_CNT'}++;

        push @::JOB_STATUS, "(/) $message was successful";

        return $rc;
    }

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Remove_Node_Labels_And_Taints_For_Tools_P011S06T02 {

        my $command = "$debug_command";
        my $ip = $::JOB_PARAMS{'NODE_VIP_TOOLS'};
        my $rc = 0;
        my @result;
        my @std_error;
        my $playlist_cnt = sprintf "%02d_", $::JOB_PARAMS{'PLAYLIST_CNT'};

        my $extra_parameters = fetch_user_specified_job_parameters("103_Tools_Management");
        $extra_parameters .= " -v DO_REMOVE_LABEL=yes" unless ($extra_parameters =~ /-v DO_REMOVE_LABEL=/);
        $extra_parameters .= " -v DO_REMOVE_TAINT=yes" unless ($extra_parameters =~ /-v DO_REMOVE_TAINT=/);
        $extra_parameters .= " -v NODE_LABEL=usage=tools" unless ($extra_parameters =~ /-v NODE_LABEL=/);
        $extra_parameters .= " -v NODE_TAINTS=NoSchedule" unless ($extra_parameters =~ /-v NODE_TAINTS=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=/);
        $extra_parameters .= " -v INPUT_AVAILABLE=no" unless ($extra_parameters =~ /-v INPUT_AVAILABLE=/);
        $extra_parameters .= " -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes" unless ($extra_parameters =~ /-v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=/);
        $extra_parameters .= " -w $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/playlist_logs";
        # We do not set KUBECONFIG or KUBECONFIG_TOOLS for this command since we connect
        # to the tools IP and there we should have $HOME/.kube/config set properly.

        $command .= "$::JOB_PARAMS{'NODE_DAFT_DIR'}/perl/bin/execute_playlist.pl -p 103_Tools_Management -j ${playlist_cnt}Remove_Node_Label_And_Taints_For_Tools -n $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}$extra_parameters";

        # Check NODE_VIP_SC node
        General::Logging::log_user_message("$message on node: $ip ($::JOB_PARAMS{'HOSTNAME_TOOLS'})\nThis will take a while to complete.");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ $command ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$message failed on node: $ip");
            push @::JOB_STATUS, "(x) $message failed";
            show_warning_message_for_rerun_of_playlist();
            return 1;
        }

        $::JOB_PARAMS{'PLAYLIST_CNT'}++;

        push @::JOB_STATUS, "(/) $message was successful";

        return $rc;
    }
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Undeploy_Tools_P011S07 {

    my $message = "Undeploy Tools";
    my $rc = 0;
    my $var_name = "SKIP_UNDEPLOY_TOOLS";

    if ($::JOB_PARAMS{$var_name} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Undeploy_Tools_P011S07T01} );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Task skipped because $var_name=yes");
        push @::JOB_STATUS, "(-) $message skipped because $var_name=yes";
    }

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Undeploy_Tools_P011S07T01 {

        my $command = "$debug_command";
        my $ip = $::JOB_PARAMS{'NODE_VIP_TOOLS'};
        my $rc = 0;
        my @result;
        my @std_error;
        my $playlist_cnt = sprintf "%02d_", $::JOB_PARAMS{'PLAYLIST_CNT'};

        my $extra_parameters = fetch_user_specified_job_parameters("106_Traffic_Tools_Remove");
        $extra_parameters .= " -v REMOVE_NAMESPACE=yes" unless ($extra_parameters =~ /-v REMOVE_NAMESPACE=/);
        $extra_parameters .= " -v TOOLS_TO_REMOVE=chfsim,dscload,k6,nrfsim,sc-monitor,seppsim,sftp" unless ($extra_parameters =~ /-v TOOLS_TO_REMOVE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=/);
        $extra_parameters .= " -v INPUT_AVAILABLE=no" unless ($extra_parameters =~ /-v INPUT_AVAILABLE=/);
        $extra_parameters .= " -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes" unless ($extra_parameters =~ /-v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=/);
        $extra_parameters .= " -w $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/playlist_logs";
        # We do not set KUBECONFIG or KUBECONFIG_TOOLS for this command since we connect
        # to the tools IP and there we should have $HOME/.kube/config set properly.

        $command .= "$::JOB_PARAMS{'NODE_DAFT_DIR'}/perl/bin/execute_playlist.pl -p 106_Traffic_Tools_Remove -j ${playlist_cnt}Undeploy_Tools -n $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}$extra_parameters";

        # Check NODE_VIP_SC node
        General::Logging::log_user_message("$message on node: $ip ($::JOB_PARAMS{'HOSTNAME_TOOLS'})\nThis will take a while to complete.");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ $command ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$message failed on node: $ip");
            push @::JOB_STATUS, "(x) $message failed";
            show_warning_message_for_rerun_of_playlist();
            return 1;
        }

        $::JOB_PARAMS{'PLAYLIST_CNT'}++;

        push @::JOB_STATUS, "(/) $message was successful";

        return $rc;
    }
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Undeploy_SC_P011S08 {

    my $message = "Undeploy SC";
    my $rc = 0;
    my $var_name = "SKIP_UNDEPLOY_SC";

    if ($::JOB_PARAMS{$var_name} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Undeploy_SC_P011S08T01} );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Task skipped because $var_name=yes");
        push @::JOB_STATUS, "(-) $message skipped because $var_name=yes";
    }

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Undeploy_SC_P011S08T01 {

        my $command = "$debug_command";
        my $ip = $::JOB_PARAMS{'NODE_VIP_SC'};
        my $rc = 0;
        my @result;
        my @std_error;
        my $playlist_cnt = sprintf "%02d_", $::JOB_PARAMS{'PLAYLIST_CNT'};

        my $extra_parameters = fetch_user_specified_job_parameters("003_Undeploy_SC");
        $extra_parameters .= " -v CLEAN_REGISTRY=no" unless ($extra_parameters =~ /-v CLEAN_REGISTRY=/);
        $extra_parameters .= " -v SKIP_UNDEPLOY_CRD=no" unless ($extra_parameters =~ /-v SKIP_UNDEPLOY_CRD=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=/);
        $extra_parameters .= " -v INPUT_AVAILABLE=no" unless ($extra_parameters =~ /-v INPUT_AVAILABLE=/);
        $extra_parameters .= " -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes" unless ($extra_parameters =~ /-v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=/);
        $extra_parameters .= " -w $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/playlist_logs";
        # We do not set KUBECONFIG or KUBECONFIG_TOOLS for this command since we connect
        # to the sc IP and there we have should $HOME/.kube/config set properly.

        $command .= "$::JOB_PARAMS{'NODE_DAFT_DIR'}/perl/bin/execute_playlist.pl -p 003_Undeploy_SC -j ${playlist_cnt}Undeploy_SC -n $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}$extra_parameters";

        # Check NODE_VIP_SC node
        General::Logging::log_user_message("$message on node: $ip ($::JOB_PARAMS{'HOSTNAME_SC'})\nThis will take a while to complete.");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ $command ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$message failed on node: $ip");
            push @::JOB_STATUS, "(x) $message failed";
            show_warning_message_for_rerun_of_playlist();
            return 1;
        }

        $::JOB_PARAMS{'PLAYLIST_CNT'}++;

        push @::JOB_STATUS, "(/) $message was successful";

        return $rc;
    }
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Reserve_Workers_P011S09 {

    my $message;
    my $rc = 0;
    my $var_name;

    # Figure out which worker nodes should be reserved for UCC and/or Tools, they should not be the same.
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Select_Workers_To_Reserve_P011S09T01} );
    return $rc if $rc < 0;

    $message = "Reserve Workers for UCC";
    $var_name = "SKIP_RESERVE_WORKERS_FOR_UCC";
    if ($::JOB_PARAMS{'DEPLOY_UCC'} eq "yes") {
        if ($::JOB_PARAMS{$var_name} eq "no") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Reserve_Workers_For_UCC_P011S09T02} );
            return $rc if $rc < 0;
        } else {
            General::Logging::log_user_message("Task '$message' skipped because $var_name=yes");
            push @::JOB_STATUS, "(-) $message skipped because $var_name=yes";
        }
    } else {
        General::Logging::log_user_message("Task '$message' skipped because DEPLOY_UCC=no");
        push @::JOB_STATUS, "(-) $message skipped because DEPLOY_UCC=no";
    }

    $message = "Reserve Workers for Tools";
    $var_name = "SKIP_RESERVE_WORKERS_FOR_TOOLS";
    if ($::JOB_PARAMS{$var_name} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Reserve_Workers_For_Tools_P011S09T03} );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Task '$message' skipped because $var_name=yes");
        push @::JOB_STATUS, "(-) $message skipped because $var_name=yes";
    }

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Select_Workers_To_Reserve_P011S09T01 {

        my @nodes;
        my $nodes_to_reserve;
        my $rc = 0;

        $::JOB_PARAMS{'WORKERS_RESERVED_FOR_SC_UCC'} = "";
        $::JOB_PARAMS{'WORKERS_RESERVED_FOR_TOOLS'} = "";

        if ($::JOB_PARAMS{'NODE_VIP_SC'} eq $::JOB_PARAMS{'NODE_VIP_TOOLS'}) {
            # SC and Tools deployed on same node.
            # We now need to 'reserve' different worker nodes for deployment of the SC UCC and Tools.
            @nodes = ADP::Kubernetes_Operations::get_worker_nodes($::JOB_PARAMS{'KUBECONFIG_SC'});

            # First reserve for tools
            $nodes_to_reserve = 2;
            if (exists $::JOB_PARAMS{'NODES_TO_SELECT'}) {
                $nodes_to_reserve = $::JOB_PARAMS{'NODES_TO_SELECT'};
            }
            while ($nodes_to_reserve > 0) {
                $::JOB_PARAMS{'WORKERS_RESERVED_FOR_TOOLS'} .= sprintf "%s,", shift @nodes;
                $nodes_to_reserve--;
            }

            # Next reserve for SC UCC
            $nodes_to_reserve = 1;
            while ($nodes_to_reserve > 0) {
                $::JOB_PARAMS{'WORKERS_RESERVED_FOR_SC_UCC'} .= sprintf "%s,", shift @nodes;
                $nodes_to_reserve--;
            }
        } else {
            # SC and Tools deployed on different nodes.

            # Reserve for tools
            @nodes = ADP::Kubernetes_Operations::get_worker_nodes($::JOB_PARAMS{'KUBECONFIG_TOOLS'});
            $nodes_to_reserve = 2;
            if (exists $::JOB_PARAMS{'NODES_TO_SELECT'}) {
                $nodes_to_reserve = $::JOB_PARAMS{'NODES_TO_SELECT'};
            }
            while ($nodes_to_reserve > 0) {
                $::JOB_PARAMS{'WORKERS_RESERVED_FOR_TOOLS'} .= sprintf "%s,", shift @nodes;
                $nodes_to_reserve--;
            }

            # Reserve for SC UCC
            @nodes = ADP::Kubernetes_Operations::get_worker_nodes($::JOB_PARAMS{'KUBECONFIG_SC'});
            $nodes_to_reserve = 1;
            while ($nodes_to_reserve > 0) {
                $::JOB_PARAMS{'WORKERS_RESERVED_FOR_SC_UCC'} .= sprintf "%s,", shift @nodes;
                $nodes_to_reserve--;
            }
        }

        $::JOB_PARAMS{'WORKERS_RESERVED_FOR_SC_UCC'} =~ s/,$//;
        $::JOB_PARAMS{'WORKERS_RESERVED_FOR_TOOLS'} =~ s/,$//;

        General::Logging::log_user_message("WORKERS_RESERVED_FOR_SC_UCC=$::JOB_PARAMS{'WORKERS_RESERVED_FOR_SC_UCC'}\nWORKERS_RESERVED_FOR_TOOLS=$::JOB_PARAMS{'WORKERS_RESERVED_FOR_TOOLS'}\n");

        return $rc;
    }

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Reserve_Workers_For_UCC_P011S09T02 {

        my $command = "$debug_command";
        my $ip = $::JOB_PARAMS{'NODE_VIP_SC'};
        my $rc = 0;
        my @result;
        my @std_error;
        my $playlist_cnt = sprintf "%02d_", $::JOB_PARAMS{'PLAYLIST_CNT'};

        my $extra_parameters = fetch_user_specified_job_parameters("103_Tools_Management");
        $extra_parameters .= " -v DO_ASSIGN_LABEL=yes";
        $extra_parameters .= " -v DO_ASSIGN_TAINT=yes";
        $extra_parameters .= " -v NODE_TAINTS=PreferNoSchedule";
        $extra_parameters .= " -v NODE_LABEL=usage=deployment1";
        $extra_parameters .= " -v NODE_NAMES=$::JOB_PARAMS{'WORKERS_RESERVED_FOR_SC_UCC'}";
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=/);
        $extra_parameters .= " -v INPUT_AVAILABLE=no" unless ($extra_parameters =~ /-v INPUT_AVAILABLE=/);
        $extra_parameters .= " -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes" unless ($extra_parameters =~ /-v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=/);
        $extra_parameters .= " -w $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/playlist_logs";
        # We do not set KUBECONFIG or KUBECONFIG_TOOLS for this command since we connect
        # to the tools IP and there we should have $HOME/.kube/config set properly.

        $command .= "$::JOB_PARAMS{'NODE_DAFT_DIR'}/perl/bin/execute_playlist.pl -p 103_Tools_Management -j ${playlist_cnt}Reserve_Workers_For_UCC -n $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}$extra_parameters";

        # Check NODE_VIP_SC node
        General::Logging::log_user_message("$message on node: $ip ($::JOB_PARAMS{'HOSTNAME_SC'})\nThis will take a while to complete.");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ $command ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$message failed on node: $ip");
            push @::JOB_STATUS, "(x) $message failed";
            show_warning_message_for_rerun_of_playlist();
            return 1;
        }

        $::JOB_PARAMS{'PLAYLIST_CNT'}++;

        push @::JOB_STATUS, "(/) $message was successful";

        return $rc;
    }

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Reserve_Workers_For_Tools_P011S09T03 {

        my $command = "$debug_command";
        my $ip = $::JOB_PARAMS{'NODE_VIP_TOOLS'};
        my $rc = 0;
        my @result;
        my @std_error;
        my $playlist_cnt = sprintf "%02d_", $::JOB_PARAMS{'PLAYLIST_CNT'};

        my $extra_parameters = fetch_user_specified_job_parameters("103_Tools_Management");
        $extra_parameters .= " -v DO_ASSIGN_LABEL=yes" unless ($extra_parameters =~ /-v DO_ASSIGN_LABEL=/);
        $extra_parameters .= " -v DO_ASSIGN_TAINT=yes" unless ($extra_parameters =~ /-v DO_ASSIGN_TAINT=/);
        $extra_parameters .= " -v NODE_TAINTS=NoSchedule" unless ($extra_parameters =~ /-v NODE_TAINTS=/);
        $extra_parameters .= " -v NODE_LABEL=usage=tools" unless ($extra_parameters =~ /-v NODE_LABEL=/);
        $extra_parameters .= " -v NODE_NAMES=$::JOB_PARAMS{'WORKERS_RESERVED_FOR_TOOLS'}";
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=/);
        $extra_parameters .= " -v INPUT_AVAILABLE=no" unless ($extra_parameters =~ /-v INPUT_AVAILABLE=/);
        $extra_parameters .= " -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes" unless ($extra_parameters =~ /-v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=/);
        $extra_parameters .= " -w $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/playlist_logs";
        # We do not set KUBECONFIG or KUBECONFIG_TOOLS for this command since we connect
        # to the tools IP and there we should have $HOME/.kube/config set properly.

        $command .= "$::JOB_PARAMS{'NODE_DAFT_DIR'}/perl/bin/execute_playlist.pl -p 103_Tools_Management -j ${playlist_cnt}Reserve_Workers_For_Tools -n $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}$extra_parameters";

        # Check NODE_VIP_SC node
        General::Logging::log_user_message("$message on node: $ip ($::JOB_PARAMS{'HOSTNAME_TOOLS'})\nThis will take a while to complete.");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ $command ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$message failed on node: $ip");
            push @::JOB_STATUS, "(x) $message failed";
            show_warning_message_for_rerun_of_playlist();
            return 1;
        }

        $::JOB_PARAMS{'PLAYLIST_CNT'}++;

        push @::JOB_STATUS, "(/) $message was successful";

        return $rc;
    }
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Deploy_SC_P011S10 {

    my $message = "Deploy SC";
    my $rc = 0;
    my $var_name = "SKIP_DEPLOY_SC";

    if ($::JOB_PARAMS{$var_name} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Deploy_SC_P011S10T01} );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Task skipped because $var_name=yes");
        push @::JOB_STATUS, "(-) $message skipped because $var_name=yes";
    }

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Deploy_SC_P011S10T01 {

        my $command = "$debug_command";
        my $ip = $::JOB_PARAMS{'NODE_VIP_SC'};
        my $rc = 0;
        my @result;
        my @std_error;
        my $playlist_cnt = sprintf "%02d_", $::JOB_PARAMS{'PLAYLIST_CNT'};

        my $extra_parameters = fetch_user_specified_job_parameters("001_Deploy_SC");
        unless ($extra_parameters =~ /-v ENABLED_CNF=/) {
            if ($::JOB_PARAMS{'ENABLED_CNF'} eq "") {
                $extra_parameters .= " -v ENABLED_CNF=bsf-scp-slf-sepp";
            } else {
                # The parameter was not specified by the user on the command line but changed by the DEPLOY_UCC=yes parameter
                # so we now use that value.
                $extra_parameters .= " -v ENABLED_CNF=$::JOB_PARAMS{'ENABLED_CNF'}";
            }
        }
        $extra_parameters .= " -v CONFIG_FILE_HELM_CHART_1=$::JOB_PARAMS{'CONFIG_FILE_HELM_CHART_1'}" if (exists $::JOB_PARAMS{'CONFIG_FILE_HELM_CHART_1'} && $::JOB_PARAMS{'CONFIG_FILE_HELM_CHART_1'} ne "");
        $extra_parameters .= " -v COLLECT_LOGS_AT_SUCCESS=yes" unless ($extra_parameters =~ /-v COLLECT_LOGS_AT_SUCCESS=/);
        $extra_parameters .= " -v SKIP_DEPLOY_UPGRADE_CRD=no" unless ($extra_parameters =~ /-v SKIP_DEPLOY_UPGRADE_CRD=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=/);
        $extra_parameters .= " -v INPUT_AVAILABLE=no" unless ($extra_parameters =~ /-v INPUT_AVAILABLE=/);
        $extra_parameters .= " -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes" unless ($extra_parameters =~ /-v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=/);
        $extra_parameters .= " -v SOFTWARE_DIR=$::JOB_PARAMS{'NODE_SOFTWARE_DIR'}";
        $extra_parameters .= " -w $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/playlist_logs";
        for my $option (split /\|/, $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'}) {
            $extra_parameters .= " -o $option";
        }
        # We do not set KUBECONFIG or KUBECONFIG_TOOLS for this command since we connect
        # to the sc IP and there we should have $HOME/.kube/config set properly.

        $command .= "$::JOB_PARAMS{'NODE_DAFT_DIR'}/perl/bin/execute_playlist.pl -p 001_Deploy_SC -j ${playlist_cnt}Deploy_SC_$::JOB_PARAMS{'SC_SOFTWARE_VERSION'} -n $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}$extra_parameters";

        # Check NODE_VIP_SC node
        General::Logging::log_user_message("$message on node: $ip ($::JOB_PARAMS{'HOSTNAME_SC'})\nThis will take a while to complete.");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ $command ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$message failed on node: $ip");
            push @::JOB_STATUS, "(x) $message failed";
            show_warning_message_for_rerun_of_playlist();
            return 1;
        }

        $::JOB_PARAMS{'PLAYLIST_CNT'}++;

        push @::JOB_STATUS, "(/) $message was successful";

        return $rc;
    }
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Add_Users_P011S11 {

    my $message = "Add Users";
    my $rc = 0;
    my $var_name = "SKIP_ADD_USERS";

    if ($::JOB_PARAMS{$var_name} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Add_Users_P011S11T01} );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Task skipped because $var_name=yes");
        push @::JOB_STATUS, "(-) $message skipped because $var_name=yes";
    }

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Add_Users_P011S11T01 {

        my $command = "$debug_command";
        my $ip = $::JOB_PARAMS{'NODE_VIP_SC'};
        my $rc = 0;
        my @result;
        my @std_error;
        my $playlist_cnt = sprintf "%02d_", $::JOB_PARAMS{'PLAYLIST_CNT'};

        my $extra_parameters = fetch_user_specified_job_parameters("005_User_Management");
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=/);
        $extra_parameters .= " -v INPUT_AVAILABLE=no" unless ($extra_parameters =~ /-v INPUT_AVAILABLE=/);
        $extra_parameters .= " -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes" unless ($extra_parameters =~ /-v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=/);
        $extra_parameters .= " -w $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/playlist_logs";
        for my $option (split /\|/, $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'}) {
            $extra_parameters .= " -o $option";
        }
        # We do not set KUBECONFIG or KUBECONFIG_TOOLS for this command since we connect
        # to the sc IP and there we should have $HOME/.kube/config set properly.

        $command .= "$::JOB_PARAMS{'NODE_DAFT_DIR'}/perl/bin/execute_playlist.pl -p 005_User_Management -j ${playlist_cnt}Add_Users_SC_$::JOB_PARAMS{'SC_SOFTWARE_VERSION'} -n $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}$extra_parameters";

        # Check NODE_VIP_SC node
        General::Logging::log_user_message("$message on node: $ip ($::JOB_PARAMS{'HOSTNAME_SC'})\nThis will take a while to complete.");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ $command ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$message failed on node: $ip");
            push @::JOB_STATUS, "(x) $message failed";
            show_warning_message_for_rerun_of_playlist();
            return 1;
        }

        $::JOB_PARAMS{'PLAYLIST_CNT'}++;

        push @::JOB_STATUS, "(/) $message was successful";

        return $rc;
    }
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Generate_Install_Certificates_For_SC_P011S12 {

    my $message = "Generate Install Certificates For SC";
    my $rc = 0;
    my $var_name = "SKIP_INSTALL_CERTIFICATES";

    if ($::JOB_PARAMS{$var_name} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Generate_Install_Certificates_For_SC_P011S12T01} );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Task skipped because $var_name=yes");
        push @::JOB_STATUS, "(-) $message skipped because $var_name=yes";
    }

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Generate_Install_Certificates_For_SC_P011S12T01{

        my $command = "$debug_command";
        my $ip = $::JOB_PARAMS{'NODE_VIP_SC'};
        my $rc = 0;
        my @result;
        my @std_error;
        my $playlist_cnt = sprintf "%02d_", $::JOB_PARAMS{'PLAYLIST_CNT'};

        my $extra_parameters = fetch_user_specified_job_parameters("102_Supreme_Certificate_Management");
        $extra_parameters .= " -v GENERATE_CERTIFICATES=yes" unless ($extra_parameters =~ /-v GENERATE_CERTIFICATES=/);
        $extra_parameters .= " -v CERTIFICATES_TO_GENERATE=rootca,scpmgr,scpwrk,seppmgr,seppwrk,nbi,diameter,transformer" unless ($extra_parameters =~ /-v CERTIFICATES_TO_GENERATE=/);
        $extra_parameters .= " -v INSTALL_CERTIFICATES=yes" unless ($extra_parameters =~ /-v INSTALL_CERTIFICATES=/);
        $extra_parameters .= " -v CERTIFICATE_VALIDITY_DAYS=7300" unless ($extra_parameters =~ /-v CERTIFICATE_VALIDITY_DAYS=/);
        $extra_parameters .= " -v CERTIFICATE_DIRECTORY=$::JOB_PARAMS{'NODE_SOFTWARE_DIR'}/certificates";
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=/);
        $extra_parameters .= " -v INPUT_AVAILABLE=no" unless ($extra_parameters =~ /-v INPUT_AVAILABLE=/);
        $extra_parameters .= " -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes" unless ($extra_parameters =~ /-v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=/);
        $extra_parameters .= " -v SOFTWARE_DIR=$::JOB_PARAMS{'NODE_SOFTWARE_DIR'}";
        $extra_parameters .= " -v KUBECONFIG=$::JOB_PARAMS{'NODE_KUBECONFIG_SC'}";
        $extra_parameters .= " -v KUBECONFIG_TOOLS=$::JOB_PARAMS{'NODE_KUBECONFIG_TOOLS'}";
        $extra_parameters .= " -w $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/playlist_logs";
        for my $option (split /\|/, $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'}) {
            $extra_parameters .= " -o $option";
        }

        $command .= "$::JOB_PARAMS{'NODE_DAFT_DIR'}/perl/bin/execute_playlist.pl -p 102_Supreme_Certificate_Management -j ${playlist_cnt}Add_Certificates_SC_$::JOB_PARAMS{'SC_SOFTWARE_VERSION'} -n $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}$extra_parameters";

        # Check NODE_VIP_SC node
        General::Logging::log_user_message("$message on node: $ip ($::JOB_PARAMS{'HOSTNAME_SC'})\nThis will take a while to complete.");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ $command ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$message failed on node: $ip");
            push @::JOB_STATUS, "(x) $message failed";
            show_warning_message_for_rerun_of_playlist();
            return 1;
        }

        $::JOB_PARAMS{'PLAYLIST_CNT'}++;

        push @::JOB_STATUS, "(/) $message was successful";

        return $rc;
    }
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Deploy_Tools_P011S13 {

    my $message;
    my $rc = 0;
    my $var_name;

    $message = "Deploy Tools";
    $var_name = "SKIP_DEPLOY_TOOLS";
    if ($::JOB_PARAMS{$var_name} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Deploy_Tools_P011S13T01} );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Task skipped because $var_name=yes");
        push @::JOB_STATUS, "(-) $message skipped because $var_name=yes";
    }

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Deploy_Tools_P011S13T01 {

        my $command = "$debug_command";
        my $ip = $::JOB_PARAMS{'NODE_VIP_TOOLS'};
        my $rc = 0;
        my @result;
        my @std_error;
        my $playlist_cnt = sprintf "%02d_", $::JOB_PARAMS{'PLAYLIST_CNT'};

        my $extra_parameters = fetch_user_specified_job_parameters("109_Traffic_Tools_Install");
        $extra_parameters .= " -v CERTIFICATE_VALIDITY_DAYS=7300" unless ($extra_parameters =~ /-v CERTIFICATE_VALIDITY_DAYS=/);
        $extra_parameters .= " -v CERTIFICATE_DIRECTORY=$::JOB_PARAMS{'NODE_SOFTWARE_DIR'}/certificates";
        $extra_parameters .= " -v TOOLS_TO_INSTALL=chfsim,dscload,k6,nrfsim,sc-monitor,seppsim,sftp" unless ($extra_parameters =~ /-v TOOLS_TO_INSTALL=/);
        $extra_parameters .= " -v TOOLS_ON_SEPARATE_WORKERS=no" unless ($extra_parameters =~ /-v TOOLS_ON_SEPARATE_WORKERS=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=/);
        $extra_parameters .= " -v INPUT_AVAILABLE=no" unless ($extra_parameters =~ /-v INPUT_AVAILABLE=/);
        $extra_parameters .= " -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes" unless ($extra_parameters =~ /-v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=/);
        $extra_parameters .= " -v SOFTWARE_DIR=$::JOB_PARAMS{'NODE_SOFTWARE_DIR'}";
        $extra_parameters .= " -v KUBECONFIG=$::JOB_PARAMS{'NODE_KUBECONFIG_SC'}";
        $extra_parameters .= " -v KUBECONFIG_TOOLS=$::JOB_PARAMS{'NODE_KUBECONFIG_TOOLS'}";
        $extra_parameters .= " -v SKIP_LOADING_SC_MONITOR_IMAGE=yes" if ($::JOB_PARAMS{'KUBECONFIG_SC'} eq $::JOB_PARAMS{'KUBECONFIG_TOOLS'});
        $extra_parameters .= " -v TOOLS_ON_SEPARATE_WORKERS=yes" if ($::JOB_PARAMS{'SKIP_RESERVE_WORKERS_FOR_TOOLS'} eq "no");
        $extra_parameters .= " -w $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/playlist_logs";
        for my $option (split /\|/, $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'}) {
            $extra_parameters .= " -o $option";
        }
        # We do not set KUBECONFIG or KUBECONFIG_TOOLS for this command since we connect
        # to the tools IP and there we should have $HOME/.kube/config set properly.

        $command .= "$::JOB_PARAMS{'NODE_DAFT_DIR'}/perl/bin/execute_playlist.pl -p 109_Traffic_Tools_Install -j ${playlist_cnt}Deploy_Tools_SC_$::JOB_PARAMS{'SC_SOFTWARE_VERSION'} -n $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}$extra_parameters";

        # Check NODE_VIP_SC node
        General::Logging::log_user_message("$message on node: $ip ($::JOB_PARAMS{'HOSTNAME_TOOLS'})\nThis will take a while to complete.");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ $command ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$message failed on node: $ip");
            push @::JOB_STATUS, "(x) $message failed";
            show_warning_message_for_rerun_of_playlist();
            return 1;
        }

        $::JOB_PARAMS{'PLAYLIST_CNT'}++;

        push @::JOB_STATUS, "(/) $message was successful";

        return $rc;
    }
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Configure_Traffic_P011S14 {

    my $message = "Configure Traffic";
    my $rc = 0;
    my $var_name = "SKIP_CONFIGURE_TRAFFIC";

    if ($::JOB_PARAMS{$var_name} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_Traffic_P011S14T01} );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Task skipped because $var_name=yes");
        push @::JOB_STATUS, "(-) $message skipped because $var_name=yes";
    }

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Configure_Traffic_P011S14T01 {

        my $command = "$debug_command";
        my $ip = $::JOB_PARAMS{'NODE_VIP_SC'};
        my $rc = 0;
        my @result;
        my @std_error;
        my $playlist_cnt = sprintf "%02d_", $::JOB_PARAMS{'PLAYLIST_CNT'};

        my $extra_parameters = fetch_user_specified_job_parameters("107_Traffic_Tools_Configure_And_Start");
        $extra_parameters .= " -v FORCE_LOAD_CONFIGURATION=yes" unless ($extra_parameters =~ /-v FORCE_LOAD_CONFIGURATION=/);
        $extra_parameters .= " -v PASSWORD_EXPIRE=no" unless ($extra_parameters =~ /-v PASSWORD_EXPIRE=/);
        $extra_parameters .= " -v SKIP_POST_HEALTHCHECK=no" unless ($extra_parameters =~ /-v SKIP_POST_HEALTHCHECK=/);
        $extra_parameters .= " -v SKIP_PRE_HEALTHCHECK=yes" unless ($extra_parameters =~ /-v SKIP_PRE_HEALTHCHECK=/);
        $extra_parameters .= " -v TRAFFIC_TO_CONFIGURE=automatic" unless ($extra_parameters =~ /-v TRAFFIC_TO_CONFIGURE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=/);
        $extra_parameters .= " -v INPUT_AVAILABLE=no" unless ($extra_parameters =~ /-v INPUT_AVAILABLE=/);
        $extra_parameters .= " -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes" unless ($extra_parameters =~ /-v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=/);
        $extra_parameters .= " -v KUBECONFIG=$::JOB_PARAMS{'NODE_KUBECONFIG_SC'}";
        $extra_parameters .= " -v KUBECONFIG_TOOLS=$::JOB_PARAMS{'NODE_KUBECONFIG_TOOLS'}";
        # Why doesn't the playlist have this variable? $extra_parameters .= " -v SOFTWARE_DIR=$::JOB_PARAMS{'NODE_SOFTWARE_DIR'}";
        $extra_parameters .= " -w $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/playlist_logs";
        for my $option (split /\|/, $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'}) {
            $extra_parameters .= " -o $option";
        }

        $command .= "$::JOB_PARAMS{'NODE_DAFT_DIR'}/perl/bin/execute_playlist.pl -p 107_Traffic_Tools_Configure_And_Start -j ${playlist_cnt}Configure_Traffic_SC_$::JOB_PARAMS{'SC_SOFTWARE_VERSION'} -n $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}$extra_parameters";

        # Check NODE_VIP_SC node
        General::Logging::log_user_message("$message on node: $ip ($::JOB_PARAMS{'HOSTNAME_SC'})\nThis will take a while to complete.");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ $command ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$message failed on node: $ip");
            push @::JOB_STATUS, "(x) $message failed";
            show_warning_message_for_rerun_of_playlist();
            return 1;
        }

        $::JOB_PARAMS{'PLAYLIST_CNT'}++;

        push @::JOB_STATUS, "(/) $message was successful";

        return $rc;
    }
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Start_Traffic_P011S15 {

    my $message = "Start Traffic";
    my $rc = 0;
    my $var_name = "SKIP_START_TRAFFIC";

    if ($::JOB_PARAMS{$var_name} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Start_Traffic_P011S15T01} );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Task skipped because $var_name=yes");
        push @::JOB_STATUS, "(-) $message skipped because $var_name=yes";
    }

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Start_Traffic_P011S15T01 {

        my $command = "$debug_command";
        my $ip = $::JOB_PARAMS{'NODE_VIP_SC'};
        my $rc = 0;
        my @result;
        my @std_error;
        my $playlist_cnt = sprintf "%02d_", $::JOB_PARAMS{'PLAYLIST_CNT'};

        my $extra_parameters = fetch_user_specified_job_parameters("107_Traffic_Tools_Configure_And_Start");
        $extra_parameters .= " -v PASSWORD_EXPIRE=no" unless ($extra_parameters =~ /-v PASSWORD_EXPIRE=/);
        $extra_parameters .= " -v TRAFFIC_DURATION=indefinite" unless ($extra_parameters =~ /-v TRAFFIC_DURATION=/);
        $extra_parameters .= " -v TRAFFIC_TO_START=automatic" unless ($extra_parameters =~ /-v TRAFFIC_TO_START=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=/);
        $extra_parameters .= " -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no" unless ($extra_parameters =~ /-v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=/);
        $extra_parameters .= " -v INPUT_AVAILABLE=no" unless ($extra_parameters =~ /-v INPUT_AVAILABLE=/);
        $extra_parameters .= " -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes" unless ($extra_parameters =~ /-v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=/);
        $extra_parameters .= " -v KUBECONFIG=$::JOB_PARAMS{'NODE_KUBECONFIG_SC'}";
        $extra_parameters .= " -v KUBECONFIG_TOOLS=$::JOB_PARAMS{'NODE_KUBECONFIG_TOOLS'}";
        # Why doesn't the playlist have this variable? $extra_parameters .= " -v SOFTWARE_DIR=$::JOB_PARAMS{'NODE_SOFTWARE_DIR'}";
        $extra_parameters .= " -w $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/playlist_logs";
        for my $option (split /\|/, $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'}) {
            $extra_parameters .= " -o $option";
        }

        $command .= "$::JOB_PARAMS{'NODE_DAFT_DIR'}/perl/bin/execute_playlist.pl -p 107_Traffic_Tools_Configure_And_Start -j ${playlist_cnt}Start_Traffic_SC_$::JOB_PARAMS{'SC_SOFTWARE_VERSION'} -n $::JOB_PARAMS{'NODE_NETWORK_CONFIG_FILE'}$extra_parameters";

        # Check NODE_VIP_SC node
        General::Logging::log_user_message("$message on node: $ip ($::JOB_PARAMS{'HOSTNAME_SC'})\nThis will take a while to complete.");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ $command ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$message failed on node: $ip");
            push @::JOB_STATUS, "(x) $message failed";
            show_warning_message_for_rerun_of_playlist();
            return 1;
        }

        $::JOB_PARAMS{'PLAYLIST_CNT'}++;

        push @::JOB_STATUS, "(/) $message was successful";

        return $rc;
    }
}

# -----------------------------------------------------------------------------
# Step logic.
#
# Input variables:
#  -
#
# Output variables:
#  - The return code from the step.
#
# Return code values:
#  0: The call was successful.
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
#      General::Playlist_Operations::RC_FALLBACK         (value -1)
#      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
#      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
#      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
#      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
#      General::Playlist_Operations::RC_STEPOUT          (value -6)
#      General::Playlist_Operations::RC_TASKOUT          (value -7)
#
# -----------------------------------------------------------------------------
sub Cleanup_Job_Environment_P011S16 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Software_On_Remote_Workspace_Directory_P011S16T01} );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Fetch_Remote_Workspace_Directory_P011S16T02} );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::902_Cleanup_Job_Environment::main } );
    return $rc if $rc < 0;

    if ($rc == 0) {
        # Mark that the Job was successful
        $::JOB_PARAMS{'JOBSTATUS'} = "SUCCESSFUL";
    }

    return $rc;

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Remove_Software_On_Remote_Workspace_Directory_P011S16T01 {
        return remove_software_on_remote_workspace();
    }

    # -----------------------------------------------------------------------------
    # Task logic.
    #
    # Input variables:
    #  -
    #
    # Output variables:
    #  - The return code from the step.
    #
    # Return code values:
    #  0: The call was successful.
    #  >0: The call failed
    #  <0: Are Playlist specific return code which triggers special actions, for
    #      example:
    #      General::Playlist_Operations::RC_FALLBACK         (value -1)
    #      General::Playlist_Operations::RC_GRACEFUL_EXIT    (value -2)
    #      General::Playlist_Operations::RC_PLAYLISTOUT      (value -3)
    #      General::Playlist_Operations::RC_RERUN_STEP       (value -4)
    #      General::Playlist_Operations::RC_RERUN_TASK       (value -5)
    #      General::Playlist_Operations::RC_STEPOUT          (value -6)
    #      General::Playlist_Operations::RC_TASKOUT          (value -7)
    #
    # -----------------------------------------------------------------------------
    sub Fetch_Remote_Workspace_Directory_P011S16T02 {
        return fetch_remote_workspace();
    }
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P011S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Software_On_Remote_Workspace_Directory_P011S16T01} );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Fetch_Remote_Workspace_Directory_P011S16T02} );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::902_Cleanup_Job_Environment::main } );
    return $rc if $rc < 0;

    return 0;
}

#####################
#                   #
# Other Subroutines #
#                   #
#####################

# -----------------------------------------------------------------------------
sub fetch_remote_host_name {
    my $ip = shift;
    my $hostname = "unknown";
    my $command;
    my $rc;
    my @result;

    $command = "ssh -o ConnectTimeout=5 -o StrictHostKeyChecking=no -o BatchMode=Yes -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd\@$ip hostname";

    General::Logging::log_user_message("Fetching hostname from node: $ip");
    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );
    if ($rc == 0) {
        if (scalar @result == 1) {
            $hostname = $result[0];
        }
    } else {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_error_message("Failed to fetch the hostname from the node");
    }

    return $hostname;
}

# -----------------------------------------------------------------------------
sub fetch_remote_workspace {
    my $command;
    my @commands = ();
    my $rc;

    if ($::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR_CREATED'} ne "yes") {
        General::Logging::log_user_message("No remote node job workspaces has been created");
        return General::Playlist_Operations::RC_TASKOUT;
    }

    # Now we need to download the workspace files from the node(s) with scp
    $command = "${debug_command}scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr";
    if ($::JOB_PARAMS{'NODE_VIP_SC'} =~ /:/) {
        $command .= " -6 eccd\@[$::JOB_PARAMS{'NODE_VIP_SC'}]:$::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/* $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}";
    } else {
        $command .= " eccd\@$::JOB_PARAMS{'NODE_VIP_SC'}:$::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/* $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}";
    }
    push @commands, $command;

    if ($::JOB_PARAMS{'NODE_VIP_SC'} ne $::JOB_PARAMS{'NODE_VIP_TOOLS'}) {
        $command = "${debug_command}scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr";
        if ($::JOB_PARAMS{'NODE_VIP_TOOLS'} =~ /:/) {
            $command .= " -6 eccd\@[$::JOB_PARAMS{'NODE_VIP_TOOLS'}]:$::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/* $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}";
        } else {
            $command .= " eccd\@$::JOB_PARAMS{'NODE_VIP_TOOLS'}:$::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/* $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}";
        }
        push @commands, $command;
    }

    General::Logging::log_user_message("Downloading remote workspace from node(s)\nThis will take a while to complete.");
    $rc = General::OS_Operations::send_command(
        {
            "commands"      => \@commands,
            "hide-output"   => 1,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_error_message("Failed to download the workspace from the node(s)");
        # Ignore any error
        $rc = 0;
    }

    General::Logging::log_user_message("Workspace downloaded from remote node(s) to directory: $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}");

    return $rc;
}

# -----------------------------------------------------------------------------
# This subroutine checks if the specified playlist has any variables that has
# been provided by the user as a job parameter, in such case it returns these
# variables and their values so they can be used in execute_playlist.pl command.
#
sub fetch_user_specified_job_parameters {
    my $playlist = shift;
    my $var = sprintf 'Playlist::%s::playlist_variables', $playlist;
    no strict;                  # Needed because we cannot do "%$var" when "use strict" is enabled, so we temporarilly disable it for the next line
    my %parameters = (%$var);   # If $playlist contains e.g. "001_Deploy_SC" then we read the following variable "%Playlist::001_Deploy_SC::playlist_variables"
    use strict;
    my $extra = "";
    my $debug = 0;

    for my $variable (sort keys %parameters) {
        printf "DBG:line=%d, variable=$variable\n", __LINE__ if ($debug);
        # We need to ignore some parameters because they are used by multiple playlists and
        # each might need different values.
        next if ($variable eq "KUBECONFIG");
        next if ($variable eq "KUBECONFIG_TOOLS");
        next if ($variable eq "SOFTWARE_DIR");

        if (exists $::JOB_PARAMS{$variable} && $::playlist_variables{$variable}{'default_value_used'} eq "no") {
            # The user has specified a variable on the command line, so we should use this value
            $extra .= " -v $variable=$::JOB_PARAMS{$variable}";
        }
    }

    # Add special optional variables owned by the execute_playlist.pl script if specified on the command line
    for my $variable (sort keys %::optional_job_variables) {
        printf "DBG:line=%d, variable=$variable\n", __LINE__ if ($debug);

        if (exists $::JOB_PARAMS{$variable} && $::playlist_variables{$variable}{'default_value_used'} eq "no") {
            # The user has specified a variable on the command line, so we should use this value unless it's already added by 'for' loop above
            $extra .= " -v $variable=$::JOB_PARAMS{$variable}" unless ($extra =~ /-v $variable=/);
        }
    }

    printf "DBG:line=%d, extra=$extra<<\n", __LINE__ if ($debug);
    return $extra;
}

# -----------------------------------------------------------------------------
# This subroutine reads all parameters from the specified network configuration
# file and updates the %::NETWORK_CONFIG_PARAMS and %::JOB_PARAMS hash variables
# after applying filters to remove unwanted parameters.
#
sub parse_network_config_parameters {
    return Playlist::920_Check_Network_Config_Information::parse_network_config_parameters();
}

# -----------------------------------------------------------------------------
sub remove_software_on_remote_workspace {
    my $command = "$debug_command";
    my $ip;
    my $rc = 0;
    my @result;
    my @std_error;

    if ($::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR_CREATED'} ne "yes") {
        General::Logging::log_user_message("No remote node job workspaces has been created");
        return General::Playlist_Operations::RC_TASKOUT;
    }

    $command .= "rm -fr $::JOB_PARAMS{'NODE_JOB_WORKSPACE_DIR'}/software";

    # Check NODE_VIP_SC node
    $ip = $::JOB_PARAMS{'NODE_VIP_SC'};
    General::Logging::log_user_message("Removing software directory (if any) on remote node job workspace: $ip ($::JOB_PARAMS{'HOSTNAME_SC'})");
    $rc = General::OS_Operations::send_command_via_ssh(
        {
            "commands"            => [ $command ],
            "expect-debug"        => 0,
            "hide-output"         => 1,
            "ip"                  => $ip,
            "port"                => 22,
            "password"            => "notneeded",
            "return-output"       => \@result,
            "return-stderr"       => \@std_error,
            "stop-on-error"       => 1,
            "timeout"             => -1,    # No timeout
            "user"                => "eccd",
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_error_message("Software directory removal failed on node: $ip");
        # We don't care if it fails
        $rc = 0;
    }

    if ($::JOB_PARAMS{'NODE_VIP_SC'} ne $::JOB_PARAMS{'NODE_VIP_TOOLS'}) {
        # Check NODE_VIP_TOOLS node
        $ip = $::JOB_PARAMS{'NODE_VIP_TOOLS'};
        General::Logging::log_user_message("Removing software directory (if any) on remote node job workspace: $ip ($::JOB_PARAMS{'HOSTNAME_TOOLS'})");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"            => [ $command ],
                "expect-debug"        => 0,
                "hide-output"         => 1,
                "ip"                  => $ip,
                "port"                => 22,
                "password"            => "notneeded",
                "return-output"       => \@result,
                "return-stderr"       => \@std_error,
                "stop-on-error"       => 1,
                "timeout"             => -1,    # No timeout
                "user"                => "eccd",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Software directory removal failed on node: $ip");
            # We don't care if it fails
            $rc = 0;
        }
    }

    return $rc;
}

# -----------------------------------------------------------------------------
sub set_playlist_variables {
    %playlist_variables = (
        # ---------------------------------------------------------------------
        'ARTIFACTORY_TOKEN' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "cmVmdGtuOjAxOjE3NDM3NTYxMjQ6TkhWdmFvMjVmY2xWZkY3QzJORDRTNVFOWGV1",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls the token name used for fetching the software from the
artifactory. Without a valid value the fetching of the software will fail.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'ARTIFACTORY_URL' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls the token name used for fetching the software from the
artifactory. Without a valid value the fetching of the software will fail.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DEPLOY_UCC' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the deployment will be with Ultra Compact Core (UCC)
which is a tiny deployment of just SEPP with low resource requirements.
See also parameter SKIP_RESERVE_WORKERS_FOR_UCC that determines how the pod's
will be distributed over the worker nodes.

If set to 'yes' then the ENABLED_CNF job variable will be set to 'sepp' unless
it was specified by the user on the command line, in which case that value will
be used instead.
It also means that we will use a special values file that sets reduced resource
values as part of the UCC helm deployment. This file comes either from a template
file from the DAFT package, or taken from a file specified by the user using the
CONFIG_FILE_HELM_CHART_1 job variable.

If set to 'no' which is also the default if not specified, then no special handling
will be done and a normal deployment will be done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'KUBECONFIG' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified then this file will be used as the config file for all helm and
kubectl commands when communicating with the SC deployment.
If you don't specify this parameter then it will take the default file which
is usually called ~/.kube/config.

NOTE:
If the parameters NODE_VIP_SC and NODE_VIP_TOOLS have different values then also
the parameters KUBECONFIG_SC and KUBECONFIG_TOOLS must be different.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'KUBECONFIG_TOOLS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified then this file will be used as the config file for all helm and
kubectl commands when communicating with the TOOLS deployment.
If you don't specify this parameter then it will take the default file which
is usually called ~/.kube/config.

NOTE:
If the parameters NODE_VIP_SC and NODE_VIP_TOOLS have different values then also
the parameters KUBECONFIG_SC and KUBECONFIG_TOOLS must be different.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'MINIMUM_NEEDED_DISK_SPACE' => {
            'case_check'    => "uppercase", # <lowercase|no|uppercase>
            'default_value' => "30000MB",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The minimum free disk space needed in the workspace directory for starting this
playlist.
It needs to be considered that it should be able to have two software packages
(one downloaded copy and one unpacked copy) so if the software package is about
15GB in size you need at least 30GB free space.
The value should be specified as an integer number followed by one of the
following units:
    - K,  M,  G,  T,  P,  E,  Z,  Y  (powers of 1024)
    - KB, MB, GB, TB, PB, EB, ZB, YB (powers of 1000)
EOF
            'validity_mask' => '\d+(K|M|G|T|P|E|Z|Y|KB|MB|GB|TB|PB|EB|ZB|YB)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'NODE_DAFT_DIR' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The directory path where the DAFT package has been unpacked on the node.
If the parameter is not set then a new DAFT package will be created and uploaded
to the node(s) and unpacked under the job workspace directory on the node.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'NODE_SOFTWARE_DIR' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The directory path where the software is stored on the node.

If the parameter is not set then the software will first be downloaded to the
current server and placed under the job workspace directory and then it will be
uploaded to the node(s) and stored temporarilly under the top level job workspace
directory.

If the parameter is specified then the specified directory on the node will be
used for storing the software.
The directory should point to the top-level directory for the software to be
used and under this directory there should be a number of sub-directories
for example csar, tools etc.
For example: NODE_SOFTWARE_DIR=/home/eccd/download/1.12.0+43
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'NODE_VIP_SC' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The IP-address or hostname for connecting to the cluster where the SC software
will be deployed.
If not specified then the address is taken from the network configuration file
parameter 'cluster_ssh_vip'.

It must be possible to login to the node using the 'eccd' user and with no
password i.e. the user must have it's SSH public key stored in the file
'~/.ssh/authorized_keys' on the node.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'NODE_VIP_TOOLS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The IP-address or hostname for connecting to the cluster where the SC software
will be deployed.
If not specified then the address is taken from the network configuration file
parameter 'cluster_ssh_vip_tools'.

It must be possible to login to the node using the 'eccd' user and with no
password i.e. the user must have it's SSH public key stored in the file
'~/.ssh/authorized_keys' on the node.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'NODE_WORKSPACE_DIR' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "/home/eccd/workspaces",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The directory path where the job workspace directory will be created on the node.
The node job workspace directory will be created under the specified path and inside
this job workspace path will be created all directories for executed playlists
on the node.
The top level job workspace on the node will have the same name as the workspace
for the current executing job.
For example if the job workspace for this job is called:
/proj/CBA_comp/esc/daft/workspaces/Snorlax_Deploy_Traffic_20231206_142457_32/
Then the job workspace on the node might look something like this:
 - /home/eccd/workspaces/Snorlax_Deploy_Traffic_20231206_142457_32/
   - daft/
   - kubeconfig_files/
   - playlist_logs/
   - software/
     - 1.12.0+43/
   - Snorlax.xml
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'REUSE_ALREADY_DOWNLOADED_SOFTWARE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the already downloaded software should be reused or
if it should always be downloaded again.

If value is 'yes' then the software will not be downloaded again if the directory
already exist.

If value is 'no' then the software will always be downloaded, regardless if it
already exist.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SC_SOFTWARE_VERSION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The SC software version to download and deploy.
This parameter together with NODE_SOFTWARE_DIR will be used to populate the
SOFTWARE_DIR parameter.
EOF
            'validity_mask' => '\d+\.\d+\.\d+[+-]\S+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_ADD_USERS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certain tasks in a step should be skipped.
By default when value is 'no' then all tasks in the step will be executed.
If the value is set to 'yes' then the tasks in the step is skipped, which might
be useful when certain tasks has already been done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_ALL' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if all tasks in this playlist should be skipped.

By default when value is 'no' then all tasks in the step will be executed, as
indicated by the SKIP_xxxx value for each task, it will not change any of the
SKIP_xxxx values.

If the value is set to 'yes' then all the tasks in the step are skipped i.e.
SKIP_xxxx values for all variables in this playlist are all set to "yes", except
for the SKIP_xxxx parameters which are specififcally set by the user on the
command line.
This might be useful when only certain task should be executed and you don't
want to set all SKIP_xxxx parameters to 'yes'.
Setting SKIP_ALL=yes will update the following variables:
    - SKIP_ADD_USERS
    - SKIP_CLEAN_REGISTRY
    - SKIP_CONFIGURE_TRAFFIC
    - SKIP_DEPLOY_SC
    - SKIP_DEPLOY_TOOLS
    - SKIP_DOWNLOAD_SOFTWARE{
    - SKIP_INSTALL_CERTIFICATES
    - SKIP_REMOVE_NODE_LABELS_FOR_TOOLS
    - SKIP_REMOVE_NODE_LABELS_FOR_UCC
    - SKIP_RESERVE_WORKERS_FOR_TOOLS
    - SKIP_RESERVE_WORKERS_FOR_UCC
    - SKIP_START_TRAFFIC
    - SKIP_UNDEPLOY_SC
    - SKIP_UNDEPLOY_TOOLS
NOTE: It should be used together with SKIP_xxxx=no on at least one other task
that should be executed.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_CLEAN_REGISTRY' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certain tasks in a step should be skipped.
By default when value is 'no' then all tasks in the step will be executed.
If the value is set to 'yes' then the tasks in the step is skipped, which might
be useful when certain tasks has already been done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_CONFIGURE_TRAFFIC' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certain tasks in a step should be skipped.
By default when value is 'no' then all tasks in the step will be executed.
If the value is set to 'yes' then the tasks in the step is skipped, which might
be useful when certain tasks has already been done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_DEPLOY_SC' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certain tasks in a step should be skipped.
By default when value is 'no' then all tasks in the step will be executed.
If the value is set to 'yes' then the tasks in the step is skipped, which might
be useful when certain tasks has already been done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_DEPLOY_TOOLS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certain tasks in a step should be skipped.
By default when value is 'no' then all tasks in the step will be executed.
If the value is set to 'yes' then the tasks in the step is skipped, which might
be useful when certain tasks has already been done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_DOWNLOAD_SOFTWARE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certain tasks in a step should be skipped.
By default when value is 'no' then all tasks in the step will be executed.
If the value is set to 'yes' then the tasks in the step is skipped, which might
be useful when certain tasks has already been done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_INSTALL_CERTIFICATES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certain tasks in a step should be skipped.
By default when value is 'no' then all tasks in the step will be executed.
If the value is set to 'yes' then the tasks in the step is skipped, which might
be useful when certain tasks has already been done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_REMOVE_NODE_LABELS_FOR_TOOLS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certain tasks in a step should be skipped.
By default when value is 'no' then all tasks in the step will be executed.
If the value is set to 'yes' then the tasks in the step is skipped, which might
be useful when certain tasks has already been done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_REMOVE_NODE_LABELS_FOR_UCC' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certain tasks in a step should be skipped.
By default when value is 'no' then all tasks in the step will be executed.
If the value is set to 'yes' then the tasks in the step is skipped, which might
be useful when certain tasks has already been done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_RESERVE_WORKERS_FOR_TOOLS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certain tasks in a step should be skipped.
By default when value is 'no' then all tasks in the step will be executed.
If the value is set to 'yes' then the tasks in the step is skipped, which might
be useful when certain tasks has already been done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_RESERVE_WORKERS_FOR_UCC' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if an UCC deployment (see DEPLOY_UCC) will have the pod's
distributed on only one or all worker nodes.

If the value is set to 'yes' then the SC pod's will be distributed on all available
worker nodes, i.e. no worker will be reserved.

If the value is set to 'no' then it will cause one worker node to be reserved
(with lowest name) with a special label 'usage=ucc' and a taint usage=ucc:PreferNoSchedule
applied that will force other non-UCC pod to be deployed on other nodes if possible.
It will also set the network config option 'UCC' that will load special helm
parameters from the network config file, for example the following is added:
global.nodeSelector.usage=ucc

This parameter is only checked if also DEPLOY_UCC=yes.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_START_TRAFFIC' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certain tasks in a step should be skipped.
By default when value is 'no' then all tasks in the step will be executed.
If the value is set to 'yes' then the tasks in the step is skipped, which might
be useful when certain tasks has already been done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_UNDEPLOY_SC' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certain tasks in a step should be skipped.
By default when value is 'no' then all tasks in the step will be executed.
If the value is set to 'yes' then the tasks in the step is skipped, which might
be useful when certain tasks has already been done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },
        # ---------------------------------------------------------------------
        'SKIP_UNDEPLOY_TOOLS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certain tasks in a step should be skipped.
By default when value is 'no' then all tasks in the step will be executed.
If the value is set to 'yes' then the tasks in the step is skipped, which might
be useful when certain tasks has already been done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        # NOTE:
        # Override the "mandatory" and "default_value" attributes for the
        # following variables since we will automatically add the needed values
        # before calling the playlists by taking the values from other variables.
        # ---------------------------------------------------------------------

        # ---------------------------------------------------------------------
        'SOFTWARE_DIR' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the base directory that holds all the software to be deployed or
upgraded on the node, i.e. the directory that contains the 'eric-sc-*.csar' and
'eric-sc-tools-*.tgz' files.

NOTE:
For this playlist the variable should point to the directory on the current
server where the software is stored or will be downloaded to from Artifactory.

If not specified then the downloaded software will be fetched from Artifactory
and stored in the current job workspace directory and then transferred to the
SC (and TOOLS) node directory as specified with the NODE_SOFTWARE_DIR parameter.
After completed job this software directory on the current server will be deleted.

If specified then it will download the software to this directory, note that
the last part of the path should match with the name from the SC_SOFTWARE_VERSION
variable.
For example if SOFTWARE_DIR=/home/CBA_comp/esc/download/1.12.0+43 then
SC_SOFTWARE_VERSION should have the value '1.12.0+43' otherwise another sub-directory
level will be created with this value.
If the software directory already exist then the software will be downloaded
again, or not, based on the value of the REUSE_ALREADY_DOWNLOADED_SOFTWARE
parameter.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TOOLS_TO_INSTALL' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "chfsim,dscload,k6,nrfsim,sc-monitor,seppsim,sftp",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which tools to install on the node and it will be used
for building a new tools package and then for installing the tools on the node
in the namespace specified by TOOLS_NAMESPACE.

Multiple tools can be specified by separating each with a space or comma.

Currently allowed tools are:
    - chfsim
    - dscload
    - k6
    - nrfsim
    - sc-monitor
    - seppsim
    - sftp
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TOOLS_TO_REMOVE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "chfsim,dscload,k6,nrfsim,sc-monitor,seppsim,sftp",
            'mandatory'     => "no",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which tools to remove on the node in the namespace
specified by TOOLS_NAMESPACE.

Multiple tools can be specified by separating each with a space or comma.

Currently allowed tools are:
    - chfsim
    - dscload
    - k6
    - nrfsim
    - sc-monitor
    - seppsim
    - sftp
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_UNDEPLOY_TOOLS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if certain tasks in a step should be skipped.
By default when value is 'no' then all tasks in the step will be executed.
If the value is set to 'yes' then the tasks in the step is skipped, which might
be useful when certain tasks has already been done.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub show_warning_message_for_rerun_of_playlist {
    General::Logging::log_user_warning_message(
        "Please note that if you SELECT ANYTHING OTHER THAN 'f' to fallback it will connect again to the remote node\n" .
        "and start a new playlist execution.\n" .
        "This means that it creates a new workspace directory and then executes the playlist from the beginning,\n" .
        "THERE IS NO 'c' continue or 'r' rerun from the task where it failed on the remote node.\n" .
        "So only use 'c' or 'r' if you know that executing the playlist again from the beginning will work.\n"
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist script is used for performing Un-Deployment and Deployment of SC
and tools software and for adding users, certificates, configuration data and
for starting traffic.
It will be called from the execute_playlist.pl script.

Used Job Parameters:
====================
EOF
    my %temp_hash = usage_return_playlist_variables();

    General::Playlist_Operations::print_info_about_job_variables(\%temp_hash);
    General::Playlist_Operations::print_used_job_variables(__FILE__, \%temp_hash);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

# -----------------------------------------------------------------------------
sub usage_return_playlist_variables {
    return (
        %Playlist::001_Deploy_SC::playlist_variables,
        %Playlist::003_Undeploy_SC::playlist_variables,
        %Playlist::004_Config_Management::playlist_variables,
        %Playlist::005_User_Management::playlist_variables,
        %Playlist::007_Certificate_Management::playlist_variables,
        %Playlist::102_Supreme_Certificate_Management::playlist_variables,
        %Playlist::103_Tools_Management::playlist_variables,
        %Playlist::106_Traffic_Tools_Remove::playlist_variables,
        %Playlist::107_Traffic_Tools_Configure_And_Start::playlist_variables,
        %Playlist::108_Miscellaneous_Tasks::playlist_variables,
        %Playlist::109_Traffic_Tools_Install::playlist_variables,
        %playlist_variables
    );
}

1;
