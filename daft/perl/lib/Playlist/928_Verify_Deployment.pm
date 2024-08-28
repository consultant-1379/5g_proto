package Playlist::928_Verify_Deployment;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.16
#  Date     : 2024-05-29 14:00:59
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
    usage
    );

# Used Perl package files
use ADP::Kubernetes_Operations;
use File::Basename qw(dirname basename);
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

    $rc = General::Playlist_Operations::execute_step( \&Verify_SC_Deployment_P928S01, \&Fallback001_P928S99 );
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
sub Verify_SC_Deployment_P928S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Verify_SC_Application_Deployment_P928S01T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Pod_Status_P928S01T02 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'JOBTYPE'} eq "UPGRADE") {

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Stable_Node_P928S01T03 } );
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
    sub Verify_SC_Application_Deployment_P928S01T01 {

        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $release_name = "";
        my @release_names = ();
        my $helm_executable =  $::JOB_PARAMS{'HELM_EXECUTABLE'};
        my $rc = 0;
        my @result;

        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
            General::Logging::log_user_message("Skipped because DEBUG_PLAYLIST=yes");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Checking deployment status:");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${helm_executable} list --all --namespace $namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check deployment status");
            push @::JOB_STATUS, "(x) Checking Deployment Status failed";
            return 1;
        }

        if (exists $::JOB_PARAMS{'DEPLOYED_UPGRADED_HELM_RELEASES'} && $::JOB_PARAMS{'DEPLOYED_UPGRADED_HELM_RELEASES'} ne "") {
            # New way to Deploy or Upgrade the application
            @release_names = split /\|/, $::JOB_PARAMS{'DEPLOYED_UPGRADED_HELM_RELEASES'};
        } else {
            # Old way, for now we keep it until all playlists has been updated to handle the new namespace and release naming etc.
            push @release_names, $::JOB_PARAMS{'SC_RELEASE_NAME'};
        }

        my $found = 0;
        for $release_name (@release_names) {
            if ($release_name eq "") {
                # Handle special case when DEPLOYED_UPGRADED_HELM_RELEASES contains a | at the end
                $found++;
                next;
            }
            General::Logging::log_user_message("  Looking in namespace '$namespace' for release name '$release_name'");
            for (@result) {
                if ($::JOB_PARAMS{'HELM_VERSION'} == 2) {
                    # NAME        REVISION    UPDATED                     STATUS      CHART                                       APP VERSION             NAMESPACE
                    # chfsim      1           Mon Oct  5 14:44:03 2020    DEPLOYED    eric-chfsim-1.2.25-0f516d8b7-dirty          1.2.25-0f516d8b7-dirty  scp
                    if (/^$release_name\s+\d+\s+.+\s+DEPLOYED\s+eric-\S+\s+$namespace\s*/) {
                        $found++;
                        last;
                    }
                } elsif ($::JOB_PARAMS{'HELM_VERSION'} == 3) {
                    # NAME    NAMESPACE       REVISION        UPDATED                                 STATUS          CHART                           APP VERSION
                    # eric-sc eiffelesc       1               2020-10-16 14:11:03.296029048 +0000 UTC deployed        eric-sc-umbrella-1.2.2+247
                    if (/^$release_name\s+$namespace\s+\d+\s+.+\s+deployed\s+eric-\S+\s*/) {
                        $found++;
                        last;
                    }
                }
            }
        }
        if ($found == scalar @release_names) {
            push @::JOB_STATUS, "(/) Checking Deployment Status successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            push @::JOB_STATUS, "(x) Checking Deployment Status failed";
            $rc = 1;
            if ($found == 0) {
                General::Logging::log_user_error_message("Could not find expected deployment status");
            } else {
                General::Logging::log_user_error_message(sprintf "Could only find %d of %d properly deployed releases in namespace '%s'\nFound Releases:\n%s", $found, scalar @release_names, $namespace, (join "\n", @result));
            }
        }

        return $rc;
    }

    # -----------------------------------------------------------------------------
    # This subroutine was added because after a deployment or an Upgrade in a playlist
    # extension we might want to e.g. check things in CMYP and if the POD's are not
    # all up it might result in SSH connection issues like 'Connection refused'.
    # So to handle this issue we check that all pods are coming up before continuing
    # playlist execution.
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
    sub Check_Pod_Status_P928S01T02 {

        my $delay_time = 0;
        my $max_attempts = 1;
        my $max_time = 0;
        my $repeated_checks = 0;
        my @exclude_ready_list = ();
        my @exclude_status_list = ();
        my $message = "";
        my $rc = 0;
        my @result;
        my $namespace = "";

        if (exists $::JOB_PARAMS{'SC_NAMESPACE'}) {
            $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        } else {
            $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        }

        # Ignore the following POD which is constantly respawned and for a
        # short time the status is not Completed.
        @exclude_status_list = (
            "eric-data-search-engine-curator.*",
            "eric-log-transformer.*",
        );

        # We wait a maximum of POD_STATUS_TIMEOUT seconds for the PODs to come up
        $delay_time = 10;
        $max_attempts = 0;
        $max_time = exists $::JOB_PARAMS{'POD_STATUS_TIMEOUT'} ? $::JOB_PARAMS{'POD_STATUS_TIMEOUT'} : 10;
        $repeated_checks = 0;

        if ($::JOB_PARAMS{'JOBTYPE'} eq "DEPLOY") {
            # After a deployment not all pod's will have x/x ready status because
            # the configuration data has not yet been loaded so ignore the following
            # POD's in status Completed or Running where the READY status is not all
            # up because of missing configuration.
            @exclude_ready_list = (
                "eric-bsf-cert-notifier.*",
                "eric-bsf-diameter.*",
                "eric-bsf-worker.*",
                "eric-csa-worker.*",
                "eric-data-search-engine-curator.*",
                "eric-log-transformer.*",
                "eric-scp-cert-notifier.*",
                "eric-scp-worker.*",
                "eric-sepp-cert-notifier.*",
                "eric-sepp-worker.*",
            );
            # We wait a maximum of POD_STATUS_TIMEOUT seconds for the PODs to come up
            $delay_time = 10;
            $max_attempts = 0;
            $max_time = exists $::JOB_PARAMS{'POD_STATUS_TIMEOUT'} ? $::JOB_PARAMS{'POD_STATUS_TIMEOUT'} : 10;
            $repeated_checks = 0;
        } elsif ($::JOB_PARAMS{'JOBTYPE'} eq "UPGRADE") {
            # For job type UPGRADE we assume that all pod's should come up and be ready
            # with x/x within a certain time limit except for the following POD's in
            # status Completed where the READY status is not all up because these POD's
            # are just Jobs and not normal pods.
            @exclude_ready_list = (
                "eric-data-search-engine-curator.*",
                "eric-log-transformer.*",
            );
        } else {
            General::Logging::log_user_warning_message("Wrong JOBTYPE (=$::JOB_PARAMS{'JOBTYPE'}), only expecting 'DEPLOY' or 'UPGRADE'.");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        if ($max_time > 0) {
            General::Logging::log_user_message("Checking every 10 seconds that all pods in $namespace namespace are up for a maximum of $max_time seconds.");
        } else {
            General::Logging::log_user_message("Checking that all pods in $namespace namespace are up.");
        }
        $rc = ADP::Kubernetes_Operations::check_pod_status(
            {
                "all-namespaces"                            => 0,
                "debug-messages"                            => 0,
                "delay-time"                                => $delay_time,
                "ignore-ready-check-for-completed-status"   => 1,
                "hide-output"                               => 1,
                "max-attempts"                              => $max_attempts,
                "max-time"                                  => $max_time,
                "namespace"                                 => $namespace,
                "pod-exclude-ready-list"                    => \@exclude_ready_list,
                "pod-exclude-status-list"                   => \@exclude_status_list,
                "progress-messages"                         => 1,
                "repeated-checks"                           => $repeated_checks,
                "return-failed-pods"                        => \@result,
                "wanted-ready"                              => "same",
                "wanted-status"                             => "up",
            }
        );
        if ($rc == 0) {
            General::Logging::log_user_message("POD status check was successful");
        } else {
            $message = join("\n",@result);
            if ($::JOB_PARAMS{"IGNORE_FAILED_HEALTH_CHECK"} =~ /^yes$/i) {
                General::Logging::log_user_warning_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n$message\nBut ignored because IGNORE_FAILED_HEALTH_CHECK=yes.");
                # Ignore the error and continue like everything was OK
                $rc = 0;
            } else {
                General::Logging::log_user_error_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n$message");
            }
        }

        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Post $::JOB_PARAMS{'JOBTYPE'} POD status check was successful";
        } else {
            push @::JOB_STATUS, "(x) Post $::JOB_PARAMS{'JOBTYPE'} POD status check failed";
            $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        }

        return $rc;
    }

    # -----------------------------------------------------------------------------
    # This subroutine was added because after an Upgrade where e.g.BSF is configured
    # and traffic is running it might take a while for the following alarm to clear:
    #   - BsfBindingDbConnectionErrors - BSF, Binding Database Connection Errors
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
    sub Wait_For_Stable_Node_P928S01T03 {

        my $delay_time = exists $::JOB_PARAMS{'WAIT_FOR_STABLE_NODE_TIMER'} ? $::JOB_PARAMS{'WAIT_FOR_STABLE_NODE_TIMER'} : 0;
        my $rc = 0;

        if ( ! exists $::JOB_PARAMS{'WAIT_FOR_STABLE_NODE_TIMER'}) {
            # We end up here only if the playlist logic has a fault and this subroutine is called
            # without setting this variable in the main playlist.
            General::Logging::log_user_warning_message("WAIT_FOR_STABLE_NODE_TIMER variable not defined, skipping this task.");
            return General::Playlist_Operations::RC_TASKOUT;
        } elsif ( $delay_time == 0) {
            # No wait wanted
            General::Logging::log_user_message("WAIT_FOR_STABLE_NODE_TIMER=0 so no wait wanted, skipping this task.");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("WAIT_FOR_STABLE_NODE_TIMER=$delay_time so wait for node to stabilize.");

        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "confirm-interrupt" => 0,
                "progress-message"  => 1,
                "progress-interval" => 10,
                "seconds"           => $delay_time,
                "use-logging"       => 1,
            }
        );
        if ($rc == 1) {
            General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
            $rc = 0;
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
sub Fallback001_P928S99 {

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

This playlist check the deployment status of the Application software.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
