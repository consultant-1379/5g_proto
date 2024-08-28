#!/usr/bin/perl

################################################################################
#
#  Author   : everhel, eustone
#
#  Revision : 1.215
#  Date     : 2024-06-14 15:26:05
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2020-2024
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
use General::Json_Operations;
use General::Logging;
use General::Message_Generation;
use General::OS_Operations;
use General::Yaml_Operations;

# Uncomment the following 2 lines if you need to use this module to print debug information.
# use Data::Dumper;
# $Data::Dumper::Sortkeys = 1;

my @allow_list_alarms;
my @allow_list_coredumps;
my @allow_list_pod_restarts;
# Special variable that contains a matrix of different applications that
# might want special treatments for specific checks.
# The verdict can be one of the following:
#   - "fail"
#
#     If the result of the check is failed then it will also be marked as
#     failed in the over all result.
#     If the result of the check is skipped then it will also be marked as
#     skipped and have no impacy on the over all result.
#     If the result of the check is successful then it will also be marked
#     as successful and not cause a failure of the over all result (unless
#     there are other checks that fail).
#
#   - "skip"
#
#     This check will not be executed at all and the check will be marked
#     as skipped and have no impact on the over all result.
#
#   - "warning"
#
#     If the result of the check is failed then it's changed to a warning
#     which will not cause a failure of the over all result (unless there
#     are other checks that fail).
#
# NOTE:
# This matrix should be updated when a specific check should have special
# treatment depending on the application and you only need to manually
# add entries when needed.
my %application_check_matrix = (
    "dsc"   => {
        "kpi"               =>  "skip",
        "kpi_success_rate"  =>  "skip",
        "master_core_dumps" =>  "warning",
        "max_connections"   =>  "skip",
        "worker_core_dumps" =>  "warning",
    },
);
# The following variable should be updated when more application types should be automatically detected.
my $application_type_regexp = 'cloud-native-base|cloud-native-nf-additions|dsc|sc-bsf|sc-cs|sc-scp|sc-sepp|sc';
my %available_checks;
    # The key is the name of the check and the value is the verdict to
    # use for the check result which can be "fail", "skip" or warning".
my %available_groups;
my %available_variables;

# Special hash or array variable that contains a short description what
# the check is doing.
my %check_description;
initialize_check_description();

# Special variable that contains a matrix of different check groups that
# can be executed and what the final vedict will be if the check is
# successful or failed.
my %check_group_matrix;
initialize_check_group_matrix();

# Special variable that contains a matrix of different groups that
# can be executed and what checks in these groups are applicable and
# what the final vedict will be if the check is successful or failed.
# This variable is automatically filled in from the %check_group_matrix
# variable and it just switches around the keys so the top level key
# is the group instead of the check, and the second level key is the
# check instead of the group.
# All to make it easier for the logic below.
my %group_check_matrix;

my $application = "";
my %application_information;
my $check_count;
my %check_execution_time;
my $check_file = "";
my $check_result;
my $check_start_time;
my $check_stop_time;
my @checks = ();
my $cmyp_ip = "";
my %cnf;    # Enabled CNF's i.e. bsf, scp and sepp
my $group_name = "";
my $health_check;
my $helm_executable;
my $helm_information;
my $json_output = 0;
my $kpi_cached_command = "";
my @kpi_cached_result = ();
my $kubeconfig = "";
my $logfile = "/tmp/system_health_check_" . time() . ".log";
my $message;
my $name;
our $namespace = "";
my $namespace_check = 0;
our $namespace_error = 0;
my $no_logfile = 0;
my $no_color = 0;
my $node_name = "";
my $own_registry_url = "";
my $pre_deployment = 0;
my @progress_chars = ();
my $progress_cnt = 0;
my $progress_type = 1;
my $post_deployment = 0;
my $return_code = 0;
my @return_codes;
my @result_details;
my $result_information;
my @result_summary;
my $show_help = 0;
my $show_variable_values = 0;
my $sub_name;
my $subref;
my $table_output = 1;       # Change to 0 if boxed printout is wanted
my %timestamp_epoch_lookup;
my $used_helm_version = 3;

# Special variables used inside the check_xxxxx subroutines to alter the validity checks being done.
my %variables;
my %variables_user_set;
initialize_variables();

# Special hash variable that contains a short description what the variable is doing.
my %variables_description;

my %variables_regexp;
    # Set default values which are in effect until the parameter --variables_regexp_file is specified
    # at which point any parameter specified in this file will overwrite the default values.
    # Currently only the following variables are supported:
    #  - pod_utilization_cpu_threshold
    #  - pod_utilization_memory_threshold
    #                  <variable name>                   <pod regexp>                           <value>       Check that is using the variable
    $variables_regexp{'pod_utilization_memory_threshold'}{'^eric-data-search-engine-data-'}   = 95;           # top_pods
my $variables_regexp_file = "";

my $verbose = 0;
my $verdict;

# *****************************
# *                           *
# * Validate input parameters *
# *                           *
# *****************************

# Parse command line parameters
use Getopt::Long;          # standard Perl module
GetOptions (
    "a|application=s"          => \$application,
    "c|check=s"                => \@checks,
    "f|log_file=s"             => \$logfile,
    "b|verbose"                => \$verbose,
    "g|group=s"                => \$group_name,
    "h|help"                   => \$show_help,
    "j|json_output"            => \$json_output,
    "l|used_helm_version=i"    => \$used_helm_version,
    "n|namespace=s"            => \$namespace,
    "o|post_deployment"        => \$post_deployment,
    "r|pre_deployment"         => \$pre_deployment,
    "progress_type=s"          => \$progress_type,
    "u|own_registry_url=s"     => \$own_registry_url,
    "v|variables=s"            => sub {
                                        if ($_[1] =~ /^(\S+?)=(.*)/) {
                                            $variables{$1} = $2;
                                            $variables_user_set{$1} = 1;
                                        }
                                    },
    "check_file=s"             => \$check_file,
    "kubeconfig=s"             => \$kubeconfig,
    "no_color"                 => \$no_color,
    "no_logfile"               => \$no_logfile,
    "show_variable_values"     => \$show_variable_values,
    "variables_regexp_file=s"  => \$variables_regexp_file,
);

if ($application) {
    $application = lc($application);
}

if ($check_file ne "") {
    my @lines = ();
    if (General::File_Operations::read_file( { "filename" => $check_file, "output-ref" => \@lines, "ignore-empty-lines" => 1, "ignore-pattern" => '^\s*#' } ) != 0) {
        print "Unable to read the file '$check_file'\n";
        exit 1;
    }
    for (@lines) {
        if (/^\s*check:\s*(\S+)\s*=\s*(.+)$/) {
            push @checks, "$1=$2";
        } elsif (/^\s*check:\s*(\S+)\s*$/) {
            push @checks, $1;
        } elsif (/^\s*(variable|variables):\s*(\S+)\s*=\s*(.+)$/) {
            $variables{$2} = $3;
        } elsif (/^\s*group:\s*(\S+)\s*$/) {
            if ($group_name ne "") {
                print "Warning: Parameter 'group' has already been set to value '$group_name', it will now be overwritten and set to value '$1'";
            }
            $group_name = $1;
        } elsif (/^\s*(\S+)\s*=\s*(.+)$/) {
            push @checks, "$1=$2";
        } elsif (/^\s*(\S+)\s*$/) {
            push @checks, $1;
        } else {
            print "--check_file unknown line format ignored: $_\n";
            next;
        }
    }
}

# If neither the --group, --check, --post_deployment nor --pre_deployment was
# given then treat it like '--group=full' was given.
if ($group_name eq "" && $pre_deployment == 0 && $post_deployment == 0 && scalar @checks == 0) {
    $group_name = "full";
}

push @{$result_information->{'execution_errors'}}, ();

# Find which checks are available for the user
find_available_checks();

initialize_variables_description();

if ( $show_help ) {
    show_help();
    exit 0;
}

if ($kubeconfig ne "" && -f "$kubeconfig") {
    # Set kubeconfig for any used library functions
    ADP::Kubernetes_Operations::set_used_kubeconfig_filename($kubeconfig);

    $kubeconfig = " --kubeconfig=$kubeconfig";
} elsif ($kubeconfig ne "") {
    print "The kubeconfig file does not exist\n";
    exit 1;
}

if ($group_name ne "" && $group_name !~ /^(full|post-deployment|post-upgrade|pre-deployment|pre-upgrade|verification)$/) {
    print "Not supported group '$group_name'\n";
    exit 1;
} elsif ($group_name ne "") {
    print "Health Check for Group: $group_name\n\n" unless ($json_output == 1);
    for $name (sort keys %{$group_check_matrix{$group_name}}) {
        if ($group_check_matrix{$group_name}{$name} ne "skip") {
            push @checks, $name;
        }
    }
    $result_information->{'group'} = $group_name;
}

$progress_type = lc($progress_type);
if ($progress_type eq "random") {
    # Select one of the types: 0 to 10
    $progress_type = int(rand(11));
}
if ($progress_type eq "0") {
    @progress_chars = ( '◡◡', '⊙⊙', '◠◠' );
} elsif ($progress_type eq "1") {
    @progress_chars = ( '▁', '▃', '▄', '▅', '▆', '▇', '█', '▇', '▆', '▅', '▄', '▃' );
} elsif ($progress_type eq "2") {
    @progress_chars = ( '◐', '◓', '◑', '◒' );
} elsif ($progress_type eq "3") {
    @progress_chars = ( '←', '↖', '↑', '↗', '→', '↘', '↓', '↙' );
} elsif ($progress_type eq "4") {
    @progress_chars = ( '┤', '┘', '┴', '└', '├', '┌', '┬', '┐' );
} elsif ($progress_type eq "5") {
    @progress_chars = ( '.', 'o', 'O', 'o' );
} elsif ($progress_type eq "6") {
    @progress_chars = ( "◜ ", " ◝", " ◞", "◟ " );
} elsif ($progress_type eq "7") {
    @progress_chars = ( '-', '\\', '|', '/', '-', '|', '/' );
} elsif ($progress_type eq "8") {
    @progress_chars = ( '◴', '◷', '◶', '◵' );
} elsif ($progress_type eq "9") {
    @progress_chars = ( '◰', '◳', '◲', '◱' );
} elsif ($progress_type eq "10") {
    @progress_chars = ( '⣾', '⣽', '⣻', '⢿', '⡿', '⣟', '⣯', '⣷', '⡀', '⡁', '⡂', '⡃', '⡄', '⡅', '⡆', '⡇', '⡈', '⡉', '⡊', '⡋', '⡌', '⡍', '⡎', '⡏', '⡐', '⡑', '⡒', '⡓', '⡔', '⡕', '⡖', '⡗', '⡘', '⡙', '⡚', '⡛', '⡜', '⡝', '⡞', '⡟', '⡠', '⡡', '⡢', '⡣', '⡤', '⡥', '⡦', '⡧', '⡨', '⡩', '⡪', '⡫', '⡬', '⡭', '⡮', '⡯', '⡰', '⡱', '⡲', '⡳', '⡴', '⡵', '⡶', '⡷', '⡸', '⡹', '⡺', '⡻', '⡼', '⡽', '⡾', '⡿', '⢀', '⢁', '⢂', '⢃', '⢄', '⢅', '⢆', '⢇', '⢈', '⢉', '⢊', '⢋', '⢌', '⢍', '⢎', '⢏', '⢐', '⢑', '⢒', '⢓', '⢔', '⢕', '⢖', '⢗', '⢘', '⢙', '⢚', '⢛', '⢜', '⢝', '⢞', '⢟', '⢠', '⢡', '⢢', '⢣', '⢤', '⢥', '⢦', '⢧', '⢨', '⢩', '⢪', '⢫', '⢬', '⢭', '⢮', '⢯', '⢰', '⢱', '⢲', '⢳', '⢴', '⢵', '⢶', '⢷', '⢸', '⢹', '⢺', '⢻', '⢼', '⢽', '⢾', '⢿', '⣀', '⣁', '⣂', '⣃', '⣄', '⣅', '⣆', '⣇', '⣈', '⣉', '⣊', '', '⣋', '⣌', '⣍', '⣎', '⣏', '⣐', '⣑', '⣒', '⣓', '⣔', '⣕', '⣖', '⣗', '⣘', '⣙', '⣚', '⣛', '⣜', '⣝', '⣞', '⣟', '⣠', '⣡', '⣢', '⣣', '⣤', '⣥', '⣦', '⣧', '⣨', '⣩', '⣪', '⣫', '⣬', '⣭', '⣮', '⣯', '⣰', '⣱', '⣲', '⣳', '⣴', '⣵', '⣶', '⣷', '⣸', '⣹', '⣺', '⣻', '⣼', '⣽', '⣾', '⣿' );
} elsif ($progress_type eq "none") {
    # Just set it to default value, but it will not be used
    @progress_chars = ( '◡◡', '⊙⊙', '◠◠' );
} else {
    if ($json_output == 0) {
        print "Invalid type for --progress_type ($progress_type), using default value 0\n";
    } else {
        push @{$result_information->{'execution_errors'}}, "Invalid type for --progress_type ($progress_type), using default value 0";
    }
    @progress_chars = ( '◡◡', '⊙⊙', '◠◠' );
}
# Randomize the starting position
$progress_cnt = int(rand scalar @progress_chars);
if ($json_output == 1) {
    $progress_type = "none";
    $verbose = 0;
}

$logfile = "" if $no_logfile;

if ($variables_regexp_file ne "") {
    my @lines;
    if (General::File_Operations::read_file( { "filename" => $variables_regexp_file, "output-ref" => \@lines, "ignore-empty-lines" => 1, "ignore-pattern" => '^\s*#' } ) == 0) {
        for my $line (@lines) {
            if ($line =~ /^(\S+),(\S+),(\S+)\s*$/) {
                # For example:
                # pod_utilization_memory_threshold,^eric-data-search-engine-data-,95
                $variables_regexp{$1}{$2} = $3;
            } else {
                if ($json_output == 0) {
                    print "File '$variables_regexp_file' contains the following line that has an unexpected syntax, line ignored:\n$line\n";
                } else {
                    push @{$result_information->{'execution_errors'}}, "File '$variables_regexp_file' contains the following line that has an unexpected syntax, line ignored:", "$line";
                }
            }
        }
    } else {
        print "Unable to read the file '$variables_regexp_file'\n";
        exit 1;
    }
}

if ($show_variable_values == 1) {
    show_variable_values();
}

# **************
# *            *
# * Main logic *
# *            *
# **************

# Start logging of output to file
if ($logfile) {
    my $log_dir = dirname $logfile;
    if ($log_dir ne ".") {
        unless (-d $log_dir) {
            # We don't care if it works, because the next command will fail in such case
            `mkdir -p $log_dir`;
        }
    }
    General::Logging::log_enable("$logfile");
    $result_information->{'logfile'} = $logfile;

    log_used_settings();
}

$helm_information = ADP::Kubernetes_Operations::get_helm_information($used_helm_version);
if ($helm_information) {
    $helm_executable = ${$helm_information}{'helm_executable'} . $kubeconfig;
} else {
    # No helm command found, we just assume it's called 'helm'
    $helm_executable = "helm" . $kubeconfig;
}

if ( $namespace ne "" ) {
    $namespace_check = ADP::Kubernetes_Operations::namespace_exists($namespace);
    if ( $namespace_check == 1 ) {
        # The namespace exists, now get information about the deployed software
        # to see if certain checks should be skipped.
        get_application_information();
    } else {
        # The namespace does not exist (=0) or error fetching the namespace data (=2)
        $namespace_error = 1;
        print_verbose(
            $namespace_check == 0 ? "NOTE: Namespace $namespace does not exist, all checks with namespace will be ignored.\n\n"
                                  : "NOTE: Error when checking if namespace $namespace exist, all checks with namespace will be ignored.\n\n"
        );
    }
}

General::Message_Generation::print_underlined_message(
    {
        "message" => "Progress Information",
        "underline-char" => "=",
        "return-output" => \$message,
    }
);
print_verbose("$message\n");

# Execute wanted health checks
if ( $pre_deployment ) {
    execute_pre_deployment_health_checks();
}

if ( $post_deployment ) {
    execute_post_deployment_health_checks();
}

if ($pre_deployment == 0 && $post_deployment == 0 && scalar @checks == 0) {
    # Run all tests
    push @checks, sort keys %available_checks;
}

# Initialize global results
$result_information->{'execution_time'} = 0;
$result_information->{'namespace'} = $namespace;
if ($application ne "") {
    $result_information->{'application_type'} = $application;

    # Check all release names we have found
    my %unique_release_names;
    for my $app (keys %application_information) {
        if (exists $application_information{$app}{'release_name'} && $application_information{$app}{'release_name'} ne "") {
            $unique_release_names{$application_information{$app}{'release_name'}}++;
        }
    }
    my $all_release_names = "";
    for my $release_name (sort keys %unique_release_names) {
        $all_release_names .= "$release_name\n                  ";
    }
    $all_release_names =~ s/\n\s+$//;
    $result_information->{'release_name'} = $all_release_names;

    $result_information->{'software_version'} = $application_information{$application}{'version'};
    $result_information->{'software_build'} = $application_information{$application}{'build'};
    if ($variables_user_set{'max_connections_value'} == 0) {
        # Only update the value if the default value was used
        $variables{'max_connections_value'} = ADP::Kubernetes_Operations::get_max_connections_default_value($result_information->{'application_type'}, $result_information->{'software_version'});
    }
    $result_information->{'node_name'} = (exists $application_information{$application}{'node_name'} ? $application_information{$application}{'node_name'} : "unknown");
} else {
    $result_information->{'application_type'} = "unknown";
    $result_information->{'release_name'} = "unknown";
    $result_information->{'software_version'} = "unknown";
    $result_information->{'software_build'} = "unknown";
    $result_information->{'node_name'} = "unknown";
}
$result_information->{'verdict'} = "unknown";
$result_information->{'failed_count'} = 0;
$result_information->{'skipped_count'} = 0;
$result_information->{'successful_count'} = 0;
$result_information->{'warning_count'} = 0;

parse_global_allow_list();

# Loop through all checks to be performed
$check_count = 0;
for (@checks) {
    # Special logic added because it did not work to access the $name
    # variable if the for-loop looked like this:
    #   for $name (@checks) {
    # VERY STRANGE.
    if (/^(.+)=(\S+)$/) {
        # Special format to override the default verdict for this check
        $name = $1;
        $verdict = $2;
        $result_information->{'checks'}->[$check_count]->{'name'} = $name;
        $check_execution_time{$name} = "0";
        if ($verdict =~ /^(fail|warning)$/) {
            $available_checks{$name} = $verdict;
        } elsif ($verdict eq "skip") {
            push @result_summary, "(-) '$name' check skipped because of user selection '-c $name=$verdict'";
            push @return_codes, 0;
            push @{$result_information->{'checks'}->[$check_count]->{'details'}}, ();
            $result_information->{'checks'}->[$check_count]->{'execution_time'} += 0;
            $result_information->{'checks'}->[$check_count]->{'summary'} = "check skipped because of user selection '-c $name=$verdict'";
            $result_information->{'checks'}->[$check_count]->{'verdict'} = "skipped";
            $result_information->{'skipped_count'}++;
            next;
        } else {
            print_always("Unknown verdict (=$verdict), using default value\n");
        }
    } else {
        $name = $_;
        $result_information->{'checks'}->[$check_count]->{'name'} = $name;
        $check_execution_time{$name} = "0";

        # Check if the currently deployed application should change
        # verdict type.
        if ($application ne "" || %application_information) {
            # The user either have specified an application with -a | --application
            # parameter or the application type has been detected in the deployed
            # software on the namespace.
            if ($application ne "") {
                if (exists $application_check_matrix{$application} && exists $application_check_matrix{$application}{$name}) {
                    # The application and check exist in the %application_check_matrix hash, so change the verdict.
                    $available_checks{$name} = $application_check_matrix{$application}{$name};
                }
            } else {
                # An application type has been detected, now check if the detected application exists in the
                # %application_check_matrix. Note if multiple applications are detected when entries in the
                # %application_check_matrix then only the last one will be used.
                for my $application (sort keys %application_information) {
                    if (exists $application_check_matrix{$application} && exists $application_check_matrix{$application}{$name}) {
                        # The application and check exist in the %application_check_matrix hash, so change the verdict.
                        $available_checks{$name} = $application_check_matrix{$application}{$name};
                    }
                }
            }
            # Next check if verdict has been changed to "skip".
            if (exists $available_checks{$name} && $available_checks{$name} eq "skip") {
                push @result_summary, "(-) '$name' check skipped because of entry in \%application_check_matrix";
                push @return_codes, 0;
                push @{$result_information->{'checks'}->[$check_count]->{'details'}}, ();
                $result_information->{'checks'}->[$check_count]->{'execution_time'} += 0;
                $result_information->{'checks'}->[$check_count]->{'summary'} = "check skipped because of entry in \%application_check_matrix";
                $result_information->{'checks'}->[$check_count]->{'verdict'} = "skipped";
                $result_information->{'skipped_count'}++;
                next;
            }
        }
    }

    $check_start_time = General::OS_Operations::detailed_time();

    execute_check($name);

    $check_stop_time = General::OS_Operations::detailed_time();
    $check_execution_time{$name} = $check_stop_time - $check_start_time;
    $result_information->{'checks'}->[$check_count]->{'execution_time'} += $check_execution_time{$name};
    $result_information->{'execution_time'} += $check_execution_time{$name};

    $check_count++;
}
$result_information->{'check_count'} = $check_count;

# Print summary of executed health checks
if ( @return_codes ) {
    print_verbose("\n\n");

    # Check all return code and set overall return code
    if ( grep{ 1 eq $_ } @return_codes ) {
        # One or more checks failed
        $result_information->{'verdict'} = "failed";
        $return_code = 1;
    } elsif ( grep{ 2 eq $_ } @return_codes ) {
        # One or more checks gave a warning
        $result_information->{'verdict'} = "warning";
        $return_code = 2;
    } else {
        # All checks were successful
        $result_information->{'verdict'} = "successful";
        $return_code = 0;
    }

    if ($json_output == 0) {
        print_result_details();
        print_execution_times();
        print_result_summary();
    } else {
        print General::Json_Operations::write_to_scalar(
            {
                "input-ref"     => $result_information,
                "pretty-print"  => 1,
                "sort-output"   => 1,
            }
        );
    }
}

# Stop logging of output to file
if ($logfile) {
    General::Logging::log_disable();
    if ($json_output == 0) {
        if ($logfile =~ /^\/tmp\/system_health_check_\d+\.log$/) {
            print "Detailed Logfile written to $logfile\n";
        }
    }
}


exit $return_code;

# ****************
# *              *
# * Sub routines *
# *              *
# ****************

# -----------------------------------------------------------------------------
sub build_kpi_statistics_command {
    my $cmyp_password = $variables{'cmyp_password'} ne "" ? $variables{'cmyp_password'} : "rootroot";
    my $cmyp_user = $variables{'cmyp_user'} ne "" ? $variables{'cmyp_user'} : "expert";
    my $command = dirname(abs_path $0) . "/kpi_statistics.pl";
    my $printout_file = "";
    #my $printout_file = "/local/eustone/git/5g_proto/daft/upgrade_verdict/kpi_statistics.txt";
    #my $printout_file = "/home/eustone/logs/20230330_kpi_statistics_test/kpi_statistics_20230304-20230307/kpi_statistics.txt";
    my $time_interval = $variables{'kpi_time_interval'};
    my $time_start;
    my $time_stop;

    # Change the following value to 1 to have extra log file messages that might be
    # useful for debugging logic issues.
    my $debug_messages = 1;

    # Comment out the following lines to get predictable results for debug purposes
    # $printout_file = "/proj/DSC/rebels/logs/test_printouts/kpi_test_file_1643976819.txt";

    unless (-f "$command") {
        report_failed("Unable to find the '$command' script.");
        return;
    }

    # Add logging to file if wanted
    if ($logfile ne "") {
        $command .= " --log-file=$logfile";
    }

    if ($variables{'kpi_definitions_file'} ne "") {
        $command .= " --kpi-file=$variables{'kpi_definitions_file'}";
    }

    if ($variables{'kpi_data_directory'} eq "") {
        # No data files and graphs to be created
        $command .= " --no-data-files";
    } else {
        # Create data files and graphs in this directory
        $command .= " --output-directory=$variables{'kpi_data_directory'}";
    }

    if ($printout_file eq "") {
        if ( get_namespace() eq "" ) {
            # No namespace given or error fetching the namespace, skip this health check
            return;
        }

        # Namespace given, fetch information for the specific namespaces.
        $command .= " --namespace=$namespace";

        $command .= " --cmyp-password=$cmyp_password";
        $command .= " --cmyp-user=$cmyp_user";

        if ($variables{'kpi_resolution'} ne "") {
            $command .= " --resolution $variables{'kpi_resolution'}";
        }

        if ($time_interval ne "") {
            # Check if we have a time interval or a start time specified with 'kpi_time_interval'.
            if ($time_interval =~ /^(.+) to (.+)$/) {
                # A time interval given "xxxxx to yyyyy"
                $time_start = $1;
                $time_stop = $2;

                if ($time_start !~ /^\d+$/) {
                    $time_start = General::OS_Operations::epoch_time($time_start);
                }
                if ($time_stop !~ /^\d+$/) {
                    $time_stop = General::OS_Operations::epoch_time($time_stop);
                }
                unless ($time_stop && $time_stop) {
                    # One or both times have invalid values
                    report_failed("Incorrect value given by variable 'kpi_time_interval' (=$variables{'kpi_time_interval'})\n");
                    return;
                }
                if ($time_start > $time_stop) {
                    # The values are turned around
                    ($time_start, $time_stop) = ($time_stop, $time_start);
                }

                if ($time_stop - $time_start < 60) {
                    # The time difference is less than 1 minute, so make it 1 minute
                    $command .= " --past 1m --date-offset $time_stop";
                    General::Logging::log_write("kpi_time_interval=$time_interval specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")\n") if $debug_messages;
                } else {
                    $command .= sprintf ' --past %ds --date-offset %d', ($time_stop - $time_start), $time_stop;
                    General::Logging::log_write("kpi_time_interval=$time_interval specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")\n") if $debug_messages;
                }
            } else {
                # A stop time given
                if ($time_interval =~ /^\d+$/) {
                    $time_stop = $time_interval;
                } else {
                    $time_stop = General::OS_Operations::epoch_time($time_interval);
                }

                if ($variables{'kpi_past'} ne "") {
                    # We don't care if the $time_stop value is already in the future, we hope CMYP can handle this.
                    $command .= " --past $variables{'kpi_past'} --date-offset $time_stop";
                    General::Logging::log_write("kpi_time_interval=$time_interval specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")\n") if $debug_messages;
                } elsif ($variables{'kpi_future'} ne "") {
                    if ($time_stop >= time()) {
                        # The stop time is already in the future, so we use current start time instead
                        # which might result in more or incorrect values to be returned but I guess this
                        # is better than nothing.
                        $command .= " --future $variables{'kpi_future'}";
                        General::Logging::log_write("kpi_time_interval=$time_interval specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")\n") if $debug_messages;
                    } else {
                        # Stop time is not in the future, now check how far into the future we would need
                        # to fetch data.
                        my $in_seconds = ADP::Kubernetes_Operations::convert_time_unit_to_seconds($variables{'kpi_future'});
                        my $calculated_stop_time = $time_stop + $in_seconds;
                        if ($calculated_stop_time <= time()) {
                            # The calculated stop time is still not in the future so use the specified stop time
                            # and then fetch back in time instead.
                            $command .= " --past $variables{'kpi_future'} --date-offset $calculated_stop_time";
                            General::Logging::log_write("kpi_time_interval=$time_interval specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")\n") if $debug_messages;
                        } elsif ($calculated_stop_time - time() >= $in_seconds) {
                            # The calculated time difference between calculated stop time and current time is
                            # greater than wanted number of seconds, so we need to expand the number of seconds
                            # to collect data at least up to the wanted stop time.
                            $command .= sprintf ' --future %ds', ($calculated_stop_time - time());
                            General::Logging::log_write("kpi_time_interval=$time_interval specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")\n") if $debug_messages;
                        } else {
                            # The calculated time difference between calculated stop time and current time is not
                            # greater than wanted number of seconds, so we just use the specified 'kpi_future'
                            # value.
                            $command .= " --future $variables{'kpi_future'}";
                            General::Logging::log_write("kpi_time_interval=$time_interval specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")\n") if $debug_messages;
                        }
                    }
                } else {
                    # Neither 'kpi_future' not 'kpi_past' specified, then just fetch data for the 1 minute before the specified time stamp
                    $command .= " --past 1m --date-offset $time_stop";
                    General::Logging::log_write("kpi_time_interval=$time_interval specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")\n") if $debug_messages;
                }
            }
        } else {
            # There is no 'kpi_time_interval'.
            if ($variables{'kpi_past'} ne "") {
                $command .= " --past $variables{'kpi_past'}";
                General::Logging::log_write("kpi_past specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")\n") if $debug_messages;
            } elsif ($variables{'kpi_future'} ne "") {
                $command .= " --future $variables{'kpi_future'}";
                General::Logging::log_write("kpi_future specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")\n") if $debug_messages;
            } else {
                $command .= " --past 1m";
                General::Logging::log_write("Neither kpi_past nor kpi_future specified (" . ( caller(0) )[3] . " line " . __LINE__ . ")\n") if $debug_messages;
            }
        }

        if ($variables{'kpi_prefer_recording_rule'} =~ /^(yes|1)$/i) {
            $command .= " --prefer-recording-rule";
        } elsif (version->parse( $application_information{$application}{'version'} ) >= version->parse( "1.8.0" ) ) {
            # Starting with SC 1.8.0 we also force the use of recording rules instead of metrics query
            $command .= " --prefer-recording-rule";
        }

        if ($kubeconfig ne "") {
            $command .= $kubeconfig;
        } elsif ($ENV{USER} eq "eccd") {
            # Assume that if we are user 'eccd' then we are executing on the node and there we have issues
            # with getting text on the PNG files and are seeing warnings/errors about pongo fonts.
            # So we don't generate any graphs on the cluster, only generate bash scripts that can be used
            # to later generate the PNG graphs.
            $command .= " --no-png-graph";
        }
    } else {
        $command .= " --input-file=$printout_file";
        if ($variables{'kpi_prefer_recording_rule'} =~ /^(yes|1)$/i) {
            $command .= " --prefer-recording-rule";
        }
    }

    return $command;
}

# -----------------------------------------------------------------------------
                    # Not After : Jul  8 13:57:02 2022 GMT
sub calculate_days_left {
    my $date = shift;

    my $current_time = time();                  # Current Epoch time (Seconds since 1970-01-01)
    my $future_time = `date -d '$date' +\%s`;   # Ecpoch seconds in future
    if ($?) {
        report_failed("Unable to calculate epoch time for date '$date', rc=$?\n");
        return 0;
    } else {
        return sprintf "%d", abs ( ( $future_time - $current_time ) / ( 3600 * 24 ) );
    }
}

#                  ****************************
#                  *                          *
#                  * START HEALTH CHECK LOGIC *
#                  *                          *
#                  ****************************

# -----------------------------------------------------------------------------
sub check_alarm_history {
    my $alarm_name = "";
    my %alarm_info;
    my %alarms;
    my $alarm_time_epoch;
    my $cmyp_password = $variables{'cmyp_password'} ne "" ? $variables{'cmyp_password'} : "rootroot";
    my $cmyp_user = $variables{'cmyp_user'} ne "" ? $variables{'cmyp_user'} : "expert";
    my $command = dirname(dirname(dirname(abs_path $0))) . "/expect/bin/send_command_to_ssh.exp";
    my $current_time_epoch = time();
    my @details;
    my $expected_output_format = 0;
    my %flapping_alarms;
    my $flapping_alarm_cnt = 0;
    my $length;
    my $max_key1_length = 10;
    my $max_key2_length = 15;
    my $pod_result = 0;
    my $printout_file = "";
    my $rc;
    my @result;
    my $show_details_at_success = $variables{'alarm_history_details_at_success'};

    # Comment out the following lines to get predictable results for debug purposes
    # $current_time_epoch = 1641353325;
    # $printout_file = "/proj/DSC/rebels/logs/test_printouts/alarm_history_test_file_20220105.log";

    if ($printout_file eq "") {
        if ( get_namespace() eq "" ) {
            # No namespace given or error fetching the namespace, skip this health check
            return;
        }

        unless (-f "$command") {
            report_failed("Unable to find the '$command' script.");
            return;
        }

        if ($cmyp_ip eq "") {
            # We have no cached value, so try to find the value
            $cmyp_ip = ADP::Kubernetes_Operations::get_cmyp_ip($namespace);
            unless ($cmyp_ip) {
                report_failed("Failed to fetch CMYP IP_address.");
                return;
            }
        }

        # Add more parameter for fetching alarm history from CMYP
        $command .= " --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=30 --command='show alarm-history'";

        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
    } else {
        $rc = General::File_Operations::read_file(
            {
                "filename"              => $printout_file,
                "output-ref"            => \@result,
            }
        );
    }

    if ( $rc == 0 ) {
        my $command_found = 0;
        my $prompt = "";
        # Parse all alarms and store then into the %alarms hash, sorted by {<alarm name> - <specific-problem>}{<last-event-time>}
        for (@result) {
            if ($command_found) {
                if (/^$prompt.+/) {
                    # Stop handling the output when a new prompt is detected in the printout
                    last;
                }
                if (/^(\S+)\s*$/) {
                    # Start of the alarm
                    $alarm_name = $1;
                    if (%alarm_info) {
                        # We have already collected data for a previous alarm, now save this information into a new hash
                        my $key1 = (keys %alarm_info)[0];
                        my $key2 = exists $alarm_info{$key1}{'specific-problem'} ? $alarm_info{$key1}{'specific-problem'} : "UNKNOWN";
                        my $key3 = exists $alarm_info{$key1}{'last-event-time'} ? $alarm_info{$key1}{'last-event-time'} : "UNKNOWN";
                        push @{$alarms{"$key1 - $key2"}{$key3}}, $alarm_info{$key1};
                        $length = length("$key1 - $key2");
                        $max_key1_length = $length if ($length > $max_key1_length);
                        $length = length($key3);
                        $max_key2_length = $length if ($length > $max_key2_length);
                    }
                    undef %alarm_info;
                    next;
                } elsif (/^\s+(\S+)\s+(.+)/) {
                    # Alarm information
                    $alarm_info{$alarm_name}{$1} = $2;
                }
            } elsif (/^(.+)show alarm-history\s*$/) {
                $command_found = 1;
                $prompt = $1;
            }
        }
        if (%alarm_info) {
            # We have already collected data for a previous alarm, now save this information into a new hash
            my $key1 = (keys %alarm_info)[0];
            my $key2 = exists $alarm_info{$key1}{'specific-problem'} ? $alarm_info{$key1}{'specific-problem'} : "UNKNOWN";
            my $key3 = exists $alarm_info{$key1}{'last-event-time'} ? $alarm_info{$key1}{'last-event-time'} : "UNKNOWN";
            push @{$alarms{"$key1 - $key2"}{$key3}}, $alarm_info{$key1};
        }

        # Check all the alarms and store alarms that have been raised or ceased within a specific time interval
        push @details, "\nAll detected alarms:\n\n";
        push @details, sprintf "%-${max_key1_length}s  %-${max_key2_length}s  %-13s  %-13s  %-13s  %s\n", "Alarm Type", "Alarm Timestamp", "Current epoch", "Alarm epoch", "Time Diff", "active-severity";
        push @details, sprintf "%s  %s  %s  %s  %s  %s\n", "-"x${max_key1_length}, "-"x${max_key2_length}, "-"x13, "-"x13, "-"x13, "-"x15;
        for my $key1 (sort keys %alarms) {
            for my $key2 (sort keys %{$alarms{$key1}}) {
                $alarm_time_epoch = `date --date='$key2' +\%s`;
                $alarm_time_epoch =~ s/[\r\n]//g;
                my @temp_array = @{$alarms{$key1}{$key2}};
                for (my $i=0; $i<=$#temp_array; $i++) {
                    my $temp_hash_ref = $temp_array[$i];
                    if ($current_time_epoch - $alarm_time_epoch <= $variables{'alarm_history_flapping_threshold'}) {
                        # The alarm has been raised or ceased within the threshold limit, count this alarm
                        push @{$flapping_alarms{$key1}}, $temp_hash_ref;
                        push @details, sprintf "%-${max_key1_length}s  %-${max_key2_length}s  %13d  %13d  %13d  %-15s%s\n", $key1, $key2, $current_time_epoch, $alarm_time_epoch, ($current_time_epoch - $alarm_time_epoch), $temp_hash_ref->{'active-severity'}, "<---- Within the threshold time";
                    } else {
                        push @details, sprintf "%-${max_key1_length}s  %-${max_key2_length}s  %13d  %13d  %13d  %s\n",      $key1, $key2, $current_time_epoch, $alarm_time_epoch, ($current_time_epoch - $alarm_time_epoch), $temp_hash_ref->{'active-severity'};
                    }
                }
            }
        }
        push @details, "\n";

        # Now check if we have any flapping alarms i.e. 3 or more within the specific time interval
        for my $key1 (sort keys %flapping_alarms) {
            if ($key1 =~ /^(\S+)/) {
                $alarm_name = $1;
            } else {
                $alarm_name = "UNKNOWN";
            }
            if (scalar @{$flapping_alarms{$key1}} > 2) {
                # We have alarms that has been repeated more than 2 times
                my $line = sprintf "This alarm has been reissued %d times, this is the last alarm:\n$alarm_name\n", scalar @{$flapping_alarms{$key1}};
                # Take the last reported alarm
                my $temp_hash_ref = @{$flapping_alarms{$key1}}[-1];
                for my $key2 (sort keys %{$temp_hash_ref}) {
                    $line .= sprintf "     %-18s  %s\n", $key2, $temp_hash_ref->{$key2};
                }
                push @details, "$line\n";
                $flapping_alarm_cnt++;
            }
        }
        if ($flapping_alarm_cnt > 0) {
            report_failed("One or more alarms has been reissued >2 times in the last $variables{'alarm_history_flapping_threshold'} seconds.\n" . join "", @details);
        } elsif ($show_details_at_success =~ /^(yes|1)$/i && @details) {
            # We have some details information to report
            report_details("\nUsed threshold: $variables{'alarm_history_flapping_threshold'} seconds\n" . join "", @details);
        }

    } else {
        if (scalar @result > 0) {
            report_failed("Command '$command' failed.\n" . join "\n", @result);
        } else {
            report_failed("Command '$command' failed and returned no data.\n");
        }
    }
}

