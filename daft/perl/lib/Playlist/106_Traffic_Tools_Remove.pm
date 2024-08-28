package Playlist::106_Traffic_Tools_Remove;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.11
#  Date     : 2024-03-14 11:30:56
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

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    usage
    usage_return_playlist_variables
    );

use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;

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
    $::JOB_PARAMS{'JOBTYPE'} = "TRAFFIC_TOOLS_REMOVE";

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

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P106S01, \&Fallback001_P106S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P106S02, \&Fallback001_P106S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Remove_Tools_P106S03, \&Fallback001_P106S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P106S04, \&Fallback001_P106S99 );
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
sub Initialize_Job_Environment_P106S01 {

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
sub Check_Job_Parameters_P106S02 {

    my $rc = 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_TOOLS_TO_REMOVE_P106S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_TOOLS_NAMESPACE_P106S02T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Tools_Namespace_On_Node_P106S02T03 } );
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
    sub Check_TOOLS_TO_REMOVE_P106S02T01 {
        my $rc = 0;

        General::Logging::log_user_message("Checking Job parameter 'TOOLS_TO_REMOVE'");
        if ($::JOB_PARAMS{'TOOLS_TO_REMOVE'} eq "") {
            General::Logging::log_user_error_message("Job parameter 'TOOLS_TO_REMOVE' not specified");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        $::JOB_PARAMS{'TOOLS_TO_REMOVE'} =~ s/\s+/,/g;
        $::JOB_PARAMS{'TOOLS_TO_REMOVE'} =~ s/,+/,/g;

        # Set default values for all traffic simulators
        $::JOB_PARAMS{'CHFSIM_WANTED'} = "no";
        $::JOB_PARAMS{'DSCLOAD_WANTED'} = "no";
        $::JOB_PARAMS{'K6_WANTED'} = "no";
        $::JOB_PARAMS{'NRFSIM_WANTED'} = "no";
        $::JOB_PARAMS{'SEPPSIM_WANTED'} = "no";
        $::JOB_PARAMS{'SFTP_WANTED'} = "no";

        for my $tool (split /,/, $::JOB_PARAMS{'TOOLS_TO_REMOVE'}) {
            if ($tool eq "chfsim") {
                $::JOB_PARAMS{'CHFSIM_WANTED'} = "yes";
            } elsif ($tool eq "dscload") {
                $::JOB_PARAMS{'DSCLOAD_WANTED'} = "yes";
            } elsif ($tool eq "k6") {
                $::JOB_PARAMS{'K6_WANTED'} = "yes";
            } elsif ($tool eq "nrfsim") {
                $::JOB_PARAMS{'NRFSIM_WANTED'} = "yes";
            } elsif ($tool eq "sc-monitor") {
                $::JOB_PARAMS{'SC_MONITOR_WANTED'} = "yes";
            } elsif ($tool eq "seppsim") {
                $::JOB_PARAMS{'SEPPSIM_WANTED'} = "yes";
            } elsif ($tool eq "sftp") {
                $::JOB_PARAMS{'SFTP_WANTED'} = "yes";
            } else {
                General::Logging::log_user_warning_message("Unknown tool '$tool' specified in job parameter 'TOOLS_TO_INSTALL', it's ignored");
            }
        }

        # Check that we have at least one tool to build and install
        if ($::JOB_PARAMS{'CHFSIM_WANTED'} eq "no" &&
            $::JOB_PARAMS{'DSCLOAD_WANTED'} eq "no" &&
            $::JOB_PARAMS{'K6_WANTED'} eq "no" &&
            $::JOB_PARAMS{'NRFSIM_WANTED'} eq "no" &&
            $::JOB_PARAMS{'SC_MONITOR_WANTED'} eq "no" &&
            $::JOB_PARAMS{'SEPPSIM_WANTED'} eq "no" &&
            $::JOB_PARAMS{'SFTP_WANTED'} eq "no") {

            General::Logging::log_user_error_message("Job parameter 'TOOLS_TO_REMOVE' contains no valid tools: $::JOB_PARAMS{'TOOLS_TO_REMOVE'}");
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
    sub Check_TOOLS_NAMESPACE_P106S02T02 {
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
    sub Check_Tools_Namespace_On_Node_P106S02T03 {
        my $found = 0;
        my $rc = 0;
        my @result;

        # Set default values for all traffic simulators
        $::JOB_PARAMS{'CHFSIM_INSTALLED'} = "no";
        $::JOB_PARAMS{'DSCLOAD_INSTALLED'} = "no";
        $::JOB_PARAMS{'K6_INSTALLED'} = "no";
        $::JOB_PARAMS{'NRFSIM_INSTALLED'} = "no";
        $::JOB_PARAMS{'SEPPSIM_INSTALLED'} = "no";
        $::JOB_PARAMS{'SC_MONITOR_INSTALLED'} = "no";
        $::JOB_PARAMS{'SFTP_INSTALLED'} = "no";
        $::JOB_PARAMS{'UNKNOWN_INSTALLED'} = "no";

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
            #eric-sc-dscload    tools        1           2023-02-06 14:04:25.602831927 +0000 UTC    deployed    eric-dscload-1.11.25-h0d437cf5f5    1.11.25-h0d437cf5f5
            #eric-sc-k6         tools        1           2023-02-06 14:04:50.839935628 +0000 UTC    deployed    eric-k6-1.11.25-h0d437cf5f5         1.11.25-h0d437cf5f5
            #eric-sc-nrfsim     tools        1           2023-02-06 14:05:21.695514038 +0000 UTC    deployed    eric-nrfsim-1.11.25-h0d437cf5f5
            #eric-sc-seppsim    tools        1           2023-02-06 14:06:12.006906013 +0000 UTC    deployed    eric-seppsim-1.11.25-h0d437cf5f5
            #eric-sc-sftp       tools        1           2023-03-23 13:42:06.274408946 +0000 UTC    deployed    eric-atmoz-sftp-1.11.25-h597269b69e.dirty    1.11.25-h597269b69e.dirty
            for (@result) {
                if (/^eric-sc-chfsim\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'CHFSIM_INSTALLED'} = "yes";
                } elsif (/^eric-sc-dscload\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'DSCLOAD_INSTALLED'} = "yes";
                } elsif (/^eric-sc-k6\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'K6_INSTALLED'} = "yes";
                } elsif (/^eric-sc-nrfsim\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'NRFSIM_INSTALLED'} = "yes";
                } elsif (/^eric-sc-monitor\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'SC_MONITOR_INSTALLED'} = "yes";
                } elsif (/^eric-sc-seppsim\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'SEPPSIM_INSTALLED'} = "yes";
                } elsif (/^eric-sc-sftp\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'SFTP_INSTALLED'} = "yes";
                } elsif (/^\S+\s+$::JOB_PARAMS{'TOOLS_NAMESPACE'}\s+\d+\s+.+/) {
                    $::JOB_PARAMS{'UNKNOWN_INSTALLED'} = "yes";
                }
            }
        } else {
            General::Logging::log_user_message("Namespace $::JOB_PARAMS{'TOOLS_NAMESPACE'} does not exist, there is nothing to be done");
            $::JOB_PARAMS{'REMOVE_NAMESPACE'} = "no";
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
sub Remove_Tools_P106S03 {

    my $rc = 0;

    if ($::JOB_PARAMS{'DSCLOAD_WANTED'} eq "yes" && $::JOB_PARAMS{'DSCLOAD_INSTALLED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_DSCLOAD_P106S03T01 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'K6_WANTED'} = "yes" && $::JOB_PARAMS{'K6_INSTALLED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_K6_P106S03T02 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'NRFSIM_WANTED'} eq "yes" && $::JOB_PARAMS{'NRFSIM_INSTALLED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_NRFSIM_P106S03T03 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'SEPPSIM_WANTED'} eq "yes" && $::JOB_PARAMS{'SEPPSIM_INSTALLED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_SEPPSIM_P106S03T04 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'SFTP_WANTED'} eq "yes" && $::JOB_PARAMS{'SFTP_INSTALLED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_SFTP_P106S03T05 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'SC_MONITOR_WANTED'} eq "yes" && $::JOB_PARAMS{'SC_MONITOR_INSTALLED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_SC_MONITOR_P106S03T06 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'CHFSIM_WANTED'} eq "yes" && $::JOB_PARAMS{'CHFSIM_INSTALLED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_CHFSIM_P106S03T07 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'REMOVE_NAMESPACE'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Namespace_P106S03T08 } );
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
    sub Remove_DSCLOAD_P106S03T01 {

        my $rc = 0;
        my $tool = "dscload";

        General::Logging::log_user_message("Removing $tool");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} uninstall -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} eric-sc-$tool",
                "hide-output"   => 1,
             }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) $tool remove was successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to uninstall $tool");
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
    sub Remove_K6_P106S03T02 {

        my $rc = 0;
        my $tool = "k6";

        General::Logging::log_user_message("Removing $tool");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} uninstall -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} eric-sc-$tool",
                "hide-output"   => 1,
             }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) $tool remove was successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to uninstall $tool");
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
    sub Remove_NRFSIM_P106S03T03 {

        my $rc = 0;
        my $tool = "nrfsim";

        General::Logging::log_user_message("Removing $tool");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} uninstall -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} eric-sc-$tool",
                "hide-output"   => 1,
             }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) $tool remove was successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to uninstall $tool");
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
    sub Remove_SEPPSIM_P106S03T04 {

        my $rc = 0;
        my $tool = "seppsim";

        General::Logging::log_user_message("Removing $tool");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} uninstall -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} eric-sc-$tool",
                "hide-output"   => 1,
             }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) $tool remove was successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to uninstall $tool");
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
    sub Remove_SFTP_P106S03T05 {

        my $rc = 0;
        my $tool = "sftp";

        General::Logging::log_user_message("Removing $tool");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} uninstall -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} eric-sc-$tool",
                "hide-output"   => 1,
             }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) $tool remove was successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to uninstall $tool");
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
    sub Remove_SC_MONITOR_P106S03T06 {

        my $rc = 0;
        my $tool = "monitor";

        General::Logging::log_user_message("Removing $tool");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} uninstall -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} eric-sc-$tool",
                "hide-output"   => 1,
             }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) $tool remove was successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to uninstall $tool");
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
    sub Remove_CHFSIM_P106S03T07 {

        my $rc = 0;
        my $tool = "chfsim";

        General::Logging::log_user_message("Removing $tool");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} uninstall -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} eric-sc-$tool",
                "hide-output"   => 1,
             }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) $tool remove was successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to uninstall $tool");
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
    sub Remove_Namespace_P106S03T08 {

        my $rc = 0;

        if ($::JOB_PARAMS{'UNKNOWN_INSTALLED'} eq "yes") {
            $rc = General::OS_Operations::send_command(
                {
                    "command"      => "$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} list -n $::JOB_PARAMS{'TOOLS_NAMESPACE'}",
                    "hide-output"   => 0,
                }
            );
            General::Logging::log_user_warning_message("There are other deployments in the $::JOB_PARAMS{'TOOLS_NAMESPACE'} namespace that will be removed");
        }

        General::Logging::log_user_message("Removing namespace $::JOB_PARAMS{'TOOLS_NAMESPACE'}.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} delete namespace $::JOB_PARAMS{'TOOLS_NAMESPACE'}",
                "hide-output"   => 1,
             }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TOOLS_NAMESPACE'} namespace removal was successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to remove namespace");
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
sub Cleanup_Job_Environment_P106S04 {

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
sub Fallback001_P106S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

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
sub set_playlist_variables {
    %playlist_variables = (
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
        'REMOVE_NAMESPACE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the namespace should be removed after the tools has
been removed.
EOF
            'validity_mask' => '(yes|no)',
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
This parameter controls in which namespace the tools has been installed in on
the node and should be specified if you want to override the parameter from the network
configuration file called 'tools_namespace'.
If you don't specify this parameter then it will take the value from the network
configuration file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TOOLS_TO_REMOVE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
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
    )
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist removes the dscload, k6, nrfsim and seppsim traffic generators
tools and atmoz-sftp server on the IaaS clusters.

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
