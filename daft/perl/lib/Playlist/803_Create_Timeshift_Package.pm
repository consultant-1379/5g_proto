package Playlist::803_Create_Timeshift_Package;

################################################################################
#
#  Author   : everhel, eustone
#
#  Revision : 1.9
#  Date     : 2024-05-21 11:02:03
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
    usage_return_playlist_variables
    );

use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::901_Initialize_Job_Environment;
use Playlist::902_Cleanup_Job_Environment;

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
    $::JOB_PARAMS{'JOBTYPE'} = "CREATE_TIMESHIFT_PACKAGE";

    # Set job status to ONGOING in case we get an unexpected reboot
    $::JOB_PARAMS{'JOBSTATUS'} = "ONGOING";

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    # Execute the different steps in this playlist
    $rc = General::Playlist_Operations::execute_step( \&Initialize_Job_Environment_P803S01, \&Fallback001_P803S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P803S02, \&Fallback001_P803S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Create_Package_P803S03, \&Fallback001_P803S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Cleanup_Job_Environment_P803S04, \&Fallback001_P803S99 );
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
sub Initialize_Job_Environment_P803S01 {

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
sub Check_Job_Parameters_P803S02 {

        my $package_version = "";
        my $director_ip = "";
        my $rc = 0;
        my @result;

        # Get the proper OPENSTACK_HOST_ADDRESS
        General::Logging::log_user_message("Checking Job parameter 'PACKAGE_VERSION'");
        if ($::JOB_PARAMS{'PACKAGE_VERSION'} eq "") {
            General::Logging::log_user_error_message("Job parameter 'PACKAGE_VERSION' has not been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

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
sub Create_Package_P803S03 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Create_Timeshift_Timebomb_Package_P803S03T01 } );
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
    sub Create_Timeshift_Timebomb_Package_P803S03T01 {

        my $artifact_token = $::JOB_PARAMS{'ARTIFACT_TOKEN'};
        my $director_ip = $::JOB_PARAMS{'DIRECTOR_IP'};
        my $expect_timeout = $::JOB_PARAMS{'EXPECT_DEFAULT_TIMEOUT'};
        my $filename = "$::JOB_PARAMS{'_JOB_CONFIG_DIR'}/create_time_shift_package.bash";
        my $package_version = $::JOB_PARAMS{'PACKAGE_VERSION'};
        my $rc = 0;
        my $timeshift_package = "/home/eccd/download/" . $package_version . "_timeshift";

        $rc = General::File_Operations::write_file(
            {
                "filename"            => $filename,
                 "file-access-mode"   => "755",
                "output-ref"          => [
                    "mkdir -p /home/eccd/daft /home/eccd/download /home/eccd/workspaces",
                    "/home/eccd/bin/download_csar.pl --artifact-token $artifact_token --package $package_version --target /home/eccd/download --noprogress --color=no --link https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/",
                    "cp -pr /home/eccd/download/$package_version $timeshift_package",
                    "cd $timeshift_package/csar",
                    "unzip eric-sc-1.*.csar 'Definitions/OtherTemplates/eric-sc-umbrella-*.tgz'",
                    "cd Definitions/OtherTemplates",
                    "mkdir unpacked; cd unpacked",
                    "tar xf ../eric-sc-umbrella-1*.tgz",
                    "sed -i 's/\"role_max_ttl\": .*/\"role_max_ttl\": \"315576000\",/' eric-sc-umbrella/charts/eric-sec-sip-tls/templates/config-map.yaml",
                    "grep -E 'override.*?tl' * -r -l | xargs -i sed -i -E \"/validity:|override-ttl:|overrideTtl:|overrideLeadTime:/d\" {}",
                    "umbrellafile=\$(ls --color=never -1 ../eric-sc-umbrella-*); rm -f \$umbrellafile; tar czf \$umbrellafile *",
                    "cd ..; rm -fr unpacked",
                    "cd ../..",
                    "csarfile=\$(ls --color=never -1 eric-sc-*.csar); zip -u \$csarfile Definitions/OtherTemplates/eric-sc-umbrella-*.tgz",
                    "rm -f \$csarfile.md5; md5sum \$csarfile | cut -d\" \" -f 1 > \$csarfile.md5",
                    "rm -f \$csarfile.sha256; sha256sum \$csarfile | cut -d\" \" -f 1 > \$csarfile.sha256",
                    "rm -fr Definitions/",
                ],
            }
        );
        if ($rc != 0) {
            General::Logging::log_user_error_message("Failed to create the file: $filename");
            return 1;
        }

        General::Logging::log_user_message("Create software package for time shift test");
        my $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}expect $::JOB_PARAMS{'_PACKAGE_DIR'}/expect/bin/send_command_to_ssh.exp " .
                    "--ip=$director_ip " .
                    "--user=eccd " .
                    "--password='notneeded' " .
                    "--timeout=$expect_timeout " .
                    "--stop-on-error " .
                    "--command-file=$filename ",
                "hide-output"   => 1,
             }
        );

        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to create the package, see details above");
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
sub Cleanup_Job_Environment_P803S04 {

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
sub Fallback001_P803S99 {

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
        'ARTIFACT_TOKEN' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "cmVmdGtuOjAxOjE3NDM3NTYxMjQ6TkhWdmFvMjVmY2xWZkY3QzJORDRTNVFOWGV1",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls the token name used for fetching the software from the
artifactory. Without a valid value the fetching of the software will fail.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DIRECTOR_IP' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls the IP-address to reach the director node where most of
the logic is executed.
NOTE: That the known_host file on the director must include the public ssh key
of the current user because it needs to be able to login to the director without
giving a password.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'DEBUG_PLAYLIST' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control if the playlist execution should be
executed as normal (=no) or if special handling should be applied (=yes).

If the value is "no" which is also the default value then normal playlist
execution will be done and all applicable commands will be executed.

If the value is "yes" then playlist execution will be altered so that any
command that changes the data on the running cluster will not be executed and
instead will just be echoed to the log files and no change is done, i.e. no
Deployment will take place.
Any command that will just change files or data on the job workspace directory
or any command that just reads data from the cluster will still be executed.
This parameter can be useful to check if the overall logic is working without
doing any changes to the running system.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'EXPECT_DEFAULT_TIMEOUT' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "7200",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls the default timeout of the commands executed by the
send_command_to_ssh.exp script used by some playlists to execute commands on
a remote node.
The value specifies how many seconds to wait before giving up on a command that
does not return a new prompt.
Some playlists can still use some other hard coded timeout value in which case
the value set by this parameter is not used, check each playlist which calls the
expect script to see what default value is being used.

By default the value is set to 2 hours (7200 seconds) which should be long
enough for most commands.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'PACKAGE_VERSION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls which software package will be used.
It should specify the version numbers, e.g. 1.7.1+922.
EOF
            'validity_mask' => '\d+\.\d+\.\d+[-+]\S+',
            'value'         => "",
        },
    )
}
# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist creates a software package that can be used for performing the time
shift test case. Basically it fetches an existing software package from artifactory
and then it creates a copy of this package which it modifies to allow for longer
validity time of all certificates from the default 7 days to 10 years.

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
