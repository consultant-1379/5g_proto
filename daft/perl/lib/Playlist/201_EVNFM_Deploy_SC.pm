package Playlist::201_EVNFM_Deploy_SC;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.42
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
use Playlist::905_Deploy_Preprocess;
use Playlist::906_Deploy_Postprocess;
use Playlist::914_Collect_Logs;
use Playlist::920_Check_Network_Config_Information;
use Playlist::922_Handle_Software_Directory_Files;
use Playlist::924_Prepare_Deploy_Upgrade;
use Playlist::927_Deploy_Upgrade_CRDS;
use Playlist::929_Handle_Secrets;
use Playlist::930_Prepare_Secret_Files;
use Playlist::931_Check_Resource_Usage;
use Playlist::934_Prepare_Value_Files;

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
    $::JOB_PARAMS{'JOBTYPE'} = "DEPLOY";

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
    General::State_Machine::always_execute_task("Playlist::201_Deploy_SC_Via_EVNFM::.*List_Packages_P201S06T98");

    # Hard code some variables that does not need to be set by the user but required to be set for some playlists
    $::JOB_PARAMS{'USING_EVNFM'} = "yes";

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P201S01, \&Fallback001_P201S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P201S02, \&Fallback001_P201S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Configuration_Files_If_Specified_P201S03, \&Fallback001_P201S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Global_Preprocessing_P201S04, \&Fallback001_P201S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Deploy_Preprocessing_P201S05, \&Fallback001_P201S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Deploy_SC_Via_EVNFM_P201S06, \&Fallback001_P201S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Deploy_Postprocessing_P201S07, \&Fallback001_P201S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Global_Postprocessing_P201S08, \&Fallback001_P201S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Collect_Logs_P201S09, \&Fallback001_P201S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P201S10, \&Fallback001_P201S99 );
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
sub Initialize_Job_Environment_P201S01 {

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
sub Check_Job_Parameters_P201S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_Part1_P201S02T01 } );
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

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_Part2_P201S02T02 } );
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
    sub Check_Job_Parameters_Part1_P201S02T01 {

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
    sub Check_Job_Parameters_Part2_P201S02T02 {

        my $clustername = "";
        my $ingress_host_address = "";
        my $password = "";
        my $rc = 0;
        my $username = "";

        # Get the proper CLUSTERNAME and CONFIG_FILE_INSTANTIATE_VNF_REQUEST
        General::Logging::log_user_message("Checking Job parameter 'CLUSTERNAME' and 'CONFIG_FILE_INSTANTIATE_VNF_REQUEST' and Network Config parameter 'evnfm_clustername'");
        if ($::JOB_PARAMS{'CLUSTERNAME'} ne "") {
            $clustername = $::JOB_PARAMS{'CLUSTERNAME'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'CLUSTERNAME'} = $::NETWORK_CONFIG_PARAMS{'evnfm_clustername'}{'value'};
                $clustername = $::JOB_PARAMS{'CLUSTERNAME'};
            } elsif ($::JOB_PARAMS{'CONFIG_FILE_INSTANTIATE_VNF_REQUEST'} eq "") {
                General::Logging::log_user_error_message("Network Config parameter 'evnfm_clustername' has not been set and Job parameter 'CLUSTERNAME' or 'CONFIG_FILE_INSTANTIATE_VNF_REQUEST' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter'CLUSTERNAME' nor 'CONFIG_FILE_INSTANTIATE_VNF_REQUEST' or Network Config parameter 'evnfm_clustername' has been set");
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
                $::JOB_PARAMS{'CONFIG_FILE_INSTANTIATE_VNF_REQUEST'} ne "" ? $::JOB_PARAMS{'CONFIG_FILE_INSTANTIATE_VNF_REQUEST'} : "",
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
sub Check_Configuration_Files_If_Specified_P201S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::930_Prepare_Secret_Files::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::934_Prepare_Value_Files::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Prepare_Instantiate_VNF_Request_File_P201S03T01 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_RESOURCE_CHECK'} eq "no") {

        # Setup variables for the sub-playlist
        $::JOB_PARAMS{'P931_COLLECT_RESOURCE_USAGE'} = "yes";
            # Output files
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_before_deployment.txt";
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_CSV'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_before_deployment.csv";
        $::JOB_PARAMS{'P931_CALCULATE_NEEDED_RESOURCES'} = "yes";
            # Output file
            $::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_requirement_for_deployment.txt";
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
        $::JOB_PARAMS{'P931_CHECK_REQUIRED_RESOURCES'} = "yes";
            # Output file
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_CHECK'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_check_before_deployment.txt";
            # Input files
            $::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_requirement_for_deployment.txt";
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_before_deployment.txt";
        # Call the sub-playlist
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::931_Check_Resource_Usage::main } );
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
    sub Prepare_Instantiate_VNF_Request_File_P201S03T01 {

        my $parameter = "CONFIG_FILE_INSTANTIATE_VNF_REQUEST";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/instantiateVnfRequest.json";
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
                    "filename"              => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/instantiateVnfRequest.json",
                    "output-ref"            => [
                        "{",
                        "    \"clusterName\" : \"$::JOB_PARAMS{'CLUSTERNAME'}\",",
                        "    \"additionalParams\" : {",
                        "        \"namespace\" : \"$::JOB_PARAMS{'SC_NAMESPACE'}\",",
                        "        \"applicationTimeout\" : \"1500\",",
                        "        \"commandTimeOut\" : \"1000\",",
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
sub Global_Preprocessing_P201S04 {

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
sub Deploy_Preprocessing_P201S05 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::924_Prepare_Deploy_Upgrade::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::905_Deploy_Preprocess::main } );
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
sub Deploy_SC_Via_EVNFM_P201S06 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::929_Handle_Secrets::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Session_P201S06T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&List_Packages_P201S06T98 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_If_Package_Exists_P201S06T02 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'PACKAGE_ID'} eq "") {
        # The package has not already been onboarded

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Package_P201S06T03 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Onboard_Package_P201S06T04 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Monitor_Onboard_Status_P201S06T05 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_Package_VNFDID_P201S06T06 } );
        return $rc if $rc < 0;

    }

    if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_CRD'} eq "no") {

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::927_Deploy_Upgrade_CRDS::main } );
        return $rc if $rc < 0;

    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_VNF_Instance_Id_P201S06T07 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Instantiate_VNF_P201S06T08 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Monitor_Instantiate_Status_P201S06T09 } );
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
    sub Create_Session_P201S06T01 {

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
    sub Check_If_Package_Exists_P201S06T02 {

        my $packages;
        my $software_version = $::JOB_PARAMS{'SC_RELEASE_VERSION'} . $::JOB_PARAMS{'SC_RELEASE_BUILD'};

        # Initialize the package id
        $::JOB_PARAMS{'PACKAGE_ID'} = "";
        $::JOB_PARAMS{'PACKAGE_VNFDID'} = "";

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
                    $::JOB_PARAMS{'PACKAGE_ID'} = $key;
                    $::JOB_PARAMS{'PACKAGE_VNFDID'} = $packages->{$key}{"vnfdId"} if (exists $packages->{$key}{"vnfdId"});
                    General::Logging::log_user_message("PACKAGE_ID = $::JOB_PARAMS{'PACKAGE_ID'}\nPACKAGE_VNFDID = $::JOB_PARAMS{'PACKAGE_VNFDID'}\n");
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
    sub Create_Package_P201S06T03 {

        General::Logging::log_user_message("Creating a package");
        $::JOB_PARAMS{'PACKAGE_ID'} = ADP::EVNFM_Operations::create_vnf_package_id(
            {
                "data"                  => '{"userDefinedData":{"description": "Package loaded by DAFT"}}',
                "debug-messages"        => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "hide-output"           => 1,
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
            }
        );

        if ($::JOB_PARAMS{'PACKAGE_ID'} ne "") {
            General::Logging::log_user_message("PACKAGE_ID = $::JOB_PARAMS{'PACKAGE_ID'}");
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
    sub Onboard_Package_P201S06T04 {

        my $csar_file = "$::JOB_PARAMS{'SOFTWARE_DIR_NAME'}/$::JOB_PARAMS{'SOFTWARE_FILE_NAME'}";
        my $rc;

        General::Logging::log_user_message("Onboarding PACKAGE_ID $::JOB_PARAMS{'PACKAGE_ID'}\nThis will take a while to complete.\n");
        my $rc = ADP::EVNFM_Operations::onboard_vnf_package(
            {
                "debug-messages"        => 1,
                "file"                  => $csar_file,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "hide-output"           => 1,
                "no-monitoring"         => 1,
                "package-id"            => $::JOB_PARAMS{'PACKAGE_ID'},
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
    sub Monitor_Onboard_Status_P201S06T05 {

        my $rc;

        General::Logging::log_user_message("Monitoring onboarding status for max 1 hour.\nThis will take a while to complete.\n");
        $rc = ADP::EVNFM_Operations::check_onboarding_status(
            {
                "debug-messages"        => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "hide-output"           => 1,
                "monitor-timeout"       => 3600,
                "package-id"            => $::JOB_PARAMS{'PACKAGE_ID'},
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
    sub Get_Package_VNFDID_P201S06T06 {

        my $packages;
        my $package_id = $::JOB_PARAMS{'PACKAGE_ID'},

        General::Logging::log_user_message("Get VNF package information");
        $packages = ADP::EVNFM_Operations::get_vnf_package_info(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "package-id"            => $::JOB_PARAMS{'PACKAGE_ID'},
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
            }
        );
        if (ref($packages) eq "HASH") {
            # HASH Reference
            if (exists $packages->{$package_id} && exists $packages->{$package_id}{"vnfdId"}) {
                $::JOB_PARAMS{'PACKAGE_VNFDID'} = $packages->{$package_id}{"vnfdId"};
            } else {
                General::Logging::log_user_error_message("Could not find the 'vnfdId' value for package $::JOB_PARAMS{'PACKAGE_ID'}");
                return 1;
            }
        } else {
            General::Logging::log_user_message("Either no packages to print or something went wrong when fetching package information");
            return 1;
        }

        General::Logging::log_user_message("PACKAGE_VNFDID = $::JOB_PARAMS{'PACKAGE_VNFDID'}");

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
    sub Create_VNF_Instance_Id_P201S06T07 {

        my $packages;

        General::Logging::log_user_message("Create VNF instance");
        $::JOB_PARAMS{'INSTANCE_ID'} = ADP::EVNFM_Operations::create_vnf_instance_id(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "instance-name"         => $::JOB_PARAMS{'SC_RELEASE_NAME'},
                "package-vnfdid"        => $::JOB_PARAMS{'PACKAGE_VNFDID'},
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
            }
        );

        if ($::JOB_PARAMS{'INSTANCE_ID'} ne "") {
            General::Logging::log_user_message("INSTANCE_ID = $::JOB_PARAMS{'INSTANCE_ID'}");
            push @::JOB_STATUS, "(/) Create VNF Instance Id successful";
            return 0;
        } else {
            General::Logging::log_user_error_message("Failed to create a package");
            push @::JOB_STATUS, "(x) Create VNF Instance Id failed";
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
    sub Instantiate_VNF_P201S06T08 {

        my $url;

        General::Logging::log_user_message("Instantiate SC VNF");
        $::JOB_PARAMS{'INSTANTIATE_JOB_URL'} = ADP::EVNFM_Operations::instantiate_vnf(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "ingress-host-address"  => $::JOB_PARAMS{'INGRESS_HOST_ADDRESS'},
                "instance-id"           => $::JOB_PARAMS{'INSTANCE_ID'},
                "instantiate-file"      => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/instantiateVnfRequest.json",
                "no-monitoring"         => 1,
                "progress-messages"     => 1,
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
                "values-file"           => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sc-values.yaml",
            }
        );

        if ($::JOB_PARAMS{'INSTANTIATE_JOB_URL'} ne "") {
            General::Logging::log_user_message("Instantiation was successfully started");
            push @::JOB_STATUS, "(/) Instanciate VNF started";
            return 0;
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Instantiation failed");
            push @::JOB_STATUS, "(x) Instanciate VNF failed";
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
    sub Monitor_Instantiate_Status_P201S06T09 {

        my $rc;

        General::Logging::log_user_message("Monitoring instantiation status for max 1 hour.\nThis will take a while to complete.\n");
        $rc = ADP::EVNFM_Operations::check_operation_status(
            {
                "debug-messages"        => 1,
                "hide-output"           => 1,
                "monitor-timeout"       => 3600,
                "progress-messages"     => 1,
                "session-id"            => $::JOB_PARAMS{'SESSION_ID'},
                "type"                  => "instantiation",
                "url"                   => $::JOB_PARAMS{'INSTANTIATE_JOB_URL'},
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message("Instantiation was successful");
            push @::JOB_STATUS, "(/) Instantiate VNF successful";
            return 0;
        } else {
            General::Logging::log_user_error_message("Instantiation failed");
            push @::JOB_STATUS, "(x) Instantiate VNF failed";
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
    sub List_Packages_P201S06T98 {

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
sub Deploy_Postprocessing_P201S07 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::906_Deploy_Postprocess::main, "dry-run-job" => $::JOB_PARAMS{'DRY_RUN'} } );
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
sub Global_Postprocessing_P201S08 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::904_Global_Postprocess::main, "dry-run-job" => $::JOB_PARAMS{'DRY_RUN'} } );
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
sub Collect_Logs_P201S09 {

    my $rc = 0;

    if ($::JOB_PARAMS{'SKIP_RESOURCE_CHECK'} eq "no") {

        # Setup variables for the sub-playlist
        $::JOB_PARAMS{'P931_COLLECT_RESOURCE_USAGE'} = "yes";
            # Output files
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_after_deployment.txt";
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_CSV'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_after_deployment.csv";
        $::JOB_PARAMS{'P931_COMPARE_RESOURCE_USAGE'} = "yes";
            # Output file
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_DIFFERENCES'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_differences_after_deployment.txt";
            # Input files
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_CHECK'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_check_before_deployment.txt";
            $::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_requirement_for_deployment.txt";
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_BEFORE'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_before_deployment.txt";
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_AFTER'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_after_deployment.txt";
        # Call the sub-playlist
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::931_Check_Resource_Usage::main } );
        return $rc if $rc < 0;

    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Collect_Certificate_Validity_Information_After_Deployment_P201S09T01 } );
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
    sub Collect_Certificate_Validity_Information_After_Deployment_P201S09T01 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/parse_certificate_validity_info.pl";
        my $command_parameters = "--output-tsv-data --namespace=$::JOB_PARAMS{'SC_NAMESPACE'} --output-file=$::JOB_PARAMS{'_JOB_LOG_DIR'}/certificate_validity_info_after_deployment.tsv";
        my $rc;
        my @result;

        General::Logging::log_user_message("Print certificate validity information after the deployment");
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
sub Cleanup_Job_Environment_P201S10 {

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
sub Fallback001_P201S99 {

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
job parameter 'CONFIG_FILE_INSTANTIATE_VNF_REQUEST'.
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
        'CONFIG_FILE_BRAGENT_SECRET' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials for SC Release Backup Export using SFTP.

Suggested filename: eric-sc-bragent-secret.yaml

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_BSF_DB_SECRET' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials required for accessing Wide Column Database CD Service.

Suggested filename: bsf-db-secret.yaml

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_DOCUMENT_DATABASE_PG_SECRET' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials required for accessing Document Database PG Service.

Suggested filename: document_database_pg_secret.yaml

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_DISTRIBUTED_COORDINATOR_ED_SC_SECRET' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials required for accessing Distributed Coordinator ED SC Service.

Suggested filename: distributed_coordinator_ed_sc_secret.yaml

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_DISTRIBUTED_COORDINATOR_ED_SECRET' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials required for accessing Distributed Coordinator ED Service.

Suggested filename: distributed_coordinator_ed_secret.yaml

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_EMBEDDED_CNOM_ACCESS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials required for accessing Embedded CNOM Service.

Suggested filename: eric-sc-oam-user-secret.yaml

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_HELM_CHART' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds the Deployment parameters.
Deployment of the SC application requires the setting of certain configuration
parameters. It is mandatory to set some of them at first installation.
The rest, optional parameters, can be configured at a later stage, through the
upgrade procedure. Those optional parameters which are not explicitly set,
acquire the default value indicated.

Suggested filename: eric-sc-values.yaml

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.

If this parameter is not specified then a deployment parameters file will be
automatically generated from the eric-sc-values-xxxx.yaml file included in the
software package with some values replaced by values taken from the network
configuration file.

If multiple files with deployment parameters should be added after the main
file then just add job parameters CONFIG_FILE_HELM_CHART_1, CONFIG_FILE_HELM_CHART_2
etc.
These files will be added in the numbered order of the job parameters.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_INSTANTIATE_VNF_REQUEST' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds the Instantiation parameters.
Instantiation of the SC application via EVNFM requires the setting of certain
configuration parameters.

Suggested filename: instantiateVnfRequest.json

For detailed information please see document "XXXXX' in chapter
'XXXXXX'.

If this parameter is not specified then an instantiation parameters file will be
automatically generated from the job parameters and/or network configuration file
parameters.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_LDAP_SECRET' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials for LDAP admin user.
This is the first user that will be automatically created during the deployment.
The admin-user is pre-configured with the Local Authentication Administrator
access rules, and can be used to create additional users, for operating on the
YANG data models through the Configuration Management interface.

Suggested filename: ldap-secret.yaml

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_MONITOR_SECRET' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <diectory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials required for accessing SC Monitor Service.

Suggested filename: monitor-secret.yaml

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_PVTB_CI_SECRET' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials required for PVTB CI.

Suggested filename: pvtb-ci-secret.yaml

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_SFTP_SERVER_SECRET' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials for SFTP Server Configuration.

Suggested filename: sftp_Config.json

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_SNMP_SECRET' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials for FM SNMP and SNMP Configuration.

Suggested filename: config.json

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_TAP_CONFIG_MAP' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters for enabling or
disabling the vTAP functionality of the SC node.

Suggested filename: tap_config.json

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_WCDBCD_ADMIN_SECRET' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials required for accessing Wide Column Database CD Service.

Suggested filename: wcdbcd-admin-secret.yaml

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_WCDBCD_DAY0_CERTIFICATE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with Day-0
certificates for Geographical Redundancy setup.

Suggested filename: eric-sec-certm-deployment-configuration.json

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
See also parameter GEOGRAPHICAL_REDUNDANCY.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_FILE_WCDBCD_DAY0_SECRET' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials required for accessing Wide Column Database CD Service.

Suggested filename: wcdbcd-day0-secret.yaml

For detailed information please see document "Deploy the SC Manually' in chapter
'Prepare SC Deployment'.
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
Specify the Containerized Network Functions (CNF) that should be enabled on the
deployed software.

If more than one CNF should be enabled then separate each CNF with either a
dash (-), comma (,) or a space ( ).
For example: "bsf-wcdb", "bsf,wcdb" or "bsf wcdb".

If ENABLED_CNF is set to a non-empty value then it will modify the respective
global.ericsson.XXXX.enabled parameter for these CNF's and set the value to
'true' and all other CNF's will be set to 'false' value in the generated
eric-sc-values.yaml file which is used for the deployment.

This parameter has no effect if the user specifies their own eric-sc-values.yaml
file with the CONFIG_FILE_HELM_CHART job parameter.

If neither CONFIG_FILE_HELM_CHART nor ENABLED_CNF job parameters are specified
then the deployed CNF's will be according to the settings in the network
configuration file parameters containing global.ericsson.XXXX.enabled strings.

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
        'GEOGRAPHICAL_REDUNDANCY' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the node is used in a Geographical Redundancy setup
with multiple sites and controls if day-0 certificates for BSF/WCDB is being
generated and/or installed.
This parameter is only being checked if the parameter ENABLED_CNF contains
'wcdb' and if the deployed release is SC 1.4 or higher.

If value is 'no' which is also the default value then no day-0 BSF/WCDB
certificates is being installed.

If value is 'yes' and BSF and WCDB is deployed then day-0 certificate will
be created and/or installed.
See also parameter CONFIG_FILE_WCDBCD_DAY0_CERTIFICATE.
EOF
            'validity_mask' => '(yes|no)',
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
            'default_value' => "900",
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

This Playlist Deploys the SC application using EVNFM.

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
