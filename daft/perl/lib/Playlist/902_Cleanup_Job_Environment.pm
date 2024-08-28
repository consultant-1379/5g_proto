package Playlist::902_Cleanup_Job_Environment;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.9
#  Date     : 2023-01-19 10:58:08
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2023
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

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Cleanup_DAFT_Server_P902S01, undef );
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
sub Cleanup_DAFT_Server_P902S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Software_Package_on_DAFT_Server_P902S01T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Tools_on_DAFT_Server_P902S01T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Update_Job_Parameters_File_P902S01T03 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Possible_Empty_Directories_on_DAFT_Server_P902S01T04 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Update_Job_Status_P902S01T05 } );
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
    sub Remove_Software_Package_on_DAFT_Server_P902S01T01 {

        my $rc = 0;

        if (-d "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software") {
            General::Logging::log_user_message("Removing software directory\n");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "rm -fr $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software",
                    "hide-output"   => 1,
                }
            );
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
    sub Remove_Tools_on_DAFT_Server_P902S01T02 {

        my $rc = 0;

        if (-d "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/tools") {
            General::Logging::log_user_message("Removing tools directory\n");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "rm -fr $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/tools",
                    "hide-output"   => 1,
                }
            );
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
    sub Update_Job_Parameters_File_P902S01T03 {

        my $job_param_file = "$::JOB_PARAMS{'_JOB_PARAMS_FILE'}";
        my $rc = 0;

        if (-f "$job_param_file") {
            General::Logging::log_user_message("Updating job_parameters.conf file\n");
            $rc = General::Playlist_Operations::job_parameters_write($job_param_file, \%::JOB_PARAMS);
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
    sub Remove_Possible_Empty_Directories_on_DAFT_Server_P902S01T04 {

        General::OS_Operations::send_command(
            {
                "commands"       => [
                    "rmdir --ignore-fail-on-non-empty $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/troubleshooting_logs",
                ],
                "ignore-errors" => 1,
                "hide-output"   => 1,
            }
        );

        # Ignore any errors since it's not important
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
    sub Update_Job_Status_P902S01T05 {

        my $rc;

        General::Logging::log_user_message("Updating Job Status Information");

        push @::JOB_STATUS, "" unless ($::JOB_STATUS[$#::JOB_STATUS] eq "");

        # Add software release information if existing
        if (exists $::JOB_PARAMS{'SC_RELEASE_VERSION'} && $::JOB_PARAMS{'SC_RELEASE_VERSION'} ne "") {
            push @::JOB_STATUS, (
                "Software Version Information",
                "============================",
                "",
            );
            if (exists $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} && $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} ne "") {
                push @::JOB_STATUS, (
                    "Before",
                    "------",
                    "SC Release Version: $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}",
                    "SC Release Build:   $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'}",
                    "",
                    "After",
                    "-----",
                    "SC Release Version: $::JOB_PARAMS{'SC_RELEASE_VERSION'}",
                    "SC Release Build:   $::JOB_PARAMS{'SC_RELEASE_BUILD'}",
                );
            } else {
                push @::JOB_STATUS, (
                    "SC Release Version: $::JOB_PARAMS{'SC_RELEASE_VERSION'}",
                    "SC Release Build:   $::JOB_PARAMS{'SC_RELEASE_BUILD'}",
                );
            }

            if (exists $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} && $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} ne "") {
                push @::JOB_STATUS, "" unless ($::JOB_STATUS[$#::JOB_STATUS] eq "");
                push @::JOB_STATUS, "ECCD Image Release: $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}";
            }
        }

        push @::JOB_STATUS, "" unless ($::JOB_STATUS[$#::JOB_STATUS] eq "");

        if (-f "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/extra_status_messages.txt") {
            # Append the extra messages
            $rc = General::File_Operations::read_file(
                {
                    "filename"          => "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/extra_status_messages.txt",
                    "output-ref"        => \@::JOB_STATUS,
                    "append-output-ref" => 1,
                }
            );
        }

        push @::JOB_STATUS, "" unless ($::JOB_STATUS[$#::JOB_STATUS] eq "");

        # Set return code to continue executing next task
        $rc = 0;

        return $rc;
    }
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

This Playlist script is used to cleanup the environment for a job which involes
e.g. deleting no longer needed files and directories.

This playlist can be called from all main playlists.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
