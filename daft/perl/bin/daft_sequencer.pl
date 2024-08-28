#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.28
#  Date     : 2024-03-04 17:09:13
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

################################
#                              #
# External module declarations #
#                              #
################################

use strict;
use warnings;

use File::Basename qw(dirname basename);
use Cwd qw(abs_path cwd);
use lib dirname(dirname abs_path $0) . '/lib';

use General::File_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;

#########################
#                       #
# Variable declarations #
#                       #
#########################

my $script_name = abs_path $0;
my $script_params = join " ", @ARGV;
my $package_dir = abs_path dirname(dirname(dirname($0)));

my $background_handling = "finish";
my $background_max_wait_time = 30 * 60; # Max time in seconds to wait for the background processes to finish naturally
my %background_process_info;
    # key1: Process id
    # key2: "Command Number"
    #       "Command"
    #       "Log File"
our $color_scheme = "dark";
my @command_list = ();
my $command_log_file;
my $command_name;
my $command_number = 0;
my $current_timestamp = `date +%Y%m%d_%H%M%S`;
$current_timestamp =~ s/[\r\n]//g;
my $dry_run = 0;
my %environment_variables;
my $expand_variables_in_commands = 0;
my $filename = "";
my $hostname;
my $jobname = "";
my $log_directory = "";
my $log_file = "";
my %playlist_variables;
my $program_rc = 0;
my $rc = 0;
my $remove_extra_space = 0;
my $show_progress = 0;
my $show_help = 0;
my $start_time = time();
my @status_messages = ();
my $stop_on_error = 0;
my $stop_time = 0;
my $workspace_dir = "";
my @xterm_options = ();
my $xterm_pid = 0;

#use Data::Dumper;
#$Data::Dumper::Sortkeys = 1;
#print "\nDBG:\%ENV:\n", Dumper(%ENV);

#################################
#                               #
# Parse command line parameters #
#                               #
#################################

