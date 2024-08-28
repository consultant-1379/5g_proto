package General::Playlist_Operations;

################################################################################
#
#  Author   : eustone, everhel
#
#  Revision : 1.96
#  Date     : 2024-01-25 14:18:13
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2017-2024
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
use warnings;

use Exporter qw(import);

our @EXPORT_OK = qw(
    execute_step
    execute_task
    is_mandatory_network_config_loaded
    is_optional_network_config_loaded
    job_parameters_read
    job_parameters_write
    parse_playlist_variables
    print_info_about_job_variables
    print_used_job_variables
    print_used_network_config_variables
    register_workaround_task
    show_checkpoint
    show_checkpoint_message
    );

use constant {
    RC_SUCCESS          => 0,
    RC_FALLBACK         => -1,
    RC_GRACEFUL_EXIT    => -2,
    RC_PLAYLISTOUT      => -3,
    RC_RERUN_STEP       => -4,
    RC_RERUN_TASK       => -5,
    RC_STEPOUT          => -6,
    RC_TASKOUT          => -7,
};

use Cwd qw(abs_path cwd);
use B qw(svref_2object);    # To show name of a reference
use General::File_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::State_Machine;

#
# Package variables
#

my $fallback_active = 0;
my $fallback_message_printed = 0;
our $ignored_errors = 0;            # How many times a task has failed but the user choose to ignore the error
our $last_task_name = "";           # The name of the last started task
our $last_task_start_time = 0;      # The start time of the last started task (used for SIGTERM handling)
our $log_step_task_start = 1;       # If start of a step and task should be logged to the system log
our $log_step_task_stop = 1;        # If stop of a step and task should be logged to the system log

my %rc_description = (
     0 => " (RC_SUCCESS)",
    -1 => " (RC_FALLBACK)",
    -2 => " (RC_GRACEFUL_EXIT)",
    -3 => " (RC_PLAYLISTOUT)",
    -4 => " (RC_RERUN_STEP)",
    -5 => " (RC_RERUN_TASK)",
    -6 => " (RC_STEPOUT)",
    -7 => " (RC_TASKOUT)",
);

# Signal handling
my %signal_counter;

# This variable is set to 1 when the user interrupted the execution of a task of a step
# and can be checked by the playlist execution.
our $user_interrupted_execution = 0;

# Variable that can be changed to have either a detailed select menu
# or a simple select menu when user feedback is needed after a failed
# task.
# The values are:
#   0 : When a detailed select menu is wanted which can take any
#       return code value supported by the playlist (0, >0, -1 to -7.
#   1 : When a simple select meny is wanted which can take only specific
#       values which is translated into a return code value.
#       'c' for CONTINUE which will be translated into RC_TASKOUT (-7).
#       'f' for FALLBACK which will be translated into RC_FALLBACK (-1).
#       'r' for RERUN TASK which will be translated into RC_RERUN_TASK (-5).
#       'q' for QUIT which will be translated into RC_GRACEFUL_EXIT (-2).
our $simple_select_menu = 1;

# Job summary
our @step_task_summary = ();

# Contains a list of tasks that has been reported as containing workaround
# logic that should at some point in time be removed when no longer needed.
our @workaround_tasks = ();

# -----------------------------------------------------------------------------
# Execute a playlist step and depending on the return code either return
# normally, rerun the step, execute fallback sub routine if specified (not undef).
#
# Input variables:
#    - subroutine reference for step subroutine to execute (mandatory).
#    - subroutine reference for fallback subroutine to execute when a return
#      code of RC_FALLBACK is returned (mandatory but can be 'undef').
#
# Return values:
#    Return code value coming from the called step subroutine.
#    If RC_FALLBACK is returned then this value will always be returned.
#
# -----------------------------------------------------------------------------
sub execute_step {
    my $step_ref = shift;
    my $fallback_ref = shift;

    # Get name of called step and fallback
    my $step_name = sub_name($step_ref);
    my $fallback_name = sub_name($fallback_ref);

    my $message;
    my $rc;
    my $rc_descr;
    my $signal_int_captured;
    my $signal_quit_captured;
    my $signal_usr1_captured;
    my $signal_usr2_captured;
    my $start_time;
    my $stop_time;
    my $total_time;

RERUN_STEP:

    # Get start time
    $start_time = General::OS_Operations::detailed_time();
    # Log the execution time to file
    General::Logging::log_execution_times($start_time,'start','step',$step_name);

    $user_interrupted_execution = 0;

    # Print name of called step
    General::Logging::log_user_progress_message("                 Starting Step: $step_name\n");

    # Log start of step to system log if wanted
    `logger -t DAFT 'PID=$$ Starting Step: $step_name'` if $log_step_task_start;

    #########################
    #                       #
    # Execute the Step code #
    #                       #
    #########################
    eval { $rc = &$step_ref(); };
    if ($@) {
        print "\n$@\n";
        General::Logging::log_user_error_message("Some bad error occurred, exit execution in controlled way\n");
        $rc = RC_FALLBACK;
    }

    # Check if the user want to interrupt the execution
    $signal_int_captured = General::OS_Operations::signal_captured("INT");
    $signal_quit_captured = General::OS_Operations::signal_captured("QUIT");
    $signal_usr1_captured = General::OS_Operations::signal_captured("USR1");
    $signal_usr2_captured = General::OS_Operations::signal_captured("USR2");
    if ($signal_usr1_captured > 0) {
        General::Logging::log_user_warning_message("Playlist step detected that the user interrupted the execution by sending signal SIGUSR1 to process id $$\n");
        $user_interrupted_execution = 1;
        $rc = RC_FALLBACK;
    } elsif ($signal_usr2_captured > 0) {
        General::Logging::log_user_warning_message("Playlist step detected that the user interrupted the execution by sending signal SIGUSR2 to process id $$\n");
        $user_interrupted_execution = 1;
        $rc = RC_GRACEFUL_EXIT;
    } elsif ($signal_int_captured > 0) {
        General::Logging::log_user_warning_message("Playlist step detected that the user interrupted the execution by pressing CTRL-C or sending signal SIGINT to process id $$\n");
        $user_interrupted_execution = 1;
        $rc = 1;
    } elsif ($signal_quit_captured > 0) {
        General::Logging::log_user_warning_message("Playlist step detected that the user interrupted the execution by pressing CTRL-\\ or sending signal SIGQUIT to process id $$\n");
        $user_interrupted_execution = 1;
        $rc = 1;
    }

    # Print return code and description
    $rc_descr = exists $rc_description{$rc} ? $rc_description{$rc} : "";
    General::Logging::log_user_progress_message("                 Finished Step: $step_name\n                   Return Code: $rc$rc_descr\n");

    # Log stop of step to system log if wanted
    `logger -t DAFT 'PID=$$ Finished Step: $step_name, Return Code: $rc$rc_descr'` if $log_step_task_stop;

    # Get stop time
    $stop_time = General::OS_Operations::detailed_time();
    $total_time = sprintf "%f", $stop_time - $start_time;

    # Store summary information
    push @step_task_summary, sprintf "%13.6f seconds  %3d%-20s  Step  %s", $total_time, $rc, $rc_descr, $step_name;

    # Log the execution time to file
    General::Logging::log_execution_times($stop_time,'stop','step',$step_name);

    if ($rc == RC_FALLBACK) {
        if ($fallback_message_printed == 0) {
            General::Message_Generation::print_message_box(
                {
                    "messages"      => [ "", "FALLBACK logic invoked, if defined", "" ],
                    "align-text"    => "left",
                    "type"          => "error",
                    "return-output" => \$message,
                }
            );
            General::Logging::log_user_error_message("\n$message");
            $fallback_message_printed = 1;
        }

        if (defined $fallback_ref) {
            General::Logging::log_user_progress_message("             Starting Fallback: $fallback_name\n");
            my $fbrc = &$fallback_ref();
            $rc_descr = exists $rc_description{$fbrc} ? $rc_description{$fbrc} : "";
            General::Logging::log_user_progress_message("             Finished Fallback: $fallback_name\n          Fallback Return Code: $fbrc$rc_descr\n");
        }

    } elsif ($rc == RC_GRACEFUL_EXIT) {
        # Let the main playlist sub routine handle what happens next

    } elsif ($rc == RC_PLAYLISTOUT) {
        # Let the main playlist sub routine handle what happens next

    } elsif ($rc == RC_RERUN_STEP) {
        goto RERUN_STEP;

    } elsif ($rc == RC_RERUN_TASK) {
        # This should not reach here, if it does it's a fault in the calling of
        # the task which should handle this case.
        # Modify the return code to be 0 (success)
        $rc = 0;

    } elsif ($rc == RC_STEPOUT) {
        # Modify the return code to be 0 (success)
        $rc = 0;

    } elsif ($rc == RC_TASKOUT) {
        # This should not reach here, if it does it's a fault in the calling of
        # the task which should handle this case.
        # Modify the return code to be 0 (success)
        $rc = 0;
    }

    # Return the code to the calling instance
    return $rc;
}

