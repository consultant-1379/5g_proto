package General::State_Machine;

################################################################################
#
#  Author   : eedvam, eustone, everhel
#
#  Revision : 1.11
#  Date     : 2023-10-24 15:32:20
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2018, 2019, 2022, 2023
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
    always_execute_task
    always_ignore_error_in_task
    always_interrupt_task
    always_skip_task
    get_always_tasks
    get_modified_return_code
    get_state_action
    initialize
    remove_always_execute_task
    remove_always_ignore_error_in_task
    remove_always_interrupt_task
    remove_always_skip_task
    state_remove
    state_update
    );

use General::File_Operations;
use General::Playlist_Operations;
use File::Basename qw(dirname basename);

my %always_execute_tasks;
my %always_ignore_error_in_tasks;
my %always_interrupt_tasks;
my %always_skip_tasks;
my $always_do_filename = "";
my $debug = 0;
my $log_state_history = 1;
my $state_filename = "";
my $state_history_file = "";
my %state_action;

# Map what should happen if task is executed again
my %states = (
    "created"           => "EXECUTE_TASK",
    "started"           => "EXECUTE_TASK",
    "triggered"         => "EXECUTE_TASK",
    "fallback"          => "CONTINUE_FALLBACK",
    "graceful_exit"     => "EXECUTE_TASK",
    "playlistout"       => "SKIP_PLAYLIST",
    "rerun_step"        => "EXECUTE_TASK",
    "rerun"             => "EXECUTE_TASK",
    "stepout"           => "SKIP_STEP",
    "taskout"           => "SKIP_TASK",
    "success"           => "SKIP_TASK",
    "failed"            => "EXECUTE_TASK",
    "user_interrupt"    => "EXECUTE_TASK",
);

# -----------------------------------------------------------------------------
# Add regular expressions of tasks that should always be executed regardless if
# they have already been executed.
# The passed array with regular expressions will be added to any already
# existing regular expressions.
# If both 'always_execute_tasks' and 'always_skip_tasks' have been specified
# and the two conflict with each other then 'always_execute_tasks' will be used.
#
# Input variables:
#    - Mandatory: Array with regular expressions.
#
# Returns:
#    - Nothing
#
# -----------------------------------------------------------------------------
sub always_execute_task {
    for (@_) {
        $always_execute_tasks{$_} = 1;
    }

    # Write changes to file
    update_always_do_file();

    if ($debug) {
        print STDERR "                  [INFO] General::State_Machine::always_excute_task\n";
        for (sort keys %always_execute_tasks) {
            print STDERR "                  [INFO] '$_'\n";
        }
    }

    return;
}

# -----------------------------------------------------------------------------
# Add regular expressions of tasks that should always return success regardless
# of the actual result.
# The passed array with regular expressions will be added to any already
# existing regular expressions.
#
# Input variables:
#    - Mandatory: Array with regular expressions.
#
# Returns:
#    - Nothing
#
# -----------------------------------------------------------------------------
sub always_ignore_error_in_task {
    for (@_) {
        $always_ignore_error_in_tasks{$_} = 1;
    }

    # Write changes to file
    update_always_do_file();

    if ($debug) {
        print STDERR "                  [INFO] General::State_Machine::always_ignore_error_in_task\n";
        for (sort keys %always_ignore_error_in_tasks) {
            print STDERR "                  [INFO] '$_'\n";
        }
    }

    return;
}

# -----------------------------------------------------------------------------
# Add regular expressions of tasks that should always be interrupted and the
# user asked what should happen next.
# The passed array with regular expressions will be added to any already
# existing regular expressions.
#
# Input variables:
#    - Mandatory: Array with regular expressions.
#
# Returns:
#    - Nothing
#
# -----------------------------------------------------------------------------
sub always_interrupt_task {
    for (@_) {
        $always_interrupt_tasks{$_} = 1;
    }

    # Write changes to file
    update_always_do_file();

    if ($debug) {
        print STDERR "                  [INFO] General::State_Machine::always_interrupt_task\n";
        for (sort keys %always_interrupt_tasks) {
            print STDERR "                  [INFO] '$_'\n";
        }
    }

    return;
}

