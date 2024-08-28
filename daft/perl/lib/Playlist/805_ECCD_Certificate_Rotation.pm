package Playlist::805_ECCD_Certificate_Rotation;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.10
#  Date     : 2024-06-19 15:06:47
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
use version;

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    parse_network_config_parameters
    usage
    usage_return_playlist_variables
    );

use ADP::Kubernetes_Operations;
use General::File_Operations;
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
    $::JOB_PARAMS{'JOBTYPE'} = "ECCD_CERTIFICATE_ROTATION";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # Check if network configuration file is specified
    if (Playlist::920_Check_Network_Config_Information::is_network_config_specified() != 0) {
        General::Logging::log_user_error_message("You must specify a network configuration file for this Playlist to run");
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    # Set tasks to always execute
    General::State_Machine::always_execute_task("Playlist::914_Collect_Logs.+");
    General::State_Machine::always_execute_task("Playlist::918_Post_Healthcheck.+");
    General::State_Machine::always_execute_task("Playlist::801_Time_Shift_Test::Change_To_CCD_Ansible_Directory_P805S06T02");
    General::State_Machine::always_execute_task("Playlist::801_Time_Shift_Test::.+P805S07.+");

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    #
    # Perform pre time shift procedures
    #

    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P805S01, \&Fallback001_P805S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P805S02, \&Fallback001_P805S99 );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_PRE_HEALTHCHECK'} eq "no") {

        $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Test_Case_Health_Check_P805S03, \&Fallback001_P805S99 );
        return $rc if $rc < 0;

    }

    if ($::JOB_PARAMS{'SKIP_COLLECT_LOGS'} eq "no") {

        $rc = General::Playlist_Operations::execute_step( \&Collect_Pre_Test_Case_Logfiles_P805S04, \&Fallback001_P805S99 );
        return $rc if $rc < 0;

    }

    #
    # Perform the time shift procedure
    #

    $rc = General::Playlist_Operations::execute_step( \&Prepare_For_Certificate_Rotation_P805S05, \&Fallback001_P805S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Prepare_For_Ansible_Playbooks_P805S06, \&Fallback001_P805S99 );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo") {
        $rc = General::Playlist_Operations::execute_step( \&Perform_ECCD_Certificate_Rotation_Procedure_NONCAPO_P805S07, \&Fallback001_P805S99 );
        return $rc if $rc < 0;
    } elsif ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "capo") {
        $rc = General::Playlist_Operations::execute_step( \&Perform_ECCD_Certificate_Rotation_Procedure_CAPO_P805S08, \&Fallback001_P805S99 );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_error_message("Unknown ECCD_STACK_TYPE (=$::JOB_PARAMS{'ECCD_STACK_TYPE'}), cannot perform the ECCD Certificate Rotation procedure for this node");
        $rc = General::Playlist_Operations::execute_step( \&Do_Fallback_P805S98, \&Fallback001_P805S99 );
        return $rc if $rc < 0;
    }

    #
    # Perform post time shift procedures
    #

    $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Case_Check_P805S09, \&Fallback001_P805S99 );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} eq "no") {

        $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Case_Health_Check_P805S10, \&Fallback001_P805S99 );
        return $rc if $rc < 0;

    }

    if ($::JOB_PARAMS{'SKIP_COLLECT_LOGS'} eq "no") {

        $rc = General::Playlist_Operations::execute_step( \&Collect_Post_Test_Case_Logfiles_P805S11, \&Fallback001_P805S99 );
        return $rc if $rc < 0;

    }

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P805S12, \&Fallback001_P805S99 );
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
sub Initialize_Job_Environment_P805S01 {

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
sub Check_Job_Parameters_P805S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P805S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::936_Check_Deployed_Software::main } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'P920_TASK'} = "READ_PARAMETERS_FROM_FILE";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::920_Check_Network_Config_Information::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_CMYP_User_Info_P805S02T02 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "capo") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Access_To_LCM_Node_P805S02T03 } );
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
    sub Check_Job_Parameters_P805S02T01 {

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
    sub Check_CMYP_User_Info_P805S02T02 {

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
            }
        }

        General::Logging::log_user_message("CMYP_USER = $::JOB_PARAMS{'CMYP_USER'}");

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
    sub Check_Access_To_LCM_Node_P805S02T03 {

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
sub Perform_Pre_Test_Case_Health_Check_P805S03 {

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
sub Collect_Pre_Test_Case_Logfiles_P805S04 {

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
sub Prepare_For_Certificate_Rotation_P805S05 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_SC_Certificates_P805S05T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_And_Check_ETCD_Certificates_P805S05T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_And_Check_Cluster_Certificates_P805S05T03 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_KPI_COLLECTION'} eq "no") {
        $::JOB_PARAMS{'P933_TASK'} = "START_KPI_COLLECTION";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::933_Handle_KPI_Statistics::main } );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Current_Time_P805S05T04 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_WCDB_Configmap_Expiry_Date_P805S05T05 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_New_Directories_For_Changing_tolerationSeconds_P805S05T06 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Collect_Information_In_The_Background_P805S05T07 } );
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
    sub Print_SC_Certificates_P805S05T01 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/parse_certificate_validity_info.pl";
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        # Initialize the expiry date for TLS certificate
        $::JOB_PARAMS{'P805_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY'} = "";
        $::JOB_PARAMS{'P805_ORIGINAL_SIP_TLS_CERTIFICATE_SECRET'} = "";
        $::JOB_PARAMS{'P805_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY_EPOCH'} = "";

        General::Logging::log_user_message("Printing certificate information for namespace $sc_namespace.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$command --namespace=$sc_namespace --sort-by=not-after --print-simple-table",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            # Find expiry date of the SIP-TLS certificate and store it for later use
            for (@result) {
                if (/^.+(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\s+\S+)\s+InternalCertificate\s+(eric-sec-sip-tls-dced-client-cert|eric-sec-sip-tls-kafka-client-cert|eric-sec-sip-tls-wdc-certs|eric-sec-sip-tls-wdc-tls-cert)\s+/) {
                    # 2022-01-25 12:50:31 GMT    2032-01-26 00:51:01 GMT    InternalCertificate   eric-sec-sip-tls-dced-client-cert (cert.pem)
                    $::JOB_PARAMS{'P805_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY'} = $1;
                    $::JOB_PARAMS{'P805_ORIGINAL_SIP_TLS_CERTIFICATE_SECRET'} = $2;
                    $::JOB_PARAMS{'P805_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY_EPOCH'} = General::OS_Operations::epoch_time($1);
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

        if ($::JOB_PARAMS{'P805_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY'} eq "" || $::JOB_PARAMS{'P805_ORIGINAL_SIP_TLS_CERTIFICATE_SECRET'} eq "") {
            # Something has gone wrong
            General::Logging::log_user_error_message("Failed to find the original SIP TLS Certificate Expiry time");
            $rc = 1;
        } else {
            General::Logging::log_user_message(sprintf "The SIP TLS certificate in secret '%s' will expire on date %s", $::JOB_PARAMS{'P805_ORIGINAL_SIP_TLS_CERTIFICATE_SECRET'}, General::OS_Operations::format_time_iso($::JOB_PARAMS{'P805_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY_EPOCH'}));
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
    sub Print_And_Check_ETCD_Certificates_P805S05T02 {

        my $master0_ip = ADP::Kubernetes_Operations::get_node_ip( { "type" => "master", "index" => 0 } );
        my $command = sprintf "ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@%s 'certs=\$(ls /etc/etcd/ |grep .crt );for c in \$certs; do printf \"%%-10s \" \$c; sudo -n openssl x509 -in /etc/etcd/\$c --enddate --nocert; done'", $master0_ip;
        my $expiry_date;
        my $expiry_epoch;
        my $rc = 0;
        my @result;

        # Save the IP address so we don't have to check every time
        $::JOB_PARAMS{'P805_MASTER_0_IP_ADDRESS'} = $master0_ip;

        # First set the earliest expiry date to a very high value which is "Sat 20 Nov 2286 05:46:39 PM UTC"
        $::JOB_PARAMS{'P805_ORIGINAL_ETCD_CERTIFICATE_EXPIRY_EPOCH'} = 9999999999;
        $::JOB_PARAMS{'P805_ORIGINAL_ETCD_CERTIFICATE_EXPIRY'} = "";

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
                if ($expiry_epoch && $expiry_epoch < $::JOB_PARAMS{'P805_ORIGINAL_ETCD_CERTIFICATE_EXPIRY_EPOCH'}) {
                    $::JOB_PARAMS{'P805_ORIGINAL_ETCD_CERTIFICATE_EXPIRY_EPOCH'} = $expiry_epoch;
                    $::JOB_PARAMS{'P805_ORIGINAL_ETCD_CERTIFICATE_EXPIRY'} = $expiry_date;
                }
            }
        }

        if ($::JOB_PARAMS{'P805_ORIGINAL_ETCD_CERTIFICATE_EXPIRY'} ne "") {
            General::Logging::log_user_message(sprintf "The ETCD certificate that expire first will be on date %s", General::OS_Operations::format_time_iso($::JOB_PARAMS{'P805_ORIGINAL_ETCD_CERTIFICATE_EXPIRY_EPOCH'}));
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("No valid expiry date was found");
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
    sub Print_And_Check_Cluster_Certificates_P805S05T03 {

        my $master0_ip = $::JOB_PARAMS{'P805_MASTER_0_IP_ADDRESS'};
        my $command = sprintf "ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@%s 'sudo -n /usr/local/bin/kubeadm certs check-expiration'", $master0_ip;
        my $error_cnt = 0;
        my $expiry_date;
        my $expiry_epoch;
        my $key;
        my $message;
        my $name;
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

        #General::Logging::log_user_message(join "\n", @result);

        # First initialize the default values
        for $name ( "admin.conf", "apiserver", "apiserver-kubelet-client", "controller-manager.conf", "front-proxy-client", "scheduler.conf", "ca", "front-proxy-ca" ) {
            $key = "P805_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH_$name";
            $::JOB_PARAMS{$key} = 0;
        }

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
            if (/^(admin\.conf|apiserver|apiserver-kubelet-client|controller-manager\.conf|front-proxy-client|scheduler\.conf|ca|front-proxy-ca)\s+(\S+\s+\d+,\s+\d+\s+\d+:\d+\s+\S+)\s+/) {
                $name = $1;
                $expiry_date = $2;
                $expiry_epoch = General::OS_Operations::epoch_time($expiry_date);
                $key = "P805_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH_$name";
                $::JOB_PARAMS{$key} = $expiry_epoch;
            }
        }

        $message  = "The ECCD Certificates expire on the following dates:\n\n";
        $message .= sprintf "%-25s  %7s\n", "Certificate", "Expires";
        $message .= sprintf "%-25s  %7s\n", "-"x25, "-"x7;
        for $name ( "admin.conf", "apiserver", "apiserver-kubelet-client", "controller-manager.conf", "front-proxy-client", "scheduler.conf", "ca", "front-proxy-ca" ) {
            $key = "P805_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH_$name";
            if ($::JOB_PARAMS{$key} != 0) {
                $message .= sprintf "%-25s  %7s\n", $name, General::OS_Operations::format_time_iso($::JOB_PARAMS{$key});
            } else {
                $message .= sprintf "%-25s  %7s\n", $name, "No expiry date was found";
                $error_cnt++;
            }
        }
        General::Logging::log_user_message($message);

        if ($error_cnt == 0) {
            return 0;
        } else {
            General::Logging::log_user_error_message("Expiry date was not found for $error_cnt certificates");
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
    sub Print_Current_Time_P805S05T04 {

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
    sub Get_WCDB_Configmap_Expiry_Date_P805S05T05 {

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
    sub Create_New_Directories_For_Changing_tolerationSeconds_P805S05T06 {

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
    sub Collect_Information_In_The_Background_P805S05T07 {

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
sub Prepare_For_Ansible_Playbooks_P805S06 {

    my $rc;

    if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo") {

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Inventory_File_With_Workers_Included_P805S06T01 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_To_CCD_Ansible_Directory_P805S06T02 } );
        return $rc if $rc < 0;

    } elsif ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "capo") {

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Inventory_File_With_Workers_Included_Capo_P805S06T03 } );
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
    sub Create_Inventory_File_With_Workers_Included_P805S06T01 {

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
    sub Change_To_CCD_Ansible_Directory_P805S06T02 {

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
    sub Create_Inventory_File_With_Workers_Included_Capo_P805S06T03 {

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
sub Perform_ECCD_Certificate_Rotation_Procedure_NONCAPO_P805S07 {

    my $length;
    my $message;
    my $rc = 0;

    # In case of a re-run of an interrupted job set information about which tasks to execute and which what time offset
    $::JOB_PARAMS{'P805_EXECUTE_P805S07T01'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S07T01'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S07T01'} : 1;
    $::JOB_PARAMS{'P805_EXECUTE_P805S07T02'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S07T02'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S07T02'} : 1;
    $::JOB_PARAMS{'P805_EXECUTE_P805S07T03'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S07T03'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S07T03'} : 1;
    $::JOB_PARAMS{'P805_EXECUTE_P805S07T04'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S07T04'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S07T04'} : 1;
    $::JOB_PARAMS{'P805_EXECUTE_P805S07T05'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S07T05'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S07T05'} : 1;
    $::JOB_PARAMS{'P805_NEW_FRONT_PROXT_CA'} = exists $::JOB_PARAMS{'P805_NEW_FRONT_PROXT_CA'} ? $::JOB_PARAMS{'P805_NEW_FRONT_PROXT_CA'} : 1;

    if ($::JOB_PARAMS{'P805_EXECUTE_P805S07T01'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_tolerationSeconds_From_0_To_30_P805S07T01 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P805_EXECUTE_P805S07T01'} = 0;
    }

    if ($::JOB_PARAMS{'P805_EXECUTE_P805S07T02'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Execute_Certificate_Rotation_Playbooks_P805S07T02 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P805_EXECUTE_P805S07T02'} = 0;
    }

    if ($::JOB_PARAMS{'P805_EXECUTE_P805S07T03'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_And_Check_Cluster_Certificates_P805S07T03 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P805_EXECUTE_P805S07T03'} = 0;
    }

    if ($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} =~ /^2\.(17|19)\.\d+$/ || version->parse($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}) >= version->parse("2.20.0")) {
        if ($::JOB_PARAMS{'P805_EXECUTE_P805S07T04'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Update_Kube_Config_File_P805S07T04 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P805_EXECUTE_P805S07T04'} = 0;
        }
    }

    if ($::JOB_PARAMS{'P805_EXECUTE_P805S07T05'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Cluster_Health_P805S07T05 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P805_EXECUTE_P805S07T05'} = 0;
    }

    # If needed, Generate a new Front-Proxy-CA
    if ($::JOB_PARAMS{'ROTATE_FRONT_PROXY_CERTIFICATE'} eq "yes") {
        if ($::JOB_PARAMS{'P805_NEW_FRONT_PROXT_CA'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::937_Generate_New_ECCD_Front_Proxy_CA::main } );
            return $rc if $rc < 0;

            # Mark it not necessary to generate a new Front Proxy CA
            $::JOB_PARAMS{'P805_NEW_FRONT_PROXT_CA'} = 0;
        }
    }

    if ($rc == 0) {
        push @::JOB_STATUS, "(/) ECCD Certificate Rotation successful";
    } else {
        push @::JOB_STATUS, "(x) ECCD Certificate Rotation failed";
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
    sub Change_tolerationSeconds_From_0_To_30_P805S07T01 {

        my @all_pod_names = ADP::Kubernetes_Operations::get_pod_names( { "hide-output" => 1, "namespace" => $::JOB_PARAMS{'SC_NAMESPACE'} } );
        my @changed_pods = ();
        my $command;
        my @failed_pods = ();
        my $failure_cnt = 0;
        my $filename;
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        for my $pod_name (@all_pod_names) {
            General::Logging::log_user_message("Processing Pod $pod_name");

            $command = "kubectl get pods -n $sc_namespace $pod_name -o yaml";
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $command,
                    "hide-output"   => 1,
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
                        "hide-output"   => 1,
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
                        "hide-output"   => 1,
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
    sub Execute_Certificate_Rotation_Playbooks_P805S07T02 {

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
    sub Print_And_Check_Cluster_Certificates_P805S07T03 {

        my $master0_ip = $::JOB_PARAMS{'P805_MASTER_0_IP_ADDRESS'};
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
    sub Update_Kube_Config_File_P805S07T04 {

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
    sub Check_Cluster_Health_P805S07T05 {

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
sub Perform_ECCD_Certificate_Rotation_Procedure_CAPO_P805S08 {

    my $length;
    my $message;
    my $rc = 0;

    # In case of a re-run of an interrupted job set information about which tasks to execute
    $::JOB_PARAMS{'P805_EXECUTE_P805S10T01'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S10T01'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S10T01'} : 1;
    $::JOB_PARAMS{'P805_EXECUTE_P805S10T02'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S10T02'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S10T02'} : 1;
    $::JOB_PARAMS{'P805_EXECUTE_P805S10T03'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S10T03'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S10T03'} : 1;
    $::JOB_PARAMS{'P805_EXECUTE_P805S10T04'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S10T04'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S10T04'} : 1;
    $::JOB_PARAMS{'P805_EXECUTE_P805S10T05'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S10T05'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S10T05'} : 1;
    $::JOB_PARAMS{'P805_EXECUTE_P805S10T06'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S10T06'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S10T06'} : 1;
    $::JOB_PARAMS{'P805_EXECUTE_P805S10T07'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S10T07'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S10T07'} : 1;
    $::JOB_PARAMS{'P805_EXECUTE_P805S10T08'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S10T08'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S10T08'} : 1;
    $::JOB_PARAMS{'P805_EXECUTE_P805S10T09'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S10T09'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S10T09'} : 1;
    $::JOB_PARAMS{'P805_EXECUTE_P805S10T10'} = exists $::JOB_PARAMS{'P805_EXECUTE_P805S10T10'} ? $::JOB_PARAMS{'P805_EXECUTE_P805S10T10'} : 1;

    if ($::JOB_PARAMS{'P805_EXECUTE_P805S10T01'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_tolerationSeconds_From_0_To_30_P805S10T01 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P805_EXECUTE_P805S10T01'} = 0;
    }

    if ($::JOB_PARAMS{'P805_EXECUTE_P805S10T02'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Apply_Workaround_For_ETCD_Rotation_To_Stop_Active_Master_From_Changing_P805S10T02 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P805_EXECUTE_P805S10T02'} = 0;
    }

    if ($::JOB_PARAMS{'P805_EXECUTE_P805S10T03'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Rotate_Kubernetes_Certificates_On_LCM_P805S10T03 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P805_EXECUTE_P805S10T03'} = 0;
    }

    if ($::JOB_PARAMS{'P805_EXECUTE_P805S10T04'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Rotate_ETCD_Certificates_On_LCM_P805S10T04 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P805_EXECUTE_P805S10T04'} = 0;
    }

    if ($::JOB_PARAMS{'P805_EXECUTE_P805S10T05'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Rotate_Internal_Registry_Certificates_P805S10T05 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P805_EXECUTE_P805S10T05'} = 0;
    }

    # If needed, Generate a new Front-Proxy-CA
    if ($::JOB_PARAMS{'ROTATE_FRONT_PROXY_CERTIFICATE'} eq "yes" && $::JOB_PARAMS{'P805_EXECUTE_P805S10T06'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Rotate_Front_Proxy_CA_Certificates_P805S10T06 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P805_EXECUTE_P805S10T06'} = 0;
    }

    if ($::JOB_PARAMS{'P805_EXECUTE_P805S10T07'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Restart_calico_and_multus_Daemonset_P805S10T07 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P805_EXECUTE_P805S10T07'} = 0;
    }

    if ($::JOB_PARAMS{'P805_EXECUTE_P805S10T08'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Pods_To_Come_Up_P805S10T08 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P805_EXECUTE_P805S10T08'} = 0;
    }

    if ($::JOB_PARAMS{'P805_EXECUTE_P805S10T09'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_And_Check_Cluster_Certificates_P805S10T09 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P805_EXECUTE_P805S10T09'} = 0;
    }

    if ($::JOB_PARAMS{'P805_EXECUTE_P805S10T10'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Cluster_Health_P805S10T10 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P805_EXECUTE_P805S10T10'} = 0;
    }

    if ($rc == 0) {
        push @::JOB_STATUS, "(/) ECCD Certificate Rotation successful";
    } else {
        push @::JOB_STATUS, "(x) ECCD Certificate Rotation failed";
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
    sub Change_tolerationSeconds_From_0_To_30_P805S10T01 {

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
    sub Apply_Workaround_For_ETCD_Rotation_To_Stop_Active_Master_From_Changing_P805S10T02 {

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
    sub Rotate_Kubernetes_Certificates_On_LCM_P805S10T03 {

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
    sub Rotate_ETCD_Certificates_On_LCM_P805S10T04 {

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
    sub Rotate_Internal_Registry_Certificates_P805S10T05 {
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
    sub Rotate_Front_Proxy_CA_Certificates_P805S10T06 {
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
    sub Restart_calico_and_multus_Daemonset_P805S10T07 {

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
    sub Wait_For_Pods_To_Come_Up_P805S10T08 {

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
    sub Print_And_Check_Cluster_Certificates_P805S10T09 {

        my $master0_ip = $::JOB_PARAMS{'P805_MASTER_0_IP_ADDRESS'};
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
    sub Check_Cluster_Health_P805S10T10 {

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
sub Perform_Post_Test_Case_Check_P805S09 {

    my $rc = 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_SIP_TLS_Certificates_P805S09T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_ETCD_Certificates_P805S09T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Cluster_Certificates_P805S09T03 } );
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
    sub Check_SIP_TLS_Certificates_P805S09T01 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/parse_certificate_validity_info.pl";
        my $expiry_timestamp;
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sip_tls_secret_name = $::JOB_PARAMS{'P805_ORIGINAL_SIP_TLS_CERTIFICATE_SECRET'};

        General::Logging::log_user_message("Printing expiry date for secret '$sip_tls_secret_name'");

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

        if ($expiry_timestamp eq $::JOB_PARAMS{'P805_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY'}) {
            # The SIP-TLS certificates has changed
            General::Logging::log_user_message("Expiry timestamp for secret '$sip_tls_secret_name' has not changed from $::JOB_PARAMS{'P805_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY'}");
        } else {
            # The SIP-TLS certificates has changed (might be unexpected but not reported as a failure)
            General::Logging::log_user_message("Expiry timestamp for secret '$sip_tls_secret_name' has changed from $::JOB_PARAMS{'P805_ORIGINAL_SIP_TLS_CERTIFICATE_EXPIRY'} to $expiry_timestamp");
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
    sub Check_ETCD_Certificates_P805S09T02 {

        my $master0_ip = $::JOB_PARAMS{'P805_MASTER_0_IP_ADDRESS'};
        my $command = sprintf "ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@%s 'certs=\$(ls /etc/etcd/ |grep .crt );for c in \$certs; do printf \"%%-10s \" \$c; sudo -n openssl x509 -in /etc/etcd/\$c --enddate --nocert; done'", $master0_ip;
        my $expiry_date;
        my $expiry_epoch;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message(join "\n", @result);

        # First set the earliest expiry date to a very high value which is "Sat 20 Nov 2286 05:46:39 PM UTC"
        $::JOB_PARAMS{'P805_NEW_ETCD_CERTIFICATE_EXPIRY_EPOCH'} = 9999999999;
        $::JOB_PARAMS{'P805_NEW_ETCD_CERTIFICATE_EXPIRY'} = "";

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

        # Returns something like this:
        # ca.crt     notAfter=Nov 30 12:39:42 2042 GMT
        # peer.crt   notAfter=Nov 30 12:39:52 2042 GMT
        # server.crt notAfter=Nov 30 12:39:51 2042 GMT
        for (@result) {
            if (/^.+notAfter=(.+)/) {
                $expiry_date = $1;
                $expiry_epoch = General::OS_Operations::epoch_time($1);
                if ($expiry_epoch && $expiry_epoch < $::JOB_PARAMS{'P805_NEW_ETCD_CERTIFICATE_EXPIRY_EPOCH'}) {
                    $::JOB_PARAMS{'P805_NEW_ETCD_CERTIFICATE_EXPIRY_EPOCH'} = $expiry_epoch;
                    $::JOB_PARAMS{'P805_NEW_ETCD_CERTIFICATE_EXPIRY'} = $expiry_date;
                }
            }
        }

        if ($::JOB_PARAMS{'P805_ORIGINAL_ETCD_CERTIFICATE_EXPIRY_EPOCH'} eq $::JOB_PARAMS{'P805_NEW_ETCD_CERTIFICATE_EXPIRY_EPOCH'}) {
            # The certificates has not changed
            General::Logging::log_user_message(sprintf "The ETCD certificate has not changed from %s", General::OS_Operations::format_time_iso($::JOB_PARAMS{'P805_ORIGINAL_ETCD_CERTIFICATE_EXPIRY_EPOCH'}));
        } else {
            # The certificates has changed (might be unexpected but not reported as a failure)
            General::Logging::log_user_message(sprintf "The ETCD certificate has changed from %s to %s", General::OS_Operations::format_time_iso($::JOB_PARAMS{'P805_ORIGINAL_ETCD_CERTIFICATE_EXPIRY_EPOCH'}),
                                                                                                         General::OS_Operations::format_time_iso($::JOB_PARAMS{'P805_NEW_ETCD_CERTIFICATE_EXPIRY_EPOCH'}));
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
    sub Check_Cluster_Certificates_P805S09T03 {

        my $master0_ip = $::JOB_PARAMS{'P805_MASTER_0_IP_ADDRESS'};
        my $command = sprintf "ssh -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@%s 'sudo -n /usr/local/bin/kubeadm certs check-expiration'", $master0_ip;
        my $error_cnt = 0;
        my $expiry_date;
        my $expiry_epoch;
        my $key;
        my $message;
        my $name;
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

        #General::Logging::log_user_message(join "\n", @result);

        # First initialize the default values
        for $name ( "admin.conf", "apiserver", "apiserver-kubelet-client", "controller-manager.conf", "front-proxy-client", "scheduler.conf", "ca", "front-proxy-ca" ) {
            $key = "P805_NEW_ECCD_CERTIFICATE_EXPIRY_EPOCH_$name";
            $::JOB_PARAMS{$key} = 0;
        }

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
            if (/^(admin\.conf|apiserver|apiserver-kubelet-client|controller-manager\.conf|front-proxy-client|scheduler\.conf|ca|front-proxy-ca)\s+(\S+\s+\d+,\s+\d+\s+\d+:\d+\s+\S+)\s+/) {
                $name = $1;
                $expiry_date = $2;
                $expiry_epoch = General::OS_Operations::epoch_time($expiry_date);
                $key = "P805_NEW_ECCD_CERTIFICATE_EXPIRY_EPOCH_$name";
                $::JOB_PARAMS{$key} = $expiry_epoch;
            }
        }

        $message  = "The comparison of ECCD Certificates expiry before and after rotation:\n\n";
        $message .= sprintf "%-25s  %-20s  %-20s  %7s\n", "Certificate", "Old Expires", "New Expires", "Verdict";
        $message .= sprintf "%-25s  %-20s  %-20s  %7s\n", "-"x25, "-"x20, "-"x20, "-"x7;
        for $name ( "admin.conf", "apiserver", "apiserver-kubelet-client", "controller-manager.conf", "front-proxy-client", "scheduler.conf", "ca", "front-proxy-ca" ) {
            $key = "P805_NEW_ECCD_CERTIFICATE_EXPIRY_EPOCH_$name";
            if ($::JOB_PARAMS{$key} != 0) {
                if ($::JOB_PARAMS{"P805_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH_$name"} ne $::JOB_PARAMS{$key}) {
                    # The certificate has been changed which is to be expected
                    $message .= sprintf "%-25s  %-20s  %-20s  %s\n", $name,
                                                                     General::OS_Operations::format_time_iso($::JOB_PARAMS{"P805_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH_$name"}),
                                                                     General::OS_Operations::format_time_iso($::JOB_PARAMS{$key}),
                                                                     "OK";
                } else {
                    # The certificate has not been changed which is unexpected
                    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "no") {
                        if ($name eq "ca") {
                            # This certificate is never touched
                            $message .= sprintf "%-25s  %-20s  %-20s  %s\n", $name,
                                                                             General::OS_Operations::format_time_iso($::JOB_PARAMS{"P805_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH_$name"}),
                                                                             General::OS_Operations::format_time_iso($::JOB_PARAMS{$key}),
                                                                             "OK, this certificate is never touched";
                        } elsif ($name eq "front-proxy-ca" && $::JOB_PARAMS{'ROTATE_FRONT_PROXY_CERTIFICATE'} eq "no") {
                            # Special handling for front-proxy-ca
                            $message .= sprintf "%-25s  %-20s  %-20s  %s\n", $name,
                                                                             General::OS_Operations::format_time_iso($::JOB_PARAMS{"P805_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH_$name"}),
                                                                             General::OS_Operations::format_time_iso($::JOB_PARAMS{$key}),
                                                                             "OK, this certificate is not touched because ROTATE_FRONT_PROXY_CERTIFICATE=no";
                        } else {
                            $message .= sprintf "%-25s  %-20s  %-20s  %s\n", $name,
                                                                            General::OS_Operations::format_time_iso($::JOB_PARAMS{"P805_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH_$name"}),
                                                                            General::OS_Operations::format_time_iso($::JOB_PARAMS{$key}),
                                                                            "NOK, the expected change of the expiry date was not done";
                            $error_cnt++;
                        }
                    } else {
                        # We are debugging the playlist, so no changes will be done
                        $message .= sprintf "%-25s  %-20s  %-20s  %s\n", $name,
                                                                        General::OS_Operations::format_time_iso($::JOB_PARAMS{"P805_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH_$name"}),
                                                                        General::OS_Operations::format_time_iso($::JOB_PARAMS{$key}),
                                                                        "OK, the change of the expiry date was not done since DEBUG_PLAYLIST=yes";
                    }
                }
            } else {
                $message .= sprintf "%-25s  %-20s  %-20s  %s\n", $name,
                                                                 General::OS_Operations::format_time_iso($::JOB_PARAMS{"P805_ORIGINAL_ECCD_CERTIFICATE_EXPIRY_EPOCH_$name"}),
                                                                 "-",
                                                                 "NOK, no expiry date was found";
                $error_cnt++;
            }
        }
        General::Logging::log_user_message($message);

        if ($error_cnt == 0) {
            return 0;
        } else {
            General::Logging::log_user_error_message("Expiry date was not found for $error_cnt certificates");
            return 1;
        }
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
sub Perform_Post_Test_Case_Health_Check_P805S10 {

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
sub Collect_Post_Test_Case_Logfiles_P805S11 {

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
sub Cleanup_Job_Environment_P805S12 {

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
sub Do_Fallback_P805S98 {

    return General::Playlist_Operations::RC_FALLBACK;
}

# -----------------------------------------------------------------------------
sub Fallback001_P805S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    if (exists $::JOB_PARAMS{'KPI_PID'} && $::JOB_PARAMS{'KPI_PID'} != 0 && $::JOB_PARAMS{'SKIP_KPI_COLLECTION'} eq "no") {
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
See also the CMYP_PASSWORD and CMYP_USER.

This parameter determines which of the user definitions in the network config
file will be used for finding the passwords to use, i.e. where the field
'password_expire' match the PASSWORD_EXPIRE parameter.

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
        'ROTATE_FRONT_PROXY_CERTIFICATE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies if also the ECCD front proxy certificates should be
rotated together with the other ECCD certificates.

If this value is 'yes' which is also the default then all ECCD certificates
including the front proxy will be rotated.

If this value is 'no' then the front proxy certificates are not rotated.
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
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist performs ECCD Certificate Rotation that will prolong the certificate
validity for a bit longer. How far they are prolonged depends on the current
expiry date of the certificates and what the current time is.

This Playlist MUST BE EXECUTED ON DIRECTOR-0 of the node because it uses Ansible
playbooks and are manipulating the kube config files.

!!!!!!!!!!!!!!!!!!!!!
!!!!!! WARNING !!!!!!
!!!!!!!!!!!!!!!!!!!!!

This playlist should not be executed unless you know what you are doing!

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
