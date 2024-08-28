package General::Logging;

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.26
#  Date     : 2022-09-12 14:59:52
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2017-2022
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
    get_execution_times_summary
    log_color
    log_disable
    log_enable
    log_execution_times
    log_filename
    log_status
    log_user_error_message
    log_user_info_message
    log_user_message
    log_user_progress_message
    log_user_prompt
    log_user_warning_message
    log_write
    );

use Cwd qw(abs_path);
use File::Basename qw(dirname basename);
use General::OS_Operations;

our $execution_times_file_name  = "";
our $logging_active = 0;    # Logging is passive
our $log_file_name  = "";
our $progress_file_name  = "";

# Color values
our $color_error     = "";   # No color
our $color_info      = "";   # No color
our $color_normal    = "";   # No color
our $color_progress  = "";   # No color
our $color_reset     = "";   # No color
our $color_time      = "";   # No color
our $color_warning   = "";   # No color

my $divider = "=" x 80;
my $hashline = "#" x 34;

my $time_hires_available = 0;
eval 'require Time::HiRes;';
if ($@) {
    print "DBG:\$@=$@\n";
    $time_hires_available = 0;
} else {
    $time_hires_available = 1;
    import 'Time::HiRes  qw(gettimeofday)';
}

# -----------------------------------------------------------------------------
# Return an array of of execution times per Step and Task to the called to be
# e.g. displayed in the summary log.
#
# Input variables:
#    -
#
# Returns:
#    - An array with all the information.
#
# -----------------------------------------------------------------------------
sub get_execution_times_summary {
    my $action;
    my %active_step_times;
        # key:      Step reference name Playlist::xxxxxx::Stepname
        # value:    total_time <epochseconds.microseconds>
    my %active_task_times;
        # key:      Task reference name Playlist::xxxxxx::Stepname::Taskname
        # value:    Total Time <epochseconds.microseconds>
    my $name;
    my @result_list = ();
    my $time;
    my %total_step_times;
        # key:      Step reference name Playlist::xxxxxx::Stepname
        # value:    total_time <epochseconds.microseconds>
    my %total_task_times;
        # key:      Task reference name Playlist::xxxxxx::Stepname::Taskname
        # value:    Total Time <epochseconds.microseconds>
    my $type;

    # Check if the file exists
    unless (-f "$execution_times_file_name") {
        return @result_list;
    }

    unless (open INF, "$execution_times_file_name") {
        print "Unable to open file '$execution_times_file_name'\n";
        return @result_list;
    }

    for (<INF>) {
        s/[\r\n]//g;
        if (/^(\d+\.\d+),(start|stop),(step|task),(.+)/) {
            $time   = $1;
            $action = $2;
            $type   = $3;
            $name   = $4;

            if ($type eq "step") {
                if ($action eq "start") {
                    $active_step_times{$name} = $time unless exists $active_step_times{$name};
                } elsif ($action eq "stop") {
                    if (exists $active_step_times{$name}) {
                        if (exists $total_step_times{$name}) {
                            $total_step_times{$name} += $time - $active_step_times{$name};
                        } else {
                            $total_step_times{$name} = $time - $active_step_times{$name};
                        }
                        delete $active_step_times{$name};
                    } else {
                        print "Error: Stoptime detected without preceeding starttime for name '$name', $_\n";
                    }
                }
            } elsif ($type eq "task") {
                if ($action eq "start") {
                    $active_task_times{$name} = $time unless exists $active_task_times{$name};
                } elsif ($action eq "stop") {
                    if (exists $active_task_times{$name}) {
                        if (exists $total_task_times{$name}) {
                            $total_task_times{$name} += $time - $active_task_times{$name};
                        } else {
                            $total_task_times{$name} = $time - $active_task_times{$name};
                        }
                        delete $active_task_times{$name};
                    } else {
                        print "Error: Stoptime detected without preceeding starttime for name '$name', $_\n";
                    }
                }
            }
        }
    }

    close INF;

    push @result_list, "";
    push @result_list, "Summary of total execution times per Step:";
    push @result_list, "------------------------------------------";
    push @result_list, "";
    push @result_list, sprintf "%-12s  %s", "Total Time", "Step Name";
    push @result_list, sprintf "%s  %s", "-"x12, "-"x9;
    for $name (sort { $total_step_times{$b} <=> $total_step_times{$a} } keys %total_step_times) {
        push @result_list, sprintf "%12.6f  %s", $total_step_times{$name}, $name;
    }

    push @result_list, "";
    push @result_list, "Summary of total execution times per Task:";
    push @result_list, "------------------------------------------";
    push @result_list, "";
    push @result_list, sprintf "%-12s  %s", "Total Time", "Task Name";
    push @result_list, sprintf "%s  %s", "-"x12, "-"x9;
    for $name (sort { $total_task_times{$b} <=> $total_task_times{$a} } keys %total_task_times) {
        push @result_list, sprintf "%12.6f  %s", $total_task_times{$name}, $name;
    }

    return @result_list;
}

