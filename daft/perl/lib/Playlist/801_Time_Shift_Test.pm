package Playlist::801_Time_Shift_Test;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.59
#  Date     : 2024-06-20 14:42:44
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

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;
use Playlist::917_Pre_Healthcheck;
use Playlist::918_Post_Healthcheck;
use Playlist::920_Check_Network_Config_Information;
use Playlist::933_Handle_KPI_Statistics;
use Playlist::936_Check_Deployed_Software;
use Playlist::937_Generate_New_ECCD_Front_Proxy_CA;

#
# Variable Declarations
#
my %playlist_variables;
set_playlist_variables();

my $debug_command = "";
my $highest_verified_eccd_version = "2.26.0";

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

    # Set job type
    $::JOB_PARAMS{'JOBTYPE'} = "TIME_SHIFT_TEST";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    if ($::JOB_PARAMS{'BYPASS_CONFIRMATION'} eq "no") {
        # Give warning message to user to have confirmation that execution should continue.
        $rc = General::Playlist_Operations::show_checkpoint("$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/messages/time_shift_confirmation.txt");
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED" if ($rc == General::Playlist_Operations::RC_FALLBACK);
        return $rc if $rc < 0;
    }

    # Check if network configuration file is specified
    if (Playlist::920_Check_Network_Config_Information::is_network_config_specified() != 0) {
        General::Logging::log_user_error_message("You must specify a network configuration file for this Playlist to run");
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    # Set tasks to always execute
    General::State_Machine::always_execute_task("Playlist::914_Collect_Logs.+");
    General::State_Machine::always_execute_task("Playlist::918_Post_Healthcheck.+");
    General::State_Machine::always_execute_task("Playlist::801_Time_Shift_Test::Change_To_CCD_Ansible_Directory_P801S07T02");
    General::State_Machine::always_execute_task("Playlist::801_Time_Shift_Test::.+P801S08.+");
    General::State_Machine::always_execute_task("Playlist::801_Time_Shift_Test::.+P801S09.+");

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    #
    # Perform pre time shift procedures
    #

    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P801S01, \&Fallback001_P801S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P801S02, \&Fallback001_P801S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Configuration_Files_If_Specified_P801S03, \&Fallback001_P801S99 );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_PRE_HEALTHCHECK'} eq "no") {

        $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Test_Case_Health_Check_P801S04, \&Fallback001_P801S99 );
        return $rc if $rc < 0;

    }

    if ($::JOB_PARAMS{'SKIP_COLLECT_LOGS'} eq "no") {

        $rc = General::Playlist_Operations::execute_step( \&Collect_Pre_Test_Case_Logfiles_P801S05, \&Fallback001_P801S99 );
        return $rc if $rc < 0;

    }

    #
    # Perform the time shift procedure
    #

    $rc = General::Playlist_Operations::execute_step( \&Prepare_For_Time_Shift_P801S06, \&Fallback001_P801S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Prepare_For_Ansible_Playbooks_P801S07, \&Fallback001_P801S99 );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo") {
        $rc = General::Playlist_Operations::execute_step( \&Perform_Time_Shift_Procedure_NONCAPO_P801S08, \&Fallback001_P801S99 );
        return $rc if $rc < 0;
    } elsif ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "capo") {
        $rc = General::Playlist_Operations::execute_step( \&Perform_Time_Shift_Procedure_CAPO_P801S09, \&Fallback001_P801S99 );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_error_message("Unknown ECCD_STACK_TYPE (=$::JOB_PARAMS{'ECCD_STACK_TYPE'}), cannot perform the Time Shift procedure for this node");
        $rc = General::Playlist_Operations::execute_step( \&Do_Fallback_P801S98, \&Fallback001_P801S99 );
        return $rc if $rc < 0;
    }

    #
    # Perform post time shift procedures
    #

    $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Time_Shift_Check_P801S10, \&Fallback001_P801S99 );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} eq "no") {

        $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Case_Health_Check_P801S11, \&Fallback001_P801S99 );
        return $rc if $rc < 0;

    }

    if ($::JOB_PARAMS{'SKIP_COLLECT_LOGS'} eq "no") {

        $rc = General::Playlist_Operations::execute_step( \&Collect_Post_Test_Case_Logfiles_P801S12, \&Fallback001_P801S99 );
        return $rc if $rc < 0;

    }

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P801S13, \&Fallback001_P801S99 );
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
sub Initialize_Job_Environment_P801S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::901_Initialize_Job_Environment::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_New_Directories_For_Changing_tolerationSeconds_P801S01T01 } );
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
    sub Create_New_Directories_For_Changing_tolerationSeconds_P801S01T01 {

        my @commands = ( "mkdir -p $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/tolerationSeconds_new", "mkdir -p $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/tolerationSeconds_original" );
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Creating directories for changing pod tolerationSeconds from 0 to 30");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => $hide_output,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

            General::Logging::log_user_error_message("Failed to create the directories");
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
sub Check_Job_Parameters_P801S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P801S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::936_Check_Deployed_Software::main } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'P920_TASK'} = "READ_PARAMETERS_FROM_FILE";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::920_Check_Network_Config_Information::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_CMYP_User_Info_P801S02T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_ECCD_IMAGE_RELEASE_Version_P801S02T03 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "capo") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Access_To_LCM_Node_P801S02T04 } );
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
    sub Check_Job_Parameters_P801S02T01 {

        my $rc = 0;
        my $sc_namespace = "";
        my $sc_release_name = "";

        # Get the proper SC_NAMESPACE
        General::Logging::log_user_message("Checking Job parameter 'SC_NAMESPACE' and Network Config parameter 'sc_namespace'");
        if ($::JOB_PARAMS{'SC_NAMESPACE'} ne "") {
            $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'SC_NAMESPACE'} = $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'};
                $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'sc_namespace' has not been set and Job parameter 'SC_NAMESPACE' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'SC_NAMESPACE', nor Network Config parameter 'sc_namespace' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper SC_RELEASE_NAME
        General::Logging::log_user_message("Checking Job parameter 'SC_RELEASE_NAME' and Network Config parameter 'sc_release_name'");
        if ($::JOB_PARAMS{'SC_RELEASE_NAME'} ne "") {
            $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'sc_release_name'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'sc_release_name'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'SC_RELEASE_NAME'} = $::NETWORK_CONFIG_PARAMS{'sc_release_name'}{'value'};
                $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
            } else {
                General::LogginGg::log_user_error_message("Network Config parameter 'sc_release_name' has not been set and Job parameter 'SC_RELEASE_NAME' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'SC_RELEASE_NAME', nor Network Config parameter 'sc_release_name' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        General::Logging::log_user_message("SC_NAMESPACE=$sc_namespace\nSC_RELEASE_NAME=$sc_release_name\n");

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
    sub Check_CMYP_User_Info_P801S02T02 {

        my $rc = 0;

        # Find the used password for the 'expert' user.
        for my $key (keys %::NETWORK_CONFIG_PARAMS) {
            next unless ($key =~ /^default_user_\d+$/);
            if ($::NETWORK_CONFIG_PARAMS{$key}{'user'} eq "expert") {
                if ($::JOB_PARAMS{'CMYP_USER'} eq "") {
                    $::JOB_PARAMS{'CMYP_USER'} = $::NETWORK_CONFIG_PARAMS{$key}{'user'};
                }
                if ($::JOB_PARAMS{'CMYP_PASSWORD'} eq "") {
                    $::JOB_PARAMS{'CMYP_PASSWORD'} = $::NETWORK_CONFIG_PARAMS{$key}{'initial_password'};
                }
                if ($::JOB_PARAMS{'CMYP_NEW_PASSWORD'} eq "") {
                    $::JOB_PARAMS{'CMYP_NEW_PASSWORD'} = $::NETWORK_CONFIG_PARAMS{$key}{'password'};
                }
            } elsif ($::NETWORK_CONFIG_PARAMS{$key}{'user'} eq "sec-admin") {
                if ($::JOB_PARAMS{'SEC_ADMIN_USER'} eq "") {
                    $::JOB_PARAMS{'SEC_ADMIN_USER'} = $::NETWORK_CONFIG_PARAMS{$key}{'user'};
                }
                if ($::JOB_PARAMS{'SEC_ADMIN_PASSWORD'} eq "") {
                    $::JOB_PARAMS{'SEC_ADMIN_PASSWORD'} = $::NETWORK_CONFIG_PARAMS{$key}{'initial_password'};
                }
                if ($::JOB_PARAMS{'SEC_ADMIN_NEW_PASSWORD'} eq "") {
                    $::JOB_PARAMS{'SEC_ADMIN_NEW_PASSWORD'} = $::NETWORK_CONFIG_PARAMS{$key}{'password'};
                }
            }
        }

        General::Logging::log_user_message("CMYP_USER = $::JOB_PARAMS{'CMYP_USER'}\nSEC_ADMIN_USER = $::JOB_PARAMS{'SEC_ADMIN_USER'}\n");

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
    sub Check_ECCD_IMAGE_RELEASE_Version_P801S02T03 {

        my $rc = 0;

        if (version->parse($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}) < version->parse("2.17.0")) {
            General::Logging::log_user_error_message("The current ECCD_IMAGE_RELEASE is $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} which is lower than the 2.17.0 version supported by this playtlist.\nExecution of this playlist cannot continue.\n");
            $rc = General::Playlist_Operations::RC_FALLBACK;
        } elsif (version->parse($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}) > version->parse($highest_verified_eccd_version)) {
            # Give warning message to user to have confirmation that execution should continue.
            $rc = General::Playlist_Operations::show_checkpoint_message("The current ECCD_IMAGE_RELEASE is $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} which is higher than $highest_verified_eccd_version which has been verified to work with this playlist.\nIf you continue the execution and you run into issues due to changes in the time shift procedure in the current ECCD version, then this playlist must be updated.\nIf however there are no issues and the time shift procedure has not changed then the variable 'highest_verified_eccd_version' value at the top of this playlist should be updated with the higher value.\n");
            $::JOB_PARAMS{'JOBSTATUS'} = "FAILED" if ($rc == General::Playlist_Operations::RC_FALLBACK);
            return $rc if $rc < 0;
        } else {
            General::Logging::log_user_message("The current ECCD_IMAGE_RELEASE is $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} which has been tested and should be supported by this playlist");
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
    sub Check_Access_To_LCM_Node_P801S02T04 {

        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;

        General::Logging::log_user_message("Checking if LCM node can be reached without password");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"          => [
                    "hostname",
                    "which ccdadm",
                ],
                "hide-output"       => $hide_output,
                "ip"                => $::JOB_PARAMS{'eccd_capo_lcm_host_address'},
                "user"              => $::JOB_PARAMS{'eccd_capo_lcm_user'},
                "stop-on-error"     => 1,
                "use-standard-ssh"  => "yes",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to access LCM node with ssh at $::JOB_PARAMS{'eccd_capo_lcm_user'}\@$::JOB_PARAMS{'eccd_capo_lcm_host_address'}\nMake sure the key from file '~/.ssh/id_rsa.pub' from this host is included in the '~/.ssh/authorized_keys' file on the LCM host.\n");
            return $rc;
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
sub Check_Configuration_Files_If_Specified_P801S03 {

    my $rc;

    if (exists $::JOB_PARAMS{'SOFTWARE_DIR'} && $::JOB_PARAMS{'SOFTWARE_DIR'} ne "") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Unpack_SC_Umbrella_File_P801S03T01 } );
        return $rc if $rc < 0;
    } else {
        $rc = General::Playlist_Operations::RC_STEPOUT;
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
    sub Unpack_SC_Umbrella_File_P801S03T01 {

        my $dir = $::JOB_PARAMS{'_JOB_CONFIG_DIR'},
        my $rc = 0;
        my @result;
        my $software_file = "$::JOB_PARAMS{'SOFTWARE_DIR_NAME'}/$::JOB_PARAMS{'SOFTWARE_FILE_NAME'}";

        General::Logging::log_user_message("Unpacking umbrella file.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "unzip -j $software_file 'Definitions/OtherTemplates/eric-sc-umbrella-*.t*gz' -d $dir",
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to unpack the umbrella file");
            return $rc;
        }

        General::Logging::log_user_message("Check that expected file was unpacked");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ls -1 --color=never $dir/eric-sc-umbrella-*.t*gz",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Not all expected files was found");
            return 1;
        }

        $::JOB_PARAMS{'SC_UMBRELLA_FILE'} = "";
        for (@result) {
            if (/^(.+eric-sc-umbrella-.+)$/) {
                $::JOB_PARAMS{'SC_UMBRELLA_FILE'} = $1;
            }
        }
        if ($::JOB_PARAMS{'SC_UMBRELLA_FILE'} eq "") {
            General::Logging::log_user_error_message("Did not find the expected 'eric-sc-umbrella-*t*gz' file");
            return 1;
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
sub Perform_Pre_Test_Case_Health_Check_P801S04 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::917_Pre_Healthcheck::main } );
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
sub Collect_Pre_Test_Case_Logfiles_P801S05 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
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
sub Prepare_For_Time_Shift_P801S06 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_SC_Certificates_P801S06T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_And_Check_ETCD_Certificates_P801S06T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_And_Check_Cluster_Certificates_P801S06T03 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'DISABLE_PASSWORD_MAX_AGE'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Disable_Password_Max_Age_P801S06T04 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'DISABLE_ACCOUNT_DORMANT_TIMER'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Disable_Account_Dormant_Timer_P801S06T05 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'SKIP_KPI_COLLECTION'} eq "no" && $::JOB_PARAMS{'APPLICATION_TYPE'} eq "sc") {
        $::JOB_PARAMS{'P933_TASK'} = "START_KPI_COLLECTION";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::933_Handle_KPI_Statistics::main } );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Current_Time_P801S06T06 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_WCDB_Configmap_Expiry_Date_P801S06T07 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_New_Directories_For_Changing_tolerationSeconds_P801S06T08 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Collect_Information_In_The_Background_P801S06T09 } );
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
    sub Print_SC_Certificates_P801S06T01 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/parse_certificate_validity_info.pl";
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        # Initialize the expiry date for TLS certificate
        $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY'} = "";
        $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_SECRET'} = "";
        $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY_EPOCH'} = "";

        General::Logging::log_user_message("Printing certificate information for namespace $sc_namespace.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command --namespace=$sc_namespace --sort-by=not-after --print-simple-table",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            # Find expiry date of the SIP-TLS certificate and store it for later use when
            # doing the time shift procedure close to the 10-year mark.
            for (@result) {
                if (/^.+(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\s+\S+)\s+InternalCertificate\s+(eric-sec-sip-tls-dced-client-cert|eric-sec-sip-tls-kafka-client-cert|eric-sec-sip-tls-wdc-certs|eric-sec-sip-tls-wdc-tls-cert)\s+/) {
                    # 2022-01-25 12:50:31 GMT    2032-01-26 00:51:01 GMT    InternalCertificate   eric-sec-sip-tls-dced-client-cert (cert.pem)
                    $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY'} = $1;
                    $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_SECRET'} = $2;
                    $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY_EPOCH'} = General::OS_Operations::epoch_time($1);
                    last;
                }
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute the command:\n$command");
            return $rc;
        }

        General::Logging::log_user_message(join "\n", @result);

        if ($::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY'} eq "" || $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_SECRET'} eq "") {
            # Something has gone wrong
            General::Logging::log_user_error_message("Failed to find the original SIP TLS Certificate Expiry time");
            $rc = 1;
        } else {
            General::Logging::log_user_message(sprintf "The SIP TLS certificate in secret '%s' will expire on date %s", $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_SECRET'}, General::OS_Operations::format_time_iso($::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY_EPOCH'}));

            # Check to make sure the certificate is valid for at least 9 years
            my $valid_seconds = $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY_EPOCH'} - time();
            if ($valid_seconds < 284083200) {   # 284083200 = (365 * 9 + 3) * 24 * 3600 = (365 days/year * 9 years + 3 leap days over 9 years) * 24 hours per day * 3600 seconds per hour
                General::Logging::log_user_error_message(sprintf "The SIP TLS certificate IS NOT VALID for a minimum of 9 years into the future which is expected.\nIt's only valid for %s.\nIf you continue anyway then other errors might be detected further on!\n", General::OS_Operations::convert_seconds_to_wdhms($valid_seconds));
                $rc = 1;
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
    sub Print_And_Check_ETCD_Certificates_P801S06T02 {

        my $master0_ip = ADP::Kubernetes_Operations::get_node_ip( { "type" => "master", "index" => 0 } );
        my $command = sprintf "ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@%s 'certs=\$(ls /etc/etcd/ |grep .crt );for c in \$certs; do printf \"%%-10s \" \$c; sudo -n openssl x509 -in /etc/etcd/\$c --enddate --nocert; done'", $master0_ip;
        my $expiry_date;
        my $expiry_epoch;
        my $rc = 0;
        my @result;

        # Save the IP address so we don't have to check every time
        $::JOB_PARAMS{'P801_MASTER_0_IP_ADDRESS'} = $master0_ip;

        # First set the earliest expiry date to a very high value which is "Sat 20 Nov 2286 05:46:39 PM UTC"
        $::JOB_PARAMS{'P801_ORIGINAL_ETCD_CERTIFICATE_EXPIRY_EPOCH'} = 9999999999;
        $::JOB_PARAMS{'P801_ORIGINAL_ETCD_CERTIFICATE_EXPIRY'} = "";

        if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "capo") {
            General::Logging::log_user_message("For CAPO installations we cannot check ETCD certificate information as before, so we skip this task");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Printing ETCD Certificate Information");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
        }

        General::Logging::log_user_message(join "\n", @result);

        # Returns something like this:
        # ca.crt     notAfter=Nov 30 12:39:42 2042 GMT
        # peer.crt   notAfter=Nov 30 12:39:52 2042 GMT
        # server.crt notAfter=Nov 30 12:39:51 2042 GMT
        for (@result) {
            if (/^.+notAfter=(.+)/) {
                $expiry_date = $1;
                $expiry_epoch = General::OS_Operations::epoch_time($1);
                if ($expiry_epoch && $expiry_epoch < $::JOB_PARAMS{'P801_ORIGINAL_ETCD_CERTIFICATE_EXPIRY_EPOCH'}) {
                    $::JOB_PARAMS{'P801_ORIGINAL_ETCD_CERTIFICATE_EXPIRY_EPOCH'} = $expiry_epoch;
                    $::JOB_PARAMS{'P801_ORIGINAL_ETCD_CERTIFICATE_EXPIRY'} = $expiry_date;
                }
            }
        }

        if ($::JOB_PARAMS{'P801_ORIGINAL_ETCD_CERTIFICATE_EXPIRY'} ne "") {
            General::Logging::log_user_message(sprintf "The ETCD certificate that expire first will be on date %s", General::OS_Operations::format_time_iso($::JOB_PARAMS{'P801_ORIGINAL_ETCD_CERTIFICATE_EXPIRY_EPOCH'}));
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("No valid expiry date was found");
            $rc = 1;
        }

        if ($::JOB_PARAMS{'P801_ORIGINAL_ETCD_CERTIFICATE_EXPIRY_EPOCH'} - time() < (9 * 365 * 24 * 3600)) {
            General::Logging::log_user_error_message("The certificate is not valid for at least 9 years into the future.\nPlease check that you have deployed the software with long valid certificates.\n");
            $rc = 1;
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
    sub Print_And_Check_Cluster_Certificates_P801S06T03 {

        my $master0_ip = $::JOB_PARAMS{'P801_MASTER_0_IP_ADDRESS'};
        my $command = sprintf "ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@%s 'sudo -n /usr/local/bin/kubeadm certs check-expiration'", $master0_ip;
        my $expiry_date;
        my $expiry_epoch;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Printing Cluster Certificate Information");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
        }

        General::Logging::log_user_message(join "\n", @result);

        # First set the earliest expiry date to a very high value which is "Sat 20 Nov 2286 05:46:39 PM UTC"
        $::JOB_PARAMS{'P801_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH'} = 9999999999;
        $::JOB_PARAMS{'P801_ORIGINAL_ECCD_CERTIFICATE_EXPIRY'} = "";

        # Returns something like this:
        # CERTIFICATE                EXPIRES                  RESIDUAL TIME   CERTIFICATE AUTHORITY   EXTERNALLY MANAGED
        # admin.conf                 Dec 02, 2032 12:41 UTC   9y              ca                      no
        # apiserver                  Dec 02, 2032 12:41 UTC   9y              ca                      no
        # apiserver-kubelet-client   Dec 02, 2032 12:41 UTC   9y              ca                      no
        # controller-manager.conf    Dec 02, 2032 12:41 UTC   9y              ca                      no
        # front-proxy-client         Dec 02, 2032 12:41 UTC   9y              front-proxy-ca          no
        # scheduler.conf             Dec 02, 2032 12:41 UTC   9y              ca                      no
        #
        # CERTIFICATE AUTHORITY   EXPIRES                  RESIDUAL TIME   EXTERNALLY MANAGED
        # ca                      Jul 13, 2048 09:07 UTC   25y             no
        # front-proxy-ca          Dec 02, 2032 12:41 UTC   9y              no
        for (@result) {
            next if (/^\s*(CERTIFICATE|CERTIFICATE AUTHORITY)\s+EXPIRES.+/);
            if (/^\S+\s+(\S+\s+\d+,\s+\d+\s+\d+:\d+\s+\S+)\s+/) {
                $expiry_date = $1;
                $expiry_epoch = General::OS_Operations::epoch_time($1);
                if ($expiry_epoch && $expiry_epoch < $::JOB_PARAMS{'P801_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH'}) {
                    $::JOB_PARAMS{'P801_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH'} = $expiry_epoch;
                    $::JOB_PARAMS{'P801_ORIGINAL_ECCD_CERTIFICATE_EXPIRY'} = $expiry_date;
                }
            }
        }

        if ($::JOB_PARAMS{'P801_ORIGINAL_ECCD_CERTIFICATE_EXPIRY'} ne "") {
            General::Logging::log_user_message(sprintf "The ECCD certificate that expire first will be on date %s", General::OS_Operations::format_time_iso($::JOB_PARAMS{'P801_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH'}));
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("No valid expiry date was found");
            $rc = 1;
        }

        if ($::JOB_PARAMS{'P801_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH'} - time() < (9 * 365 * 24 * 3600)) {
            General::Logging::log_user_error_message("The certificate is not valid for at least 9 years into the future.\nPlease check that you have deployed the software with long valid certificates.\n");
            $rc = 1;
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
    sub Disable_Password_Max_Age_P801S06T04 {

        my $cmyp_password = exists $::JOB_PARAMS{'CMYP_PASSWORD'} ? $::JOB_PARAMS{'CMYP_PASSWORD'} : "admin";
        my $cmyp_user = exists $::JOB_PARAMS{'CMYP_USER'} ? $::JOB_PARAMS{'CMYP_USER'} : "rootroot";
        my $cmyp_ip = ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Disable Password max-age");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password='$cmyp_password' --ip=$cmyp_ip --command='config' --command='system authentication default-password-policy max-age 0' --command='commit' --command='exit' --command='exit'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
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
    sub Disable_Account_Dormant_Timer_P801S06T05 {

        my $cmyp_password = exists $::JOB_PARAMS{'CMYP_PASSWORD'} ? $::JOB_PARAMS{'CMYP_PASSWORD'} : "admin";
        my $cmyp_user = exists $::JOB_PARAMS{'CMYP_USER'} ? $::JOB_PARAMS{'CMYP_USER'} : "rootroot";
        my $cmyp_ip = ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Disable Account dormant-timer");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$command --user=$cmyp_user --password='$cmyp_password' --ip=$cmyp_ip --command='config' --command='system authentication default-account-policy dormant-timer 0' --command='commit' --command='exit' --command='exit'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
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
    sub Print_Current_Time_P801S06T06 {

        my $command = "timedatectl";
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Check the current time");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => $hide_output,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
        }

        General::Logging::log_user_message(join "\n", @result);

        # Save current epoch time for later checks after each time shift
        $::JOB_PARAMS{'PREVIOUS_EPOCH_TIME'} = time();

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
    sub Get_WCDB_Configmap_Expiry_Date_P801S06T07 {

        my $command = "";
        my $configmap;
        my $current_expiry_date = "";
        my $previous_expiry_date = "";
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        @result = ADP::Kubernetes_Operations::get_resource_names(
            {
                "resource"      => "configmap",
                "namespace"     => $sc_namespace,
                "include-list"  => [ "^eric-data-wide-column-database-cd-tls-restarter", "^eric-bsf-wcdb-cd-tls-restarter", "^eric-dsc-wcdb-cd-tls-restarter" ],
                "hide-output"   => 1,
            }
        );
        if (scalar @result == 0) {
            General::Logging::log_user_message("Skip this task because we did not find any of the folloing configmaps:\n  - eric-data-wide-column-database-cd-tls-restarter\n  - eric-bsf-wcdb-cd-tls-restarter\n  - eric-dsc-wcdb-cd-tls-restarter\n");
            return General::Playlist_Operations::RC_TASKOUT;
        } elsif (scalar @result > 1) {
            General::Logging::log_user_error_message("Unexpected number of configmap returned, only expecting 0 or 1" . (join "\n", @result));
            return 1;
        }

        $configmap = $result[0];

        $command = sprintf 'kubectl -n %s get configmap %s -o yaml', $sc_namespace, $configmap;

        General::Logging::log_user_message("Fetching configmap $configmap expiry information");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get configmap");
            return 1;
        }

        # Returns something like this:
        #apiVersion: v1
        #data:
        #  eric-data-wide-column-database-cd-datacenter1-rack1-0: "2023-06-20"
        #  eric-data-wide-column-database-cd-datacenter1-rack1-1: "2023-06-20"
        #kind: ConfigMap
        #metadata:
        #  annotations:
        #  : etc.

        # Fetch just the "data:" section
        @result = ADP::Kubernetes_Operations::get_top_level_section_information("data",\@result);
        for (@result) {
            if (/^\s+(\S+):\s+"(\d{4}-\d{2}-\d{2})"\s*$/) {
                #  eric-data-wide-column-database-cd-datacenter1-rack1-0: "2023-06-20"
                #     or
                #  eric-bsf-wcdb-cd-datacenter1-rack1-0: "2024-10-23"
                $current_expiry_date = $2;
                if ($previous_expiry_date ne "" && $current_expiry_date ne $previous_expiry_date) {
                    General::Logging::log_user_error_message("The dates are not the same, $previous_expiry_date vs. $current_expiry_date");
                    return 1;
                } else {
                    $previous_expiry_date = $current_expiry_date;
                }
            }
        }

        if ($current_expiry_date ne "") {
            General::Logging::log_user_message("Expiry date: $current_expiry_date");
            $::JOB_PARAMS{'WCDB_ORIGINAL_EXPIRY_DATE'} = $current_expiry_date;
        } else {
            General::Logging::log_user_error_message("No expiry date found in the 'data' section");
            return 1;
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
    sub Create_New_Directories_For_Changing_tolerationSeconds_P801S06T08 {

        my @commands = ( "mkdir -p $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/tolerationSeconds_new", "mkdir -p $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/tolerationSeconds_original" );
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Creating directories for changing pod tolerationSeconds from 0 to 30");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => $hide_output,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

            General::Logging::log_user_error_message("Failed to create the directories");
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
    sub Collect_Information_In_The_Background_P801S06T09 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/bin/timeshift_collect_pod_logs_to_file_every_30_seconds.bash $::JOB_PARAMS{'_JOB_LOG_DIR'}/node_and_pod_status_every_30_seconds.log";
        my $rc = 0;

        General::Logging::log_user_message("Starting background process to collect POD logs");

        my $pid = General::OS_Operations::background_process_run($command);
        $::JOB_PARAMS{'LOG_COLLECT_PID'} = $pid;
        if ($pid > 0) {
            General::Logging::log_user_message("Process Id: $pid");
            $rc = 0;
        } else {
            General::Logging::log_user_error_message("Failed to start background process");
            $rc = 1;
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
sub Prepare_For_Ansible_Playbooks_P801S07 {

    my $rc;

    if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo") {

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Inventory_File_With_Workers_Included_Non_Capo_P801S07T01 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_To_CCD_Ansible_Directory_P801S07T02 } );
        return $rc if $rc < 0;

    } elsif ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "capo") {

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Inventory_File_With_Workers_Included_Capo_P801S07T03 } );
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
    sub Create_Inventory_File_With_Workers_Included_Non_Capo_P801S07T01 {

        my $role = ADP::Kubernetes_Operations::get_node_role("master");
        my $command;
        my @commands = (
            "sudo -n cp /mnt/config/inventory/ibd_inventory_file.ini /home/eccd/inventory",
            "sudo -n chown eccd:eccd /home/eccd/inventory",
            "echo '[worker]' >> /home/eccd/inventory",
            "kubectl get nodes --selector='!node-role.kubernetes.io/$role' --no-headers=true -o wide |awk '{print \$6}' >> /home/eccd/inventory",
            "sudo -n mv /home/eccd/inventory /mnt/config/inventory/ibd_inventory",
        );
        my $rc = 0;

        General::Logging::log_user_message("Create Inventory File");
        for $command (@commands) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
                return $rc;
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
    sub Change_To_CCD_Ansible_Directory_P801S07T02 {

        my $path = "/var/lib/eccd/container-images.d/erikube/ansible/erikube";

        if (chdir($path)) {
            return 0;
        } else {
            General::Logging::log_user_error_message("Failed to change to the '$path' directory");
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
    sub Create_Inventory_File_With_Workers_Included_Capo_P801S07T03 {

        my $command;
        my @commands = (
            "sudo sed -i '/host_key_checking = False/s/^#//' /etc/ansible/ansible.cfg",
            "touch $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ansible_inventory.ini",
            "echo '[master]' >> $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ansible_inventory.ini",
            "kubectl get nodes --selector='node-role.kubernetes.io/control-plane' --no-headers=true -o wide | awk '{print \$6}' >> $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ansible_inventory.ini",
            "echo '[worker]' >> $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ansible_inventory.ini",
            "kubectl get nodes --selector='node-role.kubernetes.io/worker' --no-headers=true -o wide | awk '{print \$6}' >> $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ansible_inventory.ini",
            "cat $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ansible_inventory.ini",
        );
        my $rc = 0;

        General::Logging::log_user_message("Create Inventory File");
        for $command (@commands) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
                return $rc;
            }
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
sub Perform_Time_Shift_Procedure_NONCAPO_P801S08 {

    my $length;
    my $message;
    my $rc = 0;
    my $time_offset;
    my @time_offsets = (
        # Time offset values for ECCD version 2.17 or earlier
        # Offset value        Days from original/real deployment time
        0,                  # 0 Not used
        31104000,           # 360
        62208000,           # 720
        93312000,           # 1080
        124416000,          # 1440
        155520000,          # 1800
        186624000,          # 2160
        217728000,          # 2520
        248832000,          # 2880
        279936000,          # 3240
        311040000,          # 3600
        315360000,          # 3650
    );

    if ($::JOB_PARAMS{'TIMESHIFT_360_DAYS_AT_A_TIME'} eq "no" && version->parse($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}) >= version->parse("2.18.0")) {
        # Certificates are valid for a longer period so less time shifts are needed
        if ($::JOB_PARAMS{'TIMESHIFT_1_DAY_BEFORE_CERTIFICATE_EXPIRY'} eq "yes") {
            # Shift as few times as possible to 1 day before the first certificate expire
            @time_offsets = calculate_timeshift_offsets_in_seconds();
            if (scalar @time_offsets == 0) {
                # Failed to calculate the timeshift offsets
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            # Time shift 2 times with hard coded offset values
            # 3600 and 3650 days into the future
            @time_offsets = ( 0, 311040000, 315360000 );
        }
    }

    # In case of a re-run of an interrupted job set ifnformation about which tasks to execute and which what time offset
    $::JOB_PARAMS{'P801_TIME_OFFSET_CNT'}    = exists $::JOB_PARAMS{'P801_TIME_OFFSET_CNT'}    ? $::JOB_PARAMS{'P801_TIME_OFFSET_CNT'}    : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S08T01'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S08T01'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S08T01'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S08T02'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S08T02'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S08T02'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S08T03'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S08T03'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S08T03'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S08T04'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S08T04'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S08T04'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S08T05'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S08T05'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S08T05'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S08T06'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S08T06'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S08T06'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S08T07'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S08T07'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S08T07'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S08T08'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S08T08'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S08T08'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S08T09'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S08T09'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S08T09'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S08T10'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S08T10'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S08T10'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S08T11'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S08T11'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S08T11'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S08T12'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S08T12'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S08T12'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S08T13'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S08T13'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S08T13'} : 1;
    $::JOB_PARAMS{'P801_NEW_FRONT_PROXT_CA'} = exists $::JOB_PARAMS{'P801_NEW_FRONT_PROXT_CA'} ? $::JOB_PARAMS{'P801_NEW_FRONT_PROXT_CA'} : 1;
    $::JOB_PARAMS{'P801_ROTATE_REGISTRY'}    = exists $::JOB_PARAMS{'P801_ROTATE_REGISTRY'}    ? $::JOB_PARAMS{'P801_ROTATE_REGISTRY'}    : 1;

    for (; $::JOB_PARAMS{'P801_TIME_OFFSET_CNT'} <= $#time_offsets;) {
        $time_offset = $time_offsets[$::JOB_PARAMS{'P801_TIME_OFFSET_CNT'}];

        if ($time_offset == 315360000) {
            # Always execute the task
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Fetch_Time_Offset_For_10_Year_Change_P801S08T01 } );
            return $rc if $rc < 0;
            $time_offset = $::JOB_PARAMS{'P801_10_YEAR_OFFSET'};

            # Some extra checks that can maybe be removed in a future update when we know
            # everything works as expected with calculating the new offset
            my $time_difference_from_current_time = $time_offset - 315360000;
            $time_difference_from_current_time = General::OS_Operations::convert_seconds_to_dhms($time_difference_from_current_time, 0);
            $rc = General::Playlist_Operations::show_checkpoint_message("Check that the time offset value '$time_offset' is correct before you continue the execution.\nThis offset is '$time_difference_from_current_time' from current time.\n");
            return $rc if $rc < 0;

            if ($::JOB_PARAMS{'P801_TIME_OFFSET_CNT'} == 0) {
                # There is no need to do any further time shifts because we have reached the
                # 10 year mark.
                return 0;
            }
        }

        # Print a progress message
        $message = sprintf "# Time Shift %d of %d, Time Offset=%s #", $::JOB_PARAMS{'P801_TIME_OFFSET_CNT'}, $#time_offsets, $time_offset;
        $length = length($message);
        General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S08T02'} == 1) {
            $::JOB_PARAMS{'P801_TIME_OFFSET'} = $time_offset;
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_Time_NTP_Offset_In_All_Nodes_P801S08T02 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S08T02'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S08T03'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Restart_Chronyd_P801S08T03 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S08T03'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S08T04'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Verify_Time_Change_P801S08T04 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S08T04'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S08T05'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Restart_Kube_Controller_Manager_PODs_P801S08T05 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S08T05'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S08T06'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Restart_calico_and_multus_Daemonset_P801S08T06 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S08T06'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S08T07'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Pods_To_Come_Up_P801S08T07 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S08T07'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S08T08'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Restart_WCDB_Statefulset_P801S08T08 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S08T08'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S08T09'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Execute_Certificate_Rotation_Playbooks_P801S08T09 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S08T09'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S08T10'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_And_Check_Cluster_Certificates_P801S08T10 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S08T10'} = 0;
        }

        if ($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} =~ /^2\.(17|19)\.\d+$/ || version->parse($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}) >= version->parse("2.20.0")) {
            if ($::JOB_PARAMS{'P801_EXECUTE_P801S08T11'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Update_Kube_Config_File_P801S08T11 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P801_EXECUTE_P801S08T11'} = 0;
            }
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S08T12'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Cluster_Health_P801S08T12 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S08T12'} = 0;
        }

        # If needed to go beyond 10 years for ECCD 2.17 and newer, Generate a new Front-Proxy-CA
        if ($::JOB_PARAMS{'P801_NEW_FRONT_PROXT_CA'} == 1 && $time_offset >= 311040000) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::937_Generate_New_ECCD_Front_Proxy_CA::main } );
            return $rc if $rc < 0;

            # Mark it not necessary to generate a new Front Proxy CA
            $::JOB_PARAMS{'P801_NEW_FRONT_PROXT_CA'} = 0;
        }

        # If needed to go beyond 10 years for ECCD 2.17 and newer, Rotate Container Registry and Internal Registry
        if ($::JOB_PARAMS{'P801_ROTATE_REGISTRY'} == 1 && $time_offset >= 311040000) {
            if ($::JOB_PARAMS{'P801_EXECUTE_P801S08T13'} == 1) {
                $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Rotate_Container_And_Internal_Registry_P801S08T13 } );
                return $rc if $rc < 0;
                $::JOB_PARAMS{'P801_EXECUTE_P801S08T13'} = 0;
            }

            # Mark it not necessary to rotate the container and internal registry
            $::JOB_PARAMS{'P801_ROTATE_REGISTRY'} = 0;
        }

        if ($::JOB_PARAMS{'COLLECT_LOGS_AFTER_EACH_TIMESHIFT'} eq "yes") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
            return $rc if $rc < 0;
        }

        # Step the counter and reset the executed tasks
        $::JOB_PARAMS{'P801_TIME_OFFSET_CNT'}++;
        $::JOB_PARAMS{'P801_EXECUTE_P801S08T01'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S08T02'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S08T03'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S08T04'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S08T05'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S08T06'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S08T07'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S08T08'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S08T09'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S08T10'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S08T11'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S08T12'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S08T13'} = 1;
    }

    # Print a progress message
    $message = "# Time Shift Procedure Finished #";
    $length = length($message);
    General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

    if ($rc == 0) {
        push @::JOB_STATUS, "(/) Time Shift successful";
    } else {
        push @::JOB_STATUS, "(x) Time Shift failed";
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
    sub Fetch_Time_Offset_For_10_Year_Change_P801S08T01 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/parse_certificate_validity_info.pl";
        my $expiry_timestamp;
        my $expiry_timestamp_epoch;
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sip_tls_secret_name = $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_SECRET'};

        # Set a default value
        $::JOB_PARAMS{'P801_10_YEAR_OFFSET'} = 0;

        General::Logging::log_user_message("Fetching the certificate expiry date and time for secret '$sip_tls_secret_name'");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command --namespace=$sc_namespace --print-simple-table --secret-name=$sip_tls_secret_name | grep '$sip_tls_secret_name' | grep -v ' Not ' | awk '{ print \$4,\$5,\$6 }' | head -1",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0 && scalar @result == 1 && $result[0] =~ /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}.*)/) {
            # E.g. 2021-11-19 14:08:44 GMT
            $expiry_timestamp = $1;
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute the command or wrong data returned:\n$command");
            return $rc;
        }

        if ($expiry_timestamp ne $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY'}) {
            # Starting with SC 1.6.1 the behavior of SIP-TLS certificates have changed
            # so they are renewed during the previous time shift and we need to adapt
            # the offset value.
            General::Logging::log_user_message("Expiry timestamp for secret '$sip_tls_secret_name' has changed from $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY'} to $expiry_timestamp");

            # Calculate the epoch time from the expiry date
            $command = sprintf 'date -d "%s" +%%s', $expiry_timestamp;
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0 && scalar @result == 1 && $result[0] =~ /^(\d+)$/) {
                # E.g. 311636869
                $expiry_timestamp_epoch = $1;
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute the command or wrong data returned:\n$command");
                return 1;
            }

            # Estimate what the epoch time is 10 years from the start of the job
            my $ten_years_from_job_start = $::JOB_PARAMS{'_JOB_STARTTIME'} + 315619200; # 315619200 = (365 * 10 + 3) * 24 * 3600 = (365 days/year * 10 years + 3 leap days over 10 years) * 24 hours per day * 3600 seconds per hour

            my $current_epoch_time = time();
            if ($current_epoch_time >= $ten_years_from_job_start) {
                # The current time is already past the 10 year mark, then we don't need to to any more time shift.
                $::JOB_PARAMS{'P801_10_YEAR_OFFSET'} = 0;
            } else {
                # The current time is still below the 10 year mark, do some more checking
                if ($ten_years_from_job_start <= $expiry_timestamp_epoch - 3600) {
                    # The SIP-TLS certificate is valid for a longer period of time
                    # than we need to jump forward to to reach the 10 year mark.
                    # So use the 10 year value in the calculation.
                    # 311040000 = 3600 days which is not fully 10 years
                    $::JOB_PARAMS{'P801_10_YEAR_OFFSET'} = $ten_years_from_job_start-$current_epoch_time+311040000;
                } else {
                    # The SIP-TLS certificate expire before we reach the 10 year mark
                    # So use the certificate expiry date minus 1 hour for offset.
                    # 311040000 = 3600 days which is not fully 10 years
                    $::JOB_PARAMS{'P801_10_YEAR_OFFSET'} = $expiry_timestamp_epoch-$current_epoch_time-3600+311040000;
                }
            }
        } else {
            # The expiry date for SIP-TLS certificate has not changed
            General::Logging::log_user_message("Original expiry timestamp for secret '$sip_tls_secret_name' has not changed and is $expiry_timestamp");
            $command = sprintf 'echo $(date -d "%s" +%%s)-$(date +%%s)-3600+311040000 | bc', $expiry_timestamp;

            General::Logging::log_user_message("Calculating a new time offset from current time");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0 && scalar @result == 1 && $result[0] =~ /^(\d+)$/) {
                # E.g. 311636869
                $::JOB_PARAMS{'P801_10_YEAR_OFFSET'} = $1;
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute the command or wrong data returned:\n$command");
                return 1;
            }
        }

        # An alternative way to calculate it:
        #General::Logging::log_user_message("Converting expiry date to epoch value");
        #$command = sprintf 'date -d "%s" +%%s', $expiry_timestamp;
        #$rc = General::OS_Operations::send_command(
        #    {
        #        "command"       => $command,
        #        "hide-output"   => 1,
        #        "return-output" => \@result,
        #    }
        #);
        #if ($rc == 0 && scalar @result == 1 && $result[0] =~ /^(\d+)$/) {
        #    # E.g. 311636869
        #    $expiry_timestamp_epoch = $1;
        #} else {
        #    # Display the result in case of error
        #    General::OS_Operations::write_last_temporary_output_to_progress();

        #    General::Logging::log_user_error_message("Failed to execute the command or wrong data returned:\n$command");
        #    return 1;
        #}

        #$::JOB_PARAMS{'P801_10_YEAR_OFFSET'} = ($expiry_timestamp_epoch - time() - 3600 + 311040000);

        General::Logging::log_user_message("Time offset is $::JOB_PARAMS{'P801_10_YEAR_OFFSET'} based on certificate expiry time stamp $expiry_timestamp");

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
    sub Change_Time_NTP_Offset_In_All_Nodes_P801S08T02 {

        my $command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        $command = sprintf '%ssudo ansible -a "sudo sed -i -E \'s:iburst offset [0-9]+$:iburst:g\' /etc/chrony.conf" all -i /mnt/config/inventory/ibd_inventory', $debug_command;
        General::Logging::log_user_message("Clear time offset in chrony.conf");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => $hide_output,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
        }

        $command = sprintf '%ssudo ansible -a "sudo sed -i \'s:iburst$:iburst offset %d:g\' /etc/chrony.conf" all -i /mnt/config/inventory/ibd_inventory', $debug_command, $::JOB_PARAMS{'P801_TIME_OFFSET'};
        General::Logging::log_user_message("Set new time offset in chrony.conf");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => $hide_output,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
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
    sub Restart_Chronyd_P801S08T03 {

        my $command = sprintf '%ssudo ansible -a "sudo systemctl restart chronyd" all -i /mnt/config/inventory/ibd_inventory', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Restart chronyd");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => $hide_output,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
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
    sub Verify_Time_Change_P801S08T04 {

        my $command = "timedatectl";
        my $current_time;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;
        my $time_difference;

        General::Logging::log_user_message("Waiting 5 seconds for time change to be applied");
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 0,
                "progress-message"  => 0,
                "seconds"           => 5,
                "use-logging"       => 1,
            }
        );

        $current_time = time();

        General::Logging::log_user_message("Check that the current time was updated");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => $hide_output,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
        }

        General::Logging::log_user_message(join "\n", @result);

        $time_difference = $current_time - $::JOB_PARAMS{'PREVIOUS_EPOCH_TIME'};
        if ($time_difference >= 31104000) {
            # It looks like we have have shifted time at least 360 days into the future
            # Save current epoch time for later checks after each time shift
            $::JOB_PARAMS{'PREVIOUS_EPOCH_TIME'} = $current_time;
            General::Logging::log_user_message(sprintf "It looks like the time has been shifted by %d seconds (%s).\n", $time_difference, General::OS_Operations::convert_seconds_to_wdhms($time_difference));
        } else {
            General::Logging::log_user_error_message(sprintf "It looks like the time has not been shifted with at least 360 days (31104000 seconds) as expected.\nIt has only changed by %d seconds (%s).\n", $time_difference, General::OS_Operations::convert_seconds_to_wdhms($time_difference));
            $rc = 1;
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
    sub Restart_Kube_Controller_Manager_PODs_P801S08T05 {

        my $attempt_cnt = 0;
        my $command = sprintf '%ssudo ansible -m "shell sudo crictl stop \$(sudo crictl ps --name kube-controller-manager -q)" master -i /mnt/config/inventory/ibd_inventory_file.ini && sudo ansible -m "shell sudo crictl rm \$(sudo crictl ps -a --name kube-controller-manager -q)" master -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        return 0 if ($debug_command ne "");

        if ($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} =~ /^2\.17\.\d+$/) {
            $command = sprintf '%ssudo ansible -m "shell sudo docker restart \$(sudo docker ps | grep kube-controller-manager_kube-controller-manager | awk \'{print \$1}\')" master -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        }

        while ($attempt_cnt < 2) {
            $attempt_cnt++;
            General::Logging::log_user_message("Restart kube-controller-manager, attempt $attempt_cnt of 2");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => $hide_output,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0) {
                last;
            } elsif ($attempt_cnt == 2) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

                General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
                return $rc;
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
    sub Restart_calico_and_multus_Daemonset_P801S08T06 {

        my $command;
        my @daemonsets = ();
        my $message = "";
        my $rc = 0;
        my @result;

        if (version->parse($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}) < version->parse("2.24.0")) {
            General::Logging::log_user_error_message("The current ECCD_IMAGE_RELEASE is $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} which is lower than the 2.24.0 version so no reason to do this task.\n");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        @result = ADP::Kubernetes_Operations::get_resource_names(
            {
                "resource"      => "daemonset",
                "namespace"     => "kube-system",
                "include-list"  => [ '^calico-node$', '^kube-multus-ds-amd64$' ],
                "hide-output"   => 1,
            }
        );
        if (scalar @result == 0) {
            General::Logging::log_user_message("No calico-node or kube-multus-ds-amd64 daemonset, skip this task");
            return General::Playlist_Operations::RC_TASKOUT;
        }
        for (@result) {
            if (/^(\S+)$/) {
                #NAME                                             DESIRED   CURRENT   READY   UP-TO-DATE   AVAILABLE   NODE SELECTOR                            AGE
                #calico-node                                      7         7         6       7            6           kubernetes.io/os=linux                   184d
                #kube-multus-ds-amd64                             7         7         7       7            7           kubernetes.io/arch=amd64                 183d
                push @daemonsets, $1;
            }
        }

        for my $daemonset (@daemonsets) {
            General::Logging::log_user_message("Restarting daemonset $daemonset");
            $command = sprintf '%s kubectl -n %s rollout restart daemonset %s', $debug_command, "kube-system", $daemonset;
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to restart daemonset");
                return 1;
            }

            $message = "";
            $rc = ADP::Kubernetes_Operations::check_daemonset_status(
                {
                    "namespace"         => "kube-system",
                    "check-name"        => $daemonset,
                    "debug-messages"    => 1,
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
    sub Wait_For_Pods_To_Come_Up_P801S08T07 {

        my @failed_pods = ();
        my $max_time = $::JOB_PARAMS{'POD_STATUS_TIMEOUT'};
        my $rc = 0;
        my $repeated_check_time = $::JOB_PARAMS{'POD_UP_TIMER'};
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        # The following check was implemented after discussions with Thomas Wolinski on 2023-01-13
        # because he suspects that we continue too fast with the next task to restart the WCDB
        # stateful set and this might result in that only one of the two date entries in the
        # config map is updated, causing an error because the two dates are different.
        General::Logging::log_user_message("Checking every 10 seconds that all pods in $sc_namespace namespace are up and are stable for $repeated_check_time seconds, for a maximum of $max_time seconds.");
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
                "pod-exclude-ready-list"                    => [ "eric-data-search-engine-curator.*", "eric-log-transformer.*" ],
                "pod-exclude-status-list"                   => [ "eric-data-search-engine-curator.*", "eric-log-transformer.*" ],
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
            General::Logging::log_user_error_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n$message");
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
    sub Restart_WCDB_Statefulset_P801S08T08 {

        my $command = "";
        my $configmap;
        my $current_expiry_date = "";
        my @failed_pods = ();
        my $max_time = $::JOB_PARAMS{'POD_STATUS_TIMEOUT'};
        my $message = "";
        my $previous_expiry_date = "";
        my $rc = 0;
        my $repeated_check_time = $::JOB_PARAMS{'POD_UP_TIMER'};
        my @result;
        my $statefulset;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        @result = ADP::Kubernetes_Operations::get_resource_names(
            {
                "resource"      => "statefulset",
                "namespace"     => $sc_namespace,
                "include-list"  => [ "^eric-data-wide-column-database-cd", "^eric-bsf-wcdb-cd" ],
                "hide-output"   => 1,
            }
        );
        if (scalar @result == 0) {
            General::Logging::log_user_message("No eric-data-wide-column-database-cd or eric-bsf-wcdb-cd statefulset, skip this task");
            return General::Playlist_Operations::RC_TASKOUT;
        } elsif (scalar @result > 1) {
            General::Logging::log_user_error_message("Unexpected number of statefulset returned, only expecting 0 or 1");
            return 1;
        }

        $statefulset = $result[0];

        # Check the certificate expiry date from the configmap to see if we need to
        # restart the statefulset.

        @result = ADP::Kubernetes_Operations::get_resource_names(
            {
                "resource"      => "configmap",
                "namespace"     => $sc_namespace,
                "include-list"  => [ "^eric-data-wide-column-database-cd-tls-restarter", "^eric-bsf-wcdb-cd-tls-restarter" ],
                "hide-output"   => 1,
            }
        );
        if (scalar @result == 0) {
            General::Logging::log_user_message("No eric-data-wide-column-database-cd-tls-restarter or eric-bsf-wcdb-cd-tls-restarter configmap, skip this task");
            return General::Playlist_Operations::RC_TASKOUT;
        } elsif (scalar @result > 1) {
            General::Logging::log_user_error_message("Unexpected number of configmap returned, only expecting 0 or 1");
            return 1;
        }

        $configmap = $result[0];

        $command = sprintf 'kubectl -n %s get configmap %s -o yaml', $sc_namespace, $configmap;

        General::Logging::log_user_message("Fetching configmap $configmap expiry information");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get configmap");
            return 1;
        }

        # Returns something like this:
        #apiVersion: v1
        #data:
        #  eric-data-wide-column-database-cd-datacenter1-rack1-0: "2023-06-20"
        #  eric-data-wide-column-database-cd-datacenter1-rack1-1: "2023-06-20"
        #kind: ConfigMap
        #metadata:
        #  annotations:
        #  : etc.

        # Fetch just the "data:" section
        @result = ADP::Kubernetes_Operations::get_top_level_section_information("data",\@result);
        for (@result) {
            if (/^\s+(\S+):\s+"(\d{4}-\d{2}-\d{2})"\s*$/) {
                #  eric-data-wide-column-database-cd-datacenter1-rack1-0: "2023-06-20"
                #    or
                #  eric-bsf-wcdb-cd-datacenter1-rack1-0: "2024-10-23"
                $current_expiry_date = $2;
                if ($previous_expiry_date ne "" && $current_expiry_date ne $previous_expiry_date) {
                    General::Logging::log_user_error_message("The dates are not the same, $previous_expiry_date vs. $current_expiry_date");
                    return 1;
                } else {
                    $previous_expiry_date = $current_expiry_date;
                }
            }
        }

        if ($current_expiry_date eq "") {
            General::Logging::log_user_error_message("No expiry date found in the 'data' section");
            return 1;
        }

        General::Logging::log_user_message("Expiry date: $current_expiry_date");
        if ($::JOB_PARAMS{'WCDB_ORIGINAL_EXPIRY_DATE'} ne $current_expiry_date) {
            General::Logging::log_user_message("The previous expiry date $::JOB_PARAMS{'WCDB_ORIGINAL_EXPIRY_DATE'} is different from the current expiry date $current_expiry_date.\nThis indicates that the $configmap configmap has already been updated and there is no reason to restart the $statefulset statefulset.\n");
            $::JOB_PARAMS{'WCDB_ORIGINAL_EXPIRY_DATE'} = $current_expiry_date;
            return 0;
        } else {
            General::Logging::log_user_message("The previous and current expiry date $current_expiry_date are the same.\nThis indicates that the $configmap configmap has not yet been updated with new expiry date and we need to restart the $statefulset statefulset.\n");
        }

        $command = sprintf '%skubectl -n %s rollout restart statefulset.apps/%s', $debug_command, $sc_namespace, $statefulset;

        General::Logging::log_user_message("Restarting statefulset $statefulset");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to restart statefulset");
            return 1;
        }

        # The following check was changed after discussions with Thomas Wolinski on 2023-01-13
        # because he suspects that we continue too fast with the next task before the stateful
        # set has completely restarted.
        General::Logging::log_user_message("Checking every 10 seconds that all pods in $sc_namespace namespace are up for a maximum of $max_time seconds.");
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
                "pod-include-list"                          => [ "eric-data-wide-column-database-cd-.+", "eric-bsf-wcdb-cd-.+" ],
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
            General::Logging::log_user_error_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n$message");
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
    sub Execute_Certificate_Rotation_Playbooks_P801S08T09 {

        my $command = sprintf '%ssudo ansible-playbook playbooks/rotate-certs.yml -e \'rotate_k8s_pki="yes"\' -e \'rotate_kubelet="yes"\' -i /mnt/config/inventory/ibd_inventory', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Rotating ECCD Certificates.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => $hide_output,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
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
    sub Print_And_Check_Cluster_Certificates_P801S08T10 {

        my $master0_ip = $::JOB_PARAMS{'P801_MASTER_0_IP_ADDRESS'};
        my $command = sprintf "ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@%s 'sudo -n /usr/local/bin/kubeadm certs check-expiration'", $master0_ip;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Printing Cluster Certificate Information");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
        }

        General::Logging::log_user_message(join "\n", @result);

        # TODO: Add checks on the validity time to make sure they are valid long enough

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
    sub Update_Kube_Config_File_P801S08T11 {

        my $command = sprintf '%ssudo ansible -m "fetch src=/home/eccd/.kube/config dest=/home/eccd/.kube/config flat=yes" master-0 -i /mnt/config/inventory/ibd_inventory', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Update kube config file");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => $hide_output,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
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
    sub Check_Cluster_Health_P801S08T12 {

        my $command = 'kubectl get nodes -o wide';
        my $failed_approve_cnt = 0;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $name;
        my @pending_cert_sign_requests;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Checking health on cluster after certificate rotation");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => $hide_output,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
        }

        for (my $i=1;$i<2;$i++) {

            #
            # Repeat 2 times
            #

            General::Logging::log_user_message("Checking Certificate Sign Requests, attempt $i");
            $command = 'kubectl get csr';
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => $hide_output,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

                General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
                return $rc;
            }
            for (@result) {
                if (/^(\S+)\s+\S+\s+\S+\s+\S+\s+Pending\s*$/) {
                    # NAME        AGE     SIGNERNAME                      REQUESTOR                                         CONDITION
                    # csr-zt7xj   5h30m   kubernetes.io/kubelet-serving   system:node:worker-pool1-ia3sn2o1-eccd-eevee-ds   Approved,Issued
                    # csr-228zk   15h     kubernetes.io/kubelet-serving   system:node:worker-pool1-tl89s3qx-eccd-eevee-ds   Pending
                    # csr-25prw   7h52m   kubernetes.io/kubelet-serving   system:node:worker-pool1-77wkigzo-eccd-eevee-ds   Pending
                    #  : etc.
                    push @pending_cert_sign_requests, $1;
                }
            }

            if (scalar @pending_cert_sign_requests == 0) {
                # Exit the 'for' loop
                last;
            } else {
                General::Logging::log_user_message(sprintf "Approving %d pending certificare sign requests", scalar @pending_cert_sign_requests);
                $failed_approve_cnt = 0;
                for $name (@pending_cert_sign_requests) {
                    $command = "${debug_command}kubectl certificate approve $name";
                    $rc = General::OS_Operations::send_command(
                        {
                            "command"       => $command,
                            "hide-output"   => $hide_output,
                            "return-output" => \@result,
                        }
                    );
                    if ($rc != 0) {
                        # Display the result in case of error
                        General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

                        General::Logging::log_user_warning_message("Failed to execute the command:\n$command\n");
                        $failed_approve_cnt++;
                    }
                }
                if ($failed_approve_cnt > 0) {
                    General::Logging::log_user_warning_message("Failed to approve $failed_approve_cnt certificate sign requests, but errors ignored");
                }
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
    sub Rotate_Container_And_Internal_Registry_P801S08T13 {

        my $command = sprintf '%ssudo ansible-playbook playbooks/rotate-certs.yml -e \'rotate_internal_registry="yes"\' -e \'rotate_container_registry="yes"\' -i /mnt/config/inventory/ibd_inventory', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Rotate Container And Internal Registry");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => $hide_output,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
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
sub Perform_Time_Shift_Procedure_CAPO_P801S09 {

    my $day_offset;
    my @day_offsets = calculate_timeshift_offsets_in_days();
    my $length;
    my $message;
    my $rc = 0;

    if (scalar @day_offsets == 0) {
        # Failed to calculate the timeshift offsets
        return General::Playlist_Operations::RC_FALLBACK;
    }

    # In case of a re-run of an interrupted job set ifnformation about which tasks to execute and which what time offset
    $::JOB_PARAMS{'P801_DAY_OFFSET_CNT'}     = exists $::JOB_PARAMS{'P801_DAY_OFFSET_CNT'}     ? $::JOB_PARAMS{'P801_DAY_OFFSET_CNT'}    : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T01'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T01'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T01'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T02'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T02'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T02'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T03'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T03'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T03'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T04'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T04'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T04'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T05'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T05'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T05'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T06'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T06'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T06'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T07'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T07'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T07'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T08'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T08'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T08'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T09'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T09'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T09'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T10'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T10'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T10'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T11'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T11'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T11'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T12'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T12'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T12'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T13'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T13'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T13'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T14'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T14'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T14'} : 1;
    $::JOB_PARAMS{'P801_EXECUTE_P801S09T15'} = exists $::JOB_PARAMS{'P801_EXECUTE_P801S09T15'} ? $::JOB_PARAMS{'P801_EXECUTE_P801S09T15'} : 1;

    if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T01'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Patch_TLS_Connection_Towards_Openstack_P801S09T01 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T01'} = 0;
    }

    if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T02'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_tolerationSeconds_From_0_To_30_P801S09T02 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T02'} = 0;
    }

    if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T03'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Deactivate_NTP_Server_Sync_P801S09T03 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T03'} = 0;
    }

    if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T04'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Apply_Workaround_For_ETCD_Rotation_To_Stop_Active_Master_From_Changing_P801S09T04 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T04'} = 0;
    }

    for (; $::JOB_PARAMS{'P801_DAY_OFFSET_CNT'} <= $#day_offsets;) {
        $day_offset = $day_offsets[$::JOB_PARAMS{'P801_DAY_OFFSET_CNT'}];
        $::JOB_PARAMS{'P801_DAY_OFFSET'} = $day_offset;

        # Print a progress message
        $message = sprintf "# Time Shift %d of %d, Day Offset=+%s days from current time #", $::JOB_PARAMS{'P801_DAY_OFFSET_CNT'}, $#day_offsets, $day_offset;
        $length = length($message);
        General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T05'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_Time_In_All_Nodes_P801S09T05 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S09T05'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T06'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Verify_Time_Change_P801S09T06 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S09T06'} = 0;
        }

        # TODO: Maybe do a health check

        # TODO: Maybe renew session bindings for BSF Diameter traffic

        # TODO: Do we need to wait for pods to come up?

        # TODO: When testing this only one of the two dates in WCDB got changed and the problem had something
        #       to do with the calico and multus daemonsets so I had to restart both and it took several minutes
        #       until the calico daemonset was up 12/12 but when it was up then also the second date had changed.
        if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T07'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Restart_WCDB_Statefulset_P801S09T07 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S09T07'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T08'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Rotate_Kubernetes_Certificates_On_LCM_P801S09T08 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S09T08'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T09'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Rotate_ETCD_Certificates_On_LCM_P801S09T09 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S09T09'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T10'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Rotate_Internal_Registry_Certificates_P801S09T10 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S09T10'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T11'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Rotate_Front_Proxy_CA_Certificates_P801S09T11 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S09T11'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T12'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Restart_calico_and_multus_Daemonset_P801S09T12 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S09T12'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T13'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Pods_To_Come_Up_P801S09T13 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S09T13'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T14'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_And_Check_Cluster_Certificates_P801S09T14 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S09T14'} = 0;
        }

        if ($::JOB_PARAMS{'P801_EXECUTE_P801S09T15'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Cluster_Health_P801S09T15 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P801_EXECUTE_P801S09T15'} = 0;
        }

        if ($::JOB_PARAMS{'COLLECT_LOGS_AFTER_EACH_TIMESHIFT'} eq "yes") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
            return $rc if $rc < 0;
        }

        # Step the counter and reset the tasks to be executed on the second and last time shift
        # since not all steps require to be executed since the certificates are valid for about
        # 10 years.
        $::JOB_PARAMS{'P801_DAY_OFFSET_CNT'}++;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T01'} = 0;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T02'} = 0;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T03'} = 0;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T04'} = 0;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T05'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T06'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T07'} = 0;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T08'} = 0;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T09'} = 0;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T10'} = 0;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T11'} = 0;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T12'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T13'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T14'} = 1;
        $::JOB_PARAMS{'P801_EXECUTE_P801S09T15'} = 1;
    }

    # Print a progress message
    $message = "# Time Shift Procedure Finished #";
    $length = length($message);
    General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

    if ($rc == 0) {
        push @::JOB_STATUS, "(/) Time Shift successful";
    } else {
        push @::JOB_STATUS, "(x) Time Shift failed";
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
    sub Patch_TLS_Connection_Towards_Openstack_P801S09T01 {

        my $command;
        my @commands = (
            "timeshift_TLS_Patch_OsCollect_Config.bash",
            "timeshift_TLS_Patch_Cinder_Secret.bash",
            "timeshift_TLS_Patch_Openstack_Configmap.bash",
            "timeshift_TLS_Restart_Openstack_Pods.bash",
            "timeshift_TLS_Restart_CAPI_Pods.bash",
        );
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;

        for $command (@commands) {
            General::Logging::log_user_message("Executing: $command");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'_PACKAGE_DIR'}/bin/$command",
                    "hide-output"   => $hide_output,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
                return $rc;
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
    sub Change_tolerationSeconds_From_0_To_30_P801S09T02 {

        my @all_pod_names = ADP::Kubernetes_Operations::get_pod_names( { "hide-output" => 1, "namespace" => $::JOB_PARAMS{'SC_NAMESPACE'} } );
        my @changed_pods = ();
        my $command;
        my @failed_pods = ();
        my $failure_cnt = 0;
        my $filename;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        for my $pod_name (@all_pod_names) {
            General::Logging::log_user_message("Processing Pod $pod_name");

            $command = "kubectl get pods -n $sc_namespace $pod_name -o yaml";
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => $hide_output,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0) {
                # Ignore errors on the following pods because they always are respawned
                next if ($pod_name =~ /^(eric-data-search-engine-curator|eric-log-transformer).*/);

                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute the command or wrong data returned:\n$command");

                # Update array for keeping track of failed pods
                push @failed_pods, $pod_name;
                $failure_cnt++;
                next;
            }

            # Check if tolerationSeconds have value 0
            if (grep /tolerationSeconds: 0/, @result) {
                # First create file with original value
                $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/tolerationSeconds_original/$pod_name.yaml";
                $rc = General::File_Operations::write_file(
                    {
                        "filename"      => $filename,
                        "output-ref"    => \@result,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file: $filename");

                    # Update array for keeping track of failed pods
                    push @failed_pods, $pod_name;
                    $failure_cnt++;
                    next;
                }

                # Next we need to change the value from 0 to 30 to avoid issues
                for (@result) {
                    s/tolerationSeconds: 0/tolerationSeconds: 30/g;
                }

                $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/tolerationSeconds_new/$pod_name.yaml";
                $rc = General::File_Operations::write_file(
                    {
                        "filename"      => $filename,
                        "output-ref"    => \@result,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file: $filename");

                    # Update array for keeping track of failed pods
                    push @failed_pods, $pod_name;
                    $failure_cnt++;
                    next;
                }

                # Finally apply the change of tolerationSeconds from 0 to 30
                General::Logging::log_user_message("  Changing tolerationSeconds from 0 to 30");
                $command = "${debug_command}kubectl -n $sc_namespace apply -f $filename";
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => $command,
                        "hide-output"   => $hide_output,
                    }
                );
                if ($rc != 0) {
                    # Ignore errors on the following pods because they always are respawned
                    next if ($pod_name =~ /^(eric-data-search-engine-curator|eric-log-transformer).*/);

                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to execute the command or wrong data returned:\n$command");

                    # Update array for keeping track of failed pods
                    push @failed_pods, $pod_name;
                    $failure_cnt++;
                    next;
                }

                General::Logging::log_user_message("  Checking that tolerationSeconds was changed from 0 to 30");
                $command = "kubectl get pods -n $sc_namespace $pod_name -o yaml";
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => $command,
                        "hide-output"   => $hide_output,
                        "return-output" => \@result,
                    }
                );
                if ($rc != 0) {
                    # Ignore errors on the following pods because they always are respawned
                    next if ($pod_name =~ /^(eric-data-search-engine-curator|eric-log-transformer).*/);

                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to execute the command or wrong data returned:\n$command");

                    # Update array for keeping track of failed pods
                    push @failed_pods, $pod_name;
                    $failure_cnt++;
                    next;
                }

                # Check if tolerationSeconds still have value 0
                if (grep /tolerationSeconds: 0/, @result) {
                    # Update array for keeping track of failed pods
                    push @failed_pods, $pod_name;
                    $failure_cnt++;

                    # Don't report an error if we are debugging the playlist
                    next if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes");

                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("The tolerationSeconds value was not changed from 0 to 30");
                } else {
                    # Update array for keeping track of changed pods
                    push @changed_pods, $pod_name;
                }
            } else {
                General::Logging::log_user_message("  No need to change tolerationSeconds");
            }
        }

        if (@changed_pods) {
            my $message = "Pods where tolerationSeconds was changed from 0 to 30:\n";
            for (@changed_pods) {
                $message .= "  - $_\n";
            }
            General::Logging::log_user_message($message);
        }

        if (@failed_pods) {
            my $message = "Pods where tolerationSeconds failed to be changed from 0 to 30:\n";
            for (@failed_pods) {
                $message .= "  - $_\n";
            }
            General::Logging::log_user_message($message);
        }

        if ($failure_cnt == 0) {
            return 0;
        } else {
            if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
                # We report it as successful since we have not really changed anything
                return 0;
            } else {
                return 1;
            }
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
    sub Deactivate_NTP_Server_Sync_P801S09T03 {

        my $command;
        my @commands = (
            "${debug_command}ansible -a 'sudo timedatectl set-ntp false' all -i $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ansible_inventory.ini",
        );
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;

        for $command (@commands) {
            General::Logging::log_user_message("Executing: $command");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => $hide_output,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
                return $rc;
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
    sub Apply_Workaround_For_ETCD_Rotation_To_Stop_Active_Master_From_Changing_P801S09T04 {

        my $command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my @node_ips = ();
        my $rc = 0;

        General::Logging::log_user_message("Saving a copy of file '/etc/keepalived/keepalived.conf' from current node");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "sudo cp -p /etc/keepalived/keepalived.conf $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/keepalived.conf",
                "hide-output"   => $hide_output,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to copy the file");
            return $rc;
        }

        General::Logging::log_user_message("Finding Node information for control-plane nodes");
        my %node_info = ADP::Kubernetes_Operations::get_node_information(
            {
                "type"          => "master",
                "return-filter" => '^Addresses$',
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get node information");
            return $rc;
        }
        for my $node_name (sort keys %node_info) {
            if (exists $node_info{$node_name}{'Addresses'}) {
                for (@{$node_info{$node_name}{'Addresses'}}) {
                    if (/^\s*InternalIP:\s*(\S+)\s*$/) {
                        push @node_ips, $1;
                        last;   # We only need the first IP-address
                    }
                }
            }
        }

        for my $node_ip (@node_ips) {
            General::Logging::log_user_message("Copy script to node: $node_ip");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}scp -p -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o UserKnownHostsFile=/dev/null -o loglevel=ERROR $::JOB_PARAMS{'_PACKAGE_DIR'}/bin/timeshift_patch_keepalived.bash eccd\@$node_ip:/home/eccd/",
                    "hide-output"   => $hide_output,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy script to node: $node_ip");
                return $rc;
            }

            General::Logging::log_user_message("Patching priority on EXTERNAL_VIP from 50 to 49 on node: $node_ip");
            $rc = General::OS_Operations::send_command_via_ssh(
                {
                    "commands"          => [
                        "${debug_command}/home/eccd/timeshift_patch_keepalived.bash",
                        "${debug_command}rm -f /home/eccd/timeshift_patch_keepalived.bash",
                    ],
                    "hide-output"       => $hide_output,
                    "ip"                => $node_ip,
                    "stop-on-error"     => 1,
                    "use-standard-ssh"  => "yes",
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to patch the priority on EXTERNAL_VIP on node: $node_ip");
                return $rc;
            }
        }

        General::Logging::log_user_message("Restoring original '/etc/keepalived/keepalived.conf' file on current node");
        $rc = General::OS_Operations::send_command(
            {
                "commands"       => [
                    "${debug_command}sudo cp -p $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/keepalived.conf /etc/keepalived/keepalived.conf",
                    "${debug_command}sudo systemctl reload keepalived",
                ],
                "hide-output"   => $hide_output,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to restore the file");
            return $rc;
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
    sub Change_Time_In_All_Nodes_P801S09T05 {
        my $day_offset_value = $::JOB_PARAMS{'P801_DAY_OFFSET'};
        my $command;
        my @commands = (
            "${debug_command}ansible -a 'sudo date -s \"+$day_offset_value days\"' all -i $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ansible_inventory.ini",
        );
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;

        for $command (@commands) {
            General::Logging::log_user_message("Executing: $command");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => $hide_output,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
                return $rc;
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
    sub Verify_Time_Change_P801S09T06 {

        my $command = "timedatectl";
        my $current_time;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;
        my $time_difference;

        General::Logging::log_user_message("Waiting 5 seconds for time change to be applied");
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 0,
                "progress-message"  => 0,
                "seconds"           => 5,
                "use-logging"       => 1,
            }
        );

        $current_time = time();

        General::Logging::log_user_message("Check that the current time was updated");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => $hide_output,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
        }

        General::Logging::log_user_message(join "\n", @result);

        $time_difference = $current_time - $::JOB_PARAMS{'PREVIOUS_EPOCH_TIME'};
        if ($time_difference >= (184 * 24 * 3600)) {
            # It looks like we have have shifted time at least 184 days into the future
            # Save current epoch time for later checks after each time shift
            $::JOB_PARAMS{'PREVIOUS_EPOCH_TIME'} = $current_time;
            General::Logging::log_user_message(sprintf "It looks like the time has been shifted by %d seconds (%s).\n", $time_difference, General::OS_Operations::convert_seconds_to_wdhms($time_difference));

            General::Logging::log_user_message("Printing time in all nodes");
            General::OS_Operations::send_command(
                {
                    "command"       => "ansible -a 'timedatectl' all -i $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ansible_inventory.ini",
                    "hide-output"   => $hide_output,
                }
            );
        } else {
            General::Logging::log_user_error_message(sprintf "It looks like the time has not been shifted with at least 184 days as expected.\nIt has only changed by %d seconds (%s).\n", $time_difference, General::OS_Operations::convert_seconds_to_wdhms($time_difference));
            $rc = 1;
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
    sub Restart_WCDB_Statefulset_P801S09T07 {

        my $command = "";
        my $configmap;
        my $current_expiry_date = "";
        my @failed_pods = ();
        my $max_time = $::JOB_PARAMS{'POD_STATUS_TIMEOUT'};
        my $message = "";
        my $previous_expiry_date = "";
        my $rc = 0;
        my $repeated_check_time = $::JOB_PARAMS{'POD_UP_TIMER'};
        my @result;
        my $statefulset;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        @result = ADP::Kubernetes_Operations::get_resource_names(
            {
                "resource"      => "statefulset",
                "namespace"     => $sc_namespace,
                "include-list"  => [ "^eric-data-wide-column-database-cd", "^eric-bsf-wcdb-cd", "^eric-dsc-wcdb-cd" ],
                "hide-output"   => 1,
            }
        );
        if (scalar @result == 0) {
            General::Logging::log_user_message("No eric-data-wide-column-database-cd or eric-bsf-wcdb-cd statefulset, skip this task");
            return General::Playlist_Operations::RC_TASKOUT;
        } elsif (scalar @result > 1) {
            General::Logging::log_user_error_message("Unexpected number of statefulset returned, only expecting 0 or 1");
            return 1;
        }

        $statefulset = $result[0];

        # We need to wait a while to allow the certificate expiry date in WCDB to
        # renew automatically after the time change.
        my $wcdb_delay_time = 600;
        General::Logging::log_user_message("Waiting $wcdb_delay_time seconds for the WCDB certificate expiry date to automatically be updated");
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "progress-message"  => 1,
                "seconds"           => $wcdb_delay_time,
                "use-logging"       => 1,
            }
        );
        if ($rc == 1) {
            # User pressed CTRL-C to interrupt the wait
            General::Logging::log_user_warning_message("CTRL-C pressed to interrupt the wait for the WCDB certificate expiry date to be updated");
            $rc = 0;
            last;
        }

        # Check the certificate expiry date from the configmap to see if we need to
        # restart the statefulset.

        @result = ADP::Kubernetes_Operations::get_resource_names(
            {
                "resource"      => "configmap",
                "namespace"     => $sc_namespace,
                "include-list"  => [ "^eric-data-wide-column-database-cd-tls-restarter", "^eric-bsf-wcdb-cd-tls-restarter", "^eric-dsc-wcdb-cd-tls-restarter" ],
                "hide-output"   => 1,
            }
        );
        if (scalar @result == 0) {
            General::Logging::log_user_message("No eric-data-wide-column-database-cd-tls-restarter, eric-bsf-wcdb-cd-tls-restarter or eric-dsc-wcdb-cd-tls-restarter configmap, skip this task");
            return General::Playlist_Operations::RC_TASKOUT;
        } elsif (scalar @result > 1) {
            General::Logging::log_user_error_message("Unexpected number of configmap returned, only expecting 0 or 1");
            return 1;
        }

        $configmap = $result[0];

        $command = sprintf 'kubectl -n %s get configmap %s -o yaml', $sc_namespace, $configmap;

        General::Logging::log_user_message("Fetching configmap $configmap expiry information");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get configmap");
            return 1;
        }

        # Returns something like this:
        #apiVersion: v1
        #data:
        #  eric-data-wide-column-database-cd-datacenter1-rack1-0: "2023-06-20"
        #  eric-data-wide-column-database-cd-datacenter1-rack1-1: "2023-06-20"
        #kind: ConfigMap
        #metadata:
        #  annotations:
        #  : etc.

        # Fetch just the "data:" section
        @result = ADP::Kubernetes_Operations::get_top_level_section_information("data",\@result);
        for (@result) {
            if (/^\s+(\S+):\s+"(\d{4}-\d{2}-\d{2})"\s*$/) {
                #  eric-data-wide-column-database-cd-datacenter1-rack1-0: "2023-06-20"
                #    or
                #  eric-bsf-wcdb-cd-datacenter1-rack1-0: "2024-10-23"
                $current_expiry_date = $2;
                if ($previous_expiry_date ne "" && $current_expiry_date ne $previous_expiry_date) {
                    General::Logging::log_user_error_message("The dates are not the same, $previous_expiry_date vs. $current_expiry_date");
                    return 1;
                } else {
                    $previous_expiry_date = $current_expiry_date;
                }
            }
        }

        if ($current_expiry_date eq "") {
            General::Logging::log_user_error_message("No expiry date found in the 'data' section");
            return 1;
        }

        General::Logging::log_user_message("Expiry date: $current_expiry_date");
        if ($::JOB_PARAMS{'WCDB_ORIGINAL_EXPIRY_DATE'} ne $current_expiry_date) {
            General::Logging::log_user_message("The previous expiry date $::JOB_PARAMS{'WCDB_ORIGINAL_EXPIRY_DATE'} is different from the current expiry date $current_expiry_date.\nThis indicates that the $configmap configmap has already been updated and there is no reason to restart the $statefulset statefulset.\n");
            $::JOB_PARAMS{'WCDB_ORIGINAL_EXPIRY_DATE'} = $current_expiry_date;
            return 0;
        } else {
            General::Logging::log_user_message("The previous and current expiry date $current_expiry_date are the same.\nThis indicates that the $configmap configmap has not yet been updated with new expiry date and we need to restart the $statefulset statefulset.\n");
        }

        $command = sprintf '%skubectl -n %s rollout restart statefulset.apps/%s', $debug_command, $sc_namespace, $statefulset;

        General::Logging::log_user_message("Restarting statefulset $statefulset");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to restart statefulset");
            return 1;
        }

        # The following check was changed after discussions with Thomas Wolinski on 2023-01-13
        # because he suspects that we continue too fast with the next task before the stateful
        # set has completely restarted.
        General::Logging::log_user_message("Checking every 10 seconds that all pods in $sc_namespace namespace are up for a maximum of $max_time seconds.");
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
                "pod-include-list"                          => [ "eric-data-wide-column-database-cd-.+", "eric-bsf-wcdb-cd-.+", "eric-dsc-wcdb-cd-.+" ],
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
            General::Logging::log_user_error_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n$message");
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
    sub Rotate_Kubernetes_Certificates_On_LCM_P801S09T08 {

        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;

        General::Logging::log_user_message("Rotating kubernetes certificates on LCM node with IP: $::JOB_PARAMS{'eccd_capo_lcm_host_address'}");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"          => [
                    "${debug_command}ccdadm cluster certs rotate k8s --configdir ~/.ccdadm --debug",
                ],
                "hide-output"       => $hide_output,
                "ip"                => $::JOB_PARAMS{'eccd_capo_lcm_host_address'},
                "user"              => $::JOB_PARAMS{'eccd_capo_lcm_user'},
                "stop-on-error"     => 1,
                "use-standard-ssh"  => "yes",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to rotate kubernetes certificates");
            return $rc;
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
    sub Rotate_ETCD_Certificates_On_LCM_P801S09T09 {

        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;

        General::Logging::log_user_message("Rotating ETCD certificates on LCM node with IP: $::JOB_PARAMS{'eccd_capo_lcm_host_address'}");
        $rc = General::OS_Operations::send_command_via_ssh(
            {
                "commands"          => [
                    "${debug_command}ccdadm cluster certs rotate etcd-ca --configdir ~/.ccdadm",
                ],
                "hide-output"       => $hide_output,
                "ip"                => $::JOB_PARAMS{'eccd_capo_lcm_host_address'},
                "user"              => $::JOB_PARAMS{'eccd_capo_lcm_user'},
                "stop-on-error"     => 1,
                "use-standard-ssh"  => "yes",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to rotate ETCD certificates");
            return $rc;
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
    sub Rotate_Internal_Registry_Certificates_P801S09T10 {
        my $command;
        my @commands = (
            "${debug_command}$::JOB_PARAMS{'_PACKAGE_DIR'}/bin/timeshift_rotate_internal_registry_certificates.bash",
        );
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my @node_ips = ADP::Kubernetes_Operations::get_node_ip( { "type" => "master" } );
        my $rc = 0;

        for my $node_ip (@node_ips) {
            General::Logging::log_user_message("Copy script to node: $node_ip");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}scp -p -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o UserKnownHostsFile=/dev/null -o loglevel=ERROR $::JOB_PARAMS{'_PACKAGE_DIR'}/bin/timeshift_rotate_internal_registry_certificates.bash eccd\@$node_ip:/home/eccd/",
                    "hide-output"   => $hide_output,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy script to node: $node_ip");
                return $rc;
            }

            General::Logging::log_user_message("Rotating Internal Registry Certificates on node: $node_ip");
            $rc = General::OS_Operations::send_command_via_ssh(
                {
                    "commands"          => [
                        "${debug_command}./timeshift_rotate_internal_registry_certificates.bash",
                        "${debug_command}rm -f ./timeshift_rotate_internal_registry_certificates.bash",
                    ],
                    "hide-output"       => $hide_output,
                    "ip"                => $node_ip,
                    "user"              => "eccd",
                    "stop-on-error"     => 1,
                    "use-standard-ssh"  => "yes",
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to rotate internal registry certificates on node: $node_ip");
                return $rc;
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
    sub Rotate_Front_Proxy_CA_Certificates_P801S09T11 {
        my $command;
        my @commands = (
            "${debug_command}ansible -a 'sudo rm /etc/kubernetes/pki/front-proxy-ca.crt' master -i $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ansible_inventory.ini",
            "${debug_command}ansible -a 'sudo rm /etc/kubernetes/pki/front-proxy-ca.key' master -i $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ansible_inventory.ini",
            "${debug_command}sudo kubeadm config print init-defaults > /home/eccd/kubeadmin.config",
            "${debug_command}sudo /usr/local/bin/kubeadm init phase certs front-proxy-ca --config /home/eccd/kubeadmin.config",
            "${debug_command}sudo install -C -m 640 -o eccd -g eccd /etc/kubernetes/pki/front-proxy-ca.crt /home/eccd/front-proxy-ca.crt",
            "${debug_command}sudo install -C -m 640 -o eccd -g eccd /etc/kubernetes/pki/front-proxy-ca.key /home/eccd/front-proxy-ca.key",
            "${debug_command}sudo chown eccd:eccd /home/eccd/front-proxy-ca.crt",
            "${debug_command}sudo chown eccd:eccd /home/eccd/front-proxy-ca.key",
            "${debug_command}$::JOB_PARAMS{'_PACKAGE_DIR'}/bin/timeshift_rsync_master.bash",
            "${debug_command}ansible -a 'sudo cp /home/eccd/front-proxy-ca.crt /etc/kubernetes/pki/' master -i $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ansible_inventory.ini",
            "${debug_command}ansible -a 'sudo cp /home/eccd/front-proxy-ca.key /etc/kubernetes/pki/' master -i $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ansible_inventory.ini",
            "${debug_command}ansible -a 'sudo /usr/local/bin/kubeadm certs renew front-proxy-client --config /home/eccd/kubeadmin.config' master -i $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ansible_inventory.ini",
        );
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;

        for $command (@commands) {
            General::Logging::log_user_message("Executing: $command");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => $hide_output,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
                return $rc;
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
    sub Restart_calico_and_multus_Daemonset_P801S09T12 {

        my $command;
        my @daemonsets = ();
        my $message = "";
        my $rc = 0;
        my @result;

        @result = ADP::Kubernetes_Operations::get_resource_names(
            {
                "resource"      => "daemonset",
                "namespace"     => "kube-system",
                "include-list"  => [ '^calico-node$', '^kube-multus-ds-amd64$' ],
                "hide-output"   => 1,
            }
        );
        if (scalar @result == 0) {
            General::Logging::log_user_message("No calico-node or kube-multus-ds-amd64 daemonset, skip this task");
            return General::Playlist_Operations::RC_TASKOUT;
        }
        for (@result) {
            if (/^(\S+)$/) {
                #NAME                                             DESIRED   CURRENT   READY   UP-TO-DATE   AVAILABLE   NODE SELECTOR                            AGE
                #calico-node                                      7         7         6       7            6           kubernetes.io/os=linux                   184d
                #kube-multus-ds-amd64                             7         7         7       7            7           kubernetes.io/arch=amd64                 183d
                push @daemonsets, $1;
            }
        }

        for my $daemonset (@daemonsets) {
            General::Logging::log_user_message("Restarting daemonset $daemonset");
            $command = sprintf '%s kubectl -n %s rollout restart daemonset %s', $debug_command, "kube-system", $daemonset;
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to restart daemonset");
                return 1;
            }

            $message = "";
            $rc = ADP::Kubernetes_Operations::check_daemonset_status(
                {
                    "namespace"         => "kube-system",
                    "check-name"        => $daemonset,
                    "debug-messages"    => 1,
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
    sub Wait_For_Pods_To_Come_Up_P801S09T13 {

        my @failed_pods = ();
        my $max_time = $::JOB_PARAMS{'POD_STATUS_TIMEOUT'};
        my $rc = 0;
        my $repeated_check_time = $::JOB_PARAMS{'POD_UP_TIMER'};
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        # The following check was implemented after discussions with Thomas Wolinski on 2023-01-13
        # because he suspects that we continue too fast with the next task to restart the WCDB
        # stateful set and this might result in that only one of the two date entries in the
        # config map is updated, causing an error because the two dates are different.
        General::Logging::log_user_message("Checking every 10 seconds that all pods in $sc_namespace namespace are up and are stable for $repeated_check_time seconds, for a maximum of $max_time seconds.");
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
                "pod-exclude-ready-list"                    => [ "eric-data-search-engine-curator.*", "eric-log-transformer.*" ],
                "pod-exclude-status-list"                   => [ "eric-data-search-engine-curator.*", "eric-log-transformer.*" ],
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
            General::Logging::log_user_error_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n$message");
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
    sub Print_And_Check_Cluster_Certificates_P801S09T14 {

        my $master0_ip = $::JOB_PARAMS{'P801_MASTER_0_IP_ADDRESS'};
        my $command = sprintf "ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@%s 'sudo -n /usr/local/bin/kubeadm certs check-expiration'", $master0_ip;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Printing Cluster Certificate Information");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
        }

        General::Logging::log_user_message(join "\n", @result);

        # TODO: Add checks on the validity time to make sure they are valid long enough

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
    sub Check_Cluster_Health_P801S09T15 {

        my $command = 'kubectl get nodes -o wide';
        my $failed_approve_cnt = 0;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $name;
        my @pending_cert_sign_requests;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Checking health on cluster after certificate rotation");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => $hide_output,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

            General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
            return $rc;
        }

        for (my $i=1;$i<2;$i++) {

            #
            # Repeat 2 times
            #

            General::Logging::log_user_message("Checking Certificate Sign Requests, attempt $i");
            $command = 'kubectl get csr';
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => $hide_output,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

                General::Logging::log_user_error_message("Failed to execute the command:\n$command\n");
                return $rc;
            }
            for (@result) {
                if (/^(\S+)\s+\S+\s+\S+\s+\S+\s+Pending\s*$/) {
                    # NAME        AGE     SIGNERNAME                      REQUESTOR                                         CONDITION
                    # csr-zt7xj   5h30m   kubernetes.io/kubelet-serving   system:node:worker-pool1-ia3sn2o1-eccd-eevee-ds   Approved,Issued
                    # csr-228zk   15h     kubernetes.io/kubelet-serving   system:node:worker-pool1-tl89s3qx-eccd-eevee-ds   Pending
                    # csr-25prw   7h52m   kubernetes.io/kubelet-serving   system:node:worker-pool1-77wkigzo-eccd-eevee-ds   Pending
                    #  : etc.
                    push @pending_cert_sign_requests, $1;
                }
            }

            if (scalar @pending_cert_sign_requests == 0) {
                # Exit the 'for' loop
                last;
            } else {
                General::Logging::log_user_message(sprintf "Approving %d pending certificare sign requests", scalar @pending_cert_sign_requests);
                $failed_approve_cnt = 0;
                for $name (@pending_cert_sign_requests) {
                    $command = "${debug_command}kubectl certificate approve $name";
                    $rc = General::OS_Operations::send_command(
                        {
                            "command"       => $command,
                            "hide-output"   => $hide_output,
                            "return-output" => \@result,
                        }
                    );
                    if ($rc != 0) {
                        # Display the result in case of error
                        General::OS_Operations::write_last_temporary_output_to_progress() if $hide_output;

                        General::Logging::log_user_warning_message("Failed to execute the command:\n$command\n");
                        $failed_approve_cnt++;
                    }
                }
                if ($failed_approve_cnt > 0) {
                    General::Logging::log_user_warning_message("Failed to approve $failed_approve_cnt certificate sign requests, but errors ignored");
                }
                $rc = 0;
            }
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
sub Perform_Post_Time_Shift_Check_P801S10 {

    my $rc = 0;

    # Need to keep this playlist running until
    #   1. The SIP-TLS certificates has renewed
    #   2. Until the traffic test is done
    #   3. Collected KPI data
    #   4. Until the user says it's ready to collect the final logs.

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_CMYP_Password_If_Needed_P801S10T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_SIP_TLS_Certificates_P801S10T02 } );
    return $rc if $rc < 0;

    #$rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_User_Confirmation_To_Continue_P801S10T03 } );
    #return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Pod_Status_P801S10T04 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_KPI_COLLECTION'} eq "no" && $::JOB_PARAMS{'APPLICATION_TYPE'} eq "sc") {
        $::JOB_PARAMS{'P933_TASK'} = "STOP_KPI_COLLECTION";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::933_Handle_KPI_Statistics::main } );
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
    sub Change_CMYP_Password_If_Needed_P801S10T01 {

        my $cmyp_new_password = exists $::JOB_PARAMS{'CMYP_NEW_PASSWORD'} ? $::JOB_PARAMS{'CMYP_NEW_PASSWORD'} : "";
        my $cmyp_password = exists $::JOB_PARAMS{'CMYP_PASSWORD'} ? $::JOB_PARAMS{'CMYP_PASSWORD'} : "";
        my $cmyp_user = exists $::JOB_PARAMS{'CMYP_USER'} ? $::JOB_PARAMS{'CMYP_USER'} : "";
        my $cmyp_ip = ADP::Kubernetes_Operations::get_cmyp_ip($::JOB_PARAMS{'SC_NAMESPACE'});
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $rc = 0;
        my @result;
        my $sec_admin_new_password = exists $::JOB_PARAMS{'SEC_ADMIN_NEW_PASSWORD'} ? $::JOB_PARAMS{'SEC_ADMIN_NEW_PASSWORD'} : "";
        my $sec_admin_password = exists $::JOB_PARAMS{'SEC_ADMIN_PASSWORD'} ? $::JOB_PARAMS{'SEC_ADMIN_PASSWORD'} : "";
        my $sec_admin_user = exists $::JOB_PARAMS{'SEC_ADMIN_USER'} ? $::JOB_PARAMS{'SEC_ADMIN_USER'} : "";

        #############################
        # First check "expert" user #
        #############################
        if ($cmyp_user eq "") {
            $cmyp_user = General::OS_Operations::get_user_input("Provide user name of the user that have 'expert' type rights in CMYP CLI: ", 0);
        }
        if ($cmyp_password eq "") {
            $cmyp_password = General::OS_Operations::get_user_input("Provide user password of the user that have 'expert' type rights in CMYP CLI (what you type is hidden): ", 1);
        }
        if ($cmyp_new_password eq "") {
            $cmyp_new_password = General::OS_Operations::get_user_input("Provide a NEW user password of the user that have 'expert' type rights in CMYP CLI (what you type is hidden): ", 1);
        }

        General::Logging::log_user_message("Checking if user '$cmyp_user' can login to CMYP CLI, using the new password");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command --user=$cmyp_user --password='$cmyp_new_password' --ip=$cmyp_ip --command='exit'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            # It seems like the kpi_statistics.pl script was able to change the password, or the user did it manually.
            General::Logging::log_user_message("Login with user '$cmyp_user' using new password was successful");
            $::JOB_PARAMS{'CMYP_USER'} = $cmyp_user;
            $::JOB_PARAMS{'CMYP_PASSWORD'} = $cmyp_new_password;
            $::JOB_PARAMS{'CMYP_NEW_PASSWORD'} = "";
        } elsif ($rc == 2) {
            General::Logging::log_user_message("Login with user '$cmyp_user' failed, Checking if user '$cmyp_user' can login to CMYP CLI, using the old password");

            my $password_change_needed = 0;
            for (@result) {
                if (/Password expired\. Change your password now\./) {
                    $password_change_needed = 1;
                    last;
                }
            }
            if ($password_change_needed == 1) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Login failed because the new password needs to be changed, this cannot currently be handled by this playlist");
                return $rc;
            }

            # It seems like the kpi_statistics.pl script was not able to change the password.
            # Now try with the old password.
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$command --user=$cmyp_user --password='$cmyp_password' --ip=$cmyp_ip --command='exit'",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0) {
                General::Logging::log_user_message("Login with user '$cmyp_user' using old password was successful");
                $::JOB_PARAMS{'CMYP_USER'} = $cmyp_user;
                $::JOB_PARAMS{'CMYP_PASSWORD'} = $cmyp_password;
            } elsif ($rc == 2) {
                General::Logging::log_user_warning_message("Login with user '$cmyp_user' using the old password also failed");

                # Do not change the password if we are just debugging the playlist
                return 0 if ($debug_command ne "");

                my $password_change_needed = 0;
                for (@result) {
                    if (/Password expired\. Change your password now\./) {
                        $password_change_needed = 1;
                        last;
                    }
                }
                if ($password_change_needed == 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Login failed for some unknown reason, see details above");
                    return $rc;
                }

                # We now need to change the password
                General::Logging::log_user_message("Changing password for user '$cmyp_user'");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "$command --user=$cmyp_user --password='$cmyp_password' --new-password='$cmyp_new_password' --ip=$cmyp_ip --command='exit'",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc == 0) {
                    General::Logging::log_user_message("Password change for user '$cmyp_user' successful");
                    $::JOB_PARAMS{'CMYP_USER'} = $cmyp_user;
                    $::JOB_PARAMS{'CMYP_PASSWORD'} = $cmyp_new_password;
                    $::JOB_PARAMS{'CMYP_NEW_PASSWORD'} = "";
                } else {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to change the password, see details above");
                    return $rc;
                }
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute the command:\n$command --user=$cmyp_user ...\n");
                return $rc;
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute the command:\n$command --user=$cmyp_user ...\n");
            return $rc;
        }

        ##########################################
        # Next we need to check "sec-admin" user #
        ##########################################
        if ($sec_admin_user eq "") {
            $sec_admin_user = General::OS_Operations::get_user_input("Provide user name of the user that have 'sec-admin' type rights in CMYP CLI: ", 0);
        }
        if ($sec_admin_password eq "") {
            $sec_admin_password = General::OS_Operations::get_user_input("Provide user password of the user that have 'sec-admin' type rights in CMYP CLI (what you type is hidden): ", 1);
        }
        if ($sec_admin_new_password eq "") {
            $sec_admin_new_password = General::OS_Operations::get_user_input("Provide a NEW user password of the user that have 'sec-admin' type rights in CMYP CLI (what you type is hidden): ", 1);
        }

        General::Logging::log_user_message("Checking if user '$sec_admin_user' can login to CMYP CLI, using the new password");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command --user=$sec_admin_user --password='$sec_admin_new_password' --ip=$cmyp_ip --command='exit'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            # It seems like somebody manually changed the password
            General::Logging::log_user_message("Login with user '$sec_admin_user' using new password was successful");
            $::JOB_PARAMS{'SEC_ADMIN_USER'} = $sec_admin_user;
            $::JOB_PARAMS{'SEC_ADMIN_PASSWORD'} = $sec_admin_new_password;
            $::JOB_PARAMS{'SEC_ADMIN_NEW_PASSWORD'} = "";
        } elsif ($rc == 2) {
            General::Logging::log_user_message("Login with user '$sec_admin_user' failed, Checking if user '$sec_admin_user' can login to CMYP CLI, using the old password");

            my $password_change_needed = 0;
            for (@result) {
                if (/Password expired\. Change your password now\./) {
                    $password_change_needed = 1;
                    last;
                }
            }
            if ($password_change_needed == 1) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Login failed because the new password needs to be changed, this cannot currently be handled by this playlist");
                return $rc;
            }

            # Now try with the old password.
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$command --user=$sec_admin_user --password='$sec_admin_password' --ip=$cmyp_ip --command='exit'",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0) {
                General::Logging::log_user_message("Login with user '$sec_admin_user' using old password was successful");
                $::JOB_PARAMS{'SEC_ADMIN_USER'} = $sec_admin_user;
                $::JOB_PARAMS{'SEC_ADMIN_PASSWORD'} = $sec_admin_password;
            } elsif ($rc == 2) {
                General::Logging::log_user_warning_message("Login with user '$sec_admin_user' using the old password also failed");

                # Do not change the password if we are just debugging the playlist
                return 0 if ($debug_command ne "");

                my $password_change_needed = 0;
                for (@result) {
                    if (/Password expired\. Change your password now\./) {
                        $password_change_needed = 1;
                        last;
                    }
                }
                if ($password_change_needed == 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Login failed for some unknown reason, see details above");
                    return $rc;
                }

                # We now need to change the password
                General::Logging::log_user_message("Changing password for user '$sec_admin_user'");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "$command --user=$sec_admin_user --password='$sec_admin_password' --new-password='$sec_admin_new_password' --ip=$cmyp_ip --command='exit'",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc == 0) {
                    General::Logging::log_user_message("Password change for user '$sec_admin_user' successful");
                    $::JOB_PARAMS{'SEC_ADMIN_USER'} = $sec_admin_user;
                    $::JOB_PARAMS{'SEC_ADMIN_PASSWORD'} = $sec_admin_new_password;
                    $::JOB_PARAMS{'SEC_ADMIN_NEW_PASSWORD'} = "";
                } else {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to change the password, see details above");
                    return $rc;
                }
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute the command:\n$command --user=$sec_admin_user ...\n");
                return $rc;
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute the command:\n$command --user=$sec_admin_user ...\n");
            return $rc;
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
    sub Check_SIP_TLS_Certificates_P801S10T02 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/parse_certificate_validity_info.pl";
        my $expiry_timestamp;
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $stop_time = time() + 3600;
        my $sip_tls_secret_name = $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_SECRET'};

        General::Logging::log_user_message("Initial expiry date for secret '$sip_tls_secret_name' was $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY'}.\nFetching the current certificate expiry date and time.");

        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
            General::Logging::log_user_message("Since we are running with DEBUG_PLAYLIST=yes we exit out.");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        while (1) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$command --namespace=$sc_namespace --print-simple-table --secret-name=$sip_tls_secret_name | grep '$sip_tls_secret_name' | grep -v ' Not ' | awk '{ print \$4,\$5,\$6 }' | head -1",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0 && scalar @result == 1 && $result[0] =~ /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}.*)/) {
                # E.g. 2021-11-19 14:08:44 GMT
                $expiry_timestamp = $1;
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to execute the command or wrong data returned:\n$command");
                $rc = 1;
                last;
            }

            if ($expiry_timestamp ne $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY'}) {
                # The SIP-TLS certificates have changed, so we can exit this loop
                General::Logging::log_user_message("Expiry timestamp for secret '$sip_tls_secret_name' has changed from $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY'} to $expiry_timestamp");
                $rc = 0;
                last;
            }

            if (time() >= $stop_time) {
                General::Logging::log_user_error_message("It has taken more than 1 hour and the expiry timestamp for secret '$sip_tls_secret_name' has not yet changed");
                $rc = 1;
                last;
            }

            # The SIP-TLS certificate is still the same as the beginning, wait a bit longer for it to be renewed
            General::Logging::log_user_message("Expiry timestamp for secret '$sip_tls_secret_name' has not yet changed, waiting a minute before next check");
            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "progress-message"  => 0,
                    "seconds"           => 60,
                    "use-logging"       => 1,
                }
            );
            if ($rc == 1) {
                # User pressed CTRL-C to interrupt the wait
                General::Logging::log_user_warning_message("CTRL-C pressed to interrupt the wait before the timestamp for secret '$sip_tls_secret_name' has changed");
                $rc = 0;
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
    sub Wait_For_User_Confirmation_To_Continue_P801S10T03 {

        return General::Playlist_Operations::show_checkpoint_message("When you are sure that the time shift test has been successfully done and the SIP-TLS certificate has renewed.\n\nThen stop the traffic and press press C to continue the playlist execution.\n");
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
    sub Check_Pod_Status_P801S10T04 {
        my @failed_pods = ();
        my $max_time = 600;
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc;

        General::Logging::log_user_message("Checking every 10 seconds that all pods in $namespace namespace are up for a maximum of $max_time seconds.");
        $rc = ADP::Kubernetes_Operations::check_pod_status(
            {
                "all-namespaces"                            => 0,
                "debug-messages"                            => 0,
                "delay-time"                                => 10,
                "ignore-ready-check-for-completed-status"   => 1,
                "hide-output"                               => 1,
                "max-attempts"                              => 0,
                "max-time"                                  => $max_time,
                "namespace"                                 => $namespace,
                "pod-exclude-ready-list"                    => [ "eric-data-search-engine-curator.*", "eric-log-transformer" ],
                "pod-exclude-status-list"                   => [ "eric-data-search-engine-curator.*", "eric-log-transformer" ],
                "repeated-checks"                           => 0,
                "return-failed-pods"                        => \@failed_pods,
                "wanted-ready"                              => "same",
                "wanted-status"                             => "up",
            }
        );
        if ($rc == 0) {
            General::Logging::log_user_message("POD status check was successful");
        } else {
            General::Logging::log_user_error_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n" . join("\n",@failed_pods));
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
sub Perform_Post_Test_Case_Health_Check_P801S11 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::918_Post_Healthcheck::main } );
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
sub Collect_Post_Test_Case_Logfiles_P801S12 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
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
sub Cleanup_Job_Environment_P801S13 {

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
sub Do_Fallback_P801S98 {

    return General::Playlist_Operations::RC_FALLBACK;
}

# -----------------------------------------------------------------------------
sub Fallback001_P801S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    if (exists $::JOB_PARAMS{'KPI_PID'} && $::JOB_PARAMS{'KPI_PID'} != 0 && $::JOB_PARAMS{'SKIP_KPI_COLLECTION'} eq "no" && $::JOB_PARAMS{'APPLICATION_TYPE'} eq "sc") {
        $::JOB_PARAMS{'P933_TASK'} = "STOP_KPI_COLLECTION";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::933_Handle_KPI_Statistics::main } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'LOG_COLLECT_PID'} && $::JOB_PARAMS{'LOG_COLLECT_PID'} != 0) {
        # TODO: Add logic to stop the background process
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
# This subroutine calculates the time shift offset values in days depending
# on the expiry time of the ETCD, ECCD and SIP TLS certificates.
#
sub calculate_timeshift_offsets_in_days {
    my $current_epoch = time();
    my @offsets = ( 0 );    # First index is skipped
    my $expire_epoch;
    my $message;
    my @result;
    my $timeshift_epoch;
    my @timeshift_epochs = ();

    # Find the certificate that expire first
    $expire_epoch = (sort ($::JOB_PARAMS{'P801_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH'}, $::JOB_PARAMS{'P801_ORIGINAL_ETCD_CERTIFICATE_EXPIRY_EPOCH'}, $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY_EPOCH'}))[0];
    General::Logging::log_user_message(sprintf "The certificate that expire first will be on date %s", General::OS_Operations::format_time_iso($expire_epoch));

    # Calculate the timeshift offset by deducting 48 hours from the certificate expiry date
    $timeshift_epoch = $expire_epoch - (3600 * 48);

    # Check to make sure we don't try to timeshift back in time
    if ($timeshift_epoch <= time()) {
        General::Logging::log_user_error_message(sprintf "You cannot do a timeshift back in time, because 48 hours before the first certificate expiry will be on date %s", General::OS_Operations::format_time_iso($timeshift_epoch));
        return ();
    }

    # Store the first time shift epoch value
    push @timeshift_epochs, $timeshift_epoch;
    # Calclulate the timeshift offset value as days from current day
    push @offsets, sprintf "%i", (($timeshift_epoch - $current_epoch) / (24 * 3600));

    # TODO: Maybe a special extra time shift is needed in the case the SIP TLS certificate
    #       will not automatically renew after the first time shift because it's too far
    #       away from the setting when it should auto-renew.

    # Estimate the final time shift epoch which brings us 185 days further into the future from
    # the first time shift date.
    $timeshift_epoch = $timeshift_epoch + (185 * 24 * 3600);

    # Store the final time shift epoch value
    push @timeshift_epochs, $timeshift_epoch;
    # Store the offset value that is 185 days further into the future from that day
    push @offsets, 185;

    # Print the time shift and offset values to be used
    $message = "Timeshift offset values that will be used (in days):\n\n";
    $message .= sprintf "%12s  %12s  %13s\n", "Timeshift No", "Offset Value", "Date and Time";
    $message .= sprintf "%12s  %12s  %13s\n", "-"x12, "-"x12, "-"x13;
    for (my $i = 1; $i <= $#offsets; $i++) {
        $message .= sprintf "%12i  %12i  %13s\n", $i, $offsets[$i], General::OS_Operations::format_time_iso($timeshift_epochs[$i-1]);
    }
    General::Logging::log_user_message($message);

    return @offsets;
}

# -----------------------------------------------------------------------------
# This subroutine calculates the time shift offset values in seconds depending
# on the expiry time of the ETCD, ECCD and SIP TLS certificates.
#
sub calculate_timeshift_offsets_in_seconds {
    my $current_epoch;
    my @offsets = ( 0 );    # First index is skipped
    my $expire_epoch;
    my $message;
    my @result;
    my $timeshift_epoch;
    my @timeshift_epochs = ();
    my $wcdb_used = 0;

    # Find the certificate that expire first
    $expire_epoch = (sort ($::JOB_PARAMS{'P801_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH'}, $::JOB_PARAMS{'P801_ORIGINAL_ETCD_CERTIFICATE_EXPIRY_EPOCH'}, $::JOB_PARAMS{'P801_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY_EPOCH'}))[0];
    General::Logging::log_user_message(sprintf "The certificate that expire first will be on date %s", General::OS_Operations::format_time_iso($expire_epoch));

    # Calculate the timeshift offset by deducting 24 hours from the certificate expiry date
    $timeshift_epoch = $expire_epoch - (3600 * 24);

    # Check if we have wide column database deployed which require special handling to make sure that
    # the eric-data-wide-column-database-cd-tls-restarter or eric-bsf-wcdb-cd-tls-restarter job has
    # time to run which normally happens at 03:01 in the night where it checks if the certificates
    # needs to be updated and the stateful set should be restarted to apply the new certificates.
    @result = ADP::Kubernetes_Operations::get_resource_names(
        {
            "resource"      => "statefulset",
            "namespace"     => $::JOB_PARAMS{'SC_NAMESPACE'},
            "include-list"  => [ "^eric-data-wide-column-database-cd", "^eric-bsf-wcdb-cd" ],
            "hide-output"   => 1,
        }
    );
    if (scalar @result == 1) {
        $wcdb_used = 1;
    }
    if ($wcdb_used == 1) {
        # The wide column database is deployed, next check what the expiry hour is.
        my $hour = (localtime($timeshift_epoch))[2];
        if ($hour >= 23 || $hour <= 2) {
            # If the expiry date is between 23:00 and 02:59 we need to change
            # the expiry date so it's at 22:xx on the day before.
            if ($hour == 23) {
                # Subtract 1 hour
                $timeshift_epoch = $timeshift_epoch - (1 * 3600);
            } elsif ($hour == 0) {
                # Subtract 2 hours
                $timeshift_epoch = $timeshift_epoch - (2 * 3600);
            } elsif ($hour == 1) {
                # Subtract 3 hours
                $timeshift_epoch = $timeshift_epoch - (3 * 3600);
            } elsif ($hour == 2) {
                # Subtract 4 hours
                $timeshift_epoch = $timeshift_epoch - (4 * 3600);
            }
        }
    }

    # Check to make sure we don't try to timeshift back in time
    if ($timeshift_epoch <= time()) {
        General::Logging::log_user_error_message(sprintf "You cannot do a timeshift back in time, because 24 hours before the first certificate expiry will be on date %s", General::OS_Operations::format_time_iso($timeshift_epoch));
        return ();
    }

    # Store the first time shift epoch value
    push @timeshift_epochs, $timeshift_epoch;

    # TODO: Maybe a special extra time shift is needed in the case the SIP TLS certificate
    #       will not automatically renew after the first time shift because it's too far
    #       away from the setting when it should auto-renew.

    # Estimate the final time shift epoch which brings us 11 years into the future from
    # the job start date.
    $timeshift_epoch = $::JOB_PARAMS{'_JOB_STARTTIME'} + 347155200; # 347155200 = (365 * 11 + 3) * 24 * 3600 = (365 days/year * 11 years + 3 leap days over 11 years) * 24 hours per day * 3600 seconds per hour
    if ($wcdb_used == 1) {
        # The wide column database is deployed, next check what the expiry hour is.
        my $hour = (localtime($timeshift_epoch))[2];
        if ($hour >= 23 || $hour <= 2) {
            # If the expiry date is between 23:00 and 02:59 we need to change
            # the expiry date so it's at 22:xx on the day before.
            if ($hour == 23) {
                # Subtract 1 hour
                $timeshift_epoch = $timeshift_epoch - (1 * 3600);
            } elsif ($hour == 0) {
                # Subtract 2 hours
                $timeshift_epoch = $timeshift_epoch - (2 * 3600);
            } elsif ($hour == 1) {
                # Subtract 3 hours
                $timeshift_epoch = $timeshift_epoch - (3 * 3600);
            } elsif ($hour == 2) {
                # Subtract 4 hours
                $timeshift_epoch = $timeshift_epoch - (4 * 3600);
            }
        }
    }

    # Store the final time shift epoch value
    push @timeshift_epochs, $timeshift_epoch;

    # Calclulate the timeshift offset values
    $current_epoch = time();
    for $timeshift_epoch (@timeshift_epochs) {
        push @offsets, $timeshift_epoch - $current_epoch;
    }

    # Print the time shift and offset values to be used
    $message = "Timeshift offset values that will be used:\n\n";
    $message .= sprintf "%12s  %12s  %13s\n", "Timeshift No", "Offset Value", "Date and Time";
    $message .= sprintf "%12s  %12s  %13s\n", "-"x12, "-"x12, "-"x13;
    for (my $i = 1; $i <= $#offsets; $i++) {
        $message .= sprintf "%12i  %12i  %13s\n", $i, $offsets[$i], General::OS_Operations::format_time_iso($timeshift_epochs[$i-1]);
    }
    General::Logging::log_user_message($message);

    return @offsets;
}

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
        'BYPASS_CONFIRMATION' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to bypass the confirmation messages printed by the
playlist which would normally stop the playlist until the user confirms that
execution should continue.
If value is "no" which is also the default then the playlist will always stop
and wait for user confirmation.
If value is "yes" then no confirmation message will be shown and playlist
execution continues without user confirmation.
EOF
            'validity_mask' => '(no|yes)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CMYP_NEW_PASSWORD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control which new password is used for e.g. fetching
KPI data from the CMYP CLI when the old password has expired due to the time shift.

If this job variable is not set then value is fetched from the 'expert' user in
the network config file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CMYP_PASSWORD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control which password is used for e.g. fetching
KPI data from the CMYP CLI.

If this job variable is not set then value is fetched from the 'expert' user in
the network config file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CMYP_USER' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control which user is used for e.g. fetching
KPI data from the CMYP CLI.

If this job variable is not set then value is fetched from the 'expert' user in
the network config file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

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
        'COLLECT_LOGS_AFTER_EACH_TIMESHIFT' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control if log files should be collected after
each time shift rotation.
This might be useful to enable if certain logs are rotated out and deleted after
a certain time period.

If the value is "no" which is also the default value then no extra log collection
is done after each time shift, instead logs are only collected before and after
completed time shift playlist execution.

If the value is "yes" then log collection is done after each performed time shift
operation which might mean in worst case log files are collected maximum 13 times
or for ECCD 2.18 or newer 4 times.

NOTE: Each log collection takes time and use up 10-20 MB of disk space.
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
Upgrade will take place.
Any command that will just change files or data on the job workspace directory
or any command that just reads data from the cluster will still be executed.
This parameter can be useful to check if the overall logic is working without
doing any changes to the running system.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DISABLE_ACCOUNT_DORMANT_TIMER' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control if the dormant account timer policy should
be disabled.
If the dormant timer is disabled then the accounts will no longer be marked
dormant by the time shift which is usually the case, thus requiring the account
to be locked after the time has been shifted over the "dormant-timer" defined
in CMYP.

If the value is "no" then normal behavior is done and if the parameter
"system authentication default-account-policy dormant-timer" has a non-zero value
then the accounts will expire after so many days.

If the value is "yes" which is also the default value then the dormant timer is
disabled by setting the following value:

  system authentication default-account-policy dormant-timer 0

Meaning that the account will never set to dormant.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DISABLE_PASSWORD_MAX_AGE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control if the max age for the password policy
should be disabled.
If the max age is disabled then the passwords for the users are not expired
by the time shift which is usually the case, thus requiring the passwords to
be changed after the time has been shifted over the "max-age" defined in CMYP.

If the value is "no" then normal behavior is done and if the parameter
"system authentication default-password-policy max-age" has a non-zero value
then the passwords will expire after so many days.

If the value is "yes" which is also the default value then the max age is
disabled by setting the following value:

  system authentication default-password-policy max-age 0

Meaning that the password will never expire.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'HELM_TIMEOUT' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "1500",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the timeout value to use when executing the helm command, if not
specified then the default value will be used.

The time value which must be an integer value is given as the number of seconds
to wait for the helm command to finish.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'HELM_VERSION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The version of 'helm' command to use if you want to override the parameter from
the network configuration file called 'helm_version'.
If you don't specify this parameter then it will take the value from the network
configuration file.
Valid values are 2 (for helm 2) or 3 (for helm 3).
EOF
            'validity_mask' => '(2|3)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'HIDE_COMMAND_OUTPUT' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control if the playlist execution should show
as progress messages all the commands being executed.

If the value is "yes" which is also the default value then normal playlist
execution will be done and no command output it shown to the user which produces
a clean job execution.

If the value is "no" then playlist execution will be altered so that command
output is shown to the user as the commands are being executed, this might be
useful while testing and debugging the playlist.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'IGNORE_ALARMS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Option to ignore alarms generated by alarm_check.pl script.
Valid values are yes or no (default is no).
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'IGNORE_FAILED_HEALTH_CHECK' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should ignore the result of the health checks and
continue regardless if it fails.

Select "no" which is also the default value if not specified, to have the
Playlist stop if the health checks detects an error.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'PASSWORD_EXPIRE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies if the password for e.g. the 'expert' and 'sec-admin'
user accounts expire as a result of the timeshift procedure which is usually
the case and thus needs to be changed after the time shift.
See also the CMYP_NEW_PASSWORD, CMYP_PASSWORD, CMYP_USER, SEC_ADMIN_NEW_PASSWORD,
SEC_ADMIN_PASSWORD and SEC_ADMIN_USER job variables.

This parameter determines which of the user definitions in the network config
file will be used for finding the passwords to use, i.e. where the field
'password_expire' match the PASSWORD_EXPIRE parameter.

The initial password used before the time shift is taken from the user
'initial_password' field and the new password is taken from the 'password'
field.

If this value is 'yes' which is also the default then the 'initial_password'
and 'password' fields should normally have different values in the network
config file.

If this value is 'no' which should normally only be used when debugging this
playlist with the DEBUG_PLAYLIST=yes job variable, then the 'initial_password'
and 'password' fields are normally the same and no password change should
work.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'POD_STATUS_TIMEOUT' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "600",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the timeout value to use when checking that all PODs are up and running
after performing the test case, if not specified then the default value will be
used.

The time value which must be an integer value is given as the number of seconds
to wait for the pods to have expected status.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'POD_UP_TIMER' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "60",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify how long time in seconds the POD's must stay up when checking that the
node is stable before continuing with the playlist execution.

If the value is >0 when checking for stable node the POD's must come up and stay
up for this many seconds before the node is considered as stable.

If the value is 0 then as soon as all pods come up the node is considered as
stable and playlist execution will continue.
EOF
            'validity_mask' => '\d+',
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
        'SC_RELEASE_NAME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The name to use for the SC release name if you want to override the parameter
from the network configuration file called 'sc_release_name'.
If you don't specify this parameter then it will take the value from the network
configuration file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SEC_ADMIN_NEW_PASSWORD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control which new password is used for e.g. login
to then CNOM GUI when the old password has expired due to the time shift.

If this job variable is not set then value is fetched from the 'sec-admin' user
in the network config file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SEC_ADMIN_PASSWORD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control which password is used for e.g. login to
the CNOM GUI.

If this job variable is not set then value is fetched from the 'sec-admin' user
in the network config file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SEC_ADMIN_USER' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control which user is used for e.g. login to the
CNOM GUI.

If this job variable is not set then value is fetched from the 'sec-admin' user
in the network config file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_COLLECT_LOGS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should skip collecting logs e.g. ADP logs before
and after the test case which can speed up the test by a number of minutes at
the expense of not having any logs in case something goes wrong.
At a fallback e.g. due to a failure these logs will anyway be collected.

Select "no" which is also the default value if not specified, to have the
Playlist always collect logs before and after the test.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_KPI_COLLECTION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Option to skip collection of KPI when executing the playlist.
Valid values are yes or no (default is no).
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_PRE_HEALTHCHECK' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the health check before starting the test case should be skipped.

Select "no" which is also the default value if not specified, to have the
health check performed before starting the test case.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_POST_HEALTHCHECK' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the health check after completed test case should be skipped.

Select "no" which is also the default value if not specified, to have the
health check performed after completed the test case.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SOFTWARE_DIR' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "directory", # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the base directory that holds all the software to be deployed or
upgraded on the node, i.e. the directory that contains the 'eric-sc-*.csar' and
'eric-sc-tools-*.tgz' files.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TIMESHIFT_1_DAY_BEFORE_CERTIFICATE_EXPIRY' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
In new ECCD releases 2.18 and above the ECCD certificates are valid for 10
years and don't need to be rotated and renewed every year.
With this parameter it can be controlled if the time shift should be done with
as few steps as possible to reach beyond the 10 years into the future.

NOTE: This parameter is only checked if the ECCD version is between 2.18 and 2.26.
For ECCD versions 2.27 and higher we always first shift to 2 days before the first
certificate expires and then 185 after that date, so we always shift only 2 times
and this parameter serves no purpose.

Select "yes" if the time shift should always be done to 1 day before either the
ECCD or the SIP TLS certificate expire, whichever is expiring first.
Then certificates are renewed, and after that another time shift is done so we
go to 11 years into the future from current time.
This is the default when ECCD version is 2.18 or newer and the variable
TIMESHIFT_360_DAYS_AT_A_TIME keeps it's default value of "no".

Select "no" to have the time shift done in the maximum allowed steps depending
on the ECCD release version. I.e. if ECCD version is 2.17 or lower then the time
shift is done 360 days at the time resulting in 11 time shifts to reach the 10
year time shift goal, and if the ECCD release is 2.18 or higher then the initial
time shift is done with 3600 days and then the second time shift is done to 1
hour before the expiry of the SIP TLS certificate so only 2 time shifts are
needed to reach the 10 year goal.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TIMESHIFT_360_DAYS_AT_A_TIME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
In old ECCD releases 2.17 and below the ECCD certificates were only valid for
1 year and they had to be rotated and renewed every year. But starting with
ECCD 2.18 the certificates are valid for 10 Years and don't need to be rotated
and renewed every year.
With this parameter it can be controlled if the time shift should be done with
360 days at the time regardless of the ECCD release version.

Select "yes" if the time shift should always be done 360 days at the time
regardless of the ECCD version.

Select "no" which is also the default value if not specified, to have the
time shift done in the maximum allowed steps depending on the ECCD release
version. I.e. if ECCD version is 2.17 then the time shift is done 360 days
at the time resulting in 11 time shifts to reach the 10 year time shift goal,
and if the ECCD release is 2.18 or higher then the initial time shift is done
with 3600 days and only 2 time shifts are needed to reach the 10 year goal.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist performs a time shift test of the SC software by moving time into
the future in steps and renews the ECCD certificates as needed.
It is testing that the running system can still execute properly many years into
the future and checks that certificates are renewing when needed.

This Playlist MUST BE EXECUTED ON DIRECTOR-0 of the node because it uses Ansible
playbooks and are manipulating the kube config files.

!!!!!!!!!!!!!!!!!!!!!
!!!!!! WARNING !!!!!!
!!!!!!!!!!!!!!!!!!!!!

This playlist should not be executed unless you know what you are doing!

Because after executing this test and you want to bring the system back to
current time the only safe way to do this is to RE-INSTALL the ECCD node and the
SC software, i.e. everying running on this node WILL BE DESTROYED.

YOU HAVE BEEN WARNED!

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
