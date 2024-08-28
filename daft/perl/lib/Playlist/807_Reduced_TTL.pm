package Playlist::807_Reduced_TTL;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.10
#  Date     : 2024-06-14 09:52:59
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

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    parse_network_config_parameters
    usage
    usage_return_playlist_variables
    );

use ADP::Kubernetes_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;
use Playlist::932_Test_Case_Common_Logic;

#
# Variable Declarations
#
my %playlist_variables;
set_playlist_variables();

my $debug_command = "";         # Change this value from "echo " to "" when ready to test on real system

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
    my $rc = 0;

    # Set Job Type
    $::JOB_PARAMS{'JOBTYPE'} = "REDUCED_TTL";

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Test_Checks_P807S01, \&Fallback001_P807S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Test_Case_P807S02, \&Fallback001_P807S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Checks_P807S03, \&Fallback001_P807S99 );
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
sub Perform_Pre_Test_Checks_P807S01 {

    my $rc;

    # This first call will initialize job environment and perform pre test checks
    $::JOB_PARAMS{'P932_TASK'} = "PRE_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
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
#
# -----------------------------------------------------------------------------
sub Perform_Test_Case_P807S02 {

    my $length;
    my $message;
    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P807S02T01 } );
    return $rc if $rc < 0;

    # Set start value either to 1 if never executed or to value of P807_EXECUTE_P807S02Txx job variables
    # if job has been interrupted and rerun.
    $::JOB_PARAMS{'P807_EXECUTE_P807S02T02'} = exists $::JOB_PARAMS{'P807_EXECUTE_P807S02T02'} ? $::JOB_PARAMS{'P807_EXECUTE_P807S02T02'} : 1;
    $::JOB_PARAMS{'P807_EXECUTE_P807S02T03'} = exists $::JOB_PARAMS{'P807_EXECUTE_P807S02T03'} ? $::JOB_PARAMS{'P807_EXECUTE_P807S02T03'} : 1;
    $::JOB_PARAMS{'P807_EXECUTE_P807S02T04'} = exists $::JOB_PARAMS{'P807_EXECUTE_P807S02T04'} ? $::JOB_PARAMS{'P807_EXECUTE_P807S02T04'} : 1;
    $::JOB_PARAMS{'P807_EXECUTE_P807S02T05'} = exists $::JOB_PARAMS{'P807_EXECUTE_P807S02T05'} ? $::JOB_PARAMS{'P807_EXECUTE_P807S02T05'} : 1;
    $::JOB_PARAMS{'P807_EXECUTE_P807S02T06'} = exists $::JOB_PARAMS{'P807_EXECUTE_P807S02T06'} ? $::JOB_PARAMS{'P807_EXECUTE_P807S02T06'} : 1;
    $::JOB_PARAMS{'P807_EXECUTE_P807S02T07'} = exists $::JOB_PARAMS{'P807_EXECUTE_P807S02T07'} ? $::JOB_PARAMS{'P807_EXECUTE_P807S02T07'} : 1;
    $::JOB_PARAMS{'P807_EXECUTE_P807S02T08'} = exists $::JOB_PARAMS{'P807_EXECUTE_P807S02T08'} ? $::JOB_PARAMS{'P807_EXECUTE_P807S02T08'} : 1;
    $::JOB_PARAMS{'P807_EXECUTE_P807S02T09'} = exists $::JOB_PARAMS{'P807_EXECUTE_P807S02T09'} ? $::JOB_PARAMS{'P807_EXECUTE_P807S02T09'} : 1;

    # Set start time for this repetition in case we want to have KPI verdicts for each repetition
    $::JOB_PARAMS{'KPI_START_TIME'} = time();

    # Set the description to be used for status messages
    $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Reduced TTL";

    if ($::JOB_PARAMS{'P807_EXECUTE_P807S02T02'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Configmap_Files_P807S02T02 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P807_EXECUTE_P807S02T02'} = 0;
    }

    if ($::JOB_PARAMS{'P807_EXECUTE_P807S02T03'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Certificate_Validity_Before_Change_P807S02T03 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P807_EXECUTE_P807S02T03'} = 0;
    }

    if ($::JOB_PARAMS{'P807_EXECUTE_P807S02T04'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Certificate_Renewal_Info_In_Logs_Before_Change_P807S02T04 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P807_EXECUTE_P807S02T04'} = 0;
    }

    if ($::JOB_PARAMS{'P807_EXECUTE_P807S02T05'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Patch_All_Internal_Certificates_To_Use_Default_Validity_P807S02T05 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P807_EXECUTE_P807S02T05'} = 0;
    }

    if ($::JOB_PARAMS{'P807_EXECUTE_P807S02T06'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Patch_Configmap_P807S02T06 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P807_EXECUTE_P807S02T06'} = 0;
    }

    if ($::JOB_PARAMS{'P807_EXECUTE_P807S02T07'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Patch_WCDB_Certificate_Renewal_If_Needed_P807S02T07 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P807_EXECUTE_P807S02T07'} = 0;
    }

    if ($::JOB_PARAMS{'P807_EXECUTE_P807S02T08'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Scale_sip_tls_in_and_out_P807S02T08 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P807_EXECUTE_P807S02T08'} = 0;
    }

    if ($::JOB_PARAMS{'P807_EXECUTE_P807S02T09'} == 1) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Wait_For_Certificate_Renewal_P807S02T09 } );
        return $rc if $rc < 0;
        $::JOB_PARAMS{'P807_EXECUTE_P807S02T09'} = 0;
    }

    # This call will wait for all PODs to be up
    $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
    # Only perform the pod status and pod alarm checks
    my $old_skip_post_healthcheck = $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'};
    my $old_skip_collect_logs = $::JOB_PARAMS{'SKIP_COLLECT_LOGS'};
    $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = "yes";

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );

    # Restore the old values
    $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = $old_skip_post_healthcheck;
    $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = $old_skip_collect_logs;
    return $rc if $rc < 0;

    # Delete these temporary variables
    delete $::JOB_PARAMS{'P807_EXECUTE_P807S02T02'};
    delete $::JOB_PARAMS{'P807_EXECUTE_P807S02T03'};
    delete $::JOB_PARAMS{'P807_EXECUTE_P807S02T04'};
    delete $::JOB_PARAMS{'P807_EXECUTE_P807S02T05'};
    delete $::JOB_PARAMS{'P807_EXECUTE_P807S02T06'};
    delete $::JOB_PARAMS{'P807_EXECUTE_P807S02T07'};
    delete $::JOB_PARAMS{'P807_EXECUTE_P807S02T08'};
    delete $::JOB_PARAMS{'P807_EXECUTE_P807S02T09'};

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
    sub Check_Job_Parameters_P807S02T01 {

        my $rc = 0;

        my $message = sprintf "INTERNAL_CERTIFICATE_TTL=%-20s (the internal certificates are valid for a maximum of %s)\n",
            $::JOB_PARAMS{'INTERNAL_CERTIFICATE_TTL'},
            General::OS_Operations::convert_seconds_to_wdhms($::JOB_PARAMS{'INTERNAL_CERTIFICATE_TTL'}, 0);
        $message .=   sprintf "INTERNAL_CERTIFICATE_RENEWAL_RATIO=%-10s (the internal certificates are renewed after %s)\n",
            $::JOB_PARAMS{'INTERNAL_CERTIFICATE_RENEWAL_RATIO'},
            General::OS_Operations::convert_seconds_to_wdhms($::JOB_PARAMS{'INTERNAL_CERTIFICATE_TTL'} * $::JOB_PARAMS{'INTERNAL_CERTIFICATE_RENEWAL_RATIO'}, 0);

        General::Logging::log_user_message($message);

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
    sub Create_Configmap_Files_P807S02T02 {

        my $command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} get cm eric-sec-sip-tls-config -o json";
        my $description = "Creating configmap files";
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("$description");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
                "save-to-file"  => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sec-sip-tls-config_original.json",
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$description failed because command failed");
            push @::JOB_STATUS, "(x) $description failed";
            return 1;
        }

        my $found = 0;
        for (@result) {
            if (s/\\"internal_cert\\": \{\\n    \\"ttl\\": \\"\d+\\",\\n    \\"renewal_ratio\\": \\"\d+.\d+\\",\\n/\\"internal_cert\\": \{\\n    \\"ttl\\": \\"$::JOB_PARAMS{'INTERNAL_CERTIFICATE_TTL'}\\",\\n    \\"renewal_ratio\\": \\"$::JOB_PARAMS{'INTERNAL_CERTIFICATE_RENEWAL_RATIO'}\\",\\n/) {
                # \"internal_cert\": {\n    \"ttl\": \"604800\",\n    \"renewal_ratio\": \"0.9\",\n
                $found = 1;
                last;
            }
        }
        if ($found == 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$description failed because the expected pattern was not found");
            push @::JOB_STATUS, "(x) $description failed";
            return 1;
        }

        $rc = General::File_Operations::write_file(
            {
                "filename"            => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sec-sip-tls-config_new.json",
                "output-ref"          => \@result,
                "eol-char"            => "\n",
                "append-file"         => 0,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("$description failed because the file $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sec-sip-tls-config_new.json could not be created");
            push @::JOB_STATUS, "(x) $description failed";
            return 1;
        }

        General::Logging::log_user_message("$description was successful");
        push @::JOB_STATUS, "(/) $description was successful";

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
    sub Print_Certificate_Validity_Before_Change_P807S02T03 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/parse_certificate_validity_info.pl -n $::JOB_PARAMS{'SC_NAMESPACE'} -i '(client|cql)' -e '(-ca-|-ca\$|emergency)' -m";
        my $description = "Print certificate validity dates before change";
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("$description");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
                "save-to-file"  => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/certificate_validity_before_change.log",
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$description failed");
            push @::JOB_STATUS, "(x) $description failed";
            return 1;
        }

        General::Logging::log_user_message("$description was successful");
        push @::JOB_STATUS, "(/) $description was successful";

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
    sub Print_Certificate_Renewal_Info_In_Logs_Before_Change_P807S02T04 {

        my @sip_tls_pods = ADP::Kubernetes_Operations::get_pod_names(
            {
                "hide-output"       => 1,
                "namespace"         => $::JOB_PARAMS{'SC_NAMESPACE'},
                "pod-include-list"  => [ "eric-sec-sip-tls-main-" ],
            }
        );
        my $command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} logs -c sip-tls";
        my $description = "Printing logs in pod";
        my $rc = 0;
        my @result;

        for my $pod (@sip_tls_pods) {
            General::Logging::log_user_message("$description $pod");

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$command $pod",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                    "save-to-file"  => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/sip_tls_logs_before_$pod.log",
                }
            );

            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("$description $pod failed");
                push @::JOB_STATUS, "(x) $description $pod failed";
                return 1;
            }

            General::Logging::log_user_message("$description $pod was successful");
            push @::JOB_STATUS, "(/) $description $pod was successful";
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
    sub Patch_All_Internal_Certificates_To_Use_Default_Validity_P807S02T05 {

        my $command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get internalcertificates -n $::JOB_PARAMS{'SC_NAMESPACE'} --no-headers";
        my $description = "Fetching internal certificate names";
        my $rc = 0;
        my @certificate_names;
        my @result;

        General::Logging::log_user_message("$description");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@certificate_names,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$description failed");
            push @::JOB_STATUS, "(x) $description failed";
            return 1;
        }

        for my $line (@certificate_names) {
            my $name = "";
            if ($line =~ /^(\S+)\s+\S+\s+\S+\s*$/) {
                #NAME                                                              CN                                                       SECRET
                #bsf-diameter-dced-client-cert                                     root                                                     eric-bsf-diameter-dced-client-cert
                $name = $1;
            }
            next if $name eq "";
            $description = "Patching certificate $name";
            $command = "kubectl patch internalcertificates -n $::JOB_PARAMS{'SC_NAMESPACE'} $name -p '{ \"spec\": {\"certificate\": {\"validity\": null}}}' --type merge";
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}${command}",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );

            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("$description $name failed");
                push @::JOB_STATUS, "(x) $description $name failed";
                return 1;
            }

            if ($result[0] =~ /\(no change\)/) {
                General::Logging::log_user_message("$description $name was not needed");
                push @::JOB_STATUS, "(-) $description $name was not needed";
            } else {
                General::Logging::log_user_message("$description $name was successful");
                push @::JOB_STATUS, "(/) $description $name was successful";
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
    sub Patch_Configmap_P807S02T06 {

        my $command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} apply -f $::JOB_PARAMS{'_JOB_CONFIG_DIR'}/eric-sec-sip-tls-config_new.json";
        my $description = "Patching configmap eric-sec-sip-tls-config";
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("$description");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${command}",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$description failed");
            push @::JOB_STATUS, "(x) $description failed";
            return 1;
        }

        General::Logging::log_user_message("$description was successful");
        push @::JOB_STATUS, "(/) $description was successful";

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
    sub Patch_WCDB_Certificate_Renewal_If_Needed_P807S02T07 {

        my $current_datetime = General::OS_Operations::format_time_iso(time());
        my $current_date = $current_datetime;
           $current_date =~ s/\s+\S+$//;    # Just keep the date in YYYY-MM-DD format
        my $command = "";
        my $description = "";
        my @names;
        my $rc = 0;
        my @result;

        $description = "Checking if configmap eric-data-wide-column-database-cd-tls-restarter or eric-bsf-wcdb-cd-tls-restarter exists";
        @result = ADP::Kubernetes_Operations::get_resource_names(
            {
                "resource"      => "configmap",
                "namespace"     => $::JOB_PARAMS{'SC_NAMESPACE'},
                "include-list"  => [ "^eric-data-wide-column-database-cd-tls-restarter", "^eric-bsf-wcdb-cd-tls-restarter" ],
                "hide-output"   => 1,
            }
        );
        if (scalar @result == 0) {
            General::Logging::log_user_message("No eric-data-wide-column-database-cd-tls-restarter or eric-bsf-wcdb-cd-tls-restarter configmap, skip this task");
            return General::Playlist_Operations::RC_TASKOUT;
        } elsif (scalar @result > 1) {
            General::Logging::log_user_error_message("Unexpected number of configmap returned, only expecting 0 or 1");
            push @::JOB_STATUS, "(x) Patching configmap and cronjob for eric-data-wide-column-database-cd-tls-restarter or eric-bsf-wcdb-cd-tls-restarter failed";
            return 1;
        }

        my $configmap = $result[0];

        # Check if we have WCDB to patch
        $description = "Reading data from configmap $configmap";
        $command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} get cm $configmap -o json";
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${command}",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            General::Logging::log_user_error_message("Patching configmap and cronjob for $configmap failed because reading data failed");
            push @::JOB_STATUS, "(x) Patching configmap and cronjob for eric-data-wide-column-database-cd-tls-restarter or eric-bsf-wcdb-cd-tls-restarter failed";
            return 1;
        }
        for (@result) {
            if (/^\s+(eric-data-wide-column-database-cd-\S+|eric-bsf-wcdb-cd-\S+):\s+"\d{4}-\d{2}-\d{2}"/) {
                #  eric-data-wide-column-database-cd-0: "2023-11-04"
                #  eric-data-wide-column-database-cd-1: "2023-11-04"
                # or
                #  eric-data-wide-column-database-cd-datacenter1-rack1-0: "2023-11-04"
                #  eric-data-wide-column-database-cd-datacenter1-rack1-1: "2023-11-04"
                push @names, $1;
            } elsif (/^\s+"(eric-data-wide-column-database-cd-\S+|eric-bsf-wcdb-cd-\S+)":\s+"\d{4}-\d{2}-\d{2}"/) {
                #  "eric-data-wide-column-database-cd-0": "2023-11-04"
                #  "eric-data-wide-column-database-cd-1": "2023-11-04"
                # or
                #  "eric-data-wide-column-database-cd-datacenter1-rack1-0": "2023-11-04"
                #  "eric-data-wide-column-database-cd-datacenter1-rack1-1": "2023-11-04"
                push @names, $1;
            }
        }
        if (scalar @names != 2) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$description failed, did not find 2 eric-data-wide-column-database-cd-XXX or eric-bsf-wcdb-cd-XXX values");
            push @::JOB_STATUS, "(x) $description failed";
            return 1;
        }

        # Patching configmap
        # ------------------
        $command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} patch cm $configmap --type merge -p '{\"data\":{\"$names[0]\":\"$current_date\",\"$names[1]\":\"$current_date\"}}'";
        $description = "Patching configmap $configmap";
        General::Logging::log_user_message("$description");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${command}",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$description failed");
            push @::JOB_STATUS, "(x) $description failed";
            return 1;
        }

        General::Logging::log_user_message("$description was successful");
        push @::JOB_STATUS, "(/) $description was successful";

        # Patching cronjob
        # ----------------
        $command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} patch cronjob $configmap --type merge -p '{\"spec\":{\"schedule\": \"30 * * * *\"}}'";
        $description = "Patching cronjob $configmap";
        General::Logging::log_user_message("$description");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${command}",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$description failed");
            push @::JOB_STATUS, "(x) $description failed";
            return 1;
        }

        General::Logging::log_user_message("$description was successful");
        push @::JOB_STATUS, "(/) $description was successful";

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
    sub Scale_sip_tls_in_and_out_P807S02T08 {

        my $command = "";
        my $current_replicas = undef;
        my $description = "";
        my $delay = 10;
        my $rc = 0;
        my @result;

        $description = "Fetching current replicas value for deployment eric-sec-sip-tls-main";
        General::Logging::log_user_message("$description");
        $current_replicas = ADP::Kubernetes_Operations::get_replicas(
            {
                "check-name"    => "eric-sec-sip-tls-main",
                "check-type"    => "deployment",
                "hide-output"   => 1,
                "namespace"     => $::JOB_PARAMS{'SC_NAMESPACE'}
            }
        );
        unless (defined $current_replicas) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$description failed");
            push @::JOB_STATUS, "(x) $description failed";
            return 1;
        }

        $command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} scale deployment eric-sec-sip-tls-main --replicas=0";
        $description = "Scaling deployment eric-sec-sip-tls-main in to 0 replicas";
        General::Logging::log_user_message("$description");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${command}",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$description failed");
            push @::JOB_STATUS, "(x) $description failed";
            return 1;
        }
        General::Logging::log_user_message("$description was successful");
        push @::JOB_STATUS, "(/) $description was successful";

        General::Logging::log_user_message(sprintf "Waiting %s for eric-sec-sip-tls-main deployment to scale in to 0 replicas", General::OS_Operations::convert_seconds_to_wdhms($delay, 0));
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "progress-interval" => 2,
                "progress-message"  => 1,
                "seconds"           => $delay,
                "use-logging"       => 1,
            }
        );
        if ($rc == 1) {
            # CTRL-C pressed, just ignore it
            $rc = 0;
        }

        $command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} scale deployment eric-sec-sip-tls-main --replicas=$current_replicas";
        $description = "Scaling deployment eric-sec-sip-tls-main out to $current_replicas replicas";
        General::Logging::log_user_message("$description");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${command}",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$description failed");
            push @::JOB_STATUS, "(x) $description failed";
            return 1;
        }
        General::Logging::log_user_message("$description was successful");
        push @::JOB_STATUS, "(/) $description was successful";

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
    sub Wait_For_Certificate_Renewal_P807S02T09 {

        my $current_datetime = General::OS_Operations::format_time_iso(time());
        my $current_month_day = $current_datetime;
           $current_month_day =~ s/^\d{4}-(\d{2}-\d{2}).+/$1/;    # Just keep the year in YYYY format
        my $current_year = $current_datetime;
           $current_year =~ s/^(\d{4})-.+/$1/;    # Just keep the year in YYYY format
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/parse_certificate_validity_info.pl -n $::JOB_PARAMS{'SC_NAMESPACE'} -i '(client|cql)' -e '(-ca-|-ca\$|emergency)' -m";
        my $description = "Print certificate validity dates after change";
        my $delay = int($::JOB_PARAMS{'INTERNAL_CERTIFICATE_TTL'} * $::JOB_PARAMS{'INTERNAL_CERTIFICATE_RENEWAL_RATIO'});
        my $different_cnt = 0;
        my $ignored_cnt = 0;
        my $missing_cnt = 0;
        my %new_certs;
        my %old_certs;
        my $rc = 0;
        my @result;
        my @same;
        my $same_cnt = 0;
        my $total_cnt = 0;

        # Add an extra margin of 2 minutes to make sure the certificates has renewed
        $delay += 120;
        General::Logging::log_user_message(sprintf "Waiting %s for certificates to renew", General::OS_Operations::convert_seconds_to_wdhms($delay, 0));
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "progress-interval" => 60,
                "progress-message"  => 1,
                "seconds"           => $delay,
                "use-logging"       => 1,
            }
        );
        if ($rc == 1) {
            # CTRL-C pressed, just ignore it
            $rc = 0;
        }

        $rc = General::File_Operations::read_file(
            {
                "filename"            => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/certificate_validity_before_change.log",
                "output-ref"          => \@result,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("$description failed because the file $::JOB_PARAMS{'_JOB_LOG_DIR'}/certificate_validity_before_change.log could not be read");
            push @::JOB_STATUS, "(x) $description failed";
            return 1;
        }
        for (@result) {
            if (/^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\s+\S+)\s+(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\s+\S+)\s+(\S+)\s+(.+)/) {
                #2023-10-20 13:50:28 GMT    2023-10-20 14:50:58 GMT    InternalCertificate   eric-lm-combined-server-license-getter-client-cert (cert.pem)
                $old_certs{$4}{'Not Before'} = $1;
                $old_certs{$4}{'Not After'} = $2;
                $old_certs{$4}{'Created for Kind'} = $3;
            }
        }

        DO_IT_AGAIN:

        General::Logging::log_user_message("$description");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
                "save-to-file"  => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/certificate_validity_after_change.log",
            }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$description failed because fetching certificate data could not be done");
            push @::JOB_STATUS, "(x) $description failed";
            return 1;
        }

        undef %new_certs;
        $same_cnt = 0;
        @same = ();
        $different_cnt = 0;
        $ignored_cnt = 0;
        $missing_cnt = 0;
        $total_cnt = 0;
        for (@result) {
            if (/^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\s+\S+)\s+(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\s+\S+)\s+(\S+)\s+(.+)/) {
                #2023-10-20 13:50:28 GMT    2023-10-20 14:50:58 GMT    InternalCertificate   eric-lm-combined-server-license-getter-client-cert (cert.pem)
                $new_certs{$4}{'Not Before'} = $1;
                $new_certs{$4}{'Not After'} = $2;
                $new_certs{$4}{'Created for Kind'} = $3;
                $total_cnt++;
                if (exists $old_certs{$4}) {
                    if ($old_certs{$4}{'Not Before'} eq $new_certs{$4}{'Not Before'} && $old_certs{$4}{'Not After'} eq $new_certs{$4}{'Not After'}) {
                        if ($new_certs{$4}{'Not After'} =~ /^(\d{4})-(\d{2}-\d{2}).+/) {
                            if ($1 > ($current_year + 10)) {
                                # Some certificates seems to have a validity for about 100 years, so check if the certi is valid for more than 10 years
                                $ignored_cnt++;
                            } elsif ($1 == ($current_year + 1)) {
                                # The expiry date is in the next year
                                if ($current_month_day eq "12-31" && $2 eq "01-01") {
                                    # The test is done around the new year and we have not yet switched to the new year
                                    # so mark it as the same.
                                    $same_cnt++;
                                    push @same, $_;
                                } else {
                                    # The certificate is valid for longer than 1 day, so also ignore it.
                                    $ignored_cnt++;
                                }
                            } else {
                                $same_cnt++;
                                push @same, $_;
                            }
                        } else {
                            # We should not come here, but just in case
                            $same_cnt++;
                            push @same, $_;
                        }
                    } elsif ($old_certs{$4}{'Not Before'} ne $new_certs{$4}{'Not Before'} || $old_certs{$4}{'Not After'} ne $new_certs{$4}{'Not After'}) {
                        $different_cnt++;
                    }
                } else {
                    $missing_cnt++;
                }
            }
        }

        General::Logging::log_user_message("Certificates checked: total=$total_cnt, same=$same_cnt, same ignored=$ignored_cnt, changed=$different_cnt, missing=$missing_cnt");

        if ($same_cnt == 0) {
            General::Logging::log_user_message("$description was successful");
            push @::JOB_STATUS, "(/) $description was successful";
        } else {
            $delay = 300;
            General::Logging::log_user_message(sprintf "There are still certificates that have not changed (=same):\n%s\nWaiting %s for all certificates to renew", (join "\n", @same), General::OS_Operations::convert_seconds_to_wdhms($delay, 0));
            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "progress-interval" => 60,
                    "progress-message"  => 1,
                    "seconds"           => $delay,
                    "use-logging"       => 1,
                }
            );
            if ($rc == 1) {
                # CTRL-C pressed
                General::Logging::log_user_error_message(sprintf "Since not all certificates have changed and you interrupted the wait we fail the playlist");
                push @::JOB_STATUS, "(x) $description failed";
                return 1;
            }
            goto DO_IT_AGAIN;
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
sub Perform_Post_Test_Checks_P807S03 {

    my $rc;

    # This second call will perform post test checks and job cleanup
    $::JOB_PARAMS{'P932_TASK'} = "POST_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    return $rc if $rc < 0;

    return $rc;
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P807S99 {

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
    return Playlist::932_Test_Case_Common_Logic::parse_network_config_parameters();
}

# -----------------------------------------------------------------------------
sub set_playlist_variables {
    # Define playlist specific variables that is not already defined in the
    # sub playlist Playlist::932_Test_Case_Common_Logic.
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
        'INTERNAL_CERTIFICATE_RENEWAL_RATIO' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "0.02",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the Renewal ratio for how soon before the internal certificate expire
before it's renewed. A small value will have it renewed long before it would
expire.
Should be specified as fraction of a number below 1.

Using the default value of 0.02 means that it will be renewed after e.g.
88400 * 0.02 = 1768 seconds or about every 29 minutes and 28 seconds.
See also INTERNAL_CERTIFICATE_TTL which controls how long the certificate is
valid for.
EOF
            'validity_mask' => '0\.\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'INTERNAL_CERTIFICATE_TTL' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "88400",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the Time To Live (TTL) for the internal certificates in number of
seconds.

Using the default value of 88400 is the same as 24 hours, meaning that the
certificates are valud for 24 hours.
See also INTERNAL_CERTIFICATE_RENEWAL_RATIO which controls how long before
the certificate expire it is renewed again.
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

This Playlist performs a robustness test case that changes how long the internal
certificates are valid for before they are automatically renewed.

Used Job Parameters:
====================
EOF
    # Special handling for the 3xx_Robustness_Test_xxxx playlists so we print out all variable
    # information from the main playlist and the 932_Test_Case_Common_Logic sub playlist.
    use File::Basename qw(dirname basename);
    my $length;
    my $message;
    my $path_to_932_playlist = dirname(__FILE__) . "/932_Test_Case_Common_Logic.pm";
    my $playlist_name = basename(__FILE__);
    $playlist_name =~ s/\.pm//;
    my %temp_hash = ( %Playlist::932_Test_Case_Common_Logic::playlist_variables, %playlist_variables );

    General::Playlist_Operations::print_info_about_job_variables(\%temp_hash);

    $message = "# Global variable access in playlist $playlist_name #";
    $length = length($message);
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables(__FILE__, \%playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);

    $message = "# Global variable access in playlist 932_Test_Case_Common_Logic #";
    $length = length($message);
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables($path_to_932_playlist, \%Playlist::932_Test_Case_Common_Logic::playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables($path_to_932_playlist);
}

# -----------------------------------------------------------------------------
sub usage_return_playlist_variables {
    return ( %Playlist::932_Test_Case_Common_Logic::playlist_variables, %playlist_variables );
}

1;
