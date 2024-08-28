package Playlist::103_Tools_Management;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.17
#  Date     : 2024-04-02 20:43:57
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2022-2024
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

#
# Used Perl package files
#

use ADP::Kubernetes_Operations;
use General::File_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;
use General::String_Operations;

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;
use Playlist::920_Check_Network_Config_Information;

#
# Variable Declarations
#

our %playlist_variables;
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

    # Set job type to be a deployment
    $::JOB_PARAMS{'JOBTYPE'} = "TOOLS_MANAGEMENT";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # If the playlist needs to have access to Network Configuration parameters
    # then it needs to check for this and give an error or ask the user to
    # provide the file name.
    if (Playlist::920_Check_Network_Config_Information::is_network_config_specified() != 0) {
        General::Logging::log_user_warning_message("No network configuration file specified, all mandatory parameters must be given as job parameters or the job will fail");
    }

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P103S01, \&Fallback001_P103S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P103S02, \&Fallback001_P103S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Handle_Tools_P103S03, \&Fallback001_P103S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Collect_Logs_If_Wanted_P103S04, \&Fallback001_P103S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P103S05, \&Fallback001_P103S99 );
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
sub Initialize_Job_Environment_P103S01 {

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
sub Check_Job_Parameters_P103S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P103S02T01 } );
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
    sub Check_Job_Parameters_P103S02T01 {

        my $tools_namespace = "";
        my $rc = 0;

        # Get the proper TOOLS_NAMESPACE
        General::Logging::log_user_message("Checking Job parameter 'TOOLS_NAMESPACE' and Network Config parameter 'tools_namespace' and 'sc_namespace'");
        if ($::JOB_PARAMS{'TOOLS_NAMESPACE'} ne "") {
            $tools_namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'tools_namespace'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'tools_namespace'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'TOOLS_NAMESPACE'} = $::NETWORK_CONFIG_PARAMS{'tools_namespace'}{'value'};
                $tools_namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
            } elsif ($::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'TOOLS_NAMESPACE'} = $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'};
                $tools_namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
            } else {
                if ($::JOB_PARAMS{'DO_ASSIGN_NODESELECTOR'} eq "yes" || $::JOB_PARAMS{'DO_REMOVE_NODESELECTOR'} eq "yes") {
                    # The TOOLS_NAMESPACE is needed but not specified
                    General::Logging::log_user_error_message("Network Config parameter 'tools_namespace' or 'sc_namespace' has not been set and Job parameter 'SC_NAMESPACE' or 'TOOLS_NAMESPACE' not provided");
                    return General::Playlist_Operations::RC_FALLBACK;
                }
            }
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'TOOLS_NAMESPACE'} = $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'};
                $tools_namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
            } else {
                if ($::JOB_PARAMS{'DO_ASSIGN_NODESELECTOR'} eq "yes" || $::JOB_PARAMS{'DO_REMOVE_NODESELECTOR'} eq "yes") {
                    # The TOOLS_NAMESPACE is needed but not specified
                    General::Logging::log_user_error_message("Network Config parameter 'sc_namespace' has not been set and Job parameter 'SC_NAMESPACE' or 'TOOLS_NAMESPACE' not provided");
                    return General::Playlist_Operations::RC_FALLBACK;
                }
            }
        } else {
            if ($::JOB_PARAMS{'DO_ASSIGN_NODESELECTOR'} eq "yes" || $::JOB_PARAMS{'DO_REMOVE_NODESELECTOR'} eq "yes") {
                # The TOOLS_NAMESPACE is needed but not specified
                General::Logging::log_user_error_message("Neither Job parameter 'SC_NAMESPACE' or 'TOOLS_NAMESPACE', nor Network Config parameter 'sc_namespace' has been set");
                return General::Playlist_Operations::RC_FALLBACK;
            }
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
sub Handle_Tools_P103S03 {

    my $rc = 0;

    if ($::JOB_PARAMS{'DO_REMOVE_TAINT'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Taint_From_Nodes_P103S03T01 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'DO_REMOVE_NODESELECTOR'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Node_Selector_From_Deployment_P103S03T02 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'DO_REMOVE_LABEL'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Label_From_Nodes_P103S03T03 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'DO_ASSIGN_LABEL'} eq "yes" || $::JOB_PARAMS{'DO_ASSIGN_TAINT'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Find_Nodes_To_Label_Or_Taint_P103S03T04 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'DO_ASSIGN_LABEL'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Assign_Label_To_Nodes_P103S03T05 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'DO_ASSIGN_TAINT'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Assign_Taint_To_Nodes_P103S03T06 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'DO_ASSIGN_NODESELECTOR'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Assign_Node_Selector_To_Deployment_P103S03T07 } );
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
    sub Remove_Taint_From_Nodes_P103S03T01 {

        my $error_cnt = 0;
        my $taints = "$::JOB_PARAMS{'NODE_LABEL'}:$::JOB_PARAMS{'NODE_TAINTS'}";
        my @node_names = ();
        my $rc = 0;
        my @result;

        if ($::JOB_PARAMS{'NODE_NAMES'} ne "") {
            # The user selected which nodes to remove the label from.
            @node_names = split /,/, $::JOB_PARAMS{'NODE_NAMES'};
        } else {
            # The user did not specify which nodes to remove the label from, so we
            # need to figure out which nodes already have the labels.
            @node_names = ADP::Kubernetes_Operations::get_nodes_with_taints($taints);
        }

        if (@node_names) {
            for my $name (@node_names) {
                General::Logging::log_user_message("Remove taint '$taints' from node $name");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} taint node $name $taints-",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to remove taint");
                    $error_cnt++;
                }
            }
        } else {
            General::Logging::log_user_message("No nodes found with taint '$taints'");
            push @::JOB_STATUS, '(-) Removal of Taint from nodes skipped';
        }

        if ($error_cnt == 0) {
            push @::JOB_STATUS, '(/) Removal of Taint from nodes successful' if (@node_names);
            return 0;
        } else {
            push @::JOB_STATUS, '(x) Removal of Taint from nodes failed';
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
    sub Remove_Node_Selector_From_Deployment_P103S03T02 {

        my @deployments = ();
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/label_deployment_tools_remove.yaml";
        my $label = $::JOB_PARAMS{'NODE_LABEL'};
        my $message;
        my $namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
        my $rc = 0;
        my @result;

        # Find tool deployments in the namespace
        General::Logging::log_user_message("Finding Tool Deployments in namespace '$namespace'");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} get deployment -n $namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get deployments");
            return 1;
        }
        for (@result) {
            if (/^(chfsim-\d+|eric-dscload-deployment|eric-influxdb|eric-k6-deployment|eric-nrfsim|eric-seppsim-\S+)\s+\d+\/\d+\s+\d+\s+\d+\s+\S+\s*$/) {
                #NAME                                               READY   UP-TO-DATE   AVAILABLE   AGE
                #eric-influxdb                                      1/1     1            1           5h26m
                #eric-k6-deployment                                 4/4     4            4           5h26m
                #eric-nrfsim                                        1/1     1            1           5h26m
                #eric-seppsim-c                                     1/1     1            1           5h26m
                #eric-seppsim-p                                     1/1     1            1           5h26m
                #eric-seppsim-p1                                    1/1     1            1           5h26m
                #eric-seppsim-p2                                    1/1     1            1           5h26m
                #eric-seppsim-p3                                    1/1     1            1           5h26m
                #eric-seppsim-p4                                    1/1     1            1           5h26m
                #eric-seppsim-p5                                    1/1     1            1           5h26m
                #eric-seppsim-p6                                    1/1     1            1           5h26m
                #eric-seppsim-p7                                    1/1     1            1           5h26m
                #eric-seppsim-p8                                    1/1     1            1           5h26m
                push @deployments, $1;
            }
        }

        if (@deployments) {
            $label =~ s/^(\S+)=(\S+)\s*$/$1:/;
            $rc = General::File_Operations::write_file(
                {
                    "filename"      => $filename,
                    "output-ref"    => [
                        "spec:",
                        "  template:",
                        "    spec:",
                        "      nodeSelector:",
                        "        $label"
                    ],
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to write file '$filename'");
                return 1;
            }
            for my $name (@deployments) {
                General::Logging::log_user_message("Removing nodeSelector '$label' from deployment $name");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} patch deployment $name -n $namespace --patch \"\$(cat $filename)\"",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to remove nodeSelector");
                    push @::JOB_STATUS, '(x) Removal of nodeSelector from deployments failed';
                    return 1;
                }
            }
            push @::JOB_STATUS, '(/) Removal of nodeSelector from deployments successful';
        } else {
            General::Logging::log_user_message("No tool deployments found");
            push @::JOB_STATUS, '(-) Removal of nodeSelector from deployments skipped';
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
    sub Remove_Label_From_Nodes_P103S03T03 {

        my $error_cnt = 0;
        my $label = $::JOB_PARAMS{'NODE_LABEL'};
        my @node_names = ();
        my $rc = 0;
        my @result;

        if ($::JOB_PARAMS{'NODE_NAMES'} ne "") {
            # The user selected which nodes to remove the label from.
            @node_names = split /,/, $::JOB_PARAMS{'NODE_NAMES'};
        } else {
            # The user did not specify which nodes to remove the label from, so we
            # need to figure out which nodes already have the labels.
            @node_names = ADP::Kubernetes_Operations::get_nodes_with_label($label);
        }

        if (@node_names) {
            $label =~ s/=\S+//;
            for my $name (@node_names) {
                General::Logging::log_user_message("Remove label '$label' from node $name");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} label node $name $label-",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to remove label");
                    $error_cnt++;
                }
            }
        } else {
            General::Logging::log_user_message("No nodes found with label '$label'");
            push @::JOB_STATUS, '(-) Removal of Label from nodes skipped';
        }

        if ($error_cnt == 0) {
            push @::JOB_STATUS, '(/) Removal of Label from nodes successful' if (@node_names);
            return 0;
        } else {
            push @::JOB_STATUS, '(x) Removal of Label from nodes failed';
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
    sub Find_Nodes_To_Label_Or_Taint_P103S03T04 {

        my $label = $::JOB_PARAMS{'NODE_LABEL'};
        my $message;
        my @node_names = ();
        my $rc = 0;
        my @result;
        my $taint = "$label:$::JOB_PARAMS{'NODE_TAINTS'}";

        if ($::JOB_PARAMS{'NODE_NAMES'} ne "") {
            # The user selected which nodes to assign the tools to
            @node_names = split /,/, $::JOB_PARAMS{'NODE_NAMES'};
        } else {
            # The user did not specify which nodes to use.
            # So we need to figure out which nodes to use based on the nodes with the
            # most available resources.
            my %node_resources = ADP::Kubernetes_Operations::get_node_resources( { "type" => $::JOB_PARAMS{'NODE_TYPE'} } );
            my @sorted_low_to_high = ();
            if ($::JOB_PARAMS{'PREFER_LOWEST'} eq "cpu") {
                foreach my $name (sort { $node_resources{$a}{'Allocated resources'}{'cpu Requests Percent'} <=> $node_resources{$b}{'Allocated resources'}{'cpu Requests Percent'} } keys %node_resources) {
                    push @sorted_low_to_high, $name;
                }
            } elsif ($::JOB_PARAMS{'PREFER_LOWEST'} eq "memory") {
                foreach my $name (sort { $node_resources{$a}{'Allocated resources'}{'memory Requests Percent'} <=> $node_resources{$b}{'Allocated resources'}{'memory Requests Percent'} } keys %node_resources) {
                    push @sorted_low_to_high, $name;
                }
            } elsif ($::JOB_PARAMS{'PREFER_LOWEST'} eq "name") {
                if ($::JOB_PARAMS{'NODE_TYPE'} eq "master") {
                    @sorted_low_to_high = ADP::Kubernetes_Operations::get_master_nodes();
                } elsif ($::JOB_PARAMS{'NODE_TYPE'} eq "worker") {
                    @sorted_low_to_high = ADP::Kubernetes_Operations::get_worker_nodes();
                } else {
                    @sorted_low_to_high = ADP::Kubernetes_Operations::get_nodes();
                }
            }
            @node_names = @sorted_low_to_high[0 .. $::JOB_PARAMS{'NODES_TO_SELECT'}-1];

            $message = "Nodes selected based on lowest $::JOB_PARAMS{'PREFER_LOWEST'} usage:\n";
            for my $name (@node_names) {
                $::JOB_PARAMS{'NODE_NAMES'} .= "$name,";

                if ($::JOB_PARAMS{'PREFER_LOWEST'} eq "cpu") {
                    $message .= "  $name: CPU=$node_resources{$name}{'Allocated resources'}{'cpu Requests Percent'}%, Memory=$node_resources{$name}{'Allocated resources'}{'memory Requests Percent'}%\n";
                } elsif ($::JOB_PARAMS{'PREFER_LOWEST'} eq "memory") {
                    $message .= "  $name: Memory=$node_resources{$name}{'Allocated resources'}{'memory Requests Percent'}%, CPU=$node_resources{$name}{'Allocated resources'}{'cpu Requests Percent'}%\n";
                } elsif ($::JOB_PARAMS{'PREFER_LOWEST'} eq "name") {
                    $message .= "  $name: CPU=$node_resources{$name}{'Allocated resources'}{'cpu Requests Percent'}%, Memory=$node_resources{$name}{'Allocated resources'}{'memory Requests Percent'}%\n";
                }
            }
            General::Logging::log_user_message($message);
            $::JOB_PARAMS{'NODE_NAMES'} =~ s/,$//;
        }

        if (@node_names) {
            General::Logging::log_user_message("Nodes selected:\n" . join "\n", @node_names);
        } else {
            General::Logging::log_user_message("No nodes found");
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
    sub Assign_Label_To_Nodes_P103S03T05 {

        my $label = $::JOB_PARAMS{'NODE_LABEL'};
        my $message;
        my @node_names = ();
        my $rc = 0;
        my @result;

        if ($::JOB_PARAMS{'NODE_NAMES'} ne "") {
            # The user selected which nodes to assign the tools to
            @node_names = split /,/, $::JOB_PARAMS{'NODE_NAMES'};
        }

        if (@node_names) {
            for my $name (@node_names) {
                General::Logging::log_user_message("Assigning label '$label' to node $name");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} label node $name $label --overwrite",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to assign label");
                    push @::JOB_STATUS, '(x) Assign label to node failed';
                    return 1;
                }
            }
            push @::JOB_STATUS, '(/) Assign label to nodes successful';
        } else {
            General::Logging::log_user_message("No nodes found");
            push @::JOB_STATUS, '(-) Assign label to nodes skipped';
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
    sub Assign_Taint_To_Nodes_P103S03T06 {

        my $label = $::JOB_PARAMS{'NODE_LABEL'};
        my $message;
        my @node_names = ();
        my $rc = 0;
        my @result;
        my $taint = "$label:$::JOB_PARAMS{'NODE_TAINTS'}";

        if ($::JOB_PARAMS{'NODE_NAMES'} ne "") {
            # The user selected which nodes to assign the tools to
            @node_names = split /,/, $::JOB_PARAMS{'NODE_NAMES'};
        }

        if (@node_names) {
            for my $name (@node_names) {
                General::Logging::log_user_message("Assigning taint '$taint' to node $name");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} taint nodes $name $taint --overwrite",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to assign taint");
                    push @::JOB_STATUS, '(x) Assign taint to node failed';
                    return 1;
                }
            }
            push @::JOB_STATUS, '(/) Assign taint to nodes successful';
        } else {
            General::Logging::log_user_message("No nodes found");
            push @::JOB_STATUS, '(-) Assign taint to nodes skipped';
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
    sub Assign_Node_Selector_To_Deployment_P103S03T07 {

        my @deployments = ();
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/label_deployment_tools_add.yaml";
        my $label = $::JOB_PARAMS{'NODE_LABEL'};
        my $message;
        my $namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
        my $rc = 0;
        my @result;

        # Find tool deployments in the namespace
        General::Logging::log_user_message("Finding Tool Deployments in namespace '$namespace'");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} get deployment -n $namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get deployments");
            return 1;
        }
        for (@result) {
            if (/^(chfsim-\d+|eric-dscload-deployment|eric-influxdb|eric-k6-deployment|eric-nrfsim|eric-seppsim-\S+)\s+\d+\/\d+\s+\d+\s+\d+\s+\S+\s*$/) {
                #NAME                                               READY   UP-TO-DATE   AVAILABLE   AGE
                #eric-influxdb                                      1/1     1            1           5h26m
                #eric-k6-deployment                                 4/4     4            4           5h26m
                #eric-nrfsim                                        1/1     1            1           5h26m
                #eric-seppsim-c                                     1/1     1            1           5h26m
                #eric-seppsim-p                                     1/1     1            1           5h26m
                #eric-seppsim-p1                                    1/1     1            1           5h26m
                #eric-seppsim-p2                                    1/1     1            1           5h26m
                #eric-seppsim-p3                                    1/1     1            1           5h26m
                #eric-seppsim-p4                                    1/1     1            1           5h26m
                #eric-seppsim-p5                                    1/1     1            1           5h26m
                #eric-seppsim-p6                                    1/1     1            1           5h26m
                #eric-seppsim-p7                                    1/1     1            1           5h26m
                #eric-seppsim-p8                                    1/1     1            1           5h26m
                push @deployments, $1;
            }
        }

        if (@deployments) {
            $label =~ s/^(\S+)=(\S+)\s*$/$1: $2/;
            $rc = General::File_Operations::write_file(
                {
                    "filename"      => $filename,
                    "output-ref"    => [
                        "spec:",
                        "  template:",
                        "    spec:",
                        "      nodeSelector:",
                        "        $label"
                    ],
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to write file '$filename'");
                return 1;
            }
            for my $name (@deployments) {
                General::Logging::log_user_message("Assigning nodeSelector '$label' to deployment $name");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} patch deployment $name -n $namespace --patch \"\$(cat $filename)\"",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to assign nodeSelector");
                    push @::JOB_STATUS, '(x) Assign nodeSelector to deployments failed';
                    return 1;
                }
            }
            push @::JOB_STATUS, '(/) Assign nodeSelector to deployments successful';
        } else {
            General::Logging::log_user_message("No tool deployments found");
            push @::JOB_STATUS, '(-) Assign nodeSelector to deployments skipped';
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
sub Collect_Logs_If_Wanted_P103S04 {

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
sub Cleanup_Job_Environment_P103S05 {

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
sub Fallback001_P103S99 {

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
    return Playlist::920_Check_Network_Config_Information::parse_network_config_parameters();
}

# -----------------------------------------------------------------------------
sub set_playlist_variables {
    %playlist_variables = (
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
        'DEBUG_PLAYLIST' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control if the playlist execution should be
executed as normal (=no) or if special handling should be applied (=yes).

If the value is "no" which is also the default value then normal playlist
execution will be done and all applicable commands will be executed.

If the value is "yes" then playlist execution will be altered so that any
command that changes the data on the running cluster will not be executed and
instead will just be echoed to the log files and no change is done, i.e. no
loading of certificates will take place.
Any command that will just change files or data on the job workspace directory
or any command that just reads data from the cluster will still be executed.
This parameter can be useful to check if the overall logic is working without
doing any changes to the running system.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DO_ASSIGN_LABEL' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control if the playlist should assign a label to
a selection of nodes (=yes) or if this should be skipped (=no).
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DO_ASSIGN_NODESELECTOR' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control if the playlist should assign a nodeSelector
to a selection of deployments (=yes) or if this should be skipped (=no).
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DO_ASSIGN_TAINT' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control if the playlist should assign a taint to
a selection of nodes (=yes) or if this should be skipped (=no).
This parameter must be used together with the 'DO_ASSIGN_LABEL=yes' or it will
have no affect.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DO_REMOVE_LABEL' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control if the playlist should remove a label from
a selection of nodes (=yes) or if this should be skipped (=no).
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DO_REMOVE_NODESELECTOR' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control if the playlist should remove a nodeSelector
from a selection of deployments (=yes) or if this should be skipped (=no).
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DO_REMOVE_TAINT' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control if the playlist should remove a taint from
a selection of nodes (=yes) or if this should be skipped (=no).
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
        'NODE_LABEL' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "usage=tools",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The label to assign or remove to/from the nodes where the tools are placed.
EOF
            'validity_mask' => '\S+=\S+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'NODE_NAMES' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The name of the nodes to use for placing the tools on, if more than one node is
specified then separate each name with a comma (,).
If you don't specify this parameter then it will select the node names from
the nodes with the least number of used resources from the type specified in
'NODE_TYPE'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'NODE_TAINTS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "None",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls how the PODs can be assigned to the nodes where the label
is assigned to and it can take the following values:

    None: No special taints are applied to the nodes which means that only the
          POds assigned with the special 'NODE_LABEL' will be affected.

    NoSchedule: If there is at least one un-ignored taint with effect NoSchedule,
          then Kubernetes will not schedule the pod onto that node.

    PreferNoSchedule: If there is at least one un-ignored taint with effect
          PreferNoSchedule, then Kubernetes will try to not schedule the pod onto
          the node but if it does not find any other suitable node, it will
          schedule the pod in that node.

    NoExecute: If there is at least one un-ignored taint with effect NoExecute,
          then the pod will not be scheduled on that node as in NoSchedule and
          along with this if there are some pods which are running on the node
          without suitable toleration, those will also be evicted. This can
          happen in case pods are scheduled on node before taint was applied on
          the node.
EOF
            'validity_mask' => '(None|NoSchedule|PreferNoSchedule|NoExecute)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'NODES_TO_SELECT' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "2",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The number of nodes to select for tool usage.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'NODE_TYPE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "worker",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The type of node to select to use for tools.
EOF
            'validity_mask' => '(master|worker)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'PREFER_LOWEST' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "name",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Which lowest resource usage value to prefer when selecting the node.
    'cpu':    Select the nodes based on the lowest allocated resources of cpu requests.
    'memory': Select the nodes based on the lowest allocated resources of memory requests.
    'name':   Select the nodes based on the name of the node sorted in alphabetical order.
EOF
            'validity_mask' => '(cpu|memory|name)',
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
The parameter is only needed when using either DO_ASSIGN_NODESELECTOR=yes or
DO_REMOVE_NODESELECTOR=yes.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist script perform a number of tasks to add and remove taints, node
selectors and labels on specific nodes on a cluster where tool pods will be
placed so these specific nodes can e.g. be excluded from disturbances like
restarts, drains etc. since these types of actions might affect the traffic.

Used Job Parameters:
====================
EOF
    General::Playlist_Operations::print_info_about_job_variables(\%playlist_variables);
    General::Playlist_Operations::print_used_job_variables(__FILE__, \%playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

# -----------------------------------------------------------------------------
sub usage_return_playlist_variables {
    return %playlist_variables;
}

1;
