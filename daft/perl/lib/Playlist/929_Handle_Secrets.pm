package Playlist::929_Handle_Secrets;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.28
#  Date     : 2024-06-04 14:49:20
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
    usage
    );

# Used Perl package files
use File::Basename qw(dirname basename);
use General::File_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

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
#  >0: The call failed
#  <0: Are Playlist specific return code which triggers special actions, for
#      example:
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

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    } else {
        $debug_command = "";
    }

    $rc = General::Playlist_Operations::execute_step( \&Load_Configuration_Files_If_Existing_P929S01, \&Fallback001_P929S99 );
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
sub Load_Configuration_Files_If_Existing_P929S01 {
    my $rc = 0;

    if (exists $::JOB_PARAMS{'CONFIG_FILE_SNMP_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_User_Credentials_For_FM_SNMP_And_SNMP_Configuration_P929S01T01 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_BRAGENT_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_User_Credentials_For_SC_Release_Backup_Export_Using_SFTP_P929S01T02 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_LDAP_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_User_Credentials_For_Admin_User_P929S01T03 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'APPLICATION_TYPE'} eq "sc" && exists $::JOB_PARAMS{'CONFIG_FILE_WCDBCD_ADMIN_SECRET'} && exists $::JOB_PARAMS{'CONFIG_FILE_WCDBCD_DAY0_SECRET'} && exists $::JOB_PARAMS{'CONFIG_FILE_BSF_DB_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_User_Credentials_For_Accessing_Wide_Column_Database_CD_Service_P929S01T04 } );
        return $rc if $rc < 0;
    } elsif ($::JOB_PARAMS{'APPLICATION_TYPE'} eq "dsc" && exists $::JOB_PARAMS{'CONFIG_FILE_WCDBCD_ADMIN_SECRET'} && exists $::JOB_PARAMS{'CONFIG_FILE_WCDBCD_DAY0_SECRET'} ) {
        #
        # DSC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_User_Credentials_For_Accessing_Wide_Column_Database_CD_Service_P929S01T04 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_WCDBCD_DAY0_CERTIFICATE'}) {
        #
        # SC Specific logic
        #
        if ($::JOB_PARAMS{'GEOGRAPHICAL_REDUNDANCY'} eq "yes") {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Prepare_Day0_Certificates_For_BSF_P929S01T05 } );
            return $rc if $rc < 0;
        }
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_DOCUMENT_DATABASE_PG_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_User_Credentials_For_Accessing_Document_Database_PG_Service_P929S01T06 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_DISTRIBUTED_COORDINATOR_ED_SC_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_User_Credentials_For_SC_Accessing_Distributed_Coordinator_ED_SC_Service_P929S01T07 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_DISTRIBUTED_COORDINATOR_ED_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_User_Credentials_For_Accessing_Distributed_Coordinator_ED_Service_P929S01T08 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_MONITOR_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_User_Credentials_For_Accessing_SC_Monitor_Service_P929S01T09 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_SFTP_SERVER_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_SFTP_Server_Configuration_P929S01T10 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_TAP_CONFIG_MAP'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_TAP_Config_Map_P929S01T11 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_EMBEDDED_CNOM_ACCESS'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_User_Credentials_For_Accessing_SC_Embedded_CNOM_P929S01T12 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_DAY0_ADMIN_SECRET'}) {
        #
        # cnDSC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_Day0_Admin_Secret_P929S01T13 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_WCDBCD_DSC_SECRET'}) {
        #
        # cnDSC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_WCDBCD_DSC_Secret_P929S01T14 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_PVTB_CI_SECRET'}) {
        #
        # PVTB CI Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_PVTB_CI_Secret_P929S01T15 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} && $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} =~ /\|/) {
        #
        # SC Specific logic
        #

        # It's a new SC release (>=1.15) that contains multiple helm charts.
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Configure_Fault_Mappings_Config_Map_P929S01T16 } );
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
    sub Configure_User_Credentials_For_FM_SNMP_And_SNMP_Configuration_P929S01T01 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/config.json";
        my $rc = 0;
        my @result;
        my $secret_name = $::JOB_PARAMS{'snmp_trap_target_secret_name'};

        unless (-f $filename) {
            General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create FM SNMP Credentials skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Creating secret object");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $sc_namespace create secret generic $secret_name --from-file=$filename",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create FM SNMP Credentials successful";
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create FM SNMP Credentials not needed";
                        return 0;
                    } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create FM SNMP Credentials not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create secret object");
            push @::JOB_STATUS, "(x) Create FM SNMP Credentials failed";
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
    sub Configure_User_Credentials_For_SC_Release_Backup_Export_Using_SFTP_P929S01T02 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sc-bragent-secret.yaml";
        my $rc = 0;
        my @result;

        # Check to make sure we are deploying this secret only for SC releases 1.2 up to 1.6 because in SC 1.7 the bragent was removed
        unless ($::JOB_PARAMS{'SC_RELEASE_VERSION'} =~ /^1\.[2-6]\.\d+$/) {
            General::Logging::log_user_info_message("Task is skipped because SC_RELEASE_VERSION is not 1.2 to 1.6");
            push @::JOB_STATUS, "(-) Create BR Agent Credentials skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        unless (-f $filename) {
            General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create BR Agent Credentials skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Creating secret object");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $sc_namespace create --filename=$filename",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create BR Agent Credentials successful";
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create BR Agent Credentials not needed";
                        return 0;
                    } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create BR Agent Credentials not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create secret object");
            push @::JOB_STATUS, "(x) Create BR Agent Credentials failed";
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
    sub Configure_User_Credentials_For_Admin_User_P929S01T03 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ldap-secret.yaml";
        my $rc = 0;
        my @result;

        unless (-f $filename) {
            General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create LDAP Credentials skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Creating secret object");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $sc_namespace create --filename=$filename",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create LDAP Credentials successful";
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create LDAP Credentials not needed";
                        return 0;
                    } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create LDAP Credentials not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create secret object");
            push @::JOB_STATUS, "(x) Create LDAP Credentials failed";
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
    sub Configure_User_Credentials_For_Accessing_Wide_Column_Database_CD_Service_P929S01T04 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my @filenames = (
            "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/wcdbcd-admin-secret.yaml",
            "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/wcdbcd-admin-secret-bsf.yaml",
            "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/wcdbcd-admin-secret-dsc.yaml",
            "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/wcdbcd-day0-secret.yaml",
            "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/wcdbcd-day0-secret-bsf.yaml",
            "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/wcdbcd-day0-secret-dsc.yaml",
            "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/bsf-db-secret.yaml",
        );
        my $name;
        my $rc = 0;
        my @result;

        for my $filename (@filenames) {
            $name = basename $filename;
            $name =~ s/\.yaml//;
            unless (-f $filename) {
                General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
                push @::JOB_STATUS, "(-) Create $name skipped";
                next;
            }

            General::Logging::log_user_message("Creating secret object for file:\n$filename");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $sc_namespace create --filename=$filename",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) Create $name successful";
            } else {
                if ($warn_existing_secrets == 1) {
                    for (@result) {
                        if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                            # Ignore the error and just report it as a warning
                            General::Logging::log_user_warning_message($1);
                            push @::JOB_STATUS, "(-) Create $name not needed";
                            return 0;
                        } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                            # Ignore the error and just report it as a warning
                            General::Logging::log_user_warning_message($1);
                            push @::JOB_STATUS, "(-) Create $name not needed";
                            return 0;
                        }
                    }
                }
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failure to create secret object");
                push @::JOB_STATUS, "(x) Create $name failed";

                # Exit the loop with an error
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
    sub Prepare_Day0_Certificates_For_BSF_P929S01T05 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sec-certm-deployment-configuration.json";
        my $rc = 0;
        my @result;

        # Check to make sure we are deploying BSF and WCDB and the software release is SC 1.4 or newer.
        unless ($::JOB_PARAMS{'SC_RELEASE_VERSION'} =~ /^1\.([4-9]|[1-9][0-9])\.\d+$/ && $::JOB_PARAMS{'nodetype'} =~ /bsf/ && $::JOB_PARAMS{'nodetype'} =~ /wcdb/) {
            General::Logging::log_user_info_message("Task is skipped because SC_RELEASE_VERSION is not 1.4 or higher and/or 'nodetype' does not include 'bsf' and 'wcdb'");
            push @::JOB_STATUS, "(-) Create Day-0 Certificates for BSF skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        unless (-f $filename) {
            General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create Day-0 Certificates for BSF skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Creating secret object");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} create secret generic eric-sec-certm-deployment-configuration --namespace $sc_namespace --from-file=$filename",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create Day-0 Certificates for BSF was successful";
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create Day-0 Certificates for BSF not needed";
                        return 0;
                    } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create Day-0 Certificates for BSF not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create secret object");
            push @::JOB_STATUS, "(x) Create Day-0 Certificates for BSF failed";
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
    sub Configure_User_Credentials_For_Accessing_Document_Database_PG_Service_P929S01T06 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/document_database_pg_secret.yaml";
        my $rc = 0;
        my @result;

        unless (-f $filename) {
            General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create PG Credentials skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Creating secret object");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $sc_namespace create --filename=$filename",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create PG Credentials successful";
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create PG Credentials not needed";
                        return 0;
                    } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create PG Credentials not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create secret object");
            push @::JOB_STATUS, "(x) Create PG Credentials failed";
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
    sub Configure_User_Credentials_For_SC_Accessing_Distributed_Coordinator_ED_SC_Service_P929S01T07 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/distributed_coordinator_ed_sc_secret.yaml";
        my $rc = 0;
        my @result;

        if (version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) >= version->parse( "1.12.0" ) ) {
            General::Logging::log_user_info_message("Task is skipped because SC_RELEASE_VERSION is 1.12 or higher");
            push @::JOB_STATUS, "(-) Create DCES Credentials skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        unless (-f $filename) {
            General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create DCES Credentials skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Creating secret object");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $sc_namespace create --filename=$filename",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create DCES Credentials successful";
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create DCES Credentials not needed";
                        return 0;
                    } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create DCES Credentials not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create secret object");
            push @::JOB_STATUS, "(x) Create DCES Credentials failed";
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
    sub Configure_User_Credentials_For_Accessing_Distributed_Coordinator_ED_Service_P929S01T08 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/distributed_coordinator_ed_secret.yaml";
        my $rc = 0;
        my @result;

        if (version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) >= version->parse( "1.12.0" ) ) {
            General::Logging::log_user_info_message("Task is skipped because SC_RELEASE_VERSION is 1.12 or higher");
            push @::JOB_STATUS, "(-) Create DCE Credentials skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        unless (-f $filename) {
            General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create DCE Credentials skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Creating secret object");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $sc_namespace create --filename=$filename",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create DCE Credentials successful";
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create DCE Credentials not needed";
                        return 0;
                    } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create DCE Credentials not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create secret object");
            push @::JOB_STATUS, "(x) Create DCE Credentials failed";
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
    sub Configure_User_Credentials_For_Accessing_SC_Monitor_Service_P929S01T09 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/monitor-secret.yaml";
        my $rc = 0;
        my @result;

        unless (-f $filename) {
            General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create SC Monitor Credentials skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Creating secret object");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $sc_namespace create --filename=$filename",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create SC Monitor Credentials successful";
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create SC Monitor Credentials not needed";
                        return 0;
                    } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create SC Monitor Credentials not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create secret object");
            push @::JOB_STATUS, "(x) Create SC Monitor Credentials failed";
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
    sub Configure_SFTP_Server_Configuration_P929S01T10 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/sftpConfig.json";
        my $rc = 0;
        my @result;
        my $secret_name = $::JOB_PARAMS{'sftp_server_secret_name'};

        # Check to make sure we are deploying software release is SC 1.7 or newer.
        unless ($::JOB_PARAMS{'SC_RELEASE_VERSION'} =~ /^1\.([7-9]|[1-9][0-9])\.\d+$/) {
            General::Logging::log_user_info_message("Task is skipped because SC_RELEASE_VERSION is not 1.7 or higher");
            push @::JOB_STATUS, "(-) Create SFTP Server Credentials skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        unless (-f $filename) {
            General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create SFTP Server Credentials skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Creating secret object");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $sc_namespace create secret generic $secret_name --from-file=$filename",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create SFTP Server Credentials successful";
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create SFTP Server Credentials not needed";
                        return 0;
                    } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create SFTP Server Credentials not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create secret object");
            push @::JOB_STATUS, "(x) Create SFTP Server Credentials failed";
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
    sub Configure_TAP_Config_Map_P929S01T11 {

        my $config_map_name = "eric-sc-tap-config";
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/tap_config.json";
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        # Check to make sure we are deploying software release is SC 1.7 or newer.
        unless ($::JOB_PARAMS{'SC_RELEASE_VERSION'} =~ /^1\.([7-9]|[1-9][0-9])\.\d+$/) {
            General::Logging::log_user_info_message("Task is skipped because SC_RELEASE_VERSION is not 1.7 or higher");
            push @::JOB_STATUS, "(-) Create TAP Config Map skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        unless (-f $filename) {
            General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create TAP Config Map skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Creating TAP Config Map");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $sc_namespace create configmap $config_map_name --from-file=$filename",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create TAP Config Map successful";
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(configmaps .+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create TAP Config Map not needed";
                        return 0;
                    } elsif (/^error: failed to create configmap.+(configmaps.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create TAP Config Map not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create config map");
            push @::JOB_STATUS, "(x) Create TAP Config Map failed";
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
    sub Configure_User_Credentials_For_Accessing_SC_Embedded_CNOM_P929S01T12 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sc-oam-user-secret.yaml";
        my $rc = 0;
        my @result;

        # Check to make sure we are deploying software release is SC 1.8 or newer.
        unless ($::JOB_PARAMS{'SC_RELEASE_VERSION'} =~ /^1\.([8-9]|[1-9][0-9])\.\d+$/) {
            General::Logging::log_user_info_message("Task is skipped because SC_RELEASE_VERSION is not 1.8 or higher");
            push @::JOB_STATUS, "(-) Create Embedded CNOM Access skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        unless (-f $filename) {
            General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create Embedded CNOM Access skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Creating secret object");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $sc_namespace create --filename=$filename",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create Embedded CNOM Access successful";
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create Embedded CNOM Access not needed";
                        return 0;
                    } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create Embedded CNOM Access not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create secret object");
            push @::JOB_STATUS, "(x) Create Embedded CNOM Access failed";
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
    sub Configure_Day0_Admin_Secret_P929S01T13 {

        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/day0adminsecret.yaml";
        my $rc = 0;
        my @result;

        unless (-f $filename) {
            General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create Day0 Admin Secret skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Creating secret object");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $namespace apply --filename=$filename",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create Day0 Admin Secret successful";
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create Day0 Admin Secret not needed";
                        return 0;
                    } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create Day0 Admin Secret not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create secret object");
            push @::JOB_STATUS, "(x) Create Day0 Admin Secret failed";
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
    sub Configure_WCDBCD_DSC_Secret_P929S01T14 {

        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/dsc-wcdb-secret.yaml";
        my $rc = 0;
        my @result;

        unless (-f $filename) {
            General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create DSC WCDBCD Secret skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Creating secret object");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $namespace apply --filename=$filename",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create DSC WCDBCD Secret successful";
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create DSC WCDBCD Secret not needed";
                        return 0;
                    } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create DSC WCDBCD Secret not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create secret object");
            push @::JOB_STATUS, "(x) Create DSC WCDBCD Secret failed";
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
    sub Configure_PVTB_CI_Secret_P929S01T15 {

        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sec-certm-deployment-configuration.json";
        my $secretname = "eric-sec-certm-deployment-configuration";
        my $rc = 0;
        my @result;

        unless (-f $filename) {
            General::Logging::log_user_warning_message("No $filename file present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create PVTB CI Secret skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Creating secret object");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $namespace create secret generic $secretname --from-file=$filename",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create PVTB CI Secret successful";
        } else {
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create PVTB CI Secret not needed";
                        return 0;
                    } elsif (/^error: failed to create secret.+(secrets.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create PVTB CI Secret not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create secret object");
            push @::JOB_STATUS, "(x) Create PVTB IP Secret failed";
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
    sub Configure_Fault_Mappings_Config_Map_P929S01T16 {

        my $config_map_name = "eric-fh-alarm-handler-faultmappings";
        my $base_dirpath = "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software/Definitions/OtherTemplates";
        my $config_dirpath = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/faultmappings";
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        # Check to make sure we are only performing this task for SC 1.15.0 and newer.
        if (version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) < version->parse( "1.15.0" ) ) {
            General::Logging::log_user_info_message("Task is skipped because SC_RELEASE_VERSION is not 1.15 or higher");
            push @::JOB_STATUS, "(-) Create Fault Mappings Config Map skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Now check all unpacked umbrella files (done by 922_Handle_Software_Directory playlist when
        # UNPACK_ALL_UMBRELLA_TGZ_FILES=yes) and add file paths to array for later copying only
        # the wanted files based on deployed 'nodetype'.
        General::Logging::log_user_message("Finding 'faultmappings' files in unpacked software helm chart");
        my @files = ();
        # eric-lm-combined-server.json
        # eric-sec-certm.json
        # eric-sec-sip-tls.json
        # eric-stm-diameter.json
        # ericsson-bsf.json
        # ericsson-csa.json
        # ericsson-sc.json
        # ericsson-scp.json
        # ericsson-sepp.json
        for my $filepath (
            "$base_dirpath/eric-cloud-native-base/charts/eric-lm-combined-server/faultmappings/eric-lm-combined-server.json",
            "$base_dirpath/eric-cloud-native-base/charts/eric-sec-certm/faultmappings/eric-sec-certm.json",
            "$base_dirpath/eric-cloud-native-base/charts/eric-sec-sip-tls/faultmappings/eric-sec-sip-tls.json",
            "$base_dirpath/eric-sc-bsf/faultmappings/ericsson-bsf.json",
            "$base_dirpath/eric-sc-bsf/charts/eric-stm-diameter/faultmappings/eric-stm-diameter.json",
            "$base_dirpath/eric-sc-cs/faultmappings/ericsson-sc.json",
            "$base_dirpath/eric-sc-diameter/faultmappings/eric-dsc.json",   # New name, which then got changed back again to the line below
            "$base_dirpath/eric-dsc/faultmappings/eric-dsc.json",
            "$base_dirpath/eric-sc-scp/faultmappings/ericsson-scp.json",
            "$base_dirpath/eric-sc-sepp/faultmappings/ericsson-sepp.json"
        ) {
            if (-f "$filepath") {
                push @files, $filepath;
            }
        }

        unless (@files) {
            General::Logging::log_user_warning_message("No faultmappings files found.\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create Fault Mappings Config Map skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        if (-d "$config_dirpath") {
            General::Logging::log_user_message("Subdirectory 'faultmappings' already exists");
        } else {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "mkdir -p -m 777 $config_dirpath",
                    "hide-output"   => 1,
                }
            );
            if ($rc == 0) {
                General::Logging::log_user_message("Subdirectory 'faultmappings' created");
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failure to create subdirectory 'faultmappings'");
                push @::JOB_STATUS, "(x) Create Fault Mappings Config Map failed";
                return 1;
            }
        }

        # Check which application fault mappings files we should load based on the 'nodetype' value
        my %enabled_cnf = map { $_ => 1 } split /,/, $::JOB_PARAMS{'nodetype'};
        General::Logging::log_user_message("Finding 'faultmappings' files to copy based on nodetype=$::JOB_PARAMS{'nodetype'}");

        my @files_to_copy = ();
        for my $filename (@files) {
            if ($filename =~ /^.+eric-stm-diameter\.json$/) {
                if (exists $enabled_cnf{'stmdiameter'}) {
                    General::Logging::log_user_message("  File included (stmdiameter):         $filename");
                    push @files_to_copy, $filename;
                } else {
                    General::Logging::log_user_message("  File not included (stmdiameter):     $filename");
                }
            } elsif ($filename =~ /^.+ericsson-(bsf|csa|dsc|scp|sepp)\.json$/) {
                if (exists $enabled_cnf{$1}) {
                    General::Logging::log_user_message("  File included ($1):                 $filename");
                    push @files_to_copy, $filename;
                } else {
                    General::Logging::log_user_message("  File not included ($1):             $filename");
                }
            } elsif ($filename =~ /^.+eric-(dsc)\.json$/) {
                if (exists $enabled_cnf{$1}) {
                    General::Logging::log_user_message("  File included ($1):                 $filename");
                    push @files_to_copy, $filename;
                } else {
                    General::Logging::log_user_message("  File not included ($1):             $filename");
                }
            } else {
                # Other files we always include
                    General::Logging::log_user_message("  File always included:                $filename");
                push @files_to_copy, $filename;
            }
        }

        # Now only copy the files we need from base_dirpath to config_path
        General::Logging::log_user_message("Copying needed faultmappings files to config directory");
        for my $filename (@files_to_copy) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cp -fp $filename $config_dirpath",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failure to copy faultmappings file: $filename");
                push @::JOB_STATUS, "(x) Create Fault Mappings Config Map failed";
                return 1;
            }
        }

        General::Logging::log_user_message("Creating Fault Mappings Config Map");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $sc_namespace create configmap $config_map_name --from-file=$config_dirpath",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create Fault Mappings Config Map successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create config map");
            if ($warn_existing_secrets == 1) {
                for (@result) {
                    if (/^Error from server.+AlreadyExists.+(configmaps .+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create Fault Mappings Config Map not needed";
                        return 0;
                    } elsif (/^error: failed to create configmap.+(configmaps.+already exists)/) {
                        # Ignore the error and just report it as a warning
                        General::Logging::log_user_warning_message($1);
                        push @::JOB_STATUS, "(-) Create Fault Mappings Config Map not needed";
                        return 0;
                    }
                }
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failure to create config map");
            push @::JOB_STATUS, "(x) Create Fault Mappings Config Map failed";
        }

        return $rc;
    }
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P929S99 {

    my $rc = 0;

    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    return $rc;
}

#####################
#                   #
# Other Subroutines #
#                   #
#####################

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This playlist load secrets into the deployed application.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
