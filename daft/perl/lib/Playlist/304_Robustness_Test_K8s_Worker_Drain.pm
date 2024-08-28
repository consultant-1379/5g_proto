package Playlist::304_Robustness_Test_K8s_Worker_Drain;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.26
#  Date     : 2024-05-29 14:00:59
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
    $::JOB_PARAMS{'JOBTYPE'} = "ROBUSTNESS_TEST_K8S_WORKER_DRAIN";

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Test_Checks_P304S01, \&Fallback001_P304S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Test_Case_P304S02, \&Fallback001_P304S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Checks_P304S03, \&Fallback001_P304S99 );
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
sub Perform_Pre_Test_Checks_P304S01 {

    my $rc;

    # This first call will initialize job environment and perform pre test checks
    $::JOB_PARAMS{'P932_TASK'} = "PRE_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_All_Worker_Pods_P304S01T01 } );
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
    sub Check_All_Worker_Pods_P304S01T01 {

        my $command;
        my @faults = ();
        my $namespace;
        my $node_name;
        my @node_names = ADP::Kubernetes_Operations::get_worker_nodes();
        my $podname;
        my $rc = 0;
        my @result;
        my $status;

        for $node_name (@node_names) {
            General::Logging::log_user_message("Checking POD status for all PODS in node: $node_name");
            $command = sprintf "%s get pods --all-namespaces -o wide --field-selector spec.nodeName=%s", $::JOB_PARAMS{'KUBECTL_EXECUTABLE'}, $node_name;
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );

            if ($rc != 0) {
                push @faults, "Failed to execute command: $command\n  " . join "\n  ", @result;
                next;
            }
            # NAMESPACE     NAME                                                   READY   STATUS    RESTARTS        AGE     IP                NODE                                  NOMINATED NODE   READINESS GATES
            # kube-system   calico-node-sm5lh                                      1/1     Running   0               4h38m   10.10.10.66       worker-pool1-s7er3e0v-eccd-eevee-ds   <none>           <none>
            # kube-system   calicoctl-75b66c87fd-nvzdf                             1/1     Running   0               3h56m   10.10.10.66       worker-pool1-s7er3e0v-eccd-eevee-ds   <none>           <none>
            for (@result) {
                if (/^\s*(\S+)\s+(\S+)\s+\d+\/\d+\s+(\S+)\s+\d+\s+.+$node_name/) {
                    # $1            $2                                                             $3
                    # kube-system   calico-node-sm5lh                                      1/1     Running   0               4h38m   10.10.10.66       worker-pool1-s7er3e0v-eccd-eevee-ds   <none>           <none>
                    $namespace = $1;
                    $podname   = $2;
                    $status    = $3;
                    next if ($status eq "Completed");
                    if ($status ne "Running") {
                        next if ($podname =~ /^(eric-data-search-engine-curator|eric-log-transformer).*/);
                        push @faults, "Pod '$podname' in namespace '$namespace' has status '$status' when expecting status 'Running'";
                    }
                }
            }
        }

        if (@faults) {
            General::Logging::log_user_error_message("The following faults was detected that might cause issues during the test:\n  " . join "\n  ", @faults);
            return 1;
        } else {
            General::Logging::log_user_message("All POD status seems to be OK");
            return 0;
        }
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
sub Perform_Test_Case_P304S02 {

    my $length;
    my @node_names = ADP::Kubernetes_Operations::get_worker_nodes();
    my $node_status;
    my $message;
    my $old_skip_collect_logs;
    my $old_skip_kpi_verdict_check;
    my $old_skip_post_healthcheck;
    my $rc = 0;
    my %tool_nodes = map { $_ => 1 } ADP::Kubernetes_Operations::get_nodes_with_label("usage=tools");

    # In case of a re-run of an interrupted job set ifnformation about which tasks to execute and which what time offset
    $::JOB_PARAMS{'P304_NODE_CNT'}            = exists $::JOB_PARAMS{'P304_NODE_CNT'}            ? $::JOB_PARAMS{'P304_NODE_CNT'}            : 0;
    $::JOB_PARAMS{'P304_EXECUTE_P304S02T01'}  = exists $::JOB_PARAMS{'P304_EXECUTE_P304S02T01'}  ? $::JOB_PARAMS{'P304_EXECUTE_P304S02T01'}  : 1;
    $::JOB_PARAMS{'P304_EXECUTE_P304S02T02'}  = exists $::JOB_PARAMS{'P304_EXECUTE_P304S02T02'}  ? $::JOB_PARAMS{'P304_EXECUTE_P304S02T02'}  : 1;
    $::JOB_PARAMS{'P304_EXECUTE_P304S02T03'}  = exists $::JOB_PARAMS{'P304_EXECUTE_P304S02T03'}  ? $::JOB_PARAMS{'P304_EXECUTE_P304S02T03'}  : 1;
    $::JOB_PARAMS{'P304_EXECUTE_P304S02T04'}  = exists $::JOB_PARAMS{'P304_EXECUTE_P304S02T04'}  ? $::JOB_PARAMS{'P304_EXECUTE_P304S02T04'}  : 1;
    $::JOB_PARAMS{'P304_EXECUTE_P304S02T05'}  = exists $::JOB_PARAMS{'P304_EXECUTE_P304S02T05'}  ? $::JOB_PARAMS{'P304_EXECUTE_P304S02T05'}  : 1;
    $::JOB_PARAMS{'P304_EXECUTE_P304S02T06'}  = exists $::JOB_PARAMS{'P304_EXECUTE_P304S02T06'}  ? $::JOB_PARAMS{'P304_EXECUTE_P304S02T06'}  : 1;
    $::JOB_PARAMS{'P304_EXECUTE_P304S02T07'}  = exists $::JOB_PARAMS{'P304_EXECUTE_P304S02T07'}  ? $::JOB_PARAMS{'P304_EXECUTE_P304S02T07'}  : 1;
    $::JOB_PARAMS{'P304_SKIPPED_WORKERS'}     = exists $::JOB_PARAMS{'P304_SKIPPED_WORKERS'}     ? $::JOB_PARAMS{'P304_SKIPPED_WORKERS'}     : 0;
    $::JOB_PARAMS{'P304_FAILED_POD_RESTARTS'} = exists $::JOB_PARAMS{'P304_FAILED_POD_RESTARTS'} ? $::JOB_PARAMS{'P304_FAILED_POD_RESTARTS'} : 0;

    for (; $::JOB_PARAMS{'P304_NODE_CNT'} <= $#node_names;) {
        $::JOB_PARAMS{'P304_NODE_NAME'} = $node_names[$::JOB_PARAMS{'P304_NODE_CNT'}];

        # Set start time for this repetition in case we want to have KPI verdicts for each repetition
        $::JOB_PARAMS{'KPI_START_TIME'} = time();

        # Set the description to be used for status messages
        $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case %d Node Drain (%s)", $::JOB_PARAMS{'P304_NODE_CNT'}+1, $::JOB_PARAMS{'P304_NODE_NAME'};

        $node_status = ADP::Kubernetes_Operations::get_node_status( { "name" => $::JOB_PARAMS{'P304_NODE_NAME'} } );

        # Print a progress message
        $message = sprintf "# Worker Node Drain %d of %d, Node Name=%s #", $::JOB_PARAMS{'P304_NODE_CNT'} + 1, $#node_names + 1, $::JOB_PARAMS{'P304_NODE_NAME'};
        $length = length($message);
        if ($node_status eq "Ready") {
            General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

            # Check if the node should be execluded from the test
            if (exists $tool_nodes{$::JOB_PARAMS{'P304_NODE_NAME'}}) {
                # This node is used for tools and should be excluded from the test.
                # Step the counter and take next worker
                $::JOB_PARAMS{'P304_NODE_CNT'}++;
                General::Logging::log_user_message("This node is hosting tools and will be skipped from the test");
                next;
            }

            if ($::JOB_PARAMS{'P304_EXECUTE_P304S02T01'} == 1) {
                $::JOB_PARAMS{'P304_WORKER_SKIPPED'} = 0;
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Pods_In_Node_Before_Drain_P304S02T01 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P304_EXECUTE_P304S02T01'} = 0;
                if ($::JOB_PARAMS{'P304_WORKER_SKIPPED'} == 1) {
                    $::JOB_PARAMS{'P304_SKIPPED_WORKERS'}++;
                    # Step the counter and take next worker
                    $::JOB_PARAMS{'P304_NODE_CNT'}++;
                    next;
                }
            }

            ##############
            # Drain Node #
            ##############

            if ($::JOB_PARAMS{'P304_EXECUTE_P304S02T02'} == 1) {
                $::JOB_PARAMS{'P304_NODE_DRAINED'} = "";
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Drain_Node_P304S02T02 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P304_EXECUTE_P304S02T02'} = 0;
                $::JOB_PARAMS{'P304_NODE_DRAINED'} = $::JOB_PARAMS{'P304_NODE_NAME'};
            }

            if ($::JOB_PARAMS{'P304_EXECUTE_P304S02T03'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Node_Status_After_Drain_P304S02T03 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P304_EXECUTE_P304S02T03'} = 0;
            }

            # Check POD status in namespace, waiting until the pods become stable, skip the post health check.
            # This call will wait for all PODs to be up before continuing
            $old_skip_post_healthcheck = $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'};
            $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = "yes";
            $old_skip_kpi_verdict_check = $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'};
            $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'} = "yes";
            $old_skip_collect_logs = $::JOB_PARAMS{'SKIP_COLLECT_LOGS'};
            $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = "yes";
            $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
            $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = $old_skip_post_healthcheck;
            $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'} = $old_skip_kpi_verdict_check;
            $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = $old_skip_collect_logs;
            return $rc if $rc < 0;

            if ($::JOB_PARAMS{'P304_EXECUTE_P304S02T04'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Pods_In_Node_After_Drain_P304S02T04 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P304_EXECUTE_P304S02T04'} = 0;
            }

            if ($::JOB_PARAMS{'P304_EXECUTE_P304S02T05'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Health_Of_Node_After_Drain_P304S02T05 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P304_EXECUTE_P304S02T05'} = 0;
            }

            #################
            # Uncordon Node #
            #################

            if ($::JOB_PARAMS{'P304_EXECUTE_P304S02T06'} == 1) {
                $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case %d Node Uncordon (%s)", $::JOB_PARAMS{'P304_NODE_CNT'}+1, $::JOB_PARAMS{'P304_NODE_NAME'};
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Uncordon_Node_P304S02T06 } );
                $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case %d Node Drain (%s)", $::JOB_PARAMS{'P304_NODE_CNT'}+1, $::JOB_PARAMS{'P304_NODE_NAME'};
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P304_EXECUTE_P304S02T06'} = 0;
                $::JOB_PARAMS{'P304_NODE_DRAINED'} = "";
            }

            # Check POD status in namespace, waiting until the pods become stable, skip the post health check.
            # This call will wait for all PODs to be up before continuing
            $old_skip_post_healthcheck = $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'};
            $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = "yes";
            $old_skip_kpi_verdict_check = $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'};
            $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'} = "yes";
            $old_skip_collect_logs = $::JOB_PARAMS{'SKIP_COLLECT_LOGS'};
            $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = "yes";
            $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
            $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = $old_skip_post_healthcheck;
            $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'} = $old_skip_kpi_verdict_check;
            $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = $old_skip_collect_logs;
            return $rc if $rc < 0;

            if ($::JOB_PARAMS{'P304_EXECUTE_P304S02T07'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Pods_In_Node_After_Uncordon_P304S02T07 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P304_EXECUTE_P304S02T07'} = 0;
            }

            # ##########################################
            # Check node health after Drain / Uncordon #
            # ##########################################

            # This call will wait for all PODs to be up and then check node health
            $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
            return $rc if $rc < 0;
        } else {
            General::Logging::log_user_message( sprintf "%s\n%s\n%s\n\n%s\n", "#" x $length, $message, "#" x $length, "Node skipped because Status is not Ready ($node_status)" );
        }

        # Step the counter and reset the executed tasks
        $::JOB_PARAMS{'P304_NODE_CNT'}++;
        $::JOB_PARAMS{'P304_EXECUTE_P304S02T01'} = 1;
        $::JOB_PARAMS{'P304_EXECUTE_P304S02T02'} = 1;
        $::JOB_PARAMS{'P304_EXECUTE_P304S02T03'} = 1;
        $::JOB_PARAMS{'P304_EXECUTE_P304S02T04'} = 1;
        $::JOB_PARAMS{'P304_EXECUTE_P304S02T05'} = 1;
        $::JOB_PARAMS{'P304_EXECUTE_P304S02T06'} = 1;
        $::JOB_PARAMS{'P304_EXECUTE_P304S02T07'} = 1;
    }

    # Skip the final post healthcheck since we have already done it above
    $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = "yes";

    # Delete these temporary variables
    delete $::JOB_PARAMS{'P304_EXECUTE_P304S02T01'};
    delete $::JOB_PARAMS{'P304_EXECUTE_P304S02T02'};
    delete $::JOB_PARAMS{'P304_EXECUTE_P304S02T03'};
    delete $::JOB_PARAMS{'P304_EXECUTE_P304S02T04'};
    delete $::JOB_PARAMS{'P304_EXECUTE_P304S02T05'};
    delete $::JOB_PARAMS{'P304_EXECUTE_P304S02T06'};
    delete $::JOB_PARAMS{'P304_EXECUTE_P304S02T07'};
    delete $::JOB_PARAMS{'P304_NODE_CNT'};
    delete $::JOB_PARAMS{'P304_NODE_NAME'};
    delete $::JOB_PARAMS{'P304_WORKER_SKIPPED'};

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
    sub Check_Pods_In_Node_Before_Drain_P304S02T01 {

        my $command = sprintf "%s get pods --all-namespaces -o wide --field-selector spec.nodeName=%s", $::JOB_PARAMS{'KUBECTL_EXECUTABLE'}, $::JOB_PARAMS{'P304_NODE_NAME'};
        my @faults = ();
        my $namespace;
        my $node_name = $::JOB_PARAMS{'P304_NODE_NAME'};
        my $podname;
        my $rc = 0;
        my @result;
        my $status;

        General::Logging::log_user_message("Checking for PODS in node: $::JOB_PARAMS{'P304_NODE_NAME'}");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message(join "\n", @result);
            # NAMESPACE     NAME                                                   READY   STATUS    RESTARTS        AGE     IP                NODE                                  NOMINATED NODE   READINESS GATES
            # kube-system   calico-node-sm5lh                                      1/1     Running   0               4h38m   10.10.10.66       worker-pool1-s7er3e0v-eccd-eevee-ds   <none>           <none>
            # kube-system   calicoctl-75b66c87fd-nvzdf                             1/1     Running   0               3h56m   10.10.10.66       worker-pool1-s7er3e0v-eccd-eevee-ds   <none>           <none>
            for (@result) {
                if (/^\s*(\S+)\s+(\S+)\s+\d+\/\d+\s+(\S+)\s+\d+\s+.+$node_name/) {
                    # $1            $2                                                             $3
                    # kube-system   calico-node-sm5lh                                      1/1     Running   0               4h38m   10.10.10.66       worker-pool1-s7er3e0v-eccd-eevee-ds   <none>           <none>
                    $namespace = $1;
                    $podname   = $2;
                    $status    = $3;
                    next if ($status eq "Completed");
                    if ($status ne "Running") {
                        next if ($podname =~ /^(eric-data-search-engine-curator|eric-log-transformer).*/);
                        push @faults, "Pod '$podname' in namespace '$namespace' has status '$status' when expecting status 'Running'";
                    }
                }
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute command: $command");
            return $rc;
        }

        if (@faults) {
            General::Logging::log_user_error_message("The following faults was detected that might cause issues during the test and for this reason the worker has been skipped:\n  " . join "\n  ", @faults);
            $::JOB_PARAMS{'P304_WORKER_SKIPPED'} = 1;
        } else {
            General::Logging::log_user_message("All POD status seems to be OK");
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
    sub Drain_Node_P304S02T02 {

        my $command = sprintf "%s drain --ignore-daemonsets --delete-emptydir-data --force %s", $::JOB_PARAMS{'KUBECTL_EXECUTABLE'}, $::JOB_PARAMS{'P304_NODE_NAME'};
        my $rc = 0;

        General::Logging::log_user_message("Draining node $::JOB_PARAMS{'P304_NODE_NAME'}.\nThis will take a while to complete.\n");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command",
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message("Drain of node $::JOB_PARAMS{'P304_NODE_NAME'} was successful");
            push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} was successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Drain of node $::JOB_PARAMS{'P304_NODE_NAME'} failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
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
    sub Check_Node_Status_After_Drain_P304S02T03 {

        my $command = sprintf "%s get nodes", $::JOB_PARAMS{'KUBECTL_EXECUTABLE'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Checking node status after drain");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message(join "\n", @result);
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute command: $command");
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
    sub Print_Pods_In_Node_After_Drain_P304S02T04 {

        my $command = sprintf "%s get pods --all-namespaces -o wide --field-selector spec.nodeName=%s", $::JOB_PARAMS{'KUBECTL_EXECUTABLE'}, $::JOB_PARAMS{'P304_NODE_NAME'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Checking for PODS in node: $::JOB_PARAMS{'P304_NODE_NAME'}");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message(join "\n", @result);
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute command: $command");
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
    sub Check_Health_Of_Node_After_Drain_P304S02T05 {

        my $color_info = $::color_scheme eq "no" ? "--no_color " : "";
        my $command = "perl $::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/system_health_check.pl ";
        my $pod_restarts_time_threshold = time() - $::JOB_PARAMS{'_JOB_STARTTIME'} + 1;     # 1 second before starting the job
        my $rc = 0;
        my @result;

        if ($::JOB_PARAMS{'SC_NAMESPACE'} ne "") {
            $command .= "--namespace $::JOB_PARAMS{'SC_NAMESPACE'} ";
        }

        General::Logging::log_user_message("Checking node health.\nThis will take a while to complete.\n");

        # Perform an almost complete health check
        $rc = General::OS_Operations::send_command(
            {
                "command"       =>  $command .
                                    "--log_file $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/all.log " .
                                    $color_info .
                                    "--verbose " .
                                    ($::JOB_PARAMS{'KUBECONFIG'} ne "" ? "--kubeconfig=$::JOB_PARAMS{'KUBECONFIG'} " : "") .
                                    "--variables=pod_restarts_time_threshold=$pod_restarts_time_threshold " .
                                    "--check node_status " .
                                    "--check pod_ready_status " .
                                    "--check pod_restarts",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );


        if ($rc == 0) {
            # Check was successful, show output
            General::Logging::log_user_message(join "\n", @result);
            push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} Health Check after drain was successful";
        } elsif ($rc == 2) {
            # Check reported warnings, show output but treat it as successful
            General::Logging::log_user_warning_message(join "\n", @result);
            push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} Health Check after drain gave warnings but classified as successful";
            $rc = 0;
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute command: $command ...");
            # Check to see if the failure was because of restarts
            my $found = 0;
            for (@result) {
                if (/^\s*Failed\s+pod_restarts\s*$/) {
                    # If the pod_restarts failed then we remember this fault but still continue
                    # and then report the failure at the end.
                    $found = 1;
                } elsif (/^\s*Failed\s+\S+\s*$/) {
                    # One of the other checks failed, this should cause the test case to fail.
                    $found = 0;
                    last;
                }
            }
            if ($found == 1) {
                # Only the pod_restarts check failed, remember this failure and fake the rc and then report the failure at the end.
                $::JOB_PARAMS{'P304_FAILED_POD_RESTARTS'}++;
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} Health Check after drain failed because one or more pods restarted";
                $rc = 0;
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
    sub Uncordon_Node_P304S02T06 {

        my $command = sprintf "%s uncordon %s", $::JOB_PARAMS{'KUBECTL_EXECUTABLE'}, $::JOB_PARAMS{'P304_NODE_NAME'};
        my $rc = 0;

        General::Logging::log_user_message("Uncordon node $::JOB_PARAMS{'P304_NODE_NAME'}.\nThis will take a while to complete.\n");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command",
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message("Uncordon of node $::JOB_PARAMS{'P304_NODE_NAME'} was successful");
            push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} was successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Uncordon of node $::JOB_PARAMS{'P304_NODE_NAME'} failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
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
    sub Print_Pods_In_Node_After_Uncordon_P304S02T07 {

        my $command = sprintf "%s get pods --all-namespaces -o wide --field-selector spec.nodeName=%s", $::JOB_PARAMS{'KUBECTL_EXECUTABLE'}, $::JOB_PARAMS{'P304_NODE_NAME'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Checking for PODS in node: $::JOB_PARAMS{'P304_NODE_NAME'}");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message(join "\n", @result);
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute command: $command");
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
sub Perform_Post_Test_Checks_P304S03 {

    my $rc;

    # This second call will perform post test checks and job cleanup
    $::JOB_PARAMS{'P932_TASK'} = "POST_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    return $rc if $rc < 0;

    if (exists $::JOB_PARAMS{'P304_SKIPPED_WORKERS'} && $::JOB_PARAMS{'P304_SKIPPED_WORKERS'} > 0) {
        # One or more workers has been skipped, mark job as failed
        General::Logging::log_user_error_message("Test case failed because $::JOB_PARAMS{'P304_SKIPPED_WORKERS'} were skipped");
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
    }

    if (exists $::JOB_PARAMS{'P304_FAILED_POD_RESTARTS'} && $::JOB_PARAMS{'P304_FAILED_POD_RESTARTS'} > 0) {
        # One or more workers has been skipped, mark job as failed
        General::Logging::log_user_error_message("Test case failed because $::JOB_PARAMS{'P304_FAILED_POD_RESTARTS'} POD restart checks failed");
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
sub Fallback001_P304S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    if (exists $::JOB_PARAMS{'P304_NODE_DRAINED'} && $::JOB_PARAMS{'P304_NODE_DRAINED'} ne "") {

        $::JOB_PARAMS{'P304_NODE_NAME'} = $::JOB_PARAMS{'P304_NODE_DRAINED'};
        $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case %d Node Uncordon (%s)", $::JOB_PARAMS{'P304_NODE_CNT'}+1, $::JOB_PARAMS{'P304_NODE_NAME'};
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Uncordon_Node_P304S02T06 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P304_NODE_DRAINED'} = "";

    }

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

    # NOTE:
    # We currently have no special variables in this playlist but we leave
    # the %playlist_variables variable initialization below as an example
    # in case we want to add some in the future. In such case just remove
    # this comment and the 'return;' line below.
    return;

    %playlist_variables = (
        # ---------------------------------------------------------------------
        'TIMEOUT_NODE_REACHABLE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "120",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify how long time to wait for the node to become pingable and to have SSH
back after restarting the node.
Specify in seconds how long time to wait before reporting an error.
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

This Playlist performs a robustness test case that verifies worker node drains.

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
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables($path_to_932_playlist, \%Playlist::932_Test_Case_Common_Logic::playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables($path_to_932_playlist);
}

# -----------------------------------------------------------------------------
sub usage_return_playlist_variables {
    return ( %Playlist::932_Test_Case_Common_Logic::playlist_variables, %playlist_variables );
}

1;
