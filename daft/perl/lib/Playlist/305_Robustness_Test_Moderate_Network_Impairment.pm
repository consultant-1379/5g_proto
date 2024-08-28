package Playlist::305_Robustness_Test_Moderate_Network_Impairment;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.19
#  Date     : 2023-12-13 15:46:16
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021-2022
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

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    parse_network_config_parameters
    usage
    usage_return_playlist_variables
    );

use ADP::Kubernetes_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;
use Playlist::932_Test_Case_Common_Logic;

#
# Variable Declarations
#
my %playlist_variables;
set_playlist_variables();

my $debug_command = "";         # Change this value from "echo " to "" when ready to test on real system

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

    # Set Job Type
    $::JOB_PARAMS{'JOBTYPE'} = "ROBUSTNESS_TEST_MODERATE_NETWORK_IMPAIRMENT";

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Test_Checks_P305S01, \&Fallback001_P305S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Test_Case_P305S02, \&Fallback001_P305S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Checks_P305S03, \&Fallback001_P305S99 );
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
sub Perform_Pre_Test_Checks_P305S01 {

    my $rc;

    # This first call will initialize job environment and perform pre test checks
    $::JOB_PARAMS{'P932_TASK'} = "PRE_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_TOOLS_NAMESPACE_P305S01T01 } );
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
    sub Check_TOOLS_NAMESPACE_P305S01T01 {
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
#
# -----------------------------------------------------------------------------
sub Perform_Test_Case_P305S02 {

    my $length;
    my $message;
    my $rc;
    my @values = split /[,|]/, $::JOB_PARAMS{'IMPAIRMENT_DELAY_VALUES'};

    # Set start value either to 0 if never executed or to value of P305_IMPAIRMENT_CNT job variable
    # if job has been interrupted and rerun.
    $::JOB_PARAMS{'P305_IMPAIRMENT_CNT'}       = exists $::JOB_PARAMS{'P305_IMPAIRMENT_CNT'}       ? $::JOB_PARAMS{'P305_IMPAIRMENT_CNT'}       : 0;
    $::JOB_PARAMS{'P305_EXECUTE_P305S02T01'}   = exists $::JOB_PARAMS{'P305_EXECUTE_P305S02T01'}   ? $::JOB_PARAMS{'P305_EXECUTE_P305S02T01'}   : 1;
    $::JOB_PARAMS{'P305_EXECUTE_P305S02T02'}   = exists $::JOB_PARAMS{'P305_EXECUTE_P305S02T02'}   ? $::JOB_PARAMS{'P305_EXECUTE_P305S02T02'}   : 1;
    $::JOB_PARAMS{'P305_EXECUTE_P305S02T03'}   = exists $::JOB_PARAMS{'P305_EXECUTE_P305S02T03'}   ? $::JOB_PARAMS{'P305_EXECUTE_P305S02T03'}   : 1;
    $::JOB_PARAMS{'P305_EXECUTE_P305S02T04'}   = exists $::JOB_PARAMS{'P305_EXECUTE_P305S02T04'}   ? $::JOB_PARAMS{'P305_EXECUTE_P305S02T04'}   : 1;
    $::JOB_PARAMS{'P305_EXECUTE_P305S02T05'}   = exists $::JOB_PARAMS{'P305_EXECUTE_P305S02T05'}   ? $::JOB_PARAMS{'P305_EXECUTE_P305S02T05'}   : 1;
    $::JOB_PARAMS{'P305_EXECUTE_P305S02T06'}   = exists $::JOB_PARAMS{'P305_EXECUTE_P305S02T06'}   ? $::JOB_PARAMS{'P305_EXECUTE_P305S02T06'}   : 1;
    $::JOB_PARAMS{'P305_FAILED_HEALTH_CHECKS'} = exists $::JOB_PARAMS{'P305_FAILED_HEALTH_CHECKS'} ? $::JOB_PARAMS{'P305_FAILED_HEALTH_CHECKS'} : 0;

    if ($::JOB_PARAMS{'P305_EXECUTE_P305S02T01'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P305S02T01 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P305_EXECUTE_P305S02T01'} = 0;
    }

    if ($::JOB_PARAMS{'P305_EXECUTE_P305S02T02'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Prepare_Impairment_Data_P305S02T02 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P305_EXECUTE_P305S02T02'} = 0;
    }

    if ($::JOB_PARAMS{'P305_EXECUTE_P305S02T03'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Traffic_Control_Values_Before_Test_P305S02T03 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P305_EXECUTE_P305S02T03'} = 0;
    }

    $::JOB_PARAMS{'P305_ORIGINAL_IGNORE_FAILED_HEALTH_CHECK'} = exists $::JOB_PARAMS{'P305_ORIGINAL_IGNORE_FAILED_HEALTH_CHECK'} ? $::JOB_PARAMS{'P305_ORIGINAL_IGNORE_FAILED_HEALTH_CHECK'} : $::JOB_PARAMS{'IGNORE_FAILED_HEALTH_CHECK'};
    # We always want to ignore the result of the health check while running the impairment tests
    # and then we check the result at the end.
    $::JOB_PARAMS{'IGNORE_FAILED_HEALTH_CHECK'} = "yes";

    # Repeat the test case a number of times
    for (; $::JOB_PARAMS{'P305_IMPAIRMENT_CNT'} <= $#values;) {
        $::JOB_PARAMS{'P305_IMPAIRMENT_DELAY_VALUES'} = @values[ $::JOB_PARAMS{'P305_IMPAIRMENT_CNT'} ];

        # Set start time for this repetition in case we want to have KPI verdicts for each repetition
        $::JOB_PARAMS{'KPI_START_TIME'} = time();

        # Set the description to be used for status messages
        $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case %d Moderate Network Impairment (%s)", $::JOB_PARAMS{'P305_IMPAIRMENT_CNT'}+1, $::JOB_PARAMS{'P305_IMPAIRMENT_DELAY_VALUES'};

        $message = sprintf "# Repetition %d of %d, Delay Values=%s #", $::JOB_PARAMS{'P305_IMPAIRMENT_CNT'}+1, scalar @values, $::JOB_PARAMS{'P305_IMPAIRMENT_DELAY_VALUES'};
        $length = length($message);
        General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

        if ($::JOB_PARAMS{'P305_EXECUTE_P305S02T04'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Set_Impairments_On_Workers_P305S02T04 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P305_EXECUTE_P305S02T04'} = 0;
        }

        # This call will wait for all PODs to be up and then check node health
        $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
        return $rc if $rc < 0;
        if (exists $::JOB_PARAMS{'HEALTH_CHECK_RESULT'} && $::JOB_PARAMS{'HEALTH_CHECK_RESULT'} eq "FAILED") {
            $::JOB_PARAMS{'P305_FAILED_HEALTH_CHECKS'}++;
        }

        # Set the description to be used for status messages
        $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case %d Moderate Network Impairment Removed (%s)", $::JOB_PARAMS{'P305_IMPAIRMENT_CNT'}+1, $::JOB_PARAMS{'P305_IMPAIRMENT_DELAY_VALUES'};

        if ($::JOB_PARAMS{'P305_EXECUTE_P305S02T05'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Clear_Impairments_On_Workers_P305S02T05 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P305_EXECUTE_P305S02T05'} = 0;
        }

        # This call will wait for all PODs to be up and then check node health
        $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
        return $rc if $rc < 0;
        if (exists $::JOB_PARAMS{'HEALTH_CHECK_RESULT'} && $::JOB_PARAMS{'HEALTH_CHECK_RESULT'} eq "FAILED") {
            $::JOB_PARAMS{'P305_FAILED_HEALTH_CHECKS'}++;
        }

        # Step the counter and reset the executed tasks
        $::JOB_PARAMS{'P305_IMPAIRMENT_CNT'}++;
        $::JOB_PARAMS{'P305_EXECUTE_P305S02T04'} = 1;
        $::JOB_PARAMS{'P305_EXECUTE_P305S02T05'} = 1;
    }

    # Restore the original value
    $::JOB_PARAMS{'IGNORE_FAILED_HEALTH_CHECK'} = $::JOB_PARAMS{'P305_ORIGINAL_IGNORE_FAILED_HEALTH_CHECK'};

    if ($::JOB_PARAMS{'P305_EXECUTE_P305S02T06'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Traffic_Control_Values_After_Test_P305S02T06 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P305_EXECUTE_P305S02T06'} = 0;
    }

    # Skip the final post healthcheck since we have already done it above
    $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = "yes";

    # Delete these temporary variables
    delete $::JOB_PARAMS{'P305_IMPAIRMENT_CNT'};
    delete $::JOB_PARAMS{'P305_IMPAIRMENT_DELAY_VALUES'};
    delete $::JOB_PARAMS{'P305_EXECUTE_P305S02T01'};
    delete $::JOB_PARAMS{'P305_EXECUTE_P305S02T02'};
    delete $::JOB_PARAMS{'P305_EXECUTE_P305S02T03'};
    delete $::JOB_PARAMS{'P305_EXECUTE_P305S02T04'};
    delete $::JOB_PARAMS{'P305_EXECUTE_P305S02T05'};
    delete $::JOB_PARAMS{'P305_EXECUTE_P305S02T06'};

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
    sub Check_Job_Parameters_P305S02T01 {

        my $error_cnt = 0;
        my $rc = 0;
        my $value_pair;
        my @values = split /[,|]/, $::JOB_PARAMS{'IMPAIRMENT_DELAY_VALUES'};

        # Check that valid delay values are specified
        for $value_pair (@values) {
            unless ($value_pair =~ /^\s*(\d+|\d+(s|sec|secs|ms|msec|msecs|us|usec|usecs))\s+(\d+|\d+(s|sec|secs|ms|msec|msecs|us|usec|usecs))\s*$/) {
                General::Logging::log_user_warning_message("The job variable 'IMPAIRMENT_DELAY_VALUES' is using the wrong format.\nIt should be a pair of integer numbers separated by a space and optionally with a unit that can be one of:\ns, sec, secs, ms, msec, msecs, us, usec or usecs.\nRestoring default value $playlist_variables{'IMPAIRMENT_DELAY_VALUES'}{'default_value'}");
                $::JOB_PARAMS{'IMPAIRMENT_DELAY_VALUES'} = $playlist_variables{'IMPAIRMENT_DELAY_VALUES'}{'default_value'};
                last;
            }
        }

        if ($::JOB_PARAMS{'WAIT_TIME_WITH_IMPAIRMENTS'} == 0) {
            General::Logging::log_user_warning_message("The job variable 'WAIT_TIME_WITH_IMPAIRMENTS' cannot have value 0, using default value $playlist_variables{'WAIT_TIME_WITH_IMPAIRMENTS'}{'default_value'}");
            $::JOB_PARAMS{'WAIT_TIME_WITH_IMPAIRMENTS'} = $playlist_variables{'WAIT_TIME_WITH_IMPAIRMENTS'}{'default_value'};
        }

        General::Logging::log_user_message("Used values are:\n  IMPAIRMENT_DELAY_VALUES=$::JOB_PARAMS{'IMPAIRMENT_DELAY_VALUES'}\n  WAIT_TIME_BETWEEN_IMPAIRMENTS=$::JOB_PARAMS{'WAIT_TIME_BETWEEN_IMPAIRMENTS'}\n  WAIT_TIME_WITH_IMPAIRMENTS=$::JOB_PARAMS{'WAIT_TIME_WITH_IMPAIRMENTS'}\n");

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
    sub Prepare_Impairment_Data_P305S02T02 {

        my $command_cnt;
        my %interface_info;
        my $kubectl_executable = "kubectl";
        my $namespace = "";
        my $rc = 0;
        my %pod_info;
        my $pod_tools_found = 0;
        my @result;
        my $var_name;
        my %worker_info;

        # Get worker node name and IP address information
        General::Logging::log_user_message("Fetching worker node information");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get node -o wide --no-headers --selector='!node-role.kubernetes.io/master'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to fetch worker node information");
            return 1;
        }
        for (@result) {
            # worker-pool1-0acqd6er-eccd-eevee-ds   Ready   worker   20d   v1.21.1   10.10.10.90    <none>   SUSE Linux Enterprise Server 15 SP2   5.3.18-24.75.3.22886.0.PTF.1187468-default   containerd://1.4.4
            # $1---------------------------------                                    $2---------
            if (/^(\S+)\s+\S+\s+\S+\s+\S+\s+\S+\s+(\S+)\s+/) {
                $worker_info{$1}{'ip'} = $2;
            }
        }

        # Check which namespace the tools are deployed in
        if ($::JOB_PARAMS{'TOOLS_NAMESPACE'} ne "") {
            $namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
            $kubectl_executable = $::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'};
        } else {
            $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
            $kubectl_executable = $::JOB_PARAMS{'KUBECTL_EXECUTABLE'};
        }

        # Get IP address and worker node name information for K6 and SEPPSIM PODs
        General::Logging::log_user_message("Fetching POD information for namespace $namespace");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$kubectl_executable get pod -o wide --no-headers --namespace $namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to fetch POD information");
            return 1;
        }
        for (@result) {
            # eric-dscload-deployment-66c9848dc-9vqx4 1/1     Running   0           30m    192.168.154.67    worker-pool1-l4al1bnw-eccd-snorlax-ds <none>           <none>
            # eric-k6-deployment-6f8487754f-9scvz     1/1     Running   0          4h14m   192.168.151.121   worker-pool1-q7cn8ifh-eccd-eevee-ds   <none>           <none>
            # eric-seppsim-c-58959b57cb-59j25         1/1     Running   0          127m    192.168.151.104   worker-pool1-q7cn8ifh-eccd-eevee-ds   <none>           <none>
            # $1---------------------------------                                          $2-------------   $3---------------------------------
            if (/^(eric-chfsim-\S+|eric-dscload-deployment-\S+|eric-k6-deployment-\S+|eric-nrfsim-\S+|eric-seppsim-\S+)\s+\d+\/\d+\s+\S+\s+\d+\s+\S+\s+(\S+)\s+(\S+)\s+/) {
                $pod_info{$1}{'ip'} = $2;
                $pod_info{$1}{'node'} = $3;
                $pod_info{$1}{'interface'} = "";
                $pod_info{$1}{'worker-ip'} = "";
                $pod_tools_found++;
            }
        }
        if ($pod_tools_found == 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Did not find any of the tool pods: eric-chfsim-*, eric-dscload-deployment-*, eric-k6-deployment-*, eric-nrfsim- or eric-seppsim-*");
            return 1;
        }

        # Look for the interface name for the POD IP addresses on all worker nodes
        for my $worker (keys %worker_info) {
            General::Logging::log_user_message("Fetching interface information from worker $worker");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR $worker_info{$worker}{'ip'} 'ip r'",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to fetch interface information");
                return 1;
            }
            for (@result) {
                # 192.168.151.121 dev calia192ad7ac8c scope link
                # $1-------------     $2-------------
                if (/^(\S+)\s+dev\s+(\S+)\s+scope link/) {
                    $interface_info{$1}{$worker} = $2;
                }
            }
        }

        # Store the interface name from %interface_info into %pod_info
        for my $pod_name (keys %pod_info) {
            if (exists $interface_info{ $pod_info{$pod_name}{'ip'} } && exists $interface_info{ $pod_info{$pod_name}{'ip'} }{ $pod_info{$pod_name}{'node'} }) {
                $pod_info{$pod_name}{'interface'} = $interface_info{ $pod_info{$pod_name}{'ip'} }{ $pod_info{$pod_name}{'node'} };
                $pod_info{$pod_name}{'worker-ip'} = $worker_info{ $pod_info{$pod_name}{'node'} }{'ip'};
            } else {
                General::Logging::log_user_warning_message("Did not find interface for pod $pod_name with IP $pod_info{$pod_name}{'ip'} on worker $pod_info{$pod_name}{'node'}, ignored this pod");
            }
        }

        # Create job variables for impariment commands
        $command_cnt = 1;
        for my $pod_name (keys %pod_info) {
            $var_name = sprintf "P305_IMPAIRMENT_COMMAND_%03d", $command_cnt++;
            if ($pod_info{$pod_name}{'interface'} ne "") {
                $::JOB_PARAMS{$var_name} = sprintf "ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR %s 'sudo -n tc qdisc %%s dev %s root netem delay %%s distribution normal'", $pod_info{$pod_name}{'worker-ip'}, $pod_info{$pod_name}{'interface'};
                # This will create a job variable that looks something like this:
                # $::JOB_PARAMS{'P305_IMPAIRMENT_COMMAND_001'}=ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR 10.10.10.105 'sudo -n tc qdisc %s dev calia192ad7ac8c root netem delay %s distribution normal'
                # The "%s" parameters will be replaced by values <add|change|del> and <delay value pair> at a later stage
                # for example the command might be: ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR 10.10.10.105 'sudo -n tc qdisc add dev calia192ad7ac8c root netem delay 100ms 400ms distribution normal'
                #                                                                                                                                  ---                                      -----------
            }
        }

        # Create job variables for printing traffic control definitions from the workers
        $command_cnt = 1;
        for my $worker_name (keys %worker_info) {
            $var_name = sprintf "P305_TRAFFIC_CONTROL_COMMAND_%03d", $command_cnt++;
            $::JOB_PARAMS{$var_name} = sprintf "ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR %s 'sudo -n tc qdisc show && echo ------- && sudo -n tc class show'", $worker_info{$worker_name}{'ip'};
            # This will create a job variable that looks something like this:
            # $::JOB_PARAMS{'P305_TRAFFIC_CONTROL_COMMAND_001'}=ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR 10.10.10.105 'sudo -n tc qdisc show && echo ------- && sudo -n tc class show'
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
    sub Print_Traffic_Control_Values_Before_Test_P305S02T03 {

        my $command;
        my $rc = 0;

        General::Logging::log_user_message("Getting Traffic Control values from all worker nodes");
        for my $var_name (sort keys %::JOB_PARAMS) {
            next unless ($var_name =~ /^P305_TRAFFIC_CONTROL_COMMAND_\d+$/);
            $command = $::JOB_PARAMS{$var_name};
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$command",
                    "hide-output"   => 1,
                }
            );

            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_warning_message("Failed to execute command: $command\nError ignored.\n");
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
    sub Set_Impairments_On_Workers_P305S02T04 {

        my $command;
        my $rc = 0;

        General::Logging::log_user_message("Loading Impairment delay values $::JOB_PARAMS{'P305_IMPAIRMENT_DELAY_VALUES'} for traffic from tools on worker nodes");
        for my $var_name (sort keys %::JOB_PARAMS) {
            next unless ($var_name =~ /^P305_IMPAIRMENT_COMMAND_\d+$/);
            $command = $::JOB_PARAMS{$var_name};
            # Replace the %s place holders in the command
            $command = sprintf "$command", "add", $::JOB_PARAMS{'P305_IMPAIRMENT_DELAY_VALUES'};
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$command",
                    "hide-output"   => 1,
                }
            );

            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute command: $command");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                return 1;
            }
        }

        # Now wait
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "confirm-interrupt" => 1,
                "progress-message"  => 1,
                "seconds"           => $::JOB_PARAMS{'WAIT_TIME_WITH_IMPAIRMENTS'},
                "use-logging"       => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} was successful";
        } else {
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} was interrupted by the user";
            General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
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
    sub Clear_Impairments_On_Workers_P305S02T05 {

        my $command;
        my $rc = 0;

        General::Logging::log_user_message("Clearing Impairment delay values $::JOB_PARAMS{'P305_IMPAIRMENT_DELAY_VALUES'} for traffic from tools on worker nodes");
        for my $var_name (sort keys %::JOB_PARAMS) {
            next unless ($var_name =~ /^P305_IMPAIRMENT_COMMAND_\d+$/);
            $command = $::JOB_PARAMS{$var_name};
            # Replace the %s place holders in the command
            $command = sprintf "$command", "del", $::JOB_PARAMS{'P305_IMPAIRMENT_DELAY_VALUES'};
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$command",
                    "hide-output"   => 1,
                }
            );

            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute command: $command");
                return 1;
            }
        }

        if ($::JOB_PARAMS{'WAIT_TIME_FOR_TRAFFIC_TO_STABILIZE_AFTER_REMOVING_IMPAIRMENTS'} > 0) {
            # Now wait
            General::Logging::log_user_message("Waiting $::JOB_PARAMS{'WAIT_TIME_FOR_TRAFFIC_TO_STABILIZE_AFTER_REMOVING_IMPAIRMENTS'} seconds for traffic to stabilize before checking KPI's");
            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "confirm-interrupt" => 1,
                    "progress-message"  => 1,
                    "seconds"           => $::JOB_PARAMS{'WAIT_TIME_FOR_TRAFFIC_TO_STABILIZE_AFTER_REMOVING_IMPAIRMENTS'},
                    "use-logging"       => 1,
                }
            );

            if ($rc == 1) {
                General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
            }
        }

        # Set start time for this repetition in case we want to have KPI verdicts for each repetition
        $::JOB_PARAMS{'KPI_START_TIME'} = time();

        if ($::JOB_PARAMS{'WAIT_TIME_BETWEEN_IMPAIRMENTS'} > 0) {
            # Now wait
            General::Logging::log_user_message("Waiting $::JOB_PARAMS{'WAIT_TIME_BETWEEN_IMPAIRMENTS'} seconds before collecting KPI's");
            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "confirm-interrupt" => 1,
                    "progress-message"  => 1,
                    "seconds"           => $::JOB_PARAMS{'WAIT_TIME_BETWEEN_IMPAIRMENTS'},
                    "use-logging"       => 1,
                }
            );

            if ($rc == 1) {
                General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
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
    sub Print_Traffic_Control_Values_After_Test_P305S02T06 {

        my $command;
        my $rc = 0;

        General::Logging::log_user_message("Getting Traffic Control values from all worker nodes");
        for my $var_name (sort keys %::JOB_PARAMS) {
            next unless ($var_name =~ /^P305_TRAFFIC_CONTROL_COMMAND_\d+$/);
            $command = $::JOB_PARAMS{$var_name};
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$command",
                    "hide-output"   => 1,
                }
            );

            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_warning_message("Failed to execute command: $command\nError ignored.\n");
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
sub Perform_Post_Test_Checks_P305S03 {

    my $rc;

    # This second call will perform post test checks and job cleanup
    $::JOB_PARAMS{'P932_TASK'} = "POST_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    return $rc if $rc < 0;

    if (exists $::JOB_PARAMS{'P305_FAILED_HEALTH_CHECKS'} && $::JOB_PARAMS{'P305_FAILED_HEALTH_CHECKS'} > 0) {
        # One or more workers has been skipped, mark job as failed
        General::Logging::log_user_error_message("Test case failed because $::JOB_PARAMS{'P305_FAILED_HEALTH_CHECKS'} health checks failed");
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
    }

    return $rc;
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P305S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
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
# This subroutine reads all parameters from the specified network configuration
# file and updates the %::NETWORK_CONFIG_PARAMS and %::JOB_PARAMS hash variables
# after applying filters to remove unwanted parameters.
#
sub parse_network_config_parameters {
    return Playlist::932_Test_Case_Common_Logic::parse_network_config_parameters();
}

# -----------------------------------------------------------------------------
sub set_playlist_variables {
    # Define playlist specific variables that is not already defined in the
    # sub playlist Playlist::932_Test_Case_Common_Logic.
    %playlist_variables = (
        # ---------------------------------------------------------------------
        'IMPAIRMENT_DELAY_VALUES' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "100ms 400ms|20ms 10ms|100ms 20ms|100ms 10ms|10ms 2ms",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the impairment delay values that will be used in the
test and each value pair should be separated by either a "|" or a ",".
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
This parameter specifies the namespace where the tools, K6 and SEPPSIM are
deployed in, if different than the SC Namespace.
If not specified it will read it from the network configuration file parameter
called 'tools_namespace'.
If this is also not set it will take the value from the SC_NAMESPACE or network
configuration file parameter 'sc_namespace'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'WAIT_TIME_BETWEEN_IMPAIRMENTS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "300",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the time in seconds to wait after disabling an impairment
before continuing with next task where the next impairment is enabled.
I.e. how long to run the traffic with no impairments.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'WAIT_TIME_FOR_TRAFFIC_TO_STABILIZE_AFTER_REMOVING_IMPAIRMENTS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "120",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the time in seconds to wait after disabling an impairment
before starting to check KPI values.
Normally it can take a minute or two after diabling an impairment before traffic
recovers fully and this timer can be used to make sure that traffic is recovered
before starting to check the traffic KPIs i.e. belore starting the other timer
specified with WAIT_TIME_BETWEEN_IMPAIRMENTS.
If the value is 0 then no wait is done before checking KPI values.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'WAIT_TIME_WITH_IMPAIRMENTS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "600",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the time in seconds to wait after enabling an impairment
before continuing with next task where the impairment is removed.
I.e. how long to run the traffic with a specific impairments.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist performs a robustness test case that verifies moderate impairments
of the traffic to/from the traffic simulators K6 and SEPPSIM by adding delays of
the messages on the specific interfaces used for the traffic simulators.

By default it will login to each worker node where the simulator POD's are
executing and add the following command:

sudo -n tc qdisc add dev <interface> root netem delay <delay value pair> distribution normal

For example:
sudo -n tc qdisc add dev calia192ad7ac8c root netem delay 100ms 400ms distribution normal
sudo -n tc qdisc add dev cali01425368d08 root netem delay 100ms 400ms distribution normal
 : etc.

Then it will wait 10 minutes (600 seconds) and then change the delay value to
the next delay value pair, for example:
sudo -n tc qdisc change dev calia192ad7ac8c root netem delay 20ms 10ms distribution normal
sudo -n tc qdisc change dev cali01425368d08 root netem delay 20ms 10ms distribution normal
 : etc.

This continues until each delay value pair has been tested, at which point it
will remove the last used delay, for example:
sudo -n tc qdisc del dev calia192ad7ac8c root netem delay 10ms 2ms distribution normal
sudo -n tc qdisc del dev cali01425368d08 root netem delay 10ms 2ms distribution normal
 : etc.

For more information how impariments are working in Linix see for example:
https://www.excentis.com/blog/use-linux-traffic-control-impairment-node-test-environment-part-1
https://man7.org/linux/man-pages/man8/tc.8.html


Used Job Parameters:
====================
EOF
    # Special handling for the 3xx_Robustness_Test_xxxx playlists so we print out all variable
    # information from the main playlist and the 932_Test_Case_Common_Logic sub playlist.
    use File::Basename qw(dirname basename);
    my $length;
    my $message;
    my $path_to_932_playlist = dirname(__FILE__) . "/932_Test_Case_Common_Logic.pm";
    my $playlist_name = basename(__FILE__);
    $playlist_name =~ s/\.pm//;
    my %temp_hash = ( %Playlist::932_Test_Case_Common_Logic::playlist_variables, %playlist_variables );

    General::Playlist_Operations::print_info_about_job_variables(\%temp_hash);

    $message = "# Global variable access in playlist $playlist_name #";
    $length = length($message);
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables(__FILE__, \%playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);

    $message = "# Global variable access in playlist 932_Test_Case_Common_Logic #";
    $length = length($message);
    printf "\n\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables($path_to_932_playlist, \%Playlist::932_Test_Case_Common_Logic::playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables($path_to_932_playlist);
}

# -----------------------------------------------------------------------------
sub usage_return_playlist_variables {
    return ( %Playlist::932_Test_Case_Common_Logic::playlist_variables, %playlist_variables );
}

1;
