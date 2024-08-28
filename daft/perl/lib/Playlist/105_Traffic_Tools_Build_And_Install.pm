package Playlist::105_Traffic_Tools_Build_And_Install;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.29
#  Date     : 2024-06-13 12:20:00
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

################################################################################
# NOTE  NOTE  NOTE  NOTE  NOTE  NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE
#
# This playlist is no longer maintained and should not be used for any new
# deployments of tools.
# Instead the 109_Traffic_Tools_Install playlist should be used.
#
# At some point in time this playlist will be removed.
#
# NOTE  NOTE  NOTE  NOTE  NOTE  NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE
################################################################################

use strict;
use Cwd qw(abs_path cwd);

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    usage
    usage_return_playlist_variables
    );

use ADP::Kubernetes_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::102_Supreme_Certificate_Management;
use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;

#
# Variable Declarations
#
my %playlist_variables;
set_playlist_variables();

my $debug_command = "";

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
#  1: The call failed
#  255: Stepout wanted
#
# -----------------------------------------------------------------------------
sub main {
    my $rc;

    # Set Job Type
    $::JOB_PARAMS{'JOBTYPE'} = "TRAFFIC_TOOLS_BUILD_AND_INSTALL";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # If the playlist needs to have access to Network Configuration parameters
    # then it needs to check for this and give an error or ask the user to
    # provide the file name.
    if (General::Playlist_Operations::is_mandatory_network_config_loaded() != 0) {
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    # We always want to read the network config file for each playlist
    General::State_Machine::always_execute_task("Playlist::920_Check_Network_Config_Information::.+");

    # To avoid removing directories before all tasks has been executed we will
    # only execute the cleanup at the end.
    General::State_Machine::always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

    # Show deprecated message to user.
    # --------------------------------
    $rc = General::Playlist_Operations::show_checkpoint_message("This playlist is no longer maintained and should not be used for any new deployments of tools.\nInstead the 109_Traffic_Tools_Install playlist should be used.\nIf you choose to continue anyway then there is a possibility that the deployment will fail or not be complete!\n");
    return $rc if $rc != 0;

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P105S01, \&Fallback001_P105S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P105S02, \&Fallback001_P105S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Build_And_Install_Tools_P105S03, \&Fallback001_P105S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Collect_ADP_Logs_P105S04, \&Fallback001_P105S99 );
    return $rc if $rc < 0;

    # Now we need to execute the cleanup.
    General::State_Machine::remove_always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P105S05, \&Fallback001_P105S99 );
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
sub Initialize_Job_Environment_P105S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::901_Initialize_Job_Environment::main } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'NAMESPACE_CREATED'} = "no";

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
sub Check_Job_Parameters_P105S02 {

    my $rc = 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_GIT_DIRECTORY_P105S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_NODE_VIP_ADDRESS_P105S02T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_TOOLS_TO_INSTALL_P105S02T03 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Kube_Config_File_P105S02T04 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Workspace_Certificate_Directory_P105S02T05 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_TOOLS_NAMESPACE_P105S02T06 } );
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
    sub Check_GIT_DIRECTORY_P105S02T01 {
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Checking Job parameter 'GIT_DIRECTORY'");
        if ($::JOB_PARAMS{'GIT_DIRECTORY'} eq "") {
            # Check if current directory is inside a GIT repository
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "git rev-parse --show-toplevel",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0 && scalar @result == 1 && -d "$result[0]") {
                # We seem to be inside a GIT repository directory
                $::JOB_PARAMS{'GIT_DIRECTORY'} = $result[0];
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Job parameter 'GIT_DIRECTORY' was not specified and current working directory is not inside a GIT repository tree");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        }

        # Check that the GIT directory structure contains the expected sub-directories
        if (-d "$::JOB_PARAMS{'GIT_DIRECTORY'}/daft" &&
            -d "$::JOB_PARAMS{'GIT_DIRECTORY'}/simulators/dscload" &&
            -d "$::JOB_PARAMS{'GIT_DIRECTORY'}/devtools/k6" &&
            -d "$::JOB_PARAMS{'GIT_DIRECTORY'}/simulators/seppsim" &&
            -d "$::JOB_PARAMS{'GIT_DIRECTORY'}/simulators/nrfsim") {

            General::Logging::log_user_message("Found the expected 'daft/', 'simulators/dscload/', 'devtools/k6/', 'simulators/seppsim/' and 'simulators/nrfsim/' sub-directories under:\n$::JOB_PARAMS{'GIT_DIRECTORY'}");
        } else {
            General::Logging::log_user_error_message("Did not find the expected 'daft/', 'simulators/dscload/', 'devtools/k6/', 'simulators/seppsim/' and 'simulators/nrfsim/' sub-directories under:\n$::JOB_PARAMS{'GIT_DIRECTORY'}");
            $rc = General::Playlist_Operations::RC_FALLBACK;
        }

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
    sub Check_NODE_VIP_ADDRESS_P105S02T02 {
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Checking Job parameter 'NODE_VIP_ADDRESS' and Network Config parameter 'node_vip_address'");
        if ($::JOB_PARAMS{'NODE_VIP_ADDRESS'} eq "" && exists $::NETWORK_CONFIG_PARAMS{'node_vip_address'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'node_vip_address'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'NODE_VIP_ADDRESS'} = $::NETWORK_CONFIG_PARAMS{'node_vip_address'}{'value'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'node_vip_address' has not been set and Job parameter 'NODE_VIP_ADDRESS' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'NODE_VIP_ADDRESS', nor Network Config parameter 'node_vip_address' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        General::Logging::log_user_message("Checking if SSH access to $::JOB_PARAMS{'NODE_VIP_ADDRESS'} is possible");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o UserKnownHostsFile=/dev/null -o loglevel=ERROR $::JOB_PARAMS{'NODE_USER'}\@$::JOB_PARAMS{'NODE_VIP_ADDRESS'} hostname",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            # Check that DAFT is running on a different node by comparing 'hostname' printouts
            my $remote_hostname = "";
            my $my_hostname = "";
            if (scalar @result == 1) {
                $remote_hostname = $result[0];
            }
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "hostname",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if (scalar @result == 1) {
                $my_hostname = $result[0];
            }

            if ($my_hostname eq "" || $remote_hostname eq "") {
                General::Logging::log_user_message("Current hostname ($my_hostname) and/or remote hostname ($remote_hostname) is empty, just assuming different hosts");
                $::JOB_PARAMS{'CURRENT_AND_REMOTE_SAME_HOSTS'} = "no";
            } elsif ($my_hostname eq $remote_hostname) {
                General::Logging::log_user_message("Current and remote hostname ($remote_hostname) are the same host");
                $::JOB_PARAMS{'CURRENT_AND_REMOTE_SAME_HOSTS'} = "yes";
            } else {
                General::Logging::log_user_message("Current hostname ($my_hostname) and remote hostname ($remote_hostname) are different hosts");
                $::JOB_PARAMS{'CURRENT_AND_REMOTE_SAME_HOSTS'} = "no";
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to connect with SSH to $::JOB_PARAMS{'NODE_VIP_ADDRESS'}");
            return General::Playlist_Operations::RC_FALLBACK;
        }

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
    sub Check_TOOLS_TO_INSTALL_P105S02T03 {
        my $rc = 0;

        General::Logging::log_user_message("Checking Job parameter 'TOOLS_TO_INSTALL'");
        if ($::JOB_PARAMS{'TOOLS_TO_INSTALL'} eq "") {
            General::Logging::log_user_error_message("Job parameter 'TOOLS_TO_INSTALL' not specified");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        $::JOB_PARAMS{'TOOLS_TO_INSTALL'} =~ s/\s+/,/g;
        $::JOB_PARAMS{'TOOLS_TO_INSTALL'} =~ s/,+/,/g;

        # Set default values for all traffic simulators
        $::JOB_PARAMS{'DSCLOAD_WANTED'} = "no";
        $::JOB_PARAMS{'K6_WANTED'} = "no";
        $::JOB_PARAMS{'NRFSIM_WANTED'} = "no";
        $::JOB_PARAMS{'SEPPSIM_WANTED'} = "no";
        $::JOB_PARAMS{'SFTP_WANTED'} = "no";

        for my $tool (split /,/, $::JOB_PARAMS{'TOOLS_TO_INSTALL'}) {
            if ($tool eq "dscload") {
                $::JOB_PARAMS{'DSCLOAD_WANTED'} = "yes";
            } elsif ($tool eq "k6") {
                $::JOB_PARAMS{'K6_WANTED'} = "yes";
            } elsif ($tool eq "nrfsim") {
                $::JOB_PARAMS{'NRFSIM_WANTED'} = "yes";
            } elsif ($tool eq "seppsim") {
                $::JOB_PARAMS{'SEPPSIM_WANTED'} = "yes";
            } elsif ($tool eq "sftp") {
                $::JOB_PARAMS{'SFTP_WANTED'} = "yes";
            } else {
                General::Logging::log_user_warning_message("Unknown tool '$tool' specified in job parameter 'TOOLS_TO_INSTALL', it's ignored");
            }
        }

        # Check that we have at least one tool to build and install
        if ($::JOB_PARAMS{'DSCLOAD_WANTED'} eq "no" &&
            $::JOB_PARAMS{'K6_WANTED'} eq "no" &&
            $::JOB_PARAMS{'NRFSIM_WANTED'} eq "no" &&
            $::JOB_PARAMS{'SEPPSIM_WANTED'} eq "no" &&
            $::JOB_PARAMS{'SFTP_WANTED'} eq "no") {

            General::Logging::log_user_error_message("Job parameter 'TOOLS_TO_INSTALL' contains no valid tools: $::JOB_PARAMS{'TOOLS_TO_INSTALL'}");
            return General::Playlist_Operations::RC_FALLBACK;
        }
        my $message = "DSCLOAD_WANTED: $::JOB_PARAMS{'DSCLOAD_WANTED'}\n";
        $message .= "K6_WANTED:      $::JOB_PARAMS{'K6_WANTED'}\n";
        $message .= "NRFSIM_WANTED:  $::JOB_PARAMS{'NRFSIM_WANTED'}\n";
        $message .= "SEPPSIM_WANTED: $::JOB_PARAMS{'SEPPSIM_WANTED'}\n";
        $message .= "SFTP_WANTED:    $::JOB_PARAMS{'SFTP_WANTED'}\n";
        General::Logging::log_user_message($message);

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
    sub Check_Kube_Config_File_P105S02T04 {
        my $rc = 0;
        my @result;
        my %nodes;

        General::Logging::log_user_message("Fetch node information directly from the cluster using kubectl command");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get nodes -o wide --no-headers --request-timeout=30s",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            push @::JOB_STATUS, '(x) Connection to Cluster with kube config file failed';
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to connect to cluster with kube config file");
            return General::Playlist_Operations::RC_FALLBACK;
        }
        for (@result) {
            if (/^(\S+)\s+Ready\s+\S+\s+\S+\s+\S+\s+(\S+)\s+.+/) {
                #master-0-eccd-snorlax-ds                Ready   control-plane   5d23h   v1.25.3   10.10.10.111   <none>   SUSE Linux Enterprise Server 15 SP4   5.14.21-150400.24.33-default   containerd://1.6.12
                $nodes{$1}{'kubectl_ip'} = $2;
            }
        }

        General::Logging::log_user_message("Fetch node information from the cluster using ssh to $::JOB_PARAMS{'NODE_VIP_ADDRESS'}");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o UserKnownHostsFile=/dev/null -o loglevel=ERROR $::JOB_PARAMS{'NODE_USER'}\@$::JOB_PARAMS{'NODE_VIP_ADDRESS'} kubectl get nodes -o wide --no-headers",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to connect to cluster via ssh");
            return General::Playlist_Operations::RC_FALLBACK;
        }
        for (@result) {
            if (/^(\S+)\s+Ready\s+\S+\s+\S+\s+\S+\s+(\S+)\s+.+/) {
                #master-0-eccd-snorlax-ds                Ready   control-plane   5d23h   v1.25.3   10.10.10.111   <none>   SUSE Linux Enterprise Server 15 SP4   5.14.21-150400.24.33-default   containerd://1.6.12
                $nodes{$1}{'ssh_ip'} = $2;
            }
        }

        my $mismatch = 0;
        for my $key (keys %nodes) {
            if (exists $nodes{$key}{'kubectl_ip'} && exists $nodes{$key}{'ssh_ip'}) {
                if ($nodes{$key}{'kubectl_ip'} ne $nodes{$key}{'ssh_ip'}) {
                    $mismatch = 1;
                    last;
                }
            } else {
                $mismatch = 1;
                last;
            }
        }

        if ($mismatch == 0) {
            General::Logging::log_user_message("The kubectl command and ssh via NODE_VIP_ADDRESS seems to be addressing the same node.");
        } else {
            my $message = "The kubectl command and ssh via NODE_VIP_ADDRESS seems to be addressing different nodes.\n";
            $message .= "KUBECONFIG:       $::JOB_PARAMS{'KUBECONFIG'}\n";
            $message .= "NODE_VIP_ADDRESS: $::JOB_PARAMS{'NODE_VIP_ADDRESS'}\n\n";
            $message .= sprintf "%50s  %20s  %20s\n",
                "Node Name",
                "kubectl IP",
                "ssh IP";
            $message .= sprintf "%50s  %20s  %20s\n",
                "-"x50,
                "-"x20,
                "-"x20;
            for my $key (sort keys %nodes) {
                $message .= sprintf "%50s  %20s  %20s\n",
                    $key,
                    (exists $nodes{$key}{'kubectl_ip'} ? $nodes{$key}{'kubectl_ip'} : "-"),
                    (exists $nodes{$key}{'ssh_ip'} ? $nodes{$key}{'ssh_ip'} : "-");
            }
            General::Logging::log_user_error_message($message);
            return General::Playlist_Operations::RC_FALLBACK;
        }

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
    sub Create_Workspace_Certificate_Directory_P105S02T05 {
        my $rc = 0;

        General::Logging::log_user_message("Creating workspace certificates directory");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "mkdir -p $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/certificates",
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            if ($::JOB_PARAMS{'CERTIFICATE_DIRECTORY'} eq "") {
                # The user did not specify a specific certificate directory, use the one in the workspace
                $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'} = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/certificates";
            }
        } else {
            # Display the result in case of error
            push @::JOB_STATUS, '(x) Failed to create workspace certificates directory';
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to create workspace certificates directory");
            $rc = General::Playlist_Operations::RC_FALLBACK;
        }

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
    sub Check_TOOLS_NAMESPACE_P105S02T06 {
        my $rc = 0;
        my $tools_namespace = "";

        General::Logging::log_user_message("Checking Job parameter 'TOOLS_NAMESPACE' and Network Config parameter 'tools_namespace'");
        if ($::JOB_PARAMS{'TOOLS_NAMESPACE'} ne "") {
            $tools_namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'tools_namespace'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'tools_namespace'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'TOOLS_NAMESPACE'} = $::NETWORK_CONFIG_PARAMS{'tools_namespace'}{'value'};
                $tools_namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'tools_namespace' has not been set and Job parameter 'TOOLS_NAMESPACE' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'TOOLS_NAMESPACE', nor Network Config parameter 'tools_namespace' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        General::Logging::log_user_message("TOOLS_NAMESPACE=$tools_namespace");

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
sub Build_And_Install_Tools_P105S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Tools_Namespace_On_Node_P105S03T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Certificates_From_Node_P105S03T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_And_Install_Tools_Certificates_On_Node_P105S03T03 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Certificates_To_Node_P105S03T04 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'DSCLOAD_WANTED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Build_DSCLOAD_P105S03T05 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Unpack_DSCLOAD_File_And_Change_Values_File_P105S03T06 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_DSCLOAD_P105S03T07 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'K6_WANTED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Build_K6_P105S03T08 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_K6_P105S03T09 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Certificates_To_K6_Pods_P105S03T10 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'NRFSIM_WANTED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Build_NRFSIM_P105S03T11 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_NRFSIM_P105S03T12 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'SEPPSIM_WANTED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Build_SEPPSIM_P105S03T13 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_SEPPSIM_P105S03T14 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'SFTP_WANTED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Build_SFTP_P105S03T15 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_SFTP_P105S03T16 } );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Pod_Status_P105S03T17 } );
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
    sub Create_Tools_Namespace_On_Node_P105S03T01 {
        my $found = 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Checking if namespace $::JOB_PARAMS{'TOOLS_NAMESPACE'} exist on the node");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} get namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check namespaces");
            return 1;
        }

        for (@result) {
            if (/^$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+Active\s+\S+\s*$/) {
                $found = 1;
                last;
            }
        }
        if ($found == 0) {
            General::Logging::log_user_message("Creating namespace $::JOB_PARAMS{'TOOLS_NAMESPACE'} on the node");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} create namespace $::JOB_PARAMS{'TOOLS_NAMESPACE'}",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to create namespace");
                return 1;
            }
        } else {
            General::Logging::log_user_warning_message("Namespace $::JOB_PARAMS{'TOOLS_NAMESPACE'} already exist, thist might result in failures later if it's not empty");
        }

        $::JOB_PARAMS{'NAMESPACE_CREATED'} = "yes";

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
    sub Copy_Certificates_From_Node_P105S03T02 {
        my $rc = 0;

        if ($::JOB_PARAMS{'CURRENT_AND_REMOTE_SAME_HOSTS'} eq "no") {
            General::Logging::log_user_message("Copying Certificate directory from node $::JOB_PARAMS{'NODE_USER'}\@$::JOB_PARAMS{'NODE_VIP_ADDRESS'}:$::JOB_PARAMS{'NODE_CERTIFICATE_DIRECTORY'}");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr $::JOB_PARAMS{'NODE_USER'}\@$::JOB_PARAMS{'NODE_VIP_ADDRESS'}:$::JOB_PARAMS{'NODE_CERTIFICATE_DIRECTORY'}/* $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/certificates/",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy certificates to $::JOB_PARAMS{'NODE_VIP_ADDRESS'}");
                return 1;
            }
        } else {
            General::Logging::log_user_message("Current and Remote hostname are the same, so no need to copy the Certificates");
        }

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
    sub Create_And_Install_Tools_Certificates_On_Node_P105S03T03 {
        my $rc = 0;
        my @result;

        # Set 102 playlist variables
        $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} = "";
        $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} = "";
        $::JOB_PARAMS{'GENERATE_CERTIFICATES'} = "yes";
        $::JOB_PARAMS{'INSTALL_CERTIFICATES'} = "yes";
        $::JOB_PARAMS{'NAMESPACE'} = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
        if ($::JOB_PARAMS{'DSCLOAD_WANTED'} eq "yes") {
            $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} .= "dscload,";
            $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} .= "dscload,";
        }
        if ($::JOB_PARAMS{'K6_WANTED'} eq "yes") {
            $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} .= "k6,";
            $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} .= "k6,";
        }
        if ($::JOB_PARAMS{'NRFSIM_WANTED'} eq "yes") {
            $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} .= "nrfsim,";
            $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} .= "nrfsim,";
        }
        if ($::JOB_PARAMS{'SEPPSIM_WANTED'} eq "yes") {
            $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} .= "seppsim,";
            $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} .= "seppsim,";
        }
        $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} =~ s/,$//g;
        $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} =~ s/,$//g;

        if ($::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} eq "" && $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} eq "") {
            General::Logging::log_user_message("No Certificates or Secrets to Create and Install");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Generate and install tool secrets
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::102_Supreme_Certificate_Management::main } );
        return $rc if $rc < 0;

        # Set Job Type again since it was changed inside the 102 playlist
        $::JOB_PARAMS{'JOBTYPE'} = "TRAFFIC_TOOLS_BUILD_AND_INSTALL";

        # Create special RP_206033.crt and RP_206033.key files for the SEPP traffic
        if ($::JOB_PARAMS{'SEPPSIM_WANTED'} eq "yes" && $::JOB_PARAMS{'K6_WANTED'} eq "yes") {

            #
            # Creating RP_206033.crt and RP_206033.key files
            #

            General::Logging::log_user_message("Creating RP_206033_input.txt file");
            $rc = General::File_Operations::write_file(
                {
                    "filename"            => "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/RP_206033_input.txt",
                    "output-ref"          => [
                        '[req]',
                        'distinguished_name = dn',
                        'x509_extensions = v3_req',
                        'req_extensions = v3_req',
                        'prompt = no',
                        '',
                        '[dn]',
                        'C = BE',
                        'L = Brussels',
                        'O = Ericsson',
                        'CN = nfamf.region1.amf.5gc.mnc033.mcc206.3gppnetwork.org',
                        '',
                        '[v3_req]',
                        'subjectAltName = @alt_names',
                        '',
                        '[alt_names]',
                        'DNS.1 = *.region1.amf.5gc.mnc033.mcc206.3gppnetwork.org',
                    ],
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to write the RP_206033_input.txt file");
                return 1;
            }

            General::Logging::log_user_message("Creating RP_206033.key file");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "openssl req -nodes -newkey rsa:2048 -keyout $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/RP_206033.key -out $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/csr206033.pem -config $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/RP_206033_input.txt",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to create the RP_206033.key file");
                return 1;
            }

            General::Logging::log_user_message("Creating RP_206033.crt file");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "openssl x509 -req -in $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/csr206033.pem -CA $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/rootca/cert.pem -CAkey $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/rootca/key.pem -CAcreateserial -days $::JOB_PARAMS{'CERTIFICATE_VALIDITY_DAYS'} -out $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/RP_206033.crt -extensions 'v3_req' -extfile $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/RP_206033_input.txt",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to create the RP_206033.crt file");
                return 1;
            }
        }

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
    sub Copy_Certificates_To_Node_P105S03T04 {
        my $rc = 0;

        if ($::JOB_PARAMS{'CURRENT_AND_REMOTE_SAME_HOSTS'} eq "no") {
            General::Logging::log_user_message("Copying Certificate directory to node $::JOB_PARAMS{'NODE_USER'}\@$::JOB_PARAMS{'NODE_VIP_ADDRESS'}:$::JOB_PARAMS{'NODE_CERTIFICATE_DIRECTORY'}");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -pr $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/certificates/* $::JOB_PARAMS{'NODE_USER'}\@$::JOB_PARAMS{'NODE_VIP_ADDRESS'}:$::JOB_PARAMS{'NODE_CERTIFICATE_DIRECTORY'}",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy certificates to $::JOB_PARAMS{'NODE_VIP_ADDRESS'}");
                return 1;
            }
        } else {
            General::Logging::log_user_message("Current and Remote hostname are the same, so no need to copy the Certificates");
        }

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
    sub Build_DSCLOAD_P105S03T05 {

        my $current_dir = `pwd`;
        $current_dir =~ s/[\r\n]//g;
        my $filename = "";
        my $rc = 0;
        my @result;
        my $tool = "dscload";

        chdir "$::JOB_PARAMS{'GIT_DIRECTORY'}/simulators/$tool";

        General::Logging::log_user_message("Building $tool.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "make clean build",
                "hide-output"   => 1,
                "return-output" => \@result,
             }
        );

        chdir "$current_dir";

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to build $tool");
            return 1;
        }

        # Look for the built filename
        for (@result) {
            if (/^\s+"path"\s+:\s+"\/(eric-$tool-\d+\.\d+\.\d+-\S+\.tgz)",\s*$/) {
                #  "path" : "/eric-k6-1.11.25-h764e6e0e49.tgz",
                $filename = $1;
                last;
            }
        }

        if ($filename ne "") {
            if (-f "$::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename") {
                $::JOB_PARAMS{'FILE_NAME_DSCLOAD'} = $filename;
                General::Logging::log_user_message("File for $tool is: $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename");
                push @::JOB_STATUS, "(/) $tool build was successful";
            } else {
                General::Logging::log_user_error_message("File for $tool not found: $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename");
                return 1;
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("No file found for $tool");
            return 1;
        }

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
    sub Unpack_DSCLOAD_File_And_Change_Values_File_P105S03T06 {

        my $found = 0;
        my $ip = "";
        my $ip_version = 4;
        my $prefix;
        my $rc = 0;
        my @result;

        # covers the settings for ipv6 only clusters
        if (exists $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_Diameter'}{'value'} &&
            $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_Diameter'}{'value'} !~ /CHANGEME/) {
            $ip = $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_Diameter'}{'value'};
            $ip =~ s/'//g;
            if ($ip =~ /:/) {
                $ip_version = 6;
            }
        }

        # if the cluster is DS, the deployment variants need to be checked
        if ($ip_version == 4) {
            $ip = $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} eq "IntIPv6ExtIPv6" ? $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_Diameter'}{'value_IntIPv6ExtIPv6'} : $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_Diameter'}{'value'};
            $ip =~ s/'//g;
            $ip_version = $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} eq "IntIPv6ExtIPv6" ? 6 : 4;
        }

        General::Logging::log_user_message("Using following IP: $ip with following ip version: $ip_version with ".$::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'});

        if ($ip eq "") {
            General::Logging::log_user_error_message("Unable to find the 'eric_sc_values_anchor_parameter_VIP_SIG_Diameter' value in the network config file");
            return 1;
        }

        General::Logging::log_user_message("Unpacking the dscload helm chart files");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "tar xf $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$::JOB_PARAMS{'FILE_NAME_DSCLOAD'} -C $::JOB_PARAMS{'_JOB_CONFIG_DIR'}",
                "hide-output"   => 1,
             }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to unpack the file");
            return 1;
        }

        General::Logging::log_user_message("Reading the values.yaml file");
        $rc = General::File_Operations::read_file(
            {
                "filename"            => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-dscload/values.yaml",
                "output-ref"          => \@result,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to read the values.yaml file");
            return 1;
        }
        for (@result) {
            if (/^configuration:\s*$/) {
                $found++;
            } elsif (/^(\s+af-diameter-realm:).*/) {
                $prefix = $1;
                s/^.+/$prefix "af-diamrealm"/;
                $found++;
            } elsif (/^(\s+pcf-diameter-host:).*/) {
                $prefix = $1;
                s/^.+/$prefix "pcf-diamhost.com"/;
                $found++;
            } elsif (/^(\s+pcf-diameter-realm:).*/) {
                $prefix = $1;
                s/^.+/$prefix "pcf-diamrealm.com"/;
                $found++;
            } elsif (/^(\s+diameter-service-ip:).*/) {
                $prefix = $1;
                s/^.+/$prefix "$ip"/;
                $found++;
            } elsif (/^(\s+diameter-service-port:).*/) {
                $prefix = $1;
                s/^.+/$prefix "3868"/;
                $found++;
            } elsif (/^(\s+diameter-service-name:).*/) {
                $prefix = $1;
                s/^.+/$prefix "eric-stm-diameter-traffic-tcp"/;
                $found++;
            } elsif (/^(\s+ip-version:).*/) {
                $prefix = $1;
                s/^.+/$prefix $ip_version/;
                $found++;
            }
        }
        if ($found == 0) {
            # No configuration section found, add missing data to the end
            push @result, (
                "configuration:",
                "  af-diameter-realm: \"af-diamrealm\"",
                "  pcf-diameter-host: \"pcf-diamhost.com\"",
                "  pcf-diameter-realm: \"pcf-diamrealm.com\"",
                "  diameter-service-ip: \"$ip\"",
                "  diameter-service-port: \"3868\"",
                "  diameter-service-name: \"eric-stm-diameter-traffic-tcp\"",
                "  ip-version: $ip_version"
            );
        } elsif ($found != 8) {
            General::Logging::log_user_error_message("Did not find the expected data in the values.yaml file");
            return 1;
        }

        General::Logging::log_user_message("Updating the values.yaml file");
        $rc = General::File_Operations::write_file(
            {
                "filename"            => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-dscload/values.yaml",
                "output-ref"          => \@result,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to write the values.yaml file");
            return 1;
        }

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
    sub Install_DSCLOAD_P105S03T07 {

        my $rc = 0;
        my $special_set_options = $::JOB_PARAMS{'TOOLS_ON_SEPARATE_WORKERS'} eq "yes" ? "--set usage=tools " : "";
        my $special_set_options_2 = $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} eq "IntIPv6ExtIPv6" ? "--set configuration.ip-version=6" : "";

        my $tool = "dscload";

        General::Logging::log_user_message("Installing $tool.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} install $special_set_options $special_set_options_2 -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} eric-sc-$tool $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-dscload/ --debug",
                "hide-output"   => 1,
             }
        );
        if ($rc == 0) {
            my @result;
            General::Logging::log_user_message("Checking every 5 seconds that $tool deployment in $::JOB_PARAMS{'TOOLS_NAMESPACE'} namespace comes up for a maximum of 60 seconds.");
            $rc = ADP::Kubernetes_Operations::check_deployment_status(
                {
                    "delay-time"                                => 5,
                    "deployment-name"                           => "eric-dscload-deployment",
                    "hide-output"                               => 1,
                    "kubeconfig"                                => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
                    "max-time"                                  => $::JOB_PARAMS{'POD_STATUS_TIMEOUT'},
                    "namespace"                                 => $::JOB_PARAMS{'TOOLS_NAMESPACE'},
                    "return-failed-pods"                        => \@result,
                }
            );
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) $tool installation was successful";
            } else {
                my $message = join("\n",@result);
                General::Logging::log_user_error_message("Deployment for $tool did not reach expected status:\n$message");
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to install $tool");
            return 1;
        }
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
    sub Build_K6_P105S03T08 {

        my $current_dir = `pwd`;
        $current_dir =~ s/[\r\n]//g;
        my $filename = "";
        my $rc = 0;
        my @result;
        my $tool = "k6";

        chdir "$::JOB_PARAMS{'GIT_DIRECTORY'}/devtools/$tool";

        General::Logging::log_user_message("Building $tool.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "make clean build",
                "hide-output"   => 1,
                "return-output" => \@result,
             }
        );

        chdir "$current_dir";

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to build $tool");
            return 1;
        }

        # Look for the built filename
        for (@result) {
            if (/^\s+"path"\s+:\s+"\/(eric-$tool-\d+\.\d+\.\d+-\S+\.tgz)",\s*$/) {
                #  "path" : "/eric-k6-1.11.25-h764e6e0e49.tgz",
                $filename = $1;
                last;
            }
        }

        if ($filename ne "") {
            if (-f "$::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename") {
                $::JOB_PARAMS{'FILE_NAME_K6'} = $filename;
                General::Logging::log_user_message("File for $tool is: $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename");
                push @::JOB_STATUS, "(/) $tool build was successful";
            } else {
                General::Logging::log_user_error_message("File for $tool not found: $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename");
                return 1;
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("No file found for $tool");
            return 1;
        }

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
    sub Install_K6_P105S03T09 {

        my $rc = 0;
        my $special_set_options = $::JOB_PARAMS{'TOOLS_ON_SEPARATE_WORKERS'} eq "yes" ? "--set usage=tools " : "";
        my $tool = "k6";

        General::Logging::log_user_message("Installing $tool.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} install $special_set_options -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} eric-sc-$tool $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$::JOB_PARAMS{'FILE_NAME_K6'} --set spec.replicas=$::JOB_PARAMS{'K6_REPLICAS'} --debug",
                "hide-output"   => 1,
             }
        );
        if ($rc == 0) {
            my @result;
            General::Logging::log_user_message("Checking every 5 seconds that $tool deployment in $::JOB_PARAMS{'TOOLS_NAMESPACE'} namespace comes up for a maximum of 60 seconds.");
            $rc = ADP::Kubernetes_Operations::check_deployment_status(
                {
                    "delay-time"                                => 5,
                    "deployment-name"                           => "eric-k6-deployment",
                    "hide-output"                               => 1,
                    "kubeconfig"                                => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
                    "max-time"                                  => $::JOB_PARAMS{'POD_STATUS_TIMEOUT'},
                    "namespace"                                 => $::JOB_PARAMS{'TOOLS_NAMESPACE'},
                    "return-failed-pods"                        => \@result,
                }
            );
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) $tool installation was successful";
            } else {
                my $message = join("\n",@result);
                General::Logging::log_user_error_message("Deployment for $tool did not reach expected status:\n$message");
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to install $tool");
            return 1;
        }

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
    sub Copy_Certificates_To_K6_Pods_P105S03T10 {

        my $copy_success;
        my $failures = 0;
        my $namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
        my @pods = ADP::Kubernetes_Operations::get_pod_names(
            {
                "kubeconfig"        => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
                "namespace"         => $namespace,
                "hide-output"       => 1,
                "pod-include-list"  => [ "eric-k6-deployment-.+" ],
            }
        );
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Checking every 5 seconds that all K6 pods in $namespace namespace are up for a maximum of 60 seconds.");
        $rc = ADP::Kubernetes_Operations::check_pod_status(
            {
                "all-namespaces"                            => 0,
                "debug-messages"                            => 0,
                "delay-time"                                => 5,
                "ignore-ready-check-for-completed-status"   => 1,
                "hide-output"                               => 1,
                "kubeconfig"                                => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
                "max-attempts"                              => 0,
                "max-time"                                  => 60,
                "namespace"                                 => $namespace,
                "pod-include-list"                          => \@pods,
                "repeated-checks"                           => 0,
                "return-failed-pods"                        => \@result,
                "wanted-ready"                              => "same",
                "wanted-status"                             => "up",
            }
        );
        if ($rc != 0) {
            my $message = join("\n",@result);
            General::Logging::log_user_error_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n$message");
        }

        for my $pod (@pods) {
            $copy_success = 1;
            General::Logging::log_user_message("Copying K6.crt file to pod $pod");
            # NOTE: The 'kubectl cp' seems to return $rc as 0 even if the 'from' file does not exist.
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $namespace cp $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/cert.pem $pod:/certs/K6.crt",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy K6.crt to pod $pod");
                $failures++;
                $copy_success = 0;
            }
            General::Logging::log_user_message("Copying K6.key file to pod $pod");
            # NOTE: The 'kubectl cp' seems to return $rc as 0 even if the 'from' file does not exist.
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $namespace cp $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/key.pem $pod:/certs/K6.key",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy K6.crt to pod $pod");
                $failures++;
                $copy_success = 0;
            }

            if ($::JOB_PARAMS{'SEPPSIM_WANTED'} eq "yes" && $::JOB_PARAMS{'K6_WANTED'} eq "yes") {
                General::Logging::log_user_message("Copying RP_206033.key file to pod $pod");
                # NOTE: The 'kubectl cp' seems to return $rc as 0 even if the 'from' file does not exist.
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $namespace cp $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/RP_206033.key $pod:/certs/RP_206033.key",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to copy RP_206033.key to pod $pod");
                    $failures++;
                    $copy_success = 0;
                }

                General::Logging::log_user_message("Copying RP_206033.crt file to pod $pod");
                # NOTE: The 'kubectl cp' seems to return $rc as 0 even if the 'from' file does not exist.
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $namespace cp $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/RP_206033.crt $pod:/certs/RP_206033.crt",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to copy RP_206033.crt to pod $pod");
                    $failures++;
                    $copy_success = 0;
                }
            }

            if ($copy_success == 1) {
                push @::JOB_STATUS, "(/) K6 certificate copy to pod $pod was successful";
            } else {
                push @::JOB_STATUS, "(x) K6 certificate copy to pod $pod failed";
            }
        }

        if ($failures == 0) {
            return 0;
        } else {
            return 1;
        }
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
    sub Build_NRFSIM_P105S03T11 {

        my $current_dir = `pwd`;
        $current_dir =~ s/[\r\n]//g;
        my $filename = "";
        my $rc = 0;
        my @result;
        my $tool = "nrfsim";

        chdir "$::JOB_PARAMS{'GIT_DIRECTORY'}/simulators/$tool";

        General::Logging::log_user_message("Building $tool.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "make clean build",
                "hide-output"   => 1,
                "return-output" => \@result,
             }
        );

        chdir "$current_dir";

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to build $tool");
            return 1;
        }

        # Look for the built filename
        for (@result) {
            if (/^\s+"path"\s+:\s+"\/(eric-$tool-\d+\.\d+\.\d+-\S+\.tgz)",\s*$/) {
                #  "path" : "/eric-k6-1.11.25-h764e6e0e49.tgz",
                $filename = $1;
                last;
            }
        }

        if ($filename ne "") {
            if (-f "$::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename") {
                $::JOB_PARAMS{'FILE_NAME_NRFSIM'} = $filename;
                General::Logging::log_user_message("File for $tool is: $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename");
                push @::JOB_STATUS, "(/) $tool build was successful";
            } else {
                General::Logging::log_user_error_message("File for $tool not found: $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename");
                return 1;
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("No file found for $tool");
            return 1;
        }

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
    sub Install_NRFSIM_P105S03T12 {

        my $rc = 0;
        my $special_set_options = $::JOB_PARAMS{'TOOLS_ON_SEPARATE_WORKERS'} eq "yes" ? "--set usage=tools " : "";
        my $special_set_options_2 = $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} eq "IntIPv6ExtIPv6" ? "--set service.internalIPFamily[0]=IPv6" : "";

        my $tool = "nrfsim";

        General::Logging::log_user_message("Installing $tool.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} install $special_set_options $special_set_options_2 -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} eric-sc-$tool $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$::JOB_PARAMS{'FILE_NAME_NRFSIM'} --debug",
                "hide-output"   => 1,
             }
        );
        if ($rc == 0) {
            my @result;
            General::Logging::log_user_message("Checking every 5 seconds that $tool deployment in $::JOB_PARAMS{'TOOLS_NAMESPACE'} namespace comes up for a maximum of 60 seconds.");
            $rc = ADP::Kubernetes_Operations::check_deployment_status(
                {
                    "delay-time"                                => 5,
                    "deployment-name"                           => "eric-nrfsim",
                    "hide-output"                               => 1,
                    "kubeconfig"                                => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
                    "max-time"                                  => $::JOB_PARAMS{'POD_STATUS_TIMEOUT'},
                    "namespace"                                 => $::JOB_PARAMS{'TOOLS_NAMESPACE'},
                    "return-failed-pods"                        => \@result,
                }
            );
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) $tool installation was successful";
            } else {
                my $message = join("\n",@result);
                General::Logging::log_user_error_message("Deployment for $tool did not reach expected status:\n$message");
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to install $tool");
            return 1;
        }
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
    sub Build_SEPPSIM_P105S03T13 {

        my $current_dir = `pwd`;
        $current_dir =~ s/[\r\n]//g;
        my $filename = "";
        my $rc = 0;
        my @result;
        my $tool = "seppsim";

        chdir "$::JOB_PARAMS{'GIT_DIRECTORY'}/simulators/$tool";

        General::Logging::log_user_message("Building $tool.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "make clean build-full",
                "hide-output"   => 1,
                "return-output" => \@result,
             }
        );

        chdir "$current_dir";

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to build $tool");
            return 1;
        }

        # Look for the built filename
        for (@result) {
            if (/^\s+"path"\s+:\s+"\/(eric-$tool-\d+\.\d+\.\d+-\S+\.tgz)",\s*$/) {
                #  "path" : "/eric-k6-1.11.25-h764e6e0e49.tgz",
                $filename = $1;
                last;
            }
        }

        if ($filename ne "") {
            if (-f "$::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename") {
                $::JOB_PARAMS{'FILE_NAME_SEPPSIM'} = $filename;
                General::Logging::log_user_message("File for $tool is: $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename");
                push @::JOB_STATUS, "(/) $tool build was successful";
            } else {
                General::Logging::log_user_error_message("File for $tool not found: $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename");
                return 1;
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("No file found for $tool");
            return 1;
        }

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
    sub Install_SEPPSIM_P105S03T14 {

        my $rc = 0;
        my $special_set_options = $::JOB_PARAMS{'TOOLS_ON_SEPARATE_WORKERS'} eq "yes" ? "--set usage=tools " : "";
        my $special_set_options_2 = $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} eq "IntIPv6ExtIPv6" ? " --set service.internalIPFamily[0]=IPv6" : "";
        my $tool = "seppsim";



        General::Logging::log_user_message("Installing $tool.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} install $special_set_options $special_set_options_2 -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} eric-sc-$tool $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$::JOB_PARAMS{'FILE_NAME_SEPPSIM'} --debug",
                "hide-output"   => 1,
             }
        );
        if ($rc == 0) {
            my @result;
            General::Logging::log_user_message("Checking every 5 seconds that $tool deployment in $::JOB_PARAMS{'TOOLS_NAMESPACE'} namespace comes up for a maximum of 60 seconds.");
            $rc = ADP::Kubernetes_Operations::check_deployment_status(
                {
                    "delay-time"                                => 5,
                    "deployment-name"                           => 'eric-seppsim-\S+',
                    "hide-output"                               => 1,
                    "kubeconfig"                                => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
                    "max-time"                                  => $::JOB_PARAMS{'POD_STATUS_TIMEOUT'},
                    "namespace"                                 => $::JOB_PARAMS{'TOOLS_NAMESPACE'},
                    "return-failed-pods"                        => \@result,
                }
            );
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) $tool installation was successful";
            } else {
                my $message = join("\n",@result);
                General::Logging::log_user_error_message("Deployment for $tool did not reach expected status:\n$message");
            }
            push @::JOB_STATUS, "(/) $tool installation was successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to install $tool");
            return 1;
        }
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
    sub Build_SFTP_P105S03T15 {

        my $current_dir = `pwd`;
        $current_dir =~ s/[\r\n]//g;
        my $filename = "";
        my $rc = 0;
        my @result;
        my $tool = "sftp";

        chdir "$::JOB_PARAMS{'GIT_DIRECTORY'}/devtools/$tool";

        General::Logging::log_user_message("Building $tool.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "make clean build",
                "hide-output"   => 1,
                "return-output" => \@result,
             }
        );

        chdir "$current_dir";

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to build $tool");
            return 1;
        }

        # Look for the built filename
        for (@result) {
            if (/^\s+"path"\s+:\s+"\/(eric-atmoz-$tool-\d+\.\d+\.\d+-\S+\.tgz)",\s*$/) {
                #  "path" : "/eric-atmoz-sftp-1.11.25-h597269b69e.tgz",
                $filename = $1;
                last;
            }
        }

        if ($filename ne "") {
            if (-f "$::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename") {
                $::JOB_PARAMS{'FILE_NAME_SFTP'} = $filename;
                General::Logging::log_user_message("File for $tool is: $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename");
                push @::JOB_STATUS, "(/) $tool build was successful";
            } else {
                General::Logging::log_user_error_message("File for $tool not found: $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$filename");
                return 1;
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("No file found for $tool");
            return 1;
        }

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
    sub Install_SFTP_P105S03T16 {

        my $rc = 0;
        my $special_set_options = $::JOB_PARAMS{'TOOLS_ON_SEPARATE_WORKERS'} eq "yes" ? "--set usage=tools " : "";
        my $tool = "sftp";

        General::Logging::log_user_message("Installing $tool.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} install $special_set_options -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} eric-sc-$tool $::JOB_PARAMS{'GIT_DIRECTORY'}/.bob/$::JOB_PARAMS{'FILE_NAME_SFTP'} --debug",
                "hide-output"   => 1,
             }
        );
        if ($rc == 0) {
            my @result;
            General::Logging::log_user_message("Checking every 5 seconds that $tool deployment in $::JOB_PARAMS{'TOOLS_NAMESPACE'} namespace comes up for a maximum of 120 seconds.");
            $rc = ADP::Kubernetes_Operations::check_deployment_status(
                {
                    "delay-time"                                => 5,
                    "deployment-name"                           => "eric-atmoz-sftp",
                    "hide-output"                               => 1,
                    "kubeconfig"                                => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
                    "max-time"                                  => $::JOB_PARAMS{'POD_STATUS_TIMEOUT'},
                    "namespace"                                 => $::JOB_PARAMS{'TOOLS_NAMESPACE'},
                    "return-failed-pods"                        => \@result,
                }
            );
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) $tool installation was successful";
            } else {
                my $message = join("\n",@result);
                General::Logging::log_user_error_message("Deployment for $tool did not reach expected status:\n$message");
            }
            push @::JOB_STATUS, "(/) $tool installation was successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to install $tool");
            return 1;
        }
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
    sub Check_Pod_Status_P105S03T17 {

        my $delay_time = 10;
        my $max_attempts = 0;
        my $max_time = $::JOB_PARAMS{'POD_STATUS_TIMEOUT'};
        my $repeated_checks = 0;
        my $message = "";
        my $rc = 0;
        my @result;
        my $namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};

        if ($max_time > 0) {
            General::Logging::log_user_message("Checking every 10 seconds that all pods in $namespace namespace are up for a maximum of $max_time seconds.");
        } else {
            General::Logging::log_user_message("Checking that all pods in $namespace namespace are up.");
        }
        $rc = ADP::Kubernetes_Operations::check_pod_status(
            {
                "all-namespaces"                            => 0,
                "debug-messages"                            => 0,
                "delay-time"                                => $delay_time,
                "ignore-ready-check-for-completed-status"   => 1,
                "hide-output"                               => 1,
                "kubeconfig"                                => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
                "max-attempts"                              => $max_attempts,
                "max-time"                                  => $max_time,
                "namespace"                                 => $namespace,
                "repeated-checks"                           => $repeated_checks,
                "return-failed-pods"                        => \@result,
                "wanted-ready"                              => "same",
                "wanted-status"                             => "up",
            }
        );
        if ($rc == 0) {
            General::Logging::log_user_message("POD status check was successful");
        } else {
            $message = join("\n",@result);
            General::Logging::log_user_error_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n$message");
        }

        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Post $::JOB_PARAMS{'JOBTYPE'} POD status check was successful";
        } else {
            push @::JOB_STATUS, "(x) Post $::JOB_PARAMS{'JOBTYPE'} POD status check failed";
            $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        }

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
sub Collect_ADP_Logs_P105S04 {

    my $rc = 0;

    if ($::JOB_PARAMS{'COLLECT_LOGS_AT_SUCCESS'} eq "yes" && $::JOB_PARAMS{'NAMESPACE_CREATED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Run_Collect_ADP_Logs_Script_P105S04T01 } );
        return $rc if $rc < 0;
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
    sub Run_Collect_ADP_Logs_Script_P105S04T01 {

        return locate_and_run_Collect_ADP_Logs_script();

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
sub Cleanup_Job_Environment_P105S05 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::902_Cleanup_Job_Environment::main } );
    return $rc if $rc < 0;

    if ($rc == 0) {
        # Mark that the Job was successful
        $::JOB_PARAMS{'JOBSTATUS'} = "SUCCESSFUL";
    }

    return $rc;
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P105S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    # Set Job Type again since it was changed inside the 102 playlist
    $::JOB_PARAMS{'JOBTYPE'} = "TRAFFIC_TOOLS_BUILD_AND_INSTALL";

    if ($::JOB_PARAMS{'NAMESPACE_CREATED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Run_Collect_ADP_Logs_Script_P105S99T01 } );
        return $rc if $rc < 0;
    }

    # Now we need to execute the cleanup.
    General::State_Machine::remove_always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::902_Cleanup_Job_Environment::main } );
    return $rc if $rc < 0;

    return 0;

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
    sub Run_Collect_ADP_Logs_Script_P105S99T01 {

        return locate_and_run_Collect_ADP_Logs_script();

    }
}

