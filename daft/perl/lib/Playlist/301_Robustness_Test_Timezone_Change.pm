package Playlist::301_Robustness_Test_Timezone_Change;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.24
#  Date     : 2024-06-06 10:59:24
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
    parse_network_config_parameters
    usage
    usage_return_playlist_variables
    );

use ADP::Kubernetes_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;

use Playlist::902_Cleanup_Job_Environment;
use Playlist::914_Collect_Logs;
use Playlist::932_Test_Case_Common_Logic;

#
# Variable Declarations
#
my %playlist_variables;
set_playlist_variables();

my $debug_command = "";         # Change this value from "echo " to "" when ready to test on real system

# Timezones to use for each repetition, index 0 will store the original timezone
# which will be used to restore the node timezone back to this timezone after the
# last repetition of the timezone change.
# For timezone values see e.g. https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
#my @timezones = ( "", "Etc/GMT+2", "Etc/GMT+3", "Etc/GMT+4" );     # Failing, causing eric-fh-snmp-alarm-provider- POD cyclic restarts, see DND-28846 and GSSUPP-7152
my @timezones = ( "", "Asia/Beirut", "Europe/Moscow", "Asia/Shanghai" );    # These values seems to work

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

    # Set Job Type
    $::JOB_PARAMS{'JOBTYPE'} = "ROBUSTNESS_TEST_TIMEZONE_CHANGE";

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Test_Checks_P301S01, \&Fallback001_P301S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Test_Case_P301S02, \&Fallback001_P301S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Checks_P301S03, \&Fallback001_P301S99 );
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
sub Perform_Pre_Test_Checks_P301S01 {

    my $rc;

    # This first call will initialize job environment and perform pre test checks
    $::JOB_PARAMS{'P932_TASK'} = "PRE_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
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
#
# -----------------------------------------------------------------------------
sub Perform_Test_Case_P301S02 {

    my $length;
    my $message;
    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P301S02T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Get_Current_Timezone_Value_P301S02T02 } );
    return $rc if $rc < 0;

    # Set start value either to 1 if never executed or to value of P301_TIMEZONE_CNT job variable
    # if job has been interrupted and rerun.
    $::JOB_PARAMS{'P301_TIMEZONE_CNT'}       = exists $::JOB_PARAMS{'P301_TIMEZONE_CNT'}       ? $::JOB_PARAMS{'P301_TIMEZONE_CNT'}       : 1;
    $::JOB_PARAMS{'P301_EXECUTE_P301S02T03'} = exists $::JOB_PARAMS{'P301_EXECUTE_P301S02T03'} ? $::JOB_PARAMS{'P301_EXECUTE_P301S02T03'} : 1;

    # Repeat the test case a number of times
    for (; $::JOB_PARAMS{'P301_TIMEZONE_CNT'} <= $#timezones;) {
        $::JOB_PARAMS{'P301_TIMEZONE_VALUE'} = $timezones[$::JOB_PARAMS{'P301_TIMEZONE_CNT'}];

        # Set start time for this repetition in case we want to have KPI verdicts for each repetition
        $::JOB_PARAMS{'KPI_START_TIME'} = time();

        # Set the description to be used for status messages
        $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case %d Time Zone Change (%s)", $::JOB_PARAMS{'P301_TIMEZONE_CNT'}, $::JOB_PARAMS{'P301_TIMEZONE_VALUE'};

        $message = sprintf "# Repetition %d of %d, Timezone=%s #", $::JOB_PARAMS{'P301_TIMEZONE_CNT'}, $#timezones, $timezones[$::JOB_PARAMS{'P301_TIMEZONE_CNT'}];
        $length = length($message);
        General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

        if ($::JOB_PARAMS{'P301_EXECUTE_P301S02T03'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_Timezone_P301S02T03 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P301_EXECUTE_P301S02T03'} = 0;
        }

        # This call will wait for all PODs to be up and then check node health
        $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
        $::JOB_PARAMS{'P933_HEALTH_CHECK_FILE'} = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/system_health_check_files/check_file_301_Robustness_Test_Timezone_Change.config";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
        return $rc if $rc < 0;

        # Step the counter and reset the executed tasks
        $::JOB_PARAMS{'P301_TIMEZONE_CNT'}++;
        $::JOB_PARAMS{'P301_EXECUTE_P301S02T03'} = 1;
    }

    # Restore original timezone value if known
    if ($::JOB_PARAMS{'SKIP_RESTORE_TIMEZONE'} eq "no" && $timezones[0] ne "" && $timezones[0] ne $::JOB_PARAMS{'TIMEZONE'}) {
        # We have an original timezone value and it's not the same as last used value, so change it back

        $message = sprintf "# Restoring original Timezone=%s #", $timezones[0];
        $length = length($message);
        General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

        $::JOB_PARAMS{'P301_TIMEZONE_CNT'} = 0;
        $::JOB_PARAMS{'P301_TIMEZONE_VALUE'} = $timezones[0];
        $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case Time Zone Restore (%s)", $::JOB_PARAMS{'P301_TIMEZONE_VALUE'};
        $::JOB_PARAMS{'KPI_START_TIME'} = time();
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Change_Timezone_P301S02T03 } );
        return $rc if $rc < 0;

        # This call will wait for all PODs to be up and then check node health
        $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
        $::JOB_PARAMS{'P933_HEALTH_CHECK_FILE'} = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/system_health_check_files/check_file_301_Robustness_Test_Timezone_Change.config";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
        return $rc if $rc < 0;
    }

    # Skip the final post healthcheck since we have already done it above
    $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = "yes";

    # Delete these temporary variables
    delete $::JOB_PARAMS{'P301_EXECUTE_P301S02T03'};
    delete $::JOB_PARAMS{'P301_TIMEZONE_CNT'};
    delete $::JOB_PARAMS{'P301_TIMEZONE_VALUE'};

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
    sub Check_Job_Parameters_P301S02T01 {

        my $rc = 0;

        # Check if the user want to modify the time zone values to use
        if ($::JOB_PARAMS{'TIMEZONES'} ne "") {
            @timezones = ( "", split /[,|]+/, $::JOB_PARAMS{'TIMEZONES'} );
        }

        General::Logging::log_user_message("Used Time zones are:" . join "\n - ", @timezones);

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
    sub Get_Current_Timezone_Value_P301S02T02 {

        my $rc = 0;
        my @result;

        General::Logging::log_user_message("Getting currently used timezone value.\nThis will take a while to complete.\n");
        $rc = ADP::Kubernetes_Operations::get_helm_release_extended_information(
            {
                "command"         => "values --all",
                "hide-output"     => 1,
                "namespace"       => $::JOB_PARAMS{'SC_NAMESPACE'},
                "output-format"   => "dot-separated",
                "release-name"    => $::JOB_PARAMS{'SC_RELEASE_NAME'},
                "return-output"   => \@result,
                "return-patterns" => [ '^global\.timezone=' ],
            }
        );

        if ($rc == 0 && scalar @result == 1) {
            if ($result[0] =~ /^global\.timezone=(\S+)\s*$/) {
                # Update index 0 of array with current timezone value
                $timezones[0] = $1;
                General::Logging::log_user_message("Currently used time zone is $1");
            } else {
                General::Logging::log_user_warning_message("Unexpected value ($result[0]) returned for current time zone value, so no attempt is done to set it back after the test");
            }
        } else {
            General::Logging::log_user_warning_message("Unable to fetch the currently used time zone value (rc=$rc), so no attempt is done to set it back after the test");
            $rc = 0;
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
    sub Change_Timezone_P301S02T03 {

        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $sc_release_name;
        my $helm_executable =  $::JOB_PARAMS{'HELM_EXECUTABLE'};
        my $rc = 0;
        my $sc_umbrella_file;
        my $timeout = $::JOB_PARAMS{'HELM_TIMEOUT'};
        my @release_helm_chart = ();

        if ($::JOB_PARAMS{'HELM_VERSION'} != 2) {
            $timeout .= "s";    # Add an extra 's' for number of seconds which is needed for helm 3 or higher
        }

        if ($::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'} ne "") {
            # New CNCS package.
            push @release_helm_chart, "$::JOB_PARAMS{'OLD_CLOUD_NATIVE_BASE_RELEASE_NAME'}|$::JOB_PARAMS{'CLOUD_NATIVE_BASE_UMBRELLA_FILE'}";
            push @release_helm_chart, "$::JOB_PARAMS{'OLD_CLOUD_NATIVE_NF_ADDITIONS_RELEASE_NAME'}|$::JOB_PARAMS{'CLOUD_NATIVE_NF_ADDITIONS_UMBRELLA_FILE'}";
            push @release_helm_chart, "$::JOB_PARAMS{'OLD_SC_CS_RELEASE_NAME'}|$::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'}";

            # Now add release and helm chart values for the deployed network functions
            for my $helm_chart (split /\|/, $::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'}) {
                if ($helm_chart =~ /eric-sc-bsf-\d+\.\d+\.\d+/) {
                    if ($::JOB_PARAMS{'OLD_SC_BSF_RELEASE_NAME'} ne "") {
                        # BSF is deployed
                        push @release_helm_chart, "$::JOB_PARAMS{'OLD_SC_BSF_RELEASE_NAME'}|$helm_chart";
                    }
                } elsif ($helm_chart =~ /eric-sc-scp-\d+\.\d+\.\d+/) {
                    if ($::JOB_PARAMS{'OLD_SC_SCP_RELEASE_NAME'} ne "") {
                        # SCP is deployed
                        push @release_helm_chart, "$::JOB_PARAMS{'OLD_SC_SCP_RELEASE_NAME'}|$helm_chart";
                    }
                } elsif ($helm_chart =~ /eric-sc-sepp-\d+\.\d+\.\d+/) {
                    if ($::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_NAME'} ne "") {
                        # SEPP is deployed
                        push @release_helm_chart, "$::JOB_PARAMS{'OLD_SC_SEPP_RELEASE_NAME'}|$helm_chart";
                    }
                } elsif ($helm_chart =~ /eric-dsc-\d+\.\d+\.\d+/) {
                    # This is the old cnDSC or new cncs deployment
                    if ($::JOB_PARAMS{'OLD_DSC_RELEASE_NAME'} ne "") {
                        # cnDSC is deployed
                        push @release_helm_chart, "$::JOB_PARAMS{'OLD_DSC_RELEASE_NAME'}|$helm_chart";
                    }
                }
            }
        } else {
            push @release_helm_chart, "$::JOB_PARAMS{'SC_RELEASE_NAME'}|$::JOB_PARAMS{'APPLICATION_UMBRELLA_FILE'}";
        }

        for my $release_and_helm_chart (@release_helm_chart) {
            if ($release_and_helm_chart =~ /^(.+)\|(.+)/) {
                $sc_release_name = $1;
                $sc_umbrella_file = $2;
            } else {
                # Wrong format, should not happen.
                General::Logging::log_user_error_message("Incorrect format of the release and helm chart variable ($release_and_helm_chart)");
                return 1;
            }

            General::Logging::log_user_message("Changing timezone to $::JOB_PARAMS{'P301_TIMEZONE_VALUE'} for release $sc_release_name.\nThis will take a while to complete.\n");

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${debug_command}${helm_executable} --namespace $sc_namespace upgrade $sc_release_name --set global.timezone=$::JOB_PARAMS{'P301_TIMEZONE_VALUE'} $sc_umbrella_file --timeout $timeout --reuse-values --debug",
                    "hide-output"   => 1,
                }
            );

            if ($rc == 0) {
                General::Logging::log_user_message("$::JOB_PARAMS{'TEST_DESCRIPTION'} for release $sc_release_name was successful");
            } else {
                # Display the result in case of error
                General::OS_Operations::write_last_temporary_output_to_progress();

                General::Logging::log_user_error_message("$::JOB_PARAMS{'TEST_DESCRIPTION'} for release $sc_release_name failed");
                push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
                return 1;
            }
        }

        push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} was successful";

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
sub Perform_Post_Test_Checks_P301S03 {

    my $rc;

    # This second call will perform post test checks and job cleanup
    $::JOB_PARAMS{'P932_TASK'} = "POST_CHECK";
    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
    return $rc if $rc < 0;

    return $rc;
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P301S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::914_Collect_Logs::main } );
    return $rc if $rc < 0;

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
# This subroutine reads all parameters from the specified network configuration
# file and updates the %::NETWORK_CONFIG_PARAMS and %::JOB_PARAMS hash variables
# after applying filters to remove unwanted parameters.
#
sub parse_network_config_parameters {
    return Playlist::932_Test_Case_Common_Logic::parse_network_config_parameters();
}

