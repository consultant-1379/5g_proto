#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.7
#  Date     : 2023-05-24 17:32:15
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
use warnings;

use File::Basename qw(dirname);
use Cwd qw(abs_path);
use lib dirname(dirname abs_path $0) . '/lib';

use General::File_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;

my $script_dir = dirname abs_path $0;

my $ask_before_killing = 0;
my $delete_orphan_lock_files = 0;
my %file_pids;
my $just_print = 0;
my $kill_process = 0;
my $kill_signal = "SIGINT";
my $logfile = "";
my @pids_to_kill = ();
my %ps_pids;
my $rc = 0;
my $show_help = 0;
my $wait_time = 20;                 # Wait time for process to be killed without any updates to the all.log file
my @workspace_dirs = ();
my %wanted_workspace_dirs;

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "a|ask"                         => \$ask_before_killing,
    "d|delete-orphan-lock-files"    => \$delete_orphan_lock_files,
    "f|log_file=s"                  => \$logfile,
    "h|help"                        => \$show_help,
    "k|kill-process"                => \$kill_process,
    "n|just-print"                  => \$just_print,
    "s|kill-signal=s"               => \$kill_signal,
    "t|wait-time=i"                 => \$wait_time,
    "w|workspace-dir=s"             => \@workspace_dirs,
);

if ( $show_help ) {
    show_help();
    exit 0;
}

# Check that the user have specified existing workspace directories
for my $dir (@workspace_dirs) {
    $dir =~ s/\/$//;    # Remove trailing /
    unless (-d $dir) {
        print "Error:Workspace directory $dir does not exist\n";
        exit 1;
    }
    $dir = abs_path $dir;
    $wanted_workspace_dirs{$dir} = 1;
}

$kill_signal = uc($kill_signal);
unless ($kill_signal =~ /^SIG(INT|KILL|TERM|USR1|USR2)$/) {
    print "Wrong --kill-signal specified, only SIGINT, SIGKILL, SIGTERM, SIGUSR1 or SIGUSR2 allowed\n";
    exit 1;
}

# **************
# *            *
# * Main logic *
# *            *
# **************

# Start logging of output to file
if ($logfile) {
    General::Logging::log_enable("$logfile");
}

# Find any .lock files in the workspace directories
look_for_pid_in_workspaces_lock_files();

# Find any currently running execute_playlist.pl processes
look_for_running_execute_playlist_pids();

if ($delete_orphan_lock_files == 1) {
    delete_orphan_lock_files();
}

find_processes_to_kill();
if ($kill_process == 1) {
    if (@pids_to_kill) {
        kill_running_processes();
    }
}

# Stop logging of output to file
if ($logfile) {
    General::Logging::log_disable();
}

exit $rc;

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------------------------------------
sub delete_orphan_lock_files {
    my $deleted_cnt = 0;
    my $rc;
    my @result;

    General::Message_Generation::print_underlined_message(
        {
            "message"         => "Deleting orphan .lock files",
            "underline-char"  => "=",
        }
    );

    for my $pid (keys %file_pids) {
        unless (exists $ps_pids{$pid}) {
            print "Deleting orphan file: $file_pids{$pid}.lock\nFile data:\n";
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cat $file_pids{$pid}.lock",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );
            if ($rc == 0) {
                for (@result) {
                    print "    $_\n";
                }
            }
            if ($just_print == 0) {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "rm -fr $file_pids{$pid}.lock",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc == 0) {
                    print "File deleted\n\n";
                    $deleted_cnt++;
                } else {
                    print "File deletion failed\n" . (join "\n", @result) . "\n";;
                }
            } else {
                print "File not deleted because '--just-print' is specified\n\n";
            }
        }
    }
    if ($deleted_cnt > 0) {
        printf "%d orphan .lock file%s deleted\n\n", $deleted_cnt, $deleted_cnt > 1 ? "s" : "";
    } else {
        print "No orphan .lock files found or deleted\n\n";
    }
}

