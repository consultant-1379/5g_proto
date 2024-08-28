package General::OS_Operations;

################################################################################
#
#  Author   : eustone, everhel
#
#  Revision : 1.93
#  Date     : 2024-05-24 17:24:45
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
use POSIX ":sys_wait_h";

use Exporter qw(import);

our @EXPORT_OK = qw(
    background_process_interrupt
    background_process_is_running
    background_process_kill
    background_process_kill_all
    background_process_mark_not_running
    background_process_run
    background_process_send_signal
    background_processes
    base64_decode
    base64_encode
    check_pipefail
    command_in_path
    convert_seconds_to_dhms
    convert_seconds_to_wdhms
    detailed_datetime_str
    detailed_time
    detailed_time_str
    dump_call_stack
    epoch_time
    format_time_iso
    format_time_left
    format_time_localtime
    get_log_collect_script
    get_mounted_partition_path
    get_mounted_partition_sizes
    get_ssh_key
    get_user_input
    get_user_keypress
    get_user_rc
    get_user_validated_input
    is_leap_year
    is_ssh_available
    process_information
    process_is_running
    process_kill
    process_return_all_children
    process_return_children
    process_send_signal
    return_ip_addresses
    seconds_between_dates
    send_command
    send_command_via_ssh
    set_temporary_output_file
    set_temporary_stderr_file
    signal_captured
    signal_captured_dont_clear
    signal_capture_disable
    signal_capture_enable
    sleep_with_progress
    write_current_temporary_output_to_log
    write_last_temporary_output_to_progress
    );

use Cwd qw(abs_path);
use File::Basename qw(dirname basename);
use POSIX qw(mkfifo);
use MIME::Base64;

use General::File_Operations;
use General::Logging;

our $caller_directory = abs_path dirname $0;
our $caller_tag = basename $0;
my $command_execution_in_progress = 0;  # Set to 0 when the send_command subroutine
                                        # have finished executing and set to 1 when
                                        # a command is still executing.

our $command_prompt = '$ '; # Prompt to print in front of command sent to the node
                            # when echoing the command output to the screen.
                            # Examples:
                            # '$ '
                            # '# '
                            # '-> '
                            # 'Command -> '

our $commented_result = 0;  # 0=The command, rc and result will not be preceeded
                            # by a comment character "# ".
                            # 1=The command, rc and result will be preceeded by
                            # a comment character "# ".
our $console_input_available = 1;
our $console_output_available = 1;
my $hashline = "#" x 34;
our $input_available = 1;

# Keep track of the return code information from the last
# executed command which can be read by the caller of the
# send_command subroutine.
#
# Reserved Exit Codes (taken from page: https://www.tldp.org/LDP/abs/html/exitcodes.html):
# Exit Code Number    Meaning                                                     Example                     Comments
# 1                   Catchall for general errors                                 let "var1 = 1/0"            Miscellaneous errors, such as "divide by zero" and other impermissible operations
# 2                   Misuse of shell builtins (according to Bash documentation)  empty_function() {}         Missing keyword or command, or permission problem (and diff return code on a failed binary file comparison).
# 126                 Command invoked cannot execute                              /dev/null                   Permission problem or command is not an executable
# 127                 "command not found"                                         illegal_command             Possible problem with $PATH or a typo
# 128                 Invalid argument to exit                                    exit 3.14159                exit takes only integer args in the range 0 - 255 (see first footnote)
# 128+n               Fatal error signal "n"                                      kill -9 $PPID of script     $? returns 137 (128 + 9)
# 130                 Script terminated by Control-C                              Ctl-C                       Control-C is fatal error signal 2, (130 = 128 + 2, see above)
# 255*                Exit status out of range                                    exit -1                     exit takes only integer args in the range 0 - 255
#
# According to the above table, exit codes 1 - 2, 126 - 165, and 255 [1] have special meanings, and should therefore be avoided for user-specified exit parameters.
# Ending a script with exit 127 would certainly cause confusion when troubleshooting (is the error code a "command not found" or a user-defined one?).
# However, many scripts use an exit 1 as a general bailout-upon-error. Since exit code 1 signifies so many possible errors, it is not particularly useful in debugging.
#
# There has been an attempt to systematize exit status numbers (see /usr/include/sysexits.h), but this is intended for C and C++ programmers.
# A similar standard for scripting might be appropriate.
# The author of this document proposes restricting user-defined exit codes to the range 64 - 113 (in addition to 0, for success), to conform with the C/C++ standard.
# This would allot 50 valid codes, and make troubleshooting scripts more straightforward.
# [2] All user-defined exit codes in the accompanying examples to this document conform to this standard, except where overriding circumstances exist, as in Example 9-2.
#
# Note
#
# Issuing a $? from the command-line after a shell script exits gives results consistent with the table above only from the Bash or sh prompt.
# Running the C-shell or tcsh may give different values in some cases.
# Notes
# [1] Out of range exit values can result in unexpected exit codes.
#     An exit value greater than 255 returns an exit code modulo 256.
#     For example, exit 3809 gives an exit code of 225 (3809 % 256 = 225).
# [2] An update of /usr/include/sysexits.h allocates previously unused exit codes from 64 - 78.
#     It may be anticipated that the range of unallotted exit codes will be further restricted in the future.
#     The author of this document will not do fixups on the scripting examples to conform to the changing standard.
#     This should not cause any problems, since there is no overlap or conflict in usage of exit codes between compiled C/C++ binaries and shell scripts.
#
#
# Signal values printed with 'kill -l' command.
#
# From a Linux machine including the Cluster:
# SC-1:/home/daft # kill -l
# 1) SIGHUP       2) SIGINT       3) SIGQUIT      4) SIGILL        5) SIGTRAP
# 6) SIGABRT      7) SIGBUS       8) SIGFPE       9) SIGKILL      10) SIGUSR1
# 11) SIGSEGV     12) SIGUSR2     13) SIGPIPE     14) SIGALRM     15) SIGTERM
# 16) SIGSTKFLT   17) SIGCHLD     18) SIGCONT     19) SIGSTOP     20) SIGTSTP
# 21) SIGTTIN     22) SIGTTOU     23) SIGURG      24) SIGXCPU     25) SIGXFSZ
# 26) SIGVTALRM   27) SIGPROF     28) SIGWINCH    29) SIGIO       30) SIGPWR
# 31) SIGSYS      34) SIGRTMIN    35) SIGRTMIN+1  36) SIGRTMIN+2  37) SIGRTMIN+3
# 38) SIGRTMIN+4  39) SIGRTMIN+5  40) SIGRTMIN+6  41) SIGRTMIN+7  42) SIGRTMIN+8
# 43) SIGRTMIN+9  44) SIGRTMIN+10 45) SIGRTMIN+11 46) SIGRTMIN+12 47) SIGRTMIN+13
# 48) SIGRTMIN+14 49) SIGRTMIN+15 50) SIGRTMAX-14 51) SIGRTMAX-13 52) SIGRTMAX-12
# 53) SIGRTMAX-11 54) SIGRTMAX-10 55) SIGRTMAX-9  56) SIGRTMAX-8  57) SIGRTMAX-7
# 58) SIGRTMAX-6  59) SIGRTMAX-5  60) SIGRTMAX-4  61) SIGRTMAX-3  62) SIGRTMAX-2
# 63) SIGRTMAX-1  64) SIGRTMAX
#
# From a a Windows machine running Cygwin:
# $ kill -l
# 1) SIGHUP       2) SIGINT       3) SIGQUIT      4) SIGILL        5) SIGTRAP
# 6) SIGABRT      7) SIGEMT       8) SIGFPE       9) SIGKILL      10) SIGBUS
# 11) SIGSEGV     12) SIGSYS      13) SIGPIPE     14) SIGALRM     15) SIGTERM
# 16) SIGURG      17) SIGSTOP     18) SIGTSTP     19) SIGCONT     20) SIGCHLD
# 21) SIGTTIN     22) SIGTTOU     23) SIGIO       24) SIGXCPU     25) SIGXFSZ
# 26) SIGVTALRM   27) SIGPROF     28) SIGWINCH    29) SIGPWR      30) SIGUSR1
# 31) SIGUSR2     32) SIGRTMIN    33) SIGRTMIN+1  34) SIGRTMIN+2  35) SIGRTMIN+3
# 36) SIGRTMIN+4  37) SIGRTMIN+5  38) SIGRTMIN+6  39) SIGRTMIN+7  40) SIGRTMIN+8
# 41) SIGRTMIN+9  42) SIGRTMIN+10 43) SIGRTMIN+11 44) SIGRTMIN+12 45) SIGRTMIN+13
# 46) SIGRTMIN+14 47) SIGRTMIN+15 48) SIGRTMIN+16 49) SIGRTMAX-15 50) SIGRTMAX-14
# 51) SIGRTMAX-13 52) SIGRTMAX-12 53) SIGRTMAX-11 54) SIGRTMAX-10 55) SIGRTMAX-9
# 56) SIGRTMAX-8  57) SIGRTMAX-7  58) SIGRTMAX-6  59) SIGRTMAX-5  60) SIGRTMAX-4
# 61) SIGRTMAX-3  62) SIGRTMAX-2  63) SIGRTMAX-1  64) SIGRTMAX
#
our $last_rc = 0;
our $last_rc_coredump_created = 0;
our $last_rc_original_value = 0;
our $last_rc_signal_value = 0;

# Keep track of last issued command
our $last_command = "";
our $last_command_count = 0;
our $last_command_seq_id = "0.0.0";

# The top level directory of the package
our $package_directory = abs_path "$caller_directory/../..";

# Signal handling
my %signal_counter;

# Hash that contains information about any started child processes started by DAFT.
# $started_background_processes{$pid}{'status'}: Contains the value =1 if process is started
#                                                or value =0 if already killed.
# $started_background_processes{$pid}{'command'}: Command line used for starting the background process.
our %started_background_processes;

# Logfile used for temporary output during the send_command subroutine
# $< = Real User ID of this process.
# $$ = The process number of the Perl running this script.
our $temporary_command_filename = "/tmp/command_$<_$$.cmd";
our $temporary_input_filename = "/tmp/input_$<_$$.pipe";
our $temporary_output_filename = "/tmp/command_$<_$$.log";
our $temporary_stderr_filename = "/tmp/stderr_$<_$$.log";

my $time_hires_available = 0;
eval 'require Time::HiRes;';
if ($@) {
    print "Unable to require Time::HiRes\n\$@=$@\n";
    $time_hires_available = 0;
} else {
    $time_hires_available = 1;
    import 'Time::HiRes  qw(gettimeofday)';
}

my $invalid_entry_with_allowed_values = 0;  # If a user enters an invalid value,
                                            # this variable controls if the error
                                            # message includes a list of the valid
                                            # values (=1) or not (=0).
                                            # One reason for having it set to 0 might
                                            # be that we allow more values than is
                                            # shown to the user to allow power users
                                            # to have more control over the playlist
                                            # execution when something goes wrong.

# Set some default values at invocation of this library for the first time
check_console();
check_pipefail();

# -----------------------------------------------------------------------------
# Send the SIGINT (CTRL-C) signal to a specified background process.
# What happens in the process depends on how it handles the SIGINT signal,
# if the process intercepts the signal then it can e.g. properly close down
# files etc, otherwise the process will probably just be interrupted and
# terminated.
#
# Input variables:
#    - The process id of the background process to send the SIGINT signal to.
#
# Return values:
#    - 0: If the process did not exist or could not be sent the SIGINT signal.
#    - 1: If the process was sent the SIGINT signal.
#
# -----------------------------------------------------------------------------
sub background_process_interrupt {
    my $pid = shift;
    my $rc;

    # Check that we received a command
    return 0 unless $pid;
    return 0 unless ($pid =~ /^\d+$/);
    return 0 unless (exists $started_background_processes{$pid} && $started_background_processes{$pid}{'status'} == 1);

    return process_send_signal($pid, "SIGINT");
}

# -----------------------------------------------------------------------------
# Check if the specified background process is still running.
#
# Input variables:
#    - The process id of the background process to check.
#
# Return values:
#    - 0: If the process did not exist or is no longer running.
#    - 1: If the process is still running.
#
# -----------------------------------------------------------------------------
sub background_process_is_running {
    my $pid = shift;
    my $rc;

    # Check that we received a command
    return 0 unless $pid;
    return 0 unless ($pid =~ /^\d+$/);
    return 0 unless (exists $started_background_processes{$pid} && $started_background_processes{$pid}{'status'} == 1);

    $rc = process_is_running($pid);
    if ($rc == 0) {
        # Process id is not running
        $started_background_processes{$pid}{'status'} = 0;
    }
    return $rc;
}

# -----------------------------------------------------------------------------
# Kill a specified background process.
# The provided process id will be killed if it is executing.
#
# Input variables:
#    - The process id of the background process to kill.
#
# Return values:
#    - 0: If the process did not exist or could not be killed.
#    - 1: If the process was killed.
#
# -----------------------------------------------------------------------------
sub background_process_kill {
    my $pid = shift;
    my $rc;
    my $stop_time = time() + 10;    # Wait max 10 seconds for the process to terminate

    # Check that we received a command
    return 0 unless $pid;
    return 0 unless ($pid =~ /^\d+$/);
    return 0 unless (exists $started_background_processes{$pid} && $started_background_processes{$pid}{'status'} == 1);

    # Kill the process, wait max 10 seconds for it and it's children to die
    $rc = process_kill($pid, 10);
    if ($rc == 1) {
        # The process was killed
        $started_background_processes{$pid}{'status'} = 0;
    }
    return $rc;
}