# -----------------------------------------------------------------------------
# Execute a playlist task and depending on the return code either return
# normally, rerun the task, rerun the step.
#
# Input variables:
#    - subroutine reference for task subroutine to execute (mandatory).
#
# Return values:
#    Return code value coming from the called task subroutine.
#
# -----------------------------------------------------------------------------
sub execute_task {
    my %task_params = %{$_[0]};

    my $task_ref = defined $task_params{"task-ref"} ? $task_params{"task-ref"} : undef;
    my $dry_run_job = defined $task_params{"dry-run-job"} ? $task_params{"dry-run-job"} : "no";
    my $dry_run_task = defined $task_params{"dry-run-task"} ? $task_params{"dry-run-task"} : 0;

    # Get name of called task
    my $task_name = sub_name($task_ref);

    my $answer;
    my $rc;
    my $rc_descr;
    my $epochmicroseconds;
    my $epochseconds;
    my $message;
    my $signal_int_captured;
    my $signal_quit_captured;
    my $signal_usr1_captured;
    my $signal_usr2_captured;
    my $start_time;
    my $stop_time;
    my $total_time;
    my $type;
    my $user_answer;
    my $valid;
    my $valid_display;

    if ($task_name =~ /.+::main$/) {
        $type = "Main";
    } else {
        $type = "Task";
    }

RERUN_TASK:

    # Get start time
    $start_time = General::OS_Operations::detailed_time();
    $last_task_start_time = $start_time;
    $last_task_name = $task_name;
    # Log the execution time to file
    General::Logging::log_execution_times($start_time,'start','task',$task_name);

    $user_interrupted_execution = 0;

    # Print name of called step
    General::Logging::log_user_progress_message("                 Starting Task: $task_name\n");

    # Log start of step to system log if wanted
    `logger -t DAFT 'PID=$$ Starting Task: $task_name'` if $log_step_task_start;

    # Check if task should be executed or not depending on current state of the task
    $rc = General::State_Machine::get_state_action( { "task-name" => $task_name, "dry-run-job" => $dry_run_job, "dry-run-task" => $dry_run_task } );
    if ($rc == 0) {
        # Task should be executed
        General::State_Machine::state_update( { "task-name" => $task_name, "state" => "triggered" } );

        #########################
        #                       #
        # Execute the Task code #
        #                       #
        #########################
        eval { $rc = &$task_ref(); };
        if ($@) {
            print "\n$@\n";
            General::Logging::log_user_error_message("Some bad error occurred, exit execution in controlled way\n");
            $rc = RC_FALLBACK;
        }

        # Check if we need to modify the return code
        General::State_Machine::get_modified_return_code( { "task-name" => $task_name, "return-code" => \$rc } );
    }

    # Check if the user want to interrupt the execution
    $signal_int_captured = General::OS_Operations::signal_captured("INT");
    $signal_quit_captured = General::OS_Operations::signal_captured("QUIT");
    $signal_usr1_captured = General::OS_Operations::signal_captured("USR1");
    $signal_usr2_captured = General::OS_Operations::signal_captured("USR2");
    if ($signal_usr1_captured > 0) {
        General::Logging::log_user_warning_message("Playlist task detected that the user interrupted the execution by sending signal SIGUSR1 to process id $$\n");
        General::State_Machine::state_update( { "task-name" => $task_name, "state" => "user_interrupt" } );
        $user_interrupted_execution = 1;
        $rc = RC_FALLBACK;
    } elsif ($signal_usr2_captured > 0) {
        General::Logging::log_user_warning_message("Playlist task detected that the user interrupted the execution by sending signal SIGUSR2 to process id $$\n");
        General::State_Machine::state_update( { "task-name" => $task_name, "state" => "user_interrupt" } );
        $user_interrupted_execution = 1;
        $rc = RC_GRACEFUL_EXIT;
    } elsif ($signal_int_captured > 0) {
        General::Logging::log_user_warning_message("Playlist task detected that the user interrupted the execution by pressing CTRL-C or sending signal SIGINT to process id $$\n");
        General::State_Machine::state_update( { "task-name" => $task_name, "state" => "user_interrupt" } );
        $user_interrupted_execution = 1;
        $rc = 1;
    } elsif ($signal_quit_captured > 0) {
        General::Logging::log_user_warning_message("Playlist task detected that the user interrupted the execution by pressing CTRL-\\ or sending signal SIGQUIT to process id $$\n");
        General::State_Machine::state_update( { "task-name" => $task_name, "state" => "user_interrupt" } );
        $user_interrupted_execution = 1;
        $rc = 1;
    }

    # Print return code and description
    $rc_descr = exists $rc_description{$rc} ? $rc_description{$rc} : " (RC_FAILURE)";
    General::Logging::log_user_progress_message("                 Finished Task: $task_name\n                   Return Code: $rc$rc_descr\n");

    # Log stop of step to system log if wanted
    `logger -t DAFT 'PID=$$ Finished Task: $task_name, Return Code: $rc$rc_descr'` if $log_step_task_stop;

    # Get stop time
    $stop_time = General::OS_Operations::detailed_time();
    $total_time = sprintf "%f", $stop_time - $start_time;

    # Store summary information
    push @step_task_summary, sprintf "%13.6f seconds  %3d%-20s  %-4s  %s", $total_time, $rc, $rc_descr, $type, $task_name;

    if ($rc == RC_FALLBACK) {
        # Let the step sub routine handle what happens next.
        # Set flag that fallback is active to not allow the user to select fallback again.
        $fallback_active = 1;
        General::State_Machine::state_update( { "task-name" => $task_name, "state" => "fallback" } );

        # Update the global JOB_STATUS array
        push @::JOB_STATUS, "(x) Task '$task_name' failed with rc=$rc";

    } elsif ($rc == RC_GRACEFUL_EXIT) {
        # Let the step sub routine handle what happens next
        General::State_Machine::state_update( { "task-name" => $task_name, "state" => "graceful_exit" } );

        # Update the global JOB_STATUS array
        push @::JOB_STATUS, "(x) Task '$task_name' failed with rc=$rc";

    } elsif ($rc == RC_PLAYLISTOUT) {
        # Let the step sub routine handle what happens next
        General::State_Machine::state_update( { "task-name" => $task_name, "state" => "playlistout" } );

    } elsif ($rc == RC_RERUN_STEP) {
        # Let the step sub routine handle what happens next
        General::State_Machine::state_update( { "task-name" => $task_name, "state" => "rerun_step" } );

        # Delete all states for tasks in the step to be re-run
        my $step_name = $task_name;
        $step_name =~ s/^(.+P\d\d\dS\d\d)T\d\d$/$1/;
        General::State_Machine::state_remove( {"task-name" => $step_name } );

    } elsif ($rc == RC_RERUN_TASK) {
        General::State_Machine::state_update( { "task-name" => $task_name, "state" => "rerun" } );

        # Log the execution time to file
        $stop_time = General::OS_Operations::detailed_time();
        General::Logging::log_execution_times($stop_time,'stop','task',$task_name);

        goto RERUN_TASK;

    } elsif ($rc == RC_STEPOUT) {
        # Let the step sub routine handle what happens next
        General::State_Machine::state_update( { "task-name" => $task_name, "state" => "stepout" } );

    } elsif ($rc == RC_TASKOUT) {
        # Modify the return code to be 0 (success)
        $rc = 0;
        General::State_Machine::state_update( { "task-name" => $task_name, "state" => "taskout" } );

    }
    elsif ($rc == 0) {
        # This is all good
    }
    elsif ($rc > 0) {
        # Non-zero return code considered as a failure, ask the user what to do next

        # Update the global JOB_STATUS array
        push @::JOB_STATUS, "(x) Task '$task_name' failed with rc=$rc";

        if ($simple_select_menu == 0) {
            if ($fallback_active == 0) {
                General::Message_Generation::print_message_box(
                    {
                        "header-text" => "Task Error Detected (rc=$rc)",
                        "align-text"  => "left",
                        "max-length"  => 0,
                        "return-output" => \$message,
                        "messages"    => [
                            "",
                            "Please answer how to proceed by selecting one of the following options:",
                            "",
                            sprintf("   %s : SUCCESSFUL TASK", "0"),
                            "       Mark Task as successful and Continue normal playlist execution.",
                            sprintf("  %s : FAILED TASK", ">0"),
                            "       Mark Task as failed and Continue normal playlist execution.",
                            sprintf("  %s : RC_FALLBACK", RC_FALLBACK),
                            sprintf("   %s : FALLBACK", "f"),
                            "       Perform a fallback which will exit script execution and do a",
                            "       proper cleanup of files uploaded to the Cluster etc.",
                            "       It will in some cases also collect trouble shooting logs to",
                            "       make it easier to find out why the job failed and might also",
                            "       trigger automatic recovery of the Cluster if this is wanted.",
                            "       It will not be possible to re-run the same job again.",
                            sprintf("  %s : RC_GRACEFUL_EXIT", RC_GRACEFUL_EXIT),
                            sprintf("   %s : QUIT", "q"),
                            "       Perform a graceful exit which will exit script execution but not",
                            "       do any cleanup.",
                            "       It will be possible to re-run the same job again and continue",
                            "       execution from the task where it exited by starting a new job",
                            "       giving the old workspace directory with parameter '-r'.",
                            sprintf("  %s : RC_PLAYLISTOUT", RC_PLAYLISTOUT),
                            "       Perform an exit from current executing Playlist.",
                            sprintf("  %s : RC_RERUN_STEP", RC_RERUN_STEP),
                            "       Rerun the current executing Step again, which will cause all",
                            "       Tasks in Step to be executed again.",
                            sprintf("  %s : RC_RERUN_TASK", RC_RERUN_TASK),
                            sprintf("   %s : RERUN TASK", "r"),
                            "       Rerun the current executing Task again.",
                            sprintf("  %s : RC_STEPOUT", RC_STEPOUT),
                            "       Perform an exit from current executing Step, Execution will",
                            "       continue with next Step.",
                            sprintf("  %s : RC_TASKOUT", RC_TASKOUT),
                            sprintf("   %s : CONTINUE", "c"),
                            "       Perform an exit from current executing Task, Execution will",
                            "       continue with next Task or Step.",
                            "       Selecting this option means that you know what you are doing,",
                            "       and it might still cause failures further down.",
                            "",
                        ],
                    }
                );

                $valid = sprintf '^(%s|%s|%s|%s|%s|%s|%s|\d+|c|f|r|q)$', RC_FALLBACK, RC_GRACEFUL_EXIT, RC_PLAYLISTOUT, RC_RERUN_STEP, RC_RERUN_TASK, RC_STEPOUT, RC_TASKOUT;
                $valid_display = sprintf '(%s|%s|%s|%s|%s|%s|%s|\d+|c|f|r|q)', RC_FALLBACK, RC_GRACEFUL_EXIT, RC_PLAYLISTOUT, RC_RERUN_STEP, RC_RERUN_TASK, RC_STEPOUT, RC_TASKOUT;
            } else {
                # Fallback is already active, don't allow the user to invoke fallback again
                General::Message_Generation::print_message_box(
                    {
                        "header-text" => "Task Error Detected (rc=$rc)",
                        "align-text"  => "left",
                        "max-length"  => 0,
                        "return-output" => \$message,
                        "messages"    => [
                            "",
                            "Please answer how to proceed by selecting one of the following options:",
                            "",
                            sprintf("   %s : SUCCESSFUL TASK", "0"),
                            "       Mark Task as successful and Continue normal playlist execution.",
                            sprintf("  %s : FAILED TASK", ">0"),
                            "       Mark Task as failed and Continue normal playlist execution.",
                            sprintf("  %s : RC_GRACEFUL_EXIT", RC_GRACEFUL_EXIT),
                            sprintf("   %s : QUIT", "q"),
                            "       Perform a graceful exit which will exit script execution but not",
                            "       do any cleanup.",
                            "       It will be possible to re-run the same job again and continue",
                            "       execution from the task where it exited by starting a new job",
                            "       giving the old workspace directory with parameter '-r'.",
                            sprintf("  %s : RC_PLAYLISTOUT", RC_PLAYLISTOUT),
                            "       Perform an exit from current executing Playlist.",
                            sprintf("  %s : RC_RERUN_STEP", RC_RERUN_STEP),
                            "       Rerun the current executing Step again, which will cause all",
                            "       Tasks in Step to be executed again.",
                            sprintf("  %s : RC_RERUN_TASK", RC_RERUN_TASK),
                            sprintf("   %s : RERUN TASK", "r"),
                            "       Rerun the current executing Task again.",
                            sprintf("  %s : RC_STEPOUT", RC_STEPOUT),
                            "       Perform an exit from current executing Step, Execution will",
                            "       continue with next Step.",
                            sprintf("  %s : RC_TASKOUT", RC_TASKOUT),
                            sprintf("   %s : CONTINUE", "c"),
                            "       Perform an exit from current executing Task, Execution will",
                            "       continue with next Task or Step.",
                            "       Selecting this option means that you know what you are doing,",
                            "       and it might still cause failures further down.",
                            "",
                        ],
                    }
                );

                $valid = sprintf '^(%s|%s|%s|%s|%s|%s|\d+|c|r|q)$', RC_GRACEFUL_EXIT, RC_PLAYLISTOUT, RC_RERUN_STEP, RC_RERUN_TASK, RC_STEPOUT, RC_TASKOUT;
                $valid_display = sprintf '(%s|%s|%s|%s|%s|%s|\d+|c|r|q)', RC_GRACEFUL_EXIT, RC_PLAYLISTOUT, RC_RERUN_STEP, RC_RERUN_TASK, RC_STEPOUT, RC_TASKOUT;
            }

            General::Logging::log_user_error_message(sprintf "A potential error was detected during Task execution (rc=%d), ask the user how to proceed!\nFor detailed log see: %s\n\n%s\nAnswers can also be input from another terminal on the same server using the 'input.pipe' file.\nFor example like this:\n\necho c > %s\n\nThe command must be entered from the host: %s", $rc, General::Logging::log_filename(), $message, $General::OS_Operations::temporary_input_filename, $::JOB_PARAMS{'_USER_HOSTNAME'});

            # Update the job parameters file on disk
            job_parameters_write($::JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%::JOB_PARAMS);

            General::Logging::log_user_prompt("Enter one of $valid_display as answer: ");
            $user_answer = General::OS_Operations::get_user_validated_input("", $valid, 0, 0);     # 0=Not case sensitive, 0=Not hidden
        } else {
            if ($fallback_active == 0) {
                General::Message_Generation::print_message_box(
                    {
                        "header-text" => "Task Error Detected (rc=$rc)",
                        "align-text"  => "left",
                        "max-length"  => 0,
                        "return-output" => \$message,
                        "messages"    => [
                            "",
                            "Please answer how to proceed by selecting one of the following options:",
                            "",
                            sprintf("  %s : CONTINUE", "c"),
                            "      Mark Task as successful and Continue normal playlist execution.",
                            "      Selecting this option means that you know what you are doing,",
                            "      and it might still cause failures further down.",
                            "",
                            sprintf("  %s : RERUN TASK", "r"),
                            "      Rerun the current executing Task again.",
                            "",
                            sprintf("  %s : FALLBACK", "f"),
                            "      Perform a fallback which will exit script execution and do a",
                            "      proper cleanup.",
                            "      It will not be possible to re-run the same job again.",
                            "",
                            # We hide the QUIT option from normal users because we want them to
                            # do a fallback to have proper cleanup of the job.
                            #
                            # sprintf("  %s : QUIT", "q"),
                            # "      Perform a graceful exit which will exit script execution but not",
                            # "      do any cleanup.",
                            # "      It will be possible to re-run the same job again and continue",
                            # "      execution from the task where it exited by starting a new job",
                            # "      giving the old workspace directory with parameter '-r'.",
                            # "",
                        ],
                    }
                );

                $valid = sprintf '^(%s|%s|%s|%s|%s|%s|%s|\d+|c|f|r|q)$', RC_FALLBACK, RC_GRACEFUL_EXIT, RC_PLAYLISTOUT, RC_RERUN_STEP, RC_RERUN_TASK, RC_STEPOUT, RC_TASKOUT;
                $valid_display = '(c|r|f)';
                General::Logging::log_user_error_message(sprintf "A potential error was detected during Task execution (rc=%d), ask the user how to proceed!\nFor detailed log see: %s\n\n%s\nAnswers can also be input from another terminal on the same server using the 'input.pipe' file.\nFor example like this:\n\n  echo c > %s\n  echo r > %s\n  echo f > %s\n\nThe command must be entered from the host: %s", $rc, General::Logging::log_filename(), $message, $General::OS_Operations::temporary_input_filename, $General::OS_Operations::temporary_input_filename, $General::OS_Operations::temporary_input_filename, $::JOB_PARAMS{'_USER_HOSTNAME'});
            } else {
                # Fallback is already active, don't allow the user to invoke fallback again
                General::Message_Generation::print_message_box(
                    {
                        "header-text" => "Task Error Detected (rc=$rc)",
                        "align-text"  => "left",
                        "max-length"  => 0,
                        "return-output" => \$message,
                        "messages"    => [
                            "",
                            "Please answer how to proceed by selecting one of the following options:",
                            "",
                            sprintf("  %s : CONTINUE", "c"),
                            "      Mark Task as successful and Continue normal playlist execution.",
                            "      Selecting this option means that you know what you are doing,",
                            "      and it might still cause failures further down.",
                            "",
                            sprintf("  %s : RERUN TASK", "r"),
                            "      Rerun the current executing Task again.",
                            "",
                            # We hide the QUIT option from normal users because we want them to
                            # do a fallback to have proper cleanup of the job.
                            #
                            # sprintf("  %s : QUIT", "q"),
                            # "      Perform a graceful exit which will exit script execution but not",
                            # "      do any cleanup.",
                            # "      It will be possible to re-run the same job again and continue",
                            # "      execution from the task where it exited by starting a new job",
                            # "      giving the old workspace directory with parameter '-r'.",
                            # "",
                        ],
                    }
                );

                $valid = sprintf '^(%s|%s|%s|%s|%s|%s|\d+|c|r|q)$', RC_GRACEFUL_EXIT, RC_PLAYLISTOUT, RC_RERUN_STEP, RC_RERUN_TASK, RC_STEPOUT, RC_TASKOUT;
                $valid_display = '(c|r)';
                General::Logging::log_user_error_message(sprintf "A potential error was detected during Task execution (rc=%d), ask the user how to proceed!\nFor detailed log see: %s\n\n%s\nAnswers can also be input from another terminal on the same server using the 'input.pipe' file.\nFor example like this:\n\n  echo c > %s\n  echo r > %s\n\nThe command must be entered from the host: %s", $rc, General::Logging::log_filename(), $message, $General::OS_Operations::temporary_input_filename, $General::OS_Operations::temporary_input_filename, $::JOB_PARAMS{'_USER_HOSTNAME'});
            }

            # Update the job parameters file on disk
            job_parameters_write($::JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%::JOB_PARAMS);

            General::Logging::log_user_prompt("Enter one of $valid_display as answer: ");     # 0=Not case sensitive
            $user_answer = General::OS_Operations::get_user_validated_input("", "^$valid\$", 0, 0);     # 0=Not case sensitive, 0=Not hidden
        }

        # Clean CTRL-C counter in case the user pressed CTRL-C while answering
        # the error prompt
        General::OS_Operations::signal_captured("INT");

        if (defined $user_answer) {
            $answer = lc($user_answer);
            if ($answer eq "c") {
                $answer = RC_TASKOUT;
            } elsif ($answer eq "f") {
                $answer = RC_FALLBACK;
                # Set flag that fallback is active to not allow the user to select fallback again.
                $fallback_active = 1;
            } elsif ($answer eq "r") {
                $answer = RC_RERUN_TASK;
            } elsif ($answer eq "q") {
                $answer = RC_GRACEFUL_EXIT;
                if ($simple_select_menu == 1) {
                    General::Logging::log_user_warning_message("Valid but NOT DOCUMENTED answer provided");
                }
            } elsif ($user_answer =~ /^(-1|-2|-3|-4|-5|-6|-7|\d+)$/) {
                $answer = $user_answer;
                if ($simple_select_menu == 1) {
                    General::Logging::log_user_warning_message("Valid but NOT DOCUMENTED answer provided");
                }
                if ($user_answer == -1) {
                    # Set flag that fallback is active to not allow the user to select fallback again.
                    $fallback_active = 1;
                }
            } else {
                $answer = 1;
            }
            $rc_descr = exists $rc_description{$answer} ? $rc_description{$answer} : " (RC_FAILURE)";
            General::Logging::log_user_message("Answer from user was: $user_answer$rc_descr\n");
        } else {
            # No console or named pipe present that can be used to provide answers,
            # just automatically answer with fallback or failure since there is no
            # way to get an answer anyway.
            # This is a special case when e.g. Jenkins is used to execute the playlist.
            if ($fallback_active == 0) {
                # Fallback is not currently active, so answer it with RC_FALLBACK
                $answer = RC_FALLBACK;
            } else {
                # Fallback is already active, answer it with failure to continue with next task or step
                $answer = 1;
            }
            $rc_descr = exists $rc_description{$answer} ? $rc_description{$answer} : " (RC_FAILURE)";
            General::Logging::log_user_message("Automatic answer was: $answer$rc_descr\n");
        }

        if ($answer == RC_RERUN_TASK) {
            General::State_Machine::state_update( { "task-name" => $task_name, "state" => "rerun" } );

            # Update the global JOB_STATUS array, by removing the last entry
            pop @::JOB_STATUS;     # i.e. remove "(x) Task '$task_name' failed with rc=$rc";

            # Log the execution time to file
            $stop_time = General::OS_Operations::detailed_time();
            General::Logging::log_execution_times($stop_time,'stop','task',$task_name);

            goto RERUN_TASK;

        } elsif ($answer == RC_RERUN_STEP) {
            # Let the step sub routine handle what happens next
            General::State_Machine::state_update( { "task-name" => $task_name, "state" => "rerun_step" } );

            # Update the global JOB_STATUS array, by removing the last entry
            pop @::JOB_STATUS;     # i.e. remove "(x) Task '$task_name' failed with rc=$rc";

            # Delete all tasks in the step
            my $step_name = $task_name;
            $step_name =~ s/^(.+P\d\d\dS\d\d)T\d\d$/$1/;
            General::State_Machine::state_remove( {"task-name" => $step_name } );

        } elsif ($answer == RC_TASKOUT) {
            # Modify the answer (and return code) to be 0 (success)
            $answer = 0;
            General::State_Machine::state_update( { "task-name" => $task_name, "state" => "taskout" } );

        } elsif ($answer == RC_FALLBACK) {
            # Let the step sub routine handle what happens next.
            # Set flag that fallback is active to not allow the user to select fallback again.
            $fallback_active = 1;
            General::State_Machine::state_update( { "task-name" => $task_name, "state" => "fallback" } );
        }

        if ($answer == 0) {
            # User choose to ignore the error and continue the execution, so step the counter
            $ignored_errors++;
        }

        $rc = $answer;
    }

    if ($rc == 0) {
        General::State_Machine::state_update( { "task-name" => $task_name, "state" => "success" } );
    } elsif ($rc > 0) {
        General::State_Machine::state_update( { "task-name" => $task_name, "state" => "failed" } );
    }

    # Log the execution time to file
    $stop_time = General::OS_Operations::detailed_time();
    General::Logging::log_execution_times($stop_time,'stop','task',$task_name);

    $last_task_start_time = 0;
    $last_task_name = "";

    # Return the code to the calling instance
    return $rc;
}