# -----------------------------------------------------------------------------
sub set_playlist_variables {
    # Define playlist specific variables that is not already defined in the
    # sub playlist Playlist::932_Test_Case_Common_Logic.
    %playlist_variables = (
        # ---------------------------------------------------------------------
        'SKIP_RESTORE_TIMEZONE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Select "yes" if the Playlist should skip restoring the original time zone after
completed test.

Select "no" which is also the default value if not specified, to have the
Playlist always restore the original time zone after completed test.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SOFTWARE_DIR' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "yes",       # <yes|no>
            'type'          => "directory", # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specify the base directory that holds all the software to be deployed or
upgraded on the node, i.e. the directory that contains the 'eric-sc-*.csar' and
'eric-sc-tools-*.tgz' files.
This directory should contain the same software that is currently deployed on
the cluster, from this directory the playlist will extract out the SC umbrella
file which is used by by this Robustness playlists when applying the different
timezones by using the 'helm upgrade' command.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'TIMEZONES' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the time zone values to change the node to.
If multiple time zones should be tested, one-after-another then these can be
specified by separating each one with either a "|" or a ",".

If multiple time zones are specified then these time zones will be tested in
the order they are specified.

If the parameter is not specified then a default set of timezones as defined
in the playlist logic will be used.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
sub usage {
    print <<EOF;

Description:
============

This Playlist performs a robustness test case that verifies timezone change.

Used Job Parameters:
====================
EOF
    # Special handling for the 3xx_Robustness_Test_xxxx playlists so we print out all variable
    # information from the main playlist and the 932_Test_Case_Common_Logic sub playlist.
    use File::Basename qw(dirname basename);
    my $length;
    my $message;
    my $path_to_932_playlist = dirname(__FILE__) . "/932_Test_Case_Common_Logic.pm";
    my $playlist_name = basename(__FILE__);
    $playlist_name =~ s/\.pm//;
    my %temp_hash = ( %Playlist::932_Test_Case_Common_Logic::playlist_variables, %playlist_variables );

    General::Playlist_Operations::print_info_about_job_variables(\%temp_hash);

    $message = "# Global variable access in playlist $playlist_name #";
    $length = length($message);
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables(__FILE__, \%playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables(__FILE__);

    $message = "# Global variable access in playlist 932_Test_Case_Common_Logic #";
    $length = length($message);
    printf "\n%s\n%s\n%s\n", "#" x $length, $message, "#" x $length;
    General::Playlist_Operations::print_used_job_variables($path_to_932_playlist, \%Playlist::932_Test_Case_Common_Logic::playlist_variables);
    General::Playlist_Operations::print_used_network_config_variables($path_to_932_playlist);
}

# -----------------------------------------------------------------------------
sub usage_return_playlist_variables {
    return ( %Playlist::932_Test_Case_Common_Logic::playlist_variables, %playlist_variables );
}

1;
