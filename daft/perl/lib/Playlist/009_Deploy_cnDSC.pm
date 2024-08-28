package Playlist::009_Deploy_cnDSC;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.23
#  Date     : 2024-05-07 13:49:14
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

use ADP::Kubernetes_Operations;
use General::Data_Structure_Operations;
use General::File_Operations;
use General::Json_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;
use General::State_Machine;
use General::Yaml_Operations;

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::903_Global_Preprocess;
use Playlist::904_Global_Postprocess;
use Playlist::905_Deploy_Preprocess;
use Playlist::906_Deploy_Postprocess;
use Playlist::914_Collect_Logs;
use Playlist::916_Compare_CRD;
use Playlist::919_Check_Registry_Information;
use Playlist::920_Check_Network_Config_Information;
use Playlist::922_Handle_Software_Directory_Files;
use Playlist::924_Prepare_Deploy_Upgrade;
use Playlist::926_Handle_Docker_Images;
use Playlist::927_Deploy_Upgrade_CRDS;
use Playlist::928_Verify_Deployment;
use Playlist::929_Handle_Secrets;
use Playlist::930_Prepare_Secret_Files;
use Playlist::931_Check_Resource_Usage;
use Playlist::934_Prepare_Value_Files;
use Playlist::935_Deploy_Upgrade_Application;

#
# Variable Declarations
#

