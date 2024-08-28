#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 2.79
#  Date     : 2024-05-21 11:58:43
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2018-2024
#
# The copyright to the computer program(s) herein is the property of
# Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission
# of Ericsson GmbH in accordance with the terms and conditions stipulated in
# the agreement/contract under which the program(s) have been supplied.
#
################################################################################

# ********************************
# *                              *
# * External module declarations *
# *                              *
# ********************************

use strict;
use warnings;

use File::Basename qw(dirname basename);
use Cwd qw(abs_path cwd);
use lib dirname(dirname abs_path $0) . '/lib';

use General::File_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Playlist_Operations;
use General::State_Machine;

use constant SHOW_AS_SUCCESS => 0;
use constant SHOW_AS_FAILED   => 1;
use constant SHOW_AS_WARNING => 2;

# *************************
# *                       *
# * Variable declarations *
# *                       *
# *************************

my $script_name = abs_path $0;
my $script_params = join " ", @ARGV;

# Install a handler for CTRL-C and CTRL-\ signals
General::OS_Operations::signal_capture_enable("ALL");

my $all_playlists = 0;
my @always_ask = ();
my @always_execute = ();
my @always_ignore_error = ();
my @always_skip = ();
our @append_file_to_summary = ();
my $background_tasks_file = "";
my @children = ();
our $color_scheme = "dark";
my $current_timestamp = `date +%Y%m%d_%H%M%S`;
$current_timestamp =~ s/[\r\n]//g;
my $error_cnt = 0;
our @exclude_at_packing_workspace_directory = ();       # Global variable that can be updated by the playlists if they want certain files or directories
                                                        # excluded from being packed into the compressed workspace file.
                                                        # The specified files and directories must be specified relative to the _JOB_WORKSPACE_DIR variable.
                                                        # I.e. if the workspace directory looks like this:
                                                        # /path/to/workspaces/Jobname_20220825_102532_96/all.log
                                                        # /path/to/workspaces/Jobname_20220825_102532_96/progress.log
                                                        # /path/to/workspaces/Jobname_20220825_102532_96/software/file1.txt
                                                        # /path/to/workspaces/Jobname_20220825_102532_96/software/file2.txt
                                                        # /path/to/workspaces/Jobname_20220825_102532_96/tools/file3.txt
                                                        # /path/to/workspaces/Jobname_20220825_102532_96/tools/file4.txt
                                                        # And you want all files from the software directory and one file from the tools directory not
                                                        # being packed then the @exclude_at_packing_workspace_directory needs to contain:
                                                        #   @exclude_at_packing_workspace_directory = ( "software", "tools/file3.txt" )
                                                        # NOTE: There are no trailing / for the directory name.
my $job_failure_email_addresses   = "email_reports/job_failure_email_addresses";
my $job_failure_email_content     = "email_reports/job_failure_email_content";
my $job_finished_email_addresses  = "email_reports/job_finished_email_addresses";
my $job_finished_email_content    = "email_reports/job_finished_email_content";
my $job_success_email_addresses   = "email_reports/job_success_email_addresses";
my $job_success_email_content     = "email_reports/job_success_email_content";
my $job_duration = 0;
my $jobname = "";
our %JOB_PARAMS;                # Global variable read and written by Playlist module files
our @JOB_STATUS;                # Global variable read and written by Playlist module files
my @job_summary = ();
my %job_variables;
my $job_workspace_dir = "";
my $list_playlists = 0;
my $logfile = "";
my $message;
my @messages = ();
my @network_config_options;
our %NETWORK_CONFIG_PARAMS;     # Global variable read by Playlist module files
our $network_file = "";
my $only_playlist_help = 0;
our %optional_job_variables;
   set_optional_job_variables();
my $package_dir = dirname(dirname(dirname abs_path $0));
   $JOB_PARAMS{'_PACKAGE_DIR'} = $package_dir;
my @pids = ();
my $playlist_rc = 0;
my $playlist_file = "";
my $playlist_name = "";
my $playlist_function = "";
our %playlist_variables;
my %process_info;
my $program_rc = 0;
my $rc = 0;
my $show_help = 0;
my $show_progress = 0;
my $start_time = 0;
my $stop_time = 0;
my $valid;
my $var_name;
my $verbose = 0;
my $workspace_dir = "";
my @xterm_options = ();
my $xterm_pid = 0;

# Record start time
$start_time = time;

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "all-playlists"             => \$all_playlists,
    "a|always-ask=s"            => \@always_ask,
    "b|background-tasks=s"      => \$background_tasks_file,
    "c|color=s"                 => \$color_scheme,
    "e|always-ignore-error=s"   => \@always_ignore_error,
    "g|show-progress"           => \$show_progress,
    "h|help"                    => \$show_help,
    "i|list-playlists"          => \$list_playlists,
    "j|jobname=s"               => \$jobname,
    "l|log-file=s"              => \$logfile,
    "n|network-file=s"          => \$network_file,
    "o|network-config-option=s" => \@network_config_options,
    "only-playlist-help"        => \$only_playlist_help,
    "p|playlist=s"              => \$playlist_name,
    "r|rerun-job=s"             => \$job_workspace_dir,
    "s|always-skip=s"           => \@always_skip,
    "t|xterm-option=s"          => \@xterm_options,
    "v|variable=s"              => \%job_variables,
    "w|workspace=s"             => \$workspace_dir,
    "x|always-execute=s"        => \@always_execute,
);

if ($playlist_name =~ /^.+\/(\S+)\.pm$/) {
    # The user specified the complete path, remove it and the extension
    $playlist_name = $1;
}

if ($only_playlist_help) {
    # Force showing help
    $show_help = 1;
}

if ($show_help) {
    if ($playlist_name eq "") {
        usage();
    } else {
        if ($only_playlist_help == 0) {
            # Display help information common for this script and all playlists
            usage();
        }

        # Display help information from the playlist, if existing
        usage_playlist();
    }
    exit 0;
}

if ($list_playlists) {
    list_playlist_files();
    exit 0;
}

if ($jobname eq "" && $playlist_name ne "") {
    $jobname = "Playlist_$playlist_name";
} elsif ($jobname eq "") {
    print "ERROR: You must specify a job name with parameter --jobname\n";
    exit 1;
}
$JOB_PARAMS{'JOBNAME'} = $jobname;

# Set umask to allow all files created from this process to be created
# with rwx rights for user, group and others i.e. 0777.
# It will bypass the default umask set by the user which is normally 0022
# meaning that write bit 'w' is turned off for group and others.
umask 0;

if ($workspace_dir eq "") {
    $workspace_dir = "/tmp/workspaces";
}

unless (-d $workspace_dir) {
    `mkdir -m 777 -p $workspace_dir`;
    if ($?) {
        print "ERROR: Unable to create main workspace directory '$workspace_dir'\n";
        exit 1;
    }
}
$workspace_dir = abs_path $workspace_dir;

# Set some default job parameters that might be overwritten by the code below
# that checks if it's a re-run of an earlier interrupted job.
$JOB_PARAMS{'JOBSTATUS'} = "NONE";
$JOB_PARAMS{'JOBTYPE'} = "NONE";

# Check if the user want to start a new job or continue with an already started job
if (-d "$job_workspace_dir") {
    # The user want to rerun a previously started job to pickup where it left off
    $job_workspace_dir = abs_path $job_workspace_dir;

    # If job_parameters.conf file exist, read the content and update %::JOB_PARAMS hash
    if (-f "$job_workspace_dir/job_parameters.conf") {
        General::Playlist_Operations::job_parameters_read("$job_workspace_dir/job_parameters.conf", \%JOB_PARAMS);
    }

    # Recover original start time to get a proper job duration calculation
    if (exists $JOB_PARAMS{'_JOB_STARTTIME'}) {
        if ($JOB_PARAMS{'_JOB_STARTTIME'} =~ /^(\d+)$/) {
            $start_time = $1;
        }
    }

    # If job_status.temp file exist from an interrupted job, read the content and
    # update the @JOB_STATUS array
    if (-f "$job_workspace_dir/job_status.temp") {
        General::File_Operations::read_file(
            {
                "filename"          => "$job_workspace_dir/job_status.temp",
                "output-ref"        => \@JOB_STATUS,
            }
        );
        unlink "$job_workspace_dir/job_status.temp";
    }

    # If job_step_task_summary.temp file exist from an interrupted job, read the content and
    # update the @General::Playlist_Operations::step_task_summary array
    if (-f "$job_workspace_dir/job_step_task_summary.temp") {
        General::File_Operations::read_file(
            {
                "filename"          => "$job_workspace_dir/job_step_task_summary.temp",
                "output-ref"        => \@General::Playlist_Operations::step_task_summary,
            }
        );
        unlink "$job_workspace_dir/job_step_task_summary.temp";
    }

    # If applied_workarounds.temp file exist from an interrupted job, read the content and
    # update the @General::Playlist_Operations::workaround_tasks array
    if (-f "$job_workspace_dir/workaround_tasks.temp") {
        General::File_Operations::read_file(
            {
                "filename"          => "$job_workspace_dir/workaround_tasks.temp",
                "output-ref"        => \@General::Playlist_Operations::workaround_tasks,
            }
        );
        unlink "$job_workspace_dir/workaround_tasks.temp";
    }

} else {
    # A new job is started, generate a job workspace directory
    $job_workspace_dir = sprintf "%s/%s_%s_%02d", $workspace_dir, $jobname, $current_timestamp, rand(100);
    `mkdir -m 777 -p "$job_workspace_dir"`;
    if ($?) {
        print "ERROR: Unable to create job workspace directory '$job_workspace_dir'\n";
        exit 1;
    }
}

# Set the temporary file used for OS_Operations
General::OS_Operations::set_temporary_output_file("$job_workspace_dir/command.log");
General::OS_Operations::set_temporary_stderr_file("$job_workspace_dir/stderr.log");

# Set the name of the input pipe that can be used to feed information into the playlist execution
General::OS_Operations::set_input_named_pipe_file("$job_workspace_dir/input.pipe");

# Set some environment variables if not already set
$ENV{'HOSTNAME'} = exists $ENV{'HOSTNAME'} ? $ENV{'HOSTNAME'} : (split /\n/, `hostname --fqdn 2>/dev/null`)[0];
if (exists $ENV{'HOSTNAME'} && defined $ENV{'HOSTNAME'}) {
    if ($ENV{'HOSTNAME'} eq "") {
        $ENV{'HOSTNAME'} = "UNKNOWN";
    }
} else {
    $ENV{'HOSTNAME'} = "UNKNOWN";
}
$JOB_PARAMS{'DAFT_HOSTNAME'} = (split /\n/, `hostname --fqdn 2>/dev/null`)[0];
if (exists $JOB_PARAMS{'DAFT_HOSTNAME'} && defined $JOB_PARAMS{'DAFT_HOSTNAME'}) {
    if ($JOB_PARAMS{'DAFT_HOSTNAME'} eq "") {
        $JOB_PARAMS{'DAFT_HOSTNAME'} = $ENV{'HOSTNAME'};
    }
} else {
    $JOB_PARAMS{'DAFT_HOSTNAME'} = $ENV{'HOSTNAME'};
}

# Create a lock file to keep track of if a job is using a playlist package
$rc = General::File_Operations::write_file(
    {
        "filename"            => "$job_workspace_dir.lock",
        "output-ref"          => [
            "STARTTIME=$current_timestamp",
            "HOSTNAME=$JOB_PARAMS{'DAFT_HOSTNAME'}",
            "USER=$ENV{'USER'}",
            "PID=$$",
            "SCRIPT=$script_name",
            "PROGRESS_FILE=$job_workspace_dir/progress.log",
            "PLAYLIST_NAME=$playlist_name",
        ],
        "eol-char"            => "\n",
        "append-file"         => 1,
        "file-access-mode"    => "666",
    }
);
if ($rc != 0) {
    print "ERROR: Unable to create job lock file '$job_workspace_dir.lock'\n";
    exit 1;
}

# Create a state file to keep track of tasks executed and their execution status.
# If it already exist it will read in the state of previously executed tasks.
# Create an always-do file to keep track of the tasks that should always be executes, skipped etc.
$rc = General::State_Machine::initialize( { "always-do-filename" => "$job_workspace_dir/always_do.info", "state-filename" => "$job_workspace_dir/state_report.info" } );
if ($rc != 0) {
    # Remove the lock file
    unlink "$job_workspace_dir.lock";
    exit 1;
}

# Check color scheme parameter
$color_scheme = lc $color_scheme;
if ($color_scheme =~ /^no$/i) {
    General::Logging::log_color("disable");
} elsif ($color_scheme =~ /^dark$/i) {
    General::Logging::log_color("enable","dark");
} elsif ($color_scheme =~ /^bright$/i) {
    General::Logging::log_color("enable","bright");
} elsif ($color_scheme =~ /^cyan$/i) {
    General::Logging::log_color("enable","cyan");
} elsif ($color_scheme =~ /^html$/i) {
    General::Logging::log_color("enable","html");
} else {
    print "ERROR: Incorrect --color value given, only 'html', 'no', 'dark', 'bright' or 'cyan' allowed\n";
    # Remove the lock file
    unlink "$job_workspace_dir.lock";
    exit 1;
}

