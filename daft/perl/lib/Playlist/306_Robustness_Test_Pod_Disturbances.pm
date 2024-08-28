package Playlist::306_Robustness_Test_Pod_Disturbances;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.2
#  Date     : 2023-12-01 11:42:20
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2023
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

# Commands to use for causing pod disturbances, the 'kubectl command will be replaced by value of KUBECTL_EXECUTABLE
# job variable and then a namespace will be added before sending the command.
my @pod_disturbance_commands = ( "", "kubectl rollout restart deployment", "kubectl delete pods --all" );

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
    $::JOB_PARAMS{'JOBTYPE'} = "ROBUSTNESS_TEST_POD_DISTURBANCES";

    # Check if we should skip the execution of certain helm, kubectl and other commands that change data.
    if ($::JOB_PARAMS{'DEBUG_PLAYLIST'} eq "yes") {
        $debug_command = "echo ";
    }

    $rc = General::Playlist_Operations::execute_step( \&Perform_Pre_Test_Checks_P306S01, \&Fallback001_P306S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Test_Case_P306S02, \&Fallback001_P306S99 );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_step( \&Perform_Post_Test_Checks_P306S03, \&Fallback001_P306S99 );
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
sub Perform_Pre_Test_Checks_P306S01 {

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
sub Perform_Test_Case_P306S02 {

    my $length;
    my $message;
    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P306S02T01 } );
    return $rc if $rc < 0;

    # Set start value either to 1 if never executed or to value of P306_COMMAND_CNT job variable
    # if job has been interrupted and rerun.
    $::JOB_PARAMS{'P306_COMMAND_CNT'}       = exists $::JOB_PARAMS{'P306_COMMAND_CNT'}       ? $::JOB_PARAMS{'P306_COMMAND_CNT'}       : 1;
    $::JOB_PARAMS{'P306_EXECUTE_P306S02T02'} = exists $::JOB_PARAMS{'P306_EXECUTE_P306S02T03'} ? $::JOB_PARAMS{'P306_EXECUTE_P306S02T03'} : 1;

    # Repeat the test case a number of times
    for (; $::JOB_PARAMS{'P306_COMMAND_CNT'} <= $#pod_disturbance_commands;) {
        $::JOB_PARAMS{'P306_COMMAND'} = $pod_disturbance_commands[$::JOB_PARAMS{'P306_COMMAND_CNT'}];

        # Set start time for this repetition in case we want to have KPI verdicts for each repetition
        $::JOB_PARAMS{'KPI_START_TIME'} = time();

        # Set the description to be used for status messages
        $::JOB_PARAMS{'TEST_DESCRIPTION'} = sprintf "Robustness Test Case %d Pod Disturbance (%s)", $::JOB_PARAMS{'P306_COMMAND_CNT'}, $::JOB_PARAMS{'P306_COMMAND'};

        $message = sprintf "# Repetition %d of %d, Command=%s #", $::JOB_PARAMS{'P306_COMMAND_CNT'}, $#pod_disturbance_commands, $pod_disturbance_commands[$::JOB_PARAMS{'P306_COMMAND_CNT'}];
        $length = length($message);
        General::Logging::log_user_message( sprintf "%s\n%s\n%s\n", "#" x $length, $message, "#" x $length );

        if ($::JOB_PARAMS{'P306_EXECUTE_P306S02T02'} == 1) {
            $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Disturb_Pods_P306S02T02 } );
            return $rc if $rc < 0;
            $::JOB_PARAMS{'P306_EXECUTE_P306S02T02'} = 0;
        }

        # This call will wait for all PODs to be up and then check node health
        $::JOB_PARAMS{'P932_TASK'} = "WAIT_FOR_STABLE_NODE";
        $::JOB_PARAMS{'P933_HEALTH_CHECK_FILE'} = "$::JOB_PARAMS{'_PACKAGE_DIR'}/templates/system_health_check_files/check_file_306_Robustness_Test_Pod_Disturbances.config";
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Playlist::932_Test_Case_Common_Logic::main } );
        return $rc if $rc < 0;

        # Step the counter and reset the executed tasks
        $::JOB_PARAMS{'P306_COMMAND_CNT'}++;
        $::JOB_PARAMS{'P306_EXECUTE_P306S02T02'} = 1;
    }

    # Skip the final post healthcheck since we have already done it above
    $::JOB_PARAMS{'SKIP_POST_HEALTHCHECK'} = "yes";
    $::JOB_PARAMS{'SKIP_COLLECT_LOGS'} = "yes";

    # Delete these temporary variables
    delete $::JOB_PARAMS{'P306_EXECUTE_P306S02T02'};
    delete $::JOB_PARAMS{'P306_COMMAND_CNT'};
    delete $::JOB_PARAMS{'P306_COMMAND'};

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
    sub Check_Job_Parameters_P306S02T01 {

        my $rc = 0;

        # Check if the user want to modify the pod disturbance commands to use
        if ($::JOB_PARAMS{'POD_DISTURBANCE_COMMANDS'} ne "") {
            @pod_disturbance_commands = ( "", split /[,|]+/, $::JOB_PARAMS{'POD_DISTURBANCE_COMMANDS'} );
        }

        # Replace kubectl command and add namespace
        for (@pod_disturbance_commands) {
            if (/^kubectl\s+/) {
                s/^kubectl\s+/$::JOB_PARAMS{'KUBECTL_EXECUTABLE'} /;
                $_ .= " -n $::JOB_PARAMS{'SC_NAMESPACE'}";
            }
        }

        General::Logging::log_user_message("Used Pod Disturbance commands are:" . join "\n - ", @pod_disturbance_commands);

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
    sub Disturb_Pods_P306S02T02 {

        my $command = $::JOB_PARAMS{'P306_COMMAND'};
        my $rc = 0;

        General::Logging::log_user_message("Executing Pod Disturbance Command: $command");

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${debug_command}${command}",
                "hide-output"   => 1,
            }
        );

        if ($rc == 0) {
            General::Logging::log_user_message("$::JOB_PARAMS{'TEST_DESCRIPTION'} was successful");
            push @::JOB_STATUS, "(/) $::JOB_PARAMS{'TEST_DESCRIPTION'} was successful";
        } else {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("$::JOB_PARAMS{'TEST_DESCRIPTION'} failed");
            push @::JOB_STATUS, "(x) $::JOB_PARAMS{'TEST_DESCRIPTION'} failed";
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
sub Perform_Post_Test_Checks_P306S03 {

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
sub Fallback001_P306S99 {

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
        'POD_DISTURBANCE_COMMANDS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
Specifies the Pod Disturbance commands to use.
If multiple commands should be tested, one-after-another then these can be
specified by separating each one with either a "|" or a ",".

If multiple commands are specified then these commands will be tested in the
order they are specified.

If the parameter is not specified then a default set of commands as defined
in the playlist logic will be used.
Currently defined default values are:

 - kubectl rollout restart deployment
 - kubectl delete pods --all

Any command starting with 'kubectl' will be replaced by the playlist variable
KUBECTL_EXECUTABLE contents, and then the namespace of SC_NAMESPACE will be
added to the end of the command.
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

This Playlist performs a robustness test case that verifies POD disturbances
like restarts.

By default it performs the following disturbances:

EOF

    for (my $i=1; $i<=$#pod_disturbance_commands; $i++) {
        print " - $pod_disturbance_commands[$i] -n <namespace>\n";
    }

    print <<EOF;

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
