package Playlist::005_User_Management;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.36
#  Date     : 2024-03-26 15:42:49
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2024
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
use General::File_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;
use Playlist::915_Add_Users;
use Playlist::920_Check_Network_Config_Information;
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

    my $rc;

    # Set job type to be a deployment
    $::JOB_PARAMS{'JOBTYPE'} = "USER_MANAGEMENT";

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

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P005S01, \&Fallback001_P005S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P005S02, \&Fallback001_P005S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Add_Users_P005S03, \&Fallback001_P005S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Collect_Logs_If_Wanted_P005S04, \&Fallback001_P005S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P005S05, \&Fallback001_P005S99 );
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
sub Initialize_Job_Environment_P005S01 {

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
sub Check_Job_Parameters_P005S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P005S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::936_Check_Deployed_Software::main } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'P920_TASK'} = "READ_PARAMETERS_FROM_FILE";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::920_Check_Network_Config_Information::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_User_File_P005S02T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_If_Expect_Exists_P005S02T03 } );
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
    sub Check_Job_Parameters_P005S02T01 {

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
                General::Logging::log_user_error_message("Network Config parameter 'sc_release_name' has not been set and Job parameter 'SC_RELEASE_NAME' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'SC_RELEASE_NAME', nor Network Config parameter 'sc_release_name' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # We need to have access to a directory that has not yet been created, so create it now but ignore the result
        `mkdir -p $::JOB_PARAMS{'_JOB_CONFIG_DIR'}`;

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
    sub Check_User_File_P005S02T02 {

        my @added_users = ();
        my $found_cnt = 0;
        my $rc = 0;
        my @user_data = ();
        my $user_data_file = "";
        my %users_to_add = map { $_ => 1 } split /[\s,;]/, $::JOB_PARAMS{'USERS_TO_ADD'};

        # We need to have access to a directory that has not yet been created, so create it now but ignore the result
        `mkdir -p $::JOB_PARAMS{'_JOB_CONFIG_DIR'}`;

        # Get the user file, if specified
        General::Logging::log_user_message("Checking Job parameter 'USER_DATA_FILE'");
        if ($::JOB_PARAMS{'USE_NETCONF'} eq "yes") {

            # Use CMYP NETCONF interface.

            if ($::JOB_PARAMS{'USER_DATA_FILE'} eq "") {
                # Not specified, Generate a file from parameters in the network configuration file.
                General::Logging::log_user_message("Not specified, Creating the 'add-default-users.netconf' file");

                push @user_data, '<?xml version="1.0" encoding="UTF-8"?>';
                push @user_data, '<hello xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">';
                push @user_data, '    <capabilities>';
                push @user_data, '        <capability>urn:ietf:params:netconf:base:1.0</capability>';
                push @user_data, '        <capability>urn:ietf:params:netconf:capability:writable-running:1.0</capability>';
                push @user_data, '        <capability>urn:ietf:params:netconf:capability:rollback-on-error:1.0</capability>';
                push @user_data, '    </capabilities>';
                push @user_data, '</hello>';
                push @user_data, ']]>]]>';
                push @user_data, '<rpc message-id="1" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">';
                push @user_data, '<edit-config>';
                push @user_data, '    <target>';
                push @user_data, '        <running/>';
                push @user_data, '    </target>';
                push @user_data, '    <config xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0">';
                push @user_data, '        <system xmlns="urn:ietf:params:xml:ns:yang:ietf-system">';
                push @user_data, '            <authentication>';

                if ($::JOB_PARAMS{'PASSWORD_EXPIRE'} eq "no") {
                    # This is a better and fully supported solution instead of the old
                    # LDAP pwd_reset_disable_<user>.ldif hack solution.
                    push @user_data, '               <default-password-policy xmlns="urn:rdns:com:ericsson:oammodel:ericsson-system-ext">';
                    push @user_data, '                   <must-change>false</must-change>';
                    push @user_data, '               </default-password-policy>';
                }

                for my $key1 (sort keys %::NETWORK_CONFIG_PARAMS) {
                    next unless ($key1 =~ /^default_user_\d+$/);
                    if ($::JOB_PARAMS{'USERS_TO_ADD'} ne "") {
                        next unless (exists $users_to_add{$::NETWORK_CONFIG_PARAMS{$key1}{'user'}});
                    }
                    my $changeme_cnt = 0;
                    for my $key2 (keys %{$::NETWORK_CONFIG_PARAMS{$key1}}) {
                        $changeme_cnt++ if ($::NETWORK_CONFIG_PARAMS{$key1}{$key2} eq "CHANGEME");
                    }
                    if ($changeme_cnt == 0) {
                        $found_cnt++;

                        push @user_data, '               <user>';
                        push @user_data, "                   <name>$::NETWORK_CONFIG_PARAMS{$key1}{'user'}</name>";
                        push @user_data, "                   <password>$::NETWORK_CONFIG_PARAMS{$key1}{'initial_password'}</password>";
                        for my $group (split /\s+/, $::NETWORK_CONFIG_PARAMS{$key1}{'groups'}) {
                            push @user_data, "                   <groups xmlns=\"urn:rdns:com:ericsson:oammodel:ericsson-system-ext\">$group</groups>";
                        }
                        push @user_data, '               </user>';

                    } else {
                        General::Logging::log_user_warning_message("Network config parameter $key1 had $changeme_cnt parameters with 'CHANGEME' value which has been ignored and user will not be added");
                    }
                }
                if ($found_cnt == 0) {
                    General::Logging::log_user_error_message("No valid 'default_user_XX' network config parameter found, no users can be added");
                    return General::Playlist_Operations::RC_FALLBACK;
                }

                push @user_data, '            </authentication>';
                push @user_data, '        </system>';
                push @user_data, '    </config>';
                push @user_data, '</edit-config>';
                push @user_data, '</rpc>';
                push @user_data, ']]>]]>';

                $rc = General::File_Operations::write_file(
                    {
                        "filename"          => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/add-default-users.netconf",
                        "output-ref"        => \@user_data,
                        "file-access-mode"  => "666"
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to create 'add-default-users.netconf' file in workspace directory");
                    return General::Playlist_Operations::RC_FALLBACK;
                }
            } else {
                # User file specified, so use it
                General::Logging::log_user_message("Copying user specified file to 'add-default-users.netconf' file");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "cp -fp $::JOB_PARAMS{'USER_DATA_FILE'} $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/add-default-users.netconf",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to copy user file into workspace directory");
                    return General::Playlist_Operations::RC_FALLBACK;
                }
            }

            $::JOB_PARAMS{'USER_DATA_FILE'} = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/add-default-users.netconf";

            General::Logging::log_user_message("Reading users to be added");
            $rc = General::File_Operations::read_file(
                {
                    "filename"            => $::JOB_PARAMS{'USER_DATA_FILE'},
                    "output-ref"          => \@user_data,
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to read file with users to add");
                return General::Playlist_Operations::RC_FALLBACK;
            }
            for (@user_data) {
                if (/<name>([^<]+)<\/name>/sg) {
                    push @added_users, $1;
                    $found_cnt++;
                }
            }

        } else {

            # Use CMYP CLI interface.

            if ($::JOB_PARAMS{'USER_DATA_FILE'} eq "") {
                # Not specified, Generate a file from parameters in the network configuration file.
                General::Logging::log_user_message("Not specified, Creating the 'add-default-users.cli' file");
                push @user_data, "config";

                if ($::JOB_PARAMS{'PASSWORD_EXPIRE'} eq "no") {
                    # This is a better and fully supported solution instead of the old
                    # LDAP pwd_reset_disable_<user>.ldif hack solution.
                    push @user_data, "system authentication default-password-policy must-change false";
                    push @user_data, "commit";
                }

                for my $key1 (sort keys %::NETWORK_CONFIG_PARAMS) {
                    next unless ($key1 =~ /^default_user_\d+$/);
                    if ($::JOB_PARAMS{'USERS_TO_ADD'} ne "") {
                        next unless (exists $users_to_add{$::NETWORK_CONFIG_PARAMS{$key1}{'user'}});
                    }
                    my $changeme_cnt = 0;
                    for my $key2 (keys %{$::NETWORK_CONFIG_PARAMS{$key1}}) {
                        $changeme_cnt++ if ($::NETWORK_CONFIG_PARAMS{$key1}{$key2} eq "CHANGEME");
                    }
                    if ($changeme_cnt == 0) {
                        $found_cnt++;
                        push @user_data, "system authentication user $::NETWORK_CONFIG_PARAMS{$key1}{'user'} password $::NETWORK_CONFIG_PARAMS{$key1}{'initial_password'} groups [ $::NETWORK_CONFIG_PARAMS{$key1}{'groups'} ]";
                    } else {
                        General::Logging::log_user_warning_message("Network config parameter $key1 had $changeme_cnt parameters with 'CHANGEME' value which has been ignored and user will not be added");
                    }
                }
                if ($found_cnt == 0) {
                    General::Logging::log_user_error_message("No valid 'default_user_XX' network config parameter found, no users can be added");
                    return General::Playlist_Operations::RC_FALLBACK;
                }
                push @user_data, "commit";

                $rc = General::File_Operations::write_file(
                    {
                        "filename"          => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/add-default-users.cli",
                        "output-ref"        => \@user_data,
                        "file-access-mode"  => "666"
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to create 'add-default-users.cli' file in workspace directory");
                    return General::Playlist_Operations::RC_FALLBACK;
                }
            } else {
                # User file specified, so use it
                General::Logging::log_user_message("Copying user specified file to 'add-default-users.cli' file");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "cp -fp $::JOB_PARAMS{'USER_DATA_FILE'} $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/add-default-users.cli",
                        "hide-output"   => 1,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to copy user file into workspace directory");
                    return General::Playlist_Operations::RC_FALLBACK;
                }
            }

            $::JOB_PARAMS{'USER_DATA_FILE'} = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/add-default-users.cli";

            General::Logging::log_user_message("Reading users to be added");
            $rc = General::File_Operations::read_file(
                {
                    "filename"          => "$::JOB_PARAMS{'USER_DATA_FILE'}",
                    "output-ref"        => \@user_data,
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to read user specified file");
                return General::Playlist_Operations::RC_FALLBACK;
            }
            $found_cnt = 0;
            for (@user_data) {
                if (/^\s*system authentication\s+user\s+(\S+)\s*/) {
                    push @added_users, $1;
                    $found_cnt++;
                }
            }
            if ($found_cnt == 0) {
                General::Logging::log_user_error_message("Failed to find any 'system authentication user ....' lines in the specified file");
                return General::Playlist_Operations::RC_FALLBACK;
            }

        }

        $rc = General::File_Operations::write_file(
            {
                "filename"          => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/users_to_be_added.txt",
                "output-ref"        => \@added_users,
                "file-access-mode"  => "666"
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to create 'users_to_be_added.txt' file in workspace directory");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        $user_data_file = $::JOB_PARAMS{'USER_DATA_FILE'};

        General::Logging::log_user_message("USER_DATA_FILE=$user_data_file");

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
    sub Check_If_Expect_Exists_P005S02T03 {

        my $rc = 0;

        General::Logging::log_user_message("Check that 'expect' exist in the PATH");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "perl $::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/server_check.pl --check-command='expect'",
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to find the 'expect' binary in the PATH and this is needed for this Playlist to work");
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
sub Add_Users_P005S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::915_Add_Users::main } );
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
sub Collect_Logs_If_Wanted_P005S04 {

    my $rc = 0;

    if ($::JOB_PARAMS{'COLLECT_LOGS_AT_SUCCESS'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
        return $rc if $rc < 0;
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
sub Cleanup_Job_Environment_P005S05 {

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
sub Fallback001_P005S99 {

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
Users will be added.
Any command that will just change files or data on the job workspace directory
or any command that just reads data from the cluster will still be executed.
This parameter can be useful to check if the overall logic is working without
doing any changes to the running system.
EOF
            'validity_mask' => '(yes|no)',
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
        'USE_NETCONF' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the CMYP NETCONF or CLI interface should be used for
checking and loading user data into the node.

If the value is 'yes' then the NETCONF interface on port 830 of CMYP will be
used for loading data, and if USER_DATA_FILE is specified it must contain NETCONF
data.

If the value is 'no' then the CLI interface on port 22 of CMYP will be used for
loading data, and if USER_DATA_FILE is specified it must contain CLI data.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'USER_DATA_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The path to a file containing user data to load.

If 'USE_NETCONF=yes' is specified then the data in the file MUST BE in NETCONF
format for loading users.
This file should contain all data for successfully loading the user data, which
should include any <hello and <edit-config> tags.
If you don't specify this file then a default users template file will be used.

If 'USE_NETCONF=no' is specified then the data in the file MUST BE in CLI format
for loading users.
This file should contain all the needed commands for loading the user data, which
includes 'config', 'system authentication user ...' and 'commit' commands.
If you don't specify this file then a default users template file will be created
based on data from the 'default_users' section in the network configuration file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'USERS_TO_ADD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If you only want specific users from the network configuration file to be added
then specify that list here. It must be users that exists in the network config
file otherwise that user will be ignored.

If multiple users should be added then either separate each with a space, comma
or a semicolon.
By default if this parameter is not given then all users from the network
configuration file will be added.
This parameter is ignored if parameter USER_DATA_FILE is specified.
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

This Playlist script is used for adding new users to an ESC node and it will be
called from the execute_playlist.pl script.

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
