package Playlist::904_Global_Postprocess;

################################################################################
#
#  Author   : eustone, everhel
#
#  Revision : 1.92
#  Date     : 2022-09-14 15:52:49
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2018-2022
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
use Playlist::918_Post_Healthcheck;

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

    $rc = General::Playlist_Operations::execute_step( \&Show_Message_P904S01, undef );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Process_Playlist_Extensions_Command_Files_P904S02, undef );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Environment_P904S03, undef );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Mark_Job_Status_As_Successful_P904S04, undef );
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
sub Show_Message_P904S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Show_Tasks_To_Be_Done_P904S01T01 } );
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
    sub Show_Tasks_To_Be_Done_P904S01T01 {

        my $message = "";
        my $rc = 0;

        General::Message_Generation::print_message_box(
            {
                "header-text"   => "Global Post Processing",
                "messages"      => [
                    "",
                    "Tasks to be done:",
                    "- Execute playlist extensions command files, if any",
                    "- Check node health",
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
sub Process_Playlist_Extensions_Command_Files_P904S02 {

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

    $::JOB_PARAMS{'PLAYLIST_EXTENSION_PATH'} = "$::JOB_PARAMS{'_PACKAGE_DIR'}/playlist_extensions/$app_type/global/post";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::909_Execute_Playlist_Extensions::main } );
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
sub Check_Environment_P904S03 {

    my $rc = 0;
    my $skip_check = 0;

    # Check if we should skip the health check for some reason
    if (exists $::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_SC'} && $::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_SC'} eq "yes") {
        # Deployment or Upgrade of SC not performed, makes no sense to do a post health check
        $skip_check = 1;
        $rc = General::Playlist_Operations::RC_STEPOUT;
    }
    if (exists $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} && $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} eq "yes") {
        # Post health check not wanted
        $skip_check = 1;
        $rc = General::Playlist_Operations::RC_STEPOUT;
    }

    if ($skip_check == 0) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::918_Post_Healthcheck::main } );
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
sub Mark_Job_Status_As_Successful_P904S04 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Set_JOBSTATUS_As_Successful_P904S04T01 } );
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
    sub Set_JOBSTATUS_As_Successful_P904S04T01 {

        $::JOB_PARAMS{'JOBSTATUS'} = "SUCCESSFUL";

        return 0;
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

This Playlist is used for executing tasks after Deployment, Upgrade
or Rollback has been performed.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