# -----------------------------------------------------------------------------
sub check_alarms {
    my $command = dirname(abs_path $0) . "/alarm_check.pl";
    my @details;
    my $expected_output_format = 0;
    my $pod_result = 0;
    my $rc;
    my @result;
    my $tempfile = "";

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    unless (-f "$command") {
        report_failed("Unable to find the '$command' script.");
        return;
    }

    # Namespace given, fetch information for the specific namespaces.
    $command .= " --namespace=$namespace";

    # Add logging to file if wanted
    if ($logfile ne "") {
        $command .= " --log_file=$logfile";
    }

    # Use a whitelist to ignore certain alarms
    $command .= " --use_allowlist";

    if ($variables{'alarms_allow_file'} ne "" && -f "$variables{'alarms_allow_file'}") {
        $command .= " --allow_file=" . abs_path $variables{'alarms_allow_file'};
    } elsif ($variables{'alarms_allow_file'} ne "") {
        report_failed("Unable to find the file 'alarms_allow_file' $variables{'alarms_allow_file'}.");
        return;
    } else {
        # No special allow file specified, use the default information
        if (@allow_list_alarms) {
            # Default file found and parsed into a data structure.
            # Now save this file as a temporary file to be used when calling the alarm_check.pl script
            # and then delete it afterwards.
            $tempfile = General::File_Operations::tempfile_create();
            if ($tempfile ne "") {
                my @allow_list = General::Json_Operations::write_to_array({ "input-ref" => \@allow_list_alarms, "pretty-print" => 1, "strip-eol" => 0 });
                if (@allow_list) {
                    $rc = General::File_Operations::write_file(
                        {
                            "filename"      => $tempfile,
                            "output-ref"    => \@allow_list,
                            "no-eol-char"  => 1,
                        }
                    );
                    if ($rc == 0) {
                        General::Logging::log_write("Used allow list in temporary file $tempfile:\n" . join "", @allow_list) if $logfile;
                        $command .= " --allow_file=$tempfile";
                    } else {
                        General::Logging::log_write("Failed to write allow list to temporary file $tempfile.\nThe default file from alarm_check.pl will be used.\n") if $logfile;
                    }
                } else {
                    General::Logging::log_write("Failed parse the json data struction in variable \@allow_list_alarms.\nThe default file from alarm_check.pl will be used.\n") if $logfile;
                }
            } else {
                General::Logging::log_write("Failed to create temporary file for the allow list.\nThe default file from alarm_check.pl will be used.\n") if $logfile;
            }
        } else {
            General::Logging::log_write("No default allow list found.\nThe default file from alarm_check.pl will be used.\n") if $logfile;
        }
    }


    if ($variables{'alarms_full_alarm_list'} eq "yes") {
        $command .= " --full_alarm_list";
    }

    if ($variables{'alarms_use_tls'} eq "yes") {
        $command .= " --use_tls";
    }

    $command .= " --summary";

    if ($kubeconfig ne "") {
        $command .= $kubeconfig;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );

    if ( $rc == 0 ) {
        if (scalar @result > 0 && grep /(All found alarms are in the whitelist, alarms ignored\.|All Alarms are in the Allow List:)/, @result) {
            report_warning(join "\n", @result);
        }
    } else {
        if (scalar @result > 0) {
            report_failed("Command '$command' failed.\n" . join "\n", @result);
        } else {
            report_failed("Command '$command' failed and returned no data.\n");
        }
    }

    if ($tempfile ne "") {
        General::File_Operations::tempfile_delete($tempfile);
    }
}

# -----------------------------------------------------------------------------
sub check_cassandra_cluster_status {
    my @faults;
    my @pod_names;
    my $rc;
    my @result;

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    @pod_names = ADP::Kubernetes_Operations::get_pod_names(
        {
            "namespace"         => $namespace,
            "pod-include-list"  => [ 'eric-data-wide-column-database-cd-\d+$', 'eric-data-wide-column-database-cd-.+-rack1', 'eric-bsf-wcdb-cd-(\d+|datacenter.+)' ],
            "hide-output"       => 1,
        }
    );
    if ( @pod_names != 0 ) {
        for my $pod_name ( @pod_names ) {

            print_verbose("  Fetching cassandra status from pod $pod_name\n");

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "kubectl -n $namespace exec -it $pod_name -c cassandra" . $kubeconfig . " -- nodetool status",
                    "hide-output"   => 1,
                    "raw-output"    => 1,
                    "return-output" => \@result,
                }
            );

            if ( $rc == 0 && @result != 0 ) {
                #   Datacenter: datacenter1
                #   =======================
                #   Status=Up/Down
                #   |/ State=Normal/Leaving/Joining/Moving
                #   --  Address          Load       Tokens       Owns (effective)  Host ID                               Rack
                #   UN  192.168.110.221  15.02 MiB  32           100.0%            a20322d5-4d46-41bd-ba97-2e0255ec3b31  rack1
                #   UN  192.168.167.102  15.04 MiB  32           100.0%            43f13925-3b87-493e-8f16-02e6fed97da8  rack1
                #
                #        or
                #
                #   Datacenter: datacenter1
                #   =======================
                #   Status=Up/Down
                #   |/ State=Normal/Leaving/Joining/Moving
                #   --  Address          Load       Tokens       Owns    Host ID                               Rack
                #   UN  192.168.118.129  105.54 KiB  32           ?       7fd36a6b-7cad-46b1-b36c-a63d647591ae  rack1
                #   UN  192.168.22.247   136.57 KiB  32           ?       288e2673-42b0-42f2-b293-4e28e1fb7b09  rack1
                #
                #   Note: Non-system keyspaces don't have the same replication settings, effective ownership information is meaningless

                my $found_status = 0;
                for ( @result ) {
                    if ( /^([UD][NLJM])\s+.+/ ) {
                        # UN  192.168.110.221  15.02 MiB  32           100.0%            a20322d5-4d46-41bd-ba97-2e0255ec3b31  rack1
                        $found_status++;
                        if ( $1 ne "UN" ) {
                            # Not Up and Normal
                            push @faults, "Status not 'Up' and/or State not 'Normal' for cassandra pod $pod_name.\n" . (join "", @result) . "\n";
                            last;
                        }
                    }
                }
                if ( $found_status == 0 ) {
                    push @faults, "No Status and State information found for cassandra pod $pod_name.\n" . (join "", @result) . "\n";
                }
            } else {
                push @faults, "kubectl -it exec command failed / no data returned for cassandra pod $pod_name.\n" . (join "", @result) . "\n";
            }
        }
        if (@faults) {
            # Report faults
            report_failed(join "", @faults);
        }
    } else {
        report_skipped("Check skipped because no eric-data-wide-column-database-cd-xx or eric-bsf-wcdb-cd-xx pods found.\n");
    }
}

# -----------------------------------------------------------------------------
sub check_certificate_validity {
    my @cert_commands = ();
    my $connect_cnt = 0;
    my $connect_error_cnt = 0;
    my $days_left;
    my @error;
    my @faults;
    my $heading;
    my %node_info;
    my $rc;
    my @result;
    my @warnings;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get nodes -o wide --no-headers" . $kubeconfig,
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );
    if ( $rc == 0 && @result != 0 ) {
        for ( @result ) {
            if (/^(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+.+/) {
                # master-0-eccd-sc-pikachu                Ready   control-plane,master   26h    v1.21.1   fd08::9    <none>   SUSE Linux Enterprise Server 15 SP2   5.3.18-24.67-default   containerd://1.4.4
                $node_info{$1}{'ip'} = $6;
                $node_info{$1}{'role'} = $3;
            }
        }
    } else {
        report_failed("kubectl get nodes command failed / no data returned.\n" . join "\n", @result);
        return;
    }

    # Check Cluster Certificate expiration time on the master nodes
    for my $node (sort keys %node_info) {
        next unless ($node_info{$node}{'role'} =~ /(master|control-plane)/);
        $connect_cnt++;

        print_verbose("  Fetching CCD certificate info from node $node\n");

        # For each master node check the certificate validity times
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "ssh -o ConnectTimeout=2 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@$node_info{$node}{'ip'} 'sudo -n /usr/local/bin/kubeadm certs check-expiration'",
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@error,
            }
        );
        #   CERTIFICATE                EXPIRES                  RESIDUAL TIME   CERTIFICATE AUTHORITY   EXTERNALLY MANAGED
        #   admin.conf                 Mar 22, 2022 12:46 UTC   364d                                    no
        #   apiserver                  Mar 22, 2022 12:46 UTC   364d            ca                      no
        #   apiserver-kubelet-client   Mar 22, 2022 12:46 UTC   364d            ca                      no
        #   controller-manager.conf    Mar 22, 2022 12:46 UTC   364d                                    no
        #   front-proxy-client         Mar 22, 2022 12:46 UTC   364d            front-proxy-ca          no
        #   scheduler.conf             Mar 22, 2022 12:46 UTC   364d                                    no
        #
        #   CERTIFICATE AUTHORITY   EXPIRES                  RESIDUAL TIME   EXTERNALLY MANAGED
        #   ca                      Oct 06, 2033 07:39 UTC   12y             no
        #   front-proxy-ca          Mar 20, 2031 11:23 UTC   9y              no

        if ( $rc == 0 && @result != 0 ) {
            for ( @result ) {
                if ( /^CERTIFICATE\s+EXPIRES\s+RESIDUAL TIME\s+CERTIFICATE AUTHORITY\s+EXTERNALLY MANAGED\s*$/ ) {
                    $heading = $_;
                } elsif ( /^CERTIFICATE AUTHORITY\s+EXPIRES\s+RESIDUAL TIME\s+EXTERNALLY MANAGED\s*$/ ) {
                    $heading = $_;
                } elsif ( /^(\S+)\s+.+\s+(\d+)(d|y)\s+.+/ ) {
                    $days_left = $3 eq "y" ? ($2 * 365) : $2;
                    if ( $days_left <= $variables{'certificate_validity_days_left'} ) {
                        push @faults, "You only have $days_left days left before certificate $1 on node $node expire.\n$heading\n$_\n";
                    }
                }
            }
        } else {
            if (grep /Permission denied \(publickey,password,keyboard-interactive\)/, @error) {
                push @warnings, "\nFailed to connect to node $node, no or wrong public key used when fetching CCD kublet certificate.\n" . (join "\n", @error, @result) . "\n";
                $connect_error_cnt++;
            } elsif (grep /Permission denied \(publickey,keyboard-interactive\)/, @error) {
                push @warnings, "\nFailed to connect to node $node, no or wrong public key used when fetching CCD kublet certificate.\n" . (join "\n", @error, @result) . "\n";
                $connect_error_cnt++;
            } elsif (grep /Connection timed out/, @error) {
                push @warnings, "\nFailed to connect to node $node, Connection timed out when fetching CCD kublet certificate.\n" . (join "\n", @error, @result) . "\n";
                $connect_error_cnt++;
            } else {
                push @faults, "\nFailed to fetch CCD certificate expiration from node $node.\n" . (join "\n", @error, @result) . "\n";
            }
            if ($connect_cnt == 1 && $connect_error_cnt == 1) {
                push @warnings, "\nFailed to connect to the first node, Assuming the other nodes will also fail so stopping the check.\n";
                last;
            }
        }
    }

    # Check Kubernetes client and kubelet certificates on all nodes
    if ($connect_cnt == 1 && $connect_error_cnt == 1) {
        # Failed to connect to the first two nodes, skip the time difference check
    } else {
        for my $node (sort keys %node_info) {
            # For each node check the certificate validity times

            print_verbose("  Fetching client and kubelet certificate info from node $node\n");

            # First file a list of files because it depends on the ECCD version what the filename is
            print_verbose("    Fetching list of certificate files\n");

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "ssh -o ConnectTimeout=2 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@$node_info{$node}{'ip'} 'find /var/lib/kubelet/pki/'",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                    "return-stderr" => \@error,
                }
            );
            if ( $rc == 0 && @result != 0 ) {
                for ( @result ) {
                    if ( /^.+\/kubelet-(client|server)-current\.pem$/ ) {
                        push @cert_commands, "sudo -n openssl x509 -in $_ -text -noout";
                    } elsif ( /^.+\/kubelet\.crt$/ ) {
                        push @cert_commands, "openssl x509 -in $_ -text -noout";
                    }
                }
            } else {
                if (grep /Permission denied \(publickey,password,keyboard-interactive\)/, @error) {
                    push @warnings, "\nFailed to connect to node $node, no or wrong public key used when fetching list of certificate files.\n" . (join "\n", @error, @result) . "\n";
                } else {
                    push @faults, "\nFailed to fetch list of certificate files from node $node.\n" . (join "\n", @error, @result) . "\n";
                }
                next;
            }

            # Next for each file decode the certificate
            print_verbose("    Decoding certificate file(s)\n");
            for my $command ( @cert_commands ) {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "ssh -o ConnectTimeout=2 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@$node_info{$node}{'ip'} '$command'",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                        "return-stderr" => \@error,
                    }
                );
                if ( $rc == 0 && @result != 0 ) {
                    for ( @result ) {
                        if ( /^\s*Not After\s+:\s+(.+)/ ) {
                            # Not After : Jul  8 13:57:02 2022 GMT
                            $days_left = calculate_days_left( $1 );
                            if ( $days_left <= $variables{'certificate_validity_days_left'} ) {
                                push @faults, "You only have $days_left days left before client and kubelet certificate on node $node expire.\n$_\n";
                            }
                        }
                    }
                } else {
                    push @faults, "\nFailed to fetch client and kubelet certificate expiration from node $node.\nDecode command: $command\n" . (join "\n", @error, @result) . "\n";
                }
            }
        }
    }

    if (scalar @faults != 0) {
        if (scalar @warnings != 0) {
            # Report both warnings and faults
            report_failed(join "", @warnings, @faults);
        } else {
            # Report faults
            report_failed(join "", @faults);
        }
    } elsif (scalar @warnings != 0) {
        # Mark test as warning if we have 1 or more warnings
        report_warning(join "", @warnings);
    }
}

# -----------------------------------------------------------------------------
sub check_component_info {
    my @failure_info;
    my $line;
    my $namespace;
    my $pod_name;
    my $rc;
    my @result;
    my $success = 1;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get all --all-namespaces -o wide" . $kubeconfig,
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
        }
    );

    # rc check
    if ( $rc == 0 && @result != 0 ) {
        # Loop over command results and find components where all the numbers don't match
        foreach( @result ) {
            next if ( /^NAMESPACE\s+NAME/ );
            $line = $_;

            # Check if we should look at this namespace
            if ( /^(\S+)\s+(daemonset|deployment\.apps|replicaset\.apps|statefulset\.apps|job\.batch|service|cronjob\.batch)/ ) {
                $namespace = $1;
                if ( $namespace ne $namespace ) {
                    # The namespace is not the SC namespace, check if other namespaces should be checked
                    if ( $variables{'component_info_other_namespaces'} eq "no" && $variables{'component_info_system_namespaces'}  eq "no" ) {
                        # No other namespaces wanted, so ignore this line
                        next;
                    } elsif ( $variables{'component_info_other_namespaces'} eq "no" && $variables{'component_info_system_namespaces'}  eq "yes" ) {
                        # We also want system namespaces
                        unless ( $namespace =~ /^(default|etcd|ingress-nginx|kube-node-lease|kube-public|kube-system|monitoring)$/ ) {
                            # The namespace is not one of the system defined namespaces, so ignore this line
                            next;
                        }
                    }
                    # If we reach here then either we should check other (=all) namespaces or the namespace is a system defined
                    # namespace and we should check it.
                }
            }

            if ( /^\S+\s+daemonset\.apps\S+\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+.+/ ) {
                #   NAMESPACE     NAME                                                            DESIRED   CURRENT   READY   UP-TO-DATE   AVAILABLE   NODE SELECTOR                     AGE
                #   eiffelesc     daemonset.apps/eric-log-shipper                                 6         6         6       6            6           <none>                            132m
                unless ( $1 == $2 && $1 == $3 && $1 == $4 && $1 == $5 ) {
                    push @failure_info, "All numbers are not the same: $_";
                    $success = 0;
                }
            } elsif ( /^\S+\s+deployment\.apps\S+\s+(\d+)\/(\d+)\s+(\d+)\s+(\d+)\s+.+/ ) {
                #   NAMESPACE       NAME                                                                READY   UP-TO-DATE   AVAILABLE   AGE
                #   eiffelesc       deployment.apps/eric-bsf-diameter                                   2/2     2            2           24h
                unless ( $1 == $2 && $1 == $3 && $1 == $4 ) {
                    push @failure_info, "All numbers are not the same: $_";
                    $success = 0;
                }
            } elsif ( /^\S+\s+replicaset\.apps\S+\s+(\d+)\s+(\d+)\s+(\d+)\s+.+/ ) {
                #   NAMESPACE       NAME                                                                           DESIRED   CURRENT   READY   AGE
                #   eiffelesc       replicaset.apps/eric-bsf-diameter-84868764d9                                   2         2         2       132m
                unless ( $1 == $2 && $1 == $3 ) {
                    push @failure_info, "All numbers are not the same: $_";
                    $success = 0;
                }
            } elsif ( /^\S+\s+statefulset\.apps\S+\s+(\d+)\/(\d+)\s+.+/ ) {
                #   NAMESPACE     NAME                                                          READY   AGE
                #   eiffelesc     statefulset.apps/eric-ctrl-bro                                1/1     132m
                unless ( $1 == $2 ) {
                    push @failure_info, "All numbers are not the same: $_";
                    $success = 0;
                }
            } elsif ( /^\S+\s+job\.batch\/(\S+)\s+(\d+)\/(\d+)\s+.+/ ) {
                #   NAMESPACE     NAME                                                   COMPLETIONS   DURATION   AGE
                #   eiffelesc     job.batch/eric-cm-mediator-key-init                    1/1           2m44s      132m
                #   eiffelesc     job.batch/eric-data-search-engine-curator-1625673240   1/1           10s        2m32s
                unless ( $2 == $3 ) {
                    if ($2 == 0 && $3 == 1) {
                        # For the following pods the jobs might still be running
                        next if ($1 =~ /^eric-data-search-engine-curator-\d+$/);
                        next if ($1 =~ /^eric-data-wide-column-database-cd-tls-restarter-\d+$/);
                        next if ($1 =~ /^eric-bsf-wcdb-cd-tls-restarter-\d+$/);
                    }
                    push @failure_info, "All numbers are not the same: $_";
                    $success = 0;
                }
            }
        }
        if ( $success == 0 ) {
            report_failed(join "", @failure_info);
        }

    } else {
        report_failed("kubectl get all command failed / no information found.\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_config_dump {
    my $cmyp_password = $variables{'cmyp_password'} ne "" ? $variables{'cmyp_password'} : "rootroot";
    my $cmyp_user = $variables{'cmyp_user'} ne "" ? $variables{'cmyp_user'} : "expert";

    get_cmyp_config($variables{'config_dump_type'}, $cmyp_user, $cmyp_password, 1);
}

# -----------------------------------------------------------------------------
sub check_config_validity_bsf {
    unless (%cnf) {
        # We don't have anything cached
        %cnf = get_cnf();
    }
    if (exists $cnf{'bsf'}) {
        get_cmyp_config("bsf-function", $variables{'config_validity_bsf_user'}, $variables{'config_validity_bsf_password'}, 0);
    } else {
        # The CNF does not exist, skip this test
        report_skipped("CNF 'bsf' is not deployed\n");
        return;
    }
}

# -----------------------------------------------------------------------------
sub check_config_validity_scp {
    unless (%cnf) {
        # We don't have anything cached
        %cnf = get_cnf();
    }
    if (exists $cnf{'scp'}) {
        get_cmyp_config("scp-function", $variables{'config_validity_scp_user'}, $variables{'config_validity_scp_password'}, 0);
    } else {
        # The CNF does not exist, skip this test
        report_skipped("CNF 'scp' is not deployed\n");
        return;
    }
}

# -----------------------------------------------------------------------------
sub check_config_validity_sepp {
    unless (%cnf) {
        # We don't have anything cached
        %cnf = get_cnf();
    }
    if (exists $cnf{'sepp'}) {
        get_cmyp_config("sepp-function", $variables{'config_validity_sepp_user'}, $variables{'config_validity_sepp_password'}, 0);
    } else {
        # The CNF does not exist, skip this test
        report_skipped("CNF 'sepp' is not deployed\n");
        return;
    }
}

# -----------------------------------------------------------------------------
sub check_core_dumps {
    get_core_dumps("all");
}

# -----------------------------------------------------------------------------
sub check_dced_data_sync {
    my @all_results = ();
    my $deviation;
    my $pod_name;
    my @pod_names = ();
    my $rc;
    my @result;
    my @revisions = ();

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    @pod_names = ADP::Kubernetes_Operations::get_pod_names(
        {
            "namespace"         => $namespace,
            "pod-include-list"  => [ 'eric-data-distributed-coordinator-ed-\d+' ],
            "hide-output"       => 1,
        }
    );

    for $pod_name ( @pod_names ) {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "kubectl $kubeconfig exec -n $namespace $pod_name -c dced -- bash -c 'export ETCDCTL_ENDPOINTS=https://localhost:2379 ; etcdctl endpoint status --insecure-skip-tls-verify=true -w json'",
                "hide-output"   => 1,
                "raw-output"    => 1,
                "return-output" => \@result,
            }
        );

        # Loop over command results and find the revision values
        if ( @result != 0 ) {
            foreach(@result) {
                push @all_results, $_;
                if ( $_ =~ m/.+"revision":(\d+).+/ ) {
                    # Look for this and they all should be the same value or within the a specific deviation                                      ---------------
                    # [{"Endpoint":"https://localhost:2379","Status":{"header":{"cluster_id":2104678578865624298,"member_id":4962605495301377754,"revision":69784,"raft_term":81},"version":"3.4.16","dbSize":528384,"leader":4962605495301377754,"raftIndex":81081,"raftTerm":81,"raftAppliedIndex":81081,"dbSizeInUse":512000}}]
                    push @revisions, $1;
                }
            }
        } else {
            report_failed("Command failed.\n" . join "", @result);
            return;
        }
    }

    if (scalar @revisions > 1 && scalar @revisions == scalar @pod_names) {
        for (my $i=1; $i<=$#revisions; $i++) {
            $deviation = abs($revisions[0] - $revisions[$i]);
            if ($deviation > $variables{'dced_data_sync_max_deviation'}) {
                report_failed("The revision deviation between index 0 and $i is $deviation which is > allowed deviation of $variables{'dced_data_sync_max_deviation'}.\n" . join "", @all_results);
                return;
            }
        }
    } else {
        report_warning("Did not find the expected number of revision values.\n" . join "", @all_results);
    }
}

# -----------------------------------------------------------------------------
sub check_docker_version {
    my $container_command = ADP::Kubernetes_Operations::get_docker_or_nerdctl_command(1);  # Hide status and warning/error messages from the user
    my $docker_client_version;
    my $rc;
    my @result;

    if ($container_command ne "" && $container_command !~ /docker/) {
        report_skipped("Command '$container_command' is used instead of 'docker'\n");
        return;
    } elsif ($container_command eq "") {
        report_failed("Command 'docker' not found, or require sudo with interactive password prompt.\n");
        return;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "$container_command version --format {{.Client.Version}}",
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
        }
    );

    # Loop over command results and find docker version, command get permission denied so skip
    # rc check
    if ( @result != 0 ) {
        foreach(@result) {
            if ( $_ =~ m/^([0-9]+\.[0-9]+\.[0-9]+).*/ ) {
                $docker_client_version = $1;
                last;
            } elsif ( $_ =~ /^sudo: a password is required/ ) {
                report_warning("A password is required to access the docker version.\n");
                return;
            }
        }
        if ( $docker_client_version && version->parse( $docker_client_version ) >= version->parse( $variables{'docker_version_limit'} ) ) {
            # OK, do nothing
        } elsif ( $docker_client_version ) {
            report_failed("Docker version $docker_client_version found but expected to be $variables{'docker_version_limit'} or higher.\n" . join "", @result);
        } else {
            report_failed("No docker versions found.\n" . join "", @result);
        }

    } else {
        report_failed("Command failed / docker not found.\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_ecfe_network_status {
    my $check_bfd_sessions = 0;
    my $command;
    my @failure_info;
    my $found_cnt;
    my $pod_name;
    my @pod_names;
    my $rc;
    my @result;

    # Get all eric-tm-external-connectivity-frontend-speaker-xxxx PODs
    $command = "kubectl get pods -lcomponent=speaker -n kube-system --no-headers"  . $kubeconfig;
    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@pod_names,
        }
    );

    # Loop over the pods checking BFD and BGP status
    if ( @pod_names != 0 ) {
        for (@pod_names) {
            if ( $_ =~ m/^No resources found in kube-system namespace/ ) {
                report_skipped("No ecfe present\n");
                last;
            }
            if ( $_ =~ m/^(\S+).*/ ) {
                $pod_name = $1;

                print_verbose("  Fetching BFD and BGP protocol status from pod $pod_name\n");

                $check_bfd_sessions = 0;
                $command = "kubectl exec -n kube-system -ti $pod_name -c speaker" . $kubeconfig . " -- birdcl show protocols";
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => $command,
                        "hide-output"   => 1,
                        "raw-output"    => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc == 0 && @result != 0) {
                    # When BFD is used:
                    # -----------------
                    # BIRD v2.0.6 ready.
                    # Name       Proto      Table      State  Since         Info
                    # vip_v4     Static     master4    up     2022-02-03
                    # vip_v6     Static     master6    up     2022-02-03
                    # device1    Device     ---        up     2022-02-03
                    # bfd1       BFD        ---        up     11:39:04.689

                    # When BGP is used:
                    # -----------------
                    # BIRD v2.0.6 ready.
                    # Name       Proto      Table      State  Since         Info
                    # vip_v4     Static     master4    up     2022-01-21
                    # vip_v6     Static     master6    up     2022-01-21
                    # device1    Device     ---        up     2022-01-21
                    # bgp1       BGP        ---        up     2022-01-21    Established
                    # bgp2       BGP        ---        up     2022-01-21    Established
                    # bgp3       BGP        ---        up     2022-01-21    Established
                    # bgp4       BGP        ---        up     2022-01-21    Established
                    $found_cnt = 0;
                    for (@result) {
                        if (/^\S+\s+BFD\s+\S+\s+(\S+)\s+\S+.*$/) {
                            $found_cnt++;
                            if ($1 ne "up") {
                                push @failure_info, "One or more BFD protocols not in expected State=up.\n" . join "", @result;
                                last;
                            }
                            $check_bfd_sessions = 1;
                        } elsif (/^\S+\s+BGP\s+\S+\s+(\S+)\s+\S+\s+(\S+).*$/) {
                            $found_cnt++;
                            if ($1 ne "up") {
                                push @failure_info, "One or more BGP protocols not in expected State=up.\n" . join "", @result;
                                last;
                            } elsif ($2 ne "Established") {
                                push @failure_info, "One or more BGP protocols not in expected Info=Established.\n" . join "", @result;
                                last;
                            }
                        }
                    }
                    if ($found_cnt == 0) {
                        push @failure_info, "No expected BFD or BGP protocols found.\n" . join "", @result;
                    }
                } else {
                    push @failure_info, "Command failed or no data returned: $command\n" . join "", @result;
                }

                if ($check_bfd_sessions == 0) {
                    # No BFD protocols found, nothing more to check
                    next;
                }

                print_verbose("  Fetching BFD session status from pod $pod_name\n");

                $command = "kubectl exec -n kube-system -ti $pod_name" . $kubeconfig . " -- birdcl show bfd sessions";
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => $command,
                        "hide-output"   => 1,
                        "raw-output"    => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc == 0 && @result != 0) {
                    $found_cnt = 0;
                    for (@result) {
                        next if (/^IP address\s+Interface\s+State\s+Since\s+Interval\s+Timeout\s*$/);
                        if (/^\S+\s+\S+\s+(\S+)\s+\S+\s+\S+\s+\S+\s*$/) {
                            # BIRD v2.0.6 ready.
                            # bfd1:
                            # IP address                Interface  State      Since         Interval  Timeout
                            # 10.155.107.227            eth1       Up         2021-07-05      0.500    2.500
                            # 10.155.107.226            eth1       Up         2021-07-05      0.500    2.500
                            # 10.221.169.146            eth2       Up         2021-07-05      0.500    2.500
                            # 10.221.169.147            eth2       Up         2021-07-05      0.500    2.500
                            $found_cnt++;
                            if ($1 ne "Up") {
                                push @failure_info, "One or more BFD interfaces not in expected State=Up.\n" . join "", @result;
                                last;
                            }
                        }
                    }
                    if ($found_cnt == 0) {
                        push @failure_info, "No expected State data returned.\n" . join "", @result;
                    }
                } else {
                    push @failure_info, "Command failed or no data returned:$command\n" . join "", @result;
                }
            }
        }
        if (@failure_info) {
            report_failed(join "", @failure_info);
        }
    } else {
        report_failed("Command failed or no pods returned: $command\n" . join "", @pod_names);
    }
}

# -----------------------------------------------------------------------------
sub check_endpoints {
    my $kubectl_get_endpoints = 0;
    my $rc;
    my @result;

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get endpoints -n $namespace -o jsonpath=\'{range .items[*]}{.metadata.name}{\"\\t\"}{.subsets}{\"\\n\"}\'" . $kubeconfig,
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
        }
    );

    if ( $rc == 0 && @result != 0 ) {
        foreach( @result ) {
            if ( $_ =~ m/^(\S+)\s*(\S+notReadyAddresses:.*).*$/ ) {
               unless ( $1 =~ m/^(eric-bsf-cert-notifier|eric-bsf-diameter|eric-bsf-worker|eric-scp-cert-notifier|eric-scp-worker|eric-sepp-cert-notifier|eric-sepp-worker)$/ ) {
                   $kubectl_get_endpoints = 1;
               }
            }

        }

        if ( $kubectl_get_endpoints == 1 ) {
            report_failed("kubectl get endpoints command failed / some endpoint in state notReadyAddressess in namespace $namespace.\n" . join "", @result);
        }

    } else {
        report_failed("kubectl get endpoints command failed / no endpoints found for namespace $namespace.\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_events {
    my $command = "kubectl get events -o json" . $kubeconfig;
    my $count;
    my @failure_info;
    my $indent = sprintf "%s", " "x56;
    my $line;
    my $message;
    my $pod_name;
    my $printout_file = "";
    my $rc;
    my @result;
    my @std_error;
    my $success = 1;
    my $timestamp;
    my @warning_info;

    # Comment out the following lines to get predictable results for debug purposes
    # $printout_file = "/proj/DSC/rebels/logs/test_printouts/events_test_file_20220107.log";

    if ( $namespace eq "" ) {
        # No namespace given or error fetching the namespace, fetch information for all namespaces.
        $command .= " --all-namespaces";
    } else {
        # Namespace given, fetch information for the specific namespaces.
        $command .= " --namespace=$namespace";
    }

    if ($printout_file eq "") {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@std_error,
            }
        );
    } else {
        $rc = General::File_Operations::read_file(
            {
                "filename"              => $printout_file,
                "output-ref"            => \@result,
            }
        );
    }

    # Check the result
    if ( $rc == 0 && @result != 0 ) {
        my $json_ref = General::Json_Operations::read_array_return_reference( { "input" => \@result, "hide-error-messages" => 1 } );

        # Check that we have valid data, undef means an error was detected during deconding
        unless ($json_ref) {
            report_failed("Unable to decode the JSON event data.\n" . join "", @std_error, @result);
            return;
        }

        if (ref($json_ref) eq "HASH") {
            # HASH Reference
            # Check that the returned data follows the expected data structure
            # i.e. that the items->[]->type exists.
            unless (exists $json_ref->{'items'}) {
                # Incorrect format, ignore it
                return;
            }
            my $result_data_array_ref = $json_ref->{'items'};
            return if (scalar @$result_data_array_ref == 0);

            for my $hash_ref (@$result_data_array_ref) {
                if (exists $hash_ref->{'type'}) {
                    if ($hash_ref->{'type'} eq "Warning") {
                        $message = $hash_ref->{'message'};
                        # Indent messages with LF characters so it aligns with the Message column
                        $message =~ s/\n/\n$indent/g;
                        $message =~ s/\r/\n$indent/g;
                        $message =~ s/\n$indent$//;
                        # Check if some other fields are present
                        if (exists $hash_ref->{'lastTimestamp'} && defined $hash_ref->{'lastTimestamp'}) {
                            $timestamp = $hash_ref->{'lastTimestamp'};
                        } elsif (exists $hash_ref->{'eventTime'} && defined $hash_ref->{'eventTime'}) {
                            $timestamp = $hash_ref->{'eventTime'};
                        } else {
                            $timestamp = "-";
                        }
                        if (exists $hash_ref->{'count'} && $hash_ref->{'count'}) {
                            $count = $hash_ref->{'count'};
                        } else {
                            $count = 1;
                        }
                        push @warning_info, sprintf "%-30s  %-10s  %10d  %s\n", $timestamp, $hash_ref->{'type'}, $count, $message;
                    } elsif ($hash_ref->{'type'} ne "Normal") {
                        $message = $hash_ref->{'message'};
                        # Indent messages with LF characters so it aligns with the Message column
                        $message =~ s/\n/\n$indent/g;
                        $message =~ s/\r/\n$indent/g;
                        $message =~ s/\n$indent$//;
                        # Check if some other fields are present
                        if (exists $hash_ref->{'lastTimestamp'} && defined $hash_ref->{'lastTimestamp'}) {
                            $timestamp = $hash_ref->{'lastTimestamp'};
                        } elsif (exists $hash_ref->{'eventTime'} && defined $hash_ref->{'eventTime'}) {
                            $timestamp = $hash_ref->{'eventTime'};
                        } else {
                            $timestamp = "-";
                        }
                        if (exists $hash_ref->{'count'} && $hash_ref->{'count'}) {
                            $count = $hash_ref->{'count'};
                        } else {
                            $count = 1;
                        }
                        push @failure_info, sprintf "%-30s  %-10s  %10d  %s\n", $timestamp, $hash_ref->{'type'}, $count, $message;
                    }
                }
            }

            if ( scalar @failure_info > 0) {
                if ( scalar @warning_info > 0 && $variables{'events_hide_warnings'} eq "no") {
                    report_failed(
                        "The following failure and warning events was detected:\n\n" .
                        (sprintf "%-30s  %-10s  %-10s  %s\n", "Last Timestamp", "Event Type", "Count", "Message") .
                        (sprintf "%s  %s  %s  %s\n", "-"x30, "-"x10, "-"x10, "-"x7) .
                        join "", sort @failure_info, @warning_info
                    );
                } else {
                    report_failed(
                        "The following failure events was detected:\n\n" .
                        (sprintf "%-30s  %-10s  %-10s  %s\n", "Last Timestamp", "Event Type", "Count", "Message") .
                        (sprintf "%s  %s  %s  %s\n", "-"x30, "-"x10, "-"x10, "-"x7) .
                        join "", sort @failure_info
                    );
                }
            } elsif ( scalar @warning_info > 0) {
                if ( scalar @warning_info > 0 && $variables{'events_hide_warnings'} eq "no") {
                    report_warning(
                        "The following warning events was detected:\n\n" .
                        (sprintf "%-30s  %-10s  %-10s  %s\n", "Last Timestamp", "Event Type", "Count", "Message") .
                        (sprintf "%s  %s  %s  %s\n", "-"x30, "-"x10, "-"x10, "-"x7) .
                        join "", sort @warning_info
                    );
                }
            }

        } else {
            # Incorrect format, ignore it
            return;
        }

    } else {
        report_failed("kubectl get events command failed / no information found.\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_external_interfaces {
    my $cmyp_command = dirname(dirname(dirname(abs_path $0))) . "/expect/bin/send_command_to_ssh.exp";
    my $cmyp_ip;
    my $cmyp_password = $variables{'cmyp_password'} ne "" ? $variables{'cmyp_password'} : "rootroot";
    my $cmyp_port;
    my $cmyp_user = $variables{'cmyp_user'} ne "" ? $variables{'cmyp_user'} : "admin";
    my $rc;
    my @result;

    # Comment out the following lines to get predictable results for debug purposes
    # $printout_file = "/proj/DSC/rebels/logs/test_printouts/config_check_test_file_20220221.log";

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    unless (-f "$cmyp_command") {
        report_failed("Unable to find the '$cmyp_command' script.");
        return;
    }

    (undef, $cmyp_ip, $cmyp_port, undef, undef) = ADP::Kubernetes_Operations::get_cmyp_info($namespace);

    unless ($cmyp_ip) {
        report_failed("Failed to fetch CMYP IP_address.");
        return;
    }

    # Fetch 'cli' information from CMYP
    $cmyp_command .= " --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --port=$cmyp_port --timeout=5 --command='show cli'";

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $cmyp_command,
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );

    if ( $rc != 0 ) {
        if (scalar @result > 0) {
            report_failed("Command '$cmyp_command' failed.\n" . join "\n", @result);
        } else {
            report_failed("Command '$cmyp_command' failed and returned no data.\n");
        }
    }
}