# -----------------------------------------------------------------------------
# Add regular expressions of tasks that should never be executed.
# The passed array with regular expressions will be added to any already
# existing regular expressions.
# If both 'always_skip_tasks' and 'always_skip_tasks' have been specified
# and the two conflict with each other then 'always_skip_tasks' will be used.
#
# Input variables:
#    - Mandatory: Array with regular expressions.
#
# Returns:
#    - Nothing
#
# -----------------------------------------------------------------------------
sub always_skip_task {
    for (@_) {
        $always_skip_tasks{$_} = 1;
    }

    # Write changes to file
    update_always_do_file();

    if ($debug) {
        print STDERR "                  [INFO] General::State_Machine::always_skip_task\n";
        for (sort keys %always_skip_tasks) {
            print STDERR "                  [INFO] '$_'\n";
        }
    }

    return;
}

# -----------------------------------------------------------------------------
# Get all regular expressions for tasks that has been set to always execute,
# ignore error, interrupt or skip.
# It will return an array with information about the tasks.
#
# Input variables:
#    - Nothing
#
# Returns:
#    - An array with information about tasks to always xxx.
#
# -----------------------------------------------------------------------------
sub get_always_tasks {
    my @result;

    push @result, "always_execute_tasks:";
    for (sort keys %always_execute_tasks) {
        push @result, "  - $_";
    }
    push @result, "";

    push @result, "always_ignore_error_in_tasks:";
    for (sort keys %always_ignore_error_in_tasks) {
        push @result, "  - $_";
    }
    push @result, "";

    push @result, "always_interrupt_tasks:";
    for (sort keys %always_interrupt_tasks) {
        push @result, "  - $_";
    }
    push @result, "";

    push @result, "always_skip_tasks:";
    for (sort keys %always_skip_tasks) {
        push @result, "  - $_";
    }

    return @result;
}

# -----------------------------------------------------------------------------
# Return a modified return code if the current executing task match with
# regular expressions of tasks the user want to have a modified return code
# from.
# Initially the user can only say if errors in a task should be ignored and a
# successful (rc=0) return code should always be returned at failure (rc>1).
#
# Input variables:
#    - Mandatory: Hash reference %params = Input parameters to the sub routine
#      which can be any of the following keys:
#       "task-name":
#           The name of the task that is currently executing. If no name is
#           given or if task name does not match any specified regular
#           expressions then the result will be '0' and no change of the return
#           code will be done.
#       "return-code":
#           A reference to the scalar variable that contains the return code
#           that should be modified if wanted by the user.
#           If no reference is given then the result will be 0 and no change of
#           the return code will be done.
#
# Returns:
#    0:  if no change of return code was done.
#    1:  if the return code was changed.
#
# -----------------------------------------------------------------------------
sub get_modified_return_code {
    my %params = %{$_[0]};

    my $answer;
    my $task_name = defined $params{"task-name"} ? $params{"task-name"} : "";
    my $return_code = defined $params{"return-code"} ? $params{"return-code"} : undef;
    my $old_rc;

    print STDERR "                  [INFO] General::State_Machine::get_modified_return_code\n" if ($debug);
    print STDERR "                  [INFO] TASK NAME: $task_name\n" if ($debug);

    if ($task_name eq "") {
        # No task name given, no change of return code done
        return 0;
    } elsif (! defined $return_code) {
        # No return code scalar reference given, no change of return code done
        return 0;
    } elsif (scalar keys %always_ignore_error_in_tasks == 0) {
        # No tasks with special handling specified by the user
        return 0;
    }

    print STDERR "                  [INFO] Initial Return code: $$return_code\n" if ($debug);

    # Check if special handling should apply to execute the task
    if (%always_ignore_error_in_tasks) {
        for my $regex (keys %always_ignore_error_in_tasks) {
            next unless $regex;
            if ($task_name =~ /$regex/) {
                $old_rc = $$return_code;
                if ($old_rc > 0) {
                    $$return_code = 0;
                    General::Logging::log_user_warning_message("Original return code ($old_rc) changed to $$return_code\n");
                    print STDERR "                  [INFO] New Return code: $$return_code\n" if ($debug);
                    return 1;
                } else {
                    # Return code is already 0 or negative value, keep the old value
                    return 0;
                }
            }
        }
    }

    # No change of return code needed
    return 0;
}