use Getopt::Long;          # standard Perl module
GetOptions (
    "b|background-command=s"        => sub {
                                            push @command_list, "BACKGROUND_COMMAND: $_[1]";
                                       },
    "bash-command=s"                => sub {
                                            push @command_list, "BASH_COMMAND: $_[1]";
                                       },
    "background-handling=s"         => sub {
                                            if ($_[1] =~ /^(finish|stop)$/i) {
                                                $background_handling = lc($_[1]);
                                            } else {
                                                print "Error: Incorrect format for --background-handling, only 'finish' or 'stop' allowed.\n";
                                                exit 1;
                                            }
                                       },
    "background-max-wait-time=s"    => sub {
                                            if ($_[1] =~ /^(\d+)$/i) {
                                                $background_max_wait_time = $1;
                                            } elsif ($_[1] =~ /^(\d+)s$/i) {
                                                $background_max_wait_time = $1;
                                            } elsif ($_[1] =~ /^(\d+)m$/i) {
                                                $background_max_wait_time = $1 * 60;
                                            } elsif ($_[1] =~ /^(\d+)h$/i) {
                                                $background_max_wait_time = $1  * 3600;
                                            } else {
                                                print "Error: Incorrect format for --background-max-wait-time, only <integer>, <integer>s, <integer>m or <integer>h values allowed.\n";
                                                exit 1;
                                            }
                                       },
    "color=s"                       => sub {
                                            if ($_[1] =~ /^(bright|cyan|dark|html|no)$/i) {
                                                $color_scheme = lc($_[1]);
                                            } else {
                                                print "Error: Incorrect format for --color, only 'html', 'no', 'dark', 'bright' or 'cyan' allowed\n";
                                                exit 1;
                                            }
                                       },
    "c|command=s"                   => sub {
                                            push @command_list, "$_[1]";
                                       },
    "d|dry-run"                     => \$dry_run,
    "e|environment-variable=s"      => sub {
                                            if ($_[1] =~ /^(\S+?)=(.*)/) {
                                                my $var = $1;
                                                my $value = $2;
                                                $value =~ s/^\s+//;                 # Remove preceeding whitespace
                                                $value =~ s/\s+$//;                 # Remove trailing whitespace
                                                $value =~ s/^['"](.*)['"]$/$1/;     # Remove quotes at beginning and end
                                                $ENV{$var} = $value;
                                                $environment_variables{$var} = $value;
                                            } else {
                                                print "Error: Incorrect format of environment variable: $_[1]\n";
                                                exit 1;
                                            }
                                       },
    "f|file=s"                      => sub {
                                            if (-f $_[1]) {
                                                my $fn = basename $_[1];    # Remove directory path and just keep the file name
                                                $fn =~ s/\.\w+$//;          # Remove any file extension
                                                $filename .= "${fn}_";         # If more than one file then concatenate the names
                                                read_config_file($_[1]);
                                            } else {
                                                print "Error: Unable to open file $_[1] for reading.\n";
                                                exit 1;
                                            }
                                       },
    "h|help"                        => \$show_help,
    "j|jobname=s"                   => \$jobname,
    "l|log-file=s"                  => \$log_file,
    "v|playlist-variable=s"         => sub {
                                            if ($_[1] =~ /^(\S+?)=(.*)/) {
                                                my $var = $1;
                                                my $value = $2;
                                                $value =~ s/^\s+//;                 # Remove preceeding whitespace
                                                $value =~ s/\s+$//;                 # Remove trailing whitespace
                                                $value =~ s/^['"](.*)['"]$/$1/;     # Remove quotes at beginning and end
                                                $playlist_variables{$var} = $value;
                                            } else {
                                                print "Error: Incorrect format of playlist variable: $_[1]\n";
                                                exit 1;
                                            }
                                       },
    "show-command=s"                => sub {
                                            push @command_list, "SHOW_COMMAND: $_[1]";
                                       },
    "x|expand-variables"            => \$expand_variables_in_commands,
    "remove-extra-space"            => \$remove_extra_space,
    "p|show-progress"               => \$show_progress,
    "s|stop-on-error"               => \$stop_on_error,
    "o|xterm-option=s"              => \@xterm_options,
    "w|workspace=s"                 => \$workspace_dir,
);

#print "\nDBG:\@command_list:\n", Dumper(@command_list);
#print "\nDBG:\%ENV:\n", Dumper(%ENV);
#print "\nDBG:\%playlist_variables:\n", Dumper(%playlist_variables);

if ($show_help) {
    usage();
    exit 0;
}

if ($color_scheme eq "no") {
    General::Logging::log_color("disable");
} else {
    General::Logging::log_color("enable", $color_scheme);
}

# Set umask to allow all files created from this process to be created
# with rwx rights for user, group and others i.e. 0777.
# It will bypass the default umask set by the user which is normally 0022
# meaning that write bit 'w' is turned off for group and others.
umask 0;

if ($workspace_dir eq "") {
    if (-d "/home/eccd/workspaces") {
        $workspace_dir = "/home/eccd/workspaces";
    } else {
        $workspace_dir = "/tmp/workspaces";
    }
}

unless (-d $workspace_dir) {
    `mkdir -m 777 -p $workspace_dir`;
    if ($?) {
        print "ERROR: Unable to create main workspace directory '$workspace_dir'\n";
        exit 1;
    }
}
$workspace_dir = abs_path $workspace_dir;

if ($jobname eq "") {
    $filename =~ s/_$//g;
    $jobname = "$filename";
}

$log_directory = sprintf "%s/%s_%s_%02d", $workspace_dir, $jobname, $current_timestamp, rand(100);
`mkdir -m 777 -p "$log_directory"`;
if ($?) {
    print "Error: Unable to create job workspace directory '$log_directory'\n";
    exit 1;
}

##############
#            #
# Main logic #
#            #
##############

# Set hostname
$hostname = exists $ENV{'HOSTNAME'} ? $ENV{'HOSTNAME'} : (split /\n/, `hostname --fqdn 2>/dev/null`)[0];
if (defined $hostname) {
    if ($hostname eq "") {
        $hostname = "UNKNOWN";
    }
} else {
    $hostname = "UNKNOWN";
}

# Check all commands for environment variables
check_commands_for_missing_environment_variables();

if ($dry_run == 1) {
    show_commands_to_be_executed();

    # Remove an empty directory (e.g. when executing with -d / --dry-run)
    `rmdir --ignore-fail-on-non-empty $log_directory`;

    exit 0;
}

# Start logging to file
start_logfile();

# Print startup message
print_startup_message();

# Install special handler for SIGTERM which is called
# when the Perl process is killed
$SIG{TERM} = \&cleanup_at_SIGTERM;

# Install a handler for SIGUSR1, SIGUSR2, CTRL-C and CTRL-\ signals
General::OS_Operations::signal_capture_enable("ALL");

if ($show_progress) {
    open_xterm_showing_command_log();
}

# Loop through all commands
process_commands();

EXIT_PROGRAM:

# Stop logging to file
cleanup_at_end();

# Exit the script
exit $program_rc;

################
#              #
# Sub routines #
#              #
################

# -----------------------------------------------------------------------------
#
sub check_commands_for_missing_environment_variables {
    my %env_vars;
    my @existing_variables = ();
    my @non_existing_variables = ();
    my $heading_printed = 0;

    for my $command (@command_list) {
        if ($command =~ /^\s*(IF_ENVIRONMENT_VAR|IF_NOT_ENVIRONMENT_VAR)\s+([^=]+)=/) {
            $env_vars{$2}++;
        }
        while ($command =~ /\$(\w+)/g) {
            $env_vars{$1}++;
        }
    }

    for my $var (sort keys %env_vars) {
        if (exists $ENV{$var}) {
            push @existing_variables, $var;
        } else {
            push @non_existing_variables, $var;
        }
    }

    if (@non_existing_variables) {
        print "Error: Environment variables used in the commands does not exist\n";
        if (@existing_variables) {
            print "\nThe following environment variables exist:\n";
            print   "------------------------------------------\n";
            for my $var (@existing_variables) {
                print "$var (with value '$ENV{$var}')\n";
            }
        }
        print "\nThe following environment variables does not exist:\n";
        print   "---------------------------------------------------\n";
        for my $var (@non_existing_variables) {
            print "$var\n";
        }

        print "\nPlease make sure that you have either:\n 1) Set the environment variables before calling the $0 script.\n 2) Set the variable with parameter -e or --environment-variable in the $0 script.\n 3) Defined the variable with 'ENVIRONMENT_VARIABLE: <name>=<value> in the used file(s).\n\nExecution terminated!\n";
        exit 1;
    }

    if ($expand_variables_in_commands) {
        for my $command (@command_list) {
            for my $var (keys %env_vars) {
                $command =~ s/\$$var/$ENV{$var}/g;
            }
        }
        # Always remove extra spaces since the output will anyway no longer be aligned after expanding variables
        $remove_extra_space = 1;
    }

    if ($remove_extra_space) {
        for my $command (@command_list) {
            $command =~ s/\s+/ /g;
        }
    }
}

# -----------------------------------------------------------------------------
#
sub cleanup_at_end {
    # Stop any ongoing background tasks
    if (%background_process_info) {
        stop_background_tasks();
    }

    # Record stop time and calculate job duration
    $stop_time = time;
    my $job_duration = $stop_time - $start_time;

    # Stop logging of output to file and write message to user
    my @messages = ();
    my $message = "";

    #
    # Add general information
    @messages = (
        "",
        "Time Statistics",
        "===============",
        "Job started on: " . scalar localtime($start_time),
        "Job ended on:   " . scalar localtime($stop_time),
        "Job Duration:   " . General::OS_Operations::convert_seconds_to_wdhms($job_duration, 0),
        "",
        "Job Logs",
        "========",
        "Job workspace directory:",
        "$log_directory",
        "",
        "Detailed log is written to file:",
        "$log_file",
        "",
    );

    #
    # Add overall Job status
    #
    push @messages, (
        "Job Status",
        "==========",
        "$jobname Job Finished with " . ($program_rc == 0 ? "Success" : "Failure"),
        "",
    );

    if (@status_messages) {
        push @messages, @status_messages;
    }

    # Add an empty line if needed
    if ($messages[-1] ne "") {
        push @messages, "";
    }

    if ($log_file) {
        General::Logging::log_user_message("Removing CR characters from the detailed log file\n");
        `sed -i 's/\\r//g' $log_file`;
    }

    # Draw a box around the messages
    General::Message_Generation::print_message_box(
        {
            "header-text" => "Job Information",
            "max-length"  => 0,
            "messages"    => \@messages,
            "return-output" => \$message,
        }
    );


    if ($program_rc == 0) {
        General::Logging::log_user_message($message);
    } else {
        General::Logging::log_user_error_message($message);
    }

    if ($log_file) {
        General::Logging::log_disable();
    }

    # Remove temporary files
    unlink "$log_directory/command.log";
    unlink "$log_directory/stderr.log";
    unlink "$log_directory/input.pipe";

    # Remove the lock file
    unlink "$log_directory.lock";

    # Remove any temporary files created by the playlists
    General::File_Operations::tempfile_delete_all();

    # Remove an empty directory (e.g. when executing with -d / --dry-run)
    `rmdir --ignore-fail-on-non-empty $log_directory`;

    # Write message to system log
    `logger -t DAFT_SEQ 'PID=$$ Stopped by user $ENV{USER} on host $hostname with exit code: $program_rc' 2> /dev/null`;
}

# -----------------------------------------------------------------------------
#
sub cleanup_at_SIGTERM {
    print "\n";
    General::Logging::log_user_error_message("The execution was interrupted by a SIGTERM");

    # Write output of current executing command, if any, to log file
    General::OS_Operations::write_current_temporary_output_to_log("Interrupted by a SIGTERM");

    # Mark playlist execution as failed
    $program_rc = 143;

    # Stop log file, write summary file and remove .lock file
    cleanup_at_end();

    # Write message to system log
    `logger -t DAFT_SEQ 'PID=$$ Stopped by user $ENV{USER} on host $hostname with exit code: 143 (SIGTERM or process killed)' 2> /dev/null`;

    # Enable echo to screen again in case this was disabled e.g. during password entry
    `stty echo`;

    General::Logging::log_progress("Exit Code=143\n");
    exit 143;
}

# -----------------------------------------------------------------------------
#
sub open_xterm_showing_command_log {
    my $command = "xterm ";
    my $xterm_log_file = "$log_directory/xterm.log";

    if (@xterm_options) {
        for my $option (@xterm_options) {
            $command .= "$option ";
        }
    } else {
        # Set some default options
        $command .= "-geometry 250x20+0+0 -bg white -sl 300 ";
    }
    $command .= "-e 'tail -f $log_directory/command.log'";

    General::File_Operations::write_file(
        {
            "filename"            => "$log_directory/command.log",
            "output-ref"          => ["$command\n"],
            "append-file"         => 1,
        }
    );

    $xterm_pid = General::OS_Operations::background_process_run($command, $xterm_log_file);
    if ($xterm_pid > 0) {
        `logger -t DAFT_SEQ 'PID=$$ Started in Background "$command" with process id $xterm_pid' 2> /dev/null`;
        General::Logging::log_user_message("Background PID: $xterm_pid\nLog file:  $xterm_log_file\n");
        push @status_messages, "(?) Background Command 0 (xterm) started with PID $xterm_pid";
        $rc = 0;
        $background_process_info{$xterm_pid}{"Command Number"} = 0;
        $background_process_info{$xterm_pid}{"Command"} = $command;
        $background_process_info{$xterm_pid}{"Log File"} = $xterm_log_file;
    } else {
        `logger -t DAFT_SEQ 'PID=$$ Failed to start in Background "$command"' 2> /dev/null`;
        General::Logging::log_user_error_message("Background Command Failed.\nLog file:  $command_log_file\n");
        push @status_messages, "(x) Background Command 0 (xterm) failed to start, but error ignored";
    }
}

# -----------------------------------------------------------------------------
#
sub print_startup_message {
    my $message = "";

    General::Logging::log_write("Script started with parameters:\nperl $script_name $script_params\n");

    # Write message to system log
    `logger -t DAFT_SEQ 'PID=$$ Started by user $ENV{USER} on host $hostname' 2> /dev/null`;

    # Print some work related information
    General::Message_Generation::print_message_box(
        {
            "header-text" => "Job Information",
            "max-length"  => 0,
            "messages"    => [
                "Job started on ".scalar localtime($start_time),
                "",
                "               Job Name : $jobname",
                "Job Workspace Directory : $log_directory",
                "            Command Log : $log_directory/command.log",
                "       Detailed Job Log : $log_file",
                "",
                "    Job Started by User : $ENV{'USER'}",
                "    Job Started on Host : $hostname",
                "        Job Proccess Id : $$",
                "",
                "Job Signals:",
                "------------",
                "Interrupt current task and possibly return error:        kill -s SIGINT  $$ (or press CTRL-C)",
                "Interrupt current task and return error:                 kill -s SIGQUIT $$ (or press CTRL-\\)",
                "",
            ],
            "return-output" => \$message,
        }
    );
    General::Logging::log_user_message($message);
}

# -----------------------------------------------------------------------------
#
sub process_commands {
    my $hide_output;
    my $message;
    my @result;
    my $signal_int_captured;
    my $signal_quit_captured;
    my $type;
    my $use_bash_file;
    my $user_interrupted_execution = 0;

    for my $command (@command_list) {
        $command_number++;

        # Remove spaces at the start of the line
        $command =~ s/^\s+//;

        if ($command =~ /^(IF_ENVIRONMENT_VAR\s+([^=]+)="([^"]*)"\s+)(.+)/) {
            my $remove = $1;
            my $var = $2;
            my $regexp = $3;
            $command = $4;
            if (exists $ENV{$var}) {
                if ($ENV{$var} !~ /$regexp/i) {
                    # The environment variable does not match, so don't execute this command
                    push @status_messages, "(-) Command $command_number skipped due to IF_ENVIRONMENT_VAR $var not matching $regexp";
                    next;
                }
                # The environment variable match, so we want to execute this command
            } else {
                # The environment variable does not exist, treat it like the variable did not match, so don't execute this command
                push @status_messages, "(-) Command $command_number skipped due to IF_ENVIRONMENT_VAR $var not existing";
                next;
            }
        } elsif ($command =~ /^(IF_NOT_ENVIRONMENT_VAR\s+([^=]+)="([^"]*)"\s+)(.+)/) {
            my $remove = $1;
            my $var = $2;
            my $regexp = $3;
            $command = $4;
            if (exists $ENV{$var}) {
                if ($ENV{$var} =~ /$regexp/i) {
                    # The environment variable match, so don't execute this command
                    push @status_messages, "(-) Command $command_number skipped due to IF_NOT_ENVIRONMENT_VAR $var matching $regexp";
                    next;
                }
                # The environment variable does not match, so we want to execute this command
            } else {
                # The environment variable does not exist, treat it like the variable match, so execute this command
            }
        }

        if ($command =~ /^(BACKGROUND_COMMAND|BASH_COMMAND|SHOW_COMMAND):\s*(.+)/) {
            $type = $1;
            $command = $2;
        } elsif ($command =~ /^SLEEP_WITH_PROGRESS:\s*(.+)/) {
            my $parameters = $1;
            my %sleep_parameters;
            while ($parameters ne "") {
                if ($parameters =~ /^((\S+)="([^"]*)"\s*)/) {
                    $sleep_parameters{$2} = $3;
                    $parameters =~ s/^$1//;
                } elsif ($parameters =~ /^((\S+)='([^']*)'\s*)/) {
                    $sleep_parameters{$2} = $3;
                    $parameters =~ s/^$1//;
                } elsif ($parameters =~ /^((\S+)=(\S+)\s*)/) {
                    $sleep_parameters{$2} = $3;
                    $parameters =~ s/^$1//;
                } elsif ($parameters =~ /^\$((\S+)\s*)/) {
                    my $name = $1;
                    if (exists $ENV{$name}) {
                        $parameters =~ s/^\$$name\s*/$ENV{$name} /;
                    } else {
                        # Should not happen, but just in case use default data
                        $parameters =~ s/^\$$name\s*/seconds=30 message='Missing environment variable, using default 30 seconds' /;
                    }
                } else {
                    # Unknown format, stop reading
                    $parameters = "";
                }
            }
            General::Logging::log_user_progress_message("                    Sleep Command: $command_number");
            if (exists $sleep_parameters{'message'} && $sleep_parameters{'message'} ne "") {
                General::Logging::log_user_message($sleep_parameters{'message'});
            }
            $rc = General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "progress-message"  => 1,
                    "progress-interval" => (exists $sleep_parameters{'progress-interval'} ? $sleep_parameters{'progress-interval'} : 10),
                    "seconds"           => (exists $sleep_parameters{'seconds'} ? $sleep_parameters{'seconds'} : 0),
                    "minutes"           => (exists $sleep_parameters{'minutes'} ? $sleep_parameters{'minutes'} : 0),
                    "hours"             => (exists $sleep_parameters{'hours'}   ? $sleep_parameters{'hours'}   : 0),
                    "use-logging"       => 1,
                }
            );
            if ($rc == 0) {
                push @status_messages, "(/) Command $command_number (sleep_with_progress) successful";
            } else {
                push @status_messages, "(x) Command $command_number (sleep_with_progress) was interrupted";
                General::Logging::log_user_warning_message("User interrupted Sleep with CTRL-C");
            }
            General::Logging::log_user_progress_message("                 Finished Command: $command_number\n                      Return Code: $rc");
            next;
        } else {
            $type = "COMMAND";
        }

        if ($type eq "BASH_COMMAND") {
            $use_bash_file = 1;
        } else {
            $use_bash_file = 0;
        }

        if ($type eq "SHOW_COMMAND") {
            $hide_output = 0;
        } else {
            $hide_output = 1;
        }
        @result = ();

        # Check if we need to modify the command in some way
        $command = special_command_handling($command, $command_number);

        if ($command =~ /^\s*\S*\/(\S+)\s*/) {
            $command_name = $1;
        } elsif ($command =~ /^\s*(\S+)\s*/) {
            $command_name = $1;
        } else {
            $command_name = "UNKNOWN";
        }
        if ($command_name eq "execute_playlist.pl") {
            if ($command =~ /.+(-p\s+|--playlist\s+|--playlist=)(\S+)/) {
                # Include the playlist name in command name
                $command_name .= " Playlist=$2";
            }
        }
        if ($command_name =~ /\s+/) {
            my $tmp = $command_name;
            $tmp =~ s/\s+/_/g;
            $command_log_file = sprintf "%s/CMD%03d_%s.log", $log_directory, $command_number, $tmp;
        } else {
            $command_log_file = sprintf "%s/CMD%03d_%s.log", $log_directory, $command_number, $command_name;
        }

        if ($type eq "BACKGROUND_COMMAND") {
            General::Logging::log_user_progress_message("               Background Command: $command_number ($command_name)");
            General::Logging::log_user_message("Starting: $command");
            my $pid = General::OS_Operations::background_process_run($command, $command_log_file);
            if ($pid > 0) {
                `logger -t DAFT_SEQ 'PID=$$ Started in Background "$command" with process id $pid' 2> /dev/null`;
                General::Logging::log_user_message("Background PID: $pid\nLog file:  $command_log_file\n");
                push @status_messages, "(?) Background Command $command_number ($command_name) started with PID $pid";
                $rc = 0;
                General::Logging::log_user_progress_message("                  Started Command: $command_number ($command_name)\n                      Return Code: $rc");
                $background_process_info{$pid}{"Command Number"} = $command_number;
                $background_process_info{$pid}{"Command"} = $command;
                $background_process_info{$pid}{"Log File"} = $command_log_file;
            } else {
                `logger -t DAFT_SEQ 'PID=$$ Failed to start in Background "$command"' 2> /dev/null`;
                General::Logging::log_user_error_message("Background Command Failed.\nLog file:  $command_log_file\n");
                push @status_messages, "(x) Background Command $command_number ($command_name) failed to start";
                $rc = 1;
                $program_rc = $rc;
                General::Logging::log_user_progress_message("                 Finished Command: $command_number ($command_name)\n                      Return Code: $rc");
                if ($stop_on_error == 1) {
                    last;
                }
            }
        } else {
            General::Logging::log_user_progress_message("                 Starting Command: $command_number ($command_name)");
            General::Logging::log_user_message("Executing: $command\nMonitoring of the command execution can be done in another terminal with command:\ntail -f $log_directory/command.log");
            if ($hide_output == 0) {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"           => $command,
                        "command-in-output" => 1,
                        "hide-output"       => 1,
                        "save-to-file"      => $command_log_file,
                        "return-output"     => \@result,
                        "use-bash-file"     => $use_bash_file,
                    }
                );
                shift @result;      # Remove the command
                $message = join "\n", @result;
                $message .= "\n";
            } else {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"           => $command,
                        "command-in-output" => 1,
                        "hide-output"       => 1,
                        "save-to-file"      => $command_log_file,
                        "use-bash-file"     => $use_bash_file,
                    }
                );
                $message = "";
            }
            `logger -t DAFT_SEQ 'PID=$$ Executed "$command" ended with exit code: $rc' 2> /dev/null`;

            # Check if execution was interrupted by the user.
            $signal_int_captured = General::OS_Operations::signal_captured("INT");
            $signal_quit_captured = General::OS_Operations::signal_captured("QUIT");
            if ($signal_quit_captured > 0) {
                General::Logging::log_user_warning_message("Detected that the user interrupted the execution by pressing CTRL-\\ or sending signal SIGQUIT to process id $$\n");
                $user_interrupted_execution = 1;
                $rc = 1;
                # Always stop execution
                $stop_on_error = 1;
            } elsif ($signal_int_captured > 0) {
                General::Logging::log_user_warning_message("Detected that the user interrupted the execution by pressing CTRL-C or sending signal SIGINT to process id $$\n");
                $user_interrupted_execution = 1;
                $rc = 1;
            }

            if ($rc == 0) {
                General::Logging::log_user_message("${message}Log file:  $command_log_file\n");
                push @status_messages, "(/) Command $command_number ($command_name) successful";
            } else {
                General::Logging::log_user_error_message("${message}Command Failed.\nLog file:  $command_log_file\n");
                push @status_messages, "(x) Command $command_number ($command_name) failed with rc=$rc";
                if ($user_interrupted_execution == 1 && $signal_quit_captured > 0) {
                    push @status_messages, "    Interrupted by user pressing CTRL-\\ or signal SIGQUIT to process id $$";
                } elsif ($user_interrupted_execution == 1 && $signal_int_captured > 0) {
                    push @status_messages, "    Interrupted by user pressing CTRL-C or signal SIGINT to process id $$";
                }
                $program_rc = $rc;
                if ($stop_on_error == 1) {
                    General::Logging::log_user_progress_message("                 Finished Command: $command_number ($command_name)\n                      Return Code: $rc");
                    last;
                }
            }
            General::Logging::log_user_progress_message("                 Finished Command: $command_number ($command_name)\n                      Return Code: $rc");
        }
    }
}