# -----------------------------------------------------------------------------
# Return a properly formated datetime separator string to be written to the
# log file.
#
# Input variables:
#    - Number of seconds since the epoch.
#    - Number of microseconds.
#
# Returns:
#    - A multiline scalar string containing separator characters and a date and
#      time stamp string.
#
# -----------------------------------------------------------------------------
sub log_datetime_str {
    my ($seconds, $microseconds) = @_;

    my $current_time = General::OS_Operations::detailed_datetime_str($seconds, $microseconds);

    return "\n$hashline\n# Date: $current_time\n$hashline\n";
}

# -----------------------------------------------------------------------------
# Enable or disable color output in log messages printed on the screen.
# By default if this subroutine is not called then colors are disabled.
# If called with action "enable" and no or wrong color scheme is provided then
# "bright" will be assumed.
# If called with wrong action then "disable" will be assumed.
#
# Input variables:
#    - Action to perform, which can be:
#      enable
#      disable
#    - Color scheme to use for current terminal background color, which can be:
#      dark
#      bright
#      html
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub log_color {
    my $action = shift;
    my $background = shift;

    if (lc($action) eq "enable") {
        $background = "bright" unless (defined $background);

        if ($background eq "dark") {
            # Dark background
            $color_error     = "\e[0m\e[31m";        # red
            $color_info      = "\e[0m\e[36m";        # cyan
            $color_normal    = "\e[0m\e[32m";        # green
            $color_progress  = "";
            $color_reset     = "\e[0m";              # reset
            $color_time      = "";
            $color_warning   = "\e[0m\e[33m";        # yellow
        } elsif ($background eq "cyan") {
            # Cyan color on the time and progress messages
            $color_error     = "\e[0m\e[31m";        # red
            $color_info      = "\e[0m\e[36m";        # cyan
            $color_normal    = "\e[0m\e[32m";        # green
            $color_progress  = "\e[0m\e[36m";        # cyan
            $color_reset     = "\e[0m";              # reset
            $color_time      = "\e[0m\e[36m";        # cyan
            $color_warning   = "\e[0m\e[33m";        # yellow
        } elsif ($background eq "html") {
            # Cyan color on the time and progress messages
            $color_error     = "<font color=red>";   # red
            $color_info      = "<font color=cyan>";  # cyan
            $color_normal    = "<font color=green>"; # green
            $color_progress  = "<font color=black>"; # cyan
            $color_reset     = "</font>";            # reset
            $color_time      = "<font color=black>"; # cyan
            $color_warning   = "<font color=orange>";# orange
        } else {
            # Bright background
            $color_error     = "\e[0m\e[31m";        # red
            $color_info      = "\e[0m\e[36m";        # cyan
            $color_normal    = "\e[0m\e[32m";        # green
            $color_progress  = "";
            $color_reset     = "\e[0m";              # reset
            $color_time      = "";
            $color_warning   = "\e[0m\e[33m";        # yellow
        }
    } else {
        # Disable color output
        $color_error     = "";
        $color_info      = "";
        $color_normal    = "";
        $color_progress  = "";
        $color_reset     = "";
        $color_time      = "";
        $color_warning   = "";
    }
}