# -----------------------------------------------------------------------------
# Return the action to perform for the task which is one of the following:
#  - Execute task
#  - Skip task
#  - Skip step
#  - Skip playlist
#  - End playlist execution with a graceful exit
#  - Continue the interrupted fallback logic
#
# Input variables:
#    - Mandatory: Hash reference %params = Input parameters to the sub routine
#      which can be any of the following keys:
#       "task-name":
#           The name of the task to use. If no name is given or if task was
#           not previously executed the result will be 'Execute task'.
#
# Returns:
#    0:  if 'Execute task'.
#    -1: if 'Continue Fallback'.
#    -2: if 'End playlist execution with a graceful exit'.
#    -3: if 'Skip playlist'.
#    -6: if 'Skip step'.
#    -7: if 'Skip task'.
#
# -----------------------------------------------------------------------------
sub get_state_action {
    my %params = %{$_[0]};

    my $answer;
    my $task_name = defined $params{"task-name"} ? $params{"task-name"} : "";
    my $dry_run_job = defined $params{"dry-run-job"} ? $params{"dry-run-job"} : "no";
    my $dry_run_task = defined $params{"dry-run-task"} ? $params{"dry-run-task"} : 0;
    my $rc = 0;
    my $valid;

    print STDERR "                  [INFO] General::State_Machine::get_state_action\n" if ($debug);
    print STDERR "                  [INFO] TASK NAME: $task_name\n" if ($debug);

    if ($task_name eq "") {
        $rc = 0;
    } elsif ($dry_run_job eq "yes" && $dry_run_task == 0) {
        $rc = General::Playlist_Operations::RC_TASKOUT;
    } elsif (exists $state_action{$task_name}) {
        print STDERR "                  [INFO] Action value of task: $state_action{$task_name}\n\n" if ($debug);
        if ($state_action{$task_name} eq "EXECUTE_TASK") {
            # task should be executed
            $rc = 0;
        } elsif ($state_action{$task_name} eq "END_EXECUTION") {
            $rc = General::Playlist_Operations::RC_GRACEFUL_EXIT;
        } elsif ($state_action{$task_name} eq "SKIP_PLAYLIST") {
            $rc = General::Playlist_Operations::RC_PLAYLISTOUT;
        } elsif ($state_action{$task_name} eq "SKIP_STEP") {
            $rc = General::Playlist_Operations::RC_STEPOUT;
        } elsif ($state_action{$task_name} eq "SKIP_TASK") {
            $rc = General::Playlist_Operations::RC_TASKOUT;
        } elsif ($state_action{$task_name} eq "CONTINUE_FALLBACK") {
            # Special check if we are executing the main function in the module.
            # If that is the case we need to continue executing the following steps inside the main function
            # otherwise the interrupted fallback logic will not continue to execute.
            if ($task_name =~ /^.+::main$/) {
                # task should be executed
                $rc = 0;
            } else {
                $rc = General::Playlist_Operations::RC_FALLBACK;
            }
        } else {
            # Unknown action, task should be executed
            $rc = 0;
        }
    } else {
        # New task that should be executed
        $rc = 0;
    }

    print STDERR "                  [INFO] Initial Return code: $rc\n" if ($debug);

    # Check if special handling should apply to skip the task
    if (%always_skip_tasks) {
        for my $regex (keys %always_skip_tasks) {
            next unless $regex;
            if ($task_name =~ /$regex/) {
                $rc = General::Playlist_Operations::RC_TASKOUT;
                print STDERR "                  [INFO] Skip Return code: $rc\n" if ($debug);
                last;
            }
        }
    }

    # Check if special handling should apply to execute the task
    if (%always_execute_tasks) {
        for my $regex (keys %always_execute_tasks) {
            next unless $regex;
            if ($task_name =~ /$regex/) {
                $rc = 0;
                print STDERR "                  [INFO] Execute Return code: $rc\n" if ($debug);
                last;
            }
        }
    }

    # Check if special handling should apply to interrupt the task
    if (%always_interrupt_tasks) {
        for my $regex (keys %always_interrupt_tasks) {
            next unless $regex;
            if ($task_name =~ /$regex/) {
                # Ask the user how to proceed
                General::Logging::log_user_warning_message("Playlist Execution Paused\n");
                General::Message_Generation::print_message_box(
                    {
                        "header-text" => "User Feedback Required",
                        "align-text"  => "left",
                        "max-length"  => 0,
                        "messages"    => [
                            "",
                            "Continued execution of the Playlist require an answer from you.",
                            "",
                            "Please answer how to proceed by selecting one of the following options:",
                            "",
                            sprintf("   %s : CONTINUE", "c"),
                            "       Continue with the default action which is '$rc'.",
                            "",
                            sprintf("   %s : EXECUTE TASK", "0"),
                            "       Continue to execute the task that was paused.",
                            sprintf("  %s : FAIL TASK", ">0"),
                            "       Do not execute the task that was paused and mark it as failed.",
                            sprintf("  %s : RC_FALLBACK", General::Playlist_Operations::RC_FALLBACK),
                            "       Perform a fallback which will exit script execution and do a",
                            "       proper cleanup.",
                            sprintf("  %s : RC_GRACEFUL_EXIT", General::Playlist_Operations::RC_GRACEFUL_EXIT),
                            "       Perform a graceful exit which will exit script execution but not",
                            "       do any cleanup.",
                            sprintf("  %s : RC_PLAYLISTOUT", General::Playlist_Operations::RC_PLAYLISTOUT),
                            "       Perform an exit from current executing Playlist.",
                            sprintf("  %s : RC_RERUN_STEP", General::Playlist_Operations::RC_RERUN_STEP),
                            "       Rerun the current executing Step again, which will cause all",
                            "       Tasks in Step to be executed again.",
                            sprintf("  %s : RC_RERUN_TASK", General::Playlist_Operations::RC_RERUN_TASK),
                            "       Rerun the current executing Task again.",
                            sprintf("  %s : RC_STEPOUT", General::Playlist_Operations::RC_STEPOUT),
                            "       Perform an exit from current executing Step, Execution will",
                            "       continue with next Step.",
                            sprintf("  %s : RC_TASKOUT", General::Playlist_Operations::RC_TASKOUT),
                            "       Perform an exit from current executing Task, Execution will",
                            "       continue with next Task or Step.",
                            "",
                        ],
                    }
                );

                $valid = sprintf '(c|C|%s|%s|%s|%s|%s|%s|%s|\d+)', General::Playlist_Operations::RC_FALLBACK,
                                                                   General::Playlist_Operations::RC_GRACEFUL_EXIT,
                                                                   General::Playlist_Operations::RC_PLAYLISTOUT,
                                                                   General::Playlist_Operations::RC_RERUN_STEP,
                                                                   General::Playlist_Operations::RC_RERUN_TASK,
                                                                   General::Playlist_Operations::RC_STEPOUT,
                                                                   General::Playlist_Operations::RC_TASKOUT;
                $answer = General::OS_Operations::get_user_validated_input("Enter one of $valid as answer: ", "^$valid\$");
                General::Logging::log_user_message("Answer from user was: $answer\n");
                unless ($answer =~ /^c$/i) {
                    $rc = $answer;
                }
                print STDERR "                  [INFO] Interrupt Return code: $rc\n" if ($debug);
                last;
            }
        }
    }

    print STDERR "                  [INFO] Final Return code: $rc\n" if ($debug);

    return $rc;
}