# Set job parameter values which should overwrite earlier set values
$JOB_PARAMS{'_PACKAGE_DIR'} = $package_dir;
$JOB_PARAMS{'_JOB_WORKSPACE_DIR'} = $job_workspace_dir;
$JOB_PARAMS{'_JOB_PARAMS_FILE'} = "$job_workspace_dir/job_parameters.conf";
$JOB_PARAMS{'_JOB_STARTTIME'} = "$start_time";
$JOB_PARAMS{'_JOB_STATE_LOG'} = "$job_workspace_dir/state_report.info";
$JOB_PARAMS{'_USER_HOSTNAME'} = $JOB_PARAMS{'DAFT_HOSTNAME'};
$JOB_PARAMS{'_USER_USERNAME'} = $ENV{'USER'};
$JOB_PARAMS{'_USER_PROCESS_ID'} = $$;
$JOB_PARAMS{'_PLAYLIST_NAME'} = $playlist_name;

# Set job parameters from command line
for my $key (sort keys %job_variables) {
    $JOB_PARAMS{$key} = $job_variables{$key};
    push @messages, "$key=$job_variables{$key}";
}

if (@network_config_options) {
    push @messages, "";
    push @messages, "Network Configuration File Options:";
    push @messages, "-----------------------------------";
    for (sort @network_config_options) {
        push @messages, $_;
    }
}

if ($network_file) {
    $network_file = abs_path $network_file;
}

# Check if certain tasks should always be interrupted, executed or skipped
if (@always_ask) {
    General::State_Machine::always_interrupt_task( @always_ask );
}
if (@always_execute) {
    add_main_tasks_if_needed(\@always_execute);
    General::State_Machine::always_execute_task( @always_execute );
}
if (@always_skip) {
    General::State_Machine::always_skip_task( @always_skip );
}

# Check if error return code for certain tasks should be ignored and alwas return success
if (@always_ignore_error) {
    General::State_Machine::always_ignore_error_in_task( @always_ignore_error );
}

# If package.config file exist in the used DAFT package, read the content and update %::JOB_PARAMS hash
if (-f "$package_dir/package.config") {
    General::Playlist_Operations::job_parameters_read("$package_dir/package.config", \%JOB_PARAMS);
}

# Optional parameter that can be set to "no" in case e.g. on target execution is done by
# an Upgrade workflow and no output can be provided on STDOUT.
if (exists $JOB_PARAMS{'CONSOLE_OUTPUT_AVAILABLE'}) {
    if ($JOB_PARAMS{'CONSOLE_OUTPUT_AVAILABLE'} eq "yes") {
        $General::OS_Operations::console_output_available = 1;
    } else {
        $General::OS_Operations::console_output_available = 0;
    }
} else {
    $General::OS_Operations::console_output_available = 1;
}

# Check some special job variables
if (exists $JOB_PARAMS{'SPECIAL_IDENTIFIER'}) {
    $JOB_PARAMS{'SPECIAL_IDENTIFIER'} =~ s/\s/_/g;
}

# Create special sub directories for the job related configuration and log files
`mkdir -m 777 -p "$job_workspace_dir/configurationfiles"`;
if ($?) {
    print "ERROR: Unable to create job configurationfiles directory '$job_workspace_dir/configurationfiles'\n";
    # Remove the lock file
    unlink "$job_workspace_dir.lock";
    exit 1;
}
`mkdir -m 777 -p "$job_workspace_dir/logfiles"`;
if ($?) {
    print "ERROR: Unable to create job logfiles directory '$job_workspace_dir/logfiles'\n";
    # Remove the lock file
    unlink "$job_workspace_dir.lock";
    exit 1;
}
`mkdir -m 777 -p "$job_workspace_dir/troubleshooting_logs"`;
if ($?) {
    print "ERROR: Unable to create job troubleshooting_logs directory '$job_workspace_dir/troubleshooting_logs'\n";
    # Remove the lock file
    unlink "$job_workspace_dir.lock";
    exit 1;
}
$JOB_PARAMS{'_JOB_CONFIG_DIR'} = "$job_workspace_dir/configurationfiles";
$JOB_PARAMS{'_JOB_LOG_DIR'}    = "$job_workspace_dir/logfiles";
$JOB_PARAMS{'_JOB_TROUBLESHOOTING_LOG_DIR'} = "$job_workspace_dir/troubleshooting_logs";

# Remove any previously created job status file
`rm -f ${job_workspace_dir}_JOBSTATUS.*`;

# **************
# *            *
# * Main logic *
# *            *
# **************

# Start logging to file
start_logfile();

# Print startup message
print_startup_message();

# Install special handler for SIGTERM which is called
# when the Perl process is killed
$SIG{TERM} = \&cleanup_at_SIGTERM;

# Ignore child process completion status
# The below line seems to cause issues when executing any external command
# it seems to always return exit code 1 for each system call.
# $SIG{CHLD} = 'IGNORE';

if ($show_progress) {
    open_xterm_showing_command_log();
}

# Import the main playlist file
if ($playlist_name) {
    $playlist_file = "$package_dir/perl/lib/Playlist/$playlist_name.pm";
    if (-f $playlist_file) {
        eval "require Playlist::$playlist_name";
        if ($@) {
            General::Logging::log_user_error_message("Unable to 'require Playlist::$playlist_name', Error Message:\n$@\n");
            $playlist_rc = 1;
            goto EXIT_PROGRAM;
        }
        eval "import Playlist::$playlist_name";
        if ($@) {
            General::Logging::log_user_error_message("Unable to 'import Playlist::$playlist_name', Error Message:\n$@\n");
            $playlist_rc = 1;
            goto EXIT_PROGRAM;
        }
    } else {
        General::Logging::log_user_error_message("Unable to find the playlist file '$playlist_file'");
        $playlist_rc = 1;
        goto EXIT_PROGRAM;
    }
    $playlist_function = "Playlist::${playlist_name}::main";
} else {
    General::Logging::log_user_error_message("You must specify a playlist name with parameter --playlist");
    $playlist_rc = 1;
    goto EXIT_PROGRAM;
}

# Check if playlist and optional global job variables was specified
check_playlist_variables();

# Parse network configuration file
parse_network_config_file();

# Write %::JOB_PARAMS hash to file
General::Playlist_Operations::job_parameters_write($JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%JOB_PARAMS);

# Check if we should start any background tasks
if ($background_tasks_file ne "") {
    start_background_tasks();
}

#
# Call main playlist file
#
$playlist_rc = General::Playlist_Operations::execute_task( { "task-ref" => \&$playlist_function } );

EXIT_PROGRAM:

# Set the return code of the program which is usually 0 or 1
$program_rc = $playlist_rc;

# Restore normal SIGTERM handler
$SIG{TERM} = 'DEFAULT';

# Set script exit code
if ($program_rc == 0) {
    # Success
    if ($JOB_PARAMS{'JOBSTATUS'} =~ /^FAILED$/i) {
        # Special case, the job failed but the return code from the playlist
        # execution was 0, but we mark the execution as failed
        $program_rc = 1;
    }
} else {
    # Failure
    $program_rc = 1;
}

# Stop log file, write summary file and remove .lock file
cleanup_at_end();

# Send Email if wanted
send_email($program_rc);

# Pack workspace directory if wanted
if ($program_rc == 0) {
    remove_temporary_files();
    pack_job_workspace_directory($program_rc);
} else {
    pack_job_workspace_directory($playlist_rc);
}

General::Logging::log_progress("Exit Code=$program_rc, JOBSTATUS=$JOB_PARAMS{'JOBSTATUS'}\n");

# Write message to system log
`logger -t DAFT 'PID=$$ Stopped by user $ENV{USER} on host $JOB_PARAMS{DAFT_HOSTNAME} with exit code: $program_rc'`;

# Exit the script
exit $program_rc;

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------------------------------------
#
sub add_main_tasks_if_needed {
    my $array_ref = shift;

    my $change_cnt = 0;
    my $main_pattern = "";
    my $pattern = "";
    my $playlist_no = "";
    my %used_patterns;

    # Look for duplications and add ::main parts as needed.
    for (@$array_ref) {
        $pattern = $_;
        #$pattern = "P007S02T\d\d";
        if (exists $used_patterns{$pattern}) {
            $used_patterns{$pattern}++;
            $change_cnt++;
        } else {
            $used_patterns{$pattern} = 1;
        }

        # Find the main function name that needs to be enabled as well
        # Look for patterns like this:
        #   P001S02T03
        #   P001S02T..
        #   P001S02T0.
        #   P001S02T\d\d    (Currently does not work)
        #   P001S02T\d+     (Currently does not work)
        #   P001S02
        #
        #   P001S..T..
        #   P001S..T01
        #   P001S0.T01
        #   P001S0.T0.
        #   P001S\d\dT01    (Currently does not work)
        #   P001S\d+T01     (Currently does not work)
        #
        #   P001S0.
        #
        #   P001
        #
        #                    ------- Look for the playlist number e.g. 001
        #                             ---------------- Look for the Step number e.g. S01, S0., S.., S\d\d or S\d+
        #                           ---------------------------------------- Look for the Step number as above or no Step and no Task
        #                                               ---------------- Look for the Task number e.g. T01, T0., T.., T\d\d or T\d+
        #                                             -------------------- Look for the Task number as above or no task number at all
        #
        # It's strange that the lines that is marked with (Currently does not work)
        # actually work when run with the same pattern on the command line with command:
        # perl -e '$a="P007S02T\d+";if ($a=~/^.*P(\d{3})(S([\.\\d+0-9]{2})(T([\.\\d+0-9]{2})|)|)$/){print "$1\n";}else{print "NOT FOUND\n";}'
        if ($pattern =~ /^.*P(\d{3})(S([\.\\d+0-9]{2})(T([\.\\d+0-9]{2})|)|)$/) {
            $playlist_no = $1;
            $main_pattern = "Playlist::$playlist_no.+::main";
            if (exists $used_patterns{$main_pattern}) {
                $used_patterns{$main_pattern}++;
            } else {
                $used_patterns{$main_pattern} = 1;
                $change_cnt++;
            }
        }
    }

    if ($change_cnt > 0) {
        # Something has been added or duplications found so we need to update
        # the array reference.
        @$array_ref = ();
        for my $key (sort keys %used_patterns) {
            push @$array_ref, $key;
        }
    }
}

# -----------------------------------------------------------------------------
#
sub check_playlist_variables {

    if ((exists $JOB_PARAMS{'CONSOLE_INPUT_AVAILABLE'} && $JOB_PARAMS{'CONSOLE_INPUT_AVAILABLE'} eq "no") || (exists $JOB_PARAMS{'INPUT_AVAILABLE'} && $JOB_PARAMS{'INPUT_AVAILABLE'} eq "no")) {
        $General::OS_Operations::console_input_available = 0;
        $General::OS_Operations::input_available = 0;
    }

    # Display summary of job parameters
    my $playlist_variables_function = "Playlist::${playlist_name}::usage_return_playlist_variables";
    no strict;
    if (defined &$playlist_variables_function) {
        %playlist_variables = (%optional_job_variables, &$playlist_variables_function);
    } else {
        %playlist_variables = %optional_job_variables;
    }
    use strict;

    if (General::Playlist_Operations::parse_playlist_variables(\%playlist_variables, \%::JOB_PARAMS) != 0) {
        General::Logging::log_user_error_message("Failed to parse the playlist variables");
        $playlist_rc = General::Playlist_Operations::RC_GRACEFUL_EXIT;
        goto EXIT_PROGRAM;
    }

    # Optional parameter that can be set to "no" in case e.g. on target execution is done by
    # an Upgrade workflow and no answers can be provided on STDIN by interactive prompting,
    # but answers can be provided on the named pipe file 'input.pipe'.
    if ($JOB_PARAMS{'CONSOLE_INPUT_AVAILABLE'} eq "yes") {
        $General::OS_Operations::console_input_available = 1;
    } else {
        $General::OS_Operations::console_input_available = 0;
    }

    # Optional parameter that can be set to "no" in case e.g. on target execution is done by
    # an Upgrade workflow and no output can be provided on STDOUT.
    if ($JOB_PARAMS{'CONSOLE_OUTPUT_AVAILABLE'} eq "yes") {
        $General::OS_Operations::console_output_available = 1;
    } else {
        $General::OS_Operations::console_output_available = 0;
    }

    # Optional parameter that can be set to "no" in case e.g. Jenkins is running the playlist
    # and no answers can be provided by interactive prompting on neither STDIN nor via named
    # pipe file 'input.pipe'.
    if ($JOB_PARAMS{'INPUT_AVAILABLE'} eq "yes") {
        $General::OS_Operations::input_available = 1;
    } else {
        $General::OS_Operations::input_available = 0;
    }

    # Optional parameter that can be set to "no" in case the developer want to have a more
    # detailed list of answers that can be provided when a task fails.
    # By default only a simplified list is displayed.
    if ($JOB_PARAMS{'SIMPLE_SELECT_MENU'} eq "yes") {
        $General::Playlist_Operations::simple_select_menu = 1;
    } else {
        $General::Playlist_Operations::simple_select_menu = 0;
    }
}

