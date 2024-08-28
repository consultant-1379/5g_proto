package Playlist::204_EVNFM_Upgrade_SC;

################################################################################
#
#  Author   : everhel, eustone
#
#  Revision : 1.26
#  Date     : 2024-03-26 15:42:49
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

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    parse_network_config_parameters
    usage
    usage_return_playlist_variables
    );

use ADP::EVNFM_Operations;
use ADP::Kubernetes_Operations;
use General::File_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::903_Global_Preprocess;
use Playlist::904_Global_Postprocess;
use Playlist::907_Upgrade_Preprocess;
use Playlist::908_Upgrade_Postprocess;
use Playlist::914_Collect_Logs;
use Playlist::920_Check_Network_Config_Information;
use Playlist::922_Handle_Software_Directory_Files;
use Playlist::924_Prepare_Deploy_Upgrade;
use Playlist::927_Deploy_Upgrade_CRDS;
use Playlist::929_Handle_Secrets;
use Playlist::931_Check_Resource_Usage;
use Playlist::933_Handle_KPI_Statistics;
use Playlist::934_Prepare_Value_Files;
use Playlist::936_Check_Deployed_Software;

#
# Variable Declarations
#
my %playlist_variables;
set_playlist_variables();

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
    $::JOB_PARAMS{'JOBTYPE'} = "UPGRADE";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # If the playlist needs to have access to Network Configuration parameters
    # then it needs to check for this and give an error or ask the user to
    # provide the file name.
    if (Playlist::920_Check_Network_Config_Information::is_network_config_specified() != 0) {
        General::Logging::log_user_error_message("You must specify a network configuration file for this Playlist to run");
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    # Read package specific parameters in file 'package.config' and store as $::JOB_PARAMS
    if (-f "$::JOB_PARAMS{'SOFTWARE_DIR'}/package.config") {
        # For example:
        # commercial_release_name=1.9
        # internal_release_name=NDP193_PA2
        General::Playlist_Operations::job_parameters_read("$::JOB_PARAMS{'SOFTWARE_DIR'}/package.config", \%::JOB_PARAMS);
    }

    # Set tasks to always execute
    General::State_Machine::always_execute_task("Playlist::909_Execute_Playlist_Extensions.+");

    # Hard code some variables that does not need to be set by the user but required to be set for some playlists
    $::JOB_PARAMS{'USING_EVNFM'} = "yes";

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P204S01, \&Fallback001_P204S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P204S02, \&Fallback001_P204S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Configuration_Files_If_Specified_P204S03, \&Fallback001_P204S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&KPI_Entry_Collection_P204S04, \&Fallback001_P204S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Global_Preprocessing_P204S05, \&Fallback001_P204S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Collect_Logs_Before_Upgrade_If_Wanted_P204S06, \&Fallback001_P204S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Upgrade_Preprocessing_P204S07, \&Fallback001_P204S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Upgrade_SC_Via_EVNFM_P204S08, \&Fallback001_P204S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Upgrade_Postprocessing_P204S09, \&Fallback001_P204S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Global_Postprocessing_P204S10, \&Fallback001_P204S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Collect_Logs_P204S11, \&Fallback001_P204S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&KPI_Upgrade_And_Exit_Collection_P204S12, \&Fallback001_P204S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P204S13, \&Fallback001_P204S99 );
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
sub Initialize_Job_Environment_P204S01 {

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
sub Check_Job_Parameters_P204S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_Part1_P204S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::936_Check_Deployed_Software::main } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'CHECK_CSAR_FILE'}            = "yes";
    $::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'}    = "yes";
    $::JOB_PARAMS{'CHECK_TOOLS_FILE'}           = "no";
    $::JOB_PARAMS{'CHECK_TOOLS_FILE_CONTENT'}   = "no";
    $::JOB_PARAMS{'CHECK_VALUES_FILE'}          = "yes";
    $::JOB_PARAMS{'COPY_UMBRELLA_FILE'}         = "yes";
    $::JOB_PARAMS{'COPY_VALUES_FILE'}           = "yes";
    $::JOB_PARAMS{'UNPACK_CSAR_FILE'}           = "no";
    $::JOB_PARAMS{'UNPACK_TOOLS_FILE'}          = "no";
    $::JOB_PARAMS{'UNPACK_UMBRELLA_FILE'}       = "yes";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::922_Handle_Software_Directory_Files::main } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'ENABLED_CNF'} eq "all" || $::JOB_PARAMS{'ENABLED_CNF'} eq "") {
        $::JOB_PARAMS{'ENABLED_CNF'} = join " ", ADP::Kubernetes_Operations::get_list_of_known_cnf_types("sc", $::JOB_PARAMS{'SC_RELEASE_VERSION'});
    }

    $::JOB_PARAMS{'P920_TASK'} = "READ_PARAMETERS_FROM_FILE";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::920_Check_Network_Config_Information::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_Part2_P204S02T02 } );
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
    sub Check_Job_Parameters_Part1_P204S02T01 {

        my $crd_namespace = "";
        my $crd_release_name = "";
        my $sc_namespace = "";
        my $sc_release_name = "";
        my $rc = 0;

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

        General::Logging::log_user_message("SC_NAMESPACE=$sc_namespace\nSC_RELEASE_NAME=$sc_release_name\nCRD_NAMESPACE=$crd_namespace\nCRD_RELEASE_NAME=$crd_release_name\n");

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
    sub Check_Job_Parameters_Part2_P204S02T02 {

        my $clustername = "";
        my $ingress_host_address = "";
        my $password = "";
        my $rc = 0;
        my $username = "";

        # Get the proper CLUSTERNAME and CONFIG_FILE_CHANGE_VNF_PACKAGE
        General::Logging::log_user_message("Checking Job parameter 'CLUSTERNAME' and 'CONFIG_FILE_CHANGE_VNF_PACKAGE' and Network Config parameter 'evnfm_clustername'");
        if ($::JOB_PARAMS{'CLUSTERNAME'} ne "") {
            $clustername = $::JOB_PARAMS{'CLUSTERNAME'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'CLUSTERNAME'} = $::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'};
                $clustername = $::JOB_PARAMS{'CLUSTERNAME'};
            } elsif ($::JOB_PARAMS{'CONFIG_FILE_CHANGE_VNF_PACKAGE'} eq "") {
                General::Logging::log_user_error_message("Network Config parameter 'evnfm_clustername' has not been set and Job parameter 'CLUSTERNAME' or 'CONFIG_FILE_CHANGE_VNF_PACKAGE' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter'CLUSTERNAME' nor 'CONFIG_FILE_CHANGE_VNF_PACKAGE' or Network Config parameter 'evnfm_clustername' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper INGRESS_HOST_ADDRESS
        General::Logging::log_user_message("Checking Job parameter 'INGRESS_HOST_ADDRESS' and Network Config parameter 'evnfm_ingress_host_address'");
        if ($::JOB_PARAMS{'INGRESS_HOST_ADDRESS'} ne "") {
            $ingress_host_address = $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'evnfm_ingress_host_address'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'evnfm_ingress_host_address'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'} = $::NETWORK_CONFIG_PARAMS{'evnfm_ingress_host_address'}{'value'};
                $ingress_host_address = $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'evnfm_ingress_host_address' has not been set and Job parameter 'INGRESS_HOST_ADDRESS' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter'INGRESS_HOST_ADDRESS' nor Network Config parameter 'evnfm_ingress_host_address' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper PASSWORD
        General::Logging::log_user_message("Checking Job parameter 'PASSWORD' and Network Config parameter 'evnfm_password'");
        if ($::JOB_PARAMS{'PASSWORD'} ne "") {
            $password = $::JOB_PARAMS{'PASSWORD'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'evnfm_password'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'evnfm_password'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'PASSWORD'} = $::NETWORK_CONFIG_PARAMS{'evnfm_password'}{'value'};
                $password = $::JOB_PARAMS{'PASSWORD'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'evnfm_password' has not been set and Job parameter 'PASSWORD' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter'PASSWORD' nor Network Config parameter 'evnfm_password' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper USERNAME
        General::Logging::log_user_message("Checking Job parameter 'USERNAME' and Network Config parameter 'evnfm_username'");
        if ($::JOB_PARAMS{'USERNAME'} ne "") {
            $username = $::JOB_PARAMS{'USERNAME'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'evnfm_username'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'evnfm_username'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'USERNAME'} = $::NETWORK_CONFIG_PARAMS{'evnfm_username'}{'value'};
                $username = $::JOB_PARAMS{'USERNAME'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'evnfm_username' has not been set and Job parameter 'USERNAME' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter'USERNAME' nor Network Config parameter 'evnfm_username' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        General::Logging::log_user_message(
            sprintf "%s%s%s%s",
                $clustername ne "" ? "CLUSTERNAME=$clustername\n" : "",
                $::JOB_PARAMS{'CONFIG_FILE_CHANGE_VNF_PACKAGE'} ne "" ? $::JOB_PARAMS{'CONFIG_FILE_CHANGE_VNF_PACKAGE'} : "",
                "INGRESS_HOST_ADDRESS=$ingress_host_address\n",
                "USERNAME=$username\n"
        );

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
sub Check_Configuration_Files_If_Specified_P204S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::934_Prepare_Value_Files::main } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_RESOURCE_CHECK'} eq "no") {

        # Setup variables for the sub-playlist
        $::JOB_PARAMS{'P931_COLLECT_RESOURCE_USAGE'} = "yes";
            # Output files
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_before_upgrade.txt";
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_CSV'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_before_upgrade.csv";
        $::JOB_PARAMS{'P931_CALCULATE_NEEDED_RESOURCES'} = "yes";
            # Output file
            $::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_requirement_for_upgrade.txt";
            # Input files
            $::JOB_PARAMS{'P931_FILENAME_CHART'} = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sc-values.yaml";
            # Check if we have any extra eric-sc-values files to include
            my @files = General::File_Operations::find_file(
                {
                    "directory" => $::JOB_PARAMS{'_JOB_CONFIG_DIR'},
                    "filename"  => "eric-sc-values-*.yaml",
                }
            );
            for (@files) {
                if (/^eric-sc-values-(\d+)\.yaml$/) {
                    $::JOB_PARAMS{"P931_FILENAME_CHART_$1"} = $_;
                }
            }
            $::JOB_PARAMS{'P931_FILENAME_UMBRELLA'} = $::JOB_PARAMS{'SC_UMBRELLA_FILE'};
        $::JOB_PARAMS{'P931_CHECK_REQUIRED_RESOURCES'} = "no";  # Changed from yes since check will not work for upgrade since resources are already used.
            # Output file
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_CHECK'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_check_before_upgrade.txt";
            # Input files
            # $::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'} is set above
            # $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE'} is set above
        # Call the sub-playlist
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::931_Check_Resource_Usage::main } );
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
sub KPI_Entry_Collection_P204S04 {

    my $rc;

    if ($::JOB_PARAMS{'SKIP_KPI_COLLECTION'} eq "no" && $::JOB_PARAMS{'COLLECT_ENTRY_KPI_VALUES'} eq "yes") {

        if ($::JOB_PARAMS{'SKIP_TRAFFIC_DELAY'} eq "no") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Entry_Time_To_Expire_P204S04T01 } );
            return $rc if $rc < 0;
        }

        $::JOB_PARAMS{'P933_TASK'} = "KPI_SNAPSHOT";
        $::JOB_PARAMS{'P933_SUMMARY_MESSAGE'} = "Entry KPI Values";
        $::JOB_PARAMS{'P933_RESOLUTION'} = $::JOB_PARAMS{'ENTRY_KPI_COLLECTION_RESOLUTION'};
        $::JOB_PARAMS{'P933_PAST'} = $::JOB_PARAMS{'ENTRY_KPI_COLLECTION_DURATION'};
        $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_statistics/entry_statistics";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::933_Handle_KPI_Statistics::main } );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Record_Upgrade_Start_Time_P204S04T02 } );
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
    sub Wait_For_Entry_Time_To_Expire_P204S04T01 {

        my $rc = 0;
        my $resolution = $::JOB_PARAMS{'ENTRY_KPI_COLLECTION_RESOLUTION'};
        my $wait_seconds = $::JOB_PARAMS{'ENTRY_KPI_COLLECTION_DURATION'};

        if ($wait_seconds =~ /^(\d+)s$/) {
            $wait_seconds = $1;
        } elsif ($wait_seconds =~ /^(\d+)m$/) {
            $wait_seconds = sprintf '%d', ($1 * 60);
        } elsif ($wait_seconds =~ /^(\d+)h$/) {
            $wait_seconds = sprintf '%d', ($1 * 3600);
        } elsif ($wait_seconds !~ /^\d+$/) {
            General::Logging::log_user_warning_message("Incorrect value ENTRY_KPI_COLLECTION_DURATION=$wait_seconds, using default value 5m");
            $wait_seconds = sprintf '%d', (5 * 60);
        }

        General::Logging::log_user_message("Waiting $wait_seconds seconds before collecting KPI statistics and starting Upgrade");
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "confirm-interrupt" => 0,
                "progress-message"  => 1,
                "progress-interval" => 60,
                "seconds"           => $wait_seconds,
                "use-logging"       => 1,
            }
        );
        if ($rc == 1) {
            General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
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
    sub Record_Upgrade_Start_Time_P204S04T02 {

        $::JOB_PARAMS{'UPGRADE_START_EPOCH'} = time();

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
sub Global_Preprocessing_P204S05 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::903_Global_Preprocess::main } );
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
sub Collect_Logs_Before_Upgrade_If_Wanted_P204S06 {

    my $rc = 0;

    if ($::JOB_PARAMS{'COLLECT_LOGS_BEFORE_UPGRADE'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Collection of logs before Upgrade not wanted");
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
sub Upgrade_Preprocessing_P204S07 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::924_Prepare_Deploy_Upgrade::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::907_Upgrade_Preprocess::main } );
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
sub Upgrade_SC_Via_EVNFM_P204S08 {

    my $rc;

    #$rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::929_Handle_Secrets::main } );
    #return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Session_P204S08T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&List_Packages_P204S08T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_If_Package_Exists_P204S08T03 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'PACKAGE_NEW_ID'} eq "") {
        # The package has not already been onboarded

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Package_P204S08T04 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Onboard_Package_P204S08T05 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Monitor_Onboard_Status_P204S08T06 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_Package_VNFDID_P204S08T07 } );
        return $rc if $rc < 0;

    }

    if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_CRD'} eq "no") {

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::927_Deploy_Upgrade_CRDS::main } );
        return $rc if $rc < 0;

    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_Instance_P204S08T08 } );
    return $rc if $rc < 0;

    if (exists $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'} && $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'} ne "") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Prepare_Change_VNF_Package_File_P204S08T09 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Upgrade_VNF_P204S08T10 } );
        return $rc if $rc < 0;

        if (exists $::JOB_PARAMS{'UPGRADE_JOB_URL'} && $::JOB_PARAMS{'UPGRADE_JOB_URL'} ne "") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Monitor_Upgrade_Status_P204S08T11 } );
            return $rc if $rc < 0;

            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Show_Instance_P204S08T12 } );
            return $rc if $rc < 0;
        }
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
    sub Create_Session_P204S08T01 {

        General::Logging::log_user_message("Creating a session");
        $::JOB_PARAMS{'SESSION_ID'} = ADP::EVNFM_Operations::create_session_id(
            {
                "debug-messages"        => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "hide-output"           => 1,
                "password"              => $::JOB_PARAMS{'PASSWORD'},
                "username"              => $::JOB_PARAMS{'USERNAME'},
            }
        );

        if ($::JOB_PARAMS{'SESSION_ID'} ne "") {
            General::Logging::log_user_message("SESSION_ID = $::JOB_PARAMS{'SESSION_ID'}");
            push @::JOB_STATUS, "(/) Create Session Id successful";
            return 0;
        } else {
            General::Logging::log_user_error_message("Failed to create a session");
            push @::JOB_STATUS, "(x) Create Session Id failed";
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
    sub List_Packages_P204S08T02 {

        my $json_data;
        my $length;
        my $lines = "";
        my $maxlength_vnfProductName = 14;
        my $maxlength_vnfSoftwareVersion = 18;
        my $maxlength_vnfdVersion = 11;

        General::Logging::log_user_message("Get VNF package information");
        $json_data = ADP::EVNFM_Operations::get_vnf_package_info(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "package-id"            => "",  # All packages
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
            }
        );
        if (ref($json_data) eq "HASH") {
            # HASH Reference

            # Calculate column lengths
            for my $key (keys %$json_data) {
                if (exists $json_data->{$key}{"vnfProductName"}) {
                    $length = length($json_data->{$key}{"vnfProductName"});
                    $maxlength_vnfProductName = $length if $length > $maxlength_vnfProductName;
                }
                if (exists $json_data->{$key}{"vnfSoftwareVersion"}) {
                    $length = length($json_data->{$key}{"vnfSoftwareVersion"});
                    $maxlength_vnfSoftwareVersion = $length if $length > $maxlength_vnfSoftwareVersion;
                }
                if (exists $json_data->{$key}{"vnfdVersion"}) {
                    $length = length($json_data->{$key}{"vnfdVersion"});
                    $maxlength_vnfdVersion = $length if $length > $maxlength_vnfdVersion;
                }
            }

            # Display data
            $lines .= sprintf "%-36s  %-36s  %-${maxlength_vnfProductName}s  %-${maxlength_vnfSoftwareVersion}s  %-${maxlength_vnfdVersion}s  %-15s  %-16s  %-10s\n",
                "vnfPkgId",
                "vnfdId",
                "vnfProductName",
                "vnfSoftwareVersion",
                "vnfdVersion",
                "onboardingState",
                "operationalState",
                "usageState";
            $lines .= sprintf "%-36s  %-36s  %-${maxlength_vnfProductName}s  %-${maxlength_vnfSoftwareVersion}s  %-${maxlength_vnfdVersion}s  %-15s  %-16s  %-10s\n",
                "-"x36,
                "-"x36,
                "-"x$maxlength_vnfProductName,
                "-"x$maxlength_vnfSoftwareVersion,
                "-"x$maxlength_vnfdVersion,
                "-"x15,
                "-"x16,
                "-"x10;
            for my $key (sort keys %$json_data) {
                $lines .= sprintf "%-36s  %-36s  %-${maxlength_vnfProductName}s  %-${maxlength_vnfSoftwareVersion}s  %-${maxlength_vnfdVersion}s  %-15s  %-16s  %-10s\n",
                    $key,
                    (exists $json_data->{$key}{"vnfdId"}                ? $json_data->{$key}{"vnfdId"}              : "-"),
                    (exists $json_data->{$key}{"vnfProductName"}        ? $json_data->{$key}{"vnfProductName"}      : "-"),
                    (exists $json_data->{$key}{"vnfSoftwareVersion"}    ? $json_data->{$key}{"vnfSoftwareVersion"}  : "-"),
                    (exists $json_data->{$key}{"vnfdVersion"}           ? $json_data->{$key}{"vnfdVersion"}         : "-"),
                    (exists $json_data->{$key}{"onboardingState"}       ? $json_data->{$key}{"onboardingState"}     : "-"),
                    (exists $json_data->{$key}{"operationalState"}      ? $json_data->{$key}{"operationalState"}    : "-"),
                    (exists $json_data->{$key}{"usageState"}            ? $json_data->{$key}{"usageState"}          : "-");
            }
        } else {
            General::Logging::log_user_message("Either no packages to print or something went wrong when fetching package information");
            return 0;
        }

        General::Logging::log_user_message($lines);

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
    sub Check_If_Package_Exists_P204S08T03 {

        my $packages;
        my $software_version = $::JOB_PARAMS{'SC_RELEASE_VERSION'} . $::JOB_PARAMS{'SC_RELEASE_BUILD'};

        # Initialize the package id
        $::JOB_PARAMS{'PACKAGE_NEW_ID'} = "";
        $::JOB_PARAMS{'PACKAGE_NEW_VNFDID'} = "";

        General::Logging::log_user_message("Get VNF package information");
        $packages = ADP::EVNFM_Operations::get_vnf_package_info(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "package-id"            => "",  # All packages
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
            }
        );
        if (ref($packages) eq "HASH") {
            # HASH Reference
            for my $key (sort keys %$packages) {
                if (exists $packages->{$key}{"vnfSoftwareVersion"} && $packages->{$key}{"vnfSoftwareVersion"} eq $software_version) {
                    # The package already exists, store the package id and vnfdId
                    $::JOB_PARAMS{'PACKAGE_NEW_ID'} = $key;
                    $::JOB_PARAMS{'PACKAGE_NEW_VNFDID'} = $packages->{$key}{"vnfdId"} if (exists $packages->{$key}{"vnfdId"});
                    General::Logging::log_user_message("PACKAGE_NEW_ID = $::JOB_PARAMS{'PACKAGE_NEW_ID'}\nPACKAGE_NEW_VNFDID = $::JOB_PARAMS{'PACKAGE_NEW_VNFDID'}\n");
                    last;
                }
            }
        } else {
            General::Logging::log_user_message("Either no packages to print or something went wrong when fetching package information");
            return 0;
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
    sub Create_Package_P204S08T04 {

        General::Logging::log_user_message("Creating a package");
        $::JOB_PARAMS{'PACKAGE_NEW_ID'} = ADP::EVNFM_Operations::create_vnf_package_id(
            {
                "data"                  => '{"userDefinedData":{"description": "Package loaded by DAFT"}}',
                "debug-messages"        => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "hide-output"           => 1,
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
            }
        );

        if ($::JOB_PARAMS{'PACKAGE_NEW_ID'} ne "") {
            General::Logging::log_user_message("PACKAGE_NEW_ID = $::JOB_PARAMS{'PACKAGE_NEW_ID'}");
            push @::JOB_STATUS, "(/) Create Package Id successful";
            return 0;
        } else {
            General::Logging::log_user_error_message("Failed to create a package");
            push @::JOB_STATUS, "(x) Create Package Id failed";
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
    sub Onboard_Package_P204S08T05 {

        my $csar_file = "$::JOB_PARAMS{'SOFTWARE_DIR_NAME'}/$::JOB_PARAMS{'SOFTWARE_FILE_NAME'}";
        my $rc;

        General::Logging::log_user_message("Onboarding PACKAGE_NEW_ID $::JOB_PARAMS{'PACKAGE_NEW_ID'}\nThis will take a while to complete.\n");
        my $rc = ADP::EVNFM_Operations::onboard_vnf_package(
            {
                "debug-messages"        => 1,
                "file"                  => $csar_file,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "hide-output"           => 1,
                "no-monitoring"         => 1,
                "package-id"            => $::JOB_PARAMS{'PACKAGE_NEW_ID'},
                "progress-messages"     => 1,
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message("Onboarding was successfully started");
            push @::JOB_STATUS, "(/) Onboarding Package started";
            return 0;
        } else {
            General::Logging::log_user_error_message("Onboarding failed");
            push @::JOB_STATUS, "(x) Onboarding Package failed";
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
    sub Monitor_Onboard_Status_P204S08T06 {

        my $rc;

        General::Logging::log_user_message("Monitoring onboarding status for max 1 hour.\nThis will take a while to complete.\n");
        $rc = ADP::EVNFM_Operations::check_onboarding_status(
            {
                "debug-messages"        => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "hide-output"           => 1,
                "monitor-timeout"       => 3600,
                "package-id"            => $::JOB_PARAMS{'PACKAGE_NEW_ID'},
                "progress-messages"     => 1,
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message("Onboarding was successful");
            push @::JOB_STATUS, "(/) Onboarding Package successful";
            return 0;
        } else {
            General::Logging::log_user_error_message("Onboarding failed");
            push @::JOB_STATUS, "(x) Onboarding Package failed";
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
    sub Get_Package_VNFDID_P204S08T07 {

        my $packages;
        my $package_id = $::JOB_PARAMS{'PACKAGE_NEW_ID'},

        General::Logging::log_user_message("Get VNF package information");
        $packages = ADP::EVNFM_Operations::get_vnf_package_info(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "package-id"            => $::JOB_PARAMS{'PACKAGE_NEW_ID'},
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
            }
        );
        if (ref($packages) eq "HASH") {
            # HASH Reference
            if (exists $packages->{$package_id} && exists $packages->{$package_id}{"vnfdId"}) {
                $::JOB_PARAMS{'PACKAGE_NEW_VNFDID'} = $packages->{$package_id}{"vnfdId"};
            } else {
                General::Logging::log_user_error_message("Could not find the 'vnfdId' value for package $::JOB_PARAMS{'PACKAGE_NEW_ID'}");
                return 1;
            }
        } else {
            General::Logging::log_user_message("Either no packages to print or something went wrong when fetching package information");
            return 1;
        }

        General::Logging::log_user_message("PACKAGE_NEW_VNFDID = $::JOB_PARAMS{'PACKAGE_NEW_VNFDID'}");

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
    sub Get_Instance_P204S08T08 {
        my $found = "";
        my $json_data;
        my $lines = "";
        my $instance_id = "";
        my $multiple_instances = 0;
        my $attempt = 1;
        my $max_attempts = 3;

        General::Logging::log_user_message("Get VNF instance information");

        while ($attempt < $max_attempts+1) {
            General::Logging::log_user_message("Attempt number : $attempt");
            $lines = "";
            $json_data = ADP::EVNFM_Operations::get_vnf_instance_info(
                {
                    "debug-messages"        => 1,
                    "hide-output"           => 1,
                    "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                    "instance-id"           => $instance_id,        # all instances $instance_id=""
                    "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
                }
            );
            $::JOB_PARAMS{'PACKAGE_OLD_ID'} = "";
            $::JOB_PARAMS{'PACKAGE_OLD_VNFDID'} = "";
            if (ref($json_data) eq "HASH") {
                # HASH Reference
                $lines .= sprintf "%-36s  %-36s  %-18s  %-18s  %-36s  %-25s  %-25s\n",
                    "vnfInstanceId",
                    "vnfdId",
                    "instantiationState",
                    "vnfSoftwareVersion",
                    "vnfInstanceName",
                    "clusterName",
                    "namespace";
                $lines .= sprintf "%-36s  %-36s  %-18s  %-18s  %-36s  %-25s  %-25s\n",
                    "-"x36,
                    "-"x36,
                    "-"x18,
                    "-"x18,
                    "-"x36,
                    "-"x25,
                    "-"x25;

                # Look for instances where clusterName and namespace match with the wanted deployment
                # and store them in job variables (PACKAGE_VNFDINSTANCEID among others.
                for my $key (sort keys %$json_data) {
                    if (exists $json_data->{$key}{"clusterName"}) {
                        my $ns = $json_data->{$key}{"instantiatedVnfInfo"}{"vnfcResourceInfo"};
                        my $namespace = $ns->[0]{"computeResource"}{"vimLevelAdditionalResourceInfo"}{"namespace"};
                        my $sw_ver = (exists $json_data->{$key}{"vnfSoftwareVersion"} ? $json_data->{$key}{"vnfSoftwareVersion"}  : "-");
                        $lines .= sprintf "%-36s  %-36s  %-18s  %-18s  %-36s  %-25s  %-25s\n",
                            $key,
                            (exists $json_data->{$key}{"vnfdId"}                ? $json_data->{$key}{"vnfdId"}              : "-"),
                            (exists $json_data->{$key}{"instantiationState"}    ? $json_data->{$key}{"instantiationState"}  : "-"),
                            $sw_ver,
                            (exists $json_data->{$key}{"vnfInstanceName"}       ? $json_data->{$key}{"vnfInstanceName"}     : "-"),
                            $json_data->{$key}{"clusterName"},
                            $namespace;

                        if ((exists $json_data->{$key}{"vnfdId"}) && (exists $json_data->{$key}{"instantiationState"})) {
                            if ($json_data->{$key}{"clusterName"} eq $::JOB_PARAMS{'CLUSTERNAME'}) {
                                if ($namespace) {
                                    if ($namespace eq $::JOB_PARAMS{'SC_NAMESPACE'}) {
                                        if (!$multiple_instances) {
                                            $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'} = $key;
                                            $::JOB_PARAMS{'PACKAGE_OLD_VNFDID'} = $json_data->{$key}{"vnfdId"};
                                            $::JOB_PARAMS{'PACKAGE_VNFD_NAMESPACE'} = $namespace;
                                            $found .= "Found instance on node $::JOB_PARAMS{'CLUSTERNAME'}.\n    vnfInstanceId: $key\n        namespace: $::JOB_PARAMS{'PACKAGE_VNFD_NAMESPACE'}\n          package: $sw_ver\n";
                                        } else {
                                            if ($multiple_instances == 1) {
                                                $found .= "Multiple instances found on $::JOB_PARAMS{'CLUSTERNAME'}.\n    vnfInstanceId: $key\n        namespace: $::JOB_PARAMS{'PACKAGE_VNFD_NAMESPACE'}\n          package: $sw_ver\n";
                                            } else {
                                                $found .= "\n    vnfInstanceId: $key\n        namespace: $::JOB_PARAMS{'PACKAGE_VNFD_NAMESPACE'}\n          package: $sw_ver\n";
                                            }
                                            $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID-'.$multiple_instances} = $key;
                                            $::JOB_PARAMS{'PACKAGE_OLD_VNFDID-'.$multiple_instances} = $json_data->{$key}{"vnfdId"};
                                            $::JOB_PARAMS{'PACKAGE_VNFD_NAMESPACE-'.$multiple_instances} = $namespace;
                                        }
                                        $multiple_instances++;
                                        $attempt = $max_attempts;
                                    } else {
                                        $found .= "Cluster found but namespace : $namespace not same as desired: $::JOB_PARAMS{'SC_NAMESPACE'}\n";
                                    }
                                } else {
                                    $found .= "Cluster found but without namespace.\n    vnfInstanceId: $key\n        namespace: -\n          package: $sw_ver\n";
                                    $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'} = $key;
                                    $::JOB_PARAMS{'PACKAGE_OLD_VNFDID'} = $json_data->{$key}{"vnfdId"};
                                    $::JOB_PARAMS{'PACKAGE_VNFD_NAMESPACE'} = $namespace;
                                }
                            }
                        }
                    }
                }
            } else {
                General::Logging::log_user_message("Either no packages to print or something went wrong when fetching package information");
                return 0;
            }
            $attempt++;
        }
        General::Logging::log_user_message("$lines\n$found");
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
    sub Prepare_Change_VNF_Package_File_P204S08T09 {

        my $parameter = "CONFIG_FILE_CHANGE_VNF_PACKAGE";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/changeVnfPackage.json";
        my $rc = 0;

        if ($::JOB_PARAMS{$parameter} ne "") {
            General::Logging::log_user_message("Checking Job parameter '$parameter' and creating a copy of the file");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cp -fpv $input_file $output_file",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy file '$input_file'");
            }
        } else {
            General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from job/network configuration parameters");
            $rc = General::File_Operations::write_file(
                {
                    "filename"              => $output_file,
                    "output-ref"            => [
                        "{",
                        "    \"vnfdId\" : \"$::JOB_PARAMS{'PACKAGE_NEW_VNFDID'}\",",
                        "    \"clusterName\" : \"$::JOB_PARAMS{'CLUSTERNAME'}\",",
                        "    \"additionalParams\" : {",
                        "        \"namespace\" : \"$::JOB_PARAMS{'SC_NAMESPACE'}\",",
                        "        \"applicationTimeout\" : \"6000\",",
                        "        \"commandTimeOut\" : \"5000\",",
                        "        \"helmNoHooks\" : \"false\",",
                        "        \"isAutoRollbackAllowed\" : \"false\",",
                        "        \"skipJobVerification\" : \"true\",",
                        "        \"skipVerification\" : \"false\",",
                        "        \"helmWait\" : \"false\"",
                        "    }",
                        "}",
                    ],
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to write file '$output_file'");
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
    sub Upgrade_VNF_P204S08T10 {

        my $url;

        General::Logging::log_user_message("Upgrade SC VNF");
        $::JOB_PARAMS{'UPGRADE_JOB_URL'} = ADP::EVNFM_Operations::change_vnfpkg(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "instance-id"           => $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'},
                "change-file"           => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/changeVnfPackage.json",
                "no-monitoring"         => 1,
                "progress-messages"     => 1,
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
                "values-file"           => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sc-values.yaml",
                "vnfd-id"               => $::JOB_PARAMS{'PACKAGE_NEW_VNFDID'},
                "cluster-name"          => $::JOB_PARAMS{'CLUSTERNAME'},
                "namespace"             => $::JOB_PARAMS{'SC_NAMESPACE'},
            }
        );

        if ($::JOB_PARAMS{'UPGRADE_JOB_URL'} ne "") {
            General::Logging::log_user_message("Upgrade was successfully started");
            push @::JOB_STATUS, "(/) Upgrade VNF started";
            return 0;
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Upgrade failed");
            push @::JOB_STATUS, "(x) Upgrade VNF failed";
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
    sub Monitor_Upgrade_Status_P204S08T11 {

        my $rc;

        General::Logging::log_user_message("Monitoring Upgrade status for max 1 hour.\nThis will take a while to complete.\n");
        $rc = ADP::EVNFM_Operations::check_operation_status(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "monitor-timeout"       => 3600,
                "progress-messages"     => 1,
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
                "type"                  => "upgrade",
                "url"                   => $::JOB_PARAMS{'UPGRADE_JOB_URL'},
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message("Upgrade was successful");
            push @::JOB_STATUS, "(/) Upgrade VNF successful";
            return 0;
        } else {
            General::Logging::log_user_error_message("Upgrade failed");
            push @::JOB_STATUS, "(x) Upgrade VNF failed";
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
    sub Show_Instance_P204S08T12 {
        my $json_data;
        my $lines = "";
        my $instance_id = $::JOB_PARAMS{'PACKAGE_VNFDINSTANCEID'};

        General::Logging::log_user_message("Get VNF instance information");
        $lines = "";
        $json_data = ADP::EVNFM_Operations::get_vnf_instance_info(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "instance-id"           => $instance_id,
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
            }
        );
        if (ref($json_data) eq "HASH") {
            # HASH Reference
            $lines .= sprintf "%-36s  %-36s  %-18s  %-18s  %-36s  %-25s  %-25s\n",
                "vnfInstanceId",
                "vnfdId",
                "instantiationState",
                "vnfSoftwareVersion",
                "vnfInstanceName",
                "clusterName",
                "namespace";
            $lines .= sprintf "%-36s  %-36s  %-18s  %-18s  %-36s  %-25s  %-25s\n",
                "-"x36,
                "-"x36,
                "-"x18,
                "-"x18,
                "-"x36,
                "-"x25,
                "-"x25;

            # Look for instances where clusterName and namespace match with the wanted deployment
            # and store them in job variables (PACKAGE_VNFDINSTANCEID among others.
            for my $key (sort keys %$json_data) {
                if (exists $json_data->{$key}{"clusterName"}) {
                    my $ns = $json_data->{$key}{"instantiatedVnfInfo"}{"vnfcResourceInfo"};
                    my $namespace = $ns->[0]{"computeResource"}{"vimLevelAdditionalResourceInfo"}{"namespace"};
                    my $sw_ver = (exists $json_data->{$key}{"vnfSoftwareVersion"} ? $json_data->{$key}{"vnfSoftwareVersion"}  : "-");
                    $lines .= sprintf "%-36s  %-36s  %-18s  %-18s  %-36s  %-25s  %-25s\n",
                        $key,
                        (exists $json_data->{$key}{"vnfdId"}                ? $json_data->{$key}{"vnfdId"}              : "-"),
                        (exists $json_data->{$key}{"instantiationState"}    ? $json_data->{$key}{"instantiationState"}  : "-"),
                        $sw_ver,
                        (exists $json_data->{$key}{"vnfInstanceName"}       ? $json_data->{$key}{"vnfInstanceName"}     : "-"),
                        $json_data->{$key}{"clusterName"},
                        $namespace;
                }
            }
        } else {
            General::Logging::log_user_message("Either no packages to print or something went wrong when fetching package information");
            return 0;
        }
        General::Logging::log_user_message($lines);
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
sub Upgrade_Postprocessing_P204S09 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::908_Upgrade_Postprocess::main } );
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
sub Global_Postprocessing_P204S10 {

    my $rc;

    # Since we most likely are using an external EVNFM registry we need to clear
    # the SC_REGISTRY_URL variable while calling the 904_Global_Postprocess
    # playlist to avoid the 'registry_pod_images' check in the 'system_health_check.pl'
    # script from being called which will cause a failure of the health check.
    my $old_sc_registry_url = $::JOB_PARAMS{'SC_REGISTRY_URL'};
    $::JOB_PARAMS{'SC_REGISTRY_URL'} = "";

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::904_Global_Postprocess::main } );
    # Restore the old value
    $::JOB_PARAMS{'SC_REGISTRY_URL'} = $old_sc_registry_url;
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
sub Collect_Logs_P204S11 {

    my $rc = 0;

    if ($::JOB_PARAMS{'SKIP_RESOURCE_CHECK'} eq "no") {

        # Setup variables for the sub-playlist
        $::JOB_PARAMS{'P931_COLLECT_RESOURCE_USAGE'} = "yes";
            # Output files
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_after_upgrade.txt";
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_CSV'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_after_upgrade.csv";
        $::JOB_PARAMS{'P931_COMPARE_RESOURCE_USAGE'} = "no";    # Changed from yes since the current implementation for comparing does not work for an upgrade
            # Output file
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_DIFFERENCES'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_differences_after_upgrade.txt";
            # Input files
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_CHECK'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_check_before_upgrade.txt";
            $::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_requirement_for_upgrade.txt";
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_BEFORE'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_before_upgrade.txt";
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_AFTER'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_after_upgrade.txt";
        # Call the sub-playlist
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::931_Check_Resource_Usage::main } );
        return $rc if $rc < 0;

    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Collect_Certificate_Validity_Information_After_Upgrade_P204S11T01 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'COLLECT_LOGS_AT_SUCCESS'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Collection of logs at successful job not wanted");
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
    sub Collect_Certificate_Validity_Information_After_Upgrade_P204S11T01 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/parse_certificate_validity_info.pl";
        my $command_parameters = "--output-tsv-data --namespace=$::JOB_PARAMS{'SC_NAMESPACE'} --output-file=$::JOB_PARAMS{'_JOB_LOG_DIR'}/certificate_validity_info_after_upgrade.tsv";
        my $rc;
        my @result;

        General::Logging::log_user_message("Print certificate validity information after the upgrade");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command $command_parameters",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute the '$command' command");
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
sub KPI_Upgrade_And_Exit_Collection_P204S12 {

    my $rc = 0;

    if ($::JOB_PARAMS{'SKIP_KPI_COLLECTION'} eq "no" && $::JOB_PARAMS{'COLLECT_UPGRADE_KPI_VALUES'} eq "yes") {

        $::JOB_PARAMS{'P933_TASK'} = "KPI_VERDICT";
        $::JOB_PARAMS{'P933_RESOLUTION'} = $::JOB_PARAMS{'UPGRADE_KPI_COLLECTION_RESOLUTION'};
        $::JOB_PARAMS{'P933_PAST'} = sprintf '%ds', (time() - $::JOB_PARAMS{'UPGRADE_START_EPOCH'});
        if (exists $::JOB_PARAMS{'ENABLED_CNF'} && $::JOB_PARAMS{'ENABLED_CNF'} ne "" && $::JOB_PARAMS{'ENABLED_CNF'} ne "all") {
            my $description = "(" . uc($::JOB_PARAMS{'ENABLED_CNF'}) . ").+";
            $description =~ s/[- ,]/|/g;
            $::JOB_PARAMS{'P933_SUCCESS_RATE_DESCRIPTION'} = $description;
        } else {
            $::JOB_PARAMS{'P933_SUCCESS_RATE_DESCRIPTION'} = ".+";
        }
        $::JOB_PARAMS{'P933_SUCCESS_RATE_THRESHOLD'} = $::JOB_PARAMS{'KPI_SUCCESS_RATE_THRESHOLD'};
        $::JOB_PARAMS{'P933_SUMMARY_MESSAGE'} = "Upgrade KPI Success Rate Verdict";
        $::JOB_PARAMS{'P933_VERDICT_IGNORE_FAILURE'} = "yes";
        $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_statistics/upgrade_verdict";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::933_Handle_KPI_Statistics::main } );
        return $rc if $rc < 0;

    }

    if ($::JOB_PARAMS{'SKIP_KPI_COLLECTION'} eq "no" && $::JOB_PARAMS{'COLLECT_EXIT_KPI_VALUES'} eq "yes") {

        if ($::JOB_PARAMS{'SKIP_TRAFFIC_DELAY'} eq "no") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Exit_Time_To_Expire_P204S12T01 } );
            return $rc if $rc < 0;
        }

        $::JOB_PARAMS{'P933_TASK'} = "KPI_VERDICT";
        $::JOB_PARAMS{'P933_RESOLUTION'} = $::JOB_PARAMS{'EXIT_KPI_COLLECTION_RESOLUTION'};
        $::JOB_PARAMS{'P933_PAST'} = $::JOB_PARAMS{'EXIT_KPI_COLLECTION_DURATION'};
        if (exists $::JOB_PARAMS{'ENABLED_CNF'} && $::JOB_PARAMS{'ENABLED_CNF'} ne "" && $::JOB_PARAMS{'ENABLED_CNF'} ne "all") {
            my $description = "(" . uc($::JOB_PARAMS{'ENABLED_CNF'}) . ").+";
            $description =~ s/[- ,]/|/g;
            $::JOB_PARAMS{'P933_SUCCESS_RATE_DESCRIPTION'} = $description;
        } else {
            $::JOB_PARAMS{'P933_SUCCESS_RATE_DESCRIPTION'} = ".+";
        }
        $::JOB_PARAMS{'P933_SUCCESS_RATE_THRESHOLD'} = $::JOB_PARAMS{'KPI_SUCCESS_RATE_THRESHOLD'};
        $::JOB_PARAMS{'P933_SUMMARY_MESSAGE'} = "Upgrade Exit KPI Success Rate Verdict";
        $::JOB_PARAMS{'P933_VERDICT_IGNORE_FAILURE'} = "yes";
        $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_statistics/exit_verdict";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::933_Handle_KPI_Statistics::main } );
        return $rc if $rc < 0;

    }

    if ($::JOB_PARAMS{'SKIP_KPI_COLLECTION'} eq "no" && $::JOB_PARAMS{'COLLECT_ENTRY_KPI_VALUES'} eq "yes" && $::JOB_PARAMS{'COLLECT_EXIT_KPI_VALUES'} eq "yes") {

        $::JOB_PARAMS{'P933_TASK'} = "KPI_COMPARE";
        $::JOB_PARAMS{'P933_ENTRY_DIRECTORY'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_statistics/entry_statistics";
        $::JOB_PARAMS{'P933_EXIT_DIRECTORY'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_statistics/exit_verdict";
        $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_statistics";
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
    sub Wait_For_Exit_Time_To_Expire_P204S12T01 {

        my $rc = 0;
        my $resolution = $::JOB_PARAMS{'EXIT_KPI_COLLECTION_RESOLUTION'};
        my $wait_seconds = $::JOB_PARAMS{'EXIT_KPI_COLLECTION_DURATION'};

        if ($wait_seconds =~ /^(\d+)s$/) {
            $wait_seconds = $1;
        } elsif ($wait_seconds =~ /^(\d+)m$/) {
            $wait_seconds = sprintf '%d', ($1 * 60);
        } elsif ($wait_seconds =~ /^(\d+)h$/) {
            $wait_seconds = sprintf '%d', ($1 * 3600);
        } elsif ($wait_seconds !~ /^\d+$/) {
            General::Logging::log_user_warning_message("Incorrect value EXIT_KPI_COLLECTION_DURATION=$wait_seconds, using default value 5m");
            $wait_seconds = sprintf '%d', (5 * 60);
        }

        General::Logging::log_user_message("Waiting $wait_seconds seconds before collecting KPI statistics after completed Upgrade");
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "confirm-interrupt" => 0,
                "progress-message"  => 1,
                "progress-interval" => 60,
                "seconds"           => $wait_seconds,
                "use-logging"       => 1,
            }
        );
        if ($rc == 1) {
            General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
            $rc = 0;
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
sub Cleanup_Job_Environment_P204S13 {

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
sub Fallback001_P204S99 {

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
    return Playlist::920_Check_Network_Config_Information::parse_network_config_parameters();
}

# -----------------------------------------------------------------------------
sub set_playlist_variables {
    %playlist_variables = (
        # ---------------------------------------------------------------------
        'CLUSTERNAME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The name of the cluster that EVNFM will deploy the SC software to, this parameter
is manadory unless the network configuration file parameter 'evnfm_clustername'
is filled with a valid value or a special configuration file is specified with
job parameter 'CONFIG_FILE_CHANGE_VNF_PACKAGE'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'COLLECT_ENTRY_KPI_VALUES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if entry KPIs values should be collected before starting
the Upgrade.
See also parameters 'ENTRY_KPI_COLLECTION_DURATION' and 'ENTRY_KPI_COLLECTION_RESOLUTION'.

If value is 'yes' then KPI values are collected before starting the Upgrade, this
is also the default value if not specified.

If value is 'no' then no KPI values are collected before starting the Upgrade.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'COLLECT_EXIT_KPI_VALUES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if exit KPIs values should be collected after completed
Upgrade.
See also parameters 'EXIT_KPI_COLLECTION_DURATION' and 'EXIT_KPI_COLLECTION_RESOLUTION'.

If value is 'yes' then KPI values are collected after completing the Upgrade,
this is also the default value if not specified.

If value is 'no' then no KPI values are collected after completing the Upgrade.
EOF
            'validity_mask' => '(yes|no)',
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
        'COLLECT_LOGS_BEFORE_UPGRADE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if detailed log files should be collected before the
Upgrade is done.
Normally detailed logs are only collected at the end of a job when the job fails
to aid with trouble shooting efforts.

If value is 'yes' then detailed ADP logs and other information will be
collected and stored in the all.log or under the troubleshooting_logs sub
directory of the job before starting the upgrade.
The default value is 'no' if the variable is not specified.

If value is 'no' then no collection of detailed logs will be done before starting
the upgrade.
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
logs and other information will be collected and stored in the all.log or under
the troubleshooting_logs sub directory of the job.
The default value is 'no' if the variable is not specified.

If value is 'no' then no collection of detailed logs will be done at successful
job termination.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'COLLECT_UPGRADE_KPI_VALUES' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if KPIs values should be collected during the Upgrade.
See also parameter 'UPGRADE_KPI_COLLECTION_RESOLUTION'.

If value is 'yes' then KPI values are collected during the Upgrade, this is also
the default value if not specified.

If value is 'no' then no KPI values are collected during the Upgrade.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_CHANGE_VNF_PACKAGE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds the parameters for the change of the VNF package
(upgrade).
The VNF Package Change (Upgrade) operation requires the setting of certain
parameters. These parameters points to which VNF instance id to be upgraded,
namespace and some other important parameters.

Suggested filename: changeVnfPackage.json

For detailed information please see EVNFM documentation.

If this parameter is not specified then a file will be automatically generated
with some default values. This is usually to be prefered since it will auto
detect VNF Instance Id etc.
EOF
            'validity_mask' => '.+',
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
Deployment will take place.
Any command that will just change files or data on the job workspace directory
or any command that just reads data from the cluster will still be executed.
This parameter can be useful to check if the overall logic is working without
doing any changes to the running system.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'ENABLED_CNF' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the Containerized Network Functions (CNF) that is active on the
deployed software.

If more than one CNF is enabled then separate each CNF with either a dash (-),
comma (,) or a space ( ).
For example: "bsf-wcdb", "bsf,wcdb" or "bsf wcdb".

The ENABLED_CNF value is used for the KPI collection checks and if not set
correctly then the checks might fail because it might check for KPI values
that does not exist.
If not set then a default list of allowed values are used.

Currently known CNF values are the following:
    - bsf
    - csa
    - objectstorage
    - rlf
    - scp
    - sepp
    - sftp
    - slf
    - spr
    - wcdb

Not all combinations are valid or make sense, so know what combination to use.
EOF
            'validity_mask' => '.*',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'ENTRY_KPI_COLLECTION_DURATION' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "5m",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the amount of time into the past to collect KPI statistics before
starting the upgrade.

The value can either be an integer value in which case this is how many KPI
collections with the 'ENTRY_KPI_COLLECTION_RESOLUTION' will be collected which
means that if ENTRY_KPI_COLLECTION_DURATION=10 and
ENTRY_KPI_COLLECTION_RESOLUTION=15 then it will collect KPI values for about 150
seconds into the past.
If the value is an integer followed by the 's', 'm' or 'h' character then is how
many seconds, minutes or hours into the past to collect KPI's using the
'ENTRY_KPI_COLLECTION_RESOLUTION' between each KPI.
EOF
            'validity_mask' => '(\d+|\d+[smh])',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'ENTRY_KPI_COLLECTION_RESOLUTION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "15",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the amount of time in seconds between each KPI statistics before
starting the upgrade.
See 'ENTRY_KPI_COLLECTION_DURATION' for more details.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'EXIT_KPI_COLLECTION_DURATION' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "5m",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the amount of time into the future to collect KPI statistics after a
completed upgrade.

The value can either be an integer value in which case this is how many KPI
collections with the 'EXIT_KPI_COLLECTION_RESOLUTION' will be collected which
means that if EXIT_KPI_COLLECTION_DURATION=10 and EXIT_KPI_COLLECTION_RESOLUTION=15
then it will collect KPI values for about 150 seconds into the future.
If the value is an integer followed by the 's', 'm' or 'h' character then is how
many seconds, minutes or hours into the future to collect KPI's using the
'EXIT_KPI_COLLECTION_RESOLUTION' between each KPI.
EOF
            'validity_mask' => '(\d+|\d+[smh])',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'EXIT_KPI_COLLECTION_RESOLUTION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "15",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the amount of time in seconds between each KPI statistics after a
completed upgrade.
See 'EXIT_KPI_COLLECTION_DURATION' for more details.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'HELM_TIMEOUT_CRD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "600",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the timeout value to use when deploying the CRD software using the helm
command, if not specified then the default value will be used.

The time value which must be an integer value is given as the number of seconds
to wait for the deployment to finish.
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
        'INGRESS_HOST_ADDRESS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the ingress host address to the EVNFM, this parameter is mandatory unless
the network configuration file parameter 'evnfm_ingress_host_address' is filled
with a valid value.
For example: "evnfm02.eoaas.n17.sc.sero.gic.ericsson.se"
EOF
            'validity_mask' => '.+',
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
Select "yes" if the Playlist should ignore the result of the health checks done
before an Upgrade and after and Installtion or Upgrade and continue regardless
if it fails.

Select "no" which is also the default value if not specified, to have the
Playlist stop if the health checks detects an error.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'IGNORE_FAILED_RESOURCE_CHECK' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should ignore the result of the available resource
check done before a Deployment. If it's estimated that there are not enough
resources for a Deployment then only a warning message is printed and the Deploy
will continue anyway.

Select "no" which is also the default value if not specified, to have the
Playlist stop if there is not enogh resources for a Deployment.

See also parameter "SKIP_RESOURCE_CHECK".
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'KPI_MAX_COMPARE_DIFFERENCE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "20",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This specifies the maximum difference between KPI values collected before (entry)
and after (exit) the upgrade. It is specified in percent which is calculated by
the following formula:

    ((<KPI exit value> - <KPI entry value>) / <KPI entry value>) * 100

So if the difference is greater than this value then the KPI is marked with an
error.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'KPI_SUCCESS_RATE_THRESHOLD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "95",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified and set to an integer value then this is limit to use to check if the
success rate is below this limit which would cause the check to fail.
If not specified then the default value will be used.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'KPI_TRAFFIC_COMPARE_DIFFERENCE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "10",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This specifies the maximum difference between calculated average values of the
traffic MPS/TPS counters divided by the load counters in millicores, collected
before (entry) and after (exit) the upgrade. It is specified in percent which is
calculated by the following formula:

    ((<KPI exit value> - <KPI entry value>) / <KPI entry value>) * 100

So if the difference is negative and greater than this value then the KPI is
marked with an error.
EOF
            'validity_mask' => '\d+',
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
        'PASSWORD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the password to use to access EVNFM, this parameter is mandatory unless
the network configuration file parameter 'evnfm_password' is filled with a valid
value.
For example: "DefaultP12345!"
EOF
            'validity_mask' => '.+',
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
        'POD_STATUS_TIMEOUT' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "600",
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
        'SKIP_DEPLOY_UPGRADE_CRD' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the actual deployment of the CRD Helm chart should be
skipped but everything else should be performed like installing or upgrading SC
and loading of secrets etc.
If the value is set to "yes" then the playlist will skip the deployment of the
Helm chart and checking of CRD status.

This is a special job parameter that will be used by EVNFM since they want to
do the actual deployment of the Helm chart using their own scripts.

The default if this parameter is not given is to perform the full deployment of
the CRD software.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_DEPLOY_UPGRADE_SC' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the actual deployment of the SC Helm chart should be
skipped but everything else should be performed like installing or upgrading CRD
and loading of secrets etc.
If the value is set to "yes" then the playlist will skip the deployment of the
Helm chart and checking of Cluster status.

This is a special job parameter that will be used by EVNFM since they want to
do the actual deployment of the Helm chart using their own scripts.

The default if this parameter is not given is to perform the full deployment of
the SC software.
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
        'SKIP_RESOURCE_CHECK' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the playlist should check if there are enough available
CPU and Memory resources before attempting to deploy the software.
If the value is set to "yes" then the playlist will skip the resource check which
might cause failures during the deploy if there are not enough resources available.
If the value is set to "no", which is also the default, then a check of available
resources is performed before doing the deployment and the playlist will stop with
an error if there is a lack of resources.

See also parameter "IGNORE_FAILED_RESOURCE_CHECK".
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_TRAFFIC_DELAY' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Option to skip the delay time specified by ENTRY_KPI_COLLECTION_DURATION and
EXIT_KPI_COLLECTION_DURATION. For example if there is no traffic running, it
makes little sense to wait before collecting the KPI values.
So if there is no traffic and no wait should be done then set this parameter to
the value 'yes'.
By default we always wait if KPI counters should be collected at entry and exit
KPI checks.
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
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'UPGRADE_KPI_COLLECTION_RESOLUTION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "15",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the amount of time in seconds between each KPI statistics during the
upgrade.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'USERNAME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the user name to use to access EVNFM, this parameter is mandatory unless
the network configuration file parameter 'evnfm_username' is filled with a valid
value.
For example: "vnfm-user"
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

This Playlist Upgrades the SC application using EVNFM.

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