# -----------------------------------------------------------------------------
# Disable the log function which means that no more detailed command execution
# will be written to the log file.
#
# Input variables:
#    -
#
# Return values:
#    Return code 0 = The log function was disabled successfully.
#                1 = The log function was disabled but failure to write to log file.
#
# -----------------------------------------------------------------------------
sub log_disable {
    my $calling_script = abs_path $0;
    unless (defined $calling_script) {
        # This was needed because for some reason when starting the kpi_statistics.pl
        # script as a background process inside of the 801_Time_Shift_Test.pm playlist
        # the $0 got corrupted by the termination of the kpi_statistics.pl script.
        my ($package, $filename, $line) = caller;
        $calling_script = $filename;
        $calling_script = "Unknown Caller" unless defined $calling_script;
    }

    if ($logging_active == 0) {
        # Log already passive
        return 0;
    } else {
        $logging_active = 0;
        if (open OUTF, ">>$log_file_name") {
            print OUTF log_datetime_str() . "# Logging Disabled by script '$calling_script'\n$hashline\n";
            close OUTF;
            return 0;
        } else {
            print "Unable to open log file '$log_file_name'\n";
            return 1;
        }
    }
}

# -----------------------------------------------------------------------------
# Enable the log function which means that detailed command execution will be
# written to the log file.
#
# Input variables:
#    - File name.
#
# Return values:
#    Return code 0 = The log function was activated successfully to specified log file.
#                1 = The log function could not be activated.
#
# -----------------------------------------------------------------------------
sub log_enable {
    my $new_name = shift;
    unless ($new_name) {
        print "You have to specify a log file name when calling 'log_enable'\n";
        return 1;
    }

    my $calling_script = abs_path $0;

    if ($logging_active) {
        # Log already active
        if ($new_name eq $log_file_name) {
            # Same log file, do nothing
            return 0;
        } else {
            # Different file
            if ($logging_active) {
                # Log already active
                $logging_active = 0;
                if (open OUTF, ">>$log_file_name") {
                    print OUTF log_datetime_str() . "# Logging Disabled by script '$calling_script'\n$hashline\n";
                    close OUTF;
                } else {
                    print "Unable to close log file '$log_file_name'\n";
                    return 1;
                }
            }
        }
    }

    # Open the new log file
    $log_file_name = $new_name;
    if (open OUTF, ">>$log_file_name") {
        print OUTF "\n$divider\n" . log_datetime_str() . "# Logging Enabled by script '$calling_script'\n$hashline\n";
        close OUTF;
        $progress_file_name = dirname($log_file_name) . "/progress.log";
        $execution_times_file_name = dirname($log_file_name) . "/execution_times.log";
        $logging_active = 1;
        return 0;
    } else {
        print "Unable to open log file '$log_file_name'\n";
        return 1;
    }
}

# -----------------------------------------------------------------------------
# Write execution time message to logfile if opened.
#
# Input variables:
#    - Detailed timestamp: <seconds.microseconds>
#    - Action: 'start' or 'stop'
#    - Type: 'step' or 'task'
#    - Task or Step reference string
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub log_execution_times {
    my $timestamp = shift;
    my $action = shift;
    my $type = shift;
    my $text = shift;

    if ($execution_times_file_name) {
        if (open OUTF, ">>$execution_times_file_name") {
            print OUTF "$timestamp,$action,$type,$text\n";
            close OUTF;
        } else {
            print "Unable to write to execution times file '$execution_times_file_name'\n";
        }
    }
}

# -----------------------------------------------------------------------------
# Return the current set log file name.
#
# Input variables:
#    -
#
# Return values:
#    Name of the current active log file.
#
# -----------------------------------------------------------------------------
sub log_filename {
    return $log_file_name;
}