# -----------------------------------------------------------------------------
sub check_helm_list_status {
    my $rc;
    my @result;
    my $sc_release_name = "";

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "${helm_executable} list --namespace $namespace",
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
        }
    );

    # Try to find the SC release name based on the deployed chart name
    if ( $rc == 0 && @result != 0 ) {
        foreach( @result ) {
            if ( $_ =~ m/^\s*(\S+).+\s(eric-sc-\S+|eric-dsc-\S+|eric-cloud-native-base-\S+|eric-cloud-native-nf-additions-\S+)\s.*/ ) {
                $sc_release_name .= "$1|";
            }
        }

        unless ( $sc_release_name ) {
            report_failed("helm list command found no deployments in namespace $namespace.\n" . join "", @result);
            return;
        }

    } else {
        report_failed("helm list command failed looking for deployments in namespace $namespace.\n" . join "", @result);
        return;
    }

    # Helm status returns a list of resources for the deployment, maybe useful for troubleshooting only
    if ( $sc_release_name ) {
        $sc_release_name =~ s/\|$//;
        for my $release_name (split /\|/, $sc_release_name) {
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "${helm_executable} status $release_name --namespace $namespace",
                    "hide-output"   => 1,
                    "raw-output"    => 1,
                    "return-output" => \@result,
                }
            );

            if ( $rc == 0 && @result != 0 ) {
                # OK, do nothing
            } else {
                report_failed("helm status $release_name command failed.\n" . join "", @result);
            }
        }
    } else {
        report_failed("helm status command skipped / no deployment release name found.\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_helm_version {
    my $helm_client_version;
    my $helm_server_version;
    my $helm_version;

    if ($helm_information) {
        if ( $used_helm_version == 2 ) {
            $helm_client_version = ${$helm_information}{'helm_client_version'};
            $helm_server_version = ${$helm_information}{'helm_server_version'};

            if ( $helm_client_version && version->parse( $helm_client_version ) >= version->parse( $variables{'helm_version_helm2_client_limit'} ) ) {
                # OK, Do nothing
            } else {
                report_failed("helm version command succeeded, but no client / wrong version found.\n");
            }

            if ( $helm_server_version && version->parse( $helm_server_version ) >= version->parse( $variables{'helm_version_helm2_server_limit'} ) ) {
                # OK, Do nothing
            } else {
                report_failed("helm version command succeeded, but no server / wrong version found.\n");
            }
        } elsif ( $used_helm_version == 3 ) {
            $helm_version = ${$helm_information}{'helm_version'};

            if ( $helm_version && version->parse( $helm_version ) >= version->parse( $variables{'helm_version_helm3_limit'} ) ) {
                # OK, Do nothing
            } else {
                report_failed("helm version command succeeded, but wrong version found.\n");
            }
        } else {
            report_failed("helm version not provided.\n");
        }
    } else {
        report_failed("helm version command failed / helm not found.\n");
    }
}

# -----------------------------------------------------------------------------
sub check_kpi {
    my $average_value;
    my $cmyp_password = $variables{'cmyp_password'} ne "" ? $variables{'cmyp_password'} : "rootroot";
    my $cmyp_user = $variables{'cmyp_user'} ne "" ? $variables{'cmyp_user'} : "expert";
    my $command = "";
    my $description;
    my @details;
    my @faults;
    my $kpi_count = 0;
    my @kpi_details = ();
    my $kpi_failure_cnt = 0;
    my $rc;
    my @result;
    my $show_details_at_success = $variables{'kpi_details_at_success'};
    my @std_error;

    $command = build_kpi_statistics_command();

    unless ($command) {
        # Building the command, failed.
        # Most likely due to no namespace given, just return and skip this check
        return;
    }

    # To speed up KPI checks, resuse the KPI collection from a previous run
    # if the commands to send are the same and there is some data collected.
    if ($variables{'kpi_use_cached_values'} eq "yes" && $kpi_cached_command eq $command && scalar @kpi_cached_result > 0) {
        # We have something cached from before, so reuse these values to speed things up.
        @result = @kpi_cached_result;
        $rc = 0;
    } else {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0 && scalar @result > 0) {
            # Only cache the result if the command was successful and returned some data.
            $kpi_cached_command = $command;
            @kpi_cached_result = @result;
        }
    }

    if ( $rc == 0 ) {
        my $collecting = 0;
        for (@result) {
            if ($collecting == 1) {
                push @details, "$_\n";
                if (/^\s*(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\d+)\s+(.+)\s+(\S+)\s*$/) {
                    #         0.271         0.225          0.403         0.270          0.400      1677  BSF DSL Load [%]                                         avg(bsf_dsl_load)
                    # Check KPI values to see if they are within threshold values
                    $average_value = $1;
                    $description = $7;
                    if ($description =~ /^.+Request Success Rate.+/ && $average_value < $variables{'kpi_success_rate_threshold'}) {
                        if ($average_value > 0) {
                            # Ignore success rate of 0% because it probably means that no traffic is running
                            push @faults, "Request Success Rate $average_value\% is lower than threshold $variables{'kpi_success_rate_threshold'}\% for KPI: $description\n";
                            $kpi_failure_cnt++;
                        }
                    } elsif ($description =~ /^.+ Load .+%/ && $average_value >= $variables{'kpi_load_threshold'}) {
                        push @faults, "Load $average_value\% is same or higher than threshold $variables{'kpi_load_threshold'}\% for KPI: $description\n";
                        $kpi_failure_cnt++;
                    }
                }
            } elsif (/^Statistics Summary\s*$/) {
                push @details, "$_\n";
                $collecting = 1;
            }
        }

        if ($kpi_failure_cnt > 0) {
            report_failed("$kpi_failure_cnt KPI checks failed.\n\n" . (join "", @details) . "\n" . (join "", @faults));
        } elsif ($show_details_at_success =~ /^(yes|1)$/i && @details) {
            # We have some details information to report
            report_details(join "", @details);
        }
    } else {
        if (scalar @result > 0) {
            report_failed("Command '$command' failed.\n" . join "\n", @result);
        } else {
            report_failed("Command '$command' failed and returned no data.\n");
        }
    }
}

# -----------------------------------------------------------------------------
sub check_kpi_success_rate {
    my $average_value;
    my @check_details = ( "Statistics Details", "==================", "" );
    my $command = "";
    my $description;
    my @details;
    my @faults;
    my $kpi_count = 0;
    my $kpi_count_mps_tps = 0;
    my @kpi_details = ();
    my $kpi_failure_cnt = 0;
    my @mps_tps_average_values = ();
    my @mps_tps_descriptions = ();
    my @mps_tps_queries = ();
    my $query;
    my $rc;
    my @result;
    my $show_details_at_success = $variables{'kpi_details_at_success'};
    my @std_error;
    my @success_descriptions = ();
    my @success_queries = ();

    $command = build_kpi_statistics_command();

    unless ($command) {
        # Building the command, failed.
        # Most likely due to no namespace given, just return and skip this check
        return;
    }

    if ($variables_user_set{'kpi_mps_tps_rate_description'} == 0 && $variables_user_set{'kpi_success_rate_description'} == 1) {
        # Only update the value if the default value for kpi_mps_tps_rate_description was not changed by the user but the kpi_success_rate_description was changed.
        # This is done to make it easier for the user so only one value is needed and to not have to also update
        # all places where kpi_success_rate_description was used to also pass kpi_mps_tps_rate_description.
        $variables{'kpi_mps_tps_rate_description'} = $variables{'kpi_success_rate_description'};
    }

    # To speed up KPI checks, resuse the KPI collection from a previous run
    # if the commands to send are the same and there is some data collected.
    if ($variables{'kpi_use_cached_values'} eq "yes" && $kpi_cached_command eq $command && scalar @kpi_cached_result > 0) {
        # We have something cached from before, so reuse these values to speed things up.
        @result = @kpi_cached_result;
        $rc = 0;
    } else {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ($rc == 0 && scalar @result > 0) {
            # Only cache the result if the command was successful and returned some data.
            $kpi_cached_command = $command;
            @kpi_cached_result = @result;
        }
    }

    if ( $rc == 0 ) {
        my $collecting = 0;
        for (@result) {
            if ($collecting == 1) {
                push @details, "$_\n";
                if (/^\s*(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\d+)\s+(.+)\s+(\S+)\s*$/) {
                    #         99.956        98.988         100.2       99.999        100.001       240  SCP Ingress Request Success Rate [%]                                              scp_ingress_success
                    # Check KPI values to see if they are within threshold values
                    $average_value = $1;
                    $description = $7;
                    $query = $8;
                    if ($description =~ /^$variables{'kpi_success_rate_description'}\s*Success Rate \[%]\s*$/) {
                        $kpi_count++;
                        if ($average_value < $variables{'kpi_success_rate_threshold'}) {
                            push @check_details, "NOK  Success Rate $average_value\% is lower than threshold $variables{'kpi_success_rate_threshold'}\% for KPI: $description";
                            $kpi_failure_cnt++;
                        } else {
                            push @check_details, "OK   Success Rate $average_value\% is greater than or equal to threshold $variables{'kpi_success_rate_threshold'}\% for KPI: $description";
                        }
                        # Keep track of the success rate queries to be able to check for downtime intervals
                        push @success_queries, $query;
                        push @success_descriptions, $description;
                    } elsif ($description =~ /^$variables{'kpi_mps_tps_rate_description'}\s*\[(MPS|TPS)]\s*$/) {
                        $kpi_count_mps_tps++;
                        if ($average_value > 0) {
                            push @check_details, "OK   MPS/TPS Rate average value $average_value is greater than zero for KPI: $description";
                        } else {
                            push @check_details, "NOK  MPS/TPS Rate average value $average_value is zero for KPI: $description";
                            $kpi_failure_cnt++;
                        }
                        # Keep track of the mps/tps rate queries to be able to check for downtime intervals
                        push @mps_tps_average_values, $average_value;
                        push @mps_tps_queries, $query;
                        push @mps_tps_descriptions, $description;
                    }
                }
            } elsif (/^Statistics Summary\s*$/) {
                push @details, "$_\n";
                $collecting = 1;
            }
        }

        #
        # Check Success Rate
        #
        if ($kpi_count == 0) {
            # No KPI Success Rate counters were found, this is an error
            push @faults, "No KPI counters matching the description '$variables{'kpi_success_rate_description'}' (Perl regexp /^$variables{'kpi_success_rate_description'}\\s*Success Rate [%]\\s*\$/) found\n";
            $kpi_failure_cnt++;
        }

        if (@success_queries && $variables{'kpi_data_directory'} ne "") {
            # Verification of KPI success rate downtime can only be done if detailed logs are collected
            # by prividing an output directory.
            $kpi_failure_cnt += look_for_kpi_success_rate_downtime(\@success_queries, \@success_descriptions, \@details, \@check_details);
        }

        #
        # Check MPS/TPS Rate
        #
        if ($kpi_count_mps_tps == 0) {
            # No KPI MPS/TPS Rate counters were found, this is an error
            push @faults, "No KPI counters matching the description '$variables{'kpi_mps_tps_rate_description'}' (Perl regexp /^$variables{'kpi_mps_tps_rate_description'}\\s*[(MPS|TPS)]\\s*\$/) found\n";
            $kpi_failure_cnt++;
        }

        if (@mps_tps_queries && $variables{'kpi_data_directory'} ne "") {
            # Verification of KPI success rate downtime can only be done if detailed logs are collected
            # by prividing an output directory.
            $kpi_failure_cnt += look_for_kpi_mps_tps_rate_downtime(\@mps_tps_queries, \@mps_tps_descriptions, \@details, \@check_details, \@mps_tps_average_values);
        }

        #
        # Check for any failures
        #
        if ($kpi_failure_cnt > 0) {
            report_failed("$kpi_failure_cnt KPI checks failed.\n\n" . (join "", @details) . "\n" . (join "\n", @check_details) . "\n" . (join "", @faults));
        } elsif ($show_details_at_success =~ /^(yes|1)$/i && @details) {
            # We have some details information to report
            report_details((join "", @details) . "\n" . (join "\n", @check_details));
        }
    } else {
        if (scalar @result > 0) {
            report_failed("Command '$command' failed.\n" . join "\n", @result);
        } else {
            report_failed("Command '$command' failed and returned no data.\n");
        }
    }
}

# -----------------------------------------------------------------------------
sub check_kubectl_version {
    my $kubectl_client_version;
    my $kubectl_server_version;
    my $rc;
    my @result;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl version" . $kubeconfig,
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
        }
    );

    # Loop over command results and find kubectl client and server version
    if ( $rc == 0 && @result != 0 ) {
        foreach( @result ) {
            if ( $_ =~ m/^Client Version:.*/ ) {
                if ( $_ =~ m/.*v([0-9]+\.[0-9]+\.[0-9]+).*/ ) {
                    $kubectl_client_version = $1;
                }
            } elsif ( $_ =~ m/^Server Version:.*/ ) {
                if ( $_ =~ m/.*v([0-9]+\.[0-9]+\.[0-9]+).*/ ) {
                    $kubectl_server_version = $1;
                }
            }
        }

        if ( $kubectl_client_version && version->parse( $kubectl_client_version ) >= version->parse( $variables{'kubectl_version_client_limit'} ) ) {
            # OK, do nothing
        } elsif ( $kubectl_client_version ) {
            report_failed("kubectl version command succeeded, but wrong client version found.\n" . join "", @result);
        } else {
            report_failed("kubectl version command succeeded, but no client version found.\n");
        }

        if ( $kubectl_server_version && version->parse( $kubectl_server_version ) >= version->parse( $variables{'kubectl_version_server_limit'} ) ) {
            # OK, do nothing
        } elsif ( $kubectl_server_version ) {
            report_failed("kubectl version command succeeded, but wrong server version found.\n" . join "", @result);
        } else {
            report_failed("kubectl version command succeeded, but no server version found.\n");
        }

    } else {
        report_failed("kubectl version command failed / kubectl not found.\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
# NOTE: This check is only accurate when executed on the same cluster where the
# software is running or if the times on the two systems are the same.
sub check_log_statistics {
    my $count;
    my $counters;
    my $debug_messages = 1;
    my $fault_cnt = 0;
    my @faults;
    my $log;
    my $logs_per_sec;
    my $max_length_count = 9;
    my $max_length_critical = 8;
    my $max_length_error = 5;
    my $max_length_log_per_sec = 7;
    my $max_length_log_plane = 9;
    my $max_length_seconds = 7;
    my $max_length_verdict = 7;
    my $max_length_warning = 7;
    my $now = time();
    my $number_of_seconds = 0;
    my $rc;
    my @result;
    my %result_hash;
    my $time_interval = $variables{'logs_time_interval'};
    my $verdict;

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    if ($debug_messages) {
        use Data::Dumper;
        $Data::Dumper::Sortkeys = 1;
    }

    # Convert the time interval into absolute values so the check for severity
    # will use the same interval
    if ($time_interval =~ /^(\d+)s?$/) {
        $time_interval = sprintf "%d to %d", ($now - $1), $now;
    } elsif ($time_interval =~ /^(\d+)m$/) {
        $time_interval = sprintf "%d to %d", ($now - ($1 * 60)), $now;
    } elsif ($time_interval =~ /^(\d+)h$/) {
        $time_interval = sprintf "%d to %d", ($now - ($1 * 3600)), $now;
    } elsif ($time_interval =~ /^(\d+)d$/) {
        $time_interval = sprintf "%d to %d", ($now - ($1 * 3600 * 24)), $now;
    }

    $counters = ADP::Kubernetes_Operations::get_log_count(
        {
            "debug-messages"        => $debug_messages,
            "date"                  => "",
            "force-hash-result"     => 1,
            "log"                   => $variables{'logs_to_check'},
            "namespace"             => $namespace,
            "severity"              => $variables{'logs_severity'},
            "time-period"           => $time_interval,
        }
    );

    General::Logging::log_write("Found counters:\n" . Dumper($counters)) if $debug_messages;

    for $log (sort keys %$counters) {
        $count = $counters->{$log}{'count'};
        $logs_per_sec = sprintf "%.3f", $counters->{$log}{'average'};
        $number_of_seconds = $counters->{$log}{'time-period'};
        if ($logs_per_sec > $variables{'logs_per_second_average_limit'}) {
            $verdict = "NOK, log/sec > $variables{'logs_per_second_average_limit'}";
            $fault_cnt++;
        } else {
            $verdict = "";
        }
        General::Logging::log_write("Total log entries: count=$count, time_interval=$time_interval, number_of_seconds=$number_of_seconds, logs_per_sec=$logs_per_sec, log=$log\n");

        # Add to result_hash
        $result_hash{$log}{'count'} = $count;
        $result_hash{$log}{'seconds'} = $number_of_seconds;
        $result_hash{$log}{'log_per_sec'} = $logs_per_sec;
        $result_hash{$log}{'critical'} = '-';
        $result_hash{$log}{'error'} = '-';
        $result_hash{$log}{'warning'} = '-';
        $result_hash{$log}{'verdict'} = $verdict;

        # Calculate max column lengths
        $max_length_log_plane = length($log) if ($max_length_log_plane < length($log));
        $max_length_count = length($count) if ($max_length_count < length($count));
        $max_length_seconds = length($number_of_seconds) if ($max_length_seconds < length($number_of_seconds));
        $max_length_log_per_sec = length($logs_per_sec) if ($max_length_log_per_sec < length($logs_per_sec));
        $max_length_verdict = length($verdict) if ($max_length_verdict < length($verdict));
    }

    if ($variables{'logs_severity'} eq "") {
        # User did not specifically ask for checking certain severity, so we check them all.

        #
        # severity=critical
        #
        $counters = ADP::Kubernetes_Operations::get_log_count(
            {
                "debug-messages"        => $debug_messages,
                "date"                  => "",
                "force-hash-result"     => 1,
                "log"                   => $variables{'logs_to_check'},
                "namespace"             => $namespace,
                "severity"              => "critical",
                "time-period"           => $time_interval,
                "log-details-directory" => ($variables{'logs_details_directory'} ne "" ? $variables{'logs_details_directory'} : ""),
            }
        );

        General::Logging::log_write("Found Critical counters:\n" . Dumper($counters)) if $debug_messages;

        for $log (sort keys %$counters) {
            $count = $counters->{$log}{'count'};
            $logs_per_sec = sprintf "%.3f", $counters->{$log}{'average'};
            $number_of_seconds = $counters->{$log}{'time-period'};

            unless (exists $result_hash{$log}) {
                # A strange case where the log was not seen previously when checking for total number of log entries
                # but it showed up when checking for the Critical, Error or Warning log entries. We now initialize the hash.
                # Add to result_hash
                $result_hash{$log}{'count'} = 0;
                $result_hash{$log}{'seconds'} = $number_of_seconds;
                $result_hash{$log}{'log_per_sec'} = 0;
                $result_hash{$log}{'critical'} = '-';
                $result_hash{$log}{'error'} = '-';
                $result_hash{$log}{'warning'} = '-';
                $result_hash{$log}{'verdict'} = "";

                # Calculate max column lengths
                $max_length_log_plane = length($log) if ($max_length_log_plane < length($log));
                $max_length_seconds = length($number_of_seconds) if ($max_length_seconds < length($number_of_seconds));
            }

            if ($count > $variables{'logs_severity_critical_limit'}) {
                if ($result_hash{$log}{'verdict'} eq "") {
                    $result_hash{$log}{'verdict'} = "NOK, critical > $variables{'logs_severity_critical_limit'}";
                } else {
                    $result_hash{$log}{'verdict'} .= ", critical > $variables{'logs_severity_critical_limit'}";
                }
                $fault_cnt++;
            }
            General::Logging::log_write("Severity=critical log entries: count=$count, time_interval=$time_interval, number_of_seconds=$number_of_seconds, logs_per_sec=$logs_per_sec, log=$log\n");

            # Add to result_hash
            $result_hash{$log}{'critical'} = $count;

            # Calculate max column lengths
            $max_length_critical = length($count) if ($max_length_critical < length($count));
            $max_length_verdict = length($result_hash{$log}{'verdict'}) if ($max_length_verdict < length($result_hash{$log}{'verdict'}));
        }

        #
        # severity=error
        #
        $counters = ADP::Kubernetes_Operations::get_log_count(
            {
                "debug-messages"        => $debug_messages,
                "date"                  => "",
                "force-hash-result"     => 1,
                "log"                   => $variables{'logs_to_check'},
                "namespace"             => $namespace,
                "severity"              => "error",
                "time-period"           => $time_interval,
                "log-details-directory" => ($variables{'logs_details_directory'} ne "" ? $variables{'logs_details_directory'} : ""),
            }
        );

        General::Logging::log_write("Found Error counters:\n" . Dumper($counters)) if $debug_messages;

        for $log (sort keys %$counters) {
            $count = $counters->{$log}{'count'};
            $logs_per_sec = sprintf "%.3f", $counters->{$log}{'average'};
            $number_of_seconds = $counters->{$log}{'time-period'};

            unless (exists $result_hash{$log}) {
                # A strange case where the log was not seen previously when checking for total number of log entries
                # but it showed up when checking for the Critical, Error or Warning log entries. We now initialize the hash.
                # Add to result_hash
                $result_hash{$log}{'count'} = 0;
                $result_hash{$log}{'seconds'} = $number_of_seconds;
                $result_hash{$log}{'log_per_sec'} = 0;
                $result_hash{$log}{'critical'} = '-';
                $result_hash{$log}{'error'} = '-';
                $result_hash{$log}{'warning'} = '-';
                $result_hash{$log}{'verdict'} = "";

                # Calculate max column lengths
                $max_length_log_plane = length($log) if ($max_length_log_plane < length($log));
                $max_length_seconds = length($number_of_seconds) if ($max_length_seconds < length($number_of_seconds));
            }

            if ($count > $variables{'logs_severity_error_limit'}) {
                if ($result_hash{$log}{'verdict'} eq "") {
                    $result_hash{$log}{'verdict'} = "NOK, error > $variables{'logs_severity_error_limit'}";
                } else {
                    $result_hash{$log}{'verdict'} .= ", error > $variables{'logs_severity_error_limit'}";
                }
                $fault_cnt++;
            }
            General::Logging::log_write("Severity=error log entries: count=$count, time_interval=$time_interval, number_of_seconds=$number_of_seconds, logs_per_sec=$logs_per_sec, log=$log\n");

            # Add to result_hash
            $result_hash{$log}{'error'} = $count;

            # Calculate max column lengths
            $max_length_error = length($count) if ($max_length_error < length($count));
            $max_length_verdict = length($result_hash{$log}{'verdict'}) if ($max_length_verdict < length($result_hash{$log}{'verdict'}));
        }

        #
        # severity=warning
        #
        $counters = ADP::Kubernetes_Operations::get_log_count(
            {
                "debug-messages"        => $debug_messages,
                "date"                  => "",
                "force-hash-result"     => 1,
                "log"                   => $variables{'logs_to_check'},
                "namespace"             => $namespace,
                "severity"              => "warning",
                "time-period"           => $time_interval,
                "log-details-directory" => ($variables{'logs_details_directory'} ne "" ? $variables{'logs_details_directory'} : ""),
            }
        );

        General::Logging::log_write("Found Warning counters:\n" . Dumper($counters)) if $debug_messages;

        for $log (sort keys %$counters) {
            $count = $counters->{$log}{'count'};
            $logs_per_sec = sprintf "%.3f", $counters->{$log}{'average'};
            $number_of_seconds = $counters->{$log}{'time-period'};

            unless (exists $result_hash{$log}) {
                # A strange case where the log was not seen previously when checking for total number of log entries
                # but it showed up when checking for the Critical, Error or Warning log entries. We now initialize the hash.
                # Add to result_hash
                $result_hash{$log}{'count'} = 0;
                $result_hash{$log}{'seconds'} = $number_of_seconds;
                $result_hash{$log}{'log_per_sec'} = 0;
                $result_hash{$log}{'critical'} = '-';
                $result_hash{$log}{'error'} = '-';
                $result_hash{$log}{'warning'} = '-';
                $result_hash{$log}{'verdict'} = "";

                # Calculate max column lengths
                $max_length_log_plane = length($log) if ($max_length_log_plane < length($log));
                $max_length_seconds = length($number_of_seconds) if ($max_length_seconds < length($number_of_seconds));
            }

            if ($count > $variables{'logs_severity_warning_limit'}) {
                if ($result_hash{$log}{'verdict'} eq "") {
                    $result_hash{$log}{'verdict'} = "NOK, warning > $variables{'logs_severity_warning_limit'}";
                } else {
                    $result_hash{$log}{'verdict'} .= ", warning > $variables{'logs_severity_warning_limit'}";
                }
                $fault_cnt++;
            }
            General::Logging::log_write("Severity=warning log entries: count=$count, time_interval=$time_interval, number_of_seconds=$number_of_seconds, logs_per_sec=$logs_per_sec, log=$log\n");

            # Add to result_hash
            $result_hash{$log}{'warning'} = $count;

            # Calculate max column lengths
            $max_length_warning = length($count) if ($max_length_warning < length($count));
            $max_length_verdict = length($result_hash{$log}{'verdict'}) if ($max_length_verdict < length($result_hash{$log}{'verdict'}));
        }
    }

    if (%result_hash) {
        push @faults, sprintf "%-${max_length_log_plane}s  %-${max_length_count}s  %-${max_length_seconds}s  %-${max_length_log_per_sec}s  %-${max_length_critical}s  %-${max_length_error}s  %-${max_length_warning}s  %-${max_length_verdict}s\n",
            "Log Plane",
            "Log Count",
            "Seconds",
            "log/sec",
            "Critical",
            "Error",
            "Warning",
            "Verdict";

        push @faults, sprintf "%s  %s  %s  %s  %s  %s  %s  %s\n",
            "-" x $max_length_log_plane,
            "-" x $max_length_count,
            "-" x $max_length_seconds,
            "-" x $max_length_log_per_sec,
            "-" x $max_length_critical,
            "-" x $max_length_error,
            "-" x $max_length_warning,
            "-" x $max_length_verdict;

        for $log (sort keys %result_hash) {
            push @faults, sprintf "%-${max_length_log_plane}s  %${max_length_count}s  %${max_length_seconds}s  %${max_length_log_per_sec}s  %${max_length_critical}s  %${max_length_error}s  %${max_length_warning}s  %-${max_length_verdict}s\n",
                $log,
                $result_hash{$log}{'count'},
                $result_hash{$log}{'seconds'},
                $result_hash{$log}{'log_per_sec'},
                $result_hash{$log}{'critical'},
                $result_hash{$log}{'error'},
                $result_hash{$log}{'warning'},
                ($result_hash{$log}{'verdict'} ne "" ? $result_hash{$log}{'verdict'} : "OK");
        }
    }

    # Report the check as failed if there are faults.
    if ($fault_cnt > 0) {
        report_failed(join "", @faults);
    } elsif ($variables{'log_statistics_at_success'} =~ /^(yes|1)$/i) {
        report_details(join "", @faults);
    }
}

# -----------------------------------------------------------------------------
sub check_master_core_dumps {
    get_core_dumps("master");
}

# -----------------------------------------------------------------------------
sub check_max_connections {
    my $command = "kubectl --namespace=$namespace exec eric-data-document-database-pg-0 -c eric-data-document-database-pg" . $kubeconfig;
    my @pod_names = ();
    my $sub_command;
    my @failure_info;
    my $rc;
    my @result;

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }
    @pod_names = ADP::Kubernetes_Operations::get_pod_names(
        {
            "namespace"         => $namespace,
            "pod-include-list"  => [ 'eric-data-document-database-pg-0' ],
            "hide-output"       => 1,
        }
    );
    if ( @pod_names != 0 ) {
        for $sub_command ( ("patronictl show-config", "grep max_connections /var/lib/postgresql/config/patroni.yml", "grep max_connections /var/lib/postgresql/data/pgdata/postgresql.conf") ) {
            print_verbose("  Checking max_connections using '$sub_command'\n");

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "$command -- $sub_command",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                }
            );

            if ( $rc == 0 && @result) {
                my $found = 0;
                for (@result) {
                    if (/^\s*max_connections\s*[:=]\s*'*(\d+)/) {
                        if ($1 != $variables{'max_connections_value'}) {
                            push @failure_info, "\nThe value of max_connections = $1 is not the expected $variables{'max_connections_value'} when checking '$sub_command'\n";
                        }
                        $found++;
                    }
                }
                if ($found == 0) {
                    push @failure_info, "\nNo 'max_connections' setting found when checking '$sub_command'.\n" . (join "\n", @result);
                }
            } else {
                push @failure_info, "\n'$command -- $sub_command' command failed.\n" . join "\n", @result;
            }
        }

        if (@failure_info) {
            report_failed(join "", @failure_info);
        }
    } else {
        report_skipped("Check skipped because no eric-data-document-database-pg-0 pod found.\n");
    }
}

# -----------------------------------------------------------------------------
sub check_max_map_count {
    my $connect_cnt = 0;
    my $connect_error_cnt = 0;
    my @error;
    my @faults;
    my $rc;
    my @result;
    my @worker_ip;

    @worker_ip = ADP::Kubernetes_Operations::get_node_ip( { "type" => "worker" } );
    if ( @worker_ip != 0 ) {
        for my $ip ( @worker_ip ) {
            $connect_cnt++;

            print_verbose("  Checking max_map_count definition from worker IP $ip\n");

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR $ip 'sudo -n sysctl vm.max_map_count'",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                    "return-stderr" => \@error,
                }
            );

            if ( $rc == 0 && @result != 0 ) {

                my $found_use = 0;
                for ( @result ) {
                    if ( /^vm\.max_map_count = (\d+)\s*$/ ) {
                        # vm.max_map_count = 262144
                        $found_use++;
                        if ( $1 < $variables{'max_map_count_limit'} ) {
                            push @faults, "The value of max_map_count on Worker with IP $ip is $1 which is lower than the minimum $variables{'max_map_count_limit'} value.\n" . (join "\n", @result) . "\n\n";
                        }
                        last;
                    }
                }
                if ( $found_use == 0 ) {
                    push @faults, "No max_map_count found on worker with IP $ip.\n" . (join "\n", @result) . "\n\n";
                }
            } else {
                push @faults, "Unable to fetch max_map_count from worker with IP $ip.\n" . (join "\n", @error, @result) . "\n\n";
                if (grep /Permission denied \(publickey,password,keyboard-interactive\)/, @error) {
                    $connect_error_cnt++;
                } elsif (grep /Permission denied \(publickey,keyboard-interactive\)/, @error) {
                    $connect_error_cnt++;
                } elsif (grep /Connection timed out/, @error) {
                    $connect_error_cnt++;
                }
                if ($connect_cnt == 1 && $connect_error_cnt == 1) {
                    push @faults, "Failed to connect to the first node, Assuming the other nodes will also fail so stopping the check.\n";
                    last;
                }
            }
        }
        if (@faults) {
            # Report faults
            report_failed(join "", @faults);
        }
    } else {
        report_skipped("Check skipped because no worker IP addresses found.\n");
    }
}