# -----------------------------------------------------------------------------
sub find_processes_to_kill {
    my $found_cnt = 0;

    General::Message_Generation::print_underlined_message(
        {
            "message"         => "Finding processes to kill",
            "underline-char"  => "=",
        }
    );

    for my $pid (keys %ps_pids) {
        if (%wanted_workspace_dirs) {
            # We should check pid workspace against user specified workspace directory
            if (exists $file_pids{$pid}) {
                my $dir = $file_pids{$pid};
                unless (exists $wanted_workspace_dirs{$dir}) {
                    print "Process id $pid (workspace $dir) does not match the wanted job specified with --workspace-dir\n";
                    print "Command: $ps_pids{$pid}\n\n";
                    next;
                }
            } else {
                # No .lock file found for this pid
                print "Process id $pid does not have any matching workspace directory, so no match can be made\n";
                print "Command: $ps_pids{$pid}\n\n";
                next;
            }
        }
        # If we reach here then either we found a matching --workspace-dir,
        # or user want to kill all running processes.
        push @pids_to_kill, $pid;
        if (exists $file_pids{$pid}) {
            print "Process id $pid (workspace $file_pids{$pid}) marked to be killed\n";
            print "Command: $ps_pids{$pid}\n\n";
        } else {
            print "Process id $pid marked to be killed\n";
            print "Command: $ps_pids{$pid}\n\n";
        }
        $found_cnt++;
    }
    if ($found_cnt > 0) {
        printf "%d process%s found\n\n", $found_cnt, $found_cnt > 1 ? "es" : "";
    } else {
        print "No processes found\n\n";
    }

}

# -----------------------------------------------------------------------------
sub kill_running_children {
    my @children = @_;
    my $killed_cnt = 0;
    my $stop_time;

    if ($just_print == 1) {
        print "No child process killed because '--just-print' is specified\n\n";
        return;
    }

    for my $pid (@children) {
        if (General::OS_Operations::process_send_signal($pid, "SIGTERM") == 0) {
            print "Child process id $pid no longer appear to be running, so skipped\n\n";
            next;
        }
        print "Child process id $pid sent the SIGTERM signal\n";

CHECK_AGAIN:

        $stop_time = time() + 5;

        # Now we need to wait a while to see that the process terminates
        while (time () < $stop_time) {
            if (General::OS_Operations::process_is_running($pid) == 0) {
                # Process is gone, exit the loop
                last;
            }
            sleep 1;
        }

        # Check if the process is gone and if not then send the SIGTERM signal
        if (General::OS_Operations::process_is_running($pid) == 0) {
            # Process is gone, take next process id
            $killed_cnt++;
            next;
        } else {
            # Maximum wait time for process to terminate has been exceeded.
            # Something really wrong with the process, just send the SIGKILL
            if (General::OS_Operations::process_send_signal($pid, "SIGKILL") == 0) {
                print "Child process id $pid no longer appear to be running, so skipped\n\n";
                $killed_cnt++;
                next;
            }
            print "Child process id $pid sent the SIGKILL signal\n";
            goto CHECK_AGAIN;
        }
    }

    if ($killed_cnt > 0) {
        printf "%d child process%s killed\n\n", $killed_cnt, $killed_cnt > 1 ? "es" : "";
    } else {
        print "No child processes found or killed\n\n";
    }
}

