package Playlist::302_Robustness_Test_K8s_Master_Restart;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.21
#  Date     : 2024-05-29 14:00:59
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021-2024
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
    $::JOB_PARAMS{'JOBTYPE'} = "ROBUSTNESS_TEST_K8S_MASTER_RESTART";

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Test_Checks_P302S01, \&Fallback001_P302S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Test_Case_P302S02, \&Fallback001_P302S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Checks_P302S03, \&Fallback001_P302S99 );
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
sub Perform_Pre_Test_Checks_P302S01 {

    my $rc;

    # This first call will initialize job environment and perform pre test checks
    $::JOB_PARAMS{'P932_TASK'} = "PRE_CHECK";
    $::JOB_PARAMS{'SKIP_DOCKER_AND_NERDCTL_CHECK'} = "yes";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Find_Node_IP_Addresses_And_Hostnames_To_Restart_P302S01T01 } );
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
    sub Find_Node_IP_Addresses_And_Hostnames_To_Restart_P302S01T01 {

        my $count = 1;
        my @ip_addresses = split /,/, $::JOB_PARAMS{'NODE_IP_TO_RESTART'};
        my $message = "";
        my $rc = 0;
        my @result;

        if (scalar @ip_addresses == 0) {
            # Not specified by the user, try to use the internal IP-addresses for the
            # master or control-plane nodes.
            @ip_addresses = ADP::Kubernetes_Operations::get_node_ip( { "type" => "master" } );
        }

        if (scalar @ip_addresses == 0) {
            General::Logging::log_user_error_message("No IP-addresses specified or found");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        for my $ip (@ip_addresses) {
            General::Logging::log_user_message("Finding hostname of node at IP-address $ip");

            $rc = General::OS_Operations::send_command_via_ssh(
                {
                    "commands"              => [ "hostname -f" ],
                    "hide-output"           => 1,
                    "return-output"         => \@result,
                    "command-in-output"     => 0,
                    "ip"                    => $ip,
                    "user"                  => $::JOB_PARAMS{'NODE_USER_NAME'},
                    "use-standard-ssh"      => "yes",
                }
            );

            use Data::Dumper;
            print "DBG:rc=$rc, number of lines=", scalar @result, "\n", Dumper(@result);

            if ($rc == 0 && scalar @result == 1) {
                my $name = sprintf "NODE_IP_%02d", $count;
                $::JOB_PARAMS{$name} = $ip;
                $message .= "$name = $::JOB_PARAMS{$name}\n";
                $name = sprintf "NODE_HOSTNAME_%02d", $count;
                $::JOB_PARAMS{$name} = $result[0];
                $message .= "$name = $::JOB_PARAMS{$name}\n";
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to fetch hostname for IP $ip");
                return 1;
            }
            $::JOB_PARAMS{"MAX_IP_COUNT"} = $count;
            $count++;
        }

        if ($message ne "") {
            General::Logging::log_user_message($message);
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
sub Perform_Test_Case_P302S02 {

    my $length;
    my @ip_addresses = ADP::Kubernetes_Operations::get_node_ip( { "type" => "master" } );
    my $message;
    my @node_names = ADP::Kubernetes_Operations::get_master_nodes();
    my $rc;

    # In case of a re-run of an interrupted job set ifnformation about which tasks to execute and which what time offset
    $::JOB_PARAMS{'P302_NODE_CNT'}    = exists $::JOB_PARAMS{'P302_NODE_CNT'}    ? $::JOB_PARAMS{'P302_NODE_CNT'}    : 1;
    $::JOB_PARAMS{'P302_EXECUTE_P302S02T01'} = exists $::JOB_PARAMS{'P302_EXECUTE_P302S02T01'} ? $::JOB_PARAMS{'P302_EXECUTE_P302S02T01'} : 1;
    $::JOB_PARAMS{'P302_EXECUTE_P302S02T02'} = exists $::JOB_PARAMS{'P302_EXECUTE_P302S02T02'} ? $::JOB_PARAMS{'P302_EXECUTE_P302S02T02'} : 1;
    $::JOB_PARAMS{'P302_EXECUTE_P302S02T03'} = exists $::JOB_PARAMS{'P302_EXECUTE_P302S02T03'} ? $::JOB_PARAMS{'P302_EXECUTE_P302S02T03'} : 1;
    $::JOB_PARAMS{'P302_EXECUTE_P302S02T04'} = exists $::JOB_PARAMS{'P302_EXECUTE_P302S02T04'} ? $::JOB_PARAMS{'P302_EXECUTE_P302S02T04'} : 1;
    $::JOB_PARAMS{'P302_SKIPPED_MASTERS'}    = exists $::JOB_PARAMS{'P302_SKIPPED_MASTERS'}    ? $::JOB_PARAMS{'P302_SKIPPED_MASTERS'}    : 0;

    for my $name (sort keys %::JOB_PARAMS) {
        if ($name =~ /^NODE_IP_(\d+)$/) {
            next if ($::JOB_PARAMS{'P302_NODE_CNT'} < $1);   # We have already executed this loop
            $::JOB_PARAMS{'P302_NODE_CNT'} = $1;
            $::JOB_PARAMS{'P302_NODE_IP'} = $::JOB_PARAMS{"NODE_IP_$1"};
            $::JOB_PARAMS{'P302_NODE_NAME'} = $::JOB_PARAMS{"NODE_HOSTNAME_$1"};
        } else {
            # Not the variable that we want
            next;
        }

        # Set start time for this repetition in case we want to have KPI verdicts for each repetition
        $::JOB_PARAMS{'KPI_START_TIME'} = time();

        # Set the description to be used for status messages
        $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case %d Node Reboot (%s)", $::JOB_PARAMS{'P302_NODE_CNT'}, $::JOB_PARAMS{'P302_NODE_NAME'};

        # Print a progress message
        $message = sprintf "# Restart %d of %d, Node IP=%s #", $::JOB_PARAMS{'P302_NODE_CNT'}, $::JOB_PARAMS{'MAX_IP_COUNT'}, $::JOB_PARAMS{'P302_NODE_IP'};
        $length = length($message);
        General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

        if ($::JOB_PARAMS{'P302_NODE_NAME'} eq $::JOB_PARAMS{'DAFT_HOSTNAME'}) {
            # We do not want to restart the node that DAFT is running on
            General::Logging::log_user_warning_message("$::JOB_PARAMS{'TEST_DESCRIPTION'} was skipped because DAFT is running on this node");
            push @::JOB_STATUS, "(-) $::JOB_PARAMS{'TEST_DESCRIPTION'} was skipped because DAFT is running on this node";
            # Take the next node
            $::JOB_PARAMS{'P302_NODE_CNT'}++;
            next;
        }

        if ($::JOB_PARAMS{'P302_EXECUTE_P302S02T01'} == 1) {
            $::JOB_PARAMS{'P302_MASTER_SKIPPED'} = 0;
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Pods_In_Node_Before_Restart_P302S02T01 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P302_EXECUTE_P302S02T01'} = 0;
            if ($::JOB_PARAMS{'P302_MASTER_SKIPPED'} == 1) {
                $::JOB_PARAMS{'P302_SKIPPED_MASTERS'}++;
                # Take the next node
                $::JOB_PARAMS{'P302_NODE_CNT'}++;
                next;
            }
        }

        if ($::JOB_PARAMS{'P302_EXECUTE_P302S02T02'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Restart_Node_P302S02T02 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P302_EXECUTE_P302S02T02'} = 0;
        }

        if ($::JOB_PARAMS{'P302_EXECUTE_P302S02T03'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Node_To_Become_Reachable_P302S02T03 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P302_EXECUTE_P302S02T03'} = 0;
        }

        if ($::JOB_PARAMS{'WAIT_TIME_FOR_TRAFFIC_TO_STABILIZE_AFTER_RESTART'} > 0) {
            # Now wait
            General::Logging::log_user_message("Waiting $::JOB_PARAMS{'WAIT_TIME_FOR_TRAFFIC_TO_STABILIZE_AFTER_RESTART'} seconds for traffic to stabilize before checking KPI's");
            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "confirm-interrupt" => 1,
                    "progress-message"  => 1,
                    "seconds"           => $::JOB_PARAMS{'WAIT_TIME_FOR_TRAFFIC_TO_STABILIZE_AFTER_RESTART'},
                    "use-logging"       => 1,
                }
            );

            if ($rc == 1) {
                General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
            }
        }

        # This call will wait for all PODs to be up and then check node health
        $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
        return $rc if $rc < 0;

        if ($::JOB_PARAMS{'P302_EXECUTE_P302S02T04'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Pods_In_Node_After_Restart_P302S02T04 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P302_EXECUTE_P302S02T04'} = 0;
        }

        # Step the counter and reset the executed tasks
        $::JOB_PARAMS{'P302_NODE_CNT'}++;
        $::JOB_PARAMS{'P302_EXECUTE_P302S02T01'} = 1;
        $::JOB_PARAMS{'P302_EXECUTE_P302S02T02'} = 1;
        $::JOB_PARAMS{'P302_EXECUTE_P302S02T03'} = 1;
        $::JOB_PARAMS{'P302_EXECUTE_P302S02T04'} = 1;
    }

    # Skip the final post healthcheck since we have already done it above
    $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = "yes";

    # Delete these temporary variables
    delete $::JOB_PARAMS{'P302_EXECUTE_P302S02T01'};
    delete $::JOB_PARAMS{'P302_EXECUTE_P302S02T02'};
    delete $::JOB_PARAMS{'P302_EXECUTE_P302S02T03'};
    delete $::JOB_PARAMS{'P302_EXECUTE_P302S02T04'};
    delete $::JOB_PARAMS{'P302_NODE_CNT'};
    delete $::JOB_PARAMS{'P302_NODE_IP'};

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
    sub Check_Pods_In_Node_Before_Restart_P302S02T01 {

        my $command = sprintf "%s get pods --all-namespaces -o wide --field-selector spec.nodeName=%s", $::JOB_PARAMS{'KUBECTL_EXECUTABLE'}, $::JOB_PARAMS{'P302_NODE_NAME'};
        my @faults = ();
        my $namespace;
        my $node_name = $::JOB_PARAMS{'P302_NODE_NAME'};
        my $podname;
        my $rc = 0;
        my @result;
        my $status;

        General::Logging::log_user_message("Checking for PODS in node: $::JOB_PARAMS{'P302_NODE_NAME'}");

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
            $::JOB_PARAMS{'P302_MASTER_SKIPPED'} = 1;
            push @::JOB_STATUS, "(-) $::JOB_PARAMS{'TEST_DESCRIPTION'} was skipped because one or more pods are not in Running status";
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
    sub Restart_Node_P302S02T02 {

        my $command = sprintf "ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR %s\@%s 'sudo -n reboot'", $::JOB_PARAMS{'NODE_USER_NAME'}, $::JOB_PARAMS{'P302_NODE_IP'};
        my $rc = 0;

        General::Logging::log_user_message("Restarting node on IP address: $::JOB_PARAMS{'P302_NODE_IP'}");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command",
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
                General::Logging::log_user_message("$::JOB_PARAMS{'TEST_DESCRIPTION'}, restart skipped because DEBUG_PLAYLIST=yes");
                return General::Playlist_Operations::RC_TASKOUT;
            }

            General::Logging::log_user_message("$::JOB_PARAMS{'TEST_DESCRIPTION'} was started");
            push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} was started";

            # Now wait until the restart has started, i.e. that ping fails
            my $stop_time = time() + 120;   # Wait max 2 minutes
            $command = sprintf "ping -c 1 -W 1 %s", $::JOB_PARAMS{'P302_NODE_IP'};
            while ($stop_time > time()) {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "$command",
                        "hide-output"   => 1,
                    }
                );

                if ($rc == 0) {
                    General::Logging::log_user_message("Node is still reachable by 'ping' waiting 1 second before checking if restart has started");
                    $rc = General::OS_Operations::sleep_with_progress(
                        {
                            "allow-interrupt"   => 1,
                            "progress-message"  => 0,
                            "seconds"           => 1,
                            "use-logging"       => 1,
                        }
                    );
                } else {
                    General::Logging::log_user_message("Connection lost for node on IP address $::JOB_PARAMS{'P302_NODE_IP'}, restart in progress");
                    push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} in progress";
                    last;
                }
            }
            $rc =0;
        } elsif ($rc == 255) {
            General::Logging::log_user_message("Connection lost for node on IP address $::JOB_PARAMS{'P302_NODE_IP'}, restart in progress");
            push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} in progress";
            $rc =0;
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$::JOB_PARAMS{'TEST_DESCRIPTION'} failed");
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
    sub Wait_For_Node_To_Become_Reachable_P302S02T03 {

        my $command = sprintf "ping -c 1 -W 5 %s", $::JOB_PARAMS{'P302_NODE_IP'};
        my $node_reachable = 0;
        my $stop_time;
        my $rc = 0;

        General::Logging::log_user_message("Waiting a maximum of $::JOB_PARAMS{'TIMEOUT_NODE_REACHABLE'} seconds for node IP $::JOB_PARAMS{'P302_NODE_IP'} to be pingable");
        #$stop_time = sprintf "%d", (time() + $::JOB_PARAMS{'TIMEOUT_NODE_REACHABLE'});
        $stop_time = time() + $::JOB_PARAMS{'TIMEOUT_NODE_REACHABLE'};

        # First check if node is 'pingable'
        while ($stop_time > time()) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$command",
                    "hide-output"   => 1,
                }
            );

            if ($rc == 0) {
                General::Logging::log_user_message("Node is reachable by 'ping'");
                last;
            } elsif ($stop_time > time()) {
                General::Logging::log_user_message("Node is still not reachable by 'ping' waiting 5 seconds before trying again");
                $rc = General::OS_Operations::sleep_with_progress(
                    {
                        "allow-interrupt"   => 1,
                        "progress-message"  => 0,
                        "seconds"           => 5,
                        "use-logging"       => 1,
                    }
                );
                if ($rc == 1) {
                    General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
                    last;
                }
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Maximum time expired waiting for node to be pingable");
                last;
            }
        }

        if ($stop_time > time()) {
            # Next check if node can be reached by SSH
            General::Logging::log_user_message(sprintf "Waiting a maximum of %d seconds for node IP %s to be reachable by SSH", $stop_time - time(), $::JOB_PARAMS{'P302_NODE_IP'});
            $command = sprintf "ssh-keyscan -T 5 %s", $::JOB_PARAMS{'P302_NODE_IP'};
            while ($stop_time > time()) {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "$command",
                        "hide-output"   => 1,
                    }
                );

                if ($rc == 0) {
                    General::Logging::log_user_message("Node is reachable by 'ssh'");
                    $node_reachable = 1;
                    last;
                } elsif ($stop_time > time()) {
                    General::Logging::log_user_message("Node is still not reachable by 'ssh' waiting 5 seconds before trying again");
                    $rc = General::OS_Operations::sleep_with_progress(
                        {
                            "allow-interrupt"   => 1,
                            "progress-message"  => 0,
                            "seconds"           => 5,
                            "use-logging"       => 1,
                        }
                    );
                    if ($rc == 1) {
                        General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
                        last;
                    }
                } else {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Maximum time expired waiting for node to be reachable by 'ssh'");
                    last;
                }
            }
        }

        if ($rc == 0 && $node_reachable == 1) {
            General::Logging::log_user_message("Node on IP address $::JOB_PARAMS{'P302_NODE_IP'} is reachable");
            push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} was successful";
            return 0;
        } else {
            General::Logging::log_user_error_message("Node on IP address $::JOB_PARAMS{'P302_NODE_IP'} is not reachable");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
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
    sub Check_Pods_In_Node_After_Restart_P302S02T04 {

        my $command = sprintf "%s get pods --all-namespaces -o wide --field-selector spec.nodeName=%s", $::JOB_PARAMS{'KUBECTL_EXECUTABLE'}, $::JOB_PARAMS{'P302_NODE_NAME'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Checking for PODS in node: $::JOB_PARAMS{'P302_NODE_NAME'}");

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
sub Perform_Post_Test_Checks_P302S03 {

    my $rc;

    # This second call will perform post test checks and job cleanup
    $::JOB_PARAMS{'P932_TASK'} = "POST_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    return $rc if $rc < 0;

    if (exists $::JOB_PARAMS{'P302_SKIPPED_MASTERS'} && $::JOB_PARAMS{'P302_SKIPPED_MASTERS'} > 0) {
        # One or more masters has been skipped, mark job as failed
        General::Logging::log_user_error_message("Test case failed because $::JOB_PARAMS{'P302_SKIPPED_MASTERS'} were skipped");
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
sub Fallback001_P302S99 {

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
        'NODE_IP_TO_RESTART' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify which node IP-addresses to restart, if specified it should be a comma
separated list of IP-addresses of the node(s) to be restarted.
The specified IP-address(es) should directly login to the node to restart, and
it cannot ask for a password, so if any authentication is needed it must be done
via SSH authorized key files.
If not specified then playlist will automatically find the IP-addresses of the
master/control-plane nodes and these will be restarted.

On the new (ECCD 2.26 and later) where CAPO is used, i.e. no directors are
used you SHOULD specify the IP-addresses with this parameter and execute this
playlist from another node that have direct access to login to the specified
node IP-addresses.
The IP-addresses SHOULD NOT be the INTERNAL-IP addresses but a externally
accessible IP-address, which can be found from e.g. the LCM node used to
install ECCD on the node by using the command from the LCM node:

openstack server list | grep <stack name>

See also NODE_USER_NAME which specifies the user name to use for the login.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'NODE_USER_NAME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "eccd",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the user to use for login to the node to restart.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TIMEOUT_NODE_REACHABLE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "300",
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

        # ---------------------------------------------------------------------
        'WAIT_TIME_FOR_TRAFFIC_TO_STABILIZE_AFTER_RESTART' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "0",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the time in seconds to wait after the node is reachable
after the restart before starting to check KPI values.
On some nodes it can take a while after finished restart before traffic recovers
fully and this timer can be used to make sure that traffic is recovered before
starting to check the traffic KPIs.
If the value is 0 then no wait is done before checking KPI values.
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

This Playlist performs a robustness test case that verifies master node restarts.

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