# -----------------------------------------------------------------------------
# Initialize the state machine.
# If a file is specified and it exist then state information is read from the
# file and the %state_action hash is updated.
#
# Input variables:
#    - Mandatory: Hash reference %params = Input parameters to the sub routine
#      which can be any of the following keys:
#       "always-do-filename":
#           If specified then this file will be used to keep a list of the
#           following variables:
#             - %always_execute_tasks
#             - %always_ignore_error_in_tasks
#             - %always_interrupt_tasks
#             - %always_skip_tasks
#
#       "state-filename":
#           If specified then this file will be used to keep a history of
#           already executed tasks.
#
# Returns:
#    - Return code 0: Initialization was successful.
#    - Return code 1: Initialization failed for some reason.
#
# -----------------------------------------------------------------------------
sub initialize {
    my %params = %{$_[0]};

    $always_do_filename = exists $params{"always-do-filename"} ? $params{"always-do-filename"} : "";
    $state_filename = exists $params{"state-filename"} ? $params{"state-filename"} : "";

    my @lines = ();
    my $regexp;
    my $result;
    my $state;
    my $task_name;
    my $var_name;

    print STDERR "                  [INFO] General::State_Machine::initialize\n" if ($debug);
    print STDERR "                  [INFO] STATE LOG: $state_filename\n" if ($debug);
    print STDERR "                  [INFO] ALWAYS-DO LOG: $always_do_filename\n" if ($debug);

    # Clear the %state_action and %always_xxx hash variables
    undef %always_execute_tasks;
    undef %always_ignore_error_in_tasks;
    undef %always_interrupt_tasks;
    undef %always_skip_tasks;
    undef %state_action;

    if ($always_do_filename) {
        if (-f "$always_do_filename") {
            # If the file exist, read the contents and update the %always_xxxx hash
            General::File_Operations::read_file(
                {
                    "filename"            => $always_do_filename,
                    "output-ref"          => \@lines,
                }
            );
            for (@lines) {
                if (/^(always_execute_tasks|always_ignore_error_in_tasks|always_interrupt_tasks|always_skip_tasks) : (.+)/) {
                    $var_name = $1;
                    $regexp = $2;
                    if ($var_name eq "always_execute_tasks") {
                        $always_execute_tasks{$regexp} = 1;
                    } elsif ($var_name eq "always_ignore_error_in_tasks") {
                        $always_ignore_error_in_tasks{$regexp} = 1;
                    } elsif ($var_name eq "always_interrupt_tasks") {
                        $always_interrupt_tasks{$regexp} = 1;
                    } elsif ($var_name eq "always_skip_tasks") {
                        $always_skip_tasks{$regexp} = 1;
                    }
                }
            }
        } else {
            # File does not exist, create an empty file
            $result = General::File_Operations::write_file(
                {
                    "filename"            => "$always_do_filename",
                    "output-ref"          => [],
                    "eol-char"            => "\n",
                    "append-file"         => 0,
                    "file-access-mode"    => "666",
                }
            );
            if ($result != 0) {
                print "ERROR: Unable to create always-do log of job in $always_do_filename'\n";
                return 1;
            }
        }
    }

    if ($state_filename) {
        if (-f "$state_filename") {
            # If the file exist, read the contents and update the %state_action hash
            General::File_Operations::read_file(
                {
                    "filename"            => $state_filename,
                    "output-ref"          => \@lines,
                }
            );
            for (@lines) {
                if (/^(\S+) : (\S+)/) {
                    $task_name = $1;
                    $state = $2;
                    $state_action{$task_name} = $states{$state};
                    print STDERR "                  [INFO] TASK NAME: $task_name\n" if ($debug);
                    print STDERR "                  [INFO] STATE: $state\n" if ($debug);
                    print STDERR "                  [INFO] State value of $state: $states{$state}\n\n" if ($debug);
                }
            }
        } else {
            # File does not exist, create an empty file
            $result = General::File_Operations::write_file(
                {
                    "filename"            => "$state_filename",
                    "output-ref"          => [],
                    "eol-char"            => "\n",
                    "append-file"         => 0,
                    "file-access-mode"    => "666",
                }
            );
            if ($result != 0) {
                print "ERROR: Unable to create state log of job in $state_filename'\n";
                return 1;
            }
        }

        if ($log_state_history) {
            $state_history_file = sprintf "%s/%s", dirname($state_filename), "state_change_history.log";
            my $timestamp = time();
            `echo -en "\n===============================================================================\n$timestamp Initialize file\n" >> $state_history_file`;
        }
    }

    return 0;
}

