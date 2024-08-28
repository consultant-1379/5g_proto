package Playlist::932_Test_Case_Common_Logic;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.50
#  Date     : 2024-06-17 09:12:27
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021-2023
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

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;
use Playlist::917_Pre_Healthcheck;
use Playlist::918_Post_Healthcheck;
use Playlist::920_Check_Network_Config_Information;
use Playlist::922_Handle_Software_Directory_Files;
use Playlist::933_Handle_KPI_Statistics;
use Playlist::936_Check_Deployed_Software;

#
# Variable Declarations
#
our %playlist_variables;
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
    my $length;
    my $message;
    my $rc;

    unless (exists $::JOB_PARAMS{'P932_TASK'}) {
        General::Logging::log_user_error_message("Job variable 'P932_TASK' not set before calling this playlist");
        return General::Playlist_Operations::RC_FALLBACK;
    }

    $message = "# $::JOB_PARAMS{'P932_TASK'} #";
    $length = length($message);
    General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

    # Set tasks to always execute
    General::State_Machine::always_execute_task('Playlist::3\d\d_Robustness_Test_.+_P3\d\dS02T\d\d');
    General::State_Machine::always_execute_task('Playlist::4\d\d_Maintainability_Test_.+_P4\d\dS02T\d\d');
    General::State_Machine::always_execute_task("Playlist::914_Collect_Logs.+");
    General::State_Machine::always_execute_task("Playlist::918_Post_Healthcheck.+");
    General::State_Machine::always_execute_task("Playlist::932_Test_Case_Common_Logic.+");
    General::State_Machine::always_execute_task("Playlist::933_Handle_KPI_Statistics.+");

    if ($::JOB_PARAMS{'P932_TASK'} eq "PRE_CHECK") {
        # First time we execute the logic, perform a job setup and pre test checks.

        # Set job status to ONGOING in case we get an unexpected reboot
        $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

        $::JOB_PARAMS{'COMBINED_HEALTH_CHECK_RESULT'} = "";
        $::JOB_PARAMS{'COMBINED_HEALTH_CHECK_DESCRIPTION'} = "";
        $::JOB_PARAMS{'COMBINED_KPI_VERDICT_RESULT'} = "";
        $::JOB_PARAMS{'COMBINED_KPI_VERDICT_DESCRIPTION'} = "";

        # Check if network configuration file is specified
        if (Playlist::920_Check_Network_Config_Information::is_network_config_specified() != 0) {
            General::Logging::log_user_warning_message("No network config file is loaded, any required input must be specified as job variables");
        }

        # Execute the different steps in this playlist
        $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P932S01, \&Fallback001_P932S99 );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P932S02, \&Fallback001_P932S99 );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_step( \&Check_Configuration_Files_If_Specified_P932S03, \&Fallback001_P932S99 );
        return $rc if $rc < 0;

        # The following is a temporary workaround until the DSC also supports fetching of KPI data.
        # When it is supported then the following if statement can be removed.
        if ($::JOB_PARAMS{'APPLICATION_TYPE'} eq "dsc") {
            if ($::JOB_PARAMS{'SKIP_KPI_COLLECTION'} eq "no") {
                General::Logging::log_user_warning_message("SKIP_KPI_COLLECTION changed from 'no' to 'yes' because the DSC doesn't currently support KPI collection.");
                $::JOB_PARAMS{'SKIP_KPI_COLLECTION'} = "yes";
            }
        }

        if ($::JOB_PARAMS{'SKIP_DEPLOYMENT_CHECKS'} eq "no" && $::JOB_PARAMS{'SKIP_PRE_HEALTHCHECK'} eq "no") {

            $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Test_Case_Health_Check_P932S04, \&Fallback001_P932S99 );
            return $rc if $rc < 0;

        }

        if ($::JOB_PARAMS{'SKIP_COLLECT_LOGS'} eq "no") {

            $rc = General::Playlist_Operations::execute_step( \&Collect_Pre_Test_Case_Logfiles_P932S05, \&Fallback001_P932S99 );
            return $rc if $rc < 0;

        }

    } elsif ($::JOB_PARAMS{'P932_TASK'} eq "WAIT_FOR_STABLE_NODE") {
        # Can be called multiple times, will check if all PODs are up and working and
        # if not wait a certain time for them to come up and then perform a health check
        # of the node.

        $rc = General::Playlist_Operations::execute_step( \&Wait_For_Stable_Node_P932S06, \&Fallback001_P932S99 );
        return $rc if $rc < 0;

    } elsif ($::JOB_PARAMS{'P932_TASK'} eq "POST_CHECK") {
        # Last time we execute the logic, perform post test check and job cleanup.

        if ($::JOB_PARAMS{'SKIP_DEPLOYMENT_CHECKS'} eq "no" && $::JOB_PARAMS{'SKIP_KPI_COLLECTION'} eq "no") {

            $rc = General::Playlist_Operations::execute_step( \&KPI_Collection_P932S07, \&Fallback001_P932S99 );
            return $rc if $rc < 0;

        }

        if ($::JOB_PARAMS{'SKIP_DEPLOYMENT_CHECKS'} eq "no" && $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} eq "no") {

            $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Case_Health_Check_P932S08, \&Fallback001_P932S99 );
            return $rc if $rc < 0;

        }

        if ($::JOB_PARAMS{'SKIP_COLLECT_LOGS'} eq "no") {

            $rc = General::Playlist_Operations::execute_step( \&Collect_Post_Test_Case_Logfiles_P932S09, \&Fallback001_P932S99 );
            return $rc if $rc < 0;

        }

        $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P932S10, \&Fallback001_P932S99 );
        return $rc if $rc < 0;

    } else {
        # Some unklnownThe playlist has already been called at least 2 times so write a warning to the caller
        # that something if strange with their logic.
        General::Logging::log_user_error_message("This Playlist has been called with an invalid job parameter P932_TASK=$::JOB_PARAMS{'P932_TASK'}.\nPlease check the calling playlist and fix the fault.");
        $rc = General::Playlist_Operations::RC_FALLBACK;
    }

    delete $::JOB_PARAMS{'P932_TASK'};

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
sub Initialize_Job_Environment_P932S01 {

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
sub Check_Job_Parameters_P932S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P932S02T01 } );
    return $rc if $rc < 0;

    # Extract and check the umbrella files from the software csar file
    $::JOB_PARAMS{'CHECK_CSAR_FILE'}            = "yes";
    $::JOB_PARAMS{'CHECK_CSAR_FILE_CONTENT'}    = "yes";
    $::JOB_PARAMS{'CHECK_TOOLS_FILE'}           = "no";
    $::JOB_PARAMS{'CHECK_TOOLS_FILE_CONTENT'}   = "no";
    $::JOB_PARAMS{'CHECK_VALUES_FILE'}          = "no";
    $::JOB_PARAMS{'COPY_UMBRELLA_FILE'}         = "yes";
    $::JOB_PARAMS{'COPY_VALUES_FILE'}           = "no";
    $::JOB_PARAMS{'UNPACK_CSAR_FILE'}           = "no";
    $::JOB_PARAMS{'UNPACK_TOOLS_FILE'}          = "no";
    $::JOB_PARAMS{'UNPACK_UMBRELLA_FILE'}       = "yes";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::922_Handle_Software_Directory_Files::main } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_DEPLOYMENT_CHECKS'} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::936_Check_Deployed_Software::main } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Deployed_CNF_P932S02T02 } );
        return $rc if $rc < 0;
    }

    $::JOB_PARAMS{'P920_TASK'} = "READ_PARAMETERS_FROM_FILE";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::920_Check_Network_Config_Information::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_Part2_P932S02T03 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_DEPLOYMENT_CHECKS'} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_Used_Values_P932S02T04 } );
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
    sub Check_Job_Parameters_P932S02T01 {

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
    sub Check_Deployed_CNF_P932S02T02 {

        my @nodetypes;
        my $rc = 0;

        General::Logging::log_user_message("Get deployed CNF from pods in SC deployment");
        @nodetypes = ADP::Kubernetes_Operations::get_enabled_cnf( $::JOB_PARAMS{'SC_NAMESPACE'} );

        $::JOB_PARAMS{'nodetype'} = "";
        for my $type (sort @nodetypes) {
            $::JOB_PARAMS{'nodetype'} .= "$type,";
        }
        $::JOB_PARAMS{'nodetype'} =~ s/,$//g;
        General::Logging::log_user_message("Enabled CNF: $::JOB_PARAMS{'nodetype'}");

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
    sub Check_Job_Parameters_Part2_P932S02T03 {

        my $rc = 0;

        if ($::JOB_PARAMS{'CMYP_USER'} eq "") {
            # No user specified, use default 'expert' user
            $::JOB_PARAMS{'CMYP_USER'} = "expert";
        }

        if ($::JOB_PARAMS{'CMYP_PASSWORD'} eq "") {
            # Find the used password for the user.
            for my $key (keys %::NETWORK_CONFIG_PARAMS) {
                next unless ($key =~ /^default_user_\d+$/);
                if ($::NETWORK_CONFIG_PARAMS{$key}{'user'} eq $::JOB_PARAMS{'CMYP_USER'}) {
                    $::JOB_PARAMS{'CMYP_PASSWORD'} = $::NETWORK_CONFIG_PARAMS{$key}{'password'};
                }
            }
        }

        if ($::JOB_PARAMS{'CMYP_USER'} eq "" || $::JOB_PARAMS{'CMYP_USER'} eq "CHANGEME") {
            General::Logging::log_user_error_message("Job parameter value for 'CMYP_USER' is empty or CHANGEME");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        if ($::JOB_PARAMS{'CMYP_PASSWORD'} eq "" || $::JOB_PARAMS{'CMYP_PASSWORD'} eq "CHANGEME") {
            General::Logging::log_user_error_message("Job parameter value for 'CMYP_PASSWORD' is empty or CHANGEME");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        General::Logging::log_user_message("CMYP_USER = $::JOB_PARAMS{'CMYP_USER'}\n");

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
    sub Get_Used_Values_P932S02T04 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        my $helm_executable = $::JOB_PARAMS{'HELM_EXECUTABLE'};
        my @nodetypes;
        my $rc = 0;
        my @result;
        my @stderror;
        my %yaml_contents;

        General::Logging::log_user_message("Get Used Values from SC deployment");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$helm_executable get values -o yaml --namespace $sc_namespace $sc_release_name",
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@stderror,
                "save-to-file"  => $::JOB_PARAMS{'_JOB_LOG_DIR'} . "/used_values.yaml",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get used values from deployment");
            return General::Playlist_Operations::RC_FALLBACK;
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
sub Check_Configuration_Files_If_Specified_P932S03 {

    my $rc;

    if (exists $::JOB_PARAMS{'SOFTWARE_DIR'} && $::JOB_PARAMS{'SOFTWARE_DIR'} ne "") {
        # No need to unpack the Umbrella file because it has already been done
        $rc = 0;
        # $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Unpack_SC_Umbrella_File_P932S03T01 } );
        # return $rc if $rc < 0;
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
    sub Unpack_SC_Umbrella_File_P932S03T01 {

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
sub Perform_Pre_Test_Case_Health_Check_P932S04 {

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
sub Collect_Pre_Test_Case_Logfiles_P932S05 {

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
sub Wait_For_Stable_Node_P932S06 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Pod_Status_P932S06T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Alarms_PodFailure_And_BsfCassandraNodeUnavailable_P932S06T02 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_DEPLOYMENT_CHECKS'} eq "no" && $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} eq "no") {

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::918_Post_Healthcheck::main } );
        $::JOB_PARAMS{'COMBINED_HEALTH_CHECK_RESULT'} .= "$::JOB_PARAMS{'HEALTH_CHECK_RESULT'},";
        $::JOB_PARAMS{'COMBINED_HEALTH_CHECK_DESCRIPTION'} .= "$::JOB_PARAMS{'TEST_DESCRIPTION'}|";
        return $rc if $rc < 0;

    }

    if ($::JOB_PARAMS{'SKIP_DEPLOYMENT_CHECKS'} eq "no" && $::JOB_PARAMS{'SKIP_KPI_COLLECTION'} eq "no" && $::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'} eq "no") {

        $::JOB_PARAMS{'P933_TASK'} = "KPI_VERDICT";
        $::JOB_PARAMS{'P933_RESOLUTION'} = 15;
        $::JOB_PARAMS{'P933_PAST'} = sprintf "%ds", (time() - $::JOB_PARAMS{'KPI_START_TIME'} + 1);    # Add an extra 1 second
        $::JOB_PARAMS{'P933_SUCCESS_RATE_DESCRIPTION'} = $::JOB_PARAMS{'KPI_SUCCESS_RATE_DESCRIPTION'};
        $::JOB_PARAMS{'P933_SUCCESS_RATE_THRESHOLD'} = $::JOB_PARAMS{'KPI_SUCCESS_RATE_THRESHOLD'};
        $::JOB_PARAMS{'P933_SUMMARY_MESSAGE'} = "$::JOB_PARAMS{'TEST_DESCRIPTION'} KPI Success Rate Verdict";
        $::JOB_PARAMS{'P933_VERDICT_IGNORE_FAILURE'} = "yes";
        $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_statistics_$::JOB_PARAMS{'KPI_START_TIME'}";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::933_Handle_KPI_Statistics::main } );
        $::JOB_PARAMS{'COMBINED_KPI_VERDICT_RESULT'} .= "$::JOB_PARAMS{'P933_VERDICT_RESULT'},";
        $::JOB_PARAMS{'COMBINED_KPI_VERDICT_DESCRIPTION'} .= "$::JOB_PARAMS{'TEST_DESCRIPTION'}|";
        return $rc if $rc < 0;

    }

    if ($::JOB_PARAMS{'SKIP_COLLECT_LOGS'} eq "no") {

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
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
    sub Check_Pod_Status_P932S06T01 {

        my @exclude_ready_list = ();
        my @exclude_status_list = ();
        my @failed_pods = ();
        my $max_time = $::JOB_PARAMS{'POD_STATUS_TIMEOUT'};
        my $message = "";
        my $rc = 0;
        my $repeated_check_time = $::JOB_PARAMS{'POD_UP_TIMER'};
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
    sub Check_Alarms_PodFailure_And_BsfCassandraNodeUnavailable_P932S06T02 {

        my $max_time = $::JOB_PARAMS{'POD_STATUS_TIMEOUT'};
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $stop_time = time() + $max_time;
        my $wait;

        General::Logging::log_user_message(sprintf "Checking every 10 seconds that there are no PodFailure or BsfCassandraNodeUnavailable alarms in $sc_namespace namespace for a maximum of %d seconds.", ($stop_time - time()));
        while (1) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "perl $::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/alarm_check.pl " .
                                       "--alarm-name=PodFailure " .
                                       "--alarm-name=BsfCassandraNodeUnavailable " .
                                       ($::JOB_PARAMS{'KUBECONFIG'} ne "" ? "--kubeconfig=$::JOB_PARAMS{'KUBECONFIG'} " : "") .
                                       "--log_file $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/all.log " .
                                       "--namespace $sc_namespace " .
                                       "--use_allowlist ",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0) {
                # No PodFailure alarm, or it was in the allow list
                last;
            }

            # There are one or more PodFailure or BsfCassandraNodeUnavailable alarms, wait a bit and then check again
            if ($stop_time <= time()) {
                if ($::JOB_PARAMS{"IGNORE_FAILED_HEALTH_CHECK"} =~ /^yes$/i) {
                    General::Logging::log_user_warning_message((join "\n", @result) . "\nThere are still PodFailure and/or BsfCassandraNodeUnavailable alarms, maximum wait time has expired.\nBut ignored because IGNORE_FAILED_HEALTH_CHECK=yes.");
                    $rc = 0;
                    last;
                } else {
                    General::Logging::log_user_error_message((join "\n", @result) . "\nThere are still PodFailure and/or BsfCassandraNodeUnavailable alarms, maximum wait time has expired.");
                    push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} Post PodFailure and BsfCassandraNodeUnavailable alarm check failed";
                    return 1;
                }
            }
            $wait = $stop_time - time();
            if ($wait > 10) {
                $wait = 10;
            }

            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "progress-message"  => 0,
                    "seconds"           => $wait,
                    "use-logging"       => 1,
                }
            );
            if ($rc == 1) {
                General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
                $rc = 0;
                last;
            }
        }

        General::Logging::log_user_message("PodFailure and BsfCassandraNodeUnavailable alarm check was successful");
        push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} Post PodFailure and BsfCassandraNodeUnavailable alarm check was successful";

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
sub KPI_Collection_P932S07 {

    my $rc = 0;

    $::JOB_PARAMS{'P933_TASK'} = "KPI_SNAPSHOT";
    $::JOB_PARAMS{'P933_RESOLUTION'} = 15;
    $::JOB_PARAMS{'P933_PAST'} = sprintf "%ds", (time() - $::JOB_PARAMS{'_JOB_STARTTIME'} + 15);    # Add an extra 15 seconds
    $::JOB_PARAMS{'P933_SUMMARY_MESSAGE'} = "KPI Counters for the Complete Test";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::933_Handle_KPI_Statistics::main } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_KPI_VERDICT_CHECK'} eq "no") {

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Combined_KPI_Verdicts_P932S07T01 } );
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
    sub Check_Combined_KPI_Verdicts_P932S07T01 {

        my $failure_cnt = 0;
        my $job_type = $::JOB_PARAMS{'JOBTYPE'};
        my $log_file = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_verdict.log";
        my @kpi_verdict_description = ();
        my @kpi_verdict_results = ();
        my $message = "KPI Verdict Results\n\n";
        my $rc;

        # Cleanup the job type
        $job_type =~ s/^(MAINTAINABILITY_TEST_|ROBUSTNESS_TEST_)//;
        $job_type =~ s/_/ /g;
        $job_type = lc($job_type);
        $job_type =~ s/\b(\w)/\U$1/g;   # The outcome should be e.g. "Timezone Change"

        # Cleaup the combined result
        $::JOB_PARAMS{'COMBINED_KPI_VERDICT_DESCRIPTION'} =~ s/|$//g;
        $::JOB_PARAMS{'COMBINED_KPI_VERDICT_RESULT'} =~ s/,$//g;
        @kpi_verdict_description = split /\|/, $::JOB_PARAMS{'COMBINED_KPI_VERDICT_DESCRIPTION'};
        @kpi_verdict_results = split /,/, $::JOB_PARAMS{'COMBINED_KPI_VERDICT_RESULT'};

        if (scalar @kpi_verdict_description == scalar @kpi_verdict_results) {
            # Same number of data, everything seems to be OK
            for (my $i=0; $i<=$#kpi_verdict_results; $i++) {
                if ($kpi_verdict_results[$i] eq "SUCCESS") {
                    $message .= "$kpi_verdict_description[$i] KPI Verdict was successful\n";
                } else {
                    $message .= "$kpi_verdict_description[$i] KPI Verdict was unsuccessful\n";
                    $failure_cnt++;
                }
            }
        } else {
            # Not the same number of data, use a generic format
            my $cnt = 0;
            for my $result (split /,/, $::JOB_PARAMS{'COMBINED_KPI_VERDICT_RESULT'}) {
                $cnt++;
                if ($result eq "SUCCESS") {
                    $message .= "$job_type $cnt KPIs successful\n";
                } else {
                    $message .= "$job_type $cnt KPIs failed\n";
                    $failure_cnt++;
                }
            }
        }
        $message .= "------------------------------------------------------------\n";

        if ($failure_cnt == 0) {
            $message .= "Overall KPI Verdict successful\n";
            General::Logging::log_user_message($message);
            push @::JOB_STATUS, '(/) Overall KPI Verdict was successful';
        } else {
            $message .= "Overall KPI Verdict unsuccessful\n";
            General::Logging::log_user_error_message($message);
            push @::JOB_STATUS, '(x) Overall KPI Verdict was unsuccessful';
        }

        General::File_Operations::write_file(
            {
                "filename"          => $log_file,
                "output-ref"        => [ "-" x 80, "", $message, "", "-" x 80 ],
                "append-file"       => 1,
                "file-access-mode"  => "666",
            }
        );

        # TODO: Do we need to fail this task if the $failure_cnt > 0?
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
sub Perform_Post_Test_Case_Health_Check_P932S08 {

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
sub Collect_Post_Test_Case_Logfiles_P932S09 {

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
sub Cleanup_Job_Environment_P932S10 {

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
sub Fallback001_P932S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

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
        'KPI_SUCCESS_RATE_DESCRIPTION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => ".+",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified then this description will be used for selecting which KPI success
rate KPI values to check after each executed test case.

If specified it should contain the KPI description string excluding the string
"Success Rate [%]" which is automatically appended to the specified value.
This value will be used to check if the success rate is equal to or higher than the
P933_SUCCESS_RATE_THRESHOLD value in which case the check is successful, otherwise
the check will fail.
If not specified then the value to use will be ".+" i.e. any string that ends with
"Success Rate [%]" will be checked.

The result of all KPI checks are combined at the end of the Playlist so if one or
more KPI checks had a failed result then the overall result will be failed, otherwise
if all KPI checks were successful the the overall check is also successful.
EOF
            'validity_mask' => '.+',
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
            'default_value' => "1200",
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
            'default_value' => "120",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify how long time in seconds the POD's must stay up when checking that the
node is stable before continuing with the health check.
If the value is >0 when checking for stable node the POD's must come up and stay
up for this many seconds before the node is considered as stable.
If the value is 0 then as soon as all pods come up the node is considered as
stable and health check is immediately performed.

It has been seen that during some of the robustness tests the pods all come up
for a short time and then go down again, so having this value >0 will make sure
we have a stable node before continuing with the test.
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
        'SKIP_DEPLOYMENT_CHECKS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Option to skip any tasks where a check is done if an SC deployment is present on
the node.
If 'yes' then any check where an SC deployment needs to be present will be skipped.
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
        'SKIP_KPI_VERDICT_CHECK' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Option to skip the KPI verdict check when executing the playlist.
Valid values are yes or no (default is no).

This parameter have no affect if SKIP_KPI_COLLECTION=yes/
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
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist performs the common logic to prepare for a executing a Robustness
Test case like setting up the environment and parsong certain common variables
and performing pre and post test case log collection and health checks.
It can be called multiple times and when called the first time it will setup the
job environment and perform pre test case checks. The second time it's called
it will perform post test case checks and do a cleanup of the job.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.

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
