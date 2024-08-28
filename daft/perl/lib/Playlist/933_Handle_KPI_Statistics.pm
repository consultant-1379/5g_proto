package Playlist::933_Handle_KPI_Statistics;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.32
#  Date     : 2024-05-06 16:38:53
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2022
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

use ADP::Kubernetes_Operations;
use General::Logging;
use General::OS_Operations;
use General::Playlist_Operations;
use version;

#
# Variable Declarations
#
my %playlist_variables;
set_playlist_variables();

my $job_parameters_checked = 0;

my %kpi_traffic_difference_matrix;
    # Populate this hash if specific values should be used per software release
    # instead of using the default value of the job parameter KPI_TRAFFIC_COMPARE_DIFFERENCE.
    # Note that if a value is given to the KPI_TRAFFIC_COMPARE_DIFFERENCE parameter
    # then this value will always be used and any values specified in this hash is
    # ignored.
    # The hash key should be in one of the following formats:
    #   '<OLD_SC_RELEASE_VERSION> to <SC_RELEASE_VERSION>'
    #   '<OLD_SC_RELEASE_VERSION>'
    # The value should be a positive integer or floating point value.
    #
    # For example:
    # $kpi_traffic_difference_matrix{'1.8.0 to 1.10.0'} = 8.5;
    # $kpi_traffic_difference_matrix{'1.10.0'} = 9;

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

    unless (exists $::JOB_PARAMS{'P933_TASK'}) {
        General::Logging::log_user_error_message("Job variable 'P933_TASK' not set before calling this playlist");
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    # If the playlist needs to have certain job parameter values for the execution
    # of the playlist then it needs to check if they have already been given by the
    # user when starting the job, otherwise the user should be prompted for their
    # value.
    if (General::Playlist_Operations::parse_playlist_variables(\%playlist_variables, \%::JOB_PARAMS) != 0) {
        General::Logging::log_user_error_message("Failed to parse the playlist variables");
        return General::Playlist_Operations::RC_GRACEFUL_EXIT;
    }

    # Check if network configuration file is specified
    if (General::Playlist_Operations::is_optional_network_config_loaded() != 0) {
        unless (exists $::JOB_PARAMS{'CMYP_PASSWORD'} && exists $::JOB_PARAMS{'CMYP_NEW_PASSWORD'} && exists $::JOB_PARAMS{'CMYP_USER'}) {
            General::Logging::log_user_warning_message("No network config parameter specified and one or more of the following job variables are missing:\nCMYP_PASSWORD, CMYP_NEW_PASSWORD and CMYP_USER\nDefault values will be used.");
            # Set default values and hope they work
            $::JOB_PARAMS{'CMYP_PASSWORD'} = "rootroot";
            $::JOB_PARAMS{'CMYP_NEW_PASSWORD'} = "rootroot";
            $::JOB_PARAMS{'CMYP_USER'} = "expert";
        }
    }

    unless (exists $::JOB_PARAMS{'P933_STATE'}) {
        # The first time we execute this playlist we initialize a few things
        $::JOB_PARAMS{'P933_STATE'} = "IDLE";

        # Set tasks to always execute
        General::State_Machine::always_execute_task("Playlist::933_Handle_KPI_Statistics::.+");
    }

    General::Logging::log_user_message("P933_TASK=$::JOB_PARAMS{'P933_TASK'}\nP933_STATE=$::JOB_PARAMS{'P933_STATE'}\n");

    #
    # START_KPI_COLLECTION
    #

    if ($::JOB_PARAMS{'P933_TASK'} eq "START_KPI_COLLECTION") {
        if ($::JOB_PARAMS{'P933_STATE'} ne "IDLE") {
            General::Logging::log_user_warning_message("The variable P933_STATE (=$::JOB_PARAMS{'P933_STATE'}) is not IDLE.\nPlease check the calling playlist and fix the fault.");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        if ($job_parameters_checked == 0) {
            $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P933S01, \&Fallback001_P933S99 );
            return $rc if $rc < 0;
            # Perform the check only one time
            $job_parameters_checked = 1;
        }

        $rc = General::Playlist_Operations::execute_step( \&Start_KPI_Collection_P933S02, \&Fallback001_P933S99 );
        return $rc if $rc < 0;

        $::JOB_PARAMS{'P933_STATE'} = "STARTED";

    #
    # STOP_KPI_COLLECTION
    #

    } elsif ($::JOB_PARAMS{'P933_TASK'} eq "STOP_KPI_COLLECTION") {
        if ($::JOB_PARAMS{'P933_STATE'} ne "STARTED") {
            General::Logging::log_user_warning_message("The variable P933_STATE (=$::JOB_PARAMS{'P933_STATE'}) is not STARTED.\nPlease check the calling playlist and fix the fault.");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        $rc = General::Playlist_Operations::execute_step( \&Stop_KPI_Collection_P933S03, \&Fallback001_P933S99 );
        return $rc if $rc < 0;

        $::JOB_PARAMS{'P933_STATE'} = "IDLE";

    #
    # KPI_SNAPSHOT
    #

    } elsif ($::JOB_PARAMS{'P933_TASK'} eq "KPI_SNAPSHOT") {

        if ($job_parameters_checked == 0) {
            $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P933S01, \&Fallback001_P933S99 );
            return $rc if $rc < 0;
            # Perform the check only one time
            $job_parameters_checked = 1;
        }

        $rc = General::Playlist_Operations::execute_step( \&KPI_Snapshot_P933S04, \&Fallback001_P933S99 );
        return $rc if $rc < 0;

    #
    # KPI_VERDICT
    #

    } elsif ($::JOB_PARAMS{'P933_TASK'} eq "KPI_VERDICT") {

        # Initialize the result
        $::JOB_PARAMS{'P933_VERDICT_RESULT'} = "UNKNOWN";

        if ($job_parameters_checked == 0) {
            $rc = General::Playlist_Operations::execute_step( \&Check_Job_Parameters_P933S01, \&Fallback001_P933S99 );
            return $rc if $rc < 0;
            # Perform the check only one time
            $job_parameters_checked = 1;
        }

        $rc = General::Playlist_Operations::execute_step( \&KPI_Verdict_P933S05, \&Fallback001_P933S99 );
        return $rc if $rc < 0;

    #
    # KPI_COMPARE
    #

    } elsif ($::JOB_PARAMS{'P933_TASK'} eq "KPI_COMPARE") {

        $rc = General::Playlist_Operations::execute_step( \&Compare_KPI_Statistics_P933S06, \&Fallback001_P933S99 );
        return $rc if $rc < 0;

    } else {
        # Some unknown task was give, so write an error to the caller that something if strange with their logic.
        General::Logging::log_user_warning_message("This Playlist has been called with an invalid job parameter P933_TASK=$::JOB_PARAMS{'P933_TASK'}.\nPlease check the calling playlist and fix the fault.");
        $rc = General::Playlist_Operations::RC_TASKOUT;
    }

    # Delete the contents of this variables if existing to make sure that the proper directory or file is set every time the playlist is called
    $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} = "";
    $::JOB_PARAMS{'P933_HEALTH_CHECK_FILE'} = "";

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
sub Check_Job_Parameters_P933S01 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Check_Job_Parameters_P933S01T01 } );
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
    sub Check_Job_Parameters_P933S01T01 {

        my $rc = 0;
        my $sc_namespace = "";

        # Get the proper SC_NAMESPACE
        General::Logging::log_user_message("Checking Job parameter 'SC_NAMESPACE' and Network Config parameter 'sc_namespace'");
        if (exists $::JOB_PARAMS{'SC_NAMESPACE'} && $::JOB_PARAMS{'SC_NAMESPACE'} ne "") {
            $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        } elsif (exists $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'}) {
            if ($::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'} ne "CHANGEME") {
                $::JOB_PARAMS{'SC_NAMESPACE'} = $::NETWORK_CONFIG_PARAMS{'sc_namespace'}{'value'};
                $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
            } else {
                General::Logging::log_user_error_message("Network Config parameter 'sc_namespace' has not been set and Job parameter 'SC_NAMESPACE' not provided");
                return General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            General::Logging::log_user_error_message("Neither Job parameter 'SC_NAMESPACE', nor Network Config parameter 'sc_namespace' has been set");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        # Find the used password for the 'expert' user.
        for my $key (keys %::NETWORK_CONFIG_PARAMS) {
            next unless ($key =~ /^default_user_\d+$/);
            if ($::NETWORK_CONFIG_PARAMS{$key}{'user'} eq "expert") {
                if (exists $::JOB_PARAMS{'CMYP_USER'}) {
                    if ($::JOB_PARAMS{'CMYP_USER'} eq "") {
                        $::JOB_PARAMS{'CMYP_USER'} = $::NETWORK_CONFIG_PARAMS{$key}{'user'};
                    }
                } else {
                    $::JOB_PARAMS{'CMYP_USER'} = $::NETWORK_CONFIG_PARAMS{$key}{'user'};
                }

                if (exists $::JOB_PARAMS{'CMYP_PASSWORD'}) {
                    if ($::JOB_PARAMS{'CMYP_PASSWORD'} eq "") {
                        $::JOB_PARAMS{'CMYP_PASSWORD'} = $::NETWORK_CONFIG_PARAMS{$key}{'initial_password'};
                    }
                } else {
                    $::JOB_PARAMS{'CMYP_PASSWORD'} = $::NETWORK_CONFIG_PARAMS{$key}{'initial_password'};
                }

                if (exists $::JOB_PARAMS{'CMYP_NEW_PASSWORD'}) {
                    if ($::JOB_PARAMS{'CMYP_NEW_PASSWORD'} eq "") {
                        $::JOB_PARAMS{'CMYP_NEW_PASSWORD'} = $::NETWORK_CONFIG_PARAMS{$key}{'password'};
                    }
                } else {
                    $::JOB_PARAMS{'CMYP_NEW_PASSWORD'} = $::NETWORK_CONFIG_PARAMS{$key}{'password'};
                }
            }
        }

        if ($::JOB_PARAMS{'CMYP_USER'} eq "" || $::JOB_PARAMS{'CMYP_USER'} eq "CHANGEME") {
            General::Logging::log_user_error_message("Job parameter value for 'CMYP_USER' is empty or CHANGEME");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        if ($::JOB_PARAMS{'CMYP_PASSWORD'} eq "" || $::JOB_PARAMS{'CMYP_PASSWORD'} eq "CHANGEME") {
            General::Logging::log_user_error_message("Job parameter value for 'CMYP_PASSWORD' is empty or CHANGEME");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        if ($::JOB_PARAMS{'CMYP_NEW_PASSWORD'} eq "" || $::JOB_PARAMS{'CMYP_NEW_PASSWORD'} eq "CHANGEME") {
            General::Logging::log_user_error_message("Job parameter value for 'CMYP_NEW_PASSWORD' is empty or CHANGEME");
            return General::Playlist_Operations::RC_FALLBACK;
        }

        General::Logging::log_user_message("SC_NAMESPACE=$sc_namespace\nCMYP_USER = $::JOB_PARAMS{'CMYP_USER'}\n");

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
sub Start_KPI_Collection_P933S02 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Start_Background_KPI_Collection_P933S02T01 } );
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
    sub Start_Background_KPI_Collection_P933S02T01 {

        my $output_directory = ((exists $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} && $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} ne "") ? $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} : "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_statistics");
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/kpi_statistics.pl --namespace=$::JOB_PARAMS{'SC_NAMESPACE'} --output-directory=$output_directory --no-tty --resolution=15 --future=131400h";   # 131400h is about 15 years
        my $deployed_sc_version = ADP::Kubernetes_Operations::get_sc_release_version($::JOB_PARAMS{'SC_NAMESPACE'});
        my $pid;
        my $rc = 0;
        my @result;

        if (version->parse( $deployed_sc_version ) >= version->parse( "1.8.0" ) ) {
            # Starting with SC 1.8.0 we also force the use of recording rules instead of metrics query
            $command .= " --prefer-recording-rule";
        } elsif (exists $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} && $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} ne "") {
            # We must be upgrading for the new CNCS SC 1.15 software where we currently don't know the real SC version
            # from the deployed software. So we just assume we should always be using recording rules.
            $command .= " --prefer-recording-rule";
        }

        $command .= " --cmyp-user=$::JOB_PARAMS{'CMYP_USER'}";
        $command .= " --cmyp-password=$::JOB_PARAMS{'CMYP_PASSWORD'}";
        $command .= " --cmyp-new-password=$::JOB_PARAMS{'CMYP_NEW_PASSWORD'}";
        $command .= " --no-png-graph" if ($ENV{USER} eq "eccd");   # Don't generate PNG graphs when running on the node

        General::Logging::log_user_message("Starting background process to collect KPI values");

        $pid = General::OS_Operations::background_process_run($command);
        $::JOB_PARAMS{'KPI_PID'} = $pid;
        if ($pid > 0) {
            General::Logging::log_user_message("Process Id: $pid");
            $rc = 0;
        } else {
            General::Logging::log_user_error_message("Failed to start background process");
            $rc = 1;
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
sub Stop_KPI_Collection_P933S03 {

    my $rc = 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Stop_Background_KPI_Collection_P933S03T01 } );
    return $rc if $rc < 0;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Show_KPI_Statistics_Summary_P933S03T02 } );
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
    sub Stop_Background_KPI_Collection_P933S03T01 {

        my $pid = $::JOB_PARAMS{'KPI_PID'};
        my $rc = 0;
        my @result;
        my $stop_time = time() + 60;

        if ($pid == 0) {
            General::Logging::log_user_message("No background process to stop");
            return General::Playlist_Operations::RC_STEPOUT;
        }

        if (General::OS_Operations::background_process_is_running($pid) == 0) {
            General::Logging::log_user_message("Background process id $pid not found or is no longer running");
            return General::Playlist_Operations::RC_STEPOUT;
        }

        General::Logging::log_user_message("Stopping background process id $pid that is collecting KPI values");

        $rc = General::OS_Operations::background_process_send_signal($pid, "SIGABRT");
        if ($rc == 0) {
            General::Logging::log_user_warning_message("Failed to send the SIGABRT signal to process id $pid");
            return General::Playlist_Operations::RC_STEPOUT;
        }

        General::Logging::log_user_message("Waiting for pid $pid to terminate");

        while (General::OS_Operations::background_process_is_running($pid) == 1) {
            # Wait for any child (-1) to terminate in non-blocking (0) mode
            if (waitpid(-1,0) == $pid) {
                # Child has been terminated
                General::Logging::log_user_message("The background process with pid $pid has terminated");
                last;
            }

            if (time() >= $stop_time) {
                General::Logging::log_user_warning_message("Timeout after 60 seconds waiting for the process to terminate");
                return General::Playlist_Operations::RC_STEPOUT;
            }

            General::Logging::log_user_message("The process is still running, waiting 10 seconds for it to terminate");
            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "progress-message"  => 0,
                    "seconds"           => 10,
                    "use-logging"       => 1,
                }
            );
            if ($rc == 1) {
                # CTRL-C pressed
                last;
            }
        }

        $::JOB_PARAMS{'KPI_PID'} = 0;

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
    sub Show_KPI_Statistics_Summary_P933S03T02 {

        my $output_directory = ((exists $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} && $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} ne "") ? $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} : "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_statistics");
        my $kpi_summary_file = "$output_directory/kpi_statistics_summary.txt";
        my $rc = 0;
        my @result;

        if (-f "$kpi_summary_file") {
            $rc = General::File_Operations::read_file(
                {
                    "filename"              => $kpi_summary_file,
                    "output-ref"            => \@result,
                }
            );
            if ($rc != 0) {
                General::Logging::log_user_warning_message("Failed to read the KPI summary file ($kpi_summary_file)");
                $rc = 0;
            }

            General::Logging::log_user_message("Collected KPI's:\n" . join "\n", @result);
        } else {
            General::Logging::log_user_warning_message("No KPI summary file found ($kpi_summary_file)");
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
sub KPI_Snapshot_P933S04 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Fetch_KPI_Statistics_P933S04T01 } );
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
    sub Fetch_KPI_Statistics_P933S04T01 {

        my $collecting = 0;
        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/kpi_statistics.pl --namespace=$::JOB_PARAMS{'SC_NAMESPACE'}";
        my $deployed_sc_version = ADP::Kubernetes_Operations::get_sc_release_version($::JOB_PARAMS{'SC_NAMESPACE'});
        my $message = exists $::JOB_PARAMS{'P933_SUMMARY_MESSAGE'} ? "$::JOB_PARAMS{'P933_SUMMARY_MESSAGE'}\n\n" : "";
        my $pid;
        my $rc = 0;
        my @result;

        if (version->parse( $deployed_sc_version ) >= version->parse( "1.8.0" ) ) {
            # Starting with SC 1.8.0 we also force the use of recording rules instead of metrics query
            $command .= " --prefer-recording-rule";
        } elsif (exists $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} && $::JOB_PARAMS{'SC_CS_UMBRELLA_FILE'} ne "") {
            # We must be upgrading for the new CNCS SC 1.15 software where we currently don't know the real SC version
            # from the deployed software. So we just assume we should always be using recording rules.
            $command .= " --prefer-recording-rule";
        }

        $command .= " --cmyp-user=$::JOB_PARAMS{'CMYP_USER'}";
        $command .= " --cmyp-password=$::JOB_PARAMS{'CMYP_PASSWORD'}";
        $command .= " --cmyp-new-password=$::JOB_PARAMS{'CMYP_NEW_PASSWORD'}";
        $command .= sprintf " --resolution=%d", (exists $::JOB_PARAMS{'P933_RESOLUTION'} ? $::JOB_PARAMS{'P933_RESOLUTION'} : 15);
        $command .= sprintf " --past=%s", (exists $::JOB_PARAMS{'P933_PAST'} ? $::JOB_PARAMS{'P933_PAST'} : "1m");
        $command .= sprintf " --output-directory=%s", ((exists $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} && $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} ne "") ? $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} : "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_statistics");
        $command .= " --no-png-graph" if ($ENV{USER} eq "eccd");   # Don't generate PNG graphs when running on the node

        General::Logging::log_user_message("Collecting KPI values from the node.\nThis will take a while to complete.\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc != 0) {
            # Display the result in case of error
            General::OS_Operations::write_last_temporary_output_to_progress();

            General::Logging::log_user_error_message("Failed to fetch KPI values");
            return 1;
        }
        # Look for KPI statistics data
        for (@result) {
            if ($collecting) {
                last if (/^\s*$/);      # Stop when a blank line is found
                $message .= "$_\n";
            } elsif (/Average Value/) {
                $collecting = 1;
                $message .= "$_\n";
            }
        }
        if ($message) {
            # Print the statistics
            General::Logging::log_user_message($message);
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
sub KPI_Verdict_P933S05 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Fetch_KPI_Verdict_P933S05T01 } );
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
    sub Fetch_KPI_Verdict_P933S05T01 {

        my $cmyp_password = exists $::JOB_PARAMS{'CMYP_PASSWORD'} ? $::JOB_PARAMS{'CMYP_PASSWORD'} : "";
        my $cmyp_user = exists $::JOB_PARAMS{'CMYP_USER'} ? $::JOB_PARAMS{'CMYP_USER'} : "";
        my $collecting = 0;
        my $color_info = $::color_scheme eq "no" ? "--no_color " : "";
        my $ignore_failure = $::JOB_PARAMS{'P933_VERDICT_IGNORE_FAILURE'} eq "yes" ? 1 : 0;
        my $kpi_past = $::JOB_PARAMS{'P933_PAST'};
        my $kpi_success_rate_description = $::JOB_PARAMS{'P933_SUCCESS_RATE_DESCRIPTION'};
        my $kpi_success_rate_resolution = $::JOB_PARAMS{'P933_RESOLUTION'};
        my $kpi_success_rate_threshold = $::JOB_PARAMS{'P933_SUCCESS_RATE_THRESHOLD'};
        my $log_file = (exists $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} && $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} ne "" ? "$::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'}/kpi_verdict_summary.txt" : "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_verdict_summary.txt");
        my $message = exists $::JOB_PARAMS{'P933_SUMMARY_MESSAGE'} ? "$::JOB_PARAMS{'P933_SUMMARY_MESSAGE'}\n\n" : "";
        my $sc_namespace = $::JOB_PARAMS{'SC_NAMESPACE'};
        my $rc;
        my @result;
        my $status_message = exists $::JOB_PARAMS{'P933_SUMMARY_MESSAGE'} ? "$::JOB_PARAMS{'P933_SUMMARY_MESSAGE'}" : "KPI Evaluation";

        # If $cmyp_user not yet set, try to read it from the network config parameters
        if ($cmyp_user eq "") {
            # Find the used password for the 'expert' user.
            for my $key (keys %::NETWORK_CONFIG_PARAMS) {
                if ($::NETWORK_CONFIG_PARAMS{$key}{'user'} eq "expert") {
                    $cmyp_user = $::NETWORK_CONFIG_PARAMS{$key}{'user'};
                    $cmyp_password = $::NETWORK_CONFIG_PARAMS{$key}{'password'};
                    last;
                }
            }
        }

        if ($cmyp_user eq "" || $cmyp_password eq "") {
            General::Logging::log_user_warning_message("No CMYP user or password found");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        General::Logging::log_user_message("Collecting KPI values from the node.\nThis will take a while to complete.\n");
        # Perform an almost complete health check
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "perl $::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/system_health_check.pl " .
                                    "--check=kpi_success_rate " .
                                    "--variables kpi_success_rate_description='$kpi_success_rate_description' " .
                                    "--variables kpi_past=$kpi_past " .
                                    "--variables kpi_resolution=$kpi_success_rate_resolution " .
                                    "--variables kpi_success_rate_threshold=$kpi_success_rate_threshold " .
                                    "--variables kpi_details_at_success=1 " .
                                    "--namespace $sc_namespace " .
                                    "--log_file $::JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/all.log " .
                                    "--no_color " .
                                    "--progress_type=none " .
                                    "--variables cmyp_password='$cmyp_password' " .
                                    "--variables cmyp_user=$cmyp_user " .
                                    (exists $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} && $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} ne "" ? "--variables kpi_data_directory=$::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} " : "") .
                                    (exists $::JOB_PARAMS{'P933_HEALTH_CHECK_FILE'} && $::JOB_PARAMS{'P933_HEALTH_CHECK_FILE'} ne "" ? "--check_file=$::JOB_PARAMS{'P933_HEALTH_CHECK_FILE'} " : "") .
                                    ($::JOB_PARAMS{'KUBECONFIG'} ne "" ? "--kubeconfig=$::JOB_PARAMS{'KUBECONFIG'} " : ""),
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        # Look for KPI statistics data
        for (@result) {
            if (/^Statistics Summary/) {
                $collecting = 1;
                $message .= "$_\n";
            } elsif (/^Health Check Status/) {
                $collecting = 0;
                $message =~ s/\n+$//s;
                last;
            } elsif ($collecting) {
                $message .= "$_\n";
            }
        }

        if ($rc == 0) {
            $::JOB_PARAMS{'P933_VERDICT_RESULT'} = "SUCCESS";

            $message .= "\n\n$status_message was successful";
            General::Logging::log_user_message($message);

            push @::JOB_STATUS, "(/) $status_message was successful";
        } else {
            $::JOB_PARAMS{'P933_VERDICT_RESULT'} = "FAILURE";

            $message .= "\n\n$status_message failed";
            General::Logging::log_user_error_message($message);

            push @::JOB_STATUS, "(x) $status_message was unsuccessful";
            $rc = 0 if ($ignore_failure == 1);
        }

        General::File_Operations::write_file(
            {
                "filename"          => $log_file,
                "output-ref"        => [ "-" x 80, "", $message ],
                "append-file"       => 1,
                "file-access-mode"  => "666",
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
sub Compare_KPI_Statistics_P933S06 {

    my $rc;

    $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Compare_Entry_And_Exit_KPI_Statistics_P933S06T01 } );
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
    sub Compare_Entry_And_Exit_KPI_Statistics_P933S06T01 {

        my $command = "$::JOB_PARAMS{'_PACKAGE_DIR'}/perl/bin/kpi_compare.pl";
        my $entry_filename = ((exists $::JOB_PARAMS{'P933_ENTRY_DIRECTORY'} && $::JOB_PARAMS{'P933_ENTRY_DIRECTORY'} ne "") ? $::JOB_PARAMS{'P933_ENTRY_DIRECTORY'} : "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_statistics/entry_statistics") . "/kpi_statistics_summary.txt";
        my $exit_filename =  ((exists $::JOB_PARAMS{'P933_EXIT_DIRECTORY'}  && $::JOB_PARAMS{'P933_EXIT_DIRECTORY'}  ne "") ? $::JOB_PARAMS{'P933_EXIT_DIRECTORY'}  : "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_statistics/exit_statistics")  . "/kpi_statistics_summary.txt";
        my $max_differences = exists $::JOB_PARAMS{'KPI_MAX_COMPARE_DIFFERENCE'} ? $::JOB_PARAMS{'KPI_MAX_COMPARE_DIFFERENCE'} : 20;
        my $output_directory = ((exists $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} && $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} ne "") ? $::JOB_PARAMS{'P933_OUTPUT_DIRECTORY'} : "$::JOB_PARAMS{'_JOB_LOG_DIR'}/kpi_statistics");
        my $rc = 0;
        my @result;
        my $traffic_differences = exists $::JOB_PARAMS{'KPI_TRAFFIC_COMPARE_DIFFERENCE'} ? $::JOB_PARAMS{'KPI_TRAFFIC_COMPARE_DIFFERENCE'} : 10;

        unless (-f "$entry_filename") {
            General::Logging::log_user_warning_message("Not able to find the file '$entry_filename', error ignored but no compare done");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        unless (-f "$exit_filename") {
            General::Logging::log_user_warning_message("Not able to find the file '$exit_filename', error ignored but no compare done");
            return General::Playlist_Operations::RC_TASKOUT;
        }

        if (exists $::JOB_PARAMS{'KPI_TRAFFIC_COMPARE_DIFFERENCE'} && $::playlist_variables{'KPI_TRAFFIC_COMPARE_DIFFERENCE'}{'default_value_used'} eq "yes") {
            # The default value has been used, now check if we have release dependent values to use.
            if (exists $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} && exists $::JOB_PARAMS{'SC_RELEASE_VERSION'} && exists $kpi_traffic_difference_matrix{"$::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} to $::JOB_PARAMS{'SC_RELEASE_VERSION'}"}) {
                $traffic_differences = $kpi_traffic_difference_matrix{"$::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} to $::JOB_PARAMS{'SC_RELEASE_VERSION'}"};
            } elsif (exists $::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'} && exists $kpi_traffic_difference_matrix{"$::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}"}) {
                $traffic_differences = $kpi_traffic_difference_matrix{"$::JOB_PARAMS{'OLD_SC_RELEASE_VERSION'}"};
            }
        }

        $command .= " --entry-file=$entry_filename";
        $command .= " --exit-file=$exit_filename";
        $command .= " --output-directory=$output_directory";
        $command .= " --hide-metrics-query";
        $command .= " --max-difference=$max_differences";
        $command .= " --traffic-performance-check --traffic-difference=$traffic_differences";

        # Compare entry and exit values and calculate max column lengths
        General::Logging::log_user_message("Comparing Entry and Exit KPI files");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            General::Logging::log_user_message(join "\n", @result);
        } elsif ($rc == 2) {
            General::Logging::log_user_warning_message("One or more KPI values are outside expected limits.\n" . join "\n", @result);
            $rc = 0;
        } else {
            General::Logging::log_user_error_message("Comparing Entry and Exit KPI files failed" . join "\n", @result);
            $rc = 1;
        }
        my $kpi_result = "(x) KPI Compare Check was unsuccessful, no result was found";
        my $traffic_charateristics_result = "(x) Traffic Performance Check was unsuccessful, no result was found";
        for (@result) {
            if (/^(OK|Not OK)\s+KPI Counter Check\s*$/) {
                if ($1 eq "OK") {
                    $kpi_result = "(/) KPI Compare Check was successful";
                } else {
                    $kpi_result = "(x) KPI Compare Check was unsuccessful";
                }
            } elsif (/^(OK|Not OK)\s+Traffic Performance Check\s*$/) {
                if ($1 eq "OK") {
                    $traffic_charateristics_result = "(/) Traffic Performance Check was successful";
                } else {
                    $traffic_charateristics_result = "(x) Traffic Performance Check was unsuccessful";
                }
            }
        }
        push @::JOB_STATUS, $kpi_result;
        push @::JOB_STATUS, $traffic_charateristics_result;

        return $rc;
    }
}