# -----------------------------------------------------------------------------
# Remove regular expressions of tasks that should always be executed regardless if
# they have already been executed.
# The passed array with regular expressions will be added to any already
# existing regular expressions.
# If both 'always_execute_tasks' and 'always_skip_tasks' have been specified
# and the two conflict with each other then 'always_execute_tasks' will be used.
#
# Input variables:
#    - Mandatory: Array with regular expressions.
#
# Returns:
#    - Nothing
#
# -----------------------------------------------------------------------------
sub remove_always_execute_task {
    for (@_) {
        delete $always_execute_tasks{$_} if (exists $always_execute_tasks{$_});
    }

    # Write changes to file
    update_always_do_file();

    if ($debug) {
        print STDERR "                  [INFO] General::State_Machine::remove_always_excute_task\n";
        for (sort keys %always_execute_tasks) {
            print STDERR "                  [INFO] '$_'\n";
        }
    }

    return;
}

# -----------------------------------------------------------------------------
# Remove regular expressions of tasks that should always return success regardless
# of the actual result.
# The passed array with regular expressions will be added to any already
# existing regular expressions.
#
# Input variables:
#    - Mandatory: Array with regular expressions.
#
# Returns:
#    - Nothing
#
# -----------------------------------------------------------------------------
sub remove_always_ignore_error_in_task {
    for (@_) {
        delete $always_ignore_error_in_tasks{$_} if (exists $always_ignore_error_in_tasks{$_});
    }

    # Write changes to file
    update_always_do_file();

    if ($debug) {
        print STDERR "                  [INFO] General::State_Machine::remove_always_ignore_error_in_task\n";
        for (sort keys %always_ignore_error_in_tasks) {
            print STDERR "                  [INFO] '$_'\n";
        }
    }

    return;
}