# -----------------------------------------------------------------------------
sub check_nerdctl_version {
    my $container_command = ADP::Kubernetes_Operations::get_docker_or_nerdctl_command(1);  # Hide status and warning/error messages from the user
    my $nerdctl_client_version;
    my $rc;
    my @result;

    if ($container_command ne "" && $container_command !~ /nerdctl/) {
        report_skipped("Command '$container_command' is used instead of 'nerdctl'\n");
        return;
    } elsif ($container_command eq "") {
        report_failed("Command 'nerdctl' not found, or require sudo with interactive password prompt.\n");
        return;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "$container_command version --format {{.Client.Version}}",
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
        }
    );

    # Loop over command results and find nerdctl version, command get permission denied so skip
    # rc check
    if ( @result != 0 ) {
        foreach(@result) {
            if ( $_ =~ m/^v?([0-9]+\.[0-9]+\.[0-9]+).*/ ) {
                $nerdctl_client_version = $1;
                last;
            } elsif ( $_ =~ /^sudo: a password is required/ ) {
                report_warning("A password is required to access the nerdctl version.\n");
                return;
            }
        }
        if ( $nerdctl_client_version && version->parse( $nerdctl_client_version ) >= version->parse( $variables{'nerdctl_version_limit'} ) ) {
            # OK, do nothing
        } elsif ( $nerdctl_client_version ) {
            report_failed("nerdctl version $nerdctl_client_version found but expected to be $variables{'nerdctl_version_limit'} or higher.\n" . join "", @result);
        } else {
            report_failed("No nerdctl versions found.\n" . join "", @result);
        }

    } else {
        report_failed("Command failed / nerdctl not found.\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_node_status {
    my $cnt_DiskPressure = 0;
    my $cnt_MemoryPressure = 0;
    my $cnt_NetworkUnavailable = 0;
    my $cnt_node_issues = 0;
    my $cnt_NotReady = 0;
    my $cnt_PIDPressure = 0;
    my $rc;
    my @result;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get nodes" . $kubeconfig . " -o jsonpath='{range .items[*]}{@.metadata.name}{\"\\n\"}{range @.status.conditions[*]}{@.type}={@.status}{\"\\n\"}{end}{\"\\n\"}{end}'",
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );

    # Loop over command results and find any nodes which are not ready
    if ( $rc == 0 && @result != 0 ) {
        foreach( @result ) {
            if ( $_ =~ m/NetworkUnavailable=(\S+)/ ) {
                if ($1 ne "False") {
                    $cnt_node_issues++;
                    $cnt_NetworkUnavailable++;
                }
            }
            if ( $_ =~ m/MemoryPressure=(\S+)/ ) {
                if ($1 ne "False") {
                    $cnt_node_issues++;
                    $cnt_MemoryPressure++;
                }
            }
            if ( $_ =~ m/DiskPressure=(\S+)/ ) {
                if ($1 ne "False") {
                    $cnt_node_issues++;
                    $cnt_DiskPressure++;
                }
            }
            if ( $_ =~ m/PIDPressure=(\S+)/ ) {
                if ($1 ne "False") {
                    $cnt_node_issues++;
                    $cnt_PIDPressure++;
                }
            }
            if ( $_ =~ m/Ready=(\S+)/ ) {
                if ($1 ne "True") {
                    $cnt_node_issues++;
                    $cnt_NotReady++;
                }
            }
        }

        if ( $cnt_node_issues > 0 ) {
            my $message = "Found $cnt_node_issues node issues:\n";
            $message .= "  - Found $cnt_DiskPressure nodes with DiskPressure.\n" if ($cnt_DiskPressure > 0);
            $message .= "  - Found $cnt_MemoryPressure nodes with MemoryPressure.\n" if ($cnt_MemoryPressure > 0);
            $message .= "  - Found $cnt_NetworkUnavailable nodes with NetworkUnavailable.\n" if ($cnt_NetworkUnavailable > 0);
            $message .= "  - Found $cnt_PIDPressure nodes with PIDPressure.\n" if ($cnt_PIDPressure > 0);
            $message .= "  - Found $cnt_NotReady nodes that are not Ready.\n" if ($cnt_NotReady > 0);
            $message .= "\n";
            $message .= (join "\n", @result);
            report_failed($message);
        }

    } else {
        report_failed("kubectl get nodes command failed / kubectl not found.\n" . join "\n", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_node_utilization {
    my @all_node_info;
    my @data;
    my @details;
    my @node_info;
    my %node_info;
        # 1st key: Name of node
        # 2nd key: "Capacity", "Allocatable" or "Allocated"
        # 3rd key: "cpu", "ephemeral-storage", "memory" or "pods"
    my @node_names;
    my $node_result;
    my $rc;
    my $usage_percent;

    print_verbose("  Fetching 'kubectl describe nodes --all-namespaces'\n");
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl describe nodes --all-namespaces" . $kubeconfig,
            "hide-output"   => 1,
            "return-output" => \@all_node_info,
        }
    );
    if ( $rc != 0 || @all_node_info == 0 ) {
        # Failure or no data returned
        report_failed("Failed to get node data.\n");
        return;
    }

    @node_names = ADP::Kubernetes_Operations::get_section_names(\@all_node_info);
    if ( scalar @node_names == 0 ) {
        # No node names found
        report_failed("Failed to find any node names.\n" . join "\n", @all_node_info);
        return;
    }

    for my $node_name (@node_names) {
        print_verbose("  Analyzing information for node '$node_name'\n");
        @node_info = ADP::Kubernetes_Operations::get_name_section_information($node_name, \@all_node_info);

        # Initialize the data for each node
        $node_info{$node_name}{"Capacity"}{"cpu"} = 0;
        $node_info{$node_name}{"Capacity"}{"ephemeral-storage"} = 0;
        $node_info{$node_name}{"Capacity"}{"memory"} = 0;
        $node_info{$node_name}{"Capacity"}{"pods"} = 0;

        $node_info{$node_name}{"Allocatable"}{"cpu"} = 0;
        $node_info{$node_name}{"Allocatable"}{"ephemeral-storage"} = 0;
        $node_info{$node_name}{"Allocatable"}{"memory"} = 0;
        $node_info{$node_name}{"Allocatable"}{"pods"} = 0;

        $node_info{$node_name}{"Allocated"}{"cpu"} = 0;
        $node_info{$node_name}{"Allocated"}{"ephemeral-storage"} = 0;
        $node_info{$node_name}{"Allocated"}{"memory"} = 0;
        $node_info{$node_name}{"Allocated"}{"pods"} = 0;

        $node_result = 0;

        # Check Capacity information
        @data = ADP::Kubernetes_Operations::get_top_level_section_information("Capacity", \@node_info);
        if (scalar @data > 0) {
            # Capacity:
            # cpu:                4
            # ephemeral-storage:  41902736Ki
            # hugepages-1Gi:      0
            # hugepages-2Mi:      0
            # memory:             16388108Ki
            # pods:               110
            for (@data) {
                if (/^\s+cpu:\s+(\S+)/) {
                    $node_info{$node_name}{"Capacity"}{"cpu"} = ADP::Kubernetes_Operations::convert_number_unit($1);
                } elsif (/^\s+ephemeral-storage:\s+(\S+)/) {
                    $node_info{$node_name}{"Capacity"}{"ephemeral-storage"} = ADP::Kubernetes_Operations::convert_number_unit($1);
                } elsif (/^\s+memory:\s+(\S+)/) {
                    $node_info{$node_name}{"Capacity"}{"memory"} = ADP::Kubernetes_Operations::convert_number_unit($1);
                } elsif (/^\s+pods:\s+(\S+)/) {
                    $node_info{$node_name}{"Capacity"}{"pods"} = ADP::Kubernetes_Operations::convert_number_unit($1);
                }
            }
        } else {
            push @details, "Failed to read 'Capacity' data for node '$node_name'\n";
            $node_result = 1;
        }

        # Check Allocatable information
        @data = ADP::Kubernetes_Operations::get_top_level_section_information("Allocatable", \@node_info);
        if (scalar @data > 0) {
            # Allocatable:
            # cpu:                4
            # ephemeral-storage:  38617561434
            # hugepages-1Gi:      0
            # hugepages-2Mi:      0
            # memory:             16285708Ki
            # pods:               110
            for (@data) {
                if (/^\s+cpu:\s+(\S+)/) {
                    $node_info{$node_name}{"Allocatable"}{"cpu"} = ADP::Kubernetes_Operations::convert_number_unit($1);
                } elsif (/^\s+ephemeral-storage:\s+(\S+)/) {
                    $node_info{$node_name}{"Allocatable"}{"ephemeral-storage"} = ADP::Kubernetes_Operations::convert_number_unit($1);
                } elsif (/^\s+memory:\s+(\S+)/) {
                    $node_info{$node_name}{"Allocatable"}{"memory"} = ADP::Kubernetes_Operations::convert_number_unit($1);
                } elsif (/^\s+pods:\s+(\S+)/) {
                    $node_info{$node_name}{"Allocatable"}{"pods"} = ADP::Kubernetes_Operations::convert_number_unit($1);
                }
            }
        } else {
            push @details, "Failed to read 'Allocatable' data for node '$node_name'\n";
            $node_result = 1;
        }

        # Check Non-terminated Pods information
        @data = ADP::Kubernetes_Operations::get_top_level_section_information("Non-terminated Pods", \@node_info);
        if (scalar @data > 0) {
            # Non-terminated Pods:         (14 in total)
            # Namespace                  Name                                                  CPU Requests  CPU Limits  Memory Requests  Memory Limits  AGE
            # ---------                  ----                                                  ------------  ----------  ---------------  -------------  ---
            # kube-system                calico-node-vhxjx                                     250m (6%)     0 (0%)      0 (0%)           0 (0%)         69d
            # kube-system                coredns-69454b4c7-fjpq2                               100m (2%)     0 (0%)      70Mi (0%)        340Mi (2%)     69d
            for (@data) {
                if (/^Non-terminated Pods:\s+\((\d+) in total\)/) {
                    $node_info{$node_name}{"Allocated"}{"pods"} = ADP::Kubernetes_Operations::convert_number_unit($1);
                }
            }
        } else {
            push @details, "Failed to read 'Allocated resources' data for node '$node_name'\n";
            $node_result = 1;
        }

        # Check Allocated information
        @data = ADP::Kubernetes_Operations::get_top_level_section_information("Allocated resources", \@node_info);
        if (scalar @data > 0) {
            # Allocated resources:
            # (Total limits may be over 100 percent, i.e., overcommitted.)
            # Resource           Requests     Limits
            # --------           --------     ------
            # cpu                1385m (34%)  750m (18%)
            # memory             403Mi (2%)   1196Mi (7%)
            # ephemeral-storage  0 (0%)       0 (0%)
            for (@data) {
                if (/^\s+cpu\s+(\S+)\s+\(\d+\%\)\s+\S+\s+\(\d+\%\)/) {
                    $node_info{$node_name}{"Allocated"}{"cpu"} = ADP::Kubernetes_Operations::convert_number_unit($1);
                } elsif (/^\s+ephemeral-storage\s+(\S+)\s+\(\d+\%\)\s+\S+\s+\(\d+\%\)/) {
                    $node_info{$node_name}{"Allocated"}{"ephemeral-storage"} = ADP::Kubernetes_Operations::convert_number_unit($1);
                } elsif (/^\s+memory\s+(\S+)\s+\(\d+\%\)\s+\S+\s+\(\d+\%\)/) {
                    $node_info{$node_name}{"Allocated"}{"memory"} = ADP::Kubernetes_Operations::convert_number_unit($1);
                }
            }
        } else {
            push @details, "Failed to read 'Allocated resources' data for node '$node_name'\n";
            $node_result = 1;
        }
    }

    # Analyze the collected data and determine if the check is successful or failed
    for my $node (sort keys %node_info) {
        $usage_percent = sprintf "%.2f", ($node_info{$node}{"Allocated"}{"cpu"} / $node_info{$node}{"Allocatable"}{"cpu"}) * 100;
        if ($usage_percent < $variables{'node_utilization_cpu_threshold'}) {
            # push @details, "Check successful on node $node because cpu utilization of $usage_percent\% is below the limit of $variables{'node_utilization_cpu_threshold'}\% (Allocatable=$node_info{$node}{Allocatable}{cpu}, Allocated=$node_info{$node}{Allocated}{cpu}).\n";
        } else {
            push @details, "Check failed on node $node because cpu utilization of $usage_percent\% is over the limit of $variables{'node_utilization_cpu_threshold'}\% (Allocatable=$node_info{$node}{Allocatable}{cpu}, Allocated=$node_info{$node}{Allocated}{cpu}).\n";
            $node_result = 1;
        }

        $usage_percent = sprintf "%.2f", ($node_info{$node}{"Allocated"}{"memory"} / $node_info{$node}{"Allocatable"}{"memory"}) * 100;
        if ($usage_percent < $variables{'node_utilization_memory_threshold'}) {
            # push @details, "Check successful on node $node because memory utilization of $usage_percent\% is below the limit of $variables{'node_utilization_memory_threshold'}\% (Allocatable=$node_info{$node}{Allocatable}{memory}, Allocated=$node_info{$node}{Allocated}{memory}).\n";
        } else {
            push @details, "Check failed on node $node because memory utilization of $usage_percent\% is over the limit of $variables{'node_utilization_memory_threshold'}\% (Allocatable=$node_info{$node}{Allocatable}{memory}, Allocated=$node_info{$node}{Allocated}{memory}).\n";
            $node_result = 1;
        }

        $usage_percent = sprintf "%.2f", ($node_info{$node}{"Allocated"}{"ephemeral-storage"} / $node_info{$node}{"Allocatable"}{"ephemeral-storage"}) * 100;
        if ($usage_percent < $variables{'node_utilization_disk_threshold'}) {
            # push @details, "Check successful on node $node because ephemeral-storage utilization of $usage_percent\% is below the limit of $variables{'node_utilization_disk_threshold'}\% (Allocatable=$node_info{$node}{Allocatable}{'ephemeral-storage'}, Allocated=$node_info{$node}{Allocated}{'ephemeral-storage'}).\n";
        } else {
            push @details, "Check failed on node $node because ephemeral-storage utilization of $usage_percent\% is over the limit of $variables{'node_utilization_disk_threshold'}\% (Allocatable=$node_info{$node}{Allocatable}{'ephemeral-storage'}, Allocated=$node_info{$node}{Allocated}{'ephemeral-storage'}).\n";
            $node_result = 1;
        }

        $usage_percent = sprintf "%.2f", ($node_info{$node}{"Allocated"}{"pods"} / $node_info{$node}{"Allocatable"}{"pods"}) * 100;
        if ($usage_percent < $variables{'node_utilization_pods_threshold'}) {
            # push @details, "Check successful on node $node because pods utilization of $usage_percent\% is below the limit of $variables{'node_utilization_pods_threshold'}\% (Allocatable=$node_info{$node}{Allocatable}{pods}, Allocated=$node_info{$node}{Allocated}{pods}).\n";
        } else {
            push @details, "Check failed on node $node because pods utilization of $usage_percent\% is over the limit of $variables{'node_utilization_pods_threshold'}\% (Allocatable=$node_info{$node}{Allocatable}{pods}, Allocated=$node_info{$node}{Allocated}{pods}).\n";
            $node_result = 1;
        }
    }

    if ($node_result == 0) {
        #report_details("All nodes have normal resource utilization.\n" . join "", @details);
    } else {
        report_failed("One or more nodes have high resource utilization.\n" . join "", @details);
    }
}

# -----------------------------------------------------------------------------
sub check_pm_files {
    my $command = "";
    my $pod_name;
    my @pod_names;
    my $rc;
    my @result;

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    @pod_names = ADP::Kubernetes_Operations::get_pod_names(
        {
            "namespace"         => $namespace,
            "pod-include-list"  => [ 'eric-pm-bulk-reporter-\S+', 'eric-data-object-storage-mn-0' ],
            "hide-output"       => 1,
        }
    );
    for $pod_name ( @pod_names ) {
        if ( $pod_name eq "eric-data-object-storage-mn-0" ) {
            $command = "kubectl -n $namespace exec -ti $pod_name" . $kubeconfig . " -- ls --full-time -ltr /export/eric-pmbr-rop-file-store/";
            # If object-storage pod is present then we always use this one
            last;
        } elsif ( $pod_name =~ /^eric-pm-bulk-reporter-\S+/ ) {
            # Save the command just in case this is the only pod found, then continue looking for other pods
            $command = "kubectl -n $namespace exec -ti $pod_name -c eric-pm-bulk-reporter" . $kubeconfig . " -- ls --full-time -ltr /PerformanceManagementReportFiles/";
        }
    }
    if ( $command ne "" ) {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );

        # Loop over command results and find any nodes which are not ready
        if ( $rc == 0 && @result != 0 ) {

            if ( $result[-1] =~ /^\S+\s+\S+\s+\S+\s+\S+\s+(\d+)\s+(.+)\s+(A\d{8}\.\d{4}.+)/) {
                # -rw-rw----+ 1 28276 10000 1505 2021-07-13 11:25:03.664240418 +0200 A20210713.1120+0200-1125+0200_Pikachu.xml.gz
                # Check last collected file
                my $file_size = $1;
                my $file_date = $2;
                my $current_time = time();                      # Current Epoch time (Seconds since 1970-01-01)
                my $file_time = `date -d '$file_date' +\%s`;    # File Epoch time

                if ( abs( $current_time - $file_time ) > $variables{'pm_files_max_time_deviation'} ) {
                    report_failed("Check failed because the last produced PM file was created on '$file_date' which is older than set limit of $variables{'pm_files_max_time_deviation'} seconds.\n$result[-1]\n");
                }
                if ( $file_size < $variables{'pm_files_size_limit'} ) {
                    report_failed("Check failed because the last produced PM file has a size of '$file_size' which is smaller than set limit of $variables{'pm_files_size_limit'} bytes.\n$result[-1]\n");
                }
            } else {
                report_failed("Check failed because the last file did not match the expected PM file naming convention.\n$result[-1]\n");
            }
        } else {
            report_failed("Check failed because kubectl command failed or nothing was returned.\n" . join "\n", @result);
        }
    } else {
        report_failed("Check failed because no eric-pm-bulk-reporter or eric-data-object-storage-mn-0 pods found in namespace $namespace.\n");
    }
}

