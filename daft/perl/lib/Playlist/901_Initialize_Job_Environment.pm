package Playlist::901_Initialize_Job_Environment;

################################################################################
#
#  Author   : eustone, everhel
#
#  Revision : 1.37
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

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    usage
    usage_return_playlist_variables
    );

use ADP::Kubernetes_Operations;
use General::File_Operations;
use General::Logging;
use General::Playlist_Operations;
use Playlist::920_Check_Network_Config_Information;

#
# Variable Declarations
#
my %playlist_variables;
set_playlist_variables();

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

    $rc = General::Playlist_Operations::execute_step( \&Check_Environment_P901S01, \&Fallback001_P901S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Create_Configuration_File_P901S02, \&Fallback001_P901S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Create_Job_Directories_P901S03, \&Fallback001_P901S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Collect_Miscellaneous_Information_P901S04, \&Fallback001_P901S99 );
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
sub Check_Environment_P901S01 {

    my $rc;

    # Initialize this variable if it does not exist
    $::JOB_PARAMS{'DRY_RUN'} = "no" unless exists $::JOB_PARAMS{'DRY_RUN'};

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_for_Perl_P901S01T01, "dry-run-job" => $::JOB_PARAMS{'DRY_RUN'}, "dry-run-task" => 1 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_for_KUBECONFIG_P901S01T02, "dry-run-job" => $::JOB_PARAMS{'DRY_RUN'}, "dry-run-task" => 1 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Read_Some_Network_Config_Parameters_P901S01T03, "dry-run-job" => $::JOB_PARAMS{'DRY_RUN'}, "dry-run-task" => 1 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_for_helm_P901S01T04, "dry-run-job" => $::JOB_PARAMS{'DRY_RUN'}, "dry-run-task" => 1 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_for_kubectl_P901S01T05, "dry-run-job" => $::JOB_PARAMS{'DRY_RUN'}, "dry-run-task" => 1 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Server_Health_P901S01T06, "dry-run-job" => $::JOB_PARAMS{'DRY_RUN'}, "dry-run-task" => 1 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_If_docker_or_nerdctl_Command_Require_sudo_P901S01T07, "dry-run-job" => $::JOB_PARAMS{'DRY_RUN'}, "dry-run-task" => 1 } );
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
    sub Check_for_Perl_P901S01T01 {

        General::Logging::log_user_message("Checking if Perl exist in \$PATH\n");
        my $rc = General::OS_Operations::send_command(
            {
                "command"       => "which perl",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            return 0;
        } else {
            return General::Playlist_Operations::RC_FALLBACK;
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
    sub Check_for_KUBECONFIG_P901S01T02 {

        if (exists $::JOB_PARAMS{'KUBECONFIG'} && $::JOB_PARAMS{'KUBECONFIG'} ne "") {
            # Update the package to start using the kubeconfig file
            ADP::Kubernetes_Operations::set_used_kubeconfig_filename($::JOB_PARAMS{'KUBECONFIG'});
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
    sub Read_Some_Network_Config_Parameters_P901S01T03 {
        my $rc = 0;

        if ($::network_file ne "") {
            unless (exists $::NETWORK_CONFIG_PARAMS{'helm_version'} &&
                    exists $::NETWORK_CONFIG_PARAMS{'cluster_ssh_vip'} &&
                    exists $::NETWORK_CONFIG_PARAMS{'sc_namespace'} &&
                    exists $::NETWORK_CONFIG_PARAMS{'sc_release_name'} &&
                    exists $::NETWORK_CONFIG_PARAMS{'tools_namespace'} &&
                    exists $::NETWORK_CONFIG_PARAMS{'crd_namespace'} &&
                    exists $::NETWORK_CONFIG_PARAMS{'crd_release_name'}) {
                # Parse only a few of the network configuration file parameters because we need them in the early checks before some playlists might have read them.
                # All other network configuration parameters will be parsed later after we have found the software to be deployed for the deploy playlist
                # or the current running software for other playlists.
                General::Logging::log_user_message("Reading a few network configuration file parameters");
                $rc = General::File_Operations::parse_network_configuration_file(
                    {
                        "filename"              => "$::network_file",
                        "output-ref"            => \%::NETWORK_CONFIG_PARAMS,
                        "include-ref"           => [ 'name="helm_version"', 'name="cluster_ssh_vip"', 'name="sc_namespace"', 'name="sc_release_name"', 'name="tools_namespace"', 'name="crd_namespace"', 'name="crd_release_name"', ],
                        "validator-ref"         => \&Playlist::920_Check_Network_Config_Information::validate_network_config_parameter,
                        "hide-error-messages"   => 1,
                    }
                );
                if ($rc != 0) {
                    General::Logging::log_user_error_message("Failed to read parameters from the network configuration file: $::network_file");
                    return General::Playlist_Operations::RC_FALLBACK;
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
    sub Check_for_helm_P901S01T04 {

        my $helm_information = undef;
        my $helm_version = "";
        my $kubeconfig = "";
        my $kubeconfig_tools = "";

        if (exists $::JOB_PARAMS{'KUBECONFIG'} && $::JOB_PARAMS{'KUBECONFIG'} ne "") {
            $kubeconfig = " --kubeconfig=$::JOB_PARAMS{'KUBECONFIG'}";
        }
        if (exists $::JOB_PARAMS{'KUBECONFIG_TOOLS'} && $::JOB_PARAMS{'KUBECONFIG_TOOLS'} ne "") {
            $kubeconfig_tools = " --kubeconfig=$::JOB_PARAMS{'KUBECONFIG_TOOLS'}";
        }

        # Initialize variables for helm command
        $::JOB_PARAMS{'HELM_EXECUTABLE'} = "";
        $::JOB_PARAMS{'HELM2_EXECUTABLE'} = "";
        $::JOB_PARAMS{'HELM3_EXECUTABLE'} = "";

        if (exists $::JOB_PARAMS{'SKIP_HELM_CHECK'} && $::JOB_PARAMS{'SKIP_HELM_CHECK'} eq "yes") {
            # The Playlist does not use the helm command so no check should be done
            General::Logging::log_user_message("Check of 'helm' command not wanted because SKIP_HELM_CHECK=yes");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Get Helm version
        General::Logging::log_user_message("Fetch helm executable based on Job parameter 'HELM_VERSION' and Network Config parameter 'helm_version'");
        if (exists $::JOB_PARAMS{'HELM_VERSION'} && $::JOB_PARAMS{'HELM_VERSION'} ne "") {
            $helm_version = $::JOB_PARAMS{'HELM_VERSION'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'helm_version'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'helm_version'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'HELM_VERSION'} = $::NETWORK_CONFIG_PARAMS{'helm_version'}{'value'};
                $helm_version = $::JOB_PARAMS{'HELM_VERSION'};
            } else {
                General::Logging::log_user_warning_message("Network Config parameter 'helm_version' is CHANGEME and Job parameter 'HELM_VERSION' not provided\nDefaulting to helm version 3\n");
                $::JOB_PARAMS{'HELM_VERSION'} = 3;
                $helm_version = $::JOB_PARAMS{'HELM_VERSION'};
            }
        } else {
            General::Logging::log_user_warning_message("Neither Job parameter 'HELM_VERSION', nor Network Config parameter 'helm_version' has been set\nDefaulting to helm version 3\n");
            $::JOB_PARAMS{'HELM_VERSION'} = 3;
            $helm_version = $::JOB_PARAMS{'HELM_VERSION'};
        }

        # Fetch executable name for the 'helm' command
        $helm_information = ADP::Kubernetes_Operations::get_helm_information($::JOB_PARAMS{'HELM_VERSION'});

        # Set Helm executable
        if ($helm_information) {
            $::JOB_PARAMS{'HELM_EXECUTABLE'} = ${$helm_information}{'helm_executable'} . $kubeconfig;
            $::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'} = ${$helm_information}{'helm_executable'} . $kubeconfig_tools;
            if (exists ${$helm_information}{'helm2_executable'} &&  ${$helm_information}{'helm2_executable'} ne "") {
                $::JOB_PARAMS{'HELM2_EXECUTABLE'} = ${$helm_information}{'helm2_executable'} . $kubeconfig;
                $::JOB_PARAMS{'HELM2_EXECUTABLE_TOOLS'} = ${$helm_information}{'helm2_executable'} . $kubeconfig_tools;
            }
            if (exists ${$helm_information}{'helm3_executable'} &&  ${$helm_information}{'helm3_executable'} ne "") {
                $::JOB_PARAMS{'HELM3_EXECUTABLE'} = ${$helm_information}{'helm3_executable'} . $kubeconfig;
                $::JOB_PARAMS{'HELM3_EXECUTABLE_TOOLS'} = ${$helm_information}{'helm3_executable'} . $kubeconfig_tools;
            }
            General::Logging::log_user_message(
                "HELM_VERSION=$helm_version\n" .
                "HELM_EXECUTABLE=$::JOB_PARAMS{'HELM_EXECUTABLE'}\n" .
                "HELM2_EXECUTABLE=$::JOB_PARAMS{'HELM2_EXECUTABLE'}\n" .
                "HELM3_EXECUTABLE=$::JOB_PARAMS{'HELM3_EXECUTABLE'}\n" .
                "HELM_EXECUTABLE_TOOLS=$::JOB_PARAMS{'HELM_EXECUTABLE_TOOLS'}\n" .
                "HELM2_EXECUTABLE_TOOLS=$::JOB_PARAMS{'HELM2_EXECUTABLE_TOOLS'}\n" .
                "HELM3_EXECUTABLE_TOOLS=$::JOB_PARAMS{'HELM3_EXECUTABLE_TOOLS'}\n"
            );
            return 0;
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get helm executable");
            return General::Playlist_Operations::RC_FALLBACK;
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
    sub Check_for_kubectl_P901S01T05 {

        my $kubeconfig = "";
        my $kubectl_information = undef;
        my $kubeconfig_tools = "";

        if (exists $::JOB_PARAMS{'KUBECONFIG'} && $::JOB_PARAMS{'KUBECONFIG'} ne "") {
            $kubeconfig = " --kubeconfig=$::JOB_PARAMS{'KUBECONFIG'}";
        }
        if (exists $::JOB_PARAMS{'KUBECONFIG_TOOLS'} && $::JOB_PARAMS{'KUBECONFIG_TOOLS'} ne "") {
            $kubeconfig_tools = " --kubeconfig=$::JOB_PARAMS{'KUBECONFIG_TOOLS'}";
        }

        # Initialize variables for kubectl command
        $::JOB_PARAMS{'KUBECTL_EXECUTABLE'} = "";

        if (exists $::JOB_PARAMS{'SKIP_KUBECTL_CHECK'} && $::JOB_PARAMS{'SKIP_KUBECTL_CHECK'} eq "yes") {
            # The Playlist does not use the kubectl command so no check should be done
            General::Logging::log_user_message("Check of 'kubectl' command not wanted because SKIP_KUBECTL_CHECK=yes");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Fetch executable name for the 'kubectl' command
        $kubectl_information = ADP::Kubernetes_Operations::get_kubectl_information();

        # Set kubectl executable
        if ($kubectl_information) {
            $::JOB_PARAMS{'KUBECTL_EXECUTABLE'} = ${$kubectl_information}{'kubectl_executable'} . $kubeconfig;
            $::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'} = ${$kubectl_information}{'kubectl_executable'} . $kubeconfig_tools;
            General::Logging::log_user_message("KUBECTL_EXECUTABLE=$::JOB_PARAMS{'KUBECTL_EXECUTABLE'}\nKUBECTL_EXECUTABLE_TOOLS=$::JOB_PARAMS{'KUBECTL_EXECUTABLE_TOOLS'}");
            return 0;
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get kubectl executable");
            return General::Playlist_Operations::RC_FALLBACK;
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
    sub Check_Server_Health_P901S01T06 {

        my $rc = 0;

        General::Logging::log_user_message("Checking Server Health\n");
        if ($::JOB_PARAMS{'JOBTYPE'} =~ /^(DEPLOY|DEPLOY_AND_CONFIGURE|MAINTAINABILITY_TEST_DISASTER_RECOVERY|UPGRADE)$/) {
            if (exists $::JOB_PARAMS{'USING_EVNFM'} && $::JOB_PARAMS{'USING_EVNFM'} eq "yes") {
                # EVNFM will handle onboarding container images so we don't need to check disk space for the container images
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "perl $::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/server_check.pl " .
                                                "--check-free-space 30000MB,$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'} " .
                                                "--check-command tar " .
                                                "--check-command unzip",
                        "hide-output"   => 1,
                    }
                );
            } else {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "perl $::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/server_check.pl " .
                                                "--check-free-space 30000MB,$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'} " .
                                                (exists $::JOB_PARAMS{'SKIP_DOCKER_AND_NERDCTL_CHECK'} && $::JOB_PARAMS{'SKIP_DOCKER_AND_NERDCTL_CHECK'} eq "yes" ? "" : "--check-free-space 30000MB,/var/lib/docker,sudo ") .
                                                "--check-command tar " .
                                                "--check-command unzip",
                        "hide-output"   => 1,
                    }
                );
            }
        } elsif ($::JOB_PARAMS{'JOBTYPE'} =~ /^(CREATE_TIMESHIFT_PACKAGE|TIMESHIFT_TRAFFIC_SETUP)$/) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "perl $::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/server_check.pl " .
                                            "--check-free-space 10MB,$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'} " .
                                            "--check-command expect",
                    "hide-output"   => 1,
                }
            );
        } else {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "perl $::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/server_check.pl " .
                                            "--check-free-space 10MB,$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'} " .
                                            "--check-command tar " .
                                            "--check-command unzip",
                    "hide-output"   => 1,
                }
            );
        }

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Server health check was successful';
        } else {
            push @::JOB_STATUS, '(x) Server health check was unsuccessful';
            # Mark that the Job has failed
            $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Server Health Check Failed\n");
            $rc = General::Playlist_Operations::RC_FALLBACK;
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
    sub Check_If_docker_or_nerdctl_Command_Require_sudo_P901S01T07 {

        my $rc;
        my @result;

        if (exists $::JOB_PARAMS{'SKIP_DOCKER_AND_NERDCTL_CHECK'} && $::JOB_PARAMS{'SKIP_DOCKER_AND_NERDCTL_CHECK'} eq "yes") {
            # The Playlist does not use the helm command so no check should be done
            General::Logging::log_user_message("Check of 'docker' or 'nerdctl' command not wanted because SKIP_DOCKER_AND_NERDCTL_CHECK=yes");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Check which command is needed 'docker', 'sudo docker', 'nerdctl' or 'sudo nerdctl'
        $::JOB_PARAMS{'CONTAINER_COMMAND'} = ADP::Kubernetes_Operations::get_docker_or_nerdctl_command(0);  # Show status and warning/error messages to the user

        if ($::JOB_PARAMS{'CONTAINER_COMMAND'} ne "") {
            return 0;
        } else {
            General::Logging::log_user_error_message("Command 'docker' or 'nerdctl' does not exist, require password or some other error");
            return General::Playlist_Operations::RC_FALLBACK;
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
sub Create_Configuration_File_P901S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Write_Configuration_File_P901S02T01 } );
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
    sub Write_Configuration_File_P901S02T01 {

        my $job_param_file = "$::JOB_PARAMS{'_JOB_PARAMS_FILE'}";
        my $rc = 0;

        # Add some default JOB_PARAMS values
        $::JOB_PARAMS{'DAFT_SCRIPT_PATH'}    = "$::JOB_PARAMS{'_PACKAGE_DIR'}";

        # Write %::JOB_PARAMS hash to the job_parameters.conf file
        General::Logging::log_user_message("Writing parameters to job_parameters.conf file\n");
        $rc = General::Playlist_Operations::job_parameters_write($job_param_file, \%::JOB_PARAMS);

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
sub Create_Job_Directories_P901S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Directories_P901S03T01 } );
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
    sub Create_Directories_P901S03T01 {

        my $rc;

        General::Logging::log_user_message("Creating job directories");

        $rc = General::OS_Operations::send_command(
            {
                "commands"       => [
                    "mkdir --mode=777 --parents $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software",
                    "mkdir --mode=777 --parents $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/tools",
                ],
                "ignore-errors" => 0,
                "hide-output"   => 1,
            }
        );

        # Add certain files or directories from being packed into the packed workspace file at end of the job execution.
        # The name is relative to the "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/" path.
        push @::exclude_at_packing_workspace_directory, "software";
        push @::exclude_at_packing_workspace_directory, "tools";

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
sub Collect_Miscellaneous_Information_P901S04 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_ECCD_Image_Version_P901S04T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_Disk_Usage_P901S04T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Print_ECCD_Stack_Type_P901S04T03 } );
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
    sub Print_ECCD_Image_Version_P901S04T01 {

        my $rc = 0;
        my @result = ();

        $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} = ADP::Kubernetes_Operations::get_eccd_version();

        if ($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} eq "") {
            # use alternative way of finding the version
            if (-f "/etc/eccd/eccd_image_version.ini") {
                General::Logging::log_user_message("Getting ECCD Version");
                $rc = General::OS_Operations::send_command(
                    {
                        "command"               => "cat /etc/eccd/eccd_image_version.ini",
                        "hide-output"           => 1,
                        "return-output"         => \@result,
                    }
                );
            } elsif (exists $::NETWORK_CONFIG_PARAMS{'cluster_ssh_vip'} && $::NETWORK_CONFIG_PARAMS{'cluster_ssh_vip'}{'value'} !~ /^(\s*|CHANGEME)$/) {
                General::Logging::log_user_message("Getting ECCD Version from cluster");
                $rc = General::OS_Operations::send_command_via_ssh(
                    {
                        "commands"              => [ "cat /etc/eccd/eccd_image_version.ini" ],
                        "hide-output"           => 1,
                        "return-output"         => \@result,
                        "command-in-output"     => 0,
                        "ip"                    => $::NETWORK_CONFIG_PARAMS{'cluster_ssh_vip'}{'value'},
                        "user"                  => (exists $::JOB_PARAMS{'NODE_USER_NAME'} ? $::JOB_PARAMS{'NODE_USER_NAME'} : "eccd"),
                        "use-standard-ssh"      => "yes",
                    }
                );
            } else {
                General::Logging::log_user_message("Node not running on ECCD");
                return General::Playlist_Operations::RC_TASKOUT;
            }

            if ($rc == 0) {
                my $line = join("\n",@result);
                if ($line =~ /^IMAGE_RELEASE=(\S+)$/ms) {
                    $::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} = $1;
                }
                General::Logging::log_user_message($line);
            } else {
                # Failed to get the ECCD version, maybe we are not using ECCD, just ignore this error
                $rc = 0;
            }
        }
        General::Logging::log_user_message("ECCD_IMAGE_RELEASE=$::JOB_PARAMS{'ECCD_IMAGE_RELEASE'}");

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
    sub Print_Disk_Usage_P901S04T02 {

        my $rc = 0;

        General::Logging::log_user_message("Printing Disk Utilization");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "df -B M",
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to fetch Disk Utilization, but error ignored");
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
    sub Print_ECCD_Stack_Type_P901S04T03 {

        unless (exists $::JOB_PARAMS{'ECCD_STACK_TYPE'}) {
            # The ECCD_STACK_TYPE job variable has not yet been set.
            if ($::JOB_PARAMS{'ECCD_IMAGE_RELEASE'} ne "") {
                if (ADP::Kubernetes_Operations::namespace_exists('.+capi') == 1) {
                    $::JOB_PARAMS{'ECCD_STACK_TYPE'} = "capo";
                } else {
                    $::JOB_PARAMS{'ECCD_STACK_TYPE'} = "non-capo";
                }
            } else {
                # Most likely not an ECCD cluster
                $::JOB_PARAMS{'ECCD_STACK_TYPE'} = "";
            }
        }

        General::Logging::log_user_message("ECCD_STACK_TYPE=$::JOB_PARAMS{'ECCD_STACK_TYPE'}");

        return 0;
    }
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P901S99 {

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
sub set_playlist_variables {
    %playlist_variables = (
        # ---------------------------------------------------------------------
        'SKIP_DOCKER_AND_NERDCTL_CHECK' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies if the 'docker' command check should be performed (=no) or not (=yes).
If a playlist does not use the 'docker' command then setting this parameter
to 'yes' then this check will be skipped.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_HELM_CHECK' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies if the 'helm' command check should be performed (=no) or not (=yes).
If a playlist does not use the 'helm' command then setting this parameter
to 'yes' then this check will be skipped.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SKIP_KUBECTL_CHECK' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies if the 'kubectl' command check should be performed (=no) or not (=yes).
If a playlist does not use the 'kubectl' command then setting this parameter
to 'yes' then this check will be skipped.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This playlist creates the job parameters file which contains information that
might be needed for the current DAFT job to be executed.
It is also used to initialize other job related tasks.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
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