# -----------------------------------------------------------------------------
# Check if a mandatory network configuration file has been loaded.
#
# Input variables:
#    -
#
# Return values:
#    0: If successful and file was loaded
#    1: If failure and no file was loaded
#
# -----------------------------------------------------------------------------
sub is_mandatory_network_config_loaded {
    my $key_cnt = 0;

    for (keys %::NETWORK_CONFIG_PARAMS) {
        $key_cnt++;
    }
    if ($key_cnt > 0) {
        General::Logging::log_user_message("Checking if Network Configuration file is specified by the user.\n  Yes, a file was specified\n");
        return 0;
    } else {
        General::Logging::log_user_error_message("Checking if Network Configuration file is specified by the user.\n  No, You must specify a network configuration file for this Playlist to run\n");
        return 1;
    }
}

# -----------------------------------------------------------------------------
# Check if a optional network configuration file has been loaded.
#
# Input variables:
#    -
#
# Return values:
#    0: If a file was loaded
#    1: If no file was loaded
#
# -----------------------------------------------------------------------------
sub is_optional_network_config_loaded {
    my $key_cnt = 0;

    for (keys %::NETWORK_CONFIG_PARAMS) {
        $key_cnt++;
    }
    if ($key_cnt > 0) {
        General::Logging::log_user_message("Checking if Network Configuration file is specified by the user.\n  Yes, a file was specified\n");
        return 0;
    } else {
        General::Logging::log_user_message("Checking if Network Configuration file is specified by the user.\n  No file was specified\n");
        return 1;
    }
}

