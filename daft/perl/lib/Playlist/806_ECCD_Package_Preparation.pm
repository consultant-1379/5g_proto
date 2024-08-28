package Playlist::806_ECCD_Package_Preparation;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.4
#  Date     : 2024-03-07 14:48:22
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
    usage
    usage_return_playlist_variables
    );

use General::File_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;
use Playlist::903_Global_Preprocess;
use Playlist::904_Global_Postprocess;

#
# Variable Declarations
#
my %playlist_variables;
set_playlist_variables();

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
#  1: The call failed
#  255: Stepout wanted
#
# -----------------------------------------------------------------------------
sub main {
    my $rc;

    # Set Job Type
    $::JOB_PARAMS{'JOBTYPE'} = "ECCD_PACKAGE_PREPARATION";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # If the playlist needs to have access to Network Configuration parameters
    # then it needs to check for this and give an error or ask the user to
    # provide the file name.
    if (General::Playlist_Operations::is_mandatory_network_config_loaded() != 0) {
        $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P806S01, \&Fallback001_P806S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P806S02, \&Fallback001_P806S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Create_ECCD_Package_P806S03, \&Fallback001_P806S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P806S04, \&Fallback001_P806S99 );
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
sub Initialize_Job_Environment_P806S01 {

    my $rc;

    # This playlist does not use docker, kubectl or helm commands so skip these checks
    $::JOB_PARAMS{'SKIP_DOCKER_AND_NERDCTL_CHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_HELM_CHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_KUBECTL_CHECK'} = "yes";

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::901_Initialize_Job_Environment::main } );
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
sub Check_Job_Parameters_P806S02 {

        my $openstack_host_address = "";
        my $openstack_user = "";
        my $openstack_password = "";
        my $rc = 0;

        # Get the proper OPENSTACK_HOST_ADDRESS
        General::Logging::log_user_message("Checking Job parameter 'OPENSTACK_HOST_ADDRESS'");
        if ($::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'} ne "") {
            $openstack_host_address = $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'eccd_openstack_host_address'} && exists $::NETWORK_CONFIG_PARAMS{'eccd_openstack_host_address'}{'value'} && $::NETWORK_CONFIG_PARAMS{'eccd_openstack_host_address'}{'value'} ne "CHANGEME") {
            $openstack_host_address = $::NETWORK_CONFIG_PARAMS{'eccd_openstack_host_address'}{'value'};
            $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'} = $openstack_host_address;
        } else {
            General::Logging::log_user_error_message("Job parameter 'OPENSTACK_HOST_ADDRESS' has not been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper OPENSTACK_USER
        General::Logging::log_user_message("Checking Job parameter 'OPENSTACK_USER'");
        if ($::JOB_PARAMS{'OPENSTACK_USER'} ne "") {
            $openstack_user = $::JOB_PARAMS{'OPENSTACK_USER'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'eccd_openstack_user'} && exists $::NETWORK_CONFIG_PARAMS{'eccd_openstack_user'}{'value'} && $::NETWORK_CONFIG_PARAMS{'eccd_openstack_user'}{'value'} ne "CHANGEME") {
            $openstack_user = $::NETWORK_CONFIG_PARAMS{'eccd_openstack_user'}{'value'};
            $::JOB_PARAMS{'OPENSTACK_USER'} = $openstack_user;
        } else {
            General::Logging::log_user_error_message("Job parameter 'OPENSTACK_USER' has not been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Get the proper OPENSTACK_PASSWORD
        General::Logging::log_user_message("Checking Job parameter 'OPENSTACK_PASSWORD'");
        if ($::JOB_PARAMS{'OPENSTACK_PASSWORD'} ne "") {
            $openstack_password = $::JOB_PARAMS{'OPENSTACK_PASSWORD'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'eccd_openstack_password'} && exists $::NETWORK_CONFIG_PARAMS{'eccd_openstack_password'}{'value'} && $::NETWORK_CONFIG_PARAMS{'eccd_openstack_password'}{'value'} ne "CHANGEME") {
            $openstack_password = $::NETWORK_CONFIG_PARAMS{'eccd_openstack_password'}{'value'};
            $::JOB_PARAMS{'OPENSTACK_PASSWORD'} = $openstack_password;
        } else {
            General::Logging::log_user_error_message("Job parameter 'OPENSTACK_PASSWORD' has not been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        if ($::JOB_PARAMS{'ECCD_ENVIRONMENT_FILE'} =~ /^(\/.+)\/.+$/) {
            $::JOB_PARAMS{'ECCD_ENVIRONMENT_FILE_DIR'} = $1;
        } else {
            General::Logging::log_user_error_message("Job parameter 'ECCD_ENVIRONMENT_FILE' does not contain a full path that starts with /");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Remove any trailing / on directory path
        $::JOB_PARAMS{'ECCD_SOFTWARE_INSTALL_PATH'} =~ s/\/$//;

        General::Logging::log_user_message(
            sprintf "%s\n%s\n%s\n%s\n%s\n",     "ECCD_ENVIRONMENT_FILE_DIR=$::JOB_PARAMS{'ECCD_ENVIRONMENT_FILE_DIR'}",
                                                "ECCD_ENVIRONMENT_FILE=$::JOB_PARAMS{'ECCD_ENVIRONMENT_FILE'}",
                                                "ECCD_SOFTWARE_INSTALL_PATH=$::JOB_PARAMS{'ECCD_SOFTWARE_INSTALL_PATH'}",
                                                "OPENSTACK_HOST_ADDRESS=$openstack_host_address",
                                                "OPENSTACK_USER=$openstack_user"
        );

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
sub Create_ECCD_Package_P806S03 {

    my $rc;

    # - Check if there is enough free disk space
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Available_Disk_Space_On_Openstack_Server_P806S03T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Fetch_And_Unpack_Software_File_P806S03T02 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Environment_File_P806S03T03 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Upload_Environment_File_To_Openstack_Server_P806S03T04 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Images_On_Openstack_Server_P806S03T05 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Update_Status_Messages_P806S03T06 } );
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
    sub Check_Available_Disk_Space_On_Openstack_Server_P806S03T01 {
        my $available_eccd_environment_file_dir = "";
        my $available_eccd_software_install_path = "";
        my $available_space_eccd_environment_file_dir = "";
        my $available_space_eccd_software_install_path = "";
        my $eccd_environment_file_dir = $::JOB_PARAMS{'ECCD_ENVIRONMENT_FILE_DIR'};
        my $eccd_software_install_path = $::JOB_PARAMS{'ECCD_SOFTWARE_INSTALL_PATH'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my $min_unit = $::JOB_PARAMS{'MINIMUM_FREE_DISK_SPACE'};
        my $min_value = $::JOB_PARAMS{'MINIMUM_FREE_DISK_SPACE'};
        my $openstack_host_address = $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'};
        my $openstack_password = $::JOB_PARAMS{'OPENSTACK_PASSWORD'};
        my $openstack_user = $::JOB_PARAMS{'OPENSTACK_USER'};
        my $rc = 0;
        my @result;

        $min_unit =~ s/^\d+//;

        General::Logging::log_user_message("Check available disk space on Openstack server");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$openstack_host_address " .
                                        "--user=$openstack_user " .
                                        "--password='$openstack_password' " .
                                        "--timeout=$expect_timeout " .
                                        "--command='export PS1=\"DAFT: \"' " .
                                        "--command='mkdir -p $eccd_environment_file_dir' " .
                                        "--command='mkdir -p $eccd_software_install_path' " .
                                        "--command='df -B $min_unit $eccd_environment_file_dir' " .
                                        "--command='df -B $min_unit $eccd_software_install_path' ",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            my $look_for_value = 0;
            for (@result) {
                if (/df -B $min_unit $eccd_environment_file_dir/) {
                    $look_for_value = 1;
                } elsif (/df -B $min_unit $eccd_software_install_path/) {
                    $look_for_value = 2;
                } elsif ($look_for_value > 0 && /^\S+\s+\d+$min_unit\s+\d+$min_unit\s+(\d+)$min_unit\s+\d+\%\s+(\S+)$/) {
                    #Filesystem            1G-blocks  Used Available Use% Mounted on
                    #/dev/mapper/rhel-home      184G   50G      134G  27% /home
                    if ($look_for_value == 1) {
                        $available_space_eccd_environment_file_dir = $1;
                        $available_eccd_environment_file_dir = $2;
                        $look_for_value = 0;
                    } elsif ($look_for_value == 2) {
                        $available_space_eccd_software_install_path = $1;
                        $available_eccd_software_install_path = $2;
                        $look_for_value = 0;
                    }
                }
            }
            if ($available_space_eccd_environment_file_dir eq "" || $available_space_eccd_software_install_path eq "") {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Did not find the available disk space for one or both paths");
                return 1;
            }
            # Check that there is enough space
            if ($available_eccd_environment_file_dir eq $available_eccd_software_install_path) {
                # The path's point to the same place, so we need to have double the min_value
                if ($available_space_eccd_software_install_path < ($min_value * 2)) {
                    General::Logging::log_user_error_message(sprintf "The path '$eccd_software_install_path' have $available_space_eccd_software_install_path$min_unit available, but we need %d%s", ($min_value * 2), $min_unit);
                    return 1;
                }
                # There should be enough space for both the packed and unpacked file
                $::JOB_PARAMS{'ECCD_SOFTWARE_DOWNLOAD_PATH'} = $eccd_software_install_path;
            } else {
                # Different paths
                if ($available_space_eccd_software_install_path < $min_value) {
                    # Not enough space for the unpacked file
                    General::Logging::log_user_error_message("The path '$eccd_software_install_path' have $available_space_eccd_software_install_path$min_unit available, but we need $min_value$min_unit");
                    return 1;
                } elsif ($available_space_eccd_software_install_path >= ($min_value * 2)) {
                    # There is enough space for both files
                    $::JOB_PARAMS{'ECCD_SOFTWARE_DOWNLOAD_PATH'} = $eccd_software_install_path;
                } elsif ($available_space_eccd_environment_file_dir < $min_value) {
                    # Not enough space for the packed file
                    General::Logging::log_user_error_message("The path '$eccd_environment_file_dir' have $available_space_eccd_environment_file_dir$min_unit available, but we need $min_value$min_unit");
                    return 1;
                } else {
                    # The directory have enough space for the packed file
                    $::JOB_PARAMS{'ECCD_SOFTWARE_DOWNLOAD_PATH'} = $eccd_environment_file_dir;
                }
            }

            push @::JOB_STATUS, '(/) Check available disk space on Openstack server was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to check available disk space on Openstack server");
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
    sub Fetch_And_Unpack_Software_File_P806S03T02 {
        my $openstack_host_address = $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'};
        my $openstack_user = $::JOB_PARAMS{'OPENSTACK_USER'};
        my $openstack_password = $::JOB_PARAMS{'OPENSTACK_PASSWORD'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my $eccd_software_download_path = $::JOB_PARAMS{'ECCD_SOFTWARE_DOWNLOAD_PATH'};
        my $eccd_software_install_path = $::JOB_PARAMS{'ECCD_SOFTWARE_INSTALL_PATH'};
        my $eccd_software_url = $::JOB_PARAMS{'ECCD_SOFTWARE_URL'};
        my $eccd_software_filename = $::JOB_PARAMS{'ECCD_SOFTWARE_URL'};
        my $rc = 0;
        my @result;

        $eccd_software_filename =~ s/^.+\///;   # Remove the path and only leave the last filename part

        General::Logging::log_user_message("Fetching and unpacking $eccd_software_url file.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$openstack_host_address " .
                                        "--user=$openstack_user " .
                                        "--password='$openstack_password' " .
                                        "--timeout=$expect_timeout " .
                                        "--stop-on-error " .
                                        "--command='export PS1=\"DAFT: \"' " .
                                        "--command='cd $eccd_software_download_path' " .
                                        "--command='wget $eccd_software_url' " .
                                        "--command='cd $eccd_software_install_path' " .
                                        "--command='tar xvf $eccd_software_download_path/$eccd_software_filename -C $eccd_software_install_path' " .
                                        "--command='rm -f $eccd_software_download_path/$eccd_software_filename' ",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            # Look for the image and template files
            $::JOB_PARAMS{'ECCD_DIRECTOR_IMAGE'} = "";
            $::JOB_PARAMS{'ECCD_NODE_IMAGE'} = "";
            $::JOB_PARAMS{'ECCD_TEMPLATE_FILE'} = "";

            for (@result) {
                if (/^(.+\/images)\/(eccd-.+-director-image\.qcow2)$/) {
                    #CXP9036305-2.24.0-001462-f952778e/images/eccd-2.24.0-001462-f952778e-director-image.qcow2
                    $::JOB_PARAMS{'ECCD_DIRECTOR_IMAGE_PATH'} = "$eccd_software_install_path/$1";
                    $::JOB_PARAMS{'ECCD_DIRECTOR_IMAGE'} = $2;
                } elsif (/^(.+\/images)\/(eccd-.+-node-image\.qcow2)$/) {
                    #CXP9036305-2.24.0-001462-f952778e/images/eccd-2.24.0-001462-f952778e-node-image.qcow2
                    $::JOB_PARAMS{'ECCD_NODE_IMAGE_PATH'} = "$eccd_software_install_path/$1";
                    $::JOB_PARAMS{'ECCD_NODE_IMAGE'} = $2;
                } elsif (/^.+\/templates\/eccd\.yaml$/) {
                    #CXP9036305-2.24.0-001462-f952778e/templates/eccd.yaml
                    $::JOB_PARAMS{'ECCD_TEMPLATE_FILE'} = "$eccd_software_install_path/$_";
                }
            }
            if ($::JOB_PARAMS{'ECCD_DIRECTOR_IMAGE'} ne "" && $::JOB_PARAMS{'ECCD_NODE_IMAGE'} ne "" && $::JOB_PARAMS{'ECCD_TEMPLATE_FILE'} ne "") {
                General::Logging::log_user_message(
                    sprintf "%s\n%s\n%s\n", "ECCD_DIRECTOR_IMAGE: $::JOB_PARAMS{'ECCD_DIRECTOR_IMAGE'}",
                                            "ECCD_NODE_IMAGE:     $::JOB_PARAMS{'ECCD_NODE_IMAGE'}",
                                            "ECCD_TEMPLATE_FILE:  $::JOB_PARAMS{'ECCD_TEMPLATE_FILE'}"
                );
                push @::JOB_STATUS, '(/) Fetching and unpacking of software on Openstack server was successful';
            } else {
                General::Logging::log_user_error_message(
                    sprintf "Did not find the image or template files:\n%s\n%s\n%s\n", "ECCD_DIRECTOR_IMAGE: $::JOB_PARAMS{'ECCD_DIRECTOR_IMAGE'}",
                                                                                       "ECCD_NODE_IMAGE:     $::JOB_PARAMS{'ECCD_NODE_IMAGE'}",
                                                                                       "ECCD_TEMPLATE_FILE:  $::JOB_PARAMS{'ECCD_TEMPLATE_FILE'}"
                );
                $rc = 1;
            }
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to fetch or unpack $eccd_software_url file");
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
    sub Create_Environment_File_P806S03T03 {

        my @data;
        my $output_file = "";
        my $rc = 0;
        my $template_file = $::JOB_PARAMS{'ECCD_ENVIRONMENT_TEMPLATE_FILE'};

        if ($::JOB_PARAMS{'ECCD_ENVIRONMENT_FILE'} =~ /^.+\/(.+)$/) {
            $::JOB_PARAMS{'WORKSPACE_ENVIRONMENT_FILE'} = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/$1";
        }

        General::Logging::log_user_message("Reading template file");
        $rc = General::File_Operations::read_file(
            {
                "filename"              => $template_file,
                "output-ref"            => \@data,
                "hide-error-messages"   => 1,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Unable to open the template file '$template_file'");
            return 1;
        }

        General::Logging::log_user_message("Create a copy of template file in job workspace directory");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "cp -p $template_file $::JOB_PARAMS{'_JOB_CONFIG_DIR'}",
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to copy the template file");
            return 1;
        }

        General::Logging::log_user_message("Creating environment file");
        for (@data) {
            if (/^(\s*master_image:\s*).+/) {
                #  master_image: eccd-2.24.0-001462-f952778e-node-image.qcow2
                $_ = "$1$::JOB_PARAMS{'ECCD_NODE_IMAGE'}";
            } elsif (/^(\s*director_image:\s*).+/) {
                #  director_image: eccd-2.24.0-001462-f952778e-director-image.qcow2
                $_ = "$1$::JOB_PARAMS{'ECCD_DIRECTOR_IMAGE'}";
            } elsif (/^(\s*image:\s*).+/) {
                #    image: eccd-2.24.0-001462-f952778e-node-image.qcow2
                $_ = "$1$::JOB_PARAMS{'ECCD_NODE_IMAGE'}";
            }
        }
        $rc = General::File_Operations::write_file(
            {
                "filename"          => $::JOB_PARAMS{'WORKSPACE_ENVIRONMENT_FILE'},
                "output-ref"        => \@data,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Creation of environment file was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to write environment file");
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
    sub Upload_Environment_File_To_Openstack_Server_P806S03T04 {

        my $eccd_environment_file = $::JOB_PARAMS{'ECCD_ENVIRONMENT_FILE'};
        my $openstack_host_address = $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'};
        my $openstack_user = $::JOB_PARAMS{'OPENSTACK_USER'};
        my $openstack_password = $::JOB_PARAMS{'OPENSTACK_PASSWORD'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my $rc = 0;
        my $workspace_environment_file = $::JOB_PARAMS{'WORKSPACE_ENVIRONMENT_FILE'};

        General::Logging::log_user_message("Copy authorized keys file to openstack");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/scp_files.exp " .
                                        "--from-data='$workspace_environment_file' " .
                                        "--to-data='$openstack_user\@$openstack_host_address:$eccd_environment_file' " .
                                        "--password='$openstack_password' " .
                                        "--timeout-value=$expect_timeout ",
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Copying of environment file to openstack was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to copy environment file to openstack");
            return $rc;
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
    sub Create_Images_On_Openstack_Server_P806S03T05 {
        my $openstack_host_address = $::JOB_PARAMS{'OPENSTACK_HOST_ADDRESS'};
        my $openstack_user = $::JOB_PARAMS{'OPENSTACK_USER'};
        my $openstack_password = $::JOB_PARAMS{'OPENSTACK_PASSWORD'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my $rc = 0;

        General::Logging::log_user_message("Creating director and node images.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                                        "--ip=$openstack_host_address " .
                                        "--user=$openstack_user " .
                                        "--password='$openstack_password' " .
                                        "--timeout=$expect_timeout " .
                                        "--stop-on-error " .
                                        "--command='. SC-adminovercloudrc' " .
                                        "--command='export PS1=\"DAFT: \"' " .
                                        "--command='openstack image create $::JOB_PARAMS{'ECCD_DIRECTOR_IMAGE'} --disk-format qcow2 --file $::JOB_PARAMS{'ECCD_DIRECTOR_IMAGE_PATH'}/$::JOB_PARAMS{'ECCD_DIRECTOR_IMAGE'}--container-format bare --min-disk 40 –public' " .
                                        "--command='openstack image create $::JOB_PARAMS{'ECCD_NODE_IMAGE'} --disk-format qcow2 --file $::JOB_PARAMS{'ECCD_NODE_IMAGE_PATH'}/$::JOB_PARAMS{'ECCD_NODE_IMAGE'}--container-format bare --min-disk 40 –public' ",
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            push @::JOB_STATUS, '(/) Creating director and node images was successful';
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to create director and node images");
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
    sub Update_Status_Messages_P806S03T06 {

        # Write extra status messages to file
        my $rc = General::File_Operations::write_file(
            {
                "filename"          => "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/extra_status_messages.txt",
                "append-file"       => 1,
                "output-ref"        => [
                    "Parameters for ECCD Installation",
                    "================================",
                    "",
                    "You might want to add the following file into the '5g_proto/daft/openstack_env_files/'",
                    "GIT directory structure and commit the changes so it's saved for the future:",
                    "  $::JOB_PARAMS{'WORKSPACE_ENVIRONMENT_FILE'}",
                    "",
                    "Use the following parameters when executing the 802_ECCD_Installation playlist:",
                    "",
                    "  -v ECCD_ENVIRONMENT_FILE=$::JOB_PARAMS{'ECCD_ENVIRONMENT_FILE'}",
                    "  -v ECCD_TEMPLATE_FILE=$::JOB_PARAMS{'ECCD_TEMPLATE_FILE'}",
                    "",
                ],
            }
        );

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
sub Cleanup_Job_Environment_P806S04 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::902_Cleanup_Job_Environment::main } );
    return $rc if $rc < 0;

    if ($rc == 0) {
        # Mark that the Job was successful
        $::JOB_PARAMS{'JOBSTATUS'} = "SUCCESSFUL";
    }

    return $rc;
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P806S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

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
sub set_playlist_variables {
    %playlist_variables = (
        # ---------------------------------------------------------------------
        'ECCD_ENVIRONMENT_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the path to the environment file located on the OpenStack
server that will be created and later be used for installing the ECCD software
on the node.
It can e.g. be something like:
/home/SC-admin/eccd/eevee_2.24.0_DS/env_eccd_eevee_2.24.0.yaml
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'ECCD_ENVIRONMENT_TEMPLATE_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "file",      # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the path to an already existing environment file located
on the local file system that is accessible by the DAFT job.
This file is used as a template for the new file that will be created as specified
by the ECCD_ENVIRONMENT_FILE parameter.
The contents of this template file will be copied over to the new file and only
the software images names will be changed.
It can e.g. be something like:
/path/to/5g_proto/daft/openstack_env_files/eevee/env_eccd_eevee_2.23.0.yaml
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'ECCD_SOFTWARE_INSTALL_PATH' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "/sharedimages/eccd",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the path on the Openstack server where the unpacked
software will be installed.
Please note that this directory must be write enabled by the OPENSTACK_USER and
have enough storage space for the unpacked file and possibly also for the packed
file unless the path where the new environment file have space for the unpacked
file.
It can e.g. be something like:
/sharedimages/eccd
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'ECCD_SOFTWARE_URL' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the URL for where the ECCD software file will be fetched
from and should contain the full URL including the protocol.
The file will be fetched with the 'wget' command which must exist on the Openstack
server and this server must also have access rights to fetch the file from the
remote URL.
It can e.g. be something like:
https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-erikube-generic-local/erikube/releases/2.24.0/CXP9036305/CXP9036305R32A.tgz
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'MINIMUM_FREE_DISK_SPACE' => {
            'case_check'    => "uppercase", # <lowercase|no|uppercase>
            'default_value' => "16G",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the minimum free disk space required to be able to unpack
the software in the directory specified by the ECCD_SOFTWARE_INSTALL_PATH parameter.
The size should be specified as a number followed by one of the following
characters:
    - K,M,G,T,P,E,Z,Y           (powers of 1024)
    - KB,MB,GB,TB,PB,EB,ZB,YB   (powers of 1000)
NOTE: That an unpacked software package might be at least 14G in size and if
the packed file should also be temporrailly stored in the same path then another
9G or more might be needed.
EOF
            'validity_mask' => '\d+(K|M|G|T|P|E|Z|Y|KB|MB|GB|TB|PB|EB|ZB|YB)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'OPENSTACK_HOST_ADDRESS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the host address for the OpenStack server where
installation of ECCD on the node will be executed from.
It can e.g. be something like:
10.221.146.29
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'OPENSTACK_PASSWORD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the user password for the OpenStack server where
installation of ECCD on the node will be executed from.
It can e.g. be something like:
admin
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'OPENSTACK_USER' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter specifies the user name for the OpenStack server where
installation of ECCD on the node will be executed from.
It can e.g. be something like:
SC-admin
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },
    )
}
# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist fetch a new packed ECCD software package from a remote URL and
unpacks it creates the needed images in heat and then creates a new environment
file based on a template.
The fetched packed file will be deleted after successful unpacking.

Used Job Parameters:
====================
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
