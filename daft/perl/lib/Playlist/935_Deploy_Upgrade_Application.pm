package Playlist::935_Deploy_Upgrade_Application;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.27
#  Date     : 2024-06-06 10:14:16
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

    $rc = General::Playlist_Operations::execute_step( \&Deploy_Upgrade_Application_P935S01, \&Fallback001_P935S99 );
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
sub Deploy_Upgrade_Application_P935S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Fetch_Installed_Releases_P935S01T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Install_Upgrade_Application_P935S01T02 } );
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
    sub Fetch_Installed_Releases_P935S01T01 {
        my $rc = 0;

        General::Logging::log_user_message("Fetching Installed Releases");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$::JOB_PARAMS{'HELM_EXECUTABLE'} list --all --all-namespaces",
                "hide-output"   => 1,
                "save-to-file"  => "$::JOB_PARAMS{'_JOB_LOG_DIR'}/releases_list_before.txt",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to fetch Installed Releases");
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
    sub Install_Upgrade_Application_P935S01T02 {
        my %application_file_hash;
        my @application_files = ();
        my %application_installed_hash;
        my @commands = ();
        my @commands_action = ();
        my @commands_filename = ();
        my @commands_message = ();
        my @commands_namespace = ();
        my @commands_release = ();
        my %enabled_cnf;
        my $extra_parameters = "";
        my $filename;
        my $header_text = "";
        my $helm_executable =  $::JOB_PARAMS{'HELM_EXECUTABLE'};
        my $max_length_filename = 9;
        my $max_length_namespace = 9;
        my $max_length_release = 12;
        my $message = "";
        my @messages = ();
        my $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $name_without_version;
        my $release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
        my $rc = 0;
        my $registry_secret_name = $::JOB_PARAMS{'SC_REGISTRY_SECRET_NAME'};
        my $registry_url = $::JOB_PARAMS{'SC_REGISTRY_URL'};
        my @result;
        my $timeout = $::JOB_PARAMS{'HELM_TIMEOUT_APPLICATION'};
        my $upgrade_helm_chart = 0;
        my $upgrade_needed = 0;
        my $values_file = "";
        my $values_file_extra = "";
        my $values_file_path = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}";

        #
        # This variable was introduced after a talk with Kay Behn where he want us to ignore the
        # version check i.e. to always upgrade even to the same or an older version of the software.
        # Just in case in the future there is a change of mind in the way this is handled and the
        # old check is wanted again I leve the code and just by-pass it if that is wanted.
        my $skip_version_check = exists $::JOB_PARAMS{'SKIP_UMBRELLA_VERSION_CHECK'} ? $::JOB_PARAMS{'SKIP_UMBRELLA_VERSION_CHECK'} : "yes";

        $::JOB_PARAMS{'DEPLOYED_UPGRADED_HELM_RELEASES'} = "";

        # Add helm files
        push @application_files, $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} if (exists $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} && $::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} ne "");
        push @application_files, $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} if (exists $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} && $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'} ne "");
        push @application_files, $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} if (exists $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} && $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} ne "");

        # Check which application helm charts we should load based on the ENABLED_CNF value
        my $cnf_string = $::JOB_PARAMS{'ENABLED_CNF'};
        $cnf_string =~ s/-/ /g;     # Replace dashes with spaces
        $cnf_string =~ s/,/ /g;     # Replace commas with spaces
        $cnf_string =~ s/^\s+//g;   # Remove spaces at beginning
        $cnf_string =~ s/\s+$//g;   # Remove spaces at end
        for my $cnf (split /\s+/, $cnf_string) {
            $enabled_cnf{$cnf} = 1;
        }
        #push @application_files, (split /\|/, $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'}) if (exists $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} && $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} ne "");
        if (exists $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} && $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} =~ /\|/) {
            # It contains more than one helm chart file which means it's either cnDSC release <1.15 or the new CNCS release starting with 1.15
            # which means we need to check if we should include the helm chart or not.
            $message = "Checking which files in APPLICATION_UMBRELLA_FILE should be included based on ENABLED_CNF:\n";
            for $filename (split /\|/, $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'}) {
                if ($filename =~ /Definitions\/OtherTemplates\/eric-sc-bsf-.+/) {
                    if (exists $enabled_cnf{'bsf'}) {
                        push @application_files, $filename;
                        $message .= "  File included because ENABLED_CNF includes 'bsf':        $filename\n";
                    } else {
                        $message .= "  File not included because ENABLED_CNF is missing 'bsf':  $filename\n";
                    }
                } elsif ($filename =~ /Definitions\/OtherTemplates\/eric-sc-scp-.+/) {
                    if (exists $enabled_cnf{'scp'}) {
                        push @application_files, $filename;
                        $message .= "  File included because ENABLED_CNF includes 'scp':        $filename\n";
                    } else {
                        $message .= "  File not included because ENABLED_CNF is missing 'scp':  $filename\n";
                    }
                } elsif ($filename =~ /Definitions\/OtherTemplates\/eric-sc-sepp-.+/) {
                    if (exists $enabled_cnf{'sepp'}) {
                        push @application_files, $filename;
                        $message .= "  File included because ENABLED_CNF includes 'sepp':       $filename\n";
                    } else {
                        $message .= "  File not included because ENABLED_CNF is missing 'sepp': $filename\n";
                    }
                } elsif ($filename =~ /Definitions\/OtherTemplates\/eric-(dsc|sc-diameter)-.+/) {
                    if ($::JOB_PARAMS{'APPLICATION_TYPE'} eq "dsc" && version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) < version->parse( "1.15.0" ) ) {
                        # Old non-CNCS package version < 1.15
                        push @application_files, $filename;
                        $message .= "  File included because APPLICATION_TYPE='dsc':            $filename\n";
                    } elsif (exists $enabled_cnf{'dsc'}) {
                        # New CNC S package version >= 1.15
                        push @application_files, $filename;
                        $message .= "  File included because ENABLED_CNF includes 'dsc':        $filename\n";
                    } else {
                        # New CNC S package version >= 1.15
                        $message .= "  File not included because ENABLED_CNF is missing 'dsc':  $filename\n";
                    }
                } else {
                    push @application_files, $filename;
                    $message .= "  File included because no need to check ENABLED_CNF:      $filename\n";
                }
            }
            General::Logging::log_user_message($message);
            $message = "";
        } elsif (exists $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} && $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'} ne "") {
            # Old SC <1.15 release
            # It only contain one helm chart, so no need to check which to include since this will be handled by global.ericsson.XXX.enabled=true values in the values file.
            push @application_files, $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'};
        }

        #
        # Extract versions of files to be installed
        #

        for $filename (@application_files) {
            if ($filename =~ /^.+\/(.+)-(\d+\.\d+\.\d+)([\-\+])(.+)\.(tgz|tar\.gz)$/) {
                # E.g. .../eric-dsc-1.9.0-1-hf799d8b.tgz
                # or   .../eric-sc-umbrella-1.7.25+293.tgz
                # etc.
                $application_file_hash{$filename}{'releasename'} = $1;
                $application_file_hash{$filename}{'version'} = $2;
                $application_file_hash{$filename}{'sign'} = $3;
                $application_file_hash{$filename}{'build'} = $4;
            } elsif ($filename =~ /^.+\/(.+)-(\d+\.\d+\.\d+)\.(tgz|tar\.gz)$/) {
                # E.g. .../eric-cloud-native-base-58.8.1.tgz
                # or   .../eric-cloud-native-nf-additions-23.3.0.tgz
                # etc.
                $application_file_hash{$filename}{'releasename'} = $1;
                $application_file_hash{$filename}{'version'} = $2;
                $application_file_hash{$filename}{'sign'} = "";
                $application_file_hash{$filename}{'build'} = "";
            } else {
                # This should not happen, but just in case
                $application_file_hash{$filename}{'releasename'} = "";
                $application_file_hash{$filename}{'version'} = "";
                $application_file_hash{$filename}{'sign'} = "";
                $application_file_hash{$filename}{'build'} = "";
            }
        }

        #
        # Fetch information of applications already installed
        #

        General::Logging::log_user_message("Reading information about already installed helm releases on the namespace");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$helm_executable list --all --namespace=$namespace",
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
            if (/^(\S+)\s+(\S+).+\s+deployed\s+(\S+)-(\d+\.\d+\.\d+)([\-\+])(\S+).*/) {
                # Chart with build number
                #NAME       NAMESPACE    REVISION    UPDATED                                    STATUS      CHART                          APP VERSION
                #eric-sc    eiffelesc    2           2022-08-31 10:05:42.716888171 +0000 UTC    deployed    eric-sc-umbrella-1.10.25+61
                #eric-dsc   eiffeldsc    1           2022-08-24 12:36:43.290386552 +0000 UTC    deployed    eric-dsc-1.9.0-1-h113a35e      1.9.0-936
                $application_installed_hash{$3}{'releasename'} = $1;
                $application_installed_hash{$3}{'namespace'} = $2;
                $application_installed_hash{$3}{'version'} = $4;
                $application_installed_hash{$3}{'sign'} = $5;
                $application_installed_hash{$3}{'build'} = $6;
            } elsif (/^(\S+)\s+(\S+).+\s+deployed\s+(\S+)-(\d+\.\d+\.\d+)\s*.*/) {
                # Chart without build number
                #NAME                                    NAMESPACE       REVISION        UPDATED                                 STATUS          CHART                                           APP VERSION
                #eric-cloud-native-base                  eiffeldsc       1               2022-08-24 12:28:27.462719757 +0000 UTC deployed        eric-cloud-native-base-58.8.1                   58.8.1
                #eric-cloud-native-nf-additions          eiffeldsc       1               2022-08-24 12:32:07.357666758 +0000 UTC deployed        eric-cloud-native-nf-additions-23.3.0           23.3.0
                $application_installed_hash{$3}{'releasename'} = $1;
                $application_installed_hash{$3}{'namespace'} = $2;
                $application_installed_hash{$3}{'version'} = $4;
                $application_installed_hash{$3}{'sign'} = "";
                $application_installed_hash{$3}{'build'} = "";
            }
        }

        #
        # Checking which helm charts to Install or Upgrade
        #

        General::Logging::log_user_message("Looking for software to Install or Upgrade.");

        for $filename (@application_files) {

            $upgrade_helm_chart = 0;

            $name_without_version = $application_file_hash{$filename}{'releasename'};
            General::Logging::log_user_message("  Checking Application name '$name_without_version'");

            if ($skip_version_check eq "yes") {
                #
                # No version check will be done.
                #

                if (exists $application_installed_hash{$name_without_version}) {
                    # The application is already installed, so reuse the namespace and release name from the already installed software.
                    $namespace = $application_installed_hash{$name_without_version}{'namespace'};
                    $release_name = $application_installed_hash{$name_without_version}{'releasename'};
                    General::Logging::log_user_message(
                        sprintf "    The Application '%s' is being upgraded from version %s to version %s (line %s)",
                            $name_without_version,
                            ($application_installed_hash{$name_without_version}{'version'} . $application_installed_hash{$name_without_version}{'sign'} . $application_installed_hash{$name_without_version}{'build'}),
                            ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                            __LINE__
                    );
                    $upgrade_needed++;
                    $upgrade_helm_chart = 1;
                } else {
                    # The Application is not installed, so use Application namespace from job variable and new release names
                    $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

                    if ($filename =~ /^.+eric-(sc-umbrella|dsc)-.+/ && version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) < version->parse( "1.15.0" )) {
                        $release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
                    } else {
                        # Unknown name format, use the name without revision as the release name
                        $release_name = $name_without_version;
                    }
                    General::Logging::log_user_message(
                        sprintf "    The Application '%s' file version %s was not installed, Installing it (line %s)",
                                $name_without_version,
                                ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                                __LINE__
                    );
                }

            } else {

                #
                # Check the version of the file against existing Application's
                #

                if (exists $application_installed_hash{$name_without_version}) {
                    # The Application already exist, check the version
                    if ( version->parse( $application_file_hash{$filename}{'version'} ) == version->parse( $application_installed_hash{$name_without_version}{'version'} ) ) {
                        #
                        # Same version, continue checking sign (+ or -)
                        #
                        if ( $application_file_hash{$filename}{'sign'} eq $application_installed_hash{$name_without_version}{'sign'} ) {
                            #
                            # Same sign, continue checking build number
                            #
                            if ( $application_file_hash{$filename}{'build'} eq $application_installed_hash{$name_without_version}{'build'} ) {
                                # The same version of Application already installed, no need to install it
                                General::Logging::log_user_message(
                                    sprintf "    The Application '%s' is already installed with same version %s, skipping this Application (line %s)",
                                        $name_without_version,
                                        ($application_installed_hash{$name_without_version}{'version'} . $application_installed_hash{$name_without_version}{'sign'} . $application_installed_hash{$name_without_version}{'build'}),
                                        __LINE__
                                );
                                next;
                            } elsif ( $application_file_hash{$filename}{'build'} =~ /^\d+$/ && $application_installed_hash{$name_without_version}{'build'} =~ /^\d+$/) {
                                # Different version numbers, check which is newest
                                if ( $application_file_hash{$filename}{'build'} > $application_installed_hash{$name_without_version}{'build'}) {
                                    # The file version is newer, upgrade it
                                    $namespace = $application_installed_hash{$name_without_version}{'namespace'};
                                    $release_name = $application_installed_hash{$name_without_version}{'releasename'};
                                    General::Logging::log_user_message(
                                        sprintf "    The Application '%s' is being upgraded from version %s to version %s (line %s)",
                                            $name_without_version,
                                            ($application_installed_hash{$name_without_version}{'version'} . $application_installed_hash{$name_without_version}{'sign'} . $application_installed_hash{$name_without_version}{'build'}),
                                            ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                                            __LINE__
                                    );
                                    $upgrade_needed++;
                                    $upgrade_helm_chart = 1;
                                } else {
                                    # The file version is the same or older, skip it
                                    General::Logging::log_user_message(
                                        sprintf "    The Application '%s' file version %s is same or older version than the installed version %s, skipping this Application",
                                            $name_without_version,
                                            ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                                            ($application_installed_hash{$name_without_version}{'version'} . $application_installed_hash{$name_without_version}{'sign'} . $application_installed_hash{$name_without_version}{'build'})
                                    );
                                    next;
                                }
                            } else {
                                # Different build numbers and one or both are not numbers, check which one is newer
                                if ($::JOB_PARAMS{'FORCE_UPGRADE_APPLICATION'} eq "yes") {
                                    $namespace = $application_installed_hash{$name_without_version}{'namespace'};
                                    $release_name = $application_installed_hash{$name_without_version}{'releasename'};
                                    General::Logging::log_user_warning_message(
                                        sprintf "    The Application '%s' file version is %s and installed version is %s.\n  Since FORCE_UPGRADE_APPLICATION=yes we upgrade to the file version.\n",
                                            $name_without_version,
                                            ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                                            ($application_installed_hash{$name_without_version}{'version'} . $application_installed_hash{$name_without_version}{'sign'} . $application_installed_hash{$name_without_version}{'build'})
                                    );
                                    $upgrade_needed++;
                                    $upgrade_helm_chart = 1;
                                } else {
                                    General::Logging::log_user_warning_message(
                                        sprintf "    The Application '%s' file version is %s and installed version is %s.\n  Since one or both have a non-numeric build number it's not possible to determine which one to use.\n  Skipping this Application, if you still want to upgrade to the file version then pass job parameter FORCE_UPGRADE_APPLICATION=yes.\n",
                                            $name_without_version,
                                            ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                                            ($application_installed_hash{$name_without_version}{'version'} . $application_installed_hash{$name_without_version}{'sign'} . $application_installed_hash{$name_without_version}{'build'})
                                    );
                                    next;
                                }
                            }
                        } else {
                            #
                            # Signs are different
                            #
                            if ( $application_file_hash{$filename}{'sign'} eq "+" ) {
                                # The file version is "+" and the installed is not, so assume the file is newer
                                $namespace = $application_installed_hash{$name_without_version}{'namespace'};
                                $release_name = $application_installed_hash{$name_without_version}{'releasename'};
                                General::Logging::log_user_message(
                                    sprintf "    The Application '%s' is being upgraded from version %s to version %s (line %s)",
                                        $name_without_version,
                                        ($application_installed_hash{$name_without_version}{'version'} . $application_installed_hash{$name_without_version}{'sign'} . $application_installed_hash{$name_without_version}{'build'}),
                                        ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                                        __LINE__
                                );
                                $upgrade_needed++;
                                $upgrade_helm_chart = 1;
                            } else {
                                # The installed version if probably a "+" version so keep it
                                General::Logging::log_user_message(
                                    sprintf "    The Application '%s' file version %s is same but with a different sign than the installed version %s, skipping this Application",
                                        $name_without_version,
                                        ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                                        ($application_installed_hash{$name_without_version}{'version'} . $application_installed_hash{$name_without_version}{'sign'} . $application_installed_hash{$name_without_version}{'build'})
                                );
                                next;
                            }
                        }
                    } elsif ( version->parse( $application_file_hash{$filename}{'version'} ) > version->parse( $application_installed_hash{$name_without_version}{'version'} ) ) {
                        #
                        # The file has a higher version
                        #
                        if ( $application_file_hash{$filename}{'version'} =~ /^\d+\.\d+\.25$/) {
                                # Special handling for x.x.25 version
                                my $file_version = $application_file_hash{$filename}{'version'} =~ s/\.\d+$//r;                         # Keep the first two number and throw away the last .25
                                my $installed_version = $application_installed_hash{$name_without_version}{'version'} =~ s/\.\d+$//r;   # Keep the first two numbers and throw way the last number
                                if ( version->parse( $file_version ) == version->parse( $installed_version ) ) {
                                    # We are trying to replace x.x.0-24 by x.x.25 which would basically mean a downgrade
                                    # since .25 is a temporary build before a release and number .0 to .24 are official
                                    # released versions. So keep the installed version.
                                    General::Logging::log_user_message(
                                        sprintf "    The Application '%s' file version %s is an unofficial release and the installed version %s is an official release of the same version, skipping this Application",
                                                $name_without_version,
                                                ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                                                ($application_installed_hash{$name_without_version}{'version'} . $application_installed_hash{$name_without_version}{'sign'} . $application_installed_hash{$name_without_version}{'build'})
                                    );
                                    next;
                                } else {
                                    # The file version is from a newer SC release e.g. 1.y.25 compared with the installed
                                    # 1.x.x version, upgrade to the new Application but keep existing namespace and release name.
                                    $namespace = $application_installed_hash{$name_without_version}{'namespace'};
                                    $release_name = $application_installed_hash{$name_without_version}{'releasename'};
                                    General::Logging::log_user_message(
                                        sprintf "    The Application '%s' is being upgraded from version %s to version %s (line %s)",
                                                $name_without_version,
                                                ($application_installed_hash{$name_without_version}{'version'} . $application_installed_hash{$name_without_version}{'sign'} . $application_installed_hash{$name_without_version}{'build'}),
                                                ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                                            __LINE__
                                    );
                                    $upgrade_needed++;
                                    $upgrade_helm_chart = 1;
                                }
                        } else {
                                # The file has a higher version, upgrade to the new Application but keep existing namespace and
                                # release name.
                                $namespace = $application_installed_hash{$name_without_version}{'namespace'};
                                $release_name = $application_installed_hash{$name_without_version}{'releasename'};
                                General::Logging::log_user_message(
                                    sprintf "    The Application '%s' is being upgraded from version %s to version %s (line %s)",
                                        $name_without_version,
                                        ($application_installed_hash{$name_without_version}{'version'} . $application_installed_hash{$name_without_version}{'sign'} . $application_installed_hash{$name_without_version}{'build'}),
                                        ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                                        __LINE__
                                );
                                $upgrade_needed++;
                                $upgrade_helm_chart = 1;
                        }
                    } else {
                        #
                        # The file version is lower than the installed version
                        #
                        if ( $application_installed_hash{$name_without_version}{'version'} =~ /^\d+\.\d+\.25$/) {
                            # Special handling for x.x.25 version
                            my $file_version = $application_file_hash{$filename}{'version'} =~ s/\.\d+$//r;                         # Keep the first two number and throw away the last .25
                            my $installed_version = $application_installed_hash{$name_without_version}{'version'} =~ s/\.\d+$//r;   # Keep the first two numbers and throw way the last number
                            if ( version->parse( $file_version ) == version->parse( $installed_version ) ) {
                                # We are trying to replace installed x.x.25 by file version x.x.0-24 which mean we
                                # are trying to install the official release version 0-24 over the .25 temporary
                                # build. So install the file but keep existing namespace and release name.
                                $namespace = $application_installed_hash{$name_without_version}{'namespace'};
                                $release_name = $application_installed_hash{$name_without_version}{'releasename'};
                                General::Logging::log_user_message(
                                    sprintf "    The Application '%s' is being upgraded from version %s to version %s (line %s)",
                                        $name_without_version,
                                        ($application_installed_hash{$name_without_version}{'version'} . $application_installed_hash{$name_without_version}{'sign'} . $application_installed_hash{$name_without_version}{'build'}),
                                        ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                                    __LINE__
                                );
                                $upgrade_needed++;
                                $upgrade_helm_chart = 1;
                            } else {
                                # The file version is from a newer SC release e.g. 1.y.25 compared with the
                                # installed 1.x.x version, upgrade to the new Application but keep existing namespace and release name
                                General::Logging::log_user_message(
                                    sprintf "    The Application '%s' file version %s is lower than the already installed version %s, skipping this Application (line %s)",
                                            $name_without_version,
                                            ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                                            ($application_installed_hash{$name_without_version}{'version'} . $application_installed_hash{$name_without_version}{'sign'} . $application_installed_hash{$name_without_version}{'build'}),
                                        __LINE__
                                );
                                next;
                            }
                        } else {
                            # The file version is lower than the installed version which is not x.x.25 so
                            # no special handling needed. Keep the installed version.
                            General::Logging::log_user_message(
                                sprintf "    The Application '%s' file version %s is lower than the already installed version %s, skipping this Application (line %s)",
                                        $name_without_version,
                                        ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                                        ($application_installed_hash{$name_without_version}{'version'} . $application_installed_hash{$name_without_version}{'sign'} . $application_installed_hash{$name_without_version}{'build'}),
                                        __LINE__
                            );
                            next;
                        }
                    }
                } else {

                    # The Application is not installed, so use Application namespace from job variable and new release names
                    $namespace = $::JOB_PARAMS{'SC_NAMESPACE'};

                    if ($filename =~ /^.+eric-(sc-umbrella|dsc)-.+/ && version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) < version->parse( "1.15.0" )) {
                        $release_name = $::JOB_PARAMS{'SC_RELEASE_NAME'};
                    } else {
                        # Unknown name format, use the name without revision as the release name
                        $release_name = $name_without_version;
                    }
                    General::Logging::log_user_message(
                        sprintf "    The Application '%s' file version %s was not installed, Installing it (line %s)",
                                $name_without_version,
                                ($application_file_hash{$filename}{'version'} . $application_file_hash{$filename}{'sign'} . $application_file_hash{$filename}{'build'}),
                                __LINE__
                    );
                }
            }

            #
            # Find the matching values file
            #

            $values_file = "$values_file_path/eric-sc-values.yaml";

            if ($filename =~ /^(.+eric-cloud-native-base-.+(\.tar\.gz|\.tgz))$/ && $::JOB_PARAMS{'CLOUD_NATIVE_BASE_VALUES_FILE'} ne "") {
                # Prepend the special values file
                $values_file = "$::JOB_PARAMS{'CLOUD_NATIVE_BASE_VALUES_FILE'} -f $values_file";
            } elsif ($filename =~ /^(.+eric-cloud-native-nf-additions-.+(\.tar\.gz|\.tgz))$/ && $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_VALUES_FILE'} ne "") {
                # Prepend the special values file
                $values_file = "$::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_VALUES_FILE'} -f $values_file";
            } elsif ($filename =~ /^(.+eric-sc-cs-.+(\.tar\.gz|\.tgz))$/ && $::JOB_PARAMS{'SC_CS_VALUES_FILE'} ne "") {
                # Prepend the special values file
                $values_file = "$::JOB_PARAMS{'SC_CS_VALUES_FILE'} -f $values_file";
            }

            if (exists $::JOB_PARAMS{'RECORDING_RULES_VALUES_FILE'} && $::JOB_PARAMS{'RECORDING_RULES_VALUES_FILE'} ne "") {
                $values_file .= " -f $::JOB_PARAMS{'RECORDING_RULES_VALUES_FILE'}";
            }

            # Check if we have any extra eric-sc-values-x files to include
            $values_file_extra = "";
            for my $job_parameter (General::String_Operations::sort_hash_keys_alphanumerically(\%::JOB_PARAMS)) {
                if ($job_parameter =~ /^CONFIG_FILE_HELM_CHART_(\d+)$/) {
                    $values_file_extra .= " -f $values_file_path/eric-sc-values-$1.yaml";
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

            #
            # Save the command and message for later execution
            #

            push @commands, "${debug_command}${helm_executable} upgrade --install $release_name $filename --namespace $namespace -f $values_file $values_file_extra $extra_parameters --timeout ${timeout}s --debug";

            if ($upgrade_helm_chart == 1) {
                push @commands_message, "    Upgrading Application release name '$release_name' in namespace '$namespace'.\n    This will take a while to complete.\n";
                push @commands_action, "Upgrade";
            } else {
                push @commands_message, "    Deploying Application release name '$release_name' in namespace '$namespace'.\n    This will take a while to complete.\n";
                push @commands_action, "Install";
            }

            push @commands_filename, File::Basename::basename($filename);
            $max_length_filename = length(File::Basename::basename($filename)) if (length(File::Basename::basename($filename)) > $max_length_filename);

            push @commands_namespace, $namespace;
            $max_length_namespace = length($namespace) if (length($namespace) > $max_length_namespace);

            push @commands_release, $release_name;
            $max_length_release = length($release_name) if (length($release_name) > $max_length_release);
        }

        #
        # Deploy or Upgrade the application
        #

        # Print a message box with information about the software package.
        if ($upgrade_needed > 0) {
            # An Upgrade
            $header_text = sprintf "Upgrading %s Application", uc($::JOB_PARAMS{'APPLICATION_TYPE'});

            push @messages, "";
            push @messages, "Old software before upgrade:";
            push @messages, sprintf "%-32s  %-10s  %s", ("Name", "Version", "Build");
            push @messages, sprintf "%-32s  %-10s  %s", ("----", "-------", "-----");
            push @messages, sprintf "%-32s  %-10s  %s", ("Application Release", $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}, $::JOB_PARAMS{'OLD_SC_RELEASE_BUILD'}) if ($::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} ne "");
            push @messages, sprintf "%-32s  %-10s  %s", ("Cloud Native Base Chart", $::JOB_PARAMS{'OLD_CLOUD_NATIVE_BASE_RELEASE_VERSION'}, $::JOB_PARAMS{'OLD_CLOUD_NATIVE_BASE_RELEASE_BUILD'}) if ($::JOB_PARAMS{'OLD_CLOUD_NATIVE_BASE_RELEASE_VERSION'} ne "");
            push @messages, sprintf "%-32s  %-10s  %s", ("Cloud Native NF Additions Chart", $::JOB_PARAMS{'OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION'}, $::JOB_PARAMS{'OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_BUILD'}) if ($::JOB_PARAMS{'OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION'} ne "");
            push @messages, sprintf "%-32s  %-10s  %s", ("SC BSF Chart", $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_VERSION'}, $::JOB_PARAMS{'OLD_SC_BSF_RELEASE_BUILD'}) if ($::JOB_PARAMS{'OLD_SC_BSF_RELEASE_VERSION'} ne "");
            push @messages, sprintf "%-32s  %-10s  %s", ("SC CS Chart", $::JOB_PARAMS{'OLD_SC_CS_RELEASE_VERSION'}, $::JOB_PARAMS{'OLD_SC_CS_RELEASE_BUILD'}) if ($::JOB_PARAMS{'OLD_SC_CS_RELEASE_VERSION'} ne "");
            push @messages, sprintf "%-32s  %-10s  %s", ("SC DSC Chart", $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_VERSION'}, $::JOB_PARAMS{'OLD_SC_DSC_RELEASE_BUILD'}) if ($::JOB_PARAMS{'OLD_SC_DSC_RELEASE_VERSION'} ne "");
            push @messages, sprintf "%-32s  %-10s  %s", ("SC SCP Chart", $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_VERSION'}, $::JOB_PARAMS{'OLD_SC_SCP_RELEASE_BUILD'}) if ($::JOB_PARAMS{'OLD_SC_SCP_RELEASE_VERSION'} ne "");
            push @messages, sprintf "%-32s  %-10s  %s", ("SC SEPP Chart", $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_VERSION'}, $::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_BUILD'}) if ($::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_VERSION'} ne "");
            push @messages, "";
            push @messages, "New software after upgrade:";

            # Because an upgrade with CNCS requires the order of handling the helm charts needs to be reversed compared with a deployment
            # i.e. instead of base -> additions -> sc-cs -> bsf -> dsc -> scp -> sepp for a deployment
            # it must be      sepp -> scp -> dsc bsf -> -> sc-cs -> additions -> base for an upgrade.
            # So we need to reverce the order.
            @commands = reverse @commands;
            @commands_action = reverse @commands_action;
            @commands_filename = reverse @commands_filename;
            @commands_message = reverse @commands_message;
            @commands_namespace = reverse @commands_namespace;
            @commands_release = reverse @commands_release;
        } else {
            # A Deployment
            $header_text = sprintf "Deploying %s Application", uc($::JOB_PARAMS{'APPLICATION_TYPE'});

            push @messages, "";
            push @messages, "Software to be installed:";
        }
        push @messages, sprintf "%-32s  %-10s  %s", ("Name", "Version", "Build");
        push @messages, sprintf "%-32s  %-10s  %s", ("----", "-------", "-----");
        push @messages, sprintf "%-32s  %-10s  %s", ("Application Release", $::JOB_PARAMS{'SC_RELEASE_VERSION'}, $::JOB_PARAMS{'SC_RELEASE_BUILD'}) if ($::JOB_PARAMS{'SC_RELEASE_VERSION'} ne "");
        push @messages, sprintf "%-32s  %-10s  %s", ("Cloud Native Base Chart", $::JOB_PARAMS{'CLOUD_NATIVE_BASE_RELEASE_VERSION'}, $::JOB_PARAMS{'CLOUD_NATIVE_BASE_RELEASE_BUILD'}) if ($::JOB_PARAMS{'CLOUD_NATIVE_BASE_RELEASE_VERSION'} ne "");
        push @messages, sprintf "%-32s  %-10s  %s", ("Cloud Native NF Additions Chart", $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION'}, $::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_RELEASE_BUILD'}) if ($::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_RELEASE_VERSION'} ne "");
        push @messages, sprintf "%-32s  %-10s  %s", ("SC BSF Chart", $::JOB_PARAMS{'SC_BSF_RELEASE_VERSION'}, $::JOB_PARAMS{'SC_BSF_RELEASE_BUILD'}) if ($::JOB_PARAMS{'SC_BSF_RELEASE_VERSION'} ne "" && $::JOB_PARAMS{'nodetype'} =~ /bsf/);
        push @messages, sprintf "%-32s  %-10s  %s", ("SC CS Chart", $::JOB_PARAMS{'SC_CS_RELEASE_VERSION'}, $::JOB_PARAMS{'SC_CS_RELEASE_BUILD'}) if ($::JOB_PARAMS{'SC_CS_RELEASE_VERSION'} ne "");
        if ($::JOB_PARAMS{'APPLICATION_TYPE'} eq "dsc" && version->parse( $::JOB_PARAMS{'SC_RELEASE_VERSION'} ) < version->parse( "1.15.0" ) ) {
            push @messages, sprintf "%-32s  %-10s  %s", ("SC DSC Chart", $::JOB_PARAMS{'SC_DSC_RELEASE_VERSION'}, $::JOB_PARAMS{'SC_DSC_RELEASE_BUILD'}) if ($::JOB_PARAMS{'SC_DSC_RELEASE_VERSION'} ne "");
        } else {
            push @messages, sprintf "%-32s  %-10s  %s", ("SC DSC Chart", $::JOB_PARAMS{'SC_DSC_RELEASE_VERSION'}, $::JOB_PARAMS{'SC_DSC_RELEASE_BUILD'}) if ($::JOB_PARAMS{'SC_DSC_RELEASE_VERSION'} ne "" && $::JOB_PARAMS{'nodetype'} =~ /dsc/);
        }
        push @messages, sprintf "%-32s  %-10s  %s", ("SC SCP Chart", $::JOB_PARAMS{'SC_SCP_RELEASE_VERSION'}, $::JOB_PARAMS{'SC_SCP_RELEASE_BUILD'}) if ($::JOB_PARAMS{'SC_SCP_RELEASE_VERSION'} ne "" && $::JOB_PARAMS{'nodetype'} =~ /scp/);
        push @messages, sprintf "%-32s  %-10s  %s", ("SC SEPP Chart", $::JOB_PARAMS{'SC_SEPP_RELEASE_VERSION'}, $::JOB_PARAMS{'SC_SEPP_RELEASE_BUILD'}) if ($::JOB_PARAMS{'SC_SEPP_RELEASE_VERSION'} ne "" && $::JOB_PARAMS{'nodetype'} =~ /sepp/);
        if (exists $::JOB_PARAMS{'nodetype'}) {
            push @messages, "";
            push @messages, "Network Functions and Features (nodetype):";
            push @messages, "$::JOB_PARAMS{'nodetype'}";
        }
        push @messages, "";
        General::Message_Generation::print_message_box(
            {
                "header-text"   => $header_text,
                "messages"      => \@messages,
                "align-text"    => "left",
                "max-length"    => 0,
                "return-output" => \$message,
            }
        );
        General::Logging::log_user_message($message);

        if (@commands) {
            @messages = ();
            push @messages, "The following helm charts will be Installed or Upgraded:";
            push @messages, sprintf "%s  %-7s  %-${max_length_namespace}s  %-${max_length_release}s  %-${max_length_filename}s", "No", "Action", "Namespace", "Release Name", "File Name";
            push @messages, sprintf "%s  %s  %s  %s  %s", "--", "-"x7, "-"x$max_length_namespace, "-"x$max_length_release, "-"x$max_length_filename;
            for (my $i = 0; $i <= $#commands; $i++) {
                push @messages, sprintf "%2d  %-7s  %-${max_length_namespace}s  %-${max_length_release}s  %-${max_length_filename}s",
                    $i+1,
                    $commands_action[$i],
                    $commands_namespace[$i],
                    $commands_release[$i],
                    $commands_filename[$i];
            }
            General::Logging::log_user_message( join "\n", @messages );
        }

        if ($upgrade_needed > 0) {
            if ($upgrade_needed == scalar @commands) {
                # All helm charts needs to be upgraded
                General::Logging::log_user_message("Upgrading Software:\n");
            } else {
                # Some helm charts needs to be installed and some upgraded
                General::Logging::log_user_message("Installing and Upgrading Software:\n");
            }
        } else {
            General::Logging::log_user_message("Installing Software:\n");
        }

        # Now execute the commands
        for (my $i = 0; $i <= $#commands; $i++) {

            General::Logging::log_user_message( sprintf "  Processing helm chart %i of %i\n%s", $i+1, $#commands+1, $commands_message[$i] );

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => $commands[$i],
                    "hide-output"   => 1,
                }
            );

            if ($rc == 0) {
                $::JOB_PARAMS{'DEPLOYED_UPGRADED_HELM_RELEASES'} .= "$commands_release[$i]|";

                if ($::JOB_PARAMS{'CHECK_DEPLOYMENT_STATUS_AFTER_EACH_HELM_OPERATION'} eq "yes") {
                    General::State_Machine::always_execute_task("Playlist::928_Verify_Deployment.+");

                    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::928_Verify_Deployment::main } );
                    return $rc if $rc < 0;

                    if ($rc != 0) {
                        General::Logging::log_user_error_message("    Failed to deploy/upgrade Application for release name $commands_release[$i] ");
                        push @::JOB_STATUS, "(x) Installing/Upgrading Application for release name $commands_release[$i] failed";
                        return $rc;
                    }
                }

                push @::JOB_STATUS, "(/) Installing/Upgrading Application for release name $commands_release[$i] successful";
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("    Failed to deploy/upgrade Application for release name $commands_release[$i] ");
                push @::JOB_STATUS, "(x) Installing/Upgrading Application for release name $commands_release[$i] failed";
                return $rc;
            }
        }
        $::JOB_PARAMS{'DEPLOYED_UPGRADED_HELM_RELEASES'} =~ s/\|$//;

        return $rc;
    }
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P935S99 {

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

This playlist performs a deploy or upgrade of the Application software on the
specific namespace of the node.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

1;