# -----------------------------------------------------------------------------
# Kill all started background process.
# When called, it will kill all background processes that are listed in the
# %started_background_processes hash where the key {'status'} == 1.
#
# Input variables:
#    -
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub background_process_kill_all {
    my $rc;

    for my $pid (keys %started_background_processes) {
        if ($started_background_processes{$pid}{'status'} == 1) {
            $rc = General::OS_Operations::background_process_kill($pid);
            if ($rc == 1) {
                # Process was killed, now delete the hash entry
                delete $started_background_processes{$pid};
            }
        }
    }
}

# -----------------------------------------------------------------------------
# Mark the specified background process as not running.
# This might be useful if a process should be kept running after the software
# that started it stops executing.
#
# Input variables:
#    - The process id of the background process to mark as not running.
#
# Return values:
#    - 0: If the process did not exist or is no longer running.
#    - 1: If the process is marked as not running.
#
# -----------------------------------------------------------------------------
sub background_process_mark_not_running {
    my $pid = shift;
    my $rc;

    # Check that we received a command
    return 0 unless $pid;
    return 0 unless ($pid =~ /^\d+$/);
    return 0 unless (exists $started_background_processes{$pid} && $started_background_processes{$pid}{'status'} == 1);

    # Process id is marked as not running
    $started_background_processes{$pid}{'status'} = 0;

    return 1;
}

# -----------------------------------------------------------------------------
# Run command as a background process.
# The provided command is executed as a detached background process and the
# functions returns the process id.
#
# Input variables:
#    - The command to run in the background, please note that it MUST NOT
#      contain the '&' character.
#
# Return values:
#    Process id of the started background process if >0 or 0 if something went
#    wrong when starting the process.
#
# RESULT: When using a log file Perl starts a "sh -c xxxxxxx" command which
# results in an extra process that runs the command and then even though the
# process started with the fork is killed the second command process started
# with the sh -c keep running. Actually both processes keeps running.
# -----------------------------------------------------------------------------
sub background_process_run {
    my $command = shift;
    my $filename = shift;

    $filename = "" unless $filename;

    # Check that we received a command
    return 0 unless $command;

    if ($filename eq "") {
        $command = "$package_directory/bin/run_command_with_redirect.bash no_redirect no_redirect $command";
    } else {
        $command = "$package_directory/bin/run_command_with_redirect.bash to_file $filename to_stdout $command";
    }

    my $pid = fork;
    return 0 unless defined $pid;
    if ($pid == 0) {
        # We are now executing in the child process.
        { exec("$command"); }
        # We should not get here unless the forking failed e.g. because
        # the command does not exist or something else went wrong with
        # the forking.
        $pid = 0;
        print "\n>>>>>>>>>>>>>>>>>>>>>>>>>>> Child Process died <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n\n";
    }
    if ($pid != 0) {
        # Save pid in case we need to kill it
        $started_background_processes{$pid}{'status'} = 1;
        $started_background_processes{$pid}{'command'} = $command;
    }

    return $pid;
}

# -----------------------------------------------------------------------------
# Send the specified signal to a specified background process.
# What happens in the process depends on how it handles the signal,
# if the process intercepts the signal then it can e.g. properly close down
# files etc, otherwise the process will probably just be interrupted and
# terminated.
#
# Input variables:
#    - The process id of the background process to send the SIGINT signal to.
#    - The signal to send e.g. SIGABRT.
#
# Return values:
#    - 0: If the process did not exist or could not be sent the signal.
#    - 1: If the process was sent the signal.
#
# -----------------------------------------------------------------------------
sub background_process_send_signal {
    my $pid = shift;
    my $signal = shift;
    my $rc;

    # Check that we received a command
    return 0 unless $pid;
    return 0 unless ($pid =~ /^\d+$/);
    return 0 unless (exists $started_background_processes{$pid} && $started_background_processes{$pid}{'status'} == 1);
    return 0 unless $signal;

    return process_send_signal($pid, $signal);
}

# -----------------------------------------------------------------------------
# Return an array of running background processes.
# It will only return process id's where the status==1.
#
# Input variables:
#    - Nothing
#
# Return values:
#    - Array of running background process id's.
#    - Empty array if there are no background processes.
#
# -----------------------------------------------------------------------------
sub background_processes {
    my @processes = ();
    my $rc;

    for my $pid (sort keys %started_background_processes) {
        next if ($started_background_processes{$pid}{'status'} == 0);
        push @processes, $pid;
    }

    return @processes;
}

# -----------------------------------------------------------------------------
# Decode an base64 encoded string into clear text.
#
# Input variables:
#    - Base64 encoded string.
#
# Return values:
#    - Clear text string.
#
# -----------------------------------------------------------------------------
sub base64_decode {
    my $input = shift;

    return MIME::Base64::decode_base64($input);
}

# -----------------------------------------------------------------------------
# Encode a clear text string into an base64 encoded string.
#
# Input variables:
#    - Clear text string.
#
# Return values:
#    - Base64 encoded string.
#
# -----------------------------------------------------------------------------
sub base64_encode {
    my $input = shift;

    return MIME::Base64::encode_base64($input);
}

# -----------------------------------------------------------------------------
# Set default values for variables $console_input_available,
# $console_output_available and $input_available.
# Check if STDIN and STDOUT file handles are attached to a TTY terminal.
# I.e. if it's possible to get user input from STDIN (keyboard) or to print
# output to STDOUT (screen).
#
# Input variables:
#    -
#
# Returns:
#    -
#
# -----------------------------------------------------------------------------
sub check_console {

    # Check if STDIN is attached to TTY
    if (-t STDIN) {
        $console_input_available = 1;
        $input_available = 1;
    } else {
        $console_input_available = 0;
        $input_available = 0;
    }

    # Check if STDOUT is attached to TTY
    if (-t STDOUT) {
        $console_output_available = 1;
    } else {
        $console_output_available = 0;
    }
}

# -----------------------------------------------------------------------------
# Check if the default shell that Perl uses for system() and `` calls, which
# normally defaults to "/bin/sh", supports the "set -o pipefail" option.
# On Ubuntu variants it seems like "/bin/sh" points to "/bin/dash" which does
# not support this option and it prints:
#   sh: 1: set: Illegal option -o pipefail
# and command execution fails.
# So this subroutine adds a check if this option is supported and if not
# exits with a failure.
#
# Input variables:
#    -
#
# Returns:
#    -
#
# -----------------------------------------------------------------------------
sub check_pipefail {

    `set -o pipefail; echo hello`;
    if ($?) {
        print "\nThe default shell used by Perl '/bin/sh' does not support the 'set -o pipefail' option.\n\n";
        print "Please change the default shell to something else e.g. '/bin/bash' by using for example the command:\nsudo ln -fs /bin/bash /bin/sh\n\nIt currently point to the following binary:\n";
        system("ls -l /bin/sh");
        exit 1;
    } else {
        return 0;
    }
}

# -----------------------------------------------------------------------------
# Check if specified command exist in the PATH.
#
# Input variables:
#    - Command to check (string).
#
# Returns:
#    - 0:  Command exist in the PATH.
#    - >0: Command does not exist in the PATH.
sub command_in_path {
    my $command = shift;

    return send_command(
        {
            "command"       => "which $command",
            "hide-output"   => 1,
        }
    );
}

# -----------------------------------------------------------------------------
# Convert number of seconds into a more human readable string containing days,
# hours, minutes and seconds.
#
# Input variables:
#    - Number of seconds (integer).
#    - Abbreviated output (0=no, 1=yes, 0 is default).
#      If 0 it returns back "X days X hours X minutes X seconds" and if
#      1 it returns "XdXhXmXs".
#
# Returns:
#    - A string containing how many days, hours, minutes and seconds the
#      input value is. If any value is 0 then it's not printed.
sub convert_seconds_to_dhms {
    my $seconds = shift;
    my $abbreviated_output = shift;
    my $days = 0;
    my $hours = 0;
    my $minutes = 0;
    my $result = "";
    my $sign = "";

    if ($seconds =~ /^-(\d+)$/) {
        $sign = "-";
        $seconds = $1;
    } elsif ($seconds =~ /^\d+$/) {
        $sign = "";
    } else {
        return "Invalid input, first parameter must be an integer value";
    }

    $abbreviated_output = 0 unless defined $abbreviated_output;
    if ($abbreviated_output =~ /^(yes|true)$/i) {
        $abbreviated_output = 1;
    } elsif ($abbreviated_output =~ /^(no|false|0)$/i) {
        $abbreviated_output = 0;
    } else {
        $abbreviated_output = 1;
    }

    if ($seconds >= 86400) { # 86400=3600*24 i.e. seconds per day
        $days = int($seconds / 86400);
        $seconds = $seconds - ($days * 86400);
        if ($abbreviated_output == 1) {
            $result .= "${days}d";
        } else {
            if ($days > 1) {
                $result .= "$days days ";
            } else {
                $result .= "$days day ";
            }
        }
    }

    if ($seconds >= 3600) { # i.e. seconds per hour
        $hours = int($seconds / 3600);
        $seconds = $seconds - ($hours * 3600);
        if ($abbreviated_output == 1) {
            $result .= "${hours}h";
        } else {
            if ($hours > 1) {
                $result .= "$hours hours ";
            } else {
                $result .= "$hours hour ";
            }
        }
    }

    if ($seconds >= 60) { # i.e. seconds per minute
        $minutes = int($seconds / 60);
        $seconds = $seconds - ($minutes * 60);
        if ($abbreviated_output == 1) {
            $result .= "${minutes}m";
        } else {
            if ($minutes > 1) {
                $result .= "$minutes minutes ";
            } else {
                $result .= "$minutes minute ";
            }
        }
    }

    if ($seconds > 0) {
        if ($abbreviated_output == 1) {
            $result .= "${seconds}s";
        } else {
            if ($seconds > 1) {
                $result .= "$seconds seconds";
            } else {
                $result .= "$seconds second";
            }
        }
    } elsif ($result eq "") {
        if ($abbreviated_output == 1) {
            $result .= "<1s";
        } else {
            $result .= "< 1 second";
        }
    }

    $result =~ s/\s+$//;

    return "${sign}$result";
}

# -----------------------------------------------------------------------------
# Convert number of seconds into a more human readable string containing weeks,
# days, hours, minutes and seconds.
#
# Input variables:
#    - Number of seconds (integer or float).
#    - Abbreviated output (0=no, 1=yes, 0 is default).
#      If 0 it returns back "X weeks X days X hours X minutes X seconds" and if
#      1 it returns "XwXdXhXmXs".
#
# Returns:
#    - A string containing how many weeks, days, hours, minutes and seconds the
#      input value is. If any value is 0 then it's not printed.
sub convert_seconds_to_wdhms {
    my $seconds = shift;
    my $abbreviated_output = shift;
    my $days = 0;
    my $hours = 0;
    my $milliseconds = "";
    my $minutes = 0;
    my $result = "";
    my $sign = "";
    my $weeks = 0;

    if ($seconds =~ /^(-?\d+)(\.\d+)$/) {
        $seconds = $1;
        $milliseconds = $2;
    }
    if ($seconds =~ /^-(\d+)$/) {
        $sign = "-";
        $seconds = $1;
    } elsif ($seconds =~ /^\d+$/) {
        $sign = "";
    } else {
        return "Invalid input, first parameter must be an integer value";
    }

    $abbreviated_output = 0 unless defined $abbreviated_output;
    if ($abbreviated_output =~ /^(yes|true)$/i) {
        $abbreviated_output = 1;
    } elsif ($abbreviated_output =~ /^(no|false|0)$/i) {
        $abbreviated_output = 0;
    } else {
        $abbreviated_output = 1;
    }

    if ($seconds >= 604800) { # 604800=3600*24*7 i.e. seconds per week
        $weeks = int($seconds / 604800);
        $seconds = $seconds - ($weeks * 604800);
        if ($abbreviated_output == 1) {
            $result .= "${weeks}w";
        } else {
            if ($weeks > 1) {
                $result .= "$weeks weeks ";
            } else {
                $result .= "$weeks week ";
            }
        }
    }

    if ($seconds >= 86400) { # 86400=3600*24 i.e. seconds per day
        $days = int($seconds / 86400);
        $seconds = $seconds - ($days * 86400);
        if ($abbreviated_output == 1) {
            $result .= "${days}d";
        } else {
            if ($days > 1) {
                $result .= "$days days ";
            } else {
                $result .= "$days day ";
            }
        }
    }

    if ($seconds >= 3600) { # i.e. seconds per hour
        $hours = int($seconds / 3600);
        $seconds = $seconds - ($hours * 3600);
        if ($abbreviated_output == 1) {
            $result .= "${hours}h";
        } else {
            if ($hours > 1) {
                $result .= "$hours hours ";
            } else {
                $result .= "$hours hour ";
            }
        }
    }

    if ($seconds >= 60) { # i.e. seconds per minute
        $minutes = int($seconds / 60);
        $seconds = $seconds - ($minutes * 60);
        if ($abbreviated_output == 1) {
            $result .= "${minutes}m";
        } else {
            if ($minutes > 1) {
                $result .= "$minutes minutes ";
            } else {
                $result .= "$minutes minute ";
            }
        }
    }

    if ($seconds > 0) {
        if ($abbreviated_output == 1) {
            $result .= "${seconds}${milliseconds}s";
        } else {
            if ($seconds > 1) {
                $result .= "$seconds$milliseconds seconds";
            } else {
                $result .= "$seconds$milliseconds second";
            }
        }
    } elsif ($milliseconds ne "") {
        if ($abbreviated_output == 1) {
            $result .= "0${milliseconds}s";
        } else {
            $result .= "0$milliseconds second";
        }
    } elsif ($result eq "") {
        if ($abbreviated_output == 1) {
            $result .= "<1s";
        } else {
            $result .= "< 1 second";
        }
    }

    $result =~ s/\s+$//;

    if ($abbreviated_output == 0 && $weeks > 52) {
        my $years = int($weeks / 52);
        $weeks = $weeks - ($years * 52);
        $result .= " (about";
        if ($years > 0) {
            $result .= " $years" . ($years > 1 ? " years" : " year");
        }
        if ($weeks > 0) {
            $result .= " $weeks" . ($weeks> 1 ? " weeks" : " week");
        }
        $result .= ")";
    }

    return "${sign}$result";
}

