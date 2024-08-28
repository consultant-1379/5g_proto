package Playlist::930_Prepare_Secret_Files;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.16
#  Date     : 2024-05-21 18:15:58
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

#
# Used Perl package files
#

use General::Data_Structure_Operations;
use General::Json_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;
use General::String_Operations;
use General::Yaml_Operations;

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

    $rc = General::Playlist_Operations::execute_step( \&Check_Configuration_Files_If_Specified_P930S01, \&Fallback001_P930S99 );
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
sub Check_Configuration_Files_If_Specified_P930S01 {

    my $rc;

    if (exists $::JOB_PARAMS{'CONFIG_FILE_SNMP_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_SNMP_Secret_P930S01T01 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_BRAGENT_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_BRAGENT_Secret_P930S01T02 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_LDAP_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_LDAP_Secret_P930S01T03 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_DOCUMENT_DATABASE_PG_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_DOCUMENT_DATABASE_PG_Secret_P930S01T04 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_DISTRIBUTED_COORDINATOR_ED_SC_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_DISTRIBUTED_COORDINATOR_ED_SC_Secret_P930S01T05 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_DISTRIBUTED_COORDINATOR_ED_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_DISTRIBUTED_COORDINATOR_ED_Secret_P930S01T06 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_WCDBCD_ADMIN_SECRET'} && version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) < version->parse( "1.15.0")) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_WCDBCD_ADMIN_Secret_P930S01T07 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_WCDBCD_ADMIN_SECRET_BSF'} && version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) >= version->parse( "1.15.0")) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_WCDBCD_ADMIN_Secret_BSF_P930S01T08 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_WCDBCD_ADMIN_SECRET_DSC'} && version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) >= version->parse( "1.15.0")) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_WCDBCD_ADMIN_Secret_DSC_P930S01T09 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_WCDBCD_DAY0_SECRET'} && version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) < version->parse( "1.15.0")) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_WCDBCD_DAY0_Secret_P930S01T10 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_WCDBCD_DAY0_SECRET'} && version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) >= version->parse( "1.15.0")) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_WCDBCD_DAY0_Secret_BSF_P930S01T11 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_WCDBCD_DAY0_SECRET'} && version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) >= version->parse( "1.15.0")) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_WCDBCD_DAY0_Secret_DSC_P930S01T12 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_BSF_DB_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_BSF_DB_Secret_P930S01T13 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_WCDBCD_DAY0_CERTIFICATE'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_WCDBCD_DAY0_Certificate_P930S01T14 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_MONITOR_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Monitor_Secret_P930S01T15 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_SFTP_SERVER_SECRET'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_SFTP_Server_Secret_P930S01T16 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_TAP_CONFIG_MAP'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Tap_Config_Map_File_P930S01T17 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_EMBEDDED_CNOM_ACCESS'}) {
        #
        # SC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Embedded_CNOM_Access_File_P930S01T18 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_DAY0_ADMIN_SECRET'}) {
        #
        # cnDSC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Day0_Admin_Secret_P930S01T19 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_WCDBCD_DSC_SECRET'}) {
        #
        # cnDSC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_WCDBCD_DSC_Secret_P930S01T20 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'CONFIG_FILE_PVTB_CI_SECRET'}) {
        #
        # cnDSC Specific logic
        #
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_PVTB_CI_Secret_P930S01T21 } );
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
    sub Check_SNMP_Secret_P930S01T01 {

        my $parameter = "CONFIG_FILE_SNMP_SECRET";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/config.json";
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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "snmp_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for\d{2}$/) {
                    # Old naming format with a 2 digit number in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                    push @key_values, $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the JSON file into an hash and replace all values with values from the network config file
                my %json_contents;
                my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/config_Template.json";
                General::Logging::log_user_message(" - Parsing JSON data in template file");
                $rc = General::Json_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%json_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing JSON data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%json_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated JSON hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Json_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%json_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_BRAGENT_Secret_P930S01T02 {

        my $parameter = "CONFIG_FILE_BRAGENT_SECRET";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sc-bragent-secret.yaml";
        my $rc = 0;

        # Check to make sure we are deploying this secret only for SC releases 1.2 up to 1.6 because in SC 1.7 the bragent was removed
        unless ($::JOB_PARAMS{'SC_RELEASE_VERSION'} =~ /^1\.[2-6]\.\d+$/) {
            General::Logging::log_user_info_message("Job parameter '$parameter' is skipped because SC_RELEASE_VERSION is not 1.2 to 1.6");
            return General::Playlist_Operations::RC_TASKOUT;
        }

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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "bragent_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for\d{2}$/) {
                    # Old naming format with a 2 digit number in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                    push @key_values, $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/eric-sc-bragent-secret_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_LDAP_Secret_P930S01T03 {

        my $parameter = "CONFIG_FILE_LDAP_SECRET";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/ldap-secret.yaml";
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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "ldap_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for\d{2}$/) {
                    # Old naming format with a 2 digit number in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                    push @key_values, $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/ldap-secret_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_DOCUMENT_DATABASE_PG_Secret_P930S01T04 {

        my $parameter = "CONFIG_FILE_DOCUMENT_DATABASE_PG_SECRET";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/document_database_pg_secret.yaml";
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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "document_database_pg_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for\d{2}$/) {
                    # Old naming format with a 2 digit number in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                    push @key_values, $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/document_database_pg_secret_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_DISTRIBUTED_COORDINATOR_ED_SC_Secret_P930S01T05 {

        my $parameter = "CONFIG_FILE_DISTRIBUTED_COORDINATOR_ED_SC_SECRET";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/distributed_coordinator_ed_sc_secret.yaml";
        my $rc = 0;

        if (version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) >= version->parse( "1.12.0" ) ) {
            General::Logging::log_user_info_message("Task is skipped because SC_RELEASE_VERSION is 1.12 or higher");
            return General::Playlist_Operations::RC_TASKOUT;
        }

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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "distributed_coordinator_ed_sc_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for\d{2}$/) {
                    # Old naming format with a 2 digit number in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                    push @key_values, $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/distributed_coordinator_ed_sc_secret_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_DISTRIBUTED_COORDINATOR_ED_Secret_P930S01T06 {

        my $parameter = "CONFIG_FILE_DISTRIBUTED_COORDINATOR_ED_SECRET";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/distributed_coordinator_ed_secret.yaml";
        my $rc = 0;

        if (version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) >= version->parse( "1.12.0" ) ) {
            General::Logging::log_user_info_message("Task is skipped because SC_RELEASE_VERSION is 1.12 or higher");
            return General::Playlist_Operations::RC_TASKOUT;
        }

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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "distributed_coordinator_ed_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for\d{2}$/) {
                    # Old naming format with a 2 digit number in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                    push @key_values, $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/distributed_coordinator_ed_secret_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_WCDBCD_ADMIN_Secret_P930S01T07 {

        my $parameter = "CONFIG_FILE_WCDBCD_ADMIN_SECRET";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/wcdbcd-admin-secret.yaml";
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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "wcdbcd_admin_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for\d{2}$/) {
                    # Old naming format with a 2 digit number in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                    push @key_values, $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "";
                # Old releases
                $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/wcdbcd-admin-secret_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_WCDBCD_ADMIN_Secret_BSF_P930S01T08 {

        my $parameter = "CONFIG_FILE_WCDBCD_ADMIN_SECRET_BSF";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/wcdbcd-admin-secret-bsf.yaml";
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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "wcdbcd_admin_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for\d{2}$/) {
                    # Old naming format with a 2 digit number in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                    push @key_values, $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "";
                # New CNCS based releases where the name of the secret was changed
                $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/wcdbcd-admin-secret_CNCS_bsf_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_WCDBCD_ADMIN_Secret_DSC_P930S01T09 {

        my $parameter = "CONFIG_FILE_WCDBCD_ADMIN_SECRET_DSC";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/wcdbcd-admin-secret-dsc.yaml";
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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "wcdbcd_admin_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for\d{2}$/) {
                    # Old naming format with a 2 digit number in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                    push @key_values, $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "";
                # New CNCS based releases where the name of the secret was changed
                $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/wcdbcd-admin-secret_CNCS_dsc_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_WCDBCD_DAY0_Secret_P930S01T10 {

        my $parameter = "CONFIG_FILE_WCDBCD_DAY0_SECRET";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/wcdbcd-day0-secret.yaml";
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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "wcdbcd_day0_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for\d{2}$/) {
                    # Old naming format with a 2 digit number in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                    push @key_values, $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "";
                # Old releases
                $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/wcdbcd-day0-secret_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_WCDBCD_DAY0_Secret_BSF_P930S01T11 {

        my $parameter = "CONFIG_FILE_WCDBCD_DAY0_SECRET_BSF";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/wcdbcd-day0-secret-bsf.yaml";
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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "wcdbcd_day0_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for\d{2}$/) {
                    # Old naming format with a 2 digit number in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                    push @key_values, $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "";
                # New CNCS based releases where the name of the secret was changed
                $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/wcdbcd-day0-secret_CNCS_bsf_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_WCDBCD_DAY0_Secret_DSC_P930S01T12 {

        my $parameter = "CONFIG_FILE_WCDBCD_DAY0_SECRET_DSC";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/wcdbcd-day0-secret-dsc.yaml";
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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "wcdbcd_day0_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for\d{2}$/) {
                    # Old naming format with a 2 digit number in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                    push @key_values, $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "";
                # New CNCS based releases where the name of the secret was changed
                $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/wcdbcd-day0-secret_CNCS_dsc_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_BSF_DB_Secret_P930S01T13 {

        my $parameter = "CONFIG_FILE_BSF_DB_SECRET";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/bsf-db-secret.yaml";
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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "bsf_db_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for\d{2}$/) {
                    # Old naming format with a 2 digit number in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                    push @key_values, $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/bsf-db-secret_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_WCDBCD_DAY0_Certificate_P930S01T14 {

        my $parameter = "CONFIG_FILE_WCDBCD_DAY0_CERTIFICATE";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sec-certm-deployment-configuration.json";
        my $rc = 0;

        # Only handle this parameter if GEOGRAPHICAL_REDUNDANCY=yes
        if ($::JOB_PARAMS{'GEOGRAPHICAL_REDUNDANCY'} eq "no") {
            General::Logging::log_user_message("Job parameter '$parameter' is skipped because GEOGRAPHICAL_REDUNDANCY=no");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Check to make sure we are deploying BSF and WCDB and the software release is SC 1.4 or newer.
        unless ($::JOB_PARAMS{'SC_RELEASE_VERSION'} =~ /^1\.([4-9]|[1-9][0-9])\.\d+$/ && $::JOB_PARAMS{'nodetype'} =~ /bsf/ && $::JOB_PARAMS{'nodetype'} =~ /wcdb/) {
            General::Logging::log_user_info_message("Job parameter '$parameter' is skipped because SC_RELEASE_VERSION is not 1.4 or higher and/or 'nodetype' does not include 'bsf' and 'wcdb'");
            return General::Playlist_Operations::RC_TASKOUT;
        }

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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "wcdbcd_day0_certificate_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for(\S+)$/) {
                    # New naming format with parameter name included in key
                    my $key_value = "$1";
                    # TODO: Do we want to add support to generate certificate data in case the
                    # value is CHANGEME, or possibly a new value GENERATECA or GENERATECERT?
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the JSON file into an hash and replace all values with values from the network config file
                my %json_contents;
                my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/eric-sec-certm-pvtb-deployment-configuration_Template.json";
                General::Logging::log_user_message(" - Parsing JSON data in template file");
                $rc = General::Json_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%json_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing JSON data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%json_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated JSON hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Json_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%json_contents,
                        "sort-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_Monitor_Secret_P930S01T15 {

        my $parameter = "CONFIG_FILE_MONITOR_SECRET";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/monitor-secret.yaml";
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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "monitor_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for\d{2}$/) {
                    # Old naming format with a 2 digit number in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.+=CHANGEME$/);
                    push @key_values, $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/monitor-secret_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_SFTP_Server_Secret_P930S01T16 {

        my $parameter = "CONFIG_FILE_SFTP_SERVER_SECRET";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/sftpConfig.json";
        my $rc = 0;

        # Check to make sure we are deploying software release is SC 1.7 or newer.
        unless ($::JOB_PARAMS{'SC_RELEASE_VERSION'} =~ /^1\.([7-9]|[1-9][0-9])\.\d+$/) {
            General::Logging::log_user_info_message("Job parameter '$parameter' is skipped because SC_RELEASE_VERSION is not 1.7 or higher");
            return General::Playlist_Operations::RC_TASKOUT;
        }

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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "sftp_server_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for(\S+)$/) {
                    # New naming format with parameter name included in key
                    my $key_value = "$1";
                    # TODO: Do we want to add support to generate certificate data in case the
                    # value is CHANGEME, or possibly a new value GENERATECA or GENERATECERT?
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the JSON file into an hash/array and replace all values with values from the network config file
                my $json_contents;
                my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/sftpConfig_Template.json";
                General::Logging::log_user_message(" - Parsing JSON data in template file");
                $json_contents = General::Json_Operations::read_file_return_reference(
                    {
                        "filename"      => $template_file,
                    }
                );
                unless (defined $json_contents) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing JSON data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => $json_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash/array with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated JSON hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Json_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => $json_contents,
                        "sort-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_Tap_Config_Map_File_P930S01T17 {

        my $parameter = "CONFIG_FILE_TAP_CONFIG_MAP";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/tap_config.json";
        my $rc = 0;

        # Check to make sure we are deploying software release is SC 1.7 or newer.
        unless ($::JOB_PARAMS{'SC_RELEASE_VERSION'} =~ /^1\.([7-9]|[1-9][0-9])\.\d+$/) {
            General::Logging::log_user_info_message("Job parameter '$parameter' is skipped because SC_RELEASE_VERSION is not 1.7 or higher");
            return General::Playlist_Operations::RC_TASKOUT;
        }

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
            # Use the template file as a base
            General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from a template file");
            my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/tap_config_Template.json";
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cp -fpv $template_file $output_file",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy file '$template_file'");
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
    sub Check_Embedded_CNOM_Access_File_P930S01T18 {

        my $parameter = "CONFIG_FILE_EMBEDDED_CNOM_ACCESS";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sc-oam-user-secret.yaml";
        my $rc = 0;

        # Check to make sure we are deploying software release is SC 1.8 or newer.
        unless ($::JOB_PARAMS{'SC_RELEASE_VERSION'} =~ /^1\.([8-9]|[1-9][0-9])\.\d+$/) {
            General::Logging::log_user_info_message("Job parameter '$parameter' is skipped because SC_RELEASE_VERSION is not 1.8 or higher");
            return General::Playlist_Operations::RC_TASKOUT;
        }

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
            # Use the template file as a base
            General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from a template file");
            my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/sc-oam-user-secret_Template.yaml";
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cp -fpv $template_file $output_file",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy file '$template_file'");
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
    sub Check_Day0_Admin_Secret_P930S01T19 {

        my $parameter = "CONFIG_FILE_DAY0_ADMIN_SECRET";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/day0adminsecret.yaml";
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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "day0_admin_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/day0adminsecret_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_WCDBCD_DSC_Secret_P930S01T20 {

        my $parameter = "CONFIG_FILE_WCDBCD_DSC_SECRET";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/dsc-wcdb-secret.yaml";
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
            # Replace all hash values with values from the network config file
            my @key_values = ();
            my $look_for = "dsc_wcdb_secret_hash_parameter_";
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                if ($key =~ /^$look_for(\S+)$/) {
                    my $key_value = "$1";
                    # New naming format with parameter name included in key
                    next if ($::NETWORK_CONFIG_PARAMS{$key}{'value'} =~ /^.*CHANGEME.*$/);
                    push @key_values, "$key_value=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                }
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from network configuration parameters");

                # Parse the YAML file into an hash and replace all values with values from the network config file
                my %yaml_contents;
                my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/dsc-wcdb-secret_Template.yaml";
                General::Logging::log_user_message(" - Parsing YAML data in template file");
                $rc = General::Yaml_Operations::read_file(
                    {
                        "filename"      => $template_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                    return 1;
                }

                General::Logging::log_user_message(" - Replacing YAML data from template file with network configuration parameter values");
                # Add or update the hash value
                $rc = General::Data_Structure_Operations::update_hash_values(
                    {
                        "output-ref"    => \%yaml_contents,
                        "values"        => \@key_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to update the hash with new values");
                    return 1;
                }

                General::Logging::log_user_message(" - Values updated");

                # Write the updated YAML hash back to file
                General::Logging::log_user_message(" - Write file '$output_file'");
                $rc = General::Yaml_Operations::write_file(
                    {
                        "filename"      => $output_file,
                        "output-ref"    => \%yaml_contents,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to write file '$output_file'");
                    return 1;
                }
            } else {
                General::Logging::log_user_warning_message("Job parameter '$parameter' not specified and no network configuration parameters configured which might cause a failed deployment");
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
    sub Check_PVTB_CI_Secret_P930S01T21 {

        my $parameter = "CONFIG_FILE_PVTB_CI_SECRET";
        my $input_file = $::JOB_PARAMS{$parameter};
        my $output_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sec-certm-deployment-configuration.json";
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
            General::Logging::log_user_message("Job parameter '$parameter' not specified, a file will be generated from template");

            # Parse the YAML file into an hash and replace all values with values from the network config file
            my %json_contents;
            my $template_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/eric-sec-certm-pvtb-deployment-configuration_Template.json";
            General::Logging::log_user_message(" - Parsing JSON data in template file");
            $rc = General::Yaml_Operations::read_file(
                {
                    "filename"      => $template_file,
                    "output-ref"    => \%json_contents,
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to parse file '$template_file'");
                return 1;
            }

            # Write the updated YAML hash back to file
            General::Logging::log_user_message(" - Write file '$output_file'");
            $rc = General::Yaml_Operations::write_file(
                {
                    "filename"      => $output_file,
                    "output-ref"    => \%json_contents,
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

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P930S99 {

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
sub usage {
    print <<EOF;

Description:
============

This playlist prepares secrets and values files to be used for deploy of the
software on a SC node.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
