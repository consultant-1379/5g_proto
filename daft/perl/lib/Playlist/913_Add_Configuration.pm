package Playlist::913_Add_Configuration;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.12
#  Date     : 2023-09-20 13:53:24
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021, 2023
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
    usage
    );

# Used Perl package files
use ADP::Kubernetes_Operations;
use General::File_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;
use General::String_Operations;
use Playlist::917_Pre_Healthcheck;
use Playlist::918_Post_Healthcheck;

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

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    } else {
        $debug_command = "";
    }

    $rc = General::Playlist_Operations::execute_step( \&Check_Node_Status_Before_Change_P913S01, \&Fallback001_P913S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Add_Configuration_From_File_P913S02, \&Fallback001_P913S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Node_Status_After_Change_P913S03, \&Fallback001_P913S99 );
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
sub Check_Node_Status_Before_Change_P913S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Old_SSH_Keys_P913S01T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_If_SSH_Is_Available_To_CMYP_P913S01T02 } );
    return $rc if $rc < 0;

    if (exists $::JOB_PARAMS{'SKIP_PRE_HEALTHCHECK'} && $::JOB_PARAMS{'SKIP_PRE_HEALTHCHECK'} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::917_Pre_Healthcheck::main } );
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
    sub Remove_Old_SSH_Keys_P913S01T01 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_cli_port = $::JOB_PARAMS{'CMYP_CLI_PORT'};
        my $cmyp_netconf_port = $::JOB_PARAMS{'CMYP_NETCONF_PORT'};
        my $errorcnt = 0;
        my $rc = 0;
        my $temp;

        # Create directory '$HOME/.ssh' if it doesn't exist
        $temp = "$ENV{'HOME'}/.ssh";
        unless (-d $temp) {
            General::Logging::log_user_message("Creating directory '$temp'\n");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "mkdir -p $temp; chmod 700 $temp",
                    "hide-output"   => 1,
                }
            );
            return $rc if $rc != 0;
        }

        # Create file '$HOME/.ssh/known_hosts' if it doesn't exist
        $temp = "$ENV{'HOME'}/.ssh/known_hosts";
        unless (-f $temp) {
            General::Logging::log_user_message("Creating file '$temp'\n");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "touch $temp; chmod 600 $temp",
                    "hide-output"   => 1,
                }
            );
            return $rc if $rc != 0;
        }

        General::Logging::log_user_message("Removing old SSH keys in SSH known_hosts file\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ssh-keygen -R $cmyp_ip -f $ENV{'HOME'}/.ssh/known_hosts",
                "hide-output"   => 1,
            }
        );
        $errorcnt++ if $rc != 0;

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ssh-keygen -R '[$cmyp_ip]:$cmyp_cli_port' -f $ENV{'HOME'}/.ssh/known_hosts",
                "hide-output"   => 1,
            }
        );
        $errorcnt++ if $rc != 0;

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ssh-keygen -R '[$cmyp_ip]:$cmyp_netconf_port' -f $ENV{'HOME'}/.ssh/known_hosts",
                "hide-output"   => 1,
            }
        );
        $errorcnt++ if $rc != 0;

        if ($errorcnt == 0) {
            return 0;
        } else {
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
    sub Check_If_SSH_Is_Available_To_CMYP_P913S01T02 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port = $::JOB_PARAMS{'CMYP_CLI_PORT'};
        my $repeat_at_error = 0;
        my $timeout = 2;

        if ($::JOB_PARAMS{'USE_NETCONF'} eq "yes") {
            $cmyp_port = $::JOB_PARAMS{'CMYP_NETCONF_PORT'};
        }

        General::Logging::log_user_message("Checking if SSH is available to CMYP on port $cmyp_port at host $cmyp_ip");
        if (General::OS_Operations::is_ssh_available($cmyp_ip, $cmyp_port, $timeout, $repeat_at_error) == 1) {
            # Yes it's available, continue with next task or step
            return 0;
        } else {
            # No it's not available
            General::Logging::log_user_error_message("Not able to connect to CMYP on port $cmyp_port at host $cmyp_ip");
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
sub Add_Configuration_From_File_P913S02 {

    my $rc = 0;

    for my $job_parameter (General::String_Operations::sort_hash_keys_alphanumerically(\%::JOB_PARAMS)) {
        if ($job_parameter =~ /^CONFIG_DATA_FILE_TO_LOAD_(\d+)$/) {

            # Set global input parameter to called tasks
            $::JOB_PARAMS{'CURRENT_FILE_CNT'} = $1;

            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_Config_Before_Change_P913S02T01 } );
            return $rc if $rc < 0;

            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Add_Config_P913S02T02 } );
            return $rc if $rc < 0;

            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_Config_After_Change_P913S02T03 } );
            return $rc if $rc < 0;

        }
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
    sub Get_Config_Before_Change_P913S02T01 {

        my $current_file_cnt = $::JOB_PARAMS{'CURRENT_FILE_CNT'};

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port;
        my $file_name = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/netconf/get-config.netconf";
        my $job_parameter = "CONFIG_DATA_FILE_TO_LOAD_$current_file_cnt";
        my $output_file = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/configuration_data_before_change_$current_file_cnt.log";
        my $rc = 0;
        my $use_netconf;
        my $user_name;
        my $user_password;

        if ($::JOB_PARAMS{"${job_parameter}_LOADED"} eq "yes") {
            General::Logging::log_user_message("File number $current_file_cnt has already been loaded, skipping this task");
            return 0;
        }
        if ($::JOB_PARAMS{$job_parameter} =~ /^user=(.+),password=(.+),netconf=(yes|no),filename=.+$/) {
            $user_name = $1;
            $user_password = $2;
            $use_netconf = $3;
        } else {
            General::Logging::log_user_error_message("Unknown job parameter '$job_parameter' format:\n$::JOB_PARAMS{$job_parameter}\n");
            return 1;
        }

        if ($use_netconf eq "yes") {

            General::Logging::log_user_message("File $current_file_cnt: Fetching Configuration Data via NETCONF");
            $cmyp_port = $::JOB_PARAMS{'CMYP_NETCONF_PORT'};
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                            "--ip=$cmyp_ip " .
                                            "--user=$user_name " .
                                            "--password='$user_password' " .
                                            "--port=$cmyp_port " .
                                            "--shell=netconf " .
                                            "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                            "--command-file='$file_name' ",
                    "hide-output"   => 1,
                    "save-to-file"  => $output_file,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to fetch configuration data");
            }

        } else {

            General::Logging::log_user_message("File $current_file_cnt: Fetching Configuration Data via CLI");
            $cmyp_port = $::JOB_PARAMS{'CMYP_CLI_PORT'};
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                            "--ip=$cmyp_ip " .
                                            "--user=$user_name " .
                                            "--password='$user_password' " .
                                            "--port=$cmyp_port " .
                                            "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                            "--command='show running-config|nomore'",
                    "hide-output"   => 1,
                    "save-to-file"  => $output_file,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to fetch configuration data");
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
    sub Add_Config_P913S02T02 {

        my $current_file_cnt = $::JOB_PARAMS{'CURRENT_FILE_CNT'};

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port;
        my $file_name;
        my $job_parameter = "CONFIG_DATA_FILE_TO_LOAD_$current_file_cnt";
        my $rc = 0;
        my $use_netconf;
        my $user_name;
        my $user_password;

        if ($::JOB_PARAMS{"${job_parameter}_LOADED"} eq "yes") {
            General::Logging::log_user_message("File number $current_file_cnt has already been loaded, skipping this task");
            return 0;
        }
        if ($::JOB_PARAMS{$job_parameter} =~ /^user=(.+),password=(.+),netconf=(yes|no),filename=(.+)$/) {
            $user_name = $1;
            $user_password = $2;
            $use_netconf = $3;
            $file_name = $4;
        } else {
            General::Logging::log_user_error_message("Unknown job parameter '$job_parameter' format:\n$::JOB_PARAMS{$job_parameter}\n");
            return 1;
        }

        if ($use_netconf eq "yes") {

            General::Logging::log_user_message("File $current_file_cnt: Loading Configuration Data via NETCONF:\n$file_name");
            $cmyp_port = $::JOB_PARAMS{'CMYP_NETCONF_PORT'};
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                            "--ip=$cmyp_ip " .
                                            "--user=$user_name " .
                                            "--password='$user_password' " .
                                            "--port=$cmyp_port " .
                                            "--shell=netconf " .
                                            "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                            "--command-file='$file_name' ",
                    "hide-output"   => 1,
                }
            );
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) Adding Configuration was successful: $file_name";

                # Mark successful loading and fetching of data for this file
                $::JOB_PARAMS{"${job_parameter}_LOADED"} = "yes";
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to load config data");
                push @::JOB_STATUS, "(x) Adding Configuration failed: $file_name";
            }

        } else {

            General::Logging::log_user_message("File $current_file_cnt: Loading Configuration Data via CLI:\n$file_name");
            $cmyp_port = $::JOB_PARAMS{'CMYP_CLI_PORT'};
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                            "--ip=$cmyp_ip " .
                                            "--user=$user_name " .
                                            "--password='$user_password' " .
                                            "--port=$cmyp_port " .
                                            "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                            "--command-file='$file_name' ",
                    "hide-output"   => 1,
                }
            );
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) Adding Configuration was successful: $file_name";

                # Mark successful loading and fetching of data for this file
                $::JOB_PARAMS{"${job_parameter}_LOADED"} = "yes";
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to load config data");
                push @::JOB_STATUS, "(x) Adding Configuration failed: $file_name";
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
    sub Get_Config_After_Change_P913S02T03 {

        my $current_file_cnt = $::JOB_PARAMS{'CURRENT_FILE_CNT'};

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port;
        my $file_name = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/netconf/get-config.netconf";
        my $job_parameter = "CONFIG_DATA_FILE_TO_LOAD_$current_file_cnt";
        my $output_file = "$::JOB_PARAMS{'_JOB_LOG_DIR'}/configuration_data_after_change_$current_file_cnt.log";
        my $rc = 0;
        my $use_netconf;
        my $user_name;
        my $user_password;

        if ($::JOB_PARAMS{"${job_parameter}_LOADED"} eq "no") {
            General::Logging::log_user_message("File number $current_file_cnt was not successfully loaded, skipping this task");
            return 0;
        }
        if ($::JOB_PARAMS{$job_parameter} =~ /^user=(.+),password=(.+),netconf=(yes|no),filename=.+$/) {
            $user_name = $1;
            $user_password = $2;
            $use_netconf = $3;
        } else {
            General::Logging::log_user_error_message("Unknown job parameter '$job_parameter' format:\n$::JOB_PARAMS{$job_parameter}\n");
            return 1;
        }

        if ($use_netconf eq "yes") {

            General::Logging::log_user_message("File $current_file_cnt: Fetching Configuration Data via NETCONF");
            $cmyp_port = $::JOB_PARAMS{'CMYP_NETCONF_PORT'};
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                            "--ip=$cmyp_ip " .
                                            "--user=$user_name " .
                                            "--password='$user_password' " .
                                            "--port=$cmyp_port " .
                                            "--shell=netconf " .
                                            "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                            "--command-file='$file_name' ",
                    "hide-output"   => 1,
                    "save-to-file"  => $output_file,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to fetch configuration data");
            }

        } else {

            General::Logging::log_user_message("File $current_file_cnt: Fetching Configuration Data via CLI");
            $cmyp_port = $::JOB_PARAMS{'CMYP_CLI_PORT'};
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                            "--ip=$cmyp_ip " .
                                            "--user=$user_name " .
                                            "--password='$user_password' " .
                                            "--port=$cmyp_port " .
                                            "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                            "--command='show running-config|nomore'",
                    "hide-output"   => 1,
                    "save-to-file"  => $output_file,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to fetch configuration data");
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
sub Check_Node_Status_After_Change_P913S03 {

    my $rc = 0;

    if (exists $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} && $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::918_Post_Healthcheck::main } );
        return $rc if $rc < 0;
    }

    return $rc;
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P913S99 {

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

This Playlist is used for adding configuration data via NETCONF or CLI.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