# -----------------------------------------------------------------------------
# Return a formated datetime string including date and time and microseconds to
# the user.
#
# Input variables:
#    - Optional: Number of seconds since the epoch.
#    -   Optional: Number of microseconds, if not provided then value 0 is used.
#    If not provided then current time is used.
#
# Returns:
#    - A scalar string containing a formatted date and time string in the
#      following format: yyyy-mm-dd hh:mm:ss.mysecs
#
# -----------------------------------------------------------------------------
sub detailed_datetime_str {
    my ($epochseconds, $microseconds) = @_;

    if (defined $epochseconds) {
        unless (defined $microseconds) {
            $microseconds = 0;
        }
    } else {
        if ($time_hires_available) {
            ($epochseconds, $microseconds) = Time::HiRes::gettimeofday();
        } else {
            $epochseconds = time();
            $microseconds = 0;
        }
    }

    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime($epochseconds);

    return sprintf "%04d-%02d-%02d %02d:%02d:%02d.%06d", 1900+$year, $mon+1, $mday, $hour, $min, $sec, $microseconds;
}

# -----------------------------------------------------------------------------
# Return a timestamp as a floating point value with number of seconds and
# microseconds since the epoch.
#
# Input variables:
#    -
#
# Return values:
#    Floating point value
#
# -----------------------------------------------------------------------------
sub detailed_time {
    my $epochseconds;
    my $microseconds;

    if ($time_hires_available) {
        ($epochseconds, $microseconds) = Time::HiRes::gettimeofday();
    } else {
        $epochseconds = time();
        $microseconds = 0;
    }
    return  sprintf "%f", (sprintf "%d.%06d", $epochseconds, $microseconds);
}

# -----------------------------------------------------------------------------
# Return a formated time string including time and microseconds to the user.
#
# Input variables:
#    - Optional: Number of seconds since the epoch.
#    -   Optional: Number of microseconds, if not provided then value 0 is used.
#    If not provided then current time is used.
#
# Returns:
#    - A scalar string containing a formatted time string in the following
#      format: hh:mm:ss.mysecs
#
# -----------------------------------------------------------------------------
sub detailed_time_str {
    my ($epochseconds, $microseconds) = @_;

    if (defined $epochseconds) {
        unless (defined $microseconds) {
            $microseconds = 0;
        }
    } else {
        if ($time_hires_available) {
            ($epochseconds, $microseconds) = Time::HiRes::gettimeofday();
        } else {
            $epochseconds = time();
            $microseconds = 0;
        }
    }

    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime($epochseconds);

    return sprintf "[%02d:%02d:%02d.%06d]", $hour, $min, $sec, $microseconds;
}

# -----------------------------------------------------------------------------
# Dump the Perl call stack to STDERR which shows a list of which sub routine
# calls has been made to reach a certain line of code.
#
# Code copied from:
# https://stackoverflow.com/questions/229009/how-can-i-get-a-call-stack-listing-in-perl
#
# Input variables:
#    - Nothing
#
# Returns:
#    - Output to STDERR if called sub routines.
#
# -----------------------------------------------------------------------------
sub dump_call_stack {
    my $i = 1;  # Where to start printout, 0=Include also the current subroutine
                # in the call stack.

    print STDERR "Stack Trace:\n";
    while ( (my @call_details = (caller($i++))) ){
        print STDERR abs_path($call_details[1]).":".$call_details[2]." in function ".$call_details[3]."\n";
    }
}

# -----------------------------------------------------------------------------
# Return the epoch time (number of seconds since 1970-01-01 00:00:00 UTC) for a
# specific date string.
#
# Input variables:
#    - Optional: Date and or time to convert into epoch time.
#      Any format that is supported by the 'date' Linux comand is allowed.
#    If not provided then current time is used.
#
# Returns:
#    - An integer value indicating the epoch time (number of seconds since
#      1970-01-01 00:00:00 UTC.
#      Or undefined value at failure.
#
# -----------------------------------------------------------------------------
sub epoch_time {
    my $date_string = shift;
    my $command;
    my $rc;
    my @result;

    if (defined $date_string) {
        $command = "date -d '$date_string' +\%s";
    } else {
        $command = "date +\%s";
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );
    if ($rc == 0 && $result[0] =~ /^\d+$/) {
        return $result[0];
    } else {
        return undef;
    }
}

# -----------------------------------------------------------------------------
# Return a bit mask for the file handles passed into the subroutine.
#
# Input variables:
#    - Array with file handles
#
# Return values:
#    A bit mask for the file handles.
#
# -----------------------------------------------------------------------------
sub fhbits {
    my @fhlist = @_;
    my $bits = "";

    for my $fh (@fhlist) {
        vec($bits, fileno($fh), 1) = 1;
    }

    return $bits;
}

# -----------------------------------------------------------------------------
# Format the input epoch time value according to ISO format.
#
# Input variables:
#    - Time value is epoch format (number of seconds since Jan 1 00:00:00 1970)
#
# Return values:
#    - Formatted time string. For example: 2019-03-28 19:00:28
#
# -----------------------------------------------------------------------------
sub format_time_iso {
    my $time = shift;

    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime($time);
    return sprintf "%04d-%02d-%02d %02d:%02d:%02d", ($year+1900), ($mon+1), $mday, $hour, $min, $sec;
}

# -----------------------------------------------------------------------------
# Format the input value in number of seconds into a string indicating how many
# days, hours, minutes or seconds are left.
#
# Input variables:
#    - Number of seconds that remains.
#
# Return values:
#    - Formatted time string. For example: 14 minutes, 40 seconds left
#
# -----------------------------------------------------------------------------
sub format_time_left {
    my $time = shift;   # In seconds

    my $days = 0;
    my $hours = 0;
    my $minutes = 0;
    my $seconds =0;
    my $timestr = "";
    my $weeks = 0;

    $weeks = sprintf "%d", ($time / 604800);
    $time = ($time - ($weeks * 604800));
    $days = sprintf "%d", ($time / 86400);
    $time = ($time - ($days * 86400));
    $hours = sprintf "%d", ($time / 3600);
    $time = ($time - ($hours * 3600));
    $minutes = sprintf "%d", ($time / 60);
    $time = ($time - ($minutes * 60));
    $seconds = $time;

    $timestr .= sprintf "%d week%s, ", $weeks, ($weeks ==  1 ? "" : "s") if ($weeks > 0);
    $timestr .= sprintf "%d day%s, ", $days, ($days ==  1 ? "" : "s") if ($days > 0);
    $timestr .= sprintf "%d hour%s, ", $hours, ($hours ==  1 ? "" : "s") if ($hours > 0);
    $timestr .= sprintf "%d minute%s, ", $minutes, ($minutes ==  1 ? "" : "s") if ($minutes > 0);
    $timestr .= sprintf "%d second%s, ", $seconds, ($seconds ==  1 ? "" : "s") if ($seconds > 0);
    $timestr =~ s/,\s*$/ left/;
    $timestr = "Done" if $timestr eq "";

    return $timestr;
}

# -----------------------------------------------------------------------------
# Format the input epoch time value according to local time format.
#
# Input variables:
#    - Time value is epoch format (number of seconds since Jan 1 00:00:00 1970)
#
# Return values:
#    - Formatted time string. For example: Thu Mar 28 19:00:28 2019
#
# -----------------------------------------------------------------------------
sub format_time_localtime {
    my $time = shift;

    return scalar localtime($time);
}

# -----------------------------------------------------------------------------
# Return the script command for collecting detailed log files from the system.
# This will search for the script command to use for fetching detailed log file
# based on script name preferences (name of scripts as an array reference) and
# search will be done based on a list of search paths (array reference).
#
# The first script found in the specified order: directory -> script
# will be returned to the user.
#
# If no script found then an empty string will be returned.
# If no script name or order is specified then default values will be used.
#
# Input variables:
#    - hash reference %params = Input parameters to the sub routine which can be
#      any of the following keys:
#         "script-commands":
#             An array reference of commands to look for.
#             The first matching command will be used.
#         "search-paths":
#             An array reference of directory paths where the scripts will be
#             searched.
#
# Return values:
#    - String with script command to use for collecting detailed logs.
#      If no script found then an empty string is returned.
#
# -----------------------------------------------------------------------------
sub get_log_collect_script {
    my %params = %{$_[0]};

    # Initialize local variables
    my @script_commands   = exists $params{"script-commands"} ? @{$params{"script-commands"}} : ();
    my @search_paths      = exists $params{"search-paths"} ? @{$params{"search-paths"}} : ();
    my $command_dirname   = "";
    my $command_filename  = "";
    my $command_pre       = "";
    my $command_post      = "";

    # Check if user specified and scripts and search paths
    unless (@script_commands) {
        # User did not specify any commands, so we use the following default values:
        @script_commands = ( "data_collector.sh", "collect_ADP_logs.sh" );
    }
    unless (@search_paths) {
        # User did not specify any search paths, so we use the following default values:
        push @search_paths, abs_path( dirname( __FILE__ ) . "/../../../bin/");
        push @search_paths, "$ENV{HOME}/bin/"
    }

    for my $dir (@search_paths) {
        for my $file (@script_commands) {
            if (-f "$dir/$file") {
                # We found a script command, exit loop now
                $command_filename = $file;
                $command_dirname = $dir;
                last;
            }
        }
        last if ($command_filename ne "");
    }

    return "" if ($command_filename eq "");

    # Check if the found file has the 'execute' flag set
    unless (-e "$command_dirname/$command_filename") {
        # The file is not executable, now try to determine which interpreter to use
        # to execute the script.
        if ($command_filename =~ /^.+\.(pl|perl)$/i) {
            $command_pre = "perl ";
        } elsif ($command_filename =~ /^.+\.(sh|bash)$/i) {
            $command_pre = "bash ";
        } elsif ($command_filename =~ /^.+\.(py|python)$/i) {
            $command_pre = "python3 ";
        } else {
            # We don't know what type if script it is, so we just return it as is
            # which probably causes the command to fail if executed.
        }
    }

    if ($command_filename =~ /data_collector\.sh$/) {
        if (-f "$command_dirname/data_collector_addon_sc.sh") {
            # Add as extra parameter to the command
            $command_post = " -e $command_dirname/data_collector_addon_sc.sh";
        }
    }

    return "$command_pre$command_dirname/$command_filename$command_post";
}

# -----------------------------------------------------------------------------
# Return the path where a directory is mounted on.
# If the directory does not exist it will try find the mount point for the
# nearest path.
#
# Input variables:
#    - Directory path
#
# Return values:
#    - The path where the directory is mounted on, or an empty string if not
#      found or another error has occurred.
#
# -----------------------------------------------------------------------------
sub get_mounted_partition_path {
    my $dir_path = shift;

    my $mounted_path = "$dir_path";
    my $rc = 0;
    my @result = ();

    # Find an existing directory
    while ($mounted_path ne "/") {
        if ( -d "$mounted_path") {
            # We have found an existing directory
            last;
        } else {
            $mounted_path = `dirname "$mounted_path"`;
            $mounted_path =~ s/[\r\n]//g;
        }
    }

    # Find the mounted path with 'df' command
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "df -k \"$mounted_path\"",
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );

    if ($rc == 0) {
        $dir_path = $mounted_path;
        $mounted_path = "";
        foreach (@result) {
            if (/^.+\s+\d+\%\s+(.+)$/) {
                $mounted_path = $1;
                last;
            }
        }
    } else {
        print "Failure to execute 'df -k $mounted_path'\n";
        $mounted_path = "";
    }

    return $mounted_path;
}

# -----------------------------------------------------------------------------
# Return the the partition size, the used space and available space in
# the specified scale.
# When an error is detected the the sizes are returned as 0.
#
# Input variables:
#    - Directory path
#    - scale (K,M,G,T,P,E,Z,Y), the default if not specified is K.
#
# Return values:
#    - An array with size, used and avail information in the specified scale
#      without the scale letter.
#
# -----------------------------------------------------------------------------
sub get_mounted_partition_sizes {
    my $dir_path = shift;
    my $scale    = shift;
    $scale = "K" unless defined $scale;

    my $avail = 0;
    my $rc = 0;
    my @result = ();
    my $size = 0;
    my $used = 0;

    # Find the mounted path with 'df' command
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "df --block-size $scale --output=size,used,avail $dir_path",
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );

    if ($rc == 0) {
        foreach (@result) {
            if (/^\s*(\d+)$scale\s+(\d+)$scale\s+(\d+)$scale\s*$/) {
                $size = $1;
                $used = $2;
                $avail = $3;
                last;
            }
        }
    }

    return ($size, $used, $avail);
}