# -----------------------------------------------------------------------------
#
sub read_config_file {
    my @config_data = ();
    my $config_file = shift;
    my $rc = General::File_Operations::read_file(
        {
            "filename"            => $config_file,
            "output-ref"          => \@config_data,
            "ignore-empty-lines"  => 1,
            "ignore-pattern"      => '^\s*#.*',
        }
    );

    if ($rc != 0) {
        print "Error: Failed to read the file $config_file\n";
        exit 1;
    }

    for (@config_data) {
        if (/^\s*ENVIRONMENT_VARIABLE:\s*(\S+?)=(.*)/) {
            my $var = $1;
            my $value = $2;
            $value =~ s/^\s+//;                 # Remove preceeding whitespace
            $value =~ s/\s+$//;                 # Remove trailing whitespace
            $value =~ s/^['"](.*)['"]$/$1/;     # Remove quotes at beginning and end
            $ENV{$var} = $value;
            $environment_variables{$var} = $value;
        } elsif (/^\s*PLAYLIST_VARIABLE:\s*(\S+?)=(.*)/) {
            my $var = $1;
            my $value = $2;
            $value =~ s/^\s+//;                 # Remove preceeding whitespace
            $value =~ s/\s+$//;                 # Remove trailing whitespace
            $value =~ s/^['"](.*)['"]$/$1/;     # Remove quotes at beginning and end
            $playlist_variables{$var} = $value;
        } elsif (/^\s*SCRIPT_VARIABLE:\s*(\S+?)=(.*)/) {
            my $var = lc($1);
            my $value = $2;
            $value =~ s/^\s+//;                 # Remove preceeding whitespace
            $value =~ s/\s+$//;                 # Remove trailing whitespace
            $value =~ s/^['"](.*)['"]$/$1/;     # Remove quotes at beginning and end
            if ($var eq "background-handling") {
                if ($value =~ /^(finish|stop)$/i) {
                    $background_handling = lc($value);
                } else {
                    print "Error: Incorrect format for SCRIPT_VARIABLE '$var', only 'finish' or 'stop' allowed.\n";
                    exit 1;
                }
            } elsif ($var eq "background-max-wait-time") {
                if ($value =~ /^(\d+)$/i) {
                    $background_max_wait_time = $1;
                } elsif ($value =~ /^(\d+)s$/i) {
                    $background_max_wait_time = $1;
                } elsif ($value =~ /^(\d+)m$/i) {
                    $background_max_wait_time = $1 * 60;
                } elsif ($value =~ /^(\d+)h$/i) {
                    $background_max_wait_time = $1  * 3600;
                } else {
                    print "Error: Incorrect format for SCRIPT_VARIABLE '$var', only <integer>, <integer>s, <integer>m or <integer>h values allowed.\n";
                    exit 1;
                }
            } elsif ($var eq "color") {
                if ($value =~ /^(bright|cyan|dark|html|no)$/i) {
                    $color_scheme = lc($value);
                } else {
                    print "Error: Incorrect format for SCRIPT_VARIABLE '$var', only 'html', 'no', 'dark', 'bright' or 'cyan' allowed\n";
                    exit 1;
                }
            } elsif ($var eq "jobname") {
                $jobname = $value;
            } elsif ($var eq "log-file") {
                $jobname = $value;
            } elsif ($var eq "stop-on-error") {
                if ($value =~ /^(1|yes|true)$/i) {
                    $stop_on_error = 1;
                } elsif ($value =~ /^(0|no|false)$/i) {
                    $stop_on_error = 0;
                } else {
                    print "Error: Incorrect format for SCRIPT_VARIABLE '$var', only '1', 'yes', 'true', '0', 'no' or 'false' allowed\n";
                    exit 1;
                }
            } elsif ($var eq "workspace") {
                $workspace_dir = $value;
            } else {
                print "Warning: Unknown SCRIPT_VARIABLE '$var', value ignored\n";
            }
        } elsif (/^\s*BACKGROUND_COMMAND:\s*.+/) {
            push @command_list, $_;
        } elsif (/^\s*BASH_COMMAND:\s*.+/) {
            push @command_list, $_;
        } elsif (/^\s*COMMAND:\s*(.+)/) {
            push @command_list, $1;
        } elsif (/^\s*SHOW_COMMAND:\s*.+/) {
            push @command_list, $_;
        } elsif (/^\s*SLEEP_WITH_PROGRESS:\s*.+/) {
            push @command_list, $_;
        } elsif (/^\s*IF_ENVIRONMENT_VAR\s+\S+="[^"]*"\s+.+/) {
            push @command_list, $_;
        } elsif (/^\s*IF_NOT_ENVIRONMENT_VAR\s+\S+="[^"]*"\s+.+/) {
            push @command_list, $_;
        } else {
            s/^\s*//;
            push @command_list, $_;
        }
    }
}

# -----------------------------------------------------------------------------
#
sub show_commands_to_be_executed {
    if (%environment_variables) {
        print "\nEnvironment Variables to be set:\n";
        print   "--------------------------------\n";
        for my $var (sort keys %environment_variables) {
            if ($environment_variables{$var} =~ /(\s|=)/) {
                print "$var=\"$environment_variables{$var}\"\n";
            } else {
                print "$var=$environment_variables{$var}\n";
            }
        }
    }

    if (%playlist_variables) {
        print "\nPlaylist Variables to be set:\n";
        print   "-----------------------------\n";
        for my $var (sort keys %playlist_variables) {
            if ($playlist_variables{$var} =~ /(\s|=)/) {
                print "$var=\"$playlist_variables{$var}\"\n";
            } else {
                print "$var=$playlist_variables{$var}\n";
            }
        }
    }

    if (@command_list) {
        print "\nCommands to be executed:\n";
        print   "------------------------\n";
        my $cnt = 1;
        for my $command (@command_list) {
            if ($command =~ /^(IF_ENVIRONMENT_VAR\s+(\S+)="([^"]*)"\s+).+/) {
                my $remove = $1;
                my $var = $2;
                my $regexp = $3;
                if (exists $ENV{$var}) {
                    if ($ENV{$var} !~ /$regexp/i) {
                        # The environment variable does not match, so don't execute this command
                        next;
                    }
                    # The environment variable match, so we want to execute this command
                } else {
                    # The environment variable does not exist, treat it like the variable did not match, so don't execute this command
                    next;
                }
                # Remove the IF_ENVIRONMENT_VAR part, leaving only the command
                $command =~ s/^$remove//;
            } elsif ($command =~ /^(IF_NOT_ENVIRONMENT_VAR\s+\S+="[^"]*"\s+).+/) {
                my $remove = $1;
                my $var = $2;
                my $regexp = $3;
                if (exists $ENV{$var}) {
                    if ($ENV{$var} =~ /$regexp/i) {
                        # The environment variable match, so don't execute this command
                        next;
                    }
                    # The environment variable does not match, so we want to execute this command
                } else {
                    # The environment variable does not exist, treat it like the variable match, so execute this command
                }
                # Remove the IF_NOT_ENVIRONMENT_VAR part, leaving only the command
                $command =~ s/^$remove//;
            }
            $command = special_command_handling($command, $cnt);
            printf "%2d) $command\n\n", $cnt++;
        }
    }
}

