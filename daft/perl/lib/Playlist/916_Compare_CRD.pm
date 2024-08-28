package Playlist::916_Compare_CRD;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.10
#  Date     : 2022-11-07 16:04:20
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2022
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

    $rc = General::Playlist_Operations::execute_step( \&Compare_CRD_P916S01, undef );
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
sub Compare_CRD_P916S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Compare_CRD_List_Before_and_After_Deploy_Or_Upgrade_P916S01T01 } );
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
    sub Compare_CRD_List_Before_and_After_Deploy_Or_Upgrade_P916S01T01 {

        my $app_type = exists $::JOB_PARAMS{'APPLICATION_TYPE'} ? $::JOB_PARAMS{'APPLICATION_TYPE'} : "sc";
        my @crds_after;
        my %crds_after;
        my @crds_before;
        my %crds_before;
        my @crds_changed;
        my @crds_deleted;
        my @crds_expected = ADP::Kubernetes_Operations::get_list_of_known_sc_crds($app_type, $::JOB_PARAMS{'SC_RELEASE_VERSION'});
        my %crds_expected;
        my @crds_missing;
        my @crds_new;
        my $error_cnt = 0;
        my %found_crds;
        my $message = "";
        my $not_found = "";
        my $rc = 0;

        #
        # Reading CRDS after deploy/upgrade
        #
        General::Logging::log_user_message("Fetching Installed Custom Resource Definitions");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get crds",
                "hide-output"   => 1,
                "return-output" => \@crds_after,
                "save-to-file"  => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/crds_list_after.txt",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to fetch Installed Custom Resource Definitions");
            return 1;
        }
        for (@crds_after) {
            if (/^(\S+)\s+(\d{4}-\S+)/) {
                # certificateauthorities.com.ericsson.sec.tls    2020-06-17T08:01:54Z
                $crds_after{$1} = $2;
            }
        }

        #
        # Reading CRDS before deploy/upgrade
        #
        General::Logging::log_user_message("Fetching Previously Installed Custom Resource Definitions");
        $rc = General::File_Operations::read_file(
            {
                "filename"              => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/crds_list_before.txt",
                "output-ref"            => \@crds_before,
                "hide-error-messages"   => 1,
                "ignore-empty-lines"    => 1,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to read workspace crds_list_before.txt file");
            return 1;
        }
        for (@crds_before) {
            if (/^(\S+)\s+(\d{4}-\S+)/) {
                # certificateauthorities.com.ericsson.sec.tls    2020-06-17T08:01:54Z
                $crds_before{$1} = $2;
            }
        }

        #
        # Look for new and changed CRDS
        #
        for my $crd (sort keys %crds_after) {
            if (exists $crds_before{$crd}) {
                if ($crds_before{$crd} ne $crds_after{$crd}) {
                    push @crds_changed, $crd;
                }
            } else {
                push @crds_new, $crd;
            }
        }

        #
        # Look for deleted CRDS
        #
        for my $crd (sort keys %crds_before) {
            unless (exists $crds_after{$crd}) {
                push @crds_deleted, $crd;
            }
        }

        #
        # Reading CRDS that we expect to find
        #
        for my $crd (sort @crds_expected) {
            $crds_expected{$crd} = 1;
            unless (exists $crds_after{$crd}) {
                push @crds_missing, $crd;
            }
        }

        if (@crds_changed) {
            $message .= "The following CRDS has been updated:\n";
            for my $crd (@crds_changed) {
                $message .= sprintf "  %-50s  Old Date=%s\n  %-50s  New Date=%s", $crd, $crds_before{$crd}, " ", $crds_after{$crd};
                unless (exists $crds_expected{$crd}) {
                    $error_cnt++;
                    $message .= sprintf "\n  %-50s  %s", " ", '(missing in @known_sc_crds array)';
                }
                $message .= "\n";
            }
            $message .= "\n";
        }

        if (@crds_deleted) {
            $message .= "The following CRDS has been deleted:\n";
            for my $crd (@crds_deleted) {
                $message .= sprintf "  %-50s  ", $crd;
                if (exists $crds_expected{$crd}) {
                    $error_cnt++;
                    $message .= sprintf "%s", '(included in @known_sc_crds array when it should not be)';
                }
                $message =~ s/\s+$//;
                $message .= "\n";
            }
            $message .= "\n";
        }

        if (@crds_new) {
            $message .= "The following CRDS has been added:\n";
            for my $crd (@crds_new) {
                $message .= sprintf "  %-50s  ", $crd;
                unless (exists $crds_expected{$crd}) {
                    $error_cnt++;
                    $message .= sprintf "%s", '(missing in @known_sc_crds array but added by CRD package)';
                }
                $message =~ s/\s+$//;
                $message .= "\n";
            }
            $message .= "\n";
        }

        if (@crds_missing) {
            $message .= "The following expected CRDS are missing:\n";
            for my $crd (@crds_missing) {
                $message .= sprintf "  %-50s  %s\n", $crd, '(included in @known_sc_crds array but missing on the node)';
                $error_cnt++;
            }
            $message .= "\n";
        }

        if ($error_cnt == 0) {
            $message .= "All expected Custom Resource Definitions (CRDs) were found.\n";
            General::Logging::log_user_message($message);
            return 0;
        } else {
            $message .= "All expected Custom Resource Definitions (CRDs) were not found, please see details above.\nIf needed, update the \@known_sc_crds array in package ADP::Kubernetes_Operations.\n";
            if ($::JOB_PARAMS{'IGNORE_CRD_CHECK'} eq "yes") {
                General::Logging::log_user_warning_message($message);
                return 0;
            } else {
                General::Logging::log_user_error_message($message);
                return 1;
            }
        }
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

This Playlist is used for comparing Custom Resource Definitions (CRD) before
and after doing a Deploy or Upgrade of CRDs.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