# -----------------------------------------------------------------------------
sub kill_running_processes {
    my $file_size = 0;
    my @children;
    my $killed_cnt = 0;
    my $last_signal;
    my $max_wait_time = time() + $wait_time + 120;   # Absolute maximum time to wait for the job to be killed
    my $rc;
    my @result;
    my $sleep_count;
    my $stop_time = time() + $wait_time;

    General::Message_Generation::print_underlined_message(
        {
            "message"         => "Killing running DAFT processes",
            "underline-char"  => "=",
        }
    );

    for my $pid (@pids_to_kill) {
        General::Message_Generation::print_underlined_message(
            {
                "message"         => "Process $pid",
                "underline-char"  => "-",
            }
        );

        if (General::OS_Operations::process_is_running($pid) == 0) {
            print "Process id $pid no longer appear to be running, so skipped\n\n";
            next;
        }

        # Fetch a list of children started by this task
        @children = General::OS_Operations::process_return_all_children($pid);

        if (@children) {
            printf "Process id $pid and it's children " . (join " ", @children) . " will be killed\n";
        } else {
            printf "Process id $pid will be killed\n";
        }

        if ($just_print == 1) {
            print "No process killed because '--just-print' is specified\n\n";
            next;
        }

        if ($ask_before_killing == 1) {
            my $answer = General::OS_Operations::get_user_validated_input("Are you sure you want to kill process id $pid ($ps_pids{$pid})\nAnswer Y or n ? ", '^(yes|y|no|n|)$', 0, 0);
            if ($answer =~ /^(no|n)$/i) {
                print "Skipping process id $pid\n\n";
                next;
            }
        }

        if (General::OS_Operations::process_send_signal($pid, $kill_signal) == 0) {
            print "Process id $pid no longer appear to be running, so skipped\n\n";
            kill_running_children(@children) if @children;
            next;
        }
        print "Process id $pid sent the $kill_signal signal\n";
        printf "Waiting maximum %s for process to terminate (with no updates to all.log file)\n", General::OS_Operations::convert_seconds_to_wdhms( $wait_time, 0);
        $last_signal = $kill_signal;

        if (exists $file_pids{$pid} && -f "$file_pids{$pid}/all.log") {
            # Fetch the current file size of the all.log file to be able to detect if it changes
            $file_size = (stat("$file_pids{$pid}/all.log"))[7];
        }

CHECK_AGAIN:

        $stop_time = time() + $wait_time;
        $sleep_count = 1;

        # Now we need to wait a while to see that the process terminates
        while (time() < $stop_time) {
            if (General::OS_Operations::process_is_running($pid) == 0) {
                # Process is gone, exit the loop
                last;
            } elsif (exists $file_pids{$pid} && -p "$file_pids{$pid}/input.pipe") {
                # The process is waiting for user input, exit the loop and then send SIGTERM signal.
                last;
            } elsif (exists $file_pids{$pid} && -f "$file_pids{$pid}/all.log") {
                # Fetch the current file size of the all.log file to be able to detect if it changes
                my $new_file_size = (stat("$file_pids{$pid}/all.log"))[7];
                if ($new_file_size > $file_size) {
                    # Something is still happening with the all.log which means the plexlist execution
                    # is probably still handling the kill signal, so extend the stop time.
                    $stop_time = time() + $wait_time;
                    $file_size = $new_file_size;
                    print "  The $file_pids{$pid}/all.log file is still changing (size=$file_size bytes), wait longer for playlist to finish\n";
                }
                # No change in the all.log file size
                if (time() >= $max_wait_time) {
                    # It has taken too long time for the process to be killed, so don't wait any longer, exit the loop
                    print "  We have waited long enough for the process to be terminated, exiting the wait even though the playlist might still be doing something\n";
                    last;
                }
            }
            sleep 1;
            $sleep_count++;
            if ($sleep_count >= 10) {
                # Write a progress message on the screen
                my $time_left = General::OS_Operations::convert_seconds_to_wdhms( $stop_time - time(), 0);
                my $task = "terminate";
                if (exists $file_pids{$pid} && -f "$file_pids{$pid}/state_report.info") {
                    $task = "finish task " . `tail -1 $file_pids{$pid}/state_report.info`;
                    $task =~ s/[\r\n]//;
                }
                printf "  Still waiting %s for process %d to %s\n", $time_left, $pid, $task;
                $sleep_count = 1;
            }
        }

        # Check if the process is gone and if not then send the SIGTERM signal
        if (General::OS_Operations::process_is_running($pid) == 0) {
            # Process is gone, take next process id
            $killed_cnt++;
            kill_running_children(@children) if @children;
            next;
        } else {
            # Maximum wait time for process to terminate has been exceeded, or process is waiting for user input
            if ($last_signal eq "SIGTERM") {
                # Something really wrong with the process, just send the SIGKILL
                $last_signal = "SIGKILL";
                if (General::OS_Operations::process_send_signal($pid, $last_signal) == 0) {
                    print "Process id $pid no longer appear to be running, so skipped\n\n";
                    $killed_cnt++;
                    kill_running_children(@children) if @children;
                    next;
                }
                print "Process id $pid sent the $last_signal signal\n";
                goto CHECK_AGAIN;

            } else {
                # The process did not get terminated or is waiting for user input, send SIGTERM signal.
                $last_signal = "SIGTERM";
                if (General::OS_Operations::process_send_signal($pid, $last_signal) == 0) {
                    print "Process id $pid no longer appear to be running, so skipped\n\n";
                    $killed_cnt++;
                    kill_running_children(@children) if @children;
                    next;
                }
                print "Process id $pid sent the $last_signal signal\n";
                goto CHECK_AGAIN;
            }
        }
    }

    if ($killed_cnt > 0) {
        printf "%d process%s killed\n\n", $killed_cnt, $killed_cnt > 1 ? "es" : "";
    } else {
        print "No processes found or killed\n\n";
    }
}

# -----------------------------------------------------------------------------
sub look_for_pid_in_workspaces_lock_files {
    my @errors;
    my @files;
    my $pid;
    my $rc;
    my @result;

    for my $directory ("$ENV{HOME}/workspaces", "/tmp/workspaces") {
        if (-d $directory) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "find $directory -maxdepth 1 -type f -name '*.lock'",
                    "hide-output"   => 1,
                    "return-output" => \@files,
                    "return-stderr" => \@errors,
                }
            );
            if ($rc == 0) {
                for my $filename (@files) {
                    $rc = General::File_Operations::read_file(
                        {
                            "filename"            => $filename,
                            "output-ref"          => \@result,
                            "include-pattern"   => '^PID=\d+',
                        }
                    );
                    if ($rc == 0 && scalar @result == 1 && $result[0] =~ /^PID=(\d+)/) {
                        $pid = $1;
                        $filename =~ s/\.lock$//;   # Remove the .lock extension which should leave the directory name
                        $file_pids{$pid} = $filename;
                    }
                }
            }

        }
    }
}