# -----------------------------------------------------------------------------
#
sub cleanup_at_end {
    # Stop any ongoing background tasks
    stop_background_tasks();

    # Write %::JOB_PARAMS hash to file
    General::Playlist_Operations::job_parameters_write($JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%JOB_PARAMS);

    # To make it possible to cleanup the workspace directory by anybody,
    # make it and all sub folders and files write enabled.
    make_workspace_write_enabled_for_all();

    # Record stop time and calculate job duration
    $stop_time = time;
    $job_duration = $stop_time - $start_time;

    # Stop logging of output to file and write message to user
    stop_logfile_write_message();

    # Write a summary file
    write_summary_file();

    # Remove input.pipe file
    unlink "$job_workspace_dir/input.pipe";

    # Remove the lock file
    unlink "$job_workspace_dir.lock";

    # Remove empty sub directories unless the playlist exit with RC_GRACEFUL_EXIT
    # which might indicate that the user might want to rerun the same job and we
    # don't want to remove empty directories in this case.
    if ($playlist_rc !=  General::Playlist_Operations::RC_GRACEFUL_EXIT) {
        `find $JOB_PARAMS{'_JOB_WORKSPACE_DIR'} -type d -empty -delete`;
    }

    # Remove any temporary files created by the playlists
    General::File_Operations::tempfile_delete_all();

    # Write an empty file indicating the job status to make it quick to see
    # if a terminated job was successful or not
    `touch ${job_workspace_dir}_JOBSTATUS.$JOB_PARAMS{'JOBSTATUS'}`;
}

# -----------------------------------------------------------------------------
#
sub cleanup_at_SIGINT {
    print "\n";
    General::Logging::log_user_error_message("The execution was interrupted by a SIGINT (CTRL-C)");

    # Write output of current executing command, if any, to log file
    General::OS_Operations::write_current_temporary_output_to_log("Interrupted by a SIGINT");

    if ($General::Playlist_Operations::last_task_start_time != 0) {
        my $stop_time = General::OS_Operations::detailed_time();
        my $total_time = sprintf "%f", $stop_time - $General::Playlist_Operations::last_task_start_time;
        General::Logging::log_execution_times($stop_time,'stop','task',$General::Playlist_Operations::last_task_name);

        # Store summary information
        push @General::Playlist_Operations::step_task_summary, sprintf "%13.6f seconds  %3d%-20s  %-4s  %s", $total_time, 130, " (SIGINT)", "Task", $General::Playlist_Operations::last_task_name;
    }

    # Mark playlist execution as failed
    $program_rc = 130;
    $JOB_PARAMS{"JOBSTATUS"} = "FAILED";

    # Stop log file, write summary file and remove .lock file
    cleanup_at_end();

    # Write message to system log
    `logger -t DAFT 'PID=$$ Stopped by user $ENV{USER} on host $JOB_PARAMS{DAFT_HOSTNAME} with exit code: 130 (SIGINT or CTRL-C)'`;

    # Enable echo to screen again in case this was disabled e.g. during password entry
    `stty echo`;

    General::Logging::log_progress("Exit Code=130, JOBSTATUS=$JOB_PARAMS{'JOBSTATUS'}\n");
    exit 130;
}

# -----------------------------------------------------------------------------
#
sub cleanup_at_SIGQUIT {
    print "\n";
    General::Logging::log_user_error_message("The execution was interrupted by a SIGQUIT (CTRL-\\)");

    # Write output of current executing command, if any, to log file
    General::OS_Operations::write_current_temporary_output_to_log("Interrupted by a SIGQUIT");

    if ($General::Playlist_Operations::last_task_start_time != 0) {
        my $stop_time = General::OS_Operations::detailed_time();
        my $total_time = sprintf "%f", $stop_time - $General::Playlist_Operations::last_task_start_time;
        General::Logging::log_execution_times($stop_time,'stop','task',$General::Playlist_Operations::last_task_name);

        # Store summary information
        push @General::Playlist_Operations::step_task_summary, sprintf "%13.6f seconds  %3d%-20s  %-4s  %s", $total_time, 131, " (SIGQUIT)", "Task", $General::Playlist_Operations::last_task_name;
    }

    # Mark playlist execution as failed
    $program_rc = 131;
    $JOB_PARAMS{"JOBSTATUS"} = "FAILED";

    # Stop log file, write summary file and remove .lock file
    cleanup_at_end();

    # Write message to system log
    `logger -t DAFT 'PID=$$ Stopped by user $ENV{USER} on host $JOB_PARAMS{DAFT_HOSTNAME} with exit code: 131 (SIGQUIT or CTRL-\\)'`;

    # Enable echo to screen again in case this was disabled e.g. during password entry
    `stty echo`;

    General::Logging::log_progress("Exit Code=131, JOBSTATUS=$JOB_PARAMS{'JOBSTATUS'}\n");
    exit 131;
}

# -----------------------------------------------------------------------------
#
sub cleanup_at_SIGTERM {
    print "\n";
    General::Logging::log_user_error_message("The execution was interrupted by a SIGTERM");

    # Write output of current executing command, if any, to log file
    General::OS_Operations::write_current_temporary_output_to_log("Interrupted by a SIGTERM");

    if ($General::Playlist_Operations::last_task_start_time != 0) {
        my $stop_time = General::OS_Operations::detailed_time();
        my $total_time = sprintf "%f", $stop_time - $General::Playlist_Operations::last_task_start_time;
        General::Logging::log_execution_times($stop_time,'stop','task',$General::Playlist_Operations::last_task_name);

        # Store summary information
        push @General::Playlist_Operations::step_task_summary, sprintf "%13.6f seconds  %3d%-20s  %-4s  %s", $total_time, 143, " (SIGTERM)", "Task", $General::Playlist_Operations::last_task_name;
    }

    # Mark playlist execution as failed
    $program_rc = 143;
    if ($JOB_PARAMS{"JOBSTATUS"} eq "NONE") {
        $JOB_PARAMS{"JOBSTATUS"} = "FAILED";
    }

    # Stop log file, write summary file and remove .lock file
    cleanup_at_end();

    # Write message to system log
    `logger -t DAFT 'PID=$$ Stopped by user $ENV{USER} on host $JOB_PARAMS{DAFT_HOSTNAME} with exit code: 143 (SIGTERM or process killed)'`;

    # Send Email if wanted
    send_email(1);

    # Enable echo to screen again in case this was disabled e.g. during password entry
    `stty echo`;

    General::Logging::log_progress("Exit Code=143, JOBSTATUS=$JOB_PARAMS{'JOBSTATUS'}\n");
    exit 143;
}

# -----------------------------------------------------------------------------
#
sub list_playlist_files {
    my $already_printed_000_099 = 0;
    my $already_printed_100_799 = 0;
    my $already_printed_800_899 = 0;
    my $already_printed_900_999 = 0;
    my @files = `ls -1 $package_dir/perl/lib/Playlist/*`;
    my $name;
    my $number;

    if ($?) {
        print "ERROR: Unable to list playlist files\n";
        exit 1;
    }
    print "\nAvailable Playlist files:\n";
    for (@files) {
        if (/^.+\/((\d\d\d)_\w+)\.pm$/) {
            $name = $1;
            $number = $2;

            if ($number >= 0 && $number <= 99) {
                unless ($already_printed_000_099) {
                    $already_printed_000_099 = 1;
                    print "\n  Playlists for Customer Use:\n";
                    print "  ---------------------------\n";
                }
            } elsif ($number >= 100 && $number <= 799) {
                unless ($already_printed_100_799) {
                    $already_printed_100_799 = 1;
                    print "\n  Playlists for Ericsson Internal Use:\n";
                    print "  ------------------------------------\n";
                }
            } elsif ($number >= 800 && $number <= 899) {
                unless ($already_printed_800_899) {
                    $already_printed_800_899 = 1;
                    print "\n  Playlists that might be Dangerous to Use:\n";
                    print "  -----------------------------------------\n";
                }
            } elsif ($number >= 900 && $number <= 999) {
                if ($all_playlists == 0) {
                    # Only print main playlists not the ones starting with 900-999
                    # which are sub playlists.
                    next;
                }
                unless ($already_printed_900_999) {
                    $already_printed_900_999 = 1;
                    print "\n  Playlists that should never be called directly:\n";
                    print "  -----------------------------------------------\n";
                }
            }
            print "  $name\n";
        }
    }
    print "\n";
}

# -----------------------------------------------------------------------------
#
sub make_workspace_write_enabled_for_all {
    General::Logging::log_user_message("Making workspace directory write enabled for all users\n");
    General::OS_Operations::send_command(
        {
            "command"       => "chmod -R a+rw $job_workspace_dir",
            "hide-output"   => 1,
        }
    );
}

# -----------------------------------------------------------------------------
#
sub open_xterm_showing_command_log {
    my $command = "xterm ";
    my $xterm_log_file = "$job_workspace_dir/xterm.log";

    if (@xterm_options) {
        for my $option (@xterm_options) {
            $command .= "$option ";
        }
    } else {
        # Set some default options
        if ($color_scheme =~ /^dark$/i) {
            $command .= "-geometry 250x20+0+0 -bg black -sl 3000 -sb -si -sk -rightbar ";
        } else {
            $command .= "-geometry 250x20+0+0 -bg white -sl 3000 -sb -si -sk -rightbar ";
        }
    }
    $command .= "-e 'tail -f $job_workspace_dir/command.log'";

    General::File_Operations::write_file(
        {
            "filename"            => "$job_workspace_dir/command.log",
            "output-ref"          => ["$command\n"],
            "append-file"         => 1,
        }
    );

    $xterm_pid = General::OS_Operations::background_process_run($command, $xterm_log_file);
    if ($xterm_pid > 0) {
        `logger -t DAFT 'PID=$$ Started in Background "$command" with process id $xterm_pid' 2> /dev/null`;
        General::Logging::log_user_message("Background PID: $xterm_pid\nLog file:  $xterm_log_file\n");
        push @JOB_STATUS, "(?) Background Command 0 (xterm) started with PID $xterm_pid";
    } else {
        `logger -t DAFT_SEQ 'PID=$$ Failed to start in Background "$command"' 2> /dev/null`;
        General::Logging::log_user_error_message("Background Command Failed.\nLog file:  $xterm_log_file\n");
        push @JOB_STATUS, "(x) Background Command 0 (xterm) failed to start, but error ignored";
    }
}

