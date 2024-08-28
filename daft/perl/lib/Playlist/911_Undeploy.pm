package Playlist::911_Undeploy;

################################################################################
#
#  Author   : everhel, eustone
#
#  Revision : 1.60
#  Date     : 2024-05-23 13:17:38
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
use version;

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    usage
    );

# Used Perl package files
use ADP::Kubernetes_Operations;
use File::Basename qw(dirname basename);
use General::File_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;

my $debug_command = "";         # Change this value from "echo " to "" when ready to test on real system
my $ignore_failed_removal = 1;  # Change this to 0 if we should keep old behavior to fail the undeploy
                                # when some resources like PVC cannot be deleted for some reason.
                                # Usually it's not a problem to ignore these failure because when we
                                # remove the namespace then it will properly delete any pending resources.
my $keep_some_secrets = 0;      # Change this to 1 if some secrets should be kept

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

    $rc = General::Playlist_Operations::execute_step( \&Undeploy_SC_HEML3_P911S01, \&Fallback001_P911S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Undeploy_SC_HEML2_P911S02, \&Fallback001_P911S99 );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'ONLY_REMOVE_NAMESPACE'} eq "no") {
        if ($::JOB_PARAMS{'HELM3_EXISTS'} eq "yes" || $::JOB_PARAMS{'HELM2_EXISTS'} eq "yes") {
            # Only perform these tasks if a HELM3 or HELM2 chart exists, if both does not exist
            # it could be because of a failed removal of the helm chart which in such case might
            # cause any of these steps to fail. Instead we simply remove the namespace if it
            # exists since that will "properly" remove the API resources that are still tied to
            # the namespace.

            $rc = General::Playlist_Operations::execute_step( \&Remove_PVCs_P911S03, \&Fallback001_P911S99 );
            return $rc if $rc < 0;

            $rc = General::Playlist_Operations::execute_step( \&Remove_Jobs_P911S04, \&Fallback001_P911S99 );
            return $rc if $rc < 0;

            $rc = General::Playlist_Operations::execute_step( \&Remove_Secrets_P911S05, \&Fallback001_P911S99 );
            return $rc if $rc < 0;
        }
    }

    $rc = General::Playlist_Operations::execute_step( \&Remove_Namespace_P911S06, \&Fallback001_P911S99 );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_UNDEPLOY_CRD'} eq "no") {

        $rc = General::Playlist_Operations::execute_step( \&Check_For_Existing_SC_Deployments_P911S07, \&Fallback001_P911S99 );
        return $rc if $rc < 0;

        if ($::JOB_PARAMS{'P911_CRD_REMOVAL_ALLOWED'} eq "yes") {

            $rc = General::Playlist_Operations::execute_step( \&Remove_CRD_Charts_HELM3_P911S08, \&Fallback001_P911S99 );
            return $rc if $rc < 0;

            $rc = General::Playlist_Operations::execute_step( \&Remove_CRD_Charts_HEML2_P911S09, \&Fallback001_P911S99 );
            return $rc if $rc < 0;

            $rc = General::Playlist_Operations::execute_step( \&Remove_CRDs_P911S10, \&Fallback001_P911S99 );
            return $rc if $rc < 0;
        }
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
sub Undeploy_SC_HEML3_P911S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Deployment_Exists_HELM3_P911S01T01 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'HELM3_EXISTS'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Undeploy_HELM3_P911S01T02 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Removal_HELM3_P911S01T03 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'OTHER_HELM_RELEASES'} ne "") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Undeploy_Other_Releases_HELM3_P911S01T04 } );
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
    sub Check_Deployment_Exists_HELM3_P911S01T01 {

        my $chart_name;
        my $deployment_found = 0;
        my $deployment_status;
        my $helm_executable = exists $::JOB_PARAMS{'HELM3_EXECUTABLE'} ? $::JOB_PARAMS{'HELM3_EXECUTABLE'} : "";
        my $rc = 0;
        my $release_name;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        my @result;

        $::JOB_PARAMS{'HELM3_EXISTS'} = "no";
        $::JOB_PARAMS{'SC_RELEASE_VERSION'} = "" unless (exists $::JOB_PARAMS{'SC_RELEASE_VERSION'});
        $::JOB_PARAMS{'SC_RELEASE_BUILD'} = "" unless (exists $::JOB_PARAMS{'SC_RELEASE_BUILD'});
        $::JOB_PARAMS{'OTHER_HELM_RELEASES'} = "";
        $::JOB_PARAMS{'NEW_SC_HELM_RELEASES'} = "";

        if ($helm_executable eq "") {
            General::Logging::log_user_message("Helm version 3 not installed, skip this step");
            return General::Playlist_Operations::RC_STEPOUT;
        }

        General::Logging::log_user_message("List SC deployment");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${helm_executable} list --all --namespace $sc_namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get deployments");
        } else {
            for (@result) {
                if ($_ =~ /^$sc_release_name\s+$sc_namespace\s+\d+\s+.+\s+(\S+)\s+eric-sc-umbrella-(\d+\.\d+\.\d+)(\S+)/) {
                    #NAME    NAMESPACE       REVISION        UPDATED                                 STATUS                  CHART                           APP VERSION
                    #eric-sc eiffelesc       1               2021-05-27 07:41:12.499785339 +0000 UTC deployed                eric-sc-umbrella-1.3.1+314
                    #  or
                    #eric-sc eiffelesc       3               2021-05-26 08:06:02.655940685 +0000 UTC pending-rollback        eric-sc-umbrella-1.3.0+61
                    $deployment_found = 1;
                    $deployment_status = $1;
                    $::JOB_PARAMS{'SC_RELEASE_VERSION'} = $2;
                    $::JOB_PARAMS{'SC_RELEASE_BUILD'} = $3;
                    if ($deployment_status ne "deployed") {
                        # Since the deployment to be removed is in some strange state we skip over all the individual
                        # removal of PVC, Job and secrets and just delete the namespace.
                        General::Logging::log_user_warning_message("The 'STATUS' for the following release is not 'deployed' so NO attempt to remove PVC, Job and Secrets will be done:\n$_");
                        $::JOB_PARAMS{'ONLY_REMOVE_NAMESPACE'} = "yes";
                    }
                } elsif ($_ =~ /^(\S+)\s+$sc_namespace\s+\d+\s+.+\s+(\S+)\s+(eric-.+)-\d+\.\d+\.\d+\S*/) {
                    #NAME                            NAMESPACE       REVISION        UPDATED                                 STATUS          CHART                                   APP VERSION
                    #eric-chfsim-eiffelesc           eiffelesc       1               2022-06-15 05:16:50.801079033 +0000 UTC deployed        eric-chfsim-1.8.0-66                    1.8.0-66
                    #eric-influxdb-eiffelesc         eiffelesc       1               2022-06-15 05:16:41.380712178 +0000 UTC deployed        eric-influxdb-1.8.0-h14fc561317         1.8.0-h14fc561317
                    #eric-k6-eiffelesc               eiffelesc       1               2022-06-15 05:14:15.463664057 +0000 UTC deployed        eric-k6-1.8.0-h14fc561317               1.8.0-h14fc561317
                    #eric-nrfsim-eiffelesc           eiffelesc       1               2022-06-15 05:17:00.122904038 +0000 UTC deployed        eric-nrfsim-1.8.0-66
                    #eric-seppsim-eiffelesc          eiffelesc       1               2022-06-15 05:17:15.96349621 +0000 UTC  deployed        eric-seppsim-1.8.0-66
                    #eric-vtaprecorder-eiffelesc     eiffelesc       1               2022-06-15 05:16:31.166302816 +0000 UTC deployed        eric-vtaprecorder-1.8.0-h14fc561317
                    #    or
                    #NAME                            NAMESPACE       REVISION        UPDATED                                 STATUS          CHART                                   APP VERSION
                    #eric-cloud-native-base          eiffeldsc       1               2022-09-06 10:56:58.098362891 +0000 UTC deployed        eric-cloud-native-base-58.8.1           58.8.1
                    #eric-cloud-native-nf-additions  eiffeldsc       1               2022-09-06 10:57:27.002405092 +0000 UTC deployed        eric-cloud-native-nf-additions-23.3.0   23.3.0
                    #eric-dsc                        eiffeldsc       1               2022-09-06 10:57:31.989517921 +0000 UTC deployed        eric-dsc-1.9.0-1-h113a35e               1.9.0-936
                    $release_name = $1;
                    $deployment_status = $2;
                    $chart_name = $3;
                    if ($chart_name =~ /^eric-(cloud-native-base|cloud-native-nf-additions|dsc|sc-bsf|sc-cs|sc-scp|sc-sepp)$/) {
                        $deployment_found = 1;
                        $::JOB_PARAMS{'NEW_SC_HELM_RELEASES'} .= "$release_name ";
                    } else {
                        $::JOB_PARAMS{'OTHER_HELM_RELEASES'} .= "$release_name ";
                    }
                    if ($deployment_status ne "deployed") {
                        # Since the deployment to be removed is in some strange state we skip over all the individual
                        # removal of PVC, Job and secrets and just delete the namespace.
                        General::Logging::log_user_warning_message("The 'STATUS' for the following release is not 'deployed' so NO attempt to remove PVC, Job and Secrets will be done:\n$_");
                        $::JOB_PARAMS{'ONLY_REMOVE_NAMESPACE'} = "yes";
                    }
                }
            }
            $::JOB_PARAMS{'NEW_SC_HELM_RELEASES'} =~ s/\s+$//;
            $::JOB_PARAMS{'OTHER_HELM_RELEASES'} =~ s/\s+$//;
            if ($deployment_found == 0 || !@result) {
                General::Logging::log_user_message("No SC deployments found in namespace $sc_namespace");
                if ($::JOB_PARAMS{'OTHER_HELM_RELEASES'} ne "") {
                    General::Logging::log_user_message("The following releases also found in namespace $sc_namespace:\n" . join "\n", split /\s+/, $::JOB_PARAMS{'OTHER_HELM_RELEASES'});
                }
            } elsif ($::JOB_PARAMS{'NEW_SC_HELM_RELEASES'} ne "") {
                General::Logging::log_user_message("The following SC releases also found in namespace $sc_namespace:\n" . join "\n", split /\s+/, $::JOB_PARAMS{'NEW_SC_HELM_RELEASES'});
                $::JOB_PARAMS{'SC_RELEASE_NAME'} = $::JOB_PARAMS{'NEW_SC_HELM_RELEASES'};
                if ($::JOB_PARAMS{'OTHER_HELM_RELEASES'} ne "") {
                    General::Logging::log_user_message("The following other releases also found in namespace $sc_namespace:\n" . join "\n", split /\s+/, $::JOB_PARAMS{'OTHER_HELM_RELEASES'});
                }
                $::JOB_PARAMS{'HELM3_EXISTS'} = "yes";
            } else {
                General::Logging::log_user_message("SC deployment $sc_release_name found in namespace $sc_namespace");
                if ($::JOB_PARAMS{'OTHER_HELM_RELEASES'} ne "") {
                    General::Logging::log_user_message("The following releases also found in namespace $sc_namespace:\n" . join "\n", split /\s+/, $::JOB_PARAMS{'OTHER_HELM_RELEASES'});
                }
                $::JOB_PARAMS{'HELM3_EXISTS'} = "yes";
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
    sub Undeploy_HELM3_P911S01T02 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name;
        my $failure_cnt = 0;
        my $helm_executable = $::JOB_PARAMS{'HELM3_EXECUTABLE'};
        my $rc = 0;
        my @result;
        my $timeout = $::JOB_PARAMS{'HELM_TIMEOUT_APPLICATION'};

        General::Playlist_Operations::register_workaround_task("A workaround has been added to include '--no-hooks' in the 'helm uninstall' command to avoid a hanging command due to a bug in the wide column database cleanup scripts, see DND-29094.");
        for $sc_release_name (split /\s+/, $::JOB_PARAMS{'SC_RELEASE_NAME'}) {
            General::Logging::log_user_message("Undeploy SC release $sc_release_name.\nThis will take a while to complete.\n");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}${helm_executable} uninstall $sc_release_name --no-hooks --namespace $sc_namespace --timeout ${timeout}s --debug",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );

            if ($rc == 0) {
                push @::JOB_STATUS, "(/) Undeploy of release '$sc_release_name' successful";
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to undeploy SC");
                push @::JOB_STATUS, "(x) Undeploy of release '$sc_release_name' failed";
                $failure_cnt++;
            }
        }

        return $failure_cnt;

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
    sub Check_Removal_HELM3_P911S01T03 {

        my $deployment_found = "";
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        my $helm_executable = $::JOB_PARAMS{'HELM3_EXECUTABLE'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Find SC deployment");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${helm_executable} list --all --namespace $sc_namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get deployments");
        } else {
            $deployment_found = "";
            $sc_release_name =~ s/\s+/\|/g;
            for (@result) {
                if ($_ =~ /^($sc_release_name)\s+/) {
                    $deployment_found .= "$1, ";
                }
            }
            if ($deployment_found eq "") {
                General::Logging::log_user_message("No SC deployments found in namespace $sc_namespace");
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                $deployment_found =~ s/,\s+$//;
                General::Logging::log_user_error_message("SC deployment $deployment_found found in $sc_namespace namespace");
                $rc = 1;
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
    sub Undeploy_Other_Releases_HELM3_P911S01T04 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my @release_names = split /\s+/, $::JOB_PARAMS{'OTHER_HELM_RELEASES'};
        my $error_cnt = 0;
        my $helm_executable = $::JOB_PARAMS{'HELM3_EXECUTABLE'};
        my $rc = 0;
        my @result;
        my $timeout = $::JOB_PARAMS{'HELM_TIMEOUT_APPLICATION'};

        General::Playlist_Operations::register_workaround_task("A workaround has been added to include '--no-hooks' in the 'helm uninstall' command to avoid a hanging command due to a bug in the wide column database cleanup scripts, see DND-29094.");
        for my $release_name (@release_names) {
            General::Logging::log_user_message("Undeploy release $release_name from namespace $sc_namespace\nThis will take a while to complete.\n");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}${helm_executable} uninstall $release_name --no-hooks --namespace $sc_namespace --timeout ${timeout}s --debug",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );

            if ($rc == 0) {
                push @::JOB_STATUS, "(/) Undeploy of release '$release_name' successful";
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to undeploy release $release_name from namespace $sc_namespace");
                $error_cnt++;
                push @::JOB_STATUS, "(x) Undeploy of release '$release_name' failed";
            }
        }

        if ($error_cnt == 0) {
            return 0;
        } else {
            General::Logging::log_user_error_message("There were $error_cnt errors trying to uninstall other helm charts in same $sc_namespace namespace, see details above.");
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
sub Undeploy_SC_HEML2_P911S02 {

    my $rc;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Deployment_Exists_HELM2_P911S02T01 } );
    return $rc if $rc < 0;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Undeploy_HELM2_P911S02T02 } );
    return $rc if $rc < 0;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Removal_HELM2_P911S02T03 } );
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
    sub Check_Deployment_Exists_HELM2_P911S02T01 {

        my $deployment_found = 0;
        my $deployment_status;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        my $helm_executable = exists $::JOB_PARAMS{'HELM2_EXECUTABLE'} ? $::JOB_PARAMS{'HELM2_EXECUTABLE'} : "";
        my $rc = 0;
        my @result;

        $::JOB_PARAMS{'HELM2_EXISTS'} = "no";
        $::JOB_PARAMS{'SC_RELEASE_VERSION'} = "" unless (exists $::JOB_PARAMS{'SC_RELEASE_VERSION'});
        $::JOB_PARAMS{'SC_RELEASE_BUILD'} = "" unless (exists $::JOB_PARAMS{'SC_RELEASE_BUILD'});

        if ($helm_executable eq "") {
            General::Logging::log_user_message("Helm version 2 not installed, skip this step");
            return General::Playlist_Operations::RC_STEPOUT;
        }

        General::Logging::log_user_message("List SC deployment");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${helm_executable} list --all --namespace $sc_namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            if ($::JOB_PARAMS{'HELM_VERSION'} ne "" && $::JOB_PARAMS{'HELM_VERSION'} ne "2") {
                # We are not interested in helm version 2, so ignore this error and skip this step
                General::Logging::log_user_message("Helm version 2 check failed, but since it's no longer used we ignore this failure");
                return General::Playlist_Operations::RC_STEPOUT;
            } elsif ($::JOB_PARAMS{'HELM_VERSION'} eq "" && $::NETWORK_CONFIG_PARAMS{'helm_version'}{'value'} ne "2") {
                General::Logging::log_user_message("Helm version 2 check failed, but since it's no longer used we ignore this failure");
                return General::Playlist_Operations::RC_STEPOUT;
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get deployments");
        } else {
            for (@result) {
                if ($_ =~ /^$sc_release_name\s+$sc_namespace\s+\d+\s+.+\s+(\S+)\s+eric-sc-umbrella-(\d+\.\d+\.\d+)(\S+)/) {
                    #NAME    NAMESPACE       REVISION        UPDATED                                 STATUS                  CHART                           APP VERSION
                    #eric-sc eiffelesc       1               2021-05-27 07:41:12.499785339 +0000 UTC deployed                eric-sc-umbrella-1.3.1+314
                    #  or
                    #eric-sc eiffelesc       3               2021-05-26 08:06:02.655940685 +0000 UTC pending-rollback        eric-sc-umbrella-1.3.0+61
                    $deployment_found = 1;
                    $deployment_status = $1;
                    $::JOB_PARAMS{'SC_RELEASE_VERSION'} = $2;
                    $::JOB_PARAMS{'SC_RELEASE_BUILD'} = $3;
                    if ($deployment_status ne "deployed") {
                        # Since the deployment to be removed is in some strange state we skip over all the individual
                        # removal of PVC, Job and secrets and just delete the namespace.
                        $::JOB_PARAMS{'ONLY_REMOVE_NAMESPACE'} = "yes";
                    }
                }
            }
            if ($deployment_found == 0 || !@result) {
                General::Logging::log_user_message("No SC deployments found in namespace $sc_namespace");
                return General::Playlist_Operations::RC_STEPOUT;
            } else {
                General::Logging::log_user_message("SC deployment $sc_release_name found in $sc_namespace namespace");
                $::JOB_PARAMS{'HELM2_EXISTS'} = "yes";
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
    sub Undeploy_HELM2_P911S02T02 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        my $helm_executable = $::JOB_PARAMS{'HELM2_EXECUTABLE'};
        my $rc = 0;
        my @result;
        my $timeout = $::JOB_PARAMS{'HELM_TIMEOUT_APPLICATION'};

        General::Logging::log_user_message("Undeploy SC\nThis will take a while to complete.\n");
        General::Playlist_Operations::register_workaround_task("A workaround has been added to include '--no-hooks' in the 'helm uninstall' command to avoid a hanging command due to a bug in the wide column database cleanup scripts, see DND-29094.");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${helm_executable} uninstall $sc_release_name --purge --no-hooks --timeout ${timeout} --debug",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to undeploy SC");
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
    sub Check_Removal_HELM2_P911S02T03 {

        my $deployment_found = 0;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        my $helm_executable = $::JOB_PARAMS{'HELM2_EXECUTABLE'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Find SC deployment");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${helm_executable} list --all --namespace $sc_namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get deployments");
        } else {
            $deployment_found = 0;
            for (@result) {
                if ($_ =~ /$sc_release_name/) {
                    $deployment_found = 1;
                }
            }
            if ($deployment_found == 1) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("SC deployment $sc_release_name found in $sc_namespace namespace");
                return General::Playlist_Operations::RC_FALLBACK;
            } else {
                General::Logging::log_user_message("No SC deployments found in namespace $sc_namespace");
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
sub Remove_PVCs_P911S03 {

    my $rc;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_PVCs_P911S03T01 } );
    return $rc if $rc < 0;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_PVCs_P911S03T02 } );
    return $rc if $rc < 0;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_PVCs_Removal_P911S03T03 } );
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
    sub Print_PVCs_P911S03T01 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Get PVCs");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get pvc --namespace=$sc_namespace --no-headers -o custom-columns=NAME:.metadata.name",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get PVCs");
        } else {
            my $no_resources = 0;
            for (@result) {
                if ($_ =~ /No resources found.+/) {
                    $no_resources = 1;
                }
            }
            if ($no_resources == 1 || !@result) {
                General::Logging::log_user_message("No PVCs found");
                push @::JOB_STATUS, "(-) Removal of PVC skipped";
                return General::Playlist_Operations::RC_STEPOUT;
            } else {
                General::Logging::log_user_message("PVCs found");
            }
        }

        if ($rc != 0 && $ignore_failed_removal == 1) {
            General::Logging::log_user_warning_message("Failure rc=$rc is ignored because removal of SC namespace usually takes care of any issues");
            $rc = 0;

            # Check if the user interrupted the execution.
            # This also clears the counter so the playlist logic does not see this CTRL-C or CTRL-\
            # key press since we still does not care and always return 0 from this subroutine.
            if (General::OS_Operations::signal_captured("ALL") > 0) {
                General::Logging::log_user_warning_message("User interrupted the execution by pressing CTRL-C or CTRL-\\\n");
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
    sub Remove_PVCs_P911S03T02 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $keep_pvc = 0;
        my $rc = 0;
        my $pvc;
        my @pvcs;
        my @result;
        my $wanted_pvc;
        my @wanted_pvcs = (
            'eric-influxdb-pvc',
            'jcatlogs-.+'
        );

        General::Logging::log_user_message("Find PVCs");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get pvc --namespace=$sc_namespace --no-headers -o custom-columns=NAME:.metadata.name",
                "hide-output"   => 1,
                "return-output" => \@pvcs,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get PVCs");
        } else {
            my $failure_cnt = 0;
            foreach $pvc (@pvcs) {
                $keep_pvc = 0;
                foreach $wanted_pvc (@wanted_pvcs) {
                    if ($pvc =~ /$wanted_pvc/) {
                        $keep_pvc = 1;
                    }
                }
                if ($keep_pvc == 1) {
                    next;
                } else {
                    General::Logging::log_user_message("Delete PVC $pvc");
                    $rc = General::OS_Operations::send_command(
                        {
                            "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} delete pvc $pvc --namespace=$sc_namespace",
                            "hide-output"   => 1,
                            "return-output" => \@result,
                        }
                    );
                    if ($rc != 0) {
                        for (@result) {
                            if (/^Error from server.+NotFound.+persistentvolumeclaims.+not found/) {
                                # Ignore the error and just report it as a warning
                                General::Logging::log_user_warning_message("Warning PVC: $pvc not found");
                                last;
                            } else {
                                # Display the result in case of error
                                General::OS_Operations::write_last_temporary_output_to_progress();

                                General::Logging::log_user_error_message("Failed to delete PVC $pvc");

                                $failure_cnt++;
                            }
                        }
                    }
                }
            }
            $rc = 1 if ($failure_cnt > 0);
        }

        if ($rc != 0 && $ignore_failed_removal == 1) {
            General::Logging::log_user_warning_message("Failure rc=$rc is ignored because removal of SC namespace usually takes care of any issues");
            $rc = 0;

            # Check if the user interrupted the execution.
            # This also clears the counter so the playlist logic does not see this CTRL-C or CTRL-\
            # key press since we still does not care and always return 0 from this subroutine.
            if (General::OS_Operations::signal_captured("ALL") > 0) {
                General::Logging::log_user_warning_message("User interrupted the execution by pressing CTRL-C or CTRL-\\\n");
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
    sub Check_PVCs_Removal_P911S03T03 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $keep_pvc = 0;
        my $rc = 0;
        my $pvc;
        my @pvcs;
        my $wanted_pvc;
        my @wanted_pvcs = (
            'eric-influxdb-pvc',
            'jcatlogs-.+'
        );

        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Find PVCs");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get pvc --namespace=$sc_namespace --no-headers -o custom-columns=NAME:.metadata.name",
                "hide-output"   => 1,
                "return-output" => \@pvcs,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get PVCs");
        } else {
            my $failure_cnt = 0;
            foreach $pvc (@pvcs) {
                $keep_pvc = 0;
                foreach $wanted_pvc (@wanted_pvcs) {
                    if ( $pvc =~ /$wanted_pvc/ ) {
                        $keep_pvc = 1;
                    }
                }
                if ($keep_pvc == 1) {
                    next;
                } else {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Found unexpected pvc: $pvc");

                    $failure_cnt++;
                }
            }
            $rc = 1 if ($failure_cnt > 0);
        }

        if ($rc == 0) {
            General::Logging::log_user_message("Found only expected PVCs");
            push @::JOB_STATUS, "(/) Removal of PVC successful";
        } else {
            push @::JOB_STATUS, "(x) Removal of PVC failed";
        }

        if ($rc != 0 && $ignore_failed_removal == 1) {
            General::Logging::log_user_warning_message("Failure rc=$rc is ignored because removal of SC namespace usually takes care of any issues");
            $rc = 0;

            # Check if the user interrupted the execution.
            # This also clears the counter so the playlist logic does not see this CTRL-C or CTRL-\
            # key press since we still does not care and always return 0 from this subroutine.
            if (General::OS_Operations::signal_captured("ALL") > 0) {
                General::Logging::log_user_warning_message("User interrupted the execution by pressing CTRL-C or CTRL-\\\n");
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
sub Remove_Jobs_P911S04 {

    my $rc;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Jobs_P911S04T01 } );
    return $rc if $rc < 0;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Jobs_P911S04T02 } );
    return $rc if $rc < 0;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Removal_P911S04T03 } );
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
    sub Print_Jobs_P911S04T01 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $no_resources = 0;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Get Jobs");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get jobs --namespace=$sc_namespace --no-headers -o custom-columns=NAME:.metadata.name",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get jobs");
        } else {
            for (@result) {
                if ($_ =~ /No resources found.+/) {
                    $no_resources = 1;
                }
            }
            if ($no_resources == 1 || !@result) {
                General::Logging::log_user_message("No jobs found");
                push @::JOB_STATUS, "(-) Removal of Jobs skipped";
                return General::Playlist_Operations::RC_STEPOUT;
            } else {
                General::Logging::log_user_message("Jobs found");
            }
        }

        if ($rc != 0 && $ignore_failed_removal == 1) {
            General::Logging::log_user_warning_message("Failure rc=$rc is ignored because removal of SC namespace usually takes care of any issues");
            $rc = 0;

            # Check if the user interrupted the execution.
            # This also clears the counter so the playlist logic does not see this CTRL-C or CTRL-\
            # key press since we still does not care and always return 0 from this subroutine.
            if (General::OS_Operations::signal_captured("ALL") > 0) {
                General::Logging::log_user_warning_message("User interrupted the execution by pressing CTRL-C or CTRL-\\\n");
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
    sub Remove_Jobs_P911S04T02 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $job;
        my @jobs;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Find jobs");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get jobs --namespace=$sc_namespace --no-headers -o custom-columns=NAME:.metadata.name",
                "hide-output"   => 1,
                "return-output" => \@jobs,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get jobs");
        } else {
            my $failure_cnt = 0;
            foreach $job (@jobs) {
                General::Logging::log_user_message("Delete job: $job");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} delete job $job --namespace=$sc_namespace",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc != 0) {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to delete job $job");
                    $failure_cnt++;
                }
            }
            $rc = 1 if ($failure_cnt > 0);
        }

        if ($rc != 0 && $ignore_failed_removal == 1) {
            General::Logging::log_user_warning_message("Failure rc=$rc is ignored because removal of SC namespace usually takes care of any issues");
            $rc = 0;

            # Check if the user interrupted the execution.
            # This also clears the counter so the playlist logic does not see this CTRL-C or CTRL-\
            # key press since we still does not care and always return 0 from this subroutine.
            if (General::OS_Operations::signal_captured("ALL") > 0) {
                General::Logging::log_user_warning_message("User interrupted the execution by pressing CTRL-C or CTRL-\\\n");
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
    sub Check_Job_Removal_P911S04T03 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $no_resources = 0;
        my $rc = 0;
        my @result;

        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Get Jobs");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get jobs --namespace=$sc_namespace --no-headers -o custom-columns=NAME:.metadata.name",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get jobs");
        } else {
            for (@result) {
                if ($_ =~ /No resources found.+/) {
                    $no_resources = 1;
                }
            }
            if ($no_resources == 1 || !@result) {
                General::Logging::log_user_message("No jobs found");
                push @::JOB_STATUS, "(/) Removal of Jobs successful";
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Jobs found which were not deleted");
                $rc = 1;
                push @::JOB_STATUS, "(x) Removal of Jobs failed";
            }
        }

        if ($rc != 0 && $ignore_failed_removal == 1) {
            General::Logging::log_user_warning_message("Failure rc=$rc is ignored because removal of SC namespace usually takes care of any issues");
            $rc = 0;

            # Check if the user interrupted the execution.
            # This also clears the counter so the playlist logic does not see this CTRL-C or CTRL-\
            # key press since we still does not care and always return 0 from this subroutine.
            if (General::OS_Operations::signal_captured("ALL") > 0) {
                General::Logging::log_user_warning_message("User interrupted the execution by pressing CTRL-C or CTRL-\\\n");
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
sub Remove_Secrets_P911S05 {

    my $rc;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Secrets_P911S05T01 } );
    return $rc if $rc < 0;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_Secrets_P911S05T02 } );
    return $rc if $rc < 0;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Secrets_Removal_P911S05T03 } );
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
    sub Print_Secrets_P911S05T01 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Get secrets");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get secrets --namespace=$sc_namespace --no-headers -o custom-columns=NAME:.metadata.name",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to find secrets");
        } else {
            my $no_resources = 0;
            for (@result) {
                if ($_ =~ /^No resources found.+/) {
                    $no_resources = 1;
                }
            }
            if ($no_resources == 1 || !@result) {
                General::Logging::log_user_message("No secrets found");
                push @::JOB_STATUS, "(-) Removal of Secrets skipped";
                return General::Playlist_Operations::RC_STEPOUT;
            } else {
                General::Logging::log_user_message("Secrets found");
            }
        }

        if ($rc != 0 && $ignore_failed_removal == 1) {
            General::Logging::log_user_warning_message("Failure rc=$rc is ignored because removal of SC namespace usually takes care of any issues");
            $rc = 0;

            # Check if the user interrupted the execution.
            # This also clears the counter so the playlist logic does not see this CTRL-C or CTRL-\
            # key press since we still does not care and always return 0 from this subroutine.
            if (General::OS_Operations::signal_captured("ALL") > 0) {
                General::Logging::log_user_warning_message("User interrupted the execution by pressing CTRL-C or CTRL-\\\n");
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
    sub Remove_Secrets_P911S05T02 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $keep_secret = 0;
        my $rc = 0;
        my @result;
        my $secret;
        my @secrets;
        my $wanted_secret;
        my @wanted_secrets=(
            'default-token-.+',
            'eric-data-distributed-coordinator-creds',
            'eric-data-distributed-coordinator-creds-sc',
            'eric-data-document-database-pg-hook-token-.+',
            'eric-data-document-database-pg-sc',
            'eric-jkube-token-.+',
            'eric-sec-ldap-server-creds',
            'k8s-registry-secret',
            'monitoring-token-.+',
            'snmp-alarm-provider-config',
            'eric-sw-inventory-manager-token-.+',
        );

        General::Logging::log_user_message("Find secrets");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get secrets --namespace=$sc_namespace --no-headers -o custom-columns=NAME:.metadata.name",
                "hide-output"   => 1,
                "return-output" => \@secrets,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get secrets");
        } else {
            my $failure_cnt = 0;
            foreach $secret (@secrets) {
                $keep_secret = 0;
                if ($keep_some_secrets == 1) {
                    foreach $wanted_secret (@wanted_secrets) {
                        if ( $secret =~ /$wanted_secret/ ) {
                            $keep_secret = 1;
                        }
                    }
                }
                if ( $keep_secret == 1 ) {
                    next;
                } else {
                    General::Logging::log_user_message("Delete secret: $secret");
                    $rc = General::OS_Operations::send_command(
                        {
                            "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} delete secret $secret --namespace=$sc_namespace",
                            "hide-output"   => 1,
                            "return-output" => \@result,
                        }
                    );
                    if ($rc != 0) {
                        # Display the result in case of error
                        General::OS_Operations::write_last_temporary_output_to_progress();

                        General::Logging::log_user_error_message("Failed to delete secret: $secret");
                        $failure_cnt++;
                    }
                }
            }
            $rc = 1 if ($failure_cnt > 0);
        }

        if ($rc != 0 && $ignore_failed_removal == 1) {
            General::Logging::log_user_warning_message("Failure rc=$rc is ignored because removal of SC namespace usually takes care of any issues");
            $rc = 0;

            # Check if the user interrupted the execution.
            # This also clears the counter so the playlist logic does not see this CTRL-C or CTRL-\
            # key press since we still does not care and always return 0 from this subroutine.
            if (General::OS_Operations::signal_captured("ALL") > 0) {
                General::Logging::log_user_warning_message("User interrupted the execution by pressing CTRL-C or CTRL-\\\n");
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
    sub Check_Secrets_Removal_P911S05T03 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $keep_secret = 0;
        my $rc = 0;
        my @result;
        my $secret;
        my @secrets;
        my $wanted_secret;
        my @wanted_secrets = (
            'default-token-.+',
            'eric-data-distributed-coordinator-creds',
            'eric-data-distributed-coordinator-creds-sc',
            'eric-data-document-database-pg-hook-token-.+',
            'eric-data-document-database-pg-sc',
            'eric-jkube-token-.+',
            'eric-sec-ldap-server-creds',
            'k8s-registry-secret',
            'monitoring-token-.+',
            'snmp-alarm-provider-config',
            'eric-sw-inventory-manager-token-.+',
        );

        if ($keep_some_secrets == 0) {
            # This logic was added because even if we delete all secrets in above task some secrets
            # are recreated with a different name, for example the following secrets seems to be recreated:
            # default-token-tzv6f
            # eric-data-document-database-pg-hook-token-q8fm9
            # eric-sw-inventory-manager-token-5w9px
            General::Logging::log_user_message("This task is by-passed because \$keep_some_secrets==0");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Get secrets");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get secrets --namespace=$sc_namespace --no-headers -o custom-columns=NAME:.metadata.name",
                "hide-output"   => 1,
                "return-output" => \@secrets,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get secrets");
        } else {
            my $failure_cnt = 0;
            foreach $secret (@secrets) {
                $keep_secret = 0;
                if ($keep_some_secrets == 1) {
                    foreach $wanted_secret (@wanted_secrets) {
                        if ( $secret =~ /$wanted_secret/ ) {
                            $keep_secret = 1;
                        }
                    }
                }
                if ( $keep_secret == 1 ) {
                    next;
                } else {
                    # Display the result in case of error
                    General::Logging::log_user_error_message("Found unexpected secret: $secret");

                    $failure_cnt++;
                }
            }
            $rc = 1 if ($failure_cnt > 0);
        }

        if ($rc == 0) {
            General::Logging::log_user_message("Found only expected secrets");
            push @::JOB_STATUS, "(/) Removal of Secrets successful";
        } else {
            push @::JOB_STATUS, "(x) Removal of Secrets failed";
        }

        if ($rc != 0 && $ignore_failed_removal == 1) {
            General::Logging::log_user_warning_message("Failure rc=$rc is ignored because removal of SC namespace usually takes care of any issues");
            $rc = 0;

            # Check if the user interrupted the execution.
            # This also clears the counter so the playlist logic does not see this CTRL-C or CTRL-\
            # key press since we still does not care and always return 0 from this subroutine.
            if (General::OS_Operations::signal_captured("ALL") > 0) {
                General::Logging::log_user_warning_message("User interrupted the execution by pressing CTRL-C or CTRL-\\\n");
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
sub Remove_Namespace_P911S06 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_What_Remains_Before_Removing_Namespace_P911S06T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_SC_Namespace_P911S06T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_What_Remains_After_Removing_Namespace_P911S06T03 } );
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
    sub Print_What_Remains_Before_Removing_Namespace_P911S06T01 {
        my @api_resources = ();
        my @commands = ();
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        if (ADP::Kubernetes_Operations::namespace_exists($sc_namespace) == 0) {
            General::Logging::log_user_message("SC namespace '$sc_namespace' does not exist, no need to run any tasks in this step");
            return General::Playlist_Operations::RC_STEPOUT;
        }

        General::Logging::log_user_message("Printing information for the '$sc_namespace' namespace before deleting it.\nThis will take a while to complete.\n");
        @api_resources = ADP::Kubernetes_Operations::get_api_resources();
        for my $resource (@api_resources) {
            push @commands, "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get $resource --namespace=$sc_namespace";
        }
        if (@commands) {
            General::OS_Operations::send_command(
                {
                    "command-in-output" => 1,
                    "commands"      => \@commands,
                    "hide-output"   => 1,
                    "ignore-errors" => 1,
                    "stop-on-error" => 0,
                }
            );
        }

        # We don't care about the result of these commands
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
    sub Remove_SC_Namespace_P911S06T02 {
        my $rc = 0;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        General::Logging::log_user_message("Remove SC '$sc_namespace' namespace.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} delete namespace $sc_namespace",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Removal of Namespace successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to delete SC namespace");
            push @::JOB_STATUS, "(x) Removal of Namespace failed";
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
    sub Print_What_Remains_After_Removing_Namespace_P911S06T03 {
        my @api_resources = ();
        my @commands = ();
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

        General::Logging::log_user_message("Printing information for the '$sc_namespace' namespace after deleting it.\nThis will take a while to complete.\n");
        @api_resources = ADP::Kubernetes_Operations::get_api_resources();
        for my $resource (@api_resources) {
            push @commands, "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get $resource --namespace=$sc_namespace";
        }
        if (@commands) {
            General::OS_Operations::send_command(
                {
                    "command-in-output" => 1,
                    "commands"      => \@commands,
                    "hide-output"   => 1,
                    "ignore-errors" => 1,
                    "stop-on-error" => 0,
                }
            );
        }

        # We don't care about the result of these commands
        return 0;
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
sub Check_For_Existing_SC_Deployments_P911S07 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_For_SC_Deployments_P911S07T01 } );
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
    sub Check_For_SC_Deployments_P911S07T01 {
        my @found_release_with_sc_deployment = ();
        my $helm_executable = "";
        my $rc;
        my @result = ();

        $::JOB_PARAMS{'P911_CRD_REMOVAL_ALLOWED'} = "yes";

        $helm_executable = $::JOB_PARAMS{'HELM3_EXECUTABLE'};
        if ($helm_executable ne "") {
            General::Logging::log_user_message("Looking for DSC and SC Deployments using helm version 3 before removing CRDS");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}${helm_executable} list --all --all-namespaces",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to fetch helm list");
                return $rc;
            }

            for (@result) {
                if (/^(\S+)\s+(\S+).+\s+deployed\s+(eric-sc-umbrella-|eric-dsc-|eric-cloud-native-base-|eric-cloud-native-nf-additions-|eric-sc-bsf-|eric-sc-cs-|eric-sc-diameter-|eric-sc-scp-|eric-sc-sepp-).+/) {
                    push @found_release_with_sc_deployment, "  $1 in namespace $2";
                }
            }
        } else {
            General::Logging::log_user_message("Helm version 3 not installed, no check done, so we don't allow removal of CRDs");
            $::JOB_PARAMS{'P911_CRD_REMOVAL_ALLOWED'} = "no";
            push @::JOB_STATUS, "(-) Removal of CRDs skipped because no helm3 binary found";
        }

        if (scalar @found_release_with_sc_deployment > 0) {
            General::Logging::log_user_warning_message(sprintf "Unable to remove CRDS because other DSC or SC Deployments found in the following releases names:\n%s", join "\n", @found_release_with_sc_deployment);
            $::JOB_PARAMS{'P911_CRD_REMOVAL_ALLOWED'} = "no";
            push @::JOB_STATUS, "(-) Removal of CRDs skipped because other DSC or SC Deployments exists that require the CRDs";
        }

        # We don't care about the result of these commands
        return 0;
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
sub Remove_CRD_Charts_HELM3_P911S08 {

    my $rc;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_CRDS_Exists_HELM3_P911S08T01 } );
    return $rc if $rc < 0;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_CRDS_HELM3_P911S08T02 } );
    return $rc if $rc < 0;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_CRD_Removal_HELM3_P911S08T03 } );
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
    sub Check_CRDS_Exists_HELM3_P911S08T01 {

        my $app_type = exists $::JOB_PARAMS{'APPLICATION_TYPE'} ? $::JOB_PARAMS{'APPLICATION_TYPE'} : "sc";
        my $deployment_found = 0;
        my @found_crd_release_names = ();
        my $helm_executable = exists $::JOB_PARAMS{'HELM3_EXECUTABLE'} ? $::JOB_PARAMS{'HELM3_EXECUTABLE'} : "";
        my $rc = 0;
        my $remove_crd_helm_charts = join "|", ADP::Kubernetes_Operations::get_list_of_known_sc_crd_helm_charts($app_type, $::JOB_PARAMS{'SC_RELEASE_VERSION'});
        my @result;

        $::JOB_PARAMS{'P911_CRD_NAMESPACE'} = "";
        if ($helm_executable eq "") {
            General::Logging::log_user_message("Helm version 3 not installed, skip this step");
            return General::Playlist_Operations::RC_STEPOUT;
        }

        General::Logging::log_user_message("Look for known CRD charts in all namespaces");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$helm_executable list --all --all-namespaces",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get CRD charts");
        } else {
            for (@result) {
                if ($_ =~ /^(\S+)\s+(\S+).+\s+deployed\s+($remove_crd_helm_charts)-.+/) {
                    # eric-sec-certm-crd                        eric-crd-ns    1           2021-12-08 14:50:23.647647195 +0000 UTC      deployed    eric-sec-certm-crd-3.10.0+115                      3.10.0
                    # Old SC CRD package e.g. eric-sc-crds-1.5.0+200
                    # New SC CRD packages e.g. eric-sec-sip-tls-crd-2.9.0+36, eric-sec-certm-crd-3.8.0+99, eric-tm-ingress-controller-cr-crd-6.0.0+37
                    if (exists $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} && $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} ne "" && version->parse($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}) >= version->parse("2.28.0")) {
                        if ($1 eq "eric-sec-sip-tls-crd") {
                            # Starting with ECCD release 2.28 the CRD is mandatory and cannot be removed or else a lot of things will start to go wrong.
                            General::Logging::log_user_message("This CRD is mandatory and should not be removed: $1");
                            next;
                        }
                    }
                    $deployment_found++;
                    push @found_crd_release_names, $1;

                    if ($::JOB_PARAMS{'P911_CRD_NAMESPACE'} ne "" && $::JOB_PARAMS{'P911_CRD_NAMESPACE'} ne $2) {
                        General::Logging::log_user_error_message("CRD's are deployed in different namespaces $2 and $::JOB_PARAMS{'P911_CRD_NAMESPACE'} which is unexpected");
                        return General::Playlist_Operations::RC_FALLBACK;
                    }
                    # Store the CRD namespace
                    $::JOB_PARAMS{'P911_CRD_NAMESPACE'} = $2;
                }
            }
            if ($deployment_found == 0 || !@result) {
                General::Logging::log_user_message("OK, No known CRD charts found");
                push @::JOB_STATUS, "(-) Removal of CRD helm charts skipped";
                return General::Playlist_Operations::RC_STEPOUT;
            } else {
                $::JOB_PARAMS{'P911_CRD_RELEASE_NAMES'} = join '|', @found_crd_release_names;
                if ($deployment_found == 1) {
                    General::Logging::log_user_message("CRD chart $::JOB_PARAMS{'P911_CRD_RELEASE_NAMES'} found");
                } else {
                    General::Logging::log_user_message(sprintf "CRD charts %s found", join ', ', @found_crd_release_names);
                }
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
    sub Remove_CRDS_HELM3_P911S08T02 {

        my $crd_namespace = $::JOB_PARAMS{'P911_CRD_NAMESPACE'};
        my $crd_release_name;
        my $helm_executable = $::JOB_PARAMS{'HELM3_EXECUTABLE'};
        my $rc = 0;
        my @result;

        for $crd_release_name (split /\|/, $::JOB_PARAMS{'P911_CRD_RELEASE_NAMES'}) {
            General::Logging::log_user_message("Removing CRD charts for release name $crd_release_name.\nThis will take a while to complete.\n");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}${helm_executable} delete $crd_release_name --namespace $crd_namespace",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );

            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to remove CRDs for release name $crd_release_name");
                return $rc;
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
    sub Check_CRD_Removal_HELM3_P911S08T03 {

        my $app_type = exists $::JOB_PARAMS{'APPLICATION_TYPE'} ? $::JOB_PARAMS{'APPLICATION_TYPE'} : "sc";
        my $deployment_found = 0;
        my @found_crd_release_names = ();
        my $helm_executable = $::JOB_PARAMS{'HELM3_EXECUTABLE'};
        my $rc = 0;
        my $remove_crd_helm_charts = join "|", ADP::Kubernetes_Operations::get_list_of_known_sc_crd_helm_charts($app_type, $::JOB_PARAMS{'SC_RELEASE_VERSION'});
        my @result;

        General::Logging::log_user_message("Look for known CRD charts in all namespaces");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${helm_executable} list --all --all-namespaces",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get CRD charts");
        } else {
            for (@result) {
                if ($_ =~ /^(\S+)\s+(\S+).+\s+deployed\s+($remove_crd_helm_charts)-.+/) {
                    # eric-sec-certm-crd                        eric-crd-ns    1           2021-12-08 14:50:23.647647195 +0000 UTC      deployed    eric-sec-certm-crd-3.10.0+115                      3.10.0
                    # Old SC CRD package e.g. eric-sc-crds-1.5.0+200
                    # New SC CRD packages e.g. eric-sec-sip-tls-crd-2.9.0+36, eric-sec-certm-crd-3.8.0+99, eric-tm-ingress-controller-cr-crd-6.0.0+37
                    if (exists $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} && $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} ne "" && version->parse($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}) >= version->parse("2.28.0")) {
                        if ($1 eq "eric-sec-sip-tls-crd") {
                            # Starting with ECCD release 2.28 the CRD is mandatory and cannot be removed or else a lot of things will start to go wrong.
                            General::Logging::log_user_message("This CRD is mandatory and should not be removed: $1");
                            next;
                        }
                    }
                    $deployment_found++;
                    push @found_crd_release_names, $1;
                }
            }
            if ($deployment_found > 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                if ($deployment_found == 1) {
                    General::Logging::log_user_message("CRD chart $found_crd_release_names[0] found");
                } else {
                    General::Logging::log_user_message(sprintf "CRD charts %s found", join ', ', @found_crd_release_names);
                }
                push @::JOB_STATUS, "(x) Removal of CRD helm charts failed";
                return General::Playlist_Operations::RC_FALLBACK;
            } else {
                General::Logging::log_user_message("OK, No known CRD charts found");
                push @::JOB_STATUS, "(/) Removal of CRD helm charts successful";
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
sub Remove_CRD_Charts_HEML2_P911S09 {

    my $rc;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_CRDS_Exists_HELM2_P911S09T01 } );
    return $rc if $rc < 0;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_CRDS_HELM2_P911S09T02 } );
    return $rc if $rc < 0;

    #
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_CRD_Removal_HELM2_P911S09T03 } );
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
    sub Check_CRDS_Exists_HELM2_P911S09T01 {

        my $deployment_found = 0;
        my $crd_namespace = $::JOB_PARAMS{'CRD_NAMESPACE'};
        my $crd_release_name = $::JOB_PARAMS{'CRD_RELEASE_NAME'};
        my $helm_executable = exists $::JOB_PARAMS{'HELM2_EXECUTABLE'} ? $::JOB_PARAMS{'HELM2_EXECUTABLE'} : "";
        my $rc = 0;
        my @result;

        if ($helm_executable eq "") {
            General::Logging::log_user_message("Helm version 2 not installed, skip this step");
            return General::Playlist_Operations::RC_STEPOUT;
        }

        General::Logging::log_user_message("List CRD charts");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$helm_executable list --all --namespace $crd_namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            if ($::JOB_PARAMS{'HELM_VERSION'} ne "" && $::JOB_PARAMS{'HELM_VERSION'} ne "2") {
                # We are not interested in helm version 2, so ignore this error and skip this step
                General::Logging::log_user_message("Helm version 2 check failed, but since it's no longer used we ignore this failure");
                return General::Playlist_Operations::RC_STEPOUT;
            } elsif ($::JOB_PARAMS{'HELM_VERSION'} eq "" && $::NETWORK_CONFIG_PARAMS{'helm_version'}{'value'} ne "2") {
                General::Logging::log_user_message("Helm version 2 check failed, but since it's no longer used we ignore this failure");
                return General::Playlist_Operations::RC_STEPOUT;
            }
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get CRD charts");
        } else {
            for (@result) {
                if ($_ =~ /$crd_release_name/) {
                    $deployment_found = 1;
                }
            }
            if ($deployment_found == 0 || !@result) {
                General::Logging::log_user_message("No CRD charts found in namespace $crd_namespace");
                return General::Playlist_Operations::RC_STEPOUT;
            } else {
                General::Logging::log_user_message("CRD chart $crd_release_name found in $crd_namespace namespace");
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
    sub Remove_CRDS_HELM2_P911S09T02 {

        my $crd_namespace = $::JOB_PARAMS{'CRD_NAMESPACE'};
        my $crd_release_name = $::JOB_PARAMS{'CRD_RELEASE_NAME'};
        my $helm_executable = $::JOB_PARAMS{'HELM2_EXECUTABLE'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Removing CRD chart\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${helm_executable} delete $crd_release_name --purge",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to remove CRD chart");
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
    sub Check_CRD_Removal_HELM2_P911S09T03 {

        my $deployment_found = 0;
        my $crd_namespace = $::JOB_PARAMS{'CRD_NAMESPACE'};
        my $crd_release_name = $::JOB_PARAMS{'CRD_RELEASE_NAME'};
        my $helm_executable = $::JOB_PARAMS{'HELM2_EXECUTABLE'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("List CRD charts");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${helm_executable} list --all --namespace $crd_namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get CRD charts");
        } else {
            $deployment_found = 0;
            for (@result) {
                if ($_ =~ /$crd_release_name/) {
                    $deployment_found = 1;
                }
            }
            if ($deployment_found == 1) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("CRD chart $crd_release_name found in $crd_namespace namespace");
                return General::Playlist_Operations::RC_FALLBACK;
            } else {
                General::Logging::log_user_message("No CRD charts found in namespace $crd_namespace");
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
sub Remove_CRDs_P911S10 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_CRDs_P911S10T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Remove_CRDs_P911S10T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_CRDs_P911S10T03 } );
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
    sub Print_CRDs_P911S10T01 {
        my $app_type = exists $::JOB_PARAMS{'APPLICATION_TYPE'} ? $::JOB_PARAMS{'APPLICATION_TYPE'} : "sc";
        my @remove_crds = ADP::Kubernetes_Operations::get_list_of_known_sc_crds($app_type, $::JOB_PARAMS{'SC_RELEASE_VERSION'});
        my $crd;
        my $crd_cnt;
        my @crds;
        my $delete_crd = 0;
        my $rc = 0;
        my $remove_crd;

        General::Logging::log_user_message("Get known CRDs");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get crd --no-headers -o custom-columns=NAME:.metadata.name",
                "hide-output"   => 1,
                "return-output" => \@crds,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get CRDs");
        } else {
            $crd_cnt = 0;
            foreach $crd (@crds) {
                $delete_crd = 0;
                foreach $remove_crd (@remove_crds) {
                    if ( $crd =~ /$remove_crd/ ) {
                        if (exists $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} && $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} ne "" && version->parse($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}) >= version->parse("2.28.0")) {
                            if ($crd =~ /^(internalcertificates|internalusercas)\.siptls\.sec\.ericsson\.com$/) {
                                # Starting with ECCD release 2.28 the CRD is mandatory and cannot be removed or else a lot of things will start to go wrong.
                                General::Logging::log_user_message("This CRD is mandatory and should not be removed: $crd");
                                next;
                            }
                        }
                        $delete_crd = 1;
                    }
                }
                if ( $delete_crd == 1 ) {
                    # Display the result in case of error
                    General::Logging::log_user_message("Found CRD to remove: $crd");

                    $crd_cnt++;
                } else {
                    next;
                }
            }
        }

        if ($crd_cnt == 0 || !@crds) {
            General::Logging::log_user_message("No CRDs found");
            push @::JOB_STATUS, "(-) Removal of CRDs skipped";
            return General::Playlist_Operations::RC_STEPOUT;
        } else {
            General::Logging::log_user_message("CRDs found");
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
    sub Remove_CRDs_P911S10T02 {
        my $app_type = exists $::JOB_PARAMS{'APPLICATION_TYPE'} ? $::JOB_PARAMS{'APPLICATION_TYPE'} : "sc";
        my @remove_crds = ADP::Kubernetes_Operations::get_list_of_known_sc_crds($app_type, $::JOB_PARAMS{'SC_RELEASE_VERSION'});
        my $crd;
        my @crds;
        my $delete_crd = 0;
        my $rc = 0;
        my $remove_crd;
        my @result;

        General::Logging::log_user_message("Get known CRDs");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get crd --no-headers -o custom-columns=NAME:.metadata.name",
                "hide-output"   => 1,
                "return-output" => \@crds,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get CRDs");
        } else {
            my $failure_cnt = 0;
            foreach $crd (@crds) {
                $delete_crd = 0;
                foreach $remove_crd (@remove_crds) {
                    if ( $crd =~ /$remove_crd/ ) {
                        if (exists $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} && $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} ne "" && version->parse($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}) >= version->parse("2.28.0")) {
                            if ($crd =~ /^(internalcertificates|internalusercas)\.siptls\.sec\.ericsson\.com$/) {
                                # Starting with ECCD release 2.28 the CRD is mandatory and cannot be removed or else a lot of things will start to go wrong.
                                General::Logging::log_user_message("This CRD is mandatory and should not be removed: $crd");
                                next;
                            }
                        }
                        $delete_crd = 1;
                    }
                }
                if ( $delete_crd == 1 ) {
                    General::Logging::log_user_message("Delete CRD: $crd");
                    $rc = General::OS_Operations::send_command(
                        {
                            "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} delete crd $crd",
                            "hide-output"   => 1,
                            "return-output" => \@result,
                        }
                    );
                    if ($rc == 1) {
                        # Display the result in case of error
                        General::OS_Operations::write_last_temporary_output_to_progress();

                        General::Logging::log_user_error_message("Failed to delete CRD: $crd");
                        $failure_cnt++;
                    }
                } else {
                    next;
                }
            }
            $rc = 1 if ($failure_cnt > 0);
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
    sub Check_CRDs_P911S10T03 {
        my $app_type = exists $::JOB_PARAMS{'APPLICATION_TYPE'} ? $::JOB_PARAMS{'APPLICATION_TYPE'} : "sc";
        my @remove_crds = ADP::Kubernetes_Operations::get_list_of_known_sc_crds($app_type, $::JOB_PARAMS{'SC_RELEASE_VERSION'});
        my $crd;
        my @crds;
        my $delete_crd = 0;
        my $rc = 0;
        my $remove_crd;

        General::Logging::log_user_message("Get known CRDs");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get crd --no-headers -o custom-columns=NAME:.metadata.name",
                "hide-output"   => 1,
                "return-output" => \@crds,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get CRDs");
        } else {
            my $failure_cnt = 0;
            foreach $crd (@crds) {
                $delete_crd = 0;
                foreach $remove_crd (@remove_crds) {
                    if ( $crd =~ /$remove_crd/ ) {
                        if (exists $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} && $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} ne "" && version->parse($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}) >= version->parse("2.28.0")) {
                            if ($crd =~ /^(internalcertificates|internalusercas)\.siptls\.sec\.ericsson\.com$/) {
                                # Starting with ECCD release 2.28 the CRD is mandatory and cannot be removed or else a lot of things will start to go wrong.
                                General::Logging::log_user_message("This CRD is mandatory and should not be removed: $crd");
                                next;
                            }
                        }
                        $delete_crd = 1;
                    }
                }
                if ( $delete_crd == 1 ) {
                    # Display the result in case of error
                    General::Logging::log_user_error_message("Found unexpected CRD: $crd");

                    $failure_cnt++;
                } else {
                    next;
                }
            }
            $rc = 1 if ($failure_cnt > 0);
        }

        if ($rc == 0) {
            General::Logging::log_user_message("Found only expected CRDs");
            push @::JOB_STATUS, "(/) Removal of CRDs successful";
        } else {
            push @::JOB_STATUS, "(x) Removal of CRDs failed";
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
sub Fallback001_P911S99 {

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

This playlist performs an undeploy of the software on a SC node.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
