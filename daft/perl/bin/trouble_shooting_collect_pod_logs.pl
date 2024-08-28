#!/usr/bin/perl

################################################################################
#
#  Author   : eustone
#
#  Revision : 1.3
#  Date     : 2022-12-13 18:02:03
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
use warnings;

use File::Basename qw(dirname);
use Cwd qw(abs_path);
use lib dirname(dirname abs_path $0) . '/lib';
use version;

use ADP::Kubernetes_Operations;
use General::File_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;

my $abort_execution = 0;
my %background_proccess_info;
    # Key 1: Pid number
    # Key 2: One of
    #   - 'container' : The name of the container to fetch log information for
    #   - 'log_file' : The log file that is used for logging all information
    #   - 'pod data' : The POD name and container name, e.g. "<pod name> -c <container name>" or when no container specified "<pod name>"
    #   - 'pod name' : The POD name
my @children = ();
my $ctrl_c_pressed = 0;
my $current_epoch = time();
my $error_cnt = 0;
my $kubeconfig = "";
my $logfile = "";
my $namespace = "";
my $no_logfile = 0;
my $no_tty = 0;
my $output_directory = ".";
my @pids = ();
my @pods = ();
my %process_info;
my $rc;
my @result;
my $return_code = 0;
my $show_help = 0;
my $sleep_interval = 2;
my $verbose = 0;

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "c|kubeconfig=s"           => \$kubeconfig,
    "h|help"                   => \$show_help,
    "l|log-file=s"             => \$logfile,
    "n|namespace=s"            => \$namespace,
    "o|output-directory=s"     => \$output_directory,
    "p|pods=s"                 => \@pods,
    "s|sleep-interval=i"       => \$sleep_interval,
    "v|verbose"                => \$verbose,
    "no-logfile"               => \$no_logfile,
    "no-tty"                   => \$no_tty,
);

if ( $show_help ) {
    show_help();
    exit 0;
}

# **************
# *            *
# * Main logic *
# *            *
# **************

# Check validity of command parameters
check_parameters();

# Create the output directory, if needed
handle_output_directory();

# Start log file, if needed
handle_log_file();

# Install a CTRL-C handler
$SIG{INT} = sub { $ctrl_c_pressed++; };
# Install a handler for SIGABRT
$SIG{ABRT} = sub { $abort_execution++; };

handle_log_collection();

if ($error_cnt > 0) {
    cleanup_and_exit(1);
} else {
    cleanup_and_exit($return_code);
}

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------------------------------------
sub check_parameters {
    if ($kubeconfig ne "" && -f "$kubeconfig") {
        # Set kubeconfig for any used library functions
        ADP::Kubernetes_Operations::set_used_kubeconfig_filename($kubeconfig);

        $kubeconfig = " --kubeconfig=$kubeconfig";
    } elsif ($kubeconfig ne "") {
        print "The kubeconfig file does not exist\n" if ($no_tty == 0);
        exit 1;
    }

    # Check if namespace exists
    if ( $namespace ne "" ) {
        my $namespace_check = ADP::Kubernetes_Operations::namespace_exists($namespace);
        if ( $namespace_check != 1 ) {
            # The namespace does not exist (=0) or error fetching the namespace data (=2)
            print "Namespace $namespace does not exist or problem checking the namespace\n" if ($no_tty == 0);
            cleanup_and_exit(1);
        }
    } else {
        print_message("No namespace provided\n") if ($no_tty == 0);
        cleanup_and_exit(1);
    }
}

# -----------------------------------------------------------------------------
sub cleanup_and_exit {
    my $rc = shift;

    # Stop logging of output to file
    if ($logfile) {
        General::Logging::log_disable();
        print_message("\nDetailed log file written to $logfile\n", ! $verbose);
    }

    if (-d "$output_directory") {
        print_message("\nOutput files written to directory $output_directory\n");
    }

    exit $rc;
}

