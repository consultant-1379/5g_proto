package Playlist::109_Traffic_Tools_Install;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.39
#  Date     : 2024-06-13 12:30:28
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
use Cwd qw(abs_path cwd);

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    usage
    usage_return_playlist_variables
    );

use ADP::Kubernetes_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::102_Supreme_Certificate_Management;
use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;
use Playlist::919_Check_Registry_Information;
use Playlist::920_Check_Network_Config_Information;
use Playlist::922_Handle_Software_Directory_Files;
use Playlist::926_Handle_Docker_Images;
use Playlist::936_Check_Deployed_Software;

#
# Variable Declarations
#
our %playlist_variables;
set_playlist_variables();

my $debug_command = "";
my $warn_existing_secrets = 1;  # Change this value to 0 to stop with an error if a secret already exists
                                # or change it to 1 to just show a warning and then continue.

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
    $::JOB_PARAMS{'JOBTYPE'} = "TRAFFIC_TOOLS_INSTALL";

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

    # We always want to read the network config file for each playlist
    General::State_Machine::always_execute_task("Playlist::920_Check_Network_Config_Information::.+");

    # To avoid removing directories before all tasks has been executed we will
    # only execute the cleanup at the end.
    General::State_Machine::always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P109S01, \&Fallback001_P109S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P109S02, \&Fallback001_P109S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Install_Tools_P109S03, \&Fallback001_P109S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Collect_ADP_Logs_P109S04, \&Fallback001_P109S99 );
    return $rc if $rc < 0;

    # Now we need to execute the cleanup.
    General::State_Machine::remove_always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P109S05, \&Fallback001_P109S99 );
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
sub Initialize_Job_Environment_P109S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::901_Initialize_Job_Environment::main } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'NAMESPACE_CREATED'} = "no";

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
sub Check_Job_Parameters_P109S02 {

    my $rc = 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P109S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_TOOLS_TO_INSTALL_P109S02T02 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SC_MONITOR_WANTED'} eq "yes") {
        if ($::JOB_PARAMS{'SKIP_TOOL_INSTALLATION'} eq "no") {
            # We neeed to unpack everything since we also need to load the images from the SC package to make sure that
            # the sc-monitor software is loaded into the registry (mostly for AON nodes with no access to artifactory).
            # We also need to unpack the umbrella file since we need to find the certificate files.
            $::JOB_PARAMS{'CHECK_CSAR_FILE'}            = "yes";
            $::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'}    = "yes";
            $::JOB_PARAMS{'CHECK_TOOLS_FILE'}           = "yes";
            $::JOB_PARAMS{'CHECK_TOOLS_FILE_CONTENT'}   = "yes";
            $::JOB_PARAMS{'CHECK_VALUES_FILE'}          = "no";
            $::JOB_PARAMS{'COPY_UMBRELLA_FILE'}         = "no";
            $::JOB_PARAMS{'COPY_VALUES_FILE'}           = "no";
            $::JOB_PARAMS{'UNPACK_CSAR_FILE'}           = "yes";
            $::JOB_PARAMS{'UNPACK_TOOLS_FILE'}          = "yes";
            $::JOB_PARAMS{'UNPACK_UMBRELLA_FILE'}       = "yes";
        } else {
            # We don't need to install any software so we only need to unpack the umbrella file since we need to find the certificate files.
            $::JOB_PARAMS{'CHECK_CSAR_FILE'}            = "yes";
            $::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'}    = "yes";
            $::JOB_PARAMS{'CHECK_TOOLS_FILE'}           = "no";
            $::JOB_PARAMS{'CHECK_TOOLS_FILE_CONTENT'}   = "no";
            $::JOB_PARAMS{'CHECK_VALUES_FILE'}          = "no";
            $::JOB_PARAMS{'COPY_UMBRELLA_FILE'}         = "no";
            $::JOB_PARAMS{'COPY_VALUES_FILE'}           = "no";
            $::JOB_PARAMS{'UNPACK_CSAR_FILE'}           = "no";
            $::JOB_PARAMS{'UNPACK_TOOLS_FILE'}          = "no";
            $::JOB_PARAMS{'UNPACK_UMBRELLA_FILE'}       = "yes";
        }
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::922_Handle_Software_Directory_Files::main } );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_SOFTWARE_DIR_P109S02T03 } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'IGNORE_FAILED_SOFTWARE_VERSION_CHECK'} = "yes";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::936_Check_Deployed_Software::main } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'P920_TASK'} = "READ_PARAMETERS_FROM_FILE";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::920_Check_Network_Config_Information::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Workspace_Certificate_Directory_P109S02T04 } );
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
    sub Check_Job_Parameters_P109S02T01 {
        my $rc = 0;
        my $sc_namespace = "";
        my $sc_release_name = "";
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

        if ($::JOB_PARAMS{'KUBECONFIG'} eq "") {
            # Use default value
            $::JOB_PARAMS{'KUBECONFIG'} = "$ENV{'HOME'}/.kube/config";
        }
        if ($::JOB_PARAMS{'KUBECONFIG_TOOLS'} eq "") {
            # Use same as KUBECONFIG
            $::JOB_PARAMS{'KUBECONFIG_TOOLS'} = $::JOB_PARAMS{'KUBECONFIG'};
        }

        General::Logging::log_user_message(
            "KUBECONFIG=$::JOB_PARAMS{'KUBECONFIG'}\n" .
            "KUBECONFIG_TOOLS=$::JOB_PARAMS{'KUBECONFIG_TOOLS'}\n" .
            "SC_NAMESPACE=$sc_namespace\n" .
            "SC_RELEASE_NAME=$sc_release_name\n" .
            "TOOLS_NAMESPACE=$tools_namespace\n"
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
    sub Check_TOOLS_TO_INSTALL_P109S02T02 {
        my $rc = 0;

        General::Logging::log_user_message("Checking Job parameter 'TOOLS_TO_INSTALL'");
        if ($::JOB_PARAMS{'TOOLS_TO_INSTALL'} eq "") {
            General::Logging::log_user_error_message("Job parameter 'TOOLS_TO_INSTALL' not specified");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        $::JOB_PARAMS{'TOOLS_TO_INSTALL'} =~ s/\s+/,/g;
        $::JOB_PARAMS{'TOOLS_TO_INSTALL'} =~ s/,+/,/g;

        # Set default values for all traffic simulators
        $::JOB_PARAMS{'CHFSIM_WANTED'} = "no";
        $::JOB_PARAMS{'DSCLOAD_WANTED'} = "no";
        $::JOB_PARAMS{'K6_WANTED'} = "no";
        $::JOB_PARAMS{'NRFSIM_WANTED'} = "no";
        $::JOB_PARAMS{'SEPPSIM_WANTED'} = "no";
        $::JOB_PARAMS{'SC_MONITOR_WANTED'} = "no";
        $::JOB_PARAMS{'SFTP_WANTED'} = "no";

        for my $tool (split /,/, $::JOB_PARAMS{'TOOLS_TO_INSTALL'}) {
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

            General::Logging::log_user_error_message("Job parameter 'TOOLS_TO_INSTALL' contains no valid tools: $::JOB_PARAMS{'TOOLS_TO_INSTALL'}");
            return General::Playlist_Operations::RC_FALLBACK;
        }
        my $message  = "CHFSIM_WANTED:     $::JOB_PARAMS{'CHFSIM_WANTED'}\n";
        $message    .= "DSCLOAD_WANTED:    $::JOB_PARAMS{'DSCLOAD_WANTED'}\n";
        $message    .= "K6_WANTED:         $::JOB_PARAMS{'K6_WANTED'}\n";
        $message    .= "NRFSIM_WANTED:     $::JOB_PARAMS{'NRFSIM_WANTED'}\n";
        $message    .= "SC_MONITOR_WANTED: $::JOB_PARAMS{'SC_MONITOR_WANTED'}\n";
        $message    .= "SEPPSIM_WANTED:    $::JOB_PARAMS{'SEPPSIM_WANTED'}\n";
        $message    .= "SFTP_WANTED:       $::JOB_PARAMS{'SFTP_WANTED'}\n";
        General::Logging::log_user_message($message);

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
    sub Check_SOFTWARE_DIR_P109S02T03 {
        my $sc_monitor_chart_file = "";
        my $sc_monitor_chart_path = "";
        my $error_msg = "";
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Checking Job parameter 'SOFTWARE_DIR'");
        unless (-d "$::JOB_PARAMS{'SOFTWARE_DIR'}/base_stability_traffic/traffic_simulators") {
            General::Logging::log_user_error_message("Job parameter 'SOFTWARE_DIR' contains no sub-directory called 'base_stability_traffic/traffic_simulators/'");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        if ($::JOB_PARAMS{'SC_MONITOR_WANTED'} eq "yes") {
            if (-f "$::JOB_PARAMS{'SC_UMBRELLA_FILE'}") {
                # For older SC releases < 1.15 prior to CNCS all charts were in one umbrella chart so we already know that SC monitor chart file is inside this file.
                $sc_monitor_chart_file = $::JOB_PARAMS{'SC_UMBRELLA_FILE'};
                $sc_monitor_chart_path = "eric-sc-umbrella";
            } elsif ($::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} ne "" && -f "$::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'}") {
                # For newer SC releases >= 1.15 with CNCS there are many sub charts and the SC monitor is located in the eric-sc-cs- chart file so we use it.
                $sc_monitor_chart_file = $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'};
                $sc_monitor_chart_path = "eric-sc-cs";
            }

            if ($sc_monitor_chart_file eq "") {
                General::Logging::log_user_error_message("Job parameter 'SC_UMBRELLA_FILE' or 'APPLICATION_UMBRELLA_FILE' contains no file containing eric-sc-monitor charts");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        }

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "find $::JOB_PARAMS{'SOFTWARE_DIR'}/base_stability_traffic/traffic_simulators -type f",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to read files inside directory: $::JOB_PARAMS{'SOFTWARE_DIR'}/base_stability_traffic/traffic_simulators");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        $::JOB_PARAMS{'TOOL_DIRECTORY_SC_MONITOR_HELM'} = "";
        $::JOB_PARAMS{'TOOL_FILE_CHFSIM_DOCKER'} = "";
        $::JOB_PARAMS{'TOOL_FILE_CHFSIM_HELM'} = "";
        $::JOB_PARAMS{'TOOL_FILE_CHFSIM_VERSION'} = "";
        $::JOB_PARAMS{'TOOL_FILE_DSCLOAD_DOCKER'} = "";
        $::JOB_PARAMS{'TOOL_FILE_DSCLOAD_HELM'} = "";
        $::JOB_PARAMS{'TOOL_FILE_DSCLOAD_VERSION'} = "";
        $::JOB_PARAMS{'TOOL_FILE_INFLUXDB_DOCKER'} = "";
        $::JOB_PARAMS{'TOOL_FILE_INFLUXDB_HELM'} = "";
        $::JOB_PARAMS{'TOOL_FILE_INFLUXDB_VERSION'} = "";
        $::JOB_PARAMS{'TOOL_FILE_K6_DOCKER'} = "";
        $::JOB_PARAMS{'TOOL_FILE_K6_HELM'} = "";
        $::JOB_PARAMS{'TOOL_FILE_K6_VERSION'} = "";
        $::JOB_PARAMS{'TOOL_FILE_NRFSIM_DOCKER'} = "";
        $::JOB_PARAMS{'TOOL_FILE_NRFSIM_HELM'} = "";
        $::JOB_PARAMS{'TOOL_FILE_NRFSIM_VERSION'} = "";
        $::JOB_PARAMS{'TOOL_FILE_SEPPSIM_DOCKER'} = "";
        $::JOB_PARAMS{'TOOL_FILE_SEPPSIM_HELM'} = "";
        $::JOB_PARAMS{'TOOL_FILE_SEPPSIM_VERSION'} = "";
        $::JOB_PARAMS{'TOOL_FILE_SFTP_DOCKER'} = "";
        $::JOB_PARAMS{'TOOL_FILE_SFTP_HELM'} = "";
        $::JOB_PARAMS{'TOOL_FILE_SFTP_VERSION'} = "";
        for (@result) {
            # CHFSIM
            if (/^(.+\/traffic_simulators\/docker\/eric-chfsim-1.+\.tar)$/) {
                $::JOB_PARAMS{'TOOL_FILE_CHFSIM_DOCKER'} = $1;
            } elsif (/^(.+\/traffic_simulators\/helm\/eric-chfsim-1.+\.tgz)$/) {
                $::JOB_PARAMS{'TOOL_FILE_CHFSIM_HELM'} = $1;
            } elsif (/^(.+\/traffic_simulators\/versions\/var\.chfsim-version)$/) {
                $::JOB_PARAMS{'TOOL_FILE_CHFSIM_VERSION'} = $1;
            # DSCLOAD
            } elsif (/^(.+\/traffic_simulators\/docker\/eric-dscload-.+\.tar)$/) {
                $::JOB_PARAMS{'TOOL_FILE_DSCLOAD_DOCKER'} = $1;
            } elsif (/^(.+\/traffic_simulators\/helm\/eric-dscload-.+\.tgz)$/) {
                $::JOB_PARAMS{'TOOL_FILE_DSCLOAD_HELM'} = $1;
            } elsif (/^(.+\/traffic_simulators\/versions\/var\.dscload-version)$/) {
                $::JOB_PARAMS{'TOOL_FILE_DSCLOAD_VERSION'} = $1;
            # INFLUXDB
            } elsif (/^(.+\/traffic_simulators\/docker\/eric-influxdb-.+\.tar)$/) {
                $::JOB_PARAMS{'TOOL_FILE_INFLUXDB_DOCKER'} = $1;
            } elsif (/^(.+\/traffic_simulators\/helm\/eric-influxdb-.+\.tgz)$/) {
                $::JOB_PARAMS{'TOOL_FILE_INFLUXDB_HELM'} = $1;
            } elsif (/^(.+\/traffic_simulators\/versions\/var\.influxdb-version)$/) {
                $::JOB_PARAMS{'TOOL_FILE_INFLUXDB_VERSION'} = $1;
            # K6
            } elsif (/^(.+\/traffic_simulators\/docker\/eric-k6-.+\.tar)$/) {
                $::JOB_PARAMS{'TOOL_FILE_K6_DOCKER'} = $1;
            } elsif (/^(.+\/traffic_simulators\/helm\/eric-k6-.+\.tgz)$/) {
                $::JOB_PARAMS{'TOOL_FILE_K6_HELM'} = $1;
            } elsif (/^(.+\/traffic_simulators\/versions\/var\.k6-version)$/) {
                $::JOB_PARAMS{'TOOL_FILE_K6_VERSION'} = $1;
            # NRFSIM
            } elsif (/^(.+\/traffic_simulators\/docker\/eric-nrfsim-.+\.tar)$/) {
                $::JOB_PARAMS{'TOOL_FILE_NRFSIM_DOCKER'} = $1;
            } elsif (/^(.+\/traffic_simulators\/helm\/eric-nrfsim-.+\.tgz)$/) {
                $::JOB_PARAMS{'TOOL_FILE_NRFSIM_HELM'} = $1;
            } elsif (/^(.+\/traffic_simulators\/versions\/var\.nrfsim-version)$/) {
                $::JOB_PARAMS{'TOOL_FILE_NRFSIM_VERSION'} = $1;
            # SEPPSIM
            } elsif (/^(.+\/traffic_simulators\/docker\/eric-seppsim-.+\.tar)$/) {
                $::JOB_PARAMS{'TOOL_FILE_SEPPSIM_DOCKER'} = $1;
            } elsif (/^(.+\/traffic_simulators\/helm\/eric-seppsim-.+\.tgz)$/) {
                $::JOB_PARAMS{'TOOL_FILE_SEPPSIM_HELM'} = $1;
            } elsif (/^(.+\/traffic_simulators\/versions\/var\.seppsim-version)$/) {
                $::JOB_PARAMS{'TOOL_FILE_SEPPSIM_VERSION'} = $1;
            # SFTP
            } elsif (/^(.+\/traffic_simulators\/docker\/eric-atmoz-sftp-.+\.tar)$/) {
                $::JOB_PARAMS{'TOOL_FILE_SFTP_DOCKER'} = $1;
            } elsif (/^(.+\/traffic_simulators\/helm\/eric-atmoz-sftp-.+\.tgz)$/) {
                $::JOB_PARAMS{'TOOL_FILE_SFTP_HELM'} = $1;
            } elsif (/^(.+\/traffic_simulators\/versions\/var\.sftp-version)$/) {
                $::JOB_PARAMS{'TOOL_FILE_SFTP_VERSION'} = $1;
            }
        }


        if ($::JOB_PARAMS{'CHFSIM_WANTED'} eq "yes") {
            if ($::JOB_PARAMS{'TOOL_FILE_CHFSIM_DOCKER'} eq "") {
                $error_msg .= "  - The CHFSIM docker file is missing.\n";
            }
            if ($::JOB_PARAMS{'TOOL_FILE_CHFSIM_HELM'} eq "") {
                $error_msg .= "  - The CHFSIM helm file is missing.\n";
            }
            if ($::JOB_PARAMS{'TOOL_FILE_CHFSIM_VERSION'} eq "") {
                $error_msg .= "  - The CHFSIM version file is missing.\n";
            }
        }
        if ($::JOB_PARAMS{'DSCLOAD_WANTED'} eq "yes") {
            if ($::JOB_PARAMS{'TOOL_FILE_DSCLOAD_DOCKER'} eq "") {
                $error_msg .= "  - The DSCLOAD docker file is missing.\n";
            }
            if ($::JOB_PARAMS{'TOOL_FILE_DSCLOAD_HELM'} eq "") {
                $error_msg .= "  - The DSCLOAD helm file is missing.\n";
            }
            if ($::JOB_PARAMS{'TOOL_FILE_DSCLOAD_VERSION'} eq "") {
                $error_msg .= "  - The DSCLOAD version file is missing.\n";
            }
        }
        if ($::JOB_PARAMS{'INFLUXDB_WANTED'} eq "yes") {
            if ($::JOB_PARAMS{'TOOL_FILE_INFLUXDB_DOCKER'} eq "") {
                $error_msg .= "  - The INFLUXDB docker file is missing.\n";
            }
            if ($::JOB_PARAMS{'TOOL_FILE_INFLUXDB_HELM'} eq "") {
                $error_msg .= "  - The INFLUXDB helm file is missing.\n";
            }
            if ($::JOB_PARAMS{'TOOL_FILE_INFLUXDB_VERSION'} eq "") {
                $error_msg .= "  - The INFLUXDB version file is missing.\n";
            }
        }
        if ($::JOB_PARAMS{'K6_WANTED'} eq "yes") {
            if ($::JOB_PARAMS{'TOOL_FILE_K6_DOCKER'} eq "") {
                $error_msg .= "  - The K6 docker file is missing.\n";
            }
            if ($::JOB_PARAMS{'TOOL_FILE_K6_HELM'} eq "") {
                $error_msg .= "  - The K6 helm file is missing.\n";
            }
            if ($::JOB_PARAMS{'TOOL_FILE_K6_VERSION'} eq "") {
                $error_msg .= "  - The K6 version file is missing.\n";
            }
        }
        if ($::JOB_PARAMS{'NRFSIM_WANTED'} eq "yes") {
            if ($::JOB_PARAMS{'TOOL_FILE_NRFSIM_DOCKER'} eq "") {
                $error_msg .= "  - The NRFSIM docker file is missing.\n";
            }
            if ($::JOB_PARAMS{'TOOL_FILE_NRFSIM_HELM'} eq "") {
                $error_msg .= "  - The NRFSIM helm file is missing.\n";
            }
            if ($::JOB_PARAMS{'TOOL_FILE_NRFSIM_VERSION'} eq "") {
                $error_msg .= "  - The NRFSIM version file is missing.\n";
            }
        }
        if ($::JOB_PARAMS{'SEPPSIM_WANTED'} eq "yes") {
            if ($::JOB_PARAMS{'TOOL_FILE_SEPPSIM_DOCKER'} eq "") {
                $error_msg .= "  - The SEPPSIM docker file is missing.\n";
            }
            if ($::JOB_PARAMS{'TOOL_FILE_SEPPSIM_HELM'} eq "") {
                $error_msg .= "  - The SEPPSIM helm file is missing.\n";
            }
            if ($::JOB_PARAMS{'TOOL_FILE_SEPPSIM_VERSION'} eq "") {
                $error_msg .= "  - The SEPPSIM version file is missing.\n";
            }
        }
        if ($::JOB_PARAMS{'SFTP_WANTED'} eq "yes") {
            if ($::JOB_PARAMS{'TOOL_FILE_SFTP_DOCKER'} eq "") {
                $error_msg .= "  - The SFTP docker file is missing.\n";
            }
            if ($::JOB_PARAMS{'TOOL_FILE_SFTP_HELM'} eq "") {
                $error_msg .= "  - The SFTP helm file is missing.\n";
            }
            if ($::JOB_PARAMS{'TOOL_FILE_SFTP_VERSION'} eq "") {
                $error_msg .= "  - The SFTP version file is missing.\n";
            }
        }

        if ($::JOB_PARAMS{'SC_MONITOR_WANTED'} eq "yes") {
            General::Logging::log_user_message("Unpacking SC Umbrella file to find the SC Monitor helm directory");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "tar xf $sc_monitor_chart_file -C $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to unpack SC Umbrella file: $sc_monitor_chart_file");
                return General::Playlist_Operations::RC_FALLBACK;
            }
            unless (-d "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software/$sc_monitor_chart_path/charts/eric-sc-monitor") {
                General::Logging::log_user_error_message("Failed to find directory: $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software/$sc_monitor_chart_path/charts/eric-sc-monitor");
                return General::Playlist_Operations::RC_FALLBACK;
            }
            $::JOB_PARAMS{'TOOL_DIRECTORY_SC_MONITOR_HELM'} = "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software/$sc_monitor_chart_path/charts/eric-sc-monitor";

            if (-f "$::JOB_PARAMS{'SOFTWARE_DIR'}/certificates/sc-monitor/eric-tm-ingress-controller-cr-client-ca.yaml") {
                $::JOB_PARAMS{'SC_MONITOR_CLIENT_SECRET_FILE'} = "$::JOB_PARAMS{'SOFTWARE_DIR'}/certificates/sc-monitor/eric-tm-ingress-controller-cr-client-ca.yaml";
            } else {
                General::Logging::log_user_error_message("Failed to find file: $::JOB_PARAMS{'SOFTWARE_DIR'}/certificates/sc-monitor/eric-tm-ingress-controller-cr-client-ca.yaml");
                return General::Playlist_Operations::RC_FALLBACK;
            }

            if (-f "$::JOB_PARAMS{'SOFTWARE_DIR'}/certificates/sc-monitor/eric-sc-monitor-tls-external-server-certificate.yaml") {
                $::JOB_PARAMS{'SC_MONITOR_SERVER_SECRET_FILE'} = "$::JOB_PARAMS{'SOFTWARE_DIR'}/certificates/sc-monitor/eric-sc-monitor-tls-external-server-certificate.yaml";
            } else {
                General::Logging::log_user_error_message("Failed to find file: $::JOB_PARAMS{'SOFTWARE_DIR'}/certificates/sc-monitor/eric-sc-monitor-tls-external-server-certificate.yaml");
                return General::Playlist_Operations::RC_FALLBACK;
            }

            if (-f "$::JOB_PARAMS{'SOFTWARE_DIR'}/certificates/sc-monitor/eric-sc-monitor-secret.yaml") {
                $::JOB_PARAMS{'SC_MONITOR_SECRET_FILE'} = "$::JOB_PARAMS{'SOFTWARE_DIR'}/certificates/sc-monitor/eric-sc-monitor-secret.yaml";
            } else {
                General::Logging::log_user_error_message("Failed to find file: $::JOB_PARAMS{'SOFTWARE_DIR'}/certificates/sc-monitor/eric-sc-monitor-secret.yaml");
                return General::Playlist_Operations::RC_FALLBACK;
            }

            # This code should currently be seen as a workaround until it can be checked why with the introduction
            # of CNCS in SC 1.15 the eric-sc-monitor-secret.yaml file is not enough.
            # If the following file is not loaded then we get the following error when starting up sc-monitor pod:
            #   Events:
            #   Type     Reason       Age                   From               Message
            #   ----     ------       ----                  ----               -------
            #   Normal   Scheduled    7m11s                 default-scheduler  Successfully assigned eiffelesc-tools/eric-sc-monitor-7655b46756-59lmj to eevee-capo-pool1-cj5bj
            #   Warning  FailedMount  59s (x11 over 7m11s)  kubelet            MountVolume.SetUp failed for volume "server-certificate" : secret "eric-sc-monitor-server-certificate" not found
            if (-f "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/workarounds/cncs/eric-sc-monitor-server-certificate.yaml") {
                $::JOB_PARAMS{'SC_MONITOR_SECRET_FILE2'} = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/workarounds/cncs/eric-sc-monitor-server-certificate.yaml";
            } else {
                # File does not exist, maybe no longer needed.
                $::JOB_PARAMS{'SC_MONITOR_SECRET_FILE2'} = "";
            }
        }

        my $message = "";
        for my $tag (
            'SC_MONITOR_CLIENT_SECRET_FILE',
            'SC_MONITOR_SERVER_SECRET_FILE',
            'SC_MONITOR_SECRET_FILE',
            'SC_MONITOR_SECRET_FILE2',
            'TOOL_DIRECTORY_SC_MONITOR_HELM',
            'TOOL_FILE_CHFSIM_DOCKER',
            'TOOL_FILE_CHFSIM_HELM',
            'TOOL_FILE_CHFSIM_VERSION',
            'TOOL_FILE_DSCLOAD_DOCKER',
            'TOOL_FILE_DSCLOAD_HELM',
            'TOOL_FILE_DSCLOAD_VERSION',
            'TOOL_FILE_INFLUXDB_DOCKER',
            'TOOL_FILE_INFLUXDB_HELM',
            'TOOL_FILE_INFLUXDB_VERSION',
            'TOOL_FILE_K6_DOCKER',
            'TOOL_FILE_K6_HELM',
            'TOOL_FILE_K6_VERSION',
            'TOOL_FILE_NRFSIM_DOCKER',
            'TOOL_FILE_NRFSIM_HELM',
            'TOOL_FILE_NRFSIM_VERSION',
            'TOOL_FILE_SEPPSIM_DOCKER',
            'TOOL_FILE_SEPPSIM_HELM',
            'TOOL_FILE_SEPPSIM_VERSION',
            'TOOL_FILE_SFTP_DOCKER',
            'TOOL_FILE_SFTP_HELM',
            'TOOL_FILE_SFTP_VERSION'
        ) {
            $message .= sprintf "%-31s %s\n", "$tag:", $::JOB_PARAMS{$tag};
        }
        General::Logging::log_user_message($message);

        if ($error_msg eq "") {
            return 0;
        } else {
            General::Logging::log_user_error_message("The following files are missing:\n$error_msg");
            $rc = General::Playlist_Operations::RC_FALLBACK;
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
    sub Create_Workspace_Certificate_Directory_P109S02T04 {
        my $rc = 0;

        General::Logging::log_user_message("Creating workspace certificates directory");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "mkdir -p $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/certificates",
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            if ($::JOB_PARAMS{'CERTIFICATE_DIRECTORY'} eq "") {
                # The user did not specify a specific certificate directory, use the one in the workspace
                $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'} = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/certificates";
            }
        } else {
            # Display the result in case of error
            push @::JOB_STATUS, '(x) Failed to create workspace certificates directory';
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to create workspace certificates directory");
            $rc = General::Playlist_Operations::RC_FALLBACK;
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
sub Install_Tools_P109S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Tools_Namespace_On_Node_P109S03T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Tools_Pull_Secret_P109S03T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_And_Install_Tools_Certificates_P109S03T03 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_TOOL_INSTALLATION'} eq "yes") {
        # We now make most of the tasks to be skipped, except for Copy_Certificates_To_K6_Pods_P109S03T10 and Check_Pod_Status_P109S03T20.
        General::State_Machine::always_skip_task("Playlist::919_Check_Registry_Information::.+");
        General::State_Machine::always_skip_task("Playlist::109_Traffic_Tools_Install::Docker_Login_P109S03T04");
        General::State_Machine::always_skip_task("Playlist::109_Traffic_Tools_Install::Load_DSCLOAD_P109S03T05");
        General::State_Machine::always_skip_task("Playlist::109_Traffic_Tools_Install::Unpack_DSCLOAD_File_And_Change_Values_File_P109S03T06");
        General::State_Machine::always_skip_task("Playlist::109_Traffic_Tools_Install::Install_DSCLOAD_P109S03T07");
        General::State_Machine::always_skip_task("Playlist::109_Traffic_Tools_Install::Load_K6_P109S03T08");
        General::State_Machine::always_skip_task("Playlist::109_Traffic_Tools_Install::Install_K6_P109S03T09");
        General::State_Machine::always_skip_task("Playlist::109_Traffic_Tools_Install::Load_NRFSIM_P109S03T11");
        General::State_Machine::always_skip_task("Playlist::109_Traffic_Tools_Install::Install_NRFSIM_P109S03T12");
        General::State_Machine::always_skip_task("Playlist::109_Traffic_Tools_Install::Load_SFTP_P109S03T15");
        General::State_Machine::always_skip_task("Playlist::109_Traffic_Tools_Install::Install_SFTP_P109S03T16");
        General::State_Machine::always_skip_task("Playlist::109_Traffic_Tools_Install::Install_SC_MONITOR_P109S03T17");
        General::State_Machine::always_skip_task("Playlist::926_Handle_Docker_Images::main");
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::919_Check_Registry_Information::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Docker_Login_P109S03T04 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'DSCLOAD_WANTED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_DSCLOAD_P109S03T05 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Unpack_DSCLOAD_File_And_Change_Values_File_P109S03T06 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_DSCLOAD_P109S03T07 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'K6_WANTED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_K6_P109S03T08 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_K6_P109S03T09 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Certificates_To_K6_Pods_P109S03T10 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'NRFSIM_WANTED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_NRFSIM_P109S03T11 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_NRFSIM_P109S03T12 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'SEPPSIM_WANTED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_SEPPSIM_P109S03T13 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_SEPPSIM_P109S03T14 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'SFTP_WANTED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_SFTP_P109S03T15 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_SFTP_P109S03T16 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'SC_MONITOR_WANTED'} eq "yes") {
        if ($::JOB_PARAMS{'SKIP_LOADING_SC_MONITOR_IMAGE'} eq "no") {
            $::JOB_PARAMS{'SC_REGISTRY_PASSWORD'} = $::JOB_PARAMS{'TOOLS_REGISTRY_PASSWORD'};
            $::JOB_PARAMS{'SC_REGISTRY_PORT'} = $::JOB_PARAMS{'TOOLS_REGISTRY_PORT'};
            $::JOB_PARAMS{'SC_REGISTRY_URL'} = $::JOB_PARAMS{'TOOLS_REGISTRY_URL'};
            $::JOB_PARAMS{'SC_REGISTRY_USER'} = $::JOB_PARAMS{'TOOLS_REGISTRY_USER'};
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::926_Handle_Docker_Images::main } );
            return $rc if $rc < 0;
        }

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_SC_MONITOR_P109S03T17 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'CHFSIM_WANTED'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_CHFSIM_P109S03T18 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_CHFSIM_P109S03T19 } );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Pod_Status_P109S03T20 } );
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
    sub Create_Tools_Namespace_On_Node_P109S03T01 {
        my $found = 0;
        my $rc = 0;
        my @result;

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
        if ($found == 0) {
            General::Logging::log_user_message("Creating namespace $::JOB_PARAMS{'TOOLS_NAMESPACE'} on the node");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} create namespace $::JOB_PARAMS{'TOOLS_NAMESPACE'}",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to create namespace");
                return 1;
            }
        } else {
            General::Logging::log_user_warning_message("Namespace $::JOB_PARAMS{'TOOLS_NAMESPACE'} already exist, thist might result in failures later if it's not empty");
        }

        $::JOB_PARAMS{'NAMESPACE_CREATED'} = "yes";

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
    sub Create_Tools_Pull_Secret_P109S03T02 {
        my $found = 0;
        my $rc = 0;
        my @result;
        my $tools_pull_secret = $::NETWORK_CONFIG_PARAMS{'tools_pull_secret'}{'value'};
        my $tools_registry_password = $::NETWORK_CONFIG_PARAMS{'tools_registry_password'}{'value'};
        my $tools_registry_url = $::NETWORK_CONFIG_PARAMS{'tools_registry_url'}{'value'};
        my $tools_registry_user = $::NETWORK_CONFIG_PARAMS{'tools_registry_user'}{'value'};

        General::Logging::log_user_message("Create Local Docker Registry secret");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} create secret docker-registry $tools_pull_secret --docker-server $tools_registry_url --docker-username $tools_registry_user --docker-password '$tools_registry_password'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Creation of docker registry secret was successful';
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create $tools_pull_secret not needed";
                        return 0;
                    } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create $tools_pull_secret not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to create docker registry secret $tools_pull_secret");
            push @::JOB_STATUS, "(x) Create $tools_pull_secret failed";

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
    sub Create_And_Install_Tools_Certificates_P109S03T03 {
        my $rc = 0;
        my @result;

        # Set 102 playlist variables
        $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} = "";
        $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} = "";
        $::JOB_PARAMS{'NAMESPACE'} = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
        if ($::JOB_PARAMS{'DSCLOAD_WANTED'} eq "yes") {
            if ($::JOB_PARAMS{'CERTIFICATE_DIRECTORY'} ne "") {
                if ( -d "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/dscload") {
                    # The directory exist, so we assume that all needed files also exist in this directory and we do not generate any new certificates
                } else {
                    # The directory does not exist, so we need to generate the certificate
                    $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} .= "dscload,";
                }
            } else {
                # No directoy specified so we need to generate the certificate
                $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} .= "dscload,";
            }
            $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} .= "dscload,";
        }
        if ($::JOB_PARAMS{'K6_WANTED'} eq "yes") {
            if ($::JOB_PARAMS{'CERTIFICATE_DIRECTORY'} ne "") {
                if ( -d "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6") {
                    # The directory exist, so we assume that all needed files also exist in this directory and we do not generate any new certificates
                } else {
                    # The directory does not exist, so we need to generate the certificate
                    $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} .= "k6,";
                }
            } else {
                # No directoy specified so we need to generate the certificate
                $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} .= "k6,";
            }
            $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} .= "k6,";
        }
        if ($::JOB_PARAMS{'NRFSIM_WANTED'} eq "yes") {
            if ($::JOB_PARAMS{'CERTIFICATE_DIRECTORY'} ne "") {
                if ( -d "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/nrfsim") {
                    # The directory exist, so we assume that all needed files also exist in this directory and we do not generate any new certificates
                } else {
                    # The directory does not exist, so we need to generate the certificate
                    $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} .= "nrfsim,";
                }
            } else {
                # No directoy specified so we need to generate the certificate
                $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} .= "nrfsim,";
            }
            $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} .= "nrfsim,";
        }
        if ($::JOB_PARAMS{'SEPPSIM_WANTED'} eq "yes") {
            if ($::JOB_PARAMS{'CERTIFICATE_DIRECTORY'} ne "") {
                if ( -d "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/seppsim") {
                    # The directory exist, so we assume that all needed files also exist in this directory and we do not generate any new certificates
                } else {
                    # The directory does not exist, so we need to generate the certificate
                    $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} .= "seppsim,";
                }
            } else {
                # No directoy specified so we need to generate the certificate
                $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} .= "seppsim,";
            }
            $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} .= "seppsim,";
        }
        $::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} =~ s/,$//g;
        $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} =~ s/,$//g;

        if ($::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} ne "") {
            $::JOB_PARAMS{'GENERATE_CERTIFICATES'} = "yes";
        } else {
            $::JOB_PARAMS{'GENERATE_CERTIFICATES'} = "no";
        }

        if ($::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} ne "") {
            $::JOB_PARAMS{'INSTALL_CERTIFICATES'} = "yes";
        } else {
            $::JOB_PARAMS{'INSTALL_CERTIFICATES'} = "no";
        }

        if ($::JOB_PARAMS{'CERTIFICATES_TO_GENERATE'} eq "" && $::JOB_PARAMS{'CERTIFICATES_TO_INSTALL'} eq "") {
            General::Logging::log_user_message("No Certificates or Secrets to Create and Install");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # We need to change the KUBECONFIG file to point to the KUBECONFIG_TOOLS file for the execution of the 102 playlist
        my $old_kubeconfig = $::JOB_PARAMS{'KUBECONFIG'};
        $::JOB_PARAMS{'KUBECONFIG'} = $::JOB_PARAMS{'KUBECONFIG_TOOLS'};

        #
        # Generate and install tool secrets
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::102_Supreme_Certificate_Management::main } );
        $::JOB_PARAMS{'KUBECONFIG'} = $old_kubeconfig;
        return $rc if $rc < 0;

        # Set Job Type again since it was changed inside the 102 playlist
        $::JOB_PARAMS{'JOBTYPE'} = "TRAFFIC_TOOLS_INSTALL";

        #
        # Generate and install other certificates and secrets that the SUPREME
        # tool cannot handle.
        #

        # Create special RP_206033.crt and RP_206033.key files for the SEPP traffic
        if ($::JOB_PARAMS{'SEPPSIM_WANTED'} eq "yes" && $::JOB_PARAMS{'K6_WANTED'} eq "yes") {
            #
            # Create output directory
            #

            if ( -d "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033") {
                General::Logging::log_user_message("Directory k6/rp206033 already exist");
            } else {
                General::Logging::log_user_message("Creating directory for k6/rp206033 files");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "mkdir -p $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to create the k6 directory");
                    return 1;
                }
            }

            #
            # Creating RP_206033.crt and RP_206033.key files
            #

            if ( -f "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033_input.txt") {
                General::Logging::log_user_message("File RP_206033_input.txt already exist");
            } else {
                General::Logging::log_user_message("Creating RP_206033_input.txt file");
                $rc = General::File_Operations::write_file(
                    {
                        "filename"            => "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033_input.txt",
                        "output-ref"          => [
                            '[req]',
                            'distinguished_name = dn',
                            'x509_extensions = v3_req',
                            'req_extensions = v3_req',
                            'prompt = no',
                            '',
                            '[dn]',
                            'C = BE',
                            'L = Brussels',
                            'O = Ericsson',
                            'CN = nfamf.region1.amf.5gc.mnc033.mcc206.3gppnetwork.org',
                            '',
                            '[v3_req]',
                            'subjectAltName = @alt_names',
                            '',
                            '[alt_names]',
                            'DNS.1 = *.region1.amf.5gc.mnc033.mcc206.3gppnetwork.org',
                        ],
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write the RP_206033_input.txt file");
                    return 1;
                }
            }

            if ( -f "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033.key") {
                General::Logging::log_user_message("File RP_206033.key already exist");
            } else {
                General::Logging::log_user_message("Creating RP_206033.key file");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "openssl req -nodes -newkey rsa:2048 -keyout $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033.key -out $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/csr206033.pem -config $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033_input.txt",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to create the RP_206033.key file");
                    return 1;
                }
            }

            if ( -f "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033.crt") {
                General::Logging::log_user_message("File RP_206033.crt already exist");
            } else {
                General::Logging::log_user_message("Creating RP_206033.crt file");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "openssl x509 -req -in $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/csr206033.pem -CA $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/rootca/cert.pem -CAkey $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/rootca/key.pem -CAcreateserial -days $::JOB_PARAMS{'CERTIFICATE_VALIDITY_DAYS'} -out $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033.crt -extensions 'v3_req' -extfile $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033_input.txt",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to create the RP_206033.crt file");
                    return 1;
                }
            }
        }

        if ($::JOB_PARAMS{'SC_MONITOR_WANTED'} eq "yes") {
            # Install secrets into the tools namespace
            for my $var (qw /SC_MONITOR_CLIENT_SECRET_FILE  SC_MONITOR_SERVER_SECRET_FILE  SC_MONITOR_SECRET_FILE  SC_MONITOR_SECRET_FILE2/) {
                if ($::JOB_PARAMS{$var} eq "") {
                    # File does not exist, so nothing to load. Most likely because of SC_MONITOR_SECRET_FILE2
                    next;
                }
                General::Logging::log_user_message("Loading $var: $::JOB_PARAMS{$var}");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} apply -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} -f $::JOB_PARAMS{$var}",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc != 0) {
                    my $secret_exists = 0;
                    if ($warn_existing_secrets == 1) {
                        for (@result) {
                            if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                                # Ignore the error and just report it as a warning
                                General::Logging::log_user_warning_message($1);
                                $secret_exists = 1;
                                last;
                            } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                                # Ignore the error and just report it as a warning
                                General::Logging::log_user_warning_message($1);
                                $secret_exists = 1;
                                last;
                            }
                        }
                    }
                    if ($secret_exists == 0) {
                        # Display the result in case of error
                        General::OS_Operations::write_last_temporary_output_to_progress();

                        General::Logging::log_user_error_message("Failed to load $var");
                        return 1;
                    }
                    $rc = 0;
                }
            }
        }

        if ($::JOB_PARAMS{'CHFSIM_WANTED'} eq "yes") {
            if ( -d "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/chfsim/chf-certificates.yaml") {
                General::Logging::log_user_message("File chfsim/chf-certificates.yaml already exist");
            } else {
                General::Logging::log_user_message("Encoding certificate chfsim/cert.pem to one-line");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "cat $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/chfsim/cert.pem | base64 | tr -d '\\n'",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to encode chfsim/cert.pem");
                    return 1;
                }
                my $chfsim_cert_pem = $result[0];

                General::Logging::log_user_message("Encoding certificate chfsim/key.pem to one-line");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "cat $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/chfsim/key.pem | base64 | tr -d '\\n'",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to encode chfsim/key.pem");
                    return 1;
                }
                my $chfsim_key_pem = $result[0];

                General::Logging::log_user_message("Encoding certificate rootca/cert.pem to one-line");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "cat $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/rootca/cert.pem | base64 | tr -d '\\n'",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to encode rootca/cert.pem");
                    return 1;
                }
                my $rootca_crt = $result[0];

                General::Logging::log_user_message("Creating chfsim/chf-certificates.yaml file");
                $rc = General::File_Operations::write_file(
                    {
                        "filename"            => "$::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/chfsim/chf-certificates.yaml",
                        "output-ref"          => [
                            'apiVersion: v1',
                            'data:',
                            "  cert.pem: $chfsim_cert_pem",
                            "  key.pem: $chfsim_key_pem",
                            "  rootCA.crt: $rootca_crt",
                            'kind: Secret',
                            'metadata:',
                            '  name: chf-certificates',
                            'type: Opaque',
                        ],
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write the chfsim/chf-certificates.yaml file");
                    return 1;
                }
            }

            General::Logging::log_user_message("Installing chfsim secrets");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} apply -f $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/chfsim/chf-certificates.yaml -n $::JOB_PARAMS{'TOOLS_NAMESPACE'}",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) Create CFHSIM secrets successful";
            } else {
                my $secret_exists = 0;
                if ($warn_existing_secrets == 1) {
                    for (@result) {
                        if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                            # Ignore the error and just report it as a warning
                            General::Logging::log_user_warning_message($1);
                            push @::JOB_STATUS, "(-) Create CFHSIM secrets not needed";
                            $secret_exists = 1;
                            last;
                        } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                            # Ignore the error and just report it as a warning
                            General::Logging::log_user_warning_message($1);
                            push @::JOB_STATUS, "(-) Create CFHSIM secrets not needed";
                            $secret_exists = 1;
                            last;
                        }
                    }
                }
                if ($secret_exists == 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to install chfsim secrets");
                    push @::JOB_STATUS, "(x) Create CFHSIM secrets failed";
                    return 1;
                }
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
    sub Docker_Login_P109S03T04 {

        my $container_cmd = $::JOB_PARAMS{'CONTAINER_COMMAND'};
        my $rc = 0;
        my $registry_password = $::JOB_PARAMS{'TOOLS_REGISTRY_PASSWORD'};
        my $registry_port = $::JOB_PARAMS{'TOOLS_REGISTRY_PORT'};
        my $registry_url = $::JOB_PARAMS{'TOOLS_REGISTRY_URL'};
        my $registry_user = $::JOB_PARAMS{'TOOLS_REGISTRY_USER'};
        my $url = "$registry_url:$registry_port";

        if ($registry_url eq "") {
            General::Logging::log_user_message("No need to login since no own registry exists");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        if ($registry_user eq "CHANGEME") {
            General::Logging::log_user_warning_message("No user specified for own registry, cannot do '$container_cmd login'");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        if ($registry_password eq "CHANGEME") {
            General::Logging::log_user_warning_message("No password specified for own registry, cannot do '$container_cmd login'");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        if ($container_cmd =~ /nerdctl/ && $registry_port == 443) {
            # We need to remove the port number because it seems the nerdctl command to push images does not like to have :443 included in the url
            $url = $registry_url;
        } elsif ($container_cmd =~ /docker/ && $registry_port == 443) {
            # We need to remove the port number because it seems the docker command to push images does not like to have :443 included in the url on some nodes.
            $url = $registry_url;
        }

        General::Logging::log_user_message("Performing '$container_cmd login'");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$container_cmd login --username $registry_user --password '$registry_password' $url",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) $container_cmd login successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to perform '$container_cmd login'");
            push @::JOB_STATUS, "(x) $container_cmd login failed";
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
    #/
    # -----------------------------------------------------------------------------
    sub Load_DSCLOAD_P109S03T05 {

        return load_tag_push_docker_image("dscload", $::JOB_PARAMS{'TOOL_FILE_DSCLOAD_DOCKER'});
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
    sub Unpack_DSCLOAD_File_And_Change_Values_File_P109S03T06 {

        my $diameter_tps = 1000;
        my $dualstack = "false";
        my $found = 0;
        my $ip = "";
        my $ip_version = 4;
        my $prefix;
        my $rc = 0;
        my @result;
        my $singlepeer = "true";

        # covers the settings for ipv6 only clusters
        if (exists $::JOB_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_BSF_Diameter'} &&
            $::JOB_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_BSF_Diameter'} !~ /CHANGEME/) {
            $ip = $::JOB_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_BSF_Diameter'};
        } elsif (exists $::JOB_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_Diameter'} &&
            $::JOB_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_Diameter'} !~ /CHANGEME/) {
            $ip = $::JOB_PARAMS{'eric_sc_values_anchor_parameter_VIP_SIG_Diameter'};
        } else {
            General::Logging::log_user_error_message("Unable to find the 'eric_sc_values_anchor_parameter_VIP_SIG_BSF_Diameter' or 'eric_sc_values_anchor_parameter_VIP_SIG_Diameter' value in the network config file or in the job variables.");
            return 1;
        }

        $ip =~ s/'//g;
        $ip = return_one_ip_address($ip, $::JOB_PARAMS{'PREFER_IP_VERSION'}, "no");
        if ($ip =~ /:/) {
            $ip_version = 6;
        }

        if ($ip eq "") {
            General::Logging::log_user_error_message("Unable to find the 'eric_sc_values_anchor_parameter_VIP_SIG_BSF_Diameter' or 'eric_sc_values_anchor_parameter_VIP_SIG_Diameter' value in the network config file or in the job variables.");
            return 1;
        }

        if ($::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} ne "") {
            General::Logging::log_user_message("Using IP version $ip_version with IP $ip with following NETWORK_CONFIG_OPTIONS $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'}");
        } else {
            General::Logging::log_user_message("Using IP version $ip_version with IP $ip");
        }

        General::Logging::log_user_message("Unpacking the dscload helm chart files");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "tar xf $::JOB_PARAMS{'TOOL_FILE_DSCLOAD_HELM'} -C $::JOB_PARAMS{'_JOB_CONFIG_DIR'}",
                "hide-output"   => 1,
             }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to unpack the file");
            return 1;
        }

        General::Logging::log_user_message("Reading the values.yaml file");
        $rc = General::File_Operations::read_file(
            {
                "filename"            => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-dscload/values.yaml",
                "output-ref"          => \@result,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to read the values.yaml file");
            return 1;
        }
        for (@result) {
            if (/^configuration:\s*$/) {
                $found++;
            } elsif (/^(\s+af-diameter-realm:).*/) {
                $prefix = $1;
                s/^.+/$prefix "af-diamrealm"/;
                $found++;
            } elsif (/^(\s+pcf-diameter-host:).*/) {
                $prefix = $1;
                s/^.+/$prefix "pcf-diamhost.com"/;
                $found++;
            } elsif (/^(\s+pcf-diameter-realm:).*/) {
                $prefix = $1;
                s/^.+/$prefix "pcf-diamrealm.com"/;
                $found++;
            } elsif (/^(\s+diameter-service-ip:).*/) {
                $prefix = $1;
                s/^.+/$prefix "$ip"/;
                $found++;
            } elsif (/^(\s+diameter-service-port:).*/) {
                $prefix = $1;
                s/^.+/$prefix "3868"/;
                $found++;
            } elsif (/^(\s+diameter-service-name:).*/) {
                $prefix = $1;
                s/^.+/$prefix "eric-stm-diameter-traffic-tcp"/;
                $found++;
            } elsif (/^(\s+diameter-tps:).*/) {
                $prefix = $1;
                s/^.+/$prefix $diameter_tps/;
                $found++;
            } elsif (/^(\s+dualstack:).*/) {
                $prefix = $1;
                s/^.+/$prefix $dualstack/;
                $found++;
            } elsif (/^(\s+ip-version:).*/) {
                $prefix = $1;
                s/^.+/$prefix $ip_version/;
                $found++;
            } elsif (/^(\s+singlePeer:).*/) {
                $prefix = $1;
                s/^.+/$prefix $singlepeer/;
                $found++;
            }
        }
        if ($found == 0) {
            # No configuration section found, add missing data to the end
            push @result, (
                "configuration:",
                "  af-diameter-realm: \"af-diamrealm\"",
                "  pcf-diameter-host: \"pcf-diamhost.com\"",
                "  pcf-diameter-realm: \"pcf-diamrealm.com\"",
                "  diameter-service-ip: \"$ip\"",
                "  diameter-service-port: \"3868\"",
                "  diameter-service-name: \"eric-stm-diameter-traffic-tcp\"",
                "  diameter-tps: $diameter_tps",
                "  dualstack: $dualstack",
                "  ip-version: $ip_version",
                "  singlePeer: $singlepeer"
            );
        } elsif ($found < 8) {
            General::Logging::log_user_error_message("Did not find the expected data in the values.yaml file");
            return 1;
        }

        General::Logging::log_user_message("Updating the values.yaml file");
        $rc = General::File_Operations::write_file(
            {
                "filename"            => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-dscload/values.yaml",
                "output-ref"          => \@result,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to write the values.yaml file");
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
    sub Install_DSCLOAD_P109S03T07 {

        return install_tool("dscload", "eric-sc-dscload", "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-dscload/", "eric-dscload-deployment");
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
    sub Load_K6_P109S03T08 {

        return load_tag_push_docker_image("k6", $::JOB_PARAMS{'TOOL_FILE_K6_DOCKER'});
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
    sub Install_K6_P109S03T09 {

        return install_tool("k6", "eric-sc-k6", "$::JOB_PARAMS{'TOOL_FILE_K6_HELM'}", "eric-k6-deployment");
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
    sub Copy_Certificates_To_K6_Pods_P109S03T10 {

        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
            return 0;
        }
        my $copy_success;
        my $failures = 0;
        my $namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
        my @pods = ADP::Kubernetes_Operations::get_pod_names(
            {
                "namespace"         => $namespace,
                "kubeconfig"        => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
                "hide-output"       => 1,
                "pod-include-list"  => [ "eric-k6-deployment-.+" ],
            }
        );
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Checking every 5 seconds that all K6 pods in $namespace namespace are up for a maximum of 60 seconds.");
        $rc = ADP::Kubernetes_Operations::check_pod_status(
            {
                "all-namespaces"                            => 0,
                "debug-messages"                            => 0,
                "delay-time"                                => 5,
                "ignore-ready-check-for-completed-status"   => 1,
                "hide-output"                               => 1,
                "max-attempts"                              => 0,
                "max-time"                                  => 60,
                "namespace"                                 => $namespace,
                "pod-include-list"                          => \@pods,
                "repeated-checks"                           => 0,
                "return-failed-pods"                        => \@result,
                "wanted-ready"                              => "same",
                "wanted-status"                             => "up",
            }
        );
        if ($rc != 0) {
            my $message = join("\n",@result);
            General::Logging::log_user_error_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n$message");
        }

        for my $pod (@pods) {
            $copy_success = 1;
            General::Logging::log_user_message("Copying K6.crt file to pod $pod");
            # NOTE: The 'kubectl cp' seems to return $rc as 0 even if the 'from' file does not exist.
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}kubectl -n $namespace cp $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/cert.pem $pod:/certs/K6.crt" . ($::JOB_PARAMS{'KUBECONFIG_TOOLS'} ne "" ? " --kubeconfig=$::JOB_PARAMS{'KUBECONFIG_TOOLS'}" : ""),
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy K6.crt to pod $pod");
                $failures++;
                $copy_success = 0;
            }
            General::Logging::log_user_message("Copying K6.key file to pod $pod");
            # NOTE: The 'kubectl cp' seems to return $rc as 0 even if the 'from' file does not exist.
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}kubectl -n $namespace cp $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/key.pem $pod:/certs/K6.key" . ($::JOB_PARAMS{'KUBECONFIG_TOOLS'} ne "" ? " --kubeconfig=$::JOB_PARAMS{'KUBECONFIG_TOOLS'}" : ""),
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy K6.crt to pod $pod");
                $failures++;
                $copy_success = 0;
            }

            if ($::JOB_PARAMS{'SEPPSIM_WANTED'} eq "yes" && $::JOB_PARAMS{'K6_WANTED'} eq "yes") {
                General::Logging::log_user_message("Copying RP_206033.key file to pod $pod");
                # NOTE: The 'kubectl cp' seems to return $rc as 0 even if the 'from' file does not exist.
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}kubectl -n $namespace cp $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033.key $pod:/certs/RP_206033.key" . ($::JOB_PARAMS{'KUBECONFIG_TOOLS'} ne "" ? " --kubeconfig=$::JOB_PARAMS{'KUBECONFIG_TOOLS'}" : ""),
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to copy RP_206033.key to pod $pod");
                    $failures++;
                    $copy_success = 0;
                }

                General::Logging::log_user_message("Copying RP_206033.crt file to pod $pod");
                # NOTE: The 'kubectl cp' seems to return $rc as 0 even if the 'from' file does not exist.
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}kubectl -n $namespace cp $::JOB_PARAMS{'CERTIFICATE_DIRECTORY'}/k6/rp206033/RP_206033.crt $pod:/certs/RP_206033.crt" . ($::JOB_PARAMS{'KUBECONFIG_TOOLS'} ne "" ? " --kubeconfig=$::JOB_PARAMS{'KUBECONFIG_TOOLS'}" : ""),
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to copy RP_206033.crt to pod $pod");
                    $failures++;
                    $copy_success = 0;
                }
            }

            if ($copy_success == 1) {
                push @::JOB_STATUS, "(/) K6 certificate copy to pod $pod was successful";
            } else {
                push @::JOB_STATUS, "(x) K6 certificate copy to pod $pod failed";
            }
        }

        if ($failures == 0) {
            return 0;
        } else {
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
    sub Load_NRFSIM_P109S03T11 {

        return load_tag_push_docker_image("nrfsim", $::JOB_PARAMS{'TOOL_FILE_NRFSIM_DOCKER'});
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
    sub Install_NRFSIM_P109S03T12 {

        return install_tool("nrfsim", "eric-sc-nrfsim", "$::JOB_PARAMS{'TOOL_FILE_NRFSIM_HELM'}", "eric-nrfsim");
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
    sub Load_SEPPSIM_P109S03T13 {

        return load_tag_push_docker_image("seppsim", $::JOB_PARAMS{'TOOL_FILE_SEPPSIM_DOCKER'});
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
    sub Install_SEPPSIM_P109S03T14 {

        return install_tool("seppsim", "eric-sc-seppsim", "$::JOB_PARAMS{'TOOL_FILE_SEPPSIM_HELM'}", 'eric-seppsim-\S+');
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
    sub Load_SFTP_P109S03T15 {

        return load_tag_push_docker_image("sftp", $::JOB_PARAMS{'TOOL_FILE_SFTP_DOCKER'});
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
    sub Install_SFTP_P109S03T16 {

        return install_tool("sftp", "eric-sc-sftp", "$::JOB_PARAMS{'TOOL_FILE_SFTP_HELM'}", "eric-atmoz-sftp");
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
    sub Install_SC_MONITOR_P109S03T17 {

        # Print a tool message box
        my $message = "# sc-monitor #";
        my $length = length($message);
        General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

        my $rc = install_tool("sc_monitor", "eric-sc-monitor", "$::JOB_PARAMS{'TOOL_DIRECTORY_SC_MONITOR_HELM'}", "eric-sc-monitor");
        return $rc if ($rc != 0);

        # To be able to work in a two cluster setup where tools and SC are deployed in different nodes
        # we need to change the type from ClusterIP to NodePort using 'kubectl patch' on the service
        # since it does not work with 'helm ... --set spec.type=NodePort ...' command.
        General::Logging::log_user_message("Patching service eric-sc-monitor to use NodePort instead of ClusterIP");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} patch service eric-sc-monitor -p '{\"spec\":{\"type\":\"NodePort\"}}'",
                "hide-output"   => 1,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to patch service eric-sc-monitor");
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
    sub Load_CHFSIM_P109S03T18 {

        return load_tag_push_docker_image("chfsim", $::JOB_PARAMS{'TOOL_FILE_CHFSIM_DOCKER'});
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
    sub Install_CHFSIM_P109S03T19 {

        return install_tool("chfsim", "eric-sc-chfsim", "$::JOB_PARAMS{'TOOL_FILE_CHFSIM_HELM'}", 'eric-chfsim-\S+');
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
    sub Check_Pod_Status_P109S03T20 {

        my $delay_time = 10;
        my $max_attempts = 0;
        my $max_time = $::JOB_PARAMS{'POD_STATUS_TIMEOUT'};
        my $repeated_checks = 0;
        my $message = "";
        my $rc = 0;
        my @result;
        my $namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};

        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
            return 0;
        }
        if ($max_time > 0) {
            General::Logging::log_user_message("Checking every 10 seconds that all pods in $namespace namespace are up for a maximum of $max_time seconds.");
        } else {
            General::Logging::log_user_message("Checking that all pods in $namespace namespace are up.");
        }
        $rc = ADP::Kubernetes_Operations::check_pod_status(
            {
                "all-namespaces"                            => 0,
                "debug-messages"                            => 0,
                "delay-time"                                => $delay_time,
                "ignore-ready-check-for-completed-status"   => 1,
                "hide-output"                               => 1,
                "kubeconfig"                                => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
                "max-attempts"                              => $max_attempts,
                "max-time"                                  => $max_time,
                "namespace"                                 => $namespace,
                "repeated-checks"                           => $repeated_checks,
                "return-failed-pods"                        => \@result,
                "wanted-ready"                              => "same",
                "wanted-status"                             => "up",
            }
        );
        if ($rc == 0) {
            General::Logging::log_user_message("POD status check was successful");
        } else {
            $message = join("\n",@result);
            General::Logging::log_user_error_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n$message");
        }

        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Post $::JOB_PARAMS{'JOBTYPE'} POD status check was successful";
        } else {
            push @::JOB_STATUS, "(x) Post $::JOB_PARAMS{'JOBTYPE'} POD status check failed";
            $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
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
sub Collect_ADP_Logs_P109S04 {

    my $rc = 0;

    if ($::JOB_PARAMS{'COLLECT_LOGS_AT_SUCCESS'} eq "yes" && $::JOB_PARAMS{'NAMESPACE_CREATED'} eq "yes") {
        # Since the 914 playlist only collects logs from SC_NAMESPACE
        my $old_sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        $::JOB_PARAMS{'SC_NAMESPACE'} = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
        # We don't want to execute these tasks
        General::State_Machine::always_skip_task("Playlist::914_Collect_Logs::.+P914S02T\d\d");
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
sub Cleanup_Job_Environment_P109S05 {

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
sub Fallback001_P109S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    # Set Job Type again since it was changed inside the 102 playlist
    $::JOB_PARAMS{'JOBTYPE'} = "TRAFFIC_TOOLS_INSTALL";

    if ($::JOB_PARAMS{'NAMESPACE_CREATED'} eq "yes") {
        # Since the 914 playlist only collects logs from SC_NAMESPACE
        my $old_sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        $::JOB_PARAMS{'SC_NAMESPACE'} = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
        # We don't want to execute these tasks
        General::State_Machine::always_skip_task("Playlist::914_Collect_Logs::.+P914S02T\d\d");
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
        $::JOB_PARAMS{'SC_NAMESPACE'} = $old_sc_namespace;
        return $rc if $rc < 0;
    }

    # Now we need to execute the cleanup.
    General::State_Machine::remove_always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

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
sub install_tool {
    my $tool = shift;
    my $chart_name = shift;
    my $chart_path = shift;
    my $deployment_name = shift;

    my $rc = 0;
    my $special_set_options = "";

    # Special settings per tool
    if ($tool eq "chfsim") {
        $special_set_options = $::JOB_PARAMS{'TOOLS_ON_SEPARATE_WORKERS'} eq "yes" ? "--set usage=tools " : "";
    } elsif ($tool eq "dscload") {
        $special_set_options = $::JOB_PARAMS{'TOOLS_ON_SEPARATE_WORKERS'} eq "yes" ? "--set usage=tools " : "";
        $special_set_options .= $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} eq "IntIPv6ExtIPv6" ? "--set configuration.ip-version=6 " : "";
        $special_set_options .= "--set configuration.tls.enabled=false ";
        $special_set_options .= "--set configuration.dualstack=false ";
    } elsif ($tool eq "k6") {
        $special_set_options = $::JOB_PARAMS{'TOOLS_ON_SEPARATE_WORKERS'} eq "yes" ? "--set usage=tools " : "";
    } elsif ($tool eq "nrfsim") {
        $special_set_options = $::JOB_PARAMS{'TOOLS_ON_SEPARATE_WORKERS'} eq "yes" ? "--set usage=tools " : "";
        $special_set_options .= $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} eq "IntIPv6ExtIPv6" ? "--set service.internalIPFamily[0]=IPv6 " : "";
    } elsif ($tool eq "sc_monitor") {
        if ($::JOB_PARAMS{'TOOLS_ON_SEPARATE_WORKERS'} eq "yes") {
            $special_set_options =  "--set nodeSelector.usage=tools ";
            $special_set_options .= "--set tolerations.monitor[0].key=usage,tolerations.monitor[0].operator=Equal,tolerations.monitor[0].value=tools,tolerations.monitor[0].effect=NoSchedule ";
        }
    } elsif ($tool eq "seppsim") {
        $special_set_options = $::JOB_PARAMS{'TOOLS_ON_SEPARATE_WORKERS'} eq "yes" ? "--set usage=tools " : "";
        $special_set_options .= $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} eq "IntIPv6ExtIPv6" ? " --set service.internalIPFamily[0]=IPv6 " : "";
    } elsif ($tool eq "sftp") {
        if ($::JOB_PARAMS{'TOOLS_ON_SEPARATE_WORKERS'} eq "yes") {
            $special_set_options =  "--set nodeSelector.usage=tools ";
            $special_set_options .= "--set tolerations.sftp[0].key=usage,tolerations.sftp[0].operator=Equal,tolerations.sftp[0].value=tools,tolerations.sftp[0].effect=NoSchedule ";
        }
    }

    for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
        if ($key =~ /^eric_${tool}_values_hash_parameter_(\S+)$/) {
            next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} eq "CHANGEME");
            $special_set_options .= "--set $1=$::NETWORK_CONFIG_PARAMS{$key}{'value'} ";
        }
    }

    General::Logging::log_user_message("Installing $tool");
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "${debug_command}$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} install $special_set_options -n $::JOB_PARAMS{'TOOLS_NAMESPACE'} $chart_name $chart_path --debug",
            "hide-output"   => 1,
            }
    );
    if ($rc == 0) {
        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
            return 0;
        }
        my @result;
        General::Logging::log_user_message("Checking every 5 seconds that $tool deployment in $::JOB_PARAMS{'TOOLS_NAMESPACE'} namespace comes up for a maximum of $::JOB_PARAMS{'POD_STATUS_TIMEOUT'} seconds.");
        $rc = ADP::Kubernetes_Operations::check_deployment_status(
            {
                "delay-time"                                => 5,
                "deployment-name"                           => $deployment_name,
                "hide-output"                               => 1,
                "kubeconfig"                                => $::JOB_PARAMS{'KUBECONFIG_TOOLS'},
                "max-time"                                  => $::JOB_PARAMS{'POD_STATUS_TIMEOUT'},
                "namespace"                                 => $::JOB_PARAMS{'TOOLS_NAMESPACE'},
                "return-failed-pods"                        => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) $tool installation was successful";
        } else {
            my $message = join("\n",@result);
            General::Logging::log_user_error_message("Deployment for $tool did not reach expected status:\n$message");
        }
    } else {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_error_message("Failed to install $tool");
        return 1;
    }
    return $rc;
}

# -----------------------------------------------------------------------------
sub load_tag_push_docker_image {
    my $tool = shift;
    my $filename = shift;
    my $path = "";
    my $rc = 0;
    my $repository = "";
    my @result;

    # Print a tool message box
    my $message = "# $tool #";
    my $length = length($message);
    General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

    #
    # Load docker image
    #
    General::Logging::log_user_message("Loading the $tool docker image");
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "${debug_command}$::JOB_PARAMS{'CONTAINER_COMMAND'} load -i $filename",
            "hide-output"   => 1,
            "return-output" => \@result,
            }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_error_message("Failed to load $tool docker image");
        return 1;
    }
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "no") {
        for (@result) {
            if (/^Loaded image: (\S+?)(\/\S+)/) {
                # Loaded image: armdocker.rnd.ericsson.se/proj-5g-bsf/eiffelesc/eric-k6:1.13.25-1134
                $repository = $1;
                $path = $2;
            }
        }
        if ($repository eq "" || $path eq "") {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to find the 'Loaded image:' line when loading $tool docker image");
            return 1;
        }
    } else {
        $repository = "fake_repository";
        $path = "fake_path";
    }

    #
    # Tag docker image
    #
    my $url = "$::JOB_PARAMS{'TOOLS_REGISTRY_URL'}:$::JOB_PARAMS{'TOOLS_REGISTRY_PORT'}";
    if ($::JOB_PARAMS{'CONTAINER_COMMAND'} =~ /nerdctl/ && $::JOB_PARAMS{'TOOLS_REGISTRY_PORT'} == 443) {
        # We need to remove the port number because it seems the nerdctl command to push images does not like to have :443 included in the url
        $url = $::JOB_PARAMS{'TOOLS_REGISTRY_URL'};
    } elsif ($::JOB_PARAMS{'CONTAINER_COMMAND'} =~ /docker/ && $::JOB_PARAMS{'TOOLS_REGISTRY_PORT'} == 443) {
        # We need to remove the port number because it seems the docker command to push images does not like to have :443 included in the url
        $url = $::JOB_PARAMS{'TOOLS_REGISTRY_URL'};
    }

    General::Logging::log_user_message("Tagging the $tool docker image");
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "${debug_command}$::JOB_PARAMS{'CONTAINER_COMMAND'} tag $repository$path $url$path",
            "hide-output"   => 1,
            "return-output" => \@result,
            }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_error_message("Failed to tag $tool docker image");
        return 1;
    }

    #
    # Push docker image
    #
    General::Logging::log_user_message("Pushing the $tool docker image");
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "${debug_command}$::JOB_PARAMS{'CONTAINER_COMMAND'} push $url$path",
            "hide-output"   => 1,
            "return-output" => \@result,
            }
    );
    if ($rc != 0) {
        # Display the result in case of error
        General::OS_Operations::write_last_temporary_output_to_progress();

        General::Logging::log_user_error_message("Failed to load $tool docker image");
        return 1;
    }

    return $rc;
}

