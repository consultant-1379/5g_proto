package Playlist::108_Miscellaneous_Tasks;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.6
#  Date     : 2024-06-19 13:54:00
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
use File::Basename qw(dirname basename);

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

use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;
use Playlist::919_Check_Registry_Information;
use Playlist::920_Check_Network_Config_Information;
use Playlist::936_Check_Deployed_Software;
use Playlist::938_Node_Information;

#
# Variable Declarations
#

our %playlist_variables;
set_playlist_variables();

my $debug_cmd = "";

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

    my $node_count = 0;
    my $rc;
    my $valid;
    my $var_name;

    # Set job type to be a deployment
    $::JOB_PARAMS{'JOBTYPE'} = "MISCELLANEOUS_TASKS";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_cmd = "echo ";
    }

    # If the playlist needs to have access to Network Configuration parameters
    # then it needs to check for this and give an error or ask the user to
    # provide the file name.
    if (Playlist::920_Check_Network_Config_Information::is_network_config_specified() != 0) {
        General::Logging::log_user_error_message("You must specify a network configuration file for this Playlist to run");
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P108S01, \&Fallback001_P108S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P108S02, \&Fallback001_P108S99 );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'CLEAN_REGISTRY'} eq "yes") {
        $rc = General::Playlist_Operations::execute_step( \&Clean_Registry_P108S03, \&Fallback001_P108S99 );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'CLEAN_CORE_DUMPS'} eq "yes") {
        $rc = General::Playlist_Operations::execute_step( \&Clean_Coredumps_P108S04, \&Fallback001_P108S99 );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'COLLECT_LOGS'} eq "yes") {
        $rc = General::Playlist_Operations::execute_step( \&Collect_Logs_P108S05, \&Fallback001_P108S99 );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'DELETE_BIG_WORKSPACE_FILES'} eq "yes") {
        $rc = General::Playlist_Operations::execute_step( \&Delete_Big_Workspace_Files_P108S06, \&Fallback001_P108S99 );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'DELETE_OLD_WORKSPACE_FILES'} eq "yes") {
        $rc = General::Playlist_Operations::execute_step( \&Delete_Old_Workspace_Files_P108S07, \&Fallback001_P108S99 );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'LOAD_CORE_DNS_DATA'} eq "yes") {
        $rc = General::Playlist_Operations::execute_step( \&Load_Core_Dns_Data_P108S08, \&Fallback001_P108S99 );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'CLEAN_REGISTRY'} eq "no" &&
        $::JOB_PARAMS{'CLEAN_CORE_DUMPS'} eq "no" &&
        $::JOB_PARAMS{'COLLECT_LOGS'} eq "no" &&
        $::JOB_PARAMS{'DELETE_BIG_WORKSPACE_FILES'} eq "no" &&
        $::JOB_PARAMS{'DELETE_OLD_WORKSPACE_FILES'} eq "no" &&
        $::JOB_PARAMS{'LOAD_CORE_DNS_DATA'} eq "no") {
        General::Logging::log_user_warning_message("No task specified, so nothing done");
    }

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P108S98, \&Fallback001_P108S99 );
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
sub Initialize_Job_Environment_P108S01 {

    my $rc;

    if ($::JOB_PARAMS{'CLEAN_REGISTRY'} eq "no") {
        # We only need to check docker or nerdctl when CLEAN_REGISTRY
        $::JOB_PARAMS{'SKIP_DOCKER_AND_NERDCTL_CHECK'} = "yes";
    }
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
sub Check_Job_Parameters_P108S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P108S02T01 } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'IGNORE_FAILED_SOFTWARE_VERSION_CHECK'} = "yes";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::936_Check_Deployed_Software::main } );
    return $rc if $rc < 0;

    # We need to parse the network configuration parameters again to either filter
    # out not wanted parameters or to add back all parameters in case no SC was deployed.
    $::JOB_PARAMS{'P920_TASK'} = "READ_PARAMETERS_FROM_FILE";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::920_Check_Network_Config_Information::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::919_Check_Registry_Information::main } );
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
    sub Check_Job_Parameters_P108S02T01 {

        my $crd_namespace = "";
        my $crd_release_name = "";
        my $sc_namespace = "";
        my $sc_release_name = "";
        my $rc = 0;
        my @result;
        my $tools_namespace = "";

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
                General::Logging::log_user_error_message("Network Config parameter 'sc_release_name' has not been set and Job parameter 'SC_RELEASE_NAME' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'SC_RELEASE_NAME', nor Network Config parameter 'sc_release_name' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper CRD_NAMESPACE
        General::Logging::log_user_message("Checking Job parameter 'CRD_NAMESPACE' and Network Config parameter 'crd_namespace'");
        if ($::JOB_PARAMS{'CRD_NAMESPACE'} ne "") {
            $crd_namespace = $::JOB_PARAMS{'CRD_NAMESPACE'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'crd_namespace'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'crd_namespace'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'CRD_NAMESPACE'} = $::NETWORK_CONFIG_PARAMS{'crd_namespace'}{'value'};
                $crd_namespace = $::JOB_PARAMS{'CRD_NAMESPACE'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'crd_namespace' has not been set and Job parameter 'CRD_NAMESPACE' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'CRD_NAMESPACE', nor Network Config parameter 'crd_namespace' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper CRD_RELEASE_NAME
        General::Logging::log_user_message("Checking Job parameter 'CRD_RELEASE_NAME' and Network Config parameter 'crd_release_name'");
        if ($::JOB_PARAMS{'CRD_RELEASE_NAME'} ne "") {
            $crd_release_name = $::JOB_PARAMS{'CRD_RELEASE_NAME'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'crd_release_name'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'crd_release_name'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'CRD_RELEASE_NAME'} = $::NETWORK_CONFIG_PARAMS{'crd_release_name'}{'value'};
                $crd_release_name = $::JOB_PARAMS{'CRD_RELEASE_NAME'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'crd_release_name' has not been set and Job parameter 'CRD_RELEASE_NAME' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'CRD_RELEASE_NAME', nor Network Config parameter 'crd_release_name' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

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

        # Get workspace directory if not specified
        if ($::JOB_PARAMS{'WORKSPACE_DIR'} eq "") {
            $::JOB_PARAMS{'WORKSPACE_DIR'} = dirname($::JOB_PARAMS{'_JOB_WORKSPACE_DIR'});
        }

        General::Logging::log_user_message("SC_NAMESPACE=$sc_namespace\nSC_RELEASE_NAME=$sc_release_name\nCRD_NAMESPACE=$crd_namespace\nCRD_RELEASE_NAME=$crd_release_name\nWORKSPACE_DIR=$::JOB_PARAMS{'WORKSPACE_DIR'}\nTOOLS_NAMESPACE=$::JOB_PARAMS{'TOOLS_NAMESPACE'}\n");

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
sub Clean_Registry_P108S03 {

    my $rc;

    show_boxed_message("Clean Registry");

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Clean_Registry_P108S03T01 } );
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
    sub Clean_Registry_P108S03T01 {
        my $command = "perl $::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/clear_registry.pl";
        my $rc = 0;
        my $registry_user = $::JOB_PARAMS{'SC_REGISTRY_USER'};
        my $registry_password = $::JOB_PARAMS{'SC_REGISTRY_PASSWORD'};
        my @result;
        my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/";

        $command .= " --cleanup";
        $command .= " --template-dir $template_file";
        $command .= " --template-file images_Template.txt";
        $command .= " --registry-user $registry_user";
        $command .= " --registry-password $registry_password";
        $command .= " --prune-workers";
        $command .= " --color=no";
        $command .= " --simulate" if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes");
        $command .= " --kubeconfig=$::JOB_PARAMS{'KUBECONFIG'}" if ($::JOB_PARAMS{'KUBECONFIG'} ne "");

        General::Logging::log_user_message("Call script clear_registry.pl");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            General::Logging::log_user_message(join "\n", @result);
            push @::JOB_STATUS, "(/) Clean Registry successful";
        } else {
            General::Logging::log_user_error_message(join "\n", @result);
            push @::JOB_STATUS, "(x) Clean Registry failed";
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
sub Clean_Coredumps_P108S04 {

    my $rc;

    show_boxed_message("Clean Core Dumps");

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Clean_Coredumps_P108S04T01 } );
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
    sub Clean_Coredumps_P108S04T01 {
        my $command = "${debug_cmd}perl $::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/system_health_check.pl";
        my $rc = 0;
        my @result;

        $command .= " --no_color" if ($::color_scheme eq "no");
        $command .= " --log_file $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/all.log";
        $command .= " --progress_type=none";
        $command .= " --kubeconfig=$::JOB_PARAMS{'KUBECONFIG'}" if ($::JOB_PARAMS{'KUBECONFIG'} ne "");
        $command .= " --check core_dumps=warning";
        $command .= " --variables core_dump_fetch=yes" if ($::JOB_PARAMS{'FETCH_CORE_DUMPS'} eq "yes");
        $command .= " --variables core_dump_directory=$::JOB_PARAMS{'_JOB_LOG_DIR'}/core_dumps";
        $command .= " --variables core_dump_delete=yes";
        $command .= " --variables core_dump_no_allow_file=yes";
        $command .= " --variables core_dump_time_threshold=0";

        General::Logging::log_user_message("Check and clean core dumps on the node");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            General::Logging::log_user_message(join "\n", @result);
            push @::JOB_STATUS, "(/) Clean Coredumps successful";
        } else {
            General::Logging::log_user_warning_message(join "\n", @result);
            push @::JOB_STATUS, "(x) Clean Coredumps failed";
        }

        # Always return success
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
sub Collect_Logs_P108S05 {

    my $rc = 0;

    General::State_Machine::always_execute_task("Playlist::914_Collect_Logs.+");

    if ($::JOB_PARAMS{'COLLECT_SC_LOGS'} eq "yes") {
        show_boxed_message("Collect SC Logs");

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'COLLECT_TOOLS_LOGS'} eq "yes") {
        # Since the 914 playlist only collects logs from SC_NAMESPACE
        my $old_sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        $::JOB_PARAMS{'SC_NAMESPACE'} = $::JOB_PARAMS{'TOOLS_NAMESPACE'};

        show_boxed_message("Collect Tools Logs");

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
        $::JOB_PARAMS{'SC_NAMESPACE'} = $old_sc_namespace;
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
sub Delete_Big_Workspace_Files_P108S06 {

    my $rc;

    show_boxed_message("Delete Big Workspace Files");

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Delete_Big_Workspace_Files_P108S06T01 } );
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
    sub Delete_Big_Workspace_Files_P108S06T01 {
        my $rc = 0;
        my @result;

        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
            General::Logging::log_user_message("Find files with a size $::JOB_PARAMS{'BIG_FILE_SIZE'} in $::JOB_PARAMS{'WORKSPACE_DIR'}\nThis might take a while to complete.");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "find $::JOB_PARAMS{'WORKSPACE_DIR'} -size $::JOB_PARAMS{'BIG_FILE_SIZE'} -print0 | xargs -r0 ls -lh --",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
        } else {
            General::Logging::log_user_message("Deleting files with a size $::JOB_PARAMS{'BIG_FILE_SIZE'} in $::JOB_PARAMS{'WORKSPACE_DIR'}\nThis might take a while to complete.");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "find $::JOB_PARAMS{'WORKSPACE_DIR'} -size $::JOB_PARAMS{'BIG_FILE_SIZE'} -print0 | xargs -r0 rm -fr --",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
        }

        if ($rc == 0) {
            General::Logging::log_user_message(join "\n", @result);
            push @::JOB_STATUS, "(/) Delete Big Workspace Files successful";
        } else {
            General::Logging::log_user_warning_message(join "\n", @result);
            push @::JOB_STATUS, "(x) Delete Big Workspace Files failed";
        }

        # Always return success
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
sub Delete_Old_Workspace_Files_P108S07 {

    my $rc;

    show_boxed_message("Delete Old Workspace Files");

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Delete_Old_Workspace_Files_P108S07T01 } );
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
    sub Delete_Old_Workspace_Files_P108S07T01 {
        my $rc = 0;
        my @result;

        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
            General::Logging::log_user_message("Find files with an age $::JOB_PARAMS{'OLD_FILES_IN_DAYS'} in $::JOB_PARAMS{'WORKSPACE_DIR'}\nThis might take a while to complete.");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "find $::JOB_PARAMS{'WORKSPACE_DIR'} -mtime $::JOB_PARAMS{'OLD_FILES_IN_DAYS'} -print0 | xargs -r0 ls -l --",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
        } else {
            General::Logging::log_user_message("Deleting files with an age $::JOB_PARAMS{'OLD_FILES_IN_DAYS'} in $::JOB_PARAMS{'WORKSPACE_DIR'}\nThis might take a while to complete.");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "find $::JOB_PARAMS{'WORKSPACE_DIR'} -mtime $::JOB_PARAMS{'OLD_FILES_IN_DAYS'} -print0 | xargs -r0 rm -fr --",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
        }

        if ($rc == 0) {
            General::Logging::log_user_message(join "\n", @result);
            push @::JOB_STATUS, "(/) Delete Old Workspace Files successful";
        } else {
            General::Logging::log_user_warning_message(join "\n", @result);
            push @::JOB_STATUS, "(x) Delete Old Workspace Files failed";
        }

        # Always return success
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
sub Load_Core_Dns_Data_P108S08 {

    my $rc;

    show_boxed_message("Load CoreDns Data");

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Save_Code_Dns_Data_Before_Change_P108S08T01 } );
    return $rc if $rc < 0;

    # Copy template file and replace place holders
    if ($::JOB_PARAMS{'worker_ip_ipv4'} !~ /^(CHANGEME|)$/ && $::JOB_PARAMS{'worker_ip_ipv6'} !~ /^(CHANGEME|)$/) {
        # Dual stack node
        $::JOB_PARAMS{'P938_INPUT_FILE_NAME'} = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/coredns/coredns_configmap_ds_Template.yaml";
        $::JOB_PARAMS{'P938_OUTPUT_FILE_NAME'} = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/coredns_configmap.yaml";
    } elsif ($::JOB_PARAMS{'worker_ip_ipv4'} !~ /^(CHANGEME|)$/) {
        # IPv4k node
        $::JOB_PARAMS{'P938_INPUT_FILE_NAME'} = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/coredns/coredns_configmap_ipv4_Template.yaml";
        $::JOB_PARAMS{'P938_OUTPUT_FILE_NAME'} = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/coredns_configmap.yaml";
    } else {
        # This should not happen, but just in case.
        General::Logging::log_user_message("The node does not seem to have a valid 'worker_ip_ipv4' or 'worker_ip_ipv4' and 'worker_ip_ipv6' address, so no loading of coredns data is done");
        return General::Playlist_Operations::RC_STEPOUT;
    }
    $::JOB_PARAMS{'P938_OUTPUT_FILE_ACCESSMODE'} = "666";
    $::JOB_PARAMS{'P938_REPLACE_FILE_PLACEHOLDERS'} = "yes";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::938_Node_Information::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_Core_Dns_Configmap_P108S08T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_Core_Dns_Deployment_P108S08T03 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Save_Code_Dns_Data_After_Change_P108S08T04 } );
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
    sub Save_Code_Dns_Data_Before_Change_P108S08T01 {
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Save printout of coredns configmap and deployment data before change");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => [
                    "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get configmap coredns -n kube-system -o yaml",
                    "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get deployment coredns -n kube-system -o yaml"
                ],
                "hide-output"   => 1,
                "command-in-output" => 1,
                "save-to-file"  => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/coredns_data_before_change.txt",
            }
        );

        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to print coredns data");
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
    sub Load_Core_Dns_Configmap_P108S08T02 {
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Changing coredns configmap");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_cmd}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} patch configmap coredns -n kube-system --patch \"\$(cat $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/coredns_configmap.yaml)\" --type=merge",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Change coredns configmap successful";
        } else {
            General::Logging::log_user_warning_message(join "\n", @result);
            push @::JOB_STATUS, "(x) Change coredns configmap failed";
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
    sub Load_Core_Dns_Deployment_P108S08T03 {
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Changing coredns deployment");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_cmd}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} patch deployment coredns -n kube-system --patch \"\$(cat $::JOB_PARAMS{'_PACKAGE_DIR'}/templates/coredns/coredns_deployment_Template.yaml)\" --type=merge",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Change coredns deployment successful";
        } else {
            General::Logging::log_user_warning_message(join "\n", @result);
            push @::JOB_STATUS, "(x) Change coredns deployment failed";
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
    sub Save_Code_Dns_Data_After_Change_P108S08T04 {
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Save printout of coredns configmap and deployment data after change");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => [
                    "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get configmap coredns -n kube-system -o yaml",
                    "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get deployment coredns -n kube-system -o yaml"
                ],
                "hide-output"   => 1,
                "command-in-output" => 1,
                "save-to-file"  => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/coredns_data_after_change.txt",
            }
        );

        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to print coredns data");
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
sub Cleanup_Job_Environment_P108S98 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::902_Cleanup_Job_Environment::main } );
    return $rc if $rc < 0;

    if ($rc == 0 && $::JOB_PARAMS{'JOBSTATUS'} eq "ONGOING") {
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
sub Fallback001_P108S99 {

    my $message = "";
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
        'BIG_FILE_SIZE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "+50M",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select the size that is considdered as a big file, it should be specified as an
integer value followed by one of the following suffixes:

    `b'    for 512-byte blocks (this is the default if no suffix is used)

    `c'    for bytes

    `w'    for two-byte words

    `k'    for kibibytes (KiB, units of 1024 bytes)

    `M'    for mebibytes (MiB, units of 1024 * 1024 = 1048576 bytes)

    `G'    for gibibytes (GiB, units of 1024 * 1024 * 1024 = 1073741824 bytes)

The  size  is  simply the st_size member of the struct stat populated by the
lstat (or stat) system call, rounded up as shown above.  In other words, it's
consistent with the result you get for ls -l.  Bear in mind that the `%k' and
`%b' format specifiers of -printf handle sparse files differently.
The `b' suffix always denotes 512-byte blocks and never 1024-byte blocks, which
is different to the behaviour of -ls.

The + and - prefixes signify greater than and less than, as usual; i.e., an exact
size of n units does not match.  Bear in mind that the size is rounded up to the
next unit.  Therefore -size -1M is not equivalent to -size -1048576c.
The former only matches empty files, the latter matches files from 0 to 1,048,575
bytes.
EOF
            'validity_mask' => '[+-]\d+[bcwkMG]',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CLEAN_CORE_DUMPS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should perform a cleanup of the core dumps on the node.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CLEAN_REGISTRY' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should perform a cleanup of the local registry.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'COLLECT_LOGS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should collect trouble shooting logs.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'COLLECT_SC_LOGS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should collect trouble shooting logs from SC namespace.

This parameter also require that you have "COLLECT_LOGS=yes" otherwise it is
ignored.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'COLLECT_TOOLS_LOGS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should collect trouble shooting logs from tools namespace.

This parameter also require that you have "COLLECT_LOGS=yes" otherwise it is
ignored.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CRD_NAMESPACE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The name to use for the CRD namespace if you want to override the parameter from
the network configuration file called 'crd_namespace'.
If you don't specify this parameter then it will take the value from the network
configuration file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CRD_RELEASE_NAME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The name to use for the CRD release name if you want to override the parameter
from the network configuration file called 'crd_release_name'.
If you don't specify this parameter then it will take the value from the network
configuration file.
EOF
            'validity_mask' => '.+',
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
Undeploy will take place.
Any command that will just change files or data on the job workspace directory
or any command that just reads data from the cluster will still be executed.
This parameter can be useful to check if the overall logic is working without
doing any changes to the running system.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DELETE_BIG_WORKSPACE_FILES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should delete all old files and directories that
are older than specified in the BIG_FILE_SIZE variable in the directory
specified in the WORKSPACE_DIR variable.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DELETE_OLD_WORKSPACE_FILES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should delete all old files and directories that
are older than specified in the OLD_FILES_IN_DAYS variable in the directory
specified in the WORKSPACE_DIR variable.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'FETCH_CORE_DUMPS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should fetch all core dumps and save them to the
job workspace directory before deleting them on the node.
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
        'LOAD_CORE_DNS_DATA' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should load coredns data into namespace kube-system.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'OLD_FILES_IN_DAYS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "+7",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select the number of days that a file is considdered as old, it should be
specified as an integer value.

File's data was last modified less than (prefix -), more than (prefix +) or
exactly n*24 hours ago.
See the comments for BIG_FILE_SIZE to understand how rounding affects the
interpretation of file modification times.
EOF
            'validity_mask' => '[+-]?\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'PASSWORD_EXPIRE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the password for newly created user accounts expire
on first login requiring a change of the password.

The default behavior is that all newly created user accounts will have the
password expire on the first login.

If this parameter is set to the value 'no' then a hack workaround will be used
to modify the LDAP data to mark the account to not expire. This "no" value
should only be used if you know what you are doing since it will bypass some
of the node hardening and will make the node less secure.
EOF
            'validity_mask' => '(yes|no)',
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
        'WORKSPACE_DIR' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "directory", # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select the directory path that chould be checked.
If not specified then it will use on level above the _JOB_WORKSPACE_DIR as a base.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub show_boxed_message {
    my $message = sprintf "# %s #", shift;
    my $length = length($message);

    General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist script is used for performing miscellaneous tasks and it will be
called from the execute_playlist.pl script.

The currently supported tasks are the following which should be set to "yes"
if they should be executed:

    - CLEAN_CORE_DUMPS              (Default no)
    - CLEAN_REGISTRY                (Default no)
    - COLLECT_LOGS                  (Default no)
        - COLLECT_SC_LOGS           (Default yes)
        - COLLECT_TOOLS_LOGS        (Default no)
    - DELETE_BIG_WORKSPACE_FILES    (Default no)
    - DELETE_OLD_WORKSPACE_FILES    (Default no)
    - LOAD_CORE_DNS_DATA            (Default no)

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