# -----------------------------------------------------------------------------
# Return a hash which contains the information from uname -o and also the
# information from the os-release or known release information files
#
# Input variables:
#
# Returns:
#    - A hash containing the result from uname -o and for Linux based operating
#      systems the information of VERSION_ID and ID from the os-release file
#      or from known release information files
#
#    - For SLES check either os-release or SuSE-release
#        - Normal os-release format
#        - Simplified version of os-relase
#           e.g.
#               SUSE Linux Enterprise Server 11 (x86_64)
#               VERSION = 11
#               PATCHLEVEL = 1
#
#    - For RHEL check either os-release or redhat-release
#        - Normal os-release format
#        - Single line of information
#           e.g.
#               Red Hat Enterprise Linux Server release 7.5 (Maipo)
#
#    - For Ubuntu check either os-release lsb-release
#        - Normal os-release format
#        - Similar to os-release format
#
# -----------------------------------------------------------------------------
sub get_os_information {
    my %os_info;
    my @os_release = ();
    my $os_version = "";
    my $os_patchlevel = "";
    my $rc;
    my @uname = ();

    # Send uname -o to find the system information if possible
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "uname -o",
            "hide-output"   => 1,
            "return-output" => \@uname,
        }
    );

    if ($rc == 0) {
        foreach (@uname) {
            if (/Linux/ || /Cygwin/) {
                $os_info{"uname"} = lc($_);
            }
        }
    } else {
        print "Not supported OS found\n";
    }

    # For GNU/Linux systems use os-release file to find VERSION_ID and ID
    # variables will be stored without double quotes and in lower case
    # where applicable
    if ($os_info{"uname"} eq "gnu/linux") {
        # Use the generic os-release file if found
        if (-f "/etc/os-release") {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cat /etc/os-release",
                    "hide-output"   => 1,
                    "return-output" => \@os_release,
                }
            );

            foreach (@os_release) {
                if (/^VERSION_ID="?(\d+\.\d+)"?/m) {
                    $os_info{"version_id"} = lc($1);
                } elsif (/^ID="?(\w+)"?/m) {
                    $os_info{"id"} = lc($1);
                }
            }
        # Use SuSE-release if found
        } elsif (-f "/etc/SuSE-release") {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cat /etc/SuSE-release",
                    "hide-output"   => 1,
                    "return-output" => \@os_release,
                }
            );

            foreach (@os_release) {
                if (/^VERSION\s=\s"?(\d+)"?/m) {
                    $os_version = lc($1);
                } elsif (/^PATCHLEVEL\s=\s"?(\d+)"?/m) {
                    $os_patchlevel = lc($1);
                }
                $os_info{"id"} = "sles";
                $os_info{"version_id"} = "$os_version.$os_patchlevel";
            }
        # Use redhat-release if found
        } elsif (-f "/etc/redhat-release") {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cat /etc/redhat-release",
                    "hide-output"   => 1,
                    "return-output" => \@os_release,
                }
            );

            foreach (@os_release) {
                if (/^.*(\d+\.\d+).*/m) {
                    $os_info{"version_id"} = lc($1);
                }
                $os_info{"id"} = "rhel";
            }
        # Use lsb-release if found, Ubuntu still seems to care about this file
        } elsif (-f "/etc/lsb-release") {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "cat /etc/lsb-release",
                    "hide-output"   => 1,
                    "return-output" => \@os_release,
                }
            );

            foreach (@os_release) {
                if (/^DISTRIB_ID="?(\w+)"?/m) {
                    $os_info{"version_id"} = lc($1);
                } elsif (/^DISTRIB_RELEASE="?(\d+\.\d+)"?/m) {
                    $os_info{"id"} = lc($1);
                }
            }
        }
    }

    # Return
    return \%os_info;
}

# -----------------------------------------------------------------------------
# Get the SSH key.
#
# Input variables:
#    - The command line parameters to use for the ssh-keyscan command.
#      For example: -p 830 10.20.30.40
#                   -T 10 -t rsa -p 830 10.20.30.40
#    - How many times it should attempt to fetch the SSH key when it fails.
#      If not specified then no new attempt is done at failure, a value of 1
#      means it will try one extra time to fetch it at error, 2 means try two
#      times at failure etc.
#
# Return values:
#    - An array with the result of the command or an empty array at failure.
#
# -----------------------------------------------------------------------------
sub get_ssh_key {
    my $command_parameters = shift;
    my $repeat_cnt_at_error = shift;
    my $rc;
    my @result = ();
    my @error = ();

    # Check that we received command parameters
    return @result unless $command_parameters;

    unless (defined $repeat_cnt_at_error && $repeat_cnt_at_error =~ /^\d+$/) {
        # A valid repeat count was not given, set it to not repeat
        $repeat_cnt_at_error = 0;
    }

    while ($repeat_cnt_at_error >= 0) {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ssh-keyscan $command_parameters",
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@error,
            }
        );

        if ($rc == 0) {
            # Success
            last;
        } else {
            # Something went wrong, return an empty array.
            @result = ();
            if ($repeat_cnt_at_error > 0) {
                $repeat_cnt_at_error--;
            } else {
                last;
            }
        }
    }

    return @result;
}

# -----------------------------------------------------------------------------
# Wait for user to type something on the keyboard and then return it to the
# caller minus the LF character.
# It can also read input via a named pipe which can be useful when no STDIN
# is available.
#
# Limitations:
# ------------
# The named pipe method has some limitations when used on NFS mounted
# directories as stated on this web page:
#
# https://stackoverflow.com/questions/1038788/cant-write-to-fifo-file-mouted-via-nfs
#
# Where it says:
# -----
# A FIFO is meant to be an inter-process communication mechanism. By trying to
# export the FIFO via NFS, you are asking the kernel to treat the local
# inter-process communication as more of a network communication mechanism.
#
# There are a number of issues associated with that, the most obvious one being
# that the FIFO read implementation inside the kernel expects a buffer in
# userspace to copy the data to. Such a buffer is not directly available in NFS.
# Thus, the Kernel does not support the export of FIFOs over NFS.
#
# You may want to use sockets for network communication instead.
# -----
# So if you want to send data into the pipe, you need to be on the same system
# where the pipe was opened on i.e. if the DAFT job was started on e.g. server
# edeacts0167 and the pipe created on that system then you need to login to that
# server before you can 'echo' stuff into the pipe, if you don't login to that
# same server the the echo will just block and you need to kill it to exit.
#
# Input variables:
#    - OPTIONAL Prompt text to display to the user.
#      If not specified then no prompt will be printed, instead it's up to the
#      caller to print this.
#    - OPTIONAL Hide typed characters (=1) or show typed characters (=0).
#      If not specified then default is (=0) to show typed characters.
#
# Return values:
#    Typed string minus the new line character.
#
# -----------------------------------------------------------------------------
sub get_user_input {
    my $prompt = shift;
       $prompt = "" unless defined $prompt;
    my $hidden = shift;
       $hidden = 0 unless defined $hidden;

    my $have_named_pipe = 0;
    my $input;
    my $named_pipe_path = $temporary_input_filename;
    my $nfound;
    my $rin;
    my $rout;

    if ($input_available == 0) {
        `logger -t $caller_tag "get_user_input: No input available, return undefined value to caller"`;
        return undef;
    }

    # Make sure the STDOUT is not buffered
    if ($console_output_available == 1) {
        `logger -t $caller_tag "get_user_input: Make STDOUT unbuffered"`;
        # Make stdout unbuffered = auto-flush
        # Without this logic the prompt is not visible until after input
        # is read.
        my $old_fh = select(STDOUT);
        $| = 1;
        select($old_fh);

        `logger -t $caller_tag "get_user_input: Prompt='$prompt'"` if $prompt ne "";
        print $prompt if defined $prompt;
    } else {
        `logger -t $caller_tag "get_user_input: Prompt='$prompt'"` if $prompt ne "";
    }

    # Create named pipe FIFO
    if (-p $named_pipe_path) {
        # The named pipe already exist
        $have_named_pipe = 1;
        `logger -t $caller_tag "get_user_input: Named pipe already exist"`;
    } else {
        # The named pipe does not exist, create it
        if (mkfifo($named_pipe_path, 0777)) {
            $have_named_pipe = 1;
            `logger -t $caller_tag "get_user_input: Named pipe '$named_pipe_path' created"`;
        } else {
            print "mkfifo $named_pipe_path failed: $!" if $console_output_available;
            $have_named_pipe = 0;
            `logger -t $caller_tag "get_user_input: Named pipe '$named_pipe_path' could not be created"`;
       }
    }

    # Open named pipe FIFO and set file handle bitmask for FIFO and STDIN
    if ($have_named_pipe) {
        if (open(FIFO, "+<", $named_pipe_path)) {
            if ($console_input_available) {
                # No echo on command line for password entry
                `stty -echo` if $hidden;
                $rin = fhbits(\*STDIN, \*FIFO);
                `logger -t $caller_tag "get_user_input: Named pipe and STDIN opened for input"`;
            } else {
                $rin = fhbits(\*FIFO);
                `logger -t $caller_tag "get_user_input: Named pipe opened for input"`;
            }
        } else {
            print "open on named pipe '$named_pipe_path' failed, $!\n" if $console_output_available;
            unlink $named_pipe_path;
            $have_named_pipe = 0;
            if ($console_input_available) {
                # No echo on command line for password entry
                `stty -echo` if $hidden;
                $rin = fhbits(\*STDIN);
                `logger -t $caller_tag "get_user_input: Named pipe could not be opened, only STDIN opened for input"`;
            } else {
                `logger -t $caller_tag "get_user_input: Named pipe could not be opened and no STDIN available, return undefined value to caller"`;
                return undef;
            }
        }
    } elsif ($console_input_available) {
        # No echo on command line for password entry
        `stty -echo` if $hidden;
        $rin = fhbits(\*STDIN);
        `logger -t $caller_tag "get_user_input: No named pipe, only STDIN opened for input"`;
    } else {
        `logger -t $caller_tag "get_user_input: No named pipe or STDIN available, return undefined value to caller"`;
        return undef;
    }

    # Read input from named pipe FIFO and STDIN
    while (1) {
        $nfound = select($rout=$rin, undef, undef, 0.1);
        if ($nfound) {
            `logger -t $caller_tag "get_user_input: Input waiting on STDIN and/or named pipe"`;
            # input waiting on one or more of those 2 filehandles
            if ($console_input_available) {
                if (vec($rout,fileno(STDIN),1)) {
                    `logger -t $caller_tag "get_user_input: Input waiting on STDIN"`;

                    $input = <STDIN>;

                    # Remove the CR or LF characters
                    $input =~ s/[\r\n]//g;
                    if ($hidden == 0) {
                        # Show typed data
                        `logger -t $caller_tag "get_user_input: Input='$input'"`;
                    } else {
                        # Hide typed data, so also hide it in the logger output.
                        # Replace all characters with a "*" to just show how long the input was.
                        my $temp_input = $input;
                        $temp_input =~ s/./*/g;
                        `logger -t $caller_tag "get_user_input: Input='$temp_input'"`;
                    }
                    last;
                }
            }

            if ($have_named_pipe) {
                if (vec($rout,fileno(FIFO),1)) {
                    `logger -t $caller_tag "get_user_input: Input waiting on named pipe"`;

                    $input = <FIFO>;

                    # Remove the CR or LF characters
                    $input =~ s/[\r\n]//g;
                    # Print to STDOUT
                    if ($hidden == 0) {
                        # Show typed data
                        print "$input\n" if $console_output_available;
                        `logger -t $caller_tag "get_user_input: Input='$input'"`;
                    } else {
                        # Hide typed data, so also hide it in the logger output.
                        # Replace all characters with a "*" to just show how long the input was.
                        my $temp_input = $input;
                        $temp_input =~ s/./*/g;
                        print "$temp_input\n" if $console_output_available;
                        `logger -t $caller_tag "get_user_input: Input='$temp_input'"`;
                    }
                    last;
                }
            }
        }
    }

    # Close named pipe FIFo
    if ($have_named_pipe) {
        close(FIFO);
        # Delete the named pipe
        unlink $named_pipe_path;
    }

    # Enable echo to screen again after password entry
    `stty echo` if $hidden;

    return $input;
}

# -----------------------------------------------------------------------------
# Wait for user to press a key on the keyboard and then return it to the caller.
#
# Input variables:
#    -
#
# Return values:
#    Keyboard character pressed.
#
# -----------------------------------------------------------------------------
sub get_user_keypress {
    my $key;

    if ($input_available == 0) {
        `logger -t $caller_tag "get_user_keypress: No input available, return undefined value to caller"`;
        return undef;
    } elsif ($console_input_available == 0) {
        `logger -t $caller_tag "get_user_keypress: No STDIN available, return undefined value to caller"`;
        return undef;
    }

    # Activate unbuffered input from the standard I/O library (BSD style)
    system "stty cbreak </dev/tty >/dev/tty 2>&1";

    $key=getc;

    # Restore buffered input from the standard I/O library (BSD style)
    system "stty -cbreak </dev/tty >/dev/tty 2>&1";

    return $key;
}

# -----------------------------------------------------------------------------
# Get a return code from the user.
# This function should be seen as a debug feature and probably serve no purpose
# in production code.
#
# Input variables:
#    -
#
# Return values:
#    Return code value given by user (0-255, or negative numbers)
#
# -----------------------------------------------------------------------------
sub get_user_rc {
    my $answer;
    $answer = get_user_validated_input("\nEnter a return code (rc) value (0-255, or negative): ", '-?\d+$', 0, 0);
    if (defined $answer) {
        return $answer;
    } else {
        return "-1";
    }
}