#####################
#                   #
# Other Subroutines #
#                   #
#####################

# -----------------------------------------------------------------------------
sub locate_and_run_Collect_ADP_Logs_script {

    my $namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
    my $rc = 0;
    my $script_file = "";

    #
    # Find the collect_ADP_logs.sh script file
    #
    for my $file ("$::JOB_PARAMS{'_PACKAGE_DIR'}/../scripts/collect_ADP_logs.sh", "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/tools/collect_ADP_logs.sh", "$::JOB_PARAMS{'_PACKAGE_DIR'}/bin/collect_ADP_logs.sh") {
        if (-f "$file") {
            # The file exist, use this file.
            $script_file = abs_path $file;
            last;
        }
    }
    if ($script_file eq "") {
        General::Logging::log_user_warning_message("Log collection script 'collect_ADP_logs.sh' not found.\nNo collection of ADP logs can be done.");
        push @::JOB_STATUS, "(-) Collection of ADP logs skipped";
        if ($::JOB_PARAMS{'JOBSTATUS'} eq "FAILED") {
            # Inside fallback, so we just return back success.
            return 0;
        } else {
            return General::Playlist_Operations::RC_STEPOUT;
        }
    }

    # Generate a unique temporary directory name on the cluster to storing the script and
    # for storing the collected log file.
    my $node_directory = sprintf "/home/eccd/collect_adp_logs_%s_%i_%i", $::JOB_PARAMS{'DAFT_HOSTNAME'}, $$, time();

    #
    # Create the directory on the node.
    #
    General::Logging::log_user_message("Directory to be created on node: $node_directory");
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o UserKnownHostsFile=/dev/null -o loglevel=ERROR $::JOB_PARAMS{'NODE_USER'}\@$::JOB_PARAMS{'NODE_VIP_ADDRESS'} mkdir -p $node_directory",
            "hide-output"   => 1,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_error_message("Failed to create directoy $node_directory on node.\nNo collection of ADP logs can be done.");
        push @::JOB_STATUS, "(x) Collection of ADP logs failed";
        if ($::JOB_PARAMS{'JOBSTATUS'} eq "FAILED") {
            # Inside fallback, so we just return back success.
            return 0;
        } else {
            return General::Playlist_Operations::RC_STEPOUT;
        }
    }

    #
    # Upload the script to the node
    #
    General::Logging::log_user_message("Copy 'collect_ADP_logs.sh script to node.");
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -p $script_file $::JOB_PARAMS{'NODE_USER'}\@$::JOB_PARAMS{'NODE_VIP_ADDRESS'}:$node_directory",
            "hide-output"   => 1,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_error_message("Failed to copy $script_file to $node_directory on node.\nNo collection of ADP logs can be done.");
        push @::JOB_STATUS, "(x) Collection of ADP logs failed";
        if ($::JOB_PARAMS{'JOBSTATUS'} eq "FAILED") {
            # Inside fallback, so we just return back success.
            return 0;
        } else {
            return General::Playlist_Operations::RC_STEPOUT;
        }
    }

    #
    # Execute the script on the node and collect the logs.
    #
    General::Logging::log_user_message("Execute 'collect_ADP_logs.sh script on node.\nThis will take a while to complete.\n");
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                    "--ip=$::JOB_PARAMS{'NODE_VIP_ADDRESS'} " .
                                    "--user=$::JOB_PARAMS{'NODE_USER'} " .
                                    "--password='notmeeded' " .
                                    "--port=22 " .
                                    "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                    "--command='cd $node_directory' " .
                                    "--command='$node_directory/collect_ADP_logs.sh -n $namespace -s' ",
            "hide-output"   => 1,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_error_message("Failed to execute 'collect_ADP_logs.sh' script on node.\nNo collection of ADP logs can be done.");
        push @::JOB_STATUS, "(x) Collection of ADP logs failed";
        if ($::JOB_PARAMS{'JOBSTATUS'} eq "FAILED") {
            # Inside fallback, so we just return back success.
            return 0;
        } else {
            return General::Playlist_Operations::RC_STEPOUT;
        }
    }

    #
    # Fetch the log file from the node and store it in the trouble shooting log directory under the workspace.
    #
    General::Logging::log_user_message("Copy log file from node.");
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o UserKnownHostsFile=/dev/null -o loglevel=ERROR -p $::JOB_PARAMS{'NODE_USER'}\@$::JOB_PARAMS{'NODE_VIP_ADDRESS'}:$node_directory/*.tgz $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/troubleshooting_logs/",
            "hide-output"   => 1,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_error_message("Failed to copy log file from node.\nNo collection of ADP logs can be done.");
        push @::JOB_STATUS, "(x) Collection of ADP logs failed";
        if ($::JOB_PARAMS{'JOBSTATUS'} eq "FAILED") {
            # Inside fallback, so we just return back success.
            return 0;
        } else {
            return General::Playlist_Operations::RC_STEPOUT;
        }
    }

    #
    # Delete the temporary directory on the node.
    #
    General::Logging::log_user_message("Delete directory $node_directory on node");
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o UserKnownHostsFile=/dev/null -o loglevel=ERROR $::JOB_PARAMS{'NODE_USER'}\@$::JOB_PARAMS{'NODE_VIP_ADDRESS'} rm -fr $node_directory",
            "hide-output"   => 1,
        }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_warning_message("Failed to delete directoy $node_directory on node, error ignored.\nYou need to manually delete the directory.\n");
        # Ignore this error
        $rc = 0;
    }

    return $rc;
}

# -----------------------------------------------------------------------------
sub set_playlist_variables {
    %playlist_variables = (
        # ---------------------------------------------------------------------
        'COLLECT_LOGS_AT_SUCCESS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if detailed log files should be collected at successful
job termination.
Normally detailed logs are only collected when the job fails to aid with trouble
shooting efforts.

If value is 'yes' when the playlist finish with an success then detailed ADP
logs will be collected and stored in the all.log or under the troubleshooting_logs
sub directory of the job.
The default value is 'no' if the variable is not specified.

If value is 'no' then no collection of detailed logs will be done at successful
job termination.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'GIT_DIRECTORY' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "directory", # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the top-level directory of the '5g_proto' GIT directory
that must contain the following sub-directories:

    - daft/
    - devtools/k6/
    - simulators/dscload/
    - simulators/seppsim/
    - esc/

If this parameter is not specified then the current directory where the playlist
is executed from must be inside the GIT directory tree.
If neither is the case then the playlist will fail.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'K6_REPLICAS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "3",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies how many replicas of the K6 pods to deploy.
If you want to run multiple traffic types e.g. bsf,scp and sepp at the same time
it is better to use one K6 instance per traffic type otherwise it might not be
possible to get successful traffic if multiple traffic types are sharing a K6
instance.

The default is to deploy 3 K6 instances, one per supported traffic type.
EOF
            'validity_mask' => '\d+',
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
kubectl commands.
If you don't specify this parameter then it will take the default file which
is usually called ~/.kube/config.
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
kubectl commands towards the tools deployment.
If you don't specify this parameter then it will take the default file which
is usually called ~/.kube/config.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'NODE_CERTIFICATE_DIRECTORY' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "/home/eccd/certificates",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls the directory on the node where the certificates are
stored.
This directory is used for copying the certificates from the node to the
workspace directory for creating tools certificates and then the newly
created certificates are then copied back to the node.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'NODE_USER' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "eccd",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls the user name to use when connecting to the cluster via
the IP-address specified with NODE_VIP_ADDRESS.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'NODE_VIP_ADDRESS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls the IP-address to reach the control or director node
where most of the logic is executed.
If not specified then the IP-address to the node will be taken from the network
configuration file parameter 'node_vip_address',

NOTE: That the .ssh/authorized_keys file on the director must include the public
ssh key of the current user because it needs to be able to login to the director
without giving a password.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'POD_STATUS_TIMEOUT' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "300",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the timeout value to use when checking that all PODs are up and running
after deploying the software, if not specified then the default value will be
used.

The time value which must be an integer value is given as the number of seconds
to wait for the pods to have expected status.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TOOLS_NAMESPACE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls in which namespace the tools will be created on the node
and should be specified if you want to override the parameter from the network
configuration file called 'tools_namespace'.
If you don't specify this parameter then it will take the value from the network
configuration file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TOOLS_ON_SEPARATE_WORKERS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the tools should be deployed on separate workers
to e.g. avoid disturbances from affecting the tool pods.
If the value is 'yes' then when installing the pod's with the helm install
command then an extra parameter '--set usage=tools' will be appended to the
command which then set special nodeSelector and tolerations for the pod's
which will force the deployment of the pod's to special worker nodes.
If the value is 'no' then the pod's will be placed on whatever worker node
has enough space for the pod.

NOTE: To properly use the value 'yes' you MUST also have called the playlist
103_Tools_Management.pm that will set the proper labels and taints on the worker
nodes.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TOOLS_TO_INSTALL' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which tools to install on the node and it will be used
for building a new tools package and then for installing the tools on the node
in the namespace specified by TOOLS_NAMESPACE.

Multiple tools can be specified by separating each with a space or comma.

Currently allowed tools are:
    - dscload
    - k6
    - nrfsim
    - seppsim
    - sftp
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },
    )
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist builds and installs the dscload, k6, nrfsim and seppsim traffic
generators tools and atmoz-sftp server on the IaaS clusters in preparation for
traffic setup.

Used Job Parameters:
====================
EOF
    # Special handling for this playlist so we print out all variable information from
    # the main playlist and the all other called main playlists.
    use File::Basename qw(dirname basename);
    my $length;
    my $name;
    my $message;
    my $path_to_playlist = dirname(__FILE__);
    my $playlist_name = basename(__FILE__);
    $playlist_name =~ s/\.pm//;
    my %temp_hash = ( %Playlist::102_Supreme_Certificate_Management::playlist_variables, %playlist_variables );

    General::Playlist_Operations::print_info_about_job_variables(\%temp_hash);

    $message = "# Global variable access in playlist $playlist_name #";
    $length = length($message);
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables(__FILE__, \%playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);

    no strict;
    for $playlist_name ("102_Supreme_Certificate_Management") {
        $message = "# Global variable access in playlist $playlist_name #";
        $length = length($message);
        printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
        $name = "Playlist::${playlist_name}::playlist_variables";
        General::Playlist_Operations::print_used_job_variables("$path_to_playlist/$playlist_name.pm", \%$$name);
        General::Playlist_Operations::print_used_network_config_variables("$path_to_playlist/$playlist_name.pm");
    }
    use strict;
}

# -----------------------------------------------------------------------------
sub usage_return_playlist_variables {
    return ( %Playlist::102_Supreme_Certificate_Management::playlist_variables, %playlist_variables );
}

1;