# -----------------------------------------------------------------------------
sub handle_log_collection {
    my @all_pod_names = ADP::Kubernetes_Operations::get_pod_names(
        {
            "namespace"         => $namespace,
            "hide-output"   => 1,
        }
    );
    my $command;
    my $container;
    my @containers;
    my @dead_pids = ();
    my $dir_name;
    my $file_name;
    my $message;
    my $pid;
    my $pod_dir;
    my %pod_info;
        # Key:   Pod name or Pod regexp
        # Value: Array of zero or more container names
    my $pod_name;
    my @pod_names = ();
    my $pod_regexp;
    my %pod_to_pid;
        # Key: Pod and container data e.g. "<pod name> -c <container name>" or just "<pod name>"
        # Value: Process Id (pid)

    # Look through the wanted pod regular expressions
    # and update the %pod_info hash.
    for my $pod_container (@pods) {
        @containers = split /,/, $pod_container;
        # First index is always the pod regular expression
        $pod_regexp = shift @containers;
        if (@containers) {
            push @{$pod_info{$pod_regexp}}, @containers;
        } else {
            @{$pod_info{$pod_regexp}} = ();
        }
    }

    # For all pods in the namespace, look for wanted pods
    for $pod_name (@all_pod_names) {
        # Find wanted pods from all prods in namespace and fill in the
        # @pod_names array.
        for $pod_regexp (sort keys %pod_info) {
            if ($pod_name =~ /$pod_regexp/) {
                # The pod matches the wanted pods
                @containers = @{$pod_info{$pod_regexp}};
                if (@containers) {
                    # We have one or more containers to fetch logs from
                    for $container (@containers) {
                        next unless $container;
                        push @pod_names, "$pod_name -c $container";
                    }
                } else {
                    push @pod_names, $pod_name;
                }
            }
        }
    }

    # Check that we have Diameter pods
    if (scalar @pod_names == 0) {
        $error_cnt++;
        print_message("There are no wanted pods in the $namespace namespace\n");
        cleanup_and_exit(1);
    }

    # Start background log collection in all the diameter pods
    for my $pod_data (@pod_names) {
        if ($pod_data =~ /^(\S+) -c (\S+)$/) {
            $pod_name = $1;
            $container = $2;
            $file_name = "$output_directory/${pod_name}_${container}.log";
        } else {
            $pod_name = $pod_data;
            $container = "";
            $file_name = "$output_directory/$pod_name.log";
        }

        # Write start message to the log file
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "echo -en \"\\nLog collection started \$(date)\\n\\n\" >> $file_name",
                "hide-output"   => 1,
            }
        );

        $command = "kubectl -n $namespace logs $pod_data -f $kubeconfig >> $file_name";
        $pid = General::OS_Operations::background_process_run($command);
        if ($pid > 0) {
            print_message("Background log collection started for pod $pod_data with Process Id: $pid\n");
            $background_proccess_info{$pid}{'container'} = $container;
            $background_proccess_info{$pid}{'pod_data'} = $pod_data;
            $background_proccess_info{$pid}{'pod_name'} = $pod_name;
            $background_proccess_info{$pid}{'log_file'} = $file_name;
            $pod_to_pid{$pod_data} = $pid;
        } else {
            $error_cnt++;
            print_message("Failed to start background process for pod $pod_data\n");
            cleanup_and_exit(1);
        }
    }

    if ($no_tty == 0) {
        General::Message_Generation::print_message_box(
            {
                "header-text"   => "Starting monitoring of background log collection Process Id's.",
                "messages"      => [ "Interrupt log collection by:", " - Pressing CTRL-C", " - Sending the following command from another terminal: kill -s SIGABRT $$" ],
                "align-text"    => "left",
                "max-length"    => 0,
                "return-output" => \$message,
                "empty-line-before-box" => 1,
                "empty-line-after-box" => 1,
            }
        );
        # print_message("\nStarting monitoring of background log collection Process Id's, Interrupt log collection by pressing CTRL-C.\nOr Interrupt log collection by sending the following command from another terminal:\nkill -s SIGABRT $$\n");
    } else {
        General::Message_Generation::print_message_box(
            {
                "header-text"   => "Starting monitoring of background log collection Process Id's.",
                "messages"      => [ "Interrupt log collection by:", " - Sending the following command from another terminal: kill -s SIGABRT $$" ],
                "align-text"    => "left",
                "max-length"    => 0,
                "return-output" => \$message,
                "empty-line-before-box" => 1,
                "empty-line-after-box" => 1,
            }
        );
        # print_message("\nStarting monitoring of background log collection Process Id's, Interrupt log collection by sending the following command from another terminal:\nkill -s SIGABRT $$\n");
    }
    print_message($message);
    while (1) {
        # Loop forever or until:
        #  - There are no diameter pods anymore
        #  - The user press CTRL-C to interrupt the log collection
        #  - The script receives the SIGABRT signal

        # Wait a while before checking if log collection process id's are still running
        $rc = General::OS_Operations::sleep_with_progress(
            {
                "allow-interrupt"   => 1,
                "progress-interval" => 1,
                "progress-message"  => 0,
                "seconds"           => $sleep_interval,
            }
        );
        $ctrl_c_pressed++ if ($rc == 1);

        # Check if user interrupted the log collection
        if ($ctrl_c_pressed > 0 && $no_tty == 0) {
            print_message("\nLog collection was interrupted by CTRL-C\n");
            last;
        }
        if ($abort_execution > 0) {
            print_message("\nAbort execution ordered by SIGABRT\n");
            last;
        }

        # Reset the counter
        $ctrl_c_pressed = 0;

        # Check if log collections PID's are still running in the background
        @dead_pids = ();
        for $pid (keys %background_proccess_info) {
            if (General::OS_Operations::background_process_is_running($pid) == 0) {
                # The pid is dead and not collecting any logs
                push @dead_pids, $pid;
                print_message("\nThe pid $pid monitoring logs for pod $background_proccess_info{$pid}{'pod_data'} have died\n");
            }
        }

        if (@dead_pids) {
            # There are dead pids, we now need to figure out which one it is
            # and restart the log collection.

            for $pid (@dead_pids) {
                # Delete the information from the hash
                delete $pod_to_pid{$background_proccess_info{$pid}{'pod_data'}};
                delete $background_proccess_info{$pid};
            }

            # Check which pods still exists
            @all_pod_names = ADP::Kubernetes_Operations::get_pod_names(
                {
                    "namespace"         => $namespace,
                    "hide-output"   => 1,
                }
            );

            # For all pods in the namespace, look for wanted pods
            @pod_names = ();
            for $pod_name (@all_pod_names) {
                # Find wanted pods from all prods in namespace and fill in the
                # @pod_names array.
                for $pod_regexp (sort keys %pod_info) {
                    if ($pod_name =~ /$pod_regexp/) {
                        # The pod matches the wanted pods
                        @containers = @{$pod_info{$pod_regexp}};
                        if (@containers) {
                            # We have one or more containers to fetch logs from
                            for $container (@containers) {
                                next unless $container;
                                push @pod_names, "$pod_name -c $container";
                            }
                        } else {
                            push @pod_names, $pod_name;
                        }
                    }
                }
            }

            # Start background log collection for all new missing pods
            for my $pod_data (@pod_names) {
                unless (exists $pod_to_pid{$pod_data}) {
                    if ($pod_data =~ /^(\S+) -c (\S+)$/) {
                        $pod_name = $1;
                        $container = $2;
                        $file_name = "$output_directory/${pod_name}_${container}.log";
                    } else {
                        $pod_name = $pod_data;
                        $container = "";
                        $file_name = "$output_directory/$pod_name.log";
                    }

                    # Write start message to the log file
                    $rc = General::OS_Operations::send_command(
                        {
                            "command"       => "echo -en \"\\nLog collection started \$(date)\\n\\n\" >> $file_name",
                            "hide-output"   => 1,
                        }
                    );

                    $command = "kubectl -n $namespace logs $pod_data -f $kubeconfig >> $file_name";
                    $pid = General::OS_Operations::background_process_run($command);
                    if ($pid > 0) {
                        print_message("Background log collection started for pod $pod_data with Process Id: $pid\n");
                        $background_proccess_info{$pid}{'container'} = $container;
                        $background_proccess_info{$pid}{'pod_data'} = $pod_data;
                        $background_proccess_info{$pid}{'pod_name'} = $pod_name;
                        $background_proccess_info{$pid}{'log_file'} = $file_name;
                        $pod_to_pid{$pod_data} = $pid;
                    } else {
                        $error_cnt++;
                        print_message("Failed to start background process for pod $pod_data\n");
                        cleanup_and_exit(1);
                    }
                }
            }
        }
    }

    # We end up here if we either got interrupted by CTRL-C or by the SIGABRT signal
    # and it's now time to stop the log collection by killing the background processes.
    stop_background_tasks();

    print_message("Background log collection stopped\n");
    $return_code = 0;
}