# -----------------------------------------------------------------------------
# Wait for user to type something on the keyboard and then return it to the
# caller minus the LF character.
# A validation pattern is provided and the function only returns when valid
# input is given.
#
# Input variables:
#    - Prompt to display to the user.
#    - Valid patterns to check input against.
#    - Indication if check should be case sensitive (=1) or not (=0).
#      The default is to be case sensitive (=1).
#    - Indication if the typed characters should be hidden (=1) or not (=0) to
#      the user. Hiding the typed characters might be useful for password entry.
#      The default is to show typed characters (=0).
#
# Return values:
#    Keyboard character pressed.
#
# -----------------------------------------------------------------------------
sub get_user_validated_input {
    my $prompt = shift;
    my $valid_pattern = shift;
    my $case_sensitive = shift;
    my $hidden = shift;

    $case_sensitive = 1 unless defined $case_sensitive;
    $hidden = 0 unless defined $hidden;

    my $input;

    while (1) {
        if ($console_output_available) {
            print "$prompt";
        } else {
            `logger -t $caller_tag "get_user_validated_input: Input from user required, valid pattern '$valid_pattern'. $prompt"`;
        }

        $input = get_user_input("",$hidden);

        # Special check if whatever is calling this subroutine is running without
        # a terminal in which case no answer can be provided.
        last unless (defined $input);

        # Remove the CR and LF characters
        $input =~ s/[\r\n]//g;

        if ($case_sensitive && $input =~ /$valid_pattern/) {
            # Correct value provided, exit loop
            last;
        } elsif ($case_sensitive == 0 && $input =~ /$valid_pattern/i) {
            # Correct value provided, exit loop
            last;
        } else {
            if ($console_output_available) {
                if ($invalid_entry_with_allowed_values == 0) {
                    if ($hidden == 0) {
                        print "\nInvalid input '$input', please provide a valid value according to prompt.\n\n";
                    } else {
                        print "\nInvalid input, please provide a valid value according to prompt.\n\n";
                    }
                } else {
                    if ($hidden == 0) {
                        print "\nInvalid input '$input', please provide a value matching the pattern '$valid_pattern'.\n\n";
                    } else {
                        print "\nInvalid input, please provide a value matching the pattern '$valid_pattern'.\n\n";
                    }
                }
            } else {
                if ($hidden == 0) {
                    `logger -t $caller_tag "get_user_validated_input: Invalid input '$input', please provide a value matching the pattern '$valid_pattern'"`;
                } else {
                    `logger -t $caller_tag "get_user_validated_input: Invalid input, please provide a value matching the pattern '$valid_pattern'"`;
                }
            }
        }
    }

    return $input;
}

# -----------------------------------------------------------------------------
# Check if a specific year is a leap year (February has 29 days).
# The logic taken from the following web page:
# https://www.perlmonks.org/?node_id=354078
# Which comes from the isleap function in the module Date::Leapyear.
#
# input variables:
#    - Year
#
# return values:
#    - 0: The year is NOT a leap year.
#    - 1: The year is a leap year.
#
# -----------------------------------------------------------------------------
sub is_leap_year {
    my ($year) = @_;

    return 1 if (( $year % 400 ) == 0 ); # 400's are leap
    return 0 if (( $year % 100 ) == 0 ); # Other centuries are not
    return 1 if (( $year % 4 ) == 0 );   # All other 4's are leap
    return 0;                            # Everything else is not
}

# -----------------------------------------------------------------------------
# check if ssh connectivity is available..
#
# input variables:
#    - ip-address or hostname to check. this is a mandatory parameter.
#
#    - port number, if not specified then port 22 will be used.
#
#    - timeout, how long to wait for an answer, if not specified then 20 seconds
#      will be used.
#
#    - attempts, how many times to attempt to fetch the SSH keys after failure.
#      0=Only attempt 1 time, 1=Attempt 2 times, 2=Attempt 3 times, etc.
#      If not specified then value 2 (try 3 times) will be used.
#
# return values:
#    - 1: ssh connectivity is available.
#    - 0: ssh connectivity is not working.
#
# -----------------------------------------------------------------------------
sub is_ssh_available {
    my $host = shift;
    my $port = shift;
    my $timeout = shift;
    my $attempts_at_failure = shift;

    my $rc;
    my @result = ();

    # check that we received command parameters
    return @result unless $host;
    $port = 22 unless $port;
    unless (defined $timeout) {
        $timeout = 20;
    }
    unless (defined $attempts_at_failure) {
        $attempts_at_failure = 2;
    }

    @result = get_ssh_key("-p $port -T $timeout $host", $attempts_at_failure);

    if (@result) {
        return 1;
    } else {
        return 0;
    }
}

# -----------------------------------------------------------------------------
# Return back process information as an array.
#
# Input variables:
#    - The process id of the process to check.
#
# Return values:
#    - An array of process information.
#
# -----------------------------------------------------------------------------
sub process_information {
    my $pid = shift;
    my $rc;
    my @result;

    # Check that we received a command
    return ("No PID specified") unless $pid;
    return ("Invalid PID '$pid' specified") unless ($pid =~ /^\d+$/);

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "ps -f --pid=$pid",
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );

    if ($rc == 0) {
        return @result;
    } else {
        return ("No such PID '$pid' or failure to fetch PID information");
    }
}

# -----------------------------------------------------------------------------
# Check if the specified process is still running.
#
# Input variables:
#    - The process id of the process to check.
#
# Return values:
#    - 0: If the process did not exist or is no longer running.
#    - 1: If the process is still running.
#
# -----------------------------------------------------------------------------
sub process_is_running {
    my $pid = shift;
    my $rc;
    my @result;

    # Check that we received a command
    return 0 unless $pid;
    return 0 unless ($pid =~ /^\d+$/);

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "ps -f --pid=$pid",
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );

    if ($rc == 0) {
        # Process id exist, check that the oricess is not <defunct>
        for (@result) {
            if (/<defunct>/) {
                # The process is more or less dead
                return 0;
            }
        }
        # The process appear to still be running
        return 1;
    } else {
        # Process id is not running
        return 0;
    }
}

# -----------------------------------------------------------------------------
# Kill a specified process.
# The provided process id will be killed if it is executing.
#
# Input variables:
#    - The process id of the process to kill.
#    - Maximum time in seconds to wait for the process to die, default is 10
#      seconds.
#
# Return values:
#    - 0: If the process did not exist or could not be killed, or other failure.
#    - 1: If the process was killed.
#
# -----------------------------------------------------------------------------
sub process_kill {
    my $pid = shift;
    my $time_to_wait = @_ ? shift : 10;

    my $kid;
    my $rc;
    my $stop_time;

    # Check that we received a command
    return 0 unless $pid;
    return 0 unless ($pid =~ /^\d+$/);
    return 0 unless ($time_to_wait =~ /^\d+$/);

    $stop_time = time() + $time_to_wait;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "ps -f --pid=$pid",
            "hide-output"   => 1,
        }
    );

    if ($rc == 0) {
        # Process id exist, try to kill it
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "kill -9 $pid",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            while (1) {
                $kid = waitpid(-1,WNOHANG);
                if ($kid == $pid) {
                    # Children has been terminated
                    last;
                } elsif ($kid == -1) {
                    # There are no children
                    last;
                } else {
                    if (time() >= $stop_time) {
                        # It has taken longer than expected for the process to terminate, exit now
                        $rc = 1;
                        last;
                    }
                    # Wait a second before trying again
                    $rc = General::OS_Operations::sleep_with_progress(
                        {
                            "allow-interrupt"   => 1,
                            "progress-message"  => 0,
                            "seconds"           => 1,
                            "use-logging"       => 1,
                        }
                    );
                    if ($rc == 1) {
                        # CTRL-C pressed
                        $rc = 1;
                        last;
                    }
                }
            }
        }
    }

    if ($rc == 0) {
        return 1;
    } else {
        return 0;
    }
}

# -----------------------------------------------------------------------------
# Return an array with all children and sub children for a specified process.
#
# Input variables:
#    - The process id of the process to find all children for.
#
# Return values:
#    - An array of the children processes.
#    - An empty array if there are no children, or at other errors.
#
# -----------------------------------------------------------------------------
sub process_return_all_children {
    my $pid = shift;

    my @children = ();

    # Check that we received a command
    return @children unless $pid;
    return @children unless ($pid =~ /^\d+$/);

    for my $child_pid (process_return_children($pid)) {
        push @children, $child_pid;
        my @grand_children = process_return_children($child_pid);
        push @children, @grand_children if (@grand_children);
    }

    return @children;
}

# -----------------------------------------------------------------------------
# Return an array with children for a specified process.
#
# Input variables:
#    - The process id of the process to find all children for.
#
# Return values:
#    - An array of the children processes.
#    - An empty array if there are no children, or at other errors.
#
# -----------------------------------------------------------------------------
sub process_return_children {
    my $pid = shift;

    my @children = ();
    my $rc;

    # Check that we received a command
    return @children unless $pid;
    return @children unless ($pid =~ /^\d+$/);

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "pgrep -P $pid",
            "hide-output"   => 1,
            "return-output" => \@children,
        }
    );

    if ($rc == 0) {
        # The process has children
        return @children;
    } else {
        # The process has no children or the parent process does not exists
        return ();
    }
}

# -----------------------------------------------------------------------------
# Send the specified signal to a specified process.
# What happens in the process depends on how it handles the signal,
# if the process intercepts the signal then it can e.g. properly close down
# files etc, otherwise the process will probably just be interrupted and
# terminated.
#
# Input variables:
#    - The process id of the process to send the signal to.
#    - The signal to send e.g. SIGABRT.
#
# Return values:
#    - 0: If the process did not exist or could not be sent the signal.
#    - 1: If the process was sent the signal.
#
# -----------------------------------------------------------------------------
sub process_send_signal {
    my $pid = shift;
    my $signal = shift;
    my $rc;

    # Check that we received a command
    return 0 unless $pid;
    return 0 unless ($pid =~ /^\d+$/);
    return 0 unless $signal;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "ps -f --pid=$pid",
            "hide-output"   => 1,
        }
    );

    if ($rc == 0) {
        # Process id exist, try to send a signal to it
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "kill -s $signal $pid",
                "hide-output"   => 1,
            }
        );
    }

    if ($rc == 0) {
        return 1;
    } else {
        return 0;
    }
}

# -----------------------------------------------------------------------------
# Return IPv4 and IPv6 addresses from a string which might contain one or more
# IP addresses (either IPv4 or IPv6) separated by a specific character (',' is
# the default separator.
# The the different IP addresses are returned in two arrays, one for IPv4 and
# the other for IPv6, if no addresses of a specific type is found then an empty
# array is returned.
#
# Input variables:
#    - The scalar string containing one or more IP-addresses, separated by a
#      character.
#    - The separator character or regular expression, that is used for
#      separating one IP-address from the next. If not specified then a comma
#      is expected.
#
# Return values:
#    - Array reference with IPV4 addresses, or an empty array reference.
#    - Array reference with IPV6 addresses, or an empty array reference.
#
# -----------------------------------------------------------------------------
sub return_ip_addresses {
    my $ip_address_str = shift;
    my $separator = shift;
    my @ip_addresses;
    my @ipv4_addresses = ();
    my @ipv6_addresses = ();

    $separator = "," unless $separator;

    @ip_addresses = split /$separator/, $ip_address_str;

    for my $ip_address (@ip_addresses) {
        if ($ip_address =~ /^\d+\.\d+\.\d+\.\d+$/) {
            push @ipv4_addresses, $ip_address;
            #print "DBG:IPv4=$ip_address\n";
        } elsif ($ip_address =~ /:/) {
            push @ipv6_addresses, $ip_address;
            #print "DBG:IPv6=$ip_address\n";
        } else {
            # Unknown address type, ignore it
            #print "DBG:Unknown format: $ip_address\n";
        }
    }

    return (\@ipv4_addresses, \@ipv6_addresses);
}

# -----------------------------------------------------------------------------
# Calculates the number of seconds between 2 dates.
#
# Input variables:
#    - The from date in any format supported by the 'date' command.
#    - The to date in any format supported by the 'date' command.
#
# Return values:
#    - Number of seconds between the dates.
#      It will be returning a positive value if the 'to' value is later than the
#      'from' value, and return a negative value otherwise, if the dates are the
#      same then a value of 0 (zero) is returned.
#
# -----------------------------------------------------------------------------
sub seconds_between_dates {
    my $from = shift;
    my $to = shift;

    $from = epoch_time($from);
    $to = epoch_time($to);

    return ($to - $from);
}

