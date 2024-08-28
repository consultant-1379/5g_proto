package Playlist::937_Generate_New_ECCD_Front_Proxy_CA;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.4
#  Date     : 2024-05-15 10:09:34
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
    usage
    );

use ADP::Kubernetes_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

#
# Variable Declarations
#

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
    }

    $rc = General::Playlist_Operations::execute_step( \&Generate_New_Front_Proxy_CA_P937S01, \&Fallback001_P937S99 );
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
sub Generate_New_Front_Proxy_CA_P937S01 {

    my $rc = 0;

    # In case of a re-run of an interrupted job set ifnformation about which tasks to execute and which what time offset
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T01'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T01'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T01'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T02'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T02'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T02'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T03'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T03'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T03'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T04'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T04'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T04'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T05'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T05'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T05'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T06'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T06'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T06'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T07'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T07'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T07'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T08'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T08'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T08'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T09'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T09'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T09'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T10'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T10'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T10'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T11'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T11'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T11'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T12'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T12'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T12'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T13'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T13'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T13'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T14'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T14'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T14'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T15'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T15'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T15'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T16'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T16'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T16'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T17'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T17'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T17'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T18'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T18'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T18'} : 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T19'} = exists $::JOB_PARAMS{'P937_EXECUTE_P937S01T19'} ? $::JOB_PARAMS{'P937_EXECUTE_P937S01T19'} : 1;

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T01'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Old_Front_Proxy_CA_Certificate_On_Master_P937S01T01 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T01'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T02'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Old_Front_Proxy_CA_Key_On_Master_P937S01T02 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T02'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T03'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Generate_New_Front_Proxy_CA_Certificate_And_Key_On_Master0_P937S01T03 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T03'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T04'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_New_Front_Proxy_CA_Certificate_To_Home_Directoy_On_Master0_P937S01T04 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T04'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T05'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_New_Front_Proxy_CA_Key_To_Home_Directoy_On_Master0_P937S01T05 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T05'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T06'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Front_Proxy_CA_Certificate_To_Director0_P937S01T06 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T06'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T07'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Front_Proxy_CA_Key_To_Director0_P937S01T07 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T07'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T08'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_Certificate_Ownership_On_Director0_P937S01T08 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T08'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T09'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_Key_Ownership_On_Director0_P937S01T09 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T09'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T10'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_To_Home_Directory_P937S01T10 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T10'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T11'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Copy_Front_Proxy_CA_To_Master_Hosts_P937S01T11 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T11'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T12'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_To_CCD_Ansible_Directory_P937S01T12 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T12'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T13'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Replace_With_New_Front_Proxy_CA_Certificate_P937S01T13 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T13'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T14'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Replace_With_New_Front_Proxy_CA_Key_P937S01T14 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T14'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T15'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Rotate_Front_Proxy_Certs_P937S01T15 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T15'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T16'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Front_Proxy_CA_Certificate_Validity_Time_P937S01T16 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T16'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T17'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Restart_Metrics_Server_P937S01T17 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T17'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T18'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Cleanup_Remove_Front_Proxy_CA_Certificates_And_Keys_From_Masters_P937S01T18 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T18'} = 0;
    }

    if ($::JOB_PARAMS{'P937_EXECUTE_P937S01T19'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Cleanup_Remove_Front_Proxy_CA_Certificates_And_Keys_From_Director0_P937S01T19 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P937_EXECUTE_P937S01T19'} = 0;
    }

    # Step the counter and reset the executed tasks
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T01'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T02'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T03'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T04'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T05'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T06'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T07'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T08'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T09'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T10'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T11'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T12'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T13'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T14'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T15'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T16'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T17'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T18'} = 1;
    $::JOB_PARAMS{'P937_EXECUTE_P937S01T19'} = 1;

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
    sub Remove_Old_Front_Proxy_CA_Certificate_On_Master_P937S01T01 {

        my $command = sprintf '%ssudo ansible -a "sudo rm /etc/kubernetes/pki/front-proxy-ca.crt" master -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Remove Old Front Proxy CA Certificate On Master Nodes");
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
    sub Remove_Old_Front_Proxy_CA_Key_On_Master_P937S01T02 {

        my $command = sprintf '%ssudo ansible -a "sudo rm /etc/kubernetes/pki/front-proxy-ca.key" master -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Remove Old Front Proxy CA Key On Maste Nodesr");
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
    sub Generate_New_Front_Proxy_CA_Certificate_And_Key_On_Master0_P937S01T03 {

        my $command = sprintf '%ssudo ansible -a "sudo /usr/local/bin/kubeadm init phase certs front-proxy-ca --config /etc/kubernetes/kubeadm-config.yaml" master-0 -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Generate New Front Proxy CA Certificate And Key On Master-0");
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
    sub Copy_New_Front_Proxy_CA_Certificate_To_Home_Directoy_On_Master0_P937S01T04 {

        my $command = sprintf '%ssudo ansible -a " sudo install -C -m 640 -o eccd -g eccd /etc/kubernetes/pki/front-proxy-ca.crt front-proxy-ca.crt" master-0 -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Copy New Front Proxy CA Certificate To Home Directoy On Master-0");
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
    sub Copy_New_Front_Proxy_CA_Key_To_Home_Directoy_On_Master0_P937S01T05 {

        my $command = sprintf '%ssudo ansible -a "sudo install -C -m 600 -o eccd -g eccd /etc/kubernetes/pki/front-proxy-ca.key front-proxy-ca.key" master-0 -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Copy New Front Proxy CA Key To Home Directoy On Master-0");
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
    sub Copy_Front_Proxy_CA_Certificate_To_Director0_P937S01T06 {

        my $command = sprintf '%ssudo ansible -m "fetch src=/home/eccd/front-proxy-ca.crt dest=/home/eccd/ flat=yes" master-0 -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Copy Front Proxy CA Certificate To Director-0");
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
    sub Copy_Front_Proxy_CA_Key_To_Director0_P937S01T07 {

        my $command = sprintf '%ssudo ansible -m "fetch src=/home/eccd/front-proxy-ca.key dest=/home/eccd/ flat=yes" master-0 -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Copy Front Proxy CA Key To Director-0");
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
    sub Change_Certificate_Ownership_On_Director0_P937S01T08 {

        my $command = sprintf '%ssudo -n chown eccd:eccd /home/eccd/front-proxy-ca.crt', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Change Certificate Ownership On Director-0");
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
    sub Change_Key_Ownership_On_Director0_P937S01T09 {

        my $command = sprintf '%ssudo -n chown eccd:eccd /home/eccd/front-proxy-ca.key', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Change Key Ownership On Director-0");
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
    sub Change_To_Home_Directory_P937S01T10 {

        my $path = "/home/eccd";

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
    sub Copy_Front_Proxy_CA_To_Master_Hosts_P937S01T11 {

        my $command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $master_ip;
        my @master_ips = ADP::Kubernetes_Operations::get_node_ip( { "type" => "master" } );
        my $rc = 0;
        my @result;

        for $master_ip (@master_ips) {
            General::Logging::log_user_message("Copy front-proxy-ca.crt to master node on IP $master_ip");
            $command = sprintf '%srsync -e "ssh -o StrictHostKeyChecking=no" -azvh front-proxy-ca.crt %s:/home/eccd/front-proxy-ca.crt', $debug_command, $master_ip;
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

            General::Logging::log_user_message("Waiting 2 seconds for file rsync to finish");
            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 0,
                    "progress-message"  => 0,
                    "seconds"           => 2,
                    "use-logging"       => 1,
                }
            );

            General::Logging::log_user_message("Copy front-proxy-ca.key to master node on IP $master_ip");
            $command = sprintf '%srsync -e "ssh -o StrictHostKeyChecking=no" -azvh front-proxy-ca.key %s:/home/eccd/front-proxy-ca.key', $debug_command, $master_ip;
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

            General::Logging::log_user_message("Waiting 2 seconds for file rsync to finish");
            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 0,
                    "progress-message"  => 0,
                    "seconds"           => 2,
                    "use-logging"       => 1,
                }
            );
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
    sub Change_To_CCD_Ansible_Directory_P937S01T12 {

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
    sub Replace_With_New_Front_Proxy_CA_Certificate_P937S01T13 {

        my $command = sprintf '%ssudo ansible -a "sudo cp /home/eccd/front-proxy-ca.crt /etc/kubernetes/pki/" master -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Replace With New Front Proxy CA Certificate");
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
    sub Replace_With_New_Front_Proxy_CA_Key_P937S01T14 {

        my $command = sprintf '%ssudo ansible -a "sudo cp /home/eccd/front-proxy-ca.key /etc/kubernetes/pki/" master -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Replace With New Front Proxy CA Key");
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
    sub Rotate_Front_Proxy_Certs_P937S01T15 {

        my $command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        if (version->parse($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}) < version->parse("2.18.0")) {
            $command = sprintf '%ssudo ansible -a "sudo /usr/local/bin/kubeadm alpha certs renew front-proxy-client" master -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        } else {
            $command = sprintf '%ssudo ansible -a "sudo /usr/local/bin/kubeadm certs renew front-proxy-client" master -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        }

        General::Logging::log_user_message("Rotate Front Proxy Certs");
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
    sub Check_Front_Proxy_CA_Certificate_Validity_Time_P937S01T16 {

        my $command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        if (version->parse($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}) < version->parse("2.18.0")) {
            $command = sprintf '%ssudo ansible -a "sudo /usr/local/bin/kubeadm alpha certs check-expiration" master -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        } else {
            $command = sprintf '%ssudo ansible -a "sudo /usr/local/bin/kubeadm certs check-expiration" master -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        }

        General::Logging::log_user_message("Check Front Proxy CA Certificate Validity Time");
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
    sub Restart_Metrics_Server_P937S01T17 {

        my $command = sprintf '%skubectl delete pod -n kube-system $(kubectl get pods -n kube-system |grep metrics | awk \'{print $1}\')', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        return 0 if ($debug_command ne "");

        General::Logging::log_user_message("Restart Metrics Server");
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
    sub Cleanup_Remove_Front_Proxy_CA_Certificates_And_Keys_From_Masters_P937S01T18 {

        my $command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        $command = sprintf '%ssudo ansible -a "sudo rm /home/eccd/front-proxy-ca.crt" master -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        General::Logging::log_user_message("Cleanup Remove Front Proxy CA Certificates From Masters");
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

        $command = sprintf '%ssudo ansible -a "sudo rm /home/eccd/front-proxy-ca.key" master -i /mnt/config/inventory/ibd_inventory_file.ini', $debug_command;
        General::Logging::log_user_message("Cleanup Remove Front Proxy CA Keys From Masters");
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
    sub Cleanup_Remove_Front_Proxy_CA_Certificates_And_Keys_From_Director0_P937S01T19 {

        my $command = sprintf '%ssudo -n rm /home/eccd/front-proxy-*', $debug_command;
        my $hide_output = $::JOB_PARAMS{'HIDE_COMMAND_OUTPUT'} eq "yes" ? 1 : 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Cleanup Remove Front Proxy CA Certificates And Keys From Director-0");
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
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P937S99 {

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

This Playlist generates new ECCD Front Proxy CA Certificates and Keys.
It is used when doing Time Shift and ECCD Certificate rotation test cases.

This Playlist MUST BE EXECUTED ON DIRECTOR-0 of the node because it uses Ansible
playbooks and are manipulating the kube config files.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.

Used Job Parameters:
====================
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
