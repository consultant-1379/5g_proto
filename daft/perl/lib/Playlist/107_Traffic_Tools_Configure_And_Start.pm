package Playlist::107_Traffic_Tools_Configure_And_Start;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.62
#  Date     : 2024-06-07 11:18:44
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
use Cwd qw(abs_path);
use File::Basename qw(dirname basename);

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    parse_network_config_parameters
    usage
    usage_return_playlist_variables
    );

use ADP::Kubernetes_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::004_Config_Management;
use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;
use Playlist::920_Check_Network_Config_Information;
use Playlist::933_Handle_KPI_Statistics;
use Playlist::936_Check_Deployed_Software;
use Playlist::938_Node_Information;

#
# Variable Declarations
#
our %playlist_variables;
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
    $::JOB_PARAMS{'JOBTYPE'} = "TRAFFIC_TOOLS_CONFIGURE_AND_START";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # If the playlist needs to have access to Network Configuration parameters
    # then it needs to check for this and give an error or ask the user to
    # provide the file name.
    if (Playlist::920_Check_Network_Config_Information::is_network_config_specified() != 0) {
        General::Logging::log_user_error_message("You must specify a network configuration file for this Playlist to run");
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

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P107S01, \&Fallback001_P107S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P107S02, \&Fallback001_P107S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Setup_And_Configure_Tools_P107S03, \&Fallback001_P107S99 );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'TRAFFIC_RUNNING_IN_BACKGROUND'} = "no";

    $rc = General::Playlist_Operations::execute_step( \&Start_Traffic_P107S04, \&Fallback001_P107S99 );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'TRAFFIC_RUNNING_IN_BACKGROUND'} eq "yes") {
        $rc = General::Playlist_Operations::execute_step( \&Wait_And_Then_Stop_Traffic_P107S05, \&Fallback001_P107S99 );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_step( \&Collect_Logs_P107S06, \&Fallback001_P107S99 );
    return $rc if $rc < 0;

    # Now we need to execute the cleanup.
    General::State_Machine::remove_always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P107S07, \&Fallback001_P107S99 );
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
sub Initialize_Job_Environment_P107S01 {

    my $rc;

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
sub Check_Job_Parameters_P107S02 {

    my $rc = 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Tools_Namespace_On_Node_P107S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_SC_Namespace_On_Node_P107S02T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::936_Check_Deployed_Software::main } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'P920_TASK'} = "READ_PARAMETERS_FROM_FILE";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::920_Check_Network_Config_Information::main } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'} ne "" || $::JOB_PARAMS{'TRAFFIC_TO_START'} ne "") {
        $::JOB_PARAMS{'P938_GET_SC_NODE_INFORMATION'} = "yes";
        $::JOB_PARAMS{'P938_GET_TOOLS_NODE_INFORMATION'} = "yes";
        $::JOB_PARAMS{'P938_REPLACE_FILE_PLACEHOLDERS'} = "no";
        $::JOB_PARAMS{'P938_INPUT_FILE_NAME'} = "";
        $::JOB_PARAMS{'P938_OUTPUT_FILE_NAME'} = "";
        $::JOB_PARAMS{'P938_OUTPUT_FILE_ACCESSMODE'} = "666";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::938_Node_Information::main } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_TRAFFIC_TO_CONFIGURE_Parameter_P107S02T03 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_TRAFFIC_TO_START_Parameter_P107S02T04 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_TRAFFIC_CONFIG_FILE_Parameter_P107S02T05 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Needed_Simulators_P107S02T06 } );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_error_message("Job parameter 'TRAFFIC_TO_CONFIGURE' and/or 'TRAFFIC_TO_START' not specified");
        return General::Playlist_Operations::RC_FALLBACK;
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
    sub Check_Tools_Namespace_On_Node_P107S02T01 {
        my $found = 0;
        my $message;
        my $rc = 0;
        my @result;
        my $tools_namespace = "";
        my @unknown_simulators = ();

        # Set default values for all traffic simulators
        $::JOB_PARAMS{'SIMULATOR_CHFSIM_INSTALLED'} = "no";
        $::JOB_PARAMS{'SIMULATOR_DSCLOAD_INSTALLED'} = "no";
        $::JOB_PARAMS{'SIMULATOR_K6_INSTALLED'} = "no";
        $::JOB_PARAMS{'SIMULATOR_NRFSIM_INSTALLED'} = "no";
        $::JOB_PARAMS{'SIMULATOR_SC_MONITOR_INSTALLED'} = "no";
        $::JOB_PARAMS{'SIMULATOR_SEPPSIM_INSTALLED'} = "no";
        $::JOB_PARAMS{'SIMULATOR_UNKNOWN_INSTALLED'} = "no";

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
        if ($found == 1) {
            General::Logging::log_user_message("Checking deployed helm charts in namespace $::JOB_PARAMS{'TOOLS_NAMESPACE'}");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} list -n $::JOB_PARAMS{'TOOLS_NAMESPACE'}",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to check deployed helm charts");
                return 1;
            }

            #NAME               NAMESPACE    REVISION    UPDATED                                    STATUS      CHART                               APP VERSION
            #eric-chfsim        tools        1           2024-01-16 14:26:18.641368202 +0000 UTC    deployed    eric-chfsim-1.14.25-hce874a008e.dirty              1.14.25-hce874a008e.dirty
            #eric-sc-dscload    tools        1           2023-02-06 14:04:25.602831927 +0000 UTC    deployed    eric-dscload-1.11.25-h0d437cf5f5    1.11.25-h0d437cf5f5
            #eric-sc-k6         tools        1           2023-02-06 14:04:50.839935628 +0000 UTC    deployed    eric-k6-1.11.25-h0d437cf5f5         1.11.25-h0d437cf5f5
            #eric-sc-nrfsim     tools        1           2023-02-06 14:05:21.695514038 +0000 UTC    deployed    eric-nrfsim-1.11.25-h0d437cf5f5
            #eric-sc-seppsim    tools        1           2023-02-06 14:06:12.006906013 +0000 UTC    deployed    eric-seppsim-1.11.25-h0d437cf5f5
            for (@result) {
                if (/^(eric-sc-chfsim|eric-chfsim)\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'SIMULATOR_CHFSIM_INSTALLED'} = "yes";
                } elsif (/^eric-sc-dscload\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'SIMULATOR_DSCLOAD_INSTALLED'} = "yes";
                } elsif (/^eric-sc-k6\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'SIMULATOR_K6_INSTALLED'} = "yes";
                } elsif (/^eric-sc-monitor\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'SIMULATOR_SC_MONITOR_INSTALLED'} = "yes";
                } elsif (/^eric-sc-nrfsim\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'SIMULATOR_NRFSIM_INSTALLED'} = "yes";
                } elsif (/^eric-sc-seppsim\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'SIMULATOR_SEPPSIM_INSTALLED'} = "yes";
                } elsif (/^(\S+)\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'SIMULATOR_UNKNOWN_INSTALLED'} = "yes";
                    push @unknown_simulators, $1;
                }
            }

            $message  = "TOOLS_NAMESPACE:                $::JOB_PARAMS{'TOOLS_NAMESPACE'}\n";
            $message .= "SIMULATOR_CHFSIM_INSTALLED:     $::JOB_PARAMS{'SIMULATOR_CHFSIM_INSTALLED'}\n";
            $message .= "SIMULATOR_DSCLOAD_INSTALLED:    $::JOB_PARAMS{'SIMULATOR_DSCLOAD_INSTALLED'}\n";
            $message .= "SIMULATOR_K6_INSTALLED:         $::JOB_PARAMS{'SIMULATOR_K6_INSTALLED'}\n";
            $message .= "SIMULATOR_NRFSIM_INSTALLED:     $::JOB_PARAMS{'SIMULATOR_NRFSIM_INSTALLED'}\n";
            $message .= "SIMULATOR_SC_MONITOR_INSTALLED: $::JOB_PARAMS{'SIMULATOR_SC_MONITOR_INSTALLED'}\n";
            $message .= "SIMULATOR_SEPPSIM_INSTALLED:    $::JOB_PARAMS{'SIMULATOR_SEPPSIM_INSTALLED'}\n";
            $message .= "SIMULATOR_UNKNOWN_INSTALLED:    $::JOB_PARAMS{'SIMULATOR_UNKNOWN_INSTALLED'}\n";
            if (@unknown_simulators) {
                $message .= "Unknown Simulators:          " . (join ", ", @unknown_simulators) . "\n";
            }

            General::Logging::log_user_message($message);
        } else {
            General::Logging::log_user_error_message("Namespace $::JOB_PARAMS{'TOOLS_NAMESPACE'} does not exist");
            $rc = 1;
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
    sub Check_SC_Namespace_On_Node_P107S02T02 {
        my $found = 0;
        my $message = "";
        my $rc = 0;
        my @result;
        my $sc_namespace = "";
        my $sc_release_name = "";

        # Get the proper SC_NAMESPACE
        General::Logging::log_user_message("Checking Job parameter 'SC_NAMESPACE' and Network Config parameter 'sc_namespace'");
        if ($::JOB_PARAMS{'SC_NAMESPACE'} ne "") {
            $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'SC_NAMESPACE'} = $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'};
                $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'sc_namespace' has not been set and Job parameter 'SC_NAMESPACE' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'SC_NAMESPACE', nor Network Config parameter 'sc_namespace' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper SC_RELEASE_NAME
        General::Logging::log_user_message("Checking Job parameter 'SC_RELEASE_NAME' and Network Config parameter 'sc_release_name'");
        if ($::JOB_PARAMS{'SC_RELEASE_NAME'} ne "") {
            $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'sc_release_name'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'sc_release_name'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'SC_RELEASE_NAME'} = $::NETWORK_CONFIG_PARAMS{'sc_release_name'}{'value'};
                $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'sc_release_name' has not been set and Job parameter 'SC_RELEASE_NAME' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'SC_RELEASE_NAME', nor Network Config parameter 'sc_release_name' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Set default values for installed SC worker pods
        $::JOB_PARAMS{'BSF_WORKER_INSTALLED'} = "no";
        $::JOB_PARAMS{'SCP_WORKER_INSTALLED'} = "no";
        $::JOB_PARAMS{'SEPP_WORKER_INSTALLED'} = "no";
        $::JOB_PARAMS{'BSF_WORKER_READY'} = "no";
        $::JOB_PARAMS{'SCP_WORKER_READY'} = "no";
        $::JOB_PARAMS{'SEPP_WORKER_READY'} = "no";

        General::Logging::log_user_message("Checking if namespace $::JOB_PARAMS{'SC_NAMESPACE'} exist on the node");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get namespace",
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
            if (/^$::JOB_PARAMS{'SC_NAMESPACE'}\s+Active\s+\S+\s*$/) {
                $found = 1;
                last;
            }
        }
        if ($found == 0) {
            General::Logging::log_user_error_message("Namespace $::JOB_PARAMS{'SC_NAMESPACE'} does not exist, there is no point to setup traffic since no CMYP is available");
            return 1;
        }

        General::Logging::log_user_message("Checking deployed pods in namespace $::JOB_PARAMS{'SC_NAMESPACE'}");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get pod -n $::JOB_PARAMS{'SC_NAMESPACE'}",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check deployed pods");
            return 1;
        }

        #NAME                                                              READY   STATUS      RESTARTS      AGE
        #eric-bsf-worker-594f45b888-77shf                                  5/5     Running     0             21d
        #eric-scp-worker-69bcd96947-862f5                                  7/7     Running     0             21d
        #eric-sepp-worker-546f6bb469-r8rkw                                 7/7     Running     0             21d
        # etc.
        for (@result) {
            if (/^eric-bsf-worker-\S+\s+(\d+)\/(\d+)\s+(\S+)\s+.+/) {
                $::JOB_PARAMS{'BSF_WORKER_INSTALLED'} = "yes";
                if ($1 == $2) {
                    $::JOB_PARAMS{'BSF_WORKER_READY'} = "yes";
                }
                if ($3 ne "Running") {
                    # Maybe the worker is in status CrashLoopBackOff due to some issue
                    $::JOB_PARAMS{'BSF_WORKER_READY'} = "no";
                }
            } elsif (/^eric-scp-worker-\S+\s+(\d+)\/(\d+)\s+(\S+)\s+.+/) {
                $::JOB_PARAMS{'SCP_WORKER_INSTALLED'} = "yes";
                if ($1 == $2) {
                    $::JOB_PARAMS{'SCP_WORKER_READY'} = "yes";
                }
                if ($3 ne "Running") {
                    # Maybe the worker is in status CrashLoopBackOff due to some issue
                    $::JOB_PARAMS{'SCP_WORKER_READY'} = "no";
                }
            } elsif (/^eric-sepp-worker-\S+\s+(\d+)\/(\d+)\s+(\S+)\s+.+/) {
                $::JOB_PARAMS{'SEPP_WORKER_INSTALLED'} = "yes";
                if ($1 == $2) {
                    $::JOB_PARAMS{'SEPP_WORKER_READY'} = "yes";
                }
                if ($3 ne "Running") {
                    # Maybe the worker is in status CrashLoopBackOff due to some issue
                    $::JOB_PARAMS{'SEPP_WORKER_READY'} = "no";
                }
            }
        }

        $message  = "SC_NAMESPACE:          $::JOB_PARAMS{'SC_NAMESPACE'}\n";
        $message .= "SC_RELEASE_NAME:       $::JOB_PARAMS{'SC_RELEASE_NAME'}\n";
        $message .= "BSF_WORKER_INSTALLED:  $::JOB_PARAMS{'BSF_WORKER_INSTALLED'}\n";
        $message .= "BSF_WORKER_READY:      $::JOB_PARAMS{'BSF_WORKER_READY'}\n";
        $message .= "SCP_WORKER_INSTALLED:  $::JOB_PARAMS{'SCP_WORKER_INSTALLED'}\n";
        $message .= "SCP_WORKER_READY:      $::JOB_PARAMS{'SCP_WORKER_READY'}\n";
        $message .= "SEPP_WORKER_INSTALLED: $::JOB_PARAMS{'SEPP_WORKER_INSTALLED'}\n";
        $message .= "SEPP_WORKER_READY:     $::JOB_PARAMS{'SEPP_WORKER_READY'}\n";

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
    sub Check_TRAFFIC_TO_CONFIGURE_Parameter_P107S02T03 {
        my $error_message = "";
        my $message = "";
        my $rc = 0;

        General::Logging::log_user_message("Checking Job parameter 'TRAFFIC_TO_CONFIGURE'");

        # Set default values for all traffic simulators
        $::JOB_PARAMS{'CONFIGURE_BSF_NEEDED'} = "no";
        $::JOB_PARAMS{'CONFIGURE_SCP_NEEDED'} = "no";
        $::JOB_PARAMS{'CONFIGURE_SEPP_NEEDED'} = "no";
        $::JOB_PARAMS{'POD_BSF_NEEDED'} = "no" unless (exists $::JOB_PARAMS{'POD_BSF_NEEDED'});
        $::JOB_PARAMS{'POD_SCP_NEEDED'} = "no" unless (exists $::JOB_PARAMS{'POD_SCP_NEEDED'});
        $::JOB_PARAMS{'POD_SEPP_NEEDED'} = "no" unless (exists $::JOB_PARAMS{'POD_SEPP_NEEDED'});

        # Check if we need to configure any traffic tools
        if ($::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'} ne "") {
            # Check if we should automatically determine which traffic to configure based on the
            # installed software.
            if ($::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'} eq "automatic") {
                $::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'} = "";
                if ($::JOB_PARAMS{'BSF_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'BSF_WORKER_READY'} eq "no") {
                    $::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'} .= "bsf,";
                } elsif ($::JOB_PARAMS{'BSF_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'BSF_WORKER_READY'} eq "yes" && $::JOB_PARAMS{'FORCE_LOAD_CONFIGURATION'} eq "yes") {
                    # Even though the pod's are already up we force loading of configuration data
                    $::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'} .= "bsf,";
                }
                if ($::JOB_PARAMS{'SCP_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'SCP_WORKER_READY'} eq "no") {
                    $::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'} .= "scp,";
                } elsif ($::JOB_PARAMS{'SCP_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'SCP_WORKER_READY'} eq "yes" && $::JOB_PARAMS{'FORCE_LOAD_CONFIGURATION'} eq "yes") {
                    # Even though the pod's are already up we force loading of configuration data
                    $::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'} .= "scp,";
                }
                if ($::JOB_PARAMS{'SEPP_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'SEPP_WORKER_READY'} eq "no") {
                    $::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'} .= "sepp,";
                } elsif ($::JOB_PARAMS{'SEPP_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'SEPP_WORKER_READY'} eq "yes" && $::JOB_PARAMS{'FORCE_LOAD_CONFIGURATION'} eq "yes") {
                    # Even though the pod's are already up we force loading of configuration data
                    $::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'} .= "sepp,";
                }
            }

            $::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'} =~ s/\s+/,/g;
            $::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'} =~ s/,+/,/g;
            $::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'} =~ s/,$//g;

            for my $traffic_type (split /,/, $::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'}) {
                #
                # BSF Traffic
                #
                if ($traffic_type eq "bsf") {
                    $::JOB_PARAMS{'CONFIGURE_BSF_NEEDED'} = "yes";
                    $::JOB_PARAMS{'POD_BSF_NEEDED'} = "yes";

                #
                # SCP Traffic
                #
                } elsif ($traffic_type eq "scp") {
                    $::JOB_PARAMS{'CONFIGURE_SCP_NEEDED'} = "yes";
                    $::JOB_PARAMS{'POD_SCP_NEEDED'} = "yes";

                #
                # SEPP Traffic
                #
                } elsif ($traffic_type eq "sepp") {
                    $::JOB_PARAMS{'CONFIGURE_SEPP_NEEDED'} = "yes";
                    $::JOB_PARAMS{'POD_SEPP_NEEDED'} = "yes";

                } elsif ($traffic_type eq "automatic") {
                    General::Logging::log_user_error_message("Traffic type '$traffic_type' cannot be specified together with other traffic types in job parameter 'TRAFFIC_TO_CONFIGURE'");
                    return General::Playlist_Operations::RC_FALLBACK;
                } else {
                    General::Logging::log_user_warning_message("Unknown traffic type '$traffic_type' specified in job parameter 'TRAFFIC_TO_CONFIGURE', it's ignored");
                }
            }

            # Check that we have all needed pod's installed
            $message = "";
            if ($::JOB_PARAMS{'POD_BSF_NEEDED'} eq "yes" && $::JOB_PARAMS{'BSF_WORKER_INSTALLED'} eq "no") {
                $message .= "  - Worker Pods for traffic type 'bsf' is needed but not installed\n";
            }
            if ($::JOB_PARAMS{'POD_SCP_NEEDED'} eq "yes" && $::JOB_PARAMS{'SCP_WORKER_INSTALLED'} eq "no") {
                $message .= "  - Worker Pods for traffic type 'scp' is needed but not installed\n";
            }
            if ($::JOB_PARAMS{'POD_SEPP_NEEDED'} eq "yes" && $::JOB_PARAMS{'SEPP_WORKER_INSTALLED'} eq "no") {
                $message .= "  - Worker Pods for traffic type 'sepp' is needed but not installed\n";
            }
            if ($message ne "") {
                $error_message .= "\nOne or more pods are missing for configuration:\n$message";
            }
        }

        $message = "";
        $message .= "CONFIGURE_BSF_NEEDED:        $::JOB_PARAMS{'CONFIGURE_BSF_NEEDED'}\n";
        $message .= "CONFIGURE_SCP_NEEDED:        $::JOB_PARAMS{'CONFIGURE_SCP_NEEDED'}\n";
        $message .= "CONFIGURE_SEPP_NEEDED:       $::JOB_PARAMS{'CONFIGURE_SEPP_NEEDED'}\n";
        $message .= "POD_BSF_NEEDED:              $::JOB_PARAMS{'POD_BSF_NEEDED'}\n";
        $message .= "POD_SCP_NEEDED:              $::JOB_PARAMS{'POD_SCP_NEEDED'}\n";
        $message .= "POD_SEPP_NEEDED:             $::JOB_PARAMS{'POD_SEPP_NEEDED'}\n";
        $message .= "TRAFFIC_TO_CONFIGURE:        $::JOB_PARAMS{'TRAFFIC_TO_CONFIGURE'}\n";

        if ($error_message eq "") {
            General::Logging::log_user_message($message);
            $rc = 0;
        } else {
            General::Logging::log_user_error_message($message . $error_message);
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
    sub Check_TRAFFIC_TO_START_Parameter_P107S02T04 {
        my $error_message = "";
        my $message = "";
        my $rc = 0;

        General::Logging::log_user_message("Checking Job parameter 'TRAFFIC_TO_START'");

        # Set default values for all traffic simulators
        $::JOB_PARAMS{'POD_BSF_NEEDED'} = "no" unless (exists $::JOB_PARAMS{'POD_BSF_NEEDED'});
        $::JOB_PARAMS{'POD_SCP_NEEDED'} = "no" unless (exists $::JOB_PARAMS{'POD_SCP_NEEDED'});
        $::JOB_PARAMS{'POD_SEPP_NEEDED'} = "no" unless (exists $::JOB_PARAMS{'POD_SEPP_NEEDED'});
        $::JOB_PARAMS{'START_BSF_TRAFFIC'} = "no";
        $::JOB_PARAMS{'START_SCP_TRAFFIC'} = "no";
        $::JOB_PARAMS{'START_SEPP_TRAFFIC'} = "no";

        # Check if we need to start any traffic tools
        if ($::JOB_PARAMS{'TRAFFIC_TO_START'} ne "") {

            if ($::JOB_PARAMS{'TRAFFIC_DURATION'} !~ /^(\d+|indefinite)$/) {
                General::Logging::log_user_error_message("Invalid TRAFFIC_DURATION value specified ($::JOB_PARAMS{'TRAFFIC_DURATION'}), only an integer or value 'indefinite' allowed");
                return General::Playlist_Operations::RC_FALLBACK;
            }

            # Check if we should automatically determine which traffic to start based on the
            # installed software.
            if ($::JOB_PARAMS{'TRAFFIC_TO_START'} eq "automatic") {
                $::JOB_PARAMS{'TRAFFIC_TO_START'} = "";
                if ( ($::JOB_PARAMS{'BSF_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'BSF_WORKER_READY'} eq "yes") ||
                     ($::JOB_PARAMS{'BSF_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'BSF_WORKER_READY'} eq "no" && $::JOB_PARAMS{'CONFIGURE_BSF_NEEDED'} eq "yes") ) {

                    $::JOB_PARAMS{'TRAFFIC_TO_START'} .= "bsf,";
                }
                if ( ($::JOB_PARAMS{'SCP_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'SCP_WORKER_READY'} eq "yes") ||
                     ($::JOB_PARAMS{'SCP_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'SCP_WORKER_READY'} eq "no" && $::JOB_PARAMS{'CONFIGURE_SCP_NEEDED'} eq "yes") ) {

                    $::JOB_PARAMS{'TRAFFIC_TO_START'} .= "scp,";
                }
                if ( ($::JOB_PARAMS{'SEPP_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'SEPP_WORKER_READY'} eq "yes") ||
                     ($::JOB_PARAMS{'SEPP_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'SEPP_WORKER_READY'} eq "no" && $::JOB_PARAMS{'CONFIGURE_SEPP_NEEDED'} eq "yes") ) {

                    $::JOB_PARAMS{'TRAFFIC_TO_START'} .= "sepp,";
                }
            }

            $::JOB_PARAMS{'TRAFFIC_TO_START'} =~ s/\s+/,/g;
            $::JOB_PARAMS{'TRAFFIC_TO_START'} =~ s/,+/,/g;
            $::JOB_PARAMS{'TRAFFIC_TO_START'} =~ s/,$//g;

            for my $traffic_type (split /,/, $::JOB_PARAMS{'TRAFFIC_TO_START'}) {
                #
                # BSF Traffic
                #
                if ($traffic_type eq "bsf") {
                    $::JOB_PARAMS{'POD_BSF_NEEDED'} = "yes";
                    $::JOB_PARAMS{'START_BSF_TRAFFIC'} = "yes";

                #
                # SCP Traffic
                #
                } elsif ($traffic_type eq "scp") {
                    $::JOB_PARAMS{'POD_SCP_NEEDED'} = "yes";
                    $::JOB_PARAMS{'START_SCP_TRAFFIC'} = "yes";

                #
                # SEPP Traffic
                #
                } elsif ($traffic_type eq "sepp") {
                    $::JOB_PARAMS{'POD_SEPP_NEEDED'} = "yes";
                    $::JOB_PARAMS{'START_SEPP_TRAFFIC'} = "yes";

                } elsif ($traffic_type eq "automatic") {
                    General::Logging::log_user_error_message("Traffic type '$traffic_type' cannot be specified together with other traffic types in job parameter 'TRAFFIC_TO_START'");
                    return General::Playlist_Operations::RC_FALLBACK;
                } else {
                    General::Logging::log_user_warning_message("Unknown traffic type '$traffic_type' specified in job parameter 'TRAFFIC_TO_START', it's ignored");
                }
            }

            # Check that we have all needed pod's installed
            $message = "";
            if ($::JOB_PARAMS{'POD_BSF_NEEDED'} eq "yes") {
                if ($::JOB_PARAMS{'BSF_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'CONFIGURE_BSF_NEEDED'} eq "yes") {
                    # We are OK, after the configuration is done then the worker will also be up
                } elsif ($::JOB_PARAMS{'BSF_WORKER_READY'} eq "yes") {
                    # We are OK, the workers are up
                } else {
                    $message .= "  - Worker Pods for traffic type 'bsf' is needed but not installed or not configured\n";
                }
            }
            if ($::JOB_PARAMS{'POD_SCP_NEEDED'} eq "yes") {
                if ($::JOB_PARAMS{'SCP_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'CONFIGURE_SCP_NEEDED'} eq "yes") {
                    # We are OK, after the configuration is done then the worker will also be up
                } elsif ($::JOB_PARAMS{'SCP_WORKER_READY'} eq "yes") {
                    # We are OK, the workers are up
                } else {
                    $message .= "  - Worker Pods for traffic type 'scp' is needed but not installed or not configured\n";
                }
            }
            if ($::JOB_PARAMS{'POD_SEPP_NEEDED'} eq "yes") {
                if ($::JOB_PARAMS{'SEPP_WORKER_INSTALLED'} eq "yes" && $::JOB_PARAMS{'CONFIGURE_SEPP_NEEDED'} eq "yes") {
                    # We are OK, after the configuration is done then the worker will also be up
                } elsif ($::JOB_PARAMS{'SEPP_WORKER_READY'} eq "yes") {
                    # We are OK, the workers are up
                } else {
                    $message .= "  - Worker Pods for traffic type 'sepp' is needed but not installed or not configured\n";
                }
            }
            if ($message ne "") {
                $error_message .= "\nOne or more pods are missing or not configured for starting traffic:\n$message";
            }
        }

        $message = "";
        $message .= "POD_BSF_NEEDED:              $::JOB_PARAMS{'POD_BSF_NEEDED'}\n";
        $message .= "POD_SCP_NEEDED:              $::JOB_PARAMS{'POD_SCP_NEEDED'}\n";
        $message .= "POD_SEPP_NEEDED:             $::JOB_PARAMS{'POD_SEPP_NEEDED'}\n";
        $message .= "START_BSF_TRAFFIC:           $::JOB_PARAMS{'START_BSF_TRAFFIC'}\n";
        $message .= "START_SCP_TRAFFIC:           $::JOB_PARAMS{'START_SCP_TRAFFIC'}\n";
        $message .= "START_SEPP_TRAFFIC:          $::JOB_PARAMS{'START_SEPP_TRAFFIC'}\n";
        $message .= "TRAFFIC_TO_START:            $::JOB_PARAMS{'TRAFFIC_TO_START'}\n";

        if ($error_message eq "") {
            General::Logging::log_user_message($message);
            $rc = 0;
        } else {
            General::Logging::log_user_error_message($message . $error_message);
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
    sub Check_TRAFFIC_CONFIG_FILE_Parameter_P107S02T05 {
        my %cnf_lines_to_include;
        my @config_data;
        my $include_pattern = "";
        my $line;
        my $message = "";
        my @not_replaced;
        my $rc = 0;
        my @result;
        my $var_value;
        my %vars;

        if ($::JOB_PARAMS{'TRAFFIC_CONFIG_FILE'} eq "") {
            if ($::JOB_PARAMS{'USE_BUILT_IN_FILES_AT_MISSING_TRAFFIC_CONFIG_FILE'} eq "yes") {
                $::JOB_PARAMS{'TRAFFIC_CONFIG_FILE'} = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/base_stability_traffic/$::JOB_PARAMS{'DEFAULT_TRAFFIC_CONFIG_FILE'}";
                General::Logging::log_user_warning_message("Job parameter TRAFFIC_CONFIG_FILE not specified, using default DAFT package file:\nTRAFFIC_CONFIG_FILE=$::JOB_PARAMS{'TRAFFIC_CONFIG_FILE'}\n");
            } else {
                General::Logging::log_user_error_message("Job parameter TRAFFIC_CONFIG_FILE not specified and USE_BUILT_IN_FILES_AT_MISSING_TRAFFIC_CONFIG_FILE=no");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        }

        unless (-f $::JOB_PARAMS{'TRAFFIC_CONFIG_FILE'}) {
            # The file does not exist.
            if ($::JOB_PARAMS{'USE_BUILT_IN_FILES_AT_MISSING_TRAFFIC_CONFIG_FILE'} eq "yes") {
                $::JOB_PARAMS{'TRAFFIC_CONFIG_FILE'} = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/base_stability_traffic/$::JOB_PARAMS{'DEFAULT_TRAFFIC_CONFIG_FILE'}";
                General::Logging::log_user_warning_message("File specified with job parameter TRAFFIC_CONFIG_FILE does not exist.\nBut since USE_BUILT_IN_FILES_AT_MISSING_TRAFFIC_CONFIG_FILE=yes we continue and use default DAFT package file:\nTRAFFIC_CONFIG_FILE=$::JOB_PARAMS{'TRAFFIC_CONFIG_FILE'}\n");
            } else {
                General::Logging::log_user_error_message("Job parameter TRAFFIC_CONFIG_FILE does not exist");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        }

        if ($::JOB_PARAMS{'BASE_DIR'} eq "") {
            my $dir = File::Basename::dirname($::JOB_PARAMS{'TRAFFIC_CONFIG_FILE'});
            if (-d "$dir/traffic_config" && -d "$dir/traffic_scripts") {
                $::JOB_PARAMS{'BASE_DIR'} = $dir;
            } else {
                General::Logging::log_user_error_message("Directory '$dir/traffic_config/' and/or '$dir/traffic_scripts/' does not exist and Job parameter BASE_DIR not specified");
                return 1;
            }
        } else {
            my $dir = $::JOB_PARAMS{'BASE_DIR'};
            unless (-d "$dir/traffic_config" && -d "$dir/traffic_scripts") {
                General::Logging::log_user_error_message("Directory '$dir/traffic_config/' and/or '$dir/traffic_scripts/' does not exist");
                return 1;
            }
        }

        # Find the CNF pattern to use
        for my $var ("CONFIGURE_BSF_NEEDED", "CONFIGURE_SCP_NEEDED", "CONFIGURE_SEPP_NEEDED", "START_BSF_TRAFFIC", "START_SCP_TRAFFIC", "START_SEPP_TRAFFIC") {
            if ($::JOB_PARAMS{$var} eq "yes" && $var =~ /^[^_]+_([^_]+)_\S+/) {
                # Extract out the BSF, SCP and SEPP part
                $cnf_lines_to_include{$1}++;
            }
        }
        for my $cnf (sort keys %cnf_lines_to_include) {
            $include_pattern .= "$cnf|";
        }
        $include_pattern =~ s/\|$//;
        if ($include_pattern ne "") {
            $include_pattern = sprintf '^\s*(%s)_\S+?=.*', $include_pattern;
        } else {
            $include_pattern = '^\s*\S+?=.*';
        }

        General::Logging::log_user_message("Reading TRAFFIC_CONFIG_FILE file: $::JOB_PARAMS{'TRAFFIC_CONFIG_FILE'}");
        $rc = General::File_Operations::read_file(
            {
                "filename"            => $::JOB_PARAMS{'TRAFFIC_CONFIG_FILE'},
                "output-ref"          => \@config_data,
                "ignore-empty-lines"  => 1,
                "ignore-pattern"      => '^\s*#.*',
                "include-pattern"     => $include_pattern,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to read the TRAFFIC_CONFIG_FILE file");
            return 1;
        }

        for (@config_data) {
            $line = $_;
            while ($line) {
                if ($line =~ /^.*?<(\S+?)>.*/) {
                    # There is at least 1 place holder <xxx>
                    $vars{$1}++;
                    $line =~ s/^.*?<\S+?>//;
                } else {
                    # There are no place holders
                    $line = "";
                }
            }
        }
        if (%vars) {
            for my $var (sort keys %vars) {
                if (exists $::JOB_PARAMS{$var} && $::JOB_PARAMS{$var} ne "") {
                    $message .= "  <$var>\n";
                    $var_value = $::JOB_PARAMS{$var};
                    if ($var eq "TRAFFIC_DURATION") {
                        # Always start K6 with indefinite time, we will kill it when time has expired.
                        # This is needed otherwise the KPI check will fail because the traffic is stopped in K6 before we have finished checking the KPIs.
                        $var_value = 630720000;
                    } elsif ($var eq "eric_sc_values_anchor_parameter_VIP_SIG_BSF") {
                        $var_value = return_one_ip_address($var_value, $::JOB_PARAMS{'PREFER_IP_VERSION'}, "yes");
                    } elsif ($var =~ /^(eric_sc_values_anchor_parameter_VIP_SIG_SCP|eric_sc_values_anchor_parameter_VIP_SIG_SEPP|eric_sc_values_anchor_parameter_VIP_SIG2_SEPP)$/) {
                        $var_value = return_one_ip_address($var_value, $::JOB_PARAMS{'PREFER_IP_VERSION'}, "no");
                    }
                    for (@config_data) {
                        if (/<$var>/) {
                            s/<$var>/$var_value/g;
                        }
                    }
                } else {
                    push @not_replaced, $var;
                }
            }

            if ($message ne "") {
                $message = "The following place holders are replaced by the corresponding job variable value:\n$message";
            }
            if (@not_replaced) {
                $message .= "The following place holders were not replaced because either the job variable did not exist or was empty:\n";
                for (@not_replaced) {
                    $message .= "  <$_>\n";
                }

                General::Logging::log_user_error_message($message);
                return 1;
            }
            if ($message ne "") {
                General::Logging::log_user_message($message);
            } else {
                General::Logging::log_user_message("There are no place holders to replace");
            }
        } else {
            General::Logging::log_user_message("There are no place holders to replace");
        }

        # Store variables from the file into the %::JOB_PARAMS hash, unless they were also given on the
        # command line which in such case takes precedence.
        $message = "";
        for (@config_data) {
            if (/^\s*(\S+?)=(.*)/) {
                if (exists $::playlist_variables{$1} && $::playlist_variables{$1}{'default_value_used'} eq "no") {
                    # Do not update the variable from the file if the user specified a value on the command line
                } else {
                    # The user did not specify the variable on the command line so set/update the variable
                    $::JOB_PARAMS{$1} = $2;
                    $message .= "$_\n";
                }
            }
        }
        if ($message ne "") {
            $message = "The following job variables will be set:\n$message";
            General::Logging::log_user_message($message);
        } else {
            General::Logging::log_user_message("No job variables changed");
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
    sub Check_Needed_Simulators_P107S02T06 {
        my $error_message = "";
        my $message = "";
        my $rc = 0;
        my $traffic_type;

        General::Logging::log_user_message("Checking which Simulators are needed");

        # Set default values for all traffic simulators
        $::JOB_PARAMS{'SIMULATOR_CHFSIM_NEEDED'} = "no";
        $::JOB_PARAMS{'SIMULATOR_DSCLOAD_NEEDED'} = "no";
        $::JOB_PARAMS{'SIMULATOR_K6_NEEDED'} = "no";
        $::JOB_PARAMS{'SIMULATOR_NRFSIM_NEEDED'} = "no";
        $::JOB_PARAMS{'SIMULATOR_SC_MONITOR_NEEDED'} = "no";
        $::JOB_PARAMS{'SIMULATOR_SEPPSIM_NEEDED'} = "no";

        # Check which simulators or other tools are needed based on the traffic to configure or start
        for my $var ("CONFIGURE_BSF_NEEDED", "CONFIGURE_SCP_NEEDED", "CONFIGURE_SEPP_NEEDED", "START_BSF_TRAFFIC", "START_SCP_TRAFFIC", "START_SEPP_TRAFFIC") {
            if ($::JOB_PARAMS{$var} eq "yes" && $var =~ /^[^_]+_([^_]+)_\S+/) {
                # Extract out the BSF, SCP and SEPP part
                $traffic_type = lc($1);

                #
                # BSF Traffic
                #
                if ($traffic_type eq "bsf") {

                    # CHFSIM not needed

                    # DSCLOAD
                    if (exists $::JOB_PARAMS{'BSF_SIMULATOR_DSCLOAD_NEEDED'}) {
                        if ($::JOB_PARAMS{'BSF_SIMULATOR_DSCLOAD_NEEDED'} eq "yes") {
                            # The file specifically specifies that it's needed
                            $::JOB_PARAMS{'SIMULATOR_DSCLOAD_NEEDED'} = "yes";
                        }
                    } else {
                        # For backwards compatibility with files that did not specify this parameter
                        $::JOB_PARAMS{'SIMULATOR_DSCLOAD_NEEDED'} = "yes";
                    }

                    # K6
                    if (exists $::JOB_PARAMS{'BSF_SIMULATOR_K6_NEEDED'}) {
                        if ($::JOB_PARAMS{'BSF_SIMULATOR_K6_NEEDED'} eq "yes") {
                            # The file specifically specifies that it's needed
                            $::JOB_PARAMS{'SIMULATOR_K6_NEEDED'} = "yes";
                        }
                    } else {
                        # For backwards compatibility with files that did not specify this parameter
                        $::JOB_PARAMS{'SIMULATOR_K6_NEEDED'} = "yes";
                    }

                    # NRFSIM
                    if (exists $::JOB_PARAMS{'BSF_SIMULATOR_NRFSIM_NEEDED'}) {
                        if ($::JOB_PARAMS{'BSF_SIMULATOR_NRFSIM_NEEDED'} eq "yes") {
                            # The file specifically specifies that it's needed
                            $::JOB_PARAMS{'SIMULATOR_NRFSIM_NEEDED'} = "yes";
                        }
                    } else {
                        # For backwards compatibility with files that did not specify this parameter
                        $::JOB_PARAMS{'SIMULATOR_NRFSIM_NEEDED'} = "yes";
                    }

                    # SC-MONITOR not needed

                    # SEPPSIM not needed

                #
                # SCP Traffic
                #
                } elsif ($traffic_type eq "scp") {

                    # CHFSIM
                    if (exists $::JOB_PARAMS{'SCP_SIMULATOR_CHFSIM_NEEDED'}) {
                        if ($::JOB_PARAMS{'SCP_SIMULATOR_CHFSIM_NEEDED'} eq "yes") {
                            # The file specifically specifies that it's needed
                            $::JOB_PARAMS{'SIMULATOR_CHFSIM_NEEDED'} = "yes";
                        }
                    }

                    # DSCLOAD not needed

                    # K6
                    if (exists $::JOB_PARAMS{'SCP_SIMULATOR_K6_NEEDED'}) {
                        if ($::JOB_PARAMS{'SCP_SIMULATOR_K6_NEEDED'} eq "yes") {
                            # The file specifically specifies that it's needed
                            $::JOB_PARAMS{'SIMULATOR_K6_NEEDED'} = "yes";
                        }
                    } else {
                        # For backwards compatibility with files that did not specify this parameter
                        $::JOB_PARAMS{'SIMULATOR_K6_NEEDED'} = "yes";
                    }

                    # NRFSIM
                    if (exists $::JOB_PARAMS{'SCP_SIMULATOR_NRFSIM_NEEDED'}) {
                        if ($::JOB_PARAMS{'SCP_SIMULATOR_NRFSIM_NEEDED'} eq "yes") {
                            # The file specifically specifies that it's needed
                            $::JOB_PARAMS{'SIMULATOR_NRFSIM_NEEDED'} = "yes";
                        }
                    } else {
                        # For backwards compatibility with files that did not specify this parameter
                        $::JOB_PARAMS{'SIMULATOR_NRFSIM_NEEDED'} = "yes";
                    }

                    # SC-MONITOR
                    if (exists $::JOB_PARAMS{'SCP_SIMULATOR_SC_MONITOR_NEEDED'}) {
                        if ($::JOB_PARAMS{'SCP_SIMULATOR_SC_MONITOR_NEEDED'} eq "yes") {
                            # The file specifically specifies that it's needed
                            $::JOB_PARAMS{'SIMULATOR_SC_MONITOR_NEEDED'} = "yes";
                        }
                    }

                    # SEPPSIM
                    if (exists $::JOB_PARAMS{'SCP_SIMULATOR_SEPPSIM_NEEDED'}) {
                        if ($::JOB_PARAMS{'SCP_SIMULATOR_SEPPSIM_NEEDED'} eq "yes") {
                            # The file specifically specifies that it's needed
                            $::JOB_PARAMS{'SIMULATOR_SEPPSIM_NEEDED'} = "yes";
                        }
                    } else {
                        # For backwards compatibility with files that did not specify this parameter
                        $::JOB_PARAMS{'SIMULATOR_SEPPSIM_NEEDED'} = "yes";
                    }

                #
                # SEPP Traffic
                #
                } elsif ($traffic_type eq "sepp") {

                    # CHFSIM not needed

                    # DSCLOAD not needed

                    # K6
                    if (exists $::JOB_PARAMS{'SEPP_SIMULATOR_K6_NEEDED'}) {
                        if ($::JOB_PARAMS{'SEPP_SIMULATOR_K6_NEEDED'} eq "yes") {
                            # The file specifically specifies that it's needed
                            $::JOB_PARAMS{'SIMULATOR_K6_NEEDED'} = "yes";
                        }
                    } else {
                        # For backwards compatibility with files that did not specify this parameter
                        $::JOB_PARAMS{'SIMULATOR_K6_NEEDED'} = "yes";
                    }

                    # NRFSIM
                    if (exists $::JOB_PARAMS{'SEPP_SIMULATOR_NRFSIM_NEEDED'}) {
                        if ($::JOB_PARAMS{'SEPP_SIMULATOR_NRFSIM_NEEDED'} eq "yes") {
                            # The file specifically specifies that it's needed
                            $::JOB_PARAMS{'SIMULATOR_NRFSIM_NEEDED'} = "yes";
                        }
                    } else {
                        # For backwards compatibility with files that did not specify this parameter
                        $::JOB_PARAMS{'SIMULATOR_NRFSIM_NEEDED'} = "yes";
                    }

                    # SC-MONITOR
                    if (exists $::JOB_PARAMS{'SEPP_SIMULATOR_SC_MONITOR_NEEDED'}) {
                        if ($::JOB_PARAMS{'SEPP_SIMULATOR_SC_MONITOR_NEEDED'} eq "yes") {
                            # The file specifically specifies that it's needed
                            $::JOB_PARAMS{'SIMULATOR_SC_MONITOR_NEEDED'} = "yes";
                        }
                    }

                    # SEPPSIM
                    if (exists $::JOB_PARAMS{'SEPP_SIMULATOR_SEPPSIM_NEEDED'}) {
                        if ($::JOB_PARAMS{'SEPP_SIMULATOR_SEPPSIM_NEEDED'} eq "yes") {
                            # The file specifically specifies that it's needed
                            $::JOB_PARAMS{'SIMULATOR_SEPPSIM_NEEDED'} = "yes";
                        }
                    } else {
                        # For backwards compatibility with files that did not specify this parameter
                        $::JOB_PARAMS{'SIMULATOR_SEPPSIM_NEEDED'} = "yes";
                    }
                }
            }
        }

        # Check that we have all needed traffic tools installed
        $message = "";
        if ($::JOB_PARAMS{'SIMULATOR_CHFSIM_NEEDED'} eq "yes" && $::JOB_PARAMS{'SIMULATOR_CHFSIM_INSTALLED'} eq "no") {
            $message .= "  - Simulator CHFSIM is needed but not installed\n";
        }
        if ($::JOB_PARAMS{'SIMULATOR_DSCLOAD_NEEDED'} eq "yes" && $::JOB_PARAMS{'SIMULATOR_DSCLOAD_INSTALLED'} eq "no") {
            $message .= "  - Simulator DSCLOAD is needed but not installed\n";
        }
        if ($::JOB_PARAMS{'SIMULATOR_K6_NEEDED'} eq "yes" && $::JOB_PARAMS{'SIMULATOR_K6_INSTALLED'} eq "no") {
            $message .= "  - Simulator K6 is needed but not installed\n";
        }
        if ($::JOB_PARAMS{'SIMULATOR_NRFSIM_NEEDED'} eq "yes" && $::JOB_PARAMS{'SIMULATOR_NRFSIM_INSTALLED'} eq "no") {
            $message .= "  - Simulator NRFSIM is needed but not installed\n";
        }
        if ($::JOB_PARAMS{'SIMULATOR_SC_MONITOR_NEEDED'} eq "yes" && $::JOB_PARAMS{'SIMULATOR_SC_MONITOR_INSTALLED'} eq "no") {
            $message .= "  - SC Monitor is needed but not installed\n";
        }
        if ($::JOB_PARAMS{'SIMULATOR_SEPPSIM_NEEDED'} eq "yes" && $::JOB_PARAMS{'SIMULATOR_SEPPSIM_INSTALLED'} eq "no") {
            $message .= "  - Simulator SEPPSIM is needed but not installed\n";
        }
        if ($message ne "") {
            $error_message .= "\nOne or more simulators are missing:\n$message";
        }

        $message = "";
        $message .= "SIMULATOR_CHFSIM_NEEDED:     $::JOB_PARAMS{'SIMULATOR_CHFSIM_NEEDED'}\n";
        $message .= "SIMULATOR_DSCLOAD_NEEDED:    $::JOB_PARAMS{'SIMULATOR_DSCLOAD_NEEDED'}\n";
        $message .= "SIMULATOR_K6_NEEDED:         $::JOB_PARAMS{'SIMULATOR_K6_NEEDED'}\n";
        $message .= "SIMULATOR_NRFSIM_NEEDED:     $::JOB_PARAMS{'SIMULATOR_NRFSIM_NEEDED'}\n";
        $message .= "SIMULATOR_SC_MONITOR_NEEDED: $::JOB_PARAMS{'SIMULATOR_SC_MONITOR_NEEDED'}\n";
        $message .= "SIMULATOR_SEPPSIM_NEEDED:    $::JOB_PARAMS{'SIMULATOR_SEPPSIM_NEEDED'}\n";

        if ($error_message eq "") {
            General::Logging::log_user_message($message);
            $rc = 0;
        } else {
            General::Logging::log_user_error_message($message . $error_message);
            $rc = General::Playlist_Operations::RC_FALLBACK;
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
sub Setup_And_Configure_Tools_P107S03 {

    my $rc = 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_Configuration_DT_P107S03T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Execute_Bash_Commands_P107S03T02 } );
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
    sub Load_Configuration_DT_P107S03T01 {
        my @files = ();
        my $file_cnt = 0;
        my $rc = 0;

        #
        # BSF traffic files
        #

        if ($::JOB_PARAMS{'CONFIGURE_BSF_NEEDED'} eq "yes") {
            my @tmp_files = ();
            my $action_bsf_init_db_specified = 0;
            for my $var (sort keys %::JOB_PARAMS) {
                next unless ($var =~ /^BSF_CONFIG_FILE.*/);
                if ($::JOB_PARAMS{$var} ne "") {
                    if (-f "$::JOB_PARAMS{$var}") {
                        if ($::JOB_PARAMS{$var} =~ /^.+\/action_bsf_init_db\.netconf$/) {
                            $action_bsf_init_db_specified = 1;
                        }
                        push @tmp_files, $::JOB_PARAMS{$var};
                    } else {
                        General::Logging::log_user_error_message("The file '$var' $::JOB_PARAMS{$var} does not exist");
                        return 1;
                    }
                }
            }
            if (scalar @tmp_files == 0) {
                General::Logging::log_user_error_message("'BSF_CONFIG_FILE' is not specified");
                return 1;
            }
            if ($action_bsf_init_db_specified == 0) {
                my $dir = File::Basename::dirname($tmp_files[0]);
                if (-f "$dir/action_bsf_init_db.netconf") {
                    push @tmp_files, "$dir/action_bsf_init_db.netconf";
                }
            }
            push @files, @tmp_files if @tmp_files;
        }

        #
        # SCP traffic files
        #

        if ($::JOB_PARAMS{'CONFIGURE_SCP_NEEDED'} eq "yes") {
            my @tmp_files = ();
            for my $var (sort keys %::JOB_PARAMS) {
                next unless ($var =~ /^SCP_CONFIG_FILE.*/);
                if ($::JOB_PARAMS{$var} ne "") {
                    if (-f "$::JOB_PARAMS{$var}") {
                        push @tmp_files, $::JOB_PARAMS{$var};
                    } else {
                        General::Logging::log_user_error_message("The file '$var' $::JOB_PARAMS{$var} does not exist");
                        return 1;
                    }
                }
            }
            if (scalar @tmp_files == 0) {
                General::Logging::log_user_error_message("'SCP_CONFIG_FILE' is not specified");
                return 1;
            }
            push @files, @tmp_files if @tmp_files;
        }

        #
        # SEPP traffic files
        #

        if ($::JOB_PARAMS{'CONFIGURE_SEPP_NEEDED'} eq "yes") {
            my @tmp_files = ();
            for my $var (sort keys %::JOB_PARAMS) {
                next unless ($var =~ /^SEPP_CONFIG_FILE.*/);
                if ($::JOB_PARAMS{$var} ne "") {
                    if (-f "$::JOB_PARAMS{$var}") {
                        push @tmp_files, $::JOB_PARAMS{$var};
                    } else {
                        General::Logging::log_user_error_message("The file '$var' $::JOB_PARAMS{$var} does not exist");
                        return 1;
                    }
                }
            }
            if (scalar @tmp_files == 0) {
                General::Logging::log_user_error_message("'SEPP_CONFIG_FILE' is not specified");
                return 1;
            }
            push @files, @tmp_files if @tmp_files;
        }

        if (scalar @files == 0) {
            General::Logging::log_user_message("Nothing to configure");
            push @::JOB_STATUS, "(-) Nothing to configure";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        #
        # Preparing and loading of the .cli and .netconf files using the 004_Config_Management playlist
        #

        # Set 004 playlist variables
        for my $filename (@files) {
            if ($file_cnt == 0) {
                $::JOB_PARAMS{'CONFIG_DATA_FILE'} = $filename;
            } else {
                $::JOB_PARAMS{"CONFIG_DATA_FILE_$file_cnt"} = $filename;
            }
            $file_cnt++;
        }
        if ($::JOB_PARAMS{'CONFIG_USER_NAME'} eq "") {
            # No user specified for loading the configuration data so we need to check if the files
            # to load already includes the user name as part of the file name, and if not we hard code
            # the user to the 'expert' user which should be able to load any data.
            my $found = 1;
            for my $filename (@files) {
                if ($filename !~ /,user=\S+/) {
                    # At least one file does not have the specific naming convention
                    $found = 0;
                }
            }
            if ($found == 0) {
                # Hard code the user name and take the password from the network configuration file
                $::JOB_PARAMS{'CONFIG_USER_NAME'} = "expert";
            }
        }

        # Configure the node
        $::JOB_PARAMS{'P004_FETCH_NODE_INFORMATION'} = "no";    # No need to fetch the information again
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::004_Config_Management::main } );
        return $rc if $rc < 0;

        # Set Job Type again since it was changed inside the 004 playlist
        $::JOB_PARAMS{'JOBTYPE'} = "TRAFFIC_TOOLS_CONFIGURE_AND_START";

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
    sub Execute_Bash_Commands_P107S03T02 {
        my @files = ();
        my $file_cnt = 0;
        my $rc = 0;

        #
        # BSF traffic files
        #

        if ($::JOB_PARAMS{'CONFIGURE_BSF_NEEDED'} eq "yes") {
            my @tmp_files = ();
            for my $var (sort keys %::JOB_PARAMS) {
                next unless ($var =~ /^BSF_BASH_COMMAND_FILE.*/);
                if ($::JOB_PARAMS{$var} ne "") {
                    if (-f "$::JOB_PARAMS{$var}") {
                        my $filename = check_script_file_for_placeholders_and_copy_to_workspace_if_needed($::JOB_PARAMS{$var});
                        if ($filename eq "") {
                            # Failed to parse or copy the file, just return error
                            return 1;
                        }
                        push @tmp_files, $filename;
                    } else {
                        General::Logging::log_user_error_message("The file '$var' $::JOB_PARAMS{$var} does not exist");
                        return 1;
                    }
                }
            }
            push @files, @tmp_files if @tmp_files;
        }

        #
        # SCP traffic files
        #

        if ($::JOB_PARAMS{'CONFIGURE_SCP_NEEDED'} eq "yes") {
            my @tmp_files = ();
            for my $var (sort keys %::JOB_PARAMS) {
                next unless ($var =~ /^SCP_BASH_COMMAND_FILE.*/);
                if ($::JOB_PARAMS{$var} ne "") {
                    if (-f "$::JOB_PARAMS{$var}") {
                        my $filename = check_script_file_for_placeholders_and_copy_to_workspace_if_needed($::JOB_PARAMS{$var});
                        if ($filename eq "") {
                            # Failed to parse or copy the file, just return error
                            return 1;
                        }
                        push @tmp_files, $filename;
                    } else {
                        General::Logging::log_user_error_message("The file '$var' $::JOB_PARAMS{$var} does not exist");
                        return 1;
                    }
                }
            }
            push @files, @tmp_files if @tmp_files;
        }

        #
        # SEPP traffic files
        #

        if ($::JOB_PARAMS{'CONFIGURE_SEPP_NEEDED'} eq "yes") {
            my @tmp_files = ();
            for my $var (sort keys %::JOB_PARAMS) {
                next unless ($var =~ /^SEPP_BASH_COMMAND_FILE.*/);
                if ($::JOB_PARAMS{$var} ne "") {
                    if (-f "$::JOB_PARAMS{$var}") {
                        my $filename = check_script_file_for_placeholders_and_copy_to_workspace_if_needed($::JOB_PARAMS{$var});
                        if ($filename eq "") {
                            # Failed to parse or copy the file, just return error
                            return 1;
                        }
                        push @tmp_files, $filename;
                    } else {
                        General::Logging::log_user_error_message("The file '$var' $::JOB_PARAMS{$var} does not exist");
                        return 1;
                    }
                }
            }
            push @files, @tmp_files if @tmp_files;
        }

        if (scalar @files == 0) {
            General::Logging::log_user_message("Nothing to execute");
            push @::JOB_STATUS, "(-) Nothing to execute";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        #
        # Executing the bash command files
        #

        for my $filename (@files) {
            General::Logging::log_user_message("Executing command file: $filename");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$filename",
                    "hide-output"   => 1,
                    "save-to-file"  => "$filename.log",
                }
            );
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) Command successful: $filename";
            } else {
                push @::JOB_STATUS, "(x) Command failed: $filename";
                return 1;
            }
        }

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
sub Start_Traffic_P107S04 {

    my $rc = 0;

    # - Should we create/update the certificates and secrets for the tools or we just have to assume that it's already been done?
    # - Update and copy scripts to the K6 pods
    # - Create session bindings for BSF dscload
    # - Create k6 certificates for SEPP traffic, or should this be done instde the 105_Traffic... playlist ?????

    if ($::JOB_PARAMS{'START_BSF_TRAFFIC'} eq "yes" || $::JOB_PARAMS{'START_SCP_TRAFFIC'} eq "yes" || $::JOB_PARAMS{'START_SEPP_TRAFFIC'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Pod_Status_P107S04T01 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'START_BSF_TRAFFIC'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Clear_BSF_Session_Bindings_P107S04T02 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_BSF_K6_Script_To_Pods_P107S04T03 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_BSF_K6_File_To_Pods_P107S04T04 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Start_BSF_K6_Traffic_P107S04T05 } );
        return $rc if $rc < 0;

        if ($::JOB_PARAMS{'BSF_DSCLOAD_LOAD_SESSION_BINDINGS'} eq "yes") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_BSF_dscload_Session_Bindings_P107S04T06 } );
            return $rc if $rc < 0;
        }

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Start_BSF_dscload_Traffic_P107S04T07 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'START_SCP_TRAFFIC'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_SCP_K6_Script_To_Pods_P107S04T08 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_SCP_K6_File_To_Pods_P107S04T09 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Start_SCP_K6_Traffic_P107S04T10 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'START_SEPP_TRAFFIC'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_SEPP_K6_Script_To_Pods_P107S04T11 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_SEPP_K6_File_To_Pods_P107S04T12 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Start_SEPP_K6_Traffic_P107S04T13 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'START_BSF_TRAFFIC'} eq "no" && $::JOB_PARAMS{'START_SCP_TRAFFIC'} eq "no" && $::JOB_PARAMS{'START_SEPP_TRAFFIC'} eq "no") {
        General::Logging::log_user_message("Nothing to start");
        push @::JOB_STATUS, "(-) Nothing to start";
        return General::Playlist_Operations::RC_STEPOUT;
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
    sub Check_Pod_Status_P107S04T01 {

        my $delay_time = 10;
        my $max_attempts = 0;
        my $max_time = 60;
        my $repeated_checks = 0;
        my $message = "";
        my $rc = 0;
        my @result;
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

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
                "max-attempts"                              => $max_attempts,
                "max-time"                                  => $max_time,
                "namespace"                                 => $namespace,
                "pod-exclude-ready-list"                    => [ "eric-data-search-engine-curator.*", "eric-log-transformer.*" ],
                "pod-exclude-status-list"                   => [ "eric-data-search-engine-curator.*", "eric-log-transformer.*" ],
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
            push @::JOB_STATUS, "(/) POD status check was successful";
        } else {
            push @::JOB_STATUS, "(x) POD status check failed";
            $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
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
    sub Clear_BSF_Session_Bindings_P107S04T02 {
        my @pod_names;
        my $rc = 0;

        General::Logging::log_user_message("Fetching eric-data-wide-column-database-cd or eric-bsf-wcdb-cd- pod name");
        @pod_names = ADP::Kubernetes_Operations::get_pod_names(
            {
                "hide-output"       => 1,
                "namespace"         => $::JOB_PARAMS{'SC_NAMESPACE'},
                "pod-include-list"  => [ 'eric-data-wide-column-database-cd-(\d+|datacenter.+)', 'eric-bsf-wcdb-cd-(\d+|datacenter.+)' ],
            }
        );
        if (scalar @pod_names == 0) {
            General::Logging::log_user_error_message("Failed to find any eric-data-wide-column-database-cd or eric-bsf-wcdb-cd- pods");
            return 1;
        }

        if ($::JOB_PARAMS{'BSF_FETCH_SESSION_BINDING_DATA'} eq "yes") {
            General::Logging::log_user_message("Fetch current BSF session binding data.\nThis will take a while to complete.\n");
            General::OS_Operations::send_command(
                {
                    "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} exec $pod_names[0] -c cassandra -- cqlsh -e \"select * from nbsf_management_keyspace.pcf_bindings;\"",
                    "hide-output"   => 1,
                }
            );
            # Clean CTRL-C counter in case the user pressed CTRL-C while fetching the session binding data.
            if (General::OS_Operations::signal_captured("INT") > 0) {
                General::Logging::log_user_warning_message("User interrupted the execution by pressing CTRL-C\n");
            }
        }

        General::Logging::log_user_message("Clearing BSF session binding data");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} exec $pod_names[0] -c cassandra -- cqlsh -e \"truncate nbsf_management_keyspace.pcf_bindings;\"",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Clearing BSF session binding data successful";
        } else {
            # display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_warning_message("Failed to clear session binding data, but error ignored");
            $rc = 0;
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
    sub Copy_BSF_K6_Script_To_Pods_P107S04T03 {
        my $file;

        # Look for BSF_K6_SCRIPT_FILE, BSF_K6_SCRIPT_FILE_01, BSF_K6_SCRIPT_FILE_02 etc. and copy multiple files to workspace and K6 pod
        for my $var (sort keys %::JOB_PARAMS) {
            next unless ($var =~ /^BSF_K6_SCRIPT_FILE.*/);
            if ($::JOB_PARAMS{$var} ne "") {
                #
                # Copy script file to job workspace
                #
                $file = update_k6_script_and_copy_to_workspace($::JOB_PARAMS{$var});
                if ($file eq "") {
                    # The copy operation failed
                    return 1;
                }

                #
                # Copy script file to pod
                #
                if (copy_k6_script_to_pods($file) != 0) {
                    # The copy operation failed
                    return 1;
                }
            }
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
    sub Copy_BSF_K6_File_To_Pods_P107S04T04 {
        my $file;
        my $from_path;
        my $to_path;

        # Look for BSF_K6_COPY_FILE, BSF_K6_COPY_FILE_01, BSF_K6_COPY_FILE_02 etc. and copy multiple files to K6 pod
        for my $var (sort keys %::JOB_PARAMS) {
            next unless ($var =~ /^BSF_K6_COPY_FILE.*/);
            if ($::JOB_PARAMS{$var} ne "") {
                #
                # Extract the from and to file paths
                #
                if ($::JOB_PARAMS{$var} =~ /^(\S+)\s+(\S+)\s*$/) {
                    $from_path = $1;
                    $to_path = $2;

                    # Replace any place holders in the from file path
                    $from_path =~ s/<BASE_DIR>/$::JOB_PARAMS{'BASE_DIR'}/g;
                } else {
                    General::Logging::log_user_error_message("Incorrect format of variable '$var': $::JOB_PARAMS{$var}\nShould be two file paths separated by white space.\n");
                    exit 1;
                }

                #
                # Copy file to pod
                #
                if (copy_k6_file_to_pods($from_path, $to_path) != 0) {
                    # The copy operation failed
                    return 1;
                }
            }
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
    sub Start_BSF_K6_Traffic_P107S04T05 {
        my $count = 1;
        my $rc = 0;
        my $traffic_type;

        # look for BSF_K6_TRAFFIC_COMMAND, BSF_K6_TRAFFIC_COMMAND_01, BSF_K6_TRAFFIC_COMMAND_02 etc. and create multiple commands
        for my $var (sort keys %::JOB_PARAMS) {
            next unless ($var =~ /^BSF_K6_TRAFFIC_COMMAND.*/);
            if ($::JOB_PARAMS{$var} ne "") {
                $traffic_type = sprintf "bsf_%02i", $count++;
                if ( start_k6_traffic_in_pods($traffic_type, $::JOB_PARAMS{$var}) != 0) {
                    return 1;
                }
            }
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
    sub Load_BSF_dscload_Session_Bindings_P107S04T06 {
        my $bsf_worker_service_name = ADP::Kubernetes_Operations::get_service_name($::JOB_PARAMS{'SC_NAMESPACE'}, "eric-bsf-worker");
        my $node_ip = "";
        if (exists $::NETWORK_CONFIG_PARAMS{'node_worker_address'} && $::NETWORK_CONFIG_PARAMS{'node_worker_address'}{'value'} ne "CHANGEME") {
            $node_ip = return_one_ip_address($::NETWORK_CONFIG_PARAMS{'node_worker_address'}{'value'}, $::JOB_PARAMS{'PREFER_IP_VERSION'}, "no");
        } else {
            $node_ip = sprintf '$( %s get nodes -o jsonpath="{.items[0].status.addresses[0].address}" --selector="node-role.kubernetes.io/worker")', $::JOB_PARAMS{'KUBECTL_EXECUTABLE'};
        }
        my @commands = (
            (sprintf 'export NODE_IP=%s', $node_ip),
            (sprintf 'export BSFWRK_PORT=$( kubectl get svc --namespace %s %s -o jsonpath="{.spec.ports[0].nodePort}")', $::JOB_PARAMS{'SC_NAMESPACE'}, $bsf_worker_service_name),
            'if [[ $NODE_IP =~ .*:.* ]]',
            'then',
            '  export BINDING_IPV6=\'{"supi":"imsi-12345","ipv6Prefix":"2001:db8:abcd:0012::0/64","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf-diamhost.com","pcfDiamRealm":"pcf-diamrealm.com","snssai":{"sst":2,"sd":"DEADF0"}}\'',
            '  curl -v -d "$BINDING_IPV6" -H "Content-Type: application/json" -X POST "http://[$NODE_IP]:$BSFWRK_PORT/nbsf-management/v1/pcfBindings"',
            'else',
            '  export BINDING_IPV4=\'{"supi":"imsi-12345","ipv4Addr":"10.0.0.1","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf-diamhost.com","pcfDiamRealm":"pcf-diamrealm.com","snssai":{"sst":2,"sd":"DEADF0"}}\'',
            '  curl -v -d "$BINDING_IPV4" -H "Content-Type: application/json" -X POST "http://$NODE_IP:$BSFWRK_PORT/nbsf-management/v1/pcfBindings"',
            'fi',
        );
        my $rc = 0;
        my @result;

        $rc = General::File_Operations::write_file(
            {
                "filename"          => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/bsf_dscload_session_bindings.bash",
                "output-ref"        => \@commands,
                "file-access-mode"  => "777",
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to write the bsf_dscload_session_bindings.bash file");
            return 1;
        }

        General::Logging::log_user_message("Loading BSF dscload session binding data");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/bsf_dscload_session_bindings.bash",
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to load session binding data");
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
    sub Start_BSF_dscload_Traffic_P107S04T07 {
        my $command = "";
        my $rc = 0;

        if ($::JOB_PARAMS{'BSF_DSCLOAD_TRAFFIC_COMMAND'} ne "") {
            $command = $::JOB_PARAMS{'BSF_DSCLOAD_TRAFFIC_COMMAND'};
        } else {
            $command = '/opt/dsc-load/dsc_load';
        }

        $rc = start_dscload_traffic_in_pods("bsf", $command);

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
    sub Copy_SCP_K6_Script_To_Pods_P107S04T08 {
        my $file;

        # Look for SCP_K6_SCRIPT_FILE, SCP_K6_SCRIPT_FILE_01, SCP_K6_SCRIPT_FILE_02 etc. and copy multiple files to workspace and K6 pod
        for my $var (sort keys %::JOB_PARAMS) {
            next unless ($var =~ /^SCP_K6_SCRIPT_FILE.*/);
            if ($::JOB_PARAMS{$var} ne "") {
                #
                # Copy script file to job workspace
                #
                $file = update_k6_script_and_copy_to_workspace($::JOB_PARAMS{$var});
                if ($file eq "") {
                    # The copy operation failed
                    return 1;
                }

                #
                # Copy script file to pod
                #
                if (copy_k6_script_to_pods($file) != 0) {
                    # The copy operation failed
                    return 1;
                }
            }
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
    sub Copy_SCP_K6_File_To_Pods_P107S04T09 {
        my $file;
        my $from_path;
        my $to_path;

        # Look for SCP_K6_COPY_FILE, SCP_K6_COPY_FILE_01, SCP_K6_COPY_FILE_02 etc. and copy multiple files to K6 pod
        for my $var (sort keys %::JOB_PARAMS) {
            next unless ($var =~ /^SCP_K6_COPY_FILE.*/);
            if ($::JOB_PARAMS{$var} ne "") {
                #
                # Extract the from and to file paths
                #
                if ($::JOB_PARAMS{$var} =~ /^(\S+)\s+(\S+)\s*$/) {
                    $from_path = $1;
                    $to_path = $2;

                    # Replace any place holders in the from file path
                    $from_path =~ s/<BASE_DIR>/$::JOB_PARAMS{'BASE_DIR'}/g;
                } else {
                    General::Logging::log_user_error_message("Incorrect format of variable '$var': $::JOB_PARAMS{$var}\nShould be two file paths separated by white space.\n");
                    exit 1;
                }

                #
                # Copy file to pod
                #
                if (copy_k6_file_to_pods($from_path, $to_path) != 0) {
                    # The copy operation failed
                    return 1;
                }
            }
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
    sub Start_SCP_K6_Traffic_P107S04T10 {
        my $count = 1;
        my $rc = 0;
        my $traffic_type;

        # look for SCP_K6_TRAFFIC_COMMAND, SCP_K6_TRAFFIC_COMMAND_01, SCP_K6_TRAFFIC_COMMAND_02 etc. and create multiple commands
        for my $var (sort keys %::JOB_PARAMS) {
            next unless ($var =~ /^SCP_K6_TRAFFIC_COMMAND.*/);
            if ($::JOB_PARAMS{$var} ne "") {
                $traffic_type = sprintf "scp_%02i", $count++;
                if ( start_k6_traffic_in_pods($traffic_type, $::JOB_PARAMS{$var}) != 0) {
                    return 1;
                }
            }
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
    sub Copy_SEPP_K6_Script_To_Pods_P107S04T11 {
        my $file;

        # Look for SEPP_K6_SCRIPT_FILE, SEPP_K6_SCRIPT_FILE_01, SEPP_K6_SCRIPT_FILE_02 etc. and copy multiple files to workspace and K6 pod
        for my $var (sort keys %::JOB_PARAMS) {
            next unless ($var =~ /^SEPP_K6_SCRIPT_FILE.*/);
            if ($::JOB_PARAMS{$var} ne "") {
                #
                # Copy script file to job workspace
                #
                $file = update_k6_script_and_copy_to_workspace($::JOB_PARAMS{$var});
                if ($file eq "") {
                    # The copy operation failed
                    return 1;
                }

                #
                # Copy script file to pod
                #
                if (copy_k6_script_to_pods($file) != 0) {
                    # The copy operation failed
                    return 1;
                }
            }
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
    sub Copy_SEPP_K6_File_To_Pods_P107S04T12 {
        my $file;
        my $from_path;
        my $to_path;

        # Look for SEPP_K6_COPY_FILE, SEPP_K6_COPY_FILE_01, SEPP_K6_COPY_FILE_02 etc. and copy multiple files to K6 pod
        for my $var (sort keys %::JOB_PARAMS) {
            next unless ($var =~ /^SEPP_K6_COPY_FILE.*/);
            if ($::JOB_PARAMS{$var} ne "") {
                #
                # Extract the from and to file paths
                #
                if ($::JOB_PARAMS{$var} =~ /^(\S+)\s+(\S+)\s*$/) {
                    $from_path = $1;
                    $to_path = $2;

                    # Replace any place holders in the from file path
                    $from_path =~ s/<BASE_DIR>/$::JOB_PARAMS{'BASE_DIR'}/g;
                } else {
                    General::Logging::log_user_error_message("Incorrect format of variable '$var': $::JOB_PARAMS{$var}\nShould be two file paths separated by white space.\n");
                    exit 1;
                }

                #
                # Copy file to pod
                #
                if (copy_k6_file_to_pods($from_path, $to_path) != 0) {
                    # The copy operation failed
                    return 1;
                }
            }
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
    sub Start_SEPP_K6_Traffic_P107S04T13 {
        my $count = 1;
        my $rc = 0;
        my $traffic_type;

        # look for SEPP_K6_TRAFFIC_COMMAND, SEPP_K6_TRAFFIC_COMMAND_01, SEPP_K6_TRAFFIC_COMMAND_02 etc. and create multiple commands
        for my $var (sort keys %::JOB_PARAMS) {
            next unless ($var =~ /^SEPP_K6_TRAFFIC_COMMAND.*/);
            if ($::JOB_PARAMS{$var} ne "") {
                $traffic_type = sprintf "sepp_%02i", $count++;
                if ( start_k6_traffic_in_pods($traffic_type, $::JOB_PARAMS{$var}) != 0) {
                    return 1;
                }
            }
        }

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
sub Wait_And_Then_Stop_Traffic_P107S05 {

    my $rc;

    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        General::Logging::log_user_message("No wait needed because DEBUG_PLAYLIST=yes");
        return General::Playlist_Operations::RC_STEPOUT;
    }

    if ($::JOB_PARAMS{'CHECK_TRAFFIC_SUCCESS_RATE'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Traffic_Success_Rate_P107S05T01 } );
        return $rc if $rc < 0;
    }

    # Set the start time of (successful) traffic.
    # To be used when printing KPI statistics.
    $::JOB_PARAMS{'START_EPOCH_OF_TRAFFIC'} = time();

    $::JOB_PARAMS{'TRAFFIC_SUCCESS_RATE_CHECK_FAILED'} = "no" unless (exists $::JOB_PARAMS{'TRAFFIC_SUCCESS_RATE_CHECK_FAILED'});

    if ($::JOB_PARAMS{'TRAFFIC_DURATION'} eq "indefinite") {
        General::Logging::log_user_message("No wait wanted because TRAFFIC_DURATION=indefinite");

        if ($::JOB_PARAMS{'TRAFFIC_SUCCESS_RATE_CHECK_FAILED'} eq "no") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Show_Information_How_To_Stop_Traffic_P107S05T02 } );
            return $rc if $rc < 0;
        } else {
            # The traffic failed to start, so just stop the background processes
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Stop_Traffic_P107S05T04 } );
            return $rc if $rc < 0;
        }
    } else {
        if ($::JOB_PARAMS{'TRAFFIC_SUCCESS_RATE_CHECK_FAILED'} eq "no") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Traffic_To_Finish_P107S05T03 } );
            return $rc if $rc < 0;

            if ($::JOB_PARAMS{'CHECK_TRAFFIC_SUCCESS_RATE'} eq "yes") {
                # Check KPI statistics
                $::JOB_PARAMS{'P933_TASK'} = "KPI_VERDICT";
                $::JOB_PARAMS{'P933_RESOLUTION'} = $::JOB_PARAMS{'KPI_COLLECTION_RESOLUTION'};
                $::JOB_PARAMS{'P933_PAST'} = sprintf "%ds", (time() - $::JOB_PARAMS{'START_EPOCH_OF_TRAFFIC'});
                $::JOB_PARAMS{'P933_SUCCESS_RATE_DESCRIPTION'} = uc("($::JOB_PARAMS{'TRAFFIC_TO_START'}).+");
                $::JOB_PARAMS{'P933_SUCCESS_RATE_DESCRIPTION'} =~ s/[ ,]+/|/g;
                $::JOB_PARAMS{'P933_SUCCESS_RATE_THRESHOLD'} = $::JOB_PARAMS{'KPI_SUCCESS_RATE_THRESHOLD'};
                $::JOB_PARAMS{'P933_SUMMARY_MESSAGE'} = "Traffic Exit KPI Success Rate Verdict";
                $::JOB_PARAMS{'P933_VERDICT_IGNORE_FAILURE'} = "yes";
                $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_statistics/exit_verdict";
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::933_Handle_KPI_Statistics::main } );
                return $rc if $rc < 0;

                if ($::JOB_PARAMS{'P933_VERDICT_RESULT'} eq "SUCCESS") {
                    General::Logging::log_user_message("Exit KPI Check was successful");
                } else {
                    if ($::JOB_PARAMS{'IGNORE_FAILED_TRAFFIC_SUCCESS_RATE_CHECK'} eq "yes") {
                        General::Logging::log_user_warning_message("Exit KPI Check failed, but ignored because IGNORE_FAILED_TRAFFIC_SUCCESS_RATE_CHECK=yes");
                    } else {
                        General::Logging::log_user_error_message("Exit KPI Check failed");
                        # Also mark the playlist as failed
                        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
                    }
                }
            }
        }

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Stop_Traffic_P107S05T04 } );
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
    sub Check_Traffic_Success_Rate_P107S05T01 {
        my $duration = $::JOB_PARAMS{'CHECK_TRAFFIC_SUCCESS_RATE_DURATION'};
        my $rc = 0;
        my @result;
        my $traffic_types = uc("($::JOB_PARAMS{'TRAFFIC_TO_START'}).+");

        if ($::JOB_PARAMS{'CHECK_TRAFFIC_DELAY'} > 0) {
            General::Logging::log_user_message("Waiting $::JOB_PARAMS{'CHECK_TRAFFIC_DELAY'} seconds before checking traffic success rate.\nThe wait can be interrupted by pressing CTRL-C.\n");
            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "progress-interval" => 10,
                    "progress-message"  => 1,
                    "seconds"           => $::JOB_PARAMS{'CHECK_TRAFFIC_DELAY'},
                    "use-logging"       => 1,
                }
            );
            if ($rc == 1) {
                # CTRL-C pressed, just ignore it
                $rc = 0;
            }
        }

        # E.g. replace (BSF,SCP,SEPP) with (BSF|SCP|SEPP)
        $traffic_types =~ s/,/|/g;

        General::Logging::log_user_message("Checking traffic success rate.\nThis will take at least $duration seconds.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/system_health_check.pl -n $::JOB_PARAMS{'SC_NAMESPACE'} -c kpi_success_rate -v kpi_details_at_success=1 -v kpi_use_cached_values=no -v kpi_success_rate_description='$traffic_types' -v kpi_future=${duration}s --progress_type=none",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            General::Logging::log_user_message((join "\n", @result) . "\nTraffic entry success rate check was successful.");
            $::JOB_PARAMS{'TRAFFIC_SUCCESS_RATE_CHECK_FAILED'} = "no";
            push @::JOB_STATUS, "(/) Traffic Entry KPI Success Rate Verdict was successful";
        } else {
            push @::JOB_STATUS, "(x) Traffic Entry KPI Success Rate Verdict was unsuccessful";
            if ($::JOB_PARAMS{'IGNORE_FAILED_TRAFFIC_SUCCESS_RATE_CHECK'} eq "yes") {
                General::Logging::log_user_warning_message((join "\n", @result) . "\nTraffic entry success rate check failed, but ignored because IGNORE_FAILED_TRAFFIC_SUCCESS_RATE_CHECK=yes.");
                $::JOB_PARAMS{'TRAFFIC_SUCCESS_RATE_CHECK_FAILED'} = "no";
                $rc = 0;
            } else {
                General::Logging::log_user_error_message((join "\n", @result) . "\nTraffic entry success rate check failed.");
                $::JOB_PARAMS{'TRAFFIC_SUCCESS_RATE_CHECK_FAILED'} = "yes";
                $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
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
    sub Show_Information_How_To_Stop_Traffic_P107S05T02 {
        my @children;
        my $message = "";
        my $pid;
        my @pids = ();
        my @pod_names = ();
        my $rc = 0;
        my @status_messages1;
        my @status_messages2;
        my $traffic_type;

        # Find all background process id's
        @pids = get_traffic_pids();

        if (scalar @pids > 0) {
            $message  = "Traffic will not be stopped because TRAFFIC_DURATION=indefinite.\n";
            $message .= "So to stop the traffic you need to manually kill the following processes:\n";
            for $pid (@pids) {
                @children = General::OS_Operations::process_return_all_children($pid);
                $message .= sprintf "  - kill $pid %s\n", (join " ", @children);
                $message .= sprintf "  - ps -fl $pid %s    (check)\n", (join " ", @children);
                push @status_messages1, sprintf"kill $pid %s", (join " ", @children);
                push @status_messages2, sprintf "ps -fl $pid %s", (join " ", @children);

                # Now we need to clean out any knowledge about the background processes so that
                # DAFT doesn't automatically kill them when the job terminates.
                General::OS_Operations::background_process_mark_not_running($pid);
            }
            $message .= "Then you need to login to the k6 and dscload pods and also kill the running processes there:\n";
            @pod_names = ADP::Kubernetes_Operations::get_pod_names(
                {
                    "hide-output"       => 1,
                    "kubeconfig"        => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
                    "namespace"         => $::JOB_PARAMS{'TOOLS_NAMESPACE'},
                    "pod-include-list"  => [ "eric-dscload-deployment-", "eric-k6-deployment-" ],
                }
            );
            if (@pod_names) {
                for my $pod_name (@pod_names) {
                    @pids = get_traffic_pids_in_pod($pod_name);
                    for $pid (@pids) {
                        $message .= "  - $::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} exec $pod_name -- kill $pid\n";
                        $message .= "  - $::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} exec $pod_name -- ps -fel    (check)\n";
                        push @status_messages1, "$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} exec $pod_name -- kill $pid";
                        push @status_messages2, "$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} exec $pod_name -- ps -fl -p $pid";
                    }
                }
            } else {
                General::Logging::log_user_error_message("No 'eric-dscload-deployment-' pods found");
                return 1;
            }

            $message .= "Or execute one of the following commands:\n";
            $message .= "  - $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/stop_traffic.bash\n";
            $message .= "    or more forcefull, which might also kill other namespace traffic:\n";
            $message .= "  - $::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/check_traffic_processes.pl -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} -k\n";

            General::Logging::log_user_message($message);

            # Write extra status messages to file
            General::File_Operations::write_file(
                {
                    "filename"          => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/stop_traffic.bash",
                    "append-file"       => 1,
                    "output-ref"        => \@status_messages1,
                    "file-access-mode"  => "777",
                }
            );

            unshift @status_messages1, ( "Stop Traffic", "============", "", "To stop the traffic; kill the following processes:" );
            push @status_messages1, ( "" , "Or execute the following command:", "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/stop_traffic.bash", "", "If the processes does not terminate then add the parameter '-9' to the kill commands." );
            unshift @status_messages2, ( "", "To check that processes has been stopped:" );
            push @status_messages1, @status_messages2;
            push @status_messages1, ( "", "Or execute the following command:", "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/check_traffic_processes.pl -n $::JOB_PARAMS{'TOOLS_NAMESPACE'}", "" );

            # Write extra status messages to file
            General::File_Operations::write_file(
                {
                    "filename"          => "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/extra_status_messages.txt",
                    "append-file"       => 1,
                    "output-ref"        => \@status_messages1,
                }
            );
        } else {
            General::Logging::log_user_message("No background processes found, so no information to show");
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
    sub Wait_For_Traffic_To_Finish_P107S05T03 {
        my $rc = 0;
        my $start_time = time();
        my $duration = General::OS_Operations::convert_seconds_to_wdhms( $::JOB_PARAMS{'TRAFFIC_DURATION'}, 0);

        General::Logging::log_user_message("Waiting $duration before stopping the traffic.\nThe wait can be interrupted by pressing CTRL-C.\n");
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "progress-interval" => 60,
                "progress-message"  => 1,
                "seconds"           => $::JOB_PARAMS{'TRAFFIC_DURATION'},
                "use-logging"       => 1,
            }
        );
        if ($rc == 1) {
            # CTRL-C pressed, just ignore it
            $rc = 0;
        }

        push @::JOB_STATUS, sprintf "(/) Traffic was running for %s", General::OS_Operations::convert_seconds_to_wdhms( time() - $start_time, 0);

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
    sub Stop_Traffic_P107S05T04 {
        my $rc = 0;

        # Find all background process id's
        my @pids = get_traffic_pids();

        if (scalar @pids > 0) {
            # Now we need to stop all the background processes.
            General::OS_Operations::background_process_kill_all();

            # Then we need to also kill the k6 and dscload processes in the pods.
            if ($::JOB_PARAMS{'SIMULATOR_DSCLOAD_NEEDED'} eq "yes") {
                $rc += kill_dscload_traffic_in_pods();
            }
            if ($::JOB_PARAMS{'SIMULATOR_K6_NEEDED'} eq "yes") {
                $rc += kill_k6_traffic_in_pods();
            }
        } else {
            General::Logging::log_user_message("No background processes found, so no traffic to stop");
        }

        if ($rc != 0) {
            General::Logging::log_user_warning_message(sprintf "%d %s could not be killed, see details above.\nSo you might have to manually stop %s!\n", $rc, $rc == 1 ? ("process", "this process") : ("processes", "these processes"));
        }

        # Always return success
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
sub Collect_Logs_P107S06 {

    my $rc = 0;

    if ($::JOB_PARAMS{'COLLECT_LOGS_AT_SUCCESS'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Collection of logs at successful job not wanted");
    }

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
sub Cleanup_Job_Environment_P107S07 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::902_Cleanup_Job_Environment::main } );
    return $rc if $rc < 0;

    if ($rc == 0 && $::JOB_PARAMS{'JOBSTATUS'} ne "FAILED") {
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
sub Fallback001_P107S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    # Set Job Type again since it was changed inside the 004 playlist
    $::JOB_PARAMS{'JOBTYPE'} = "TRAFFIC_TOOLS_CONFIGURE_AND_START";

    if ($::JOB_PARAMS{'START_BSF_TRAFFIC'} eq "yes" || $::JOB_PARAMS{'START_SCP_TRAFFIC'} eq "yes" || $::JOB_PARAMS{'START_SEPP_TRAFFIC'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Stop_Traffic_P107S05T04 } );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
    return $rc if $rc < 0;

    # Now we need to execute the cleanup.
    General::State_Machine::remove_always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

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
sub check_script_file_for_placeholders_and_copy_to_workspace_if_needed {
    my $input_file = shift;
    my $output_file = $input_file;
    my @data;
    my %placeholders;

    if (-f $input_file) {
        unless (-T $input_file) {
            # The file does not seem to be a text file, maybe it's a binary file.
            # So no need to look for place holders in the file, just use it as is.
            return $input_file;
        }
    } else {
        General::Logging::log_user_error_message("The file does not exist '$input_file'");
        return "";
    }

    my $rc = General::File_Operations::read_file(
        {
            "filename"            => $input_file,
            "output-ref"          => \@data,
        }
    );
    if ($rc != 0) {
        General::Logging::log_user_error_message("Unable to read the contents of file '$input_file'");
        return "";
    }

    # First check if there are any place holders in the file, i.e. <xxxx>
    for (@data) {
        my $line = $_;
        while ($line ne "") {
            if ($line =~ /^.*?<(\S+?)>.*/) {
                # There is at least 1 place holder <xxx>
                if (exists $::JOB_PARAMS{$1}) {
                    $placeholders{$1}++;
                } else {
                    General::Logging::log_user_error_message("Place holder name '$1' in file '$input_file' does not exist in hash \%::JOB_PARAMS, might cause failure to execute the script");
                }
                $line =~ s/^.*?<\S+?>//;
            } else {
                # There are no place holders
                $line = "";
            }
        }
    }

    unless (%placeholders) {
        # No place holders found, just use the input file unchanged
        return $input_file;
    }

    # Next replace place holders in file data
    for my $line (@data) {
        for my $name (keys %placeholders) {
            $line =~ s/<$name>/$::JOB_PARAMS{$name}/g;
        }
    }

    # Write new file into the job workspace directory
    my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/" . File::Basename::basename($input_file);
    $rc = General::File_Operations::write_file(
        {
            "filename"            => $output_file,
            "output-ref"          => \@data,
            "file-access-mode"    => "755",
        }
    );
    if ($rc != 0) {
        General::Logging::log_user_error_message("Unable to write the contents to file '$output_file'");
        return "";
    }

    return $output_file;
}

# -----------------------------------------------------------------------------
sub copy_k6_file_to_pods {
    my $input_file = shift;
    my $pod_file = shift;
    my $failure_cnt = 0;
    my @pod_names;
    my $rc;
    my @result;

    @pod_names = ADP::Kubernetes_Operations::get_pod_names(
        {
            "hide-output"       => 1,
            "kubeconfig"        => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
            "namespace"         => $::JOB_PARAMS{'TOOLS_NAMESPACE'},
            "pod-include-list"  => [ "eric-k6-deployment-" ],
        }
    );
    if (@pod_names) {
        for my $pod_name (@pod_names) {
            General::Logging::log_user_message("Copying file $input_file to K6 Pod file $pod_name:$pod_file");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} cp $input_file $pod_name:$pod_file",
                    "hide-output"   => 1,
                }
            );
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) Copying file $input_file to K6 Pod $pod_name:$pod_file successful";
            } else {
                # display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy the $input_file to pod $pod_name");
                $failure_cnt++;
            }
        }
    } else {
        General::Logging::log_user_error_message("No 'eric-k6-deployment-' pods found");
        return 1;
    }

    if ($failure_cnt == 0) {
        return 0;
    } else {
        return 1;
    }
}

# -----------------------------------------------------------------------------
sub copy_k6_script_to_pods {
    my $input_file = shift;
    my $failure_cnt = 0;
    my @pod_names;
    my $rc;
    my @result;

    @pod_names = ADP::Kubernetes_Operations::get_pod_names(
        {
            "hide-output"       => 1,
            "kubeconfig"        => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
            "namespace"         => $::JOB_PARAMS{'TOOLS_NAMESPACE'},
            "pod-include-list"  => [ "eric-k6-deployment-" ],
        }
    );
    if (@pod_names) {
        for my $pod_name (@pod_names) {
            General::Logging::log_user_message("Copying K6 script $input_file to Pod $pod_name");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} cp $input_file $pod_name:/tests/",
                    "hide-output"   => 1,
                }
            );
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) Copying K6 script $input_file to Pod $pod_name successful";
            } else {
                # display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy the $input_file to pod $pod_name");
                $failure_cnt++;
            }
        }
    } else {
        General::Logging::log_user_error_message("No 'eric-k6-deployment-' pods found");
        return 1;
    }

    if ($failure_cnt == 0) {
        return 0;
    } else {
        return 1;
    }
}

# -----------------------------------------------------------------------------
sub get_traffic_pids {
    my $pid;
    my @pids = ();

    # Find all background process id's
    for my $traffic_type ( "bsf", "scp", "sepp" ) {
        if (exists $::JOB_PARAMS{"PID_K6_$traffic_type"} && $::JOB_PARAMS{"PID_K6_$traffic_type"} ne "") {
            for $pid (split /,/, $::JOB_PARAMS{"PID_K6_$traffic_type"}) {
                push @pids, $pid;
            }
        }
        if (exists $::JOB_PARAMS{"PID_DSCLOAD_$traffic_type"} && $::JOB_PARAMS{"PID_DSCLOAD_$traffic_type"} ne "") {
            for $pid (split /,/, $::JOB_PARAMS{"PID_DSCLOAD_$traffic_type"}) {
                push @pids, $pid;
            }
        }
    }

    return @pids;
}

# -----------------------------------------------------------------------------
sub get_traffic_pids_in_pod {
    my $pod_name = shift;
    my @pids = ();
    my $rc;
    my @result;

    General::Logging::log_user_message("Checking for running traffic in Pod $pod_name");
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} exec $pod_name -- ps -fel",
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );
    if ($rc != 0) {
        # display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_error_message("Failed to fetch traffic process id's in pod $pod_name");
        return ();
    }
    #F S UID         PID   PPID  C PRI  NI ADDR SZ WCHAN  STIME TTY          TIME CMD
    #4 S root          1      0  0  80   0 -   381 hrtime Feb17 ?        00:00:16 tail -f /dev/null
    #4 S root        277      0  7  80   0 - 235501 futex_ 14:44 ?       00:03:58 /usr/bin/k6 run /tests/sepp_stability_test.js -e CERT=RP_206033.crt -e KEY=RP_206033.key -e RPS=1000 -e DURATION=604000s -e MAX_VUS=40 -e SEPP_IP=214.14.14.145 -e SEPP_HOST=sepp1.region1.sepp.5gc.mnc073.mcc262.3gppnetwork.org -e SEPP_PORT=4
    #4 S root        300      0  0  80   0 -   400 do_wai 15:32 pts/0    00:00:00 sh
    #0 R root        311    300  0  80   0 -   406 -      15:37 pts/0    00:00:00 ps -fel
    #
    #    or
    #
    #F S UID         PID   PPID  C PRI  NI ADDR SZ WCHAN  STIME TTY          TIME CMD
    #4 S root          1      0  0  80   0 -  1087 hrtime Feb17 ?        00:00:17 tail -f /dev/null
    #4 S root     140388      0 22  80   0 - 1112420 futex_ 09:08 ?      00:00:08 /opt/dsc-load/dsc_load
    #4 S root     140556      0  0  80   0 -  3551 do_wai 09:09 pts/0    00:00:00 sh
    #0 R root     140574 140556  0  80   0 -  8719 -      09:09 pts/0    00:00:00 ps -fel
    @pids = ();
    for (@result) {
        if (/^\d+\s+\S+\s+\S+\s+(\d+)\s+\d+.+(k6 run|dsc_load).*/) {
            push @pids, $1;
        }
    }

    return @pids;
}

# -----------------------------------------------------------------------------
# Kill a specific PID in a specific POD.
#
# Input:
#   - POD Name
#   - PID
#   - If forced kill (SIGKILL) should be sent (not 0) or normal kill (SIGTERM) should be sent (=0).
#   - Reference to array where failed commands will be stored.
#
# Return:
#   - 0: Process successfully killed
#   - 1: Failure to kill the process, command failed
#   - 2: Process not killed within time limit, process still running.
#
sub kill_pid_in_pod {
    my $pod_name = shift;
    my $pid = shift;
    my $force = shift;
    my $failed_commands = shift;

    my $command;
    my $max_wait = 4;
    my $rc;
    my @result;
    my $stop_time;


    if ($force) {
        General::Logging::log_user_message("  Force stopping traffic pid $pid in Pod $pod_name");
        $command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} exec $pod_name -- kill -9 $pid";
    } else {
        General::Logging::log_user_message("Stopping traffic pid $pid in Pod $pod_name");
        $command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} exec $pod_name -- kill $pid";
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "${debug_command}$command",
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );
    if ($rc != 0) {
        # display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        if (@result && $result[0] =~ /No such process/) {
            # Process probably just killed, ignore this error
            General::Logging::log_user_message("  Traffic pid $pid was already stopped in Pod $pod_name");
            return 0;
        }

        if ($force) {
            General::Logging::log_user_error_message("Failed to force stop traffic pid $pid in pod $pod_name");
        } else {
            General::Logging::log_user_error_message("Failed to stop traffic pid $pid in pod $pod_name");
        }
        push @$failed_commands, $command;
        return 1;
    }

    # Wait max 2 seconds for the
    $stop_time = time() + $max_wait;
    while (time() <= $stop_time) {

        General::Logging::log_user_message("  Checking if traffic pid $pid is still running in Pod $pod_name");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} exec $pod_name -- ps -fl -p $pid",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            # Process id still exists, check that it's still a traffic pid
            my $found = 0;
            for (@result) {
                if (/^\d+\s+\S+\s+\S+\s+$pid\s+\d+.+(k6 run|dsc_load).*/) {
                    $found = 1;
                    last;
                }
            }
            if ($found == 0) {
                # The same pid number must have been taken by some other process
                General::Logging::log_user_message("  Traffic pid $pid was stopped in Pod $pod_name");
                push @::JOB_STATUS, "(/) Traffic Stopped for PID $pid in POD $pod_name";
                return 0;
            }
        } elsif ($rc == 1) {
            # Process does not exist.
            #F S UID         PID   PPID  C PRI  NI ADDR SZ WCHAN  STIME TTY          TIME CMD
            #command terminated with exit code 1
            General::Logging::log_user_message("  Traffic pid $pid was stopped in Pod $pod_name");
            push @::JOB_STATUS, "(/) Traffic Stopped for PID $pid in POD $pod_name";
            return 0;
        }

        General::Logging::log_user_message("  Waiting 1 second for pid $pid to disappear for a maximum of $max_wait seconds");
        General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "progress-message"  => 0,
                "seconds"           => 1,
                "use-logging"       => 1,
            }
        );
    }

    # If we got down here it means the PID did not get killed within the maximum wait time.
    General::Logging::log_user_message("  Pid $pid did not get killed within $max_wait seconds");
    if ($force) {
        push @$failed_commands, $command;
    }
    return 2;
}

# -----------------------------------------------------------------------------
sub kill_traffic_pid_in_pod {
    my $pod_name = shift;
    my @failed_commands = ();   # Updated inside the kill_pid_in_pod subroutine
    my @pids;
    my $rc;

    @pids = get_traffic_pids_in_pod($pod_name);

    if (scalar @pids == 0) {
        General::Logging::log_user_message("   There are no running traffic pid's in Pod $pod_name, so nothing to stop");
        return 0;
    }

    for my $pid (@pids) {
        # First try a normal kill (0).
        $rc = kill_pid_in_pod($pod_name, $pid, 0, \@failed_commands);
        if ($rc == 0) {
            # Process was killed
            next;
        } elsif ($rc == 2) {
            # The process was not killed within max wait time, try a forced kill (1).
            $rc = kill_pid_in_pod($pod_name, $pid, 1, \@failed_commands);
            if ($rc == 0) {
                # Process was killed
                next;
            } else {
                # Process could not be killed
                next;
            }
        } else {
            # Process could not be killed
            next;
        }
    }

    if (scalar @failed_commands == 0) {
        return 0;
    } else {
        General::Logging::log_user_error_message("Failed to stop the following traffic pid's in Pod $pod_name so they must be manually killed:\n" . (join "\n", @failed_commands));
        return 1;
    }
}

# -----------------------------------------------------------------------------
sub kill_dscload_traffic_in_pods {
    my $failure_cnt = 0;
    my @pod_names;
    my $rc;

    @pod_names = ADP::Kubernetes_Operations::get_pod_names(
        {
            "hide-output"       => 1,
            "kubeconfig"        => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
            "namespace"         => $::JOB_PARAMS{'TOOLS_NAMESPACE'},
            "pod-include-list"  => [ "eric-dscload-deployment-" ],
        }
    );
    if (@pod_names) {
        for my $pod_name (@pod_names) {
            $rc = kill_traffic_pid_in_pod($pod_name);
            if ($rc == 1) {
                $failure_cnt++;
            }
        }
    } else {
        General::Logging::log_user_error_message("No 'eric-dscload-deployment-' pods found");
        return 1;
    }

    if ($failure_cnt == 0) {
        return 0;
    } else {
        return 1;
    }
}

# -----------------------------------------------------------------------------
sub kill_k6_traffic_in_pods {
    my $failure_cnt = 0;
    my @pod_names;
    my $rc;

    @pod_names = ADP::Kubernetes_Operations::get_pod_names(
        {
            "hide-output"       => 1,
            "kubeconfig"        => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
            "namespace"         => $::JOB_PARAMS{'TOOLS_NAMESPACE'},
            "pod-include-list"  => [ "eric-k6-deployment-" ],
        }
    );
    if (@pod_names) {
        for my $pod_name (@pod_names) {
            $rc = kill_traffic_pid_in_pod($pod_name);
            if ($rc == 1) {
                $failure_cnt++;
            }
        }
    } else {
        General::Logging::log_user_error_message("No 'eric-k6-deployment-' pods found");
        return 1;
    }

    if ($failure_cnt == 0) {
        return 0;
    } else {
        return 1;
    }
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
# This subroutine takes a variable value containing one or more IP-addresses
# separated by a comma and returns the first address of the wanted type and
# adding if wanted [ ] surrounding an IPv6 address.
#
sub return_one_ip_address {
    my $var_value = shift;  # One or more IP-addresses
                            # For example: 10.117.41.213,2001:1b74:20:c003::0213
                            #              10.117.41.213
                            #              2001:1b74:20:c003::0213
    my $prefer = shift;     # (IPV4|IPV6)
    my $add_brakets = shift;# (no|yes) Add [ ] around IPv6 address

    my $quote = "";
    if ($var_value =~ /^(['"])(.*)['"]$/) {
        # The value is surrounded by single (') or double (") quotes
        $quote = $1;
        $var_value = $2;
    }
    my @ipv4 = ();
    my @ipv6 = ();
    my @ip = split /,/, $var_value;
    for (@ip) {
        if (/:/) {
            push @ipv6, $_;
        } else {
            push @ipv4, $_;
        }
    }
    if (@ipv4 && @ipv6) {
        # We have both ipv4 and ipv6 addresses, we need to pick one of them
        if ($prefer =~ /^IPV4$/i) {
            $var_value = sprintf "%s%s%s", $quote, $ipv4[0], $quote;
        } else {
            if ($add_brakets =~ /^(no|0)$/i) {
                $var_value = sprintf "%s%s%s", $quote, $ipv6[0], $quote;
            } else {
                $var_value = sprintf "%s[%s]%s", $quote, $ipv6[0], $quote;
            }
        }
    } elsif (@ipv4) {
        $var_value = sprintf "%s%s%s", $quote, $ipv4[0], $quote;
    } elsif (@ipv6) {
        if ($add_brakets =~ /^(no|0)$/i) {
            $var_value = sprintf "%s%s%s", $quote, $ipv6[0], $quote;
        } else {
            $var_value = sprintf "%s[%s]%s", $quote, $ipv6[0], $quote;
        }
    }

    return $var_value;
}

# -----------------------------------------------------------------------------
sub set_playlist_variables {
    %playlist_variables = (
        # ---------------------------------------------------------------------
        'BASE_DIR' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",       # <yes|no>
            'type'          => "directory", # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the base directory that holds all the scripts and configuration file
templates to be used by the playlist.
This parameter only needs to be specified if the TRAFFIC_CONFIG_FILE is specified
and the directory where this file is stored DOES NOT contain the sub-directories
traffic_config and traffic_scripts which stores the needed files.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'BSF_BASH_COMMAND_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if a file that contains bash commands (or in fact any
executable file) that will be executed.
The file specified here will be loaded when TRAFFIC_TO_CONFIGURE contains 'bsf'.

If multiple files are needed then just append the name with a string, for
example BSF_BASH_COMMAND_FILE_01.

If a file is not specified then no extra commands are needed for this traffic
type.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'BSF_CONFIG_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls a file that contains BSF configuration data to be loaded
into CMYP cli or netconf interface dependent on the file extention (.cli or .netconf).
The file specified here will be loaded when TRAFFIC_TO_CONFIGURE contains 'bsf'.

If multiple config files are needed then just append the name with a string, for
example BSF_CONFIG_FILE_01.

If a file is not specified then no configuration data for BSF is loaded and it's
up to the user to have manually loaded this data before calling this playlist.

NOTE:
If the file should be loaded using a specific user and password then the
file to load must follow a specific naming convention:

<filename prefix>,user=<username>[,password=<password>].<cli|netconf>

    For example: -v BSF_DATA_FILE=/path/bsf_config,user=bsf-admin.netconf

If a generic user should be used for all files then no special naming convention
is needed, instead the user name and password is read from the job parameters
CONFIG_USER_NAME and CONFIG_USER_PASSWORD and if not specified then the user
'expert' will be used.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'BSF_DSCLOAD_LOAD_SESSION_BINDINGS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies if the playlist should load session binding (=yes) or
if the user will do this manually (=no).
If the value is 'yes' then the following data will be loaded from the director
node:
  - export BINDING_IPV4='{"supi":"imsi-12345","ipv4Addr":"10.0.0.1","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf-diamhost.com","pcfDiamRealm":"pcf-diamrealm.com","snssai":{"sst":2,"sd":"DEADF0"}}'
  - export NODE_IP=\$( kubectl get nodes -o jsonpath="{.items[0].status.addresses[0].address}" --selector='node-role.kubernetes.io/worker')
  - export BSFWRK_PORT=\$( kubectl get svc --namespace eiffelesc eric-bsf-worker -o jsonpath="{.spec.ports[0].nodePort}")
  - curl -v -d "\$BINDING_IPV4" -H "Content-Type: application/json" -X POST "http://\$NODE_IP:\$BSFWRK_PORT/nbsf-management/v1/pcfBindings"
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'BSF_DSCLOAD_TRAFFIC_COMMAND' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the command to use for starting the DSCLOAD tool from
inside the dscload pod.
If not specified then the following default command will be used:
    /opt/dsc-load/dsc_load
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'BSF_FETCH_SESSION_BINDING_DATA' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies if the playlist should fetch session binding data before
clearing the data.

If the value is 'no' then no session binding data is fetched before clearing the
database.
Since fetching all session binding data can take quite a while and also return
back a lot of data if the traffic has been running for a while, it is better
to leave this value set to 'no' unless you really need this data.

If the value is 'yes' then all session binding data is fetched before the
database is cleared.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'BSF_K6_SCRIPT_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the K6 script file that will be uploaded to the k6 pod
and contains the logic for running the BSF traffic and it should also match with
the script specified in the BSF_K6_TRAFFIC_COMMAND command.

If multiple script files are needed then just append the name with a string, for
example SEPP_K6_SCRIPT_FILE_01.

If not specified then no script file will be uploaded to the k6 pod and it
is the responsibility of the user to make sure the needed script already exist
inside the k6 pod.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'BSF_K6_TRAFFIC_COMMAND' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the command to use for starting the K6 tool from
inside the k6 pod.
If multiple commands is needed then just append the name with a string, for
example SEPP_K6_TRAFFIC_COMMAND_01.

If not specified then K6 BSF traffic will not be started.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CHECK_TRAFFIC_DELAY' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "180",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls how long to wait (in seconds) after traffic has been
started before checking if the traffic success rate is within acceptable levels.
This parameter is only used when CHECK_TRAFFIC_SUCCESS_RATE=yes.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CHECK_TRAFFIC_SUCCESS_RATE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if a check of the success rate of the traffic should be
done (=yes) or not (=no) after starting the traffic.
If the success rate is not within the accepted limits then then the traffic will
be stopped again and the playlist will fail.
See also CHECK_TRAFFIC_SUCCESS_RATE_DURATION, CHECK_TRAFFIC_DELAY and
IGNORE_FAILED_TRAFFIC_SUCCESS_RATE_CHECK.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CHECK_TRAFFIC_SUCCESS_RATE_DURATION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "30",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls how long to time to sample (in seconds) the traffic to
determine if the success rate is within acceptable levels.
This parameter is only used when CHECK_TRAFFIC_SUCCESS_RATE=yes.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'COLLECT_LOGS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the steps and tasks inside the playlist 914_Collect_Logs
will be executed or not.

Select "yes" which is also the default value if not specified, to always execute
the steps and tasks inside the 914_Collect_Logs playlist when it's being called.
Setting it to "yes" does not mean that logs will always be collected, it just
means that they will be collected if the playlist is called. I.e. setting
SKIP_COLLECT_LOGS=yes will skip calling the playlist for the normal cases.

Select "no", to skip all steps and tasks inside the playlist 914_Collect_Logs, i.e.
to not collect any log information done by this playlist.

This parameter has higher priority than for example SKIP_COLLECT_LOGS which only
affects normal log collection and not log collection done when doing fallback,
which is always collected if the main playlist has the logic to call the
914_Collect_Logs playlist.
I.e. if COLLECT_LOGS=no then logs will not be collected at all by the playlist
914_Collect_Logs no matter if the playlist was successful or ended up in a
fallback. This can for example be useful if you know that a playlist execution
will fail and you are not interested in the logs collected by the 914_Collect_Logs
playlist which could save you 15 minutes or more from collecting the logs.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

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
logs and other information will be collected and stored in the all.log or under
the troubleshooting_logs sub directory of the job.
The default value is 'no' if the variable is not specified.

If value is 'no' then no collection of detailed logs will be done at successful
job termination.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_DATA_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter is needed by the 004_Config_Management.pm playlist but will be
initialized with values from the BSF_CONFIG_FILE, SCP_CONFIG_FILE or
SEPP_CONFIG_FILE parameters and it's only specified here to override the
mandatory attribute.
Any value assigned to this parameter by the user will be ignored.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DEFAULT_TRAFFIC_CONFIG_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "base_stability_traffic_bsf_scp_sepp_model_01.config",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter is used to change the default traffic configuration file that will
be used if the TRAFFIC_CONFIG_FILE is not specified or not found and the variable
USE_BUILT_IN_FILES_AT_MISSING_TRAFFIC_CONFIG_FILE=yes.
This file must exist in the following DAFT package template directory:
$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/base_stability_traffic/
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DISCARD_TRAFFIC_SIMULATOR_LOGS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the traffic simulator logs should be discarded (=yes)
or not (=no), the default is that the logs will not be discared but saved to files
in the workspace directory.

Sometimes for very long running traffic it can result in very large files e.g.
>20GB when run for 5 days and then it might be better to not save log logs, so
instead of redirecting the tool output to file it will be sent to /dev/null and
discarded instead.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'FORCE_LOAD_CONFIGURATION' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the loading of configuration data should be done even
if all pod's are already up (indicating that DT is already loaded).

By default if the value of this parameter is 'no' and all pods's are up then the
loading of configuration data is skipped..

If the value of this parameter is 'yes' then the configuration data is loaded
regardless if all pod's are up or not.
This can e.g. be needed if the tools deployment are left even though the SC
deployment has been undeployed and deployed again and you want to start traffic
on the new deployment. Especially for SEPP traffic the DT has seppsim port numbers
which needs to be adapted for a new SC deployment.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'IGNORE_FAILED_TRAFFIC_SUCCESS_RATE_CHECK' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if a failed traffic success rate check shall be ignored
(=yes) or not (=no).
This parameter is only used when CHECK_TRAFFIC_SUCCESS_RATE=yes.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'KPI_COLLECTION_RESOLUTION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "15",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the amount of time in seconds between each KPI statistics.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'KPI_SUCCESS_RATE_THRESHOLD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "95",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified and set to an integer value then this is limit to use to check if the
success rate is below this limit which would cause the check to fail.
If not specified then the default value will be used.
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
kubectl commands directed towards the SC deployment.
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
        'PREFER_IP_VERSION' => {
            'case_check'    => "uppercase", # <lowercase|no|uppercase>
            'default_value' => "IPV4",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which IP-address is used when both IPv4 and IPv6 address
is used in the node.
EOF
            'validity_mask' => '(IPV4|IPV6)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SC_NAMESPACE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The name to use for the SC namespace if you want to override the parameter from
the network configuration file called 'sc_namespace'.
If you don't specify this parameter then it will take the value from the network
configuration file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SC_RELEASE_NAME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The name to use for the SC release name if you want to override the parameter
from the network configuration file called 'sc_release_name'.
If you don't specify this parameter then it will take the value from the network
configuration file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SCP_BASH_COMMAND_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if a file that contains bash commands (or in fact any
executable file) that will be executed.
The file specified here will be loaded when TRAFFIC_TO_CONFIGURE contains 'scp'.

If multiple files are needed then just append the name with a string, for
example SCP_BASH_COMMAND_FILE_01.

If a file is not specified then no extra commands are needed for this traffic
type.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SCP_CONFIG_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls a file that contains SCP configuration data to be loaded
into CMYP cli or netconf interface dependent on the file extention (.cli or .netconf).
The file specified here will be loaded when TRAFFIC_TO_CONFIGURE contains 'scp'.

If multiple config files are needed then just append the name with a string, for
example SCP_CONFIG_FILE_01.

If a file is not specified then no configuration data for SCP is loaded and it's
up to the user to have manually loaded this data before calling this playlist.

NOTE:
If the file should be loaded using a specific user and password then the
file to load must follow a specific naming convention:

<filename prefix>,user=<username>[,password=<password>].<cli|netconf>

    For example: -v SCP_DATA_FILE=/path/scp_config,user=scp-admin.netconf

If a generic user should be used for all files then no special naming convention
is needed, instead the user name and password is read from the job parameters
CONFIG_USER_NAME and CONFIG_USER_PASSWORD and if not specified then the user
'expert' will be used.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SCP_K6_SCRIPT_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the K6 script file that will be uploaded to the k6 pod
and contains the logic for running the SCP traffic and it should also match with
the script specified in the SCP_K6_TRAFFIC_COMMAND command.

If multiple script files are needed then just append the name with a string, for
example SCP_K6_SCRIPT_FILE_01.

If not specified then no script file will be uploaded to the k6 pod and it
is the responsibility of the user to make sure the needed script already exist
inside the k6 pod.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SCP_K6_TRAFFIC_COMMAND' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the command to use for starting the K6 tool from
inside the k6 pod.
If multiple commands is needed then just append the name with a string, for
example SCP_K6_TRAFFIC_COMMAND_01.

If not specified then K6 SCP traffic will not be started.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SEPP_BASH_COMMAND_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if a file that contains bash commands (or in fact any
executable file) that will be executed.
The file specified here will be loaded when TRAFFIC_TO_CONFIGURE contains 'sepp'.

If multiple files are needed then just append the name with a string, for
example SEPP_BASH_COMMAND_FILE_01.

If a file is not specified then no extra commands are needed for this traffic
type.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SEPP_CONFIG_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls a file that contains SEPP configuration data to be loaded
into CMYP cli or netconf interface dependent on the file extention (.cli or .netconf).
The file specified here will be loaded when TRAFFIC_TO_CONFIGURE contains 'sepp'.

If multiple config files are needed then just append the name with a string, for
example SEPP_CONFIG_FILE_01.

If a file is not specified then no configuration data for SEPP is loaded and it's
up to the user to have manually loaded this data before calling this playlist.

NOTE:
If the file should be loaded using a specific user and password then the
file to load must follow a specific naming convention:

<filename prefix>,user=<username>[,password=<password>].<cli|netconf>

    For example: -v SEPP_DATA_FILE=/path/sepp_config,user=sepp-admin.netconf

If a generic user should be used for all files then no special naming convention
is needed, instead the user name and password is read from the job parameters
CONFIG_USER_NAME and CONFIG_USER_PASSWORD and if not specified then the user
'expert' will be used.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SEPP_K6_SCRIPT_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the K6 script file that will be uploaded to the k6 pod
and contains the logic for running the SEPP traffic and it should also match with
the script specified in the SEPP_K6_TRAFFIC_COMMAND command.
If multiple script files are needed then just append the name with a string, for
example SEPP_K6_SCRIPT_FILE_01.

If not specified then no script file will be uploaded to the k6 pod and it
is the responsibility of the user to make sure the needed script already exist
inside the k6 pod.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SEPP_K6_TRAFFIC_COMMAND' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the command to use for starting the K6 tool from
inside the k6 pod.
If multiple commands is needed then just append the name with a string, for
example SEPP_K6_TRAFFIC_COMMAND_01.

If not specified then K6 SEPP traffic will not be started.
EOF
            'validity_mask' => '.+',
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
        'TRAFFIC_CONFIG_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to specify a file that contains job parameters that
will be set with values from this file.
This file will only set job parameters if they have not specifically been set by
the user on the command line, which have precidence over any variables set in the file.

The file can for example contain the following definitions:

    BSF_BASH_COMMAND=<value>
    BSF_BASH_COMMAND_n=<value> (where is n is any number or other string)
    BSF_CONFIG_FILE=<value>
    BSF_CONFIG_FILE_n=<value> (where is n is any number or other string)
    BSF_DSCLOAD_LOAD_SESSION_BINDINGS=<yes|no>
    BSF_DSCLOAD_TRAFFIC_COMMAND=<value>
    BSF_FETCH_SESSION_BINDING_DATA=<yes|no>
    BSF_K6_SCRIPT_FILE=<value>
    BSF_K6_SCRIPT_FILE_n=<value> (where is n is any number or other string)
    BSF_K6_TRAFFIC_COMMAND=<value>
    BSF_K6_TRAFFIC_COMMAND_n=<value> (where is n is any number or other string)

    SCP_BASH_COMMAND=<value>
    SCP_BASH_COMMAND_n=<value> (where is n is any number or other string)
    SCP_CONFIG_FILE=<value>
    SCP_CONFIG_FILE_n=<value> (where is n is any number or other string)
    SCP_K6_SCRIPT_FILE=<value>
    SCP_K6_SCRIPT_FILE_n=<value> (where is n is any number or other string)
    SCP_K6_TRAFFIC_COMMAND=<value>
    SCP_K6_TRAFFIC_COMMAND_n=<value> (where is n is any number or other string)

    SEPP_BASH_COMMAND=<value>
    SEPP_BASH_COMMAND_n=<value> (where is n is any number or other string)
    SEPP_CONFIG_FILE=<value>
    SEPP_CONFIG_FILE_n=<value> (where is n is any number or other string)
    SEPP_K6_SCRIPT_FILE=<value>
    SEPP_K6_SCRIPT_FILE_n=<value> (where is n is any number or other string)
    SEPP_K6_TRAFFIC_COMMAND=<value>
    SEPP_K6_TRAFFIC_COMMAND_n=<value> (where is n is any number or other string)

The <value> part can also contain one or more place holders which will be
replaced by the real playlist variable values from %::JOB_PARAMS hash, for example:

    <SC_NAMESPACE> which is replaced by the value of variable \$::JOB_PARAMS{'SC_NAMESPACE'}.
    <TRAFFIC_DURATION> which if 'indefinite' will be replaced by the value 630720000.

See also BASE_DIR variable above.
For more information about the variables see individual variables above.

If the parameter is not specified or if the file is not found then a check is done on the
variable USE_BUILT_IN_FILES_AT_MISSING_TRAFFIC_CONFIG_FILE and if 'yes' then the default
built-in file is used as specified by DEFAULT_TRAFFIC_CONFIG_FILE.
And if 'no' then the playlist fails.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TRAFFIC_DURATION' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "3600",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls how long the traffic will run before being stopped.

It should either be specified as an integer value of how many seconds the traffic
will run or the value 'indefinite' if the user will manually stop the traffic
in which case this playlist will finish and let the background jobs continue.

Example values:

    - indefinite     (in which case the K6 Duration will be set to 630720000
                      seconds which is the same as 7300 days or roughly 20 years
                      which can be used when e.g. testing time shift
                      NOTE: This will also terminate the playlist and run the
                      traffic in the background and they need to be manually
                      stopped when wanted)
    - 6048000        (which is the same as 70 days)
    - 1209600        (which is the same as 14 days)
    - 604800         (which is the same as 7 days)
    - 259200         (which is the same as 3 days)
    - 226800         (which is the same as 2 days and 15 hours)
    - 86400          (which is the same as 1 day or 24 hours)
    - 28800          (which is the same as 8 hours)
    - 14400          (which is the same as 4 hours)
    - 10800          (which is the same as 3 hours)
    - 7200           (which is the same as 2 hours)
    - 3600           (which is the same as 1 hour, which is also the default value)
    - 2700           (which is the same as 45 minutes)
    - 1800           (which is the same as 30 minutes)
    - 900            (which is the same as 15 minutes)
    - 600            (which is the same as 10 minutes)
    - 300            (which is the same as 5 minutes)
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TRAFFIC_TO_CONFIGURE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which traffic types to configure on the node to prepare
it for running traffic which means that needed configuration data will be loaded
into the CM Yang Provider CLI via netconf and needed traffic scripts will be
uploaded to the k6 traffic simulator.

The needed traffic tools must have already been installed in the namespace
specified by TOOLS_NAMESPACE e.g. by running the playlist
105_Traffic_Tools_Build_And_Install.pm.

Multiple traffic types can be specified by separating each with a space or comma.

Currently allowed traffic types are:

    - automatic    Automatically select which traffic to configure which is based
                   on logic that checks which software is loaded and not yet
                   configured.
                   If this is selected then no other types are allowed.
    - bsf          (which requires that the dscload, k6 and nrfsim tools has
                    already been installed on the node).
    - scp          (which requires that the k6 and nrfsim tools has already been
                    installed on the node).
    - sepp         (which requires that the seppsim, k6 and nrfsim tools has
                    already been installed on the node).

If not specified but TRAFFIC_TO_START is specified then the node must already
have been configured.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TRAFFIC_TO_START' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which traffic types to start running on the node.

The needed traffic tools must have already been installed and configured in the
namespace specified by TOOLS_NAMESPACE e.g. by running the playlist
105_Traffic_Tools_Build_And_Install.pm and by already have configured them using
job variable TRAFFIC_TO_CONFIGURE above.

Multiple traffic types can be specified by separating each with a space or comma
but it might only be realistic from a capacity point of view to run one traffic
type at the time.

Currently allowed traffic types are:

    - automatic    Automatically select which traffic to start which is based
                   on logic that checks which traffic generators are installed
                   and which software is loaded and configured.
                   If this is selected then no other types are allowed.
    - bsf          (which requires that the dscload, k6 and nrfsim tools has
                    already been installed and configured on the node).
    - scp          (which requires that the k6 and nrfsim tools has already been
                    installed and configured on the node).
    - sepp         (which requires that the seppsim, k6 and nrfsim tools has
                    already been installed and configured on the node).

If not specified then no traffic will be started.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'USE_BUILT_IN_FILES_AT_MISSING_TRAFFIC_CONFIG_FILE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies if the playlist should use the built-in parameters
and files if the TRAFFIC_CONFIG_FILE file is missing.

See also TRAFFIC_CONFIG_FILE.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },
    )
}

# -----------------------------------------------------------------------------
sub start_dscload_traffic_in_pods {
    my $traffic_type = shift;
    my $command = shift;
    my $failure_cnt = 0;
    my $file_cnt = 0;
    my $log_stderr_file;
    my $log_stdout_file;
    my $pid;
    my @pod_names;
    my $rc;
    my @result;
    my $script_file;
    my $message = "";
    my @pod_names;
    my $rc;
    my @result;

    @pod_names = ADP::Kubernetes_Operations::get_pod_names(
        {
            "hide-output"       => 1,
            "kubeconfig"        => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
            "namespace"         => $::JOB_PARAMS{'TOOLS_NAMESPACE'},
            "pod-include-list"  => [ "eric-dscload-deployment-" ],
        }
    );
    if (@pod_names) {
        $message = "If you need to manually start the traffic, execute the following commands:\n";
        for my $pod_name (@pod_names) {
            if ($file_cnt == 0) {
                $script_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/${traffic_type}_dscload_start_traffic.bash";
                $log_stderr_file = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/${traffic_type}_dscload_traffic_stderr.log";
                $log_stdout_file = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/${traffic_type}_dscload_traffic_stdout.log";
            } else {
                $script_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/${traffic_type}_dscload_start_traffic_$file_cnt.bash";
                $log_stderr_file = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/${traffic_type}_dscload_traffic_stderr_$file_cnt.log";
                $log_stdout_file = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/${traffic_type}_dscload_traffic_stdout_$file_cnt.log";
            }
            if ($::JOB_PARAMS{'DISCARD_TRAFFIC_SIMULATOR_LOGS'} eq "yes") {
                $log_stderr_file = "/dev/null";
                $log_stdout_file = "/dev/null";
            }
            $rc = General::File_Operations::write_file(
                {
                    "filename"          => $script_file,
                    "output-ref"        => [ "$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} exec $pod_name -- $command" ],
                    "file-access-mode"  => "777",
                }
            );
            if ($rc == 0) {
                $message .= "$script_file  > $log_stdout_file 2> $log_stderr_file &\n";

                if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
                    General::Logging::log_user_message("Background process for dscload $traffic_type traffic NOT STARTED because DEBUG_PLAYLIST=yes");
                    push @::JOB_STATUS, "(-) Background process for dscload $traffic_type traffic NOT STARTED because DEBUG_PLAYLIST=yes";
                    $file_cnt++;
                    next;
                }
                General::Logging::log_user_message("Starting background process for dscload $traffic_type traffic");
                $pid = General::OS_Operations::background_process_run("$script_file  > $log_stdout_file 2> $log_stderr_file");
                $::JOB_PARAMS{"PID_DSCLOAD_$traffic_type"} .= "$pid,";
                if ($pid > 0) {
                    General::Logging::log_user_message("Process Id: $pid");
                    $::JOB_PARAMS{'TRAFFIC_RUNNING_IN_BACKGROUND'} = "yes";
                    push @::JOB_STATUS, "(/) DSCLOAD $traffic_type Traffic Started with PID $pid in POD $pod_name";
                } else {
                    General::Logging::log_user_error_message("Failed to start background process");
                    $failure_cnt++;
                }
            } else {
                General::Logging::log_user_error_message("Failed to write the file: $script_file");
                $failure_cnt++;
            }
            $file_cnt++;
        }
        General::Logging::log_user_message($message);
    } else {
        General::Logging::log_user_error_message("No 'eric-dscload-deployment-' pods found");
        return 1;
    }

    if ($failure_cnt == 0) {
        return 0;
    } else {
        return 1;
    }
}

# -----------------------------------------------------------------------------
sub start_k6_traffic_in_pods {
    my $traffic_type = shift;
    my $command = shift;
    my $failure_cnt = 0;
    my $log_stderr_file;
    my $log_stdout_file;
    my $message = "";
    my $pid;
    my $pod_name;
    my @pod_names;
    my $rc;
    my @result;
    my $script_file;

    $::JOB_PARAMS{"PID_K6_$traffic_type"} = "";

    @pod_names = ADP::Kubernetes_Operations::get_pod_names(
        {
            "hide-output"       => 1,
            "kubeconfig"        => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
            "namespace"         => $::JOB_PARAMS{'TOOLS_NAMESPACE'},
            "pod-include-list"  => [ "eric-k6-deployment-" ],
        }
    );
    if (@pod_names) {
        if ($traffic_type =~ /^bsf/) {
            $pod_name = $pod_names[0];
        } elsif ($traffic_type =~ /^scp/) {
            if (scalar @pod_names == 1) {
                $pod_name = $pod_names[0];
            } elsif (scalar @pod_names > 1) {
                $pod_name = $pod_names[1];
            }
        } elsif ($traffic_type =~ /^sepp/) {
            if (scalar @pod_names == 1) {
                $pod_name = $pod_names[0];
            } elsif (scalar @pod_names == 2) {
                $pod_name = $pod_names[1];
            } elsif (scalar @pod_names > 2) {
                $pod_name = $pod_names[2];
            }
        }

        $script_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/${traffic_type}_k6_start_traffic.bash";
        if ($::JOB_PARAMS{'DISCARD_TRAFFIC_SIMULATOR_LOGS'} eq "no") {
            $log_stderr_file = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/${traffic_type}_k6_traffic_stderr.log";
            $log_stdout_file = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/${traffic_type}_k6_traffic_stdout.log";
        } else {
            $log_stderr_file = "/dev/null";
            $log_stdout_file = "/dev/null";
        }

        $message = "If you need to manually start the traffic, execute the following command:\n";
        $message .= "$script_file  > $log_stdout_file 2> $log_stderr_file &\n";
        General::Logging::log_user_message($message);

        $rc = General::File_Operations::write_file(
            {
                "filename"          => $script_file,
                "output-ref"        => [ "$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} exec $pod_name -- $command" ],
                "file-access-mode"  => "777",
            }
        );
        if ($rc == 0) {
            # Replace any <xxxx> place holder in the file with values from the %::JOB_PARAMS hash, write the contents back to the same file
            $rc = &Playlist::938_Node_Information::replace_place_holders_in_file($script_file, $script_file, "777" );
            return $rc if $rc < 0;

            if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "no") {
                General::Logging::log_user_message("Starting background process for K6 $traffic_type traffic");
                $pid = General::OS_Operations::background_process_run("$script_file  > $log_stdout_file 2> $log_stderr_file");
                $::JOB_PARAMS{"PID_K6_$traffic_type"} .= "$pid,";
                if ($pid > 0) {
                    General::Logging::log_user_message("Process Id: $pid");
                    $::JOB_PARAMS{'TRAFFIC_RUNNING_IN_BACKGROUND'} = "yes";
                    push @::JOB_STATUS, "(/) K6 $traffic_type Traffic Started with PID $pid in POD $pod_name";
                } else {
                    General::Logging::log_user_error_message("Failed to start background process");
                    $failure_cnt++;
                }
            } else {
                General::Logging::log_user_message("Background process for K6 $traffic_type traffic NOT STARTED because DEBUG_PLAYLIST=yes");
                push @::JOB_STATUS, "(-) Background process for K6 $traffic_type traffic NOT STARTED because DEBUG_PLAYLIST=yes";
            }
        } else {
            General::Logging::log_user_error_message("Failed to write the file: $script_file");
            $failure_cnt++;
        }

        $::JOB_PARAMS{"PID_K6_$traffic_type"} =~ s/,$//g;
    } else {
        General::Logging::log_user_error_message("No 'eric-k6-deployment-' pods found");
        return 1;
    }

    if ($failure_cnt == 0) {
        return 0;
    } else {
        return 1;
    }
}

# -----------------------------------------------------------------------------
sub update_k6_script_and_copy_to_workspace {
    my $input_file = shift;
    my $output_file;
    my $rc;
    my @result;

    if (-f "$input_file") {
        $input_file = abs_path $input_file;
    } else {
        General::Logging::log_user_error_message("The K6 script file does not exist: $input_file");
        return "";
    }
    if ($input_file =~ /^.+\/(.+)/) {
        $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/$1";
    } else {
        $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/$input_file";
    }

    General::Logging::log_user_message("Reading K6 script file: $input_file");
    $rc = General::File_Operations::read_file(
        {
            "filename"            => $input_file,
            "output-ref"          => \@result,
        }
    );
    if ($rc != 0) {
        General::Logging::log_user_error_message("Failed to read the K6 script file: $input_file");
        return "";
    }

    # Now replace any place holders in the file data
    for (@result) {
        if (/<TOOLS_NAMESPACE>/) {
            s/<TOOLS_NAMESPACE>/$::JOB_PARAMS{'TOOLS_NAMESPACE'}/g;
        }
        if (/<(SEPPSIM_P[1-8]_NODEPORT)>/) {
            my $name = $1;
            s/<$name>/$::JOB_PARAMS{$name}/g;
        }
    }

    General::Logging::log_user_message("Writing K6 script file: $output_file");
    $rc = General::File_Operations::write_file(
        {
            "filename"            => $output_file,
            "output-ref"          => \@result,
        }
    );
    if ($rc != 0) {
        General::Logging::log_user_error_message("Failed to write the K6 script file: $output_file");
        return "";
    }

    return $output_file;
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist configures and starts the dscload, k6, nrfsim and seppsim traffic
generators tools on the IaaS clusters to generate traffic.

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
    my %temp_hash = ( %Playlist::004_Config_Management::playlist_variables, %playlist_variables );

    General::Playlist_Operations::print_info_about_job_variables(\%temp_hash);

    $message = "# Global variable access in playlist $playlist_name #";
    $length = length($message);
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables(__FILE__, \%playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);

    no strict;
    for $playlist_name ("004_Config_Management") {
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
    return ( %Playlist::004_Config_Management::playlist_variables, %playlist_variables );
}

1;