# -----------------------------------------------------------------------------
# Send a command to the Operating System with the option to hide the output to
# the user and to log output to a detailed log file. It can also return the
# collected output to the user.
# NOTE: STDERR printouts are currently redirected to STDOUT when the command
# is executed using ' 2>&1'.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "command":
#           Command to send to the OS as a scalar string value.
#       "commands":
#           An array reference of commands to send to the OS as a scalar string
#           value.
#       "command-in-output":
#           Include the executed command in returned output when value =1 or
#           don't include it when value =0 or when not specified.
#           This parameter is probably only useful when you execute multiple
#           commands and want to look for the start of a command in the output.
#           The default is to not include the executed command in the returned
#           output.
#       "discard-stderr":
#           If specified then STDERR output is redirected to /dev/null and thus
#           discarded.
#           The default is to always redirect STDERR to STDOUT.
#       "hide-command":
#           Hide the command from the user when value =1 and don't hide it when
#           value =0 or when not specified.
#           The default is to always show the executed command to the user.
#       "hide-output":
#           Hide the output from the command from the user when value =1 and
#           don't hide output when value =0 or when not specified.
#           The default is to always show the executed command and it's result
#           to the user.
#       "ignore-errors":
#           Ignore errors reported by the command or commands and always return
#           successful return code to the caller when value =1 or return back
#           failure code to the caller if a command failed when value =0 or
#           when not specified.
#           The default is to always return back an error if a command failed.
#       "no-command-timestamp":
#           Do not show a timestamp to the user when a command is executed when
#           value =1 or show it when value =0 or not specified.
#           The default is to always show the timestamp to the user when a
#           command is executed.
#       "raw-output":
#           If specified then what is returned is exactly what is received from
#           the node including CR LF.
#           By default CR and LF are removed.
#       "return-output":
#           Array reference of variable where the result of the command is
#           returned to the caller.
#           If multiple commands are sent then the returned result will be the
#           output of all commands.
#       "return-stderr":
#           Array reference of variable where the output on STDERR of the
#           command is returned to the caller.
#           If multiple commands are sent then the returned output will be the
#           output of all commands.
#           If this is not specified and "discard-stderr" is also not specified
#           then STDERR is redirected to STDOUT and passed back to the user as
#           normal output from the command.
#       "save-to-file":
#           If specified then all output from executed command or commands will
#           also be written to the specified file.
#           This can also be used together with "return-output" if also a copy
#           of the data should be written to a specific file.
#
#           The command output sent to this file will be overwritten unless the
#           "commands" parameter is specified in which case the output will be
#           appended to the file.
#       "stop-on-error":
#           If specified and multiple commands are specified with key "commands"
#           and an error occurs then execution of remaining commands after the
#           failed command will not be done.
#           The default is to always execute all commands even if there are
#           failures, but the final return code will be the return code of
#           the last failed command.
#       "use-bash-file":
#           If specified and set to value =1 then when sending the command an
#           intermediary bash file is used for sending the command which might be
#           helpful if there are redirections of STDOUT and STDERR so it does not
#           interfer with the command execution.
#
# Example:
# my @result = ();
# my $rc = General::OS_Operations::send_command(
#     {
#         "command"       => "ls -lR ~/log/2017",
#         "hide-output"   => 1,
#         "return-output" => \@result,
#     }
# );
#
# my @result = ();
# my $rc = General::OS_Operations::send_command(
#     {
#         "commands"            => [
#             "netstat -r",
#             "ls -l /",
#             "df -hT /tmp",
#         ],
#         "hide-output"         => 1,
#         "command-in-output"   => 1,
#         "return-output"       => \@result,
#     }
# );
#
#
# Return values:
#    Return code 0 = The call was successful.
#                1 = The call failed for some reason.
#                x = Any other value returned by the sent command.
#
# -----------------------------------------------------------------------------
sub send_command {
    my %params = %{$_[0]};

    # Initialize local variables
    my $command           = exists $params{"command"} ? $params{"command"} : undef;
    my @commands          = exists $params{"commands"} ? @{$params{"commands"}} : ();
    my $command_in_output = exists $params{"command-in-output"} ? $params{"command-in-output"} : 0;
    my @command_result    = ();
    my $current_time      = scalar localtime;
    my $discard_stderr    = exists $params{"discard-stderr"} ? $params{"discard-stderr"} : 0;
    my $epochseconds;
    my $error_msg         = "";
    my $hide_command      = exists $params{"hide-command"} ? $params{"hide-command"} : 0;
    my $hide_output       = exists $params{"hide-output"} ? $params{"hide-output"} : 0;
    my $ignore_errors     = exists $params{"ignore-errors"} ? $params{"ignore-errors"} : 0;
    my $microseconds;
    my $no_cmd_timestamp  = exists $params{"no-command-timestamp"} ? $params{"no-command-timestamp"} : 0;
    my $rc                = 0;
    my $rc_failure        = 0;
    my @result            = ();
    my $raw_output        = exists $params{"raw-output"} ? $params{"raw-output"} : 0;
    my $return_output     = exists $params{"return-output"} ? 1 : 0;
    my $return_stderr     = exists $params{"return-stderr"} ? 1 : 0;
    my @stderr            = ();
    my $save_to_file      = exists $params{"save-to-file"} ? $params{"save-to-file"} : "";
    my $savetofile_append = @commands ? 1 : 0;
    my $stderr_output     = "";
    my $stop_on_error     = exists $params{"stop-on-error"} ? $params{"stop-on-error"} : 0;
    my $use_bash_file     = exists $params{"use-bash-file"} ? $params{"use-bash-file"} : 0;

    # Check that we have a command to send
    if (defined $command) {
        # One command specified
        if (@commands) {
            print "You have to specify either 'command' or 'commands', not both\n";
            return 1;
        } else {
            push @commands, $command;
        }
    } elsif (@commands) {
        # Multiple commands specified
    } else {
        # No commands specified
        print "You have to specify a command to send, with 'command' or 'commands'\n";
        return 1;
    }

    # Mark that we are currently executing a command in case we get interrupted by
    # a SIGTERM.
    $command_execution_in_progress = 1;

    for (my $i = 0; $i <= $#commands; $i++) {
        # Get command to execute
        $command = $commands[$i];
        $last_command = $command;
        $last_command_count++;

        # Write header code to log file if active
        if (General::Logging::log_status() == 1) {

            # Get time value to use for the log entry
            if ($time_hires_available) {
                ($epochseconds, $microseconds) = Time::HiRes::gettimeofday();
            } else {
                $epochseconds = time();
                $microseconds = 0;
            }
            $last_command_seq_id = "$$.$last_command_count.$epochseconds.$microseconds";

            if ($commented_result) {
                # General::Logging::log_write("# Command:\n# $command\n$hashline\n", $epochseconds, $microseconds);
                General::Logging::log_write("# Command No: $last_command_seq_id\n# Command:\n# $command\n$hashline\n", $epochseconds, $microseconds);
            } else {
                # General::Logging::log_write("# Command:\n$command\n$hashline\n", $epochseconds, $microseconds);
                General::Logging::log_write("# Command No: $last_command_seq_id\n# Command:\n$command\n$hashline\n", $epochseconds, $microseconds);
            }
        }

        if ($use_bash_file == 1) {
            # We use a temporary bash file so we don't mess up the command result
            # because of the redirection of STDOUT and using tee.
            $rc = General::File_Operations::write_file(
                {
                    "filename"            => "$temporary_command_filename",
                    "output-ref"          => [ "$command" ],
                    "eol-char"            => "\n",
                    "file-access-mode"    => 777,
                }
            );
            if ($rc != 0) {
                # No commands specified
                print "Failed to write the temporary command file: $temporary_command_filename\n";
                return 1;
            }
        }

        # Echo the command to the temporary log file
        $rc = General::File_Operations::write_file(
            {
                "filename"            => "$temporary_output_filename",
                "output-ref"          => [ "$command_prompt$command" ],
                "eol-char"            => "\n",
            }
        );
        # And to retuned output if wanted
        push @result, "$command_prompt$command\n" if $command_in_output;

        # Check if STDERR needs special handling
        if ($return_stderr) {
            $stderr_output = "2>$temporary_stderr_filename";
        } elsif ($discard_stderr) {
            # Redirect it to a file even though the user does not care about STDERR
            # just so we can print it in the results output in the log file.
            $stderr_output = "2>$temporary_stderr_filename";
        } else {
            $stderr_output = "2>&1";
        }

        # Empty the result of the current executing command
        @command_result = ();

        #
        # Execute the command and fetch the result
        # -------------------
        #
        if ($hide_output) {
            # Send command to the OS, returning printed data to the @result array and also
            # to the temporary file.
            # Special workaround needed to get the exit code from the command by setting
            # 'set -o pipefail' otherwise the 'tail' will normally always return back
            # exit code 0 regardless if the command failed.
            # The 'stdbuf -oL -eL' command turns on buffering on lines for STDOUT and STDERR.
            if ($use_bash_file == 1) {
                push @command_result, `set -o pipefail; stdbuf -oL -eL $temporary_command_filename $stderr_output | tee -a $temporary_output_filename`;
            } else {
                push @command_result, `set -o pipefail; stdbuf -oL -eL $command $stderr_output | tee -a $temporary_output_filename`;
            }

            $rc = $?;
        } else {
            if ($no_cmd_timestamp) {
                print "\n";
            } else {
                print "\n\n$hashline\n# Date: $current_time\n$hashline\n\n";
            }

            # Send command to the OS, storing printed data in a temporary file
            # Special workaround needed to get the exit code from the command by setting
            # 'set -o pipefail' otherwise the 'tail' will normally always return back
            # exit code 0 regardless if the command failed.
            # The 'stdbuf -oL -eL' command turns on buffering on lines for STDOUT and STDERR.
            print "$command_prompt$command\n" unless $hide_command;
            if ($use_bash_file == 1) {
                $rc = system("set -o pipefail; stdbuf -oL -eL $temporary_command_filename $stderr_output | tee -a $temporary_output_filename");
            } else {
                $rc = system("set -o pipefail; stdbuf -oL -eL $command $stderr_output | tee -a $temporary_output_filename");
            }

            if (open (TEMP, $temporary_output_filename)) {
                @command_result = <TEMP>;
                close TEMP;

                # Remove the first line which contain the executed command
                shift @command_result;
            } else {
                push @command_result, "ERROR:Unable to read data from file '$temporary_output_filename'\n";
            }
        }

        # Append command output to overall result array
        push @result, @command_result;

        # Check the Perl return code and convert it to a proper return code
        $last_rc_coredump_created = 0;
        $last_rc_original_value = $rc;
        $last_rc_signal_value = 0;
        if ($rc == -1) {
            $error_msg = "Failed to execute the command: $command";
            $rc = 1;
        } elsif ($rc & 127) {
            $last_rc_signal_value = ($rc & 127);
            $last_rc_coredump_created = ($rc & 128);
            $error_msg = sprintf "Child died with signal %d, %s coredump",
                            ($rc & 127),  ($rc & 128) ? 'with' : 'without';
            $rc = 1;
        } else {
            $rc = $rc >> 8;
        }
        $last_rc = $rc;

        # Check if we should return STDERR to user
        if ($return_stderr || $discard_stderr) {
            General::File_Operations::read_file(
                {
                    "filename"            => "$temporary_stderr_filename",
                    "output-ref"          => \@stderr,
                }
            );
            if ($return_stderr) {
                if ($raw_output) {
                    @{$params{"return-stderr"}} = @stderr;
                } else {
                    for (@stderr) {
                        s/[\r\n]//g;
                    }
                    @{$params{"return-stderr"}} = @stderr;
                }
            }
        }

        # Print result to log file
        if (General::Logging::log_status() == 1) {
            my $print_string = "# Command No: $last_command_seq_id\n# Return code: $rc\n";
            if ($error_msg) {
                if ($commented_result) {
                    $print_string .= "# $error_msg\n";
                } else {
                    $print_string .= "$error_msg\n";
                }
                print "$error_msg\n" unless $hide_output;
            }
            if (($return_stderr || $discard_stderr) && scalar @stderr > 0) {
                $print_string .= "$hashline\n";
                $print_string .= "# Error (STDERR):\n";
                my $result = "";
                if ($commented_result) {
                    $result = join "# ",  @stderr;
                    if ($result =~ /\n$/s) {
                        # The last line contains a LF, print as is
                        $print_string .= "# $result";
                    } else {
                        # The last line is missing a LF, add a LF in printed data
                        $print_string .= "# $result\n";
                    }
                } else {
                    $result = join "",  @stderr;
                    if ($result =~ /\n$/s) {
                        # The last line contains a LF, print as is
                        $print_string .= "$result";
                    } else {
                        # The last line is missing a LF, add a LF in printed data
                        $print_string .= "$result\n";
                    }
                }
            }
            $print_string .= "$hashline\n";
            if ($return_stderr == 0 && $discard_stderr == 0) {
                $print_string .= "# Result (STDOUT and STDERR):\n";
            } else {
                $print_string .= "# Result (STDOUT):\n";
            }
            if (@command_result) {
                my $result = "";
                if ($commented_result) {
                    $result = join "# ",  @command_result;
                    if ($result =~ /\n$/s) {
                        # The last line contains a LF, print as is
                        $print_string .= "# $result";
                    } else {
                        # The last line is missing a LF, add a LF in printed data
                        $print_string .= "# $result\n";
                    }
                } else {
                    $result = join "",  @command_result;
                    if ($result =~ /\n$/s) {
                        # The last line contains a LF, print as is
                        $print_string .= "$result";
                    } else {
                        # The last line is missing a LF, add a LF in printed data
                        $print_string .= "$result\n";
                    }
                }
            }
            $print_string .= "$hashline\n";
            General::Logging::log_write("$print_string");
        }

        if ($save_to_file ne "") {
            # Add back the command to be able to separate multiple printouts
            # from each other if so wanted by the user
            unshift @command_result, "$command_prompt$command" if $command_in_output;

            # We don't care about failure to write to file
            General::File_Operations::write_file(
                {
                    "filename"            => "$save_to_file",
                    "output-ref"          => \@command_result,
                    "eol-char"            => "\n",
                    "append-file"         => $savetofile_append,
                }
            );
        }

        # Keep track of the return code for last failed command
        if ($rc > 0) {
            if ($ignore_errors == 0) {
                $rc_failure = $rc;
                if ($stop_on_error) {
                    # Exit the 'for' loop and don't execute remaining commands
                    last;
                }
            }
        }
    }

    # Mark that we have finished executing the command
    $command_execution_in_progress = 0;

    # Copy the result to user variable if wanted
    if ($return_output) {
        if ($raw_output) {
            @{$params{"return-output"}} = @result;
        } else {
            my @temp = @result;
            for (@temp) {
                s/[\r\n]//g;
            }
            @{$params{"return-output"}} = @temp;
        }
    }

    # If we used a temporary command file then remove it
    if ($use_bash_file == 1) {
        unlink $temporary_command_filename;
    }

    # Return return code to caller
    return $rc_failure;
}