# -----------------------------------------------------------------------------
# Write progress message to logfile if opened.
#
# Input variables:
#    - Message to write to log file
#
# Return values:
#    -
#
# -----------------------------------------------------------------------------
sub log_progress {
    my $text = shift;

    if ($progress_file_name) {
        if (open OUTF, ">>$progress_file_name") {
            print OUTF "$text";
            close OUTF;
        } else {
            print "Unable to write to progress file '$progress_file_name'\n";
        }
    }
}

# -----------------------------------------------------------------------------
# Get log function status.
#
# Input variables:
#    -
#
# Return values:
#    Return code 0 = The log function is not active.
#                1 = The log function is active.
#
# -----------------------------------------------------------------------------
sub log_status {
    return $logging_active;
}

# -----------------------------------------------------------------------------
# Write error text to user and to log file and add color if enabled.
#
# Input variables:
#    - Text to write to user and to log file, including new line characters.
#    - Error array reference, if specified, where the error text is also pushed.
#
# Return values:
#    Return code 0 = The text was written successfully to the log file and to
#                    the user.
#                1 = Message written to the user but failure to write text to
#                    log file.
#                2 = Logging is not enabled, nothing written to the log file but
#                    written to user.
#
# -----------------------------------------------------------------------------
sub log_user_error_message {
    my $text = shift;
    my $error_array_ref = shift;

    my $rc;
    my ($epochseconds, $microseconds);
    if ($time_hires_available) {
        ($epochseconds, $microseconds) = Time::HiRes::gettimeofday();
    } else {
        $epochseconds = time();
        $microseconds = 0;
    }

    $rc = log_write("ERROR Message:\n$text", $epochseconds, $microseconds);

    # Add the text to the error array if specified
    if (defined $error_array_ref) {
        push @$error_array_ref, split /\n/, $text;
    }

    if ($text =~ /\n.*\n/s) {
        # A multiline text is given, indent it properly
        $text =~ s/\n/\n                  /sg;
        # Remove the spaces at the end of the last line
        $text =~ s/\s+$/\n/;
    } elsif ($text =~ /\n[^\n]+$/s) {
        # A multiline text is given, indent it properly
        $text =~ s/\n/\n                  /sg;
        # Remove the spaces at the end of the last line
        $text =~ s/\s+$/\n\n/;
    }

    # Add color to the text
    $text =~ s/\n$//;
    $text = "${color_error}ERROR:\n                  $text$color_reset\n";

    # Print the message to both screen and the progress log file
    $text = $color_time . General::OS_Operations::detailed_time_str($epochseconds, $microseconds) . $color_reset . " $text";
    print "$text" if ($General::OS_Operations::console_output_available == 1);
    log_progress("$text");

    return $rc;
}

# -----------------------------------------------------------------------------
# Write information text to user and to log file and add color if enabled.
#
# Input variables:
#    - Text to write to user and to log file, including new line characters.
#
# Return values:
#    Return code 0 = The text was written successfully to the log file and to
#                    the user.
#                1 = Message written to the user but failure to write text to
#                    log file.
#                2 = Logging is not enabled, nothing written to the log file but
#                    written to user.
#
# -----------------------------------------------------------------------------
sub log_user_info_message {
    my $text = shift;

    my $rc;
    my ($epochseconds, $microseconds);
    if ($time_hires_available) {
        ($epochseconds, $microseconds) = Time::HiRes::gettimeofday();
    } else {
        $epochseconds = time();
        $microseconds = 0;
    }

    $rc = log_write("INFO Message:\n$text", $epochseconds, $microseconds);

    if ($text =~ /\n.*\n/s) {
        # A multiline text is given, indent it properly
        $text =~ s/\n/\n                  /sg;
        # Remove the spaces at the end of the last line
        $text =~ s/\s+$/\n/;
    } elsif ($text =~ /\n[^\n]+$/s) {
        # A multiline text is given, indent it properly
        $text =~ s/\n/\n                  /sg;
        # Remove the spaces at the end of the last line
        $text =~ s/\s+$/\n\n/;
    }

    # Add color to the text
    $text =~ s/\n$//;
    $text = "${color_info}INFO:\n                  $text$color_reset\n";

    # Print the message to both screen and the progress log file
    $text = $color_time . General::OS_Operations::detailed_time_str($epochseconds, $microseconds) . $color_reset . " $text";
    print "$text" if ($General::OS_Operations::console_output_available == 1);
    log_progress("$text");

    return $rc;
}

