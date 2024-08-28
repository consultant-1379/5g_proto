package Playlist::401_Maintainability_Test_Backup_And_Restore;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.15
#  Date     : 2023-09-20 13:53:24
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2022-2023
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
    $::JOB_PARAMS{'JOBTYPE'} = "MAINTAINABILITY_TEST_BACKUP_AND_RESTORE";

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Test_Checks_P401S01, \&Fallback001_P401S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Test_Case_P401S02, \&Fallback001_P401S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Checks_P401S03, \&Fallback001_P401S99 );
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
sub Perform_Pre_Test_Checks_P401S01 {

    my $rc;

    # This first call will initialize job environment and perform pre test checks
    $::JOB_PARAMS{'P932_TASK'} = "PRE_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
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
sub Perform_Test_Case_P401S02 {

    my $length;
    my $message;
    my $old_skip_kpi_verdict_check = $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'};
    my $old_skip_collect_logs = $::JOB_PARAMS{'SKIP_COLLECT_LOGS'};
    my $old_repeated_check_time = $::JOB_PARAMS{'POD_UP_TIMER'};
    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P401S02T01 } );
    return $rc if $rc < 0;

    # Set start values
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T02'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T02'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T02'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T03'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T03'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T03'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T04'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T04'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T04'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T05'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T05'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T05'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T06'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T06'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T06'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T07'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T07'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T07'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T08'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T08'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T08'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T09'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T09'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T09'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T10'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T10'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T10'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T11'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T11'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T11'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T12'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T12'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T12'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T13'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T13'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T13'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T14'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T14'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T14'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T15'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T15'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T15'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T16'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T16'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T16'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T17'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T17'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T17'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T18'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T18'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T18'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T19'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T19'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T19'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T20'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T20'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T20'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T21'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T21'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T21'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T22'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T22'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T22'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T23'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T23'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T23'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T24'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T24'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T24'} : 1;
    $::JOB_PARAMS{'P401_EXECUTE_P401S02T25'} = exists $::JOB_PARAMS{'P401_EXECUTE_P401S02T25'} ? $::JOB_PARAMS{'P401_EXECUTE_P401S02T25'} : 1;

    #
    # Manual Backup and Restore
    #

    # Set start time for this repetition in case we want to have KPI verdicts for each repetition
    $::JOB_PARAMS{'KPI_START_TIME'} = time();

    # Set the description to be used for status messages
    $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Maintainability Test Case %d (%s)", 1, "Manual Backup and Restore";

    $message = sprintf "# %s #", $::JOB_PARAMS{'TEST_DESCRIPTION'};
    $length = length($message);
    General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T02'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_sftp_Server_P401S02T02 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T02'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T03'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_CMYP_CLI_Access_P401S02T03 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T03'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T04'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Configuration_At_Start_P401S02T04 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T04'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T05'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Available_Backup_Slots_P401S02T05 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T05'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T06'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Manual_Backup_1_P401S02T06 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T06'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T07'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Export_Manual_Backup_1_P401S02T07 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T07'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T08'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Add_New_Test_User_P401S02T08 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T08'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T09'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Manual_Backup_2_P401S02T09 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T09'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T10'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Export_Manual_Backup_2_P401S02TT10 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T10'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T11'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Restore_Manual_Backup_1_P401S02T11 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T11'} = 0;

        # This call will wait for all PODs to be up and then check node health
        $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'} = "yes";
        $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = "yes";
        $::JOB_PARAMS{'POD_UP_TIMER'} = 15;
        $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
        $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'} = $old_skip_kpi_verdict_check;
        $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = $old_skip_collect_logs;
        $::JOB_PARAMS{'POD_UP_TIMER'} = $old_repeated_check_time;
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T12'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Non_Existence_Of_Test_User_P401S02T12 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T12'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T13'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Import_Manual_Backup_2_P401S02T13 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T13'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T14'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Restore_Manual_Backup_2_P401S02T14 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T14'} = 0;

        # This call will wait for all PODs to be up and then check node health
        $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'} = "yes";
        $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = "yes";
        $::JOB_PARAMS{'POD_UP_TIMER'} = 15;
        $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
        $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'} = $old_skip_kpi_verdict_check;
        $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = $old_skip_collect_logs;
        $::JOB_PARAMS{'POD_UP_TIMER'} = $old_repeated_check_time;
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T15'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Existence_Of_Test_User_P401S02T15 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T15'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T16'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Delete_Manual_Backup_1_P401S02T16 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T16'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T17'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Delete_Manual_Backup_2_P401S02T17 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T17'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T18'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Restore_Available_Backup_Slots_If_Needed_P401S02T18 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T18'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T19'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Delete_New_Test_User_P401S02T19 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T19'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T20'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Manual_Backup_3_P401S02T20 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T20'} = 0;

        # This call will wait for all PODs to be up and then check node health
        $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'} = "yes";
        $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = "yes";
        $::JOB_PARAMS{'POD_UP_TIMER'} = 15;
        $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
        $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'} = $old_skip_kpi_verdict_check;
        $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = $old_skip_collect_logs;
        $::JOB_PARAMS{'POD_UP_TIMER'} = $old_repeated_check_time;
        return $rc if $rc < 0;
    }

    #
    # Scheduled Backups
    #

    # Set start time for this repetition in case we want to have KPI verdicts for each repetition
    $::JOB_PARAMS{'KPI_START_TIME'} = time();

    # Set the description to be used for status messages
    $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Maintainability Test Case %d (%s)", 2, "Scheduled Backups";

    $message = sprintf "# %s #", $::JOB_PARAMS{'TEST_DESCRIPTION'};
    $length = length($message);
    General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T21'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Enable_Housekeeping_Auto_Delete_P401S02T21 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T21'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T22'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Enable_Scheduler_For_Backups_P401S02T22 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T22'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T23'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Scheduled_Backups_P401S02T23 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T23'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T24'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Disable_Scheduler_For_Backups_P401S02T24 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T24'} = 0;
    }

    if ($::JOB_PARAMS{'P401_EXECUTE_P401S02T25'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Disable_Housekeeping_Auto_Delete_If_Needed_P401S02T25 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P401_EXECUTE_P401S02T25'} = 0;
    }

    # This call will wait for all PODs to be up and then check node health and collects KPI's etc.
    $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = "yes";
    $::JOB_PARAMS{'POD_UP_TIMER'} = 15;
    $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'} = $old_skip_kpi_verdict_check;
    $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = $old_skip_collect_logs;
    $::JOB_PARAMS{'POD_UP_TIMER'} = $old_repeated_check_time;
    return $rc if $rc < 0;

    # Delete these temporary variables
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T02'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T03'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T04'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T05'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T06'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T07'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T08'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T09'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T10'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T11'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T12'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T13'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T14'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T15'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T16'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T17'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T18'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T19'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T20'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T21'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T22'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T23'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T24'};
    delete $::JOB_PARAMS{'P401_EXECUTE_P401S02T25'};

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
    sub Check_Job_Parameters_P401S02T01 {

        my $rc = 0;

        # Check if the user want to modify the time zone values to use
#       if ($::JOB_PARAMS{'TIMEZONES'} ne "") {
#           @timezones = ( "", split /[,|]+/, $::JOB_PARAMS{'TIMEZONES'} );
#       }

#       General::Logging::log_user_message("Used Time zones are:" . join "\n - ", @timezones);

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
    sub Check_sftp_Server_P401S02T02 {

        my $command = "";
        my $namespace = $::JOB_PARAMS{'SFTP_NAMESPACE'} ne "" ? $::JOB_PARAMS{'SFTP_NAMESPACE'} : $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my @result;

        if ($::JOB_PARAMS{'SFTP_IP'} eq "") {
            General::Logging::log_user_message("Fetching sftp node information from namespace $namespace");

            # Find the node port to use
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get svc --namespace $namespace --no-headers eric-atmoz-sftp",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0 || scalar @result != 1) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to fetch node information");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                return 1;
            }
            if ($result[0] =~ /^eric-atmoz-sftp\s+\S+\s+\S+\s+\S+\s+\d+:(\d+)\/TCP/) {
                # eric-atmoz-sftp                                       NodePort       10.104.154.208   <none>          8922:32370/TCP                                 128m
                $::JOB_PARAMS{'SFTP_PORT'} = $1;
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to fetch node information, unexpected format returned");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                return 1;
            }

            # Find the worker IP address
            $::JOB_PARAMS{'SFTP_IP'} = ADP::Kubernetes_Operations::get_node_ip(
                {
                    "index" => 0,
                    "type"  => "worker",
                }
            );
            if ($::JOB_PARAMS{'SFTP_IP'} eq "") {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to fetch node information");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                return 1;
            }
        }

        # Check that sftp server is reachable
        $command = "ssh-keyscan ";
        if ($::JOB_PARAMS{'SFTP_PORT'} ne "") {
            General::Logging::log_user_message("Checking that sftp server on IP-address $::JOB_PARAMS{'SFTP_IP'} and port $::JOB_PARAMS{'SFTP_PORT'} is reachable");
            $command .= "-p $::JOB_PARAMS{'SFTP_PORT'} ";
        } else {
            General::Logging::log_user_message("Checking that sftp server on IP-address $::JOB_PARAMS{'SFTP_IP'} is reachable");
        }
        $command .= "-T 2 $::JOB_PARAMS{'SFTP_IP'}";
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check sftp status");
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
    sub Check_CMYP_CLI_Access_P401S02T03 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/system_health_check.pl";
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;

        General::Logging::log_user_message("Checking that external interfaces like CMYP is accessible");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command --namespace $namespace -c external_interfaces",
                "hide-output"   => 1,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check CMYP CLI access");
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
    sub Print_Configuration_At_Start_P401S02T04 {

        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $rc = 0;
        my @result;
        my $timeout = 60;

        General::Logging::log_user_message("Printing configuration data from CMYP");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout" .
                                   " --command='paginate false'" .
                                   " --command='show running-config'",
                "hide-output"   => 1,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Printing configuration data from CMYP failed");
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
    sub Check_Available_Backup_Slots_P401S02T05 {

        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $rc = 0;
        my @result;
        my $timeout = 60;

        General::Logging::log_user_message("Fetching max-stored-manual-backups from CMYP");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout --command='paginate false' --command='show running-config brm backup-manager DEFAULT'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        $::JOB_PARAMS{'BRM_MAX_STORED_MANUAL_BACKUPS'} = "";
        if ($rc == 0) {
            for (@result) {
                if (/^\s*housekeeping max-stored-manual-backups\s+(\d+)\s*$/) {
                    $::JOB_PARAMS{'BRM_MAX_STORED_MANUAL_BACKUPS'} = $1;
                }
            }
            if ($::JOB_PARAMS{'BRM_MAX_STORED_MANUAL_BACKUPS'} eq "") {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Unable to find max-stored-manual-backups value");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                return 1;
            } elsif ($::JOB_PARAMS{'BRM_MAX_STORED_MANUAL_BACKUPS'} == 1) {
                # Not enough storage slots for 2 backups, so increase it to 2
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout" .
                                           " --command='config'" .
                                           " --command='brm backup-manager DEFAULT housekeeping max-stored-manual-backups 2'" .
                                           " --command='commit'",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Unable to change max-stored-manual-backups value to 2");
                    push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                    return 1;
                }
                General::Logging::log_user_message("Changed max-stored-manual-backups from 1 to 2");
            } else {
                General::Logging::log_user_message("max-stored-manual-backups=$::JOB_PARAMS{'BRM_MAX_STORED_MANUAL_BACKUPS'}");
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Fetching max-stored-manual-backups from CMYP failed");
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
    sub Create_Manual_Backup_1_P401S02T06 {

        my $timestamp = time();
        my $backup_name = sprintf "Manual_Backup_1_%d", $timestamp;
        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/backup_create.exp";
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my @result;
        my $timeout = 60;

        General::Logging::log_user_message("Creating manual backup with name: $backup_name");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout --backup-name=$backup_name",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        $::JOB_PARAMS{'BRM_BACKUP_1_NAME'} = "";
        if ($rc == 0) {
            $::JOB_PARAMS{'BRM_BACKUP_1_NAME'} = $backup_name;

            # Create a backup of the unseal key for future restore of backup on newly deployed system
            General::Logging::log_user_message("Saving the unseal key for the backup");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $namespace get secret/eric-sec-key-management-unseal-key -o yaml",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                    "save-to-file"  => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/key-management-unseal-key_$timestamp.yaml",
                }
            );
            if ($rc == 0) {
                General::Logging::log_user_message("Backup saved as: $backup_name\nUnseal Key file: $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/key-management-unseal-key_$timestamp.yaml");
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to save the unseal key");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Creating manual backup failed: $backup_name");
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
    sub Export_Manual_Backup_1_P401S02T07 {

        my $backup_name = $::JOB_PARAMS{'BRM_BACKUP_1_NAME'};
        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/backup_export.exp";
        my $exported_backup_name;
        my $rc = 0;
        my @result;
        my $sftp_password = $::JOB_PARAMS{'SFTP_PASSWORD'};
        my $sftp_url;
        my $timeout = 60;

        General::Logging::log_user_message("Exporting manual backup with name: $backup_name");

        if ($::JOB_PARAMS{'SFTP_IP'} =~ /:/) {
            $sftp_url = sprintf '%s@[%s]:%d%s', $::JOB_PARAMS{'SFTP_USER'}, $::JOB_PARAMS{'SFTP_IP'}, $::JOB_PARAMS{'SFTP_PORT'}, $::JOB_PARAMS{'SFTP_EXPORTED_FILE_PATH'};
        } else {
            $sftp_url = sprintf '%s@%s:%d%s', $::JOB_PARAMS{'SFTP_USER'}, $::JOB_PARAMS{'SFTP_IP'}, $::JOB_PARAMS{'SFTP_PORT'}, $::JOB_PARAMS{'SFTP_EXPORTED_FILE_PATH'};
        }

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout --backup-name=$backup_name --sftp-password='$sftp_password' --sftp-url='$sftp_url'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_1_NAME'} = "";
        if ($rc == 0) {
            for (@result) {
                if (/^\s+additional-info.+"Exported Backup: (\S+)" ]/) {
                    #   additional-info            [ "Exported Backup: myBackup-2021-05-05T09:37:31.401196Z.tar.gz" ]
                    $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_1_NAME'} = $1;
                    last;
                }
            }

            if ($::JOB_PARAMS{'BRM_EXPORTED_BACKUP_1_NAME'} ne "") {
                General::Logging::log_user_message("Exported filename: $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_1_NAME'}");
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Unable to find exported backup file name");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Exporting manual backup failed: $backup_name");
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
    sub Add_New_Test_User_P401S02T08 {

        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my @result;
        my $timeout = 60;
        my $user_groups = "system-admin system-security-admin scp-admin bsf-admin csa-admin sepp-admin pc-probe-privacy-admin";
        my $user_name = "temptestuser1";
        my $user_password = "rootroot";

        General::Logging::log_user_message("Creating new test user $user_name in CMYP CLI");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout --command='config' --command='system authentication default-password-policy must-change false' --command='commit' --command='system authentication user $user_name password $user_password groups [ $user_groups ]' --command='commit'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Creating user failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1;
        }

        # Wait for the user to have time to be created before we check that it works
        General::Logging::log_user_message("Waiting 10 seconds for the user to be created");
        General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "progress-message"  => 0,
                "seconds"           => 10,
            }
        );

        General::Logging::log_user_message("Printing configuration data from CMYP");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command --user=$user_name --password=$user_password --ip=$cmyp_ip --timeout=$timeout" .
                                   " --command='paginate false'" .
                                   " --command='show running-config'",
                "hide-output"   => 1,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Printing configuration data from CMYP failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1
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
    sub Create_Manual_Backup_2_P401S02T09 {

        my $timestamp = time();
        my $backup_name = sprintf "Manual_Backup_2_%d", $timestamp;
        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/backup_create.exp";
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my @result;
        my $timeout = 60;

        General::Logging::log_user_message("Creating manual backup with name: $backup_name");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout --backup-name=$backup_name",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        $::JOB_PARAMS{'BRM_BACKUP_2_NAME'} = "";
        if ($rc == 0) {
            $::JOB_PARAMS{'BRM_BACKUP_2_NAME'} = $backup_name;

            # Create a backup of the unseal key for future restore of backup on newly deployed system
            General::Logging::log_user_message("Saving the unseal key for the backup");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $namespace get secret/eric-sec-key-management-unseal-key -o yaml",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                    "save-to-file"  => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/key-management-unseal-key_$timestamp.yaml",
                }
            );
            if ($rc == 0) {
                General::Logging::log_user_message("Backup saved as: $backup_name\nUnseal Key file: $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/key-management-unseal-key_$timestamp.yaml");
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to save the unseal key");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Creating manual backup failed: $backup_name");
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
    sub Export_Manual_Backup_2_P401S02TT10 {

        my $backup_name = $::JOB_PARAMS{'BRM_BACKUP_2_NAME'};
        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/backup_export.exp";
        my $exported_backup_name;
        my $rc = 0;
        my @result;
        my $sftp_password = $::JOB_PARAMS{'SFTP_PASSWORD'};
        my $sftp_url;
        my $timeout = 60;

        General::Logging::log_user_message("Exporting manual backup with name: $backup_name");

        if ($::JOB_PARAMS{'SFTP_IP'} =~ /:/) {
            $sftp_url = sprintf '%s@[%s]:%d%s', $::JOB_PARAMS{'SFTP_USER'}, $::JOB_PARAMS{'SFTP_IP'}, $::JOB_PARAMS{'SFTP_PORT'}, $::JOB_PARAMS{'SFTP_EXPORTED_FILE_PATH'};
        } else {
            $sftp_url = sprintf '%s@%s:%d%s', $::JOB_PARAMS{'SFTP_USER'}, $::JOB_PARAMS{'SFTP_IP'}, $::JOB_PARAMS{'SFTP_PORT'}, $::JOB_PARAMS{'SFTP_EXPORTED_FILE_PATH'};
        }

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout --backup-name=$backup_name --sftp-password='$sftp_password' --sftp-url='$sftp_url'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_2_NAME'} = "";
        if ($rc == 0) {
            for (@result) {
                if (/^\s+additional-info.+"Exported Backup: (\S+)" ]/) {
                    #   additional-info            [ "Exported Backup: myBackup-2021-05-05T09:37:31.401196Z.tar.gz" ]
                    $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_2_NAME'} = $1;
                    last;
                }
            }

            if ($::JOB_PARAMS{'BRM_EXPORTED_BACKUP_2_NAME'} ne "") {
                General::Logging::log_user_message("Exported filename: $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_2_NAME'}");
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Unable to find exported backup file name");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Exporting manual backup failed: $backup_name");
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
    sub Restore_Manual_Backup_1_P401S02T11 {

        my $backup_name = $::JOB_PARAMS{'BRM_BACKUP_1_NAME'};
        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/backup_restore.exp";
        my $rc = 0;
        my @result;
        my $timeout = 60;

        General::Logging::log_user_message("Restoring manual backup with name: $backup_name");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout --backup-name=$backup_name",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Restoring manual backup failed: $backup_name");
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
    sub Check_Non_Existence_Of_Test_User_P401S02T12 {

        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my @result;
        my $timeout = 60;
        my $user_name = "temptestuser1";
        my $user_password = "rootroot";

        General::Logging::log_user_message("Checking that it's not possible to login to CMYP with user $user_name");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command --user=$user_name --password=$user_password --ip=$cmyp_ip --timeout=$timeout" .
                                   " --command='paginate false'" .
                                   " --command='show running-config'",
                "hide-output"   => 1,
            }
        );

        if ($rc == 2) {
            General::Logging::log_user_message("Check was successful, it's not possible to login with user $user_name");
            $rc = 0;
        } elsif ($rc == 2) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Check failed, login with user $user_name was successful which should not be the case");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Login failed for some other reason");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1
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
    sub Import_Manual_Backup_2_P401S02T13 {

        my $backup_name = $::JOB_PARAMS{'BRM_BACKUP_2_NAME'};
        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/backup_import.exp";
        my $rc = 0;
        my @result;
        my $sftp_password = $::JOB_PARAMS{'SFTP_PASSWORD'};
        my $sftp_url;
        my $timeout = 60;

        General::Logging::log_user_message("Importing backup with name: $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_2_NAME'}");

        if ($::JOB_PARAMS{'SFTP_IP'} =~ /:/) {
            $sftp_url = sprintf '%s@[%s]:%d%s/DEFAULT/%s', $::JOB_PARAMS{'SFTP_USER'}, $::JOB_PARAMS{'SFTP_IP'}, $::JOB_PARAMS{'SFTP_PORT'}, $::JOB_PARAMS{'SFTP_EXPORTED_FILE_PATH'}, $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_2_NAME'};
        } else {
            $sftp_url = sprintf '%s@%s:%d%s/DEFAULT/%s', $::JOB_PARAMS{'SFTP_USER'}, $::JOB_PARAMS{'SFTP_IP'}, $::JOB_PARAMS{'SFTP_PORT'}, $::JOB_PARAMS{'SFTP_EXPORTED_FILE_PATH'}, $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_2_NAME'};
        }

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout --sftp-password='$sftp_password' --sftp-url='$sftp_url'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Importing backup failed");
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
    sub Restore_Manual_Backup_2_P401S02T14 {

        my $backup_name = $::JOB_PARAMS{'BRM_BACKUP_2_NAME'};
        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/backup_restore.exp";
        my $rc = 0;
        my @result;
        my $timeout = 60;

        General::Logging::log_user_message("Restoring manual backup with name: $backup_name");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout --backup-name=$backup_name",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Restoring manual backup failed: $backup_name");
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
    sub Check_Existence_Of_Test_User_P401S02T15 {

        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $rc = 0;
        my @result;
        my $timeout = 60;
        my $user_name = "temptestuser1";
        my $user_password = "rootroot";

        General::Logging::log_user_message("Printing configuration data from CMYP");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command --user=$user_name --password=$user_password --ip=$cmyp_ip --timeout=$timeout" .
                                   " --command='paginate false'" .
                                   " --command='show running-config'",
                "hide-output"   => 1,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Printing configuration data from CMYP failed");
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
    sub Delete_Manual_Backup_1_P401S02T16 {

        my $backup_name = $::JOB_PARAMS{'BRM_BACKUP_1_NAME'};
        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/backup_delete.exp";
        my $rc = 0;
        my @result;
        my $timeout = 60;

        General::Logging::log_user_message("Deleting backup with name: $backup_name");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout --backup-name=$backup_name",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Deleting backup failed: $backup_name");
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
    sub Delete_Manual_Backup_2_P401S02T17 {

        my $backup_name = $::JOB_PARAMS{'BRM_BACKUP_2_NAME'};
        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/backup_delete.exp";
        my $rc = 0;
        my @result;
        my $timeout = 60;

        General::Logging::log_user_message("Deleting backup with name: $backup_name");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout --backup-name=$backup_name",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Deleting backup failed: $backup_name");
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
    sub Restore_Available_Backup_Slots_If_Needed_P401S02T18 {

        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $rc = 0;
        my @result;
        my $timeout = 60;

        if ($::JOB_PARAMS{'BRM_MAX_STORED_MANUAL_BACKUPS'} > 1) {
            General::Logging::log_user_message("Restoring max-stored-manual-backups not needed because it was already > 1");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Restoring max-stored-manual-backups to 1");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout" .
                                   " --command='config'" .
                                   " --command='brm backup-manager DEFAULT housekeeping max-stored-manual-backups 1'" .
                                   " --command='commit'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Unable to change max-stored-manual-backups value to 1");
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
    sub Delete_New_Test_User_P401S02T19 {

        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $rc = 0;
        my @result;
        my $timeout = 60;
        my $user_name = "temptestuser1";

        General::Logging::log_user_message("Deleting new test user $user_name in CMYP CLI");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout" .
                                   " --command='config'" .
                                   " --command='no system authentication user $user_name'" .
                                   " --command='commit'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Deleting user failed");
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
    sub Create_Manual_Backup_3_P401S02T20 {

        my $timestamp = time();
        my $backup_name = sprintf "Manual_Backup_3_%d", $timestamp;
        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/backup_create.exp";
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my @result;
        my $timeout = 60;

        General::Logging::log_user_message("Creating manual backup with name: $backup_name");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout --backup-name=$backup_name",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        $::JOB_PARAMS{'BRM_BACKUP_3_NAME'} = "";
        if ($rc == 0) {
            $::JOB_PARAMS{'BRM_BACKUP_3_NAME'} = $backup_name;

            # Create a backup of the unseal key for future restore of backup on newly deployed system
            General::Logging::log_user_message("Saving the unseal key for the backup");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $namespace get secret/eric-sec-key-management-unseal-key -o yaml",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                    "save-to-file"  => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/key-management-unseal-key_$timestamp.yaml",
                }
            );
            if ($rc == 0) {
                General::Logging::log_user_message("Backup saved as: $backup_name\nUnseal Key file: $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/key-management-unseal-key_$timestamp.yaml");
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to save the unseal key");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Creating manual backup failed: $backup_name");
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
    sub Enable_Housekeeping_Auto_Delete_P401S02T21 {

        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $rc = 0;
        my @result;
        my $timeout = 60;

        General::Logging::log_user_message("Enable auto-delete in CMYP CLI");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout" .
                                   " --command='paginate false'" .
                                   " --command='show running-config brm backup-manager DEFAULT housekeeping auto-delete'" .
                                   " --command='config'" .
                                   " --command='brm backup-manager DEFAULT housekeeping auto-delete enabled'" .
                                   " --command='commit'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Enable auto-delete failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
        }

        $::JOB_PARAMS{'BRM_AUTO_DELETE'} = "";
        for (@result) {
            if (/^\s+housekeeping auto-delete (\S+)\s*$/) {
                $::JOB_PARAMS{'BRM_AUTO_DELETE'} = $1;
                last;
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
    sub Enable_Scheduler_For_Backups_P401S02T22 {

        my $timestamp = time();
        my $backup_prefix = sprintf "AB%d", $timestamp;
        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $rc = 0;
        my @result;
        my $schedule_delay = $::JOB_PARAMS{'SCHEDULE_DELAY_MINUTES'};
        my $schedule_name = sprintf "EVERY_%d_MIN_%d", $schedule_delay, $timestamp;
        my $sftp_password = $::JOB_PARAMS{'SFTP_PASSWORD'};
        my $sftp_url;
        my $timeout = 60;

        $::JOB_PARAMS{'BRM_BACKUP_PREFIX'} = $backup_prefix;
        $::JOB_PARAMS{'BRM_SCHEDULE_NAME'} = $schedule_name;

        General::Logging::log_user_message("Schedule automatic backup with prefix $backup_prefix every $schedule_delay minutes using schedule name $schedule_name in CMYP CLI");

        if ($::JOB_PARAMS{'SFTP_IP'} =~ /:/) {
            $sftp_url = sprintf '%s@[%s]:%d%s', $::JOB_PARAMS{'SFTP_USER'}, $::JOB_PARAMS{'SFTP_IP'}, $::JOB_PARAMS{'SFTP_PORT'}, $::JOB_PARAMS{'SFTP_EXPORTED_FILE_PATH'};
        } else {
            $sftp_url = sprintf '%s@%s:%d%s', $::JOB_PARAMS{'SFTP_USER'}, $::JOB_PARAMS{'SFTP_IP'}, $::JOB_PARAMS{'SFTP_PORT'}, $::JOB_PARAMS{'SFTP_EXPORTED_FILE_PATH'};
        }

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout" .
                                   " --command='paginate false'" .
                                   " --command='show running-config brm backup-manager DEFAULT scheduler'" .
                                   " --command='config'" .
                                   " --command='brm backup-manager DEFAULT scheduler admin-state locked'" .
                                   " --command='brm backup-manager DEFAULT scheduler scheduled-backup-name $backup_prefix'" .
                                   " --command='commit'" .
                                   " --command='brm backup-manager DEFAULT scheduler auto-export enabled'" .
                                   " --command='brm backup-manager DEFAULT scheduler auto-export-uri sftp://$sftp_url'" .
                                   " --command='brm backup-manager DEFAULT scheduler auto-export-password $sftp_password'" .
                                   " --command='brm backup-manager DEFAULT scheduler periodic-event $schedule_name weeks 0 days 0 hours 0 minutes $schedule_delay'" .
                                   " --command='brm backup-manager DEFAULT scheduler admin-state unlocked'" .
                                   " --command='commit'" .
                                   " --command='exit'" .
                                   " --command='show running-config brm backup-manager DEFAULT scheduler'" .
                                   " --command='show brm backup-manager DEFAULT scheduler progress-report'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Schedule automatic backup failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";

            return 1;
        }

        # Wait for scheduled backup to start + 1 more minute for it to finish
        my $sleep_time = ($schedule_delay * 60) + 60;
        General::Logging::log_user_message("Schedule started, now waiting $sleep_time seconds for backup to start and finish");
        General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "progress-message"  => 1,
                "seconds"           => $sleep_time,
            }
        );

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
    sub Check_Scheduled_Backups_P401S02T23 {

        my $backup_prefix = $::JOB_PARAMS{'BRM_BACKUP_PREFIX'};
        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $rc = 0;
        my @result;
        my $schedule_name = $::JOB_PARAMS{'BRM_SCHEDULE_NAME'};
        my $timeout = 60;

        General::Logging::log_user_message("Check that automatic backup was created in CMYP CLI");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout" .
                                   " --command='paginate false'" .
                                   " --command='show brm backup-manager DEFAULT backup'" .
                                   " --command='show brm backup-manager DEFAULT scheduler progress-report'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Enable auto-delete failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
        }

        my $reading = 0;
        my $backup_found = 0;
        my $backup_status_ok = 0;
        my $backup_result_ok = 0;
        my $backup_state_ok = 0;
        for (@result) {
            if ($reading == 1) {
                if (/^\s+status\s+backup-complete\s*$/) {
                    # status        backup-complete
                    $backup_status_ok = 1;
                } elsif (/^\s+result\s+success\s*$/) {
                    #  result                     success
                    $backup_result_ok = 1;
                } elsif (/^\s+state\s+finished\s*$/) {
                    #  result                     success
                    $backup_state_ok = 1;
                } elsif (/^\S+/) {
                    $reading = 0;
                }
            } elsif (/^backup $backup_prefix-\S+/) {
                #backup AUTOBACKUP-2022-12-05T12:58:31.416114Z
                $backup_found = 1;
                $reading = 1;
            }
        }
        if ($backup_found == 1 && $backup_status_ok == 1 && $backup_result_ok == 1 && $backup_state_ok == 1) {
            General::Logging::log_user_message("Automatic backup was successfully created");
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";

            General::Logging::log_user_error_message(
                sprintf "Automatic backup either not started or failed:\n  Backup found: %s\n  Status OK:    %s\n  Result OK:    %s\n  State OK:     %s\n",
                    ($backup_found == 1 ? "Yes" : "No"),
                    ($backup_status_ok == 1 ? "Yes" : "No"),
                    ($backup_result_ok == 1 ? "Yes" : "No"),
                    ($backup_state_ok == 1 ? "Yes" : "No")
            );

            return 1;
        }

        # TODO: Maybe we should also check that the backup was exported to the SFTP server.

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
    sub Disable_Scheduler_For_Backups_P401S02T24 {

        my $timestamp = time();
        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $rc = 0;
        my @result;
        my $schedule_name = $::JOB_PARAMS{'BRM_SCHEDULE_NAME'};
        my $timeout = 60;

        General::Logging::log_user_message("Remove automatic backup schedule with schedule name $schedule_name in CMYP CLI");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout" .
                                   " --command='paginate false'" .
                                   " --command='show running-config brm backup-manager DEFAULT scheduler'" .
                                   " --command='config'" .
                                   " --command='brm backup-manager DEFAULT scheduler admin-state locked'" .
                                   " --command='no brm backup-manager DEFAULT scheduler periodic-event $schedule_name'" .
                                   " --command='brm backup-manager DEFAULT scheduler admin-state unlocked'" .
                                   " --command='commit'" .
                                   " --command='exit'" .
                                   " --command='show running-config brm backup-manager DEFAULT scheduler'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Enable auto-delete failed");
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
    sub Disable_Housekeeping_Auto_Delete_If_Needed_P401S02T25 {

        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $rc = 0;
        my @result;
        my $timeout = 60;

        if ($::JOB_PARAMS{'BRM_AUTO_DELETE'} eq "disabled") {
            # The function was previously disabled, so we need to disable it again
            General::Logging::log_user_message("Disable auto-delete in CMYP CLI");

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout" .
                                       " --command='config'" .
                                       " --command='brm backup-manager DEFAULT housekeeping auto-delete disabled'" .
                                       " --command='commit'",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );

            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Configuration of auto-delete failed");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            }
        } else {
            # The function was previously enabled, so no need to change it
            General::Logging::log_user_message("auto-delete was previously enabled, so we do not need to disable it");
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
sub Perform_Post_Test_Checks_P401S03 {

    my $rc;

    # This second call will perform post test checks and job cleanup
    $::JOB_PARAMS{'P932_TASK'} = "POST_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    return $rc if $rc < 0;

    return $rc;
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P401S99 {

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
        'SCHEDULE_DELAY_MINUTES' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "2",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the delay between scheduled automatic backups in minutes.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SFTP_EXPORTED_FILE_PATH' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "/data/backups",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the path on the SFTP server where the exported backup files are saved to.

If not specified then it's assumed that the SFTP server is deployed in the same
namespace as the SC application and is using the default file path used by the
kubernets service named eric-atmoz-sftp.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SFTP_IP' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the IP-address used for reaching the SFTP server to be used for the
export and import of backups.

If not specified then it's assumed that the SFTP server is deployed in the same
namespace as the SC application and it will use the IP-address of one of the
worker nodes and the port number (SFTP_PORT) from the kubernets service named
eric-atmoz-sftp.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SFTP_NAMESPACE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the namespace where the SFTP server is deployed which is to be used for
the export and import of backups.

If not specified and also SFTP_IP is not specified then it's assumed that the
SFTP server is deployed in the same namespace as the SC application and it will
use the IP-address of one of the worker nodes and the port number (SFTP_PORT)
from the kubernets service named eric-atmoz-sftp.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SFTP_PASSWORD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "admin",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the Password used for login to the SFTP server to be used for the
export and import of backups.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SFTP_PORT' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the IP-address used for reaching the SFTP server to be used for the
export and import of backups.

If not specified then it's assumed that the SFTP server is deployed in the same
namespace as the SC application and it will use the node port of the kubernets
service named eric-atmoz-sftp.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SFTP_USER' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "admin",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the User used for login to the SFTP server to be used for the export
and import of backups.
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

This Playlist performs maintainability test cases that verifies backup and
restore functionality.

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