# -----------------------------------------------------------------------------
sub handle_log_file {

    if ($no_logfile == 1) {
        $logfile = "";
    } elsif ($logfile eq "") {
        $logfile = "$output_directory/trouble_shooting_collect_diameter_logs.log";
    }

    # Start logging of output to file
    if ($logfile) {
        $rc = General::Logging::log_enable("$logfile");
        if ($rc != 0) {
            exit 1;
        }
        $logfile = abs_path $logfile;
    }
}

# -----------------------------------------------------------------------------
sub handle_output_directory {
    $output_directory =~ s/\/$//g;   # Remove trailing /
    if ($output_directory eq ".") {
        $output_directory = "./diameter_logs_$current_epoch";
    }
    if (-d "$output_directory") {
        # Directory exists
        $output_directory = abs_path $output_directory;
    } else {
        # The directory does not exist, so create it
        # print_message("Output directory '$output_directory' does not exists, trying to create it\n");
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "mkdir -p $output_directory",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0) {
            $output_directory = abs_path $output_directory;
        } else {
            print_message("Failed to create the output directory\n  '$output_directory'\n" . (join "\n", @result) . "\n");
            exit 1;
        }
    }
}

# -----------------------------------------------------------------------------
sub print_message {
    my $text = shift;
    my $only_log = 0;

    if (scalar @_ > 0) {
        $only_log = shift;
    }

    if ($logfile ne "") {
        General::Logging::log_write($text);
    }

    if ($only_log == 0) {
        print $text if ($no_tty == 0);
    }
}