# -----------------------------------------------------------------------------
sub look_for_running_execute_playlist_pids {
    my @errors;
    my @files;
    my $rc;
    my @result;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "ps -eo pid,args",
            "hide-output"   => 1,
            "return-output" => \@result,
            "return-stderr" => \@errors,
        }
    );
    if ($rc == 0) {
        for (@result) {
            if (/^\s*(\d+)\s+(.*execute_playlist\.pl.*)/) {
                $ps_pids{$1} = $2;
            }
        }
    }
}

# -----------------------------------------------------------------------------
sub show_help {
    print <<EOF;

Description:
============

This script can be used to kill running DAFT processes (execute_playlist.pl) in
a controlled or non-controlled way.
It will look for running DAFT processes depending on a number of input parameters
and it will then either:
- Send special signal into the running DAFT process that will cause a controlled
  termination of the process which might include executing fallback logic and
  creating log files etc.
- Or if this does not successfully terminate the running DAFT process it will
  brutally kill the process.
- It will also terminate any child processes started by these DAFT processes.


Syntax:
=======

$0 [<OPTIONAL>]

<OPTIONAL> are one or more of the following parameters:
  -a                | --ask
  -d                | --delete-orphan-lock-files
  -f <filename>     | --log_file <filename>
  -h                | --help
  -k                | --kill-process
  -n                | --just-print
  -s <string>       | --kill-signal <string>
  -t <integer>      | --wait-time <integer>
  -w <directory>    | --workspace-dir <directory>

where:

  -a
  --ask
  -----
  If specified then the user will have to confirm before any matching process
  will be killed.


  -d
  --delete-orphan-lock-files
  --------------------------
  If specified then any .lock file in the workspace directory will be deleted
  if there is no process with the same process id currently running.
  This might be useful to clean up orphan .lock files where DAFT was killed
  without properly cleaning up.


  -f <filename>
  --log_file <filename>
  ---------------------
  If specified then all executed commands and their results will be written
  into the given <filename>.
  If not specified then no log file is created and only what is printed to STDOUT
  will be shown.


  -h
  --help
  ------
  Shows this help information.


  -k
  --kill-process
  --------------
  If specified then the matching running processes will be killed.
  If not specified then only information about running processes will be printed
  but no processes will be killed.


  -n
  --just-print
  -----------------------
  If specified then it will just print information about the running DAFT
  processes but will not kill anything.


  -s <string>
  --kill-signal <string>
  ----------------------
  Specifies the kill signal to send the process, which can be one of the following:

    - SIGINT
      Send the interrupt signal (similar to as if CTRL-C was pressed).
      This will interrupt any current ongoing task in the DAFT job and if this
      does not terminate the job then a SIGTERM will be sent.
      This is the default if the parameter is not specified.

    - SIGQUIT
      Send the quit signal (similar to as if CTRL\\ was pressed).
      This will interrupt any current ongoing task in the DAFT job with a failure
      and if this does not terminate the job then a SIGTERM will be sent.

    - SIGUSR1
      Send the user-defined signal 1.
      This will cause the job to go into Fallback and execute any fallback steps
      as soon as the current running task has finished.
      If this does not terminate the job then a SIGTERM is sent.

    - SIGUSR2
      Send the user-defined signal 2.
      This will cause the job to gracefully quit without executing any furter
      tasks as soon as the current running task has finished.
      If this does not terminate the job then a SIGTERM is sent.

    - SIGTERM
      Send the terminate signal.
      This will cause the job to immediately terminate but do a cleanup by
      closing any open files and creating packed workspace files if so wanted.

    - SIGKILL
      Send the ultimate kill signal.
      This will cause the job to immediately terminate without any cleanup.
      This should only be used if everything else fails.


  -t <integer>
  --wait-time <integer>
  ---------------------
  If specified it will change the maximum time to wait (in seconds) for the
  process to be killed when there is no longer any updates to the all.log
  file before force killing the process with the SIGTERM signal.
  Changing the default time to something larger e.g. 300 seconds might be
  useful if the killed process process needs to finish collecting trouble
  shooting logs which can take a few minutes to finish.

  If not specified the default value is 20 seconds.


  -w <directory>
  --workspace-dir <directory>
  ---------------------------
  If specified then only running processes matching this workspace directory
  will be killed.
  This parameter can be specified multiple times if more running processes
  should be killed.
  If not specified then all running processes will be killed.
  See also --kill-process which also must be specified if processes should be
  killed.
EOF
}
