package Playlist::910_Deploy;

################################################################################
#
#  Author   : eustone, everhel
#
#  Revision : 1.81
#  Date     : 2024-05-15 10:09:34
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
use General::String_Operations;
use General::Yaml_Operations;
use Playlist::916_Compare_CRD;
use Playlist::919_Check_Registry_Information;
use Playlist::924_Prepare_Deploy_Upgrade;
use Playlist::926_Handle_Docker_Images;
use Playlist::927_Deploy_Upgrade_CRDS;
use Playlist::928_Verify_Deployment;
use Playlist::929_Handle_Secrets;
use Playlist::935_Deploy_Upgrade_Application;
use version;

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

    #
    # Prepare SC Deployment
    #

    $rc = General::Playlist_Operations::execute_step( \&Prepare_SC_Deployment_P910S01, \&Fallback001_P910S99 );
    return $rc if $rc < 0;

    #
    # Prepare the Artifacts from Software GW
    #

    $rc = General::Playlist_Operations::execute_step( \&Load_And_Tag_Docker_Images_P910S02, \&Fallback001_P910S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Install_Secrets_P910S03, \&Fallback001_P910S99 );
    return $rc if $rc < 0;

    #
    # Deploy the CRD
    #

    if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_CRD'} eq "no") {

        $rc = General::Playlist_Operations::execute_step( \&Deploy_Custom_Resource_Definitions_P910S04, \&Fallback001_P910S99 );
        return $rc if $rc < 0;

    }

    #
    # Deploy the SC
    #

    if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_SC'} eq "no") {

        $rc = General::Playlist_Operations::execute_step( \&Deploy_SC_P910S05, \&Fallback001_P910S99 );
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
sub Prepare_SC_Deployment_P910S01 {

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
sub Load_And_Tag_Docker_Images_P910S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::926_Handle_Docker_Images::main } );
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
sub Install_Secrets_P910S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::929_Handle_Secrets::main } );
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
sub Deploy_Custom_Resource_Definitions_P910S04 {

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
sub Deploy_SC_P910S05 {

    my $rc;

    if ($::JOB_PARAMS{'USING_EVNFM'} eq "yes") {
        # Special handling for EVNFM because it will handle deploying the SC.
        General::Logging::log_user_message("EVNFM will handle deploying the SC Application, skip tasks in this step");
        push @::JOB_STATUS, "(-) Installing Software skipped, EVNFM handles this";
        return General::Playlist_Operations::RC_STEPOUT;
    }

    my $use_legacy_deploy_upgrade = 0;  # Change it to 1 if the old way to deploy or upgrade the SC should be done. Will not work from SC 1.15 when we use CNCS.
    if ($use_legacy_deploy_upgrade == 0) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::935_Deploy_Upgrade_Application::main } );
        return $rc if $rc < 0;
    } else {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Deploy_SC_Application_P910S05T01 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'CHECK_DEPLOYMENT_STATUS_AFTER_EACH_HELM_OPERATION'} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::928_Verify_Deployment::main } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'TIMESHIFT_VALID_LIFETIME_SECONDS'} >= 315576000) {
        # Maximum allowed validity, now patch and remove any overrideTtl values to make sure most certificates
        # have the maximum allowed validity.
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Patch_And_Remove_OverrideTtl_If_Needed_P910S05T02 } );
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
    sub Deploy_SC_Application_P910S05T01 {

        my $extra_parameters = "";
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

        # Special parameters for time shift and time bomb testing to extend the
        # valid lifetime of certificates to a higher value.
        if ($::JOB_PARAMS{'TIMESHIFT_VALID_LIFETIME_SECONDS'} > 0) {
            if (version->parse($::JOB_PARAMS{'SC_RELEASE_VERSION'}) < version->parse("1.11.0")) {
                # Starting with SC release 1.11.0 these parameters are no longer used
                if ($::JOB_PARAMS{'TIMESHIFT_VALID_LIFETIME_SECONDS'} > 604800) {
                    # Max 7 days
                    $extra_parameters .= " --set eric-sec-sip-tls.serverCertificate.validLifetimeSeconds=604800";
                    $extra_parameters .= " --set eric-sec-sip-tls.clientCertificate.validLifetimeSeconds=604800";
                } else {
                    $extra_parameters .= " --set eric-sec-sip-tls.serverCertificate.validLifetimeSeconds=$::JOB_PARAMS{'TIMESHIFT_VALID_LIFETIME_SECONDS'}";
                    $extra_parameters .= " --set eric-sec-sip-tls.clientCertificate.validLifetimeSeconds=$::JOB_PARAMS{'TIMESHIFT_VALID_LIFETIME_SECONDS'}";
                }
            }
            if ($::JOB_PARAMS{'TIMESHIFT_VALID_LIFETIME_SECONDS'} > 315576000) {
                # Max 10 years
                $extra_parameters .= " --set eric-sec-sip-tls.internalCertificate.validLifetimeSeconds=315576000";
            } else {
                $extra_parameters .= " --set eric-sec-sip-tls.internalCertificate.validLifetimeSeconds=$::JOB_PARAMS{'TIMESHIFT_VALID_LIFETIME_SECONDS'}";
            }
        }

        General::Message_Generation::print_message_box(
            {
                "header-text"   => "Deploying SC Application",
                "messages"      => [
                    "",
                    "Software to be installed:",
                    "Version: $::JOB_PARAMS{'SC_RELEASE_VERSION'}",
                    "Build:   $::JOB_PARAMS{'SC_RELEASE_BUILD'}",
                    "",
                    "Network Functions and Features to Deploy (nodetype):",
                    "$::JOB_PARAMS{'nodetype'}",
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
                    "command"       => "${debug_command}${helm_executable} --namespace $sc_namespace install --name $sc_release_name -f $sc_values_file_path/eric-sc-values.yaml $sc_values_file_extra $sc_umbrella_file --timeout $timeout --debug $extra_parameters",
                    "hide-output"   => 1,
                }
            );
        } elsif ($::JOB_PARAMS{'HELM_VERSION'} == 3) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}${helm_executable} --namespace $sc_namespace install $sc_release_name -f $sc_values_file_path/eric-sc-values.yaml $sc_values_file_extra $sc_umbrella_file --timeout ${timeout}s --debug $extra_parameters",
                    "hide-output"   => 1,
                }
            );
        } else {
            General::Logging::log_user_error_message("No helm version defined or left blank");
            $rc = 1;
        }

        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Installing Software successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to deploy SC Application");
            push @::JOB_STATUS, "(x) Installing Software failed";
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
    sub Patch_And_Remove_OverrideTtl_If_Needed_P910S05T02 {

        my $command;
        my $found = 0;
        my $certificate;
        my %patch_certificate;
        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Check if any internal certificates needs to be patched (overrideTtl < 315576000).\n");
        General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get internalcertificates -n $::JOB_PARAMS{'SC_NAMESPACE'} -o json | grep -iP '\"(name|overrideTtl)\":'",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        for (@result) {
            if (/^\s*"name":\s*"(\S+)"/i) {
                $certificate = $1;
            } elsif (/^\s*"overrideTtl":\s*(\d+)/i) {
                if ($1 < 315576000) {
                    # The value is less than about 10 years
                    $patch_certificate{$certificate} = $1;
                }
            }
        }

        if (%patch_certificate) {
            for $certificate (sort keys %patch_certificate) {
                General::Logging::log_user_message("Patching internal certificate '$certificate' by removing 'overrideTtl: $patch_certificate{$certificate}'.\n");
                $command = "${debug_command}$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} patch internalcertificates $certificate -n $::JOB_PARAMS{'SC_NAMESPACE'} -p '{ \"spec\": {\"certificate\": {\"validity\": null}}}' --type merge";
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => $command,
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc == 0) {
                    # Check if any certificates were patched
                    for (@result) {
                        if (/^\S+\s+patched\s*$/) {
                            #internalcertificate.siptls.sec.ericsson.com/eric-bsf-manager-server-certificate patched
                            #  or: that will not be matched
                            #internalcertificate.siptls.sec.ericsson.com/eric-bsf-manager-server-certificate patched (no change)
                            $found++;
                        }
                    }
                } else {
                    # Display the result in case of error
                    General::OS_Operations::write_last_temporary_output_to_progress();

                    General::Logging::log_user_error_message("Failed to execute the command:\n$command");
                    return $rc;
                }
            }

            if ($found > 0) {
                General::Logging::log_user_message("$found certificates patched, now waiting for the certificates to get renewed.\nCurrently this is a hard coded wait that can be interrupted with CTRL-C if you know all certificates has been renewed or don't care to wait.\n");
                $rc = General::OS_Operations::sleep_with_progress(
                    {
                        "allow-interrupt"   => 1,
                        "progress-message"  => 1,
                        "seconds"           => 600,
                        "use-logging"       => 1,
                    }
                );
                if ($rc == 1) {
                    # User pressed CTRL-C to interrupt the wait
                    General::Logging::log_user_warning_message("CTRL-C pressed to interrupt the wait");
                    $rc = 0;
                }
            } else {
                General::Logging::log_user_warning_message("No internal certificates patched.");
            }
        } else {
            General::Logging::log_user_message("No internal certificates need patching.\n");
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
sub Fallback001_P910S99 {

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

This playlist performs a deploy of the software on a SC node.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
