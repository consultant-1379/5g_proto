package Playlist::903_Global_Preprocess;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.7
#  Date     : 2022-09-14 15:52:49
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2022
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
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;
use Playlist::909_Execute_Playlist_Extensions;
use Playlist::917_Pre_Healthcheck;

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

    $rc = General::Playlist_Operations::execute_step( \&Show_Message_P903S01, undef );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Health_Check_P903S02, undef );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Process_Playlist_Extensions_Command_Files_P903S03, undef );
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
sub Show_Message_P903S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Show_Tasks_To_Be_Done_P903S01T01 } );
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
    sub Show_Tasks_To_Be_Done_P903S01T01 {

        my $message = "";
        my $rc = 0;

        General::Message_Generation::print_message_box(
            {
                "header-text"   => "Global Pre Processing",
                "messages"      => [
                    "",
                    "Tasks to be done:",
                    "- Execute playlist extensions command files, if any",
                    "",
                ],
                "align-text"    => "left",
                "return-output" => \$message,
            }
        );
        General::Logging::log_user_message($message);

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
sub Perform_Pre_Health_Check_P903S02 {

    my $rc = 0;

    if (exists $::JOB_PARAMS{'SKIP_PRE_HEALTHCHECK'} && $::JOB_PARAMS{'SKIP_PRE_HEALTHCHECK'} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::917_Pre_Healthcheck::main } );
        return $rc if $rc < 0;
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
sub Process_Playlist_Extensions_Command_Files_P903S03 {

    my $app_type = exists $::JOB_PARAMS{'APPLICATION_TYPE'} ? $::JOB_PARAMS{'APPLICATION_TYPE'} : "sc";
    my $rc;

    # Set 909 playlist parameters to get default behaviour
    $::JOB_PARAMS{'P909_HIDE_ERROR_MESSAGES'} = "no";
    $::JOB_PARAMS{'P909_JUST_PRINT'} = "no";
    $::JOB_PARAMS{'P909_MATCH_REGEX'} = "";
    $::JOB_PARAMS{'P909_SHOW_APPLIED_FILTERS'} = "yes";
    $::JOB_PARAMS{'P909_SHOW_COMMAND_EXECUTION'} = "no";
    $::JOB_PARAMS{'P909_SHOW_IGNORED_FILES'} = "no";
    $::JOB_PARAMS{'P909_SHOW_PROGRESS'} = "yes";
    $::JOB_PARAMS{'P909_SHOW_SKIPPED_FILES'} = "no";
    $::JOB_PARAMS{'P909_SHOW_VALID_FILES'} = "no";
    $::JOB_PARAMS{'P909_STOP_ON_ERROR'} = "yes";

    $::JOB_PARAMS{'PLAYLIST_EXTENSION_PATH'} = "$::JOB_PARAMS{'_PACKAGE_DIR'}/playlist_extensions/$app_type/global/pre";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::909_Execute_Playlist_Extensions::main } );
    return $rc if $rc < 0;

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

This Playlist is used for executing tasks before any Deployment, Upgrade
or Rollback is performed.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
