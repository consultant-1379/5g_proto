package Playlist::914_Collect_Logs;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.29
#  Date     : 2024-06-05 10:04:41
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
use General::Logging;
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

    my $rc = 0;

    $::JOB_PARAMS{'COLLECT_LOGS'} = "yes" unless (exists $::JOB_PARAMS{'COLLECT_LOGS'});

    if ($::JOB_PARAMS{'COLLECT_LOGS'} eq "yes") {
        $rc = General::Playlist_Operations::execute_step( \&Collect_ADP_Logs_P914S01, undef );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_step( \&Collect_Other_Information_P914S02, undef );
        return $rc if $rc < 0;
    } else {
        General::Logging::log_user_message("Log collection not done because 'COLLECT_LOGS' is not 'yes'.");
        $rc = General::Playlist_Operations::RC_TASKOUT;
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
sub Collect_ADP_Logs_P914S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Locate_And_Run_Collect_ADP_Logs_Script_P914S01T01 } );
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
    sub Locate_And_Run_Collect_ADP_Logs_Script_P914S01T01 {

        my $command = "";
        my $current_directory = `pwd`;
        $current_directory =~ s/[\r\n]//g;
        my $message = "";
        my $rc = 0;
        my @result;
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $script_file = General::OS_Operations::get_log_collect_script( { "search-paths" => [ "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/tools", "$::JOB_PARAMS{'_PACKAGE_DIR'}/bin" ] } );

        if ($script_file eq "") {
            General::Logging::log_user_warning_message("Log collection script not found.\nNo collection of ADP logs can be done.");
            push @::JOB_STATUS, "(-) Collection of ADP logs skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        unless (-d "$::JOB_PARAMS{'_JOB_TROUBLESHOOTING_LOG_DIR'}") {
            # If we end up here we failed the job very early before the directory
            # have been created and there is no point in creating the ADP logs since
            # nothing has really been done to the cluster.
            General::Logging::log_user_warning_message("The directory for storing ADP Logs does not exist, skipping the collection");
            push @::JOB_STATUS, "(-) Collection of ADP logs skipped";
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # The tool needs to be run from the current directory so change to it
        chdir "$::JOB_PARAMS{'_JOB_TROUBLESHOOTING_LOG_DIR'}";

        # Different scripts needs different parameters
        if ($script_file =~ /data_collector\.sh/) {
            # -s : Collect logs containing sensitive data
            $command = "$script_file -s $sc_namespace";
        } elsif ($script_file =~ /collect_ADP_logs\.sh/) {
            # -s : Also kubernetes logs from system namespaces will be collected
            $command = "$script_file -s -n $sc_namespace";
        } else {
            # Unknown command, we just assume we can execute the command as-is
            $command = $script_file;
        }

        General::Logging::log_user_message("Collecting ADP Logs.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Collection of ADP logs successful";

            if (exists $::JOB_PARAMS{'RENAME_ADP_LOG_TO_INCLUDE_JOBNAME'} && $::JOB_PARAMS{'RENAME_ADP_LOG_TO_INCLUDE_JOBNAME'} eq "no") {
                General::Logging::log_user_message("Rename of ADP log file not wanted because RENAME_ADP_LOG_TO_INCLUDE_JOBNAME=no");
            } else {
                # Look for the generated tgz file, for example:
                # Generated file /home/eccd/workspaces/mc-ipv4-5576_IaaS-upgrade-3092_Upgrade_SCP_from_1.12.0+43_to_1.13.25+662_20230901_122003_72/troubleshooting_logs/logs_eiffelesc_2023-09-01-12-30-24.tgz, Please collect and send to ADP or SC support!
                # And rename the file so it includes the job name.
                my $old_name = "";
                my $new_name = "";
                for (@result) {
                    if (/Generated file\s+(.+\/troubleshooting_logs)\/(logs_.+\.tgz).*/) {
                        $old_name = "$1/$2";
                        $new_name = "$1/$::JOB_PARAMS{'JOBNAME'}_$2";
                        last;
                    }
                }
                if ($new_name ne "") {
                    $rc = General::OS_Operations::send_command(
                        {
                            "command"       => "mv $old_name $new_name",
                            "hide-output"   => 1,
                        }
                    );
                    if ($rc == 0) {
                        General::Logging::log_user_message("Renamed ADP log file from: $old_name\n                       to: $new_name\n");
                    } else {
                        # Display the result in case of error
                        General::OS_Operations::write_last_temporary_output_to_progress();

                        General::Logging::log_user_message("Unable to rename ADP log file because command failed");
                        $rc = 0;
                    }
                } else {
                    General::Logging::log_user_warning_message("Unable to rename ADP log file because no filename found in printout");
                }
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to collect ADP logs, error ignored.");
            push @::JOB_STATUS, "(x) Collection of ADP logs failed";
            $rc = 0;
        }

        # Go back to the previous current directory
        chdir "$current_directory";

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
sub Collect_Other_Information_P914S02 {

    my $rc;
    my $trouble_shooting_logs = 0;  # Change to 1 to enable collection of trouble shooting logs

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Local_Repo_Disk_Usage_P914S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Search_Engine_Error_Information_P914S02T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Secrets_P914S02T03 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Search_Engine_Data_P914S02T04 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_LDAP_Data_P914S02T05 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Search_Engine_Data_DND43712_P914S02T06 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Object_Storage_Data_P914S02T07 } );
    return $rc if $rc < 0;

    if ($trouble_shooting_logs == 1) {

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Fetch_Diameter_Certificates_GSSUPP_12317_P914S02T98 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Fetch_Diameter_Logs_GSSUPP_12317_P914S02T99 } );
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
    sub Check_Local_Repo_Disk_Usage_P914S02T01 {

        my $message = "";
        my $own_registry_url = exists $::JOB_PARAMS{'OWN_REGISTRY_URL'} ? $::JOB_PARAMS{'OWN_REGISTRY_URL'} : "";
        my $rc = 0;
        my $registry_pod = "";
        my @result = ();
        my $warning_level = 90;

        if ($own_registry_url eq "") {
            General::Logging::log_user_message("No local registry exists, so nothing to check");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Finding local registry pod");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n kube-system get pod",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to execute command to find local registry pod, error ignored.");
            return General::Playlist_Operations::RC_TASKOUT;
        }
        for (@result) {
            if (/^(eric-lcm-container-registry-registry-\S+)/) {
                $registry_pod = $1;
            }
        }
        if ($registry_pod eq "") {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to find local registry pod named 'eric-lcm-container-registry-registry-*', error ignored.");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Checking local registry disk utilization");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n kube-system exec -i -t $registry_pod -c registry -- /usr/bin/df",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            # Check if the registry utilization is higher than the warning level
            my $disk_utilization = 0;
            for (@result) {
                if (/\s+(\d+)\%\s+\S+var\/lib\/registry\s*$/) {
                    # /dev/vdc           10016  5634      4367  57% /var/lib/registry
                    $disk_utilization = $1;
                    last;
                }
            }
            if ($disk_utilization < $warning_level) {
                General::Logging::log_user_message("Local registry disk utilization is $disk_utilization\% which is lower than the $warning_level\% warning level");
            } else {
                General::Logging::log_user_warning_message("Warning: Local registry disk utilization is $disk_utilization\% which is higher or equal to the $warning_level\% warning level");
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check local registry disk utilization, error ignored.");
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
    sub Print_Search_Engine_Error_Information_P914S02T02 {

        my $debug_messages = 1;     # Change this to 0 if no extra information should be logged to the all.log
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;

        # Collect extra information in case there is something wrong, ignore the returned data
        # because all we want is to log it to the all.log
        ADP::Kubernetes_Operations::get_log_cluster_allocation(
            {
                "debug-messages"        => $debug_messages,
                "namespace"             => $namespace,
            }
        );

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
    sub Print_Secrets_P914S02T03 {

        my $rc = 0;

        General::Logging::log_user_message("Printing secrets from namespace $::JOB_PARAMS{'SC_NAMESPACE'}");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get secrets -o yaml -n $::JOB_PARAMS{'SC_NAMESPACE'}",
                "hide-output"   => 1,
                "save-to-file"  => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/sc-secrets.yaml",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to fetch secrets, error ignored.");
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
    sub Print_Search_Engine_Data_P914S02T04 {

        my $kubectl_command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} exec -it -c ingest";
        my @pod_names = ADP::Kubernetes_Operations::get_pod_names(
            {
                "namespace"         => $::JOB_PARAMS{'SC_NAMESPACE'},
                "pod-include-list"  => [ "eric-data-search-engine-ingest-tls-.+" ],
                "hide-output"       => 1,
            }
        );
        my $rc = 0;

        for my $pod_name (@pod_names) {
            # Collect extra information in case there is something wrong, ignore the returned data
            # because all we want is to log it to the all.log
            General::Logging::log_user_message("Printing curl information from pod $pod_name");

            $rc = General::File_Operations::write_file(
                {
                    "filename"            => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/collect_info_$pod_name.bash",
                    "output-ref"          => [
                        "echo '---------------------------------------------------------------------------------------'",
                        "echo $kubectl_command $pod_name -- esRest GET / -vI",
                        "$kubectl_command $pod_name -- esRest GET / -vI",
                        "echo '---------------------------------------------------------------------------------------'",
                        "echo $kubectl_command $pod_name -- curl --cert /run/secrets/http-client-certificates/clicert.pem --key /run/secrets/http-client-certificates/cliprivkey.pem --cacert /run/secrets/sip-tls-trusted-root-cert/ca.crt https://localhost:9200/ -vl",
                        "$kubectl_command $pod_name -- curl --cert /run/secrets/http-client-certificates/clicert.pem --key /run/secrets/http-client-certificates/cliprivkey.pem --cacert /run/secrets/sip-tls-trusted-root-cert/ca.crt https://localhost:9200/ -vl",
                        "echo '---------------------------------------------------------------------------------------'",
                        "echo $kubectl_command $pod_name -- curl --cert /run/secrets/http-client-certificates/clicert.pem --key /run/secrets/http-client-certificates/cliprivkey.pem --cacert /run/secrets/sip-tls-trusted-root-cert/ca.crt https://localhost:9200/ -vI",
                        "$kubectl_command $pod_name -- curl --cert /run/secrets/http-client-certificates/clicert.pem --key /run/secrets/http-client-certificates/cliprivkey.pem --cacert /run/secrets/sip-tls-trusted-root-cert/ca.crt https://localhost:9200/ -vI",
                        "echo '---------------------------------------------------------------------------------------'",
                        "echo $kubectl_command $pod_name -- curl --cert /run/secrets/http-client-certificates/clicert.pem --key /run/secrets/http-client-certificates/cliprivkey.pem --cacert /run/secrets/sip-tls-trusted-root-cert/ca.crt https://eric-data-search-engine-tls:9200/ -vI",
                        "$kubectl_command $pod_name -- curl --cert /run/secrets/http-client-certificates/clicert.pem --key /run/secrets/http-client-certificates/cliprivkey.pem --cacert /run/secrets/sip-tls-trusted-root-cert/ca.crt https://eric-data-search-engine-tls:9200/ -vI",
                        "echo '---------------------------------------------------------------------------------------'",
                        "echo $kubectl_command $pod_name -- openssl x509 -noout -text -in /run/secrets/http-client-certificates/clicert.pem",
                        "$kubectl_command $pod_name -- openssl x509 -noout -text -in /run/secrets/http-client-certificates/clicert.pem",
                        "echo '---------------------------------------------------------------------------------------'",
                        "echo $kubectl_command $pod_name -- openssl x509 -noout -text -in /run/secrets/http-certificates/srvcert.pem",
                        "$kubectl_command $pod_name -- openssl x509 -noout -text -in /run/secrets/http-certificates/srvcert.pem",
                        "echo '---------------------------------------------------------------------------------------'",
                        "echo $kubectl_command $pod_name -- openssl x509 -noout -text -in /run/secrets/http-ca-certificates/client-cacertbundle.pem",
                        "$kubectl_command $pod_name -- openssl x509 -noout -text -in /run/secrets/http-ca-certificates/client-cacertbundle.pem",
                        "echo '---------------------------------------------------------------------------------------'",
                        "echo $kubectl_command $pod_name -- bash -c 'echo -n | openssl s_client -showcerts -noservername -connect localhost:9200 -CAfile /run/secrets/sip-tls-trusted-root-cert/ca.crt -cert /run/secrets/http-client-certificates/clicert.pem -key /run/secrets/http-client-certificates/cliprivkey.pem'",
                        "$kubectl_command $pod_name -- bash -c 'echo -n | openssl s_client -showcerts -noservername -connect localhost:9200 -CAfile /run/secrets/sip-tls-trusted-root-cert/ca.crt -cert /run/secrets/http-client-certificates/clicert.pem -key /run/secrets/http-client-certificates/cliprivkey.pem'",
                        "echo '---------------------------------------------------------------------------------------'",
                    ],
                    "file-access-mode"    => "755"
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to write the collect_info_$pod_name.bash file");
                return 0;
            }

            General::OS_Operations::send_command(
                {
                    "command"       => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/collect_info_$pod_name.bash",
                    "hide-output"   => 1,
                    "save-to-file"  => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/pod_info_$pod_name.log",
                }
            );
        }

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
    sub Print_LDAP_Data_P914S02T05 {

        my $kubectl_command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} get secret";
        my $rc = 0;

        $rc = General::File_Operations::write_file(
            {
                "filename"            => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/collect_info_ldap.bash",
                "output-ref"          => [
                    "echo '---------------------------------------------------------------------------------------'",
                    "echo eric-sec-admin-user-management-ldap-cert - cert.pem",
                    "$kubectl_command eric-sec-admin-user-management-ldap-cert -o json | jq '.data[\"cert.pem\"]' | cut -f 2 -d '\"'| base64 -d | openssl x509 -noout -text",
                    "echo '---------------------------------------------------------------------------------------'",
                    "echo eric-sec-admin-user-management-ldap-cert - key.pem",
                    "$kubectl_command eric-sec-admin-user-management-ldap-cert -o json | jq '.data[\"key.pem\"]' | cut -f 2 -d '\"'| base64 -d | openssl x509 -noout -text",
                    "echo '-------'",
                    "$kubectl_command eric-sec-admin-user-management-ldap-cert -o json | jq '.data[\"key.pem\"]' | cut -f 2 -d '\"'| base64 -d",
                    "echo '---------------------------------------------------------------------------------------'",
                    "echo eric-sec-sip-tls-trusted-root-cert - ca.crt",
                    "$kubectl_command eric-sec-sip-tls-trusted-root-cert -o json | jq '.data[\"ca.crt\"]' | cut -f 2 -d '\"'| base64 -d | openssl x509 -noout -text",
                    "echo '---------------------------------------------------------------------------------------'",
                    "echo eric-sec-sip-tls-trusted-root-cert - cacertbundle.pem",
                    "$kubectl_command eric-sec-sip-tls-trusted-root-cert -o json | jq '.data[\"cacertbundle.pem\"]' | cut -f 2 -d '\"'| base64 -d | openssl x509 -noout -text",
                    "echo '---------------------------------------------------------------------------------------'",
                    "echo eric-sec-ldap-server-ldapagent-client-certs - clicert.pem",
                    "$kubectl_command eric-sec-ldap-server-ldapagent-client-certs -o json | jq '.data[\"clicert.pem\"]' | cut -f 2 -d '\"'| base64 -d | openssl x509 -noout -text",
                    "echo '---------------------------------------------------------------------------------------'",
                    "echo eric-sec-ldap-server-ldapagent-client-certs - cliprivkey.pem",
                    "$kubectl_command eric-sec-ldap-server-ldapagent-client-certs -o json | jq '.data[\"cliprivkey.pem\"]' | cut -f 2 -d '\"'| base64 -d | openssl x509 -noout -text",
                    "echo '-------'",
                    "$kubectl_command eric-sec-ldap-server-ldapagent-client-certs -o json | jq '.data[\"cliprivkey.pem\"]' | cut -f 2 -d '\"'| base64 -d",
                    "echo '---------------------------------------------------------------------------------------'",
                ],
                "file-access-mode"    => "755"
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to write the collect_info_ldap.bash file");
            return 0;
        }

        # Collect extra information in case there is something wrong, ignore the returned data
        # because all we want is to log it to the all.log
        General::Logging::log_user_message("Printing LDAP information");
        General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/collect_info_ldap.bash",
                "hide-output"   => 1,
                "save-to-file"  => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/secret_info_ldap.log",
            }
        );

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
    sub Print_Search_Engine_Data_DND43712_P914S02T06 {

        my $kubectl_command;
        my @pod_names = ADP::Kubernetes_Operations::get_pod_names(
            {
                "namespace"         => $::JOB_PARAMS{'SC_NAMESPACE'},
                "pod-include-list"  => [ "eric-data-search-engine-data-.+", "eric-data-search-engine-ingest-tls-.+", "eric-data-search-engine-master-.+" ],
                "hide-output"       => 1,
            }
        );
        my $rc = 0;

        for my $pod_name (@pod_names) {
            # Collect extra information in case there is something wrong, ignore the returned data
            # because all we want is to log it to the all.log
            General::Logging::log_user_message("Printing curl information from pod $pod_name");

            if ($pod_name =~ /^eric-data-search-engine-ingest-tls-/) {
                $kubectl_command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} exec -it -c ingest";
                $rc = General::File_Operations::write_file(
                    {
                        "filename"            => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/collect_info_DND43712_$pod_name.bash",
                        "output-ref"          => [
                            "echo '---------------------------------------------------------------------------------------'",
                            "echo $kubectl_command $pod_name -- esRest GET /_cat/nodes?v",
                            "$kubectl_command $pod_name -- esRest GET /_cat/nodes?v",
                            "echo '---------------------------------------------------------------------------------------'",
                            "echo $kubectl_command $pod_name -- curl --cert /run/secrets/transport-certificates/srvcert.pem --key /run/secrets/transport-certificates/srvprivkey.pem --cacert /run/secrets/transport-ca-certificates/client-cacertbundle.pem  https://localhost:9300/ -vI",
                            "$kubectl_command $pod_name -- curl --cert /run/secrets/transport-certificates/srvcert.pem --key /run/secrets/transport-certificates/srvprivkey.pem --cacert /run/secrets/transport-ca-certificates/client-cacertbundle.pem  https://localhost:9300/ -vI",
                            "echo '---------------------------------------------------------------------------------------'",
                            "echo $kubectl_command $pod_name -- bash -c 'echo -n | openssl s_client -showcerts -noservername -connect localhost:9300 -cert /run/secrets/transport-certificates/srvcert.pem -key /run/secrets/transport-certificates/srvprivkey.pem -CAfile /run/secrets/transport-ca-certificates/client-cacertbundle.pem'",
                            "$kubectl_command $pod_name -- bash -c 'echo -n | openssl s_client -showcerts -noservername -connect localhost:9300 -cert /run/secrets/transport-certificates/srvcert.pem -key /run/secrets/transport-certificates/srvprivkey.pem -CAfile /run/secrets/transport-ca-certificates/client-cacertbundle.pem'",
                            "echo '---------------------------------------------------------------------------------------'",
                        ],
                        "file-access-mode"    => "755"
                    }
                );
            } elsif ($pod_name =~ /^eric-data-search-engine-data-/) {
                $kubectl_command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} exec -it -c data";
                $rc = General::File_Operations::write_file(
                    {
                        "filename"            => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/collect_info_DND43712_$pod_name.bash",
                        "output-ref"          => [
                            "echo '---------------------------------------------------------------------------------------'",
                            "echo $kubectl_command $pod_name -- esRest GET /_cat/nodes?v",
                            "$kubectl_command $pod_name -- esRest GET /_cat/nodes?v",
                            "echo '---------------------------------------------------------------------------------------'",
                            "echo $kubectl_command $pod_name -- curl --cert /run/secrets/transport-certificates/srvcert.pem --key /run/secrets/transport-certificates/srvprivkey.pem --cacert /run/secrets/transport-ca-certificates/client-cacertbundle.pem  https://localhost:9300/ -vI",
                            "$kubectl_command $pod_name -- curl --cert /run/secrets/transport-certificates/srvcert.pem --key /run/secrets/transport-certificates/srvprivkey.pem --cacert /run/secrets/transport-ca-certificates/client-cacertbundle.pem  https://localhost:9300/ -vI",
                            "echo '---------------------------------------------------------------------------------------'",
                            "echo $kubectl_command $pod_name -- bash -c 'echo -n | openssl s_client -showcerts -noservername -connect localhost:9300 -cert /run/secrets/transport-certificates/srvcert.pem -key /run/secrets/transport-certificates/srvprivkey.pem -CAfile /run/secrets/transport-ca-certificates/client-cacertbundle.pem'",
                            "$kubectl_command $pod_name -- bash -c 'echo -n | openssl s_client -showcerts -noservername -connect localhost:9300 -cert /run/secrets/transport-certificates/srvcert.pem -key /run/secrets/transport-certificates/srvprivkey.pem -CAfile /run/secrets/transport-ca-certificates/client-cacertbundle.pem'",
                            "echo '---------------------------------------------------------------------------------------'",
                        ],
                        "file-access-mode"    => "755"
                    }
                );
            } elsif ($pod_name =~ /^eric-data-search-engine-master-/) {
                $kubectl_command = "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} -n $::JOB_PARAMS{'SC_NAMESPACE'} exec -it -c master";
                $rc = General::File_Operations::write_file(
                    {
                        "filename"            => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/collect_info_DND43712_$pod_name.bash",
                        "output-ref"          => [
                            "echo '---------------------------------------------------------------------------------------'",
                            "echo $kubectl_command $pod_name -- esRest GET /_cat/nodes?v",
                            "$kubectl_command $pod_name -- esRest GET /_cat/nodes?v",
                            "echo '---------------------------------------------------------------------------------------'",
                            "echo $kubectl_command $pod_name -- curl --cert /run/secrets/transport-certificates/srvcert.pem --key /run/secrets/transport-certificates/srvprivkey.pem --cacert /run/secrets/transport-ca-certificates/client-cacertbundle.pem  https://localhost:9300/ -vI",
                            "$kubectl_command $pod_name -- curl --cert /run/secrets/transport-certificates/srvcert.pem --key /run/secrets/transport-certificates/srvprivkey.pem --cacert /run/secrets/transport-ca-certificates/client-cacertbundle.pem  https://localhost:9300/ -vI",
                            "echo '---------------------------------------------------------------------------------------'",
                            "echo $kubectl_command $pod_name -- bash -c 'echo -n | openssl s_client -showcerts -noservername -connect localhost:9300 -cert /run/secrets/transport-certificates/srvcert.pem -key /run/secrets/transport-certificates/srvprivkey.pem -CAfile /run/secrets/transport-ca-certificates/client-cacertbundle.pem'",
                            "$kubectl_command $pod_name -- bash -c 'echo -n | openssl s_client -showcerts -noservername -connect localhost:9300 -cert /run/secrets/transport-certificates/srvcert.pem -key /run/secrets/transport-certificates/srvprivkey.pem -CAfile /run/secrets/transport-ca-certificates/client-cacertbundle.pem'",
                            "echo '---------------------------------------------------------------------------------------'",
                        ],
                        "file-access-mode"    => "755"
                    }
                );
            }
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to write the collect_info_DND43712_$pod_name.bash file");
                return 0;
            }

            General::OS_Operations::send_command(
                {
                    "command"       => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/collect_info_DND43712_$pod_name.bash",
                    "hide-output"   => 1,
                    "save-to-file"  => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/pod_info_DND43712_$pod_name.log",
                }
            );
        }

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
    sub Print_Object_Storage_Data_P914S02T07 {

        my $kubectl_command;

        General::OS_Operations::send_command(
            {
                "commands"      => [
                    "openssl s_client -connect eric-data-object-storage-mn:9000 -CAfile /run/secrets/cacert/cacertbundle.pem  -cert /run/secrets/obs/clicert.pem -key /run/secrets/obs/cliprivkey.pem -state",
                    "mc admin info client",
                    "curl -k https://eric-data-object-storage-mn:9000/minio/health/cluster --head",
                    "curl -k https://eric-data-object-storage-mn:9000/minio/health/cluster/read --head",
                ],
                "command-in-output" => 1,
                "hide-output"   => 1,
                "save-to-file"  => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/object_storage_information.log",
            }
        );

        return 0;
    }

    # -----------------------------------------------------------------------------
    # This task should be seen as a temporary task that is implemented to collect
    # for information for a specific support case GSSUPP-12317, so when the
    # information is no longer needed then this task can be removed.
    # For details see: https://eteamproject.internal.ericsson.com/browse/GSSUPP-12317
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
    sub Fetch_Diameter_Certificates_GSSUPP_12317_P914S02T98 {

        my $dir_name;
        my $error_cnt = 0;
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $pod_dir;
        my @pod_names = ADP::Kubernetes_Operations::get_pod_names(
            {
                "namespace"         => $namespace,
                "pod-include-list"  => [ "eric-bsf-diameter-.+", "eric-stm-diameter-.+" ],
                "hide-output"   => 1,
            }
        );
        my $rc = 0;
        my $time_stamp = time();

        # Only execute this during an Upgrade job
        if ($::JOB_PARAMS{'JOBTYPE'} ne "UPGRADE") {
            General::Logging::log_user_message("This task is only applicable for an UPGRADE job, so nothing to fetch");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Check if we have diameter pods deployed
        if (scalar @pod_names == 0) {
            $::JOB_PARAMS{'GSSUPP-12317_COLLECT_DATA'} = "no";
            General::Logging::log_user_message("No diameter pods found, so nothing to fetch");
            return General::Playlist_Operations::RC_TASKOUT;
        } else {
            $::JOB_PARAMS{'GSSUPP-12317_COLLECT_DATA'} = "yes";
        }

        General::Playlist_Operations::register_workaround_task("A workaround has been added to collect more data for a support request, see GSSUPP-12317.");

        for my $pod_name (@pod_names) {
            # Create a directory structure for storing the data
            $dir_name = "$::JOB_PARAMS{'_JOB_TROUBLESHOOTING_LOG_DIR'}/GSSUPP-12317/certificates_$time_stamp/$pod_name";
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "mkdir -p $dir_name",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                $error_cnt++;
                General::Logging::log_user_warning_message("Failed to create directory: $dir_name");
                next;
            }

            # Fetch the ca certificate files
            $pod_dir = "/run/secrets/dsd/ca";
            for my $file_name ("ca.crt", "cacertbundle.pem") {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "kubectl -n $namespace exec -it $pod_name -c dsl -- cat $pod_dir/$file_name",
                        "hide-output"   => 1,
                        "save-to-file"  => "$dir_name/$file_name",
                    }
                );
                if ($rc != 0) {
                    $error_cnt++;
                    General::Logging::log_user_warning_message("Failed to fetch file $pod_dir/$file_name from pod $pod_name");
                    next;
                }
            }

            # Fetch the internal certificate files
            $pod_dir = "/run/secrets/dsd/internal";
            for my $file_name ("cert.pem", "key.pem") {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "kubectl -n $namespace exec -it $pod_name -c dsl -- cat $pod_dir/$file_name",
                        "hide-output"   => 1,
                        "save-to-file"  => "$dir_name/$file_name",
                    }
                );
                if ($rc != 0) {
                    $error_cnt++;
                    General::Logging::log_user_warning_message("Failed to fetch file $pod_dir/$file_name from pod $pod_name");
                    next;
                }
            }
        }

        if ($error_cnt > 0) {
            General::Logging::log_user_warning_message("Fetching of Diameter Certificates failed for some reason, but classified as a warning since it's not a critical task.");
        }

        # Always return success
        return 0;
    }

    # -----------------------------------------------------------------------------
    # This task should be seen as a temporary task that is implemented to collect
    # for information for a specific support case GSSUPP-12317, so when the
    # information is no longer needed then this task can be removed.
    # For details see: https://eteamproject.internal.ericsson.com/browse/GSSUPP-12317
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
    sub Fetch_Diameter_Logs_GSSUPP_12317_P914S02T99 {

        my $command;
        my $dir_name = "$::JOB_PARAMS{'_JOB_TROUBLESHOOTING_LOG_DIR'}/GSSUPP-12317/pod_logs";
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $pid;
        my $rc = 0;

        # Only execute this during an Upgrade job
        if ($::JOB_PARAMS{'JOBTYPE'} ne "UPGRADE") {
            General::Logging::log_user_message("This task is only applocable for an UPGRADE job, so nothing to fetch");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Check if we have diameter pods deployed
        if ($::JOB_PARAMS{'GSSUPP-12317_COLLECT_DATA'} eq "no") {
            General::Logging::log_user_message("No diameter pods found, so nothing to fetch");
            return General::Playlist_Operations::RC_TASKOUT;
        } elsif (exists $::JOB_PARAMS{'DIAMETER_LOG_COLLECTION_PID'} && $::JOB_PARAMS{'DIAMETER_LOG_COLLECTION_PID'} != 0) {
            General::Logging::log_user_message("Collection of Diameter logs is already running in the background, trying to stop it");

            $pid = $::JOB_PARAMS{'DIAMETER_LOG_COLLECTION_PID'};

            if (General::OS_Operations::background_process_is_running($pid) == 0) {
                General::Logging::log_user_message("Background process id $pid not found or is no longer running");
                return General::Playlist_Operations::RC_TASKOUT;
            }

            General::Logging::log_user_message("Stopping background process id $pid that is collecting diameter logs");

            $rc = General::OS_Operations::background_process_send_signal($pid, "SIGABRT");
            if ($rc == 0) {
                General::Logging::log_user_warning_message("Failed to send the SIGABRT signal to process id $pid");
                return General::Playlist_Operations::RC_TASKOUT;
            }

            General::Logging::log_user_message("Waiting for pid $pid to terminate");

            my $stop_time = time() + 60;
            while (General::OS_Operations::background_process_is_running($pid) == 1) {
                # Wait for any child (-1) to terminate in non-blocking (0) mode
                if (waitpid(-1,0) == $pid) {
                    # Child has been terminated
                    General::Logging::log_user_message("The background process with pid $pid has terminated");
                    last;
                }

                if (time() >= $stop_time) {
                    General::Logging::log_user_warning_message("Timeout after 60 seconds waiting for the process to terminate");
                    return General::Playlist_Operations::RC_TASKOUT;
                }

                General::Logging::log_user_message("The process is still running, waiting 10 seconds for it to terminate");
                $rc = General::OS_Operations::sleep_with_progress(
                    {
                        "allow-interrupt"   => 1,
                        "progress-message"  => 0,
                        "seconds"           => 10,
                        "use-logging"       => 1,
                    }
                );
                if ($rc == 1) {
                    # CTRL-C pressed
                    last;
                }
            }

            $::JOB_PARAMS{'DIAMETER_LOG_COLLECTION_PID'} = 0;
        } elsif (exists $::JOB_PARAMS{'DIAMETER_LOG_COLLECTION_PID'} && $::JOB_PARAMS{'DIAMETER_LOG_COLLECTION_PID'} == 0) {
            General::Logging::log_user_message("Collection of Diameter logs have already been stopped");
            return General::Playlist_Operations::RC_TASKOUT;
        } else {

            General::Playlist_Operations::register_workaround_task("A workaround has been added to collect more data for a support request, see GSSUPP-12317.");

            $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/trouble_shooting_collect_pod_logs.pl -n $namespace --no-tty -o $dir_name -p 'eric-bsf-diameter-.+,dsl' -p 'eric-stm-diameter-.+,dsl'";
            $pid = General::OS_Operations::background_process_run($command);
            $::JOB_PARAMS{'DIAMETER_LOG_COLLECTION_PID'} = $pid;
            if ($pid > 0) {
                General::Logging::log_user_message("Fetching of Diameter Logs in the background started with process id $pid.");
            } else {
                General::Logging::log_user_warning_message("Fetching of Diameter Logs in the background failed, but classified as a warning since it's not a critical task.");
            }
        }

        # Always return success
        return 0;
    }
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

This Playlist is used for collecting log files and other information that might
be useful when trouble shooting problems.

To skip the log collection done by this playlist, set the job parameter
\$::JOB_PARAMS{'COLLECT_LOGS'}="no" in your playlist or with '-v COLLECT_LOGS=no'
when executing the playlist with script 'execute_playlist.pl'.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