# -----------------------------------------------------------------------------
sub show_help {
    print <<EOF;

Description:
============

This script collects logs from one or more pod containers and writes the
information to log files, one per pod container.

It monitors that the log collection is ongoing until the user press CTRL-C
or the script is sent the SIGABRT signal.
If any log collection stops e.g. due to a POD restart then the log collection
is automatically re-started.

This was initially used as trouble shooting help for the GSSUPP-12317.


Syntax:
=======

$0 <MANDATORY> [<OPTIONAL>]

<MANDATORY> is one of the following parameters:

  -n <string>           | --namespace <string>
  -p <pod,container>    | --pods <pod,container>

<OPTIONAL> are one or more of the following parameters:

  -c <filename>         | --kubeconfig <filename>
  -h                    | --help
  -l <filename>         | --log-file <filename>
  -o <dirname>          | --output-directory <dirname>
  -s <integer>          | --sleep-interval <integer>
  -v                    | --verbose
                          --no-logfile
                          --no-tty

where:

  -c <filename>
  --kubeconfig <filename>
  -----------------------
  If specified then it will use this file as the config file for helm and
  kubectl commands.
  If not specified then it will use the default file which is normally the
  file ~/.kube/config.


  -h
  --help
  ------
  Shows this help information.


  -l <filename>
  --log-file <filename>
  ---------------------
  If specified then all executed commands and their results will be written
  into the given <filename>.
  By default a log file is created either in the file specified with -f/--log-file
  or in the directory specified with --output-dir with a file name of:

    trouble_shooting_collect_diameter_logs.log
  See also --no_logfile if no log file should be created.


  -n <string>
  --namespace <string>
  -----------------------
  The namespace of the SC deployment to check.
  This parameter is mandatory.


  -o <dirname>
  --output-directory <dirname>
  ----------------------------
  If specified then the output files (log) will be written to this directory instead
  of the default directory called diameter_logs_XXXX in the current directory where
  XXXX is the current epoch time (seconds since Jan 1 1970).


  -p <pod,container>
  --pods <pod,container>
  ----------------------
  Specifies the pod and container to collect logs from, and the container name
  should be separated from the pod name or regular expression by a comma (,).
  The parameter can be specified multiple times if collection should be done from
  multiple pods.
  For example the following parameters will collect logs from the 'dsl' container
  for all pods that match the names 'eric-bsf-diameter-xxxx' and
  'eric-stm-diameter-xxxx':

    --pods 'eric-bsf-diameter-.+,dsl'
    --pods 'eric-stm-diameter-.+,dsl'


  -s <integer>
  --sleep-interval <integer>
  --------------------------
  Specifies how frequent (in seconds) the script should check if the background
  processes are still running and if not restart the log collection for the
  pods that are missing.
  If not specified then the default value is every $sleep_interval seconds.


  -v
  --verbose
  ---------
  Specify this if verbose output showing more details should be written to
  STDOUT.
  By default only the minimum amount of printouts are shown on STDOUT.


  --no-logfile
  ------------
  Specify this parameter if no log file with detailed information should be
  created and information is ONLY printed to STDOUT and STDERR.


  --no-tty
  --------
  Specify this parameter if the script is running in the background and you
  don't want to have any printouts to STDOUT.
  Using this parameter will also disable the CTRL-C mechanism so it's not possible
  to interrupt the sleep, and the only way to abort the program ahead of time
  will be to send the SIGABRT signal.

    kill -s SIGABRT <pid>


Examples:
=========

  - Start log collection for the Diameter pods allowing CTRL-C interruption:
    $0 -n eiffelesc --pods 'eric-bsf-diameter-.+,dsl' --pods 'eric-stm-diameter-.+,dsl'

  - Start log collection for the Diameter pods in the background without any output
    written to the console:
    $0 -n eiffelesc --no-tty --pods 'eric-bsf-diameter-.+,dsl' --pods 'eric-stm-diameter-.+,dsl'


Return code:
============

   0:  Successful, the execution was successful.
   >0: Unsuccessful, some failure was reported.

EOF
}

# -----------------------------------------------------------------------------
sub stop_background_tasks {
    my $child;
    my $message = "";
    my $rc;
    my $wait_time = 0;  # Change this to a non-zero value if SIGABRT should be sent

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

    print_message("Checking if there are any background processes that needs to be stopped\n");
    # Fetch any started background processes
    @pids = General::OS_Operations::background_processes();

    # First kill any started background processes
    for my $pid (@pids) {
        # Fetch process information
        fetch_process_info($pid);

        if ($wait_time > 0) {
            # First try to gracefully close the process
            print_message("Sending background process PID=$pid the signal SIGABRT\n");
            $rc = General::OS_Operations::background_process_send_signal($pid, "SIGABRT");
            if ($rc == 1) {
                # The process existed, wait a while to allow it to close down
                print_message("Waiting $wait_time seconds for the background process PID=$pid to gracefully close down\n");
                sleep $wait_time;
            }
        }

        # Kill the process, wait max 10 seconds for it and it's children to die
        print_message("Killing background process PID=$pid\n");
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
        print_message("Killing child process PID=$child for parent PID=$process_info{$child}{'parent'}\n");
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
        print_message("$message");
    } else {
        print_message("There are no background processes\n");
    }
}