# -----------------------------------------------------------------------------
# Read job parameters from file and replace the contents of hash variable that
# that exist with same key.
#
# Input variables:
#    - File name of job parameters file to read (mandatory).
#    - Hash reference to variable to update (mandatory).
#
# Return values:
#    0: If successful
#    1: If failure was detected
#
# -----------------------------------------------------------------------------
sub job_parameters_read {
    my $filename = shift;
    my $hash_ref = shift;

    my @lines = ();
    my $rc;

    # Show the checkpoint instruction from file on the screen
    $rc = General::File_Operations::read_file(
        {
            "filename"            => $filename,
            "output-ref"          => \@lines,
        }
    );
    if ($rc == 0) {
        for (@lines) {
            # Look for xxxxxx=yyyyy lines
            if (/^\s*#/) {
                # Ignore comment lines
            } elsif (/^([^=\s]+)=(.+)/) {
                # Store the key value pairs into hash
                $hash_ref->{$1}=$2;
            }
        }
    }

    # Return the code to the calling instance
    return $rc;
}

# -----------------------------------------------------------------------------
# Write job parameters hash variable to specified file overwriting the content
# of the file.
#
# Input variables:
#    - File name of job parameters file to write (mandatory).
#    - Hash reference to variable to write to file (mandatory).
#
# Return values:
#    0: If successful
#    1: If failure was detected
#
# -----------------------------------------------------------------------------
sub job_parameters_write {
    my $filename = shift;
    my $hash_ref = shift;

    my @lines = ();
    my $rc;

    for my $key (sort keys %{$hash_ref}) {
        push @lines, "$key=$hash_ref->{$key}"
    }

    # Write to file
    $rc = General::File_Operations::write_file(
        {
            "filename"            => $filename,
            "output-ref"          => \@lines,
        }
    );

    # Return the code to the calling instance
    return $rc;
}