# -----------------------------------------------------------------------------
#
sub special_command_handling {
    my $input_cmd = shift;
    my $cmd_num = shift;
    my $output_cmd;

    if ($input_cmd =~ /^send_command_to_ssh\.exp\s+/) {
        # Add absolute path to the script
        $input_cmd = "$package_dir/expect/bin/$input_cmd";
    }

    if ($input_cmd =~ /execute_playlist\.pl\s+/) {
        my $before = "";
        my $after  = "";
        if ($input_cmd =~ /^execute_playlist\.pl\s+/) {
            # Add absolute path to the playlist script
            $input_cmd = "$package_dir/perl/bin/$input_cmd";
        } elsif ($input_cmd =~ /^(.+"\S*)(execute_playlist\.pl\s+.+?)(".*)$/) {
            $before = $1;
            $input_cmd = $2;
            $after = $3;
        } elsif ($input_cmd =~ /^(.+'\S*)(execute_playlist\.pl\s+.+?)('.*)$/) {
            $before = $1;
            $input_cmd = $2;
            $after = $3;
        }

        # Use same colors for execute_playlist.pl command
        $input_cmd .= " -c $color_scheme";

        # Check if any playlist variables need to be added
        for my $var_name (keys %playlist_variables) {
            if ($input_cmd =~ / (-v |--variable |--variable=)$var_name=/) {
                # The command already includes this parameter, leave it alone
                next;
            } else {
                # The command does not include this parameter, add it to the end
                $input_cmd .= " -v $var_name=$playlist_variables{$var_name}";
            }
        }

        # By default do not allow user input, i.e. if something is missing or when fault detected then cause fallback
        if ($input_cmd =~ / (-v |--variable |--variable=)INPUT_AVAILABLE=/) {
            # User already specified this variable, leave it as-is even if it might not work
        } elsif ($input_cmd =~ / (-v |--variable |--variable=)CONSOLE_INPUT_AVAILABLE=/) {
            # User already specified this variable, leave it as-is even if it might not work
        } else {
            # Neither INPUT_AVAILABLE nor CONSOLE_INPUT_AVAILABLE specified, so add not allowed to ask for information
            $input_cmd .= " -v INPUT_AVAILABLE=no";
        }

        # Check if a job name has been specified, prepend the CMDxxx_ string
        if ($input_cmd =~ / (-j\s+|--jobname\s+|--jobname=)([^\s]+)/) {
            # User already specified this parameter, prepend the command number string
            my $param = $1;
            my $value = $2;
            my $newname = sprintf "CMD%03d_%s", $cmd_num, $value;
            $value =~ s/\+/\\+/g;
            $input_cmd =~ s/$param$value/$param$newname/;
        } else {
            # Not specified, just use the command number string
            $input_cmd .= sprintf " -j CMD%03d", $cmd_num;
        }

        # Check if a workspace directory has been specified, if not use log_directory
        if ($input_cmd =~ / (-w |--workspace |--workspace=)/) {
            # User already specified this parameter, leave it as-is
        } else {
            $input_cmd .= " -w $log_directory";
        }

        $input_cmd = "$before$input_cmd$after";
    } elsif ($input_cmd =~ /^download_csar\.pl\s+/) {
        # Add absolute path to the script
        if (-f "$package_dir/perl/bin/download_csar.pl") {
            $input_cmd = "$package_dir/perl/bin/$input_cmd";
        } elsif (-f "$package_dir/../scripts/download_csar.pl") {
            $input_cmd = "$package_dir/../scripts/$input_cmd";
        }
    }

    return $input_cmd;
}

