package Playlist::924_Prepare_Deploy_Upgrade;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.5
#  Date     : 2023-09-20 13:53:24
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021-2022
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
use Playlist::919_Check_Registry_Information;

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

    $rc = General::Playlist_Operations::execute_step( \&Create_Namespaces_P924S01, \&Fallback001_P924S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Registry_P924S02, \&Fallback001_P924S99 );
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
sub Create_Namespaces_P924S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_And_Create_Deployment_Namespace_P924S01T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_And_Create_CRD_Namespace_P924S01T02 } );
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
    sub Check_And_Create_Deployment_Namespace_P924S01T01 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Check if deployment namespace $sc_namespace exist");
        $rc = ADP::Kubernetes_Operations::namespace_exists($sc_namespace);
        if ($rc == 1) {
            # Namespace exist
            General::Logging::log_user_message("Namespace $sc_namespace already exist");
            push @::JOB_STATUS, "(-) Namespace $sc_namespace already exist";
            $rc = 0;
        } elsif ($rc == 0) {
            # Namespace does not exist
            General::Logging::log_user_message("Creating deployment namespace $sc_namespace");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} create namespace $sc_namespace",
                    "hide-output"   => 1,
                }
            );
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) Create namespace $sc_namespace successful";
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to create namespace $sc_namespace");
                push @::JOB_STATUS, "(x) Create namespace $sc_namespace failed";
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get the defined namespaces");
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
    sub Check_And_Create_CRD_Namespace_P924S01T02 {

        my $crd_namespace = $::JOB_PARAMS{'CRD_NAMESPACE'};
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Check if CRD namespace $crd_namespace exist");
        $rc = ADP::Kubernetes_Operations::namespace_exists($crd_namespace);
        if ($rc == 1) {
            # Namespace exist
            General::Logging::log_user_message("Namespace already exist");
            push @::JOB_STATUS, "(-) Namespace $crd_namespace already exist";
            $rc = 0;
        } elsif ($rc == 0) {
            # Namespace does not exist
            General::Logging::log_user_message("Creating CRD namespace $crd_namespace");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} create namespace $crd_namespace",
                    "hide-output"   => 1,
                }
            );
            if ($rc == 0) {
                push @::JOB_STATUS, "(/) Create namespace $crd_namespace successful";
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to create namespace $crd_namespace");
                push @::JOB_STATUS, "(x) Create namespace $crd_namespace failed";
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to get the defined namespaces");
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
sub Check_Registry_P924S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::919_Check_Registry_Information::main } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'JOBTYPE'} eq "DEPLOY") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_And_Create_Deployment_Registry_Secret_P924S02T01 } );
        return $rc if $rc < 0;

        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_And_Create_CRD_Registry_Secret_P924S02T02 } );
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
    sub Check_And_Create_Deployment_Registry_Secret_P924S02T01 {

        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc = 0;
        my $registry_password = $::JOB_PARAMS{'SC_REGISTRY_PASSWORD'};
        my $registry_secret_name = $::JOB_PARAMS{'SC_REGISTRY_SECRET_NAME'};
        my $registry_url = $::JOB_PARAMS{'SC_REGISTRY_URL'};
        my $registry_user = $::JOB_PARAMS{'SC_REGISTRY_USER'};
        my @result;

        General::Logging::log_user_message("Check if SC secret $registry_secret_name in namespace $namespace exist");
        @result = ADP::Kubernetes_Operations::get_resource_names(
            {
                "resource"      => "secret",
                "namespace"     => $namespace,
                "include-list"  => [ "^$registry_secret_name\$" ],
                "hide-output"   => 1,
            }
        );
        if (scalar @result == 1) {
            General::Logging::log_user_message("The SC secret already exists, no need to create it");
            push @::JOB_STATUS, "(-) Create SC secret $registry_secret_name not needed";
            return 0;
        }

        General::Logging::log_user_message("Creating SC secret $registry_secret_name in namespace $namespace");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $namespace create secret docker-registry $registry_secret_name --docker-server $registry_url --docker-username $registry_user --docker-password '$registry_password'",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create SC secret $registry_secret_name successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to create SC registry secret");
            push @::JOB_STATUS, "(x) Create SC secret $registry_secret_name failed";
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
    sub Check_And_Create_CRD_Registry_Secret_P924S02T02 {

        my $namespace = $::JOB_PARAMS{'CRD_NAMESPACE'};
        my $rc = 0;
        my $registry_password = $::JOB_PARAMS{'CRD_REGISTRY_PASSWORD'};
        my $registry_secret_name = $::JOB_PARAMS{'CRD_REGISTRY_SECRET_NAME'};
        my $registry_url = $::JOB_PARAMS{'CRD_REGISTRY_URL'};
        my $registry_user = $::JOB_PARAMS{'CRD_REGISTRY_USER'};
        my @result;

        General::Logging::log_user_message("Check if CRD secret $registry_secret_name in namespace $namespace exist");
        @result = ADP::Kubernetes_Operations::get_resource_names(
            {
                "resource"      => "secret",
                "namespace"     => $namespace,
                "include-list"  => [ "^$registry_secret_name\$" ],
                "hide-output"   => 1,
            }
        );
        if (scalar @result == 1) {
            General::Logging::log_user_message("The CRD secret already exists, no need to create it");
            push @::JOB_STATUS, "(-) Create CRD secret $registry_secret_name not needed";
            return 0;
        }

        General::Logging::log_user_message("Creating CRD secret $registry_secret_name in namespace $namespace");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} --namespace $namespace create secret docker-registry $registry_secret_name --docker-server $registry_url --docker-username $registry_user --docker-password '$registry_password'",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Create CRD secret $registry_secret_name successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to create CRD registry secret");
            push @::JOB_STATUS, "(x) Create CRD secret $registry_secret_name failed";
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
sub Fallback001_P924S99 {

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

This playlist performs preparation tasks before a deploy or upgrade of the
software on a SC node.
For example creating of namespaces if needed and to check Registry information.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
