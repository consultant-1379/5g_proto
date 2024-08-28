package Playlist::402_Maintainability_Test_Disaster_Recovery;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.26
#  Date     : 2024-05-29 14:00:59
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
use version;

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

use Playlist::001_Deploy_SC;
use Playlist::003_Undeploy_SC;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;
use Playlist::932_Test_Case_Common_Logic;

#
# Variable Declarations
#
my %playlist_variables;
set_playlist_variables();

my $debug_command = "";         # Change this value from "echo " to "" when ready to test on real system

# >>>> Start Temporary Trouble Shooting Code DND-64493 <<<<
# Special Troubleshooting code to get more information about a fault.
# Set it back to 0 to skip the code.
my $troubleshooting_code_dnd_64493 = 1;
# >>>> End Temporary Trouble Shooting Code DND-64493 <<<<

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
    $::JOB_PARAMS{'JOBTYPE'} = "MAINTAINABILITY_TEST_DISASTER_RECOVERY";

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    print_start_playlist_message("402_Maintainability_Test_Disaster_Recovery Part 1");

    $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Test_Checks_P402S01, \&Fallback001_P402S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Test_Case_P402S02, \&Fallback001_P402S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Checks_P402S03, \&Fallback001_P402S99 );
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
sub Perform_Pre_Test_Checks_P402S01 {

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
sub Perform_Test_Case_P402S02 {

    my $length;
    my $message;
    my $old_skip_kpi_verdict_check = $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'};
    my $old_skip_collect_logs = $::JOB_PARAMS{'SKIP_COLLECT_LOGS'};
    my $old_repeated_check_time = $::JOB_PARAMS{'POD_UP_TIMER'};
    my $rc;

    # We always want to read the network config file and read software directory for each playlist
    General::State_Machine::always_execute_task("Playlist::920_Check_Network_Config_Information::.+");
    General::State_Machine::always_execute_task("Playlist::922_Handle_Software_Directory_Files::.+");

    # To avoid removing directories before all tasks has been executed we will
    # only execute the cleanup at the end.
    General::State_Machine::always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

    # Set start values
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T01'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T01'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T01'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T02'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T02'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T02'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T03'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T03'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T03'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T04'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T04'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T04'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T05'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T05'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T05'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T06'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T06'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T06'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T07'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T07'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T07'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T08'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T08'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T08'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T09'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T09'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T09'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T10'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T10'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T10'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T11'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T11'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T11'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T12'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T12'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T12'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T13'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T13'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T13'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T14'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T14'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T14'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T15'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T15'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T15'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T16'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T16'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T16'} : 1;
    $::JOB_PARAMS{'P402_EXECUTE_P402S02T17'} = exists $::JOB_PARAMS{'P402_EXECUTE_P402S02T17'} ? $::JOB_PARAMS{'P402_EXECUTE_P402S02T17'} : 1;

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T01'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P402S02T01 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T01'} = 0;
    }

    #
    # Manual Backup and Export
    #

    # Set start time for this repetition in case we want to have KPI verdicts for each repetition
    $::JOB_PARAMS{'KPI_START_TIME'} = time();

    # Set the description to be used for status messages
    $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Maintainability Test Case %d (%s)", 1, "Manual Backup and Restore";

    $message = sprintf "# %s #", $::JOB_PARAMS{'TEST_DESCRIPTION'};
    $length = length($message);
    General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T02'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_sftp_Server_P402S02T02 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T02'} = 0;
    }

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T03'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_CMYP_CLI_Access_P402S02T03 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T03'} = 0;
    }

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T04'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Configuration_At_Start_P402S02T04 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T04'} = 0;
    }

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T05'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Available_Backup_Slots_P402S02T05 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T05'} = 0;
    }

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T06'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Manual_Backup_P402S02T06 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T06'} = 0;
    }

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T07'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Export_Manual_Backup_P402S02T07 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T07'} = 0;
    }

    #
    # Uninstall the software
    #

    print_start_playlist_message("003_Undeploy_SC");
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::003_Undeploy_SC::main } );
    return $rc if $rc < 0;

    #
    # Reinstall the software
    #

    if ($::JOB_PARAMS{'CONFIG_FILE_HELM_CHART'} eq "" && -f "$::JOB_PARAMS{'_JOB_LOG_DIR'}/used_values.yaml") {
        # No specific values file was specified so we try to reuse the values used from the existing deployment
        $::JOB_PARAMS{'CONFIG_FILE_HELM_CHART'} = $::JOB_PARAMS{'_JOB_LOG_DIR'} . "/used_values.yaml";
    }

    # We need to figure out what CNF's were previously deployed and set the ENABLED_CNF to match this.
    $::JOB_PARAMS{'ENABLED_CNF'} = "";
    for my $cnf (split /,/, $::JOB_PARAMS{'DEPLOYED_CNF'}) {
        $::JOB_PARAMS{'ENABLED_CNF'} .= "$cnf-" if ($cnf =~ /^(bsf|dsc|scp|sepp)$/);
    }
    $::JOB_PARAMS{'ENABLED_CNF'} =~ s/-$//;

    print_start_playlist_message("001_Deploy_SC");
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::001_Deploy_SC::main } );
    return $rc if $rc < 0;

    #
    # Manual Backup Restore
    #

    # Set job type again since it was changed by the other main playlists.
    $::JOB_PARAMS{'JOBTYPE'} = "MAINTAINABILITY_TEST_DISASTER_RECOVERY";

    print_start_playlist_message("402_Maintainability_Test_Disaster_Recovery Part 2");

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T08'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Add_CMYP_User_P402S02T08 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T08'} = 0;
    }

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T09'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Import_Manual_Backup_P402S02T09 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T09'} = 0;
    }

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T10'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Disable_Key_Management_P402S02T10 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T10'} = 0;
    }

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T11'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Replace_Unseal_Key_P402S02T11 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T11'} = 0;
    }

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T12'} == 1) {
        # After the restore operation has finished, the temporary CMYP user added in task P402S02T10 will
        # be replaced by the same user from the backup. So no need to delete the user after the restore.
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Restore_Manual_Backup_P402S02T12 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T12'} = 0;
    }

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T13'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_CMYP_Pod_To_Come_Up_P402S02T13 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T13'} = 0;
    }

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T14'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Enable_Key_Management_P402S02T14 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T14'} = 0;
    }

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T15'} == 1 && version->parse($::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}) < version->parse("1.14.0")) {
        # First we check that all pods except for the eric-bsf-diameter- and eric-bsf-worker- are up
        # But this check is only needs for SC releases up to 1.13, from SC 1.14 and newer this is no longer needed.
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Scale_ObjectStorage_If_Needed_P402S02T15 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T15'} = 0;
    }

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T16'} == 1) {
        # First we check that all pods except for the eric-bsf-diameter- and eric-bsf-worker- are up
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Pod_Status_P402S02T16 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T16'} = 0;
    }

    if ($::JOB_PARAMS{'P402_EXECUTE_P402S02T17'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Initialize_Database_If_BSF_Deployment_P402S02T17 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P402_EXECUTE_P402S02T17'} = 0;
    }

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

    # Delete these temporary variables
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T01'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T02'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T03'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T04'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T05'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T06'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T07'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T08'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T09'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T10'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T11'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T12'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T13'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T14'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T15'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T16'};
    delete $::JOB_PARAMS{'P402_EXECUTE_P402S02T17'};

    # Now we need to execute the cleanup.
    General::State_Machine::remove_always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

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
    sub Check_Job_Parameters_P402S02T01 {

        my $rc;

        # Check that deployed software match the version specified in SOFTWARE_DIR, if not matching stop with an error
        if ($::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} eq $::JOB_PARAMS{'SC_RELEASE_VERSION'} && $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} eq $::JOB_PARAMS{'SC_RELEASE_BUILD'}) {
            General::Logging::log_user_message("Deployed software $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}$::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} matches software package version");
            $rc = 0;
        } elsif ($::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} eq "1.15.99" && $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} eq "-CncsFakedVersionAndBuild") {
            # TODO: This is a temporary workaround until we can read the real version from CMYP SWIM information.
            General::Logging::log_user_warning_message("Deployed software has a faked version $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}$::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} because we don't know what the real CNCS version is.\nSo we just assume the correct software package is provided by the user and we update the old version variables.\n");
            $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} = $::JOB_PARAMS{'SC_RELEASE_VERSION'};
            $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} = $::JOB_PARAMS{'SC_RELEASE_BUILD'};
            $rc = 0;
        } else {
            General::Logging::log_user_error_message("Deployed software $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}$::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} does not match software package version $::JOB_PARAMS{'SC_RELEASE_VERSION'}$::JOB_PARAMS{'SC_RELEASE_BUILD'}");
            return 1;
        }

        # Check that we have valid information for the SFTP server to be used for exporting the backup file, if not we stop with an error
        if ($::JOB_PARAMS{'SFTP_IP'} ne "") {
            General::Logging::log_user_message("An SFTP IP-address specified ($::JOB_PARAMS{'SFTP_IP'}) which will be used for backup export");
            $rc = 0;
        } elsif ($::JOB_PARAMS{'SFTP_NAMESPACE'} eq "") {
            General::Logging::log_user_error_message("No SFTP_IP or SFTP_NAMESPACE values specified so backup export is not possible");
            return 1;
        } elsif ($::JOB_PARAMS{'SFTP_NAMESPACE'} eq $::JOB_PARAMS{'SC_NAMESPACE'}) {
            General::Logging::log_user_error_message("SFTP_NAMESPACE cannot be the same as SC_NAMESPACE since anything deployed in SC_NAMESPACE will be removed by this playlist, so backup export is not possible");
            return 1;
        } else {
            General::Logging::log_user_message("An SFTP namespace specified ($::JOB_PARAMS{'SFTP_NAMESPACE'}) which will be used for backup export");
            $rc = 0;
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
    sub Check_sftp_Server_P402S02T02 {

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

                General::Logging::log_user_error_message("Failed to fetch sftp node information");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                return 1;
            }
            if ($result[0] =~ /^eric-atmoz-sftp\s+\S+\s+\S+\s+\S+\s+\d+:(\d+)\/TCP/) {
                # eric-atmoz-sftp                                       NodePort       10.104.154.208   <none>          8922:32370/TCP                                 128m
                $::JOB_PARAMS{'SFTP_PORT'} = $1;
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to fetch sftp node information, unexpected format returned");
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

                General::Logging::log_user_error_message("Failed to fetch sftp information, no IP address found");
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
    sub Check_CMYP_CLI_Access_P402S02T03 {

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
    sub Print_Configuration_At_Start_P402S02T04 {

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
    sub Check_Available_Backup_Slots_P402S02T05 {

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
    sub Create_Manual_Backup_P402S02T06 {

        my $timestamp = time();
        my $backup_name = sprintf "Manual_Backup_%d", $timestamp;
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

        $::JOB_PARAMS{'BRM_BACKUP_NAME'} = "";
        if ($rc == 0) {
            $::JOB_PARAMS{'BRM_BACKUP_NAME'} = $backup_name;

            # Create a backup of the unseal key for future restore of backup on newly deployed system
            General::Logging::log_user_message("Saving the unseal key for the backup");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $namespace get secret/eric-sec-key-management-unseal-key -o yaml",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                    "save-to-file"  => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/key-management-unseal-key.yaml",
                }
            );
            if ($rc == 0) {
                General::Logging::log_user_message("Backup saved as: $backup_name\nUnseal Key file: $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/key-management-unseal-key.yaml");
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
    sub Export_Manual_Backup_P402S02T07 {

        my $backup_name = $::JOB_PARAMS{'BRM_BACKUP_NAME'};
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

        $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_NAME'} = "";
        if ($rc == 0) {
            for (@result) {
                if (/^\s+additional-info.+"Exported Backup: (\S+)" ]/) {
                    #   additional-info            [ "Exported Backup: myBackup-2021-05-05T09:37:31.401196Z.tar.gz" ]
                    $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_NAME'} = $1;
                    last;
                }
            }

            if ($::JOB_PARAMS{'BRM_EXPORTED_BACKUP_NAME'} ne "") {
                General::Logging::log_user_message("Exported filename: $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_NAME'}");
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
    sub Add_CMYP_User_P402S02T08 {

        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $day0_password = "";
        my $day0_user = "admin";
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my @result;
        my $timeout = 60;
        my $user_groups = "system-admin system-security-admin scp-admin bsf-admin csa-admin sepp-admin pc-probe-privacy-admin";

        # Convert encrypted password to clear text
        General::Logging::log_user_message("Reading password for day0 admin user from network config file");
        for my $key (sort keys %::JOB_PARAMS) {
            if ($key =~ /^wcdbcd_day0_secret_hash_parameter_\d{2}$/) {
                # Old naming format with a 2 digit number in key
                next if ($::JOB_PARAMS{$key} =~ /^.+=CHANGEME$/);
                if ($::JOB_PARAMS{$key} =~ /^data\.admin_password=(.+)/) {
                    $day0_password = $1;    # Encrypted password
                }
            } elsif ($key =~ /^wcdbcd_day0_secret_hash_parameter_(\S+)$/) {
                # New naming format with parameter name included in key
                my $key_value = "$1";
                next if ($::JOB_PARAMS{$key} =~ /^CHANGEME$/);
                if ($key_value eq "data.admin_password") {
                    $day0_password = $::JOB_PARAMS{$key};    # Encrypted password
                }
            } elsif ($key =~ /^day0_admin_secret_hash_parameter_(\S+)$/) {
                # New naming format with parameter name included in key
                my $key_value = "$1";
                next if ($::JOB_PARAMS{$key} =~ /^CHANGEME$/);
                if ($key_value eq "data.adminpasswd") {
                    $day0_password = $::JOB_PARAMS{$key};    # Encrypted password
                }
            }
        }

        if ($day0_password ne "") {
            # Decrypt the password
            $day0_password = General::OS_Operations::base64_decode($day0_password);
            $::JOB_PARAMS{'DAY0_PASSWORD'} = $day0_password;
        } else {
            General::Logging::log_user_error_message("No password provided for day0 admin user in network config file");
            return 1;
        }

        General::Logging::log_user_message("Creating CMYP user $cmyp_user in CMYP CLI");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$day0_user --password=$day0_password --ip=$cmyp_ip --timeout=$timeout --command='config' --command='system authentication default-password-policy must-change false' --command='commit' --command='system authentication user $cmyp_user password $cmyp_password groups [ $user_groups ]' --command='commit'",
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
    sub Import_Manual_Backup_P402S02T09 {

        my $backup_name = $::JOB_PARAMS{'BRM_BACKUP_NAME'};
        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/backup_import.exp";
        my $rc = 0;
        my @result;
        my $sftp_password = $::JOB_PARAMS{'SFTP_PASSWORD'};
        my $sftp_url;
        my $timeout = 60;

        General::Logging::log_user_message("Importing backup with name: $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_NAME'}");

        if ($::JOB_PARAMS{'SFTP_IP'} =~ /:/) {
            $sftp_url = sprintf '%s@[%s]:%d%s/DEFAULT/%s', $::JOB_PARAMS{'SFTP_USER'}, $::JOB_PARAMS{'SFTP_IP'}, $::JOB_PARAMS{'SFTP_PORT'}, $::JOB_PARAMS{'SFTP_EXPORTED_FILE_PATH'}, $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_NAME'};
        } else {
            $sftp_url = sprintf '%s@%s:%d%s/DEFAULT/%s', $::JOB_PARAMS{'SFTP_USER'}, $::JOB_PARAMS{'SFTP_IP'}, $::JOB_PARAMS{'SFTP_PORT'}, $::JOB_PARAMS{'SFTP_EXPORTED_FILE_PATH'}, $::JOB_PARAMS{'BRM_EXPORTED_BACKUP_NAME'};
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

        # >>>> Start Temporary Trouble Shooting Code DND-64493 <<<<
        if ($troubleshooting_code_dnd_64493 == 1) {
            General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
            # Ignore any errors from the log collection
        }
        # >>>> End Temporary Trouble Shooting Code DND-64493 <<<<

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
    sub Disable_Key_Management_P402S02T10 {

        my $message;
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Disabling key management by scaling statefulset/eric-sec-key-management-main down to zero");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} scale --replicas=0 statefulset/eric-sec-key-management-main --namespace $namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message("Checking that statefulset eric-sec-key-management-main has scaled to 0 for a maximum of 300 seconds");
            $rc = ADP::Kubernetes_Operations::check_scaling_status(
                {
                    "namespace"         => $namespace,
                    "check-type"        => "statefulset",
                    "check-name"        => "eric-sec-key-management-main",
                    "debug-messages"    => 1,
                    "scale-value"       => 0,
                    "delay-time"        => 2,
                    "max-time"          => 300,
                    "hide-output"       => 1,
                    "return-message"    => \$message,
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message($message);
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                return 1;
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Disabling key management failed");
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
    sub Replace_Unseal_Key_P402S02T11 {

        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Deleting the current unseal key");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} delete secret eric-sec-key-management-unseal-key --namespace $namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Deleting the current unseal key failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1;
        }

        General::Logging::log_user_message("Loading the old unseal key");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} apply -f $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/key-management-unseal-key.yaml --namespace $namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Loading the old unseal key failed");
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
    sub Restore_Manual_Backup_P402S02T12 {

        my $backup_name = $::JOB_PARAMS{'BRM_BACKUP_NAME'};
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
    sub Wait_For_CMYP_Pod_To_Come_Up_P402S02T13 {

        my $rc = 0;

        General::Logging::log_user_message("Waiting max 3 minutes for the eric-cm-yang-provider pod to come up.\nThis will take a while to complete.\n");
        $rc = ADP::Kubernetes_Operations::check_pod_status(
            {
                "all-namespaces"    => 0,
                "debug-messages"    => 0,
                "delay-time"        => 10,
                "hide-output"       => 1,
                "max-attempts"      => 0,
                "max-time"          => 180,
                "namespace"         => $::JOB_PARAMS{'SC_NAMESPACE'},
                "pod-include-list"  => [
                    "eric-cm-yang-provider-.+",
                ],
                "repeated-checks"   => 0,
                "wanted-ready"      => "same",
                "wanted-status"     => "Running",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("The eric-cm-yang-provider pod failed to come up within the specified time");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
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
    sub Enable_Key_Management_P402S02T14 {

        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $message;
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my @result;
        my $timeout = 60;

        General::Logging::log_user_message("Enabling key management by scaling statefulset/eric-sec-key-management-main back to two replicas");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} scale --replicas=2 statefulset/eric-sec-key-management-main --namespace $namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Disabling key management failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1;
        }

        General::Logging::log_user_message("Checking that statefulset eric-sec-key-management-main has scaled to 2 for a maximum of 300 seconds");
        $rc = ADP::Kubernetes_Operations::check_scaling_status(
            {
                "namespace"         => $namespace,
                "check-type"        => "statefulset",
                "check-name"        => "eric-sec-key-management-main",
                "debug-messages"    => 1,
                "scale-value"       => 2,
                "delay-time"        => 2,
                "max-time"          => 300,
                "hide-output"       => 1,
                "return-message"    => \$message,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message($message);
            return 1;
        }

        # Added a hard coded delay of 5 minutes to make sure everything is up before we check CMYP
        # after a Teams message request from Kay and Dong on 2024-05-28 10:23
        General::Logging::log_user_message("We wait 5 minutes to make sure CMYP and the database is up");
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "confirm-interrupt" => 0,
                "progress-message"  => 1,
                "progress-interval" => 10,
                "seconds"           => 300,
                "use-logging"       => 1,
            }
        );
        if ($rc == 1) {
            General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
        }

        General::Logging::log_user_message("Checking if CMYP access is working by printing configuration");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout" .
                                   " --command='paginate false'" .
                                   " --command='show running-config'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # >>>> Start Temporary Trouble Shooting Code DND-64493 <<<<
            if ($troubleshooting_code_dnd_64493 == 1) {
                # Temporary code to stop the restart in order to hopefully get more useful logs.
                # This temporary code was added as a result of Thomas W. investigating the following error:
                # https://eteamproject.internal.ericsson.com/browse/DND-64493
                # Where basically after the restart of CMYP all configuration DT was gone.
                # This is the error message from DAFT:
                #   [23:51:38.941427]                  Starting Task: Playlist::402_Maintainability_Test_Disaster_Recovery::Enable_Key_Management_P402S02T14
                #   [23:51:39.141924] Enabling key management by scaling statefulset/eric-sec-key-management-main back to two replicas
                #   [23:51:39.233856] Checking that statefulset eric-sec-key-management-main has scaled to 2 for a maximum of 300 seconds
                #   [23:52:34.583839] Checking if CMYP access is working by printing configuration
                #   [23:52:34.891580] WARNING:
                #                   Password:
                #                   Welcome
                #                   Last login time: Mon Apr  1 21:51:18 2024
                #                   Failed to allocate session: node is in upgrade mode
                #                   Connection to 10.137.89.232 closed.
                #
                #                   We were unable to login to the node due to lost connection
                #
                #
                #                   **********************************************************
                #                   ** Potential error(s) have occurred, check output above **
                #                   **********************************************************
                #                   Unable to login to node via SSH at IP address 10.137.89.232
                #
                #                   The information printed below can be used as trouble shooting help to find out why the script failed but is probably only useful for the developers of the script.
                #                   This error was triggered due to the following events:
                #                   -----------------------------------------------------
                #                   [2024-04-01 23:52:34.604762] login_via_ssh: spawn -noecho ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR expert@10.137.89.232
                #                   [2024-04-01 23:52:34.632175]     Prompt before login: aPromptThatShouldNeverBeMatched1234567890
                #                   [2024-04-01 23:52:34.699882]     Potential Prompt detected:
                #                   [2024-04-01 23:52:34.889688]     eof detected, use_special_key=0
                #                   -----------------------------------------------------
                #
                #                   Login to CMYP CLI does now work!
                #                   Restarting CMYP to see if it improves.
                #   [23:52:43.487153] Waiting max 3 minutes for the eric-cm-yang-provider pod to come up.
                #                   This will take a while to complete.

                General::Logging::log_user_error_message((join "\n", @result) . "\nLogin to CMYP CLI does now work!\nRestarting CMYP not done on request from Thomas W. to see if the logs contain any useful information.\n");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                return 1;
                # When error has been found and we want to have the original code back to restart CMYP then delete all the lines between '>>>> Start Temporary Trouble Shooting Code DND-64493 <<<<'
                # and '>>>> End Temporary Trouble Shooting Code DND-64493 <<<<'.
            }
            # >>>> End Temporary Trouble Shooting Code DND-64493 <<<<

            General::Logging::log_user_warning_message((join "\n", @result) . "\nLogin to CMYP CLI does now work!\nRestarting CMYP to see if it improves.\n");
            # Find the CMYP pod name.
            my @pod_names = ADP::Kubernetes_Operations::get_pod_names(
                {
                    "hide-output"       => 1,
                    "namespace"         => $namespace,
                    "pod-include-list"  => [ "eric-cm-yang-provider-" ],
                }
            );
            if (scalar @pod_names ne 1) {
                General::Logging::log_user_error_message("Unable to find the eric-cm-yang-provider pod name");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                return 1;
            }

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} delete pod $pod_names[0] --namespace $namespace",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to restart the $pod_names[0] pod");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                return 1;
            }

            General::Logging::log_user_message("Waiting max 3 minutes for the eric-cm-yang-provider pod to come up.\nThis will take a while to complete.\n");
            $rc = ADP::Kubernetes_Operations::check_pod_status(
                {
                    "all-namespaces"    => 0,
                    "debug-messages"    => 0,
                    "delay-time"        => 10,
                    "hide-output"       => 1,
                    "max-attempts"      => 0,
                    "max-time"          => 180,
                    "namespace"         => $namespace,
                    "pod-include-list"  => [
                        "eric-cm-yang-provider-.+",
                    ],
                    "repeated-checks"   => 0,
                    "wanted-ready"      => "same",
                    "wanted-status"     => "Running",
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("The eric-cm-yang-provider pod failed to come up within the specified time");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                return 1;
            }

            General::Logging::log_user_message("Checking if CMYP access is working by printing configuration");

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$command --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=$timeout" .
                                    " --command='paginate false'" .
                                    " --command='show running-config'",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );

            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to connect to CMYP and print config data");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                return 1;
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
    sub Scale_ObjectStorage_If_Needed_P402S02T15 {

        my $message;
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my @pod_names;
        my $rc = 0;
        my @result;
        my $scale_value = 0;

        General::Logging::log_user_message("Checking if statefulset eric-data-object-storage-mn exists");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get statefulset eric-data-object-storage-mn --no-headers --namespace $namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            General::Logging::log_user_message("Statefulset eric-data-object-storage-mn does not exist, no need to perform this task");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        if ($result[0] =~ /^eric-data-object-storage-mn\s+\d+\/(\d+)\s+\S+/) {
            # eric-data-object-storage-mn   4/4   5h12m
            $scale_value = $1;
        }

        if ($scale_value == 0) {
            General::Logging::log_user_warning_message("Did not find the old scale value or it was 0, no need to perform this task");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Scaling statefulset/eric-data-object-storage-mn to zero replicas");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} scale --replicas=0 statefulset/eric-data-object-storage-mn --namespace $namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Scaling failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1;
        }

        General::Logging::log_user_message("Checking that statefulset eric-data-object-storage-mn has scaled to 0 for a maximum of 300 seconds");
        $rc = ADP::Kubernetes_Operations::check_scaling_status(
            {
                "namespace"         => $namespace,
                "check-type"        => "statefulset",
                "check-name"        => "eric-data-object-storage-mn",
                "debug-messages"    => 1,
                "scale-value"       => 0,
                "delay-time"        => 2,
                "max-time"          => 300,
                "hide-output"       => 1,
                "return-message"    => \$message,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message($message);
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1;
        }

        General::Logging::log_user_message("Deleting secret eric-data-object-storage-mn-kms-set");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} delete secret eric-data-object-storage-mn-kms-set --namespace $namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Deleting secret failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1;
        }

        @pod_names = ADP::Kubernetes_Operations::get_pod_names(
            {
                "hide-output"       => 1,
                "namespace"         => $namespace,
                "pod-include-list"  => [ "eric-data-object-storage-mn-mgt-" ],
            }
        );
        if (scalar @pod_names ne 1) {
            General::Logging::log_user_error_message("Unable to find one eric-data-object-storage-mn-mgt pod name");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1;
        }

        General::Logging::log_user_message("Restarting $pod_names[0] pod");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} delete pod $pod_names[0] --namespace $namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Restarting pod failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1;
        }

        General::Logging::log_user_message("Checking that secret eric-data-object-storage-mn-kms-set get recreated for a maximum of 60 seconds");

        my $stop_time = time() + 60;
        while (time() < $stop_time) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get secret eric-data-object-storage-mn-kms-set --namespace $namespace",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );

            if ($rc == 0) {
                last;
            }

            # We need to wait longer for it to create the secret
            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "confirm-interrupt" => 0,
                    "progress-message"  => 0,
                    "seconds"           => 1,
                    "use-logging"       => 1,
                }
            );
            if ($rc == 1) {
                General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} was interrupted by the user";
                return $rc;
            }
        }

        General::Logging::log_user_message("Scaling statefulset/eric-data-object-storage-mn back to $scale_value replicas");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} scale --replicas=$scale_value statefulset/eric-data-object-storage-mn --namespace $namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Scaling failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1;
        }

        General::Logging::log_user_message("Checking that statefulset eric-data-object-storage-mn has scaled to $scale_value for a maximum of 300 seconds");
        $rc = ADP::Kubernetes_Operations::check_scaling_status(
            {
                "namespace"         => $namespace,
                "check-type"        => "statefulset",
                "check-name"        => "eric-data-object-storage-mn",
                "debug-messages"    => 1,
                "scale-value"       => $scale_value,
                "delay-time"        => 2,
                "max-time"          => 300,
                "hide-output"       => 1,
                "return-message"    => \$message,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message($message);
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
            return 1;
        }

        return $rc;
    }

    # -----------------------------------------------------------------------------
    # This specialized check is needed in case there is a BSF deployment which
    # require that the initialize-db operation is executed which is done in the
    # next task before the eric-bsf-diameter- and eric-bsf-worker- come up
    # x/x but in order to connect to CMYP to execute this command we need to
    # make sure that all other pods are up.
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
    sub Check_Pod_Status_P402S02T16 {

        my @exclude_ready_list = ();
        my @exclude_status_list = ();
        my @failed_pods = ();
        my $max_time = $::JOB_PARAMS{'POD_STATUS_TIMEOUT'};
        my $message = "";
        my $rc = 0;
        my $repeated_check_time = 15;   # $::JOB_PARAMS{'POD_UP_TIMER'};
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        # Ignore the following POD which is constantly respawned and for a
        # short time the status is not Completed.
        @exclude_status_list = (
            "eric-data-search-engine-curator.*",
            "eric-log-transformer.*",
        );

        # For other job types like HEALTHCHECK we assume that all pod's should
        # be already up and ready with x/x except for the following POD's in
        # status Completed where the READY status is not all up because these
        # POD's are just Jobs and not normal pods.
        @exclude_ready_list = (
            "eric-data-search-engine-curator.*",
            "eric-log-transformer.*",
            "eric-bsf-diameter-.+",
            "eric-bsf-worker-.+",
        );

        if ($max_time > 0) {
            General::Logging::log_user_message("Checking every 10 seconds that all pods in $sc_namespace namespace are up and are stable for $repeated_check_time seconds, for a maximum of $max_time seconds.");
        } else {
            General::Logging::log_user_message("Checking that all pods in $sc_namespace namespace are up.");
        }
        $rc = ADP::Kubernetes_Operations::check_pod_status(
            {
                "all-namespaces"                            => 0,
                "debug-messages"                            => 0,
                "delay-time"                                => 10,
                "ignore-ready-check-for-completed-status"   => 1,
                "hide-output"                               => 1,
                "max-attempts"                              => 0,
                "max-time"                                  => $max_time,
                "namespace"                                 => $sc_namespace,
                "pod-exclude-ready-list"                    => \@exclude_ready_list,
                "pod-exclude-status-list"                   => \@exclude_status_list,
                "repeated-checks"                           => ($repeated_check_time > 0 ? 1 : 0),
                "repeated-check-time"                       => $repeated_check_time,
                "return-output"                             => \@result,
                "return-failed-pods"                        => \@failed_pods,
                "wanted-ready"                              => "same",
                "wanted-status"                             => "up",
            }
        );
        if ($rc != 0) {
            $message = join("\n",@failed_pods);
            if ($message =~/^pm-bulk-reporter$/i) {
                # pm-bulk-reporter is not up and running x/x
                # delete the pod and check if it comes up
                # remove pm-bulk-reporter from $message
                $message = join("\n", grep { $_ !~ m{"pm-bulk-reporter"} } split(/\n/, $message) );
                my @std_error       = ();
                my @include_list    = ();
                my $delete_pod      = "";
                foreach my $failed_pod (@failed_pods) {
                    if ($failed_pod =~ /^pm-bulk-reporter$/i) {
                                                                                        # $failed_pod : eric-pm-bulk-reporter-75555cc5d9-mw5j9 (READY is 1/2)
                        $failed_pod =~ s/\+.*//;                                        # Remove everything after a space $failed_pod : eric-pm-bulk-reporter-75555cc5d9-mw5j9
                        $delete_pod = "kubectl delete pod ".$failed_pod;
                        push(@include_list,"eric-pm-bulk-reporter.+");
                        last;
                    }
                }
                if ($delete_pod) {
                    # Delete the pod
                    General::Logging::log_user_error_message("Pod $delete_pod is not in expected STATUS 'Running', restarting the pod\n");
                    $rc = General::OS_Operations::send_command(
                        {
                            "command"       => $delete_pod,
                            "hide-output"   => 1,
                            "return-output" => \@result,
                            "return-stderr" => \@std_error,
                        }
                    );

                    # Check if pod is coming up and running x/x
                    $rc = ADP::Kubernetes_Operations::check_pod_status(
                        {
                            "all-namespaces"                            => 0,
                            "debug-messages"                            => 0,
                            "delay-time"                                => 10,
                            "ignore-ready-check-for-completed-status"   => 1,
                            "hide-output"                               => 1,
                            "max-attempts"                              => 0,
                            "max-time"                                  => $max_time,
                            "namespace"                                 => $sc_namespace,
                            "pod-include-list"                          => @include_list,
                            "return-output"                             => \@result,
                            "return-failed-pods"                        => \@failed_pods,
                            "wanted-ready"                              => "same",
                            "wanted-status"                             => "up",
                        }
                    );
                    if ($rc != 0) {
                        $message = join("\n",@failed_pods);
                    }
                }
            }
        }
        if ($rc != 0) {
            if ($::JOB_PARAMS{"IGNORE_FAILED_HEALTH_CHECK"} =~ /^yes$/i) {
                General::Logging::log_user_warning_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n$message\nBut ignored because IGNORE_FAILED_HEALTH_CHECK=yes.");
                # Ignore the error and continue like everything was OK
                $rc = 0;
            } else {
                General::Logging::log_user_error_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n$message");
            }

            if ($rc == 0) {
                push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} Post POD status check was successful (errors ignored)";
            } else {
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} Post POD status check failed";
            }
            return $rc;
        }

        if (scalar @result == 1 && $result[0] =~ /No resources found/) {
            if ($::JOB_PARAMS{"IGNORE_FAILED_HEALTH_CHECK"} =~ /^yes$/i) {
                General::Logging::log_user_warning_message("No Pods found.\nBut ignored because IGNORE_FAILED_HEALTH_CHECK=yes.");
                # Ignore the error and continue like everything was OK
                push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} Post POD status check was successful (errors ignored)";
                return 0;
            } else {
                General::Logging::log_user_error_message("No Pods found");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} Post POD status check failed";
                return 1;
            }
        }

        General::Logging::log_user_message("POD status check was successful");
        push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} Post POD status check was successful";

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
    sub Initialize_Database_If_BSF_Deployment_P402S02T17 {

        my $cmyp_ip = exists $::JOB_PARAMS{'CMYP_IP'} ? $::JOB_PARAMS{'CMYP_IP'} : ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $cmyp_password = $::JOB_PARAMS{'CMYP_PASSWORD'};
        my $cmyp_user = $::JOB_PARAMS{'CMYP_USER'};
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $rc = 0;
        my @result;

        # Check if the nodetype includes BSF
        if ($::JOB_PARAMS{'nodetype'} !~ /bsf/) {
            General::Logging::log_user_message("Initialize the database");
            print "Job variable 'nodetype' does not include 'bsf', no need to initialize the database";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Initialize the database
        General::Logging::log_user_message("Initialize the database");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command " .
                                    "--ip=$cmyp_ip " .
                                    "--user=$cmyp_user " .
                                    "--password='$cmyp_password' " .
                                    # TODO: Do we need to figure out what datacenter names to use from the already deployed software ?
                                    # "--command='bsf-function nf-instance BSF-Node-1 bsf-service BindingSupportFunction binding-database initialize-db datacenter { name datacenter1 replication-factor 2 } datacenter { name datacenter2 replication-factor 2 }' " .
                                    "--command='bsf-function nf-instance bsf1 bsf-service serviceName1 binding-database initialize-db datacenter { name datacenter1 replication-factor 2 }' " .
                                    "--command='exit'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            General::Logging::log_user_message("Database initialized");
        } else {
            General::Logging::log_user_error_message((join "\n", @result) . "\nFailed to initialize the database");
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
sub Perform_Post_Test_Checks_P402S03 {

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
sub Fallback001_P402S99 {

    my $rc = 0;

    # Set job type again since it might have been changed by the other main playlists.
    $::JOB_PARAMS{'JOBTYPE'} = "MAINTAINABILITY_TEST_DISASTER_RECOVERY";

    # Now we need to execute the cleanup.
    General::State_Machine::remove_always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

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
# This subroutine print a message to the progress log and to the status log for
# every started playlist.
#
sub print_start_playlist_message {
    my $playlist_name = shift;
    my $message = "";

    General::Message_Generation::print_message_box(
        {
            "messages"      => [
                "",
                "Playlist $playlist_name",
                "",
            ],
            "align-text"    => "left",
            "return-output" => \$message,
        }
    );
    General::Logging::log_user_message($message);
    push @::JOB_STATUS, "(-) <<<<< Starting Playlist $playlist_name >>>>>";
}

# -----------------------------------------------------------------------------
sub set_playlist_variables {
    # Define playlist specific variables that is not already defined in the
    # sub playlist Playlist::932_Test_Case_Common_Logic.
    %playlist_variables = (
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

        # ---------------------------------------------------------------------
        'SOFTWARE_DIR' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "directory", # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the base directory that holds all the software to be deployed or
upgraded on the node, i.e. the directory that contains the 'eric-sc-*.csar' and
'eric-sc-tools-*.tgz' files.
This directory should contain the same software that is currently deployed on
the cluster, and it will be used when redeploying the software as part of the
Disaster Recover Maintainability test performed by this playlist.
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

This Playlist performs a maintainability test case that verifies disaster recovery.

Used Job Parameters:
====================
EOF
    # Special handling for this playlist so we print out all variable information from
    # the main playlist and the all other called main playlists.
    use File::Basename qw(dirname basename);
    my $length;
    my $name;
    my $message;
    my $path_to_playlist = dirname(__FILE__);
    my $playlist_name = basename(__FILE__);
    $playlist_name =~ s/\.pm//;
    my %temp_hash = ( %Playlist::932_Test_Case_Common_Logic::playlist_variables, %Playlist::003_Undeploy_SC::playlist_variables, %Playlist::001_Deploy_SC::playlist_variables, %playlist_variables );

    General::Playlist_Operations::print_info_about_job_variables(\%temp_hash);

    $message = "# Global variable access in playlist $playlist_name #";
    $length = length($message);
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables(__FILE__, \%playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);

    no strict;
    for $playlist_name ("001_Deploy_SC", "003_Undeploy_SC", "932_Test_Case_Common_Logic") {
        $message = "# Global variable access in playlist $playlist_name #";
        $length = length($message);
        printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
        $name = "Playlist::${playlist_name}::playlist_variables";
        General::Playlist_Operations::print_used_job_variables("$path_to_playlist/$playlist_name.pm", \%$$name);
        General::Playlist_Operations::print_used_network_config_variables("$path_to_playlist/$playlist_name.pm");
    }
    use strict;
}

# -----------------------------------------------------------------------------
sub usage_return_playlist_variables {
    return ( %Playlist::932_Test_Case_Common_Logic::playlist_variables, %Playlist::003_Undeploy_SC::playlist_variables, %Playlist::001_Deploy_SC::playlist_variables, %playlist_variables );
}

1;