# -----------------------------------------------------------------------------
# Remove regular expressions of tasks that should always be interrupted and the
# user asked what should happen next.
# The passed array with regular expressions will be added to any already
# existing regular expressions.
#
# Input variables:
#    - Mandatory: Array with regular expressions.
#
# Returns:
#    - Nothing
#
# -----------------------------------------------------------------------------
sub remove_always_interrupt_task {
    for (@_) {
        delete $always_interrupt_tasks{$_} if (exists $always_interrupt_tasks{$_});
    }

    # Write changes to file
    update_always_do_file();

    if ($debug) {
        print STDERR "                  [INFO] General::State_Machine::remove_always_interrupt_task\n";
        for (sort keys %always_interrupt_tasks) {
            print STDERR "                  [INFO] '$_'\n";
        }
    }

    return;
}

# -----------------------------------------------------------------------------
# Remove regular expressions of tasks that should never be executed.
# The passed array with regular expressions will be added to any already
# existing regular expressions.
# If both 'always_skip_tasks' and 'always_skip_tasks' have been specified
# and the two conflict with each other then 'always_skip_tasks' will be used.
#
# Input variables:
#    - Mandatory: Array with regular expressions.
#
# Returns:
#    - Nothing
#
# -----------------------------------------------------------------------------
sub remove_always_skip_task {
    print STDERR "                  [INFO] General::State_Machine::remove_always_skip_task\n" if ($debug);

    for (@_) {
        if (exists $always_skip_tasks{$_}) {
            delete $always_skip_tasks{$_};
            state_remove( {"task-name" => $_ } );
        }
    }

    # Write changes to file
    update_always_do_file();

    if ($debug) {
        for (sort keys %always_skip_tasks) {
            print STDERR "                  [INFO] '$_'\n";
        }
    }

    return;
}