# -----------------------------------------------------------------------------
#
sub pack_job_workspace_directory {
    my $input_rc = shift;

    my $command;
    my $current_dir = Cwd::cwd();
    my $exclude_paths = "";
    my $job_directory_file;
    my $job_directory_name;
    my $message;
    my $rc = 0;
    my @result;
    my $create_file = "no";

    if ($input_rc == 0 || $input_rc ==  General::Playlist_Operations::RC_GRACEFUL_EXIT) {
        $create_file = exists $JOB_PARAMS{'CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS'} ? "$JOB_PARAMS{'CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS'}" : "no";
    } else {
        # Other type of failures
        $create_file = exists $JOB_PARAMS{'CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE'} ? "$JOB_PARAMS{'CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE'}" : "yes";
    }

    # Check if we should skip creating the packed file
    if ($create_file eq "no") {
        return;
    }

    $message = "Creating a compressed file of the job workspace directory, please wait...\n";
    print $message if ($General::OS_Operations::console_output_available == 1);
    General::Logging::log_progress($message);

    # Pack the workspace directory as one file
    $job_directory_name = basename $job_workspace_dir;
    $job_directory_file = "$workspace_dir/$job_directory_name.tar.bz2";

    chdir($job_workspace_dir);

    # Check if the playlists want to have certain files and directories excluded
    # from the packed workspace file.
    if (@exclude_at_packing_workspace_directory) {
        for my $path (@exclude_at_packing_workspace_directory) {
            if ($path =~ /\//) {
                # Looks like a file path or directory path
                if (-f "$path") {
                    # The file exist, mark it to be excluded
                    $exclude_paths .= "--exclude=$path ";
                } elsif (-d "$path") {
                    # The directory exist, mark it to be excluded
                    $exclude_paths .= "--exclude=$path ";
                } else {
                    # The file or directory does not seem to exist, so skip this path
                    next;
                }
            } else {
                # Does not look like a file or directory path, maybe it's a list of file extensions e.g.
                # {*.png,*.mp3,*.wav,.git,node_modules}
                # Just to be safe add it to the exclude list.
                $exclude_paths .= "--exclude=$path ";
            }
        }
    }
    if ($JOB_PARAMS{'INCLUDE_TROUBLESHOOTING_LOGS'} eq "no") {
        # We should not include the troubleshooting_logs directory in the packed
        # workspaces file.
        $exclude_paths .= "--exclude=troubleshooting_logs ";
    }

    $command = "tar cjf $job_directory_file ${exclude_paths}*";
    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );

    if ($rc == 0) {
        $message = "\nCreated a compressed file with content of the workspace directory that can be\n";
        $message .= "used to troubleshoot why a job failed, or to save logs from a successful job.\n";
        $message .= "Please share the following file when needed:\n\n";
        $message .= "$job_directory_file\n\n";

        # Make file read and write enabled for all users
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "chmod 666 $job_directory_file",
                "hide-output"   => 1,
            }
        );
        if ($rc != 0) {
            $message .= "Failed to change access rights for the file\n\n";
        }
    } else {
        $message = "\n$command\n " . (join "\n", @result) . "\n\nFailed to create a packed file for workspace directory '$job_directory_name'\n\n";
    }

    if ($JOB_PARAMS{'PACK_TROUBLESHOOTING_LOGS_SEPARATELY'} eq "yes" && -d "troubleshooting_logs") {
        $job_directory_file = "$workspace_dir/${job_directory_name}_troubleshooting_logs.tar.bz2";
        $command = "tar cjf $job_directory_file troubleshooting_logs";
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        if ($rc == 0) {
            $message .= "\nCreated a compressed file with content of the workspace troubleshooting_logs\n";
            $message .= "sub-directory that can be used to troubleshoot why a job failed.\n";
            $message .= "Please share the following file when needed:\n\n";
            $message .= "$job_directory_file\n\n";

            # Make file read and write enabled for all users
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "chmod 666 $job_directory_file",
                    "hide-output"   => 1,
                }
            );
            if ($rc != 0) {
                $message .= "Failed to change access rights for the file\n\n";
            }
        } else {
            $message = "\n$command\n " . (join "\n", @result) . "\n\nFailed to create a packed file for workspace directory '$job_directory_name'\n\n";
        }
    }

    chdir($current_dir);

    print $message if ($General::OS_Operations::console_output_available == 1);
    General::Logging::log_progress($message);

    return;
}

# -----------------------------------------------------------------------------
#
sub parse_network_config_file {
    my @discarded = ();

    if ($network_file) {
        # Check if the user specified any network configuration options that might impact the validity
        # check of the network config file parameters, which is currently only done by the specific
        # parse_network_config_parameters subroutine in the playlist.
        if (@network_config_options) {
            $JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} = "";
            for my $option (@network_config_options) {
                $JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} .= "$option|";
            }
            $JOB_PARAMS{'NETWORK_CONFIG_OPTIONS'} =~ s/\|$//;
        }

        # Keep a copy of the used network configuration file
        `cp -fp $network_file $JOB_PARAMS{'_JOB_CONFIG_DIR'} 2>/dev/null`;

        # Check if the playlist have a validator function called validate_network_config_parameter
        # if so then pass this to the parse_network_configuration_file function.
        my $parse_playlist_function = "Playlist::${playlist_name}::parse_network_config_parameters";
        my $validate_playlist_function = "Playlist::${playlist_name}::validate_network_config_parameter";
        if (defined &$parse_playlist_function) {
            # The playlist has a function that will be called by the playlist when it's time to parse all network
            # configuration file parameters so we shall not do anything here.
            # This functionality is used when the playlist is not yet ready to parse the network configuration
            # file because it might first need to fetch some other information e.g. from the node before the
            # network configuration file is read.
            return;
        } elsif (defined &$validate_playlist_function) {
            # The playlist has a function to validate the network configuration file values before they are stored
            # in the NETWORK_CONFIG_PARAMS hash, so pass this reference.
            $rc = General::File_Operations::parse_network_configuration_file(
                {
                    "filename"              => "$network_file",
                    "output-ref"            => \%NETWORK_CONFIG_PARAMS,
                    "validator-ref"         => \&$validate_playlist_function,
                    "discarded-ref"         => \@discarded,
                    "hide-error-messages"   => 1,
                }
            );

            # Now replace any 'REPLACEMEWITH_xxxx' values with the value from the 'xxxx' parameter.
            General::File_Operations::replace_network_configuration_variable_placeholders(
                {
                    "input-ref"             => \%::NETWORK_CONFIG_PARAMS,
                    "discarded-ref"         => \@discarded,
                }
            );

            # Now update the %::JOB_PARAMS hash with values from %::NETWORK_CONFIG_PARAMS hash.
            General::File_Operations::update_job_variables_with_network_configuration_variables(
                {
                    "input-ref"             => \%::NETWORK_CONFIG_PARAMS,
                    "output-ref"            => \%::JOB_PARAMS,
                    "ignore-attributes"     => ["application_type", "cnf_type", "description", "playlist", "reuse_deployed_helm_value", "valid_from_releases", "valid_releases"],
                    "discarded-ref"         => \@discarded,
                }
            );

            # Since we have updated the job variables then we need to write %::JOB_PARAMS hash to file
            General::Playlist_Operations::job_parameters_write($::JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%::JOB_PARAMS);
        } else {
            $rc = General::File_Operations::parse_network_configuration_file(
                {
                    "filename"              => "$network_file",
                    "output-ref"            => \%NETWORK_CONFIG_PARAMS,
                    "discarded-ref"         => \@discarded,
                    "hide-error-messages"   => 1,
                }
            );

            # Now replace any 'REPLACEMEWITH_xxxx' values with the value from the 'xxxx' parameter.
            General::File_Operations::replace_network_configuration_variable_placeholders(
                {
                    "input-ref"             => \%::NETWORK_CONFIG_PARAMS,
                    "discarded-ref"         => \@discarded,
                }
            );

            # Now update the %::JOB_PARAMS hash with values from %::NETWORK_CONFIG_PARAMS hash.
            General::File_Operations::update_job_variables_with_network_configuration_variables(
                {
                    "input-ref"             => \%::NETWORK_CONFIG_PARAMS,
                    "output-ref"            => \%::JOB_PARAMS,
                    "ignore-attributes"     => ["application_type", "cnf_type", "description", "playlist", "reuse_deployed_helm_value", "valid_from_releases", "valid_releases"],
                    "discarded-ref"         => \@discarded,
                }
            );

            # Since we have updated the job variables then we need to write %::JOB_PARAMS hash to file
            General::Playlist_Operations::job_parameters_write($::JOB_PARAMS{'_JOB_PARAMS_FILE'}, \%::JOB_PARAMS);
        }
        if ($rc) {
            General::Logging::log_user_error_message("Unable to read network configuration file");
            $playlist_rc = 1;
            goto EXIT_PROGRAM;
        }

        General::File_Operations::update_job_variables_with_network_configuration_variables(
            {
                "input-ref"     => \%NETWORK_CONFIG_PARAMS,
                "output-ref"    => \%JOB_PARAMS,
            }
        );

        if (@discarded) {
            General::Logging::log_write("The following entries in the network config file was discarded:\n" . (join "\n", @discarded) . "\n");
        }
    }
}

# -----------------------------------------------------------------------------
#
sub print_startup_message {
    General::Logging::log_write("Script started with parameters:\nperl $script_name $script_params\n");

    # Write message to system log
    `logger -t DAFT 'PID=$$ Started by user $ENV{USER} on host $JOB_PARAMS{DAFT_HOSTNAME} using command: perl $script_name $script_params'`;

    if (-f "$JOB_PARAMS{'_PACKAGE_DIR'}/package.config") {
        my @lines;
        General::File_Operations::read_file(
            {
                "filename"            => "$JOB_PARAMS{'_PACKAGE_DIR'}/package.config",
                "output-ref"          => \@lines,
                "hide-error-messages" => 1,
                "ignore-empty-lines"  => 1,
            }
        );
        if (@lines) {
            General::Logging::log_write("DAFT package information:\n" . (join "\n", @lines) . "\n");
        }
    }

    # Find the used version of DAFT
    my $daft_revision = "";
    my $daft_date = "";
    my @temp;
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "head -10 $script_name",
            "hide-output"   => 1,
            "return-output" => \@temp,
        }
    );
    if ($rc == 0) {
        #  Revision : 2.77
        #  Date     : 2024-05-14 17:10:15
        for (@temp) {
            if (/^#\s*Revision\s*:\s*(.+)/) {
                $daft_revision = $1;
            } elsif (/^#\s*Date\s*:\s*(.+)/) {
                $daft_date = $1;
            }
        }
    }

    # Print some work related information
    General::Message_Generation::print_message_box(
        {
            "header-text" => "Job Information",
            "max-length"  => 0,
            "messages"    => [
                "Job started on ".scalar localtime($start_time),
                "",
                "               Job Name : $jobname",
                "          Playlist Name : $playlist_name",
                "      Package Directory : $package_dir",
                "    Network Config File : $network_file",
                "Job Workspace Directory : $job_workspace_dir",
                "            Command Log : $job_workspace_dir/command.log",
                "       Detailed Job Log : $logfile",
                "",
                "    Job Started by User : $ENV{'USER'}",
                "    Job Started on Host : $JOB_PARAMS{'DAFT_HOSTNAME'}",
                "        Job Proccess Id : $$",
                "       DAFT Information : $daft_revision ($daft_date)",
                "",
                "Job Parameters:",
                "---------------",
                @messages,
                "",
                "Job Signals:",
                "------------",
                "Interrupt current task and possibly return error:        kill -s SIGINT  $$ (or press CTRL-C)",
                "Interrupt current task and return error:                 kill -s SIGQUIT $$ (or press CTRL-\\)",
                "Interrupt current task and cause playlist fallback:      kill -s SIGUSR1 $$",
                "Interrupt current task and cause playlist graceful exit: kill -s SIGUSR2 $$",
            ],
            "return-output" => \$message,
        }
    );
    General::Logging::log_user_message($message);
}

# -----------------------------------------------------------------------------
#
sub remove_temporary_files {
    # Remove any previously created temporary files
    `rm -f ${job_workspace_dir}/*.temp`;
}

# -----------------------------------------------------------------------------
#
sub seconds_to_hms {
    my $duration = shift;
    my $line = "";

    if ($duration > 0) {
        my $hours = sprintf "%d", $duration / 3600;
        my $minutes = sprintf "%d", ($duration % 3600) / 60;
        my $seconds = sprintf "%d", ($duration % 3600) % 60;
        $line = "";

        $line .= sprintf " $hours %s,", $hours > 1 ? "hours" : "hour" if ($hours > 0);
        $line .= sprintf " $minutes %s,", $minutes > 1 ? "minutes" : "minute" if ($minutes > 0);
        $line .= sprintf " $seconds %s,", $seconds > 1 ? "seconds" : "second" if ($seconds > 0);
        $line =~ s/^\s+//;
        $line =~ s/,$//;
    } else {
        $line = "<1 second";
    }

    return $line;
}

