package Playlist::934_Prepare_Value_Files;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.23
#  Date     : 2024-06-14 11:27:43
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
use version;

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    usage
    );

#
# Used Perl package files
#

use ADP::Kubernetes_Operations;
use General::Data_Structure_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;
use General::String_Operations;
use General::Yaml_Operations;
use boolean;

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

    $rc = General::Playlist_Operations::execute_step( \&Handle_Helm_Chart_Files_P934S01, \&Fallback001_P934S99 );
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
sub Handle_Helm_Chart_Files_P934S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Handle_Main_Helm_Chart_File_P934S01T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Handle_Secondary_Helm_Chart_Files_P934S01T02 } );
    return $rc if $rc < 0;

    if (exists $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} && $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} =~ /\|/) {
        # It's a new SC release (>=1.15) that contains multiple helm charts.
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Handle_Recording_Rule_Files_P934S01T03 } );
        return $rc if $rc < 0;
    } else {
        $::JOB_PARAMS{'RECORDING_RULES_VALUES_FILE'} = "";
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
    sub Handle_Main_Helm_Chart_File_P934S01T01 {

        my $parameter = "CONFIG_FILE_HELM_CHART";
        my $failed_fetching_old_values = 0;
        my $input_file = $::JOB_PARAMS{$parameter};
        my $key_value;
        my $key_value_part1;
        my $key_value_part2;
        my $log_messages = "";
        my @nodetypes = ();
        my @old_helm_values = ();
        my %old_helm_values;
        my $output_path = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}";
        my $rc = 0;
        my $sc_values_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sc-values.yaml";
        my $sc_values_software_file = $::JOB_PARAMS{'VALUES_FILE_NAME'};
        my $sc_values_template_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sc-values-template.yaml";
        my $values_updated = 0;
        my %yaml_contents;
        my $disable_global_enabled_flags = 1;   # Change to 0 if we should not disable all global enabled flags from template file
        my $execute_global_enabled_logic = (exists $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} && $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} =~ /\|/) ? 0 : 1;  # Execute global.ericsson.XXX.enabled logic only when we have one helm chart, not with many when deploying cnDSC and CNCS

        if (exists $::JOB_PARAMS{$parameter} && $::JOB_PARAMS{$parameter} ne "") {
            # The user specified a values.yaml file so we perform no check of validity of the values
            # in the file since this is the user's responisibility to provide valid values.
            General::Logging::log_user_message("Checking Job parameter '$parameter' and creating a copy of the file");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cp -fpv $input_file $output_path/eric-sc-values.yaml",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy file '$input_file'");
                return $rc;
            }

            # Read the helm chart file and extract out the node type scp, bsf etc.
            General::Logging::log_user_message("Reading $parameter file");
            $rc = General::Yaml_Operations::read_file(
                {
                    "filename"      => $input_file,
                    "output-ref"    => \%yaml_contents,
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to parse file '$input_file'");
                return 1;
            }
        } else {
            # No file was specified by the user so try to generate one using the values file from
            # the software directory.
            General::Logging::log_user_message("No 'eric-sc-values.yaml' file exists, one will be generated from network configuration parameters");

            # Copy the helm chart included in the CSAR package
            General::Logging::log_user_message(" - Copying original deployment parameters file");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cp -fpv $sc_values_software_file $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy file '$sc_values_software_file'");
                return 1;
            }

            # Copy the helm chart included in the CSAR package as an template file
            General::Logging::log_user_message(" - Copying original deployment parameters file as an template file");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cp -fpv $sc_values_software_file $sc_values_template_file",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy file '$sc_values_software_file' to '$sc_values_template_file'");
                return 1;
            }

            # Check if we should read existing helm values in case we need to reuse the old value
            # when building the new eric-sc-values.yaml file.
            if (exists $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} && $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} ne "") {
                General::Logging::log_user_message(" - Reading user deployed values from node");
                $rc = ADP::Kubernetes_Operations::get_helm_release_extended_information(
                    {
                        "command"         => "values",
                        "hide-output"     => 1,
                        "namespace"       => $::JOB_PARAMS{'SC_NAMESPACE'},
                        "output-format"   => "dot-separated",
                        "release-name"    => $::JOB_PARAMS{'SC_RELEASE_NAME'},
                        "return-output"   => \@old_helm_values,
                    }
                );
                if ($rc == 0) {
                    for (@old_helm_values) {
                        if (/^(.+)?=(.*)/) {
                            $old_helm_values{$1} = $2;
                        }
                    }
                    # Save a copy of the data to a file, ignore any errors
                    General::File_Operations::write_file(
                        {
                            "filename"              => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sc-values_old_$::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}$::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'}.txt",
                            "output-ref"            => \@old_helm_values,
                            "hide-error-messages"   => 1,
                        }
                    );
                } else {
                    $failed_fetching_old_values = 1;
                }
            }

            # Create an array with all anchor values to be replaced, and also look for parameters
            # where we should copy the value from the already deployed system (in case of an upgrade).
            my @anchor_values = ();
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                next unless ($key =~ /^eric_sc_values_anchor_parameter_\S+$/);
                if (exists $::NETWORK_CONFIG_PARAMS{$key}{'valid_from_releases'} && exists $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}) {
                    # This is an upgrade and this parameter has the attribute 'valid_from_releases'.
                    # We need to check if the value should be used.
                    if ($::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} !~ /$::NETWORK_CONFIG_PARAMS{$key}{'valid_from_releases'}/) {
                        # The release we are trying to upgrade from does not match, so the parameter needs to be ignored.
                        $log_messages .= sprintf "%5d  %s\n", __LINE__, "Old release $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} not matching 'valid_from_releases' $::NETWORK_CONFIG_PARAMS{$key}{'valid_from_releases'}, key $key skipped";
                        next;
                    }
                }

                if ($key =~ /^eric_sc_values_anchor_parameter_\d{2}$/) {
                    # Old format
                    $key_value = $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^eric_sc_values_anchor_parameter_(\S+)$/) {
                    # New format
                    $key_value = "$1=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                } else {
                    $key_value = "";
                    printf "Error: Unknown format \$key=%s (Line=%d)\n", $key, __LINE__;
                }
                if ($key_value ne "") {
                    if ($key_value =~ /^(.+)?=(.*)/) {
                        $key_value_part1 = $1;
                        $key_value_part2 = $2;
                    } else {
                        # Unknown format
                        printf "Error: Unknown format \$key_value=%s (Line=%d)\n", $key_value, __LINE__;
                        next;
                    }
                    # Check if value from already deployed software should be reused instead of using value from network config file
                    if (exists $::NETWORK_CONFIG_PARAMS{$key}{'reuse_deployed_helm_value'} && $::NETWORK_CONFIG_PARAMS{$key}{'reuse_deployed_helm_value'} eq "true") {
                        if ($failed_fetching_old_values == 1) {
                            General::Logging::log_user_error_message("Failed to fetch old values from the deployed system and network configuration file parameter '$key' has attribute reuse_deployed_helm_value='true'");
                            return 1;
                        }
                        if (exists $old_helm_values{"definitions.$key_value_part1"}) {
                            if ($old_helm_values{"definitions.$key_value_part1"} =~ /[\.:]/) {
                                # The value contains a "." or a ":" so we need to single quote the value
                                $key_value = sprintf "%s='%s'", $key_value_part1, $old_helm_values{"definitions.$key_value_part1"};
                            } else {
                                $key_value = sprintf "%s=%s", $key_value_part1, $old_helm_values{"definitions.$key_value_part1"};
                            }
                        }
                    }

                    if ($key_value =~ /^.+?=.*CHANGEME.*/) {
                        # Ignore CHANGEME values since the parameter has not been changed
                        next;
                    }

                    push @anchor_values, $key_value;

                    $log_messages .= sprintf "%5d  %s\n", __LINE__, "Parameter $key, used value '$key_value' (network config value $::NETWORK_CONFIG_PARAMS{$key}{'value'})";
                }
            }
            if (scalar @anchor_values > 0) {
                General::Logging::log_user_message(" - Replace anchor values in template file");
                # Replace anchor values in template file with values from the network configuration file
                $rc = General::Yaml_Operations::update_file_anchor_values(
                    {
                        "filename"      => $sc_values_template_file,
                        "output-ref"    => \@anchor_values,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to replace anchor values in file '$sc_values_template_file'");
                    return 1;
                }
                $values_updated++;
            }

            # Parse the YAML file into an hash and replace all values with values from the network config file
            General::Logging::log_user_message(" - Parsing YAML data in template file");
            $rc = General::Yaml_Operations::read_file(
                {
                    "filename"      => $sc_values_template_file,
                    "output-ref"    => \%yaml_contents,
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to parse file '$sc_values_template_file'");
                return 1;
            }

            # Find out if only certain CNF's should be enabled accoring to the user.
            my %enabled_cnf;
            my %real_cnf = map { $_ => 1 } ADP::Kubernetes_Operations::get_list_of_known_cnf_types("sc", $::JOB_PARAMS{'SC_RELEASE_VERSION'});
            if (exists $::JOB_PARAMS{'ENABLED_CNF'} && $::JOB_PARAMS{'ENABLED_CNF'} ne "") {
                my $cnf_string = $::JOB_PARAMS{'ENABLED_CNF'};
                $cnf_string =~ s/-/ /g;     # Replace dashes with spaces
                $cnf_string =~ s/,/ /g;     # Replace commas with spaces
                $cnf_string =~ s/^\s+//g;   # Remove spaces at beginning
                $cnf_string =~ s/\s+$//g;   # Remove spaces at end
                if ($cnf_string eq "all") {
                    # We should not really come here because the main playlist should already
                    # have replaced the 'all' value with the appropriate values, but just in
                    # case we set them here as well.
                    $cnf_string = join " ", ADP::Kubernetes_Operations::get_list_of_known_cnf_types("sc", $::JOB_PARAMS{'SC_RELEASE_VERSION'});   # Replace 'all' with the currently known CNF's
                }
                for my $cnf (split /\s+/, $cnf_string) {
                    $enabled_cnf{$cnf} = 1;
                }

                if ($execute_global_enabled_logic == 1 && $disable_global_enabled_flags == 1) {
                    # We need to set all the global.ericsson.xxx.enabled flags to 'false' so that our
                    # network config file and DAFT ENABLED_CNF logic can create a properly filled in
                    # eric-values.yaml file.
                    if (exists $yaml_contents{'global'} && exists $yaml_contents{'global'}->{'ericsson'}) {
                        my $temp = $yaml_contents{'global'}->{'ericsson'};
                        for my $key (keys %$temp) {
                            if (exists $temp->{$key}->{'enabled'}) {
                                $temp->{$key}->{'enabled'} = false;
                            }
                        }
                    }
                }
            }

            # Replace all hash values with values from the network config file
            my @key_values = ();
            for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
                next unless ($key =~ /^eric_sc_values_hash_parameter_\S+$/);

                if ($key =~ /^eric_sc_values_hash_parameter_\d{2}$/) {
                    # Old format
                    $key_value = $::NETWORK_CONFIG_PARAMS{$key}{'value'};
                } elsif ($key =~ /^eric_sc_values_hash_parameter_(\S+)$/) {
                    # New format
                    $key_value = "$1=$::NETWORK_CONFIG_PARAMS{$key}{'value'}";
                } else {
                    $key_value = "";
                    printf "Error: Unknown format \$key=%s (Line=%d)\n", $key, __LINE__;
                }

                if ($key_value ne "") {
                    if ($key_value =~ /^(.+)?=(.*)/) {
                        $key_value_part1 = $1;
                        $key_value_part2 = $2;
                    } else {
                        # Unknown format
                        printf "Error: Unknown format \$key_value=%s (Line=%d)\n", $key_value, __LINE__;
                        next;
                    }

                    if ($key_value =~ /^(global\.ericsson\.(.+?)\.enabled)=.+/) {
                        # It's a CNF or NF Feature parameter.

                        if ($execute_global_enabled_logic == 0) {
                            # No need to check the global.ericsson.XXX.enabled parameters
                            next;
                        }

                        my $enabled_string = $1;
                        my $cnf = $2;

                        # Check if the value should be used for the upgrade path
                        if (exists $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} && $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} ne "" &&
                            exists $::NETWORK_CONFIG_PARAMS{$key}{'valid_from_releases'} &&
                            $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} !~ /$::NETWORK_CONFIG_PARAMS{$key}{'valid_from_releases'}/) {
                            # This is an upgrade and this parameter has the attribute 'valid_from_releases'.
                            # The release we are trying to upgrade from does not match 'valid_from_releases'. So this parameter should either:
                            # - Be completely removed and not set in the new eric-sc-values.yaml file.
                            # - If a new CNF where the enable flag is set to true in the template then it should be set to false.
                            #   Check if the parameter is trying to enable is a CNF, we don't want to enable a CNF that was not previously enabled.
                            if (exists $yaml_contents{'global'} &&
                                exists $yaml_contents{'global'}->{'ericsson'} &&
                                exists $yaml_contents{'global'}->{'ericsson'}->{$cnf} &&
                                exists $yaml_contents{'global'}->{'ericsson'}->{$cnf}->{'enabled'}) {

                                # The CNF exists in the template file and might be enabled or not.

                                if (exists $old_helm_values{$key_value_part1}) {
                                    # It strangely enough exists on the deployed system, so keep the old used value
                                    $key_value = sprintf "%s=%s", $key_value_part1, $old_helm_values{$key_value_part1};
                                    $log_messages .= sprintf "%5d  %s\n", __LINE__, "Parameter $key, used value '$key_value' (network config value $::NETWORK_CONFIG_PARAMS{$key}{'value'})";
                                } else {
                                    # We should disable the CNF to avoid enabling something that maybe should not be enabled
                                    # for an upgrade.
                                    $key_value = sprintf "%s=false", $key_value_part1;
                                    $log_messages .= sprintf "%5d  %s\n", __LINE__, "Parameter $key, used value '$key_value' (network config value $::NETWORK_CONFIG_PARAMS{$key}{'value'})";
                                }
                            } else {

                                # The value does not exist in the template file but exist in the network config file.
                                $log_messages .= sprintf "%5d  %s\n", __LINE__, "Parameter $key, value '$key_value' Ignored because 'valid_from_releases' does not match";
                                next;
                            }

                        # Check if value from already deployed software should be reused instead of using value from network config file
                        } elsif (exists $::NETWORK_CONFIG_PARAMS{$key}{'reuse_deployed_helm_value'} && $::NETWORK_CONFIG_PARAMS{$key}{'reuse_deployed_helm_value'} eq "true") {
                            if ($failed_fetching_old_values == 1) {
                                General::Logging::log_user_error_message("Failed to fetch old values from the deployed system and network configuration file parameter '$key' has attribute reuse_deployed_helm_value='true'");
                                return 1;
                            }
                            if (exists $old_helm_values{$key_value_part1}) {
                                # It is an upgrade case and we should reuse the old value from the deployed software instead of using the
                                # value from the network config file.
                                $key_value = sprintf "%s=%s", $key_value_part1, $old_helm_values{$key_value_part1};
                                $log_messages .= sprintf "%5d  %s\n", __LINE__, "Parameter $key, used value '$key_value' (network config value $::NETWORK_CONFIG_PARAMS{$key}{'value'}) old helm value used";
                            } else {
                                # Is a deployment or an upgrade and the value did not exist in the deployed software.
                                if (exists $enabled_cnf{$cnf}) {
                                    # The CNF should be enabled according to the user
                                    $key_value = "$enabled_string=true";
                                    $log_messages .= sprintf "%5d  %s\n", __LINE__, "Parameter $key, used value '$key_value' (network config value $::NETWORK_CONFIG_PARAMS{$key}{'value'}) user enabled value in ENABLED_CNF";
                                } elsif (exists $real_cnf{$cnf}) {
                                    # It is a real CNF (e.g. bsf, csa, scp or sepp) but the user does not want to have it enabled, therefore we disable this CNF
                                    $key_value = "$enabled_string=false";
                                    $log_messages .= sprintf "%5d  %s\n", __LINE__, "Parameter $key, used value '$key_value' (network config value $::NETWORK_CONFIG_PARAMS{$key}{'value'}) real CNF not wanted by user (not in ENABLED_CNF)";
                                } else {
                                    # It's not a real CNF (e.g. wcdb, pvtb, rlf, slf, spr, objectStorage or sftp), so we use whatever value comes from the network config file
                                    $log_messages .= sprintf "%5d  %s\n", __LINE__, "Parameter $key, used value '$key_value' (network config value $::NETWORK_CONFIG_PARAMS{$key}{'value'}) not real CNF, use network config value";
                                }
                            }
                        } else {
                            # It is a CNF enable parameter, check if the value should be changed
                            if (exists $enabled_cnf{$cnf}) {
                                # The CNF should be enabled according to the user
                                $key_value = "$enabled_string=true";
                                $log_messages .= sprintf "%5d  %s\n", __LINE__, "Parameter $key, used value '$key_value' (network config value $::NETWORK_CONFIG_PARAMS{$key}{'value'}) user enabled value in ENABLED_CNF";
                            } elsif (exists $real_cnf{$cnf}) {
                                # It is a real CNF (e.g. bsf, csa, scp or sepp) but the user does not want to have it enabled, therefore we disable this CNF
                                $key_value = "$enabled_string=false";
                                $log_messages .= sprintf "%5d  %s\n", __LINE__, "Parameter $key, used value '$key_value' (network config value $::NETWORK_CONFIG_PARAMS{$key}{'value'}) real CNF not wanted by user (not in ENABLED_CNF)";
                            } else {
                                # It's not a real CNF (e.g. wcdb, pvtb, rlf, slf, spr, objectStorage or sftp), so we use whatever value comes from the network config file
                                $log_messages .= sprintf "%5d  %s\n", __LINE__, "Parameter $key, used value '$key_value' (network config value $::NETWORK_CONFIG_PARAMS{$key}{'value'}) not real CNF, use network config value";
                            }
                        }

                    } else {
                        # It's not a CNF or NF Feature parameter (i.e. not a global.ericsson.XXXX.enabled parameter).

                        # Check if the value should be used for the upgrade path.
                        if (exists $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} &&
                            exists $::NETWORK_CONFIG_PARAMS{$key}{'valid_from_releases'} &&
                            $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} !~ /$::NETWORK_CONFIG_PARAMS{$key}{'valid_from_releases'}/) {
                            # This is an upgrade and this parameter has the attribute 'valid_from_releases'.
                            # The release we are trying to upgrade from does not match 'valid_from_releases' so the parameter from
                            # the network config file needs to be ignored and we don't care if it is set in the template or not.
                            $log_messages .= sprintf "%5d  %s\n", __LINE__, "Parameter $key, value '$key_value' Ignored because 'valid_from_releases' does not match";
                            next;

                        # Check if value from already deployed software should be reused instead of using value from network config file.
                        } elsif (exists $::NETWORK_CONFIG_PARAMS{$key}{'reuse_deployed_helm_value'} && $::NETWORK_CONFIG_PARAMS{$key}{'reuse_deployed_helm_value'} eq "true") {
                            if ($failed_fetching_old_values == 1) {
                                General::Logging::log_user_error_message("Failed to fetch old values from the deployed system and network configuration file parameter '$key' has attribute reuse_deployed_helm_value='true'");
                                return 1;
                            }
                            if (exists $old_helm_values{$key_value_part1}) {
                                # It is an upgrade case and we should reuse the old value from the deployed software instead of using the
                                # value from the network config file.
                                $key_value = sprintf "%s=%s", $key_value_part1, $old_helm_values{$key_value_part1};
                                $log_messages .= sprintf "%5d  %s\n", __LINE__, "Parameter $key, used value '$key_value' (network config value $::NETWORK_CONFIG_PARAMS{$key}{'value'})";
                            } else {
                                # It's not an upgrade case, or the value did not exist in the old release; so we just use the value from the network config file as-is.
                            }
                        } else {
                            # Just use the value from the network config file as-is.
                        }
                    }

                    if ($key_value =~ /^.+?=.*CHANGEME.*/) {
                        # Ignore CHANGEME values since the parameter has not been changed
                        next;
                    }

                    push @key_values, $key_value;
                }

            }

            # Special workaround to update the SWIM information.
            my $special_swim_handling = 1;  # Set to 0 to disable the special handling
            if ($special_swim_handling == 1) {
                # The SC 1.15.25 CSAR package still doesn't have the correct version and was hard coded to 1.15.0 and 1.15.0+5
                # so we now update these two parameters with the value we have read from the software CSAR file name.
                push @key_values, "swim.commercialVersion=$::JOB_PARAMS{'SC_RELEASE_VERSION'}";
                push @key_values, "swim.semanticVersion=$::JOB_PARAMS{'SC_RELEASE_VERSION'}$::JOB_PARAMS{'SC_RELEASE_BUILD'}";
            }

            # Special handling to avoid getting log flooding, see DND-67312
            my $special_dnd_67312_handling = 1;
            if ($special_dnd_67312_handling == 1 && version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) >= version->parse( "1.15.0" ) ) {
                # BSF
                if (exists $enabled_cnf{'bsf'}) {
                    push @key_values, "eric-sc-manager.features.monitoring.certificates.functions.bsf=true";
                } else {
                    push @key_values, "eric-sc-manager.features.monitoring.certificates.functions.bsf=false";
                }

                # SCP
                if (exists $enabled_cnf{'scp'}) {
                    push @key_values, "eric-sc-manager.features.monitoring.certificates.functions.scp=true";
                } else {
                    push @key_values, "eric-sc-manager.features.monitoring.certificates.functions.scp=false";
                }

                # SEPP
                if (exists $enabled_cnf{'sepp'}) {
                    push @key_values, "eric-sc-manager.features.monitoring.certificates.functions.sepp=true";
                } else {
                    push @key_values, "eric-sc-manager.features.monitoring.certificates.functions.sepp=false";
                }

                # Others
                push @key_values, "eric-sc-manager.features.monitoring.certificates.functions.csa=false";
                push @key_values, "eric-sc-manager.features.monitoring.certificates.functions.diameter=false";
            }

            if (scalar @key_values > 0) {
                General::Logging::log_user_message(" - Replacing other YAML data from template file with network configuration parameter values");
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

                $values_updated++;
            }

            General::Logging::log_user_message(" - Values updated") if $values_updated > 0;

            # Write the updated YAML hash back to file
            General::Logging::log_user_message(" - Write file '$sc_values_file'");
            $rc = General::Yaml_Operations::write_file(
                {
                    "filename"      => $sc_values_file,
                    "output-ref"    => \%yaml_contents,
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to write file '$sc_values_file'");
                return 1;
            }

            # A hack solution to get rid of empty "loadBalancerIPs:" lines in the file to make upgrades to SC 1.13 to work.
            General::Playlist_Operations::register_workaround_task("A workaround has been added to remove empty 'loadBalancerIPs:' lines in the generated eric-sc-values.yaml file to make upgrades to SC 1.13 to work, see DND-xxxxx.");
            $rc = General::OS_Operations::send_command(
                {
                    "commands"      => [
                        "sed -i \"/^\\s*loadBalancerIPs:\\s*''\\s*\$/d\" $sc_values_file",
                        "sed -i '/^\\s*loadBalancerIPs:\\s*\"\"\\s*\$/d' $sc_values_file",
                    ],
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Remove empty 'loadBalancerIPs:' lines in '$sc_values_file' file");
                return $rc;
            }

            # A hack solution to get rid of single quotes around certain number lines in the file to make deployments and upgrades to SC 1.15 to work.
            # Added after phone conversation with Kay and Thomas on 2024-05-13.
            General::Playlist_Operations::register_workaround_task("A workaround has been added to remove single quotes around certain number lines in the generated eric-sc-values.yaml file to make deployments and upgrades to SC 1.15 to work, see DND-xxxxx.");
            $rc = General::OS_Operations::send_command(
                {
                    "commands"      => [
                        "sed -Ei \"s/^(\\s*- port:)\\s*'(3868|3869)'/\\1 \\2/g\" $sc_values_file",
                        "sed -Ei \"s/^(\\s*targetport:)\\s*'(3868|3869)'/\\1 \\2/g\" $sc_values_file",
                    ],
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Remove single quotes around certain number lines in '$sc_values_file' file");
                return $rc;
            }
        }

        # Check which CNF's are currently enabled
        $::JOB_PARAMS{'nodetype'} = "";
        if ($execute_global_enabled_logic == 1) {
            # Older pre-CNCS (< 1.15)
            if (exists $yaml_contents{'global'} && exists $yaml_contents{'global'}->{'ericsson'}) {
                my $yaml = $yaml_contents{'global'}->{'ericsson'};
                for my $type (sort keys %$yaml) {
                    if (exists $yaml->{$type}->{'enabled'} && $yaml->{$type}->{'enabled'} =~ /^(true|1)$/i) {
                        push @nodetypes, lc($type);
                    }
                }
            }
            for my $type (sort @nodetypes) {
                $::JOB_PARAMS{'nodetype'} .= "$type,";
            }
        } else {
            # Newer post-CNCS (>= 1.15)
            # Find out which NF's are/will be deployed.
            my %nodetypes = map { $_ => 1 } split /[-,\s]+/, $::JOB_PARAMS{'ENABLED_CNF'};
            $nodetypes{'bsfdiameter'}   = 1 if (exists $yaml_contents{'eric-bsf-diameter'}              && exists $yaml_contents{'eric-bsf-diameter'}->{'enabled'}              && $yaml_contents{'eric-bsf-diameter'}->{'enabled'}             =~ /^(true|1)$/i);
            $nodetypes{'nlf'}           = 1 if (exists $yaml_contents{'eric-sc-nlf'}                    && exists $yaml_contents{'eric-sc-nlf'}->{'enabled'}                    && $yaml_contents{'eric-sc-nlf'}->{'enabled'}                   =~ /^(true|1)$/i);
            $nodetypes{'objectstorage'} = 1 if (exists $yaml_contents{'eric-data-object-storage-mn'}    && exists $yaml_contents{'eric-data-object-storage-mn'}->{'enabled'}    && $yaml_contents{'eric-data-object-storage-mn'}->{'enabled'}   =~ /^(true|1)$/i);
            $nodetypes{'pvtb'}          = 1 if (exists $yaml_contents{'eric-probe-virtual-tap-broker'}  && exists $yaml_contents{'eric-probe-virtual-tap-broker'}->{'enabled'}  && $yaml_contents{'eric-probe-virtual-tap-broker'}->{'enabled'} =~ /^(true|1)$/i);
            $nodetypes{'rlf'}           = 1 if (exists $yaml_contents{'eric-sc-rlf'}                    && exists $yaml_contents{'eric-sc-rlf'}->{'enabled'}                    && $yaml_contents{'eric-sc-rlf'}->{'enabled'}                   =~ /^(true|1)$/i);
            $nodetypes{'sftp'}          = 1 if (exists $yaml_contents{'eric-data-sftp-server'}          && exists $yaml_contents{'eric-data-sftp-server'}->{'enabled'}          && $yaml_contents{'eric-data-sftp-server'}->{'enabled'}         =~ /^(true|1)$/i);
            $nodetypes{'slf'}           = 1 if (exists $yaml_contents{'eric-sc-slf'}                    && exists $yaml_contents{'eric-sc-slf'}->{'enabled'}                    && $yaml_contents{'eric-sc-slf'}->{'enabled'}                   =~ /^(true|1)$/i);
            $nodetypes{'stmdiameter'}   = 1 if (exists $yaml_contents{'eric-stm-diameter'}              && exists $yaml_contents{'eric-stm-diameter'}->{'enabled'}              && $yaml_contents{'eric-stm-diameter'}->{'enabled'}             =~ /^(true|1)$/i);
            $nodetypes{'wcdb'}          = 1 if (exists $nodetypes{'bsf'});
            for my $type (sort keys %nodetypes) {
                $::JOB_PARAMS{'nodetype'} .= "$type,";
            }
        }

        $::JOB_PARAMS{'nodetype'} =~ s/,$//g;
        if ($::JOB_PARAMS{'nodetype'} ne "") {
            General::Logging::log_user_message("Enabled CNF and NF Features (nodetype): $::JOB_PARAMS{'nodetype'}");
        } else {
            General::Logging::log_user_warning_message("Enabled CNF and NF Features (nodetype): NONE\nUsing this eric-sc-values.yaml file might cause failed deployment or upgrade.");
        }

        if ($log_messages ne "") {
            General::Logging::log_write(sprintf "Task details for easier trouble shooting:\n%5s  %s\n%5s  %s\n%s", "Line", "Information", "-"x5, "-"x10, $log_messages);
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
    sub Handle_Secondary_Helm_Chart_Files_P934S01T02 {

        my $output_path = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}";
        my $rc = 0;

        # Check if the user want to have more than one helm chart values file
        for my $job_parameter (General::String_Operations::sort_hash_keys_alphanumerically(\%::JOB_PARAMS)) {
            if ($job_parameter =~ /^CONFIG_FILE_HELM_CHART_(\d+)$/) {
                General::Logging::log_user_message("Checking Job parameter '$job_parameter' and creating a copy of the file");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "cp -fpv $::JOB_PARAMS{$job_parameter} $output_path/eric-sc-values-$1.yaml",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to copy file '$::JOB_PARAMS{$job_parameter}'");
                    return $rc;
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
    sub Handle_Recording_Rule_Files_P934S01T03 {
        my $base_dirpath = "$::JOB_PARAMS{'SOFTWARE_DIR'}/pm-recording-rules";
        my $rc = 0;
        my @result;

        $::JOB_PARAMS{'RECORDING_RULES_VALUES_FILE'} = "";

        # Check to make sure we are deploying software release is SC 1.7 or newer.
        unless ($::JOB_PARAMS{'SC_RELEASE_VERSION'} =~ /^1\.(1[5-9]|[2-9][0-9])\.\d+$/) {
            General::Logging::log_user_info_message("Task is skipped because SC_RELEASE_VERSION is not 1.15 or higher");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        if (-d "$base_dirpath") {
            General::Logging::log_user_message("Using subdirectory 'pm-recording-rules' from software package");
        } elsif (-d "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/workarounds/cncs/eric-cloud-native-base/pm-recording-rules") {
            General::Playlist_Operations::register_workaround_task("A workaround has been added to create recording rules values file from DAFT package files instead of from software directory, see DND-64608.");
            General::Logging::log_user_message("Using subdirectory 'pm-recording-rules' from DAFT package");
            $base_dirpath = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/workarounds/cncs/eric-cloud-native-base/pm-recording-rules";
        } else {
            General::Logging::log_user_warning_message("No $base_dirpath directory present\nSkipping this task, Deployment might fail");
            push @::JOB_STATUS, "(-) Create Fault Mappings Config Map skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Check which pm recording rules files we should load based on the 'nodetype' value
        my %enabled_cnf = map { $_ => 1 } split /,/, $::JOB_PARAMS{'nodetype'};

        General::Logging::log_user_message("Finding 'pm-recording-rules' files to use based on nodetype=$::JOB_PARAMS{'nodetype'}");
        my @files = General::File_Operations::find_file( { "directory" => "$base_dirpath", "filename" => "*.yaml" } );
            # bsf-diameter.yaml
            # nlf.yaml
            # pvtb.yaml
            # rlf.yaml
            # sc-bsf.yaml
            # sc-cs.yaml
            # sc-diameter.yaml
            # scp.yaml
            # sepp.yaml
            # slf.yaml
        my %files_to_use;
        for my $filename (@files) {
            if ($filename =~ /^.+(bsf-diameter)\.yaml$/) {
                if (exists $enabled_cnf{'bsfdiameter'}) {
                    General::Logging::log_user_message("  File included (bsf-diameter):     $filename");
                    $files_to_use{$filename}++;
                } else {
                    General::Logging::log_user_message("  File not included (bsf-diameter): $filename");
                }
            } elsif ($filename =~ /^.+(sc-bsf)\.yaml$/) {
                if (exists $enabled_cnf{'bsf'}) {
                    General::Logging::log_user_message("  File included (bsf):              $filename");
                    $files_to_use{$filename}++;
                } else {
                    General::Logging::log_user_message("  File not included (bsf):          $filename");
                }
            } elsif ($filename =~ /^.+(nlf|rlf|scp|slf)\.yaml$/) {
                if (exists $enabled_cnf{$1}) {
                    General::Logging::log_user_message("  File included ($1):              $filename");
                    $files_to_use{$filename}++;
                } else {
                    General::Logging::log_user_message("  File not included ($1):          $filename");
                }
            } elsif ($filename =~ /^.+(sepp)\.yaml$/) {
                if (exists $enabled_cnf{'sepp'}) {
                    General::Logging::log_user_message("  File included ($1):             $filename");
                    $files_to_use{$filename}++;
                } else {
                    General::Logging::log_user_message("  File not included ($1):         $filename");
                }
            } elsif ($filename =~ /^.+(dsc|sc-diameter)\.yaml$/) {
                if (exists $enabled_cnf{'dsc'}) {
                    General::Logging::log_user_message("  File included (dsc):              $filename");
                    $files_to_use{$filename}++;
                } else {
                    General::Logging::log_user_message("  File not included (dsc):          $filename");
                }
            } elsif ($filename =~ /^.+(pvtb)\.yaml$/) {
                if (exists $enabled_cnf{'pvtb'}) {
                    General::Logging::log_user_message("  File included (pvtb):             $filename");
                    $files_to_use{$filename}++;
                } else {
                    General::Logging::log_user_message("  File not included (pvtb):         $filename");
                }
            } else {
                # Other files we always include, e.g. sc-cs.yaml
                    General::Logging::log_user_message("  File always included:             $filename");
                $files_to_use{$filename}++;
            }
        }

        my @file_data;
        General::Logging::log_user_message("Reading 'pm-recording-rules' files");
        # Store default data that will always be used
        push @file_data, "eric-pm-server:";
        push @file_data, "  server:";
        push @file_data, "    extraConfigmapMounts:";
        for my $filename (sort keys %files_to_use) {
            $rc = General::File_Operations::read_file(
                {
                    "filename"              => $filename,
                    "output-ref"            => \@file_data,
                    "ignore-empty-lines"    => 1,
                    "append-output-ref"     => 1,
                    "ignore-pattern"        => '^\s*(eric-pm-server|server|extraConfigmapMounts):\s*$',
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_message("Failed to read 'pm-recording-rules' file: $filename");
                return 1;
            }
        }
        # >>>>> Start Workaround <<<<<
        # TODO: The following are a temporary fix for a fault introduced by the challengers where they forgot to add eric-pm-server when reading data in the template file.
        # Looks like it's designed this way, so this code needs to stay. See https://eteamproject.internal.ericsson.com/browse/DND-65438
        my @workaround_data = ();
        for my $filename (sort keys %files_to_use) {
            $rc = General::File_Operations::read_file(
                {
                    "filename"              => $filename,
                    "output-ref"            => \@workaround_data,
                    "ignore-empty-lines"    => 1,
                    "append-output-ref"     => 1,
                    "ignore-pattern"        => '^\s*(eric-pm-server|server|extraConfigmapMounts):\s*$',
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_message("Failed to read 'pm-recording-rules' file: $filename");
                return 1;
            }
        }
        # Now remove one indentation level
        for (@workaround_data) {
            s/^\s\s//;
        }
        push @file_data, "server:";
        push @file_data, "  extraConfigmapMounts:";
        push @file_data, @workaround_data;
        # >>>>> End Workaround <<<<<

        $rc = General::File_Operations::write_file(
            {
                "filename"              => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/pm-recording-rules-values.yaml",
                "output-ref"            => \@file_data,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_message("Failed to write 'pm-recording-rules-values.yaml' file to workspace directory");
            return 1;
        }

        $::JOB_PARAMS{'RECORDING_RULES_VALUES_FILE'} = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/pm-recording-rules-values.yaml";

        return $rc;
    }
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P934S99 {

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

This playlist prepares the helm chart eric-sc-values.yaml file and copies it to
the workspace directory.
It also copies extra helm chart files to the workspace directory.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
