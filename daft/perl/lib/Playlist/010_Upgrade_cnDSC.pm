package Playlist::010_Upgrade_cnDSC;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.15
#  Date     : 2024-05-14 10:20:43
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
use Playlist::907_Upgrade_Preprocess;
use Playlist::908_Upgrade_Postprocess;
use Playlist::912_Upgrade;
use Playlist::914_Collect_Logs;
use Playlist::920_Check_Network_Config_Information;
use Playlist::922_Handle_Software_Directory_Files;
use Playlist::924_Prepare_Deploy_Upgrade;
use Playlist::926_Handle_Docker_Images;
use Playlist::927_Deploy_Upgrade_CRDS;
use Playlist::928_Verify_Deployment;
use Playlist::933_Handle_KPI_Statistics;
use Playlist::934_Prepare_Value_Files;
use Playlist::935_Deploy_Upgrade_Application;
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
    General::State_Machine::always_execute_task("Playlist::914_Collect_Logs.+");

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P010S01, \&Fallback001_P010S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P010S02, \&Fallback001_P010S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Configuration_Files_If_Specified_P010S03, \&Fallback001_P010S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&KPI_Entry_Collection_P010S04, \&Fallback001_P010S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Global_Preprocessing_P010S05, \&Fallback001_P010S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Collect_Logs_Before_Upgrade_If_Wanted_P010S06, \&Fallback001_P010S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Upgrade_Preprocessing_P010S07, \&Fallback001_P010S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Upgrade_P010S08, \&Fallback001_P010S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Upgrade_Postprocessing_P010S09, \&Fallback001_P010S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Global_Postprocessing_P010S10, \&Fallback001_P010S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Collect_Logs_If_Wanted_P010S11, \&Fallback001_P010S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&KPI_Upgrade_And_Exit_Collection_P010S12, \&Fallback001_P010S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P010S13, \&Fallback001_P010S99 );
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
sub Initialize_Job_Environment_P010S01 {

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
sub Check_Job_Parameters_P010S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P010S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::936_Check_Deployed_Software::main } );
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
    sub Check_Job_Parameters_P010S02T01 {

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

        # Skip certain tasks when using EVNFM
        if ($::JOB_PARAMS{'USING_EVNFM'} eq "yes") {
            General::Logging::log_user_message("Skipping KPI collection when using EVNFM");
            $::JOB_PARAMS{'SKIP_KPI_COLLECTION'} = "yes";
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
sub Check_Configuration_Files_If_Specified_P010S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::934_Prepare_Value_Files::main } );
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
sub Global_Preprocessing_P010S05 {

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
sub KPI_Entry_Collection_P010S04 {

    my $rc;

    if ($::JOB_PARAMS{'SKIP_KPI_COLLECTION'} eq "no" && $::JOB_PARAMS{'COLLECT_ENTRY_KPI_VALUES'} eq "yes") {

        if ($::JOB_PARAMS{'SKIP_TRAFFIC_DELAY'} eq "no") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Entry_Time_To_Expire_P010S04T01 } );
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

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Record_Upgrade_Start_Time_P010S04T02 } );
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
    sub Wait_For_Entry_Time_To_Expire_P010S04T01 {

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
    sub Record_Upgrade_Start_Time_P010S04T02 {

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
sub Collect_Logs_Before_Upgrade_If_Wanted_P010S06 {

    my $rc = 0;

    if ($::JOB_PARAMS{'COLLECT_LOGS_BEFORE_UPGRADE'} eq "yes") {
        if ($::JOB_PARAMS{'DRY_RUN'} eq "no") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
            return $rc if $rc < 0;
        } else {
            General::Logging::log_user_message("No reason to collect logs for a DRY_RUN job");
        }
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
sub Upgrade_Preprocessing_P010S07 {

    my $rc;

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
sub Upgrade_P010S08 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Show_Tasks_To_Be_Done_P010S08T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::924_Prepare_Deploy_Upgrade::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::926_Handle_Docker_Images::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::927_Deploy_Upgrade_CRDS::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::935_Deploy_Upgrade_Application::main } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'CHECK_DEPLOYMENT_STATUS_AFTER_EACH_HELM_OPERATION'} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::928_Verify_Deployment::main } );
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
    sub Show_Tasks_To_Be_Done_P010S08T01 {

        my $message = "";
        my $rc = 0;

        General::Message_Generation::print_message_box(
            {
                "header-text"   => "Upgrade",
                "messages"      => [
                    "",
                    "Tasks to be done:",
                    "- Load Docker Images",
                    "- Prepare SC Upgrade",
                    "- Deploy or Upgrade CRD Software",
                    "- Upgrade SC Software",
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
sub Upgrade_Postprocessing_P010S09 {

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
sub Global_Postprocessing_P010S10 {

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
sub Collect_Logs_If_Wanted_P010S11 {

    my $rc = 0;

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
sub KPI_Upgrade_And_Exit_Collection_P010S12 {

    my $rc = 0;

    if ($::JOB_PARAMS{'SKIP_KPI_COLLECTION'} eq "no" && $::JOB_PARAMS{'COLLECT_UPGRADE_KPI_VALUES'} eq "yes") {

        $::JOB_PARAMS{'P933_TASK'} = "KPI_VERDICT";
        $::JOB_PARAMS{'P933_RESOLUTION'} = $::JOB_PARAMS{'UPGRADE_KPI_COLLECTION_RESOLUTION'};
        $::JOB_PARAMS{'P933_PAST'} = sprintf '%ds', (time() - $::JOB_PARAMS{'UPGRADE_START_EPOCH'});
        if (exists $::JOB_PARAMS{'ENABLED_CNF'} && $::JOB_PARAMS{'ENABLED_CNF'} ne "" && $::JOB_PARAMS{'ENABLED_CNF'} ne "all") {
            my $description = "(" . uc($::JOB_PARAMS{'ENABLED_CNF'}) . ").+";
            $description =~ s/-/|/g;
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
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Exit_Time_To_Expire_P010S12T01 } );
            return $rc if $rc < 0;
        }

        $::JOB_PARAMS{'P933_TASK'} = "KPI_VERDICT";
        $::JOB_PARAMS{'P933_RESOLUTION'} = $::JOB_PARAMS{'EXIT_KPI_COLLECTION_RESOLUTION'};
        $::JOB_PARAMS{'P933_PAST'} = $::JOB_PARAMS{'EXIT_KPI_COLLECTION_DURATION'};
        if (exists $::JOB_PARAMS{'ENABLED_CNF'} && $::JOB_PARAMS{'ENABLED_CNF'} ne "" && $::JOB_PARAMS{'ENABLED_CNF'} ne "all") {
            my $description = "(" . uc($::JOB_PARAMS{'ENABLED_CNF'}) . ").+";
            $description =~ s/-/|/g;
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
    sub Wait_For_Exit_Time_To_Expire_P010S12T01 {

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
sub Cleanup_Job_Environment_P010S13 {

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
sub Fallback001_P010S99 {

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
            'default_value' => "yes",
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

For an upgrade setting this parameter to 'no' might make the upgrade a little bit
faster but might cause more traffic disturbances.
EOF
            'validity_mask' => '(yes|no)',
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
        'CONFIG_FILE_HELM_CHART' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",       # <yes|no>
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
'Prepare SC Deployment'.

If multiple files with deployment parameters should be added after the main
file then just add job parameters CONFIG_FILE_HELM_CHART_1, CONFIG_FILE_HELM_CHART_2
etc.
These files will be added in the numbered order of the job parameters.
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
Specify the timeout value to use when upgrading the software using the helm
command, if not specified then the default value will be used.

The time value which must be an integer value is given as the number of seconds
to wait for the upgrade to finish.
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
            'default_value' => "1800",
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
The name to use for the Application namespace if you want to override the parameter from
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
The name to use for the Application release name if you want to override the parameter
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
        'SKIP_KPI_COLLECTION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "yes",
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
upgraded on the node, i.e. the directory that contains the 'eric-dsc-*.csar' and
'eric-dsc-tools-*.tgz' files.
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
        'USING_EVNFM' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the actual upgrade is triggered from EVNFM or not
and it changes how DAFT is executing certain tasks.

If the value is set to "yes" then the playlist will apply the following changes
to the task execution:

    - It will unpack only the CRD file from the software csar file if included
      in the file.
    - Loading and tagging CRD container images is skipped if SC release is 1.4.x or
      newer since EVNFM will take care of onboarding the images.
    - When SC release is 1.3.1 where the CRD images is delivered in the SC CSAR
      file, DAFT will still upgrade the CRDS since EVNFM is still not ready to
      handle this.
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
will take place and what is upgraded will depend on the values of the following
job parameters:

    - SKIP_UPGRADE_CRD
    - SKIP_UPGRADE_SC
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'WAIT_FOR_STABLE_NODE_TIMER' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "60",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the time to wait for the node to stabilize after the upgrade helm command
has finished successfully and all Pods has reached normal status before
continuing with the Playlist execution.

This timer might have to be increased due to for example certain BSF alarms does
not clear directly after a finished upgrade.

If no wait is wanted set this value to 0.

The time value which must be an integer value is given as the number of seconds
to wait after finished upgrade before Playlist execution continues.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist script is used for performing Upgrade of an DSC node and it
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