# -----------------------------------------------------------------------------
# Write text to user and to log file and add color if enabled.
#
# Input variables:
#    - Text to write to user and to log file, including new line characters.
#
# Return values:
#    Return code 0 = The text was written successfully to the log file and to
#                    the user.
#                1 = Message written to the user but failure to write text to
#                    log file.
#                2 = Logging is not enabled, nothing written to the log file but
#                    written to user.
#
# -----------------------------------------------------------------------------
sub log_user_message {
    my $text = shift;

    my $rc;
    my ($epochseconds, $microseconds);
    if ($time_hires_available) {
        ($epochseconds, $microseconds) = Time::HiRes::gettimeofday();
    } else {
        $epochseconds = time();
        $microseconds = 0;
    }

    $rc = log_write($text, $epochseconds, $microseconds);

    if ($text =~ /\n.*\n/s) {
        # A multiline text is given, indent it properly
        $text =~ s/\n/\n                  /sg;
        # Remove the spaces at the end of the last line
        $text =~ s/\s+$/\n/;
    } elsif ($text =~ /\n[^\n]+$/s) {
        # A multiline text is given, indent it properly
        $text =~ s/\n/\n                  /sg;
        # Remove the spaces at the end of the last line
        $text =~ s/\s+$/\n\n/;
    }

    # Add color to the text
    $text =~ s/\n$//;
    $text = "$color_normal$text$color_reset\n";

    # Print the message to both screen and the progress log file
    $text = $color_time . General::OS_Operations::detailed_time_str($epochseconds, $microseconds) . $color_reset . " $text";
    print "$text" if ($General::OS_Operations::console_output_available == 1);
    log_progress("$text");

    return $rc;
}

# -----------------------------------------------------------------------------
# Write progress text to user and to log file.
#
# Input variables:
#    - Text to write to user and to log file, including new line characters.
#
# Return values:
#    Return code 0 = The text was written successfully to the log file and to
#                    the user.
#                1 = Message written to the user but failure to write text to
#                    log file.
#                2 = Logging is not enabled, nothing written to the log file but
#                    written to user.
#
# -----------------------------------------------------------------------------
sub log_user_progress_message {
    my $text = shift;

    my $rc;
    my ($epochseconds, $microseconds);
    if ($time_hires_available) {
        ($epochseconds, $microseconds) = Time::HiRes::gettimeofday();
    } else {
        $epochseconds = time();
        $microseconds = 0;
    }

    $rc = log_write($text, $epochseconds, $microseconds);

    if ($text =~ /\n.*\n/s) {
        # A multiline text is given, indent it properly
        $text =~ s/\n/\n                  /sg;
        # Remove the spaces at the end of the last line
        $text =~ s/\s+$/\n/;
    } elsif ($text =~ /\n[^\n]+$/s) {
        # A multiline text is given, indent it properly
        $text =~ s/\n/\n                  /sg;
        # Remove the spaces at the end of the last line
        $text =~ s/\s+$/\n\n/;
    }

    # Add color to the text
    $text =~ s/\n$//;
    $text = "$color_progress$text$color_reset\n";

    # Print the message to both screen and the progress log file
    $text = $color_time . General::OS_Operations::detailed_time_str($epochseconds, $microseconds) . $color_reset . " $text";
    print "$text" if ($General::OS_Operations::console_output_available == 1);
    log_progress("$text");

    return $rc;
}