# -----------------------------------------------------------------------------
# This subroutine takes a variable value containing one or more IP-addresses
# separated by a comma and returns the first address of the wanted type and
# adding if wanted [ ] surrounding an IPv6 address.
#
sub return_one_ip_address {
    my $var_value = shift;  # One or more IP-addresses
                            # For example: 10.117.41.213,2001:1b74:20:c003::0213
                            #              10.117.41.213
                            #              2001:1b74:20:c003::0213
    my $prefer = shift;     # (IPV4|IPV6)
    my $add_brakets = shift;# (no|yes) Add [ ] around IPv6 address

    my $quote = "";
    if ($var_value =~ /^(['"])(.*)['"]$/) {
        # The value is surrounded by single (') or double (") quotes
        $quote = $1;
        $var_value = $2;
    }
    my @ipv4 = ();
    my @ipv6 = ();
    my @ip = split /,/, $var_value;
    for (@ip) {
        if (/:/) {
            push @ipv6, $_;
        } else {
            push @ipv4, $_;
        }
    }
    if (@ipv4 && @ipv6) {
        # We have both ipv4 and ipv6 addresses, we need to pick one of them
        if ($prefer =~ /^IPV4$/i) {
            $var_value = sprintf "%s%s%s", $quote, $ipv4[0], $quote;
        } else {
            if ($add_brakets =~ /^(no|0)$/i) {
                $var_value = sprintf "%s%s%s", $quote, $ipv6[0], $quote;
            } else {
                $var_value = sprintf "%s[%s]%s", $quote, $ipv6[0], $quote;
            }
        }
    } elsif (@ipv4) {
        $var_value = sprintf "%s%s%s", $quote, $ipv4[0], $quote;
    } elsif (@ipv6) {
        if ($add_brakets =~ /^(no|0)$/i) {
            $var_value = sprintf "%s%s%s", $quote, $ipv6[0], $quote;
        } else {
            $var_value = sprintf "%s[%s]%s", $quote, $ipv6[0], $quote;
        }
    }

    return $var_value;
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
        'COLLECT_LOGS_AT_SUCCESS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if detailed log files should be collected at successful
job termination.
Normally detailed logs are only collected when the job fails to aid with trouble
shooting efforts.

If value is 'yes' when the playlist finish with an success then detailed ADP
logs will be collected and stored in the all.log or under the troubleshooting_logs
sub directory of the job.
The default value is 'no' if the variable is not specified.

If value is 'no' then no collection of detailed logs will be done at successful
job termination.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'GIT_DIRECTORY' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "directory", # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the top-level directory of the '5g_proto' GIT directory
that must contain the following sub-directories:

    - daft/
    - devtools/k6/
    - simulators/dscload/
    - simulators/seppsim/
    - esc/

If this parameter is not specified then the current directory where the playlist
is executed from must be inside the GIT directory tree.
If neither is the case then the playlist will fail.
EOF
            'validity_mask' => '.+',
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
        'NODE_CERTIFICATE_DIRECTORY' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "/home/eccd/certificates",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls the directory on the node where the certificates are
stored.
This directory is used for copying the certificates from the node to the
workspace directory for creating tools certificates and then the newly
created certificates are then copied back to the node.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'POD_STATUS_TIMEOUT' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "300",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the timeout value to use when checking that all PODs are up and running
after deploying the software, if not specified then the default value will be
used.

The time value which must be an integer value is given as the number of seconds
to wait for the pods to have expected status.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'PREFER_IP_VERSION' => {
            'case_check'    => "uppercase", # <lowercase|no|uppercase>
            'default_value' => "IPV4",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which IP-address is used when both IPv4 and IPv6 address
is used in the node.
EOF
            'validity_mask' => '(IPV4|IPV6)',
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
        'SKIP_LOADING_SC_MONITOR_IMAGE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if loading of docker image for sc-monitor should be skipped.

If value is 'yes' then the sc-monitor image has already been loaded into docker
or containerd and thus this step can be skipped.
This can e.g. be used when the SC and Tools software are installed on the same
cluster and the SC software has already been installed.
If set to 'yes' then the playlist Playlist::926_Handle_Docker_Images::main task
is skipped, which will speed up the deployment of the tools because duplicated
loading of the same images can be skipped.

If value is 'no' then the sc-monitor and all other SC images will be loaded
by calling the playlist Playlist::926_Handle_Docker_Images::main task.
This might be needed if the SC and tools software is installed on different
nodes and the SC software has not previosly been loaded into the node where
the tools will be installed.

This parameter only has any effect when the TOOLS_TO_INSTALL contains the
string 'sc-monitor', otherwise the value of this parameter has no effect.

The default value is 'no' if the variable is not specified.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_TOOL_INSTALLATION' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if installation of the tools should be skipped.

If value is 'yes' then the tools is assumed to be already installed on the cluster
and the tasks that installs the tools will be skipped and only tasks that loads
secrets or performs other tasks will be done.
I.e. all tasks that handles docker images or deploys the tools with helm command
will be skipped.

If value is 'no' then tools will be be installed even if they are already
installed which might cause failures in this case.
The default value is 'no' if the variable is not specified.
EOF
            'validity_mask' => '(yes|no)',
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

It also should include the traffic tools that will be installed by this Playlist
inside of the following sub-directories:
    - base_stability_traffic/traffic_simulators/docker/
    - base_stability_traffic/traffic_simulators/helm/
    - base_stability_traffic/traffic_simulators/versions/

If also could include the certificates to use instead of creating new certificates
and if so should be inside the following sub-directories:
    - certificates/
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
This parameter controls in which namespace the tools will be created on the node
and should be specified if you want to override the parameter from the network
configuration file called 'tools_namespace'.
If you don't specify this parameter then it will take the value from the network
configuration file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TOOLS_ON_SEPARATE_WORKERS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the tools should be deployed on separate workers
to e.g. avoid disturbances from affecting the tool pods.
If the value is 'yes' then when installing the pod's with the helm install
command then an extra parameter '--set usage=tools' will be appended to the
command which then set special nodeSelector and tolerations for the pod's
which will force the deployment of the pod's to special worker nodes.
If the value is 'no' then the pod's will be placed on whatever worker node
has enough space for the pod.

NOTE: To properly use the value 'yes' you MUST also have called the playlist
103_Tools_Management.pm that will set the proper labels and taints on the worker
nodes.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TOOLS_TO_INSTALL' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which tools to install on the node and it will be used
for building a new tools package and then for installing the tools on the node
in the namespace specified by TOOLS_NAMESPACE.

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

This Playlist builds and installs the dscload, k6, nrfsim and seppsim traffic
generators tools and atmoz-sftp server on the IaaS clusters in preparation for
traffic setup.

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
    my %temp_hash = ( %Playlist::102_Supreme_Certificate_Management::playlist_variables, %playlist_variables );

    General::Playlist_Operations::print_info_about_job_variables(\%temp_hash);

    $message = "# Global variable access in playlist $playlist_name #";
    $length = length($message);
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables(__FILE__, \%playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);

    no strict;
    for $playlist_name ("102_Supreme_Certificate_Management") {
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
    return ( %Playlist::102_Supreme_Certificate_Management::playlist_variables, %playlist_variables );
}

1;