my $debug_command = "";

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
    $::JOB_PARAMS{'JOBTYPE'} = "DEPLOY";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    } else {
        $debug_command = "";
    }

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

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P009S01, \&Fallback001_P009S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P009S02, \&Fallback001_P009S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Configuration_Files_If_Specified_P009S03, \&Fallback001_P009S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Global_Preprocessing_P009S04, \&Fallback001_P009S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Deploy_Preprocessing_P009S05, \&Fallback001_P009S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Deploy_P009S06, \&Fallback001_P009S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Deploy_Postprocessing_P009S07, \&Fallback001_P009S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Global_Postprocessing_P009S08, \&Fallback001_P009S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Collect_Logs_P009S09, \&Fallback001_P009S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P009S10, \&Fallback001_P009S99 );
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
sub Initialize_Job_Environment_P009S01 {

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
sub Check_Job_Parameters_P009S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P009S02T01 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_APPLICATION'} eq "no" && $::JOB_PARAMS{'USING_EVNFM'} eq "no") {
        # Extract and check the files from the software csar file, the tools package and value files
        $::JOB_PARAMS{'CHECK_CSAR_FILE'}            = "yes";
        $::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'}    = "yes";
        $::JOB_PARAMS{'CHECK_TOOLS_FILE'}           = "yes";
        $::JOB_PARAMS{'CHECK_TOOLS_FILE_CONTENT'}   = "yes";
        $::JOB_PARAMS{'CHECK_VALUES_FILE'}          = "yes";
        $::JOB_PARAMS{'COPY_UMBRELLA_FILE'}         = "yes";
        $::JOB_PARAMS{'COPY_VALUES_FILE'}           = "yes";
        $::JOB_PARAMS{'UNPACK_CSAR_FILE'}           = "yes";
        $::JOB_PARAMS{'UNPACK_TOOLS_FILE'}          = "yes";
        $::JOB_PARAMS{'UNPACK_UMBRELLA_FILE'}       = "yes";
    } elsif ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_CRD'} eq "no") {
        # Extract and check the files from the software csar file and the tools package, no need to copy the umbrella files
        $::JOB_PARAMS{'CHECK_CSAR_FILE'}            = "yes";
        $::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'}    = "yes";
        $::JOB_PARAMS{'CHECK_TOOLS_FILE'}           = "yes";
        $::JOB_PARAMS{'CHECK_TOOLS_FILE_CONTENT'}   = "yes";
        $::JOB_PARAMS{'CHECK_VALUES_FILE'}          = "yes";
        $::JOB_PARAMS{'COPY_UMBRELLA_FILE'}         = "no";
        $::JOB_PARAMS{'COPY_VALUES_FILE'}           = "no";
        $::JOB_PARAMS{'UNPACK_CSAR_FILE'}           = "yes";
        $::JOB_PARAMS{'UNPACK_TOOLS_FILE'}          = "yes";
        $::JOB_PARAMS{'UNPACK_UMBRELLA_FILE'}       = "no";
    } else {
        # Check the files from the software csar file and the tools package, no need to unpack and check file contents
        $::JOB_PARAMS{'CHECK_CSAR_FILE'}            = "yes";
        $::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'}    = "no";
        $::JOB_PARAMS{'CHECK_TOOLS_FILE'}           = "yes";
        $::JOB_PARAMS{'CHECK_TOOLS_FILE_CONTENT'}   = "no";
        $::JOB_PARAMS{'CHECK_VALUES_FILE'}          = "yes";
        $::JOB_PARAMS{'COPY_UMBRELLA_FILE'}         = "no";
        $::JOB_PARAMS{'COPY_VALUES_FILE'}           = "no";
        $::JOB_PARAMS{'UNPACK_CSAR_FILE'}           = "no";
        $::JOB_PARAMS{'UNPACK_TOOLS_FILE'}          = "no";
        $::JOB_PARAMS{'UNPACK_UMBRELLA_FILE'}       = "no";
    }
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::922_Handle_Software_Directory_Files::main } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'P920_TASK'} = "READ_PARAMETERS_FROM_FILE";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::920_Check_Network_Config_Information::main } );
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
    sub Check_Job_Parameters_P009S02T01 {

        my $crd_namespace = "";
        my $crd_release_name = "";
        my $sc_namespace = "";
        my $sc_release_name = "";
        my $rc = 0;
        my @result;

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

        General::Logging::log_user_message("SC_NAMESPACE=$sc_namespace\nSC_RELEASE_NAME=$sc_release_name\nCRD_NAMESPACE=$crd_namespace\nCRD_RELEASE_NAME=$crd_release_name\nSKIP_DEPLOY_UPGRADE_APPLICATION=$::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_APPLICATION'}\nSKIP_DEPLOY_UPGRADE_CRD=$::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_CRD'}\n");

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
sub Check_Configuration_Files_If_Specified_P009S03 {

    my $rc = 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::930_Prepare_Secret_Files::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::934_Prepare_Value_Files::main } );
    return $rc if $rc < 0;

    # Check if resource usage check should be performed
    if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_APPLICATION'} eq "no" && $::JOB_PARAMS{'USING_EVNFM'} eq "no" && $::JOB_PARAMS{'SKIP_RESOURCE_CHECK'} eq "no") {

        # Mark that current resource usage should be fetched from the cluster.
        $::JOB_PARAMS{'P931_COLLECT_RESOURCE_USAGE'} = "yes";
            # Output files
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_before_deployment.txt";
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_CSV'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_before_deployment.csv";


        # Mark that resource needs for the application to be deployed shall be calculated/estimated.
        $::JOB_PARAMS{'P931_CALCULATE_NEEDED_RESOURCES_MULTIPLE_FILES'} = "yes";
            # Output file
            $::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_requirement_for_deployment.txt";
            # Input files
            my $filecnt = 1;
            if ($::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} ne "" && $::JOB_PARAMS{'VALUES_FILE_NAME'} ne "") {
                if ($::JOB_PARAMS{'CLOUD_NATIVE_BASE_VALUES_FILE'} ne "") {
                    $::JOB_PARAMS{"P931_FILENAME_PAIR_$filecnt"} = "$::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'},$::JOB_PARAMS{'CLOUD_NATIVE_BASE_VALUES_FILE'},$::JOB_PARAMS{'VALUES_FILE_NAME'},";
                } else {
                    $::JOB_PARAMS{"P931_FILENAME_PAIR_$filecnt"} = "$::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'},$::JOB_PARAMS{'VALUES_FILE_NAME'},";
                }
                my @files = General::File_Operations::find_file(
                    {
                        "directory" => $::JOB_PARAMS{'_JOB_CONFIG_DIR'},
                        "filename"  => "eric-sc-values*.yaml",
                    }
                );
                for (sort @files) {
                    if (/^.+\/eric-(sc-values-\d+)\.yaml/) {
                        $::JOB_PARAMS{"P931_FILENAME_PAIR_$filecnt"} .= "$_,";
                    }
                }
                $::JOB_PARAMS{"P931_FILENAME_PAIR_$filecnt"} =~ s/,$//;
                $filecnt++;
            }
            if ($::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} ne "" && $::JOB_PARAMS{'VALUES_FILE_NAME'} ne "") {
                if ($::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_VALUES_FILE'} ne "") {
                    $::JOB_PARAMS{"P931_FILENAME_PAIR_$filecnt"} = "$::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'},$::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_VALUES_FILE'},$::JOB_PARAMS{'VALUES_FILE_NAME'},";
                } else {
                    $::JOB_PARAMS{"P931_FILENAME_PAIR_$filecnt"} = "$::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'},$::JOB_PARAMS{'VALUES_FILE_NAME'},";
                }
                my @files = General::File_Operations::find_file(
                    {
                        "directory" => $::JOB_PARAMS{'_JOB_CONFIG_DIR'},
                        "filename"  => "eric-sc-values*.yaml",
                    }
                );
                for (sort @files) {
                    if (/^.+\/eric-(sc-values-\d+)\.yaml/) {
                        $::JOB_PARAMS{"P931_FILENAME_PAIR_$filecnt"} .= "$_,";
                    }
                }
                $::JOB_PARAMS{"P931_FILENAME_PAIR_$filecnt"} =~ s/,$//;
                $filecnt++;
            }
            if ($::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} ne "" && $::JOB_PARAMS{'VALUES_FILE_NAME'} ne "") {
                # CNDSC seems to have multiple application umbrella file: e.g. eric-dsc-1.11.2-1-hd026c7a.tgz and eric-sc-cs-1.4.0.tgz
                # so we need to split them up into separate P931_FILENAME_PAIT_xxx variables
                my @files = General::File_Operations::find_file(
                    {
                        "directory" => $::JOB_PARAMS{'_JOB_CONFIG_DIR'},
                        "filename"  => "eric-sc-values*.yaml",
                    }
                );
                for my $application_umbrella_file (split /\|/, $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'}) {
                    $::JOB_PARAMS{"P931_FILENAME_PAIR_$filecnt"} = "$application_umbrella_file,$::JOB_PARAMS{'VALUES_FILE_NAME'},";
                    for (sort @files) {
                        if (/^.+\/eric-(sc-values-\d+)\.yaml/) {
                            $::JOB_PARAMS{"P931_FILENAME_PAIR_$filecnt"} .= "$_,";
                        }
                    }
                    $::JOB_PARAMS{"P931_FILENAME_PAIR_$filecnt"} =~ s/,$//;
                    $filecnt++;
                }
            } elsif ($::JOB_PARAMS{'SC_UMBRELLA_FILE'} ne "" && $::JOB_PARAMS{'VALUES_FILE_NAME'} ne "") {
                $::JOB_PARAMS{"P931_FILENAME_PAIR_$filecnt"} = "$::JOB_PARAMS{'SC_UMBRELLA_FILE'},$::JOB_PARAMS{'VALUES_FILE_NAME'},";
                my @files = General::File_Operations::find_file(
                    {
                        "directory" => $::JOB_PARAMS{'_JOB_CONFIG_DIR'},
                        "filename"  => "eric-sc-values*.yaml",
                    }
                );
                for (sort @files) {
                    if (/^.+\/eric-(sc-values-\d+)\.yaml/) {
                        $::JOB_PARAMS{"P931_FILENAME_PAIR_$filecnt"} .= "$_,";
                    }
                }
                $::JOB_PARAMS{"P931_FILENAME_PAIR_$filecnt"} =~ s/,$//;
                $filecnt++;
            } else {
                General::Logging::log_user_error_message("No helm chart file found for the application");
                return 1;
            }

        # Mark that a check if needed resources for the deployment shall be checked against available resources.
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
sub Global_Preprocessing_P009S04 {

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
sub Deploy_Preprocessing_P009S05 {

    my $rc;

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
sub Deploy_P009S06 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Show_Tasks_To_Be_Done_P009S06T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::924_Prepare_Deploy_Upgrade::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::926_Handle_Docker_Images::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::929_Handle_Secrets::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::927_Deploy_Upgrade_CRDS::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::935_Deploy_Upgrade_Application::main } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'CHECK_DEPLOYMENT_STATUS_AFTER_EACH_HELM_OPERATION'} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::928_Verify_Deployment::main } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'TIMESHIFT_VALID_LIFETIME_SECONDS'} >= 315576000) {
        # Maximum allowed validity, now patch and remove any overrideTtl values to make sure most certificates
        # have the maximum allowed validity.
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Patch_And_Remove_OverrideTtl_If_Needed_P009S06T02 } );
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
    sub Show_Tasks_To_Be_Done_P009S06T01 {

        my $message = "";
        my $rc = 0;

        General::Message_Generation::print_message_box(
            {
                "header-text"   => "Deployment",
                "messages"      => [
                    "",
                    "Tasks to be done:",
                    "- Load Docker Images",
                    "- Prepare SC Deployment",
                    "- Install Secrets",
                    "- Deploy or Upgrade CRD Software",
                    "- Deploy SC Software",
                    "",
                ],
                "align-text"    => "left",
                "return-output" => \$message,
            }
        );
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
    sub Patch_And_Remove_OverrideTtl_If_Needed_P009S06T02 {

        my $command;
        my $found = 0;
        my $certificate;
        my %patch_certificate;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Check if any internal certificates needs to be patched (overrideTtl < 315576000).\n");
        General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get internalcertificates -n $::JOB_PARAMS{'SC_NAMESPACE'} -o json | grep -iP '\"(name|overrideTtl)\":'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        for (@result) {
            if (/^\s*"name":\s*"(\S+)"/i) {
                $certificate = $1;
            } elsif (/^\s*"overrideTtl":\s*(\d+)/i) {
                if ($1 < 315576000) {
                    # The value is less than about 10 years
                    $patch_certificate{$certificate} = $1;
                }
            }
        }

        if (%patch_certificate) {
            for $certificate (sort keys %patch_certificate) {
                General::Logging::log_user_message("Patching internal certificate '$certificate' by removing 'overrideTtl: $patch_certificate{$certificate}'.\n");
                $command = "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} patch internalcertificates $certificate -n $::JOB_PARAMS{'SC_NAMESPACE'} -p '{ \"spec\": {\"certificate\": {\"validity\": null}}}' --type merge";
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => $command,
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc == 0) {
                    # Check if any certificates were patched
                    for (@result) {
                        if (/^\S+\s+patched\s*$/) {
                            #internalcertificate.siptls.sec.ericsson.com/eric-bsf-manager-server-certificate patched
                            #  or: that will not be matched
                            #internalcertificate.siptls.sec.ericsson.com/eric-bsf-manager-server-certificate patched (no change)
                            $found++;
                        }
                    }
                } else {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to execute the command:\n$command");
                    return $rc;
                }
            }

            if ($found > 0) {
                General::Logging::log_user_message("$found certificates patched, now waiting for the certificates to get renewed.\nCurrently this is a hard coded wait that can be interrupted with CTRL-C if you know all certificates has been renewed or don't care to wait.\n");
                $rc = General::OS_Operations::sleep_with_progress(
                    {
                        "allow-interrupt"   => 1,
                        "progress-message"  => 1,
                        "seconds"           => 600,
                        "use-logging"       => 1,
                    }
                );
                if ($rc == 1) {
                    # User pressed CTRL-C to interrupt the wait
                    General::Logging::log_user_warning_message("CTRL-C pressed to interrupt the wait");
                    $rc = 0;
                }
            } else {
                General::Logging::log_user_warning_message("No internal certificates patched.");
            }
        } else {
            General::Logging::log_user_message("No internal certificates need patching.\n");
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
sub Deploy_Postprocessing_P009S07 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::906_Deploy_Postprocess::main } );
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
sub Global_Postprocessing_P009S08 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::904_Global_Postprocess::main } );
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
sub Collect_Logs_P009S09 {

    my $rc = 0;

    if ($::JOB_PARAMS{'SKIP_RESOURCE_CHECK'} eq "no") {

        # Setup variables for the sub-playlist
        $::JOB_PARAMS{'P931_COLLECT_RESOURCE_USAGE'} = "yes";
            # Output files
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_after_deployment.txt";
            $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_CSV'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_after_deployment.csv";
        if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_APPLICATION'} eq "no" && $::JOB_PARAMS{'USING_EVNFM'} eq "no") {
            $::JOB_PARAMS{'P931_COMPARE_RESOURCE_USAGE'} = "yes";
                # Output file
                $::JOB_PARAMS{'P931_FILENAME_RESOURCE_DIFFERENCES'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_differences_after_deployment.txt";
                # Input files
                $::JOB_PARAMS{'P931_FILENAME_RESOURCE_CHECK'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_check_before_deployment.txt";
                $::JOB_PARAMS{'P931_FILENAME_NEEDED_RESOURCES'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_requirement_for_deployment.txt";
                $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_BEFORE'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_before_deployment.txt";
                $::JOB_PARAMS{'P931_FILENAME_RESOURCE_USAGE_AFTER'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/resource_usage_after_deployment.txt";
        }
        # Call the sub-playlist
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::931_Check_Resource_Usage::main } );
        return $rc if $rc < 0;

    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Collect_Certificate_Validity_Information_After_Deployment_P009S09T01 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'COLLECT_LOGS_AT_SUCCESS'} eq "yes") {
        if ($::JOB_PARAMS{'DRY_RUN'} eq "no") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
            return $rc if $rc < 0;
        } else {
            General::Logging::log_user_message("No reason to collect logs for a DRY_RUN job");
        }
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
    sub Collect_Certificate_Validity_Information_After_Deployment_P009S09T01 {

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
sub Cleanup_Job_Environment_P009S10 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::902_Cleanup_Job_Environment::main } );
    return $rc if $rc < 0;

    if (($rc == 0) && ($::JOB_PARAMS{'DRY_RUN'} eq "no")) {
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
sub Fallback001_P009S99 {

    my $message = "";
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
        'CHECK_DEPLOYMENT_STATUS_AFTER_EACH_HELM_OPERATION' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if checking of the deployment status should be done after
each helm operation has been done i.e. after either a helm install or upgrade
operation. Or if the check will only be done after all helm operations has been
completed.

By checking deployment status after each helm chart operation there might be more
time for the node to recover and less traffic disturbances.

For a deployment, since there is no traffic, so setting this parameter to 'yes'
will probably only increase the time for the deployment and will probably not
add any benefits.
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
        'CONFIG_FILE_DAY0_ADMIN_SECRET' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials and secret name for the Day 0 user secret.

Suggested filename: day0adminsecret.yaml

For detailed information please see document "Deploy the DSC Manually' in chapter
'Prepare DSC Deployment'.
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
Deployment of the cnDSC application requires the setting of certain configuration
parameters. It is mandatory to set some of them at first installation.
The rest, optional parameters, can be configured at a later stage, through the
upgrade procedure. Those optional parameters which are not explicitly set,
acquire the default value indicated.

Suggested filename: eric-dsc-values.yaml

For detailed information please see document "Deploy the DSC Manually' in chapter
'Prepare DSC Deployment'.

If this parameter is not specified then a deployment parameters file will be
automatically generated from the eric-dsc-values-xxxx.yaml file included in the
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

For detailed information please see document "Deploy the DSC Manually' in chapter
'Prepare DSC Deployment'.
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
        'CONFIG_FILE_WCDBCD_DSC_SECRET' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the file that holds all the configuration parameters with User
Credentials required for accessing Wide Column Database CD Service from the DSC.

Suggested filename: dsc-wcdb-secret.yaml.yaml

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
        'DRY_RUN' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should perform a dry run of the installation or
upgrade parts in order to run through and check the environment and requirements.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'FORCE_UPGRADE_APPLICATION' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if Applications should be upgraded even if the installed
version might already be newer.
By default an older file version will never be used for the upgrade if a newer
version is already installed.
But with this parameter set to 'yes' you can override this and still potentially
downgrade to an older version, but choosing this option you better know what you
are doing since it might cause strange problems with installed applications that
might use functionality that might not exist in the version to be upgraded to.

Currently this parameter is only checked when upgrading to a different build number
or name of the same version i.e. where the x.y.z number is the same but the build
name after the sign (+ or -) is different and one of them is not a number.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'FORCE_UPGRADE_CRD' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if CRDS should be upgraded even if the installed version
might already be newer.
By default an older file version will never be used for the upgrade if a newer
version is already installed.
But with this parameter set to 'yes' you can override this and still potentially
downgrade to an older version, but choosing this option you better know what you
are doing since it might cause strange problems with installed applications that
might use functionality that might not exist in the version to be upgraded to.

Currently this parameter is only checked when upgrading to a different build number
or name of the same version i.e. where the x.y.z number is the same but the build
name after the sign (+ or -) is different and one of them is not a number.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'HELM_TIMEOUT_APPLICATION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "1500",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the timeout value to use when deploying the software using the helm
command, if not specified then the default value will be used.

The time value which must be an integer value is given as the number of seconds
to wait for the deployment to finish.
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
        'IGNORE_CRD_CHECK'  => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" which is also the default value if not specified, if the Playlist should
ignore the result of found CRDs on the cluster and perform the upgrade/installation of newer CRDS.

Select "no" to have the Playlist check if CRDs are already installed on the cluster and skip over
the CRD upgrade/installation.
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
The name to use for the Application namespace if you want to override the parameter
from the network configuration file called 'sc_namespace'.
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
The name to use for the Application release name if you want to override the
parameter from the network configuration file called 'sc_release_name'.
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
        'SKIP_DEPLOY_UPGRADE_APPLICATION' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the actual deployment of the Application Helm chart
should be skipped but everything else should be performed like installing or
upgrading CRD and loading of secrets etc.
If the value is set to "yes" then the playlist will skip the deployment of the
Helm chart and checking of Cluster status.

This is a special job parameter that will be used by EVNFM since they want to
do the actual deployment of the Helm chart using their own scripts.

The default if this parameter is not given is to perform the full deployment of
the Application software.
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
Option to skip pre deployemnt/upgrade healthcheck.
Valid values are yes or no (default is no).
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
Option to skip post deployemnt/upgrade healthcheck.
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
        'SKIP_UMBRELLA_VERSION_CHECK' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the playlist should check before upgrading the
application software if the already installed version is the same or newer version
than the package to be upgraded.
By default we skip this check which makes it possible to basically "downgrade"
the software if you try to upgrade with an older version than what's installed.
Change the value to "no" if want to have the check so we don't downgrade the
software.

NOTE: This parameter only affects the application software check, for CRD's the
version check will always be done to make sure we do not downgrade the CRD software.
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
        'TIMESHIFT_VALID_LIFETIME_SECONDS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "0",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Special job variable used for Timeshift and Timebomb testing (DND-24595) where
you can extend the valid lifetime parameter settings for eric-sec-sip-tls
certificates.
If this parameter is set to a value of 315576000 to get a valid lifetime of
the certificates close to 10 years it will add the following parameters to
the 'helm install' command for deploying the SC software:

    --set eric-sec-sip-tls.serverCertificate.validLifetimeSeconds=604800
    --set eric-sec-sip-tls.clientCertificate.validLifetimeSeconds=604800
    --set eric-sec-sip-tls.internalCertificate.validLifetimeSeconds=315576000

Setting it to a higher value than 315576000 will be ignored and you only get the
maximum values mentioned above.

NOTE:
This value should only be changed if you know what you are doing and are testing
time bomb or time shift of the SC software.
It will also require a specially prepared CSAR file where all umbrella files
has been modified to comments out the following lines:

    validity:
    override-ttl:
    overrideTtl:
    overrideLeadTime:

Which can be done by executing the following command:

grep -E 'override.*?tl' * -r -l | xargs -i sed -i -E \\
's/validity:|override-ttl:|overrideTtl:|overrideLeadTime:/#timeshifchangedit:/g' {}

Also the "charts/eric-sec-sip-tls/templates/config-map.yaml" file must be updated
to replace the "role_max_ttl" parameter value from default 604800 (~ 7 days) up
to 315576000 (~10 years). This change is needed on 2 places in the file.

For more information, see:
https://cc-jira.rnd.ki.sw.ericsson.se/browse/DND-24595
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'USING_EVNFM' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the actual deployment is triggered from EVNFM or not
and it changes how DAFT is executing certain tasks.

If the value is set to "yes" then the playlist will apply the following changes
to the task execution:

    - It will unpack only the CRD file from the software csar file if included
      in the file.
    - Loading and tagging CRD container images is skipped if SC release is 1.3.1 or
      newer since EVNFM will take care of onboarding the images.
    - When SC release is 1.2.2 or 1.3.0 where the CRD images is delivered in the
      tools file, DAFT will use the following new network configuration file
      parameters for loading, tagging and installing CRDs to the local registry
      and not the EVNFM registry:
        - private_crd_registry_password
        - private_crd_registry_port
        - private_crd_registry_secret
        - private_crd_registry_url
        - private_crd_registry_user
      For SC release 1.3.1 or newer these parameters can be left to CHANGEME
      or should be set to same values as parameters below.
    - The following network configuration file parameters should contain values
      for the EVNFM registry:
        - private_registry_password
        - private_registry_port
        - private_registry_user
        - eric_sc_values_hash_parameter_global.pullSecret
            or
          eric_sc_values_anchor_parameter_private_registry_secret_name
        - eric_sc_values_hash_parameter_global.registry.url
            or
          eric_sc_values_anchor_parameter_private_registry_url

If value is set to "no" which is also the default then normal playlist execution
will take place and what is installed will depend on the values of the following
job parameters:

    - SKIP_DEPLOY_UPGRADE_CRD
    - SKIP_DEPLOY_UPGRADE_APPLICATION
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

This Playlist script is used for performing Deployment of an DSC node and it
will be called from the execute_playlist.pl script.

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
