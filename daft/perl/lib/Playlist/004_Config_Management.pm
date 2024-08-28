package Playlist::004_Config_Management;

################################################################################
#
#  Author   : eustone, everhel
#
#  Revision : 1.54
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

#
# Used Perl package files
#

use File::Basename qw(dirname basename);

use ADP::Kubernetes_Operations;
use General::File_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;
use General::String_Operations;

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::913_Add_Configuration;
use Playlist::914_Collect_Logs;
use Playlist::920_Check_Network_Config_Information;
use Playlist::936_Check_Deployed_Software;
use Playlist::938_Node_Information;

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
    $::JOB_PARAMS{'JOBTYPE'} = "CONFIG_MANAGEMENT";

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

    # Always execute the following tasks
    General::State_Machine::always_execute_task("Playlist::913_Add_Configuration::.+P913S02.+");

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P004S01, \&Fallback001_P004S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P004S02, \&Fallback001_P004S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Add_Configuration_P004S03, \&Fallback001_P004S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Collect_Logs_If_Wanted_P004S04, \&Fallback001_P004S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P004S05, \&Fallback001_P004S99 );
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
sub Initialize_Job_Environment_P004S01 {

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
sub Check_Job_Parameters_P004S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_Part1_P004S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::936_Check_Deployed_Software::main } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'P920_TASK'} = "READ_PARAMETERS_FROM_FILE";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::920_Check_Network_Config_Information::main } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_Part2_P004S02T02 } );
    return $rc if $rc < 0;

    $::JOB_PARAMS{'P004_FETCH_NODE_INFORMATION'} = "yes" unless (exists $::JOB_PARAMS{'P004_FETCH_NODE_INFORMATION'});
    if ($::JOB_PARAMS{'P004_FETCH_NODE_INFORMATION'} eq "yes") {
        $::JOB_PARAMS{'P938_GET_SC_NODE_INFORMATION'} = "yes";
        $::JOB_PARAMS{'P938_GET_TOOLS_NODE_INFORMATION'} = "yes";
        $::JOB_PARAMS{'P938_REPLACE_FILE_PLACEHOLDERS'} = "no";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::938_Node_Information::main } );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Config_Files_P004S02T03 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_If_Expect_Exists_P004S02T04 } );
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
    sub Check_Job_Parameters_Part1_P004S02T01 {

        my $sc_namespace = "";
        my $sc_release_name = "";
        my $rc = 0;
        my $tools_namespace = "";

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

        # Get the proper TOOLS_NAMESPACE
        General::Logging::log_user_message("Checking Job parameter 'TOOLS_NAMESPACE' and Network Config parameter 'tools_namespace'");
        if ($::JOB_PARAMS{'TOOLS_NAMESPACE'} ne "") {
            $tools_namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'tools_namespace'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'tools_namespace'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'TOOLS_NAMESPACE'} = $::NETWORK_CONFIG_PARAMS{'tools_namespace'}{'value'};
                $tools_namespace = $::JOB_PARAMS{'TOOLS_NAMESPACE'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'tools_namespace' has not been set and Job parameter 'TOOLS_NAMESPACE' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'TOOLS_NAMESPACE', nor Network Config parameter 'tools_namespace' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # We need to have access to a directory that has not yet been created, so create it now but ignore the result
        `mkdir -p $::JOB_PARAMS{'_JOB_CONFIG_DIR'}`;

        General::Logging::log_user_message("SC_NAMESPACE=$sc_namespace\nSC_RELEASE_NAME=$sc_release_name\nTOOLS_NAMESPACE=$tools_namespace\n");

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
    sub Check_Job_Parameters_Part2_P004S02T02 {

        my $info_provided_password = 0;
        my $info_provided_username = 0;
        my $file_cnt = 1;
        my $file_netconf_interface;
        my $file_password;
        my $file_user;
        my $files_to_be_loaded = "";
        my $rc = 0;
        my %user_password;

        # Read user information from the network configuration file
        for my $key (keys %::NETWORK_CONFIG_PARAMS) {
            $user_password{$::NETWORK_CONFIG_PARAMS{$key}{'user'}} = $::NETWORK_CONFIG_PARAMS{$key}{'password'};
        }

        # Check user name in parameter CONFIG_USER_NAME
        if ($::JOB_PARAMS{'CONFIG_USER_NAME'} eq "") {
            General::Logging::log_user_message("Job parameter 'CONFIG_USER_NAME' not specified");
        }

        # Check user name in parameter CONFIG_USER_PASSWORD
        if ($::JOB_PARAMS{'CONFIG_USER_PASSWORD'} eq "") {
            if ($::JOB_PARAMS{'CONFIG_USER_NAME'} eq "") {
                General::Logging::log_user_message("Job parameters 'CONFIG_USER_PASSWORD' and 'CONFIG_USER_NAME' not specified, the data must come from file naming convention");
            } elsif (exists $user_password{$::JOB_PARAMS{'CONFIG_USER_NAME'}}) {
                if ($user_password{$::JOB_PARAMS{'CONFIG_USER_NAME'}} ne "CHANGEME") {
                    $::JOB_PARAMS{'CONFIG_USER_PASSWORD'} = $user_password{$::JOB_PARAMS{'CONFIG_USER_NAME'}};
                    General::Logging::log_user_message("Password for user $::JOB_PARAMS{'CONFIG_USER_NAME'} read from network configuration file");
                } else {
                    General::Logging::log_user_erro_message("Job parameter 'CONFIG_USER_PASSWORD' not specified, and password for user $::JOB_PARAMS{'CONFIG_USER_NAME'} read from the network config file is CHANGEME");
                }
            } else {
                General::Logging::log_user_error_message("Job parameter 'CONFIG_USER_PASSWORD' not specified or there is no matching user in the network config file");
            }
        }

        # Check configuration data files to be loaded to see if naming convention
        # specifies the user name, user password and if netconf or cli should be
        # used for loading the file.
        for my $job_parameter (General::String_Operations::sort_hash_keys_alphanumerically(\%::JOB_PARAMS)) {
            next unless ($job_parameter =~ /^(CONFIG_DATA_FILE|CONFIG_DATA_FILE_\d+)$/);
            General::Logging::log_user_message("Checking Job parameter '$job_parameter'");
            # Check that file exist
            unless (-f "$::JOB_PARAMS{$job_parameter}") {
                General::Logging::log_user_error_message("Job parameter '$job_parameter' point to a file that does not exist or cannot be read:\n$::JOB_PARAMS{$job_parameter}\n");
                return General::Playlist_Operations::RC_FALLBACK;
            }

            # Check which interface to use, netconf or cli
            if ($::JOB_PARAMS{$job_parameter} =~ /^.+\.netconf$/) {
                $file_netconf_interface = "yes";
            } elsif ($::JOB_PARAMS{$job_parameter} =~ /^.+\.cli$/) {
                $file_netconf_interface = "no";
            } else {
                $file_netconf_interface = $::JOB_PARAMS{'USE_NETCONF'};
            }

            # Check if user is specified in file name
            if ($::JOB_PARAMS{$job_parameter} =~ /^.+,user=([^,\.]+)/) {
                $file_user = $1;
            } elsif ($::JOB_PARAMS{'CONFIG_USER_NAME'} ne "") {
                $file_user = $::JOB_PARAMS{'CONFIG_USER_NAME'};
            } else {
                General::Logging::log_user_error_message("Job parameter 'CONFIG_USER_NAME' not specified and job parameter '$job_parameter' does not contain the ',user=xxx' naming convention:\n$::JOB_PARAMS{$job_parameter}\n");
                return General::Playlist_Operations::RC_FALLBACK;
            }

            # Check if password is specified in file name
            if ($::JOB_PARAMS{$job_parameter} =~ /^.+,password=([^,\.]+)/) {
                $file_password = $1;
            } elsif (exists $user_password{$file_user}) {
                $file_password = $user_password{$file_user};
            } elsif ($::JOB_PARAMS{'CONFIG_USER_PASSWORD'} ne "") {
                $file_password = $::JOB_PARAMS{'CONFIG_USER_PASSWORD'};
            } else {
                General::Logging::log_user_error_message("Job parameter 'CONFIG_USER_PASSWORD' not specified, or password for user '$file_user' not found in network config file.\nAnd job parameter '$job_parameter' does not contain the ',password=xxx' naming convention:\n$::JOB_PARAMS{$job_parameter}\n");
                return General::Playlist_Operations::RC_FALLBACK;
            }

            $::JOB_PARAMS{"CONFIG_DATA_FILE_TO_LOAD_$file_cnt"} = "user=$file_user,password=$file_password,netconf=$file_netconf_interface,filename=$::JOB_PARAMS{$job_parameter}";
            $::JOB_PARAMS{"CONFIG_DATA_FILE_TO_LOAD_${file_cnt}_LOADED"} = "no";
            $files_to_be_loaded .= "  $::JOB_PARAMS{$job_parameter}\n";
            $file_cnt++;
        }

        General::Logging::log_user_message("The following files will be loaded:\n$files_to_be_loaded");

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
    sub Check_Config_Files_P004S02T03 {

        my @config_data = ();
        my $data_changed = 0;
        my $data_file_info;
        my $filename_in;
        my $filename_nopath;
        my $filename_out;
        my $file_cnt;
        my $found_close_cnt = 0;
        my $found_config_cnt = 0;
        my $found_hello_cnt = 0;
        my $highest_message_id = 0;
        my $rc = 0;
        my $use_netconf;
        my $valid_format = 0;
        my $written_files = "";

        for my $job_parameter (General::String_Operations::sort_hash_keys_alphanumerically(\%::JOB_PARAMS)) {
            if ($job_parameter =~ /^CONFIG_DATA_FILE_TO_LOAD_(\d+)$/) {
                $file_cnt = $1;

                General::Logging::log_user_message("Checking Job Parameter '$job_parameter'");

                if ($::JOB_PARAMS{$job_parameter} =~ /^(user=.+,password=.+,netconf=(yes|no),filename)=(.+)$/) {
                    $data_file_info = $1;
                    $use_netconf = $2;
                    $filename_in = $3;
                    $filename_nopath = basename($filename_in);

                    # Save original file name
                    $::JOB_PARAMS{"CONFIG_DATA_FILE_TO_LOAD_${file_cnt}_ORIG_FILE"} = $filename_in;

                    if ($use_netconf eq "yes") {

                        # Use CMYP NETCONF interface.

                        $filename_out = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/$filename_nopath";

                        # Update information with new file name
                        $::JOB_PARAMS{$job_parameter} = "$data_file_info=$filename_out";

                        # Replace all place holders in the input file and write it to the output file
                        $::JOB_PARAMS{'P938_GET_SC_NODE_INFORMATION'} = "no";
                        $::JOB_PARAMS{'P938_GET_TOOLS_NODE_INFORMATION'} = "no";
                        $::JOB_PARAMS{'P938_REPLACE_FILE_PLACEHOLDERS'} = "yes";
                        $::JOB_PARAMS{'P938_INPUT_FILE_NAME'} = $filename_in;
                        $::JOB_PARAMS{'P938_OUTPUT_FILE_NAME'} = $filename_out;
                        $::JOB_PARAMS{'P938_OUTPUT_FILE_ACCESSMODE'} = "666";
                        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::938_Node_Information::main } );
                        return $rc if $rc < 0;

                        # Check that specified config file contains both "<hello" and "<close-session/>" tags
                        General::Logging::log_user_message("  Reading file: $filename_out");
                        $rc = General::File_Operations::read_file(
                            {
                                "filename"            => $filename_out,
                                "output-ref"          => \@config_data,
                            }
                        );
                        if ($rc != 0) {
                            General::Logging::log_user_error_message("  Failed to read file with configuration data");
                            return General::Playlist_Operations::RC_FALLBACK;
                        }
                        $data_changed = 0;
                        $found_close_cnt = 0;
                        $found_hello_cnt = 0;
                        $highest_message_id = 0;
                        $valid_format = 0;
                        for (@config_data) {
                            if (/<hello\s+/sg) {
                                $found_hello_cnt++;
                            } elsif (/<close-session\/>/sg) {
                                $found_close_cnt++;
                            } elsif (/\]\]>\]\]>/sg) {
                                $valid_format = 1;
                            }

                            # Count highest message-id
                            if (/\s+message-id="(\d+)"/sg) {
                                $highest_message_id = $1 if ($1 > $highest_message_id);
                            }
                        }

                        if ($valid_format == 0) {
                            General::Logging::log_user_error_message("  The file does not appear to be a valid netconf file, the string ']]>]]>' not found");
                            return General::Playlist_Operations::RC_FALLBACK;
                        }

                        if ($found_hello_cnt == 0) {
                            # Load the following at the beginning of the array
                            unshift @config_data, (
                                '<?xml version="1.0" encoding="UTF-8"?>',
                                '<hello xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">',
                                '    <capabilities>',
                                '        <capability>urn:ietf:params:netconf:base:1.0</capability>',
                                '        <capability>urn:ietf:params:netconf:capability:writable-running:1.0</capability>',
                                '        <capability>urn:ietf:params:netconf:capability:rollback-on-error:1.0</capability>',
                                '    </capabilities>',
                                '</hello>',
                                ']]>]]>',
                            );
                            General::Logging::log_user_warning_message("  Adding missing '<hello ' tag to configuration data");
                            $data_changed++;
                        }

                        if ($found_close_cnt == 0) {
                            # Load the following at the end of the array
                            push @config_data, sprintf '<rpc message-id="%d" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">', $highest_message_id++;
                            push @config_data, '    <close-session/>';
                            push @config_data, '</rpc>';
                            push @config_data, ']]>]]>';
                            General::Logging::log_user_warning_message("  Adding missing '<close-session/>' tag to configuration data");
                            $data_changed++;
                        }

                        if ($data_changed > 0) {
                            General::Logging::log_user_message("  Writing file: $filename_out");
                            $rc = General::File_Operations::write_file(
                                {
                                    "filename"          => $filename_out,
                                    "output-ref"        => \@config_data,
                                    "file-access-mode"  => "666"
                                }
                            );
                            if ($rc != 0) {
                                General::Logging::log_user_error_message("  Failed to write file with configuration data");
                                return General::Playlist_Operations::RC_FALLBACK;
                            }
                        } else {
                            General::Logging::log_user_message("  No need to update the file any further");
                        }
                        $written_files .= "  $filename_out\n";
                    } else {

                        # Use CMYP CLI interface.

                        $filename_out = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/$filename_nopath";

                        # Update information with new file name
                        $::JOB_PARAMS{$job_parameter} = "$data_file_info=$filename_out";

                        # Replace all place holders in the input file and write it to the output file
                        $::JOB_PARAMS{'P938_GET_SC_NODE_INFORMATION'} = "no";
                        $::JOB_PARAMS{'P938_GET_TOOLS_NODE_INFORMATION'} = "no";
                        $::JOB_PARAMS{'P938_REPLACE_FILE_PLACEHOLDERS'} = "yes";
                        $::JOB_PARAMS{'P938_INPUT_FILE_NAME'} = $filename_in;
                        $::JOB_PARAMS{'P938_OUTPUT_FILE_NAME'} = $filename_out;
                        $::JOB_PARAMS{'P938_OUTPUT_FILE_ACCESSMODE'} = "666";
                        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::938_Node_Information::main } );
                        return $rc if $rc < 0;

                        General::Logging::log_user_message("  Reading file: $filename_out");
                        $rc = General::File_Operations::read_file(
                            {
                                "filename"            => $filename_out,
                                "output-ref"          => \@config_data,
                            }
                        );
                        if ($rc != 0) {
                            General::Logging::log_user_error_message("  Failed to read file with configuration data");
                            return General::Playlist_Operations::RC_FALLBACK;
                        }
                        $data_changed = 0;
                        $found_config_cnt = 0;
                        $valid_format = 1;
                        for (@config_data) {
                            if (/^\s*config\d*/) {
                                $found_config_cnt++;
                            } elsif (/\]\]>\]\]>/sg) {
                                $valid_format = 0;
                            }
                        }

                        if ($valid_format == 0) {
                            General::Logging::log_user_error_message("  The file appear to be a netconf file, you must use job parameter 'USE_NETCONF=yes' or filename must contain file extention '.netconf'");
                            return General::Playlist_Operations::RC_FALLBACK;
                        }

                        if ($found_config_cnt == 0) {
                            General::Logging::log_user_warning_message("  The 'config' command is missing, might cause failure during loading of data");
                        }

                        if ($data_changed > 0) {
                            General::Logging::log_user_message("  Writing file: $filename_out");
                            $rc = General::File_Operations::write_file(
                                {
                                    "filename"          => $filename_out,
                                    "output-ref"        => \@config_data,
                                    "file-access-mode"  => "666"
                                }
                            );
                            if ($rc != 0) {
                                General::Logging::log_user_error_message("  Failed to write file with configuration data");
                                return General::Playlist_Operations::RC_FALLBACK;
                            }
                        } else {
                            General::Logging::log_user_message("  No need to update the file any further");
                        }
                        $written_files .= "  $filename_out\n";
                    }
                } else {
                    # We should never come here, if we do then something is wrong in this playlist
                    General::Logging::log_user_error_message("  Not expected format for job parameter '$job_parameter':\n  $::JOB_PARAMS{$job_parameter}\n");
                    return General::Playlist_Operations::RC_FALLBACK;
                }
            }
        }

        General::Logging::log_user_message("Written configuration files:\n$written_files");

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
    sub Check_If_Expect_Exists_P004S02T04 {

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
sub Add_Configuration_P004S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::913_Add_Configuration::main } );
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
sub Collect_Logs_If_Wanted_P004S04 {

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
sub Cleanup_Job_Environment_P004S05 {

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
sub Fallback001_P004S99 {

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
        'CONFIG_DATA_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The path to a file containing configuration data to load.

If 'USE_NETCONF=yes' is specified then the data in the file MUST BE in NETCONF
format for loading configuration data.
This file should contain all data for successfully loading the data, which
should include any <hello and <edit-config> tags.

If 'USE_NETCONF=no' is specified then the data in the file MUST BE in CLI format
for loading configuration data.
This file should contain all the needed commands for loading the data, which
includes 'config', '...' and 'commit' commands.

NOTE 1:
If multiple configurations files should be loaded then these files should be
specified with job parameter CONFIG_DATA_FILE_1, CONFIG_DATA_FILE_2 etc. but
the first file to be loaded is always specified with the CONFIG_DATA_FILE job
parameter.

NOTE 2:
If the files should be loaded using different users and passwords then the
files to load must follow a specific naming convention:

<filename prefix>,user=<username>[,password=<password>].<cli|netconf>

    For example: -v CONFIG_DATA_FILE=/path/scp_config,user=scp-admin.netconf
                 -v CONFIG_DATA_FILE_1=/path/csa_config,user=csa-admin.netconf
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_USER_NAME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which user name is used for loading the configuration
file into CMYP either via CLI or via NETCONF.

This parameter is optional if the name of the configuration data file contains
the user name to use in the following format:

    <filename prefix>,user=<username>[<filename suffix>]

If the naming convention is not used, then this parameter is mandatory and must
be specified.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONFIG_USER_PASSWORD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which password to be used for loading the configuration
file into CMYP either via CLI or via NETCONF.

If not specified then a check is done if the specified user (CONFIG_USER_NAME or file
naming convention) is a known user from the network configuration file and if
so then the password is taken from the network configuration file parameter for
the specified user.

This parameter can also be specified in the name of the configuration data file
using the following format:

    <filename prefix>,password=<password>[<filename suffix>]
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
Configuration Loading will take place.
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
before and after the configuration change and continue regardless if it fails.

Select "no" which is also the default value if not specified, to have the
Playlist stop if the health checks detects an error.
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
            'default_value' => "180",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the timeout value to use when checking that all PODs are up and running
after loading the configuration, if not specified then the default value will be
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
        'SKIP_PRE_HEALTHCHECK' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Option to skip pre configuration healthcheck.
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
Option to skip post configuration healthcheck.
Valid values are yes or no (default is no).
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TOOLS_NAMESPACE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls in which namespace the tools are installed on the node
and should be specified if you want to override the parameter from the network
configuration file called 'tools_namespace'.
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
checking and loading configuration data into the node.

If the value is 'yes' then the NETCONF interface on port 830 of CMYP will be
used for loading data, and if CONFIG_DATA_FILE is specified it must contain NETCONF
data.

If the value is 'no' then the CLI interface on port 22 of CMYP will be used for
loading data, and if CONFIG_DATA_FILE is specified it must contain CLI data.

This parameter can also be specified in the file extension of the configuration
data file using the following format:

    <filename prefix>[,user=<username>[,password=<password>].(netconf|cli)
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

This Playlist script is used for adding configuration data to an ESC node and
it will be called from the execute_playlist.pl script.

The loaded files can have one or more of the following place holders which will
be replaced by the real IP or Port values from the node deployment or from
network configuration file:

    - <BSF_WRKR_PORT>
    - <BSF_WRKR_TLS_PORT>
    - <CSA_WRKR_PORT>
    - <CSA_WRKR_TLS_PORT>
    - <NRFSIM_NODEPORT_HTTP>
    - <SCP_WRKR_IP>
    - <SCP_WRKR_PORT>
    - <SCP_WRKR_TLS_PORT>
    - <SEPPSIM_P1_NODEPORT> (deprecated, same port as <SEPPSIM_P1_NODEPORT_HTTPS>)
    - <SEPPSIM_P1_NODEPORT_HTTP>
    - <SEPPSIM_P1_NODEPORT_HTTPS>
    - <SEPPSIM_P2_NODEPORT> (deprecated, same port as <SEPPSIM_P2_NODEPORT_HTTPS>)
    - <SEPPSIM_P2_NODEPORT_HTTP>
    - <SEPPSIM_P2_NODEPORT_HTTPS>
    - <SEPPSIM_P3_NODEPORT> (deprecated, same port as <SEPPSIM_P3_NODEPORT_HTTPS>)
    - <SEPPSIM_P3_NODEPORT_HTTP>
    - <SEPPSIM_P3_NODEPORT_HTTPS>
    - <SEPPSIM_P4_NODEPORT> (deprecated, same port as <SEPPSIM_P4_NODEPORT_HTTPS>)
    - <SEPPSIM_P4_NODEPORT_HTTP>
    - <SEPPSIM_P4_NODEPORT_HTTPS>
    - <SEPPSIM_P5_NODEPORT> (deprecated, same port as <SEPPSIM_P5_NODEPORT_HTTPS>)
    - <SEPPSIM_P5_NODEPORT_HTTP>
    - <SEPPSIM_P5_NODEPORT_HTTPS>
    - <SEPPSIM_P6_NODEPORT> (deprecated, same port as <SEPPSIM_P6_NODEPORT_HTTPS>)
    - <SEPPSIM_P6_NODEPORT_HTTP>
    - <SEPPSIM_P6_NODEPORT_HTTPS>
    - <SEPPSIM_P7_NODEPORT> (deprecated, same port as <SEPPSIM_P7_NODEPORT_HTTPS>)
    - <SEPPSIM_P7_NODEPORT_HTTP>
    - <SEPPSIM_P7_NODEPORT_HTTPS>
    - <SEPPSIM_P8_NODEPORT> (deprecated, same port as <SEPPSIM_P8_NODEPORT_HTTPS>)
    - <SEPPSIM_P8_NODEPORT_HTTP>
    - <SEPPSIM_P8_NODEPORT_HTTPS>
    - <SEPP_WRKR_IP>
    - <SEPP_WRKR_PORT>
    - <SEPP_WRKR_TLS_PORT>
    - <TOOLS_NAMESPACE>
    - <VIP_SIG_BSF>
    - <VIP_SIG_SCP>
    - <VIP_SIG_SEPP>
    - <VIP_SIG2_SEPP>
    - <WORKER_IP>
    - <WORKER_IP_TOOLS>

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