########################
#                      #
# Fallback Definitions #
#                      #
########################

# -----------------------------------------------------------------------------
sub Fallback001_P933S99 {

    my $rc = 0;

    # Mark that the Job has failed
    $::JOB_PARAMS{'JOBSTATUS'} = "FAILED";

    if (exists $::JOB_PARAMS{'KPI_PID'} && $::JOB_PARAMS{'KPI_PID'} != 0) {
        $rc = General::Playlist_Operations::execute_task( { "task-ref" => \&Stop_Background_KPI_Collection_P933S03T01 } );
        return $rc if $rc < 0;
    }

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
        'CMYP_NEW_PASSWORD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control which new password is used for e.g. fetching
KPI data from the CMYP CLI when the old password has expired due to the time shift.

If this job variable is not set then value is fetched from the 'expert' user in
the network config file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CMYP_PASSWORD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control which password is used for e.g. fetching
KPI data from the CMYP CLI.

If this job variable is not set then value is fetched from the 'expert' user in
the network config file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CMYP_USER' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be used to control which user is used for e.g. fetching
KPI data from the CMYP CLI.

If this job variable is not set then value is fetched from the 'expert' user in
the network config file.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P933_ENTRY_DIRECTORY' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter defines in which directory the generated KPI files for the collected
entry parameters are stored.

If not specified then the directory 'logfiles/kpi_statistics/entry_statistics'
relative to the job workspace directory will be used.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P933_EXIT_DIRECTORY' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter defines in which directory the generated KPI files for the collected
exit parameters are stored.

If not specified then the directory 'logfiles/kpi_statistics/exit_statistics'
relative to the job workspace directory will be used.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P933_HEALTH_CHECK_FILE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter defines if a special file with special variable values should be
used when performing KPI_VERDICT check.

If not specified then default values will be used.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P933_OUTPUT_DIRECTORY' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter defines in which directory the generated KPI files are stored.

If not specified then the directory 'logfiles/kpi_statistics/' relative to the
job workspace directory will be used.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P933_PAST' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "1m",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter defines how far back to fetch the KPI values.

If the <value> is an integer value then this is how many sets of KPI values are
fetched from the past history with an interval of 'P933_RESOLUTION' seconds.
If the <value> is an integer value followed by the character 's', 'm' or 'h'
then this is how far back in time the collection is done specified in seconds,
minutes or hours, between each collection there is a delay of 'P933_RESOLUTION'
seconds.

If not specified then the value '1m' will be used i.e. to fetch KPI counters
for the last minute.
EOF
            'validity_mask' => '(\d+|\d+[smh])',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P933_RESOLUTION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "15",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter defines what resolution to have between the fetched KPI values.

The <value> should be specified in number of seconds between each KPI collection.

If not specified then the default value will be used i.e. to have a resolution of
15 seconds between each KPI collection.
So if none of the 'P933_PAST' or 'P933_RESOLUTION' is given then it will basically
fetch 4 KPI values with 15 seconds between each.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P933_SUCCESS_RATE_DESCRIPTION' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => ".+",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified then this description will be used for selecting which KPI success
rate KPI values to check after each executed test case.

If specified it should contain the KPI description string excluding the string
"Success Rate [%]" which is automatically appended to the specified value.
This value will be used to check if the success rate is equal to or higher than the
P933_SUCCESS_RATE_THRESHOLD value in which case the check is successful, otherwise
the check will fail.
If not specified then the value to use will be ".+" i.e. any string that ends with
"Success Rate [%]" will be checked.

The result of all KPI checks are combined at the end of the Playlist so if one or
more KPI checks had a failed result then the overall result will be failed, otherwise
if all KPI checks were successful the the overall check is also successful.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P933_SUCCESS_RATE_THRESHOLD' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "95",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "integer",   # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified and set to an integer value then this is limit to use to check if the
success rate is below this limit which would cause the check to fail.
If not specified then the default value will be used.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P933_SUMMARY_MESSAGE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter defines what message is printed when displaying the collected KPI
summary.
If not specified then no message is printed before the KPI statistics.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P933_TASK' => {
            'case_check'    => "uppercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter defines what task it performes when calling this playlist.

Valid values are:

  KPI_COMPARE
    When called with this value it will compare two KPI_SNAPSHOT collections
    and show the differences.

  KPI_SNAPSHOT
    When called with this value it will fetch KPI data from the stored history,
    see also P933_PAST and P933_RESOLUTION.

  KPI_VERDICT
    When called with this value it will check the KPI success rate to make sure
    it is within specific levels.

  START_KPI_COLLECTION:
    When called with this value it will check that Job variables has been set
    properly and then it will start the KPI collection in the background.

  STOP_KPI_COLLECTION:
    When called with this value it will stop the KPI collection and print a
    summary of the collected counters.
EOF
            'validity_mask' => '(KPI_COMPARE|KPI_SNAPSHOT|KPI_VERDICT|START_KPI_COLLECTION|STOP_KPI_COLLECTION)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P933_VERDICT_RESULT' => {
            'case_check'    => "uppercase", # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter is set by this playlist when called with P933_TASK=KPI_VERDICT and
it will contain the result of the KPI verdict check and will contain the
following values:

    SUCCESS: When the KPI Verdict check was successful and all KPI values are
             equal to or above the theshold value.

    FAILURE: When the KPI Verdict check failed because the all KPI values are
             below the threshold value., or if fetching the KPI values failed.

    UNKNOWN: When something went wrong with the check.

NOTE: This parameter should never be changed by the user, it will be set by this
playlist and returned to the caller.
EOF
            'validity_mask' => '(SUCCESS|FAILURE|UNKNOWN)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'P933_VERDICT_IGNORE_FAILURE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter defines if the KPI verdict check should ignore (=yes) the result
and always return a successful return code (=0) or if failue of the check (=no)
should return back a non-zero return code.
Regardless of the value of this parameter, the parameter P933_VERDICT_RESULT will
always show the true result of the check.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SC_NAMESPACE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
The name to use for the SC namespace if you want to override the parameter from
the network configuration file called 'sc_namespace'.
If you don't specify this parameter then it will take the value from the network
configuration file.
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

This Playlist performs KPI operations like start and stop of KPI collection.
When the KPI collection is stopped then it prints out a summary of the collected
KPI counters.

This is a sub-playlist and it should never be called directly from the
execute_playlist.pl script.

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