# -----------------------------------------------------------------------------
#
sub send_email {
    my $return_code = shift;    # 0=success, 1=failure

    my $attachment_file = "";
    my $current_dir = Cwd::cwd();
    my $email_address = exists $JOB_PARAMS{'EMAIL_ADDRESS_FINISHED'} ? "$JOB_PARAMS{'EMAIL_ADDRESS_FINISHED'}" : "";
    my $email_attachment = "";
    my $email_content = "";
    my $email_skip = exists $JOB_PARAMS{'EMAIL_SKIP'} ? lc($JOB_PARAMS{'EMAIL_SKIP'}) : "no";
    my $email_subject = "";
    my $filename;
    my $job_directory_name;
    my $job_host = exists $JOB_PARAMS{'DAFT_HOSTNAME'} ? $JOB_PARAMS{'DAFT_HOSTNAME'} : "Unknown";
    my $job_starttime = scalar localtime($start_time);
    my $job_type = "";
    my $job_user = exists $ENV{'USER'} ? $ENV{'USER'} : "Unknown";
    my $message;
    my $rc = 0;

    # Check if we should skip Email sending
    if ($email_skip eq "yes") {
        return;
    }

    # Check Job type
    if ($JOB_PARAMS{'JOBTYPE'} ne "NONE") {
        $job_type = "$JOB_PARAMS{'JOBTYPE'} ";
    }

    # Check if special email address files exist
    $filename = "$package_dir/../$job_finished_email_addresses";
    if (-f "$filename") {
        if (open INF, "$filename") {
            for (<INF>) {
                s/[\r\n]//g;
                $email_address .= "$_,";
            }
            close INF;
        } else {
            print "Unable to open file '$filename'\n";
        }
    }
    if ($return_code == 0) {
        # Successful job
        $email_address .= exists $JOB_PARAMS{'EMAIL_ADDRESS_SUCCESS'} ? "$JOB_PARAMS{'EMAIL_ADDRESS_SUCCESS'}," : "";
        $filename = "$package_dir/../$job_success_email_addresses";
        if (-f "$filename") {
            if (open INF, "$filename") {
                for (<INF>) {
                    s/[\r\n]//g;
                    $email_address .= "$_,";
                }
                close INF;
            } else {
                print "Unable to open file '$filename'\n";
            }
        }
        $email_content = "$package_dir/templates/$job_success_email_content";
        $email_subject = "DAFT ${job_type}job successful: Started on $job_starttime by $job_user on host $job_host";
    } else {
        # Failed job
        $email_address .= exists $JOB_PARAMS{'EMAIL_ADDRESS_FAILURE'} ? "$JOB_PARAMS{'EMAIL_ADDRESS_FAILURE'}," : "";
        $filename = "$package_dir/../$job_failure_email_addresses";
        if (-f "$filename") {
            if (open INF, "$filename") {
                for (<INF>) {
                    s/[\r\n]//g;
                    $email_address .= "$_,";
                }
                close INF;
            } else {
                print "Unable to open file '$filename'\n";
            }
        }
        $email_content = "$package_dir/templates/$job_failure_email_content";
        $email_subject = "DAFT ${job_type}job failed: Started on $job_starttime by $job_user on host $job_host";
    }

    # Cleanup the email address
    $email_address =~ s/,+/,/g;     # Replace multiple ,, with one ,
    $email_address =~ s/,$//g;      # Remove , at the end
    $email_address =~ s/\s+//g;     # Remove all white spaces

    if ($email_address ne "") {
        # Remove duplicate email addresses
        my %email_addresses;
        my @email_addresses = split /,/, $email_address;
        for (@email_addresses) {
            $email_addresses{$_}++;
        }
        $email_address = "";
        for my $key (sort keys %email_addresses) {
            $email_address .= "$key,";
        }
        $email_address =~ s/,$//g;      # Remove , at the end

        # Pack the workspace directory as an attachment
        $job_directory_name = basename $job_workspace_dir;
        $attachment_file = "$workspace_dir/$job_directory_name.tar.bz2";
        chdir($job_workspace_dir);
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "tar cjf $attachment_file --exclude *.tar.gz *",
                "hide-output"   => 1,
            }
        );
        chdir($current_dir);

        # Check if we should add an attachment
        if (-f "$attachment_file") {
            if (-s "$attachment_file" < 3000000) {
                $email_attachment = "-a $attachment_file";
            } else {
                # Big file, don't send it
                if (-f "$job_workspace_dir/progress.log") {
                    $email_attachment = "-a $job_workspace_dir/progress.log";
                } elsif (-f "$job_workspace_dir/summary.txt") {
                    $email_attachment = "-a $job_workspace_dir/summary.txt";
                } else {
                    # No attachment
                    $email_attachment = "";
                }
            }
        } elsif (-f "$job_workspace_dir/progress.log") {
            $email_attachment = "-a $job_workspace_dir/progress.log";
        } elsif (-f "$job_workspace_dir/summary.txt") {
            $email_attachment = "-a $job_workspace_dir/summary.txt";
        } else {
            # No attachment
            $email_attachment = "";
        }

        # Check if the content file exist
        unless (-f "$email_content") {
            $email_content = "/dev/null";    # No content included
        }

        # Send Email
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "mail $email_attachment -s \"$email_subject\" $email_address < $email_content",
                "hide-output"   => 1,
            }
        );
        if ($rc == 0) {
            $message = "\nEmail sent to interested users\n\n";
        } else {
            $message = "\nFailed to send email to interested users\n\n";
        }
        print $message if ($General::OS_Operations::console_output_available == 1);
        General::Logging::log_progress($message);

        # Remove the created attachment file
        if (-f "$attachment_file") {
            unlink "$attachment_file";
        }
    }
    return;
}

# -----------------------------------------------------------------------------
#
sub set_optional_job_variables {
    %optional_job_variables = (
        # ---------------------------------------------------------------------
        'CONSOLE_INPUT_AVAILABLE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls how the playlist will collect information from
the user when needed.

If value is 'yes' then STDIN and named pipes (file input.pipe) are
supported for input to the playlist. This is also the default value
if the variable is not specified.

If value is 'no' then STDIN is not supported but named pipes can still
be used for input to the playlist.

If specified then it's normally combined like this:
CONSOLE_INPUT_AVAILABLE=no
CONSOLE_OUTPUT_AVAILABLE=no
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CONSOLE_OUTPUT_AVAILABLE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls how the playlist will display information to the
the user.

If value is 'yes' then STDOUT is supported for output by the playlist,
i.e. everything is output to the terminal window where DAFT was started.
This is also the default value if the variable is not specified.

If value is 'no' then STDOUT is not supported and little or no output
will be written to the screen. Some output might be written to the
system log.

If specified then it's normally combined like this:
CONSOLE_INPUT_AVAILABLE=no
CONSOLE_OUTPUT_AVAILABLE=no
  or
INPUT_AVAILABLE=no
CONSOLE_OUTPUT_AVAILABLE=no
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if a packed file with content of the job workspace
directory should be created when a job fails.
This packed file can be useful when troubleshooting why the job failed.

If value is 'yes' then when the playlist finish with an error then a compressed
file will be created in the workspaces directory which can then be shared with
the people that investigate why the job failed.
This is also the default value if the variable is not specified.

If value is 'no' then no packed file will be created.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if a packed file with content of the job workspace
directory should be created when a job is successful.
This packed file can be useful to collect a single file with all the logs from
the job.

If value is 'yes' then when the playlist finish with an success then a compressed
file will be created in the workspaces directory which can then be shared with
the people that needs the file.
The default value is 'no' if the variable is not specified.

If value is 'no' then no packed file will be created.
EOF
            'validity_mask' => '(yes|no)',
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

This parameter can e.g. be used by a playlist to not execute some commands if
this debug function is active (=yes) for example by prefixing such commands with
the "echo " statement.
It's up to the Playlist to check and apply special handling if this parameter is
set.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'EMAIL_ADDRESS_FAILURE' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified then an email will be sent if a job fails to the specified
email address(es).
Multiple addresses are specified by separating them with a comma,
for example:
EMAIL_ADDRESS_FAILURE=<email1\@work.com[,email2\@work.com..]>

The default value if not specified is to not send any email.

Also a file can be used to enter the addresses (comma separated)
located at:
../../../$job_failure_email_addresses
If this file is existing and it contains one or more email
addresses then an email will be sent to those addresses as
well as the addresses specified with the EMAIL_ADDRESS_FAILURE
variable.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'EMAIL_ADDRESS_FINISHED' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified then an email will be sent if a job finish to the specified
email address(es), regardless if it was successful or failed.
Multiple addresses are specified by separating them with a comma,
for example:
EMAIL_ADDRESS_FINISHED=<email1\@work.com[,email2\@work.com..]>

The default value if not specified is to not send any email.

Also a file can be used to enter the addresses (comma separated)
located at:
../../../$job_finished_email_addresses
If this file is existing and it contains one or more email
addresses then an email will be sent to those addresses as
well as the addresses specified with the EMAIL_ADDRESS_FINISHED
variable.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'EMAIL_ADDRESS_SUCCESS' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
If specified then an email will be sent if a job was successful to the
specified email address(es).
Multiple addresses are specified by separating them with a comma,
for example:
EMAIL_ADDRESS_SUCCESS=<email1\@work.com[,email2\@work.com..]>

The default value if not specified is to not send any email.

Also a file can be used to enter the addresses (comma separated)
located at:
../../../$job_success_email_addresses
If this file is existing and it contains one or more email
addresses then an email will be sent to those addresses as
well as the addresses specified with the EMAIL_ADDRESS_SUCCESS
variable.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'EMAIL_SKIP' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if an email will be sent at the end of a job or
not and it depends on the precence of the variables EMAIL_ADDRESS_FAILURE,
EMAIL_ADDRESS_FAILURE or EMAIL_ADDRESS_SUCCESS.

If specified and set to 'yes' then no email will be sent regardless if
any of the EMAIL_ADDRESS_FAILURE, EMAIL_ADDRESS_FINISHED or
EMAIL_ADDRESS_SUCCESS variables has been specified.

If value is 'no' then an email is sent if one of the other variables
EMAIL_ADDRESS_FAILURE, EMAIL_ADDRESS_FINISHED or EMAIL_ADDRESS_SUCCESS
is set and it matches the status of the job.
This is also the default value if the variable is not specified.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'EXPECT_DEFAULT_TIMEOUT' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "1800",
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

By default the value is set to 30 minutes (1800 seconds) which should be long
enough for most commands.
EOF
            'validity_mask' => '\d+',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'INCLUDE_TROUBLESHOOTING_LOGS' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the troubleshooting_logs directory should be included
in the packed workspaces directory file.

If value is 'yes' then any files inside the troubleshooting_logs/ directory will
be included in the packed workspace directory file.
This might be useful to have everything in the same packed file, but it can result
in a very big file.

If value is 'no' then any files inside the troubleshooting_logs/ directory will
NOT be included in the packed workspace directory file.
This might be useful to reduce the size of the packed file.
This is also the default value if the variable is not specified.

See also PACK_TROUBLESHOOTING_LOGS_SEPARATELY.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'INPUT_AVAILABLE' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls how the playlist will collect information from
the user when needed.

If value is 'yes' then STDIN (if CONSOLE_INPUT_AVAILABLE=yes) and named
pipes (file input.pipe) are supported for input to the playlist.
This is also the default value if the variable is not specified.

If value is 'no' then neither STDIN nor named pipes are supported for
input to the playlist, in such case all questions for input will be
answered by the undefined value which usually result in fallback of
the playlist.

If specified then it's normally combined like this:
INPUT_AVAILABLE=no
CONSOLE_OUTPUT_AVAILABLE=no
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'PACK_TROUBLESHOOTING_LOGS_SEPARATELY' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "no",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the troubleshooting_logs directory should be packed
into a separate file.

If value is 'yes' then any files inside the troubleshooting_logs/ directory will
be packed into a special packed workspace directory file that is separate from
the normal packed workspace directory file.
This might be useful to have all troubleshooting related logs in a separate file
when INCLUDE_TROUBLESHOOTING_LOGS=no.

If value is 'no' then any files inside the troubleshooting_logs/ directory will
included in the packed workspace directory file when INCLUDE_TROUBLESHOOTING_LOGS=yes
or not included at all in a packed workspace directory file when
INCLUDE_TROUBLESHOOTING_LOGS=no.
This is also the default value if the variable is not specified.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'RENAME_ADP_LOG_TO_INCLUDE_JOBNAME' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls if the ADP logs inside the troubleshooting_logs directory
should be renamed from the default 'logs_eiffelesc_YYYY-MM-DD-HH-MM-SS.tgz' name
to have the job name prepended to the file name.

If value is 'yes' then any collected ADP logs (using collect_ADP_logs.sh script)
will be renamed so they also include the name of the job at the beginning of
the file name.
This might be useful if many ADP logs are stored in the same directory so it's
easier to see which job the file belongs to.
This is also the default value if the variable is not specified.

If value is 'no' then any collected ADP logs (using collect_ADP_logs.sh script)
will keep their original names.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SIMPLE_SELECT_MENU' => {
            'case_check'    => "lowercase", # <lowercase|no|uppercase>
            'default_value' => "yes",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "yesno",     # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter controls what information is displayed to the user
when an error is detected and the playlist needs feedback from the user.

If value is 'yes' then when the playlist stops due to an error it will
show a simplified menu of valid options that the user can select from.
This is also the default value if the variable is not specified.

If value is 'no' then when the playlist stops due to an error it will
show a complete menu of valid options that the user can select from
which includes any kind of return code (positive or negative values)
and is probably only useful for the playlist designers during
development.
EOF
            'validity_mask' => '(yes|no)',
            'value'         => "",
        },

        # ---------------------------------------------------------------------
        'SPECIAL_IDENTIFIER' => {
            'case_check'    => "no",        # <lowercase|no|uppercase>
            'default_value' => "",
            'mandatory'     => "no",        # <yes|no>
            'type'          => "string",    # <directory|file|integer|string|yesno>
#        1         2         3         4         5         6         7         8
#2345678901234567890123456789012345678901234567890123456789012345678901234567890
            'usage'         => <<EOF,
This parameter can be set by the user to a special string which can be
used by some playlists.
What the string is used for depends on the playlist, but one example
can be to include this string in a temporary file name that is created
when performing docker repository manipulations to stop other playlists
to do the same operations until the first playlist is done.

Any white space in the string is converted to underscore characters '_'.
EOF
            'validity_mask' => '.+',
            'value'         => "",
        },
    );
}