# -----------------------------------------------------------------------------
# Parse Playlist specific variables and store the values into another hash.
# The input data is a hash of hashes containing the definitions for variables
# to be parsed and another hash variable containing the job variable where data
# will be read and written to.
# If no variable is found in the job variable matching a mandatory variable in
# the definitions variable then the user will be asked for the value to use.
#
# Input variables:
#    - Definitions hash reference variable with the following layout:
#      %definitions{<variable name>}
#          {'case_check'} = <lowercase|no|uppercase>
#          {'default_value_used'} = <yes|no> Set to 'yes' if the user did not
#               not specifiy the variable as a job input variable and set to
#               'no' if either the variable was given by the user as an input
#               variable or added on request by the playlist.
#          {'default_value'} = string
#          {'mandatory'} = <yes|no>
#          {'type'} = <directory|file|integer|password|string|yesno>
#          {'usage'} = multiline string with help text
#          {'validity_mask'} = Perl regular expression to validate data
#          {'value'} = the value for the variable
#
#     - Job variable hash reference with the following layout:
#     %jobvariable{<variable name>} = the value for the variable
#
#     - Value indicating if parsing of playlist parameters should be shown in
#       the progress log (=1) or not (=0 also default) .
#
# Return values:
#    0: Successful parsing of variables
#    1: Failure to parse at least one of the variables
#
# -----------------------------------------------------------------------------
sub parse_playlist_variables {
    my $definitions = shift;
    my $jobvariable = shift;
    my $show_in_progress = shift;

    my $case_check;
    my $default_value;
    my $default_value_used;
    my $mandatory;
    my $old_SIGINT = $SIG{INT};
    my $old_SIGQUIT = $SIG{QUIT};
    my $old_SIGUSR1 = $SIG{USR1};
    my $old_SIGUSR2 = $SIG{USR2};
    my $rc = 0;
    my $type;
    my $usage;
    my $validity_mask;
    my $var_name;
    my $var_value;

    unless (defined $show_in_progress) {
        $show_in_progress = 0;
    }

    # Add special CTRL-C and CTRL-\ handlers while checking job parameters
    # to cleanly exit program.
    $SIG{INT} = sub { &::cleanup_at_SIGINT(); };
    $SIG{QUIT} = sub { &::cleanup_at_SIGQUIT(); };

    General::Logging::log_user_message("Checking for special playlist variables\n");

    for $var_name (sort keys %$definitions) {
        # Fetch data from %definitions hash
        $case_check = exists $$definitions{$var_name}{'case_check'} ? lc($$definitions{$var_name}{'case_check'}) : "no";
        $default_value = exists $$definitions{$var_name}{'default_value'} ? $$definitions{$var_name}{'default_value'} : "";
        $mandatory = exists $$definitions{$var_name}{'mandatory'} ? lc($$definitions{$var_name}{'mandatory'}) : "no";
        $type = exists $$definitions{$var_name}{'type'} ? lc($$definitions{$var_name}{'type'}) : "string";

        # Check that expected values has been specified for some of the expected attributes
        unless ($case_check =~ /^(uppercase|lowercase|no)$/i) {
            General::Logging::log_user_error_message("Invalid 'case_check=$case_check' specified for parameter '$var_name', only 'uppercase|lowercase|no' allowed, 'no' assumed");
            $case_check = "no";
        }
        unless ($mandatory =~ /^(yes|no)$/i) {
            General::Logging::log_user_error_message("Invalid 'mandatory=$mandatory' specified for parameter '$var_name', only 'yes|no' allowed, 'no' assumed");
            $mandatory = "no";
        }
        unless ($type =~ /^(directory|file|integer|password|string|yesno)$/i) {
            General::Logging::log_user_error_message("Invalid 'type=$type' specified for parameter '$var_name', only 'directory|file|integer|password|string|yesno' allowed, 'string' assumed");
            $type = "string";
        }

        $usage = exists $$definitions{$var_name}{'usage'} ? $$definitions{$var_name}{'usage'} : "\nNo help available\n";
        $validity_mask = exists $$definitions{$var_name}{'validity_mask'} ? $$definitions{$var_name}{'validity_mask'} : "";
        if ($validity_mask eq "") {
            # Not specified by the user, we need to set it according to the type
            if ($type =~ /^directory$/i) {
                if ($mandatory =~ /^yes$/i) {
                    $validity_mask = '.+';
                } else {
                    $validity_mask = '.*';
                }
            } elsif ($type =~ /^file$/i) {
                if ($mandatory =~ /^yes$/i) {
                    $validity_mask = '.+';
                } else {
                    $validity_mask = '.*';
                }
            } elsif ($type =~ /^integer$/i) {
                $validity_mask = '\d+';
            } elsif ($type =~ /^password$/i) {
                if ($mandatory =~ /^yes$/i) {
                    $validity_mask = '.+';
                } else {
                    $validity_mask = '.*';
                }
            } elsif ($type =~ /^string$/i) {
                if ($mandatory =~ /^yes$/i) {
                    $validity_mask = '.+';
                } else {
                    $validity_mask = '.*';
                }
            } elsif ($type =~ /^yesno$/i) {
                $validity_mask = "(yes|no)";
            }
        }

        # Fetch current value from job variable
        $var_value = exists $$jobvariable{$var_name} ? $$jobvariable{$var_name} : undef;
        if (defined $var_value) {
            if ($case_check =~ /^lowercase$/i) {
                $var_value = lc($var_value);
            } elsif ($case_check =~ /^uppercase$/i) {
                $var_value = uc($var_value);
            }
        }

        # Check validity of the value
        if (defined $var_value) {
            # Defined variable, i.e. the parameter has already been set in the $jobvariable hash reference (usually
            # %::JOB_PARAMS) which can either be because the user provided the value as a job variable with --variable
            # parameter, or that the value has been set as a playlist value from a previous playlist or the execute_playlist.pl
            # script.
            $default_value_used = "no";
            if ($mandatory =~ /^yes$/i) {
                # A Mandatory variable
                if ($type =~ /^directory$/i) {
                    unless ( -d "$var_value") {
                        do {
                            General::Logging::log_user_error_message("Directory '$var_value' does not exist");
                            General::Logging::log_user_prompt("Enter a directory name for $var_name: ");  # Case sensitive, show output
                            $var_value = General::OS_Operations::get_user_validated_input("", "^$validity_mask\$", 1, 0);  # Case sensitive, show output
                            unless (defined $var_value) {
                                # It seems like we have no STDIN to fetch input from, exit now with an error message
                                General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                                $rc = 1;
                                goto EXIT_SUB;
                            }
                        } until ( -d "$var_value");
                    }
                    # Expand to full directory path
                    $var_value = abs_path $var_value;
                } elsif ($type =~ /^file$/i) {
                    unless ( -f "$var_value") {
                        do {
                            General::Logging::log_user_error_message("File '$var_value' does not exist");
                            General::Logging::log_user_prompt("Enter a file name for $var_name: ");  # Case sensitive, show output
                            $var_value = General::OS_Operations::get_user_validated_input("", "^$validity_mask\$", 1, 0);  # Case sensitive, show output
                            unless (defined $var_value) {
                                # It seems like we have no STDIN to fetch input from, exit now with an error message
                                General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                                $rc = 1;
                                goto EXIT_SUB;
                            }
                        } until ( -f "$var_value");
                    }
                    # Expand to full file path
                    $var_value = abs_path $var_value;
                } elsif ($type =~ /^password$/i) {
                    unless ($var_value =~ /^$validity_mask$/) {
                        General::Logging::log_user_error_message("Value is not valid");
                        General::Logging::log_user_prompt("Enter value $validity_mask for $var_name: ");
                        $var_value = General::OS_Operations::get_user_validated_input("", "^$validity_mask\$", $case_check =~ /^no$/i ? 0 : 1, 1);     # Hide output
                        unless (defined $var_value) {
                            # It seems like we have no STDIN to fetch input from, exit now with an error message
                            General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                            $rc = 1;
                            goto EXIT_SUB;
                        }
                    }
                } elsif ($type =~ /^yesno$/i) {
                    unless ($var_value =~ /^$validity_mask$/i) {
                        General::Logging::log_user_error_message("Value '$var_value' is not valid");
                        General::Logging::log_user_prompt("Enter value (yes|no) for $var_name: ");
                        $var_value = General::OS_Operations::get_user_validated_input("", "^(yes|no)\$", 0, 0);     # Ignore case, show output
                        unless (defined $var_value) {
                            # It seems like we have no STDIN to fetch input from, exit now with an error message
                            General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                            $rc = 1;
                            goto EXIT_SUB;
                        }
                    }
                } else {
                    unless ($var_value =~ /^$validity_mask$/) {
                        General::Logging::log_user_error_message("Value '$var_value' is not valid");
                        General::Logging::log_user_prompt("Enter value $validity_mask for $var_name: ");
                        $var_value = General::OS_Operations::get_user_validated_input("", "^$validity_mask\$", $case_check =~ /^no$/i ? 1 : 0, 0);  # show output
                        unless (defined $var_value) {
                            # It seems like we have no STDIN to fetch input from, exit now with an error message
                            General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                            $rc = 1;
                            goto EXIT_SUB;
                        }
                    }
                }

                if ($case_check =~ /^lowercase$/i) {
                    $var_value = lc($var_value);
                } elsif ($case_check =~ /^uppercase$/i) {
                    $var_value = uc($var_value);
                }

                if ($type !~ /^password$/i) {
                    if ($show_in_progress) {
                        General::Logging::log_user_message("Checking if job parameter $var_name is specified by the user:\n  Yes, value used:        $var_name=\"$var_value\"\n");
                    } else {
                        General::Logging::log_write("Checking if job parameter $var_name is specified by the user:\n  Yes, value used:        $var_name=\"$var_value\"\n");
                    }
                } else {
                    my $temp_var_value = $var_value;
                    $temp_var_value =~ s/./*/g;
                    if ($show_in_progress) {
                        General::Logging::log_user_message("Checking if job parameter $var_name is specified by the user:\n  Yes, value used:        $var_name=\"$temp_var_value\"\n");
                    } else {
                        General::Logging::log_write("Checking if job parameter $var_name is specified by the user:\n  Yes, value used:        $var_name=\"$temp_var_value\"\n");
                    }
                }
            } else {
                # An Optional variable
                if ($type =~ /^directory$/i) {
                    if (($var_value eq "") || ( -d "$var_value")) {
                        # Nothing to do
                    } else {
                        do {
                            General::Logging::log_user_error_message("Directory '$var_value' does not exist");
                            General::Logging::log_user_prompt("Enter a directory name for $var_name: ");
                            $var_value = General::OS_Operations::get_user_validated_input("", "^$validity_mask\$", 1, 0);  # Case sensitive, show output
                            unless (defined $var_value) {
                                # It seems like we have no STDIN to fetch input from, exit now with an error message
                                General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                                $rc = 1;
                                goto EXIT_SUB;
                            }
                        } until (($var_value eq "") || (-d "$var_value"));
                    }
                    # Expand to full directory path
                    $var_value = abs_path $var_value;
                } elsif ($type =~ /^file$/i) {
                    if (($var_value eq "") || ( -f "$var_value")) {
                        # Nothing to do
                    } else {
                        do {
                            General::Logging::log_user_error_message("File '$var_value' does not exist");
                            General::Logging::log_user_prompt("Enter a file name for $var_name: ");
                            $var_value = General::OS_Operations::get_user_validated_input("", "^$validity_mask\$", 1, 0);  # Case sensitive, show output
                            unless (defined $var_value) {
                                # It seems like we have no STDIN to fetch input from, exit now with an error message
                                General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                                $rc = 1;
                                goto EXIT_SUB;
                            }
                        } until (($var_value eq "") || (-f "$var_value"));
                    }
                    # Expand to full file path
                    $var_value = abs_path $var_value;
                } elsif ($type =~ /^password$/i) {
                    if (($var_value eq "") || ($var_value =~ /^$validity_mask$/)) {
                        # Nothing to do
                    } else {
                        General::Logging::log_user_error_message("Value is not valid");
                        General::Logging::log_user_prompt("Enter value $validity_mask for $var_name: ");
                        $var_value = General::OS_Operations::get_user_validated_input("", "^$validity_mask\$", $case_check =~ /^no$/i ? 0 : 1, 1);     # Hide output
                        unless (defined $var_value) {
                            # It seems like we have no STDIN to fetch input from, exit now with an error message
                            General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                            $rc = 1;
                            goto EXIT_SUB;
                        }
                    }
                } elsif ($type =~ /^yesno$/i) {
                    if (($var_value eq "") || ($var_value =~ /^(yes|no)$/i)) {
                        # Nothing to do
                    } else {
                        General::Logging::log_user_error_message("Value '$var_value' is not valid");
                        General::Logging::log_user_prompt("Enter value (yes|no) for $var_name: ");
                        $var_value = General::OS_Operations::get_user_validated_input("", "^(yes|no)\$", 0, 0);     # Ignore case, show output
                        unless (defined $var_value) {
                            # It seems like we have no STDIN to fetch input from, exit now with an error message
                            General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                            $rc = 1;
                            goto EXIT_SUB;
                        }
                    }
                } else {
                    if (($var_value eq "") || ($var_value =~ /^$validity_mask$/)) {
                        # Nothing to do
                    } else {
                        General::Logging::log_user_error_message("Value '$var_value' is not valid");
                        General::Logging::log_user_prompt("Enter value $validity_mask for $var_name: ");
                        $var_value = General::OS_Operations::get_user_validated_input("", "^$validity_mask\$", $case_check =~ /^no$/i ? 1 : 0, 0);  # show output
                        unless (defined $var_value) {
                            # It seems like we have no STDIN to fetch input from, exit now with an error message
                            General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                            $rc = 1;
                            goto EXIT_SUB;
                        }
                    }
                }

                if ($case_check =~ /^lowercase$/i) {
                    $var_value = lc($var_value);
                } elsif ($case_check =~ /^uppercase$/i) {
                    $var_value = uc($var_value);
                }

                if ($type !~ /^password$/i) {
                    if ($show_in_progress) {
                        General::Logging::log_user_message("Checking if job parameter $var_name is specified by the user:\n  Yes, value used:        $var_name=\"$var_value\"\n");
                    } else {
                        General::Logging::log_write("Checking if job parameter $var_name is specified by the user:\n  Yes, value used:        $var_name=\"$var_value\"\n");
                    }
                } else {
                    my $temp_var_value = $var_value;
                    $temp_var_value =~ s/./*/g;
                    if ($show_in_progress) {
                        General::Logging::log_user_message("Checking if job parameter $var_name is specified by the user:\n  Yes, value used:        $var_name=\"$temp_var_value\"\n");
                    } else {
                        General::Logging::log_write("Checking if job parameter $var_name is specified by the user:\n  Yes, value used:        $var_name=\"$temp_var_value\"\n");
                    }
                }
            }
        } else {
            # Not defined in $jobvariable
            if ($mandatory =~ /^yes$/i) {
                # Mandatory variable
               #if ($::JOB_PARAMS{'CONSOLE_OUTPUT_AVAILABLE'} eq "yes") {
               #    print "-" x 80, "\n";
               #    print "\n$var_name\n\n";
               #    print "$usage\n";
               #    print "-" x 80, "\n";
               #}
                $default_value_used = "no";
                General::Logging::log_user_message("-" x 80 . "\n\n$var_name\n\n$usage\n" . "-" x 80 . "\n");
                if ($type =~ /^directory$/i) {
                    do {
                        General::Logging::log_user_prompt("Enter a directory name for $var_name: ");
                        $var_value = General::OS_Operations::get_user_validated_input("", "^$validity_mask\$", 1, 0);  # Case sensitive, show output
                        unless (defined $var_value) {
                            # It seems like we have no STDIN to fetch input from, exit now with an error message
                            General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                            $rc = 1;
                            goto EXIT_SUB;
                        }
                        General::Logging::log_user_error_message("Directory '$var_value' does not exist") unless ( -d "$var_value");
                    } until ( -d "$var_value");
                    # Expand to full directory path
                    $var_value = abs_path $var_value;
                } elsif ($type =~ /^file$/i) {
                    do {
                        General::Logging::log_user_prompt("Enter a file name for $var_name: ");
                        $var_value = General::OS_Operations::get_user_validated_input("", "^$validity_mask\$", 1, 0);  # Case sensitive, show output
                        unless (defined $var_value) {
                            # It seems like we have no STDIN to fetch input from, exit now with an error message
                            General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                            $rc = 1;
                            goto EXIT_SUB;
                        }
                        General::Logging::log_user_error_message("File '$var_value' does not exist") unless ( -f "$var_value");
                    } until ( -f "$var_value");
                    # Expand to full file path
                    $var_value = abs_path $var_value;
                } elsif ($type =~ /^password$/i) {
                    General::Logging::log_user_prompt("Enter value $validity_mask for $var_name: ");
                    $var_value = General::OS_Operations::get_user_validated_input("", "^$validity_mask\$", $case_check =~ /^no$/i ? 0 : 1, 1);     # Hide output
                    unless (defined $var_value) {
                        # It seems like we have no STDIN to fetch input from, exit now with an error message
                        General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                        $rc = 1;
                        goto EXIT_SUB;
                    }
                } elsif ($type =~ /^yesno$/i) {
                    General::Logging::log_user_prompt("Enter value (yes|no) for $var_name: ");
                    $var_value = General::OS_Operations::get_user_validated_input("", "^(yes|no)\$", 0, 0);     # Ignore case, show output
                    unless (defined $var_value) {
                        # It seems like we have no STDIN to fetch input from, exit now with an error message
                        General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                        $rc = 1;
                        goto EXIT_SUB;
                    }
                } else {
                    General::Logging::log_user_prompt("Enter value $validity_mask for $var_name: ");
                    $var_value = General::OS_Operations::get_user_validated_input("", "^$validity_mask\$", $case_check =~ /^no$/i ? 1 : 0, 0);  # show output
                    unless (defined $var_value) {
                        # It seems like we have no STDIN to fetch input from, exit now with an error message
                        General::Logging::log_user_error_message("No console found where user can provide value for '$var_name'");
                        $rc = 1;
                        goto EXIT_SUB;
                    }
                }

                if ($case_check =~ /^lowercase$/i) {
                    $var_value = lc($var_value);
                } elsif ($case_check =~ /^uppercase$/i) {
                    $var_value = uc($var_value);
                }

                if ($type !~ /^password$/i) {
                    if ($show_in_progress) {
                        General::Logging::log_user_message("Checking if job parameter $var_name is specified by the user:\n  No, new value set:      $var_name=\"$var_value\"\n");
                    } else {
                        General::Logging::log_write("Checking if job parameter $var_name is specified by the user:\n  No, new value set:      $var_name=\"$var_value\"\n");
                    }
                } else {
                    my $temp_var_value = $var_value;
                    $temp_var_value =~ s/./*/g;
                    if ($show_in_progress) {
                        General::Logging::log_user_message("Checking if job parameter $var_name is specified by the user:\n  No, new value set:      $var_name=\"$temp_var_value\"\n");
                    } else {
                        General::Logging::log_write("Checking if job parameter $var_name is specified by the user:\n  No, new value set:      $var_name=\"$temp_var_value\"\n");
                    }
                }
            } else {
                # Optional variable
                $default_value_used = "yes";
                $var_value = $default_value;

                if ($case_check =~ /^lowercase$/i) {
                    $var_value = lc($var_value);
                } elsif ($case_check =~ /^uppercase$/i) {
                    $var_value = uc($var_value);
                }

                if ($type !~ /^password$/i) {
                    if ($show_in_progress) {
                        General::Logging::log_user_message("Checking if job parameter $var_name is specified by the user:\n  No, default value used: $var_name=\"$var_value\"\n");
                    } else {
                        General::Logging::log_write("Checking if job parameter $var_name is specified by the user:\n  No, default value used: $var_name=\"$var_value\"\n");
                    }
                } else {
                    my $temp_var_value = $var_value;
                    $temp_var_value =~ s/./*/g;
                    if ($show_in_progress) {
                        General::Logging::log_user_message("Checking if job parameter $var_name is specified by the user:\n  No, default value used: $var_name=\"$temp_var_value\"\n");
                    } else {
                        General::Logging::log_write("Checking if job parameter $var_name is specified by the user:\n  No, default value used: $var_name=\"$temp_var_value\"\n");
                    }
                }
            }
        }

        $$definitions{$var_name}{'value'} = $var_value;
        $$definitions{$var_name}{'default_value_used'} = $default_value_used;
        $$jobvariable{$var_name} = $var_value;
    }

EXIT_SUB:

    # Restore old USR1, USR2, CTRL-C and CTRL-\ handlers after checking job parameters
    $SIG{INT} = $old_SIGINT;
    $SIG{QUIT} = $old_SIGQUIT;
    $SIG{USR1} = $old_SIGUSR1;
    $SIG{USR2} = $old_SIGUSR2;

    # Return the code to the calling instance
    return $rc;
}

