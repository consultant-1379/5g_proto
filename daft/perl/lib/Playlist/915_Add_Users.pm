package Playlist::915_Add_Users;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.46
#  Date     : 2024-05-29 14:00:59
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
    usage
    );

# Used Perl package files
use ADP::Kubernetes_Operations;
use General::File_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

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

    $rc = General::Playlist_Operations::execute_step( \&Check_Node_Status_P915S01, \&Fallback001_P915S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Add_Users_From_File_P915S02, \&Fallback001_P915S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Reset_User_Passwords_P915S03, \&Fallback001_P915S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Access_Rights_To_Config_Data_P915S04, \&Fallback001_P915S99 );
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
sub Check_Node_Status_P915S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_That_Pods_Are_Up_P915S01T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_Day0_User_Password_P915S01T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_CMYP_IP_Address_P915S01T03 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Old_SSH_Keys_P915S01T04 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_If_SSH_Is_Available_To_CMYP_P915S01T05 } );
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
    sub Check_That_Pods_Are_Up_P915S01T01 {

        my @failed_pods;
        my $rc = 0;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        General::Logging::log_user_message("Checking that all pods in $sc_namespace namespace are up.");
        $rc = ADP::Kubernetes_Operations::check_pod_status(
            {
                "all-namespaces"            => 0,
                "debug-messages"            => 0,
                "delay-time"                => 0,
                "hide-output"               => 1,
                "max-attempts"              => 1,
                "max-time"                  => 0,
                "namespace"                 => $sc_namespace,
                # "pod-exclude-list"        => \@pod_exclude_list,
                "pod-exclude-ready-list"    => [
                    "eric-bsf-cert-notifier.*",
                    "eric-bsf-diameter.*",
                    "eric-bsf-wcdb-cd-tls-restarter.*",
                    "eric-bsf-worker.*",
                    "eric-cm-mediator-key-init.*",
                    "eric-data-wide-column-database-cd-tls-restarter.*",
                    "eric-data-search-engine-curator.*",
                    "eric-log-transformer.*",
                    "eric-scp-cert-notifier.*",
                    "eric-scp-worker.*",
                    "eric-sec-key-management-job.*",
                    "eric-sepp-cert-notifier.*",
                    "eric-sepp-worker.*",
                    "eric-sw-inventory-manager.*",
                ],
                "pod-exclude-status-list"   => [
                    "eric-data-search-engine-curator.*",
                    "eric-log-transformer.*",
                ],
                # "pod-include-list"        => \@pod_include_list,
                "repeated-checks"           => 0,
                "return-failed-pods"        => \@failed_pods,
                # "return-output"           => \@check_result,
                "wanted-ready"              => "same",
                "wanted-status"             => "up",
            }
        );
        if ($rc == 1) {
            # All POD's are not yet up, wait for 3 specific pods to come up and stay up
            if (@failed_pods) {
                General::Logging::log_user_message("The following PODs are not up:\n" . (join "\n", @failed_pods));
            }
            General::Logging::log_user_message("Waiting for 3 specific pods to stay up for at least 3 minutes.\nThis will take a while to complete.\n");
            $rc = ADP::Kubernetes_Operations::check_pod_status(
                {
                    "all-namespaces"    => 0,
                    "debug-messages"    => 0,
                    "delay-time"        => 10,
                    "hide-output"       => 1,
                    "max-attempts"      => 0,
                    "max-time"          => 180,
                    "namespace"         => $sc_namespace,
                    # "pod-exclude-list"  => \@pod_exclude_List,
                    "pod-include-list"  => [
                        "eric-cm-yang-provider-.+",
                        "eric-sec-admin-user-management.+",
                        "eric-sec-ldap-server-0",
                    ],
                    "repeated-checks"   => 1,
                    # "return-output"     => \@check_result,
                    "wanted-ready"      => "same",
                    "wanted-status"     => "Running",
                }
            );
        }

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) All expected PODs are up';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check pod status");
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
    sub Get_Day0_User_Password_P915S01T02 {

        my $day0_password = "";
        my $rc = 0;

        # Convert encrypted password to clear text
        General::Logging::log_user_message("Reading password for day0 admin user from network config file");
        for my $key (sort keys %::JOB_PARAMS) {
            if ($key =~ /^wcdbcd_day0_secret_hash_parameter_\d{2}$/) {
                # Old naming format with a 2 digit number in key
                next if ($::JOB_PARAMS{$key} =~ /^.+=CHANGEME$/);
                if ($::JOB_PARAMS{$key} =~ /^data\.admin_password=(.+)/) {
                    $day0_password = $1;    # Encrypted password
                }
            } elsif ($key =~ /^wcdbcd_day0_secret_hash_parameter_(\S+)$/) {
                # New naming format with parameter name included in key
                my $key_value = "$1";
                next if ($::JOB_PARAMS{$key} =~ /^CHANGEME$/);
                if ($key_value eq "data.admin_password") {
                    $day0_password = $::JOB_PARAMS{$key};    # Encrypted password
                }
            } elsif ($key =~ /^day0_admin_secret_hash_parameter_(\S+)$/) {
                # New naming format with parameter name included in key
                my $key_value = "$1";
                next if ($::JOB_PARAMS{$key} =~ /^CHANGEME$/);
                if ($key_value eq "data.adminpasswd") {
                    $day0_password = $::JOB_PARAMS{$key};    # Encrypted password
                }
            }
        }

        if ($day0_password ne "") {
            # Decrypt the password
            $day0_password = General::OS_Operations::base64_decode($day0_password);
            $::JOB_PARAMS{'DAY0_PASSWORD'} = $day0_password;
        } else {
            General::Logging::log_user_error_message("No password provided for day0 admin user in network config file");
            return 1;
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
    sub Get_CMYP_IP_Address_P915S01T03 {

        my $rc = 0;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        General::Logging::log_user_message("Get eric-cm-yang-provider IP");
        $::JOB_PARAMS{'CMYP_IP'} = ADP::Kubernetes_Operations::get_cmyp_ip($sc_namespace);
        if (defined $::JOB_PARAMS{'CMYP_IP'}) {
            General::Logging::log_user_message("CMYP IP address=$::JOB_PARAMS{'CMYP_IP'}");
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get eric-cm-yang-provider IP");
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
    sub Remove_Old_SSH_Keys_P915S01T04 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
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
                "command"       => "ssh-keygen -R '[$cmyp_ip]:830' -f $ENV{'HOME'}/.ssh/known_hosts",
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
    sub Check_If_SSH_Is_Available_To_CMYP_P915S01T05 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port = 22;
        my $repeat_at_error = 0;
        my $timeout = 2;

        if ($::JOB_PARAMS{'USE_NETCONF'} eq "yes") {
            $cmyp_port = 830;
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
sub Add_Users_From_File_P915S02 {

    my $rc;

    if ($::JOB_PARAMS{'USE_NETCONF'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_If_Users_Already_Exists_Via_Netconf_P915S02T01 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Add_Users_Via_Netconf_P915S02T02 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Users_Via_Netconf_P915S02T03 } );
        return $rc if $rc < 0;
    } else {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_If_Users_Already_Exists_Via_CLI_P915S02T04 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Add_Users_Via_CLI_P915S02T05 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Users_Via_CLI_P915S02T06 } );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Users_In_LDAP_P915S02T07 } );
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
    sub Check_If_Users_Already_Exists_Via_Netconf_P915S02T01 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port = 830;
        my $day0_password = $::JOB_PARAMS{'DAY0_PASSWORD'};
        my $day0_user = "admin";
        my $file_name = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/netconf/get-config.netconf";
        my $not_found_users = "";
        my $rc = 0;
        my @result = ();
        my @users = ();

        General::Logging::log_user_message("Reading users to be added");
        $rc = General::File_Operations::read_file(
            {
                "filename"            => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/users_to_be_added.txt",
                "output-ref"          => \@users,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to read file with users to add");
            return 1;
        }

        General::Logging::log_user_message("Checking if users exists via NETCONF");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$cmyp_ip " .
                                        "--user=$day0_user " .
                                        "--password='$day0_password' " .
                                        "--port=$cmyp_port " .
                                        "--shell=netconf " .
                                        "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                        "--command-file='$file_name' ",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            my $found = 0;
            for my $user (@users) {
                $found = 0;
                for (@result) {
                    if (/<user>.*?<name>$user<\/name>.*?<\/user>/) {
                        $found = 1;
                        last;
                    }
                }
                if ($found == 0) {
                    $not_found_users .= "  $user\n";
                }
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check users");
        }

        if ($rc == 0) {
            if ($not_found_users eq "") {
                General::Logging::log_user_message("All users was already present, so nothing needs to be added");
                push @::JOB_STATUS, '(/) Users to be added was already present';
                $rc = General::Playlist_Operations::RC_STEPOUT;
            } else {
                General::Logging::log_user_message("The following users were not found and will be added in next task:\n$not_found_users");
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
    sub Add_Users_Via_Netconf_P915S02T02 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port = 830;
        my $day0_password = $::JOB_PARAMS{'DAY0_PASSWORD'};
        my $day0_user = "admin";
        my $file_name = $::JOB_PARAMS{'USER_DATA_FILE'};
        my $rc = 0;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        General::Logging::log_user_message("Loading user data in CMYP via netconf");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$cmyp_ip " .
                                        "--user=$day0_user " .
                                        "--password='$day0_password' " .
                                        "--port=$cmyp_port " .
                                        "--shell=netconf " .
                                        "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                        "--command-file='$file_name' ",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Adding Users was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to load user data");
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
    sub Check_Users_Via_Netconf_P915S02T03 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port = 830;
        my $day0_password = $::JOB_PARAMS{'DAY0_PASSWORD'};
        my $day0_user = "admin";
        my $file_name = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/netconf/get-config.netconf";
        my $not_found_users = "";
        my $rc = 0;
        my @result = ();
        my @users = ();

        General::Logging::log_user_message("Reading users to be added");
        $rc = General::File_Operations::read_file(
            {
                "filename"            => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/users_to_be_added.txt",
                "output-ref"          => \@users,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to read file with users to add");
            return 1;
        }

        General::Logging::log_user_message("Checking if users exists via NETCONF");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$cmyp_ip " .
                                        "--user=$day0_user " .
                                        "--password='$day0_password' " .
                                        "--port=$cmyp_port " .
                                        "--shell=netconf " .
                                        "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                        "--command-file='$file_name' ",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            my $found = 0;
            for my $user (@users) {
                $found = 0;
                for (@result) {
                    if (/<user>.*?<name>$user<\/name>.*?<\/user>/) {
                        $found = 1;
                        last;
                    }
                }
                if ($found == 0) {
                    $not_found_users .= "  $user\n";
                }
            }
            if ($not_found_users ne "") {
                General::Logging::log_user_error_message("The following users were not found:\n$not_found_users");
                $rc = 1;
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check users");
        }

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Checking Added Users was successful';
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
    sub Check_If_Users_Already_Exists_Via_CLI_P915S02T04 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port = 22;
        my $day0_password = $::JOB_PARAMS{'DAY0_PASSWORD'};
        my $day0_user = "admin";
        my $file_name = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/users_to_be_added.txt";
        my $not_found_users = "";
        my $rc = 0;
        my @result = ();
        my @users = ();
        my $use_workaround = 1;

        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
            $use_workaround = 0;
        }

CHECK_IF_USERS_ALREADY_EXISTS_VIA_CLI:

        General::Logging::log_user_message("Reading users to be added");
        $rc = General::File_Operations::read_file(
            {
                "filename"            => $file_name,
                "output-ref"          => \@users,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to read file with users to add");
            return 1;
        }

        General::Logging::log_user_message("Checking if users exists via CLI");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$cmyp_ip " .
                                        "--user=$day0_user " .
                                        "--password='$day0_password' " .
                                        "--port=$cmyp_port " .
                                        "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                        "--command='show running-config system authentication user|nomore'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            my $found = 0;
            for my $user (@users) {
                $found = 0;
                for (@result) {
                    if (/^system authentication user $user$/) {
                        $found = 1;
                        last;
                    }
                }
                if ($found == 0) {
                    $not_found_users .= "  $user\n";
                }
            }
        } else {
            if ($use_workaround == 1) {
                # If we come here we a dealing with a workaround to restart CMYP pod because of a
                # known issue on the 1.3.x software release where the 'system' object does not
                # become available until CMYP POD is restarted.
                # This logic should be reworked or removed when the restart is no longer needed.

                General::Playlist_Operations::register_workaround_task("This is a workaround for a known problem in the 1.3 release where it is not possible to view the system object in CMYP after a deployment unless the POD is first restarted.");

                my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

                my $found = 0;
                for (@result) {
                    if (/^syntax error: element does not exist\s*$/) {
                        $found = 1;
                        last;
                    }
                }
                if ($found == 1) {
                    # Only attempt this workaround once
                    $use_workaround = 0;

                    # Get CMYP pod name
                    my @pod_names = ADP::Kubernetes_Operations::get_pod_names(
                        {
                            "namespace"         => $sc_namespace,
                            "pod-include-list"  => [ "eric-cm-yang-provider-.+" ],
                            "hide-output"   => 1,
                        }
                    );
                    if (scalar @pod_names != 1) {
                        General::Logging::log_user_error_message("Not able to find pod name for eric-cm-yang-provider");
                        return 1;
                    }
                    my $cmyp_pod_name = $pod_names[0];

                    General::Logging::log_user_message("Restarting eric-cm-yang-provider pod");
                    $rc = General::OS_Operations::send_command(
                        {
                            "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} delete pod $cmyp_pod_name --namespace $sc_namespace",
                            "hide-output"   => 1,
                        }
                    );
                    if ($rc != 0) {
                        # Display the result in case of error
                        General::OS_Operations::write_last_temporary_output_to_progress();

                        General::Logging::log_user_error_message("Failed to restart eric-cm-yang-provider");
                        return 1;
                    }

                    General::Logging::log_user_message("Waiting for eric-cm-yang-provider pod to stay up for at least 3 minutes.\nThis will take a while to complete.\n");
                    $rc = ADP::Kubernetes_Operations::check_pod_status(
                        {
                            "all-namespaces"    => 0,
                            "debug-messages"    => 0,
                            "delay-time"        => 0,
                            "hide-output"       => 1,
                            "max-attempts"      => 0,
                            "max-time"          => 180,
                            "namespace"         => $sc_namespace,
                            # "pod-exclude-list"  => \@pod_exclude_List,
                            "pod-include-list"  => [
                                "eric-cm-yang-provider-.+",
                            ],
                            "repeated-checks"   => 1,
                            # "return-output"     => \@check_result,
                            "wanted-ready"      => "same",
                            "wanted-status"     => "Running",
                        }
                    );
                    if ($rc != 0) {
                        # Display the result in case of error
                        General::OS_Operations::write_last_temporary_output_to_progress();

                        General::Logging::log_user_error_message("Failed to get proper status of eric-cm-yang-provider pod");
                        return 1;
                    }

                    goto CHECK_IF_USERS_ALREADY_EXISTS_VIA_CLI;
                }
            }

            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check users");
        }

        if ($rc == 0) {
            if ($not_found_users eq "") {
                General::Logging::log_user_message("All users was already present, so nothing needs to be added");
                push @::JOB_STATUS, '(/) Users to be added was already present';
                $rc = General::Playlist_Operations::RC_STEPOUT;
            } else {
                General::Logging::log_user_message("The following users were not found and will be added in next task:\n$not_found_users");
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
    sub Add_Users_Via_CLI_P915S02T05 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port = 22;
        my $day0_password = $::JOB_PARAMS{'DAY0_PASSWORD'};
        my $day0_user = "admin";
        my $file_name = $::JOB_PARAMS{'USER_DATA_FILE'};
        my $rc = 0;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        General::Logging::log_user_message("Loading user data in CMYP via CLI");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$cmyp_ip " .
                                        "--user=$day0_user " .
                                        "--password='$day0_password' " .
                                        "--port=$cmyp_port " .
                                        "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                        "--command-file='$file_name' ",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Adding Users was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to load user data");
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
    sub Check_Users_Via_CLI_P915S02T06 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port = 22;
        my $day0_password = $::JOB_PARAMS{'DAY0_PASSWORD'};
        my $day0_user = "admin";
        my $file_name = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/users_to_be_added.txt";
        my $not_found_users = "";
        my $rc = 0;
        my @result = ();
        my @users = ();

        General::Logging::log_user_message("Reading users to be added");
        $rc = General::File_Operations::read_file(
            {
                "filename"            => $file_name,
                "output-ref"          => \@users,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to read file with users to add");
            return 1;
        }

        General::Logging::log_user_message("Checking if users exists via CLI");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$cmyp_ip " .
                                        "--user=$day0_user " .
                                        "--password='$day0_password' " .
                                        "--port=$cmyp_port " .
                                        "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                        "--command='show running-config system authentication user|nomore' ",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            my $found = 0;
            for my $user (@users) {
                $found = 0;
                for (@result) {
                    if (/^system authentication user $user$/) {
                        $found = 1;
                        last;
                    }
                }
                if ($found == 0) {
                    $not_found_users .= "  $user\n";
                }
            }
            if ($not_found_users ne "") {
                General::Logging::log_user_error_message("The following users were not found:\n$not_found_users");
                $rc = 1;
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check users");
        }

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Checking Added Users was successful';
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
    sub Check_Users_In_LDAP_P915S02T07 {

        my $attempt_count = 1;
        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port = 22;
        my $command = "";
        my $day0_password = $::JOB_PARAMS{'DAY0_PASSWORD'};
        my $day0_user = "admin";
        my $file_name = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/users_to_be_added.txt";
        my $not_found_users = "";
        my $rc = 0;
        my @result = ();
        my $timeout_epoch = time() + 120;   # Wait max 2 minutes for the users to be available in LDAP
        my @users = ();

        General::Logging::log_user_message("Reading users to be added");
        $rc = General::File_Operations::read_file(
            {
                "filename"            => $file_name,
                "output-ref"          => \@users,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to read file with users to add");
            return 1;
        }

        while (time() < $timeout_epoch) {
            if ($attempt_count > 1) {
                General::Logging::log_user_message("The '$not_found_users' users are still missing in LDAP, waiting 5 seconds before checking again");
                General::OS_Operations::sleep_with_progress(
                    {
                        "allow-interrupt"   => 1,
                        "confirm-interrupt" => 0,
                        "progress-message"  => 0,
                        "progress-interval" => 1,
                        "seconds"           => 5,
                        "use-logging"       => 1,
                    }
                );
            }

            General::Logging::log_user_message("Checking if users exists in LDAP, attempt $attempt_count");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} " . 'exec -it eric-sec-ldap-server-0 -- ldapsearch -Y EXTERNAL -H ldapi://%2Frun%2Fslapd%2Fslapd.sock -b "ou=people,dc=la,dc=adp,dc=ericsson"',
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0) {
                my %ldap_users;
                for (@result) {
                    if (/^uid:\s+(\S+)/) {
                        # E.g. uid: sepp-sec-admin
                        $ldap_users{$1} = 1;
                    }
                }
                my $found = 0;
                $not_found_users = "";
                for my $user (@users) {
                    unless (exists $ldap_users{$user}) {
                        $not_found_users .= "$user ";
                    }
                }
                if ($not_found_users eq "") {
                    # All users found
                    last;
                } else {
                    $not_found_users =~ s/\s*$//;
                }
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to check users in LDAP");
                return 1;
            }
            $attempt_count++;
        }

        if ($not_found_users eq "") {
            General::Logging::log_user_message("All users exists in LDAP");
            push @::JOB_STATUS, '(/) Users existing in LDAP';
            return 0;
        } else {
            General::Logging::log_user_error_message("The following users are missing in LDAP: $not_found_users");
            push @::JOB_STATUS, '(x) Users missing in LDAP';
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
sub Reset_User_Passwords_P915S03 {

    my $rc = 0;

    if ($::JOB_PARAMS{'PASSWORD_EXPIRE'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_Password_If_Needed_P915S03T01 } );
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
    sub Change_Password_If_Needed_P915S03T01 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port = 22;
        my $error_cnt = 0;
        my $file_name = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/users_to_be_added.txt";
        my $new_password;
        my $password;
        my $rc = 0;
        my @result = ();
        my @users = ();

        if ($::JOB_PARAMS{'USE_NETCONF'} eq "yes") {
            $cmyp_port = 830;
        }

        General::Logging::log_user_message("Reading users who's password should be changed");
        $rc = General::File_Operations::read_file(
            {
                "filename"            => $file_name,
                "output-ref"          => \@users,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to read file with users to add");
            return 1;
        }

        for my $user (@users) {
            $new_password = "";
            $password = "";
            for my $key1 (sort keys %::NETWORK_CONFIG_PARAMS) {
                next unless ($key1 =~ /^default_user_\d+$/);
                if (exists $::NETWORK_CONFIG_PARAMS{$key1}{'user'} && $::NETWORK_CONFIG_PARAMS{$key1}{'user'} eq $user) {
                    if (exists $::NETWORK_CONFIG_PARAMS{$key1}{'initial_password'} && $::NETWORK_CONFIG_PARAMS{$key1}{'initial_password'} ne "CHANGEME") {
                        $password = $::NETWORK_CONFIG_PARAMS{$key1}{'initial_password'};
                    }
                    if (exists $::NETWORK_CONFIG_PARAMS{$key1}{'password'} && $::NETWORK_CONFIG_PARAMS{$key1}{'password'} ne "CHANGEME") {
                        $new_password = $::NETWORK_CONFIG_PARAMS{$key1}{'password'};
                    }
                    last;
                } else {
                    next;
                }
            }
            if ($password eq "" || $new_password eq "") {
                push @::JOB_STATUS, "(-) Changing password on user '$user' was skipped";
                General::Logging::log_user_warning_message("User '$user' ignored because either, no network config 'default_user_xx' parameter exist for this user, or it's password or initial_password have the value 'CHANGEME'");
                next;
            }

            General::Logging::log_user_message("Changing password for user '$user'");
            if ($::JOB_PARAMS{'USE_NETCONF'} eq "yes") {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                                "--ip=$cmyp_ip " .
                                                "--user=$user " .
                                                "--password='$password' " .
                                                "--new-password='$new_password' " .
                                                "--port=$cmyp_port " .
                                                "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                                "--shell=netconf " .
                                                "--command='# Do not execute any command'",
                        "hide-output"   => 1,
                    }
                );
            } else {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                                "--ip=$cmyp_ip " .
                                                "--user=$user " .
                                                "--password='$password' " .
                                                "--new-password='$new_password' " .
                                                "--port=$cmyp_port " .
                                                "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                                "--command='# Do not execute any command'",
                        "hide-output"   => 1,
                    }
                );
            }
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) Changing password on user '$user' was successful";
            } else {
                push @::JOB_STATUS, "(x) Changing password on user '$user' failed";

                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to change password for user '$user'");
                $error_cnt++;
            }
        }

        if ($error_cnt == 0) {
            return 0;
        } else {
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
sub Check_Access_Rights_To_Config_Data_P915S04 {

    my $rc = 0;

    if ($::JOB_PARAMS{'USE_NETCONF'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Access_Right_To_Config_Data_Via_Netconf_P915S04T01 } );
        return $rc if $rc < 0;
    } else {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Access_Right_To_Config_Data_Via_CLI_P915S04T02 } );
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
    sub Check_Access_Right_To_Config_Data_Via_Netconf_P915S04T01 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port = 830;
        my $error_cnt = 0;
        my $file_name = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/netconf/get-config.netconf";
        my $password;
        my $rc = 0;
        my @result = ();
        my $user;
        my @users = ();

        General::Logging::log_user_message("Reading users to be checked");
        $rc = General::File_Operations::read_file(
            {
                "filename"            => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/users_to_be_added.txt",
                "output-ref"          => \@users,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to read file with users to check");
            return 1;
        }

        for my $user (@users) {
            $password = "";
            for my $key1 (sort keys %::NETWORK_CONFIG_PARAMS) {
                next unless ($key1 =~ /^default_user_\d+$/);
                if (exists $::NETWORK_CONFIG_PARAMS{$key1}{'user'} && $::NETWORK_CONFIG_PARAMS{$key1}{'user'} eq $user) {
                    if (exists $::NETWORK_CONFIG_PARAMS{$key1}{'password'} && $::NETWORK_CONFIG_PARAMS{$key1}{'password'} ne "CHANGEME") {
                        $password = $::NETWORK_CONFIG_PARAMS{$key1}{'password'};
                    }
                    last;
                } else {
                    next;
                }
            }
            if ($password eq "") {
                push @::JOB_STATUS, "(-) Checking access to configuration data for user '$user' was skipped";
                General::Logging::log_user_warning_message("User '$user' ignored because either, no network config 'default_user_xx' parameter exist for this user, or it's password have the value 'CHANGEME'");
                next;
            }

            General::Logging::log_user_message("Checking access to configuration data for user '$user'");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                            "--ip=$cmyp_ip " .
                                            "--user=$user " .
                                            "--password='$password' " .
                                            "--port=$cmyp_port " .
                                            "--shell=netconf " .
                                            "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                            "--command-file='$file_name' ",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0) {
                my $found = 0;
                for (@result) {
                    $found = 0;
                    # TODO: Add logic to check that the data recived match with expected data for the user.
                    # I.e. that an scp-admin user can only see scp related data etc.
                    # $error_cnt++ if ......;
                }
                push @::JOB_STATUS, "(/) User '$user' MAYBE has access to the correct configuration data";
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to check access to configuration data");
                $error_cnt++;
            }
        }

        if ($error_cnt == 0) {
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
    sub Check_Access_Right_To_Config_Data_Via_CLI_P915S04T02 {

        my $cmyp_ip = $::JOB_PARAMS{'CMYP_IP'};
        my $cmyp_port = 22;
        my $error_cnt = 0;
        my $password;
        my $rc = 0;
        my @result = ();
        my $user;
        my @users = ();

        General::Logging::log_user_message("Reading users to be checked");
        $rc = General::File_Operations::read_file(
            {
                "filename"            => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/users_to_be_added.txt",
                "output-ref"          => \@users,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to read file with users to check");
            return 1;
        }

        for my $user (@users) {
            $password = "";
            for my $key1 (sort keys %::NETWORK_CONFIG_PARAMS) {
                next unless ($key1 =~ /^default_user_\d+$/);
                if (exists $::NETWORK_CONFIG_PARAMS{$key1}{'user'} && $::NETWORK_CONFIG_PARAMS{$key1}{'user'} eq $user) {
                    if (exists $::NETWORK_CONFIG_PARAMS{$key1}{'password'} && $::NETWORK_CONFIG_PARAMS{$key1}{'password'} ne "CHANGEME") {
                        $password = $::NETWORK_CONFIG_PARAMS{$key1}{'password'};
                    }
                    last;
                } else {
                    next;
                }
            }
            if ($password eq "") {
                push @::JOB_STATUS, "(-) Checking access to configuration data for user '$user' was skipped";
                General::Logging::log_user_warning_message("User '$user' ignored because either, no network config 'default_user_xx' parameter exist for this user, or it's password have the value 'CHANGEME'");
                next;
            }

            General::Logging::log_user_message("Checking access to configuration data for user '$user'");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                            "--ip=$cmyp_ip " .
                                            "--user=$user " .
                                            "--password='$password' " .
                                            "--port=$cmyp_port " .
                                            "--timeout=$::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'} " .
                                            "--command='show running-config|nomore'",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0) {
                my $found = 0;
                for (@result) {
                    $found = 0;
                    # TODO: Add logic to check that the data recived match with expected data for the user.
                    # I.e. that an scp-admin user can only see scp related data etc.
                    # $error_cnt++ if ......;
                }
                push @::JOB_STATUS, "(/) User '$user' MAYBE has access to the correct configuration data";
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to check access to configuration data");
                $error_cnt++;
            }
        }

        if ($error_cnt == 0) {
            return 0;
        } else {
            return 1;
        }
    }
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P915S99 {

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

This Playlist is used for adding new users.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