# -----------------------------------------------------------------------------
#
sub start_logfile {
    # First create the log directory, if not existing
    unless (-d $log_directory) {
        my $output = `mkdir -p $log_directory 2>&1`;
        if ($?) {
            print "Error: Failed to create the log directory.\n$output\n";
            exit 1;
        }
    }
    $log_directory = abs_path $log_directory;

    # Next create the directory for the log file, if different than log directory
    if ($log_file eq "") {
        $log_file = "$log_directory/all.log";
    } else {
        my $dir = dirname($log_file);
        my $fn = basename($log_file);
        unless (-d $dir) {
            my $output = `mkdir -p $dir 2>&1`;
            if ($?) {
                print "Error: Failed to create the directory for the log file ($dir).\n$output\n";
                exit 1;
            }
        }
        $log_file = abs_path $dir . "/" . $fn;
    }

    # Start logging of output to file
    if ( General::Logging::log_enable("$log_file") != 0) {
        print "Error: Unable to create the log file ($log_file).\n";
        exit 1;
    }

    # Set the temporary file used for OS_Operations
    General::OS_Operations::set_temporary_output_file("$log_directory/command.log");
    General::OS_Operations::set_temporary_stderr_file("$log_directory/stderr.log");

    # Set the name of the input pipe that can be used to feed information into the playlist execution
    General::OS_Operations::set_input_named_pipe_file("$log_directory/input.pipe");

    # Change default progress.log file name
    $General::Logging::progress_file_name = "$log_directory/progress.log";

    # Create a lock file to keep track of if a job is using the workspace directory
    $rc = General::File_Operations::write_file(
        {
            "filename"            => "$log_directory.lock",
            "output-ref"          => [
                "STARTTIME=$current_timestamp",
                "HOSTNAME=$hostname",
                "USER=$ENV{'USER'}",
                "PID=$$",
                "SCRIPT=$script_name",
                "PROGRESS_FILE=$log_directory/progress.log",
            ],
            "eol-char"            => "\n",
            "append-file"         => 1,
            "file-access-mode"    => "666",
        }
    );
    if ($rc != 0) {
        print "ERROR: Unable to create job lock file '$log_directory.lock'\n";
        exit 1;
    }
}