# -----------------------------------------------------------------------------
# Call this subroutine to print information about the variables that is defined
# by a playlist file.
# The hash reference that is passed in to this subroutine should have the
# following format:
# %hash_ref->
#   <variable name> =>
#       'case_check'    = <value>
#       'default_value' = <value>
#       'mandatory'     = <value>
#       'type'          = <value>
#       'usage'         = <value>
#       'validity_mask' = <value>
#       'value'         = <value>
# The output is written to STDOUT.
#
# Input variables:
#    - Hash variable reference to print information about.
#    - Prefix to put in front ove every line (OPTIONAL).
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub print_info_about_job_variables {
    my $hash_ref = shift;
    my $prefix = shift;
    my $usage;

    $prefix = "" unless $prefix;

    for my $name (sort keys %$hash_ref) {
        #printf "\n$prefix$name\n$prefix%s\n", "-" x length($name);
        printf "\n%s%s\n%s%s\n",        $prefix, $name, $prefix, "-" x length($name);
        printf "%s%-15s %s\n",          $prefix, "Mandatory:",     $hash_ref->{$name}{'mandatory'};
        printf "%s%-15s %s\n",          $prefix, "Type:",          $hash_ref->{$name}{'type'};
        printf "%s%-15s %s\n",          $prefix, "Case Check:",    $hash_ref->{$name}{'case_check'};
        printf "%s%-15s %s\n",          $prefix, "Validity Mask:", $hash_ref->{$name}{'validity_mask'};
        printf "%s%-15s %s\n",          $prefix, "Default Value:", $hash_ref->{$name}{'default_value'};
        printf "%sDescription:    ",    $prefix;
        $usage = $hash_ref->{$name}{'usage'};
        if ($usage =~ /\n[^\n]+$/s) {
            # A multiline text is given, indent it properly
            $usage =~ s/\n/\n$prefix                /sg;
            # Remove the spaces at the end of the last line
            $usage =~ s/\s+$/\n/;
        }
        print "$usage\n";
    }
}