# -----------------------------------------------------------------------------
# Send a command to a remote node using ssh (via send_command_to_ssh.exp script).
# The commands will be sent by one send_command_to_ssh.exp command and it calls
# the send_command sub routine for executing the command.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys in addition to all parameters supported by the
#    send_command sub routine:
#       "commands":
#           An array reference of commands to send to the remote node.
#       "expect-debug":
#           If set to 1, 'yes' or 'true' then expect debug printouts will be
#           enabled. NOTE: enabling this will cause a very messy printout
#           unless STDERR is also redirected to a file.
#           By default no debug printouts are printed.
#       "ip":
#           The remote IP-address or hostname to send the command(s) to.
#           If not specified then the commands are sent to localhost by starting
#           a new bash shell.
#       "password":
#           If specified then this password is used for any password prompt
#           from the node.
#           If not specified then the password 'noneeded' will be used.
#       "port":
#           If specified then this is the port to use when connecting to the
#           node.
#           If not specified then the default port (usually 22) is used.
#       "stop-on-error":
#           If specified and multiple commands are specified with key "commands"
#           and an error occurs then execution of remaining commands after the
#           failed command will not be done.
#           The default is to always execute all commands even if there are
#           failures, but the final return code will be the return code of
#           the last failed command.
#       "timeout":
#           The timeout to use for login and prompt detection between sent
#           commands.
#           If not specified then no timeout will be used i.e. the same as
#           using the value -1.
#       "use-standard-ssh":
#           If specified and =1 or =yes then the standard ssh program will be
#           used instead of the default 'send_command_to_ssh.exp' script.
#           Any other value than =1 or =yes will be treated as =no.
#           NOTE: That using this parameter makes it not possible to specify
#           a password, i.e. it must be possible to send the command via ssh
#           without prompting for any password.
#       "user":
#           If specified then this user will be used for the login to the
#           remote node.
#           If not specified then the current executing user will be used i.e.
#           $USER environment variable.
#
# my @result = ();
# my $rc = General::OS_Operations::send_command(
#     {
#         "commands"            => [
#             "netstat -r",
#             "ls -l /",
#             "df -hT /tmp",
#         ],
#         "hide-output"         => 1,
#         "command-in-output"   => 1,
#         "return-output"       => \@result,
#         "ip"                  => "10.10.10.10",
#         "password"            => "notneeded",
#         "user"                => "eccd",
#         "timeout"             => 20,
#     }
# );
#
#
# Return values:
#    Return code 0 = The call was successful.
#                1 = The call failed for some reason.
#                x = Any other value returned by the sent command.
#
# -----------------------------------------------------------------------------
sub send_command_via_ssh {
    my %params = %{$_[0]};

    # Initialize local variables
    my $commands          = exists $params{"commands"} ? $params{"commands"} : undef;
    my $command           = "";
    my $expect_debug      = exists $params{"expect-debug"} ? $params{"expect-debug"} : undef;
    my $ip                = exists $params{"ip"} ? $params{"ip"} : "localhost";
    my $password          = exists $params{"password"} ? $params{"password"} : "notneeded";
    my $port              = exists $params{"port"} ? $params{"port"} : "";
    my $stop_on_error     = exists $params{"stop-on-error"} ? $params{"stop-on-error"} : 0;
    my $timeout           = exists $params{"timeout"} ? $params{"timeout"} : -1;
    my $use_standard_ssh  = exists $params{"use-standard-ssh"} ? $params{"use-standard-ssh"} : "no";
    my $user              = exists $params{"user"} ? $params{"user"} : $ENV{'USER'};

    # We need to have commands to send
    unless (defined $commands) {
        print "You have to specify at least one command to execute in 'commands'\n";
        return 1;
    }

    if ($expect_debug && $expect_debug =~ /^(1|yes|true)$/i) {
        $expect_debug = 1;
    } else {
        $expect_debug = 0;
    }

    if ($use_standard_ssh =~ /^(1|yes)$/i) {
        $use_standard_ssh = "yes";
    } else {
        $use_standard_ssh = "no";
    }

    if ($use_standard_ssh eq "yes") {
        if (exists $params{"password"}) {
            print "You CANNOT specify a password with 'password' => ... when also using 'use-standard-ssh' => 'yes'\n";
            return 1;
        }

        my $ssh_command = sprintf "ssh -o ConnectTimeout=%d -o StrictHostKeyChecking=no -o BatchMode=Yes -o UserKnownHostsFile=/dev/null -o loglevel=ERROR%s $user\@$ip", ($timeout == -1 ? 10 : $timeout), ($port ne "" ? " -p $port" : "");
        for (@$commands) {
            if (/'/) {
                # The command already contains single quotes, so don't add any more
                s/^/$ssh_command /;
            } else {
                s/^/$ssh_command '/;
                s/$/'/;
            }
        }
    } elsif (-f "$package_directory/expect/bin/send_command_to_ssh.exp") {
        if ($expect_debug == 0) {
            $command = "$package_directory/expect/bin/send_command_to_ssh.exp";
        } else {
            $command = "expect -d $package_directory/expect/bin/send_command_to_ssh.exp";
        }

        if ($port ne "") {
            $command .= " --port=$port";
        }

        if ($password ne "") {
            $command .= " --password='$password'";
        }

        if ($user ne "") {
            $command .= " --user=$user";
        }

        if ($timeout ne "") {
            $command .= " --timeout=$timeout";
        }

        if ($ip ne "") {
            $command .= " --ip=$ip";
        }

        if ($stop_on_error == 1) {
            $command .= " --stop-on-error";
        }

        for my $command_string (@$commands) {
            $command .= " --command='$command_string'";
        }

        delete $params{"commands"};
        $params{"command"} = $command;
    } else {
        print "Could not find the $package_directory/expect/bin/send_command_to_ssh.exp file\nAnd 'use-standard-ssh' => 'yes' not specified.";
        return 1;
    }

    # Execute the command
    return send_command( \%params );
}

# -----------------------------------------------------------------------------
# Set the temporary input file used for sending input into the script when no
# STDIN is available and the script needs input from the user.
# This temporary file which is created when a scrip calls the get_user_input
# subroutine.
#
# Input variables:
#    - File name to use for the temporary output.
#
# Returns:
#    -
#
# -----------------------------------------------------------------------------
sub set_input_named_pipe_file {
    $temporary_input_filename = shift;
}

# -----------------------------------------------------------------------------
# Set the temporary output file used for the send_command output to collect
# the printed data to file when showing output to the user, so it can be
# returned to the caller if so wanted.
#
# Input variables:
#    - File name to use for the temporary output.
#
# Returns:
#    -
#
# -----------------------------------------------------------------------------
sub set_temporary_output_file {
    $temporary_output_filename = shift;
}

# -----------------------------------------------------------------------------
# Set the temporary stderr file used for the send_command output to collect
# the printed data from STDERR to file, so it can be returned to the caller
# if so wanted.
#
# Input variables:
#    - File name to use for the temporary STDERR output.
#
# Returns:
#    -
#
# -----------------------------------------------------------------------------
sub set_temporary_stderr_file {
    $temporary_stderr_filename = shift;
}

# -----------------------------------------------------------------------------
# Return true (>0) or false (=0) if one of the supported signals has been
# captured. A signal can be e.g. USR1, USR2, INT (when CTRL-C is pressed) or
# QUIT (when CTRL-\ is pressed).
# After calling this sub routine the specified signal counter will also be
# cleared.
# When a signal is being captured and a user press the special key combination
# for the signal then only a counter is stepped instead of e.g. aborting the
# script execution.
#
# Input variables:
#    - name of the signal, currently only the following names are supported:
#       ALL     (All of the supported signals)
#       INT     (CTRL-C pressed)
#       QUIT    (CTRL-\ pressed)
#       USR1    (kill -s SIGUSR1 sent to the execute_playlist.pl PID)
#       USR2    (kill -s SIGUSR2 sent to the execute_playlist.pl PID)
#
# Return values:
#    A counter of how many times the signal has been captured.
#
# -----------------------------------------------------------------------------
sub signal_captured {
    my $signalname = uc(shift);

    my $count = 0;
    if ($signalname eq "ALL") {
        $count += exists $signal_counter{INT}   ? $signal_counter{INT}  : 0;
        $signal_counter{INT} = 0;
        $count += exists $signal_counter{QUIT}  ? $signal_counter{QUIT} : 0;
        $signal_counter{QUIT} = 0;
        $count += exists $signal_counter{USR1}  ? $signal_counter{USR1} : 0;
        $signal_counter{USR1} = 0;
        $count += exists $signal_counter{USR2}  ? $signal_counter{USR2} : 0;
        $signal_counter{USR2} = 0;
    } elsif ($signalname eq "INT") {
        $count += exists $signal_counter{INT}   ? $signal_counter{INT}  : 0;
        $signal_counter{INT} = 0;
    } elsif ($signalname eq "QUIT") {
        $count += exists $signal_counter{QUIT}  ? $signal_counter{QUIT} : 0;
        $signal_counter{QUIT} = 0;
    } elsif ($signalname eq "USR1") {
        $count += exists $signal_counter{USR1}  ? $signal_counter{USR1} : 0;
        $signal_counter{USR1} = 0;
    } elsif ($signalname eq "USR2") {
        $count += exists $signal_counter{USR2}  ? $signal_counter{USR2} : 0;
        $signal_counter{USR2} = 0;
    } else {
        # Not supported signal
        $count = 0;
    }

    return $count;
}

# -----------------------------------------------------------------------------
# Return true (>0) or false (=0) if one of the supported signals has been
# captured. A signal can be e.g. USR1, USR2, INT (when CTRL-C is pressed) or
# QUIT (when CTRL-\ is pressed).
# This call DOES NOT clear the specified signal counter.
# When a signal is being captured and a user press the special key combination
# for the signal then only a counter is stepped instead of e.g. aborting the
# script execution.
#
# Input variables:
#    - name of the signal, currently only the following names are supported:
#       ALL     (All of the supported signals)
#       INT     (CTRL-C pressed)
#       QUIT    (CTRL-\ pressed)
#       USR1    (kill -s SIGUSR1 sent to the execute_playlist.pl PID)
#       USR2    (kill -s SIGUSR2 sent to the execute_playlist.pl PID)
#
# Return values:
#    A counter of how many times the signal has been captured.
#
# -----------------------------------------------------------------------------
sub signal_captured_dont_clear {
    my $signalname = uc(shift);

    my $count = 0;
    if ($signalname eq "ALL") {
        $count += exists $signal_counter{INT}   ? $signal_counter{INT}  : 0;
        $count += exists $signal_counter{QUIT}  ? $signal_counter{QUIT} : 0;
        $count += exists $signal_counter{USR1}  ? $signal_counter{USR1} : 0;
        $count += exists $signal_counter{USR2}  ? $signal_counter{USR2} : 0;
    } elsif ($signalname eq "INT") {
        $count += exists $signal_counter{INT}   ? $signal_counter{INT}  : 0;
    } elsif ($signalname eq "QUIT") {
        $count += exists $signal_counter{QUIT}  ? $signal_counter{QUIT} : 0;
    } elsif ($signalname eq "USR1") {
        $count += exists $signal_counter{USR1}  ? $signal_counter{USR1} : 0;
    } elsif ($signalname eq "USR2") {
        $count += exists $signal_counter{USR2}  ? $signal_counter{USR2} : 0;
    } else {
        # Not supported signal
        $count = 0;
    }

    return $count;
}

# -----------------------------------------------------------------------------
# Disable signal capture for the specified or all supported signals.
# By disabling the signal capture then pressing e.g. CTRL-C will interrupt the
# script execution.
#
# Input variables:
#    - name of the signal, currently only the following names are supported:
#       ALL     (All of the supported signals)
#       INT     (CTRL-C pressed)
#       QUIT    (CTRL-\ pressed)
#       USR1    (kill -s SIGUSR1 sent to the execute_playlist.pl PID)
#       USR2    (kill -s SIGUSR2 sent to the execute_playlist.pl PID)
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub signal_capture_disable {
    my $signalname = uc(shift);

    if ($signalname eq "ALL") {
        $SIG{INT} = 'DEFAULT';
        $signal_counter{INT} = 0;

        $SIG{QUIT} = 'DEFAULT';
        $signal_counter{QUIT} = 0;

        $SIG{USR1} = 'DEFAULT';
        $signal_counter{USR1} = 0;

        $SIG{USR2} = 'DEFAULT';
        $signal_counter{USR2} = 0;
    } elsif ($signalname eq "INT") {
        $SIG{INT} = 'DEFAULT';
        $signal_counter{INT} = 0;
    } elsif ($signalname eq "QUIT") {
        $SIG{QUIT} = 'DEFAULT';
        $signal_counter{QUIT} = 0;
    } elsif ($signalname eq "USR1") {
        $SIG{USR1} = 'DEFAULT';
        $signal_counter{USR1} = 0;
    } elsif ($signalname eq "USR2") {
        $SIG{USR2} = 'DEFAULT';
        $signal_counter{USR2} = 0;
    } else {
        # Not supported signal, do nothing
    }

    return 0;
}

# -----------------------------------------------------------------------------
# Enable signal capture for the specified or all supported signals.
# By enabling the signal capture then pressing e.g. CTRL-C will just increment
# a counter instead of interrupting the script execution.
#
# Input variables:
#    - name of the signal, currently only the following names are supported:
#       ALL     (All of the supported signals)
#       INT     (CTRL-C pressed)
#       QUIT    (CTRL-\ pressed)
#       USR1    (kill -s SIGUSR1 sent to the execute_playlist.pl PID)
#       USR2    (kill -s SIGUSR2 sent to the execute_playlist.pl PID)
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub signal_capture_enable {
    my $signalname = uc(shift);

    if ($signalname eq "ALL") {
        # Install a handler for CTRL-C
        $signal_counter{INT} = 0;
        $SIG{INT} = sub { print "\n"; $signal_counter{INT}++ };

        # Install a handler for CTRL-\ and treat it like CTRL-C
        $signal_counter{QUIT} = 0;
        $SIG{QUIT} = sub { $signal_counter{QUIT}++ };

        # Install a handler for SIGUSR1 which will cause the playlist execution
        # to terminate with a RC_FALLBACK when the current executing
        # task/step has finished.
        $signal_counter{USR1} = 0;
        $SIG{USR1} = sub { $signal_counter{USR1}++ };

        # Install a handler for SIGUSR2 which will cause the playlist execution
        # to terminate with a RC_GRACEFUL_EXIT when the current executing
        # task/step has finished.
        $signal_counter{USR2} = 0;
        $SIG{USR2} = sub { $signal_counter{USR2}++ };
    } elsif ($signalname eq "INT") {
        $signal_counter{INT} = 0;
        $SIG{INT} = sub { print "\n"; $signal_counter{INT}++ };
    } elsif ($signalname eq "QUIT") {
        $signal_counter{QUIT} = 0;
        $SIG{QUIT} = sub { $signal_counter{QUIT}++ };
    } elsif ($signalname eq "USR1") {
        $signal_counter{USR1} = 0;
        $SIG{USR1} = sub { $signal_counter{USR1}++ };
    } elsif ($signalname eq "USR2") {
        $signal_counter{USR2} = 0;
        $SIG{USR2} = sub { $signal_counter{USR2}++ };
    } else {
        # Not supported signal, do nothing
    }

    return 0;
}