# -----------------------------------------------------------------------------
# Remove state entries of an already present task.
# Calling this subroutine might be needed when for example executing all
# previously executed tasks in a step. It should be passed a regular expression
# matching the tasks to be removed from the hash and the state file.
#
# Input variables:
#    - Mandatory: Hash reference %params = Input parameters to the sub routine
#      which can be any of the following keys:
#       "task-name":
#           The name of the task to use. If no name is given then no change
#           will be done.
#
# Returns:
#    - Nothing
#
# -----------------------------------------------------------------------------
sub state_remove {
    my %params = %{$_[0]};

    my $task_name = exists $params{"task-name"} ? $params{"task-name"} : "";

    print STDERR "                  [INFO] General::State_Machine::state_remove\n" if ($debug);

    if ($task_name eq "") {
        print "ERROR: No 'task-name' was given, no state removal was done.\n";
        return;
    }

    if ($debug) {
        use Data::Dumper;
        print STDERR "\n                  Hash \%state_action before deleting '$task_name':\n                  " . (join "                  ", Dumper(%state_action)) . "\n";
    }
    # Update the %state_action hash
    my @to_be_deleted = ();
    for my $key (%state_action) {
        if ($key =~ /^$task_name/) {
            print STDERR "                  [INFO] TASK NAME: $key\n" if ($debug);
            print STDERR "                  [INFO] Deleted from hash with value: $state_action{$key}\n" if ($debug);
            push @to_be_deleted, $key;
        }
    }
    for (@to_be_deleted) {
        delete $state_action{$_};

        if ($log_state_history) {
            my $timestamp = time();
            `echo -en "$timestamp Task=$_, Delete state_action key\n" >> $state_history_file`;
        }
    }
    print STDERR "\n                  Hash \%state_action after deleting '$task_name':\n                  " . (join "                  ", Dumper(%state_action)) . "\n" if ($debug);

    # If a log file has been specified, update state information into that file.
    # Look for all lines that match the task and remove that line and then write
    # back the whole content to the file.
    if ($state_filename) {
        my @lines = ();
        my @new_lines = ();
        General::File_Operations::read_file(
            {
                "filename"            => $state_filename,
                "output-ref"          => \@lines,
            }
        );

        # Update state for the last entry matching the task name
        for (@lines) {
            if (/^$task_name/) {
                print STDERR "                  [INFO] Deleted from file: $_\n" if ($debug);
            } else {
                push @new_lines, $_;
            }
        }

        # Rewrite the file with updated states
        my $result = General::File_Operations::write_file(
            {
                "filename"            => "$state_filename",
                "output-ref"          => \@new_lines,
                "append-file"         => 0,
            }
        );
        print STDERR "\n                  Array written to file:\n                  " . (join "                  ", Dumper(@new_lines)) . "\n" if ($debug);

        if ($result != 0) {
            print "ERROR: Unable to update state of job in file '$state_filename'.\n";
            return;
        }
    }

    return;
}

