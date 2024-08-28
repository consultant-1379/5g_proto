package Playlist::920_Check_Network_Config_Information;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.19
#  Date     : 2024-06-06 16:59:10
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
    is_network_config_specified
    parse_network_config_parameters
    usage
    validate_network_config_parameter
    );

# Used Perl package files
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

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

    $::JOB_PARAMS{'P920_TASK'} = "CHECK_EXISTING_PARAMETERS" unless (exists $::JOB_PARAMS{'P920_TASK'});

    if ($::JOB_PARAMS{'P920_TASK'} eq "CHECK_EXISTING_PARAMETERS") {

        $rc = General::Playlist_Operations::execute_step( \&Check_Valid_Network_Config_Parameters_P920S01, undef );
        return $rc if $rc < 0;

    } elsif ($::JOB_PARAMS{'P920_TASK'} eq "READ_PARAMETERS_FROM_FILE") {

        $rc = General::Playlist_Operations::execute_step( \&Read_And_Validate_Network_Config_Parameters_P920S02, undef );
        return $rc if $rc < 0;

    } else {

    }

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
sub Check_Valid_Network_Config_Parameters_P920S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Valid_Releases_Tags_P920S01T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Password_Expire_Tags_P920S01T02 } );
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
    sub Check_Valid_Releases_Tags_P920S01T01 {

        my @job_params_to_delete = ();
        my @network_config_params_to_delete = ();
        my $number_of_deleted_parameters = 0;
        my $rc = 0;
        my $removed_variables = "The following variables has been deleted because 'valid_releases' in network config file does not match with the SC_RELEASE_VERSION value:\n";

        unless (exists $::JOB_PARAMS{'SC_RELEASE_VERSION'}) {
            if (exists $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}) {
                $::JOB_PARAMS{'SC_RELEASE_VERSION'} = $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'};
                # Just for completeness sake even though the BUILD is not needed in this playlist
                if (exists $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'}) {
                    $::JOB_PARAMS{'SC_RELEASE_BUILD'} = $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'};
                }
            } else {
                # It neither parameter has been set, no need to check the 'valid_releases' parameter
                General::Logging::log_user_message("No need to check the 'valid_releases' attribute");
                return General::Playlist_Operations::RC_TASKOUT;
            }
        }

        # Check which network configuration parameters are valid for the used
        # SC_RELEASE_VERSION and delete the ones that should not be used.
        for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
            if (exists $::NETWORK_CONFIG_PARAMS{$key}{'valid_releases'} && $::JOB_PARAMS{'SC_RELEASE_VERSION'} !~ /$::NETWORK_CONFIG_PARAMS{$key}{'valid_releases'}/) {
                # The network config parameter does not match the SC_RELEASE_VERSION so it should be deleted
                # from the %::NETWORK_CONFIG_PARAMS hash and also any %::JOB_PARAMS variables based on this
                # network config parameter should also be deleted.
                push @network_config_params_to_delete, $key;
                push @job_params_to_delete, $key;
                for my $subkey (sort keys %{$::NETWORK_CONFIG_PARAMS{$key}}) {
                    next if ($subkey eq "value");   # Already marked to be deleted above
                    push @job_params_to_delete, "${key}_$subkey";
                }
            }
        }
        # Now delete any found parameters
        for my $key (@network_config_params_to_delete) {
            delete $::NETWORK_CONFIG_PARAMS{$key};
            $removed_variables .= " - \$::NETWORK_CONFIG_PARAMS{'$key'}\n";
        }
        for my $key (@job_params_to_delete) {
            delete $::JOB_PARAMS{$key};
            $number_of_deleted_parameters++;
            $removed_variables .= " - \$::JOB_PARAMS{'$key'}\n";
        }
        if (scalar @job_params_to_delete == 0) {
            General::Logging::log_user_message("All parameters from the network configuration file are valid");
        } else {
            General::Logging::log_user_message("Some parameters from the network configuration file are not valid for this $::JOB_PARAMS{'SC_RELEASE_VERSION'} release.\n$number_of_deleted_parameters job parameters have been removed.\n");
            # Log details about removed variables
            General::Logging::log_write($removed_variables);
            # If we have deleted any job parameters then we need to write %::JOB_PARAMS hash to file
            General::Playlist_Operations::job_parameters_write($::JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%::JOB_PARAMS);
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
    sub Check_Password_Expire_Tags_P920S01T02 {

        my @job_params_to_delete = ();
        my @network_config_params_to_delete = ();
        my $number_of_deleted_parameters = 0;
        my $rc = 0;
        my $removed_variables = "The following variables has been deleted because 'password_expire' in network config file does not match with the PASSWORD_EXPIRE value:\n";

        unless (exists $::JOB_PARAMS{'PASSWORD_EXPIRE'}) {
            # It has not been set, assume default value
            $::JOB_PARAMS{'PASSWORD_EXPIRE'} = "no";
        }

        # Check which network configuration parameters are valid for the used
        # SC_RELEASE_VERSION and delete the ones that should not be used.
        for my $key (sort keys %::NETWORK_CONFIG_PARAMS) {
            if (exists $::NETWORK_CONFIG_PARAMS{$key}{'password_expire'} && $::JOB_PARAMS{'PASSWORD_EXPIRE'} ne $::NETWORK_CONFIG_PARAMS{$key}{'password_expire'}) {
                # The network config parameter does not match the PASSWORD_EXPIRE so it should be deleted
                # from the %::NETWORK_CONFIG_PARAMS hash and also any %::JOB_PARAMS variables based on this
                # network config parameter should also be deleted.
                push @network_config_params_to_delete, $key;
                push @job_params_to_delete, $key;
                for my $subkey (sort keys %{$::NETWORK_CONFIG_PARAMS{$key}}) {
                    next if ($subkey eq "value");   # Already marked to be deleted above
                    push @job_params_to_delete, "${key}_$subkey";
                }
            }
        }
        # Now delete any found parameters
        for my $key (@network_config_params_to_delete) {
            delete $::NETWORK_CONFIG_PARAMS{$key};
            $removed_variables .= " - \$::NETWORK_CONFIG_PARAMS{'$key'}\n";
        }
        for my $key (@job_params_to_delete) {
            delete $::JOB_PARAMS{$key};
            $number_of_deleted_parameters++;
            $removed_variables .= " - \$::JOB_PARAMS{'$key'}\n";
        }
        if (scalar @job_params_to_delete == 0) {
            General::Logging::log_user_message("All parameters from the network configuration file are valid");
        } else {
            General::Logging::log_user_message("Some parameters from the network configuration file are not valid for this PASSWORD_EXPIRE=$::JOB_PARAMS{'PASSWORD_EXPIRE'} value.\n$number_of_deleted_parameters job parameters have been removed.\n");
            # Log details about removed variables
            General::Logging::log_write($removed_variables);
            # If we have deleted any job parameters then we need to write %::JOB_PARAMS hash to file
            General::Playlist_Operations::job_parameters_write($::JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%::JOB_PARAMS);
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
sub Read_And_Validate_Network_Config_Parameters_P920S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Read_Network_Config_Parameters_From_File_P920S02T01 } );
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
    sub Read_Network_Config_Parameters_From_File_P920S02T01 {
        return parse_network_config_parameters();
    }
}