# -----------------------------------------------------------------------------
#
sub start_logfile {
# Start logging of output to file
    if ($logfile) {
        $logfile = abs_path dirname($logfile) . "/" . basename($logfile);
        General::Logging::log_enable("$logfile");
    } else {
        $logfile = "$job_workspace_dir/all.log";
        General::Logging::log_enable("$logfile");
    }
}

# -----------------------------------------------------------------------------
#
sub stop_background_tasks {
    my $child;
    my $message = "";
    my $process_info_fetched = 0;
    my $process_exist_cnt = 0;
    my $rc;
    my $send_sigabrt = 1;   # Change this to a 0 when no SIGABRT signal should be sent
                            # to the background process.
    my $wait_time = 5;      # Change this to a value that is higher than any value used
                            # in the background process for checking how often to check
                            # for the SIGABRT signal.

    # -----------------------------------------------------------------------------
    #
    sub fetch_process_info {
        my $parent_pid = shift;
        my $child_pid;

        # Fetch process information
        @{$process_info{$parent_pid}{'details'}} = General::OS_Operations::process_information($parent_pid);
        $process_info{$parent_pid}{'status'} = "";
        $process_info{$parent_pid}{'parent'} = "";

        # Fetch child processes
        @{$process_info{$parent_pid}{'children'}} = General::OS_Operations::process_return_children($parent_pid);
        if (@{$process_info{$parent_pid}{'children'}}) {
            push @children, @{$process_info{$parent_pid}{'children'}};

            # Fetch children process information recursiv
            for $child_pid (@{$process_info{$parent_pid}{'children'}}) {
                fetch_process_info($child_pid);
                $process_info{$child_pid}{'parent'} = $parent_pid;
            }
        }
    }

    General::Logging::log_user_message("Checking if there are any background processes that needs to be stopped");
    # Fetch any started background processes
    @pids = General::OS_Operations::background_processes();

    if (scalar @pids > 0 && $send_sigabrt > 0) {
        # First try to gracefully close all the background processes by sending signal SIGABRT
        $process_info_fetched = 1;
        for my $pid (@pids) {
            # Fetch process information
            fetch_process_info($pid);

            General::Logging::log_user_message("Sending background process PID=$pid the signal SIGABRT");
            $rc = General::OS_Operations::background_process_send_signal($pid, "SIGABRT");
            if ($rc == 1) {
                # The process existed
                $process_exist_cnt++;
            }
        }

        if ($wait_time > 0) {
            General::Logging::log_user_message("Waiting $wait_time seconds for all the background processes to gracefully close down");
            # Wait a while before checking if log collection process id's are still running
            General::OS_Operations::sleep_with_progress(
                {
                    "allow-interrupt"   => 1,
                    "progress-interval" => 1,
                    "progress-message"  => 1,
                    "seconds"           => $wait_time,
                    "use-logging"       => 1,
                }
            );
        }
    }

    # Next kill any started background processes
    for my $pid (@pids) {
        # Fetch process information
        if ($process_info_fetched == 0) {
            # Only fetch process information if not already done above when
            # sending the SIGABRT signal.
            fetch_process_info($pid);
        }

        # Kill the process, wait max 10 seconds for it and it's children to die
        General::Logging::log_user_message("Killing background process PID=$pid");
        $rc = General::OS_Operations::background_process_kill($pid, 10);
        if ($rc == 1) {
            $process_info{$pid}{'status'} = "Killed";
        } else {
            $process_info{$pid}{'status'} = "Not existing/Not Killed";
        }
    }

    # Next check any child processes and kill them if needed
    for $child (@children) {
        if ($process_info{$child}{'status'} ne "") {
            # This process has already been processed
            next;
        }

        # Kill the child process, wait max 10 seconds for it and it's children to die
        General::Logging::log_user_message("Killing child process PID=$child for parent PID=$process_info{$child}{'parent'}");
        $rc = General::OS_Operations::process_kill($child, 10);
        if ($rc == 1) {
            $process_info{$child}{'status'} = "Killed";
        } else {
            $process_info{$child}{'status'} = "Not existing/Not Killed";
        }
    }

    # Print the result, if any
    if (%process_info) {
        $message = "The following background processes were killed.\n\n";
        $message .= sprintf "%-10s  %-25s  %7s\n", "Process Id", "Status", "Details";
        $message .= sprintf "%-10s  %-25s  %7s\n", "-"x10, "-"x25, "-"x7;
        for my $pid (sort keys %process_info) {
            $message .= sprintf "%10s  %-25s  ", $pid,
                                                 $process_info{$pid}{'status'};
            for (@{$process_info{$pid}{'details'}}) {
                $message .= "$_\n" . " "x39;
            }
            if (@{$process_info{$pid}{'children'}}) {
                # This process has children
                $message .= "Children: " . (join ", ", @{$process_info{$pid}{'children'}}) . "\n";
            } else {
                # No children
                $message .= "No children\n";
            }
        }
    }

    if ($message ne "") {
        General::Logging::log_user_message($message);
    } else {
        General::Logging::log_user_message("There are no background processes");
    }
}

# -----------------------------------------------------------------------------
#
sub start_background_tasks {
    my $command;
    my @commands = ();
    my $current_epoch = time();
    my $failed_cnt = 0;
    my $heading_printed = 0;
    my $message = "";
    my $pid;

    $rc = General::File_Operations::read_file(
        {
            "filename"            => $background_tasks_file,
            "output-ref"          => \@commands,
            "ignore-empty-lines"  => 1,
            "ignore-pattern"      => '^\s*#',
        }
    );
    if ($rc != 0) {
        General::Logging::log_user_error_message("Unable to read $background_tasks_file file");
        $playlist_rc = 1;
        goto EXIT_PROGRAM;
    }

    for $command (@commands) {
        # Replace some place holder variables
        $command =~ s/\@\@_CURRENT_EPOCH\@\@/$current_epoch/g;
        $command =~ s/\@\@_JOB_CONFIG_DIR\@\@/$JOB_PARAMS{'_JOB_CONFIG_DIR'}/g;
        $command =~ s/\@\@_JOB_LOG_DIR\@\@/$JOB_PARAMS{'_JOB_LOG_DIR'}/g;
        $command =~ s/\@\@_JOB_TROUBLESHOOTING_LOG_DIR\@\@/$JOB_PARAMS{'_JOB_TROUBLESHOOTING_LOG_DIR'}/g;
        $command =~ s/\@\@_JOB_WORKSPACE_DIR\@\@/$JOB_PARAMS{'_JOB_WORKSPACE_DIR'}/g;
        $command =~ s/\@\@_PACKAGE_DIR\@\@/$JOB_PARAMS{'_PACKAGE_DIR'}/g;

        if ($heading_printed == 0) {
            $message = "Background Processes started\n\n";
            $message .= sprintf "%-10s  %-7s\n", "Process Id", "Command";
            $message .= sprintf "%10s  %s\n", "-"x10, "-"x7;
            $heading_printed = 1;
        }

        # Start the background process
        $pid = General::OS_Operations::background_process_run($command);
        $::JOB_PARAMS{'DIAMETER_LOG_COLLECTION_PID'} = $pid;
        if ($pid > 0) {
            $message .= sprintf "%-10s  %-7s\n", $pid, $command;
        } else {
            $message .= sprintf "%-10s  %-7s\n", "Failed", $command;
            $failed_cnt++;
        }
    }
    if ($failed_cnt == 0) {
        General::Logging::log_user_message($message);
    } else {
        General::Logging::log_user_warning_message("$message\nSome background tasks failed but we only classify this as a warning\n");
    }
}

# -----------------------------------------------------------------------------
#
sub stop_logfile_write_message {
    my $jobstatus = "";
    my $jobtype = "";
    my @messages = ();
    my $show_as = SHOW_AS_FAILED;

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
        "Job Summary is written to file:",
        "$job_workspace_dir/summary.txt",
        "",
        "Detailed logging is written to file:",
        "$logfile",
        "",
    );

    #
    # Add overall Job status
    #
    if ($JOB_PARAMS{"JOBTYPE"} eq "NONE") {
        $jobtype = "";
    } else {
        $jobtype = $::JOB_PARAMS{"JOBTYPE"} . " ";
    }

    if ($JOB_PARAMS{"JOBSTATUS"} eq "NONE") {
        $jobstatus = "";
        if ($program_rc == 0) {
            $show_as = SHOW_AS_SUCCESS;
        } else {
            $show_as = SHOW_AS_FAILED;
        }
    } else {
        $jobstatus = ", Status is " . $JOB_PARAMS{"JOBSTATUS"};
        if ($JOB_PARAMS{"JOBSTATUS"} eq "FAILED") {
            $show_as = SHOW_AS_FAILED;
        } elsif ($JOB_PARAMS{"JOBSTATUS"} eq "ONGOING") {
            $show_as = SHOW_AS_WARNING;
        } else {
            if ($program_rc == 0) {
                $show_as = SHOW_AS_SUCCESS;
                if ($General::Playlist_Operations::ignored_errors > 0) {
                    # Some tasks failed but user choose to ignore them
                    $jobstatus .= " ($General::Playlist_Operations::ignored_errors errors ignored)";
                }
            } else {
                $show_as = SHOW_AS_FAILED;
            }
        }
    }

    push @messages, (
        "Job Status",
        "==========",
        "${jobtype}Job Finished$jobstatus",
        "",
    );

    #
    # Add summary of job tasks performed
    #
    if (@JOB_STATUS) {
        push @messages, @JOB_STATUS;
    }

    # Add an empty line if needed
    if ($messages[-1] ne "") {
        push @messages, "";
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

    # Preserve the job_parameters.conf file in case of a rerun of the job
    General::Logging::log_user_message("Updating job_parameters.conf file");
    General::Playlist_Operations::job_parameters_write("$job_workspace_dir/job_parameters.conf", \%JOB_PARAMS);

    # Also preserve the @General::Playlist_Operations::workaround_tasks array in case of a rerun of the job
    General::Logging::log_user_message("Updating workaround tasks file");
    General::File_Operations::write_file(
        {
            "filename"          => "$job_workspace_dir/workaround_tasks.temp",
            "output-ref"        => \@General::Playlist_Operations::workaround_tasks,
            "file-access-mode"  => 666,
        }
    );

    # Also preserve the @::JOB_STATUS array in case of a rerun of the job
    General::Logging::log_user_message("Updating job status file");
    General::File_Operations::write_file(
        {
            "filename"          => "$job_workspace_dir/job_status.temp",
            "output-ref"        => \@JOB_STATUS,
            "file-access-mode"  => 666,
        }
    );

    # And also preserve @General::Playlist_Operations::step_task_summary array
    General::Logging::log_user_message("Updating job task summary file");
    General::File_Operations::write_file(
        {
            "filename"          => "$job_workspace_dir/job_step_task_summary.temp",
            "output-ref"        => \@General::Playlist_Operations::step_task_summary,
            "file-access-mode"  => 666,
        }
    );

    if ($logfile) {
        General::Logging::log_user_message("Removing CR characters from the detailed log file\n");
        `sed -i 's/\\r//g' $logfile`;

        if ($show_as == SHOW_AS_SUCCESS) {
            General::Logging::log_user_message($message);
        } elsif ($show_as == SHOW_AS_WARNING) {
            General::Logging::log_user_warning_message($message);
        } else {
            General::Logging::log_user_error_message($message);
        }

        General::Logging::log_disable();
    } else {
        if ($show_as == SHOW_AS_SUCCESS) {
            General::Logging::log_user_message($message);
        } elsif ($show_as == SHOW_AS_WARNING) {
            General::Logging::log_user_warning_message($message);
        } else {
            General::Logging::log_user_error_message($message);
        }
    }
}

