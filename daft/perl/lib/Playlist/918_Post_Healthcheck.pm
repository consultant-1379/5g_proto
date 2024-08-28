package Playlist::918_Post_Healthcheck;

################################################################################
#
#  Author   : everhel, eustone
#
#  Revision : 1.61
#  Date     : 2024-05-29 14:00:59
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2023
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

use ADP::Kubernetes_Operations;
use General::Logging;
use General::Message_Generation;
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

    $rc = General::Playlist_Operations::execute_step( \&Post_Healthcheck_P918S01, \&Fallback001_P918S99 );
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
sub Post_Healthcheck_P918S01 {

    my $rc = 0;

    # Set default values
    $::JOB_PARAMS{'HEALTH_CHECK_NODE_HEALTH_RESULT'} = "SKIPPED";
    $::JOB_PARAMS{'HEALTH_CHECK_PODFAILURE_ALARM_RESULT'} = "SKIPPED";
    $::JOB_PARAMS{'HEALTH_CHECK_POD_STATUS_RESULT'} = "SKIPPED";
    $::JOB_PARAMS{'HEALTH_CHECK_RESULT'} = "SKIPPED";

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Pod_Status_P918S01T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_PodFailure_Alarm_To_Clear_P918S01T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Node_Health_P918S01T03 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Overall_Health_Check_Result_P918S01T04 } );
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
    sub Check_Pod_Status_P918S01T01 {

        my $delay_time = 0;
        my $max_attempts = 1;
        my $max_time = 0;
        my $repeated_checks = 0;
        my @exclude_ready_list = ();
        my @exclude_status_list = ();
        my $message = "";
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        if ($::JOB_PARAMS{'JOBTYPE'} =~ /^(CONFIG_MANAGEMENT|DEPLOY|HEALTHCHECK|MAINTAINABILITY_TEST_.+|ROBUSTNESS_TEST_.+|ROLLBACK|SCALING|TIME_SHIFT_TEST|UPGRADE)$/) {
            # Ignore the following POD which is constantly respawned and for a
            # short time the status is not Completed.
            @exclude_status_list = (
                "eric-data-search-engine-curator.*",
                "eric-log-transformer.*",
            );

            if ($::JOB_PARAMS{'JOBTYPE'} eq "CONFIG_MANAGEMENT") {
                # After a completed configuration should all pod's have x/x ready status
                # except for the following which constantly is being respawned and might
                # not always be in Completed status.
                @exclude_ready_list = (
                    "eric-data-search-engine-curator.*",
                    "eric-log-transformer.*",
                );
                # We wait a maximum of POD_STATUS_TIMEOUT seconds for the PODs to come up
                $delay_time = 10;
                $max_attempts = 0;
                $max_time = exists $::JOB_PARAMS{'POD_STATUS_TIMEOUT'} ? $::JOB_PARAMS{'POD_STATUS_TIMEOUT'} : 10;
                $repeated_checks = 0;
            } elsif ($::JOB_PARAMS{'JOBTYPE'} eq "DEPLOY") {
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
            } elsif ($::JOB_PARAMS{'JOBTYPE'} =~ /^(ROLLBACK|SCALING|UPGRADE)$/) {
                # For job types  ROLLBACK and UPGRADE we assume that all pod's should
                # come up and be ready with x/x within a certain time limit except for the
                # following POD's in status Completed where the READY status is not all
                # up because these POD's are just Jobs and not normal pods.
                @exclude_ready_list = (
                    "eric-data-search-engine-curator.*",
                    "eric-log-transformer.*",
                );
                # We wait a maximum of POD_STATUS_TIMEOUT seconds for the PODs to come up
                $delay_time = 10;
                $max_attempts = 0;
                $max_time = exists $::JOB_PARAMS{'POD_STATUS_TIMEOUT'} ? $::JOB_PARAMS{'POD_STATUS_TIMEOUT'} : 10;
                $repeated_checks = 0;
            } elsif ($::JOB_PARAMS{'JOBTYPE'} =~ /^TIME_SHIFT_TEST$/) {
                # For other job types like HEALTHCHECK we assume that all pod's should
                # be already up and ready with x/x except for the following POD's in
                # status Completed where the READY status is not all up because these
                # POD's are just Jobs and not normal pods.
                @exclude_ready_list = (
                    "eric-data-search-engine-curator.*",
                    "eric-log-transformer.*",
                    "eric-data-wide-column-database-cd-tls-restarter-.*",
                    "eric-bsf-wcdb-cd-tls-restarter-.*",
                );
                push @exclude_status_list, "eric-data-wide-column-database-cd-tls-restarter-.*";
                push @exclude_status_list, "eric-bsf-wcdb-cd-tls-restarter-.*";
                # We expect all pods to be up on first check
                $delay_time = 0;
                $max_attempts = 1;
                $max_time = 0;
                $repeated_checks = 0;
            } else {
                # For other job types like HEALTHCHECK we assume that all pod's should
                # be already up and ready with x/x except for the following POD's in
                # status Completed where the READY status is not all up because these
                # POD's are just Jobs and not normal pods.
                @exclude_ready_list = (
                    "eric-data-search-engine-curator.*",
                    "eric-log-transformer.*",
                );
                # We expect all pods to be up on first check
                $delay_time = 0;
                $max_attempts = 1;
                $max_time = 0;
                $repeated_checks = 0;
            }

            if ($max_time > 0) {
                General::Logging::log_user_message("Checking every 10 seconds that all pods in $sc_namespace namespace are up for a maximum of $max_time seconds.");
            } else {
                General::Logging::log_user_message("Checking that all pods in $sc_namespace namespace are up.");
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
                    "namespace"                                 => $sc_namespace,
                    "pod-exclude-ready-list"                    => \@exclude_ready_list,
                    "pod-exclude-status-list"                   => \@exclude_status_list,
                    "repeated-checks"                           => $repeated_checks,
                    "return-failed-pods"                        => \@result,
                    "wanted-ready"                              => "same",
                    "wanted-status"                             => "up",
                }
            );
            if ($rc == 0) {
                General::Logging::log_user_message("POD status check was successful");
                push @::JOB_STATUS, '(/) Post POD status check was successful';
                # Mark the this health check as successful
                $::JOB_PARAMS{'HEALTH_CHECK_POD_STATUS_RESULT'} = "SUCCESSFUL";
            } else {
                # Mark the this health check as failed in case the playlist want to continue executing but then fail at the end.
                $::JOB_PARAMS{'HEALTH_CHECK_POD_STATUS_RESULT'} = "FAILED";

                $message = join("\n",@result);
                if ($::JOB_PARAMS{"IGNORE_FAILED_HEALTH_CHECK"} =~ /^yes$/i) {
                    General::Logging::log_user_warning_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n$message\nBut ignored because IGNORE_FAILED_HEALTH_CHECK=yes.");
                    # Ignore the error and continue like everything was OK
                    $rc = 0;
                } else {
                    General::Logging::log_user_error_message("One or more pods are not in expected STATUS 'Running' or 'Completed' or not READY:\n$message");
                }

                if ($rc == 0) {
                    push @::JOB_STATUS, '(/) Post POD status check was successful';
                } else {
                    push @::JOB_STATUS, '(x) Post POD status check failed';
                    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
                }
            }

        } else {
            # Other JOBTYPE where this POD check makes no sence.
            General::Logging::log_user_message("POD status check skipped for job type $::JOB_PARAMS{'JOBTYPE'}");
            push @::JOB_STATUS, "(-) Post POD status check not needed for job type $::JOB_PARAMS{'JOBTYPE'}";
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
    sub Wait_For_PodFailure_Alarm_To_Clear_P918S01T02 {

        my @alarms = ();
        my $max_time = $::JOB_PARAMS{'POD_STATUS_TIMEOUT'};
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $stop_time = time() + $max_time;
        my $wait;

        General::Logging::log_user_message(sprintf "Checking every 10 seconds that there are no PodFailure alarms in $sc_namespace namespace for a maximum of %d seconds.", ($stop_time - time()));
        while (1) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "perl $::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/alarm_check.pl " .
                                       "--alarm-name=PodFailure " .
                                       ($::JOB_PARAMS{'KUBECONFIG'} ne "" ? "--kubeconfig=$::JOB_PARAMS{'KUBECONFIG'} " : "") .
                                       "--log_file $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/all.log " .
                                       "--namespace $sc_namespace " .
                                       "--use_allowlist ",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0) {
                # No PodFailure alarm, or it was in the allow list

                # Mark the this health check as successful
                $::JOB_PARAMS{'HEALTH_CHECK_PODFAILURE_ALARM_RESULT'} = "SUCCESSFUL";
                last;
            }

            # There are one or more PodFailure alarms, wait a bit and then check again
            if ($stop_time <= time()) {
                # Display the result in case of warnings
                General::OS_Operations::write_last_temporary_output_to_progress();

                # Mark the this health check as failed in case the playlist want to continue executing but then fail at the end.
                $::JOB_PARAMS{'HEALTH_CHECK_PODFAILURE_ALARM_RESULT'} = "FAILED";

                if ($::JOB_PARAMS{"IGNORE_FAILED_HEALTH_CHECK"} =~ /^yes$/i) {
                    General::Logging::log_user_warning_message("There are still PodFailure alarms, maximum wait time has expired.\nBut ignored because IGNORE_FAILED_HEALTH_CHECK=yes.");
                    $rc = 0;
                    last;
                } else {
                    General::Logging::log_user_error_message("There are still PodFailure alarms, maximum wait time has expired.");
                    push @::JOB_STATUS, "(x) Post PodFailure alarm check failed";
                    return 1;
                }
            }
            $wait = $stop_time - time();
            if ($wait > 10) {
                $wait = 10;
            }

            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "progress-message"  => 0,
                    "seconds"           => $wait,
                    "use-logging"       => 1,
                }
            );
            if ($rc == 1) {
                General::Logging::log_user_warning_message("Wait interrupted by user pressing CTRL-C");
                $rc = 0;
                last;
            }
        }

        General::Logging::log_user_message("PodFailure alarm check was successful");
        push @::JOB_STATUS, "(/) Post PodFailure alarm check was successful";

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
    sub Check_Node_Health_P918S01T03 {

        my $application_type = exists $::JOB_PARAMS{'APPLICATION_TYPE'} ? $::JOB_PARAMS{'APPLICATION_TYPE'} : "";
        my $cmyp_password = exists $::JOB_PARAMS{'CMYP_PASSWORD'} ? $::JOB_PARAMS{'CMYP_PASSWORD'} : "";
        my $cmyp_user = exists $::JOB_PARAMS{'CMYP_USER'} ? $::JOB_PARAMS{'CMYP_USER'} : "";
        my $command = "";
        my $message;
        my $own_registry_url = exists $::JOB_PARAMS{'SC_REGISTRY_URL'} ? $::JOB_PARAMS{'SC_REGISTRY_URL'} : "";
        my $rc;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $skip_kpi = exists $::JOB_PARAMS{'SKIP_KPI_COLLECTION'} ? $::JOB_PARAMS{'SKIP_KPI_COLLECTION'} : "no";
        my $time_threshold = time() - $::JOB_PARAMS{'_JOB_STARTTIME'} + 1;     # 1 second before starting the job
        my $used_helm_version = $::JOB_PARAMS{'HELM_VERSION'};

        $command  = "perl $::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/system_health_check.pl";
        $command .= " --namespace $sc_namespace";
        $command .= " --log_file $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/all.log";
        $command .= " --progress_type=none";
        $command .= " --used_helm_version $used_helm_version";
        $command .= " --no_color"                                       if ($::color_scheme eq "no");
        $command .= " --kubeconfig $::JOB_PARAMS{'KUBECONFIG'}"         if ($::JOB_PARAMS{'KUBECONFIG'} ne "");
        $command .= " --own_registry_url $own_registry_url"             if ($own_registry_url ne "");

        if (exists $::JOB_PARAMS{'HEALTH_CHECK_FILE'} && $::JOB_PARAMS{'HEALTH_CHECK_FILE'} ne "" && -f $::JOB_PARAMS{'HEALTH_CHECK_FILE'}) {
            # All checks and variables to be used is coming from the file
            $command .= " --check_file=$::JOB_PARAMS{'HEALTH_CHECK_FILE'}";
            # We delete the variable again to make sure that the next time this playlist is called again
            delete $::JOB_PARAMS{'HEALTH_CHECK_FILE'};
        } else {
            # If $cmyp_user not yet set, try to read it from the network config parameters
            if ($cmyp_user eq "") {
                # Find the used password for the 'expert' or 'admin' user.
                for my $key (keys %::NETWORK_CONFIG_PARAMS) {
                    if ($::JOB_PARAMS{'JOBTYPE'} =~ /^(DEPLOY)$/) {
                        if ($::NETWORK_CONFIG_PARAMS{$key}{'user'} eq "admin") {
                            $cmyp_user = $::NETWORK_CONFIG_PARAMS{$key}{'user'};
                            $cmyp_password = $::NETWORK_CONFIG_PARAMS{$key}{'password'};
                            last;
                        }
                    } else {
                        if ($::NETWORK_CONFIG_PARAMS{$key}{'user'} eq "expert") {
                            $cmyp_user = $::NETWORK_CONFIG_PARAMS{$key}{'user'};
                            $cmyp_password = $::NETWORK_CONFIG_PARAMS{$key}{'password'};
                            last;
                        }
                    }
                }

                if ($cmyp_user ne "") {
                    General::Logging::log_user_message("cmyp_user = $cmyp_user\n");
                }
            }

            # Set common variable values for all job types
            $command .= " --variables pod_restarts_time_threshold=$time_threshold";
            $command .= " --variables core_dump_fetch=yes";
            $command .= " --variables core_dump_directory=$::JOB_PARAMS{'_JOB_LOG_DIR'}/core_dumps";
            $command .= " --variables core_dump_time_threshold=$time_threshold";
            $command .= " --variables ignore_failed_pm_files=yes";          # This should be removed when the checks are working
            $command .= " --variables ignore_failed_replicasets=yes";       # This should be removed when the checks are working
            $command .= " --variables ignore_failed_top_nodes=yes";
            $command .= " --variables ignore_failed_top_pods=yes";
            $command .= " --variables logs_details_directory=$::JOB_PARAMS{'_JOB_LOG_DIR'}/log_details/post";
            $command .= " --variables skip_alarm_history=yes"               if ($cmyp_user eq "");
            $command .= " --variables skip_kpi=yes"                         if ($cmyp_user eq "");
            $command .= " --variables kpi_details_at_success=1"             if ($cmyp_user ne "");
            $command .= " --variables skip_kpi=yes"                         if ($skip_kpi eq "yes");

            if ($::JOB_PARAMS{'JOBTYPE'} eq "DEPLOY") {
                $command .= " --group post-deployment";
                $command .= " --variables skip_pm_files=yes";               # Since no PM files has been created directly after a deployment
                $command .= " --variables skip_pod_ready_status=yes";       # Since we already checked it in previous task
                $command .= " --variables skip_pod_start_restarts=yes";     # Since all pods have started during the deployment so they would be reproted as a warning
                if (exists $::JOB_PARAMS{'DAY0_PASSWORD'} && $::JOB_PARAMS{'DAY0_PASSWORD'} ne "") {
                    $command .= sprintf " --variables cmyp_password='%s'", $::JOB_PARAMS{'DAY0_PASSWORD'};
                } else {
                    $command .= " --variables cmyp_password='rootroot'";
                }
                $command .= " --variables cmyp_user=admin";
            } elsif ($::JOB_PARAMS{'JOBTYPE'} eq "UPGRADE") {
                $command .= " --group post-upgrade";
                $command .= " --variables skip_pod_ready_status=yes";       # Since we already checked it in previous task
                $command .= " --variables cmyp_password='$cmyp_password'"       if ($cmyp_password ne "");
                $command .= " --variables cmyp_user=$cmyp_user"                 if ($cmyp_user ne "");
            } elsif ($::JOB_PARAMS{'JOBTYPE'} eq "CONFIG_MANAGEMENT") {
                $command .= " --group verification";
                $command .= " --variables skip_registry_pod_images=yes";    # Since we have not changed the registry
                $command .= " --variables cmyp_password='$cmyp_password'"       if ($cmyp_password ne "");
                $command .= " --variables cmyp_user=$cmyp_user"                 if ($cmyp_user ne "");
            } elsif ($::JOB_PARAMS{'JOBTYPE'} eq "ECCD_INSTALLATION") {
                General::Logging::log_user_message("Post System health check skipped for job type $::JOB_PARAMS{'JOBTYPE'}");
                push @::JOB_STATUS, "(-) Post System health check not needed for job type $::JOB_PARAMS{'JOBTYPE'}";
                return General::Playlist_Operations::RC_TASKOUT;
            } else {
                $command .= " --group verification";
                $command .= " --variables skip_pod_ready_status=yes";        # Since we already checked it in previous task
                $command .= " --variables skip_registry_pod_images=yes";     # Since we have not changed the registry
                $command .= " --variables cmyp_password='$cmyp_password'"       if ($cmyp_password ne "");
                $command .= " --variables cmyp_user=$cmyp_user"                 if ($cmyp_user ne "");
            }
        }

        #
        # Execute health check
        #
        General::Logging::log_user_message("Checking node health.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            General::Logging::log_user_message("Health Check was successful");
            # Mark the this health check as successful
            $::JOB_PARAMS{'HEALTH_CHECK_NODE_HEALTH_RESULT'} = "SUCCESSFUL";
        } elsif ($rc == 2) {
            # Display the result in case of warnings
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_warning_message("Health Check reported warnings but treated as successful");
            # Mark the this health check as successful
            $::JOB_PARAMS{'HEALTH_CHECK_NODE_HEALTH_RESULT'} = "SUCCESSFUL";
            $rc = 0;
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            # Mark the this health check as failed in case the playlist want to continue executing but then fail at the end.
            $::JOB_PARAMS{'HEALTH_CHECK_NODE_HEALTH_RESULT'} = "FAILED";

            if ($::JOB_PARAMS{"IGNORE_FAILED_HEALTH_CHECK"} =~ /^yes$/i) {
                General::Message_Generation::print_message_box(
                    {
                        "messages"      => [
                            "",
                            "Health Check failed.",
                            "But ignored because IGNORE_FAILED_HEALTH_CHECK=yes.",
                            "",
                        ],
                        "align-text"    => "center",
                        "type"          => "warning",
                        "return-output" => \$message,
                    }
                );
                # Ignore the error and continue like everything was OK
                $rc = 0;
            } else {
                General::Message_Generation::print_message_box(
                    {
                        "messages"      => [
                            "",
                            "Health Check failed",
                            "",
                        ],
                        "align-text"    => "left",
                        "type"          => "error",
                        "return-output" => \$message,
                    }
                );
            }
            if ($rc == 0) {
                General::Logging::log_user_warning_message($message);
            } else {
                General::Logging::log_user_error_message($message);
            }
        }

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Post System health check was successful';
        } else {
            push @::JOB_STATUS, '(x) Post System health check was unsuccessful';
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
    sub Check_Overall_Health_Check_Result_P918S01T04 {

        if ($::JOB_PARAMS{'HEALTH_CHECK_NODE_HEALTH_RESULT'} eq "FAILED" ||
            $::JOB_PARAMS{'HEALTH_CHECK_PODFAILURE_ALARM_RESULT'} eq "FAILED" ||
            $::JOB_PARAMS{'HEALTH_CHECK_POD_STATUS_RESULT'} eq "FAILED") {
            # There is at least one failed check, mark the whoel health check as failed

            $::JOB_PARAMS{'HEALTH_CHECK_RESULT'} = "FAILED";
        } else {
            # All are successful or skipped, mark it as successful
            $::JOB_PARAMS{'HEALTH_CHECK_RESULT'} = "SUCCESSFUL";
        }

        return 0;
    }
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P918S99 {

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    $::JOB_PARAMS{'HEALTH_CHECK_RESULT'} = "FAILED";

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

This playlist performs a health check of the node after finishing a specific
job to make sure the node is still in an acceptable state.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