# -----------------------------------------------------------------------------
# Updates the state entry of an already present task or adds a new entry.
#
# Input variables:
#    - Mandatory: Hash reference %params = Input parameters to the sub routine
#      which can be any of the following keys:
#       "task-name":
#           The name of the task to use. If no name is given then no change
#           will be done.
#       "state":
#           The state value to use. If incorrect state name is given the no
#           change will be done.
#
# Returns:
#    - Nothing
#
# -----------------------------------------------------------------------------
sub state_update {
    my %params = %{$_[0]};

    my $task_name = exists $params{"task-name"} ? $params{"task-name"} : "";
    my $state =  exists $params{"state"} ? $params{"state"} : "";

    print STDERR "                  [INFO] General::State_Machine::state_update\n" if ($debug);

    if ($task_name eq "") {
        print "ERROR: No 'task-name' was given, no state change was done.\n";
        return;
    }
    if ($state eq "") {
        print "ERROR: No 'state' was given, no state change was done.\n";
        return;
    } else {
        unless (exists $states{$state}) {
            print "ERROR: Invalid 'state' value '$state' was given, no state change was done.\n";
            print "Valid state values are:\n";
            for my $key (sort keys %states) {
                print "  $key\n";
            }
        }
    }

    # If a log file has been specified, update state information into that file.
    # Look for last line that contains the task that changed state and update that line
    # and then write back the whole content to the file.
    if ($state_filename) {
        if (exists $state_action{$task_name}) {
            # State already exist, update the state value in the file
            my @lines = ();
            General::File_Operations::read_file(
                {
                    "filename"            => $state_filename,
                    "output-ref"          => \@lines,
                }
            );

            # Update state for the last entry matching the task name
            for (my $i = $#lines; $i >= 0; $i--) {
                if ($lines[$i] =~ /^$task_name : \w+/) {
                    $lines[$i] =~ s/^.+$/$task_name : $state/;
                    last;
                }
            }

            # Rewrite the file with updated states
            my $result = General::File_Operations::write_file(
                {
                    "filename"            => "$state_filename",
                    "output-ref"          => \@lines,
                    "append-file"         => 0,
                }
            );

            if ($result != 0) {
                print "ERROR: Unable to update state of job in file '$state_filename'.\n";
                return;
            }
        } else {
            # State does not exist, add a new state value in the file
            my $result = General::File_Operations::write_file(
                {
                    "filename"            => "$state_filename",
                    "output-ref"          => [
                        "$task_name : $state"
                    ],
                    "eol-char"            => "\n",
                    "append-file"         => 1,
                }
            );
            if ($result != 0) {
                print "ERROR: Unable to add state of job in file '$state_filename'.\n";
                return;
            }
        }
    }

    if ($log_state_history) {
        my $timestamp = time();
        `echo -en "$timestamp Task=$task_name, New State=$state, Action=$states{$state}\n" >> $state_history_file`;
    }

    # Update the %state_action hash
    $state_action{$task_name} = $states{$state};

    print STDERR "                  [INFO] TASK NAME: $task_name\n" if ($debug);
    print STDERR "                  [INFO] STATE: $state\n" if ($debug);
    print STDERR "                  [INFO] State value of $state: $states{$state}\n" if ($debug);

    return;
}

# -----------------------------------------------------------------------------
# Updates the state entry of an already present task or adds a new entry.
#
# Input variables:
#    - Mandatory: Hash reference %params = Input parameters to the sub routine
#      which can be any of the following keys:
#       "task-name":
#           The name of the task to use. If no name is given then no change
#           will be done.
#       "state":
#           The state value to use. If incorrect state name is given the no
#           change will be done.
#
# Returns:
#    - Nothing
#
# -----------------------------------------------------------------------------
sub update_always_do_file {
    my $hash_ref;

    # If a log file has been specified, update always_do information into that file.
    if ($always_do_filename) {
        my @lines = ();
        for my $var_name ("always_execute_tasks", "always_ignore_error_in_tasks", "always_interrupt_tasks", "always_skip_tasks") {
            if ($var_name eq "always_execute_tasks") {
                $hash_ref = \%always_execute_tasks;
            } elsif ($var_name eq "always_ignore_error_in_tasks") {
                $hash_ref = \%always_ignore_error_in_tasks;
            } elsif ($var_name eq "always_interrupt_tasks") {
                $hash_ref = \%always_interrupt_tasks;
            } elsif ($var_name eq "always_skip_tasks") {
                $hash_ref = \%always_skip_tasks;
            }
            for my $key (sort keys %{$hash_ref}) {
                push @lines, "$var_name : $key";
            }
        }

        if (@lines) {
            my $result = General::File_Operations::write_file(
                {
                    "filename"            => "$always_do_filename",
                    "output-ref"          => \@lines,
                    "append-file"         => 0,
                }
            );

            if ($result != 0) {
                print "ERROR: Unable to update always-do of job in file '$always_do_filename'.\n";
                return;
            }
        }
    }
}

1;
