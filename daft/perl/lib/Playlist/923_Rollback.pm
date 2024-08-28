package Playlist::923_Rollback;

################################################################################
#
#  Author   : eedjoz, eustone
#
#  Revision : 1.8
#  Date     : 2023-09-20 13:53:24
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021
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
    );

# Used Perl package files
#
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::918_Post_Healthcheck;
use Playlist::919_Check_Registry_Information;

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

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    } else {
        $debug_command = "";
    }

    #
    # Rollback the SC
    #

    $rc = General::Playlist_Operations::execute_step( \&Rollback_SC_P923S01, \&Fallback001_P923S99 );
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
sub Rollback_SC_P923S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::919_Check_Registry_Information::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Rollback_SC_Application_P923S01T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Verify_SC_Application_Rollback_P923S01T02 } );
    return $rc if $rc < 0;

    if (exists $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} && $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::918_Post_Healthcheck::main } );
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
    sub Rollback_SC_Application_P923S01T01 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        my $helm_executable =  $::JOB_PARAMS{'HELM_EXECUTABLE'};
        my $rc = 0;
        my $timeout = $::JOB_PARAMS{'ROLLBACK_TIMEOUT'};
        my $revision = $::JOB_PARAMS{'ROLLBACK_REVISION'};

        General::Logging::log_user_message("Rolling back SC Application.\nThis will take a while to complete.\n");

        if ($::JOB_PARAMS{'HELM_VERSION'} == 2) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}${helm_executable} --namespace $sc_namespace rollback --name $sc_release_name $revision --timeout $timeout --debug",
                    "hide-output"   => 1,
                }
            );
        } elsif ($::JOB_PARAMS{'HELM_VERSION'} == 3) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}${helm_executable} --namespace $sc_namespace rollback $sc_release_name $revision --timeout ${timeout}s --debug",
                    "hide-output"   => 1,
                }
            );
        } else {
            General::Logging::log_user_error_message("No helm version defined or left blank");
            $rc = 1;
        }

        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Rolling back Software successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to rollback SC Application");
            push @::JOB_STATUS, "(x) Rolling back Software failed";
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
    sub Verify_SC_Application_Rollback_P923S01T02 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        my $helm_executable =  $::JOB_PARAMS{'HELM_EXECUTABLE'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Checking deployment status");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${helm_executable} list --all --namespace $sc_namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            my $found = 0;
            for (@result) {
                if ($::JOB_PARAMS{'HELM_VERSION'} == 2) {
                    # NAME        REVISION    UPDATED                     STATUS      CHART                                       APP VERSION             NAMESPACE
                    # chfsim      1           Mon Oct  5 14:44:03 2020    DEPLOYED    eric-chfsim-1.2.25-0f516d8b7-dirty          1.2.25-0f516d8b7-dirty  scp
                    if ($_ =~ /^$sc_release_name\s+\d+\s+.+\s+DEPLOYED\s+eric-\S+-(\d+\.\d+\.\d+)(\S+)\s+$sc_namespace\s*$/) {
                        $::JOB_PARAMS{'SC_RELEASE_VERSION'} = $1;
                        $::JOB_PARAMS{'SC_RELEASE_BUILD'} = $2;
                        $found = 1;
                        last;
                    }
                } elsif ($::JOB_PARAMS{'HELM_VERSION'} == 3) {
                    # NAME    NAMESPACE       REVISION        UPDATED                                 STATUS          CHART                           APP VERSION
                    # eric-sc eiffelesc       1               2020-10-16 14:11:03.296029048 +0000 UTC deployed        eric-sc-umbrella-1.2.2+247
                    if ($_ =~ /^$sc_release_name\s+$sc_namespace\s+\d+\s+.+\s+deployed\s+eric-\S+-(\d+\.\d+\.\d+)(\S+)\s*$/) {
                        $::JOB_PARAMS{'SC_RELEASE_VERSION'} = $1;
                        $::JOB_PARAMS{'SC_RELEASE_BUILD'} = $2;
                        $found = 1;
                        last;
                    }
                }
            }
            if ($found == 1) {
                General::Logging::log_user_message("SC deployment $sc_release_name found in $sc_namespace namespace with release version $::JOB_PARAMS{'SC_RELEASE_VERSION'} and build number $::JOB_PARAMS{'SC_RELEASE_BUILD'}");
                push @::JOB_STATUS, "(/) Checking Deployment Status successful";
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Could not find expected deployment status");
                push @::JOB_STATUS, "(x) Checking Deployment Status failed";
                $rc = 1;
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check deployment status");
            push @::JOB_STATUS, "(x) Checking Deployment Status failed";
        }

        return $rc;
    }
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P923S99 {

    my $rc = 0;

    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    return $rc;
}

#####################
#                   #
# Other Subroutines #
#                   #
#####################

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This playlist performs a rollback of the software on a SC node.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