# -----------------------------------------------------------------------------
# Call this subroutine to print information about which $::JOB_PARAMS variables
# that is used by a file.
# It will scan the provided filename for any use of the $::JOB_PARAMS variable
# and list which ones are read and which are written by the file.
# The output is written to STDOUT.
#
# Input variables:
#    - Path to the file to check.
#    - Hash variable reference to also check (OPTIONAL).
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub print_used_job_variables {
    my $file_path = shift;
    my $hash_ref = shift;

    my %access_times;
    my $debug = 0;
    my $heading_printed = 0;
    my $line;
    my @lines;
    my $rc;
    my $var_name;

    print "DBG:file_path=$file_path\n" if $debug;
    # Read the input file
    $rc = General::File_Operations::read_file(
        {
            "filename"            => $file_path,
            "output-ref"          => \@lines,
            "include-pattern"     => '\$::JOB_PARAMS\{',
        }
    );
    if ($rc != 0) {
        print "Unable to read file $file_path\n";
        return;
    }
    for $line (@lines) {
        $line =~ s/\s+//g;   # Remove all spaces
        print "\nLine: $line\n" if $debug;
        while ($line =~ /\$::JOB_PARAMS\{/) {
            # Remove beginning up to first $::JOB_PARAMS
            if ($line =~ /^.*?\$::JOB_PARAMS\{([^}]+)\}(.*)/) {
                $var_name = $1;
                $line = $2;
                print "      $line\n" if $debug;
                if ($var_name =~ /['"].+{/ && $line =~ /^(.+?['"])}(.*)/) {
                    # Special case e.g. $::JOB_PARAMS{"DATA_FILE_${file_number_str}_LOADED"} = "no";
                    $var_name = "$var_name}$1";
                    $line = $2;
                    print "      $line\n" if $debug;
                }
                $var_name =~ s/^['"](.+)['"]$/$1/;
                if ($line =~ /^==/) {
                    print "Read:    $var_name\n" if $debug;
                    $access_times{$var_name}{'read'}++;
                } elsif ($line =~ /^=/) {
                    print "Written: $var_name\n" if $debug;
                    $access_times{$var_name}{'write'}++;
                } else {
                    print "Read:    $var_name\n" if $debug;
                    $access_times{$var_name}{'read'}++;
                }
            }
        }
    }

    # Check if also a hash variable reference was provided with contains playlist specific variables
    # which is read at the startup of the playlist.
    if (defined $hash_ref) {
        for $var_name (keys %$hash_ref) {
            $access_times{$var_name}{'read'}++;
            $access_times{$var_name}{'write'}++;
        }
    }

    # Print the statistics for variable access
    for $var_name (sort keys %access_times) {
        next unless ($var_name =~ /[A-Z]/);  # Only print variable names that contains uppercase letters
        if ($heading_printed == 0) {
            print "\nAccessed Job Parameters:\n";
            print "========================\n";
            print "\nThis playlist file is accessing the following global job parameters:\n\n";
            printf "%-5s  %-5s  %-13s\n", "Read", "Write", "Variable Name";
            printf "%-5s  %-5s  %-13s\n", "-"x5, "-"x5, "-"x13;
            $heading_printed = 1;
        }
        printf "%5s  %5s  %s\n", exists $access_times{$var_name}{'read'} ? $access_times{$var_name}{'read'} : "",
                                 exists $access_times{$var_name}{'write'} ? $access_times{$var_name}{'write'} : "",
                                 $var_name;
    }
}

# -----------------------------------------------------------------------------
# Call this subroutine to print information about which $::NETWORK_CONFIG_PARAMS
# variables that is used by a file.
# It will scan the provided filename for any use of the $::NETWORK_CONFIG_PARAMS
# variable and list which ones are read and which are written by the file.
# The output is written to STDOUT.
#
# Input variables:
#    - Path to the file to check.
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub print_used_network_config_variables {
    my $file_path = shift;

    my %access_times;
    my $debug = 0;
    my $heading_printed = 0;
    my $line;
    my @lines;
    my $rc;
    my $var_name;

    print "DBG:file_path=$file_path\n" if $debug;
    # Read the input file
    $rc = General::File_Operations::read_file(
        {
            "filename"            => $file_path,
            "output-ref"          => \@lines,
            "include-pattern"     => '\$::NETWORK_CONFIG_PARAMS\{',
        }
    );
    if ($rc != 0) {
        print "Unable to read file $file_path\n";
        return;
    }
    for $line (@lines) {
        $line =~ s/\s+//g;   # Remove all spaces
        print "\nLine: $line\n" if $debug;
        while ($line =~ /\$::NETWORK_CONFIG_PARAMS\{/) {
            # Remove beginning up to first $::NETWORK_CONFIG_PARAMS
            if ($line =~ /^.*?\$::NETWORK_CONFIG_PARAMS\{([^}]+)\}(.+)/) {
                $var_name = $1;
                $line = $2;
                print "      $line\n" if $debug;
                $var_name =~ s/^['"](.+)['"]$/$1/;
                if ($line =~ /^==/) {
                    print "Read:    $var_name\n" if $debug;
                    $access_times{$var_name}{'read'}++;
                } elsif ($line =~ /^=/) {
                    print "Written: $var_name\n" if $debug;
                    $access_times{$var_name}{'write'}++;
                } else {
                    print "Read:    $var_name\n" if $debug;
                    $access_times{$var_name}{'read'}++;
                }
            }
        }
    }

    # Print the statistics for variable access
    for $var_name (sort keys %access_times) {
        if ($heading_printed == 0) {
            print "\nAccessed Network Configuration Parameters:\n";
            print "==========================================\n";
            print "\nThis playlist file is accessing the following global network configuration parameters:\n\n";
            printf "%-5s  %-5s  %-13s\n", "Read", "Write", "Variable Name";
            printf "%-5s  %-5s  %-13s\n", "-"x5, "-"x5, "-"x13;
            $heading_printed = 1;
        }
        printf "%5s  %5s  %s\n", exists $access_times{$var_name}{'read'} ? $access_times{$var_name}{'read'} : "",
                                 exists $access_times{$var_name}{'write'} ? $access_times{$var_name}{'write'} : "",
                                 $var_name;
    }
}

# -----------------------------------------------------------------------------
# Call this subroutine to register a task as being used to apply some workaround
# which at some point in time should be removed or reworked with different logic
# when the problem has been fixed.
#
# Input variables:
#    - Message that describes for what reason this workaround is used, which
#      can e.g. be a reference to a bug or user story or some other descriptive
#      text etc.
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub register_workaround_task {
    my $message = shift;

    # Read out information about the caller.
    #   (0) : Current subroutine
    #   (1) : Parent subroutine
    #   (2) : Grandparent subroutine
    #   (x) : ... etc.
    #
    #   [0]  : package
    #   [1]  : filename
    #   [2]  : line
    #   [3]  : subroutine
    #   [4]  : hasargs
    #   [5]  : wantarray
    #   [6]  : evaltext
    #   [7]  : is_require
    #   [8]  : hints
    #   [9]  : bitmask
    #   [10] : hinthash
    my $caller_subroutine = ( caller(1) )[3];

    # Add starting divider
    push @workaround_tasks, "-" x 80;
    push @workaround_tasks, "Task: $caller_subroutine";

    if ($message) {
        $message =~ s/\s*\n*$//;   # Remove any NL characters at the end of the message
        $message =~ s/\r//g;       # Remove all CR characters in the message
        if ($message =~ /\n/) {
            # The message contains multiple lines
            my @messages = split /\n/, $message;
            push @workaround_tasks, "Text:";
            push @workaround_tasks, @messages;
        } else {
            push @workaround_tasks, "Text: $message";
        }
    } else {
        push @workaround_tasks, "Text: No details specified";
    }
    push @workaround_tasks, "-" x 80;
    push @workaround_tasks, "";
}

# -----------------------------------------------------------------------------
# Show a checkpoint message stored in a file and wait for the user to select
# either 'c' to continue playlist execution or 'f' to exit playlist execution
# and to perform a fallback.
#
# Input variables:
#    - Filename with message to display.
#
# Return values:
#    Return code value coming from the called subroutine.
#
# -----------------------------------------------------------------------------
sub show_checkpoint {
    my $filename = shift;

    my $answer;
    my $rc;

    # Show the checkpoint instruction from file on the screen
    $rc = General::File_Operations::show_file_content(
        {
            "filename"            => $filename,
        }
    );
    if ($rc == 0) {
        General::Logging::log_user_prompt("Type 'c' and press ENTER to continue, or 'f' to Fallback and Exit: ");
        $answer = General::OS_Operations::get_user_validated_input("","(c|f|q|-[1-7])", 0, 0);   # 0=Not case sensitive, 0=Not hidden
        if (defined $answer) {
            if ($answer =~ /^f$/i) {
                $rc = RC_FALLBACK;
            } elsif ($answer =~ /^q$/i) {
                # Undocumented answer to allow rerun of job after e.g. fixing the code.
                $rc = RC_GRACEFUL_EXIT;
            } elsif ($answer =~ /^-[1-7]$/) {
                # Undocumented answer to allow the user to e.g. skip a task with -7 etc.
                $rc = $answer;
            } else {
                $rc = 0;
            }
            General::Logging::log_user_message("Answer from user was: $answer\n");
        } else {
            $answer = "f";
            General::Logging::log_user_message("Automatic answer was: $answer\n");
            $rc = RC_FALLBACK;
        }
    }

    # Return the code to the calling instance
    return $rc;
}

# -----------------------------------------------------------------------------
# Show a checkpoint message and wait for the user to select either 'c' to
# continue playlist execution or 'f' to exit playlist execution and to perform
# a fallback.
#
# Input variables:
#    - Message to show to the user, it can be one line or multiple lines.
#
# Return values:
#    Return code value coming from the called subroutine.
#
# -----------------------------------------------------------------------------
sub show_checkpoint_message {
    my $message = shift;

    my $answer;
    my $rc;

    # Show the checkpoint instruction from file on the screen
    General::Logging::log_user_message($message);
    General::Logging::log_user_prompt("Type 'c' and press ENTER to continue, or 'f' to Fallback and Exit: ");
    $answer = General::OS_Operations::get_user_validated_input("","(c|f|q|-[1-7])", 0, 0);   # 0=Not case sensitive, 0=Not hidden
    if (defined $answer) {
        if ($answer =~ /^f$/i) {
            $rc = RC_FALLBACK;
        } elsif ($answer =~ /^q$/i) {
            # Undocumented answer to allow rerun of job after e.g. fixing the code.
            $rc = RC_GRACEFUL_EXIT;
        } elsif ($answer =~ /^-[1-7]$/) {
            # Undocumented answer to allow the user to e.g. skip a task with -7 etc.
            $rc = $answer;
        } else {
            $rc = 0;
        }
        General::Logging::log_user_message("Answer from user was: $answer\n");
    } else {
        $answer = "f";
        General::Logging::log_user_message("Automatic answer was: $answer\n");
        $rc = RC_FALLBACK;
    }

    # Return the code to the calling instance
    return $rc;
}

# -----------------------------------------------------------------------------
# Find the sub routine name from a reference and return the fully qualified name.
#
# Code found on the following web page:
# https://stackoverflow.com/questions/7419071/determining-the-subroutine-name-of-a-perl-code-reference
#
# Subroutine Execution Flow:
#   - Check that the input is a reference.
#   - Convert reference into an object from the B::SV-derived class (B - The Perl Compiler Backend)
#     (SV - Scalar value).
#   - Check that the CV (Code value) object is a fully qualified CV object,
#     and that it has a GV (Glob value) since GV contains the needed information.
#   - If the GV has a STASH (Symbol table as hash), set name from stash to get top most level of the call.
#         e.g. main:: if called from main routine of a program.
#   - If GV has name append it to name.
#         e.g. GV name contains the name of the input subroutine
#   - If however the subroutine is anonymous then append name with the file name
#     and line number instead.
#         e.g. SomePackage::__ANON__() defined at <File name>:<Line number>
#              ANON subroutines can also be given names by defining  local *__ANON__ = "..."
#              inside the routine.
#
# Input variables:
#    - subroutine reference.
#
# Return values:
#    String of the fully qualified sub routine name.
#
# -----------------------------------------------------------------------------
sub sub_name {
    return unless ref( my $r = shift );
    return unless my $cv = svref_2object( $r );
    return unless $cv->isa( 'B::CV' )
              and my $gv = $cv->GV
              ;
    my $name = '';
    if ( my $st = $gv->STASH ) {
        $name = $st->NAME . '::';
    }
    my $n = $gv->NAME;
    if ( $n ) {
        $name .= $n;
        if ( $n eq '__ANON__' ) {
            $name .= ' defined at ' . $gv->FILE . ':' . $gv->LINE;
        }
    }
    return $name;
}

1;
