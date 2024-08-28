package Playlist::802_ECCD_Installation;

################################################################################
#
#  Author   : everhel, eustone
#
#  Revision : 1.37
#  Date     : 2024-06-19 13:54:00
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
use File::Basename qw(dirname basename);

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    usage
    usage_return_playlist_variables
    );

use General::File_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::108_Miscellaneous_Tasks;
use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::903_Global_Preprocess;
use Playlist::904_Global_Postprocess;

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
#  1: The call failed
#  255: Stepout wanted
#
# -----------------------------------------------------------------------------
sub main {
    my $rc;

    # Set Job Type
    $::JOB_PARAMS{'JOBTYPE'} = "ECCD_INSTALLATION";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # If the playlist needs to have access to Network Configuration parameters
    # then it needs to check for this and give an error or ask the user to
    # provide the file name.
    if (General::Playlist_Operations::is_mandatory_network_config_loaded() != 0) {
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P802S01, \&Fallback001_P802S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P802S02, \&Fallback001_P802S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Install_ECCD_P802S03, \&Fallback001_P802S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Post_Install_Actions_P802S04, \&Fallback001_P802S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P802S05, \&Fallback001_P802S99 );
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
sub Initialize_Job_Environment_P802S01 {

    my $rc;

    # This playlist does not use docker, kubectl or helm commands so skip these checks
    $::JOB_PARAMS{'SKIP_DOCKER_AND_NERDCTL_CHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_HELM_CHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_KUBECTL_CHECK'} = "yes";

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
sub Check_Job_Parameters_P802S02 {

        my $openstack_host_address = "";
        my $openstack_user = "";
        my $openstack_password = "";
        my $openstack_timeout = "90";   # Change this default value if needed
        my $eccd_template_file = "";
        my $eccd_environment_file = "";
        my $eccd_stack_name = "";
        my $eccd_stack_name_to_be_deleted = "";
        my $vip_oam = "";
        my $rc = 0;
        my @result;

        # Get the proper OPENSTACK_HOST_ADDRESS
        General::Logging::log_user_message("Checking Job parameter 'OPENSTACK_HOST_ADDRESS'");
        if ($::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'} ne "") {
            $openstack_host_address = $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'};
        } elsif ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "capo" && exists $::NETWORK_CONFIG_PARAMS{'eccd_capo_lcm_host_address'} && exists $::NETWORK_CONFIG_PARAMS{'eccd_capo_lcm_host_address'}{'value'} && $::NETWORK_CONFIG_PARAMS{'eccd_capo_lcm_host_address'}{'value'} ne "CHANGEME") {
            $openstack_host_address = $::NETWORK_CONFIG_PARAMS{'eccd_capo_lcm_host_address'}{'value'};
            $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'} = $openstack_host_address;
        } elsif ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo" && exists $::NETWORK_CONFIG_PARAMS{'eccd_openstack_host_address'} && exists $::NETWORK_CONFIG_PARAMS{'eccd_openstack_host_address'}{'value'} && $::NETWORK_CONFIG_PARAMS{'eccd_openstack_host_address'}{'value'} ne "CHANGEME") {
            $openstack_host_address = $::NETWORK_CONFIG_PARAMS{'eccd_openstack_host_address'}{'value'};
            $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'} = $openstack_host_address;
        } else {
            General::Logging::log_user_error_message("Job parameter 'OPENSTACK_HOST_ADDRESS' has not been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper OPENSTACK_USER
        General::Logging::log_user_message("Checking Job parameter 'OPENSTACK_USER'");
        if ($::JOB_PARAMS{'OPENSTACK_USER'} ne "") {
            $openstack_user = $::JOB_PARAMS{'OPENSTACK_USER'};
        } elsif ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "capo" && exists $::NETWORK_CONFIG_PARAMS{'eccd_capo_lcm_user'} && exists $::NETWORK_CONFIG_PARAMS{'eccd_capo_lcm_user'}{'value'} && $::NETWORK_CONFIG_PARAMS{'eccd_capo_lcm_user'}{'value'} ne "CHANGEME") {
            $openstack_user = $::NETWORK_CONFIG_PARAMS{'eccd_capo_lcm_user'}{'value'};
            $::JOB_PARAMS{'OPENSTACK_USER'} = $openstack_user;
        } elsif ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo" && exists $::NETWORK_CONFIG_PARAMS{'eccd_openstack_user'} && exists $::NETWORK_CONFIG_PARAMS{'eccd_openstack_user'}{'value'} && $::NETWORK_CONFIG_PARAMS{'eccd_openstack_user'}{'value'} ne "CHANGEME") {
            $openstack_user = $::NETWORK_CONFIG_PARAMS{'eccd_openstack_user'}{'value'};
            $::JOB_PARAMS{'OPENSTACK_USER'} = $openstack_user;
        } else {
            General::Logging::log_user_error_message("Job parameter 'OPENSTACK_USER' has not been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper OPENSTACK_PASSWORD
        General::Logging::log_user_message("Checking Job parameter 'OPENSTACK_PASSWORD'");
        if ($::JOB_PARAMS{'OPENSTACK_PASSWORD'} ne "") {
            $openstack_password = $::JOB_PARAMS{'OPENSTACK_PASSWORD'};
        } elsif ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "capo" && exists $::NETWORK_CONFIG_PARAMS{'eccd_capo_lcm_password'} && exists $::NETWORK_CONFIG_PARAMS{'eccd_capo_lcm_password'}{'value'} && $::NETWORK_CONFIG_PARAMS{'eccd_capo_lcm_password'}{'value'} ne "CHANGEME") {
            $openstack_password = $::NETWORK_CONFIG_PARAMS{'eccd_capo_lcm_password'}{'value'};
            $::JOB_PARAMS{'OPENSTACK_PASSWORD'} = $openstack_password;
        } elsif ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo" && exists $::NETWORK_CONFIG_PARAMS{'eccd_openstack_password'} && exists $::NETWORK_CONFIG_PARAMS{'eccd_openstack_password'}{'value'} && $::NETWORK_CONFIG_PARAMS{'eccd_openstack_password'}{'value'} ne "CHANGEME") {
            $openstack_password = $::NETWORK_CONFIG_PARAMS{'eccd_openstack_password'}{'value'};
            $::JOB_PARAMS{'OPENSTACK_PASSWORD'} = $openstack_password;
        } else {
            General::Logging::log_user_error_message("Job parameter 'OPENSTACK_PASSWORD' has not been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper OPENSTACK_PASSWORD
        General::Logging::log_user_message("Checking Job parameter 'OPENSTACK_TIMEOUT'");
        if ($::JOB_PARAMS{'OPENSTACK_TIMEOUT'} ne "") {
            $openstack_timeout = $::JOB_PARAMS{'OPENSTACK_TIMEOUT'};
        } else {
            General::Logging::log_user_message("Job parameter 'OPENSTACK_TIMEOUT' has not been set, using default of $openstack_timeout minutes");
            $::JOB_PARAMS{'OPENSTACK_TIMEOUT'} = $openstack_timeout;
        }

        # Get the proper ECCD_TEMPLATE_FILE
        General::Logging::log_user_message("Checking Job parameter 'ECCD_TEMPLATE_FILE'");
        if ($::JOB_PARAMS{'ECCD_TEMPLATE_FILE'} ne "") {
            $eccd_template_file = $::JOB_PARAMS{'ECCD_TEMPLATE_FILE'};
        } elsif ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo") {
            General::Logging::log_user_error_message("Job parameter 'ECCD_TEMPLATE_FILE' has not been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper ECCD_ENVIRONMENT_FILE
        General::Logging::log_user_message("Checking Job parameter 'ECCD_ENVIRONMENT_FILE'");
        if ($::JOB_PARAMS{'ECCD_ENVIRONMENT_FILE'} ne "") {
            $eccd_environment_file = $::JOB_PARAMS{'ECCD_ENVIRONMENT_FILE'};
        } else {
            General::Logging::log_user_error_message("Job parameter 'ECCD_ENVIRONMENT_FILE' has not been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper ECCD_STACK_NAME
        General::Logging::log_user_message("Checking Job parameter 'ECCD_STACK_NAME'");
        if ($::JOB_PARAMS{'ECCD_STACK_NAME'} ne "") {
            $eccd_stack_name = $::JOB_PARAMS{'ECCD_STACK_NAME'};
        } else {
            General::Logging::log_user_error_message("Job parameter 'ECCD_STACK_NAME' has not been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper ECCD_STACK_NAME_TO_BE_DELETED
        General::Logging::log_user_message("Checking Job parameter 'ECCD_STACK_NAME_TO_BE_DELETED'");
        if ($::JOB_PARAMS{'ECCD_STACK_NAME_TO_BE_DELETED'} ne "") {
            $eccd_stack_name_to_be_deleted = $::JOB_PARAMS{'ECCD_STACK_NAME_TO_BE_DELETED'};
        } else {
            $::JOB_PARAMS{'ECCD_STACK_NAME_TO_BE_DELETED'} = $eccd_stack_name;
            $eccd_stack_name_to_be_deleted = $::JOB_PARAMS{'ECCD_STACK_NAME_TO_BE_DELETED'};
        }

        # Get the proper VIP_OAM
        General::Logging::log_user_message("Checking Job parameter 'VIP_OAM' and Network Config parameter 'eric_sc_values_anchor_parameter_VIP_OAM'");
        if ($::JOB_PARAMS{'VIP_OAM'} ne "") {
            $vip_oam = $::JOB_PARAMS{'VIP_OAM'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_OAM'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_OAM'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'VIP_OAM'} = $::NETWORK_CONFIG_PARAMS{'eric_sc_values_anchor_parameter_VIP_OAM'}{'value'};
                $vip_oam = $::JOB_PARAMS{'VIP_OAM'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'eric_sc_values_anchor_parameter_VIP_OAM' has not been set and Job parameter 'VIP_OAM' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'VIP_OAM', nor Network Config parameter 'eric_sc_values_anchor_parameter_VIP_OAM' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        General::Logging::log_user_message(
            sprintf "%s\n%s\n%s\n%s\n%s\n%s\n%s\n",
                                                "ECCD_ENVIRONMENT_FILE=$eccd_environment_file",
                                                "ECCD_STACK_NAME=$eccd_stack_name",
                                                "ECCD_STACK_NAME_TO_BE_DELETED=$eccd_stack_name_to_be_deleted",
                                                "ECCD_TEMPLATE_FILE=$eccd_template_file",
                                                "OPENSTACK_HOST_ADDRESS=$openstack_host_address",
                                                "OPENSTACK_TIMEOUT=$openstack_timeout",
                                                "OPENSTACK_USER=$openstack_user"
        );

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
sub Install_ECCD_P802S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Delete_ECCD_Stack_P802S03T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_ECCD_Stack_P802S03T02 } );
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
    sub Delete_ECCD_Stack_P802S03T01 {
        my $openstack_host_address = $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'};
        my $openstack_user = $::JOB_PARAMS{'OPENSTACK_USER'};
        my $openstack_password = $::JOB_PARAMS{'OPENSTACK_PASSWORD'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my $eccd_stack_name = $::JOB_PARAMS{'ECCD_STACK_NAME_TO_BE_DELETED'};

        my $rc = 0;
        my @result;

        if ($::JOB_PARAMS{'SKIP_STACK_DELETE'} eq "no") {
            if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo") {
                General::Logging::log_user_message("Delete ECCD Stack.\nThis will take a while to complete.\n");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                                "--ip=$openstack_host_address " .
                                                "--user=$openstack_user " .
                                                "--password='$openstack_password' " .
                                                "--timeout=$expect_timeout " .
                                                "--command='. SC-adminovercloudrc' " .
                                                "--command='openstack stack delete --yes --wait $eccd_stack_name' ",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );

                if ($rc == 0) {
                    push @::JOB_STATUS, '(/) Deletion of ECCD stack was successful';
                } else {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to delete ECCD stack");
                }
            } else {
                General::Logging::log_user_message("Delete ECCD Stack.\nThis will take a while to complete.\n");
                $rc = General::OS_Operations::send_command(
                {
                        "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                                "--ip=$openstack_host_address " .
                                                "--user=$openstack_user " .
                                                "--password='$openstack_password' " .
                                                "--timeout=$expect_timeout " .
                                                "--command='export PS1=\"daft: \"' " .
                                                "--command='ccdadm context set -n $eccd_stack_name' " .
                                                "--command='ccdadm cluster undeploy --debug' " .
                                                "--command='rm -fr .ccdadm/$eccd_stack_name' ",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );

                if ($rc == 0) {
                    push @::JOB_STATUS, '(/) Deletion of ECCD stack was successful';
                } else {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    push @::JOB_STATUS, '(x) Deletion of ECCD stack failed';
                    General::Logging::log_user_error_message("Failed to delete ECCD stack");
                }
            }
        } else {
            push @::JOB_STATUS, '(-) Deletion of ECCD stack was skipped';
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
    sub Install_ECCD_Stack_P802S03T02 {
        my $openstack_host_address = $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'};
        my $openstack_user = $::JOB_PARAMS{'OPENSTACK_USER'};
        my $openstack_password = $::JOB_PARAMS{'OPENSTACK_PASSWORD'};
        my $openstack_timeout = $::JOB_PARAMS{'OPENSTACK_TIMEOUT'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my $eccd_template_file = $::JOB_PARAMS{'ECCD_TEMPLATE_FILE'};
        my $eccd_environment_file = $::JOB_PARAMS{'ECCD_ENVIRONMENT_FILE'};
        my $eccd_stack_name = $::JOB_PARAMS{'ECCD_STACK_NAME'};

        my $rc = 0;
        my $result;
        my @result;
        my $run_update = 0;

        if ($::JOB_PARAMS{'SKIP_STACK_INSTALLATION'} eq "no") {
            if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo") {
                General::Logging::log_user_message("Install ECCD Stack.\nThis will take a while to complete.\n");
                $rc = General::OS_Operations::send_command(
                {
                        "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                                "--ip=$openstack_host_address " .
                                                "--user=$openstack_user " .
                                                "--password='$openstack_password' " .
                                                "--timeout=$expect_timeout " .
                                                "--command='. SC-adminovercloudrc' " .
                                                "--command='openstack stack create -t $eccd_template_file -e $eccd_environment_file --wait --timeout $openstack_timeout $eccd_stack_name' ",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );

                if ($rc == 0) {
                    push @::JOB_STATUS, '(/) Installing ECCD stack was successful';
                } else {
                    if ( $rc == 1 && @result != 0 ) {
                        foreach $result ( @result ) {
                            if ( $result =~ m/^.*:\s+CREATE_FAILED\s+CREATE\s+aborted\s+\(Task\s+create\s+from\s+.*Resource.*\s+"post_deployment"\s+Stack\s+"$eccd_stack_name"\s+\[.*\]\s+Timed out.*\)$/ ) {
                                $run_update = 1;
                                last;
                            }
                        }

                        if ( $run_update == 1 ) {
                            General::Logging::log_user_message("Trying to Update ECCD Stack due to post_deployment timeout");
                            $rc = General::OS_Operations::send_command(
                                {
                                    "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                                            "--ip=$openstack_host_address " .
                                                            "--user=$openstack_user " .
                                                            "--password='$openstack_password' " .
                                                            "--timeout=$expect_timeout " .
                                                            "--command='. SC-adminovercloudrc' " .
                                                            "--command='openstack stack update -t $eccd_template_file -e $eccd_environment_file --wait --timeout $openstack_timeout $eccd_stack_name' ",
                                    "hide-output"   => 1,
                                    "return-output" => \@result,
                                }
                            );

                            if ($rc == 0) {
                                push @::JOB_STATUS, '(/) Installing (Updating) ECCD stack was successful';
                            } else {
                                # Display the result in case of error
                                General::OS_Operations::write_last_temporary_output_to_progress();

                                General::Logging::log_user_error_message("Failed to install (update) ECCD stack");
                            }
                        }
                    } else {
                        # Display the result in case of error
                        General::OS_Operations::write_last_temporary_output_to_progress();

                        General::Logging::log_user_error_message("Failed to install ECCD stack");
                    }
                }
            } else {
                my $dir_name = dirname $eccd_environment_file;

                General::Logging::log_user_message("Install ECCD Stack.\nThis will take a while to complete.\n");
                $rc = General::OS_Operations::send_command(
                {
                        "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                                "--ip=$openstack_host_address " .
                                                "--user=$openstack_user " .
                                                "--password='$openstack_password' " .
                                                "--timeout=$expect_timeout " .
                                                "--command='export PS1=\"daft: \"' " .
                                                "--command='ccdadm cluster config validate -n $eccd_stack_name -c $eccd_environment_file' " .
                                                "--command='ccdadm cluster define -n $eccd_stack_name -c $eccd_environment_file' " .
                                                "--command='ccdadm context set -n $eccd_stack_name' " .
                                                "--command='\@\@PROMPT_DETECT_TIMEOUT=60' " .         # Extend the prompt detection timeout because of progress messages in the printout coming every 10-15 seconds
                                                "--command='ccdadm cluster deploy --debug --swPackage $dir_name' ",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );

                if ($rc == 0) {
                    push @::JOB_STATUS, '(/) Installing ECCD stack was successful';
                } else {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    push @::JOB_STATUS, '(x) Installing ECCD stack failed';
                    General::Logging::log_user_error_message("Failed to install ECCD stack");
                }
            }
        } else {
            push @::JOB_STATUS, '(-) Installing (Updating) ECCD stack was skipped';
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
sub Post_Install_Actions_P802S04 {

    my $rc;

    if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_Generated_Local_Registry_Password_P802S04T01 } );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_Director_or_Controlplane_IPs_P802S04T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Update_Known_Hosts_File_On_Openstack_Server_P802S04T03 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Update_Known_Hosts_File_On_Localhost_P802S04T04 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Add_Authorized_Keys_On_Nodes_P802S04T05 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Needed_Files_To_Nodes_P802S04T06 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_DAFT_Files_To_Nodes_P802S04T07 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Update_Hosts_File_On_Nodes_P802S04T08 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Local_Docker_Registry_Secret_P802S04T09 } );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_BFD_Sessions_P802S04T10 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&CMYP_Workaround_P802S04T11 } );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_Expect_P802S04T12 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_Gnuplot_P802S04T13 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Update_bashrc_File_P802S04T14 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_And_Fix_Ingress_Nginx_Service_P802S04T15 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_kubeconfig_File_P802S04T16 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_kubeconfig_File_Towards_Cluster_P802S04T17 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Update_kubeconfig_File_On_Build_Slaves_P802S04T18 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Worker_Address_Against_Network_Config_File_P802S04T19 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Add_Missing_Route_For_Worker_Eth2_Traffic_P802S04T20 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Add_Missing_Route_For_External_kubeconfig_Access_P802S04T21 } );
        return $rc if $rc < 0;
    }

    #
    # Start CoreDNS handling
    #

    if ($::JOB_PARAMS{'NODE_KUBECONFIG_FILE_WORKS'} eq "yes") {

        # To avoid removing directories before all tasks has been executed we will
        # only execute the cleanup at the end.
        General::State_Machine::always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

        # Save old values and set new values for the 108 playlist
        my $saved_worker_ip_ipv4 = $::JOB_PARAMS{'worker_ip_ipv4'};
        my $saved_worker_ip_ipv6 = $::JOB_PARAMS{'worker_ip_ipv6'};
        $::JOB_PARAMS{'worker_ip_ipv4'} = $::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv4_ADDRESS'};
        $::JOB_PARAMS{'worker_ip_ipv6'} = $::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv6_ADDRESS'};

        # We now need to update the kubectl and helm commands to use the new kube config file
        # so for this reason we need to enable execution of certain tasks again.
        General::State_Machine::state_update( { "state" => "rerun", "task-name" => "Playlist::901_Initialize_Job_Environment::main" } );
        General::State_Machine::state_update( { "state" => "rerun", "task-name" => "Playlist::901_Initialize_Job_Environment::Check_for_KUBECONFIG_P901S01T02" } );
        General::State_Machine::state_update( { "state" => "rerun", "task-name" => "Playlist::901_Initialize_Job_Environment::Check_for_helm_P901S01T04" } );
        General::State_Machine::state_update( { "state" => "rerun", "task-name" => "Playlist::901_Initialize_Job_Environment::Check_for_kubectl_P901S01T05" } );
        $::JOB_PARAMS{'KUBECONFIG'} = $::JOB_PARAMS{'NODE_KUBECONFIG_FILE'};
        $::JOB_PARAMS{'SKIP_HELM_CHECK'} = "no";
        $::JOB_PARAMS{'SKIP_KUBECTL_CHECK'} = "no";

        $::JOB_PARAMS{'LOAD_CORE_DNS_DATA'} = "yes";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::108_Miscellaneous_Tasks::main } );

        # Restore old values
        $::JOB_PARAMS{'worker_ip_ipv4'} = $saved_worker_ip_ipv4;
        $::JOB_PARAMS{'worker_ip_ipv6'} = $saved_worker_ip_ipv6;

        # Now we need to allow execution of the cleanup.
        General::State_Machine::remove_always_skip_task("Playlist::902_Cleanup_Job_Environment::main");

        return $rc if $rc < 0;

    } else {

        General::Logging::log_user_warning_message("Unable to connect to the node with the new kube config file, so not possible to automatically load coredns data.\nYou should manually execute the 108_Miscellaneous_Tasks playlist with '-v LOAD_CORE_DNS_DATA=yes' on the node.\n");

        # Write extra status messages to file
        $rc = General::File_Operations::write_file(
            {
                "filename"          => "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/extra_status_messages.txt",
                "append-file"       => 1,
                "output-ref"        => [
                    "",
                    "coredns Data",
                    "============",
                    "",
                    "Make sure you manually execute playlist 108_Miscellaneous_Tasks with parameter 'LOAD_CORE_DNS_DATA=yes' on the node.",
                    "",
                ],
            }
        );
    }

    #
    # End CoreDNS handling
    #

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
    sub Get_Generated_Local_Registry_Password_P802S04T01 {

        my $openstack_host_address = $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'};
        my $openstack_user = $::JOB_PARAMS{'OPENSTACK_USER'};
        my $openstack_password = $::JOB_PARAMS{'OPENSTACK_PASSWORD'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my $eccd_stack_name = $::JOB_PARAMS{'ECCD_STACK_NAME'};

        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Get Local Registry Password");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$openstack_host_address " .
                                        "--user=$openstack_user " .
                                        "--password='$openstack_password' " .
                                        "--timeout=$expect_timeout " .
                                        "--command='. SC-adminovercloudrc' " .
                                        "--command='openstack stack output show $eccd_stack_name container_registry_custom_pw -f json' ",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0 && @result != 0) {
            foreach ( @result ) {
                if ( $_ =~ m/^\s*"output_value": "([^"]+)"/ ) {
                    $::JOB_PARAMS{'LOCAL_REGISTRY_KEY'} = $1;
                }
            }
            push @::JOB_STATUS, '(/) Fetching local registry password was successful';

            if ($::JOB_PARAMS{'private_registry_password'} ne $::JOB_PARAMS{'LOCAL_REGISTRY_KEY'}) {
                # Write extra status messages to file
                $rc = General::File_Operations::write_file(
                    {
                        "filename"          => "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/extra_status_messages.txt",
                        "append-file"       => 1,
                        "output-ref"        => [
                            "Local Registry Password",
                            "=======================",
                            "",
                            "Make sure that you update the network configuration file(s) inside the GIT repository with the new local registry password.",
                            "The parameter to update is called \"private_registry_password\" and the value should be set to \"$::JOB_PARAMS{'LOCAL_REGISTRY_KEY'}\".",
                            "",
                        ],
                    }
                );
            } else {
                General::Logging::log_user_message("The Local Registry Password read from ECCD already match the password in the network config file, so no need to update the 'private_registry_password' parameter");
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to fetch local registry password");
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
    sub Get_Director_or_Controlplane_IPs_P802S04T02 {

        my $openstack_host_address = $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'};
        my $openstack_user = $::JOB_PARAMS{'OPENSTACK_USER'};
        my $openstack_password = $::JOB_PARAMS{'OPENSTACK_PASSWORD'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my $eccd_stack_name = $::JOB_PARAMS{'ECCD_STACK_NAME'};

        my $rc = 0;
        my @result;

        if ($::JOB_PARAMS{'ECCD_STACK_TYPE'} eq "non-capo") {
            General::Logging::log_user_message("Get director IP Addresses");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                            "--ip=$openstack_host_address " .
                                            "--user=$openstack_user " .
                                            "--password='$openstack_password' " .
                                            "--timeout=$expect_timeout " .
                                            "--command='. SC-adminovercloudrc' " .
                                            "--command='openstack server list | grep $eccd_stack_name | grep --color=never director-' ",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
            );
        } else {
            General::Logging::log_user_message("Get control plane IP Addresses");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                            "--ip=$openstack_host_address " .
                                            "--user=$openstack_user " .
                                            "--password='$openstack_password' " .
                                            "--timeout=$expect_timeout " .
                                            "--command='openstack server list | grep $eccd_stack_name | grep --color=never controlplane-' ",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
            );
        }
        $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'} = "";
        if ($rc == 0) {
            foreach ( @result ) {
                if (/.+ ACTIVE\s+\|\s+\S+=(\S+),/) {
                    $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'} .= "$1,";
                } elsif (/.+ ACTIVE\s+\|\s+\S+=(\S+);/ ) {
                    $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'} .= "$1,";
                }
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to fetch director IP addresses");
            return $rc;
        }
        $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'} =~ s/,$//;

        if ($::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'} ne "") {
            General::Logging::log_user_message("DIRECTOR_OR_CONTROLPLANE_IPS=$::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'}");
            push @::JOB_STATUS, '(/) Fetching director or control plane IP addresses was successful';
            return 0;
        } else {
            General::Logging::log_user_message("No director or control plane IP addresses found");
            push @::JOB_STATUS, '(x) Fetching director or control plane IP addresses failed';
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
    sub Update_Known_Hosts_File_On_Openstack_Server_P802S04T03 {

        my $command = "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp";
        my $openstack_host_address = $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'};
        my $openstack_user = $::JOB_PARAMS{'OPENSTACK_USER'};
        my $openstack_password = $::JOB_PARAMS{'OPENSTACK_PASSWORD'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my $rc = 0;

        $command .= " --ip=$openstack_host_address";
        $command .= " --user=$openstack_user";
        $command .= " --password='$openstack_password'";
        $command .= " --timeout=$expect_timeout";
        $command .= " --command='export PS1=\"daft: \"'";
        for my $ip (@ip_addresses) {
            $command .= " --command='ssh-keygen -R $ip -f ~/.ssh/known_hosts'";
            $command .= " --command='ssh-keyscan $ip >> ~/.ssh/known_hosts'";
        }

        General::Logging::log_user_message("Update known_hosts file on Openstack server with new director or control plane IP addresses");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Updating known_hosts file on Openstack Server was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to update known_hosts file");
            push @::JOB_STATUS, '(x) Updating known_hosts file on Openstack Server failed';
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
    sub Update_Known_Hosts_File_On_Localhost_P802S04T04 {

        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my @commands = ();
        my $node_vip_address = exists $::JOB_PARAMS{'node_vip_address'} ? $::JOB_PARAMS{'node_vip_address'} : "CHANGEME";
        my $rc = 0;

        for my $ip (@ip_addresses) {
            push @commands, "${debug_command}ssh-keygen -R $ip -f ~/.ssh/known_hosts";
            push @commands, "${debug_command}ssh-keyscan $ip >> ~/.ssh/known_hosts";
        }

        if ($node_vip_address ne "CHANGEME") {
            push @commands, "${debug_command}ssh-keygen -R $node_vip_address -f ~/.ssh/known_hosts";
            push @commands, "${debug_command}ssh-keyscan $node_vip_address >> ~/.ssh/known_hosts";
        }

        General::Logging::log_user_message("Update known_hosts file on localhost with new director IP addresses");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Updating known_hosts file on localhost was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to update known_hosts file");
            push @::JOB_STATUS, '(x) Updating known_hosts file on localhost failed';
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
    sub Add_Authorized_Keys_On_Nodes_P802S04T05 {

        my $openstack_host_address = $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'};
        my $openstack_user = $::JOB_PARAMS{'OPENSTACK_USER'};
        my $openstack_password = $::JOB_PARAMS{'OPENSTACK_PASSWORD'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my $rc = 0;
        my @result;
        my $command = "";

        General::Logging::log_user_message("Copy authorized key file to workspace directory");
        $rc = General::OS_Operations::send_command(
            {
                "commands"       => [
                    "${debug_command}cp -f $::JOB_PARAMS{'_PACKAGE_DIR'}/templates/authorized_keys/authorized_keys_Template $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/configurationfiles/",
                ],
                "ignore-errors" => 0,
                "hide-output"   => 1,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to copy authorized key file to workspace");
            return $rc;
        }

        General::Logging::log_user_message("Copy authorized keys file to openstack");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/scp_files.exp " .
                                        "--from-data='$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/configurationfiles/authorized_keys_Template' " .
                                        "--to-data='$openstack_user\@$openstack_host_address:/home/$openstack_user/authorized_keys_Template' " .
                                        "--password='$openstack_password' " .
                                        "--timeout-value=$expect_timeout ",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Copying authorized keys to openstack successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to copy authorized keys to openstack");
            return $rc;
        }

        $command = "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp --ip=$openstack_host_address --user=$openstack_user --password='$openstack_password' --timeout=$expect_timeout";
        for my $ip (@ip_addresses) {
            $command .= " --command='scp -q /home/$openstack_user/authorized_keys_Template eccd\@$ip:/home/eccd/authorized_keys_Template'";
            $command .= " --command='ssh -q eccd\@$ip'";
            $command .= " --command='cat /home/eccd/authorized_keys_Template >> /home/eccd/.ssh/authorized_keys'";
            $command .= " --command='rm -f /home/eccd/authorized_keys_Template'";
            $command .= " --command='exit'";
        }
        $command .= " --command='rm -f /home/$openstack_user/authorized_keys_Template'";

        General::Logging::log_user_message("Add authorized keys to director or control plane nodes");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Adding authorized keys to director or control plane nodes was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            push @::JOB_STATUS, '(x) Adding authorized keys to director or control plane nodes failed';
            General::Logging::log_user_error_message("Failed to add authorized keys to director or control plane nodes");
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
    sub Copy_Needed_Files_To_Nodes_P802S04T06 {

        my @commands = ();
        my $director_0_ip = $::JOB_PARAMS{'DIRECTOR_0_IP'};
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my $rc = 0;
        my @result;

        for my $ip (@ip_addresses) {
            # Copy template directory structure
            push @commands, "${debug_command}scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR -pr $::JOB_PARAMS{'_PACKAGE_DIR'}/templates/eccd_installation_files/* eccd\@$ip:/home/eccd/";

            # Check if we need to copy over other files
            #
            # bin/ files
            #
            if (! -f "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/eccd_installation_files/bin/clean_directory.pl") {
                # The directory structure does not seem to contain the file, check if it exist in an alternative path
                if (-f "$::JOB_PARAMS{'_PACKAGE_DIR'}/../scripts/clean_directory.pl") {
                    push @commands, "${debug_command}scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR -pr $::JOB_PARAMS{'_PACKAGE_DIR'}/../scripts/clean_directory.pl eccd\@$ip:/home/eccd/bin/";
                }
            }
            if (! -f "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/eccd_installation_files/bin/collect_ADP_logs.sh") {
                # The directory structure does not seem to contain the file, check if it exist in an alternative path
                if (-f "$::JOB_PARAMS{'_PACKAGE_DIR'}/../scripts/collect_ADP_logs.sh") {
                    push @commands, "${debug_command}scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR -pr $::JOB_PARAMS{'_PACKAGE_DIR'}/../scripts/collect_ADP_logs.sh eccd\@$ip:/home/eccd/bin/";
                }
            }
            if (! -f "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/eccd_installation_files/bin/download_csar.pl") {
                # The directory structure does not seem to contain the file, check if it exist in an alternative path
                if (-f "$::JOB_PARAMS{'_PACKAGE_DIR'}/../scripts/download_csar.pl") {
                    push @commands, "${debug_command}scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR -pr $::JOB_PARAMS{'_PACKAGE_DIR'}/../scripts/download_csar.pl eccd\@$ip:/home/eccd/bin/";
                }
            }
            if (! -f "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/eccd_installation_files/bin/protected_artifactory_files.txt") {
                # The directory structure does not seem to contain the file, check if it exist in an alternative path
                if (-f "$::JOB_PARAMS{'_PACKAGE_DIR'}/../scripts/protected_artifactory_files.txt") {
                    push @commands, "${debug_command}scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR -pr $::JOB_PARAMS{'_PACKAGE_DIR'}/../scripts/protected_artifactory_files.txt eccd\@$ip:/home/eccd/bin/";
                }
            }
            #
            # DT/ files
            #
            if (! -f "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/eccd_installation_files/DT/sample_broker_config,user=expert.netconf") {
                # The directory structure does not seem to contain the file, check if it exist in an alternative path
                if (-f "$::JOB_PARAMS{'_PACKAGE_DIR'}/../Jenkins/PipeConfig/sample_broker_config.netconf") {
                    push @commands, "${debug_command}scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR -pr $::JOB_PARAMS{'_PACKAGE_DIR'}/../Jenkins/PipeConfig/sample_broker_config.netconf eccd\@$ip:/home/eccd/DT/sample_broker_config,user=expert.netconf";
                }
            }
            if (! -f "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/eccd_installation_files/DT/sample_bsf_func_and_diameter,user=bsf-admin.netconf") {
                # The directory structure does not seem to contain the file, check if it exist in an alternative path
                if (-f "$::JOB_PARAMS{'_PACKAGE_DIR'}/../Jenkins/PipeConfig/sample_bsf_func_and_diameter.netconf") {
                    push @commands, "${debug_command}scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR -pr $::JOB_PARAMS{'_PACKAGE_DIR'}/../Jenkins/PipeConfig/sample_bsf_func_and_diameter.netconf eccd\@$ip:/home/eccd/DT/sample_bsf_func_and_diameter,user=bsf-admin.netconf";
                }
            }
            if (! -f "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/eccd_installation_files/DT/sample_config_scp,user=scp-admin.netconf") {
                # The directory structure does not seem to contain the file, check if it exist in an alternative path
                if (-f "$::JOB_PARAMS{'_PACKAGE_DIR'}/../Jenkins/PipeConfig/sample_config_scp.netconf") {
                    push @commands, "${debug_command}scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR -pr $::JOB_PARAMS{'_PACKAGE_DIR'}/../Jenkins/PipeConfig/sample_config_scp.netconf eccd\@$ip:/home/eccd/DT/sample_config_scp,user=scp-admin.netconf";
                }
            }
            if (! -f "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/eccd_installation_files/DT/sample_sepp_poc_config,user=sepp-admin.netconf") {
                # The directory structure does not seem to contain the file, check if it exist in an alternative path
                if (-f "$::JOB_PARAMS{'_PACKAGE_DIR'}/../Jenkins/PipeConfig/sample_sepp_poc_config.netconf") {
                    push @commands, "${debug_command}scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR -pr $::JOB_PARAMS{'_PACKAGE_DIR'}/../Jenkins/PipeConfig/sample_sepp_poc_config.netconf eccd\@$ip:/home/eccd/DT/sample_sepp_poc_config,user=sepp-admin.netconf";
                }
            }
            #
            # tools/ files
            #
            if (! -d "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/eccd_installation_files/tools/expect_tcl_files") {
                # The directory structure does not seem to contain the directory, check if it exist in an alternative path
                if (-d "/proj/DSC/rebels/expect_tcl_files") {
                    push @commands, "${debug_command}scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR -pr /proj/DSC/rebels/expect_tcl_files eccd\@$ip:/home/eccd/tools/";
                }
            }
            if (! -d "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/eccd_installation_files/tools/gnuplot_files") {
                # The directory structure does not seem to contain the directory, check if it exist in an alternative path
                if (-d "/proj/DSC/rebels/gnuplot_files") {
                    push @commands, "${debug_command}scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR -pr /proj/DSC/rebels/gnuplot_files eccd\@$ip:/home/eccd/tools/";
                }
            }
        }

        General::Logging::log_user_message("Copy needed files to director or control plane nodes");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Copying files to director or control plane nodes was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            push @::JOB_STATUS, '(x) Copying files to director or control plane nodes failed';
            General::Logging::log_user_error_message("Failed to copy files to director or control plane nodes");
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
    sub Copy_DAFT_Files_To_Nodes_P802S04T07 {

        my @commands = ();
        my $current_directory = `pwd`;
        $current_directory =~ s/[\r\n]//g;
        my $filename = sprintf "DAFT_temp_package_%d_%d.tar.gz", time(), $$;
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my $rc = 0;
        my @result;

        # The tar command needs to be run from the current directory so change to it
        chdir "$::JOB_PARAMS{'_PACKAGE_DIR'}";

        push @commands, "tar cvzf $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/configurationfiles/$filename bin expect perl playlist_extensions templates";
        for my $ip (@ip_addresses) {
            # Create DAFT package into workspace directory and then upload it to the node and unpack it there
            push @commands, "${debug_command}scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR -pr $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/configurationfiles/$filename eccd\@$ip:/home/eccd/";
            push @commands, "${debug_command}ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@$ip tar xf /home/eccd/$filename -C /home/eccd/daft/";
            push @commands, "${debug_command}ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@$ip rm -f /home/eccd/$filename";
        }

        General::Logging::log_user_message("Copy DAFT files to director or control plane nodes");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Copying of DAFT files director or control plane nodes was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            push @::JOB_STATUS, '(x) Copying of DAFT files director or control plane nodes failed';
            General::Logging::log_user_error_message("Failed to copy DAFT files to director or control plane nodes");
        }

        # Go back to the previous current directory
        chdir "$current_directory";

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
    sub Update_Hosts_File_On_Nodes_P802S04T08 {

        my @commands = ();
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my $rc = 0;
        my @result;

        for my $ip (@ip_addresses) {
            push @commands, "${debug_command}ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@$ip /home/eccd/daft/bin/add_k8s_registry_to_hosts.bash";
        }

        # We only update the file on director 0 since we have not uploaded DAFT to director 1
        General::Logging::log_user_message("Update /etc/hosts file on directors with k8s-registry.eccd.local address");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Update of /etc/hosts file on directors was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            push @::JOB_STATUS, '(x) Update of /etc/hosts file on directors failed';
            General::Logging::log_user_error_message("Failed to update /etc/hosts file on directors");
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
    sub Create_Local_Docker_Registry_Secret_P802S04T09 {

        my $container_command = "sudo -n docker";
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my $registry_password = $::JOB_PARAMS{'LOCAL_REGISTRY_KEY'};
        my $rc = 0;
        my @result;

        # NOTE: Not really needed according to Dong when running from inside the cluster, but it does not harm to do it.
        # We only need to do it on one of the nodes.

        General::Logging::log_user_message("Getting ECCD Version");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$ip_addresses[0] " .
                                        "--user=eccd " .
                                        "--password='notneeded' " .
                                        "--timeout=$expect_timeout " .
                                        "--command='cat /etc/eccd/eccd_image_version.ini'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            my $line = join("\n",@result);
            if ($line =~ /^IMAGE_RELEASE=(\S+)$/ms) {
                $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} = $1;
                if (version->parse($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}) >= version->parse("2.26.0")) {
                    # The 'docker' command was removed in 2.26.0 release of ECCD so we use 'nerdctl' instead.
                    $container_command = "sudo -n nerdctl";
                }
            }
            General::Logging::log_user_message($line);
        } else {
            # Failed to get the ECCD version, just ignore this error
            $rc = 0;
        }

        General::Logging::log_user_message("Create Local Docker Registry secret");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$ip_addresses[0] " .
                                        "--user=eccd " .
                                        "--password='notneeded' " .
                                        "--timeout=$expect_timeout " .
                                        "--command='kubectl -n kube-system create secret docker-registry k8s-registry-secret --docker-server k8s-registry.eccd.local --docker-username admin --docker-password $registry_password' " .
                                        "--command='$container_command login --username admin --password $registry_password k8s-registry.eccd.local:\$(kubectl get --namespace ingress-nginx -o jsonpath=\"{.spec.ports[1].nodePort}\" services ingress-nginx)' ",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Creation of docker registry secret was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to create docker registry secret");
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
    sub Check_BFD_Sessions_P802S04T10 {

        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my $rc = 0;
        my @result;

        # We only check the status on one node since it should be the same result on all of them.
        General::Logging::log_user_message("Checking BFD and BGP protocol status");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@$ip_addresses[0] /home/eccd/daft/perl/bin/system_health_check.pl -c ecfe_network_status --progress_type=none --no_color --no_logfile",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Checking BFD and BGP protocol status was successful';
        } else {
            # Display the result in case of error
            push @::JOB_STATUS, '(x) Checking BFD and BGP protocol status failed';
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check the BFD and BGP protocol status");
            $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} .= "The BFD and BGP protocol status check failed in task Check_BFD_Sessions_P802S04T10.\n";
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
    sub CMYP_Workaround_P802S04T11 {

        my @commands = ();
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my $vip_oam = $::JOB_PARAMS{'VIP_OAM'};
        my $worker_ip = "";

        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Apply CMYP Workaround");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$ip_addresses[0] " .
                                        "--user=eccd " .
                                        "--password='notneeded' " .
                                        "--timeout=$expect_timeout " .
                                        "--command='kubectl get nodes --no-headers -o wide --selector=node-role.kubernetes.io/worker' ",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0 && @result != 0) {
            foreach ( @result ) {
                if ( $_ =~ m/^worker-\S+\s+\S+\s+\S+\s+\S+\s+\S+\s+(\S+)\s+.*$/ ) {
                    $worker_ip = $1;
                    last;
                }
            }
            push @::JOB_STATUS, '(/) Fetching worker IP address was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to fetch worker IP address");
            return $rc;
        }

        if ( $rc == 0 && $worker_ip != "" ) {
            for my $ip (@ip_addresses) {
                push @commands, "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp --ip=$ip --user=eccd --password='notneeded' --timeout=$expect_timeout --command='sudo -n ip r a $vip_oam via $worker_ip dev eth0' --command='sudo -n ip r a 10.96.0.0/12 via $worker_ip dev eth0'";
            }
            $rc = General::OS_Operations::send_command(
                {
                    "commands"      => \@commands,
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
        }

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Application of CMYP workaround was successful';
        } else {
            # Display the result in case of error
            push @::JOB_STATUS, '(x) Application of CMYP workaround failed';
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to apply CMYP workaround");
            $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} .= "Failed to apply CMYP workaround in task CMYP_Workaround_P802S04T11.\n";
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
    sub Install_Expect_P802S04T12 {

        my @commands = ();
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my $rc = 0;
        my @result;

        for my $ip (@ip_addresses) {
            push @commands, "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp --ip=$ip --user=eccd --password='notneeded' --timeout=$expect_timeout --command='sudo -n /home/eccd/tools/expect_tcl_files/install_expect.bash'";
        }

        General::Logging::log_user_message("Install expect");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Expect installation was successful';
        } else {
            # Display the result in case of error
            push @::JOB_STATUS, '(x) Expect installation failed';
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to install expect");
            $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} .= "Failed to install expect in task Install_Expect_P802S04T12.\n";
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
    sub Install_Gnuplot_P802S04T13 {

        my @commands = ();
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my $rc = 0;
        my @result;

        for my $ip (@ip_addresses) {
            push @commands, "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp --ip=$ip --user=eccd --password='notneeded' --timeout=$expect_timeout --command='sudo -n /home/eccd/tools/gnuplot_files/install_gnuplot.bash'";
        }

        General::Logging::log_user_message("Install gnuplot");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Gnuplot installation was successful';
        } else {
            # Display the result in case of error
            push @::JOB_STATUS, '(x) Gnuplot installation failed';
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to install gnuplot");
            $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} .= "Failed to install gnuplot in task Install_Gnuplot_P802S04T13.\n";
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
    sub Update_bashrc_File_P802S04T14 {

        my @commands = ();
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my $rc = 0;

        for my $ip (@ip_addresses) {
            push @commands, "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp --ip=$ip --user=eccd --password='notneeded' --timeout=$expect_timeout --command='cp -p /home/eccd/.bashrc /home/eccd/.bashrc_ORIG' --command='cat /home/eccd/bashrc_Template > /home/eccd/.bashrc' --command='rm -f /home/eccd/bashrc_Template'";
        }

        General::Logging::log_user_message("Updating .bashrc file on Cluster");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Update of .bashrc file was successful';
        } else {
            # Display the result in case of error
            push @::JOB_STATUS, '(x) Update of .bashrc file failed';
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to update .bashrc file");
            $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} .= "Failed to update .bashrc file in task Update_bashrc_File_P802S04T14.\n";
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
    sub Check_And_Fix_Ingress_Nginx_Service_P802S04T15 {

        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my $pending = 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Checking ingress-nginx service");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$ip_addresses[0] " .
                                        "--user=eccd " .
                                        "--password='notneeded' " .
                                        "--timeout=$expect_timeout " .
                                        "--command='kubectl get svc ingress-nginx -n ingress-nginx --no-headers'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to read ingress-nginx service information from Cluster");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # For successful case:
        #ingress-nginx   LoadBalancer   10.105.153.40   10.221.168.104   80:31121/TCP,443:30478/TCP   11y
        #
        # For failure case:
        #ingress-nginx   LoadBalancer   10.105.153.40   <pending>        80:31121/TCP,443:30478/TCP   11y
        for (@result) {
            if (/^\s*ingress-nginx\s+LoadBalancer\s+\S+\s+<pending>\s+\d+:\d+\/TCP,\d+:\d+\/TCP\s+\S+\s*$/) {
                $pending = 1;
            }
        }
        if ($pending == 0) {
            # We most likely found a valid IP address and we just continue with next task
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Changing ingress-nginx service data on Cluster");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$ip_addresses[0] " .
                                        "--user=eccd " .
                                        "--password='notneeded' " .
                                        "--timeout=$expect_timeout " .
                                        "--stop-on-error " .
                                        "--command='kubectl get svc ingress-nginx -n ingress-nginx -o yaml' " .
                                        "--command='kubectl get svc ingress-nginx -n ingress-nginx -o yaml > /home/eccd/change_ingress-nginx_service.yaml' " .
                                        "--command='sed -i \"s/ipFamilyPolicy: PreferDualStack/ipFamilyPolicy: SingleStack/\" /home/eccd/change_ingress-nginx_service.yaml' " .
                                        "--command='sed -i \"/- IPv6/d\" /home/eccd/change_ingress-nginx_service.yaml' " .
                                        "--command='kubectl apply -f /home/eccd/change_ingress-nginx_service.yaml'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to change ingress-nginx service data on Cluster");
            return 1;
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Checking ingress-nginx service again");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$ip_addresses[0] " .
                                        "--user=eccd " .
                                        "--password='notneeded' " .
                                        "--timeout=$expect_timeout " .
                                        "--command='kubectl get svc ingress-nginx -n ingress-nginx --no-headers'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        # We don't care what the result is because if it didn't change and it
        # still says <pending> then the next task of checking the kube config
        # file will anyway fail.
        # So we just return back success

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
    sub Create_kubeconfig_File_P802S04T16 {

        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my @kubeconfig = ();
        my $kubeconfig_file = "";
        my $rc = 0;
        my $reading_cat = 0;
        my $reading_kubectl = 0;
        my @result;
        my $server_found = 0;
        my $server_url = "";

        General::Logging::log_user_message("Reading kube config file and server URL from Cluster");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$ip_addresses[0] " .
                                        "--user=eccd " .
                                        "--password='notneeded' " .
                                        "--timeout=$expect_timeout " .
                                        "--command='cat /home/eccd/.kube/config' " .
                                        "--command='kubectl get ing -n kube-system kubernetes-api --no-headers'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to read kube config file and server URL from Cluster");
            push @::JOB_STATUS, '(x) Creation of kube config file failed';
            $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} .= "Failed to read kube config file and server URL from Cluster in task Create_kubeconfig_File_P802S04T15.\n";
            # Skip remaining tasks in this step.
            return General::Playlist_Operations::RC_STEPOUT;
        }

        for (@result) {
            if (/cat \/home\/eccd\/\.kube\/config\s*$/) {
                $reading_cat = 1;
                $reading_kubectl = 0;
            #} elsif (/kubectl get ing -n kube-system kubernetes-api --no-headers\s*$/) {
            #  For whatever reason when executing the command on Eevee from Jenkins
            #  then the printout always contained ---no-headers even though the command sent
            #  contained only --no-headers, so the "fix" is to not check for the last parameter.
            #  Another problem seen when connecting to Pikachu from Jenkins this time it added
            #  an extra 's' to the command output, so instead of the sent command containing
            #  'kubernetes-api' we got in the printout 'kubernetess-api' causing the check
            #  for the command to fail.
            #  The problem must have something to do with the terminal used from Jenkins
            #  where after 80 characters it duplicates the character, so as a work around
            #  I will just check for part of the command, hopefully resulting in better
            #  detection until again we have a prompt that together with the command
            #  will again be > 80 characters.
            } elsif (/kubectl get ing -n kube-system.+/) {
                $reading_kubectl = 1;
                $reading_cat = 0;
            } elsif (/echo \$\?\s*$/) {
                $reading_cat = 0;
                $reading_kubectl = 0;
            } elsif ($reading_cat == 1) {
                push @kubeconfig, $_;
                $server_found = 1 if (/^\s+server:\s*\S+\s*$/);
            } elsif ($reading_kubectl == 1 && /^kubernetes-api\s+\S+\s+(\S+)\s+\S+\s+.+/) {
                #kubernetes-api   <none>   api.snorlax.n60vpod1.sc.sero.gic.ericsson.se   10.10.10.11,10.10.10.83   80, 443   231d
                #kubernetes-api   <none>   api.eccd.local,n106-vpod6-kubeapi.sc.seli.gic.ericsson.se   10.0.60.101,10.0.60.102,10.0.60.103   80, 443   59d
                for (split /,/, $1) {
                    next if (/^api\.eccd\.local/);
                    $server_url = "https://$_";
                }
            }
        }

        # Check that we seem to have received valid data
        if ($server_found == 0) {
            General::Logging::log_user_error_message("Failed to find a line containing 'server: ' in kube config file data");
            push @::JOB_STATUS, '(x) Creation of kube config file failed';
            $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} .= "Failed to find a line containing 'server: ' in kube config file data in task Create_kubeconfig_File_P802S04T15.\n";
            # Skip remaining tasks in this step.
            return General::Playlist_Operations::RC_STEPOUT;
        }
        if (scalar @kubeconfig < 10) {
            General::Logging::log_user_error_message("The kube config does not seem to contain enough lines");
            push @::JOB_STATUS, '(x) Creation of kube config file failed';
            $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} .= "The kube config does not seem to contain enough lines in task Create_kubeconfig_File_P802S04T15.\n";
            # Skip remaining tasks in this step.
            return General::Playlist_Operations::RC_STEPOUT;
        }
        if ($server_url eq "") {
            General::Logging::log_user_error_message("Did not find the server URL");
            push @::JOB_STATUS, '(x) Creation of kube config file failed';
            $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} .= "Did not find the server URL in task Create_kubeconfig_File_P802S04T15.\n";
            # Skip remaining tasks in this step.
            return General::Playlist_Operations::RC_STEPOUT;
        }

        # Now replace the 'server" in the kube config data
        for (@kubeconfig) {
            if (/^(\s+server:\s+)\S+/) {
                $_ = "$1$server_url";
                # No need to continue looking
                last;
            }
        }
        if (exists $::JOB_PARAMS{'eric_sc_values_hash_parameter_eric-pm-bulk-reporter.env.nodeName'} && $::JOB_PARAMS{'eric_sc_values_hash_parameter_eric-pm-bulk-reporter.env.nodeName'} ne "CHANGEME") {
            $kubeconfig_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/$::JOB_PARAMS{'eric_sc_values_hash_parameter_eric-pm-bulk-reporter.env.nodeName'}.config";
        } else {
            $kubeconfig_file = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/UNKNOWN_NODE_NAME.config";
        }

        General::Logging::log_user_message("Writing kube config file to workspace:\n$kubeconfig_file");
        $rc = General::File_Operations::write_file(
            {
                "filename"          => $kubeconfig_file,
                "append-file"       => 0,
                "output-ref"        => \@kubeconfig,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to write the kube config file to workspace");
            push @::JOB_STATUS, '(x) Creation of kube config file failed';
            $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} .= "Failed to write the kube config file to workspace in task Create_kubeconfig_File_P802S04T15.\n";
            # Skip remaining tasks in this step.
            return General::Playlist_Operations::RC_STEPOUT;
        }

        # Save the file path
        $::JOB_PARAMS{'NODE_KUBECONFIG_FILE'} = $kubeconfig_file;

        push @::JOB_STATUS, '(/) Creation of kube config file was successful';

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
    sub Check_kubeconfig_File_Towards_Cluster_P802S04T17 {

        my $kubeconfig_file = $::JOB_PARAMS{'NODE_KUBECONFIG_FILE'};
        my $rc = 0;

        General::Logging::log_user_message("Check if kube config file works towards the Cluster");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "kubectl get nodes --request-timeout=30s --kubeconfig $kubeconfig_file",
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Connection to Cluster with kube config file was successful';
            $::JOB_PARAMS{'NODE_KUBECONFIG_FILE_WORKS'} = "yes";
        } else {
            # Display the result in case of error
            push @::JOB_STATUS, '(x) Connection to Cluster with kube config file failed';
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_warning_message("Failed to connect to cluster with kube config file");

            # We ignore this error since it could be caused by backbone connectivity issues
            # so user needs to manually investigate.
            $::JOB_PARAMS{'NODE_KUBECONFIG_FILE_WORKS'} = "no";
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
    sub Update_kubeconfig_File_On_Build_Slaves_P802S04T18 {

        my $build_slave_info_file = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/kubeconfig_files/build_slave_information.txt";
        my @build_slave_info;
        my $command;
        my $failure_cnt = 0;
        my $file;
        my $host;
        my $kubeconfig_file = $::JOB_PARAMS{'NODE_KUBECONFIG_FILE'};
        my $rc = 0;
        my $user;

        # Check if the kube config file worked and that is has a node specific file name
        if ($::JOB_PARAMS{'NODE_KUBECONFIG_FILE_WORKS'} eq "no") {
            General::Logging::log_user_warning_message("The kube config file does not work, so skipping this task.\nManually check the file following file:\n$kubeconfig_file");
            push @::JOB_STATUS, '(x) Update of kube config file on build slaves not done because the file does not work';
            return General::Playlist_Operations::RC_TASKOUT;
        }
        if ($kubeconfig_file =~ /UNKNOWN_NODE_NAME\.config$/) {
            General::Logging::log_user_warning_message("The kube config file has a generic name that is not unique, so skipping this task.\nManually rename and copy the following file to all build slaves:\n$kubeconfig_file");
            push @::JOB_STATUS, '(x) Update of kube config file on build slaves not done because the file does not have a node specific name';
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Check if we have information about the different build slaves to copy the kube config file to
        unless (-f $build_slave_info_file) {
            General::Logging::log_user_warning_message("No file with information about which build slaves to copy the kube config file to exists in this path:\n$build_slave_info_file\nSo skipping this task.\nManually copy the following file to all build slaves:\n$kubeconfig_file");
            push @::JOB_STATUS, '(x) Update of kube config file on build slaves not done because no info exist about build slaves';
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Reading file with build slave information");
        $rc = General::File_Operations::read_file(
            {
                "filename"              => $build_slave_info_file,
                "output-ref"            => \@build_slave_info,
                "hide-error-messages"   => 1,
                "ignore-empty-lines"    => 1,
                "ignore-pattern"        => '^\s*#',
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_warning_message("Unable to open the file '$build_slave_info_file'.\nSo skipping this task.\nManually copy the following file to all build slaves:\n$kubeconfig_file");
            push @::JOB_STATUS, '(x) Update of kube config file on build slaves not done because no info exist about build slaves';
            return General::Playlist_Operations::RC_TASKOUT;
        } elsif (scalar @build_slave_info == 0) {
            General::Logging::log_user_warning_message("No build slave information in the file '$build_slave_info_file'.\nSo skipping this task.\nManually copy the following file to all build slaves:\n$kubeconfig_file");
            push @::JOB_STATUS, '(x) Update of kube config file on build slaves not done because no info exist about build slaves';
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Updating kube config file on build slaves:");
        for (@build_slave_info) {
            if (/^\s*(\S+)\s+(\S+)\s+(\S+)\s*$/) {
                $host = $1;
                $user = $2;
                $file = $3;
            }
            if ($host eq "" || $user eq "" || $file eq "") {
                $failure_cnt++;
                General::Logging::log_user_message("  - Not valid build slave information: $_");
                next;
            }
            if ($file !~ /\//) {
                # The file contains no path, so it's relative to the users home directory
                $file = "$ENV{'HOME'}/.ssh/$file";
            } elsif ($file !~ /^\/.+/) {
                $failure_cnt++;
                General::Logging::log_user_message("  - Private ssh key file is not an absolute file path (not starting with a /): $file");
                next;
            }
            if (-f $file) {
                $command = "scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR -i $file";
            } else {
                $command = "scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR";
            }

            General::Logging::log_user_message("  - Copying kube config file to build slave: $host");

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$command -p $kubeconfig_file $user\@$host:/home/$user/.kube/",
                    "hide-output"   => 1,
                }
            );

            if ($rc != 0) {
                $failure_cnt++;
                General::Logging::log_user_message("    Failed to copy kube config file to build slave, see all.log for details");
                next;
            }
        }

        if ($failure_cnt == 0) {
            push @::JOB_STATUS, '(/) Update of kube config file on build slaves was successful';
        } else {
            General::Logging::log_user_warning_message("Failure to copy kube config file to one or more build slaves, see details above.\nManually copy the following file to all build slaves where the copy failed:\n$kubeconfig_file");
            push @::JOB_STATUS, '(x) Update of kube config file on build slaves not completely done because copy failed to some build slaves';
            $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} .= "Failed to update kube config file on one or more build slaves in task Update_kubeconfig_File_On_Build_Slaves_P802S04T17.\n";
        }

        # Always return success
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
    sub Check_Worker_Address_Against_Network_Config_File_P802S04T19 {

        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my $found = 0;
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my @messages = ();
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Get IP-address for first worker node");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$ip_addresses[0] " .
                                        "--user=eccd " .
                                        "--password='notneeded' " .
                                        "--timeout=$expect_timeout " .
                                        "--command=\"kubectl get nodes --no-headers -o custom-columns=NAME:.metadata.name --selector='node-role.kubernetes.io/worker' -o jsonpath='{.items[0].status.addresses[0].address}{\\\"\\n\\\"}'\"",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get IP-address for first worker node");
            return 1;
        }
        $found = 0;
        $::JOB_PARAMS{'FIRST_WORKER_IP_ADDRESS'} = "";
        for (@result) {
            if ($found && /^(\S+)\s*$/) {
                $::JOB_PARAMS{'FIRST_WORKER_IP_ADDRESS'} = $1;
                last;
            } elsif (/kubectl get nodes --no-headers.+/) {
                # We have found the start of the command
                $found = 1;
            }
        }
        if ($::JOB_PARAMS{'FIRST_WORKER_IP_ADDRESS'} eq "") {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to find IP-address for first worker node in printout");
            return 1;
        }

        General::Logging::log_user_message("Get IP-address for eth2 interface from first worker node");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$ip_addresses[0] " .
                                        "--user=eccd " .
                                        "--password='notneeded' " .
                                        "--timeout=$expect_timeout " .
                                        "--command=\"ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@$::JOB_PARAMS{'FIRST_WORKER_IP_ADDRESS'} ip a show dev eth2\"",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get IP_address for first worker node");
            return 1;
        }

        #4: eth2: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP group default qlen 1000
        #    link/ether fa:16:3e:49:04:dd brd ff:ff:ff:ff:ff:ff
        #    altname enp0s5
        #    altname ens5
        #    inet 10.221.169.157/28 brd 10.221.169.159 scope global eth2
        #    valid_lft forever preferred_lft forever
        $::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv4_ADDRESS'} = "";
        $::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv6_ADDRESS'} = "";
        for (@result) {
            if (/^\s+inet (\d+\.\d+\.\d+\.\d+)\/\d+\s+.+/) {
                $::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv4_ADDRESS'} = $1;
            }
            if (/^\s+inet6 (\S+)\/\d+\s+.+/) {
                my $ipv6 = $1;
                if ($ipv6 !~ /^fe80::/i) {
                    $::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv6_ADDRESS'} = $ipv6;
                }
            }
        }

        General::Logging::log_user_message( sprintf "%s=%s\n%s=%s\n",
            "FIRST_WORKER_ETH2_IPv4_ADDRESS", $::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv4_ADDRESS'},
            "FIRST_WORKER_ETH2_IPv6_ADDRESS", $::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv6_ADDRESS'}
        );

        if ($::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv4_ADDRESS'} eq "" && $::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv6_ADDRESS'} eq "") {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get IP_address for eth2 interface from first worker node");
            return 1;
        }

        if ($::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv4_ADDRESS'} ne "" && $::JOB_PARAMS{'worker_ip_ipv4'} ne $::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv4_ADDRESS'}) {
            push @messages, "";
            push @messages, "The parameter to update is called \"worker_ip_ipv4\" and the value should be set to \"$::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv4_ADDRESS'}\".";
            if ($::JOB_PARAMS{'worker_ip_ds'} ne "CHANGEME") {
                push @messages, "";
                push @messages, "The parameter to update is called \"worker_ip_ds\" and the value should contain \"$::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv4_ADDRESS'}\".";
            }

            if ($::JOB_PARAMS{'worker_ip_ipv4'} eq $::JOB_PARAMS{'worker_ip_ipv4_tools'}) {
                push @messages, "";
                push @messages, "The parameter to update is called \"worker_ip_ipv4_tools\" and the value should be set to \"$::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv4_ADDRESS'}\".";
                if ($::JOB_PARAMS{'worker_ip_ds_tools'} ne "CHANGEME") {
                    push @messages, "";
                    push @messages, "The parameter to update is called \"worker_ip_ds_tools\" and the value should contain \"$::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv4_ADDRESS'}\".";
                }
            }
        }

        if ($::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv6_ADDRESS'} ne "" && $::JOB_PARAMS{'worker_ip_ipv6'} ne $::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv6_ADDRESS'}) {
            push @messages, "";
            push @messages, "The parameter to update is called \"worker_ip_ipv6\" and the value should be set to \"$::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv6_ADDRESS'}\".";
            if ($::JOB_PARAMS{'worker_ip_ds'} ne "CHANGEME") {
                push @messages, "";
                push @messages, "The parameter to update is called \"worker_ip_ds\" and the value should contain \"$::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv6_ADDRESS'}\".";
            }

            if ($::JOB_PARAMS{'worker_ip_ipv6'} eq $::JOB_PARAMS{'worker_ip_ipv6_tools'}) {
                push @messages, "";
                push @messages, "The parameter to update is called \"worker_ip_ipv6_tools\" and the value should be set to \"$::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv6_ADDRESS'}\".";
                if ($::JOB_PARAMS{'worker_ip_ds_tools'} ne "CHANGEME") {
                    push @messages, "";
                    push @messages, "The parameter to update is called \"worker_ip_ds_tools\" and the value should contain \"$::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv6_ADDRESS'}\".";
                }
            }
        }

        if (@messages) {
            unshift @messages, ("*************","* IMPORTANT *",  "*************", "", "Worker IP-Address", "=================", "", "Make sure that you update the network configuration file(s) inside the GIT repository with the new worker IPv4-address and IPv6-address.");
            push @messages, "";
            # Write extra status messages to file
            $rc = General::File_Operations::write_file(
                {
                    "filename"          => "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/extra_status_messages.txt",
                    "append-file"       => 1,
                    "output-ref"        => \@messages,
                }
            );
        } else {
            General::Logging::log_user_message("The eth2 IPv4/IPv6-address read from the first worker node already match the IPv4/IPv6-Address in the network config file, so no need to update the 'worker_ip_ipv4'/'worker_ip_ipv6' parameters");
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
    sub Add_Missing_Route_For_Worker_Eth2_Traffic_P802S04T20 {

        my @commands = ();
        my $eth2_ip_address = $::JOB_PARAMS{'FIRST_WORKER_ETH2_IPv4_ADDRESS'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my $rc = 0;
        my $worker_ip = $::JOB_PARAMS{'FIRST_WORKER_IP_ADDRESS'};

        for my $ip (@ip_addresses) {
            push @commands, "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp --ip=$ip --user=eccd --password='notneeded' --timeout=$expect_timeout --command='sudo ip r add $eth2_ip_address/32 via $worker_ip dev eth0'";
        }

        General::Logging::log_user_message("Updating routing table towards eth2 of first worker on nodes");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Adding missing route for worker eth2 was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            push @::JOB_STATUS, '(x) Adding missing route for worker eth2 failed';
            General::Logging::log_user_error_message("Failed to update routing table");
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
    sub Add_Missing_Route_For_External_kubeconfig_Access_P802S04T21 {

        my @commands = ();
        my $kubeconfig_server_ip_address = "";
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my @ip_addresses = split /,/, $::JOB_PARAMS{'DIRECTOR_OR_CONTROLPLANE_IPS'};
        my $rc = 0;
        my $worker_ip = $::JOB_PARAMS{'FIRST_WORKER_IP_ADDRESS'};

        for my $ip (@ip_addresses) {
            if (exists $::JOB_PARAMS{'eric_sc_values_hash_parameter_eric-pm-bulk-reporter.env.nodeName'} && $::JOB_PARAMS{'eric_sc_values_hash_parameter_eric-pm-bulk-reporter.env.nodeName'} ne "") {
                if ($::JOB_PARAMS{'eric_sc_values_hash_parameter_eric-pm-bulk-reporter.env.nodeName'} =~ /^Snorlax/i) {
                    # This is the IP-address network range for the CNOM fqdn
                    # $ nslookup nbi.snorlax.sc.sero.gic.ericsson.se
                    # Server:		127.0.0.53
                    # Address:	127.0.0.53#53
                    #
                    # Name:	nbi.snorlax.sc.sero.gic.ericsson.se
                    # Address: 214.13.236.227
                    push @commands, "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp --ip=$ip --user=eccd --password='notneeded' --timeout=$expect_timeout --command='sudo ip r add 214.13.236.224/28 via $worker_ip dev eth0'";
                }
            }
        }
        if (scalar @commands == 0) {
            General::Logging::log_user_message("Updating routing table towards eth0 of first worker on nodes not needed");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Updating routing table towards eth0 of first worker on nodes");
        $rc = General::OS_Operations::send_command(
            {
                "commands"      => \@commands,
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Adding missing route for worker eth0 was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            push @::JOB_STATUS, '(x) Adding missing route for worker eth0 failed';
            General::Logging::log_user_error_message("Failed to update routing table");
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
sub Cleanup_Job_Environment_P802S05 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::902_Cleanup_Job_Environment::main } );
    return $rc if $rc < 0;

    if ($rc == 0) {
        # Mark that the Job was successful
        $::JOB_PARAMS{'JOBSTATUS'} = "SUCCESSFUL";
    }

    if (exists $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} && $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} ne "") {
        General::Logging::log_user_error_message($::JOB_PARAMS{'JOB_FAILED_MESSAGE'});
        # Mark that the Job as failed
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
    }

    return $rc;
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P802S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::902_Cleanup_Job_Environment::main } );
    return $rc if $rc < 0;

    if (exists $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} && $::JOB_PARAMS{'JOB_FAILED_MESSAGE'} ne "") {
        General::Logging::log_user_error_message($::JOB_PARAMS{'JOB_FAILED_MESSAGE'});
    }

    return 0;
}

#####################
#                   #
# Other Subroutines #
#                   #
#####################

# -----------------------------------------------------------------------------
sub set_playlist_variables {
    %playlist_variables = (
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
Deployment will take place.
Any command that will just change files or data on the job workspace directory
or any command that just reads data from the cluster will still be executed.
This parameter can be useful to check if the overall logic is working without
doing any changes to the running system.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'ECCD_ENVIRONMENT_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the path to the environment file located on the OpenStack
server to be used for installing the ECCD software on the node.
It can e.g. be something like:
/home/SC-admin/eccd/eevee_2.21.0_DS/env_eccd_eevee_2.21.0.yaml
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'ECCD_STACK_NAME' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the stack name to use for deleting and installing ECCD
on the node.
See also ECCD_STACK_NAME_TO_BE_DELETED below if the stack names a to be deleted
and installed are different.
It can e.g. be something like:
eccd-eevee-ds
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'ECCD_STACK_NAME_TO_BE_DELETED' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the stack name to use for deleting ECCD on the node.
If this is the same as the name to be used for ECCD installation as specified
by ECCD_STACK_NAME above then this parameter does not need to be specified.
It can e.g. be something like:
eccd-eevee-ipv4
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'ECCD_STACK_TYPE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "capo",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the stack type to use for deleting and installing ECCD
on the node.
This parameter also controls which other parameters are used for the installation.

    capo
    ----
    - ECCD_STACK_NAME is mandatory.

    - ECCD_ENVIRONMENT_FILE is mandatory.

    - ECCD_TEMPLATE_FILE not needed.

    - OPENSTACK_HOST_ADDRESS if not specified will take it's value from the
      network configuration file parameter 'eccd_capo_lcm_host_address'.

    - OPENSTACK_PASSWORD if not specified will take it's value from the
      network configuration file parameter 'eccd_capo_lcm_password'.

    - OPENSTACK_TIMEOUT not needed.

    - OPENSTACK_USER if not specified will take it's value from the
      network configuration file parameter 'eccd_capo_lcm_user'.

    non-capo
    --------
    - ECCD_STACK_NAME is mandatory.

    - ECCD_ENVIRONMENT_FILE is mandatory.

    - ECCD_TEMPLATE_FILE is mandatory.

    - OPENSTACK_HOST_ADDRESS if not specified will take it's value from the
      network configuration file parameter 'eccd_openstack_host_address'.

    - OPENSTACK_PASSWORD if not specified will take it's value from the
      network configuration file parameter 'eccd_openstack_password'.

    - OPENSTACK_TIMEOUT if not specified then a default value of 90 minutes
      will be used.

    - OPENSTACK_USER if not specified will take it's value from the
      network configuration file parameter 'eccd_openstack_user'.
EOF
            'validity_mask' => '(capo|non-capo)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'ECCD_TEMPLATE_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the path to the template file located on the OpenStack
server to be used for installing the ECCD software on the node.
It can e.g. be something like:
/sharedimages/eccd/CXP9036305R29A/CXP9036305-2.21.0-1011-e3333973/templates/eccd.yaml
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'EXPECT_DEFAULT_TIMEOUT' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "7200",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls the default timeout of the commands executed by the
send_command_to_ssh.exp script used by some playlists to execute commands on
a remote node.
The value specifies how many seconds to wait before giving up on a command that
does not return a new prompt.
Some playlists can still use some other hard coded timeout value in which case
the value set by this parameter is not used, check each playlist which calls the
expect script to see what default value is being used.

By default the value is set to 2 hours (7200 seconds) which should be long
enough for most commands.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'OPENSTACK_HOST_ADDRESS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the host address for the OpenStack server where
installation of ECCD on the node will be executed from.
It can e.g. be something like:
10.221.146.29
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'OPENSTACK_PASSWORD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the user password for the OpenStack server where
installation of ECCD on the node will be executed from.
It can e.g. be something like:
admin
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'OPENSTACK_TIMEOUT' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",       # <yes|no>
            'type'          => "integer",  # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the timeout value in minutes to be used for the stack
creation.
Normally it should be enough to use a value of 70, but in recent ECCD (>2.17)
this timer needs to be larger e.g. 120.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'OPENSTACK_USER' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the user name for the OpenStack server where
installation of ECCD on the node will be executed from.
It can e.g. be something like:
SC-admin
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        'SKIP_STACK_DELETE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if stack deletion should be skipped (=yes) or not (=no).
By default any existing stack will be deleted.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        'SKIP_STACK_INSTALLATION' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if stack creation should be skipped (=yes) or not (=no).
By default any existing stack will be created.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'VIP_OAM' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the OAM VIP address to be used.
If no value is specified then the value will be taken from the network
configuration file parameter eric_sc_values_anchor_parameter_VIP_OAM.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },
    )
}
# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist Installs ECCD on a node.

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