# -----------------------------------------------------------------------------
#
sub stop_background_tasks {
    my @children = ();
    my $command;
    my $command_number;

TRY_AGAIN:

    if ($background_handling eq "stop") {
        # The user want any background processes that are still running to be stopped
        # when all other commands has been sent.

        # Stop any ongoing background tasks
        for my $pid (keys %background_process_info) {
            $command = $background_process_info{$pid}{"Command"};
            $command_number = $background_process_info{$pid}{"Command Number"};

            # Check if the background process is still running
            if (General::OS_Operations::process_is_running($pid) == 1) {
                General::Logging::log_user_message("Stopping background command $command_number with process id $pid");
                # Process exists, fetch children
                push @children, General::OS_Operations::process_return_all_children($pid);

                # Next kill the main process
                if (General::OS_Operations::background_process_kill($pid) == 1) {
                    # Process killed
                    `logger -t DAFT_SEQ 'PID=$$ Background process $pid "$command" was successfully stopped' 2> /dev/null`;
                    push @status_messages, "(/) Background Command $command_number with PID $pid was successfully stopped";
                } else {
                    # Process did not exist or could not be killed
                    `logger -t DAFT_SEQ 'PID=$$ Background process $pid "$command" failed to be killed' 2> /dev/null`;
                    push @status_messages, "(x) Background Command $command_number with PID $pid failed to be killed";
                }
            } else {
                # Process did not exist, nothing to kill (hopefully it also stopped aall it's running children)
                `logger -t DAFT_SEQ 'PID=$$ Background process $pid "$command" had already been stopped' 2> /dev/null`;
                push @status_messages, "(?) Background Command $command_number with PID $pid had already been stopped";
            }
        }

        # Next go through all children to see if they also needs to be stopped
        for my $pid (@children) {
            if (General::OS_Operations::process_is_running($pid) == 1) {
                General::Logging::log_user_message("Stopping background command child with process id $pid");
                if (General::OS_Operations::background_process_kill($pid) == 0) {
                    General::Logging::log_user_error_message("Failed to stop background command child with process id $pid");
                }
            } else {
                General::Logging::log_user_message("Background command child with process id $pid has already been stopped");
            }
        }
    } elsif ($background_handling eq "finish") {
        # The user want any background processes that are still running to be allowed
        # to finish normally when all other commands has been sent.

        my @pids = keys %background_process_info;
        my $abort_wait_time = time() + $background_max_wait_time;
        while (@pids) {
            my $pid = $pids[0];

            $command = $background_process_info{$pid}{"Command"};
            $command_number = $background_process_info{$pid}{"Command Number"};

            # Check if the background process is still running
            if (General::OS_Operations::process_is_running($pid) == 1) {
                if ($pid == $xterm_pid) {
                    # For this process we do special handling
                    shift @pids;
                    #delete $background_process_info{$pid};
                }

                if ($background_max_wait_time > 0 && time() >= $abort_wait_time) {
                    General::Logging::log_user_warning_message("Background command $command_number with process id $pid is still running, but we have waited long enough and will give up now.\n");
                    last;
                }
                General::Logging::log_user_message("Background command $command_number with process id $pid is still running.\nWaiting for it to finish naturally.\n");
                $rc = General::OS_Operations::sleep_with_progress(
                    {
                        "allow-interrupt"   => 1,
                        "progress-message"  => 0,
                        "seconds"           => 5,
                        "use-logging"       => 1,
                    }
                );
            } else {
                # Process did not exist, nothing to kill (hopefully it also stopped aall it's running children)
                `logger -t DAFT_SEQ 'PID=$$ Background process $pid "$command" had already been stopped' 2> /dev/null`;
                push @status_messages, "(?) Background Command $command_number with PID $pid had already been stopped";
                # Remove this pid from the list
                shift @pids;
                delete $background_process_info{$pid};
            }
        }

        my $heading_printed = 0;
        my $message = "";
        for my $pid (keys %background_process_info) {
            # Check if the background process is still running
            if (General::OS_Operations::process_is_running($pid) == 1) {
                next if ($pid == $xterm_pid);
                if ($heading_printed == 0) {
                    $message = "The following process id's might need to be manually killed:\n";
                    $heading_printed = 1;
                }
                $command = $background_process_info{$pid}{"Command"};
                $command_number = $background_process_info{$pid}{"Command Number"};
                $message .= "  Process $pid, Command: $command\n  kill -9 $pid\n";

                # Fetch children
                @children = General::OS_Operations::process_return_all_children($pid);
                if (@children) {
                    $message .= "    And it's children:\n";
                    for my $child (@children) {
                        $message .= "      kill -9 $child\n";
                    }
                }
                $message .= "\n";
            }
        }
        if ($message ne "") {
            General::Logging::log_user_warning_message($message);
        }

        # Now kill any remaining xterm process
        if ($xterm_pid != 0) {
            $background_handling = "stop";
            goto TRY_AGAIN;
        }
    } else {
    }
}

