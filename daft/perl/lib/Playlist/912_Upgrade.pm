package Playlist::912_Upgrade;

################################################################################
#
#  Author   : everhel, eustone
#
#  Revision : 1.39
#  Date     : 2024-05-07 11:28:23
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
use File::Basename qw(dirname basename);
use General::File_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;
use General::Server_Operations;
use General::Yaml_Operations;
use Playlist::916_Compare_CRD;
use Playlist::924_Prepare_Deploy_Upgrade;
use Playlist::926_Handle_Docker_Images;
use Playlist::927_Deploy_Upgrade_CRDS;
use Playlist::928_Verify_Deployment;
use Playlist::935_Deploy_Upgrade_Application;

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

    my $rc;

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    } else {
        $debug_command = "";
    }

    #
    # Prepare SC Upgrade
    #

    $rc = General::Playlist_Operations::execute_step( \&Prepare_SC_Upgrade_P912S01, \&Fallback001_P912S99 );
    return $rc if $rc < 0;

    #
    # Prepare the Artifacts from Software GW
    #

    $rc = General::Playlist_Operations::execute_step( \&Load_And_Tag_Docker_Images_P912S02, \&Fallback001_P912S99 );
    return $rc if $rc < 0;

    #
    # Deploy the CRD
    #

    if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_CRD'} eq "no") {

        $rc = General::Playlist_Operations::execute_step( \&Upgrade_Custom_Resource_Definitions_P912S03, \&Fallback001_P912S99 );
        return $rc if $rc < 0;

    }

    #
    # Upgrade the SC
    #

    if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_SC'} eq "no") {

        $rc = General::Playlist_Operations::execute_step( \&Upgrade_SC_P912S04, \&Fallback001_P912S99 );
        return $rc if $rc < 0;

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
sub Prepare_SC_Upgrade_P912S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::924_Prepare_Deploy_Upgrade::main } );
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
sub Load_And_Tag_Docker_Images_P912S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::926_Handle_Docker_Images::main } );
    return $rc if $rc < 0;
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
sub Upgrade_Custom_Resource_Definitions_P912S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::927_Deploy_Upgrade_CRDS::main } );
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
sub Upgrade_SC_P912S04 {

    my $rc;

    if ($::JOB_PARAMS{'USING_EVNFM'} eq "yes") {
        # Special handling for EVNFM because it will handle upgrading the SC.
        General::Logging::log_user_message("EVNFM will handle upgrading the SC Application, skip tasks in this step");
        push @::JOB_STATUS, "(-) Upgrading Software skipped, EVNFM handles this";
        return General::Playlist_Operations::RC_STEPOUT;
    }

    my $use_legacy_deploy_upgrade = 0;  # Change it to 1 if the old way to deploy or upgrade the SC should be done. Will not work from SC 1.15 when we use CNCS.
    if ($use_legacy_deploy_upgrade == 0) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::935_Deploy_Upgrade_Application::main } );
        return $rc if $rc < 0;
    } else {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Upgrade_SC_Application_P912S04T01 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'CHECK_DEPLOYMENT_STATUS_AFTER_EACH_HELM_OPERATION'} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::928_Verify_Deployment::main } );
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
    sub Upgrade_SC_Application_P912S04T01 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        my $helm_executable =  $::JOB_PARAMS{'HELM_EXECUTABLE'};
        my $message = "";
        my $rc = 0;
        my $sc_umbrella_file = $::JOB_PARAMS{'SC_UMBRELLA_FILE'};
        my $sc_values_file_extra = "";
        my $sc_values_file_path = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}";
        my $timeout = $::JOB_PARAMS{'HELM_TIMEOUT_APPLICATION'};

        # Check if we have any extra eric-sc-values files to include
        for my $job_parameter (General::String_Operations::sort_hash_keys_alphanumerically(\%::JOB_PARAMS)) {
            if ($job_parameter =~ /^CONFIG_FILE_HELM_CHART_(\d+)$/) {
                $sc_values_file_extra .= " -f $sc_values_file_path/eric-sc-values-$1.yaml";
            }
        }

        General::Message_Generation::print_message_box(
            {
                "header-text"   => "Upgrading SC Application",
                "messages"      => [
                    "",
                    "Old software before upgrade:",
                    "Version: $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}",
                    "Build:   $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'}",
                    "",
                    "New software after upgrade:",
                    "Version: $::JOB_PARAMS{'SC_RELEASE_VERSION'}",
                    "Build:   $::JOB_PARAMS{'SC_RELEASE_BUILD'}",
                    "",
                    "This will take a while to complete.",
                    "",
                ],
                "align-text"    => "left",
                "return-output" => \$message,
            }
        );
        General::Logging::log_user_message($message);

        if ($::JOB_PARAMS{'HELM_VERSION'} == 2) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}${helm_executable} --namespace $sc_namespace upgrade --name $sc_release_name -f $sc_values_file_path/eric-sc-values.yaml $sc_values_file_extra $sc_umbrella_file --timeout $timeout --debug",
                    "hide-output"   => 1,
                }
            );
        } elsif ($::JOB_PARAMS{'HELM_VERSION'} == 3) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}${helm_executable} --namespace $sc_namespace upgrade $sc_release_name -f $sc_values_file_path/eric-sc-values.yaml $sc_values_file_extra $sc_umbrella_file --timeout ${timeout}s --debug",
                    "hide-output"   => 1,
                }
            );
        } else {
            General::Logging::log_user_error_message("No helm version defined or left blank");
            $rc = 1;
        }

        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Upgrading Software successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to upgrade SC Application");
            push @::JOB_STATUS, "(x) Upgrading Software failed";
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
sub Fallback001_P912S99 {

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

This playlist performs an upgrade of the software on a SC node.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