# -----------------------------------------------------------------------------
# Halt execution for a specified amount of time and print a status message at
# regular intervals.
#
# Input variables:
#    hash reference %params = Input parameters to the sub routine which can be
#    any of the following keys:
#       "allow-interrupt":
#           Specifies if it will be possible to interrupt the sleep by pressing
#           CTRL-C when the value is =1.
#           If not specified or if value =0 then CTRL-C interruption is not
#           possible and execution will continue until the timer has expired.
#       "confirm-interrupt":
#           Specifies if a confirmation message should be presented to the user
#           after pressing CTRL-C to make sure they want to abort the sleep.
#           If =1 then a confirmation message will be printed asking user if
#           sleep should be aborted and if value is =0 ir not specified then
#           the sleep will be aborted without any message.
#           This only have an effect if also parameter allow-interrupt=1.
#       "hours":
#           Specifies how many hours to wait before resuming execution.
#       "minutes":
#           Specifies how many minutes to wait before resuming execution.
#       "progress-interval":
#           The interval between progress messages in seconds.
#           If not specified then the default values will be dependent on the
#           delay time specified:
#
#           "hours" given then default is:       600 seconds
#           "minutes" given then the default is: 10 seconds
#           "seconds" given then the default is: 1 second
#       "progress-message":
#           Specifies if progress messages should be printed (=1) or not (=0).
#           If no progress messages is wanted then this function will silently
#           sleep without any information printed to the user.
#           The default is to always show progress messages.
#       "seconds":
#           Specifies how many seconds to wait before resuming execution.
#       "use-logging":
#           If called with a value >0 then any messages printed will be using
#           the General::Logging.pm module to print information to the screen.
#           The default if not specified and if value is 0 is to just print
#           normal text to the STDOUT.
#
# Returns:
#    - 0 If sleep run until completion.
#    - 1 If sleep was interrupted with CTRL-C.
#
# -----------------------------------------------------------------------------
sub sleep_with_progress {
    my %params = %{$_[0]};

    # Initialize local variables
    my $allow_interrupt     = exists $params{"allow-interrupt"} ? $params{"allow-interrupt"} : 0;
    my $confirm_interrupt   = exists $params{"confirm-interrupt"} ? $params{"confirm-interrupt"} : 0;
    my $ctrl_c_pressed      = 0;
    my $current_time        = 0;
    my $delay_time          = 0;
    my $end_time            = 0;
    my $hours               = exists $params{"hours"} ? $params{"hours"} : 0;
    my $message             = "";
    my $minutes             = exists $params{"minutes"} ? $params{"minutes"} : 0;
    my $now_str             = "";
    my $progress_interval   = exists $params{"progress-interval"} ? $params{"progress-interval"} : 0;
    my $progress_message    = exists $params{"progress-message"} ? $params{"progress-message"} : 1;
    my $progress_time       = 0;
    my $seconds             = exists $params{"seconds"} ? $params{"seconds"} : 0;
    my $time_left           = "";
    my $use_logging         = exists $params{"use-logging"} ? $params{"use-logging"} : 0;

    if ($allow_interrupt !~ /^\d+$/) {
        # Not a 0 or >1 value, convert it to a valid value
        if ($allow_interrupt =~ /^(yes|y)$/i) {
            $allow_interrupt = 1;
        } elsif ($allow_interrupt =~ /^(no|n)$/i) {
            $allow_interrupt = 0;
        } else {
            $allow_interrupt = 0;
        }
    }

    if ($confirm_interrupt !~ /^\d+$/) {
        # Not a 0 or >1 value, convert it to a valid value
        if ($confirm_interrupt =~ /^(yes|y)$/i) {
            $confirm_interrupt = 1;
        } elsif ($confirm_interrupt =~ /^(no|n)$/i) {
            $confirm_interrupt = 0;
        } else {
            $confirm_interrupt = 0;
        }
    }

    if ($progress_message !~ /^\d+$/) {
        # Not a 0 or >1 value, convert it to a valid value
        if ($progress_message =~ /^(yes|y)$/i) {
            $progress_message = 1;
        } elsif ($progress_message =~ /^(no|n)$/i) {
            $progress_message = 0;
        } else {
            $progress_message = 1;
        }
    }

    if ($use_logging !~ /^\d+$/) {
        # Not a 0 or >1 value, convert it to a valid value
        if ($use_logging =~ /^(yes|y)$/i) {
            $use_logging = 1;
        } elsif ($use_logging =~ /^(no|n)$/i) {
            $use_logging = 0;
        } else {
            $use_logging = 0;
        }
    }

    if ($hours) {
        $delay_time += ($hours*3600);
    }
    if ($minutes) {
        $delay_time += ($minutes*60);
    }
    if ($seconds) {
        $delay_time += $seconds;
    }

    if ($progress_interval == 0) {
        if ($delay_time >= 3600) {
            $progress_interval = 600;
        } elsif ($delay_time >= 60) {
            $progress_interval = 10;
        } else {
            $progress_interval = 1;
        }
    }

    if ($progress_interval >= $delay_time) {
        $progress_interval = $delay_time >> 1;    # Half the delay_time
    }

    if ($delay_time > 0) {
        $current_time = time();
        $end_time = $current_time + $delay_time;
        # Set timestamp for showing a new progress message
        $progress_time = $current_time + $progress_interval;

        # Make stdout unbuffered = auto-flush
        my $old_fh = select(STDOUT);
        $| = 1;

        if ($progress_message) {
            $message = sprintf "Current Time: %s\n", format_time_iso($current_time);
            $message .= sprintf "    End Time: %s\n\n", format_time_iso($end_time);

            if ($use_logging) {
                $message .= "Time left\n";
                $message .= "---------\n";
                $message .= format_time_left($end_time - $current_time);
                General::Logging::log_user_message($message);
            } else {
                $message .= "Current Time          Time left\n";
                $message .= "-------------------   ---------\n";
                $message .= sprintf "%s : %s\n", format_time_iso($current_time), format_time_left($end_time - $current_time);
                print $message;
            }
        }

        # Clean CTRL-C counter in case the user pressed CTRL-C while answering
        # the error prompt
        signal_captured("INT");

        while ($current_time < $end_time) {
            if ($current_time + $progress_interval >= $end_time) {
                $progress_interval = $end_time - $current_time;
                if ($progress_interval < 0) {
                    $progress_interval = 0;
                }
                # Set timestamp for showing a new progress message
                $progress_time = $current_time + $progress_interval;
            }

            # Short sleeps to faster detect received signals
            sleep(1);

            # Check if the user interrupted the execution
            if (signal_captured_dont_clear("USR1") > 0) {
                # We do not clear the SIGUSR1 signal counter because we want the playlist
                # task or step check to still catch this signal and then perform the proper
                # fallback and return code RC_FALLBACK to the caller.
                print "\n";     # Needed to not get a messy printout
                if ($use_logging) {
                    General::Logging::log_user_warning_message("User interrupted the sleep by sending signal SIGUSR1 to process id $$");
                } else {
                    print "User interrupted the sleep by sending signal SIGUSR1 to process id $$\n";
                }
                $ctrl_c_pressed = 1;
                last;
            } elsif (signal_captured_dont_clear("USR2") > 0) {
                # We do not clear the SIGUSR2 signal counter because we want the playlist
                # task or step check to still catch this signal and then perform the proper
                # graceful exit and return code RC_GRACEFUL_EXIT to the caller.
                print "\n";     # Needed to not get a messy printout
                if ($use_logging) {
                    General::Logging::log_user_warning_message("User interrupted the sleep by sending signal SIGUSR2 to process id $$");
                } else {
                    print "User interrupted the sleep by sending signal SIGUSR2 to process id $$\n";
                }
                $ctrl_c_pressed = 1;
                last;
            } elsif (signal_captured_dont_clear("QUIT") > 0) {
                # We do not check if $allow_interrupt is set because with this signal we don't
                # allow the user to continue the sleep loop, it always exits this sub routine.
                # We also do not clear the SIGQUIT signal counter because we want the playlist
                # task or step check to still catch this signal and then return exit code 1
                # to the caller which then either allows the user to select if they want to
                # continue or if no console is available then a fallback will be triggered.
                print "\n";     # Needed to not get a messy printout
                if ($use_logging) {
                    General::Logging::log_user_warning_message("User interrupted the sleep by pressing CTRL-\\ or by sending signal SIGQUIT to process id $$");
                } else {
                    print "User interrupted the sleep by pressing CTRL-\\ or signal SIGQUIT to process if $$\n";
                }
                $ctrl_c_pressed = 1;
                last;
            } elsif (signal_captured("INT") > 0) {
                # With this signal we also check if $allow_interrupt is set in which case we also check
                # if the user should confirm this because $confirm_interrupt is set.
                # Here we clear the SIGINT signal counter so it doesn't later cause failure when the
                # playlist task or step logic would otherwise see this signal counter.
                print "\n";     # Needed to not get a messy printout
                if ($allow_interrupt) {
                    # We allow CTRL-C interruption of the timer
                    if ($use_logging) {
                        General::Logging::log_user_warning_message("User interrupted the sleep by pressing CTRL-C or by sending signal SIGINT to process id $$");
                    } else {
                        print "User interrupted the sleep by pressing CTRL-C or signal SIGINT to process if $$\n";
                    }
                    $ctrl_c_pressed = 1;
                    if ($confirm_interrupt) {
                        my $answer = get_user_validated_input("Are you sure you want to interrupt the countdown (yes|y|no|n) ? ", '^(yes|y|no|n)$', 0, 0);  # Not case sensitive, not hidden
                        if ($answer =~ /^(yes|y)$/i) {
                            last;
                        } else {
                            $ctrl_c_pressed = 0;
                            if ($use_logging) {
                                General::Logging::log_user_message("Resuming countdown");
                            } else {
                                print "Resuming countdown\n";
                            }
                        }
                    } else {
                        last;
                    }
                }
            }

            $current_time = time();
            if ($progress_message) {
                if ($current_time >= $progress_time) {
                    # It's time to show a new progress message
                    if ($use_logging) {
                        $message = format_time_left($end_time - $current_time);
                        General::Logging::log_user_message($message);
                    } else {
                        $message = sprintf "%s : %s\n", format_time_iso($current_time), format_time_left($end_time - $current_time);
                        print $message;
                    }
                    # Set timestamp for showing a new progress message
                    $progress_time = $current_time + $progress_interval;
                }
            }
        }

        # Restore buffering
        select($old_fh);
    }

    return $ctrl_c_pressed;
}

# -----------------------------------------------------------------------------
# Function to call if an ongoing command execution in send_command has been
# interrupted by a SIGTERM and we was to preserve the output in the temporary
# output file to the log file.
#
# Input variables:
#    - Return code message to print
#
# Returns:
#    -
#
# -----------------------------------------------------------------------------
sub write_current_temporary_output_to_log {
    my $rc = shift;

    my @result = ();

    # If no command is currently executing just return
    return 0 unless $command_execution_in_progress;

    if (open (TEMP, $temporary_output_filename)) {
        @result = <TEMP>;
        close TEMP;

        # Remove the first line which contain the executed command
        shift @result;
    } else {
        push @result, "ERROR:Unable to read data from file '$temporary_output_filename'\n";
    }

    # Print result to log file
    if (General::Logging::log_status() == 1) {
        my $print_string = "# Return code: $rc\n";
        $print_string .= "$hashline\n";
        $print_string .= "# Result:\n";
        if (@result) {
            my $result = "";
            if ($commented_result) {
                $result = join "# ",  @result;
                $print_string .= "# $result\n";
            } else {
                $result = join "",  @result;
                $print_string .= "$result\n";
            }
        }
        $print_string .= "$hashline\n";
        $print_string =~ s/\r//g;
        General::Logging::log_write("$print_string");
    }
    return 0;
}

# -----------------------------------------------------------------------------
# Function to call if command execution failed and the result of the last
# executed command should be written to the console and to the progress.log
# file so the user can see the result of the last failed command without having
# to open up the all.log file.
#
# Input variables:
#    -
#
# Returns:
#    -
#
# -----------------------------------------------------------------------------
sub write_last_temporary_output_to_progress {
    my $print_string;
    my @result = ();

    if (open (TEMP, $temporary_output_filename)) {
        @result = <TEMP>;
        close TEMP;

        # Remove the first line which contain the executed command
        shift @result;
    } else {
        push @result, "ERROR:Unable to read data from file '$temporary_output_filename'\n";
    }

    # Print result to console (if existing) and to progress.log
    $print_string = join "",  @result;
    $print_string =~ s/\r//g;                           # Remove CR characters
    $print_string =~ s/^(.+)/                  $1/mg;   # Indent all lines to match up with timestamp
    print $print_string if ($General::OS_Operations::console_output_available == 1);
    General::Logging::log_progress($print_string);

    return 0;
}

# -----------------------------------------------------------------------------
# This function is called when the Perl interpreter is being exited normally
# also as a result of calling the die() function.
# But it's not called if Perl is morphing into another program via exec or
# blown out of the water by a signal, which needs to be caught (if possible)
# via a signal trap.
#
# This function will delete any temporary files created by this module.
#
# Input variables:
#    -
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
END {
    unlink "$temporary_input_filename";
    unlink "$temporary_output_filename";
    unlink "$temporary_stderr_filename";
}

1;