# -----------------------------------------------------------------------------
# Write prompt text to user and to log file without any colors.
#
# Input variables:
#    - Text to write to user and to log file, including new line characters.
#
# Return values:
#    Return code 0 = The text was written successfully to the log file and to
#                    the user.
#                1 = Message written to the user but failure to write text to
#                    log file.
#                2 = Logging is not enabled, nothing written to the log file but
#                    written to user.
#
# -----------------------------------------------------------------------------
sub log_user_prompt {
    my $text = shift;

    my $rc;
    my ($epochseconds, $microseconds);
    if ($time_hires_available) {
        ($epochseconds, $microseconds) = Time::HiRes::gettimeofday();
    } else {
        $epochseconds = time();
        $microseconds = 0;
    }

    $rc = log_write("$text\n", $epochseconds, $microseconds);

    if ($text =~ /\n.*\n/s) {
        # A multiline text is given, indent it properly
        $text =~ s/\n/\n                  /sg;
        # Remove the spaces at the end of the last line
        $text =~ s/                  $/\n/;
    } elsif ($text =~ /\n[^\n]+$/s) {
        # A multiline text is given, indent it properly
        $text =~ s/\n/\n                  /sg;
        # Remove the spaces at the end of the last line
        $text =~ s/                  $/\n\n/;
    }

    # Remove last NL character(s)
    $text =~ s/\n$//g;

    # Print the message to both screen and the progress log file
    $text = General::OS_Operations::detailed_time_str($epochseconds, $microseconds) . " $text";
    print "$text" if ($General::OS_Operations::console_output_available == 1);
    log_progress("$text\n");

    return $rc;
}

# -----------------------------------------------------------------------------
# Write warning text to user and to log file and add color if enabled.
#
# Input variables:
#    - Text to write to user and to log file, including new line characters.
#
# Return values:
#    Return code 0 = The text was written successfully to the log file and to
#                    the user.
#                1 = Message written to the user but failure to write text to
#                    log file.
#                2 = Logging is not enabled, nothing written to the log file but
#                    written to user.
#
# -----------------------------------------------------------------------------
sub log_user_warning_message {
    my $text = shift;

    my $rc;
    my ($epochseconds, $microseconds);
    if ($time_hires_available) {
        ($epochseconds, $microseconds) = Time::HiRes::gettimeofday();
    } else {
        $epochseconds = time();
        $microseconds = 0;
    }

    $rc = log_write("WARNING Message:\n$text", $epochseconds, $microseconds);

    if ($text =~ /\n.*\n/s) {
        # A multiline text is given, indent it properly
        $text =~ s/\n/\n                  /sg;
        # Remove the spaces at the end of the last line
        $text =~ s/\s+$/\n/;
    } elsif ($text =~ /\n[^\n]+$/s) {
        # A multiline text is given, indent it properly
        $text =~ s/\n/\n                  /sg;
        # Remove the spaces at the end of the last line
        $text =~ s/\s+$/\n\n/;
    }

    # Add color to the text
    $text =~ s/\n$//;
    $text = "${color_warning}WARNING:\n                  $text$color_reset\n";

    # Print the message to both screen and the progress log file
    $text = $color_time . General::OS_Operations::detailed_time_str($epochseconds, $microseconds) . $color_reset . " $text";
    print "$text" if ($General::OS_Operations::console_output_available == 1);
    log_progress("$text");

    return $rc;
}

# -----------------------------------------------------------------------------
# Write text to log file.
#
# Input variables:
#    Text to write to log file, including new line characters.
#
# Return values:
#    Return code 0 = The text was written successfully to the log file.
#                1 = Failure to write text to log file.
#                2 = Logging is not enabled, nothing written to the log file.
#
# -----------------------------------------------------------------------------
sub log_write {
    my ($text, $seconds, $microseconds) = @_;

    return 2 if $logging_active == 0;

    if ($log_file_name) {
        if (open OUTF, ">>$log_file_name") {
            print OUTF log_datetime_str($seconds, $microseconds) . "$text";
            close OUTF;
            return 0;
        } else {
            print "Unable to write to log file '$log_file_name'\n";
            return 1;
        }
    } else {
        print "No log file set with 'log_enable'\n";
        return 1;
    }
}

1;
