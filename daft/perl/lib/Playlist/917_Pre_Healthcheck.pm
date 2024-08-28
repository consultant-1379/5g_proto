package Playlist::917_Pre_Healthcheck;

################################################################################
#
#  Author   : everhel, eustone
#
#  Revision : 1.48
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

    $rc = General::Playlist_Operations::execute_step( \&Pre_Healthcheck_P917S01, \&Fallback001_P917S99 );
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
sub Pre_Healthcheck_P917S01 {

    my $rc;

    # Set default values
    $::JOB_PARAMS{'HEALTH_CHECK_NODE_HEALTH_RESULT'} = "SKIPPED";
    $::JOB_PARAMS{'HEALTH_CHECK_POD_STATUS_RESULT'} = "SKIPPED";
    $::JOB_PARAMS{'HEALTH_CHECK_RESULT'} = "SKIPPED";

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Node_Health_P917S01T01, "dry-run-job" => $::JOB_PARAMS{'DRY_RUN'}, "dry-run-task" => 1 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Pod_Status_P917S01T02, "dry-run-job" => $::JOB_PARAMS{'DRY_RUN'}, "dry-run-task" => 1 } );
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
    sub Check_Node_Health_P917S01T01 {

        my $application_type = exists $::JOB_PARAMS{'APPLICATION_TYPE'} ? $::JOB_PARAMS{'APPLICATION_TYPE'} : "";
        my $cmyp_password = exists $::JOB_PARAMS{'CMYP_PASSWORD'} ? $::JOB_PARAMS{'CMYP_PASSWORD'} : "";
        my $cmyp_user = exists $::JOB_PARAMS{'CMYP_USER'} ? $::JOB_PARAMS{'CMYP_USER'} : "";
        my $color_info = $::color_scheme eq "no" ? "--no_color " : "";
        my $command = "";
        my $message;
        my $own_registry_url = exists $::JOB_PARAMS{'SC_REGISTRY_URL'} ? $::JOB_PARAMS{'SC_REGISTRY_URL'} : "";
        my $rc = 0;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $skip_kpi = exists $::JOB_PARAMS{'SKIP_KPI_COLLECTION'} ? $::JOB_PARAMS{'SKIP_KPI_COLLECTION'} : "no";
        my $time_threshold = time() - $::JOB_PARAMS{'_JOB_STARTTIME'} + 1;     # 1 second before starting the job
        my $used_helm_version = $::JOB_PARAMS{'HELM_VERSION'};

        $command  = "perl $::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/system_health_check.pl";
        $command .= " --log_file $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/all.log";
        $command .= " --progress_type=none";
        $command .= " --used_helm_version $used_helm_version";
        $command .= " --no_color"                                       if ($::color_scheme eq "no");
        $command .= " --kubeconfig $::JOB_PARAMS{'KUBECONFIG'}"         if ($::JOB_PARAMS{'KUBECONFIG'} ne "");
        $command .= " --own_registry_url $own_registry_url"             if ($own_registry_url ne "");

        if (exists $::JOB_PARAMS{'HEALTH_CHECK_FILE'} && $::JOB_PARAMS{'HEALTH_CHECK_FILE'} ne "" && -f $::JOB_PARAMS{'HEALTH_CHECK_FILE'}) {
            # All checks and variables to be used is coming from the file
            $command .= " --check_file=$::JOB_PARAMS{'HEALTH_CHECK_FILE'}";
            $command .= " --namespace $sc_namespace";
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
            $command .= " --variables skip_pod_ready_status=yes";
            $command .= " --variables logs_details_directory=$::JOB_PARAMS{'_JOB_LOG_DIR'}/log_details/pre";
            $command .= " --variables cmyp_password='$cmyp_password'"       if ($cmyp_password ne "");
            $command .= " --variables cmyp_user=$cmyp_user"                 if ($cmyp_user ne "");
            $command .= " --variables skip_alarm_history=yes"               if ($cmyp_user eq "");
            $command .= " --variables skip_kpi=yes"                         if ($cmyp_user eq "");
            $command .= " --variables kpi_details_at_success=1"             if ($cmyp_user ne "");
            $command .= " --variables skip_kpi=yes"                         if ($skip_kpi eq "yes");

            if ($::JOB_PARAMS{'JOBTYPE'} eq "CONFIG_MANAGEMENT") {
                $command .= " --group post-deployment";
                $command .= " --namespace $sc_namespace";
                $command .= " --variables skip_component_info=yes";
                $command .= " --variables ignore_failed_top_pods=yes";
            } elsif ($::JOB_PARAMS{'JOBTYPE'} eq "DEPLOY") {
                $command .= " --group pre-deployment";
                $command .= " --variables skip_component_info=yes";
            } elsif ($::JOB_PARAMS{'JOBTYPE'} eq "UPGRADE") {
                $command .= " --group pre-upgrade";
                $command .= " --namespace $sc_namespace";
                $command .= " --variables ignore_failed_top_pods=yes";
            } elsif ($::JOB_PARAMS{'JOBTYPE'} eq "ECCD_INSTALLATION") {
                General::Logging::log_user_message("Pre System health check skipped for job type $::JOB_PARAMS{'JOBTYPE'}");
                push @::JOB_STATUS, "(-) Pre System health check not needed for job type $::JOB_PARAMS{'JOBTYPE'}";
                return General::Playlist_Operations::RC_TASKOUT;
            } else {
                # E.g. HEALTHCHECK and ROBUSTNESS_TEST
                $command .= " --group verification";
                $command .= " --namespace $sc_namespace";
                $command .= " --variables ignore_failed_top_pods=yes";
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
            push @::JOB_STATUS, '(/) Pre System health check was successful';
        } else {
            push @::JOB_STATUS, '(x) Pre System health check was unsuccessful';
            $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        }

        if ($::JOB_PARAMS{'DRY_RUN'} eq "yes") {
            return 0;
        } else {
            return $rc;
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
    sub Check_Pod_Status_P917S01T02 {

        my @exclude_ready_list = ();
        my @exclude_status_list = ();
        my $message = "";
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        if ($::JOB_PARAMS{'JOBTYPE'} =~ /^(CONFIG_MANAGEMENT|HEALTHCHECK|MAINTAINABILITY_TEST_.+|ROBUSTNESS_TEST_.+|SCALING|TIME_SHIFT_TEST|UPGRADE)$/) {
            # Ignore the following POD which is constantly respawned and for a
            # short time the status is not Completed.
            @exclude_status_list = (
                "eric-data-search-engine-curator.*",
            );

            if ($::JOB_PARAMS{'JOBTYPE'} eq "CONFIG_MANAGEMENT") {
                # Ignore the following POD's in status Completed or Running where
                # the READY status is not all up because of missing configuration.
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
            } else {
                # Ignore the following POD's in status Completed where the READY status
                # is not all up because these POD's are just Jobs and not normal pods.
                @exclude_ready_list = (
                    "eric-data-search-engine-curator.*",
                    "eric-log-transformer.*",
                );
            }
            # We expect that all the pod's are up on the first check
            General::Logging::log_user_message("Checking that all pods in $sc_namespace namespace are up.");
            $rc = ADP::Kubernetes_Operations::check_pod_status(
                {
                    "all-namespaces"                            => 0,
                    "debug-messages"                            => 0,
                    "delay-time"                                => 0,
                    "ignore-ready-check-for-completed-status"   => 1,
                    "hide-output"                               => 1,
                    "max-attempts"                              => 1,
                    "max-time"                                  => 0,
                    "namespace"                                 => $sc_namespace,
                    "pod-exclude-ready-list"                    => \@exclude_ready_list,
                    "pod-exclude-status-list"                   => \@exclude_status_list,
                    "repeated-checks"                           => 0,
                    "return-failed-pods"                        => \@result,
                    "wanted-ready"                              => "same",
                    "wanted-status"                             => "up",
                }
            );
            if ($rc == 0) {
                General::Logging::log_user_message("POD status check was successful");
                push @::JOB_STATUS, '(/) Pre POD status check was successful';
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
                    push @::JOB_STATUS, '(/) Pre POD status check was successful';
                } else {
                    push @::JOB_STATUS, '(x) Pre POD status check failed';
                    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
                }
            }

        } else {
            # Other JOBTYPE e.g. DEPLOY or ROLLBACK where this POD check makes no sence.
            General::Logging::log_user_message("POD status check skipped for job type $::JOB_PARAMS{'JOBTYPE'}");
            push @::JOB_STATUS, "(-) Pre POD status check not needed for job type $::JOB_PARAMS{'JOBTYPE'}";
        }

        if ($::JOB_PARAMS{'DRY_RUN'} eq "yes") {
            return 0;
        } else {
            return $rc;
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
                # Mark the this health check as failed in case the playlist want to continue executing but then fail at the end.
                $::JOB_PARAMS{'HEALTH_CHECK_POD_STATUS_RESULT'} = "FAILED";
    # -----------------------------------------------------------------------------
    sub Check_Overall_Health_Check_Result_P917S01T03 {

        if ($::JOB_PARAMS{'HEALTH_CHECK_NODE_HEALTH_RESULT'} eq "FAILED" ||
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
sub Fallback001_P917S99 {

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

This playlist performs a health check of the node before starting a specific
job to make sure the node is in an acceptable state.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
