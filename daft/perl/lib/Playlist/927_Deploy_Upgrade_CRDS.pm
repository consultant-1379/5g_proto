package Playlist::927_Deploy_Upgrade_CRDS;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.18
#  Date     : 2024-05-15 10:09:34
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2021-2024
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
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;
use Playlist::916_Compare_CRD;
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

    $rc = General::Playlist_Operations::execute_step( \&Deploy_Custom_Resource_Definitions_P927S01, \&Fallback001_P927S99 );
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
sub Deploy_Custom_Resource_Definitions_P927S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Fetch_Installed_Custom_Resource_Definitions_P927S01T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_Upgrade_Custom_Resource_Definitions_P927S01T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::916_Compare_CRD::main } );
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
    sub Fetch_Installed_Custom_Resource_Definitions_P927S01T01 {
        my $rc = 0;

        if ($::JOB_PARAMS{'USING_EVNFM'} eq "yes" && $::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'} eq "yes") {
            # Special handling for EVNFM depending on the software release because of the way the CRDS are
            # delivered. When CRDS are delivered in software package and EVNFM is invoking DAFT then we
            # skip all tasks in this step because onboarding and installing CRDS will be handled by EVNFM.
            # But if CRDS are delivered in the tools package then DAFT need to continue because EVNFM cannot
            # handle installing CRDS from the tools package.
            if (version->parse($::JOB_PARAMS{'SC_RELEASE_VERSION'}) >= version->parse("1.13.0")) {
                # Another special case for EVNFM, for SC releases below 1.13.0 (i.e. 1.3.1 up to 1.12.0)
                # we still need to handle installation or upgrade of the CRDS because EVNFM cannot handle it.
                # So only starting with SC 1.13.x can EVNFM handle the CRDS by itself and we can skip this step.
                General::Logging::log_user_message("EVNFM will handle onboarding of all images, skip remaining tasks in this step");
                push @::JOB_STATUS, "(-) Installing/Upgrading CRDs skipped, EVNFM handles this";
                return General::Playlist_Operations::RC_STEPOUT;
            }
        }

        General::Logging::log_user_message("Fetching Installed Custom Resource Definitions");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} get crds",
                "hide-output"   => 1,
                "save-to-file"  => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/crds_list_before.txt",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to fetch Installed Custom Resource Definitions");
            return General::Playlist_Operations::RC_TASKOUT;
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
    sub Install_Upgrade_Custom_Resource_Definitions_P927S01T02 {
        my $app_type = exists $::JOB_PARAMS{'APPLICATION_TYPE'} ? $::JOB_PARAMS{'APPLICATION_TYPE'} : "sc";
        my %crd_file_hash;
        my %crd_installed_hash;
        my $crd_namespace = "";
        my $crd_release_name = "";
        my $helm_executable =  $::JOB_PARAMS{'HELM_EXECUTABLE'};
        my %known_crd_helm_charts;
        my $known_crd_helm_charts_regex = "";
        my $name_without_version;
        # The following variable will be set by the playlist extention script:
        #   5g_proto/daft/playlist_extensions/upgrade/pre/change_crd_annotation_localhost.apply
        # if the old versions CRD's helm chart was removed to prepare for the upgrade to SC 1.6 release or newer
        # because in such case we need to install the new CRD's into the same namespace.
        # It will also be set if a newer version of the CRD's already exist, in which case we also need to use
        # the same namespace.
        my $old_crd_namespace = exists $::JOB_PARAMS{'OLD_CRD_NAMESPACE'} ? $::JOB_PARAMS{'OLD_CRD_NAMESPACE'} : "";
        my $rc = 0;
        my $registry_secret_name = $::JOB_PARAMS{'CRD_REGISTRY_SECRET_NAME'};
        my $registry_url = $::JOB_PARAMS{'CRD_REGISTRY_URL'};
        my @result;
        my $sc_crds_file;
        my @sc_crds_files = split /\|/, $::JOB_PARAMS{'CRDS_FILE'};
        my $timeout = $::JOB_PARAMS{'HELM_TIMEOUT_CRD'};

        if ($::JOB_PARAMS{'CRDS_FILE'} eq "") {
            # This should not happen.
            General::Logging::log_user_warning_message("No CRD file found which should not happen, skip the upgrade");
            return General::Playlist_Operations::RC_STEPOUT;
        }

        if ($::JOB_PARAMS{'HELM_VERSION'} > 2) {
            # helm version 3 and above require the timeout value to include a unit, e.g. '10s' for 10 seconds
            $timeout .= "s";
        }

        # Extract versions of CRD files to be installed
        for $sc_crds_file (@sc_crds_files) {
            if ($sc_crds_file =~ /^.+\/(.+)-(\d+\.\d+\.\d+)([\-\+])(.+)\.(tgz|tar\.gz)$/) {
                # E.g. .../eric-sc-crds-1.5.0+200.tgz or .../eric-sec-sip-tls-crd-2.9.0+36.tgz etc.
                $crd_file_hash{$sc_crds_file}{'releasename'} = $1;
                $crd_file_hash{$sc_crds_file}{'version'} = $2;
                $crd_file_hash{$sc_crds_file}{'sign'} = $3;
                $crd_file_hash{$sc_crds_file}{'build'} = $4;
            } else {
                # This should not happen, but just in case
                $crd_file_hash{$sc_crds_file}{'releasename'} = "";
                $crd_file_hash{$sc_crds_file}{'version'} = "";
                $crd_file_hash{$sc_crds_file}{'sign'} = "";
                $crd_file_hash{$sc_crds_file}{'build'} = "";
            }
        }

        # Find the known CRD helm charts for the new and old software (if exists) and create a regex of the names.
        # We need to include also the old software list in case some CRD's has been removed in the new software
        # but the CRD's has not been removed.
        %known_crd_helm_charts = map { $_ => 1 } (
            ADP::Kubernetes_Operations::get_list_of_known_sc_crd_helm_charts($app_type, $::JOB_PARAMS{'SC_RELEASE_VERSION'}),
            exists $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} ? ADP::Kubernetes_Operations::get_list_of_known_sc_crd_helm_charts($app_type, $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}) : ()
        );
        $known_crd_helm_charts_regex = join "|", sort keys %known_crd_helm_charts;

        # Fetch information of CRD already installed
        General::Logging::log_user_message("Reading information about already installed CRD helm charts");
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

            General::Logging::log_user_error_message("Failed to fetch list of helm releases");
            return 1;
        }
        for (@result) {
            if (/^(\S+)\s+(\S+).+\s+deployed\s+($known_crd_helm_charts_regex)-(\d+\.\d+\.\d+)([\-\+])(\S+).*/) {
                # Old SC CRD package e.g. eric-sc-crds-1.5.0+200
                # New SC CRD packages e.g. eric-sec-sip-tls-crd-2.9.0+36, eric-sec-certm-crd-3.8.0+99, eric-tm-ingress-controller-cr-crd-6.0.0+37
                $crd_installed_hash{$3}{'releasename'} = $1;
                $crd_installed_hash{$3}{'namespace'} = $2;
                $crd_installed_hash{$3}{'version'} = $4;
                $crd_installed_hash{$3}{'sign'} = $5;
                $crd_installed_hash{$3}{'build'} = $6;
            }
        }

        # Check if we need to install/upgrade the CRD's and set proper namespace and release name
        if (scalar @sc_crds_files == 1) {
            # One CRD file to install/upgrade
            $sc_crds_file = $sc_crds_files[0];
            if (%crd_installed_hash && scalar keys %crd_installed_hash == 1) {
                # One CRD already installed
                $name_without_version = $crd_file_hash{$sc_crds_file}{'releasename'};
                if (exists $crd_installed_hash{$name_without_version}) {
                    # And it matches with the one to upgrade, so continue to upgrade it using the already existing namespace and release name
                    $crd_namespace = $crd_installed_hash{$name_without_version}{'namespace'};
                    $crd_release_name = $crd_installed_hash{$name_without_version}{'releasename'};
                } else {
                    # This should not happen but handle it just in case
                    General::Logging::log_user_error_message("There is one CRD file to load and one installed CRD helm chart but they don't match, skipping the install/upgrade");
                    # On request from Kay on 2022-08-15 this will be written as an error message and fail the task.
                    # return General::Playlist_Operations::RC_STEPOUT;
                    return 1;
                }
            } elsif (%crd_installed_hash && scalar keys %crd_installed_hash > 1) {
                # We already have the new multi CRD helm charts installed, so don't install the file
                General::Logging::log_user_message("There are already multiple helm charts installed:\n" . join("\n", keys %crd_installed_hash) . "\nNo need to install/upgrade the single helm chart file: $sc_crds_file\n");
                return General::Playlist_Operations::RC_STEPOUT;
            } else {
                # No CRD installed, continue to install it using the namespace and release name from the job variables.
                $crd_namespace = $::JOB_PARAMS{'CRD_NAMESPACE'};
                $crd_release_name = $::JOB_PARAMS{'CRD_RELEASE_NAME'};
            }
        } else {
            # Multiple CRD files to install/upgrade
            if (%crd_installed_hash && scalar keys %crd_installed_hash == 1) {
                # One CRD already installed.
                # Now we have two specific cases to check for:
                # 1. If the installed CRD is the old one e.g. eric-sc-crds-1.5.0+200 then we have a problem
                #    because it should not happen since this would indicate that the playlist extension that
                #    is supposed to remove this helm chart was not executed or failed.
                #    So report this as a failure.
                # 2. The installed CRD is something else which might be an indication that we initially had
                #    all CRD's that SC is using installed and then we undeployed the SC without removing the
                #    CRD's. Then we installed the DSC software and since this software is only using a few of
                #    the same CRD's that the SC is using we might just have upgraded these CRD's.
                #    Then we removed the DSC and also removed all the CRD's used by the DSC which now only
                #    removed a few CRD's and left one from the previous SC deployment loaded.
                #    For example the eric-data-key-value-database-rd-crd-1.1.0+1 CRD.
                #    Without this new check we would previously reported an error thinking that this CRD
                #    was in fact the eric-sc-crds-1.5.0+200 CRD.
                #    In this case we should just continue and install and/or upgrade all CRD's in the software
                #    package.
                if ( ((%crd_installed_hash)[0] eq "eric-sc-crds") ) {
                    # 1. This is an indication that something went wrong with the playlist extension.
                    General::Logging::log_user_error_message("There are multiple CRD files to load and only one installed CRD helm chart which should not be the case.\nPlease check if the pre-upgrade playlist extension was properly executed, skipping the install/upgrade\n");
                    # On request from Kay on 2022-08-15 this will be written as an error message and fail the task.
                    # return General::Playlist_Operations::RC_STEPOUT;
                    return 1;
                } else {
                    # 2. This is an indication that it is some other kind of CRD so we just continue.
                    # One of the known CRD's is already installed, continue to upgrade them using their existing namespace and release names
                    $crd_namespace = "";
                    $crd_release_name = "";
                }
            } elsif (%crd_installed_hash && scalar keys %crd_installed_hash > 1) {
                # Multiple CRD's already installed, continue to upgrade them using their existing namespace and release names
                $crd_namespace = "";
                $crd_release_name = "";
            } else {
                # No CRD's installed, or one was installed but the playlist extension removed the
                # helm chart because of upgrade from SC <1.6 to SC >=1.6 where the CRD was changed
                # from one helm chart to 3 or more helm charts.
                if ($old_crd_namespace ne "") {
                    # Old single CRD helm chart was previous installed and was removed by the playlist extension.
                    # Reuse the old namespace for installing the new multi CRD helm chart files.
                    $crd_namespace = $::JOB_PARAMS{'OLD_CRD_NAMESPACE'};
                    $crd_release_name = "";
                } else {
                    # Nothing was previously installed, install them into the namespace from the job variable
                    $crd_namespace = $::JOB_PARAMS{'CRD_NAMESPACE'};
                    $crd_release_name = "";
                }
            }
        }

        General::Logging::log_user_message("Deploying CRDs.");

        for $sc_crds_file (@sc_crds_files) {

            $name_without_version = $crd_file_hash{$sc_crds_file}{'releasename'};
            General::Logging::log_user_message("  Checking CRD name '$name_without_version'");

            # Check the version of the file against existing CRD's
            if (exists $crd_installed_hash{$name_without_version}) {
                # The CRD already exist, check the version
                if ( version->parse( $crd_file_hash{$sc_crds_file}{'version'} ) == version->parse( $crd_installed_hash{$name_without_version}{'version'} ) ) {
                    #
                    # Same version, continue checking sign (+ or -)
                    #
                    if ( $crd_file_hash{$sc_crds_file}{'sign'} eq $crd_installed_hash{$name_without_version}{'sign'} ) {
                        #
                        # Same sign, continue checking build number
                        #
                        if ( $crd_file_hash{$sc_crds_file}{'build'} eq $crd_installed_hash{$name_without_version}{'build'} ) {
                            # The same version of CRD already installed, no need to install it
                            General::Logging::log_user_message(
                                sprintf "    The CRD '%s' is already installed with same version %s, skipping this CRD (line %s)",
                                    $name_without_version,
                                    ($crd_installed_hash{$name_without_version}{'version'} . $crd_installed_hash{$name_without_version}{'sign'} . $crd_installed_hash{$name_without_version}{'build'}),
                                    __LINE__
                            );
                            next;
                        } elsif ( $crd_file_hash{$sc_crds_file}{'build'} =~ /^\d+$/ && $crd_installed_hash{$name_without_version}{'build'} =~ /^\d+$/) {
                            # Different version numbers, check which is newest
                            if ( $crd_file_hash{$sc_crds_file}{'build'} > $crd_installed_hash{$name_without_version}{'build'}) {
                                # The file version is newer, upgrade it
                                $crd_namespace = $crd_installed_hash{$name_without_version}{'namespace'};
                                $crd_release_name = $crd_installed_hash{$name_without_version}{'releasename'};
                                General::Logging::log_user_message(
                                    sprintf "    The CRD '%s' is being upgraded from version %s to version %s (line %s)",
                                        $name_without_version,
                                        ($crd_installed_hash{$name_without_version}{'version'} . $crd_installed_hash{$name_without_version}{'sign'} . $crd_installed_hash{$name_without_version}{'build'}),
                                        ($crd_file_hash{$sc_crds_file}{'version'} . $crd_file_hash{$sc_crds_file}{'sign'} . $crd_file_hash{$sc_crds_file}{'build'}),
                                        __LINE__
                                );
                            } else {
                                # The file version is the same or older, skip it
                                General::Logging::log_user_message(
                                    sprintf "    The CRD '%s' file version %s is same or older version than the installed version %s, skipping this CRD",
                                        $name_without_version,
                                        ($crd_file_hash{$sc_crds_file}{'version'} . $crd_file_hash{$sc_crds_file}{'sign'} . $crd_file_hash{$sc_crds_file}{'build'}),
                                        ($crd_installed_hash{$name_without_version}{'version'} . $crd_installed_hash{$name_without_version}{'sign'} . $crd_installed_hash{$name_without_version}{'build'})
                                );
                                next;
                            }
                        } else {
                            # Different build numbers and one or both are not numbers, check which one is newer
                            if ($::JOB_PARAMS{'FORCE_UPGRADE_CRD'} eq "yes") {
                                $crd_namespace = $crd_installed_hash{$name_without_version}{'namespace'};
                                $crd_release_name = $crd_installed_hash{$name_without_version}{'releasename'};
                                General::Logging::log_user_warning_message(
                                    sprintf "    The CRD '%s' file version is %s and installed version is %s.\n  Since FORCE_UPGRADE_CRD=yes we upgrade to the file version.\n",
                                        $name_without_version,
                                        ($crd_file_hash{$sc_crds_file}{'version'} . $crd_file_hash{$sc_crds_file}{'sign'} . $crd_file_hash{$sc_crds_file}{'build'}),
                                        ($crd_installed_hash{$name_without_version}{'version'} . $crd_installed_hash{$name_without_version}{'sign'} . $crd_installed_hash{$name_without_version}{'build'})
                                );
                            } else {
                                General::Logging::log_user_warning_message(
                                    sprintf "    The CRD '%s' file version is %s and installed version is %s.\n  Since one or both have a non-numeric build number it's not possible to determine which one to use.\n  Skipping this CRD, if you still want to upgrade to the file version then pass job parameter FORCE_UPGRADE_CRD=yes.\n",
                                        $name_without_version,
                                        ($crd_file_hash{$sc_crds_file}{'version'} . $crd_file_hash{$sc_crds_file}{'sign'} . $crd_file_hash{$sc_crds_file}{'build'}),
                                        ($crd_installed_hash{$name_without_version}{'version'} . $crd_installed_hash{$name_without_version}{'sign'} . $crd_installed_hash{$name_without_version}{'build'})
                                );
                                next;
                            }
                        }
                    } else {
                        #
                        # Signs are different
                        #
                        if ( $crd_file_hash{$sc_crds_file}{'sign'} eq "+" ) {
                            # The file version is "+" and the installed is not, so assume the file is newer
                            $crd_namespace = $crd_installed_hash{$name_without_version}{'namespace'};
                            $crd_release_name = $crd_installed_hash{$name_without_version}{'releasename'};
                            General::Logging::log_user_message(
                                sprintf "    The CRD '%s' is being upgraded from version %s to version %s (line %s)",
                                    $name_without_version,
                                    ($crd_installed_hash{$name_without_version}{'version'} . $crd_installed_hash{$name_without_version}{'sign'} . $crd_installed_hash{$name_without_version}{'build'}),
                                    ($crd_file_hash{$sc_crds_file}{'version'} . $crd_file_hash{$sc_crds_file}{'sign'} . $crd_file_hash{$sc_crds_file}{'build'}),
                                    __LINE__
                            );
                        } else {
                            # The installed version if probably a "+" version so keep it
                            General::Logging::log_user_message(
                                sprintf "    The CRD '%s' file version %s is same but with a different sign than the installed version %s, skipping this CRD",
                                    $name_without_version,
                                    ($crd_file_hash{$sc_crds_file}{'version'} . $crd_file_hash{$sc_crds_file}{'sign'} . $crd_file_hash{$sc_crds_file}{'build'}),
                                    ($crd_installed_hash{$name_without_version}{'version'} . $crd_installed_hash{$name_without_version}{'sign'} . $crd_installed_hash{$name_without_version}{'build'})
                            );
                            next;
                        }
                    }
                } elsif ( version->parse( $crd_file_hash{$sc_crds_file}{'version'} ) > version->parse( $crd_installed_hash{$name_without_version}{'version'} ) ) {
                    #
                    # The file has a higher version
                    #
                    if ( $crd_file_hash{$sc_crds_file}{'version'} =~ /^\d+\.\d+\.25$/) {
                            # Special handling for x.x.25 version
                            my $file_version = $crd_file_hash{$sc_crds_file}{'version'} =~ s/\.\d+$//r;                     # Keep the first two number and throw away the last .25
                            my $installed_version = $crd_installed_hash{$name_without_version}{'version'} =~ s/\.\d+$//r;   # Keep the first two numbers and throw way the last number
                            if ( version->parse( $file_version ) == version->parse( $installed_version ) ) {
                                # We are trying to replace x.x.0-24 by x.x.25 which would basically mean a downgrade
                                # since .25 is a temporary build before a release and number .0 to .24 are official
                                # released versions. So keep the installed version.
                                General::Logging::log_user_message(
                                    sprintf "    The CRD '%s' file version %s is an unofficial release and the installed version %s is an official release of the same version, skipping this CRD",
                                            $name_without_version,
                                            ($crd_file_hash{$sc_crds_file}{'version'} . $crd_file_hash{$sc_crds_file}{'sign'} . $crd_file_hash{$sc_crds_file}{'build'}),
                                            ($crd_installed_hash{$name_without_version}{'version'} . $crd_installed_hash{$name_without_version}{'sign'} . $crd_installed_hash{$name_without_version}{'build'})
                                );
                                next;
                            } else {
                                # The file version is from a newer SC release e.g. 1.y.25 compared with the installed
                                # 1.x.x version, upgrade to the new CRD but keep existing namespace and release name.
                                $crd_namespace = $crd_installed_hash{$name_without_version}{'namespace'};
                                $crd_release_name = $crd_installed_hash{$name_without_version}{'releasename'};
                                General::Logging::log_user_message(
                                    sprintf "    The CRD '%s' is being upgraded from version %s to version %s (line %s)",
                                            $name_without_version,
                                            ($crd_installed_hash{$name_without_version}{'version'} . $crd_installed_hash{$name_without_version}{'sign'} . $crd_installed_hash{$name_without_version}{'build'}),
                                            ($crd_file_hash{$sc_crds_file}{'version'} . $crd_file_hash{$sc_crds_file}{'sign'} . $crd_file_hash{$sc_crds_file}{'build'}),
                                        __LINE__
                                );
                            }
                    } else {
                            # The file has a higher version, upgrade to the new CRD but keep existing namespace and
                            # release name.
                            $crd_namespace = $crd_installed_hash{$name_without_version}{'namespace'};
                            $crd_release_name = $crd_installed_hash{$name_without_version}{'releasename'};
                                General::Logging::log_user_message(
                                    sprintf "    The CRD '%s' is being upgraded from version %s to version %s (line %s)",
                                        $name_without_version,
                                        ($crd_installed_hash{$name_without_version}{'version'} . $crd_installed_hash{$name_without_version}{'sign'} . $crd_installed_hash{$name_without_version}{'build'}),
                                        ($crd_file_hash{$sc_crds_file}{'version'} . $crd_file_hash{$sc_crds_file}{'sign'} . $crd_file_hash{$sc_crds_file}{'build'}),
                                        __LINE__
                                );
                    }
                } else {
                    #
                    # The file version is lower than the installed version
                    #
                    if ( $crd_installed_hash{$name_without_version}{'version'} =~ /^\d+\.\d+\.25$/) {
                        # Special handling for x.x.25 version
                        my $file_version = $crd_file_hash{$sc_crds_file}{'version'} =~ s/\.\d+$//r;                     # Keep the first two number and throw away the last .25
                        my $installed_version = $crd_installed_hash{$name_without_version}{'version'} =~ s/\.\d+$//r;   # Keep the first two numbers and throw way the last number
                        if ( version->parse( $file_version ) == version->parse( $installed_version ) ) {
                            # We are trying to replace installed x.x.25 by file version x.x.0-24 which mean we
                            # are trying to install the official release version 0-24 over the .25 temporary
                            # build. So install the file but keep existing namespace and release name.
                            $crd_namespace = $crd_installed_hash{$name_without_version}{'namespace'};
                            $crd_release_name = $crd_installed_hash{$name_without_version}{'releasename'};
                                General::Logging::log_user_message(
                                    sprintf "    The CRD '%s' is being upgraded from version %s to version %s (line %s)",
                                        $name_without_version,
                                        ($crd_installed_hash{$name_without_version}{'version'} . $crd_installed_hash{$name_without_version}{'sign'} . $crd_installed_hash{$name_without_version}{'build'}),
                                        ($crd_file_hash{$sc_crds_file}{'version'} . $crd_file_hash{$sc_crds_file}{'sign'} . $crd_file_hash{$sc_crds_file}{'build'}),
                                    __LINE__
                                );
                        } else {
                            # The file version is from a newer SC release e.g. 1.y.25 compared with the
                            # installed 1.x.x version, upgrade to the new CRD but keep existing namespace and release name
                            General::Logging::log_user_message(
                                sprintf "    The CRD '%s' file version %s is lower than the already installed version %s, skipping this CRD (line %s)",
                                        $name_without_version,
                                        ($crd_file_hash{$sc_crds_file}{'version'} . $crd_file_hash{$sc_crds_file}{'sign'} . $crd_file_hash{$sc_crds_file}{'build'}),
                                        ($crd_installed_hash{$name_without_version}{'version'} . $crd_installed_hash{$name_without_version}{'sign'} . $crd_installed_hash{$name_without_version}{'build'}),
                                    __LINE__
                            );
                            next;
                        }
                    } else {
                        # The file version is lower than the installed version which is not x.x.25 so
                        # no special handling needed. Keep the installed version.
                        General::Logging::log_user_message(
                            sprintf "    The CRD '%s' file version %s is lower than the already installed version %s, skipping this CRD (line %s)",
                                    $name_without_version,
                                    ($crd_file_hash{$sc_crds_file}{'version'} . $crd_file_hash{$sc_crds_file}{'sign'} . $crd_file_hash{$sc_crds_file}{'build'}),
                                    ($crd_installed_hash{$name_without_version}{'version'} . $crd_installed_hash{$name_without_version}{'sign'} . $crd_installed_hash{$name_without_version}{'build'}),
                                    __LINE__
                        );
                        next;
                    }
                }
            } else {

                # The CRD is not installed, or was installed but removed by a playlist extension
                if ($::JOB_PARAMS{'OLD_CRD_NAMESPACE'} ne "") {
                    # It was removed by a playlist extension, so install it but using the old namespace but new release names
                    $crd_namespace = $::JOB_PARAMS{'OLD_CRD_NAMESPACE'};
                } else {
                    # It was not installed before, so use CDR namespace from job variable and new release names
                    $crd_namespace = $::JOB_PARAMS{'CRD_NAMESPACE'};
                }

                if ($sc_crds_file =~ /^.+eric-sc-crds-.+/) {
                    $crd_release_name = $::JOB_PARAMS{'CRD_RELEASE_NAME'};
                } elsif ($sc_crds_file =~ /^.+\/(eric-.+crd)-.+/) {
                    $crd_release_name = $1;
                } else {
                    # Unknown name format, ignore it
                    General::Logging::log_user_warning_message(
                        sprintf "    Unexpected file name '%s', did not contain '*eric-sc-crds-*' or '*eric-*crd-*', skipping this CRD (line %s)",
                            $sc_crds_file,
                            __LINE__
                    );
                    next;
                }
            }

            General::Logging::log_user_message("    Deploying CRD release name '$crd_release_name' in namespace '$crd_namespace'.\n    This will take a while to complete.\n");

            if ($::JOB_PARAMS{'SC_RELEASE_VERSION'} eq "1.2.2") {
                # Special handling for older SC release 1.2.2 where the url and secret names
                # are located in a different place in the eric-sc-values.yaml file.
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}${helm_executable} upgrade --install $crd_release_name $sc_crds_file --namespace $crd_namespace --atomic --set eric-sec-certm-crd.imageCredentials.registry.url=$registry_url --set eric-sec-certm-crd.imageCredentials.registry.pullSecret=$registry_secret_name --set eric-sec-sip-tls-crd.imageCredentials.registry.url=$registry_url --set eric-sec-sip-tls-crd.imageCredentials.registry.pullSecret=$registry_secret_name --set eric-tm-ingress-controller-cr-crd.imageCredentials.registry.url=$registry_url --set eric-tm-ingress-controller-cr-crd.imageCredentials.registry.pullSecret=$registry_secret_name --timeout $timeout --debug",
                        "hide-output"   => 1,
                    }
                );
            } else {
                # In SC release 1.3.0 or newer the url and secrets are stored in the
                # global part of the eric-sc-values.yaml file.
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "${debug_command}${helm_executable} upgrade --install $crd_release_name $sc_crds_file --namespace $crd_namespace --atomic --set global.registry.url=$registry_url --set global.pullSecret=$registry_secret_name --timeout $timeout --debug",
                        "hide-output"   => 1,
                    }
                );
            }

            if ($rc == 0) {
                push @::JOB_STATUS, "(/) Installing/Upgrading CRDs for release name $crd_release_name successful";
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to deploy CRDsfor release name $crd_release_name ");
                push @::JOB_STATUS, "(x) Installing/Upgrading CRDs for release name $crd_release_name failed";
                return $rc;
            }
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
sub Fallback001_P927S99 {

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

This playlist performs a deploy and upgrade of the CRDS on a SC node.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
