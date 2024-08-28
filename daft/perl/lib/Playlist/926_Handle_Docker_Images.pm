package Playlist::926_Handle_Docker_Images;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.15
#  Date     : 2024-04-15 16:30:13
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

use Exporter qw(import);

our @EXPORT_OK = qw(
    main
    usage
    );

# Used Perl package files
use File::Basename qw(dirname basename);
use General::File_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

my $debug_command = "";

# If checking for active docker operations on this cluster set this value to 1
# and to disable the check set it to 0.
my $check_for_ongoing_docker_action = 1;
my $tempfile_template = sprintf "/tmp/DAFT_docker_busy_with_id_%s_pid%i_XXXXXXXX", $::JOB_PARAMS{'SPECIAL_IDENTIFIER'}, $$;

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
    $rc = General::Playlist_Operations::execute_step( \&Load_And_Tag_Docker_Images_P926S01, \&Fallback001_P926S99 );
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
sub Load_And_Tag_Docker_Images_P926S01 {

    my $rc;

    if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_SC'} eq "no" || $::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_CRD'} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Update_Retagger_Script_If_Needed_P926S01T01 } );
        return $rc if $rc < 0;
    }

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Docker_Login_P926S01T02 } );
    return $rc if $rc < 0;

    if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_SC'} eq "no" || $::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_Software_Docker_Images_P926S01T03 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_CRD'} eq "no" && $::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Load_CRDS_Docker_Images_P926S01T04 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_SC'} eq "no" || $::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_CRD'} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&List_Docker_Images_P926S01T05 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_SC'} eq "no" || $::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'} eq "yes") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Tag_Software_Docker_Images_P926S01T06 } );
        return $rc if $rc < 0;
    }

    if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_CRD'} eq "no" && $::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'} eq "no") {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Tag_CRDS_Docker_Images_P926S01T07 } );
        return $rc if $rc < 0;
    }

    if (exists $::JOB_PARAMS{'SKIP_DOCKER_AND_NERDCTL_CHECK'} && $::JOB_PARAMS{'SKIP_DOCKER_AND_NERDCTL_CHECK'} eq "yes") {
        # Nothing to print
    } else {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&List_Docker_Images_P926S01T08 } );
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
    sub Update_Retagger_Script_If_Needed_P926S01T01 {

        my $rc = 0;
        my $replace_docker_with_nerdctl = 0;
        my @temp;

        if ($::JOB_PARAMS{'USING_EVNFM'} eq "yes") {
            General::Logging::log_user_message("EVNFM will handle onboarding of software container images, skipping this task");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        $rc = General::File_Operations::read_file(
            {
                "filename"            => $::JOB_PARAMS{'RETAGGER_SCRIPT'},
                "output-ref"          => \@temp,
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to read file: $::JOB_PARAMS{'RETAGGER_SCRIPT'}");
            push @::JOB_STATUS, "(x) Failed to update retagger script";
            return $rc;
        }

        if ($::JOB_PARAMS{'CONTAINER_COMMAND'} =~ /nerdctl/) {
            # New 'nerdctl' command that replaces 'docker' starting with ECCD 2.26.0
            # which means that in case the retagger.sh script is still using 'docker'
            # command then we need to replace 'docker' with 'nerdctl' in the whole file.
            if (grep /nerdctl/, @temp) {
                # The script is already updated to handle the nerdctl command, so no need to replace 'docker' with 'nerdctl'.
                $replace_docker_with_nerdctl = 0;
            } else {
                # The script does not handle the nerdctl command so we need to replace all instances of 'docker' with 'nerdctl'.
                $replace_docker_with_nerdctl = 1;
            }
        }

        # Go through the array and replace docker with nerdctl and comment out error at 'rmi' operation.
        my $replaced_docker = 0;
        my $replaced_inspect = 0;
        my $replaced_rmi = 0;
        for (my $i = 0; $i <= $#temp; $i++) {
            # Replace 'docker' with 'nerdctl' on all places.
            if ($replace_docker_with_nerdctl == 1 && $temp[$i] =~ /docker/) {
                $temp[$i] =~ s/docker/nerdctl/g;
                $replaced_docker++;
            }

            # Comments out error for the 'inspect' command
            if ($temp[$i] =~ /^\s+sudo .+ inspect \$url > .+/) {
                #sudo $DOCKER_BINARY inspect $url > /dev/null     or     sudo docker inspect $url > /dev/null
                #if [ $? != 0 ]; then
                #    echo ""
                #    echo "The image $url is not found. Exiting..."
                #    exit 1
                #fi

                if ($temp[$i+3] =~ /^(\s+)(echo "The image \$url is not found)\. Exiting\.\.\.\s*$/) {
                    $temp[$i+3] = "$1$2. Error ignored.";
                    $replaced_inspect++;
                }
                if ($temp[$i+4] =~ /^(\s+)(exit 1)\s*$/) {
                    $temp[$i+4] = "$1# $2";
                    $replaced_inspect++;
                }
            }

            # Comments out error for the 'rmi' command
            if ($temp[$i] =~ /^\s+sudo .+ rmi \$url > .+/) {
                #sudo $DOCKER_BINARY rmi $url > /dev/null     or     sudo docker rmi $url > /dev/null
                #
                #if [ $? != 0 ]; then
                #    echo ""
                #    echo "The image $url could not be untagged. Exiting..."
                #    exit 1
                #fi

                if ($temp[$i+4] =~ /^(\s+)(echo "The image \$url could not be) untagged\. Exiting\.\.\.\s*$/) {
                    $temp[$i+4] = "$1$2 removed. Error ignored.";
                    $replaced_rmi++;
                }
                if ($temp[$i+5] =~ /^(\s+)(exit 1)\s*$/) {
                    $temp[$i+5] = "$1# $2";
                    $replaced_rmi++;
                }
            }
        }

        if ($replaced_docker == 0 && $replaced_inspect == 0 && $replaced_rmi == 0) {
            General::Logging::log_user_message("No need to update the $::JOB_PARAMS{'RETAGGER_SCRIPT'} script");
        } else {
            if ($replaced_docker > 0) {
                General::Logging::log_user_message("Replacing 'docker' with 'nerdctl' command in $::JOB_PARAMS{'RETAGGER_SCRIPT'} script");
            }
            if ($replaced_inspect > 0) {
                General::Logging::log_user_message("Updated file to ignore image 'inspect' error in $::JOB_PARAMS{'RETAGGER_SCRIPT'} script");
            }
            if ($replaced_rmi > 0) {
                General::Logging::log_user_message("Updated file to ignore image 'rmi' error in $::JOB_PARAMS{'RETAGGER_SCRIPT'} script");
            }

            General::Logging::log_user_message("Creating a backup copy of $::JOB_PARAMS{'RETAGGER_SCRIPT'} script");
            $rc = General::OS_Operations::send_command(
                {
                    "command"           => "cp -p $::JOB_PARAMS{'RETAGGER_SCRIPT'} $::JOB_PARAMS{'RETAGGER_SCRIPT'}_ORIGINAL",
                    "hide-output"       => 1,
                }
            );
            if ($rc != 0) {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("Failed to copy the script");
                return $rc;
            }

            General::Logging::log_user_message("Writing updated $::JOB_PARAMS{'RETAGGER_SCRIPT'} script");
            $rc = General::File_Operations::write_file(
                {
                    "filename"            => $::JOB_PARAMS{'RETAGGER_SCRIPT'},
                    "output-ref"          => \@temp,
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_error_message("Failed to update the script");
                return $rc;
            }
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
    sub Docker_Login_P926S01T02 {

        my $container_cmd = $::JOB_PARAMS{'CONTAINER_COMMAND'};
        my $rc = 0;
        my $registry_password = $::JOB_PARAMS{'SC_REGISTRY_PASSWORD'};
        my $registry_port = $::JOB_PARAMS{'SC_REGISTRY_PORT'};
        my $registry_url = $::JOB_PARAMS{'SC_REGISTRY_URL'};
        my $registry_user = $::JOB_PARAMS{'SC_REGISTRY_USER'};
        my $url = "$registry_url:$registry_port";

        if ($::JOB_PARAMS{'USING_EVNFM'} eq "yes") {
            # Special handling for EVNFM and depending on the software release because of the
            # way the CRDS are delivered.
            if ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_SC'} eq "yes" && $::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_CRD'} eq "yes") {
                # Since EVNFM does not want DAFT to do anything with the SC and CRD software there is no need to work with docker or nerdctl.
                # Skip all other tasks in this step.
                General::Logging::log_user_message("EVNFM will handle all onboarding, skip remaining tasks in this step");
                return General::Playlist_Operations::RC_STEPOUT;
            } elsif ($::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'} eq "no") {
                # Special handling for software release 1.2.2 and 1.3.0 where the CRDS was delivered
                # in the tools package which require DAFT to load the CRD images to the local registry
                # because EVNFM cannot handle onboarding of CRDS.
                General::Logging::log_user_message("EVNFM cannot install CRDS delivered in the tools package, so we load the images to special registry");
                $registry_password = $::JOB_PARAMS{'CRD_REGISTRY_PASSWORD'};
                $registry_port = $::JOB_PARAMS{'CRD_REGISTRY_PORT'};
                $registry_url = $::JOB_PARAMS{'CRD_REGISTRY_URL'};
                $registry_user = $::JOB_PARAMS{'CRD_REGISTRY_USER'};
            } elsif ($::JOB_PARAMS{'SC_RELEASE_VERSION'} =~ /^(1\.3\.1|1\.4\.\d+)/) {
                # Another special case for EVNFM, for SC release 1.3.1 and 1.4.x even though the CRDS are included
                # in the software CSAR file EVNFM still cannot handle the installations of the CRDS, it
                # will only onboard the images into it's own registry but DAFT must install the CRDS.
                $registry_password = $::JOB_PARAMS{'CRD_REGISTRY_PASSWORD'};
                $registry_port = $::JOB_PARAMS{'CRD_REGISTRY_PORT'};
                $registry_url = $::JOB_PARAMS{'CRD_REGISTRY_URL'};
                $registry_user = $::JOB_PARAMS{'CRD_REGISTRY_USER'};
            } else {
                # CRDS are delivered in the software CSAR file and EVNFM will onboard and
                # install them by itself and DAFT does not need to handle any container images.
                # Skip all other tasks in this step.
                General::Logging::log_user_message("EVNFM will handle onboarding of all images, skip remaining tasks in this step");
                return General::Playlist_Operations::RC_STEPOUT;
            }
        }

        if ($registry_url eq "") {
            General::Logging::log_user_message("No need to login since no own registry exists");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        if ($registry_user eq "CHANGEME") {
            General::Logging::log_user_warning_message("No user specified for own registry, cannot do '$container_cmd login'");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        if ($registry_password eq "CHANGEME") {
            General::Logging::log_user_warning_message("No password specified for own registry, cannot do '$container_cmd login'");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        if ($container_cmd =~ /nerdctl/ && $registry_port == 443) {
            # We need to remove the port number because it seems the nerdctl command to push images does not like to have :443 included in the url
            $url = $registry_url;
        } elsif ($container_cmd =~ /docker/ && $registry_port == 443) {
            # We need to remove the port number because it seems the docker command to push images does not like to have :443 included in the url on some nodes.
            $url = $registry_url;
        }

        General::Logging::log_user_message("Performing '$container_cmd login'");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$container_cmd login --username $registry_user --password '$registry_password' $url",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) $container_cmd login successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to perform '$container_cmd login'");
            push @::JOB_STATUS, "(x) $container_cmd login failed";
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
    sub Load_Software_Docker_Images_P926S01T03 {

        my $rc = 0;
        my $container_cmd = $::JOB_PARAMS{'CONTAINER_COMMAND'};
        my $docker_file = "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software/Files/images/docker.tar";
        my $tempfile = "";

        if ($::JOB_PARAMS{'USING_EVNFM'} eq "yes") {
            General::Logging::log_user_message("EVNFM will handle onboarding of software container images, skipping this task");
            return General::Playlist_Operations::RC_TASKOUT;
        } elsif ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_SC'} eq "yes" && $::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'} eq "yes" && $::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_CRD'} eq "yes") {
            General::Logging::log_user_message("CRDS are in software package but SKIP_DEPLOY_UPGRADE_CRD=yes, skipping this task");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Check if we are allowed to continue or have to wait
        if ($check_for_ongoing_docker_action == 1 && wait_for_docker_idle($tempfile_template) == 1) {
            # Create a temporary file to block other deployments or upgrades while this task is ongoing.
            $tempfile = General::File_Operations::tempfile_create($tempfile_template);
        }

        General::Logging::log_user_message("Loading software container images.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$container_cmd load --input $docker_file",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Loading Docker Software Images successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to load container images");
            push @::JOB_STATUS, "(x) Loading Docker Images failed";
        }

        if ($tempfile ne "") {
            # Delete the temporary file
            General::File_Operations::tempfile_delete($tempfile);
        }

        return $rc;
    }

    # -----------------------------------------------------------------------------
    # This subroutine is only called for very old SC packages where the CRDs were
    # included in the tools package. For newer SC releases the CRDs are included
    # in the CSAR package and there is no need to specifically load the CRDs into
    # docker or nerdctl.
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
    sub Load_CRDS_Docker_Images_P926S01T04 {

        my $crds_csar_file = $::JOB_PARAMS{'CRDS_CSAR_FILE'};
        my $container_cmd = $::JOB_PARAMS{'CONTAINER_COMMAND'};
        my $docker_file = "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/tools/Files/images/docker.tar";
        my $rc = 0;
        my $tempfile = "";

        if ($crds_csar_file eq "") {
            # We are using the package format where the CRDS were included in the software CSAR package
            # so there is no special CRDS CSAR file to load container images from.
            General::Logging::log_user_message("CRDS included in Software CSAR package, Skipping this task");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Check if we are allowed to continue or have to wait
        if ($check_for_ongoing_docker_action == 1 && wait_for_docker_idle($tempfile_template) == 1) {
            # Create a temporary file to block other deployments or upgrades while this task is ongoing.
            $tempfile = General::File_Operations::tempfile_create($tempfile_template);
        }

        General::Logging::log_user_message("Loading CRDS container images.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}$container_cmd load --input $docker_file",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Loading Docker CRD Images successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to load container images");
            push @::JOB_STATUS, "(x) Loading Docker CRD Images failed";
        }

        if ($tempfile ne "") {
            # Delete the temporary file
            General::File_Operations::tempfile_delete($tempfile);
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
    sub List_Docker_Images_P926S01T05 {

        my $container_cmd = $::JOB_PARAMS{'CONTAINER_COMMAND'};
        my $rc = 0;

        General::Logging::log_user_message("Listing container images before retagging");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$container_cmd images",
                "hide-output"   => 1,
                "save-to-file"  => "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/docker_images_before_retagging.txt",
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to list container images");
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
    sub Tag_Software_Docker_Images_P926S01T06 {

        my $images_txt_file = "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/software/Files/images.txt";
        my $rc = 0;
        my $registry_url = $::JOB_PARAMS{'SC_REGISTRY_URL'};
        my $registry_port = $::JOB_PARAMS{'SC_REGISTRY_PORT'};
        my $retagger = $::JOB_PARAMS{'RETAGGER_SCRIPT'};    # The retagger.sh script always calls 'docker' with 'sudo docker'
        my $tempfile = "";
        my $url = "$registry_url:$registry_port";

        if ($::JOB_PARAMS{'USING_EVNFM'} eq "yes") {
            General::Logging::log_user_message("EVNFM will handle onboarding of software container images, skipping this task");
            return General::Playlist_Operations::RC_TASKOUT;
        } elsif ($::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_SC'} eq "yes" && $::JOB_PARAMS{'CRDS_IN_SOFTWARE_CSAR_FILE'} eq "yes" && $::JOB_PARAMS{'SKIP_DEPLOY_UPGRADE_CRD'} eq "yes") {
            General::Logging::log_user_message("CRDS are in software package but SKIP_DEPLOY_UPGRADE_CRD=yes, skipping this task");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        if ($registry_url eq "") {
            General::Logging::log_user_message("No need to retag container images since no own registry exists");
            return 0;
        }

        # Check if we are allowed to continue or have to wait
        if ($check_for_ongoing_docker_action == 1 && wait_for_docker_idle($tempfile_template) == 1) {
            # Create a temporary file to block other deployments or upgrades while this task is ongoing.
            $tempfile = General::File_Operations::tempfile_create($tempfile_template);
        }

        if ($::JOB_PARAMS{'CONTAINER_COMMAND'} =~ /nerdctl/ && $registry_port == 443) {
            # We need to remove the port number because it seems the nerdctl command to push images does not like to have :443 included in the url
            $url = $registry_url;
        } elsif ($::JOB_PARAMS{'CONTAINER_COMMAND'} =~ /docker/ && $registry_port == 443) {
            # We need to remove the port number because it seems the docker command to push images does not like to have :443 included in the url on some nodes.
            $url = $registry_url;
        }

        General::Logging::log_user_message("Retagging software container images with registry URL $url.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}bash $retagger $url $images_txt_file",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Tagging Docker Software Images successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to retag container images");
            push @::JOB_STATUS, "(x) Tagging Docker Software Images failed";
        }

        if ($tempfile ne "") {
            # Delete the temporary file
            General::File_Operations::tempfile_delete($tempfile);
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
    sub Tag_CRDS_Docker_Images_P926S01T07 {

        my $crds_csar_file = $::JOB_PARAMS{'CRDS_CSAR_FILE'};
        my $images_txt_file = "$::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/tools/Files/images.txt";
        my $rc = 0;
        my $registry_url = $::JOB_PARAMS{'CRD_REGISTRY_URL'};
        my $registry_port = $::JOB_PARAMS{'CRD_REGISTRY_PORT'};
        my $retagger = $::JOB_PARAMS{'RETAGGER_SCRIPT'};    # The retagger.sh script always calls 'docker' with 'sudo docker'
        my $tempfile = "";
        my $url = "$registry_url:$registry_port";

        if ($registry_url eq "") {
            General::Logging::log_user_message("No need to retag container images since no own registry exists");
            return 0;
        }

        if ($crds_csar_file eq "") {
            # We are using the old package format where the CRDS were included in the software CSAR package
            # so there is no special CRDS CSAR file to load container images from.
            General::Logging::log_user_message("CRDS included in Software CSAR package, Skipping this task");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        # Check if we are allowed to continue or have to wait
        if ($check_for_ongoing_docker_action == 1 && wait_for_docker_idle($tempfile_template) == 1) {
            # Create a temporary file to block other deployments or upgrades while this task is ongoing.
            $tempfile = General::File_Operations::tempfile_create($tempfile_template);
        }

        if ($::JOB_PARAMS{'CONTAINER_COMMAND'} =~ /nerdctl/ && $registry_port == 443) {
            # We need to remove the port number because it seems the nerdctl command to push images does not like to have :443 included in the url
            $url = $registry_url;
        } elsif ($::JOB_PARAMS{'CONTAINER_COMMAND'} =~ /docker/ && $registry_port == 443) {
            # We need to remove the port number because it seems the docker command to push images does not like to have :443 included in the url on some nodes.
            $url = $registry_url;
        }

        General::Logging::log_user_message("Retagging CRDS container images with registry URL $registry_url:$registry_port.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}bash $retagger $url $images_txt_file",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            push @::JOB_STATUS, "(/) Tagging Docker CRD Images successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to retag container images");
            push @::JOB_STATUS, "(x) Tagging Docker CRD Images failed";
        }

        if ($tempfile ne "") {
            # Delete the temporary file
            General::File_Operations::tempfile_delete($tempfile);
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
    sub List_Docker_Images_P926S01T08 {

        my $container_cmd = $::JOB_PARAMS{'CONTAINER_COMMAND'};
        my $rc = 0;

        $::JOB_PARAMS{'DOCKER_IMAGES_FILE'} = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/docker_images_after_retagging.txt";

        General::Logging::log_user_message("Listing container images after retagging");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "$container_cmd images",
                "hide-output"   => 1,
                "save-to-file"  => $::JOB_PARAMS{'DOCKER_IMAGES_FILE'},
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to list container images");
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
sub Fallback001_P926S99 {

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

This playlist loads and tag the container images.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.
EOF
    General::Playlist_Operations::print_used_job_variables(__FILE__, undef);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);
}

# -----------------------------------------------------------------------------
# Checks if some docker operation is currently ongoing by some other DAFT job
# by checking for the presense of a certain temporary file in a specific
# directory.
# If the file is present then this sub routine will block waiting for the
# file to be removed before returning to the caller.
sub wait_for_docker_idle {
    my $template = shift;
    my $ctrl_c_pressed = 0;
    my $directory;
    my @files;
    my $key;
    my $maxtime = time() + 3600;    # Max 1 hour wait

    # Set default value if no template was given
    unless ($template) {
        General::Logging::log_user_warning_message("No template specified when calling 'wait_for_docker_idle', we continue and hope for the best.");
        return 0;
    }

    if ($template =~ /^(.+\/)(\S+)$/) {
        $directory = $1;
        $template = $2;
    } else {
        $directory = ".";
    }

    # Remove the random part from the template, i.e. id_eiffelesc_pid1234_XXX... etc.
    # and replacing it with a star '*', i.e. id_*pid*_*.
    $template =~ s/id_\S*_pid\d+_X{3,}/id_*_pid*_*/;

    while (1) {
        # Look for files matching the template
        @files = General::File_Operations::find_file( { "directory" => $directory, "filename" => $template, "maxdepth" => 1 } );
        if (@files) {
            General::Logging::log_user_message("Some other DAFT job seems to be doing something with docker because the following file exists:\n" . (join "\n", @files) . "\n\nExecution of this task will wait until this file has been removed by that DAFT process.\nA new check will be done every 15 seconds for a maximum time of 1 hour.\nPress CTRL-C to interrupt this delay and check.\n");
            $ctrl_c_pressed = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "progress-message"  => 0,
                    "seconds"           => 15,
                }
            );
            if ($ctrl_c_pressed == 1) {
                General::Logging::log_user_warning_message("The wait was interrupted, are you sure you want to stop waiting?\nConfirm by typing 'y'.");
                $key = General::OS_Operations::get_user_keypress();
                if ($key =~ /^[yY]$/) {
                    # We passivate the check function so we don't end up in another wait for the next task using docker.
                    $check_for_ongoing_docker_action = 0;
                    last;
                }
            }
        } else {
            # No files present, exit now
            last;
        }
        if (time() >= $maxtime) {
            General::Logging::log_user_warning_message("Maximum wait time of 1 hour has expired, we continue and hope for the best.");
            # We passivate the check function so we don't end up in another wait for the next task using docker.
            $check_for_ongoing_docker_action = 0;
            last;
        }
    }

    return 1;
}

1;