#####################
#                   #
# Other Subroutines #
#                   #
#####################

# -----------------------------------------------------------------------------
# Checks if the user have specified a network configuration file.
#
# Return values:
#    0: If successful and file was specified and it exists.
#    1: If failure and no file was specified or does not exists.
#
sub is_network_config_specified {
    if ($::network_file ne "" and -f "$::network_file") {
        return 0;
    } else {
        return 1;
    }
}

# -----------------------------------------------------------------------------
# This subroutine reads the network configuration file and removes not wanted
# parameters from being stored in the %::NETWORK_CONFIG_PARAMS and %::JOB_PARAMS
# hashes.
# This subroutine is called by certain playlists that want to load the data
# from the network configuration file only when all information is available
# that will be used to filter away unwanted parameters.
#
sub parse_network_config_parameters {
    my @discarded = ();
    my $message = "";
    my $rc;

    General::Logging::log_user_message("Reading Network Configuration File");

    # Initialize default values
    if (exists $::JOB_PARAMS{'SC_RELEASE_VERSION'}) {
        $message .= sprintf "  Variable 'SC_RELEASE_VERSION' with value '%s' compared against attribute 'valid_releases'\n", $::JOB_PARAMS{'SC_RELEASE_VERSION'};
    } else {
        if (exists $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}) {
            $::JOB_PARAMS{'SC_RELEASE_VERSION'} = $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'};
            $message .= sprintf "  Variable 'SC_RELEASE_VERSION' with value '%s' compared against attribute 'valid_releases'\n", $::JOB_PARAMS{'SC_RELEASE_VERSION'};
            # Just for completeness sake even though the BUILD is not needed in this playlist
            if (exists $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'}) {
                $::JOB_PARAMS{'SC_RELEASE_BUILD'} = $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'};
            }
        } else {
            # If neither parameter has been set, no need to check the 'valid_releases' parameter
            General::Logging::log_user_message("Neither 'SC_RELEASE_VERSION' nor 'OLD_SC_RELEASE_VERSION' specified, no need to check the 'valid_releases' attribute");
        }
    }
    if (exists $::JOB_PARAMS{'PASSWORD_EXPIRE'}) {
        $message .= sprintf "  Variable 'PASSWORD_EXPIRE' with value '%s' compared against attribute 'password_expire'\n", $::JOB_PARAMS{'PASSWORD_EXPIRE'};
    } else {
        # It has not been set, assume default value
        General::Logging::log_user_message("'PASSWORD_EXPIRE' not specified, no need to check the 'password_expire' attribute");
    }
    if (exists $::JOB_PARAMS{'APPLICATION_TYPE'}) {
        $message .= sprintf "  Variable 'APPLICATION_TYPE' with value '%s' compared against attribute 'application_type'\n", $::JOB_PARAMS{'APPLICATION_TYPE'};
    } else {
        # It has not been set, assume default value
        General::Logging::log_user_message("'APPLICATION_TYPE' not specified, no need to check the 'application_type' attribute");
    }
    if (exists $::JOB_PARAMS{'DEPLOYED_CNF'}) {
        $message .= sprintf "  Variable 'DEPLOYED_CNF' with value '%s' compared against attribute 'cnf_type'\n", $::JOB_PARAMS{'DEPLOYED_CNF'};
    } else {
        if (exists $::JOB_PARAMS{'ENABLED_CNF'}) {
            $::JOB_PARAMS{'DEPLOYED_CNF'} = $::JOB_PARAMS{'ENABLED_CNF'};
            $message .= sprintf "  Variable 'DEPLOYED_CNF' with value '%s' compared against attribute 'cnf_type'\n", $::JOB_PARAMS{'DEPLOYED_CNF'};
        } else {
            # If neither parameter has been set, no need to check the 'valid_releases' parameter
            General::Logging::log_user_message("Neither 'DEPLOYED_CNF' nor 'ENABLED_CNF' specified, no need to check the 'cnf_type' attribute");
        }
    }

    # After we now know the software version of the software package and the deployed software we can
    # now read all network configuration parameters and filter out not wanted parameters.
    $rc = General::File_Operations::parse_network_configuration_file(
        {
            "filename"              => $::network_file,
            "output-ref"            => \%::NETWORK_CONFIG_PARAMS,
            "validator-ref"         => \&validate_network_config_parameter,
            "discarded-ref"         => \@discarded,
            "hide-error-messages"   => 1,
        }
    );
    if ($rc != 0) {
        General::Logging::log_user_error_message("Unable to read network configuration file: $::network_file");
        return 1;
    }

    # Now replace any 'REPLACEMEWITH_xxxx' values with the value from the 'xxxx' parameter.
    General::File_Operations::replace_network_configuration_variable_placeholders(
        {
            "input-ref"             => \%::NETWORK_CONFIG_PARAMS,
            "discarded-ref"         => \@discarded,
        }
    );

    # Now update the %::JOB_PARAMS hash with values from %::NETWORK_CONFIG_PARAMS hash.
    General::File_Operations::update_job_variables_with_network_configuration_variables(
        {
            "input-ref"             => \%::NETWORK_CONFIG_PARAMS,
            "output-ref"            => \%::JOB_PARAMS,
            "ignore-attributes"     => ["application_type", "cnf_type", "description", "playlist", "reuse_deployed_helm_value", "valid_from_releases", "valid_releases"],
            "discarded-ref"         => \@discarded,
        }
    );

    # Since we have updated the job variables then we need to write %::JOB_PARAMS hash to file
    General::Playlist_Operations::job_parameters_write($::JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%::JOB_PARAMS);

    if (@discarded) {
        if ($message ne "") {
            $message = "The following filters are used for removing network config file parameters:\n$message\n";
        }
        $message .= "The following entries in the network config file was discarded:\n";
        $message .= join "\n", @discarded;
        $message .= "\n";
        General::Logging::log_write($message);
        General::Logging::log_user_message("Some parameters from the network configuration file are not valid and has been ignored");
    } else {
        General::Logging::log_user_message("All parameters from the network configuration file are valid");
    }

    return 0;
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This playlist check network configuration parameters for for specific attributes
and removes parameters from the %::NETWORK_CONFIG_PARAMS and %::JOB_PARAMS hashes
that should not be used for the current job.