# -----------------------------------------------------------------------------
#
sub usage {
    my $description;
    my $script_dir = abs_path (dirname dirname $0) . "/lib/Playlist" ;

    print <<EOF;

Description:
============

This script is used for executing Playlist modules stored under the directory:
$script_dir

It is a replacement of the Software Upgrade Framework (SUF).

A Playlist is a specially written Perl module that contains a set of
instructions of what should be done, for example Installing or Upgrading an SC
oe DSC node. A Playlist can also call other sub-playlists which makes it possible
to create re-usable modules.
The Playlists can also contain "fallback" logic which is called when a Playlist
fails and can be used to e.g. cleanup after itself.

A Playlist consist of the "main()" function which calls one or more Steps which
is are normal Perl subroutines. The Step subroutine calls one or more Tasks which
are also normal Perl subroutines.

This script creates a number of files under the workspace directory:

    - all.log
      This file contains a detailed log of all executed commands by the Playlist
      and printouts shown to the user on the screen.

    - command.log
      This file contains output of the currently executing command and it might
      sometimes be useful to monitor this file to see what is currently
      happening for a long running task.
      To follow the detailed job execution use the following command:
      tail -f /path/to/workspace/command.log

    - execution_times.log
      This file contains the information when each step and task was started
      and stopped.
      The information will be used to calculate the total execution times
      for each step and task which will be included in the 'summary.txt' file
      mentioned below.

    - input.pipe
      This file contains an alternative way to communicate with the Playlist
      execution instead of using the Terminal that started the Playlist
      execution. The file is created whenever the Playlist needs feedback from
      the user and is removed when input is no longer needed.
      To interact with the Playlist and provide an answer, a user can e.g.
      pass in the value "0" by giving the following command:
      echo 0 > /path/to/workspace/input.pipe

    - job_parameters.conf
      This file contains all job related parameters and their values.

    - progress.log
      This file contains the same information that is also shown on the Terminal
      screen which can be useful when multiple users want to observe what is
      happening with a Playlist execution.
      To follow the job execution use the following command:
      tail -f /path/to/workspace/progress.log

    - state_change_history.log
      This file contains a list of the state changes performed by the playlist
      execution and is only useful for trouble shooting purposes.

    - state_report.info
      This file contains information about the current progress of a job and
      can be used to restart a job from the last point where it might have been
      interrupted.

    - summary.txt
      This file contains a summary of all steps and tasks that was executed
      during the playlist execution.
      Information found in this file is for example the time when a job was
      started and finished and how long time it took to execute each step and
      task.


Syntax:
=======

$0 [<OPTIONAL>] <MANDATORY>

<MANDATORY> are all the following parameters:

  -p <name>             | --playlist <name>

<OPTIONAL> are one or more of the following parameters:

                          --all-playlists
  -a <perl regexp>      | --always-ask <perl regexp>
  -b <filename>         | --background-tasks <filename>
  -c <dark|bright|cyan> | --color <dark|bright|cyan>
  -e <perl regexp>      | --always-ignore-error <perl regexp>
  -g                    | --show-progress
  -h                    | --help
  -i                    | --list-playlists
  -j <name>             | --jobname <name>
  -l <filename>         | --log-file <filename>
  -n <filename>         | --network-file <filename>
  -o <string>           | --network-config-option <string>
                          --only-playlist-help
  -r <dirpath>          | --rerun-job <dirpath>
  -s <perl regexp>      | --always-skip <perl regexp>
  -t <string>           | --xterm-option <string>
  -v <name>=<value>     | --variable <name>=<value>
  -w <dirpath>          | --workspace <dirpath>
  -x <perl regexp>      | --always-execute <perl regexp>

where:

  --all-playlists
  ---------------
  When used together with the -i or --list-playlists parameter it will print
  a list of all playlists including sub-playlists that should not be called
  directly.
  The default, if not specified, is to only print main playlists that should
  be called.


  -a <perl regex>
  --always-ask <perl regex>
  -------------------------
  If specified then all task names matching the <perl regexp> in the executed
  playlists will always be interrupted and the user asked what should happen
  next.
  This parameter can be specified multiple times if more than one regular
  expression is needed.
  For example:
    -a 'Playlist::001_Installation_or_Upgrade_DSC::Create_Configuration_Files_Directory_P001S01T01'
        or shorter
    -a 'P001S01T01'


  -b <filename>
  --background-tasks <filename>
  -----------------------------
  If specified then it should be a file name that contains a list of commands that
  will be started in the background before calling the playlist.
  This can e.g. be used for collecting logs in the background.
  After finished playlist execution the background tasks will be stopped.
  This file name should contain one or more commands that will be started as
  background processes and it should contain the command name including all parameters
  for the command.
  The file can use place holder variables that will be replaced by actual values
  from the running playlist and they can currently be the following:

    \@\@_CURRENT_EPOCH\@\@
    \@\@_JOB_CONFIG_DIR\@\@
    \@\@_JOB_LOG_DIR\@\@
    \@\@_JOB_TROUBLESHOOTING_LOG_DIR\@\@
    \@\@_JOB_WORKSPACE_DIR\@\@
    \@\@_PACKAGE_DIR\@\@

  The file can also contain empty lines and comment lines that starts with the
  hash character (#) as the first non-whitespace character.
  For example:
    # Start collection of log collection in realtime
    \@\@_PACKAGE_DIR\@\@/perl/bin/trouble_shooting_collect_pod_logs.pl -n eiffelesc --no-tty -o \@\@_JOB_TROUBLESHOOTING_LOG_DIR\@\@/GSSUPP-12317/pod_logs -p 'eric-bsf-diameter-.+,dsl' -p 'eric-stm-diameter-.+,dsl'

  NOTE: The background tasks that are started MUST NOT create any output to
  STDOUT or STDERR or ask for any input from STDIN or this will mess up the
  output/input from the running playlist. Either the running task should not
  print anything at all, or the command should contain redirection of STDOUT
  and STDERR to files. For example:
    # Discard all STDOUT and STDERR output
    /path/to/command >> /dev/null 2>&1

    # Send all STDOUT and STDERR output to a log file
    /path/to/command >> stdout_stderr.log 2>&1

    # Send all STDOUT and STDERR output to separate log files
    /path/to/command >> stdout.log 2>> stderr.log


  -c <no|dark|bright|cyan>
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

  The default, if this parameter is not given, is no color output of log
  messages printed to the screen.


  -e <perl regex>
  --always-ignore-error <perl regex>
  -------------------------
  If specified then all task names matching the <perl regexp> in the executed
  playlists will always return successful exit code =0 even if it fails with a
  return code >0.
  This parameter can be specified multiple times if more than one regular
  expression is needed.
  For example:
    -e 'Playlist::001_Installation_or_Upgrade_DSC::Create_Configuration_Files_Directory_P001S01T01'
        or shorter
    -e 'P001S01T01'

  This parameter can be useful when there is a problem with one task but it
  does not matter for the overall execution of the playlist, if the task fails
  then the execution will not stop when it's specified with this parameter.


  -g
  --show-progress
  ---------------
  If specified then a separate xterm window will open and show the command.log file
  for the job, i.e. showing the output of the commands that the execute_playlist.pl
  executes. This window will be opened as a background process and will show the
  contents of the command.log file as it's being changes, i.e. some kind of progress
  messages.
  To control the apperance of this window see the --xterm-option parameter below.

  NOTE:
  This option ONLY WORKS if the 'xterm' command exist, which means on most clusters
  this command doesn't exist and thus this parameter cannot be used.
  For these nodes you can manually open another terminal window to this node and
  then execute the following command to get the same information:
  tail -f <workspace directory>/command.log


  -h
  --help
  ------
  Shows this help information.

  If also parameter --playlist or -p is given with a valid Playlist name
  (use parameter --list-playlists or -i to see a list) then help for the
  specific Playlist is shown instead.


  -i
  --list-playlists
  ----------------
  When specified then it will list available playlists in the ../lib/Playlist/
  directory except for playlists starting with digits 900 or higher which are
  with the current naming convention supposed to be sub playlists called from
  the main playlists.


  -j <name>
  --jobname <name>
  ----------------
  Specifies the name of the job, this can e.g. be a descriptive name of what
  the playlist will do.
  For example: --jobname EBS073_Install_NDP1800

  If not specified then a default job name will be used consisting of the string
  "Playlist_" followed by the playlist name.
  For example: Playlist_001_Deploy_SC


  -l <filename>
  --log-file <filename>
  ---------------------
  If specified then all executed commands and their results will be written
  into the given <filename>.
  If not specified then a log file will be automatically created with the name
  'all.log' in the workspace directory specified with the --workspace parameter.


  -n <filename>
  --network-file <filename>
  -------------------------
  If specified then the filename given should be a Network Configuration
  XML file which will be read and parameters in the file will be used when
  e.g. connecting to a remote node.
  Most playlists will require this parameter but it's still optional since we
  have at least one playlist that takes no network configuration file as input.


  -o <string>
  --network-config-option <string>
  --------------------------------
  If specified then it is used to pick which of the different value_xxx
  attributes to use from the network configuration file.
  For example if you have the following attributes for a parameter in the
  network configuration file:
    - value="xxxx"
    - value_EVNFM="xxxx"
    - value_IPv4="xxxx"
    - value_DEPLOYMENT1="xxxx"
    - value_DEPLOYMENT2="xxxx"
  Then with the --network-config-option parameter you can select which of
  the value attributes to select as the "value" attribute.
  So if e.g. --network-config-option=DEPLOYMENT1 is specified then the
  value from the attribute "value_DEPLOYMENT1" will be used for the "value"
  attribute.
  If multiple attributes match (for a specific network config file parameter)
  the specified --network-config-option parameters which can also be given
  multiple times then the last matched value (sorted in alphabetical order)
  will be used as the "value" attribute.
  If none of the --network-config-option parameters match then the value
  set to the "value" attribute will be used.

  The use of these options will make it possible to have one network config
  file to be used for multiple purposes.


  --only-playlist-help
  --------------------
  If specified then only help for the playlist is shown instead of showing
  help for both the execute_playlist.pl script and the playlist.


  -p <name>
  --playlist <name>
  -----------------
  Specifies the playlist name to execute, this is the name of the playlist
  file stored in the package directory 'perl/lib/Playlist/' minus the
  '.pm' file extension.
  If a complete file path is specified then the path and extension are
  removed, leaving only the playlist name.
  To see a list of available playlists that can be used please use the
  command parameter -i or --list-playlists.


  -r <dirpath>
  --rerun-job <dirpath>
  ---------------------
  If specified and the job directory exist then the state file for the job
  is read and execution of the job is continued after the last executed task.


  -s <perl regex>
  --always-skip <perl regex>
  --------------------------
  If specified then all task names matching the <perl regexp> in the executed
  playlists will always be skipped.
  Parameter '--always-execute' has higher priority than '--always-skip'
  if both parameters match the same task name.
  This parameter can be specified multiple times if more than one regular
  expression is needed.
  For example:
    -s '.+'


  -t <string>
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


  -v <name>=<value>
  --variable <name>=<value>
  -------------------------
  If specified then the given variables will be stored in the \%::JOB_PARAMS
  hash variable which can be used by the executed Playlist files to access
  the variable values.
  If the needed variables are not provided by the --variable parameter and a
  Playlist depend on these variables then it's up to the Playlist to ask the
  user for the values, otherwise they will have undefined value and Playlist
  execution might result in failures.
  This parameter can be specified multiple times if many variables values
  needs to be initialized.
  NOTE: The <name> and <value> are case sensitive, so VAR1=VALUE1 is different
  from var1=value1.

  Special variables that are common for all playlists are the following:
EOF

    General::Playlist_Operations::print_info_about_job_variables(\%optional_job_variables, "    ");

    print <<EOF;

  -w <dirpath>
  --workspace <dirpath>
  ---------------------
  Specifies the directory name which will be used as the main workspace directory
  for storing playlist related files (temporary or permanent) during the execution
  of the playlist.
  Under this directory a sub directory will be created named as follows:
  <jobname>_<datestamp YYYYMMDD>_<timestamp HHMMSS>_<random 2 digit number>

  If this parameter is not specified then the default location will be:
  /tmp/workspaces/


  -x <perl regex>
  --always-execute <perl regex>
  -----------------------------
  If specified then all task names matching the <perl regexp> in the executed
  playlists will always be executed regardless if they have previously been executed.
  Parameter '--always-execute' has higher priority than '--always-skip'
  if both parameters match the same task name.
  This parameter can be specified multiple times if more than one regular
  expression is needed.
  For example:
    -x 'P001S01T01'
    -x 'P001S01T..'
    -x 'P001S..T..'


Output:
=======

The script writes the Playlist execution status to the screen for the user to
follow and it also writes a detailed log file in the used workspaces directory
as file 'all.log' or the file given by parameter -l or --log-file which contains
the output of all executed commands.


Intercepted process signals:
============================
This script intercepts the following signals sent to the process:

    - INT
      This is the interrupt signal that is triggered by either the user
      pressing the keyboard key combination CTRL-C, or by the user sending
      the signal with the 'kill' command (e.g. kill -s SIGINT <PID>).
      This will cause any playlist execution to be interrupted and the return
      code from a task or step to be the value 1 (for error) or if checked
      by special tasks it will just e.g. interrupt the current wait or loop
      with or without returning a failure.
      In some cases the user can then select if the playlist execution should
      continue or not when a console terminal is available, or a playlist
      fallback will be triggered if there is no console terminal available
      to answer the user prompt.
      If this signal is detected while executing the sleep operation inside
      the General::OS_Operations::sleep_with_progress sub routine then the
      sleep will be interrupted with the user being able to confirm if
      the sleep should continue or not and the signal will be forgotten
      and will not be detected by the step or task logic, so normal playlist
      execution will continue.

    - QUIT
      This is the quit signal that is triggered by either the user pressing
      the keyboard key combination CTRL-\\, or by the user sending
      the signal with the 'kill' command (e.g. kill -s SIGQUIT <PID>).
      This will cause any playlist execution to be interrupted and the return
      code from a task or step to be the value 1 (for error) or if checked
      by special tasks it will just e.g. interrupt the current wait or loop
      with or without returning a failure.
      In some cases the user can then select if the playlist execution should
      continue or not when a console terminal is available, or a playlist
      fallback will be triggered if there is no console terminal available
      to answer the user prompt.
      If this signal is detected while executing the sleep operation inside
      the General::OS_Operations::sleep_with_progress sub routine then the
      sleep will be interrupted without the user being able to confirm if
      the sleep should continue and the signal will later be detected by the
      step or task logic which will then fail the step/task.

    - USR1
      This is the user-defined signal 1 that is triggered by the user sending
      the signal with the 'kill' command (e.g. kill -s SIGUSR1 <PID>).
      This will cause any playlist execution to be terminated at the end of
      the currently executing step or task with a fallback which causes a
      failure of the whole playlist without giving the user any choice to
      continue execution, instead any defined playlist fallback logic to be
      executed.
      It will no longer be possible to continue execution of this job.

    - USR2
      This is the user-defined signal 2 that is triggered by the user sending
      the signal with the 'kill' command (e.g. kill -s SIGUSR2 <PID>).
      This will cause any playlist execution to be terminated at the end of
      the current executing step or task with a graceful exit without executing
      any further steps or tasks and without giving the user any choice to
      continue execution.
      If so wanted, it will be possible to continue execution of this job from
      the last started task and skipping all previously executed tasks if the
      user invokes the execute_playlist.pl command with the exact same parameters
      as before and adding the following parameter: --re-run <workspace directory

    - TERM
      This is the terminate signal that is triggered by the user sending the
      the signal with the 'kill' command (e.g. kill -s SIGTERM <PID>).
      This will cause any playlist execution to be immediately interrupted and
      no further playlist logic will be executed but it will cleanup the job
      by closing any open files and created packed files if so wanted.

    - KILL
      This is the ultimate kill signal that is triggered by the user sending the
      the signal with the 'kill' command (e.g. kill -s SIGKILL <PID>).
      This will cause any playlist execution to be immediately interrupted and
      nothing else will be done, including cleanup of the job.
      This signal should only be used if nothing else works.


Examples:
=========

  Example 1 List available Playlists
  ----------------------------------
  To show a list of available Playlists that can be used with the tool.
  $0 -i

  Example 2 Show help for this script
  -----------------------------------
  To show the help text for this script with details about parameters to use.
  $0 -h

  Example 3 Show help for a Playlist
  ----------------------------------
  To show the help text for a specific Playlist which includes special Job
  parameters used by the Playlist.
  $0 -h -p 001_Installation_or_Upgrade_DSC

  Example 4 Installation of node
  ------------------------------
  The job parameters CLUSTER_CONFIG_FILE, EVIP_FILE, GEP_CLISS_FILE,
  INSTALLATION_CONFIG_FILE and SS7CAF_FILE can be given a value if the user want
  to use specially congigured files, if the values are given as the empty string ("")
  then automatically generated files that will suit most installations will be used
  which is also the default if the parameters are not given.

  $0 \\
      -p 001_Installation_or_Upgrade_DSC \\
      -j BSP078_Installation_NDP191 \\
      -n /home/daftuser/network_config_files/BSP-078.xml \\
      -v SOFTWARE_DIR=/var/lib/sufstorage/software_deliveries/DSC/NDP191_ESM_DSC_R1A918_DDB_P1A341_SS7CAF_6.2_no_MAP_New_DBS_Distribution \\
      -v CONNECTION_TYPE=direct_ssh \\
      -v CLUSTER_CONFIG_FILE="" \\
      -v EVIP_FILE="" \\
      -v GEP_CLISS_FILE="" \\
      -v INSTALLATION_CONFIG_FILE="/home/daftuser/config_files/BSP-078/installation_files/smaller_30G_cluster_disk_installtion.conf" \\
      -v SS7CAF_FILE="/home/daftuser/config_files/BSP-078/installation_files/NDP191_ss7caf_config_no_mapiwf.txt" \\
      -v BACKUPS_BY_PLAYLIST=yes \\
      -v INSTALLATION_ALLOWED=yes

  Example 5 Upgrade of node
  -------------------------
  The job parameters CLUSTER_CONFIG_FILE, EVIP_FILE, GEP_CLISS_FILE,
  INSTALLATION_CONFIG_FILE and SS7CAF_FILE serve no purpose for an upgrade but must
  be given anyway, so just enter them as the empty string ("").

  $0 \\
      -p 001_Installation_or_Upgrade_DSC \\
      -j BSP078_Upgrade_NDP191 \\
      -n /home/daftuser/network_config_files/BSP-078.xml \\
      -v SOFTWARE_DIR=/var/lib/sufstorage/software_deliveries/DSC/NDP191_ESM_DSC_R1A918_DDB_P1A341_SS7CAF_6.2_no_MAP_New_DBS_Distribution \\
      -v CONNECTION_TYPE=nbi \\
      -v BACKUPS_BY_PLAYLIST=yes \\
      -v INSTALLATION_ALLOWED=no

  Example 6 Setting back GEP boards to factory default
  ----------------------------------------------------
  $0 \\
      -p 801_Revert_System_To_Factory_Default \\
      -j BSP078_Reset_Factory_Default_GEP \\
      -n /home/daftuser/network_config_files/BSP-078.xml \\
      -v CONNECTION_TYPE=direct_ssh \\
      -v RESET_CMXB_SCXB=no \\
      -v RESET_GEPS=yes


Return code:
============

   0: Successful, the execution was successful.
   1: Unsuccessful, some failure was reported.
 130: Unsuccessful, User interrupted the execution by pressing CTRL-C.
 131: Unsuccessful, User interrupted the execution by pressing CTRL-\\.
 143: Execution was interrupted by a SIGTERM most likly due to a reboot of the
      cluster. This might be OK because execution should start up again after
      the reboot (only applicable for native DSC or vDSC, not for SC or cnDSC).

EOF
}

# -----------------------------------------------------------------------------
#
sub usage_playlist {
    if ($only_playlist_help == 0) {
        $message = "# Specific help information for the $playlist_name playlist #";
        printf "\n%s\n%s\n%s\n", "#" x length($message), $message, "#" x length($message);
    }

    $playlist_file = "$package_dir/perl/lib/Playlist/$playlist_name.pm";
    if (-f $playlist_file) {
        require $playlist_file;
        eval "import Playlist::$playlist_name";
        if ($@) {
            print "ERROR: Unable to import playlist\nError=$@\n";
            exit 1;
        }
    } else {
        print "ERROR: Unable to find the playlist file '$playlist_file'\n";
        exit 1;
    }

    $playlist_function = "Playlist::${playlist_name}::usage";
    no strict;
    if (defined &$playlist_function) {
        &$playlist_function;
    } else {
        print "\nNo help available\n\n";
    }

    # Fetch all used playlist variables
    $playlist_function = "Playlist::${playlist_name}::usage_return_playlist_variables";
    if (defined &$playlist_function) {
        %playlist_variables = (%optional_job_variables, &$playlist_function);
    } else {
        %playlist_variables = %optional_job_variables;
    }

    # Calculate max column length
    my $length;
    my $max_length_default_value = 13;
    my $max_length_name = 4;
    for my $name (sort keys %playlist_variables) {
        $length = length($name);
        $max_length_name = $length if ($max_length_name < $length);
        $length = length($playlist_variables{$name}{'default_value'});
        $max_length_default_value = $length if ($max_length_default_value < $length);
    }

    # Display summary of job parameters
    my $heading_printed = 0;
    for my $name (sort keys %playlist_variables) {
        if ($playlist_variables{$name}{'mandatory'} eq "no") {
            if ($heading_printed == 0) {
                $message = "# Optional Job Parameters #";
                printf "\n\n%s\n%s\n%s\n\n", "#" x length($message), $message, "#" x length($message);
                printf "%-${max_length_name}s  %-${max_length_default_value}s  %-10s  %-10s  %s\n", "Name", "Default Value", "Type", "Case Check", "Validity Mask";
                printf "%-${max_length_name}s  %-${max_length_default_value}s  %-10s  %-10s  %-13s\n", "-"x${max_length_name}, "-"x${max_length_default_value}, "-"x10, "-"x10, "-"x13;
                $heading_printed = 1;
            }
            printf "%-${max_length_name}s  %-${max_length_default_value}s  %-10s  %-10s  %s\n", $name, $playlist_variables{$name}{'default_value'}, $playlist_variables{$name}{'type'}, $playlist_variables{$name}{'case_check'}, $playlist_variables{$name}{'validity_mask'};
        }
    }
    $heading_printed = 0;
    for my $name (sort keys %playlist_variables) {
        if ($playlist_variables{$name}{'mandatory'} eq "yes") {
            if ($heading_printed == 0) {
                $message = "# Mandatory Job Parameters #";
                printf "\n\n%s\n%s\n%s\n\n", "#" x length($message), $message, "#" x length($message);
                printf "%-${max_length_name}s  %-${max_length_default_value}s  %-10s  %-10s  %s\n", "Name", "Default Value", "Type", "Case Check", "Validity Mask";
                printf "%-${max_length_name}s  %-${max_length_default_value}s  %-10s  %-10s  %-13s\n", "-"x${max_length_name}, "-"x${max_length_default_value}, "-"x10, "-"x10, "-"x13;
                $heading_printed = 1;
            }
            printf "%-${max_length_name}s  %-${max_length_default_value}s  %-10s  %-10s  %s\n", $name, $playlist_variables{$name}{'default_value'}, $playlist_variables{$name}{'type'}, $playlist_variables{$name}{'case_check'}, $playlist_variables{$name}{'validity_mask'};
        }
    }
}

# -----------------------------------------------------------------------------
#
sub write_summary_file {
    my $jobstatus = $JOB_PARAMS{'JOBSTATUS'};
    if ($jobstatus eq "NONE") {
        $jobstatus = $program_rc == 0 ? "SUCCESSFUL" : "FAILED";
    }

    @job_summary = (
        "Job Summary",
        "===========",
        "",
        "           Job Name : $jobname",
        "           Job Type : $JOB_PARAMS{'JOBTYPE'}",
        "         Job Status : $jobstatus",
        "     Job Start Time : " . scalar localtime($start_time),
        "       Job End Time : " . scalar localtime($stop_time),
        "   Job Elapsed Time : " . General::OS_Operations::convert_seconds_to_wdhms($job_duration, 0),
        "",
        "Job Started by User : $ENV{'USER'}",
        "Job Started on Host : $JOB_PARAMS{'DAFT_HOSTNAME'}",
    );

    #
    # Add other files to summary if wanted
    #
    if (@append_file_to_summary) {
        for my $filename (@append_file_to_summary) {
            if (-f "$filename") {
                # Add an empty line if needed
                push @job_summary, "" if ($job_summary[-1] ne "");

                General::File_Operations::read_file(
                    {
                        "filename"            => "$filename",
                        "output-ref"          => \@job_summary,
                        "append-output-ref"   => 1,
                    }
                );
            }
        }
    }

    #
    # Add summary of job tasks performed
    #
    if (@JOB_STATUS) {
        # Add an empty line if needed
        push @job_summary, "" if ($job_summary[-1] ne "");

        push @job_summary, (
            "Job Status",
            "==========",
            "",
        );

        push @job_summary, @JOB_STATUS;
    }

    #
    # Add summary of applied workarounds
    #
    if (@General::Playlist_Operations::workaround_tasks) {
        # Add an empty line if needed
        push @job_summary, "" if ($job_summary[-1] ne "");

        push @job_summary, (
            #--------------------------------------------------------------------------------
            "Applied Workarounds",
            "===================",
            "",
            "The following workarounds has been applied in the executed playlist tasks and",
            "should be removed from the playlist at some point in time when the problem they",
            "try to work around has been fixed.",
            "",
        );

        push @job_summary, @General::Playlist_Operations::workaround_tasks;
    }

    # Add an empty line if needed
    push @job_summary, "" if ($job_summary[-1] ne "");

    push @job_summary, "Execution Summary";
    push @job_summary, "=================";
    push @job_summary, "";
    push @job_summary, "Elapsed Time           Return Code              Type  Name";
    push @job_summary, "---------------------  -----------------------  ----  ----";

    push @job_summary, @General::Playlist_Operations::step_task_summary;
    push @job_summary, General::Logging::get_execution_times_summary();

    General::File_Operations::write_file(
        {
            "filename"            => "$job_workspace_dir/summary.txt",
            "output-ref"          => \@job_summary,
            "eol-char"            => "\n",
            "append-file"         => 0,
            "file-access-mode"    => "666",
        }
    );
}