# -----------------------------------------------------------------------------
#
sub usage {
    print <<EOF;

Description:
============

This script is used for executing a number of DAFT playlists or other scripts in
a sequential maner WITHOUT possibility for manual interventions.

NOTE:
The commands that are specified must either; exist in the PATH, include the full
path in the command line, or for send_command_to_ssh.exp, execute_playlist.pl
and download_csar.pl commands the path is taken from the same path as this script.

Syntax:
=======

$0 [<OPTIONAL>]

<OPTIONAL> are one or more of the following parameters:

  -b <string>           | --background-command <string>
                          --background-handling <finish|stop>
                          --background-max-wait-time <string>
                          --color <no|dark|bright|cyan>
  -c <string>           | --command <string>
  -d                    | --dry-run
  -e <string>           | --environment-variable <string>
  -f <filename>         | --file <filename>
  -h                    | --help
  -j <string>           | --jobname <string>
  -l <filename>         | --log-file <filename>
  -o <string>           | --xterm-option <string>
                          --remove-extra-space
  -p                    | --show-progress
  -s                    | --stop-on-error
  -v <name>=<value>     | --playlist-variable <name>=<value>
  -w <directory>        | --workspace <directory>
  -x                    | --expand-variables

where:

  -b <string>
  --background-command <string>
  -----------------------------
  Specifies a command that will be executed in the background while execution
  of other commands continues without waiting for the result of the background
  command.

  This parameter can be specified multiple times and the commands will be executed
  in the order they have been gived with the --background-command, --bash-command,
  --command, --show-command and --file parameters.

  NOTE:
  The commands running in the background SHOULD NOT produce any output to
  standard output (STDOUT) or standard error (STDERR) file handles or it will
  mess up the output from the current running scripts.
  If the commands need to produce output they should be redirected to files
  instead.


  --background-handling <finish|stop>
  -----------------------------------
  Specifies the handling to be done for still running background processes when
  all commands has been executed.

  If "finish" which is also the default value, then the background processes
  are allowed to finish naturally as long as the maximum waiting time has not
  expired as specified with --background-max-wait-time parameter.

  If "stop", then all background processes that are still running when the last
  command has finished will be forcefully terminated by 'kill -9 <pid>'.
  Also any if it's childern processes that are still running will be terminated.


  --background-max-wait-time <string>
  -----------------------------------
  It specifies the maximum time to wait for all background processes to naturally finish after
  the last command has finished.
  It only is used when --background-handling=finish is in affect.
  If not specified then the default time is 30m (30 minutes).
  It can be specified as:
    - And integer value which means this many seconds.
    - <integer>s meaning this many seconds.
    - <integer>m meaning this many minutes.
    - <integer>h meaning this many hours.
    - 0 meaning it will wait indefinitely for the background processes to terminate.


  --bash-command <string>
  -----------------------
  Specifies a command that will be executed in the foreground while execution
  of other commands will be delayed until the current command completes.
  The difference between --command and --bash-command is that the latter is wrapped
  inside of a bash file which can be useful if the command contains pipes '|' or
  redirection '>' or '>>' commands that might otherwise cause issues.

  This parameter can be specified multiple times and the commands will be executed
  in the order they have been gived with the --background-command, --bash-command,
  --command, --show-command and --file parameters.


  --color <no|dark|bright|cyan>
  --------------------------
  If specified then color output is enabled for log messages printed to the
  screen.
  The selected color scheme can be either "no" "dark", "bright" or "cyan"
  depending on the current used Terminal background color.
  If you don't want any colors, select "no".
  If you have a dark background, select "dark".
  If you have a bright background, select "bright".
  If you want to have time and progress messages in 'cyan' color, select "cyan".

  The default, if this parameter is not given, is to use the 'dark' color scheme
  for messages printed to the screen.


  -c <string>
  --command <string>
  ------------------
  Specifies a command that will be executed in the foreground while execution
  of other commands will be delayed until the current command completes.

  This parameter can be specified multiple times and the commands will be executed
  in the order they have been gived with the --background-command, --bash-command,
  --command, --show-command and --file parameters.


  -d
  --dry-run
  ---------
  If specified then it will only show the commands to be executed and not execute them.


  -e <string>
  --environment-variable <string>
  -------------------------------
  Specifies an environment variable that will be set before executing any of the
  commands.

  This parameter can be specified multiple times if more than one file should be loaded.


  -f <filename>
  --file <filename>
  -----------------
  Specifies a file that contains commands, environment and playlist variables to be set
  instead of specifying these with the the --background-command, --command, --environment-variable,
  and --playlist-variables parameters.

  This parameter can be specified multiple times if more than one file should be used.
  The commands will be executed in the order they have been gived with the --background-command,
  --bash-command, --command, --show-command and --file parameters.

  The input file can have the following tags at the start of the line (preceeding whitespace is ignored) :

    - BACKGROUND_COMMAND: command with parameters
      The command will be executed in the background and execution of the next command
      will immediately start.

    - BASH_COMMAND: command with parameters
      The command will be executed in the foreground from a bash wrapper file which
      might be useful if the command contains pipes or redirections, the next command
      is executed when this command has finished.

    - COMMAND: command with parameters
      The command will be executed in the foreground and the next command will be
      executed when this command has finished.

    - ENVIRONMENT_VARIABLE: XXXX=yyyy
      Set an environment variable which might be used by the command.

    - IF_ENVIRONMENT_VAR XXXX="yyyy" [(BACKGROUND_COMMAND|BASH_COMMAND|COMMAND|SLEEP_WITH_PROGRESS|SHOW_COMMAND):] ....
      It the environment variable XXXX contents match the Perl regular expression yyyy then the
      command that follows will be executed, and if not matching then the command will be skipped.
      If the environment variable does not exist then the command will be skipped.

    - IF_NOT_ENVIRONMENT_VAR XXXX="yyyy" [(BACKGROUND_COMMAND|BASH_COMMAND|COMMAND|SLEEP_WITH_PROGRESS|SHOW_COMMAND):] ....
      It the environment variable XXXX contents does not match the Perl regular expression yyyy then the
      command that follows will be executed, and if matching then the command will be skipped.
      If the environment variable does not exist then the command will be executed.

    - PLAYLIST_VARIABLE: XXXX=yyyy
      Set a specific playlist variable which will be used as the default value when
      executing the execute_playlist.pl command.

    - SCRIPT_VARIABLE: XXXX=yyyy
      Set a specific variable used by this script which migth be useful if the loaded
      file should set a variable that should change the default behavior of a command
      line parameter e.g. --stop-on-error.
      Currently allowed values are:
        - SCRIPT_VARIABLE: background-handling=(finish|stop)
        - SCRIPT_VARIABLE: background-max-wait-time=<integer>[s|m|h]
        - SCRIPT_VARIABLE: color=(bright|cyan|dark|html|no)
        - SCRIPT_VARIABLE: jobname=<string>
        - SCRIPT_VARIABLE: log-file=<file path>
        - SCRIPT_VARIABLE: stop-on-error=(1|yes|true|0|no|false)
        - SCRIPT_VARIABLE: workspace=<directory path>

    - SLEEP_WITH_PROGRESS: [seconds=<integer>] [minutes=<integer>] [hours=<integer>] [progress-interval=<integer>] [message="<string>"]
      This will start a sleep timeir for the amount of time specified with a progress message every
      'progress-interval' seconds and with optionally showing a 'message'.

    - SHOW_COMMAND: command with parameters
      The command will be executed in the foreground and the next command will be
      executed when this command has finished.
      The output from the command is shown to the user after it finish execution.

    - # This is a comment line that will be ignored.

    - Blank lines are also ignored.

    - Any other line will be taken as a command in the same way as the COMMAND: line.


  -h
  --help
  ------
  Shows this help information.


  -j <string>
  --jobname <string>
  ------------------
  Specifies the name of the job, this can e.g. be a descriptive name of what
  the playlist will do.
  For example: --jobname Snorlax_Deploy_SC_1.12.0+43

  If not specified then a default job name will be used consisting of the file name
  (just the file name part without the extension) given in --file parameter and if
  multiple files are specified then a concatenation of the names.
  For example:
    --file=/path/to/deploy_and_configure_sc.txt
        will result in job name "deploy_and_configure_sc"

    --file=/path/to/deploy_and_configure_sc.txt
    --file=/path/to/upgrade_sc.txt
        will result in job name "deploy_and_configure_sc_upgrade_sc"


  -l <filename>
  --log-file <filename>
  ---------------------
  Specifies the log file that will be used for logging all output from this script.
  If not specified it will use the file name 'all.log'
  which is placed in the sub directory created under the --workspace parameter.


  -o <string>
  --xterm-option <string>
  -----------------------
  This parameter can be used to change the appearance of the xterm window that is opened
  when parameter --show-progress is given.
  This parameter can be specified multiple time if more than one xterm parameter should be
  set.
  If not specified then the following defaults will be used:
  --xterm-option="-geometry 250x20+0+0 -bg white -sl 300"

  Here are some examples:

  To change the size and location of the window <width>x<height>+<x-position>+<y-position>:
  --xterm-option="-geometry 250x20+0+0"

  To change the background color:
  --xterm-option="-bg darkblue"

  To change the background color:
  --xterm-option="-fg cyan"

  To change the number of lines in the scrollback buffer:
  --xterm-option="-sl 300"

  To show the window full screen:
  --xterm-option="-fullscreen"


  --remove-extra-space
  --------------------
  If specified it will remove all extra spaces that might have been used to nicely
  format the commands in the input file and just replace them with one space.
  This might be useful to shrink down the command length before executing it in the
  shell.


  --show-command <string>
  -----------------------
  Specifies a command that will be executed in the foreground while execution
  of other commands will be delayed until the current command completes.
  The difference between --command and --show-command is that the latter showing
  the command output to the screen after it finished it's execution whereas all
  other commands will not show any output to the screen but only logged to file.

  This parameter can be specified multiple times and the commands will be executed
  in the order they have been gived with the --background-command, --bash-command,
  --command, --show-command and --file parameters.


  -p
  --show-progress
  ---------------
  If specified then a separate xterm window will open and show the command.log file
  for the job, i.e. showing the output of the commands that the daft_sequencer.pl
  executes. This window will be opened as a background process and will show the
  contents of the command.log file as it's being changes, i.e. some kind of progress
  messages.
  To control the apperance of this window see the --xterm-option parameter above.

  NOTE:
  This option ONLY WORKS if the 'xterm' command exist, which means on most clusters
  this command doesn't exist and thus this parameter cannot be used.
  For these nodes you can manually open another terminal window to this node and
  then execute the following command to get the same information:
  tail -f <workspace directory/command.log


  -s
  --stop-on-error
  ---------------
  If specified then execution will stop and the script will exit when a command results in a
  non-zero return code.
  By default the execution will continue until all commands has been executed.


  -v <name>=<value>
  --playlist-variable <name>=<value>
  ----------------------------------
  Specifies a default playlist variable that will be set when executing the playlist
  command 'execute_playlist.pl' unless the command already includes this parameter in
  which case this value takes precedence.

  This parameter can be specified multiple times if more that one variable should be set.


  -w <directory>
  --workspace <directory>
  -----------------------
  Specifies the directory name which will be used as the main workspace directory
  for storing playlist related files (temporary or permanent) during the execution
  of the playlist.
  Under this directory a sub directory will be created named as follows:
  <jobname>_<datestamp YYYYMMDD>_<timestamp HHMMSS>_<random 2 digit number>
  This directory will also be used as the workspace directory for the execute_playlist.pl script
  unless it's already specified in the execute_playlist.pl command.

  If this parameter is not specified then the default location will be:
  /tmp/workspaces/


  -x
  --expand-variables
  ------------------
  If specified then expand all variables before executing the command, i.e. any used
  variables in the commands will be replaced by their value instead of allowing the
  shell to do this.
  This also makes it easier to see what the command to be executed actually looks like.
  NOTE: When this parameter is specified then --remove-extra-space will also automatically
  be enabled since the commands are no longer properly aligned after the expanding the
  variables.


Output:
=======

The script writes the execution summary of all commands executed by the script and
all details to log files.


Examples:
=========

  Example 1
  ---------
  Execute commands specified in a file.
  $0 -f /path/to/file

  Example 2
  ---------
  Execute commands specified in a file and set a few environment variables that is used inside the file.
  $0 -f /path/to/file \\
     -e DAFT_NETWORK_CONFIG_FILE=/home/eccd/network_config_files/Snorlax.xml \\
     -e DAFT_SC_SOFTWARE_VERSION=1.13.25+746

  or if using bash shell:

  DAFT_NETWORK_CONFIG_FILE=/home/eccd/network_config_files/Snorlax.xml DAFT_SC_SOFTWARE_VERSION=1.13.25+746 $0 -f /path/to/file


Return code:
============

   0: Successful, the execution was successful.
   1: Unsuccessful, some failure was reported.

EOF
}