The following attributes from the network config files are checked:

 - application_type
   Is compared against the \$::JOB_PARAMS{'APPLICATION_TYPE'} value.

 - cnf_type
   Is compared against the \$::JOB_PARAMS{'DEPLOYED_CNF'} or the
   \$::JOB_PARAMS{'ENABLED_CNF'} value.

 - password_expire
   Is compared against the \$::JOB_PARAMS{'PASSWORD_EXPIRE'} value.

 - valid_releases
   Is compared against the \$::JOB_PARAMS{'SC_RELEASE_VERSION'} or the
   \$::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} value.

If a playlist or it's called sub-playlists are not using any network config
parameters or they have no dependency on the above mentioned \$::JOB_PARAMS
then there is no need to call this sub-playlist.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

# -----------------------------------------------------------------------------
# This subroutine validates each network configuration file parameters to see
# if it is valid in regards to the currently deployed SC release and for some
# other tags in the network config file.
#
# If the parameter is valid then it returns back a true value (>0) to the
# caller or if not valid then it returns back a false (=0) value.
#
# It is called with one parameter which is a hash reference which should
# contain one main key and one or more subkeys e.g.:
#   $hash_ref->{"eric_sc_values_hash_parameter_eric-cm-mediator.backend.dbuser"}{'value'} => "somevalue"
#   $hash_ref->{"eric_sc_values_hash_parameter_eric-cm-mediator.backend.dbuser"}{'valid_releases'} => "^(1)\.(3|4)\.(\d+)$"
#
# It will check the 'valid_releases' subkey against the SC_RELEASE_VERSION
# value.
# It will also check the 'password_expire' subkey against the PASSWORD_EXPIRE
# value.
#
sub validate_network_config_parameter {
    my $hash_ref = shift;
    my $invalid = 0;

    # Check valid_releases against job variable SC_RELEASE_VERSION
    if (exists $::JOB_PARAMS{'SC_RELEASE_VERSION'} && $::JOB_PARAMS{'SC_RELEASE_VERSION'} ne "") {
        for my $key (keys %{$hash_ref}) {
            if (exists $hash_ref->{$key}{'valid_releases'} && $::JOB_PARAMS{'SC_RELEASE_VERSION'} !~ /$hash_ref->{$key}{'valid_releases'}/) {
                # This parameter is not valid for the current release, so return back false to ignore the parameter
                $invalid++;
                last;
            }
        }
    }

    # Check password_expire against job variable PASSWORD_EXPIRE
    if (exists $::JOB_PARAMS{'PASSWORD_EXPIRE'}) {
        for my $key (keys %{$hash_ref}) {
            if (exists $hash_ref->{$key}{'password_expire'} && $::JOB_PARAMS{'PASSWORD_EXPIRE'} ne $hash_ref->{$key}{'password_expire'}) {
                # This parameter is not valid for the current setting of PASSWORD_EXPIRE, so return back false to ignore the parameter
                $invalid++;
                last;
            }
        }
    }

    # Check application_type against job variable APPLICATION_TYPE
    if (exists $::JOB_PARAMS{'APPLICATION_TYPE'} && $::JOB_PARAMS{'APPLICATION_TYPE'} ne "") {
        for my $key (keys %{$hash_ref}) {
            if (exists $hash_ref->{$key}{'application_type'} && $::JOB_PARAMS{'APPLICATION_TYPE'} !~ /$hash_ref->{$key}{'application_type'}/) {
                # This parameter is not valid for the current setting of APPLICATION_TYPE, so return back false to ignore the parameter
                $invalid++;
                last;
            }
        }
    }

    # Check cnf_type against job variable DEPLOYED_CNF
    if (exists $::JOB_PARAMS{'DEPLOYED_CNF'} && $::JOB_PARAMS{'DEPLOYED_CNF'} ne "") {
        for my $key (keys %{$hash_ref}) {
            if (exists $hash_ref->{$key}{'cnf_type'} && $::JOB_PARAMS{'DEPLOYED_CNF'} !~ /$hash_ref->{$key}{'cnf_type'}/) {
                if ($::JOB_PARAMS{'DEPLOYED_CNF'} ne "all") {
                    # This parameter is not valid for the current setting of DEPLOYED_CNF, so return back false to ignore the parameter
                    $invalid++;
                    last;
                }
            }
        }
    }

    if ($invalid > 0) {
        # This parameter is not valid for the current setting of DEPLOYED_CNF, so return back false to ignore the parameter
        for my $key (keys %{$hash_ref}) {
            # Also remove the parameter from the $::JOB_PARAMS hash
            for my $attribute (sort keys %{$hash_ref->{$key}}) {
                if (exists $::JOB_PARAMS{"${key}_$attribute"}) {
                    # Delete the value_XXX variable from the $::JOB_PARAMS
                    delete $::JOB_PARAMS{"${key}_$attribute"};
                }
            }
        }
        return 0;
    }

    # Check if there are special handling need for the value attributes.
    if (exists $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} && $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} ne "") {
        my %options;
        # First create a hash of available options for easy access
        for my $option (split /\|/, $::JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'}) {
            $options{"value_$option"} = 1;
        }

        my @used_values = ();
        my %values_from_option;
        for my $key (keys %{$hash_ref}) {
            my @attributes_to_delete = ();
            for my $attribute (sort keys %{$hash_ref->{$key}}) {
                next unless ($attribute =~ /^value_\S+/);
                push @attributes_to_delete, $attribute;
                if (exists $options{$attribute}) {
                    # Set the "value" attribute to this value
                    $hash_ref->{$key}{'value'} = $hash_ref->{$key}{$attribute};
                    $values_from_option{$hash_ref->{$key}{$attribute}}++;
                    push @used_values, "Name: $key, $attribute=\"$hash_ref->{$key}{'value'}\"";
                }
            }
            # Now delete all the "value_xxx" attributes so we only leave the "value" if it exist.
            for my $attribute (@attributes_to_delete) {
                delete $hash_ref->{$key}{$attribute};
                if (exists $::JOB_PARAMS{"${key}_$attribute"}) {
                    # Delete the value_XXX variable from the $::JOB_PARAMS
                    delete $::JOB_PARAMS{"${key}_$attribute"};
                }
            }
        }
        if (scalar @used_values > 1) {
            # Multiple values matched
            if (scalar keys %values_from_option == 1) {
                # Maybe not good design, but not causing any issues since all value_xxx values were the same.
                General::Logging::log_user_message("Multiple value options matched but all with the same value:\n" . join "\n", @used_values);
            } else {
                # Different values matched, so report it that only the last found value was used.
                General::Logging::log_user_warning_message("Multiple value options matched, which might indicate a problem with the network configuration file, only the last one used for the 'value' attribute:\n" . join "\n", @used_values);
            }
        }
    } else {
        for my $key (keys %{$hash_ref}) {
            my @attributes_to_delete = ();
            for my $attribute (sort keys %{$hash_ref->{$key}}) {
                next unless ($attribute =~ /^value_\S+/);
                push @attributes_to_delete, $attribute;
            }
            # Now delete all the "value_xxx" attributes so we only leave the "value" if it exist.
            for my $attribute (@attributes_to_delete) {
                delete $hash_ref->{$key}{$attribute};
                if (exists $::JOB_PARAMS{"${key}_$attribute"}) {
                    # Delete the value_XXX variable from the $::JOB_PARAMS
                    delete $::JOB_PARAMS{"${key}_$attribute"};
                }
            }
        }
    }

    # The parameter seems to be valid or it cannot be checked.
    return 1;
}

1;
