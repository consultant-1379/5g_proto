package Playlist::936_Check_Deployed_Software;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.17
#  Date     : 2024-06-03 17:38:39
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2022-2024
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

    $rc = General::Playlist_Operations::execute_step( \&Check_Deployed_Software_P936S01, \&Fallback001_P936S99 );
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
sub Check_Deployed_Software_P936S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Deployed_Application_Software_P936S01T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Deployed_CNF_P936S01T02 } );
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
    sub Check_Deployed_Application_Software_P936S01T01 {

        my $app_type = "";
        my $app_version = "";
        my $build = "";
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        my $helm_executable = $::JOB_PARAMS{'HELM_EXECUTABLE'};
        my $rc = 0;
        my $release_name = "";
        my @result;
        my $version = "";

        $::JOB_PARAMS{'APPLICATION_TYPE'} = "";
        $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} = "";
        $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} = "";
        $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_NAME'} = "";
        $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_VERSION'} = "";
        $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_BUILD'} = "";
        $::JOB_PARAMS{'OLD_SC_CS_RELEASE_NAME'} = "";
        $::JOB_PARAMS{'OLD_SC_CS_RELEASE_VERSION'} = "";
        $::JOB_PARAMS{'OLD_SC_CS_RELEASE_BUILD'} = "";
        $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_NAME'} = "";
        $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_VERSION'} = "";
        $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_BUILD'} = "";
        $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_NAME'} = "";
        $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_VERSION'} = "";
        $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_BUILD'} = "";
        $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_NAME'} = "";
        $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_VERSION'} = "";
        $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_BUILD'} = "";
        $::JOB_PARAMS{'OLD_CLOUD_NATIVE_BASE_RELEASE_NAME'} = "";
        $::JOB_PARAMS{'OLD_CLOUD_NATIVE_BASE_RELEASE_VERSION'} = "";
        $::JOB_PARAMS{'OLD_CLOUD_NATIVE_BASE_RELEASE_BUILD'} = "";
        $::JOB_PARAMS{'OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_NAME'} = "";
        $::JOB_PARAMS{'OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION'} = "";
        $::JOB_PARAMS{'OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_BUILD'} = "";

        General::Logging::log_user_message("List SC deployment");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$helm_executable list --all --namespace $sc_namespace",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            if (exists $::JOB_PARAMS{'IGNORE_FAILED_SOFTWARE_VERSION_CHECK'} && $::JOB_PARAMS{'IGNORE_FAILED_SOFTWARE_VERSION_CHECK'} eq "yes") {
                General::Logging::log_user_warning_message("Failed to get deployments in namespace $sc_namespace, but ignored because IGNORE_FAILED_SOFTWARE_VERSION_CHECK=yes");
                return General::Playlist_Operations::RC_STEPOUT;
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to get deployments");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        }

        if (@result) {
            # We have some data to parse
            # TODO: For now we hard code the user and password, ideally they should be e.g. $::JOB_PARAMS{'CMYP_USER'} and $::JOB_PARAMS{'CMYP_PASSWORD'}.
            #       But since the deploy and upgrade playlists does not currently have these variables we just hard code it which should work in most cases.
            my $semantic_version = ADP::Kubernetes_Operations::get_semantic_version($sc_namespace, "expert", "rootroot");
            if ($semantic_version =~ /^(\d+\.\d+\.\d+)(\S+)/) {
                $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} = $1;
                $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} = $2;
            } elsif ($semantic_version =~ /^(\d+\.\d+\.\d+)$/) {
                $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} = $1;
                $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} = "";
            } else {
                # We didn't find the version so for now we fake the version
                $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} = "1.15.99";
                $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} = "-CncsFakedVersionAndBuild";
            }
        }

        for (@result) {
            if ($_ =~ /^(\S+)\s+$sc_namespace\s+.+deployed\s+(\S+-)(\d+\.\d+\.\d+)(\S*)(.*)/) {
                $release_name = $1;
                $app_type = $2;
                $version = $3;
                $build = $4;
                $app_version = $5;
                if ($app_type =~ /^eric-sc-umbrella-\S*/) {
                    if ($release_name ne $sc_release_name) {
                        # Special EVNFM case where we said we want release name 'eric-sc' but they
                        # named it 'eric-sc-1'.
                        # So we need to update the SC_RELEASE_NAME job variable with the currently
                        # used value.
                        $sc_release_name = $release_name;
                        $::JOB_PARAMS{'SC_RELEASE_NAME'} = $release_name;
                    }
                    $::JOB_PARAMS{'APPLICATION_TYPE'} = "sc";
                    $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} = $version;
                    $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} = $build;
                } elsif ($app_type =~ /^(eric-dsc|eric-sc-diameter)-\S*/) {
                    if (version->parse($::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}) < version->parse("1.15.0")) {
                        # It's an older release 1.14 and below
                        if ($release_name ne $sc_release_name) {
                            # Special EVNFM case where we said we want release name 'eric-sc' but they
                            # named it 'eric-sc-1'.
                            # So we need to update the SC_RELEASE_NAME job variable with the currently
                            # used value.
                            $sc_release_name = $release_name;
                            $::JOB_PARAMS{'SC_RELEASE_NAME'} = $release_name;
                        }
                        $::JOB_PARAMS{'APPLICATION_TYPE'} = "dsc";
                        $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} = $version;
                        $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} = $build;
                        $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_VERSION'} = $version;
                        $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_BUILD'} = $build;
                        if (defined $app_version && defined $build && $build !~ /^[-+]\d+$/ && $app_version =~ /^\s+(\d+\.\d+\.\d+)([-+]\d+)\s*$/) {
                            # A special case where the CHART column contains a build that is not a number.
                            # For example: "-1-h9ffcf88" as in "eric-dsc-1.9.0-1-h9ffcf88" but the APP VERSION
                            # contains the correct build number "-1015" as in "1.9.0-1015".
                            my $temp_app_version = $1;
                            my $temp_app_build = $2;
                            if ($version eq $temp_app_version) {
                                # We use the build number from the APP VERSION instead of CHART
                                $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'} = $temp_app_build;
                                $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_BUILD'} = $temp_app_build;
                            }
                        }
                    } else {
                        # It's a new release 1.15 and newer
                        $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_NAME'} = $release_name;
                        $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_VERSION'} = $version;
                        $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_BUILD'} = $build;
                        if (defined $app_version && defined $build && $build !~ /^[-+]\d+$/ && $app_version =~ /^\s+(\d+\.\d+\.\d+)([-+]\d+)\s*$/) {
                            # A special case where the CHART column contains a build that is not a number.
                            # For example: "-1-h9ffcf88" as in "eric-sc-diameter-1.15.0-1-h9ffcf88" but the APP VERSION
                            # contains the correct build number "-1015" as in "1.9.0-1015".
                            my $temp_app_version = $1;
                            my $temp_app_build = $2;
                            if ($version eq $temp_app_version) {
                                # We use the build number from the APP VERSION instead of CHART
                                $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_BUILD'} = $temp_app_build;
                            }
                        }
                    }
                } elsif ($app_type =~ /^eric-sc-bsf-\S*/) {
                    # New CNCS from SC 1.15.0
                    $::JOB_PARAMS{'APPLICATION_TYPE'} = "sc";
                    $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_NAME'} = $release_name;
                    $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_VERSION'} = $version;
                    $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_BUILD'} = $build;
                    if (defined $app_version && defined $build && $build !~ /^[-+]\d+$/ && $app_version =~ /^\s+(\d+\.\d+\.\d+)([-+]\d+)\s*$/) {
                        # A special case where the CHART column contains a build that is not a number.
                        # For example: "-1-h9ffcf88" as in "eric-dsc-1.9.0-1-h9ffcf88" but the APP VERSION
                        # contains the correct build number "-1015" as in "1.9.0-1015".
                        my $temp_app_version = $1;
                        my $temp_app_build = $2;
                        if ($version eq $temp_app_version) {
                            # We use the build number from the APP VERSION instead of CHART
                            $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_BUILD'} = $temp_app_build;
                        }
                    }
                } elsif ($app_type =~ /^eric-sc-cs-\S*/) {
                    # New CNCS from SC 1.15.0
                    #$::JOB_PARAMS{'APPLICATION_TYPE'} = "sc";
                    $::JOB_PARAMS{'OLD_SC_CS_RELEASE_NAME'} = $release_name;
                    $::JOB_PARAMS{'OLD_SC_CS_RELEASE_VERSION'} = $version;
                    $::JOB_PARAMS{'OLD_SC_CS_RELEASE_BUILD'} = $build;
                    if (defined $app_version && defined $build && $build !~ /^[-+]\d+$/ && $app_version =~ /^\s+(\d+\.\d+\.\d+)([-+]\d+)\s*$/) {
                        # A special case where the CHART column contains a build that is not a number.
                        # For example: "-1-h9ffcf88" as in "eric-dsc-1.9.0-1-h9ffcf88" but the APP VERSION
                        # contains the correct build number "-1015" as in "1.9.0-1015".
                        my $temp_app_version = $1;
                        my $temp_app_build = $2;
                        if ($version eq $temp_app_version) {
                            # We use the build number from the APP VERSION instead of CHART
                            $::JOB_PARAMS{'OLD_SC_CS_RELEASE_BUILD'} = $temp_app_build;
                        }
                    }
                    if ($release_name ne $sc_release_name) {
                        # This is special handling to make all old calls where we e.g. fetch helm values
                        # etc. work.
                        # In the past we only had one release name to deal with but now with CNCS we have
                        # many and we basically force these calls to fetch the data from the sc-cs release name.
                        # This should be OK since all helm charts bsf, cs, scp and sepp are all deployed
                        # with the same eric-sc-values.yaml file so the values returned should be the same
                        # regardless of which chart we use and since sc-cs will always be installed this
                        # is a good choice to use as the "SC release name".
                        # So we need to update the SC_RELEASE_NAME job variable with the currently
                        # used value.
                        $sc_release_name = $release_name;
                        $::JOB_PARAMS{'SC_RELEASE_NAME'} = $release_name;
                    }
                } elsif ($app_type =~ /^eric-sc-scp-\S*/) {
                    # New CNCS from SC 1.15.0
                    $::JOB_PARAMS{'APPLICATION_TYPE'} = "sc";
                    $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_NAME'} = $release_name;
                    $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_VERSION'} = $version;
                    $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_BUILD'} = $build;
                    if (defined $app_version && defined $build && $build !~ /^[-+]\d+$/ && $app_version =~ /^\s+(\d+\.\d+\.\d+)([-+]\d+)\s*$/) {
                        # A special case where the CHART column contains a build that is not a number.
                        # For example: "-1-h9ffcf88" as in "eric-dsc-1.9.0-1-h9ffcf88" but the APP VERSION
                        # contains the correct build number "-1015" as in "1.9.0-1015".
                        my $temp_app_version = $1;
                        my $temp_app_build = $2;
                        if ($version eq $temp_app_version) {
                            # We use the build number from the APP VERSION instead of CHART
                            $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_BUILD'} = $temp_app_build;
                        }
                    }
                } elsif ($app_type =~ /^eric-sc-sepp-\S*/) {
                    # New CNCS from SC 1.15.0
                    $::JOB_PARAMS{'APPLICATION_TYPE'} = "sc";
                    $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_NAME'} = $release_name;
                    $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_VERSION'} = $version;
                    $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_BUILD'} = $build;
                    if (defined $app_version && defined $build && $build !~ /^[-+]\d+$/ && $app_version =~ /^\s+(\d+\.\d+\.\d+)([-+]\d+)\s*$/) {
                        # A special case where the CHART column contains a build that is not a number.
                        # For example: "-1-h9ffcf88" as in "eric-dsc-1.9.0-1-h9ffcf88" but the APP VERSION
                        # contains the correct build number "-1015" as in "1.9.0-1015".
                        my $temp_app_version = $1;
                        my $temp_app_build = $2;
                        if ($version eq $temp_app_version) {
                            # We use the build number from the APP VERSION instead of CHART
                            $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_BUILD'} = $temp_app_build;
                        }
                    }
                } elsif ($app_type =~ /^eric-cloud-native-base-/) {
                    $::JOB_PARAMS{'OLD_CLOUD_NATIVE_BASE_RELEASE_NAME'} = $release_name;
                    $::JOB_PARAMS{'OLD_CLOUD_NATIVE_BASE_RELEASE_VERSION'} = $version;
                    $::JOB_PARAMS{'OLD_CLOUD_NATIVE_BASE_RELEASE_BUILD'} = $build;
                } elsif ($app_type =~ /^eric-cloud-native-nf-additions-/) {
                    $::JOB_PARAMS{'OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_NAME'} = $release_name;
                    $::JOB_PARAMS{'OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION'} = $version;
                    $::JOB_PARAMS{'OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_BUILD'} = $build;
                }
            }
        }

        if ($::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} ne "" ||
            $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_VERSION'} ne "" ||
            $::JOB_PARAMS{'OLD_SC_CS_RELEASE_VERSION'} ne "" ||
            $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_VERSION'} ne "" ||
            $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_VERSION'} ne "" ||
            $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_VERSION'} ne "") {

            my $message = "";
            if ($::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} ne "") {
                # Older < 1.15.0 SC Releases where we only have 1 helm charts for the SC or cnDSC application
                $message = "SC deployment '$sc_release_name' found in namespace '$sc_namespace' with version:\n";
            } else {
                # Newer >= 1.15.0 SC Releases with CNCS where we have 2 up to 5 helm charts for the SC or cnDSC application
                $message = "SC deployments found in namespace '$sc_namespace' with versions:\n";
            }
            $message .= "  APPLICATION_TYPE:                              $::JOB_PARAMS{'APPLICATION_TYPE'}\n";
            $message .= "  SC_RELEASE_NAME:                               $::JOB_PARAMS{'SC_RELEASE_NAME'}\n";
            if ($::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} ne "") {
                $message .= "  OLD_SC_RELEASE_VERSION:                        $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}\n";
                $message .= "  OLD_SC_RELEASE_BUILD:                          $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'}\n\n";
            } else {
                $message .= "\n";
            }
            if ($::JOB_PARAMS{'OLD_SC_BSF_RELEASE_VERSION'} ne "") {
                $message .= "  OLD_SC_BSF_RELEASE_NAME:                       $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_NAME'}\n";
                $message .= "  OLD_SC_BSF_RELEASE_VERSION:                    $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_VERSION'}\n";
                $message .= "  OLD_SC_BSF_RELEASE_BUILD:                      $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_BUILD'}\n\n";
            }
            if ($::JOB_PARAMS{'OLD_SC_CS_RELEASE_VERSION'} ne "") {
                $message .= "  OLD_SC_CS_RELEASE_NAME:                        $::JOB_PARAMS{'OLD_SC_CS_RELEASE_NAME'}\n";
                $message .= "  OLD_SC_CS_RELEASE_VERSION:                     $::JOB_PARAMS{'OLD_SC_CS_RELEASE_VERSION'}\n";
                $message .= "  OLD_SC_CS_RELEASE_BUILD:                       $::JOB_PARAMS{'OLD_SC_CS_RELEASE_BUILD'}\n\n";
            }
            if ($::JOB_PARAMS{'OLD_SC_DSC_RELEASE_VERSION'} ne "") {
                $message .= "  OLD_SC_DSC_RELEASE_NAME:                       $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_NAME'}\n";
                $message .= "  OLD_SC_DSC_RELEASE_VERSION:                    $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_VERSION'}\n";
                $message .= "  OLD_SC_DSC_RELEASE_BUILD:                      $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_BUILD'}\n\n";
            }
            if ($::JOB_PARAMS{'OLD_SC_SCP_RELEASE_VERSION'} ne "") {
                $message .= "  OLD_SC_SCP_RELEASE_NAME:                       $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_NAME'}\n";
                $message .= "  OLD_SC_SCP_RELEASE_VERSION:                    $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_VERSION'}\n";
                $message .= "  OLD_SC_SCP_RELEASE_BUILD:                      $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_BUILD'}\n\n";
            }
            if ($::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_VERSION'} ne "") {
                $message .= "  OLD_SC_SEPP_RELEASE_NAME:                      $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_NAME'}\n";
                $message .= "  OLD_SC_SEPP_RELEASE_VERSION:                   $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_VERSION'}\n";
                $message .= "  OLD_SC_SEPP_RELEASE_BUILD:                     $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_BUILD'}\n\n";
            }
            if ($::JOB_PARAMS{'OLD_CLOUD_NATIVE_BASE_RELEASE_VERSION'} ne "") {
                $message .= "  OLD_CLOUD_NATIVE_BASE_RELEASE_NAME:            $::JOB_PARAMS{'OLD_CLOUD_NATIVE_BASE_RELEASE_NAME'}\n";
                $message .= "  OLD_CLOUD_NATIVE_BASE_RELEASE_VERSION:         $::JOB_PARAMS{'OLD_CLOUD_NATIVE_BASE_RELEASE_VERSION'}\n";
                $message .= "  OLD_CLOUD_NATIVE_BASE_RELEASE_BUILD:           $::JOB_PARAMS{'OLD_CLOUD_NATIVE_BASE_RELEASE_BUILD'}\n\n";
            }
            if ($::JOB_PARAMS{'OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION'} ne "") {
                $message .= "  OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_NAME:    $::JOB_PARAMS{'OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_NAME'}\n";
                $message .= "  OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION: $::JOB_PARAMS{'OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION'}\n";
                $message .= "  OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_BUILD:   $::JOB_PARAMS{'OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_BUILD'}\n\n";
            }
            General::Logging::log_user_message($message);
        } else {
            if (exists $::JOB_PARAMS{'IGNORE_FAILED_SOFTWARE_VERSION_CHECK'} && $::JOB_PARAMS{'IGNORE_FAILED_SOFTWARE_VERSION_CHECK'} eq "yes") {
                General::Logging::log_user_warning_message("No SC deployment found in namespace $sc_namespace, but ignored because IGNORE_FAILED_SOFTWARE_VERSION_CHECK=yes");
                return General::Playlist_Operations::RC_STEPOUT;
            } else {
                # display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("No SC deployment $sc_release_name found in namespace $sc_namespace, playlist execution cannot continue");
                return General::Playlist_Operations::RC_FALLBACK;
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
    sub Check_Deployed_CNF_P936S01T02 {
        my @deployed_cnf = ADP::Kubernetes_Operations::get_enabled_cnf( $::JOB_PARAMS{'SC_NAMESPACE'} );

        $::JOB_PARAMS{'DEPLOYED_CNF'} = "";
        for my $type (@deployed_cnf) {
            $::JOB_PARAMS{'DEPLOYED_CNF'} .= "$type,";
        }
        $::JOB_PARAMS{'DEPLOYED_CNF'} =~ s/,$//g;
        General::Logging::log_user_message("Deployed Features and Network Functions: $::JOB_PARAMS{'DEPLOYED_CNF'}");

        return 0;
    }
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P936S99 {

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

This playlist checks what the current deployed software version is, based on the
SC_RELEASE_NAME and SC_NAMESPACE job variables and it updates the following job
variables:

    - APPLICATION_TYPE
      This is the type of application e.g. SC or cnDSC.

    - DEPLOYED_CNF
      This is the currently deployed and enabled features and network functions.

    - OLD_SC_RELEASE_VERSION
      This is the SC or cnDSC software release version e.g. 1.10.1

    - OLD_SC_RELEASE_BUILD
      This is the SC or cnDSC software build number e.g. +1099

    - OLD_CLOUD_NATIVE_BASE_RELEASE_NAME
      This is the Cloud Native Base software release name.

    - OLD_CLOUD_NATIVE_BASE_RELEASE_VERSION
      This is the Cloud Native Base software release version e.g. 65.1.0

    - OLD_CLOUD_NATIVE_BASE_RELEASE_BUILD
      This is the Cloud Native Base software build number, this is normally empty.

    - OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_NAME
      This is the Cloud Native NF Additions software release name.

    - OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION
      This is the Cloud Native NF Additions software release version e.g. 25.0.0

    - OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_BUILD
      This is the Cloud Native NF Additions software build number, this is normally empty.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