# -----------------------------------------------------------------------------
sub check_pm_system_jobs {
    my $cmyp_password = $variables{'cmyp_password'} ne "" ? $variables{'cmyp_password'} : "rootroot";
    my $cmyp_port = 22;
    my $cmyp_user = $variables{'cmyp_user'} ne "" ? $variables{'cmyp_user'} : "expert";
    my @commands = ();
    my $rc;
    my @result;

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    my %enabled_cnf = map { $_ => 1 } ADP::Kubernetes_Operations::get_enabled_cnf($namespace);
    if (exists $enabled_cnf{'bsf'}) {
        push @commands, "show pm job bsf_job current-job-state";
    }
    if (exists $enabled_cnf{'scp'}) {
        push @commands, "show pm job scp_ingress_rate_limit_job current-job-state";
        push @commands, "show pm job scp_system_job current-job-stat";
    }
    if (exists $enabled_cnf{'sepp'}) {
        push @commands, "show pm job sepp_ingress_rate_limit_job current-job-state";
        push @commands, "show pm job sepp_system_job current-job-state";
        push @commands, "show pm job sepp_topology_hiding_job current-job-state";
    }
    if (exists $enabled_cnf{'rlf'}) {
        push @commands, "show pm job rlf_service_job current-job-state";
        push @commands, "show pm job rlf_system_job current-job-state";
    }
    if (exists $enabled_cnf{'slf'}) {
        if (version->parse( $result_information->{'software_version'} ) >= version->parse( "1.15.0" ) ) {
            # This only exist from 1.15.0 and up
            push @commands, "show pm job slf_system_job current-job-state";
        }
    }

    if ($cmyp_ip eq "") {
        (undef, $cmyp_ip, $cmyp_port, undef, undef) = ADP::Kubernetes_Operations::get_cmyp_info($namespace);
    }

    unless ($cmyp_ip) {
        report_failed("Failed to fetch CMYP IP_address.");
        return;
    }

    $rc = General::OS_Operations::send_command_via_ssh(
        {
            "commands"          => \@commands,
            "hide-output"       => 1,
            "return-output"     => \@result,
            "stop-on-error"     => 0,
            "timeout"           => 10,
            "ip"                => $cmyp_ip,
            "port"              => $cmyp_port,
            "password"          => $cmyp_password,
            "user"              => $cmyp_user,
            "use-standard-ssh"  => 0,   # use send_command_to_ssh.exp
        }
    );

    if ( $rc != 0 || scalar @result == 0) {
        if (scalar @result > 0) {
            report_failed("One or more PM job checks failed.\n" . join "\n", @result);
        } else {
            report_failed("The command failed and/or returned no data.\n");
        }
        return;
    }

    my $active_cnt = 0;
    my $not_active_cnt = 0;
    for (@result) {
        if (/^current-job-state\s+(\S+)/) {
            if ($1 eq "active") {
                $active_cnt++;
            } else {
                $not_active_cnt++;
            }
        }
    }

    if ($not_active_cnt > 0) {
        report_failed("There are $not_active_cnt PM jobs that are not 'active'.\n" . join "\n", @result);
    } elsif ($active_cnt != scalar @commands) {
        report_failed((sprintf "Expected to find %i PM jobs that are 'active', but only found %i.\n", scalar @commands, $active_cnt) . join "\n", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_pod_logs {
    my @faults;
    my @pod_names;
    my $rc;
    my @result;
    my $since = (exists $variables{'pod_logs_since'} && $variables{'pod_logs_since'} ne "") ? " --since $variables{'pod_logs_since'}" : "";

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    @pod_names = ADP::Kubernetes_Operations::get_pod_names(
        {
            "namespace"         => $namespace,
            "pod-include-list"  => [ '^eric-[^-]+-manager-', '^eric-[^-]+-worker-', '^eric-dsc-fdr-', '^eric-dsc-agent-' ],
            "hide-output"       => 1,
        }
    );
    if ( @pod_names != 0 ) {
        for my $pod_name ( @pod_names ) {

            print_verbose("  Fetching logs from pod $pod_name\n");

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "kubectl -n $namespace logs $pod_name --all-containers --timestamps" . $since . $kubeconfig,
                    "hide-output"   => 1,
                    "raw-output"    => 1,
                    "return-output" => \@result,
                }
            );

            if ( $rc == 0 ) {
                # TODO: Add some logic to check the log entries
            } else {
                push @faults, "kubectl logs command failed for pod $pod_name.\n" . (join "", @result) . "\n";
            }
        }
        if (@faults) {
            # Report faults
            report_failed(join "", @faults);
        }
    } else {
        report_skipped("Check skipped because no manager or worker pods found.\n");
    }
}

# -----------------------------------------------------------------------------
sub check_pod_ready_status {
    my $command = "kubectl get pods --no-headers" . $kubeconfig;
    my @failure_info;
    my $line;
    my $pod_name;
    my $rc;
    my @result;
    my $success = 1;

    if ( $namespace eq "" ) {
        # No namespace given or error fetching the namespace, fetch information for all namespaces.
        $command .= " --all-namespaces";
    } else {
        # Namespace given, fetch information for the specific namespaces.
        $command .= " --namespace=$namespace";
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
        }
    );

    # rc check
    if ( $rc == 0 && @result != 0 ) {
        # Loop over command results and find pods where the pod STATUS or READY columns don't match the expected values
        foreach( @result ) {
            $line = $_;

            if ( /^((\S+)\s+(\S+)|\s*(\S+))\s+(\d+)\/(\d+)\s+(\S+)\s+(\d+)\s+(\S+).*/ ) {
                # $2              $3 or $4                                                         $5/6   $7          $8    $9
                #                 eric-sc-manager-5f59b58dd9-srx54                                  2/2   Running     0     2d19h
                #   or
                # eiffelesc       eric-sc-manager-5f59b58dd9-srx54                                  2/2   Running     0     2d21h
                if (defined $2) {
                    $pod_name = $3;
                } else {
                    $pod_name = $4;
                }
                next if ($pod_name =~ /^eric-data-search-engine-curator-\S+$/);
                next if ($pod_name =~ /^eric-log-transformer-\S+$/);
                next if ($7 eq "Completed");
                unless ( $5 == $6 && $7 eq "Running" ) {
                    if ($7 eq "Running") {
                        push @failure_info, "Ready numbers are not the same: $_";
                    } else {
                        push @failure_info, "Status is not 'Running':        $_";
                    }
                    $success = 0;
                }
            }
        }
        if ( $success == 0 ) {
            report_failed(join "", @failure_info);
        }

    } else {
        report_failed("kubectl get pods command failed / no information found.\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_pod_restarts {
    my @allow_list = ();
    my $command = "kubectl get pods --no-headers" . $kubeconfig;
    my $container_restart_timestamp;
    my $current_timestamp;
    my @failure_info;
    my @ignored_pods = ();
    my @ignored_reason = ();
    my $line;
    my $pod_name;
    my $rc;
    my @result;
    my $seconds_since_restart;
    my $success = 1;
    my $temp_namespace;

    if ($variables{'pod_restarts_allow_file'} ne "") {
        if (General::File_Operations::read_file( { "filename" => $variables{'pod_restarts_allow_file'}, "output-ref" => \@allow_list, "ignore-empty-lines" => 1, "ignore-pattern" => '^\s*#' } ) != 0) {
            print "Unable to read the file $variables{'pod_restarts_allow_file'}, file ignored\n";
            @allow_list = ();
        }
    } elsif (@allow_list_pod_restarts) {
        # Use the default allow list
        for my $hash_ref (@allow_list_pod_restarts) {
            if (exists $hash_ref->{'pod_name_regexp'}) {
                push @allow_list, $hash_ref->{'pod_name_regexp'};
            }
        }
        if (@allow_list) {
            General::Logging::log_write("Used allow list:\n" . join "\n", @allow_list) if $logfile;
        }
    }

    if ( $namespace eq "" ) {
        # No namespace given or error fetching the namespace, fetch information for all namespaces.
        $command .= " --all-namespaces";
    } else {
        # Namespace given, fetch information for the specific namespaces.
        $command .= " --namespace=$namespace";
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
        }
    );

    # rc check
    if ( $rc == 0 && @result != 0 ) {
        # Loop over command results and find pods that have restarted within the allowed time interval.
        foreach( @result ) {
            $line = $_;

            if ( /^((\S+)\s+(\S+)|\s*(\S+))\s+(\d+)\/(\d+)\s+(\S+)\s+(\d+)\s+(\S+).*/ ) {
                # $2              $3 or $4                                                         $5/6   $7          $8    $9
                #                 eric-sc-manager-5f59b58dd9-srx54                                  2/2   Running     0     2d19h
                #   or
                # eiffelesc       eric-sc-manager-5f59b58dd9-srx54                                  2/2   Running     0     2d21h
                if (defined $2) {
                    $temp_namespace = $2;
                    $pod_name = $3;
                } else {
                    $temp_namespace = $namespace;
                    $pod_name = $4;
                }
                # If the pod has restarted then check when it restarted and check if it happened recently
                if ( $8 > 0 ) {
                    $container_restart_timestamp = get_last_pod_restart_time($temp_namespace, $pod_name);
                    $current_timestamp = time();
                    if ( ( $current_timestamp - $container_restart_timestamp ) < $variables{'pod_restarts_time_threshold'} ) {
                        $seconds_since_restart = $current_timestamp - $container_restart_timestamp;
                        if ( $seconds_since_restart < $variables{'pod_restarts_time_threshold'} ) {
                            if (@allow_list) {
                                my $skip = 0;
                                for (@allow_list) {
                                    if ($pod_name =~ /$_/) {
                                        # The pod name path match a line in the allow_list, so it will be skipped
                                        $skip++;
                                        push @ignored_pods, "  $pod_name\n";
                                        push @ignored_reason, "Pod restart in pod $pod_name ignored because of '$_'\n";
                                        last;
                                    }
                                }
                                next if ($skip > 0);
                            }
                            push @failure_info, sprintf "The pod has recently restarted (%s ago): $line", General::OS_Operations::convert_seconds_to_dhms($seconds_since_restart, 1);
                            $success = 0;
                        }
                    }
                }
            }
        }

        if (@ignored_reason) {
            General::Logging::log_write("The following pod restarts has been ignored because they match the allow file entries:\n" . join "", @ignored_reason) if $logfile;
        }

        if (@ignored_pods) {
            unshift @ignored_pods, "\nThe following pod restarts has been ignored because they match the allow file entries:\n";
        }

        if ( $success == 0 ) {
            unshift @failure_info, "The threshold value used is $variables{'pod_restarts_time_threshold'} seconds or " . General::OS_Operations::convert_seconds_to_dhms($variables{'pod_restarts_time_threshold'}, 1) . "\n";
            report_failed(join "", @failure_info, @ignored_pods);
        } elsif (@ignored_pods) {
            # Report details
            report_details(join "", @ignored_pods);
        }

    } else {
        report_failed("kubectl get pods command failed / no information found.\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_pod_start_restarts {
    my @allow_list = ();
    my $command = "kubectl get pods --no-headers" . $kubeconfig;
    my $container_restart_timestamp;
    my $current_timestamp;
    my @failure_info;
    my @ignored_pods = ();
    my @ignored_reason = ();
    my $line;
    my $pod_name;
    my $rc;
    my @result;
    my $seconds_since_restart;
    my $success = 1;
    my $temp_namespace;

    if ($variables{'pod_restarts_allow_file'} ne "") {
        if (General::File_Operations::read_file( { "filename" => $variables{'pod_restarts_allow_file'}, "output-ref" => \@allow_list, "ignore-empty-lines" => 1, "ignore-pattern" => '^\s*#' } ) != 0) {
            print "Unable to read the file $variables{'pod_restarts_allow_file'}, file ignored\n";
            @allow_list = ();
        }
    } elsif (@allow_list_pod_restarts) {
        # Use the default allow list
        for my $hash_ref (@allow_list_pod_restarts) {
            if (exists $hash_ref->{'pod_name_regexp'}) {
                push @allow_list, $hash_ref->{'pod_name_regexp'};
            }
        }
        if (@allow_list) {
            General::Logging::log_write("Used allow list:\n" . join "\n", @allow_list) if $logfile;
        }
    }

    if ( $namespace eq "" ) {
        # No namespace given or error fetching the namespace, fetch information for all namespaces.
        $command .= " --all-namespaces";
    } else {
        # Namespace given, fetch information for the specific namespaces.
        $command .= " --namespace=$namespace";
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
        }
    );

    # rc check
    if ( $rc == 0 && @result != 0 ) {
        # Loop over command results and find pods that have started or restarted within the allowed time interval.
        foreach( @result ) {
            $line = $_;

            if ( /^((\S+)\s+(\S+)|\s*(\S+))\s+(\d+)\/(\d+)\s+(\S+)\s+(\d+)\s+(\S+).*/ ) {
                # $2              $3 or $4                                                         $5/6   $7          $8    $9
                #                 eric-sc-manager-5f59b58dd9-srx54                                  2/2   Running     0     2d19h
                #   or
                # eiffelesc       eric-sc-manager-5f59b58dd9-srx54                                  2/2   Running     0     2d21h
                if (defined $2) {
                    $temp_namespace = $2;
                    $pod_name = $3;
                } else {
                    $temp_namespace = $namespace;
                    $pod_name = $4;
                }
                # Check when the pod was started or restarted and check if it happened recently
                $container_restart_timestamp = get_last_pod_start_restart_time($temp_namespace, $pod_name);
                $current_timestamp = time();
                $seconds_since_restart = $current_timestamp - $container_restart_timestamp;
                if ( $seconds_since_restart < $variables{'pod_restarts_time_threshold'} ) {
                    if (@allow_list) {
                        my $skip = 0;
                        for (@allow_list) {
                            if ($pod_name =~ /$_/) {
                                # The pod name path match a line in the allow_list, so it will be skipped
                                $skip++;
                                push @ignored_pods, "  $pod_name\n";
                                push @ignored_reason, "Pod restart in pod $pod_name ignored because of '$_'\n";
                                last;
                            }
                        }
                        next if ($skip > 0);
                    }
                    push @failure_info, sprintf "The pod has recently started or restarted (%s ago): $line", General::OS_Operations::convert_seconds_to_dhms($seconds_since_restart, 1);
                    $success = 0;
                }
            }
        }

        if (@ignored_reason) {
            General::Logging::log_write("The following pod start/restarts has been ignored because they match the allow file entries:\n" . join "", @ignored_reason) if $logfile;
        }

        if (@ignored_pods) {
            unshift @ignored_pods, "\nThe following pod start/restarts has been ignored because they match the allow file entries:\n";
        }

        if ( $success == 0 ) {
            unshift @failure_info, "The threshold value used is $variables{'pod_restarts_time_threshold'} seconds or " . General::OS_Operations::convert_seconds_to_dhms($variables{'pod_restarts_time_threshold'}, 1) . "\n";
            report_failed(join "", @failure_info, @ignored_pods);
        } elsif (@ignored_pods) {
            # Report details
            report_details(join "", @ignored_pods);
        }

    } else {
        report_failed("kubectl get pods command failed / no information found.\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_pvc {
    my $kubectl_get_pvc = 0;
    my $rc;
    my @result;

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get pvc -n $namespace -o jsonpath=\'{range .items[*]}{.metadata.name}{\"\\t\"}{.status.phase}{\"\\n\"}\'" . $kubeconfig,
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
        }
    );

    if ( $rc == 0 && @result != 0 ) {
        foreach( @result ) {
            if( $_ =~ m/^.*^(Bound).*$/ ) {
                $kubectl_get_pvc = 1;
            }
        }

        if ( $kubectl_get_pvc == 1 ) {
            report_failed("kubectl get pvc command failed / some pvcs not Bound in namespace $namespace.\n" . join "", @result);
        }

    } else {
        report_failed("kubectl get pvc command failed / no pvcs found for namespace $namespace.\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_registry_pod_images {
    my $kubectl_get_pods;
    my $rc;
    my %found_registry_pods;
    my $image_cnt = 0;
    my $registry_pod_name = "";
    my $registry_pod_name_matches = 0;
    my %registry_pod_white_list;
        $registry_pod_white_list{'eric-aat'} = "";
        $registry_pod_white_list{'eric-atmoz-sftp'} = "";
        $registry_pod_white_list{'eric-bsf-load'} = "";
        $registry_pod_white_list{'eric-chfsim'} = "";
        $registry_pod_white_list{'eric-chfsim-redis'} = "";
        $registry_pod_white_list{'eric-dscload'} = "";
        $registry_pod_white_list{'eric-dscload-deployment'} = "";
        $registry_pod_white_list{'eric-influxdb'} = "";
        $registry_pod_white_list{'eric-k6'} = "";
        $registry_pod_white_list{'eric-nrfsim'} = "";
        $registry_pod_white_list{'eric-sc-aat-ts'} = "";
        $registry_pod_white_list{'eric-seppsim'} = "";
        $registry_pod_white_list{'eric-vtaprecorder'} = "";
        $registry_pod_white_list{'jcatserver'} = "";
        $registry_pod_white_list{'ubuntu'} = "";
    my $result;
    my @result;
    my @std_err;

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    if ($own_registry_url ne "") {
        $rc = General::OS_Operations::send_command(
            {
                "command"       => "kubectl describe pods -n $namespace" . $kubeconfig . " | grep -ie 'Image:' | sort -u",
                "hide-output"   => 1,
                "raw-output"    => 1,
                "return-output" => \@result,
                "return-stderr" => \@std_err,
            }
        );

        if ( @result != 0 ) {
            foreach $result ( @result ) {
                if ( $result !~ m/Image:.*($own_registry_url).*/ ) {
                    # Image with incorrect URL
                    $image_cnt++;
                    if ( $result =~ m/Image:.*\/([^"]+):.*/ ) {
                        $registry_pod_name = $1;
                        if ( exists $registry_pod_white_list{$registry_pod_name} ) {
                            $registry_pod_name_matches = 1;
                        } else {
                            $registry_pod_name_matches = 0;
                            $found_registry_pods{$result}++;
                         }
                    }
                } elsif ( $result =~ m/Image:.*($own_registry_url).*/ ) {
                    # Image with correct URL
                    $image_cnt++;
                }
            }

            if ( %found_registry_pods ) {
                report_failed("Found images:\n" . join("", @result) . "\nNot in the whitelist:\n" . join("", keys %found_registry_pods));
            }
        }
        if ( $rc != 0 ) {
            if ( %found_registry_pods ) {
                # The command failed but it did find some images that were not having the correct
                # URL and it's already been reported above, so nothing else to be done.
            } elsif ( $image_cnt > 0) {
                # The command failed but returned some Images that had the correct URL so maybe the
                # command failed for some other reason. We just report this as a warning.
                report_warning("kubectl describe pods command failed for some reason but pods found for namespace $namespace where the image URL was correct, so we classify this as a warning.\nSee all.log for details why it failed.\n");
            } else {
                report_failed("kubectl describe pods command failed and no pods with images found for namespace $namespace.\n" . join "", @result, @std_err);
            }
            # This is just some extra logic to try to find out why it gives an error.
            # When we know the reason then this whole 'send_command...' logic below can be removed.
            General::OS_Operations::send_command(
                {
                    "command"       => "kubectl describe pods -n $namespace" . $kubeconfig,
                    "hide-output"   => 1,
                }
            );
        }
    } else {
        # Also considered OK but we report it under details
        report_skipped("kubectl describe pods command skipped because no local registry URL provided.\n");
    }
}

# -----------------------------------------------------------------------------
sub check_replicasets {
    my $kubectl_get_replicasets = 0;
    my @problems = ();
    my $rc;
    my @result;

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get replicasets -n $namespace -o jsonpath=\'{range .items[*]}{.metadata.name}{\"\\t\"}{.status}{\"\\n\"}\'" . $kubeconfig,
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
        }
    );

    # Loop over replica sets to find not ready or missing replicas
    if ( $rc == 0 && @result != 0 ) {
        foreach( @result ) {
            if ( $_ =~ m/^.*availableReplicas"*:([0-9]+).*replicas"*:([0-9]+).*$/ ) {
                if ( $1 != $2 ) {
                    $kubectl_get_replicasets = 1;
                    push @problems, $_;
                }
            } elsif ( $_ =~ m/^.*replicas"*:([0-9]+).*$/ ) {
                if ($1 != 0) {
                    # Only count it as an error if value is not 0
                    $kubectl_get_replicasets = 1;
                    push @problems, $_;
                }
            }
        }

        if ( $kubectl_get_replicasets == 1 ) {
            report_failed("kubectl get replicasets command failed / some replica sets not available in namespace $namespace.\n" . join "", @problems);
        }
    } else {
        report_failed("kubectl get replicasets command failed / no replicasets found for namespace $namespace.\n" . join "", @problems);
    }
}

# -----------------------------------------------------------------------------
sub check_storageclass_backend {
    my $rc;
    my @result;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl describe storageclass" . $kubeconfig,
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
        }
    );
    if ( $rc == 0 && @result != 0 ) {
        # TODO: Maybe some more checks are needed or the existing check should be changed.
        for ( @result ) {
            if (/Events:\s+(.+)$/) {
                if ( $1 ne "<none>" ) {
                    report_failed("Events are present.\n" . join "", @result);
                    last;
                }
            }
        }
    } else {
        report_failed("kubectl describe storageclass command failed / kubectl not found.\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_svc {
    my $errors = "";
    my $rc;
    my @result;

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get svc -n $namespace -o jsonpath=\'{range .items[*]}{.metadata.name}{\"\\t\"}{.spec.loadBalancerIP}{\"\\n\"}\'" . $kubeconfig,
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
        }
    );

    if ( $rc == 0 && @result != 0 ) {
        foreach( @result ) {
            if( $_ =~ m/^.*(pending).*$/ ) {
                $errors .= $$_;
            }
        }

        if ( $errors ne "" ) {
            report_failed("kubectl get svc command failed / some svcs pending in namespace $namespace.\n$errors");
        }

    } else {
        report_failed("kubectl get svc command failed / no svcs found for namespace $namespace.\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_time_synchronization {
    my $connect_cnt = 0;
    my $connect_error_cnt = 0;
    my @failure_info;
    my %node_info;
    my $rc;
    my @result;
    my @error;
    my $starttime;
    my $timediff;
    my @warnings;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl get nodes -o wide --no-headers" . $kubeconfig,
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );
    if ( $rc == 0 && @result != 0 ) {
        for ( @result ) {
            if (/^(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+.+/) {
                # master-0-eccd-sc-pikachu                Ready   control-plane,master   26h    v1.21.1   fd08::9    <none>   SUSE Linux Enterprise Server 15 SP2   5.3.18-24.67-default   containerd://1.4.4
                $node_info{$1}{'ip'} = $6;
                $node_info{$1}{'epochtime'} = 0;
            }
        }

        # Keep track of the start time.
        # The information will be used to calculate the time
        # differences between all nodes.
        $starttime = time();

        # Connect to each node and check the time
        for my $node ( sort keys %node_info ) {
            $connect_cnt++;
            print_verbose("  Fetching time from node $node\n");
            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR eccd\@$node_info{$node}{'ip'} 'timedatectl; date +\%s'",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                    "return-stderr" => \@error,
                }
            );
            #       Local time: Fri 2021-07-09 17:19:16 UTC
            #   Universal time: Fri 2021-07-09 17:19:16 UTC
            #           RTC time: Fri 2021-07-09 17:19:17
            #       Time zone: UTC (UTC, +0000)
            #   Network time on: no
            #   NTP synchronized: yes
            #   RTC in local TZ: no
            #   1625851156

            # Check how long time it took to fetch the information.
            $timediff = time() - $starttime;

            if ( $rc == 0 && @result != 0 ) {
                # Check the result
                for ( @result ) {
                    if (/^(\d+)$/) {
                        # Subtract the time difference from the start of first check.
                        $node_info{$node}{'epochtime'} = $1 - $timediff;
                    } elsif (/^\s*NTP synchronized:\s+(\S+)/) {
                        if ( $1 ne "yes" ) {
                            push @failure_info, "The node $node is not NTP synchronized.\n" . join "\n", @result;
                            delete $node_info{$node};
                        }
                    }
                }
            } else {
                if (grep /Permission denied \(publickey,password,keyboard-interactive\)/, @error) {
                    push @warnings, "\nFailed to connect to node $node, no or wrong public key used when fetching time information.\n" . (join "\n", @error, @result) . "\n";
                    $connect_error_cnt++;
                } elsif (grep /Permission denied \(publickey,keyboard-interactive\)/, @error) {
                    push @warnings, "\nFailed to connect to node $node, no or wrong public key used when fetching time information.\n" . (join "\n", @error, @result) . "\n";
                    $connect_error_cnt++;
                } elsif (grep /Connection timed out/, @error) {
                    push @warnings, "\nFailed to connect to node $node, Connection timed out when fetching time information.\n" . (join "\n", @error, @result) . "\n";
                    $connect_error_cnt++;
                } else {
                    push @failure_info, "\nFailed to fetch time information from the node $node.\n" . (join "\n", @error, @result) . "\n";
                }
                delete $node_info{$node};
                if ($connect_cnt == 1 && $connect_error_cnt == 1) {
                    push @warnings, "\nFailed to connect to the first node, Assuming the other nodes will also fail so stopping the check.\n";
                    last;
                }
            }
        }

        # Compare the time difference between the nodes and allow only a few seconds difference
        if ($connect_cnt == 1 && $connect_error_cnt == 1) {
            # Failed to connect to the first two nodes, skip the time difference check
        } else {
            for my $node ( sort keys %node_info ) {
                if ( abs( $node_info{$node}{'epochtime'} - $starttime ) > $variables{'time_synchronization_max_deviation'} ) {
                    push @failure_info, "The node $node has a time deviation > $variables{'time_synchronization_max_deviation'} seconds compared to node where the health check is running from.\n" . join "", @result;
                }
            }
        }

        if (scalar @failure_info != 0) {
            if (scalar @warnings != 0) {
                # Report both warnings and faults
                report_failed(join "", @warnings, @failure_info);
            } else {
                # Report faults
                report_failed(join "", @failure_info);
            }
        } elsif (scalar @warnings != 0) {
            # Mark test as warning if we have 1 or more warnings
            report_warning(join "", @warnings);
        }
    } else {
        report_failed("Failed to get IP addresses of all nodes.\n" . join "\n", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_top_nodes {
    my $kubectl_top_nodes = 0;
    my $rc;
    my @result;

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl top nodes" . $kubeconfig,
            "hide-output"   => 1,
            "raw-output"    => 1,
            "return-output" => \@result,
        }
    );

    # Loop over command results and find CPU / memory utilization over 90%
    if ( $rc == 0 && @result != 0 ) {
        foreach( @result ) {
            if ( $_ =~ /^.+\s(\d+)%.+\s(\d+)%/ ) {
                if ($1 >= $variables{'top_nodes_cpu_limit'}) {
                    $kubectl_top_nodes = 1;
                    last;
                }
                if ($2 >= $variables{'top_nodes_memory_limit'}) {
                    $kubectl_top_nodes = 1;
                    last;
                }
            }
        }

        if ( $kubectl_top_nodes == 1) {
            report_failed("Found hardware with CPU utilization of $variables{'top_nodes_cpu_limit'}\% or higher or memory utilization of $variables{'top_nodes_memory_limit'}\% or higher.\n" . join "", @result);
        }

    } else {
        report_failed("kubectl top nodes command failed / kubectl not found.\n" . join "", @result);
    }
}

# -----------------------------------------------------------------------------
sub check_top_pods {
    my $command = dirname(abs_path $0) . "/print_resource_statistics.pl";
    my @details;
    my $expected_output_format = 0;
    my $pod_result = 0;
    my $pod_white_list = '(eric-aat|eric-sc-aat-ts|eric-chfsim|eric-chfsim-redis|eric-influxdb|eric-k6|eric-nrfsim|eric-seppsim|eric-dscload-deployment|jcatserver).+';
    my $rc;
    my @result;

    unless (-f "$command") {
        report_failed("Unable to find the '$command' script.");
        return;
    }

    if ( $namespace eq "" ) {
        # No namespace given or error fetching the namespace, fetch information for all namespaces.
        $command .= " --all-namespaces";
    } else {
        # Namespace given, fetch information for the specific namespaces.
        $command .= " --namespace=$namespace";
    }

    # Output comma separated value format
    $command .= " --output-format=csv";

    if ($kubeconfig ne "") {
        $command .=  $kubeconfig;
    }

    # Add logging to file if wanted
    if ($logfile ne "") {
        $command .= " --log-file=$logfile";
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => $command,
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );

    unless ( $rc =~ /^(0|2)$/ && @result != 0 ) {
        report_failed("Command '$command' failed or returned no data.\n" . join "", @result);
        return;
    }

    # Parse the output
    for (@result) {
        next if /^\s*#/;    # Ignore comments
        #      1           2     3           4                    5                      6                 7                         8                           9                      10                                   11                                     12              13     14
        if (/^"Namespace","Pod","Container","CPU Limits \(cpu\)","CPU Requests \(cpu\)","CPU Top \(cpu\)","Memory Limits \(bytes\)","Memory Requests \(bytes\)","Memory Top \(bytes\)","Ephemeral-Storage Limits \(bytes\)","Ephemeral-Storage Requests \(bytes\)","PVC \(bytes\)","Node","State"\s*$/) {
            $expected_output_format = 1;
        #                                            1         2         3         4         5         6         7         8         9         10        11        12        13        14
        } elsif ($expected_output_format == 1 && /^"([^"]*)","([^"]*)","([^"]*)","([^"]*)","([^"]*)","([^"]*)","([^"]*)","([^"]*)","([^"]*)","([^"]*)","([^"]*)","([^"]*)","([^"]*)","([^"]*)"\s*$/) {
            my $temp_namespace = $1;
            my $pod = $2;
            my $container = $3;
            my $cpu_limits = $4;
            my $cpu_top = $6;
            my $memory_limits = $7;
            my $memory_top = $9;
            my $state = $14;
            my $usage_percent = 0;

            next unless $state eq "Running";
            next if ($pod eq "All Pods" || $container eq "All Containers");

            if ($cpu_limits > 0) {
                $usage_percent = sprintf "%.2f", ($cpu_top / $cpu_limits) * 100;
            } else {
                $usage_percent = 0;
            }
            if (exists $variables_regexp{'pod_utilization_cpu_threshold'}) {
                my $found = "";
                # Check if the pod name match any regexp
                for my $regexp (keys %{$variables_regexp{'pod_utilization_cpu_threshold'}}) {
                    if ($pod =~ /$regexp/) {
                        $found = $regexp;
                        last;
                    }
                }
                if ( $found ne "") {
                    if ($usage_percent < $variables_regexp{'pod_utilization_cpu_threshold'}{$found}) {
                        # push @details, "Check successful on namespace $temp_namespace, pod $pod and container $container because cpu utilization of $usage_percent\% is below the limit of $variables_regexp{'pod_utilization_cpu_threshold'}{$found}\% (Limits=$cpu_limits, Used=$cpu_top).\n";
                    } else {
                        push @details, "Check failed on namespace $temp_namespace, pod $pod and container $container because cpu utilization of $usage_percent\% is below the limit of $variables_regexp{'pod_utilization_cpu_threshold'}{$found}\% (Limits=$cpu_limits, Used=$cpu_top).\n";
                    }
                } elsif ($usage_percent < $variables{'pod_utilization_cpu_threshold'}) {
                    # push @details, "Check successful on namespace $temp_namespace, pod $pod and container $container because cpu utilization of $usage_percent\% is below the limit of $variables{'pod_utilization_cpu_threshold'}\% (Limits=$cpu_limits, Used=$cpu_top).\n";
                } else {
                    if ($variables{'top_pods_whitelist_tools'} eq "yes") {
                        if ($pod !~ /^$pod_white_list/) {
                            push @details, "Check failed on namespace $temp_namespace, pod $pod and container $container because cpu utilization of $usage_percent\% is above the limit of $variables{'pod_utilization_cpu_threshold'}\% (Limits=$cpu_limits, Used=$cpu_top).\n";
                            $pod_result = 1;
                        }
                    } else {
                        push @details, "Check failed on namespace $temp_namespace, pod $pod and container $container because cpu utilization of $usage_percent\% is above the limit of $variables{'pod_utilization_cpu_threshold'}\% (Limits=$cpu_limits, Used=$cpu_top).\n";
                        $pod_result = 1;
                    }
                }
            } elsif ($usage_percent < $variables{'pod_utilization_cpu_threshold'}) {
                # push @details, "Check successful on namespace $temp_namespace, pod $pod and container $container because cpu utilization of $usage_percent\% is below the limit of $variables{'pod_utilization_cpu_threshold'}\% (Limits=$cpu_limits, Used=$cpu_top).\n";
            } else {
                if ($variables{'top_pods_whitelist_tools'} eq "yes") {
                    if ($pod !~ /^$pod_white_list/) {
                        push @details, "Check failed on namespace $temp_namespace, pod $pod and container $container because cpu utilization of $usage_percent\% is above the limit of $variables{'pod_utilization_cpu_threshold'}\% (Limits=$cpu_limits, Used=$cpu_top).\n";
                        $pod_result = 1;
                    }
                } else {
                    push @details, "Check failed on namespace $temp_namespace, pod $pod and container $container because cpu utilization of $usage_percent\% is above the limit of $variables{'pod_utilization_cpu_threshold'}\% (Limits=$cpu_limits, Used=$cpu_top).\n";
                    $pod_result = 1;
                }
            }

            if ($memory_limits > 0) {
                $usage_percent = sprintf "%.2f", ($memory_top / $memory_limits) * 100;
            } else {
                $usage_percent = 0;
            }
            if (exists $variables_regexp{'pod_utilization_memory_threshold'}) {
                my $found = "";
                # Check if the pod name match any regexp
                for my $regexp (keys %{$variables_regexp{'pod_utilization_memory_threshold'}}) {
                    if ($pod =~ /$regexp/) {
                        $found = $regexp;
                        last;
                    }
                }
                if ( $found ne "") {
                    if ($usage_percent < $variables_regexp{'pod_utilization_memory_threshold'}{$found}) {
                        # push @details, "Check successful on namespace $temp_namespace, pod $pod and container $container because memory utilization of $usage_percent\% is below the limit of $variables_regexp{'pod_utilization_memory_threshold'}{$found}\% (Limits=$memory_limits, Used=$memory_top).\n";
                    } else {
                        push @details, "Check failed on namespace $temp_namespace, pod $pod and container $container because memory utilization of $usage_percent\% is below the limit of $variables_regexp{'pod_utilization_memory_threshold'}{$found}\% (Limits=$memory_limits, Used=$memory_top).\n";
                    }
                } elsif ($usage_percent < $variables{'pod_utilization_memory_threshold'}) {
                    # push @details, "Check successful on namespace $temp_namespace, pod $pod and container $container because memory utilization of $usage_percent\% is below the limit of $variables{'pod_utilization_memory_threshold'}\% (Limits=$memory_limits, Used=$memory_top).\n";
                } else {
                    if ($variables{'top_pods_whitelist_tools'} eq "yes") {
                        if ($pod !~ /^$pod_white_list/) {
                            push @details, "Check failed on namespace $temp_namespace, pod $pod and container $container because memory utilization of $usage_percent\% is above the limit of $variables{'pod_utilization_memory_threshold'}\% (Limits=$memory_limits, Used=$memory_top).\n";
                            $pod_result = 1;
                        }
                    } else {
                        push @details, "Check failed on namespace $temp_namespace, pod $pod and container $container because memory utilization of $usage_percent\% is above the limit of $variables{'pod_utilization_memory_threshold'}\% (Limits=$memory_limits, Used=$memory_top).\n";
                        $pod_result = 1;
                    }
                }
            } elsif ($usage_percent < $variables{'pod_utilization_memory_threshold'}) {
                # push @details, "Check successful on namespace $temp_namespace, pod $pod and container $container because memory utilization of $usage_percent\% is below the limit of $variables{'pod_utilization_memory_threshold'}\% (Limits=$memory_limits, Used=$memory_top).\n";
            } else {
                if ($variables{'top_pods_whitelist_tools'} eq "yes") {
                    if ($pod !~ /^$pod_white_list/) {
                        push @details, "Check failed on namespace $temp_namespace, pod $pod and container $container because memory utilization of $usage_percent\% is above the limit of $variables{'pod_utilization_memory_threshold'}\% (Limits=$memory_limits, Used=$memory_top).\n";
                        $pod_result = 1;
                    }
                } else {
                    push @details, "Check failed on namespace $temp_namespace, pod $pod and container $container because memory utilization of $usage_percent\% is above the limit of $variables{'pod_utilization_memory_threshold'}\% (Limits=$memory_limits, Used=$memory_top).\n";
                    $pod_result = 1;
                }
            }
        }
    }
    if ($pod_result == 0 && scalar @details > 0) {
        #report_details("All pod and container utilization checks was successful.\n" . join "", @details);
        if ($rc == 2) {
            report_warning("Fetching resource data might be incomplete because 'describe pod' command failed before retruning all data.\n");
        }
    } elsif ($pod_result == 1) {
        report_failed("One or more pod and container utilization checks failed.\n" . join "", @details);
    } elsif ($rc == 2) {
        report_warning("Fetching resource data might be incomplete because 'describe pod' command failed before retruning all data.\n");
    }
}

# -----------------------------------------------------------------------------
sub check_worker_core_dumps {
    get_core_dumps("worker");
}

# -----------------------------------------------------------------------------
sub check_worker_disk_usage {
    my $connect_cnt = 0;
    my $connect_error_cnt = 0;
    my @errors;
    my $rc;
    my @result;
    my @stderror;
    my @warnings;
    my @worker_ip;

    @worker_ip = ADP::Kubernetes_Operations::get_node_ip( { "type" => "worker" } );
    if ( @worker_ip != 0 ) {
        for my $ip ( @worker_ip ) {
            $connect_cnt++;

            print_verbose("  Fetching Disk usage from worker IP $ip\n");

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR $ip 'df -h /'",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                    "return-stderr" => \@stderror,
                }
            );

            if ( $rc == 0 && @result != 0 ) {
                # Filesystem      Size  Used Avail Use% Mounted on
                # /dev/vda3        60G   49G   11G  82% /

                my $found_use = 0;
                for ( @result ) {
                    if ( /^\S+\s+\S+\s+\S+\s+(\S+)\s+(\d+)\%\s+\/\s*$/ ) {
                        # /dev/vda3        60G   49G   11G  82% /
                        $found_use++;
                        if ( ADP::Kubernetes_Operations::convert_number_unit($1) < ADP::Kubernetes_Operations::convert_number_unit($variables{'worker_disk_usage_available_size_minimum'}) ) {
                            # Not enough available available disk space
                            push @errors, "Disk available space on Worker with IP $ip is $1 which is less than minimum allowed $variables{'worker_disk_usage_available_size_minimum'} value.\n" . (join "\n", @result) . "\n\n";
                        } elsif ( $2 >= $variables{'worker_disk_usage_threshold'} ) {
                            # Using more than threshold disk usage
                            push @warnings, "Disk usage on Worker with IP $ip is $2\% which is higher than the threshold $variables{'worker_disk_usage_threshold'}\% value.\n" . (join "\n", @result) . "\n\n";
                        }
                        last;
                    }
                }
                if ( $found_use == 0 ) {
                    push @warnings, "No disk usage found on worker with IP $ip.\n" . (join "\n", @result) . "\n\n";
                }
            } else {
                push @warnings, "Unable to fetch disk usage from worker  with IP $ip.\n" . (join "\n", @result) . "\n\n";
                if (grep /Permission denied \(publickey,password,keyboard-interactive\)/, @stderror) {
                    $connect_error_cnt++;
                } elsif (grep /Permission denied \(publickey,keyboard-interactive\)/, @stderror) {
                    $connect_error_cnt++;
                } elsif (grep /Connection timed out/, @stderror) {
                    $connect_error_cnt++;
                }
                if ($connect_cnt == 1 && $connect_error_cnt == 1) {
                    push @warnings, "Failed to connect to the first node, Assuming the other nodes will also fail so stopping the check.\n";
                    last;
                }
            }
        }
        if (@errors) {
            # Report faults
            if (@warnings) {
                report_failed(join "", @errors, @warnings);
            } else {
                report_failed(join "", @errors);
            }
        } elsif (@warnings) {
            report_warning(join "", @warnings);
        }
    } else {
        report_skipped("Check skipped because no worker IP addresses found.\n");
    }
}

#                  **************************
#                  *                        *
#                  * END HEALTH CHECK LOGIC *
#                  *                        *
#                  **************************

# -----------------------------------------------------------------------------
sub execute_check {
    $name = shift;

    push @{$result_information->{'checks'}->[$check_count]->{'details'}}, ();
    $result_information->{'checks'}->[$check_count]->{'execution_time'} += 0;
    $result_information->{'checks'}->[$check_count]->{'summary'} .= "";
    $result_information->{'checks'}->[$check_count]->{'verdict'} = "unknown";

    # Check that the wanted check has a subroutine to call to
    # avoid a runtime error.
    unless (exists $available_checks{$name}) {
        push @result_summary, "(x) '$name' check does not exist";
        push @return_codes, 1;
        $result_information->{'checks'}->[$check_count]->{'summary'} = "check does not exist";
        $result_information->{'checks'}->[$check_count]->{'verdict'} = "failed";
        return;
    }

    # Check if the user want to skip this check
    if ( exists $variables{"skip_$name"} && $variables{"skip_$name"} eq "yes" ) {
        push @result_summary, "(-) '$name' check skipped because skip_$name=yes";
        push @return_codes, 0;
        $result_information->{'checks'}->[$check_count]->{'summary'} = "check skipped because skip_$name=yes";
        $result_information->{'checks'}->[$check_count]->{'verdict'} = "skipped";
        return;
    }

    print_verbose("Executing check: $name\n");

    # We assume that the health check is successful unless the
    # health check subroutine calls subrountine 'report_failed'
    # or 'report_skipped'.
    # The check subroutine should not directly modify this variable.
    $check_result = "successful";

    # Call the specific health check subroutine
    if ($name =~ /^Health_Checks::\S+/) {
        $sub_name = "${name}::main";
    } else {
        $sub_name = "check_$name";
    }
    $subref = \&$sub_name;
    &$subref($name);

    # Check the result of the health check subroutine
    if ( $check_result eq "successful" ) {
        push @result_summary, "(/) '$name' check successful";
        push @return_codes, 0;
        $result_information->{'checks'}->[$check_count]->{'summary'} = "check successful";
        $result_information->{'checks'}->[$check_count]->{'verdict'} = "successful";
        $result_information->{'successful_count'}++;
    } elsif ( $check_result eq "skipped" ) {
        push @result_summary, "(-) '$name' check skipped";
        push @return_codes, 0;
        $result_information->{'checks'}->[$check_count]->{'summary'} = "check skipped";
        $result_information->{'checks'}->[$check_count]->{'verdict'} = "skipped";
        $result_information->{'skipped_count'}++;
    } elsif ( $check_result eq "failed" ) {
        if ( exists $variables{"ignore_failed_$name"} && $variables{"ignore_failed_$name"} eq "yes" ) {
            push @result_summary, "(/) '$name' check failed but ignored because ignore_failed_$name=yes";
            push @return_codes, 0;
            $result_information->{'checks'}->[$check_count]->{'summary'} = "check failed but ignored because ignore_failed_$name=yes";
            $result_information->{'checks'}->[$check_count]->{'verdict'} = "successful";
            $result_information->{'successful_count'}++;
        } elsif ( $available_checks{$name} eq "warning" ) {
            push @result_summary, "(!) '$name' check failed but classified as a warning";
            push @return_codes, 2;
            $result_information->{'checks'}->[$check_count]->{'summary'} = "check failed but classified as a warning";
            $result_information->{'checks'}->[$check_count]->{'verdict'} = "warning";
            $result_information->{'warning_count'}++;
        } else {
            push @result_summary, "(x) '$name' check failed";
            push @return_codes, 1;
            $result_information->{'checks'}->[$check_count]->{'summary'} = "check failed";
            $result_information->{'checks'}->[$check_count]->{'verdict'} = "failed";
            $result_information->{'failed_count'}++;
        }
    } elsif ( $check_result eq "warning" ) {
        push @result_summary, "(!) '$name' check classified as a warning";
        push @return_codes, 2;
        $result_information->{'checks'}->[$check_count]->{'summary'} = "check classified as a warning";
        $result_information->{'checks'}->[$check_count]->{'verdict'} = "warning";
        $result_information->{'warning_count'}++;
    } else {
        push @result_summary, "(x) '$name' check failed because of an unknown check result '$check_result'";
        push @return_codes, 1;
        $result_information->{'checks'}->[$check_count]->{'summary'} = "check failed because of an unknown check result '$check_result'";
        $result_information->{'checks'}->[$check_count]->{'verdict'} = "failed";
        $result_information->{'failed_count'}++;
    }
}

# -----------------------------------------------------------------------------
sub execute_pre_deployment_health_checks {
    push @checks, qw /helm_version
                      kubectl_version
                      docker_version
                      node_status
                      top_nodes/;
}

# -----------------------------------------------------------------------------
sub execute_post_deployment_health_checks {
    push @checks, qw /helm_list_status
                      endpoints
                      pvc
                      svc
                      registry_pod_images
                      top_pods/;
}

# -----------------------------------------------------------------------------
#
# This subroutine scans this file for all subroutine names starting with the
# string "check_" and updates the global hash variable %available_checks.
# It also scans the file for all setting of the global hash variable
# %variables and set the global hash variable %available_variables.
#
sub find_available_checks {
    my $file_path = $0;
    my $group;
    my $key1;
    my $key2;
    my @lines;
    my @missing_checks = ();
    my $package_name;
    my $plugin_dir = dirname(dirname abs_path $0) . '/lib/Health_Checks';
    my $plugin_file;
    my @plugin_files = ();
    my $rc;

    # Read the input file
    $rc = General::File_Operations::read_file(
        {
            "filename"            => $0,
            "output-ref"          => \@lines,
            "include-pattern"     => '^(\s*sub\s+check_\S+\s*{|\s*\$variables\{\'\S+\'\}\s*=\s*.+;\s*#\s*\S+)',
        }
    );
    if ($rc != 0) {
        if ($json_output == 0) {
            print "Unable to read file $file_path\n";
        } else {
            push @{$result_information->{'execution_errors'}}, "Unable to read file $file_path";
        }
        return;
    }
    for (@lines) {
        if (/^\s*sub\s+check_(\S+)\s*{/) {
            # Look for something like this:
            # sub check_top_pods {
            $available_checks{$1} = "fail";
        } elsif (/^\s*\$variables\{'(\S+)'\}\s*=\s*(.+);\s*#\s*(\S+)/) {
            # Look for something like this:
            # $variables{'top_nodes_cpu_limit'}           = 90;           # top_nodes
            $available_variables{$1}{'value'} = $2;
            $available_variables{$1}{'check'} = $3;
        }
    }

    # Check if we have any plugin health check scripts to load
    if ( -d "$plugin_dir" ) {
        @plugin_files = General::File_Operations::find_file(
            {
                "directory"     => $plugin_dir,
                "filename"      => '*.pm',
                "maxdepth"      => 1,
            }
        );
        if (@plugin_files > 0) {
            # There are some plugin health check scripts to load
            for $plugin_file (@plugin_files) {

                if ($plugin_file =~ /^.+\/(Health_Checks.+)\.pm$/) {
                    $package_name = $1;
                    $package_name =~ s/\//::/g;

                    # Ignore the example template script
                    if ($package_name eq "Health_Checks::00_Example_Check") {
                        next;
                    }

                    $name = $package_name;

                    eval "require $package_name";
                    if ($@) {
                        if ($json_output == 0) {
                            print "Plugin health check script $plugin_file failed to 'require', test ignored.\n$@\n";
                        } else {
                            push @{$result_information->{'execution_errors'}}, "Plugin health check script $plugin_file failed to 'require', test ignored.", split /\r*\n/, $@;
                        }
                        next;
                    }
                    eval "import $package_name";
                    if ($@) {
                        if ($json_output == 0) {
                            print "Plugin health check script $plugin_file failed to 'import', test ignored.\n$@\n";
                        } else {
                            push @{$result_information->{'execution_errors'}}, "Plugin health check script $plugin_file failed to 'import', test ignored.", split /\r*\n/, $@;
                        }
                        next;
                    }
                    $available_checks{$package_name} = "fail";
                }
            }
        }
    }

    # Check which groups exist in the %check_group_matrix
    for $key1 (keys %check_group_matrix) {
        for $key2 (keys %{$check_group_matrix{$key1}}) {
            $available_groups{$key2}++;
        }
    }

    # Check that the %check_group_matrix contains all existing checks, and if not update it.
    for $key1 (sort keys %available_checks) {
        if (exists $check_group_matrix{$key1}) {
            if ($group_name ne "" && exists $check_group_matrix{$key1}{$group_name}) {
                # Update the verdict to be used when analyzing the result of the check
                $available_checks{$key1} = $check_group_matrix{$key1}{$group_name};
            }
        } else {
            # A new check exist that was not included in the %check_group_matrix hash, add it with default values
            if ($json_output == 0) {
                print "The \%check_group_matrix hash is missing the check '$key1', using default values.\n";
            } else {
                push @{$result_information->{'execution_errors'}}, "The \%check_group_matrix hash is missing the check '$key1', using default values.";
            }
            push @missing_checks, $key1;
        }
    }
    for $key1 (@missing_checks) {
        for $group (keys %available_groups) {
            # Always skip new checks
            $check_group_matrix{$key1}{$group} = "skip";
        }
        # For the 'full' group execute the check
        $check_group_matrix{$key1}{'full'} = "fail";
    }

    # Copy the contents from %check_group_matrix into %group_check_matrix except swap
    # the keys.
    for $key1 (keys %check_group_matrix) {
        for $key2 (keys %{$check_group_matrix{$key1}}) {
            $group_check_matrix{$key2}{$key1} = $check_group_matrix{$key1}{$key2};
        }
    }
}

# -----------------------------------------------------------------------------
#
# This subroutine finds the application type 'sc' or 'dsc' by looking at the
# deployed software.
# It updates the %application_information hash of hashes with the following information:
#   - key1: The application name e.g. 'sc' or 'dsc'
#   - key2: One of the following:
#       - 'release_name': The release name of the deployed software.
#       - 'version': The deployed software version.
# If no SC or DSC deployment is found then it will write an error message to the log file
# and leave the %application_information hash undefined.
#
sub get_application_information {
    my $rc;
    my @result;

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return;
    }

    $rc = General::OS_Operations::send_command(
        {
            "command"       => "${helm_executable} list --namespace $namespace",
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );

    # Try to find the SC release name based on the deployed chart name
    if ( $rc == 0 && @result != 0 ) {
        foreach( @result ) {
            if ( $_ =~ m/^\s*(\S+)\s+$namespace\s+\d+\s+.+\s+\S+\s+eric-($application_type_regexp)-\D*(\d+\.\d+\.\d+)(\S+)/ ) {
                #eric-sc    eiffelesc       1           2023-03-03 02:31:21.271767605 +0000 UTC    failed      eric-sc-umbrella-1.11.25+1080
                #eric-sc    eiffelesc       1           2023-03-03 09:33:41.397462452 +0100 CET    deployed    eric-sc-umbrella-1.11.25+1080
                #  or with cnDSC
                #eric-dsc   cndsc           1           2023-02-08 09:39:14.244456472 +0100 CET    deployed    eric-dsc-1.9.2-1-h0e25834                         1.9.2-21
                #  or with CNCS
                #eric-cloud-native-base        	eiffelesc	1       	2024-04-23 09:38:28.026489327 +0000 UTC	deployed	eric-cloud-native-base-152.2.0        	152.2.0
                #eric-cloud-native-nf-additions	eiffelesc	1       	2024-04-23 09:39:27.74087286 +0000 UTC 	deployed	eric-cloud-native-nf-additions-48.10.0	48.10.0
                #eric-sc-bsf                   	eiffelesc	1       	2024-04-23 09:39:58.130357248 +0000 UTC	deployed	eric-sc-bsf-1.1.0-3-h27860fc          	1.1.0-3
                #eric-sc-cs                    	eiffelesc	1       	2024-04-23 09:39:47.36267819 +0000 UTC 	deployed	eric-sc-cs-2.0.0-26-h47e985d          	2.0.0-26
                #eric-sc-scp                   	eiffelesc	1       	2024-04-23 09:48:15.475006217 +0000 UTC	deployed	eric-sc-scp-1.1.0-4-h27860fc          	1.1.0-4
                #eric-sc-sepp                  	eiffelesc	1       	2024-04-23 09:48:18.717133068 +0000 UTC	deployed	eric-sc-sepp-1.1.0-3-h27860fc         	1.1.0-3
                $application_information{$2}{'release_name'} = $1;
                $application_information{$2}{'version'} = $3;
                $application_information{$2}{'build'} = $4;
                if ( $_ =~ m/^.+eric-dsc-\d+\.\d+\.\d+\S+\s+\d+\.\d+\.\d+(\S+)\s*$/ ) {
                    # Special handling for dsc build number
                    $application_information{'dsc'}{'build'} = $1;
                }
            }
        }

        unless ( %application_information ) {
            General::Logging::log_write("helm list command found no SC or DSC deployments in namespace $namespace.\n");
            return;
        }

        unless ($application) {
            # The $application value has not yet been set, now set it based on what is deployed
            if (exists $application_information{'sc-cs'}) {
                # New CNCS
                $application = "sc";
                $application_information{'sc'}{'release_name'} = $application_information{'sc-cs'}{'release_name'};
                # Fetch version either from eric-sc-cs helm chart or from CMYP
                my $semantic_version = ADP::Kubernetes_Operations::get_semantic_version(
                    $namespace,
                    $variables{'cmyp_user'} ne "" ? $variables{'cmyp_user'} : "expert",
                    $variables{'cmyp_password'} ne "" ? $variables{'cmyp_password'} : "rootroot"
                );
                if ($semantic_version =~ /^(\d+\.\d+\.\d+)(\S+)/) {
                   $application_information{'sc'}{'version'} = $1;
                   $application_information{'sc'}{'build'} = $2;
                } elsif ($semantic_version =~ /^(\d+\.\d+\.\d+)$/) {
                    $application_information{'sc'}{'version'} = $1;
                    $application_information{'sc'}{'build'} = "";
                } else {
                    # We didn't find the version.
                    # TODO: We now fake the version number until we have a better way of finding the real SC release version e.g. via SWIM information
                    $application_information{'sc'}{'version'} = "1.15.99";
                    $application_information{'sc'}{'build'} = "-CncsFakedVersionAndBuild";
                }
            } elsif (exists $application_information{'sc'}) {
                # The old SC (pre CNCS)
                $application = "sc";
            } elsif (exists $application_information{'dsc'}) {
                # The old cnDSC (pre CNCS)
                $application = "dsc";
            }
        }

        $rc = General::OS_Operations::send_command(
            {
                "command"       => "${helm_executable} get values --namespace $namespace $application_information{$application}{'release_name'} | grep --color=never nodeName",
                "hide-output"   => 1,
                "return-output" => \@result,
            }
        );
        if ( $rc == 0 && scalar @result == 1 && $result[0] =~ /^\s+nodeName:\s+(\S+)\s*$/) {
            #    nodeName: Snorlax
            $application_information{$application}{'node_name'} = $1;
        }
    } else {
        General::Logging::log_write("helm list command failed looking for SC or DSC deployments in namespace $namespace.\n");
        return;
    }
}

# -----------------------------------------------------------------------------
sub get_cmyp_config {
    my $config_dump_type = shift;
    my $cmyp_user = shift;
    my $cmyp_password = shift;
    my $xml_output = shift;

    my $command = dirname(dirname(dirname(abs_path $0))) . "/expect/bin/send_command_to_ssh.exp";
    my $printout_file = "";
    my $rc;
    my @result;
    my @std_err;

    # Comment out the following lines to get predictable results for debug purposes
    # $printout_file = "/proj/DSC/rebels/logs/test_printouts/config_check_test_file_20220221.log";

    if ($printout_file eq "") {
        if ( get_namespace() eq "" ) {
            # No namespace given or error fetching the namespace, skip this health check
            return;
        }

        unless (-f "$command") {
            report_failed("Unable to find the '$command' script.");
            return;
        }

        if ($cmyp_ip eq "") {
            # We have no cached value, so try to find the value
            $cmyp_ip = ADP::Kubernetes_Operations::get_cmyp_ip($namespace);
            unless ($cmyp_ip) {
                report_failed("Failed to fetch CMYP IP_address.");
                return;
            }
        }

        # Add more parameter for fetching alarm history from CMYP
        if ($xml_output) {
            $command .= " --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=30 --command='show running-config $config_dump_type | display xml | nomore'";
        } else {
            $command .= " --user=$cmyp_user --password=$cmyp_password --ip=$cmyp_ip --timeout=30 --command='show running-config $config_dump_type | nomore'";
        }

        $rc = General::OS_Operations::send_command(
            {
                "command"       => $command,
                "hide-output"   => 1,
                "return-output" => \@result,
                "return-stderr" => \@std_err,
            }
        );
    } else {
        $rc = General::File_Operations::read_file(
            {
                "filename"              => $printout_file,
                "output-ref"            => \@result,
            }
        );
    }

    if ( $rc == 0 ) {
        my $found = 0;
        # Check if no data was found
        for (@result) {
            if (/^\s*% No entries found/) {
                $found++;
            }
        }
        if ($found > 0) {
            report_failed("Command '$command' failed, no entries found.\n" . join "\n", @result);
        }
    } else {
        if (scalar @result > 0) {
            report_failed("Command '$command' failed.\n" . join "\n", @result);
        } else {
            report_failed("Command '$command' failed and returned no data.\n");
        }
    }
}

# -----------------------------------------------------------------------------
#
# This subroutine checks which CNF's are deployed by checking which pods exists
# and then returns a hash where the key is the cnf name.
#
# Input:
#  -
#
# Output:
#  - A hash where the key is the name of the CNF that exists.
#    Currently supported CNF's are:
#     - bsf
#     - scp
#     - sepp
#
sub get_cnf {
    my %cnf_hash;
    my @pod_names;

    if ( get_namespace() eq "" ) {
        # No namespace given or error fetching the namespace, skip this health check
        return %cnf_hash;
    }

    @pod_names = ADP::Kubernetes_Operations::get_pod_names(
        {
            "hide-output"       => 1,
            "namespace"         => $namespace,
        }
    );
    for (@pod_names) {
        if (/^eric-bsf-worker-\S+/) {
            $cnf_hash{'bsf'} = 1;
        } elsif (/^eric-scp-worker-\S+/) {
            $cnf_hash{'scp'} = 1;
        } elsif (/^eric-sepp-worker-\S+/) {
            $cnf_hash{'sepp'} = 1;
        }
    }
    return %cnf_hash;
}

# -----------------------------------------------------------------------------
#
# This subroutine checks for core dumps on either master, worker or all nodes
# with the possibility to also fetch the core dumps and delete them.
#
# Input:
#  - The type of node to check core dumps on
#    It can be one of the following:
#     - all
#     - master
#     - worker
#
sub get_core_dumps {
    my @allow_list = ();
    my $node_type = @_ ? lc(shift) : "all";

    my $connect_cnt = 0;
    my $connect_error_cnt = 0;
    my @error;
    my @faults;
    my @found_files = ();
    my @ignored_files = ();
    my @ignored_reason = ();
    my @node_ip;
    my $rc;
    my @result;
    my @warnings;

    if ($node_type !~ /^(all|master|worker)$/) {
        report_failed("Wrong node_type specified, only all, master or worker allowed");
        return;
    } elsif ($node_type eq "all") {
        $node_type = "";    # Needed for the get_node_ip subroutine
    }

    if ($variables{'core_dump_no_allow_file'} eq "no" && $variables{'core_dump_allow_file'} ne "") {
        if (General::File_Operations::read_file( { "filename" => $variables{'core_dump_allow_file'}, "output-ref" => \@allow_list, "ignore-empty-lines" => 1, "ignore-pattern" => '^\s*#' } ) != 0) {
            print "Unable to read the file $variables{'core_dump_allow_file'}, file ignored\n";
            @allow_list = ();
        }
    } elsif ($variables{'core_dump_no_allow_file'} eq "no" && @allow_list_coredumps) {
        # Use the default allow list
        for my $hash_ref (@allow_list_coredumps) {
            if (exists $hash_ref->{'filename_regexp'}) {
                push @allow_list, $hash_ref->{'filename_regexp'};
            }
        }
        if (@allow_list) {
            General::Logging::log_write("Used allow list:\n" . join "\n", @allow_list) if $logfile;
        }
    }

    if ($variables{'core_dump_fetch'} eq "yes") {
        if ($variables{'core_dump_directory'} eq "") {
            # It makes no sense to fetch the core dumps if we have no place to download them to, mark fetching not to be done.
            push @faults, "Variable 'core_dump_fetch=yes' set but no directory given by variable 'core_dump_directory', so we mark 'core_dump_fetch=no' and 'core_dump_delete=no' and fail this check.\n";
            $variables{'core_dump_fetch'} = "no";
            $variables{'core_dump_delete'} = "no";
        } else {
            unless (-d "$variables{'core_dump_directory'}") {
                $rc = General::OS_Operations::send_command(
                    {
                        "command"       => "mkdir -p $variables{'core_dump_directory'}",
                        "hide-output"   => 1,
                        "return-output" => \@result,
                    }
                );
                if ($rc != 0) {
                    # Creation of directory failed, so it makes no sense to fetch the core dumps if we have no place to download them to, mark fetching not to be done.
                    push @faults, "Creation of directory $variables{'core_dump_directory'} failed, so we mark 'core_dump_fetch=no' and 'core_dump_delete=no' and fail this check.\n" . join "\n", @result;
                    $variables{'core_dump_fetch'} = "no";
                    $variables{'core_dump_delete'} = "no";
                }
            }
        }
    }

    @node_ip = ADP::Kubernetes_Operations::get_node_ip( { "type" => $node_type } );
    if ( @node_ip != 0 ) {
        for my $ip ( @node_ip ) {
            $connect_cnt++;

            print_verbose("  Fetching Core Dump information from node IP $ip\n");

            @found_files = ();

            $rc = General::OS_Operations::send_command(
                {
                    "command"       => "ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR $ip 'sudo -n find /var/lib/systemd/coredump/ -type f'",
                    "hide-output"   => 1,
                    "return-output" => \@result,
                    "return-stderr" => \@error,
                }
            );

            if ( $rc == 0 ) {
                # /var/lib/systemd/coredump/core.dsc_agent.1000.3e15c7e402224370a6a3277a9f8a8819.9208.1664191728000000.zst

                my $coredump_timestamp;
                my $current_timestamp = time();
                my $found = 0;
                my $file_path;
                my $seconds_since_coredump;
                for ( @result ) {
                    $file_path = $_;
                    if ( $file_path =~ /^.+\/core\..+\.(\d+)\.\S+$/ ) {
                        # $1 should contain e.g. 1664191728000000
                        $coredump_timestamp = $1;

                        if (@allow_list) {
                            my $skip = 0;
                            for (@allow_list) {
                                if ($file_path =~ /$_/) {
                                    # The core dump file path match a line in the allow_list, so it will be skipped
                                    $skip++;
                                    push @ignored_files, "  $file_path\n";
                                    push @ignored_reason, "File on node with IP $ip ignored because of '$_': $file_path\n";
                                    last;
                                }
                            }
                            next if ($skip > 0);
                        }

                        if (length $coredump_timestamp == 16 && $coredump_timestamp =~ /^(\d{10})\d{6}$/) {
                            # Remove the microseconds value which is most likely just 000000.
                            # The first 10 digits should hold an epoch value from "Sun Sep  9 01:46:40 2001" up to "Sat Nov 20 17:46:39 2286"
                            $coredump_timestamp = $1;
                        }
                        $seconds_since_coredump = $current_timestamp - $coredump_timestamp;
                        if ( $variables{'core_dump_time_threshold'} == 0 ) {
                            # All core dumps are reported regardless of how old they are.
                            push @faults, sprintf "Core dump found on node with IP $ip created %s ago. File path: %s\n",
                                General::OS_Operations::convert_seconds_to_dhms($seconds_since_coredump, 1),
                                $file_path;
                            $found++;
                            push @found_files, $file_path;
                        } elsif ( $seconds_since_coredump < $variables{'core_dump_time_threshold'} ) {
                            push @faults, sprintf "Core dump found on node with IP $ip created %s ago which is sooner than threshold of %s. File path: %s\n",
                                General::OS_Operations::convert_seconds_to_dhms($seconds_since_coredump, 1),
                                General::OS_Operations::convert_seconds_to_dhms($variables{'core_dump_time_threshold'}, 1),
                                $file_path;
                            $found++;
                            push @found_files, $file_path;
                        } else {
                            push @ignored_files, "  $file_path\n";
                            push @ignored_reason, "File on node with IP $ip ignored because of file age $seconds_since_coredump >= $variables{'core_dump_time_threshold'} seconds: $file_path\n";
                        }
                    }
                }

                if (@found_files) {
                    # Create an hopefully unique temporary directory name to use on the node
                    my $dir_name = sprintf "/tmp/temp_core_dumps_%i", time();

                    # Create a list of the found files
                    my $files = join " ", @found_files;

                    # Check if we need to fetch the core dumps from the node
                    if ($variables{'core_dump_fetch'} eq "yes") {
                        # Create a sub directory per node in output directory
                        my $sub_dir = "$ip/";
                        $rc = General::OS_Operations::send_command(
                            {
                                "command"       => "mkdir $variables{'core_dump_directory'}/$sub_dir",
                                "hide-output"   => 1,
                            }
                        );
                        if ($rc != 0) {
                            # Creation of sub directory failed, so we just store the files in the main directory
                            $sub_dir = "";
                        }

                        # Fetch the files by first copying them to a temporary directory which can be accessed by anybody, ignoring any failures
                        General::OS_Operations::send_command(
                            {
                                "command"       => "ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR $ip 'sudo -n rm -fr $dir_name && sudo -n mkdir -m 755 $dir_name && sudo -n cp -p $files $dir_name && sudo -n chmod -R 644 $dir_name/*'",
                                "hide-output"   => 1,
                            }
                        );
                        # Next we fetch the core dumps from the temporary directory, ignoring any failures
                        General::OS_Operations::send_command(
                            {
                                "command"       => "scp -pr -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR $ip:$dir_name/* $variables{'core_dump_directory'}/$sub_dir",
                                "hide-output"   => 1,
                            }
                        );
                        # Finally we delete the temporary directory, ignoring any failures
                        General::OS_Operations::send_command(
                            {
                                "command"       => "ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR $ip 'sudo -n rm -fr $dir_name'",
                                "hide-output"   => 1,
                            }
                        );
                    }

                    # Check if we need to delete the core dumps from the node
                    if ($variables{'core_dump_delete'} eq "yes") {
                        # Delete the files and we don't care what the result is.
                        General::OS_Operations::send_command(
                            {
                                "command"       => "ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -o BatchMode=Yes -o loglevel=ERROR $ip 'sudo -n rm -f $files'",
                                "hide-output"   => 1,
                            }
                        );
                    }
                }
            } else {
                if (grep /Permission denied \(publickey,password,keyboard-interactive\)/, @error) {
                    $connect_error_cnt++;
                    push @warnings, "Unable to fetch Core Dump information from node with IP $ip.\n" . (join "\n", @result) . "\n\n";
                } elsif (grep /Permission denied \(publickey,keyboard-interactive\)/, @error) {
                    $connect_error_cnt++;
                    push @warnings, "Unable to fetch Core Dump information from node with IP $ip.\n" . (join "\n", @result) . "\n\n";
                } elsif (grep /Connection timed out/, @error) {
                    $connect_error_cnt++;
                    push @warnings, "Unable to fetch Core Dump information from node with IP $ip.\n" . (join "\n", @result) . "\n\n";
                } else {
                    push @faults, "Unable to fetch Core Dump information from node with IP $ip.\n" . (join "\n", @result) . "\n\n";
                }
                if ($connect_cnt == 1 && $connect_error_cnt == 1) {
                    push @warnings, "Failed to connect to the first node, Assuming the other nodes will also fail so stopping the check.\n";
                    last;
                }
            }
        }
        if (@ignored_reason) {
            General::Logging::log_write("The following core dump files has been ignored because they match the allow file entries or because of file age:\n" . join "", @ignored_reason) if $logfile;
        }

        if (@ignored_files) {
            unshift @ignored_files, "\nThe following core dump files has been ignored because they match the allow file entries or because of file age:\n";
        }
        if (@faults) {
            if (@warnings) {
                # Report faults and warnings
                report_failed(join "", @faults, @warnings, @ignored_files);
            } else {
                report_failed(join "", @faults, @ignored_files);
            }
        } elsif (@warnings) {
            # Report warnings
            report_warning(join "", @warnings, @ignored_files);
        } elsif (@ignored_files) {
            # Report details
            report_details(join "", @ignored_files);
        }
    } else {
        report_skipped("Check skipped because no node IP addresses found.\n");
    }
}

# -----------------------------------------------------------------------------
#
# This subroutine finds the last time the pod was restarted and it returns back
# an epoch time value for the restart.
#
sub get_last_pod_restart_time {
    my $namespace = shift;
    my $pod_name = shift;

    my $last_restart_epoch = 0;
    my $rc;
    my $restart_epoch = 0;
    my @result;
    my $timestamp;

    print_verbose("  Fetching restart information for pod $pod_name\n");
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl describe pod $pod_name -n $namespace" . $kubeconfig,
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );
    if ( $rc == 0 && @result != 0 ) {
        for ( @result ) {
            if ( /^\s+Started:\s+(.+)/ ) {
                $timestamp = $1;
            } elsif ( /^\s+Finished:\s+(.+)/ ) {
                $timestamp = $1;
            } elsif ( /\s+Restart Count:\s+(\d+)/ ) {
                if ( $1 > 0 && $timestamp ne "") {
                    # There was a restart for this container, now check the time when the restart either started or finsihed
                    if (exists $timestamp_epoch_lookup{$timestamp}) {
                        $restart_epoch = $timestamp_epoch_lookup{$timestamp};
                    } else {
                        $restart_epoch = sprintf "%d", `date -d "$timestamp" +\%s`;
                        $timestamp_epoch_lookup{$timestamp} = $restart_epoch;
                    }
                    $restart_epoch = sprintf "%d", `date -d "$timestamp" +\%s`;
                    $last_restart_epoch = $restart_epoch if ($restart_epoch > $last_restart_epoch);
                }
                # Clear the values for the next container
                $restart_epoch = 0;
                $timestamp = "";
            }
        }
        if ( $last_restart_epoch == 0 ) {
            report_failed("Did not find the last restart time for the pod '$pod_name'.\n" . join "\n", @result);
        }
    } else {
        report_failed("Failed to get the last restart time for the pod '$pod_name'.\n" . join "\n", @result);
    }

    return $last_restart_epoch;
}

# -----------------------------------------------------------------------------
#
# This subroutine finds the last time the pod was started or restarted and it
# returns back an epoch time value for the start or restart.
#
sub get_last_pod_start_restart_time {
    my $namespace = shift;
    my $pod_name = shift;

    my $last_restart_epoch = 0;
    my $rc;
    my $restart_epoch = 0;
    my @result;
    my $timestamp;

    print_verbose("  Fetching Start and restart information for pod $pod_name\n");
    $rc = General::OS_Operations::send_command(
        {
            "command"       => "kubectl describe pod $pod_name -n $namespace" . $kubeconfig,
            "hide-output"   => 1,
            "return-output" => \@result,
        }
    );
    if ( $rc == 0 && @result != 0 ) {
        for ( @result ) {
            if ( /^(Start Time:|\s+Started:|\s+Finished:)\s+(.+)/ ) {
                $timestamp = $2;
                if (exists $timestamp_epoch_lookup{$timestamp}) {
                    $restart_epoch = $timestamp_epoch_lookup{$timestamp};
                } else {
                    $restart_epoch = sprintf "%d", `date -d "$timestamp" +\%s`;
                    $timestamp_epoch_lookup{$timestamp} = $restart_epoch;
                }
                $last_restart_epoch = $restart_epoch if ($restart_epoch > $last_restart_epoch);
            }
        }
        if ( $last_restart_epoch == 0 ) {
            report_failed("Did not find the last restart time for the pod '$pod_name'.\n" . join "\n", @result);
        }
    } else {
        report_failed("Failed to get the last restart time for the pod '$pod_name'.\n" . join "\n", @result);
    }

    return $last_restart_epoch;
}

# -----------------------------------------------------------------------------
#
# Checks if the SC namespace has been given and if it exists and in such case
# returns the namespace.
#
# It returns back:
#   An empty string if namespace does not exist or if it was not specified.
#   Returns the namespace if it exists.0: Namespace does not exist or was not specified.
#
sub get_namespace {
    if ( $namespace eq "" ) {
        # Also considered OK but we report it under details
        report_skipped("Check skipped because no namespace provided.\n");
        return "";
    } elsif ( $namespace_error == 1 ) {
        # Treat this check as an error
        report_failed("Check failed because namespace $namespace does not exist.\n");
        return "";
    } else {
        # Namespace was specified and it exist
        return $namespace;
    }
}

# -----------------------------------------------------------------------------
#
# Initialize the help description for the different check.
#
# Special hash or array variable that contains a short description what
# the check is doing.
# The key for the hash is the check name and the data is an array of lines
# containing the description.
#
# NOTE:
# This variable must be manually updated when a new check is added, if this
# is not done then no description is provided for this check.
# The updating should be done inside of the check sub routine.
sub initialize_check_description {
    %check_description = (
        #---------------------------------------------------------------------------
        'alarm_history' => qq{
        This check the alarm history by connecting to CMYP CLI and issues the
        'show alarm-history' command and then will check if any alarms has been
        repeated more than 2 times in the last hour (3600 seconds) or whatever the
        'alarm_history_flapping_threshold' variable is set to.
        },
        #---------------------------------------------------------------------------
        'alarms' => qq{
        This check the alarm list to see if any are present in the system and can
        ignore certain alarms specified by an 'allow-list'. It uses an external
        script called 'alarm_check.pl' shich should be delivered in the same
        directory as the '$0' script.

        If all alarms are found in the 'allow-list' then it reports a warning,
        otherwise it will report an error.
        },
        #---------------------------------------------------------------------------
        'cassandra_cluster_status' => qq{
        This check the cassandra database cluster status by connecting to the
        eric-data-wide-column-database-cd- or eric-bsf-wcdb-cd- pods and issuing
        the 'nodetool status' command where it checks that the status is up (U)
        and the state is normal (N).
        },
        #---------------------------------------------------------------------------
        'certificate_validity' => qq{
        This check the validity of the CCD certificates on all nodes and checks that
        the days left are > the value of variable ''certificate_validity_days_left'.
        On the master nodes if checks with command 'kubeadm certs check-expiration',
        and on all the nodes it checks certificates in the '/var/lib/kubelet/pki/'
        path.
        },
        #---------------------------------------------------------------------------
        'component_info' => qq{
        This check the daemonset, deployment, replicaset, statefulset and job data
        from the command 'kubectl get all -o wide' that all the numbers are the same.

        For daemonset it checks the DESIRED, CURRENT, READY, UP-TO-DATE and AVAILABLE
        columns that they have the same value.

        For deployment it checks the READY, UP-TO-DATE and AVAILABLE columns that
        they have the same value.

        For replicaset it checks the DESIRED, CURRENT and READY columns that they
        have the same values.

        For statefulset it checks the READY colums that they have the same value.

        For job it check the COMPLETIONS column have the same values, except for the
        eric-data-search-engine-curator- job which is ignored since it's contantly
        in the process of starting new jobs.
        },
        #---------------------------------------------------------------------------
        'config_dump' => qq{
        This prints the running configuration by connecting to CMYP CLI and issues the
        'show running-config | display xml | nomore' command.
        No check of the printed data is done.
        },
        #---------------------------------------------------------------------------
        'config_validity_bsf' => qq{
        This prints the running configuration by connecting to CMYP CLI and issues the
        'show running-config bsf-function | nomore' command.
        A check is done that some configuration data is printed, if the printout
        shows '% No entries found.' then the check will fail.
        },
        #---------------------------------------------------------------------------
        'config_validity_scp' => qq{
        This prints the running configuration by connecting to CMYP CLI and issues the
        'show running-config scp-function | nomore' command.
        A check is done that some configuration data is printed, if the printout
        shows '% No entries found.' then the check will fail.
        },
        #---------------------------------------------------------------------------
        'config_validity_sepp' => qq{
        This prints the running configuration by connecting to CMYP CLI and issues the
        'show running-config sepp-function | nomore' command.
        A check is done that some configuration data is printed, if the printout
        shows '% No entries found.' then the check will fail.
        },
        #---------------------------------------------------------------------------
        'core_dumps' => qq{
        This checks that no coredumps has been created on any master or worker nodes
        within the time frame specified by the 'core_dump_time_threshold' variable
        value.
        },
        #---------------------------------------------------------------------------
        'dced_data_sync' => qq{
        This check the 'distributed coordinator ed' endpoint status by connecting to
        the eric-data-distributed-coordinator-ed- pods and executing the following
        command:
        etcdctl endpoint status --insecure-skip-tls-verify=true -w json
        Where is checks the 'revision' value to make sure that all within the
        deviation specified with the ''dced_data_sync_max_deviation' variable value.
        },
        #---------------------------------------------------------------------------
        'docker_version' => qq{
        This prints the docker client version and checks that it is the same or
        higher version than specified with the 'docker_version_limit' variable value.
        },
        #---------------------------------------------------------------------------
        'ecfe_network_status' => qq{
        This check connects to the 'eric-tm-external-connectivity-frontend-speaker-'
        pods and executes the 'birdcl show protocols' command and checks that the BFD
        protocols shows State as 'up' and that the BGP protocol shows State as 'up'
        and the Info shows 'Established'.
        },
        #---------------------------------------------------------------------------
        'endpoints' => qq{
        This check endpoints data if any shows 'notReadyAddresses:' which is an error.
        The following are not checked:
            - eric-bsf-cert-notifier
            - eric-bsf-diameter
            - eric-bsf-worker
            - eric-scp-cert-notifier
            - eric-scp-worker
            - eric-sepp-cert-notifier
            - eric-sepp-worker
        },
        #---------------------------------------------------------------------------
        'events' => qq{
        This check the events from command 'kubectl get events' and check if any are
        of type 'Warning' or of any type other than 'Normal' then it reports then
        as a warning or error.
        },
        #---------------------------------------------------------------------------
        'external_interfaces' => qq{
        This check that the external interfaces are working, for example that it is
        possible to login to the CMYP CLI.
        If no namespace is provided then this check is skipped.
        If the check on the interface fails then it is reported as a warning or error.
        Currently supported interfaces are:
            - CMYP CLI interface
        },
        #---------------------------------------------------------------------------
        'helm_list_status' => qq{
        This check in the specified namespace if any of the releases returns non-zero
        return code or returns no data for the 'helm status ...' command.
        No other check is done on the data.
        },
        #---------------------------------------------------------------------------
        'helm_version' => qq{
        This prints the helm client or server version and checks that it is the same
        or higher version than specified with the 'helm_version_helm2_client_limit',
        'helm_version_helm2_server_limit' or 'helm_version_helm3_limit' variable
        values.
        },
        #---------------------------------------------------------------------------
        'kpi' => qq{
        This check the KPI (Key Performance Index) by connecting to CMYP CLI and
        issues the 'metrics-query <query string>' command and then will check if any
        KPI's are outside of expected values which is specified by the
        'kpi_success_rate_threshold' and 'kpi_load_threshold' variable values.
        },
        #---------------------------------------------------------------------------
        'kpi_success_rate' => qq{
        This check the KPI (Key Performance Index) by connecting to CMYP CLI and
        issues the 'metrics-query <query string>' command and then will check if any
        KPI's for success rate are outside of expected values which is specified
        with the 'kpi_success_rate_threshold' and 'kpi_success_rate_description'
        variable values.
        },
        #---------------------------------------------------------------------------
        'kubectl_version' => qq{
        This prints the kubectl client or server version and checks that it is the
        same or higher version than specified with the 'kubectl_version_client_limit'
        or 'kubectl_server_version' variable values.
        },
        #---------------------------------------------------------------------------
        'log_statistics' => qq{
        This checks that the number of log entries being generated per second
        averaged over a certain time interval does not exceed a specific limit.
        The values checked can be changed by variables 'logs_per_second_average_limit'
        'logs_severity', 'logs_severity_critical_limit', 'logs_severity_error_limit',
        'logs_severity_warning_limit', 'logs_time_interval' and 'logs_to_check'.

        NOTE: This check is only accurate when executed on the same cluster where the
        software is running or if the times on the two systems are the same.
        },
        #---------------------------------------------------------------------------
        'master_core_dumps' => qq{
        This checks that no coredumps has been created on any master nodes within the
        time frame specified by the 'core_dump_time_threshold' variable value.
        },
        #---------------------------------------------------------------------------
        'max_connections' => qq{
        This checks that the max_connections values is the same as the value given
        by the 'max_connections_value' variable (default 350) for the following
        files and commands in the eric-data-document-database-pg-0 pod:
            Command: patronictl show-config
            File:    /var/lib/postgresql/config/patroni.yml
            File:    /var/lib/postgresql/data/pgdata/postgresql.conf
        },
        #---------------------------------------------------------------------------
        'max_map_count' => qq{
        This checks on all worker nodes that the 'sudo -n sysctl vm.max_map_count' value
        is less than thge ''max_map_count_limit' variable value.
        },
        #---------------------------------------------------------------------------
        'nerdctl_version' => qq{
        This prints the nerdctl client version and checks that it is the same or
        higher version than specified with the 'nerdctl_version_limit' variable value.
        },
        #---------------------------------------------------------------------------
        'node_status' => qq{
        This checks on all nodes that none have status 'Ready=False'.
        },
        #---------------------------------------------------------------------------
        'node_utilization' => qq{
        This checks on all nodes that none are using more 'cpu', ephemeral-storage',
        'memory' or deployed 'pods' than the values of the 'node_utilization_cpu_threshold',
        ''node_utilization_disk_threshold', 'node_utilization_memory_threshold' ro
        'node_utilization_pods_threshold' variable values.
        },
        #---------------------------------------------------------------------------
        'pm_files' => qq{
        This checks on either the 'eric-pm-bulk-reporter-' or 'eric-data-object-storage-mn-0'
        pods that there are PM files created within the latest number of seconds
        specified by the 'pm_files_max_time_deviation' variable value and the files
        must have a size of at least 'pm_files_size_limit' bytes.
        },
        #---------------------------------------------------------------------------
        'pm_system_jobs' => qq{
        This checks in CMYP if certain PM system jobs are defined and active.
        It will check for the following jobs using commands:

        BSF:
            show pm job bsf_job current-job-state

        RLF:
            show pm job rlf_service_job current-job-state
            show pm job rlf_system_job current-job-state

        SCP:
            show pm job scp_ingress_rate_limit_job current-job-state
            show pm job scp_system_job current-job-state

        SEPP:
            show pm job sepp_ingress_rate_limit_job current-job-state
            show pm job sepp_system_job current-job-state
            show pm job sepp_topology_hiding_job current-job-state

        SLF:
            show pm job slf_system_job current-job-state
        },
        #---------------------------------------------------------------------------
        'pod_logs' => qq{
        This prints the logs from the following pod names:
            - eric-*-manager-
            - eric-*-worker-
            - eric-dsc-fdr-
            - eric-dsc-agent-
        It can limit the amount of logs to fetch by the 'pod_logs_since' variable
        value.
        Currently, no check is done on the collected data.
        },
        #---------------------------------------------------------------------------
        'pod_ready_status' => qq{
        This checks the READY status of all pods that the values are the same and
        that the status is 'Running or Completed.
        Pods 'eric-data-search-engine-curator-' are ignored since they are started
        and stopped the whole time.
        },
        #---------------------------------------------------------------------------
        'pod_restarts' => qq{
        This checks if any of the pods has been restarted within the time limit
        specified by the 'pod_restarts_time_threshold' variable value.
        Pods 'eric-data-search-engine-curator-' are ignored since they are started
        and stopped the whole time.
        },
        #---------------------------------------------------------------------------
        'pod_start_restarts' => qq{
        This checks if any of the pods has been started or restarted within the time
        limit specified by the 'pod_restarts_time_threshold' variable value.
        Pods 'eric-data-search-engine-curator-' are ignored since they are started
        and stopped the whole time.
        },
        #---------------------------------------------------------------------------
        'pvc' => qq{
        This checks that the PVCs are 'Bound'.
        },
        #---------------------------------------------------------------------------
        'registry_pod_images' => qq{
        This checks that all pods has loaded the image from a specific registry that
        is specified with the command parameter --own_registry_url.
        Excluded from the check are the following pod names:
            - eric-aat
            - eric-sc-aat-ts
            - eric-chfsim
            - eric-chfsim-redis
            - eric-influxdb
            - eric-k6
            - eric-nrfsim
            - eric-seppsim
            - eric-dscload
            - eric-dscload-deployment
            - jcatserver
            - ubuntu
            - eric-vtaprecorder
        },
        #---------------------------------------------------------------------------
        'replicasets' => qq{
        This checks that the availableReplicas match the replicas values and the
        replicas value is not 0.
        },
        #---------------------------------------------------------------------------
        'storageclass_backend' => qq{
        This checks that storageclass does not have any events, it expects <none>.
        },
        #---------------------------------------------------------------------------
        'svc' => qq{
        This checks that none of the services (svc) are 'pending'.
        },
        #---------------------------------------------------------------------------
        'time_synchronization' => qq{
        This checks connects to all nodes and checks that the time difference is not
        larger than the 'time_synchronization_max_deviation' variable value and that
        they are all synchronized by NTP.
        },
        #---------------------------------------------------------------------------
        'top_nodes' => qq{
        This checks that none of the node utilization for 'cpu' and 'memory' are
        below the 'top_nodes_cpu_limit' and 'top_nodes_memory_limit' variable values.
        },
        #---------------------------------------------------------------------------
        'top_pods' => qq{
        This checks that none of the pod utilization for 'cpu' and 'memory' are
        below the 'pod_utilization_cpu_threshold' and 'pod_utilization_memory_threshold'
        variable values.
        The following pods are not checked:
            - eric-aat
            - eric-sc-aat-ts
            - eric-chfsim
            - eric-chfsim-redis
            - eric-influxdb
            - eric-k6
            - eric-nrfsim
            - eric-seppsim
            - eric-dscload-deployment
            - jcatserver
        },
        #---------------------------------------------------------------------------
        'worker_core_dumps' => qq{
        This checks that no coredumps has been created on any worker nodes within the
        time frame specified by the 'core_dump_time_threshold' variable value.
        },
        #---------------------------------------------------------------------------
        'worker_disk_usage' => qq{
        This checks that disk utilization on any worker nodes are below the limit
        specified by the 'worker_disk_usage_threshold' variable value and that the
        available space is at least 'worker_disk_usage_available_size_minimum'
        variable value.
        },
    );
}

# -----------------------------------------------------------------------------
#
# Initialize the check vs. group matrix.
#
# Special variable that contains a matrix of different check groups that
# can be executed and what the final vedict will be if the check is
# successful or failed.
# The verdict can be one of the following:
#   - "fail"
#
#     If the result of the check is failed then it will also be marked as
#     failed in the over all result.
#     If the result of the check is skipped then it will also be marked as
#     skipped and have no impacy on the over all result.
#     If the result of the check is successful then it will also be marked
#     as successful and not cause a failure of the over all result (unless
#     there are other checks that fail).
#
#   - "skip"
#
#     This check will not be executed at all and the check will be marked
#     as skipped and have no impact on the over all result.
#
#   - "warning"
#
#     If the result of the check is failed then it's changed to a warning
#     which will not cause a failure of the over all result (unless there
#     are other checks that fail).
#
# NOTE:
# This matrix must be manually updated when a new check is added, if this
# is not done then the verdict will be set to "skip" for all groups
# except for the "full" group which still be set to "fail".
sub initialize_check_group_matrix {
    %check_group_matrix = (
        "alarm_history" => {
            "full" =>  "fail",
            "post-deployment" =>  "skip",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
        "alarms" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "cassandra_cluster_status" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "certificate_validity" => {
            "full" =>  "fail",
            "post-deployment" =>  "warning",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "warning",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
        "component_info" => {
            "full" =>  "fail",
            "post-deployment" =>  "warning",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "warning",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
        "config_dump" => {
            "full" =>  "fail",
            "post-deployment" =>  "skip",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
        "config_validity_bsf" => {
            "full" =>  "fail",
            "post-deployment" =>  "skip",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "config_validity_scp" => {
            "full" =>  "fail",
            "post-deployment" =>  "skip",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "config_validity_sepp" => {
            "full" =>  "fail",
            "post-deployment" =>  "skip",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "core_dumps" => {
            "full" =>  "skip",
            "post-deployment" =>  "skip",
            "post-upgrade" =>  "skip",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "skip",
            "verification" =>  "skip",
        },
        "dced_data_sync" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "docker_version" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "fail",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "ecfe_network_status" => {
            "full" =>  "fail",
            "post-deployment" =>  "warning",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "warning",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
        "endpoints" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "events" => {
            "full" =>  "fail",
            "post-deployment" =>  "warning",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
        "external_interfaces" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "helm_list_status" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "helm_version" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "fail",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "kpi" => {
            "full" =>  "fail",
            "post-deployment" =>  "skip",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
        "kpi_success_rate" => {
            "full" =>  "warning",
            "post-deployment" =>  "skip",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "warning",
            "verification" =>  "skip",
        },
        "kubectl_version" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "fail",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "log_statistics" => {
            "full" =>  "warning",
            "post-deployment" =>  "warning",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
        "master_core_dumps" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "max_connections" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "skip",
        },
        "max_map_count" => {
            "full" =>  "fail",
            "post-deployment" =>  "skip",
            "post-upgrade" =>  "skip",
            "pre-deployment" =>  "fail",
            "pre-upgrade" =>  "fail",
            "verification" =>  "skip",
        },
        "nerdctl_version" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "fail",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "node_status" => {
            "full" =>  "warning",
            "post-deployment" =>  "warning",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "warning",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
        "node_utilization" => {
            "full" =>  "fail",
            "post-deployment" =>  "warning",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "warning",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
        "pm_files" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "pm_system_jobs" => {
            "full" =>  "fail",
            "post-deployment" =>  "warning",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "pod_logs" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "pod_ready_status" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "pod_restarts" => {
            "full" =>  "fail",
            "post-deployment" =>  "warning",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
        "pod_start_restarts" => {
            "full" =>  "fail",
            "post-deployment" =>  "warning",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
        "pvc" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "registry_pod_images" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "skip",
        },
        "replicasets" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "storageclass_backend" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "fail",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "svc" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "time_synchronization" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "fail",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "top_nodes" => {
            "full" =>  "fail",
            "post-deployment" =>  "warning",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "warning",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
        "top_pods" => {
            "full" =>  "fail",
            "post-deployment" =>  "warning",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
        "worker_core_dumps" => {
            "full" =>  "fail",
            "post-deployment" =>  "fail",
            "post-upgrade" =>  "fail",
            "pre-deployment" =>  "skip",
            "pre-upgrade" =>  "fail",
            "verification" =>  "fail",
        },
        "worker_disk_usage" => {
            "full" =>  "warning",
            "post-deployment" =>  "warning",
            "post-upgrade" =>  "warning",
            "pre-deployment" =>  "warning",
            "pre-upgrade" =>  "warning",
            "verification" =>  "warning",
        },
    );
}

# -----------------------------------------------------------------------------
#
# set special variables used inside the check_xxxxx subroutines to alter the
# validity checks being done.
# these variables can be passed into the script on the command line using
# the parameter "--variable <variable name>=<value>" and the parameter
# can be specified multiple times to set more than one variable.
# what is listed below are default values that will be used if the user
# does not specify a new value on the comand line.
#
sub initialize_variables {
    # Set default values:
    #           <variable name>                               <value>       Check that is using the variable
    $variables{'alarm_history_details_at_success'}          = "no";         # alarm_history
    $variables{'alarm_history_flapping_threshold'}          = 3600;         # alarm_history
    $variables{'alarms_allow_file'}                         = "";           # alarms
    $variables{'alarms_full_alarm_list'}                    = "no";         # alarms
    $variables{'alarms_use_tls'}                            = "no";         # alarms
    $variables{'certificate_validity_days_left'}            = 30;           # certificate_validity
    $variables{'cmyp_password'}                             = "";           # alarm_history,config_dump,external_interfaces,kpi,kpi_success_rate,pm_system_jobs
    $variables{'cmyp_user'}                                 = "";           # alarm_history,config_dump,external_interfaces,kpi,kpi_success_rate,pm_system_jobs
    $variables{'component_info_other_namespaces'}           = "no";         # component_info
    $variables{'component_info_system_namespaces'}          = "yes";        # component_info
    $variables{'config_dump_type'}                          = "";           # config_dump
    $variables{'config_validity_bsf_password'}              = "bsfbsf";     # config_validity_bsf
    $variables{'config_validity_bsf_user'}                  = "bsf-admin";  # config_validity_bsf
    $variables{'config_validity_scp_password'}              = "scpscp";     # config_validity_scp
    $variables{'config_validity_scp_user'}                  = "scp-admin";  # config_validity_scp
    $variables{'config_validity_sepp_password'}             = "seppsepp";   # config_validity_sepp
    $variables{'config_validity_sepp_user'}                 = "sepp-admin"; # config_validity_sepp
    $variables{'core_dump_allow_file'}                      = "";           # core_dumps,master_core_dumps,worker_core_dumps
    $variables{'core_dump_directory'}                       = "";           # core_dumps,master_core_dumps,worker_core_dumps
    $variables{'core_dump_delete'}                          = "no";         # core_dumps,master_core_dumps,worker_core_dumps
    $variables{'core_dump_fetch'}                           = "no";         # core_dumps,master_core_dumps,worker_core_dumps
    $variables{'core_dump_no_allow_file'}                   = "no";         # core_dumps,master_core_dumps,worker_core_dumps
    $variables{'core_dump_time_threshold'}                  = 604800;       # core_dumps,master_core_dumps,worker_core_dumps
    $variables{'dced_data_sync_max_deviation'}              = 10;           # dced_data_sync
    $variables{'docker_version_limit'}                      = "19.03.1";    # docker_version
    $variables{'events_hide_warnings'}                      = "yes";        # events
    $variables{'helm_version_helm2_client_limit'}           = "2.0.0";      # helm_version
    $variables{'helm_version_helm2_server_limit'}           = "2.0.0";      # helm_version
    $variables{'helm_version_helm3_limit'}                  = "3.0.0";      # helm_version
    $variables{'kpi_data_directory'}                        = "";           # kpi,kpi_success_rate
    $variables{'kpi_definitions_file'}                      = "";           # kpi,kpi_success_rate
    $variables{'kpi_details_at_success'}                    = "no";         # kpi,kpi_success_rate
    $variables{'kpi_downtime_time_limit'}                   = 120;          # kpi_success_rate
    $variables{'kpi_downtime_success_rate_threshold'}       = 0;            # kpi_success_rate
    $variables{'kpi_future'}                                = "";           # kpi,kpi_success_rate
    $variables{'kpi_load_threshold'}                        = 80;           # kpi
    $variables{'kpi_mps_tps_downtime_time_limit'}           = 15;           # kpi_success_rate
    $variables{'kpi_mps_tps_rate_description'}              = ".+";         # kpi_success_rate
    $variables{'kpi_mps_tps_rate_threshold'}                = 30;           # kpi_success_rate
    $variables{'kpi_past'}                                  = "";           # kpi,kpi_success_rate
    $variables{'kpi_prefer_recording_rule'}                 = "no";         # kpi,kpi_success_rate
    $variables{'kpi_resolution'}                            = "15";         # kpi,kpi_success_rate
    $variables{'kpi_success_rate_description'}              = ".+";         # kpi_success_rate
    $variables{'kpi_success_rate_threshold'}                = 95;           # kpi,kpi_success_rate
    $variables{'kpi_time_interval'}                         = "";           # kpi,kpi_success_rate
    $variables{'kpi_use_cached_values'}                     = "yes";        # kpi,kpi_success_rate
    $variables{'kubectl_version_client_limit'}              = "1.15.3";     # kubectl_version
    $variables{'kubectl_version_server_limit'}              = "1.15.3";     # kubectl_version
    $variables{'logs_details_directory'}                    = "";           # log_statistics
    $variables{'logs_per_second_average_limit'}             = 20;           # log_statistics
    $variables{'logs_time_interval'}                        = "10m";        # log_statistics
    $variables{'logs_severity'}                             = "";           # log_statistics
    $variables{'logs_severity_critical_limit'}              = 0;            # log_statistics
    $variables{'logs_severity_error_limit'}                 = 0;            # log_statistics
    $variables{'logs_severity_warning_limit'}               = 0;            # log_statistics
    $variables{'log_statistics_at_success'}                 = "no";         # log_statistics
    $variables{'logs_to_check'}                             = "all";        # log_statistics
    $variables{'max_map_count_limit'}                       = 262144;       # max_map_count
    $variables{'max_connections_value'}                     = ADP::Kubernetes_Operations::get_max_connections_default_value('default','default');  # max_connections
    $variables{'nerdctl_version_limit'}                     = "1.1.0";      # nerdctl_version
    $variables{'node_utilization_cpu_threshold'}            = 80;           # node_utilization
    $variables{'node_utilization_disk_threshold'}           = 80;           # node_utilization
    $variables{'node_utilization_memory_threshold'}         = 80;           # node_utilization
    $variables{'node_utilization_pods_threshold'}           = 80;           # node_utilization
    $variables{'pm_files_max_time_deviation'}               = 1800;         # pm_files
    $variables{'pm_files_size_limit'}                       = 500;          # pm_files
    $variables{'pod_logs_since'}                            = "";           # pod_logs
    $variables{'pod_restarts_allow_file'}                   = "";           # pod_restarts,pod_start_restarts
    $variables{'pod_restarts_time_threshold'}               = 604800;       # pod_restarts,pod_start_restarts
    $variables{'pod_utilization_cpu_threshold'}             = 80;           # top_pods
    $variables{'pod_utilization_disk_threshold'}            = 80;           # top_pods
    $variables{'pod_utilization_memory_threshold'}          = 80;           # top_pods
    $variables{'pod_utilization_pods_threshold'}            = 80;           # top_pods
    $variables{'time_synchronization_max_deviation'}        = 10;           # time_synchronization
    $variables{'top_nodes_cpu_limit'}                       = 90;           # top_nodes
    $variables{'top_nodes_memory_limit'}                    = 90;           # top_nodes
    $variables{'top_pods_whitelist_tools'}                  = "yes";        # top_pods
    $variables{'worker_disk_usage_available_size_minimum'}  = "10G";        # worker_disk_usage
    $variables{'worker_disk_usage_threshold'}               = 80;           # worker_disk_usage

    # Mark that these values are default values
    for my $key (keys %variables) {
        $variables_user_set{$key} = 0;
    }
}

# -----------------------------------------------------------------------------
#
sub initialize_variables_description {
    %variables_description = (
        #---------------------------------------------------------------------------
        'alarm_history_details_at_success' => qq{
        If specified and set to value 'yes' or '1' then alarm history details will be
        printed also at successful execution of the check. By default the details are
        only printed when the check fails.
        },

        #---------------------------------------------------------------------------
        'alarm_history_flapping_threshold' => qq{
        If specified and set to an integer value then this is the threshold value that will
        be used to determine if an alarm is flapping i.e. raised and ceased more than three
        times within the threshold value.
        },

        #---------------------------------------------------------------------------
        'alarms_allow_file' => qq{
        If specified and set to a file path then this file will be used instead of the
        default file when checking if an alarm should be ignored or not.
        },

        #---------------------------------------------------------------------------
        'alarms_full_alarm_list' => qq{
        If specified and set to "yes" then extra parameters are printed with the alarms.
        This might only work on SC release 1.10 and later.
        },

        #---------------------------------------------------------------------------
        'alarms_use_tls' => qq{
        If specified and set to "yes" then a secure connection will be used for fetching the alarms.
        This might only work on SC release 1.10 and later.
        },

        #---------------------------------------------------------------------------
        'certificate_validity_days_left' => qq{
        If specified and set to an integer value then this is how many days must be left
        of the certificate validity date before it's reported as an error.
        },

        #---------------------------------------------------------------------------
        'cmyp_password' => qq{
        If specified and set to a string then this is the password value to use to login to
        the CM Yang Provider CLI to fetch data.
        If not specified then the default value of 'rootroot' will be used.
        },

        #---------------------------------------------------------------------------
        'cmyp_user' => qq{
        If specified and set to a string then this is the user value to use to login to
        the CM Yang Provider CLI to fetch data.
        If not specified then the default value of 'admin' or 'expert' will be used,
        depending on for which check it is being used.
        },

        #---------------------------------------------------------------------------
        'component_info_other_namespaces' => qq{
        If specified and set to 'yes' then when checking component info all other non system
        defined namespaces will also be included in the check.
        By default only the specified namespace will be checked.
        },

        #---------------------------------------------------------------------------
        'component_info_system_namespaces' => qq{
        If specified and set to 'no' then no default system namespaces will be included in
        the check.
        By default the following default namespaces will be included in the check:
            - default
            - etcd
            - ingress-nginx
            - kube-node-lease
            - kube-public
            - kube-system
            - monitoring
        },

        #---------------------------------------------------------------------------
        'config_dump_type' => qq{
        If specified and set to a string then only configuration data for this type will
        be fetched from the node.
        By default all configuration data will be fetched.
        },

        #---------------------------------------------------------------------------
        'core_dump_allow_file' => qq{
        If specified and pointing to a valid file that contain a list of Perl regular
        expressions (one per line) for core dumps to ignore and not report as a fault.
        },

        #---------------------------------------------------------------------------
        'core_dump_delete' => qq{
        If specified and set to "yes" then any found core dumps that match the condition
        set with 'core_dump_time_threshold' will be deleted on the node.
        },

        #---------------------------------------------------------------------------
        'core_dump_directory' => qq{
        If specified and set to a string pointing to a directory where found core dumps that
        match the condition set with 'core_dump_time_threshold' will be downloaded to.
        If the directory does not exist then it will be created.
        If creation of the directory fails for whatever reason then no core dumps will be
        fetched and they will also not be deleted is specified with 'core_dump_delete=yes'.
        This parameter is only used if also 'core_dump_fetch=yes' is set.
        },

        #---------------------------------------------------------------------------
        'core_dump_fetch' => qq{
        If specified and set to "yes" then any found core dumps that match the condition
        set with 'core_dump_time_threshold' will be fetched from the node and downloaded to
        the directory specified with 'core_dump_directory' which must be pointing to a
        directory.
        If no directory was specified or creation of the directory fails then no core dumps
        will be fetched.
        },

        #---------------------------------------------------------------------------
        'core_dump_no_allow_file' => qq{
        If specified and set to "yes" then all found core dumps will be reported i.e.
        no check if the core dump is in the allow list will be done.
        By default or if "no" then core dumps will be checked against the specified
        allow file given by "core_dump_allow_file" or the default allow file.
        },

        #---------------------------------------------------------------------------
        'core_dump_time_threshold' => qq{
        If specified and set to an integer value then this is limit to use to check if the
        core dump timestamp occurred at or below this limit which will cause the check to fail.
        If the value is set to zero then any found core dump will cause the check to fail.
        },

        'dced_data_sync_max_deviation' => qq{
        If specified and set to an integer value then this is the maximum deviation between
        the different values that is allowed.
        },
        #---------------------------------------------------------------------------
        'docker_version_limit' => qq{
        If specified and set to a version string then the check will use this version
        as the minimum value to compare against. If the version is lower than this version
        then the check will fail.
        },

        #---------------------------------------------------------------------------
        'events_hide_warnings' => qq{
        If specified and set to 'no' then when checking events, all events (warning and
        errors) will be reported.
        By default only the error events will be shown, warning events will be hidden.
        },

        #---------------------------------------------------------------------------
        'helm_version_helm2_client_limit' => qq{
        If specified and set to a version string then the check will use this version
        as the minimum value to compare against. If the version is lower than this version
        then the check will fail.
        },

        #---------------------------------------------------------------------------
        'helm_version_helm2_server_limit' => qq{
        If specified and set to a version string then the check will use this version
        as the minimum value to compare against. If the version is lower than this version
        then the check will fail.
        },

        #---------------------------------------------------------------------------
        'helm_version_helm3_limit' => qq{
        If specified and set to a version string then the check will use this version
        as the minimum value to compare against. If the version is lower than this version
        then the check will fail.
        },

        #---------------------------------------------------------------------------
        'kpi_data_directory' => qq{
        If specified then this is the directory where collected KPI files and graphs are
        stored.

        If not specified which is also the default value then no KPI files are created.
        Instead just an analysis of the printed data is done.
        },

        #---------------------------------------------------------------------------
        'kpi_definitions_file' => qq{
        If specified then this file should contain KPI definitions which will be used
        when parsing KPI data to translate the query strings into an easier to understand
        KPI string which will also be used for the created output files.

        If not specified then the template file containing all defined KPI's (which are
        known at the time of last update of that file) which is stored in the following
        relative directory from this script will be used:

            ../../templates/kpi/all_sc_kpi.json
        },

        #---------------------------------------------------------------------------
        'kpi_details_at_success' => qq{
        If specified and set to value 'yes' or '1' then KPI details will be printed also at
        successful execution of the check. By default the details are only printed when
        the check fails.
        },

        #---------------------------------------------------------------------------
        'kpi_downtime_time_limit' => qq{
        If specified and set to an integer value then this is the number of seconds
        that the total amount of downtime must reach for a check to be set to failed.
        See also 'kpi_downtime_success_rate_threshold'.
        NOTE: Downtime calculations can only be performed if detailed values are
        collected by specifying the parameter 'kpi_data_directory'.
        },

        #---------------------------------------------------------------------------
        'kpi_downtime_success_rate_threshold' => qq{
        If specified and set to an integer value then this is the limit to use to check
        if the success rate for a specific time interval as defined by 'kpi_resolution'
        falls below this limit then the interval is considered as downtime and if the
        total downtime for all intervals falls below the value specified by the
        'kpi_downtime_time_limit' then it would cause the check to fail.
        If not set by the user then the value specified by 'kpi_success_rate_threshold'
        will be used.
        NOTE: Downtime calculations can only be performed if detailed values are
        collected by specifying the parameter 'kpi_data_directory'.
        },

        #---------------------------------------------------------------------------
        'kpi_future' => qq{
        If specified then KPI values are collected into the future with an interval
        specified by the 'kpi_resolution' parameter.
        If the <value> is an integer value then this is how many sets of KPI values are
        fetched from the future with an interval of 'kpi_resolution' seconds.
        If the <value> is an integer value followed by the character 's', 'm' or 'h'
        then this is how far into the future the collection is done specified in seconds,
        minutes or hours, between each collection there is a delay of 'kpi_resolution'
        seconds.
        Note: Only one of kpi_future or kpi_past should be specified, if both are specified
        then kpi_past will be used. If none are specified then kpi_past=1m will be used.
        },

        #---------------------------------------------------------------------------
        'kpi_load_threshold' => qq{
        If specified and set to an integer value then this is limit to use to check if the
        load is at or above this limit which would cause the check to fail.
        },

        #---------------------------------------------------------------------------
        'kpi_mps_tps_downtime_time_limit' => qq{
        If specified and set to an integer value then this is the number of seconds
        that the total amount of downtime for MPS or TPS rates must reach for a check
        to be set to failed.
        See also 'kpi_mps_tps_rate_threshold'.
        NOTE: Downtime calculations can only be performed if detailed values are
        collected by specifying the parameter 'kpi_data_directory'.
        },

        #---------------------------------------------------------------------------
        'kpi_mps_tps_rate_description' => qq{
        If specified it should contain the KPI description string excluding the string
        "[MPS]" or "[TPS]" which is automatically appended to the specified value.
        This value will be used to check if the MPS or TPS rate is not zero in which
        case the check is successful, otherwise the check will fail.
        It is also used to check if for any time interval the value falls down to zero;
        or falls below 'kpi_mps_tps_rate_threshold' % of the average value for all
        time intervals for longer than 'kpi_mps_tps_downtime_time_limit' seconds in
        which case the check also fails.
        If not specified then the value to use will be ".+" i.e. any string that ends with
        "[MPS]" or "[TPS]" will be checked.
        },

        #---------------------------------------------------------------------------
        'kpi_mps_tps_rate_threshold' => qq{
        If specified and set to an integer value then this is the limit in percent of
        the average MPS/TPS value that traffic level is allowed to fall for the number
        of seconds given by 'kpi_mps_tps_downtime_time_limit' before the check is
        marked as failed.
        If the traffic rate in MPS or TPS rate for a specific time interval as
        defined by 'kpi_resolution' falls below this limit then the interval is
        considered as downtime and if the total downtime for all intervals falls
        below the value specified by the 'kpi_mps_tps_downtime_time_limit' then it
        would cause the check to fail.
        NOTE: Downtime calculations can only be performed if detailed values are
        collected by specifying the parameter 'kpi_data_directory'.
        },

        #---------------------------------------------------------------------------
        'kpi_past' => qq{
        If specified then KPI values are collected from the past with an interval
        specified by the 'kpi_resolution' parameter.
        If the <value> is an integer value then this is how many sets of KPI values are
        fetched from the past history with an interval of 'kpi_resolution' seconds.
        If the <value> is an integer value followed by the character 's', 'm' or 'h'
        then this is how far back in time the collection is done specified in seconds,
        minutes or hours, between each collection there is a delay of 'kpi_resolution'
        seconds.
        Note: Only one of kpi_future or kpi_past should be specified, if both are specified
        then kpi_past will be used. If none are specified then kpi_past=1m will be used.
        },

        #---------------------------------------------------------------------------
        'kpi_prefer_recording_rule' => qq{
        If specified and set to a value other than 'no' or '0' then the recording rules
        will be used instead of the metrics query string because it is more efficient.
        But recording rules are only available starting with SC 1.8 release.
        },

        #---------------------------------------------------------------------------
        'kpi_resolution' => qq{
        If specified and set to an integer value then this is the resolution i.e. how
        often to fetch the KPI values. If not specified then the default value will be used.
        },

        #---------------------------------------------------------------------------
        'kpi_success_rate_description' => qq{
        If specified it should contain the KPI description string excluding the string
        "Success Rate [%]" which is automatically appended to the specified value.
        This value will be used to check if the success rate is equal to or higher than the
        kpi_success_rate_threshold value in which case the check is successful, otherwise
        the check will fail.
        If not specified then the value to use will be ".+" i.e. any string that ends with
        "Success Rate [%]" will be checked.
        },

        #---------------------------------------------------------------------------
        'kpi_success_rate_threshold' => qq{
        If specified and set to an integer value then this is limit to use to check if the
        success rate is below this limit which would cause the check to fail.
        },

        #---------------------------------------------------------------------------
        'kpi_time_interval' => qq{
        If specified it should be either, the start time from where KPI counters should be
        collected instead of the current time, or it can be a time interval between two
        time stamps that KPI counters should be collected from.

        It can be specified as an integer value which would indicate the epoch time (number
        of seconds from 1970-01-01 00:00:00 UTC).
            - 1668607860 (indicating Wed Nov 16 15:11:00 CET 2022).

        It can be specified as a date and/or time string (supported by the 'date' Linux comand).
        It would then be used as the start date instead of current date.
            - '2022-10-17 16:00:00'

        It can also be specified as an absolute time interval between two epoch time values
        like this '<timestamp1> to <timestamp2>', for example:
            - '1665871200 to 1665871260'
            This corresponds to '2022-10-16T00:00:00 to 2022-10-16T00:01:00'.
        In which case the 'kpi_future' and 'kpi_past' values are ignored.

        Or any other valid time range supported by the 'date' command, for example:
            - '2022-10-17 16:00:00 to 2022-10-17 16:15:21'
            - 'Mon Oct 17 16:00:00 CEST 2022 to Mon Oct 17 16:15:21 CEST 2022'
            - '2022-10-16T12:00:00 to Mon Oct 17 16:15:21 CEST 2022'
        In which case the 'kpi_future' and 'kpi_past' values are ignored.

        If not given then the default start value will be current time.
        },

        #---------------------------------------------------------------------------
        'kpi_use_cached_values' => qq{
        If specified and set to value "yes" then the different kpi checks (kpi and kpi_success_rate)
        will re-use the data printed from a previous check (during the same run of the script).
        This will cut down on the execution time needed to fetch data that is more or less
        exactly the same (with just a few seconds time difference).
        },

        #---------------------------------------------------------------------------
        'kubectl_version_client_limit' => qq{
        If specified and set to a version string then the check will use this version
        as the minimum value to compare against. If the version is lower than this version
        then the check will fail.
        },

        #---------------------------------------------------------------------------
        'kubectl_version_server_limit' => qq{
        If specified and set to a version string then the check will use this version
        as the minimum value to compare against. If the version is lower than this version
        then the check will fail.
        },

        #---------------------------------------------------------------------------
        'log_statistics_at_success' => qq{
        If specified and set to value 'yes' or '1' then log statistics details will be
        printed also at successful execution of the check. By default the details are
        only printed when the check fails.
        },

        #---------------------------------------------------------------------------
        'logs_details_directory' => qq{
        If specified and set to an directory path string then any log details will be written
        to files in this directory path where the file name includes the log plane name and
        the severity and file extension .json, for example 'sc-logs_error.json'.
        If not specified then no detailed log entries are fetched.
        A maximum of 10000 log entries can be fetched and written to the file.
        },

        #---------------------------------------------------------------------------
        'logs_per_second_average_limit' => qq{
        If specified and set to an integer or floating point value it is used for checking if
        the number of log entries per second is above this value then the check fails.
        },

        #---------------------------------------------------------------------------
        'logs_severity' => qq{
        If specified it should be set to one or more severity levels that will be checked
        when counting the number of log entries. If more than one value uis specified then it
        should be space delimited.
        For example: "critical", "error", "warning", "info" or "critical error warning"
        },

        #---------------------------------------------------------------------------
        'logs_severity_critical_limit' => qq{
        If specified it should be set to how many log entries with severity set to 'critical'
        is allowed.
        If not specified then the default value will be used.
        },

        #---------------------------------------------------------------------------
        'logs_severity_error_limit' => qq{
        If specified it should be set to how many log entries with severity set to 'error'
        is allowed.
        If not specified then the default value will be used.
        },

        #---------------------------------------------------------------------------
        'logs_severity_warning_limit' => qq{
        If specified it should be set to how many log entries with severity set to 'warning'
        is allowed.
        If not specified then the default value will be used.
        },

        #---------------------------------------------------------------------------
        'logs_time_interval' => qq{
        If specified it should be the amount of time from current time that will be used
        for checking the number of log entries.
        It can be specified as either an integer value of the number of seconds or using the
        following formats:
            - 1s, 3600s etc.
            - 1m, 60m etc.
            - 1h, 3h etc.
            - 1d (currently only makes sense with the value 1d which calculates the number
            or log entries from the start of the day but with the interval as if 24h).

        It can also be specified as an absolute time interval between two epoch time values
        like this '<timestamp1> to <timestamp2>', for example:
            - '1665871200 to 1665871260'
            This corresponds to '2022-10-16T00:00:00 to 2022-10-16T00:01:00'.

        Or any other valid time range supported by the 'date' command, for example:
            - '2022-10-17 16:00:00 to 2022-10-17 16:15:21'
            - 'Mon Oct 17 16:00:00 CEST 2022 to Mon Oct 17 16:15:21 CEST 2022'
            - '2022-10-16T12:00:00 to Mon Oct 17 16:15:21 CEST 2022'

        If not given then the default value of 10m will be used, meaning 10 minutes back
        in time from current time.
        },

        #---------------------------------------------------------------------------
        'logs_to_check' => qq{
        If specified then it should be either one or more log plane type to check or the
        string "all" which will check every log plane. If multiple log planes are specified
        then it should be given as a valid Perl regular expression e.g.
        "(<log plane 1>|<log plane 2>|<...>)".
        Examples of currently known log plane types:
            - all
            - adp-app-logs
            - sc-rlf-logs
            - sc-scp-logs
            - adp-app-asi-logs
            - adp-app-audit-logs
            - sc-logs
            - (sc-logs|sc-scp-logs)
            - etc...
        },

        #---------------------------------------------------------------------------
        'max_map_count_limit' => qq{
        If specified and set to an integer value then the check will use this number as
        minimum value to compare against. If the value is lower than this number then
        the check will fail.
        },

        #---------------------------------------------------------------------------
        'max_connections_value' => qq{
        If specified and set to an integer value then the check will use this number as
        the value to compare against. If the value is not this number then the check will
        fail.
        },

        #---------------------------------------------------------------------------
        'nerdctl_version_limit' => qq{
        If specified and set to a version string then the check will use this version
        as the minimum value to compare against. If the version is lower than this version
        then the check will fail.
        },

        #---------------------------------------------------------------------------
        'node_utilization_cpu_threshold' => qq{
        If specified and set to
        If specified and set to an integer value then this is limit to use to check if the
        utilization is at or above this limit which would cause the check to fail.
        },

        #---------------------------------------------------------------------------
        'node_utilization_disk_threshold' => qq{
        If specified and set to an integer value then this is limit to use to check if the
        utilization is at or above this limit which would cause the check to fail.
        },

        #---------------------------------------------------------------------------
        'node_utilization_memory_threshold' => qq{
        If specified and set to an integer value then this is limit to use to check if the
        utilization is at or above this limit which would cause the check to fail.
        },

        #---------------------------------------------------------------------------
        'node_utilization_pods_threshold' => qq{
        If specified and set to an integer value then this is limit to use to check if the
        utilization is at or above this limit which would cause the check to fail.
        },

        #---------------------------------------------------------------------------
        'pm_files_max_time_deviation' => qq{
        If specified and set to an integer value then this is time deviation in seconds that
        is allowed, so if the last stored PM file is older than this limit then the check is
        reported as failed.
        },

        #---------------------------------------------------------------------------
        'pm_files_size_limit' => qq{
        If specified and set to an integer value then this is file size in bytes that
        is allowed, so if the last stored PM file is smaller than this limit then the check
        is reported as failed.
        },

        #---------------------------------------------------------------------------
        'pod_logs_since' => qq{
        If specified and set to a string value then this is used for how far back the
        logs are checked. For example "120s", "2m" or "3h".
        By defaults all available logs are checked.
        },

        #---------------------------------------------------------------------------
        'pod_restarts_allow_file' => qq{
        If specified and pointing to a valid file that contain a list of Perl regular
        expressions (one per line) for pod names of restarting pods to ignore and not
        report as a fault.
        },

        #---------------------------------------------------------------------------
        'pod_restarts_time_threshold' => qq{
        If specified and set to an integer value then this is the threshold value in seconds
        to use for checking when the last POD restart occurred.
        By default the check will be for 7 days.
        },

        #---------------------------------------------------------------------------
        'pod_utilization_cpu_threshold' => qq{
        If specified and set to an integer value then this is limit to use to check if the
        utilization is at or above this limit which would cause the check to fail.
        },

        #---------------------------------------------------------------------------
        'pod_utilization_disk_threshold' => qq{
        If specified and set to an integer value then this is limit to use to check if the
        utilization is at or above this limit which would cause the check to fail.
        },

        #---------------------------------------------------------------------------
        'pod_utilization_memory_threshold' => qq{
        If specified and set to an integer value then this is limit to use to check if the
        utilization is at or above this limit which would cause the check to fail.
        },

        #---------------------------------------------------------------------------
        'pod_utilization_pods_threshold' => qq{
        If specified and set to an integer value then this is limit to use to check if the
        utilization is at or above this limit which would cause the check to fail.
        },

        #---------------------------------------------------------------------------
        'time_synchronization_max_deviation' => qq{
        If specified and set to an integer value in seconds then this is the maximum time
        deviation between the different nodes that is allowed before marking the check as
        failed.
        },

        #---------------------------------------------------------------------------
        'top_nodes_cpu_limit' => qq{
        If specified and set to an integer value then this is limit to use to check if the
        utilization is at or above this limit which would cause the check to fail.
        },

        #---------------------------------------------------------------------------
        'top_nodes_memory_limit' => qq{
        If specified and set to an integer value then this is limit to use to check if the
        utilization is at or above this limit which would cause the check to fail.
        },

        #---------------------------------------------------------------------------
        'top_pods_whitelist_tools' => qq{
        If specified and set to "no" then all PODS will be checked against the pod limits.
        By default all tools will be ignored in this check which currently includes the
        following pods names:
            - eric-aat
            - eric-chfsim
            - eric-chfsim-redis
            - eric-dscload-deployment
            - eric-influxdb
            - eric-k6
            - eric-nrfsim
            - eric-sc-aat-ts
            - eric-seppsim
            - jcatserver
        },

        #---------------------------------------------------------------------------
        'worker_disk_usage_available_size_minimum' => qq{
        If specified and set to a valid unit value then this is minimum available
        disk size that is allowed and if the value is less than this value it would
        cause the check to fail.
        Valid unit numbers are:
            - Integer value indicating the size in bytes.
            - Integer or floating point value followed by a unit number
              m,k,M,G,T,P,E,Ki,Mi,Gi,Ti,Pi,Ei.
              For example: 10G
        },

        #---------------------------------------------------------------------------
        'worker_disk_usage_threshold' => qq{
        If specified and set to an integer value then this is limit to use to check if the
        utilization is at or above this limit which would cause the check to fail.
        },
    );

    # Add help for the special ignore_failed_XXXX and skip_XXXX variables
    for my $name (sort keys %available_checks) {
        # ignore_failed_XXXX
        unless (exists $variables{"ignore_failed_$name"}) {
            $variables{"ignore_failed_$name"} = "no";
        }
        $available_variables{"ignore_failed_$name"}{'value'} = $variables{"ignore_failed_$name"};
        $available_variables{"ignore_failed_$name"}{'check'} = $name;
        $variables_description{"ignore_failed_$name"} = qq{
        If specified and set to "yes" then any failure in the "$name" check will be
        ignored and instead reported as successful.
        By default if not set then failure in the check will be reported as a failure.
        };

        # skip_XXXX
        unless (exists $variables{"skip_$name"}) {
            $variables{"skip_$name"} = "no";
        }
        $available_variables{"skip_$name"}{'value'} = $variables{"skip_$name"};
        $available_variables{"skip_$name"}{'check'} = $name;
        $variables_description{"skip_$name"} = qq{
        If specified and set to "yes" then the "$name" check will be skipped.
        By default if not specified then the check will be executed, unless marked as
        skipped in the used --group setting.
        };
    }
}

# -----------------------------------------------------------------------------
# This subroutine will print to the log file the currently used variables and
# checks to be executed.
sub log_used_settings {
    my $log_text = "The following variable definitions are used:\n";

    for my $var (sort keys %variables) {
        $log_text .= "  $var=$variables{$var}\n";
    }

    $log_text .= "\nThe following checks will be executed:\n";
    for (@checks) {
        $log_text .= "  $_\n";
    }

    General::Logging::log_write($log_text);
}

# -----------------------------------------------------------------------------
# This subroutine looks for any MPS or TPS rate dips to zero or below a specific
# level and counts the total downtime and if this time is above a specific
# threshold then it is reportes as an error.
# It returns back how many how many MPS/TPS rate counters contains downtime
# and it updates the details array references.
sub look_for_kpi_mps_tps_rate_downtime {
    my $queries         = shift;    # Array reference of queies to check
    my $descriptions    = shift;    # Array reference of query descriptions
    my $details         = shift;    # Array reference to details array that contains everything to be printed, both at success and failure
    my $check_details   = shift;    # Array reference to check details that summarize if a check was OK or NOK
    my $average_values  = shift;    # Array reference to average values

    my $average_value;
    my $description;
    my $downtime_seconds_total = 0;
    my $downtime_avarage_rate_threshold = $variables{'kpi_mps_tps_rate_threshold'};
    my $downtime_time_limit = $variables{'kpi_mps_tps_downtime_time_limit'};
    my @downtimes;
    my $failure_cnt = 0;
    my $filename;
    my @lines;
    my $low_limit;
    my $previous_timestamp;
    my $query;
    my $start_timestamp = "";
    my $timestamp;
    my $value;
    my $zero_value_cnt;

    push @$details, ( "\n", "Downtime MPS/TPS Rate Summary\n", "=============================\n", "\n", "Time intervals when the MPS/TPS rate falls below $downtime_avarage_rate_threshold\% of the average value during each $variables{'kpi_resolution'}s interval.\n", "\n" );

    push @$details, sprintf "%-14s  %-17s  %-55s  %s\n",
        "Total Downtime",
        "Interval Downtime",
        "Time Interval",
        "Description";
    push @$details, sprintf "%s  %s  %s  %s\n",
        "-"x14,
        "-"x17,
        "-"x55,
        "-"x11;

    # Add a separator line
    push @$check_details, "";

    for (my $cnt = 0; $cnt < scalar @$queries; $cnt++) {
        $query = $queries->[$cnt];
        $description = $descriptions->[$cnt];
        $description =~ s/\s+$//;
        $average_value = $average_values->[$cnt];
        $low_limit = ($average_value * $downtime_avarage_rate_threshold) / 100;
        $filename = $description;
        $filename =~ s/\s+/_/g;

        if (General::File_Operations::read_file( { "filename" => "$variables{'kpi_data_directory'}/gnuplot/$filename.data", "output-ref" => \@lines } ) != 0) {
            # Failure to read the file
            push @$details, sprintf "%14s  %17s  %-55s  %s\n",
                "-",
                "-",
                "-",
                $description;

            push @$check_details, "NOK  Downtime calculactions not possible because no data file found for KPI: $description";
            $failure_cnt++;
            next;
        }

        $downtime_seconds_total = 0;
        $previous_timestamp = "";
        $start_timestamp = "";
        @downtimes = ();
        $zero_value_cnt = 0;
        for (@lines) {
            if (/^(.+)\t(.+)$/) {
                # 2022-11-26T03:16:00Z\t99.98666755549628
                $timestamp = $1;
                $value = $2;
                if ($value < $low_limit) {
                    # The value is below threshold % of the average value and we need to count this as downtime.
                    if ($value == 0) {
                        $zero_value_cnt++;
                    }
                    if ($start_timestamp eq "") {
                        # We are now starting a new period of downtime and we wait to count the downtime
                        # until we find an interval that is above the threshold at which time we count
                        # complete interval.
                        $start_timestamp = $timestamp;
                    } else {
                        # We are already in a downtime interwal, so nothing to step yet
                    }
                } else {
                    # The value is not below the threshold value.
                    if ($start_timestamp ne "") {
                        # We were in a period of downtime and now we are not
                        if ($start_timestamp eq $previous_timestamp) {
                            # The previous period was only one interval, so we need to count this single downtime
                            $downtime_seconds_total += $variables{'kpi_resolution'};
                            push @downtimes, "$variables{'kpi_resolution'},$previous_timestamp";
                        } else {
                            # The previous value was in an interval of multiple downtimes, so we need to
                            # calculate the total downtime for the interval.
                            my $seconds = abs( General::OS_Operations::seconds_between_dates( $start_timestamp, $previous_timestamp ) );
                            # Add an extra kpi_resolution
                            $seconds += $variables{'kpi_resolution'};
                            $downtime_seconds_total += $seconds;
                            push @downtimes, "$seconds,$start_timestamp - $previous_timestamp";
                        }
                        # Mark that we are not in any downtime interval.
                        $start_timestamp = "";
                    }
                }
                # Remember the current time stamp for next timestamp
                $previous_timestamp = $timestamp;
            }
        }
        # Check if we were in a downtime interval when ending the loop then we need to count the total downtime values
        if ($start_timestamp ne "" && $previous_timestamp ne "") {
            # We were in a period of downtime
            if ($start_timestamp eq $previous_timestamp) {
                # The previous period was only one interval, so we need to count this single downtime
                $downtime_seconds_total += $variables{'kpi_resolution'};
                push @downtimes, "$variables{'kpi_resolution'},$previous_timestamp";
            } else {
                # The previous value was in an interval of multiple downtimes, so we need to
                # calculate the total downtime for the interval.
                my $seconds = abs( General::OS_Operations::seconds_between_dates( $start_timestamp, $previous_timestamp ) );
                # Add one extra kpi_resolution
                $seconds += $variables{'kpi_resolution'};
                $downtime_seconds_total += $seconds;
                push @downtimes, "$seconds,$start_timestamp - $previous_timestamp";
            }
        }

        if (@downtimes) {
            # We have some downtimes to report, first print the first interval
            if ($downtimes[0] =~ /^(\d+),(.+)$/) {

                push @$details, sprintf "%14s  %17s  %-55s  %s\n",
                    General::OS_Operations::convert_seconds_to_wdhms($downtime_seconds_total,1),
                    General::OS_Operations::convert_seconds_to_wdhms($1,1),
                    $2,
                    $description;

                # Remove the first element from the array
                shift @downtimes;
            }
            # Now handle any other downtime intervals
            for (@downtimes) {
                if (/^(\d+),(.+)$/) {

                    push @$details, sprintf "%14s  %17s  %-55s  %s\n",
                        "",
                        General::OS_Operations::convert_seconds_to_wdhms($1,1),
                        $2,
                        "";
                }
            }

            if ($zero_value_cnt > 0) {
                push @$check_details, "NOK  MPS/TPS Rate fell down to zero $zero_value_cnt times for KPI: $description";
                $failure_cnt++;
            } elsif ($downtime_seconds_total < $downtime_time_limit) {
                push @$check_details, sprintf 'OK   MPS/TPS Rate never falls below %s%% of the average rate %s (i.e. %s) for more than the allowed %s for KPI: %s', $downtime_avarage_rate_threshold,
                                                                                                                                                                     $average_value,
                                                                                                                                                                     $low_limit,
                                                                                                                                                                     General::OS_Operations::convert_seconds_to_wdhms($downtime_time_limit,1), $description;
            } else {
                push @$check_details, sprintf 'NOK  MPS/TPS Rate fell below %s%% of the average rate %s (i.e. %s) for %s which is more than the allowed %s for KPI: %s', $downtime_avarage_rate_threshold,
                                                                                                                                                                         $average_value,
                                                                                                                                                                         $low_limit,
                                                                                                                                                                         General::OS_Operations::convert_seconds_to_wdhms($downtime_seconds_total,1), General::OS_Operations::convert_seconds_to_wdhms($downtime_time_limit,1),
                                                                                                                                                                         $description;
                $failure_cnt++;
            }

        } else {
            # No downtime
            push @$details, sprintf "%14s  %17s  %-55s  %s\n",
                "0s",
                "0s",
                "-",
                $description;

            push @$check_details, "OK   MPS/TPS Rate never falls below $downtime_avarage_rate_threshold\% of the average rate $average_value (i.e. $low_limit) for KPI: $description";
        }
    }

    return $failure_cnt;
}

# -----------------------------------------------------------------------------
# This subroutine looks for any success rate dips below a specific level and
# counts the total downtime and if this time is above a specific threshold then
# it is reportes as an error.
# It returns back how many how many success rate counters contains downtime
# and it updates the details array references.
sub look_for_kpi_success_rate_downtime {
    my $queries         = shift;    # Array reference of queies to check
    my $descriptions    = shift;    # Array reference of query descriptions
    my $details         = shift;    # Array reference to details array that contains everything to be printed, both at success and failure
    my $check_details   = shift;    # Array reference to check details that summarize if a check was OK or NOK

    my $description;
    my $downtime_seconds_total = 0;
    my $downtime_success_rate_threshold = $variables{'kpi_success_rate_threshold'};
    my $downtime_time_limit = $variables{'kpi_downtime_time_limit'};
    my @downtimes;
    my $failure_cnt = 0;
    my $filename;
    my @lines;
    my $previous_timestamp;
    my $query;
    my $start_timestamp = "";
    my $timestamp;
    my $value;

    if ($variables_user_set{'kpi_downtime_success_rate_threshold'} == 1) {
        # Only update the value if the default value was changed by the user
        $downtime_success_rate_threshold = $variables{'kpi_downtime_success_rate_threshold'};
    }

    push @$details, ( "\n", "Downtime Summary\n", "================\n", "\n", "Time intervals when the success rate falls below $downtime_success_rate_threshold\% during each $variables{'kpi_resolution'}s interval.\n", "\n" );

    push @$details, sprintf "%-14s  %-17s  %-55s  %s\n",
        "Total Downtime",
        "Interval Downtime",
        "Time Interval",
        "Description";
    push @$details, sprintf "%s  %s  %s  %s\n",
        "-"x14,
        "-"x17,
        "-"x55,
        "-"x11;

    # Add a separator line
    push @$check_details, "";

    for (my $cnt = 0; $cnt < scalar @$queries; $cnt++) {
        $query = $queries->[$cnt];
        $description = $descriptions->[$cnt];
        $description =~ s/\s+$//;
        $filename = $description;
        $filename =~ s/\s+/_/g;

        if (General::File_Operations::read_file( { "filename" => "$variables{'kpi_data_directory'}/gnuplot/$filename.data", "output-ref" => \@lines } ) != 0) {
            # Failure to read the file
            push @$details, sprintf "%14s  %17s  %-55s  %s\n",
                "-",
                "-",
                "-",
                $description;

            push @$check_details, "NOK  Downtime calculactions not possible because no data file found for KPI: $description";
            $failure_cnt++;
            next;
        }

        $downtime_seconds_total = 0;
        $previous_timestamp = "";
        $start_timestamp = "";
        @downtimes = ();
        for (@lines) {
            if (/^(.+)\t(.+)$/) {
                # 2022-11-26T03:16:00Z\t99.98666755549628
                $timestamp = $1;
                $value = $2;
                if ($value < $downtime_success_rate_threshold) {
                    # The value is below threshold and we need to count this as downtime.
                    if ($start_timestamp eq "") {
                        # We are now starting a new period of downtime and we wait to count the downtime
                        # until we find an interval that is above the threshold at which time we count
                        # complete interval.
                        $start_timestamp = $timestamp;
                    } else {
                        # We are already in a downtime interwal, so nothing to step yet
                    }
                } else {
                    # The value is not below the threshold value.
                    if ($start_timestamp ne "") {
                        # We were in a period of downtime and now we are not
                        if ($start_timestamp eq $previous_timestamp) {
                            # The previous period was only one interval, so we need to count this single downtime
                            $downtime_seconds_total += $variables{'kpi_resolution'};
                            push @downtimes, "$variables{'kpi_resolution'},$previous_timestamp";
                        } else {
                            # The previous value was in an interval of multiple downtimes, so we need to
                            # calculate the total downtime for the interval.
                            my $seconds = abs( General::OS_Operations::seconds_between_dates( $start_timestamp, $previous_timestamp ) );
                            # Add an extra kpi_resolution
                            $seconds += $variables{'kpi_resolution'};
                            $downtime_seconds_total += $seconds;
                            push @downtimes, "$seconds,$start_timestamp - $previous_timestamp";
                        }
                        # Mark that we are not in any downtime interval.
                        $start_timestamp = "";
                    }
                }
                # Remember the current time stamp for next timestamp
                $previous_timestamp = $timestamp;
            }
        }
        # Check if we were in a downtime interval when ending the loop then we need to count the total downtime values
        if ($start_timestamp ne "" && $previous_timestamp ne "") {
            # We were in a period of downtime
            if ($start_timestamp eq $previous_timestamp) {
                # The previous period was only one interval, so we need to count this single downtime
                $downtime_seconds_total += $variables{'kpi_resolution'};
                push @downtimes, "$variables{'kpi_resolution'},$previous_timestamp";
            } else {
                # The previous value was in an interval of multiple downtimes, so we need to
                # calculate the total downtime for the interval.
                my $seconds = abs( General::OS_Operations::seconds_between_dates( $start_timestamp, $previous_timestamp ) );
                # Add one extra kpi_resolution
                $seconds += $variables{'kpi_resolution'};
                $downtime_seconds_total += $seconds;
                push @downtimes, "$seconds,$start_timestamp - $previous_timestamp";
            }
        }

        if (@downtimes) {
            # We have some downtimes to report, first print the first interval
            if ($downtimes[0] =~ /^(\d+),(.+)$/) {

                push @$details, sprintf "%14s  %17s  %-55s  %s\n",
                    General::OS_Operations::convert_seconds_to_wdhms($downtime_seconds_total,1),
                    General::OS_Operations::convert_seconds_to_wdhms($1,1),
                    $2,
                    $description;

                # Remove the first element from the array
                shift @downtimes;
            }
            # Now handle any other downtime intervals
            for (@downtimes) {
                if (/^(\d+),(.+)$/) {

                    push @$details, sprintf "%14s  %17s  %-55s  %s\n",
                        "",
                        General::OS_Operations::convert_seconds_to_wdhms($1,1),
                        $2,
                        "";
                }
            }

            if ($downtime_seconds_total < $downtime_time_limit) {
                push @$check_details, sprintf 'OK   Success Rate never falls below %s%% for more than the allowed %s for KPI: %s', $downtime_success_rate_threshold,
                                                                                                                                   General::OS_Operations::convert_seconds_to_wdhms($downtime_time_limit,1),
                                                                                                                                   $description;
            } else {
                push @$check_details, sprintf 'NOK  Success Rate fell below %s%% for %s which is more than the allowed %s for KPI: %s', $downtime_success_rate_threshold,
                                                                                                                                        General::OS_Operations::convert_seconds_to_wdhms($downtime_seconds_total,1),
                                                                                                                                        General::OS_Operations::convert_seconds_to_wdhms($downtime_time_limit,1),
                                                                                                                                        $description;
                $failure_cnt++;
            }

        } else {
            # No downtime
            push @$details, sprintf "%14s  %17s  %-55s  %s\n",
                "0s",
                "0s",
                "-",
                $description;

            push @$check_details, "OK   Success Rate never falls below $downtime_success_rate_threshold\% for KPI: $description";
        }
    }

    return $failure_cnt;
}

# -----------------------------------------------------------------------------
#
# Read the global allow list for alarms and core dumps.
#
sub parse_global_allow_list {
    my $default_allow_file = dirname(dirname abs_path $0) . '/../templates/system_health_check_files/default_allowlist.json';
    my @discarded = ();
    my $json_ref;
    my $rc;

    unless (-f $default_allow_file) {
        # File does not exist, just return
        print_verbose("Failed to find the global allow list: $default_allow_file\nNo global allow list will be used.\n");
        return;
    }
    $default_allow_file = abs_path $default_allow_file;

    # First read the alarms allow list
    print_verbose("Parsing 'alarms' global allow list in file: $default_allow_file\n");
    @discarded = ();
    $rc = General::File_Operations::parse_allow_list_file(
        {
            "filename"              => $default_allow_file,
            "output-ref"            => \@allow_list_alarms,
            "entry-type"            => "alarm_allowlist",
            "discarded-ref"         => \@discarded,
            "hide-error-messages"   => 1,
            "application-type"      => $result_information->{'application_type'},
            "node-name"             => $result_information->{'node_name'},
            "software-version"      => $result_information->{'software_version'},
        }
    );
    if ($rc != 0) {
        print_verbose("Failed to parse the 'alarms' global allow list: $default_allow_file\nNo 'alarms' global allow list will be used.\n");
        return;
    }
    if (@discarded) {
        print_verbose(join "", @discarded);
    }

    # Next read the coredump allow list
    print_verbose("\nParsing 'coredump' global allow list in file: $default_allow_file\n");
    @discarded = ();
    $rc = General::File_Operations::parse_allow_list_file(
        {
            "filename"              => $default_allow_file,
            "output-ref"            => \@allow_list_coredumps,
            "entry-type"            => "coredump_allowlist",
            "discarded-ref"         => \@discarded,
            "hide-error-messages"   => 1,
            "application-type"      => $result_information->{'application_type'},
            "node-name"             => $result_information->{'node_name'},
            "software-version"      => $result_information->{'software_version'},
        }
    );
    if ($rc != 0) {
        print_verbose("Failed to parse the 'coredump' global allow list: $default_allow_file\nNo 'coredump' global allow list will be used.\n");
        return;
    }
    if (@discarded) {
        print_verbose(join "", @discarded);
    }

    # Next read the pod_restart allow list
    print_verbose("\nParsing 'pod_restart' global allow list in file: $default_allow_file\n");
    @discarded = ();
    $rc = General::File_Operations::parse_allow_list_file(
        {
            "filename"              => $default_allow_file,
            "output-ref"            => \@allow_list_pod_restarts,
            "entry-type"            => "pod_restart_allowlist",
            "discarded-ref"         => \@discarded,
            "hide-error-messages"   => 1,
            "application-type"      => $result_information->{'application_type'},
            "node-name"             => $result_information->{'node_name'},
            "software-version"      => $result_information->{'software_version'},
        }
    );
    if ($rc != 0) {
        print_verbose("Failed to parse the 'pod_restart' global allow list: $default_allow_file\nNo 'pod_restart' global allow list will be used.\n");
        return;
    }
    if (@discarded) {
        print_verbose(join "", @discarded);
    }
}

# -----------------------------------------------------------------------------
#
# Print some information to screen and to log file if open.
#
sub print_always {
    my $text = shift;

    if ($json_output == 0) {
        # Only print when not outputting JSON text
        print $text;
    }

    # Also write the data to the log file
    if ($logfile) {
        # Remove ANSI color codes
        $text =~ s/\e\[\d+m//g;

        General::Logging::log_write($text);
    }
}

# -----------------------------------------------------------------------------
#
# Print a summary of how long each check took to execute.
#
sub print_execution_times {
    my $text;
    my $total = 0;

    General::Message_Generation::print_underlined_message(
        {
            "message" => "Health Check Execution Times",
            "underline-char" => "=",
            "return-output" => \$text,
        }
    );
    $text .= "\n";
    $text .= sprintf "%-11s  %-5s\n", "Time (s)", "Check";
    $text .= sprintf "%-11s  %-5s\n", "-"x11, "-"x5;
    for my $name (sort keys %check_execution_time) {
        $text .= sprintf "%11f  %s\n", $check_execution_time{$name}, $name;
        $total += $check_execution_time{$name};
    }
    $text .= sprintf "%-11s  %-5s\n", "-"x11, "-"x5;
    $text .= sprintf "%11f  %s\n", $total, "All Combined";
    $text .= "\n\n";
    print_always($text);
}

# -----------------------------------------------------------------------------
#
# Print message box containing summary result of executed DSC health check plug-ins
#
# Using library function from perl/lib/General/Message_Generation.pm
#
sub print_result_details {
    my $text = "";

    if (@result_details) {
        General::Message_Generation::print_underlined_message(
            {
                "message" => "Health Check Details",
                "underline-char" => "=",
                "return-output" => \$text,
            }
        );
        print_always(
            "$text\nOne or more checks was skipped, gave a warning or failed and below are details about these checks:\n" .
            (join "", @result_details) .
            "\n\n"
        );
    }
}

# -----------------------------------------------------------------------------
#
# Print message box containing summary result of executed DSC health check plug-ins
#
# Using library function from perl/lib/General/Message_Generation.pm
#
sub print_result_summary {
    my $check;
    my $details;
    my $result;
    my $text = "";

    if (@result_summary) {
        if ($table_output) {
            General::Message_Generation::print_underlined_message(
                {
                    "message" => "Health Check Status",
                    "underline-char" => "=",
                    "return-output" => \$text,
                }
            );
            $text .= "\n";

            if ($result_information->{'application_type'} ne "unknown") {
                $text .= "       Namespace: $namespace\n";
                $text .= "    Release Name: $result_information->{'release_name'}\n";
                $text .= "Application Type: $result_information->{'application_type'}\n";
                $text .= "       Node Name: $result_information->{'node_name'}\n";
                $text .= "Software Version: $result_information->{'software_version'}\n";
                $text .= "  Software Build: $result_information->{'software_build'}\n";
                $text .= "\n";
            } elsif ($namespace ne "") {
                $text .= "       Namespace: $namespace\n\n";
            }

            $text .= sprintf "%-10s  %-5s\n", "Result", "Check";
            $text .= sprintf "%-10s  %-5s\n", "-"x10, "-"x5;
            for (@result_summary) {
                if (/^\((.)\)\s+'(\S+)'\s+(.+)/) {
                    $result  = $1;
                    $check   = $2;
                    $details = $3;
                    if ($result eq "/") {
                        $result = "Successful";
                    } elsif ($result eq "x") {
                        $result = "Failed";
                    } elsif ($result eq "-") {
                        $result = "Skipped";
                    } elsif ($result eq "!") {
                        $result = "Warning";
                    } else {
                        $result = "Unknown";
                    }
                    if ($details =~ /^check (successful|failed|skipped)$/) {
                        $details = "";
                    } else {
                        $details = " ($details)";
                    }
                } else {
                    $result  = "Unknown";
                    $check   = "$_";
                    $details = "";
                }

                if ($no_color == 0) {
                    if ($result eq "Successful") {
                        # Print Green text
                        $text .= sprintf "\e[0m\e[32m";
                    } elsif ($result eq "Failed") {
                        # Print Red text
                        $text .= sprintf "\e[0m\e[31m";
                    } else {
                        # Print Yellow text
                        $text .= sprintf "\e[0m\e[33m";
                    }
                }

                $text .= sprintf "%-10s  %s%s\n", $result, $check, $details;

                $text .= sprintf "\e[0m" unless $no_color;
            }
            $text .= sprintf "%-10s  %-5s\n", "-"x10, "-"x5;
            if ($no_color == 0) {
                if ($return_code == 0) {
                    # Print Green text
                    $text .= sprintf "%s%-10s  %s\n", "\e[0m\e[32m", "Successful", "Combined Result";
                } elsif ($return_code == 2) {
                    # Print Yellow text
                    $text .= sprintf "%s%-10s  %s\n", "\e[0m\e[33m", "Warning", "Combined Result";
                } else {
                    # Print Red text
                    $text .= sprintf "%s%-10s  %s\n", "\e[0m\e[31m", "Failed", "Combined Result";
                }
                $text .= sprintf "\e[0m";
            } else {
                if ($return_code == 0) {
                    $text .= sprintf "%-10s  %s\n", "Successful", "Combined Result";
                } elsif ($return_code == 2) {
                    $text .= sprintf "%-10s  %s\n", "Warning", "Combined Result";
                } else {
                    $text .= sprintf "%-10s  %s\n", "Failed", "Combined Result";
                }
            }
            $text .= sprintf "\n";
        } else {
            General::Message_Generation::print_message_box(
                {
                    "header-text" => "Health Check Status",
                    "align-text" => "left",
                    "max-length" => 0,
                    "messages" => \@result_summary,
                    "return-output" => \$text,
                }
            );
            if ($no_color == 0) {
                if ($return_code == 0) {
                    # Print Green text
                    $text = "\e[0m\e[32m$text";
                } elsif ($return_code == 2) {
                    # Print Yellow text
                    $text = "\e[0m\e[33m$text";
                } else {
                    # Print Red text
                    $text = "\e[0m\e[31m$text";
                }
            }
            $text .= sprintf "\e[0m" unless $no_color;
            $text .= sprintf "\n";
        }

        print_always($text);
    }
}

# -----------------------------------------------------------------------------
#
# Print some information to screen if verbose level is enabled and to log file
# if open.
#
sub print_verbose {
    my $text = shift;

    if ($verbose == 1) {
        print STDERR $text;
    } elsif ($progress_type ne "none") {
        print STDERR "  $progress_chars[$progress_cnt++]\r";
        $progress_cnt = 0 if ($progress_cnt > $#progress_chars);
    }

    # Also write the data to the log file
    if ($logfile) {
        # Remove ANSI color codes
        $text =~ s/\e\[\d+m//g;

        General::Logging::log_write($text);
    }
}

# -----------------------------------------------------------------------------
sub report_details {
    my $information = shift;

    my $text = "";

    $information .= "\n" unless ($information =~ /\n$/);

    General::Message_Generation::print_message_box(
        {
            "messages" => [ $name ],
            "max-length" => 0,
            "return-output" => \$text,
        }
    );
    $text = sprintf "\n%s\n%s", $text, $information;
    push @result_details, $text;

    push @{$result_information->{'checks'}->[$check_count]->{'details'}}, (split /\r*\n/, $information);
}

# -----------------------------------------------------------------------------
sub report_failed {
    report_details(shift);
    $check_result = "failed";
}

# -----------------------------------------------------------------------------
sub report_skipped {
    report_details(shift);
    $check_result = "skipped";
}

# -----------------------------------------------------------------------------
sub report_warning {
    report_details(shift);
    $check_result = "warning";
}

# -----------------------------------------------------------------------------
sub show_help {
    print <<EOF;

Description:
============

This script performs a health check of the system which consists of a number of
different checks.
If all checks are successful, it exists with return code 0 but if one or more
checks fails it exits with return code 1 or one or more checks are classified
as a warning then it exists with return code 2.
Return code 1 has higher priority than 2 and 0.

It also prints a summary of the status of the different performed checks.

Available Checks:
EOF

    for my $name (sort keys %available_checks) {
        print "\n";
        General::Message_Generation::print_underlined_message(
            {
                "message"         => "$name:",
                "indent-string"   => "  ",
                "underline-char"  => "-",
            }
        );
        if (exists $check_description{$name}) {
            print General::String_Operations::strip_leading_characters("      ", $check_description{$name});
        } else {
            print "\n  No description available.\n"
        }
    }

    print <<EOF;

If multiple checks are specified via the parameters --check, --check_file, --group,
--pre_deployment and --post_deployment then the checks are performed in the
following order:

    1. --check
    2. --check_file
    3. --group
    4. --pre_deployment
    5. --post_deployment

Within the --group, --pre_deployment and --post_deployment groups the checks will be
performed in alphabetical order.


Syntax:
=======

$0 [<OPTIONAL>]

<OPTIONAL> are one or more of the following parameters:

  -a <string>           | --application <string>
  -b                    | --verbose
  -c <string>           | --check <string>
                          --check_file <filename>
  -f <filename>         | --log_file <filename>
  -g <string>           | --group <string>
  -h                    | --help
  -j                    | --json_output
                          --kubeconfig <filename>
  -l <integer>          | --used_helm_version <integer>
  -n <string>           | --namespace <string>
                          --no_color
                          --no_logfile
  -o                    | --post_deployment
  -r                    | --pre_deployment
                          --progress_type <type>
  -u <string>           | --own_registry_url <string>
  -v <string>=<value>   | --variables <string>=<value>
                          --variables_regexp_file <filename>
                          --show_variable_values

where:

  -a <string>
  --application <string>
  ----------------------
  Specifies the type of application that the checks should be performed on.
  By specifying this parameter the automatic detection logic can be
  by-passed e.g. if there are problems with it or the script has not been
  updated to support new types, or simply you want to have the check behave
  like it's a different application deployed.
  Currently supported automatically detected applications are:

EOF

    for my $name (sort split /\|/, $application_type_regexp) {
        print "    - $name\n";
    }

    print <<EOF;

  In the program the following checks will have different verdict as
  defined in the \%application_check_matrix hash:

EOF

    for my $app (sort keys %application_check_matrix) {
        print "    - $app\n";
        for my $name (sort keys %{$application_check_matrix{$app}}) {
            print "        - $name ($application_check_matrix{$app}{$name})\n";
        }
    }

    print <<EOF;


  -b
  --verbose
  ---------
  Specify this if verbose output showing which tests are being executed
  should be printed to STDERR.
  See also parameter --progress_type.
  By default only the results of the health check is printed to STDOUT.


  -c <string>
  --check <string>
  ----------------
  Specifies which check should be executed and in which order, and this
  parameter can be specified multiple times if more than one check should
  be performed.
  If not specified and -g/--group is also not specified then all available
  checks will be executed in alphabetical order, same as '-g full'.
  The <string> value can be a combination of a check and a verdict defined
  as <check>[=<verdict>] where the verdict part is optional and can be one
  of the following:
    - fail
    - skip
    - warning

  and the <check> can be one of the following:

EOF

    for my $name (sort keys %available_checks) {
        print "    - $name\n";
    }

    print <<EOF;

  For example:
  -c top_pods
  -c top_pods=warning


  --check_file <filename>
  -----------------------
  If specified then the file should contain a list of checks (one per line)
  that should be performed, they can be preceeded by the optional "check:"
  attribute.
  See details for parameter --check above.

  The file can also contain variable changes (one per line) that should be
  set, they MUST BE preceeded by the mandatory "variable:" attribute.
  See details for parameter --variables below.

  The file can also contain a group name in which case all checks defined
  in the specific group will be used, if specified it MUST BE preceeded by
  the mandatory 'group:' attribute.
  See details  for parameter --group below.

  Contents of the file could be for example:
  node_status
  check:top_pods
  check: top_nodes=warning
  variable:top_pods_whitelist_tools=no
  variable: top_nodes_cpu_limit=75
  group: full

  NOTE: If the same variable are set both with the file and with parameter
  --variables then the value set in the file will be used, and always the
  last value in the file will also be used.


  -f <filename>
  --log_file <filename>
  ---------------------
  If specified then all executed commands and their results will be written
  into the given <filename>.
  If not specified then a log file is created the /tmp/ directory with a file
  name of:
    system_health_check_XXXX.log
  where the XXXX is the epoch timestamp (seconds since Jan 1 1970).
  See also --no_logfile if no log file should be created.


  -g <string>
  --group <string>
  ----------------
  Specifies which group of predefined checks to execute.
  If not specified then by default the 'full' group is used, see also the
  -c/--check parameter.
  For each group the result of the check can be one of the following:
    - "fail"

      If the result of the check is failed then it will also be marked as
      failed in the over all result.
      If the result of the check is skipped then it will also be marked as
      skipped and have no impacy on the over all result.
      If the result of the check is successful then it will also be marked
      as successful and not cause a failure of the over all result (unless
      there are other checks that fail).

    - "warning"

      If the result of the check is failed then it's changed to a warning
      which will not cause a failure of the over all result (unless there
      are other checks that fail).

  The <string> value can be one of the following and the result are shown
  inside the "()":
EOF

    for my $group (sort keys %available_groups) {
        print "\n    - $group\n";
        print "      Which consists of the following checks:\n";
        for my $check (sort keys %check_group_matrix) {
            if ($check_group_matrix{$check}{$group} ne "skip") {
                print "        - $check ($check_group_matrix{$check}{$group})\n";
            }
        }
    }

    print <<EOF;


  -h
  --help
  ------
  Shows this help information.


  -j
  --json_output
  -------------
  If specified then the printed output will be JSON formatted text printed
  to STDOUT.
  This option will also disable any progress messages, the same as if
  parameter --progress_type=none is given.


  --kubeconfig <filename>
  -----------------------
  If specified then it will use this file as the config file for helm and
  kubectl commands.
  If not specified then it will use the default file which is normally the
  file ~/.kube/config.


  -l <integer>
  --used_helm_version <integer>
  -----------------------------
  Helm version that is used for the deployment


  -n <string>
  --namespace <string>
  --------------------
  The namespace of the Application deployment to check.
  If the namespace is not given then certain checks that require a namespace will
  be skipped.


  --no_color
  ----------
  Specify this parameter if no colored output should be done, by default
  the status printout will be shown in green text if all checks are successful
  and red text if one or more checks failed.


  --no_logfile
  ------------
  Specify this parameter if no log file with detailed information should be
  created and information is ONLY printed to STDOUT and STDERR, by default a
  log file is created either in the file specified with -f/--log-file or in the
  /tmp/ directory with a file name of:
    system_health_check_XXXX.log
  where the XXXX is the epoch timestamp (seconds since Jan 1 1970).


  -o
  --post_deployment
  ----------------
  Execute post deployment health check which is the following subset of the
  available checks:

    - helm_list_status
    - replicasets
    - endpoints
    - pvc
    - svc
    - registry_pod_images
    - top_pods

  This parameter is present for backwards compatibility and should probably
  no longer be used.
  Consider using --group=post-deployment instead.


  -r
  --pre_deployment
  ----------------
  Execute pre deployment health check which is the following subset of the
  available checks:

    - helm_version
    - kubectl_version
    - docker_version
    - node_status
    - top_nodes

  This parameter is present for backwards compatibility and should probably
  no longer be used.
  Consider using --group=pre-deployment instead.


  --progress_type <type>
  ----------------------
  Specifies the type of progress indicator to use when not using verbose
  mode (when parameter -b/--verbose not given).
  Progress information is printed to STDERR.
  The default value if not specified is: 1
  Can be one of the following:
    none:   Prints no progress indicator at all.
    random: Gives a randomly selected progress type from below.
    0:      Gives a sequence of ◡◡ ⊙⊙ ◠◠
    1:      Gives a sequence of ▁ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃
    2:      Gives a sequence of ◐ ◓ ◑ ◒
    3:      Gives a sequence of ← ↖ ↑ ↗ → ↘ ↓ ↙
    4:      Gives a sequence of ┤ ┘ ┴ └ ├ ┌ ┬ ┐
    5:      Gives a sequence of . o O o
    6:      Gives a sequence of "◜ " " ◝" " ◞" "◟ "
    7:      Gives a sequence of - \\ | / - | /
    8:      Gives a sequence of ◴ ◷ ◶ ◵
    9:      Gives a sequence of ◰ ◳ ◲ ◱
    10:     Gives a sequence of ⣾ ⣽ ⣻ ⢿ ⡿ ⣟ ⣯ ⣷ ⡀ ⡁ ⡂ ⡃ ⡄ ⡅ ⡆ ⡇ ⡈ ⡉ ⡊ ⡋ ⡌ ⡍ ⡎ ⡏ ⡐
                                ⡑ ⡒ ⡓ ⡔ ⡕ ⡖ ⡗ ⡘ ⡙ ⡚ ⡛ ⡜ ⡝ ⡞ ⡟ ⡠ ⡡ ⡢ ⡣ ⡤ ⡥ ⡦ ⡧ ⡨ ⡩
                                ⡪ ⡫ ⡬ ⡭ ⡮ ⡯ ⡰ ⡱ ⡲ ⡳ ⡴ ⡵ ⡶ ⡷ ⡸ ⡹ ⡺ ⡻ ⡼ ⡽ ⡾ ⡿ ⢀ ⢁ ⢂
                                ⢃ ⢄ ⢅ ⢆ ⢇ ⢈ ⢉ ⢊ ⢋ ⢌ ⢍ ⢎ ⢏ ⢐ ⢑ ⢒ ⢓ ⢔ ⢕ ⢖ ⢗ ⢘ ⢙ ⢚ ⢛
                                ⢜ ⢝ ⢞ ⢟ ⢠ ⢡ ⢢ ⢣ ⢤ ⢥ ⢦ ⢧ ⢨ ⢩ ⢪ ⢫ ⢬ ⢭ ⢮ ⢯ ⢰ ⢱ ⢲ ⢳ ⢴
                                ⢵ ⢶ ⢷ ⢸ ⢹ ⢺ ⢻ ⢼ ⢽ ⢾ ⢿ ⣀ ⣁ ⣂ ⣃ ⣄ ⣅ ⣆ ⣇ ⣈ ⣉ ⣊  ⣋ ⣌ ⣍
                                ⣎ ⣏ ⣐ ⣑ ⣒ ⣓ ⣔ ⣕ ⣖ ⣗ ⣘ ⣙ ⣚ ⣛ ⣜ ⣝ ⣞ ⣟ ⣠ ⣡ ⣢ ⣣ ⣤ ⣥ ⣦
                                ⣧ ⣨ ⣩ ⣪ ⣫ ⣬ ⣭ ⣮ ⣯ ⣰ ⣱ ⣲ ⣳ ⣴ ⣵ ⣶ ⣷ ⣸ ⣹ ⣺ ⣻ ⣼ ⣽ ⣾ ⣿


  -u <string>
  --own_registry_url <string>
  ---------------------------
  Own registry URL. If specified then a check is done that all loaded images
  is tagged for this URL.


  -v <string>=<value>
  --variables <string>=<value>
  ----------------------------
  This parameter controls how some of the checks are performed, and the user
  can specify other values if the following default values are not good enough.

  The <string>=<value> can be one of the following:

EOF

    # Print a description for each available variable that can be changed by the user.
    for my $name (sort keys %available_variables) {
        print "\n";
        General::Message_Generation::print_underlined_message(
            {
                "message"         => "$name=<value>",
                "indent-string"   => "    ",
                "underline-char"  => "-",
            }
        );
        print "    Current value: $variables{$name}\n";
        print "    Used by check: $available_variables{$name}{'check'}\n";
        if (exists $variables_description{$name}) {
            print General::String_Operations::strip_leading_characters("    ", $variables_description{$name});
        } else {
            print "\n    No description available.\n"
        }
    }

    print <<EOF;

  --variables_regexp_file <filename>
  ----------------------------------
  This parameter controls if special checks should be applied to the values used
  for setting variables as described in the --variables part above.
  The special check means that for certain POD names a different value should be used.
  This parameter specifies a filename that contain a list of variable name, POD regular
  expression and value that should be changed.

  The file should contain the following information:
  <variable name>,<pod regular expression>,<value>

  Currently the only supported <variable name> values are:
    - pod_utilization_cpu_threshold
    - pod_utilization_memory_threshold
    Any other values are ignored.

  Example of file contents:
  pod_utilization_memory_threshold,^eric-data-search-engine-data-,95
  pod_utilization_cpu_threshold,^eric-k6-deployment-,99

  Current default values are:

EOF
    printf "  %-45s  %-45s  %s\n", "<variable name>",
                                    "<pod regular expression>",
                                    "<value>";
    printf "  %s  %s  %s\n", "-"x45,
                             "-"x45,
                             "-"x7;
    for my $name (sort keys %variables_regexp) {
        for my $regexp (sort keys %{$variables_regexp{$name}}) {
            printf "  %-45s  %-45s  %s\n", $name,
                                           $regexp,
                                           $variables_regexp{$name}{$regexp};
        }
    }

    print <<EOF;


  --show_variable_values
  ----------------------
  This parameter controls if a list of variables and their current values should be printed
  on the screen.
  This parameter can be used by itself or together with the -h / --help parameters.


Return code:
============

   0: Successful, the execution was successful.
   1: Unsuccessful, some failure was reported.
   2: Some warnings was reported.

EOF

    # Next print a list of available variables and checks to make it easier for the user to
    # find it.
    General::Message_Generation::print_underlined_message(
        {
            "message"         => "Available Variables:",
            "underline-char"  => "=",
        }
    );
    print "\n";
    my $count = 0;
    for my $name (sort keys %available_variables) {
        $count++;
        if ($count < 2) {
            if (length($name) < 50) {
                printf "%-50s", $name;
            } else {
                print "$name\n";
                $count = 0;
            }
        } else {
            print "$name\n";
            $count = 0;
        }
    }
    if ($count == 0) {
        print "\n";
    } else {
        print "\n\n";
    }

    General::Message_Generation::print_underlined_message(
        {
            "message"         => "Available Groups:",
            "underline-char"  => "=",
        }
    );
    print "\n";
    $count = 0;
    for my $name (sort keys %available_groups) {
        $count++;
        if ($count < 3) {
            if (length($name) < 30) {
                printf "%-30s", $name;
            } else {
                print "$name\n";
                $count = 0;
            }
        } else {
            print "$name\n";
            $count = 0;
        }
    }
    if ($count == 0) {
        print "\n";
    } else {
        print "\n\n";
    }

    General::Message_Generation::print_underlined_message(
        {
            "message"         => "Available Checks:",
            "underline-char"  => "=",
        }
    );
    print "\n";
    $count = 0;
    for my $name (sort keys %available_checks) {
        $count++;
        if ($count < 3) {
            if (length($name) < 30) {
                printf "%-30s", $name;
            } else {
                print "$name\n";
                $count = 0;
            }
        } else {
            print "$name\n";
            $count = 0;
        }
    }
    if ($count == 0) {
        print "\n";
    } else {
        print "\n\n";
    }

    if ($show_variable_values == 1) {
        show_variable_values();
    }
}

# -----------------------------------------------------------------------------
sub show_variable_values {
    my $length;
    my $max_length = 13;
    for my $name (sort keys %variables) {
        $length = length($name);
        $max_length = $length if $length > $max_length;
    }
    printf "%-${max_length}s  %s\n", "Variable Name", "Current Value";
    printf "%s  %s\n", "-"x$max_length, "-"x13;
    for my $name (sort keys %variables) {
        printf "%-${max_length}s  %s\n", $name, $variables{$name};
    }
    print "\n";
}
